/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.targetrules;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.*;
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
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2WF.Paresnip2WorkflowModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2TargetRulesSceneController implements Initializable, ControlledScreen {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;

    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;

    private JFXStatusTracker tracker = new JFXStatusTracker();
    private Paresnip2TargetRulesJSReceiver myJSBridge;

    private final Paresnip2TargetRulesWorkflowModule myParent;

    public Paresnip2TargetRulesSceneController(Rectangle2D visualBounds, Paresnip2TargetRulesWorkflowModule parent) {
        myParent = parent;
        webViewSize = visualBounds;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'PAREsnip2DataInputScene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'PAREsnip2DataInputScene.fxml'.";

        mainWebEngine = mainWebView.getEngine();

        myJSBridge = new Paresnip2TargetRulesJSReceiver(this.myController);
        mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState)
                -> {

            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());

        mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("PAREsnip 2 Target Rules Event: " + arg0);
        });

        refreshMainView();
    }

    private void refreshMainView() {
        try {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/PAREsnip2TargetRulesView.html");
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

    public class Paresnip2TargetRulesJSReceiver extends JavascriptBridge {

        RuleSet rules = RuleSet.getRuleSet();

        public Paresnip2TargetRulesJSReceiver(ScreensController controller) {
            super(controller);
        }

        public boolean setMMPos10() {
            if (rules.isAllowedMM10()) {
                rules.setAllowMM10(false);
            } else {
                rules.setAllowMM10(true);
            }

            return rules.isAllowedMM10();
        }

        public boolean setMMPos11() {
            if (rules.isAllowedMM11()) {
                rules.setAllowMM11(false);
            } else {
                rules.setAllowMM11(true);
            }

            return rules.isAllowedMM11();
        }

        public boolean setGapsAsMM() {
            if (rules.isGapCountAsMM()) {
                rules.setGapCountAsMM(false);
            } else {
                rules.setGapCountAsMM(true);
            }

            return rules.isGapCountAsMM();
        }

        public boolean setGUAsMM() {
            if (rules.isGUCountAsMM()) {
                rules.setGUCountAsMM(false);
            } else {
                rules.setGUCountAsMM(true);
            }

            return rules.isGUCountAsMM();
        }

        public void setCoreRegionStartPos(int n) {
            rules.setCoreRegionStart(n);
        }

        public void setCoreRegionEndPos(int n) {
            rules.setCoreRegionEnd(n);
        }

        public void setCoreRegionMultiplier(double n) {
            rules.setCoreRegionMultiplier(n);

        }

        public void setMaxAdjacentMMCore(int n) {
            rules.setMaxAdjacentMMCoreRegion(n);
        }

        public void setMaxMMCore(int n) {
            rules.setMaxMMCoreRegion(n);
        }

        public void setMMScore(double n) {
            rules.setScoreMM(n);
        }

        public void setGapScore(double n) {
            rules.setGapScore(n);
        }

        public void setGUScore(double n) {
            rules.setGUScore(n);
        }

        public void setMaxScore(double n) {
            rules.setMaxScore(n);
        }

        public void setMaxMM(int n) {
            rules.setMaxMM(n);
        }

        public void setMaxGU(int n) {
            rules.setMaxGUWobbles(n);
        }

        public void setMaxGaps(int n) {
            rules.setMaxGaps(n);
        }

        public void setMaxAdjacentMM(int n) {
            rules.setMaxAdjacentMM(n);
        }

        public void setPos10MMScore(double n) {
            rules.setScoreMM10(n);
        }

        public void setPos11MMScore(double n) {
            rules.setScoreMM11(n);
        }

        public boolean setAllowedMM(int pos) {
            //set the position to the position in the array
            pos = pos - 1;
            return rules.setAllowedMM(pos);
        }

        public boolean setNotAllowedMM(int pos) {
            //set the position to the position in the array
            pos = pos - 1;
            return rules.setNotAllowedMM(pos);
        }

        public boolean isValid() {
            //must check things here such as
            //allowed and not allowed MM being
            //the same.... etc

            return true;
        }

        public void setAllenRules() {
            rules.setDefaultAllen();

        }

        public void setCarringtonRules() {
            rules.setDefaultCarrington();

        }

        public void setReady() throws InterruptedException {

            myParent.setReadyToContinue();
            WorkflowSceneController.setReadyNode(myParent.getID());
            returnToMainWorkflow();

        }

        public void hideWizard() {
            //ScreensController.getInstance().setScreen(parentController.getModule().getID());
            returnToMainWorkflow();

        }

        private String configFileSaveLocation() {
            FileChooser chooser = new FileChooser();
//            if (this.lastFileDir != null) {
//                chooser.setInitialDirectory(this.lastFileDir);
//            }
            chooser.setTitle("Select output directory");

            File selectedDirectory = chooser.showSaveDialog(scene.getWindow());

            if (selectedDirectory != null) {
                //this.lastFileDir = selectedDirectory;
                //myParent.setOutputDir(selectedDirectory.toPath());
                return selectedDirectory.getAbsolutePath();
            }

            return "Not Selected";
        }

        public void saveConfig() {

            //reset the last directory so the user can choose where to save the file
            //this.lastFileDir = null;
            String dir = configFileSaveLocation();
            try {
                if (!dir.equals("Not Selected")) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("allow_mismatch_position_10=").append(rules.isAllowedMM10());
                    sb.append(System.lineSeparator());
                    sb.append("mismatch_position_10_penalty=").append(rules.getScoreMM10());
                    sb.append(System.lineSeparator());
                    sb.append("allow_mismatch_position_11=").append(rules.isAllowedMM11());
                    sb.append(System.lineSeparator());
                    sb.append("mismatch_position_11_penalty=").append(rules.getScoreMM11());
                    sb.append(System.lineSeparator());
                    sb.append("gaps_count_as_mismatch=").append(rules.isGapCountAsMM());
                    sb.append(System.lineSeparator());
                    sb.append("gu_count_as_mismatch=").append(rules.isGUCountAsMM());
                    sb.append(System.lineSeparator());
                    sb.append("core_region_start=").append(rules.getCoreRegionStart());
                    sb.append(System.lineSeparator());
                    sb.append("core_region_end=").append(rules.getCoreRegionEnd());
                    sb.append(System.lineSeparator());
                    sb.append("core_region_multiplier=").append(rules.getCoreRegionMultiplier());
                    sb.append(System.lineSeparator());
                    sb.append("max_adjacent_mismatches_core_region=").append(rules.getMaxAdjacentMMCoreRegion());
                    sb.append(System.lineSeparator());
                    sb.append("max_mismatches_core_region=").append(rules.getMaxMMCoreRegion());
                    sb.append(System.lineSeparator());
                    sb.append("mismatch_score=").append(rules.getMMScore());
                    sb.append(System.lineSeparator());
                    sb.append("gap_score=").append(rules.getGapScore());
                    sb.append(System.lineSeparator());
                    sb.append("gu_score=").append(rules.getGUWobbleScore());
                    sb.append(System.lineSeparator());
                    sb.append("max_score=").append(rules.getMaxScore());
                    sb.append(System.lineSeparator());
                    sb.append("max_mismatches=").append(rules.getMaxMM());
                    sb.append(System.lineSeparator());
                    sb.append("max_gu_pairs=").append(rules.getMaxGUWobbles());
                    sb.append(System.lineSeparator());
                    sb.append("max_gaps=").append(rules.getMaxGaps());
                    sb.append(System.lineSeparator());
                    sb.append("max_adjacent_mismatches=").append(rules.getMaxAdjacentMM());
                    sb.append(System.lineSeparator());
                    sb.append("permissible_mismatch_positions=").append(rules.getPermissibleMM());
                    sb.append(System.lineSeparator());
                    sb.append("non_permissible_mismatch_positions=").append(rules.getNonPermissibleMM());
                    

                    File file = new File(dir);

                    FileWriter writer = new FileWriter(file);
                    writer.write(sb.toString());
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
    }
}
