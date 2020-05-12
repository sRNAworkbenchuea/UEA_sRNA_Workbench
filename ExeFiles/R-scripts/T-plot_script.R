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

paresnip2_df <- read_csv("results.csv")


# to store t-plots in one PDF file
pdf("T-plots.pdf", width=7, height=5) 

for(r in 1:nrow(paresnip2_df)) {
  
  par(family="mono") # change the font
  geneName <- paresnip2_df[r,]$`Gene ID`
  cleavage <- paresnip2_df[r,]$`Cleavage Position`
  duplex <- paresnip2_df[r,]$`Duplex`
  abund <- paresnip2_df[r,]$`Fragment Abundance`
  alignScore <- paresnip2_df[r,]$`Alignment Score`
  weightedAbund <- round(paresnip2_df[r,]$`Weighted Fragment Abundance`,3)
  category <- paresnip2_df[r,]$Category
  mfeRatio <- round(paresnip2_df[r,]$`MFE Ratio`,3)
  pVal <- round(paresnip2_df[r,]$`p-value`,3)
  shortReadAbundance = paresnip2_df[r,]$`Short Read Abundance`
  

  for(i in trans_hits){
    m <- match(cleavage, i$cleavagePosition, nomatch = 0)
    if(m > 0){
      heading = i$geneID[1]
      if(all(heading==geneName)){
        
        #Optionaly trim the heading
        #heading <- substr(heading, 1, 100)
        
		#Uncomment this line if you want the duplex mRNA to be from 5' to 3'
		duplex<-revstring(duplex)
		
        plot(i$cleavagePosition, i$fragmentAbundance, col=ifelse(i$cleavagePosition==cleavage, "red", "black"), type='h', 
             main = paste(heading,"",duplex, sep = "\n"),
             sub = paste0(" Cleavage site: ", cleavage, "   Tag abundance: ", abund, "   Weighted abundance: " , weightedAbund, "   Category: " , category,"\nsRNA abundance: " , shortReadAbundance,   "   Alignment score: ", alignScore,    "  MFE ratio: " , mfeRatio, "        p-value: " , pVal),
             xlab="", ylab = "Fragment Abundance", 
             cex.main	= 0.7,
			 #ylim = c(0, 15),
             cex.axis	= 0.65,
             cex.sub = 0.725,
             font.sub = 2
        )
             points(i$cleavagePosition, i$fragmentAbundance, pch=16, cex=ifelse(i$cleavagePosition==cleavage, 1, 0.01), col=ifelse(i$cleavagePosition==cleavage, "red", "black"))
             mtext("Transcript position", line=-19,cex=0.85)
      }
    }
  }
}


# make sure to run dev.off() after each run to close PDF file
dev.off()


