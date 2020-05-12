/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author rew13hpu
 */
public class FiRePatDataInputWorkflowModule extends WorkflowModule {

    public static FiRePatDataInputWorkflowModule instance;
    static boolean isSetUp;
    boolean readyToContinue;

    public static void setUp(Rectangle2D visualBounds) throws WorkflowException {
        if (instance == null) {
            instance = new FiRePatDataInputWorkflowModule("DataInput", visualBounds);
            isSetUp = true;
        }

    }
    private Rectangle2D bounds;
    
    public boolean isReady()
    {
        if(readyToContinue())
        {
            return true;
        }
        
        return false;
    }

    public static FiRePatDataInputWorkflowModule getInstance() throws WorkflowException {
        if (isSetUp) {
            return instance;
        }
        return null;
    }

    private FiRePatDataInputWorkflowModule(String id) {
        this(id, new Rectangle2D(200, 200, 200, 200));
    }

    private FiRePatDataInputWorkflowModule(String id, Rectangle2D visualBounds) {
        super(id, "Data Input");

        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/FiRePatDataInputScene.fxml";

        this.controller = new FiRePatDataInputSceneController(visualBounds, this);

    }

    @Override
    protected void process() throws Exception {
     readyToContinue = true;
    }

    public boolean readyToContinue() {

        return readyToContinue;

    }
    
    public void setReadyToContinue()
    {
        readyToContinue = true;
    }

}
