/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.parefirst.paresnip2;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author salmayz
 */
public class Paresnip2WithinPAREfirstWorkflowModule extends WorkflowModule{
    
    public static Paresnip2WithinPAREfirstWorkflowModule instance;
    static boolean isSetUp;
    boolean readyToContinue;
    //boolean isComplete = false;

    public static void setUp(Rectangle2D visualBounds) throws WorkflowException {
        if (instance == null) {
            instance = new Paresnip2WithinPAREfirstWorkflowModule("PAREsnip2Settings", visualBounds);
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

    public static Paresnip2WithinPAREfirstWorkflowModule getInstance() throws WorkflowException {
        if (isSetUp) {
            return instance;
        }
        throw new WorkflowException("PAREsnip2 within miRPARE exception", new Exception());
    }

    private Paresnip2WithinPAREfirstWorkflowModule(String id) {
        this(id, new Rectangle2D(200, 200, 200, 200));
    }

    public Paresnip2WithinPAREfirstWorkflowModule(String id, Rectangle2D visualBounds) {
        super(id, "PAREsnip2 Settings");

        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/PAREsnip2DataInputScene.fxml";

        this.controller = new Paresnip2WithinPAREfirstSceneController(visualBounds, this);

    }

    @Override
    protected synchronized void process() throws Exception {
        WorkflowSceneController.setWaitingNode(this.getID());
        if(!AppUtils.INSTANCE.isCommandLine()){
            if(!readyToContinue)
                this.wait();
        }

        //Set to busy
        WorkflowSceneController.setBusyNode(this.getID());
                
        Engine engine = new Engine();

    }

    public boolean readyToContinue() {

        return readyToContinue;

    }
    
    public synchronized void setReadyToContinue()
    {
        readyToContinue = true;
        this.notifyAll();
    }
    
//    public boolean isComplete(){
//        return isComplete;
//    }
}
