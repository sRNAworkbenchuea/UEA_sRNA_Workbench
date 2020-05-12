/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.parefirst.paresnip2;

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
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2DataInputSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2DataInputWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author salmayz
 */
public class Paresnip2WithinPAREfirstSceneController implements Initializable, ControlledScreen {
    
    
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;

    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;

    private JFXStatusTracker tracker = new JFXStatusTracker();
    private Paresnip2WithinMirpareJSReceiver myJSBridge;

    private final Paresnip2WithinPAREfirstWorkflowModule myParent;

    private Paresnip2InputFiles input;

    public Paresnip2WithinPAREfirstSceneController(Rectangle2D visualBounds, Paresnip2WithinPAREfirstWorkflowModule parent) {
        myParent = parent;
        webViewSize = visualBounds;
        input = Paresnip2InputFiles.getInstance();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'PAREsnip2DataInputScene.fxml'.";

        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'PAREsnip2DataInputScene.fxml'.";

        mainWebEngine = mainWebView.getEngine();

        myJSBridge = new Paresnip2WithinMirpareJSReceiver(this.myController);
        mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState)
                -> {

            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());

        mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("PAREsnip 2 within miRPARE Event: " + arg0);
        });

        refreshMainView();
    }

    private void refreshMainView() {
        try {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/PAREsnip2View_for_miRPARE.html");
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

    public class Paresnip2WithinMirpareJSReceiver extends JavascriptBridge {

        private File lastFileDir = null;
        RuleSet rules = RuleSet.getRuleSet();
        
        public Paresnip2WithinMirpareJSReceiver(ScreensController controller) {
            super(controller);
        }

        public boolean isStepComplete(int stepNumber, int toStep) {

            if (toStep < stepNumber) {
                return true;
            }

            String msg = "";
            switch (stepNumber) {
                case 1:
                    msg = input.isStepOneComplete();
                    break;
                case 2:
                    msg = input.isStepTwoComplete();
                    break;
                case 3:
                    msg = Paresnip2Configuration.getInstance().isSetThreeComplete();
                    break;
            }

            if (!msg.isEmpty()) {
                showErrorMessage(msg);
                return false;
            }

            return true;
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

        public String setOutputDir() {
            DirectoryChooser chooser = new DirectoryChooser();
//            if (this.lastFileDir != null) {
//                chooser.setInitialDirectory(this.lastFileDir);
//            }
            chooser.setTitle("Select directory to save results");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                input.setOuputDirectory(selectedDirectory);
                return selectedDirectory.getAbsolutePath();
            }
            return "Not Selected";

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

        
        public File pickSequenceFile(String mes) {
            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle(mes);

            if (this.lastFileDir != null) {
                fileChooser.setInitialDirectory(this.lastFileDir);
            }

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.fa, *.fasta)", "*.fa", "*.fasta");
            fileChooser.getExtensionFilters().add(extFilter);

            //File file = fileChooser.showOpenDialog(scene.getWindow());
            File file = fileChooser.showOpenDialog(scene.getWindow());

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                return file;
            }
            return null;
        }

        public String pickDegradomeFile() {

            File file = pickSequenceFile("Select degradome sequence file");

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                if (!input.addDegradomeReplicate(file)) {
                    showErrorMessage("A degradome file with that name already exists!");
                    return "";
                }

                return file.getName();
            }
            return "";
        }

        public String pickGFF3File() {
            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("Select GFF3 file");

            if (this.lastFileDir != null) {
                fileChooser.setInitialDirectory(this.lastFileDir);
            }

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("GFF3 file (*.gff3)", "*.gff3");
            fileChooser.getExtensionFilters().add(extFilter);

            //File file = fileChooser.showOpenDialog(scene.getWindow());
            File file = fileChooser.showOpenDialog(scene.getWindow());

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                if (!input.addGFF(file)) {
                    return "";
                }

                return file.getName();
            }
            return "";
        }

        public String pickSmallRNAFile() {

            File file = pickSequenceFile("Select small RNA sequence file");

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                if (!input.addSmallRNAReplicate(file)) {
                    showErrorMessage("A sRNA file with that name already exists!");
                    return "";
                }

                return file.getName();
            }
            return "";
        }

        public String pickTranscriptomeFile() {

            File file = pickSequenceFile("Select transcriptome sequence file");

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                if (!input.addTranscriptome(file)) {
                    return "";
                }

                return file.getName();
            }
            return "";
        }

        public String pickGenomeFile() {

            File file = pickSequenceFile("Select genome sequence file");

            if (file != null) {
                this.lastFileDir = file.getParentFile();

                if (!input.addGenome(file)) {
                    return "";
                }

                return file.getName();
            }
            return "";
        }

        public boolean checkIfValid() {

            if (!input.isStepOneComplete().isEmpty()) {
                showErrorMessage(input.isStepOneComplete());
                return false;
            }

            if (!input.isStepTwoComplete().isEmpty()) {
                showErrorMessage(input.isStepTwoComplete());
                return false;
            }

            if (!Paresnip2Configuration.getInstance().isSetThreeComplete().isEmpty()) {
                showErrorMessage(Paresnip2Configuration.getInstance().isSetThreeComplete());
                return false;
            }

            return true;

        }

        public void setReady() throws InterruptedException {

            if(!myParent.readyToContinue){
                myParent.setReadyToContinue();
                WorkflowSceneController.setReadyNode(myParent.getID());
            }
            hideWizard();
        }

        public void hideWizard() {
            //ScreensController.getInstance().setScreen(parentController.getModule().getID());
            returnToMainWorkflow();

        }

        public boolean removeSmallRNADataset(String name) {
            return input.removeSmallRNAReplicate(name);
        }

        public void changeAbundanceType() {
            Paresnip2Configuration config = Paresnip2Configuration.getInstance();
            config.setUseWeightedFragmentAbundance(!config.isUseWeightedFragmentAbundance());
        }

        public boolean removeDegradomeDataset(String name) {
            return input.removeDegradomeReplicate(name);
        }

        public boolean removeTranscriptome() {
            input.addTranscriptome(null);
            return true;
        }

        public boolean removeGenome() {
            input.addGenome(null);
            return true;
        }

        public boolean removeGff() {
            input.addGFF(null);
            return true;
        }

        public boolean alignSmallRNAsToGenome() {
            boolean align = Paresnip2Configuration.getInstance().isAlignSmallRNAsToGenome();
            Paresnip2Configuration.getInstance().setAlignSmallRNAsToGenome(!align);
            return Paresnip2Configuration.getInstance().isAlignSmallRNAsToGenome();
        }

        public void setUsingTranscriptome(boolean b) {
            Paresnip2Configuration.getInstance().setUsingTranscriptome(b);
            Paresnip2Configuration.getInstance().setUsingGenomeGFF(!Paresnip2Configuration.
                    getInstance().isUsingGenomeGFF());
        }

        public void setUsingGenomeGFF(boolean b) {

            Paresnip2Configuration.getInstance().setUsingGenomeGFF(b);
            Paresnip2Configuration.getInstance().setUsingTranscriptome(!Paresnip2Configuration.
                    getInstance().isUsingGenomeGFF());
        }

        public boolean setUseConservation() {
            Paresnip2Configuration config = Paresnip2Configuration.getInstance();
            config.setUseConservationFilter(!config.isUseConservationFilter());
            return config.isUseConservationFilter();
        }

        public int getThreads() {
            return Runtime.getRuntime().availableProcessors();
        }

        public void saveConfig() {

            //reset the last directory so the user can choose where to save the file
            //this.lastFileDir = null;
            String dir = configFileSaveLocation();

            if (!dir.equals("Not Selected")) {

                Paresnip2Configuration config = Paresnip2Configuration.getInstance();
                StringBuilder sb = new StringBuilder();

                // Targetting rules
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
                    

                File file = new File(dir + "_rules");

                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(sb.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(Paresnip2DataInputSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                // Parameters
                sb = new StringBuilder();

                sb.append("use_transcriptome=").append(config.isUsingTranscriptome());
                sb.append(System.lineSeparator());
                sb.append("use_genome_gff=").append(config.isUsingGenomeGFF());
                sb.append(System.lineSeparator());
                sb.append("include_UTR=").append(config.isIncludeUTR());
                sb.append(System.lineSeparator());
                sb.append("align_sRNAs=").append(config.isAlignSmallRNAsToGenome());
                sb.append(System.lineSeparator());
                sb.append("use_conservation_filter=").append(config.isUseConservationFilter());
                sb.append(System.lineSeparator());
                sb.append("use_mfe_filter=").append(config.isUseFilter());
                sb.append(System.lineSeparator());
                sb.append("mfe_filter_cutoff=").append(config.getFilterCutoff());
                sb.append(System.lineSeparator());
                sb.append("use_p_value=").append(config.isUsePValue());
                sb.append(System.lineSeparator());
                sb.append("p_value_cutoff=").append(config.getpValueCutoff());
                sb.append(System.lineSeparator());
                sb.append("filter_low_complexity_seqs").append(config.isFilterLowComplexitySeqs());
                sb.append(System.lineSeparator());
                sb.append("category_0=").append(config.getAllowedCategories(0));
                sb.append(System.lineSeparator());
                sb.append("category_1=").append(config.getAllowedCategories(1));
                sb.append(System.lineSeparator());
                sb.append("category_2=").append(config.getAllowedCategories(2));
                sb.append(System.lineSeparator());
                sb.append("category_3=").append(config.getAllowedCategories(3));
                sb.append(System.lineSeparator());
                sb.append("category_4=").append(config.getAllowedCategories(4));
                sb.append(System.lineSeparator());
                sb.append("number_of_threads=").append(config.getNumberOfThreads());
                sb.append(System.lineSeparator());
                sb.append("min_sRNA_abundance=").append(config.getMinSmallRNAAbundance());
                sb.append(System.lineSeparator());
                sb.append("min_sRNA_length=").append(config.getMinSmallRNALenth());
                sb.append(System.lineSeparator());
                sb.append("max_sRNA_length=").append(config.getMaxSmallRNALenth());
                sb.append(System.lineSeparator());
                sb.append("min_fragment_length=").append(config.getMinFragmentLenth());
                sb.append(System.lineSeparator());
                sb.append("max_fragment_length=").append(config.getMaxFragmentLenth());

                file = new File(dir + "_params");

                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(sb.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(Paresnip2DataInputSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        public boolean usingFilter() {
            Paresnip2Configuration.getInstance().setUseFilter(!Paresnip2Configuration.
                    getInstance().isUseFilter());
            return Paresnip2Configuration.
                    getInstance().isUseFilter();
        }

        public boolean usingPValue() {
            Paresnip2Configuration.getInstance().setUsePValue(!Paresnip2Configuration.
                    getInstance().isUsePValue());
            return Paresnip2Configuration.
                    getInstance().isUsePValue();
        }
        
        public boolean includeUTR() {
            Paresnip2Configuration.getInstance().setIncludeUTR(!Paresnip2Configuration.
                    getInstance().isIncludeUTR());
            return Paresnip2Configuration.
                    getInstance().isIncludeUTR();
        }

        public boolean usingLowComplexityFilter() {
            Paresnip2Configuration.getInstance().
                    setFilterLowComplexitySeqs(!Paresnip2Configuration.
                            getInstance().isFilterLowComplexitySeqs());

            return Paresnip2Configuration.getInstance().
                    isFilterLowComplexitySeqs();
        }

        public void setPValue(double value) {
            Paresnip2Configuration.getInstance().setpValueCutoff(value);
        }

        public void setFilterValue(double value) {
            Paresnip2Configuration.getInstance().setFilterCutoff(value);
        }

        public void setMinSRNALength(int n) {
            Paresnip2Configuration.getInstance().setMinSmallRNALenth(n);
        }

        public void setMaxSRNALength(int n) {
            Paresnip2Configuration.getInstance().setMaxSmallRNALenth(n);
            RuleSet.getRuleSet().resetMMArrays(n);
        
        }

        public void setMinTagLength(int n) {
            Paresnip2Configuration.getInstance().setMinFragmentLenth(n);
        }

        public void setMaxTagLength(int n) {
            Paresnip2Configuration.getInstance().setMaxFragmentLenth(n);

        }

        public void setNumThreads(int n) {
            Paresnip2Configuration.getInstance().setNumberOfThreads(n);
        }

        public void setMinAbundance(int n) {
            Paresnip2Configuration.getInstance().setMinSmallRNAAbundance(n);
        }

        public boolean setCategory(int n) {
            Paresnip2Configuration config = Paresnip2Configuration.getInstance();
            config.setAllowedCategories(n);
            return config.getAllowedCategories(n);
        }

        public boolean setDefaultStringentParameters() {

            try {
                Paresnip2Configuration.getInstance().setDefaultStringentParameters();
                return true;
            } catch (Exception ex) {
                return false;
            }

        }

        public boolean setDefaultFlexibleParameters() {

            try {
                Paresnip2Configuration.getInstance().setDefaultFlexibleParameters();
                return true;
            } catch (Exception ex) {
                return false;
            }

        }

        public void loadConfig() {
            try {
                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Select saved configuration file");

                if (this.lastFileDir != null) {
                    fileChooser.setInitialDirectory(this.lastFileDir);
                }

                //File file = fileChooser.showOpenDialog(scene.getWindow());
                File file = fileChooser.showOpenDialog(scene.getWindow());
                Paresnip2Configuration.getInstance().loadConfig(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        
        /*
            Target rules
        */
        
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

//        public void setReady() {
//
//            myParent.setReadyToContinue();
//            WorkflowSceneController.setReadyNode(myParent.getID());
//            returnToMainWorkflow();
//
//        }
//
//        public void hideWizard() {
//            //ScreensController.getInstance().setScreen(parentController.getModule().getID());
//            returnToMainWorkflow();
//
//        }


        public void saveTargetRulesConfig() {

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
    }
}
