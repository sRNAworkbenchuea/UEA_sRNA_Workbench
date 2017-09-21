genes<-read.csv('firepat_test150_genes.csv',sep=',',header=TRUE)
srnas<-read.csv('firepat_test150_srna_loci.csv',sep=',',header=TRUE)

pc <- rep(0, nrow(genes)*nrow(srnas))
sc <- rep(0, nrow(genes)*nrow(srnas))
kc <- rep(0, nrow(genes)*nrow(srnas))

for(i in (1:nrow(genes)))
{
	print(i)
	for(j in (1:nrow(srnas)))
	{
		g <- genes[i,2:11]
		s <- srnas[j,2:11]
		c <-rbind(g,s)
		
		pc[(i-1)*nrow(srnas)+j] <- cor(t(c),method = 'pearson')[1,2]
		sc[(i-1)*nrow(srnas)+j] <- cor(t(c),method = 'spearman')[1,2]
		kc[(i-1)*nrow(srnas)+j] <- cor(t(c),method = 'kendall')[1,2]
	}
}

sink('pearson.csv')
print(pc)
sink()

sink('spearman.csv')
print(sc)
sink()

sink('kendall.csv')
print(kc)
sink()