/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author rew13hpu
 */
public class miRPlantParameters {
    
    
    File genome;
    File candidates;
    File knownMature;
    File knownHairpin;
    File speciesKnownGFF;
    File genomeBuildDirectory;
    File builtGenomeDirectory;
    File outputDir;
    String genomeName;
       
    double maxInconRatio;
    int lengthOfFlank;
    int minLoopLength;
    int precursorLength;
    String assembly;
    String candidateFile;
    String adapter;
    int minSeqLength;
    int maxSeqLength;
    int minPhredQuality;
    int maximumMaps;
    int minAbundance;
    double minScore;
    
    public miRPlantParameters()
    {
        maxInconRatio = 0.1;
        lengthOfFlank = 10;
        minLoopLength = 20;
        precursorLength = 200;        
        minSeqLength = 18;
        maxSeqLength = 23;
        minPhredQuality = 20;
        maximumMaps = 101;
        minAbundance = 5;
        minScore = -10;
        assembly = null;
        adapter = null;
    }

    public double getMaxInconRatio() {
        return maxInconRatio;
    }

    public void setMaxInconRatio(double maxInconRatio) {
        this.maxInconRatio = maxInconRatio;
    }

    public int getLengthOfFlank() {
        return lengthOfFlank;
    }

    public void setLengthOfFlank(int lengthOfFlank) {
        this.lengthOfFlank = lengthOfFlank;
    }

    public int getMinLoopLength() {
        return minLoopLength;
    }

    public void setMinLoopLength(int minLoopLength) {
        this.minLoopLength = minLoopLength;
    }

    public int getPrecursorLength() {
        return precursorLength;
    }

    public void setPrecursorLength(int precursorLength) {
        this.precursorLength = precursorLength;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getCandidateFile() {
        return candidateFile;
    }

    public void setCandidateFile(String candidateFile) {
        this.candidateFile = candidateFile;
    }

    public String getAdapter() {
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public int getMinSeqLength() {
        return minSeqLength;
    }

    public void setMinSeqLength(int minSeqLength) {
        this.minSeqLength = minSeqLength;
    }

    public int getMaxSeqLength() {
        return maxSeqLength;
    }

    public void setMaxSeqLength(int maxSeqLength) {
        this.maxSeqLength = maxSeqLength;
    }

    public int getMinPhredQuality() {
        return minPhredQuality;
    }

    public void setMinPhredQuality(int minPhredQuality) {
        this.minPhredQuality = minPhredQuality;
    }

    public int getMaximumMaps() {
        return maximumMaps;
    }

    public void setMaximumMaps(int maximumMaps) {
        this.maximumMaps = maximumMaps;
    }

    public int getMinAbundance() {
        return minAbundance;
    }

    public void setMinAbundance(int minAbundance) {
        this.minAbundance = minAbundance;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public File getGenome() {
        return genome;
    }

    public void setGenome(File genome) {
        this.genome = genome;
    }

    public File getCandidates() {
        return candidates;
    }

    public void setCandidates(File candidates) {
        this.candidates = candidates;
    }

    public File getKnownMature() {
        return knownMature;
    }

    public void setKnownMature(File knownMature) {
        this.knownMature = knownMature;
    }

    public File getKnownHairpin() {
        return knownHairpin;
    }

    public void setKnownHairpin(File knownHairpin) {
        this.knownHairpin = knownHairpin;
    }

    public File getSpeciesKnownGFF() {
        return speciesKnownGFF;
    }

    public void setSpeciesKnownGFF(File speciesKnownGFF) {
        this.speciesKnownGFF = speciesKnownGFF;
    }

    public File getGenomeBuildDirectory() {
        return genomeBuildDirectory;
    }

    public void setGenomeBuildDirectory(File genomeBuildDirectory) {
        this.genomeBuildDirectory = genomeBuildDirectory;
    }

    public File getBuiltGenomeDirectory() {
        return builtGenomeDirectory;
    }

    public void setBuiltGenomeDirectory(File builtGenomeDirectory) {
        this.builtGenomeDirectory = builtGenomeDirectory;
    }

    public String getGenomeName() {
        return genomeName;
    }

    public void setGenomeName(String genomeName) {
        this.genomeName = genomeName;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
              
    
    
    
            
}
