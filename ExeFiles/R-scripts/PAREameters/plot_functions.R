list.of.packages <- c("ggplot2", "reshape", "plyr", "gtable", "ggpubr")
new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages,repos='http://cran.us.r-project.org')


##code for clustered histograms and significance tests
##on the mm, gu, gaps
clusteredHistogram <- function(fileIn, fileOut)
{
  dd         <- read.csv(fileIn,sep=',',header=TRUE)
  dd.melt.mm <- data.frame(dd$Position,dd$Type,dd$Mismatches,rep("MM",nrow(dd)), dd$Count)
  dd.melt.gu <- data.frame(dd$Position,dd$Type,dd$G.U.pairs,rep("GU",nrow(dd)), dd$Count)
  dd.melt.ga <- data.frame(dd$Position,dd$Type,dd$Gaps,rep("gap",nrow(dd)),dd$Count)
  
  dd.melt <- t(data.frame(t(dd.melt.mm), t(dd.melt.gu), t(dd.melt.ga)))
  colnames(dd.melt) = c("Position", "Type", "Value", "Attribute", "Count")
  
  dd.melt.df = data.frame(
    Position = as.factor(as.numeric(dd.melt[,1])),
    MType    = as.factor(as.character(dd.melt[,2])),
    Attribute= as.factor(as.character(dd.melt[,4])),
    Value    = as.numeric(dd.melt[,3]),
    Count    = as.numeric(dd.melt[,5])
  )
  
  rr.return <- ggplot(data=dd.melt.df, aes(x=MType, y=Value, fill=Attribute)) + 
    geom_bar(stat="identity") +
    facet_grid(~Position) + 
    labs(title="", 
         x="Position vs Length (nt)", y="Percentage", fill="Attribute") + 
    theme(plot.title = element_text(size=25, margin=margin(t=20, b=20))) + 
    theme(axis.text.x = element_text(angle = 90))
  return(rr.return)
}

properties.significance.offset <- function(fileIn, fileOut,offset)
{
  dd         <- read.csv(fileIn,sep=',',header=TRUE)
  dd.melt.mm <- data.frame(dd$Position,dd$Type,dd$Mismatches,rep("MM",nrow(dd)), dd$Count)
  dd.melt.gu <- data.frame(dd$Position,dd$Type,dd$G.U.pairs,rep("GU",nrow(dd)), dd$Count)
  dd.melt.ga <- data.frame(dd$Position,dd$Type,dd$Gaps,rep("gap",nrow(dd)),dd$Count)
  
  dd.melt <- t(data.frame(t(dd.melt.mm), t(dd.melt.gu), t(dd.melt.ga)))
  colnames(dd.melt) = c("Position", "Type", "Value", "Attribute", "Count")
  
  dd.melt.df = data.frame(
    Position = as.factor(as.numeric(dd.melt[,1])),
    MType    = as.factor(as.character(dd.melt[,2])),
    Attribute= as.factor(as.character(dd.melt[,4])),
    Value    = as.numeric(dd.melt[,3]),
    Count    = as.numeric(dd.melt[,5])
  )
  levels(dd.melt.df$MType)
  attribute.count   = length(levels(as.factor(dd.melt.df$Attribute)))
  ath.select    = dd.melt.df$MType=="ath"
  
  organisms     = levels(dd.melt.df$MType)
  orgs          = organisms[organisms != "ath"]
  
  for(o in orgs)
  {
    print(o)
    # significanceTests = rep(0,1+attribute.count)
    
    org.select    = dd.melt.df$MType == o
    dd.melt.df.sel= rbind(dd.melt.df[ath.select,], dd.melt.df[org.select,])
    ath.label     = "ath"
    org.label     = o
    
    attribute.count   = length(levels(as.factor(dd.melt.df$Attribute)))
    significanceTests = rep(0,1+attribute.count)
    
    
    for(p in 1:21)
    {
      fisher.in <- matrix(rep(0,4),nrow=2)
      selected <- dd.melt.df.sel[dd.melt.df.sel$Position == p,]
      dd.cons  <- selected[selected$MType == ath.label,]
      dd.spec  <- selected[selected$MType == org.label,]
      dd.cons$Attribute = as.character(dd.cons$Attribute)
      dd.spec$Attribute = as.character(dd.spec$Attribute)
      
      ##noise cut-off
      dd.cons$Value = dd.cons$Value + offset
      dd.spec$Value = dd.spec$Value + offset
      
      stat.out <- rep(0,1+attribute.count)
      if(sum(dd.cons$Value) >0 && sum(dd.spec$Value) > 0)
      {
        dd.cons  =  rbind(dd.cons,c(p,"ath","diff",1-sum(dd.cons$Value),-1))
        dd.spec  =  rbind(dd.spec,c(p,o,"diff",1-sum(dd.spec$Value),-1))
        dd.cons$Value = as.numeric(dd.cons$Value)
        dd.spec$Value = as.numeric(dd.spec$Value)
        
        dd.cons.1<- floor(dd.cons$Value/sum(dd.cons$Value)*100 - 0.5)+1
        dd.spec.1<- floor(dd.spec$Value/sum(dd.spec$Value)*100 - 0.5)+1
        
        chiSq.pval <- chisq.test(cbind(dd.cons.1,dd.spec.1))$p.value
        stat.out[1] = floor(chiSq.pval * 100)/100
        
        for (i in 1:(length(dd.cons.1)-1))
        {
          co1 <- dd.cons.1[i]
          co2 <- sum(dd.cons.1[-i])
          sp1 <- dd.spec.1[i]
          sp2 <- sum(dd.spec.1[-i])
          
          fisher.in[1,1] = co1
          fisher.in[2,1] = co2
          fisher.in[1,2] = sp1
          fisher.in[2,2] = sp2
          
          stat.out[i+1] = floor(fisher.test(t(fisher.in))$p.value * 100)/100
        }
      }
      significanceTests = rbind(significanceTests,stat.out)
    }
    significanceTests = significanceTests[2:nrow(significanceTests),]
    #attr.names        = levels(as.factor(dd.melt.df$Attribute))
    attr.names         = dd.cons[1:(nrow(dd.cons)-1),]$Attribute
    colnames(significanceTests) = c("ChiSq",as.character(attr.names))
    rownames(significanceTests) = 1:nrow(significanceTests)
    
    write.csv(significanceTests,file=paste(fileOut,"_",o,"_significance.csv",sep=""))
  }##endfor a selected organism
}

#######
### test the number of samples
#######
##'the input for this script is a csv file containing, on each column the mfe ratios
##'for a sample; all line plots are represented on the same plot
##'the significance analysis is done on a Kolmogorov Smirnov test
##' @param fileIn = input file, on each column present the mfe in one sample
##' @param fileOut = output file, cummulative plot and density plot
##' @param nrep = number of repeats for the subsampling
mfe.ratio.analysis.ggplot.sampleCount <- function(fileIn, nrep, fileOut)
{
  inp <- read.csv(fileIn, header=TRUE)
  all.org = inp
  col.plot = c("black","red","blue","orange","darkgreen")
  ##count the nas and subsample to make the distirbutions comparable
  ##the subsampling is done on the smallest set of non-na entries
  if(ncol(all.org)>1)
  {
    na.count <- rep(0,ncol(all.org))
    for(i in 1:ncol(all.org))
    { na.count[i] = sum(is.na(all.org[,i]))}
    
    min.y = min(all.org[!is.na(all.org)])
    
    ##create the subsampled input data
    ss.min = nrow(all.org) - max(na.count)
    ss.out = rep(-1,ss.min)
    for(i in 1:ncol(all.org))
    {
      non.na = all.org[!is.na(all.org[,i]),i]
      sub.sam= matrix(rep(0,nrep*ss.min),ncol=nrep)
      for(k in 1:nrep)
      {
        sub.sam.k =sample(non.na,replace = F,size=ss.min)
        sub.sam[,k] = sort(sub.sam.k)
      }
      sub.sam.avg = apply(sub.sam,1,mean)
      ss.out = cbind(ss.out,sort(sub.sam.avg))
    }
    ss.out = ss.out[,2:ncol(ss.out)]
    colnames(ss.out) = colnames(all.org)
  }
  else
  {
    ss.out = all.org
    colnames(ss.out) = colnames(all.org)
  }
  
  ## create the data frame for the ggplot
  dd.in <- c("",0,0)
  for(i in 1:nrow(ss.out))
  {
    for(j in 1:ncol(ss.out))
    {
      dd.add <- c(colnames(ss.out)[j],i,ss.out[i,j])
      dd.in = rbind(dd.in,dd.add)
    }
  }
  dd.in = dd.in[2:nrow(dd.in),]
  colnames(dd.in) = c("group","idx","mfe")
  rownames(dd.in) = rep("",nrow(dd.in))
  dd = data.frame(as.character(dd.in[,1]),
                  as.numeric(dd.in[,2]),as.numeric(dd.in[,3]))
  colnames(dd) = colnames(dd.in)
  
  ##this is the cumulative plot
  p1 <- ggplot(dd, aes(x=idx, y=mfe, group=group, color=group)) +
    geom_line(aes(color=group)) + 
    theme(legend.position = "bottom") +
    scale_x_continuous(name="Number of miRNA-mRNA interactions")
  
  ##density plot component
  mu <- ddply(dd, "group", summarise, grp.mean=mean(mfe))
  p2 <- ggplot(dd, aes(x=mfe,group=group, color = group)) + 
    geom_density() + xlim(0.4,1.1)+
    # geom_vline(data=mu, aes(xintercept=grp.mean, color=group),
    #            linetype="dashed") + 
    theme(legend.position = "bottom")
  p.return <- ggarrange(p1, p2, widths = 1:2)
  # plot(p.return)
  
  ##Kolmogorov Smirnov statistic
  ##two sided test
  ##the statistical test will only be performed if there is more than one sample
  if(ncol(ss.out) >1)
  {
    for(j in 1:ncol(ss.out))
    {
      for(j1 in (1:ncol(ss.out)))
      {
        if(j != j1)
        {
          ks.test.ss <- ks.test(ss.out[,j],ss.out[,j1])$p.value
          cat("sample ",colnames(ss.out)[j]," compared to ",colnames(ss.out)[j1],
              " has a KS statistic of ",ks.test.ss,"\n")
        }
      }
    } ## end main for
  }
  
  return(p.return)
}