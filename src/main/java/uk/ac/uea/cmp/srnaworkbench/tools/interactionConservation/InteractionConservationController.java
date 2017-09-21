/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.interactionConservation;

import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import java.io.FileNotFoundException;
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
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class InteractionConservationController extends Controller implements ControlledScreen {

    // module (model)
    private InteractionConservationModule module;
    @FXML
    private WebView mainWebView;
    private JavaApplication jApp;

    public InteractionConservationController(InteractionConservationModule module) {
        this.module = module;
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "conservation.html");
    }

    @Override
    protected void initJSClass() {
        jApp = new InteractionConservationController.JavaApplication();
        setJSClass(jApp);
    }

    @Override
    protected WebView getWebView() {
        return this.mainWebView;
    }
    /*
     * FXML injectable handlers start.
     */

    public void write2Log(String log) {
        jApp.write2Log(log);
    }

    @FXML
    private void goToMain(ActionEvent event) {
        getScreensController().setScreen(WorkflowViewer.MAIN_SCREEN);
    }
    /*
     * FXML injectable handlers end.
     */

    public void setModule(InteractionConservationModule module) {
        this.module = module;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {

        try {
            jApp.updateUI();
        } catch (NotJavaFXThreadException | NoSQLException | HQLQuery.HQLFormatException ex) {
            Logger.getLogger(InteractionConservationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(InteractionConservationController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class JavaApplication {

        private HQLDataTable table;

        public JavaApplication() {
            this.table = null;

        }

        public void saveCSV(String str, boolean includeFirstColumn) throws FileNotFoundException {
            saveDialogCSV(str, includeFirstColumn);
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException, Exception {
            if (this.table == null) {
                this.table = new HQLDataTable("conserved");
                this.table.setPreTagCol("srna");

            }

            if (module.isComplete()) {
                // how many files are there
                int numInteractionInputs = module.getNumInteractionInputs();
                this.table.addRightColumn(2);
                for (int col = 4; col <= numInteractionInputs; col++) {
                    this.table.addRightColumn(col);
                    System.out.println("setting right coloumn: " + col);
                }
                this.table.setQuery(module.generateOutput());
                this.table.update(getEngine());
            }
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

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void initController() {
            getEngine().executeScript("var log = new StatusLog('output');");
        }

    }
}
