/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

import java.awt.*;

/**
 *
 * @author prb07qmu
 */
public class GlyphHelper
{
  // Avoid instantiation
  private GlyphHelper() {}

  /**
   * Draw a label in a pixelbox if there is sufficient room
   *
   * @param g
   * @param fm
   * @param label The label's text
   * @param pixelbox
   * @param color
   *
   * @return Whether the label was actually drawn
   */
  static boolean addLabel( Graphics2D g, String label, Rectangle pixelbox, Color color )
  {
    FontMetrics fm = g.getFontMetrics();
    int textWidth = fm.stringWidth( label );

    if ( textWidth < pixelbox.width )
    {
      int mid = pixelbox.x + ( pixelbox.width - textWidth ) / 2;
      int midline = pixelbox.y + pixelbox.height / 2;
      int adjust = (int)( ( fm.getAscent() - fm.getDescent() ) / 2 );

      g.setColor( color );
      g.drawString( label, mid, midline + adjust );

      return true;
    }

    return false;
  }
}
