list.of.packages <- c("ggplot2", "reshape", "plyr", "gtable", "ggpubr")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages,repos='http://cran.us.r-project.org')

library(ggplot2)
library(reshape)
library(plyr)
library(gtable)
library(ggpubr)

source("plot_functions.R")

file.in  <- "position_property_plot.csv"
file.out <- "position.csv"
inp <- read.csv(file.in,sep=',',header = TRUE)
if(length(levels(as.factor(inp$Type))) == 1)
{
   my.plot <- clusteredHistogram(file.in, file.out)
  pdf(paste(file.out,"_propertiesOverview.pdf",sep=""),width=30,height=5)
  plot(my.plot)
  dev.off()
}else
{
  my.plot <- clusteredHistogram(file.in, file.out)
  pdf(paste(file.out,"_propertiesOverview.pdf",sep=""),width=30,height=5)
  plot(my.plot)
  dev.off()
  offset = 0.01
  properties.significance.offset(file.in, file.out,offset)
}

file.in = "mfe_ratio_plot.csv"
file.out= "mfe_ratio_plot.pdf"
p.out <- mfe.ratio.analysis.ggplot.sampleCount(fileIn = file.in,
                                               nrep = 50, fileOut = file.out)
pdf(file.out,width = 10, height = 5)
plot(p.out)
dev.off()