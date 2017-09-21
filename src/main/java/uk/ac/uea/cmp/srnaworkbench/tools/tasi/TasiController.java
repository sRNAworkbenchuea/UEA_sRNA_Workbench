/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.util.logging.Level;
import java.util.logging.Logger;
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
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class TasiController extends Controller implements ControlledScreen {

    private TasiModule module;
    @FXML
    private WebView mainWebView;
    private JavaApplication javaApp;

    public TasiController(TasiModule module, Rectangle2D visualBounds) {
        this.module = module;
    }

    @Override
    protected void initWebFile() {
        setWebFile(IOUtils.DIR_SEPARATOR + "HTML" + IOUtils.DIR_SEPARATOR + "tasi.html");
    }

    @Override
    protected void initJSClass() {
        this.javaApp = new TasiController.JavaApplication();
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

    public void setModule(TasiModule module) {
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
            Logger.getLogger(TasiController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TasiController.class.getName()).log(Level.SEVERE, null, ex);
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



        public JavaApplication() {
          
        }

  

        public synchronized void updateUI() throws NotJavaFXThreadException, HQLQuery.HQLFormatException, NoSQLException, Exception {

        }

       
        

       
        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }

       
    }
}
