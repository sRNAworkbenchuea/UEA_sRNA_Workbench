/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author a019349
 */
public final class FiRePatConfiguration 
{    
        private File first_input_file;
        private File second_input_file;
        private String output_file_dir;
        private String first_input_file_path;
        private String second_input_file_path;        
        private String corr_csv_output_file_name;
        private String csv_results_output_file_name;
        private String cytoscape_results_output_file_name;
        private String local_corr_csv_output_file_name_stem;
        private String html_results_output_file_name;
        private String latex_results_output_file_name;
        private String correlation_method;
        private String sample_name_root;
        private String sample_names;
        private String first_dataset_name;
        private String second_dataset_name;
        private String log_file_name;
        
        private boolean output_corr_to_csv;
        private boolean output_local_corr_to_csv;
        private boolean output_results_to_csv;
        private boolean output_results_to_cytoscape;
        private boolean record_log_file;
        
        private boolean filter_non_srna_data;
        private boolean filter_srna_data;
        private boolean first_has_header;
        private boolean first_is_srna;
        private boolean output_results_to_html;
        private boolean include_patterns_in_output;
        private boolean use_local_correlations;
        private boolean output_first_dataset_first;
        private boolean output_results_to_latex;
        private boolean second_has_header;
        private boolean second_is_srna;
        private boolean sort_by_patterns_from_first_dataset;
        private boolean auto_sample_names;
        private boolean use_output_file_name_root;
        private String output_file_name_root;
               
        private int down_colour;
        private int min_window_for_local_corr;
        private int num_replicates;
        private Integer num_samples;
        private int up_colour;
        
        private double correlation_threshold;
        private double non_srnas_filter_level;
        private double srna_ofc_PC_cutoff;
        
        private static FiRePatConfiguration config;

        public static FiRePatConfiguration getInstance() 
        {
            if (config == null) 
            {
                config = new FiRePatConfiguration();
            }
            return config;
        }

        private FiRePatConfiguration()
        {
            first_input_file = null;
            second_input_file = null;
            output_file_dir = null;
            first_input_file_path = null;
            second_input_file_path = null;
            correlation_method = "PEARSON";
            csv_results_output_file_name = "FiRePat.csv";
            cytoscape_results_output_file_name = "FiRePatCorr.csv";
            corr_csv_output_file_name = "FiRePatCorr.csv";
            local_corr_csv_output_file_name_stem = "FiRePatLocalCorr";
            html_results_output_file_name = "FiRePat.html";
            latex_results_output_file_name = "FiRePat.tex";
            log_file_name = "FiRePat.log";
            use_output_file_name_root = false;
            output_file_name_root = "FiRePat";
            sample_name_root = "S-";
            sample_names = "";
            first_dataset_name = "First";
            second_dataset_name = "Second";
        
            down_colour = 0;
            min_window_for_local_corr = 4;
            num_replicates = 1;
            num_samples = null;
            up_colour = 1;
        
            correlation_threshold = 0.9;
            non_srnas_filter_level = 10;
            srna_ofc_PC_cutoff = 10;
            
            filter_non_srna_data = true;
            filter_srna_data = true;
            first_has_header = false;
            first_is_srna = true;
            second_has_header = false;
            second_is_srna = false;
            auto_sample_names = true;
            record_log_file = true;
            
            output_results_to_csv = true;
            output_results_to_cytoscape = true;
            output_corr_to_csv = false;
            output_local_corr_to_csv = false;
            use_local_correlations = false;
            
            output_results_to_html = true;
            output_results_to_latex = true;           
            
            include_patterns_in_output = true;
            output_first_dataset_first = true;
            sort_by_patterns_from_first_dataset = true;
        }
        
        public void loadFromFile(File configFile) throws IOException 
        {
            config = getInstance();           
            Scanner in = new Scanner(configFile);
            String fname = configFile.getPath();
            System.out.println("FiRePat: loading parameters from file '"+fname+"'");
            in.useDelimiter("[/n]");
            int line_num = 0;// to count lines read 
            try 
            {
                boolean[] required = new boolean[]{false,false,false,false};
                String[] reqParams = new String[]{"first_input_file_path", "second_input_file_path", "num_samples", "output_file_dir"};
                while (in.hasNextLine()) 
                {
                    line_num++;
                    String line = in.nextLine();
                    if(line!="")
                    {
                        String[] linein = line.split("=");
                        if(linein.length==2)
                        {
                            String parameter = linein[0].trim();
                            String value = linein[1].trim();
                            if(value.equals("null")) 
                            {
                                System.out.println("FiRePat: Error! value of parameter '"+parameter+"' is 'null' !");
                                throw new IOException();
                            }
                            switch(parameter)
                            {
                                // check required parameters first
                                case "first_input_file_path":
                                    required[0] = true;
                                    config.setFirstInputFilePath(value);
                                    break;
                                case "second_input_file_path":
                                    required[1] = true;
                                    config.setSecondInputFilePath(value);
                                    break;
                                case "num_samples":
                                    required[2] = true;
                                    config.setNumSamples(Integer.parseInt(value));
                                    break;
                                case "output_file_dir":
                                    required[3] = true;
                                    config.setOutputFileDirName(value);
                                    break;
                                // now check other parameters (i.e. the default values will be overridden)
                                // input parameters
                                case "first_dataset_name":
                                    config.setFirstDatasetName(value);
                                    break;
                                case "second_dataset_name":
                                    config.setSecondDatasetName(value);
                                    break;                           
                                case "first_has_header":
                                    config.setFirstHasHeader(Boolean.parseBoolean(value));
                                    break;
                                case "first_is_srna":
                                    config.setFirstIsSrna(Boolean.parseBoolean(value));
                                    break;
                                case "second_has_header":
                                    config.setSecondHasHeader(Boolean.parseBoolean(value));
                                    break;
                                case "second_is_srna":
                                    config.setSecondIsSrna(Boolean.parseBoolean(value));
                                    break;
                                case "num_replicates":
                                    config.setNumReplicates(Integer.parseInt(value));
                                    break;
                                case "auto_sample_names":
                                    config.setAutoSampleNames(Boolean.parseBoolean(value));
                                    break;
                                case "sample_name_root":
                                    config.setSampleNameRoot(value);
                                    break;
                                case "sample_names":
                                    config.setSampleNames(value);
                                    break;
                                // processing parameters
                                case "filter_srna_data":
                                    config.setFilterSrnaData(Boolean.parseBoolean(value));
                                    break;    
                                case "srna_ofc_PC_cutoff":
                                    config.setPCcutoff(Double.parseDouble(value));
                                    break;    
                                case "filter_non_srna_data":
                                    config.setFilterNonSrnaData(Boolean.parseBoolean(value));
                                    break;    
                                case "non_srnas_filter_level":
                                    config.setNonSrnasFilterLevel(Double.parseDouble(value));
                                    break;    
                                case "correlation_method":
                                    config.setCorrMethod(value);
                                    break;
                                case "correlation_threshold":
                                    config.setCorrThr(Double.parseDouble(value));
                                    break;    
                                case "use_local_correlations":
                                    config.setLocalCorrs(Boolean.parseBoolean(value));
                                    break;    
                                case "min_window_for_local_corr":
                                    config.setMinWindow(Integer.parseInt(value));
                                    break;
                                    // output parameters
                                case "record_log_file":
                                    config.setRecordLogFile(Boolean.parseBoolean(value));
                                    break;    
                                case "log_file_name":
                                    config.setLogFileName(value);
                                    break;
                                case "output_corr_to_csv":
                                    config.setOutputCorrToCsv(Boolean.parseBoolean(value));
                                    break;    
                                case "corr_csv_output_file_name":
                                    config.setCorrCsvOutputFileName(value);
                                    break;
                                case "output_local_corr_to_csv":
                                    config.setOutputLocalCorrToCsv(Boolean.parseBoolean(value));
                                    break;    
                                case "local_corr_csv_output_file_name_stem":
                                    config.setLocalCorrCsvOutputFileStem(value);
                                    break;                               
                                case "output_results_to_csv":
                                    config.setOutputToCsv(Boolean.parseBoolean(value));
                                    break;    
                                case "output_results_to_cytoscape":
                                    config.setOutputToCytoscape(Boolean.parseBoolean(value));
                                    break;    
                                case "csv_results_output_file_name":
                                    config.setCsvOutputFileName(value);
                                    break;                                
                                case "cytoscape_results_output_file_name":
                                    config.setCytoscapeOutputFileName(value);
                                    break;                                
                                case "output_results_to_html":
                                    config.setOutputToHtml(Boolean.parseBoolean(value));
                                    break;    
                                case "html_results_output_file_name":
                                    config.setHtmlOutputFileName(value);
                                    break;                               
                                case "output_results_to_latex":
                                    config.setOutputToLatex(Boolean.parseBoolean(value));
                                    break;    
                                case "latex_results_output_file_name":
                                    config.setLatexOutputFileName(value);
                                    break;                            
                                case "include_patterns_in_output":
                                    config.setIncludePatterns(Boolean.parseBoolean(value));
                                    break;    
                                case "output_first_data_first":
                                    config.setFirstDatasetFirst(Boolean.parseBoolean(value));
                                    break;    
                                case "sort_by_patterns_from_first_dataset":
                                    config.setSortOnFirstDataPatterns(Boolean.parseBoolean(value));
                                    break;
                                case "down_colour":
                                    config.setDownColour(Integer.parseInt(value));
                                    break;    
                                case "up_colour":
                                    config.setUpColour(Integer.parseInt(value));
                                    break;  
                                case "use_output_file_name_root":
                                    setUseOutputFileNameRoot(Boolean.parseBoolean(value));
                                    break;
                                case "output_file_name_root":
                                    setOutputFileNameRoot(value);
                                    break;
                                default:// give notification of unknown parameter 
                                    System.out.println("FiRePat: unknown parameter; '"+parameter+"'");
                            }                                                        
                        }
                    }
                    // add code to use/check file root here
                }
                // give notification of missing required parameters
                for(int i=0; i<required.length; i++)
                {
                    if(required[i]==false)
                    {
                        System.out.println("FiRePat: required parameter '"+reqParams[i]+"' missing from configuration file '"+fname+"' !");
                        System.exit(1);
                    }
                }
                if(use_output_file_name_root) setAllFileNames();
            } 
            //catch (IOException | NumberFormatException ex) 
            catch (Exception ex) 
            {
                System.out.println(ex);
                throw new IOException("FiRePat: there was an error reading the configuration file at line "+line_num+". Please generate a new one or use the default file provided.");
            }
        }
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
        
        public void setFirstInputFile(File f)
        {
            first_input_file = f;
        }
        public File  getFirstInputFile()
        {
            return first_input_file;
        }
        public void setSecondInputFile(File f)
        {
            second_input_file = f;
        }
        public File  getSecondInputFile()
        {
            return second_input_file;
        }
        public void setRecordLogFile(boolean b)
        {
            record_log_file = b;
        }
        public boolean getRecordLogFile()
        {
            return record_log_file;
        }
        public void setAutoSampleNames(boolean b)
        {
            auto_sample_names = b;
        }
        public boolean getAutoSampleNames()
        {
            return auto_sample_names;
        }
        
        public void setSampleNameRoot(String root)
        {
            sample_name_root = root;
        }
        public String getSampleNameRoot()
        {
            return sample_name_root;
        }
        
        public void setSampleNames(String s)
        {
            sample_names = s;
        }
        public String getSampleNames()
        {
            return sample_names;
        }
        
        
        
        
        public void setIncludePatterns(boolean b)
        {
            include_patterns_in_output = b;
        }
        public boolean getIncludePatterns()
        {
            return include_patterns_in_output;
        }
        public void setFirstDatasetFirst(boolean b)
        {
            output_first_dataset_first = b;
        }
        public boolean getFirstDatasetFirst()
        {
            return output_first_dataset_first;
        }
        public void setSortOnFirstDataPatterns(boolean b)
        {
            sort_by_patterns_from_first_dataset = b;
        }
        public boolean getSortOnFirstDataPatterns()
        {
            return sort_by_patterns_from_first_dataset;
        }
        
        public void setOutputToHtml(boolean b)
        {
            output_results_to_html = b;
        }
        public boolean getOutputToHtml()
        {
            return output_results_to_html;
        }
        public void setOutputToLatex(boolean b)
        {
            output_results_to_latex = b;
        }
        public boolean getOutputToLatex()
        {
            return output_results_to_latex;
        }       
        
        
        
        public void setLocalCorrs(boolean b)
        {
            use_local_correlations = b;
        }
        public boolean getLocalCorrs()
        {
            return use_local_correlations;
        }       
        
        
        public void setOutputToCsv(boolean b)
        {
            output_results_to_csv = b;
        }
        public boolean getOutputToCsv()
        {
            return output_results_to_csv;
        }       
        public void setOutputToCytoscape(boolean b)
        {
            output_results_to_cytoscape = b;
        }
        public boolean getOutputToCytoscape()
        {
            return output_results_to_cytoscape;
        }       
        public void setOutputCorrToCsv(boolean b)
        {
            output_corr_to_csv = b;
        }
        public boolean getOutputCorrToCsv()
        {
            return output_corr_to_csv;
        }       
        public void setOutputLocalCorrToCsv(boolean b)
        {
            output_local_corr_to_csv = b;
        }
        public boolean getOutputLocalCorrToCsv()
        {
            return output_local_corr_to_csv;
        }       
        
        public void setFirstIsSrna(boolean b)
        {
            first_is_srna = b;
        }
        public boolean getFirstIsSrna()
        {
            return first_is_srna;
        }       
        public void setFirstHasHeader(boolean b)
        {
            first_has_header = b;
        }
        public boolean getFirstHasHeader()
        {
            return first_has_header;
        }       
        public void setSecondIsSrna(boolean b)
        {
            second_is_srna = b;
        }
        public boolean getSecondIsSrna()
        {
            return second_is_srna;
        }       
        public void setSecondHasHeader(boolean b)
        {
            second_has_header = b;
        }
        public boolean getSecondHasHeader()
        {
            return second_has_header;
        }       
        
        
        public void setFilterSrnaData(boolean b)
        {
            filter_srna_data = b;
        }
        public boolean getFilterSrnaData()
        {
            return filter_srna_data;
        }       
        public void setFilterNonSrnaData(boolean b)
        {
            filter_non_srna_data = b;
        }
        public boolean getFilterNonSrnaData()
        {
            return filter_non_srna_data;
        }       
        
        public void setCorrThr(double d)
        {
            correlation_threshold = d;
        }
        public double getCorrThr()
        {
            return correlation_threshold;
        }       
        public void setNonSrnasFilterLevel(double d)
        {
            non_srnas_filter_level = d;
        }
        public double getNonSrnasFilterLevel()
        {
            return non_srnas_filter_level;
        }       
        public void setPCcutoff(double d)
        {
            srna_ofc_PC_cutoff = d;
        }
        public double getPCcutoff()
        {
            return srna_ofc_PC_cutoff;
        }       
        
        
        public void setDownColour(int i)
        {
            down_colour = i;
        }
        public int getDownColour()
        {
            return down_colour;
        }       
        public void setUpColour(int i)
        {
            up_colour = i;
        }
        public int getUpColour()
        {
            return up_colour;
        }      
        public void setMinWindow(int i)
        {
            min_window_for_local_corr = i;
        }
        public int getMinWindow()
        {
            return min_window_for_local_corr;
        }       
        public void setNumReplicates(int i)
        {
            num_replicates = i;
        }
        public int getNumReplicates()
        {
            return num_replicates;
        }       
        public void setNumSamples(Integer i)
        {
            num_samples = i;
        }
        public Integer getNumSamples()
        {
            return num_samples;
        }       
        
        
        
        public void setOutputFileDirName(String s)
        {
            output_file_dir = s;
        }
        public String getOutputFileDirName()
        {
            return output_file_dir;
        }       
        public void setFirstInputFilePath(String s)
        {
            first_input_file_path = s;
        }
        public String getFirstInputFilePath()
        {
            return first_input_file_path;
        }
        public void setSecondInputFilePath(String s)
        {
            second_input_file_path = s;
        }
        public String getSecondInputFilePath()
        {
            return second_input_file_path;
        }
        public void setCorrMethod(String s)
        {
            correlation_method = s;
        }
        public String getCorrMethod()
        {
            return correlation_method;
        }
        
        public void setLogFileName(String s)
        {
            log_file_name = s;
        }
        public String getLogFileName()
        {
            return log_file_name;
        }
        
        
        public void setCsvOutputFileName(String s)
        {
            csv_results_output_file_name = s;
        }
        public String getCsvOutputFileName()
        {
            return csv_results_output_file_name;
        }
        public void setCytoscapeOutputFileName(String s)
        {
            cytoscape_results_output_file_name = s;
        }
        public String getCytoscapeOutputFileName()
        {
            return cytoscape_results_output_file_name;
        }
        public void setUseOutputFileNameRoot(boolean b)
        {
            use_output_file_name_root = b;
        }
        public boolean getUseOutputFileNameRoot()
        {
            return use_output_file_name_root;
        }
        public void setOutputFileNameRoot(String s)
        {
            output_file_name_root = s;
        }
        public String getOutputFileNameRoot()
        {
            return output_file_name_root;
        }
        public void setCorrCsvOutputFileName(String s)
        {
            corr_csv_output_file_name = s;
        }
        public String getCorrCsvOutputFileName()
        {
            return corr_csv_output_file_name;
        }
        public void setLocalCorrCsvOutputFileStem(String s)
        {
            local_corr_csv_output_file_name_stem = s;
        }
        public String getLocalCorrCsvOutputFileStem()
        {
            return local_corr_csv_output_file_name_stem;
        }
        public void setHtmlOutputFileName(String s)
        {
            html_results_output_file_name = s;
        }
        public String getHtmlOutputFileName()
        {
            return html_results_output_file_name;
        }
        public void setLatexOutputFileName(String s)
        {
            latex_results_output_file_name = s;
        }
        public String getLatexOutputFileName()
        {
            return latex_results_output_file_name;
        }
        
        @Override
        public String toString() 
        {
            StringBuilder sb = new StringBuilder();
            sb.append("first_input_file_path=").append(config.getFirstInputFilePath());
            sb.append(System.lineSeparator());
            sb.append("first_has_header=").append(config.getFirstHasHeader());
            sb.append(System.lineSeparator());               
            sb.append("first_is_srna=").append(config.getFirstIsSrna());
            sb.append(System.lineSeparator());                
            sb.append("second_input_file_path=").append(config.getSecondInputFilePath());
            sb.append(System.lineSeparator());
            sb.append("second_has_header=").append(config.getSecondHasHeader());
            sb.append(System.lineSeparator());
            sb.append("second_is_srna=").append(config.getSecondIsSrna());
            sb.append(System.lineSeparator());
            sb.append("num_samples=").append(config.getNumSamples());
            sb.append(System.lineSeparator());
            sb.append("num_replicates=").append(config.getNumReplicates());
            sb.append(System.lineSeparator());
            sb.append("auto_sample_names=").append(config.getAutoSampleNames());
            sb.append(System.lineSeparator());
            sb.append("sample_name_root=").append(config.getSampleNameRoot());
            sb.append(System.lineSeparator());
            sb.append("sample_names=").append(config.getSampleNames());
            sb.append(System.lineSeparator());
            sb.append("filter_srna_data=").append(config.getFilterSrnaData());
            sb.append(System.lineSeparator());
            sb.append("srna_ofc_PC_cutoff=").append(config.getPCcutoff());
            sb.append(System.lineSeparator());
            sb.append("filter_non_srna_data=").append(config.getFilterNonSrnaData());
            sb.append(System.lineSeparator());
            sb.append("non_srnas_filter_level=").append(config.getNonSrnasFilterLevel());
            sb.append(System.lineSeparator());              
            sb.append("correlation_method=").append(config.getCorrMethod());
            sb.append(System.lineSeparator());
            sb.append("correlation_threshold=").append(config.getCorrThr());
            sb.append(System.lineSeparator());
            sb.append("use_local_correlations=").append(config.getLocalCorrs());
            sb.append(System.lineSeparator());
            sb.append("min_window_for_local_corr=").append(config.getMinWindow());
            sb.append(System.lineSeparator());           
            sb.append("output_file_dir=").append(config.getOutputFileDirName());
            sb.append(System.lineSeparator());
            sb.append("use_output_file_name_root=").append(config.getUseOutputFileNameRoot());
            sb.append(System.lineSeparator());
            sb.append("output_file_name_root=").append(config.getOutputFileNameRoot());
            sb.append(System.lineSeparator());
            sb.append("record_log_file=").append(config.getRecordLogFile());
            sb.append(System.lineSeparator()); 
            sb.append("log_file_name=").append(config.getLogFileName());
            sb.append(System.lineSeparator());            
            sb.append("output_corr_to_csv=").append(config.getOutputCorrToCsv());
            sb.append(System.lineSeparator());                
            sb.append("corr_csv_output_file_name=").append(config.getCorrCsvOutputFileName());
            sb.append(System.lineSeparator());
            sb.append("output_local_corr_to_csv=").append(config.getOutputLocalCorrToCsv());
            sb.append(System.lineSeparator());
            sb.append("local_corr_csv_output_file_name_stem=").append(config.getLocalCorrCsvOutputFileStem());
            sb.append(System.lineSeparator());                               
            sb.append("output_results_to_csv=").append(config.getOutputToCsv());
            sb.append(System.lineSeparator());
            sb.append("csv_results_output_file_name=").append(config.getCsvOutputFileName());
            sb.append(System.lineSeparator());                
            sb.append("output_results_to_cytoscape=").append(config.getOutputToCytoscape());
            sb.append(System.lineSeparator());
            sb.append("cytoscape_results_output_file_name=").append(config.getCytoscapeOutputFileName());
            sb.append(System.lineSeparator());                
            sb.append("output_results_to_html=").append(config.getOutputToHtml());
            sb.append(System.lineSeparator());
            sb.append("html_results_output_file_name=").append(config.getHtmlOutputFileName());
            sb.append(System.lineSeparator());
            sb.append("output_results_to_latex=").append(config.getOutputToLatex());
            sb.append(System.lineSeparator());
            sb.append("latex_results_output_file_name=").append(config.getLatexOutputFileName());
            sb.append(System.lineSeparator());
            
                
            
            sb.append("down_colour=").append(config.getDownColour());
            sb.append(System.lineSeparator());
            sb.append("up_colour=").append(config.getUpColour());
            sb.append(System.lineSeparator());               
            sb.append("include_patterns_in_output=").append(config.getIncludePatterns());
            sb.append(System.lineSeparator());
            sb.append("output_first_dataset_first=").append(config.getFirstDatasetFirst());
            sb.append(System.lineSeparator());
            sb.append("sort_by_patterns_from_first_dataset=").append(config.getSortOnFirstDataPatterns());
            sb.append(System.lineSeparator());

            return sb.toString();
        }
}


