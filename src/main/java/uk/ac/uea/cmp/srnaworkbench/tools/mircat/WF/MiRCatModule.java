package uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.springframework.beans.BeansException;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.PredictionException;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.DB.MirCatProcessor;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.FX.miRCatController;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/*
Run from CLI --tool mircat -srna_file TutorialData/FASTA/RNAOME/GSM118373_Rajagopalan_col0_leaf_nr.fa -genome /Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas  -pcw -basespace
*/


public class MiRCatModule extends WorkflowModule {

    private miRCatController mirCatController;
    // inputs
    private final DataContainerList<HQLQuerySimple> in_sRNAQuery;
    private final DataContainerList<GenomeManager> in_genome;
    // outputs

    private final DataContainerList<HQLQuerySimple> out_predictionQuery;
    private File outDir;
    private MiRCatParams params;
    private boolean complete;
    private long startTime;
    private boolean writeToFileMode = true;
    private boolean runDatabaseMode = false;
    private boolean outputToFileMode = true;
    
    MirCatProcessor miRCat = (MirCatProcessor) DatabaseWorkflowModule.getInstance().getContext().getBean("MiRCatServiceLayer");


    public MiRCatModule(String id, String title, Rectangle2D visualBounds) {
        super(id, title);
        complete = false;

        this.outDir = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + id + "_output");
        this.outDir.mkdirs();


     
        this.params = MiRCatParams.createDefaultPlantParams();
        
        if(DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            params.setLengthRange(16, 35);
        }

        // inputs
        this.in_genome = new DataContainerList<>("genome", CompatibilityKey.GENOME, 1, 1);
        this.in_sRNAQuery = new DataContainerList<>("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, 1);
        // outputs
        this.out_predictionQuery = new DataContainerList<>("predictionQuery", CompatibilityKey.PREDICTION_QUERY, 1, 1);
        try {
            this.out_predictionQuery.add(new DataContainer<>(CompatibilityKey.PREDICTION_QUERY, new HQLQuerySimple(Prediction_Entity.class)));//DetachedCriteria.forClass(Prediction_Entity.class)));
            addInputDataContainerList(this.in_genome);
            addInputDataContainerList(this.in_sRNAQuery);
            addOutputDataContainerList(this.out_predictionQuery);
            setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + "miRCatScene.fxml");
            this.controller = mirCatController = new miRCatController(this);
        } catch (MaximumCapacityException | CompatibilityException | InitialisationException | DuplicateIDException ex) {
            LOGGER.log(Level.SEVERE, "Could not initialise miRCat module", ex);
            LOGGER.log(Level.SEVERE, Tools.getStackTrace(ex));
        }
    }

    public MiRCatParams getParameters() {
        return this.params;
    }

    
    
    public miRCatController getMiRCatController() {
        return mirCatController;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public void setParameters(MiRCatParams params) {
        this.params = params;
        mirCatController.updateUI();
    }

    @Override
    public synchronized void process() throws Exception {
        startTime = System.currentTimeMillis();
        if (!runDatabaseMode)
        {
            writeToFileRun();
        } else {
            databaseModeRun();
            //this.wait();
            LOGGER.log(Level.INFO, "miRCat Population complete...");
            
            while(!miRCat.getReadyToContinue())//replace this with progress update
            {
                System.out.println("Tasks remaining: " + miRCat.getWaitingTasks());
//                System.out.println("Processing.");
//                System.out.print("\b\b\b\b\b\b\b\b\b\b\b");
//                System.out.println("Processing..");
//                System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b");
//                System.out.println("Processing...");
//                System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b");
            }
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("miRCat time: " + elapsedTime);

    }

    public synchronized void notifyComplete() {
        this.notifyAll();
    }

    /**
     * Checks all files in the database for miRNAs
     *
     * @throws Exception
     */
    private synchronized void databaseModeRun() throws Exception {
        Map<String, List<Path>> samples = DatabaseWorkflowModule.getInstance().getSamples();
        for (Entry<String, List<Path>> sample : samples.entrySet()) {
            for (Path filename : sample.getValue()) {
                
                //        HQLQuerySimple alignmentQuery = new HQLQuerySimple(Aligned_Sequences_Entity.class);
                //       filterByLengthQuery.addWhere("length(A.rna)");
//                DataContainer<HQLQuerySimple> container = this.in_sRNAQuery.getContainer(0);
//                HQLQuerySimple data = container.getData();
//                String eval = data.eval();
                //String eval = this.in_sRNAQuery.getContainer(0).getData().eval();
                
                //StringBuilder sql = new StringBuilder(this.in_sRNAQuery.getContainer(0).getData().eval());
                
                //StringBuilder sql = new StringBuilder();

//                sql.append("SELECT * FROM ALIGNED_SEQUENCES "
//                        + "WHERE CHAR_LENGTH(ALIGNED_SEQUENCES.RNA_SEQUENCE)>=" + params.getMinLength() + " "
//                        + "AND CHAR_LENGTH(ALIGNED_SEQUENCES.RNA_SEQUENCE)<=" + params.getMaxLength() + " "
//                        + "ORDER BY chrom, Seq_Start");
                
                
                HQLQuerySimple q = new HQLQuerySimple(Aligned_Sequences_Entity.class);
                q.addWhere("CHAR_LENGTH(A.rna_sequence)>=" + params.getMinLength() + " " +
                         "AND CHAR_LENGTH(A.rna_sequence)<=" + params.getMaxLength() + " ");
                q.addOrder("A.id.chrom", HQLQuery.HQL_DIR.ASC);
                q.addOrder("A.id.start", HQLQuery.HQL_DIR.ASC);
                
                WorkflowManager.getInstance().addInputData("srnaQuery", CompatibilityKey.sRNA_QUERY, q);

                
//                
//                if(DatabaseWorkflowModule.getInstance().isDebugMode())
//                {
//                    writeToFileMode = true;
//                    outputDir = new File( "/Developer/Applications/sRNAWorkbench/TestingData/DB_MC_test");
//                }
                
                if (this.outputToFileMode)
                {
                    Path outputCSVPath = Paths.get(outDir.getAbsolutePath() + DIR_SEPARATOR + "output.csv");
                    Path outputHairpinPath = Paths.get(outDir.getAbsolutePath() + DIR_SEPARATOR + "miRNA_hairpins.txt");
                    Path outputmiRNAPath = Paths.get(outDir.getAbsolutePath() + DIR_SEPARATOR + "miRNA.fa");

                    if (Files.exists(outputCSVPath))
                    {
                        Files.delete(outputCSVPath);
                    }
                    if (Files.exists(outputHairpinPath))
                    {
                        Files.delete(outputHairpinPath);
                    }
                    if (Files.exists(outputmiRNAPath))
                    {
                        Files.delete(outputmiRNAPath);
                    }
                    if(!Files.isDirectory(outDir.toPath()))
                    {
                        Files.createDirectory(outDir.toPath());
                    }
                }
                
                

                miRCat.setupMirCatProcessor(new MiRCatParams(), this.in_genome.getContainer(0).getData(), filename.getFileName().toString(), 
                        this.outputToFileMode, outDir.getAbsolutePath(),null,this);
                miRCat.categorise_miRNAs();

                //this.wait();

            }
        }

    }
    
    
    
    private List<Prediction_Entity> parseMiRCatResultLine(String line, String structureStr) throws IOException, PredictionException, FileNotFoundException, InterruptedException {
        List<Prediction_Entity> predictions = new ArrayList<>();
        GenomeManager genomeManager = this.in_genome.getContainer(0).getData();

        String[] detailComponents = line.split(",");
        // get chromosome
        String chromosome = detailComponents[0].trim();
        // get mature start and end indices
        int mature_sIndex = Integer.parseInt(detailComponents[1].trim());
        int mature_eIndex = Integer.parseInt(detailComponents[2].trim());
        // get strand
        String strand = detailComponents[3].trim().charAt(0) + "";
        GenomeManager.DIR gdir = GenomeManager.DIR.POSITIVE;
        if (strand.equals("-")) {
            gdir = GenomeManager.DIR.NEGATIVE;
        }
        // get mature sequence ( 5'->3' on approproate strand )
        String mature_sequence = detailComponents[5].trim();
        // check mature sequence is correct
        String mature_sequence_from_alignment = genomeManager.getDNA(chromosome, mature_sIndex, mature_eIndex, gdir);
        if (!mature_sequence.equals(mature_sequence_from_alignment)) {
            throw new PredictionException("ERROR: Mature sequence from file does not match mature sequence from alignment!");
        }
        // get MFE
        double mfe = Double.parseDouble(detailComponents[10].trim());
        String[] starCandidates = detailComponents[12].split(" ");
        // set-up precursor
        String[] structure = structureStr.split(";");
        String precursor_sequence = structure[0]; // from 5'->3' on appropraite strand
        String precursor_structure = structure[1]; // from 5'->3' on appropraite strand
        String mod_precursor_structure = precursor_structure;
        mod_precursor_structure = mod_precursor_structure.replaceAll("[>}]", ")");
        mod_precursor_structure = mod_precursor_structure.replaceAll("[<{]", "(");
        mod_precursor_structure = mod_precursor_structure.replaceAll("[-=]", ".");
        // check lengths of sequence and strucutre match
        if (precursor_sequence.length() != mod_precursor_structure.length()) {
            throw new PredictionException("ERROR: Precursor sequence and structure lengths do not match!");
        }
        int hairpin_sIndex;
        int hairpin_eIndex;
        int extended_sIndex = Math.max(0, mature_sIndex - precursor_sequence.length());
        int extended_eIndex = Math.min(genomeManager.getChrLength(chromosome) - 1, mature_sIndex + precursor_sequence.length());
        String extendedWindow = genomeManager.getDNA(chromosome, extended_sIndex, extended_eIndex, gdir);
        int offset = extendedWindow.indexOf(precursor_sequence);
        if (gdir == GenomeManager.DIR.POSITIVE) {

            hairpin_sIndex = extended_sIndex + offset;
            hairpin_eIndex = hairpin_sIndex + precursor_sequence.length() - 1;
        } else {
            hairpin_eIndex = extended_eIndex - offset;
            hairpin_sIndex = hairpin_eIndex - precursor_sequence.length() + 1;
        }
        // check sequence 
        String precursor_sequence_from_alignment = genomeManager.getDNA(chromosome, hairpin_sIndex, hairpin_eIndex, gdir);
        if (!precursor_sequence.equals(precursor_sequence_from_alignment)) {
            throw new PredictionException("ERROR: Precursor sequence and sequence from alignment do not match!");
        }

        Precursor_Entity precursor = new Precursor_Entity(precursor_sequence, mod_precursor_structure, chromosome, hairpin_sIndex, hairpin_eIndex, strand, mfe);
        /*  double e = precursor.getMFE();
         precursor.computeMFE();
         if(e != precursor.getMFE())
         {
         System.out.println("MFE ERROR!!!!!!!!!!!!!!!!!!!!!!!");
         System.out.println(e);
         System.out.println(precursor.getMFE());
         }*/

// set-up mature
        Aligned_Sequences_Entity matureAlignment = new Aligned_Sequences_Entity(Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING, chromosome, mature_sequence, mature_sIndex, mature_eIndex, strand, 0);

        for (int i = 0; i < starCandidates.length; i++) {
            // set-up prediction
            Prediction_Entity prediction = new Prediction_Entity();
            prediction.setPrecursor(precursor);
            prediction.setPredictor(getID());
            // set-up mature
            prediction.setMature(matureAlignment);
            // set-up star
            String star_sequence = starCandidates[i].split("[()]", 2)[0];
            if (!star_sequence.equals("NO")) {
                int star_offset_index = precursor_sequence.indexOf(star_sequence);
                int star_sIndex;
                int star_eIndex;
                if (gdir == GenomeManager.DIR.POSITIVE) {
                    star_sIndex = hairpin_sIndex + star_offset_index;
                    star_eIndex = star_sIndex + star_sequence.length() - 1;

                } else {
                    star_eIndex = hairpin_eIndex - star_offset_index;
                    star_sIndex = star_eIndex - star_sequence.length() + 1;
                }

                // check star sequence
                String star_from_alignment = genomeManager.getDNA(chromosome, star_sIndex, star_eIndex, gdir);
                if (!star_sequence.equals(star_from_alignment)) {
                    throw new PredictionException("ERROR: Star sequence and star sequence from alignment do not match!");
                }
                Aligned_Sequences_Entity starAlignment = new Aligned_Sequences_Entity(Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING, chromosome, star_sequence, star_sIndex, star_eIndex, strand, 0);

                prediction.setStar(starAlignment);
            }
            predictions.add(prediction);
            

        }
        return predictions;

    }

    public void parseMiRCatResults(File dir, String id, GenomeManager g) throws FileNotFoundException, IOException, DuplicateIDException, Exception {

        PredictionServiceImpl prediction_service = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");

        // create precursors
        List<String> structures = new LinkedList<>();
        // get haipin sequences and structures
        File hairpinFile = new File(dir.toString() + IOUtils.DIR_SEPARATOR + "miRNA_hairpins.txt");
        Scanner hairpinFileIn = new Scanner(hairpinFile);
        String current_sequence = "", current_structure = "";
        while (hairpinFileIn.hasNextLine()) {
            String line = hairpinFileIn.nextLine();
            if (!line.startsWith(">")) {
                if (line.contains("T") || line.contains("G") || line.contains("A") || line.contains("C")) {
                    current_sequence = line.trim();
                } else if (line.contains("(") || line.contains(")") || line.contains(".") || line.contains("{") || line.contains("=") || line.contains("}")) {
                    current_structure = line.trim();
                } else if (line.isEmpty()) {
                    structures.add(current_sequence + ";" + current_structure);
                }
            }
        }
        hairpinFileIn.close();

        // get hairpin details
        int hpCounter = 0;
        int nPredictions = 0;
        File detailFile = new File(dir.toString() + IOUtils.DIR_SEPARATOR + "output.csv");
        Scanner detailFileIn = new Scanner(detailFile);
        detailFileIn.nextLine(); // skip file header
        while (detailFileIn.hasNextLine()) {
            String detailLine = detailFileIn.nextLine().trim();
            try {
                List<Prediction_Entity> predictions = parseMiRCatResultLine(detailLine, structures.get(hpCounter));
                for (Prediction_Entity prediction : predictions) {
                    nPredictions++;
                    try {
                        prediction_service.saveOrUpdate(prediction);
                    } catch (Exception ex) {
                        System.out.println("MIRCAT ERROR: Was unable to persist prediction to DB. Ignoring record: " + ex);
                        LOGGER.log(Level.WARNING, "MIRCAT ERROR: Was unable to persist prediction to DB. Ignoring record: {0}", ex);
                    }
                }
            } catch (IOException | PredictionException ex) {
                System.out.println("MIRCAT ERROR: Was unable to persist prediction to DB. Ignoring record: " + ex);
                LOGGER.log(Level.WARNING, "MIRCAT ERROR: Was unable to persist prediction to DB. Ignoring record: {0}", ex);
            }
            hpCounter++;
        }
        System.out.println("nPredictions: " + nPredictions);
    }

    private void writeToFileRun() {
        try {
            mirCatController.write2Log("INFORMATION: miRCat module started.");
            mirCatController.write2Log("INFORMATION: writing parameters to file:");

            // output mircat params to file
            File paramsFile = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + getID() + "_params.cfg");

            this.params.save(paramsFile);
           // mirCatController.write2Log(this.params.toDescriptiveString());

            mirCatController.write2Log("INFORMATION: Prediction started.");
            ToolBox tool = ToolBox.getToolForName("mircat");
            Map<String, String> args = new HashMap();

            // retrieve the srnas
            SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");

            List<Sequence_Entity> sequences = sequence_service.executeSQL(this.in_sRNAQuery.getContainer(0).getData().eval());
            // write sequences to file
            File inputSequencesFile = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences.fa");
            PrintWriter writer = new PrintWriter(inputSequencesFile);
            for (Sequence_Entity sequence : sequences) {
                for (int i = 0; i < sequence.getAbundance(); i++) {
                    writer.println(">" + sequence.getRNA_Sequence());
                    writer.println(sequence.getRNA_Sequence());
                }
            }
            writer.close();
            File inputSequencesFileFormatted = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + getID() + "_input_sequences_formatted.fa");
            parseFA(inputSequencesFile, inputSequencesFileFormatted);

            args.put("srna_file", inputSequencesFileFormatted.getAbsolutePath());
            args.put("genome", this.in_genome.getContainer(0).getData().getPath().toString());
            args.put("params", paramsFile.getAbsolutePath());
            args.put("output_directory", this.outDir.getAbsolutePath());
            tool.startTool(args);

            mirCatController.write2Log("INFORMATION: Prediction completed.");

            mirCatController.write2Log("INFORMATION: Processing results.");
            parseMiRCatResults(this.outDir, getID(), this.in_genome.getContainer(0).getData());
            //  parseResults();
          //  String query = "FROM Prediction_Entity A WHERE A.predictor = '" + getID() + "'";
            
            setOutputPredictionQuery();
            //HQLQuerySimple q = this.out_predictionQuery.getContainer(0).getData();
            //q.addWhere("A.predictor = '" + getID() + "'");
            //q.lock();
            //this.out_predictionQuery.getContainer(0).getData().append(query);

            //inputSequencesFile.delete();
            //inputSequencesFileFormatted.delete();
            mirCatController.write2Log("INFORMATION: miRCat module completed.");
            this.complete = true;

            mirCatController.updateUI();

        } catch (InterruptedException | IOException | BeansException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (DuplicateIDException ex) {
            Logger.getLogger(MiRCatModule.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MiRCatModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOutputPredictionQuery() throws HQLQuery.HQLQueryLockedException
    {
         HQLQuerySimple q = this.out_predictionQuery.getContainer(0).getData();
            q.addWhere("A.predictor = '" + getID() + "'");
            q.lock();
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

    public JsonObject getJSON() {
        JsonObjectBuilder model = Json.createObjectBuilder();
        model.add("id", getID());
        model.add("description", "mircat");
        return model.build();
    }

    public HQLQuery generateOutput() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        HQLQueryComplex hqlQuery = new HQLQueryComplex();
        hqlQuery.addSelect("P.precursor.alignment.id.chrom", "Chr");
        hqlQuery.addSelect("P.precursor.alignment.id.start", "Start");
        hqlQuery.addSelect("P.precursor.alignment.id.end", "End");
        hqlQuery.addSelect("P.precursor.alignment.id.strand", "Strand");
        hqlQuery.addSelect("concat(P.precursor.alignment.rna_sequence, concat('" + IOUtils.LINE_SEPARATOR + "', P.precursor.structure))", "Hairpin");
        hqlQuery.addSelect("P.precursor.mfe", "MFE");
        hqlQuery.addSelect("P.mature.rna_sequence", "Mature");
        hqlQuery.addSelect("P.mature.id.start", "Mature_Start");
        hqlQuery.addSelect("P.mature.id.end", "Mature_End");
        hqlQuery.addSelect("P.star.rna_sequence", "Star");
        hqlQuery.addSelect("P.star.id.start", "Star_Start");
        hqlQuery.addSelect("P.star.id.end", "Star_End");
        hqlQuery.addFrom(Prediction_Entity.class, "P");
        hqlQuery.addWhere("P.predictor = '" + this.getID() + "'");

        return hqlQuery;

    }
    
     /*
     * *GETTERS AND SETTERS
     */

    /**
     * 
     * @return 
     */
    public boolean isWriteToFileMode()
    {
        return writeToFileMode;
    }

    public void setWriteToFileMode(boolean writeToFileMode)
    {
        this.writeToFileMode = writeToFileMode;
    }

    public boolean isRunDatabaseMode()
    {
        return runDatabaseMode;
    }

    public void setRunDatabaseMode(boolean runDatabaseMode)
    {
        this.runDatabaseMode = runDatabaseMode;
    }

    public boolean isOutputToFileMode()
    {
        return outputToFileMode;
    }

    public void setOutputToFileMode(boolean outputToFileMode)
    {
        this.outputToFileMode = outputToFileMode;
    }
    
    

    public File getOutputDir()
    {
        return outDir;
    }

    public void setOutputDir(File outputDir)
    {
        this.outDir = outputDir;
    }
    
    public void setThreadCount(int count)
    {
        MirCatProcessor.setTotalThread(count);
    }

    boolean readyToContinue()
    {
        return miRCat.getReadyToContinue();
    }
    
   
    
}
