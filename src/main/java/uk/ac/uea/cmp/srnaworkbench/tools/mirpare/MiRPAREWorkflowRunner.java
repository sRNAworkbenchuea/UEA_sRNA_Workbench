/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mirpare;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;

/**
 *
 * @author Chris Applegate
 */
public class MiRPAREWorkflowRunner extends WorkflowRunner {

    public MiRPAREWorkflowRunner(MiRPAREModule module) {
        super(module);
    }

   /* @Override
    public boolean getActive() {
        return !this.getEngine().isComplete();
    }
*/
}
