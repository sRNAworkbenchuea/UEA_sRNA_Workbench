package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.util.logging.Level;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.help.JHLauncher;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class Degradome extends JInternalFrame implements GUIInterface, FeedbackI
{
    /** The panel containing all of the PARESnip parameters*/
    private Parameters paramsPanel;

    /**Small RNA file.**/
    private File srnaFile;

    /**Degradome file.**/
    private File degradomeFile;

    /**Transcripts file.**/
    private File transcriptomeFile;

    /**Optional genome file.**/
    private File genomeFile;

    /**The primary save file.**/
    private File saveAsFile;

    /**The secondary save file.**/
    private File secondarySaveAsFile = null;

    /**For rendering duplexes in the gui table.**/
    private PARESnipJTableCellRenderer duplexRender;

    /**Flag to say if parameters browser is showing.**/
    private boolean showingParams;

    /**srnaFile file selector object for use with choosing files.**/
    private ChooseDataDialog fileSelector;

    /** The collection of secondary hits.**/
    private HashMap<String, ArrayList<String[]>> secondaryHits;

    /** The configuration file if provided.**/
    private File configFile;

    /**The output file.**/
    private File outputFile;

    /**The engine. **/
    private Engine engine;

    /**The parameters**/
    private final ParesnipParams params = new ParesnipParams();

    /** Creates new form GuiInternalFrame */
    public Degradome()
    {
        duplexRender = new PARESnipJTableCellRenderer();
        initComponents();
        initMoreComponents();
        //showingParams = true;
        secondaryHits = new HashMap<String,ArrayList<String[]>>();
        pack();

          JHLauncher.getInstance().addContextToButton( miHelp, "HTML_paresnip_html" );
          JHLauncher.getInstance().enableHelpKey( this.getRootPane(), "HTML_paresnip_html" );
          
          Tools.trackPage( "PAREsnip Main GUI Frame Class Loaded");
        }

    /**
     * Constructor - typically used when in command-line mode
     */
    public Degradome( File srnaFile, File degradomeFile, File transcriptomeFile, File genomeFile, File outFile, File configFile )
    {
        this.srnaFile = srnaFile;
        this.degradomeFile = degradomeFile;
        this.transcriptomeFile = transcriptomeFile;
        this.genomeFile = genomeFile;
        this.outputFile = outFile;
        this.configFile = configFile;


        if (outputFile == null) {
            System.err.println("No file for output ( -out file ) has been provided - please refer to the user manual and check the parameters.");
            System.exit(1);
        }

        if (srnaFile == null) {
            System.err.println("Small RNA file not provided - please refer to the user manual and check the parameters.");
            System.exit(1);
        }
        if ( ! (srnaFile.exists() && srnaFile.canRead() && srnaFile.isFile()) )
        {
            System.err.println("Something wrong with srna file");
            System.exit(1);
        }

        if (degradomeFile == null) {
            System.err.println("Degradome file not provided - please refer to the user manual and check the parameters.");
            System.exit(1);
        }
        if ( ! (degradomeFile.exists() && degradomeFile.canRead() && degradomeFile.isFile()) )
        {
            System.err.println("Something wrong with degradome file");
            System.exit(1);
        }

        if (transcriptomeFile == null) {
            System.err.println("Transcripts file not provided - please refer to the user manual and check the parameters.");
            System.exit(1);
        }

        if ( ! (transcriptomeFile.exists() && transcriptomeFile.canRead() && transcriptomeFile.isFile()) )
        {
            System.err.println("Something wrong with transcripts file");
            System.exit(1);
        }

        if (genomeFile == null) {
            System.out.println("Genome not provided - no mapping to genome.");
        } else if ( !(genomeFile != null && genomeFile.exists() && genomeFile.canRead() && genomeFile.isFile()) )
        {
            System.err.println("Something wrong with genome file");
        }

        if (configFile == null) {
            System.out.println("Configuration file not provided - using default settings.");
        } else if ( !(configFile != null && configFile.exists() && configFile.canRead() && configFile.isFile()) )
        {
            System.err.println("Something wrong with configFile file");
        }

        if ( configFile != null )
        {
          try
          {
            ParesnipParams.load( params, configFile );
          }
          catch ( IOException ex )
          {
            System.err.print( "An error occurred while reading the contents of the parameters file. Error : " + ex.getMessage() );
            System.exit( 1 );
          }
        }

        // Output the list of files
        System.out.println( "Small RNA file     : " + srnaFile.getName() );
        System.out.println( "Transcriptome file : " + transcriptomeFile.getName() );
        System.out.println( "Degradome file     : " + degradomeFile.getName() );
        System.out.println( "Genome file        : " + ( genomeFile == null ? "<omitted>" : genomeFile.getName() ) );
        
        Tools.trackPage( "PAREsnip Main Procedure Class Loaded");

//        if (configFile != null) {
//            //We have some config input - do something with it...
//            try {
//                BufferedReader r = new BufferedReader(new FileReader(configFile));
//                String line;
//                line = r.readLine();
//
//                while (line != null) {
//                    //Check to see if this is an empty line in the file..
//                    if (line.trim().isEmpty()) {
//                        //It is, so just move onto the next line. - Makes cmd line more robust.
//                        line = r.readLine();
//                        continue;
//                    }
//
//                    String[] params = line.split("=");
//                    params[0] = params[0].trim();
//                    params[1] = params[1].trim();

//                    if (params[0].equalsIgnoreCase("Small RNA Abundance Cutoff")) {
//                        Data.smallRnaAbundanceCutOff = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Use Weighted Fragment Abundance")) {
//                        Data.isUsingWeightedAbundance = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Category 0")) {
//                        Data.c0 = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Category 1")) {
//                        Data.c1 = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Category 2")) {
//                        Data.c2 = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Category 3")) {
//                        Data.c3 = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Category 4")) {
//                        Data.c4 = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Remove Low Complexity Sequences")) {
//                        Data.Deg_Tool_removeLowComplexity = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Remove Low Complexity Candidates")) {
//                        Data.deg_tool_filter_candidates_low_complexity = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Min Fragment Length")) {
//                        Data.Deg_Tool_minFragLength = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Max Fragment Length")) {
//                        Data.Deg_Tool_maxFragLength = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Min Sequence Length")) {
//                        Data.minimumSequenceLength = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Max Sequence Length")) {
//                        Data.maximumSequenceLength = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Using Gaps")) {
//                        Data.usingGaps = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Calculate P-Values")) {
//                        Data.usingPvalues = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Number of Shuffles")) {
//                        Data.totalShuffles = Integer.parseInt(params[1]);
//                    } else if (params[0].equalsIgnoreCase("P-Value Cut Off")) {
//                        Data.pValuCutOff = Double.parseDouble(params[1]);
//                    } else if (params[0].equalsIgnoreCase("Do Not Include Results > Cut Off")) {
//                        Data.notReportingResultsAboveCutoff = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("Threads")) {
//                        Data.threadCount = Tools.getThreadCount();
//                    } else if (params[0].equalsIgnoreCase("Print Alignments")) {
//                        Data.Deg_Tool_printingAlignmentsInCMD = params[1].equalsIgnoreCase("true");
//                    } else if (params[0].equalsIgnoreCase("CmdVerbose")) {
//                        //Data.cmdVerbose = params[1].equalsIgnoreCase("true");NOT A USER OPTION!!
//                    } else {
//                        System.err.println("An unknown parameter was provided in the configuration file. "
//                                + "Please see the user manual and check the configuration file.");
//                        System.err.println("Unknown parameter: " + params[0]);
//                        System.err.println("Exiting...");
//                        System.exit(1);
//                    }
//                    line = r.readLine();
//                }
//                r.close();
//            }
//            catch (IOException ex)
//            {
//                System.err.println("Exception.");
//                System.err.println(ex.toString());
//                System.err.println("Exiting...");
//                System.exit(1);
//                Logger.getLogger(Degradome.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        Data.Deg_Tool_CMD_LINE = true;
//
//        System.out.println("\n\nSETTINGS:");
//        System.out.println("Small RNA File: " + srnaFile.getAbsolutePath());
//        System.out.println("Degradome File: " + degradomeFile.getAbsolutePath());
//        System.out.println("Transcripts File: " + transcriptomeFile.getAbsolutePath());
//        if (genomeFile != null) {
//            System.out.println("Genome File: " + genomeFile.getAbsolutePath());
//        }
//        if (configFile != null) {
//            System.out.println("Configuration File: " + configFile.getAbsolutePath());
//        }
//        System.out.println("Small RNA Abundance Cutoff: " + Data.smallRnaAbundanceCutOff);
//        System.out.println("Use Weighted Fragment Abundance: " + Data.isUsingWeightedAbundance);
//        System.out.println("Category 0: " + Data.c0);
//        System.out.println("Category 1: " + Data.c1);
//        System.out.println("Category 2: " + Data.c2);
//        System.out.println("Category 3: " + Data.c3);
//        System.out.println("Category 4: " + Data.c4);
//        System.out.println("Remove Low Complexity Sequences: " + Data.Deg_Tool_removeLowComplexity);
//        System.out.println("Remove Low Complexity Candidates: " + Data.deg_tool_filter_candidates_low_complexity);
//        System.out.println("Min Fragment Length: " + Data.Deg_Tool_minFragLength);
//        System.out.println("Max Fragment Length: " + Data.Deg_Tool_maxFragLength);
//        System.out.println("Min Sequence Length: " + Data.minimumSequenceLength);
//        System.out.println("Max Sequence Length: " + Data.maximumSequenceLength);
//        System.out.println("Using Gaps: " + Data.usingGaps);
//        System.out.println("Calculate P-Values: " + Data.usingPvalues);
//        System.out.println("Number of Shuffles: " + Data.totalShuffles);
//        System.out.println("Do Not Include Results > Cut Off: " + Data.notReportingResultsAboveCutoff);
//        System.out.println("Threads: " + Data.threadCount);
//        System.out.println("Print Alignments: " + Data.Deg_Tool_printingAlignmentsInCMD);
//        System.out.println("CmdVerbose: " + Data.cmdVerbose);
//        System.out.println("\n\n");
//
        System.out.println("Performing analysis...");
    }//end constructor.

    /**
     * Set up the components any other components which are not dealt with in locked methods.
     */
    private void initMoreComponents() {
        tableOutput.getTableHeader().setPreferredSize(new Dimension(0, 65));
        saveAsFile = null;
        paramsPanel = new Parameters(params);
        fileSelector = new ChooseDataDialog((Frame) this.getTopLevelAncestor(), true);
        progressBar.setVisible(false);

        if ( Tools.isMac() )
        {
          // Makes no difference on windows but (should automatically) put an icon in the box on a mac
          txtSearch.putClientProperty( "JTextField.variant", "search" );
        }
    }

    /**
     * Performs a clean up before super default close action is called.
     */
    @Override
    public void doDefaultCloseAction() {
      performReset();

      showingParams = false;

      super.doDefaultCloseAction();
    }

    private void performGuiStart()
    {
      // Make sure !
      if ( AppUtils.INSTANCE.isCommandLine() )
        return;

      // Disable buttons
      btnStart.setEnabled(false);
      miStart.setEnabled( false );
      miOpenFiles.setEnabled(false);
      miReset.setEnabled( false );

      // We have started processing, so disable the parameters panel.
      paramsPanel.setAllComponentsEnabled(false);

      progressBar.setVisible(true);

      // Enable the cancel
      miCancel.setEnabled( true );
      btnCancel.setEnabled( true );

      runProcedure();
    }

    private void performReset()
    {
      if ( engine != null )
      {
        engine.reset();
      }

      // Reset variables

      srnaFile = null;
      degradomeFile = null;
      transcriptomeFile = null;
      genomeFile = null;
      saveAsFile = null;
      secondarySaveAsFile = null;
      secondaryHits.clear();
      configFile = null;
      outputFile = null;
      engine = null;

      // Reset GUI

      infoTextArea.setText("");
      infoLabel.setText("Please open fasta data files using the File -> Open menu.");

      DefaultTableModel t = (DefaultTableModel) tableOutput.getModel();
      t.setRowCount(0);
      txtSearch.setEnabled( false );

      progressBar.setVisible(false);
      btnStart.setEnabled( false );

      // Reset menus

      miOpenFiles.setEnabled(true);
      miSaveAs.setEnabled( false );
      miSaveOutput.setEnabled( false );

      miStart.setEnabled( false );
      btnStart.setEnabled( false );
      miCancel.setEnabled( false );
      btnCancel.setEnabled( false );

      miViewTplots.setEnabled( false );

      paramsPanel.setVisible(false);
      paramsPanel = new Parameters(params);

      fileSelector = new ChooseDataDialog((Frame) this.getTopLevelAncestor(), true);
    }
    /**
     * Method is called from within the constructor to
     * initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        pnlTop = new javax.swing.JPanel();
        scrollTopLeft = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        pnlTopRight = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        scrollTable = new javax.swing.JScrollPane();
        tableOutput = new javax.swing.JTable();
        panelBottom = new javax.swing.JPanel();
        pnlBottomSearch = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        pnlProgress = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 6), new java.awt.Dimension(0, 6), new java.awt.Dimension(32767, 6));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        pnlBottomStart = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        miOpenFiles = new javax.swing.JMenuItem();
        miSaveOutput = new javax.swing.JMenuItem();
        miSaveAs = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        miReset = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        Close = new javax.swing.JMenuItem();
        menuRun = new javax.swing.JMenu();
        miStart = new javax.swing.JMenuItem();
        miCancel = new javax.swing.JMenuItem();
        menuTplots = new javax.swing.JMenu();
        miViewTplots = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        miHelp = new javax.swing.JMenuItem();

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("PAREsnip");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/PAREsnipLOGO.png"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(1000, 675));
        try
        {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1)
        {
            e1.printStackTrace();
        }
        setVisible(true);

        pnlTop.setBackground(new java.awt.Color(120, 120, 120));
        pnlTop.setForeground(java.awt.Color.white);
        pnlTop.setMinimumSize(new java.awt.Dimension(800, 160));
        pnlTop.setPreferredSize(new java.awt.Dimension(800, 160));
        pnlTop.setLayout(new java.awt.BorderLayout());

        scrollTopLeft.setBackground(new java.awt.Color(120, 120, 120));
        scrollTopLeft.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.white)); // NOI18N
        scrollTopLeft.setMaximumSize(new java.awt.Dimension(110, 120));
        scrollTopLeft.setMinimumSize(new java.awt.Dimension(110, 120));
        scrollTopLeft.setPreferredSize(new java.awt.Dimension(110, 120));

        infoTextArea.setBackground(new java.awt.Color(120, 120, 120));
        infoTextArea.setColumns(20);
        infoTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        infoTextArea.setForeground(java.awt.Color.white);
        infoTextArea.setLineWrap(true);
        infoTextArea.setRows(5);
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setCaretColor(new java.awt.Color(51, 204, 255));
        infoTextArea.setMargin(new java.awt.Insets(10, 10, 10, 10));
        scrollTopLeft.setViewportView(infoTextArea);

        pnlTop.add(scrollTopLeft, java.awt.BorderLayout.CENTER);

        pnlTopRight.setBackground(new java.awt.Color(120, 120, 120));
        pnlTopRight.setMaximumSize(new java.awt.Dimension(2147483647, 140));
        pnlTopRight.setMinimumSize(new java.awt.Dimension(500, 140));
        pnlTopRight.setPreferredSize(new java.awt.Dimension(500, 140));
        pnlTopRight.setLayout(new java.awt.BorderLayout());

        infoLabel.setBackground(new java.awt.Color(120, 120, 120));
        infoLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        infoLabel.setForeground(java.awt.Color.white);
        infoLabel.setText(" Please open fasta data files using the File -> Open menu.");
        infoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        infoLabel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Messages", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.white)); // NOI18N
        pnlTopRight.add(infoLabel, java.awt.BorderLayout.CENTER);

        pnlTop.add(pnlTopRight, java.awt.BorderLayout.LINE_END);

        getContentPane().add(pnlTop, java.awt.BorderLayout.PAGE_START);

        scrollTable.setBackground(new java.awt.Color(120, 120, 120));
        scrollTable.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.white)); // NOI18N
        scrollTable.setForeground(java.awt.Color.white);

        tableOutput.setAutoCreateRowSorter(true);
        tableOutput.setBackground(new java.awt.Color(120, 120, 120));
        tableOutput.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        tableOutput.setForeground(java.awt.Color.white);
        tableOutput.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Select", "Gene", "Category", "<html>Cleavage<br>Position", "P-Value", "<html>Fragment<br>Abundance", "<html>Weighted<br>Fragment<br>Abundance", "<html>Normalised<br>Weighted<br>Fragment<br>Abundance", "Duplex", "<html>Alignment<br>Score", "Short Read ID", "<html>Short<br>Read<br>Abundance", "<html>Normalised<br>Short<br>Read<br>Abundance", "Record ID", "Secondary"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Boolean.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.String.class, java.lang.Float.class, java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean []
            {
                true, false, false, false, false, false, false, true, false, false, false, false, false, false, true
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
        tableOutput.setColumnSelectionAllowed(true);
        tableOutput.setFillsViewportHeight(true);
        tableOutput.setRowHeight(60);
        tableOutput.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollTable.setViewportView(tableOutput);
        tableOutput.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tableOutput.getColumnModel().getColumnCount() > 0)
        {
            tableOutput.getColumnModel().getColumn(0).setPreferredWidth(40);
            tableOutput.getColumnModel().getColumn(1).setPreferredWidth(115);
            tableOutput.getColumnModel().getColumn(2).setPreferredWidth(55);
            tableOutput.getColumnModel().getColumn(3).setPreferredWidth(55);
            tableOutput.getColumnModel().getColumn(4).setPreferredWidth(50);
            tableOutput.getColumnModel().getColumn(5).setPreferredWidth(65);
            tableOutput.getColumnModel().getColumn(6).setPreferredWidth(65);
            tableOutput.getColumnModel().getColumn(7).setPreferredWidth(65);
            tableOutput.getColumnModel().getColumn(8).setPreferredWidth(290);
            tableOutput.getColumnModel().getColumn(8).setCellRenderer(duplexRender);
            tableOutput.getColumnModel().getColumn(9).setPreferredWidth(60);
            tableOutput.getColumnModel().getColumn(10).setPreferredWidth(100);
            tableOutput.getColumnModel().getColumn(11).setPreferredWidth(65);
            tableOutput.getColumnModel().getColumn(12).setPreferredWidth(65);
            tableOutput.getColumnModel().getColumn(13).setPreferredWidth(60);
            tableOutput.getColumnModel().getColumn(14).setPreferredWidth(65);
        }

        getContentPane().add(scrollTable, java.awt.BorderLayout.CENTER);

        panelBottom.setBackground(new java.awt.Color(120, 120, 120));
        panelBottom.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelBottom.setMinimumSize(new java.awt.Dimension(10, 40));
        panelBottom.setPreferredSize(new java.awt.Dimension(10, 40));
        panelBottom.setLayout(new java.awt.BorderLayout(10, 0));

        pnlBottomSearch.setBackground(new java.awt.Color(120, 120, 120));
        pnlBottomSearch.setPreferredSize(new java.awt.Dimension(300, 30));
        pnlBottomSearch.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSearch.setForeground(java.awt.Color.white);
        lblSearch.setText("Search:");
        pnlBottomSearch.add(lblSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 6, 60, 24));

        txtSearch.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        txtSearch.setEnabled(false);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                txtSearchKeyPressed(evt);
            }
        });
        pnlBottomSearch.add(txtSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 6, 210, 24));

        panelBottom.add(pnlBottomSearch, java.awt.BorderLayout.LINE_START);

        pnlProgress.setBackground(new java.awt.Color(120, 120, 120));
        pnlProgress.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        pnlProgress.setMinimumSize(new java.awt.Dimension(10, 30));
        pnlProgress.setPreferredSize(new java.awt.Dimension(100, 30));
        pnlProgress.setLayout(new java.awt.BorderLayout());

        progressBar.setBackground(new java.awt.Color(120, 120, 120));
        progressBar.setValue(50);
        progressBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(120, 120, 120)));
        progressBar.setPreferredSize(new java.awt.Dimension(100, 10));
        progressBar.setString("Progress...");
        progressBar.setStringPainted(true);
        pnlProgress.add(progressBar, java.awt.BorderLayout.CENTER);
        pnlProgress.add(filler1, java.awt.BorderLayout.PAGE_START);
        pnlProgress.add(filler2, java.awt.BorderLayout.PAGE_END);

        panelBottom.add(pnlProgress, java.awt.BorderLayout.CENTER);

        pnlBottomStart.setBackground(new java.awt.Color(120, 120, 120));
        pnlBottomStart.setMaximumSize(new java.awt.Dimension(250, 28));
        pnlBottomStart.setMinimumSize(new java.awt.Dimension(250, 28));
        pnlBottomStart.setPreferredSize(new java.awt.Dimension(215, 28));
        pnlBottomStart.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnStart.setBackground(new java.awt.Color(120, 120, 120));
        btnStart.setText("Start");
        btnStart.setMaximumSize(new java.awt.Dimension(57, 26));
        btnStart.setMinimumSize(new java.awt.Dimension(57, 26));
        btnStart.setPreferredSize(new java.awt.Dimension(57, 26));
        btnStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnStartActionPerformed(evt);
            }
        });
        pnlBottomStart.add(btnStart, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 5, 100, -1));

        btnCancel.setBackground(new java.awt.Color(120, 120, 120));
        btnCancel.setText("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.setMaximumSize(new java.awt.Dimension(57, 26));
        btnCancel.setMinimumSize(new java.awt.Dimension(57, 26));
        btnCancel.setPreferredSize(new java.awt.Dimension(57, 26));
        btnCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnCancelActionPerformed(evt);
            }
        });
        pnlBottomStart.add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 5, 100, -1));

        panelBottom.add(pnlBottomStart, java.awt.BorderLayout.LINE_END);

        getContentPane().add(panelBottom, java.awt.BorderLayout.PAGE_END);

        menuFile.setText("File");

        miOpenFiles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/add-item.png"))); // NOI18N
        miOpenFiles.setText("Open...");
        miOpenFiles.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miOpenFilesActionPerformed(evt);
            }
        });
        menuFile.add(miOpenFiles);

        miSaveOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        miSaveOutput.setText("Save...");
        miSaveOutput.setEnabled(false);
        miSaveOutput.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miSaveOutputActionPerformed(evt);
            }
        });
        menuFile.add(miSaveOutput);

        miSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        miSaveAs.setText("Save as...");
        miSaveAs.setEnabled(false);
        miSaveAs.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(miSaveAs);
        menuFile.add(jSeparator2);

        miReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/reload.png"))); // NOI18N
        miReset.setText("Reset");
        miReset.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miResetActionPerformed(evt);
            }
        });
        menuFile.add(miReset);
        menuFile.add(jSeparator1);

        Close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/close-tool.png"))); // NOI18N
        Close.setText("Close");
        Close.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CloseActionPerformed(evt);
            }
        });
        menuFile.add(Close);

        menuBar.add(menuFile);

        menuRun.setText("Run");

        miStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/light.png"))); // NOI18N
        miStart.setText("Start");
        miStart.setEnabled(false);
        miStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miStartActionPerformed(evt);
            }
        });
        menuRun.add(miStart);

        miCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lightCancel.png"))); // NOI18N
        miCancel.setText("Cancel");
        miCancel.setEnabled(false);
        miCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miCancelActionPerformed(evt);
            }
        });
        menuRun.add(miCancel);

        menuBar.add(menuRun);

        menuTplots.setText("View");

        miViewTplots.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/bar-chart-02.png"))); // NOI18N
        miViewTplots.setText("View T-plots in VisSR...");
        miViewTplots.setEnabled(false);
        miViewTplots.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                miViewTplotsActionPerformed(evt);
            }
        });
        menuTplots.add(miViewTplots);

        menuBar.add(menuTplots);

        menuHelp.setText("Help");

        miHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        miHelp.setText("Contents");
        menuHelp.add(miHelp);

        menuBar.add(menuHelp);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * File menu open file command.
     * @param evt
     */
    private void miOpenFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenFilesActionPerformed
        openFile();
    }//GEN-LAST:event_miOpenFilesActionPerformed

    /**
     * File menu save file command.
     * @param evt
     */
    private void miSaveOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveOutputActionPerformed
      saveLog();
      saveFile();
    }//GEN-LAST:event_miSaveOutputActionPerformed

    /**
     * File menu save file as command.
     * @param evt
     */
    private void miSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveAsActionPerformed
      saveLog();
      saveAs();
    }//GEN-LAST:event_miSaveAsActionPerformed

    private void CloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseActionPerformed
        this.doDefaultCloseAction();
    }//GEN-LAST:event_CloseActionPerformed
@Override
  public void shutdown()
  {
    performReset();
   this.doDefaultCloseAction();
  }
  private void miViewTplotsActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_miViewTplotsActionPerformed
  {//GEN-HEADEREND:event_miViewTplotsActionPerformed
    PlotRecordCollection prc = new PlotRecordCollection();

    if ( engine != null )
    {
      engine.populatePlotRecordCollection( prc );
    }

    SequenceVizMainFrame.createVisSRInstanceForPARESnip( prc, true );
  }//GEN-LAST:event_miViewTplotsActionPerformed

  private void txtSearchKeyPressed( java.awt.event.KeyEvent evt )//GEN-FIRST:event_txtSearchKeyPressed
  {//GEN-HEADEREND:event_txtSearchKeyPressed
    if ( evt.getKeyCode() == KeyEvent.VK_ENTER )
    {
      String searchString = txtSearch.getText();

      if ( searchString != null && ! searchString.isEmpty() )
      {
        performSearch( searchString );
      }
    }
  }//GEN-LAST:event_txtSearchKeyPressed

  private void miCancelActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_miCancelActionPerformed
  {//GEN-HEADEREND:event_miCancelActionPerformed
    performReset();
  }//GEN-LAST:event_miCancelActionPerformed

  private void miStartActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_miStartActionPerformed
  {//GEN-HEADEREND:event_miStartActionPerformed
    performGuiStart();
  }//GEN-LAST:event_miStartActionPerformed

  private void miResetActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_miResetActionPerformed
  {//GEN-HEADEREND:event_miResetActionPerformed
    performReset();
  }//GEN-LAST:event_miResetActionPerformed

  private void btnStartActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_btnStartActionPerformed
  {//GEN-HEADEREND:event_btnStartActionPerformed
    performGuiStart();
  }//GEN-LAST:event_btnStartActionPerformed

  private void btnCancelActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_btnCancelActionPerformed
  {//GEN-HEADEREND:event_btnCancelActionPerformed
    performReset();
  }//GEN-LAST:event_btnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Close;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnStart;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenu menuRun;
    private javax.swing.JMenu menuTplots;
    private javax.swing.JMenuItem miCancel;
    private javax.swing.JMenuItem miHelp;
    private javax.swing.JMenuItem miOpenFiles;
    private javax.swing.JMenuItem miReset;
    private javax.swing.JMenuItem miSaveAs;
    private javax.swing.JMenuItem miSaveOutput;
    private javax.swing.JMenuItem miStart;
    private javax.swing.JMenuItem miViewTplots;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel pnlBottomSearch;
    private javax.swing.JPanel pnlBottomStart;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JPanel pnlTopRight;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollTable;
    private javax.swing.JScrollPane scrollTopLeft;
    private javax.swing.JTable tableOutput;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

    /**
     * Opens a file.
     * @return True when all required files have been selected.
     */
    public boolean openFile() {

        fileSelector.setVisible(true);

        srnaFile = fileSelector.getSrnaome();
        degradomeFile = fileSelector.getDegradome();
        transcriptomeFile = fileSelector.getTranscriptome();
        genomeFile = fileSelector.getGenome();

        boolean goA = false;
        boolean goB = false;
        boolean goC = false;

        infoTextArea.setText("");
        infoTextArea.append("Files Selected:" + LINE_SEPARATOR);

        if (srnaFile == null) {
            infoLabel.setText("<html>A file containing small RNA data is required.<br>Please open a file containing small RNA data.</html>");
            infoTextArea.append("Small RNA File: MISSING!" + LINE_SEPARATOR);
        } else if (checkFile(srnaFile)) {
            infoTextArea.append("Small RNA File: " + srnaFile.getName() + LINE_SEPARATOR);
            goA = true;
        } else {
            infoLabel.setText("Check the file containing the small RNA data.");
        }

        if (degradomeFile == null) {
            infoLabel.setText("<html>A file containing degraded fragments is required.<br>Please open a file containing degradome data.</html>");
            infoTextArea.append("Degraded Fragment File: MISSING!" + LINE_SEPARATOR);
        } else if (checkFile(degradomeFile)) {
            infoTextArea.append("Degraded Fragment File: " + degradomeFile.getName() + LINE_SEPARATOR);
            goB = true;
        } else {
            infoLabel.setText("Check the file containing the degraded fragment data.");
        }

        if (transcriptomeFile == null) {
            infoLabel.setText("<html>A file containing transcripts is required.<br>Please open a file containing transcript data.</html>");
            infoTextArea.append("Transcripts File: MISSING!" + LINE_SEPARATOR);
        } else if (checkFile(transcriptomeFile)) {
            infoTextArea.append("Transcripts File: " + transcriptomeFile.getName() + LINE_SEPARATOR);
            goC = true;
        } else {
            infoLabel.setText("Check the file containing the transcripts.");
        }

        if (genomeFile != null && checkFile(genomeFile)) {
            infoTextArea.append("Genome File: " + genomeFile.getName() + LINE_SEPARATOR);
        }

        boolean ok = goA && goB && goC;

        if (ok)
        {
            infoLabel.setText("Please check/set the parameters and then press the start button.");

            showingParams = true;
            AppUtils.INSTANCE.getMDIDesktopPane().showParams( this );
        }

        btnStart.setEnabled(ok);
        miStart.setEnabled( ok );

        return ok;
    }

    private boolean checkFile(File f) {
        if (!f.exists()) {
            infoTextArea.append("ERROR: " + f.getName() + " - FILE DOES NOT EXIST!" + LINE_SEPARATOR);
            return false;
        }

        if (!f.canRead()) {
            infoTextArea.append("ERROR: " + f.getName() + " - CAN NOT READ FILE!" + LINE_SEPARATOR);
            return false;
        }

        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(f));

            String line1 = r.readLine();
            String line2 = r.readLine();

            if (line1 == null || line1.isEmpty()) {
                return false;
            }
            if (line2 == null || line2.isEmpty()) {
                return false;
            }

            if (line1.startsWith(">")) {
                // Get the first character...
                String s1 = line2.substring(0, 1).toUpperCase();

                // ...and check it's a valid nucleotide letter
                if ("ACGTU".indexOf(s1) != -1) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            infoTextArea.append("ERROR: " + f.getName() + " - FILE NOT FOUND!" + LINE_SEPARATOR);
            return false;
        } catch (IOException ex) {
            infoTextArea.append("ERROR: " + f.getName() + " - PROBLEM WITH FILE I/O!" + LINE_SEPARATOR);
            return false;
        } finally {
            IOUtils.closeQuietly( r );
        }

        infoTextArea.append("ERROR: " + f.getName() + " - IS NOT FASTA FORMAT!" + LINE_SEPARATOR);

        return false;
    }

  @Override
  public void runProcedure()
  {
    //Make the engine and get it going...

    if ( AppUtils.INSTANCE.isCommandLine() )
    {
      System.out.println( "*** Parameters ***" );
      System.out.println( params.toString() );

      engine = new Engine( params, srnaFile, degradomeFile, transcriptomeFile, genomeFile, outputFile );
      engine.doInBackground();
    }
    else
    {
      engine = new Engine( params, (DefaultTableModel) tableOutput.getModel(), infoTextArea, infoLabel, progressBar, srnaFile, degradomeFile, transcriptomeFile, genomeFile, secondaryHits, this );
      engine.execute();
    }
  }


    @Override
    public JPanel getParamsPanel() {
        return this.paramsPanel;
    }

    private void saveLog()
    {
      File logFile = FileDialogUtils.showFileSaveDialog( this, "Save log", FileDialogUtils.FileExtFilter.TEXT.getFilter() );

      if ( logFile != null )
      {
        try
        {
          FileUtils.writeStringToFile( logFile, infoTextArea.getText() );
        }
        catch(IOException ioe)
        {
          LOGGER.log( Level.FINE, "A problem writing to logFile occured: {0}", logFile );
        }
      }
    }

    /**
     * Save the work done by the degradome.
     * @return
     */
    public boolean saveFile() {
        //If there is no table or text area, then there is noting to be saved.
        if (tableOutput.getModel().getRowCount() == 0) {
            //Tell the user early and do not proced any further.
            JOptionPane.showMessageDialog(this, "There is nothing to save.");
            return false;
        } else {//Else there is something to be saved.
            //Make a flag to be set to true after user input operations.
            boolean haveFileToWriteTo = false;
            //If there is not an existing file to write to.
            if (saveAsFile == null) {
                //Make a file chooser and set it up for the user to select a file.....

                JFileChooser c = new JFileChooser();
                JLabel label = new JLabel("<html> Primary Save:"
                        + "<br/> Save the contents of the"
                        + "<br/> Primary hits table to"
                        + "<br/> a .csv file.");
                label.setBorder(BorderFactory.createTitledBorder("Information"));
                c.setAccessory(label);

                c.setCurrentDirectory( new File( FileDialogUtils.getLastDir() ) );
                String extension = "csv";
                FileTypeFilter filter = new FileTypeFilter(extension, null);
                c.setFileFilter(filter);

                int save = c.showSaveDialog(this);

                //If the user aproved their choice of file.
                if (save == JFileChooser.APPROVE_OPTION) {
                    File tempSaveAsFile = c.getSelectedFile();
                    //Check to see if it already exists.
                    if (tempSaveAsFile.exists()) {
                        //If it does exist, ask the user what to do, overwrite it or not?
                        int answer = JOptionPane.showConfirmDialog(this, "The file " + tempSaveAsFile.getName() + " already exists. Overwrite existing file?",
                                "PAREsnip Primary Save", JOptionPane.YES_NO_OPTION);
                        //If the user said yes overwrite the file.
                        if (answer == JOptionPane.YES_OPTION) {
                            //Check that the file has the correct extension.
                            String path = tempSaveAsFile.getPath();
                            if (Data.IS_TABLE_MODE && !path.toLowerCase().endsWith(".csv")) {
                                saveAsFile = new File(path + ".csv");
                            } else {
                                saveAsFile = tempSaveAsFile;
                            }
                            //All is good if we got to here, so we have a file to write to.
                            haveFileToWriteTo = true;
                        }
                    } else {//A file does not exist so check that it has the correct file extension, if not, put it there.
                        String path = tempSaveAsFile.getPath();
                        if (Data.IS_TABLE_MODE && !path.toLowerCase().endsWith(".csv")) {
                            saveAsFile = new File(path + ".csv");
                        } else {
                            saveAsFile = tempSaveAsFile;
                        }
                        //All is good if we got to here so set flag to true.
                        haveFileToWriteTo = true;
                    }//end if.
                } else {//The user cancelled the save operation, so just return false.
                    return false;
                }//end else.
            } else {//Else, we already have a file to save to from a previous save opperation.
                haveFileToWriteTo = true;
            }//end else.

            //If all is good and we have a file to write to after all the checking above.
            if (haveFileToWriteTo) {
                //Grab some memory.
                PrintStream out = null;
                try {
                    //Set up a print stream to the users file.
                    out = new PrintStream(new FileOutputStream(saveAsFile));
                    //save the tabe in .csv format.
                    int columns = tableOutput.getModel().getColumnCount();
                    int rows = tableOutput.getModel().getRowCount();
                    //Print out the table headders first.
                    for (int headder = 0; headder < columns; headder++) {
                        String temp = tableOutput.getModel().getColumnName(headder);
                        //If the column headder has any html tags.
                        if (temp.startsWith("<html>")) {
                            //Remove the html tags.
                            temp = temp.substring(6);
                            temp = temp.replaceAll("<br>", " ");
                        }
                        out.print(temp + ",");
                    }
                    out.print(LINE_SEPARATOR);
                    for (int row = 0; row < rows; row++) {
                        for (int column = 0; column < columns; column++) {
                            String stringOutput = tableOutput.getModel().getValueAt(row, column).toString();
                            out.print("\"" + stringOutput + "\",");
                        }
                        out.print(LINE_SEPARATOR);
                    }//end for.
                    //Tidy up as we are done here.
                    out.close();
                } catch (FileNotFoundException ex) {
                    //Catch exception and let the user know.
                    JOptionPane.showMessageDialog(this, "Cannot save to file " + saveAsFile.getName() + ". Please try again.");
                    System.err.println("An error occured when trying to save to file.");
                    //Tidy up.
                    if (out != null) {
                        out.close();
                    }
                    saveAsFile = null;
                }//end catch.
            }//end if.

            if(params.isSecondaryOutputToFile()) {
                saveSecondaryOutput();
            }

            return haveFileToWriteTo;
        }//else else.
    }//end method.

    /**
     * Save function for secondary output in GUI mode.
     * @return
     */
    public boolean saveSecondaryOutput() {
        //If there is no table or text area, then there is noting to be saved.
        if (secondaryHits.isEmpty()) {
            //Tell the user early and do not proced any further.
            JOptionPane.showMessageDialog(this, "There are no secondary hits to save.");
            return false;
        } else {//Else there is something to be saved.
            //Make a flag to be set to true after user input operations.
            boolean haveFileToWriteTo = false;
            //If there is not an existing file to write to.
            if (secondarySaveAsFile == null) {
                //Make a file chooser and set it up for the user to select a file.....
                JFileChooser c = new JFileChooser();
                JLabel label = new JLabel("<html> Secondary Save:"
                        + "<br/> Save the contents of the"
                        + "<br/> Secondary hits collection"
                        + "<br/> to a .csv file.");
                label.setBorder(BorderFactory.createTitledBorder("Information"));
                c.setAccessory(label);
                String extension = "csv";
                c.setCurrentDirectory( new File( FileDialogUtils.getLastDir() ) );
                FileTypeFilter filter = new FileTypeFilter(extension, null);
                c.setFileFilter(filter);
                int save = c.showSaveDialog(this);
                //If the user aproved their choice of file.
                if (save == JFileChooser.APPROVE_OPTION) {
                    File tempSaveAsFile = c.getSelectedFile();
                    //Check to see if it already exists.
                    if (tempSaveAsFile.exists()) {
                        //If it does exist, ask the user what to do, overwrite it or not?
                        int answer = JOptionPane.showConfirmDialog(this, "The file " + tempSaveAsFile.getName() + " already exists. Overwrite existing file?",
                                "PAREsnip Secondary Save", JOptionPane.YES_NO_OPTION);
                        //If the user said yes overwrite the file.
                        if (answer == JOptionPane.YES_OPTION) {
                            //Check that the file has the correct extension.
                            String path = tempSaveAsFile.getPath();
                            if (Data.IS_TABLE_MODE && !path.toLowerCase().endsWith(".csv")) {
                                secondarySaveAsFile = new File(path + ".csv");
                            } else {
                                secondarySaveAsFile = tempSaveAsFile;
                            }
                            //All is good if we got to here, so we have a file to write to.
                            haveFileToWriteTo = true;
                        }
                    } else {//A file does not exist so check that it has the correct file extension, if not, put it there.
                        String path = tempSaveAsFile.getPath();
                        if (!path.toLowerCase().endsWith(".csv")) {
                            secondarySaveAsFile = new File(path + ".csv");
                        } else {
                            secondarySaveAsFile = tempSaveAsFile;
                        }
                        //All is good if we got to here so set flag to true.
                        haveFileToWriteTo = true;
                    }//end if.
                } else {//The user cancelled the save operation, so just return false.
                    return false;
                }//end else.
            } else {//Else, we already have a file to save to from a previous save opperation.
                haveFileToWriteTo = true;
            }//end else.

            //If all is good and we have a file to write to after all the checking above.
            if (haveFileToWriteTo) {
                //Grab some memory.
                PrintStream out = null;
                try {
                    //PRINT THE ROW HEADDERS
                    //Set up a print stream to the users file.
                    out = new PrintStream(new FileOutputStream(secondarySaveAsFile));
                    //save the tabe in .csv format.
                    int columns = tableOutput.getModel().getColumnCount();
                    //Print out the table headders first.
                    for (int headder = 1; headder < columns - 2; headder++) {
                        String temp = tableOutput.getModel().getColumnName(headder);
                        //If the column headder has any html tags.
                        if (temp.startsWith("<html>")) {
                            //Remove the html tags.
                            temp = temp.substring(6);
                            temp = temp.replaceAll("<br>", " ");
                        }
                        out.print(temp + ",");
                    }
                    out.print(LINE_SEPARATOR);
                    //PRINT THE ROWS
                    //Print the contents of the secondary hits collection to the secondary file.
                    //Get an iterator for the secondary hits collection.
                    Iterator<String> itr = secondaryHits.keySet().iterator();
                    //While we have something to print out..
                    while (itr.hasNext()) {
                        //Get the gene for the next collection of records
                        String key = itr.next();
                        //Get the list of reccords for this gene.
                        ArrayList<String[]> records = secondaryHits.get(key);
                        //For each of the records in the list for this gene.
                        for (int i = 0; i < records.size(); i++) {
                            //Get the fields for this record.
                            String[] fields = records.get(i);
                            //Print out each field in the record with commas.
                            for (int j = 0; j < fields.length; j++) {
                                out.print("\"" + fields[j] + "\",");
                            }
                            //This record is done, so a new line for the next record.
                            out.print(LINE_SEPARATOR);
                        }//end for each record.
                    }//end for each gene in the collection.
                    //Tidy up as we are done here.
                    out.close();
                } catch (FileNotFoundException ex) {
                    //Catch exception and let the user know.
                    JOptionPane.showMessageDialog(this, "Cannot save to file " + secondarySaveAsFile.getName() + ". Please try again.");
                    System.err.println("An error occured when trying to save to file.");
                    //Tidy up.
                    if (out != null) {
                        out.close();
                    }
                    secondarySaveAsFile = null;
                }//end catch.
            }//end if.
            return haveFileToWriteTo;
        }//else else.
    }//end method.

    /**
     * Save As functionality.
     */
    public void saveAs() {
        if (tableOutput.getModel().getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "There is nothing to save.");
        } else {
            JFileChooser c = new JFileChooser();
            JLabel label = new JLabel("<html> Primary Save:"
                    + "<br/> Save the contents of the"
                    + "<br/> Primary hits table to"
                    + "<br/> a .csv file.");
            label.setBorder(BorderFactory.createTitledBorder("Information"));
            c.setAccessory(label);
            String extension = "csv";
            c.setCurrentDirectory( new File( FileDialogUtils.getLastDir() ) );
            FileTypeFilter filter = new FileTypeFilter(extension, null);
            c.setFileFilter(filter);
            int save = c.showSaveDialog(this);
            if (save == JFileChooser.APPROVE_OPTION) {
                File tempSaveAsFile = c.getSelectedFile();
                if (tempSaveAsFile.exists()) {
                    int answer = JOptionPane.showConfirmDialog(this, "The file " + tempSaveAsFile.getName() + " already exists. Overwrite existing file?",
                            "PAREsnip Primary Save", JOptionPane.YES_NO_OPTION);

                    if (answer == JOptionPane.YES_OPTION) {
                        String path = tempSaveAsFile.getPath();
                        if (Data.IS_TABLE_MODE && !path.toLowerCase().endsWith(".csv")) {
                            saveAsFile = new File(path + ".csv");
                        } else {
                            saveAsFile = tempSaveAsFile;
                        }
                        saveFile();
                    }
                } else {
                    String path = tempSaveAsFile.getPath();
                    if (Data.IS_TABLE_MODE && !path.toLowerCase().endsWith(".csv")) {
                        saveAsFile = new File(path + ".csv");
                    } else {
                        saveAsFile = tempSaveAsFile;
                    }
                    saveFile();
                }
            }
        }
    }//end method.

    public void setToolManager(ToolManager openTools) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setShowingParams(boolean newState) {
        this.showingParams = newState;
    }

    @Override
    public boolean getShowingParams() {
        return this.showingParams;
    }

    private void performSearch( String str )
    {
      if ( ! Data.IS_TABLE_MODE || str == null || str.isEmpty() )
        return;

      final int COLUMNS = tableOutput.getModel().getColumnCount();
      final int ROWS = tableOutput.getModel().getRowCount();

      if ( ROWS == 0 )
        return;

      String searchForLC = str.toLowerCase();

      int startCol = tableOutput.getSelectedColumn();
      int startRow = tableOutput.getSelectedRow();

      // Move it to the next cell (next column and then next row)
      if ( startCol == -1 && startRow == -1 )
      {
        startCol = startRow = 0;
      }
      else
      {
        startCol = ( startCol + 1 ) % COLUMNS;
        if ( startCol == 0 )
        {
          startRow = ( startRow + 1 ) % ROWS;
        }
      }

      int row = 0, column = 0;
      boolean found = false;

      for ( row = startRow; row < ROWS; row++ )
      {
        for ( column = startCol; column < COLUMNS; column++ )
        {
          Object obj = tableOutput.getValueAt( row, column );
          if ( obj == null )
            continue;

          String content = obj.toString().toLowerCase();

          if ( content.contains( searchForLC ) )
          {
            found = true;
            break;
          }
        }

        startCol = 0;

        if ( found )
          break;
      }

      if ( ! found )
      {
        // Go back to the start

        for ( row = 0; row <= startRow; row++ )
        {
          for ( column = 0; column < COLUMNS; column++ )
          {
            Object obj = tableOutput.getValueAt( row, column );
            if ( obj == null )
              continue;

            String content = obj.toString().toLowerCase();

            if ( content.contains( searchForLC ) )
            {
              found = true;
              break;
            }
          }

          if ( found )
           break;
        }
      }

      if ( found )
      {
        tableOutput.scrollRectToVisible( tableOutput.getCellRect( row, column, true ) );
        tableOutput.changeSelection( row, column, false, false );
      }
      else
      {
        tableOutput.changeSelection( -1, -1, false, false );
      }
    }

  // Start of FeedbackI implemention
  //
  @Override
  public void setStatusText( String text )
  {
    if ( text == null )
      return;

    String msg = text;

    if ( text.contains( LINE_SEPARATOR ) )
    {
      msg = "<html>" + msg.replace( LINE_SEPARATOR, "<br/>" ) + "</html>";
    }

    if ( SwingUtilities.isEventDispatchThread() )
    {
      infoLabel.setText( msg );
    }
    else
    {
      final String finalMessage = msg;

      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          infoLabel.setText( finalMessage );
        }
      });
    }
  }

  @Override
  public void addProgressMessage( String text, boolean append )
  {
    if ( append )
    {
      infoTextArea.append( text );
    }
    else
    {
      infoTextArea.setText( text );
    }
  }

  @Override
  public void done( boolean success )
  {
    if ( this.isClosed )
      return;

    if ( AppUtils.INSTANCE.isCommandLine() )
      return;

    progressBar.setIndeterminate(false);
    progressBar.setVisible( false );

    // Enable/disable menus

    miOpenFiles.setEnabled( false );
    miReset.setEnabled( true );
    miSaveAs.setEnabled( success );
    miSaveOutput.setEnabled( success );
    miStart.setEnabled( false );
    miCancel.setEnabled( false );

    btnStart.setEnabled( false );
    btnCancel.setEnabled( false );

    miViewTplots.setEnabled( success );

    // Enable the table search
    txtSearch.setEnabled( success && tableOutput.getRowCount() > 0 );

    if ( success )
    {
        //Let the user know the analysis is completed.
        infoLabel.setText("<html>ANALYSIS COMPLETED! <br/><br/>Please save your results. File -> Save or Save As.</html>");
    }
  }
  //
  // End of FeedbackI implemention
}
