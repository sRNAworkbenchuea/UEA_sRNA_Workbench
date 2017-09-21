package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;


/**
 * Stores and manages parameters for miRProf
 * @author ezb11yfu
 */
public final class MirprofParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each MiRProf parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        MINIMUM_LENGTH              ("min_length",              Integer.valueOf(18),    Integer.valueOf(16),    Integer.valueOf(35)),
        MAXIMUM_LENGTH              ("max_length",              Integer.valueOf(25),    Integer.valueOf(16),    Integer.valueOf(35)),
        MINIMUM_ABUNDANCE           ("min_abundance",           Integer.valueOf(1),     Integer.valueOf(1),     Integer.MAX_VALUE),
        ALLOWED_MISMATCHES          ("mismatches",              Integer.valueOf(0),     Integer.valueOf(0),     Integer.valueOf(3)),
        MIRBASE_VERSION             ("mirbase_version",         "N/A"),
        ALLOW_OVERHANGS             ("overhangs",               Boolean.TRUE),
        GROUP_MISMATCHES            ("group_mismatches",        Boolean.TRUE),
        GROUP_ORGANISMS             ("group_organisms",         Boolean.TRUE),
        GROUP_MATURE_AND_STAR       ("group_mature_and_star",   Boolean.FALSE),
        GROUP_VARIANTS              ("group_variant",           Boolean.TRUE),
        GROUP_OPTIONS_STRING        ("group_options_string",    "None"),
        ONLY_KEEP_BEST_MATCH        ("only_keep_best",          Boolean.TRUE);
        
                        
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
    public MirprofParams()
    {
        this(new Builder());
    }
    
    /**
     * Assignment constructor using the Builder design pattern. Only for internal
     * use.  Client's must use the Builder.build() to create a MirprofParams object
     * or use the assignment constructor.
     * @param builder 
     */
    private MirprofParams(Builder builder)
    {
        setMismatches(builder.getMismatches());
        setOverhangsAllowed(builder.getOverhangsAllowed());
        setGroupMismatches(builder.getGroupMismatches());
        setGroupOrganisms(builder.getGroupOrganisms());
        setGroupVariant(builder.getGroupVariant());
        setGroupMatureAndStar(builder.getGroupMatureAndStar());
        setOnlyKeepBest(builder.getOnlyKeepBest());
        setLengthRange(builder.getMinLength(), builder.getMaxLength());
        setMinAbundance(builder.getMinAbundance());
        setMirBaseVersion(builder.getMirBaseVersion());
        setGroupOptionsString(builder.getGroupOptionsString());
    }
    

    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getMismatches()             {return getParameterValue(Integer.class, Definition.ALLOWED_MISMATCHES.getName());}
    public boolean  getOverhangsAllowed()       {return getParameterValue(Boolean.class, Definition.ALLOW_OVERHANGS.getName());}
    public boolean  getGroupMismatches()        {return getParameterValue(Boolean.class, Definition.GROUP_MISMATCHES.getName());}
    public boolean  getGroupOrganisms()         {return getParameterValue(Boolean.class, Definition.GROUP_ORGANISMS.getName());}
    public boolean  getGroupVariant()           {return getParameterValue(Boolean.class, Definition.GROUP_VARIANTS.getName());}
    public boolean  getGroupMatureAndStar()     {return getParameterValue(Boolean.class, Definition.GROUP_MATURE_AND_STAR.getName());}
    public boolean  getOnlyKeepBest()           {return getParameterValue(Boolean.class, Definition.ONLY_KEEP_BEST_MATCH.getName());}
    public int      getMinLength()              {return getParameterValue(Integer.class, Definition.MINIMUM_LENGTH.getName());}
    public int      getMaxLength()              {return getParameterValue(Integer.class, Definition.MAXIMUM_LENGTH.getName());}
    public int      getMinAbundance()           {return getParameterValue(Integer.class, Definition.MINIMUM_ABUNDANCE.getName());}
    public String   getMiRBaseVersion()         {return getParameterValue(String.class,  Definition.MIRBASE_VERSION.getName());}
    public String   getGroupOptionsString()     {return getParameterValue(String.class,  Definition.GROUP_OPTIONS_STRING.getName());}
    


    // **** Setters ****
    public void setMismatches(int mismatches)                           {setParam(Definition.ALLOWED_MISMATCHES, mismatches);}
    public void setOverhangsAllowed(boolean allow_overhangs)            {setParam(Definition.ALLOW_OVERHANGS, allow_overhangs);}
    public void setGroupMismatches(boolean group_mismatches)            {setParam(Definition.GROUP_MISMATCHES, group_mismatches);}
    public void setGroupOrganisms(boolean group_organisms)              {setParam(Definition.GROUP_ORGANISMS, group_organisms);}
    public void setGroupVariant(boolean group_variant)                  {setParam(Definition.GROUP_VARIANTS, group_variant);}
    public void setGroupMatureAndStar(boolean group_mature_and_star)    {setParam(Definition.GROUP_MATURE_AND_STAR, group_mature_and_star);}
    public void setOnlyKeepBest(boolean only_keep_best)                 {setParam(Definition.ONLY_KEEP_BEST_MATCH, only_keep_best);}
    private void setMinLength(int min_length)                           {setParam(Definition.MINIMUM_LENGTH, min_length);}
    private void setMaxLength(int max_length)                           {setParam(Definition.MAXIMUM_LENGTH, max_length);}
    public void setMinAbundance(int min_abundance)                      {setParam(Definition.MINIMUM_ABUNDANCE, min_abundance);}
    public void setMirBaseVersion(String version)                       {setParam(Definition.MIRBASE_VERSION, version);}
    public void setGroupOptionsString(String options)                   {setParam(Definition.GROUP_OPTIONS_STRING, options);}
   
    public void setLengthRange(int min_length, int max_length)
    {
        if (min_length > max_length)
        {
            throw new IllegalArgumentException("Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinLength(min_length);
        setMaxLength(max_length);
    }

    
    public static final class Builder
    {
        private int     mismatches              = Definition.ALLOWED_MISMATCHES.getDefault(Integer.class);
        private boolean only_keep_best          = Definition.ONLY_KEEP_BEST_MATCH.getDefault(Boolean.class);
        private boolean allow_overhangs         = Definition.ALLOW_OVERHANGS.getDefault(Boolean.class);

        private boolean group_mismatches        = Definition.GROUP_MISMATCHES.getDefault(Boolean.class);
        private boolean group_organisms         = Definition.GROUP_ORGANISMS.getDefault(Boolean.class);
        private boolean group_variant           = Definition.GROUP_VARIANTS.getDefault(Boolean.class);
        private boolean group_mature_and_star   = Definition.GROUP_MATURE_AND_STAR.getDefault(Boolean.class);
        private String mirbase_version          = Definition.MIRBASE_VERSION.getDefault( String.class);
        private String group_options_string     = Definition.GROUP_OPTIONS_STRING.getDefault( String.class);

        // Filter params
        private int     min_length              = Definition.MINIMUM_LENGTH.getDefault(Integer.class);
        private int     max_length              = Definition.MAXIMUM_LENGTH.getDefault(Integer.class);
        private int     min_abundance           = Definition.MINIMUM_ABUNDANCE.getDefault(Integer.class);
        
        
        // **** Getters ****
        public int      getMismatches()             {return mismatches;}
        public boolean  getOverhangsAllowed()       {return allow_overhangs;}
        public boolean  getGroupMismatches()        {return group_mismatches;}
        public boolean  getGroupOrganisms()         {return group_organisms;}
        public boolean  getGroupVariant()           {return group_variant;}
        public boolean  getGroupMatureAndStar()     {return group_mature_and_star;}
        public boolean  getOnlyKeepBest()           {return only_keep_best;}
        public int      getMinLength()              {return min_length;}
        public int      getMaxLength()              {return max_length;}
        public int      getMinAbundance()           {return min_abundance;}
        public String   getMirBaseVersion()         {return mirbase_version;}
        public String   getGroupOptionsString()     {return group_options_string;}
        
        
        // **** Setters ****
        public Builder setMismatches(int mismatches)                       {this.mismatches = mismatches;                       return this;}        
        public Builder setOverhangsAllowed(boolean allow_overhangs)        {this.allow_overhangs = allow_overhangs;             return this;}        
        public Builder setGroupMismatches(boolean group_mismatches)        {this.group_mismatches = group_mismatches;           return this;}
        public Builder setGroupOrganisms(boolean group_organisms)          {this.group_organisms = group_organisms;             return this;}
        public Builder setGroupVariant(boolean group_variant)              {this.group_variant = group_variant;                 return this;}
        public Builder setGroupMatureAndStar(boolean group_mature_and_star){this.group_mature_and_star = group_mature_and_star; return this;}
        public Builder setOnlyKeepBest(boolean only_keep_best)             {this.only_keep_best = only_keep_best;               return this;}
        public Builder setMinLength(int min_length)                        {this.min_length = min_length;                       return this;}        
        public Builder setMaxLength(int max_length)                        {this.max_length = max_length;                       return this;}        
        public Builder setMinAbundance(int min_abundance)                  {this.min_abundance = min_abundance;                 return this;}   
        public Builder setMirBaseVersion(String version)                   {this.mirbase_version = version;                     return this;}
        public Builder setGroupOptionsString(String options)               {this.group_options_string = options;                return this;}
     
        /**
         * Constructs the FilterParams object
         * @return
         */
        public MirprofParams build()
        {
          return new MirprofParams( this );
        }
    }

    public static void main(String[] args)
    {
        try
        {
            MirprofParams ok = MirprofParams.load(new MirprofParams(), new File("C:\\LocalData\\Research\\RNA Tools\\miRProf\\mirprofparamtest1-ok.cfg"));
            MirprofParams ko = MirprofParams.load(new MirprofParams(), new File("C:\\LocalData\\Research\\RNA Tools\\miRProf\\mirprofparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
