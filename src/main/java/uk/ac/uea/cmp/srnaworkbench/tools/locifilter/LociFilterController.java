/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.locifilter;

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
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQLFormatException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQLQueryLockedException;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class LociFilterController extends Controller implements ControlledScreen {

    private LociFilterModule module;
    @FXML
    private WebView mainWebView;
    
    private JavaApplication javaApp;

    public LociFilterController(LociFilterModule module, Rectangle2D size) {
        this.module = module;
    }

    public void write2Log(String log) {
        javaApp.write2Log(log);
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "locifilter.html");
    }

    @Override
    protected void initJSClass() {
        this.javaApp = new LociFilterController.JavaApplication();
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

    public void setModule(LociFilterModule module) {
        this.module = module;
    }

    public void update() throws NotJavaFXThreadException, NoSQLException, HQLFormatException, HQLQueryLockedException {

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
        } catch (NotJavaFXThreadException | HQLFormatException | NoSQLException | HQLQueryLockedException ex) {
            Logger.getLogger(LociFilterController.class.getName()).log(Level.SEVERE, null, ex);
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

        private HQLDataTable table;

        public JavaApplication() {
            this.table = null;
        }

        public synchronized void updateUI() throws NotJavaFXThreadException, HQLQuery.HQLFormatException, NoSQLException, HQLQueryLockedException {
            if (this.table == null) {
                this.table = new HQLDataTable("predictions");
            }

            if (module.isComplete()) {
                this.table.setQuery(module.generateOutput());
                this.table.update(getEngine());
            }

        }

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
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

        public void initController() {
            getEngine().executeScript("var log = new StatusLog('output');");
        }
    }
}
