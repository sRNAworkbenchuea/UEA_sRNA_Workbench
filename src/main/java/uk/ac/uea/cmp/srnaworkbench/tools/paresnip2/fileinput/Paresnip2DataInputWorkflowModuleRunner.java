/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2DataInputWorkflowModuleRunner extends WorkflowRunner {
 
    
     public Paresnip2DataInputWorkflowModuleRunner(Paresnip2DataInputWorkflowModule engine)
    {
        super(engine);
    }
    
      @Override
    public boolean getActive()
    {

        if (this.engine instanceof Paresnip2DataInputWorkflowModule)
        {
            System.out.println("Paresnip 2 data input returning: " + ((Paresnip2DataInputWorkflowModule)engine).readyToContinue());
            return !((Paresnip2DataInputWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
