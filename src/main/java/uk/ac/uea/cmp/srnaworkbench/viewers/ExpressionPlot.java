package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.AbstractXYDataset;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/** 
 * A class that creates returns a plot that uses ExpressionData
 * There are two input methods that can be implemented: one for data from FastaMaps
 * and one for data from a SparseExpressionMatrix
 * @author Matt
 */
public abstract class ExpressionPlot<D extends AbstractXYDataset>
{
  D data;
  String chartTitle = null;
  
  /**
   * Should return a completed customized JFreeChart. All chart customizations
   * go in here
   * @return The JFreeChart object you want to displau
   */
  protected abstract JFreeChart createChart(D data, String title);
  
//  public void inputData(SparseExpressionMatrix sem, List<String> samples)
//  {
//    ArrayList<NormalisationType> normTypes = new ArrayList<>();
//    normTypes.add( NormalisationType.NONE );
//    inputData(sem, samples, normTypes);
//  }
//  
//  public abstract void inputData(SparseExpressionMatrix sem, List<String> samples, List<NormalisationType> normTypes);
  
  /**
   * Creates the JFreeChart and returns the chart inside a ChartPanel
   * @return ChartPanel containing the created JFreeChart
   */
  public final ChartPanel getChartPanel()
  {
    JFreeChart maChart = createChart(data, chartTitle);
    ChartPanel chartPanel = new ChartPanel( maChart );
    chartPanel.setPreferredSize( new java.awt.Dimension( 500, 400 ) );
    return chartPanel;
  }
  
}
