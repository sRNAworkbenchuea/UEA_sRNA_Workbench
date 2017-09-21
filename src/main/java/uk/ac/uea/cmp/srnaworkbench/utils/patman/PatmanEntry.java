/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;



/**
 * Manages an entry in a {@link Patman} results file.  Provides mechanism
 * to parse the line from the file into this object and write it back out the same
 * way.
 * @author Dan Mapleson
 */
public class PatmanEntry implements SequenceRangeI
{
  // Want to break up a String like "1  AAGAGAAGAGAGAGA(100)  100 200 + 0" into its tokens
  //private static final Pattern PATTERN = Pattern.compile( "[\\t\\(\\)]");
  private static final Pattern TAB_PATTERN = Pattern.compile( "\\t" );
  private static final Pattern PARANTHESES_PATTERN = Pattern.compile( "[\\(\\)]" );

  private static final double DEFAULT_ABUNDANCE = 1;

  private String long_read_header;
  private String short_read_header;
  // Note: this is not in a separate column of the patman file, but it is usually contained
  // within the short_read_header column in brackets, especially within the workbenches context, so we
  // extract the abundance from there.
  private String short_read_seq;
  private double abundance;
  // Note: start and end are 1-based - they are the base positions
  private int start;
  private int end;
  private SequenceStrand strandedness;
  private int mismatches;

  // -1 indicates 'use the default'
  private float glyphHeightScalePercent = -1f;

  /**
   * Creates an empty PatmanEntry object.
   */
  public PatmanEntry()
  {
    this( null, null, -1, -1, SequenceStrand.UNKNOWN, 0 );
  }
  public PatmanEntry( PatmanEntry ele )
  {
    //String long_read_header, String short_read_seq, double abundance, int start, int end,
                      //SequenceStrand strandedness, int mismatches
    //ele.long_read_header = "";
    this(ele.long_read_header, ele.short_read_seq, ele.abundance, ele.start, ele.end, ele.strandedness, ele.mismatches);
  }
  
  public PatmanEntry(int start, int end)
  {
    this( null, null, start, end, SequenceStrand.UNKNOWN, 0 );
  }

  /**
   * Creates a {@link Patman} entry from the specified parameters.
   * We'll almost always want to use this PatmanEntry constructor in the workbench,
   * as it's important to use abundance information directly from the patman file in a lot of the tools.
   * @param long_read_header The header of the matched region in the long reads file
   * @param short_read_seq The header of the sequence that matched in the short reads file
   * @param abundance The abundance of the short read
   * @param start The start location in the long read region that matched
   * @param end The end location in the long read region that matched
   * @param strandedness Whether the match was to the + or - strand in the long read
   * @param mismatches The number of mismatches between the short read and the long read region that matched
   */
  public PatmanEntry( String long_read_header, String short_read_seq, double abundance, int start, int end,
                      SequenceStrand strandedness, int mismatches )
  {
    this( long_read_header, short_read_seq + "(" + abundance + ")",
      start, end, strandedness, mismatches );

    this.short_read_seq = short_read_seq;
    this.abundance = abundance;
  }

  /**
   * Creates a {@link Patman} entry from the specified parameters.  This particular
   * constructor variant is for general patman usage where the short read header
   * does not necessarily contain abundance information.
   * @param long_read_header The header of the matched region in the long reads file
   * @param short_read_header The header of the sequence that matched in the short reads file
   * @param start The start location in the long read region that matched
   * @param end The end location in the long read region that matched
   * @param strandedness Whether the match was to the + or - strand in the long read
   * @param mismatches The number of mismatches between the short read and the long read region that matched
   */
  public PatmanEntry( String long_read_header, String short_read_header, int start, int end,
                      SequenceStrand strandedness, int mismatches )
  {
    this.long_read_header = long_read_header;
    this.short_read_header = short_read_header;
    this.short_read_seq = null;
    this.abundance = DEFAULT_ABUNDANCE;
    this.start = start;
    this.end = end;
    this.strandedness = strandedness;
    this.mismatches = mismatches;
  }

  // This is only some of the getters, the others are implemented via SequenceRangeI
  public String getLongReadHeader()   {return this.long_read_header;}
  public String getShortReadHeader()  {return this.short_read_header;}
  public double getAbundance()           {return this.abundance;}
  public int getStart()               {return this.start;}
  public int getEnd()                 {return this.end;}
  public int getMismatches()          {return this.mismatches;}
  public String getPosition()         {return this.long_read_header + "," + this.start + "," + this.strandedness.getCharCode();}

  // SequenceRangeI implementation
  //
  @Override public int getStartIndex()                           { return start - 1; }
  @Override public int getEndIndex()                             { return end - 1; }
  @Override public String getSequence()                          { return short_read_seq; }
  @Override public int getSequenceLength()                       { return short_read_seq.length(); }
  @Override public SequenceStrand getSequenceStrand()            { return strandedness; }
  @Override public String getSequenceId()                        { return long_read_header; }
  @Override public Map<String, String> getAttributes()           { return null; }
  @Override public void addAttribute( String key, String value ) {}
  @Override public String getAttribute( String key )             { return null; }
  @Override public SequenceRangeI getParentSequenceRangeI()      { return null; }
  @Override public float getGlyphHeightScalePercent()            { return glyphHeightScalePercent == -1f ? 100 : glyphHeightScalePercent; }
  @Override public void setGlyphHeightScalePercent( float pc )   { glyphHeightScalePercent = Math.min( 100f, Math.max( 1f, pc ) ); }
  //
  // End of SequenceRangeI implementation

  public int getLength()
  {
    // TODO: Should assert that 'match_str.length() == end - start + 1'
    return end - start + 1;
  }

  public int getLengthInRange( int rangeStartIndex, int rangeEndIndex )
  {
    /*
     * Range is between x and y:   -----x-------------------------------y-----
     * Seq can be                   ---                  OR               ---
     *                                -----              OR           -----
     *                                                   OR   -----
     */

    // start and end are base positions (1-based) => make them 0-based
    int startIndex = start - 1;
    int endIndex = end - 1;

    if ( endIndex < rangeStartIndex || rangeEndIndex <= startIndex )
    {
      return 0;
    }

    if ( rangeStartIndex <= startIndex && endIndex < rangeEndIndex )
    {
      return getLength();
    }

    // If we are here then the sequence straddles one (or both !?) of the range limits
    int num = Math.min( endIndex, rangeEndIndex - 1 ) - Math.max( startIndex, rangeStartIndex ) + 1;

    return num;
  }

  @Override
  public String toString()
  {
    return StringUtils.nullSafeConcatenation( "[id=", long_read_header, ", seq=", short_read_seq,
      ", abundance=", abundance, ", start=", start, ", end=", end, ", length=", getLength(),
      ", polarity=", strandedness, ", mismatches=", mismatches, "]" );
  }

  /**
   * Creates a new PatmanEntry from the given String.  This method is often used
   * when reading a patman file from disk.  It can handle UEA style short read data
   * as well as generic short read data.
   * @param line The line to convert into a PatmanEntry object
   * @return The newly created PatmanEntry object
   */
  private static String[] lineSeparatedValue;
  private static String[] short_read_header_array;
  public static PatmanEntry parse( String line )
  {
      try{
          
      
    lineSeparatedValue = TAB_PATTERN.split( line );

    String long_read_header = lineSeparatedValue[0];

    short_read_header_array = PARANTHESES_PATTERN.split( lineSeparatedValue[1] );

    String short_read_seq = ( short_read_header_array.length > 0 ? short_read_header_array[0] : "" );
    double abundance = ( short_read_header_array.length > 1 ? Double.parseDouble( short_read_header_array[1] ) : DEFAULT_ABUNDANCE );

    int start = Integer.parseInt( lineSeparatedValue[2] );
    int end = Integer.parseInt( lineSeparatedValue[3] );
    SequenceStrand polarity = "+".equals( lineSeparatedValue[4] ) ? SequenceStrand.POSITIVE : SequenceStrand.NEGATIVE;
    int mismatches = Integer.parseInt( lineSeparatedValue[5] );
    
    short_read_header_array = null;
    lineSeparatedValue = null;
        return new PatmanEntry( long_read_header, short_read_seq, abundance, start, end, polarity, mismatches );

      }
      catch(NullPointerException e)
      {
          LOGGER.log(Level.WARNING, "A ling in the input file or alignment caused an error and was ignored");
      }
      
      return null;

  }
 

  public static void main( String[] args )
  {
    PatmanEntry.parse( "TGGCAGTGTGGTTAGCTGGTTGT	aga-miR-34 MIMAT0005526 Anopheles gambiae miR-34	1	20	+	0" );
    PatmanEntry.parse( "TGACTAGATCCACACTCATCCA	bmo-miR-279a MIMAT0004204 Bombyx mori miR-279a	1	19	+	0" );
  }

  public void setAbundance( double value )
  {
    this.abundance = value;
  }
}
