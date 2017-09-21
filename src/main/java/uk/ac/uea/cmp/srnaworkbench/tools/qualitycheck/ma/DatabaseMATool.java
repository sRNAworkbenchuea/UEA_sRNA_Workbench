package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author Matthew
 */
public class DatabaseMATool extends QualityCheckTool {
    private static final String TOOL_NAME = "DatabaseMA";
            
    private MAService maService = (MAService) DatabaseWorkflowModule.getInstance().getContext().getBean("MAService");

    private List<FilePair> pairs;
    private List<NormalisationType> normTypes;
    private AnnotationSetList annotations;
    private List<Integer> offsets;
    
    public DatabaseMATool(List<FilePair> filePairs, List<NormalisationType> normTypes, AnnotationSetList annotationTypes, 
                          List<Integer> offsets)
    {
        super(TOOL_NAME);
        this.pairs = filePairs;
        this.normTypes = normTypes;
        this.annotations = annotationTypes;
        this.offsets = offsets;
    }
    public DatabaseMATool(List<FilePair> filePairs, List<NormalisationType> normTypes, AnnotationSetList annotationTypes,
            List<Integer> offsets, StatusTracker tracker) {
        this(filePairs, normTypes, annotationTypes, offsets);
        setTracker(tracker);
    }
    
    @Override
    protected void processWithTimer() throws Exception {
        if (!pairs.isEmpty())
        {
            ArrayList<Logarithm> logs = new ArrayList<>();
            logs.add(Logarithm.BASE_2);
//        maService.buildList(pairs, normTypes, new ArrayList<>(annotations.getAllTypes()), offsets, logs, false, false);
            maService.buildNonRedundantList(pairs, normTypes, new ArrayList<>(annotations.getAllTypes()), offsets, logs, false, false);
            this.lapTimer("Built all MAs");
//        maService.printNRMAvalues();
            maService.writeNrMaToJson(this.getJsonOutputPath(), annotations);
            maService.writeNrAbundanceToJson(this.getJsonOutputPath("exp"), annotations);
            this.lapTimer("MAs to JSON");
        }
    }
    
}
