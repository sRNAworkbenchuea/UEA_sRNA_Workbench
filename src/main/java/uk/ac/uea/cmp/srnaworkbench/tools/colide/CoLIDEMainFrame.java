/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.awt.Cursor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter;
import uk.ac.uea.cmp.srnaworkbench.utils.GenerateWaitCursor;
import uk.ac.uea.cmp.srnaworkbench.utils.GoController;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import uk.ac.uea.cmp.srnaworkbench.help.JHLauncher;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;

/**
 *
 * @author w0445959
 */
public class CoLIDEMainFrame extends javax.swing.JInternalFrame implements GUIInterface, ToolHost
{
  private final StatusTracker tracker;
  private GoController go_control;
  private CoLIDEParamBrowser myParamsGUI = new CoLIDEParamBrowser();
  CoLIDEParams params = null;
  private boolean myShowingParams = false;
  private ArrayList<ArrayList<File>> myFileList = new ArrayList<ArrayList<File>>(0);
  
  private CoLIDEResultsPanel coLIDEResultsTable;
  private int currentSample = 0;
  private CoLIDERunner coLIDERunner;
  /**
   * Creates new form CoLIDEMainFrame
   */
  public CoLIDEMainFrame()
  {
    
    
    initComponents();
    this.tracker = new StatusTracker( coLIDEProgressBar, coLIDEStatusLabel );
    JHLauncher.setupContextDependentHelp( "HTML_CoLIDE_html", mnuHelpContents, this.getRootPane() );
    
    Tools.trackPage( "CoLIDE Main GUI Frame Class Loaded");
  }
  public boolean setup()
  {
    boolean result = false;
    String sampleCountString = JOptionPane.showInputDialog( this.getContentPane(), "How many samples are you analysing", "CoLIDE Setup", JOptionPane.QUESTION_MESSAGE );
    if(sampleCountString != null)
    {
      int sampleCount = Integer.parseInt( sampleCountString );
      if ( sampleCount >= 2 )
      {
        for ( int i = 0; i < sampleCount; i++ )
        {
          JScrollPane listScroller = new JScrollPane();

          sampleListTabPane.addTab( ( "Sample" + ( i + 1 ) ), listScroller ); // NOI18N

          myFileList.add( new ArrayList<File>() );
        }
        sampleListTabPane.addChangeListener( new ChangeListener()
        {
          // This method is called whenever the selected tab changes
          @Override
          public void stateChanged( ChangeEvent evt )
          {
            JTabbedPane pane = (JTabbedPane) evt.getSource();

            // Get current tab
            int sel = pane.getSelectedIndex();
            currentSampleNumberLbl.setText( "Adding files to sample number: " + ( sel + 1 ) );
          }
        } );
        coLIDEResultsTable = new CoLIDEResultsPanel();

        //this.resultsScrollPane.setViewportView( coLIDEResultsTable );
        //this.resultsPanel.add(coLIDEResultsTable);

        javax.swing.GroupLayout resultsPanelLayout = new javax.swing.GroupLayout( coLIDEOutput );
        coLIDEOutput.setLayout( resultsPanelLayout );
        resultsPanelLayout.setHorizontalGroup(
          resultsPanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( coLIDEResultsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE ) );
        resultsPanelLayout.setVerticalGroup(
          resultsPanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( coLIDEResultsTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE ) );
        this.go_control = new GoController( this.cmdStart, this.cmdCancel, this.mnuRunStart, this.mnuRunCancel );

        int widths[] =
        {
          fspInput.getLabelWidth(),
          fspGenome.getLabelWidth()
        };

        int maxWidth = Integer.MIN_VALUE;
        for ( int currentWidth : widths )
        {
          if ( currentWidth > maxWidth )
          {
            maxWidth = currentWidth;
          }
        }
        fspInput.setLabelWidth( maxWidth );
        fspGenome.setLabelWidth( maxWidth );

        //this.invalidate();


        fspInput.setFilters( new FileNameExtensionFilter[]
          {
            FileExtFilter.FASTA.getFilter()
          } );
        fspGenome.setFilters( new FileNameExtensionFilter[]
          {
            FileExtFilter.FASTA.getFilter()
          } );
        setRunningStatus( false );

        result = true;
      }
      else
      {
        JOptionPane.showMessageDialog( this.getContentPane(), "Sample Count must be two or higher", "Configuration Error", JOptionPane.ERROR_MESSAGE );
        this.setVisible( false );
        this.dispose();
        result = false;
      }
    
    }
    else
    {
      JOptionPane.showMessageDialog( this.getContentPane(), "Must know sample count to continue", "Configuration Error", JOptionPane.ERROR_MESSAGE ); 
      this.setVisible( false);
      this.dispose();
    }
    return result;
  
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        mainCoLIDEResults = new javax.swing.JTabbedPane();
        sampleListTabPane = new javax.swing.JTabbedPane();
        filterResultsPanel = new uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel();
        coLIDEOutput = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        coLIDEStatusLabel = new javax.swing.JLabel();
        coLIDEProgressBar = new javax.swing.JProgressBar();
        cmdStart = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        fileIOPanel = new javax.swing.JPanel();
        fspInput = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        fspGenome = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        addRepToSample = new javax.swing.JButton();
        currentSampleNumberLbl = new javax.swing.JLabel();
        resetSamples = new javax.swing.JButton();
        mnuMain = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        exportMnu = new javax.swing.JMenu();
        exportCSVMnu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuFileExit = new javax.swing.JMenuItem();
        mnuRun = new javax.swing.JMenu();
        mnuRunStart = new javax.swing.JMenuItem();
        mnuRunCancel = new javax.swing.JMenuItem();
        mnuRunSep1 = new javax.swing.JPopupMenu.Separator();
        mnuRunReset = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuHelpContents = new javax.swing.JMenuItem();

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.title")); // NOI18N
        setAutoscrolls(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/CoLIDESmall.png"))); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));

        mainCoLIDEResults.setBackground(new java.awt.Color(120, 120, 120));
        mainCoLIDEResults.setAutoscrolls(true);

        sampleListTabPane.setBackground(new java.awt.Color(120, 120, 120));
        sampleListTabPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.sampleListTabPane.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N
        sampleListTabPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        sampleListTabPane.setAutoscrolls(true);
        sampleListTabPane.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        mainCoLIDEResults.addTab(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.sampleListTabPane.TabConstraints.tabTitle"), sampleListTabPane); // NOI18N

        filterResultsPanel.setBackground(new java.awt.Color(120, 120, 120));
        mainCoLIDEResults.addTab(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.filterResultsPanel.TabConstraints.tabTitle"), filterResultsPanel); // NOI18N

        coLIDEOutput.setBackground(new java.awt.Color(120, 120, 120));

        javax.swing.GroupLayout coLIDEOutputLayout = new javax.swing.GroupLayout(coLIDEOutput);
        coLIDEOutput.setLayout(coLIDEOutputLayout);
        coLIDEOutputLayout.setHorizontalGroup(
            coLIDEOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 925, Short.MAX_VALUE)
        );
        coLIDEOutputLayout.setVerticalGroup(
            coLIDEOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 257, Short.MAX_VALUE)
        );

        mainCoLIDEResults.addTab(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.coLIDEOutput.TabConstraints.tabTitle"), coLIDEOutput); // NOI18N

        controlPanel.setBackground(new java.awt.Color(120, 120, 120));
        controlPanel.setMaximumSize(new java.awt.Dimension(32767, 97));

        coLIDEStatusLabel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        coLIDEStatusLabel.setForeground(new java.awt.Color(255, 255, 255));
        coLIDEStatusLabel.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.coLIDEStatusLabel.text")); // NOI18N

        cmdStart.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdStart.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.cmdStart.text")); // NOI18N
        cmdStart.setToolTipText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.cmdStart.toolTipText")); // NOI18N
        cmdStart.setEnabled(false);
        cmdStart.setMaximumSize(new java.awt.Dimension(69, 27));
        cmdStart.setMinimumSize(new java.awt.Dimension(69, 27));
        cmdStart.setPreferredSize(new java.awt.Dimension(69, 27));
        cmdStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdStartActionPerformed(evt);
            }
        });

        cmdCancel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdCancel.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.cmdCancel.text")); // NOI18N
        cmdCancel.setToolTipText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.cmdCancel.toolTipText")); // NOI18N
        cmdCancel.setEnabled(false);
        cmdCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(coLIDEProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(coLIDEStatusLabel)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cmdStart, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(cmdCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(coLIDEStatusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(coLIDEProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdStart, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fileIOPanel.setBackground(new java.awt.Color(120, 120, 120));
        fileIOPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.fileIOPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N
        fileIOPanel.setMaximumSize(new java.awt.Dimension(32767, 239));

        fspInput.setFileLineAmount(16);
        fspInput.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.SRNA);
        fspInput.setLabel(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.fspInput.label")); // NOI18N
        fspInput.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.MULTI_LOAD);
        fspInput.setToolName(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.fspInput.toolName")); // NOI18N

        fspGenome.setFileLineAmount(1);
        fspGenome.setHistorySingleMode(true);
        fspGenome.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.GENOME);
        fspGenome.setLabel(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.fspGenome.label")); // NOI18N
        fspGenome.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.LOAD);
        fspGenome.setToolName(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.fspGenome.toolName")); // NOI18N

        addRepToSample.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        addRepToSample.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.addRepToSample.text")); // NOI18N
        addRepToSample.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addRepToSampleActionPerformed(evt);
            }
        });

        currentSampleNumberLbl.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        currentSampleNumberLbl.setForeground(new java.awt.Color(255, 255, 255));
        currentSampleNumberLbl.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.currentSampleNumberLbl.text")); // NOI18N

        resetSamples.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        resetSamples.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.resetSamples.text")); // NOI18N
        resetSamples.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                resetSamplesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fileIOPanelLayout = new javax.swing.GroupLayout(fileIOPanel);
        fileIOPanel.setLayout(fileIOPanelLayout);
        fileIOPanelLayout.setHorizontalGroup(
            fileIOPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileIOPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fileIOPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileIOPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(currentSampleNumberLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addRepToSample, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetSamples, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fspInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fspGenome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        fileIOPanelLayout.setVerticalGroup(
            fileIOPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileIOPanelLayout.createSequentialGroup()
                .addComponent(fspInput, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fspGenome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileIOPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentSampleNumberLbl)
                    .addComponent(addRepToSample, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetSamples, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainCoLIDEResults)
            .addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(fileIOPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(fileIOPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainCoLIDEResults, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jScrollPane1.setViewportView(jPanel1);

        getContentPane().add(jScrollPane1);

        mnuMain.setBackground(new java.awt.Color(213, 219, 245));

        mnuFile.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuFile.text")); // NOI18N

        exportMnu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        exportMnu.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.exportMnu.text")); // NOI18N

        exportCSVMnu.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.exportCSVMnu.text")); // NOI18N
        exportCSVMnu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportCSVMnuActionPerformed(evt);
            }
        });
        exportMnu.add(exportCSVMnu);

        mnuFile.add(exportMnu);
        mnuFile.add(jSeparator1);

        mnuFileExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/close-tool.png"))); // NOI18N
        mnuFileExit.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuFileExit.text")); // NOI18N
        mnuFileExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuFileExitActionPerformed(evt);
            }
        });
        mnuFile.add(mnuFileExit);

        mnuMain.add(mnuFile);

        mnuRun.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuRun.text")); // NOI18N

        mnuRunStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/light.png"))); // NOI18N
        mnuRunStart.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuRunStart.text")); // NOI18N
        mnuRunStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuRunStartActionPerformed(evt);
            }
        });
        mnuRun.add(mnuRunStart);

        mnuRunCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lightCancel.png"))); // NOI18N
        mnuRunCancel.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuRunCancel.text")); // NOI18N
        mnuRunCancel.setEnabled(false);
        mnuRunCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuRunCancelActionPerformed(evt);
            }
        });
        mnuRun.add(mnuRunCancel);
        mnuRun.add(mnuRunSep1);

        mnuRunReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/reload.png"))); // NOI18N
        mnuRunReset.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuRunReset.text")); // NOI18N
        mnuRunReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuRunResetActionPerformed(evt);
            }
        });
        mnuRun.add(mnuRunReset);

        mnuMain.add(mnuRun);

        mnuHelp.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuHelp.text")); // NOI18N

        mnuHelpContents.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        mnuHelpContents.setText(org.openide.util.NbBundle.getMessage(CoLIDEMainFrame.class, "CoLIDEMainFrame.mnuHelpContents.text")); // NOI18N
        mnuHelp.add(mnuHelpContents);

        mnuMain.add(mnuHelp);

        setJMenuBar(mnuMain);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void cmdStartActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdStartActionPerformed
  {//GEN-HEADEREND:event_cmdStartActionPerformed
    runProcedure();
  }//GEN-LAST:event_cmdStartActionPerformed

  private void mnuFileExitActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuFileExitActionPerformed
  {//GEN-HEADEREND:event_mnuFileExitActionPerformed
    cancel();
    dispose();
  }//GEN-LAST:event_mnuFileExitActionPerformed

  private void mnuRunStartActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuRunStartActionPerformed
  {//GEN-HEADEREND:event_mnuRunStartActionPerformed
    runProcedure();
  }//GEN-LAST:event_mnuRunStartActionPerformed

  private void mnuRunCancelActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuRunCancelActionPerformed
  {//GEN-HEADEREND:event_mnuRunCancelActionPerformed
    cancel();
  }//GEN-LAST:event_mnuRunCancelActionPerformed

  private void mnuRunResetActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuRunResetActionPerformed
  {//GEN-HEADEREND:event_mnuRunResetActionPerformed
    reset();
  }//GEN-LAST:event_mnuRunResetActionPerformed

  private void addRepToSampleActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_addRepToSampleActionPerformed
  {//GEN-HEADEREND:event_addRepToSampleActionPerformed
    addToSamples();
  }//GEN-LAST:event_addRepToSampleActionPerformed

  private void resetSamplesActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_resetSamplesActionPerformed
  {//GEN-HEADEREND:event_resetSamplesActionPerformed
    int index = sampleListTabPane.getSelectedIndex();
      
      if(myFileList.get( index ).isEmpty())
      {
        JOptionPane.showMessageDialog( this.getContentPane(), "No files to clear for this sample", "No Action Performed", JOptionPane.INFORMATION_MESSAGE);
      }
      else
      {
        myFileList.get( index ).clear();
        JScrollPane tabComponentAt = (JScrollPane)sampleListTabPane.getSelectedComponent( );
        tabComponentAt.setViewportView( null );
      }
      
  }//GEN-LAST:event_resetSamplesActionPerformed

  private void exportCSVMnuActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_exportCSVMnuActionPerformed
  {//GEN-HEADEREND:event_exportCSVMnuActionPerformed
    exportCSV();
  }//GEN-LAST:event_exportCSVMnuActionPerformed

  private void cmdCancelActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdCancelActionPerformed
  {//GEN-HEADEREND:event_cmdCancelActionPerformed
    cancel();
  }//GEN-LAST:event_cmdCancelActionPerformed

  private void exportCSV()
  {
    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileExtFilter.CSV.getFilter() );
    if ( outputFile != null )
    {
      exportMainTable( outputFile );
    }
  }
  
   private void exportMainTable( File output )
  {
    FileWriter outCSVFile = null;
    PrintWriter out = null;
    try
    {
      outCSVFile = new FileWriter( output );
      out = new PrintWriter( outCSVFile );
      // Write text to file
      String sampleDataString = "";
      String header = "Locus ID,Start,End,Locus Length,P Value,";
      int sampleID=1;
      for(ArrayList<File> sampleFiles : myFileList)
      {
        header += "Sample: " + sampleID + ",";
        sampleDataString += "Sample " + sampleID + " contains files: ";
        for(File file : sampleFiles)
        {
          sampleDataString += file.getName() + " ; ";
        }
        sampleDataString += LINE_SEPARATOR;
        sampleID++;
      }
      
      header += "Chromosome, Differential Expression" + LINE_SEPARATOR;
      out.println(sampleDataString);
      out.println();
      out.println(header);
      Outline outline = coLIDEResultsTable.getOutline();
      DefaultOutlineModel model = (DefaultOutlineModel)coLIDEResultsTable.getOutline().getModel();
      OutlineNode treeModel = (OutlineNode)model.getRoot();
      int chromosomeCount = treeModel.getChildCount();
      

      for(int i = 0; i < chromosomeCount; i++ )
      {
        OutlineNode chromosomeModel = (OutlineNode)treeModel.getChildAt( i );
        //out.println("Chromso")
        int locusCount = chromosomeModel.getChildCount();
        for(int locusID = 0; locusID < locusCount; locusID++)
        {
          OutlineNode locusData = (OutlineNode)chromosomeModel.getChildAt( locusID);
          List<String> userObject = locusData.getUserObject();
          for(String data : userObject)
          {
            out.print( data + ",");
          }
          //double
          out.println();
        }
        //Object child = model.getChild( model.g, i );
        //          model.get
      }
//      for ( int i = 0; i < coLIDEResultsTable.getOutline().getModel().getRowCount(); i++ )
//      {
//        
////        for ( int j = 0; j < coLIDEResultsTable.getOutline().getModel().getRowCount(); j++ )
////        {
////
////          out.print( coLIDEResultsTable.getOutline().getValueAt( i, j).toString()  + "," );
////        }
//        out.println();
//      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outCSVFile.close();
        out.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }
 
  private void addToSamples()
  {
    final ArrayList<File> files = this.fspInput.getFiles();
    if ( !files.isEmpty() )
    {
      int index = sampleListTabPane.getSelectedIndex();
      
      this.myFileList.get( index).addAll( files );
      JScrollPane tabComponentAt = (JScrollPane)sampleListTabPane.getSelectedComponent( );
      DefaultListModel listModel = new DefaultListModel();
      
      for(File fileName : myFileList.get( index ))
      {
        this.filterResultsPanel.addOutputTab( fileName.getName() );
        listModel.addElement( fileName.getAbsolutePath() );
      }
  


      JList nextSampleList = new JList(  );
      
      nextSampleList.setBackground( new java.awt.Color( 120, 120, 120 ) );
      nextSampleList.setFont( new java.awt.Font( "Lucida Sans Unicode", 0, 12 ) ); // NOI18N
      nextSampleList.setForeground( new java.awt.Color( 255, 255, 255 ) );
      //nextSampleList.setPreferredSize( d );
      nextSampleList.setModel( listModel );
      

      tabComponentAt.setViewportView( nextSampleList );
      
      if(index +1 < sampleListTabPane.getTabCount())
        sampleListTabPane.setSelectedIndex( index +1 );
      
      myParamsGUI.incrementSampleNumber();
      fspInput.clear();
      currentSample++;

     
    }
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRepToSample;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdStart;
    private javax.swing.JPanel coLIDEOutput;
    private javax.swing.JProgressBar coLIDEProgressBar;
    private javax.swing.JLabel coLIDEStatusLabel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JLabel currentSampleNumberLbl;
    private javax.swing.JMenuItem exportCSVMnu;
    private javax.swing.JMenu exportMnu;
    private javax.swing.JPanel fileIOPanel;
    private uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel filterResultsPanel;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel fspGenome;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel fspInput;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane mainCoLIDEResults;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenuItem mnuFileExit;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuHelpContents;
    private javax.swing.JMenuBar mnuMain;
    private javax.swing.JMenu mnuRun;
    private javax.swing.JMenuItem mnuRunCancel;
    private javax.swing.JMenuItem mnuRunReset;
    private javax.swing.JPopupMenu.Separator mnuRunSep1;
    private javax.swing.JMenuItem mnuRunStart;
    private javax.swing.JButton resetSamples;
    private javax.swing.JTabbedPane sampleListTabPane;
    // End of variables declaration//GEN-END:variables

  @Override
  public void runProcedure()
  {

 
    params = myParamsGUI.getParams();
    coLIDERunner = new CoLIDERunner( this );
    coLIDERunner.reset();
    this.coLIDEResultsTable.resetAll();
    //this.filterResultsPanel.resetTable();
    coLIDERunner.runCoLIDETool( myFileList, fspGenome.getFile(),
      params, this.filterResultsPanel, tracker, coLIDEResultsTable );
   
    
    


  }

  @Override
  public JPanel getParamsPanel()
  {
    return myParamsGUI;
  }

  @Override
  public void setShowingParams( boolean newState )
  {
    myShowingParams = newState;
  }

  @Override
  public boolean getShowingParams()
  {
    return myShowingParams;
  }

  @Override
  public void shutdown()
  {
    
  }

  @Override
  public void update()
  {
    
  }

  @Override
  public void setRunningStatus( boolean running )
  {
    if ( this.go_control != null )
    {
      if ( !running )
      {
        this.setCursor( Cursor.getDefaultCursor() );
      }
      else
      {
        new Thread( new GenerateWaitCursor( this ) ).start();
      }

      this.go_control.setRunning( running );

      this.mnuRunReset.setEnabled( !running );
    }
  }

  @Override
  public void showErrorDialog( String message )
  {
     JOptionPane.showMessageDialog( this,
      message,
      "Pre-processing error",
      JOptionPane.ERROR_MESSAGE );
  }

  private void cancel()
  {
    if(coLIDERunner != null)
      coLIDERunner.cancel();
  }

  private void reset()
  {
    filterResultsPanel.resetTable();
    for(ArrayList<File> sampleList : myFileList)
    {
      for ( File fileName : sampleList )
      {
        this.filterResultsPanel.setResultsWaiting( fileName.getName(), true );
      }
    }
//    this.coLIDEResultsTable.resetAll();
//    this.mainCoLIDEResults.getComponents();
    if(coLIDERunner != null)
      coLIDERunner.reset();
//    setVisible(false);
//    dispose();
//    setup();
  }

  
}
