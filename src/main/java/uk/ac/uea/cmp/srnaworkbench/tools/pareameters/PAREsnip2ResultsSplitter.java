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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

/**
 *
 * @author rew13hpu
 */
public class PAREsnip2ResultsSplitter {

    //split paresnip2 results file up into categories and then sort by
    //alignment score, MFE ratio and p-value
    static HashMap<Integer, Set<String>> categoryReusults;
    static File resultsFile;
    static File output;

    public static void main(String args[]) throws Exception {

        Tools.getInstance();
        HashMap<String, HashMap<Integer, Integer>> map = new HashMap();

        output = new File("/scratch/testing/");
        File dir = new File("/scratch/p-collaboration/processed_fasta/PARE");
        File resultsDir = new File("/scratch/backup_12_11_18/ping/paresnip2_results/CDS_analysis");
        File transcript = new File("/scratch/p-collaboration/ITAG3.2_CDS.fasta");

        for (File subdir : dir.listFiles()) {
            if (!subdir.getName().contains("sat")) {
                for (File sample : subdir.listFiles()) {
                    //Align these to reference
                    //split gene name alignments on the first space and store positions
                    //check results don't contain predicted targets at these alignment sites

                    sample = makeNonRedundant(sample, 19, 21);

                    File patmanAlignment = alignSeqs(sample, transcript);

                    BufferedReader br = new BufferedReader(new FileReader(patmanAlignment));

                    String line;

                    while ((line = br.readLine()) != null) {
                        String splits[] = line.split("\t");
                        String gene = splits[0].split(" ")[0];
                        Integer abundance = Integer.parseInt(splits[1].split("\\(")[1].split("\\)")[0]);
                        Integer pos = Integer.parseInt(splits[2]);

                        if (!map.containsKey(gene)) {
                            map.put(gene, new HashMap());
                        }

                        if (!map.get(gene).containsKey(pos)) {
                            map.get(gene).put(pos, abundance);
                        } else {
                            map.get(gene).put(pos, (map.get(gene).get(pos) + abundance));
                        }

                    }

                }
            }
        }

        for (File replicate : resultsDir.listFiles()) {
            for (File sample : replicate.listFiles()) {
                for (File catSplit : sample.listFiles()) {

                    BufferedReader br = new BufferedReader(new FileReader(catSplit));
                    String header = br.readLine();
                    String line;
                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(sample.getAbsolutePath() + File.separator + catSplit.getName() + "_processed.csv")));
                    bw.write(header);
                    bw.newLine();

                    while ((line = br.readLine()) != null) {
                        line += System.lineSeparator();
                        line += br.readLine();
                        line += System.lineSeparator();
                        line += br.readLine();

                        String splits[] = line.split(",");
                        String gene = splits[2].split(" ")[0].replace("\"", "");
                        Integer cleavePos = Integer.parseInt(splits[4]);

                        if (!map.containsKey(gene)) {
                            bw.write(line);
                            bw.newLine();
                        } else if (!map.get(gene).containsKey(cleavePos)) {
                            bw.write(line);
                            bw.newLine();
                        }
                    }
                    
                    bw.flush();
                }
            }
        }

//        File dir = new File("/scratch/backup_12_11_18/ping/paresnip2_results");
//
//        for (File reference : dir.listFiles()) {
//            if (reference.isDirectory()) {
//                for (File sample : reference.listFiles()) {
//                    for (File replicate : sample.listFiles()) {
//                        categoryReusults = new HashMap<>();
//                        resultsFile = replicate.listFiles()[0];
//                        BufferedReader br = new BufferedReader(new FileReader(resultsFile));
//                        output = replicate;
//                        String header = br.readLine();
//                        String line;
//
//                        while ((line = br.readLine()) != null) {
//                            line += System.lineSeparator();
//                            line += br.readLine();
//                            line += System.lineSeparator();
//                            line += br.readLine();
//
//                            String splits[] = line.split(",");
//
//                            String alignment[] = splits[10].split(System.lineSeparator());
//
//                            String smallRNA = new StringBuilder(alignment[0]).reverse().toString().replace("\"", "");
//                            String align = new StringBuilder(alignment[1]).reverse().toString();
//                            String mRNA = new StringBuilder(alignment[2]).reverse().toString().replace("\"", "");
//
//                            align = align.trim();
//
//                            int i = 2;
//
//                            while (smallRNA.charAt(i) == ' ') {
//                                i++;
//                            }
//
//                            StringBuilder sb = new StringBuilder();
//
//                            for (int x = 0; x < i; x++) {
//                                sb.append(" ");
//                            }
//
//                            for (; i < smallRNA.length() && smallRNA.charAt(i) != ' '; i++) {
//                                sb.append(getChar(smallRNA.charAt(i), mRNA.charAt(i)));
//                            }
//
//                            StringBuilder a = new StringBuilder();
//                            a.append("\"");
//                            a.append(smallRNA);
//                            a.append(System.lineSeparator());
//                            a.append(sb);
//                            a.append(System.lineSeparator());
//                            a.append(mRNA);
//                            a.append("\"");
//
//                            splits[10] = a.toString();
//
//                            StringBuilder result = new StringBuilder();
//
//                            for (String s : splits) {
//                                result.append(s);
//                                result.append(",");
//                            }
//
//                            result.deleteCharAt(result.lastIndexOf(","));
//
//                            Integer category = Integer.parseInt(splits[3]);
//
//                            if (!categoryReusults.containsKey(category)) {
//                                categoryReusults.put(category, new HashSet());
//                            }
//
//                            categoryReusults.get(category).add(result.toString());
//
//                        }
//
//                        output.mkdir();
//
//                        // String path = output.getAbsolutePath() + File.separator + resultsFile.getName().split("\\.")[0];
//                        //File p = new File(path);
//                        //p.mkdir();
//                        for (Integer cat : categoryReusults.keySet()) {
//
//                            File outFile = new File(output.getAbsolutePath() + File.separator + resultsFile.getName() + "_category_" + cat + ".csv");
//                            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
//                            writer.write(header);
//                            writer.newLine();
//                            List<String> list = new ArrayList(categoryReusults.get(cat));
//                            Collections.sort(list, new ResultsSorter());
//
//                            for (String s : list) {
//                                writer.write(s);
//                                writer.newLine();
//                            }
//
//                            writer.flush();
//                        }
//                    }
//                }
//            }
//        }
    }

    private static String getChar(char a, char b) {

        if ((a == 'A' && b == 'T') || (a == 'T' && b == 'A')) {
            return "|";
        } else if ((a == 'G' && b == 'C') || (a == 'C' && b == 'G')) {
            return "|";
        } else if ((a == 'G' && b == 'T') || (a == 'T' && b == 'G')) {
            return "o";
        }

        return " ";
    }

    public static class ResultsSorter implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {

            String splitsStrA[] = o1.split(",");
            String splitsStrB[] = o2.split(",");

            double mfeRatioA = Double.parseDouble(splitsStrA[14]);
            double mfeRatioB = Double.parseDouble(splitsStrB[14]);

            double pvalA = Double.parseDouble(splitsStrA[15]);
            double pvalB = Double.parseDouble(splitsStrB[15]);

            int abundA = Integer.parseInt(splitsStrA[5]);
            int abundB = Integer.parseInt(splitsStrB[5]);

            if (abundA != abundB) {
                return Integer.compare(abundB, abundA);
            } else if (mfeRatioA != mfeRatioB) {
                return Double.compare(mfeRatioB, mfeRatioA);
            } else {
                return Double.compare(pvalB, pvalA);
            }
        }

    }

    private static File alignSeqs(File sRNAs, File longReads) {

        String alignedFile = output.getAbsolutePath() + File.separator + sRNAs.getName() + "_" + longReads.getName() + ".patman";

        File patmanOutput = new File(alignedFile);

        PatmanParams newP_Params = new PatmanParams();
        newP_Params.setMaxGaps(0);
        newP_Params.setMaxMismatches(0);
        newP_Params.setPreProcess(false);
        newP_Params.setPostProcess(false);
        newP_Params.setMakeNR(false);
        newP_Params.setPositiveStrandOnly(true);

        File tmpDir = new File(output.getAbsolutePath() + File.separator + "/ping_tmp");

        try {
            tmpDir.mkdir();
            PatmanRunner runner = new PatmanRunner(sRNAs, longReads,
                    patmanOutput, tmpDir, newP_Params);
            runner.setUsingDatabase(false);

            Thread myThread = new Thread(runner);

            myThread.start();
            myThread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpDir.deleteOnExit();
        return patmanOutput;
    }

    private static File makeNonRedundant(File seqs, int min, int max) {

        boolean lowComplexity = false;
        HashMap<String, Integer> smallRNAs = new HashMap();
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
                                    if(lowComplexity)
                                    {
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

        File seqsNonRedundant = new File(output.getAbsolutePath() + File.separator + seqs.getName().split("\\.")[0] + ".NR.fa");
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

    private static boolean isLowComplexity(String seq) {

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

    static boolean isInvalid(String seq) {
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
