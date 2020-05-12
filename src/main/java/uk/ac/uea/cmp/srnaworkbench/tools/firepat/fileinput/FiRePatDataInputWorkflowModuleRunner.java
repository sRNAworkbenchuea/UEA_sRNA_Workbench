/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author rew13hpu
 */
public class FiRePatDataInputWorkflowModuleRunner extends WorkflowRunner {
 
    
     public FiRePatDataInputWorkflowModuleRunner(FiRePatDataInputWorkflowModule engine)
    {
        super(engine);
    }
    
      @Override
    public boolean getActive()
    {

        if (this.engine instanceof FiRePatDataInputWorkflowModule)
        {
            System.out.println("FiRePat data input returning: " + ((FiRePatDataInputWorkflowModule)engine).readyToContinue());
            return !((FiRePatDataInputWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
