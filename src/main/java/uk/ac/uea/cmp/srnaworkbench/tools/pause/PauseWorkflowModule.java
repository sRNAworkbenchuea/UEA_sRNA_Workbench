/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pause;

import uk.ac.uea.cmp.srnaworkbench.workflow.gui.ViewerGUI;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
/**
 *
 * @author w0445959
 */
public class PauseWorkflowModule extends WorkflowModule
{

    public PauseWorkflowModule(String id)
    {
        super(id, "Pause");
    }

    @Override
    protected void process() throws WFModuleFailedException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}
