package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.TimeableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution.DistributionService;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Uses the DistributionService to make abundance distribution boxplots and
 * writes these out json
 * @author Matthew
 */
public class AbundanceDistributionTool extends QualityCheckTool{
    
    public static final String TOOL_NAME = "AbundanceDistribution";

    DistributionService distService = (DistributionService) DatabaseWorkflowModule.getInstance().getContext().getBean("DistributionService");
    
    List<String> samples;
    List<NormalisationType> norms;
    AnnotationSetList annotations;
    AbundanceDistributionParameters params;
    
    public AbundanceDistributionTool(List<String> samples, List<NormalisationType> normTypes, 
                                     AnnotationSetList annotationSets, AbundanceDistributionParameters parameters) {
        super(TOOL_NAME);
        this.samples = samples;
        this.norms = normTypes;
        this.annotations = annotationSets;
        this.params = parameters;
    }
    
    public AbundanceDistributionTool(List<String> samples, List<NormalisationType> normTypes,
            AnnotationSetList annotationSets, AbundanceDistributionParameters parameters, StatusTracker tracker) {
        this(samples, normTypes, annotationSets, parameters);
        setTracker(tracker);
    }
    
    private void createDistributions()
    {
        distService.calculateAbundanceWindowDistribution2(samples, norms, annotations, params.getNumberOfWindows(), params.getWindowSize(), params.getFirstCount());
        this.lapTimer("Process abundance distributions");
//        for(String sample : samples)
//        {
//            for(NormalisationType norm : norms)
//            {
//                for(AnnotationSet annotation : annotations.getAllSets())
//                {
//                   // this.distService.calculateAbundanceWindowDistribution(sample, norm, params.getLogarithm(), annotation, params.getNumberOfWindows(), params.getWindowSize());
////                    DatabaseWorkflowModule.getInstance().printLap("Abundance distributions for " + sample + " " + norm + " " + annotation);
//                    this.lapTimer((new StringJoiner(", ")).add(sample).add(norm.getAbbrev()).add(annotation.getName()).toString());
//                }
//            }
//        }
    }

    @Override
    protected void processWithTimer() throws Exception {
        this.distService.clearAbundanceDistributions();
        createDistributions();
//        List<AnnotationSet> allSets = annotations.getAllSets();
//        String[] annotationTypes = new String[allSets.size()];
//        int i = 0;
//        for(AnnotationSet set : allSets)
//        {
//            annotationTypes[i] = set.getName();
//            i++;
//        }
//        this.distService.writeAllAbundanceDistributions(this.getJsonOutputPath(), annotationTypes);
//        this.lapTimer("Write to json " + this.getJsonOutputPath());
        this.distService.writeAllAbundanceDistributions(this.getJsonOutputPath());
        this.lapTimer("Write abw to json " + this.getJsonOutputPath());
    }
    
}
