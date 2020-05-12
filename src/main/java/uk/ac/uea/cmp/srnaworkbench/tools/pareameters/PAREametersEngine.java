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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.MiRCat2Main;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

/**
 * This class runs the PAREameters algorithm on the provided input data using
 * the provided configuration.
 *
 * @author Josh Thody
 */
public class PAREametersEngine {

    private PAREametersConfiguration config;
    private PAREametersInput input;

    private File alignmentPredictionsFile;

    private HashMap<String, Integer> smallRNAs;
    private HashMap<String, Integer> miRPlantCandidates;
    private HashMap<String, Integer> predicted_miRNAs;
    private HashMap<String, String> miRNA_Annotations;
    private HashMap<String, String> usersMiRNAs;
    private HashMap<String, String> uniquePositions;
    private HashMap<String, List<String>> miRNAIsoMirs;

    private Paresnip2InputFiles PAREsnip2Input;

    private miRPlantParameters miRPlantParameters;
    private File miRPlantResults;

    private File sRNALibraryNR;
    private File candidateMiRNAsNR;
    private File candidateMiRNAs;
    private File patmanOutput;
    private File PAREsnip2OutputDir;
    private File miRPlantOutputDir;
    private File mircat2OutputDir;

    public PAREametersEngine() {
        config = PAREametersConfiguration.getInstance();
        input = PAREametersInput.getInstance();

        File outputDirectory = input.getOutputDir();

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        mircat2OutputDir = new File(outputDirectory + File.separator + "mircat2");
        PAREsnip2OutputDir = new File((outputDirectory + File.separator + "paresnip2"));
        miRPlantOutputDir = new File((outputDirectory + File.separator + "mirplant"));

        candidateMiRNAs = new File(outputDirectory + File.separator + "candidates.fa");
        candidateMiRNAsNR = new File(outputDirectory + File.separator + "candidatesNR.fa");

        predicted_miRNAs = new HashMap();

        mircat2OutputDir.mkdirs();
        PAREsnip2OutputDir.mkdirs();
        miRPlantOutputDir.mkdirs();

        miRPlantParameters = new miRPlantParameters();

        miRPlantParameters.setGenome(input.getGenomeFile());
        miRPlantParameters.setGenomeName(input.getOrganismName());
        miRPlantParameters.setKnownHairpin(input.getHairpinSeqs());
        miRPlantParameters.setKnownMature(input.getMatureMiRNAs());
        miRPlantParameters.setSpeciesKnownGFF(input.getKnownMiRGFF());
        miRPlantParameters.setOutputDir(input.getOutputDir());

    }

    public void process() {

        //This needs changing to match paper
        //We need to find candidates absed on conservaton using mirplant
        //Then find candidats from the whole set of sRNAs using mircat2
        //Then put these sequences into PAREsnip2
        long startTime = System.nanoTime();

        //Convert sRNA file to non-redundant format
        sRNALibraryNR = makeNonRedundant(input.getSmallRNAs());

        processUsersMiRNAs();

        //align sRNA library to known miRNAs allowing up to 2 mismatches
        alignMicroRNAs(input.getMatureMiRNAs(), sRNALibraryNR);
        //process smallRNAs using mirCat2 
        runMiRCat2Algorithm();
        //process mircat2 reults
        //extractMiRCat2Results();
        //extract the aligned sequences
        findCandidateSequences();
        //Perform target prediction using the miRNA-aligned sequences
        runPAREsnip2();
        //Construct a set of potential miRNAs with targets
        constructSmallRNASet();
        //populate a list of NR known mature miRNAs
        populateNonRedundantMiRNAs(input.getMatureMiRNAs());
        //run mirPlant algorithm on candidate miRNAs with targets
        runMiRPlant();
        //process the mirPlant results to extract predicted miRNAs with targets
        processMiRPlantResults();
        //Combine the known miRNAs for this species
        combineKnownMicroRNAs();
        //compare PAREsnip2 results against predicted miRNAs
        comparePAREsnip2ResultsAgainstMicroRNAs();

        //Build plots
        PlotProducer pp = new PlotProducer(new File(input.getOutputDir() + File.separator + "plot_csv_files"), alignmentPredictionsFile, input.getOrganismName());
        pp.producePlots();

        //infer targeting rules
        inferTargetingCriteria(config.getRetainRate(), alignmentPredictionsFile, input.getOutputDir(), pp.getOutputDirectory());
        
        try {
            //copy the files to the R script directory
            Files.copy(new File(pp.getOutputDirectory() + File.separator + "mfe_ratio_plot.csv").toPath(), new File("ExeFiles" + File.separator + "R-scripts" + File.separator +"PAREameters" +File.separator + "mfe_ratio_plot.csv").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new File(pp.getOutputDirectory() + File.separator + "position_property_plot.csv").toPath(), new File("ExeFiles" + File.separator + "R-scripts" + File.separator +"PAREameters" +File.separator + "position_property_plot.csv").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(PAREametersEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (AppUtils.INSTANCE.isCommandLine()) {
            long duration = System.nanoTime() - startTime;
            long timeInMilliSeconds = duration / 1000000;
            long seconds = timeInMilliSeconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            String time = days + " days : " + hours % 24 + " hours : " + minutes % 60 + " minutes : " + seconds % 60 + " seconds";
            System.out.println("The analysis was completed in: " + time);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(input.getOutputDir().getAbsolutePath() + File.separator + "timing_results.txt")));
                bw.write(time);
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.exit(0);
        }

    }

    public static void inferTargetingCriteria(double percentRetained, File ps2Results, File outputDir, File plotDataDir) {
        //TODO implement
        File plotDirectory = plotDataDir;
        RuleSet rs = RuleSet.getRuleSet();
        rs.setDefaultCarrington();
        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        config.setDefaultStringentParameters();

        File adjMismatchesPlot = new File(plotDirectory + File.separator + "adj_mismatches_plot.csv");
        File alignmentScorePlot = new File(plotDirectory + File.separator + "alignment_score_plot.csv");
        File coreAdjMismatchesPlot = new File(plotDirectory + File.separator + "core_adj_mismatches_plot.csv");
        File coreGUPlot = new File(plotDirectory + File.separator + "core_GU_pairs_plot.csv");
        File coreMismatchesPlot = new File(plotDirectory + File.separator + "core_mismatches_plot.csv");
        File guPairPlot = new File(plotDirectory + File.separator + "gu_pair_plot.csv");
        File mismatchPlot = new File(plotDirectory + File.separator + "mismatch_plot.csv");
        File mfeRatioPlot = new File(plotDirectory + File.separator + "mfe_ratio_plot.csv");
        File positionPropertyPlot = new File(plotDirectory + File.separator + "position_property_plot.csv");

        //update the values of the RuleSet based on plot data
        int maxAdjMismatches = getIntParameterValue(adjMismatchesPlot, percentRetained);
        rs.setMaxAdjacentMM(maxAdjMismatches);

        double alignmentScore = getDoubleParameterValue(alignmentScorePlot, percentRetained);
        rs.setMaxScore(alignmentScore);

        int coreAdjMismatches = getIntParameterValue(coreAdjMismatchesPlot, percentRetained);
        rs.setMaxAdjacentMMCoreRegion(coreAdjMismatches);

        int coreGU = getIntParameterValue(coreGUPlot, percentRetained);
        //this needs adding into the AlignmentPath clas

        int coreMismatches = getIntParameterValue(coreMismatchesPlot, percentRetained);
        rs.setMaxMMCoreRegion(coreMismatches);

        int guPair = getIntParameterValue(guPairPlot, percentRetained);
        rs.setMaxGUWobbles(guPair);

        int mismatch = getIntParameterValue(mismatchPlot, percentRetained);
        rs.setMaxMM(mismatch);

        //This method is not correct. It goes low to high rather than high to low
        double mfeRatio = getMFEParameterValue(mfeRatioPlot, percentRetained);
        mfeRatio = Double.parseDouble(String.format("%.2f", mfeRatio - 0.005));

        config.setFilterCutoff(mfeRatio);

        //we might need to check for allowing mismatch at pos 10 and 11 too 
        //this can be based on the position property file
        rs.setAllowMM10(hasMismatch(positionPropertyPlot, 10));
        rs.setAllowMM11(hasMismatch(positionPropertyPlot, 11));

        config.setAllowedCategories(2, false);
        config.setAllowedCategories(3, false);
        config.setAllowedCategories(4, false);

        config.setMinFragmentLenth(19);

        //System.out.println(rs);
        //System.out.println(config);
        try {

            if (!outputDir.exists()) {
                System.out.println("Output does not exist.... Creating...");
                outputDir.mkdirs();
            }

            BufferedWriter rulesWriter = new BufferedWriter(new FileWriter(new File(outputDir + File.separator + "targeting_rules.txt")));
            BufferedWriter configWriter = new BufferedWriter(new FileWriter(new File(outputDir + File.separator + "parameters.txt")));

            rulesWriter.write(rs.toString());
            configWriter.write(config.toString());

            rulesWriter.close();
            configWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean hasMismatch(File positionPropertyPlot, int position) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(positionPropertyPlot));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {

                int pos = Integer.parseInt(line.split(",")[0]);
                if (pos == position) {
                    return Double.parseDouble(line.split(",")[1]) > 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    static double getMFEParameterValue(File plotFile, double percentRetained) {

        int totalTargets = 0;
        double value = 0.0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(plotFile));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                totalTargets++;
            }

            br = new BufferedReader(new FileReader(plotFile));
            br.readLine();
            int num = 0;
            while ((line = br.readLine()) != null) {
                //check if cumulative interactions >= percentRained of total
                //and return the parameter
                value = Double.parseDouble(line.split(",")[0]);
                num++;

                if ((((double) num / (double) totalTargets)) >= (1.0 - percentRetained)) {
                    return value;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0.0;
    }

    static double getDoubleParameterValue(File plotFile, double percentRetained) {
        int totalTargets = 0;
        double value = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(plotFile));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                totalTargets = Integer.parseInt(line.split(",")[1]);
            }

            br = new BufferedReader(new FileReader(plotFile));
            br.readLine();
            while ((line = br.readLine()) != null) {
                //check if cumulative interactions >= percentRained of total
                //and return the parameter
                value = Double.parseDouble(line.split(",")[0]);
                int num = Integer.parseInt(line.split(",")[1]);

                if ((((double) num / (double) totalTargets)) >= percentRetained) {
                    return value;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    static int getIntParameterValue(File plotFile, double percentRetained) {
        int totalTargets = 0;
        int value = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(plotFile));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                totalTargets = Integer.parseInt(line.split(",")[1]);
            }

            br = new BufferedReader(new FileReader(plotFile));
            br.readLine();
            while ((line = br.readLine()) != null) {
                //check if cumulative interactions >= percentRained of total
                //and return the parameter
                value = Integer.parseInt(line.split(",")[0]);
                int num = Integer.parseInt(line.split(",")[1]);

                if ((((double) num / (double) totalTargets)) >= percentRetained) {
                    return value;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private void comparePAREsnip2ResultsAgainstMicroRNAs() {

        try {

            HashSet<String> writtenMiRNAs = new HashSet();
            File miRNAsWithTargets = new File(input.getOutputDir().getAbsolutePath()
                    + File.separator + "predicted_miRNAs_with_targets.fa");

            BufferedWriter miRNAWriter = new BufferedWriter(new FileWriter(miRNAsWithTargets));

            alignmentPredictionsFile = new File(input.getOutputDir().getAbsolutePath()
                    + File.separator + "miRNA_alignments.csv");

            BufferedWriter bw = new BufferedWriter(new FileWriter(alignmentPredictionsFile));

            BufferedReader br = new BufferedReader(new FileReader(new File(PAREsnip2OutputDir.getAbsolutePath()
                    + File.separator + candidateMiRNAs.getName().split("\\.")[0] + "_"
                    + input.getDegradome().getName().split("\\.")[0] + ".csv")));

            String headerLine = br.readLine();
            int splitLength = headerLine.split(",").length;
            String line;
            bw.write(headerLine);
            bw.newLine();

            int novelNum = 1;

            while ((line = br.readLine()) != null) {
                line += "\n" + br.readLine() + "\n";
                line += br.readLine();

                String smallRNA;
                Integer abundance;
                String alignment_splits[];
                Integer fragAbundance;

                String splits[] = line.split(",");

                if (splits.length == splitLength) {
                    alignment_splits = splits[10].split("'");
                    smallRNA = alignment_splits[1].trim().replace("-", "");
                    abundance = Integer.parseInt(splits[8]);
                    fragAbundance = Integer.parseInt(splits[5]);
                } else {

                    //If there exists a comma in the gene annotaton
                    //we need to concatenate these splits together
                    String geneName = "";

                    int missing = splits.length - splitLength;

                    for (int i = 0; i <= missing; i++) {
                        geneName += splits[i + 2] + ",";
                    }
                    geneName = geneName.replace("\"", "");
                    //remove trailing comma
                    geneName = geneName.substring(0, geneName.length() - 1);
                    alignment_splits = splits[10 + missing].split("'");
                    smallRNA = alignment_splits[1 + missing].trim().replace("-", "");
                    abundance = Integer.parseInt(splits[8 + missing]);
                    fragAbundance = Integer.parseInt(splits[5 + missing]);

                }

                if (predicted_miRNAs.containsKey(smallRNA)) {
                    if (fragAbundance >= config.getMinTagAbundance()) {

                        if (miRNA_Annotations.containsKey(smallRNA)) {
                            line = line.replace(">" + smallRNA, miRNA_Annotations.get(smallRNA));
                        }

                        bw.write(line);
                        bw.write("\n");

                        if (!writtenMiRNAs.contains(smallRNA)) {
                            writtenMiRNAs.add(smallRNA);

                            if (miRNA_Annotations.containsKey(smallRNA)) {
                                miRNAWriter.write(miRNA_Annotations.get(smallRNA) + "(" + abundance + ")");
                                miRNAWriter.write("\n");
                                miRNAWriter.write(smallRNA);
                                miRNAWriter.write("\n");
                            } else {
                                if (miRNAIsoMirs.containsKey(smallRNA)) {

                                    StringBuilder sb = new StringBuilder(">");

                                    for (String s : miRNAIsoMirs.get(smallRNA)) {
                                        sb.append(s).append("_");
                                    }

                                    sb.append("possible_isoMir");
                                    miRNAWriter.write(sb.toString() + "(" + abundance + ")");
                                    miRNAWriter.write("\n");
                                    miRNAWriter.write(smallRNA);
                                    miRNAWriter.write("\n");

                                } else {
                                    miRNAWriter.write(">novel_miRNA_" + novelNum++ + "(" + abundance + ")");
                                    miRNAWriter.write("\n");
                                    miRNAWriter.write(smallRNA);
                                    miRNAWriter.write("\n");
                                }
                            }

                        }

                    }
                }

            }

            miRNAWriter.flush();
            miRNAWriter.close();
            bw.flush();
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void combineKnownMicroRNAs() {

        int minAbundance = config.getMinSmallRNAAbundance();

        if (input.getUsersMiRNAs() != null) {
            System.out.println("before combining user-specific miRNAs size: " + predicted_miRNAs.size());
            for (String s : usersMiRNAs.keySet()) {
                if (!predicted_miRNAs.containsKey(s)) {
                    if (miRPlantCandidates.containsKey(s)) {
                        if (smallRNAs.get(s) >= minAbundance) {
                            predicted_miRNAs.put(s, smallRNAs.get(s));
                        }
                    }
                }
            }
            System.out.println("After combining user-specific miRNAs size: " + predicted_miRNAs.size());
        }

        smallRNAs.clear();

    }

    private void runMiRCat2Algorithm() {
        System.out.println("------- Running miRCat2 to find potential miRNAs -------");
        try {
            MiRCat2Main.verbose = false;
            new MiRCat2Main(writeJsonFile());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("------- Finished running miRCat2 -------");
    }

    private void processMiRPlantResults() {

        System.out.println("before mirplant size: " + predicted_miRNAs.size());
        try {
            File miRPlantResultsCopy = new File(miRPlantOutputDir.getAbsolutePath() + File.separator + "miRPlant_results.txt");
            if (miRPlantResultsCopy.exists()) {
                miRPlantResultsCopy.delete();
            }
            Files.copy(miRPlantResults.toPath(), miRPlantResultsCopy.toPath());
            miRPlantResults.delete();
            miRPlantResults = miRPlantResultsCopy;

            BufferedReader br = new BufferedReader(new FileReader(miRPlantResults));
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String seq = splits[9].toUpperCase();
                Integer abundance = Integer.parseInt(splits[5]);
                if (!predicted_miRNAs.containsKey(seq)) {
                    predicted_miRNAs.put(seq, abundance);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("after mirplant size: " + predicted_miRNAs.size());
    }

    private void runMiRPlant() {

        System.out.println("Running miRPlant to look for potential miRNAs with predicted targets...");

        miRPlantParameters.setCandidates(candidateMiRNAs);

        miRPlantRunner runner = new miRPlantRunner(miRPlantParameters);
        runner.process();
        miRPlantResults = runner.getResults();

        System.out.println("Finished running miRPlant...");

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
                        comment = line.split(">")[1].split(" ")[0];
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

    private void constructSmallRNASet() {
        Set<String> seen = new HashSet();
        miRPlantCandidates = new HashMap<>();

        try {

            File PAREsnip2Results = new File(PAREsnip2OutputDir.getAbsolutePath()
                    + File.separator + candidateMiRNAs.getName().split("\\.")[0] + "_"
                    + input.getDegradome().getName().split("\\.")[0] + ".csv");

            BufferedReader br = new BufferedReader(new FileReader(PAREsnip2Results));

            String headerLine = br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                line += "\n" + br.readLine() + "\n";
                line += br.readLine();

                String smallRNA;
                Integer abundance;
                String alignment_splits[];

                String splits[] = line.split(",");

                if (splits.length == 16) {
                    alignment_splits = splits[10].split("'");
                    smallRNA = alignment_splits[1].trim().replace("-", "");
                    abundance = Integer.parseInt(splits[8]);
                } else {

                    //If there exists a comma in the gene annotaton
                    //we need to concatenate these splits together
                    String geneName = "";

                    int missing = splits.length - 16;

                    for (int i = 0; i <= missing; i++) {
                        geneName += splits[i + 2] + ",";
                    }
                    geneName = geneName.replace("\"", "");
                    //remove trailing comma
                    geneName = geneName.substring(0, geneName.length() - 1);
                    alignment_splits = splits[10 + missing].split("'");
                    smallRNA = alignment_splits[1 + missing].trim().replace("-", "");
                    abundance = Integer.parseInt(splits[8 + missing]);

                }

                if (!seen.contains(smallRNA)) {
                    seen.add(smallRNA);
                    miRPlantCandidates.put(smallRNA, abundance);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void runPAREsnip2() {

        try {
            RuleSet.reset();
            //Set up PAREsnip2 targeting rules and configuration
            //This seems silly because RuleSet is a singleton class
            //however, I never designed it to be used outside of PAREsnip2
            //So we'll have to just go with it for now
            if (input.getPAREsnip2TargetingRules() != null) {
                RuleSet.getRuleSet().loadRules(input.getPAREsnip2TargetingRules());
            } else {
                RuleSet.getRuleSet().setParameterSearchRules();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Paresnip2Configuration PAREsnip2Config = Paresnip2Configuration.getInstance();
        PAREsnip2Config.setConfig(config);
        PAREsnip2Config.setVerbose(false);

        System.out.println("--------- Running PAREsnip2 to look for potential cleavage events ---------");

        PAREsnip2Input = Paresnip2InputFiles.getInstance();
        PAREsnip2Input.reset();

        //set up input
        PAREsnip2Input.addDegradomeReplicate(input.getDegradome());
        PAREsnip2Input.addSmallRNAReplicate(candidateMiRNAs);
        PAREsnip2Input.addTranscriptome(input.getTranscriptome());
        PAREsnip2Input.setOuputDirectory(PAREsnip2OutputDir);

        //run paresnip2
        uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine engine = new uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine();

        System.out.println("--------- PAREsnip2 completed sucessfully ---------");
    }

    private void findCandidateSequences() {

        try {

            miRNAIsoMirs = new HashMap<>();
            BufferedWriter bw = new BufferedWriter(new FileWriter(candidateMiRNAsNR));
            BufferedReader br = new BufferedReader(new FileReader(patmanOutput));
            HashMap<String, Integer> uniques = new HashMap();
            String line;

            while ((line = br.readLine()) != null) {
                String splits[] = line.split("\t");
                String seq = splits[1].split("\\(")[0].trim();
                Integer abundance = Integer.parseInt(splits[1].split("\\(")[1].split("\\)")[0]);
                Integer numMM = Integer.parseInt(splits[5]);
                String alignedMiRNA = splits[0];

                if (numMM > 0) {
                    if (!miRNAIsoMirs.containsKey(seq)) {
                        miRNAIsoMirs.put(seq, new ArrayList<String>());
                    }

                    if (!miRNAIsoMirs.get(seq).contains(alignedMiRNA)) {
                        miRNAIsoMirs.get(seq).add(alignedMiRNA);
                    }
                }

                if (!uniques.containsKey(seq)) {
                    uniques.put(seq, abundance);
                }
            }

            //mircat2 results
            File mirCatResults = new File(input.getOutputDir().getAbsoluteFile() + File.separator + "mircat2" + File.separator + sRNALibraryNR.getName().substring(0, sRNALibraryNR.getName().lastIndexOf(".")) + "_output.csv");

            //We could process the mircat2 results here
            br = new BufferedReader(new FileReader(mirCatResults));
            //read header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                String splits[] = line.split(",");
                if (!uniques.containsKey(splits[2])) {

                    uniques.put(splits[2], Integer.parseInt(splits[3]));
                    predicted_miRNAs.put(splits[2], Integer.parseInt(splits[3]));

                }
                if (!splits[15].equals("N/A")) {
                    if (!uniques.containsKey(splits[15])) {
                        uniques.put(splits[15], Integer.parseInt(splits[16]));
                        predicted_miRNAs.put(splits[15], Integer.parseInt(splits[16]));
                    }
                }

            }

            for (String s : uniques.keySet()) {
                if (uniques.get(s) >= 5) {
                    bw.write(">" + s + "(" + uniques.get(s) + ")");
                    bw.newLine();
                    bw.write(s);
                    bw.newLine();
                    bw.flush();
                }

            }

            bw = new BufferedWriter(new FileWriter(candidateMiRNAs));
            for (String s : uniques.keySet()) {
                if (uniques.get(s) >= 5) {
                    for (int i = 0; i < uniques.get(s); i++) {
                        bw.write(">" + s);
                        bw.newLine();
                        bw.write(s);
                        bw.newLine();
                    }
                    bw.flush();
                }

            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void alignMicroRNAs(File miRNAs, File sRNAs) {

        System.out.println("Aligning small RNAs to all known plant miRNAs...");
        String alignedFile = input.getOutputDir() + File.separator + miRNAs.getName() + ".patman";

        patmanOutput = new File(alignedFile);

        PatmanParams newP_Params = new PatmanParams();
        newP_Params.setMaxGaps(0);
        newP_Params.setMaxMismatches(2);
        newP_Params.setPreProcess(false);
        newP_Params.setPostProcess(false);
        newP_Params.setMakeNR(false);
        newP_Params.setPositiveStrandOnly(true);

        File tmpDir = new File(input.getOutputDir() + File.separator + "tmp");

        try {
            tmpDir.mkdir();
            PatmanRunner runner = new PatmanRunner(sRNAs, miRNAs,
                    patmanOutput, tmpDir, newP_Params);
            runner.setUsingDatabase(false);

            Thread myThread = new Thread(runner);

            myThread.start();
            myThread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpDir.deleteOnExit();
        patmanOutput.deleteOnExit();
    }

    private File makeNonRedundant(File seqs) {

        String line;
        HashMap<String, Integer> checkedSeqs = new HashMap();
        HashSet<String> lowComplexSeqs = new HashSet();
        smallRNAs = new HashMap<>();

        int min = config.getMinSmallRNALength();
        int max = config.getMaxSmallRNALength();

        try (BufferedReader br = new BufferedReader(new FileReader(seqs))) {

            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (!line.startsWith(">")) {
                        if (line.length() <= max && line.length() >= min) {

                            boolean lowComplexity = true;
                            boolean isInvalid = true;

                            if (checkedSeqs.containsKey(line)) {
                                checkedSeqs.put(line, checkedSeqs.get(line) + 1);
                                lowComplexity = false;
                            } else if (!lowComplexSeqs.contains(line)) {
                                isInvalid = isInvalid(line);
                                if (!isInvalid) {
                                    lowComplexity = isLowComplexity(line);
                                }
                                if (!lowComplexity && !isInvalid) {
                                    checkedSeqs.put(line, 1);
                                } else {
                                    lowComplexSeqs.add(line);
                                }
                            }

                            if (!smallRNAs.containsKey(line) && !lowComplexity) {
                                smallRNAs.put(line, 1);
                            } else if (smallRNAs.containsKey(line)) {
                                smallRNAs.put(line, smallRNAs.get(line) + 1);
                            }
                        }

                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        int minAbundance = config.getMinSmallRNAAbundance();

        File seqsNonRedundant = new File(input.getOutputDir().getAbsolutePath() + File.separator + seqs.getName().split("\\.")[0] + ".NR.fa");
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(seqsNonRedundant));
            for (String seq : checkedSeqs.keySet()) {
                int abundance = checkedSeqs.get(seq);
                if (abundance >= minAbundance) {
                    br.write(">" + seq + " (" + abundance + ")");
                    br.newLine();
                    br.write(seq);
                    br.newLine();

                    br.flush();
                }
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        seqsNonRedundant.deleteOnExit();
        System.out.println("Finished making: " + seqs.getName() + " non-redundant.");
        return seqsNonRedundant;

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

    /**
     * Method to check sequence quality
     *
     * @param seq the sequence to check
     * @return true if the sequence contains any ambiguous bases
     */
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

    private File writeJsonFile() throws IOException {

        File f = new File(input.getOutputDir().getAbsolutePath() + File.separator + "mircat2" + File.separator + "micat2.json");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        //miRCat2Results = mapToFastaNR();

        bw.write("{");
        bw.newLine();
        bw.write("\"srna_files\": [");
        bw.write("{");
        bw.newLine();

        String sRNAPath = sRNALibraryNR.getAbsolutePath();
        String genomePath = input.getGenomeFile().getAbsolutePath();
        String outputPath = input.getOutputDir().getAbsolutePath() + File.separator + "mircat2";
        //This may need to be put into the PAREameters input file
        File paramFile = new File(Tools.DEFAULT_PARAMS_DIR + File.separator + "default_miRCat2_plant_params.cfg");
        String mircat2Params = paramFile.getAbsolutePath();

        if (AppUtils.INSTANCE.getArchitecture().isWindows()) {
            sRNAPath = sRNAPath.replace("\\", "\\\\");
            genomePath = genomePath.replace("\\", "\\\\");
            outputPath = outputPath.replace("\\", "\\\\");
            mircat2Params = mircat2Params.replace("\\", "\\\\");
        }

        bw.write("\"srna_filename\": \"" + sRNAPath + "\"");
        bw.newLine();
        bw.write("}");
        bw.newLine();
        bw.write("],");
        bw.newLine();

        bw.write("\"genome_filename\": \"" + genomePath + "\",");
        bw.newLine();
        bw.write("\"miRCAT2_Output_Dir\": \"" + outputPath + "\",");
        bw.newLine();
        bw.write("\"miRCAT2_params\": \"" + mircat2Params + "\"");
        bw.newLine();
        bw.write("}");

        bw.flush();
        return f;
    }

    private void processUsersMiRNAs() {

        usersMiRNAs = new HashMap();

        if (input.getUsersMiRNAs() != null) {
            try {

                //Store all plant miRNA annotations
                File miRNAFile = input.getUsersMiRNAs();

                BufferedReader br = new BufferedReader(new FileReader(miRNAFile));
                HashMap<String, List<String>> nonRedunantMiRNAs = new HashMap();

                String line;

                String comment = "";
                while ((line = br.readLine()) != null) {

                    if (line.startsWith(">")) {
                        comment = line.split(">")[1].split(" ")[0];
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
                    usersMiRNAs.put(miRNA, comment);
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

    }

}
