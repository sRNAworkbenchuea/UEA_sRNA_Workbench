/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradometoolWF;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.tools.degradometoolFX.DegradomeToolSceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author w0445959
 */
public class DegradomeToolWorkflowModule extends WorkflowModule
{
    
    public DegradomeToolWorkflowModule(String id)
    {
         this(id, new Rectangle2D(200,200,200,200));
    }
    
    public DegradomeToolWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "Degradome Tool");
        
        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/DegradomeToolScene.fxml";

   

        this.controller = new DegradomeToolSceneController(visualBounds, this);
        
    }

    private boolean readyToContinue;

    @Override
    protected void process() throws Exception
    {
        readyToContinue = true;
    }

    public boolean readyToContinue()
    {
        return this.readyToContinue;
        
    }
    
}
