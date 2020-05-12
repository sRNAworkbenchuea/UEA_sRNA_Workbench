/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SiLoCoMainFrame.java
 *
 * Created on Sep 12, 2011, 3:18:45 PM
 */
package uk.ac.uea.cmp.srnaworkbench.tools.siloco;

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter;
import uk.ac.uea.cmp.srnaworkbench.utils.ThreadCompleteListener;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class SiLoCoMainFrame extends javax.swing.JInternalFrame implements GUIInterface, ThreadCompleteListener
{
  private boolean showingParams = false;
  private File myGenomeFile;
  /**
   * Creates new form SiLoCoMainFrame
   */
  public SiLoCoMainFrame()
  {
    initComponents();
    paramsPanel = new SiLoCoParamsGUI( this );
    StatusTracker tracker = new StatusTracker(this.mainProgressBar, this.lblStatus);
    this.mySiLoco = new SiLoCoExpressionMatrixProcess( this, tracker );
    this.setName( "SiLocoMainFrame");
//    this.mainResultsTable.setVisible( false );
//
//    this.mainTableScrollPane.getViewport().setBackground( new java.awt.Color( 120, 120, 120 ) );
    
  
    
    fspInput.setFilters( new FileNameExtensionFilter[]{FileExtFilter.FASTA.getFilter()});
    fspGenome.setFilters( new FileNameExtensionFilter[]{FileExtFilter.FASTA.getFilter()});
    int widths[] = {fspInput.getLabelWidth(),
    fspGenome.getLabelWidth(),
    };

    int maxWidth = Integer.MIN_VALUE;
    for(int currentWidth : widths)
    {
      if(currentWidth > maxWidth)
        maxWidth = currentWidth;
    }
    fspInput.setLabelWidth( maxWidth );
    fspGenome.setLabelWidth( maxWidth );
 
    fspInput.setFileLineAmount( 16);
    
    Tools.trackPage( "SiLoCo Main GUI Frame Class Loaded");
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

        jPanel1 = new javax.swing.JPanel();
        fspInput = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        fspGenome = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        lblStatus = new javax.swing.JLabel();
        mainProgressBar = new javax.swing.JProgressBar();
        cmdStart = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainResultsContainer = new uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoResultsPanel();
        filterResultsPanel = new uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        exportMainMenu = new javax.swing.JMenu();
        exportToCSV = new javax.swing.JMenuItem();
        runMenu = new javax.swing.JMenu();
        mnuRun = new javax.swing.JMenuItem();
        mnuCancel = new javax.swing.JMenuItem();
        mnuView = new javax.swing.JMenu();
        vissrMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        loadHelp = new javax.swing.JMenuItem();

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("SiLoCo");
        setAutoscrolls(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/balance_big.jpg"))); // NOI18N

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Input", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        fspInput.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.SRNA);
        fspInput.setLabel("Path(s) to Sample files: ");
        fspInput.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.MULTI_LOAD);
        fspInput.setToolName("SiLoCo");

        fspGenome.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.GENOME);
        fspGenome.setLabel("Path to Genome file:");
        fspGenome.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.LOAD);
        fspGenome.setToolName("SiLoCo");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fspInput, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(fspGenome, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(fspInput, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(fspGenome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblStatus.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(255, 255, 255));
        lblStatus.setText("Status:");

        cmdStart.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdStart.setText("Start");
        cmdStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdStartActionPerformed(evt);
            }
        });

        cmdCancel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdCancel.setText("Cancel");
        cmdCancel.setEnabled(false);
        cmdCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdCancelActionPerformed(evt);
            }
        });

        jTabbedPane1.addTab("SiLoCo Results", mainResultsContainer);
        jTabbedPane1.addTab("Filter Results", filterResultsPanel);

        menuBar.setBackground(new java.awt.Color(213, 219, 245));

        fileMenu.setText("File");

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/close-tool.png"))); // NOI18N
        exitMenuItem.setText("Close");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        exportMainMenu.setText("Export");
        exportMainMenu.setEnabled(false);

        exportToCSV.setText("Export to CSV");
        exportToCSV.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportToCSVActionPerformed(evt);
            }
        });
        exportMainMenu.add(exportToCSV);

        fileMenu.add(exportMainMenu);

        menuBar.add(fileMenu);

        runMenu.setText("Run");

        mnuRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/light.png"))); // NOI18N
        mnuRun.setText("Start");
        mnuRun.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuRunActionPerformed(evt);
            }
        });
        runMenu.add(mnuRun);

        mnuCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lightCancel.png"))); // NOI18N
        mnuCancel.setText("Cancel");
        mnuCancel.setEnabled(false);
        mnuCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuCancelActionPerformed(evt);
            }
        });
        runMenu.add(mnuCancel);

        menuBar.add(runMenu);

        mnuView.setText("View");

        vissrMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/bar-chart-02.png"))); // NOI18N
        vissrMenuItem.setText("Show Genome View");
        vissrMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                vissrMenuItemActionPerformed(evt);
            }
        });
        mnuView.add(vissrMenuItem);

        menuBar.add(mnuView);

        helpMenu.setText("Help");

        loadHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        loadHelp.setText("Contents");
        helpMenu.add(loadHelp);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 972, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 741, Short.MAX_VALUE)
                                .add(cmdStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cmdCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(10, 10, 10)))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(lblStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(43, 43, 43))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(2, 2, 2)
                .add(lblStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmdStart)
                    .add(cmdCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
    {//GEN-HEADEREND:event_exitMenuItemActionPerformed

      this.setVisible( false );
      this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void mnuRunActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuRunActionPerformed
    {//GEN-HEADEREND:event_mnuRunActionPerformed

      this.runProcedure();
    }//GEN-LAST:event_mnuRunActionPerformed

  private boolean clearWindows()
  {
    return 
      mainResultsContainer.clearAll();
  }

	private void mnuCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuCancelActionPerformed
    {//GEN-HEADEREND:event_mnuCancelActionPerformed

      cancel();
	}//GEN-LAST:event_mnuCancelActionPerformed
@Override
  public void shutdown()
  {
     cancel();
   this.dispose();
  }
    private void cmdStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdStartActionPerformed
    {//GEN-HEADEREND:event_cmdStartActionPerformed

      runProcedure();
	}//GEN-LAST:event_cmdStartActionPerformed

    private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cmdCancelActionPerformed
    {//GEN-HEADEREND:event_cmdCancelActionPerformed

      cancel();

	}//GEN-LAST:event_cmdCancelActionPerformed

  private void vissrMenuItemActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_vissrMenuItemActionPerformed
  {//GEN-HEADEREND:event_vissrMenuItemActionPerformed

    Object[] options =
    {
      "Continue with VisSR Generation",
      "Cancel operation"
    };
    int n = JOptionPane.showOptionDialog( this,
      "Warning, this operation can take a long time to complete, do you wish to continue?",
      "SiLoCo Warning",
      JOptionPane.YES_NO_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE,
      null,
      options,
      options[1] );
    if ( n == 0 )
    {
      showInVisSR();
    }
    
      
    }//GEN-LAST:event_vissrMenuItemActionPerformed

  private void exportToCSVActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_exportToCSVActionPerformed
  {//GEN-HEADEREND:event_exportToCSVActionPerformed
    File csvFile = FileDialogUtils.showFileSaveDialog(this, FileExtFilter.CSV.getFilter() );
      if ( csvFile != null )
      {
        
        this.mySiLoco.outputCSVFileFromTable( csvFile );
      }
  }//GEN-LAST:event_exportToCSVActionPerformed

  
  public void showInVisSR()
  {
    //mySiLoco.generateVisSR();
    //throw new UnsupportedException();

  }

  private void cancel()
  {
    mySiLoco.cancelRun();
    this.exportMainMenu.setEnabled( false );
  }

  
  private SiLoCoExpressionMatrixProcess mySiLoco = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdStart;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu exportMainMenu;
    private javax.swing.JMenuItem exportToCSV;
    private javax.swing.JMenu fileMenu;
    private uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel filterResultsPanel;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel fspGenome;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel fspInput;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JMenuItem loadHelp;
    private javax.swing.JProgressBar mainProgressBar;
    private uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoResultsPanel mainResultsContainer;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnuCancel;
    private javax.swing.JMenuItem mnuRun;
    private javax.swing.JMenu mnuView;
    private javax.swing.JMenu runMenu;
    private javax.swing.JMenuItem vissrMenuItem;
    // End of variables declaration//GEN-END:variables
  private SiLoCoParamsGUI paramsPanel;

  
  // ***** Start GUIInterface methods *****
  @Override
  public void runProcedure()
  {
    if ( this.paramsPanel.createParams() && clearWindows() && fspGenome.getFile() != null && !fspInput.getFiles().isEmpty() )
    {
      
      
      ArrayList<ArrayList<File>> inputData = new ArrayList<ArrayList<File>>();
      for(File file : fspInput.getFiles())
      {
        ArrayList<File> newList = new ArrayList<File>();
        newList.add( file );
        this.filterResultsPanel.addOutputTab( file.getName() );
        inputData.add( newList );
      }
      mySiLoco.inputSampleData(inputData  );
      mySiLoco.inputGenomeFile( fspGenome.getFile() );
      mySiLoco.inputParams( paramsPanel.getParams() );
      mySiLoco.inputFilterResultsPanel( filterResultsPanel );
      mySiLoco.inputRefs( mainResultsContainer, this.mainProgressBar, this.lblStatus, paramsPanel.getLog() );
      mySiLoco.setListener( this );
      new Thread( mySiLoco ).start();
    }
    else
    {
      JOptionPane.showMessageDialog( new JFrame(),
        "SiLoCo Configuration Options Not Set Correctly.",
        "SiLoCo Configuration error",
        JOptionPane.ERROR_MESSAGE );
    }
  }

  @Override
  public JPanel getParamsPanel()
  {
    return paramsPanel;
  }

  @Override
  public void setShowingParams( boolean newState )
  {
    showingParams = newState;
  }

  @Override
  public boolean getShowingParams()
  {
    return showingParams;
  }

  // ***** End GUIInterface methods *****
  @Override
  public void notifyOfThreadCompletion()
  {
    if ( mySiLoco.failed() )
    {
      JOptionPane.showMessageDialog( this,
        this.mySiLoco.getErrorMessage(),
        "SiLoCo Error",
        JOptionPane.ERROR_MESSAGE );
      this.setCursor( Cursor.getDefaultCursor() );
      this.exportMainMenu.setEnabled( false );
    }
    else
    {
      this.exportMainMenu.setEnabled( true );
    }
  }
}