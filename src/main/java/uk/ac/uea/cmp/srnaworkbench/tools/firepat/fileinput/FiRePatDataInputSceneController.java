/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author rew13hpu
 */
public class FiRePatDataInputSceneController implements Initializable, ControlledScreen {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;

    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;

    private JFXStatusTracker tracker = new JFXStatusTracker();
    private FiRePatDataInputJSReceiver myJSBridge;

    private final FiRePatDataInputWorkflowModule myParent;

    private FiRePatInputFiles input;

    public FiRePatDataInputSceneController(Rectangle2D visualBounds, FiRePatDataInputWorkflowModule parent) {
        myParent = parent;
        webViewSize = visualBounds;
        input = FiRePatInputFiles.getInstance();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'FiRePatDataInputScene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'FiRePatDataInputScene.fxml'.";

        mainWebEngine = mainWebView.getEngine();

        myJSBridge = new FiRePatDataInputJSReceiver(this.myController);
        mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState)
                -> {

            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());

        mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("FiRePat Data Input Event: " + arg0);
        });

        refreshMainView();
    }

    private void refreshMainView() {
        try {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/FiRePatDataInputView.html");
            //final URL h_view = new URL("http://www.google.com");

            mainWebEngine.load(h_view.toExternalForm());
            //  mainWebEngine.loadContent("<html><body>This is a test</body></html>");
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        myController = screenPage;
    }

    @Override
    public void setStageAndSetupListeners(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        return tracker;
    }

    @Override
    public void workflowStartedListener() {
    }

    // internal class, add functions for firepat gui here 
    public class FiRePatDataInputJSReceiver extends JavascriptBridge {

        public FiRePatDataInputJSReceiver(ScreensController controller) {
            super(controller);
        }
        
        public void setNumSamples(Integer n) 
        {
            num_samples = n;
            input.setnsamples(n);
            FiRePatConfiguration.getInstance().setNumSamples(n);
        }
        public void setNumReplicates(int n) 
        {
            num_replicates = n;
            FiRePatConfiguration.getInstance().setNumReplicates(n);
        }
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
        public String getFirstDatasetName()
        {
            return first_dataset_name;
        }
        public String getSecondDatasetName()
        {
            return second_dataset_name;
        }
        public void setCorrCsvOutputFile(String n) 
        {
            corr_csv_output_file_name = n;
            FiRePatConfiguration.getInstance().setCorrCsvOutputFileName(corr_csv_output_file_name);
        }
        public void setLocalCorrCsvOutputFileStem(String n) 
        {
            local_corr_csv_output_file_name_stem = n;
            FiRePatConfiguration.getInstance().setLocalCorrCsvOutputFileStem(local_corr_csv_output_file_name_stem);
        }
        public void setLogFile(String n) 
        {
            log_file_name = n;
            FiRePatConfiguration.getInstance().setLogFileName(log_file_name);
        }
        public void setCsvOutputFile(String n) 
        {
            csv_results_output_file_name = n;
            FiRePatConfiguration.getInstance().setCsvOutputFileName(csv_results_output_file_name);
        }
        public void setCytoscapeOutputFile(String n) 
        {
            cytoscape_results_output_file_name = n;
            FiRePatConfiguration.getInstance().setCytoscapeOutputFileName(cytoscape_results_output_file_name);
        }
        public void setHtmlOutputFile(String n) 
        {
            html_results_output_file_name = n;
            FiRePatConfiguration.getInstance().setHtmlOutputFileName(html_results_output_file_name);
        }
        public void setLatexOutputFile(String n) 
        {
            latex_results_output_file_name = n;
            FiRePatConfiguration.getInstance().setLatexOutputFileName(latex_results_output_file_name);
        }
        public void setOutputFileNameRoot(String n) 
        {
            output_file_name_root = n;
            FiRePatConfiguration.getInstance().setOutputFileNameRoot(output_file_name_root);
        }
        private String[] getFilePathName(File f)
        {
            String[] output = new String[] {"","",""};
            output[0] = f.getName();
            String p = f.getPath();
            String d = f.getPath().substring(0,f.getPath().lastIndexOf(File.separator));
            output[1] = p;
            output[2] = d;
            return output;
        }
        public String[] getFirstInputFilePathName() 
        {
            String[] output = new String[] {"Not Yet Selected","",""};
            if(first_input_file != null)
            {
                output = getFilePathName(first_input_file);
                return getFilePathName(first_input_file);       
            }
            else
            {
                return output;
            }
        }
        public String[] getSecondInputFilePathName() 
        {
            String[] output = new String[] {"Not Yet Selected","",""};
            if(second_input_file != null)
            {
                output = getFilePathName(second_input_file);
                return getFilePathName(second_input_file);       
            }
            else
            {
                return output;
            }
        }
        public String[] setFirstInputFile() 
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select first data file");
            if (this.lastInputFileDir != null) 
            {
                fileChooser.setInitialDirectory(this.lastInputFileDir);
            }
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Expression data files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(scene.getWindow());
            String[] output = new String[] {"Not Yet Selected","",""};
            if (file != null)
            {
                this.lastInputFileDir = file.getParentFile();
                first_input_file = file;// pass file to FiRePat object at this point?
                input.setFirstInputFile(first_input_file);
                output = getFilePathName(file);
                first_input_file_path = output[1];
                FiRePatConfiguration.getInstance().setFirstInputFilePath(output[1]);
                setDefaultOutputDir(output[2]);
                return output;
            }
            else if(first_input_file != null)
            {
                return getFilePathName(first_input_file);       
            }
            else return output;          
        }
        public String[] setSecondInputFile() 
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select first data file");
            if (this.lastInputFileDir != null) {
                fileChooser.setInitialDirectory(this.lastInputFileDir);
            }
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Expression data files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(scene.getWindow());
            String[] output = new String[] {"Not Yet Selected","",""};
            if (file != null)
            {
                this.lastInputFileDir = file.getParentFile();
                second_input_file = file; 
                input.setSecondInputFile(second_input_file);
                output = getFilePathName(file);
                second_input_file_path = output[1];
                FiRePatConfiguration.getInstance().setSecondInputFilePath(output[1]);
                setDefaultOutputDir(output[2]);
                return output;
            }
            else if(second_input_file != null)
            {
                return getFilePathName(second_input_file);      
            }
            else return output;          
        }
        
        public String setOutputDir() 
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.output_file_dir != null) 
            {
                chooser.setInitialDirectory(this.output_file_dir);
            }
            else if(this.lastInputFileDir != null) 
            {
                chooser.setInitialDirectory(this.lastInputFileDir);
            }
            chooser.setTitle("Select directory to save results");
            File selectedDirectory = chooser.showDialog(scene.getWindow());
            if (selectedDirectory != null) 
            {
                input.setOutputDirectory(selectedDirectory);
                output_file_dir = input.getOutputDirectory();
                System.out.println("selectedDir="+selectedDirectory.getAbsolutePath());
                FiRePatConfiguration.getInstance().setOutputFileDirName(selectedDirectory.getAbsolutePath());
                return selectedDirectory.getAbsolutePath();
            }
            if(output_file_dir!=null)
            {
                return output_file_dir.getAbsolutePath();
            }
            return "Not Yet Selected";
        }
        public void setDefaultOutputDir(String dir)
        {
            if(output_file_dir==null)
            {
                output_file_dir = new File(dir);
                input.setOutputDirectory(output_file_dir);
                FiRePatConfiguration.getInstance().setOutputFileDirName(dir);
            }
        }
        
        private File lastInputFileDir = null;
        private File output_file_dir = null;
        private File first_input_file = null;
        private File second_input_file = null;
        private String first_input_file_path = "";
        private String second_input_file_path = "";        
        
        private boolean output_results_to_csv = true;
        private boolean output_results_to_cytoscape = true;
        private boolean output_corr_to_csv = false;
        private boolean output_local_corr_to_csv = false;
        private boolean output_results_to_html = true;
        private boolean output_results_to_latex = true;
        private boolean first_has_header = false;
        private boolean second_has_header = false;
        private boolean first_is_srna = true;
        private boolean second_is_srna = false;
        private boolean firstShowPath = true;
        private boolean secondShowPath = true;
        private boolean include_patterns_in_output = true;
        private boolean output_first_dataset_first = true;
        private boolean sort_by_patterns_from_first_dataset = true;
        private boolean auto_sample_names = true;
        private boolean record_log_file = true;
        
        private Integer num_samples = null;
        private int num_replicates = 1;
        private boolean filter_srna_data = true;
        private boolean filter_non_srna_data = true;
        private boolean use_local_correlations = false;
        private boolean use_output_file_name_root = false;
        private int min_window_for_local_corr = 4;
        private double correlation_threshold = 0.9;
        double srna_ofc_PC_cutoff = 10; // top % by OFC to retain when filtering out noise
        private double non_srnas_filter_level = 10;
        private String correlation_method = "PEARSON";
        private String csv_results_output_file_name = "FiRePat.csv";
        private String cytoscape_results_output_file_name = "FiRePatCyto.csv";
        private String corr_csv_output_file_name = "FiRePatCorr.csv";
        private String local_corr_csv_output_file_name_stem = "FiRePatLocalCorr";
        private String html_results_output_file_name = "FiRePat.html";
        private String latex_results_output_file_name = "FiRePat.tex";
        private String output_file_name_root = "FiRePat";
        private String sample_name_root = "S-";
        private String sample_names = "";
        private String first_dataset_name = "First";
        private String second_dataset_name = "Second";
        private String log_file_name = "FiRePat.log";
        private int up_colour = 1;
        private int down_colour = 0;
        
        // colours for html and latex output
        public void setDownColour(String col)
        {
            down_colour = getColour(col);
            FiRePatConfiguration.getInstance().setDownColour(down_colour);
        }
        public void setUpColour(String col)
        {
            up_colour = getColour(col);
            FiRePatConfiguration.getInstance().setUpColour(up_colour);
        }
        public int getDownColour()
        {
            return(down_colour);
        }
        public int getUpColour()
        {
            return(up_colour);
        }
        private int getColour(String i)
        {
            switch(i)
            {
                case "RED" : return(0);
                case "GREEN": return(1);
                case "BLUE": return(2);
                case "CYAN": return(3);
                case "MAGENTA": return(4);
                case "ORANGE": return(5);
                case "BLACK": return(6);
                case "WHITE": return(7);
                case "LIGHTGREY": return(8); 
                case "DARKGREY": return(9); 
            }
            return(6);// should never reach here
        }
        public boolean getFirstShowPath()
        {
            return firstShowPath;
        }
        public boolean getSecondShowPath()
        {
            return secondShowPath;
        }
        public void toggleRecordLogFile()
        {
            if(record_log_file==true) record_log_file = false;
            else record_log_file = true;
            FiRePatConfiguration.getInstance().setRecordLogFile(record_log_file);
        }
        public void toggleCorrOutputToCsv()
        {
            if(output_corr_to_csv==true) output_corr_to_csv = false;
            else output_corr_to_csv = true;
            FiRePatConfiguration.getInstance().setOutputCorrToCsv(output_corr_to_csv);
        }
        public void toggleLocalCorrOutputToCsv()
        {
            if(output_local_corr_to_csv==true) output_local_corr_to_csv = false;
            else output_local_corr_to_csv = true;
            FiRePatConfiguration.getInstance().setOutputLocalCorrToCsv(output_local_corr_to_csv);;
        }
        public void toggleOutputToCsv()
        {
            if(output_results_to_csv==true) output_results_to_csv = false;
            else output_results_to_csv = true;
            FiRePatConfiguration.getInstance().setOutputToCsv(output_results_to_csv);
        }
        public void toggleOutputToCytoscape()
        {System.out.println("toggleCyto");
            if(output_results_to_cytoscape==true) output_results_to_cytoscape = false;
            else output_results_to_cytoscape = true;
            FiRePatConfiguration.getInstance().setOutputToCytoscape(output_results_to_cytoscape);
        }
        public void toggleOutputToHtml()
        {
            if(output_results_to_html==true) output_results_to_html = false;
            else output_results_to_html = true;
            FiRePatConfiguration.getInstance().setOutputToHtml(output_results_to_html);
        }
        public void toggleOutputToLatex()
        {
            if(output_results_to_latex==true) output_results_to_latex = false;
            else output_results_to_latex = true;
            FiRePatConfiguration.getInstance().setOutputToLatex(output_results_to_latex);
        }
        public void toggleFirstHasHeader()
        {
            if(first_has_header==true) first_has_header = false;
            else first_has_header = true;
            FiRePatConfiguration.getInstance().setFirstHasHeader(first_has_header);
        }
        public void toggleSecondHasHeader()
        {
            if(second_has_header==true) second_has_header = false;
            else second_has_header = true;
            FiRePatConfiguration.getInstance().setSecondHasHeader(second_has_header);
        }
        public void toggleFirstShowPath()
        {
            if(firstShowPath==true) firstShowPath = false;
            else firstShowPath = true;                
        }       
       public void toggleSecondShowPath()
        {
            if(secondShowPath==true) secondShowPath = false;
            else secondShowPath = true;                
        }       
        public void toggleFirstIsSrna()
        {
            if(first_is_srna==true) first_is_srna = false;
            else first_is_srna = true;
            FiRePatConfiguration.getInstance().setFirstIsSrna(first_is_srna);
        }
        public void toggleSecondIsSrna()
        {
            if(second_is_srna==true) second_is_srna = false;
            else second_is_srna = true;
            FiRePatConfiguration.getInstance().setSecondIsSrna(second_is_srna);
        }
        public void toggleFilterSrnaData()
        {
            if(filter_srna_data==true) filter_srna_data = false;
            else filter_srna_data = true;
            FiRePatConfiguration.getInstance().setFilterSrnaData(filter_srna_data);
        }
        public void toggleFilterNonSrnaData()
        {
            if(filter_non_srna_data==true) filter_non_srna_data = false;
            else filter_non_srna_data = true;
            FiRePatConfiguration.getInstance().setFilterNonSrnaData(filter_non_srna_data);
        }
        public void toggleAutoSampleNames()
        {
            if(auto_sample_names==true) auto_sample_names = false;
            else auto_sample_names = true;
            input.setAutoSampleNames(auto_sample_names);
            FiRePatConfiguration.getInstance().setAutoSampleNames(auto_sample_names);
        }
        public boolean getAutoSampleNames()
        {
            return(auto_sample_names);
        }
        public void setSampleNameRoot(String root)
        {
            sample_name_root = root;
            FiRePatConfiguration.getInstance().setSampleNameRoot(sample_name_root);
        }
        public String getSampleNameRoot()
        {
            return sample_name_root;
        }
        public void setSampleNames(String n)
        {
            sample_names = n;
            String[] snames = sample_names.split(",");
            for(int i=0; i<snames.length; i++) snames[i] = snames[i].trim();
            sample_names = String.join(",", snames);
            FiRePatConfiguration.getInstance().setSampleNames(sample_names);
            input.setSampleNames(n);
        }
        public String getSampleNames()
        {
            return sample_names;
        }
        public void toggleIncludePatterns()
        {
            if(include_patterns_in_output==true) include_patterns_in_output = false;
            else include_patterns_in_output = true;
            FiRePatConfiguration.getInstance().setIncludePatterns(include_patterns_in_output);
        }
        public void toggleFirstDataFirst()
        {
            if(output_first_dataset_first==true) output_first_dataset_first = false;
            else output_first_dataset_first = true;
            FiRePatConfiguration.getInstance().setFirstDatasetFirst(output_first_dataset_first);
        }
        public void toggleSortOnFirstDataPatterns()
        {
            if(sort_by_patterns_from_first_dataset==true) sort_by_patterns_from_first_dataset = false;
            else sort_by_patterns_from_first_dataset = true;
            FiRePatConfiguration.getInstance().setSortOnFirstDataPatterns(sort_by_patterns_from_first_dataset);
        }
        public void toggleUseOutputFileNameRoot()
        {
            if(use_output_file_name_root==true) use_output_file_name_root = false;
            else use_output_file_name_root = true;
            FiRePatConfiguration.getInstance().setUseOutputFileNameRoot(use_output_file_name_root);
        }
        
        
        
        public void setPCcutoff(double pcctoff)
        {
            srna_ofc_PC_cutoff = pcctoff;
            FiRePatConfiguration.getInstance().setPCcutoff(srna_ofc_PC_cutoff);
        }
        
        public void setNonSrnasFilterLevel(double level)// minimum value required to keep an expression series
        {
            non_srnas_filter_level = level;
            FiRePatConfiguration.getInstance().setNonSrnasFilterLevel(non_srnas_filter_level);
        }  
        public void toggleLocalCorrs()
        {
            if(use_local_correlations==true) use_local_correlations = false;
            else use_local_correlations = true;
            FiRePatConfiguration.getInstance().setLocalCorrs(use_local_correlations);
        }
        public void setMinWindow(int w)
        {
            min_window_for_local_corr = w;
            FiRePatConfiguration.getInstance().setMinWindow(min_window_for_local_corr);
        }
        public void setCorThr(double thr)
        {
            correlation_threshold = thr;
            FiRePatConfiguration.getInstance().setCorrThr(correlation_threshold);
        }
        public void setCorrMethod(String corrmethod)
        {
            correlation_method = corrmethod.toUpperCase();
            FiRePatConfiguration.getInstance().setCorrMethod(correlation_method);
        }
        public String getCorThr()
        {
            return(String.valueOf(correlation_threshold));
        }
        public boolean[] getInputFileParameters()
        {
            boolean [] output = {first_has_header, first_is_srna, second_has_header, second_is_srna};
            return output;
        }
        public File[] getInputFiles()
        {
            File[] output = new File[2];
            output[0] = first_input_file;
            output[1] = second_input_file;
            return output;
        }
        public int[] getInputDataParameters()
        {
            int[] output = {num_samples, num_replicates};
            return output;
        }
        public boolean inputFilesAndParametersSet()
        {
            boolean set = true;
            if(first_input_file==null) set = false;
            else if(second_input_file==null) set = false;
            else if(num_samples==null) set = false;
            return set;
        }
        public void showErrorMessage(String msg) {
            String alertContent = msg;
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Input Error");
            alert.setContentText(alertContent);
            Platform.runLater(()
                    -> alert.show()
            );
        }
        public boolean checkNumSamplesAndNames(String n)
        {
            
            if(auto_sample_names==true) return true;
            if(num_samples==null) return false;
            if(n=="") return false;
            String[] allnames = n.split(",");           
            if(allnames.length!=num_samples) return false;
            for(int i=0; i<allnames.length; i++)
            {
                String allnamesi = allnames[i].trim();
                if(allnamesi.equals(""))  return false;
                allnames[i] = allnamesi;
            }
            return true;
        }
        
        public void hideWizard() 
        {
            returnToMainWorkflow();
        }
         
        
         
         public boolean isStepComplete(int stepNumber, int toStep) 
         {
            if (toStep < stepNumber) {
                return true;
            }
            String msg = "";
            switch (stepNumber) 
            {
                case 1:
                    msg = input.isStepOneComplete(first_dataset_name, second_dataset_name, first_input_file_path, second_input_file_path);
                    //System.out.println("STEP 1");
                    break;
                case 2:
                    //msg = input.isStepTwoComplete();
                    //System.out.println("STEP 2");
                    break;
                case 3:
                    msg = input.isStepThreeComplete(up_colour, down_colour);
                    //System.out.println("STEP 3");
                    break;
            }
            if (!msg.isEmpty()) {
                showErrorMessage(msg);
                return false;
            }
            return true;
        }
         
        private String configFileSaveLocation() 
        {
            FileChooser chooser = new FileChooser();
            if (this.output_file_dir != null)  chooser.setInitialDirectory(this.output_file_dir);
            chooser.setTitle("Select output directory");
            File selectedDirectory = chooser.showSaveDialog(scene.getWindow());
            if (selectedDirectory != null) return selectedDirectory.getAbsolutePath();
            return "Not Selected";
        }
         public void saveConfig() {

            String dir = configFileSaveLocation();
            if (!dir.equals("Not Selected")) 
            {
                StringBuilder sb = new StringBuilder();

                FiRePatConfiguration config = FiRePatConfiguration.getInstance();
                
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
                sb.append("first_dataset_name=").append(config.getFirstDatasetName());
                sb.append(System.lineSeparator());
                sb.append("second_dataset_name=").append(config.getSecondDatasetName());
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


                File file = new File(dir);
                try 
                {
                    FileWriter writer = new FileWriter(file);
                    writer.write(sb.toString());
                    writer.flush();
                    writer.close();
                }
                catch (IOException ex) 
                {
                    Logger.getLogger(FiRePatDataInputSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        public boolean checkIfValid() 
        {
            if (!input.isStepOneComplete(first_dataset_name, second_dataset_name, first_input_file_path, second_input_file_path).isEmpty()) {
                showErrorMessage(input.isStepOneComplete(first_dataset_name, second_dataset_name, first_input_file_path, second_input_file_path));
                return false;
            }
            if (!input.isStepThreeComplete(up_colour, down_colour).isEmpty()) {
                showErrorMessage(input.isStepThreeComplete(up_colour, down_colour));
                return false;
            }
            return true;
        }
        public void setReady() throws InterruptedException {

            myParent.setReadyToContinue();
            WorkflowSceneController.setReadyNode(myParent.getID());
            hideWizard();
        }
    }
}
