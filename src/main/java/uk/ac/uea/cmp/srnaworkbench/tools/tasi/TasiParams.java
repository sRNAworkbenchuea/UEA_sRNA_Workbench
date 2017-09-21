/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;

/**
 * Describes and manages all parameters associated with the tasi prediction tool
 * @author Dan Mapleson
 */
public final class TasiParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each Adaptor Remover parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        P_VALUE_THRESHOLD       ("p_val_threshold", Double.valueOf(0.0001), Double.valueOf(0.0),    Double.valueOf(0.1)),
        MINIMUM_ABUNDANCE       ("min_abundance",   Integer.valueOf(2),     Integer.valueOf(1),     Integer.MAX_VALUE),
        PHASING_REGISTER        ("Phasing_Register",Integer.valueOf( 21),   Integer.valueOf(16),    Integer.valueOf(60) ),
        GENOME_FILE             ("genome",          (File)null);
                
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
    
    
    // **** Tasi analyser constants
    public static final int     REGION_SIZE             = 231;
    public static final int     NEG_STRAND_OFFSET       = 2;
    
    
    /**
     * Default constructor.  Produces a TasiParams instance with default parameters.
     */
    public TasiParams()
    {
        this(new Builder());
    }
    
    /**
     * Assignment constructor using the Builder design pattern.  Should a client
     * wish to use this constructor directly, they are required to pass a TasiParams.Builder
     * object as an argument, which they can use to pick and choose parameters that 
     * are not default.
     * @param builder A AdaptorRemoverParams.Builder object containing the TasiParams to
     * use.
     */
    private TasiParams(Builder b)
    {
        setPValThreshold(b.getPValThreshold());
        setMinAbundance(b.getMinAbundance());
        setGenome(b.getGenome());
        setPhasingRegister(b.getPhasingRegister());
    }


    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}
    

    // **** Getters ****
    public double   getPValThreshold()          {return getParameterValue(Double.class, Definition.P_VALUE_THRESHOLD.getName());}
    public int      getMinAbundance()           {return getParameterValue(Integer.class, Definition.MINIMUM_ABUNDANCE.getName());}
    public File     getGenome()                 {return getParameterValue(File.class, Definition.GENOME_FILE.getName());}
    public int      getPhasingRegister()        {return getParameterValue(Integer.class, Definition.PHASING_REGISTER.getName());}


    // **** Setters ****
    public final void setPValThreshold(double p_val_threshold)      {setParam(Definition.P_VALUE_THRESHOLD, p_val_threshold);}
    public final void setMinAbundance(int min_abundance)            {setParam(Definition.MINIMUM_ABUNDANCE, min_abundance);}
    public final void setGenome(File genome)                        {setParam(Definition.GENOME_FILE, genome);}
    public final void setGenome(String genome_path)                 {setParam(Definition.GENOME_FILE, genome_path);}
    public final void setPhasingRegister(int register_value)        {setParam(Definition.PHASING_REGISTER, register_value);}
    

    public static final class Builder
    {
        private double  p_val_threshold  = Definition.P_VALUE_THRESHOLD.getDefault(Double.class); 
        private int     min_abundance    = Definition.MINIMUM_ABUNDANCE.getDefault(Integer.class);
        private File    genome           = Definition.GENOME_FILE.getDefault(File.class);
        private int     phasing_register = Definition.PHASING_REGISTER.getDefault( Integer.class);
        
        // **** Getters ****
        public double   getPValThreshold()          {return p_val_threshold;}
        public int      getMinAbundance()           {return min_abundance;}
        public File     getGenome()                 {return genome;}
        public int      getPhasingRegister()        {return phasing_register;}
        
        // **** Setters ****
        public Builder   setPValThreshold(double p_val_threshold)   {this.p_val_threshold = p_val_threshold;    return this;}
        public Builder   setMinAbundance(int min_abundance)         {this.min_abundance = min_abundance;        return this;}
        public Builder   setGenome(File genome)                     {this.genome = genome;                      return this;}
        public Builder   setPhasingRegister(int register)           {this.phasing_register = register;          return this;}
        
        /**
         * Constructs the TasiParams object
         * @return
         */
        public TasiParams build()
        {
            return new TasiParams( this );
        }
    }
    
}
