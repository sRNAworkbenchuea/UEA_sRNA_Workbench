/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.targetprediction.WF;

import java.nio.file.Path;
import org.hibernate.Query;
import org.hibernate.Session;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author w0445959
 */
public class TargetPredictionWorkflowModule extends WorkflowModule
{
    
    public TargetPredictionWorkflowModule(String id, String title) {
        
        super(id,title);
        
    }
    
    private void alignSequencesToTranscriptome()
    {
        Path transcriptPath = HTMLWizardViewController.getTranscriptome();
        
        //Session session = locusDAO.getSessionFactory().openSession();
//        String sql = ((HQLQuerySimple)WorkflowManager.getInstance().getInputData("srnaQuery").getContainer(1).getData()).eval(); 
//        Query sQLQuery = session.createQuery(sql);

    }

    @Override
    protected void process() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
