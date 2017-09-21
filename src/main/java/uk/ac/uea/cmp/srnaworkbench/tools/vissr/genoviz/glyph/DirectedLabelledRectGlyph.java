/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

import com.affymetrix.genoviz.bioviews.*;

import java.awt.*;

/**
 * Class for a rectangular glyph which can have arrows at none, one or both ends.
 * A text label can be specified and it will be displayed, if required, and there
 * is sufficient space within the glyph to display it.
 *
 * This class combines elements from several glyphs in the com.affymetrix.genoviz.glyph package
 *
 * @author prb07qmu
 */
public class DirectedLabelledRectGlyph extends com.affymetrix.genoviz.glyph.SolidGlyph implements DirectedLabelledGlyphI
{
  /*
   * Static state
   */

  private static final int MIN_PIXELBOX_WIDTH_FOR_LABEL = 32;
  private static final int ARROW_LENGTH = 6;

  private static boolean _drawLabels = true;

  public static boolean drawLabels()
  {
    return _drawLabels;
  }

  public static void setDrawLabels( boolean b )
  {
    _drawLabels = b;
  }

  // Polygon for the arrow(s) (lazily initialised)
  private Polygon _poly = null;
  private Direction _glyphDirection = Direction.None;
  private String _label = null;

  // DirectedLabelledGlyphI implementation
  //
  @Override
  public Direction getGlyphDirection()
  {
    return _glyphDirection;
  }

  @Override
  public void setGlyphDirection( Direction d )
  {
    _glyphDirection = d;
  }

  @Override
  public void setGlyphDirection( char c )
  {
    _glyphDirection = Direction.valueOf( c );
  }

  @Override
  public String getLabel()
  {
    return _label;
  }

  @Override
  public void setLabel( String label )
  {
    _label = label;
  }
  //
  // End of DirectedLabelledGlyphI implementation

  /*
   * Other methods
   */

  /**
   * Get the polygon - lazy initialised since it is only relevant to glyphs with direction
   *
   * @return A {@link Polygon}
   */
  private Polygon getPolygon()
  {
    if ( _poly == null )
    {
      _poly = new Polygon();
      _poly.addPoint( 0, 0 );
      _poly.addPoint( 0, 0 );
      _poly.addPoint( 0, 0 );
    }

    return _poly;
  }

  private boolean reallyDrawLeftArrow()
  {
    return _glyphDirection == Direction.Left && pixelbox.width > ARROW_LENGTH;
  }

  private boolean reallyDrawRightArrow()
  {
    return _glyphDirection == Direction.Right && pixelbox.width > ARROW_LENGTH;
  }

  private boolean wantToShowLabel()
  {
    return DirectedLabelledRectGlyph.drawLabels() &&
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

    if ( pixelbox.height == 0 )
    {
      pixelbox.height = 1;
    }

    g.setColor( getBackgroundColor() );

    int rectStart = pixelbox.x;
    int rectWidth = pixelbox.width;

    if ( pixelbox.height > 2 && reallyDrawLeftArrow() )
    {
      // Start top-left and go clockwise around the triangle

      Polygon p = getPolygon();

      p.xpoints[0] = p.xpoints[1] = pixelbox.x + ARROW_LENGTH;
      p.xpoints[2] = pixelbox.x;

      p.ypoints[0] = pixelbox.y;
      p.ypoints[1] = pixelbox.y + pixelbox.height;
      p.ypoints[2] = pixelbox.y + pixelbox.height / 2;

      g.fillPolygon( p );

      rectStart += ARROW_LENGTH;
      rectWidth -= ARROW_LENGTH;
    }

    if ( pixelbox.height > 2 && reallyDrawRightArrow() )
    {
      // Start top-left and go clockwise around the triangle

      Polygon p = getPolygon();

      p.xpoints[0] = p.xpoints[2] = pixelbox.x + pixelbox.width - ARROW_LENGTH;
      p.xpoints[1] = pixelbox.x + pixelbox.width;

      p.ypoints[0] = pixelbox.y;
      p.ypoints[1] = pixelbox.y + pixelbox.height / 2;
      p.ypoints[2] = pixelbox.y + pixelbox.height;

      g.fillPolygon( p );

      rectWidth -= ARROW_LENGTH;
    }

    g.fillRect( rectStart, pixelbox.y, rectWidth, pixelbox.height );

    if ( wantToShowLabel() )
    {
      GlyphHelper.addLabel( g, getLabel(), pixelbox, getForegroundColor() );
    }

    super.draw( view );
	}
}
