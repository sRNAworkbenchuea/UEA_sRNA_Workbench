/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MirCat.java
 *
 * Created on 19-Oct-2010, 11:00:18
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRange;
import uk.ac.uea.cmp.srnaworkbench.help.JHLauncher;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.rnaannotation.RNAannotationMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultilineCellRenderer;

/**
 *
 * @author w0445959
 */
public final class MirCatMainFrame extends JInternalFrame implements GUIInterface, ToolHost
{
  private boolean showingParams = false;
  private GoController go_control;
  private File genomeFile;
  private MiRCatRunner m_runner;
  private MiRCatOpenFileOptions open_options = null;
  private MirCatParamsGui paramsPanel;
  private RNAannotationMainFrame HA_tool = null;
  private SequenceVizMainFrame SV_tool = null;
  private static final int MAX_ANNOTATION_SEQUENCES = 50;
  private MiRCatSplashScreen splashScreen = new MiRCatSplashScreen();
  private Point mousePoint;

  /**
   * Creates new form MirCat
   */
  public MirCatMainFrame()
  {
    initComponents();

    paramsPanel = new MirCatParamsGui( this );

    paramsPanel.setVisible( false );

    mirnaPanel.setVisible( true );

    m_runner = new MiRCatRunner( this, mirCAT_OutputTable, paramsPanel.getTracker() );


//
//        mirCAT_OutputTable.getTableHeader().setFont( new Font( "Lucida Sans Unicode" , Font.TRUETYPE_FONT, 12 ));
//        mirCAT_OutputTable.getTableHeader().setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 204, 255),
//                new java.awt.Color(153, 254, 255), new java.awt.Color(255, 255, 255), new java.awt.Color(200, 200, 200)));
//
//

    MultilineCellRenderer textAreaRenderer = new MultilineCellRenderer();
    mirCAT_OutputTable.setDefaultRenderer( String.class, new MultilineCellRenderer() );
    mirCAT_OutputTable.setDefaultRenderer( Integer.class, new MultilineCellRenderer() );
    mirCAT_OutputTable.setDefaultRenderer( Float.class, new MultilineCellRenderer() );
    
    mirCAT_OutputTable.addMouseListener( new java.awt.event.MouseAdapter()
    {
      @Override
      public void mouseClicked( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }

      @Override
      public void mousePressed( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }

      @Override
      public void mouseReleased( java.awt.event.MouseEvent evt )
      {
        checkPopup( evt );
      }
    } );
//
    //TableColumnModel main_model = mirCAT_OutputTable.getColumnModel();
    setupColumnsInView( mirCAT_OutputTable );

    JHLauncher.setupContextDependentHelp( "HTML_miRCat_html", loadHelp, this.getRootPane() );

    go_control = new GoController( this.cmdStart, this.cmdCancel, this.runFromMenu, this.cancelFromMenu );

    setRunningStatus( false );
    
    showAllResultsInVisSR.setVisible(false);
    
    Tools.trackPage( "mirCat Main Frame Loaded");
  }

  public final TableColumn[] getColumnsInView( JTable table )
  {
    TableColumn[] result = new TableColumn[table.getColumnCount()];

    // Use an enumeration
    Enumeration e = table.getColumnModel().getColumns();
    for ( int i = 0; e.hasMoreElements(); i++ )
    {
      result[i] = (TableColumn) e.nextElement();
    }

    return result;
  }

  public final void setupColumnsInView( JTable table )
  {


      

    // Use an enumeration
    Enumeration e = table.getColumnModel().getColumns();
    for ( int i = 0; e.hasMoreElements(); i++ )
    {
      MultilineCellRenderer textAreaRenderer = new MultilineCellRenderer();
      ( (TableColumn) e.nextElement() ).setCellRenderer( textAreaRenderer );
    }


  }

  public void checkTutorialWindow()
  {
    try
    {
      File f = new File( Tools.userDataDirectoryPath + DIR_SEPARATOR + "UserSettings" );

      BufferedReader inUserDat = new BufferedReader( new FileReader( f ) );


      String thisLine = "";
      boolean foundMirCatSplash = false;
      while ( ( thisLine = inUserDat.readLine() ) != null )
      {
        //System.out.println(thisLine);
        if ( thisLine.startsWith( "miRCaT_Preferences" ) )
        {
          thisLine = inUserDat.readLine();
          if ( thisLine.equals( "true" ) )
          {
            //System.out.println(myOpenTools.searchTools("mircat"));
            if ( ToolManager.getInstance().searchTools( "mircat" ) <= 1 )
            {
              LOGGER.log( Level.FINE, "MIRCAT: creating window!" );
              this.mirnaPanel.setVisible( false );
              mainToolBar.setVisible( false );
              this.splashScreen.setVisible( true );
              //this.add(splash);
              //this.pack();

            }
            else
            {
              mirnaPanel.setVisible( true );
              mainToolBar.setVisible( true );
              this.splashScreen.setVisible( false );
            }
          }
        }
      }
//
//        FileWriter outFile = new FileWriter(Tools.curDir + Tools.FILE_SEPARATOR + "output" + Tools.FILE_SEPARATOR + "output.csv", true);
//
//        PrintWriter outputCSV = new PrintWriter(outFile);
//        System.out.println(f + (f.exists() ? " is found " : " is missing "));

    }
    catch ( IOException e )
    {
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

        popupMenu = new javax.swing.JPopupMenu();
        renderFullLocus = new javax.swing.JMenuItem();
        renderSingleHairpin = new javax.swing.JMenuItem();
        mainToolBar = new javax.swing.JToolBar();
        searchTextBox = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        saveCSV = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        outputMiRNAFASTA = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        outputHairpinTxt = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        renderAllHairpins = new javax.swing.JButton();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        showAllResultsInVisSR = new javax.swing.JButton();
        mirnaPanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        mirCAT_OutputTable = new javax.swing.JTable();
        cmdStart = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        exportMenu = new javax.swing.JMenu();
        exportCSV = new javax.swing.JMenuItem();
        exportFASTA = new javax.swing.JMenuItem();
        exportHairpins = new javax.swing.JMenuItem();
        exportGFF = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        runMenu = new javax.swing.JMenu();
        runFromMenu = new javax.swing.JMenuItem();
        cancelFromMenu = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        loadHelp = new javax.swing.JMenuItem();

        renderFullLocus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        renderFullLocus.setText("Show Locus in Genome View");
        renderFullLocus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                renderFullLocusActionPerformed(evt);
            }
        });
        popupMenu.add(renderFullLocus);

        renderSingleHairpin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/newHAIRPIN.jpg"))); // NOI18N
        renderSingleHairpin.setText("Render Hairpin");
        renderSingleHairpin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                renderSingleHairpinActionPerformed(evt);
            }
        });
        popupMenu.add(renderSingleHairpin);

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("miRCat ");
        setAutoscrolls(true);
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/mirCAT_Icon.jpg"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(910, 33));
        setPreferredSize(new java.awt.Dimension(1000, 792));
        setVisible(true);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener()
        {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt)
            {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt)
            {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt)
            {
                closeFrame(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt)
            {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt)
            {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt)
            {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt)
            {
            }
        });

        mainToolBar.setBackground(new java.awt.Color(120, 120, 120));
        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        searchTextBox.setBackground(new java.awt.Color(120, 120, 120));
        searchTextBox.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        searchTextBox.setForeground(new java.awt.Color(255, 255, 255));
        searchTextBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        searchTextBox.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                searchTextBoxKeyPressed(evt);
            }
        });
        mainToolBar.add(searchTextBox);

        searchButton.setBackground(new java.awt.Color(255, 255, 255));
        searchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/search.png"))); // NOI18N
        searchButton.setBorder(null);
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                searchButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(searchButton);
        mainToolBar.add(filler1);

        saveCSV.setText("Export Results to CSV");
        saveCSV.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveCSVActionPerformed(evt);
            }
        });
        mainToolBar.add(saveCSV);
        mainToolBar.add(filler2);

        outputMiRNAFASTA.setText("Export miRNAs to FASTA");
        outputMiRNAFASTA.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputMiRNAFASTAActionPerformed(evt);
            }
        });
        mainToolBar.add(outputMiRNAFASTA);
        mainToolBar.add(filler3);

        outputHairpinTxt.setText("Output Hairpins");
        outputHairpinTxt.setFocusable(false);
        outputHairpinTxt.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        outputHairpinTxt.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputHairpinTxt.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputHairpinTxtActionPerformed(evt);
            }
        });
        mainToolBar.add(outputHairpinTxt);
        mainToolBar.add(filler5);

        renderAllHairpins.setText("Render all Hairpins");
        renderAllHairpins.setFocusable(false);
        renderAllHairpins.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        renderAllHairpins.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        renderAllHairpins.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                renderAllHairpinsActionPerformed(evt);
            }
        });
        mainToolBar.add(renderAllHairpins);
        mainToolBar.add(filler6);
        mainToolBar.add(filler4);

        showAllResultsInVisSR.setText("Show All Results In VisSR");
        showAllResultsInVisSR.setFocusable(false);
        showAllResultsInVisSR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        showAllResultsInVisSR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        showAllResultsInVisSR.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showAllResultsInVisSRActionPerformed(evt);
            }
        });
        mainToolBar.add(showAllResultsInVisSR);

        mirnaPanel.setBackground(new java.awt.Color(120, 120, 120));
        mirnaPanel.setAutoscrolls(true);

        tableScrollPane.setBackground(new java.awt.Color(120, 120, 120));
        tableScrollPane.setAutoscrolls(true);

        mirCAT_OutputTable.setAutoCreateRowSorter(true);
        mirCAT_OutputTable.setBackground(new java.awt.Color(120, 120, 120));
        mirCAT_OutputTable.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        mirCAT_OutputTable.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        mirCAT_OutputTable.setForeground(new java.awt.Color(255, 255, 255));
        mirCAT_OutputTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Chromosome", "Sequence", "miRNA*", "Hairpin Sequence", "miRNA Start Coordinate", "miRNA End Coordinate", "Orientation", "Abundance", "sRNA Length", "Hairpin Length", "Hairpin Start", "Hairpin End", "Hairpin Structure", "Genomic Hits", "Minimum Free Energy (MFE)", "Adjusted MFE", "Hairpin G/C %", "miRNA* start coord", "miRNA* end coord", "miRBase ID", "P_Val"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Float.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        mirCAT_OutputTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        mirCAT_OutputTable.setFillsViewportHeight(true);
        mirCAT_OutputTable.setGridColor(new java.awt.Color(153, 204, 255));
        mirCAT_OutputTable.setRowMargin(10);
        mirCAT_OutputTable.setSelectionBackground(new java.awt.Color(102, 102, 102));
        mirCAT_OutputTable.setSelectionForeground(new java.awt.Color(153, 204, 255));
        tableScrollPane.setViewportView(mirCAT_OutputTable);
        if (mirCAT_OutputTable.getColumnModel().getColumnCount() > 0)
        {
            mirCAT_OutputTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            mirCAT_OutputTable.getColumnModel().getColumn(1).setPreferredWidth(250);
            mirCAT_OutputTable.getColumnModel().getColumn(2).setPreferredWidth(250);
            mirCAT_OutputTable.getColumnModel().getColumn(3).setPreferredWidth(500);
            mirCAT_OutputTable.getColumnModel().getColumn(4).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(5).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(6).setPreferredWidth(100);
            mirCAT_OutputTable.getColumnModel().getColumn(7).setPreferredWidth(100);
            mirCAT_OutputTable.getColumnModel().getColumn(8).setPreferredWidth(100);
            mirCAT_OutputTable.getColumnModel().getColumn(9).setPreferredWidth(120);
            mirCAT_OutputTable.getColumnModel().getColumn(12).setPreferredWidth(500);
            mirCAT_OutputTable.getColumnModel().getColumn(13).setPreferredWidth(100);
            mirCAT_OutputTable.getColumnModel().getColumn(14).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(15).setPreferredWidth(220);
            mirCAT_OutputTable.getColumnModel().getColumn(16).setPreferredWidth(100);
            mirCAT_OutputTable.getColumnModel().getColumn(17).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(18).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(19).setPreferredWidth(200);
            mirCAT_OutputTable.getColumnModel().getColumn(20).setPreferredWidth(200);
        }

        cmdStart.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdStart.setText("Start");
        cmdStart.setToolTipText("<html> Start processing. </html>");
        cmdStart.setEnabled(false);
        cmdStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdStartActionPerformed(evt);
            }
        });

        cmdCancel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmdCancel.setText("Cancel");
        cmdCancel.setToolTipText("<html> Cancel process. </html>");
        cmdCancel.setEnabled(false);
        cmdCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmdCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mirnaPanelLayout = new javax.swing.GroupLayout(mirnaPanel);
        mirnaPanel.setLayout(mirnaPanelLayout);
        mirnaPanelLayout.setHorizontalGroup(
            mirnaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 976, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mirnaPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmdStart, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mirnaPanelLayout.setVerticalGroup(
            mirnaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mirnaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mirnaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmdCancel)
                    .addComponent(cmdStart))
                .addContainerGap())
        );

        menuBar.setBackground(new java.awt.Color(213, 219, 245));

        fileMenu.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/add-item.png"))); // NOI18N
        openMenuItem.setText("New Project");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        exportMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        exportMenu.setText("Export");
        exportMenu.setEnabled(false);

        exportCSV.setText("Export Results to CSV");
        exportCSV.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportCSVActionPerformed(evt);
            }
        });
        exportMenu.add(exportCSV);

        exportFASTA.setText("Export miRNAs to FASTA");
        exportFASTA.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportFASTAActionPerformed(evt);
            }
        });
        exportMenu.add(exportFASTA);

        exportHairpins.setText("Export Hairpins to FASTA");
        exportHairpins.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportHairpinsActionPerformed(evt);
            }
        });
        exportMenu.add(exportHairpins);

        exportGFF.setText("Export GFF records");
        exportGFF.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportGFFActionPerformed(evt);
            }
        });
        exportMenu.add(exportGFF);

        fileMenu.add(exportMenu);
        fileMenu.add(jSeparator1);

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

        menuBar.add(fileMenu);

        runMenu.setText("Run");

        runFromMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/light.png"))); // NOI18N
        runFromMenu.setText("Run");
        runFromMenu.setEnabled(false);
        runFromMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                runFromMenuActionPerformed(evt);
            }
        });
        runMenu.add(runFromMenu);

        cancelFromMenu.setIcon(new javax.swing.ImageIcon("C:\\gitrepos\\maven\\Workbench\\src\\main\\resources\\images\\SharedImages\\lightCancel.png")); // NOI18N
        cancelFromMenu.setText("Cancel");
        cancelFromMenu.setEnabled(false);
        cancelFromMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelFromMenuActionPerformed(evt);
            }
        });
        runMenu.add(cancelFromMenu);

        menuBar.add(runMenu);

        helpMenu.setText("Help");

        loadHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        loadHelp.setText("Contents");
        helpMenu.add(loadHelp);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mirnaPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mirnaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void exportMainTable( File output )
  {
    FileWriter outCSVFile = null;
    PrintWriter out = null;
    try
    {
      outCSVFile = new FileWriter( output );
      out = new PrintWriter( outCSVFile );
      // Write text to file
      out.println( "Chromosome,"
        + "Sequence,"
        + "miRNA*,"
        + "Hairpin Sequence,"
        + "Start,"
        + "End,"
        + "Orientation,"
        + "Abundance,"
        + "sRNA length,"
        + "Hairpin Length,"
        + "Hairpin Start,"
        + "Hairpin End,"
        + "Hairpin Structure,"
        + "Genomic Hits,"
        + "Minimum Free Energy(MFE),"
        + "Adjusted MFE,"
        + "Hairpin G/C%,"
        + "miRNA* Start Coord,"
        + "miRNA* End Coord,"
        + "miRBase ID,"
        + "P_Val," );
      for ( int i = 0; i < this.mirCAT_OutputTable.getRowCount(); i++ )
      {
        for ( int j = 0; j < this.mirCAT_OutputTable.getColumnCount(); j++ )
        {

          out.print( StringUtils.removeHTML( mirCAT_OutputTable.getValueAt( i, j ).toString() ) + "," );
        }
        out.println();
      }
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

  private void exportMiRNAs( File output )
  {
    FileWriter outCSVFile = null;
    try
    {
      outCSVFile = new FileWriter( output );
      PrintWriter out = new PrintWriter( outCSVFile );
      // Write text to file

      for ( int i = 0; i < this.mirCAT_OutputTable.getRowCount(); i++ )
      {

        out.println( ">" + mirCAT_OutputTable.getValueAt( i, 1 ) + "(" + mirCAT_OutputTable.getValueAt( i, 7 ) + ")" );

        out.println( mirCAT_OutputTable.getValueAt( i, 1 ) );
      }
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
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }

  }
    private void outputHairpinTxtActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_outputHairpinTxtActionPerformed
    {//GEN-HEADEREND:event_outputHairpinTxtActionPerformed
      //File outputFile = FileDialogUtils.showFileSaveDialog(this);
      exportHairpinsFile();

    }//GEN-LAST:event_outputHairpinTxtActionPerformed

    private void closeFrame(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_closeFrame
    {//GEN-HEADEREND:event_closeFrame
     shutdown();
    }//GEN-LAST:event_closeFrame

  @Override
  public void shutdown()
  {
    this.setVisible( false );
    terminateRun();
    ensureThreadShutdown();
    //paramsPanel.setVisible( false );
   //( (MDIDesktopPane) getParent() )
    AppUtils.INSTANCE.getMainMDIWindow().hideParameterBrowser();
    this.dispose();
  }

   private void ensureThreadShutdown()
  {
    
    
  }
  public void terminateRun()
  {
    this.m_runner.cancel();
    this.m_runner.getEngine().clearAllData();
    //this.m_runner.getEngine().terminateThreads();


    //TODO maybe clear the output as well?
  }

    private void renderAllHairpinsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_renderAllHairpinsActionPerformed
    {//GEN-HEADEREND:event_renderAllHairpinsActionPerformed

      //System.out.println("here");
      new Thread( new GenerateWaitCursor( this.getParent() ) ).start();
      try
      {
        renderAllHairpins();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
      //System.out.println(" now here");
      this.getParent().setCursor( Cursor.getDefaultCursor() );

    }//GEN-LAST:event_renderAllHairpinsActionPerformed

  private void showAllInVisSR()
  {

    TierParameters[] generateVissrTP = new TierParameters[2];
    generateVissrTP[0] = new TierParameters.Builder( "miRNA Loci" ).build();
    generateVissrTP[1] = new TierParameters.Builder( "miRNA GFF" ).tierLabelBackgroundColour( Color.yellow ).glyphBackgroundColour( Color.GREEN ).build();

    for ( int row = 0; row < mirCAT_OutputTable.getRowCount(); row++ )
    {

      int start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 4 ).toString() );
      int end = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 5 ).toString() );
      this.m_runner.getEngine().generateVissrTP( start, end, row, generateVissrTP);
    }

    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( genomeFile, false, (TierParameters) null );
    
    vissr.addTier(generateVissrTP[0]);
    vissr.addTier(generateVissrTP[1]);

    AppUtils.INSTANCE.activateFrame(vissr);

  }

  private void renderAllHairpins() throws IOException
  {
    if ( HA_tool == null )
    {
      HA_tool = new RNAannotationMainFrame();
      
      //HA_tool.setVisible(true);
      ToolManager.getInstance().addTool( HA_tool );

    }

    final HairpinGenerateMainFrame generator = new HairpinGenerateMainFrame( mirCAT_OutputTable.getRowCount() );
    generator.setVisible( true);
    ( (MDIDesktopPane) getParent() ).add(generator);
    ( (MDIDesktopPane) getParent() ).activateFrame( generator );

    new Thread(
      new Runnable()
      {
        @Override
        public void run()
        {
          HA_tool.clearAllCurrentImages();
          boolean outputToDisc = false;
          File toUse = null;
          FileWriter outImageFile = null;
          PrintWriter outImageWriter = null;
          String selectedExtension[] =
          {
            ""
          };

          if ( mirCAT_OutputTable.getRowCount() >= MAX_ANNOTATION_SEQUENCES )
          {
            JOptionPane.showMessageDialog( null,
              "Large amount of results detected." + LINE_SEPARATOR + "Image files will need to be output to disk",
              "Render all hairpins memory warning",
              JOptionPane.INFORMATION_MESSAGE );
            outputToDisc = true;
            String[] availableFormats = FileTypeFilter.getImageFormats();


            File saveFile = FileDialogUtils.showFileSaveDialog( null, availableFormats, "Select Image Format", true, selectedExtension );
            if ( saveFile != null )
            {
              //System.out.println(Tools.getExtension(saveFile));
              //System.out.println("format: " + selectedExtension[0]);

              if ( saveFile.getAbsolutePath().contains( selectedExtension[0] ) )
              {
                toUse = new File( saveFile.getAbsolutePath() );
              }
              else
              {
                toUse = new File( saveFile.getAbsolutePath() + selectedExtension[0] );
              }
            }
            try
            {
              outImageFile = new FileWriter( new File( FilenameUtils.removeExtension( toUse.getAbsolutePath() ) + DIR_SEPARATOR
                + "HAIRPIN_LEGEND" + ".txt" ) );
            }
            catch ( IOException ex )
            {
              LOGGER.log( Level.SEVERE,  ex.getMessage() );
            }
            outImageWriter = new PrintWriter( outImageFile );
            outImageWriter.println( "File type selected: " + selectedExtension[0] );
            outImageWriter.println( "Total hairpins rendered: " + mirCAT_OutputTable.getRowCount() );
            outImageWriter.println( "-----------------------------" );
          }
          for ( int i = 0; i < mirCAT_OutputTable.getRowCount(); i++ )
          {
            generator.tracker.increment();
            String hairpinSequenceHTML = mirCAT_OutputTable.getValueAt( i, 3 ).toString();
            String hairpinDotBracketHTML = mirCAT_OutputTable.getValueAt( i, 12 ).toString();


            int miRNALength = mirCAT_OutputTable.getModel().getValueAt( i, 1 ).toString().length();

            int miRNASTAR_Start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( i, 17 ).toString() );
            int miRNASTAR_End = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( i, 18 ).toString() );
            String MFE = mirCAT_OutputTable.getModel().getValueAt( i, 18 ).toString();
            //System.out.println(hairpinSequenceHTML + " " + hairpinDotBracketHTML);

            if ( !outputToDisc )
            {
              HA_tool.generateAndAdd( hairpinSequenceHTML, hairpinDotBracketHTML, miRNALength, miRNASTAR_End - miRNASTAR_Start, MFE,
                mirCAT_OutputTable.getValueAt( i, 6 ).toString() );
            }
            else
            {

              String fileName = FilenameUtils.removeExtension( toUse.getAbsolutePath() ) + DIR_SEPARATOR
                + "HAIRPIN_" + i + "." + selectedExtension[0];
              LOGGER.log( Level.FINE, "filename: {0}", fileName );
              File updatedFileName = new File( fileName );
              outImageWriter.println( "Image name:" + updatedFileName.getName() );
              outImageWriter.println( "Hairpin Sequence:" + StringUtils.removeHTML( hairpinSequenceHTML ) );
              outImageWriter.println( "-----------------------------" );
              outImageWriter.println();

              HA_tool.generateAndSave( hairpinSequenceHTML, hairpinDotBracketHTML, miRNALength, miRNASTAR_End - miRNASTAR_Start,
                updatedFileName, selectedExtension );

            }


          }
          generator.dispose();
          if ( outImageFile != null )
          {
            try
            {
              outImageFile.close();
            }
            catch ( IOException ex )
            {
             LOGGER.log( Level.SEVERE,  ex.getMessage() );
            }
          }
          if ( outImageWriter != null )
          {
            outImageWriter.close();
          }
          HA_tool.setVisible( true );

          if ( outputToDisc )
          {
            HA_tool.openImagesFromDisc( new File( FilenameUtils.removeExtension( toUse.getAbsolutePath() ) ), selectedExtension[0] );
          }
          ( (MDIDesktopPane) getParent() ).add( HA_tool );
          ( (MDIDesktopPane) getParent() ).activateFrame( HA_tool );

        }
      } ).start();

    //
  }
    private void outputMiRNAFASTAActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_outputMiRNAFASTAActionPerformed
    {//GEN-HEADEREND:event_outputMiRNAFASTAActionPerformed
      exportFASTA();
}//GEN-LAST:event_outputMiRNAFASTAActionPerformed

    private void saveCSVActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveCSVActionPerformed
    {//GEN-HEADEREND:event_saveCSVActionPerformed

      exportCSV();
}//GEN-LAST:event_saveCSVActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_searchButtonActionPerformed
    {//GEN-HEADEREND:event_searchButtonActionPerformed
      searchTable( searchTextBox.getText() );
}//GEN-LAST:event_searchButtonActionPerformed

    private void searchTextBoxKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_searchTextBoxKeyPressed
    {//GEN-HEADEREND:event_searchTextBoxKeyPressed
      if ( evt.isActionKey() )
      {
        searchTable( searchTextBox.getText() );
      }
}//GEN-LAST:event_searchTextBoxKeyPressed

private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
  openFile();
}//GEN-LAST:event_openMenuItemActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
  terminateRun();
  dispose();
}//GEN-LAST:event_exitMenuItemActionPerformed

private void exportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCSVActionPerformed
  exportCSV();
}//GEN-LAST:event_exportCSVActionPerformed

private void exportFASTAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportFASTAActionPerformed
  exportFASTA();
}//GEN-LAST:event_exportFASTAActionPerformed

private void exportHairpinsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportHairpinsActionPerformed
  exportHairpinsFile();
}//GEN-LAST:event_exportHairpinsActionPerformed
  private void exportFASTA()
  {
    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileExtFilter.FASTA.getFilter() );
    if ( outputFile != null )
    {
      exportMiRNAs( outputFile );
    }
  }

  private void exportHairpinsFile()
  {
    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileExtFilter.FASTA.getFilter() );
    if ( outputFile != null )
    {
      exportHairpins( outputFile );
    }
  }

  private void exportCSV()
  {
    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileExtFilter.CSV.getFilter() );
    if ( outputFile != null )
    {
      exportMainTable( outputFile );
    }
  }
private void runFromMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runFromMenuActionPerformed
  runProcedure();
}//GEN-LAST:event_runFromMenuActionPerformed

private void cancelFromMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelFromMenuActionPerformed
  this.terminateRun();
}//GEN-LAST:event_cancelFromMenuActionPerformed

private void showAllResultsInVisSRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllResultsInVisSRActionPerformed
  showAllInVisSR();

}//GEN-LAST:event_showAllResultsInVisSRActionPerformed

private void exportGFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGFFActionPerformed
  File outputFile = FileDialogUtils.showFileSaveDialog( this, FileExtFilter.GFF.getFilter() );
  if ( outputFile != null )
  {
    exportGFF( outputFile );
  }
}//GEN-LAST:event_exportGFFActionPerformed

  private void cmdStartActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdStartActionPerformed
  {//GEN-HEADEREND:event_cmdStartActionPerformed
    runProcedure();
  }//GEN-LAST:event_cmdStartActionPerformed

  private void cmdCancelActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmdCancelActionPerformed
  {//GEN-HEADEREND:event_cmdCancelActionPerformed
    terminateRun();
  }//GEN-LAST:event_cmdCancelActionPerformed

  private void checkPopup(java.awt.event.MouseEvent evt)
  {
    if ( evt.isPopupTrigger() )
    {
      mousePoint = evt.getPoint();
      this.popupMenu.show( mirCAT_OutputTable, evt.getX(), evt.getY());
      int row = this.mirCAT_OutputTable.rowAtPoint( mousePoint );
      int column = mirCAT_OutputTable.columnAtPoint( mousePoint );
      if ( !mirCAT_OutputTable.isRowSelected( row ) )
      {
        mirCAT_OutputTable.changeSelection( row, column, false, false );
        
      }
    }
  }
  private void renderFullLocusActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_renderFullLocusActionPerformed
  {//GEN-HEADEREND:event_renderFullLocusActionPerformed

    this.m_runner.getEngine().generateVissrArrow( false, mousePoint);
  }//GEN-LAST:event_renderFullLocusActionPerformed

  private void renderSingleHairpinActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_renderSingleHairpinActionPerformed
  {//GEN-HEADEREND:event_renderSingleHairpinActionPerformed
    if ( HA_tool == null )
    {
      HA_tool = new RNAannotationMainFrame();
      
      //HA_tool.setVisible(true);
      ToolManager.getInstance().addTool( HA_tool );

    }

    int row = mirCAT_OutputTable.rowAtPoint( mousePoint );


    row = this.mirCAT_OutputTable.convertRowIndexToModel( mirCAT_OutputTable.getSelectedRow() );
    
    
      String hairpinSequenceHTML = mirCAT_OutputTable.getValueAt( row, 3 ).toString();
      String hairpinDotBracketHTML = mirCAT_OutputTable.getValueAt( row, 12 ).toString();


      int miRNALength = mirCAT_OutputTable.getModel().getValueAt( row, 1 ).toString().length();

      int miRNASTAR_Start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 17 ).toString() );
      int miRNASTAR_End = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 18 ).toString() );
      String MFE = mirCAT_OutputTable.getModel().getValueAt( row, 18 ).toString();
      //System.out.println(hairpinSequenceHTML + " " + hairpinDotBracketHTML);
        HA_tool.generateAndAdd( hairpinSequenceHTML, hairpinDotBracketHTML, miRNALength, miRNASTAR_End - miRNASTAR_Start, MFE,
          mirCAT_OutputTable.getValueAt( row, 6 ).toString() );
      
        HA_tool.setVisible( true );
     ( (MDIDesktopPane) getParent() ).add( HA_tool );
          ( (MDIDesktopPane) getParent() ).activateFrame( HA_tool );

  }//GEN-LAST:event_renderSingleHairpinActionPerformed

  private void exportGFF( File output )
  {

    FileWriter outGFFFile = null;
    PrintWriter out = null;
    try
    {
      outGFFFile = new FileWriter( output );
      out = new PrintWriter( outGFFFile );
      // Write text to file

      for ( int row = 0; row < mirCAT_OutputTable.getRowCount(); row++ )
      {
        String ID = mirCAT_OutputTable.getModel().getValueAt( row, 0 ).toString();

        int start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 4 ).toString() );
        int end = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 5 ).toString() );
        char strand = mirCAT_OutputTable.getModel().getValueAt( row, 6 ).toString().charAt( 0 );

        String hairpinSequenceHTML = mirCAT_OutputTable.getModel().getValueAt( row, 3 ).toString();
        String hairpinDotBracketHTML = mirCAT_OutputTable.getModel().getValueAt( row, 12 ).toString();

        String hairpinSequence = hairpinSequenceHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" ).replace( "<u><font color=#0000FF>", "" ).replace( "</u></font>", "" ).replace( "<u><font color=#FF0000>", "" ).replace( "<font color=#FFFFFF>", "" ).replace( "</u>", "" );
        String hairpinDotBracket = hairpinDotBracketHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" );
        //String finalHairpinDotBracket = (((((hairpinDotBracket.replace("-", ".")).replace("<", "(")).replace(">", ")")).replace("{", "(")).replace("}", ")")).replace("=", ".");

        GFFRecord record_miRNA = new GFFRecord( ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte) 0 );
        record_miRNA.addAttribute( "hairpin_element", "miRNA" );

        //out.println( record_miRNA.toGFFFileEntry() );

        start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 10 ).toString() );
        end = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 11 ).toString() );

        GFFRecord recordHairpin = new GFFRecord( ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte) 0 );
        recordHairpin.addAttribute( "Hairpin Sequence", hairpinSequence );
        recordHairpin.addAttribute( "Marked up Dot-Bracket", hairpinDotBracket );

        out.println( recordHairpin.toGFFFileEntry() );
        out.println( record_miRNA.toGFFFileEntry() );

        start = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 17 ).toString() );
        end = Integer.parseInt( mirCAT_OutputTable.getModel().getValueAt( row, 18 ).toString() );

        if ( start >= 0 && end > 0 )
        {
          GFFRecord recordSTAR = new GFFRecord( ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte) 0 );
          recordSTAR.addAttribute( "hairpin_element", "miRNA*" );

          out.println( recordSTAR.toGFFFileEntry() );
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outGFFFile.close();
        out.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }



  }

  private void exportHairpins( File output )
  {
    FileWriter outHairpinFile = null;
    PrintWriter out = null;
    try
    {
      outHairpinFile = new FileWriter( output );
      out = new PrintWriter( outHairpinFile );
      // Write text to file

      for ( int i = 0; i < this.mirCAT_OutputTable.getRowCount(); i++ )
      {
        String miRSTAR = "Not Found";
        String[] valueAt = mirCAT_OutputTable.getValueAt( i, 2 ).toString().split( " ");
        int currentMax = Integer.MIN_VALUE;
        for(String STAR : valueAt)
        {
          String[] data_and_abund = STAR.split("\\(");
          if ( data_and_abund.length > 1)
          {
            String abundString =  data_and_abund[1].replaceAll( "\\)", "" );
            int abundance = StringUtils.safeIntegerParse(abundString, 0 );
            if ( abundance > currentMax )
            {
              currentMax = abundance;
              miRSTAR = data_and_abund[0].replace( "\\(", "" );
            }
          }
        }
        out.println( ">maturemiRNA_" + mirCAT_OutputTable.getValueAt( i, 1 ) + "_miRNA*_"+ miRSTAR + "_" + 
          mirCAT_OutputTable.getValueAt( i, 0 ) + "_PRECURSOR-START_" + mirCAT_OutputTable.getValueAt( i, 10 ) + 
          "_END_" + mirCAT_OutputTable.getValueAt( i, 11 ) + "_MFE_" + mirCAT_OutputTable.getValueAt( i, 14 ) + "_STRAND_" + mirCAT_OutputTable.getValueAt( i, 6 ).toString());
        out.println( StringUtils.removeHTML( mirCAT_OutputTable.getValueAt( i, 3 ).toString() ) );
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outHairpinFile.close();
        out.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
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

  private void searchTable( String toFind )
  {
    if ( toFind.isEmpty() )
    {
      resetSearchBox();
      return;

    }
    for ( int i = 0; i < this.mirCAT_OutputTable.getRowCount(); i++ )
    {
      for ( int j = 0; j < this.mirCAT_OutputTable.getColumnCount(); j++ )
      {
        if ( mirCAT_OutputTable.getColumnClass( j ).getName().equals( "java.lang.String" ) )
        {
          //System.out.println("found string in column: " + j);

          String next = mirCAT_OutputTable.getValueAt( i, j ).toString();
          String toTest = StringUtils.removeHTML( next );
//                if (next instanceof String)
//                {
          if ( toTest.equals( (String) toFind ) )
          {
            // System.out.println("found");
            resetSearchBox();

            showSearchResults( i, j );
            return;
          }
        }
        else
        {
          if ( mirCAT_OutputTable.getColumnClass( j ).getName().equals( "java.lang.Integer" ) && ( Character.isDigit( toFind.charAt( 0 ) ) ) )
          {

            //System.out.println("found integer in column: " + j);
            if ( ( (Integer) mirCAT_OutputTable.getValueAt( i, j ) ).compareTo( Integer.parseInt( toFind ) ) == 0 )
            {
              //System.out.println("found");
              resetSearchBox();

              showSearchResults( i, j );
              return;

            }

          }
          else
          {
            if ( mirCAT_OutputTable.getColumnClass( j ).getName().equals( "java.lang.Float" ) && ( Character.isDigit( toFind.charAt( 0 ) ) ) )
            {
              //System.out.println("found float in column: " + j);
              if ( ( (Float) mirCAT_OutputTable.getValueAt( i, j ) ).compareTo( Float.parseFloat( toFind ) ) == 0 )
              {
                //System.out.println("found");
                resetSearchBox();
                showSearchResults( i, j );
                return;

              }

            }
          }
        }


      }
    }

    // reset table display after failed search
    //        > target null/not found <

    JOptionPane.showMessageDialog( this,
      "Required search value was not found",
      "miRCat search results",
      JOptionPane.INFORMATION_MESSAGE );
    resetSearchBox();



  }

  public void resetSearchBox()
  {
    MultilineCellRenderer renderer =
      (MultilineCellRenderer) mirCAT_OutputTable.getDefaultRenderer( String.class );
    renderer.setTargetCell( -1, -1 );

    renderer =
      (MultilineCellRenderer) mirCAT_OutputTable.getDefaultRenderer( Integer.class );
    renderer.setTargetCell( -1, -1 );

    renderer =
      (MultilineCellRenderer) mirCAT_OutputTable.getDefaultRenderer( Float.class );
    renderer.setTargetCell( -1, -1 );
    mirCAT_OutputTable.repaint();

  }

  private void showSearchResults( int row, int col )
  {
    MultilineCellRenderer renderer = (MultilineCellRenderer) mirCAT_OutputTable.getCellRenderer( row, col );
    renderer.setTargetCell( row, col );
//    Rectangle r = mirCAT_OutputTable.getCellRect( row, col, true );
    mirCAT_OutputTable.changeSelection( row, col, false, false );
//    mirCAT_OutputTable.scrollRectToVisible( r );
//    mirCAT_OutputTable.repaint();
    JViewport viewport = (JViewport) mirCAT_OutputTable.getParent();

    // This rectangle is relative to the table where the
    // northwest corner of cell (0,0) is always (0,0).
    Rectangle rect = mirCAT_OutputTable.getCellRect( row, col, true );

    // The location of the viewport relative to the table
    Point pt = viewport.getViewPosition();

    // Translate the cell location so that it is relative
    // to the view, assuming the northwest corner of the
    // view is (0,0)
    rect.setLocation( rect.x - pt.x, rect.y - pt.y );

    // Scroll the area into view
    viewport.scrollRectToVisible( rect );
  }

  @Override
  public String toString()
  {
    return "mircat";


  }

  public void setupEngine( MiRCatOpenFileOptions fileOpenOptions )
  {
    //mirCAT_OutputTable.addMouseListener( new MiRCatPopUpMenuListener( getParent(), fileOpenOptions.getGenomeFile() ) );

    if ( fileOpenOptions.isRemovingAdaptors() )
    {
      paramsPanel.addTab( fileOpenOptions.getARResultsPanel(), "Adapter Removal Results" );
    }
    if ( fileOpenOptions.isFiltering() )
    {
      paramsPanel.addTab( fileOpenOptions.getFilterResultsPanel(), "Filter Results Panel" );
    }
    paramsPanel.inputRefs( fileOpenOptions );
    paramsPanel.enableRun();

    this.genomeFile = fileOpenOptions.getGenomeFile();

    open_options = fileOpenOptions;
  }

  /**
   *
   * @return
   */
  public boolean openFile()
  {

    if ( clearWindows() )
    {
      JFrame temp = new JFrame();
      temp.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
      MiRCatOpenFileOptions fileOpenOptions = new MiRCatOpenFileOptions( temp, "File Open Options", "pipeline configuration menu" );

      fileOpenOptions.setVisible( true );
      //fileOpenOptions.invalidate();
      //fileOpenOptions.revalidate();
      if ( fileOpenOptions.getValue() == JOptionPane.OK_OPTION )
      {

        setupEngine( fileOpenOptions );

        mirnaPanel.setVisible( true );
        mainToolBar.setVisible( true );
//
        splashScreen.setVisible( false );
        showingParams = true;
        ( (MDIDesktopPane) this.getParent() ).showParams( this );
        this.exportMenu.setEnabled( true );
        this.runFromMenu.setEnabled( true );
        this.cancelFromMenu.setEnabled( true );
      }

    }
    return false;

  }

  public boolean clearWindows()
  {
    if ( mirCAT_OutputTable.getRowCount() > 0 )
    {
      int result = JOptionPane.showConfirmDialog( this,
        "Information found in table will be cleared." + LINE_SEPARATOR + "Do you wish to continue?",
        "Opening new file",
        JOptionPane.WARNING_MESSAGE );
      if ( result == JOptionPane.YES_OPTION )
      {

        resetSearchBox();
        DefaultTableModel dm = (DefaultTableModel) mirCAT_OutputTable.getModel();
        dm.getDataVector().removeAllElements();
        return true;
      }
      else
      {
        return false;
      }
    }
    return true;
  }

  /**
   *
   */
  @Override
  public void runProcedure()
  {
    
    MiRCatParams params = paramsPanel.getParams();
    paramsPanel.beginRun();
    m_runner.runPipeline( params, open_options );
  }

  /**
   *
   * @return
   */
  @Override
  public JPanel getParamsPanel()
  {
    //paramsPanel.setVisible(true);
    return paramsPanel;
  }

  public Process_Hits_Patman getEngine()
  {
    return this.m_runner.getEngine();
  }

  // ***** ToolHost implementations *****
  @Override
  public void update()
  {
    this.m_runner.getEngine().clearAllData();
  }

  @Override
  public void setRunningStatus( boolean running )
  {
    if ( !running )
    {
      this.mirCAT_OutputTable.setCursor( Cursor.getDefaultCursor() );
    }
    else
    {
      new Thread( new GenerateWaitCursor( this.mirCAT_OutputTable ) ).start();
    }

    if ( this.go_control != null )
    {
      this.go_control.setRunning( running );
    }
  }

  @Override
  public void showErrorDialog( String message )
  {
    JOptionPane.showMessageDialog( this,
      message,
      "miRCat Error",
      JOptionPane.ERROR_MESSAGE );
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem cancelFromMenu;
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdStart;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportCSV;
    private javax.swing.JMenuItem exportFASTA;
    private javax.swing.JMenuItem exportGFF;
    private javax.swing.JMenuItem exportHairpins;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem loadHelp;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTable mirCAT_OutputTable;
    private javax.swing.JPanel mirnaPanel;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JButton outputHairpinTxt;
    private javax.swing.JButton outputMiRNAFASTA;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JButton renderAllHairpins;
    private javax.swing.JMenuItem renderFullLocus;
    private javax.swing.JMenuItem renderSingleHairpin;
    private javax.swing.JMenuItem runFromMenu;
    private javax.swing.JMenu runMenu;
    private javax.swing.JButton saveCSV;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchTextBox;
    private javax.swing.JButton showAllResultsInVisSR;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

  void setRunEnabled( boolean b )
  {
    this.runFromMenu.setEnabled( b );
  }

 
}
