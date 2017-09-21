/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.WF;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ProgressIndicator;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowRunner;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author w0445959
 */
public class DatabaseWorkflowRunner extends WorkflowRunner
{

    public DatabaseWorkflowRunner()
    {
        super(DatabaseWorkflowModule.getInstance());

    }
    public DatabaseWorkflowRunner(Map<String, List<Path>> samples, Path genome, Path transcriptome)
    {
        super(DatabaseWorkflowModule.getInstance());
        DatabaseWorkflowModule.getInstance().insertRawData(samples, genome, transcriptome);
    }
    
    
    /**
     * Overrides the getAvtive method. This is because the usual method checks for active threads,
     * however, the database thread will spawn many other threads that are realted to database activity and
     * possible will not die until the program dies
     * @return 
     */
    @Override
     public boolean getActive()
    {
        if (this.engine instanceof DatabaseWorkflowModule)
        {
            return !DatabaseWorkflowModule.getInstance().getSetup();
        }
        return false;
    }


    boolean isComplete()
    {
        if (this.engine instanceof DatabaseWorkflowModule)
        {
            return DatabaseWorkflowModule.getInstance().getSetup();
        }
        return false;
    }

    
}
