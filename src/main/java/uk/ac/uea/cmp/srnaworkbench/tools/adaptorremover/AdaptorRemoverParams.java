
package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters.ParameterDefinition;

/**
 * Stores and manages parameters for AdaptorRemover
 * @author ezb11yfu
 */
public final class AdaptorRemoverParams extends ToolParameters
{
  /**
   * An enumeration containing a list of definitions for each Adaptor Remover parameter.
   * Use this enum to access parameter default values and limits.
   */
  public enum Definition
  {
    MINIMUM_LENGTH( "min_length", Integer.valueOf( 16 ), Integer.valueOf( 1 ), Integer.valueOf( 49 ) ),
    MAXIMUM_LENGTH( "max_length", Integer.valueOf( 35 ), Integer.valueOf( 9 ), Integer.valueOf( 80 ) ),
    ADAPTOR_3_PRIME_SEQ( "adaptor_sequence_3", "" ),
    ADAPTOR_5_PRIME_SEQ( "adaptor_sequence_5", "" ),
    ADAPTOR_3_LENGTH_TO_USE( "adaptor_sequence_3_length", Integer.valueOf( 8 ), Integer.valueOf( 5 ), Integer.valueOf( 20 ) ),
    ADAPTOR_5_LENGTH_TO_USE( "adaptor_sequence_5_length", Integer.valueOf( 8 ), Integer.valueOf( 5 ), Integer.valueOf( 20 ) ),
    HD_PROTOCOL("HD_full_protocol", HD_Protocol.HD_NONE),
    DISCARD_LOG( "discard_log", (File) null );
    private ParameterDefinition definition;


    private Definition( String name, HD_Protocol default_value )                            { this.definition = new ParameterDefinition<HD_Protocol>( name, default_value ); }
    private Definition( String name, Boolean default_value )                                { this.definition = new ParameterDefinition<Boolean>( name, default_value ); }
    private Definition( String name, String default_value )                                 { this.definition = new ParameterDefinition<String>( name, default_value ); }
    private Definition( String name, File default_value )                                   { this.definition = new ParameterDefinition<File>( name, default_value ); }
    private Definition( String name, Integer default_value, Integer lower, Integer upper )  { this.definition = new ParameterDefinition<Integer>( name, default_value, lower, upper ); }
    private Definition( String name, Float default_value, Float lower, Float upper )        { this.definition = new ParameterDefinition<Float>( name, default_value, lower, upper ); }
    private Definition( String name, Double default_value, Double lower, Double upper )     { this.definition = new ParameterDefinition<Double>( name, default_value, lower, upper ); }

    // ***** Shortcuts to defaults and limits *****
    public String getName()                         { return this.definition.getName(); }
    public <T> T getDefault( Class<T> type )        { return type.cast( this.definition.getDefaultValue() ); }
    public <T> T getLowerLimit( Class<T> type )     { return type.cast( this.definition.getLimits().getLower() ); }
    public <T> T getUpperLimit( Class<T> type )     { return type.cast( this.definition.getLimits().getUpper() ); }
    public ParameterDefinition getDefinition()      { return this.definition; }
  }
  
  //enum used to store the type of HD protocol used in library preperation
  public enum HD_Protocol
  {
    HD_FULL,
    HD_SIMPLE_3P,
    HD_SIMPLE_5P,
    HD_NONE;//default no HD adapter
    
   
  }

  /**
   * An enum containing known 3' adaptor sequences.  Can be used elsewhere to simplify
   * access to these values.
   */
  public enum Known3PrimeAdaptor
  {
    UNKNOWN( "" )
    {
      @Override
      public String toString()
      {
        return "";
      }
    },
    LMN_1( "TCGTATGCCGTCTTCTGCTTG" ),
    LMN_2( "ATCTCGTATGCCGTCTTCTGC" ),
    LMN_3( "TGGAATTCTCGGGTGCCAAGG" ),
    P454_1( "ACTGTAGGCACCATCAAT" );
    
    private String seq;

    private Known3PrimeAdaptor( String seq )
    {
      this.seq = seq;
    }

    /**
     * Gets the sequence associated with this platform
     * @return The sequence
     */
    public String getSequence()
    {
      return this.seq;
    }

    /**
     * Represents the known 3' adaptor as a string where the platform name comes
     * first followed by the sequence in brackets.
     * @return String representation of the known 3' adaptor
     */
    @Override
    public String toString()
    {
      return this.name() + " (" + this.seq + ")";
    }
  }
  
  /**
   * An enum containing known 3' adaptor sequences.  Can be used elsewhere to simplify
   * access to these values.
   */
  public enum Known5PrimeAdaptor
  {
    UNKNOWN( "" )
    {
      @Override
      public String toString()
      {
        return "";
      }
    },
    P454_1( "ACGTAGGCACCTGAAA" );    
    
    private String seq;

    private Known5PrimeAdaptor( String seq )
    {
      this.seq = seq;
    }

    /**
     * Gets the sequence associated with this platform
     * @return The sequence
     */
    public String getSequence()
    {
      return this.seq;
    }

    /**
     * Represents the known 5' adaptor as a string where the platform name comes
     * first followed by the sequence in brackets.
     * @return String representation of the known 5' adaptor
     */
    @Override
    public String toString()
    {
      return this.name() + " (" + this.seq + ")";
    }
  }

  /**
   * Default constructor.  Produces a AdaptorRemoverParams instance with default parameters.
   */
  public AdaptorRemoverParams()
  {
    this( new Builder() );
  }

  /**
   * Assignment constructor using the Builder design pattern.  Should a client
   * wish to use this constructor directly, they are required to pass a AdaptorRemoverParams.Builder
   * object as an argument, which they can use to pick and choose parameters that
   * are not default.
   * @param builder A AdaptorRemoverParams.Builder object containing the AdaptorRemoverParams to
   * use.
   */
  private AdaptorRemoverParams( Builder builder )
  {
    set3PrimeAdaptor( builder.get3PrimeAdaptor() );
    set5PrimeAdaptor( builder.get5PrimeAdaptor() );
    set3PrimeAdaptorLength( builder.get3PrimeAdaptorLength() );
    set5PrimeAdaptorLength( builder.get5PrimeAdaptorLength() );
    setLengthRange( builder.getMinLength(), builder.getMaxLength() );
    setDiscardLog( builder.getDiscardLog() );
    set_HD_Protocol(builder.get_HD_Protocol());
  }

  // **** Helpers ****    
  // Don't put in the wrong valued type with the wrong type of ParameterDefinition!!!
  @SuppressWarnings("unchecked")
  private <T> void setParam( Definition def, T value )
  {
    setParameter( def.getDefinition(), value );
  }

  // **** Getters ****
  public int getMinLength()              { return getParameterValue( Integer.class, Definition.MINIMUM_LENGTH.getName() ); }
  public int getMaxLength()              { return getParameterValue( Integer.class, Definition.MAXIMUM_LENGTH.getName() ); }
  public String get3PrimeAdaptor()       { return getParameterValue( String.class, Definition.ADAPTOR_3_PRIME_SEQ.getName() ); }
  public String get5PrimeAdaptor()       { return getParameterValue( String.class, Definition.ADAPTOR_5_PRIME_SEQ.getName() ); }
  public int get3PrimeAdaptorLength()    { return getParameterValue( Integer.class, Definition.ADAPTOR_3_LENGTH_TO_USE.getName() ); }
  public int get5PrimeAdaptorLength()    { return getParameterValue( Integer.class, Definition.ADAPTOR_5_LENGTH_TO_USE.getName() ); }
  public File getDiscardLog()            { return getParameterValue( File.class, Definition.DISCARD_LOG.getName() ); }
  public HD_Protocol getHD_Setting()     { return getParameterValue( HD_Protocol.class, Definition.HD_PROTOCOL.getName() ); }

  // **** Setters ****
  private void setMinLength( int min_length )               { setParam( Definition.MINIMUM_LENGTH, min_length ); }
  private void setMaxLength( int max_length )               { setParam( Definition.MAXIMUM_LENGTH, max_length ); }
  public void set3PrimeAdaptorLength( int adpt_seq_3_len )  { setParam( Definition.ADAPTOR_3_LENGTH_TO_USE, adpt_seq_3_len ); }
  public void set5PrimeAdaptorLength( int adpt_seq_5_len )  { setParam( Definition.ADAPTOR_5_LENGTH_TO_USE, adpt_seq_5_len ); }
  public void setDiscardLog( File discard_log )             { setParam( Definition.DISCARD_LOG, discard_log ); }
  public void setDiscardLog( String discard_log_path )      { setParam( Definition.DISCARD_LOG, discard_log_path ); }
  public void set_HD_Protocol(HD_Protocol hd_prot)          { setParam( Definition.HD_PROTOCOL, hd_prot ); }

  public void setLengthRange( int min_length, int max_length )
  {
    if ( min_length > max_length )
    {
      throw new IllegalArgumentException( "Illegal min_length and max_length parameter values. Valid values: max_length must be greater than min_length." );
    }
    setMinLength( min_length );
    setMaxLength( max_length );
  }

  /**
   * Validates and sets the 3 prime adaptor.  Automatically converts from RNA
   * to DNA form ('U' -> 'T')
   * @param adpt_seq_3
   */
  public void set3PrimeAdaptor( String adpt_seq_3 )
  {
    //System.out.println(adpt_seq_3);
    if ( adpt_seq_3 == null )
    {
      setParam( Definition.ADAPTOR_5_PRIME_SEQ, null );
      return;
    }

    String temp = adpt_seq_3.trim().toUpperCase().replace( 'U', 'T' );

    for ( int i = 0; i < temp.length(); i++ )
    {
      char c = temp.charAt( i );

      if ( c != 'A' && c != 'C' && c != 'G' && c != 'T' )
      {
        throw new IllegalArgumentException( "Illegal 3' adaptor (adaptor_sequence_3). Valid nucleotides: {A,C,G,T}. 3' adaptor must not be empty." );
      }
    }

    setParam( Definition.ADAPTOR_3_PRIME_SEQ, temp );
  }

  /**
   * Validates and sets the 5 prime adaptor.  Automatically converts from RNA
   * to DNA form ('U' -> 'T')
   * @param adpt_seq_5
   */
  public void set5PrimeAdaptor( String adpt_seq_5 )
  {
    if ( adpt_seq_5 == null )
    {
      setParam( Definition.ADAPTOR_5_PRIME_SEQ, null );
      return;
    }
    

    String temp = adpt_seq_5.trim().toUpperCase().replace( 'U', 'T' );

    for ( int i = 0; i < temp.length(); i++ )
    {
      char c = temp.charAt( i );

      if ( c != 'A' && c != 'C' && c != 'G' && c != 'T' )
      {
        throw new IllegalArgumentException( "Illegal 5' adaptor (adaptor_sequence_5). Valid nucleotides: {A,C,G,T}." );
      }
    }

    setParam( Definition.ADAPTOR_5_PRIME_SEQ, temp );
  }


  public static final class Builder
  {
    private int min_length = Definition.MINIMUM_LENGTH.getDefault( Integer.class );
    private int max_length = Definition.MAXIMUM_LENGTH.getDefault( Integer.class );
    private String adpt_seq_3 = Definition.ADAPTOR_3_PRIME_SEQ.getDefault( String.class );
    private String adpt_seq_5 = Definition.ADAPTOR_5_PRIME_SEQ.getDefault( String.class );
    private int adpt_seq_3_len = Definition.ADAPTOR_3_LENGTH_TO_USE.getDefault( Integer.class );
    private int adpt_seq_5_len = Definition.ADAPTOR_5_LENGTH_TO_USE.getDefault( Integer.class );
    private File discard_log = Definition.DISCARD_LOG.getDefault( File.class );
    private HD_Protocol HD_type = HD_Protocol.HD_NONE;

    // **** Getters ****
    public int getMinLength()           { return min_length; }
    public int getMaxLength()           { return max_length; }
    public String get3PrimeAdaptor()    { return adpt_seq_3; }
    public String get5PrimeAdaptor()    { return adpt_seq_5; }
    public int get3PrimeAdaptorLength() { return adpt_seq_3_len; }
    public int get5PrimeAdaptorLength() { return adpt_seq_5_len; }
    public File getDiscardLog()         { return discard_log; }
    public HD_Protocol get_HD_Protocol(){ return HD_type; }

    // **** Setters ****
    public Builder setMinLength( int min_length )               { this.min_length = min_length;         return this; }
    public Builder setMaxLength( int max_length )               { this.max_length = max_length;         return this; }
    public Builder set3PrimeAdaptor( String adpt_seq_3 )        { this.adpt_seq_3 = adpt_seq_3;         return this; }
    public Builder set5PrimeAdaptor( String adpt_seq_5 )        { this.adpt_seq_5 = adpt_seq_5;         return this; }
    public Builder set3PrimeAdaptorLength( int adpt_seq_3_len ) { this.adpt_seq_3_len = adpt_seq_3_len; return this; }
    public Builder set5PrimeAdaptorLength( int adpt_seq_5_len ) { this.adpt_seq_5_len = adpt_seq_5_len; return this; }
    public Builder setDiscardLog( File discard_log )            { this.discard_log = discard_log;       return this; }
    public Builder set_HD_protocol(HD_Protocol hd_prot)         { this.HD_type = hd_prot;               return this; }

    /**
     * Constructs the FilterParams object
     * @return
     */
    public AdaptorRemoverParams build()
    {
      return new AdaptorRemoverParams( this );
    }
  }

  public static void main( String[] args )
  {
    try
    {
    }
    catch ( Exception e )
    {
    }
  }
}
