/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 *
 * @author Matt
 */
public class BoxplotViewer extends Viewer
{
  DefaultBoxAndWhiskerCategoryDataset data;
  
  /**
   * Creates new form BoxplotViewer
   */
  public BoxplotViewer()
  {
    initComponents();
    this.setClosable( true );
    this.setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE );
    data = new DefaultBoxAndWhiskerCategoryDataset();
    this.setSize( new java.awt.Dimension( 800, 450 ));
  }
  
  public void initialiseBoxplot()
  {
    // create dataset
    // new JFreeChart
    // new ChartPanel
    JFreeChart chart = createBoxplotChart();
    ChartPanel chartPanel = new ChartPanel( chart );
    //chartPanel.setPreferredSize( new java.awt.Dimension( 700, 350 ) );
    chartPanel.setMouseZoomable( true, false );
    //this.setLayout( new BorderLayout());
    setContentPane( chartPanel );
     //System.out.println(chartPanel.getSize());

  }
 
  public static BoxplotViewer testBoxplot()
  {
    final BoxplotViewer test = new BoxplotViewer();
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        JFrame frame = new JFrame();

        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(inset, inset,
                  screenSize.width  - inset*2,
                  screenSize.height - inset*2);
        
        JDesktopPane testDesk = new JDesktopPane();
        testDesk.add( test );
        test.setVisible( true );
        frame.setContentPane( testDesk );

        //frame.pack();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
        
      }
    });
    return(test);
  }
  
  private JFreeChart createBoxplotChart()
  {
    JFreeChart chart = ChartFactory.createBoxAndWhiskerChart( "Boxplot", "Methods", "Abundance", data, false);
    // Theme customisations - doesn't work
    // TODO: construct chart manually for more customization control
    //chart.setBackgroundPaint( Color.WHITE );
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint( Color.WHITE );
    plot.setRangeGridlinePaint( Color.LIGHT_GRAY );
        
    return chart;
  }
  
  public void inputDataset(HashMap<String, FastaMap> fastaData, String datasetName)
  {
    //DefaultBoxAndWhiskerCategoryDataset data = new DefaultBoxAndWhiskerCategoryDataset();
    // Use samples as the series
    for(Entry<String, FastaMap> e : fastaData.entrySet()){
      String sampleName = e.getKey();
      FastaMap thisFastaMap = e.getValue();
      
      final ArrayList<Double> list = new ArrayList<>();
      // We just need the abundances from the FastaMap
      for (FastaRecord f : thisFastaMap.values()){
        // Get the real abundance (a double) - compatible with normalised fasta
        // data (continuous non-count data)
        double rawDataPoint = f.getRealAbundance();
        Double transformedDataPoint = Math.log( rawDataPoint );
        list.add(transformedDataPoint);
      }
      data.add(list, sampleName, datasetName);
    }
    
    //return(data);
  }
  
  /**
   * Add data from a SparseExpressionMatrix that contains the specified Normalisation
   * Types
   * @param expressionData
   * @return 
   */
  public void inputDataset(SparseExpressionMatrix expressionData, ArrayList<NormalisationType> normTypes){
    // Flip data matrix
    
    ArrayList<String> samples = expressionData.getFlattenedFileNames();
    HashMap<String, HashMap<String, ExpressionElement>> sampleData = new HashMap<>();
          
    int numSamples = samples.size();
    
    // Add a distribution to the chart for each sample 
    for(String sample : samples)
    {
      //HashMap<String, ExpressionElement> thisSampleMap =  new HashMap<>();
      // For each norm type specified
      for(NormalisationType norm : normTypes)
      {
        // The distribution to eventually add to the chart
        ArrayList<Double> distribution = new ArrayList<>();
        
        // Loop over sequences in the matrix
        for(Entry<String, HashMap<String, ExpressionElement>> seqEntry : expressionData.getEntries())
        {
          // Expressions for this this sequence
          HashMap<String, ExpressionElement> sampleExpressions = seqEntry.getValue();
          boolean containsZeros = false;
          
          // FIXME: CORNER CASE: BootstrapNormalisation adds zeroes to dataset
          for(ExpressionElement e : sampleExpressions.values())
          {
              if(e.getNormalisedAbundance( norm ) == 0)
              {
                  containsZeros = true;
              }
          }

          // Do not add data that is not present in every sample
          // This is a filtering step that resolves boxplots better, but the negative
          // effects of loss of expression points is not yet known...
          if(!containsZeros && sampleExpressions.size() == samples.size())
          {
            // get the expression for the current sample
            ExpressionElement thisExp = sampleExpressions.get(sample);
            
            // Add the expression level to the distribution with logarithm.
            // FIXME: can floats be used?
            distribution.add( (double) Math.log( thisExp.getNormalisedAbundance( norm ) ) );
          }
        }
        
        // Add the distribution for this sample and this norm method to the chart
        data.add( distribution, sample, norm.name() );
      }
    }
  }
  
  public static void main(String[] args)
  {
    File f1 = new File("test/data/norm/ath_366868_head.fa");
    File f2 = new File("test/data/norm/ath_366868_head2.fa");
    
    SRNAFastaReader fr1;
    SRNAFastaReader fr2;
    try
    {
      fr1 = new SRNAFastaReader( f1 );
      fr2 = new SRNAFastaReader( f2 );
      HashMap<String, FastaMap> testData1 = new HashMap<>();

      testData1.put( "366868", new FastaMap( fr1.process() ) );
      testData1.put( "366869", new FastaMap( fr2.process() ) );
      
      fr1 = new SRNAFastaReader( f1 );
      fr2 = new SRNAFastaReader( f2 );
      HashMap<String, FastaMap> testData2 = new HashMap<>();
      testData2.put( "366860", new FastaMap( fr1.process() ) );
      testData2.put( "366861", new FastaMap( fr2.process() ) );

      BoxplotViewer myBP = testBoxplot();
      myBP.inputDataset( testData1, "Dataset1" );
      myBP.inputDataset( testData1, "Dataset2" );
      myBP.initialiseBoxplot();
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage());
    }
   
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setTitle(org.openide.util.NbBundle.getMessage(BoxplotViewer.class, "BoxplotViewer.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 394, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
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
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }
}
