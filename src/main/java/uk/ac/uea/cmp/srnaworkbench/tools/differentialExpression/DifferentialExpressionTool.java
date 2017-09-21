package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import java.io.File;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX.DifferentialExpressionSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;

/**
 *
 * @author Matthew Beckers
 */
public class DifferentialExpressionTool extends QualityCheckTool{

    private SamplePairManager samplePairs = new SamplePairManager();
    private NormalisationType normType;
    private AnnotationSetList annotations;
    
    DifferentialExpressionService deServ = (DifferentialExpressionService) DatabaseWorkflowModule.getInstance().getContext().getBean("DifferentialExpressionService");
    private double FC_Cutoff;
    private DifferentialExpressionSceneController controller;
    
    public DifferentialExpressionTool(NormalisationType normType){
        super("DifferentialExpression");
        this.normType = normType;
    }
    
    /**
     * Add a pair of samples to run differential expression on
     * @param referenceSample
     * @param observedSample
     * @throws NotInDatabaseException 
     */
    public void addSamplePair(String referenceSample, String observedSample) throws NotInDatabaseException
    {
        this.samplePairs.addSamplePair(referenceSample, observedSample);
    }
    
    public void setSamplePairs(SamplePairManager samplePairs)
    {
        this.samplePairs = samplePairs;
    }
    
    public void setFCCutoff(double newCutoff, DifferentialExpressionSceneController controller)
    {
        this.FC_Cutoff = newCutoff;
        this.controller = controller;
    }

    @Override
    protected void processWithTimer() throws Exception {
        this.annotations = FileHierarchyViewController.getAnnotationSetList(false);
        System.out.println("Calculate confidence intervals");
        deServ.calculateConfidenceIntervals(this.samplePairs.getFilenames(), normType, annotations);
        System.out.println("Calculating fold changes");
        deServ.calculateFoldChanges(samplePairs.getSamplePairs(), normType, annotations);
        deServ.writeDeMaToJson(this.getJsonOutputPath(), normType);
        deServ.populateTable(FC_Cutoff, controller);
        
        // Boxplots are not currently produced because they are too intensive
        // deServ.writeDEdistributions(this.getJsonOutputPath("dist"), normType, FC_Cutoff);
    }

    public void exportToCSV(File file)
    {
        deServ.exportToCSV(file);
    }

    public void queryAnnotations(String sequence, DifferentialExpressionSceneController controller)
    {
        this.deServ.queryAnnotationSet(sequence, controller);
    }

    public void loadResultSet(int currentWindowStart, int currentWindowEnd, DifferentialExpressionSceneController controller)
    {
        this.deServ.loadResultSet(currentWindowStart, currentWindowEnd, controller);
    }

    

}
