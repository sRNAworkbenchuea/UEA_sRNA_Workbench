/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.io.File;
import java.io.IOException;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Stores and manages parameters for MiRCat
 * @author ezb11yfu
 */
public final class MiRCatParams extends ToolParameters
{

  
    /**
     * An enumeration containing a list of definitions for each MiRProf parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        FLANKING_SEQ_EXTENSION              ("extend",                  Float.valueOf(100.0f),  Float.valueOf(0.0f),    Float.valueOf(1000.0f)),
        MFE_THRESHOLD                       ("min_energy",              Float.valueOf(-25.0f),  Float.valueOf(-100.0f), Float.valueOf(0.0f)),
        P_VAL_THRESHOLD                     ("pval",                    Float.valueOf(0.05f),    Float.valueOf(0.0f),    Float.valueOf(1.0f)),
        
        MINIMUM_SRNA_LENGTH                 ("min_length",              Integer.valueOf(20),    Integer.valueOf(16),    Integer.valueOf(35)),
        MAXIMUM_SRNA_LENGTH                 ("max_length",              Integer.valueOf(22),    Integer.valueOf(16),    Integer.valueOf(35)),
        MINIMUM_SRNA_ABUNDANCE              ("min_abundance",           Integer.valueOf(1),     Integer.valueOf(1),     Integer.MAX_VALUE),
        MINIMUM_PAIRED_BASES                ("min_paired",              Integer.valueOf(17),    Integer.valueOf(10),    Integer.valueOf(25)),
        MAXIMUM_GAP_SIZE                    ("max_gaps",                Integer.valueOf(3),     Integer.valueOf(0),     Integer.valueOf(5)),
        MAXIMUM_GENOME_HITS                 ("max_genome_hits",         Integer.valueOf(16),    Integer.valueOf(1),     Integer.MAX_VALUE),
        MINIMUM_GC_PERCENTAGE               ("min_gc",                  Integer.valueOf(20),    Integer.valueOf(1),     Integer.valueOf(100)),
        MAXIMUM_UNPAIRED_BASES_PERCENTAGE   ("max_unpaired",            Integer.valueOf(50),    Integer.valueOf(1),     Integer.valueOf(100)),
        MAXIMUM_SRNA_OVERLAP_PERCENTAGE     ("max_overlap_percentage",  Integer.valueOf(80),    Integer.valueOf(1),     Integer.valueOf(100)),
        MINIMUM_LOCUS_SIZE                  ("min_locus_size",          Integer.valueOf(1),     Integer.valueOf(1),     Integer.MAX_VALUE),
        MINIMUM_ORIENTATION_PERCENTAGE      ("orientation",             Integer.valueOf(80),    Integer.valueOf(1),     Integer.valueOf(100)),
        MINIMUM_HAIRPIN_LENGTH              ("min_hairpin_len",         Integer.valueOf(60),    Integer.valueOf(20),    Integer.MAX_VALUE),
        MINIMUM_CLUSTER_SEPARATION_DISTANCE ("cluster_sentinel",        Integer.valueOf(200),   Integer.valueOf(1),     Integer.MAX_VALUE),
        THREAD_COUNT                        ("Thread_Count",            Tools.getThreadCount(), Integer.valueOf(1),     Tools.getThreadCount()),
        
        ALLOW_COMPLEX_LOOPS                 ("complex_loops",           Boolean.TRUE);
                
                        
        private ParameterDefinition definition;

        private Definition(String name, Boolean default_value)                                  {this.definition = new ParameterDefinition<Boolean>( name, default_value ); }
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
     * Default constructor.  Produces a MiRCatParams instance with default parameters.
     */
    public MiRCatParams()
    {
        this(new Builder());
    }
    
    /**
     * Assignment constructor using the Builder design pattern.  Should a client
     * wish to use this constructor directly, they are required to pass a MiRCatParams.Builder
     * object as an argument, which they can use to pick and choose parameters that 
     * are not default.
     * @param builder A AdaptorRemoverParams.Builder object containing the MiRCatParams to
     * use.
     */
    private MiRCatParams(Builder b)
    {
        setExtend(b.getExtend());
        setMinEnergy(b.getMinEnergy());
        setMinPaired(b.getMinPaired());
        setMaxGaps(b.getMaxGaps());
        setMaxGenomeHits(b.getMaxGenomeHits());
        setLengthRange(b.getMinLength(), b.getMaxLength());
        setMinGC(b.getMinGC());
        setMaxUnpaired(b.getMaxUnpaired());
        setMaxOverlapPercentage(b.getMaxOverlapPercentage());
        setMinLocusSize(b.getMinLocusSize());
        setOrientation(b.getOrientation());
        setMinHairpinLength(b.getMinHairpinLength());
        setComplexLoops(b.getComplexLoops());
        setPVal(b.getPVal());
        setMinConsider(b.getMinConsider());
        setClusterSentinel(b.getClusterSentinel());
        setPVal( b.getPVal() );
        setThreadCount(b.getThreadCount());
    }

    /**
     * Assignment constructor creating an instance with all client defined parameters
     * @param extend Length of flanking sequence to grab for miRNA hairpin detection (both 3' and 5')
     * @param minenergy Minimum free energy cutoff for miRNA hairpins (default -25.0 kcal/mol) - uses adjusted MFE (MFE/hairpin length) * 100.
     * @param min_paired Minimum number of bases in 25 nt centered on the miRNA that must be involved in base pairing
     * @param max_gaps Maximum number of consecutive mismatches allowed between potential miRNA and potential miRNA*
     * @param max_genome_hits Maximum number of genomic loci of candidate mature miRNA
     * @param min_length Minimum length of mature miRNA
     * @param max_length Maximum length of mature miRNA
     * @param min_gc Minimum percentage G/C richness of sRNA
     * @param max_unpaired Maximum percentage of unpaired nucleotides in the hairpin
     * @param max_overlap_percentage Max combined length of overlapping sRNAs in locus
     * @param min_locus_size clust_length
     * @param orientation Minimum percentage of sRNAs in region that must be in the same orientation as the predicted miRNA
     * @param min_hairpin_len Minimum length of valid hairpin
     * @param complex_loops Dis/allow complex loop structures
     * @param p_val p-value cutoff for randfold
     * @param min_consider Minimum sRNA abundance to be considered real
     * @param cluster_sentinel Used to determine a valid cluster
     * @throws IllegalArgumentException 
     */
    public MiRCatParams(float extend, float min_energy, int min_paired,
            int max_gaps, int max_genome_hits, int min_length, int max_length,
            int min_gc, int max_unpaired, int max_overlap_percentage, int min_locus_size,
            int orientation, int min_hairpin_len, boolean complex_loops,
            float p_val, int min_consider, 
            int cluster_sentinel)
    {
        this(new Builder()
                .setExtend(extend)
                .setMinEnergy(min_energy)
                .setMinPaired(min_paired)
                .setMaxGaps(max_gaps)
                .setMaxGenomeHits(max_genome_hits)
                .setMinLength(min_length)
                .setMaxLength(max_length)
                .setMinGC(min_gc)
                .setMaxUnpaired(max_unpaired)
                .setMaxOverlapPercentage(max_overlap_percentage)
                .setMinLocusSize(min_locus_size)
                .setOrientation(orientation)
                .setMinHairpinLength(min_hairpin_len)
                .setComplexLoops(complex_loops)
                .setPVal(p_val)
                .setMinConsider(min_consider)
                .setClusterSentinel(cluster_sentinel)
                .setPVal( p_val )
        ); 
    }

    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}
   
    // **** Getters ****
    public float    getExtend()                 {return getParameterValue(Float.class, Definition.FLANKING_SEQ_EXTENSION.getName());}
    public float    getMinEnergy()              {return getParameterValue(Float.class, Definition.MFE_THRESHOLD.getName());}
    public int      getMinPaired()              {return getParameterValue(Integer.class, Definition.MINIMUM_PAIRED_BASES.getName());}
    public int      getMaxGaps()                {return getParameterValue(Integer.class, Definition.MAXIMUM_GAP_SIZE.getName());}
    public int      getMaxGenomeHits()          {return getParameterValue(Integer.class, Definition.MAXIMUM_GENOME_HITS.getName());}
    public int      getMinLength()              {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_LENGTH.getName());}
    public int      getMaxLength()              {return getParameterValue(Integer.class, Definition.MAXIMUM_SRNA_LENGTH.getName());}
    public int      getMinGC()                  {return getParameterValue(Integer.class, Definition.MINIMUM_GC_PERCENTAGE.getName());}
    public int      getMaxUnpaired()            {return getParameterValue(Integer.class, Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getName());}
    public int      getMaxOverlapPercentage()   {return getParameterValue(Integer.class, Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getName());}
    public int      getMinLocusSize()           {return getParameterValue(Integer.class, Definition.MINIMUM_LOCUS_SIZE.getName());}
    public int      getOrientation()            {return getParameterValue(Integer.class, Definition.MINIMUM_ORIENTATION_PERCENTAGE.getName());}
    public int      getMinHairpinLength()       {return getParameterValue(Integer.class, Definition.MINIMUM_HAIRPIN_LENGTH.getName());}
    public boolean  getComplexLoops()           {return getParameterValue(Boolean.class, Definition.ALLOW_COMPLEX_LOOPS.getName());}
    public float    getPVal()                   {return getParameterValue(Float.class, Definition.P_VAL_THRESHOLD.getName());}
    public int      getMinConsider()            {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_ABUNDANCE.getName());}
    public int      getClusterSentinel()        {return getParameterValue(Integer.class, Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getName());}
    public int      getThreadCount()            {return getParameterValue(Integer.class, Definition.THREAD_COUNT.getName());}

    // **** Setters ****
    public void setExtend(float extend)                             {setParam(Definition.FLANKING_SEQ_EXTENSION, extend);}
    public void setMinEnergy(float min_energy)                      {setParam(Definition.MFE_THRESHOLD, min_energy);}
    public void setMinPaired(int min_paired)                        {setParam(Definition.MINIMUM_PAIRED_BASES, min_paired);}
    public void setMaxGaps(int max_gaps)                            {setParam(Definition.MAXIMUM_GAP_SIZE, max_gaps);}
    public void setMaxGenomeHits(int max_genome_hits)               {setParam(Definition.MAXIMUM_GENOME_HITS, max_genome_hits);}
    private void setMinLength(int min_length)                       {setParam(Definition.MINIMUM_SRNA_LENGTH, min_length);}
    private void setMaxLength(int max_length)                       {setParam(Definition.MAXIMUM_SRNA_LENGTH, max_length);}
    public void setMinGC(int min_gc)                                {setParam(Definition.MINIMUM_GC_PERCENTAGE, min_gc);}
    public void setMaxUnpaired(int max_unpaired)                    {setParam(Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE, max_unpaired);}
    public void setMaxOverlapPercentage(int max_overlap_percentage) {setParam(Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE, max_overlap_percentage);}
    public void setMinLocusSize(int min_locus_size)                 {setParam(Definition.MINIMUM_LOCUS_SIZE, min_locus_size);}
    public void setOrientation(int orientation)                     {setParam(Definition.MINIMUM_ORIENTATION_PERCENTAGE, orientation);}
    public void setMinHairpinLength(int min_hairpin_len)            {setParam(Definition.MINIMUM_HAIRPIN_LENGTH, min_hairpin_len);}
    public void setComplexLoops(boolean complex_loops)              {setParam(Definition.ALLOW_COMPLEX_LOOPS, complex_loops);}
    public void setPVal(float p_val)                                {setParam(Definition.P_VAL_THRESHOLD, p_val);}
    public void setMinConsider(int min_consider)                    {setParam(Definition.MINIMUM_SRNA_ABUNDANCE, min_consider);}
    public void setClusterSentinel(int cluster_sentinel)            {setParam(Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE, cluster_sentinel);}
    public void setThreadCount(int thread_count)                    {setParam(Definition.THREAD_COUNT, thread_count);}
    
    public void setLengthRange(int min_length, int max_length)
    {
        if (min_length > max_length)
        {
            throw new IllegalArgumentException("Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinLength(min_length);
        setMaxLength(max_length);
    }
    
    
    /**
     * Creates a new MiRCatParams object with parameters set to default for animal jobs
     * @return Default parameters for animal jobs
     */
    public static MiRCatParams createDefaultAnimalParams()                                                   
    {
      return new Builder()
        .setExtend( 40 )
        .setMinEnergy( -25 )
        .setMinPaired( 17 )
        .setMinConsider( 5 )
        .setMaxGaps( 3 )
        .setMaxGenomeHits( 10 )
        .setMinLength( 21 ).setMaxLength( 23 )
        .setMinGC( 30 )
        .setMaxUnpaired( 60 )
        .setMaxOverlapPercentage( 60 )
        .setClusterSentinel( 30 )
        .setMaxOverlapPercentage( 80 )
        .setMinHairpinLength( 55 )
        .setComplexLoops( true )
        .setPVal( 0.1f)
        .build();       
    }                                                   

    /**
     * Creates a new MiRCatParams object with parameters set to default for plant jobs
     * @return Default parameters for plant jobs
     */
    public static MiRCatParams createDefaultPlantParams()
    {                                                       
      return new MiRCatParams();
    }                                                  

    
    
    
    @Override
    public String toString()
    {
        return super.toString();
    }

    
    public static final class Builder
    {
        private float   extend                  = Definition.FLANKING_SEQ_EXTENSION.getDefault(Float.class); 
        private float   min_energy              = Definition.MFE_THRESHOLD.getDefault(Float.class); 
        private int     min_paired              = Definition.MINIMUM_PAIRED_BASES.getDefault(Integer.class); 
        private int     max_gaps                = Definition.MAXIMUM_GAP_SIZE.getDefault(Integer.class);
        private int     max_genome_hits         = Definition.MAXIMUM_GENOME_HITS.getDefault(Integer.class);
        private int     min_gc                  = Definition.MINIMUM_GC_PERCENTAGE.getDefault(Integer.class); 
        private int     max_unpaired            = Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefault(Integer.class);
        private int     max_overlap_percentage  = Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefault(Integer.class); 
        private int     min_locus_size          = Definition.MINIMUM_LOCUS_SIZE.getDefault(Integer.class);
        private int     orientation             = Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefault(Integer.class);
        private int     min_hairpin_len         = Definition.MINIMUM_HAIRPIN_LENGTH.getDefault(Integer.class); 
        private boolean complex_loops           = Definition.ALLOW_COMPLEX_LOOPS.getDefault(Boolean.class); 
        private float   p_val                   = Definition.P_VAL_THRESHOLD.getDefault(Float.class);
        private int     min_consider            = Definition.MINIMUM_SRNA_ABUNDANCE.getDefault(Integer.class);
        private int     cluster_sentinel        = Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefault(Integer.class);
        private int     min_length              = Definition.MINIMUM_SRNA_LENGTH.getDefault(Integer.class); 
        private int     max_length              = Definition.MAXIMUM_SRNA_LENGTH.getDefault(Integer.class); 
        private int     thread_count            = Definition.THREAD_COUNT.getDefault( Integer.class );
    
        // **** Getters ****
        public float    getExtend()                 {return extend;}
        public float    getMinEnergy()              {return min_energy;}
        public int      getMinPaired()              {return min_paired;}
        public int      getMaxGaps()                {return max_gaps;}
        public int      getMaxGenomeHits()          {return max_genome_hits;}
        public int      getMinLength()              {return min_length;}
        public int      getMaxLength()              {return max_length;}
        public int      getMinGC()                  {return min_gc;}
        public int      getMaxUnpaired()            {return max_unpaired;}
        public int      getMaxOverlapPercentage()   {return max_overlap_percentage;}
        public int      getMinLocusSize()           {return min_locus_size;}
        public int      getOrientation()            {return orientation;}
        public int      getMinHairpinLength()       {return min_hairpin_len;}
        public boolean  getComplexLoops()           {return complex_loops;}
        public float    getPVal()                   {return p_val;}
        public int      getMinConsider()            {return min_consider;}
        public int      getClusterSentinel()        {return cluster_sentinel;}
        public int      getThreadCount()            {return thread_count;}
        
        // **** Setters ****
        public Builder  setExtend(float extend)                             {this.extend = extend;                                  return this;}
        public Builder  setMinEnergy(float min_energy)                      {this.min_energy = min_energy;                          return this;}
        public Builder  setMinPaired(int min_paired)                        {this.min_paired = min_paired;                          return this;}
        public Builder  setMaxGaps(int max_gaps)                            {this.max_gaps = max_gaps;                              return this;}
        public Builder  setMaxGenomeHits(int max_genome_hits)               {this.max_genome_hits = max_genome_hits;                return this;}
        public Builder  setMinLength(int min_length)                        {this.min_length = min_length;                          return this;}
        public Builder  setMaxLength(int max_length)                        {this.max_length = max_length;                          return this;}
        public Builder  setMinGC(int min_gc)                                {this.min_gc = min_gc;                                  return this;}
        public Builder  setMaxUnpaired(int max_unpaired)                    {this.max_unpaired = max_unpaired;                      return this;}
        public Builder  setMaxOverlapPercentage(int max_overlap_percentage) {this.max_overlap_percentage = max_overlap_percentage;  return this;}
        public Builder  setMinLocusSize(int min_locus_size)                 {this.min_locus_size = min_locus_size;                  return this;}
        public Builder  setOrientation(int orientation)                     {this.orientation = orientation;                        return this;}
        public Builder  setMinHairpinLength(int min_hairpin_len)            {this.min_hairpin_len = min_hairpin_len;                return this;}
        public Builder  setComplexLoops(boolean complex_loops)              {this.complex_loops = complex_loops;                    return this;}
        public Builder  setPVal(float p_val)                                {this.p_val = p_val;                                    return this;}
        public Builder  setMinConsider(int min_consider)                    {this.min_consider = min_consider;                      return this;}
        public Builder  setClusterSentinel(int cluster_sentinel)            {this.cluster_sentinel = cluster_sentinel;              return this;}
        public Builder  setThreadCount(int threadCount)                      {this.thread_count = threadCount;                       return this;}
        
        /**
         * Constructs the FilterParams object
         * @return
         */
        public MiRCatParams build()
        {
            return new MiRCatParams( this );
        }
    }
}
