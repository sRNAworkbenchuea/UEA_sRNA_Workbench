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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Given two or more PAREameters results this class we find specific and
 * conserved miRNAs and their targets. It will then produce the plots from
 * individual miRNAs and specific/conserved miRNAs.
 *
 * @author rew13hpu
 */
public class PAREametersResultsProcessor {

    HashMap<String, File> resultsDirectories;
    int numResults;
    PAREametersInput input;
    PAREametersConfiguration config;
    File knownMatureMicroRNAs;
    HashMap<String, String> miRNA_Annotations;
    String headerLine;
    File outputDirectory;
    
    public static void main(String args[])
    {
        
    }

    public PAREametersResultsProcessor(File outputDirectory, File... results) {
        resultsDirectories = new HashMap();
        numResults = results.length;
        input = PAREametersInput.getInstance();
        config = PAREametersConfiguration.getInstance();
        knownMatureMicroRNAs = input.getMatureMiRNAs();
        this.outputDirectory = outputDirectory;
        populateNonRedundantMiRNAs(knownMatureMicroRNAs);

        for (File result : results) {
            resultsDirectories.put(result.getName(), result);
        }
    }

    public void process() {
        HashMap<String, HashSet<String>> resultsMicroRNAs = getResultsMicroRNAs();

        HashSet<String> conservedMicroRNAs = getConservedMicroRNAs(resultsMicroRNAs);

        HashMap<String, HashMap<String, HashSet<String>>> miRNAsWithTargets = getMicroRNAsWithTargets();

        HashMap<String, HashSet<String>> conservedMicroRNATargets = getConservedMicroRNATargets(conservedMicroRNAs, miRNAsWithTargets);

        writeSpecificAndConservedToFile(miRNAsWithTargets, conservedMicroRNATargets);

    }

    private void readMirCat2Results(File mircat2Results, HashSet<String> predictedMiRNAs) {

        //read mircat results
        try {
            BufferedReader br = new BufferedReader(new FileReader(mircat2Results));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String splits[] = line.split(",");
                if (!predictedMiRNAs.contains(splits[2])) {
                    predictedMiRNAs.add(splits[2]);
                }
                if (!splits[10].equals("-1")) {
                    if (!predictedMiRNAs.contains(splits[10])) {
                        predictedMiRNAs.add(splits[10]);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readMirPlantResults(File mirplantResults, HashSet<String> predictedMiRNAs) {
//read mirplant results
        try {
            BufferedReader br = new BufferedReader(new FileReader(mirplantResults));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String seq = splits[9].toUpperCase();
                if (!predictedMiRNAs.contains(seq)) {
                    predictedMiRNAs.add(seq);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashMap<String, Integer> makeNonRedundant(File seqs) {

        String line;
        HashMap<String, Integer> smallRNAs = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        if (!smallRNAs.containsKey(line)) {
                            smallRNAs.put(line, 1);
                        } else if (smallRNAs.containsKey(line)) {
                            smallRNAs.put(line, smallRNAs.get(line) + 1);
                        }

                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Iterator<Entry<String, Integer>> it = smallRNAs.entrySet().iterator();

        //Remove small RNAs if they don't meet the minimum criteria
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();

            String seq = entry.getKey();
            Integer abundance = entry.getValue();

            if (seq.length() < config.getMinSmallRNALength() || seq.length() > config.getMaxSmallRNALength()) {
                it.remove();
            } else if (abundance < config.getMinSmallRNAAbundance()) {
                it.remove();
            }
        }

        return smallRNAs;

    }

    private void populateNonRedundantMiRNAs(File miRNAFile) {

        try {

            miRNA_Annotations = new HashMap();
            //Store all plant miRNA annotations
            if (miRNAFile != null) {
                BufferedReader br = new BufferedReader(new FileReader(miRNAFile));
                HashMap<String, List<String>> nonRedunantMiRNAs = new HashMap();

                String line;

                String comment = "";
                while ((line = br.readLine()) != null) {

                    if (line.startsWith(">")) {
                        comment = line.split(">")[1];
                    } else if (nonRedunantMiRNAs.containsKey(line.replace("U", "T"))) {
                        line = line.replace("U", "T");
                        nonRedunantMiRNAs.get(line).add(comment);
                    } else {
                        line = line.replace("U", "T");
                        nonRedunantMiRNAs.put(line, new ArrayList());
                        nonRedunantMiRNAs.get(line).add(comment);
                    }
                }

                for (String miRNA : nonRedunantMiRNAs.keySet()) {

                    comment = "";

                    for (String annotation : nonRedunantMiRNAs.get(miRNA)) {
                        comment += annotation;
                        comment += " ";
                    }
                    comment = ">" + comment.trim();
                    miRNA_Annotations.put(miRNA, comment);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void addKnownMiRNAs(HashMap<String, Integer> seqs, HashSet<String> predictedMiRNAs) {

        for (String miRNA : miRNA_Annotations.keySet()) {
            if (seqs.containsKey(miRNA) && seqs.get(miRNA) >= config.getMinSmallRNAAbundance()) {
                if (!predictedMiRNAs.contains(miRNA)) {
                    predictedMiRNAs.add(miRNA);
                }
            }

        }

    }

    private HashMap<String, HashSet<String>> getResultsMicroRNAs() {

        HashMap<String, HashSet<String>> resultsMicroRNAs = new HashMap();

        for (String key : resultsDirectories.keySet()) {
            File results = resultsDirectories.get(key);
            resultsMicroRNAs.put(results.getName(), new HashSet());

            File mircat2Results = new File(results.getAbsolutePath() + File.separator + "mircat2" + File.separator + "smallRNAs.NR_output.csv");
            File mirplantResults = new File(results.getAbsolutePath() + File.separator + "miRPlant_results.txt");

            //Store predicted miRNAs for this PAREameters run
            HashSet<String> predictedMiRNAs = resultsMicroRNAs.get(results.getName());

            readMirCat2Results(mircat2Results, predictedMiRNAs);
            readMirPlantResults(mirplantResults, predictedMiRNAs);

            File smallRNAs = input.getSmallRNAs();

            HashMap<String, Integer> seqs = makeNonRedundant(smallRNAs);

            addKnownMiRNAs(seqs, predictedMiRNAs);
        }

        return resultsMicroRNAs;
    }

    private HashSet<String> getConservedMicroRNAs(HashMap<String, HashSet<String>> resultsMicroRNAs) {

        HashSet<String> conservedMicroRNAs = new HashSet();

        List<String> keys = new ArrayList<String>(resultsMicroRNAs.keySet());

        HashSet<String> firstSet = resultsMicroRNAs.get(keys.get(0));

        for (String miRNASeq : firstSet) {
            boolean contains = true;
            for (int i = 1; i < keys.size(); i++) {
                if (!resultsMicroRNAs.get(keys.get(i)).contains(miRNASeq)) {
                    contains = false;
                }
            }

            if (contains) {
                conservedMicroRNAs.add(miRNASeq);
            }
        }

        return conservedMicroRNAs;

    }

    private HashMap<String, HashMap<String, HashSet<String>>> getMicroRNAsWithTargets() {

        HashMap<String, HashMap<String, HashSet<String>>> miRNAsWithTargets = new HashMap();

        for (String key : resultsDirectories.keySet()) {
            File resultsDir = resultsDirectories.get(key);

            File microRNAsWithTargetsFile = new File(resultsDir.getAbsolutePath() + File.separator + "predicted_miRNAs_with_targets.fa");
            File PAREsnip2Results = new File(resultsDir.getAbsolutePath() + File.separator + "candidates_degradome.csv");
            miRNAsWithTargets.put(resultsDir.getName(), new HashMap());

            //Populate the miRNA sequences
            try {
                BufferedReader br = new BufferedReader(new FileReader(microRNAsWithTargetsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    miRNAsWithTargets.get(resultsDir.getName()).put(br.readLine(), new HashSet());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                BufferedReader br = new BufferedReader(new FileReader(PAREsnip2Results));
                String line;
                headerLine = br.readLine();

                while ((line = br.readLine()) != null) {
                    line += "\n" + br.readLine() + "\n";
                    line += br.readLine();

                    String splits[] = line.split(",");
                    String alignment_splits[] = splits[10].split("'");
                    String smallRNA = alignment_splits[1].trim().replace("-", "");

                    if (miRNA_Annotations.containsKey(smallRNA)) {
                        line = line.replace(">" + smallRNA, miRNA_Annotations.get(smallRNA));
                    } else {
                        line = line.replace(">" + smallRNA, "novel_miRNA_" + smallRNA);
                    }

                    if (miRNAsWithTargets.get(resultsDir.getName()).containsKey(smallRNA)) {

                        miRNAsWithTargets.get(resultsDir.getName()).get(smallRNA).add(line);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        return miRNAsWithTargets;

    }

    private HashMap<String, HashSet<String>> getConservedMicroRNATargets(HashSet<String> conservedMicroRNAs, HashMap<String, HashMap<String, HashSet<String>>> miRNAsWithTargets) {

        HashMap<String, HashSet<String>> conservedMicroRNATargets = new HashMap();

        for (String key : resultsDirectories.keySet()) {
            File resultsDir = resultsDirectories.get(key);

            Iterator<Entry<String, HashSet<String>>> it = miRNAsWithTargets.get(resultsDir.getName()).entrySet().iterator();

            while (it.hasNext()) {
                String miRNA_seq = it.next().getKey();
                HashSet<String> targets = miRNAsWithTargets.get(resultsDir.getName()).get(miRNA_seq);

                //Check if the miRNA is conserved
                if (conservedMicroRNAs.contains(miRNA_seq)) {
                    //check if the targets already exist (same gene, miRNA and cleavage site)
                    Iterator<String> subIterator = targets.iterator();

                    while (subIterator.hasNext()) {
                        String resultString = subIterator.next();
                        String splits[] = resultString.split(",");
                        Integer cleavePos = Integer.parseInt(splits[4]);
                        String gene = splits[2];

                        if (!conservedMicroRNATargets.containsKey(miRNA_seq)) {
                            conservedMicroRNATargets.put(miRNA_seq, new HashSet());
                            conservedMicroRNATargets.get(miRNA_seq).add(resultString);
                            subIterator.remove();
                        } else {

                            boolean found = false;
                            for (String existingResult : conservedMicroRNATargets.get(miRNA_seq)) {
                                String existingSplits[] = existingResult.split(",");
                                Integer existingCleavePos = Integer.parseInt(existingSplits[4]);
                                String existingGene = existingSplits[2];

                                if (existingCleavePos.equals(cleavePos) && existingGene.equals(gene)) {
                                    found = true;
                                }

                            }

                            if (!found) {
                                conservedMicroRNATargets.get(miRNA_seq).add(resultString);
                                subIterator.remove();
                            }
                        }

                    }
                }
            }

        }

        return conservedMicroRNATargets;
    }

    private void writeSpecificAndConservedToFile(HashMap<String, HashMap<String, HashSet<String>>> miRNAsWithTargets, HashMap<String, HashSet<String>> conservedMicroRNATargets) {
        for (String key : resultsDirectories.keySet()) {
            File resultsDir = resultsDirectories.get(key);
            
            Iterator<Entry<String, HashSet<String>>> it = miRNAsWithTargets.get(resultsDir.getName()).entrySet().iterator();
            try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(resultsDir.getAbsolutePath() + File.separator + "specific_miRNA_targets.csv")));
                    bw.write(headerLine + "\n");

                    while (it.hasNext()) {
                        String miRNA_seq = it.next().getKey();
                        HashSet<String> targets = miRNAsWithTargets.get(resultsDir.getName()).get(miRNA_seq);

                        Iterator<String> targetsIterator = targets.iterator();

                        while (targetsIterator.hasNext()) {
                            String result = targetsIterator.next();
                            bw.write(result + "\n");

                        }

                    }
            } catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
        }
        
        //Write conserved miRNA targets file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDirectory.getAbsolutePath() + File.separator + "conserved_miRNA_targets.csv")));
            bw.write(headerLine + "\n");

            for (String conserved_miRNA : conservedMicroRNATargets.keySet()) {
                for (String interaction : conservedMicroRNATargets.get(conserved_miRNA)) {
                    bw.write(interaction + "\n");
                }
            }

            bw.flush();
            bw.close();
        } catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
