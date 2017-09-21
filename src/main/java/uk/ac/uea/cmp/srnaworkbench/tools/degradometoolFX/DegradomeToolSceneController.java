/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradometoolFX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import netscape.javascript.JSObject;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.tools.degradometoolWF.DegradomeToolWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filter2WF.Filter2WorkflowModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class DegradomeToolSceneController implements Initializable, ControlledScreen
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
    private DegradomeToolJSReceiver myJSBridge;
    
    
    
    private final DegradomeToolWorkflowModule myParent;
    
    public DegradomeToolSceneController(Rectangle2D visualBounds, DegradomeToolWorkflowModule parent)
    {
        myParent = parent;
        webViewSize = visualBounds;
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'DegradomeToolScene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'DegradomeToolScene.fxml'.";
        
        mainWebEngine = mainWebView.getEngine();

        myJSBridge = new DegradomeToolJSReceiver(this.myController);
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
    
    private void refreshMainView()
    {
        try
        {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/DegardomeToolView.html");
            //final URL h_view = new URL("http://www.google.com");

            mainWebEngine.load(h_view.toExternalForm());
            //  mainWebEngine.loadContent("<html><body>This is a test</body></html>");
        }
        catch (MalformedURLException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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
    
    public class DegradomeToolJSReceiver extends JavascriptBridge
    {
        private File lastFileDir = null;

        public DegradomeToolJSReceiver(ScreensController controller)
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
                //myParent.setOutputDir(selectedDirectory.toPath());
                return selectedDirectory.getAbsolutePath();
            }
            
            return "Not Selected";
        }
    }
    
}
