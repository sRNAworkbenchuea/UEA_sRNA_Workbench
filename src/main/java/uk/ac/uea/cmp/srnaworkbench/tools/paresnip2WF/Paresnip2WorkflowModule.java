/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2WF;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2FX.Paresnip2SceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author Josh Thody
 */
public class Paresnip2WorkflowModule extends WorkflowModule
{
    
    public Paresnip2WorkflowModule(String id)
    {
         this(id, new Rectangle2D(200,200,200,200));
    }
    
    public Paresnip2WorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "PAREsnip 2");
        
        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/PAREsnip2Scene.fxml";

   

        this.controller = new Paresnip2SceneController(visualBounds, this);
        
    }

    private boolean readyToContinue;

    @Override
    protected void process() throws Exception
    {
        //Set to busy
        WorkflowSceneController.setBusyNode(this.getID());
        //Set the screen to the PAREsnip2 view
        ScreensController.getInstance().setScreen(this.getID());
        ((Paresnip2SceneController)controller).updateSceneRunning();
        
        //System.out.println("Running the tool");
        
        Engine engine = new Engine((Paresnip2SceneController)controller);
        
        readyToContinue = true;
        
        //WorkflowSceneController.setCompleteNode(this.getID());
    }

    public boolean readyToContinue()
    {
        return this.readyToContinue;
        
    }
    
}
