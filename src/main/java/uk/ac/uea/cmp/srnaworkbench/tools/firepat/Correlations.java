/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author a019349
 */
public class Correlations {
    double[] corr = null;
    double[][] allcorr = null;
    final String PEARSON = "PEARSON";   // correlation method
    final String SPEARMAN = "SPEARMAN"; // ditto
    final String KENDALL = "KENDALL";   // ditto
    String first_dataset_name = "First"; // identifier for the first set of data in output files
    String second_dataset_name = "Second"; // identifier for the second set of data in output files
    String[] first_names;  // to hold identifiers for first dataset
    String[] second_names;  // ditto second
    ArrayList<double[]> corrdata; // to hold correlation data
    boolean local; // indicates whether correlations are local
    boolean datavalidity;// default is true, set to false if anything goes wrong
    boolean dataproc;// set to false if data in FiRePatdata object has not been (pre)processed
    boolean corrsvalid; // set to false if anything goes wrong whan calculating correlations
    private boolean output_first_dataset_first = true;// which set of seq pairs appear first in output tables
    int maxwindow;
    int minwindow;
    FiRePatLog fpl;
    Correlations()
    {
        datavalidity= false;
    }
    
    /**
    * For correlations on complete sets of data values using data from FiRePatData object.
    * 
    */
    Correlations(FiRePatData datain, String method,  double corrThr, FiRePatLog fplog)
    {
        fpl = fplog;
        // check data (pre)processed ok before doing anything
        dataproc = datain.getDataProc();
        if(dataproc==false)
        {
            fpl.println("FiRePat: input data not (pre)processed, unable to calculate correlations.");
            return;
        }
        local = false; // not using local correlations
        datavalidity = datain.getDataValidity(); // check if data valid in datain
        if(dataproc)
        {
            fpl.println("FiRePat: calculating correlations");
            method = method.toUpperCase();
            DataMatrix srnamatrix = datain.getFirstData(); // first dataset
            DataMatrix genematrix = datain.getSecondData(); // second dataset
            first_names = datain.getFirstNames();
            second_names = datain.getSecondNames();
            // dataset names
            first_dataset_name = datain.getFirstDatasetName();
            second_dataset_name = datain.getSecondDatasetName();
            corrdata = new ArrayList<>(); // to store correlation values 
            //iterate through sRNAs and genes, get correlation and retain those >= threshold
            for(int s = 0; s < srnamatrix.getM(); s++)
            {
                for(int g = 0; g< genematrix.getM(); g++)
                {
                    double x = 0; // correlation value 
                    if(method.equals(PEARSON)) x = PearsonCorrelation(srnamatrix.getRow(s),genematrix.getRow(g),srnamatrix.getN());
                    else if(method.equals(SPEARMAN)) x = SpearmanCorrelation(srnamatrix.getRow(s),genematrix.getRow(g),srnamatrix.getN());
                    else if(method.equals(KENDALL)) x = KendallCorrelation(srnamatrix.getRow(s),genematrix.getRow(g),srnamatrix.getN());                   
                    if(Math.abs(x)>=corrThr) // check that correlation is above threshhold
                    {  // double[] to hold indexes of srna and gene identifiers and correlation value
                       // value in posn[3] indicates the window width for local correlations,
                       // here the value zero indicates that it is NOT a local correlation
                       double[] srnagenecorr = new double[]{0,0,0,0};
                       srnagenecorr[0] = s;
                       srnagenecorr[1] = g;
                       srnagenecorr[2] = x;
                       corrdata.add(srnagenecorr);
                    }
                }
            }
            if(datavalidity) fpl.println("FiRePat: calculations completed successfully");
            else fpl.println("FiRePat: calculations not completed successfully, unable to continue");
        }
        else
        {
            fpl.println("FiRePat: input data not (pre)processed, unable to calculate correlations.");
            return;
        }
    }
    // for correlations on local sets of data values using data from FiRePatData object
    Correlations(FiRePatData datain, String method, double corrThr, int shortestwindow, int longestwindow, FiRePatLog fplog)
    {
        fpl = fplog;
        // check data (pre)processed ok before doing anything
        dataproc = datain.getDataProc();
        if(dataproc==false)
        {
            fpl.println("FiRePat: input data not (pre)processed, unable to calculate correlations.");
            return;
        }
        datavalidity = datain.getDataValidity();
        local = true;
        maxwindow = longestwindow;
        minwindow = shortestwindow;
        if(dataproc)
        {
            if(maxwindow==0)
            {
                if(minwindow==0) fpl.println("FiRePat: calculating local correlations using full width only");
                else fpl.println("FiRePat: calculating local correlations, minimum window = "+minwindow);
            }  
            else fpl.println("FiRePat: calculating local correlations, minimum window = "+minwindow+", maximum window = "+maxwindow);
            DataMatrix srnamatrix = datain.getFirstData(); // srna data
            DataMatrix genematrix = datain.getSecondData(); // gene data
            first_names = datain.getFirstNames();
            second_names = datain.getSecondNames();
            // dataset names
            first_dataset_name = datain.getFirstDatasetName();
            second_dataset_name = datain.getSecondDatasetName();
            corrdata = new ArrayList<>();// to store correlation values
            int nc = datain.getNumColumns();
            // set to full width, in practice this will be the default
            if(maxwindow==0) maxwindow = nc; 
            if(minwindow==0) minwindow = nc; 
            //iterate through data, get correlation and retain those >= threshold
            // s is srna number, g is gene number
            for(int s = 0; s < srnamatrix.getM(); s++)
            {
                Runtime rt = Runtime.getRuntime();
                long freeMem = rt.freeMemory();
                double megs = 1048576.0;
                // delete following line in final version
                for(int g = 0; g< genematrix.getM(); g++)
                {
                    double [] x = new double[]{0,0}; // correlation value and window width
                    x = localCorrelation(srnamatrix.getRow(s),genematrix.getRow(g), minwindow, maxwindow, method);
                    if(Math.abs(x[0])>=corrThr) // check that correlation is above threshhold
                    {  // double[] to hold indexes of identifiers and correlation value
                       // value at posn[3] indicates the window width used for the local correlation calculated
                       double[] srnagenecorr = new double[]{0,0,0,0};
                       srnagenecorr[0] = s;
                       srnagenecorr[1] = g;
                       srnagenecorr[2] = x[0];// correlation
                       srnagenecorr[3] = x[1];// window width
                       corrdata.add(srnagenecorr);
                    }
                }
            }           
            if(datavalidity) fpl.println("FiRePat: calculations completed successfully");
            else fpl.println("FiRePat: calculations not completed successfully, unable to continue");
        }
        else
        {
            fpl.println("FiRePat: input data not (pre)processed, unable to calculate correlations.");
            return;
        }
    }
    
   /**
    * Returns the total number of local correlations between one sRNA and one mRNA for a given range of window sizes. 
    *
    * @param  nsamples  the number of samples
    * @param  minwindow the minimum width of window to be used
    * @param  maxwindow the largest width of window to be used
    * @return      the total number of correlations
    */
    int numLocalCorrelations(int nsamples, int minwindow, int maxwindow)
    {
        int nlc = 0;
        int i = minwindow;
        while(i<=maxwindow)
        {
            nlc = nlc + nsamples - i + 1;
            i = i + 1;
        }
        return nlc;
    }
    // 'best' local correlation between one pairs of seqs if the same correlation is given
    // by windows of different widths, the value of window width returned is the largest
    double[] localCorrelation(double[] x, double[] y, int minwindow, int maxwindow, String method)
    {   // x = array of levels from 1st dataset, y = array of levels from second dataset
        if(maxwindow<minwindow)
        {
            fpl.println("ERROR: the maximum window is less than than minimum");
            return(null);
        }
        int nsamples = x.length;
        int nlc = numLocalCorrelations(nsamples, minwindow, maxwindow);
        double[] output = new double[]{0,0};// to hold output, the highest correlartion value will be used
        // now calculate each value...
        int imax = maxwindow - minwindow + 1;
        int windowi = minwindow;
        int rowindex = 0; //index of row in output matrix
        // main loop to increase window size
        for(int i=0; i<imax; i++)
        {   // inner loop to run window across samples
            int j = -1;
            int k = j + windowi;
            while(k<nsamples)
            {
                j = j + 1;
                k = k + 1;
                double[] xj = Arrays.copyOfRange(x, j, k);
                double[] yj = Arrays.copyOfRange(y, j, k);
                double[] currentvalue = new double[]{0,0};
                double wcorr = 0;
                if(method.equals(PEARSON)) wcorr = PearsonCorrelation(xj, yj, windowi);
                else if(method.equals(SPEARMAN)) wcorr = SpearmanCorrelation(xj, yj, windowi);
                else if(method.equals(KENDALL)) wcorr = KendallCorrelation(xj, yj, windowi);
                // check if correlation is higher than previous highest, NB uses >= not > so that
                // a longer window replaces a shorter window when the correlation is the same value
                if(Math.abs(wcorr)>=output[0])
                {
                    output[0] = wcorr;
                    output[1] = windowi;
                }
                rowindex++;
            }
            windowi++;
        }
        return(output);
    }
   
    // the two vectors and the number of elements
    double PearsonCorrelation(double[] x, double[] y,int m)
    {
        double PCC = 0;
        double meanX = 0;
        double stDevX = 0;
        double meanY = 0;
        double stDevY = 0;
        //calculate meanX, stDevX
        for(int j = 0; j <= m-1; j++)
        {
                meanX += x[j];
        }
        meanX /= m;
        for(int j = 0; j <= m-1; j++)
        {
                stDevX += (x[j]-meanX)*(x[j]-meanX);
        }
        stDevX /= m; stDevX = Math.sqrt(stDevX);
        //calculate meanY, stDevY
        for(int j = 0; j <= m-1; j++)
        {
                meanY += y[j];
        }
        meanY /= m;
        for(int j = 0; j <= m-1; j++)
        {
                stDevY += (y[j]-meanY)*(y[j]-meanY);
        }
        stDevY /= m; stDevY = Math.sqrt(stDevY);
        //calculate the PCC
        for(int j = 0; j <= m-1; j++)
        {
            PCC += (x[j]-meanX)*(y[j]-meanY);
        }
        PCC /= m;
        PCC /= (stDevX * stDevY);
        return PCC;
    }
    // the two vectors and the number of elements
    double SpearmanCorrelation(double[] x, double[] y,int m)
    {
        double SCC = 0;
        //create ranks
        double[][] xRank = new double[m][2];
        double[][] yRank = new double[m][2];       
        for(int i=0; i<m;i++)
        {
            xRank[i][0] = x[i];xRank[i][1]=i;
            yRank[i][0] = y[i];yRank[i][1]=i;
        }        
        double[] xSort = sortRanks(xRank,m);
        double[] ySort = sortRanks(yRank,m);       
        SCC = PearsonCorrelation(xSort, ySort,m);        
        return SCC;
    }
    
    double[] sortRanks(double[][] x, int m)
    {
        double[][] sortedRanks = new double[m][2];
        for(int i=0; i<m;i++)
        {
            sortedRanks[i][0] = x[i][0];sortedRanks[i][1]=x[i][1];
        }
        //bubble sort on the numeric column
        int sorted = 0;
        while(sorted != 1)
        {
            sorted = 1;
            for(int i=0; i < m-1;i++)
            {
                if(sortedRanks[i][0] > sortedRanks[i+1][0])
                {
                    sorted = 0;
                    double aux;
                    aux = sortedRanks[i][0]; sortedRanks[i][0]=sortedRanks[i+1][0]; sortedRanks[i+1][0]=aux;
                    aux = sortedRanks[i][1]; sortedRanks[i][1]=sortedRanks[i+1][1]; sortedRanks[i+1][1]=aux;
                }
            }
        }
        double[] srtRnk = new double[m];
        for(int i=0; i < m;i++)
        {
             srtRnk[i] = sortedRanks[i][1];
        }       
        return srtRnk;
    }
    // the two vectors and the number of elements
    double KendallCorrelation(double[] x, double[] y,int m)
    {
        double KCC = 0;
        int concordantPairs = 0;
        int discordantPairs = 0;
        for(int i = 0; i < m-1; i++)
        {
            for(int j = i+1; j < m; j++)
            {
                if(x[i] <= y[i] && x[j] <= y[j]){concordantPairs++;}
                if(x[i] > y[i]  && x[j] > y[j]){concordantPairs++;}
                if(x[i] < y[i]  && x[j] > y[j]){discordantPairs++;}
                if(x[i] > y[i]  && x[j] < y[j]){discordantPairs++;}
            }
        }
        KCC = (concordantPairs - discordantPairs)/(m*(m-1)/2);
        return KCC;
    }
    // print method for correlations generated by Correlations object constructed using FiRePatData object
    // an option to include the expression data was included, but not implemented, i.e. if includeData is set
    // to 'true' no data will be printed, this may be changed in future versions
    void writeCorrelationsToFile(String fileName, boolean includeData)
    {   // ensure .csv at end of file name
        int fnlength = fileName.length();
        if(fnlength>4)
        {
            String last4 = fileName.substring(fnlength-4).toLowerCase();
            if(!last4.equals(".csv")) fileName = fileName + ".csv";
        }
        else fileName = fileName + ".csv";
        // write file
        PrintWriter writer = null;
        fpl.println("FiRePat: writing correlations to file '"+fileName+"'");
        int ii=-1;
        try {
                writer = new PrintWriter(fileName, "UTF-8");
                if(!output_first_dataset_first)  writer.println(second_dataset_name+","+first_dataset_name+",correlation");
                else writer.println(first_dataset_name+","+second_dataset_name+",correlation");                       
                int numcorr = corrdata.size();
                for(int i = 0; i<numcorr; i++)
                {
                    ii=i;
                    double[] srnagenecorr = new double[]{0,0,0,0};
                    srnagenecorr = corrdata.get(i);
                    String srna = first_names[(int)srnagenecorr[0]];
                    String gene = second_names[(int)srnagenecorr[1]];
                    String value = Double.toString(srnagenecorr[2]);
                    if(!output_first_dataset_first) writer.print(gene+","+srna+","+value);
                    else writer.print(srna+","+gene+","+value);
                    writer.println();
                }
                fpl.println("FiRePat: writing correlations completed");
        } 
        catch (IOException ex) {fpl.println("FiRePat: IO exception");} 
        catch (Exception ex) {fpl.println("i="+ii);} 
        finally 
        {
            try {writer.close();} 
            catch (Exception ex){fpl.println("FiRePat: cannot close output file '"+fileName+"'");}
        }
    }
    // print method for local correlations generated by Correlations object constructed using FiRePatData object
    // adds "_w*.csv" to supplied filestem where * is the window width
    void writelocalCorrelationsToFiles(String fileNameStem, String output_dir)
    {   // first remove ".csv" if it is at end of fileNameStem
        fpl.println("FiRePat: writing local correlations to set of files...");
        int stemlength = fileNameStem.length();
        if(stemlength>4)
        {
            String last4 = fileNameStem.substring(stemlength-4).toLowerCase();
            if(last4.equals(".csv")) fileNameStem = fileNameStem.substring(0,stemlength-4);
        }
        // need to check which window sizes are used
        int numcorr = corrdata.size(); // number of correlation values
        // Boolean [] to indicate which windows are used
        int imax = maxwindow+1;
        Boolean[] allsizes = new Boolean[imax];
        Arrays.fill(allsizes, Boolean.FALSE);
        // add warning here if no data to write
        for(int i = 0; i<numcorr; i++)
        {
                int wsize = (int)corrdata.get(i)[3];//window size;
                allsizes[wsize] = true;
        }
        // check file directory is not an empty String and add file separator, otherwise do not (i.e. will use startup directory)
        if(!output_dir.equals("")) output_dir = output_dir+File.separator;
        // then write one file for each window, probably better to do sequentially rather than have several files
        // open at once, starts at 1 for safety, although 4 is the minimum window to get sensible results
        for(int i = 1; i<imax; i++)
        {   // i is window size
            boolean windowused = allsizes[i];
            if(windowused)
            {  
                PrintWriter writer = null; 
                String filename = "";
                try 
                {   
                    filename = output_dir+fileNameStem+"_w"+i+".csv";
                    fpl.println("FiRePat: writing local correlations to file '"+filename+"'");
                    writer = new PrintWriter(filename, "UTF-8");
                    if(!output_first_dataset_first) writer.println(second_dataset_name+","+first_dataset_name+",correlation");
                    else writer.println(first_dataset_name+","+second_dataset_name+",correlation");                         
                    for(int j = 0; j<numcorr; j++)
                    {
                        double[] srnagenecorr = new double[]{0,0,0,0};
                        srnagenecorr = corrdata.get(j);
                        // check if correct window size
                        int wsize = (int)srnagenecorr[3];
                        if(wsize==i)
                        {
                            String srna = first_names[(int)srnagenecorr[0]];
                            String gene = second_names[(int)srnagenecorr[1]];
                            String value = Double.toString(srnagenecorr[2]);
                            if(!output_first_dataset_first) writer.print(gene+","+srna+","+value);
                            else writer.print(srna+","+gene+","+value);
                            writer.println();
                        }
                    } 
                }
                catch (IOException ex) {fpl.println("FiRePat: IO exception");} 
                finally 
                {
                    try {writer.close();} 
                    catch (Exception ex){fpl.println("FiRePat: cannot close output file '"+filename+"'");}
                }
            }
        }
        fpl.println("FiRePat: writing local correlations completed");
    }
    ArrayList<double[]> getCorrelationData()
    {
        return(corrdata);
    }
    String [] getFirstNames()
    {
        return(first_names);
    }
    String [] getSecondNames()
    {
        return(second_names);
    }
    int getNumSecond()
    {
        return(second_names.length);
    }
    int getNumFirst()
    {
        return(first_names.length);
    }
    public void setFirstDatasetFirst(boolean b)
    {
        output_first_dataset_first = b;
    }
}
