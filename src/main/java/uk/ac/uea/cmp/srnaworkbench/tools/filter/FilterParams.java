package uk.ac.uea.cmp.srnaworkbench.tools.filter;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;


/**
 * Stores and manages parameters for the Filter tool.
 * @author ezb11yfu
 */
public final class FilterParams extends ToolParameters
{
    /**
     * An enumeration containing a list of definitions for each Filter parameter.
     * Use this enum to access parameter default values and limits.
     */
    public enum Definition
    {
        MINIMUM_LENGTH              ("min_length",          Integer.valueOf(16),    Integer.valueOf(1),    Integer.valueOf(49)),
        MAXIMUM_LENGTH              ("max_length",          Integer.valueOf(35),    Integer.valueOf(17),    Integer.valueOf(81)),
        MINIMUM_ABUNDANCE           ("min_abundance",       Integer.valueOf(1),     Integer.valueOf(1),     Integer.MAX_VALUE),
        MAXIMUM_ABUNDANCE           ("max_abundance",       Integer.MAX_VALUE,      Integer.valueOf(1),     Integer.MAX_VALUE),
        NORMALISE_ABUNDANCE         ("norm_abundance",      Boolean.FALSE ),//this feature is disabled in Version 4 (leave false for code designed for this version)
        DISCARD_LOW_COMPLEXITY_SEQS ("filter_low_comp",     Boolean.FALSE),
        DISCARD_INVALID_SEQS        ("filter_invalid",      Boolean.FALSE),
        DISCARD_TRRNA               ("trrna",               Boolean.FALSE),
        DISCARD_TRRNA_SENSE_ONLY    ("trrna_sense_only",    Boolean.FALSE),
        OUTPUT_NON_REDUNDANT        ("output_nr",           Boolean.TRUE),
        OUTPUT_REDUNDANT            ("output_r",            Boolean.FALSE),
        DISCARD_GENOME_HITS         ("filter_genome_hits",  Boolean.FALSE),
        FILTER_NORM_ABUND           ("filter_norm_abund",   Boolean.FALSE),//this feature is disabled in Version 4 (leave false for code designed for this version)
        DISCARD_KILL_LIST           ("filter_kill_list",    Boolean.FALSE),
        ADD_DISCARD_LOG             ("add_discard_log",     Boolean.FALSE),
        GENOME_FILE                 ("genome",              new File("")),
        KILL_LIST                   ("kill_list",           new File("")),
        DISCARD_LOG                 ("discard_log",         new File(""));

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
    public FilterParams()
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
    private FilterParams(Builder builder)
    {
        setLengthRange(builder.getMinLength(), builder.getMaxLength());
        setAbundanceRange(builder.getMinAbundance(), builder.getMaxAbundance());
        setNormaliseAbundance(builder.getNormaliseAbundance());
        setFilterByNormaliseAbundance(builder.getFilterByNormalisedAbundance());
        setFilterLowComplexitySeq(builder.getFilterLowComplexitySeq());
        setFilterInvalidSeq(builder.getFilterInvalidSeq());
        setDiscardTRRNA(builder.getDiscardTRRNA());
        setDiscardTRRNASenseOnly(builder.getDiscardTRRNASenseOnly());
        setOutputNonRedundant(builder.getOutputNonRedundant());
        setOutputRedundant(builder.getOutputRedundant());
        setFilterGenomeHits(builder.getFilterGenomeHits());
        setFilterKillList(builder.getFilterKillList());
        setAddDiscardLog(builder.getAddDiscardLog());
        setGenome(builder.getGenome());
        setKillList(builder.getKillList());
        setDiscardLog(builder.getDiscardLog());
    }
    
    public FilterParams(FilterParams copyMe)
    {
        setLengthRange(copyMe.getMinLength(), copyMe.getMaxLength());
        setAbundanceRange(copyMe.getMinAbundance(), copyMe.getMaxAbundance());
        setNormaliseAbundance(copyMe.getNormaliseAbundance());
        setFilterLowComplexitySeq(copyMe.getFilterLowComplexitySeq());
        setFilterInvalidSeq(copyMe.getFilterInvalidSeq());
        setFilterByNormaliseAbundance(copyMe.getFilterByNormalisedAbundance());
        setDiscardTRRNA(copyMe.getDiscardTRRNA());
        setDiscardTRRNASenseOnly(copyMe.getDiscardTRRNASenseOnly());
        setOutputNonRedundant(copyMe.getOutputNonRedundant());
        setOutputRedundant(copyMe.getOutputRedundant());
        setFilterGenomeHits(copyMe.getFilterGenomeHits());
        setFilterKillList(copyMe.getFilterKillList());
        setAddDiscardLog(copyMe.getAddDiscardLog());
        setGenome(copyMe.getGenome());
        setKillList(copyMe.getKillList());
        setDiscardLog(copyMe.getDiscardLog());
    }

    
    // **** Helpers ****    
    // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
    @SuppressWarnings("unchecked")
    private <T> void setParam(Definition def, T value)    {setParameter(def.getDefinition(), value);}

    // **** Getters ****
    public int      getMinLength()              {return getParameterValue(Integer.class, Definition.MINIMUM_LENGTH.getName());}
    public int      getMaxLength()              {return getParameterValue(Integer.class, Definition.MAXIMUM_LENGTH.getName());}
    public int      getMinAbundance()           {return getParameterValue(Integer.class, Definition.MINIMUM_ABUNDANCE.getName());}
    public int      getMaxAbundance()           {return getParameterValue(Integer.class, Definition.MAXIMUM_ABUNDANCE.getName());}
    public boolean  getNormaliseAbundance()     {return getParameterValue(Boolean.class, Definition.NORMALISE_ABUNDANCE.getName());}
    public boolean  getFilterLowComplexitySeq() {return getParameterValue(Boolean.class, Definition.DISCARD_LOW_COMPLEXITY_SEQS.getName());}
    public boolean  getFilterInvalidSeq()       {return getParameterValue(Boolean.class, Definition.DISCARD_INVALID_SEQS.getName());}
    public boolean  getDiscardTRRNA()           {return getParameterValue(Boolean.class, Definition.DISCARD_TRRNA.getName());}
    public boolean  getDiscardTRRNASenseOnly()  {return getParameterValue(Boolean.class, Definition.DISCARD_TRRNA_SENSE_ONLY.getName());}
    public boolean  getOutputNonRedundant()     {return getParameterValue(Boolean.class, Definition.OUTPUT_NON_REDUNDANT.getName());}
    public boolean  getOutputRedundant()        {return getParameterValue(Boolean.class, Definition.OUTPUT_REDUNDANT.getName());}
    public boolean  getFilterGenomeHits()       {return getParameterValue(Boolean.class, Definition.DISCARD_GENOME_HITS.getName());}
    public boolean  getFilterKillList()         {return getParameterValue(Boolean.class, Definition.DISCARD_KILL_LIST.getName());}
    public boolean  getFilterByNormalisedAbundance()        {return getParameterValue(Boolean.class, Definition.FILTER_NORM_ABUND.getName());}
    public boolean  getAddDiscardLog()          {return getParameterValue(Boolean.class, Definition.ADD_DISCARD_LOG.getName());}
    public File     getGenome()                 {return getParameterValue(File.class,    Definition.GENOME_FILE.getName());}
    public File     getKillList()               {return getParameterValue(File.class,    Definition.KILL_LIST.getName());}
    public File     getDiscardLog()             {return getParameterValue(File.class,    Definition.DISCARD_LOG.getName());}


    // **** Setters ****
    private void setMinLength(int min_length)                       {setParam(Definition.MINIMUM_LENGTH, min_length);}
    private void setMaxLength(int max_length)                       {setParam(Definition.MAXIMUM_LENGTH, max_length);}
    public void setMinAbundance(int min_abundance)                  {setParam(Definition.MINIMUM_ABUNDANCE, min_abundance);}
    public void setMaxAbundance(int max_abundance)                  {setParam(Definition.MAXIMUM_ABUNDANCE, max_abundance);}
    public void setNormaliseAbundance(boolean normalise_abundance)  {setParam(Definition.NORMALISE_ABUNDANCE, normalise_abundance);}
    public void setFilterLowComplexitySeq(boolean low_comp)         {setParam(Definition.DISCARD_LOW_COMPLEXITY_SEQS, low_comp);}
    public void setFilterInvalidSeq(boolean invalid)                {setParam(Definition.DISCARD_INVALID_SEQS, invalid);}
    public void setDiscardTRRNA(boolean trrna)                      {setParam(Definition.DISCARD_TRRNA, trrna);}
    public void setDiscardTRRNASenseOnly(boolean trrna_sense_only)  {setParam(Definition.DISCARD_TRRNA_SENSE_ONLY, trrna_sense_only);}
    public void setOutputNonRedundant(boolean make_nr)              {setParam(Definition.OUTPUT_NON_REDUNDANT, make_nr);}
    public void setOutputRedundant(boolean make_nr)                 {setParam(Definition.OUTPUT_REDUNDANT, make_nr);}
    public void setFilterGenomeHits(boolean genome_hits)            {setParam(Definition.DISCARD_GENOME_HITS, genome_hits);}
    public void setFilterByNormaliseAbundance(boolean norm_abund)   {setParam(Definition.FILTER_NORM_ABUND, norm_abund);}
    public void setFilterKillList(boolean kill_list)                {setParam(Definition.DISCARD_KILL_LIST, kill_list);}
    public void setAddDiscardLog(boolean discard_log)               {setParam(Definition.ADD_DISCARD_LOG, discard_log);}
    public void setGenome(File genome)                              {setParam(Definition.GENOME_FILE, genome);}
    //public void setGenome(String genome_path)                       {setParam(Definition.GENOME_FILE, genome_path);}
    public void setKillList(File kill_list)                         {setParam(Definition.KILL_LIST, kill_list);}
    public void setKillList(String kill_list_path)                  {setParam(Definition.KILL_LIST, kill_list_path);}
    public void setDiscardLog(File discard_log)                     {setParam(Definition.DISCARD_LOG, discard_log);}
    public void setDiscardLog(String discard_log_path)              {setParam(Definition.DISCARD_LOG, discard_log_path);}


    public void setLengthRange(int min_length, int max_length)
    {
        if (min_length > max_length)
        {
            throw new IllegalArgumentException("Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length.");
        }
        setMinLength(min_length);
        setMaxLength(max_length);
    }

    public void setAbundanceRange(int min_abundance, int max_abundance)
    {
        if (min_abundance > max_abundance)
        {
            throw new IllegalArgumentException("Illegal min_abundance and max_abundance parameter values. Valid values: min_abundance <= max_abundance.");
        }
        setMinAbundance(min_abundance);
        setMaxAbundance(max_abundance);
    }


    public String generateFiltersString()
    {
        ArrayList<String> filters = new ArrayList<String>();

        if (this.getFilterLowComplexitySeq())
        {
            filters.add("low complexity");
        }

        filters.add("<" + this.getMinLength() + "nt");
        filters.add(">" + this.getMaxLength() + "nt");
        if (this.getMinAbundance() > 1)
        {
            filters.add("<" + this.getMinAbundance() + "hits");
        }
        if (this.getMaxAbundance() >= 1)
        {
            filters.add(">" + this.getMaxAbundance() + "hits");
        }

        if (this.getDiscardTRRNA())
        {
            filters.add("t/rRNA matches");
        }

        if (this.getGenome() != null)
        {
            filters.add("unmatched to genome " + this.getGenome().getPath());
        }

        if (this.getKillList() != null)
        {
            filters.add("matched to kill-list " + this.getKillList().getPath());
        }

        return StringUtils.join(filters, "; ");
    }


    /**
     * Provides the client with a simple mechanism for setting specific parameters
     * at FilterParams creation time.  Multiple parameter setters can be chained
     * together each returning another Builder object with the specific parameter
     * set.  The build() method creates the actual FilterParams object, which does
     * parameter validation.
     */
    public static final class Builder
    {
        private int min_length              = Definition.MINIMUM_LENGTH.getDefault(Integer.class);
        private int max_length              = Definition.MAXIMUM_LENGTH.getDefault(Integer.class);
        private int min_abundance           = Definition.MINIMUM_ABUNDANCE.getDefault(Integer.class);
        private int max_abundance           = Definition.MAXIMUM_ABUNDANCE.getDefault(Integer.class);
        private boolean norm_abundance      = Definition.NORMALISE_ABUNDANCE.getDefault(Boolean.class);
        private boolean filter_low_comp     = Definition.DISCARD_LOW_COMPLEXITY_SEQS.getDefault(Boolean.class);
        private boolean filter_invalid      = Definition.DISCARD_INVALID_SEQS.getDefault(Boolean.class);
        private boolean trrna               = Definition.DISCARD_TRRNA.getDefault(Boolean.class);
        private boolean trrna_sense_only    = Definition.DISCARD_TRRNA_SENSE_ONLY.getDefault(Boolean.class);
        private boolean output_nr           = Definition.OUTPUT_NON_REDUNDANT.getDefault(Boolean.class);
        private boolean output_r            = Definition.OUTPUT_REDUNDANT.getDefault(Boolean.class);
        private boolean filter_genome_hits  = Definition.DISCARD_GENOME_HITS.getDefault(Boolean.class);
        private boolean filter_norm_abund   = Definition.FILTER_NORM_ABUND.getDefault(Boolean.class);
        private boolean filter_kill_list    = Definition.DISCARD_KILL_LIST.getDefault(Boolean.class);
        private boolean add_discard_log     = Definition.ADD_DISCARD_LOG.getDefault(Boolean.class);
        private File genome                 = Definition.GENOME_FILE.getDefault(File.class);
        private File kill_list              = Definition.KILL_LIST.getDefault(File.class);
        private File discard_log            = Definition.DISCARD_LOG.getDefault(File.class);

        // **** Getters ****
        public int      getMinLength()              {return min_length;}
        public int      getMaxLength()              {return max_length;}
        public int      getMinAbundance()           {return min_abundance;}
        public int      getMaxAbundance()           {return max_abundance;}
        public boolean  getNormaliseAbundance()     {return norm_abundance;}
        public boolean  getFilterByNormalisedAbundance(){return filter_norm_abund;}
        public boolean  getFilterLowComplexitySeq() {return filter_low_comp;}
        public boolean  getFilterInvalidSeq()       {return filter_invalid;}
        public boolean  getDiscardTRRNA()           {return trrna;}
        public boolean  getDiscardTRRNASenseOnly()  {return trrna_sense_only;}
        public boolean  getOutputNonRedundant()     {return output_nr;}
        public boolean  getOutputRedundant()        {return output_r;}
        public boolean  getFilterGenomeHits()       {return filter_genome_hits;}
        public boolean  getFilterKillList()         {return filter_kill_list;}
        public boolean  getAddDiscardLog()          {return add_discard_log;}
        public File     getGenome()                 {return genome;}
        public File     getKillList()               {return kill_list;}
        public File     getDiscardLog()             {return discard_log;}

        // **** Setters ****
        public Builder setMinLength(int min_length)                        {this.min_length = min_length;                   return this;}
        public Builder setMaxLength(int max_length)                        {this.max_length = max_length;                   return this;}
        public Builder setMinAbundance(int min_abundance)                  {this.min_abundance = min_abundance;             return this;}
        public Builder setMaxAbundance(int max_abundance)                  {this.max_abundance = max_abundance;             return this;}
        public Builder setNormaliseAbundance(boolean normalise_abundance)  {this.norm_abundance = normalise_abundance;      return this;}
        public Builder setFilterByNormalisedAbundance(boolean filterNorm)  {this.filter_norm_abund = filterNorm;            return this;}
        public Builder setFilterLowComplexitySeq(boolean low_comp)         {this.filter_low_comp = low_comp;                return this;}
        public Builder setFilterInvalidSeq(boolean invalid)                {this.filter_invalid = invalid;                  return this;}
        public Builder setDiscardTRRNA(boolean trrna)                      {this.trrna = trrna;                             return this;}
        public Builder setDiscardTRRNASenseOnly(boolean trrna_sense_only)  {this.trrna_sense_only = trrna_sense_only;       return this;}
        public Builder setOutputNonRedundant(boolean output_nr)            {this.output_nr = output_nr;                     return this;}
        public Builder setOutputRedundant(boolean output_r)                {this.output_r = output_r;                       return this;}
        //public Builder set
        public Builder setFilterGenomeHits(boolean filter_genome_hits)     {this.filter_genome_hits = filter_genome_hits;   return this;}
        public Builder setFilterKillList(boolean filter_kill_list)         {this.filter_kill_list = filter_kill_list;       return this;}
        public Builder setAddDiscardLog(boolean add_discard_log)           {this.add_discard_log = add_discard_log;         return this;}
        public Builder setGenome(File genome)                              {this.genome = genome;                           return this;}
        public Builder setKillList(File kill_list)                         {this.kill_list = kill_list;                     return this;}
        public Builder setDiscardLog(File discard_log)                     {this.discard_log = discard_log;                 return this;}

        /**
         * Constructs the FilterParams object
         * @return
         */
        public FilterParams build()
        {
          return new FilterParams( this );
        }
    }


    public static void main(String[] args)
    {
        try
        {
            FilterParams ok = FilterParams.load(new FilterParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest1-ok.cfg"));
            FilterParams ko = FilterParams.load(new FilterParams(), new File("C:\\LocalData\\Research\\RNA Tools\\filter\\cfg\\filterparamtest2-ko.cfg"));
        }
        catch(Exception e)
        {
        }
    }
}
