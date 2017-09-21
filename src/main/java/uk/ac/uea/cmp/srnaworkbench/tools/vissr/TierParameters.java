/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import com.affymetrix.genoviz.bioviews.GlyphI;

import java.awt.Color;
import java.util.*;
import java.util.logging.Level;

/**
 * Store the data, state and display details for a tier in the {@link VisSRDataTiersPanel}.
 * This object can then be used to reinitialise the display when the governing
 * sequence (e.g. chromosome) is changed by the user.<br>
 *
 * @author prb07qmu
 */
public final class TierParameters
{
  public static final Color V_DARK_GRAY = AppUtils.INSTANCE.getWorkbenchGray();
  
  public static Color ODDS_COLOUR = new Color(51,51,51);//used for odd number tier
  public static Color EVENS_COLOUR = Color.WHITE;//used for even number tier

  private static final Color    DEFAULT_GLYPH_BG_COLOUR = Color.blue.darker();
  private static final Class    DEFAULT_GLYPH_CLASS     = DirectedLabelledRectGlyph.class;
  private static final Color    DEFAULT_LABEL_BG_COLOUR = AppUtils.INSTANCE.getVeryLightGray();
  private static final TierType DEFAULT_TIER_TYPE       = TierType.USER_SPECIFIED;

  private Color    _glyphBackgroundColour;
  private Class    _glyphClass;
  private String   _tierLabel;
  private Color    _tierLabelBackgroundColour;
  private TierType _tierType;

  

  public enum TierType
  {
    COORDINATES,
    SEQUENCE,
    USER_SPECIFIED { @Override boolean isUserSpecified() { return true; } },
    USER_SPECIFIED_PARESNIP { @Override boolean isUserSpecified() { return true; } };

    boolean isUserSpecified() { return false; }
  };

  /** Index of the tier in VisSRDataTiersPanel */
  private int _tierIndex = -1;

  /** Whether the tier has been displayed in VisSRDataTiersPanel */
  private boolean _hasBeenDisplayed = false;

  /** Associate a list of SequenceRangeI objects with a string identifier */
  private final Map< String, List< ? extends SequenceRangeI > > _sequenceIdToListMap = CollectionUtils.newHashMap();

  /**
   * Use builder for construction
   */
  private TierParameters( Builder builder )
  {
    setGlyphBackgroundColour( builder.glyphBackgroundColour );

    _glyphClass = builder.glyphClass == null ? DEFAULT_GLYPH_CLASS : builder.glyphClass;

    setTierLabel( builder.tierLabel );
    setTierLabelBackgroundColour( builder.tierLabelBackgroundColour );

    _tierType = builder.tierType;
  }

  void clear()
  {
    _sequenceIdToListMap.clear();
  }

  public String getlabel()
  {
    return _tierLabel;
  }
  public static void resetTierColours()
  {
    ODDS_COLOUR = new Color(51,51,51);//used for odd number tier
    EVENS_COLOUR = Color.WHITE;//used for even number tier
  }
  public static void setODDS_COLOUR( Color ODDS_COLOUR )
  {
    TierParameters.ODDS_COLOUR = ODDS_COLOUR;
  }

  public static void setEVENS_COLOUR( Color EVENS_COLOUR )
  {
    TierParameters.EVENS_COLOUR = EVENS_COLOUR;
  }

  public static Color getODDS_COLOUR()
  {
    return ODDS_COLOUR;
  }

  public static Color getEVENS_COLOUR()
  {
    return EVENS_COLOUR;
  }


  /**
   * Builder class for the TierParameters - allows optional parameters to be set in a similar way to a setter method.
   * If they are not set then they are assigned default values.
   */
  public static final class Builder
  {
    // Required
    private final String tierLabel;

    // Optional parameters
    private Color  glyphBackgroundColour     = DEFAULT_GLYPH_BG_COLOUR;
    private Class  glyphClass                = DEFAULT_GLYPH_CLASS;
    private Color  tierLabelBackgroundColour = DEFAULT_LABEL_BG_COLOUR;
    private TierType tierType                = DEFAULT_TIER_TYPE;

    public Builder( String tierLabel )
    {
      this.tierLabel = ( tierLabel == null ? "" : tierLabel );
    }

    public Builder glyphBackgroundColour( Color col )
    {
      glyphBackgroundColour = col;
      return this;
    }

    public Builder glyphClass( Class<? extends GlyphI> c )
    {
      glyphClass = c;
      return this;
    }

    public Builder tierLabelBackgroundColour( Color col )
    {
      tierLabelBackgroundColour = col;
      return this;
    }

    public Builder tierType( TierType tt )
    {
      tierType = tt;
      return this;
    }

    /**
     * Constructs the TierParameters object
     * @return
     */
    public TierParameters build()
    {
      return new TierParameters( this );
    }
  }

  // Getters and setters

  public final Color getGlyphBackgroundColour()
  {
    return _glyphBackgroundColour;
  }

  public final void setGlyphBackgroundColour( Color glyphBackgroundColour )
  {
    _glyphBackgroundColour = glyphBackgroundColour == null ? DEFAULT_GLYPH_BG_COLOUR : glyphBackgroundColour;
  }

  public final Class getGlyphClass()
  {
    return _glyphClass;
  }

  public final String getTierLabel()
  {
    return _tierLabel;
  }

  public final void setTierLabel( String tierLabel )
  {
    _tierLabel = ( tierLabel == null ? "" : tierLabel );
  }

  public final Color getTierLabelBackgroundColour()
  {
    return _tierLabelBackgroundColour;
  }

  public final void setTierLabelBackgroundColour( Color tierLabelBackgroundColour )
  {
    _tierLabelBackgroundColour = tierLabelBackgroundColour == null ? DEFAULT_LABEL_BG_COLOUR : tierLabelBackgroundColour;
  }

  public final TierType getTierType()
  {
    return _tierType;
  }

  /**
   * Add a list of {@link SequenceRangeI} subclasses for the given id
   *
   * @param id The String identifier, e.g. the chromosome id for GFF annotations.
   * @param l The list
   */
  public final void addListForId( String id, List< ? extends SequenceRangeI > l )
  {
//    List<SequenceRangeI> listForId = getListForId( id );
//    listForId.addAll( l );
    _sequenceIdToListMap.put(id, l);
 
  }

  /**
   * Add a list of {@link SequenceRangeI} subclasses - the ids are obtained from the given collection.
   * @param l
   */
  public final void addList( List< ? extends SequenceRangeI > l )
  {
    // Get all the identifiers
    //
    Set<String> ids = new HashSet<String>();

    for ( SequenceRangeI sr : l )
    {
      ids.add( sr.getSequenceId() );
    }

    // Create the 'id --> list of sequences' map
    //
    Map< String, List< SequenceRangeI > > m = CollectionUtils.newHashMap();

    for ( String id : ids )
    {
      m.put( id, new ArrayList< SequenceRangeI >() );
    }

    // Populate the lists in the map
    //
    for ( SequenceRangeI sr : l )
    {
      m.get( sr.getSequenceId() ).add( sr );
    }

    // Store the lists
    //
    for ( Map.Entry< String, List< SequenceRangeI > > ent : m.entrySet() )
    {
      String id = ent.getKey();
      List< SequenceRangeI > items = ent.getValue();

      addListForId( id, items );

      WorkbenchLogger.LOGGER.log( Level.FINE, "TierParameters: Added list with {0} items for ''{1}''", new Object[]{ items.size(), id });
    }
  }

  /**
   * Get a list of sequences for a given sequence identifier
   *
   * @param sequenceId
   * @return A list, guaranteed not to be null
   */
  public final List< SequenceRangeI > getListForId( String sequenceId )
  {
    // Suppressing the warning because the list contains SequenceRangeI objects or an object which extends SequenceRangeI
    @SuppressWarnings("unchecked")
    List< SequenceRangeI > result = ( List< SequenceRangeI > ) _sequenceIdToListMap.get( sequenceId );

    if ( result == null )
    {
      return Collections.emptyList();
    }

    return result;
  }

  // Only for the tiers panel (should probably be stored there really)
  //
  final int getTierIndex()                    { return _tierIndex; }
  final void setTierIndex( int tierIndex )    { _tierIndex = tierIndex; }
  final boolean hasBeenDisplayed()            { return _hasBeenDisplayed; }
  final void setHasBeenDisplayed( boolean b ) { _hasBeenDisplayed = b; }
  //
  // End of methods for the tiers panel
}
