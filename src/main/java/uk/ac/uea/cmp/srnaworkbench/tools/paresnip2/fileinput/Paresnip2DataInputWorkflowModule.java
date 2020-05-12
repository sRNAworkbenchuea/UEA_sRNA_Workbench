/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2DataInputWorkflowModule extends WorkflowModule {

    public static Paresnip2DataInputWorkflowModule instance;
    static boolean isSetUp;
    boolean readyToContinue;

    public static void setUp(Rectangle2D visualBounds) throws WorkflowException {
        if (instance == null) {
            instance = new Paresnip2DataInputWorkflowModule("DataInput", visualBounds);
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

    public static Paresnip2DataInputWorkflowModule getInstance() throws WorkflowException {
        if (isSetUp) {
            return instance;
        }
        throw new WorkflowException("PAREsnip2 data input exception", new Exception());
    }

    private Paresnip2DataInputWorkflowModule(String id) {
        this(id, new Rectangle2D(200, 200, 200, 200));
    }

    public Paresnip2DataInputWorkflowModule(String id, Rectangle2D visualBounds) {
        super(id, "Data Input");

        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/PAREsnip2DataInputScene.fxml";

        this.controller = new Paresnip2DataInputSceneController(visualBounds, this);

    }

    @Override
    protected void process() throws Exception {

    }

    public boolean readyToContinue() {

        return readyToContinue;

    }
    
    public void setReadyToContinue()
    {
        readyToContinue = true;
    }

}
