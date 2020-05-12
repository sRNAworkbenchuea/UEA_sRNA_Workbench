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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.MessengerRNA;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class CrossValidation {

   

    public static void runRetainRateAnalysis() {

        Tools.getInstance();

        File dir = new File("D:\\PAREameters_blank_data\\Dalmay");
        for (File subDir : dir.listFiles()) {

            File miRNAs = new File(subDir.getAbsolutePath() + File.separator + "present_targets_miRNAs.fa");
            File PAREametersResults = new File(subDir.getAbsolutePath() + File.separator + "miRNA_alignments.csv");
            File validatedSet = new File(subDir.getAbsolutePath() + File.separator + "present_targets_cat_0_1.csv");
            File degradome = new File(subDir.getAbsolutePath() + File.separator + "degradome.fasta");
            File transcriptome = new File(subDir.getAbsolutePath() + File.separator + "transcript.fa");
            File smallRNAs = new File(subDir.getAbsolutePath() + File.separator + "smallRNAs.fasta");
            try {
                File results = new File(subDir.getAbsolutePath() + File.separator + "results.csv");
                BufferedWriter foldWriter = new BufferedWriter(new FileWriter(results));
                foldWriter.write("Retain rate, Possible validated, Inferred TP, Inferred FP, Test Inferred SE, Test Inferred PPV");
                foldWriter.write("\n");

                for (double j = 50.0; j <= 100.0; j += 5.0) {

                    File foldDir = new File(subDir.getAbsolutePath() + File.separator + ((int) j));
                    foldDir.mkdirs();

                    File plotDir = new File(foldDir.getAbsolutePath() + File.separator + "plot_data");
                    plotDir.mkdir();

                    PlotProducer pp = new PlotProducer(plotDir, PAREametersResults);
                    pp.producePlots();

                    double ratio = j / 100;

                    PAREametersEngine.inferTargetingCriteria(ratio, PAREametersResults, foldDir, plotDir);

                    File generatedRules = new File(foldDir.getAbsolutePath() + File.separator + "targeting_rules.txt");
                    File generatedConfig = new File(foldDir.getAbsolutePath() + File.separator + "parameters.txt");

                    //Evaluate test using inferred
                    int testInferred[] = runInferred(miRNAs, degradome, transcriptome, generatedRules, generatedConfig, validatedSet, new ArrayList<String>(), foldDir);

                    int testInferredPossible = testInferred[0];
                    int testInferredValidated = testInferred[1];
                    int testInferredNonValidated = testInferred[2];

                    //  foldWriter.write("Fold, Test possible TP, Test inferred TP, Test Inferred FP, Test Inferred SE, Test Inferred PPV");
                    foldWriter.write(j + "," + testInferredPossible + "," + testInferredValidated + "," + testInferredNonValidated + "," + ",");
                    foldWriter.write("\n");
                    foldWriter.flush();

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    //Remove any of the validated targets from the PAREameters results
    //Train on 50,60,70,etc of those and then evaluate on the validated set
    //HOPEFULLY as we look at an incresing amount, the Se and PPV will improve?
    public static void runTrainAmountExperimentsPAREameters() {
        Tools.getInstance();

        File dir = new File("/scratch/Josh/PAREameters_amount_analysis_low/");
        //File dir = new File("D:\\PAREameters_blank_data");

        for (File group : dir.listFiles()) {
            if (group.getName().contains("Dalmay")) {
                for (File subDir : group.listFiles()) {

                    File miRNAs = new File(subDir.getAbsolutePath() + File.separator + "present_targets_miRNAs.fa");
                    File PAREametersResults = new File(subDir.getAbsolutePath() + File.separator + "miRNA_alignments.csv");
                    File validatedSet = new File(subDir.getAbsolutePath() + File.separator + "present_targets_cat_0_1.csv");
                    File degradome = new File(subDir.getAbsolutePath() + File.separator + "degradome.fasta");
                    File transcriptome = new File(subDir.getAbsolutePath() + File.separator + "transcript.fa");

                    try {

                        for (double j = 10.0; j <= 40.0; j += 10.0) {

                            File foldDir = new File(subDir.getAbsolutePath() + File.separator + ((int) j));
                            foldDir.mkdirs();
                            File results = new File(foldDir.getAbsolutePath() + File.separator + "results.csv");
                            BufferedWriter foldWriter = new BufferedWriter(new FileWriter(results));
                            foldWriter.write("Fold, Test size, Test inferred TP, Test Inferred FP, Test Inferred SE, Test Inferred PPV");
                            foldWriter.write("\n");

                            for (int i = 1; i <= 50; i++) {
                                File generatedDir = new File(foldDir.getAbsolutePath() + File.separator + i);
                                generatedDir.mkdir();

                                File trainSet = new File(generatedDir.getAbsolutePath() + File.separator + "train.csv");
                                File testSet = new File(generatedDir.getAbsolutePath() + File.separator + "test.csv");

                                File trainMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "train_miRNAs.fa");
                                File testMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "test_miRNAs.fa");

                                BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainSet));
                                BufferedWriter testWriter = new BufferedWriter(new FileWriter(testSet));
                                BufferedWriter testMiRNAsWriter = new BufferedWriter(new FileWriter(testMiRNAs));

                                String header;
                                List<String> validatedTargets = new ArrayList();
                                List<String> pareametersTargets = new ArrayList();
                                HashMap<String, String> miRNA_seqs = new HashMap();

                                BufferedReader br = new BufferedReader(new FileReader(validatedSet));
                                header = br.readLine();

                                trainWriter.write(header + "\n");
                                testWriter.write(header + "\n");

                                String line;
                                while ((line = br.readLine()) != null) {
                                    line += "\n";
                                    line += br.readLine();
                                    line += "\n";
                                    line += br.readLine();

                                    String splits[] = line.split(",");

                                    String id = splits[0].replace("\"", "");
                                    String seq = splits[1].replace("\"", "");

                                    if (!miRNA_seqs.containsKey(id)) {
                                        miRNA_seqs.put(id, seq);
                                    }

                                    validatedTargets.add(line);
                                }

                                br.close();

                                br = new BufferedReader(new FileReader(PAREametersResults));

                                br.readLine();

                                while ((line = br.readLine()) != null) {
                                    line += "\n";
                                    line += br.readLine();
                                    line += "\n";
                                    line += br.readLine();

                                    pareametersTargets.add(line);
                                }

                                System.out.println("Before remove: " + pareametersTargets.size());
                                Iterator<String> it = pareametersTargets.iterator();

                                while (it.hasNext()) {
                                    String entry = it.next();
                                    String entrySplits[] = entry.split(",");
                                    String gene = entrySplits[2].replace("\"", "").split("\\.")[0];
                                    int pos = Integer.parseInt(entrySplits[4]);
                                    String miRNASeq = entrySplits[10].replace("\"", "").replace("-", "").split("'")[1].trim();
                                    boolean found = false;

                                    for (String validated : validatedTargets) {
                                        String validatedSplits[] = validated.split(",");
                                        String validatedGene = validatedSplits[2];
                                        String validatedMiRNASeq = validatedSplits[1];
                                        int validatedPos = Integer.parseInt(validatedSplits[4]);

                                        if (validatedPos == pos && gene.equals(validatedGene) && miRNASeq.equals(validatedMiRNASeq)) {
                                            found = true;
                                        }
                                    }

                                    if (found) {
                                        it.remove();
                                    }
                                }

                                System.out.println("After remove: " + pareametersTargets.size());

                                HashMap<String, String> testMiRNAsToWrite = new HashMap();
                                HashMap<String, String> trainMiRNAsToWrite = new HashMap();

                                List<String> trainData = new ArrayList();
                                List<String> testData = new ArrayList();

                                int train = (int) Math.ceil(((double) pareametersTargets.size() / 100.0) * j);

                                int count = 0;

                                Collections.shuffle(pareametersTargets);

                                it = pareametersTargets.iterator();

                                while (it.hasNext()) {
                                    count++;

                                    String l = it.next();

                                    String id = l.split(",")[0].replace("\"", "");

                                    if (count <= train) {
                                        trainWriter.write(l);
                                        trainWriter.write("\n");
                                        trainData.add(l);
                                        // trainMiRNAsToWrite.put(id, miRNA_seqs.get(id));
                                    }

                                    it.remove();

                                }

                                trainWriter.close();

                                br.close();

                                File plotDir = new File(generatedDir.getAbsolutePath() + File.separator + "plot_data");
                                plotDir.mkdir();

                                PlotProducer pp = new PlotProducer(plotDir, trainSet);
                                pp.producePlots();

                                PAREametersEngine.inferTargetingCriteria(0.85, trainSet, generatedDir, plotDir);

                                File generatedRules = new File(generatedDir.getAbsolutePath() + File.separator + "targeting_rules.txt");
                                File generatedConfig = new File(generatedDir.getAbsolutePath() + File.separator + "parameters.txt");

                                //Evaluate test using inferred
                                int testInferred[] = runInferred(miRNAs, degradome, transcriptome, generatedRules, generatedConfig, validatedSet, new ArrayList(), generatedDir);

                                int testInferredPossible = testInferred[0];
                                int testInferredValidated = testInferred[1];
                                int testInferredNonValidated = testInferred[2];

                                //  foldWriter.write("Fold, Test possible TP, Test inferred TP, Test Inferred FP, Test Inferred SE, Test Inferred PPV");
                                foldWriter.write(i + "," + validatedTargets.size() + "," + testInferredValidated + "," + testInferredNonValidated + "," + ",");
                                foldWriter.write("\n");
                                foldWriter.flush();

                            }

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }

        }

    }

    public static void runTrainAmountExperiments() {
        Tools.getInstance();

        File dir = new File("/scratch/Josh/PAREameters_amount_analysis_low_validated/");
        //File dir = new File("D:\\PAREameters_blank_data");

        for (File group : dir.listFiles()) {
            if (group.getName().contains("Dalmay")) {
                for (File subDir : group.listFiles()) {

                    File miRNAs = new File(subDir.getAbsolutePath() + File.separator + "present_targets_miRNAs.fa");
                    File validatedSet = new File(subDir.getAbsolutePath() + File.separator + "present_targets_cat_0_1.csv");
                    File degradome = new File(subDir.getAbsolutePath() + File.separator + "degradome.fasta");
                    File transcriptome = new File(subDir.getAbsolutePath() + File.separator + "transcript.fa");

                    try {

                        for (double j = 10.0; j <= 40.0; j += 10.0) {

                            File foldDir = new File(subDir.getAbsolutePath() + File.separator + ((int) j));
                            foldDir.mkdirs();
                            File results = new File(foldDir.getAbsolutePath() + File.separator + "results.csv");
                            BufferedWriter foldWriter = new BufferedWriter(new FileWriter(results));
                            foldWriter.write("Fold, Train size, Test size, Test inferred TP, Test Inferred FP, Test Inferred SE, Test Inferred PPV");
                            foldWriter.write("\n");

                            for (int i = 1; i <= 50; i++) {
                                File generatedDir = new File(foldDir.getAbsolutePath() + File.separator + i);
                                generatedDir.mkdir();

                                File trainSet = new File(generatedDir.getAbsolutePath() + File.separator + "train.csv");
                                File testSet = new File(generatedDir.getAbsolutePath() + File.separator + "test.csv");

                                File trainMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "train_miRNAs.fa");
                                File testMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "test_miRNAs.fa");

                                BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainSet));
                                BufferedWriter testWriter = new BufferedWriter(new FileWriter(testSet));
                                BufferedWriter testMiRNAsWriter = new BufferedWriter(new FileWriter(testMiRNAs));

                                String header;
                                List<String> validatedTargets = new ArrayList();
                                HashMap<String, String> miRNA_seqs = new HashMap();

                                BufferedReader br = new BufferedReader(new FileReader(validatedSet));
                                header = br.readLine();

                                trainWriter.write(header + "\n");
                                testWriter.write(header + "\n");

                                String line;
                                while ((line = br.readLine()) != null) {
                                    line += "\n";
                                    line += br.readLine();
                                    line += "\n";
                                    line += br.readLine();

                                    String splits[] = line.split(",");

                                    String id = splits[0].replace("\"", "");
                                    String seq = splits[1].replace("\"", "");

                                    if (!miRNA_seqs.containsKey(id)) {
                                        miRNA_seqs.put(id, seq);
                                    }

                                    validatedTargets.add(line);
                                }

                                int train = (int) Math.ceil(((double) validatedTargets.size() / 100.0) * j);

                                int count = 0;

                                Collections.shuffle(validatedTargets);

                                Iterator<String> it = validatedTargets.iterator();

                                HashMap<String, String> testMiRNAsToWrite = new HashMap();
                                HashMap<String, String> trainMiRNAsToWrite = new HashMap();

                                List<String> trainData = new ArrayList();
                                List<String> testData = new ArrayList();

                                while (it.hasNext()) {
                                    count++;

                                    String l = it.next();

                                    String id = l.split(",")[0].replace("\"", "");

                                    if (count <= train) {
                                        trainWriter.write(l);
                                        trainWriter.write("\n");
                                        trainData.add(l);
                                        trainMiRNAsToWrite.put(id, miRNA_seqs.get(id));
                                    } else {
                                        testWriter.write(l);
                                        testWriter.write("\n");
                                        testData.add(l);
                                        testMiRNAsToWrite.put(id, miRNA_seqs.get(id));

                                    }

                                    it.remove();

                                }

                                for (String s : testMiRNAsToWrite.keySet()) {
                                    for (int k = 0; k < 5; k++) {
                                        testMiRNAsWriter.write(s);
                                        testMiRNAsWriter.write("\n");
                                        testMiRNAsWriter.write(miRNA_seqs.get(s));
                                        testMiRNAsWriter.write("\n");
                                    }
                                }

                                //Write the command to the script
                                testMiRNAsWriter.close();

                                trainWriter.close();
                                testWriter.close();
                                br.close();

                                File plotDir = new File(generatedDir.getAbsolutePath() + File.separator + "plot_data");
                                plotDir.mkdir();

                                PlotProducer pp = new PlotProducer(plotDir, trainSet);
                                pp.producePlots();

                                PAREametersEngine.inferTargetingCriteria(0.85, trainSet, generatedDir, plotDir);

                                File generatedRules = new File(generatedDir.getAbsolutePath() + File.separator + "targeting_rules.txt");
                                File generatedConfig = new File(generatedDir.getAbsolutePath() + File.separator + "parameters.txt");

                                //Evaluate test using inferred
                                int testInferred[] = runInferred(testMiRNAs, degradome, transcriptome, generatedRules, generatedConfig, testSet, trainData, generatedDir);

                                int testInferredPossible = testInferred[0];
                                int testInferredValidated = testInferred[1];
                                int testInferredNonValidated = testInferred[2];

                                //  foldWriter.write("Fold, Test possible TP, Test inferred TP, Test Inferred FP, Test Inferred SE, Test Inferred PPV");
                                foldWriter.write(i + "," + trainData.size() + "," + testInferredPossible + "," + testInferredValidated + "," + testInferredNonValidated + "," + ",");
                                foldWriter.write("\n");
                                foldWriter.flush();

                            }

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }

        }

    }

    public static void runCVExperiments() {
        Tools.getInstance();

        File dir = new File("/scratch/Josh/PAREameters_CV_analysis/");
        ///scratch/Josh/PAREameters_CV_analysis/

        for (File group : dir.listFiles()) {

            for (File subDir : group.listFiles()) {

                File miRNAs = new File(subDir.getAbsolutePath() + File.separator + "present_targets_miRNAs.fa");
                File validatedSet = new File(subDir.getAbsolutePath() + File.separator + "present_targets_cat_0_1.csv");
                File degradome = new File(subDir.getAbsolutePath() + File.separator + "degradome.fasta");
                File transcriptome = new File(subDir.getAbsolutePath() + File.separator + "transcript.fa");
                File results = new File(subDir.getAbsolutePath() + File.separator + "results.csv");

                try {

                    BufferedWriter foldWriter = new BufferedWriter(new FileWriter(results));
                    //Fold, Training possible TP, Training inferred TP, Training Allen TP, Training inferred FP, Training Allen FP, Test possible TP, Test inferred TP, Test Allen TP, Test inferred FP, Test Allen FP, Training Inferred SE, Training Allen SE, Training Inferred PPV, Training Allen PPV, Test Inferred SE, Test Allen SE, Test Inferred PPV, Test Allen PPV
                    foldWriter.write("Fold, Training possible TP, Training inferred TP, Training Allen TP, Training inferred FP, Training Allen FP, Test possible TP, Test inferred TP, Test Allen TP, Test Inferred FP, Test Allen FP, Training Inferred SE, Training Allen SE, Training Inferred PPV, Training Allen PPV, Test Inferred SE, Test Allen SE, Test Inferred PPV, Test Allen PPV");
                    foldWriter.write("\n");

                    for (int i = 1; i <= 50; i++) {
                        //create the folders with the randomly sampled data

                        File generatedDir = new File(subDir.getAbsolutePath() + File.separator + i);
                        generatedDir.mkdir();

                        File trainSet = new File(generatedDir.getAbsolutePath() + File.separator + "train.csv");
                        File testSet = new File(generatedDir.getAbsolutePath() + File.separator + "test.csv");

                        File trainMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "train_miRNAs.fa");
                        File testMiRNAs = new File(generatedDir.getAbsolutePath() + File.separator + "test_miRNAs.fa");

                        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainSet));
                        BufferedWriter testWriter = new BufferedWriter(new FileWriter(testSet));
                        BufferedWriter testMiRNAsWriter = new BufferedWriter(new FileWriter(testMiRNAs));
                        BufferedWriter trainMiRNAsWriter = new BufferedWriter(new FileWriter(trainMiRNAs));

                        String header;
                        List<String> validatedTargets = new ArrayList();
                        HashMap<String, String> miRNA_seqs = new HashMap();

                        BufferedReader br = new BufferedReader(new FileReader(validatedSet));
                        header = br.readLine();

                        trainWriter.write(header + "\n");
                        testWriter.write(header + "\n");

                        String line;
                        while ((line = br.readLine()) != null) {
                            line += "\n";
                            line += br.readLine();
                            line += "\n";
                            line += br.readLine();

                            String splits[] = line.split(",");

                            String id = splits[0].replace("\"", "");
                            String seq = splits[1].replace("\"", "");

                            if (!miRNA_seqs.containsKey(id)) {
                                miRNA_seqs.put(id, seq);
                            }

                            validatedTargets.add(line);
                        }

                        int train = (int) Math.ceil(((double) validatedTargets.size() / 100.0) * 75.0);

                        int count = 0;

                        Collections.shuffle(validatedTargets);

                        Iterator<String> it = validatedTargets.iterator();

                        HashMap<String, String> testMiRNAsToWrite = new HashMap();
                        HashMap<String, String> trainMiRNAsToWrite = new HashMap();

                        List<String> trainData = new ArrayList();
                        List<String> testData = new ArrayList();

                        while (it.hasNext()) {
                            count++;

                            String l = it.next();

                            String id = l.split(",")[0].replace("\"", "");

                            if (count <= train) {
                                trainWriter.write(l);
                                trainWriter.write("\n");
                                trainData.add(l);
                                trainMiRNAsToWrite.put(id, miRNA_seqs.get(id));
                            } else {
                                testWriter.write(l);
                                testWriter.write("\n");
                                testData.add(l);
                                testMiRNAsToWrite.put(id, miRNA_seqs.get(id));

                            }

                            it.remove();

                        }

                        for (String s : testMiRNAsToWrite.keySet()) {
                            for (int j = 0; j < 5; j++) {
                                testMiRNAsWriter.write(s);
                                testMiRNAsWriter.write("\n");
                                testMiRNAsWriter.write(miRNA_seqs.get(s));
                                testMiRNAsWriter.write("\n");
                            }
                        }

                        for (String s : trainMiRNAsToWrite.keySet()) {
                            for (int j = 0; j < 5; j++) {
                                trainMiRNAsWriter.write(s);
                                trainMiRNAsWriter.write("\n");
                                trainMiRNAsWriter.write(miRNA_seqs.get(s));
                                trainMiRNAsWriter.write("\n");
                            }
                        }

                        //Write the command to the script
                        testMiRNAsWriter.close();
                        trainMiRNAsWriter.close();
                        trainWriter.close();
                        testWriter.close();
                        br.close();

                        File plotDir = new File(generatedDir.getAbsolutePath() + File.separator + "plot_data");
                        plotDir.mkdir();

                        PlotProducer pp = new PlotProducer(plotDir, trainSet);
                        pp.producePlots();

                        PAREametersEngine.inferTargetingCriteria(0.85, trainSet, generatedDir, plotDir);

                        File generatedRules = new File(generatedDir.getAbsolutePath() + File.separator + "targeting_rules.txt");
                        File generatedConfig = new File(generatedDir.getAbsolutePath() + File.separator + "parameters.txt");

                        //Evaluate train using inferred
//                    int trainInferred[] = runInferred(trainMiRNAs, degradome, transcriptome, generatedRules, generatedConfig, trainSet, testData, new File(generatedDir.getAbsolutePath() + File.separator + "trainInferred"));
//
//                    int trainInferredPossible = trainInferred[0];
//                    int trainInferredValidated = trainInferred[1];
//                    int trainInferredNonValidated = trainInferred[2];
                        //Evaluate train using Allen
//                    int trainAllen[] = runAllen(trainMiRNAs, degradome, transcriptome, trainSet, testData, new File(generatedDir.getAbsolutePath() + File.separator + "trainAllen"));
//
//                    int trainAllenPossible = trainAllen[0];
//                    int trainAllenValidated = trainAllen[1];
//                    int trainAllenNonValidated = trainAllen[2];
                        //Evaluate test using inferred
                        int testInferred[] = runInferred(testMiRNAs, degradome, transcriptome, generatedRules, generatedConfig, testSet, trainData, new File(generatedDir.getAbsolutePath() + File.separator + "testInferred"));

                        int testInferredPossible = testInferred[0];
                        int testInferredValidated = testInferred[1];
                        int testInferredNonValidated = testInferred[2];

                        //Evaluate test using Allen
//                    int testAllen[] = runAllen(testMiRNAs, degradome, transcriptome, testSet, trainData, new File(generatedDir.getAbsolutePath() + File.separator + "testAllen"));
//
//                    int testAllenPossible = testAllen[0];
//                    int testAllenValidated = testAllen[1];
//                    int testAllenNonValidated = testAllen[2];
                        //Fold, Training possible TP, Training inferred TP, Training Allen TP, Training inferred FP, Training Allen FP, Test possible TP, Test inferred TP, Test Allen TP, Test inferred FP, Test Allen FP, Training Inferred SE, Training Allen SE, Training Inferred PPV, Training Allen PPV, Test Inferred SE, Test Allen SE, Test Inferred PPV, Test Allen PPV
                        foldWriter.write(i + "," + trainData.size() + "," + 1 + "," + 1 + "," + 1 + "," + 1 + "," + testData.size() + "," + testInferredValidated + "," + 1 + "," + testInferredNonValidated + "," + 1 + "," + "" + "," + "" + "," + "" + "," + "" + "," + "" + "," + "" + "," + "" + "," + "");
                        foldWriter.write("\n");
                        foldWriter.flush();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    static int[] runInferred(File sRNAs, File degradome, File transcriptome, File rules, File parameters, File evaluate, List<String> ignore, File outputDir) {

        try {
            //Run PAREsnip2 using generated parameters and miRNAs
            RuleSet.reset();
            RuleSet.getRuleSet().loadRules(rules);
            Paresnip2Configuration.getInstance();

            Paresnip2InputFiles input = Paresnip2InputFiles.getInstance();
            Paresnip2Configuration.reset();
            Paresnip2Configuration.getInstance().loadConfig(parameters);

            //populate input
            input.reset();
            input.addSmallRNAReplicate(sRNAs);
            input.addDegradomeReplicate(degradome);
            input.addTranscriptome(transcriptome);
            input.setOuputDirectory(outputDir);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return runAndEvaluate(sRNAs, degradome, transcriptome, evaluate, ignore, outputDir);
    }

    static int[] runAndEvaluate(File sRNAs, File degradome, File transcriptome, File evaluate, List<String> ignore, File outputDir) {
        int[] arr = {0, 0, 0};
        try {
            MessengerRNA.setIsInit(false);
            uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine ps2 = new Engine();
            File ps2Results = new File(outputDir.getAbsolutePath() + File.separator + sRNAs.getName().split("\\.")[0] + "_" + degradome.getName().split("\\.")[0] + ".csv");

            File processedPS2Results = new File(outputDir.getAbsolutePath() + File.separator + "processed.csv");

            //We need to remove any train data from the PS2 results otherwise results will be nonsense
            BufferedWriter processedPS2Writer = new BufferedWriter(new FileWriter(processedPS2Results));
            BufferedReader ps2Reader = new BufferedReader(new FileReader(ps2Results));
            String ps2Line;
            String ps2Header = ps2Reader.readLine();

            processedPS2Writer.write(ps2Header);
            processedPS2Writer.write("\n");

            while ((ps2Line = ps2Reader.readLine()) != null) {

                ps2Line += "\n";
                ps2Line += ps2Reader.readLine();
                ps2Line += "\n";
                ps2Line += ps2Reader.readLine();

                String splits[] = ps2Line.split(",");

                String id = splits[1].replace("\"", "");
                String gene = splits[2].replace("\"", "").split("\\.")[0];
                int cleaveSite = Integer.parseInt(splits[4]);

                boolean found = false;

                for (String l : ignore) {
                    String trainSplits[] = l.split(",");

                    String trainID = trainSplits[0].replace("\"", "");
                    String trainGene = trainSplits[2].replace("\"", "");
                    int trainCleaveSite = Integer.parseInt(trainSplits[4]);

                    if (trainID.equals(id) && trainGene.equals(gene) && cleaveSite == trainCleaveSite) {
                        found = true;
                        break;
                    }

                }

                if (!found) {
                    processedPS2Writer.write(ps2Line);
                    processedPS2Writer.write("\n");
                }

            }

            processedPS2Writer.close();
            //Evaluate PAREsnip2 on test set                   
            File evOutput = new File(outputDir.getAbsolutePath() + File.separator + "evaluation");
            PAREametersEvaluator ev = new PAREametersEvaluator(sRNAs, degradome, transcriptome, evOutput, evaluate, processedPS2Results);
            ev.process();

            //Write evaluation results to a file
            int possible = 0;
            int validated = 0;
            int nonValidated = 0;

            BufferedReader resultsReader = new BufferedReader(new FileReader(new File(evOutput.getAbsolutePath() + File.separator + "compare_to_validated.csv")));

            String line;

            while ((line = resultsReader.readLine()) != null) {
                possible = Integer.parseInt(resultsReader.readLine().split(":")[1].trim());
                validated = Integer.parseInt(resultsReader.readLine().split(":")[1].trim());
                nonValidated = Integer.parseInt(resultsReader.readLine().split(":")[1].trim());
            }

            arr[0] = possible;
            arr[1] = validated;
            arr[2] = nonValidated;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return arr;
    }

    static int[] runAllen(File sRNAs, File degradome, File transcriptome, File evaluate, List<String> ignore, File outputDir) {

        try {
            //Run PAREsnip2 using generated parameters and miRNAs
            RuleSet rs = RuleSet.getRuleSet();
            Paresnip2InputFiles input = Paresnip2InputFiles.getInstance();
            Paresnip2Configuration config = Paresnip2Configuration.getInstance();

            rs.setDefaultAllen();
            config.setDefaultStringentParameters();
            config.setAllowedCategories(2, false);
            config.setAllowedCategories(3, false);
            config.setAllowedCategories(4, false);

            //populate input
            input.reset();
            input.addSmallRNAReplicate(sRNAs);
            input.addDegradomeReplicate(degradome);
            input.addTranscriptome(transcriptome);
            input.setOuputDirectory(outputDir);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return runAndEvaluate(sRNAs, degradome, transcriptome, evaluate, ignore, outputDir);
    }

}
