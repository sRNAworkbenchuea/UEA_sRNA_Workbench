/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.srnaConservation;

import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import java.io.FileNotFoundException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.web.WebView;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class ConservationController extends Controller implements ControlledScreen {

    private JavaApplication jApp;

    public ConservationController(ConservationModule module) {
        this.module = module;
    }
    // module (model)
    private ConservationModule module;
    @FXML
    private WebView mainWebView;

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "conservation.html");
    }

    @Override
    protected void initJSClass() {
        jApp = new ConservationController.JavaApplication();
        setJSClass(jApp);
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

    public void setModule(ConservationModule module) {
        this.module = module;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {
        try {
            jApp.updateUI();
        } catch (NotJavaFXThreadException | NoSQLException | HQLQuery.HQLFormatException ex) {
            Logger.getLogger(ConservationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConservationController.class.getName()).log(Level.SEVERE, null, ex);
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

    public void write2Log(String log) {
        jApp.write2Log(log);
    }

    public class JavaApplication {

        //private WFQueryResultsTable2 table;
        private HQLDataTable table;

        public JavaApplication() {
            // table = null;
            table = new HQLDataTable("conserved");
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, Exception {
            if (table == null) {
                table = new HQLDataTable("conserved");
            }
            if (module.isComplete()) {
                table.setQuery(module.generateOutput());
                table.update(getEngine());
            }
        }

        /*public void selectHeader(String id, int index) throws NotJavaFXThreadException, NoSQLException {
         this.table.toggleOrder(index);
         this.table.updateTable(getEngine());
         }*/
//        public void updateUI() {
//
//            if (this.table == null) {
//               // this.table = new WFQueryResultsTable("conserved", "CONSERVED sRNAs:", getEngine(), 10);
//            }
//            if (module != null) {
//
//                // String msg = module.getMessage();
//                //if (!msg.equals("")) {
//                //    print(msg);
//                // }
//                updateProgressBar();
//
//                if (module.isComplete()) {
//                    if (this.needsUpdating) {
//                        needsUpdating = false;
//                        //getEngine().executeScript("showLoader('" + this.table.getID() + "');");
//                        Thread thread = new Thread() {
//                            @Override
//                            public void run() {
//                                table.setSQL(module.generateOutput());
//                                table.update();
//                                Platform.runLater(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        updateUI();
//                                    }
//                                });
//                            }
//                        };
//                        thread.start();
//                    }
//                    if (table.needsRedrawing) {
//                        this.table.needsRedrawing = false;
//                        table.remove();
//                        table.show();
//
//                        getEngine().executeScript("hideLoader('" + this.table.getID() + "');");
//
//                    }
//                }
//            }
//        }
//
//        private void updateProgressBar() {
//            int progress = (int) module.getProgress();
//            getEngine().executeScript("updateProgressBar(" + progress + ")");
//        }
//
        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void saveCSV(String str, boolean includeFirstColumn) throws FileNotFoundException {
            saveDialogCSV(str, includeFirstColumn);
        }
//
//        public void previousPage(String id) {
//            this.table.previousPage();
//            this.needsUpdating = true;
//            updateUI();
//        }
//
//        public void nextPage(String id) {
//            this.table.nextPage();
//            this.needsUpdating = true;
//            updateUI();
//        }
//
//        public void firstPage(String id) {
//            this.table.firstPage();
//            this.needsUpdating = true;
//            updateUI();
//        }
//
//        public void lastPage(String id) {
//            this.table.lastPage();
//            this.needsUpdating = true;
//            updateUI();
//        }
//
//        public void setSearchStr(String str) {
//            this.searchStr = str;
//            this.needsUpdating = true;
//            this.table.firstPage();
//            this.table.setHighlightText(str);
//            updateUI();
//        }
//
//        public void sortBy(String column_name) {
//            this.table.sortBy(column_name);
//            this.needsUpdating = true;
//            this.table.firstPage();
//            updateUI();
//        }
//    }

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

        public void initController() {
            getEngine().executeScript("var log = new StatusLog('output');");
        }
    }
}
