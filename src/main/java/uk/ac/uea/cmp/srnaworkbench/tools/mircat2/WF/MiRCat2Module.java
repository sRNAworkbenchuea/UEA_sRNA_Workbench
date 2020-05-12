/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.PredictionException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
//import uk.ac.uea.cmp.srnaworkbench.tools.mircat.FX.miRCatController;
//import uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModule;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat2Params;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat2ServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.MiRCat2Main;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author keu13sgu
 */
public class MiRCat2Module extends WorkflowModule{

    private MiRCat2SceneController mirCat2Controller;
    
    private Path genome = null;
    private GenomeManager genomeManager = null;
    private static Path outPath = null;
    
    private final StopWatch globalStopWatch = new StopWatch();
    

    private MiRCat2ServiceLayer miRCat2DB = (MiRCat2ServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("miRCat2ServiceLayer");
    boolean isActive; // it was set to true
    private Path sRNAFilePath;
    private Path mirbasegff3;
    private boolean readyStatus = false;
    public boolean isPAREfirst = false;
    public static String paramsFile;
    
    // inputs
    private DataContainerList<HQLQuerySimple> in_sRNAQuery;
    private DataContainerList<GenomeManager> in_genome;
    // outputs
    
    private DataContainerList<HQLQuerySimple> out_predictionQuery;
    
    
    public MiRCat2Module(String id, String title,  Rectangle2D visualBounds, boolean isPAREfirst) {
        super(id, title);

        this.isPAREfirst = isPAREfirst;
        //setDefaultParams();
        if(this.isPAREfirst){
            // inputs
            this.in_genome = new DataContainerList<>("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);
            this.in_sRNAQuery = new DataContainerList<>("srnaQuery", WorkflowManager.CompatibilityKey.sRNA_QUERY, 1, 1);
            // outputs
            this.out_predictionQuery = new DataContainerList<>("predictionQuery", WorkflowManager.CompatibilityKey.PREDICTION_QUERY, 1, 1);
            try {
                this.out_predictionQuery.add(new DataContainer<>(WorkflowManager.CompatibilityKey.PREDICTION_QUERY, new HQLQuerySimple(Prediction_Entity.class)));//DetachedCriteria.forClass(Prediction_Entity.class)));
                addInputDataContainerList(this.in_genome);
                addInputDataContainerList(this.in_sRNAQuery);
                addOutputDataContainerList(this.out_predictionQuery);

                this.fxmlResource = "/fxml/MiRCat2Scene.fxml";
                this.controller = mirCat2Controller = new MiRCat2SceneController(visualBounds, this);   
       
            } catch (MaximumCapacityException | CompatibilityException | InitialisationException | DuplicateIDException ex) {
                LOGGER.log(Level.SEVERE, "Could not initialise miRCat2 module", ex);
                LOGGER.log(Level.SEVERE, Tools.getStackTrace(ex));
            }
            File outDir = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + id + "_output");
            if(!outDir.exists()){
                outDir.mkdir();
            }
            setOutputDir(outDir.toPath());
        }else{
            this.fxmlResource = "/fxml/MiRCat2Scene.fxml";
            this.controller = mirCat2Controller = new MiRCat2SceneController(visualBounds, this);   
        }
    
    }
    
    public void printLap(String label)
    {
        System.out.println(this.globalStopWatch.printLap(label));
    }
    

    
    public void setGenomePath(Path genome){
        this.genome = genome;
    }
    
    public static void setOutputDir(Path path){
        outPath = path;
    }
    public static Path getOutputDIR()
    {
        return outPath;
    }
        
    @Override
    protected synchronized void process() throws Exception {
        
        this.globalStopWatch.start();
        //genome file must be set before process() is called
        //Retrieve genome path from setup wizard
        if(!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine())
        {
            WorkflowSceneController.setWaitingNode(this.getID());
            if(!this.readyStatus)
                this.wait();
            WorkflowSceneController.setBusyNode(this.getID());
            mirCat2Controller.setBusy(true);
            

        }

        this.configureInputs();
        this.isActive = true;
      
        
        this.printLap("Wating for user config");
        if(this.isPAREfirst){
            String mirbase = "none";
            if(this.mirbasegff3 != null)
                mirbase = this.mirbasegff3.toString();
            MiRCat2Main mc2 = new MiRCat2Main(this.sRNAFilePath.toString(), this.genome.toString(),outPath.toString(), this.paramsFile, mirbase, ((MiRCat2SceneController) this.controller));
            parseMiRCat2Results(new File(outPath.toString()), getID(), this.in_genome.getContainer(0).getData());
            setOutputPredictionQuery();
        }else{
            readGenome(this.genome);
        
            this.printLap("Reading genome file");

                String header = "Precursor Score, Chromosome, Sequence, Abundance, Start,"
                        + " End, Strand, Mismatches, Hairpin Sequence,Hairpin Start, Hairpin End, "
                        + "Hairpin Dot-Bracket, Hairpin MFE, Hairpin aMFE, p-Value, "
                    + "Star Sequence, Star Abundance, Star Start, Star End, miRBase Precursor";

            String outputFileName = sRNAFilePath.getFileName().toString();
            int extIndex = outputFileName.lastIndexOf(".");

            if(extIndex > 0 ){
                outputFileName = outputFileName.substring(0, extIndex);
            }

            //outPath to be set from interface. 
            //This output method is temporary, will give more significant file names later
            try (BufferedWriter outPatman = new BufferedWriter(new FileWriter(outPath.toString() + DIR_SEPARATOR + outputFileName + "_output.patman"));
                 BufferedWriter outCSV = new BufferedWriter(new FileWriter(outPath.toString() + DIR_SEPARATOR + outputFileName + "_output.csv"));  )
            {
                outCSV.append(header);
                outCSV.append("\n");

                    this.miRCat2DB.process(this.genomeManager, outPatman, outCSV, mirCat2Controller);
                this.printLap("miRCat2 Processing reads");
                outPatman.flush();
                outCSV.flush();

                outPatman.close();
                outCSV.close();
            }
        }
        
        this.isActive = false;
        
        if(!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine())
        {
            mirCat2Controller.setBusy(false);
        }
    }
    
    public void parseMiRCat2Results(File dir, String id, GenomeManager g) throws FileNotFoundException, IOException, DuplicateIDException, Exception {
        // mircat2
        PredictionServiceImpl prediction_service = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");
        String outputFileName = sRNAFilePath.getFileName().toString();
        
//        // create precursors
//        List<String> structures = new LinkedList<>();
//        // get haipin sequences and structures
//        
//        File hairpinFile = new File(dir.toString() + IOUtils.DIR_SEPARATOR + outputFileName + "_output_miRNA_hairpins.txt");
//        Scanner hairpinFileIn = new Scanner(hairpinFile);
//        String current_sequence = "", current_structure = "";
//        while (hairpinFileIn.hasNextLine()) {
//            String line = hairpinFileIn.nextLine();
//            if (!line.startsWith(">")) {
//                if (line.contains("T") || line.contains("G") || line.contains("A") || line.contains("C")) {
//                    current_sequence = line.trim();
//                } else if (line.contains("(") || line.contains(")") || line.contains(".") || line.contains("{") || line.contains("=") || line.contains("}")) {
//                    current_structure = line.trim();
//                } else if (line.isEmpty()) {
//                    structures.add(current_sequence + ";" + current_structure);
//                }
//            }
//        }
//        hairpinFileIn.close();

        // get hairpin details
        //int hpCounter = 0;
        int nPredictions = 0;
        File detailFile = new File(dir.toString() + IOUtils.DIR_SEPARATOR + outputFileName.replace(".fa", "_output.csv") );
//        detailFile = new File(dir.toString() + IOUtils.DIR_SEPARATOR + outputFileName.replace(".fa", "_output copy.csv") );
        //detailFile = new File("/Users/salmayz/Documents/Workbench_mirpare_modified_move_to_repository/target/release/User/miRPARE_Data/miRCat22_output/ath_WTA_chr1_output.csv" );
        System.out.println("miRCat2 output file name: " + detailFile.getAbsolutePath());
        Scanner detailFileIn = new Scanner(detailFile);
        detailFileIn.nextLine(); // skip file header
        while (detailFileIn.hasNextLine()) {
            String detailLine = detailFileIn.nextLine().trim();
            if (!AppUtils.INSTANCE.isCommandLine()&& mirCat2Controller != null) {
                mirCat2Controller.outputToGUI(detailLine);
            }
            try {
                List<Prediction_Entity> predictions = parseMiRCatResultLine(detailLine);
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
            //hpCounter++;
        }
        System.out.println("nPredictions: " + nPredictions);
    }
    
    private List<Prediction_Entity> parseMiRCatResultLine(String line) throws IOException, PredictionException, FileNotFoundException, InterruptedException {
        // mircat2
        List<Prediction_Entity> predictions = new ArrayList<>();
        GenomeManager genomemanager = this.in_genome.getContainer(0).getData();

        String[] detailComponents = line.split(",");
        // get precursor score
        double precursorScore = Double.parseDouble(detailComponents[0].trim());
        //System.out.println("Precursor score = " + precursorScore);
        // get chromosome
        String chromosome = detailComponents[1].trim();
        // get mature start and end indices
        int mature_sIndex = Integer.parseInt(detailComponents[4].trim());
        int mature_eIndex = Integer.parseInt(detailComponents[5].trim());
        // get strand
        String strand = detailComponents[6].trim().charAt(0) + "";
        GenomeManager.DIR gdir = GenomeManager.DIR.POSITIVE;
        if (strand.equals("-")) {
            gdir = GenomeManager.DIR.NEGATIVE;
        }
        // get mature sequence ( 5'->3' on approproate strand )
        String mature_sequence = detailComponents[2].trim();
        // check mature sequence is correct
        String mature_sequence_from_alignment = genomemanager.getDNA(chromosome, mature_sIndex, mature_eIndex, gdir);
        if (!mature_sequence.equals(mature_sequence_from_alignment)) {
            throw new PredictionException("ERROR: Mature sequence from file does not match mature sequence from alignment!");
        }
        // get MFE
        double mfe = Double.parseDouble(detailComponents[12].trim());
        double aMFE = Double.parseDouble(detailComponents[13].trim());
        String star_sequence = detailComponents[15].trim();
        // set-up precursor
        //String[] structure = structureStr.split(";");
        String precursor_sequence = detailComponents[8]; // from 5'->3' on appropraite strand
        String precursor_structure = detailComponents[11]; // from 5'->3' on appropraite strand
        String mod_precursor_structure = precursor_structure;
        mod_precursor_structure = mod_precursor_structure.replaceAll("[>}]", ")");
        mod_precursor_structure = mod_precursor_structure.replaceAll("[<{]", "(");
        mod_precursor_structure = mod_precursor_structure.replaceAll("[-=]", ".");
        // check lengths of sequence and strucutre match
        if (precursor_sequence.length() != mod_precursor_structure.length()) {
            System.out.println("ERROR: Precursor sequence and structure lengths do not match!");
            throw new PredictionException("ERROR: Precursor sequence and structure lengths do not match!");
        }
        int hairpin_sIndex = Integer.parseInt(detailComponents[9].trim());
        int hairpin_eIndex = Integer.parseInt(detailComponents[10].trim());
        if(gdir == GenomeManager.DIR.NEGATIVE){
            precursor_sequence = genomemanager.getDNA(chromosome, hairpin_sIndex, hairpin_eIndex, gdir);
            //System.out.println(precursor_sequence);
        }
        if (precursor_sequence.length() != mod_precursor_structure.length()) {
            System.out.println("ERROR: Precursor sequence and structure lengths do not match!");
            throw new PredictionException("ERROR: Precursor sequence and structure lengths do not match!");
        }
        //String precursor_sequence_from_alignment1 = genomemanager.getDNA(chromosome, hairpin_sIndex, hairpin_eIndex, gdir);
        //System.out.println(chromosome + "\t" + gdir +" , hp start: " + hairpin_sIndex + ",   hp end: " +hairpin_eIndex);
        // check sequence 
        int extended_sIndex = Math.max(0, mature_sIndex - precursor_sequence.length());
        int extended_eIndex = Math.min(genomemanager.getChrLength(chromosome) - 1, mature_sIndex + precursor_sequence.length());
        String extendedWindow = genomemanager.getDNA(chromosome, extended_sIndex, extended_eIndex, gdir);
        int offset = extendedWindow.indexOf(precursor_sequence);
        if (gdir == GenomeManager.DIR.POSITIVE) {
            hairpin_sIndex = extended_sIndex + offset;
            hairpin_eIndex = hairpin_sIndex + precursor_sequence.length() - 1;
        } else {
            hairpin_eIndex = extended_eIndex - offset;
            hairpin_sIndex = hairpin_eIndex - precursor_sequence.length() + 1;
        }
            //System.out.println(" hp start: " + hairpin_sIndex + ",   hp end: " +hairpin_eIndex);
                
                
        String precursor_sequence_from_alignment = genomemanager.getDNA(chromosome, hairpin_sIndex, hairpin_eIndex, gdir);
        if (!precursor_sequence.equals(precursor_sequence_from_alignment)) {
            throw new PredictionException("ERROR: Precursor sequence and sequence from alignment do not match!");
        }
  
        Precursor_Entity precursor = new Precursor_Entity(precursorScore, precursor_sequence, mod_precursor_structure, chromosome, hairpin_sIndex, hairpin_eIndex, strand, mfe, aMFE);
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

        //for (int i = 0; i < starCandidates.length; i++) {
            // set-up prediction
            Prediction_Entity prediction = new Prediction_Entity();
            prediction.setPrecursor(precursor);
            prediction.setPredictor(getID());
            // set-up mature
            prediction.setMature(matureAlignment);
            // set-up star
//            String star_sequence = starCandidates[i].split("[()]", 2)[0];
            if (!star_sequence.equals("-1") && !star_sequence.equals("N/A")) {
                int star_offset_index = precursor_sequence.indexOf(star_sequence);
                
                int star_sIndex = Integer.parseInt(detailComponents[17].trim());
                int star_eIndex = Integer.parseInt(detailComponents[18].trim());
                if(star_offset_index == -1){
                    star_sequence = genomemanager.getDNA(chromosome, star_sIndex, star_eIndex, gdir);
                }
                star_offset_index = precursor_sequence.indexOf(star_sequence);
                
                if (gdir == GenomeManager.DIR.POSITIVE) {
                    star_sIndex = hairpin_sIndex + star_offset_index;
                    star_eIndex = star_sIndex + star_sequence.length() - 1;

                } else {
                    star_eIndex = hairpin_eIndex - star_offset_index;
                    star_sIndex = star_eIndex - star_sequence.length() + 1;
                }
                // check star sequence
                String star_from_alignment = genomemanager.getDNA(chromosome, star_sIndex, star_eIndex, gdir);
//                System.out.println("1: " + star_sequence);
//                System.out.println("2: " + star_from_alignment);
//                System.out.println(star_sIndex + "-" +star_eIndex);
                if (!star_sequence.equals(star_from_alignment)) {
                    System.out.println("ERROR: Star sequence and star sequence from alignment do not match!");
                    throw new PredictionException("ERROR: Star sequence and star sequence from alignment do not match!");
                }
                

                Aligned_Sequences_Entity starAlignment = new Aligned_Sequences_Entity(Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING, chromosome, star_sequence, star_sIndex, star_eIndex, strand, 0);

                prediction.setStar(starAlignment);
            }
            predictions.add(prediction);
            

        //}
        return predictions;

    }
    
    public void setOutputPredictionQuery() throws HQLQuery.HQLQueryLockedException
    {
         HQLQuerySimple q = this.out_predictionQuery.getContainer(0).getData();
            q.addWhere("A.predictor = '" + getID() + "'");
            q.lock();
    }
    
   
    
    public synchronized void setReadyToContinue(boolean readyStatus)
    {
        if (readyStatus)
        {
            this.readyStatus = readyStatus;
            this.notifyAll();
        }
    }

    public boolean readyToContinue() {
        return this.isActive;
    }
    
     public void setGenome(GenomeManager genomeFile){
        this.genomeManager = genomeFile;
    }
    
      public void readGenome(Path genomeFile){
        try {
            this.genomeManager = new GenomeManager(genomeFile);
        } catch (IOException ex) {
            Logger.getLogger(MiRCat2ServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DuplicateIDException ex) {
            Logger.getLogger(MiRCat2ServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
      public GenomeManager getGenome(){
          return this.genomeManager;
      }

    
    
    public static boolean readParams(String paramFile) throws IOException {
        paramsFile = paramFile;
        
            BufferedReader in = new BufferedReader(new FileReader(new File(paramFile)));
            String line;

            while ((line = in.readLine()) != null) {
                if(line.trim().isEmpty())
                    continue;
                String parts[] = line.split("=");
                switch(parts[0].trim().toLowerCase()){
                    case "repeats": 
                        MiRCat2Params.REPEATS = Integer.parseInt(parts[1].trim());
                        break;

                    case "complex": 
                        MiRCat2Params.complex = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "pval":
                        MiRCat2Params.pVal = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "minlen":
                        MiRCat2Params.minLen = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "maxlen":
                        MiRCat2Params.maxLen = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "minfoldlen":
                        MiRCat2Params.minFoldLen = Integer.parseInt(parts[1].trim());
                        break;
                        
//                    case "maxfoldlen":
//                        MiRCat2Params.lFoldL = Integer.parseInt(parts[1].trim());
//                        break;
                        
                    case "maxamfe":
                        MiRCat2Params.maxAmfe = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "gapsinmirna":
                        MiRCat2Params.gapsInMirna = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "mirstarpresent":
                        MiRCat2Params.miRStarPresent = (parts[1].trim().equals("true"));
                        break;
                        
                    case "maxfoldlen":
                        MiRCat2Params.foldDist = Integer.parseInt(parts[1].trim());
                        MiRCat2Params.lFoldL = MiRCat2Params.foldDist;
                        break;
                        
//                    case "window":
//                        MiRCat2Params.WINDOW = Integer.parseInt(parts[1].trim());
//                        break;
                        
                    case "allowcomplexloop":
                        MiRCat2Params.allowComplexLoop = (parts[1].trim().equals("true"));
                        break;
                        
                    case "noloops":
                        MiRCat2Params.noLoops = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "rudval":
                        MiRCat2Params.UDVAL = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "clearcutpercent":
                        MiRCat2Params.clearCutPercent = Double.parseDouble(parts[1].trim());
                        break;
                       
                    case "randfold":
                        MiRCat2Params.execRANDFold = (parts[1].trim().equals("true"));
                        break;
                            
                    default:
                        System.out.println("Param " + parts[0] + " is not a valid parameter");
                        return false;
                }
            }
        return true;
    }
    
    public void setSpecificParams(boolean isPlant){
        if(isPlant){
            MiRCat2Params.WINDOW = 500;
        }
        else{
            MiRCat2Params.WINDOW = 300;
        }
    }

    public void write_miRNA_FASTA(Path toPath){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void write_Precursor_FASTA(Path toPath){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void writeTableCSV(Path toPath){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean configureInputs(){
        String next = FileHierarchyViewController.retrieveDataPaths().keySet().iterator().next();
        this.sRNAFilePath = FileHierarchyViewController.retrieveDataPaths().get(next).get(0);
        setGenomePath(FileHierarchyViewController.retrieveGenomePath());
        if(!FileHierarchyViewController.retrieveGFFPaths().isEmpty()){
            this.mirbasegff3 = FileHierarchyViewController.retrieveGFFPaths().get(0);
        }
        if (genome != null && outPath != null && Files.exists(genome) && Files.isDirectory(outPath)){
            return true;
        }
        if (!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine()){
            Platform.runLater(() ->
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("miRCat 2 Error State");
                alert.setContentText("miRCat has not been configured. Please set the genome and output path and check the parameters");

                alert.showAndWait();
            });
            LOGGER.log(Level.SEVERE, "miRCat has not been configured. Please set the genome and output path and check the parameters");
        }
        else{
            LOGGER.log(Level.SEVERE, "miRCat has not been configured. Please set the genome and output path and check the parameters");

        }
        return false;
    }

     
    
}
