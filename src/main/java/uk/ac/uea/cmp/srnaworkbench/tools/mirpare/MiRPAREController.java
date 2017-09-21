/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mirpare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.InteractionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import static uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable.FIELD_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.PdfHelper;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class MiRPAREController extends Controller implements ControlledScreen {

    private MiRPAREModule module;
    @FXML
    private WebView mainWebView;
    private JavaApplication javaApp;

    public MiRPAREController(MiRPAREModule module) {
        this.module = module;
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "mirpare.html");
    }

    @Override
    protected void initJSClass() {
        this.javaApp = new MiRPAREController.JavaApplication();
        setJSClass(this.javaApp);
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

    public void setModule(MiRPAREModule module) {
        this.module = module;
    }

    public void update() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, Exception {

        this.javaApp.updateUI();
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {
        try {
            javaApp.updateUI();
        } catch (NotJavaFXThreadException | HQLQuery.HQLFormatException | NoSQLException ex) {
            Logger.getLogger(MiRPAREController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MiRPAREController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener() {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class JavaApplication {

        private HQLDataTable parent_table;
        private HQLDataTable interactions_table;
        private HQLDataTable predictions_table;
        private String transcriptID;
        private String predictorID;

        public JavaApplication() {
            this.parent_table = null;
            this.interactions_table = null;
            this.predictions_table = null;
        }

        private HQLDataTable getTablePointer(String id) {

            switch (id) {
                case "parent": {
                    return parent_table;
                }
                case "interactions": {
                    return interactions_table;
                }
                case "predictions": {
                    return predictions_table;
                }
            }
            return null;
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, HQLQuery.HQLFormatException, NoSQLException, Exception {
            String lengthMenu = "[1,2,3,4,5,6,7,8,9,10]";
            if (this.parent_table == null) {
                this.parent_table = new HQLDataTable("parent");
                this.parent_table.setSelectableFunction("function(data){app.setSelectedsRNA(data);}");
                this.parent_table.addRightColumn(2);
                //this.parent_table.addRightColumn(3);
                ///this.parent_table.addRightColumn(4);
                //this.parent_table.addRightColumn(5);

            }
            if (this.interactions_table == null) {
                this.interactions_table = new HQLDataTable("interactions");
                this.interactions_table.setSelectableFunction("function(data){app.setSelectedInteraction(data);}");
                this.interactions_table.addInitialisationFlag("lengthMenu", lengthMenu);
                this.interactions_table.addInitialisationFlag("pageLength", "4");
                this.interactions_table.addInitialisationFlag("bScrollCollapse", "false");
                this.interactions_table.addInitialisationFlag("scrollCollapse", "false");
                this.interactions_table.addInitialisationFlag("paging", "true");
                this.interactions_table.setPreTagCol("Duplex");
                this.interactions_table.addRightColumn(2);
                this.interactions_table.addRightColumn(3);
                //this.interactions_table.addRightColumn(5);
                this.interactions_table.addRightColumn(6);
                this.interactions_table.addRightColumn(7);
                this.interactions_table.addRightColumn(8);
                this.interactions_table.addRightColumn(9);

            }
            if (this.predictions_table == null) {

                this.predictions_table = new HQLDataTable("predictions");
                this.predictions_table.addInitialisationFlag("lengthMenu", lengthMenu);
                this.predictions_table.addInitialisationFlag("pageLength", "4");
                this.predictions_table.setSelectableFunction("function(data){app.setSelectedHairpin(data);}");
                this.predictions_table.setPreTagCol("srna");
                this.predictions_table.addRightColumn(1);
                this.predictions_table.addRightColumn(3);
                this.predictions_table.addRightColumn(4);
                this.predictions_table.addRightColumn(7);
                this.predictions_table.addRightColumn(9);
                this.predictions_table.addRightColumn(10);
                this.predictions_table.addRightColumn(12);
                this.predictions_table.addRightColumn(13);
            }

            if (module.isComplete()) {

                // get a string that represents
                /*String summaryStr = module.getSummary();
                 if (!summaryStr.isEmpty()) {
                 getEngine().executeScript(summaryStr);
                 }*/
                this.parent_table.setQuery(module.generateOutput());

                int nSamples = module.filenames.size();
                int nDeg = module.nDeg;
                // generate thead
                String row1 = "<tr><th rowspan='2'></th><th rowspan='2'>sRNA</th><th rowspan='2'>Annotation</th>";
                for (int i = 0; i < module.normTypes.size(); i++) {
                    if (module.normTypes.get(i) == NormalisationType.NONE) {
                        row1 += "<th colspan='" + nSamples + "'>" + module.normTypes.get(i).getFullName() + "</th>";
                    }
                }

                row1 += "<th colspan='" + nDeg + "'>Strongest Cat</th></tr>";

                String row2 = "<tr>";
                for (int j = 0; j < module.normTypes.size(); j++) {
                    if (module.normTypes.get(j) == NormalisationType.NONE) {
                        for (int i = 0; i < nSamples; i++) {
                            row2 += "<th>S" + (i + 1) + "</th>";
                        }
                    }
                }

                for (int i = 0; i < nDeg; i++) {
                    row2 += "<th>D" + (i + 1) + "</th>";
                }
                row2 += "</tr>";
                //  row2 += "<th>Cat Score</th></tr>";

                this.parent_table.setTableHeader(row1 + row2);

                System.out.println("updating tables");
                this.parent_table.update(getEngine());
                this.interactions_table.update(getEngine());
                this.predictions_table.update(getEngine());
                System.out.println("complete");
            }
        }

        public void setSelectedsRNA(String dataRow) throws HQLQuery.HQLFormatException, NotJavaFXThreadException, NoSQLException, HQLQuery.HQLQueryLockedException {
            if (dataRow.isEmpty()) {
                System.err.println("ERROR: no data in selected row");
                return;
            }
            String[] fields = dataRow.split(FIELD_SEPARATOR);
            String srna = fields[1];
            System.out.println("selected sRNA: " + srna);
            if (module.isComplete()) {

                getEngine().executeScript("clearHairpin('hp_svg');");

                this.interactions_table.setQuery(module.generateFunctionalOutput(srna));
                this.predictions_table.setQuery(module.generatePredictionOutput(srna));
                this.interactions_table.update(getEngine());
                this.predictions_table.update(getEngine());
            }
        }

        public void setSelectedInteraction(String dataRow) throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {
            if (dataRow.isEmpty()) {
                System.err.println("ERROR: no data in selected row");
                return;
            }
            String[] fields = dataRow.split(FIELD_SEPARATOR);
            this.transcriptID = fields[1];

            List<String> predictors = new ArrayList<>();
            HQLQueryComplex query = new HQLQueryComplex();
            query.addSelect("I.predictor", "predictor");
            query.addFrom(Interaction_Entity.class, "I");
            query.addWhere("I.gene.id = '" + this.transcriptID + "'");
            query.addGroup("I.predictor");
            InteractionServiceImpl interaction_service = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");
            List<Map<String, Object>> results = interaction_service.executeGenericSQL(query.eval());
            for (Map<String, Object> result : results) {
                String predictor = (String) result.get("predictor");
                predictors.add(predictor);
            }
            getEngine().executeScript("clearDropdown();");
            getEngine().executeScript("addToDropdown('ResultVisualiser', 'All');");
            for (String predictor : predictors) {
                getEngine().executeScript("addToDropdown('" + predictor + "', '" + predictor + "');");
            }

            getEngine().executeScript("selectDropdownOption(0);");
            this.predictorID = "ResultVisualiser";//fields[11];
            showTPlot();
        }

        public void exportSVG(String svg, int width, int height) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export File to SVG");
                File file = fileChooser.showSaveDialog(scene.getWindow());
                if (file != null) {
                    Files.write(Paths.get(file.toString() + ".svg"), svg.getBytes("UTF-8"));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Workbench Information");
                    alert.setHeaderText("Workflow Status Message");
                    alert.setContentText("Error saving SVG.");
                    alert.showAndWait();
                });
            }
        }

        public void setPredictor(String predictor) {
            this.predictorID = predictor;
            System.out.println("selected degradome:" + predictorID);
            showTPlot();
        }

        public void showTPlot() {
            File file = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + transcriptID + "_" + predictorID + "_tplot.json");
            if (file.exists()) {
                String str = "render_tplot('" + file + "', 'tplot_svg');";
                getEngine().executeScript(str);
            } else {
                System.err.println("Could not find tplot file");
            }
        }

        public void setSelectedHairpin(String dataRow) {
            if (dataRow.isEmpty()) {
                System.err.println("ERROR: no data in selected row");
                return;
            }
            String[] fields = dataRow.split(FIELD_SEPARATOR);
            // note: indexes include row number column
            Long predictionID = Long.parseLong(fields[1]);
            int mature_startIndex = Integer.parseInt(fields[9]);
            int mature_endIndex = Integer.parseInt(fields[10]);
            int star_startIndex = Integer.parseInt(fields[12]);
            int star_endIndex = Integer.parseInt(fields[13]);
            int hpStart = Integer.parseInt(fields[3]);
            int hpEnd = Integer.parseInt(fields[4]);
            String strand = fields[5];
            // get mature position (assume + strand)
            int mature_sIndex = mature_startIndex - hpStart;
            int mature_eIndex = mature_endIndex - hpStart;
            // get star position (assume - strand)
            int star_sIndex = -1;
            int star_eIndex = -1;
            if (star_startIndex != -1 && star_endIndex != -1) {
                star_sIndex = star_startIndex - hpStart;
                star_eIndex = star_endIndex - hpStart;
            }
            // correct positions if - strand
            if (strand.equals("-")) {
                mature_sIndex = hpEnd - mature_endIndex;
                mature_eIndex = hpEnd - mature_startIndex;
                if (star_startIndex != -1 && star_endIndex != -1) {
                    star_sIndex = hpEnd - star_endIndex;
                    star_eIndex = hpEnd - star_startIndex;
                }
            }

            if (module.isComplete()) {
                String hpID = Tools.miRPARE_DATA_Path + DIR_SEPARATOR + "hp_" + predictionID + ".xrna";
                File xrna_file = new File(hpID);
                if (xrna_file.exists()) {
                    String hpStr = "renderHairpin('" + xrna_file + "', 'hp_svg', " + mature_sIndex + ", " + mature_eIndex + ", " + star_sIndex + ", " + star_eIndex + ")";
                    getEngine().executeScript(hpStr);
                } else {
                    System.err.println("Could not find xrna file");
                }
            }
        }

        /* public synchronized void setSelected(String id, int index) throws NotJavaFXThreadException, HQLQuery.HQLFormatException {

         if (this.parent_table == null) {
         this.parent_table = new WFQueryResultsTable("parent");
         }
         if (this.interactions_table == null) {
         this.interactions_table = new WFQueryResultsTable("interactions");
         }
         if (this.predictions_table == null) {
         this.predictions_table = new WFQueryResultsTable("predictions");
         }

         if (module.isComplete()) {
         switch (id) {
         case "parent": {
         String srna = (String) this.parent_table.getOutput(index, "sRNA");
         this.interactions_table.setSQL(module.generateFunctionalOutput(srna));
         this.predictions_table.setSQL(module.generatePredictionOutput(srna));
         updateUI();
         break;
         }
         case "predictions": {
         Long predictionID = (Long) this.predictions_table.getOutput(index, "id");
         int mature_startIndex = (int) this.predictions_table.getOutput(index, "matureStart");
         int mature_endIndex = (int) this.predictions_table.getOutput(index, "matureEnd");
         int star_startIndex = (int) this.predictions_table.getOutput(index, "starStart");
         int star_endIndex = (int) this.predictions_table.getOutput(index, "starEnd");
         int hpStart = (int) this.predictions_table.getOutput(index, "start");
         int mature_sIndex = mature_startIndex - hpStart;
         int mature_eIndex = mature_endIndex - hpStart;
         int star_sIndex = -1;
         int star_eIndex = -1;
         if (star_sIndex != 0 && star_eIndex != 0) {
         star_sIndex = star_startIndex - hpStart;
         star_eIndex = star_endIndex - hpStart;
         }

         String hpID = Tools.miRPARE_DATA_Path + DIR_SEPARATOR + "hp_" + predictionID + ".xrna";
         File xrna_file = new File(hpID);
         if (xrna_file.exists()) {
         getEngine().executeScript("renderHairpin('" + xrna_file + "', 'hp_svg', " + mature_sIndex + ", " + mature_eIndex + ", " + star_sIndex + ", " + star_eIndex + ")");
         } else {
         System.err.println("Could not find xrna file");
         }
         }
         break;
         }
         }

         }

         public void selectHeader(String id, int index) throws NotJavaFXThreadException, NoSQLException {
         WFQueryResultsTable table = null;
         switch (id) {
         case "parent": {
         table = this.parent_table;
         break;
         }
         case "interactions": {
         table = this.interactions_table;
         break;
         }
         case "predictions": {
         table = this.predictions_table;
         break;
         }
         }
         if (table != null) {
         table.toggleOrder(index);
         }

         }*/
        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void saveJSON() throws FileNotFoundException, Exception {
            // save everything in json format
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName("data.json");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON", "*.json"));
            File file = fileChooser.showSaveDialog(scene.getWindow());
            if (file != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.getButtonTypes().clear();
                alert.setTitle("Workbench Information");
                alert.setHeaderText("Please Wait...");
                alert.setContentText("The file is saving. Please wait!...");
                alert.show();
                final Task<Integer> task = new Task<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        try {
                            module.saveJSON(file);
                        } catch (Exception ex) {
                            Logger.getLogger(MiRPAREController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return 0;
                    }
                };
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        alert.setTitle("Workbench Information");
                        alert.setHeaderText("Output Saved!");
                        alert.setContentText("The output has been saved to file, press to continue");
                        alert.getButtonTypes().add(ButtonType.OK);
                        alert.hide();
                        alert.show();
                    }
                });
                Thread t = new Thread(task);
                t.setDaemon(true); // thread will not prevent application shutdown
                t.start(); // start the thread
            }
        }
    }
}
