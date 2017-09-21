/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow.gui;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import netscape.javascript.JSObject;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.MainMDIWindow;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;
import uk.ac.uea.cmp.srnaworkbench.workflow.PreconfiguredWorkflows;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;

/**
 * FXML Controller class Master scene controller for all workflows
 *
 * @author Matt Stocks
 */
public class WorkflowSceneController implements Initializable, ControlledScreen {

    private Stage stage;
    private Scene scene;
    private WorkflowViewer parentFrame;

    ScreensController myController;

    @FXML
    private WebView workflowWebView;
    
    @FXML
    private AnchorPane mainWorkflowAnchor;
    
    @FXML
    private ScrollPane mainScrollPane;
    
    @FXML
    private StackPane mainStackPane;

    private static WebEngine workflowWebEngine;
    
    private Rectangle2D visualBounds;
    
    private MainMDIWindow mainWindow;
    
    final SwingNode swingNode;
    private WFJavascriptReceiver myJSBridge = null;
    
    public WorkflowSceneController()
    {
        swingNode = new SwingNode();
        //mainWindow = new MainMDIWindow();
        createAndSetSwingContent();
    }

    public static WebEngine getEngine()
    {
        return WorkflowSceneController.workflowWebEngine;
    }
    
    @Override
    public void setStageAndSetupListeners(Scene scene) {
        this.scene = scene;
    }

    public void addParentFrame(WorkflowViewer parent) {
        this.parentFrame = parent;
    }
    
    private void createAndSetSwingContent()
    {
        //System.out.println("fire");
        mainWindow = new MainMDIWindow(this);
        
        SwingUtilities.invokeLater(() ->
        {
            //System.out.println("Starting Main MDI window");
//                mainWindow = new MainMDIWindow();
//                // mainWindow.setVisible( true );
//                System.out.println("here");
//                //System.out.println("max size: " + mainWindow.getRootPane().getMaximumSize());
            mainWindow.getRootPane().setMaximumSize(new Dimension(2147483647, 2147483647));
//                
            swingNode.setContent(mainWindow.getRootPane());
        });

        //System.out.println("complete");
    }
    
    public void setWindowToV3()
    {
        Platform.runLater(() ->
        {
            mainStackPane.getChildren().add(swingNode);
        });
    }
    public void setWindowToV4()
    {
        Platform.runLater(() ->
        {
            mainStackPane.getChildren().remove(1);
        });
        
    }
    
    public void setSizeValues(Rectangle2D visualBounds)
    {
        this.visualBounds = visualBounds;
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert workflowWebView != null : "fx:id=\"webView\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert mainWorkflowAnchor != null : "fx:id=\"mainWorkflowAnchor\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";

        workflowWebEngine = workflowWebView.getEngine();

        refreshWorkflowView();
        

        workflowWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
            myJSBridge = new WFJavascriptReceiver(this.myController);

            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) workflowWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
//        frameSize = this.parentFrame.getFrameSize();
        mainWorkflowAnchor.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        mainScrollPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        
        
        workflowWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("workflow Event: " + arg0);
        });
        
        workflowWebEngine.setOnError((WebErrorEvent event) ->
        {
            System.out.println("error in worflow scene: " + event.getMessage());
        });

    }

    public void loadQC() {
        workflowWebEngine.executeScript(" createPreconfiguredFlow(' " + "../json/Workflows/QualityCheck.json" + " ') ");
        System.out.println("executed QC script");
    
    }

    private void refreshWorkflowView() {
        try {

            final URL wf_view = new URL("file:" + WEB_SCRIPTS_DIR + "/HTML/WorkflowView.html");

            workflowWebEngine.load(wf_view.toExternalForm());
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }

    public void destroy()
    {
        if(mainWindow != null)
            mainWindow.dispose();
    }

    /*
    * static methods for setting node to busy (GUI)
    */
    public static void setBusyNode(String id)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                if ((workflowWebEngine != null) && !DatabaseWorkflowModule.getInstance().isDebugMode())
                {
                    workflowWebEngine.executeScript("setWorkflowNodeGFXtoBusy( '" + id + "')");
                }
            });
        }
    }

    public static void setCompleteNode(String id)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                if ((workflowWebEngine != null) && !DatabaseWorkflowModule.getInstance().isDebugMode())
                {
                    workflowWebEngine.executeScript("setWorkflowNodeGFXtoComplete( '" + id + "')");
                }
            });
        }
    }
    
    public static void setReadyNode(String id)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                if ((workflowWebEngine != null) && !DatabaseWorkflowModule.getInstance().isDebugMode())
                {
                    workflowWebEngine.executeScript("setWorkflowNodeGFXtoReady( '" + id + "')");
                }
            });
        }
    }
    
    public static void setWaitingNode(String id)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                if ((workflowWebEngine != null) && !DatabaseWorkflowModule.getInstance().isDebugMode())
                {
                    workflowWebEngine.executeScript("setWorkflowNodeGFXtoWaitingInput( '" + id + "')");
                }
            });
        }
    }

     public static void addToConsole(String message)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                if ((workflowWebEngine != null) && !DatabaseWorkflowModule.getInstance().isDebugMode())
                {
                    workflowWebEngine.executeScript("addToConsoleText( '" + message + "')");
                }
            });
        }
    }

    
    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener() {
  //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void stopWorkflow()
    {
         workflowWebEngine.executeScript("setBusy( '" + false + "')");
    }


    /**
     * Class for recieving input from the main workflow window
     */
    public class WFJavascriptReceiver extends JavascriptBridge {

        public WFJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        public void startWorkflow() throws Exception
        {
            
            if (DatabaseWorkflowModule.getInstance().checkReadyToBuild())
            {
                WorkflowManager.getInstance().start();
                //disable interface components
                workflowWebEngine.executeScript("setBusy( '" + true + "')");
            }
            else
            {
             String alertContent = WorkflowManager.getInstance().containsID("FileManager") ? 
                     "Files have not been setup correctly. Please refer to the File Manager module " :
                     "Files have not been setup correctly. Please refer to the Database module ";
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Configuration Error");
                alert.setContentText(alertContent);

                Platform.runLater(()
                        -> alert.show()
                );
            }
        }
        public String createMiRCatWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.runCommandLineWorkflowMiRCat("mircat", new File("/Users/ujy06jau/Workbench/target/release/data/web/json/mirpare_parameters.json"), visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createMiRPAREWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createDefaultMiRPAREWorkflow(visualBounds);
            JavascriptBridge.resetFirstLoad();

            return fp;
        }

        public String createMiRPAREWorkflowCommandLine() throws Exception
        {
            String fp = PreconfiguredWorkflows.runCommandLineWorkflowMiRPARE("mirpare", new File("/Users/ujy06jau/Workbench/target/release/data/web/json/mirpare_parameters.json"), visualBounds);
            JavascriptBridge.resetFirstLoad();

            return fp;
        }

        public String createPAREsnipWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.runCommandLineWorkflowPAREsnip("paresnip", new File("/Users/ujy06jau/Workbench/target/release/data/web/json/mirpare_parameters.json"), visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createQCWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createQCWorkflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        } 
        
        public String createMiRCat2Workflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createMiRCat2Workflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createQC_DEWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createQC_DE_Workflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createNORM_DEWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createNorm_DE_Workflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createQC_DE_NO_QC_Workflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createQC_DE_NO_QC_Workflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }

        public String createFM_FilterWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createFM_FilterWorkflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }
        
        public String createFM_DegWorkflow() throws Exception
        {
            String fp = PreconfiguredWorkflows.createDegradome2Workflow(visualBounds);
            JavascriptBridge.resetFirstLoad();
            return fp;
        }
        
        public void showVersion3()
        {
            setWindowToV3();
        }
        
//        public void debug()
//        {
//            workflowWebView.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 
//        }
        
    }

}
