package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceSizeClass;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author Matthew
 */
public class SizeClassDistributionTool extends QualityCheckTool {
    private static final String TOOL_NAME = "SizeClassDistribution";
    private SizeClassDistributionServiceLayer sdService = (SizeClassDistributionServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("SizeClassDistributionCalculator");
    
    private List<String> files;
    private List<NormalisationType> norms;
    private AnnotationSetList annotations;
    
    public SizeClassDistributionTool(List<String> files, List<NormalisationType> normTypes, AnnotationSetList annotations)
    {
        super(TOOL_NAME);
        this.files = files;
        this.norms = normTypes;
        this.annotations = annotations;
    }
    public SizeClassDistributionTool(List<String> files, List<NormalisationType> normTypes, AnnotationSetList annotations, StatusTracker tracker)
    {
        this(files, normTypes, annotations);
        setTracker(tracker);
    }

    @Override
    protected void processWithTimer() throws Exception
    {
        this.sdService.buildAllDistribution(files, norms, annotations.getAllTypes());
//                this.lapTimer("Size class for " + file + ", " + norm.getAbbrev());
        this.sdService.writeToJson(this.getJsonOutputPath(), files, norms, annotations);
//        DatabaseWorkflowModule.getInstance().printLap("Writing size classes");
        DatabaseWorkflowModule.getInstance().printLap("Size class distributions");
//        this.sdService.writeTotalToJson(Paths.get(this.getJsonOutputPath().toString() + "_total"));
//        this.lapTimer("Written total");
    }
}
