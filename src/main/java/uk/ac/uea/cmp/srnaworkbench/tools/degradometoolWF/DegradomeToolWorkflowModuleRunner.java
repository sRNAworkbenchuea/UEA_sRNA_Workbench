/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradometoolWF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author w0445959
 */
public class DegradomeToolWorkflowModuleRunner extends WorkflowRunner
{
     
     public DegradomeToolWorkflowModuleRunner(DegradomeToolWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof DegradomeToolWorkflowModule)
        {
            System.out.println("file manager returning: " + ((DegradomeToolWorkflowModule)engine).readyToContinue());
            return !((DegradomeToolWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
