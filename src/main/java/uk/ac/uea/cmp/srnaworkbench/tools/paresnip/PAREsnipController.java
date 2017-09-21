/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class PAREsnipController extends Controller implements ControlledScreen {

    private List<ToolParameters.ParameterDefinition> paramDefs;

    public PAREsnipController(PAREsnipModule module) {
        this.module = module;
        paramDefs = new ArrayList<>();
        paramDefs.add(ParesnipParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefinition());
        paramDefs.add(ParesnipParams.Definition.SUBSEQUENCES_ARE_SECONDARY_HITS.getDefinition());
        paramDefs.add(ParesnipParams.Definition.OUTPUT_SECONDARY_HITS_TO_FILE.getDefinition());
        paramDefs.add(ParesnipParams.Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CATEGORY_0.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CATEGORY_1.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CATEGORY_2.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CATEGORY_3.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CATEGORY_4.getDefinition());
        paramDefs.add(ParesnipParams.Definition.DISCARD_TRRNA.getDefinition());
        paramDefs.add(ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES.getDefinition());
        paramDefs.add(ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_SRNAS.getDefinition());
        paramDefs.add(ParesnipParams.Definition.MINIMUM_FRAGMENT_LENGTH.getDefinition());
        paramDefs.add(ParesnipParams.Definition.MAXIMUM_FRAGMENT_LENGTH.getDefinition());
        paramDefs.add(ParesnipParams.Definition.MINIMUM_SRNA_LENGTH.getDefinition());
        paramDefs.add(ParesnipParams.Definition.MAXIMUM_SRNA_LENGTH.getDefinition());
        paramDefs.add(ParesnipParams.Definition.ALLOW_SINGLE_NT_GAP.getDefinition());
        paramDefs.add(ParesnipParams.Definition.ALLOW_MISMATCH_POSITION_11.getDefinition());
        paramDefs.add(ParesnipParams.Definition.ALLOW_ADJACENT_MISMATCHES.getDefinition());
        paramDefs.add(ParesnipParams.Definition.MAXIMUM_MISMATCHES.getDefinition());
        paramDefs.add(ParesnipParams.Definition.CALCULATE_PVALUES.getDefinition());
        paramDefs.add(ParesnipParams.Definition.NUMBER_OF_SHUFFLES.getDefinition());
        paramDefs.add(ParesnipParams.Definition.PVALUE_CUTOFF.getDefinition());
        paramDefs.add(ParesnipParams.Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF.getDefinition());

        //getEngine().getDocument().getElementById("module_title").setTextContent(module.getDegradomeFile().getName());
    }

    private PAREsnipModule module;
    @FXML
    private WebView mainWebView;
    private JavaApplication jApp;

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "paresnip.html");
    }

    @Override
    protected void initJSClass() {
        jApp = new PAREsnipController.JavaApplication();
        setJSClass(jApp);

    }

    public void write2Log(String log) {
        jApp.write2Log(log);
    }

    @Override
    protected WebView getWebView() {
        return this.mainWebView;
    }
    /*
     * FXML injectable handlers start.
     */

    @FXML
    private void goToMain(ActionEvent event) {
        getScreensController().setScreen(WorkflowViewer.MAIN_SCREEN);
    }
    /*
     * FXML injectable handlers end.
     */

    public void setModule(PAREsnipModule module) {
        this.module = module;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {

        try {
            jApp.updateUI();
        } catch (NotJavaFXThreadException | NoSQLException | HQLQuery.HQLFormatException | HQLQuery.HQLQueryLockedException ex) {
            Logger.getLogger(PAREsnipController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener() {
        jApp.disableInputs();
    }

    public class JavaApplication {

        private boolean initialised;
        private HQLDataTable table;

        public JavaApplication() {
            this.initialised = false;
            this.table = null;
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {
            if (this.table == null) {
                //this.table = new WFQueryResultsTable2("predictions");
                this.table = new HQLDataTable("predictions");
                this.table.setPreTagCol("Duplex");
                this.table.setPreTagCol("sRNA");
                this.table.addRightColumn(2);
                this.table.addRightColumn(3);
                this.table.addRightColumn(5);
                this.table.addRightColumn(7);
            }

            if (module.isComplete()) {
                this.table.setQuery(module.generateOutput());
                this.table.update(getEngine());
            }
            displayParameters();
        }

        public void displayParameters() {
            if (!this.initialised) {
                return;
            }
            try {
                // id of html form elementmust be the same name as the parameter definition
                ParesnipParams parameters = module.getParameters();
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.ALLOW_ADJACENT_MISMATCHES.getName() + "\").checked = " + parameters.isAllowAdjacentMismatches() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.ALLOW_MISMATCH_POSITION_11.getName() + "\").checked = " + parameters.isAllowMismatchAtPositionEleven() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.ALLOW_SINGLE_NT_GAP.getName() + "\").checked = " + parameters.isAllowSingleNtGap() + ";");
                //getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.AUTO_OUTPUT_TPLOT_PDF.getName() + "\").checked = " + parameters.getAutoOutputTplotPdf() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CALCULATE_PVALUES.getName() + "\").checked = " + parameters.isCalculatePvalues() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CATEGORY_0.getName() + "\").checked = " + parameters.isCategory0() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CATEGORY_1.getName() + "\").checked = " + parameters.isCategory1() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CATEGORY_2.getName() + "\").checked = " + parameters.isCategory2() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CATEGORY_3.getName() + "\").checked = " + parameters.isCategory3() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.CATEGORY_4.getName() + "\").checked = " + parameters.isCategory4() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES.getName() + "\").checked = " + parameters.isDiscardLowComplexityCandidates() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_SRNAS.getName() + "\").checked = " + parameters.isDiscardLowComplexitySRNAs() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.DISCARD_TRRNA.getName() + "\").checked = " + parameters.isDiscardTrrna() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF.getName() + "\").checked = " + parameters.isNotIncludePvalueGrtCutoff() + ";");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MAXIMUM_FRAGMENT_LENGTH.getName()).setAttribute("value", parameters.getMaxFragmentLength() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MAXIMUM_MISMATCHES.getName()).setAttribute("value", parameters.getMaxMismatches() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MAXIMUM_SRNA_LENGTH.getName()).setAttribute("value", parameters.getMaxSrnaLength() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MINIMUM_FRAGMENT_LENGTH.getName()).setAttribute("value", parameters.getMinFragmentLength() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MINIMUM_SRNA_ABUNDANCE.getName()).setAttribute("value", parameters.getMinSrnaAbundance() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.MINIMUM_SRNA_LENGTH.getName()).setAttribute("value", parameters.getMinSrnaLength() + "");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.NUMBER_OF_SHUFFLES.getName()).setAttribute("value", parameters.getShuffleCount() + "");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.OUTPUT_SECONDARY_HITS_TO_FILE.getName() + "\").checked = " + parameters.isSecondaryOutputToFile() + ";");
                getEngine().getDocument().getElementById(ParesnipParams.Definition.PVALUE_CUTOFF.getName()).setAttribute("value", parameters.getPvalueCutoff() + "");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.SUBSEQUENCES_ARE_SECONDARY_HITS.getName() + "\").checked = " + parameters.isSubsequenceSecondaryHit() + ";");
                getEngine().executeScript("document.getElementById(\"" + ParesnipParams.Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE.getName() + "\").checked = " + parameters.isWeightedFragmentAbundance() + ";");

            } catch (Exception ex) {
                Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, "Was not able to update GUI for parameters for PAREsnip: ");
            }
        }

        //this recieves a message from the dndTree java script file
        //only when asked for by calling the function callToJavaFX within the js
        public boolean save(int srna_min_abundance, boolean subsequences_secondary_hits, boolean secondary_hits_file,
                boolean weighted_fragment_abundance, boolean category0, boolean category1, boolean category2,
                boolean category3, boolean category4, boolean remove_t_r_rna, boolean remove_low_complexity_candidates,
                boolean remove_low_complexity_sequences, int min_fragment_length, int max_fragment_length,
                int min_sequence_length, int max_sequence_length, boolean single_nucleotide_gap,
                boolean mismatches_at_pos_11, boolean adjacent_mismatches_2_plus, double num_mismatches_allowed,
                boolean p_values, int num_shuffles, double p_value_cut_off, boolean results_greater_cut_off) {

            ParesnipParams.Builder builder = new ParesnipParams.Builder();
            builder.setMin_sRNA_abundance(srna_min_abundance);
            builder.setSubsequences_are_secondary_hits(subsequences_secondary_hits);
            builder.setOutput_secondary_hits_to_file(secondary_hits_file);
            builder.setUse_weighted_fragments_abundance(weighted_fragment_abundance);
            builder.setCategory_0(category0);
            builder.setCategory_1(category1);
            builder.setCategory_2(category2);
            builder.setCategory_3(category3);
            builder.setCategory_4(category4);
            builder.setDiscardTrrna(remove_t_r_rna);
            builder.setDiscard_low_complexity_candidates(remove_low_complexity_candidates);
            builder.setDiscard_low_complexity_srnas(remove_low_complexity_sequences);
            builder.setMin_fragment_length(min_fragment_length);
            builder.setMax_fragment_length(max_fragment_length);
            builder.setMin_sRNA_length(min_sequence_length);
            builder.setMax_sRNA_length(max_sequence_length);
            builder.setAllow_single_nt_gap(single_nucleotide_gap);
            builder.setAllow_mismatch_position_11(mismatches_at_pos_11);
            builder.setAllow_adjacent_mismatches(adjacent_mismatches_2_plus);
            builder.setMax_mismatches(num_mismatches_allowed);
            builder.setCalculate_pvalues(p_values);
            builder.setNumber_of_shuffles(num_shuffles);
            builder.setPvalue_cutoff(p_value_cut_off);
            builder.setDo_not_include_if_greater_than_cutoff(results_greater_cut_off);
            try {
                ParesnipParams parameters = builder.build();
                module.setParameters(parameters);
                return true;
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Parameters");
                alert.setHeaderText("Invalid Parameters");
                alert.setContentText("Failed Parameters: " + e);
                alert.showAndWait();
                return false;
            }
        }

        public void saveCSV(String str, boolean includeFirstColumn) throws FileNotFoundException {
            saveDialogCSV(str, includeFirstColumn);
        }

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void setUpLog() {
            getEngine().executeScript("var log = new StatusLog('output')");
        }

        public void setUpParameters() {
            this.initialised = true;
            for (ToolParameters.ParameterDefinition paramDef : paramDefs) {
                String id = paramDef.getName();
                String name = id;
                Object defaultVal = paramDef.getDefaultValue();
                String type = "";
                if (defaultVal instanceof Double || defaultVal instanceof Float) {
                    type = "float";
                } else if (defaultVal instanceof Integer) {
                    type = "int";
                } else if (defaultVal instanceof Boolean) {
                    type = "checkbox";
                }
                getEngine().executeScript("parameterSet.addParameter(\"" + id + "\", \"" + name + "\", \"" + type + "\", " + defaultVal + ");");
            }
            getEngine().executeScript("parameterSet.displayParameters();");
            displayParameters();
        }

        // returns whether or not the parameters should be shown
        // this will be false when PAREsnip output file is input and algorithm does not need to be run but results displayed
        public boolean showParameters() {
            return module.runPAREsnip;
        }

        public void write2Log(String log) {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getEngine().executeScript("log.writeLine('" + log + "');");
                        } catch (Exception ex) {
                            Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

            } else {
                getEngine().executeScript("log.writeLine('" + log + "');");
            }
        }

        public void disableInputs() {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getEngine().executeScript("parameterSet.disableInputs();");
                        } catch (Exception ex) {
                            Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {
                getEngine().executeScript("parameterSet.disableInputs();");
            }
        }

    }
}
