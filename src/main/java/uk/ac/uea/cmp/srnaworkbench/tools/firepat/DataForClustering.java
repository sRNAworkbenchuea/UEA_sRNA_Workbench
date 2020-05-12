/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author a019349
 */
public class DataForClustering {
    double[][] allfirstdata; //all expression data
    double[][] allseconddata; //all expression data
    int[] allfirstindex; // to hold index of which seqs in firstdata correspond with which in allfirstdata
    int[] allsecondindex; // ditto for second
    double[][] firstdata; // expression data required for clustering, subset of allfirstdata for convenience
    double[][] seconddata; // ditto for second
    ArrayList<double[]> corrdata; // correlation data
    ArrayList<String[]> first_second_names; // names of each seq pair for which correlation has passed filter, corresponds to order in corr data
    
    String[] firstnames;  // ids
    String[] secondnames;  // ids
    String[] samplenames; // names of samples/experiments
    String first_dataset_name = "First"; // identifier for the first set of data in output files
    String second_dataset_name = "Second"; // identifier for the second set of data in output files
    
    
    ArrayList<double[]> firstexpdata; // expression data for each seq, corresponds to order in corrdata, has replicates
    ArrayList<double[]> secondexpdata; // expression data for each seq, corresponds to order in corrdata, has replicates
    int nsamples;
    int nreps;
    int ncolumns;
    boolean datavalidity;
    double offset = 20; // default offset for offset fold change (OFC) calculations
    FiRePatLog fpl;
            
    DataForClustering()
    {
        datavalidity = false;
    }
    // corrs contains correlation data, datain contains expression profile data
    DataForClustering(Correlations corrs, FiRePatData datain, FiRePatLog fplog)
    {
        fpl = fplog;
        datavalidity = datain.getDataValidity();
        if(datavalidity)
        {
            samplenames = datain.getSampleNames();
            offset = datain.getOffset();
            first_dataset_name = datain.getFirstDatasetName();
            second_dataset_name = datain.getSecondDatasetName();
            // correlation data we use to identify which seqs are to be kept
            corrdata = corrs.getCorrelationData();
            int numcorr = corrdata.size(); // number of correlation values
            first_second_names = new ArrayList<>();
            firstexpdata = new ArrayList<>();
            secondexpdata = new ArrayList<>();
            // check that we have any correlations!
            if(numcorr==0)
            {
                fpl.println("FiRePat: number of correlations is zero!");
                datavalidity = false;
            }
            else
            {
                // number of data samples, replicates and data columns
                fpl.println("FiRePat: getting subset of data for clustering");
                nsamples = datain.getNumSamples();
                nreps = datain.getNumReplicates();    
                ncolumns = datain.getNumColumns();

                //all expression profiles
                allfirstdata = datain.getFirstData().getMatrix(); 
                allseconddata = datain.getSecondData().getMatrix();   
                // indexes to data, initially fill with -1 for safety
                allfirstindex = new int[allfirstdata.length];
                Arrays.fill(allfirstindex, -1);
                allsecondindex = new int[allseconddata.length];
                Arrays.fill(allsecondindex, -1);
                
                // all seqs 
                String [] all_first_names = datain.getFirstNames();
                String[] all_second_names = datain.getSecondNames();  

                // check which seqs are in corrdata
                // Boolean []s to indicate which seqs present, initially all false
                int total_first = all_first_names.length;
                int total_second = all_second_names.length;
                Boolean[] first_used = new Boolean[total_first];
                Boolean[] second_used = new Boolean[total_second];
                Arrays.fill(first_used, Boolean.FALSE);
                Arrays.fill(second_used, Boolean.FALSE);
                // loop through corrdata to get seq pairs for each correlation
                for(int i = 0; i<numcorr; i++)
                {   
                    int first_i = (int)corrdata.get(i)[0];// index;
                    String first_i_name = all_first_names[first_i];// idenifier
                    firstexpdata.add(allfirstdata[first_i]);// add expression data
                    int second_i = (int)corrdata.get(i)[1];// index;
                    String second_i_name = all_second_names[second_i];// identifier;
                    secondexpdata.add(allseconddata[second_i]);// add expression data
                    String [] seq_pair_names_i = new String[]{first_i_name,second_i_name};
                    first_second_names.add(seq_pair_names_i);// add the identifiers
                    first_used[first_i] = true;
                    second_used[second_i] = true;          
                }
            
                // ArrayLists to hold subsets of names and expression profiles of sRNA and genes used
                ArrayList<String> fnames = new ArrayList<>();
                ArrayList<String> snames = new ArrayList<>();
                
                int afindex = 0;// index 
                int asindex = 0;// index 
                // get ids of first dataset seqs
                for(int i = 0; i<total_first; i++)
                {
                    boolean first_req = first_used[i];
                    if(first_req)
                    {
                        fnames.add(all_first_names[i]);
                        allfirstindex[i] = afindex;
                        afindex++;
                    }
                }
                // get ids of second dataset seqs
                for(int i = 0; i<total_second; i++)
                {
                    boolean second_req = second_used[i];
                    if(second_req)
                    {
                        snames.add(all_second_names[i]);
                        allsecondindex[i] = asindex;
                        asindex++;
                    }
                }
                int num_first = fnames.size();
                int num_second = snames.size();
                firstnames = fnames.toArray(new String[fnames.size()]);
                secondnames = snames.toArray(new String[snames.size()]);     

                // get profiles of which sRNAs and genes are to be kept and put into double[][]s
                firstdata = new double[num_first][ncolumns]; 
                seconddata = new double[num_second][ncolumns];           
                int findex = 0; // index of firstdata row       
                int sindex = 0; // index of seconddata row
                for(int i = 0; i<total_first; i++)
                {
                    boolean first_req = first_used[i];
                    if(first_req)
                    {
                        firstdata[findex] =  allfirstdata[i];
                        findex++;
                    }
                }
                // get names of which genes are to be used
                for(int i = 0; i<total_second; i++)
                {
                    boolean second_req = second_used[i];
                    if(second_req)
                    {
                        seconddata[sindex] = allseconddata[i];
                        sindex++;
                    }
                }
                fpl.println("FiRePat: data subsetting completed");
            }
        }
        else
        {
            fpl.println("FiRePat: input data not valid, unable to process"); 
        }
    }
    
    
    int[] getAllFirstIndex()
    {
        return allfirstindex;
    }    
    int[] getAllSecondIndex()
    {
        return allsecondindex;
    }    
    double [][] getFirstData()
    {
        return(firstdata);
    }
    double [][] getSecondData()
    {
        return(seconddata);
    }
    String [] getFirstNames()
    {
        return(firstnames);
    }
    String [] getSecondNames()
    {
        return(secondnames);
    }
    boolean getDataValidity()
    { 
        return(datavalidity);
    }
    int getNumSamples()
    { 
        return(nsamples);
    }
    int getNumReplicates()
    { 
        return(nreps);
    }
    ArrayList<double[]> getCorrData()
    {
        return(corrdata);
    }
    String [] getSampleNames()
    {
        return(samplenames);
    }
    double getOffset()
    {
        return(offset);
    }
    public String getFirstDatasetName()
    {
        return first_dataset_name;
    }
    public String getSecondDatasetName()
    {
        return second_dataset_name;
    }
    
}