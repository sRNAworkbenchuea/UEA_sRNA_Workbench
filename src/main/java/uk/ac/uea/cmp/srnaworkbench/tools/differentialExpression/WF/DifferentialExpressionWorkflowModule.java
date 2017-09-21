package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF;

import java.io.File;
import java.util.List;
import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.DifferentialExpressionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX.DifferentialExpressionSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.SamplePairManager;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * Module that that calculates differential expression between various
 * libraries and reports the results
 * @author Matthew and Matthew 
 */
public class DifferentialExpressionWorkflowModule extends WorkflowModule {
    private DifferentialExpressionTool d_e;
    
    public DifferentialExpressionWorkflowModule(String id)
    {
        this(id, new Rectangle2D(200,200,200,200));
    }
    
    public DifferentialExpressionWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "Differential Expression");
         //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/DifferentialExpressionScene.fxml";

        this.controller = new DifferentialExpressionSceneController(visualBounds, this);
    }

    public synchronized void setReadyToContinue()
    {
        System.out.println("de ready to go");
        //Differential Expression is ready to go
        this.notifyAll();
    }

    
    @Override
    protected synchronized void process() throws Exception {
        
        ((DifferentialExpressionSceneController)controller).populateSampleID_Interface();
        
        //hold this thread until the user has specified the sample pairs
        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            WorkflowSceneController.setWaitingNode(this.getID());

            this.wait();
        }
        WorkflowSceneController.setBusyNode(this.getID());

        NormalisationType type = OffsetReviewWorkflowModule.getNorm_type();
        
        d_e = new DifferentialExpressionTool(type);
        
        SamplePairManager samplePairs = new SamplePairManager();
                
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            samplePairs = ((DifferentialExpressionSceneController)controller).getSamplePairs();
        }
        else
        {
            FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
            List<String> samplenames = fileServ.getSamplenames();

            samplePairs.addSamplePair(samplenames.get(0), samplenames.get(1));
        }
        d_e.setSamplePairs(samplePairs);
        
        double newCutoff = ((DifferentialExpressionSceneController)controller).getFCCutoff();


        d_e.setFCCutoff(newCutoff, ((DifferentialExpressionSceneController)controller));
        d_e.run();
        
        ((DifferentialExpressionSceneController)controller).enableExport();
               
        
    }

    public void exportToCSV(File file)
    {
        d_e.exportToCSV(file);
    }

    public void queryAnnotations(String sequence, DifferentialExpressionSceneController controller)
    {
        this.d_e.queryAnnotations(sequence, controller);
    }

    public void loadResultSet(int currentWindowStart, int currentWindowEnd, DifferentialExpressionSceneController controller)
    {
        this.d_e.loadResultSet(currentWindowStart, currentWindowEnd, controller);
    }
    
}
