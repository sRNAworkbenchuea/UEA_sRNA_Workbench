/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author w0445959
 */
public class ReportWorkflowModuleRunner extends WorkflowRunner
{
    public ReportWorkflowModuleRunner(ReportWorkflowModule engine)
    {
        super(engine);

    }
    
    @Override
    public boolean getActive()
    {
        if (this.engine instanceof ReportWorkflowModule)
        {
            
            return !((ReportWorkflowModule)engine).readyToContinue();
        }
        return false;
    }
}
