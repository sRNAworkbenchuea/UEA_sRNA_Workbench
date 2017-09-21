/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author Chris Applegate
 */
public class MiRCatModuleRunner extends WorkflowRunner
{
    public MiRCatModuleRunner(MiRCatModule module)
    {
        super(module);
    }
    
    @Override
    public boolean getActive()
    {
        if (this.engine instanceof MiRCatModule)
        {
            
            return ((MiRCatModule)engine).readyToContinue();
        }
        return false;
    }

//    @Override
//    public boolean getActive()
//    {
//        //return !this.getEngine().isComplete();
//    }
}
