
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

import java.io.IOException;

/**
 * Stores and manages parameters for the PARESnip tool.
 */
public final class ParesnipParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each PARESnip parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        MINIMUM_SRNA_ABUNDANCE                ("min_sRNA_abundance",  Integer.valueOf(1),     Integer.valueOf(1),     Integer.valueOf(1000000000)),
        SUBSEQUENCES_ARE_SECONDARY_HITS       ("subsequences_are_secondary_hits", Boolean.FALSE),
        OUTPUT_SECONDARY_HITS_TO_FILE         ("output_secondary_hits_to_file",   Boolean.FALSE),
        USE_WEIGHTED_FRAGMENT_ABUNDANCE       ("use_weighted_fragments_abundance",Boolean.TRUE),
        CATEGORY_0                            ("category_0", Boolean.TRUE),
        CATEGORY_1                            ("category_1", Boolean.TRUE),
        CATEGORY_2                            ("category_2", Boolean.TRUE),
        CATEGORY_3                            ("category_3", Boolean.TRUE),
        CATEGORY_4                            ("category_4", Boolean.TRUE),
        DISCARD_TRRNA                         ("discard_tr_rna", Boolean.TRUE),
        DISCARD_LOW_COMPLEXITY_CANDIDATES     ("discard_low_complexity_candidates", Boolean.TRUE),
        DISCARD_LOW_COMPLEXITY_SRNAS          ("discard_low_complexity_srnas", Boolean.TRUE),
        MINIMUM_FRAGMENT_LENGTH               ("min_fragment_length", Integer.valueOf(20),    Integer.valueOf(1),     Integer.valueOf(1000000000)),
        MAXIMUM_FRAGMENT_LENGTH               ("max_fragment_length", Integer.valueOf(21),    Integer.valueOf(1),     Integer.valueOf(1000000000)),
        MINIMUM_SRNA_LENGTH                   ("min_sRNA_length",     Integer.valueOf(19),    Integer.valueOf(19),    Integer.valueOf(24)),
        MAXIMUM_SRNA_LENGTH                   ("max_sRNA_length",     Integer.valueOf(24),    Integer.valueOf(19),    Integer.valueOf(24)),
        ALLOW_SINGLE_NT_GAP                   ("allow_single_nt_gap", Boolean.TRUE),
        ALLOW_MISMATCH_POSITION_11            ("allow_mismatch_position_11", Boolean.TRUE),
        ALLOW_ADJACENT_MISMATCHES             ("allow_adjacent_mismatches", Boolean.TRUE),
        MAXIMUM_MISMATCHES                    ("max_mismatches", Double.valueOf(4.5), Double.valueOf(0.0), Double.valueOf(7.0)),
        CALCULATE_PVALUES                     ("calculate_pvalues", Boolean.TRUE),
        NUMBER_OF_SHUFFLES                    ("number_of_shuffles", Integer.valueOf(100), Integer.valueOf(10), Integer.valueOf(1000)),
        PVALUE_CUTOFF                         ("pvalue_cutoff", Double.valueOf(0.05), Double.valueOf(0.0), Double.valueOf(1.0)),
        DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF ("do_not_include_if_greater_than_cutoff", Boolean.TRUE),
        NUMBER_OF_THREADS                     ("number_of_threads", Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1000000000)),
        AUTO_OUTPUT_TPLOT_PDF                 ("auto_output_tplot_pdf", Boolean.FALSE );

        private ParameterDefinition definition;

        private Definition(String name, Boolean default_value)                                  {this.definition = new ParameterDefinition<Boolean>( name, default_value );}
        private Definition(String name, Integer default_value, Integer lower, Integer upper)    {this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper );}
        private Definition(String name, Double default_value, Double lower, Double upper)       {this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper );}

        // ***** Shortcuts to defaults and limits *****
        public String   getName()                       {return this.definition.getName();}
        public <T> T    getDefault(Class<T> type)       {return type.cast(this.definition.getDefaultValue());}
        public <T> T    getLowerLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getLower());}
        public <T> T    getUpperLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getUpper());}

        public ParameterDefinition getDefinition()      {return this.definition;}
    }


    /**
     * Default constructor.  Produces a FilterParams instance with default parameters.
     */
    public ParesnipParams() {
        this(new Builder());
    }

    /**
     * Assignment constructor using the Builder design pattern.  Should a client
     * wish to use this constructor directly, they are required to pass a ParesnipParams.Builder
     * object as an argument, which they can use to pick and choose parameters that
     * are not default.
     * All other constructors must go through this constructor.
     * @param builder A ParesnipParams.Builder object containing the ParesnipParams to
     * use.
     */
    private ParesnipParams(Builder builder) {

        setMinSrnaAbundance(builder.getMin_sRNA_abundance());
        setSubsequenceSecondaryHit(builder.isSubsequences_are_secondary_hits());
        setSecondaryOutputToFile(builder.isOutput_secondary_hits_to_file());
        setIsWeightedFragmentAbundance(builder.isUse_weighted_fragments_abundance());
        setCategory0(builder.isCategory_0());
        setCategory1(builder.isCategory_1());
        setCategory2(builder.isCategory_2());
        setCategory3(builder.isCategory_3());
        setCategory4(builder.isCategory_4());
        setDiscardTrrna(builder.isDiscardTrrna());
        setDiscardLowComplexitySRNAs(builder.isDiscard_low_complexity_srnas());
        setDiscardLowComplexityCandidates(builder.isDiscard_low_complexity_candidates());
        setMinFragmentLength(builder.getMin_fragment_length());
        setMaxFragmentLength(builder.getMax_fragment_length());
        setMinSrnaLength(builder.getMin_sRNA_length());
        setMaxSrnaLength(builder.getMax_sRNA_length());
        setAllowSingleNtGap(builder.isAllow_single_nt_gap());
        setAllowMismatchAtPositionEleven(builder.isAllow_mismatch_position_11());
        setAllowAdjacentMismatches(builder.isAllow_adjacent_mismatches());
        setMaxMismatches(builder.getMax_mismatches());
        setCalculatePvalues(builder.isCalculate_pvalues());
        setShuffleCount(builder.getNumber_of_shuffles());
        setPvalueCutoff(builder.getPvalue_cutoff());
        setNotIncludePvalueGrtCutoff(builder.isDo_not_include_if_greater_than_cutoff());
        setThreadCount(builder.getNumber_of_threads());
        setAutoOutputTplotPdf( builder.getAutoOutputTplotPdf() );
    }

    /**
     *
     * @param min_sRNA_abundance
     * @param subsequences_are_secondary_hits
     * @param output_secondary_hits_to_file
     * @param use_weighted_fragments_abundance
     * @param category_0
     * @param category_1
     * @param category_2
     * @param category_3
     * @param category_4
     * @param discard_low_complexity_srnas
     * @param discard_low_complexity_candidates
     * @param min_fragment_length
     * @param max_fragment_length
     * @param min_sRNA_length
     * @param max_sRNA_length
     * @param allow_single_nt_gap
     * @param allow_mismatch_position_11
     * @param allow_adjacent_mismatches
     * @param max_mismatches
     * @param calculate_p-values
     * @param number_of_shuffles
     * @param pvalue_cutoff
     * @param do_not_include_if_greater_than_cutoff
     * @param number_of_threads
     * @param discard_tr_rna
     * @throws IllegalArgumentException
     * @throws IOException
     */
/*
    public ParesnipParams( int min_sRNA_abundance, boolean subsequences_are_secondary_hits, boolean output_secondary_hits_to_file, boolean use_weighted_fragments_abundance,
         boolean category_0, boolean category_1, boolean category_2, boolean category_3, boolean category_4, boolean discard_low_complexity_srnas,
         boolean discard_low_complexity_candidates, int min_fragment_length, int max_fragment_length, int min_sRNA_length, int max_sRNA_length,
         boolean allow_single_nt_gap, boolean allow_mismatch_position_11, boolean allow_adjacent_mismatches, double max_mismatches,
         boolean calculate_pvalues, int number_of_shuffles, double pvalue_cutoff, boolean do_not_include_if_greater_than_cutoff,
         int number_of_threads, boolean discard_tr_rna) throws IllegalArgumentException, IOException
      {
        this(new Builder()
           .setAllow_adjacent_mismatches(allow_adjacent_mismatches)
           .setAllow_mismatch_position_11(allow_mismatch_position_11 )
           .setAllow_single_nt_gap(allow_single_nt_gap )
           .setCalculate_pvalues(calculate_pvalues )
           .setCategory_0(category_0 )
           .setCategory_1(category_1 )
           .setCategory_2(category_2 )
           .setCategory_3(category_3 )
           .setCategory_4(category_4 )
           .setDiscard_low_complexity_candidates(  discard_low_complexity_candidates )
           .setDiscard_low_complexity_srnas(discard_low_complexity_srnas )
           .setDo_not_include_if_greater_than_cutoff(  do_not_include_if_greater_than_cutoff )
           .setMax_fragment_length(max_fragment_length )
           .setMax_mismatches(max_mismatches )
           .setMax_sRNA_length(max_sRNA_length )
           .setMin_fragment_length(min_fragment_length )
           .setMin_sRNA_abundance(min_sRNA_abundance )
           .setMin_sRNA_length(min_sRNA_length )
           .setNumber_of_shuffles(number_of_shuffles )
           .setNumber_of_threads(number_of_threads )
           .setOutput_secondary_hits_to_file(output_secondary_hits_to_file )
           .setPvalue_cutoff(pvalue_cutoff )
           .setSubsequences_are_secondary_hits(subsequences_are_secondary_hits )
           .setUse_weighted_fragments_abundance(use_weighted_fragments_abundance )
           .setDiscardTrrna( discard_tr_rna )

        );
    }
*/

    // **** Helpers ****
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getMinSrnaAbundance()             {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_ABUNDANCE.getName());}
    public boolean  isSubsequenceSecondaryHit()       {return getParameterValue(Boolean.class, Definition.SUBSEQUENCES_ARE_SECONDARY_HITS.getName());}
    public boolean  isSecondaryOutputToFile()         {return getParameterValue(Boolean.class, Definition.OUTPUT_SECONDARY_HITS_TO_FILE.getName());}
    public boolean  isWeightedFragmentAbundance()     {return getParameterValue(Boolean.class, Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE.getName());}
    public boolean  isCategory0()                     {return getParameterValue(Boolean.class, Definition.CATEGORY_0.getName());}
    public boolean  isCategory1()                     {return getParameterValue(Boolean.class, Definition.CATEGORY_1.getName());}
    public boolean  isCategory2()                     {return getParameterValue(Boolean.class, Definition.CATEGORY_2.getName());}
    public boolean  isCategory3()                     {return getParameterValue(Boolean.class, Definition.CATEGORY_3.getName());}
    public boolean  isCategory4()                     {return getParameterValue(Boolean.class, Definition.CATEGORY_4.getName());}
    public boolean  isDiscardLowComplexitySRNAs()     {return getParameterValue(Boolean.class, Definition.DISCARD_LOW_COMPLEXITY_SRNAS.getName());}
    public boolean  isDiscardLowComplexityCandidates(){return getParameterValue(Boolean.class, Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES.getName());}
    public int      getMinFragmentLength()            {return getParameterValue(Integer.class, Definition.MINIMUM_FRAGMENT_LENGTH.getName());}
    public int      getMaxFragmentLength()            {return getParameterValue(Integer.class, Definition.MAXIMUM_FRAGMENT_LENGTH.getName());}
    public int      getMinSrnaLength()                {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_LENGTH.getName());}
    public int      getMaxSrnaLength()                {return getParameterValue(Integer.class, Definition.MAXIMUM_SRNA_LENGTH.getName());}
    public boolean  isAllowSingleNtGap()              {return getParameterValue(Boolean.class, Definition.ALLOW_SINGLE_NT_GAP.getName());}
    public boolean  isAllowMismatchAtPositionEleven() {return getParameterValue(Boolean.class, Definition.ALLOW_MISMATCH_POSITION_11.getName());}
    public boolean  isAllowAdjacentMismatches()       {return getParameterValue(Boolean.class, Definition.ALLOW_ADJACENT_MISMATCHES.getName());}
    public double   getMaxMismatches()                {return getParameterValue(Double.class, Definition.MAXIMUM_MISMATCHES.getName());}
    public boolean  isCalculatePvalues()              {return getParameterValue(Boolean.class, Definition.CALCULATE_PVALUES.getName());}
    public int      getShuffleCount()                 {return getParameterValue(Integer.class, Definition.NUMBER_OF_SHUFFLES.getName());}
    public double   getPvalueCutoff()                 {return getParameterValue(Double.class, Definition.PVALUE_CUTOFF.getName());}
    public boolean  isNotIncludePvalueGrtCutoff()     {return getParameterValue(Boolean.class, Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF.getName());}
    public int      getThreadCount()                  {return getParameterValue(Integer.class, Definition.NUMBER_OF_THREADS.getName());}
    public boolean  isDiscardTrrna()                  {return getParameterValue(Boolean.class, Definition.DISCARD_TRRNA.getName());}
    public boolean  getAutoOutputTplotPdf()           {return getParameterValue(Boolean.class, Definition.AUTO_OUTPUT_TPLOT_PDF.getName());}

    // **** Setters ****
    public void  setMinSrnaAbundance(int minAbundance)                              {setParam(Definition.MINIMUM_SRNA_ABUNDANCE, minAbundance);}
    public void  setSubsequenceSecondaryHit(boolean isSubsequenceSecondary)         {setParam(Definition.SUBSEQUENCES_ARE_SECONDARY_HITS, isSubsequenceSecondary);}
    public void  setSecondaryOutputToFile(boolean isSecondaryToFile)                {setParam(Definition.OUTPUT_SECONDARY_HITS_TO_FILE, isSecondaryToFile);}
    public void  setIsWeightedFragmentAbundance(boolean isWeightedFragmentAbundance){setParam(Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE, isWeightedFragmentAbundance);}
    public void  setCategory0(boolean isCategory0)                                  {setParam(Definition.CATEGORY_0, isCategory0);}
    public void  setCategory1(boolean isCategory1)                                  {setParam(Definition.CATEGORY_1, isCategory1);}
    public void  setCategory2(boolean isCategory2)                                  {setParam(Definition.CATEGORY_2, isCategory2);}
    public void  setCategory3(boolean isCategory3)                                  {setParam(Definition.CATEGORY_3, isCategory3);}
    public void  setCategory4(boolean isCategory4)                                  {setParam(Definition.CATEGORY_4, isCategory4);}
    public void  setDiscardLowComplexitySRNAs(boolean discardLowComplexity)         {setParam(Definition.DISCARD_LOW_COMPLEXITY_SRNAS, discardLowComplexity);}
    public void  setDiscardLowComplexityCandidates(boolean discardLowComplexity)    {setParam(Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES, discardLowComplexity);}
    public void  setMinFragmentLength(int minFragmentLength)                        {setParam(Definition.MINIMUM_FRAGMENT_LENGTH, minFragmentLength);}
    public void  setMaxFragmentLength(int maxFragmentLength)                        {setParam(Definition.MAXIMUM_FRAGMENT_LENGTH, maxFragmentLength);}
    public void  setMinSrnaLength(int minSrnaLength)                                {setParam(Definition.MINIMUM_SRNA_LENGTH, minSrnaLength);}
    public void  setMaxSrnaLength(int maxSrnaLength)                                {setParam(Definition.MAXIMUM_SRNA_LENGTH, maxSrnaLength);}
    public void  setAllowSingleNtGap(boolean allowGap)                              {setParam(Definition.ALLOW_SINGLE_NT_GAP, allowGap);}
    public void  setAllowMismatchAtPositionEleven(boolean allowMismatch)            {setParam(Definition.ALLOW_MISMATCH_POSITION_11, allowMismatch);}
    public void  setAllowAdjacentMismatches(boolean allowMismatch)                  {setParam(Definition.ALLOW_ADJACENT_MISMATCHES, allowMismatch);}
    public void  setMaxMismatches(double max)                                       {setParam(Definition.MAXIMUM_MISMATCHES, max);}
    public void  setCalculatePvalues(boolean usePvalues)                            {setParam(Definition.CALCULATE_PVALUES, usePvalues);}
    public void  setShuffleCount(int numShuffles)                                   {setParam(Definition.NUMBER_OF_SHUFFLES, numShuffles);}
    public void  setPvalueCutoff(double cutOff)                                     {setParam(Definition.PVALUE_CUTOFF, cutOff);}
    public void  setNotIncludePvalueGrtCutoff(boolean useCutoff)                    {setParam(Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF, useCutoff);}
    public void  setThreadCount(int numThreads)                                     {setParam(Definition.NUMBER_OF_THREADS, numThreads);}
    public void  setDiscardTrrna(boolean discard)                                   {setParam(Definition.DISCARD_TRRNA, discard);}
    public void  setAutoOutputTplotPdf(boolean b)                                   {setParam(Definition.AUTO_OUTPUT_TPLOT_PDF, b);}

    public void setSrnaLengthRange(int min_length, int max_length) {
        if (min_length > max_length) {
            throw new IllegalArgumentException("(sRNA) Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinSrnaLength(min_length);
        setMaxSrnaLength(max_length);
    }

    public void setFragmentLengthRange(int min_length, int max_length) {
        if (min_length > max_length) {
            throw new IllegalArgumentException("(fragment) Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinFragmentLength(min_length);
        setMaxFragmentLength(max_length);
    }

//    /**
//     * Loads a set of paresnip parameters from disk.
//     * @param file The file to load parameters from
//     * @return A FilterParams object with values set to those found in the file
//     * @throws IOException Thrown if there were any problems loading the file
//     */
//    public static ParesnipParams load(File file) throws IOException {
//        return (ParesnipParams)load(new ParesnipParams(), file);
//    }

    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at FilterParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int min_sRNA_abundance                    =   Definition.MINIMUM_SRNA_ABUNDANCE.getDefault(Integer.class);
        private boolean subsequences_are_secondary_hits   =   Definition.SUBSEQUENCES_ARE_SECONDARY_HITS.getDefault(Boolean.class);
        private boolean output_secondary_hits_to_file     =   Definition.OUTPUT_SECONDARY_HITS_TO_FILE.getDefault(Boolean.class);
        private boolean use_weighted_fragments_abundance  =   Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE.getDefault(Boolean.class);
        private boolean category_0                        =   Definition.CATEGORY_0.getDefault(Boolean.class);
        private boolean category_1                        =   Definition.CATEGORY_1.getDefault(Boolean.class);
        private boolean category_2                        =   Definition.CATEGORY_2.getDefault(Boolean.class);
        private boolean category_3                        =   Definition.CATEGORY_3.getDefault(Boolean.class);
        private boolean category_4                        =   Definition.CATEGORY_4.getDefault(Boolean.class);
        private boolean discard_low_complexity_srnas      =   Definition.DISCARD_LOW_COMPLEXITY_SRNAS.getDefault(Boolean.class);
        private boolean discard_low_complexity_candidates =   Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES.getDefault(Boolean.class);
        private int min_fragment_length                   =   Definition.MINIMUM_FRAGMENT_LENGTH.getDefault(Integer.class);
        private int max_fragment_length                   =   Definition.MAXIMUM_FRAGMENT_LENGTH.getDefault(Integer.class);
        private int min_sRNA_length                       =   Definition.MINIMUM_SRNA_LENGTH.getDefault(Integer.class);
        private int max_sRNA_length                       =   Definition.MAXIMUM_SRNA_LENGTH.getDefault(Integer.class);
        private boolean allow_single_nt_gap               =   Definition.ALLOW_SINGLE_NT_GAP.getDefault(Boolean.class);
        private boolean allow_mismatch_position_11        =   Definition.ALLOW_MISMATCH_POSITION_11.getDefault(Boolean.class);
        private boolean allow_adjacent_mismatches         =   Definition.ALLOW_ADJACENT_MISMATCHES.getDefault(Boolean.class);
        private double max_mismatches                     =   Definition.MAXIMUM_MISMATCHES.getDefault(Double.class);
        private boolean calculate_pvalues                 =   Definition.CALCULATE_PVALUES.getDefault(Boolean.class);
        private int number_of_shuffles                    =   Definition.NUMBER_OF_SHUFFLES.getDefault(Integer.class);
        private double pvalue_cutoff                      =   Definition.PVALUE_CUTOFF.getDefault(Double.class);
        private boolean do_not_include_if_greater_than_cutoff = Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF.getDefault(Boolean.class);
        private int number_of_threads                     =   Definition.NUMBER_OF_THREADS.getDefault(Integer.class);
        private boolean discard_tr_rna                    =   Definition.DISCARD_TRRNA.getDefault( Boolean.class );
        private boolean auto_output_tplot_pdf             =   Definition.AUTO_OUTPUT_TPLOT_PDF.getDefault( Boolean.class );

        // **** Getters ****
        public boolean isAllow_adjacent_mismatches()            { return allow_adjacent_mismatches;  }
        public boolean isAllow_mismatch_position_11()           { return allow_mismatch_position_11; }
        public boolean isAllow_single_nt_gap()                  { return allow_single_nt_gap; }
        public boolean isCalculate_pvalues()                    { return calculate_pvalues; }
        public boolean isCategory_0()                           { return category_0; }
        public boolean isCategory_1()                           { return category_1; }
        public boolean isCategory_2()                           { return category_2; }
        public boolean isCategory_3()                           { return category_3; }
        public boolean isCategory_4()                           { return category_4; }
        public boolean isDiscard_low_complexity_candidates()    { return discard_low_complexity_candidates; }
        public boolean isDiscard_low_complexity_srnas()         { return discard_low_complexity_srnas; }
        public boolean isDo_not_include_if_greater_than_cutoff(){ return do_not_include_if_greater_than_cutoff; }
        public int getMax_fragment_length()                     { return max_fragment_length; }
        public double getMax_mismatches()                       { return max_mismatches; }
        public int getMax_sRNA_length()                         { return max_sRNA_length; }
        public int getMin_fragment_length()                     { return min_fragment_length; }
        public int getMin_sRNA_abundance()                      { return min_sRNA_abundance; }
        public int getMin_sRNA_length()                         { return min_sRNA_length; }
        public int getNumber_of_shuffles()                      { return number_of_shuffles; }
        public int getNumber_of_threads()                       { return number_of_threads; }
        public boolean isOutput_secondary_hits_to_file()        { return output_secondary_hits_to_file; }
        public double getPvalue_cutoff()                        { return pvalue_cutoff; }
        public boolean isSubsequences_are_secondary_hits()      { return subsequences_are_secondary_hits; }
        public boolean isUse_weighted_fragments_abundance()     { return use_weighted_fragments_abundance; }
        public boolean isDiscardTrrna()                         { return discard_tr_rna; }
        public boolean  getAutoOutputTplotPdf()                 { return auto_output_tplot_pdf; }

         // **** Setters ****
        public Builder setAllow_adjacent_mismatches( boolean allow_adjacent_mismatches )                 { this.allow_adjacent_mismatches = allow_adjacent_mismatches; return this;}
        public Builder setAllow_mismatch_position_11( boolean allow_mismatch_position_11 )               { this.allow_mismatch_position_11 = allow_mismatch_position_11; return this;}
        public Builder setAllow_single_nt_gap( boolean allow_single_nt_gap )                             { this.allow_single_nt_gap = allow_single_nt_gap; return this; }
        public Builder setCalculate_pvalues( boolean calculate_pvalues )                                 { this.calculate_pvalues = calculate_pvalues; return this;}
        public Builder setCategory_0( boolean category_0 )                                               { this.category_0 = category_0;return this; }
        public Builder setCategory_1( boolean category_1 )                                               { this.category_1 = category_1;return this; }
        public Builder setCategory_2( boolean category_2 )                                               { this.category_2 = category_2;return this; }
        public Builder setCategory_3( boolean category_3 )                                               { this.category_3 = category_3;return this; }
        public Builder setCategory_4( boolean category_4 )                                               { this.category_4 = category_4;return this; }
        public Builder setDiscard_low_complexity_candidates( boolean discard_low_complexity_candidates ) { this.discard_low_complexity_candidates = discard_low_complexity_candidates;return this; }
        public Builder setDiscard_low_complexity_srnas( boolean discard_low_complexity_srnas )           { this.discard_low_complexity_srnas = discard_low_complexity_srnas;return this; }
        public Builder setDo_not_include_if_greater_than_cutoff( boolean do_not_include_if_greater_than_cutoff ) { this.do_not_include_if_greater_than_cutoff = do_not_include_if_greater_than_cutoff;return this; }
        public Builder setMax_fragment_length( int max_fragment_length )                                 { this.max_fragment_length = max_fragment_length; return this; }
        public Builder setMax_mismatches( double max_mismatches )                                        { this.max_mismatches = max_mismatches; return this; }
        public Builder setMax_sRNA_length( int max_sRNA_length )                                         { this.max_sRNA_length = max_sRNA_length; return this; }
        public Builder setMin_fragment_length( int min_fragment_length )                                 { this.min_fragment_length = min_fragment_length; return this; }
        public Builder setMin_sRNA_abundance( int min_sRNA_abundance )                                   { this.min_sRNA_abundance = min_sRNA_abundance; return this; }
        public Builder setMin_sRNA_length( int min_sRNA_length )                                         { this.min_sRNA_length = min_sRNA_length;return this; }
        public Builder setNumber_of_shuffles( int number_of_shuffles )                                   { this.number_of_shuffles = number_of_shuffles;return this; }
        public Builder setNumber_of_threads( int number_of_threads )                                     { this.number_of_threads = number_of_threads;return this; }
        public Builder setOutput_secondary_hits_to_file( boolean output_secondary_hits_to_file )         { this.output_secondary_hits_to_file = output_secondary_hits_to_file; return this; }
        public Builder setPvalue_cutoff( double pvalue_cutoff )                                          { this.pvalue_cutoff = pvalue_cutoff;return this; }
        public Builder setSubsequences_are_secondary_hits( boolean subsequences_are_secondary_hits )     { this.subsequences_are_secondary_hits = subsequences_are_secondary_hits; return this; }
        public Builder setUse_weighted_fragments_abundance( boolean use_weighted_fragments_abundance )   { this.use_weighted_fragments_abundance = use_weighted_fragments_abundance; return this; }
        public Builder setDiscardTrrna(boolean discard )                                                 { this.discard_tr_rna = discard; return this; }
        public Builder setAutoOutputTplotPdf(boolean b)                                                  { this.auto_output_tplot_pdf = b; return this; }

        /**
         * Constructs the ParesnipParams object
         * @return
         */
        public ParesnipParams build() {
          return new ParesnipParams( this );
        }
    }
}
