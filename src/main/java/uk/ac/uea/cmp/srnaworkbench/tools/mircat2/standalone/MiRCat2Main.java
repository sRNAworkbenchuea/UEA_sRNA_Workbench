/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.Engine;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.JsonUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

/**
 *
 * @author keu13sgu
 */
public final class MiRCat2Main {

    public static boolean verbose = true;

    private String patmanFile;
    private String sRNAfile;
    private String genomeFile;
    private String outputFile;
    private String miRBaseFile;
    private String DEFile;

    long startTime;

    public MiRCat2Main(File json) {

        startTime = System.nanoTime();
        miRBaseFile = "none";
        try {
            configureJson(json);
            
            if(sRNAfile.toLowerCase().endsWith(".patman")){
                patmanFile = sRNAfile;
            }else{
                patmanFile = createPatamanFile(new File(sRNAfile), new File(genomeFile));
            }
            DEFile = "none";
            
            File tempGenome = new File(genomeFile.substring(0, genomeFile.lastIndexOf(".")) + "_temp.fa");
            if (!tempGenome.exists()) {
                //Make a copy of the genome
                Files.copy(new File(genomeFile).toPath(), tempGenome.toPath());

                //Append a fake Chr to avoid it skipping last chr
                BufferedWriter bw = new BufferedWriter(new FileWriter(tempGenome, true));
                bw.newLine();
                bw.write(">ChrTemp");
                bw.newLine();
                bw.write("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                bw.flush();
                tempGenome.deleteOnExit();
            }

            
            
            InputOutput.readAllAndExecute(patmanFile, tempGenome.getAbsolutePath(), miRBaseFile, DEFile, outputFile, Params.allInfo);

            shutdown();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public MiRCat2Main(String srnaFile, String geneFile, String outDir, String paramFile, String miRBase, MiRCat2SceneController controller) throws IOException {
        sRNAfile = srnaFile;
        genomeFile = geneFile;
        outputFile = outDir + DIR_SEPARATOR + Paths.get(sRNAfile).toFile().getName();
        
        if (new File(paramFile).exists()) {
            InputOutput.readParams(paramFile);
            LOGGER.log(Level.INFO, "mircat 2 params loaded");
        } else {
            throw new IOException("Params file: " + paramFile + " not found.");
        }
        
        startTime = System.nanoTime();
        try {

            patmanFile = createPatamanFile(new File(sRNAfile), new File(genomeFile));
            DEFile = "none";
            miRBaseFile = miRBase;

            File tempGenome = new File(genomeFile.substring(0, genomeFile.lastIndexOf(".")) + "_temp.fa");
            if (!tempGenome.exists()) {
                //Make a copy of the genome
                Files.copy(new File(genomeFile).toPath(), tempGenome.toPath());

                //Append a fake Chr to avoid it skipping last chr
                BufferedWriter bw = new BufferedWriter(new FileWriter(tempGenome, true));
                bw.newLine();
                bw.write(">ChrTemp");
                bw.newLine();
                bw.write("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                bw.flush();
                tempGenome.deleteOnExit();
            }
            InputOutput.setController(controller);
            InputOutput.readAllAndExecute(patmanFile, tempGenome.getAbsolutePath(), miRBaseFile, DEFile, outputFile, Params.allInfo);

            shutdown();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) throws IOException {
    }

    private void shutdown() {

        LOGGER.log(Level.INFO, "Analysis complete! You can find your results in the selected output directory.");

        if (AppUtils.INSTANCE.isCommandLine()) {
            long duration = System.nanoTime() - startTime;
            long timeInMilliSeconds = duration / 1000000;
            long seconds = timeInMilliSeconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            String time = days + " days : " + hours % 24 + " hours : " + minutes % 60 + " minutes : " + seconds % 60 + " seconds";
            if (verbose) {
                System.out.println("The analysis was completed in: " + time);
            }
            //System.exit(0);
        }

    }

    public void configureJson(File json) throws IOException {
        JsonObject jsonObject = null;
        try {
            jsonObject = JsonUtils.parseJsonFile(json);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
        }

        if (jsonObject.containsKey("miRCAT2_params")) {
            Path mc_paramsPath = Paths.get(jsonObject.getString("miRCAT2_params"));
            if (Files.exists(mc_paramsPath)) {
//                MiRCat2Module.readParams(mc_paramsPath.toString());
                InputOutput.readParams(jsonObject.getString("miRCAT2_params"));
                LOGGER.log(Level.INFO, "mircat 2 params loaded");
            } else {
                throw new IOException("Params file: " + mc_paramsPath + " not found.");
            }
        } else {
            throw new IOException("A miRCat 2 parameter file is required.");
        }

        // Get sRNA file(s)
        HashMap<String, List<Path>> srna_samples = new HashMap<>();
        List<Path> srna_filenames_paths = new ArrayList<>();
        List<String> srna_filesToInclude = new ArrayList<>();
        if (jsonObject.containsKey("srna_files")) {
            JsonArray srna_files = jsonObject.getJsonArray("srna_files");
            for (int i = 0; i < srna_files.size(); i++) {
                JsonObject srna_file_obj = srna_files.getJsonObject(i);
                if (srna_file_obj.containsKey("srna_filename")) {
                    sRNAfile = srna_file_obj.getString("srna_filename");
                    if (Paths.get(sRNAfile).toFile().exists()) {
                        srna_filenames_paths.add(Paths.get(sRNAfile));
                        srna_filesToInclude.add(Paths.get(sRNAfile).getFileName().toString());
                        String message = "Added sRNA file: " + sRNAfile;
                        if (verbose) {
                            System.out.println(message);
                        }
                        LOGGER.log(Level.INFO, message);
                    } else {
                        throw new IOException("sRNA file: " + sRNAfile + " not found.");
                    }
                }
            }
        }
        // ensure at least one sRNA file has been added
        if (srna_filenames_paths.isEmpty()) {
            //throw new JSONParserException("At least one sRNA file is required.");
            throw new IOException("At least one sRNA file is required.");
        }
        srna_samples.put("sample_0", srna_filenames_paths);

        // get genome
        if (jsonObject.containsKey("genome_filename")) {
            Path genomePath = Paths.get(jsonObject.getString("genome_filename"));
            if (genomePath.toFile().exists()) {
                String message = "Added genome file: " + genomePath;
                genomeFile = jsonObject.getString("genome_filename");
                LOGGER.log(Level.INFO, message);
            } else {
                throw new IOException("Genome file: " + genomePath + " not found.");
            }
        } else {
            //throw new JSONParserException("A genome file is required.");
            throw new IOException("A genome file is required.");
        }

        // Get output file
        if (jsonObject.containsKey("miRCAT2_Output_Dir")) {
            Path mc_outPath = Paths.get(jsonObject.getString("miRCAT2_Output_Dir"));
            if (Files.exists(mc_outPath)) {
                //outputFile = mc_outPath.toString()+"/mircat2_result_test";
                outputFile = mc_outPath.toString() + "/" + Paths.get(sRNAfile).toFile().getName();
                LOGGER.log(Level.INFO, "mircat 2 out path detected: {0}", mc_outPath);
            }
        } else {
            throw new IOException("A miRCat 2 output directory is required.");

        }
        
        if (jsonObject.containsKey("annotation_filename")) {
            Path mirbase_gff_path = Paths.get(jsonObject.getString("annotation_filename"));
            if (Files.exists(mirbase_gff_path)) {
                miRBaseFile = mirbase_gff_path.toString();
                LOGGER.log(Level.INFO, "mircat 2 out path detected: {0}", mirbase_gff_path);
            }
        }
    }

    public String createPatamanFile(File smallRNAs, File genome) {
        String alignedFile = outputFile.substring(0, outputFile.lastIndexOf(DIR_SEPARATOR) + 1) + smallRNAs.getName() + ".patman";

        File patman_out = new File(alignedFile);

        PatmanParams newP_Params = new PatmanParams();
        newP_Params.setMaxGaps(0);
        newP_Params.setMaxMismatches(0);
        newP_Params.setPreProcess(false);
        newP_Params.setPostProcess(false);
        newP_Params.setMakeNR(true);
        newP_Params.setPositiveStrandOnly(false);

        File tmpDir = new File("tmp");

        try {
            tmpDir.mkdir();
            PatmanRunner runner = new PatmanRunner(smallRNAs, genome,
                    patman_out, tmpDir, newP_Params);
            runner.setUsingDatabase(false);

            Thread myThread = new Thread(runner);

            myThread.start();
            myThread.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tmpDir.deleteOnExit();
        return alignedFile;
    }

}
