/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author a019349
 */
public class FiRePatData {
    String first_dataset_name = "First"; // identifier for the first set of data in output files
    String second_dataset_name = "Second"; // identifier for the second set of data in output files
    // objects to hold names and data read from files before processing
    DataMatrix firstdatain;
    DataMatrix seconddatain; 
    ArrayList<String> secondnamesin;  
    ArrayList<String> firstnamesin;  
    DataMatrix firstdata; // to hold expression data
    DataMatrix seconddata; // to hold expression data
    String[] firstnames;  // to hold identifiers
    String[] secondnames;  // to hold identifiers
    String[] samplenames;// names of samples/experiments
    boolean firstdatavalidity;// if true, indicates data loaded properly, i.e. proper format, does not indicate if data any good
    boolean seconddatavalidity;// ditto
    boolean firstdataproc;// if true, indicates if data (pre)processed, does not indicate anything else
    boolean seconddataproc;// ditto
    int nsamples; // number of samples
    int nreps;  // number of replicates of each sample
    int nc;    // number of data columns (nsamp*nrep)
    double offset = 20; // default offset for offset fold change (OFC) calculations
    double ofcPCcutoff = 10; // top % by OFC to retain when filtering out noise
    double stringency = 5; // maximum % difference allowed for EV checks
    double sizecutoffthreshhold = 10; // 10% cutoff for sRNAs of a given size to be retained when filtering
    double non_srnas_filter_level = 10; // cutoff value for removing data with v low expression levels, default will do nothing, but anything else will 
    boolean filter_non_srna_data = true;
    private FiRePatLog fpl;
    
    FiRePatData()
    {
        firstdatavalidity = false;
        seconddatavalidity = false;    
    }

    public void setLogFile(FiRePatLog f)
    {
        fpl = f;
    }
    public void setNumSamples(int ns)
    {
        nsamples = ns;
    }
    public void setNumReplicates(int nr)
    {
        nreps = nr;
    }
    String [] getFirstNames()
    {
        return(firstnames);
    }
    String [] getSecondNames()
    {
        return(secondnames);
    }
    String [] getSampleNames()
    {
        return(samplenames);
    }
    void setSampleNames(String[] snames)
    {
        samplenames = snames;
    }
    void setSampleNames(String snames)
    {       
        samplenames = snames.split(",");
        for(int i=0; i<samplenames.length; i++) samplenames[i] = samplenames[i].trim();
    }
    DataMatrix getFirstData()
    {
        return(firstdata);
    }
    DataMatrix getSecondData()
    {
        return(seconddata);
    }
    boolean getDataValidity()
    { 
        if(firstdatavalidity==false) return(false);
        if(seconddatavalidity==false) return(false);               
        return(true);
    }
    boolean getDataProc()
    { 
        if(firstdataproc==false) return(false);
        if(seconddataproc==false) return(false);               
        return(true);
    }
    boolean getFirstDataValidity()
    { 
        return(firstdatavalidity);
    }
    boolean getSecondDataValidity()
    { 
        return(seconddatavalidity);
    }
    boolean getFirstDataProc()
    { 
        return(firstdataproc);
    }
    boolean getSecondDataProc()
    { 
        return(seconddataproc);
    }
    int getNumSamples()
    { 
        return(nsamples);
    }
    int getNumReplicates()
    { 
        return(nreps);
    }
    int getNumColumns()
    { 
        return(nc);
    }
    int getNumSecond()
    { 
        return(secondnames.length);
    }
    int getNumFirst()
    { 
        return(firstnames.length);
    }
    // methods to get statistics of input data
    boolean isEven(int x)
    {
        if((x%2)==0) return(true);
        return(false);
    }
    private double getMedian(double[] datain)
    {   // datain MUST be sorted
        int ndata = datain.length;
        double median = 0;
        int pos1 = (int)Math.floor((ndata - 1.0) / 2.0);
        int pos2 = (int)Math.ceil((ndata - 1.0) / 2.0);
        if (pos1 == pos2 )  median = datain[(int)pos1];        
        else  median = (datain[pos1] + datain[pos2]) / 2.0 ;       
        return(median);
    }
    // method to get maximum offset fold changes for several series of values
    double[] getAllMaxOFC(double[][] d)
    {
        double[] allOFC = new double[d.length];
        int nr = d.length;
        for(int i = 0; i<nr; i++)  allOFC[i] = getMaxOFC(d[i]);
        return(allOFC);
    }
    // method to get maximum offset fold change within a series of values
    double getMaxOFC(double [] d)
    {
        //double offset = 20;
        double[] dclone = d.clone();
        Arrays.sort(dclone);
        double dmin = dclone[0];
        double dmax = dclone[dclone.length-1];
        double output = getOFC(dmax,dmin);
        return(output);
    }
    // method to get offset fold change between two values
    double getOFC(double d1, double d2)
    {
        double output = (d1+offset)/(d2+offset);
        return(output);
    }
    double getOFCpcCutoff(double[] input)
    {   // clone to prevent original being sorted
        double[] allOFC = input.clone();
        Arrays.sort(allOFC);
        //sorted ascending, so need top %
        double retainPC = 100 - ofcPCcutoff;
        //convert to index of element in array
        double retain = allOFC.length*retainPC/100;
        int index = (int) Math.ceil(retain)-1;
        return(allOFC[index]);
    }
    double getOffset()
    {
        return(offset);
    }
    // to change OFC parameter
    void setStringency(double str)
    {
        stringency = str;
    }
    void setOffset(double ofst)
    {
        offset = ofst;
    }
    // to change percentage sRNAs/genes to retain
    void setPCcutoff(double pcctoff)
    {
        ofcPCcutoff = pcctoff;
    }
    // method to perform (pre)processing/data cleansing on first dataset and store the cleansed data
    void processFirstInputData(double[][] expdata, ArrayList<String> firstnamesList, boolean shortrna, boolean hasheader, boolean filtersrnas)
    {
        if(firstdatavalidity==false)
        {
            fpl.println("FiRePat: first dataset not valid, unable to process.");
            return;
        }           
        try
        {   // if exception occurs will be set to false, true does not mean data
            // is any good, only that it has the right format                        
            // read and check data 
            fpl.println("FiRePat: processing first dataset");
            // need to throw exception here if nc does not match number of data columns in expdata
            if(nc!=expdata[0].length)
            {
                throw new Exception("FiRePat: number of columns in first dataset does not match the number of data columns required.");
            }
            if(shortrna&&filtersrnas) // is sRNA data and to be filtered
            {// get length (class size) of each sRNA seq, returned array also has max and min length values
                int[] allLengths = measureLengths(firstnamesList);
                int[] sizes = keepLengths(allLengths);// which class sizes are to be checked
                // get subset of data for all size classes to be retained
                boolean [] sequencesToKeep = seqsToKeep(allLengths, sizes);// which ones to retain
                firstnamesList = retain(firstnamesList, sequencesToKeep);
                expdata = retain(expdata, sequencesToKeep);
                // filter out noise: calculate OFC for all sRNAs
                fpl.println("FiRePat: first dataset, filtering sRNA data, retaining top "+ofcPCcutoff+"%");
                double[] allOFC = getAllMaxOFC(expdata);
                // then calculate values for top % cutoff
                double OFCcutoff = getOFCpcCutoff(allOFC);    
                int ndatain = expdata.length;// number of sRNAs
                boolean[] keep = new boolean[ndatain]; // to hold which sRNAs are to be kept
                Arrays.fill(keep, true);
                int nkeep = 0; // number of sRNAs to keep
                for(int j = 0; j<ndatain; j++)
                {
                    if(allOFC[j]<OFCcutoff) keep[j] = false;
                    else nkeep++;
                }
                double[][] retainedData = new double[nkeep][nc];
                firstnames = new String[nkeep];
                int k = 0; // index of retainedData
                for(int j = 0; j<ndatain; j++)
                {
                    if(keep[j])
                    {
                        retainedData[k] = expdata[j];
                        firstnames[k] = firstnamesList.get(j);
                        k++;
                    }
                }
                firstdata = new DataMatrix(retainedData);
                //writeMatrix(srnadata.getMatrix(), "checkinput.csv");
                firstdataproc = true;// set to true, processing completed
            }
            else // is any other type (mRNA, proteomic, etc) of data
            {
                if(filter_non_srna_data==true)
                {// apply filter for minimum expression level
                    fpl.println("FiRePat: first dataset, filtering non-sRNA data, expression level cutoff = "+non_srnas_filter_level);
                    boolean[] toKeep = minExpLevelFilter(expdata);
                    firstnamesList = retain(firstnamesList, toKeep);
                    expdata = retain(expdata, toKeep);
                }                
                firstnames = firstnamesList.toArray(new String[0]);
                firstdata = new DataMatrix(expdata);
                firstdataproc = true;// set to true, processing completed
            }            
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: first dataset, problem with data processing ");
            System.err.println(e);
            firstdataproc = false;// should be false, but set anyway
        } 
    }
    // method to perform (pre)processing/data cleansing on second data file and store the cleansed data
    void processSecondInputData(double[][] expdata, ArrayList<String> secondnamesList, boolean shortrna, boolean hasheader, boolean filtersrnas)
    {
        if(seconddatavalidity==false)
        {
            fpl.println("FiRePat: second dataset not valid, unable to process.");
            return;
        }          
        PrintWriter writer = null;
        fpl.println("FiRePat: processing second dataset");
        try
        {                           
            // need to throw exception here if nc does not match number of data columns in expdata
            if(nc!=expdata[0].length)
            {
                throw new Exception("FiRePat: number of columns in second dataset does not match the number of data columns required.");
            }
            if(shortrna) // is sRNA data
            {   // get length (class size) of each seq, returned array also has max and min length values
                int[] allLengths = measureLengths(secondnamesList);
                int[] sizes = keepLengths(allLengths);// which class sizes are to be checked
                boolean[] sizepassed = new boolean[sizes.length];// to hold info on if size class ok
                // get subset of data for all size classes to be retained
                boolean [] sequencesToKeep = seqsToKeep(allLengths, sizes);// which ones to retain
                secondnamesList = retain(secondnamesList, sequencesToKeep);
                expdata = retain(expdata, sequencesToKeep);
                // filter out noise:
                // calculate OFC for all sRNAs
                fpl.println("FiRePat: second dataset, filtering sRNA data, retaining top "+ofcPCcutoff+"%");
                double[] allOFC = getAllMaxOFC(expdata);
                // then calculate values for top % cutoff
                double OFCcutoff = getOFCpcCutoff(allOFC);    
                int ndatain = expdata.length;// number of sRNAs
                boolean[] keep = new boolean[ndatain]; // to hold which sRNAs are to be kept
                Arrays.fill(keep, true);
                int nkeep = 0; // number of sRNAs to keep
                for(int j = 0; j<ndatain; j++)
                {
                    if(allOFC[j]<OFCcutoff) keep[j] = false;
                    else nkeep++;
                }
                double[][] retainedData = new double[nkeep][nc];
                secondnames = new String[nkeep];
                int k = 0; // index of retainedData
                for(int j = 0; j<ndatain; j++)
                {
                    if(keep[j])
                    {
                        retainedData[k] = expdata[j];
                        secondnames[k] = secondnamesList.get(j);
                        k++;
                    }
                }
                seconddata = new DataMatrix(retainedData);
                seconddataproc = true;   // set to true, data processed
            }
            else // is any other type (mRNA, proteomic, etc) of data
            {
                if(filter_non_srna_data==true)
                {// apply filter for minimum expression level
                    fpl.println("FiRePat: second dataset, filtering non-sRNA data, expression level cutoff = "+non_srnas_filter_level);
                    boolean[] toKeep = minExpLevelFilter(expdata);
                    secondnamesList = retain(secondnamesList, toKeep);
                    expdata = retain(expdata, toKeep);
                }
                secondnames = secondnamesList.toArray(new String[0]);
                seconddata = new DataMatrix(expdata);
                seconddataproc = true;   // set to true, data processed
            }            
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: second dataset, problem with data preprocessing");
            System.err.println(e);
            seconddataproc = false;// should be false, but set anyway
        } 
    }

    
    private int[] measureLengths(ArrayList<String> srnas)
    {// measure lengths of all sRNAs using sequence to do so
        int imax = srnas.size();
        int[] allLengths = new int[imax+2];
        allLengths[0] = srnas.get(0).length();
        int minlength = allLengths[0];
        int maxlength = allLengths[0];
        int i = 1;
        while (i < imax) 
        {
            allLengths[i] = srnas.get(i).length();
            if(allLengths[i]<minlength) minlength=allLengths[i];
            if(allLengths[i]>maxlength) maxlength=allLengths[i];
            i++;       
        }
        allLengths[i] = minlength;
        allLengths[i+1] = maxlength;
        return allLengths;
    }
    // method to count number of seqs of each size, then decide if number of seqs of that length 
    // is above cutoff threshold (default 5%) and return boolean[] output indicating whether to retain them or not
    private int[] keepLengths(int[] allLengths)
    {// value at tokeep[x] is whether to keep all seqs of length x or not
        int imax = allLengths.length-2;
        boolean [] tokeep = new boolean[allLengths[imax+1]+1];
        int [] lengthcounts = new int[allLengths[imax+1]+1];
        int total = 0;
        int jmax = lengthcounts.length;
        for(int i=0; i<imax; i++) lengthcounts[allLengths[i]]++;
        for(int j=0; j<jmax; j++) total = total + lengthcounts[j];
        double fpc = total*sizecutoffthreshhold/100;
        int nsizes = 0;// number of size classes to keep
        for(int k=0; k<jmax; k++)
        {
            if(lengthcounts[k]>=fpc)
            {
                tokeep[k] = true;
                nsizes++;
            }
        } 
        int[] output = new int[nsizes];
        int n = 0;
        for(int m=0; m<jmax; m++)
        {
            boolean keep = tokeep[m];
            if(keep==true)
            {
                output[n] = m;
                n++;
            }
        }         
        return(output);
    }
    // method to indicate which seqs are to be kept for filtering, based on size
    private boolean[] seqsToKeep(int[] allLengths, int[] sizes)
    {
        int imax = allLengths.length-2;
        boolean [] tokeep = new boolean[allLengths[imax+1]+1];
        boolean [] output = new boolean[imax];
        int jmax = sizes.length;
        for(int i=0; i<imax; i++) 
        {
            int lengthi = allLengths[i];// length of sequence i
            for(int j=0; j<jmax; j++)
            {// check if matches size to be retained
                int sj = sizes[j];
                if (lengthi==sj) output[i] = true;
            }
        }
        return(output);
    }
    
    // method to identify which elements of array have given value, this is specifically
    // for an array of RNA sizes produced by measureLengths(), so the last 2 elements are ignored
    private int[] whichSizesMatch(int[] a, int b)
    {
        int count = 0;
        for(int k=0; k<a.length-2; k++) if(a[k]==b) count++;
        int[] output = new int[count];
        int i = 0;
        int j = 0;
        while(i<a.length-2)
        {
            if(a[i]==b)
            {
                output[j] = i;
                j++;
            }
            i++;
        }
        return(output);
    }
    // method to get subset of array for specific indexes
    private double[][] dataSubset(double[][] input, int[] indexes)
    {
        double[][] output = new double[indexes.length][input[0].length];
        for(int i=0; i<indexes.length; i++)  output[i] = input[indexes[i]];
        return(output);
    }
    private double[] dataColumn(double[][] input, int index)
    {// method to get 'column' from 2D array
        double[] output = new double[input.length];
        for(int i=0; i<input.length; i++) output[i] = input[i][index];
        return(output);
    }
    private double[] ofcLog2(double[] a, double[] b)
    {// calculate log-2 ofc
        double[] output = new double[a.length];
        for(int i=0; i<a.length; i++)   output[i] = Math.log((a[i]+offset)/(b[i]+offset)) / Math.log(2);
        return(output);
    }
    // methods to make subsets of data using boolean[] to indicate wchich to keep    
    private ArrayList<String> retain(ArrayList<String> input, boolean[] toKeep)
    {
        int nkeep = 0;
        for(int n=0; n<toKeep.length; n++) if(toKeep[n]==true) nkeep++;
        ArrayList<String> output = new ArrayList<>();
        int j = 0;
        for(int i=0; i<input.size(); i++)
        {
            if(toKeep[i])
            {
                String inputi = input.get(i);
                output.add(inputi);
                j++;
            }
        }
        return(output);
    }
    private String[] retain(String[] input, boolean[] toKeep)
    {
        int nkeep = 0;
        for(int n=0; n<toKeep.length; n++) if(toKeep[n]==true) nkeep++;
        String[] output = new String[nkeep];
        int j = 0;
        for(int i=0; i<input.length; i++)
        {
            if(toKeep[i])
            {
                output[j] = input[i];
                j++;
            }
        }
        return(output);
    }
    private double[][] retain(double[][] input, boolean[] toKeep)
    {
        int nkeep = 0;
        for(int n=0; n<toKeep.length; n++) if(toKeep[n]==true) nkeep++;
        double[][] output = new double[nkeep][input[0].length];
        int j = 0;
        for(int i=0; i<input.length; i++)
        {
            if(toKeep[i])
            {
                output[j] = input[i];
                j++;
            }
        }
        return(output);
    }
    private boolean withinStringency(double a, double b)
    {
        double diff = b - a;
        if(diff==0) return true;
        double pcdiff = diff/b*100;
        if(pcdiff>stringency) return false;
        else return true;
    }
    // method to return initially loaded expression data
    double[][] getSecondDataIn()
    {
        return(seconddatain.getMatrix());
    }
    // method to return initially loaded names
    ArrayList<String> getSecondNamesIn()
    {
        return(secondnamesin);
    }
    // method to return initially loaded sRNA expression data
    double[][] getFirstDataIn()
    {
        return(firstdatain.getMatrix());
    }
    // method to return initially loaded sRNA names
    ArrayList<String> getFirstNamesIn()
    {
        return(firstnamesin);
    }
    // method to load sRNA data directly
    void loadFirstData(double[][] datain, ArrayList<String> names)
    {
        firstdatain = new DataMatrix(datain);
        firstnamesin = names;
    }
    // method to load data directly
    void loadSecondData(double[][] datain, ArrayList<String> names)
    {
        seconddatain = new DataMatrix(datain);
        secondnamesin = names;
    }
    // methods to read expression data from input .csv files
    void loadFirstInputFile(String file_name, boolean hasheader)
    {
        File inFile = null;
        try
        {
            inFile = new File(file_name);
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: problem with file '"+file_name);
            System.err.println(e);
            firstdatavalidity = false;
        } 
        loadFirstInputFile(inFile,  hasheader);
    }
    void loadFirstInputFile(File inFile, boolean hasheader)
    {
        String file_name = "";
        nc = nsamples*nreps;
        int i = 0;
        //PrintWriter writer = null;
        //String logFileName = "";
        try
        {   // if exception occurs will be set to false, true does not mean data
            // is any good, only that it has the right format
            file_name = inFile.getPath();
            firstdatavalidity = true;
            // read and check sRNA data 
            fpl.println("FiRePat: reading and checking first data file '"+file_name+"'");     
            Scanner in = new Scanner(inFile);
            in.useDelimiter("[/n]");
            String [] header = in.nextLine().trim().split(",", -1);// req -1 to prevent trimming empty strings
            int hl = header.length-1;
            String[] dataheader = new String[hl];
            System.arraycopy(header, 1, dataheader, 0, hl);
            // need to throw exception here if hl does not match number of data columns expected
            if(nc!=hl)
            {
                fpl.println("FiRePat: number of data columns ("+hl+") in file '"+file_name+"' does not match the number ("+nsamples+") required.");
                throw new Exception("FiRePat: number of data columns in file '"+file_name+"' does not match the number required.");
            }
            // now get number of rows, assume columns correct
            String line;
            int nr = 0;
            while (in.hasNextLine()) 
            {
                line = in.nextLine();
                nr++;
            }      
            // now get data from file but skip header first
            ArrayList<String> srnanamesList = new ArrayList<>();
            in = new Scanner(inFile);
            in.useDelimiter("[/n]");
            i = 0;
            if(hasheader) line = in.nextLine(); //skip header line
            else nr++; // need extra row as first row is data
            double[][] expdata = new double[nr][nc];
            while (i<nr) 
            {
                line = in.nextLine().trim();
                Scanner lineIn = new Scanner(line);
                lineIn.useDelimiter(",");
                String srnai = lineIn.next().trim();
                srnanamesList.add(srnai);
                int j = 0;
                while(lineIn.hasNextLine())
                {
                    double value = Double.parseDouble(lineIn.next().trim());
                    expdata[i][j] = value;
                    j++;
                }
                i++;
            }      
            firstnamesin = srnanamesList;
            firstdatain = new DataMatrix(expdata);
         /*   try {writer.close();} 
            catch (Exception ex){fpl.println("FiRePat: cannot close log file '"+logFileName+"'");}*/
        }
        catch(IOException e)
        {
            fpl.println("FiRePat: file '"+ file_name + "' not found");
            firstdatavalidity = false;
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: problem with file '"+file_name+"' data line "+(i+1));
            System.err.println(e);
            firstdatavalidity = false;
        } 
    }      
    void loadSecondInputFile(String file_name, boolean hasheader)
    {
        File inFile = null;
        try
        {
            inFile = new File(file_name);
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: problem with file '"+file_name);
            System.err.println(e);
            firstdatavalidity = false;
        } 
        loadSecondInputFile(inFile,  hasheader);
    }
    void loadSecondInputFile(File inFile, boolean hasheader)
    {
        String file_name = "";
        nc = nsamples*nreps;
        int i = 0;
        //PrintWriter writer = null;
        //String logFileName = "";
        try
        {   // if exception occurs will be set to false, true does not mean data
            // is any good, only that it has the right format
            file_name = inFile.getPath();
            seconddatavalidity = true;            
            // read and check data 
            fpl.println("FiRePat: reading and checking second data file '"+file_name+"'");     
            Scanner in = new Scanner(inFile);
            in.useDelimiter("[/n]");
            String [] header = in.nextLine().trim().split(",", -1);// req -1 to prevent trimming empty strings
            int hl = header.length-1;
            String[] dataheader = new String[hl];
            System.arraycopy(header, 1, dataheader, 0, hl);
            // need to throw exception here if hl does not match number of data columns expected
            if(nc!=hl)
            {
                fpl.println("FiRePat: number of data columns ("+hl+") in file '"+file_name+"' does not match the number ("+nsamples+") required.");
                throw new Exception("FiRePat: number of columns in file '"+file_name+"' does not match the number of data columns required.");
            }
            String line;
            int nr = 0;
            while (in.hasNextLine()) 
            {
                line = in.nextLine();
                nr++;
            }      
            // now get data from file but skip header first
            ArrayList<String> srnanamesList = new ArrayList<>();
            in = new Scanner(inFile);
            in.useDelimiter("[/n]");
            i = 0;
            if(hasheader) line = in.nextLine(); //skip header line
            else nr++; // need extra row as first row is data
            double[][] expdata = new double[nr][nc];
            while (i<nr) 
            {
                line = in.nextLine().trim();
                Scanner lineIn = new Scanner(line);
                lineIn.useDelimiter(",");
                String srnai = lineIn.next().trim();
                srnanamesList.add(srnai);
                int j = 0;
                while(lineIn.hasNextLine())
                {
                    double value = Double.parseDouble(lineIn.next().trim());
                    expdata[i][j] = value;
                    j++;
                }
                i++;
            }      
            secondnamesin = srnanamesList;
            seconddatain = new DataMatrix(expdata);
        }
        catch(IOException e)
        {
            fpl.println("FiRePat: file '"+ file_name + "' not found");
            seconddatavalidity = false;
        }
        catch(Exception e)
        {
            fpl.println("FiRePat: problem with file '"+file_name+"' data line "+(i+1));
            System.err.println(e);
            seconddatavalidity = false;
        } 
    }
    // methods for basic filter to remove noise
    void setNonSrnasFilterLevel(double level)// minimum value required to keep an expression series
    {
        non_srnas_filter_level = level;
    }
    void setNonSrnasFilter(boolean setfilter)// whether to filter or not
    {
        filter_non_srna_data = setfilter;
    }
    // method to return a boolean array indicating which expression series to keep
    boolean[] minExpLevelFilter(double[][] expdata)
    {
        int nr = expdata.length;
        int nc = expdata[0].length;
        boolean[] output = new boolean[nr];
        for(int i=0; i<nr; i++)
        {
            int j = 0;
            while(j<nc)
            {
                double value = expdata[i][j];
                if(value>=non_srnas_filter_level)
                {
                    output[i] = true;
                    j = nr;
                }
                j++;
            }
        }
        return output;
    }
    
    // names of datasets
    public void setFirstDatasetName(String s)
    {
        first_dataset_name = s;          
    }
    public void setSecondDatasetName(String s)
    {
        second_dataset_name = s;
    }
    public String getFirstDatasetName()
    {
        return first_dataset_name;
    }
    public String getSecondDatasetName()
    {
        return second_dataset_name;
    }
    
    // following methods are for use in development, not needed to run program
    void printGeneNames()
    {
        for (String i : secondnames) System.out.println(i);
    }
    void printSrnaNames()
    {
        for (String i : firstnames) System.out.println(i);
    }
    void printSrnaData()
    {
        firstdata.printMatrix();
    }
    void printGeneData()
    {
        seconddata.printMatrix();
    } 
    void writeDoubleArray(double[] values, String filename) throws Exception
    {
        PrintWriter writer = new PrintWriter(filename);
        
        int m = values.length;
        
        for(int i=0; i < m; i++)
        {
            writer.println(values[i]);
            
        }
    }    
    void writeMatrix(double[][] values, String filename) throws Exception
    {
        int m = values.length;
        int n = values[0].length;
        PrintWriter writer = new PrintWriter(filename);
        
        for(int i=0; i < m; i++)
        {
            for(int j=0; j < n-1; j++)
            {
                writer.print(values[i][j]+",");
            }
            writer.println(values[i][n-1]);
        }
        writer.close();
    }
    
    void printintArray(int[] values)
    {
        
        int m = values.length;
        
        for(int i=0; i < m; i++)
        {
            System.out.println(values[i]);
            
        }
    }    
  /*  void printbooleanArray(boolean[] values)
    {
        
        int m = values.length;
        
        for(int i=0; i < m; i++)
        {
            System.out.println(values[i]);
            
        }
    }  */  
    void printArListDou(ArrayList<double[]> values)
    {
        int m = values.size();
        int n = values.get(0).length;
        
        for(int i=0; i < m; i++)
        {
            for(int j=0; j < n-1; j++)
            {
                System.out.print(values.get(i)[j]+",");
            }
            System.out.println(values.get(i)[n-1]);
        }
    }
    void printArListStr(ArrayList<String[]> values)
    {
        int m = values.size();
        int n = values.get(0).length;
        
        for(int i=0; i < m; i++)
        {
            for(int j=0; j < n-1; j++)
            {
                System.out.print(values.get(i)[j]+",");
            }
            System.out.println(values.get(i)[n-1]);
        }
    }
    void printMatrix(double[][] values)
    {
        int m = values.length;
        int n = values[0].length;
        
        for(int i=0; i < m; i++)
        {
            for(int j=0; j < n-1; j++)
            {
                System.out.print(values[i][j]+",");
            }
            System.out.println(values[i][n-1]);
        }
    }
    // crude but effective method to count number of seqs of each size, allLengths is output from measureLengths()
    // now replaced by keepLengths()
    private int[] countLengths(int[] allLengths)
    {// value at output[x] is number of seqs of length x
        int imax = allLengths.length-2;
        int [] output = new int[allLengths[imax+1]+1];
        for(int i=0; i<imax; i++) output[allLengths[i]]++;
        return output;   
    }
    
}
