
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeI;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;


/**
 * A Plot Record for moving around plots between applications.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public final class PlotRecord implements SequenceRangeI
{
  /** Width in bases of a PlotRecord in VisSR - the gene length is ignored intentionally (irrelevant to the visualisation) */
  private static final int VISUALISATION_WIDTH = 92;

  /** Number of spaces which will be placed between records in VisSR */
  public static final int VISUALISATION_GAP_WIDTH = 8;

  public static final float UNKNOWN_PVALUE = 100f;

  private static final String START_RECORD = "START_RECORD";
  private static final String END_RECORD   = "END_RECORD";

  /** Used by VisSR */
  private int _startIndex = 0;
  private float _heightScaling = 1;
  /** Used by VisSR */

  private String geneId;
  private int geneLength;
  private Category maxCategory = Category.UNDEFINED;
  private float medianDegradomeHitAbundance;

  private float maxDegradomeHitAbundance = Float.MIN_VALUE;
  private int maxSmallRNAHitAbundance    = Integer.MIN_VALUE;

  private final ArrayList< DegradomeHit > _degHits = new ArrayList< DegradomeHit >();
  private ArrayList< SmallRNAHit > _srnaHits;

  // The integer positions at which cleavage occurs. There will be one degradome hit at each position and 0,1,2... smallRNA hits.
  private final HashSet< Integer > _cleavagePositions = new HashSet< Integer >();

  public PlotRecord() {}

  public boolean hasSRNAhit()
  {
    return _srnaHits != null && _srnaHits.size() > 0;
  }

  /**
   * Return free-text gene identifier
   * @return Guaranteed not to be null
   */
  public String getGeneId()
  {
    return geneId == null ? "" : geneId;
  }

  public void setGeneId( String geneId )
  {
    this.geneId = geneId;
  }

  public int getGeneLength()
  {
      return geneLength;
  }

  public void setGeneLength( int length )
  {
    this.geneLength = length;
  }

  public Category getMaxCategory()
  {
    return maxCategory;
  }

  public float getMedianDegradomeHitAbundance()
  {
    return medianDegradomeHitAbundance;
  }

  public void setMedianDegradomeHitAbundance( float medianDegradomeHitAbundance )
  {
    this.medianDegradomeHitAbundance = medianDegradomeHitAbundance;
  }

  /**
   * Use {@code getMedianDegradomeHitAbundance()}
   *
   * @return
   * @deprecated
   */
  public float getMedianValue()
  {
    return getMedianDegradomeHitAbundance();
  }

  /**
   * Use {@code setMedianDegradomeHitAbundance(float)}
   *
   * @param medianValue
   * @deprecated
   */
  public void setMedianValue( float medianValue )
  {
    setMedianDegradomeHitAbundance( medianValue );
  }

  public float getMaxDegradomeHitAbundance()
  {
    return maxDegradomeHitAbundance;
  }

  public int getMaxSmallRNAHitAbundance()
  {
    return maxSmallRNAHitAbundance;
  }

  public void addD_Hit( int position, float abundance )
  {
    DegradomeHit dh = new DegradomeHit( abundance, position );
    _degHits.add( dh );

    maxDegradomeHitAbundance = Math.max( abundance, maxDegradomeHitAbundance );

    _cleavagePositions.add( position );
  }

  public void addS_Hit( int position, int abundance, int category, float alignScore, float pValue, String alignmentSRNA, String alignmentBars, String alignmentMRNA )
  {
    Category catObj = Category.getCategory( category );

    if ( _srnaHits == null )
    {
      _srnaHits = new ArrayList< SmallRNAHit >();
    }

    SmallRNAHit srnaHit = new SmallRNAHit( position, abundance, catObj, alignScore, pValue, alignmentSRNA, alignmentBars, alignmentMRNA );

    _srnaHits.add( srnaHit );

    if ( srnaHit.getCategory().getCategoryValue() < maxCategory.getCategoryValue() )
    {
      maxCategory = srnaHit.getCategory();
    }

    maxSmallRNAHitAbundance = Math.max( abundance, maxSmallRNAHitAbundance );

    _cleavagePositions.add( position );
  }

  public List<DegradomeHit> getDegradomeHits()
  {
    return Collections.unmodifiableList( _degHits );
  }

  public DegradomeHit getDegradomeHitForPosition( int cleavagePosition )
  {
    DegradomeHit result = null;

    if ( _cleavagePositions.contains( cleavagePosition ) )
    {
      for ( DegradomeHit dh : _degHits )
      {
        if ( cleavagePosition == dh.getPosition() )
        {
          result = dh;
          break;
        }
      }
    }

    return result;
  }

  public List<SmallRNAHit> getSmallRNAHits()
  {
    if ( _srnaHits == null )
      return Collections.emptyList();

    return Collections.unmodifiableList( _srnaHits );
  }

  public List< SmallRNAHit > getSmallRNAHitsForPosition( int cleavagePosition )
  {
    if ( ! hasSRNAhit() || ! _cleavagePositions.contains( cleavagePosition ) )
    {
      return Collections.emptyList();
    }

    List< SmallRNAHit > result = CollectionUtils.newArrayList();

    for ( SmallRNAHit sh : _srnaHits )
    {
      if ( cleavagePosition == sh.getPosition() )
      {
        result.add( sh );
      }
    }

    return result;
  }

  public static void outputPlotRecord( BufferedWriter out, PlotRecord pr ) throws IOException
  {
    final String NL = LINE_SEPARATOR;
    StringBuilder sb = new StringBuilder( 2500 );

    sb.append( PlotRecord.START_RECORD ).append( NL );
    sb.append( "1_" ).append( pr.maxCategory.getCategoryValue() ).append( NL );
    sb.append( "2_" ).append( pr.geneId ).append( NL );

    for ( DegradomeHit dh : pr._degHits )
    {
      sb.append( "3_" )
        .append( dh.getPosition() ).append( "_" )
        .append( dh.getAbundance() ).append( NL );
    }

    if ( pr.hasSRNAhit() )
    {
      for ( SmallRNAHit sh : pr._srnaHits )
      {
        sb.append( "4_" )
          .append( sh.getPosition() ).append( "_" )
          .append( sh.getAbundance() ).append( "_" )
          .append( sh.getCategory().getCategoryValue() ).append( "_" )
          .append( sh.getScore() ).append( "_" )
          .append( sh.getAlignmentSRNA() ).append( "_" )
          .append( sh.getAlignmentBars() ).append( "_" )
          .append( sh.getAlignmentMRNA() ).append( "_" )
          .append( sh.getPValue() ).append( NL );
      }
    }

    sb.append( "5_" ).append( pr.geneLength ).append( NL );
    sb.append( "6_" ).append( pr.medianDegradomeHitAbundance ).append( NL );
    sb.append( PlotRecord.END_RECORD ).append( NL );

    out.write( sb.toString() );
  }

  public static PlotRecord inputPlotRecord( BufferedReader in ) throws IOException
  {
    String lineIn = in.readLine();

    if ( ! START_RECORD.equalsIgnoreCase( lineIn ) )
    {
      throw new IOException( "Unable to read PAREsnip results - check the format." );
    }

    PlotRecord pr = new PlotRecord();

    lineIn = in.readLine();

    while ( ! END_RECORD.equalsIgnoreCase( lineIn ) )
    {
      String[] tokens = lineIn.split( "_" );

      int dataType = Integer.parseInt( tokens[0] );

      switch ( dataType )
      {
        case 1:
        {
          int cat = Integer.parseInt( tokens[1] );
          pr.maxCategory = Category.getCategory( cat );
          break;
        }
        case 2:
        {
          pr.setGeneId( tokens[1] );
          break;
        }
        case 3:
        {
          int position = Integer.parseInt( tokens[1] );
          float abundance = Float.parseFloat( tokens[2] );
          pr.addD_Hit( position , abundance );
          break;
        }
        case 4:
        {
          int position  = Integer.parseInt( tokens[1] );
          int abundance = Integer.parseInt( tokens[2] );
          int category  = Integer.parseInt( tokens[3] );
          float alignScore = Float.parseFloat( tokens[4] );
          String alignmentSRNA = tokens[5];
          String alignmentBars = tokens[6];
          String alignmentMRNA = tokens[7];

          float pValue = UNKNOWN_PVALUE;
          if ( tokens.length == 9 )
          {
            // We need to check the length of the split array because the p-value was added later.
            pValue = Float.parseFloat( tokens[8] );
          }

          pr.addS_Hit( position, abundance, category, alignScore, pValue, alignmentSRNA, alignmentBars, alignmentMRNA );
          break;
        }
        case 5:
        {
          pr.setGeneLength( Integer.parseInt( tokens[1] ) );
          break;
        }
        case 6:
        {
          pr.setMedianDegradomeHitAbundance( Float.parseFloat( tokens[1] ) );
          break;
        }
      }

      lineIn = in.readLine();
    }

    return pr;
  }

  public void setStartIndex( int i )
  {
    _startIndex = i;
  }

  // Start of SequenceRangeI implementation
  //
  @Override public int getStartIndex()                           { return _startIndex; }
  @Override public int getEndIndex()                             { return _startIndex + VISUALISATION_WIDTH - 1; }
  @Override public String getSequence()                          { return ""; }
  @Override public int getSequenceLength()                       { throw new UnsupportedOperationException( "Unsupported - visualisation width is constant, gene length is given by getGeneLength() !" ); }
  @Override public SequenceStrand getSequenceStrand()            { return SequenceStrand.UNKNOWN; }
  @Override public String getSequenceId()                        { return geneId; }
  @Override public SequenceRangeI getParentSequenceRangeI()      { return null; }
  @Override public Map<String, String> getAttributes()           { return null; }
  @Override public void addAttribute( String key, String value ) { throw new UnsupportedOperationException( "Attributes are not supported here!" ); }
  @Override public String getAttribute( String key )             { return null; }
  @Override public float getGlyphHeightScalePercent()            { return _heightScaling; }
  @Override public void setGlyphHeightScalePercent( float pc )   { _heightScaling = Math.min( 100f, Math.max( 1f, pc ) );
  }
  //
  // End of SequenceRangeI implementation

  public boolean containsSearchKey( String searchFor )
  {
    // Do a case-sensitive search in the gene identifier
    if ( getGeneId().indexOf( searchFor ) != -1 )
      return true;

    // Do a case-insensitive search in the gene identifier
    if ( getGeneId().toUpperCase().indexOf( searchFor.toUpperCase() ) != -1 )
      return true;

    // Check mirbase ids and nt bases
    //
    if ( hasSRNAhit() )
    {
      for ( SmallRNAHit hit : getSmallRNAHits() )
      {
        if ( hit.getMirbaseId().indexOf( searchFor ) != -1 )
          return true;

        if ( hit.getAlignmentMRNA().indexOf( searchFor ) != -1 )
          return true;

        if ( hit.getAlignmentSRNA().indexOf( searchFor ) != -1 )
          return true;
      }
    }

    return false;
  }

  /**
   * Comparator which sorts PlotRecord objects so that the lowest category (most significant)
   * with the highest abundance come first.
   */
  public static final Comparator< PlotRecord > CATEGORY_ABUNDANCE_COMPARATOR = new CategoryAbundanceComparator();

  private static class CategoryAbundanceComparator implements Comparator< PlotRecord >
  {
    @Override
    public int compare( PlotRecord o1, PlotRecord o2 )
    {
      if ( o1 == o2 )
        return 0;

      // Check the category
      //
      if ( o1.getMaxCategory().getCategoryValue() != o2.getMaxCategory().getCategoryValue() )
        return o1.getMaxCategory().getCategoryValue() - o2.getMaxCategory().getCategoryValue();

      int degradomeHitAbundanceCheck = Float.compare( o2.getMaxDegradomeHitAbundance(), o1.getMaxDegradomeHitAbundance() );

      if ( degradomeHitAbundanceCheck != 0 )
        return degradomeHitAbundanceCheck;

      return o2.getMaxSmallRNAHitAbundance() - o1.getMaxSmallRNAHitAbundance();
    }
  }

  void sortSmallRNAHits()
  {
    if ( hasSRNAhit() )
    {
      Collections.sort( _srnaHits, SmallRNAHit.DEFAULT_COMPARATOR );

      int index = 1;
      for ( SmallRNAHit sh : _srnaHits )
      {
        sh.setIndex( index++ );
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // Utility class representing a 'degradome hit'
  //
  public static final class DegradomeHit
  {
    private final float _abundance;
    private final int _position;

    public DegradomeHit( float abundance, int position )
    {
      _abundance = abundance;
      _position = position;
    }

    public float getAbundance()
    {
      return _abundance;
    }

    public int getPosition()
    {
      return _position;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // Utility class representing a 'small RNA hit'
  //
  public static final class SmallRNAHit
  {
    private final int _abundance;
    private final String _alignmentSRNA;
    private final String _alignmentBars;
    private final String _alignmentMRNA;
    private final Category _category;
    private final int _position;
    private final float _pValue;
    private final float _score;

    private int _index = 0;
    private String _mirbaseId = "";

    public SmallRNAHit( int position, int abundance, Category category, float score, float pValue, String alignmentSRNA, String alignmentBars, String alignmentMRNA )
    {
      _position = position;
      _pValue = pValue;
      _abundance = abundance;
      _category = category;
      _score = score;
      _alignmentSRNA = alignmentSRNA;
      _alignmentBars = alignmentBars;
      _alignmentMRNA = alignmentMRNA;
    }

    public int getAbundance()        { return _abundance; }
    public String getAlignmentBars() { return _alignmentBars; }
    public String getAlignmentMRNA() { return _alignmentMRNA; }
    public String getAlignmentSRNA() { return _alignmentSRNA; }
    public Category getCategory()    { return _category; }
    public int getPosition()         { return _position; }
    public float getPValue()         { return _pValue; }
    public float getScore()          { return _score; }

    private void setIndex( int i )   { _index = i; }
    public int getIndex()            { return _index; }

    // For storing the ID of a known RNA, for example a miRBase entry like 'mtr-miR2618b'.
    void setMirbaseId( String s ) { _mirbaseId = s; }
    public String getMirbaseId()  { return _mirbaseId == null ? "" : _mirbaseId; }

    private static final Comparator< SmallRNAHit > DEFAULT_COMPARATOR = new CategoryScorePositionComparator();

    private static class CategoryScorePositionComparator implements Comparator< SmallRNAHit >
    {
      @Override
      public int compare( SmallRNAHit o1, SmallRNAHit o2 )
      {
        if ( o1 == o2 )
          return 0;

        // Check the category
        //
        if ( o1.getCategory().getCategoryValue() != o2.getCategory().getCategoryValue() )
          return o1.getCategory().getCategoryValue() - o2.getCategory().getCategoryValue();

        // Check abundance
        //
        if ( o1.getAbundance() != o2.getAbundance() )
          return o2.getAbundance() - o1.getAbundance();

        // Check the score
        //
        int scoreComparison = Float.compare( o1.getScore(), o2.getScore() );

        if ( scoreComparison != 0 )
          return scoreComparison;

        // Just left with the position
        //
        return o1.getPosition() - o2.getPosition();
      }
    }
  }
}
