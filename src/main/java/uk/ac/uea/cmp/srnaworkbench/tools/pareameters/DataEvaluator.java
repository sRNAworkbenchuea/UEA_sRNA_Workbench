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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class DataEvaluator {

    /*
    Step 1: See how many known plant miRNAs are present and extract them
    Step 2: Perform target prediction with these looking at category 0/1
            using the permissive rules
    Step 3: If there is enough, e.g. ~350, then the data set is probably fine
     */
    static File smallRNAs;
    static File plantMicroRNAs;
    static File degradome;
    static File transcriptome;
    static File miRNA_Candidates;

    public static void main(String[] args) {

        Tools.getInstance();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("D:\\Dropbox\\PAREameters\\conserved_specific\\conserved_miRNA.fa")));
            BufferedReader br = new BufferedReader(new FileReader(new File("D:\\Dropbox\\PAREameters\\conserved_specific\\conserved_miRNA_targets.csv")));
            String line;
            br.readLine();
            HashMap<String, String> miRNAs = new HashMap();
            while ((line = br.readLine()) != null) {

                String splits[] = line.split(",");

                String id = splits[0].replace("\"", "").trim();
                String seq = splits[1].replace("\"", "").trim();
                
                miRNAs.put(seq, id);
                
                br.readLine();
                br.readLine();

            }
            
            List<String> sequences = new ArrayList(miRNAs.keySet());
            Collections.sort(sequences);
            
            for(String s : sequences)
            {
                System.out.println(miRNAs.get(s) + "," + s);
            }
            
            System.out.println("");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        smallRNAs = new File("D:\\PAREameters\\Oryza_sativa\\GSE18251\\seedling\\smallRNAs.fasta");
//        plantMicroRNAs = new File("D:\\PAREameters\\Oryza_sativa\\mature.fa");
//        degradome = new File("D:\\PAREameters\\Oryza_sativa\\GSE18251\\seedling\\degradome.fasta");
//        transcriptome = new File("D:\\PAREameters\\Oryza_sativa\\transcript.fa");
//
//        miRNA_Candidates = getPresentMicroRNAs();
//
//        runPAREsnip2();
//
//        evaluateResults();
    }

    private static File getPresentMicroRNAs() {

        File candidates = new File("D:\\PAREameters\\testing\\candidates.fa");
        candidates.deleteOnExit();

        HashMap<String, Integer> seqs = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(smallRNAs));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(">")) {
                    if (!seqs.containsKey(line)) {
                        seqs.put(line, 1);
                    } else {
                        seqs.put(line, seqs.get(line) + 1);
                    }
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        HashSet<String> seen = new HashSet();
        try {

            BufferedReader br = new BufferedReader(new FileReader(plantMicroRNAs));
            BufferedWriter bw = new BufferedWriter(new FileWriter(candidates));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(">")) {
                    if (seqs.containsKey(line) && seqs.get(line) >= 5 && !seen.contains(line)) {
                        for (int i = 0; i < seqs.get(line); i++) {
                            bw.write(">" + line + "\n");
                            bw.write(line + "\n");
                        }
                        seen.add(line);
                        bw.flush();
                    }
                }
            }
            br.close();
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Number of plant miRNA seqs present with abundance >=5: " + seen.size());
        return candidates;
    }

    private static void runPAREsnip2() {
        RuleSet rs = RuleSet.getRuleSet();
        rs.setDefaultCarrington();
        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        config.setDefaultFlexibleParameters();
        config.setAllowedCategories(2, false);
        config.setAllowedCategories(3, false);
        config.setAllowedCategories(4, false);
        Paresnip2InputFiles input = Paresnip2InputFiles.getInstance();

        input.addDegradomeReplicate(degradome);
        input.addSmallRNAReplicate(miRNA_Candidates);
        input.addTranscriptome(transcriptome);

        input.setOuputDirectory(new File("D:\\PAREameters\\testing"));

        uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine e = new Engine();

    }

    private static void evaluateResults() {
        File output = new File("D:\\PAREameters\\testing\\candidates_degradome.csv");

        HashSet<String> miRNAs_with_targets = new HashSet();
        int count = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(output));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                line += "\n";
                line += br.readLine();
                line += "\n";
                line += br.readLine();

                String splits[] = line.split(",");

                String seq = splits[1].replace("\"", "").substring(0).trim();
                miRNAs_with_targets.add(seq);

                count++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(count + " interactions from " + miRNAs_with_targets.size() + " miRNA sequences");

    }

}
