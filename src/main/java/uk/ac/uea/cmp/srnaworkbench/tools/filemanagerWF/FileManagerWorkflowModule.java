/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF;

import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author w0445959
 */
public class FileManagerWorkflowModule extends WorkflowModule
{
    private boolean readyToContinue = false;

    public FileManagerWorkflowModule(String id)
    {
        this(id, new Rectangle2D(200,200,200,200));
    }
    
    public FileManagerWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "File Manager");
        
        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/FileHierarchyView.fxml";

        //this.globalStopWatch.start();

        this.controller = new FileHierarchyViewController(visualBounds, this);
        ((FileHierarchyViewController)controller).setEntryPoint(FileHierarchyViewController.EntryPoint.FILEMANAGER);
        
    }
    @Override
    protected synchronized void process() throws Exception
    {
//        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
//        {
//
//            this.wait();
//        }
//        else
//        {
            readyToContinue = true;
        //}
        WorkflowSceneController.setBusyNode(this.getID());
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
    
}
