/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.BinarySearchAligner;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.ShortRead;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.ShortReadAlignment;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Given a set of validated miRNA target duplex in Ath and a set of PAREameters
 * results. It will compare the predicted targets against the known targets with
 * category 0 or 1 degradome signals
 *
 * @author rew13hpu
 */
public class PAREametersEvaluator {

    HashMap<String, Integer> smallRNAs;
    File smallRNAFile;
    File degradomeFile;
    File transcriptFile;
    File validatedTargets;
    File outputDirectory;
    HashMap<String, List<ShortReadAlignment>> processedReads;
    File alignmentFileOut;
    HashMap<String, HashMap<String, Integer>> shouldFind;
    PAREametersInput input;
    PAREametersConfiguration config;
    File PAREametersResultsDir;
    File paresnip2Results;

    public static void outputKnownMicroRNATargets(File smallRNAFile, File degradomeFile, File transcriptFile, File outputDirectory) {
        //Given a set of small RNAs and degradome, output the known
        //miRNAs with targets from the set of validated targets.

        //These can be used for evaluating a set of parameters on
        //the Ath data
        PAREametersEvaluator ev = new PAREametersEvaluator(smallRNAFile, degradomeFile, transcriptFile, outputDirectory);
        ev.writeMicroRNAs();

    }

    private PAREametersEvaluator(File smallRNAFile, File degradomeFile, File transcriptFile, File outputDirectory) {
        shouldFind = new HashMap();
        config = PAREametersConfiguration.getInstance();
        //check if it checks tag abundance...
        config.setMinSmallRNAAbundance(5);
        config.setMinTagAbundance(5);
        this.smallRNAFile = smallRNAFile;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        this.outputDirectory = outputDirectory;
        this.PAREametersResultsDir = outputDirectory;
        this.validatedTargets = new File("D:\\PAREameters\\Arabidopsis_thaliana\\alignment_predictions.csv");
        this.paresnip2Results = new File(PAREametersResultsDir.getAbsolutePath() + File.separator + "present_targets_miRNAs_degradome.csv");
    }

    private void writeMicroRNAs() {
        degradomeFile = makeNonRedundant(degradomeFile, config.getMinTagLength(), config.getMaxTagLength());
        smallRNAFile = makeNonRedundant(smallRNAFile, config.getMinSmallRNALength(), config.getMaxSmallRNALength());

        BinarySearchAligner bsa = new BinarySearchAligner(degradomeFile, transcriptFile, 19, 21, outputDirectory, 12, true);
        bsa.run();
        alignmentFileOut = new File(outputDirectory.getAbsoluteFile() + File.separator + degradomeFile.getName().split("\\.")[0]
                + "_" + transcriptFile.getName().split("\\.")[0] + ".align");
        alignmentFileOut.deleteOnExit();
        readAlignments(alignmentFileOut);
        checkIfTargetPresent(true);
    }

    public PAREametersEvaluator(File smallRNAFile, File degradomeFile, File transcriptFile, File outputDirectory, File PAREametersResultsDir) {
        shouldFind = new HashMap();
        config = PAREametersConfiguration.getInstance();
        config.setMinSmallRNAAbundance(5);
        config.setMinTagAbundance(5);
        this.smallRNAFile = smallRNAFile;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        this.outputDirectory = outputDirectory;
        this.PAREametersResultsDir = PAREametersResultsDir;
        this.validatedTargets = new File("D:\\PAREameters\\Arabidopsis_thaliana\\alignment_predictions.csv");
        this.paresnip2Results = new File(PAREametersResultsDir.getAbsolutePath() + File.separator + "present_targets_miRNAs_degradome.csv");
    }
    
    public PAREametersEvaluator(File smallRNAFile, File degradomeFile, File transcriptFile, File outputDirectory, File testSet, File paresnip2Results) {
        shouldFind = new HashMap();
        config = PAREametersConfiguration.getInstance();
        config.setMinSmallRNAAbundance(5);
        config.setMinTagAbundance(5);
        this.smallRNAFile = smallRNAFile;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        this.outputDirectory = outputDirectory;
        this.validatedTargets = testSet;
        this.paresnip2Results = paresnip2Results;
    }

    public PAREametersEvaluator() {

        //Data structures needed
        shouldFind = new HashMap();

        this.input = PAREametersInput.getInstance();
        this.config = PAREametersConfiguration.getInstance();
        this.PAREametersResultsDir = input.getOutputDir();
        this.validatedTargets = new File("D:\\PAREameters\\Arabidopsis_thaliana\\alignment_predictions.csv");
        this.smallRNAFile = input.getSmallRNAs();
        this.degradomeFile = input.getDegradome();
        this.transcriptFile = input.getTranscriptome();

    }

    public void process() {

        degradomeFile = makeNonRedundant(degradomeFile, config.getMinTagLength(), config.getMaxTagLength());
        smallRNAFile = makeNonRedundant(smallRNAFile, config.getMinSmallRNALength(), config.getMaxSmallRNALength());

        BinarySearchAligner bsa = new BinarySearchAligner(degradomeFile, transcriptFile, config.getMinTagLength(), config.getMaxTagLength(), outputDirectory, 12);
        bsa.run();
        alignmentFileOut = new File(outputDirectory.getAbsoluteFile() + File.separator + degradomeFile.getName().split("\\.")[0]
                + "_" + transcriptFile.getName().split("\\.")[0] + ".align");
        alignmentFileOut.deleteOnExit();
        readAlignments(alignmentFileOut);
        checkIfTargetPresent(false);
        checkFoundTargets();

    }

    void checkFoundTargets() {
        
        File resultsFile = paresnip2Results;

        //File resultsFile = new File("D:\\PAREameters_dalmay_data\\WTC\\inferred\\present_targets_miRNAs_degradome.csv");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDirectory.getAbsolutePath() + File.separator + "compare_to_validated.csv")));
            bw.write("Number of known miRNA with validated targets present: " + shouldFind.size());
            bw.newLine();

            int count = 0;

            //Count the number of miRNAs with targets in sample
            for (HashMap<String, Integer> map : shouldFind.values()) {
                count += map.size();
            }
            bw.write("Number of known miRNA targets present with category 0/1 signal: " + count);
            bw.newLine();

            int countValidated = 0;
            int countUnvalidated = 0;

            BufferedReader br = new BufferedReader(new FileReader(resultsFile));
            //read header line
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                line += "\n";
                line += br.readLine();
                line += "\n";
                line += br.readLine();

                String splits[] = line.split(",");

                String geneID = splits[2].split("\\.")[0].replace("\"", "");
                Integer cleavePos = Integer.parseInt(splits[4]);
                String alignment_splits[] = splits[10].split("'");
                String seq = alignment_splits[1].trim().replace("-", "");

                if (shouldFind.containsKey(seq)) {
                    if (shouldFind.get(seq).containsKey(geneID)) {
                        if (shouldFind.get(seq).get(geneID).equals(cleavePos)) {
                            countValidated++;
                        } else {
                            countUnvalidated++;
                        }
                    } else {
                        countUnvalidated++;
                    }
                } else {
                    countUnvalidated++;
                }
            }

            bw.write("Number of validated targets found: " + countValidated);
            bw.newLine();
            bw.write("Number of non-validated targets found: " + countUnvalidated);
            bw.flush();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void checkIfTargetPresent(boolean writeToFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(validatedTargets));
            BufferedWriter targetsPresent = null;
            BufferedWriter miRNAsWithTargets = null;

            if (writeToFile) {
                targetsPresent = new BufferedWriter(new FileWriter(new File(outputDirectory.getAbsolutePath() + File.separator + "present_targets_cat_0_1.csv")));
                miRNAsWithTargets = new BufferedWriter(new FileWriter(new File(outputDirectory.getAbsolutePath() + File.separator + "present_targets_miRNAs.fa")));
            }
            String line;
            String header = br.readLine();
            if (writeToFile) {
                targetsPresent.write(header + "\n");
            }

            while ((line = br.readLine()) != null) {
                line += "\n";
                line += br.readLine();
                line += "\n";
                line += br.readLine();

                String splits[] = line.split(",");

                String id = splits[0].replace("\"", "");;
                String miRNA = splits[1].replace("\"", "");
                String gene = splits[2].replace("\"", "");
                Integer cleavePos = Integer.parseInt(splits[4]);

                if (smallRNAs.containsKey(miRNA) && smallRNAs.get(miRNA) >= config.getMinSmallRNAAbundance() && !isLowComplexity(miRNA)) {
                    if (determineCategories(gene, cleavePos)) {
                        if (!shouldFind.containsKey(miRNA)) {
                            shouldFind.put(miRNA, new HashMap<>());
                            if (writeToFile) {
                                for (int i = 0; i < 5; i++) {
                                    miRNAsWithTargets.write(id + "\n");
                                    miRNAsWithTargets.write(miRNA + "\n");
                                    miRNAsWithTargets.flush();
                                }

                            }
                        }

                        if (!shouldFind.get(miRNA).containsKey(gene)) {
                            shouldFind.get(miRNA).put(gene, cleavePos);
                            if (writeToFile) {
                                StringBuilder sb = new StringBuilder();

                                String write[] = line.split(",");

                                write[5] = String.valueOf(getTagAbundance(gene, cleavePos));
                                write[6] = String.valueOf(getTagWeightedAbundance(gene, cleavePos));
                                write[7] = String.valueOf(smallRNAs.get(miRNA));

                                for (String s : write) {
                                    sb.append(s).append(",");
                                }
                                sb.deleteCharAt(sb.length() - 1);

                                targetsPresent.write(sb.toString() + "\n");
                                targetsPresent.flush();
                            }
                        }
                    }                
                }
                 

            }
            if (writeToFile) {
                targetsPresent.close();
                miRNAsWithTargets.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void readAlignments(File alignmentFile) {

        //read through line by line and group alignments per gene
        //abundance at each position per gene is recorded
        //adjusted weighted abundance is calculated by adding the weighted abundance
        //of each tag that aligns to a particular position
        processedReads = new HashMap();
        HashMap<String, Integer> numAlignments = new HashMap();
        //Count the number of alignments for each sequence
        try {
            BufferedReader br = new BufferedReader(new FileReader(alignmentFile));

            String line = null;

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                String[] splits = line.split("\t");
                String seqInfo[] = splits[1].split("\\(");
                String seq = seqInfo[0];

                if (numAlignments.containsKey(seq)) {
                    numAlignments.put(seq, numAlignments.get(seq) + 1);
                } else {
                    numAlignments.put(seq, 1);
                }

            }

            br.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String line = null;
        ShortReadAlignment prev = null;
        String prevGene = "";
        String seq = null;
        Integer abundance = null;
        Integer pos = null;
        //colapse tags and calculate their weighted abudnance
        try {
            BufferedReader br = new BufferedReader(new FileReader(alignmentFile));

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                String[] splits = line.split("\t");
                String seqInfo[] = splits[1].split("\\(");
                seq = seqInfo[0];
                abundance = Integer.parseInt(seqInfo[1].split("\\)")[0]);
                pos = Integer.parseInt(splits[2]);
                splits[0] = splits[0].split("\\.")[0];

                if (prev == null || !prevGene.equals(splits[0])) {

                    if (!processedReads.containsKey(splits[0])) {
                        processedReads.put(splits[0], new ArrayList());
                        if (prev != null) {
                            processedReads.get(prevGene).add(prev);
                        }
                    }

                    //reset the position checker
                    prevGene = splits[0];
                    prev = new ShortReadAlignment(new ShortRead(seq, abundance), pos);
                    prev.getTag().setNumAlignmentsPositive(numAlignments.get(seq));
                    prev.getTag().calculateWeightedPositiveAbundance();

                } else //Check if there is another sequence that also aligns to this position
                if (prev.getGenePosition() == pos) {
                    //increment the abundance at that peak and add the weighted fragment abundance
                    prev.getTag().setAbundance(abundance + prev.getTag().getAbundance());
                    double curWeightedAbundance = (double) abundance / (double) numAlignments.get(seq);
                    prev.getTag().setAdjustedWeightedAbundance(curWeightedAbundance + prev.getTag().getWeightedPositiveAbundance());
                } else {

                    //add the peak to the processed reads
                    processedReads.get(splits[0]).add(prev);

                    //reset at the new position
                    prev = new ShortReadAlignment(new ShortRead(seq, abundance), pos);
                    prev.getTag().setNumAlignmentsPositive(numAlignments.get(seq));
                    prev.getTag().calculateWeightedPositiveAbundance();

                }

            }

            //alignmentFile.deleteOnExit();
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (prev != null) {
                if (!processedReads.get(prevGene).isEmpty()) {
                    if (prev.getGenePosition() == processedReads.get(prevGene).get(processedReads.get(prevGene).size() - 1).getGenePosition()) {
                        //increment the abundance at that peak and add the weighted fragment abundance

                        ShortReadAlignment p = processedReads.get(prevGene).get(processedReads.get(prevGene).size() - 1);

                        p.getTag().setAbundance(p.getTag().getAbundance() + prev.getTag().getAbundance());
                        double curWeightedAbundance = (double) prev.getTag().getAbundance() / (double) numAlignments.get(seq);
                        p.getTag().setAdjustedWeightedAbundance(curWeightedAbundance + p.getTag().getWeightedPositiveAbundance());
                    } else {
                        processedReads.get(prevGene).add(prev);
                    }
                } else {

                    //add the peak to the processed reads
                    processedReads.get(prevGene).add(prev);

                }
            }
        }

    }

    private int getTagAbundance(String gene, int pos) {

        List<ShortReadAlignment> list = new ArrayList(processedReads.get(gene));

        //sort on read abundance
        Collections.sort(list, new Comparator<ShortReadAlignment>() {
            @Override
            public int compare(ShortReadAlignment o1, ShortReadAlignment o2) {

                return Integer.compare(o1.getTag().getAbundance(), o2.getTag().getAbundance());

            }
        });

        if (list.get(list.size() - 1).getTag().getAbundance() > 1) {
            int abundance = list.get(list.size() - 1).getTag().getAbundance();

            int index = list.size() - 1;
            List<ShortReadAlignment> listOfConsideredPeaks = new ArrayList();

            while (index >= 0 && list.get(index).getTag().getAbundance() == abundance) {
                listOfConsideredPeaks.add(list.get(index));
                index--;
            }

            for (ShortReadAlignment sra : listOfConsideredPeaks) {
                if (sra.getGenePosition() == pos && sra.getTag().getAbundance() >= config.getMinTagAbundance()) {
                    return sra.getTag().getAbundance();
                }
            }

        }

        return 0;
    }

    private double getTagWeightedAbundance(String gene, int pos) {

        List<ShortReadAlignment> list = new ArrayList(processedReads.get(gene));

        //sort on read abundance
        Collections.sort(list, new Comparator<ShortReadAlignment>() {
            @Override
            public int compare(ShortReadAlignment o1, ShortReadAlignment o2) {

                return Integer.compare(o1.getTag().getAbundance(), o2.getTag().getAbundance());

            }
        });

        if (list.get(list.size() - 1).getTag().getAbundance() > 1) {
            int abundance = list.get(list.size() - 1).getTag().getAbundance();

            int index = list.size() - 1;
            List<ShortReadAlignment> listOfConsideredPeaks = new ArrayList();

            while (index >= 0 && list.get(index).getTag().getAbundance() == abundance) {
                listOfConsideredPeaks.add(list.get(index));
                index--;
            }

            for (ShortReadAlignment sra : listOfConsideredPeaks) {
                if (sra.getGenePosition() == pos && sra.getTag().getAbundance() >= config.getMinTagAbundance()) {
                    return sra.getTag().getAdjustedWeightedAbundance();
                }
            }

        }

        return 0.0;
    }

    private boolean determineCategories(String gene, int cleavagePos) {

        if (processedReads.containsKey(gene)) {
            List<ShortReadAlignment> list = new ArrayList(processedReads.get(gene));

            //sort on read abundance
            Collections.sort(list, new Comparator<ShortReadAlignment>() {
                @Override
                public int compare(ShortReadAlignment o1, ShortReadAlignment o2) {

                    return Integer.compare(o1.getTag().getAbundance(), o2.getTag().getAbundance());

                }
            });

            if (list.get(list.size() - 1).getTag().getAbundance() > 1) {
                int abundance = list.get(list.size() - 1).getTag().getAbundance();

                int index = list.size() - 1;
                List<ShortReadAlignment> listOfConsideredPeaks = new ArrayList();

                while (index >= 0 && list.get(index).getTag().getAbundance() == abundance) {
                    listOfConsideredPeaks.add(list.get(index));
                    index--;
                }

                for (ShortReadAlignment sra : listOfConsideredPeaks) {
                    if (sra.getGenePosition() == cleavagePos && sra.getTag().getAbundance() >= config.getMinTagAbundance()) {
                        return true;
                    }
                }

            }
            return false;
        }

        return false;

    }

    private File makeNonRedundant(File seqs, int min, int max) {

        smallRNAs = new HashMap<>();
        boolean lowComplexity = false;
        HashSet<String> checkedSeq = new HashSet();
        HashSet<String> lowComplexSeqs = new HashSet();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        if (line.length() <= max && line.length() >= min) {

                            if (!isInvalid(line)) {

                                if (!checkedSeq.contains(line)) {
                                    lowComplexity = isLowComplexity(line);
                                    checkedSeq.add(line);
                                    if (lowComplexity) {
                                        lowComplexSeqs.add(line);
                                    }
                                } else {
                                    lowComplexity = false;
                                }

                                if (!smallRNAs.containsKey(line) && !lowComplexSeqs.contains(line)) {
                                    smallRNAs.put(line, 1);
                                } else if (smallRNAs.containsKey(line)) {
                                    smallRNAs.put(line, smallRNAs.get(line) + 1);
                                }
                            }
                        }

                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
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

        //System.out.println("Finished making: " + seqs.getName() + " non-redundant.");
        return seqsNonRedundant;

    }

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
}
