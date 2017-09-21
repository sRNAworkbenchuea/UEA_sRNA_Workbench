/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.event.*;
import com.affymetrix.genoviz.glyph.*;
import com.affymetrix.genoviz.widget.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

/**
 * The overview panel for the VisSR main internal frame.
 * The panel contains a NeoMap which shows the whole of the governing sequence
 * together with a 'shadow' (or ' you are here ') rectangle which reflects the
 * area which is magnified and shown in the data tier area of the parent frame.
 *
 * Methods are private or package-private since the panel is specific to the
 * parent frame.
 *
 * @author prb07qmu
 */
public final class VisSRDataOverviewPanel extends javax.swing.JPanel
{
  private final SequenceVizMainFrame _vissrMainFrame;

  // Genoviz controls...
  private NeoMap _overviewMap = new NeoMap( false, false );
  private GlyphI _shadowRect = new OutlineRectGlyph();
  //...end of Genoviz controls

  /**
   * Constructor for the panel
   *
   * @param vissrMainFrame  The main VisSR frame
   */
  VisSRDataOverviewPanel( SequenceVizMainFrame vissrMainFrame )
  {
    _vissrMainFrame = vissrMainFrame;

    initComponents();
    initialiseOverviewMap();

    reinitialise();
  }

  /***
   * Reinitialise the overview map for a given sequence length.
   * The length of the map is slightly longer than the value given to slightly
   * improve the rendering.
   * The overview map is cleared first of all and then reconfigured.
   *
   * @param sequenceLength The length of the governing sequence
   */
  final void reinitialise()
  {
    // We take an extreme approach - the widget is cleared, before reinitialising...
    // clearWidget() sets a new scene glyph (copying some state) and removes axes and transients
    //
    _overviewMap.clearWidget();

    // Set the map ranges (making it a bit longer makes it look better)...
    //
    double end = 1.01 * _vissrMainFrame.getCurrentReferenceSequenceLength();
    _overviewMap.setMapRange( 0, (int) end );
    _overviewMap.setMapOffset( 0, 30 );

    AxisGlyph ag = _overviewMap.addAxis( 20 );
    ag.setBackgroundColor( this.getBackground() );
    ag.setForegroundColor( Color.white );

    _overviewMap.stretchToFit( true, false );

    // Configure the 'Overview' map which will show the whole sequence
    // and a 'you are here' rectangle.
    //
    _overviewMap.setBackground( this.getBackground() );
    _overviewMap.setForeground( Color.white );
    _overviewMap.setRubberBandBehavior( false );
    _overviewMap.setTransientOptimized( true ); // optimise for transient fast redraw

    configureShadowRectangleGlyph();
  }

  /**
   * Move the shadow rectangle to the specified coordinates.<br>
   * The validity of the parameters is assumed.
   *
   * @param newStart  The new left-hand side coordinate
   * @param newEnd    The new right-hand side coordinate
   */
  final void updateShadowRectangle( double newStart, double newEnd )
  {
    Rectangle2D.Double shadR = _shadowRect.getCoordBox();
    _shadowRect.setCoords( newStart, shadR.y, newEnd - newStart, shadR.height );

    _overviewMap.updateWidget();
  }

  /**
   * Return the shadow rectangle's coordinate box
   * @return The rectangle
   */
  final Rectangle2D.Double getShadowRectangleCoordBox()
  {
    return _shadowRect.getCoordBox();
  }

  //////////////////////////////////////////////////////////////////////////////
  
  /***
   * Add the overview map to the panel and make it mouse aware.
   */
  private void initialiseOverviewMap()
  {
    // Add the overview map
    //
    this.add( _overviewMap, BorderLayout.CENTER );
    _overviewMap.setBackground( this.getBackground() );
    _overviewMap.setForeground( Color.white );
    _overviewMap.setMapOffset( 0, 30 );

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

    // Make the 'overview' NeoMap mouse aware
    //
    VisSRDataOverviewMouseAdapter vdoma = new VisSRDataOverviewMouseAdapter();
    _overviewMap.addMouseListener( vdoma );
    _overviewMap.addMouseMotionListener( vdoma );
  }

  /***
   * Reinitialise the 'shadow' rectangle which shows the current location on the
   * governing sequence.
   * A {@link TransientGlyph} provides the bridge between the overview map and
   * the shadow rectangle.
   */
  private void configureShadowRectangleGlyph()
  {
    _shadowRect.setParent( null );
    _overviewMap.addItem( _shadowRect );

    Rectangle2D.Double sbox = _overviewMap.getScene().getCoordBox();
    double y = sbox.y + 2;
    double h = sbox.height - 4;

    _shadowRect.setCoords( 0, y, 10, h );
    _shadowRect.setColor( Color.red );
    _shadowRect.setSelectable( true );

    // Set up the transient glyph
    //
    TransientGlyph transientGlyph = new TransientGlyph();
    transientGlyph.setCoords( sbox.x, y, sbox.width, h );
    transientGlyph.addChild( _shadowRect );

    _overviewMap.getScene().addGlyph( transientGlyph ); // add to scene (special handling)
  }

  /** This method is called from within the constructor to
   * initialise the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 10), new java.awt.Dimension(5, 10), new java.awt.Dimension(5, 10));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 10), new java.awt.Dimension(5, 10), new java.awt.Dimension(5, 10));

        setBackground(new java.awt.Color(120, 120, 120));
        setForeground(java.awt.Color.white);
        setLayout(new java.awt.BorderLayout());
        add(filler1, java.awt.BorderLayout.LINE_START);
        add(filler2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    // End of variables declaration//GEN-END:variables

  //////////////////////////////////////////////////////////////////////////////

  private final class VisSRDataOverviewMouseAdapter extends MouseAdapter
  {
    private boolean _isShadowActive = false;
    private double _mouseOffsetFromRectLHS = 0;

    /***
     * Utility method to check whether the mouse event is a valid
     * genoviz mouse event.
     *
     * @param e The {@link MouseEvent}
     * @return A {@link NeoMouseEvent} or null if it is invalid
     */
    private NeoMouseEvent getNeoMouseEvent( MouseEvent e )
    {
      if ( ( e instanceof NeoMouseEvent ) && ( e.getSource() == _overviewMap ) )
      {
        return (NeoMouseEvent) e;
      }

      return null;
    }

    // MouseListener implementation...
    //
    /***
     * {@inheritDoc}
     */
    @Override
    public void mousePressed( MouseEvent e )
    {
      NeoMouseEvent nme = getNeoMouseEvent( e );

      if ( nme == null )
      {
        return;
      }

      boolean isInList = false;

      if ( nme.getItems() != null )
      {
        isInList = nme.getItems().contains( _shadowRect );
      }

      if ( isInList )
      {
        _isShadowActive = true;
        _mouseOffsetFromRectLHS = nme.getCoordX() - _shadowRect.getCoordBox().x;
      }
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased( MouseEvent e )
    {
      if ( getNeoMouseEvent( e ) == null )
      {
        return;
      }

      _isShadowActive = false;
    }
    //
    //...end of MouseListener implementation.

    // MouseMotionListener implementation...
    //
    /***
     * {@inheritDoc}
     * When the mouse-drag is valid the {@link VisSRDataOverviewI} implementation is notified.
     */
    @Override
    public void mouseDragged( MouseEvent e )
    {
      if ( !_isShadowActive )
      {
        return;
      }

      NeoMouseEvent nme = getNeoMouseEvent( e );

      if ( nme == null )
      {
        return;
      }

      // right, that's out of the way, try and move the rectangle

      double newShadowX = nme.getCoordX() - _mouseOffsetFromRectLHS;

      if ( ! isNewShadowWithinBounds( newShadowX ) )
      {
        return;
      }

      // Propagate the mouse dragged event upwards
      //
      Rectangle2D.Double rect = _shadowRect.getCoordBox();
      _vissrMainFrame.updateViewableRange( newShadowX, newShadowX + rect.width );
    }
    //
    //...end of MouseMotionListener implementation.

    /***
     * Check whether the new LHS coordinate is acceptable.
     *
     * @param newShadowLeftHandX  The new x-coordinate
     * @return
     */
    private boolean isNewShadowWithinBounds( double newShadowLeftHandX )
    {
      // Check the LHS of the rectangle
      //
      double doX = _overviewMap.getCoordBounds().x;

      if ( newShadowLeftHandX < doX )
      {
        return false;
      }

      // Check the RHS of the rectangle
      //
      double l = _vissrMainFrame.getCurrentReferenceSequenceLength();
      double w = _shadowRect.getCoordBox().width;

      return newShadowLeftHandX + w <= doX + l;
    }
  }
}
