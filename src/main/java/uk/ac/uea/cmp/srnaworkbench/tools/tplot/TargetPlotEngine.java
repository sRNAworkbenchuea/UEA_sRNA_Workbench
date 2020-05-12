/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tplot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.BinarySearchAligner;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.ShortRead;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.ShortReadAlignment;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class TargetPlotEngine {

    File PAREsnip2Results, outputDir, degradomeFile, transcriptFile,
            alignmentFile, resultsTranscripts, plotData, scriptDir, degradomeNR;
    private HashMap<String, String> transcriptSeqs = new HashMap();
    private TargetPlotConfiguration config = TargetPlotConfiguration.getInstance();
    String outputFileName;

  
    public TargetPlotEngine(File degradomeFile, File transcriptFile) {

        this.outputDir = new File("t_plot_tmp");
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        scriptDir = new File("ExeFiles" + File.separator + "R-scripts");
        outputFileName = "T-plots";
        //scriptDir = new File(Tools.EXE_DIR + File.separator + "R-scripts");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public TargetPlotEngine(File PAREsnip2Results, File degradomeFile, File transcriptFile) {

        this.PAREsnip2Results = PAREsnip2Results;
        this.outputDir = new File("t_plot_tmp");
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        scriptDir = new File("ExeFiles" + File.separator + "R-scripts");
        outputFileName = "T-plots";
        //scriptDir = new File(Tools.EXE_DIR + File.separator + "R-scripts");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public TargetPlotEngine(File PAREsnip2Results, File degradomeFile, File transcriptFile, File outputDir) {
        this.PAREsnip2Results = PAREsnip2Results;
        this.outputDir = outputDir;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        scriptDir = new File("ExeFiles" + File.separator + "R-scripts");
        outputFileName = "T-plots";
        //scriptDir = new File(Tools.EXE_DIR + File.separator + "R-scripts");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public TargetPlotEngine(File PAREsnip2Results, File degradomeFile, File transcriptFile, File outputDir, String outputFileName) {
        this.PAREsnip2Results = PAREsnip2Results;
        this.outputDir = outputDir;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        scriptDir = new File("ExeFiles" + File.separator + "R-scripts");
        this.outputFileName = outputFileName;
        //scriptDir = new File(Tools.EXE_DIR + File.separator + "R-scripts");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public TargetPlotEngine(File PAREsnip2Results, File degradomeFile, File transcriptFile, File outputDir, File degradomeNR, String outputFileName) {
        this.PAREsnip2Results = PAREsnip2Results;
        this.outputDir = outputDir;
        this.degradomeFile = degradomeFile;
        this.transcriptFile = transcriptFile;
        scriptDir = new File("ExeFiles" + File.separator + "R-scripts");
        this.degradomeNR = degradomeNR;
        this.outputFileName = outputFileName;
        //scriptDir = new File(Tools.EXE_DIR + File.separator + "R-scripts");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

    }

    public void process() {
        populateTranscriptSequences(transcriptFile);
        if (PAREsnip2Results != null) {
            writePAREsnip2ResultsTranscripts();
        } else {
            resultsTranscripts = transcriptFile;
        }
        alignSeqs();
        readAlignments();
        copyFiles();

        runScripts(findExecutableOnPath("Rscript"), "--vanilla", "T-plot_script.R");

    }

    private void runScripts(String... commands) {
        try {
            String[] command = commands;
            ProcessBuilder builder = new ProcessBuilder(command);
            builder = builder.directory(new File("ExeFiles" + File.separator + "R-scripts"));
            Process p = builder.start();
            p.waitFor();
            Path temp = Files.move(Paths.get("ExeFiles" + File.separator + "R-scripts" + File.separator + "T-plots.pdf"),
                    Paths.get(outputDir.getAbsolutePath() + File.separator + outputFileName + ".pdf"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void populateTranscriptSequences(File transcripts) {
        /**
         * Here, we populate all the transcript sequences for later use.
         *
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
                        gene = gene.substring(1);
                        transcriptSeqs.put(gene, sb.toString());
                    }
                    gene = line;
                    sb = new StringBuilder();
                } else {
                    sb.append(line);
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            //add it to map
            gene = gene.substring(1);
            transcriptSeqs.put(gene, sb.toString());
        }
    }

    private void alignSeqs() {

        //We should include the PATMAN alignment here too
        if (degradomeNR == null) {
            degradomeNR = makeNonRedundant(degradomeFile, outputDir, config.getMinTagLength(), config.getMaxTagLength());
        }

        BinarySearchAligner bsa = new BinarySearchAligner(degradomeNR, resultsTranscripts, config.getMinTagLength(),
                config.getMaxTagLength(), outputDir, config.getNumThreads(), true);

        bsa.run();

        alignmentFile = bsa.getOutputFile();

    }

    private File makeNonRedundant(File seqs, File outputDirectory, int min, int max) {
        HashMap<String, Integer> smallRNAs = new HashMap();
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
            br.close();
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

    private void writePAREsnip2ResultsTranscripts() {
        resultsTranscripts = new File(outputDir.getAbsolutePath() + File.separator + "transcripts.fa");

        HashSet<String> transcriptsToWrite = new HashSet();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(resultsTranscripts));

            BufferedReader br = new BufferedReader(new FileReader(PAREsnip2Results));

            //read header file
            String splits[] = br.readLine().split(",");
            int len = splits.length;
            String line;
            while ((line = br.readLine()) != null) {
                line += br.readLine();
                line += br.readLine();

                String geneName = "";

                splits = line.split(",");

                if (splits.length <= len) {
                    geneName = splits[2].replace("\"", "");
                    //geneName = geneName.replace(";", ",");
                } else {

                    //If there exists a comma in the gene annotaton
                    //we need to concatenate these splits together
                    geneName = "";

                    int missing = splits.length - len;

                    for (int i = 0; i <= missing; i++) {
                        geneName += splits[i + 2] + ",";
                    }
                    geneName = geneName.replace("\"", "");
                    //remove trailing comma
                    geneName = geneName.substring(0, geneName.length() - 1);

                }

                transcriptsToWrite.add(geneName);
            }
            br.close();
            for (String transcript : transcriptsToWrite) {
                try {
                    bw.write(">" + transcript);
                    bw.write("\n");
                    bw.write(transcriptSeqs.get(transcript));
                    bw.write("\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            bw.close();
            resultsTranscripts.deleteOnExit();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readAlignments() {

        HashMap<String, List<ShortReadAlignment>> processedReads = new HashMap();

        //read through line by line and group alignments per gene
        //abundance at each position per gene is recorded
        //adjusted weighted abundance is calculated by adding the weighted abundance
        //of each tag that aligns to a particular position
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
                    prev.getTag().setAdjustedWeightedAbundance(curWeightedAbundance + prev.getTag().getAdjustedWeightedAbundance());
                } else {

                    //add the peak to the processed reads
                    processedReads.get(splits[0]).add(prev);

                    //reset at the new position
                    prev = new ShortReadAlignment(new ShortRead(seq, abundance), pos);
                    prev.getTag().setNumAlignmentsPositive(numAlignments.get(seq));
                    prev.getTag().calculateWeightedPositiveAbundance();

                }

            }
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
                    processedReads.get(prevGene).add(prev);

                }
            }
        }

        //Should this be starting from 1?
        try {
            plotData = new File(outputDir.getAbsolutePath() + File.separator + "plot_data.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(plotData));
            for (String gene : processedReads.keySet()) {
                List<ShortReadAlignment> list = processedReads.get(gene);

                for (ShortReadAlignment sra : list) {
                    bw.write("\"" + gene + "\"," + sra.getGenePosition() + "," + sra.getTag().getAbundance() + "\n");
                }
            }
            bw.close();
            plotData.deleteOnExit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void copyFiles() {
        //Move files to the script directory
        try {
            File f = new File(scriptDir.getAbsolutePath() + File.separator + "plot_data.csv");
            if (f.exists()) {
                f.delete();
            }
            Files.copy(plotData.toPath(), f.toPath());

            if (PAREsnip2Results != null) {
                f = new File(scriptDir.getAbsolutePath() + File.separator + "results.csv");
                if (f.exists()) {
                    f.delete();
                }
                Files.copy(PAREsnip2Results.toPath(), f.toPath());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String findExecutableOnPath(String name) {

        //we need to do a windows/mac/linux check here
        if (AppUtils.INSTANCE.getArchitecture().isWindows()) {
            name = name + ".exe";
        }

        for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
            File file = new File(dirname, name);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

}
