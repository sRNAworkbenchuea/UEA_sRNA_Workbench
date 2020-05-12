package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2FX.Paresnip2SceneController;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

/**
 *
 * @author Josh
 */
public class Engine {

    private HashMap<SmallRNA, SmallRNA> smallRNAs;
    private HashMap<String, Integer> smallRNALibrarySize;
    public static HashMap<String, Integer> transcriptAlignedTags;
    public static HashMap<String, Integer> transcriptLengths;
    private ExecutorService executor;
    private BufferedWriter writer;
    public static double[] categoryPercentages;
    public static HashMap<String, double[]> transcriptCategoryPercentages;
    DegradomeTagAligner tagAligner = null;
    RuleSet rs = null;
    static Paresnip2SceneController controller;
    Paresnip2Configuration config;
    Paresnip2InputFiles input;
    int resultNum;
    boolean shouldContinue;
    ArrayList<File> generatedFiles;
    long startTime;
    File filteredSeqFile;
    int sRNALibrarySize;
    
    
    public Engine() {
        shouldContinue = true;

        setUpAnalysis();
        if (shouldContinue) {
            startAnalysis();
            shutdown();
        } else {
            Engine.updateProgressText("Something went wrong during set up. Does the output directory already contain a results file for these datasets?");

        }
    }

    private static void setSceneController(Paresnip2SceneController c) {
        controller = c;
    }

    public Engine(Paresnip2SceneController c) {

        Engine.setSceneController(c);
        AppUtils.INSTANCE.setRunningIDE(true);
        shouldContinue = true;
        setUpAnalysis();
        if (shouldContinue) {
            startAnalysis();
            shutdown();
        } else {
            Engine.updateProgressText("Something went wrong during set up. Does the output directory already contain a results file for these datasets?");
        }
    }

    private void setUpAnalysis() {

        startTime = System.nanoTime();
        Engine.updateProgressText("Setting up the analysis...");
        rs = RuleSet.getRuleSet();
        input = Paresnip2InputFiles.getInstance();
        config = Paresnip2Configuration.getInstance();
        generatedFiles = new ArrayList();
        transcriptCategoryPercentages = new HashMap<>();
        smallRNALibrarySize = new HashMap();
        if (AppUtils.INSTANCE.isCommandLine() && config.isVerbose()) {
            System.out.println("-----------------------------");
            System.out.println("Parameter file loaded:");
            System.out.println("-----------------------------");
            System.out.println(config);
            System.out.println("-----------------------------");
            System.out.println("Targeting rules file loaded:");
            System.out.println("-----------------------------");
            System.out.println(RuleSet.getRuleSet());
            System.out.println("");
        }

        Engine.updateProgressText("Converting files into non-redundant format...");
        filteredSeqFile = new File(input.getOutputDirectory() + File.separator + "filtered_seqs_info.txt");
        if (filteredSeqFile.exists()) {
            filteredSeqFile.delete();
        }

        Map<String, File> sRNA_samples = new LinkedHashMap<>();
        for (File sRNAFile : input.getsRNA_samples().values()) {
            File tmp = makeNonRedundant(sRNAFile, false);
            sRNA_samples.put(tmp.getName(), tmp);
            generatedFiles.add(tmp);
            tmp.deleteOnExit();
        }
        input.setsRNA_samples(sRNA_samples);

        Map<String, File> deg_samples = new LinkedHashMap<>();
        for (File degFile : input.getDegradome_samples().values()) {
            File tmp = makeNonRedundant(degFile, true);
            deg_samples.put(tmp.getName(), tmp);
            generatedFiles.add(tmp);
            tmp.deleteOnExit();
        }
        input.setDegradome_samples(deg_samples);
        Engine.updateProgressText("Finished converting files into non-redundant format...");
        if (config.isUseConservationFilter()) {
            Engine.updateProgressText("Performing the conservation filter...");
            doConservationFilter();
            //Attempt to clean up the conservation filter
            System.gc();
        }

        sRNA_samples = new LinkedHashMap<>();
        for (File sRNAFile : input.getsRNA_samples().values()) {

            if (config.isAlignSmallRNAsToGenome()) {
                Engine.updateProgressText("Aligning sequences to genome...");
                File patman_file = new File(mapReads(sRNAFile, input.getGenomeFile()));
                sRNAFile = filterSmallRNAFile(sRNAFile, patman_file);
                generatedFiles.add(sRNAFile);
                patman_file.delete();
            }

            sRNA_samples.put(sRNAFile.getName(), sRNAFile);
        }

        if (config.isAlignSmallRNAsToGenome()) {
            input.setsRNA_samples(sRNA_samples);
        }

        if (config.isUsingGenomeGFF()) {
            CDSExtractor extractor = new CDSExtractor(input.getGenomeFile(), input.getGff3File(), config.isIncludeUTR());
            input.addTranscriptome(extractor.createTranscriptome(new File(input.getOutputDirectory() + File.separator + "generated_transcript.fasta")));
        }

        createOutputFiles();

    }

    private void startAnalysis() {

        //For each smallRNA replicate
        for (File sRNAFile : input.getsRNA_samples().values()) {
            
            sRNALibrarySize = smallRNALibrarySize.get(sRNAFile.getName());
            
            Engine.updateProgressText("Reading small RNAs...");

            readSmallRNAs(sRNAFile);
            //For each degradome library
            for (File degFile : input.getDegradome_samples().values()) {
                executor = Executors.newFixedThreadPool(config.getNumberOfThreads());
                try {
                    writer = new BufferedWriter(new FileWriter(input.getOutputDirectory().getAbsolutePath()
                            + File.separator + sRNAFile.getName().split("\\.")[0] + "_"
                            + degFile.getName().split("\\.")[0] + ".csv", true));

                    writeResultsFileHeader();
                } catch (IOException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
                Engine.updateProgressPercentage(0);
                //tagAligner = new DegradomeTagAligner(degFile, input.getTranscriptomeFile());
                tagAligner = new DegradomeTagAligner(degFile, input.getTranscriptomeFile(), input.getOutputDirectory());
                tagAligner.run();
                Engine.updateProgressText("Performing the target search...");
                process();
                waitForExecutorFinish();
                if (AppUtils.INSTANCE.isCommandLine()) {
                    System.out.println("Finished analysing files: " + sRNAFile.getName() + " and " + degFile.getName());
                    System.out.println("");
                }
            }
        }

    }

    private void shutdown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);

            } catch (InterruptedException ex) {
                Logger.getLogger(Engine.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (File f : generatedFiles) {
            f.delete();
        }

        Engine.updateProgressText("Analysis complete! You can find your results in the selected output directory.");

        Engine.updateProgressPercentage(100);

        if (AppUtils.INSTANCE.isCommandLine()) {
            long duration = System.nanoTime() - startTime;
            long timeInMilliSeconds = duration / 1000000;
            long seconds = timeInMilliSeconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            String time = days + " days : " + hours % 24 + " hours : " + minutes % 60 + " minutes : " + seconds % 60 + " seconds";
            System.out.println("The analysis was completed in: " + time);
            //System.exit(0);
        }

    }

    private void writeResultsFileHeader() {
        try {

            //reset the result number
            resultNum = 1;

            writer.write("Record ID");
            writer.write(",");
            writer.write("Short Read ID");
            writer.write(",");
            writer.write("Gene ID");
            writer.write(",");
            writer.write("Category");
            writer.write(",");
            writer.write("Cleavage Position");
            writer.write(",");
            writer.write("Fragment Abundance");
            writer.write(",");
            writer.write("Weighted Fragment Abundance");
            writer.write(",");
            writer.write("Normalized Fragment Abundance");
            writer.write(",");
            writer.write("Short Read Abundance");
            writer.write(",");
            writer.write("Normalized Short Read Abundance");
            writer.write(",");
            writer.write("Duplex");
            writer.write(",");
            writer.write("Alignment Score");
            if (config.isUseFilter()) {
                writer.write(",");
                writer.write("Duplex MFE");
                writer.write(",");
                writer.write("Perfect MFE");
                writer.write(",");
                writer.write("MFE Ratio");
            }

            if (config.isUsePValue()) {
                writer.write(",");
                writer.write("p-value");
            }
            //More could possiblity go here

            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean isUpper(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isUpperCase(c)) {
                return false;
            }
        }

        return true;
    }

    private void readSmallRNAs(File file) {

        smallRNAs = new HashMap();
        String line;
        String commentLine = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {

                        SmallRNA srna = new SmallRNA(line);

                        int abundance = Integer.parseInt(commentLine.split("\\(")[1].split("\\)")[0]);
                        commentLine = commentLine.split("\\(")[0].trim();

                        srna.setComment(commentLine);
                        srna.setAbundance(abundance);

                        if (!smallRNAs.containsKey(srna)) {
                            smallRNAs.put(srna, srna);
                        } else {
                            System.out.println("Something has gone wrong with NR converter");
                        }

                    } else {
                        commentLine = line;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<SmallRNA> it = smallRNAs.keySet().iterator();
        while (it.hasNext()) {
            SmallRNA pair = it.next();
            if (pair.abundance < config.getMinSmallRNAAbundance()) {
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

    }

    private void process() {

        Engine.updateProgressText("Starting the search");
        Worker.setTotal(smallRNAs.size());



        for (SmallRNA s : smallRNAs.keySet()) {
            s.setNormalisedAbundance(((double) s.getAbundance() / (double) sRNALibrarySize) * 1000000);
        }

        for (HashMap.Entry<SmallRNA, SmallRNA> entry : smallRNAs.entrySet()) {
            SmallRNA key = entry.getKey();          
            executor.execute(new Worker(key, this, rs));

        }

    }

    public static void calculateCategoryPerTranscript(String gene, double length) {

        length = length - 20 + 1;
        if (transcriptCategoryPercentages.containsKey(gene)) {
            for (int i = 0; i < 5; i++) {
                transcriptCategoryPercentages.get(gene)[i] = transcriptCategoryPercentages.get(gene)[i] / length;
            }
        }

    }

    public static double getCalculatedCategoryPercentagePerTranscript(String gene, int category) {
        try {
            return transcriptCategoryPercentages.get(gene)[category];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return 1;
    }

    public static void addCategory(String gene, int category) {

        if (transcriptCategoryPercentages.containsKey(gene)) {
            transcriptCategoryPercentages.get(gene)[category]++;
        } else {
            transcriptCategoryPercentages.put(gene, new double[5]);
            transcriptCategoryPercentages.get(gene)[category]++;
        }
    }

    public static synchronized void updateProgressText(String text) {

        if (AppUtils.INSTANCE.isRunningInIDE() && controller != null) {
            controller.setProgressText(text);
        } else {
            System.out.println(text);
        }
    }

    public static synchronized void updateProgressPercentage(int percent) {
        if (AppUtils.INSTANCE.isRunningInIDE() && controller != null) {
            controller.updateProgress(percent);
        }
    }

    public synchronized void writeToFile(List<AlignmentPath> list) {
        try {
            for (AlignmentPath ap : list) {
                writer.write(System.lineSeparator());
                writer.write(Integer.toString(resultNum));
                writer.write(",");
                writer.write(ap.toString());
                writer.flush();
                resultNum++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Map<String, File> getConservedSequences(Map<String, File> seqFiles, boolean isDegradomeLibrary) {
        Map<String, File> conserved = new LinkedHashMap();

        HashMap<String, String> tmp = null;

        for (File s1 : seqFiles.values()) {
            HashMap<String, String> s1map = new HashMap();

            readSequenceFile(s1, s1map, isDegradomeLibrary);

            if (tmp == null) {
                for (File s2 : seqFiles.values()) {
                    if (s1 != s2) {
                        HashMap<String, String> s2map = new HashMap();
                        readSequenceFile(s2, s2map, isDegradomeLibrary);

                        Iterator<String> it = s1map.keySet().iterator();

                        while (it.hasNext()) {
                            String key = it.next();

                            if (!s2map.containsKey(key)) {
                                it.remove();
                            }
                        }
                    }
                }
            } else {
                //Compare this to the first completed one
                Iterator<String> it = s1map.keySet().iterator();

                while (it.hasNext()) {
                    String key = it.next();

                    if (!tmp.containsKey(key)) {
                        it.remove();
                    }
                }
            }

            //Check if the first one has not yet been completed
            if (tmp == null) {
                //keep a record of the first to use as a reference
                tmp = s1map;
            }

            File outputDir = input.getOutputDirectory();
            File conservedSeqFile = new File(outputDir.getAbsolutePath()
                    + File.separator + s1.getName() + ".conserved.fasta");
            conservedSeqFile.deleteOnExit();
            BufferedWriter w = null;
            try {
                w = new BufferedWriter(new FileWriter(conservedSeqFile));

                for (String s : s1map.keySet()) {
                    w.write(s1map.get(s));
                    w.newLine();
                    w.write(s);
                    w.newLine();
                    w.flush();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (w != null) {
                    try {
                        w.close();
                        conserved.put(conservedSeqFile.getName(), conservedSeqFile);
                    } catch (IOException ex) {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
        if (tmp != null) {
            tmp.clear();
        }
        return conserved;
    }

    private void doConservationFilter() {

        if (input.getsRNA_samples().size() > 1) {
            input.setsRNA_samples(getConservedSequences(input.getsRNA_samples(), false));
            for (File f : input.getsRNA_samples().values()) {
                generatedFiles.add(f);
            }
        }
        if (input.getDegradome_samples().size() > 1) {
            input.setDegradome_samples(getConservedSequences(input.getDegradome_samples(), true));
            for (File f : input.getDegradome_samples().values()) {
                generatedFiles.add(f);
            }
        }
    }

    private void readSequenceFile(File seqs, HashMap<String, String> map, boolean isDegradomeLibrary) {
        String line;
        int abundance = 0;
        String commentLine = null;
        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        if ((isDegradomeLibrary && line.length() <= config.getMaxFragmentLenth() && line.length() >= config.getMinFragmentLenth())
                                || (!isDegradomeLibrary && line.length() <= config.getMaxSmallRNALenth() && line.length() >= config.getMinSmallRNALenth())) {

                            if (!map.containsKey(line)) {
                                map.put(line, commentLine);
                            } else {
                                System.out.println("Something went wrong with the NR converter... exiting");
                                System.exit(1);
                            }
                        }
                    } else {
                        commentLine = line;
                        abundance = Integer.parseInt(line.split("\\(")[1].split("\\)")[0]);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createOutputFiles() {

        File outputDir = input.getOutputDirectory();

        Map<String, File> sRNAFiles = input.getsRNA_samples();
        Map<String, File> degFiles = input.getDegradome_samples();

        File f;
        for (File s : sRNAFiles.values()) {
            for (File d : degFiles.values()) {
                String dir = outputDir.getAbsolutePath()
                        + File.separator + s.getName().split("\\.")[0] + "_"
                        + d.getName().split("\\.")[0] + ".csv";
                f = new File(dir);
                try {
                    boolean b = f.createNewFile();
                    if (!b) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    if (AppUtils.INSTANCE.isRunningInIDE()) {
                        //Create an alert box stating there is an issue with creating the output files
                    } else {
                        System.out.println("There was an error creating the output files. Do they already exist?");
                    }
                    shouldContinue = false;
                }
            }
        }

    }

    public String mapReads(File smallRNAs, File genome) {
        String alignedFile = input.getOutputDirectory() + File.separator + smallRNAs.getName() + ".patman";

        File patman_out = new File(alignedFile);

        PatmanParams newP_Params = new PatmanParams();
        newP_Params.setMaxGaps(0);
        newP_Params.setMaxMismatches(0);
        newP_Params.setPreProcess(false);
        newP_Params.setPostProcess(false);
        newP_Params.setMakeNR(false);
        newP_Params.setPositiveStrandOnly(false);

        File tmpDir = new File("tmp");

        try {
            tmpDir.mkdir();
            PatmanRunner runner = new PatmanRunner(smallRNAs, genome,
                    patman_out, tmpDir, newP_Params);
            runner.setUsingDatabase(false);

            Thread myThread = new Thread(runner);

            myThread.start();
            myThread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpDir.deleteOnExit();
        return alignedFile;
    }

    private void waitForExecutorFinish() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException ex) {
                Logger.getLogger(Engine.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Takes a smallRNA seq file and a patman file and returns filters seqs that
     * did not align
     *
     * @param sRNAFile
     * @param patman_file
     * @return
     */
    private File filterSmallRNAFile(File sRNAFile, File patman_file) {

        //THIS NEEDS SORTING AS THE FILE WILL NOW ALREADY BE IN NR FORMAT
        //BEFORE IT STARTS BEING PROCESSED BY PATMAN
        HashSet<String> alignedReads = new HashSet();
        try {
            BufferedReader patmanReader = new BufferedReader(new FileReader(patman_file));
            BufferedReader smallRNAReader = new BufferedReader(new FileReader(sRNAFile));
            File alignedSmallRNAs = new File(input.getOutputDirectory() + File.separator + sRNAFile.getName() + ".aligned.fasta");
            alignedSmallRNAs.deleteOnExit();
            BufferedWriter alignedWriter = new BufferedWriter(new FileWriter(alignedSmallRNAs));

            String line;
            while ((line = patmanReader.readLine()) != null) {
                String seq = line.split("\t")[1];
                if (!alignedReads.contains(seq)) {
                    alignedReads.add(seq);
                }
            }

            while ((line = smallRNAReader.readLine()) != null) {

                if (line.startsWith(">")) {
                    if (alignedReads.contains(line.split(">")[1])) {
                        alignedWriter.write(line);
                        alignedWriter.newLine();
                        alignedWriter.write(smallRNAReader.readLine());
                        alignedWriter.newLine();
                        alignedWriter.flush();
                    }
                }
            }

            alignedWriter.close();
            return alignedSmallRNAs;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sRNAFile;
    }

    private File filterLowComplexitySeqs(File seqFile) {

        updateProgressText("Performing low complexity sequences filter...");
        BinaryExecutor exe = new BinaryExecutor();

        String args = "-in " + seqFile.getAbsolutePath() + " -out " + input.getOutputDirectory().getAbsolutePath() + File.separator + seqFile.getName() + ".filtered.fa -infmt fasta -outfmt fasta";
        exe.execDustmasker(args);
        File newFile = new File(input.getOutputDirectory().getAbsolutePath() + File.separator + seqFile.getName() + ".filtered.fa");
        newFile.deleteOnExit();

        return newFile;
    }

    private File makeNonRedundant(File seqs, boolean isDegradomeLibrary) {

        String line;
        String commentLine = null;
        HashMap<String, Integer> checkedSeqs = new HashMap();
        HashMap<String, String> smallRNAComments = new HashMap();
        HashMap<String, Integer> lowComplexSeqs = new HashMap();
        HashMap<String, Integer> sizeIssueSeqs = new HashMap();
        HashMap<String, Integer> invalidSeqs = new HashMap();

        int librarySize = 0;
        int count = 0;
        int abundanceDiscard = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        librarySize++;
                        if ((isDegradomeLibrary && line.length() <= config.getMaxFragmentLenth() && line.length() >= config.getMinFragmentLenth())
                                || (!isDegradomeLibrary && line.length() <= config.getMaxSmallRNALenth() && line.length() >= config.getMinSmallRNALenth())) {

                            boolean lowComplexity = true;
                            boolean isInvalid = true;

                            if (checkedSeqs.containsKey(line)) {
                                checkedSeqs.put(line, checkedSeqs.get(line) + 1);
                                lowComplexity = false;
                                isInvalid = false;
                            } else if (!lowComplexSeqs.containsKey(line) && !invalidSeqs.containsKey(line)) {

                                isInvalid = isInvalid(line);
                                if (!isInvalid) {
                                    if (config.isFilterLowComplexitySeqs()) {
                                        lowComplexity = isLowComplexity(line);
                                    } else {
                                        lowComplexity = false;
                                    }

                                    if (lowComplexity) {
                                        lowComplexSeqs.put(line, 1);
                                    }
                                } else {
                                    invalidSeqs.put(line, 1);
                                }

                                if (!lowComplexity && !isInvalid) {
                                    checkedSeqs.put(line, 1);
                                }
                            } else if (lowComplexSeqs.containsKey(line)) {
                                lowComplexSeqs.put(line, lowComplexSeqs.get(line) + 1);
                            } else {
                                invalidSeqs.put(line, invalidSeqs.get(line) + 1);
                            }

                            if (!isDegradomeLibrary && !smallRNAComments.containsKey(line) && !lowComplexity && !isInvalid) {
                                smallRNAComments.put(line, commentLine);
                            }
                        } else //Some counters needed for filtering statistics
                        if (sizeIssueSeqs.containsKey(line)) {
                            sizeIssueSeqs.put(line, sizeIssueSeqs.get(line) + 1);
                        } else {
                            sizeIssueSeqs.put(line, 1);
                        }
                    } else if (!isDegradomeLibrary) {
                        commentLine = line;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        File seqsNonRedundant = new File(input.getOutputDirectory() + File.separator + seqs.getName().split("\\.")[0] + ".NR.fa");
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(seqsNonRedundant));
            for (String seq : checkedSeqs.keySet()) {
                int abundance = checkedSeqs.get(seq);
                if (!isDegradomeLibrary) {
                    if (abundance >= config.getMinSmallRNAAbundance()) {
                        count++;
                        String comment = smallRNAComments.get(seq);
                        comment += " (" + abundance + ")";
                        br.write(comment);
                        br.newLine();
                        br.write(seq);
                        br.newLine();
                    } else {
                        abundanceDiscard++;
                    }
                } else {
                    count++;
                    br.write(">" + seq + " (" + abundance + ")");
                    br.newLine();
                    br.write(seq);
                    br.newLine();
                }
                br.flush();
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            //Sequence filtering stage, if running CLI output to console AND write to file
            //Otherwise just write to file
            BufferedWriter br = new BufferedWriter(new FileWriter(filteredSeqFile, true));
            br.write("-------------------------------------------");
            br.newLine();
            br.write("File: " + seqs.getName());
            br.newLine();
            br.write("Total number of valid non-redunant sequences: " + count);
            br.newLine();
            br.write("Number of non-redunant sequences discarded due to length: " + sizeIssueSeqs.size());
            br.newLine();
            br.write("Number of non-redunant sequences discarded as invald: " + invalidSeqs.size());
            br.newLine();
            br.write("Number of non-redunant sequences discarded as low complexity: " + lowComplexSeqs.size());
            br.newLine();
            if (!isDegradomeLibrary) {
                br.write("Number of non-redunant sequences discarded due to abundance: " + abundanceDiscard);
                br.newLine();
            }
            br.flush();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if(!isDegradomeLibrary)
        {
            smallRNALibrarySize.put(seqsNonRedundant.getName(), librarySize);
        }

        return seqsNonRedundant;

    }

    /**
     * Low compelxity sequence checker using proportion of single, di, and tri
     * nucleotide composition
     *
     * @param seq
     * @return
     */
    private boolean isLowComplexity(String seq) {

        seq = seq.toUpperCase();
        double proportion = 0.75;

        HashMap<String, Integer> ntComposition = new HashMap();
        ntComposition.put("A", 0);
        ntComposition.put("G", 0);
        ntComposition.put("C", 0);
        ntComposition.put("T", 0);

        HashMap<String, Integer> diNtComposition = new HashMap();
        diNtComposition.put("AA", 0);
        diNtComposition.put("AT", 0);
        diNtComposition.put("AC", 0);
        diNtComposition.put("AG", 0);
        diNtComposition.put("TA", 0);
        diNtComposition.put("TT", 0);
        diNtComposition.put("TC", 0);
        diNtComposition.put("TG", 0);
        diNtComposition.put("CA", 0);
        diNtComposition.put("CT", 0);
        diNtComposition.put("CC", 0);
        diNtComposition.put("CG", 0);
        diNtComposition.put("GA", 0);
        diNtComposition.put("GT", 0);
        diNtComposition.put("GC", 0);
        diNtComposition.put("GG", 0);

        HashMap<String, Integer> triNtComposition = new HashMap();
        triNtComposition.put("AAA", 0);

        triNtComposition.put("AAT", 0);
        triNtComposition.put("AAC", 0);
        triNtComposition.put("AAG", 0);
        triNtComposition.put("ATA", 0);
        triNtComposition.put("ATT", 0);
        triNtComposition.put("ATC", 0);
        triNtComposition.put("ATG", 0);
        triNtComposition.put("ACA", 0);
        triNtComposition.put("ACT", 0);
        triNtComposition.put("ACC", 0);
        triNtComposition.put("ACG", 0);
        triNtComposition.put("AGA", 0);
        triNtComposition.put("AGT", 0);
        triNtComposition.put("AGC", 0);
        triNtComposition.put("AGG", 0);
        triNtComposition.put("TAA", 0);
        triNtComposition.put("TAT", 0);
        triNtComposition.put("TAC", 0);
        triNtComposition.put("TAG", 0);
        triNtComposition.put("TTA", 0);
        triNtComposition.put("TTT", 0);
        triNtComposition.put("TTC", 0);
        triNtComposition.put("TTG", 0);
        triNtComposition.put("TCA", 0);
        triNtComposition.put("TCT", 0);
        triNtComposition.put("TCC", 0);
        triNtComposition.put("TCG", 0);
        triNtComposition.put("TGA", 0);
        triNtComposition.put("TGT", 0);
        triNtComposition.put("TGC", 0);
        triNtComposition.put("TGG", 0);
        triNtComposition.put("CAA", 0);
        triNtComposition.put("CAT", 0);
        triNtComposition.put("CAC", 0);
        triNtComposition.put("CAG", 0);
        triNtComposition.put("CTA", 0);
        triNtComposition.put("CTT", 0);
        triNtComposition.put("CTC", 0);
        triNtComposition.put("CTG", 0);
        triNtComposition.put("CCA", 0);
        triNtComposition.put("CCT", 0);
        triNtComposition.put("CCC", 0);
        triNtComposition.put("CCG", 0);
        triNtComposition.put("CGA", 0);
        triNtComposition.put("CGT", 0);
        triNtComposition.put("CGC", 0);
        triNtComposition.put("CGG", 0);
        triNtComposition.put("GAA", 0);
        triNtComposition.put("GAT", 0);
        triNtComposition.put("GAC", 0);
        triNtComposition.put("GAG", 0);
        triNtComposition.put("GTA", 0);
        triNtComposition.put("GTT", 0);
        triNtComposition.put("GTC", 0);
        triNtComposition.put("GTG", 0);
        triNtComposition.put("GCA", 0);
        triNtComposition.put("GCT", 0);
        triNtComposition.put("GCC", 0);
        triNtComposition.put("GCG", 0);
        triNtComposition.put("GGA", 0);
        triNtComposition.put("GGT", 0);
        triNtComposition.put("GGC", 0);
        triNtComposition.put("GGG", 0);

        int highestSingle = 0;
        int highestDouble = 0;
        int highestTripple = 0;

        char[] arr = seq.replace("U", "T").toCharArray();

        for (int i = 0; i < arr.length; i++) {
            for (int x = i; x <= i + 2; x++) {
                StringBuilder sb = new StringBuilder();
                String s = "";
                if (x < seq.length()) {
                    if (x == i) {
                        sb.append(arr[i]);
                        s = sb.toString();
                        ntComposition.put(s, ntComposition.get(s) + 1);

                        if (ntComposition.get(s) > highestSingle) {
                            highestSingle = ntComposition.get(s);
                        }

                    } else if (x == i + 1) {
                        sb.append(arr[i]);
                        sb.append(arr[i + 1]);
                        s = sb.toString();
                        diNtComposition.put(s, diNtComposition.get(s) + 1);

                        if (diNtComposition.get(s) > highestDouble) {
                            highestDouble = diNtComposition.get(s);
                        }
                    } else {
                        sb.append(arr[i]);
                        sb.append(arr[i + 1]);
                        sb.append(arr[i + 2]);
                        s = sb.toString();
                        triNtComposition.put(s, triNtComposition.get(s) + 1);

                        if (triNtComposition.get(s) > highestTripple) {
                            highestTripple = triNtComposition.get(s);
                        }
                    }

                }
            }
        }

        double highestSingleProportion = (double) highestSingle / (double) seq.length();
        double highestDoubleProportion = (double) highestDouble / ((double) seq.length() / (double) 2);
        double highestTrippleProportion = (double) highestTripple / ((double) seq.length() / (double) 3);;

        if (highestSingleProportion >= proportion) {
            return true;
        }

        if (highestDoubleProportion >= proportion) {
            return true;
        }

        if (highestTrippleProportion >= proportion) {
            return true;
        }

        return false;
    }

    boolean isInvalid(String seq) {
        seq = seq.toUpperCase();
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c != 'A' && c != 'C' && c != 'G' && c != 'T' && c != 'U') {
                return true;
            }
        }

        return false;
    }

    //    public static double[] getCategoryPercentages() {
//        return categoryPercentages;
//    }
}
