/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.tools.*;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows.SequenceWindow;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;


import com.affymetrix.genoviz.awt.AdjustableJSlider;
import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.event.*;
import com.affymetrix.genoviz.glyph.*;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.genoviz.widget.*;
import com.affymetrix.genoviz.widget.tieredmap.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;

/**
 * The data tiers panel for the VisSR main internal frame.
 * The panel contains 'TieredNeoMap's which display the sequence, annotations and
 * custom data together with scroll bars and a zoomer to update the display.<br/>
 * <br/>
 * Methods are private or package-private since the panel is specific to the
 * parent frame.<br/>
 * <br/>
 * Note: For control over how the text is centred in a label it will be necessary
 * to subclass MapTierGlyph and override 'draw'.
 *
 * @author prb07qmu
 */
public final class VisSRDataTiersPanel extends javax.swing.JPanel
{
  private static final double TIER_HEIGHT = 60;

  private final SequenceVizMainFrame _vissrMainFrame;
  private final TierParameters _sequenceTierParameters = new TierParameters.Builder( "Sequence" ).tierType( TierParameters.TierType.SEQUENCE ).build();
  private final TierParameters _coordsTierParameters = new TierParameters.Builder( "Coordinates" ).tierType( TierParameters.TierType.COORDINATES ).build();

  /**
   * List of all TierParameters objects (including the sequence and coordinates)
   */
  private final List< TierParameters > _tierParameters = CollectionUtils.newArrayList();

  // Genoviz controls...
  //
  private TieredLabelMap _labelMap = new TieredLabelMap( false, false );
  private TieredNeoMap _detailMap = new TieredNeoMap( false, false );
  private AdjustableJSlider _xzoomer = new AdjustableJSlider( JSlider.HORIZONTAL );
  private VisibleRange _zoomPoint = new VisibleRange();
  private Shadow _hairline = null;
  private GlyphI _glyphUnderPointer = null;
  //
  // end of Genoviz controls

  /**
   * The index of the right-clicked label glyph (for the label popup)
   */
  private int _labelGlyphPopupIndex = -1;

  /**
   * Whether or not a full repack needs to be performed
   */
  private boolean _doFullRepack = false;
  private boolean normalised = false;

  /**
   * Constructor
   */
  public VisSRDataTiersPanel( SequenceVizMainFrame vissrMainFrame, boolean normalised )
  {
    _vissrMainFrame = vissrMainFrame;
    this.normalised = normalised;

    
    initComponents();
    initOtherComponents();
  }

  // api - package-private /////////////////////////////////////////////////////

  /**
   * Add a tier parameters to the data map
   *
   * @param tp The parameters for the tier
   */
  final void addTierParameters( TierParameters tp )
  {
    if ( tp == null )
      return;

    // Add to the parameters collection
    _tierParameters.add( tp );

    // Add the tier to the display
    addTierIfNecessary( tp );
  }

  final void modifyTierParameterColour(String tierLabel, Color newColor)
  {
    for(TierParameters tp : _tierParameters)
    {
      if(tp.getlabel().equals(tierLabel))
      {
        tp.setGlyphBackgroundColour( newColor );
      }
    }
  }
  /**
   * Prepare the panel for data
   *
   * @param showSequenceTier
   */
  void initialiseView( boolean showSequenceTier, boolean showCoordinatesTier )
  {
    // Clear and then add the required TierParameters objects
    //
    _tierParameters.clear();

    if ( showSequenceTier )
    {
      addTierParameters( _sequenceTierParameters );
    }

    if ( showCoordinatesTier )
    {
      addTierParameters( _coordsTierParameters );
    }

    // Reinitialise the state of the tier parameters objects
    //
    for ( TierParameters tp : Arrays.asList( _coordsTierParameters, _sequenceTierParameters ) )
    {
      tp.setTierIndex( -1 );
      tp.setHasBeenDisplayed( false );
    }

    // Reset class instance state
    //
    _doFullRepack = true;
    _glyphUnderPointer = null;
    _labelGlyphPopupIndex = -1;

    layoutNeoMap();
  }

  //public void
  
  /***
   * Reinitialise the tiered map. Firstly the widget is cleared before axes, tiers etc.
   * are added.
   *
   * @param sr The {@link SequenceRange} object to use as the governing sequence - usually a chromosome.
   */
  final void reinitialise()
  {
    _doFullRepack = true;

    _labelGlyphPopupIndex = -1;

    clearNeoMaps();

    _detailMap.setMapRange( 0, getSequenceLength() );
    _detailMap.stretchToFit( true, false );

    // Configure the detail tiered map
    //
    _detailMap.enableDragScrolling( true );
    _detailMap.setBackground( Color.white );
    _detailMap.setPixelFuzziness( 1 );
    _detailMap.setReshapeBehavior( NeoAbstractWidget.X, NeoConstants.NONE );
    _detailMap.setSelectionAppearance( SceneI.SELECT_OUTLINE );
    _detailMap.setSelectionColor( Color.darkGray );
    _detailMap.setSelectionEvent( NeoMap.ON_MOUSE_DOWN );
    _detailMap.setScroller( NeoMap.X, scrollH );
    _detailMap.setScroller( NeoMap.Y, scrollV );

    configureDetailMapZoom();

    // Configure the label map
    //
    _labelMap.setBackground( TierParameters.V_DARK_GRAY );
    _labelMap.setBackground( pnlSplitLeft.getBackground() );
    _labelMap.setSelectionAppearance( SceneI.SELECT_OUTLINE );
    _labelMap.setSelectionColor( Color.red );
    _labelMap.setSelectionEvent( NeoMap.ON_MOUSE_DOWN );
    _labelMap.setScroller( NeoMap.Y, scrollV );
    _labelMap.setRubberBandBehavior( false );
    _labelMap.setUniformBackgroundColor( TierParameters.V_DARK_GRAY );

    // The reshape behaviour must be the same
    //
    _detailMap.setReshapeBehavior( NeoAbstractWidget.Y, NeoConstants.NONE );
    _labelMap.setReshapeBehavior( NeoAbstractWidget.Y, NeoConstants.NONE );

    // Add the tiers to the NeoMap
    addTiers();

    displayData();
  }

  private void clearNeoMaps()
  {
    if ( _detailMap != null )
    {
      // Break the link between the glyphs and the objects to help GC
      //
      for ( int i = 0; i < _detailMap.getTierCount(); ++i )
      {
        List<GlyphI> l = _detailMap.getTierAt( i ).getChildren();

        if ( l == null )
          continue;

        for ( GlyphI g : l )
        {
          g.setInfo( null );
        }
      }

      // The clearWidget() method sets a new scene glyph (copying some state), removes axes, transients and tiers
      //
      _detailMap.clearWidget();
    }

    if ( _labelMap != null )
    {
      _labelMap.clearWidget();
    }

    System.gc();
  }

  /***
   * Move the detail map to the specified start position
   *
   * @param xStart The start coordinate
   */
  final void moveTo( double xStart )
  {
    _detailMap.scrollRange( xStart );
    _detailMap.updateWidget();
  }

  /***
   * Move the view by the specified 'jump' percentage, i.e. 100 means one screenful to the right
   *
   * @param jump Percentage expressed as an integer
   */
  final void jumpView( int jumpPercent )
  {
    if ( jumpPercent == 0 || _vissrMainFrame.getCurrentReferenceSequence() == null )
      return;

    Rectangle2D.Double vb = _detailMap.getViewBounds();

    // Make sure the coordinate stays within the current bounds
    double x1 = Math.max( 0, vb.x + vb.width * jumpPercent / 100 );
    x1 = Math.min( x1, _vissrMainFrame.getCurrentReferenceSequenceLength() - vb.width );

    _vissrMainFrame.updateViewableRange( x1, x1 + vb.width );
  }

  /***
   * Zoom the detail map
   *
   * @param start The new start coordinate
   * @param end The new end coordinate
   */
  final void zoomTo( double start, double end )
  {
    start = Math.max( start, 0 );
    end   = Math.min( end, getSequenceLength() );

    double midCoord = 0.5 * ( start + end );

    long coord_width = Math.round( end - start );
    int pixel_width = _detailMap.getView().getPixelBox().width;

    double maxZoom = _detailMap.getMaxZoom( NeoAbstractWidget.X );
    double pixels_per_coord = (double) pixel_width / coord_width;

    if ( pixels_per_coord >= maxZoom )
    {
      pixels_per_coord = maxZoom;

      start = midCoord - pixel_width / 2 / pixels_per_coord;
      start = Math.max( start, 0 );
    }

    _detailMap.zoom( NeoAbstractWidget.X, pixels_per_coord );
    _detailMap.scrollRange( start );

    _detailMap.updateWidget();
  }

  final void zoomTo( GlyphI glyph )
  {
    if ( glyph == null )
      return;

    double padding = 0.1 * glyph.getCoordBox().width;

    double start = glyph.getCoordBox().x - padding;
    double end   = glyph.getCoordBox().x + glyph.getCoordBox().width + padding;

    zoomTo( start, end );
  }

  /***
   * Iterate through the tier parameters and display the data.
   * Perform a map layout when the glyphs have been added.
   *
   * @param displaySequenceTier Whether or not the sequence tier needs displaying
   */
  final void displayData()
  {
    for ( TierParameters tp : _tierParameters )
    {
      if ( ! tp.hasBeenDisplayed() )
      {
        addGlyphsToTier( tp );
        tp.setHasBeenDisplayed( true );
      }
    }

    layoutNeoMap();
  }

  /***
   * Search for a given key - search implementation is in {@link VisSRSequenceHelper#isInSearchKeys( SequenceRangeI, String )}
   *
   * @param searchKey
   * @return Whether anything was found
   */
  final boolean findByKey( String searchKey )
  {
    if ( _tierParameters.isEmpty() || _vissrMainFrame.getCurrentReferenceSequence() == null )
      return false;

    // Perform the search...
    //
    ArrayList< SequenceRangeI> hits = CollectionUtils.newArrayList();

    for ( TierParameters tp : _tierParameters )
    {
      if ( ! tp.getTierType().isUserSpecified() )
        continue;

      List< SequenceRangeI > l = tp.getListForId( _vissrMainFrame.getCurrentReferenceSequence().getSequenceId() );

      for ( SequenceRangeI sr : l )
      {
        if ( VisSRSequenceHelper.isInSearchKeys( sr, searchKey ) )
        {
          hits.add( sr );
        }
      }
    }

    // Select the glyphs (and zoom to them if necessary)

    deselectGlyphs();

    if ( ! hits.isEmpty() )
    {
      _vissrMainFrame.setBusyCursor( true );
      try
      {
        selectGlyphs( hits );
      }
      finally
      {
        _vissrMainFrame.setBusyCursor( false );
      }

      int minIndex = Integer.MAX_VALUE;
      int maxIndex = Integer.MIN_VALUE;

      for ( SequenceRangeI hit : hits )
      {
        minIndex = Math.min( minIndex, hit.getStartIndex() );
        maxIndex = Math.max( maxIndex, hit.getEndIndex() );
      }

      _zoomPoint.setSpot( ( minIndex + maxIndex ) / 2 );

      zoomTo( minIndex - 100, maxIndex + 100 );
    }

    return ! hits.isEmpty();
  }

  /***
   * Help tidy up
   */
  final void dispose()
  {
    _tierParameters.clear();

    clearNeoMaps();

    // Nullify instance variables
    //
    _detailMap = null;
    _hairline = null;
    _labelMap = null;
    _glyphUnderPointer = null;
    _xzoomer = null;
    _zoomPoint = null;
  }

  //////////////////////////////////////////////////////////////////////////////

  /***
   * Add the sequence tier and the coordinates tier to the detail map, followed
   * by a number of empty tiers.
   */
  private void addTiers()
  {
    // Clear state in each TierParameters
    //
    for ( TierParameters tp : _tierParameters )
    {
      tp.setTierIndex( -1 );
      tp.setHasBeenDisplayed( false );
    }

    // Add the tiers which will display sequence annotations and user data
    //
    for ( TierParameters tp : _tierParameters )
    {
      addTierIfNecessary( tp );
    }

    setTiersBackgroundColour();
  }

  private void setTiersBackgroundColour()
  {
    // The background colour changes in alternate tiers to aid visibility

    for ( int i = 0; i < _tierParameters.size(); ++i )
    {
      TierParameters tp = _tierParameters.get( i );

      if ( tp.getTierIndex() == -1 )
        continue;

      Color fillColor = ( i % 2 == 0 ? TierParameters.ODDS_COLOUR : TierParameters.EVENS_COLOUR );

      _detailMap.getTierAt( tp.getTierIndex() ).setBackgroundColor( fillColor );
    }
  }

  private void addTierIfNecessary( TierParameters tp )
  {
    // Add the tier to the map if it's not there already
    //
    int index = tp.getTierIndex();

    if ( index == -1 )
    {
      double h = TIER_HEIGHT / ( tp.getTierType().isUserSpecified() ? 1 : 2 );

      index = addBasicTier( h );
      tp.setTierIndex( index );

      if ( ! tp.getTierType().isUserSpecified() )
      {
        // The coordinates and sequence tiers are fixed size.
        _detailMap.getTierAt( tp.getTierIndex() ).setState( MapTierGlyph.FIXED_SIZE );
      }
    }

    // Set the label
    //
    MapTierGlyph labelTier = _labelMap.getTierAt( index );

    String label = tp.getTierLabel();

    if ( tp.getTierType() == TierParameters.TierType.USER_SPECIFIED_PARESNIP )
    {
      String sequenceId = _vissrMainFrame.getCurrentReferenceSequence().getSequenceId();
      List< SequenceRangeI > l = tp.getListForId( sequenceId );

      label = tp.getTierLabel() + " (" + l.size() + ")";
    }
    labelTier.setLabel( label );

    labelTier.setBackgroundColor( tp.getTierLabelBackgroundColour() );

    // Want to do a full repack next time
    _doFullRepack = true;
  }

  /**
   * Create an empty tier with the given label and add it to the detail map
   * @param label The tier label
   * @return The index of the new {@code MapTierGlyph} object
   */
  private int addBasicTier( double tierHeight )
  {
    MapTierGlyph mtg = new MapTierGlyph();

    int[] r = _detailMap.getMapRange();

    mtg.setCoords( (double) r[0], 0, (double) r[1], tierHeight );
    mtg.setFillColor( Color.white );
    mtg.setHitable( true );
    mtg.setSelectable( false );
    mtg.setShowLabel( false );
    mtg.setState( MapTierGlyph.EXPANDED );

    _detailMap.addTier( mtg );

    return _detailMap.getAllTiers().indexOf( mtg );
  }

  private void layoutNeoMap()
  {
    _vissrMainFrame.setBusyCursor( true );

    try
    {
      setTiersBackgroundColour();

      if ( _doFullRepack )
      {
        _detailMap.repack();
        _labelMap.repack();

        _doFullRepack = false;
      }

      _detailMap.stretchToFit( false, false );
      _labelMap.stretchToFit( false, false );

      _detailMap.updateWidget();
      _labelMap.updateWidget();

      _glyphUnderPointer = null;
    }
    finally
    {
      _vissrMainFrame.setBusyCursor( false );
    }
  }

  /***
   * Select the glyphs corresponding to the records in the list.
   *
   * @param hits List of {@link GFFRecord}s
   */
  private void selectGlyphs( List< SequenceRangeI > hits )
  {
    if ( hits == null || hits.isEmpty() )
      return;

    // limit the search range
    //
    int minIndex = Integer.MAX_VALUE;
    int maxIndex = Integer.MIN_VALUE;

    for ( SequenceRangeI sr : hits )
    {
      minIndex = Math.min( minIndex, sr.getStartIndex() );
      maxIndex = Math.max( maxIndex, sr.getEndIndex() );
    }

    // Get a list of glyphs which are contained by a rectangle (i.e. let the detail map do the work)
    Rectangle2D.Double rect = new Rectangle2D.Double( minIndex, _detailMap.getBounds().y, maxIndex - minIndex, _detailMap.getBounds().height );
    List<GlyphI> l = _detailMap.getItemsByCoord( rect );

    if ( l == null || l.isEmpty() )
      return;

    // select the glyphs
    //
    for ( SequenceRangeI sr : hits )
    {
      for ( GlyphI g : l )
      {
        if ( sr == g.getInfo() )
        {
          g.setSelected( true );
        }
      }
    }

    _detailMap.updateWidget();
  }

  private void deselectGlyphs()
  {
    // Using _detailMap.getSelected() doesn't always work, so make sure...

    for ( int i = 0; i < _detailMap.getTierCount(); ++i )
    {
      List<GlyphI> l = _detailMap.getTierAt( i ).getChildren();

      if ( l == null )
        continue;

      for ( GlyphI g : l )
      {
        g.setSelected( false );
      }
    }

    _detailMap.updateWidget();
  }

  /***
   * Utility method to avoid checking for a null current sequence
   *
   * @return The sequence length or 1000 if the sequence object is null
   */
  private int getSequenceLength()
  {
    return _vissrMainFrame.getCurrentReferenceSequenceLength();
  }

  /*
   * Perform the search - code not in generated method to avoid inadvertant deletion from the GUIBuilder
   */
  private void doPopupDataSearch()
  {
    SequenceRangeI sr = getSequenceRangeUnderPointer();

    if ( sr == null )
      return;

    Set<String> keys = VisSRSequenceHelper.getWebSearchKeys( sr );

    if ( keys == null || keys.isEmpty() )
      return;

    StringBuilder sb = new StringBuilder( "http://www.google.com/search?q=" );

    for ( String s : keys )
    {
      sb.append( s ).append( '+' );
    }

    String searchStr = sb.substring( 0, sb.length() - 1 );

    try
    {
      URI uri = URI.create( searchStr );
      Desktop.getDesktop().browse( uri );
    }
    catch ( IOException ex )
    {
      _vissrMainFrame.displayStatusMessage( "An error occurred when navigating to " + searchStr, false );
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  /*
   * Perform the copy - code not in generated method to avoid inadvertant deletion from the GUIBuilder
   */
  private void doPopupDataCopy()
  {
    SequenceRangeI sr = getSequenceRangeUnderPointer();

    if ( sr == null )
      return;

    String copyText = VisSRSequenceHelper.formatPlainText( sr );

    StringSelection ss = new StringSelection( copyText );

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( ss, null );

    _vissrMainFrame.displayStatusMessage( "Copied to clipboard", false );
  }

  private void gotoFeature( boolean gotoNextFeature )
  {
    MapTierGlyph mtg = getMapTierGlyph( _glyphUnderPointer );

    if ( mtg == null )
      return;

    final double startIndex = (double)_glyphUnderPointer.getCoordBox().x;

    GlyphI nextGlyph = null;

    if ( gotoNextFeature )
    {
      for ( GlyphI glyph : mtg.getChildren() )
      {
        if ( glyph.getCoordBox().x > startIndex )
        {
          if ( nextGlyph == null )
          {
            nextGlyph = glyph;
          }
          else
          {
            if ( glyph.getCoordBox().x < nextGlyph.getCoordBox().x )
            {
              nextGlyph = glyph;
            }
          }
        }
      }
    }
    else
    {
      for ( GlyphI glyph : mtg.getChildren() )
      {
        if ( glyph.getCoordBox().x < startIndex )
        {
          if ( nextGlyph == null )
          {
            nextGlyph = glyph;
          }
          else
          {
            if ( glyph.getCoordBox().x > nextGlyph.getCoordBox().x )
            {
              nextGlyph = glyph;
            }
          }
        }
      }
    }

    if ( nextGlyph != null )
    {
      deselectGlyphs();

      zoomTo( nextGlyph );

      nextGlyph.setSelected( true );
      _detailMap.updateWidget();
    }
  }

  private void doShowReads()
  {
    SequenceRangeI sr = getSequenceRangeUnderPointer();

    if ( ! ( sr instanceof SequenceWindow ) )
      return;

    SequenceWindow sw = (SequenceWindow)sr;

    Patman p = sw.getPatmanEntries();

    p.setGlyphHeightFromAbundance();

    TierParameters tp = new TierParameters.Builder( sw.getSequenceId() ).build();
    tp.addListForId( _vissrMainFrame.getCurrentReferenceSequence().getSequenceId(), p );

    addTierParameters( tp );
    displayData();
  }

  void showParesnipTplots( Set< Category > cats )
  {
    if ( cats == null || cats.isEmpty() )
      return;

    // Get the lists of PlotRecords for which the category matches
    // Assume a tier contains records with the same category.

    List< List< SequenceRangeI > > records = CollectionUtils.newArrayList();

    String sequenceId = _vissrMainFrame.getCurrentReferenceSequence().getSequenceId();

    for ( TierParameters tp : _tierParameters )
    {
      if ( tp.getTierType().isUserSpecified() && tp.getTierIndex() != -1 )
      {
        List< SequenceRangeI > l = tp.getListForId( sequenceId );

        if ( l.size() > 0 )
        {
          SequenceRangeI sr = l.get( 0 );

          if ( sr instanceof PlotRecord )
          {
            Category cat = ( (PlotRecord)sr ).getMaxCategory();
            if ( cats.contains( cat ) )
            {
              records.add( l );
            }

            continue;
          }
        }
      }
    }

    if ( records.isEmpty() )
      return;

    // Build a list of the PlotRecords

    List< PlotRecord > plotRecords = new ArrayList< PlotRecord >();

    for ( List< SequenceRangeI > l : records )
    {
      for ( SequenceRangeI sr : l )
      {
        if ( sr instanceof PlotRecord ) // this is belt and braces
        {
          plotRecords.add( (PlotRecord) sr );
        }
      }
    }

    if ( plotRecords.isEmpty() )
      return;

    showTPlotFrame( plotRecords, 0 );
  }

  private void showParesnipTPlots()
  {
    SequenceRangeI srup = getSequenceRangeUnderPointer();

    if ( ! ( srup instanceof PlotRecord ) )
      return;

    // Get the plot records from the TierParameters

    PlotRecord selectedPlotRecord = (PlotRecord) srup;
    List< SequenceRangeI > records = null;

    String sequenceId = _vissrMainFrame.getCurrentReferenceSequence().getSequenceId();

    for ( TierParameters tp : _tierParameters )
    {
      if ( tp.getTierType().isUserSpecified() && tp.getTierIndex() != -1 )
      {
        List< SequenceRangeI > l = tp.getListForId( sequenceId );

        if ( l.contains( selectedPlotRecord ) )
        {
          records = l;
          break;
        }
      }
    }

    if ( records == null )
      return;

    // Build a list of the PlotRecords

    List< PlotRecord > plotRecords = new ArrayList< PlotRecord >( records.size() );

    for ( SequenceRangeI sr : records )
    {
      if ( sr instanceof PlotRecord ) // this is belt and braces
      {
        plotRecords.add( (PlotRecord) sr );
      }
    }

    if ( plotRecords.isEmpty() )
      return;

    int plotRecordIndex = records.indexOf( srup );

    showTPlotFrame( plotRecords, plotRecordIndex );
  }

  private void showTPlotFrame( List< PlotRecord > plotRecords, int selectionIndex )
  {
    // Create the frame
    TPlotFrame tpf = new TPlotFrame( plotRecords, selectionIndex );

    // Add it as a tool etc.
    _vissrMainFrame.getParent().add( tpf );
    ToolManager.getInstance().addTool( tpf );

    // Set size and show it
    tpf.setBounds( _vissrMainFrame.getX() + 20, _vissrMainFrame.getY() + 20, tpf.getWidth(), tpf.getHeight() );
    tpf.setVisible( true );
  }

  private SequenceRangeI getSequenceRangeUnderPointer()
  {
    if ( _glyphUnderPointer == null )
      return null;

    if ( !( _glyphUnderPointer.getInfo() instanceof SequenceRangeI ) )
      return null;

    SequenceRangeI sr = (SequenceRangeI) _glyphUnderPointer.getInfo();

    return sr;
  }

  /**
   * Get the MapTierGlyph which contains the given the glyph
   *
   * @param glyph
   * @return A MapTierGlyph
   */
  private MapTierGlyph getMapTierGlyph( GlyphI glyph )
  {
    if ( glyph == null )
      return null;

    MapTierGlyph result = null;

    for ( int i = 0; i < _detailMap.getTierCount(); ++i )
    {
      MapTierGlyph mtg = _detailMap.getTierAt( i );

      if ( mtg.getChildCount() > 0 )
      {
        if ( mtg.getChildren().indexOf( _glyphUnderPointer ) != -1 )
        {
          result = mtg;
          break;
        }
      }
    }

    return result;
  }

  private void configureDetailMapZoom()
  {
    // Set the zoomer GUI control
    _detailMap.setZoomer( NeoMap.X, _xzoomer );

    // Add a hairline to the detail map (NOTE: HORIZONTAL refers to map's orientation)
    //
    if ( _hairline == null )
    {
      _hairline = new Shadow( _detailMap, NeoConstants.HORIZONTAL, Color.darkGray );
      _zoomPoint.addListener( _hairline );
    }
    else
    {
      _hairline.resetShadow( _detailMap, NeoConstants.HORIZONTAL, Color.darkGray );
    }
  }

  /**
   * Add glyphs to the main data area
   *
   * @param tp TierParameters providing meta-data and data for the tier
   */
  private void addGlyphsToTier( TierParameters tp )
  {
    if ( tp == null || _vissrMainFrame.getCurrentReferenceSequence() == null )
      return;

    MapTierGlyph dataTier = _detailMap.getTierAt( tp.getTierIndex() );

    switch ( tp.getTierType() )
    {
      case COORDINATES:
        GlyphI axisGlyph = _detailMap.addAxis( (int) dataTier.getCoordBox().y + 5 );
        dataTier.addChild( axisGlyph );
        break;

      case SEQUENCE:
        ColoredResiduesGlyph sequenceGlyph = new ColoredResiduesGlyph();

        sequenceGlyph.setResidues( _vissrMainFrame.getCurrentReferenceSequence().getSequence() );
        sequenceGlyph.setCoordBox( dataTier.getCoordBox() );
        sequenceGlyph.setBackgroundColor( TierParameters.V_DARK_GRAY );

        dataTier.addChild( sequenceGlyph );
        break;

      case USER_SPECIFIED:
      case USER_SPECIFIED_PARESNIP:
        List< SequenceRangeI> l = tp.getListForId( _vissrMainFrame.getCurrentReferenceSequence().getSequenceId() );

        if ( l.isEmpty() )
          return;

        // Initialise the factory before adding glyphs
        MapGlyphFactory fac = _detailMap.getFactory();

        fac.setOffset( 0 );
        fac.setBackgroundColor( tp.getGlyphBackgroundColour() );
        fac.setGlyphtype( tp.getGlyphClass() );

        // Iterate through the data, create the glyphs and add them to the tier
        //
        for ( SequenceRangeI sr : l )
        {
          GlyphI glyph = VisSRSequenceHelper.createGlyph( sr, fac, TIER_HEIGHT );

          dataTier.addChild( glyph );
        }

        break;
    }
  }

  /**
   * Get the {@code TierParameters} object index.<br/>
   * There is usually a one-to-one relation between tier indices and {@code TierParameters} but sometimes
   * the sequence tier is not shown and so the indices will be off by one.
   *
   * @return Integer index of {link TierParameters} object corresponding to the right-clicked label.
   */
  private int getTierParametersIndex()
  {
    if ( _labelGlyphPopupIndex == -1 )
      return -1;

    for ( int i = 0; i < _tierParameters.size(); ++i )
    {
      TierParameters t = _tierParameters.get( i );

      if ( _labelGlyphPopupIndex == t.getTierIndex() )
        return i;
    }

    return -1;
  }

  /** This method is called from within the constructor to
   * initialise the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    popupData = new javax.swing.JPopupMenu();
    popupDataMI_doCopy = new javax.swing.JMenuItem();
    popupDataMI_doSearch = new javax.swing.JMenuItem();
    popupDataMI_paresnipTPlot = new javax.swing.JMenuItem();
    popupDataMI_doNextFeature = new javax.swing.JMenuItem();
    popupDataMI_doPrevFeature = new javax.swing.JMenuItem();
    popupDataMI_showReads = new javax.swing.JMenuItem();
    popupLabel = new javax.swing.JPopupMenu();
    popupLabelMI_MoveUp = new javax.swing.JMenuItem();
    popupLabelMI_MoveDown = new javax.swing.JMenuItem();
    sep1 = new javax.swing.JPopupMenu.Separator();
    popupLabelMI_SetLabel = new javax.swing.JMenuItem();
    popupLabelMI_UseThisColour = new javax.swing.JMenuItem();
    popupLabelMI_SetLabelBGColour = new javax.swing.JMenuItem();
    popupLabelMI_SetGraphicColour = new javax.swing.JMenuItem();
    sep2 = new javax.swing.JPopupMenu.Separator();
    popupLabelMI_DeleteTier = new javax.swing.JMenuItem();
    pnlBottom = new javax.swing.JPanel();
    scrollH = new javax.swing.JScrollBar();
    filler = new javax.swing.Box.Filler(new java.awt.Dimension(17, 17), new java.awt.Dimension(17, 17), new java.awt.Dimension(17, 17));
    split = new javax.swing.JSplitPane();
    pnlSplitLeft = new javax.swing.JPanel();
    pnlSplitRight = new javax.swing.JPanel();
    scrollV = new javax.swing.JScrollBar();
    pnlTop = new javax.swing.JPanel();
    lblZoomOut = new javax.swing.JLabel();
    lblZoomIn = new javax.swing.JLabel();
    pnlZoomer = new javax.swing.JPanel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 4), new java.awt.Dimension(0, 4), new java.awt.Dimension(32767, 4));
    zoomSliderDummy = new javax.swing.JSlider();

    popupDataMI_doCopy.setText("Copy");
    popupData.add(popupDataMI_doCopy);

    popupDataMI_doSearch.setText("Search...");
    popupData.add(popupDataMI_doSearch);

    popupDataMI_paresnipTPlot.setText("View T-plots");
    popupData.add(popupDataMI_paresnipTPlot);

    popupDataMI_doNextFeature.setText("Next feature in track");
    popupData.add(popupDataMI_doNextFeature);

    popupDataMI_doPrevFeature.setText("Previous feature in track");
    popupData.add(popupDataMI_doPrevFeature);

    popupDataMI_showReads.setText("Show all reads");
    popupData.add(popupDataMI_showReads);

    popupLabelMI_MoveUp.setText("Move up");
    popupLabelMI_MoveUp.setActionCommand("UP");
    popupLabel.add(popupLabelMI_MoveUp);

    popupLabelMI_MoveDown.setText("Move down");
    popupLabelMI_MoveDown.setActionCommand("DOWN");
    popupLabel.add(popupLabelMI_MoveDown);
    popupLabel.add(sep1);

    popupLabelMI_SetLabel.setText("Rename tier...");
    popupLabel.add(popupLabelMI_SetLabel);

    popupLabelMI_UseThisColour.setText("Use colour for all labels");
    popupLabel.add(popupLabelMI_UseThisColour);

    popupLabelMI_SetLabelBGColour.setText("Set label colour...");
    popupLabel.add(popupLabelMI_SetLabelBGColour);

    popupLabelMI_SetGraphicColour.setText("Set glyph colour...");
    popupLabel.add(popupLabelMI_SetGraphicColour);
    popupLabel.add(sep2);

    popupLabelMI_DeleteTier.setText("Remove tier...");
    popupLabel.add(popupLabelMI_DeleteTier);

    setBackground(new java.awt.Color(120, 120, 120));
    setForeground(java.awt.Color.white);
    setLayout(new java.awt.BorderLayout());

    pnlBottom.setLayout(new java.awt.BorderLayout());

    scrollH.setBackground(new java.awt.Color(120, 120, 120));
    scrollH.setForeground(new java.awt.Color(120, 120, 120));
    scrollH.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
    pnlBottom.add(scrollH, java.awt.BorderLayout.CENTER);

    filler.setForeground(new java.awt.Color(120, 120, 120));
    pnlBottom.add(filler, java.awt.BorderLayout.LINE_END);

    add(pnlBottom, java.awt.BorderLayout.PAGE_END);

    split.setDividerLocation(120);
    split.setDividerSize(4);

    pnlSplitLeft.addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(java.awt.event.ComponentEvent evt)
      {
        pnlSplitLeftComponentResized(evt);
      }
    });
    pnlSplitLeft.setLayout(new java.awt.BorderLayout());
    split.setLeftComponent(pnlSplitLeft);

    pnlSplitRight.setLayout(new java.awt.BorderLayout());
    split.setRightComponent(pnlSplitRight);

    add(split, java.awt.BorderLayout.CENTER);

    scrollV.setBackground(new java.awt.Color(120, 120, 120));
    scrollV.setForeground(new java.awt.Color(120, 120, 120));
    add(scrollV, java.awt.BorderLayout.LINE_END);

    pnlTop.setLayout(new java.awt.BorderLayout());

    lblZoomOut.setBackground(new java.awt.Color(120, 120, 120));
    lblZoomOut.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    lblZoomOut.setForeground(java.awt.Color.white);
    lblZoomOut.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblZoomOut.setText("-");
    lblZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    lblZoomOut.setMaximumSize(new java.awt.Dimension(20, 29));
    lblZoomOut.setMinimumSize(new java.awt.Dimension(20, 29));
    lblZoomOut.setOpaque(true);
    lblZoomOut.setPreferredSize(new java.awt.Dimension(20, 29));
    pnlTop.add(lblZoomOut, java.awt.BorderLayout.LINE_START);

    lblZoomIn.setBackground(new java.awt.Color(120, 120, 120));
    lblZoomIn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
    lblZoomIn.setForeground(java.awt.Color.white);
    lblZoomIn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblZoomIn.setText("+");
    lblZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    lblZoomIn.setOpaque(true);
    lblZoomIn.setPreferredSize(new java.awt.Dimension(20, 29));
    pnlTop.add(lblZoomIn, java.awt.BorderLayout.LINE_END);

    pnlZoomer.setLayout(new java.awt.BorderLayout());

    filler1.setBackground(new java.awt.Color(120, 120, 120));
    filler1.setOpaque(true);
    pnlZoomer.add(filler1, java.awt.BorderLayout.PAGE_START);

    zoomSliderDummy.setBackground(new java.awt.Color(120, 120, 120));
    pnlZoomer.add(zoomSliderDummy, java.awt.BorderLayout.CENTER);

    pnlTop.add(pnlZoomer, java.awt.BorderLayout.CENTER);

    add(pnlTop, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents

  private void pnlSplitLeftComponentResized( java.awt.event.ComponentEvent evt )//GEN-FIRST:event_pnlSplitLeftComponentResized
  {//GEN-HEADEREND:event_pnlSplitLeftComponentResized
    // Update the maps to make sure they fit the new size
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        _labelMap.updateWidget();
        _detailMap.updateWidget();
      }
    } );
  }//GEN-LAST:event_pnlSplitLeftComponentResized

    /***
   * Initialise non-designed components, e.g. genoviz
   */
  private void initOtherComponents()
  {
    /** Add to the panels **/
    // this zoomer was only added to get a feel for what the UI will look like
    this.remove( zoomSliderDummy );

    // Add the zoomer (which implements Adjustable)
    pnlZoomer.add( _xzoomer, BorderLayout.CENTER );
    _xzoomer.setBackground( this.getBackground() );
    _xzoomer.setOpaque( true );

    // Add the tiered maps
    pnlSplitLeft.add( _labelMap, BorderLayout.CENTER );
    pnlSplitRight.add( _detailMap, BorderLayout.CENTER );

    _labelMap.setBackground( Color.white );
    _labelMap.setUniformBackgroundColor( Color.white );
    _detailMap.setBackground( Color.white );

    addEventHandlers();
  }

  /***
   * Add additional event handlers
   */
  private void addEventHandlers()
  {
    /*
     * VERY IMPORTANT NOTE
     * ===================
     * Adding listeners to some (all ?) of the Genoviz controls should be performed with care.
     * The Genoviz classes maintain their own collections, e.g. MouseListeners, and do not add
     * the listeners to the collection in the super-class (e.g. java.awt.Component).
     * So when the listener collection is requested, e.g. 'getMouseListeners()', the
     * Component's implementation is called because the method has not been overridden and
     * an empty array is returned !
     * So, add event handlers to Genoviz controls once.
     * Or keep a reference to the listener because the remove***Listener(..) method works.
     */

    // Make the detail map respond to rangeChanged events which are triggered
    // when the user moves the viewable area about
    //
    _detailMap.addRangeListener( new NeoRangeListener()
    {
      @Override
      public void rangeChanged( NeoRangeEvent evt )
      {
        _vissrMainFrame.updateViewableRange( evt.getVisibleStart(), evt.getVisibleEnd() );
      }

    } );

    // Add a mouse adapter to the labels on the left of the main data view
    //
    LabelMouseAdapterImpl lma = new LabelMouseAdapterImpl();
    _labelMap.addMouseListener( (MouseListener) lma );

    // Move the zoom point in the detail map when the mouse button is released
    //
    DetailMouseAdapterImpl dma = new DetailMouseAdapterImpl(normalised);
    _detailMap.addMouseListener( (MouseListener) dma );
    _detailMap.addMouseMotionListener( (MouseMotionListener) dma );

    // Set up a symbiotic relationship between the TieredNeoMaps for automatic labelling
    //
    _labelMap.addTierEventListener( _detailMap );
    _detailMap.addTierEventListener( _labelMap );

    // Configure the zoom adjustment for the zoom point
    // This listener code makes sure the hairline stays in the same place as the view is zoomed.
    //
    _zoomPoint.addListener( new NeoRangeListener()
    {
      @Override
      public void rangeChanged( NeoRangeEvent e )
      {
        // e.getSource() is com.affymetrix.genoviz.widget.VisibleRange (i.e. _zoomPoint)

        double midPoint = ( e.getVisibleEnd() + e.getVisibleStart() ) / 2;

        _detailMap.setZoomBehavior( NeoMap.X, NeoMap.CONSTRAIN_COORD, midPoint );
        _detailMap.updateWidget();
      }

    } );

    // Data popup stuff

    popupDataMI_doCopy.setAction( new AbstractAction( popupDataMI_doCopy.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        doPopupDataCopy();
      }
    } );

    popupDataMI_doSearch.setAction( new AbstractAction( popupDataMI_doSearch.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        doPopupDataSearch();
      }
    } );

    popupDataMI_paresnipTPlot.setAction( new AbstractAction( popupDataMI_paresnipTPlot.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        showParesnipTPlots();
      }
    } );

    popupDataMI_doNextFeature.setAction( new AbstractAction( popupDataMI_doNextFeature.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        gotoFeature( true );
      }
    } );

    popupDataMI_doPrevFeature.setAction( new AbstractAction( popupDataMI_doPrevFeature.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        gotoFeature( false );
      }
    } );

    popupDataMI_showReads.setAction( new AbstractAction( popupDataMI_showReads.getText() )
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        doShowReads();
      }
    } );

    // Label popup stuff

    popupLabelMI_MoveDown.setAction( new MoveTierAction( popupLabelMI_MoveDown.getText(), false ) );
    popupLabelMI_MoveUp.setAction( new MoveTierAction( popupLabelMI_MoveUp.getText(), true ) );

    popupLabelMI_SetLabel.setAction( new RenameTierAction( popupLabelMI_SetLabel.getText() ) );

    popupLabelMI_SetGraphicColour.setAction( new SetTierGraphicColourAction( popupLabelMI_SetGraphicColour.getText() ) );
    popupLabelMI_SetLabelBGColour.setAction( new SetTierLabelBackgroundColourAction( popupLabelMI_SetLabelBGColour.getText() ) );
    popupLabelMI_UseThisColour.setAction( new SetTierLabelsColourAction( popupLabelMI_UseThisColour.getText() ) );

    popupLabelMI_DeleteTier.setAction( new RemoveTierAction( popupLabelMI_DeleteTier.getText() ) );
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.Box.Filler filler;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JLabel lblZoomIn;
  private javax.swing.JLabel lblZoomOut;
  private javax.swing.JPanel pnlBottom;
  private javax.swing.JPanel pnlSplitLeft;
  private javax.swing.JPanel pnlSplitRight;
  private javax.swing.JPanel pnlTop;
  private javax.swing.JPanel pnlZoomer;
  private javax.swing.JPopupMenu popupData;
  private javax.swing.JMenuItem popupDataMI_doCopy;
  private javax.swing.JMenuItem popupDataMI_doNextFeature;
  private javax.swing.JMenuItem popupDataMI_doPrevFeature;
  private javax.swing.JMenuItem popupDataMI_doSearch;
  private javax.swing.JMenuItem popupDataMI_paresnipTPlot;
  private javax.swing.JMenuItem popupDataMI_showReads;
  private javax.swing.JPopupMenu popupLabel;
  private javax.swing.JMenuItem popupLabelMI_DeleteTier;
  private javax.swing.JMenuItem popupLabelMI_MoveDown;
  private javax.swing.JMenuItem popupLabelMI_MoveUp;
  private javax.swing.JMenuItem popupLabelMI_SetGraphicColour;
  private javax.swing.JMenuItem popupLabelMI_SetLabel;
  private javax.swing.JMenuItem popupLabelMI_SetLabelBGColour;
  private javax.swing.JMenuItem popupLabelMI_UseThisColour;
  private javax.swing.JScrollBar scrollH;
  private javax.swing.JScrollBar scrollV;
  private javax.swing.JPopupMenu.Separator sep1;
  private javax.swing.JPopupMenu.Separator sep2;
  private javax.swing.JSplitPane split;
  private javax.swing.JSlider zoomSliderDummy;
  // End of variables declaration//GEN-END:variables

  void setNormalised( boolean normalised )
  {
    this.normalised = normalised;
    //MouseListener[] mouseListeners = _detailMap.getMouseListeners();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Mouse adapter for the detail map
   */
  private final class DetailMouseAdapterImpl extends MouseAdapter
  {
    private int _savedDismissDelay = 0;
    private int _savedInitialDelay = 0;
    private boolean normalised = false;

    public DetailMouseAdapterImpl(boolean normalised) 
    {
      this.normalised = normalised;
    }

    private void processPopupTrigger( MouseEvent e )
    {
      SequenceRangeI sr = getSequenceRangeUnderPointer();

      if ( sr == null )
        return;

      popupDataMI_doSearch.setEnabled( VisSRSequenceHelper.isWebSearchable( sr ) );
      popupDataMI_paresnipTPlot.setVisible( sr instanceof PlotRecord );
      popupDataMI_showReads.setVisible( sr instanceof SequenceWindow );

      popupData.show( e.getComponent(), e.getX(), e.getY() );
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
      if ( !( e instanceof NeoMouseEvent ) )
        return;

      if ( e.getSource() != _detailMap )
        return;

      // Only in here to catch the popup trigger on a mac (since isPopupTrigger is platform dependent)
      if ( Tools.isWindows() )
        return;

      if ( e.isPopupTrigger() )
      {
        processPopupTrigger( e );
      }
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
      if ( !( e instanceof NeoMouseEvent ) )
        return;

      if ( e.getSource() != _detailMap )
        return;

      // Show the popup menu or set the zoom point ?
      // isPopupTrigger's behaviour is platform dependent

      if ( e.isPopupTrigger() )
      {
        processPopupTrigger( e );
      }
      else if ( SwingUtilities.isLeftMouseButton( e ) )
      {
        NeoMouseEvent nme = (NeoMouseEvent) e;

        if ( e.getClickCount() == 1 )
        {
          _zoomPoint.setSpot( nme.getCoordX() );
        }
        else if ( e.getClickCount() == 2 )
        {
          // Want to zoom in on selected component(s) when we double-click

          List<GlyphI> glyphs = nme.getItems();

          if ( glyphs != null && ! glyphs.isEmpty() )
          {
            boolean doZoom = false;
            double startIndex = Double.MAX_VALUE;
            double endIndex = 0;

            for ( GlyphI g : glyphs )
            {
              if ( g instanceof MapTierGlyph )
              {
                int index = _detailMap.getAllTiers().indexOf( g );

                // double-clicking in the coordinates or sequence tiers zooms out fully
                if ( index == _coordsTierParameters.getTierIndex() || index == _sequenceTierParameters.getTierIndex() )
                {
                  doZoom     = true;
                  startIndex = 0;
                  endIndex   = getSequenceLength();
                  break;
                }
                else
                {
                  continue;
                }
              }

              doZoom = true;

              startIndex = Math.min( startIndex, g.getCoordBox().x );
              endIndex = Math.max( endIndex, g.getCoordBox().x + g.getCoordBox().width );
            }

            if ( doZoom )
            {
              double margin = 0.02 * ( endIndex - startIndex );

              zoomTo( startIndex - margin, endIndex + margin );
              _zoomPoint.setSpot( ( startIndex + endIndex ) / 2 );
            }
          }
        }
      }
    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
      // Store the delays
      _savedDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
      _savedInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();

      // Don't want the tooltips to disappear
      ToolTipManager.sharedInstance().setDismissDelay( Integer.MAX_VALUE );

      // Popup the tooltips quickly
      ToolTipManager.sharedInstance().setInitialDelay( 100 );

      clearState();
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
      // Restore the delays
      ToolTipManager.sharedInstance().setDismissDelay( _savedDismissDelay );
      ToolTipManager.sharedInstance().setInitialDelay( _savedInitialDelay );

      // DON'T call clearState from here !
      // Moving from a glyph to the popup menu causes a MouseExit event which
      // will clear the state.
    }

    private void clearState()
    {
      _glyphUnderPointer = null;
      _detailMap.getNeoCanvas().setToolTipText( null );
    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
      // not interested in non-NeoMouseEvents
      if ( ! ( e instanceof NeoMouseEvent ) )
      {
        clearState();
        return;
      }

      NeoMouseEvent nme = (NeoMouseEvent) e;

      List< GlyphI > glyphs = nme.getItems();

      // Clear the glyph and tooltip, if the mouse pointer has moved away from a glyph
      //
      if ( glyphs == null || glyphs.isEmpty() )
      {
        clearState();
        return;
      }

      GlyphI newGlyphUnderPointer = null;
      SequenceRangeI sr = null;

      for ( GlyphI g : glyphs )
      {
        if ( g.getInfo() instanceof SequenceRangeI )
        {
          newGlyphUnderPointer = g;
          sr = (SequenceRangeI) g.getInfo();
          break;
        }
      }

      if ( newGlyphUnderPointer == null )
      {
        // The pointer has moved away from a glyph, so clear the state
        clearState();
      }
      else if ( newGlyphUnderPointer != _glyphUnderPointer )
      {
        // There's a new glyph under the pointer so update state accordingly
        _glyphUnderPointer = newGlyphUnderPointer;
        String tooltipText = VisSRSequenceHelper.formatHTML( sr, normalised );
        _detailMap.getNeoCanvas().setToolTipText( tooltipText );
      }
    }
  }

  /**
   * Mouse adapter for the label map
   */
  private final class LabelMouseAdapterImpl extends MouseAdapter
  {
    public LabelMouseAdapterImpl() {}

    @Override
    public void mousePressed( MouseEvent e )
    {
      // The label popup is not displayed for the PARESnip data, i.e. the colours and layout are set
      if ( _vissrMainFrame.getViewMode().isParesnip() )
        return;

      _labelGlyphPopupIndex = -1;

      if ( !( e instanceof NeoMouseEvent ) )
        return;

      if ( e.getSource() != _labelMap )
        return;

      if ( ! e.isPopupTrigger() )
        return;

      processPopupTrigger( (NeoMouseEvent) e );
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
      // The label popup is not displayed for the PARESnip data, i.e. the colours and layout are set
      if ( _vissrMainFrame.getViewMode().isParesnip() )
        return;

//      _labelGlyphPopupIndex = -1;

      if ( !( e instanceof NeoMouseEvent ) )
        return;

      if ( e.getSource() != _labelMap )
        return;

      if ( ! e.isPopupTrigger() )
        return;

      processPopupTrigger( (NeoMouseEvent) e );
    }

    private void processPopupTrigger( NeoMouseEvent nme )
    {
      List<GlyphI> labelTierGlyphs = nme.getItems();

      if ( labelTierGlyphs == null || labelTierGlyphs.isEmpty() )
        return;

      MapTierGlyph labelGlyph = null;

      for ( GlyphI g : labelTierGlyphs )
      {
        if ( g instanceof MapTierGlyph )
        {
          labelGlyph = (MapTierGlyph) g;
          break;
        }
      }

      if ( labelGlyph == null )
        return;

      _labelGlyphPopupIndex = _labelMap.getAllTiers().indexOf( labelGlyph );

      // So, we do want to show the popup...

      int tpIndex = getTierParametersIndex();
      int maxSpecialIndex = Math.max( _sequenceTierParameters.getTierIndex(), _coordsTierParameters.getTierIndex() );

      /*
       * Rules are:
       * o  Can't move the sequence or coordinates tiers up or down.
       * o  Can't delete the sequence or coordinates tiers.
       * o  Can't move the first tier user specified tier up
       * o  Can't move the last tier down
       * o  Can only set the glyph colour for user specified tiers
       */

      popupLabelMI_MoveUp.setEnabled( tpIndex - maxSpecialIndex >= 2 );
      popupLabelMI_MoveDown.setEnabled( tpIndex > maxSpecialIndex && tpIndex < _tierParameters.size() - 1 );
      popupLabelMI_SetGraphicColour.setEnabled( tpIndex > maxSpecialIndex );
      popupLabelMI_DeleteTier.setEnabled( tpIndex > maxSpecialIndex );

      popupLabel.show( nme.getComponent(), nme.getX(), nme.getY() );
    }
  }

  /**
   * Action for the 'move tier' popup menu item
   */
  private final class MoveTierAction extends AbstractAction
  {
    private final boolean _moveUp;

    public MoveTierAction( String name, boolean moveUp )
    {
      super( name );

      _moveUp = moveUp;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( _labelGlyphPopupIndex == -1 )
        return;

      int offset = _moveUp ? -1 : 1;

      // Do the tiers in the maps...
      //
      moveMapTierGlyph( _labelMap.getAllTiers(), _labelGlyphPopupIndex, _labelGlyphPopupIndex + offset );
      moveMapTierGlyph( _detailMap.getAllTiers(), _labelGlyphPopupIndex, _labelGlyphPopupIndex + offset );

      // Update the tier parameters collection
      //
      int tpIndex = getTierParametersIndex();

      if ( tpIndex > -1 )
      {
        TierParameters tp1 = _tierParameters.get( tpIndex );
        TierParameters tp2 = _tierParameters.get( tpIndex + offset );

        int idx1 = tp1.getTierIndex();
        int idx2 = tp2.getTierIndex();

        _tierParameters.set( tpIndex, tp2 );
        _tierParameters.set( tpIndex + offset, tp1 );

        tp1.setTierIndex( idx2 );
        tp2.setTierIndex( idx1 );
      }

      _doFullRepack = true;
      layoutNeoMap();
    }

    /**
     * Move a tier from one index to a new one.
     * There is a moveTier(...) method in the genoviz AbstractTieredMap which also
     * performs a repack etc., but we want control over the timing of the repack.
     *
     * @param l         List of tiers
     * @param fromIdx   Source index
     * @param toIdx     New index
     */
    @SuppressWarnings( "unchecked" )
    private void moveMapTierGlyph( List l, int fromIdx, int toIdx )
    {
      List<MapTierGlyph> tiers = (List<MapTierGlyph>) l;

      MapTierGlyph mtgLabel = tiers.get( _labelGlyphPopupIndex );
      tiers.remove( fromIdx );
      tiers.add( toIdx, mtgLabel );
    }
  }

  /**
   * Action for the 'remove tier' popup menu item
   */
  private final class RemoveTierAction extends AbstractAction
  {
    public RemoveTierAction( String name )
    {
      super( name );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( _labelGlyphPopupIndex == -1 )
        return;

      MapTierGlyph mtg = _labelMap.getTierAt( _labelGlyphPopupIndex );

      if ( mtg == null )
        return;

      String label = mtg.getLabel();

      int doDelete = JOptionPane.showConfirmDialog( VisSRDataTiersPanel.this,
        "Are you sure you want to remove the '" + label + "' tier ?",
        "Remove tier",
        JOptionPane.YES_NO_OPTION );

      if ( doDelete == JOptionPane.YES_OPTION )
      {
        _vissrMainFrame.setBusyCursor( true );

        try
        {
          performTierDeletion();
        }
        finally
        {
          _vissrMainFrame.setBusyCursor( false );
        }
      }
    }

    private void performTierDeletion()
    {
      // Remove the tier parameters

      int tpIndex = getTierParametersIndex();

      if ( tpIndex > -1 )
      {
        TierParameters tp = _tierParameters.get( tpIndex );
        tp.clear();

        _tierParameters.remove( tpIndex );
      }

      double startIndex = _detailMap.getViewBounds().x;
      double endIndex = startIndex + _detailMap.getViewBounds().width;

      // Perform a full reinitialise
      // NOTE: Removing the label and the data tiers individually caused layout issues
      //       which is why the heavy-handed 'reinitialise' method is called.
      reinitialise();

      zoomTo( startIndex, endIndex );
    }
  }

  /**
   * Action the for 'rename tier' popup menu item
   */
  private final class RenameTierAction extends AbstractAction
  {
    public RenameTierAction( String text )
    {
      super( text );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      MapTierGlyph labelTier = _labelMap.getTierAt( _labelGlyphPopupIndex );

      if ( labelTier == null )
        return;

      String label = labelTier.getLabel() == null ? "" : labelTier.getLabel();

      label = (String) JOptionPane.showInputDialog( VisSRDataTiersPanel.this,
        "Please enter a new label for the tier:",
        "Rename tier",
        JOptionPane.PLAIN_MESSAGE,
        null,
        null,
        label );

      // cancel clicked if result is null
      if ( label == null || label.equals( labelTier.getLabel() ) )
        return;

      // Store the change

      int tpIndex = getTierParametersIndex();

      if ( tpIndex > -1 )
      {
        TierParameters tp = _tierParameters.get( tpIndex );

        tp.setTierLabel( label );
      }

      // Update the view
      labelTier.setLabel( label );
      _labelMap.updateWidget();
    }
  }

  /**
   * Action for setting the background colour of the tier's label
   */
  private final class SetTierLabelBackgroundColourAction extends AbstractAction
  {
    public SetTierLabelBackgroundColourAction( String name )
    {
      super( name );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      MapTierGlyph mtg = _labelMap.getTierAt( _labelGlyphPopupIndex );

      if ( mtg == null )
        return;

      Color c = JColorChooser.showDialog( VisSRDataTiersPanel.this,
        "Label colour", mtg.getBackgroundColor() );

      if ( c == null || c.equals( mtg.getBackgroundColor() ) )
        return;

      // Store the change

      int tpIndex = getTierParametersIndex();

      if ( tpIndex > -1 )
      {
        TierParameters tp = _tierParameters.get( tpIndex );

        tp.setTierLabelBackgroundColour( c );
      }

      // Update the view
      mtg.setBackgroundColor( c );
      _labelMap.updateWidget();
    }
  }

  /**
   * Action for setting the colour of the tier's glyph
   */
  private final class SetTierGraphicColourAction extends AbstractAction
  {
    public SetTierGraphicColourAction( String name )
    {
      super( name );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      int index = getTierParametersIndex();

      if ( index < 0 || index >= _tierParameters.size() )
        return;

      TierParameters tp = _tierParameters.get( index );

      Color c = JColorChooser.showDialog( VisSRDataTiersPanel.this,
        "Graphic colour", tp.getGlyphBackgroundColour() );

      if ( c == null || tp.getGlyphBackgroundColour().equals( c ) )
        return;

      // Store the change

      tp.setGlyphBackgroundColour( c );

      // Update the view

      MapTierGlyph mtg = _detailMap.getTierAt( _labelGlyphPopupIndex );

      List<GlyphI> glyphs = mtg.getChildren();

      if ( glyphs != null )
      {
        for ( GlyphI g : glyphs )
        {
          g.setBackgroundColor( c );

          VisSRSequenceHelper.performGlyphColorSpecialisation( g );
        }
      }

      _detailMap.updateWidget();
    }
  }

  /**
   * Action for setting the colour of all of the tier labels
   */
  private final class SetTierLabelsColourAction extends AbstractAction
  {
    public SetTierLabelsColourAction( String name )
    {
      super( name );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      MapTierGlyph mtg = _labelMap.getTierAt( _labelGlyphPopupIndex );

      if ( mtg == null )
        return;

      Color c = mtg.getBackgroundColor();

      // Store the change

      for ( TierParameters tp : _tierParameters )
      {
        tp.setTierLabelBackgroundColour( c );
      }

      // Update the view

      for ( int i = 0; i < _labelMap.getTierCount(); ++i )
      {
        _labelMap.getTierAt( i ).setBackgroundColor( c );
      }

      _labelMap.updateWidget();
    }
  }
}
