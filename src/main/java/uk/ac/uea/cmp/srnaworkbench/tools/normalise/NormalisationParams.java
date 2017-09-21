/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.normalise;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 *
 * @author w0445959
 */
public final class NormalisationParams extends ToolParameters
{
  public static final String STR_AUTO_CHOOSE = "<choose automatically>";
  public enum Definition
  {
    
    // Filtering parameters - in the case that filtering is used
// Filtering parameters - in the case that filtering is used
    MIN_SRNA_LENGTH ("min_length", Integer.valueOf(16), Integer.valueOf(9), Integer.valueOf(60)),
    MAX_SRNA_LENGTH ("max_length", Integer.valueOf(35), Integer.valueOf(17), Integer.valueOf(61)),
    
    // Output options
    OUTPUT_SUFFIX ("output_suffix", "_norm"),

    // Per Total parameters
    MINIMUM_SRNA_ABUNDANCE              ("min_abundance",           Integer.valueOf(1),     Integer.valueOf(1),     Integer.MAX_VALUE),
    ABUNDANCE_DIST_LOG_BASE             ( "log_base", Integer.valueOf( 10 ), Integer.valueOf( 1 ), Integer.MAX_VALUE ),
    WEIGHT_BY_HITS                      ("weight_by_hits", Boolean.valueOf( false )),
    
    // TMM parameters. Based on the paper, M trimming defaults to 30% and A
    // trimming to 5%
    // No more than 50%, though setting to 50% will lead to ALL values being removed.
    M_TRIM_PERCENTAGE ("percentage_m_trim", Integer.valueOf(30), Integer.valueOf(0), Integer.valueOf(50)),
    A_TRIM_PERCENTAGE ("percentage_a_trim", Integer.valueOf(5), Integer.valueOf(0), Integer.valueOf(50)),
    WEIGHT_FACTORS ("weight_factors", Boolean.valueOf( true )),
    
    REFERENCE_SAMPLE_NAME ("reference_sample_name", NormalisationParams.STR_AUTO_CHOOSE),
    
    //Qnorm
    RNASEQ_QNORM ("rnaseq_quantile_normalisation", Boolean.valueOf( true ));
    
    private ParameterDefinition definition;

    private Definition( String name, Boolean default_value )    { this.definition = new ParameterDefinition<Boolean>( name, default_value ); }
    private Definition( String name, String default_value )    {  this.definition = new ParameterDefinition<String>( name, default_value ); }
    private Definition( String name, File default_value )    { this.definition = new ParameterDefinition<File>( name, default_value ); }
    private Definition( String name, Integer default_value, Integer lower, Integer upper ) { this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper ); }
    private Definition( String name, Float default_value, Float lower, Float upper ) { this.definition = new ParameterDefinition<Float>( name, default_value, lower, upper ); }
    private Definition( String name, Double default_value, Double lower, Double upper ) { this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper ); }

    // ***** Shortcuts to defaults and limits *****
    public String getName()
    {
      return this.definition.getName();
    }

    public <T> T getDefault( Class<T> type )
    {
      return type.cast( this.definition.getDefaultValue() );
    }

    public <T> T getLowerLimit( Class<T> type )
    {
      return type.cast( this.definition.getLimits().getLower() );
    }

    public <T> T getUpperLimit( Class<T> type )
    {
      return type.cast( this.definition.getLimits().getUpper() );
    }

    public ParameterDefinition getDefinition()
    {
      return this.definition;
    }
  }

  public NormalisationParams()
  { 
    this( new Builder() );
  }

  private NormalisationParams( Builder b )
  {
    setMinLength(b.getMinLength());
    setMaxLength(b.getMaxLength());
    setOutSfx(b.getOutSfx());
    setLogBase(b.getLogBase());
    setMinConsider( b.getMinConsider() );
    setWeightByHits( b.isWeightByHits() );
    setMtrim( b.getMtrim() );
    setAtrim( b.getAtrim() );
    setWeightFactors(b.isWeightFactors());
    setRefSample( b.getRefSample() );
    setRNASEQqnorm( b.isRNASEQqnorm() );

  }

  public NormalisationParams( int min_length, int max_length, String out_sfx,
                          int log_base, int min_consider, boolean weightByHits,
                          int mTrim, int aTrim,  boolean weightFactors, 
                          String refSample, boolean rnaseqQnorm) throws IllegalArgumentException
  {
    this( new Builder()
      .setMinLength( min_length )
      .setMaxLength( max_length )
      .setOutSfx( out_sfx )
      .setLogBase( log_base )
      .setMinConsider( min_consider )
            .setWeightByHits( weightByHits )
      .setMtrim( mTrim )
      .setAtrim( aTrim )
      .setWeightFactors( weightFactors )
      .setRefSample( refSample )
      .setRNASEQqnorm( rnaseqQnorm ));
  }
  

  /**
   * Creates a new SiLoCoParams object with parameters set to default for plant jobs
   * @return Default parameters for plant jobs
   */
  public static NormalisationParams createDefaultParams()
  {
    return new NormalisationParams();
  }

  @SuppressWarnings("unchecked")
  private <T> void setParam( Definition def, T value )
  {
    setParameter( def.getDefinition(), value );
  }

  // **** Setters ****
  
  public void setMinLength( int min_length ) { setParam( Definition.MIN_SRNA_LENGTH, min_length); }
  public void setMaxLength( int max_length ) { setParam( Definition.MAX_SRNA_LENGTH, max_length); }
  
  public void setOutSfx( String out_sfx ) { setParam( Definition.OUTPUT_SUFFIX, out_sfx ); }
 
  public void setLogBase( int log_base )
  {
    setParam( Definition.ABUNDANCE_DIST_LOG_BASE, log_base );
  }
  public void setMinConsider( int min_consider )
  {
    setParam( Definition.MINIMUM_SRNA_ABUNDANCE, min_consider );
  }
    public void setWeightByHits( boolean weight_by_hits )
  {
    setParam( Definition.WEIGHT_BY_HITS, weight_by_hits );
  }
  
  // TMM
  public void setMtrim ( int m_trim )
  {
    setParam( Definition.M_TRIM_PERCENTAGE, m_trim );
  }
  public void setAtrim ( int a_trim )
  {
    setParam( Definition.A_TRIM_PERCENTAGE, a_trim );
  }
  public void setWeightFactors( boolean weightFactors )
  {
    setParam( Definition.WEIGHT_FACTORS, weightFactors);
  }
  
  public void setRefSample ( String refSample ){
    setParam( Definition.REFERENCE_SAMPLE_NAME, refSample);
  }
  
  public void setRNASEQqnorm ( boolean rnaseqQnorm )
  {
    setParam( Definition.RNASEQ_QNORM, rnaseqQnorm );
  }

  // **** Getters ****
  public int getMinLength() { return getParameterValue( Integer.class, Definition.MIN_SRNA_LENGTH.getName());}
  public int getMaxLength() { return getParameterValue( Integer.class, Definition.MAX_SRNA_LENGTH.getName());}

  public String getOutSfx() { return getParameterValue( String.class, Definition.OUTPUT_SUFFIX.getName());}
  public int getLogBase() { return getParameterValue( Integer.class, Definition.ABUNDANCE_DIST_LOG_BASE.getName() );}

  public int      getMinConsider()            {return getParameterValue(Integer.class, Definition.MINIMUM_SRNA_ABUNDANCE.getName());}
  public boolean isWeightByHits() { return getParameterValue(Boolean.class, Definition.WEIGHT_BY_HITS.getName() ); }
  
  //TMM
  public int getMtrim() { return getParameterValue(Integer.class, Definition.M_TRIM_PERCENTAGE.getName()); }
  public int getAtrim() { return getParameterValue(Integer.class, Definition.A_TRIM_PERCENTAGE.getName()); }
  public boolean isWeightFactors() { return getParameterValue(Boolean.class, Definition.WEIGHT_FACTORS.getName()); }
  
  public String getRefSample() { return getParameterValue(String.class, Definition.REFERENCE_SAMPLE_NAME.getName()); }
  
  public boolean isRNASEQqnorm() { return getParameterValue(Boolean.class, Definition.RNASEQ_QNORM.getName()); }

  
  public static final class Builder
  {
    private int min_length = Definition.MIN_SRNA_LENGTH.getDefault( Integer.class );
    private int max_length = Definition.MAX_SRNA_LENGTH.getDefault( Integer.class );
    
    private String out_sfx = Definition.OUTPUT_SUFFIX.getDefault( String.class );
    
    private int log_base = Definition.ABUNDANCE_DIST_LOG_BASE.getDefault( Integer.class );
    private int min_consider = Definition.MINIMUM_SRNA_ABUNDANCE.getDefault( Integer.class );
    private boolean weight_by_hits = Definition.WEIGHT_BY_HITS.getDefault( Boolean.class );
    
    private int mTrim = Definition.M_TRIM_PERCENTAGE.getDefault( Integer.class );
    private int aTrim = Definition.A_TRIM_PERCENTAGE.getDefault( Integer.class );
    private boolean weightFactors = Definition.WEIGHT_FACTORS.getDefault( Boolean.class );
    
    private String refSample = Definition.REFERENCE_SAMPLE_NAME.getDefault( String.class );
    
    private boolean rnaseqQnorm = Definition.RNASEQ_QNORM.getDefault( Boolean.class );
        
    // **** Getters ****
    public int getMinLength(){ return min_length; }
    public int getMaxLength(){ return max_length; }
    
    public String getOutSfx(){ return out_sfx; }

    public int getLogBase() { return log_base; }

    public int getMinConsider() { return min_consider; }
    public boolean isWeightByHits() { return weight_by_hits; }
    
    public int getMtrim() { return mTrim; }
    public int getAtrim() { return aTrim; }
    public boolean isWeightFactors() { return weightFactors; }
    
    public String getRefSample() { return this.refSample; }
    
    public boolean isRNASEQqnorm() {return this.rnaseqQnorm; }
   
    // **** Setters ****
    public Builder setMinLength( int min_length )
    {
      this.min_length = min_length;
      return this;
    }
    public Builder setMaxLength( int max_length )
    {
      this.max_length = max_length;
      return this;
    }
    
    public Builder setOutSfx( String out_sfx )
    {
      this.out_sfx = out_sfx;
      return this;
    }
    
    public Builder setLogBase( int log_base )
    {
      this.log_base = log_base;
      return this;
    }
    public Builder setMinConsider( int min_consider )
    {
      this.min_consider = min_consider;
      return this;
    }
    public Builder setWeightByHits( boolean weight_by_hits )
    {
      this.weight_by_hits = weight_by_hits;
      return this;
    }
    
    public Builder setMtrim( int mTrim ){
      this.mTrim = mTrim;
      return this;
    }
    
    public Builder setAtrim( int aTrim ){
      this.aTrim = aTrim;
      return this;
    }
    
    public Builder setWeightFactors( boolean weightFactors )
    {
      this.weightFactors = weightFactors;
      return this;
    }
    
    public Builder setRefSample ( String refSample ){
      this.refSample = refSample;
      return this;
    }
    
    public Builder setRNASEQqnorm( boolean rnaseqQnorm )
    {
      this.rnaseqQnorm = rnaseqQnorm;
      return this;
    }

    /**
     * Constructs the FilterParams object
     * @return
     */
    public NormalisationParams build()
    {
      return new NormalisationParams( this );
    }
  }
}
