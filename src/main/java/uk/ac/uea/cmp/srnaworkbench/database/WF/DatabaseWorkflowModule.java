package uk.ac.uea.cmp.srnaworkbench.database.WF;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ProgressIndicator;
import javax.swing.JFrame;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.CannotCreateTransactionException;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.DuplicateReferenceException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.TranscriptServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import uk.ac.uea.cmp.srnaworkbench.io.externalops.ExternalSort;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.DatabaseAnnotationTool;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.FastaAnnotationParams;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.FastaAnnotationWorkflow;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.DifferentialExpressionService;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.DifferentialExpressionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.StrandBiasDivergenceTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController.EntryPoint;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF.MiRCat2Module;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution.AbundanceDistributionParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution.AbundanceDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceSizeClass.SizeClassDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard.JaccardTableParams;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard.JaccardTableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.DatabaseMATool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MValueDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.nucleotideSizeClassService.NucleotideSizeClassDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.aligners.BowtieRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;
import static uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanStream.patman_SeqLexicomparator;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author w0445959
 */
public class DatabaseWorkflowModule extends WorkflowModule {
    

    

    //charset for input data 
    private static final Charset charset = Charset.forName("UTF-8");

    //Service objects
    private static final ApplicationContext applicationContext;
    private static final SequenceServiceImpl sequenceService;
    private static final FilenameServiceImpl filenameService;

    private static final AlignedSequenceServiceImpl aligned_seqService;
    private static final UniqueSequencesServiceImpl unique_seqService;
    private static final AnnotationService annotationService;



    private static int totalUniqueReads = 0;
    
    private int alignmentFilterValue = 200;
    private boolean filterRepeats = true;

    private static Path myGenomePath = null;
     private static Path myTranscriptomePath = null; // chris added

    private static Map<String, List<Path>> mySamples = null;
    private static Path mappingFile = null;
    private static List<Path> annotationFiles = new ArrayList<>(); // field used for debugging purposes
    private static List<String> annotationTypes = new ArrayList<>();
    private static Path combinedFile;

    //list of alignments that hit the genome too many times
    private static HashMap<Aligned_Sequences_Entity.Id, Integer> alignmentsToModify = new HashMap<>();

    private static boolean isConfigured = false;

    private ProgressIndicator databaseProgressInd;

    private static DatabaseWorkflowModule instance;
    
    private boolean debugMode = false;
    private boolean timed = false;
    
    //patman is default mapper
    private Mapper mapper = Mapper.PATMAN;

    private static int gaps,mismatch =0;
    

    private DatabaseWorkflowModule(String id) {
        super(id, "Database");
        
        this.fxmlResource = "/fxml/FileHierarchyView.fxml";
        
        this.globalStopWatch.start();
        timed = true;
        this.controller = new FileHierarchyViewController(Rectangle2D.EMPTY, this);

//        this.sessionFactory = new org.hibernate.cfg.Configuration().configure("/configuration/app-config.xml").buildSessionFactory();
       
        //System.out.println("resource: " + fxmlResource);
    }
    
    public Map<String, List<Path>> getSamples()
    {
        return mySamples;
    }
    
    private double lowestComplexity = 0.0;
    /**
     *  Temporary setting to weed out silly sequences such
     as TTTTTTTTTTTTTTTTTAG. lowestComplexity is the minimum percentage
     that at least three nucleotides must make for the sequence to be used.
     This was intended to weed out useless sequences that mapped a lot before
     taking the time to map them but
     it turns out these are not the sequences that are mapping the most, so
     it makes very little difference in this aspect. This is turned off
     * (set to 0.0) by default.
     * @param lowestPercentage
     **/
    public void setLowestComplexity(double lowestPercentage)
    {
        this.lowestComplexity = lowestPercentage;
    }
    
    /**
     * Call this method if not the entry point to a new workflow 
     * (i.e.) the FileManager has been selected
     */
    public void setNotFirst()
    {
        
    }
    
    public static List<String> getFileIDs() {
        return filenameService.getFileIDs();
    }
    
    private final StopWatch globalStopWatch = new StopWatch();
    public void printLap(String label)
    {
        if(this.timed)
        System.out.println(this.globalStopWatch.printLap(label));
    }
    public void printTotal()
    {
        if(this.timed)
            this.globalStopWatch.printTimes();
    }
    
    /**
     * Convenience method for
     * @param label 
     */
    public static void lap(String label)
    {
        DatabaseWorkflowModule.getInstance().printLap(label);
    }
    
    /**
     * Produces a silent lap (unprinted) which can be used to clear the current timing
     * before timing something else
     * @param label 
     */
    public static void lap()
    {
        DatabaseWorkflowModule.getInstance().globalStopWatch.lap();
    }

    // JsonFactory for this database.
    // There should only be one instance of JsonFactory with which all
    // JsonGenerators are created.
    private JsonFactory jsonFactory = new JsonFactory();
    
//    private SessionFactory sessionFactory;
//    public SessionFactory getSessionFactory()
//    {
//        return this.sessionFactory;
//    }

    static {

        System.setProperty("textdb.allow_full_path", "true");

        // Load the application context
        applicationContext = new ClassPathXmlApplicationContext("classpath:configuration/app-config.xml");

        // Load our customer service beans
        sequenceService = (SequenceServiceImpl) applicationContext.getBean("SequenceService");
        filenameService = (FilenameServiceImpl) applicationContext.getBean("FilenameService");

        aligned_seqService = (AlignedSequenceServiceImpl) applicationContext.getBean("AlignedSequenceService");
        unique_seqService = (UniqueSequencesServiceImpl) applicationContext.getBean("UniqueSequencesService");
        annotationService = (AnnotationService) applicationContext.getBean("AnnotationService");


    }

    /**
     * Returns the filename's id if it is in path2id otherwise uses its toString
     *
     * @param file
     * @return
     */

    /**
     * Required for FastaAnnotationServiceLayer, which uses the combined file to
     * map to other reference sequences
     *
     * @return the Path of the combined fasta file containing all sequences
     * found in each sample just once.
     */
    public static Path getCombinedFastaFile() {
        return DatabaseWorkflowModule.combinedFile;
    }

    public static DatabaseWorkflowModule getInstance() throws WorkflowException{
        if (instance == null) {
            instance = new DatabaseWorkflowModule("Database");
        }
        
        // test working database
        try{
            DatabaseWorkflowModule.filenameService.findAll();
        }
        catch(CannotCreateTransactionException e)
        {
            throw new WorkflowException("Database no available for use", e);
        }
        return instance;
    }

    /**
     * Retrieve the JsonFactory that can bes used to create JsonGenerators for
     * this Database
     *
     * @return
     */
    public JsonFactory getJsonFactory() {
        return this.jsonFactory;
    }

    //***************************************************//
    /*
    
     PRINT AREA   
    
     */
    //***************************************************//
    public void showAlignments() {
        DatabaseWorkflowModule.aligned_seqService.printAlignments();
    }

    public void showSequences() {
        sequenceService.printSequences();
    }
    
    public void showSequenceExpression()
    {
        sequenceService.printExpressions();
    }

    public void showUniqueSequences() {
        List<Unique_Sequences_Entity> seqs = unique_seqService.findAll();
        System.out.println("Sequences:");
        for (Unique_Sequences_Entity seq : seqs) {
            System.out.println(" seq: " + seq.getRNA_Sequence());
        }
    }
    
    public void showUniqueTable()
    {
        unique_seqService.printUniqueSequenceInformation();
    }

    public void showFiles() {
        List<Filename_Entity> f_e_list = filenameService.findAll();
        System.out.println("Files:");
        for (Filename_Entity f_e : f_e_list) {
            System.out.println("\t" + f_e.getFilename() + " genome match: " + f_e.getTotalGenomeMatchAbundance() + " Sample: " + f_e.getSampleID() + " Offset: " + f_e.getOffset());
        }
    }
    
    public void showSamples(){
        filenameService.printSamples();
    }

    public void showRelationships() {
        System.out.println("Showing Sequences to Files: ");
        sequenceService.printFileRelationships();
        System.out.println("Showing Files to Sequences: ");
        filenameService.printSequenceRelationships();
        System.out.println("Showing Unique Sequences to Sequences");
        unique_seqService.printSequenceRelationships();
        unique_seqService.printAlignedRelationships();
        //sequenceService.printAlignedRelationships();
    }

    //***************************************************//
    /*
    
     SEARCH AND MODIFY AREA   
    
     */
    //***************************************************//
    public int findSequenceAbund(String toSearch) {
        return sequenceService.findSequenceAbund(toSearch);
    }

    public void addSequence(String seq, int hitCount, int startingAbundance, String filename) {
//        sequenceService.saveOrUpdate(new Sequence_Entity(null, seq, hitCount, startingAbundance, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, filename));
       sequenceService.saveOrUpdate(new Sequence_Entity(null, seq, hitCount, startingAbundance,  0.0, filename)); 
    }

    public void addFile(String filename, String abs_path, String sample_id) {
        filenameService.saveOrUpdate(new Filename_Entity(filename, abs_path, sample_id));
    }

    public void addSequence(Sequence_Entity se) {
        sequenceService.save(se);
    }

    public void updateSequence(Sequence_Entity se) {
        sequenceService.update(se);
    }

    public void saveOrUpdateSeqs(List<Sequence_Entity> seqs) {
        sequenceService.saveOrUpdateAll(seqs);
    }



    public void addFile(Filename_Entity fe) {
        filenameService.saveOrUpdate(fe);
    }

    public void executeGenericQuery(String query) {

    }
    
    public  boolean isDebugMode()
    {
        
        return debugMode;
        
    }

    /**
     *
     * @return
     */
    public Mapper getMapper()
    {
        return mapper;
    }

    /**
     *
     * @param newMapper
     */
    public void setMapper(Mapper newMapper)
    {
        this.mapper = newMapper;
    }
    
    

    //***************************************************//
    /*
    CONSTRUCTION AREA
     */
    //***************************************************//
    public  void setDebugMode(boolean debugMode)    
    {
        this.debugMode = debugMode;
        if (debugMode)
        {

            setTestData(Datasets.DATA_MS_BEE);   

            
            
        }
        
    }

    public boolean checkReadyToBuild()
    {

        // I can't see any harm in resetting the input files on each check
            Map<String, List<Path>> samples = FileHierarchyViewController.retrieveDataPaths();
            Path genomePath = FileHierarchyViewController.retrieveGenomePath();

            Path mappingFilePath = FileHierarchyViewController.retrieveMappingFilePath();
            this.insertMappingFile(mappingFilePath);
             // chris added for transcriptome
            Path transcriptomePath = FileHierarchyViewController.retrieveTranscriptomePath();
            insertRawData(samples, genomePath, transcriptomePath);


        boolean genomeSetupResult = WorkflowManager.getInstance().containsID("Database") ? ((myGenomePath != null || mappingFile != null)) : true ;
        boolean sampleSetupResult = (mySamples != null && !mySamples.isEmpty());
        return  sampleSetupResult && genomeSetupResult;
    }
    public void construct() throws IOException, Exception {

        //add default locus
        buildInitialData();
        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(0.25));

        }
        //this.lapTimer("Build initial data");
        combinedFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "total_to_align.fa");
        Path uniqueFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "unique_data.csv");
        Files.deleteIfExists(combinedFile);
        Files.deleteIfExists(uniqueFile);
        
        writeCombinedSequenceFile(combinedFile, uniqueFile);
        this.printLap("Writing combined fasta");

        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(0.33));

        }
        buildUniqueTable(uniqueFile.toString());
        this.printLap("Building unique sequence table");
        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(0.45));

        }
        mapReads();
        
        List<Path> GFFPaths = new ArrayList<>();
        List<String> annotations;
        List<String> otherAnnotations;
        if (debugMode)
        {
            GFFPaths = annotationFiles;
            annotations = annotationTypes;
            otherAnnotations = Lists.newArrayList();
        }
        else
        {
            //retrieve and process annotations
            GFFPaths = FileHierarchyViewController.retrieveGFFPaths();
            annotations = FileHierarchyViewController.getAnnotations();
           otherAnnotations = FileHierarchyViewController.getOtherAnnotations();
        }
        DatabaseAnnotationTool annotater = new DatabaseAnnotationTool(GFFPaths, annotations, otherAnnotations);
        annotater.run();
        this.printLap("Annotating all sequences");
        
        
        // ------ chris added code to add transcripts to DB --------
        // if there is a provided transcriptome and the file exists
        if (myTranscriptomePath != null && myTranscriptomePath.toFile().exists()) {
            TranscriptServiceImpl transcript_service = (TranscriptServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("TranscriptService");
            transcript_service.parseTranscriptome(myTranscriptomePath);
        }
        // ------ chris  code end -----------------------------------
        isConfigured = true;
    }

    private void buildInitialData() throws IOException {
        Map<String, Path> files = generateFilenameTable(mySamples);
        formatFiles(files);
        this.printLap("Formatting fasta files");
        generateTables();
        this.printLap("Creating sequence table");
    }

    /**
     *
     * @throws IOException
     */
    private void mapReads() throws IOException {
        //trackerInitUnknownRuntime("Aligning merged file to genome...");

        String alignedFile = "";
        // if a mapping file is specified, don't run patman.
        if (mappingFile == null)
        {
            alignedFile = mapper.mapReads();
        }
        else
        {
            // if specified, use the file given by mappingFIle
            alignedFile = mappingFile.toString();
        }
        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(0.65));

        }
        try{
            buildAlignedFileTable(alignedFile);
        }
        catch(ConstraintViolationException ex)
        {
            throw new WorkbenchAlignmentException("Error reading alignments in to database. Please make sure that the alignment files output by the mapping program are of unique Chromosome, start, end and strand combinations per sequence.", ex);
        }
        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(0.90));

        }
        calculateTotalAlignmentPerFile();
        this.printLap("Calculating alignment totals");
        if (databaseProgressInd != null) {
            Platform.runLater(()
                    -> databaseProgressInd.setProgress(1.0));

        }

//        Database.showUniqueSequences();
//        Database.showRelationships();
        System.gc();

    }

    public int getAlignmentFilterValue()
    {
        return alignmentFilterValue;
    }
    
    public void setFilterRepeats(boolean state)
    {
        this.filterRepeats = state;
    }

    public void setAlignmentFilterValue(int alignmentFilterValue)
    {
        this.alignmentFilterValue = alignmentFilterValue;
    }

    public void setGapsMismatch(int new_gaps, int new_mismatch)
    {
        gaps = new_gaps;
        mismatch = new_mismatch;
    }
    public Path formatBwt(String filename) throws IOException
    {
        Path finalBwtFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "formatted_bwt.tab");
        Files.deleteIfExists(finalBwtFile);
        Files.createFile(finalBwtFile);
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename), charset); FileOutputStream out = new FileOutputStream(finalBwtFile.toFile())) {
            String line;
            while((line = reader.readLine()) != null){
                String[] records = line.split(Tools.TAB);
                String chr = records[2];
                String seq = records[0];
                int start = Integer.valueOf(records[3]);
                int end = start + (seq.length() - 1);
                String strand = records[1];
                int gaps = 0;
                String outline = chr + Tools.TAB + seq + Tools.TAB + start + Tools.TAB + end + Tools.TAB + strand + Tools.TAB + gaps 
                                + Tools.TAB + ReferenceSequenceManager.INTERGENIC_TYPE_NAME
                                + Tools.TAB + ReferenceSequenceManager.GENOME_REFERENCE_NAME
                                + Tools.TAB + ReferenceSequenceManager.GENOME_REFERENCE_NAME
                                + Tools.TAB + " "
                                + Tools.TAB +" "
                                + Tools.TAB + " "
                                + Tools.TAB + " "
                                + Tools.TAB + " "
                                + LINE_SEPARATOR;
                out.write(outline.getBytes());
            }
        }
        return finalBwtFile;
    }
    
    public Path filterAlignments(int maxAlignment, String filename) {
        Path finalFilteredFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "filtered_alignments.patman");
        //convert create paths to sort input file
        String absolutePath = Tools.getNextDirectory().getAbsolutePath();
        String removeExtension = FilenameUtils.removeExtension(filename);

        Path tempSortedPath = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "patman_SORT.fa");
        try {
            Files.deleteIfExists(finalFilteredFile);
            Files.deleteIfExists(tempSortedPath);
            Files.createFile(finalFilteredFile);
            Files.createFile(tempSortedPath);
            //external sort uses sequence for comparison

            String line = null;
            String previousSequence = "";
            String[] maxAlignmentArray = null;
            int sequenceCount = 0;
            ArrayList<Byte> toWrite = new ArrayList<>();
            
            ExternalSort.sort(new File(filename), tempSortedPath.toFile(), patman_SeqLexicomparator);
            this.printLap("Sorting file");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] linSepBytes = LINE_SEPARATOR.getBytes();
            try (BufferedReader reader = Files.newBufferedReader(tempSortedPath, charset); 
                 FileOutputStream out = new FileOutputStream(finalFilteredFile.toFile())) {

                line = reader.readLine();
                if (line != null) {
                    //now add the new lines requried for the alignment table
                    line = convertLineData(line);
                    //first retrieve the sequence record from patman
                    String[] records = line.split(Tools.TAB);
                    
                   
                    if (records.length > 0) {
                        previousSequence = records[1];
                        sequenceCount++;
                        //add this sequence to the list of alignment details to write
                        baos.write(line.getBytes());
                        baos.write(linSepBytes);
                    }

                    while ((line = reader.readLine()) != null) {
                        //now add the new lines requried for the alignment table
                        line = convertLineData(line);

                        //first retrieve the sequence record from patman
                        records = line.split(Tools.TAB);
                        if (records.length > 0) {
                            if (sequenceCount >= maxAlignment) {
                                //if we have found more than the max value
                                //continue until we find a new sequence to work on

                                if (!records[1].equals(previousSequence)) {//reset the counter and sequence

                                    //write this line out
                                    if (baos.size() > 0) {
                                        //rebuild alignment line with negative values
                                        String maxAlignmentLine = maxAlignmentArray[0] + Tools.TAB
                                                + maxAlignmentArray[1] + Tools.TAB
                                                + "-" + maxAlignmentArray[2] + Tools.TAB
                                                + "-" + maxAlignmentArray[3] + Tools.TAB
                                                + maxAlignmentArray[4] + Tools.TAB
                                                + maxAlignmentArray[5] + Tools.TAB
                                                + maxAlignmentArray[6] + Tools.TAB
                                                + maxAlignmentArray[7] + Tools.TAB
                                                + maxAlignmentArray[8] + Tools.TAB
                                                + maxAlignmentArray[9] + Tools.TAB
                                                + maxAlignmentArray[10] + Tools.TAB
                                                + maxAlignmentArray[11] + Tools.TAB
                                                + maxAlignmentArray[12] + Tools.TAB
                                                + maxAlignmentArray[13] + Tools.TAB;
//                                        String maxAlignmentLine = maxAlignmentArray[0] + Tools.TAB
//                                                + maxAlignmentArray[1] + Tools.TAB
//                                                + "-" + maxAlignmentArray[2] + Tools.TAB
//                                                + "-" + maxAlignmentArray[3] + Tools.TAB
//                                                + maxAlignmentArray[4] + Tools.TAB
//                                                + maxAlignmentArray[5];

                                                                                        
                                        
                                        out.write(maxAlignmentLine.getBytes());
                                        out.write(LINE_SEPARATOR.getBytes());
                                    }

                                    previousSequence = records[1];
                                    sequenceCount = 0;
                                    baos.reset();

                                    sequenceCount++;
                                    //add this sequence to the list of alignment details to write
                                    baos.write(line.getBytes());
                                    baos.write(linSepBytes);
                                } else {
                                    sequenceCount++;
                                }

                            } else {
                                if (records[1].equals(previousSequence)) {//if this sequence is the same as the last one then increment the counter
                                    sequenceCount++;
                                    //add this sequence to the list of alignment details to write
                                    baos.write(line.getBytes());
                                    baos.write(linSepBytes);
                                } else {//reset the counter and sequence
                                    if(baos.size()>0){
                                        out.write(baos.toByteArray());
                                    }
                                    previousSequence = records[1];
                                    sequenceCount = 0;
                                    baos.reset();

                                    sequenceCount++;
//                                  //add this sequence to the list of alignment details to write
                                    baos.write(line.getBytes());
                                    baos.write(linSepBytes);
                                    
                                }
                            }
                        }
                        maxAlignmentArray = records;

                    }
                    //clear any remaining sequences out
                    if(baos.size() > 0){
                        out.write(baos.toByteArray());
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(DatabaseWorkflowModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.printLap("Filtering alignments");

        return finalFilteredFile;

    }
    
    private String convertLineData(String line)
    {
        String recordLine = line;

        recordLine += "\t" + ReferenceSequenceManager.INTERGENIC_TYPE_NAME;
        recordLine += "\t" + ReferenceSequenceManager.GENOME_REFERENCE_NAME;
        recordLine += "\t" + ReferenceSequenceManager.GENOME_REFERENCE_NAME;
        recordLine += "\t" + " ";
        recordLine += "\t" + " ";
        recordLine += "\t" + " ";
        recordLine += "\t" + " ";
        recordLine += "\t" + " ";

        return recordLine;
    }
    /*
    private String[] getLineData(String line)
    {
        //first retrieve the sequence record from patman
        String[] original_records = line.split(Tools.TAB);
        //now add the new lines requried for the alignment table
        String[] records = new String[original_records.length + 8];
        for (int i = 0; i < original_records.length + 8; i++)
        {
            if (i < original_records.length)//copy original alignment data
            {
                records[i] = original_records[i];
            }
            else
            {
                switch (i)
                {
                    case 6:
                        records[i] = ReferenceSequenceManager.INTERGENIC_TYPE_NAME;
                        break;
                    case 7:
                        records[i] = ReferenceSequenceManager.GENOME_REFERENCE_NAME;
                        break;
                    case 8:
                        records[i] = ReferenceSequenceManager.GENOME_REFERENCE_NAME;
                        break;
                    case 9:
                        records[i] = "";
                        break;
                    case 10:
                        records[i] = "";
                        break;
                    case 11:
                        records[i] = "";
                        break;
                    case 12:
                        records[i] = "";
                        break;
                    case 13:
                        records[i] = "";
                        break;

                }
            }
        }
        return records;
    }
*/

    public void insertRawData(Map<String, List<Path>> samples, Path genome, Path transcriptome) {
        mySamples = samples;
        myGenomePath = genome;
        myTranscriptomePath = transcriptome;
    }

    public void insertRawSampleData(Map<String, List<Path>> samples) {
        mySamples = samples;
    }

    public void insertGenomeFile(Path genome) {
        myGenomePath = genome;
    }

    public void insertMappingFile(Path patmanFile) {
        mappingFile = patmanFile;
    }
    
    public void addGFFAnnotationFile(Path gff) {
        annotationFiles.add(gff);
    }
    
    public void addAnnotationType(String type){
        annotationTypes.add(type);
    }
    
    /**
     * Retrieves a standard annotation set that includes sets for any types
     * that were specified by addAnnotationType. Mostly used for debugging
     */
    public AnnotationSetList getAnnotationSetList() throws AnnotationNotInDatabaseException
    {
        AnnotationSetList setlist =  AnnotationSetList.createAnnotationSetList();
        for(String type : annotationTypes)
            setlist.addType(type);
        
        return setlist;
    }
    
    public Path getMappingFile(){
        return mappingFile;
    }

    public Path getGenomePath() {
        return myGenomePath;
    }

    public void formatFiles(Map<String, Path> samples) throws IOException {
        
        int numBad = 0;
        Path formattedFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "totalData.csv");
        Files.deleteIfExists(formattedFile);
        Files.createFile(formattedFile);
        Long index = 1L;
        for (Entry<String, Path> reps : samples.entrySet()) {
            //sampleID++;
            //int totalForRep = 0;
            Path repFile = reps.getValue();
            String repID = reps.getKey();

                try (BufferedReader reader = Files.newBufferedReader(repFile, charset);
                        BufferedWriter writer = Files.newBufferedWriter(formattedFile, charset, APPEND)) {
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(">"))//assume line is descriptor
                        {
                            String[] seqData = line.split("\\(");

                            if (seqData.length > 0) {
                                String tempAbun = seqData[1].replace(")", "");
                                int newAbund = Integer.parseInt(tempAbun.trim());
                                String sequence = seqData[0].replaceAll(">", "");
                                if(this.lowestComplexity > 0){
                                    // TEMPORARY SEQUENCE COMPLEXITY FILTER
                                    int a = 0, g = 0, c = 0, t = 0;
                                    boolean bad = false;
                                    seqLoop:
                                    for (int i = 0; i < sequence.length(); i++) {
                                        char n = sequence.charAt(i);
                                        switch (n) {
                                            case 'A':
                                                a++;
                                                break;
                                            case 'G':
                                                g++;
                                                break;
                                            case 'C':
                                                c++;
                                                break;
                                            case 'T':
                                                t++;
                                                break;
                                            default:
                                                bad = true;
                                                break seqLoop;
                                        }
                                    }
                                    if(!bad){
                                        double total = a+g+c+t;
                                        int numok = 0;
                                        if(a/total > lowestComplexity) numok++;
                                        if(c/total > lowestComplexity) numok++;
                                        if(g/total > lowestComplexity) numok++;
                                        if(t/total > lowestComplexity) numok++;
                                        if(numok > 2){
                                            //String final_sequence = sequence.replaceAll(">", "");
        //                                    writer.write(index + "," + sequence + ",0," + newAbund + ",0.0,0.0,0.0,0.0,0.0,0.0,0.0," + repFile.getFileName().toString() + LINE_SEPARATOR);
                                            writer.write(index + "," + sequence + ",0," + newAbund + ",0.0," + repFile.getFileName().toString() + "," + repID + LINE_SEPARATOR);
                                        }
                                        else {
                                            numBad++;
                                        }
                                    }
                                }
                                else
                                {
                                    // skip the complexity filter stuff if not > 0 because it can take a few minutes extra
                                    writer.write(index + "," + sequence + ",0," + newAbund + ",0.0," + repFile.getFileName().toString() + "," + repID + LINE_SEPARATOR);
                                }

                            }
                            index++;
                        }

                    }
                }

            
        }
        System.out.println("Filtered out " + numBad + " poor complexity sequences");
    }
    
    public Map<String, Path> generateFilenameTable(Map<String, List<Path>> samples){
        Map<String, Path> files = new HashMap<>();
        for (String sample : samples.keySet()) {
            for (Path repFile : samples.get(sample)) {
                String filename = repFile.getFileName().toString();
                Filename_Entity fe = new Filename_Entity(filename, repFile.toString(), sample);
                addFile(fe);
                files.put(fe.getFileID(), repFile);
            }
        }
        return files;
    }

    public void generateTables() {
        //totalAbund += entry.getValue();


        //build filename table

        //showFiles();
        Path formattedFile = Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "totalData.csv");
        sequenceService.importDataFromFile(formattedFile.toString());
        //showSequences();
//        showRelationships();
    }

    public void buildUniqueTable(String path) {
        unique_seqService.importDataFromFile(path);
    }

    public void buildAlignedFileTable(String aligned_file_path) throws IOException {
        
        String processed_alignments = mapper.processAlignments(aligned_file_path);
        aligned_seqService.importDataFromFile(processed_alignments);
        
            
    }

    public void calculateTotalAlignmentPerFile() {
//        System.out.println("before");
//        showFiles();
        //unique_seqService.calculateTotalAlignedPerFileThreaded(totalUniqueReads);
        unique_seqService.calculateTotalAlignedPerFile();
//        System.out.println("after");
//        showFiles();
    }

    public void writeCombinedSequenceFile(Path location, Path unique_data) {
        totalUniqueReads = sequenceService.statelessFASTAFileWrite(location, unique_data);
        System.out.println("");
    }
    //***************************************************//
    /*
    
     MISC AREA   
    
     */

    //***************************************************//
    public boolean getSetup() {
        return isConfigured;
    }

    public void setSetup(boolean setupState) {
        isConfigured = setupState;
    }

    public ApplicationContext getContext() {
        return applicationContext;
    }
    
    public void setFrameSize(Dimension size, JFrame frame)
    {
        ((FileHierarchyViewController)controller).setFrameSize(size, frame);
    }
    
    public void setFrameSize(Dimension size)
    {
        ((FileHierarchyViewController)controller).setFrameSize(size);
    }
    
    public void setFrameSize(Rectangle2D size)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            ((FileHierarchyViewController) controller).setEntryPoint(EntryPoint.DATABASE);
            ((FileHierarchyViewController) controller).setVisualBounds(size);

            //((FileHierarchyViewController) controller).loadWizard_setVisualBounds(size);
        }
    }
    
    public void setFrameSize(Rectangle2D size, EntryPoint ep)
    {
        if (!AppUtils.INSTANCE.isCommandLine())
        {
            //this.controller = new FileHierarchyViewController(size, this);
            ((FileHierarchyViewController) controller).setVisualBounds(size);

            ((FileHierarchyViewController) controller).setEntryPoint(ep);
        }
    }

    public static Map<String, List<Path>> getMySamples()
    {
        return mySamples;
    }
    
    public int getMaxSizeClass() {
        return aligned_seqService.findMaxSizeClass();
    }

    public int getMinSizeClass() {
        return aligned_seqService.findMinSizeClass();

    }



    public enum Mapper
    {
        PATMAN
        {
            @Override
            public String mapReads()
            {
                String alignedFile = Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + myGenomePath.getFileName() + ".patman";

                File patman_out = new File(alignedFile);

                PatmanParams newP_Params = new PatmanParams();
                newP_Params.setMaxGaps(gaps);
                newP_Params.setMaxMismatches(mismatch);
                newP_Params.setPreProcess(false);
                newP_Params.setPostProcess(false);
                newP_Params.setMakeNR(false);
                PatmanRunner p = new PatmanRunner(combinedFile.toFile(), myGenomePath.toFile(),
                        patman_out, Tools.getNextDirectory(), newP_Params);

                p.run();
                
                return alignedFile;
            }
            @Override
            public String processAlignments(String aligned_file_path)  throws IOException
            {
                if (DatabaseWorkflowModule.getInstance().filterRepeats)
                {
                    Path filterAlignments = DatabaseWorkflowModule.getInstance().filterAlignments(
                            DatabaseWorkflowModule.getInstance().alignmentFilterValue, aligned_file_path);
                    
                    return filterAlignments.toString();


                }
                else
                {
                    return aligned_file_path;
                }
            }
        },
        BOWTIE
        {
            @Override
            public String mapReads()
            {
                String alignedFile = Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + myGenomePath.getFileName() + ".bowtie";
                
                Path bowtie_out = Paths.get(alignedFile);

                BowtieRunner bowtieRunner = new BowtieRunner(combinedFile, myGenomePath,
                        bowtie_out, Tools.getNextDirectory().toPath(), DatabaseWorkflowModule.getInstance().alignmentFilterValue);
                
                bowtieRunner.setGapsMismatch(mismatch);
                
                Thread myThread = new Thread(bowtieRunner);

                myThread.start();
                try
                {
                    myThread.join();
                }
                catch (InterruptedException ex)
                {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                

                return alignedFile;
            }
            
            @Override
            public String processAlignments(String aligned_file_path) throws IOException
            {
                System.out.println("Formatting bowtie");
                Path formatted = DatabaseWorkflowModule.getInstance().formatBwt(aligned_file_path);
                System.out.println("Importing formatted file");
                return formatted.toString();

            }
        };
        
        public abstract String mapReads(  );
        public abstract String processAlignments(String aligned_file_path)  throws IOException;

    }
    
    
    //***************************************************//
    /*
    
     LOCAL STORAGE DATABASE   
    
     */
    //***************************************************//
    private void readLocalStorageDatabase() throws SqlJetException {
        SqlJetDb db = null;

        try {
            File dbFile = new File(Tools.WEB_VIEW_DATA_Path + DIR_SEPARATOR + "localstorage" + DIR_SEPARATOR + "file__0.localstorage");
            //dbFile.delete();

            db = SqlJetDb.open(dbFile, true);

            printAllRecords(db);
            listTableNames(db);

        } finally {
            db.close();
        }
    }

    private void printRecords(ISqlJetCursor cursor) throws SqlJetException {
        try {
            if (!cursor.eof()) {
                do {
                    System.out.println("row ID: " + cursor.getRowId());
                    System.out.println("Key: " + cursor.getString("key"));
                    System.out.println("Value: " + cursor.getString("value"));

//                    System.out.println(cursor.getRowId() + " : "
//                            + cursor.getString(FIRST_NAME_FIELD) + " "
//                            + cursor.getString(SECOND_NAME_FIELD) + " was born on "
//                            + formatDate(cursor.getInteger(DOB_FIELD)));
                } while (cursor.next());
            }
        } finally {
            cursor.close();
        }
    }

    public void printAllRecords(SqlJetDb db) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
        try {
            ISqlJetCursor open = db.getTable("ItemTable").open();

            printRecords(open);
        } finally {
            db.commit();
        }

    }

    private void listTableNames(SqlJetDb db) throws SqlJetException {
        db.beginTransaction(SqlJetTransactionMode.WRITE);
        try {
            Set<String> indices = db.getSchema().getIndexNames();
            Set<String> tables = db.getSchema().getTableNames();
            for (String tableName : tables) {
                System.out.println("TableName: " + tableName);
//                ISqlJetTableDef tableDef = db.getSchema().getTable(tableName);
//                Set<ISqlJetIndexDef> tableIndices = db.getSchema().getIndexes(tableName);
//                for (ISqlJetIndexDef indexDef : tableIndices)
//                {
//                    if (!indexDef.isImplicit())
//                    {
//                        db.dropIndex(indexDef.getName());
//                    }
//                }
//                db.dropTable(tableName);
            }
        } finally {
            db.commit();
        }
    }

     //***************************************************//
    /*
    
     TESTING AREA   
    
     */
    //***************************************************//
    public static void test() {

//        // Test code
//        showSequences();
//        Filename_Entity fe = new Filename_Entity("test", "test_full", 0);
//        addFile(fe);
//        Filename_Entity fe2 = new Filename_Entity("test2", "test_full2", 0);
//        addFile(fe2);
//        addSequence( "ACTGACTG", 1, 1, "test" );
//        addSequence( "ACTGACTG", 1, 1, "test2" );
//        addSequence( "AAAAA", 1, 1, "test" );
//        showSequences();
//        
//        showRelationships();
        //shutdown();
    }

    public static void test_construct_DB() throws Exception {
       

        //BinaryExecutor be = ExeManagerClientConfig.startNewServer( 8000, LOGGER );
        DatabaseWorkflowModule.getInstance().debugMode=true;
        DatabaseWorkflowModule.getInstance().timed=true;
         DatabaseWorkflowModule.getInstance().globalStopWatch.start();

        setTestData(Datasets.DATA_HYPOXIA);

//        DatabaseWorkflowModule.getInstance().formatBwt(DatabaseWorkflowModule.getInstance().getMappingFile().toString());
//        Batcher.DEFAULT_BATCH_SIZE = 5000;

        DatabaseWorkflowModule.getInstance().process();

        System.out.println("Done constructing");
//        Session session = DatabaseWorkflowModule.getInstance().getSessionFactory().openSession();
        DatabaseWorkflowModule.getInstance().showSamples();
        DatabaseWorkflowModule.getInstance().showFiles();
    }

    public static void testReal() throws Exception {
//        try {
            System.out.println("DB construction");
            
            test_construct_DB();
//            Database.getInstance().showSequences();
//            Database.showRelationships();

            StatusTracker tracker = new StatusTracker(null, null);

            // Uncomment the normalisations that you don't want to test here
            List<NormalisationType> norms = new ArrayList<>(Arrays.asList(NormalisationType.NONE,
//                    NormalisationType.TRIMMED_MEAN,
                    NormalisationType.QUANTILE
//                    NormalisationType.TOTAL_COUNT,
//                    NormalisationType.DESEQ,
//                    NormalisationType.BOOTSTRAP,
//                    NormalisationType.UPPER_QUARTILE
            ));
             List<String> files = DatabaseWorkflowModule.filenameService.getFilenames();
             List<String> fileids = DatabaseWorkflowModule.filenameService.getFileIDs();
//             DatabaseWorkflowModule.getInstance().showSequenceExpression();
//             DatabaseWorkflowModule.getInstance().showAlignments();
//             DatabaseWorkflowModule.getInstance().showSequences();
//             AnnotationSet intergenic = new AnnotationSet("intergenic");
//             intergenic.addAnnotationType("intergenic");
 
            FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
//            

//        // Here, we set up a default set of annotations. Types pertaining to ...
        AnnotationSetList annots = DatabaseWorkflowModule.getInstance().getAnnotationSetList();
        AnnotationSet mapped = AnnotationSet.getMappedSet();
        
        
        Set<QC> qcList = Sets.newHashSet(QC.ADIST);
//        test_QC_switch(qcList, fileids, Arrays.asList(NormalisationType.NONE), annots);
//        
        NormalisationServiceLayer normServ = (NormalisationServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("NormalisationService");


        System.out.println("Normalising");
        NormalisationWorkflowServiceModule normaliser = new NormalisationWorkflowServiceModule("test");
        normaliser.setSamples(fileids);
        normaliser.setAnnotations(mapped);
        normaliser.setNormalisationTypes(norms);
        normaliser.run();
        
//        test_QC_switch(qcList, fileids, norms, annots);
//        finish();

        
        System.out.println("Strandbias divergence tool");
//        StrandBiasDivergenceTool sbd = new StrandBiasDivergenceTool();
//       // sbd.addFilename(files.get(0));
//        sbd.setFilenames(files);
//        sbd.setNormType(NormalisationType.QUANTILE);
//        sbd.run();
        fserv.setAllOffsets(20);
        DatabaseWorkflowModule.getInstance().showFiles();
        
        
//        System.out.println("Creating sample pairs");
        List<Sample_Entity> samples = fserv.getSamples();
        DifferentialExpressionTool deTool = new DifferentialExpressionTool(NormalisationType.QUANTILE);
        deTool.addSamplePair(samples.get(0).getSampleId(), samples.get(1).getSampleId());
        deTool.addSamplePair(samples.get(1).getSampleId(), samples.get(2).getSampleId());
        deTool.run();
        
//        DifferentialExpressionService deServ = (DifferentialExpressionService) DatabaseWorkflowModule.getInstance().getContext().getBean("DifferentialExpressionService");

//        deServ.writeDEtoCSV(Paths.get("de.csv"), NormalisationType.NONE, 0.1);
//        System.out.println("Printing patterns");
//            deServ.printPatterns();
//            deServ.writeDEdistributions(Paths.get("web/json/DEBP.json"), NormalisationType.NONE, 0.1);
//            deServ.printSampleSequences();
//            DatabaseWorkflowModule.aligned_seqService.printWindows();
//            DatabaseWorkflowModule.aligned_seqService.printAlignments();
        
        DatabaseWorkflowModule.getInstance().globalStopWatch.stop();
        DatabaseWorkflowModule.getInstance().globalStopWatch.printTimes();

//        } catch (IOException ex ) {
//            System.out.println(ex.getMessage());
//            LOGGER.log(Level.SEVERE, ex.getMessage());
//        } 
                
    }
    
    private static void test_QC_switch(Set<QC> qc, List<String> fileIDs, List<NormalisationType> normTypes, AnnotationSetList annotations)
    {
        FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        List<FilePair> replicatePairs = fserv.getAllReplicatePairs();
        ArrayList<Integer> offsets = new ArrayList();
        offsets.add(1);
        if(qc.contains(QC.MA)){
            DatabaseMATool matool = new DatabaseMATool(replicatePairs, normTypes, annotations, offsets);
            matool.run();
            DatabaseWorkflowModule.lap("QC MA values");
        }
        if(qc.contains(QC.MADIST)){
            
           // MValueDistributionTool mtool = new MValueDistributionTool(replicatePairs, normTypes, offsets, annotations);
            //mtool.run();
            DatabaseWorkflowModule.lap("QC M Distribution");

        }
        if(qc.contains(QC.JACCARD)){
            JaccardTableTool jtool = new JaccardTableTool(fileIDs, normTypes, new JaccardTableParams());
            jtool.run();
            DatabaseWorkflowModule.lap("QC Jaccard");

        }
        if(qc.contains(QC.ADIST)){
            AbundanceDistributionParameters abwParams = new AbundanceDistributionParameters();
            abwParams.setNumberOfWindows(1);
            abwParams.setWindowSize(10000);
            abwParams.setFirstCount(1000);
            AbundanceDistributionTool abwtool = new AbundanceDistributionTool(fileIDs, normTypes, annotations, abwParams);
            abwtool.setTimeable(true);
            abwtool.run();
            DatabaseWorkflowModule.lap("Abundance Distribution");
        }
        if (qc.contains(QC.SIZE)) {
            SizeClassDistributionTool sdt = new SizeClassDistributionTool(fileIDs, normTypes, annotations);
            sdt.setTimeable(true);
            sdt.run();
            DatabaseWorkflowModule.lap("Size Distribution");
        }
        if(qc.contains(QC.NUCSIZE)){
            NucleotideSizeClassDistributionTool nucTool = new NucleotideSizeClassDistributionTool();
            nucTool.run();
            DatabaseWorkflowModule.lap("NucSize Distribution");

        }
    }

    private enum QC{
        MA, MADIST, JACCARD, ADIST, SIZE, NUCSIZE;
    }
    
    // Easy way to to give a number alternative filenames due to switching
    // computers all the time
    private static Path whichFile(String... filenames){
        for(String filename : filenames){
            Path path = Paths.get(filename);
            if(Files.exists(path))
                return path;
        }
        return null;
    }

    public static void setTestData(Datasets data) {
        Map<String, List<Path>> testSamples = new HashMap<>();

        ArrayList<Path> sample1 = new ArrayList<>();
        ArrayList<Path> sample2 = new ArrayList<>();
        ArrayList<Path> sample3 = new ArrayList<>();
        ArrayList<Path> sample4 = new ArrayList<>();
        
        String defName1 = "Test1";
        String defName2 = "Test2";
        String defName3 = "Test3";
        String defName4 = "Test4";

        Path MattBdropbox = whichFile("F:/Dropbox", "C:/Users/Matthew/Dropbox");  
        Path testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/TAIR10_chr_all.fas");
//        Path testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/TAIR10_chr_all_formatted.fasta");  
//        DatabaseWorkflowModule.i Paths.get("TutorialData/GFF/TAIR9_GFF3_genes_transposons.Chr1.ID=1.gff");
        Path annotationFile = Paths.get("TutorialData/GFF/TAIR9_GFF3_genes_transposons.Chr1.ID=1.gff");
        String dir;
        switch (data) {
            case DATA_ATH_TINY:
                //sample1.add(Paths.get("src/test/data/norm/ath_366868_head.fa"));
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/nr_small_ATH.fa"));
                //sample2.add(Paths.get("src/test/data/norm/ath_366868_head2.fa"));
                annotationFile = Paths.get("TutorialData/GFF/TAIR9_GFF3_genes_transposons.Chr1.ID=1.gff");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                testGenomeFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas");


                break;
                
            case DATA_ATH_UNIT_TEST:
                sample1.add(Paths.get("src/test/data/norm/ath_366868_head.fa"));
                sample2.add(Paths.get("src/test/data/norm/ath_366868_head2.fa"));
//                annotationFile = Paths.get("TutorialData/GFF/TAIR9_GFF3_genes_transposons.Chr1.ID=1.gff");
//                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/TAIR10_chr_all.fas");

                break;
            case DATA_ATH:
                sample1.add(Paths.get("src/test/data/norm/X366868.fasta"));
                sample2.add(Paths.get("src/test/data/norm/X366869.fasta"));
                sample3.add(Paths.get("src/test/data/norm/X366870.fasta"));
                break;
                
            case DATA_ATH_DEMO: // demo data potentially used for the lab meeting
                String location = "TutorialData/FASTA/RNAOME/Sample2/";
                sample1.add(Paths.get(location+"118372.fa"));
                sample1.add(Paths.get(location+"118373.fa"));
                break;
            
            case DATA_ATH_SINGLE:
                sample1.add(Paths.get("src/test/data/norm/X366868.fasta"));
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;

            case DATA_ATH_TUTORIAL:
                sample1.add(Paths.get("TutorialData/FASTA/RNAOME/GSM118373_Rajagopalan_col0_leaf_nr.fa"));
                //sample2.add(Paths.get("TutorialData/FASTA/RNAOME/GSM154336_carrington_col0_flower_nr.fa"));
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                break;
            case DATA_ATH_BOOKCHAPTER:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/A.TH_Book/Sample1.1/SRR1297338_na.fasta"));
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/A.TH_Book/Sample1.1/SRR1297340_na.fasta"));
                
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/A.TH_Book/Sample2.1/SRR1297344_na.fasta"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/A.TH_Book/Sample2.1/SRR1297346_na.fasta"));

                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/TAIR10_chr_all.fas");

                annotationFile = Paths.get("/Developer/Applications/sRNAWorkbench/Workbench/TutorialData/GFF/TAIR10_GFF3_genes.gff");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                break;

            case DATA_ATH_CARRINGTON:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154336_carrington_col0_flower_nr.fa"));
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154370_carrington_col0_leaf_nr.fa"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM257235_DCB_col0_flower_nr.fa"));
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                break;
            case DATA_ATH_CARRINGTON_MATTB:
                
                
                String FILEPATH1 = "F:/Dropbox/phd/quality check/ATH4Samples/";
                String FILEPATH2 = "/home/mka07yyu/Dropbox/phd/quality check/ATH4Samples/";
                String FILEPATH3 = "D:/users/Matt/Dropbox/phd/quality check/ATH4Samples/";
                String FILEPATH;
                if(Files.exists(Paths.get(FILEPATH1))){
                    FILEPATH = FILEPATH1;
                }
                else if(Files.exists(Paths.get(FILEPATH2))){
                    FILEPATH = FILEPATH2;  
                } else if(Files.exists(Paths.get(FILEPATH3))){
                    FILEPATH = FILEPATH3;
                }
                else {
                    throw new RuntimeException("Can't find directory for CARRINGTON data");
                }
                
                sample1.add(Paths.get(FILEPATH + "GSM118373_Rajagopalan_col0_leaf_nr.fa"));
                sample2.add(Paths.get(FILEPATH + "GSM154336_carrington_col0_flower_nr.fa"));
                sample1.add(Paths.get(FILEPATH + "GSM154370_carrington_col0_leaf_nr.fa"));
                sample2.add(Paths.get(FILEPATH + "GSM257235_DCB_col0_flower_nr.fa"));
                DatabaseWorkflowModule.getInstance().insertMappingFile(Paths.get("TutorialData/FASTA/RNAOME/Carrington.patman"));
                break;

            case DATA_DARRELL:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/Normal_bone_ACTTGA_L002_R1_AR.fa"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/Mesenchymal_tumour_GTTTCG_L002_R1_AR.fa"));
                testGenomeFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Human/GRCh38_no_alt/human_set");
                annotationFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/hsa.gff3");
                DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                break;
            case DATA_DROS:
                sample1.add(Paths.get("/Volumes/Data/DrosophilaBookChapter/Sample1/SRR1593704_AR.fa"));
                sample1.add(Paths.get("/Volumes/Data/DrosophilaBookChapter/Sample1/SRR1593705_AR.fa"));
                sample2.add(Paths.get("/Volumes/Data/DrosophilaBookChapter/Sample2/SRR1593706_AR.fa"));
                sample2.add(Paths.get("/Volumes/Data/DrosophilaBookChapter/Sample2/SRR1593707_AR.fa"));

                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                testGenomeFile = Paths.get("/Volumes/Data/DrosophilaBookChapter/dmel-all-chromosome-r6.09.fasta");
                DatabaseWorkflowModule.getInstance().insertMappingFile(Paths.get("/Volumes/Data/DrosophilaBookChapter/dmel-all-chromosome-r6.09.fasta.patman"));
                break;
            case DATA_MATTB_MCF7:
                sample1.add(Paths.get("/home/mka07yyu/data/mcf7/SRR873387_na.fasta"));
                sample2.add(Paths.get("/home/mka07yyu/data/mcf7/SRR873388_na.fasta"));
                sample3.add(Paths.get("/home/mka07yyu/data/mcf7/SRR873389_na.fasta"));
                testGenomeFile = Paths.get("/home/mka07yyu/data/genomes/Homo_sapiens_GRCh37_75.fa");
                DatabaseWorkflowModule.getInstance().insertMappingFile(Paths.get("/home/mka07yyu/data/mcf7/mapped_repfiltered.pat"));
                break;

            case DATA_MATTS_MCF7:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/mcf7-data-for-matts/SRR873388_na.fasta"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/mcf7-data-for-matts/SRR873389_na.fasta"));
                testGenomeFile = Paths.get("/home/mka07yyu/data/genomes/Homo_sapiens_GRCh37_75.fa");
                break;

            case DATA_ATH_REPS:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Mutants_Fahlgren/366868.fa"));
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Mutants_Fahlgren/366869.fa"));
                //sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Mutants_Fahlgren/366870.fa"));

                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/organSamples_Rajagopalan/118372.fa"));
                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/organSamples_Rajagopalan/118373.fa"));
//                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/organSamples_Rajagopalan/118374.fa"));

//                sample3.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Reps_Fahlgren/366865.fa"));
//                sample3.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Reps_Fahlgren/366866.fa"));
//                sample3.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/Reps_Fahlgren/366867.fa"));
                testGenomeFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                break;
            case DATA_HYPOXIA:
                String hypDir = whichFile(MattBdropbox + "/phd/quality check/hypoxia-data/", "G:/data/MCF7_hypoxia/fasta/").toString() + "/";
                sample1.add(Paths.get(hypDir + "SRR873382_na.fasta"));
                sample1.add(Paths.get(hypDir + "SRR873383_na.fasta"));
//                sample2.add(Paths.get(hypDir + "SRR873384_na.fasta"));
//                sample2.add(Paths.get(hypDir + "SRR873385_na.fasta"));
                sample3.add(Paths.get(hypDir + "SRR873386_na.fasta"));
                sample3.add(Paths.get(hypDir + "SRR873387_na.fasta"));
                sample4.add(Paths.get(hypDir + "SRR873388_na.fasta"));
                sample4.add(Paths.get(hypDir + "SRR873389_na.fasta"));
                defName1 = "N00";
                defName2 = "H16";
                defName3 = "H32";
                defName4 = "H48";
                
                Path hypmap = whichFile("F:/phd/data/hypoxia-data/bwtmapped", "E:/phd-local/hypoxia-data/bwtmapped", "G:/data/MCF7_hypoxia/mapped/bwtmapped_grch38.tab");
  
                if(hypmap != null){
                    System.out.println("Using ready-made mapping file " + hypmap);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(hypmap);
                    DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                }
//                annotationFile = Paths.get("TutorialData/GFF/hsa.gff3");
                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/Homo_sapiens_GRCh37_75.fa");
//                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_HYPOXIA_SVR_4:
                String svr_hypDir = "/scratch/mstocks/Hyp/HYP/";
                sample1.add(Paths.get(svr_hypDir + "SRR873382_na.fasta"));
                sample1.add(Paths.get(svr_hypDir + "SRR873383_na.fasta"));
                sample2.add(Paths.get(svr_hypDir + "SRR873384_na.fasta"));
                sample2.add(Paths.get(svr_hypDir + "SRR873385_na.fasta"));
                sample3.add(Paths.get(svr_hypDir + "SRR873386_na.fasta"));
                sample3.add(Paths.get(svr_hypDir + "SRR873387_na.fasta"));
                sample4.add(Paths.get(svr_hypDir + "SRR873388_na.fasta"));
                sample4.add(Paths.get(svr_hypDir + "SRR873389_na.fasta"));
                defName1 = "N00";
                defName2 = "H16";
                defName3 = "H32";
                defName4 = "H48";
                
                Path svr_hypmap = Paths.get(svr_hypDir + "human_set.bowtie");
  
                if(svr_hypmap != null){
                    System.out.println("Using ready-made mapping file " + svr_hypmap);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(svr_hypmap);
                    DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                }
                annotationFile = Paths.get(svr_hypDir + "hsa.gff3");
                testGenomeFile = Paths.get(svr_hypDir + "human_set");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_HYPOXIA_SVR_3:
                String svr_hypDir3 = "/scratch/mstocks/Hyp/HYP/";
                sample1.add(Paths.get(svr_hypDir3 + "SRR873382_na.fasta"));
                sample1.add(Paths.get(svr_hypDir3 + "SRR873383_na.fasta"));
                sample2.add(Paths.get(svr_hypDir3 + "SRR873384_na.fasta"));
                sample2.add(Paths.get(svr_hypDir3 + "SRR873385_na.fasta"));
                sample3.add(Paths.get(svr_hypDir3 + "SRR873386_na.fasta"));
                sample3.add(Paths.get(svr_hypDir3 + "SRR873387_na.fasta"));
                sample4.add(Paths.get(svr_hypDir3 + "SRR873388_na.fasta"));
                sample4.add(Paths.get(svr_hypDir3 + "SRR873389_na.fasta"));
                defName1 = "N00";
                defName2 = "H16";
                defName3 = "H32";
                defName4 = "H48";
                
                Path svr_hypmap3 = Paths.get(svr_hypDir3 + "human_set.bowtie");
  
                if(svr_hypmap3 != null){
                    System.out.println("Using ready-made mapping file " + svr_hypmap3);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(svr_hypmap3);
                    DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                }
                annotationFile = Paths.get(svr_hypDir3 + "hsa.gff3");
                testGenomeFile = Paths.get(svr_hypDir3 + "human_set");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_MS_BEE:
                svr_hypDir = "/scratch/mstocks/Hyp/HYP/";

                String svr_beeDir = "/scratch/mstocks/BEE/";
                sample1.add(Paths.get(svr_beeDir + "SRR1734818_AR.fa"));
                sample1.add(Paths.get(svr_beeDir + "SRR1734819_AR.fa"));
                sample1.add(Paths.get(svr_beeDir + "SRR1734820_AR.fa"));
                sample2.add(Paths.get(svr_beeDir + "SRR1734822_AR.fa"));
                sample2.add(Paths.get(svr_beeDir + "SRR1734823_AR.fa"));
                sample2.add(Paths.get(svr_beeDir + "SRR1734824_AR.fa"));

                defName1 = "S1";
                defName2 = "S2";

                testGenomeFile = Paths.get(svr_beeDir + "Bter20110317-genome.fa");

                annotationFile = Paths.get(svr_hypDir + "hsa.gff3");

                //annotationFile = null;

                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_HYPOXIA_MS:
                String matt_hypDir =  "C:\\Users\\w0445959\\LocalDisk\\PostDoc\\NetBeansProjects\\Testing\\HYP\\";
                sample1.add(Paths.get(matt_hypDir + "SRR873382_na.fasta"));
                sample1.add(Paths.get(matt_hypDir + "SRR873383_na.fasta"));
                sample2.add(Paths.get(matt_hypDir + "SRR873384_na.fasta"));
                sample2.add(Paths.get(matt_hypDir + "SRR873385_na.fasta"));
                sample3.add(Paths.get(matt_hypDir + "SRR873386_na.fasta"));
                sample3.add(Paths.get(matt_hypDir + "SRR873387_na.fasta"));
                sample4.add(Paths.get(matt_hypDir + "SRR873388_na.fasta"));
                sample4.add(Paths.get(matt_hypDir + "SRR873389_na.fasta"));
                defName1 = "N00";
                defName2 = "H16";
                defName3 = "H32";
                defName4 = "H48";
                
                testGenomeFile = Paths.get(matt_hypDir + "human_set");
                annotationFile = Paths.get(matt_hypDir + "hsa.gff3");
                DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_HYPOXIA_SMALL:
                dir = "TutorialData/FASTA/RNAOME/hypoxia/small/";
                sample1.add(Paths.get(dir + "SRR873382_na_sampled_0.1.fasta"));
                sample1.add(Paths.get(dir + "SRR873383_na_sampled_0.1.fasta"));
                defName1 = "N00";
                sample2.add(Paths.get(dir + "SRR873384_na_sampled_0.1.fasta"));
                sample2.add(Paths.get(dir + "SRR873385_na_sampled_0.1.fasta"));
                defName2 = "H16";
                sample3.add(Paths.get(dir + "SRR873386_na_sampled_0.1.fasta"));
                sample3.add(Paths.get(dir + "SRR873387_na_sampled_0.1.fasta"));
                defName3 = "H32";
                sample4.add(Paths.get(dir + "SRR873388_na_sampled_0.1.fasta"));
                sample4.add(Paths.get(dir + "SRR873389_na_sampled_0.1.fasta"));
                defName4 = "H48";
                Path hyp_small_mappingFile = Paths.get(dir + "mapped_0.1.tab");
                if (Files.exists(hyp_small_mappingFile)) {
                    System.out.println("Using ready-made mapping file " + hyp_small_mappingFile);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(hyp_small_mappingFile);
                    DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                }
                annotationFile = Paths.get("TutorialData/GFF/hsa.gff3");
//                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/Homo_sapiens_GRCh37_75.fa");
                break;
            case DATA_HYPOXIA_SMALL_MS:
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/SRR873382_na_sampled_0.1.fasta"));
                sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/SRR873383_na_sampled_0.1.fasta"));
                defName1 = "N00";
//                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/SRR873386_na_sampled_0.1.fasta"));
//                sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/SRR873387_na_sampled_0.1.fasta"));
                defName2 = "H32";
//                sample3.add(Paths.get(dir + "SRR873388_na_sampled_0.1.fasta"));
//                sample3.add(Paths.get(dir + "SRR873389_na_sampled_0.1.fasta"));
//                defName3 = "H48";
                testGenomeFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/hg38.fa");
                
                Path hyp_mappingFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/hg38.fa.patman");
                
                if(Files.exists(hyp_mappingFile)){
                    System.out.println("Using ready-made mapping file " + hyp_mappingFile);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(hyp_mappingFile);
                }
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");

                annotationFile = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/Hypoxia/hsa.gff3");

                break;
            case DATA_HYPOXIA_TINY:
                String hypTinyDir = "TutorialData/FASTA/RNAOME/hypoxia/tiny/";
                sample1.add(Paths.get(hypTinyDir + "SRR873382_na_sampled_0.01.fasta"));
                sample1.add(Paths.get(hypTinyDir + "SRR873383_na_sampled_0.01.fasta"));
                sample2.add(Paths.get(hypTinyDir + "SRR873384_na_sampled_0.01.fasta"));
                sample2.add(Paths.get(hypTinyDir + "SRR873385_na_sampled_0.01.fasta"));
                sample3.add(Paths.get(hypTinyDir + "SRR873386_na_sampled_0.01.fasta"));
                sample3.add(Paths.get(hypTinyDir + "SRR873387_na_sampled_0.01.fasta"));
                sample4.add(Paths.get(hypTinyDir + "SRR873388_na_sampled_0.01.fasta"));
                sample4.add(Paths.get(hypTinyDir + "SRR873389_na_sampled_0.01.fasta"));
                defName1 = "N00";
                defName2 = "H16";
                defName3 = "H32";
                defName4 = "H48";

                Path tinyMappingFile = Paths.get(hypTinyDir + "mapped.tab");

                System.out.println("Using ready-made mapping file " + tinyMappingFile);
                DatabaseWorkflowModule.getInstance().insertMappingFile(tinyMappingFile);
                DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/Homo_sapiens_GRCh37_75.fa");
                annotationFile = Paths.get("TutorialData/GFF/hsa.gff3");
//                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                break;
            case DATA_HYPOXIA_HALF:
                dir = whichFile("F:/phd/data/hypoxia-data/half/", "E:/phd-local/hypoxia-data/half/").toString() + "/";
                sample1.add(Paths.get(dir + "SRR873382_na_0.5.fasta"));
                sample1.add(Paths.get(dir + "SRR873383_na_0.5.fasta"));
                defName1 = "N00";
                sample2.add(Paths.get(dir + "SRR873384_na_0.5.fasta"));
                sample2.add(Paths.get(dir + "SRR873385_na_0.5.fasta"));
                defName2 = "H16";
                sample3.add(Paths.get(dir + "SRR873386_na_0.5.fasta"));
                sample3.add(Paths.get(dir + "SRR873387_na_0.5.fasta"));
                defName3 = "H32";
                sample4.add(Paths.get(dir + "SRR873388_na_0.5.fasta"));
                sample4.add(Paths.get(dir + "SRR873389_na_0.5.fasta"));
                defName4 = "H48";
                hyp_small_mappingFile = Paths.get(dir + "mapped_0.5.tab");
                if (Files.exists(hyp_small_mappingFile)) {
                    System.out.println("Using ready-made mapping file " + hyp_small_mappingFile);
                    DatabaseWorkflowModule.getInstance().insertMappingFile(hyp_small_mappingFile);
                    DatabaseWorkflowModule.getInstance().setMapper(Mapper.BOWTIE);
                }
                annotationFile = whichFile("TutorialData/GFF/hsa.gff3");
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                testGenomeFile = Paths.get("TutorialData/FASTA/GENOME/Homo_sapiens_GRCh37_75.fa");
                break;
            case DATA_CHRIS: {
                    testGenomeFile = Paths.get("/Users/ujy06jau/Documents/Work/PostDoc/Data/Ath_TAIR10.fa");
                    //String testDegradome1Path = "/Users/ujy06jau/Documents/Work/PostDoc/Data/degradomes/GSM280226-44886-short.fa";
                    //String testDegradome2Path = "/Users/ujy06jau/Documents/Work/PostDoc/Data/degradomes/GSM280227-44889-short.fa";
                    Path p1 = Paths.get("/Users/ujy06jau/Documents/Work/PostDoc/Data/srnaDatasets/GSM121453.fa");
                    File s1 = new File(p1.toString() + "_formatted.fa");
                    Path p2 = Paths.get("/Users/ujy06jau/Documents/Work/PostDoc/Data/srnaDatasets/GSM121454.fa");
                    File s2 = new File(p2.toString() + "_formatted.fa");


                    sample1.add(s1.toPath());
                    sample2.add(s2.toPath());
                    //String testTranscriptPath = "/Users/ujy06jau/Documents/Work/PostDoc/Data/ATH_cDNA_sequences_20101108.fas";
                    annotationFile = Paths.get("/Users/ujy06jau/Documents/Work/PostDoc/Data/GFF/tair10.gff");
                    DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                      break;         
            }
            case DATA_CLAUDIA:{
                String patman;//= "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\TESTING\\PATMANS\\GSM10.patman";
//                String genomeFile;// = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\hg38.fa";
                String outputFile;//= "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Human datasets\\A\\GSM10.patman";
                String miRBaseFile;// =  "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\hsa_all.csv";
                // String DEFile  = "none";
                // String allInfo ;//= "Y";
                String paramFile;
                System.out.println("Setting DATA_CLAUDIA");
               //DatabaseWorkflowModule.getInstance().setFilterRepeats(false);
                testGenomeFile = Paths.get("C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\Zv9_toplevel.fa");
                
                //patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\ZF2\\WT1.patman";
                patman = "C:\\Users\\keu13sgu\\Workbench1\\target\\release\\User\\ExpressionMatrixData\\Zv9_toplevel.fa.patman";
                //outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\ZF2\\MC2\\WT1_rez2.patman";
                //miRBaseFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\zf.csv";
                //paramFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\params.txt";
                
                sample1.add(Paths.get("C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\ZF2\\WT1.fa")); 
                DatabaseWorkflowModule.getInstance().insertMappingFile(Paths.get(patman));                            
                DatabaseWorkflowModule.getInstance().setMapper(Mapper.PATMAN);                 
                DatabaseWorkflowModule.getInstance().addAnnotationType("miRNA");
                annotationFile = Paths.get("C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\zf.gff");
            }

        }
        testSamples.put(defName1, sample1);
        testSamples.put(defName2, sample2);
        testSamples.put(defName3, sample3);
        testSamples.put(defName4, sample4);

        DatabaseWorkflowModule.getInstance().insertRawData(testSamples, testGenomeFile, null);
        if(annotationFile != null)
            DatabaseWorkflowModule.getInstance().addGFFAnnotationFile(annotationFile);
        

    }
    
    public static enum Datasets{
        DATA_ATH_TINY,
        DATA_ATH_UNIT_TEST,
        DATA_ATH,
        DATA_ATH_TUTORIAL,
        DATA_ATH_DEMO,
        DATA_ATH_CARRINGTON,
        DATA_ATH_CARRINGTON_MATTB,
        DATA_DARRELL,
        DATA_MATTB_MCF7,
        DATA_MATTS_MCF7,
        DATA_ATH_REPS,
        DATA_ATH_SINGLE,
        DATA_HYPOXIA,
        DATA_HYPOXIA_SVR_4,
        DATA_HYPOXIA_SVR_3,
        DATA_HYPOXIA_SMALL,
        DATA_HYPOXIA_MS,
        DATA_HYPOXIA_SMALL_MS,
        DATA_HYPOXIA_TINY,
        DATA_HYPOXIA_HALF,
        DATA_CHRIS,
        DATA_CLAUDIA,
        DATA_ATH_BOOKCHAPTER,
        DATA_DROS,
        DATA_MS_BEE
    }

    private static void finish() throws IOException {
        DatabaseWorkflowModule.getInstance().globalStopWatch.stop();
        DatabaseWorkflowModule.getInstance().globalStopWatch.printTimes();
        Socket clientSocket = new Socket("localhost", 8000);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes("close" + LINE_SEPARATOR);

        System.out.println("fin");
        clientSocket.close();
        System.exit(0);
    }
    
    private static void test_fasta_annotation(StopWatch sw) throws DuplicateReferenceException {
        Path fastaTestPath = Paths.get(Tools.DATA_DIR + "/Rfam.fasta");
        FastaAnnotationWorkflow fa = new FastaAnnotationWorkflow("eh", fastaTestPath, "Rfam", new FastaAnnotationParams());

        fa.addType("rRNA");
        fa.addTypeKeyword("rRNA", "rRNA");
        fa.addTypeKeyword("5S", "rRNA");
        annotationService.printAnnotations();
        fa.run();
//        sw.lap("Fasta annotation");
        //DatabaseWorkflowModule.getInstance().showAlignments();
//        DatabaseWorkflowModule.getInstance().showUniqueTable();
    }

    public static void main(String[] args) throws Exception {

        DatabaseWorkflowModule.testReal();     
        //DatabaseWorkflowModule.getInstance().filterAlignments(15, "/Developer/Applications/sRNAWorkbench/TestingData/testMultipleAlignment.patman");
    }

    void insertProgressInd(ProgressIndicator prInd) {
        databaseProgressInd = prInd;
    }

    @Override
    protected void process() throws Exception {
        ((FileHierarchyViewController)controller).setBusy(true);
        DatabaseWorkflowModule.getInstance().construct();
        //AnnotationService aServ = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
        //aServ.printAnnotations();
        //DatabaseWorkflowModule.getInstance().showFiles();
       // DatabaseWorkflowModule.getInstance().showSequences();
//        ((FileHierarchyViewController)controller).setBusy(false);
//        ((FileHierarchyViewController)controller).setBusy(true);


    }

    public static List<Map<String, Object>> executeGenericSQLMapped(Session session, String sql) {  
        // run query with no restrictions on first index and max number of results to show
        return executeGenericSQLMapped(session, sql, -1, -1);
    }
    
    public static List<Map<String, Object>> executeGenericSQLMapped(Session session, String sql, int firstResult, int maxResults) {
        // generate the HQL query
        Query query = session.createQuery(sql);
        // enable HQL query caching
        query.setCacheable(true);
        // set the first result index (optional)
        if(firstResult >= 0)
        {
            query.setFirstResult(firstResult);
        }
        // set max results (optional)
        if(maxResults >= 0)
        {
            query.setMaxResults(maxResults);
        }
        // set the result transformer
        query.setResultTransformer(new OrderedResultTransformer());
        // return list of results
        return query.list();
    }

    public static int getNumRecords(Session session, String sql) {

        Query query = session.createQuery(sql);
        query.setCacheable(true);
        ScrollableResults results = query.scroll();
        results.last();
        int nRecords = results.getRowNumber() + 1;
        if (session.isOpen()) {
            session.close();
        }
        return nRecords;
    }

    private static String generateSQLRange(String fieldName, Comparable a, Comparable b) {
        String str;
        if (a.compareTo(b) == 0) {
            str = String.format("%s = %s", fieldName, a);
        } else {
            str = String.format("(%s >= %s AND %s <= %s)", fieldName, a, fieldName, b);
        }
        return str;
    }

    public static String sqlSelectByPrimaryKey(List<Long> idList, String tableName, String primaryKeyName) {
        if (idList.isEmpty()) {
            return "SELECT * FROM " + tableName + " LIMIT 0";
        }

        List<String> sqlIDs = new LinkedList<>();
        // order the list of IDs
        Collections.sort(idList);
        // where IDs are consecutive list range
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < idList.size() - 1; i++) {
            Long id = idList.get(i);
            Long nextID = idList.get(i + 1);
            if (nextID - id <= 1) {
                endIndex++;
            } else {
                sqlIDs.add(generateSQLRange(primaryKeyName, idList.get(startIndex), idList.get(endIndex)));
                startIndex = i + 1;
                endIndex = startIndex;
            }
        }
        // output final id range
        sqlIDs.add(generateSQLRange(primaryKeyName, idList.get(startIndex), idList.get(endIndex)));
        String sql = "SELECT * FROM " + tableName + " WHERE ";
        for (int i = 0; i < sqlIDs.size(); i++) {
            if (i > 0) {
                sql += " OR ";
            }
            sql += sqlIDs.get(i);
        }
        return sql;
    }

}
