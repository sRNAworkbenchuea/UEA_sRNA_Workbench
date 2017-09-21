/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author w0445959
 */
public class FileManagerWorkflowModuleRunner extends WorkflowRunner
{
    
    public FileManagerWorkflowModuleRunner(FileManagerWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof FileManagerWorkflowModule)
        {
            System.out.println("file manager returning: " + ((FileManagerWorkflowModule)engine).readyToContinue());
            return !((FileManagerWorkflowModule)engine).readyToContinue();
        }
        return false;
    }


    
    
}
