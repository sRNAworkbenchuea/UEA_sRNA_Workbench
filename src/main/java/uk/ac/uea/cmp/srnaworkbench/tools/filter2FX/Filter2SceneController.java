/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filter2FX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;
import netscape.javascript.JSObject;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.rfam.RFAM_FTP_Access;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterGroup;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import static uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams.Definition.MAXIMUM_ABUNDANCE;
import static uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams.Definition.MAXIMUM_LENGTH;
import static uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams.Definition.MINIMUM_ABUNDANCE;
import static uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams.Definition.MINIMUM_LENGTH;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.tools.filter2WF.Filter2WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class Filter2SceneController implements  ControlledScreen, ToolHost
{
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;
    
    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;
    
    private JFXStatusTracker tracker = new JFXStatusTracker();
    private Filter2JavascriptReceiver myJSBridge;
    
    private final Filter2WorkflowModule myParent;
    
    private FilterParams f_params;


    public Filter2SceneController(Rectangle2D size, Filter2WorkflowModule parent)
    {
        myParent = parent;
        webViewSize = size;

    }
    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize()
    {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'Filter2Scene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'Filter2Scene.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'Filter2Scene.fxml'.";

        mainWebEngine = mainWebView.getEngine();

        myJSBridge = new Filter2JavascriptReceiver(this.myController);
        mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) ->
        {
            
            if (newState == State.SUCCEEDED)
            {
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());
        
        mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Filter2 Event: " + arg0);
        });

        

        refreshMainView();
    }    

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        myController = screenPage;
    }

    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        this.scene = scene;
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        return tracker;
    }

    @Override
    public void workflowStartedListener()
    {
    }

    private void refreshMainView()
    {
        try
        {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/Filter2View.html");
            //final URL h_view = new URL("http://www.google.com");

            mainWebEngine.load(h_view.toExternalForm());
            //  mainWebEngine.loadContent("<html><body>This is a test</body></html>");
        }
        catch (MalformedURLException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public FilterParams getParams()
    {
        return this.f_params;
    }

    public void addFilesToInterface(Map<String, List<Path>> samples)
    {
        Platform.runLater(() ->
        {
            for (Map.Entry<String, List<Path>> e : samples.entrySet())
            {
                for (Path p : e.getValue())
                {
                    mainWebEngine.executeScript("addFileToWaitingTable( '"
                            + p.toString() + "','"
                            + FilenameUtils.removeExtension(p.getFileName().toString())
                            + "')");

                }
            }

        });
    }
    
    public void addCompletedFile(Path completedPath)
    {
        Platform.runLater(() ->
        {
         
            mainWebEngine.executeScript("addFileToFinishedTable( '"
                    + completedPath.toString() + "','#"
                    + FilenameUtils.removeExtension(completedPath.getFileName().toString())
                    + "')");

        });
        
    }
    
    @Override
    public void update()
    {
        //a Filter run completed. Update progress bar when available
        this.myParent.updateRun();
        
    }

    /**
     * Superceded by methods in the workflow manager for setting node to busy status from workflow main view
     * @param running 
     */
    @Deprecated
    @Override
    public void setRunningStatus(boolean running)
    {
        
    }

    @Override
    public void showErrorDialog(String message)
    {
        Platform.runLater(() ->
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Workbench Error");
            alert.setHeaderText("Workflow Filter error");
            alert.setContentText(message);

            alert.showAndWait();
        });
    }

    public void addOutputInfoToInterface(Path fileName, FilterGroup results)
    {
        //results.contains(this)
        Platform.runLater(() ->
        {
            for (FilterStage fs : results)
            {
                System.out.println(fs.getStageName());
                switch(fs.getStageName())
                {
                    case "input":
                        mainWebEngine.executeScript("addRowToInputStats( '" +
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter by sequence length":
                        mainWebEngine.executeScript("addRowToLengthStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter low-complexity sequences":
                        mainWebEngine.executeScript("addRowToCompStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter by min-abundance and max-abundance":
                        mainWebEngine.executeScript("addRowToAbundStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter invalid sequences":
                        mainWebEngine.executeScript("addRowToInvalidStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter by kill-list (matches out)":
                        mainWebEngine.executeScript("addRowToKillListStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter by t/rRNA (matches out)":
                        mainWebEngine.executeScript("addRowTotrRNAStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    case "filter by genome (matches in)":
                        mainWebEngine.executeScript("addRowToGenomeStats( '"+
                                fileName.getFileName() +
                                "','" +
                                fs.getTotalReadCount() +
                                "','" +
                                fs.getDistinctReadCount() +
                                "')");
                        break;
                    default:
                        System.out.println("unknown filter stage");
                        LOGGER.log(Level.WARNING, "unknown filter stage");
                        break;
                }

//                mainWebEngine.executeScript("addToStats( '" +
//                                "')");

            }

        });
        System.out.println("Filename" + fileName + " ::: adding" + results.toString() );    
    }
    
    public class Filter2JavascriptReceiver extends JavascriptBridge
    {
        private File lastFileDir = null;
        private File fspKillList;
        private File fspDiscardedLogFile;

        public Filter2JavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        
        public String setOutputDir()
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null)
            {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory for filtered files");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null)
            {
                this.lastFileDir = selectedDirectory;
                myParent.setOutputDir(selectedDirectory.toPath());
                return selectedDirectory.getName();
            }
            
            return "Not Selected";
        }
        public String setDiscardLog()
        {
            
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null)
            {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to save discarded sequence log");

            try{
            File selectedDirectory = chooser.showDialog(scene.getWindow());
            
            if (selectedDirectory != null)
            {
                this.lastFileDir = selectedDirectory;
                fspDiscardedLogFile = selectedDirectory;
                return selectedDirectory.getName();
            }
            
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

           
            return "Not Selected";
            
        }
        public String setKillList()
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select kill list for sequence removal");
            
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (fasta format)", "*.fa", "*.fasta","*.fas");
            
            fileChooser.setSelectedExtensionFilter(extFilter);

            File file = fileChooser.showOpenDialog(scene.getWindow());
            
            
            if (file != null)
            {
                this.lastFileDir = file.getParentFile();
            
                // add each chosen file to the sample list
                
                this.fspKillList = file;
                return file.getName();
            }
            // No files returned from chooser. No changes should happen in the JS
            
            return "Not Selected";
        }
        public String getDefaultParamsPath()
        {
            String defaultPath = Tools.ROOT_DIR + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "default_params" + DIR_SEPARATOR + "default_filter_params.cfg";
            return defaultPath;
        }
        public boolean createParams( String min_len_str, String max_len_str,
                String min_abd_str,String max_abd_str, 
                boolean chkLowComplexity,
                boolean chkFilterInvalidSeq,
                boolean chkDiscardTRRNA,
                boolean chkTRRNASense,
                boolean chkOutputNonRedundant,
                boolean chkOutputRedundant,
                boolean chkGenomeFilter,
                boolean chkKillList,
                boolean chkAddDiscardedLog)
        {
            try
            {
                

                int min_len = min_len_str.length() > 0 ? Integer.parseInt(min_len_str) : MINIMUM_LENGTH.getDefault(Integer.class);
                int max_len = max_len_str.length() > 0 ? Integer.parseInt(max_len_str) : MAXIMUM_LENGTH.getDefault(Integer.class);
                int min_abd = min_abd_str.length() > 0 ? Integer.parseInt(min_abd_str) : MINIMUM_ABUNDANCE.getDefault(Integer.class);
                int max_abd = max_abd_str.length() > 0 ? Integer.parseInt(max_abd_str) : MAXIMUM_ABUNDANCE.getDefault(Integer.class);

                File genome_file = (chkGenomeFilter) ? FileHierarchyViewController.retrieveGenomePath().toFile() : null;
                File kill_file = this.fspKillList;
                File discard_log = this.fspDiscardedLogFile;

                f_params = new FilterParams.Builder()
                        .setMinLength(min_len).setMaxLength(max_len)
                        .setMinAbundance(min_abd).setMaxAbundance(max_abd)
                        .setFilterLowComplexitySeq(chkLowComplexity)
                        .setFilterInvalidSeq(chkFilterInvalidSeq)
                        .setDiscardTRRNA(chkDiscardTRRNA)
                        .setDiscardTRRNASenseOnly(chkTRRNASense)
                        .setOutputNonRedundant(chkOutputNonRedundant)
                        .setOutputRedundant(chkOutputRedundant)
                        .setFilterGenomeHits(chkGenomeFilter)
                        .setFilterKillList(chkKillList)
                        .setAddDiscardLog(chkAddDiscardedLog)
                        .setGenome(genome_file)
                        .setKillList(kill_file)
                        .setDiscardLog(discard_log)
                        .build();
            }
            catch (NumberFormatException ex)
            {
                WorkbenchLogger.LOGGER.log(Level.SEVERE, null, ex);
                Platform.runLater(() ->
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Workbench Error");
                        alert.setHeaderText("Filter Params setup error");
                        alert.setContentText("Creation of params could not be compeleted. Please check the logs for more details");

                        alert.showAndWait();
                    });
               

                return false;
            }
            
            myParent.setParams(f_params);
            return true;
        
        }
        
        public boolean checkReadyStatus()
        {
            
            boolean ready = false;
   
            if (myParent.getOutputDIR() == null || !myParent.getOutputDIR().toFile().isDirectory())
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("Filter setup error");
                alert.setContentText("Output directory is not set (or is not a directory)");

                alert.showAndWait();
                return false;
            }
            
            if(f_params != null)
            {
                ready = true;
                if(f_params.getAddDiscardLog())
                {
                    if(f_params.getDiscardLog().isDirectory())
                    {
                        ready = true;
                    }
                    else
                    {
                        ready = false;

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Workbench Error");
                        alert.setHeaderText("Filter Params setup error");
                        alert.setContentText("Discard log must be a directory");

                        alert.showAndWait();
                    }
                }
                if(f_params.getFilterGenomeHits())
                {
                    if(f_params.getGenome() != null)
                    {
                        ready = true;
                    }
                    else
                    {
                        ready = false;

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Workbench Error");
                        alert.setHeaderText("Filter Params setup error");
                        alert.setContentText("Genome file does not appear to exist");

                        alert.showAndWait();
                    }
                }
                if(f_params.getFilterKillList())
                {
                    if(f_params.getKillList() != null)
                    {
                        ready = true;
                    }
                    else
                    {
                        ready = false;

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Workbench Error");
                        alert.setHeaderText("Filter Params setup error");
                        alert.setContentText("Kill list file does not appear to exist");

                        alert.showAndWait();
                    }
                }
            }
            
            return ready;
        }
        
        public void setContinue(boolean state) throws Exception
        {
            myParent.setReadyToContinue(state);
        }
        
        public boolean updateRFAM()
        {
            boolean result = false;
            
             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Workbench Information");
                        alert.setHeaderText("Slow operation warning");
                        alert.setContentText("Updating the RFAM database can be a potentially slow operation");

            Optional<ButtonType> userInput = alert.showAndWait();
            if (userInput.isPresent() && userInput.get() == ButtonType.OK)
            {
                RFAM_FTP_Access.accessRFAMFTPServer();
                
                
                result = true;
            }

            return result;
        }
    }

}
