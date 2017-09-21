package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.geometry.Rectangle2D;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import static uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModule.parseFA;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

public class TasiModule extends WorkflowModule {

    // inputs
    private final DataContainerList<GenomeManager> in_genome;
    private final DataContainerList<HQLQuerySimple> in_sRNAQuery;
    private TasiController c;
    private boolean complete;
    private File outDir;

    public TasiModule(String id, String title, Rectangle2D visualBounds) {
        super(id, title);
        this.complete = false;
        // inputs
        this.in_genome = new DataContainerList<>("genome", CompatibilityKey.GENOME, 1, 1);
        this.in_sRNAQuery = new DataContainerList<>("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, -1);
        setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + "TasiScene.fxml");
        c = new TasiController(this, visualBounds);
        this.controller = c;
        this.outDir = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + id + "_output");
        try {
            addInputDataContainerList(this.in_genome);
            addInputDataContainerList(this.in_sRNAQuery);
        } catch (InitialisationException | DuplicateIDException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public boolean isComplete() {
        return this.complete;
    }

    @Override
    public void process() throws HQLQuery.HQLFormatException, Exception {

            // output mircat params to file
        // File paramsFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_params.cfg");
           // this.params.save(paramsFile);
//            c.write2Log(this.params.toDescriptiveString());
        //          c.write2Log("INFORMATION: Prediction started.");
        ToolBox tool = ToolBox.getToolForName("tasi");
        Map<String, String> args = new HashMap();

        // retrieve the srnas
        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");
        List<Sequence_Entity> sequences = sequence_service.executeSQL(this.in_sRNAQuery.getContainer(0).getData().eval());
        // write sequences to file
        File inputSequencesFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences.fa");
        PrintWriter writer = new PrintWriter(inputSequencesFile);
        for (Sequence_Entity sequence : sequences) {
            for (int i = 0; i < sequence.getAbundance(); i++) {
                writer.println(">" + sequence.getRNA_Sequence());
                writer.println(sequence.getRNA_Sequence());
            }
        }
        writer.close();
        File inputSequencesFileFormatted = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences_formatted.fa");
        parseFA(inputSequencesFile, inputSequencesFileFormatted);

        args.put("srna_file", inputSequencesFileFormatted.getAbsolutePath());
        args.put("genome", this.in_genome.getContainer(0).getData().getPath().toString());
        // args.put("params", paramsFile.getAbsolutePath());
        args.put("out_file", this.outDir.getAbsolutePath());
        tool.startTool(args);

        
        this.complete = true;
        c.updateUI();

    }

}
