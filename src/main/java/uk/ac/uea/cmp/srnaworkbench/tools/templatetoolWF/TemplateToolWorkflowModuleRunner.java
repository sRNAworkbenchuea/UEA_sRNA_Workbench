/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.templatetoolWF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author Josh Thody
 */
public class TemplateToolWorkflowModuleRunner extends WorkflowRunner
{
     
     public TemplateToolWorkflowModuleRunner(TemplateToolWorkflowModule engine)
    {
        super(engine);
    }
    
    @Override
    public boolean getActive()
    {

        if (this.engine instanceof TemplateToolWorkflowModule)
        {
            System.out.println("template tool returning: " + ((TemplateToolWorkflowModule)engine).readyToContinue());
            return !((TemplateToolWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
