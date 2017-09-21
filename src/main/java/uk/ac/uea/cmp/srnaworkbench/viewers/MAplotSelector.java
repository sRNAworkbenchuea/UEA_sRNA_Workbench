/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.viewers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.swing.RotatingIcon;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAList;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 * Can display multiple input MAplots based on the selection made by a user
 * in the drop down menus of available data.
 * @author Matt
 */
public class MAplotSelector extends Viewer
{
  private ArrayList<String> samples;
  
  
  ArrayList<NormalisationType> normTypes;
  private static ArrayList<NormalisationType> getDefaultTypeList(){
    ArrayList<NormalisationType> nt = new ArrayList<>();
    nt.add( NormalisationType.NONE );
    return(nt);
  }
  
  private SparseExpressionMatrix sem;
  
  
  /**
   * Creates new form MAplotSelector with available data from a matrix
   */
  public MAplotSelector()
  {
    this.setClosable( true );
    this.setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE );
    initComponents();
  }
  
  public void initialise()
  {
    //updateChart();
  }
  
  public void setData(SparseExpressionMatrix sem, ArrayList<NormalisationType> normTypes)
  {
    this.sem = sem;
    this.normTypes = normTypes;
    
    //this.setContentPane( this.jPanel1 );
    samples = sem.getFlattenedFileNames();

    setComboSampleSelection( dataCombo1 );
    setComboSampleSelection( dataCombo2 );
    dataCombo2.setSelectedIndex( 1 );
  }
  
  private void updateChart()
  {
    final String sample1 = (String) this.dataCombo1.getSelectedItem();
    final String sample2 = (String) this.dataCombo2.getSelectedItem();

    this.labIconLoad.setIcon( new RotatingIcon( new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/ProgressSmall.gif" ) ), this.labIconLoad));
    // remove any older chart
    this.chartPanel.removeAll();
    
    SwingWorker sw = new SwingWorker<ArrayList<MAplot>, Void>(){

      @Override
      protected ArrayList<MAplot> doInBackground() throws Exception
      {
        // Calculate MA values for samples selected by user
        ArrayList<MAplot> plots = new ArrayList<>();
        for ( NormalisationType nt : normTypes )
        {
          MAList malist = new MAList( sem, sample1, sample2, 2, 0, nt );
          MAplot maplot = new MAplot();
          maplot.setTitle( nt.name().toLowerCase() );
          maplot.inputData( malist );
          plots.add( maplot );        
        }
        
        // return an array of plots for different normalised values
        return(plots);
      
      }
      
      @Override
      public void done()
      {
        // get created plots for different normalised values and arrange in the
        // graph panel
        try
        {
          ArrayList<MAplot> plots = get();
          
          // Setup grid layout to hold correct number of plots
          GridLayout newGL = new GridLayout(0,2);
          chartPanel.setLayout( newGL );
          
          for(MAplot p : plots)
          {
            chartPanel.add( p.getChartPanel() );
          }
          labIconLoad.setIcon(new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/completeSmall.jpeg" )));
          chartPanel.validate();
        }
        catch ( InterruptedException ex )
        {
          //LOGGER.log(Level.SEVERE, ex.getMessage());
        }
        catch ( ExecutionException ex )
        {
          LOGGER.log(Level.SEVERE, ex.getMessage() );
        }
      }
    };
    sw.execute();
  }
  
  /**
   * Sets the list of filenames that the Reference sample combobox lists
   * This is found from the valid files that are currently in the input box
   * @param filenames a list of Files
   */
  private void setComboSampleSelection(JComboBox dataCombo)
  {
    DefaultComboBoxModel<String> comboSampleModel = new DefaultComboBoxModel<>();
    if (samples.isEmpty())
    {
      // Blank combo box when no files have been given
      comboSampleModel.addElement( "" );
    }
    else
    {
      // First element should be an auto choose prompt
      for (String thisName : samples)
      {
        comboSampleModel.addElement( thisName );
      }
    }
    dataCombo.setModel( comboSampleModel );

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

        chartPanel = new javax.swing.JPanel();
        selectionPanel = new javax.swing.JPanel();
        dataCombo2 = new javax.swing.JComboBox();
        dataCombo1 = new javax.swing.JComboBox();
        labIconLoad = new javax.swing.JLabel();

        setTitle(org.openide.util.NbBundle.getMessage(MAplotSelector.class, "MAplotSelector.title")); // NOI18N

        chartPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        chartPanel.setPreferredSize(new java.awt.Dimension(350, 350));
        chartPanel.setLayout(new java.awt.GridLayout(1, 0));

        dataCombo2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataCombo2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dataCombo2ActionPerformed(evt);
            }
        });

        dataCombo1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataCombo1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dataCombo1ActionPerformed(evt);
            }
        });

        labIconLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/completeSmall.jpeg"))); // NOI18N

        javax.swing.GroupLayout selectionPanelLayout = new javax.swing.GroupLayout(selectionPanel);
        selectionPanel.setLayout(selectionPanelLayout);
        selectionPanelLayout.setHorizontalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dataCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labIconLoad)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        selectionPanelLayout.setVerticalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectionPanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dataCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dataCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(selectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labIconLoad)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(selectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void dataCombo1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataCombo1ActionPerformed
  {//GEN-HEADEREND:event_dataCombo1ActionPerformed
    updateChart();
  }//GEN-LAST:event_dataCombo1ActionPerformed

  private void dataCombo2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataCombo2ActionPerformed
  {//GEN-HEADEREND:event_dataCombo2ActionPerformed
    updateChart();
  }//GEN-LAST:event_dataCombo2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox dataCombo1;
    private javax.swing.JComboBox dataCombo2;
    private javax.swing.JLabel labIconLoad;
    private javax.swing.JPanel selectionPanel;
    // End of variables declaration//GEN-END:variables

  public static void main(String [] args)
  {
    try
    {
      final SparseExpressionMatrix s = SparseExpressionMatrix.getTestMatrix();
            EventQueue.invokeLater( new Runnable(){
        public void run(){
      MAplotSelector ma = new MAplotSelector();
      ma.setData( s, MAplotSelector.getDefaultTypeList());
      
      
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      JDesktopPane desktop = new JDesktopPane();
      frame.setContentPane( desktop );
      

      ma.pack();
      //ma.setLocationRelativeTo(null);
      //ma.chartPanel.setVisible( true );
      ma.setVisible( true );

      desktop.add( ma );
      
      
      //Make the big window be indented 50 pixels from each edge
      //of the screen.
      int inset = 50;
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setBounds( inset, inset,
        screenSize.width - inset * 2,
        screenSize.height - inset * 2 );
      //frame.pack();
      frame.setVisible( true );
      //desktop.setVisible( true );
    }
                
      });
  }
    catch ( Exception ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage());
    }
  }

  @Override
  public void runProcedure()
  {
    // nothing to be run
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
    // nothing to be done
  }
}
