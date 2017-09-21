package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import uk.ac.uea.cmp.srnaworkbench.tools.de.*;
import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;


/**
 * Stores and manages parameters for the Filter tool.
 * @author ezb11yfu
 */
public final class ECParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each Filter parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        /**
         * Only consider correlated pair if distance between pair exceeds this percentage
         * threshold.
         */
        SIMILARITY_THRESHOLD    ("sim_threshold",    Integer.valueOf(5),    Integer.valueOf(1),    Integer.valueOf(100)),
        
        /**
         * Method to determine expression level for sRNA/loci/gene across samples.
         * Allowed values: "PEARSON_DISTANCE", "COSINE_DISTANCE", "SPEARMAN_RANK_DISTANCE" and "KENDALLS_TAU_DISTANCE"
         */
        METHOD               ("method",       "SPEARMAN_RANK_DISTANCE");
         
        
        
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
    public ECParams()
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
    private ECParams(Builder builder)
    {
        setSimilarityThreshold(builder.getSimilarityThreshold());
        setMethod(builder.getMethod());
    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getSimilarityThreshold()  {return getParameterValue(Integer.class, Definition.SIMILARITY_THRESHOLD.getName());}
    public String   getMethod()               {return getParameterValue(String.class,  Definition.METHOD.getName());}
    

    // **** Setters ****
    public void setSimilarityThreshold(int de_threshold)          {setParam(Definition.SIMILARITY_THRESHOLD, de_threshold);}
    public void setMethod(String de_method)                       {setParam(Definition.METHOD, de_method);}
    
    
    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at FilterParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int similarityThreshold  = Definition.SIMILARITY_THRESHOLD.getDefault(Integer.class);
        private String method            = Definition.METHOD.getDefault(String.class);
        
        // **** Getters ****
        public int      getSimilarityThreshold()  {return similarityThreshold;}
        public String   getMethod()               {return method;}
        
        // **** Setters ****
        public Builder setSimilarityThreshold(int pctThreshold)      {this.similarityThreshold = pctThreshold;   return this;}
        public Builder setMethod(String method)                      {this.method = method;               return this;}
        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public ECParams build()
        {
          return new ECParams( this );
        }
    }


    public static void main(String[] args)
    {
        try
        {
            ECParams ok = ECParams.load(new ECParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest1-ok.cfg"));
            ECParams ko = ECParams.load(new ECParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
