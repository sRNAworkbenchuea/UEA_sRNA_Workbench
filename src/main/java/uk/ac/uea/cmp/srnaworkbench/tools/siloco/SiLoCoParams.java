/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.siloco;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 *
 * @author w0445959
 */
public final class SiLoCoParams extends ToolParameters
{
// **** Params ****
  public enum Definition
  {
    MINIMUM_SRNA_LENGTH( "min_length", Integer.valueOf( 16 ), Integer.valueOf( 0 ), Integer.MAX_VALUE ),
    MAXIMUM_SRNA_LENGTH( "max_length", Integer.valueOf( 30 ), Integer.valueOf( 16 ), Integer.MAX_VALUE),
    MINIMUM_SRNA_ABUNDANCE( "min_abundance", Integer.valueOf( 1 ), Integer.valueOf( 1 ), Integer.MAX_VALUE ),
    MAXIMUM_GENOME_HITS( "max_genome_hits", Integer.valueOf( 100 ), Integer.valueOf( 1 ), Integer.MAX_VALUE ),
    MINIMUM_LOCUS_SIZE( "min_locus_size", Integer.valueOf( 1 ), Integer.valueOf( 1 ), Integer.MAX_VALUE ),
    MINIMUM_CLUSTER_SEPARATION_DISTANCE( "cluster_sentinel", Integer.valueOf( 100 ), Integer.valueOf( 1 ), Integer.MAX_VALUE );
    private ParameterDefinition definition;

    private Definition( String name, Boolean default_value )
    {
      this.definition = new ParameterDefinition<Boolean>( name, default_value );
    }

    private Definition( String name, String default_value )
    {
      this.definition = new ParameterDefinition<String>( name, default_value );
    }

    private Definition( String name, File default_value )
    {
      this.definition = new ParameterDefinition<File>( name, default_value );
    }

    private Definition( String name, Integer default_value, Integer lower, Integer upper )
    {
      this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper );
    }

    private Definition( String name, Float default_value, Float lower, Float upper )
    {
      this.definition = new ParameterDefinition<Float>( name, default_value, lower, upper );
    }

    private Definition( String name, Double default_value, Double lower, Double upper )
    {
      this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper );
    }

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

  public SiLoCoParams()
  { 
    this( new Builder() );
  }

  private SiLoCoParams( Builder b )
  {
    setMaxGenomeHits( b.getMaxGenomeHits() );
    setMinConsider( b.getMinConsider() );
    setClusterSentinel( b.getClusterSentinel() );
    setMinLength( b.getMinLength() );
    setMaxLength( b.getMaxLength() );
    setMinLocusSize( b.getMinLocusSize() );

  }

  public SiLoCoParams( int min_hits,
                       int max_genome_hits,
                       int min_length,
                       int max_length,
                       int maxUnique,
                       int min_consider,
                       int cluster_sentinel ) throws IllegalArgumentException
  {



    this( new Builder().setMinConsider( min_consider ).setMaxGenomeHits( max_genome_hits ).setMinLength( min_length ).
      setMaxLength( max_length ).setClusterSentinel( cluster_sentinel ) );
  }
  

  public static SiLoCoParams createDefaultAnimalParams()
  {
    return new Builder().setMinConsider( 2 ).setMaxGenomeHits( 10 ).setMinLength( 19 ).setMaxLength( 24 ).setClusterSentinel( 100 ).build();
  }

  /**
   * Creates a new SiLoCoParams object with parameters set to default for plant jobs
   * @return Default parameters for plant jobs
   */
  public static SiLoCoParams createDefaultPlantParams()
  {
    return new SiLoCoParams();
  }

  @SuppressWarnings("unchecked")
  private <T> void setParam( Definition def, T value )
  {
    setParameter( def.getDefinition(), value );
  }

  // **** Setters ****
  public void setMaxGenomeHits( int max_genome_hits )
  {
    setParam( Definition.MAXIMUM_GENOME_HITS, max_genome_hits );
  }

  private void setMinLength( int min_length )
  {
    setParam( Definition.MINIMUM_SRNA_LENGTH, min_length );
  }

  private void setMaxLength( int max_length )
  {
    setParam( Definition.MAXIMUM_SRNA_LENGTH, max_length );
  }

  public void setMinLocusSize( int min_locus_size )
  {
    setParam( Definition.MINIMUM_LOCUS_SIZE, min_locus_size );
  }

  public void setMinConsider( int min_consider )
  {
    setParam( Definition.MINIMUM_SRNA_ABUNDANCE, min_consider );
  }

  public void setClusterSentinel( int cluster_sentinel )
  {
    setParam( Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE, cluster_sentinel );
  }

  // **** Getters ****
  public int getMaxGenomeHits()
  {
    return getParameterValue( Integer.class, Definition.MAXIMUM_GENOME_HITS.getName() );
  }

  public int getMinLength()
  {
    return getParameterValue( Integer.class, Definition.MINIMUM_SRNA_LENGTH.getName() );
  }

  public int getMaxLength()
  {
    return getParameterValue( Integer.class, Definition.MAXIMUM_SRNA_LENGTH.getName() );
  }

  public int getMinLocusSize()
  {
    return getParameterValue( Integer.class, Definition.MINIMUM_LOCUS_SIZE.getName() );
  }

  public int getMinConsider()
  {
    return getParameterValue( Integer.class, Definition.MINIMUM_SRNA_ABUNDANCE.getName() );
  }

  public int getClusterSentinel()
  {
    return getParameterValue( Integer.class, Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getName() );
  }

  public static final class Builder
  {
    private int max_genome_hits = Definition.MAXIMUM_GENOME_HITS.getDefault( Integer.class );
    private int min_locus_size = Definition.MINIMUM_LOCUS_SIZE.getDefault( Integer.class );
    private int min_consider = Definition.MINIMUM_SRNA_ABUNDANCE.getDefault( Integer.class );
    private int cluster_sentinel = Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefault( Integer.class );
    private int min_length = Definition.MINIMUM_SRNA_LENGTH.getDefault( Integer.class );
    private int max_length = Definition.MAXIMUM_SRNA_LENGTH.getDefault( Integer.class );

    // **** Getters ****
    public int getMaxGenomeHits()
    {
      return max_genome_hits;
    }

    public int getMinLength()
    {
      return min_length;
    }

    public int getMaxLength()
    {
      return max_length;
    }

    public int getMinLocusSize()
    {
      return min_locus_size;
    }

    public int getMinConsider()
    {
      return min_consider;
    }

    public int getClusterSentinel()
    {
      return cluster_sentinel;
    }

    // **** Setters ****
    public Builder setMaxGenomeHits( int max_genome_hits )
    {
      this.max_genome_hits = max_genome_hits;
      return this;
    }

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

    public Builder setMinLocusSize( int min_locus_size )
    {
      this.min_locus_size = min_locus_size;
      return this;
    }

    public Builder setMinConsider( int min_consider )
    {
      this.min_consider = min_consider;
      return this;
    }

    public Builder setClusterSentinel( int cluster_sentinel )
    {
      this.cluster_sentinel = cluster_sentinel;
      return this;
    }

    /**
     * Constructs the FilterParams object
     * @return
     */
    public SiLoCoParams build()
    {
      return new SiLoCoParams( this );
    }
  }
}
