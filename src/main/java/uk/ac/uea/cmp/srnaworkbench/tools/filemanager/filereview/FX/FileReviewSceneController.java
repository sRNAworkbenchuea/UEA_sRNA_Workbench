/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.FX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
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
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModule;


/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class FileReviewSceneController implements Initializable, ControlledScreen
{



    

    @FXML
    private WebView webView;
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    
    
    private WebEngine webEngine;
    
    private Rectangle2D visualBounds;
    
    private ScreensController myController;
    
    private String tableHTML;
    
    private JSObject window;

    private static List<String> selectedFiles = new ArrayList<>();
    private static ArrayList<Integer> removalFlags = new ArrayList<>();


    
    private boolean readyStatus = false;
    
    private FileReviewWorkflowModule myParentModule = null;
    private FRJavascriptReceiver frJavascriptReceiver;
  

    public FileReviewSceneController(Rectangle2D visualBounds, FileReviewWorkflowModule myParentModule)
    {
        this.visualBounds = visualBounds;
        this.myParentModule = myParentModule;
    }

    public final void setVisualBounds(Rectangle2D visualBounds)
    {
        
        mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        
    }
    
    public static void setSelectedFiles(List<String> data)
    {
        selectedFiles = data;
    }
    private void refreshReviewView() {
        try {

            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR + DIR_SEPARATOR + "HTML" + DIR_SEPARATOR + "FileReviewView.html");

            webEngine.load(h_view.toExternalForm());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FileHierarchyViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        assert webView != null : "fx:id=\"webView\" was not injected: check your FXML file 'FileReviewScene.fxml'.";
        
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
            frJavascriptReceiver = new FRJavascriptReceiver(this.myController);
            if (newState == Worker.State.SUCCEEDED) {
                window = (JSObject) webEngine.executeScript("window");
                
                window.setMember("app", frJavascriptReceiver);
            }
            if (webEngine.getLoadWorker().getException() != null && newState == State.FAILED) {
                System.out.println( "Web Engine: " + webEngine.getLoadWorker().getException().toString());
            }
        });
        webEngine.getLoadWorker().exceptionProperty().addListener((ObservableValue<? extends Throwable> ov, Throwable t, Throwable t1) -> {
            System.out.println("Received exception: " + t1.getMessage());
        });
        

        refreshReviewView();
        
       



    }    
    
    public boolean getReadyStatus()
    {
        //System.out.println("ready: " + readyStatus);
        return readyStatus;
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

    public void setupTable()
    {
        Platform.runLater(() ->
        {
            String fileTable = "$('#file_review_fieldset').fileTable(";
            //tableHTML = FileHierarchyViewController.getTableHTML();
            Map <String, List<Path>> samples = FileHierarchyViewController.retrieveDataPaths();
            for(Entry<String, List<Path>> sample : samples.entrySet())
            {
                //webEngine.executeScript("fileReviewTable.addSample('"+sample.getKey()+"')");
                webEngine.executeScript(fileTable + "'addSample'" + ",'" + sample.getKey() + "')");
                for(Path replicate : sample.getValue())
                {
                    //webEngine.executeScript("fileReviewTable.addReplicate('"+sample.getKey()+"', '"+replicate.getFileName().toString()+"')");
                    webEngine.executeScript(fileTable + "'add_file'" + ",'" + sample.getKey() + "', 'sRNA', ['" + replicate.getFileName().toString()+ "'])");
                }
            }
            
            int minSizeClass = DatabaseWorkflowModule.getInstance().getMinSizeClass();
            int maxSizeClass = DatabaseWorkflowModule.getInstance().getMaxSizeClass();
            
            webEngine.executeScript("setupRangeSlider(" + minSizeClass + "," + maxSizeClass + ")");

            //window.setMember("tableHTML", tableHTML);
            //webEngine.executeScript("updateTable()");
            reloadHierarchy();
            
            
        });
    }
    
    public static List<String> getSelectedFiles()
    {
        return selectedFiles;
    }
    
    public static ArrayList<Integer> getRemovalFlags()
    {
        return removalFlags;
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void reloadHierarchy()
    {
        this.webEngine.executeScript("reloadHierarchy()");
        //webEngine.reload();
    }

    @Override
    public void workflowStartedListener() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
    /**
     *
     */
    public class FRJavascriptReceiver extends JavascriptBridge {

        public FRJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        public void setMinMax(int min, int max)
        {
            for(int i = DatabaseWorkflowModule.getInstance().getMinSizeClass(); i<min;i++)
            {
                removalFlags.add(i);
            }
            
            for(int i = max+1; i <= DatabaseWorkflowModule.getInstance().getMaxSizeClass();i++)
            {
                removalFlags.add(i);
            }
        }
        public void addToRemovalList(int entry)
        {
            removalFlags.add(entry);
            
        }
        public void setContinue(String state) throws Exception
        {
            //System.out.println("ready status: " + readyStatus);
            //System.out.println("new State: " + state);
            readyStatus = Boolean.parseBoolean(state);
            myParentModule.setReadyToContinue(readyStatus);
        }
        
        public void addToSelectedFiles(String selected)
        {
            selectedFiles.add(selected);
        }
        
 
        
    }
    
}
