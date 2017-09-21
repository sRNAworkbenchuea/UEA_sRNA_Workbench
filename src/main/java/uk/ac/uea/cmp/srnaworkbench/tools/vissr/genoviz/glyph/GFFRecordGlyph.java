/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;

import com.affymetrix.genoviz.bioviews.*;

import java.awt.*;
import java.util.List;

/**
 *
 * @author prb07qmu
 */
public class GFFRecordGlyph extends com.affymetrix.genoviz.glyph.SolidGlyph implements DirectedLabelledGlyphI
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

  /*
   * Instance state
   */

  // Polygon for the arrow(s) (lazily initialised)
  private Polygon _poly = null;
  private Direction _glyphDirection = Direction.None;
  private String _label = null;

  private int _arrowX = 0;

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

    g.setColor( getBackgroundColor() );

    int rectStart = pixelbox.x;
    int rectWidth = pixelbox.width;

    _arrowX = -1;

    if ( reallyDrawLeftArrow() )
    {
      setPolygonPoints( false, getPolygon(), 0 );
      g.fillPolygon( getPolygon() );

      _arrowX = getPolygon().xpoints[0];

      rectStart += ARROW_LENGTH;
      rectWidth -= ARROW_LENGTH;
    }

    if ( reallyDrawRightArrow() )
    {
      setPolygonPoints( true, getPolygon(), 0 );
      g.fillPolygon( getPolygon() );

      _arrowX = getPolygon().xpoints[0];

      rectWidth -= ARROW_LENGTH;
    }

    g.fillRect( rectStart, pixelbox.y, rectWidth, pixelbox.height );

    // Draw the children if there's enough space
    if ( pixelbox.width > 2 * ARROW_LENGTH && hasGFFChildren() )
    {
      GFFRecord gffrec = getGFFRecord();

      List<GFFRecord> gffChildren = gffrec.getChildren();

      Color saveColor = g.getColor();

      Color otherColor = getBackgroundColor().darker();
      Color utrColor = getBackgroundColor().brighter();

      g.setColor( otherColor );
      g.drawLine( pixelbox.x + 1, pixelbox.y + pixelbox.height / 2, pixelbox.x + pixelbox.width - 2, pixelbox.y + pixelbox.height / 2);

      boolean hasUTRs = false;

      // Draw non-UTRs first
      //
      for ( GFFRecord gffChild : gffChildren )
      {
        if ( gffChild.isUTR() )
        {
          hasUTRs = true;
        }
        else
        {
          drawGFFChildGlyph( gffrec, gffChild, g );
        }
      }

      if ( hasUTRs )
      {
        // Draw UTRs last (since exons sometimes overlap UTRs which they shouldn't)
        //
        g.setColor( utrColor );
        for ( GFFRecord gffChild : gffChildren )
        {
          if ( gffChild.isUTR() )
            drawGFFChildGlyph( gffrec, gffChild, g );
        }
      }

      g.setColor( saveColor );
    }

    if ( wantToShowLabel() )
    {
      GlyphHelper.addLabel( g, getLabel(), pixelbox, getForegroundColor() );
    }

    super.draw( view );
	}

  private void setPolygonPoints( boolean toRight, Polygon p, int y_offset )
  {
    if ( toRight )
    {
      /* Triangle indices:
       *
       *   0
       *   |\
       *   | 1
       *   |/
       *   2
       */

      p.xpoints[0] = p.xpoints[2] = pixelbox.x + pixelbox.width - ARROW_LENGTH;
      p.xpoints[1] = pixelbox.x + pixelbox.width;

      p.ypoints[0] = pixelbox.y + y_offset;
      p.ypoints[1] = pixelbox.y + pixelbox.height / 2;
      p.ypoints[2] = pixelbox.y + pixelbox.height - y_offset;
    }
    else
    {
      /* Triangle indices:
       *
       *   0
       *  /|
       * 2 |
       *  \|
       *   1
       */

      p.xpoints[0] = p.xpoints[1] = pixelbox.x + ARROW_LENGTH;
      p.xpoints[2] = pixelbox.x;

      p.ypoints[0] = pixelbox.y + y_offset;
      p.ypoints[1] = pixelbox.y + pixelbox.height - y_offset;
      p.ypoints[2] = pixelbox.y + pixelbox.height / 2;
    }
  }

  private void drawGFFChildGlyph( GFFRecord parentGffRecord, GFFRecord gffChild, Graphics2D g )
  {
    final int Y_OFF = Math.max( 2, pixelbox.height / 5 );

    float pixels_per_base = (float)pixelbox.width / parentGffRecord.getSequenceLength();
    float gffpixX = pixelbox.x + pixels_per_base * ( gffChild.getStartIndex() - parentGffRecord.getStartIndex() );
    float gffpixW = pixels_per_base * gffChild.getSequenceLength();

    int x = (int)gffpixX;
    int w = (int)gffpixW;

    if ( _glyphDirection == Direction.Left && x < _arrowX )
    {
      // Draw triangle on left
      setPolygonPoints( false, getPolygon(), Y_OFF );
      g.fillPolygon( getPolygon() );

      // Adjust the x and w of the rectangle to ensure the glyph is drawn as contiguous regions
      // We do this because at certain zoom levels a vertical gap can appear (implement as a polygon with more points ?)
      x = x + ARROW_LENGTH - 1;
      w = w - ARROW_LENGTH + 1;

      g.fillRect( x, pixelbox.y + Y_OFF, w, pixelbox.height - 2 * Y_OFF );
    }
    else if ( _glyphDirection == Direction.Right && x + w > _arrowX )
    {
      // Draw triangle on right
      setPolygonPoints( true, getPolygon(), Y_OFF );
      g.fillPolygon( getPolygon() );

      // Adjust the width for the same reason as above, i.e. make sure rectangle and triangle are contiguous.
      w = w - ARROW_LENGTH + 1;

      g.fillRect( x, pixelbox.y + Y_OFF, w, pixelbox.height - 2 * Y_OFF ); // +1 to
    }
    else
    {
      g.fillRect( x, pixelbox.y + Y_OFF, w, pixelbox.height - 2 * Y_OFF );
    }
  }

  private boolean hasGFFChildren()
  {
    GFFRecord gffrec = getGFFRecord();

    if ( gffrec == null )
      return false;

    return gffrec.hasChildren();
  }

  private GFFRecord getGFFRecord()
  {
    if ( getInfo() instanceof GFFRecord )
    {
      return (GFFRecord)getInfo();
    }

    return null;
  }
}
