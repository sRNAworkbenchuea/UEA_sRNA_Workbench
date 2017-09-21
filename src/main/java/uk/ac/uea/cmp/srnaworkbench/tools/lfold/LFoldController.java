/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.lfold;

import java.io.FileNotFoundException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import uk.ac.uea.cmp.srnaworkbench.workflow.Controller;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;

/**
 * FXML Controller class
 *
 * @author Chris Applegate
 */
public class LFoldController extends Controller implements ControlledScreen {

    private LFoldModule module;
    @FXML
    private WebView mainWebView;

    public LFoldController(LFoldModule module)
    {
        this.module = module;
    }
    
    @Override
    protected void initWebFile() {
        setWebFile("/HTML/lfold.html");
    }

    @Override
    protected void initJSClass() {
        setJSClass(new LFoldController.JavaApplication());
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

    public void setModule(LFoldModule module) {
        this.module = module;
    }

    @Override
    public void setScreenParent(ScreensController screenPage) {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateUI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

        private int currentPrediction;
        private boolean needsUpdating;

        public JavaApplication() {
            this.currentPrediction = 0;
            this.needsUpdating = true;
        }

        public void updateUI() {

            if (module != null) {

               // String msg = module.getMessage();
              ///  if (!msg.equals("")) {
                 //   print(msg);
                //}

                
                try {
                    updatePredictionTbl();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }

        }

        private void updatePredictionTbl() throws FileNotFoundException {
           // if (module.isComplete() && needsUpdating) {
            //    this.needsUpdating = false;
//
  //              outputTable("predictions", module.generateOutput());
    //        }
        }

        /*  public void updateUI() {

         if(WorkflowManager.getInstance().isRunning())
         {
         getEngine().executeScript("disableParameters()");
         }
            
         if (module.getViewer().isVisible()) {
         if (module != null) {
         String msg = module.getMessage();
         if (!msg.equals("")) {
         print(msg);
         }

         // update progress bar
         int progress = (int) module.getProgress();
         getEngine().executeScript("updateProgressBar(" + progress + ")");

         msg = genSequencePrecursors(currentPrediction);
         if (!msg.equals("")) {
         getEngine().executeScript("printPredictions('" + msg + "')");
         }

         if (!module.isPredictionComplete(currentPrediction)) {
         getEngine().executeScript("printPredictionMessage('STATUS: Computing precursors...')");
         } else {
         getEngine().executeScript("printPredictionMessage('STATUS: Predictions complete.')");
         }

         getEngine().executeScript("setRecordID(" + this.currentPrediction + ", " + module.getNumInputSequences() + ")");
         }
         }

         }*/
       

        private String genSequencePrecursors(int seqNum) {
            int counter = 0;
            String str = "<tr><th>#</th><th>Chr</th><th>sIndex</th><th>eIndex</th><th>Strand</th><th>Sequence/Structure</th></tr>";
            List<Prediction_Entity> predictions = module.getPredictions(seqNum);
            for (Prediction_Entity p : predictions) {
                str += String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", ++counter, p.getChromosome().substring(0, 4), p.getSIndex() + "", p.getEIndex() + "", p.getStrand(), p.toWebString());
            }

            return str;
        }

        public void next() {
            currentPrediction++;
            currentPrediction = Math.min(module.getNumInputSequences(), currentPrediction);
        }

        public void previous() {
            currentPrediction--;
            currentPrediction = Math.max(0, currentPrediction);
        }

        public void save(int extend) {
            module.setExtend(extend);
        }

        public void goHome() {
            ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        }
    }

}
