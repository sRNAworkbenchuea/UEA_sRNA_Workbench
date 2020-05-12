/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class contains the configuration for a given PAREameters run.
 * It has some default values but can be configured by the user.
 * @author rew13hpu
 */
public class PAREametersConfiguration {

    private static PAREametersConfiguration parameters;

    boolean useWeightedAbundance;
    boolean lowComplexityFilter;
    double retainRate;
    int minSmallRNAAbundance;
    int minSmallRNALength;
    int maxSmallRNALength;
    int minTagLength;
    int maxTagLength;
    boolean allowedCategories[];
    double mfeRatio;
    int minTagAbundance;

    public static PAREametersConfiguration getInstance() {
        if (parameters == null) {
            parameters = new PAREametersConfiguration();
        }
        return parameters;
    }

    private PAREametersConfiguration() {
        this.useWeightedAbundance = false;
        this.minSmallRNAAbundance = 5;
        this.minTagAbundance = 5;
        this.minSmallRNALength = 19;
        this.maxSmallRNALength = 24;
        this.minTagLength = 19;
        this.maxTagLength = 21;
        retainRate = 0.85;
        mfeRatio = 0.6;
        lowComplexityFilter = true;
        allowedCategories = new boolean[5];
        allowedCategories[0] = true;
        allowedCategories[1] = true;
        allowedCategories[2] = true;
        allowedCategories[3] = true;
    }

    public double getRetainRate() {
        return retainRate;
    }

    public void setRetainRate(double retainRate) {
        this.retainRate = retainRate;
    }
    
    

    public void setPermittedCategory(int i) {
        if (i < allowedCategories.length) {
            allowedCategories[i] = true;
        }
    }

    public void setForbiddenCategory(int i) {
        if (i < allowedCategories.length) {
            allowedCategories[i] = false;
        }
    }
    
    public boolean isCategoryPermitted(int i)
    {
        if (i < allowedCategories.length) {
            return allowedCategories[i];
        }
        
        return false;
    }

    public boolean isUseWeightedAbundance() {
        return useWeightedAbundance;
    }

    public void setUseWeightedAbundance(boolean useWeightedAbundance) {
        this.useWeightedAbundance = useWeightedAbundance;
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

    public double getMfeRatio() {
        return mfeRatio;
    }

    public void setMfeRatio(double mfeRatio) {
        this.mfeRatio = mfeRatio;
    }

    public int getMinTagAbundance() {
        return minTagAbundance;
    }

    public void setMinTagAbundance(int minTagAbundance) {
        this.minTagAbundance = minTagAbundance;
    }

    public void loadFromFile(File file) {

         

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            allowedCategories = new boolean[5];
            
            this.setUseWeightedAbundance(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));
            this.setMinSmallRNAAbundance(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMinTagAbundance(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMinSmallRNALength(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxSmallRNALength(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMinTagLength(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxTagLength(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setRetainRate(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMfeRatio(Double.parseDouble(reader.readLine().split("=")[1].trim()));
             this.setAllowedCategories(0, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(1, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(2, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(3, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(4, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setLowComplexityFilter(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));

            

            
        } catch (IOException | NumberFormatException ex) {
            System.out.println("There was an error reading the config file. Please generate a new one or use the default file provided.");
            System.exit(1);
        }

    }void setAllowedCategories(int cat, boolean  val)
    {
        allowedCategories[cat] = val;
    }

    boolean isLowComplexityFilter() {
return lowComplexityFilter;
    }
    
    void setLowComplexityFilter(boolean value)
    {
        this.lowComplexityFilter = value;
    }

    
    
    
    
    

}
