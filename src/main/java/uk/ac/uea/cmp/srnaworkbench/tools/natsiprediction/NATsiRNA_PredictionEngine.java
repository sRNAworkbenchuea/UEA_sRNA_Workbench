package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.BinarySearchAligner;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.CDSExtractor;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.utils.JsonUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

/**
 *
 * @author Josh Thody
 */
public class NATsiRNA_PredictionEngine {

    //Data structures
    private HashMap<String, Integer> smallRNAs = new HashMap();
    private HashMap<String, String> transcriptSeqs = new HashMap();
    private HashMap<String, String> transcriptAnnotation;
    //Alignment maps
    //Gene name -> position -> sequence -> abundance
    private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> smallRNA_alignment_map = new HashMap();
    private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> degradome_alignment_map = new HashMap();

    private List<NATPair> candidateNATs;
    private List<NATPair> processedNATs;
    private List<NATsiRNA> predictedSmallRNAs;
    private List<NATsiRNA> paresnip2Candidates;

    private NATsiRNA_PredictionInput input;
    private NATsiRNA_PredictionConfiguration config;

    private File smallRNA_alignments;
    private File degradome_alignments;
    private File candidateRegions;
    private File patmanAlignmentFile;
    File redundantDegradome;

    private boolean hasDegradome;

    ExecutorService executor;

    public NATsiRNA_PredictionEngine() {

        //Make it so that the user can provide a GFF file instead of transcriptome...
        //This will enable us to annotate cis/trans nats ETC
        input = NATsiRNA_PredictionInput.getInstance();
        config = NATsiRNA_PredictionConfiguration.getInstance();
        processedNATs = new ArrayList();
        predictedSmallRNAs = new ArrayList();

    }

    public void process() {

        //check if the input was a gff file
        if (input.getTranscript() == null && input.getGFF_file() != null) {
            if (input.getGenomeFile() != null) {
                File transcripts = new File(input.getOutputDir().getAbsolutePath() + File.separator + "extracted_transcripts.fa");

                CDSExtractor extractor = new CDSExtractor(input.getGenomeFile(), input.getGFF_file(), true);
                input.setTranscript(extractor.createTranscriptome(transcripts));

            } else {
                System.out.println("Provided GFF3 file for transcript sequence extraction but no genome provided. Exiting.");
                System.exit(1);
            }
        }

        //Step 1: Process the input data (sequence filtering and storing) 
        trimTranscriptAnnotation();
        File degradomeFileNR;
        if (input.getDegradomeFile() != null) {
            hasDegradome = true;
            degradomeFileNR = makeNonRedundant(input.getDegradomeFile(), input.getOutputDir(), config.getMinTagLength(), config.getMaxTagLength(), true);
        } else {
            hasDegradome = false;
            degradomeFileNR = new File(input.getOutputDir().getAbsolutePath() + File.separator + "empty_degradome.fa");
            try {
                degradomeFileNR.createNewFile();
                degradomeFileNR.deleteOnExit();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        File smallRNAFileNR = makeNonRedundant(input.getSmallRNAFile(), input.getOutputDir(), config.getMinSmallRNALength(), config.getMaxSmallRNALength(), false);

        if (config.isAlignToGenome()) {

            File patman_file = new File(mapReads(smallRNAFileNR, input.getGenomeFile()));
            smallRNAFileNR = filterSmallRNAFile(smallRNAFileNR, patman_file);
            patman_file.delete();

        }

        populateTranscriptSequences(input.getTranscript());

        //Keep record of redundant degradome for PS2
        redundantDegradome = input.getDegradomeFile();

        //Update the input instance with the NR files
        input.setDegradomeFile(degradomeFileNR);
        input.setSmallRNAFile(smallRNAFileNR);

        if (input.getGFF_file() != null) {
            Worker.extractGeneAnnotation(input.getGFF_file());
        }

        // Step 2: Align sRNA and degradome reads to transcript sequences.     
        alignReadsToTranscript();
        // Next, populate the map of gene alignment positions
        processAlignments();
        // Step 3: Process these alignments and extract longer regions
        generateReads();
        // Step 4: Align longer regions to other transcripts 
        initialAlignmentSearch();
        // Step 5: Do a BLAST search on potential transcript-transcript pairs
        // Step 6: Evaluate annealing potential using RNAplex
        // Both these steps are performed concurrently using the
        // ThreadPoolExecutor
        runWorkers();
        waitForExecutor();

        //Step 7: Re-evaluate sRNAs/tags that align to valid predicted NATs
        //        Determine the sRNA alignment densities
        calculateAlignmentDensities();

        // Step 8: Categorize the predicted nat-siRNAs into biogenesis groups (e.g. 1-5)
        // based on how well they fit the canonical biogenesis of sRNAs
        categorizeSmallRNAs();

        //Step 9: Determine distribution pattern of the small RNAs
        // this can be based on the 2012 genome biology paper using windows
        determineDistributionType();

        outputSmallRNAResults();

        // Step 10: Input the predicted nat-siRNAs into PAREsnip2 to search for potential targets
        if (hasDegradome) {
            performTargetPrediction();
        }
        // combineResults();
    }

    private File filterSmallRNAFile(File sRNAFile, File patman_file) {

        //THIS NEEDS SORTING AS THE FILE WILL NOW ALREADY BE IN NR FORMAT
        //BEFORE IT STARTS BEING PROCESSED BY PATMAN
        HashSet<String> alignedReads = new HashSet();
        try {
            BufferedReader patmanReader = new BufferedReader(new FileReader(patman_file));
            BufferedReader smallRNAReader = new BufferedReader(new FileReader(sRNAFile));
            File alignedSmallRNAs = new File(input.getOutputDir() + File.separator + sRNAFile.getName() + ".aligned.fasta");
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

    public String mapReads(File smallRNAs, File genome) {
        String alignedFile = input.getOutputDir() + File.separator + smallRNAs.getName() + ".patman";

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

    synchronized public void addProcessedNAT(NATPair nat) {
        processedNATs.add(nat);
    }

    private void runWorkers() {

        executor = Executors.newFixedThreadPool(config.getNumberOfThreads());

        System.out.println("A total of " + candidateNATs.size() + " potential NATs to analyze");

        /**
         * Multi-thread the analysis of potential NATpairs This is arguably the
         * most time consuming process due to the hybridization analysis step
         * using RNAplex.
         *
         */
        for (NATPair nat : candidateNATs) {
            Worker w = new Worker(nat, this);
            executor.execute(w);
        }
    }

    private void waitForExecutor() {
        executor.shutdown();

        //This is not a clean way to handle this...
        //
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
        candidateNATs.clear();
    }

    private void populateTranscriptSequences(File transcripts) {
        /**
         * Here, we populate all the transcript sequences for later use.
         *
         * Also, we write them to file to remove any data except the gene name.
         */

        StringBuilder sb = new StringBuilder();
        String gene = null;
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(transcripts));

            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (gene != null) {
                        //add it to map
                        gene = gene.substring(1).split(" ")[0];
                        transcriptSeqs.put(gene, sb.toString());
                    }
                    gene = line.split(" ")[0];
                    sb = new StringBuilder();
                } else {
                    sb.append(line);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            //add it to map
            gene = gene.substring(1).split(" ")[0];
            transcriptSeqs.put(gene, sb.toString());
        }

        //write to file and update the input config
        //This could probably (and should) be done for both
        //input transcriptomes. However, PAREsnip2 probably handles
        //this all no problem. But best to be safe than sorry!!
    }

    private void performTargetPrediction() {
        /**
         * Here, I will input the results of nat-siRNA prediction into PAREsnip2
         * to search for any potential targets and give some indication into
         * their function. This should be a secondary analysis step. e.g. we
         * should have results first for the nat-siRNA prediction and then for
         * the target prediction
         *
         */

        HashMap<String, Integer> toWrite = new HashMap();

        for (NATsiRNA siRNA : paresnip2Candidates) {
            String seq = siRNA.getSequence();
            Integer abundance = siRNA.getAbundance();

            if (!toWrite.containsKey(seq)) {
                toWrite.put(seq, abundance);
            }
        }

        File candidateNats = new File(input.getOutputDir().getAbsolutePath() + File.separator + "candidate_NATsiRNAs.fa");

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(candidateNats));
            for (String seq : toWrite.keySet()) {
                for (int i = 0; i < toWrite.get(seq); i++) {
                    bw.write(">" + seq);
                    bw.write("\n");
                    bw.write(seq);
                    bw.write("\n");
                }
                bw.flush();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Paresnip2Configuration ps2Config = Paresnip2Configuration.getInstance();
        RuleSet ps2Ruleset = RuleSet.getRuleSet();
        Paresnip2InputFiles ps2Input = Paresnip2InputFiles.getInstance();

        if (input.getPAREsnip2Config() == null) {
            ps2Config.setDefaultNATsiParameters();
        } else {
            try {
                ps2Config.loadConfig(input.getPAREsnip2Config());
            } catch (Exception ex) {
                Logger.getLogger(NATsiRNA_PredictionEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (input.getPAREsnip2TargetingRules() == null) {
            ps2Ruleset.setDefaultAllen();
        } else {
            try {
                ps2Ruleset.loadRules(input.getPAREsnip2TargetingRules());
            } catch (IOException ex) {
                Logger.getLogger(NATsiRNA_PredictionEngine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException ex) {
                Logger.getLogger(NATsiRNA_PredictionEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ps2Input.addDegradomeReplicate(redundantDegradome);
        ps2Input.addSmallRNAReplicate(candidateNats);
        ps2Input.addTranscriptome(input.getTranscript());
        ps2Input.setOuputDirectory(input.getOutputDir());

        Engine ps2Engine = new Engine();

    }

    private void calculateAlignmentDensities() {
        /**
         * Here, we will determine the sRNA alignment enrichment scores at the
         * overlapping region between two transcripts. A p-value will also be
         * calculated to determine if this enrichment score is significant. This
         * is based off of the PlantNATsDB method.
         * http://bis.zju.edu.cn/pnatdb/document/
         *
         *
         * No longer convinced this is a good method. Should investigate other
         * papers to see how they calculate this score + p-val.
         *
         * Perhaps consider siRNA density instead?
         * http://repository.essex.ac.uk/14289/1/DNA%20Res-2015-Yuan-233-43.pdf
         * https://genome.cshlp.org/content/early/2008/12/03/gr.084806.108.full.pdf
         *
         * Likely we would want to see higher siRNA alignment density in the
         * overlapping region of two NAT pairs compared to the whole
         * transcripts.
         *
         * For each NAT (i.e., cis-NATs or trans-NATs), we computed the
         * densities of small RNA loci in the overlapping region and along the
         * whole regions of the two NAT genes as follows: We first counted the
         * number of unique small RNAs, No, mapping to the overlapping region
         * and the total number of unique small RNAs, Ng, matching the two
         * genes. Then we measured the length of the overlapping region, Lo, and
         * the sum of the length of the two genes, Lg. Finally, the ratios No/Lo
         * and Ng/Lg were considered as small-RNA locus densities in the
         * overlapping regions and the whole regions of the NAT genes,
         * respectively
         */

        for (NATPair nat : processedNATs) {
            calcAlignmentDensities(nat);

        }

    }

    private void categorizeSmallRNAs() {
        /**
         * Here, we will categorize the predicted nat-siRNAs into one of
         * four/five groups. These will be based on the data regarding their
         * biogenesis. E.g. is the mature and star present. Is there 2nt (+/- 1)
         * overhang? Is there degradome evidence for dicer cleavage?
         *
         * The small RNA in question is always considered the 'mature' strand.
         *
         * Group 1: mature and star strand present with 2nt overhang at 5' end
         * and dicer cleavage evidence
         *
         * Group 2: mature and star strand present with 2nt overhang at 5' end
         * and dicer cleavage evidence for mature seq
         *
         * Group 3: Mature and star strand present with 2nt overhang
         *
         * Group 4: mature present with dicer cleavage
         *
         * Group 5: Just the small RNA
         *
         */

        for (NATsiRNA siRNA : predictedSmallRNAs) {

            String query = siRNA.getNat().getQueryName();
            String subject = siRNA.getNat().getSubjectName();

            //Gene name -> position -> sequence -> abundance
            //private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> smallRNA_alignment_map = new HashMap();
            HashMap<Integer, HashMap<String, Integer>> querySmallRNAAlignments = smallRNA_alignment_map.get(query);
            HashMap<Integer, HashMap<String, Integer>> subjectSmallRNAAlignments = smallRNA_alignment_map.get(subject);

            HashMap<Integer, HashMap<String, Integer>> queryTagAlignments = degradome_alignment_map.get(query);
            HashMap<Integer, HashMap<String, Integer>> subjectTagAlignments = degradome_alignment_map.get(subject);

            //Need to somehow determine the location of the star sequence
            //based on the overlap position and alignment position
            //Check if sRNA originated from query of subject strand
            if (querySmallRNAAlignments != null) {
                if (querySmallRNAAlignments.get(siRNA.getAlignmentPosition()) != null) {
                    if (querySmallRNAAlignments.get(siRNA.getAlignmentPosition()).containsKey(siRNA.getSequence())) {
                        siRNA.setAlignmentGene(query);
                        siRNA.setOppositeGene(subject);
                    }
                }
            }

            if (subjectSmallRNAAlignments != null) {
                if (subjectSmallRNAAlignments.get(siRNA.getAlignmentPosition()) != null) {
                    if (subjectSmallRNAAlignments.get(siRNA.getAlignmentPosition()).containsKey(siRNA.getSequence())) {
                        siRNA.setAlignmentGene(subject);
                        siRNA.setOppositeGene(query);
                    }
                }
            }

            //the position of the opposite strand where the star sequence should align
            //Currently we are assuming 2nt overhang and nothing else...
            boolean starStrand = false;
            boolean matureDeg = false;
            boolean starDeg = false;

            if (siRNA.getAlignmentGene().equals(query)) {
                //check if star sequence exists...
                if (subjectSmallRNAAlignments != null) {
                    int starSite = siRNA.getNat().getQueryEnd() - (siRNA.getAlignmentPosition() + siRNA.getSequence().length() - 1) + 2;
                    starSite = starSite + siRNA.getNat().getSubjectStart();
                    //Does it exist with canonical 2nt overhang?
                    if (subjectSmallRNAAlignments.containsKey(starSite)) {
                        //Does the siRNA have to be the same length?
                        //for now assume yes...
                        for (String starSeq : subjectSmallRNAAlignments.get(starSite).keySet()) {
                            if (starSeq.length() == siRNA.getSequence().length()) {
                                starStrand = true;
                                siRNA.setStarAlignmentPosition(starSite);
                                siRNA.setStarSequence(starSeq);
                                //Check if star deg exists

                                if (subjectTagAlignments != null) {
                                    if (subjectTagAlignments.containsKey(starSite + starSeq.length())) {
                                        starDeg = true;
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }

                //check if mature deg exists
                if (queryTagAlignments != null) {
                    if (queryTagAlignments.containsKey(siRNA.getAlignmentPosition() + siRNA.getSequence().length())) {
                        matureDeg = true;
                    }
                }

            } else {
                //check if star sequence exists...
                if (querySmallRNAAlignments != null) {
                    int starSite = siRNA.getNat().getSubjectEnd() - (siRNA.getAlignmentPosition() + siRNA.getSequence().length() - 1) + 2;
                    starSite = starSite + siRNA.getNat().getQueryStart();
                    //Does it exist with canonical 2nt overhang?
                    if (querySmallRNAAlignments.containsKey(starSite)) {
                        //Does the siRNA have to be the same length?
                        //for now assume yes...
                        for (String starSeq : querySmallRNAAlignments.get(starSite).keySet()) {
                            if (starSeq.length() == siRNA.getSequence().length()) {
                                siRNA.setStarAlignmentPosition(starSite);
                                siRNA.setStarSequence(starSeq);
                                starStrand = true;

                                if (queryTagAlignments != null) {
                                    if (queryTagAlignments.containsKey(starSite + starSeq.length())) {
                                        starDeg = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                //check if mature deg exists
                if (subjectTagAlignments != null) {
                    if (subjectTagAlignments.containsKey(siRNA.getAlignmentPosition() + siRNA.getSequence().length())) {
                        matureDeg = true;
                    }
                }
            }

            //Determine the group based on biogenesis information
            if (starStrand) {
                if (matureDeg) {
                    if (starDeg) {
                        siRNA.setCategory(1);
                    } else {
                        siRNA.setCategory(2);
                    }
                } else {
                    siRNA.setCategory(3);
                }
            } else {
                if (matureDeg) {
                    siRNA.setCategory(4);
                } else {
                    siRNA.setCategory(5);
                }
            }

        }

    }

    private void initialAlignmentSearch() {
        /**
         * Here, we can use patman for the complementary alignment. If we take
         * the reverse complement of the sequence to be aligned, this will allow
         * us to search for complementary regions in the correct direction and
         * will also allow for mismatches.
         */

        HashSet<String> genePairs = new HashSet();
        candidateNATs = new ArrayList();

        patmanAlignmentFile = alignSeqs(candidateRegions, input.getTranscript());

        try {
            BufferedReader br = new BufferedReader(new FileReader(patmanAlignmentFile));
            String line;
            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String geneA = splits[0];
                String geneB = splits[1].split("_")[0];

                if (!genePairs.contains(geneA + "_" + geneB) && !genePairs.contains(geneB + "_" + geneA)) {
                    genePairs.add(geneA + "_" + geneB);
                    genePairs.add(geneB + "_" + geneA);
                    String geneA_seq = transcriptSeqs.get(geneA);
                    String geneB_seq = transcriptSeqs.get(geneB);

                    NATPair nat = new NATPair(geneA, geneA_seq, geneB, geneB_seq);
                    candidateNATs.add(nat);

                }

            }

            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        genePairs = null;

    }

    private File alignSeqs(File shortReads, File longReads) {

        String alignedFile = input.getOutputDir().getAbsolutePath() + File.separator + shortReads.getName().split("\\.")[0] + "_" + longReads.getName().split("\\.")[0] + ".patman";

        File patmanOutput = new File(alignedFile);
        patmanOutput.deleteOnExit();

        try {
            //patmanOutput.createNewFile();

            PatmanParams newP_Params = new PatmanParams();
            newP_Params.setMaxGaps(0);
            newP_Params.setMaxMismatches(0);
            newP_Params.setPreProcess(false);
            newP_Params.setPostProcess(false);
            newP_Params.setMakeNR(false);
            newP_Params.setPositiveStrandOnly(true);

            File tmpDir = new File(input.getOutputDir().getAbsolutePath() + File.separator + "/tmp");

            try {
                tmpDir.mkdir();
                PatmanRunner runner = new PatmanRunner(shortReads, longReads,
                        patmanOutput, tmpDir, newP_Params);
                runner.setUsingDatabase(false);

                Thread myThread = new Thread(runner);

                myThread.start();
                myThread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            tmpDir.deleteOnExit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return patmanOutput;
    }

    private void generateReads() {
        /**
         * Here, I will go through the small RNA alignments, looking for either
         * site specific or phased distributed patterns. The alignments can be
         * classified using either the genome biology paper criteria or the
         * ta-siRNA prediction paper. Degradome evidence can be used to increase
         * the sequence length of the site specific alignments and also act as a
         * confidence increase.
         */

        HashSet<String> readsToWrite = new HashSet();

        for (String gene : smallRNA_alignment_map.keySet()) {
            for (Integer position : smallRNA_alignment_map.get(gene).keySet()) {

                for (String smallRNA : smallRNA_alignment_map.get(gene).get(position).keySet()) {

                    StringBuilder sb = null;

                    if (isPhasedSmallRNA(smallRNA, position, smallRNA_alignment_map.get(gene))) {

                        int endPos = position + (config.getMinSmallRNALength() * config.getMinPhases());

                        //Build phased sRNA string. Check that the sequences are correct
                        sb = new StringBuilder();
                        sb.append(">").append(gene).append("_").append(position).append("_").append(endPos).append("\n");
                        sb.append(SequenceUtils.DNA.reverseComplement(transcriptSeqs.get(gene).substring(position - 1, endPos - 1)));

                    } else if (isContiguousSmallRNA(smallRNA, position, smallRNA_alignment_map.get(gene))) {
                        int endPos = position + config.getMinSmallRNALength() + config.getMinPhases() - 1;

                        //Build contiguous sRNA string. Check that the sequences are correct
                        sb = new StringBuilder();
                        sb.append(">").append(gene).append("_").append(position).append("_").append(endPos).append("\n");
                        sb.append(SequenceUtils.DNA.reverseComplement(transcriptSeqs.get(gene).substring(position - 1, endPos - 1)));
                    } else if (degradome_alignment_map.containsKey(gene)) {
                        if (isSiteSpecific(smallRNA, position, degradome_alignment_map.get(gene))) {
                            //extract sRNA + tag region
                            sb = new StringBuilder();
                            sb.append(">").append(gene).append("_").append(position).append("_").append((position + smallRNA.length() + config.getMinTagLength())).append("\n");
                            sb.append(SequenceUtils.DNA.reverseComplement(transcriptSeqs.get(gene).substring(position - 1, position + smallRNA.length() + config.getMinTagLength() - 1)));
                        }

                    }

                    if (sb != null) {
                        String toWrite = sb.toString();
                        if (!readsToWrite.contains(toWrite)) {
                            readsToWrite.add(toWrite);
                        }
                    }

                    //reset
                    sb = null;

                }

            }
        }

        try {

            candidateRegions = new File(input.getOutputDir().getAbsolutePath() + File.separator + "candidate_regions.fa");

            candidateRegions.deleteOnExit();

            BufferedWriter bw = new BufferedWriter(new FileWriter(candidateRegions));

            for (String seq : readsToWrite) {
                bw.write(seq);
                bw.write("\n");
                bw.flush();
            }

            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void alignReadsToTranscript() {
        BinarySearchAligner smallRNA_aligner = new BinarySearchAligner(input.getSmallRNAFile(), input.getTranscript(),
                config.getMinSmallRNALength(), config.getMaxSmallRNALength(),
                input.getOutputDir(), config.getNumberOfThreads(), true);

        BinarySearchAligner degradome_aligner
                = new BinarySearchAligner(input.getDegradomeFile(),
                        input.getTranscript(), config.getMinTagLength(),
                        config.getMaxTagLength(), input.getOutputDir(),
                        config.getNumberOfThreads(), true);

        smallRNA_aligner.run();
        degradome_aligner.run();

        smallRNA_alignments = smallRNA_aligner.getOutputFile();
        degradome_alignments = degradome_aligner.getOutputFile();
    }

    private void processAlignments() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(smallRNA_alignments));
            String line;

            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String geneName = splits[0];
                String sequence = splits[1].split("\\(")[0];
                int abundance = Integer.parseInt(splits[1].split("\\(")[1].split("\\)")[0]);
                int start = Integer.parseInt(splits[2]);

                if (!smallRNA_alignment_map.containsKey(geneName)) {
                    smallRNA_alignment_map.put(geneName, new HashMap());
                }

                if (!smallRNA_alignment_map.get(geneName).containsKey(start)) {
                    smallRNA_alignment_map.get(geneName).put(start, new HashMap());
                }

                smallRNA_alignment_map.get(geneName).get(start).put(sequence, abundance);
            }

            br = new BufferedReader(new FileReader(degradome_alignments));

            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String geneName = splits[0];
                String sequence = splits[1].split("\\(")[0];
                int abundance = Integer.parseInt(splits[1].split("\\(")[1].split("\\)")[0]);
                int start = Integer.parseInt(splits[2]);

                if (!degradome_alignment_map.containsKey(geneName)) {
                    degradome_alignment_map.put(geneName, new HashMap());
                }

                if (!degradome_alignment_map.get(geneName).containsKey(start)) {
                    degradome_alignment_map.get(geneName).put(start, new HashMap());
                }

                degradome_alignment_map.get(geneName).get(start).put(sequence, abundance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File makeNonRedundant(File seqs, File outputDirectory, int min, int max, boolean isDegradome) {

        smallRNAs = new HashMap();
        //Check that the low complexity part of this works (ie is the logic correct)
        HashSet<String> lowComplexSeqs = new HashSet();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        if (line.length() <= max && line.length() >= min) {

                            if (!smallRNAs.containsKey(line) && !lowComplexSeqs.contains(line)) {
                                if (!isInvalid(line)) {

                                    if (!isLowComplexity(line)) {
                                        smallRNAs.put(line, 1);
                                    } else {
                                        lowComplexSeqs.add(line);
                                    }

                                }
                            } else if (smallRNAs.containsKey(line)) {
                                smallRNAs.put(line, smallRNAs.get(line) + 1);
                            }
                        }

                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        File seqsNonRedundant = new File(outputDirectory + File.separator + seqs.getName().split("\\.")[0] + ".NR.fa");
        seqsNonRedundant.getParentFile().mkdirs();

        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(seqsNonRedundant));

            for (String seq : smallRNAs.keySet()) {
                int abundance = smallRNAs.get(seq);

                br.write(">" + seq + " (" + abundance + ")");
                br.newLine();
                br.write(seq);
                br.newLine();
                br.flush();

            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        seqsNonRedundant.deleteOnExit();

        System.out.println(
                "Finished making: " + seqs.getName() + " non-redundant.");

        return seqsNonRedundant;

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

    /**
     * Low complexity sequence checker using proportion of single, di, and tri
     * nucleotide composition. Published in PAREsnip2 paper.
     *
     * @param seq
     * @return
     */
    private boolean isLowComplexity(String seq) {

        if (!config.isLowComplexityFilter()) {
            return false;
        }

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

    private boolean isContiguousSmallRNA(String smallRNA, Integer position,
            HashMap<Integer, HashMap<String, Integer>> geneAlignmentPositions) {

        //These need to go both ways not just forward
        /**
         * Here, given an alignment site for a small RNA and all other alignment
         * sites, we can determine if there exists small RNAs alignments in a
         * contigious distribution pattern. In paper, speak about potential draw
         * back off this being depth of sequencing. You may be missing phases
         * from sequencing data (flaw with natpipe).
         */
        if (geneAlignmentPositions == null) {
            return false;
        }

        int minPhases = config.getMinPhases();

        boolean isPhased = true;

        int tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos += 1;
            if (!geneAlignmentPositions.containsKey(tempPos)) {
                return false;
            }
        }

        return isPhased;
    }

    private boolean isPhasedSmallRNA(String smallRNA, Integer position,
            HashMap<Integer, HashMap<String, Integer>> geneAlignmentPositions) {

        /**
         * Here, given an alignment site for a small RNA and all other alignment
         * sites, we can determine if there exists small RNAs alignments in a
         * phased distribution pattern. In paper, speak about potential draw
         * back off this being depth of sequencing. You may be missing phases
         * from sequencing data (flaw with natpipe).
         */
        if (geneAlignmentPositions == null) {
            return false;
        }

        int minPhases = config.getMinPhases();

        boolean isPhased = true;

        int tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos += smallRNA.length();
            if (!geneAlignmentPositions.containsKey(tempPos)) {
                return false;
            }
        }

        return isPhased;
    }

    private boolean isSiteSpecific(String smallRNA, Integer position,
            HashMap<Integer, HashMap<String, Integer>> degradome_AlignmentPositions) {
        /**
         * Here, given an alignment site for a small RNA and all degradome
         * alignment sites, determine if a small RNA has corresponding degradome
         * tag at the 3' end
         */

        if (degradome_AlignmentPositions == null) {
            return false;
        }

        int degPosition = position + smallRNA.length();

        return degradome_AlignmentPositions.containsKey(degPosition);
    }

    private void trimTranscriptAnnotation() {

        //Need to check if the file transcript.fa already exists here....
        input.setTranscript(trimAnnotation(input.getTranscript(), input.getOutputDir(), input.getTranscript().getName().split("\\.")[0] + "_trimmed.fa"));

    }

    private File trimAnnotation(File fasta, File outputDir, String outFileName) {

        String fullTranscriptAnnotaiton = null;
        transcriptAnnotation = new HashMap();

        if (outputDir.exists() && !outputDir.isDirectory()) {
            System.out.println(outputDir + " is not a directory! Exiting...");
            System.exit(1);
        }

        File returnFile = new File(outputDir.getAbsolutePath() + File.separator + outFileName);

        String line;
        StringBuilder sb = null;
        String geneName = null;

        BufferedWriter bw = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fasta));
            bw = new BufferedWriter(new FileWriter(returnFile));
            while ((line = br.readLine()) != null) {

                if (line.startsWith(">")) {

                    if (sb != null) {
                        //Write to file 
                        bw.write(geneName + "\n");
                        bw.write(sb.toString() + "\n");
                        bw.flush();

                        transcriptAnnotation.put(geneName, fullTranscriptAnnotaiton);

                    }

                    //update for next sequence
                    fullTranscriptAnnotaiton = line;
                    geneName = line.split(" ")[0];
                    sb = new StringBuilder();

                } else {
                    sb.append(line);
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.write(geneName + "\n");
                    bw.write(sb.toString() + "\n");
                    bw.close();
                    transcriptAnnotation.put(geneName, fullTranscriptAnnotaiton);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        returnFile.deleteOnExit();
        return returnFile;

    }

    private void calcAlignmentDensities(NATPair nat) {

        //Also keep track of non-redundant alignments here
        //calc proportion of aignments within overlapping region
        String query = nat.getQueryName();
        String subject = nat.getSubjectName();

        int queryOverlappingStart = nat.getQueryStart();
        int queryOverlappingEnd = nat.getQueryEnd();

        int queryEnd = transcriptSeqs.get(query).length(); // -1?

        int subjectOverlappingStart = nat.getSubjectStart();
        int subjectOverlappingEnd = nat.getSubjectEnd();

        int subjectEnd = transcriptSeqs.get(subject).length(); // -1?

        int overlappingLength = nat.getOverlappingLength() * 2;
        int totalLength = queryEnd + subjectEnd;

        //Gene name -> position -> sequence -> abundance
        //private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> smallRNA_alignment_map = new HashMap();
        HashMap<Integer, HashMap<String, Integer>> queryAlignments = smallRNA_alignment_map.get(query);
        HashMap<Integer, HashMap<String, Integer>> subjectAlignments = smallRNA_alignment_map.get(subject);

        int wholeQueryAlignments = 0;
        int wholeSubjectAlignments = 0;
        int overlapQueryAlignments = 0;
        int overlapSubjectAlignments = 0;
        int wholeQueryAlignmentsNR = 0;
        int wholeSubjectAlignmentsNR = 0;
        int overlapQueryAlignmentsNR = 0;
        int overlapSubjectAlignmentsNR = 0;

        int overlapQueryA = 0, overlapSubjectA = 0;

        //Iterate over and count number of alignments for each transcript
        //Query
        Iterator<Entry<Integer, HashMap<String, Integer>>> it;

        if (queryAlignments != null) {
            it = queryAlignments.entrySet().iterator();

            while (it.hasNext()) {
                Entry<Integer, HashMap<String, Integer>> e = it.next();

                int pos = e.getKey();
                int numAlignments = e.getValue().size();

                for (Integer i : e.getValue().values()) {
                    wholeQueryAlignmentsNR += i;
                }

                wholeQueryAlignments += numAlignments;

                if (pos >= queryOverlappingStart && pos <= queryOverlappingEnd) {

                    for (Integer i : e.getValue().values()) {
                        overlapQueryAlignmentsNR += i;
                    }

                    overlapQueryA++;
                    overlapQueryAlignments += numAlignments;

                    for (String seq : e.getValue().keySet()) {
                        NATsiRNA siRNA = new NATsiRNA(seq, nat, pos);
                        siRNA.setAbundance(smallRNAs.get(seq));
                        predictedSmallRNAs.add(siRNA);
                    }
                }

            }
        }

        //Subject
        if (subjectAlignments != null) {
            it = subjectAlignments.entrySet().iterator();

            while (it.hasNext()) {
                Entry<Integer, HashMap<String, Integer>> e = it.next();

                int pos = e.getKey();
                int numAlignments = e.getValue().size();

                for (Integer i : e.getValue().values()) {
                    wholeSubjectAlignmentsNR += i;
                }

                wholeSubjectAlignments += numAlignments;

                if (pos >= subjectOverlappingStart && pos <= subjectOverlappingEnd) {
                    overlapSubjectA++;
                    overlapSubjectAlignments += numAlignments;

                    for (Integer i : e.getValue().values()) {
                        overlapSubjectAlignmentsNR += i;
                    }

                    for (String seq : e.getValue().keySet()) {
                        NATsiRNA siRNA = new NATsiRNA(seq, nat, pos);
                        siRNA.setAbundance(smallRNAs.get(seq));
                        predictedSmallRNAs.add(siRNA);
                    }
                }

            }
        }

        //Calc densities
        double overlappingDensity;
        double overalDensity;

        //overlappingDensity = ((double) overlapQueryAlignments + (double) overlapSubjectAlignments) / (double) overlappingLength;
        int wholeQueryA, wholeSubjectA;

        if (queryAlignments != null) {
            wholeQueryA = queryAlignments.size();
        } else {
            wholeQueryA = 0;
        }

        if (subjectAlignments != null) {
            wholeSubjectA = subjectAlignments.size();
        } else {
            wholeSubjectA = 0;
        }

        overlappingDensity = (double) (overlapQueryA + overlapSubjectA) / (double) overlappingLength;
        nat.setOverlappingDensitity(overlappingDensity);

        overalDensity = (double) (wholeQueryA + wholeSubjectA) / (double) totalLength;
        nat.setOveralDensitiy(overalDensity);

        //This might need to be checked over....
        nat.setQueryTotalAlignments(wholeQueryAlignments);
        nat.setQueryOverlapAlignments(overlapQueryAlignments);

        nat.setSubjectTotalAlignments(wholeSubjectAlignments);
        nat.setSubjectOverlapAlignments(overlapSubjectAlignments);

        nat.setQueryTotalAlignmentsNR(wholeQueryAlignmentsNR);
        nat.setQueryOverlapAlignmentsNR(overlapQueryAlignmentsNR);

        nat.setSubjectTotalAlignmentsNR(wholeSubjectAlignmentsNR);
        nat.setSubjectOverlapAlignmentsNR(overlapSubjectAlignmentsNR);

    }

    private void determineDistributionType() {

        //This needs to be potentially reworked...
        //When tested with just 1 small RNA it should be site-specific...
        /*
           Starting from the first reads closest to the 5' end of a cis-NAT, 
           reads were clustered if the positions of their first nucleotides were
           within a ten-nucleotide long segment. Clusters with more than five 
           reads were retained for further analysis.

           We calculated two metrics for each cis-NAT: 
           1) the number of small-RNA clusters and 
           2) the percentage of the number of reads within these clusters 
           relative to the total number of reads mapped to the whole cis-NAT.

           We then categorized a cis-NAT to have a site-specific pattern if 
           1) it had no more than ten siRNA clusters and 
           2) the percentage of the reads within the clusters was greater 
           than 50%; otherwise we categorized the cis-NAT to have 
           a distributed pattern.
         */
        HashMap<String, List<AlignmentCluster>> transcriptAlignmentClusters = new HashMap();

        for (NATsiRNA siRNA : predictedSmallRNAs) {

            if (transcriptAlignmentClusters.get(siRNA.getNat().getQueryName()) == null) {
                transcriptAlignmentClusters.put(siRNA.getNat().getQueryName(), getClusters(siRNA.getNat().getQueryName()));
            }

            if (siRNA.getNat().getQueryDistributionType() == null) {
                //Calc for query
                List<AlignmentCluster> queryClusters = transcriptAlignmentClusters.get(siRNA.getNat().getQueryName());

                if (queryClusters.size() > 10) {

                    siRNA.getNat().setQueryDistributionType("Distributed");

                } else {

                    int totalInClusters = 0;
                    for (AlignmentCluster c : queryClusters) {
                        totalInClusters += c.getNumAlignments();
                    }

                    //this needs to be updated to redundant reads that align not just non-redundant reads...
                    if (totalInClusters >= ((double) siRNA.getNat().getQueryTotalAlignmentsNR() * 0.5)) {
                        siRNA.getNat().setQueryDistributionType("Site-specific");
                    } else {
                        siRNA.getNat().setQueryDistributionType("Distributed");
                    }
                }
            }

            if (transcriptAlignmentClusters.get(siRNA.getNat().getSubjectName()) == null) {
                transcriptAlignmentClusters.put(siRNA.getNat().getSubjectName(), getClusters(siRNA.getNat().getSubjectName()));
            }

            if (siRNA.getNat().getSubjectDistributionType() == null) {
                //Calc for subject
                List<AlignmentCluster> subjectClusters = transcriptAlignmentClusters.get(siRNA.getNat().getSubjectName());

                if (subjectClusters.size() > 10) {

                    siRNA.getNat().setSubjectDistributionType("Distributed");

                } else {

                    int totalInClusters = 0;
                    for (AlignmentCluster c : subjectClusters) {
                        totalInClusters += c.getNumAlignments();
                    }

                    if (totalInClusters >= ((double) siRNA.getNat().getSubjectTotalAlignmentsNR() * 0.5)) {
                        siRNA.getNat().setSubjectDistributionType("Site-specific");
                    } else {
                        siRNA.getNat().setSubjectDistributionType("Distributed");
                    }
                }
            }

        }

    }

    private List<AlignmentCluster> getClusters(String gene) {

        List<AlignmentCluster> list = new ArrayList();

        //Gene name -> position -> sequence -> abundance
        //private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> smallRNA_alignment_map = new HashMap();
        HashMap<Integer, HashMap<String, Integer>> alignments = smallRNA_alignment_map.get(gene);

        //Sort based on alignment site
        if (alignments != null) {
            SortedSet<Integer> sortedKeys = new TreeSet<>(alignments.keySet());

            int windowSize = 10;
            AlignmentCluster prev = null;

            for (Integer pos : sortedKeys) {
                if (prev == null) {
                    // prev = new AlignmentCluster(pos, alignments.get(pos).size());
                    prev = new AlignmentCluster(pos, 0);

                    for (Integer i : alignments.get(pos).values()) {
                        prev.incrementAlignments(i);
                    }
                } else {
                    if (pos - prev.getStartSite() <= windowSize) {
                        //prev.incrementAlignments(alignments.get(pos).size());
                        for (Integer i : alignments.get(pos).values()) {
                            prev.incrementAlignments(i);
                        }
                    } else {
                        if (prev.getNumAlignments() >= 5) {
                            list.add(prev);
                        }
                        //prev = new AlignmentCluster(pos, alignments.get(pos).size());
                        prev = new AlignmentCluster(pos, 0);

                        for (Integer i : alignments.get(pos).values()) {
                            prev.incrementAlignments(i);
                        }
                    }
                }
            }

            if (prev != null && prev.getNumAlignments() >= 5) {
                list.add(prev);
            }
        }

        return list;

    }

    private void outputSmallRNAResults() {

        //ID, Sequence, Category, Originating gene, Alignment position, NAT gene,
        //NAT type, Alignment distribution, Originating gene overlap start, 
        //Originating gene overlap end, NAT overlap start, NAT overlap end, 
        //Originating gene alignment density, Originating gene overlapping alignment density,
        //alignment ratios? 
        //RNAplex alignment 
        paresnip2Candidates = new ArrayList();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(input.getOutputDir().getAbsolutePath() + File.separator + "predicted_NATsiRNAs.csv")));
            //I excluded the Gene A alignment density bit until I can confirm it is correct and working as intendedS
            bw.write("#, Sequence, Read abundance, Biogenesis category, Gene A (originating transcript), Alignment site, Gene B (corresponding NAT), Star sequence, Star alignment site, Type, Coverage type, Alignment distribution, Gene A length, Gene A overlap start,"
                    + "Gene A overlap end, Gene B length, Gene B overlap start, Gene B overlap end, NAT alignment density, NAT overlap alignment density");
            bw.write("\n");

            int recordID = 1;

            Collections.sort(predictedSmallRNAs);

            for (NATsiRNA siRNA : predictedSmallRNAs) {
                if (!config.isCisOnly() || (config.isCisOnly() && siRNA.getNat().getType().contains("cis"))) {
                    if (siRNA.getAbundance() >= config.getMinSmallRNAAbundance() && config.allowedBiogenesisCategory(siRNA.getCategory())) {
                        if (isValid(siRNA)) {
                            bw.write(recordID++ + "," + siRNA + "\n");
                            paresnip2Candidates.add(siRNA);
                        }
                    }
                }
            }

            bw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isValid(NATsiRNA siRNA) {

        //Check if the predicted NATsiRNA meets the criteria in the config
        String smallRNA = siRNA.getSequence();
        Integer position = siRNA.getAlignmentPosition();
        HashMap<Integer, HashMap<String, Integer>> smallRNAAlignmentPositions = smallRNA_alignment_map.get(siRNA.getAlignmentGene());
        HashMap<Integer, HashMap<String, Integer>> degradomeAlignmentPositions = degradome_alignment_map.get(siRNA.getAlignmentGene());

        //These need to go both ways not just forward
        /**
         * Here, given an alignment site for a small RNA and all other alignment
         * sites, we can determine if there exists small RNAs alignments in a
         * contigious distribution pattern. In paper, speak about potential draw
         * back off this being depth of sequencing. You may be missing phases
         * from sequencing data (flaw with natpipe).
         */
        if (smallRNAAlignmentPositions == null) {
            return false;
        }

        int minPhases = config.getMinPhases();

        boolean isPhasedForward = true;

        int tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos += smallRNA.length();
            if (!smallRNAAlignmentPositions.containsKey(tempPos)) {
                isPhasedForward = false;
            }
        }

        boolean isPhasedBackwards = true;

        tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos -= smallRNA.length();
            if (!smallRNAAlignmentPositions.containsKey(tempPos)) {
                isPhasedBackwards = false;
            }
        }

        if (isPhasedForward || isPhasedBackwards) {
            return true;
        }

        isPhasedForward = true;

        tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos += 1;
            if (!smallRNAAlignmentPositions.containsKey(tempPos)) {
                isPhasedForward = false;
            }
        }

        isPhasedBackwards = true;

        tempPos = position;

        //we already have one 'phase' so check for the num phases
        //equal to the config parameter - 1
        for (int i = 0; i < minPhases - 1; i++) {
            tempPos -= 1;
            if (!smallRNAAlignmentPositions.containsKey(tempPos)) {
                isPhasedBackwards = false;
            }
        }

        if (isPhasedForward || isPhasedBackwards) {
            return true;
        }

        if (degradomeAlignmentPositions == null) {
            return false;
        }

        int degPosition = position + smallRNA.length();

        return degradomeAlignmentPositions.containsKey(degPosition);

    }
    


}
