/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepatWF;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.tools.firepatFX.FiRePatToolSceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.*;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput.FiRePatConfiguration;

/**
 *
 * @author Josh Thody
 */
public class FiRePatToolWorkflowModule extends WorkflowModule
{
    
    public FiRePatToolWorkflowModule(String id)
    {
         this(id, new Rectangle2D(200,200,200,200));
    }
    
    public FiRePatToolWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "FiRePat");
        
        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/FiRePatScene.fxml";

   

        this.controller = new FiRePatToolSceneController(visualBounds, this);
        
    }

    private boolean readyToContinue;

    @Override
    protected void process() throws Exception
    {
         //Set to busy
        WorkflowSceneController.setBusyNode(this.getID());
        //Set the screen to the PAREsnip2 view
        ScreensController.getInstance().setScreen(this.getID());
    
        
        System.out.println("FiRePat: Running the tool");
        
        FiRePat fp = new FiRePat(); // create FiRePat object        
        fp.setAllParameters(); // load all parameters
        fp.loadData(); // load data from input files
        fp.preprocessData(); // filter/clean up input data
        fp.calculateCorrelations(); // get correlations
        fp.getDataForClustering();  // get subset of 'high quality' data for clustering
        fp.processDataForClustering(); // process this subset
        fp.writeOutputFiles(); // write required output to files
        
        readyToContinue = true;
    }

    public boolean readyToContinue()
    {
        return this.readyToContinue;
        
    }
    
}
