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
public class DefaultToolWorkflowModuleRunner extends WorkflowRunner
{
     
     public DefaultToolWorkflowModuleRunner(DefaultToolWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof DefaultToolWorkflowModule)
        {
            System.out.println("file manager returning: " + ((DefaultToolWorkflowModule)engine).readyToContinue());
            return !((DefaultToolWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
