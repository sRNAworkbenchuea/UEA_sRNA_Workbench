/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.expressionCalculator;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;


/**
 *
 * @author w0445959
 */
public class ExpressionCalculatorParams extends ToolParameters
{
  public enum ExpressionDataType
  {
    MRNA,
    SRNA;
  }
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
        MIN_ABUNDANCE        ("min_abundance", Integer.valueOf(1),    Integer.valueOf(1),    Integer.MAX_VALUE);
        
        
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
    public ExpressionCalculatorParams()
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
    private ExpressionCalculatorParams(Builder builder)
    {
        setDEThreshold(builder.getDEThreshold());
    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getDEThreshold()          {return getParameterValue(Integer.class, Definition.DIFF_EXPR_THRESHOLD.getName());}

    // **** Setters ****
    public void setDEThreshold(int de_threshold)                  {setParam(Definition.DIFF_EXPR_THRESHOLD, de_threshold);}

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

        // **** Getters ****
        public int      getDEThreshold()          {return deThreshold;}

        
        // **** Setters ****
        public Builder setDEThreshold(int deThreshold)               {this.deThreshold = deThreshold;     return this;}

        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public ExpressionCalculatorParams build()
        {
          return new ExpressionCalculatorParams( this );
        }
    }


    
}
