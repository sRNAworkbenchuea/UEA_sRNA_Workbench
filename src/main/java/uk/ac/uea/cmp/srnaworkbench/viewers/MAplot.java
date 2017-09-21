package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAelement;

/**
 *
 * @author Matt
 */
public class MAplot<XYD extends XYSeriesCollection> extends ExpressionPlot<XYSeriesCollection>
{
  /**
   * Creates new form MAplot
   */
  public MAplot()
  {
    data = new XYSeriesCollection();
  }

  @Override
  protected JFreeChart createChart( XYSeriesCollection xyData, String title )
  {
    JFreeChart maChart = ChartFactory.createScatterPlot( title, "M", "A",
      xyData, PlotOrientation.HORIZONTAL, false, true, false );
    maChart.getXYPlot().setForegroundAlpha( 0.75f );
    XYPlot plot = (XYPlot) maChart.getPlot();
    plot.getRenderer().setSeriesShape(0, new Ellipse2D.Double(-1, -1, 2, 2));
    XYPlot xyPlot = (XYPlot) maChart.getPlot();
    maChart.setBackgroundPaint( Color.white );
    xyPlot.setBackgroundPaint( Color.white );
    return ( maChart );
  }

  public void inputData( MAList maList )
  {
    XYSeries xyS = new XYSeries( "MA" );

    for ( MAelement thisma : maList )
    {
      xyS.add( thisma.getM(), thisma.getA() );
    }
    
    this.data.addSeries( xyS );
  }
  
  public void setTitle(String newTitle)
  {
    this.chartTitle = newTitle;
  }

  public void highlightArea(double m, double a, XYPlot plot)
  {
    Range mRange = plot.getDataRange( plot.getDomainAxis() );
    Range aRange = plot.getDataRange( plot.getRangeAxis() );
    IntervalMarker mI = new IntervalMarker(m,mRange.getUpperBound() - m);
    IntervalMarker aI = new IntervalMarker(a, aRange.getUpperBound() - a);
    plot.addDomainMarker( mI, Layer.BACKGROUND );
    plot.addRangeMarker( aI, Layer.BACKGROUND );
  } 
}
