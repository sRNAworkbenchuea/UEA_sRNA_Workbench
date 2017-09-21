/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.InteractionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
import uk.ac.uea.cmp.srnaworkbench.tools.mirpare.MiRPAREModule;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author Chris Applegate
 */
public class PAREsnipModule extends WorkflowModule {

    // FXML file for PAREsnip
    private final static String PARESNIP_FXML = "PAREsnipScene.fxml";
    // input data containers
    private final DataContainerList<File> in_degradome;
    private final DataContainerList<GenomeManager> in_genome;
    private final DataContainerList<HQLQuerySimple> in_srnaQuery;
    private final DataContainerList<Path> in_transcripts;
    // outputs data containers
    private final DataContainerList<HQLQuerySimple> out_interactionQuery;
    // PAREsnip parameters
    private ParesnipParams parameters;
    // flag to indicate completion status
    private boolean complete;
    // controller specific to PAREsnip
    private PAREsnipController paresnipController;
    // flag to determine iwhether we actually need to run PAREsnip or just use provided output from another PAREsnip run
    public boolean runPAREsnip;

    /*
     * Constructor
     * @param id: unique ID for the module (must only contain alpha-numeric characters)
     * @param title: descriptive title for display purposes
     */
    public PAREsnipModule(String id, String title, Rectangle2D visualBounds) throws InitialisationException {
        super(id, title);
        this.complete = false;
        this.runPAREsnip = true;
        this.in_degradome = new DataContainerList<>("degradome", CompatibilityKey.DEGRADOME_FILE, 1, 1);
        this.in_genome = new DataContainerList<>("genome", CompatibilityKey.GENOME, 1, 1);
        this.in_srnaQuery = new DataContainerList<>("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, 1);
        this.in_transcripts = new DataContainerList<>("transcripts", CompatibilityKey.TRANSCRIPT_FILE, 1, 1);
        this.out_interactionQuery = new DataContainerList<>("interactionQuery", CompatibilityKey.INTERACTION_QUERY, 1, 1);
        // create default parameters for PAREsnip
        this.parameters = new ParesnipParams();
        // set-up FXML resources
        setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + PARESNIP_FXML);
        this.controller = paresnipController = new PAREsnipController(this);
        try {
            this.out_interactionQuery.add(new DataContainer<>(CompatibilityKey.INTERACTION_QUERY, new HQLQuerySimple(Interaction_Entity.class)));
            addInputDataContainerList(this.in_degradome);
            addInputDataContainerList(this.in_genome);
            addInputDataContainerList(this.in_srnaQuery);
            addInputDataContainerList(this.in_transcripts);
            addOutputDataContainerList(this.out_interactionQuery);
        } catch (MaximumCapacityException | CompatibilityException | InitialisationException | DuplicateIDException ex) {
            throw new InitialisationException("PAREsnip module could not be initialised:" + ex);
        }
    }

    /*
     * returns current PAREsnip parameter set
     */
    public ParesnipParams getParameters() {
        return this.parameters;
    }

    /*
     * returns the completion status of the module
     */
    public boolean isComplete() {
        return this.complete;
    }

    private void parsePAREsnipGUIFile(File inFile, Set<String> seqs) throws FileNotFoundException {
        // access the interaction service
        InteractionServiceImpl interaction_service = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");
        // open the PAREsnip output file
        Scanner in = new Scanner(inFile);
        in.nextLine();
        // create a new interaction for each record
        Interaction_Entity interaction = new Interaction_Entity();
        // line counter is used to determine the separation between records in PAREsnip output
        String str = "";
        int lineCounter = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            int lineOffset = lineCounter % 3;
            if (lineOffset == 0) {
                str = line + System.getProperty("line.separator");
                interaction = new Interaction_Entity();
            } else {
                str += line + System.getProperty("line.separator");
                if (lineOffset == 2) {
                    String[] components = str.split("[\"]");
                    interaction.setTranscript(components[3].trim());
                    interaction.setCategory(Integer.parseInt(components[5].trim()));
                    interaction.setCleaveagePos(Integer.parseInt(components[7].trim()));
                    interaction.setPVal(Double.parseDouble(components[9].trim()));
                    interaction.setFragmentAbundance(Integer.parseInt(components[11].trim()));
                    interaction.setWeightedFragmentAbundance(Double.parseDouble(components[13].trim()));
                    interaction.setNormalisedWeightedFragmentAbundance(Double.parseDouble(components[15].trim()));
                    interaction.setDuplex(components[17]);
                    interaction.setAlignmentScore(Double.parseDouble(components[19]));
                    interaction.setShortReadID(components[21]);
                    interaction.setPredictor(getID());
                    // extract srna from duplex and check is valid based on input set
                    try {
                        Unique_Sequences_Entity unique_sequence = findTarget_sRNA(interaction, seqs);
                        interaction.setsRNA(unique_sequence);
                        interaction_service.saveOrUpdate(interaction);
                    } catch (Exception ex) {
                        this.paresnipController.write2Log("WARNING:" + ex);
                    }
                }
            }
            lineCounter++;
        }

        in.close();
    }

    private void parsePAREsnipCommandLineFile(File inFile, Set<String> seqs) throws FileNotFoundException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException, Exception {
        // access the interaction service
        InteractionServiceImpl interaction_service = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");
        // open the PAREsnip output file
        Scanner in = new Scanner(inFile);
        // create a new interaction for each record
        Interaction_Entity interaction = new Interaction_Entity();
        // line counter is used to determine the separation between records in PAREsnip output
        int lineCounter = 0;
        String duplex = "";
        while (in.hasNextLine()) {
            String line = in.nextLine();
            int lineOffset = lineCounter % 5;
            if (lineOffset == 0) {

                String[] components = line.split("\t");
                interaction.setTranscript(components[0].trim());
                interaction.setCategory(Integer.parseInt(components[1]));
                interaction.setCleaveagePos(Integer.parseInt(components[2]));
                interaction.setPVal(Double.parseDouble(components[3]));
                interaction.setFragmentAbundance(Integer.parseInt(components[4]));
                interaction.setWeightedFragmentAbundance(Double.parseDouble(components[5]));
                interaction.setNormalisedWeightedFragmentAbundance(Double.parseDouble(components[6]));
                interaction.setAlignmentScore(Double.parseDouble(components[7]));
                interaction.setShortReadID(components[8].trim());
                interaction.setPredictor(getID());
            } else if (lineOffset == 4) { // reached the end of a record
                interaction.setDuplex(duplex);
                // extract srna from duplex and check is valid based on input set
                try {
                    Unique_Sequences_Entity unique_sequence = findTarget_sRNA(interaction, seqs);
                    interaction.setsRNA(unique_sequence);
                    interaction_service.saveOrUpdate(interaction);
                } catch (Exception ex) {
                    this.paresnipController.write2Log("WARNING:" + ex);
                }

                interaction = new Interaction_Entity();
                duplex = "";
            } else {
                duplex += line + System.getProperty("line.separator");
            }
            lineCounter++;
        }
        in.close();
    }

    public Unique_Sequences_Entity findTarget_sRNA(Interaction_Entity interaction, Set<String> srnas) throws Exception {

        if (interaction == null) {
            System.out.println("interaction is null");
            throw new NullPointerException("Interaction is null");
        }

        UniqueSequencesServiceImpl service = (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
        String srna = interaction.extract_sRNAFromDuplex();
        String best_match_srna = "";
        if (!srnas.contains(srna)) {
            int bestMatchDiff = 0;
            // loop through srnas in the input sRNA list to find what we think is the sequence of interest
            Iterator<String> it = srnas.iterator();
            while (it.hasNext()) {
                String s = it.next();
                if (s.contains(srna) && s.startsWith(srna)) {
                    int sizeDiff = s.length() - srna.length();
                    if (sizeDiff < bestMatchDiff || best_match_srna.isEmpty()) {
                        best_match_srna = s;
                        bestMatchDiff = sizeDiff;
                    }
                }
            }
            if (best_match_srna.isEmpty()) {
                String str = "The target sRNA was not found in the sRNAome:";
                str += interaction.get_sRNA();
                throw new Exception(str);

            }
        }
        // now that we know the sRNA is in the input sRNA list: find it's corresponding unique sequence in the DB
        Unique_Sequences_Entity db_unique_sequence = service.findById(srna);
        // if there is no corresponding unique sequence: throw exception
        if (db_unique_sequence != null) {
            return db_unique_sequence;
        } else {
            throw new Exception("The target sRNA was not found in the database.");

        }
    }
    /*
     * parse the PAREsnip result file
     * @param inFile: PAREsnip output file to be parsed
     */

    private void parsePAREsnipFile(File inFile, Set<String> seqs) throws FileNotFoundException, HQLQuery.HQLQueryLockedException, Exception {
        Scanner in = new Scanner(inFile);
        String headerLine = in.nextLine();
        in.close();
        if (headerLine.startsWith("Select,Gene,Category,")) {
            parsePAREsnipGUIFile(inFile, seqs);
        } else {
            parsePAREsnipCommandLineFile(inFile, seqs);
        }
        // produce module interaction output query
        HQLQuerySimple outputInteractionQuery = this.out_interactionQuery.getContainer(0).getData();
        outputInteractionQuery.addWhere("A.predictor = '" + getID() + "'");
        // lock the query so that other modules cannot modify this
        outputInteractionQuery.lock();
        // create t-plots
        MiRPAREModule.createTPlots(false, this.getID());
    }

    /*public void createTPlots() throws FileNotFoundException {
        InteractionServiceImpl interaction_service = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");

        List<Interaction_Entity> interactions = interaction_service.getInteractionsOrderByGene();
        PrintWriter writer = null;
        String currentGene = "";
        JsonArrayBuilder nodeArray = null;

        for (Interaction_Entity i : interactions) {

            // if the interaction was predicted by this module
            if (i.getPredictor().equals(this.getID())) {
                // if we are now looking at uniqueInputSequencesQuery different gene
                if (!currentGene.equals(i.getGene().getID() + "")) {
                    if (writer != null) {
                        // save json array as single object
                        JsonObjectBuilder json = Json.createObjectBuilder();
                        json.add("points", nodeArray);
                        JsonObject jsonObj = json.build();
                        JsonWriter jsonWriter = Json.createWriter(writer);
                        jsonWriter.writeObject(jsonObj);
                        jsonWriter.close();
                    }
                    // open new file
                    File file = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + i.getGene().getID() + "_" + this.getID() + "_tplot.json");
                    writer = new PrintWriter(file);
                    nodeArray = Json.createArrayBuilder();
                }
                // add interaction to gene json file
                JsonObjectBuilder jsonNode = Json.createObjectBuilder();
                jsonNode.add("x", i.getCleavagePos());
                jsonNode.add("y", i.getFragmentAbundance());
                jsonNode.add("radius", 3);
                if (i.getCategory() == 0) {
                    jsonNode.add("color", "rgb(255,51,0)");
                } else if (i.getCategory() == 1) {
                    jsonNode.add("color", "rgb(204,255,0)");
                } else if (i.getCategory() == 2) {
                    jsonNode.add("color", "rgb(0,204,255)");
                } else if (i.getCategory() == 3) {
                    jsonNode.add("color", "rgb(255,255,0)");
                } else if (i.getCategory() == 4) {
                    jsonNode.add("color", "rgb(255,0,204)");
                }

                nodeArray.add(jsonNode);
                currentGene = i.getGene().getID() + "";
            }
        }
    }*/

    /*
     * Set PAREsnip parameters
     * @parameters: PAREsnip parameters to be set
     */
    public void setParameters(ParesnipParams parameters) {
        // set the new PAREsnip parameters
        this.parameters = parameters;
        // update the GUI to reflect the new parameter values
        this.paresnipController.updateUI();
    }

    @Override
    protected void process() throws WFModuleFailedException, HQLQuery.HQLQueryLockedException, Exception {
        this.paresnipController.write2Log("INFORMATION: PAREsnip module started.");
        // setup output file
        File outFile;
        Set<String> seqs = new HashSet<>();

        // get the IDs of the sequences from the in-coming sRNA query
        HQLQuerySimple inputSequencesQuery = this.in_srnaQuery.getContainer(0).getData();
        HQLQueryComplex inputSequencesIDQuery = new HQLQueryComplex(inputSequencesQuery);
        inputSequencesIDQuery.addSelect(inputSequencesQuery.getFromAlias() + ".id", "id");
        // get all unique sRNAs IDs that are in input sRNA query
        HQLQueryComplex uniqueInputSequencesIDQuery = new HQLQueryComplex();
        uniqueInputSequencesIDQuery.addSelect("U.id", "id");
        uniqueInputSequencesIDQuery.addFrom(Unique_Sequences_Entity.class, "U");
        uniqueInputSequencesIDQuery.addFrom(Sequence_Entity.class, "S");
        uniqueInputSequencesIDQuery.addWhere(String.format("S.id IN (%s)", inputSequencesIDQuery.eval()));
        uniqueInputSequencesIDQuery.addWhere("U.RNA_Sequence = S.RNA_Sequence");
        // get full unqiue sRNA records
        HQLQuerySimple uniqueInputSequencesQuery = new HQLQuerySimple(Unique_Sequences_Entity.class);

        uniqueInputSequencesQuery.addWhere(String.format("A.id IN (%s)", uniqueInputSequencesIDQuery.eval()));
        // get the unique uniqueSequence service and return all unique sequences from query
        UniqueSequencesServiceImpl uniqueSequenceService = (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
        List<Unique_Sequences_Entity> uniqueSequences = uniqueSequenceService.executeSQL(uniqueInputSequencesQuery.eval());

        File inputSequencesFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences.fa");
        PrintWriter writer = new PrintWriter(inputSequencesFile);
        for (Unique_Sequences_Entity uniqueSequence : uniqueSequences) {
            writer.println(">" + uniqueSequence.getRNA_Sequence());
            writer.println(uniqueSequence.getRNA_Sequence());
            seqs.add(uniqueSequence.getRNA_Sequence());
        }
        writer.close();

        if (this.runPAREsnip) {
            outFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_output.tmp");
            // setup parameter file
            File paramsFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_params.cfg");
            try {
                this.paresnipController.write2Log("INFORMATION: Saving PAREsnip parameters.");
                // save the current parameter set to file
                this.parameters.save(paramsFile);
                this.paresnipController.write2Log("INFORMATION: Retrieving unique sRNA sequences.");

                this.paresnipController.write2Log(
                        "INFORMATION: Writing unique sRNA sequences to file for PAREsnip algorithm.");
                // write these unique sequences to file

                /*File inputSequencesFile = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences.fa");
                PrintWriter writer = new PrintWriter(inputSequencesFile);
                for (Unique_Sequences_Entity uniqueSequence : uniqueSequences) {
                    writer.println(">" + uniqueSequence.getRNA_Sequence());
                    writer.println(uniqueSequence.getRNA_Sequence());
                    seqs.add(uniqueSequence.getRNA_Sequence());
                }

                writer.close();*/

                this.paresnipController.write2Log(
                        "INFORMATION: Creating unique sequence file in non-redundant format.");
                // format the input file to be in redundant format for Workbench
                File inputSequencesFileFormatted = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences_formatted.fa");

                parseFA(inputSequencesFile, inputSequencesFileFormatted);

                this.paresnipController.write2Log(
                        "INFORMATION: Executing PAREsnip core algorithm.");
                // run the tool
                Map<String, String> args = new HashMap();

                args.put(
                        "srna_file", inputSequencesFileFormatted.getAbsolutePath());
                args.put(
                        "deg_file", this.in_degradome.getContainer(0).getData().getAbsolutePath());
                args.put(
                        "tran_file", this.in_transcripts.getContainer(0).getData().toAbsolutePath().toString());
                args.put(
                        "genome_file", this.in_genome.getContainer(0).getData().getPath().toAbsolutePath().toString());
                args.put(
                        "out_file", outFile.getAbsolutePath());
                args.put(
                        "verbose", "");
                args.put(
                        "f", "");
                args.put(
                        "params", paramsFile.getAbsolutePath());
                ToolBox tool = ToolBox.getToolForName("paresnip");

                tool.startTool(args);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            this.paresnipController.write2Log("INFORMATION: PAREsnip output file provided: skipping algorithm.");
            outFile = new File(this.in_degradome.getContainer(0).getData().getAbsolutePath());
        }
        // parse the results
        this.paresnipController.write2Log("INFORMATION: Parsing the PAREsnip results.");

        parsePAREsnipFile(outFile, seqs);
        // set the module status to complete
        this.complete = true;
        // update any graphical user interface
        paresnipController.updateUI();
        this.paresnipController.write2Log("INFORMATION: PAREsnip module complete.");

    }

    public static void parseFA(File input, File output) throws FileNotFoundException {
        Map<String, Integer> rnas = new HashMap<>();
        Scanner in = new Scanner(input);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (!line.startsWith(">") && !line.isEmpty()) {
                String rna = line.trim();
                int abundance = 0;
                if (rnas.containsKey(rna)) {
                    abundance = rnas.get(rna);
                }
                abundance++;
                rnas.put(rna, abundance);
            }
        }
        in.close();

        PrintWriter writer = new PrintWriter(output);
        for (String rna : rnas.keySet()) {
            writer.println(">" + rna + "(" + rnas.get(rna) + ")");
            writer.println(rna);
        }
        writer.close();
    }

    /*
     * returns a query of the PAREsnip results for display purposes
     */
    public HQLQuery generateOutput() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {
        // generate query for GUI
        HQLQueryComplex hqlQuery = new HQLQueryComplex();
        hqlQuery.addSelect("I.gene.gene", "Transcript");
        hqlQuery.addSelect("I.category", "Category");
        hqlQuery.addSelect("I.cleavagePos", "Cleavage_Pos");
        hqlQuery.addSelect("I.sRNA.RNA_Sequence", "sRNA");
        hqlQuery.addSelect("I.pVal", "P_Val");
        hqlQuery.addSelect("I.duplex", "Duplex");
        hqlQuery.addSelect("I.alignmentScore", "Alignment_Score");
        hqlQuery
                .addFrom(Interaction_Entity.class, "I");
        hqlQuery.addWhere(
                "I.predictor = '" + this.getID() + "'");
        return hqlQuery;
    }

    /* public void cleanUpTranscriptome() throws FileNotFoundException {
     File tFile = in_transcripts.getContainer(0).getData().toFile();
     File ctFile = new File(tFile.getName() + "_clean.fa");
     Scanner in = new Scanner(tFile);
     PrintWriter w = new PrintWriter(ctFile);
     while (in.hasNextLine()) {
     String line = in.nextLine();
     if (line.startsWith(">")) {
     w.println(line);
     } else {
     line = line.replaceAll("[^TGACUtgacu]", "N");
     w.println(line);
     }
     }
     w.close();
     in.close();

     }*/

    /*public String mapReads() throws FileNotFoundException {
     String alignedFile = Tools.miRPARE_DATA_Path + DIR_SEPARATOR + getID() + ".patman";

     File patman_out = new File(alignedFile);

     PatmanParams newP_Params = new PatmanParams();
     newP_Params.setMaxGaps(0);
     newP_Params.setMaxMismatches(0);
     newP_Params.setPreProcess(false);
     newP_Params.setPostProcess(false);
     newP_Params.setMakeNR(false);

     cleanUpTranscriptome();
     File tFile = in_transcripts.getContainer(0).getData().toFile();
     File cleanTFile = new File(tFile.getName() + "_clean.fa");
     Thread myThread = new Thread(new PatmanRunner(cleanTFile, this.in_genome.getContainer(0).getData().getPath().toFile(),
     patman_out, Tools.getNextDirectory(), newP_Params));

     myThread.start();
     try {
     myThread.join();
     } catch (InterruptedException ex) {
     LOGGER.log(Level.SEVERE, null, ex);
     }

     return alignedFile;
     }*/
}
