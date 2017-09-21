/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

import com.affymetrix.genoviz.bioviews.*;

import java.awt.*;

/**
 *
 * @author prb07qmu
 */
public class LabelledVerticallyAlignedRectGlyph extends com.affymetrix.genoviz.glyph.SolidGlyph
{
  /*
   * Static state
   */

  private static final int MIN_PIXELBOX_WIDTH_FOR_LABEL = 32;

  private static boolean _drawLabels = true;

  public static boolean drawLabels()
  {
    return _drawLabels;
  }

  public static void setDrawLabels( boolean b )
  {
    _drawLabels = b;
  }

  /**
   * Class enum (to avoid an external reference) used for the sequence's strandedness
   */
  public enum VerticalAlignment
  {
    TOP,
    MIDDLE,
    BOTTOM
  };

  /*
   * Instance state
   */

  private VerticalAlignment _glyphAlignment = VerticalAlignment.BOTTOM;
  private String _label = null;
  private double _heightScale = 1;

  /*
   * Getters and setters
   */

  public VerticalAlignment getVerticalGlyphAlignment()
  {
    return _glyphAlignment;
  }

  public void setVerticalGlyphAlignment( VerticalAlignment glyphAlignment )
  {
    _glyphAlignment = glyphAlignment;
  }

  public String getLabel()
  {
    return _label;
  }

  public void setLabel( String label )
  {
    _label = label;
  }

  public double getHeightScale()
  {
    return _heightScale;
  }

  /**
   * Set the height scale
   * @param heightScale Value in the range [ 0, 1 ]
   */
  public void setHeightScale( double heightScale )
  {
    _heightScale = heightScale < 0 ? 0 : ( heightScale > 1 ? 1 : heightScale ) ;
  }

  /*
   * Other methods
   */

  private boolean wantToShowLabel()
  {
    return drawLabels() &&
      getLabel() != null &&
      MIN_PIXELBOX_WIDTH_FOR_LABEL < pixelbox.width;
  }

  /**
   * {@inheritDoc}
   */
  @Override
	public void draw( ViewI view )
  {
    Graphics2D g = view.getGraphics();

    view.transformToPixels( coordbox, pixelbox );

    // Really small => return
    //
    if ( pixelbox.width == 0 )
    {
      pixelbox.width = 1;
    }

    g.setColor( getBackgroundColor() );

    int rectH = rectH = (int) Math.max( _heightScale * pixelbox.height, 1.0 );
    int rectY = pixelbox.y; // TOP

    switch ( getVerticalGlyphAlignment() )
    {
      case BOTTOM:
        rectY = pixelbox.y + pixelbox.height - rectH;
        break;
      case MIDDLE:
        rectY = pixelbox.y + ( pixelbox.height - rectH ) / 2;
        break;
    }

    g.fillRect( pixelbox.x, rectY, pixelbox.width, rectH );

    if ( wantToShowLabel() )
    {
      GlyphHelper.addLabel( g, getLabel(), pixelbox, getForegroundColor() );
    }

    super.draw( view );
	}
}
