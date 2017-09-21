
package uk.ac.uea.cmp.srnaworkbench.utils.patman;


import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;


/**
 * Stores and manages parameters for miRProf
 * @author ezb11yfu
 */
public final class PatmanParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each MiRProf parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        MAXIMUM_MISMATCHES          ("max_mismatches",        Integer.valueOf(0),    Integer.valueOf(0),    Integer.valueOf(3)),
        MAXIMUM_GAPS                ("max_gaps",              Integer.valueOf(0),    Integer.valueOf(0),    Integer.valueOf(3)),
        POSITIVE_STRAND_ONLY        ("positive_strand_only",    Boolean.FALSE),
        NEGATIVE_STRAND_ONLY        ("negative_strand_only",    Boolean.FALSE),
        CHUNK_SIZE                  ("chunk_size",            Integer.valueOf(3000000), Integer.valueOf(100000), Integer.MAX_VALUE ),
        PRE_PROCESS                 ("pre_process",           Boolean.FALSE),
        MAKE_NR                     ("make_nr",           Boolean.TRUE),
        MINIMUM_SRNA_LENGTH         ("min_length",            Integer.valueOf(16),   Integer.valueOf(16),   Integer.valueOf(49)),
        MAXIMUM_SRNA_LENGTH         ("max_length",            Integer.valueOf(35),   Integer.valueOf(17),   Integer.valueOf(50)),
        MINIMUM_SRNA_ABUNDANCE      ("min_abundance",         Integer.valueOf(1),    Integer.valueOf(1),    Integer.MAX_VALUE),
        POST_PROCESS                ("post_process",          Boolean.FALSE),
        MINIMUM_WEIGHTED_ABUNDANCE  ("min_weighted_abd",      Double.valueOf(0.0),   Double.valueOf(0.0),   Double.MAX_VALUE);
                        
        private ParameterDefinition definition;

        private Definition(String name, Boolean default_value)                                  {this.definition = new ParameterDefinition<Boolean>( name, default_value );}
        private Definition(String name, String default_value)                                   {this.definition = new ParameterDefinition<String>( name, default_value );}
        private Definition(String name, File default_value)                                     {this.definition = new ParameterDefinition<File>( name, default_value );}
        private Definition(String name, Integer default_value, Integer lower, Integer upper)    {this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper );}
        private Definition(String name, Float default_value, Float lower, Float upper)          {this.definition = new ParameterDefinition<Float>( name, default_value, lower, upper );}
        private Definition(String name, Double default_value, Double lower, Double upper)       {this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper );}
        
        // ***** Shortcuts to defaults and limits *****
        public String   getName()                       {return this.definition.getName();}
        public <T> T    getDefault(Class<T> type)       {return type.cast(this.definition.getDefaultValue());}
        public <T> T    getLowerLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getLower());}
        public <T> T    getUpperLimit(Class<T> type)    {return type.cast(this.definition.getLimits().getUpper());}
        
        public ParameterDefinition getDefinition()      {return this.definition;}
    }
    
    
    /**
     * Default constructor.  Produces a MirprofParams instance with default parameters.
     */
    public PatmanParams()
    {
        this(new Builder());
    }
    
    /**
     * Assignment constructor using the Builder design pattern. Only for internal
     * use.  Client's must use the Builder.build() to create a MirprofParams object
     * or use the assignment constructor.
     * @param builder 
     */
    private PatmanParams(Builder builder)
    {
        setMaxMismatches(builder.getMaxMismatches());
        setMaxGaps(builder.getMaxGaps());
        setPositiveStrandOnly(builder.getPositiveStrandOnly());
        setNegativeStrandOnly(builder.getNegativeStrandOnly());
        setChunkSize(builder.getChunkSize());
        setPreProcess(builder.getPreProcess());
        setMakeNR(builder.getMakeNR());
        setSRNALengthRange(builder.getMinSRNALength(), builder.getMaxSRNALength());
        setMinSRNAAbundance(builder.getMinSRNAAbundance());
        setPostProcess(builder.getPostProcess());
        setMinWeightedAbundance(builder.getMinWeightedAbundance());
    }
    

    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getMaxMismatches()          {return getParameterValue(Integer.class, Definition.MAXIMUM_MISMATCHES.getName());}
    public int      getMaxGaps()                {return getParameterValue(Integer.class, Definition.MAXIMUM_GAPS.getName());}
    public boolean  getPositiveStrandOnly()     {return getParameterValue(Boolean.class, Definition.POSITIVE_STRAND_ONLY.getName());}
    public boolean  getNegativeStrandOnly()     {return getParameterValue(Boolean.class, Definition.NEGATIVE_STRAND_ONLY.getName());}
    public int      getChunkSize()              {return getParameterValue(Integer.class, Definition.CHUNK_SIZE.getName());}
    
    public boolean  getPreProcess()             {return getParameterValue(Boolean.class, Definition.PRE_PROCESS.getName());}
    public boolean  getMakeNR()                 {return getParameterValue(Boolean.class, Definition.MAKE_NR.getName());}
    public int      getMinSRNALength()          {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_LENGTH.getName());}
    public int      getMaxSRNALength()          {return getParameterValue(Integer.class, Definition.MAXIMUM_SRNA_LENGTH.getName());}
    public int      getMinSRNAAbundance()       {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_ABUNDANCE.getName());}
    
    public boolean  getPostProcess()            {return getParameterValue(Boolean.class, Definition.POST_PROCESS.getName());}
    public double   getMinWeightedAbundance()   {return getParameterValue(Double.class, Definition.MINIMUM_WEIGHTED_ABUNDANCE.getName());}


    // **** Setters ****
    public void setMaxMismatches(int max_mismatches)                    {setParam(Definition.MAXIMUM_MISMATCHES, max_mismatches);}
    public void setMaxGaps(int max_gaps)                                {setParam(Definition.MAXIMUM_GAPS, max_gaps);}
    public void setPositiveStrandOnly(boolean pos_only)                 {setParam(Definition.POSITIVE_STRAND_ONLY, pos_only);}
    public void setNegativeStrandOnly(boolean neg_only)                 {setParam(Definition.NEGATIVE_STRAND_ONLY, neg_only);}
    public void setChunkSize(int chunk_size)                            
    {
      int mod = chunk_size % 2;
      if (mod != 0)
      {
        throw new IllegalArgumentException("Illegal chunk_size parameter value.  Valid values: any even integer greater than " + 
          Definition.CHUNK_SIZE.getUpperLimit( Integer.class ) );
      }
      setParam(Definition.CHUNK_SIZE, chunk_size);
    }
    
    public void setPreProcess(boolean pre_process)                      {setParam(Definition.PRE_PROCESS, pre_process);}
    public void setMakeNR(boolean make_nr)              {setParam(Definition.MAKE_NR, make_nr);}
    private void setMinSRNALength(int min_length)                       {setParam(Definition.MINIMUM_SRNA_LENGTH, min_length);}
    private void setMaxSRNALength(int max_length)                       {setParam(Definition.MAXIMUM_SRNA_LENGTH, max_length);}
    public void setMinSRNAAbundance(int min_abundance)                  {setParam(Definition.MINIMUM_SRNA_ABUNDANCE, min_abundance);}
    
    public void setSRNALengthRange(int min_length, int max_length)
    {
        if (min_length > max_length)
        {
            throw new IllegalArgumentException("Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinSRNALength(min_length);
        setMaxSRNALength(max_length);
    }
    
    public void setPostProcess(boolean post_process)                    {setParam(Definition.POST_PROCESS, post_process);}
    public void setMinWeightedAbundance(double min_weighted_abd)        {setParam(Definition.MINIMUM_WEIGHTED_ABUNDANCE, min_weighted_abd);}

    
    public static final class Builder
    {
        private int     max_mismatches          = Definition.MAXIMUM_MISMATCHES.getDefault(Integer.class);
        private int     max_gaps                = Definition.MAXIMUM_GAPS.getDefault(Integer.class);
        private boolean positive_only           = Definition.POSITIVE_STRAND_ONLY.getDefault(Boolean.class);
        private boolean negative_only           = Definition.NEGATIVE_STRAND_ONLY.getDefault(Boolean.class);
        private int     chunk_size              = Definition.CHUNK_SIZE.getDefault(Integer.class);
        
        private boolean pre_process             = Definition.PRE_PROCESS.getDefault( Boolean.class );
        private boolean make_nr                 = Definition.MAKE_NR.getDefault( Boolean.class );
        private int     min_length              = Definition.MINIMUM_SRNA_LENGTH.getDefault(Integer.class);
        private int     max_length              = Definition.MAXIMUM_SRNA_LENGTH.getDefault(Integer.class);
        private int     min_abundance           = Definition.MINIMUM_SRNA_ABUNDANCE.getDefault(Integer.class);
        
        private boolean post_process            = Definition.POST_PROCESS.getDefault( Boolean.class );
        private double  min_weighted_abd        = Definition.MINIMUM_WEIGHTED_ABUNDANCE.getDefault(Double.class);

        
        // **** Getters ****
        public int      getMaxMismatches()          {return max_mismatches;}
        public int      getMaxGaps()                {return max_gaps;}
        public boolean  getPositiveStrandOnly()     {return positive_only;}
        public boolean  getNegativeStrandOnly()     {return negative_only;}
        public int      getChunkSize()              {return chunk_size;}
        
        public boolean  getPreProcess()             {return pre_process;}
        public boolean  getMakeNR()                 {return make_nr;}
        public int      getMinSRNALength()          {return min_length;}
        public int      getMaxSRNALength()          {return max_length;}
        public int      getMinSRNAAbundance()       {return min_abundance;}
        
        public boolean  getPostProcess()            {return post_process;}
        public double   getMinWeightedAbundance()   {return min_weighted_abd;}
        
        
        // **** Setters ****
        public Builder setMaxMismatches(int max_mismatches)                {this.max_mismatches = max_mismatches;               return this;}        
        public Builder setMaxGaps(int max_gaps)                            {this.max_gaps = max_gaps;                           return this;}        
        public Builder setPositiveStrandOnly(boolean pos_only)             {this.positive_only = pos_only;                      return this;}
        public Builder setNegativeStrandOnly(boolean neg_only)             {this.negative_only = neg_only;                      return this;}
        public Builder setChunkSize(int chunk_size)                        {this.chunk_size = chunk_size;                       return this;}
        
        public Builder setPreProcess(boolean pre_process)                  {this.pre_process = pre_process;                     return this;}
        public Builder setMinSRNALength(int min_length)                    {this.min_length = min_length;                       return this;}
        public Builder setMaxSRNALength(int max_length)                    {this.max_length = max_length;                       return this;}
        public Builder setMinSRNAAbundance(int min_abundance)              {this.min_abundance = min_abundance;                 return this;}
        
        public Builder setPostProcess(boolean post_process)                {this.post_process = post_process;                   return this;}
        public Builder setMinWeightedAbundance(double min_weighted_abd)    {this.min_weighted_abd = min_weighted_abd;           return this;}
        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public PatmanParams build()
        {
          return new PatmanParams( this );
        }
    }

    public static void main(String[] args)
    {
        try
        {
            PatmanParams ok = PatmanParams.load(new PatmanParams(), new File("C:\\LocalData\\Research\\RNA Tools\\miRProf\\mirprofparamtest1-ok.cfg"));
            PatmanParams ko = PatmanParams.load(new PatmanParams(), new File("C:\\LocalData\\Research\\RNA Tools\\miRProf\\mirprofparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
