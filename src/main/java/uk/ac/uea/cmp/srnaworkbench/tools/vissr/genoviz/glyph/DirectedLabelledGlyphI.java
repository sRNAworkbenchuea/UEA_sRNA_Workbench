/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

/**
 *
 * @author prb07qmu
 */
public interface DirectedLabelledGlyphI
{
  /**
   * Class enum (to avoid an external reference) used for the sequence's strandedness
   */
  public enum Direction
  {
    None,
    Left,
    Right;

    public static Direction valueOf( char c )
    {
      if ( c == '+' ) return Right;
      if ( c == '-' ) return Left;
      return None;
    }
  };

  public Direction getGlyphDirection();

  public void setGlyphDirection( Direction d );

  /**
   * Set the direction.
   * Pass '+' for L->R, '-' for R->L.
   * Anything else yields an unknown direction.
   *
   * @param c
   */
  public void setGlyphDirection( char c );

  public String getLabel();

  public void setLabel( String label );
}
