package uk.ac.uea.cmp.srnaworkbench.tools.cpc;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;


/**
 * Stores and manages parameters for the Filter tool.
 * @author ezb11yfu
 */
public final class CPCParams extends ToolParameters
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
        CLUSTER_THRESHOLD    ("cluster_threshold",    Integer.valueOf(4),    Integer.valueOf(1),    Integer.valueOf(100)),
        
        /**
         * 
         */
        PEARSON_THRESHOLD    ("pearson_threshold",    Integer.valueOf(5),    Integer.valueOf(1),    Integer.valueOf(100)),
        
        /**
         * 
         */
        PERMITTED_NR_CLUSTERS   ("permitted_nr_clusters",   Integer.valueOf(10),    Integer.valueOf(1),    Integer.valueOf(100)),
        
        /**
         * 
         */
        SELECTED_NR_CLUSTERS    ("selected_nr_clusters",    Integer.valueOf(5),    Integer.valueOf(1),    Integer.valueOf(100));
        
        
        
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
    public CPCParams()
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
    private CPCParams(Builder builder)
    {
        setClusterThreshold(builder.getClusterThreshold());
        setPearsonThreshold(builder.getPearsonThreshold());
        setPermittedNRClusters(builder.getPermittedNRClusters());
        setSelectedNRClusters(builder.getSelectedNRClusters());
    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getClusterThreshold()         {return getParameterValue(Integer.class, Definition.CLUSTER_THRESHOLD.getName());}
    public int      getPearsonThreshold()         {return getParameterValue(Integer.class, Definition.PEARSON_THRESHOLD.getName());}
    public int      getPermittedNRClusters()      {return getParameterValue(Integer.class, Definition.PERMITTED_NR_CLUSTERS.getName());}
    public int      getSelectedNRClusters()       {return getParameterValue(Integer.class, Definition.SELECTED_NR_CLUSTERS.getName());}
    

    // **** Setters ****
    public void setClusterThreshold(int clusterThreshold)         {setParam(Definition.CLUSTER_THRESHOLD, clusterThreshold);}
    public void setPearsonThreshold(int pearsonThreshold)         {setParam(Definition.PEARSON_THRESHOLD, pearsonThreshold);}
    public void setPermittedNRClusters(int permittedNRClusters)   {setParam(Definition.PERMITTED_NR_CLUSTERS, permittedNRClusters);}
    public void setSelectedNRClusters(int selectedNRClusters)     {setParam(Definition.SELECTED_NR_CLUSTERS, selectedNRClusters);}
    
    
    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at FilterParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int clusterThreshold         = Definition.CLUSTER_THRESHOLD.getDefault(Integer.class);
        private int pearsonThreshold         = Definition.PEARSON_THRESHOLD.getDefault(Integer.class);
        private int permittedNRClusters      = Definition.PERMITTED_NR_CLUSTERS.getDefault(Integer.class);
        private int selectedNRClusters       = Definition.SELECTED_NR_CLUSTERS.getDefault(Integer.class);
        
        // **** Getters ****
        public int      getClusterThreshold()         {return clusterThreshold;}
        public int      getPearsonThreshold()         {return pearsonThreshold;}
        public int      getPermittedNRClusters()      {return permittedNRClusters;}
        public int      getSelectedNRClusters()       {return selectedNRClusters;}
        
        // **** Setters ****
        public Builder setClusterThreshold(int clusterThreshold)             {this.clusterThreshold = clusterThreshold;       return this;}
        public Builder setPearsonThreshold(int pearsonThreshold)             {this.pearsonThreshold = pearsonThreshold;       return this;}
        public Builder setPermittedNRClusters(int permittedNRClusters)       {this.permittedNRClusters = permittedNRClusters; return this;}
        public Builder setSelectedNRClusters(int selectedNRClusters)         {this.selectedNRClusters = selectedNRClusters;   return this;}
        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public CPCParams build()
        {
          return new CPCParams( this );
        }
    }


    public static void main(String[] args)
    {
        try
        {
            CPCParams ok = CPCParams.load(new CPCParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest1-ok.cfg"));
            CPCParams ko = CPCParams.load(new CPCParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
