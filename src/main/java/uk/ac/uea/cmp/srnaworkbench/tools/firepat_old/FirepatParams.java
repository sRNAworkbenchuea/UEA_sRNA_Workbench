package uk.ac.uea.cmp.srnaworkbench.tools.firepat_old;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;


/**
 * Stores and manages parameters for the Filter tool.
 * @author ezb11yfu
 */
public final class FirepatParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each Filter parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
      /**
         * Discard entries that are not in the top N percent based on their overall
         * expression level.
         */
        DIFF_EXPR_THRESHOLD    ("de_threshold",    Integer.valueOf(5),    Integer.valueOf(1),    Integer.valueOf(100)),
        /**
         * Only consider correlated pair if distance between pair exceeds this percentage
         * threshold.
         */
        SIMILARITY_THRESHOLD    ("sim_threshold",    Integer.valueOf(5),    Integer.valueOf(1),    Integer.valueOf(100)),
        /**
         * Discard entries which have an overall expression value less than this
         * figure
         */
        MIN_EXP_LVL          ("min_exp_lvl",  Double.valueOf(0.0),   Double.MIN_VALUE,      Double.MAX_VALUE),
        /**
         * Discard entries which have an abundance less than this figure
         */
        MIN_ABUNDANCE        ("min_abundance", Integer.valueOf(1),    Integer.valueOf(1),    Integer.MAX_VALUE),
        /**
         * Method to determine expression level for sRNA/loci/gene across samples.
         * Allowed values: "OFFSET_FOLD_CHANGE", "UNUSUAL_RATIO" and "MODIFIED_SAM"
         */
        DE_METHOD            ("de_method",       "OFFSET_FOLD_CHANGE"),
        /**
         * Method to determine expression level for sRNA/loci/gene across samples.
         * Allowed values: "PEARSON_DISTANCE", "COSINE_DISTANCE", "SPEARMAN_RANK_DISTANCE" and "KENDALLS_TAU_DISTANCE"
         */
        SIM_METHOD           ("sim_method",      "SPEARMAN_RANK_DISTANCE"),
        /**
         * The amount of offset to apply to expression levels (used for some methods)
         */
        OFFSET               ("method_offset",       Double.valueOf(20),    Double.valueOf(0),     Double.MAX_VALUE ),
        /**
         * Whether the data set is ordered or not (ordered might be a time series, 
         * where as unordered might be a collection of random tissues).
         */
        ORDERED              ("ordered",      Boolean.FALSE ),
        /**
         * How to interpret the columns in the data matrix for the ModifiedSAM algorithm.
         * Each number, delimited by commas, represents the number of replicates
         * for a given sample.  Columns are grouped according to these numbers.
         * Therefore the sum of the number in this array should equal the number of
         * columns in the data matrix.
         */
        REPLICATE_COUNTS     ("replicate_counts", "1,1");
        
        
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
     * Default constructor.  Produces a FilterParams instance with default parameters.
     */
    public FirepatParams()
    {
        this(new Builder());
    }

    /**
     * Assignment constructor using the Builder design pattern.  Should a client
     * wish to use this constructor directly, they are required to pass a FilterParams.Builder
     * object as an argument, which they can use to pick and choose parameters that
     * are not default.
     * All other constructors must go through this constructor.
     * @param builder A FilterParams.Builder object containing the FilterParams to
     * use.
     */
    private FirepatParams(Builder builder)
    {
        setDEThreshold(builder.getDEThreshold());
        setSimilarityThreshold(builder.getSimilarityThreshold());
        setMinExpressionLevel(builder.getMinExpressionLevel());
        setMinAbundance(builder.getMinAbundance());
        setDEMethod(builder.getDEMethod());
        setSimMethod(builder.getSimMethod());
        setOffset(builder.getOffset());
        setOrdered(builder.getOrdered());
        setReplicateCounts(builder.getReplicateCounts());
    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getDEThreshold()          {return getParameterValue(Integer.class, Definition.DIFF_EXPR_THRESHOLD.getName());}
    public int      getSimilarityThreshold()  {return getParameterValue(Integer.class, Definition.SIMILARITY_THRESHOLD.getName());}
    public double   getMinExpressionLevel()   {return getParameterValue(Double.class,  Definition.MIN_EXP_LVL.getName());}
    public int      getMinAbundance()         {return getParameterValue(Integer.class, Definition.MIN_ABUNDANCE.getName());}
    public String   getDEMethod()             {return getParameterValue(String.class, Definition.DE_METHOD.getName());}
    public String   getSimMethod()            {return getParameterValue(String.class, Definition.SIM_METHOD.getName());}
    public double   getOffset()               {return getParameterValue(Double.class,  Definition.OFFSET.getName());}
    public boolean  getOrdered()              {return getParameterValue(Boolean.class, Definition.ORDERED.getName());}
    public int[]    getReplicateCounts()      
    {
      String strRC = getParameterValue(String.class,  Definition.REPLICATE_COUNTS.getName());
      String[] counts = strRC.split( ",");
      List<Integer> intCounts = new ArrayList<Integer>();
      for(String s : counts)
      {
        String trimmed = s.trim();
        if (!trimmed.isEmpty())
        {
          Integer c = Integer.parseInt( trimmed );
          intCounts.add( c );
        }
      }
      
      int[] output = new int[intCounts.size()];
      for(int i = 0; i < intCounts.size(); i++)
      {
        output[i] = intCounts.get( i );
      }
      
      return output;
    }
    

    // **** Setters ****
    public void setDEThreshold(int de_threshold)                  {setParam(Definition.DIFF_EXPR_THRESHOLD, de_threshold);}
    public void setSimilarityThreshold(int sim_threshold)         {setParam(Definition.SIMILARITY_THRESHOLD, sim_threshold);}
    public void setMinExpressionLevel(double min_exp_lvl)         {setParam(Definition.MIN_EXP_LVL, min_exp_lvl);}
    public void setMinAbundance(int min_abundance)                {setParam(Definition.MIN_ABUNDANCE, min_abundance);}
    public void setDEMethod(String de_method)                     {setParam(Definition.DE_METHOD, de_method);}
    public void setSimMethod(String sim_method)                   {setParam(Definition.SIM_METHOD, sim_method);}
    public void setOffset(double ofc_offset)                      {setParam(Definition.OFFSET, ofc_offset);}
    public void setOrdered(boolean ofc_ordered)                   {setParam(Definition.ORDERED, ofc_ordered);}
    public void setReplicateCounts(String replicate_counts)       {setParam(Definition.REPLICATE_COUNTS, replicate_counts);}
    
    
    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at FilterParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int deThreshold          = Definition.DIFF_EXPR_THRESHOLD.getDefault(Integer.class);
        private int simThreshold         = Definition.SIMILARITY_THRESHOLD.getDefault(Integer.class);
        private double minExpLvl         = Definition.MIN_EXP_LVL.getDefault(Double.class);
        private int minAbundance         = Definition.MIN_ABUNDANCE.getDefault(Integer.class);
        private String deMethod          = Definition.DE_METHOD.getDefault(String.class);
        private String simMethod         = Definition.SIM_METHOD.getDefault(String.class);
        private double offset            = Definition.OFFSET.getDefault(Double.class);
        private boolean ordered          = Definition.ORDERED.getDefault(Boolean.class);
        private String replicateCounts   = Definition.REPLICATE_COUNTS.getDefault(String.class);
        
        // **** Getters ****
        public int      getDEThreshold()          {return deThreshold;}
        public int      getSimilarityThreshold()  {return simThreshold;}
        public double   getMinExpressionLevel()   {return minExpLvl;}
        public int      getMinAbundance()         {return minAbundance;}
        public String   getDEMethod()             {return deMethod;}
        public String   getSimMethod()            {return simMethod;}
        public double   getOffset()               {return offset;}
        public boolean  getOrdered()              {return ordered;}
        public String   getReplicateCounts()      {return replicateCounts;}
        
        // **** Setters ****
        public Builder setDEThreshold(int deThreshold)               {this.deThreshold = deThreshold;     return this;}
        public Builder setSimilarityThreshold(int simThreshold)      {this.simThreshold = simThreshold;   return this;}
        public Builder setMinExpressionLevel(double minExpLvl)       {this.minExpLvl = minExpLvl;         return this;}
        public Builder setMinAbundance(int minAbundance)             {this.minAbundance = minAbundance;   return this;}
        public Builder setDEMethod(String deMethod)                  {this.deMethod = deMethod;           return this;}
        public Builder setSimMethod(String simMethod)                {this.simMethod = simMethod;         return this;}
        public Builder setOffset(double offset)                      {this.offset = offset;               return this;}
        public Builder setOrdered(boolean ordered)                   {this.ordered = ordered;             return this;}
        public Builder setReplicateCounts(String replicateCounts)    {this.replicateCounts = replicateCounts;  return this;}
        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public FirepatParams build()
        {
          return new FirepatParams( this );
        }
    }


    public static void main(String[] args)
    {
        try
        {
            FirepatParams ok = FirepatParams.load(new FirepatParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest1-ok.cfg"));
            FirepatParams ko = FirepatParams.load(new FirepatParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
