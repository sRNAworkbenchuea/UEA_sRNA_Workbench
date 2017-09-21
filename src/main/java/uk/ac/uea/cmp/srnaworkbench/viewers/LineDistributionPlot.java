package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 * Like AbundanceDistributionViewer but with lines rather than bars, enabling
 * multiple samples to be shown in one graphic
 * @author Matthew Beckers
 */
public class LineDistributionPlot<XYD extends XYSeriesCollection> extends ExpressionPlot<XYSeriesCollection>
{
  //XYSeriesCollection data = new XYSeriesCollection();
  public final static int REDUNDANT = 0;
  public final static int NON_REDUNDANT = 1;
  public final static int COUNT_COMPLEXITY = 2;
  
  int abundanceType = REDUNDANT;
  
  public LineDistributionPlot()
  {
    data = new XYSeriesCollection();
  }
  
  public void setCountType(int countType)
  {
    this.abundanceType = countType;
  }
  
  public void inputData(SparseExpressionMatrix sem, NormalisationType normType)
  {
    HashMap<Integer, HashMap<String, Abundance>> datamap = new HashMap<>();
    
    for(Entry<String, HashMap<String, ExpressionElement>> e : sem.getEntries())
    {
      String seq = e.getKey();
      int length = seq.length();
      //HashMap<String, ExpressionElement> sampleMap = e.getValue();
      
      //VIVICATION
      if(!datamap.containsKey( length ))
      { 
        datamap.put( length, new HashMap<String, Abundance>());
      }
      
      for ( Entry<String, ExpressionElement> sampleElement : e.getValue().entrySet() )
      {
        String sample = sampleElement.getKey();
        
        //VIVICATION
        if(!datamap.get( length ).containsKey( sample ))
        {
          datamap.get( length ).put(sample, new Abundance());
        }
        
        // Sum count for length and sample
        Abundance thisCount = datamap.get( length ).get( sample );
        thisCount.add( sampleElement.getValue().getNormalisedAbundance( normType ));
        
      }
    }
    
    ArrayList<String> samples = sem.getFlattenedFileNames();
    for(String sample : samples)
    {
      final XYSeries thisSeries = new XYSeries(sample);
      for(Integer length : datamap.keySet())
      {
        Abundance a = datamap.get( length ).get( sample );
        Double n;
        switch(abundanceType)
        {
          case REDUNDANT:
            n = a.r;
            break;
          case NON_REDUNDANT:
            n = Double.valueOf( a.nr );
            break;
          case COUNT_COMPLEXITY:
            n = a.getComplexity();
            break;
          default:
            throw new IllegalArgumentException("Bad abundance type");
        }
        thisSeries.add( length, n);
      }
      
      this.data.addSeries( thisSeries );
    }
    
  }
  
  @Override
  protected JFreeChart createChart(XYSeriesCollection data, String title)
  {
    String yTitle;
    switch ( abundanceType )
    {
      case REDUNDANT:
        yTitle = "Redundant read abundance";
        break;
      case NON_REDUNDANT:
        yTitle = "Non-redundant read abundance";
        break;
      case COUNT_COMPLEXITY:
        yTitle = "Read abundance complexity";
        break;
      default:
        throw new IllegalArgumentException( "Bad abundance type" );
    }
    JFreeChart lineChart = ChartFactory.createXYLineChart( "", "Read length (nt)", yTitle, data, PlotOrientation.VERTICAL, true, true, true);
    XYPlot plot = lineChart.getXYPlot();
    
    // colour customization
    plot.setBackgroundPaint( Color.WHITE );
    plot.setDomainGridlinePaint( Color.LIGHT_GRAY );
    plot.setRangeGridlinePaint( Color.LIGHT_GRAY );

    // Set x axis to show discrete integer ticks
    final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
    xAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
    return(lineChart);
  }
  
  public static void main(String[] args)
  {
    try
    {
      final SparseExpressionMatrix s = SparseExpressionMatrix.getTestMatrix();
      
      
      
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      JDesktopPane desktop = new JDesktopPane();
      frame.setContentPane( desktop );
      
      JInternalFrame chart = new JInternalFrame();
      LineDistributionPlot aplot = new LineDistributionPlot();
      aplot.inputData( s, NormalisationType.NONE );
      chart.setContentPane( aplot.getChartPanel() );

      chart.pack();
      //ma.setLocationRelativeTo(null);
      //ma.chartPanel.setVisible( true );
      chart.setVisible( true );

      desktop.add( chart );


      //Make the big window be indented 50 pixels from each edge
      //of the screen.
      int inset = 50;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setBounds( inset, inset,
        screenSize.width - inset * 2,
        screenSize.height - inset * 2 );
      //frame.pack();
      frame.setVisible( true );
    }
    catch ( Exception ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
  }
  
  class Abundance
  {
    public int nr = 0;
    public double r = 0;
    
    public void add(double r)
    {
      this.r += r;
      nr++;
    }
    
    public double getComplexity()
    {
      return(nr/r);
    }
  }
  
}
