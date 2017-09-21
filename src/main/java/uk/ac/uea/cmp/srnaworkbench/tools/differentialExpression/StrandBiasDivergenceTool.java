package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import java.util.ArrayList;
import java.util.Collection;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;

/**
 * Calculates the Kulback-Leibler divergence metric for each expression level in a library
 * on the strand biases of sliding windows of alignments. These divergence measures can be
 * later used to find the best offset to use in an offset-fold change differential expression
 * analysis.
 * 
 * @author Matthew
 */
public class StrandBiasDivergenceTool extends QualityCheckTool {
    Collection<Filename_Entity> files = new ArrayList<>();
    NormalisationType normType;
    StrandBiasDivergenceParams params = new StrandBiasDivergenceParams();

    public StrandBiasDivergenceTool() {
        super("StrandBiasDivergence");
        params = new StrandBiasDivergenceParams();
    }
    
    public void addFilename(String filename) throws NotInDatabaseException
    { 
        FilenameServiceImpl service = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        Filename_Entity fe = service.findById(filename);
        if(fe == null)
            throw new NotInDatabaseException("Filename " + filename + " is not in the database" );
        this.files.add(fe);
    }
    
    public void setFilenames(Collection<String> filenames) throws NotInDatabaseException
    {
        for(String f : filenames)
        {
            this.addFilename(f);
        }
    }
    
    public void addFilename(Filename_Entity fe)
    {
        this.files.add(fe);
    }
    
    public void setNormType(NormalisationType normType)
    {
        this.normType = normType;
    }
    
    public void setParams(StrandBiasDivergenceParams params)
    {
        this.params = params;
    }

    @Override
    protected void processWithTimer() throws Exception {
        AlignmentWindowService service = (AlignmentWindowService) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignmentWindowService");

        System.out.println("Create windows");
        service.createWindows(params.getWindowSize());
        DatabaseWorkflowModule.getInstance().printLap("Offsets: generating windows of size " + params.getWindowSize() + " nt");
        service.calculateStrandBiasPerWindow(files, normType);
        DatabaseWorkflowModule.getInstance().printLap("Offsets: caclulating strand biases");
        for(Filename_Entity filename : files){
            service.calculateKulbackLeiblerDivergence(filename, params.getMaxExpressionLevel(), params.getNumberOfBins());
            DatabaseWorkflowModule.getInstance().printLap("Offsets: KL divergence for " + filename);
            service.findKLderivedOffset(filename, 0.2);
            DatabaseWorkflowModule.getInstance().printLap("Offsets: offset generated for " + filename);
        }
        service.writeKLtoJson(this.getJsonOutputPath());
        DatabaseWorkflowModule.getInstance().printLap("Offsets: KL measures written to JSON ");
    }
    
}
