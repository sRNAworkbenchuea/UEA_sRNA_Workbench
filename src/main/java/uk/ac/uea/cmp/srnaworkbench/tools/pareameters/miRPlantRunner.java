/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class miRPlantRunner {

    File miRPlantDirectory;
    File results;
    miRPlantParameters parameters;
    boolean genomeExists;
    ArrayList<File> individualChromosomeFiles;

    public miRPlantRunner(miRPlantParameters params) {
        miRPlantDirectory = new File(Tools.EXE_DIR + File.separator + "miRPlant");
        parameters = params;

    }

    public void process() {
        try {
            System.out.println("Attempting to run miRPlant to predict miRNAs...");
            checkSetUp();

            if (!genomeExists) {
                System.out.println("Genome index doesn't exist... ");
                System.out.println("Splitting genome into single chromosomes...");
                splitChromosome();
                System.out.println("Split complete...");
                System.out.println("Indexing the supplied genome...");
                buildIndex();
                System.out.println("Completed indexing...");
                System.out.println("Moving the indexed genome to required folder...");
                deleteGeneratedChromosomeFiles();
                copyDirectory(parameters.getGenomeBuildDirectory(), parameters.getBuiltGenomeDirectory());
                deleteDir(parameters.getGenomeBuildDirectory());
                System.out.println("Moving complete...");
                System.out.println("Adding miRBase files...");
                addMiRBaseFiles();
                System.out.println("Adding complete...");
            } else {
                System.out.println("Genome index already exists...");
            }

            System.out.println("Moving small RNA sequences to miRPlant directory...");
            File f = new File(miRPlantDirectory.getAbsolutePath() + File.separator + "candidates.fa");
            if (f.exists()) {
                f.delete();
            }
            Files.copy(parameters.getCandidates().toPath(), f.toPath());
            parameters.setCandidates(f);

            System.out.println("Starting analysis...");
            runMiRPlant();

            cleanMiRPlantDir();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void checkSetUp() {
        //check miRPlant actually exists
        if (!miRPlantDirectory.isDirectory()) {
            System.out.println("Problem with miRPlant directory set up. Exiting...");
            System.exit(1);
        }

        parameters.setBuiltGenomeDirectory(new File(miRPlantDirectory.getAbsolutePath() + File.separator + "genome" + File.separator + parameters.getGenomeName()));
        parameters.setGenomeBuildDirectory(new File(miRPlantDirectory.getAbsolutePath() + File.separator + "build_bwt_idx_v32" + File.separator + "genome" + File.separator + parameters.getGenomeName()));

        //Check if the genome is already built
        if (parameters.getBuiltGenomeDirectory().exists()) {
            genomeExists = true;
        } else {
            parameters.genomeBuildDirectory.mkdirs();
        }

    }

    private void splitChromosome() {
        //need to split long chromosomes up into Chr1A, Chr1B etc as contig the BAM index format cannot handle a contig that is longer than 512MB.
        //see bottom of here http://broadinstitute.github.io/picard/faq.html

        //Probably shouldn't read the whole chr seq into one string either, not good for memory!
        StringBuilder sb = new StringBuilder();
        individualChromosomeFiles = new ArrayList<>();
        BufferedReader br;
        BufferedWriter bw = null;
        String chrName = null;
        String line;
        int chrSplitNum = 1;
        boolean hasSplits = false;

        try {
            br = new BufferedReader(new FileReader(parameters.getGenome()));

            while ((line = br.readLine()) != null) {

                if (line.startsWith(">")) {
                    if (chrName != null) {
                        if (!hasSplits) {
                            File tmpChr = new File(parameters.getGenomeBuildDirectory().getAbsolutePath() + File.separator + chrName.substring(1) + ".fa");
                            individualChromosomeFiles.add(tmpChr);

                            if (bw != null) {
                                bw.flush();
                                bw.close();
                            }

                            bw = new BufferedWriter(new FileWriter(tmpChr));
                            bw.write(chrName);
                        } else {
                            File tmpChr = new File(parameters.getGenomeBuildDirectory().getAbsolutePath() + File.separator + chrName.substring(1) + "_" + chrSplitNum + ".fa");
                            individualChromosomeFiles.add(tmpChr);
                            if (bw != null) {
                                bw.flush();
                                bw.close();
                            }
                            bw = new BufferedWriter(new FileWriter(tmpChr));
                            bw.write(chrName + "_" + chrSplitNum);
                        }
                        bw.newLine();
                        bw.write(sb.toString());
                        bw.flush();
                        sb = new StringBuilder();
                    }

                    chrName = line.split(" ")[0];
                    chrSplitNum = 1;
                    hasSplits = false;
                } else {
                    sb.append(line);
                    if (sb.length() >= 500000000) {
                        hasSplits = true;
                        File tmpChr = new File(parameters.getGenomeBuildDirectory().getAbsolutePath() + File.separator + chrName.substring(1) + "_" + chrSplitNum + ".fa");
                        individualChromosomeFiles.add(tmpChr);

                        if (bw != null) {
                            bw.flush();
                            bw.close();
                        }

                        bw = new BufferedWriter(new FileWriter(tmpChr));
                        bw.write(chrName + "_" + chrSplitNum);
                        bw.newLine();
                        bw.write(sb.toString());
                        bw.flush();
                        sb = new StringBuilder();
                        chrSplitNum++;
                    }
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (chrName != null) {
                    File tmpChr = new File(parameters.getGenomeBuildDirectory().getAbsolutePath() + File.separator + chrName.substring(1) + ".fa");
                    individualChromosomeFiles.add(tmpChr);
                    if (bw != null) {
                        bw.flush();
                        bw.close();
                    }
                    bw = new BufferedWriter(new FileWriter(tmpChr));
                    bw.write(chrName);
                    bw.newLine();
                    bw.write(sb.toString());
                    bw.flush();

                    bw.close();

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    private void buildIndex() {

        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "-Xmx8g", "build_bwt_idx.jar", parameters.getGenomeName()).inheritIO();
            pb.directory(new File(miRPlantDirectory.getAbsolutePath() + File.separator + "build_bwt_idx_v32"));
            //System.out.println(pb.command().toString());
           int errCode = pb.start().waitFor();

        System.out.println("Indexing finished executed, any errors?: " + (errCode == 0 ? "No" : "Yes"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    

    private void copyDirectory(File sourceLocation, File targetLocation) {

        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }

                String[] children = sourceLocation.list();
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceLocation, children[i]),
                            new File(targetLocation, children[i]));
                }
            } else {

                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
            return true;
        }

        return false;
    }

    private void addMiRBaseFiles() {
        File mirbaseDir = new File(parameters.getBuiltGenomeDirectory().getAbsolutePath() + File.separator + "miRBase");
        mirbaseDir.mkdir();

        File matureSeqs = new File(mirbaseDir.getAbsolutePath() + File.separator + "mature.fa");
        File hairpinSeqs = new File(mirbaseDir.getAbsolutePath() + File.separator + "hairpin.fa");
        File miRBaseGFF3 = new File(mirbaseDir.getAbsolutePath() + File.separator + "knownMiR.gff3");

        try {
            Files.copy(parameters.getKnownMature().toPath(), matureSeqs.toPath());
            Files.copy(parameters.getKnownHairpin().toPath(), hairpinSeqs.toPath());
            Files.copy(parameters.getSpeciesKnownGFF().toPath(), miRBaseGFF3.toPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void deleteGeneratedChromosomeFiles() {

        for (File f : individualChromosomeFiles) {
            f.delete();
        }

        for (File f : parameters.getGenomeBuildDirectory().listFiles()) {
            if (f.getName().contains("media_tmp")) {
                f.delete();
            }
        }
    }

    private void runMiRPlant() {

        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "miRPlant_command_line.jar", "-g", parameters.getGenomeName(), "candidates.fa");
            pb.directory(new File(miRPlantDirectory.getAbsolutePath()));
            //System.out.println(pb.command().toString());
            Process p = pb.start();
            p.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public File getResults() {
        return results;
    }

    private void cleanMiRPlantDir() {

        for (File f : miRPlantDirectory.listFiles()) {
            if (!f.isDirectory()) {
                if (f.getName().contains("candidates")) {
                    if (!f.getName().equals("candidates.result")) {
                        f.delete();
                    } else {
                        results = f;
                    }
                }
            }
        }

    }

}
