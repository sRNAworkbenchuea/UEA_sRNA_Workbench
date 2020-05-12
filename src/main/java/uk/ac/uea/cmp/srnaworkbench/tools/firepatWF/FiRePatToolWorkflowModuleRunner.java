/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepatWF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author Josh Thody
 */
public class FiRePatToolWorkflowModuleRunner extends WorkflowRunner
{
     
     public FiRePatToolWorkflowModuleRunner(FiRePatToolWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof FiRePatToolWorkflowModule)
        {
            System.out.println("FiRePat returning: " + ((FiRePatToolWorkflowModule)engine).readyToContinue());
            return !((FiRePatToolWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
