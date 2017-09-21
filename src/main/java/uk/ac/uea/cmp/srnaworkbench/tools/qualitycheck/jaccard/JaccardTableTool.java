package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.QualityCheckTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author Matthew
 */
public class JaccardTableTool extends QualityCheckTool {
    private static final String TOOL_NAME = "JaccardTable";
    
    List<String> files;
    List<NormalisationType> norms;
    JaccardTableParams params;
    
    JaccardIndexServiceLayer jaccService = (JaccardIndexServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("JaccardIndex");
    public JaccardTableTool(List<String> filenames, List<NormalisationType> normTypes, JaccardTableParams params) {
        super(TOOL_NAME);
        files = filenames;
        norms = normTypes;
        this.params = params;
    }
    public JaccardTableTool(List<String> filenames, List<NormalisationType> normTypes, JaccardTableParams params, StatusTracker tracker) {
        this(filenames, normTypes, params);
        setTracker(tracker);
    }

    @Override
    protected void processWithTimer() throws Exception {
        System.out.println("Building jaccard");
        this.lapTimer("Building jaccard table");

        jaccService.calculateJaccardTable(files, norms, params.getNumberOfSequences());
        DatabaseWorkflowModule.getInstance().printLap("Built Jaccard Table");

        jaccService.writeToJson(this.getJsonOutputPath());
        
        this.lapTimer("Writing jaccard table");
        DatabaseWorkflowModule.getInstance().printLap("Written Jaccard");

    }
    
}
