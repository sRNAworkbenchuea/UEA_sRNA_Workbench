package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

/**
 *
 * @author Josh
 */
public class DegradomeTagAligner extends BinarySearchAligner {

    HashMap<String, String> longReadSeqs;
    HashMap<String, int[]> abundancePositions;
    int[] categoryCounts;
    int transcriptomeSize;
    int effectiveTranscriptomeSize;
    int maxLength;
    HashMap<String, List<ShortReadAlignment>> processedReads;

    public DegradomeTagAligner(File shortReadsFile, File longReadsFile, File outputDir) {
        super(shortReadsFile, longReadsFile, Paresnip2Configuration.getInstance().getMinFragmentLenth(), Paresnip2Configuration.getInstance().getMaxFragmentLenth(),
                outputDir, Paresnip2Configuration.getInstance().getNumberOfThreads());
        maxLength = Paresnip2Configuration.getInstance().getMaxFragmentLenth();
        categoryCounts = new int[5];
    }

    public DegradomeTagAligner(File shortReadsFile, File longReadsFile, int min, int max, File outputDir, int numThreads) {
        super(shortReadsFile, longReadsFile, min, max, outputDir, numThreads);
        maxLength = max;
        categoryCounts = new int[5];
    }

    @Override
    protected void process() throws Exception {
        Engine.updateProgressText("Aligning degradome reads to reference...");
        if (maxLength <= 30) {
            //We can use the BinarySearchAligner....
            super.process();
        } else {
            //We have to use another alignment algorithm due to max length of seqs
            //Might need to process these in different ways based on the alignment method
            //E.g. sequences >32nt in length may result in strange results
            mapReads(shortReadFile, longReadFile);
            calculateTotalReads();
        }

        processedReads = new HashMap();
        longReadSeqs = new HashMap();
        Engine.updateProgressText("Building categories...");
        populateLongReadSequences();

        readAlignments();
        determineCategories();
        longReadSeqs.clear();
        alignedReads.clear();
        processedReads.clear();
        MessengerRNA.shrinkToFit();
    }

    private void determineCategories() {

        abundancePositions = new HashMap();

        for (String gene : processedReads.keySet()) {
            List<ShortReadAlignment> list = new ArrayList(processedReads.get(gene));

            if (list.size() >= 0) {
                //sort on read abundance
                Collections.sort(list);

                double average = 0.0;

                boolean singleHighestAbundance;

                int count = 0;
                //Average without including those with abundance of 1
                for (ShortReadAlignment a : list) {
                    if (a.getTag().getAbundance() != 1) {
                        if (!Paresnip2Configuration.getInstance().isUseWeightedFragmentAbundance()) {
                            average += a.getTag().getAbundance();
                        } else {
                            average += a.getTag().getAdjustedWeightedAbundance();
                        }
                        count++;
                    }
                }
                average /= (double) count;

                if (list.size() == 1) {
                    singleHighestAbundance = true;
                } else if (!Paresnip2Configuration.getInstance().isUseWeightedFragmentAbundance()) {
                    singleHighestAbundance = list.get(list.size() - 1).getTag().getAbundance()
                            > list.get(list.size() - 2).getTag().getAbundance();
                } else {
                    singleHighestAbundance = list.get(list.size() - 1).getTag().getWeightedPositiveAbundance()
                            > list.get(list.size() - 2).getTag().getAdjustedWeightedAbundance();
                }

                double largestAbundance;

                if (!Paresnip2Configuration.getInstance().isUseWeightedFragmentAbundance()) {
                    largestAbundance = list.get(list.size() - 1).getTag().getAbundance();
                } else {
                    largestAbundance = list.get(list.size() - 1).getTag().getAdjustedWeightedAbundance();
                }

                String geneSeq = longReadSeqs.get(gene);

                abundancePositions.put(gene, new int[geneSeq.length()]);

                for (ShortReadAlignment sra : list) {
                    double abundance;

                    if (!Paresnip2Configuration.getInstance().isUseWeightedFragmentAbundance()) {
                        abundance = sra.getTag().getAbundance();
                    } else {
                        abundance = sra.getTag().getAdjustedWeightedAbundance();
                    }

                    if (abundance == largestAbundance && sra.getTag().getAbundance() != 1 && singleHighestAbundance) {
                        add(gene, geneSeq, 0, sra);
                    } else if (abundance == largestAbundance && sra.getTag().getAbundance() != 1) {
                        add(gene, geneSeq, 1, sra);
                    } else if (abundance > average && abundance < largestAbundance && sra.getTag().getAbundance() != 1) {
                        add(gene, geneSeq, 2, sra);
                    } else if (abundance <= average && sra.getTag().getAbundance() != 1) {
                        add(gene, geneSeq, 3, sra);
                    } else if (Paresnip2Configuration.getInstance().getAllowedCategories(4)) {
                        add(gene, geneSeq, 4, sra);
                    }
                }
                if (!list.isEmpty()) {
                    Engine.calculateCategoryPerTranscript(gene, geneSeq.length());
                }
            }
        }

    }

    private void add(String geneName, String geneSeq, int category, ShortReadAlignment sra) {

        MessengerRNA fragment = null;

        int transcriptCleavagePos = sra.getGenePosition();
        String fragString;
        try {

            if (transcriptCleavagePos - 17 >= 0) {
                fragString = geneSeq.substring(transcriptCleavagePos - 17, transcriptCleavagePos + 15);
                fragment = new MessengerRNA(fragString, geneName);
                fragment.setTranscriptPosition(transcriptCleavagePos);
                fragment.setCategory(category);
                fragment.abundance = sra.getTag().getAbundance();
                fragment.weightedAbundance = sra.getTag().getAdjustedWeightedAbundance();
                fragment.setNormalisedAbundance(((double) sra.getTag().getAbundance() / (double)totalReads) * 1000000);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (fragment != null) {
            abundancePositions.get(geneName)[fragment.transcriptPosition - 1] = fragment.abundance;
            //categoryCounts[category]++;
            Engine.addCategory(geneName, category);
        }

    }

    private void populateLongReadSequences() {
        FileReader input;
        StringBuilder sb = new StringBuilder();
        String gene = null;

        try {

            input = new FileReader(longReadFile);

            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while ((myLine = bufRead.readLine()) != null) {

                if (myLine.startsWith(">")) {
                    if (gene != null) {
                        String longRead = sb.toString();
                        longReadSeqs.put(gene, longRead);
                        //transcriptomeSize += longRead.length();
                        //Engine.populateTranscriptLengths(gene, longRead.length());
                        sb = new StringBuilder();
                    }

                    //gene = myLine.split(" ")[0].split(">")[1];
                    gene = myLine.substring(1, myLine.length());
                    gene = gene.replace("\n", "").replace("\r", "");

                    //replace any , with a ;
                    //gene = gene.replace(',', ';');
                } else {
                    sb.append(myLine.trim());
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {

            String longRead = sb.toString();
            gene = gene.replace("\n", "").replace("\r", "");
            longReadSeqs.put(gene, longRead);
            //Engine.populateTranscriptLengths(gene, longRead.length());
            //transcriptomeSize += longRead.length();

        }
    }

    public void readAlignments() {

        //read through line by line and group alignments per gene
        //abundance at each position per gene is recorded
        //adjusted weighted abundance is calculated by adding the weighted abundance
        //of each tag that aligns to a particular position
        HashMap<String, Integer> numAlignments = new HashMap();
        //Count the number of alignments for each sequence
        try {
            BufferedReader br = new BufferedReader(new FileReader(outFile));

            String line = null;

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                try {
                    String[] splits = line.split("\t");
                    String seqInfo[] = splits[1].split("\\(");
                    String seq = seqInfo[0];
                    seq = seq.trim();

                    if (numAlignments.containsKey(seq)) {
                        numAlignments.put(seq, numAlignments.get(seq) + 1);
                    } else {
                        numAlignments.put(seq, 1);
                    }
                } catch (Exception ex) {
                    System.out.println(line);
                    ex.printStackTrace();
                }

            }

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
            BufferedReader br = new BufferedReader(new FileReader(outFile));

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                String[] splits = line.split("\t");
                String seqInfo[] = splits[1].split("\\(");
                seq = seqInfo[0];
                seq = seq.trim();
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

                } else {//Check if there is another sequence that also aligns to this position

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

            }

            //outFile.deleteOnExit();
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

        Iterator<Entry<String, List<ShortReadAlignment>>> it = processedReads.entrySet().iterator();

        while (it.hasNext()) {
            if (it.next().getValue().isEmpty()) {
                it.remove();
            }
        }

    }

    private void mapReads(File degradome, File transcriptome) {
        File tmpDir = new File("tmp");
        File patman_out = new File(tmpDir.getAbsolutePath() + File.separator + "alignment.patman");

        PatmanParams newP_Params = new PatmanParams();
        newP_Params.setMaxGaps(0);
        newP_Params.setMaxMismatches(0);
        newP_Params.setPreProcess(false);
        newP_Params.setPostProcess(false);
        newP_Params.setMakeNR(false);
        newP_Params.setPositiveStrandOnly(true);

        try {
            tmpDir.mkdir();
            PatmanRunner runner = new PatmanRunner(degradome, transcriptome,
                    patman_out, tmpDir, newP_Params);
            runner.setUsingDatabase(false);

            Thread myThread = new Thread(runner);

            myThread.start();
            myThread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpDir.deleteOnExit();

        try {
            HashMap<String, ArrayList<PatmanAlignment>> geneAlignments = new HashMap();
            //We need to order the patman output here...

            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            BufferedReader br = new BufferedReader(new FileReader(patman_out));
            String line;

            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");

                if (!geneAlignments.containsKey(splits[0])) {
                    geneAlignments.put(splits[0], new ArrayList());
                }
                geneAlignments.get(splits[0]).add(new PatmanAlignment(line));
            }

            for (String gene : geneAlignments.keySet()) {
                Collections.sort(geneAlignments.get(gene));

                for(PatmanAlignment pma : geneAlignments.get(gene))
                {
                    bw.write(pma.line + "\n");
                }
                bw.flush();
                    
            }
            br.close();
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void calculateTotalReads() {
        try {
            FileReader input = new FileReader(shortReadFile);
            ShortRead prev = null;

            BufferedReader bufRead = new BufferedReader(input, 4096);
            String seq;
            int abundance = 0;
            //Need to find a better way to do this! HASH MAPS ARE NOT THE WAY!!!
            while ((seq = bufRead.readLine()) != null) {
                if (seq.startsWith(">")) {

                    abundance = Integer.parseInt(seq.split("\\(")[1].split("\\)")[0]);
                    totalReads += abundance;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    private class PatmanAlignment implements Comparable<PatmanAlignment> {

        String line;
        String gene, seq, strand;
        int start, end, gaps;

        public PatmanAlignment(String line) {
            this.line = line;

            String splits[] = line.split("\t");
            
            gene = splits[0].split(" ")[0];
            seq = splits[1];
            start = Integer.parseInt(splits[2]);
            end = Integer.parseInt(splits[3]);
                    

        }

        @Override
        public int compareTo(PatmanAlignment o) {

            if (gene.equals(o.gene)) {
                if (start != o.start) {
                    return Integer.compare(start, o.start);
                } else {
                    return Integer.compare(seq.split(" ")[0].length(), o.seq.split(" ")[0].length());
                }
            }

            return 1;

        }

    }

}
