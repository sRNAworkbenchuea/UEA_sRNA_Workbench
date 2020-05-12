/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Given a set of PAREsnip2 results, this class will process plots based on the
 * results
 *
 * @author rew13hpu
 */
public class PlotProducer {

    private final File PAREsnip2Results;
    private File outputDirectory;
    private HashMap<String, String> uniquePositions;
    private HashMap<Integer, File> lengthSplit;
    private HashMap<String, File> microRNADirectories;
    String speciesName;

    PlotProducer(File outputDir, File PAREsnip2Results) {

        this.outputDirectory = outputDir;
        this.PAREsnip2Results = PAREsnip2Results;
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }
    
     PlotProducer(File outputDir, File PAREsnip2Results, String speciesName) {

        this.outputDirectory = outputDir;
        this.PAREsnip2Results = PAREsnip2Results;
        this.speciesName = speciesName;
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public void produceMicroRNAPlots() {

        produceMicroRNADirectories();
        for (String s : microRNADirectories.keySet()) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            outputDirectory = microRNADirectories.get(s).getParentFile();
            processUniquePositions(microRNADirectories.get(s));
            producePositionPropertyPlots();
            produceAlignmentScorePlots();
            produceMFERatioPlots();
        }
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void produceLengthPlots() {
        lengthSplit = splitResultsOnLength();
        for (Integer key : lengthSplit.keySet()) {
            producePlots(key);
        }
    }

    public void producePlots() {
        processUniquePositions(PAREsnip2Results);
        producePositionPropertyPlots();
        produceAlignmentScorePlots();
        produceMFERatioPlots();
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

    }

    public void producePlots(int seqLengh) {
        if (lengthSplit.get(seqLengh) != null) {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            outputDirectory = lengthSplit.get(seqLengh).getParentFile();
            processUniquePositions(lengthSplit.get(seqLengh));
            producePositionPropertyPlots();
            produceAlignmentScorePlots();
            produceMFERatioPlots();
        }
    }

    void produceMicroRNADirectories() {
        microRNADirectories = new HashMap();
        outputDirectory = new File(outputDirectory.getAbsolutePath() + File.separator + "miRNA_specific");

        try {
            HashMap<String, BufferedWriter> map = new HashMap();
            BufferedReader reader = new BufferedReader(new FileReader(PAREsnip2Results));

            String line;

            //read the header of the file
            String header = reader.readLine();

            while ((line = reader.readLine()) != null) {
                line += "\n" + reader.readLine();
                line += "\n" + reader.readLine();

                String splits[] = line.split(",");
                String miRNA_annotation = splits[0].substring(1).split(" ")[0].replace("\"", "");
                String seq = splits[1].replace("\"", "");

                if (!microRNADirectories.containsKey(seq)) {
                    File miRNAInteractions = new File(outputDirectory.getAbsolutePath() + File.separator + miRNA_annotation + File.separator + "interaction_alignments.csv");
                    miRNAInteractions.getParentFile().mkdirs();
                    microRNADirectories.put(seq, miRNAInteractions);
                    map.put(seq, new BufferedWriter(new FileWriter(miRNAInteractions)));
                    map.get(seq).write(header + "\n");
                }

                map.get(seq).write(line + "\n");

            }

            for (String key : map.keySet()) {
                map.get(key).close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void processUniquePositions(File alignmentPredictionsFile) {
        uniquePositions = new HashMap();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(alignmentPredictionsFile));

            String line;

            //read the header of the file
            line = reader.readLine();
            int counter = 0;
            while ((line = reader.readLine()) != null) {
                line += reader.readLine();
                line += reader.readLine();

                line = line.replace("\t", ",");

                String splits[] = line.split(",");

                String gene = splits[2];
                Integer position = Integer.parseInt(splits[4]);
                Double score = Double.parseDouble(splits[11]);
                Double mfeRatio = Double.parseDouble(splits[14]);

                if (!uniquePositions.containsKey(gene + "-" + position + "-" + counter)) {
                    uniquePositions.put(gene + "-" + position + "-" + counter, line);
                    //uniquePositions.put(gene + "-" + position, line);
                    counter++;
                } else {
                    String currentBest = uniquePositions.get(gene + "-" + position);
                    String currentSplits[] = currentBest.split(",");
                    Double currentScore = Double.parseDouble(currentSplits[11]);
                    Double currentMfeRatio = Double.parseDouble(currentSplits[14]);

                    if (score < currentScore) {

                        uniquePositions.put(gene + "-" + position, line);

                    } else if (score.equals(currentScore)) {
                        if (mfeRatio < currentMfeRatio) {
                            uniquePositions.put(gene + "-" + position, line);
                        }
                    }

                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void producePositionPropertyPlots() {

        int[] totalNumMMs = new int[7];
        int[] totalNumGUs = new int[7];
        int[] totalNumAdjMMs = new int[7];
        int[] totalNumCoreMMs = new int[7];
        int[] totalNumCoreGUs = new int[7];
        int[] totalNumCoreAdjMMs = new int[7];

        int[][] basepairs = new int[24][11];

        //replot the graphs based on length of the miRNA
        try {

            int count = 0;
            int gaps[] = new int[24];
            int mm[] = new int[24];
            int wobble[] = new int[24];

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "output_info.txt")), true));

            System.out.println("---------------------------------------");
            System.out.println("A total of " + uniquePositions.size() + " miRNA/target duplexes are being considered...");
            System.out.println("---------------------------------------");

            for (String str : uniquePositions.values()) {

                int numMM = 0;
                int numGU = 0;
                int numAdjMM = 0;
                boolean prevMM = false;

                //This will be super useful to also include
                int coreRegionMM = 0;
                int coreRegionGU = 0;
                int coreRegionGap = 0;
                int coreRegionAdjMM = 0;

                //System.out.println(str);
                String splits[] = str.split(",");
                String alignments = splits[10].split("3")[1].trim();

                String alignment_splits[] = splits[10].split("'");

                //System.out.println(splits[6]);
                String smallRNA = alignment_splits[1];
                String mRNA = alignment_splits[3];

                int whitespace = 0;

                while (mRNA.charAt(whitespace) == ' ') {
                    whitespace++;
                }

                int temp = 0;
                while (smallRNA.charAt(temp) == ' ') {
                    temp++;
                }

                whitespace = Math.abs(temp - whitespace);

                smallRNA = smallRNA.trim();
                mRNA = mRNA.trim();
                alignments = alignments.trim();

                if (Double.parseDouble(splits[11]) > 0) {

                    boolean gapInSRNA = false;
                    int sRNAGapPos = 0;

                    for (int i = 0; i < smallRNA.length(); i++) {
                        if (smallRNA.charAt(i) == '-') {
                            gapInSRNA = true;
                            sRNAGapPos = i;
                        }
                    }

                    if (gapInSRNA) {
                        if (sRNAGapPos <= 9) {
                            int offset = -1;
                            for (int i = 10; i >= 0; i--) {
                                char s = smallRNA.charAt(i);
                                char m = mRNA.charAt(i + whitespace);

                                if (s == '-') {
                                    offset++;
                                    prevMM = true;
                                } else {
                                    if (isWobble(s, m)) {
                                        wobble[i + offset]++;
                                        numGU++;
                                        if ((i + offset) > 0) {
                                            coreRegionGU++;
                                        }
                                        prevMM = false;
                                    } else if (!isComplementary(s, m)) {

                                        mm[i + offset]++;
                                        numMM++;

                                        if ((i + offset) > 0) {
                                            coreRegionMM++;
                                        }

                                        if (prevMM) {
                                            numAdjMM++;
                                            if ((i + offset) > 0) {
                                                coreRegionAdjMM++;
                                            }
                                        }
                                        prevMM = true;

                                    } else {
                                        prevMM = false;
                                    }

                                }

                            }
                            for (int i = 11; i < smallRNA.length(); i++) {
                                char s = smallRNA.charAt(i);
                                char m = mRNA.charAt(i + whitespace);

                                if (isWobble(s, m)) {
                                    prevMM = false;
                                    wobble[i - 1]++;
                                    numGU++;
                                    if ((i - 1) < 13) {
                                        coreRegionGU++;
                                    }

                                } else if (!isComplementary(s, m)) {
                                    mm[i - 1]++;
                                    numMM++;

                                    if ((i - 1) < 13) {
                                        coreRegionMM++;
                                    }

                                    if (prevMM) {
                                        numAdjMM++;
                                        if ((i - 1) < 13) {
                                            coreRegionAdjMM++;
                                        }
                                    }
                                    prevMM = true;
                                } else {
                                    prevMM = false;
                                }
                            }
                        } else {
                            int offset = 0;
                            for (int i = 0; i < smallRNA.length(); i++) {
                                char s = smallRNA.charAt(i);
                                char m = mRNA.charAt(i + whitespace);
                                if (s == '-') {
                                    offset = -1;
                                    prevMM = true;
                                } else {
                                    if (isWobble(s, m)) {
                                        prevMM = false;
                                        wobble[i + offset]++;
                                        numGU++;

                                        if ((i + offset) > 0 && (i + offset) < 13) {
                                            coreRegionGU++;
                                        }
                                    } else if (!isComplementary(s, m)) {

                                        mm[i + offset]++;
                                        numMM++;

                                        if ((i + offset) > 0 && (i + offset) < 13) {
                                            coreRegionMM++;
                                        }

                                        if (prevMM) {
                                            numAdjMM++;
                                            if ((i + offset) > 0 && (i + offset) < 13) {
                                                coreRegionAdjMM++;
                                            }
                                        }
                                        prevMM = true;
                                    } else {
                                        prevMM = false;
                                    }
                                }
                            }

                        }
                    } else {

                        for (int i = 0; i < smallRNA.length(); i++) {
                            char s = smallRNA.charAt(i);
                            char m = mRNA.charAt(i + whitespace);

                            if (isWobble(s, m)) {
                                prevMM = false;
                                wobble[i]++;
                                numGU++;

                                if (i > 0 && i < 13) {
                                    coreRegionGU++;
                                }

                            } else if (isGap(s, m)) {
                                gaps[i]++;
                                numMM++;
                                if (i > 0 && i < 13) {
                                    coreRegionGap++;
                                    coreRegionMM++;
                                }
                                if (prevMM) {
                                    numAdjMM++;
                                    if (i > 0 && i < 13) {
                                        coreRegionAdjMM++;
                                    }
                                }
                                prevMM = true;
                            } else if (!isComplementary(s, m)) {

                                mm[i]++;
                                numMM++;
                                if (i > 0 && i < 13) {
                                    coreRegionMM++;
                                }
                                if (prevMM) {
                                    numAdjMM++;
                                    if (i > 0 && i < 13) {
                                        coreRegionAdjMM++;
                                    }
                                }
                                prevMM = true;
                            } else {
                                prevMM = false;
                            }
                        }

                    }

                }
                int offset = 0;
                for (int i = 0; i < smallRNA.length(); i++) {
                    if (smallRNA.charAt(i) == '-') {
                        offset = -1;
                        i++;
                    }
                    basepairs[i + offset][basepairPos(smallRNA.charAt(i), mRNA.charAt(i + whitespace))]++;
                }
                count++;
                totalNumGUs[numGU]++;
                totalNumMMs[numMM]++;
                totalNumAdjMMs[numAdjMM]++;
                totalNumCoreGUs[coreRegionGU]++;
                totalNumCoreMMs[coreRegionMM]++;
                totalNumCoreAdjMMs[coreRegionAdjMM]++;

            }


            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "basepair_property_plot.csv")), true));

            System.out.println("Position, A:U, G:C, G:U, A:M, U:M, G:M, C:M, A/-, U:-, G/-, C/-");
            for (int i = 0; i < basepairs.length; i++) {
                System.out.println((i + 1) + "," + ((double) basepairs[i][0] / (double) count) + "," + ((double) basepairs[i][1] / (double) count) + "," + ((double) basepairs[i][2] / (double) count) + "," + ((double) basepairs[i][3] / (double) count) + "," + ((double) basepairs[i][4] / (double) count) + ","
                        + ((double) basepairs[i][5] / (double) count) + "," + ((double) basepairs[i][6] / (double) count) + "," + ((double) basepairs[i][7] / (double) count) + "," + ((double) basepairs[i][8] / (double) count) + ","
                        + ((double) basepairs[i][9] / (double) count) + "," + ((double) basepairs[i][10] / (double) count));
            }


            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "position_property_plot.csv")), true));

            System.out.println("Position, Mismatches, G:U pairs, Gaps, Type, Count");
            for (int i = 0; i < mm.length; i++) {
                System.out.println((i + 1) + "," + ((double) mm[i] / (double) count) + "," + ((double) wobble[i] / (double) count) + "," + ((double) gaps[i] / (double) count) +","+ speciesName +"," + count);
            }

            //Do something with number of MMs, GUs, etc
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "mismatch_plot.csv")), true));

            System.out.println("Number of mismatches, Cumulative interactions");
            int runningCount = 0;
            int total = 0;

            for (int i = 0; i < totalNumMMs.length; i++) {
                total += totalNumMMs[i];
            }

            for (int i = 0; i < totalNumMMs.length; i++) {

                int cumulative = runningCount + totalNumMMs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumMMs[i];
            }

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "gu_pair_plot.csv")), true));

            System.out.println("Number of GU pairs, Cumulative interactions");
            runningCount = 0;
            total = 0;
            for (int i = 0; i < totalNumGUs.length; i++) {
                total += totalNumGUs[i];
            }

            for (int i = 0; i < totalNumGUs.length; i++) {

                int cumulative = runningCount + totalNumGUs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumGUs[i];
            }
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "adj_mismatches_plot.csv")), true));

            System.out.println("Number of adjacent mismatches, Cumulative interactions");
            runningCount = 0;
            total = 0;
            for (int i = 0; i < totalNumAdjMMs.length; i++) {
                total += totalNumAdjMMs[i];
            }
            for (int i = 0; i < totalNumAdjMMs.length; i++) {
                int cumulative = runningCount + totalNumAdjMMs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumAdjMMs[i];
            }

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "core_adj_mismatches_plot.csv")), true));

            System.out.println("Number of adjacent mismatches in core region, Cumulative interactions");
            runningCount = 0;
            total = 0;
            for (int i = 0; i < totalNumCoreAdjMMs.length; i++) {
                total += totalNumCoreAdjMMs[i];
            }
            for (int i = 0; i < totalNumCoreAdjMMs.length; i++) {
                int cumulative = runningCount + totalNumCoreAdjMMs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumCoreAdjMMs[i];
            }

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "core_mismatches_plot.csv")), true));

            System.out.println("Number of mismatches in core region, Cumulative interactions");
            runningCount = 0;
            total = 0;
            for (int i = 0; i < totalNumCoreMMs.length; i++) {
                total += totalNumCoreMMs[i];
            }
            for (int i = 0; i < totalNumCoreMMs.length; i++) {
                int cumulative = runningCount + totalNumCoreMMs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumCoreMMs[i];
            }

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "core_GU_pairs_plot.csv")), true));

            System.out.println("Number of G:U pairs in core region, Cumulative interactions");
            runningCount = 0;
            total = 0;
            for (int i = 0; i < totalNumCoreGUs.length; i++) {
                total += totalNumCoreGUs[i];
            }
            for (int i = 0; i < totalNumCoreGUs.length; i++) {
                int cumulative = runningCount + totalNumCoreGUs[i];
                System.out.println(i + "," + cumulative);
                if (cumulative == total) {
                    break;
                }
                runningCount += totalNumCoreGUs[i];
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void produceAlignmentScorePlots() {

        try {

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "alignment_score_plot.csv")), true));

            ArrayList<Double> list = new ArrayList();

            for (String line : uniquePositions.values()) {
                String splits[] = line.split(",");
                double score = Double.parseDouble(splits[11]);
                list.add(score);
            }

            Collections.sort(list);

            System.out.println("Score, Cumulative Interactions");

            int count = 0;
            int cumulativeCount = 0;

            //populate the first value and increment the count
            double current = list.get(0);
            count++;

        
            for (int i = 1; i < list.size(); i++) {

                if (current == list.get(i)) {
                    count++;
                } else {

                    cumulativeCount += count;
                    System.out.println(current + "," + cumulativeCount);
                    current = list.get(i);
                    count = 1;

                }
                
               
            }
            cumulativeCount += count;
            System.out.println(current + "," + cumulativeCount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void produceMFERatioPlots() {

        try {

            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outputDirectory + File.separator + "mfe_ratio_plot.csv")), true));
            ArrayList<Double> list = new ArrayList();

            for (String line : uniquePositions.values()) {
                String splits[] = line.split(",");
                double score = Double.parseDouble(splits[14]);
                list.add(score);
            }

            Collections.sort(list);

            System.out.println(speciesName);

            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isComplementary(char a, char b) {
        if (a == 'G' && b == 'C') {
            return true;
        }

        if (a == 'C' && b == 'G') {
            return true;
        }

        if (a == 'A' && b == 'T') {
            return true;
        }

        if (a == 'T' && b == 'A') {
            return true;
        }

        return false;
    }

    private HashMap<Integer, File> splitResultsOnLength() {

        lengthSplit = new HashMap();
        HashMap<Integer, List<String>> results = new HashMap();

        try {
            BufferedReader br = new BufferedReader(new FileReader(PAREsnip2Results));
            String headerLine = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                line += "\n" + br.readLine() + "\n";
                line += br.readLine();

                String splits[] = line.split(",");
                String alignment_splits[] = splits[10].split("'");
                String smallRNA = alignment_splits[1].trim().replace("-", "");

                if (!results.containsKey(smallRNA.length())) {
                    results.put(smallRNA.length(), new ArrayList());
                    File out = new File(outputDirectory.getAbsolutePath() + File.separator + "length_specific" + File.separator + smallRNA.length() + File.separator + "interaction_alignments.csv");
                    out.getParentFile().mkdirs();
                    lengthSplit.put(smallRNA.length(), out);
                }

                results.get(smallRNA.length()).add(line);
            }

            for (Integer key : lengthSplit.keySet()) {
                File out = lengthSplit.get(key);

                BufferedWriter bw = new BufferedWriter(new FileWriter(out));
                bw.write(headerLine + "\n");

                for (String s : results.get(key)) {
                    bw.write(s + "\n");
                }

                bw.flush();
                bw.close();

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lengthSplit;

    }

    private boolean isWobble(char a, char b) {
        if (a == 'G' && b == 'T') {
            return true;
        }

        return a == 'T' && b == 'G';
    }

    private boolean isGap(char a, char b) {
        return (a == '-' || b == '-');
    }

    private int basepairPos(char a, char b) {

        if (b == '-') {
            if (a == 'A' && b == '-') {
                return 7;
            } else if (a == 'T' && b == '-') {
                return 8;
            } else if (a == 'G' && b == '-') {
                return 9;
            } else if (a == 'C' && b == '-') {
                return 10;
            }
        } else {
            if (a == 'A' && b == 'T' || a == 'T' && b == 'A') {
                return 0;
            } else if (a == 'G' && b == 'C' || a == 'C' && b == 'G') {
                return 1;
            } else if (a == 'G' && b == 'T' || a == 'T' && b == 'G') {
                return 2;
            } else if (a == 'A' && b != 'T') {
                return 3;
            } else if (a == 'T' && b != 'A') {
                return 4;
            } else if (a == 'G' && b != 'C') {
                return 5;
            } else if (a == 'C' && b != 'G') {
                return 6;
            }
        }

        return -1;
    }
}
