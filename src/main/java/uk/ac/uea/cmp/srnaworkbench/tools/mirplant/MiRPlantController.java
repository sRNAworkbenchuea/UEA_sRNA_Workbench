/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mirplant;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLDataTable;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class MiRPlantController extends Controller implements ControlledScreen {

    private MiRPlantModule module;
    @FXML
    private WebView mainWebView;
    private JavaApplication javaApp;

    public MiRPlantController(MiRPlantModule module) {
        this.module = module;
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "mirplant.html");
    }

    @Override
    protected void initJSClass() {
        this.javaApp = new MiRPlantController.JavaApplication();
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

    public void setModule(MiRPlantModule module) {
        this.module = module;
    }

    public void update() throws NotJavaFXThreadException, NoSQLException, HQLQuery.HQLFormatException {

    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {
        try {
            javaApp.updateUI();
        } catch (NotJavaFXThreadException | NoSQLException | HQLQuery.HQLFormatException | HQLQuery.HQLQueryLockedException ex) {
            Logger.getLogger(MiRPlantController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public JFXStatusTracker getStatusTracker() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener() {
        javaApp.disableInputs();
    }

    public class JavaApplication {

        private HQLDataTable table;

        public JavaApplication() {
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
        }

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

        public void disableInputs()
        {
            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getEngine().executeScript("disableInputs();");
                        } catch (Exception ex) {
                            Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        }
        
        public void save(String path) {
            module.setPath(new File(path).toPath());
        }
        /*
         * @param str: csv string where each row is separated by \n
         */

        public void browseFile() throws FileNotFoundException {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load File");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("miRPlant Result File", "*.result"));
            File file = fileChooser.showOpenDialog(scene.getWindow());
            if (file != null) {
                getEngine().getDocument().getElementById("path").setAttribute("value", file.getName());     
            }
            save(file.getAbsolutePath());
        }

    }
}
