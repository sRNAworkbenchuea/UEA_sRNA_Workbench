/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput.FiRePatConfiguration;

/**
 *
 * @author a019349
 */
public class FiRePat {
    // FiRePatdata object and its parameters
    private File first_input_file;
    private File second_input_file;
    private FiRePatData inputdata; // to hold 'cleaned' raw data for processing
    private FiRePatLog fpl;
    private String first_input_file_path;  // path/name of first input file
    private String second_input_file_path;  // path/name of second input file
    private String first_dataset_name = "First"; // identifier for the first set of data in output files
    private String second_dataset_name = "Second"; // identifier for the second set of data in output files
    private String[] sample_names;  // to hold the names of the samples
    private String sample_name_root = "S-"; // prefix for automatically generated sample names, default is S-, can be changed
    
    private String log_file_name;
    private boolean record_log_file;
    
    private boolean first_has_header; // whether file has header
    private boolean second_has_header; // whether file has header
    private boolean first_is_srna; // whether file contains sRNA data
    private boolean second_is_srna; // whether file contains sRNA data
    private boolean filter_srna_data; // whether sRNA data to be filtered during loading, default true
    private boolean output_first_dataset_first; // whether results from first dataset are printed first in output tables, default true
    
    private double srna_ofc_PC_cutoff; // top % sRNA data to retain
    private double non_srnas_filter_level; // cutoff value for removing data with v low expression levels, default will do nothing, but anything else will 
    private boolean filter_non_srna_data; // apply cutoff to filter non-sRNA data
    private boolean auto_sample_names;//whether to automatically generate sample names
    
    private boolean output_corr_to_csv;
    private boolean output_local_corr_to_csv;
    private boolean output_results_to_csv;
    private boolean output_results_to_cytoscape;
    private boolean output_results_to_html;
    private boolean output_results_to_latex;
    private String output_file_dir;
    
    
    // Correlations object and its parameters
    private Correlations corrs; // to hold correlation values
    private boolean local; // whether local correlations or not
    private final String PEARSON = "PEARSON";   // correlation method
    private final String SPEARMAN = "SPEARMAN"; // ditto
    private final String KENDALL = "KENDALL";   // ditto
    private String correlation_method; //correlation method to be used
    private Integer num_samples; // number of samples
    private int num_replicates; // number of replicates
    private double correlation_threshold; // threshold abs cutoff value for correlations
    private int min_window_for_local_corr; // minimum window for local correlations
    private int max_window_for_local_corr; // maximum window for local correlations, probably will not be used, as the full width will be used,  but included for completeness
    private String correlationsOutputFileName; // file name (stem) for output file(s) of (local) correlation values
    
    // DataForClustering object and its parameters
    private DataForClustering dfclust; // to hold subset of highly correlated data for clustering 
    // Clustering object and its parameters
    private Clustering clust; // to hold clustered data
    private String csv_results_output_file_name;
    private String cytoscape_results_output_file_name;
    private String corr_csv_output_file_name;
    private String local_corr_csv_output_file_name_stem;
    private String html_results_output_file_name;
    private String latex_results_output_file_name;
    private String sortby; // whether to sort by first or second SDU patterns
    private final String FIRST = "FIRST"; // sortby method
    private final String SECOND = "SECOND"; // ditto
    private boolean sort_by_patterns_from_first_dataset;// which results to sort output by        
    private boolean include_patterns_in_output; // whether to unclude SDU patterns in output file
    private int down_colour;// colour for indicating reduced expression levels in html output
    private int up_colour;// colour for indicating increased expression levels in html output
    FiRePatConfiguration config; // to provide the values for the parameters 
    
    private boolean use_output_file_name_root;
    private String output_file_name_root;

    HTMLutil htm; // for exporting results in HTML format;
    
    PrintWriter writer;
    
    public FiRePat()
    {
        first_input_file = null;
        second_input_file = null;
        inputdata = new FiRePatData();
        fpl = new FiRePatLog();
        first_input_file_path = "";
        second_input_file_path = "";
        first_dataset_name = "First"; 
        second_dataset_name = "Second";
    
        corrs = new Correlations();
        dfclust = new DataForClustering();
        clust = new Clustering();
        htm =  new HTMLutil();
        correlation_method = PEARSON;// default, can be changed by user
        sortby = FIRST;// default, can be changed by user
        sort_by_patterns_from_first_dataset = true;// which results to sort by
        first_is_srna = true; // default
        second_is_srna = false; // default
        first_has_header = false;
        second_has_header = false;
        filter_srna_data = true; // default
        output_first_dataset_first = true; // default
        correlation_threshold = 0.9; // default, can be changed by user
        non_srnas_filter_level = 10; // cutoff value for removing data with v low expression levels 
        srna_ofc_PC_cutoff = 10; // top % sRNA data to retain
        filter_non_srna_data = true;
        csv_results_output_file_name = "FiRePat.csv";
        cytoscape_results_output_file_name = "FiRePatCyto.csv";
        corr_csv_output_file_name = "FiRePatCorr.csv";
        local_corr_csv_output_file_name_stem = "FiRePatLocalCorr";
        html_results_output_file_name = "FiRePat.html";
        latex_results_output_file_name = "FiRePat.tex";
        down_colour = 0;
        up_colour = 1;        
        output_results_to_csv = true;
        output_results_to_cytoscape = true;
        output_corr_to_csv = false;
        output_local_corr_to_csv = false;
        output_results_to_html = true;
        output_results_to_latex = true;
        output_file_dir = "";
        log_file_name = "FiRePat.log";
        record_log_file = true;
        use_output_file_name_root = false;
        output_file_name_root = "FiRePat";
        
    }
    // method to set all parameters
    public void setAllParameters()
    {
        config = FiRePatConfiguration.getInstance();
        // parameters relating to inputs
        setFirstDatasetName(config.getFirstDatasetName());
        setSecondDatasetName(config.getSecondDatasetName());
        setFirstInputFilePath(config.getFirstInputFilePath());
        setSecondInputFilePath(config.getSecondInputFilePath());
        setFirstHasHeader(config.getFirstHasHeader());
        setSecondHasHeader(config.getSecondHasHeader());
        setFirstIsSrna(config.getFirstIsSrna());       
        setSecondIsSrna(config.getSecondIsSrna());
        setNumSamples(config.getNumSamples());
        setNumReplicates(config.getNumReplicates());
        setAutoSampleNames(config.getAutoSampleNames());
        setSampleNameRoot(config.getSampleNameRoot());
        setSampleNames(config.getSampleNames());
        // parameters relating to processing
        setFilterSrnaData(config.getFilterSrnaData());
        setPCcutoff(config.getPCcutoff());
        setFilterNonSrnaData(config.getFilterNonSrnaData());
        setNonSrnasFilterLevel(config.getNonSrnasFilterLevel());
        setCorrMethod(config.getCorrMethod());
        setCorrThr(config.getCorrThr());
        setLocalCorrs(config.getLocalCorrs());
        setMinWindow(config.getMinWindow());
        // parameters relating to output
        setOutputFileDirName(config.getOutputFileDirName());
        setOutputCorrToCsv(config.getOutputCorrToCsv());
        setOutputLocalCorrToCsv(config.getOutputLocalCorrToCsv());
        setCorrsOutputFileName(config.getCorrCsvOutputFileName());
        setLocalCorrCsvOutputFileStem(config.getLocalCorrCsvOutputFileStem());
        
        setOutputToCsv(config.getOutputToCsv());
        setOutputToCytoscape(config.getOutputToCytoscape());
        setOutputToHtml(config.getOutputToHtml());
        setOutputToLatex(config.getOutputToLatex());
        setCsvOutputFile(config.getCsvOutputFileName());
        setCytoscapeOutputFile(config.getCytoscapeOutputFileName());
        setHtmlOutputFile(config.getHtmlOutputFileName());
        setLatexOutputFile(config.getLatexOutputFileName());

        setIncludeTransformedPattern(config.getIncludePatterns());
        setFirstDatasetFirst(config.getFirstDatasetFirst());
        setSortOnFirstDataPatterns(config.getSortOnFirstDataPatterns());
        setUpColour(config.getUpColour());
        setDownColour(config.getDownColour());
        
        setRecordLogFile(config.getRecordLogFile());
        setLogFile(config.getLogFileName());
        
        setUseOutputFileNameRoot(config.getUseOutputFileNameRoot());
        setOutputFileNameRoot(config.getOutputFileNameRoot());
    }
    // log file
    public void setLogFile(String s)
    {
        log_file_name = s;
    }
    public void setRecordLogFile(boolean b)
    {
        record_log_file = b;
    }
    // names of datasets
    public void setFirstDatasetName(String s)
    {
        first_dataset_name = s;
        FiRePatConfiguration.getInstance().setFirstDatasetName(first_dataset_name);
    }
    public void setSecondDatasetName(String s)
    {
        second_dataset_name = s;
        FiRePatConfiguration.getInstance().setSecondDatasetName(second_dataset_name);
    }
    // methods relating to FiRePatData object
    public void setFirstInputFile(File f)
    {
            first_input_file = f;
    }
    public void setSecondInputFile(File f)
    {
        second_input_file = f;
    }       
    public void setFirstInputFilePath(String name)
    {
        first_input_file_path = name;
    }
    public void setSecondInputFilePath(String name)
    {
        second_input_file_path = name;
    }
    public void setFirstHasHeader(boolean hasheader)
    {
        first_has_header = hasheader;
    }
    public void setFirstIsSrna(boolean b)
    {
        first_is_srna = b;
    }
    public void setSecondIsSrna(boolean b)
    {
        second_is_srna = b;
    }
    public void setSecondHasHeader(boolean hasheader)
    {
        second_has_header = hasheader;
    }
    public void setNumSamples(int ns)
    {
        num_samples = ns;
        inputdata.setNumSamples(ns);
    }
    public void setNumReplicates(int nrep)
    {
        num_replicates = nrep;
        inputdata.setNumReplicates(nrep);
    }  
    // for entering the names of the samples, can be used to change them after processing, 
    // and before (re)printing output, e.g. to make them shorter in order to get neater output tables
    // tries to update values in clust first, if wrong number of sample names does not update,
    // see Clustering.setSampleNames()
    public void updateSampleNames(String[] smpnames)
    {     // not sure about this method, need to recheck   
        if(clust.updateSampleNames(smpnames)) sample_names = smpnames;
    }
    // other methods for setting sample names, 
    void setSampleNames(String snames)
    {
        sample_names = snames.split(",");
    }
    void setSampleNames(String[] snames)
    {
        sample_names = snames;
    }
    void setSampleNames()
    {   // sets sample names in 
        if(sample_names==null) generateSampleNames();
        inputdata.setSampleNames(sample_names);
    }
    void setSampleNameRoot(String prefix)
    {
        sample_name_root = prefix;
    }
    public void setAutoSampleNames(boolean b)
    {
        auto_sample_names = b;
    }
        
    void generateSampleNames()
    {   // automatically generates sample names numbered from 1, with prefix 
        String[] snames = new String[num_samples];
        for(int i = 0; i<num_samples; i++) snames[i] = sample_name_root + Integer.toString(i+1);
        sample_names = snames;
    }
    public void setFilterNonSrnaData(boolean b)
    {
            filter_non_srna_data = b;
    }
    public void setFilterSrnaData(boolean b)
    {
        filter_srna_data = b;
    }
    public void setFilterSrnaDataOn()
    {
        filter_srna_data = true;
    }
    public void setFilterSrnaDataOff()
    {
        filter_srna_data = false;
    }
    // methods relating to Correlation object
    public void setMinWindow(int minwin)
    {
        min_window_for_local_corr = minwin;
    }
    public void setMaxWindow(int maxwin)
    {
        max_window_for_local_corr = maxwin;
    }
    public void setPearson()
    {
        correlation_method = PEARSON;
    }
    public void setSpearman()
    {
        correlation_method = SPEARMAN;
    }
    public void setKendall()
    {
        correlation_method = KENDALL;
    }
    public void setCorrMethod(String corrmethod)
    {
        correlation_method = corrmethod;
    }
    public void setLocalCorrOn()
    {
        local = true;
    }
    public void setLocalCorrOff()
    {
        local = false;
    }
    public void setLocalCorrs(boolean islocal)
    {
        local = islocal;
    }
    public void setCorrThr(double corThr)
    {
        correlation_threshold = corThr;
    }   
    
    
    // methods relating to final output
    public void setOutputToCsv(boolean b)
    {
        output_results_to_csv = b;
    }
    public void setOutputToCytoscape(boolean b)
    {
        output_results_to_cytoscape = b;
    }
    public void setOutputCorrToCsv(boolean b)
    {
        output_corr_to_csv = b;
    }
    public void setOutputLocalCorrToCsv(boolean b)
    {
        output_local_corr_to_csv = b;
    }
    public void setOutputToHtml(boolean b)
    {
        output_results_to_html = b;
    }
    public void setOutputToLatex(boolean b)
    {
        output_results_to_latex = b;
    }
    public void setCsvOutputFileName(String s)
    {
        csv_results_output_file_name = s;
    }
    public void setCytoscapeOutputFileName(String s)
    {
        cytoscape_results_output_file_name = s;
    }
    public void setCorrCsvOutputFileName(String s)
    {
        corr_csv_output_file_name = s;
    }
    public void setLocalCorrCsvOutputFileStem(String s)
    {
        local_corr_csv_output_file_name_stem = s;
    }
    public void setHtmlOutputFileName(String s)
    {
        html_results_output_file_name = s;
    }
    public void setLatexOutputFileName(String s)
    {
        latex_results_output_file_name = s;
    }
    public void setOutputFileDirName(String s)
    {
        output_file_dir = s;
    }
    public String getOutputFileDirName()
    {
        return output_file_dir;
    }       
        
    public void calculateCorrelations()
    {   // need checks that inputs are valid
        if(local) corrs = new Correlations(inputdata, correlation_method, correlation_threshold, min_window_for_local_corr, max_window_for_local_corr, fpl);
        else corrs = new Correlations(inputdata, correlation_method, correlation_threshold, fpl);
        corrs.setFirstDatasetFirst(output_first_dataset_first);
    }
    public void setCorrsOutputFileName(String name)
    {
        correlationsOutputFileName = name;
    }
    void writeCorrelationsToCSV(boolean includeData)
    {
        String f = output_file_dir+File.separator+correlationsOutputFileName;
        if(local) corrs.writelocalCorrelationsToFiles(correlationsOutputFileName, output_file_dir);
        else corrs.writeCorrelationsToFile(f, includeData);
    }
    void writeCorrelationsToCSV(String filename, boolean includeData)
    {
        String f = output_file_dir+File.separator+filename;
        if(local) corrs.writelocalCorrelationsToFiles(f, output_file_dir);
        else corrs.writeCorrelationsToFile(f, includeData);
    }
    
    // methods relating to DataForClustering object
    public void getDataForClustering()
    {   // will need check that input values are valid
        dfclust = new DataForClustering(corrs, inputdata, fpl);
    }
    
    // methods relating to Clustering object and output of patterns
    public void sortBySecond()
    {
        sortby = SECOND;
    }
    public void sortByFirst()
    {
        sortby = FIRST;
    }
    public void setSortby(String sortmethod)
    {
        sortby = sortmethod;
    }
    public void processDataForClustering()
    {   // may need check that input values are valid
        clust = new Clustering(dfclust, htm, fpl);
        clust.setFirstDatasetFirst(output_first_dataset_first);
    }
    public void setCsvOutputFile(String n) 
    {
        csv_results_output_file_name = n;
    }
    public void setCytoscapeOutputFile(String n) 
    {
        cytoscape_results_output_file_name = n;
    }
    public void setHtmlOutputFile(String n) 
    {
        html_results_output_file_name = n;
    }
    public void setLatexOutputFile(String n) 
    {
        latex_results_output_file_name = n;
    }

    void writeSortedPatternsToCsv()
    {
        String f = "";
        if(output_file_dir!="") f = output_file_dir+File.separator+csv_results_output_file_name;
        else f = csv_results_output_file_name;
        clust.writeSortedPatternsToCsv(f, sortby, include_patterns_in_output);
    }
    void writeSortedPatternsToCytoscape()
    {
        String f = "";
        if(output_file_dir!="") f = output_file_dir+File.separator+cytoscape_results_output_file_name;
        else f = cytoscape_results_output_file_name;
        clust.writeSortedPatternsToCytoscape(f, sortby);
    }
    void writeSortedPatternsToHtml()
    {
        String f = "";
        if(output_file_dir!="") f = output_file_dir+File.separator+html_results_output_file_name;
        else f = html_results_output_file_name;
        clust.writeSortedPatternsToHtml(f, sortby, include_patterns_in_output);
    }   
    void writeSortedPatternsToLatex()
    {
        String f = "";
        if(output_file_dir!="") f = output_file_dir+File.separator+latex_results_output_file_name;
        else f = latex_results_output_file_name;
        clust.writeSortedPatternsToLatex(f, sortby, include_patterns_in_output);
    }
    
    public void setIncludeTransformedPattern(boolean include)
    {
        include_patterns_in_output = include;
    }
    public void setSortOnFirstDataPatterns(boolean b)
    {
        sort_by_patterns_from_first_dataset = b;
        if(sort_by_patterns_from_first_dataset) sortby = FIRST;
        else sortby = SECOND;
    }
    public void writeOutputFiles()
    {
        if(output_corr_to_csv||output_local_corr_to_csv) writeCorrelationsToCSV(false);       
        if(output_results_to_csv) writeSortedPatternsToCsv();
        if(output_results_to_cytoscape) writeSortedPatternsToCytoscape();
        if(output_results_to_html) writeSortedPatternsToHtml();
        if(output_results_to_latex) writeSortedPatternsToLatex();  
    }    
        
    public void setFirstDatasetFirst(boolean b)
    {
        output_first_dataset_first = b;
        corrs.setFirstDatasetFirst(b);
        clust.setFirstDatasetFirst(b);
    }
    public void outputSecondDatasetFirst()
    {
        output_first_dataset_first = true;
        corrs.setFirstDatasetFirst(true);
        clust.setFirstDatasetFirst(true);
    }
    public void outputFirstDatasetFirst()
    {
        output_first_dataset_first = false;
        corrs.setFirstDatasetFirst(false);
        clust.setFirstDatasetFirst(false);
    }
    // commands for setting colours of html output table
    public void setUpColour(int i)
    {
        htm.setUpColour(i);
    }
    public void setDownColour(int i)
    {
        htm.setDownColour(i);
    }
    public void setBorderColour(int i)
    {
        htm.setBorderColour(i);
    }  
    // method to set percentage stringency for EV checks(default is 5%) 
    void setStringency(double str)
    {
        inputdata.setStringency(str);
    }
    // methods for reading data from files and loading into FiRePat data object prior to (pre)processing/cleansing
    void loadFirstInputFile()
    {
        inputdata.loadFirstInputFile(first_input_file, first_has_header);
    }
    void loadSecondInputFile()
    {
        inputdata.loadSecondInputFile(second_input_file_path, first_has_header);
    }
    void readFirstDataFile()
    {
        if(first_input_file==null)
        {
            try
            {
                first_input_file = new File(first_input_file_path);
            }
            catch(Exception e)
            {
                fpl.println("FiRePat: problem with file '"+first_input_file_path+"'");
                System.err.println(e);
            } 
        }
        inputdata.setFirstDatasetName(first_dataset_name);
        inputdata.loadFirstInputFile(first_input_file, first_has_header);
    }
    void readSecondDataFile()
    {
        if(second_input_file==null)
        {
            try
            {
                second_input_file = new File(second_input_file_path);
            }
            catch(Exception e)
            {
                fpl.println("FiRePat: problem with file '"+second_input_file_path+"'");
                System.err.println(e);
            } 
        }
        inputdata.setSecondDatasetName(second_dataset_name);
        inputdata.loadSecondInputFile(second_input_file, second_has_header);
    }
    // methods for loading data into FiRePatData object prior to (pre)processing/cleansing
    void loadFirstData(double[][] datain, ArrayList<String> names)
    {
        inputdata.setFirstDatasetName(first_dataset_name);
        inputdata.loadFirstData(datain, names);
    }
    void loadSecondData(double[][] datain, ArrayList<String> names)
    {
        inputdata.setSecondDatasetName(second_dataset_name);
        inputdata.loadFirstData(datain, names);
    }
    // methods for (pre)processing/cleansing data in FiRePatData object
    void processSecondInputData(boolean shortrna, boolean hasheader, boolean filtersrnas)
    {
        inputdata.processSecondInputData(inputdata.getSecondDataIn(), inputdata.getSecondNamesIn(), shortrna, hasheader, filtersrnas);
    }
    void processFirstInputData(boolean shortrna, boolean hasheader, boolean filtersrnas)
    {
        inputdata.processFirstInputData(inputdata.getFirstDataIn(), inputdata.getFirstNamesIn(), shortrna, hasheader, filtersrnas);
    }
    public void setPCcutoff(double d)
    {
            srna_ofc_PC_cutoff = d;
    }        
    void setNonSrnasFilterLevel(double level)
    {
        non_srnas_filter_level = level;
        inputdata.setNonSrnasFilterLevel(level);
    }
    void setNonSrnasFilter(boolean b)
    {
        filter_non_srna_data = b;
        inputdata.setNonSrnasFilter(b);
    }
    
    // wrapper method to load and preprocess data from files
    public void loadData()
    {   
        // initialize log file
        initializeLogFile();
        // make sample names if req, then load the data
        if(auto_sample_names) generateSampleNames();
        inputdata.setSampleNames(sample_names);
        readFirstDataFile();
        readSecondDataFile();
    }    
    public void initializeLogFile()
    {
        String f = log_file_name;
        if(!output_file_dir.equals("")) f = output_file_dir+File.separator+log_file_name;
        fpl.initialize(f, record_log_file);
        inputdata.setLogFile(fpl);
        if(record_log_file)
        {
            fpl.println("PARAMETERS", true, false);
            fpl.println("first_input_file_path=" + first_input_file_path, true, false);
            fpl.println("first_has_header=" + first_has_header, true, false);
            fpl.println("first_is_srna=" + first_is_srna, true, false);
            fpl.println("second_input_file_path=" + second_input_file_path, true, false);
            fpl.println("second_has_header=" + second_has_header, true, false);
            fpl.println("second_is_srna=" + second_is_srna, true, false);
            fpl.println("first_dataset_name=" + first_dataset_name, true, false);
            fpl.println("second_dataset_name=" + second_dataset_name, true, false);
            fpl.println("num_samples=" + num_samples, true, false);
            fpl.println("num_replicates=" + num_replicates, true, false);
            fpl.println("auto_sample_names=" + auto_sample_names, true, false);
            fpl.println("sample_name_root=" + sample_name_root, true, false);
            fpl.println("sample_names=" + sampleNamestoString(sample_names), true, false);
            fpl.println("filter_srna_data=" + filter_srna_data, true, false);
            fpl.println("srna_ofc_PC_cutoff=" + srna_ofc_PC_cutoff, true, false);
            fpl.println("filter_non_srna_data=" + filter_non_srna_data, true, false);
            fpl.println("non_srnas_filter_level=" + non_srnas_filter_level, true, false);
            fpl.println("correlation_method=" + correlation_method, true, false);
            fpl.println("correlation_threshold=" + correlation_threshold, true, false);
            fpl.println("use_local_correlations=" + local, true, false);
            fpl.println("min_window_for_local_corr=" + min_window_for_local_corr, true, false);
            fpl.println("output_file_dir=" + output_file_dir, true, false);
            fpl.println("use_output_file_name_root="+use_output_file_name_root, true, false);
            fpl.println("output_file_name_root="+output_file_name_root, true, false);
            fpl.println("record_log_file=" + record_log_file, true, false);
            fpl.println("log_file_name=" + log_file_name, true, false);
            fpl.println("output_corr_to_csv=" + output_corr_to_csv, true, false);
            fpl.println("corr_csv_output_file_name=" + corr_csv_output_file_name, true, false);
            fpl.println("output_local_corr_to_csv=" + output_local_corr_to_csv, true, false);
            fpl.println("local_corr_csv_output_file_name_stem=" + local_corr_csv_output_file_name_stem, true, false);
            fpl.println("output_results_to_csv=" + output_results_to_csv, true, false);
            fpl.println("csv_results_output_file_name=" + csv_results_output_file_name, true, false);
            fpl.println("output_results_to_cytoscape=" + output_results_to_cytoscape, true, false);
            fpl.println("cytoscape_results_output_file_name=" + cytoscape_results_output_file_name, true, false);
            fpl.println("output_results_to_html=" + output_results_to_html, true, false);
            fpl.println("html_results_output_file_name=" + html_results_output_file_name, true, false);
            fpl.println("output_results_to_latex=" + output_results_to_latex, true, false);
            fpl.println("latex_results_output_file_name=" + latex_results_output_file_name, true, false);
            fpl.println("down_colour=" + down_colour, true, false);
            fpl.println("up_colour=" + up_colour, true, false);
            fpl.println("include_patterns_in_output=" + include_patterns_in_output, true, false);
            fpl.println("output_first_dataset_first=" + output_first_dataset_first, true, false);
            fpl.println("sort_by_patterns_from_first_dataset=" + sort_by_patterns_from_first_dataset, true, false);
            fpl.println("MESSAGES", true, false);
        }
    }
    public void closeLogFile()
    {
        if(record_log_file) fpl.close();
    }    
    public void preprocessData()
    {
        processFirstInputData(first_is_srna, first_has_header, filter_srna_data);
        processSecondInputData(second_is_srna, second_has_header, filter_srna_data);
    }
    private void setUseOutputFileNameRoot(boolean b)
    {
        use_output_file_name_root = b;
    }
    private void setOutputFileNameRoot(String s)
    {
        output_file_name_root = s;
    }
    // next function to be moved to FiRePatConfiguration.java
    private void setAllFileNames()// only to be used when running from command line
    {
        if(use_output_file_name_root)
        {
            System.out.println("FiRePat: using output file name root '"+output_file_name_root+"' for all output file names");
            try
            {
                csv_results_output_file_name = output_file_name_root+".csv";
                cytoscape_results_output_file_name = output_file_name_root+"Cyto.csv";
                corr_csv_output_file_name = output_file_name_root+"Corr.csv";
                local_corr_csv_output_file_name_stem = output_file_name_root+"LocalCorr";
                html_results_output_file_name = output_file_name_root+".html";
                latex_results_output_file_name = output_file_name_root+".tex";
                log_file_name = output_file_name_root+".log";
            }
            catch(Exception e)// very unlikely to happen
            {
                System.out.println("FiRePat: problem trying to use output file name root '"+output_file_name_root+"', resetting to default names for all output files");
                csv_results_output_file_name = "FiRePat.csv";
                cytoscape_results_output_file_name = "FiRePatCyto.csv";
                corr_csv_output_file_name = "FiRePatCorr.csv";
                local_corr_csv_output_file_name_stem = "FiRePatLocalCorr";
                html_results_output_file_name = "FiRePat.html";
                latex_results_output_file_name = "FiRePat.tex";
                log_file_name = "FiRePat.log";
            }
        }
    }
    private String sampleNamestoString(String[] s)
    {
        if(s==null) return "";
        else return String.join(",", s);
    }
}
