/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author rew13hpu
 */
public class NATpipe_parser {

    File smallRNAFile, alignmentFile, clusterHC, cluster100nt, cluster10k;

    HashMap<String, HashMap<String, HashSet<String>>> NATpipe_NAT_siRNAs;

    HashMap<String, HashMap<Integer, List<String>>> smallRNA_alignments;

    HashMap<String, String> smallRNAs;

    public NATpipe_parser(File smallRNAFile, File alignmentFile, File clusterHC, File cluster100nt, File cluster10k) {
        this.smallRNAFile = smallRNAFile;
        this.alignmentFile = alignmentFile;
        this.clusterHC = clusterHC;
        this.cluster100nt = cluster100nt;
        this.cluster10k = cluster10k;
    }

    private void process() {
        populatSmallRNAs();
        processAlignments();
        populateNATs();

    }

    private void populateNATs() {
        NATpipe_NAT_siRNAs = new HashMap();

        processCluster(clusterHC);
        processCluster(cluster100nt);
        processCluster(cluster10k);

    }

    private void processCluster(File cluster) {
        String A_indicator = "q";
        String B_indicator = "r";
        try {
            BufferedReader br = new BufferedReader(new FileReader(cluster));
            String line;
            //read header line
            br.readLine();
            while ((line = br.readLine()) != null) {

                String splits[] = line.split("\t");

                String geneA = splits[1];
                String geneB = splits[2];

                String alignedGene;
                String otherGene;

                String geneAStartPhase = splits[6];

                String geneBStartPhase = splits[5];

                String siRNA_cluster = splits[8].split("\\(")[0].substring(1);

                int alignmentSite = Integer.parseInt(siRNA_cluster.split("_")[0]);

                int numPhases = Integer.parseInt(splits[7]);

                if (numPhases == 1) {
                    if (siRNA_cluster.contains(A_indicator)) {
                        //Cluster in gene A
                        alignedGene = geneA;
                        otherGene = geneB;
                    } else {
                        //Cluster in gene B
                        alignedGene = geneB;
                        otherGene = geneA;
                    }

                    if (!NATpipe_NAT_siRNAs.containsKey(alignedGene)) {
                        NATpipe_NAT_siRNAs.put(alignedGene, new HashMap());
                    }

                    if (!NATpipe_NAT_siRNAs.get(alignedGene).containsKey(otherGene)) {
                        NATpipe_NAT_siRNAs.get(alignedGene).put(otherGene, new HashSet());
                    }

                    List<String> reads = smallRNA_alignments.get(alignedGene).get(alignmentSite);

                    if (reads != null) {
                        for (String s : reads) {
                            NATpipe_NAT_siRNAs.get(alignedGene).get(otherGene).add(smallRNAs.get(s));
                        }
                    } else {
                        System.out.println("HMMM");
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void processAlignments() {
        smallRNA_alignments = new HashMap();
        try {
            BufferedReader br = new BufferedReader(new FileReader(alignmentFile));
            String line;
            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");

                String annotation = splits[0];
                String gene = splits[2];
                Integer position = Integer.parseInt(splits[3]) + 1;

                if (!smallRNA_alignments.containsKey(gene)) {
                    smallRNA_alignments.put(gene, new HashMap<>());
                }

                if (!smallRNA_alignments.get(gene).containsKey(position)) {
                    smallRNA_alignments.get(gene).put(position, new ArrayList());
                }

                if (!smallRNA_alignments.get(gene).get(position).contains(annotation)) {
                    smallRNA_alignments.get(gene).get(position).add(annotation);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Set<String> getAlignments(String geneA, String geneB) {
        if (!NATpipe_NAT_siRNAs.containsKey(geneA) || !NATpipe_NAT_siRNAs.get(geneA).containsKey(geneB)) {
            return new HashSet();
        }
        return NATpipe_NAT_siRNAs.get(geneA).get(geneB);
    }

    private void populatSmallRNAs() {
        smallRNAs = new HashMap();
        try {
            BufferedReader br = new BufferedReader(new FileReader(smallRNAFile));
            String line;
            String annotation = "";
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    //trim the > character
                    annotation = line.substring(1);
                } else {
                    smallRNAs.put(annotation, line);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File smallRNAs = new File("D:\\NATPipe\\paper_analysis\\soybean\\normalized_smallRNAs.fa");
        File alignmentFile = new File("D:\\NATPipe\\paper_analysis\\soybean\\S1.bwt");
        File clusterHC = new File("D:\\NATPipe\\paper_analysis\\soybean\\HC_S1.cluster");
        File cluster100nt = new File("D:\\NATPipe\\paper_analysis\\soybean\\100nt_S1.cluster");
        File cluster10k = new File("D:\\NATPipe\\paper_analysis\\soybean\\10k_S1.cluster");

        NATpipe_parser parser = new NATpipe_parser(smallRNAs, alignmentFile, clusterHC, cluster100nt, cluster10k);

        parser.process();

//        NATpipeVsNATpare compare = new NATpipeVsNATpare(parser, new File("D:\\NAT_analysis\\performance_benchmarking\\NATpare\\predicted_NATsiRNAs.csv"));
//        compare.process();
    }

    private static class NATpipeVsNATpare {

        NATpipe_parser NATpipeResults;
        File NATpareFile, toFind;
        HashMap<String, HashMap<String, HashSet<String>>> NATpareResults = new HashMap();

        public NATpipeVsNATpare(NATpipe_parser NATpipeResults, File NATpareFile) {
            this.NATpipeResults = NATpipeResults;
            this.NATpareFile = NATpareFile;
            toFind = new File("D:\\NAT_analysis\\performance_benchmarking\\toFind.csv");
        }

        public void process() {
            int totalCount = 0;
            int missedCount = 0;

            try {
                BufferedReader br = new BufferedReader(new FileReader(toFind));
                String line;
                //read header
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String splits[] = line.split(",");

                    String sequence = splits[1];
                    String originatingGene = splits[0];
                    String natGene = splits[2];
//                    int readAbundance = Integer.parseInt(splits[2]);
//                    int sizeOriginal = Integer.parseInt(splits[12]);
//                    int sizeNAT = Integer.parseInt(splits[15]);

                    if (!NATpipeResults.getAlignments(originatingGene, natGene).contains(sequence)) {
                        if (!NATpipeResults.getAlignments(natGene, originatingGene).contains(sequence)) {

                            missedCount++;

                        }
                    }
                    //   System.out.println("AHHH");

                    if (!NATpareResults.containsKey(originatingGene)) {
                        NATpareResults.put(originatingGene, new HashMap());
                    }

                    if (!NATpareResults.get(originatingGene).containsKey(natGene)) {
                        NATpareResults.get(originatingGene).put(natGene, new HashSet());
                    }
                    totalCount++;
                    NATpareResults.get(originatingGene).get(natGene).add(sequence);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println(
                    "Total to catch: " + totalCount);
            System.out.println(
                    "Missed: " + missedCount);

            int NATpipeTotal = 0;

            for (String originating : NATpipeResults.NATpipe_NAT_siRNAs.keySet()) {
                for (String nat : NATpipeResults.NATpipe_NAT_siRNAs.get(originating).keySet()) {
                    NATpipeTotal += NATpipeResults.NATpipe_NAT_siRNAs.get(originating).get(nat).size();
                }
            }

            System.out.println("Natpipe total: " + NATpipeTotal);

        }

    }

}
