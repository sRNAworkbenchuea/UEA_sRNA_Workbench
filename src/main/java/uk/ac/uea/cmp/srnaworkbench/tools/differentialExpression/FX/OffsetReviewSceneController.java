/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
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
import netscape.javascript.JSObject;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.OffsetReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class OffsetReviewSceneController implements Initializable, ControlledScreen
{
    @FXML
    private WebView webView;
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    private WebEngine webEngine;
    
    private ScreensController myController;
    
    private Rectangle2D visualBounds;
    
    private JSObject window;
    
    private NormalisationType norm_type = null;
    
    private OffsetReviewWorkflowModule parent_module = null;
    
    private ORJavascriptReceiver myJSBridge;
    
    public OffsetReviewSceneController(Rectangle2D visualBounds, OffsetReviewWorkflowModule parent_module)
    {
        this.visualBounds = visualBounds;
        this.parent_module = parent_module;
    }
    
    /**
     * Return chosen normalisation type, which is null if not yet chosen
     */
    public NormalisationType getChosenNormType()
    {
        return norm_type;
    }
    
    public final void setVisualBounds(Rectangle2D visualBounds)
    {
        
        mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        
    }
    
    /**
     * Call when this node needs to be ready to receive user input
     */
    public void setupOffsetReview()
    {
        Platform.runLater(()->
        {
//            List<NormalisationType> norms = NormalisationWorkflowServiceModule.getNorms();
//            String[] normTypes = new String[norms.size()];
//            for(int i = 0; i < norms.size(); i++)
//            {
//                
//                switch (norms.get(i).getAbbrev())
//                {
//                    case "RAW":
//                        webEngine.executeScript("appendIndividualNormalisation('RAW')");
//                        break;
//                    case "TC":
//                        webEngine.executeScript("appendIndividualNormalisation('PER_TOTAL')");
//
//                        break;
//                    case "UQ":
//                        webEngine.executeScript("appendIndividualNormalisation('UPPER_QUARTILE')");
//
//                        break;
//                    case "TMM":
//                        webEngine.executeScript("appendIndividualNormalisation('TRIMMED_MEAN')");
//
//                        break;
//                    case "Q":
//                        webEngine.executeScript("appendIndividualNormalisation('QUANTILE')");
//
//                        break;
//                    case "B":
//                        webEngine.executeScript("appendIndividualNormalisation('BOOTSTRAP')");
//
//                        break;
//                    case "DE":
//                        webEngine.executeScript("appendIndividualNormalisation('DESEQ')");
//
//                        break;
//                    default:
//                        System.out.println("unknown normalisation found when populating offset controller");
//                        break;
//                }
// 
//            
//            }
            webEngine.executeScript("detectNorms()");
        });
    }
    
    private void refreshReviewView() {
        try {

            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR + DIR_SEPARATOR + "HTML" + DIR_SEPARATOR + "OffsetReview.html");

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
        
        //setVisualBounds(visualBounds);
        
        mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());

        
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
            myJSBridge = new ORJavascriptReceiver(this.myController);

            if (newState == Worker.State.SUCCEEDED) {
                window = (JSObject) webEngine.executeScript("window");
                window.setMember("app", myJSBridge);
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
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void rebuildOffsetView()
    {
        Platform.runLater(() ->
        {
            webEngine.executeScript("initialiseKL()");
        });
    }

    @Override
    public void workflowStartedListener() {
     //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public class ORJavascriptReceiver extends JavascriptBridge 
    {

        public ORJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
             
        public void setNormType(String type)
        {
            System.out.println("TYPE: " + type);

            switch (type)
            {
                case "RAW":
                    norm_type = NormalisationType.NONE;
                    break;
                case "PER_TOTAL":
                    norm_type = NormalisationType.TOTAL_COUNT;
                    break;
                case "UPPER_QUARTILE":
                    norm_type = NormalisationType.UPPER_QUARTILE;
                    break;
                case "TRIMMED_MEAN":
                    norm_type = NormalisationType.TRIMMED_MEAN;
                    break;
                case "QUANTILE":
                    norm_type = NormalisationType.QUANTILE;
                    break;
                case "BOOTSTRAP":
                    norm_type = NormalisationType.BOOTSTRAP;
                    break;
                case "DESEQ":
                    norm_type = NormalisationType.DESEQ;
                    break;
                default:
                    System.out.println("unknown normalisation read back from offset interface");
                    norm_type = null;
                    break;
            }
            
            System.out.println("Normalisation type for offset review: " + norm_type);
        }
        
        public String[] getNormalisations()
        {
            List<NormalisationType> norms = NormalisationWorkflowServiceModule.getNorms();
            String[] normTypes = new String[norms.size()];
            for(int i = 0; i < norms.size(); i++)
            {
                //normTypes[i] = norms.get(i).getFullName();
                switch (norms.get(i).getAbbrev())
                {
                    case "RAW":
                        normTypes[i] = "RAW";
                        break;
                    case "TC":
                        normTypes[i] = "PER_TOTAL";
                        break;
                    case "UQ":
                        normTypes[i] = "UPPER_QUARTILE";
                        break;
                    case "TMM":
                        normTypes[i] = "TRIMMED_MEAN";
                        break;
                    case "Q":
                        normTypes[i] = "QUANTILE";
                        break;
                    case "B":
                        normTypes[i] = "BOOTSTRAP";
                        break;
                    case "DE":
                        normTypes[i] = "DESEQ";
                        break;
                    default:
                        System.out.println("unknown normalisation found when populating offset controller");
                        break;
                }
 
            
            }
            return normTypes;
        }
        
        public boolean begin_review()
        {
            if(DatabaseWorkflowModule.getInstance().isDebugMode())
            {
                norm_type = NormalisationType.TOTAL_COUNT;
            }
            if (norm_type == null)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Offset Review Configuration Error");
                alert.setContentText("The normalisation type has not been selected");

                Platform.runLater(()
                        -> alert.show()
                );
                return false;
            }
            
            
            parent_module.setNorm_type(norm_type);
            parent_module.setReadyToContinue();

            return true;
        }
        
        public void setWindowLength(String value)
        {
            parent_module.getStrandBiasDivergenceParams().setWindowSize(Integer.valueOf(value));
        }

        public void setOffsetValue(String fileID, String value)
        {
            FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
            Double offset = Double.parseDouble(value);
            //fserv.
            fserv.setOffsetForFileID(fileID, offset);

        }
        
        public void test()
        {
            System.out.println("");
        }
        
        public void onPageLoaded()
        {
             // Initialise the window length setting
            //webEngine.executeScript("$('#winlength').val("+parent_module.getStrandBiasDivergenceParams().getWindowSize()+")");
        }
        
 
        
    }
    
}
