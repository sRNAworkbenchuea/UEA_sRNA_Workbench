/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat.FX;

import java.util.ArrayList;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModule;
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
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class miRCatController extends Controller implements ControlledScreen {

    private MiRCatModule module;
    private List<ToolParameters.ParameterDefinition> paramDefs;
    @FXML
    private WebView mainWebView;
    private JavaApplication jApp;

    public miRCatController(MiRCatModule module) {
        this.module = module;
        paramDefs = new ArrayList<>();
        paramDefs.add(MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MFE_THRESHOLD.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getDefinition());
        paramDefs.add(MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefinition());
        paramDefs.add(MiRCatParams.Definition.P_VAL_THRESHOLD.getDefinition());
        paramDefs.add(MiRCatParams.Definition.ALLOW_COMPLEX_LOOPS.getDefinition());
       
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "mircat.html");
    }

    @Override
    protected void initJSClass() {
        jApp = new miRCatController.JavaApplication();
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

    public void setModule(MiRCatModule module) {
        this.module = module;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {
        try {
            jApp.updateUI();
        } catch (NotJavaFXThreadException | NoSQLException | HQLQuery.HQLFormatException | HQLQuery.HQLQueryLockedException ex) {
            Logger.getLogger(miRCatController.class.getName()).log(Level.SEVERE, null, ex);
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

        private HQLDataTable table;
        private boolean initialised;

        public JavaApplication() {
            this.table = null;
            this.initialised = false;
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {
            if (this.table == null) {
                this.table = new HQLDataTable("predictions");
                this.table.addRightColumn(2);
                this.table.addRightColumn(3);
                this.table.addRightColumn(6);
                this.table.addRightColumn(8);
                this.table.addRightColumn(9);
                this.table.addRightColumn(11);
                this.table.addRightColumn(12);

            }

            if (module.isComplete()) {
                this.table.setQuery(module.generateOutput());
                this.table.update(getEngine());
            }

            displayParameters();
        }

        public void exception(String ex) {
            System.err.println("EXCEPTION: " + ex);
        }

        public boolean save(int flank, int min_energy, int min_srna_length, int max_srna_length, int max_genome_hits,
                int max_unpaired_bases_percent, int max_consecutive_gaps, int min_gc_percent, int min_srna_locus_size,
                int min_srna_orientation_percent, int min_hairpin_length, int min_srna_abundance, int loci_separation_distance,
                int min_paired_bases, int max_overlap_percent, float p_value, boolean complex_loops) {

            MiRCatParams.Builder builder = new MiRCatParams.Builder();
            builder.setExtend(flank);
            builder.setMinEnergy(min_energy);
            builder.setMinLength(min_srna_length);
            builder.setMaxLength(max_srna_length);
            builder.setMaxGenomeHits(max_genome_hits);
            builder.setOrientation(min_srna_orientation_percent);
            builder.setMinHairpinLength(min_hairpin_length);
            builder.setMinConsider(min_srna_abundance);
            builder.setClusterSentinel(loci_separation_distance);
            builder.setMinPaired(min_paired_bases);
            builder.setMaxOverlapPercentage(max_overlap_percent);
            builder.setPVal(p_value);
            builder.setComplexLoops(complex_loops);
            try {
                MiRCatParams parameters = builder.build();
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

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void refresh() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {
            this.table.setQuery(module.generateOutput());
            this.table.update(getEngine());
        }

        private void setParam(String id, String value) {
            getEngine().executeScript("setParamValue('" + id + "', '" + value + "');");
        }

        public void displayParameters() {
            if(!this.initialised)
                return;
            try {
                // id of html form elementmust be the same name as the parameter definition
                MiRCatParams parameters = module.getParameters();
                setParam(MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getName(), parameters.getClusterSentinel() + "");
                setParam(MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getName(), parameters.getExtend() + "");
                setParam(MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getName(), parameters.getMaxGaps() + "");
                setParam(MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getName(), parameters.getMaxGenomeHits() + "");
                setParam(MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getName(), parameters.getMaxLength() + "");
                setParam(MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getName(), parameters.getMaxOverlapPercentage() + "");
                setParam(MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getName(), parameters.getMaxUnpaired() + "");
                setParam(MiRCatParams.Definition.MFE_THRESHOLD.getName(), parameters.getMinEnergy() + "");
                setParam(MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getName(), parameters.getMinGC() + "");
                setParam(MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getName(), parameters.getMinHairpinLength() + "");
                setParam(MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getName(), parameters.getMinLocusSize() + "");
                setParam(MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getName(), parameters.getOrientation() + "");
                setParam(MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getName(), parameters.getMinPaired() + "");
                setParam(MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getName(), parameters.getMinConsider() + "");
                setParam(MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getName(), parameters.getMinLength() + "");
                setParam(MiRCatParams.Definition.P_VAL_THRESHOLD.getName(), parameters.getPVal() + "");
                getEngine().executeScript("document.getElementById(\"" + MiRCatParams.Definition.ALLOW_COMPLEX_LOOPS.getName() + "\").checked = " + parameters.getComplexLoops() + ";");

            } catch (Exception ex) {
                Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, "Was not able to update GUI for parameters for miRCat:{0}", ex.toString());
            }
        }

        public void setUpParameters() {
            this.initialised = true;
            getEngine().executeScript("var log = new StatusLog('output');");

            String script = "";
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
                script += "parameterSet.addParameter(\"" + id + "\", \"" + name + "\", \"" + type + "\", " + defaultVal + ");";
                //getEngine().executeScript("parameterSet.addParameter(\"" + id + "\", \"" + name + "\", \"" + type + "\", " + defaultVal + ");");
            }
            script += "parameterSet.displayParameters();";
            //getEngine().executeScript("parameterSet.displayParameters();");
            getEngine().executeScript(script);
            //displayParameters();

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
                getEngine().executeScript("log.write('" + log + "');");
            }
        }

        public void disableInputs() {
            if (!DatabaseWorkflowModule.getInstance().isDebugMode()) {
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
                }
            }
        }

        public void initController() {
            getEngine().executeScript("var log = new StatusLog('output');");
        }
    }
}
