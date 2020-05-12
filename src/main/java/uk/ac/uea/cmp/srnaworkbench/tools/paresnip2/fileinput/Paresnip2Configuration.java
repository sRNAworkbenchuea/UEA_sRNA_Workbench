/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import uk.ac.uea.cmp.srnaworkbench.tools.pareameters.PAREametersConfiguration;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;

/**
 *
 * @author Josh
 */
public final class Paresnip2Configuration {

    private boolean usingTranscriptome;
    private boolean usingGenomeGFF;
    private boolean alignSmallRNAsToGenome;
    private boolean useMfeFilter;
    private double pValueCutoff;
    private double mfe_ratio_cutoff;
    private boolean useWeightedFragmentAbundance;
    private boolean useConservationFilter;
    private boolean[] allowedCategories;
    private int numberOfThreads;
    private int minSmallRNALenth;
    private int maxSmallRNALenth;
    private int minFragmentLenth;
    private int maxFragmentLenth;
    private int minSmallRNAAbundance;
    private boolean usePValue;
    //NEED TO ADD THIS TO THE GUI AND CONFIGURATION FILE
    private boolean filterLowComplexitySeqs;
    private boolean includeUTR;
    private boolean verbose;

    private static Paresnip2Configuration config;

    public static void reset()
    {
        config = null;
    }
    
    public static Paresnip2Configuration getInstance() {
        if (config == null) {
            config = new Paresnip2Configuration();
        }

        return config;
    }

    private Paresnip2Configuration() {
        usingTranscriptome = true;
        usingGenomeGFF = false;
        includeUTR = false;
        alignSmallRNAsToGenome = false;
        useWeightedFragmentAbundance = false;
        useConservationFilter = false;
        filterLowComplexitySeqs = true;
        useMfeFilter = true;
        numberOfThreads = Runtime.getRuntime().availableProcessors();
        usePValue = true;
        verbose = true;

        allowedCategories = new boolean[5];

        setDefaultStringentParameters();
    }

    public void loadConfig(File configFile) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        allowedCategories = new boolean[5];

        try {
            this.setUsingTranscriptome(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setUsingGenomeGFF(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setIncludeUTR(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAlignSmallRNAsToGenome(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setUseConservationFilter(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setUseFilter(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setFilterCutoff(Double.parseDouble(reader.readLine().split("=")[1]));
            this.setUsePValue(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setpValueCutoff(Double.parseDouble(reader.readLine().split("=")[1]));
            this.setFilterLowComplexitySeqs(Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(0, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(1, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(2, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(3, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setAllowedCategories(4, Boolean.parseBoolean(reader.readLine().split("=")[1]));
            this.setNumberOfThreads(Integer.parseInt(reader.readLine().split("=")[1]));
            this.setMinSmallRNAAbundance(Integer.parseInt(reader.readLine().split("=")[1]));
            this.setMinSmallRNALenth(Integer.parseInt(reader.readLine().split("=")[1]));
            this.setMaxSmallRNALenth(Integer.parseInt(reader.readLine().split("=")[1]));
            this.setMinFragmentLenth(Integer.parseInt(reader.readLine().split("=")[1]));
            this.setMaxFragmentLenth(Integer.parseInt(reader.readLine().split("=")[1]));
        } catch (Exception ex) {
            throw new Exception("There was an error reading the configuration file. Please generate a new one or use the default file provided.");
        }

    }


    public double getpValueCutoff() {
        return pValueCutoff;
    }

    public boolean isFilterLowComplexitySeqs() {
        return filterLowComplexitySeqs;
    }

    public void setFilterLowComplexitySeqs(boolean filterLowComplexitySeqs) {
        this.filterLowComplexitySeqs = filterLowComplexitySeqs;
    }

    public void setpValueCutoff(double pValueCutoff) {
        this.pValueCutoff = pValueCutoff;
    }

    public boolean isUsePValue() {
        return usePValue;
    }

    public void setUsePValue(boolean usePValue) {
        this.usePValue = usePValue;
    }

    public double getFilterCutoff() {
        return mfe_ratio_cutoff;
    }

    public boolean isUsingTranscriptome() {
        return usingTranscriptome;
    }

    public void setUsingTranscriptome(boolean usingTranscriptome) {
        this.usingTranscriptome = usingTranscriptome;

    }

    public boolean isUsingGenomeGFF() {
        return usingGenomeGFF;
    }

    public void setUsingGenomeGFF(boolean usingGenomeGFF) {
        this.usingGenomeGFF = usingGenomeGFF;
    }

    public boolean isAlignSmallRNAsToGenome() {
        return alignSmallRNAsToGenome;
    }

    public void setAlignSmallRNAsToGenome(boolean alignSmallRNAsToGenome) {
        this.alignSmallRNAsToGenome = alignSmallRNAsToGenome;

    }

    public boolean isUseFilter() {
        return useMfeFilter;
    }

    public void setUseFilter(boolean useFilter) {
        this.useMfeFilter = useFilter;
    }

    public void setFilterCutoff(double filterCutoff) {
        this.mfe_ratio_cutoff = filterCutoff;
    }

    public boolean isUseWeightedFragmentAbundance() {
        return useWeightedFragmentAbundance;
    }

    public void setUseWeightedFragmentAbundance(boolean useWeightedFragmentAbundance) {
        this.useWeightedFragmentAbundance = useWeightedFragmentAbundance;
    }

    public boolean isUseConservationFilter() {
        return useConservationFilter;
    }

    public void setUseConservationFilter(boolean useConservationFilter) {
        this.useConservationFilter = useConservationFilter;
    }

    public boolean getAllowedCategories(int cat) {
        return allowedCategories[cat];
    }

    public void setAllowedCategories(int cat, boolean b) {
        this.allowedCategories[cat] = b;
    }

    public void setAllowedCategories(int cat) {
        this.allowedCategories[cat] = !allowedCategories[cat];
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getMinSmallRNALenth() {
        return minSmallRNALenth;
    }

    public void setMinSmallRNALenth(int minSmallRNALenth) {
        this.minSmallRNALenth = minSmallRNALenth;
    }

    public int getMaxSmallRNALenth() {
        return maxSmallRNALenth;
    }

    public void setMaxSmallRNALenth(int maxSmallRNALenth) {
        this.maxSmallRNALenth = maxSmallRNALenth;

    }

    public int getMinSmallRNAAbundance() {
        return minSmallRNAAbundance;
    }

    public void setMinSmallRNAAbundance(int minSmallRNAAbundance) {
        this.minSmallRNAAbundance = minSmallRNAAbundance;
    }

    public int getMinFragmentLenth() {
        return minFragmentLenth;
    }

    public void setMinFragmentLenth(int minFragmentLenth) {
        if(minFragmentLenth < 16)
        {
            System.out.println("Minimum fragment length has to be at least 16!");
            minFragmentLenth = 16;
        }
        this.minFragmentLenth = minFragmentLenth;
    }

    public int getMaxFragmentLenth() {
        return maxFragmentLenth;
    }

    public void setMaxFragmentLenth(int maxFragmentLenth) {
        
//        if(maxFragmentLenth > 30)
//        {
//            if(AppUtils.INSTANCE.isCommandLine())
//            {
//                System.out.println("PARE data is only supported up to 30nt in length.");
//                System.exit(1);
//            }
//        }
        
        this.maxFragmentLenth = maxFragmentLenth;
    }

    public String isSetThreeComplete() {

        Paresnip2InputFiles input = Paresnip2InputFiles.getInstance();
        String msg = "";
        if (input.getOutputDirectory() == null) {
            msg = "The output directory has not been selected!";
            return msg;
        }

        //TODO Check that the max >= the min for the settings here.....
        if (minFragmentLenth > maxFragmentLenth) {
            msg = "The minimum fragment length is greater than the maximum.";
            return msg;
        }

        if (minSmallRNALenth > maxSmallRNALenth) {
            msg = "The minimum sRNA length is greater than the maximum.";
            return msg;
        }

        return msg;

    }

    public boolean isIncludeUTR() {
        return includeUTR;
    }

    public void setIncludeUTR(boolean b) {
        this.includeUTR = b;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDefaultStringentParameters() {
        minSmallRNALenth = 19;
        maxSmallRNALenth = 24;
        minFragmentLenth = 19;
        maxFragmentLenth = 21;
        mfe_ratio_cutoff = 0.7;
        minSmallRNAAbundance = 5;
        pValueCutoff = 0.05;
        for (int i = 0; i < allowedCategories.length; i++) {
            allowedCategories[i] = true;
        }

        allowedCategories[4] = false;
    }
    
     public void setDefaultNATsiParameters() {
        minSmallRNALenth = 19;
        maxSmallRNALenth = 24;
        minFragmentLenth = 19;
        maxFragmentLenth = 21;
        mfe_ratio_cutoff = 0.6;
        minSmallRNAAbundance = 1;
        pValueCutoff = 1;
        filterLowComplexitySeqs = false;
        
        for (int i = 0; i < allowedCategories.length; i++) {
            allowedCategories[i] = true;
        }

        allowedCategories[4] = false;
    }

    public void setDefaultPAREAmetersParameters() {
        minFragmentLenth = 19;
        mfe_ratio_cutoff = 0.7;
        minSmallRNAAbundance = 2;
        useWeightedFragmentAbundance = false;
        for (int i = 2; i < allowedCategories.length; i++) {
            allowedCategories[i] = false;
        }
    }

    public void setPAREametersSearchParameters() {
        minFragmentLenth = 19;
        mfe_ratio_cutoff = 0.65;
        minSmallRNAAbundance = 5;
        useWeightedFragmentAbundance = false;
        for (int i = 2; i < allowedCategories.length; i++) {
            allowedCategories[i] = false;
        }
    }

    public void setDefaultFlexibleParameters() {
        minSmallRNALenth = 19;
        maxSmallRNALenth = 24;
        minFragmentLenth = 19;
        maxFragmentLenth = 21;
        mfe_ratio_cutoff = 0.7;
        minSmallRNAAbundance = 1;
        for (int i = 0; i < allowedCategories.length; i++) {
            allowedCategories[i] = true;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("use_transcriptome=").append(config.isUsingTranscriptome());
        sb.append(System.lineSeparator());
        sb.append("use_genome_gff=").append(config.isUsingGenomeGFF());
        sb.append(System.lineSeparator());
        sb.append("include_UTR=").append(config.isIncludeUTR());
        sb.append(System.lineSeparator());
        sb.append("align_sRNAs=").append(config.isAlignSmallRNAsToGenome());
        sb.append(System.lineSeparator());
        sb.append("use_conservation_filter=").append(config.isUseConservationFilter());
        sb.append(System.lineSeparator());
        sb.append("use_mfe_filter=").append(config.isUseFilter());
        sb.append(System.lineSeparator());
        sb.append("mfe_filter_cutoff=").append(config.getFilterCutoff());
        sb.append(System.lineSeparator());
        sb.append("use_p_value=").append(config.isUsePValue());
        sb.append(System.lineSeparator());
        sb.append("p_value_cutoff=").append(config.getpValueCutoff());
        sb.append(System.lineSeparator());
        sb.append("filter_low_complexity_seqs=").append(config.isFilterLowComplexitySeqs());
        sb.append(System.lineSeparator());
        sb.append("category_0=").append(config.getAllowedCategories(0));
        sb.append(System.lineSeparator());
        sb.append("category_1=").append(config.getAllowedCategories(1));
        sb.append(System.lineSeparator());
        sb.append("category_2=").append(config.getAllowedCategories(2));
        sb.append(System.lineSeparator());
        sb.append("category_3=").append(config.getAllowedCategories(3));
        sb.append(System.lineSeparator());
        sb.append("category_4=").append(config.getAllowedCategories(4));
        sb.append(System.lineSeparator());
        sb.append("number_of_threads=").append(config.getNumberOfThreads());
        sb.append(System.lineSeparator());
        sb.append("min_sRNA_abundance=").append(config.getMinSmallRNAAbundance());
        sb.append(System.lineSeparator());
        sb.append("min_sRNA_length=").append(config.getMinSmallRNALenth());
        sb.append(System.lineSeparator());
        sb.append("max_sRNA_length=").append(config.getMaxSmallRNALenth());
        sb.append(System.lineSeparator());
        sb.append("min_fragment_length=").append(config.getMinFragmentLenth());
        sb.append(System.lineSeparator());
        sb.append("max_fragment_length=").append(config.getMaxFragmentLenth());

        return sb.toString();
    }

    public void setConfig(PAREametersConfiguration pareametersconfig) {

        /**
         * minFragmentLenth = 19; mfe_ratio_cutoff = 0.65; minSmallRNAAbundance
         * = 5; useWeightedFragmentAbundance = false; for (int i = 2; i <
         * allowedCategories.length; i++) { allowedCategories[i] = false; }
         *
         */
        minFragmentLenth = pareametersconfig.getMinTagLength();
        maxFragmentLenth = pareametersconfig.getMaxTagLength();
        minSmallRNAAbundance = pareametersconfig.getMinSmallRNAAbundance();
        minSmallRNALenth = pareametersconfig.getMinSmallRNALength();
        maxSmallRNALenth = pareametersconfig.getMaxSmallRNALength();
        useWeightedFragmentAbundance = pareametersconfig.isUseWeightedAbundance();
        mfe_ratio_cutoff = pareametersconfig.getMfeRatio();

        for (int i = 0; i < allowedCategories.length; i++) {
            if (pareametersconfig.isCategoryPermitted(i)) {
                allowedCategories[i] = true;
            } else {
                allowedCategories[i] = false;
            }
        }

    }
}
