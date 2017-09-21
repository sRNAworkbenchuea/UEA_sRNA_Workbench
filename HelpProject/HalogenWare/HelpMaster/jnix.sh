#!/bin/bash
declare headertype
#if being used as a generic launcher jar is not set, if a jar is wrapped set jar="$0", if used as a launcher for a specific jar file set jar=relative path to jar
declare jar
declare errtitle
declare downloadurl="http://java.com/download"
declare supporturl
declare cmdline
declare chdir
declare priority
#var format is "export name1=value1;export name2=value2" if value contains spaces it must be quoted eg \"spaced value\"
declare var
declare mainclass
#cp is a colon(:) separated list of glob patterns
declare cp
declare path
declare minversion
declare maxversion
declare jdkpreference
declare initialheapsize
declare initialheappercent
declare maxheapsize
declare maxheappercent
#opt format is a space separated list of options to pass to java, options that contain spaces must be quoted eg \"option with space\"
declare opt
#declare startuperr="An error occurred while starting the application."
declare bundledjreerr="This application was configured to use a bundled Java Runtime Environment but the runtime is missing or corrupted."
declare jreversionerr="This application requires a Java Runtime Environment."
#declare launchererr="The registry refers to a nonexistent Java Runtime Environment installation or the runtime is corrupted."
#constants for comparison
declare -r console=console
declare -r gui=gui
declare -r jreonly=jreOnly
declare -r preferjre=preferJre
declare -r preferjdk=preferJdk
declare -r jdkonly=jdkOnly
declare -r normal=normal
declare -r idle=idle
declare -r high=high
#if this script is edited do not change anything above this line

#set to true to disable prompts to run updatedb
declare nolocateerror
#by default returns 0 for jre, 1 for jdk
#if jdkpreference equals $preferjdk returns 0 for jdk, 1 for jre
#returns 2 for unspecified
jtype () {
	declare jre=${1/jre/}
	declare jdk=${1/jdk/}
	if [[ "$jre" != "$1" && "$jdk" = "$1" ]]
	then
		if [[ -n $jdkpreference && "$jdkpreference" = "$preferjdk" ]]
		then
			return 1
		else
			return 0
		fi
	fi
	if [[ "$jdk" != "$1" ]]
	then
		if [[ -n $jdkpreference && "$jdkpreference" = "$preferjdk" ]]
		then
			return 0
		else
			return 1
		fi
	fi
	return 2
}

checkextra () {
	declare jv="$1"
	declare hd=${jv/-/}
	declare -i jve=0
	if [[ "$hd" != "$jv" ]]
	then
		jv=${jv%%-*}\_
		jve=1
	else
		jv=$jv\_
	fi
	echo "$jv"
	return $jve
}

extractvn () {
	declare vn
	if [[ x"$1" != x"" ]]
	then
		declare t=${1%%.*}
		if [[ x"$t" = x"$1" ]]
		then
			t=${1%%_*}
		fi
		t=${t##0}
		vn="$t"
	else
		vn=0
	fi
	echo "$vn"
	return 0
}

extractrvn () {
	declare nsn=${1#*.}
	if [[ x"$nsn" = x"$1" ]]
	then
		nsn=${sn1#*_}
	fi
	echo "$nsn"
	return 0
}

#returns zero if both args are equal, 1 if $1 is higher than $2 and 2 if $1 is lower than $2
compare () {
	declare jv1=$(checkextra "$1")
	declare -i jve1=$?
	declare jv2=$(checkextra "$2")
	declare -i jve2=$?
	declare sn1="$jv1"
	declare sn2="$jv2"
	if [[ x"$sn1" != x"$sn2" ]]
	then
		while [[ x"$sn1" != x"" || x"$sn2" != x"" ]]
		do
			declare -i vn1=$(extractvn "$sn1")
			declare -i vn2=$(extractvn "$sn2")
			if [[ $vn1 -gt $vn2 ]]
			then
				return 1
			fi
			if [[ $vn1 -lt $vn2 ]]
			then
				return 2
			fi
			sn1=$(extractrvn "$sn1")
			sn2=$(extractrvn "$sn2")
		done
	fi
	if [[ $jve1 -lt $jve2 ]]
	then
		return 1
	fi
	if [[ $jve1 -gt $jve2 ]]
	then
		return 2
	fi
	#compare jre and jdk
	if [[ -z $3 || -z $4 ]]
	then
		return 0
	fi
	jtype $3
	declare -i jt1=$?
	jtype $4
	declare -i jt2=$?
	if [[ $jt1 -lt $jt2 ]]
	then
		return 1
	fi
	if [[ $jt1 -gt $jt2 ]]
	then
		return 2
	fi
	return 0
}

#two parameters fixed and percentage higher value is returned
getheapmem () {
	declare -i heapsize=$1
	if [[ -n $2 ]]
	then
		#change $4 to $2 to get total memory
		declare -i mem=$(free -m | grep Mem | awk '{ print $4 }')
		mem=$2*mem/100
		if [[ $mem -gt $heapsize ]]
		then
			heapsize=$mem
		fi
	fi
	echo $heapsize
	return 0
}

expandcp () {
	declare fullclasspath
	declare classpath="$@":
	while [[  x"$classpath" != x"" ]]
	do
		declare cpc=${classpath%%:*}
		fullclasspath="$fullclasspath"$(printf %b: "$EXECDIR/$cpc" 2>/dev/null)
		classpath=${classpath#*:}
	done
	echo "$fullclasspath"
	return 0
}

#builds the command line and starts the specified java executable
runjava () {
	if [[ -n $var ]]
	then
		eval $var
	fi
	declare -i niceness
	if [[ -n $priority ]]
	then
		if [[ $priority = $idle ]]
		then
			niceness=19
		fi
		#only root can create high priority processes
		if [[ $priority = $high && $EUID -eq 0 ]]
		then
			niceness=-20
		fi
	fi
	declare cl
	if [[ -n $niceness ]]
	then
		cl="nice -n $niceness $1"
	else
		cl=$1
	fi
	declare fv1=0
	if [[ -n $initialheapsize ]]
	then
		fv1=$initialheapsize
	fi
	declare -i ih=$(getheapmem $fv1 $initialheappercent)
	if [[ $ih -gt 0 ]]
	then
		cl="$cl -Xms"$ih"m"
	fi
	declare fv2=0
	if [[ -n $maxheapsize ]]
	then
		fv2=$maxheapsize
	fi
	declare -i mh=$(getheapmem $fv2 $maxheappercent)
	if [[ $mh -gt 0 ]]
	then
		cl="$cl -Xmx"$mh"m"
	fi
	if [[ -n $opt ]]
	then
		cl="$cl $(eval echo "$opt")"
	fi
	declare l4jini=${EXECPATH/%.*/.l4j.ini}
	if [[ -e $l4jini ]]
	then
		declare inilines=$(cat "$l4jini")
		for il in $inilines
		do
			cl="$cl $(eval echo "$il")"
		done
	fi
	declare wholejar
	if [[ -n $jar ]]
	then
		if [[ ${jar#/} = $jar ]]
		then
			wholejar=$(readlink -f "$EXECDIR/$jar")
		else
			wholejar="$jar"
		fi
	fi
	if [[ -n $mainclass ]]
	then
		declare classpath
		if [[ -n $cp ]]
		then
			classpath=$(expandcp "$cp")
		fi
		if [[ -n $wholejar ]]
		then
			if [[ -n $classpath ]]
			then
				classpath="$wholejar:$classpath"
			else
				classpath="$wholejar"
			fi
		fi
		if [[ -n $classpath ]]
		then
			cl="$cl -cp \"$classpath\""
		fi
		cl="$cl $mainclass"
	else
		if [[ -n $wholejar ]]
		then
			cl="$cl -jar \"$wholejar\""
		fi
	fi
	if [[ -n $cmdline ]]
	then
		cl="$cl $(eval echo "$cmdline")"
	fi
	shift
	eval $cl "$@"
	return $?
}

#determines the type of dialog to display
declare popuptype
declare realtty
declare xtermcommand
getpopuptype () {
	if [[ $realtty -eq 0 ]]
	then
		echo console
		return 0
	fi
	if [[ x"$KDE_FULL_SESSION" = x"true" ]]
	then
		which kdialog &>/dev/null
		if [[ $? -eq 0 ]]
		then
			echo kdialog
			return 0
		fi
	fi
	#x"$GNOME_DESKTOP_SESSION_ID" != x"" && 
	which zenity &>/dev/null
	if [[ $? -eq 0 ]]
	then
		echo zenity
		return 0
	fi
	which xmessage &>/dev/null
	if [[ $? -eq 0 ]]
	then
		echo xmessage
		return 0
	fi
	#no other method exists for displaying a message so open a new console and print some messages
	#if [[ x"$(which x-terminal-emulator)" != x"" ]]
	#then
	#	echo newconsole
	#	return 0
	#fi
	#absolutely no way to display a message to the user so dump some data in an error log
	#echo dump
	return 0
}

showerror () {
	declare et
	if [[ -n $errtitle ]]
	then
		et="$errtitle"
	else
		et="$0 - Error"
	fi
	if [[ -z $popuptype ]]
	then
		popuptype=$(getpopuptype)
	fi
	declare message=${!1}
	which xdg-open &>/dev/null
	declare canopen=$?
	declare url
	if [[ -n $2 ]]
	then
		url=${!2}
		if [[ canopen -eq 0 ]]
		then
			if [[ x"$url" = x"$downloadurl" ]]
			then
				message="$message\\nWould you like to visit the java download page?"
			fi
			if [[ x"$url" = x"$supporturl" ]]
			then
				message="$message\\nWould you like to visit the support page?"
			fi
		else
			message="$message\\nPlease visit $url for help."
		fi
	fi
	declare -i result
	declare dialogtype
	case "$popuptype" in
	"console")
		declare mmessage=${message//"\\n"/" "}
		echo "$et : $mmessage"
		if [[ -n $url && canopen -eq 0 ]]
		then
			select choice in "yes" "no"
			do
				if [[ x"$choice" = x"yes" ]]
				then
					result=0
				else
					result=1
				fi
				break
			done
		fi
	;;
	"kdialog")
		if [[ -n $url && canopen -eq 0 ]]
		then
			dialogtype=--yesno
		else
			dialogtype=--error
		fi
		kdialog --title "$et" $dialogtype "$message"
		result=$?
	;;
	"zenity")
		if [[ -n $url && canopen -eq 0 ]]
		then
			dialogtype=--question
		else
			dialogtype=--error
		fi
		zenity $dialogtype --title "$et" --text "$message"
		result=$?
	;;
	"xmessage")
		if [[ -n $url && canopen -eq 0 ]]
		then
			dialogtype="Yes:100,No:101 -default Yes"
		else
			dialogtype="Ok"
		fi
		declare mmessage=${message//"\\n"/" "}
		xmessage -buttons $dialogtype -center "$mmessage"
		result=$?-100
	;;
	esac
	if [[ $canopen -eq 0 && -n $url && $result -eq 0 ]]
	then
		xdg-open $url
	fi
}

#returns 0 if updatedb was run succcessfully or 1 if not
runupdatedb () {
	if [[ x"$nolocateerror" = x"true" ]]
	then
		return 1
	fi
	which updatedb &>/dev/null
	if [[ $? -gt 0 ]]
	then
		return 1
	fi
	if [[ $EUID -ne 0 && realtty -ne 0 && -z xtermcommand ]]
	then
		return 1
	fi
	if [[ -z $popuptype ]]
	then
		popuptype=$(getpopuptype)
	fi
	declare et
	if [[ -n $errtitle ]]
	then
		et="$errtitle"
	else
		et="$0 - Invalid locate database"
	fi
	declare badlocatedb="The locate database is either non-existent or out of date."
	declare needrootpw="Please enter the root password to run updatedb (may take a few minutes to complete)."
	declare message
	if [[ $EUID -eq 0 ]]
	then
		message="$badlocatedb\\nWould you like to update it now (may take a few minutes to complete)?"
	else
		if [[ x"$popuptype" = x"console" ]]
		then
			message="$badlocatedb $needrootpw"
		else
			message="$badlocatedb\\nWould you like to update it now (requires root password and may take a few minutes to complete)?"
		fi
	fi
	declare message2=${message//"\\n"/" "}
	declare -i result
	declare dialogtype
	case "$popuptype" in
	"console")
		echo "$et : $message2"
		if [[ $EUID -eq 0 ]]
		then
			select choice in "yes" "no"
			do
				if [[ x"$choice" = x"yes" ]]
				then
					result=0
				else
					result=1
				fi
			done
		else
			su root -c updatedb
			return $?
		fi
	;;
	"kdialog")
		kdialog --title "$et" --yesno "$message"
		result=$?
	;;
	"zenity")
		zenity --question --title "$et" --text "$message"
		result=$?
	;;
	"xmessage")
		xmessage -buttons "Yes:100,No:101" -default Yes -center "$message2"
		result=$?-100
	;;
	esac
	if [[ $result -eq 0 ]]
	then
		if [[ $EUID -eq 0 ]]
		then
			updatedb
			return $?
		else
			#need to open x-terminal-emulator because su will not run unless connected to tty or pty
			#but x-terminal-emulator always returns zero so by creating a temp file it will be deleted if su is successful 
			declare tmpcode=$(mktemp)
			$xtermcommand -T "$et" -e sh -c "echo \"$needrootpw\" && su root -c updatedb && rm -f \"$tmpcode\"" 2>/dev/null
			if [[ -e $tmpcode ]]
			then
				rm -f "$tmpcode"
				return 1
			else
				return 0
			fi
		fi
	fi
	return 1
}

#extract version number from java -version command
getjavaversion () {
	declare jver=$("$1" -version 2>&1)
	if [[ $? -gt 0 ]]
	then
		return 1
	fi
	jver=${jver#*\"}
	jver=${jver%%\"*}
	echo "$jver"
	return 0
}

#compare against max and min versions
compareminmax () {
	if [[ -z $1 ]]
	then
		return 1
	fi
	if [[ -n $minversion ]]
	then
		compare $1 $minversion
		if [[ $? -eq 2 ]]
		then
			return 1
		fi
	fi
	if [[ -n $maxversion ]]
	then
		compare $maxversion $1
		if [[ $? -eq 2 ]]
		then
			return 1
		fi
	fi
	return 0
}

#try to run using a default java
trydefault () {
	compareminmax $(getjavaversion "$1")
	if [[ $? -eq 0 ]]
	then
		runjava "$@"
		exit $?
	else
		#still try to run using java's version:release option, if it fails then continue with a search, a problem here is that there is no way to distinguish if the error occurs within java or the application, interpret an error within two seconds of launching as being a java error
		if [[ -n $maxversion ]]
		then
			return 0
		fi
		declare oldopt="$opt"
		if [[ -n "$opt" ]]
		then
			opt="$opt -version:$minversion+"
		else
			opt="-version:$minversion+"
		fi
		declare -i elapsed=$SECONDS
		runjava "$@"
		declare result=$?
		elapsed=$SECONDS-elapsed
		if [[ $result -eq 0 || elapsed -gt 2 ]]
		then
			exit $result
		else
			opt="$oldopt"
		fi
	fi
	return 0
}

#find highest java version
findbest () {
	declare jv
	declare jp
	for jpath in $@
	do
		 if [[ ! -e $jpath || ! -r $jpath ]]
		 then
			continue
		fi
		if [[ -n $jdkpreference ]]
		then
			if [[ "$jdkpreference" = "$jreonly" ]]
			then
				jtype $jpath
				if [[ $? -eq 1 ]]
				then
					continue
				fi
			fi
			if [[ "$jdkpreference" = "$jdkonly" ]]
			then
				jtype $jpath
				if [[ $? -ne 1 ]]
				then
					continue
				fi
			fi
		fi
		declare jver=$(getjavaversion $jpath)
		compareminmax $jver
		if [[ $? -gt 0 ]]
		then
			continue
		fi
		if [[ -n $jv && -n $jp ]]
		then 
			compare $jver $jv $jpath $jp
			if [[ $? -eq 1 ]]
			then
				jv="$jver"
				jp="$jpath"
			fi
		else
			jv="$jver"
			jp="$jpath"
		fi
	done
	echo "$jp"
}

#script execution starts here
#check if we are connected to a real terminal, if not and headertype=console spawn one
tty -s
realtty=$?
if [[ $realtty -ne 0 ]]
then
	which x-terminal-emulator &>/dev/null
	if [[ $? -eq 0 ]]
	then
		xtermcommand="x-terminal-emulator"
	else
		which xterm &>/dev/null
		if [[ $? -eq 0 ]]
		then
			xtermcommand="xterm"
		fi
	fi
	if [[ x"$headertype" = x"$console" ]]
	then
		if [[ -n $xtermcommand ]]
		then
			$xtermcommand -e "$0" "$@"
		else
			showerror "This application needs to be run from a terminal."
		fi
		exit $?
	fi
fi
#you can override the launcher settings by providing command line options, launcher options are prefixed with --jnixopt eg. --jnixoptminversion=1.5.0, options with spaces must be escape quoted eg. --jnixoptpath=\"/usr/sun java/bin/java\"
declare -a newargs
declare -i position=1
while [[ -n "$1" ]]
do
	declare o="$1"
	declare jno=${o#--jnixopt}
	if [[ x"$jno" != x"$o" ]]
	then
		eval "$jno"
	else
		newargs[$position]=\"$o\"
		position=$position+1
	fi
	shift
done
#export these for use in java invocation
declare export EXECPATH="$0"
declare export EXECDIR=$(readlink -f $(dirname "$0"))
if [[ -n $chdir ]]
then
	declare mcd=${chdir#/}
	if [[ x"$mcd" = x"$chdir" ]]
	then
		cd "$EXECDIR/$chdir"
	else
		cd $chdir
	fi
fi
#first try to run using internal java path
if [[ -n $path ]]
then
	if [[ -e $path ]]
	then
		runjava $path "${newargs[@]}"
		exit $?
	else
		if [[ -z $minversion && -n $jar ]]
		then
			showerror bundledjreerr supporturl
			exit 1
		fi
	fi
fi

#if version information is supplied check some defaults
if [[ -n $minversion || -n $maxversion ]]
then
	#try $JAVA_HOME
	if [[ -n $JAVA_HOME ]]
	then
		trydefault "$JAVA_HOME" "${newargs[@]}"
	fi
	
	#then java in path
	which java &>/dev/null
	if [[ $? -eq 0 ]]
	then
		trydefault java "${newargs[@]}"
	fi
fi

#if $path is not null do a search of $path parents to find alternate java installations
if [[ -n $path ]]
then
	declare pathroot=$path
	while [[ ! -e "$pathroot" ]]
	do
		pathroot=$(dirname "$pathroot")
	done
	declare prj=$(find "$pathroot" -name java -type f -print 2>/dev/null)
	declare pj=$(findbest $prj)
	if [[ -n "$pj" ]]
	then
		runjava "$pj" "${newargs[@]}"
		exit $?
	fi
fi
#prefer to use locate since its fast
declare javapaths=$(locate -i -w -A "*/bin/java" 2>/dev/null)
#if locate fails fallback to using find
if [[ $? -gt 0 || x"$javapaths" = x"" ]]
then
	#prompt user to run updatedb
	runupdatedb
	if [[ $? -eq 0 ]]
	then
		javapaths=$(locate -i -w -A "*/bin/java" 2>/dev/null)
	else
		javapaths=$(find / -name java -type f -print 2>/dev/null)
	fi
fi
declare jp=$(findbest $javapaths)
if [[ -n "$jp" ]]
then
	runjava "$jp" "${newargs[@]}"
	exit $?
else
	 showerror jreversionerr downloadurl
	 exit 1
fi
#do not remove the blank line below

