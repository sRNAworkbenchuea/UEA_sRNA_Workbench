package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck;

import java.nio.file.Path;
import java.nio.file.Paths;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.tools.TimeableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Base class for all quality check tools
 * @author Matthew
 */
public abstract class QualityCheckTool extends TimeableTool {

    public QualityCheckTool(String toolName) {
        super(toolName);
    }
    
    private String stageName = "";
    private static final String JSON_OUTPUT_DIR = Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR;
    
    public void setStageName(String name)
    {
        this.stageName = name;
    }
    
    public String getStageName()
    {
        return this.stageName;
    }
    
    /**
     * Returns the path that should be used
     * to write the results of this QC into a json
     * 
     * If more than one json file is required, the modifier parameter
     * can be used to distinguish between them using the rule
     * [pathName]_[modifier].json
     * @param modifier
     * @return 
     */
    public Path getJsonOutputPath(String modifier)
    {
        if(!modifier.equals(""))
            modifier = "_"+modifier;
        return Paths.get(JSON_OUTPUT_DIR + stageName + this.getToolName() + modifier + ".json");
    }
    
    public Path getJsonOutputPath() {
        return getJsonOutputPath("");
    }
    
}
