/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.normalise.FX;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
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
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class NormalisationController implements Initializable, ControlledScreen {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;

    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;

    private JFXStatusTracker tracker = new JFXStatusTracker();

    private List<NormalisationType> norms = new ArrayList<>();

    private final NormalisationWorkflowServiceModule parentModule;
    private NormJavascriptReceiver normJavascriptReceiver;

    public NormalisationController(Rectangle2D size, NormalisationWorkflowServiceModule parentModule) {
        webViewSize = size;

        this.parentModule = parentModule;

        //all normalisations enabled by default
        norms.add(NormalisationType.TOTAL_COUNT);

        norms.add(NormalisationType.UPPER_QUARTILE);

        norms.add(NormalisationType.TRIMMED_MEAN);

        norms.add(NormalisationType.QUANTILE);

        norms.add(NormalisationType.BOOTSTRAP);

        norms.add(NormalisationType.DESEQ);

    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'NormalisationScene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'NormalisationScene.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'NormalisationScene.fxml'.";

        mainWebEngine = mainWebView.getEngine();

        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());

        tracker.setProgressWheelName("Normalisation Status");
        tracker.setWebEngine(mainWebEngine);

        mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Normalisation Event: " + arg0);
        });

        mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
            normJavascriptReceiver = new NormJavascriptReceiver(this.myController);
            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", normJavascriptReceiver);
            }
        });

        refreshMainView();
//        // mainWebEngine.reload();
//        try {
//            String html = (String) mainWebEngine.executeScript("document.documentElement.outerHTML");
//            System.out.println("html:'" + html + "'");
//        } catch (Exception ex) {
//        }
    }

    private void refreshMainView()
    {

        try
        {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/NormalisationView.html");
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
    public void setStageAndSetupListeners(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        myController = screenPage;
    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        return tracker;
    }

    public List<NormalisationType> getNorms() {
//        if(DatabaseWorkflowModule.getInstance().isDebugMode())
//        {
//            norms.add(NormalisationType.TOTAL_COUNT);
//            //norms.add(NormalisationType.QUANTILE);
//        }
//        else
//        {
//            Platform.runLater(() ->
//            {
//                this.mainWebEngine.executeScript("insertNormalisationTypes()");
//            });
//        }
        return norms;
    }

    public void setBusy(boolean state)
    {
        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            Platform.runLater(() ->
            {
                mainWebEngine.executeScript("setBusy( '" + state + "' )");
            });
        }

    }

    @Override
    public void workflowStartedListener() {
        //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class NormJavascriptReceiver extends JavascriptBridge {

        // stores the last directory that was looked in using a filechooser

        private File lastFileDir = null;

        public NormJavascriptReceiver(ScreensController controller) {
            super(controller);
        }

        public void addToNorms(String normType) {

            switch (normType) {
                case "TC":
                    norms.add(NormalisationType.TOTAL_COUNT);
                    break;
                case "UQ":
                    norms.add(NormalisationType.UPPER_QUARTILE);
                    break;
                case "TMM":
                    norms.add(NormalisationType.TRIMMED_MEAN);
                    break;
                case "Q":
                    norms.add(NormalisationType.QUANTILE);
                    break;
                case "B":
                    norms.add(NormalisationType.BOOTSTRAP);
                    break;
                case "DE":
                    norms.add(NormalisationType.DESEQ);
                    break;
            }
        }

        public void removeFromNorms(String normType) {

            switch (normType) {
                case "TC":
                    norms.remove(NormalisationType.TOTAL_COUNT);
                    break;
                case "UQ":
                    norms.remove(NormalisationType.UPPER_QUARTILE);
                    break;
                case "TMM":
                    norms.remove(NormalisationType.TRIMMED_MEAN);
                    break;
                case "Q":
                    norms.remove(NormalisationType.QUANTILE);
                    break;
                case "B":
                    norms.remove(NormalisationType.BOOTSTRAP);
                    break;
                case "DE":
                    norms.remove(NormalisationType.DESEQ);
                    break;
            }
        }

        public void exportToFASTA() {

            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export FASTA");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                parentModule.writeFASTA(selectedDirectory.toPath());

            }

        }

        public void exportToCSV() {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export FASTA");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                parentModule.writeCSV(selectedDirectory.toPath());

            }

        }
        
        public void setContinue(String state) throws Exception
        {
            //System.out.println("ready status: " + readyStatus);
            //System.out.println("new State: " + state);
            boolean readyStatus = Boolean.parseBoolean(state);
            parentModule.setReadyToContinue(readyStatus);
        }

    }

}
