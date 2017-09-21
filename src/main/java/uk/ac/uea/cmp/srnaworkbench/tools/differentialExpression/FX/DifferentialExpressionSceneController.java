/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.SamplePairManager;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.DifferentialExpressionWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class DifferentialExpressionSceneController implements Initializable, ControlledScreen
{
    @FXML
    private WebView webView;
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    private WebEngine webEngine;
    
    private ScreensController myController;
    
    private Rectangle2D visualBounds;
    
    private JSObject window;
    
    private SamplePairManager samplePairs = new SamplePairManager();
    private final DifferentialExpressionWorkflowModule parent_module;
    
    private double FCcutoff = 1;
    private Scene scene;
    
    public static boolean debugMode = false;
    
    private List<String[]> DE_data = new ArrayList<>();
    private DEJavascriptReceiver deJavascriptReceiver;

    public DifferentialExpressionSceneController(Rectangle2D visualBounds, DifferentialExpressionWorkflowModule parent_module)
    {
        this.visualBounds = visualBounds;
        this.parent_module= parent_module;
        
        
    }
    
    public final void setVisualBounds(Rectangle2D visualBounds)
    {
        
        mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        
    }
    
    private void refreshReviewView() {
        try {

            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR + DIR_SEPARATOR + "HTML" + DIR_SEPARATOR + "DifferentialExpressionReview.html");

            webEngine.load(h_view.toExternalForm());
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        assert webView != null : "fx:id=\"webView\" was not injected: check your FXML file 'DifferentialExpressionScene.fxml'.";
        
        webEngine = webView.getEngine();
        
        setVisualBounds(visualBounds);
        
        
        //using this as the directory should already exist and error if not
        Path getWV_Dir = Paths.get(Tools.WEB_VIEW_DATA_Path);
        if (Files.exists(getWV_Dir)) {
            File file = getWV_Dir.toFile();
            webEngine.setUserDataDirectory(file);
        } else {
            LOGGER.log(Level.SEVERE, "Cannot create web view local storage data");
        }

        webEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Event: " + arg0);
        });

        webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
            deJavascriptReceiver = new DEJavascriptReceiver(this.myController);
            if (newState == Worker.State.SUCCEEDED) {
                window = (JSObject) webEngine.executeScript("window");
                
                window.setMember("app", deJavascriptReceiver);
                
                window.setMember("DE_data", DE_data);
            }
        });

        refreshReviewView();
        
        
 
    }    

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        this.myController = screenPage;
    }

    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        this.scene = scene;
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SamplePairManager getSamplePairs()
    {
        return samplePairs;
    }

    public void populateSampleID_Interface()
    {
        //FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        //List<String> samplenames = fileServ.getSamplenames();
        
        List<String> samplenames = new ArrayList<>(FileHierarchyViewController.retrieveDataPaths().keySet());
        
        Platform.runLater(() ->
        {
            
            for (String sample : samplenames)
            {
                webEngine.executeScript("addToSamplePairTable('" + sample + "')");
            }
        });
        //System.out.println("");
        

    }

    public double getFCCutoff()
    {
        return FCcutoff;
    }

    public void populateTable(Object[] get)
    {
        Platform.runLater(() ->
        {
            if(!debugMode)
            {
                webEngine.executeScript("addToData('" + get[0] + "','" + 
                                                        get[0].toString().length() + "','" + 
                                                        get[6] + "','" + 
                                                        get[3] + "','" + 
                                                        get[4] + "','" + 
                                                        get[1] + "','" + 
                                                        get[2] + "','" +
                                                        get[7] + "','" + 
                                                        get[5] + "')");
            }
            else
            {
                for(int i = 0; i < 10000; i++)
                {
                    //String[] array= {"","ACTG","test","test","test","test","test","test","test","test"};
                    //DE_data.add(array);
                    webEngine.executeScript("addToData('ACTG','test','test','test','test','test','test','test','test')");
                }
                
                //window.setMember("DE_data", DE_data.toArray());
            }
        });
    }

    public void showResults()
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("showMainTable()");
        });
    }

    public void enableExport()
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("enableExportMenu()");
        });
    }

    public void populateAnnotationResults(final List<Object[]> results)
    {
        
        Platform.runLater(() ->
        {
            if (!results.isEmpty())
            {
                for (int i = 0; i < results.size(); i++)
                {
                    String details = "";
                    for (int j = 0; j < 5; j++)
                    {
                        details += "'" + results.get(i)[j].toString() + "'";
                        if (j <= 3)
                        {
                            details += ",";
                        }
                    }
                    webEngine.executeScript("addToAnnotation(" + details + ")");

//            switch(i)
//            {
//                case 2:
//                    details = StringUtils.safeIntegerParse(results.get(i).toString(), -1);
//                    break;
//                case 3:
//                    details = StringUtils.safeIntegerParse(results.get(i).toString(), -1);
//                    break;
//            }
                }
                webEngine.executeScript("showAnnotationsTable()");
            }
            else
            {
                webEngine.executeScript("emptyAnnotationsTable()");
            }
        });
        
        
    }
    
    public void populateAnnotationResults(final ArrayList<String[]> results)
    {
        
        Platform.runLater(() ->
        {
            if (!results.isEmpty())
            {
                for (int i = 0; i < results.size(); i++)
                {
                    String details = "";
                    for (int j = 0; j < 5; j++)
                    {
                        details += "'" + results.get(i)[j] + "'";
                        if (j <= 3)
                        {
                            details += ",";
                        }
                    }
                    webEngine.executeScript("addToAnnotation(" + details + ")");

//            switch(i)
//            {
//                case 2:
//                    details = StringUtils.safeIntegerParse(results.get(i).toString(), -1);
//                    break;
//                case 3:
//                    details = StringUtils.safeIntegerParse(results.get(i).toString(), -1);
//                    break;
//            }
                }
                webEngine.executeScript("showAnnotationsTable()");
            }
            else
            {
                webEngine.executeScript("emptyAnnotationsTable()");
            }
        });
        
        
    }
    
    
    public void displayAnnotationTable()
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("showAnnotationsTable()");
        });
        
    }

    public void setResultSize(int count_results, int threshold)
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("setDEResultSize('" + count_results + "','" + threshold + "')");
        });
    }

    public void enableChunkSelect()
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("enableChunkSelect()");
        });
    }

    @Override
    public void workflowStartedListener() {
     //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
    public class DEJavascriptReceiver extends JavascriptBridge 
    {

        public DEJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        
        public void addSamplePair(String referenceSample, String observedSample)
        {
            //System.out.println("adding " + referenceSample + " observedSample: " + observedSample);
            try
            {
                samplePairs.addSamplePair(referenceSample, observedSample);
            }
            catch (NotInDatabaseException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        public void showListSetupError()
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Differential Expression Configuration Error");
            alert.setContentText("The reference and observed list lengths to not match");

            Platform.runLater(()
                    -> alert.show()
            );
        }
        public boolean begin_D_E()
        {
            System.out.println("checking");
            if (samplePairs.isEmpty())
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Differential Expression Configuration Error");
                alert.setContentText("There are no sample pairs selected for analysis");

                Platform.runLater(()
                        -> alert.show()
                );
                return false;
            }
            System.out.println("begining");

//            parent_module.setNorm_type(norm_type);
            parent_module.setReadyToContinue();

            return true;
        }
        
        public void setFCCutoff(double newValue)
        {
            System.out.println("cutoff set" + newValue);
            FCcutoff = newValue;
        }
 
        public void exportToCSV()
        {
            System.out.println("exporting file to CSV");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose CSV file for Differential Expression table");

            File file = fileChooser.showSaveDialog(scene.getWindow());

            if (file != null)
            {
                try
                {
                    Files.deleteIfExists(file.toPath());
                    parent_module.exportToCSV(file);

                }
                catch (IOException ex)
                {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        public void displayAnnotations(String sequence)
        {
            if(!debugMode)
            {
                parent_module.queryAnnotations(sequence, DifferentialExpressionSceneController.this);
            }
            else
            {
                List<Object[]> test = new ArrayList<>();
                String[] data = {"0","1","2","3","4"};
                test.add(data);
                
                populateAnnotationResults(test);
                displayAnnotationTable();
            }
            
        }
        
        public void debug()
        {
            debugMode = true;
            populateTable(null);
            showResults();
            
        }
        
        public void loadResultSet(int currentWindowStart, int currentWindowEnd)
        {
            
            parent_module.loadResultSet(currentWindowStart, currentWindowEnd, DifferentialExpressionSceneController.this);
        }
 
        
    }
    
}
