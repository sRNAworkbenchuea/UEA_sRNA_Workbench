/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;

/**
 *
 * @author rew13hpu
 */
public class CDSExtractor {

    File genome, gff;
    HashMap<String, String> geneSequencesCDS;
    HashMap<String, String> geneSequencesExon;
    HashMap<String, ArrayList<Record>> geneRecordsCDS;
    HashMap<String, ArrayList<Record>> geneRecordsExon;
    HashMap<String, HashSet<String>> chromosomeGenes;
    HashMap<String, String> geneNames;
    HashMap<String, String> chromosomeSequences;
    boolean includeUTR;
    boolean includeNonCodingRNA;

    public CDSExtractor(File genome, File gff) {
        this.genome = genome;
        this.gff = gff;
        geneSequencesCDS = new HashMap();
        geneSequencesExon = new HashMap();
        geneRecordsCDS = new HashMap();
        geneRecordsExon = new HashMap();
        chromosomeGenes = new HashMap();
        geneNames = new HashMap();
        chromosomeSequences = new HashMap();
        includeUTR = false;
    }

    public CDSExtractor(File genome, File gff, boolean includeUTR) {
        this(genome, gff);
        this.includeUTR = includeUTR;
    }

    public File createTranscriptome(File newTranscriptomeFile) {

        getGeneSequences();

        //Takes gene version with the longest sequence
//        HashMap<String, Integer> geneVersions = new HashMap();
//
//        for (String s : geneSequencesCDS.keySet()) {
//
//            String[] splits = s.split("\\.");
//            String gName = splits[0];
//            int versionNum = Integer.parseInt(splits[1]);
//            int length = geneSequencesCDS.get(s).length();
//
//            if (!geneVersions.containsKey(gName)) {
//                geneVersions.put(gName, versionNum);
//            } else if (!includeUTR) {
//                if (length == geneSequencesCDS.get(gName + "." + geneVersions.get(gName)).length()) {
//                    if (versionNum < geneVersions.get(gName)) {
//                        geneVersions.put(gName, versionNum);
//                    }
//                } else if (length > geneSequencesCDS.get(gName + "." + geneVersions.get(gName)).length()) {
//                    geneVersions.put(gName, versionNum);
//                }
//            } else if (length == geneSequencesExon.get(gName + "." + geneVersions.get(gName)).length()) {
//                if (versionNum < geneVersions.get(gName)) {
//                    geneVersions.put(gName, versionNum);
//                }
//            } else if (length > geneSequencesExon.get(gName + "." + geneVersions.get(gName)).length()) {
//                geneVersions.put(gName, versionNum);
//            }
//        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(newTranscriptomeFile));

            if (!includeUTR) {
                for (String s : geneSequencesCDS.keySet()) {
                    writer.write(">" + s);
                    writer.newLine();
                    writer.write(geneSequencesCDS.get(s));
                    writer.newLine();
                    writer.flush();
                }
            } else {
                for (String s : geneSequencesExon.keySet()) {
                    writer.write(">" + s);
                    writer.newLine();
                    writer.write(geneSequencesExon.get(s));
                    writer.newLine();
                    writer.flush();
                }
            }

//            for (String s : geneVersions.keySet()) {
//                writer.write(">" + s + "." + geneVersions.get(s));
//                writer.newLine();
//                if (!includeUTR) {
//                    writer.write(geneSequencesCDS.get(s + "." + geneVersions.get(s)));
//                } else {
//                    writer.write(geneSequencesExon.get(s + "." + geneVersions.get(s)));
//                }
//                writer.newLine();
//                writer.flush();
//            }
        } catch (Exception ex) {
ex.printStackTrace();
        }

        return newTranscriptomeFile;
    }

    public HashMap<String, String> getGeneSequences() {

        try {
            BufferedReader br = new BufferedReader(new FileReader(gff));

//            if (!br.readLine().equals("##gff-version 3")) {
//                throw new Exception("Input file is not a valid GFF3 file!");
//            }
            String line;
            String currentChromosome = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {

                    String splits[] = line.split("\t");

                    Record record = new Record(splits[0], splits[1], splits[2], Integer.parseInt(splits[3]), Integer.parseInt(splits[4]), splits[5], splits[6], splits[7], splits[8]);

                    if (record.getType().equals("chromosome")) {
                        currentChromosome = record.getAttribute("ID");
                        chromosomeGenes.put(currentChromosome, new HashSet());
                    } else if (record.getType().equals("gene")) {
                        geneNames.put(record.getAttribute("ID"), record.getAttribute("Name"));
                    } else if (record.getType().equals("mRNA")) {
                        geneRecordsCDS.put(record.getAttribute("ID"), new ArrayList());
                        geneRecordsExon.put(record.getAttribute("ID"), new ArrayList());
                        chromosomeGenes.get(currentChromosome).add(record.getAttribute("ID"));
                    } else if (record.getType().equals("ncRNA")) {
                        if (includeNonCodingRNA) {
                            geneRecordsExon.put(record.getAttribute("ID"), new ArrayList());
                            chromosomeGenes.get(currentChromosome).add(record.getAttribute("ID"));
                        }
                    } else if (record.getType().equals("CDS")) {

                        String parent = record.getAttribute("Parent");
                        if (geneRecordsCDS.containsKey(parent)) {
                            geneRecordsCDS.get(parent).add(record);
                        }

                    } else if (record.getType().equals("exon")) {

                        if (includeUTR) {
                            String parent = record.getAttribute("Parent");
                            if (geneRecordsExon.containsKey(parent)) {
                                geneRecordsExon.get(parent).add(record);
                            }
                        }

                    }
                }
            }

            //Sort records
            for (String s : geneRecordsCDS.keySet()) {
                Collections.sort(geneRecordsCDS.get(s));
            }

            for (String s : geneRecordsExon.keySet()) {
                Collections.sort(geneRecordsExon.get(s));
            }

            loadChromosomes();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return geneSequencesCDS;

    }

    public static void main(String args[]) {
        File gff = new File("gff_test/TAIR10_GFF3_genes.gff");
        File genome = new File("gff_test/genome.fa");

        CDSExtractor extractor = new CDSExtractor(genome, gff, true);
        extractor.createTranscriptome(new File("gff_test.fa"));

        String line;
        String geneName = null;
        HashMap<String, HashMap<String, String>> sequences = new HashMap();
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("gff_test.fa")));

            while ((line = br.readLine()) != null) {

                if (line.startsWith(">")) {
                    if (geneName != null) {
                        String trimmedGeneName = geneName.split("\\.")[0];

                        if (!sequences.containsKey(trimmedGeneName)) {
                            sequences.put(trimmedGeneName, new HashMap());
                        }

                        sequences.get(trimmedGeneName).put(geneName, sb.toString());

                    }
                    geneName = line.split(" ")[0].substring(1);
                    sb = new StringBuilder();
                } else {
                    sb.append(line);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            String trimmedGeneName = geneName.split("\\.")[0];

            if (!sequences.containsKey(trimmedGeneName)) {
                sequences.put(trimmedGeneName, new HashMap());
            }

            sequences.get(trimmedGeneName).put(geneName, sb.toString());
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("longest_transcripts.fa")));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadChromosomes() {

        String line;
        String chromosomeName = null;
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(genome));
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (chromosomeName == null) {
                        chromosomeName = line.split(">")[1];
                    } else {

                        processChromosome(chromosomeName, sb.toString());

                        chromosomeName = line.split(">")[1];
                        sb = new StringBuilder();
                    }
                } else {
                    sb.append(line);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            processChromosome(chromosomeName, sb.toString());
        }

    }

    private void processChromosome(String chromosomeName, String chromosomeSeq) {

        if (!includeUTR) {
            if (chromosomeGenes.containsKey(chromosomeName)) {
                for (String gene : chromosomeGenes.get(chromosomeName)) {

                    boolean positive = geneRecordsCDS.get(gene).get(0).getStrand().equals("+");
                    ArrayList<String> seqRegions = new ArrayList();

                    for (int i = 0; i < geneRecordsCDS.get(gene).size(); i++) {
                        Record r = geneRecordsCDS.get(gene).get(i);
                        int start = r.getStartPos();
                        int end = r.getEndPos();
                        String region;

                        if (positive) {
                            region = chromosomeSeq.substring(start - 1, end);
                        } else {
                            region = SequenceUtils.DNA.reverseComplement(chromosomeSeq.substring(start - 1, end));
                        }
                        seqRegions.add(region);
                    }

                    StringBuilder sb = new StringBuilder();
                    if (positive) {

                        for (int i = 0; i < seqRegions.size(); i++) {
                            sb.append(seqRegions.get(i));
                        }
                    } else {
                        for (int i = 0; i < seqRegions.size(); i++) {
                            sb.append(seqRegions.get(i));
                        }
                    }

                    String geneSeq = sb.toString();

                    geneSequencesCDS.put(gene, geneSeq);

                }
            }
        }

        if (includeUTR) {
            if (chromosomeGenes.containsKey(chromosomeName)) {
                for (String gene : chromosomeGenes.get(chromosomeName)) {

                    boolean positive = geneRecordsExon.get(gene).get(0).getStrand().equals("+");
                    ArrayList<String> seqRegions = new ArrayList();

                    for (int i = 0; i < geneRecordsExon.get(gene).size(); i++) {
                        Record r = geneRecordsExon.get(gene).get(i);
                        int start = r.getStartPos();
                        int end = r.getEndPos();
                        String region;

                        if (positive) {
                            region = chromosomeSeq.substring(start - 1, end);
                        } else {
                            region = SequenceUtils.DNA.reverseComplement(chromosomeSeq.substring(start - 1, end));
                        }
                        seqRegions.add(region);
                    }

                    StringBuilder sb = new StringBuilder();
                    if (positive) {

                        for (int i = 0; i < seqRegions.size(); i++) {
                            sb.append(seqRegions.get(i));
                        }
                    } else {
                        for (int i = seqRegions.size() - 1; i >= 0; i--) {
                            sb.append(seqRegions.get(i));
                        }
                    }

                    String geneSeq = sb.toString();

                    geneSequencesExon.put(gene, geneSeq);

                }
            }
        }

    }

    private class Record implements Comparable<Record> {

        String id;
        String source;
        String type;
        int startPos;
        int endPos;
        String score;
        String strand;
        String phase;
        String attributeString;
        HashMap<String, String> attributes;

        public Record(String id, String source, String type, int startPos, int endPos, String score, String strand, String phase, String attributeString) {
            this.id = id;
            this.source = source;
            this.type = type;
            this.startPos = startPos;
            this.endPos = endPos;
            this.score = score;
            this.strand = strand;
            this.phase = phase;
            this.attributeString = attributeString;
            attributes = new HashMap();
            processAtributes();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }

        public int getEndPos() {
            return endPos;
        }

        public void setEndPos(int endPos) {
            this.endPos = endPos;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getStrand() {
            return strand;
        }

        public void setStrand(String strand) {
            this.strand = strand;
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public String getAttribute(String attribute) {

            return attributes.get(attribute);
        }

        public void setAttributeString(String attributes) {
            this.attributeString = attributes;
        }

        private void processAtributes() {

            String splits[] = attributeString.split(";");
            for (String s : splits) {
                String[] attributePair = s.split("=");
                String commaSplit = attributePair[1].split(",")[0];
                attributes.put(attributePair[0], commaSplit);
            }

        }

        @Override
        public int compareTo(Record o) {

            int compareStart = Integer.compare(startPos, o.getStartPos());

            if (compareStart != 0) {
                return compareStart;
            } else {
                return Integer.compare(endPos, o.getEndPos());
            }

        }
    }

}
