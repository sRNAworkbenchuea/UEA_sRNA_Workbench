/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.File;

/**
 *
 * Class that represents the input files required for running PAREameters
 *
 * @author rew13hpu
 */
public class PAREametersInput {

    private File genomeFile;
    private File transcriptome;
    private File matureMiRNAs;
    private File hairpinSeqs;
    private File speciesMiRNAs;
    private File knownMiRGFF;
    private File smallRNAs;
    private File degradome;
    private File PAREsnip2TargetingRules;
    private File outputDir;
    private File usersMiRNAs;
    private String organismName;

    private static PAREametersInput input = null;

    public static PAREametersInput getInstance() {
        if (input == null) {
            input = new PAREametersInput();

        }
        return input;
    }

    public File getGenomeFile() {
        return genomeFile;
    }

    public void setGenomeFile(File genomeFile) {
        this.genomeFile = genomeFile;
    }

    public File getTranscriptome() {
        return transcriptome;
    }

    public void setTranscriptome(File transcriptome) {
        this.transcriptome = transcriptome;
    }

    public File getMatureMiRNAs() {
        return matureMiRNAs;
    }

    public void setMatureMiRNAs(File matureMiRNAs) {
        this.matureMiRNAs = matureMiRNAs;
    }

    public File getHairpinSeqs() {
        return hairpinSeqs;
    }

    public void setHairpinSeqs(File hairpinSeqs) {
        this.hairpinSeqs = hairpinSeqs;
    }

    public File getSpeciesMiRNAs() {
        return speciesMiRNAs;
    }

    public void setSpeciesMiRNAs(File speciesMiRNAs) {
        this.speciesMiRNAs = speciesMiRNAs;
    }

    public File getKnownMiRGFF() {
        return knownMiRGFF;
    }

    public void setKnownMiRGFF(File knownMiRGFF) {
        this.knownMiRGFF = knownMiRGFF;
    }

    public File getSmallRNAs() {
        return smallRNAs;
    }

    public void setSmallRNAs(File smallRNAs) {
        this.smallRNAs = smallRNAs;
    }

    public File getDegradome() {
        return degradome;
    }

    public void setDegradome(File degradome) {
        this.degradome = degradome;
    }

    public File getPAREsnip2TargetingRules() {
        return PAREsnip2TargetingRules;
    }

    public void setPAREsnip2TargetingRules(File PAREsnip2TargetingRules) {
        this.PAREsnip2TargetingRules = PAREsnip2TargetingRules;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDirectory) {
        this.outputDir = outputDirectory;

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        if (outputDirectory.isDirectory()) {
            this.outputDir = outputDirectory;
        } else {
            System.out.println("The output location must be a valid directory!");
            System.exit(0);
        }

    }

    public String getOrganismName() {
        return organismName;
    }

    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }

    public File getUsersMiRNAs() {
        return usersMiRNAs;
    }

    public void setUsersMiRNAs(File usersMiRNAs) {
        this.usersMiRNAs = usersMiRNAs;
    }
    
    

}
