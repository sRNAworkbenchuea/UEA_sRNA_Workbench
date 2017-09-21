 // ****************************************************************************
  // * JFREECHART DEVELOPER GUIDE                                               *
  // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
  // * to purchase from Object Refinery Limited:                                *
  // *                                                                          *
  // * http://www.object-refinery.com/jfreechart/guide.html                     *
  // *                                                                          *
  // * Sales are used to provide funding for the JFreeChart project - please    * 
  // * support us so that we can continue developing free software.             *
  // ****************************************************************************

package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.awt.Color;
import java.awt.Cursor;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.GenerateWaitCursor;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class AbundanceDistributionViewer extends javax.swing.JInternalFrame implements GUIInterface
{
  
  private HashMap<String, FastaMap> filenameToData;
  private FastaMap singleDataEntry;
  private ArrayList<Integer> sizesToReturn;
  private String chart_title = "Default Chart Title";

  /**
   * Creates new form abundanceDistributionViewer
   */
  public AbundanceDistributionViewer()
  {
    initComponents();
    
    
   
  }

  public void inputData(HashMap<String, FastaMap> data, String title)
  {
    filenameToData = data;
    
        
    //add title to chart
    chart_title = title;
  }
  public void inputData( FastaMap data, String title )
  {
    //create shell for fasta data
    filenameToData = new HashMap<String, FastaMap>();
    filenameToData.put( "nullentry", data );

    //add title to chart
    chart_title = title;
  }

  public void setGraphTitle( String title )
  {

    chart_title = title;
  }
  public void initiliseAbundanceFrequencyPlot(int log_base, int min_abund)
  {
    IntervalXYDataset dataset = createFrequencyDataset(log_base, min_abund);
    JFreeChart chart = createHistogramChart( dataset );
    XYPlot myplot = (XYPlot)chart.getPlot();
    LogAxis myLogAxis = new LogAxis ();
    myLogAxis.setSmallestValue(1);
    myplot.setRangeAxis(myLogAxis);
    myplot.setDomainAxis( myLogAxis );
    //chart.
    //dataset.
    ChartPanel chartPanel = new ChartPanel( chart );
     chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
    chartPanel.setMouseZoomable( true, false );
    setContentPane( chartPanel );
  }
  
  public void initiliseSizeClassDistribution( )
  {
    new Thread( new GenerateWaitCursor( this ) ).start();

    CategoryDataset dataset = createSizeClassDataset();
    JFreeChart chart = this.createBarChart( dataset );
//    XYPlot myplot = (XYPlot)chart.getPlot();
//    LogAxis myLogAxis = new LogAxis ();
//    myLogAxis.setSmallestValue(1);
//    myplot.setRangeAxis(myLogAxis);
//    myplot.setDomainAxis( myLogAxis );
    //chart.
    //dataset.
    ChartPanel chartPanel = new ChartPanel( chart );
    chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
    chartPanel.setMouseZoomable( true, false );
    setContentPane( chartPanel );
    this.setCursor( Cursor.getDefaultCursor() );
  }

  public void initiliseSizeClassDistribution( Map<Integer, FilterStage> length_abundance )
  {
    new Thread( new GenerateWaitCursor( this ) ).start();

    CategoryDataset dataset = createSizeClassDataset(length_abundance);
    JFreeChart chart = this.createBarChart( dataset );
    ChartPanel chartPanel = new ChartPanel( chart );
    chartPanel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
    chartPanel.setMouseZoomable( true, false );
    setContentPane( chartPanel );
    this.setCursor( Cursor.getDefaultCursor() );
  }

  private TreeMap<Integer, Integer> countVals( Map<String, Integer> input, int[] minMax )
  {
    TreeMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
    for ( Entry<String, Integer> entry : input.entrySet() )
    {
      Integer value = entry.getValue();
      if(value <= minMax[0])
      {
        minMax[0] = value;
      }
      if(value >= minMax[1])
      {
        minMax[1] = value;
      }
      Integer count = result.get( value );
      if ( count == null )
      {
        result.put( value, new Integer( 1 ) );
      }
      else
      {
        result.put( value, new Integer( count + 1 ) );
      }
    }
    return result;
  }
  private IntervalXYDataset createFrequencyDataset(int log_base, int minAbund)
  {

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType( HistogramType.FREQUENCY );

    for(Entry<String, FastaMap> entry : filenameToData.entrySet())
    {
     
      //Map<String, Integer> sorted = Tools.sortByValue( entry.getValue());

      Object[] series = entry.getValue().values().toArray( );
      double[] finalSeries = new double[series.length];
      for(int i = 0; i < series.length; i++)
      {
        finalSeries[i] = ((FastaRecord)series[i]).getAbundance();//Double.parseDouble( series[i].toString());
      }
      dataset.addSeries( entry.getKey(), finalSeries, entry.getValue().size());
     
    }

    return dataset;
  }
  private CategoryDataset createSizeClassDataset(  )
  {


    DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
    String seq = "Abundance";

    for ( int i = 1; i < 52; i++ )
    {
      defaultCategoryDataset.addValue( 0.0, "Abundance", String.valueOf( i ) );
    }

    for(Entry<String, FastaMap> entry : filenameToData.entrySet())
    {
      FastaMap sequenceData = entry.getValue();
      for ( Entry<String, FastaRecord> record : sequenceData.entrySet() )
      {
        
        String currentSize = String.valueOf( record.getKey().length() );
        try
        {
          Number value = defaultCategoryDataset.getValue( seq, currentSize );
          int currentAmount = value.intValue() + record.getValue().getAbundance();
          defaultCategoryDataset.setValue( currentAmount, seq, currentSize );
          //         }
        }
        catch ( UnknownKeyException e )
        {
          defaultCategoryDataset.addValue( record.getValue().getAbundance(), seq, currentSize );
        }
      }   
    }
    selectK(defaultCategoryDataset, 4);
    
    return defaultCategoryDataset;
  }
  

  private CategoryDataset createSizeClassDataset( Map<Integer, FilterStage> length_abundance )
  {


    DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
    String seq = "Abundance";

    for ( int i = 1; i < 52; i++ )
    {
      defaultCategoryDataset.addValue( 0.0, "Abundance", String.valueOf( i ) );
    }
    
    for(Entry<Integer, FilterStage> entry : length_abundance.entrySet())
    {
      String currentSize = String.valueOf( entry.getKey() );
        try
        {
          Number value = defaultCategoryDataset.getValue( seq, currentSize );
          int currentAmount = value.intValue() + entry.getValue().getTotalReadCount();
          defaultCategoryDataset.setValue( currentAmount, seq, currentSize );
          //         }
        }
        catch ( UnknownKeyException e )
        {
          defaultCategoryDataset.addValue( entry.getValue().getTotalReadCount(), seq, currentSize );
        }
    }
//
//    for(Entry<String, FastaMap> entry : filenameToData.entrySet())
//    {
//      FastaMap sequenceData = entry.getValue();
//      for ( Entry<String, FastaRecord> record : sequenceData.entrySet() )
//      {
//        
//        String currentSize = String.valueOf( record.getKey().length() );
//        try
//        {
//          Number value = defaultCategoryDataset.getValue( seq, currentSize );
//          int currentAmount = value.intValue() + record.getValue().getAbundance();
//          if(currentAmount == 0)
//            System.out.println( "" );
//          defaultCategoryDataset.setValue( currentAmount, seq, currentSize );
//          //         }
//        }
//        catch ( UnknownKeyException e )
//        {
//          defaultCategoryDataset.addValue( record.getValue().getAbundance(), seq, currentSize );
//        }
//      }   
//    }
//    selectK(defaultCategoryDataset, 4);
//    
    return defaultCategoryDataset;
  }
  /**
   * Select top K values from a DefaultCategoryDataset
   *function select(list[1..n], k)
   *  for i from 1 to k
   *    minIndex = i
   *     minValue = list[i]
   *      for j from i+1 to n
   *          if list[j] < minValue
   *             minIndex = j
   *              minValue = list[j]
   *      swap list[i] and list[minIndex]
   *  return list[k]
   * 
   * @param data
   * @param K 
   */

  private void selectK(DefaultCategoryDataset data, int K)
  {
    
    sizesToReturn = new ArrayList<Integer>(K);
    DefaultCategoryDataset localData = null;
    try
    {
      localData = (DefaultCategoryDataset) data.clone();
    }
    catch ( CloneNotSupportedException ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage() );
    }
    //for ( int r = 0; r < data.getRowCount(); r++ )//only 1 row anyway
    //{
      int maxIndex = 0;
      Number maxNumber;// = data.getValue( 0, 0 );
      int n = localData.getColumnCount();
      for ( int i = 0; i < K; i++ )
      {
        maxIndex = i;
        maxNumber = data.getValue( 0, i );
        for(int j = i+1; j < n; j++)
        {
          if(localData.getValue( 0, j).doubleValue() > maxNumber.doubleValue() )
          {
            maxIndex = j;
            maxNumber = data.getValue( 0, j);
          }
        }
        
        Number firstVal = data.getValue( 0, i);
        Number secondVal = data.getValue( 0, maxIndex);
        localData.setValue( secondVal, data.getRowKey( 0), data.getColumnKey( i) );
        localData.setValue( firstVal, data.getRowKey( 0), data.getColumnKey( maxIndex ) );
        sizesToReturn.add( Integer.valueOf( data.getColumnKey( maxIndex ).toString() ) );
        
      }   
  }

  /**
   * Creates a chart.
   *
   * @param dataset a dataset.
   *
   * @return The chart.
   */
  private JFreeChart createHistogramChart( IntervalXYDataset dataset )
  {
    JFreeChart chart = ChartFactory.createHistogram(
      "Histogram Demo",
      "Sequence Abundance",
      "Frequency",
      dataset,
      PlotOrientation.VERTICAL,
      true,
      false,
      false );
    chart.getXYPlot().setForegroundAlpha( 0.75f );
    return chart;
  }
  
  private  JFreeChart createBarChart( CategoryDataset intervalDataset )
  {
    JFreeChart jfreechart = ChartFactory.createBarChart( chart_title, "Size Class", "Abundance", intervalDataset, 
      PlotOrientation.VERTICAL, false,true, false );
    
    jfreechart.setBackgroundPaint( Color.white );
    CategoryPlot categoryPlot = (CategoryPlot) jfreechart.getPlot();
    categoryPlot.setBackgroundPaint( new Color( 238, 238, 255 ) );
    //CategoryDataset categorydataset = createDataset2();
    //categoryplot.setDataset( 1, categorydataset );
    categoryPlot.mapDatasetToRangeAxis( 1, 1 );
    //categoryplot.set
    CategoryAxis domainXAxis = categoryPlot.getDomainAxis();
    domainXAxis.setMaximumCategoryLabelLines( 60 );
    //domainXAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    //((BarRenderer) (categoryPlot.getRenderer())).setItemMargin(.001);
    //br.setMaximumBarWidth(1.0); 
    //br.setItemMargin(.1);
    //categoryaxis;
//    categoryaxis.setCategoryLabelPositions( CategoryLabelPositions.DOWN_45 );
//    NumberAxis numberaxis = new NumberAxis( "Secondary" );
//    categoryplot.setRangeAxis( 1, numberaxis );
    LineAndShapeRenderer lineandshaperenderer = new LineAndShapeRenderer();
    lineandshaperenderer.setBaseToolTipGenerator( new StandardCategoryToolTipGenerator() );
    categoryPlot.setRenderer( 1, lineandshaperenderer );
    categoryPlot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );
    LegendTitle legendtitle = new LegendTitle( categoryPlot.getRenderer( 0 ) );
    legendtitle.setMargin( new RectangleInsets( 2D, 2D, 2D, 2D ) );
    legendtitle.setFrame( new BlockBorder() );
    LegendTitle legendtitle1 = new LegendTitle( categoryPlot.getRenderer( 1 ) );
    legendtitle1.setMargin( new RectangleInsets( 2D, 2D, 2D, 2D ) );
    legendtitle1.setFrame( new BlockBorder() );
    BlockContainer blockcontainer = new BlockContainer( new BorderArrangement() );
    blockcontainer.add( legendtitle, RectangleEdge.LEFT );
    blockcontainer.add( legendtitle1, RectangleEdge.RIGHT );
    blockcontainer.add( new EmptyBlock( 2000D, 0.0D ) );
    CompositeTitle compositetitle = new CompositeTitle( blockcontainer );
    compositetitle.setPosition( RectangleEdge.BOTTOM );
    jfreechart.addSubtitle( compositetitle );
    return jfreechart;
  } 
  
  public ArrayList<Integer> retreiveTopAbundances()
  {
    //Arrays.sort( sizesToReturn );
    Collections.sort( sizesToReturn );
    return sizesToReturn;
  }
  public static AbundanceDistributionViewer generateAbundanceDistribution()
  {
    
    final AbundanceDistributionViewer newInstance = new AbundanceDistributionViewer();
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {


        // Add VisSR to the desktop pane...
        MDIDesktopPane pane = AppUtils.INSTANCE.getMDIDesktopPane();

        if ( pane != null )
        {
          pane.add( newInstance );
        }

        ToolManager.getInstance().addTool( newInstance );

      }
    } );
    return newInstance;
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle(org.openide.util.NbBundle.getMessage(AbundanceDistributionViewer.class, "AbundanceDistributionViewer.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 578, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 437, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

  @Override
  public void runProcedure()
  {
    
  }

  @Override
  public JPanel getParamsPanel()
  {
    return null;
  }

  @Override
  public void setShowingParams( boolean newState )
  {
    
  }

  @Override
  public boolean getShowingParams()
  {
    return false;
  }

  @Override
  public void shutdown()
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }




}
