/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.MapGlyphFactory;
import java.awt.Color;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeI;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.PlotRecord;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.DirectedLabelledGlyphI;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.LabelledVerticallyAlignedRectGlyph;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.StripedSolidRectGlyph;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows.SequenceWindow;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows.SequenceWindowHelper;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * The link between VisSR and the objects which implement SequenceRangeI.<br/>
 * This class implements the methods required by the VisSR GUI without putting
 * visualisation methods in the data objects.
 */
final class VisSRSequenceHelper
{
  private VisSRSequenceHelper() {}

  private static final HashMap< Class< ? extends SequenceRangeI >, VisSRSequenceHelperI > CLASS_HELPER_MAP = CollectionUtils.newHashMap();
  private static final BaseVisSRHelper DEFAULT_HELPER = new BaseVisSRHelper();

  static
  {
    // Associate each SequenceRangeI class with a helper...
    //
    CLASS_HELPER_MAP.put( GFFRecord.class,      new GFFRecordVisSRHelper() );
    CLASS_HELPER_MAP.put( FastaRecord.class,    new FastaRecordVisSRHelper() );
    CLASS_HELPER_MAP.put( PlotRecord.class,     new PARESnipRecordVisSRHelper() );
    CLASS_HELPER_MAP.put( SequenceWindow.class, new SequenceWindowVisSRHelper() );
    CLASS_HELPER_MAP.put( PatmanEntry.class,    new PatmanEntryVisSRHelper() );
  }

  private static VisSRSequenceHelperI getVisSRSequenceHelper( Class< ? extends SequenceRangeI > c )
  {
    VisSRSequenceHelperI result = CLASS_HELPER_MAP.get( c );

    if ( result == null )
      return DEFAULT_HELPER;

    return result;
  }

  /**
   * Get the plain-text representation
   *
   * @param sr
   * @return
   */
  static String formatPlainText( SequenceRangeI sr )
  {
    return getVisSRSequenceHelper( sr.getClass() ).formatPlainText( sr );
  }

  /**
   * Get the HTML representation
   *
   * @param sr
   * @return
   */
  static String formatHTML( SequenceRangeI sr, boolean normalised )
  {
    return getVisSRSequenceHelper( sr.getClass() ).formatHTML( sr, normalised );
  }

  /**
   * Get the colour for the associated SequenceRangeI which is obtained by {@code g.getInfo()}<br/>
   * If a non-null 'special' colour is obtained then it used and it cannot be overridden from the GUI.
   *
   * @param g The {@link GlyphI} object
   */
  static void performGlyphColorSpecialisation( GlyphI g )
  {
    if ( !( g.getInfo() instanceof SequenceRangeI ) )
      return;

    SequenceRangeI sr = (SequenceRangeI) g.getInfo();

    Color c = getVisSRSequenceHelper( sr.getClass() ).getSpecialisedColor( sr );

    if ( c != null )
    {
      g.setBackgroundColor( c );
    }
  }

  /**
   * Tailor the glyph to the SequenceRangeI.
   *
   * @param sr
   */
  static GlyphI createGlyph( SequenceRangeI sr, MapGlyphFactory fac, double tierHeight )
  {
    // The vertical height (which is called 'width' on the glyph) must be set before the glyph is created
    //
    double glyphHeight = getVisSRSequenceHelper( sr.getClass() ).getGlyphHeight( sr, tierHeight );
    fac.setWidth( glyphHeight );

    // Create the glyph
    GlyphI g = fac.makeGlyph( sr.getStartIndex(), sr.getEndIndex() + 1 );

    // Associate the data object with the glyph
    g.setInfo( sr );

    performGlyphColorSpecialisation( g );

    // Set the direction and label, if appropriate
    //
    if ( g instanceof DirectedLabelledGlyphI )
    {
      DirectedLabelledGlyphI dlg = (DirectedLabelledGlyphI) g;

      String label = sr.getAttribute( "ID" );

      if ( label != null )
      {
        dlg.setLabel( label );
      }

      dlg.setGlyphDirection( sr.getSequenceStrand().getCharCode() );
    }

    // Perform other specialisations...
    getVisSRSequenceHelper( sr.getClass() ).performGlyphSpecialisation( sr, g );

    return g;
  }

  /**
   * Is an internet search relevant ?
   * @return boolean
   */
  static boolean isWebSearchable( SequenceRangeI sr )
  {
    return getVisSRSequenceHelper( sr.getClass() ).isWebSearchable();
  }

  /**
   * Get the web-search keys
   * @return A set of strings
   */
  static Set<String> getWebSearchKeys( SequenceRangeI sr )
  {
    return getVisSRSequenceHelper( sr.getClass() ).getWebSearchKeys( sr );
  }

  /**
   * Does the search string appear in the keys ?
   * @param searchFor String for which search will be performed
   * @return Whether it was found
   */
  static boolean isInSearchKeys( SequenceRangeI sr, String searchFor )
  {
    if ( searchFor == null )
      return false;

    return getVisSRSequenceHelper( sr.getClass() ).isInSearchKeys( sr, searchFor );
  }

  static String getPrintableGFFType( String gffType )
  {
    return ( (GFFRecordVisSRHelper) getVisSRSequenceHelper( GFFRecord.class ) ).getPrintableGFFType( gffType );
  }

  static Color getColorForGFFType( String gffType )
  {
    return ( (GFFRecordVisSRHelper) getVisSRSequenceHelper( GFFRecord.class ) ).getColorForGFFType( gffType );
  }
  static void setColourForGFFType( String gffType, Color newColor )
  {
    GFFRecordVisSRHelper.setGFF_Type_Colour(gffType, newColor);
    
  }
  
  public static void resetGFF_Type_Colours()
  {
    GFFRecordVisSRHelper.cloneColourMap();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Private interface for sequence visualisation
   *
   * TODO: Turn this into a parameterised interface, i.e. VisSRSequenceHelperI< T >
   */
  private interface VisSRSequenceHelperI
  {
    public String formatPlainText( SequenceRangeI sr );
    public String formatHTML( SequenceRangeI sr, boolean normalised );
    public Color getSpecialisedColor( SequenceRangeI sr );
    public boolean isWebSearchable();
    public Set<String> getWebSearchKeys( SequenceRangeI sr );
    public boolean isInSearchKeys( SequenceRangeI sr, String searchFor );
    public double getGlyphHeight( SequenceRangeI sr, double tierHeight );
    public void performGlyphSpecialisation( SequenceRangeI sr, GlyphI g );
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * The default helper - if there isn't one defined for the class, this one will be used
   */
  private static class BaseVisSRHelper implements VisSRSequenceHelperI
  {
    private int _sbLength = 200;

    protected final StringBuilder createStringBuilder()
    {
      return new StringBuilder( _sbLength );
    }

    protected final void updateCachedLength( StringBuilder sb )
    {
      if ( sb.length() > _sbLength )
      {
        _sbLength = sb.length() + 10;
      }
    }

    protected final String formatPositionHTML( SequenceRangeI sr )
    {
      return "<b>Position:</b> " +
        FormattingUtils.format( "#,##0", sr.getStartIndex() + 1 ) +
        "-" +
        FormattingUtils.format( "#,##0", sr.getEndIndex() + 1 );
    }

    // Start of VisSRSequenceHelperI implementation
    //
    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      StringBuilder sb = createStringBuilder();

      if ( sr.getParentSequenceRangeI() != null )
      {
        String id = sr.getParentSequenceRangeI().getSequenceId();

        if ( id != null && ! id.isEmpty() )
        {
          sb.append( "Ref. sequence id: " ).append( id ).append( LINE_SEPARATOR );
        }
      }

      String id = sr.getSequenceId();

      if ( id != null && ! id.isEmpty() )
      {
        sb.append( "Sequence id: " ).append( id ).append( LINE_SEPARATOR );
      }

      sb.append( "Position: " ).append( sr.getStartIndex() + 1 ).append( "-").append( sr.getEndIndex() + 1 ).append( LINE_SEPARATOR )
        .append( "Length: " ).append( sr.getSequenceLength() ).append( LINE_SEPARATOR )
        .append( "Strand: " ).append( sr.getSequenceStrand().getCharCode() ).append( LINE_SEPARATOR );

      updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised )
    {
      StringBuilder sb = new StringBuilder( _sbLength );

      sb.append( "<html> ")
        .append( "<b>Id:</b> " ).append( sr.getSequenceId() ).append( "<br/>" )
        .append( formatPositionHTML( sr ) ).append( "<br/>" )
        .append( "<b>Length:</b> " ).append( sr.getSequenceLength() ).append(  "<br/>" )
        .append( "<b>Strand:</b> " ).append( sr.getSequenceStrand().getCharCode() ).append( "<br/>" )
        .append( "</html>");

      return sb.toString();
    }

    @Override
    public Color getSpecialisedColor( SequenceRangeI sr )
    {
      return null;
    }

    @Override
    public boolean isWebSearchable()
    {
      return false;
    }

    @Override
    public Set<String> getWebSearchKeys( SequenceRangeI sr )
    {
      return Collections.emptySet();
    }

    @Override
    public boolean isInSearchKeys( SequenceRangeI sr, String searchFor )
    {
      return false;
    }

    @Override
    public void performGlyphSpecialisation( SequenceRangeI sr, GlyphI g )
    {
    }

    @Override
    public double getGlyphHeight( SequenceRangeI sr, double tierHeight )
    {
      return tierHeight / 100 * sr.getGlyphHeightScalePercent();
    }
    //
    // End of VisSRSequenceHelperI implementation

    final protected String getSequenceAsFasta( SequenceRangeI sr )
    {
      final int LINE_LENGTH = 80;

      String sequence = sr.getSequence();
      String fastaId = "unspecified";

      if ( sequence.length() > 0 )
      {
        fastaId = sr.getSequenceId();
      }
      else if ( sequence.length() == 0 && sr.getParentSequenceRangeI() != null )
      {
        int startIndex = sr.getStartIndex();
        int endIndex = sr.getEndIndex();

        sequence = StringUtils.safeSubstring( sr.getParentSequenceRangeI().getSequence(), startIndex, endIndex + 1, "" );

        String attrID = sr.getAttribute( "ID" );

        if ( attrID == null )
        {
          if ( sequence.length() > 0 )
          {
            fastaId = sr.getParentSequenceRangeI().getSequenceId() + "-excerpt";
          }
        }
        else
        {
          fastaId = attrID;
        }
      }

      // Allow some extra space for new-lines
      StringBuilder sb = new StringBuilder( sequence.length() + 2 * sequence.length() % LINE_LENGTH );

      sb.append( ">" ).append( fastaId ).append( " :: Copied from the VisSR tool in the " + AppUtils.APP_NAME ).append( LINE_SEPARATOR );

      int startIndex = 0, endIndex;

      while ( startIndex < sequence.length() )
      {
        endIndex = Math.min( sequence.length(), startIndex + LINE_LENGTH );
        String subSequenceN = sequence.substring( startIndex, endIndex );
        sb.append( subSequenceN ).append( LINE_SEPARATOR );
        startIndex = endIndex;
      }

      return sb.toString();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * The helper specific to GFF records
   */
  private static final class GFFRecordVisSRHelper extends BaseVisSRHelper
  {
    private static final HashMap< String, Color > GFF_TYPE_COLOR = CollectionUtils.newHashMap();
    private static final HashMap< String, Color > DEFAULT_GFF_TYPE_COLOR = CollectionUtils.newHashMap();
    private static final HashMap< String, String > TYPE_TO_DISPLAY_TYPE_MAP = CollectionUtils.newHashMap();

    private static final Color HAIRPIN_MIRNA_COLOR = Color.blue;
    private static final Color HAIRPIN_MIRNA_STAR_COLOR = Color.red;

    static
    {
      DEFAULT_GFF_TYPE_COLOR.put( "gene",                 new Color( 0, 120, 240 ) );
      DEFAULT_GFF_TYPE_COLOR.put( "miRNA",                Color.magenta );
      DEFAULT_GFF_TYPE_COLOR.put( "pseudogene",           Color.cyan.darker() );
      DEFAULT_GFF_TYPE_COLOR.put( "transposable_element", Color.orange );
      
      resetGFF_Type_Colours();

      TYPE_TO_DISPLAY_TYPE_MAP.put( "CDS",                  "CDSs" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "exon",                 "Exons" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "five_prime_UTR",       "5' UTRs" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "gene",                 "Genes" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "mRNA",                 "mRNAs" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "miRNA",                "miRNAs" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "pseudogene",           "Pseudogenes" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "three_prime_UTR",      "3' UTRs" );
      TYPE_TO_DISPLAY_TYPE_MAP.put( "transposable_element", "Transposable elements" );
    }
    
    public static void setGFF_Type_Colour(String gffType, Color newColor)
    {
//      GFF_TYPE_COLOR.put( "gene",                 new Color( 0, 120, 240 ) );
//      GFF_TYPE_COLOR.put( "miRNA",                Color.magenta );
//      GFF_TYPE_COLOR.put( "pseudogene",           Color.cyan.darker() );
//      GFF_TYPE_COLOR.put( "transposable_element", Color.orange );
      
      GFF_TYPE_COLOR.put( gffType, newColor );
    }

    private static void cloneColourMap()
    {
      for ( Map.Entry<String, Color> e : new TreeMap<>( DEFAULT_GFF_TYPE_COLOR ).entrySet() )
      {
        GFF_TYPE_COLOR.put( e.getKey(), e.getValue() );
      }
    }
    private GFFRecord cast( Object o )
    {
      if ( o instanceof GFFRecord )
        return (GFFRecord) o;

      throw new ClassCastException( "Must be a GFFRecord ! Found " + o.getClass().getName() );
    }

    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      GFFRecord gr = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      // Append details about the GFF record
      //
      sb.append( super.formatPlainText( sr ) )
        .append( LINE_SEPARATOR )
        .append( "GFF entry: ").append( LINE_SEPARATOR )
        .append( gr.toGFFFileEntry() ).append( LINE_SEPARATOR );

      // Append portion of the reference sequence
      //
      sb.append( LINE_SEPARATOR )
        .append( "FASTA record:" ).append( LINE_SEPARATOR )
        .append( super.getSequenceAsFasta( sr ) );

      super.updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised )
    {
      GFFRecord gr = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      sb.append( "<html> ")
        .append( "<b>Seq. id:</b> " ).append( gr.getSeqid() ).append( "<br/>" )
        .append( "<b>Source:</b> " ).append( gr.getSource() ).append( "<br/>" )
        .append( "<b>Type:</b> " ).append( gr.getType() ).append( "<br/>" )
        .append( "<b>Score:</b> ").append( Float.compare( gr.getScore(), GFFRecord.UNKNOWN_SCORE ) == 0 ? "" : Float.toString( gr.getScore() ) ).append( "<br/>" )
        .append( formatPositionHTML( sr ) ).append( "<br/>" )
        .append( "<b>Length:</b> " ).append( FormattingUtils.format( "#,##0", gr.getSequenceLength() ) ).append(  "<br/>" )
        .append( "<b>Strand:</b> " ).append( gr.getSequenceStrand().getCharCode() ).append( "<br/>" )
        .append( "<b>Attributes:</b> " ).append( gr.getAttributesString() ).append( "<br/>" );

      if ( gr.hasChildren() )
      {
        List<GFFRecord> children = gr.getChildren();

        sb.append("<br/>").append( "<b>Features:</b><br/><br/>");

        if ( gr.getSequenceStrand() == SequenceStrand.NEGATIVE )
        {
          for ( int i = children.size() - 1; i >= 0; --i )
          {
            GFFRecord child = children.get( i );

            sb.append( "<b>" ).append( child.getType() ).append( "</b> : " )
              .append( " <i>length:</i> " ).append( FormattingUtils.format( "#,##0", child.getSequenceLength() ) );

            int x = gr.getEndBasePosition();

            sb.append( " <i>coords.:</i> " )
              .append( x - child.getEndBasePosition() + 1 )
              .append( "-" )
              .append( x - child.getStartBasePosition() + 1 )
              .append( "<br/>" );
          }
        }
        else
        {
          for ( GFFRecord child : children )
          {
            sb.append( "<b>" ).append( child.getType() ).append( "</b> : " )
              .append( " <i>length:</i> " ).append( FormattingUtils.format( "#,##0", child.getSequenceLength() ) );

            if ( gr.getSequenceStrand() == SequenceStrand.POSITIVE )
            {
              int x = gr.getStartBasePosition();

              sb.append( " <i>coords.:</i> " )
                .append( child.getStartBasePosition() - x + 1 )
                .append( "-" )
                .append( child.getEndBasePosition() - x + 1 );
            }

            sb.append( "<br/>");
          }
        }
      }

      sb.append( "</html> ");

      super.updateCachedLength( sb );

      return sb.toString();
    }

    /**
     * Get a colour for a GFF type from the type->colour map.
     * Add the GFF type and colour if it's not present.
     * Calculate the colour from the hashcode
     *
     * @param gffType
     * @return
     */
    private Color getColorForGFFType( String gffType )
    {
      Color col = GFF_TYPE_COLOR.get( gffType );

      if ( col == null )
      {
        col = new Color( gffType.hashCode() & 0x00ffffff );
        GFF_TYPE_COLOR.put( gffType, col );
        DEFAULT_GFF_TYPE_COLOR.put( gffType, col );
      }

      return col;
    }

    @Override
    public Color getSpecialisedColor( SequenceRangeI sr )
    {
      Color col = null;

      String hairpinElement = sr.getAttribute( GFFRecord.HAIRPIN_ELEMENT_ATTRIBUTE_KEY );

      if ( hairpinElement != null )
      {
        if ( GFFRecord.HAIRPIN_MIRNA_TAG.equals( hairpinElement ) )
        {
          col = HAIRPIN_MIRNA_COLOR;
        }
        else if ( GFFRecord.HAIRPIN_MIRNA_STAR_TAG.equals( hairpinElement ) )
        {
          col = HAIRPIN_MIRNA_STAR_COLOR;
        }
      }

      return col;
    }

    @Override
    public boolean isWebSearchable()
    {
      return true;
    }

    @Override
    public Set<String> getWebSearchKeys( SequenceRangeI sr )
    {
      HashSet<String> searchKeys = CollectionUtils.newHashSet();

      String[] keys = { "ID", "Name", "Parent" };

      for ( String s : keys )
      {
        String value = sr.getAttribute( s );

        if ( value != null )
        {
          searchKeys.add( value );
        }
      }

      return searchKeys;
    }

    @Override
    public boolean isInSearchKeys( SequenceRangeI sr, String searchFor )
    {
      if ( searchFor == null || searchFor.isEmpty() )
        return false;

      if ( sr.getAttributes().isEmpty() )
        return false;

      Map< String, String > attribs = sr.getAttributes();

      if ( attribs.containsValue( searchFor ) )
        return true;

      // Use a mimimum length to avoid returning loads
      if ( searchFor.length() > 3 )
      {
        searchFor = searchFor.toLowerCase();

        for ( String s : attribs.values() )
        {
          if ( s.toLowerCase().contains( searchFor ) )
            return true;
        }
      }

      return false;
    }

    private String getPrintableGFFType( String gffType )
    {
      if ( gffType == null || gffType.isEmpty() )
        return "";

      String result = TYPE_TO_DISPLAY_TYPE_MAP.get( gffType );

      if ( result == null )
      {
        String temp = gffType.replace( "_", " " );

        String firstChar = StringUtils.safeSubstring( temp, 0, 1, "" );
        String remainder = StringUtils.safeSubstring( temp, 1, temp.length(), "" );

        result = firstChar + remainder + "s";

        TYPE_TO_DISPLAY_TYPE_MAP.put( gffType, result );
      }

      return result;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * The helper specific to GFF records
   */
  private static final class FastaRecordVisSRHelper extends BaseVisSRHelper
  {
    private FastaRecord cast( Object o )
    {
      if ( o instanceof FastaRecord )
        return (FastaRecord) o;

      throw new ClassCastException( "Must be a FastaRecord ! Found " + o.getClass().getName() );
    }

    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      FastaRecord fr = cast( sr );

      return StringUtils.nullSafeConcatenation( fr.getSequenceId(), " [",
        "seq. length = ", fr.getSequenceLength(), ", ",
        "FASTA header = '", fr.getFastaHeader(), "']" );
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised )
    {
      FastaRecord fr = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      sb.append( "<html> ")
        .append( "<b>Id:</b> " ).append( fr.getSequenceId() ).append( "<br/>" )
        .append( "<b>FASTA header:</b> " ).append( fr.getFastaHeader() ).append( "<br/>" );


      if ( normalised )
      {
        sb.append( "<b>Normalised Abundance:</b> " ).append( fr.getRealAbundance() ).append( "<br/>" );
      }
      else
      {
        sb.append( "<b>Abundance:</b> " ).append( fr.getRealAbundance() ).append( "<br/>" );
      }

      sb.append( "<b>Length:</b> " ).append( FormattingUtils.format( "#,##0", fr.getSequenceLength() ) ).append(  "<br/>" )
        .append( "</html> ");

      super.updateCachedLength( sb );

      return sb.toString();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * The helper specific to GFF records
   */
  private static final class PARESnipRecordVisSRHelper extends BaseVisSRHelper
  {
    private PlotRecord cast( Object o )
    {
      if ( o instanceof PlotRecord )
        return (PlotRecord) o;

      throw new ClassCastException( "Must be a PlotRecord ! Found " + o.getClass().getName() );
    }

    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      PlotRecord pr = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      sb.append( "Category: " ).append( pr.getMaxCategory().getCategoryValue() ).append( LINE_SEPARATOR )
        .append( "Gene Id: " ).append( pr.getGeneId() ).append( LINE_SEPARATOR )
        .append( "Gene length: " ).append( FormattingUtils.format( "#,##0", pr.getGeneLength() ) ).append( LINE_SEPARATOR )
        .append( "Max. degradome abundance: " ).append( pr.getMaxDegradomeHitAbundance() ).append( LINE_SEPARATOR )
        .append( "Median abundance: " ).append( pr.getMedianDegradomeHitAbundance() ).append( LINE_SEPARATOR )
        .append( "Max. sRNA abundance: " ).append( FormattingUtils.format( "#,##0", pr.getMaxSmallRNAHitAbundance() ) ).append( LINE_SEPARATOR )
        .append( "Number sRNA hits: " ).append( pr.hasSRNAhit() ? pr.getSmallRNAHits().size() : 0 ).append( LINE_SEPARATOR );

      if ( pr.hasSRNAhit() )
      {
        for ( PlotRecord.SmallRNAHit srh : pr.getSmallRNAHits() )
        {
          sb.append( LINE_SEPARATOR )
            .append( "Position: " ).append( srh.getPosition() )
            .append( ", Abundance: " ).append( srh.getAbundance() )
            .append( ", Score: " ).append( srh.getScore() )
            .append( ", p-value: " ).append( srh.getPValue() );

          if ( ! srh.getMirbaseId().isEmpty() )
          {
            sb.append( ", miRBase: " ).append( srh.getMirbaseId() );
          }

          sb.append( LINE_SEPARATOR )
            .append( srh.getAlignmentSRNA() ).append( LINE_SEPARATOR )
            .append( srh.getAlignmentBars() ).append( LINE_SEPARATOR )
            .append( srh.getAlignmentMRNA() ).append( LINE_SEPARATOR );
        }

        sb.append( LINE_SEPARATOR );
      }

      super.updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised )
    {
      PlotRecord pr = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      sb.append( "<html> " )
        .append( "<b>Category:</b> " ).append( pr.getMaxCategory().getCategoryValue() ).append( "<br/>" )
        .append( "<b>Gene Id:</b> " ).append( pr.getGeneId() ).append( "<br/>" )
        .append( "<b>Gene length:</b> " ).append( FormattingUtils.format( "#,##0", pr.getGeneLength() ) ).append( "<br/>" )
        .append( "<b>Max. sRNA abundance:</b> " ).append( FormattingUtils.format( "#,##0", pr.getMaxSmallRNAHitAbundance() ) ).append( "<br/>" )
        .append( "<b>Max. / median degradome abundance:</b> " ).append( FormattingUtils.format( "#,##0.00", pr.getMaxDegradomeHitAbundance() ) ).append( " / " )
        .append( FormattingUtils.format( "#,##0.00", pr.getMedianDegradomeHitAbundance() ) ).append( "<br/>" );

      if ( pr.hasSRNAhit() )
      {
        // Limit the tooltip to 3 and display a message if there are more
        final int LIMIT = 3;

        List< PlotRecord.SmallRNAHit > hits = pr.getSmallRNAHits();

        if ( hits.size() > LIMIT )
        {
          sb.append( "<br/>" ).append( "Showing " ).append( LIMIT ).append( " of " ).append( hits.size() ).append( "...<br/>" );
        }

        for ( int i = 0, n = Math.min( LIMIT, hits.size() ); i < n; ++i )
        {
          PlotRecord.SmallRNAHit srh = hits.get( i );

          sb.append( "<br/>" )
            .append( "<b>Category:</b> " ).append( srh.getCategory().getCategoryValue() )
            .append( " <b>Abundance:</b>  " ).append( FormattingUtils.format( "#,##0", srh.getAbundance() ) )
            .append( " <b>Score:</b>  " ).append( FormattingUtils.format( "#,##0.0", srh.getScore() ) )
            .append( " <b>Position:</b> " ).append( FormattingUtils.format( "#,##0", srh.getPosition() ) );

          if ( srh.getPValue() == PlotRecord.UNKNOWN_PVALUE )
          {
            sb.append( " <b>p-value:</b> N/A (" ).append( ((int)PlotRecord.UNKNOWN_PVALUE) ).append( ")" );
          }
          else
          {
            sb.append( " <b>p-value:</b> " ).append( FormattingUtils.format( "0.00", srh.getPValue() ) );
          }

          if ( ! srh.getMirbaseId().isEmpty() )
          {
            sb.append( " <b>miRBase:</b> " ).append( srh.getMirbaseId() );
          }

          sb.append( "<pre>" ).append( srh.getAlignmentSRNA() ).append( '\n' )
            .append( srh.getAlignmentBars() ).append( '\n' )
            .append( srh.getAlignmentMRNA() ).append( "</pre>" );
        }
      }

      sb.append( "</html> ");

      super.updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public boolean isInSearchKeys( SequenceRangeI sr, String searchFor )
    {
      PlotRecord pr = cast( sr );

      return pr.containsSearchKey( searchFor );
    }

    @Override
    public double getGlyphHeight( SequenceRangeI sr, double tierHeight )
    {
      // Want the glyph to be the height of the tier for easy selection and tooltip display
      return tierHeight;
    }

    @Override
    public void performGlyphSpecialisation( SequenceRangeI sr, GlyphI g )
    {
      PlotRecord pr = cast( sr );

      if ( ! ( g instanceof LabelledVerticallyAlignedRectGlyph ) )
        return;

      LabelledVerticallyAlignedRectGlyph varg = (LabelledVerticallyAlignedRectGlyph) g;

      // Make the label text black
      varg.setForegroundColor( Color.black );

      // Align the coloured region of the glyph to the bottom
      varg.setVerticalGlyphAlignment( LabelledVerticallyAlignedRectGlyph.VerticalAlignment.BOTTOM );

      // Tell the glyph how big the coloured region should be
      varg.setHeightScale( 0.01 * pr.getGlyphHeightScalePercent() );

      // Give it a label to display
      varg.setLabel( pr.getGeneId() );
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final class SequenceWindowVisSRHelper extends BaseVisSRHelper
  {
    private SequenceWindow cast( SequenceRangeI sr )
    {
      if ( sr instanceof SequenceWindow )
        return (SequenceWindow) sr;

      throw new ClassCastException( "Must be a SequenceWindow ! Found " + sr.getClass().getName() );
    }

    private StripedSolidRectGlyph cast( GlyphI g )
    {
      if ( g instanceof StripedSolidRectGlyph )
        return (StripedSolidRectGlyph) g;

      throw new ClassCastException( "Must be a StripedSolidRectGlyph ! Found " + g.getClass().getName() );
    }

    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      SequenceWindow sw = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      sb.append( super.formatPlainText( sr) );

      // Append details of the sequence window
      //
      sb.append( "Length -> abundance: " ).append( sw.getLengthAbundanceMap() ).append( LINE_SEPARATOR );

      // Append details of each Patman entry
      //
      sb.append( LINE_SEPARATOR )
        .append( "Patman entries: ").append( LINE_SEPARATOR );

      for ( PatmanEntry pe : sw.getPatmanEntries() )
      {
        sb.append( pe.getSequenceId() ).append( '\t' )
          .append( pe.getSequence() ).append( '(' ).append( pe.getAbundance() ).append( ')' ).append( '\t' )
          .append( pe.getStart() ).append( '\t' )
          .append( pe.getEnd() ).append( '\t' )
          .append( pe.getSequenceStrand().getCharCode() ).append( '\t' )
          .append( pe.getMismatches() ).append( LINE_SEPARATOR );
      }

      // Append portion of the reference sequence
      //
      sb.append( LINE_SEPARATOR )
        .append( "FASTA record:" ).append( LINE_SEPARATOR )
        .append( super.getSequenceAsFasta( sr ) );

      super.updateCachedLength( sb );

      return sb.toString();
    }

    private String getColorHexString( Color c )
    {
      // remove left-most word of RGB
      int rgb = c.getRGB() & 0x00ffffff;

      return Integer.toHexString( rgb );
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised)//aggregate tooltip generator
    {
      SequenceWindow sw = cast( sr );

      StringBuilder sb = super.createStringBuilder();

      NumberFormat intFormat = FormattingUtils.getNumberFormat( "#,##0", true );

      sb.append( "<html> ")
        .append( "<b>Id:</b> " ).append( sw.getSequenceId() ).append( "<br/>" )
        .append( formatPositionHTML( sr ) ).append( "<br/>" )
        .append( "<b>Length:</b> " ).append( intFormat.format( sw.getSequenceLength() ) ).append(  "<br/>" );

      String abundanceString;
      if(normalised)
        abundanceString = "<b>Total abundance (normalised):</b> ";
      else
        abundanceString = "<b>Total abundance:</b> ";
      
      double totalAbundance = sw.getTotalAbundance();
      sb.append( abundanceString ).append( NumberUtils.roundToSignificantFigures( totalAbundance, 2 ) ).append(  "<br/>" );

      if ( sw.getLengthAbundanceMap() != null )
      {
        boolean includePc = ( totalAbundance > 0 );

        sb.append( "<br/>" );
        sb.append( "<table>" );
        sb.append( "<tr><th>Seq. length</th><th>Abundance</th>" ).append( includePc ? "<th>Percent</th>" :"" ).append( "</tr>" );

        SequenceWindowHelper swh = sw.getSequenceWindowHelper();

        for ( Integer length : swh.getLengths() )
        {
          Color c = VisSRGlobals.getColourForLength( length );
          String colorName = getColorHexString( c );
          sb.append( "<tr style='text-align:center;color:" ).append( colorName ).append( "'>");

          // Put the sequence length in the first column
          sb.append( "<td>" ).append( length ).append( "</td>" );

          Double abundance = sw.getLengthAbundanceMap().get( length );
          if ( abundance == null )
          {
            abundance = 0.0;
          }

          // Put the total abundance in the second column
          sb.append( "<td>" ).append( NumberUtils.roundToSignificantFigures( abundance, 2 ) ).append( "</td>" );

          // Put the (abundance / total abundance) percentage in the third column (if necessary)
          if ( includePc )
          {
            sb.append( "<td>" );

            double pc = 100.0 * ( abundance.doubleValue() / totalAbundance );
            String formattedPc = NumberUtils.format2DP( pc, RoundingMode.HALF_EVEN );//oneDPFormat.format( pc );

            if ( abundance > 0 && pc < 0.0625 && "0.0".equals( formattedPc ) )
            {
              sb.append( "~" );
            }
            sb.append( formattedPc );

            sb.append( "</td>" );
          }

          sb.append( "</tr>" );
        }

        sb.append( "</table>" );
      }

      sb.append( "</html> ");

      return sb.toString();
    }

    @Override
    public double getGlyphHeight( SequenceRangeI sr, double tierHeight )
    {
      // Want the glyph to be the height of the tier for easy selection and tooltip display
      return tierHeight;
    }

    @Override
    public void performGlyphSpecialisation( SequenceRangeI sr, GlyphI g )
    {
      // Store the abundance data in the glyph

      SequenceWindow sw = cast( sr );
      StripedSolidRectGlyph ssrg = cast( g );

      Map< Integer, Double > laMap = sw.getLengthAbundanceMap();

      int i = 0;
      for ( Integer length : sw.getSequenceWindowHelper().getLengths() )
      {
        double abundance = 0.0;

        if ( laMap != null )
        {
          Double iObj = laMap.get( length );

          if ( iObj != null )
          {
            abundance = iObj.doubleValue();
          }
        }

        Color c = VisSRGlobals.getColourForLength( length.intValue() );
        ssrg.setDataByIndex( i, c, abundance );

        ++i;
      }

      ssrg.setHeightScaling( 0.01 * sw.getGlyphHeightScalePercent() );
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Patman helper
   */
  private static class PatmanEntryVisSRHelper extends BaseVisSRHelper
  {
    private static PatmanEntry cast( SequenceRangeI sr )
    {
      if ( sr instanceof PatmanEntry )
        return (PatmanEntry) sr;

      throw new ClassCastException( "Must be a PatmanEntry! Found " + sr.getClass().getName() );
    }

    // Start of VisSRSequenceHelperI implementation
    //
    @Override
    public String formatPlainText( SequenceRangeI sr )
    {
      StringBuilder sb = super.createStringBuilder();

      PatmanEntry pe = cast( sr );

      sb.append( super.formatPlainText( sr) );

      sb.append( "Sequence: " ).append( pe.getSequence() ).append(  LINE_SEPARATOR )
        .append( "Abundance: " ).append( pe.getAbundance() ).append(  LINE_SEPARATOR )
        .append( "Num. mismatches: " ).append( pe.getMismatches() ).append(  LINE_SEPARATOR );

      super.updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public String formatHTML( SequenceRangeI sr, boolean normalised )//arrow mode tooltip code
    {
      StringBuilder sb = super.createStringBuilder();

      String abundanceString = "";
      if(normalised)
        abundanceString = "<b>Normalised Abundance:</b> ";
      else
      {
        abundanceString = "<b>Abundance:</b> ";
      }
      PatmanEntry pe = cast( sr );

      sb.append( "<html> ")
        .append( "<b>Id:</b> " ).append( sr.getSequenceId() ).append( "<br/>" )
        .append( formatPositionHTML( sr ) ).append( "<br/>" )
        .append( "<b>Length:</b> " ).append( sr.getSequenceLength() ).append(  "<br/>" )
        .append( "<b>Strand:</b> " ).append( sr.getSequenceStrand().getCharCode() ).append( "<br/>" )
        .append( "<b>Sequence:</b> " ).append( sr.getSequence() ).append( "<br/>" )
        .append( abundanceString ).append( NumberUtils.roundToSignificantFigures( pe.getAbundance(), 2 ) ).append(  "<br/>" )
        .append( "<b>Num. mismatches:</b> " ).append( pe.getMismatches() ).append(  "<br/>" )
        .append( "</html> ");

      super.updateCachedLength( sb );

      return sb.toString();
    }

    @Override
    public Color getSpecialisedColor( SequenceRangeI sr )
    {
      if ( ! ( sr instanceof PatmanEntry ) )
        return null;

      PatmanEntry pe = (PatmanEntry) sr;

      Color c = VisSRGlobals.getColourForLength( pe.getLength() );

      return c;
    }

    @Override
    public boolean isWebSearchable()
    {
      return false;
    }

    @Override
    public Set<String> getWebSearchKeys( SequenceRangeI sr )
    {
      return Collections.emptySet();
    }

    @Override
    public boolean isInSearchKeys( SequenceRangeI sr, String searchFor )
    {
      return false;
    }

    @Override
    public void performGlyphSpecialisation( SequenceRangeI sr, GlyphI g )
    {
    }
    //
    // End of VisSRSequenceHelperI implementation
  }

  //////////////////////////////////////////////////////////////////////////////
}
