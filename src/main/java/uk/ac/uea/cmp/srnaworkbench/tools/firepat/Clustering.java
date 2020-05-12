/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author a019349
 */
public class Clustering {
    int nsamples;
    int nreps;
    int ncolumns;
    boolean datavalidity;
    final String FIRST = "FIRST";
    final String SECOND = "SECOND";
    double offset = 20; // default offset for offset fold change (OFC) calculations
    double pcwindow = 5; // percentage  window for cutoff when deciding SDU patterns
    ArrayList<double[]> corrdata; // correlation data 
    String first_dataset_name = "First"; // identifier for the first set of data in output files
    String second_dataset_name = "Second"; // identifier for the second set of data in output files
    double[][] first_data; // expression data from first datraset
    double[][] second_data; // expression data from second dataset
    int[] all_first_index; // to hold index linking subsets with those in original first dataset
    int[] all_second_index; // ditto for second
    String[] first_names;  // seq identifiers
    String[] second_names;  // seq identifiers
    String[] sample_names;  // names of samples/experiments
    String[][] first_patterns; // to hold the patterns of expression from first dataset
    String[][] second_patterns; // ditto second
    String[][] patternsNamesCorrs; // to hold names, patterns, and correlations prior to sorting and output
    // format of output expression level values (alter number of trailing zeros as req)
    String outputDecimalFormat = "#0.000";
    private boolean output_first_dataset_first = true;// controls which results appear first in output tables
    HTMLutil htm; // for exporting results in HTML and LaTeX format;
    double max_first_up; // to record largest increase in expression for seq in first dataset
    double max_first_down; // ditto decrease  
    double max_second_up; // to record largest increase in expression for seq in second dataset
    double max_second_down; // ditto decrease
    FiRePatLog fpl;
    
    Clustering() 
    {
        datavalidity = false;
    }
    Clustering(DataForClustering datain, HTMLutil hutil, FiRePatLog fplog) 
    {
        fpl = fplog;
        htm = hutil;
        first_data = datain.getFirstData();
        second_data = datain.getSecondData();
        all_first_index = datain.getAllFirstIndex();
        all_second_index = datain.getAllSecondIndex();
        first_names = datain.getFirstNames();
        second_names = datain.getSecondNames();
        sample_names = datain.getSampleNames();
        corrdata = datain.getCorrData();   
        nsamples = datain.getNumSamples();
        nreps = datain.getNumReplicates();    
        ncolumns = nsamples * nreps; 
        datavalidity = datain.getDataValidity();
        max_first_up = 0; // initial value
        max_first_down = 0; // initial value
        max_second_up = 0; // initial value
        max_second_down = 0; // initial value
        offset = datain.getOffset();
        if(datavalidity) // only process if data is valid
        {
            // dataset names
            first_dataset_name = datain.getFirstDatasetName();
            second_dataset_name = datain.getSecondDatasetName();
            // process data to get expression patterns
            if(nreps>1)// use max, avg and min values of each set of replicates
            {   // process data to get expression patterns and average expression levels
                // boolean indicates whether from second (true) or first (false) dataset
                first_patterns = getPatternsFromReplicatedDataSet(first_data, first_names, false);
                second_patterns = getPatternsFromReplicatedDataSet(second_data, second_names, true);
            }
            else // no replicates 
            {
                first_patterns = getPatternsFromUnReplicatedDataSet(first_data, first_names, false);
                second_patterns = getPatternsFromUnReplicatedDataSet(second_data, second_names, true);
            }   
            // now make String[][] names, patterns and corrs
            int ncols = (nsamples*2) + 5; // total number of columns required
            int npatterns = first_patterns.length;
            patternsNamesCorrs = new String[npatterns][ncols];
            for(int i = 0; i<npatterns; i++)
            {   // get data
                double[] corrdatai = new double[3];
                corrdatai = corrdata.get(i);
                int first_i = all_first_index[(int)corrdatai[0]];
                int second_i = all_second_index[(int)corrdatai[1]];
                String[] first_pattern_i = first_patterns[first_i];
                String[] second_pattern_i = second_patterns[second_i];
                // now populate row:
                // add id, pattern and expression values
                int j = 0;
                while(j<second_pattern_i.length)
                {
                    patternsNamesCorrs[i][j] = second_pattern_i[j];
                    j++;
                }
                // add id, pattern and expression values 
                for(int k = 0; k<first_pattern_i.length; k++)
                {
                    patternsNamesCorrs[i][j] = first_pattern_i[k];
                    j++;
                }
                // add correlation value
                patternsNamesCorrs[i][j] = doubleAsString(corrdatai[2]);
            }
        }
        else // data not valid, do nothing
        {
            ;
        }       
    }
    // method to sort String[][] by patterns in a given column
    private void sortStringMatrix(String[][] matrix, int column)            
    {
        replaceUDSwithABC(matrix, column);
        Arrays.sort(matrix, (String[] s1, String[] s2) -> s1[column].compareTo(s2[column]));
        replaceABCwithUDS(matrix, column);
    }
    // method to sort String[][] by patterns in a given first column, then on a second; probably not the most efficient way, but it does the job
    private void sortStringMatrix(String[][] matrix, int firstcolumn, int secondcolumn)            
    {   // initial sort on first column
        sortStringMatrix(matrix, firstcolumn);
        // get all patterns in firstcolumn
        String[] patterns = new String[matrix.length];
        for(int i=0; i<matrix.length; i++) patterns[i] = matrix[i][firstcolumn];
        // get unique values
        Set<String> all_patterns = new HashSet<String>();
        for (int i = 0; i < patterns.length; i++) all_patterns.add(patterns[i]);
        String[] unique_patterns = all_patterns.toArray(new String[all_patterns.size()]);
        // iterate over unique values and sort the array rows containing that value by the second column of patterns
        for(int i=0; i<unique_patterns.length; i++)
        {   // get index of first and last occurrence
            int first = -1;
            int last = 0;
            String patterni = unique_patterns[i];
            for(int j=0; j<patterns.length; j++)
            {
                String patternj = patterns[j];
                if(patternj.equals(patterni))
                {
                    if(first<0) first = j; //first occurrence
                    last = j; //latest occurrence
                }
            }
            // check more than one occurrence, otherwise ignore
            if(first<last)
            {   // get cloned subset of the array to sort
                int nrows = last-first+1;
                int k = first;
                String[][] subset = new String[nrows][];
                for(int j=0; j<nrows; j++)
                {
                    subset[j] = matrix[k].clone();
                    k = k+1;
                }
                // sort it
                sortStringMatrix(subset, secondcolumn);
                // replace in original array
                k = first;
                for(int j=0; j<nrows; j++)
                {
                    matrix[k] = subset[j];
                    k = k+1;
                }               
            }
        }
    }   
    // methods to replace letters to control sort order, so that all S patterns sort last
    // change UDS to ABC
    private void replaceUDSwithABC(String[][] matrix, int column)
    {
        for(int i=0; i<matrix.length; i++)
        {
            char[] pattern = matrix[i][column].toCharArray();
            String newpattern = "";
            for(int j = 0; j<pattern.length; j++)
            {
                if(pattern[j]=='U') newpattern = newpattern + 'A';
                else if(pattern[j]=='D') newpattern = newpattern + 'B';
                else newpattern = newpattern + 'C';               
            }
            matrix[i][column] = newpattern;
        }
    }
    // change ABC back to UDS
    private void replaceABCwithUDS(String[][] matrix, int column)
    {
        for(int i=0; i<matrix.length; i++)
        {
            char[] pattern = matrix[i][column].toCharArray();
            String newpattern = "";
            for(int j = 0; j<pattern.length; j++)
            {
                if(pattern[j]=='A') newpattern = newpattern + 'U';
                else if(pattern[j]=='B') newpattern = newpattern + 'D';
                else newpattern = newpattern + 'S';               
            }
            matrix[i][column] = newpattern;
        }
    }
    // get Same/Up/Down pattern from expression levels without replicates
    private String[][] getPatternsFromUnReplicatedDataSet(double[][] data, String[] names, boolean is_second)
    {
        int nseq = data.length;
        int nc = nsamples + 2; // number of columns in output
        String[][] output = new String[nseq][nc];
        for(int i = 0; i<nseq; i++)
        {
            double[][] cutoffs = getCutoffsForPattern(data[i]);
            // now get patterns + values as strings
            String[] patterni = getPatternFromUnReplicatedData(cutoffs, names[i], is_second);
            output[i] = patterni;
        }
        return(output);
    } 
    String[] getPatternFromUnReplicatedData(double[][] avgmaxmin, String name, boolean is_second)
    {
        String pattern = "";// holds SDU coding of expression pattern
        String [] values = new String[nsamples]; // holds strings of expression level values
        // average values, maximims and minimums for windows
        double[] vals = avgmaxmin[0];
        double[] maxs = avgmaxmin[1];
        double[] mins = avgmaxmin[2];
        //values for previous sample
        double prevval = vals[0];
        double prevmax = maxs[0];
        double prevmin = mins[0];
        for(int i=0; i<nsamples; i++)
        {
            //get values for this iteration
            double vali = vals[i];
            double maxi = maxs[i];
            double mini = mins[i];
            // get pattern value at this iteration and log2 OFC value for mean expression levels, unless initial iteration
            double change = 0;
            if(i>0)
            {
                pattern = pattern + getPatternPointValue(vali, maxi, mini, prevval, prevmax, prevmin);
                change = ofcLog2(vali, prevval);
            }
            // series starts from 0 when printed, log2 OFC values are calculated and stored in 'values[i]'
            // for printed output, not calculations, but there would be no difference in SDU coding either way            
            values[i] = doubleAsString(change); 
            // also need to get overall maximum and minimum of these values for when coloured html outout being produced
            setMaxChange(change, is_second);
            //update previous values for next iteration
            prevval = vali;
            prevmax = maxi;
            prevmin = mini;
        }
        // output, posn 0 holds name, posn 1 holds SDU coded pattern, remainder the average expression levels
        int outputlength = nsamples + 2;
        String[] output = new String[outputlength];
        output[0] = name; // id
        output[1] = pattern; //SDU pattern
        for(int i = 2; i<outputlength; i++) // mean expression values
        {
            output[i] = values[i-2];
        }
        return output;
    }
    // get Same/Up/Down pattern from expression levels with replicates
    // all expression patterns
    private String[][] getPatternsFromReplicatedDataSet(double[][] data, String[] names, boolean is_second)
    {
        int nseq = data.length;
        int nc = nsamples + 2; // number of columns in output
        String[][] output = new String[nseq][nc];
        for(int i = 0; i<nseq; i++)
        {
            double[][] avgmaxmin = getAvgMaxMin(data[i]);
            // now get patterns + values as strings
            String[] patterni = getPatternFromReplicatedData(avgmaxmin, names[i], is_second);
            output[i] = patterni;
        }
        return(output);
    }   
    // individual pattern and values
    private String[] getPatternFromReplicatedData(double[][] avgmaxmin, String name, boolean is_second)
    {
        String pattern = "";// holds SDU coding of expression pattern
        String [] values = new String[nsamples]; // holds strings of expression level values
        // average values, maximims and minimums for windows
        double[] vals = avgmaxmin[0];
        double[] maxs = avgmaxmin[1];
        double[] mins = avgmaxmin[2];
        //values for previous sample
        double prevval = vals[0];
        double prevmax = maxs[0];
        double prevmin = mins[0];
        for(int i=0; i<nsamples; i++)
        {
            //get values for this iteration
            double vali = vals[i];
            double maxi = maxs[i];
            double mini = mins[i];
            // get pattern value at this iteration and log2 OFC value for worse case scenario, unless initial iteration
            double change = 0;
            if(i>0)
            {
                String patterni = getPatternPointValue(vali, maxi, mini, prevval, prevmax, prevmin);
                pattern = pattern + patterni;
                change = ofcLog2repData(vali, maxi, mini, prevval, prevmax, prevmin, patterni);             
            }
            // series starts from 0 when printed, log2 OFC values are calculated and stored in 'values[i]'
            // for printed output, not calculations, but there would be no difference in SDU coding either way            
            values[i] = doubleAsString(change); 
            // also need to get overall maximum and minimum of these values for when coloured html outout being produced
            setMaxChange(change, is_second);
            //update previous values for next iteration
            prevval = vali;
            prevmax = maxi;
            prevmin = mini;           
        }
        // output, posn 0 holds name, posn 1 holds SDU coded pattern, remainder the average expression levels
        int outputlength = nsamples + 2;
        String[] output = new String[outputlength];
        output[0] = name; // id
        output[1] = pattern; //SDU pattern
        for(int i = 2; i<outputlength; i++) // mean expression values
        {
            output[i] = values[i-2];
        }
        return output;
    }
    // get letter code for individual data point
    private String getPatternPointValue(double val, double max, double min, double prevval, double prevmax, double prevmin)
    {
        String output = "S";// same
        if(val==prevval) return(output);
        if(val>prevval){ if(min>prevmax) output = "U";}// up
        else  {if(max<prevmin) output = "D";}// down
        return(output);
    }
    // get average, maximum and minimum values for each set of replicates
    private double[][] getAvgMaxMin(double[] values)
    {   // array to hold avg max and min values for each set of samples
        int start = 0;
        int end = nreps;
        double[][] avgmaxmin = new double[3][nsamples];
        for(int i = 0; i<nsamples; i++) //loop through each set of samples
        {
            double[] datai = new double[nreps];
            datai = Arrays.copyOfRange(values, start, end);
            start = start + nreps;
            end = end + nreps;
            double total = datai[0];
            double maximum = datai[0];
            double minimum = datai[0];
            for(int j = 1; j<nreps; j++)
            {
                double vj = datai[j];
                total = total + vj;
                if(vj>maximum) maximum = vj;
                if (vj< minimum) minimum = vj;
            }
            avgmaxmin[0][i] = total/nreps;
            avgmaxmin[1][i] = maximum;
            avgmaxmin[2][i] = minimum; 
        }
        return(avgmaxmin);
    }
    // get values equivalent to those from getAvgMaxMin() but for unreplicated data
    private double[][] getCutoffsForPattern(double[] values)
    {   // array to hold max and min cutoff values for each set of samples
        double[][] output = new double[3][nsamples];
        for(int i = 0; i<nsamples; i++) //loop through samples
        {
            double datai = values[i];
            // window is +/- 5%, or offset, whichever is greater
            double window = datai*pcwindow/100;
            if(window<offset) window = offset;
            double maximum = datai + window;
            double minimum = datai - window;
            output[0][i] = datai;
            output[1][i] = maximum;
            output[2][i] = minimum;
        }
        return(output);
    }
    
    String doubleAsString(double value)
    {
        NumberFormat formatter = new DecimalFormat(outputDecimalFormat);
        String output=formatter.format(value);
        return(output);
    }
    // method to print the clustered resuilts to .csv file, includeTransformedPattern
     // indicates whether the SDU patterns are to be included in the output
     void writeSortedPatternsToCsv(String fileName, String sortby, boolean includeTransformedPattern)
     {
         writeSortedPatterns(fileName, sortby, includeTransformedPattern, false);
     }
     // method to write cytoscape .csv file
     void writeSortedPatternsToCytoscape(String fileName, String sortby)
     {
         writeSortedPatterns(fileName, sortby, false, true);
     }
     // method to actually write the standard .csv and cytoscape files
     private void writeSortedPatterns(String fileName, String sortby, boolean includeTransformedPattern, boolean iscytoscape)
     {   // ensure .csv at end of file name
        int fnlength = fileName.length();
        if(fnlength>4)
        {
            String last4 = fileName.substring(fnlength-4).toLowerCase();
            if(!last4.equals(".csv")) fileName = fileName + ".csv";
        }
        if(datavalidity)
        {
            if(iscytoscape) fpl.println("FiRePat: writing Cytoscape input file '"+fileName+"'");
            else fpl.println("FiRePat: writing sorted patterns to file '"+fileName+"'");
            sortPatterns(sortby);
            PrintWriter writer = null;
            try 
            {
                writer = new PrintWriter(fileName, "UTF-8");
                int nrows = patternsNamesCorrs.length;
                int ncols = patternsNamesCorrs[0].length;
                int ncolsout = ncols;
                if(!includeTransformedPattern) ncolsout = ncols-2;
                // write row by row
                if(includeTransformedPattern)
                {   // write header
                    String header = "";
                    if(!output_first_dataset_first) header = second_dataset_name+",Pattern,";
                    else header = first_dataset_name+",Pattern,";                                             
                    int s1 = (ncolsout-1)/2-1;
                    int h = 0;
                    for(int i = 1; i<s1; i++)
                    {
                        if(sample_names!=null)
                        {
                            header = header + sample_names[h];
                            h++;
                        }
                        header = header+",";
                    }
                    if(!output_first_dataset_first) header = header + first_dataset_name+",Pattern,";
                    else header = header + second_dataset_name+",Pattern,";                                             
                    s1 = s1 + 3;
                    int s2 = ncolsout-1;
                     h = 0;
                    for(int i = s1; i<s2; i++)
                    {
                        if(sample_names!=null)
                        {
                            header = header + sample_names[h];
                            h++;
                        }
                        header = header+",";
                    }
                    header = header+"Correlation";
                    writer.println(header);
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first data to be printed first
                        if(output_first_dataset_first) input = reorderOutput(input);                       
                        String output = input[0];
                        for(int j=1; j < ncols; j++) output = output+","+input[j];
                        writer.println(output);
                    }
                }
                else // omit SDU patterns Cytoscape files are written here, output is only IDs and correlations
                {   // write header
                    String header = "";
                    if(!output_first_dataset_first) header = second_dataset_name+",";
                    else header = first_dataset_name+",";                                             
                    int s1 = (ncolsout-1)/2;
                    int h = 0;
                    for(int i = 1; i<s1; i++)
                    {
                        if(sample_names!=null)
                        {
                            if(!iscytoscape) header = header + sample_names[h];
                            h++;
                        }
                        if(!iscytoscape) header = header+",";
                    }
                    if(!output_first_dataset_first) header = header+first_dataset_name+",";
                    else header = header+second_dataset_name+",";                                             
                    s1 = s1 + 1;
                    int s2 = ncolsout-1;
                    h = 0;
                    for(int i = s1; i<s2; i++)
                    {
                        if(sample_names!=null)
                        {
                            if(!iscytoscape) header = header + sample_names[h];
                            h++;
                        }
                        if(!iscytoscape) header = header+",";
                    }
                    header = header+"Correlation";
                    writer.println(header);
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first data to be printed first
                        if(output_first_dataset_first) input = reorderOutput(input);
                        String output = input[0];
                        // skip first SDU pattern
                        int j = 2;// input index
                        int x = (ncols-5)/2+3;// index of second SDU pattern
                        while(j<x)
                        {
                            if(!iscytoscape) output = output+","+input[j];
                            j++;
                        }
                        if(iscytoscape) output = output+","+input[j-1];
                        j++; // skip second SDU pattern
                        while(j<ncols)
                        {
                            if(!iscytoscape) output = output+","+input[j];
                            j++;
                        }
                        if(iscytoscape) output = output+","+input[j-1];
                        writer.println(output);
                    }
                }
            }
            catch (IOException ex) 
            {
                fpl.println("FiRePat: IO exception");
            }
            finally 
            {
                 try {writer.close();} 
                 catch (Exception ex) 
                 {
                        fpl.println("FiRePat: cannot close output file '"+fileName+"'");
                 }
            } 
        }
        else fpl.println("FiRePat: no results to write to output file '"+fileName+"'");
    }
    
    void writeSortedPatternsToHtml(String fileName, String sortby, boolean includeTransformedPattern)
    {
        String headerAlignment = "left"; // temporary, sets alignment of html table header cells
        // ensure .html at end of file name
        int fnlength = fileName.length();
        if(fnlength>4)
        {
            String last5 = fileName.substring(fnlength-5).toLowerCase();
            if(!last5.equals(".html")) fileName = fileName + ".html";
        }        
        if(datavalidity)
        {
            fpl.println("FiRePat: writing sorted patterns to file '"+fileName+"'");
            // first call method to set colour values for output of different OFC values
            htm.setOutputColourIntensities(patternsNamesCorrs);
            sortPatterns(sortby);
            htm.setMaxUpDown(max_second_up, max_second_down, max_first_up, max_first_down);
            PrintWriter writer = null;
            try 
            {
                writer = new PrintWriter(fileName, "UTF-8");
                // add html code for file up to start of table
                writer.println(htm.startDoc());
                int nrows = patternsNamesCorrs.length;
                int ncols = patternsNamesCorrs[0].length;
                int ncolsout = ncols;
                if(!includeTransformedPattern) ncolsout = ncols-2;
                // write row by row
                if(includeTransformedPattern)
                {   // write file header
                    writer.println(htm.startTableHead());// start of table
                    // write table header
                    writer.println(htm.startTableRow());// start of header row
                    int tc = 1; // number for cell in html table
                    if(!output_first_dataset_first)
                    {
                        writer.println(htm.plainHeaderCell(second_dataset_name, tc, headerAlignment));// 1st cell
                        tc++;
                        writer.println(htm.plainHeaderCell("Pattern", tc, headerAlignment));// 2nd cell
                        tc++;
                    }
                    else
                    {
                        writer.println(htm.plainHeaderCell(first_dataset_name, tc, headerAlignment));
                        tc++;
                        writer.println(htm.plainHeaderCell("Pattern", tc, headerAlignment));
                        tc++;
                    }                                             
                    int s1 = (ncolsout-1)/2-1;
                    int h = 0;
                    for(int i = 1; i<s1; i++)
                    {
                        if(sample_names!=null) writer.println(htm.plainHeaderCell(sample_names[h], tc, headerAlignment));
                        else    writer.println(htm.plainHeaderCell("", tc, headerAlignment));
                        h++; tc++;
                    }
                    if(!output_first_dataset_first)
                    {
                        writer.println(htm.plainHeaderCell(first_dataset_name, tc, headerAlignment));
                        tc++;
                        writer.println(htm.plainHeaderCell("Pattern", tc, headerAlignment));
                        tc++;
                    }
                    else
                    {
                        writer.println(htm.plainHeaderCell(second_dataset_name, tc, headerAlignment));
                        tc++;
                        writer.println(htm.plainHeaderCell("Pattern", tc, headerAlignment));
                        tc++;
                    }                                             
                    s1 = s1 + 3;
                    int s2 = ncolsout-1;
                    h = 0;
                    for(int i = s1; i<s2; i++)
                    {
                        if(sample_names!=null) writer.println(htm.plainHeaderCell(sample_names[h], tc, headerAlignment));
                        else    writer.println(htm.plainHeaderCell("", tc, headerAlignment));
                        h++; tc++;
                    }
                    writer.println(htm.plainHeaderCell("Correlation", tc, headerAlignment));
                    writer.println(htm.endTableRow());// end of header row
                    writer.println(htm.endTableHead());// end of table header
                    writer.println(htm.startTableBody());// start table body                   
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first dataset data to be printed first
                        // using these booleans in this way is a convoluted way of telling the HTMLutil what type if data
                        // is is dealing with, but it does work!
                        boolean is_second = true;
                        boolean is_first = false;
                        if(output_first_dataset_first) 
                        {
                            input = reorderOutput(input);
                            is_second = false;
                            is_first = true;
                        }
                        writer.println(htm.startTableRow());// start row
                        tc = 1;                        
                        writer.println(htm.plainTableCell(input[0], tc, ""));// id
                        tc++;
                        // print first SDU pattern
                        writer.println(htm.plainTableCell(input[1], tc, ""));
                        // print expr level, always zero value and "S" (same) SDU value
                        writer.println(htm.startTableCell(input[2], tc, ""));
                        tc++;
                        int j = 3;// input index
                        int x = (ncols-5)/2+2;
                        int k = 0; //index of sdupattern
                        // print remaining expression levels
                        while(j<x)
                        {
                            writer.println(htm.patternTableCell(input[j], tc, "", is_second));
                            tc++; j++; k++;
                        }
                        // print id
                        writer.println(htm.plainTableCell(input[j], tc, ""));
                        tc++; j++;                       
                        // print second SDU pattern
                        writer.println(htm.plainTableCell(input[j], tc, ""));
                        j++;
                        // print expr level, always zero value and "S" (same) SDU value
                        writer.println(htm.startTableCell(input[2], tc, ""));
                        tc++; j++;
                        x = ncols-1;
                        k = 0;
                        while(j<x)
                        {
                            writer.println(htm.patternTableCell(input[j], tc, "", is_first));
                            tc++; j++; k++;
                        }
                        writer.println(htm.plainTableCell(input[j], tc, ""));
                        writer.println(htm.endTableRow());//end row
                    }
                    writer.println(htm.endTableBody());// end table body                   
                }
                else // omit SDU patterns
                {   // write header
                    String header = "";
                    // write file header
                    writer.println(htm.startTableHead());// start of table
                    // write table header
                    writer.println(htm.startTableRow());// start of header row
                    int tc = 1; // number for cell in html table
                    if(!output_first_dataset_first) writer.println(htm.plainHeaderCell(second_dataset_name, tc, headerAlignment));
                    else writer.println(htm.plainHeaderCell(first_dataset_name, tc, headerAlignment));
                    tc++;
                    int s1 = (ncolsout-1)/2;
                    int h = 0;
                    for(int i = 1; i<s1; i++)
                    {
                        if(sample_names!=null) writer.println(htm.plainHeaderCell(sample_names[h], tc, headerAlignment));
                        else    writer.println(htm.plainHeaderCell("", tc, headerAlignment));
                        h++; tc++;                        
                    }
                    if(!output_first_dataset_first)  writer.println(htm.plainHeaderCell(first_dataset_name, tc, headerAlignment));
                    else  writer.println(htm.plainHeaderCell(second_dataset_name, tc, headerAlignment));  
                    s1 = s1 + 1;
                    int s2 = ncolsout-1;
                    h = 0;
                    tc++;
                    for(int i = s1; i<s2; i++)
                    {
                        if(sample_names!=null) writer.println(htm.plainHeaderCell(sample_names[h], tc, headerAlignment));
                        else    writer.println(htm.plainHeaderCell("", tc, headerAlignment));
                        h++; tc++;
                    }
                    writer.println(htm.plainHeaderCell("Correlation", tc, headerAlignment));
                    writer.println(htm.endTableRow());// end of header row
                    writer.println(htm.endTableHead());// end of table header
                    writer.println(htm.startTableBody());// start table body                   
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first dataset data to be printed first
                        // using these booleans in this way is a convoluted way of telling the HTMLutil what type if data
                        // is is dealing with, but it does work!
                        boolean is_second = true;
                        boolean is_first = false;
                        if(output_first_dataset_first) 
                        {
                            input = reorderOutput(input);
                            is_second = false;
                            is_first = true;
                        }
                        String output = input[0];
                        writer.println(htm.startTableRow());// start row
                        tc = 1;                        
                        writer.println(htm.plainTableCell(input[0], tc, ""));// id
                        tc++;
                        // skip first SDU pattern
                        String[] sdupattern = input[1].split("");
                        // print expr level, always zero value and "S" (same) SDU value
                        writer.println(htm.startTableCell(input[2], tc, ""));
                        tc++;
                        int j = 3;// input index
                        int x = (ncols-5)/2+2;
                        int k = 0; //index of sdupattern
                        // print remaining expression levels
                        while(j<x)
                        {
                            writer.println(htm.patternTableCell(input[j], tc, "", is_second));
                            tc++; j++; k++;
                        }
                        // print id
                        writer.println(htm.plainTableCell(input[j], tc, ""));
                        tc++; j++;                       
                        // skip second SDU pattern
                        j++;
                        // print expr level, always zero value and "S" (same) SDU value
                        writer.println(htm.startTableCell(input[2], tc, ""));
                        tc++; j++;
                        x = ncols-1;
                        k = 0;
                        while(j<x)
                        {
                            writer.println(htm.patternTableCell(input[j], tc, "", is_first));
                            tc++; j++; k++;
                        }
                        writer.println(htm.plainTableCell(input[j], tc, ""));
                        writer.println(htm.endTableRow());//end row                  
                    }
                }
                writer.println(htm.endDoc()); //  end of document
            }
            catch (IOException ex) 
            {
                fpl.println("FiRePat: IO exception");
            }
            finally 
            {
                 try {writer.close();} 
                 catch (Exception ex) 
                 {
                        fpl.println("FiRePat: cannot close output file '"+fileName+"'");
                 }
            }       
        }
        else fpl.println("FiRePat: no results to write to output file file '"+fileName+"'");
    }
    void writeSortedPatternsToLatex(String fileName, String sortby, boolean includeTransformedPattern)
    {
        // ensure .tex at end of file name
        int fnlength = fileName.length();
        if(fnlength>3)
        {
            String last4 = fileName.substring(fnlength-4).toLowerCase();
            if(!last4.equals(".tex")) fileName = fileName + ".tex";
        }        
        if(datavalidity)
        {
            fpl.println("FiRePat: writing sorted patterns to file '"+fileName+"'");
            // first call method to set colour values for output of different OFC values
            htm.setOutputColourIntensities(patternsNamesCorrs);
            sortPatterns(sortby);
            htm.setMaxUpDown(max_second_up, max_second_down, max_first_up, max_first_down);
            htm.setFirstDatasetName(first_dataset_name);
            htm.setSecondDatasetName(second_dataset_name);
            PrintWriter writer = null;
            try 
            {
                writer = new PrintWriter(fileName, "UTF-8");
                // add html code for file up to start of table
                writer.println(htm.startLatexDoc());
                // writer.println(htm.latexColourCommandsDoc());
                int nrows = patternsNamesCorrs.length;
                int ncols = patternsNamesCorrs[0].length;
                int ncolsout = ncols;
                if(!includeTransformedPattern) ncolsout = ncols-2;
                // define latex colours
                String[] latex_code = htm.makeLatexColorCommands()[2];
                int ncolours = latex_code.length;
                int colcount = 1;
                int ncolperrow = 4;
                for(int c=0; c<ncolours; c++)
                {
                    writer.print(latex_code[c]+" ");
                    if(colcount==4)
                    {
                        colcount = 1;
                        writer.println();
                    }
                    else colcount = colcount+1;
                }
                if(colcount>1) writer.println();
                // write latex table header
                writer.println(htm.startLatexTable(nsamples, ncolsout, includeTransformedPattern, output_first_dataset_first, sample_names));// start of table               
                // write row by row
                if(includeTransformedPattern)
                {                    
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first dataset data to be printed first
                        // using these booleans in this way is a convoluted way of telling the HTMLutil what type if data
                        // it is dealing with, but it works!
                        boolean is_second = true;
                        boolean is_first = false;
                        if(output_first_dataset_first) 
                        {
                            input = reorderOutput(input);
                            is_second = false;
                            is_first = true;
                        }
                        int tc = 1; // counter for column number
                        // print id of 'first' dataset seq
                        writer.print(input[0]);
                        tc++;
                        // print first SDU pattern
                        writer.print(" & " + input[1]);
                        writer.print(htm.startLatexTableCell(input[2]));
                        tc++;
                        int j = 3;// input index
                        int x = (ncols-5)/2+2;
                        int k = 0; //index of sdupattern
                        // print remaining expression levels
                        while(j<x)
                        {
                            writer.print(htm.patternLatexTableCell(input[j], is_second));
                            tc++; j++; k++;
                        }
                        // print id
                        writer.print(" & " + input[j]);
                        tc++; j++;                       
                        // print second SDU pattern
                        writer.print(" & " + input[j]);
                        j++;
                        writer.print(htm.startLatexTableCell(input[2])); 
                       tc++; j++;
                        x = ncols-1;
                        k = 0;
                        while(j<x)
                        {
                            writer.print(htm.patternLatexTableCell(input[j], is_first));
                            tc++; j++; k++;
                        }
                        // print corr value and end of row commands
                        writer.println(" & " + input[j] + " \\\\ \\hline");
                    }
                }
                else
                {
                    for(int i = 0; i<nrows; i++)
                    {
                        String[] input = patternsNamesCorrs[i];
                        // now reorder if first dataset data to be printed first
                        // using these booleans in this way is a convoluted way of telling the HTMLutil what type if data
                        // it is dealing with, but it works!
                        boolean is_second = true;
                        boolean is_first = false;
                        if(output_first_dataset_first) 
                        {
                            input = reorderOutput(input);
                            is_second = false;
                            is_first = true;
                        }
                        int tc = 1; // counter for column number
                        // print id of 'first' dataset seq
                        writer.print(input[0]);
                        tc++;
                        writer.print(htm.startLatexTableCell(input[2]));
                        tc++;
                        int j = 3;// input index
                        int x = (ncols-5)/2+2;
                        int k = 0; //index of sdupattern
                        // print remaining expression levels
                        while(j<x)
                        {
                            writer.print(htm.patternLatexTableCell(input[j], is_second));
                            tc++; j++; k++;
                        }
                        // print id
                        writer.print(" & " + input[j]);
                        tc++; j++;                       
                        j++;
                        writer.print(htm.startLatexTableCell(input[2])); 
                       tc++; j++;
                        x = ncols-1;
                        k = 0;
                        while(j<x)
                        {
                            writer.print(htm.patternLatexTableCell(input[j], is_first));
                            tc++; j++; k++;
                        }
                        // print corr value and end of row commands
                        writer.println(" & " + input[j] + " \\\\ \\hline");
                    }
                }
                writer.println(htm.endLatexDoc()); //  end of document
            }
            catch (IOException ex) 
            {
                fpl.println("FiRePat: IO exception");
            }
            finally 
            {
                 try {writer.close();} 
                 catch (Exception ex) 
                 {
                        fpl.println("FiRePat: cannot close output file '"+fileName+"'");
                 }
            }       
        }
        else fpl.println("FiRePat: no results to write to output file '"+fileName+"'");
            
    }
    void sortPatterns(String sortby)
    {   
        int n = patternsNamesCorrs[0].length;                    
        int firstsortcol = 1;// index of first column of patterns
        int secondsortcol = (n-5)/2+3;// index of second column of patterns
        if(sortby.equals(SECOND))
        {
            sortStringMatrix(patternsNamesCorrs, firstsortcol, secondsortcol);
        }     
        if(sortby.equals(FIRST))
        {
            sortStringMatrix(patternsNamesCorrs, secondsortcol, firstsortcol);  
        }
    }
    public void setFirstDatasetFirst(boolean b)
    {
        output_first_dataset_first = b;
    }
    // method to reorder the output data when first dataset data to be printed first
    public String[] reorderOutput(String[] input)
    {
        int i = 0;
        int inlength = input.length;
        int j = (inlength-1)/2;
        int imax = j;
        String[] output = new String[inlength];
        while(i<imax)
        {
            output[i] = input[j];
            output[j] = input[i]; 
            i++;
            j++;
        }
        output[j] = input[j];
        return(output);
    }
    // method to reset the sample names, checks if correct length
    // does nothing if data not valid, returns false if wrong number of samples
    // otherwise returns true
    public boolean updateSampleNames(String[] smpnames)
    {
        if(datavalidity)
        {
            int numsamnames = smpnames.length;
            if(smpnames.length==nsamples)
            {
                sample_names = smpnames;
                fpl.println("FiRePat: sample names updated");
                return(true);
            }
            else
            {
                fpl.println("FiRePat: number of sample names ("+numsamnames+") does not"+
                    " match number of samples ("+nsamples+"), sample names cannot be updated");
                return(false);
            }
        }
        else return(true);
    }
    public boolean getDataValidity()
    {
        return(datavalidity);
    }
    private void setMaxChange(double value, boolean is_second)
    {   // check if value of change is greater than stored value
        if(is_second) // change in value of expression
        {
            if(value>0)// increase
            {
                if(value>max_second_up) max_second_up = value;
            }
            if(value<0)// decrease
            {
                if(value<max_second_down)  max_second_down = value;
            }      
        }
        else 
        {
            if(value>0)// increase
            {
                if(value>max_first_up) max_first_up = value;
            }
            if(value<0)// decrease
            {
                if(value<max_first_down) max_first_down = value;
            }         
        }
    }
    private double ofcLog2(double a, double b)
    {// calculate log-2 OFC
        return(Math.log((a+offset)/(b+offset)) / Math.log(2));
    }
    private double ofcLog2repData(double val, double max, double min, double prevval, double prevmax, double prevmin, String pattern)
    {// calculate log-2 OFC for replicated day, using the 'worstcase' scenario
        if(pattern.equals("S")) return(0);// no change, return 0
        // up, so calc OFC for change from previous max to current min
        if(pattern.equals("U")) return(Math.log((min+offset)/(prevmax+offset)) / Math.log(2));
        // otherwise down, calc OFC for change from previous min to current max
        return(Math.log((max+offset)/(prevmin+offset)) / Math.log(2));
    }
}
