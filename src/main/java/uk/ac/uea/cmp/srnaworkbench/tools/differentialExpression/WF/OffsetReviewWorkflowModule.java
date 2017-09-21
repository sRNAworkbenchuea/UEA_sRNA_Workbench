/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.geometry.Rectangle2D;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX.OffsetReviewSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.StrandBiasDivergenceParams;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.StrandBiasDivergenceTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author w0445959
 */
public class OffsetReviewWorkflowModule extends WorkflowModule
{
    private static NormalisationType norm_type = null;
    
    private StrandBiasDivergenceParams params = new StrandBiasDivergenceParams();
    
    public OffsetReviewWorkflowModule(String id)
    {
        this(id, new Rectangle2D(200,200,200,200));
    }
    
    public OffsetReviewWorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "Offset Review");
         //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/OffsetReviewScene.fxml";

        this.controller = new OffsetReviewSceneController(visualBounds, this);
    }
    
    public void setStrandBiasDivergenceParams(StrandBiasDivergenceParams params)
    {
        this.params= params;
    }
    
    public StrandBiasDivergenceParams getStrandBiasDivergenceParams()
    {
        return this.params;
    }

    public static NormalisationType getNorm_type()
    {
        return norm_type;
    }

    public void setNorm_type(NormalisationType new_norm_type)
    {
        norm_type = new_norm_type;
    }

    public synchronized void setReadyToContinue()
    {
        //normalisation is ready to go
        this.notifyAll();
    }

    @Override
    protected synchronized void process() throws Exception
    {
        //hold this thread until the user has specified a normalisation
        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            OffsetReviewSceneController oc = ((OffsetReviewSceneController)this.controller);
            oc.setupOffsetReview();
            
            /**
             * If chosen norm type is already set (probably due to only one norm type available),
             * immediately continue with processing offsets.
             */
            if(oc.getChosenNormType() != null)
            {
                this.norm_type = oc.getChosenNormType();
            }
            else
            {
                WorkflowSceneController.setWaitingNode(this.getID());
                this.wait();
            }
        }
        else
        {
            norm_type = NormalisationType.TOTAL_COUNT;
        }
        WorkflowSceneController.setBusyNode(this.getID());

        List<String> filenames;

        FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        if (DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            
            filenames = fserv.getFilenames();
        }
        else
        {
            //DatabaseWorkflowModule.getMySamples();
            filenames = FileReviewWorkflowModule.getSelectedFiles();
            if(filenames.isEmpty())
            {
                filenames = fserv.getFilenames();
            }
        }
        if (filenames.isEmpty())
        {
            throw new IOException("No samples were specified for offset review");
        }


        StrandBiasDivergenceTool sbd = new StrandBiasDivergenceTool();
        sbd.setFilenames(filenames);
        sbd.setParams(params);
        sbd.setNormType(norm_type);
        sbd.run();
        ((OffsetReviewSceneController)controller).rebuildOffsetView();
    }
    
}
