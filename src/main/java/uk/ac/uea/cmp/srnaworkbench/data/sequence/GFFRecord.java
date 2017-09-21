/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.util.*;

// TODO: Remove attributes from this class. Move attributes to their own class and reference from superclass.

/**
 * Represent an entry in a GFF file.<br/>
 * <br/>
 * See http://www.sequenceontology.org/gff3.shtml for a full description of the GFF format
 *
 * @author prb07qmu
 */
public final class GFFRecord extends SequenceRange
{
  public static final String HAIRPIN_ELEMENT_ATTRIBUTE_KEY = "hairpin_element";

  public static final String HAIRPIN_MIRNA_TAG      = "miRNA";
  public static final String HAIRPIN_MIRNA_STAR_TAG = HAIRPIN_MIRNA_TAG + "*";

  public static final float UNKNOWN_SCORE = Float.MIN_VALUE;

  /* Attribute keys are v. common (e.g. ID, Name, etc.) so use a StringPool to avoid recreating strings */
  private static final StringPool ATTRIBUTE_KEYS = new StringPool( 4 );

  private final String _seqId;
  private final String _source;
  private final String _type;
  private final float _score;
  private final GFFPhase _phase;

  private final boolean _isUTR;

  private List<GFFRecord> _children = null;

  public boolean isUTR()
  {
    return _isUTR;
  }

  /**
   * The phase of a GFF entry is only relevant when the GFF type is 'CDS'.<br/>
   * For non-CDS types the value is {@link GFFPhase#UNDEFINED}.<br/>
   * For CDS types the value is in { {@link GFFPhase#ZERO}, {@link GFFPhase#ONE}, {@link GFFPhase#TWO} } which have the obvious numerical value.
   */
  public enum GFFPhase
  {
    UNDEFINED( Integer.MAX_VALUE, '.' ),
    ZERO(0, '0'),
    ONE(1,'1'),
    TWO(2,'2');

    private final int _phaseValue;
    private final char _phaseChar;

    private GFFPhase( int phaseValue, char phaseChar )
    {
      _phaseValue = phaseValue;
      _phaseChar  = phaseChar;
    }

    public int getPhaseValue() { return _phaseValue; }
    public char getPhaseChar() { return _phaseChar;  }

    public static GFFPhase valueOf( byte b )
    {
      GFFPhase result = UNDEFINED;

      switch (b)
      {
        case 0: result = ZERO; break;
        case 1: result = ONE;  break;
        case 2: result = TWO;  break;
      }

      return result;
    }
  }

  public GFFRecord( String seqId, String source, String type, int startIndex, int endIndex,
    float score, char strand, byte phase )
  {
    super( startIndex, endIndex, SequenceStrand.getSequenceStrand( strand ), seqId );

    _seqId = seqId;
    _source = source;
    _type = type == null ? "" : type;
    _score = score;
    _phase = GFFPhase.valueOf( phase );

    String typeLC = _type.toLowerCase();
    _isUTR = "three_prime_utr".equals( typeLC ) || "five_prime_utr".equals( typeLC ) || typeLC.contains( "utr" );
  }

  // In the GFF spec. the first column is called the 'seqid'
  public String getSeqid()  { return _seqId; }

  public GFFPhase getPhase() { return _phase; }
  public float getScore()    { return _score; }
  public String getSource()  { return _source; }
  public String getType()    { return _type; }

  /**
   * Produce a formatted string conforming to the GFF file specification
   * @return
   */
  public String toGFFFileEntry()
  {
    StringBuilder sb = new StringBuilder( 200 );

    sb.append( getSeqid() ).append( '\t' )
      .append( getSource() ).append( '\t' )
      .append( getType() ).append( '\t' )
      .append( getStartBasePosition() ).append( '\t' )
      .append( getEndBasePosition() ).append( '\t' );

    if ( Float.compare( getScore(), UNKNOWN_SCORE ) == 0 )
    {
      sb.append( '.' ).append( '\t' );
    }
    else
    {
      sb.append( getScore() ).append( '\t' );
    }

    sb.append( getSequenceStrand().getCharCode() ).append( '\t' )
      .append( getPhase().getPhaseChar() ).append( '\t' )
      .append( getAttributesString() );

    return sb.toString();
  }

  @Override
  public String toString()
  {
    return StringUtils.nullSafeConcatenation( "GFFRecord {source=", _source, ", type=", _type,
      ", attributes='", getAttributesString(), "', ", super.toString(), "}" );
  }

  public String getAttributesString()
  {
    if ( super.getAttributes().isEmpty() )
      return "";

    StringBuilder sb = new StringBuilder( 100 );

    for ( Map.Entry<String,String> ent : super.getAttributes().entrySet() )
    {
      String key = ent.getKey();
      String value = ent.getValue();

      sb.append( key ).append( '=' ).append( value ).append( ';' );
    }

    return sb.toString();
  }

  public static Map<String,String> parseAttributesString( String attributes )
  {
    return parseAttributesString( attributes, null );
  }

  public static Map<String,String> parseAttributesString( String attributes, StringPool pool )
  {
    if ( attributes == null || attributes.length() == 0 )
      return null;

    HashMap< String, String > attributesMap = new HashMap< String, String >();

    StringTokenizer st = new StringTokenizer( attributes, "=;", true );
    String key, delim = "", value = "";
    boolean checkPool = ( pool != null );

    while ( st.hasMoreElements() )
    {
      key = st.nextToken();
      delim = ( st.hasMoreElements() ? st.nextToken() : null );
      value = ( st.hasMoreElements() ? st.nextToken() : null );

      if ( st.hasMoreElements() )
      {
        // Get rid of the last delimiter
        st.nextToken();
      }

      if ( "=".equals( delim ) && key != null && value != null )
      {
        key = ATTRIBUTE_KEYS.getString( key );

        if ( checkPool )
        {
          value = pool.getString( value );
        }

        attributesMap.put( key, value );
      }
    }

    return attributesMap;
  }

  public void addChild( GFFRecord g )
  {
    if ( _children == null )
    {
      _children = CollectionUtils.newArrayList();
    }

    _children.add( g );
  }

  public List<GFFRecord> getChildren()
  {
    return _children;
  }

  public boolean hasChildren()
  {
    if ( _children == null || _children.isEmpty() )
      return false;

    return true;
  }

  void sortChildrenByStartIndex()
  {
    if ( hasChildren() )
    {
      Collections.sort( _children, INDEX_COMPARATOR );
    }
  }

  private static final IndexComparator INDEX_COMPARATOR = new IndexComparator();

  private static class IndexComparator implements Comparator<GFFRecord>
  {
    @Override
    public int compare( GFFRecord o1, GFFRecord o2 )
    {
      int diff = o1.getStartIndex() - o2.getStartIndex();

      if ( diff != 0 )
        return diff;

      diff = o1.getSequenceLength() - o2.getSequenceLength();

      return diff;
    }
  }
}
