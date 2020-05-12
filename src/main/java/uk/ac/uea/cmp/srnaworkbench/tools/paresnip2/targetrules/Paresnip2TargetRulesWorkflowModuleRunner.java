/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.targetrules;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2TargetRulesWorkflowModuleRunner extends WorkflowRunner {
 
    
     public Paresnip2TargetRulesWorkflowModuleRunner(Paresnip2TargetRulesWorkflowModule engine)
    {
        super(engine);
    }
    
      @Override
    public boolean getActive()
    {

        if (this.engine instanceof Paresnip2TargetRulesWorkflowModule)
        {
            System.out.println("Paresnip 2 target rules returning: " + ((Paresnip2TargetRulesWorkflowModule)engine).readyToContinue());
            return !((Paresnip2TargetRulesWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
