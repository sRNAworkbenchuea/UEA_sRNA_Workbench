/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Draw the T-plot, i.e. plot abundance (y-axis) vs. nucleotide position (x-axis).
 * The axes are scaled accordingly. The degradome hits are represented by blue
 * circles and the sRNA hits by differently coloured squares.
 * The colour corresponds to the category of the hit.
 * A list of the sRNA hits is displayed to the right of the plot. Each one has an
 * integer index which is used to label the points in the plot.
 *
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 * @author prb07qmu
 */
public final class TPlotCanvas extends Component
{
  /** The landscape width of an A4 page in points. **/
  static final int A4_WIDTH_POINTS = 842;

  /** The landscape height of an A4 page in points. **/
  static final int A4_HEIGHT_POINTS = 595;

  /** The dimension of an A4 page in landscape in points. **/
  static final Dimension MASTER_DIMENSION = new Dimension( A4_WIDTH_POINTS, A4_HEIGHT_POINTS );


  /** Small mono space font of size 6. **/
  private static final Font FONT_SMALLEST = new Font( Font.MONOSPACED, Font.PLAIN, 6 );

  /** Small mono space font of size 10. **/
  private static final Font FONT_SMALL = new Font( Font.MONOSPACED, Font.PLAIN, 10 );

  /** Small mono space font of size 12. **/
  private static final Font FONT_TITLE = new Font( Font.MONOSPACED, Font.PLAIN, 12 );

  /** Used for dashed lines */
  private static final Stroke DASHED_STROKE = new BasicStroke( 0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2.5f}, 0 );

  /** Diameter of the circles used for degradome hits */
  private static final int CIRCLE_DIAMETER = 4;

  /** Height/width of the box used for the small RNA hits */
  private static final int BOX_SIZE = 4;

  /** Length of the x/y tic */
  private static final int TIC_LENGTH = 6;

  /** The origin of the axes, rather than the origin of component space which is top-left */
  private static final Point2D.Float AXES_ORIGIN = new Point2D.Float( 60f, 520f );

  private static final Point2D.Float X_AXIS_END = new Point2D.Float( 580f, AXES_ORIGIN.y );
  private static final Point2D.Float Y_AXIS_END = new Point2D.Float( AXES_ORIGIN.x, 50f );

  private static final Point2D.Float SEPARATOR_TOP    = new Point2D.Float( X_AXIS_END.x + 15, Y_AXIS_END.y - 10 );
  private static final Point2D.Float SEPARATOR_BOTTOM = new Point2D.Float( SEPARATOR_TOP.x, A4_HEIGHT_POINTS - 10 );

  /** X Point Multiplier - scales position to coordinate space */
  private float _positionScale = 1;

  /** The position increment value for the x-axis */
  private int _positionIncrement = 100;

  /** Y Point Multiplier - scales abundance to coordinate space */
  private float _abundanceScale = 1;

  /** The abundance increment value for the y-axis */
  private int _abundanceIncrement;

  private boolean debug = AppUtils.INSTANCE.getShowDevFeatures();


  private PlotRecord _pr;
  private List< TPlotPoint > _tplotPoints;

  enum DisplayableItem
  {
    DEGRADOME_HIT,
    LABEL,
    CAT_0 { @Override Category getCategory() { return Category.CATEGORY_0; } },
    CAT_1 { @Override Category getCategory() { return Category.CATEGORY_1; } },
    CAT_2 { @Override Category getCategory() { return Category.CATEGORY_2; } },
    CAT_3 { @Override Category getCategory() { return Category.CATEGORY_3; } },
    CAT_4 { @Override Category getCategory() { return Category.CATEGORY_4; } };

    Category getCategory() { return Category.UNDEFINED; }
  };

  private final Set< DisplayableItem > _displayableItems = EnumSet.allOf( DisplayableItem.class );

  private final Set< Category > _displayableCategories = EnumSet.range( Category.CATEGORY_0, Category.CATEGORY_4 );

  /**
   * Sorted map with lowest position first.
   *
   * Keys are : position, abundance (actually the 'int bits' of the float value)
   * Value is : a TPlotPoint
   */
  private final TwoKeyMap< Integer, Integer, TPlotPoint > _positionAbundanceHitMap = TwoKeyMap.createTreeMap();

  private Graphics2D _g2d = null;
  private boolean _inDoubleBufferedMode = false;

  /**
   * Create a new instance of my canvas.
   */
  TPlotCanvas()
  {
    this( false );
  }

  TPlotCanvas( boolean useBufferedImageGraphics )
  {
    super();

    setPreferredSize( MASTER_DIMENSION );
    setMinimumSize( MASTER_DIMENSION );
    setMaximumSize( MASTER_DIMENSION );

    setBackground( Color.white );

    if ( useBufferedImageGraphics )
    {
      _inDoubleBufferedMode = true;

      Dimension d = MASTER_DIMENSION;

      BufferedImage bi = new BufferedImage( d.width, d.height, BufferedImage.TYPE_INT_RGB );
      _g2d = bi.createGraphics();
    }
  }

  void dispose()
  {
    clearState();

    if ( _g2d != null )
    {
      _g2d.dispose();
      _g2d = null;
    }
  }

  void clearState()
  {
    _pr = null;

    if ( _tplotPoints != null )
    {
      _tplotPoints.clear();
      _tplotPoints = null;
    }

    _positionAbundanceHitMap.clear();
  }

  void displayPlotRecord( PlotRecord pr )
  {
    clearState();

    _pr = pr;

    setIntervalsXAxis();
    setIntervalsYAxis();

    _tplotPoints = TPlotPoint.wrap( pr );

    // Put the sRNA hits into the (position, abundance) map
    //
    for ( TPlotPoint ht : _tplotPoints )
    {
      if ( ht.hasSrnaHit() )
      {
        // Only want to label the sRNA hits
        _positionAbundanceHitMap.put( ht.getPosition(), Float.floatToIntBits( ht.getAbundance() ), ht );
      }
    }

    // show it...
    if ( _inDoubleBufferedMode )
    {
      paintTPlot( _g2d );
    }
    else
    {
      paintTPlot( (Graphics2D)getGraphics() );
    }
  }

  void addDisplayableItem( DisplayableItem displayableItem )
  {
    _displayableItems.add( displayableItem );

    if ( displayableItem.getCategory() != Category.UNDEFINED )
    {
      _displayableCategories.add( displayableItem.getCategory() );
    }

    repaint();
  }

  void removeDisplayableItem( DisplayableItem displayableItem )
  {
    _displayableItems.remove( displayableItem );

    if ( displayableItem.getCategory() != Category.UNDEFINED )
    {
      _displayableCategories.remove( displayableItem.getCategory() );
    }

    repaint();
  }

  private int _componentCount = 0;
  private int _paintCount = 0;

  @Override
  public void paint( Graphics graphics )
  {
    paintTPlot( (Graphics2D) graphics );
  }

  private void paintTPlot( Graphics2D g )
  {
    // Clear
    g.setColor( getBackground() );
    g.fillRect( 0, 0, getWidth(), getHeight() );

    /*
     * To zoom in/out we need something along these lines...
     *
     * AffineTransform at = g.getTransform();
     * g.scale( _scale, _scale );
     *
     * ...but we also need to use a ScrollPane and relax the hard-coded A4-dimension
     */

    if ( _pr != null )
    {
      g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

      drawTitle( g );
      drawSeparator( g );
      drawKey( g );

      // Draw in this order so that the topmost layer has the most significant item, e.g. draw small-RNA hits last

      paintLabels( g );

      paintMedianLine( g );

      paintXAxis( g );
      paintYAxis( g );

      paintDegradomeHits( g );
      paintSmallRNAHits( g );
    }

    if ( debug )
    {
      // show the rectangular workspace
      g.setColor( Color.red );
      g.drawRect( 0, 0, A4_WIDTH_POINTS-1, A4_HEIGHT_POINTS-1 );
    }

    // g.setTransform( at );
  }

  private void paintLabels( Graphics2D g )
  {
    if ( ! _displayableItems.contains( DisplayableItem.LABEL ) )
      return;

    g.setFont( FONT_SMALLEST );

    final int Y_INC = g.getFontMetrics().getHeight();
    final float LABEL_DISTANCE = 4 * Y_INC;

    // These are for the line
    float x, y, y_end, x_prev = 0, y_prev = 0;

    // These are for the label
    float x_label, y_label, y_label_prev = 0;

    for ( Integer position : _positionAbundanceHitMap.getKey1Set() )
    {
      x = AXES_ORIGIN.x + _positionScale * position.floatValue();

      Map< Integer, TPlotPoint > innerMap = _positionAbundanceHitMap.getInnerMap( position );

      if ( innerMap.isEmpty() )
        continue;

      for ( Integer abundanceBits : innerMap.keySet() )
      {
        TPlotPoint ht = innerMap.get( abundanceBits );

        if ( ! _displayableCategories.contains( ht.getCategory() ) )
          continue;

        float abundance = Float.intBitsToFloat( abundanceBits );

        // y is the position of the hit and the start of the line to the label
        y = AXES_ORIGIN.y - _abundanceScale * abundance;

        // Calculate x-position of the label
        //
        String annotation = ht.getLabel();
        g.setColor( Color.black );

        int stringWidth = g.getFontMetrics().stringWidth( annotation );
        x_label = Math.max( x + 3 - stringWidth, AXES_ORIGIN.x + 1 );

        // y-position of the line end and label is trickier !
        //
        // First : use default distance
        y_end = y - LABEL_DISTANCE;

        // Second : If the new label will overlap the previous one then check the y-coordinate
        //
        if ( x_label + 1 < x_prev )
        {
          // Only adjust the y-position if it's close to the previous one
          if ( Math.abs( y - y_prev ) < 2 * Y_INC )
          {
            // In this case use the position of the previous label
            y_end = y_label_prev - Y_INC;
          }

          // Don't want the line to keep growing...
          if ( Math.abs( y - y_end ) > 0.5f * ( AXES_ORIGIN.y - Y_AXIS_END.y ) )
          {
            y_end = y - LABEL_DISTANCE;
          }
        }

        y_label = y_end;

        // Draw the line to the label
        //
        Stroke stroke = g.getStroke();
        g.setColor( Color.lightGray );
        g.setStroke( DASHED_STROKE );
        g.draw( new Line2D.Float( x, y, x, y_end ) );
        g.setStroke( stroke );

        // Draw the label
        //
        g.drawString( annotation, x_label, y_label );

        x_prev       = x_label + stringWidth;
        y_prev       = y;
        y_label_prev = y_label;
      }
    }
  }

  /**
   * Paint the x axis and its intervals.
   * @param g
   */
  private void paintXAxis( Graphics2D g )
  {
    // The axis
    //
    g.setColor( Color.black );
    g.draw( new Line2D.Float( AXES_ORIGIN, X_AXIS_END ) );

    /*
     * Draw the tics (plus an extra one to show the gene's length)
     */

    // Draw an extra tic to show the gene-length

    // Draw a vertical dashed line
    Stroke stroke = g.getStroke();
    g.setStroke( DASHED_STROKE );
    float x = AXES_ORIGIN.x + _positionScale * _pr.getGeneLength();
    g.draw( new Line2D.Float( x, AXES_ORIGIN.y, x, AXES_ORIGIN.y + 4 * TIC_LENGTH ) );
    g.setStroke( stroke );

    // Draw the text
    g.setFont( FONT_SMALLEST );
    FontMetrics fm = g.getFontMetrics();
    final int FONT_HEIGHT = fm.getHeight();

    String s = "Transcript length: " + _pr.getGeneLength();
    int stringWidth = fm.stringWidth( s );
    g.drawString( s, x - stringWidth + 3, AXES_ORIGIN.y + 4 * TIC_LENGTH + FONT_HEIGHT );

    // Draw the axis tics
    //
    int position = 0;
    x = AXES_ORIGIN.x;
    final float X_INCREMENT = _positionScale * _positionIncrement;

    while ( position < _pr.getGeneLength() )
    {
      // Draw the vertical line
      g.draw( new Line2D.Float( x, AXES_ORIGIN.y, x, AXES_ORIGIN.y + TIC_LENGTH ) );

      // Draw the value
      s = Integer.toString( position );
      stringWidth = fm.stringWidth( s );
      g.drawString( s, x - stringWidth / 2, AXES_ORIGIN.y + TIC_LENGTH + FONT_HEIGHT );

      // Increment user space
      position += _positionIncrement;

      // Increment coordinate space
      x += X_INCREMENT;
    }

    // Draw the axis label
    //
    g.setFont( FONT_SMALL );
    fm = g.getFontMetrics();

    final String X_LABEL = "POSITION";
    int labelWidth = fm.stringWidth( X_LABEL );

    g.drawString( X_LABEL, ( X_AXIS_END.x + AXES_ORIGIN.x - labelWidth ) / 2, AXES_ORIGIN.y + 40 );
  }

  /**
   * Paints the y axis and its intervals.
   * @param g
   */
  private void paintYAxis( Graphics2D g )
  {
    g.setColor( Color.black );
    g.draw( new Line2D.Float( AXES_ORIGIN, Y_AXIS_END ) );

    // Draw the tics
    //
    g.setFont( FONT_SMALLEST );
    FontMetrics fm = g.getFontMetrics();

    float y = AXES_ORIGIN.y;
    final float Y_INCREMENT = _abundanceScale * _abundanceIncrement;
    final int ASCENT = fm.getAscent();

    int abundance = 0;

    while ( Math.round( y - Y_AXIS_END.y ) >= 0 )
    {
      // Draw the horizontal line
      g.draw( new Line2D.Float( AXES_ORIGIN.x - TIC_LENGTH, y, AXES_ORIGIN.x, y ) );

      // Draw the value
      String s = Integer.toString( abundance );
      int stringLength = fm.stringWidth( s );
      g.drawString( s, ( AXES_ORIGIN.x - TIC_LENGTH - 2 ) - stringLength, y + ASCENT / 2 - 1 );

      // Increment user space
      abundance += _abundanceIncrement;

      // Increment coordinate space
      y -= Y_INCREMENT;
    }

    // Draw the y-axis label (rotated through 90 degrees)
    //
    g.setFont( FONT_SMALL );
    AffineTransform at = g.getTransform();

    g.rotate( -Math.PI / 2 );

    final String label = "ABUNDANCE";
    int len = g.getFontMetrics().stringWidth( label );
    g.drawString( label, -( AXES_ORIGIN.y + Y_AXIS_END.y + len ) / 2, 24 );

    g.setTransform( at );
  }

  private void paintMedianLine( Graphics2D g )
  {
    g.setColor( Color.lightGray );

    float y = AXES_ORIGIN.y - _abundanceScale * _pr.getMedianDegradomeHitAbundance();

    g.draw( new Line2D.Float( AXES_ORIGIN.x - 3, y, X_AXIS_END.x + 3, y ) );
  }

  private void paintDegradomeHits( Graphics2D g )
  {
    if ( ! _displayableItems.contains( DisplayableItem.DEGRADOME_HIT ) )
      return;

    g.setColor( Color.blue );

    for ( PlotRecord.DegradomeHit dh : _pr.getDegradomeHits() )
    {
      float x = AXES_ORIGIN.x + _positionScale * dh.getPosition();
      float y = AXES_ORIGIN.y - _abundanceScale * dh.getAbundance();

      paintDegradomeHit( g, x, y );
    }
  }

  private void paintDegradomeHit( Graphics2D g, float x, float y )
  {
    g.setColor( Color.blue );
    g.fill( new Ellipse2D.Float( x - CIRCLE_DIAMETER / 2, y - CIRCLE_DIAMETER / 2, CIRCLE_DIAMETER, CIRCLE_DIAMETER ) );
  }

  /**
   * Print the plot points for each of the hits.
   */
  private void paintSmallRNAHits( Graphics2D g )
  {
    final int margin = 10;
    final int offsetIncrement = 6;

    g.setFont( FONT_SMALLEST );

    final float LIST_MARGIN_X = SEPARATOR_TOP.x + margin / 2;
    final float LIST_TOP      = Y_AXIS_END.y + 3 * g.getFontMetrics().getHeight();

    int offset = 0;

    boolean truncate = false;

    // Make sure the most significant category (i.e. 0) is painted last so that it's on top
    //
    for ( int catInt = Category.CATEGORY_4.getCategoryValue(); catInt >= Category.CATEGORY_0.getCategoryValue(); --catInt )
    {
      Category cat = Category.getCategory( catInt );
      if ( ! _displayableCategories.contains( cat ) )
        continue;

      for ( TPlotPoint ht : _tplotPoints )
      {
        if ( ht.getCategory().getCategoryValue() == catInt )
        {
          float x = AXES_ORIGIN.x + _positionScale * ht.getPosition();
          float y = AXES_ORIGIN.y - _abundanceScale * ht.getAbundance();

          paintSmallRNAHit( g, ht.getCategory(), x, y );
        }
      }
    }

    // List the small RNA hits in a list on the right
    //
    List< PlotRecord.SmallRNAHit > sRNAHits = _pr.getSmallRNAHits();

    for ( PlotRecord.SmallRNAHit sh : sRNAHits )
    {
      // Draw the box in the list
      //
      if ( truncate )
      {
        g.setColor( Color.gray );

        Line2D.Float line = new Line2D.Float( LIST_MARGIN_X, A4_HEIGHT_POINTS - 15, A4_WIDTH_POINTS - 10, A4_HEIGHT_POINTS - 15 );
        g.draw( line );

        String msg = String.format( "List truncated - shown %d of %d", sh.getIndex()-1, sRNAHits.size() );

        float x = 0.5f * ( line.x1 + line.x2 ) - g.getFontMetrics().stringWidth( msg ) / 2;

        g.drawString( msg, x, A4_HEIGHT_POINTS - 7 );

        break;
      }
      else
      {
        if ( ! _displayableCategories.contains( sh.getCategory() ) )
          continue;

        // Draw the coloured box
        paintSmallRNAHit( g, sh.getCategory(), LIST_MARGIN_X, LIST_TOP + offset - 2 );

        // Draw the category number; 0,1 etc.
        g.setColor( Color.black );
        g.drawString( "" + sh.getCategory().getCategoryValue(), LIST_MARGIN_X + BOX_SIZE, LIST_TOP + offset - 2 + BOX_SIZE / 2 );

        float textX = LIST_MARGIN_X + BOX_SIZE + 10;

        PlotRecord.DegradomeHit dh = _pr.getDegradomeHitForPosition( sh.getPosition() );
        String abundanceStr = ( dh == null ? "Unknown" : FormattingUtils.format( "#0.00", dh.getAbundance() ) );

        String s1 = String.format( "#%d  Position:%d  Abundance: %s(deg) %d(sRNA)", sh.getIndex(), sh.getPosition(), abundanceStr, sh.getAbundance() );
        g.drawString( s1, textX, LIST_TOP + offset );
        offset += offsetIncrement;

        String s2 = sh.getAlignmentSRNA() + "  ID:" + sh.getMirbaseId();
        g.drawString( s2, textX, LIST_TOP + offset );
        offset += offsetIncrement;

        String s3 = sh.getAlignmentBars() + "     Score: " + sh.getScore();
        g.drawString( s3, textX, LIST_TOP + offset );
        offset += offsetIncrement;

        String s4 = sh.getAlignmentMRNA() + "  p-value: " + ( sh.getPValue() == PlotRecord.UNKNOWN_PVALUE ? "N/A" : sh.getPValue() );
        g.drawString( s4, textX, LIST_TOP + offset );
        offset += offsetIncrement * 2;

        if ( LIST_TOP + offset >= A4_HEIGHT_POINTS - 20 )
        {
          truncate = true;
        }
      }
    }
  }

  private void paintSmallRNAHit( Graphics2D g, Category cat, float x, float y )
  {
    g.setColor( cat.getCategoryColor() );
    g.fill( new Rectangle2D.Float( x - BOX_SIZE / 2, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE ) );
  }

  private void drawTitle( Graphics2D g )
  {
    g.setFont( FONT_TITLE );
    g.setColor( Color.black );

    FontMetrics fm = g.getFontMetrics();

    String tplotTitle = _pr.getGeneId();

    int len = fm.stringWidth( tplotTitle );

    if ( len > A4_WIDTH_POINTS )
    {
      // The title is wider than the page so truncate it...
      //
      while ( tplotTitle.length() > 20 )
      {
        tplotTitle = tplotTitle.substring( 0, tplotTitle.length() - 10 );

        len = fm.stringWidth( tplotTitle );
        if ( len < A4_WIDTH_POINTS )
          break;
      }

      // and add a message to say that we've truncated it

      final String TRUNC_MSG = " (truncated)";

      if ( tplotTitle.length() > TRUNC_MSG.length() )
      {
        tplotTitle = tplotTitle.substring( 0, tplotTitle.length() - TRUNC_MSG.length() ) + TRUNC_MSG;
      }
    }

    g.drawString( tplotTitle, ( A4_WIDTH_POINTS - len ) / 2, 25 );
  }

  private void drawSeparator( Graphics2D g )
  {
    // Draw vertical separator line to show separation between the plot and the key.
    g.setColor( Color.gray );
    g.draw( new Line2D.Float( SEPARATOR_BOTTOM, SEPARATOR_TOP ) );
  }

  private void drawKey( Graphics2D g )
  {
    // Draw the key showing the category dots/squares

    final String CATEGORY  = "Category:";
    final String DEGRADOME_ALIGNMENT = "Degradome alignment: ";
    final String MEDIAN = "Median: ";

    final int PADDING = 10;

    final float LEFT = SEPARATOR_TOP.x + PADDING / 2;

    float x = LEFT;
    float y = Y_AXIS_END.y;

    g.setFont( FONT_SMALLEST );
    FontMetrics fm = g.getFontMetrics();

    /*
     * Draw the sRNA hit categories
     */

    // Draw 'category' string
    g.setColor( Color.black );
    g.drawString( CATEGORY, x, y );

    x += fm.stringWidth( CATEGORY ) + PADDING / 2 + 2;

    // Draw number then coloured box for each one
    //
    for ( Category c : Category.values() )
    {
      if ( c == Category.UNDEFINED )
        continue;

      // Draw the box
      paintSmallRNAHit( g, c, x, y - BOX_SIZE / 2 );

      // Move to the right
      x += BOX_SIZE;

      // Draw the text
      g.setColor( Color.black );
      String categoryTitle = "" + c.getCategoryValue();
      g.drawString( categoryTitle, x, y );

      // Move to the right
      x += fm.stringWidth( categoryTitle ) + PADDING;
    }

    // Next line
    x = LEFT;
    y += fm.getHeight() + 1;

    /*
     * Degradome alignment
     */

    // Draw the text
    g.setColor( Color.black );
    g.drawString( DEGRADOME_ALIGNMENT, x, y );

    // Move to the right
    x += fm.stringWidth( DEGRADOME_ALIGNMENT ) + 2;

    // Draw the blue circle
    paintDegradomeHit( g, x, y - fm.getAscent() / 2 + 1 );

    // Move to the right
    x += CIRCLE_DIAMETER + PADDING;

    /*
     * Median line
     */

    // Draw the text
    g.setColor( Color.black );
    g.drawString( MEDIAN, x, y );

    // Move to the right
    x += fm.stringWidth( MEDIAN ) + 2;

    // Draw the grey line
    g.setColor( Color.lightGray );
    g.draw( new Line2D.Float( x, y - fm.getAscent() / 2 + 1, x + 12, y - fm.getAscent() / 2 + 1 ) );
  }

  /**
   * Set the intervals on the x-axis and the plotting multiplier.
   *
   * @param geneLength total number of nucleotides on the x-axis.
   */
  private void setIntervalsXAxis()
  {
    int geneLength = _pr.getGeneLength();

    _positionIncrement = 100;
    int numIntervals = geneLength / _positionIncrement;

    if ( numIntervals > 20 )
    {
      _positionIncrement = 20;
      numIntervals = geneLength / _positionIncrement;

      while ( numIntervals > 20 )
      {
        _positionIncrement *= 2;
        numIntervals = Math.round( (float)geneLength / _positionIncrement );
      }
    }
    else if ( numIntervals < 10 )
    {
      while ( numIntervals < 10 && _positionIncrement > 1 )
      {
        _positionIncrement /= 2;
        numIntervals = geneLength / _positionIncrement;
      }
    }

    _positionScale = ( X_AXIS_END.x - AXES_ORIGIN.x ) / geneLength;
  }

  /**
   * Set the intervals on the y-axis and the plotting multiplier.
   *
   * @param highestAbundance used to calibrate the y-axis
   */
  private void setIntervalsYAxis()
  {
    int maxY;

    if ( _pr.getMaxDegradomeHitAbundance() < 10 )
    {
      maxY = 10;
      _abundanceIncrement = 1;
    }
    else
    {
      maxY = 10 * Math.round( 0.51f + _pr.getMaxDegradomeHitAbundance() / 10 );
      _abundanceIncrement = 10;
    }

    int numIntervals = maxY / _abundanceIncrement;

    if ( numIntervals < 10 || numIntervals > 25 )
    {
      if ( numIntervals > 25 )
      {
        _abundanceIncrement = 25;
        numIntervals = maxY / _abundanceIncrement;

        while ( numIntervals > 25 )
        {
          _abundanceIncrement *= 2;
          numIntervals = Math.round( (float)maxY / _abundanceIncrement );
        }
      }
      else if ( numIntervals < 10 )
      {
        while ( numIntervals < 10 && _abundanceIncrement > 1 )
        {
          _abundanceIncrement /= 2;
          numIntervals = maxY / _abundanceIncrement;
        }
      }

      numIntervals += 2;
      maxY = _abundanceIncrement * numIntervals;
    }

    float y_axis_height = AXES_ORIGIN.y - Y_AXIS_END.y;

    _abundanceScale = y_axis_height / maxY;
  }

  //////////////////////////////////////////////////////////////////////////////
  // Utility class : Stores the Degradome hit and the sRNA hit for each point in a PlotRecord
  // A point is ( position, abundance ).
  // Each points has a degradome hit and a list of sRNA hits
  //
  private static final class TPlotPoint
  {
    private final PlotRecord.DegradomeHit _degradomeHit;
    private final List< PlotRecord.SmallRNAHit > _sRnaHits;
    private final Category _category;

    private String _label = "";

    private TPlotPoint( PlotRecord.DegradomeHit dh, List< PlotRecord.SmallRNAHit > sRnaHits )
    {
      _degradomeHit = dh;
      _sRnaHits = sRnaHits;

      Category cat = Category.UNDEFINED;

      for ( PlotRecord.SmallRNAHit sh : _sRnaHits )
      {
        if ( sh.getCategory().getCategoryValue() < cat.getCategoryValue() )
        {
          cat = sh.getCategory();
        }
      }

      _category = cat;

      setLabel();
    }

    private void setLabel()
    {
      if ( _sRnaHits.isEmpty() )
        return;

      if ( _sRnaHits.size() == 1 )
      {
        _label = Integer.toString( _sRnaHits.get( 0 ).getIndex() ) + ";";
        return;
      }

      StringBuilder sb = new StringBuilder();

      final int N = _sRnaHits.size();
      boolean sequence = false;
      int i = 0;

      while ( true )
      {
        int idx0 = _sRnaHits.get( i ).getIndex();
        int idx1 = _sRnaHits.get( i+1 ).getIndex();

        if ( sequence )
        {
          if ( idx0 + 1 != idx1 )
          {
            sb.append( idx0 ).append( ';' );
            sequence = false;
          }
        }
        else
        {
          sb.append( idx0 );

          if ( idx0 + 1 == idx1 )
          {
            sb.append( '-' );
            sequence = true;
          }
          else
          {
            sb.append( ';' );
          }
        }

        if ( i == N - 2 )
        {
          sb.append( idx1 ).append( ';' );
          break;
        }

        ++i;
      }

      _label = sb.toString();
    }

    private int getPosition()
    {
      return _degradomeHit.getPosition();
    }

    private float getAbundance()
    {
      // The sRNA hit abundance is ignored !

      return _degradomeHit.getAbundance();
    }

    private Category getCategory()
    {
      return _category;
    }

    private boolean hasSrnaHit()
    {
      return ! _sRnaHits.isEmpty();
    }

    private String getLabel()
    {
      return _label;
    }

    /**
     * Static factory method to wrap-up a PlotRecord and return a list of TPlotPoints.
     *
     * @param pr The PlotRecord
     * @return List of TPlotPoint objects - for all hits (degradome and sRNA)
     */
    private static List< TPlotPoint > wrap( PlotRecord pr )
    {
      List< TPlotPoint > result = CollectionUtils.newArrayList();

      for ( PlotRecord.DegradomeHit dh : pr.getDegradomeHits() )
      {
        List< PlotRecord.SmallRNAHit > srnas = pr.getSmallRNAHitsForPosition( dh.getPosition() );

        result.add( new TPlotPoint( dh, srnas ) );
      }

      return result;
    }
  };
}

