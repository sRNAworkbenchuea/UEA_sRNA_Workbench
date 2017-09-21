
package uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph;

import com.affymetrix.genoviz.bioviews.ViewI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.logging.Level;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * A glyph which displays its data as a series of striped rectangles.<br/>
 * The number of stripes is fixed at <b>four</b>.<br/>
 * The default colour for each stripe can be set.
 *
 * @author prb07qmu
 */
public class StripedSolidRectGlyph extends com.affymetrix.genoviz.glyph.SolidGlyph // full name specified to indicate we are extending across jars
{
  public static final int NUMBER_OF_STRIPES = 24;

  private static final Color[] DEFAULT_COLOURS = new Color[ NUMBER_OF_STRIPES ];

  static
  {
    DEFAULT_COLOURS[0] = Color.red.darker();
    DEFAULT_COLOURS[1] = Color.green.darker();
    DEFAULT_COLOURS[2] = new Color( 0xFF, 0x8C, 0 ); // orangey
    DEFAULT_COLOURS[3] = Color.blue.darker();
    DEFAULT_COLOURS[4] = Color.CYAN.darker();
    DEFAULT_COLOURS[5] = Color.GREEN.darker();
    DEFAULT_COLOURS[6] = Color.MAGENTA.darker();
    DEFAULT_COLOURS[7] = Color.YELLOW.darker();
    DEFAULT_COLOURS[8] = Color.PINK.darker();
//    DEFAULT_COLOURS[9] = Color.blue.darker();
//    DEFAULT_COLOURS[10] = Color.blue.darker();
//    DEFAULT_COLOURS[11] = Color.blue.darker();
//    DEFAULT_COLOURS[12] = Color.blue.darker();
//    DEFAULT_COLOURS[13] = Color.blue.darker();
//    DEFAULT_COLOURS[14] = Color.blue.darker();
//    DEFAULT_COLOURS[15] = Color.blue.darker();
//    DEFAULT_COLOURS[16] = Color.blue.darker();
//    DEFAULT_COLOURS[17] = Color.blue.darker();
//    DEFAULT_COLOURS[18] = Color.blue.darker();
//    DEFAULT_COLOURS[19] = Color.blue.darker();
//    DEFAULT_COLOURS[20] = Color.blue.darker();
//    DEFAULT_COLOURS[21] = Color.blue.darker();
//    DEFAULT_COLOURS[22] = Color.blue.darker();
//    DEFAULT_COLOURS[23] = Color.blue.darker();
    
  }

  private final Color[] _colours = new Color[ NUMBER_OF_STRIPES ];
  private final double[] _data      = new double[ NUMBER_OF_STRIPES ];
  private final int[] _widths    = new int[ NUMBER_OF_STRIPES ];

  private double _heightScaling = 1;

  private boolean _willDrawBorder = false;
  private Color _borderColour = Color.black;

  public StripedSolidRectGlyph()
  {
    super();

    for ( int i = 0; i < NUMBER_OF_STRIPES; i++ )
    {
      _colours[i] = DEFAULT_COLOURS[i];
      _data[i]    = 0;
      _widths[i]  = 0;
    }
  }

  public void setDataByIndex( int i, Color c, double value )
  {
    if ( i < 0 || i >= NUMBER_OF_STRIPES )
      throw new IllegalArgumentException( "Index must be 0 to 3 - received " + i );

    _colours[ i ] = c;
    _data[ i ]    = value;
  }

  /**
   * Set the height scaling
   * @param heightScaling A value in the range [ 0, 1 ]
   */
  public void setHeightScaling( double heightScaling )
  {
    _heightScaling = heightScaling;
  }

  /***
   * Get the colour of the line border
   * @return The colour
   */
  public Color getBorderColour()
  {
    return _borderColour;
  }

  /***
   * Set the colour of the line border
   * @param borderColour The colour to use
   */
  public void setBorderColour( Color borderColour )
  {
    _borderColour = borderColour;
  }

  /***
   * Find out whether the border will be drawn
   * @return Boolean
   */
  public boolean willDrawBorder()
  {
    return _willDrawBorder;
  }

  /***
   * Set whether or not the border will be drawn
   * @param b
   */
  public void setWillDrawBorder( boolean b )
  {
    _willDrawBorder = b;
  }

  /***
   * Draw the glyph.
   * If the glyph is really small then return ASAP.
   * The glyph will be drawn as a single-coloured rectangle unless {@code getInfo()}
   * returns the String "test" or a {@link SequenceWindow}.
   * Both cases result in calling the same drawing code but the latter case uses
   * the length to abundance data stored in the {@link SequenceWindow} object.
   *
   * @param view A view object which implements {@link ViewI}.
   */
  @Override
	public void draw(ViewI view)
  {
    Graphics2D g = view.getGraphics();

    view.transformToPixels( coordbox, pixelbox );

    // Really small => return
    //
    if ( pixelbox.width == 0 )
    {
      super.draw( view );

      return;
    }

    // Really small => draw v. simple gray rectangle and return
    //
    if ( pixelbox.width < 3 || super.getInfo() == null )
    {
      g.setColor( Color.darkGray );
      g.fillRect( pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height / 2 );

      super.draw( view );

      return;
    }

    drawData( g );

    // draw the black border rectangle (if desired)
    //
    if ( _willDrawBorder )
    {
      g.setColor( _borderColour );
      g.drawRect( pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height );
    }

		super.draw(view);
	}

  /***
   * Draws the striped rectangle with the width of stripe {@code i} proportional
   * to the relative size {@code data[i]}.<br>
   * <br>
   * NOTE: The start and end of the glyph will be positioned correctly with respect
   *       to the sequence but the borders between colours will not because the length
   *       of a coloured band corresponds to a proportion and not to a particular
   *       part of the sequence
   *
   * @param g A graphics object
   * @param data Integer array
   * @param scaling Multiplication factor which governs the height of the overall box
   */
  private void drawData( Graphics2D g )
  {
    int x = pixelbox.x;
    double pxwidth = pixelbox.width;
    int y = pixelbox.y;
    int h = (int)Math.max( 1.0, Math.min( (double)pixelbox.height, 0.5 + _heightScaling * pixelbox.height ) );

    int totalwidth = 0;
    int i;

    double sum = 0;
    for ( i = 0; i < NUMBER_OF_STRIPES; ++i )
    {
      sum += _data[i];
    }

    // Calculate the widths of the bands
    //
    for ( i = 0; i < NUMBER_OF_STRIPES; ++i )
    {
      // convoluted operation but avoids a silent overflow (e.g. width * data[i])
      
      _widths[i] = (int) ((pxwidth / sum )  * _data[i] );

      if ( _widths[i] == 0 && _data[i] != 0 )
      {
        _widths[i] = 1;
      }

      totalwidth += _widths[i];
    }

    // Make sure the total width matches the total of the widths
    //
    // TODO: Make sure the calculated total width matches the width of the pixel box
    //
    if ( false && totalwidth != pxwidth && pxwidth > 1 )
    {
      boolean decrease = ( totalwidth > pxwidth );
      int dw = ( decrease ? -1 : 1 );
      i = 0;

      LOGGER.log( Level.INFO, "totalwidth={0}, width={1}, dw={2}", new Object[]{ totalwidth, pxwidth, dw });

      while ( totalwidth != pxwidth )
      {
        if ( i < NUMBER_OF_STRIPES )
        {
          LOGGER.log( Level.INFO, "i={0}, widths[i]={1}, totalwidth={2}", new Object[]{ i, _widths[i], totalwidth });
        }

        boolean madeChange = false;

        for( i = 0; i < NUMBER_OF_STRIPES; ++i )
        {
          // Don't want to decrease 1 or increase 0
          if ( _widths[i] > (decrease ? 1 : 0) )
          {
            _widths[i] += dw;
            totalwidth += dw;
            madeChange = true;
          }
        }

        if ( ! madeChange )
          break;
      }
    }

    for ( i = 0; i < NUMBER_OF_STRIPES; ++i )
    {
      g.setColor( _colours[i] );
      g.fillRect( x, y, _widths[i], h );
      x += _widths[i];
    }
  }
}
