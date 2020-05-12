list.of.packages <- c("readr")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages,repos='http://cran.us.r-project.org')

revstring=function(s)
return(paste(rev(strsplit(s,"")[[1]]),collapse=""))

library(readr)

# CSV file contains: gene ID , cleavage position, fragment abundance
transcript_hits_df <- read_csv("plot_data.csv",
                               col_names = FALSE)
colnames(transcript_hits_df)<- c("geneID" , "cleavagePosition", "fragmentAbundance")


# to split the table based on the genes
trans_hits <- split(transcript_hits_df, transcript_hits_df$geneID)

# to store t-plots in one PDF file
pdf("T-plots.pdf", width=7, height=5) 


  for(i in trans_hits){

      heading = i$geneID[1]
        
        #Optionaly trim the heading
        #heading <- substr(heading, 1, 100)

		
        plot(i$cleavagePosition, i$fragmentAbundance, type='h', 
             main = paste(heading),
             #sub = paste0(" Cleavage site: ", cleavage, "   Tag abundance: ", abund, "   Weighted abundance: " , weightedAbund, "   Category: " , category,"\nsRNA abundance: " , shortReadAbundance,   "   Alignment score: ", alignScore,    "  MFE ratio: " , mfeRatio, "        p-value: " , pVal),
             xlab="", ylab = "Fragment Abundance",
             cex.main	= 0.7,
			 #ylim = c(0, 25),
             #cex.axis	= 0.65,
             #cex.sub = 0.725,
             #font.sub = 2
        )
             #points(i$cleavagePosition, i$fragmentAbundance, pch=16)
             mtext("Transcript position", line=-19,cex=0.85)
   
  }



# make sure to run dev.off() after each run to close PDF file
dev.off()


