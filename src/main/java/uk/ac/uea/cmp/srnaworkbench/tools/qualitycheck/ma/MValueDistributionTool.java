package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution.DistributionService;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author Matthew
 */
public class MValueDistributionTool extends QualityCheckTool{

    private static final String TOOL_NAME = "MValueDistribution";
    
    DistributionService distService = (DistributionService) DatabaseWorkflowModule.getInstance().getContext().getBean("DistributionService");

    List<FilePair> filePairs;
    List<NormalisationType> norms;
    List<Integer> offsets;
    AnnotationSetList annotations;
    
    MValueDistributionParameters params;
    
    public MValueDistributionTool(List<FilePair> filePairs, List<NormalisationType> normTypes, List<Integer> offsets, AnnotationSetList annotationSets, MValueDistributionParameters params)
    {
        super(TOOL_NAME);
        this.filePairs = filePairs;
        this.norms = normTypes;
        this.offsets = offsets;
        this.annotations = annotationSets;
        this.params = params;
    }
    public MValueDistributionTool(List<FilePair> filePairs, List<NormalisationType> normTypes, List<Integer> offsets, AnnotationSetList annotationSets, StatusTracker tracker, MValueDistributionParameters params) {
        this(filePairs, normTypes, offsets, annotationSets, params);
        setTracker(tracker);
    }
    
    private void createDistribution()
    {
        for(FilePair pair : this.filePairs)
        {
            for(NormalisationType norm : norms)
            {
                for(int offset : offsets)
                {
                    for(AnnotationSet annotation : annotations.getAllSets())
                    {
//                        distService.createMValueDistribution(pair, norm, offset, annotation);
                        distService.createMValueNrDistribution(pair, norm, (double) offset, annotation, params.getMinExpression());
                        this.lapTimer((new StringJoiner(",")).add(pair.toString()).add(norm.getAbbrev()).add(annotation.getName()).toString());
                    }
                }
            }
        }
    }
    @Override
    protected void processWithTimer() throws Exception {
        this.distService.clearMValueDistributions();
        this.createDistribution();
        this.distService.writeAllMValueDistributions(this.getJsonOutputPath());
        this.lapTimer("Writing to json");
    }
    
}
