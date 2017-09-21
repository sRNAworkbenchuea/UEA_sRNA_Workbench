/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filter2WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author w0445959
 */
public class Filter2WorkflowModuleRunner extends WorkflowRunner
{
    
     public Filter2WorkflowModuleRunner(Filter2WorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof Filter2WorkflowModule)
        {
            System.out.println("file manager returning: " + ((Filter2WorkflowModule)engine).readyToContinue());
            return !((Filter2WorkflowModule)engine).readyToContinue();
        }
        return false;
    }


    
}
