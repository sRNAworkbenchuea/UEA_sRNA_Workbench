/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import uk.ac.uea.cmp.srnaworkbench.io.stream.Utils;

/**
 *
 * @author rew13hpu
 */
public class Paresnip2InputFiles implements Serializable {

    private Map<String, File> sRNA_samples = new LinkedHashMap<>();
    private Map<String, File> degradome_samples = new LinkedHashMap<>();
    private File outputDirectory;
    private File genomeFile;
    private File transcriptomeFile;
    private File gff3File;

    private Paresnip2InputFiles() {
        sRNA_samples = new LinkedHashMap<>();
        degradome_samples = new LinkedHashMap<>();
    }

    private static Paresnip2InputFiles input;

    public static Paresnip2InputFiles getInstance() {
        if (input == null) {
            input = new Paresnip2InputFiles();
        }

        return input;
    }

    public boolean addSmallRNAReplicate(File file) {
        if (sRNA_samples.containsKey(file.getName())) {
            return false;
        }

        sRNA_samples.put(file.getName(), file);

        return true;
    }

    public boolean addDegradomeReplicate(File file) {
        if (degradome_samples.containsKey(file.getName())) {
            return false;
        }

        degradome_samples.put(file.getName(), file);

        return true;
    }

    public boolean isValid() {

        return true;
    }

    public String isStepOneComplete() {
        boolean b = (!sRNA_samples.isEmpty() && !degradome_samples.isEmpty());

        if (!b) {
            String alertContent = "Please add at least one small RNA and one degradome library!";
            return alertContent;
        }

        return "";

    }

    public String isStepTwoComplete() {

        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        String msg = null;

        if (config.isAlignSmallRNAsToGenome() && genomeFile == null) {
            msg = "You must input a genome file for the alignment!";
        }

        if (config.isUsingTranscriptome()) {
            if (transcriptomeFile == null) {
                msg = "You must input a transcriptome file!";
            }
        } else if (gff3File == null || genomeFile == null) {
            msg = "You must input both a GFF3 file and a genome file!";
        }

        if (msg != null) {
            return msg;
        }

        return "";
    }

    public boolean removeSmallRNAReplicate(String name) {
        return sRNA_samples.remove(name) != null;
    }

    public boolean removeDegradomeReplicate(String name) {
        return degradome_samples.remove(name) != null;
    }

    public boolean addTranscriptome(File file) {
        transcriptomeFile = file;
        return true;
    }

    public boolean addGenome(File file) {

        genomeFile = file;
        return true;
    }

    public boolean addGFF(File file) {
        gff3File = file;
        return true;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOuputDirectory(File outputDirectory) {

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        if (outputDirectory.isDirectory()) {
            this.outputDirectory = outputDirectory;
        } else {
            System.out.println("The output location must be a valid directory!");
            System.exit(0);
        }
    }

    public void setsRNA_samples(Map<String, File> sRNA_samples) {
        this.sRNA_samples = sRNA_samples;
    }

    public void setDegradome_samples(Map<String, File> degradome_samples) {
        this.degradome_samples = degradome_samples;
    }

    public Map<String, File> getsRNA_samples() {
        return sRNA_samples;
    }

    public Map<String, File> getDegradome_samples() {
        return degradome_samples;
    }

    public File getGenomeFile() {
        return genomeFile;
    }

    public File getTranscriptomeFile() {
        return transcriptomeFile;
    }

    public File getGff3File() {
        return gff3File;
    }

    public void reset() {
        sRNA_samples = new LinkedHashMap<>();
        degradome_samples = new LinkedHashMap<>();
        outputDirectory = null;
        genomeFile = null;
        transcriptomeFile = null;
        gff3File = null;
    }

}
