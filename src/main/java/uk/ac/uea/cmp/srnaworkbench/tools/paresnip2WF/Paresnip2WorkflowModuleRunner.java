/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author Josh Thody
 */
public class Paresnip2WorkflowModuleRunner extends WorkflowRunner
{
     
     public Paresnip2WorkflowModuleRunner(Paresnip2WorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof Paresnip2WorkflowModule)
        {
            System.out.println("PAREsnip 2 returning: " + ((Paresnip2WorkflowModule)engine).readyToContinue());
            return !((Paresnip2WorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
