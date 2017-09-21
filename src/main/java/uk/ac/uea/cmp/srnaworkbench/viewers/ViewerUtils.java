/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.util.Random;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;

/**
 *
 * @author Matt
 */
public class ViewerUtils
{
  /**
   * For generating random numbers.
   */
  static Random random = new Random();
  

   
  /**
   * Generates an array of sample data.
   *
   * @param size the array size.
   * @param shift the shift from zero.
   *
   * @return The array of sample data.
   */
  static double[] gaussianData( int size, double shift )
  {
    double[] d = new double[size];
    for ( int i = 0; i < d.length; i++ )
    {
      d[i] = random.nextGaussian() + shift;
    }
    return d;
  }
  
    /**
   * Creates a sample {@link HistogramDataset}.
   *
   * @return The dataset.
   */
  static IntervalXYDataset createTestDataset()
  {
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType( HistogramType.RELATIVE_FREQUENCY );
    dataset.addSeries( "H1", gaussianData( 1000, 3.0 ), 20 );
    dataset.addSeries( "H0", gaussianData( 1000, 0 ), 20 );
    return dataset;
  }
  
  static XYSeriesCollection createTestSeriesDataset()
  {
    XYSeriesCollection dataset = new XYSeriesCollection();
    double[] x = gaussianData(100, 10);
    double[] y = gaussianData(100, 3);
    XYSeries xy = new XYSeries("MA");
    
    for(int i=0; i<x.length; i++)
    {
      xy.add(x[i], y[i]);
    }
    dataset.addSeries( xy );
    return(dataset);
  }

  
}
