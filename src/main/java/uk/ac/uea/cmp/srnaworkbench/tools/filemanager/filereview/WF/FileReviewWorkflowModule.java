/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF;

import java.util.ArrayList;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.FX.FileReviewSceneController;
import java.util.List;
import javafx.geometry.Rectangle2D;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.FX.NormalisationController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author w0445959
 */
public class FileReviewWorkflowModule extends WorkflowModule
{
    private boolean readyToContinue = false;
    
    public FileReviewWorkflowModule(String id)
    {
        this(id, new Rectangle2D(200,200,200,200));
    }
    
    public FileReviewWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "File Review");
        
        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/FileReviewScene.fxml";

        this.controller = new FileReviewSceneController(visualBounds, this);
        
        
    }
    
    public boolean readyToContinue()
    {
        return readyToContinue;
    }
    
    public synchronized void setReadyToContinue(boolean newState)
    {
        readyToContinue = newState;
        if(readyToContinue)
        {

            
            this.notifyAll();
        }
    }
    
    public static List<String> getSelectedFiles()
    {
        return FileReviewSceneController.getSelectedFiles();
    }
    
    private void processRemovals(ArrayList<Integer> allRemovalFlags)
    {
        AlignedSequenceServiceImpl alignedService = (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");
//        
//        Integer[] removalArray = new Integer[allRemovalFlags.size()];
//        allRemovalFlags.toArray(removalArray);
        
        alignedService.deleteListBySizeClass(allRemovalFlags);
    }

    @Override
    protected synchronized void process() throws Exception
    {
        ((FileReviewSceneController)controller).setupTable();
        
        WorkflowSceneController.setWaitingNode(this.getID());
        
        //hold this thread util user hits continue...

        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
           

            this.wait();
        }
        else
            readyToContinue = true;
        WorkflowSceneController.setBusyNode(this.getID());
        
        //process the sequence filter
        ArrayList<Integer> allRemovalFlags = FileReviewSceneController.getRemovalFlags();
        processRemovals(allRemovalFlags);
    }

    
    
}
