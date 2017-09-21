/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.targetprediction.FX;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.tools.targetprediction.WF.TargetPredictionWorkflowModule;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class TargetPredictionSceneController implements Initializable, ControlledScreen
{
    private final TargetPredictionWorkflowModule module;
    private final Rectangle2D size;

    public TargetPredictionSceneController(TargetPredictionWorkflowModule myModule,  Rectangle2D size)
    {
        
        this.module = myModule;
        this.size = size;
        
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void workflowStartedListener()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class TPJavascriptReceiver extends JavascriptBridge
    {

        public TPJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }

    }

    
}
