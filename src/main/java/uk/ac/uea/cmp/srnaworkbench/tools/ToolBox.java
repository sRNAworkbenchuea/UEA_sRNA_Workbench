/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.*;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.*;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.FiRePat;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput.FiRePatConfiguration;
import uk.ac.uea.cmp.srnaworkbench.tools.firepat.fileinput.FiRePatInputFiles;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.*;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.*;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2InputFiles;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoParams;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.tasi.*;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.workflow.PreconfiguredWorkflows;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.MiRCat2Main;
import uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction.NATsiRNA_PredictionConfiguration;
import uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction.NATsiRNA_PredictionEngine;
import uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction.NATsiRNA_PredictionInput;
import uk.ac.uea.cmp.srnaworkbench.tools.pareameters.PAREametersConfiguration;
import uk.ac.uea.cmp.srnaworkbench.tools.pareameters.PAREametersEngine;
import uk.ac.uea.cmp.srnaworkbench.tools.pareameters.PAREametersInput;
import uk.ac.uea.cmp.srnaworkbench.tools.pareameters.PAREametersRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.tplot.TargetPlotConfiguration;
import uk.ac.uea.cmp.srnaworkbench.tools.tplot.TargetPlotEngine;

/**
 *
 * @author prb07qmu
 */
public enum ToolBox {
    // The unknown tool (default)
    //
// The unknown tool (default)
    //
    UNKNOWN_TOOL("unknown") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            throw new IllegalArgumentException("Error: The tool name is unrecognised.");
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + "<toolname> <options>");
            System.out.println("where");

            ArrayList<String> toolNames = new ArrayList<String>();

            for (ToolBox tool : ToolBox.values()) {
                if (tool == UNKNOWN_TOOL || tool == DEBUG_TOOL) {
                    continue;
                }

                toolNames.add(tool.getName());
            }

            System.out.println("  <toolname> is one of " + toolNames);
        }
    },
    //
    // The adaptor removal tool
    //
    ADAPTOR_TOOL("adaptor") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            boolean overwriteOutput = argmap.containsKey("f");

            File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
            File outFile = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

            AdaptorRemoverParams params = (paramsFile == null ? new AdaptorRemoverParams() : AdaptorRemoverParams.load(new AdaptorRemoverParams(), paramsFile));

            ToolBox.printParameters(params);

            AdaptorRemover ar = new AdaptorRemover(srnaFile, outFile, params);
            ar.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file");
        }
    },
    //
    // The debug tool
    //
    DEBUG_TOOL("debug") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            // An aid to debugging the command line - command line options will be listed
            // The option 'sleep' causes the thread to sleep for a few seconds

            System.out.println("Debugging command line. Option and value will be listed.");

            //uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots.TPlotFrame.main( null );
            for (String key : argmap.keySet()) {
                String value = argmap.get(key);
                System.out.println("Option: " + key + ", Value: " + value);
            }

            if (argmap.containsKey("sleep")) {
                final int SECONDS_TO_SLEEP = 20;
                System.out.println("Workbench sleeping for " + SECONDS_TO_SLEEP + " seconds...");

                try {
                    TimeUnit.SECONDS.sleep(SECONDS_TO_SLEEP);
                } catch (InterruptedException ex) {
                    // shhh
                }
            }
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [ -sleep [ list-of-parameters ] ]");
        }
    },
    //
    // The PARESnip tool
    //
    PARESNIP_TOOL("paresnip") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            // Check files according to the order given in the usage
            boolean overwriteOutput = argmap.containsKey("f");

            // Check mandatory files first...
            File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
            File degradomeFile = ToolBox.checkInputFile("deg_file", argmap.get("deg_file"), true);
            File transcriptomeFile = ToolBox.checkInputFile("tran_file", argmap.get("tran_file"), true);
            File outFile = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);

            // Now check the optional files...
            File genomeFile = ToolBox.checkInputFile("genome_file", argmap.get("genome_file"), false);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

            Degradome d = new Degradome(srnaFile, degradomeFile, transcriptomeFile, genomeFile, outFile, paramsFile);
            d.runProcedure();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -deg_file degradome-file-path  -tran_file transcriptome-file-path  -out_file output-file-path  [ -genome_file genome-file-path  -params params-file-path ]\n -f = Force overwriting of output file");
        }
    },
    //
    // The PARESnip tool
    //
    CV_TOOL("cv") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {

            PAREametersRunner.main(new String[0]);

        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -deg_file degradome-file-path  -tran_file transcriptome-file-path  -out_file output-file-path  [ -genome_file genome-file-path  -params params-file-path ]\n -f = Force overwriting of output file");
        }
    },
    // The PARESnip2 tool
    //
    PARESNIP2_TOOL("paresnip2") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException, NumberFormatException {

            System.out.println("");

            Paresnip2InputFiles input = Paresnip2InputFiles.getInstance();

            if (argmap.containsKey("h") || argmap.containsKey("help") || (argmap.containsKey("tool") && argmap.size() == 1)) {
                printUsage();
                System.exit(1);
            }

            if (!argmap.containsKey("parameters")) {
                System.out.println("Parameter file has not been specified... Using default stringent configuration and transcriptome as reference...");
                Paresnip2Configuration.getInstance().setDefaultStringentParameters();
            } else {

                try {
                    Paresnip2Configuration.getInstance().loadConfig(new File(argmap.get("parameters")));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }

            if (!argmap.containsKey("targeting_rules")) {
                System.out.println("Targeting rules file has not been specified... Using default Allen et al rules...");
                RuleSet.getRuleSet().setDefaultAllen();
            } else {
                RuleSet.getRuleSet().loadRules(new File(argmap.get("targeting_rules")));
            }

            if (!argmap.containsKey("srna_files")) {
                System.out.println("ERROR - Please input at least one small RNA library using the parameter -srna_files path-to-file-1 [path-to-file-2] [...]");
                System.exit(1);
            } else {
                String files = argmap.get("srna_files");
                String[] fileLoc = files.split(" ");
                for (String s : fileLoc) {
                    File f = new File(s);

                    if (!checkFileIsOK(f)) {
                        throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "srna_files", s));
                    }

                    input.addSmallRNAReplicate(f);
                }
            }

            if (!argmap.containsKey("pare_files")) {
                System.out.println("ERROR - Please input at least one PARE library using the parameter -pare_files path-to-file-1 [path-to-file-2] [...]");
                System.exit(1);
            } else {
                String files = argmap.get("pare_files");
                String[] fileLoc = files.split(" ");
                for (String s : fileLoc) {
                    File f = new File(s);

                    if (!checkFileIsOK(f)) {
                        throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "pare_files", s));
                    }

                    input.addDegradomeReplicate(f);
                }
            }

            if (!argmap.containsKey("reference")) {
                System.out.println("ERROR - Please input a reference file (either genome or transcriptome) using the parameter -refernce path-to-file");
                System.exit(1);
            }

            if (Paresnip2Configuration.getInstance().isUsingGenomeGFF() && !argmap.containsKey("gff3")) {
                System.out.println("ERROR - You configuration is set to use a genome + corresponding gff3 file but the -gff3 path-to-file parameter is missing");
                System.exit(1);
            }

            if (Paresnip2Configuration.getInstance().isUsingGenomeGFF()) {
                File f = new File(argmap.get("gff3"));

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "gff3", f.getName()));
                }

                input.addGFF(f);

                f = new File(argmap.get("reference"));

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "reference", f.getName()));
                }
                input.addGenome(f);
            } else {

                File f = new File(argmap.get("reference"));

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "reference", f.getName()));
                }

                input.addTranscriptome(f);
            }

            if (Paresnip2Configuration.getInstance().isAlignSmallRNAsToGenome()) {
                File genome = new File(argmap.get("genome"));

                if (!checkFileIsOK(genome)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "reference", genome.getName()));
                }

                input.addGenome(genome);
            }

            if (!argmap.containsKey("output_dir")) {
                System.out.println("Output directory has not been specified... Using current directory: " + System.getProperty("user.dir"));
                Paresnip2InputFiles.getInstance().setOuputDirectory(new File(System.getProperty("user.dir")));
            } else {
                input.setOuputDirectory(new File(argmap.get("output_dir")));
            }

            Engine engine = new Engine();
            System.exit(0);

        }

        @Override
        public void printUsage() {
            System.out.println("Usage:\n  java [-XmxNg] -jar /path/to/Workbench.jar -tool " + getName() + " -parameters /path/to/parameter/file -targeting_rules /path/to/targeting_rules/file -srna_files /path/to/srna/file1 [/path/to/srna/file2] [...] -pare_files /path/to/pare/file1 [/path/to/pare/file2] [...] -reference /path/to/reference/file -output_dir /path/to/output/directory [-genome /path/to/genome/file] [-gff3 /path/to/gff3/file]");
            System.out.println("Where: \n -XmxNg (optional but recommended) gives N GB of memory to the Workbench process");
        }
    },
    // The PARESnip2 tool
    //
    NATSI_TOOL("natpare") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException, NumberFormatException {

            NATsiRNA_PredictionConfiguration config = NATsiRNA_PredictionConfiguration.getInstance();
            NATsiRNA_PredictionInput input = NATsiRNA_PredictionInput.getInstance();

            if (argmap.containsKey("h") || argmap.containsKey("help") || (argmap.containsKey("tool") && argmap.size() == 1)) {
                printUsage();
                System.exit(1);
            }

            File inputJson = null;

            if (!argmap.containsKey("config")) {
                System.out.println("ERROR - Please input the configuration json file using the parameter -config path-to-file");
                System.exit(1);
            } else {
                String filepath = argmap.get("config");
                File f = new File(filepath);
                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "config", filepath));
                } else {
                    inputJson = f;
                }
            }

            File parameters = null;

            if (!argmap.containsKey("parameters")) {
                System.out.println("No parameter file included as input. Using default parameters.");
            } else {
                String filepath = argmap.get("parameters");
                File f = new File(filepath);
                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "parameters", filepath));
                } else {
                    parameters = f;
                }
            }

            input.loadFromJson(inputJson);

            //if parameters are not present then we just use default...
            if (parameters != null) {
                config.loadFromFile(parameters);
            }
            
            NATsiRNA_PredictionEngine engine = new NATsiRNA_PredictionEngine();
            
            engine.process();

            System.exit(0);

        }

        @Override
        public void printUsage() {
            System.out.println("Usage:\n  java [-XmxNg] -jar /path/to/Workbench.jar -tool " + getName() + " -config path-to-file [-parameters path-to-file]");
            System.out.println("Where: \n -XmxNg (optional but recommended) gives N GB of memory to the Workbench process");
        }
    },
    // The PARESnip2 tool
    //
    TPLOT_TOOL("t-plot") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException, NumberFormatException {
            System.out.println("");

            TargetPlotConfiguration config = TargetPlotConfiguration.getInstance();

            File degradomeFile, transcriptFile, PAREsnip2Results, outputDir;

            degradomeFile = transcriptFile = PAREsnip2Results = outputDir = null;

            if (argmap.containsKey("h") || argmap.containsKey("help") || (argmap.containsKey("tool") && argmap.size() == 1)) {
                printUsage();
                System.exit(1);
            }

            if (!argmap.containsKey("pare_file")) {
                System.out.println("ERROR - Please input the PARE library used for PAREsnip2 analysis -pare_file path-to-file");
                System.exit(1);
            } else {
                String file = argmap.get("pare_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "pare_file", file));
                }

                degradomeFile = f;

            }

            if (!argmap.containsKey("reference")) {
                System.out.println("ERROR - Please input the reference transcriptome file using the parameter -refernce path-to-file");
                System.exit(1);
            } else {
                String file = argmap.get("reference");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "pare_file", file));
                }

                transcriptFile = f;
            }

            if (!argmap.containsKey("results")) {
                System.out.println("No PAREsnip2 results provided... creating T-plots without sRNA");
            } else {
                String file = argmap.get("results");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "pare_file", file));
                }

                PAREsnip2Results = f;
            }

            if (!argmap.containsKey("output")) {
                System.out.println("No output dir provided... using PAREsnip2 results directory");
            } else {
                String file = argmap.get("output");

                File f = new File(file);

                if (f.exists() && !f.isDirectory()) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', exists but is not a directory.", "output", file));
                }

                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', can not be created as a directory.", "output", file));
                    }
                }

                outputDir = f;
            }

            if (argmap.containsKey("min")) {
                Integer minLength = Integer.parseInt(argmap.get("min"));
                config.setMinTagLength(minLength);
            }

            if (argmap.containsKey("max")) {
                Integer maxLength = Integer.parseInt(argmap.get("max"));
                config.setMaxTagLength(maxLength);
            }

            if (argmap.containsKey("filter")) {

                Boolean b = Boolean.parseBoolean(argmap.get("filter"));
                config.setLowComplexityFilter(b);

            }
            TargetPlotEngine engine;
            if (outputDir == null) {
                engine = new TargetPlotEngine(PAREsnip2Results, degradomeFile, transcriptFile);
            } else {
                engine = new TargetPlotEngine(PAREsnip2Results, degradomeFile, transcriptFile, outputDir);
            }
            engine.process();
            System.exit(0);
        }

        @Override
        public void printUsage() {
            System.out.println("Usage:\n  java [-XmxNg] -jar /path/to/Workbench.jar -tool " + getName() + " -pare_file path/to/pare/file -reference path/to/reference -results path/to/PAREsnip2/results [-min] minTagLength [-max] maxTagLength [-filter] true/false");
        }
    },
    // The PAREameters tool
    //
    PAREAMETERS_TOOL("pareameters") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException, NumberFormatException {

            //Which files can we take from the user and which files should be
            //included with the bench?
            /**
             * Included: hairpin, known miRNA GFF, mature miRNAs
             *
             * User supplied: degradome, sRNAs, transcriptome, speciesName,
             * miRNAs
             */
            System.out.println("");

            PAREametersInput input = PAREametersInput.getInstance();
            PAREametersConfiguration config = PAREametersConfiguration.getInstance();

            File degradomeFile, transcriptFile, genomeFile;
            File hairpinFile, knownMiRNAGFF_file, matureMiRNAsFile;
            File smallRNAFile, usersMiRNAs;
            String speciesName = null;

            degradomeFile = smallRNAFile = genomeFile = usersMiRNAs = transcriptFile = null;

            //All these from the data directory
            hairpinFile = new File(Tools.DATA_DIR + File.separator + "hairpin.fa");
            knownMiRNAGFF_file = new File(Tools.DATA_DIR + File.separator + "knownMiR.gff3");
            matureMiRNAsFile = new File(Tools.DATA_DIR + File.separator + "mature.fa");

            if (argmap.containsKey("h") || argmap.containsKey("help") || (argmap.containsKey("tool") && argmap.size() == 1)) {
                printUsage();
                System.exit(1);
            }

            if (!argmap.containsKey("pare_file")) {
                System.out.println("ERROR - Please input the PARE library used for PAREameters analysis e.g. -pare_file path/to/file");
                System.exit(1);
            } else {
                String file = argmap.get("pare_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "pare_file", file));
                }

                degradomeFile = f;

            }

            if (!argmap.containsKey("transcript_file")) {
                System.out.println("ERROR - Please input the transcript sequences to be used for PAREameters analysis -transcript_file path/to/file");
                System.exit(1);
            } else {
                String file = argmap.get("transcript_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "transcript_file", file));
                }

                transcriptFile = f;

            }
//
            if (!argmap.containsKey("genome_file")) {
                System.out.println("ERROR - Please input the genome file for PAREameters analysis -genome_file path/to/file");
                System.exit(1);
            } else {
                String file = argmap.get("genome_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "genome_file", file));
                }

                genomeFile = f;

            }

            if (!argmap.containsKey("srna_file")) {
                System.out.println("ERROR - Please input the small RNA file for PAREameters analysis -srna_file path/to/file");
                System.exit(1);
            } else {
                String file = argmap.get("srna_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "srna_file", file));
                }

                smallRNAFile = f;

            }

            if (!argmap.containsKey("output_dir")) {
                System.out.println("Output directory has not been specified... Using current directory: " + System.getProperty("user.dir"));
                input.setOutputDir(new File(System.getProperty("user.dir")));
            } else {
                input.setOutputDir(new File(argmap.get("output_dir")));
            }

            if (argmap.containsKey("mirna_file")) {
                String file = argmap.get("mirna_file");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "mirna_file", file));
                }

                usersMiRNAs = f;

                input.setUsersMiRNAs(usersMiRNAs);
            }

            if (!argmap.containsKey("species_name")) {
                System.out.println("ERROR - Please input the species name used for pareameters analysis -pare_file species_name");
                System.exit(1);
            } else {
                speciesName = argmap.get("species_name");

            }

            if (argmap.containsKey("config")) {
                File parameters = new File(argmap.get("config"));

                if (!checkFileIsOK(parameters)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "config", config));
                }

                config.loadFromFile(parameters);
            }

            if (argmap.containsKey("paresnip2_rules")) {
                String file = argmap.get("paresnip2_rules");

                File f = new File(file);

                if (!checkFileIsOK(f)) {
                    throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", "paresnip2_rules", file));
                }

                input.setPAREsnip2TargetingRules(f);
            }

            input.setDegradome(degradomeFile);
            input.setGenomeFile(genomeFile);
            input.setHairpinSeqs(hairpinFile);
            input.setKnownMiRGFF(knownMiRNAGFF_file);
            input.setMatureMiRNAs(matureMiRNAsFile);
            input.setOrganismName(speciesName);

            input.setSmallRNAs(smallRNAFile);
            input.setTranscriptome(transcriptFile);

            PAREametersEngine engine = new PAREametersEngine();
            engine.process();

            System.exit(0);
        }

        @Override
        public void printUsage() {
            System.out.println("Usage:\n  java [-XmxNg] -jar /path/to/Workbench.jar -tool " + getName() + " -pare_file path/to/pare/file -srna_file path/to/srna/file -genome_file path/to/genome/file -transcript_file path/to/transcript/file -output_dir path/to/output/directory -species_name species_name [-mirna_file path/to/mirna/file] [-config path/to/PAREameters/config/file] [-paresnip2_rules path/to/paresnip2_rules/file]");
        }
    },
    FIREPAT_TOOL("firepat") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            // Check files according to the order given in the usage
            boolean overwriteOutput = argmap.containsKey("f");

            File parameters = null;
            FiRePatConfiguration config = FiRePatConfiguration.getInstance();
            FiRePatInputFiles input = FiRePatInputFiles.getInstance();

            if (argmap.containsKey("parameters")) {
                parameters = ToolBox.checkInputFile("parameters", argmap.get("parameters"), true);
                config.loadFromFile(new File(argmap.get("parameters")));
            } else {
                System.out.println("No parameter file loaded");
                System.exit(1);
            }

            if (parameters != null) {

                //Load parameter
                //start FiRepat
                FiRePat fp = new FiRePat(); // create FiRePat object        
                fp.setAllParameters(); // load all parameters from config
                fp.loadData(); // load data from input files
                fp.preprocessData(); // filter/clean up input data
                fp.calculateCorrelations(); // get correlations
                fp.getDataForClustering();  // get subset of 'high quality' data for clustering
                fp.processDataForClustering(); // process this subset
                fp.writeOutputFiles(); // write required output to files

                System.exit(0);

            }

        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + " -parameters parameters/file/path");
        }
    },
    //
    // The filter tool
    //
    FILTER_TOOL("filter") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            boolean overwriteOutput = argmap.containsKey("f");
            File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
            File outFile = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

            FilterParams params = (paramsFile == null ? new FilterParams() : FilterParams.load(new FilterParams(), paramsFile));
//       System.out.println();
//      System.out.println( "Parameters:" );
//      System.out.println( params.toString() );
//      System.out.println();

            ToolBox.printParameters(params);

            Filter filter = new Filter(srnaFile, outFile, Tools.getNextDirectory(), params);
            filter.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path  -out_file output-file-path  [ -params params-file-path ]");
        }
    },
    //
    // The miRCat tool
    //
    MIRCAT_TOOL("mircat") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

            // Check using workflow mode
            boolean workflowMode = argmap.containsKey("pcw");

            MiRCatParams params = (paramsFile == null ? new MiRCatParams() : MiRCatParams.load(new MiRCatParams(), paramsFile));

            int numToGenerate = params.getThreadCount();
            ToolBox.printParameters(params);

            if (workflowMode) {
                ArrayList<Path> sample1 = new ArrayList<>();
                sample1.add(srnaFile.toPath());
                Map<String, List<Path>> samples = new HashMap<>();
                samples.put("miRCATRun", sample1);
                DatabaseWorkflowModule.getInstance().insertRawData(samples, genomeFile.toPath(), null);

                final String OUTPUT_DIR_ARG = "output_directory";

                Path outputPath = Paths.get(Tools.miRCAT_DATA_Path + DIR_SEPARATOR + "default_mircat_output");
                // Implementing argument to change the default ouput directory for miRCat results.
                if (argmap.containsKey(OUTPUT_DIR_ARG)) {
                    outputPath = Paths.get(argmap.get(OUTPUT_DIR_ARG));
                }

                PreconfiguredWorkflows.createMirCatWorkflow(Rectangle2D.EMPTY, true, outputPath, numToGenerate);

                try {
                    HTMLWizardViewController.setGenomePath(genomeFile.toPath());

                    HTMLWizardViewController.configureWorkflowData();
                } catch (Exception ex) {
                    Logger.getLogger(ToolBox.class.getName()).log(Level.SEVERE, null, ex);
                }

                WorkflowManager.getInstance().start();

            } else {

                Process_Hits_Patman mirCatProcess = new Process_Hits_Patman(params, srnaFile, genomeFile, Tools.getNextDirectory(), null);

                // Implementing argument to change the default ouput directory for miRCat results.
                final String OUTPUT_DIR_ARG = "output_directory";
                if (argmap.containsKey(OUTPUT_DIR_ARG)) {
                    mirCatProcess.setOutputDirectoryFilename(argmap.get(OUTPUT_DIR_ARG));
                }
                try {
                    mirCatProcess.setupThreads(numToGenerate);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
                mirCatProcess.run();
            }
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  -srna_file srna-file-path  -genome genome-file-path  [ -params params-file-path ] [ -output_directory ouput-directory-path ] [-pcw use-workflow-mode]");
        }
    },
    //
    // The SiLoCo tool
    //  //
    SILOCO_TOOL("siloco") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            String fileListString = argmap.get("srna_file_list");
            if (fileListString == null || fileListString.isEmpty()) {
                throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified.");
            }
            String fileList[] = fileListString.split(",");
            ArrayList<File> sampleFiles = new ArrayList<File>(fileList.length);
            int i = 0;
            for (String fileName : fileList) {
                sampleFiles.add(ToolBox.checkInputFile("srna_file_list", fileName, true));
                i++;
            }
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);

            SiLoCoParams params = (paramsFile == null ? new SiLoCoParams() : SiLoCoParams.load(new SiLoCoParams(), paramsFile));

            ToolBox.printParameters(params);

            SiLoCoProcess siLoCoProcess = new SiLoCoProcess(params, genomeFile, sampleFiles);
            siLoCoProcess.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  -srna_file_list comma-seperated-srna-file-paths  -genome genome-file-path  [ -params params-file-path ]");
        }
    },
    //
    // The miRProf tool
    //
    MIRPROF_TOOL("mirprof") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            boolean overwriteOutput = argmap.containsKey("f");

            //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
            String fileListString = argmap.get("srna_file_list");
            if (fileListString == null || fileListString.isEmpty()) {
                throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified.");
            }
            String fileList[] = fileListString.split(",");
            File sampleFiles[] = new File[fileList.length];
            int i = 0;
            for (String fileName : fileList) {
                sampleFiles[i] = ToolBox.checkInputFile("srna_file_list", fileName, true);
                i++;
            }
            File mirbaseFile = ToolBox.checkInputFile("mirbase_db", argmap.get("mirbase_db"), true);
            File outFile = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), false);

            MirprofParams params = (paramsFile == null) ? new MirprofParams() : MirprofParams.load(new MirprofParams(), paramsFile);

            ToolBox.printParameters(params);

            Mirprof mirprof = new Mirprof(Arrays.asList(sampleFiles), genomeFile, mirbaseFile, outFile, Tools.getNextDirectory(), params);
            mirprof.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file_list comma-seperated-srna-file-paths  -mirbase_db mirbase-file-path  -out_file output-file-path  [ -params params-file-path -genome genome-file-path ]\n -f = Force overwriting of output file");
        }
    },
    //
    // The tasi prediction tool
    //
    TASI_TOOL("tasi") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            boolean overwriteOutput = argmap.containsKey("f");

            File srnaFile = ToolBox.checkInputFile("srna_file", argmap.get("srna_file"), true);
            File outFile = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);

            TasiParams params = (paramsFile == null) ? new TasiParams() : TasiParams.load(new TasiParams(), paramsFile);

            ToolBox.printParameters(params);
            params.setGenome(genomeFile);

            TasiAnalyser ta = new TasiAnalyser(srnaFile, outFile, Tools.getNextDirectory(), params);
            ta.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path -genome genome-file-path -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file");
        }
    },
    // Normalisation tool
    NORMALISE_TOOL("normalise") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            String fileListString = argmap.get("srna_file_list");
            if (fileListString == null || fileListString.isEmpty()) {
                throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified.");
            }
            String fileArray[] = fileListString.split(",");
            int i = 0;
//      String sampleFiles[] = new String[fileArray.length]; 
            ArrayList<File> fileList = new ArrayList<>();
            for (String fileName : fileArray) {
                fileList.add(ToolBox.checkInputFile("srna_file_list", fileName, true));
                //sampleFiles[i] = ToolBox.checkInputFile( "srna_file_list", fileName, true ).getName();
                //i++;

            }

            ArrayList<ArrayList<File>> nestedFileList = new ArrayList<>();
            nestedFileList.add(fileList);

            File outDir = ToolBox.checkOutputFile("out_dir", argmap.get("out_dir"), true, true);
            if (!outDir.isDirectory()) {
                throw new IllegalArgumentException("Argument out_dir must be a directory");
            }
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);

            boolean writeCSV = argmap.containsKey("c");

            try {
                NormalisationType normType = NormalisationType.valueOf(argmap.get("normalisation_type"));

                NormalisationParams params = NormalisationParams.load(new NormalisationParams(), paramsFile);

                NormalisationProcess norm_engine = new NormalisationProcess(normType, nestedFileList, genomeFile, null, params, null, outDir);
                norm_engine.run();
                if (writeCSV) {
                    norm_engine.getMatrix().writeToCsv(new File(outDir.getPath() + DIR_SEPARATOR + normType.toString() + "_out.csv"), normType);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error: the normalisation_type defined is not known");
            }

        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  -srna_file_list [srna-file,]+ -genome genome-file-path -out_dir output-directory  [ -params params-file-path ] [-c] write-csv");
        }
    },
    //
    // The sequence alignment tool
    //
    SEQ_ALIGNMENT("seq_align") {
        @Override
        public void startTool(Map<String, String> argmap) throws IOException {
            boolean overwriteOutput = argmap.containsKey("f");

            //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
            String fileListString = argmap.get("srna_file_list");
            if (fileListString == null || fileListString.isEmpty()) {
                throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified.");
            }
            String fileList[] = fileListString.split(",");
            File sampleFiles[] = new File[fileList.length];
            int i = 0;
            for (String fileName : fileList) {
                sampleFiles[i] = ToolBox.checkInputFile("srna_file_list", fileName, true);
                i++;
            }
            File out_file = ToolBox.checkOutputFile("out_file", argmap.get("out_file"), true, overwriteOutput);
            File paramsFile = ToolBox.checkInputFile("params", argmap.get("params"), false);
            File genomeFile = ToolBox.checkInputFile("genome", argmap.get("genome"), true);

            PatmanParams params = (paramsFile == null) ? new PatmanParams() : PatmanParams.load(new PatmanParams(), paramsFile);

            ToolBox.printParameters(params);

//    LOGGER.log( Level.INFO,  + "Starting job: Short Reads: {0}; Long Reads: {1}; Out: {2}; Params: {3}", 
//      new Object[]{short_reads.getPath(), long_reads.getPath(), output.getPath(), pp.toString()} );
            if (out_file.isDirectory()) {
                //String singleOutPath = out_file.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_Align.patman";

                for (File inputFile : sampleFiles) {
//          PatmanRunner newRunner = new PatmanRunner( inputFile, genomeFile );
//          newRunner.runSequenceAligner( inputFile, long_reads, output_dir, params, null );
//          this.sa_runners.put( inputFile.getName(), newRunner );
                }
            } else {

            }

//      TasiAnalyser ta = new TasiAnalyser( srnaFile, outFile, Tools.getNextDirectory(), params );
//      ta.run();
        }

        @Override
        public void printUsage() {
            System.out.println(USAGE_PREFIX + getName() + "  [-f] -srna_file srna-file-path -genome genome-file-path -out_file output-file-path  [ -params params-file-path ]\n -f = Force overwriting of output file");
        }
    },
    //
    // The sequence alignment tool
    //
    MIRCAT_2("mircat2") {

        @Override
        public void startTool(Map<String, String> argmap) throws IOException {

            boolean overwriteOutput = argmap.containsKey("f");

            //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
            String jsonConfigString = argmap.get("config");
            if (jsonConfigString == null || jsonConfigString.isEmpty()) {
                throw new IllegalArgumentException("Error: The parameter config must be specified.");
            }

            if (argmap.get("standalone") == null) {
                PreconfiguredWorkflows.runCommandLineWorkflowMiRCat2("mircat2", new File(jsonConfigString));

                WorkflowManager.getInstance().start();

            } else {
                new MiRCat2Main(new File(jsonConfigString));
                System.exit(0);
            }
        }

        @Override
        public void printUsage() {

            System.out.println(USAGE_PREFIX + getName() + "  [-f] -config configuration file containing all required input for miRCAT 2 in JSON format"
                    + LINE_SEPARATOR
                    + "more specific information can be found at http://srna-workbench.cmp.uea.ac.uk/"
                    + LINE_SEPARATOR
                    + "an example of a miRCAT 2 configuration file can be found in: [install directory]/data/default_json_setup"
                    + LINE_SEPARATOR
                    + "-f = Force overwriting of output file");
        }
    },
    
    PAREfirst("parefirst") {

        @Override
        public void startTool(Map<String, String> argmap) throws IOException {


            String jsonConfigString = argmap.get("config");
            if (jsonConfigString == null || jsonConfigString.isEmpty()) {
                System.out.println("Error: The parameter config must be specified.");
                System.exit(1);
            }

                PreconfiguredWorkflows.runCommandLineWorkflowPAREfirst("parefirst", new File(jsonConfigString));

                WorkflowManager.getInstance().start();
                System.out.println("You can find your results in this directory: " + new File(Tools.PAREfirst_DATA_Path).getAbsolutePath());
                System.exit(0);

        }
        @Override
        public void printUsage() {

            System.out.println(USAGE_PREFIX + getName() + "  -config configuration file containing all required input for PAREfirst in JSON format"
                    + LINE_SEPARATOR
                    + "more specific information can be found at http://srna-workbench.cmp.uea.ac.uk/"
                    + LINE_SEPARATOR
                    + "an example of a PAREfirst configuration file can be found in: [install directory]/data/default_json_setup");
        }
    };
    //
    // The tasi prediction tool
    //
//  SILOCO( "siloco" )
//  {
//    @Override
//    public void startTool( Map<String, String> argmap ) throws IOException
//    {
//      File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
//      File genomeFile = ToolBox.checkOutputFile( "genome_file", argmap.get( "genome_file" ), true );
//      File paramsFile = ToolBox.checkInputFile( "params", argmap.get( "params" ), false );
//
//      SiLoCoParams params = ( paramsFile == null ) ? new SiLoCoParams() : SiLoCoParams.load( new SiLoCoParams(), paramsFile );
//
//      ToolBox.printParameters( params );
//
//      SiLoCoProcess sp = new SiLoCoProcess(null);
//      sp.inputSampleData( new File[]
//        {
//          srnaFile
//        } );
//      sp.inputGenomeFile( genomeFile );
//      sp.inputParams( params );
//
//      sp.run();
//
//      //TODO Siloco currently doesn't dump data to file (AFAIK).  Need to implement this for command line use.
//    }
//
//    @Override
//    public void printUsage()
//    {
//      System.out.println( USAGE_PREFIX + getName() + "  -srna_file srna-file-path  -genome_file genome-file-path  [ -params params-file-path ]" );
//    }
//  };

    private static void printParameters(ToolParameters params) {
        if (AppUtils.INSTANCE.verbose()) {
            System.out.println();
            System.out.println("Parameters:");
            System.out.println(params.toString());
            System.out.println();
        }
    }

    /*
   * Instance methods and state
     */
    private final String _name;

    private ToolBox(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    /**
     * Start the tool
     *
     * @param argmap Map of command line arguments
     * @throws IOException
     */
    public abstract void startTool(Map<String, String> argmap) throws IOException;

    /**
     * Explain how to run a tool
     */
    public abstract void printUsage();

    /* Static methods and state */
    private static final String USAGE_PREFIX = "Usage:\n  java -jar /path/to/Workbench.jar [-verbose] -tool ";
    private static final Map< String, ToolBox> TOOLS = new HashMap< String, ToolBox>();

    static {
        for (ToolBox t : ToolBox.values()) {
            TOOLS.put(t._name, t);
        }
    }

    /**
     * Get a tool for a given name
     *
     * @param toolName The name of the tool
     * @return The tool or the unknown tool
     */
    public static ToolBox getToolForName(String toolName) {
        if (toolName != null) {
            toolName = toolName.toLowerCase();
        }

        ToolBox t = TOOLS.get(toolName);

        if (t == null) {
            return UNKNOWN_TOOL;
        }

        return t;
    }

    private static boolean checkFileIsOK(File f) {
        return (f.exists() && f.isFile() && f.canRead());
    }

    /**
     * Check the validity of an input file. If a file is specified, then it is
     * considered valid if it exists and it's readable.
     *
     * @param parameterName
     * @param filename
     * @param isMandatory
     *
     * @return The File object, or null
     */
    private static File checkInputFile(String parameterName, String filename, boolean isMandatory) {
        if (filename == null || filename.trim().isEmpty()) {
            if (isMandatory) {
                throw new IllegalArgumentException(String.format("Error: The parameter '%s' must be specified.", parameterName));
            } else {
                return null;
            }
        }

        // The filename is specified, check it's a proper file...
        //
        File f = new File(filename);

        boolean fileExistsAndIsOK = (f.exists() && f.isFile() && f.canRead());

        if (!fileExistsAndIsOK) {
            throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', either does not exist or is unreadable.", parameterName, filename));
        }

        return f;
    }

    /**
     * Check the validity of an output file. If the file already exists then it
     * is not overwritten.
     *
     * @param parameterName
     * @param filename
     * @param isMandatory
     *
     * @return The File object
     */
    private static File checkOutputFile(String parameterName, String filename, boolean isMandatory, boolean overwriteOutput) {
        if (filename == null || filename.trim().isEmpty()) {
            if (isMandatory) {
                throw new IllegalArgumentException(String.format("Error: The parameter '%s' must be specified.", parameterName));
            } else {
                return null;
            }
        }

        // The filename is specified, check whether it exists...
        //
        File f = new File(filename);

        if (f.exists() && !overwriteOutput) {
            throw new IllegalArgumentException(String.format("Error: The '%s' file, '%s', already exists, please choose a different name for the file.", parameterName, filename));
        }

        return f;
    }
}
