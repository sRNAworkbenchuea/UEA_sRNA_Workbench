/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author keu13sgu 
 * 
 */
public class miRCat2ModuleRunner extends WorkflowRunner{
    
    public miRCat2ModuleRunner(MiRCat2Module module)
    {
        super(module);
    }
    
    @Override
    public boolean getActive()
    {
        if (this.engine instanceof MiRCat2Module)
        {
            
            return ((MiRCat2Module)engine).readyToContinue();
        }
        return false;
    }
    
}
