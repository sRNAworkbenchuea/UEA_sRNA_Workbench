/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author w0445959
 */
public class FileReviewWorkflowModuleRunner extends WorkflowRunner
{
    
    public FileReviewWorkflowModuleRunner(FileReviewWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof FileReviewWorkflowModule)
        {
            System.out.println("file review returning: " + ((FileReviewWorkflowModule)engine).readyToContinue());
            return !((FileReviewWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
    
}
