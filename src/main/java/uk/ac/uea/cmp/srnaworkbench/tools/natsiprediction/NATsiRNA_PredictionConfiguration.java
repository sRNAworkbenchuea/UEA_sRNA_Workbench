/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Josh Thody
 */
public class NATsiRNA_PredictionConfiguration {

    private static NATsiRNA_PredictionConfiguration config = null;

    private double MFE_ratioCutoff, coverageRatio, largestBubbleRegion;
    private int minSmallRNALength, maxSmallRNALength, minTagLength,
            maxTagLength, minSmallRNAAbundance, minPhases, minTagAbundance,
            numThreads;

    private boolean lowComplexityFilter, cisOnly, alignToGenome;

    private int minOverlapLength;

    //Maybe include for PAREsnip2?
    private boolean[] allowedTargetCategories;
    private boolean[] allowedPredictionCategories;

    public static NATsiRNA_PredictionConfiguration getInstance() {
        if (config == null) {
            config = new NATsiRNA_PredictionConfiguration();
        }

        return config;
    }

    private NATsiRNA_PredictionConfiguration() {
        defaultConfiguration();
    }

    private void defaultConfiguration() {
        MFE_ratioCutoff = 0.7;
        minOverlapLength = 100;
        largestBubbleRegion = 0.1;
        coverageRatio = 0.8;
        minSmallRNALength = 19;
        maxSmallRNALength = 24;
        minTagLength = 19;
        maxTagLength = 21;
        minSmallRNAAbundance = 1;
        minTagAbundance = 1;
        minPhases = 1;
        numThreads = Runtime.getRuntime().availableProcessors();
        lowComplexityFilter = true;

        allowedPredictionCategories = new boolean[5];
        allowedTargetCategories = new boolean[5];

        for (int i = 0; i < 5; i++) {
            allowedPredictionCategories[i] = true;
            allowedTargetCategories[i] = true;
        }

    }

    public boolean allowedBiogenesisCategory(int cat) {
        return allowedPredictionCategories[cat - 1];
    }

    public int getMinOverlapLength() {
        return minOverlapLength;
    }

    public boolean isAlignToGenome() {
        return alignToGenome;
    }

    public void setAlignToGenome(boolean alignToGenome) {
        this.alignToGenome = alignToGenome;
    }

    public void setMinOverlapLength(int minOverlapLength) {
        this.minOverlapLength = minOverlapLength;
    }

    public double getCoverageRatio() {
        return coverageRatio;
    }

    public void setCoverageRatio(double ratio) {
        coverageRatio = ratio;
    }

    public double getMFE_ratioCutoff() {
        return MFE_ratioCutoff;
    }

    public void setMFE_ratioCutoff(double MFE_ratioCutoff) {
        this.MFE_ratioCutoff = MFE_ratioCutoff;
    }

    public int getMinSmallRNALength() {
        return minSmallRNALength;
    }

    public void setMinSmallRNALength(int minSmallRNALength) {
        this.minSmallRNALength = minSmallRNALength;
    }

    public int getMaxSmallRNALength() {
        return maxSmallRNALength;
    }

    public void setMaxSmallRNALength(int maxSmallRNALength) {
        this.maxSmallRNALength = maxSmallRNALength;
    }

    public int getMinTagLength() {
        return minTagLength;
    }

    public void setMinTagLength(int minTagLength) {
        this.minTagLength = minTagLength;
    }

    public int getMaxTagLength() {
        return maxTagLength;
    }

    public void setMaxTagLength(int maxTagLength) {
        this.maxTagLength = maxTagLength;
    }

    public int getMinSmallRNAAbundance() {
        return minSmallRNAAbundance;
    }

    public void setMinSmallRNAAbundance(int minSmallRNAAbundance) {
        this.minSmallRNAAbundance = minSmallRNAAbundance;
    }

    public int getMinPhases() {
        return minPhases;
    }

    public void setMinPhases(int minPhases) {
        this.minPhases = minPhases;
    }

    public int getMinTagAbundance() {
        return minTagAbundance;
    }

    public void setMinTagAbundance(int minTagAbundance) {
        this.minTagAbundance = minTagAbundance;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumberOfThreads() {
        return numThreads;
    }

    double getLargestBubbleRegion() {
        return largestBubbleRegion;
    }

    public void setLargestBubbleRegion(double largestBubbleRegion) {
        this.largestBubbleRegion = largestBubbleRegion;
    }

    public boolean isLowComplexityFilter() {
        return lowComplexityFilter;
    }

    public void setLowComplexityFilter(boolean lowComplexityFilter) {
        this.lowComplexityFilter = lowComplexityFilter;
    }

    public boolean isCisOnly() {
        return cisOnly;
    }

    public void setCisOnly(boolean cisOnly) {
        this.cisOnly = cisOnly;
    }

    void reset() {
        config = null;
    }

    public void loadFromFile(File parameters) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(parameters));
            String line = br.readLine();
            minOverlapLength = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            minPhases = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            minSmallRNALength = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            maxSmallRNALength = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            minSmallRNAAbundance = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            minTagLength = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            maxTagLength = Integer.parseInt(line.split("=")[1].trim());
            line = br.readLine();
            cisOnly = Boolean.parseBoolean(line.split("=")[1].trim());
            line = br.readLine();
            coverageRatio = Double.parseDouble(line.split("=")[1].trim());
            line = br.readLine();
            largestBubbleRegion = Double.parseDouble(line.split("=")[1].trim());
            line = br.readLine();
            lowComplexityFilter = Boolean.parseBoolean(line.split("=")[1].trim());
            line = br.readLine();
            alignToGenome = Boolean.parseBoolean(line.split("=")[1].trim());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
