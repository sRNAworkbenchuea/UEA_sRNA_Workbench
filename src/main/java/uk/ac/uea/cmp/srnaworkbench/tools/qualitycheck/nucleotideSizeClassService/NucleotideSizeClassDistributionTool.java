package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.nucleotideSizeClassService;

import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author Matthew
 */
public class NucleotideSizeClassDistributionTool extends QualityCheckTool {
    private static final String TOOL_NAME = "NucleotideSizeClassDistribution";
    NucleotideSizeClassServiceLayer nucService = (NucleotideSizeClassServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("NucleotideSizeClassService");
    public NucleotideSizeClassDistributionTool()
    {
        super(TOOL_NAME);
    }
    public NucleotideSizeClassDistributionTool(StatusTracker tracker)
    {
        super(TOOL_NAME);
        this.setTracker(tracker);
    }
    
    @Override
    protected void processWithTimer() throws Exception {
        if(this.nucService.containsData()){
            nucService.buildSizeClass();
        }
        else
        {
            LOGGER.log(Level.INFO, "Nucleotide size class distribution table already built. The data will just be written out again.");
        }
        this.lapTimer("Building nucleotide position size classes");
        nucService.writeToJson(this.getJsonOutputPath());
        this.lapTimer("Writing to json");
    }
    
}
