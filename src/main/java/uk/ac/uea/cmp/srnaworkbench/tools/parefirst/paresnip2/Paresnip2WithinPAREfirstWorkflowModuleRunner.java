/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.parefirst.paresnip2;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author salmayz
 */
public class Paresnip2WithinPAREfirstWorkflowModuleRunner extends WorkflowRunner{
    public Paresnip2WithinPAREfirstWorkflowModuleRunner(Paresnip2WithinPAREfirstWorkflowModule engine)
    {
        super(engine);
    }
    
      @Override
    public boolean getActive()
    {

        if (this.engine instanceof Paresnip2WithinPAREfirstWorkflowModule)
        {
            System.out.println("Paresnip 2 within miRPare returning: " + ((Paresnip2WithinPAREfirstWorkflowModule)engine).readyToContinue());
            return !((Paresnip2WithinPAREfirstWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
