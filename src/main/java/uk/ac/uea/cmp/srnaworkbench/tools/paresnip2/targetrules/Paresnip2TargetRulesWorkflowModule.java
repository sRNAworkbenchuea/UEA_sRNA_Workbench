/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.targetrules;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.*;
import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2TargetRulesWorkflowModule extends WorkflowModule {

    public Paresnip2TargetRulesWorkflowModule(String id) {
        this(id, new Rectangle2D(200, 200, 200, 200));
    }

    public Paresnip2TargetRulesWorkflowModule(String id, Rectangle2D visualBounds) {
        super(id, "Targeting Rules");

        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/PAREsnip2DataInputScene.fxml";

        this.controller = new Paresnip2TargetRulesSceneController(visualBounds, this);

    }

    private boolean readyToContinue;

    @Override
    protected synchronized void process() throws Exception {
        
        WorkflowSceneController.setWaitingNode(this.getID());
        
        if(!readyToContinue)
            this.wait();
        
        readyToContinue = true;
    }

    public boolean readyToContinue() {
        return this.readyToContinue;

    }
    
    public synchronized void setReadyToContinue()
    {
        readyToContinue = true;
        this.notifyAll();
        
    }

}
