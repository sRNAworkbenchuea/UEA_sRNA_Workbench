/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * The sequence visualisation form
 *
 * TODO: Move the navigation panel into its own class
 *
 */
package uk.ac.uea.cmp.srnaworkbench.tools.vissr;

import uk.ac.uea.cmp.srnaworkbench.*;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.help.*;
import uk.ac.uea.cmp.srnaworkbench.io.*;
import uk.ac.uea.cmp.srnaworkbench.swing.*;
import uk.ac.uea.cmp.srnaworkbench.tools.*;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;


/**
 * The sequence visualisation form
 *
 * @author prb07qmu
 */
public final class SequenceVizMainFrame extends javax.swing.JInternalFrame implements GUIInterface
{
  private static final boolean SHOW_DEV_FEATURES = AppUtils.INSTANCE.getShowDevFeatures();

  private static final MessageFormat NAVIGATION_TEXT_FORMATTER = new MessageFormat( " {0,number,integer} - {1,number,integer}" );

  private final WideJComboBox<ReferenceSequenceComboWrapper> cmbGovSeq = new WideJComboBox<ReferenceSequenceComboWrapper>();
  private VisSRDataOverviewPanel _vdoPanel;
  private VisSRDataTiersPanel _tiersPanel;

  private List< SequenceRangeI > _referenceSequences = null;
  private SequenceRangeI _currentReferenceSequence = null;
  private PlotRecordCollection _paresnipResults = null;

  private boolean _configuredTPlotMenu = false;
  private boolean _initialisingReferenceSequence = false;
  
  private boolean CoLiDE_Full_render = false;

  private VisSRGlobals.ViewMode _viewMode = VisSRGlobals.ViewMode.NORMAL;
  private boolean normalised = false;
  private double currentStart;
  private double currentEnd;
  
  private VisSRSettingsControl VSC = new VisSRSettingsControl(null, true);
  private List<String> gffTypesToDisplay;

  /***
   * Default constructor.
   * The frame is initialised with an empty sequence and no data tiers.
   */
  public SequenceVizMainFrame(boolean normalised)
  {
    initComponents();
    this.normalised = normalised;
    initOtherComponents();

    
    this.setName( "VISSR");

    JHLauncher.setupContextDependentHelp( "HTML_vissr_html", miHelpContents, this.getRootPane() );
    
    Tools.trackPage( "VisSR Main GUI Frame Class Loaded");
  }

  /**
   * Package-private method used by panels added to the form
   * @return The current reference sequence
   */
  public final SequenceRangeI getCurrentReferenceSequence()
  {
    return _currentReferenceSequence;
  }
  public void setNormalised(boolean newState)
  {
    this.normalised = newState;
    _tiersPanel.setNormalised(normalised);
  }
  public void setCoLiDERenderMode(boolean newState)
  {
    this.CoLiDE_Full_render = newState;
  }
  public boolean getCoLIDERenderMode()
  {
    return this.CoLiDE_Full_render;
  }

  /**
   * Package-private method to get the length of the reference sequence.
   * @return Integer length. Return 1000 if the sequence is null.
   */
  final int getCurrentReferenceSequenceLength()
  {
    return _currentReferenceSequence == null ? 1000 : _currentReferenceSequence.getSequenceLength();
  }

  /***
   * Initialise non-designed components, e.g. genoviz
   */
  private void initOtherComponents()
  {
    // Add the reference sequence combo to the navigation panel (done here because the WideJComboBox has not been added to the designer's palette)
    cmbGovSeq.setMaximumRowCount( 25 );
    cmbGovSeq.setMinimumSize( new java.awt.Dimension( 118, 51 ) );
    cmbGovSeq.setPreferredSize( new java.awt.Dimension( 118, 51 ) );
    cmbGovSeq.setBackground( Color.white );
    cmbGovSeq.setEnabled( false );
    //pnlNav.add( cmbGovSeq, new org.netbeans.lib.awtextra.AbsoluteConstraints( 7, 12, 118, 21 ) );
    pnlNav.add( cmbGovSeq );
    pnlNav.remove( lblIgnore );

    // Add the overview panel (which contains the overview NeoMap)
    _vdoPanel = new VisSRDataOverviewPanel( this );
    pnlOverview.add( _vdoPanel, BorderLayout.CENTER );

    // Add the tiers panel
    _tiersPanel = new VisSRDataTiersPanel( this, normalised );
    pnlDetail.add( _tiersPanel, BorderLayout.CENTER );

    addEventHandlers();

    miFileTakePicture.setAction( new ImageUtils.TakePictureAction( miFileTakePicture, pnlVis, lblStatusBar ) );

    miPatmanDisplayAggregated.setAction( new LoadPatmanDataAction( miPatmanDisplayAggregated.getText(), true ) );
    miPatmanDisplayAll.setAction( new LoadPatmanDataAction( miPatmanDisplayAll.getText(), false ) );

    // Only show the development features when requested
    miFileTest.setVisible( SHOW_DEV_FEATURES );

    miFileParesnip.setAction( new LoadParesnipDataAction( miFileParesnip ) );
    btnParesnipFilterResults.setAction( new FilterParesnipResultsAction( btnParesnipFilterResults.getText(), true ) );
    btnParesnipClearFilter.setAction( new FilterParesnipResultsAction( btnParesnipClearFilter.getText(), false ) );

    // Invisible until we've loaded some Paresnip data
    menViewTPlots.setVisible( false );
    btnParesnipFilterResults.setVisible( false );
    btnParesnipClearFilter.setVisible( false );

    configureMenuItems();
  }

  @Override
  public void shutdown()
  {
   this.setVisible( false );

  this.dispose();
  }
  /***
   * Add additional event handlers
   */
  private void addEventHandlers()
  {
    // Add an action listener to the navigation textbox (to update the maps)
    //
    txtNav.addActionListener( new ActionListener()
    {
      private Double convertTokenToDouble( String token )
      {
        if ( token == null )
          return null;

        String tok = token.trim().replace( ",", "" ).toLowerCase();

        double mult = 1;

        // Check for a 'kb'
        if ( tok.indexOf( "kb" ) != -1 )
        {
          mult *= 1e3;
          tok = tok.replace( "kb", "" );
        }

        // Check for an 'mb'
        if ( tok.indexOf( "mb" ) != -1 )
        {
          mult *= 1e6;
          tok = tok.replace( "mb", "" );
        }

        double d = 0;

        try
        {
          d = Double.parseDouble( tok );
        }
        catch ( NumberFormatException e )
        {
          return null;
        }

        return new Double( d * mult );
      }

      @Override
      public void actionPerformed( ActionEvent e )
      {
        if ( _currentReferenceSequence == null )
          return;

        String enteredText = txtNav.getText();

        if ( _viewMode.isParesnip() )
        {
          doSearch( enteredText );
          return;
        }

        StringTokenizer st = new StringTokenizer( enteredText, ":+-" );

        boolean plusPresent = ( enteredText.indexOf( '+' ) != -1 );

        if ( st.countTokens() == 1 )
        {
          // Assume entered text is an id - search the GFF annotations for the id...

          String token = st.nextToken().trim();

          doSearch( token );
        }
        else if ( st.countTokens() == 2 )
        {
          Double start = convertTokenToDouble( st.nextToken() );
          Double end   = convertTokenToDouble( st.nextToken() );

          if ( start == null || end == null )
          {
            displayStatusMessage( "The entered text is in an unrecognised format", false );
          }
          else
          {
            if ( plusPresent )
            {
              end += start;
            }

            zoomTo( start.doubleValue(), end.doubleValue() );
          }
        }
        else
        {
          displayStatusMessage( "The entered text is in an unrecognised format", false );
        }
      }

      private void doSearch( String searchText)
      {
        if ( searchText == null || searchText.trim().isEmpty() )
          return;

        String msg = "";

        if ( ! _tiersPanel.findByKey( searchText ) )
        {
          msg = "Unable to find '" + searchText + "'";
        }

        displayStatusMessage( msg, false );
      }
    } );

    cmbGovSeq.addActionListener( new ActionListener()
    {
      @Override
      public void actionPerformed( ActionEvent e )
      {
        Object o = cmbGovSeq.getSelectedItem();

        if ( o instanceof ReferenceSequenceComboWrapper )
        {
          ReferenceSequenceComboWrapper wrap = (ReferenceSequenceComboWrapper) o;

          if ( _currentReferenceSequence == wrap.getSequenceRangeI() )
            return;

          cmbGovSeq.setPopupVisible( false );

          ThreadUtils.safeSleep( 10 ); // to allow drop-down popup to disappear

          cmbGovSeq.setToolTipText( null );

          if ( wrap.getSequenceRangeI() == null )
          {
            lblStatusBar.setText( " Invalid sequence" );
            return;
          }

          _currentReferenceSequence = wrap.getSequenceRangeI();

          // Set a tooltip on the combo...
          cmbGovSeq.setToolTipText( VisSRSequenceHelper.formatHTML( _currentReferenceSequence, false ) );

          setBusyCursor( true );
          try
          {
            // Reinitialise the panels
            _vdoPanel.reinitialise();
            _tiersPanel.reinitialise();

            zoomTo( 0, 100000 );
          }
          finally
          {
            setBusyCursor( false );
          }
        }
      }
    } );

    // Wire-up the 'jump' buttons
    //
    btnLL.setAction( new ScrollTierAction( "<<", "Scroll the view to the left 100%", -100 ) );
    btnL.setAction(  new ScrollTierAction(  "<", "Scroll the view to the left 50%",   -50 ) );
    btnR.setAction(  new ScrollTierAction( ">",  "Scroll the view to the right 50%",   50 ) );
    btnRR.setAction( new ScrollTierAction( ">>", "Scroll the view to the right 100%", 100 ) );
  }

  /**
   *
   * @param sequenceId
   * @param startIndex
   * @param endIndex
   */
  public void displaySequenceRegion( final String sequenceId, final int startIndex, final int endIndex )
  {
    if ( sequenceId == null || sequenceId.isEmpty() )
      return;

    //new Thread( new GenerateWaitCursor( this ) ).start();
    this.setBusyCursor( true );
    while( _initialisingReferenceSequence )
    {
    }
    this.setBusyCursor( false );
    //this.setCursor( Cursor.getDefaultCursor() );
//      SwingUtilities.invokeLater( new Runnable()
//      {
//        @Override
//        public void run()
//        {
          //displaySequenceRegion( sequenceId, startIndex, endIndex );
//        }
//      } );

//      return;
//    }

    if ( _referenceSequences == null || _referenceSequences.isEmpty() )
      return;

    for ( SequenceRangeI sr : _referenceSequences )
    {
      if ( sequenceId.equals( sr.getSequenceId() ) )
      {
        cmbGovSeq.setSelectedIndex( _referenceSequences.indexOf( sr ) );
        zoomTo( startIndex, endIndex );
        break;
      }
    }
  }

  /***
   * Set the navigation text
   * @param start The start coordinate
   * @param end The end coordinate
   */
  private void setNavigationText( double start, double end )
  {
    if ( _viewMode.isParesnip() )
      return;

    if ( Double.isInfinite( start ) || Double.isNaN( start ) )
      return;

    if ( Double.isInfinite( end ) || Double.isNaN( end ) )
      return;

    Object[] args = { start, end };
    txtNav.setText( NAVIGATION_TEXT_FORMATTER.format( args ) );
  }

  /***
   * Zoom the detail map
   * @param start The new start coordinate
   * @param end The new end coordinate
   */
  public void zoomTo( double start, double end )
  {
    if ( _currentReferenceSequence == null )
      return;

    start = Math.max( start, 0 );
    end   = Math.min( end, (double) _currentReferenceSequence.getSequenceLength() );

    _tiersPanel.zoomTo( start, end );

    setNavigationText( start, end );
    
    this.currentStart = start;
    this.currentEnd = end;
  }

  private void displayParesnipResults( File paresnipFile, File mirbaseFile )
  {
    if ( paresnipFile == null || ! paresnipFile.exists() )
      return;

    // Read in the paresnip results
    //
    _paresnipResults = null;

    setBusyCursor( true );

    try
    {
      _paresnipResults = ParesnipIO.readIn( paresnipFile );
    }
    catch ( IOException ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );

      displayStatusMessage( "An error occurred while loading data from the file " + paresnipFile, false );

      return;
    }
    finally
    {
      setBusyCursor( false );
    }

    if ( mirbaseFile != null && mirbaseFile.exists() )
    {
      setBusyCursor( true );

      try
      {
        FastaFileReader ffr = new FastaFileReader( mirbaseFile );

        if ( ffr.processFile() )
        {
          List< SequenceRangeI > mirbaseRecords = ffr.getFastaRecords();

          if ( ! mirbaseRecords.isEmpty() )
          {
            _paresnipResults.performMirbaseLookup( mirbaseRecords );
          }
        }
      }
      finally
      {
        setBusyCursor( true );
      }
    }

    String sequenceId = "File: " + paresnipFile.getName();
    displayParesnipResults( sequenceId, _paresnipResults );
  }

  /**
   * Show the PARESnip results, first tier is for category 0, second for category 1, etc.
   *
   * @param sequenceId
   * @param prc The collection to show. It is usually the instance variable but can be a filtered.
   */
  private void displayParesnipResults( String sequenceId, PlotRecordCollection prc )
  {
    // Create a 'dummy' sequence
    //
    SequenceRangeI referenceSequence = prc.createReferenceSequence( sequenceId );
    setReferenceSequences( Arrays.asList( referenceSequence ), "", VisSRGlobals.ViewMode.PARESNIP );

    // Put the data into the tiers
    //
    for ( Category c : Category.definedCategories() )
    {
      List< PlotRecord > lpr = prc.getPlotRecordsForCategory( c );

      TierParameters tp = new TierParameters.Builder( c.toString() )
        .glyphClass( LabelledVerticallyAlignedRectGlyph.class )
        .glyphBackgroundColour( c.getCategoryColor() )
        .tierType( TierParameters.TierType.USER_SPECIFIED_PARESNIP )
        .build();

      tp.addListForId( sequenceId, lpr );

      _tiersPanel.addTierParameters( tp );
    }

    _tiersPanel.displayData();

    // extra formatting for Paresnip mode
    displayStatusMessage( "", false );
    txtNav.setText( "" );
    cmbGovSeq.setToolTipText( null );

    // The view is not quite right - it needs some repainting at the bottom.
    // Calling displayData again sorts it out.
    //
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        _tiersPanel.displayData();
      }
    } );
  }

  public List<SequenceRangeI> getAllReferenceSequence()
  {
    return this._referenceSequences;
  }

  /***
   * The {@link SequenceRangeI}s are put in the combo using this wrapper class
   * which overrides toString() and allows direct access to the underlying object.
   */
  private static class ReferenceSequenceComboWrapper
  {
    private final SequenceRangeI _sr;

    ReferenceSequenceComboWrapper( SequenceRangeI sr )
    {
      _sr = sr;
    }

    SequenceRangeI getSequenceRangeI()
    {
      return _sr;
    }

    @Override
    public String toString()
    {
      return _sr.getSequenceId();
    }
  }

  /***
   * Call to update the whole of the user interface
   *
   * @param startIndex  The start index
   * @param stopIndex   The stop index
   */
  void updateViewableRange( double startIndex, double stopIndex )
  {
    double newStart = Math.max( startIndex, 0 );
    double newEnd   = Math.min( stopIndex, getCurrentReferenceSequenceLength() );

    _vdoPanel.updateShadowRectangle( newStart, newEnd );

    _tiersPanel.moveTo( newStart );

    setNavigationText( newStart, newEnd );
    
    this.currentStart = newStart;
    this.currentEnd = newEnd;
  }

  /**
   * Display a message in the status bar
   *
   * @param message The message text
   * @param append  Whether to append or replace
   */
  void displayStatusMessage( String message, boolean append )
  {
    lblStatusBar.setText( ( append ? lblStatusBar.getText() : " " ) + message );
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Action for jumping the view to the left and right
   */
  private class ScrollTierAction extends AbstractAction
  {
    private final int _jumpPercent;

    public ScrollTierAction( String name, String tooltip, int jumpPercent )
    {
      super( name );
      super.putValue( AbstractAction.SHORT_DESCRIPTION, tooltip );

      _jumpPercent = jumpPercent;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      _tiersPanel.jumpView( _jumpPercent );
    }
  }

  // Start of GUIInterface implementation...
  //
  @Override public void runProcedure()                {}
  @Override public JPanel getParamsPanel()                 { return null; }
  @Override public void setShowingParams( boolean b ) {}
  @Override public boolean getShowingParams()         { return false; }
  //
  //...end of GUIInterface implementation

  /** This method is called from within the constructor to initialise the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlVis = new javax.swing.JPanel();
        pnlTop = new javax.swing.JPanel();
        pnlNav = new javax.swing.JPanel();
        lblIgnore = new javax.swing.JLabel();
        txtNav = new javax.swing.JTextField();
        btnLL = new javax.swing.JButton();
        btnL = new javax.swing.JButton();
        btnR = new javax.swing.JButton();
        btnRR = new javax.swing.JButton();
        btnParesnipFilterResults = new javax.swing.JButton();
        btnParesnipClearFilter = new javax.swing.JButton();
        pnlOverview = new javax.swing.JPanel();
        pnlDetail = new javax.swing.JPanel();
        lblStatusBar = new javax.swing.JLabel();
        mb = new javax.swing.JMenuBar();
        menFile = new javax.swing.JMenu();
        miFileOpenFASTA = new javax.swing.JMenuItem();
        miFileOpenGFF = new javax.swing.JMenuItem();
        menFilePatman = new javax.swing.JMenu();
        miPatmanDisplayAll = new javax.swing.JMenuItem();
        miPatmanDisplayAggregated = new javax.swing.JMenuItem();
        miFileParesnip = new javax.swing.JMenuItem();
        sep1 = new javax.swing.JPopupMenu.Separator();
        miFileTakePicture = new javax.swing.JMenuItem();
        sep2 = new javax.swing.JPopupMenu.Separator();
        miFileTest = new javax.swing.JMenuItem();
        miClose = new javax.swing.JMenuItem();
        menViewTPlots = new javax.swing.JMenu();
        menHelp = new javax.swing.JMenu();
        miHelpContents = new javax.swing.JMenuItem();
        settingsMnu = new javax.swing.JMenuItem();

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("VisSR");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(800, 600));
        setName("_frame"); // NOI18N
        setRequestFocusEnabled(false);
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameOpened(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });

        pnlVis.setLayout(new java.awt.BorderLayout());

        pnlTop.setLayout(new java.awt.BorderLayout());

        pnlNav.setBackground(new java.awt.Color(120, 120, 120));
        pnlNav.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlNav.setForeground(java.awt.Color.white);
        pnlNav.setMaximumSize(new java.awt.Dimension(32767, 42));
        pnlNav.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlNav.setPreferredSize(new java.awt.Dimension(0, 42));

        lblIgnore.setText("Combo added here");
        lblIgnore.setOpaque(true);

        txtNav.setColumns(20);
        txtNav.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        txtNav.setToolTipText("<html>\nEnter range here.<br>\nUse the format 'start - end' or 'start + width' (kb and mb are shorthand for kilobases and megabases).<br>\nEnter an identifier to perform a search, e.g. a gene identifier.\n</html>");
        txtNav.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        txtNav.setMargin(new java.awt.Insets(0, 4, 0, 0));
        txtNav.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        txtNav.setMinimumSize(new java.awt.Dimension(24, 20));
        txtNav.setPreferredSize(new java.awt.Dimension(190, 20));

        btnLL.setBackground(new java.awt.Color(120, 120, 120));
        btnLL.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnLL.setText("<<");
        btnLL.setIconTextGap(0);
        btnLL.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnLL.setPreferredSize(new java.awt.Dimension(45, 23));

        btnL.setBackground(new java.awt.Color(120, 120, 120));
        btnL.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnL.setText("<");
        btnL.setIconTextGap(0);
        btnL.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnL.setPreferredSize(new java.awt.Dimension(45, 23));

        btnR.setBackground(new java.awt.Color(120, 120, 120));
        btnR.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnR.setText(">");
        btnR.setIconTextGap(0);
        btnR.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnR.setPreferredSize(new java.awt.Dimension(45, 23));

        btnRR.setBackground(new java.awt.Color(120, 120, 120));
        btnRR.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnRR.setText(">>");
        btnRR.setIconTextGap(0);
        btnRR.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnRR.setPreferredSize(new java.awt.Dimension(45, 23));

        btnParesnipFilterResults.setBackground(new java.awt.Color(120, 120, 120));
        btnParesnipFilterResults.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnParesnipFilterResults.setText("Filter results...");
        btnParesnipFilterResults.setIconTextGap(0);
        btnParesnipFilterResults.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btnParesnipFilterResults.setPreferredSize(new java.awt.Dimension(42, 23));

        btnParesnipClearFilter.setBackground(new java.awt.Color(120, 120, 120));
        btnParesnipClearFilter.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        btnParesnipClearFilter.setText("Clear filter");
        btnParesnipClearFilter.setIconTextGap(0);
        btnParesnipClearFilter.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btnParesnipClearFilter.setPreferredSize(new java.awt.Dimension(42, 23));

        javax.swing.GroupLayout pnlNavLayout = new javax.swing.GroupLayout(pnlNav);
        pnlNav.setLayout(pnlNavLayout);
        pnlNavLayout.setHorizontalGroup(
            pnlNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblIgnore, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(txtNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(btnLL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(btnL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(btnR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(btnRR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(btnParesnipFilterResults, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(btnParesnipClearFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlNavLayout.setVerticalGroup(
            pnlNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblIgnore))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(txtNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnLL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnRR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnParesnipFilterResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnlNavLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(btnParesnipClearFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlTop.add(pnlNav, java.awt.BorderLayout.PAGE_START);

        pnlOverview.setBackground(new java.awt.Color(120, 120, 120));
        pnlOverview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlOverview.setForeground(java.awt.Color.white);
        pnlOverview.setMaximumSize(new java.awt.Dimension(2147483647, 42));
        pnlOverview.setMinimumSize(new java.awt.Dimension(0, 42));
        pnlOverview.setPreferredSize(new java.awt.Dimension(0, 42));
        pnlOverview.setLayout(new java.awt.BorderLayout());
        pnlTop.add(pnlOverview, java.awt.BorderLayout.PAGE_END);

        pnlVis.add(pnlTop, java.awt.BorderLayout.PAGE_START);

        pnlDetail.setBackground(new java.awt.Color(120, 120, 120));
        pnlDetail.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlDetail.setLayout(new java.awt.BorderLayout());
        pnlVis.add(pnlDetail, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlVis, java.awt.BorderLayout.CENTER);

        lblStatusBar.setBackground(new java.awt.Color(120, 120, 120));
        lblStatusBar.setForeground(java.awt.Color.white);
        lblStatusBar.setText(" ");
        lblStatusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        lblStatusBar.setMaximumSize(new java.awt.Dimension(0, 18));
        lblStatusBar.setMinimumSize(new java.awt.Dimension(600, 18));
        lblStatusBar.setOpaque(true);
        lblStatusBar.setPreferredSize(new java.awt.Dimension(0, 18));
        getContentPane().add(lblStatusBar, java.awt.BorderLayout.PAGE_END);

        menFile.setMnemonic('f');
        menFile.setText("File");

        miFileOpenFASTA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/database.png"))); // NOI18N
        miFileOpenFASTA.setText("Open sequence file...");
        miFileOpenFASTA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFileOpenFASTAActionPerformed(evt);
            }
        });
        menFile.add(miFileOpenFASTA);

        miFileOpenGFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/edit.png"))); // NOI18N
        miFileOpenGFF.setText("Open annotations file...");
        miFileOpenGFF.setEnabled(false);
        miFileOpenGFF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFileOpenGFFActionPerformed(evt);
            }
        });
        menFile.add(miFileOpenGFF);

        menFilePatman.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/processing-02.png"))); // NOI18N
        menFilePatman.setText("Open Patman file");
        menFilePatman.setEnabled(false);

        miPatmanDisplayAll.setText("View all...");
        menFilePatman.add(miPatmanDisplayAll);

        miPatmanDisplayAggregated.setText("View aggregated...");
        menFilePatman.add(miPatmanDisplayAggregated);

        menFile.add(menFilePatman);

        miFileParesnip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/bar-chart.png"))); // NOI18N
        miFileParesnip.setText("View PARESnip results...");
        menFile.add(miFileParesnip);
        menFile.add(sep1);

        miFileTakePicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/screen.png"))); // NOI18N
        miFileTakePicture.setMnemonic('p');
        miFileTakePicture.setText("Take picture...");
        menFile.add(miFileTakePicture);
        menFile.add(sep2);

        miFileTest.setText("Run test");
        miFileTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFileTestActionPerformed(evt);
            }
        });
        menFile.add(miFileTest);

        miClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/close-tool.png"))); // NOI18N
        miClose.setText("Close");
        miClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCloseActionPerformed(evt);
            }
        });
        menFile.add(miClose);

        mb.add(menFile);

        menViewTPlots.setText("T-plots");
        mb.add(menViewTPlots);

        menHelp.setText("Help");

        miHelpContents.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        miHelpContents.setText("Contents");
        menHelp.add(miHelpContents);

        settingsMnu.setText("Settings");
        settingsMnu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMnuActionPerformed(evt);
            }
        });
        menHelp.add(settingsMnu);

        mb.add(menHelp);

        setJMenuBar(mb);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  /***
   * When the frame opens, initialise the view.
   * @param evt
   */
  private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameOpened
  {//GEN-HEADEREND:event_formInternalFrameOpened
    invokeRepaintLater();
  }//GEN-LAST:event_formInternalFrameOpened

  /***
   * Repaint when activated
   * @param evt
   */
  private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt)//GEN-FIRST:event_formInternalFrameActivated
  {//GEN-HEADEREND:event_formInternalFrameActivated
    repaint();
  }//GEN-LAST:event_formInternalFrameActivated

  /***
   * Repaint the window when it gets maximised
   * @param evt
   */
  private void formPropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_formPropertyChange
  {//GEN-HEADEREND:event_formPropertyChange
    if ( JInternalFrame.IS_MAXIMUM_PROPERTY.equals( evt.getPropertyName() ) )
    {
      // Only interested in the maximise event

      if ( evt.getNewValue() == null )
        return;
      if ( !( evt.getNewValue() instanceof Boolean ) )
        return;

      boolean isMaximising = ( (Boolean) evt.getNewValue() ).booleanValue();
      if ( !isMaximising )
        return;

      // Invoke the repaint later so that the frame has time to maximise.
      // Otherwise only the unmaximised portion gets repainted !
      invokeRepaintLater();
    }
  }//GEN-LAST:event_formPropertyChange

  private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
  {//GEN-HEADEREND:event_formComponentResized
    repaint();

    // Maintain the view-size
    Rectangle2D.Double rect = _vdoPanel.getShadowRectangleCoordBox();

    final double xStart = rect.x;
    final double xStop  = rect.x + rect.width;

    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        zoomTo( xStart, xStop );
      }
    } );
  }//GEN-LAST:event_formComponentResized

  private void miFileOpenGFFActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miFileOpenGFFActionPerformed
  {//GEN-HEADEREND:event_miFileOpenGFFActionPerformed
    if ( _currentReferenceSequence == null )
    {
      lblStatusBar.setText( " Please choose a sequence before loading annotations." );
      return;
    }

    GFFChooser gffPanel = new GFFChooser( _referenceSequences );

    try
    {
      int optionPaneResult = JOptionPane.showConfirmDialog( this,
        gffPanel,
        "Select GFF types",
        JOptionPane.OK_CANCEL_OPTION );

      if ( optionPaneResult != JOptionPane.OK_OPTION )
        return;

      setBusyCursor( true );

      // Get the lists
      gffTypesToDisplay = gffPanel.getSelectedGFFTypes();
      List<GFFRecord> gffRecords = new ArrayList<GFFRecord>( gffPanel.getGFFRecords() );
      
      //setup the lists in the colour chooser

      

      displayGFFAnnotations( gffTypesToDisplay, gffRecords );
      
      VSC.resetGFFColours(gffTypesToDisplay);
    }
    finally
    {
      setBusyCursor( false );

      gffPanel.dispose();
      System.gc();
    }
  }//GEN-LAST:event_miFileOpenGFFActionPerformed

  private void miFileOpenFASTAActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miFileOpenFASTAActionPerformed
  {//GEN-HEADEREND:event_miFileOpenFASTAActionPerformed
     
      SwingUtilities.invokeLater(() -> 
      {
          
          File sequenceFile = FileDialogUtils.showSingleFileOpenDialog(FileExtFilter.FASTA.getFilter(), this);
          loadSequencesFromFile(sequenceFile);
      });
  }//GEN-LAST:event_miFileOpenFASTAActionPerformed

  private void miFileTestActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miFileTestActionPerformed
  {//GEN-HEADEREND:event_miFileTestActionPerformed
    // Some test code...
    int test = 1;

    if ( test == 1 )
    {
      TierParameters tp = new TierParameters.Builder( "test" ).tierLabelBackgroundColour( Color.pink.darker() ).build();

      ArrayList<GFFRecord> l = new ArrayList<GFFRecord>();
      l.add( new GFFRecord( "Chr1", "src", "miRNA", 100, 1000, 0.0f, '+', (byte) 0 ) );
      l.add( new GFFRecord( "Chr1", "src", "miRNA", 200, 2000, 0.0f, '-', (byte) 0 ) );

      tp.addList( l );

      addTier( tp );
    }
    else if ( test == 2 )
    {
      // Load the sequence from a FASTA file

      //String seqFileName = "D:/LocalData/hugh/seq_data/plant/Ath_TAIR9.Chr1.fa";
      String seqFileName = "D:/LocalData/hugh/seq_data/plant/Ath_TAIR9.fa";
      //String seqFileName = "D:/LocalData/hugh/seq_data/scratch/test.3chr.fas";

      File seqFile = new File( seqFileName );

      if ( ! seqFile.exists() )
      {
        lblStatusBar.setText( " The file '" + seqFileName + "' does not exist." );
        return;
      }

      loadSequencesFromFile( seqFile );

      // Load the sequence annotations from a GFF file

      //String gffFileName = "D:/LocalData/hugh/seq_data/irina_data/TAIR9_GFF3_genes_transposons.Chr1.gff";
      String gffFileName = "D:/LocalData/hugh/seq_data/irina_data/TAIR9_GFF3_genes_transposons.gff";
      File gffFile = new File( gffFileName );

      if ( !gffFile.exists() )
      {
        lblStatusBar.setText( " The file '" + gffFileName + "' does not exist." );
        return;
      }

//      loadAnnotationsFromFile( gffFile );
    }
    else if ( test == 3 )
    {
      // Mimics programmatic access to VisSR from a tool running in its own thread

      class MyTest extends Thread
      {
        private SequenceVizMainFrame _vissr = null;

        MyTest() {}

        @Override
        public void run()
        {
          File aFile = new File( "D:/LocalData/hugh/seq_data/plant/Ath_TAIR9.fa" );

          //
          ArrayList<GFFRecord> l = new ArrayList<GFFRecord>();
          l.add( new GFFRecord( "Chr1", "src", "miRNA", 100, 1000, 0.0f, '+', (byte) 0 ) );
          l.add( new GFFRecord( "Chr1", "src", "miRNA", 200, 2000, 0.0f, '-', (byte) 0 ) );

          TierParameters tp = new TierParameters.Builder( "test" ).tierLabelBackgroundColour( Color.pink.darker() ).build();
          tp.addListForId( "Chr1", l );
          //

          //
          TierParameters tp2 = new TierParameters.Builder( "test2" ).tierLabelBackgroundColour( Color.pink.brighter() ).build();

          l = new ArrayList<GFFRecord>();
          l.add( new GFFRecord( "Chr1", "src", "miRNA", 2000, 3000, 0.0f, '+', (byte) 0 ) );
          tp2.addListForId( "Chr1", l );

          l = new ArrayList<GFFRecord>();
          l.add( new GFFRecord( "Chr2", "src", "miRNA", 500, 1500, 0.0f, '-', (byte) 0 ) );
          tp2.addListForId( "Chr2", l );
          //

          _vissr = SequenceVizMainFrame.createVisSRInstance( aFile, false, tp, tp2 );

          //
          l = new ArrayList<GFFRecord>();
          l.add( new GFFRecord( "Chr1", "src", "miRNA", 300, 500, 0.0f, '+', (byte) 0 ) );

          tp = new TierParameters.Builder( "test3" ).tierLabelBackgroundColour( Color.red ).build();
          tp.addListForId( "Chr1", l );
          //

          _vissr.addTier( tp );
        }
      }

      new MyTest().start();
    }
    else if ( test == 4 )
    {
      File paresnipFile = new File( "D:/LocalData/hugh/seq_data/paresnip/run1/PlotRecords.txt" );
      File mirbaseFile  = new File( "D:/LocalData/hugh/seq_data/paresnip/DNA_Medicago_Mature_miRNA_mirBase270411.fa" );

      displayParesnipResults( paresnipFile, mirbaseFile );
    }
  }//GEN-LAST:event_miFileTestActionPerformed

  private void miCloseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miCloseActionPerformed
  {//GEN-HEADEREND:event_miCloseActionPerformed
    this.dispose();
  }//GEN-LAST:event_miCloseActionPerformed

  private void settingsMnuActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_settingsMnuActionPerformed
  {//GEN-HEADEREND:event_settingsMnuActionPerformed
    
    VSC.setVisible( true);
    
    //this._tiersPanel.se
    //TierParameters.setEVENS_COLOUR( Color.YELLOW );
    //this._tiersPanel
    for ( String gffType : gffTypesToDisplay )
    {
      String printableGFFType = VisSRSequenceHelper.getPrintableGFFType( gffType );
      Color c = VisSRSequenceHelper.getColorForGFFType( gffType );
      _tiersPanel.modifyTierParameterColour( printableGFFType, c );
    }

    this._tiersPanel.reinitialise();
    
    this.zoomTo(currentStart, currentEnd);
    
    
  }//GEN-LAST:event_settingsMnuActionPerformed

  //////////////////////////////////////////////////////////////////////////////

  private void configureMenuItems()
  {
    miFileOpenGFF.setEnabled( _currentReferenceSequence != null );
    menFilePatman.setEnabled( _currentReferenceSequence != null );

    if ( miFileParesnip.isVisible() )
    {
      miFileParesnip.setEnabled( true );
    }
  }

  /***
   * Load the sequences from a FASTA file
   *
   * @param sequenceFile The FASTA file containing the sequences
   */
  private void loadSequencesFromFile( File sequenceFile )
  {
    if ( sequenceFile == null || ! sequenceFile.exists() )
      return;

    setBusyCursor( true );

    try
    {
      loadSequencesFromFileImpl( sequenceFile );
    }
    finally
    {
      setBusyCursor( false );
    }
  }

  private void loadSequencesFromFileImpl( File sequenceFile )
  {
    FastaFileReader ffr = new FastaFileReader( sequenceFile );
    List< SequenceRangeI > records = null;

    StopWatch sw = new StopWatch( "Load " + sequenceFile.getPath() );
    sw.start();

    if ( ffr.processFile() )
    {
      sw.lap( "Process file" );
      records = ffr.getFastaRecords();
    }

    if ( records.isEmpty() )
    {
      lblStatusBar.setText( " No sequences loaded from '" + sequenceFile.getPath() + "'" );
    }
    else
    {
      sw.lap();

      setReferenceSequences( records, sequenceFile.getPath(), VisSRGlobals.ViewMode.NORMAL );
      sw.lap( "Set Fasta records" );
    }

    sw.stop();
    //sw.printTimes();

    configureMenuItems();
  }

  VisSRGlobals.ViewMode getViewMode()
  {
    return _viewMode;
  }

  private void setViewMode( VisSRGlobals.ViewMode vm )
  {
    _viewMode = vm;

    // Configure the view based on the mode

    menViewTPlots.setVisible( _viewMode.isParesnip() );
    btnParesnipFilterResults.setVisible( _viewMode.isParesnip() );
    btnParesnipClearFilter.setVisible( _viewMode.isParesnip() );
    btnParesnipClearFilter.setEnabled( false );

    switch ( _viewMode )
    {
      case NORMAL:
        // Want to see the sequence and coordinates tiers
        _tiersPanel.initialiseView( true, true );
        // Show the overview panel
        pnlOverview.setVisible( true );
        break;

      case PARESNIP:
        // Don't show the sequence and coordinates tiers
        _tiersPanel.initialiseView( false, false );
        // Don't show the overview panel
        pnlOverview.setVisible( false );

        configureTPlotMenu();

        break;
    }
  }

  private void configureTPlotMenu()
  {
    if ( _configuredTPlotMenu )
      return;

    JMenuItem mi;

    // Add the 'view all T-plots' item
    mi = new JMenuItem( new AbstractAction( "View all..." )
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          _tiersPanel.showParesnipTplots( Category.definedCategories() );
        }
      } );

    menViewTPlots.add(  mi );

    // Add one for each category
    //
    for ( final Category cat : Category.values() )
    {
      if ( cat == Category.UNDEFINED )
        continue;

      mi = new JMenuItem( new AbstractAction( "Category " + cat.getCategoryValue() + "..." )
        {
          @Override
          public void actionPerformed( ActionEvent e )
          {
            _tiersPanel.showParesnipTplots( EnumSet.of( cat ) );
          }
        } );

      menViewTPlots.add(  mi );
    }

    _configuredTPlotMenu = true;
  }

  private void displayGFFAnnotations( List<String> gffTypesToDisplay, List<GFFRecord> records )
  {
    if ( gffTypesToDisplay == null || records == null )
      return;

    StopWatch sw = new StopWatch( "Display GFF annotations" );

    sw.lap();
    IndexedGFFRecords igr = new IndexedGFFRecords( records );
    igr.index();
    sw.lap( "Index GFF records" );

    for ( String gffType : gffTypesToDisplay )
    {
      String label = VisSRSequenceHelper.getPrintableGFFType( gffType );
      Color glyphColour = VisSRSequenceHelper.getColorForGFFType( gffType );

      TierParameters tp = new TierParameters.Builder( label )
        .glyphBackgroundColour( glyphColour )
        .glyphClass( GFFRecordGlyph.class )
        .build();

      for ( SequenceRangeI sr : _referenceSequences )
      {
        String govSeqId = sr.getSequenceId();

        // Get the list of 'SequenceRange' objects for the 2-part key : ( sequence id, gff type )
        List< GFFRecord > gffRecords = igr.getRecords( govSeqId, gffType );

        for ( GFFRecord gr : gffRecords )
        {
          gr.setParentSequenceI( sr );
        }

        tp.addListForId( govSeqId, gffRecords );
      }

      _tiersPanel.addTierParameters( tp );

      sw.lap( "Added tier parameters" );
    }

    igr.clear();
    sw.lap( "Cleared indexed gff records" );

    _tiersPanel.displayData();
    sw.lap( "Display data on tiers panel" );

    sw.stop();
    //sw.printTimes();

    System.gc();
  }

  /***
   * Sets the {@code FastaRecord} collection, adds the ids (usually chromosomes)
   * to the combo and selects the first one.
   * No action is taken if the collection is {@code null} or empty.
   *
   * @param records The {@code List} of {FastaRecord}s
   * @param fromWhere Informational text - can be {@code null}
   */
  private void setReferenceSequences( List< SequenceRangeI > referenceSequences, String fromWhere, VisSRGlobals.ViewMode vm )
  {
    if ( referenceSequences == null || referenceSequences.isEmpty() )
      return;

    setViewMode( vm );

    _referenceSequences = referenceSequences;
    _currentReferenceSequence = null;

    String message = String.format( "Loaded %d sequence%s", referenceSequences.size(), ( referenceSequences.size() == 1 ? "" : "s" ) );
    displayStatusMessage( message, false );

    if ( fromWhere != null && ! fromWhere.trim().isEmpty() )
    {
      displayStatusMessage( " from '" + fromWhere + "'", true );
    }

    // Put the wrapped records in the combo - use a wrapper to override toString()
    //
    cmbGovSeq.removeAllItems();
    for ( SequenceRangeI sr : _referenceSequences )
    {
      cmbGovSeq.addItem( new ReferenceSequenceComboWrapper( sr ) );
    }
    cmbGovSeq.setEnabled( _referenceSequences.size() != 1 );

    // Select the first entry in the collection - this calls the action event ( cmbGovSeq.addActionListener ... )
    cmbGovSeq.setSelectedIndex( 0 );

    System.gc();
  }

  /***
   * Add a tier based on the specified parameters.
   * If this method is not called from the event dispatch thread then the call
   * is postponed via a call to invokeLater in SwingUtilities.
   *
   * @param tp {@link TierParameters} object for the tier
   */
  public void addTier( final TierParameters tp )
  {
    if ( tp == null )
      return;

    if ( SwingUtilities.isEventDispatchThread() )
    {
      _tiersPanel.addTierParameters( tp );
      _tiersPanel.displayData();
    }
    else
    {
      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          addTier( tp );
        }
      } );
    }
  }

  /***
   * Put a repaint request on the event queue
   */
  private void invokeRepaintLater()
  {
    invokeRepaintLater( false, -1, -1 );
  }

  /***
   * Put a repaint request on the event queue. Zoom to a portion of the sequence if requested.
   * @param doZoom Whether to perform a zoom prior to the repaint
   * @param start  The start coordinate of the sequence for the zoom
   * @param end    The end coordinate of the sequence for the zoom
   */
  private void invokeRepaintLater( final boolean doZoom, final double start, final double end )
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        if ( doZoom )
        {
          zoomTo( start, end );
        }

        repaint();
      }
    } );

  }

  @Override
  public void dispose()
  {
    super.dispose();

    cmbGovSeq.removeAllItems();

    if ( _tiersPanel != null )
    {
      _tiersPanel.dispose();
      _tiersPanel = null;
    }

    _referenceSequences = null;
    _currentReferenceSequence = null;
    _paresnipResults = null;

    System.gc();
  }

  void setBusyCursor( boolean setBusy )
  {
    Cursor c = setBusy ? Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) : Cursor.getDefaultCursor();

    // The root and frame title panes
    //
    for ( Component cpt : getComponents() )
    {
      cpt.setCursor( c );
    }

    // The detail panel is not included in the above so set the cursor explicitly
    //
    for ( Component cpt : getContentPane().getComponents() )
    {
      cpt.setCursor( c );
    }

    cmbGovSeq.setCursor( c );

    // To let the cursor update on the screen
    ThreadUtils.safeSleep( 10 );
  }

  /**
   * Creates an instance of the visualisation frame, loads the reference sequences from the specified file and adds tiers.<br>
   * The frame is created in the current thread so that it can be returned. All loading code is now sequential to ensure the
   * genome is completely loaded before going further.
   *
   * @param genomeFile File containing the reference sequences
   * @param tps Variable length array of {@link TierParameters} objects
   *
   * @return The visualisation frame
   */
  public static SequenceVizMainFrame createVisSRInstance( final File genomeFile, boolean normalised, final TierParameters... tps )
  {
    final SequenceVizMainFrame vissr = new SequenceVizMainFrame(normalised);

//    try
//    {
//
//      //vissr.setMaximum( true );
//    }
//    catch ( PropertyVetoException ex )
//    {
//      LOGGER.log( Level.SEVERE, "Vissr creation error{0}", ex.getMessage());
//    }
    vissr._initialisingReferenceSequence = true;

    // Add VisSR to the desktop pane...
    MDIDesktopPane pane = AppUtils.INSTANCE.getMDIDesktopPane();

    if ( pane != null )
    {
      pane.add( vissr );
    }

    ToolManager.getInstance().addTool( vissr );

    vissr.setVisible( true );
    try
    {
      vissr.setMaximum( true );
    }
    catch ( PropertyVetoException ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage() );
    }

    // Load the sequences...
    vissr.loadSequencesFromFile( genomeFile );

    if ( tps != null )
    {
      //...and display the tier(s)
      for ( TierParameters tp : tps )
      {
        vissr.addTier( tp );
      }
    }
    vissr._initialisingReferenceSequence = false;

    return vissr;
  }

  /**
   * Creates an instance of the visualisation frame for PARESnip<br>
   * The frame is created in the current thread so that it can be returned.
   * The remaining code is run on the event dispatch thread.
   *
   * @param prc Collection of PlotRecord objects
   * @param showTPlots Whether to perform a 'show all T-plots' as well
   *
   * @return The visualisation frame
   */
  public static SequenceVizMainFrame createVisSRInstanceForPARESnip( final PlotRecordCollection prc, final boolean showTPlots )
  {
    final SequenceVizMainFrame vissr = new SequenceVizMainFrame(false);

    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        // Add VisSR to the desktop pane...
        MDIDesktopPane pane = AppUtils.INSTANCE.getMDIDesktopPane();

        if ( pane != null )
        {
          pane.add( vissr );
        }

        ToolManager.getInstance().addTool( vissr );

        vissr._paresnipResults = prc;
        vissr.setVisible( true );
        vissr.displayParesnipResults( "PARESnip results", prc );

        if ( showTPlots )
        {
          vissr._tiersPanel.showParesnipTplots( Category.definedCategories() );
        }
      }
    } );

    return vissr;
  }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnL;
    private javax.swing.JButton btnLL;
    private javax.swing.JButton btnParesnipClearFilter;
    private javax.swing.JButton btnParesnipFilterResults;
    private javax.swing.JButton btnR;
    private javax.swing.JButton btnRR;
    private javax.swing.JLabel lblIgnore;
    private javax.swing.JLabel lblStatusBar;
    private javax.swing.JMenuBar mb;
    private javax.swing.JMenu menFile;
    private javax.swing.JMenu menFilePatman;
    private javax.swing.JMenu menHelp;
    private javax.swing.JMenu menViewTPlots;
    private javax.swing.JMenuItem miClose;
    private javax.swing.JMenuItem miFileOpenFASTA;
    private javax.swing.JMenuItem miFileOpenGFF;
    private javax.swing.JMenuItem miFileParesnip;
    private javax.swing.JMenuItem miFileTakePicture;
    private javax.swing.JMenuItem miFileTest;
    private javax.swing.JMenuItem miHelpContents;
    private javax.swing.JMenuItem miPatmanDisplayAggregated;
    private javax.swing.JMenuItem miPatmanDisplayAll;
    private javax.swing.JPanel pnlDetail;
    private javax.swing.JPanel pnlNav;
    private javax.swing.JPanel pnlOverview;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JPanel pnlVis;
    private javax.swing.JPopupMenu.Separator sep1;
    private javax.swing.JPopupMenu.Separator sep2;
    private javax.swing.JMenuItem settingsMnu;
    private javax.swing.JTextField txtNav;
    // End of variables declaration//GEN-END:variables

  private final class LoadPatmanDataAction extends AbstractAction
  {
    private final boolean _aggregateData;

    LoadPatmanDataAction( String name, boolean aggregate )
    {
      super( name );

      _aggregateData = aggregate;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( _currentReferenceSequence == null )
      {
        lblStatusBar.setText( " Please choose a sequence before loading a Patman file." );
        return;
      }

      int numberRequired = _aggregateData ? SequenceWindowHelper.NUM_LENGTHS : -1;
      LengthChooser lengthChooser = new LengthChooser( _currentReferenceSequence.getSequenceId(), _aggregateData, numberRequired );

      List<Integer> lengths = null;

      if ( numberRequired == -1 )
      {
        // -1 is special => can pick any number, even zero

        int result = JOptionPane.showConfirmDialog( SequenceVizMainFrame.this, lengthChooser,
          "Select sequence lengths",
          JOptionPane.OK_CANCEL_OPTION );

        if ( result != JOptionPane.OK_OPTION )
          return;

        lengths = lengthChooser.getSelectedLengths();

        if ( lengths.isEmpty() )
          return;
      }
      else
      {
        // Must pick the specified number

        while ( true )
        {
          int result = JOptionPane.showConfirmDialog( SequenceVizMainFrame.this, lengthChooser,
            "Select " + numberRequired + " sequence lengths",
            JOptionPane.OK_CANCEL_OPTION );

          if ( result != JOptionPane.OK_OPTION )
            return;

          lengths = lengthChooser.getSelectedLengths();

          if ( lengths.size() <= numberRequired )
            break;

          JOptionPane.showMessageDialog( SequenceVizMainFrame.this,
            "Please choose " + numberRequired + " sequence lengths, or cancel.",
            "Sequence lengths",
            JOptionPane.INFORMATION_MESSAGE );
        }
      }

      lblStatusBar.setText( " Using sequence lengths " + lengths + " for sequence '" + _currentReferenceSequence.getSequenceId() + "'" );

      Patman p = lengthChooser.getPatman();
      Patman plen = p.performSizeClassReduction( _currentReferenceSequence.getSequenceId(), new HashSet<Integer>( lengths ) );

      if ( plen == null || plen.isEmpty() )
      {
        lblStatusBar.setText( lblStatusBar.getText() + " - No data returned for specified lengths and sequence" );
        return;
      }

      if ( ! _aggregateData )
      {
        int startIndex = getIndex( true, _currentReferenceSequence.getSequenceLength() );
        if ( startIndex == -1 )
          return;

        int endIndex = getIndex( false, _currentReferenceSequence.getSequenceLength() );
        if ( endIndex == -1 )
          return;

        plen = plen.performStartStopReduction( "", startIndex, endIndex, Patman.StraddleInclusionCriterion.MAJORITY );
      }

      setBusyCursor( true );

      try
      {
        if ( _aggregateData )
        {
          displayAggregatedPatmanData( lengths, plen );
        }
        else
        {
          displayRawPatmanData( lengthChooser.getFileName(), plen );
        }
      }
      finally
      {
        setBusyCursor( false );
      }
    }

    private int getIndex( boolean isStartIndex, int size )
    {
      // Get the index (ask for the base position => subtract 1 to get the index)

      Object obj = JOptionPane.showInputDialog( SequenceVizMainFrame.this,
            "Please enter the " + ( isStartIndex ? "start" : "end" ) + " base position ?",
            ( isStartIndex ? "Start" : "Stop" ) + " position",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            ( isStartIndex ? 1 : Math.min( 100000, size ) ) );

      if ( obj == null )
        return -1;

      int idx = 1;

      if ( isStartIndex )
      {
        idx = Math.max( 1, StringUtils.safeIntegerParse( obj.toString(), 1 ) ) - 1;
      }
      else
      {
        idx = Math.min( size, StringUtils.safeIntegerParse( obj.toString(), 1 ) ) - 1;
      }

      return idx;
    }

    /***
     * Divide up the {@link Patman} data into windows and add to a tier.
     * The user is prompted for configuration, e.g. number of windows, start and end position.
     *
     * @param p The source {@link Patman} object containing the data
     */
    private void displayAggregatedPatmanData( List< Integer > lengths, Patman p )
    {
      SequenceWindowHelper swh = new SequenceWindowHelper( _currentReferenceSequence, lengths );

      if ( ! swh.getUserInput( SequenceVizMainFrame.this ) )
      {
        String msg = swh.getMessage();

        if ( msg != null && msg.length() > 0 )
        {
          lblStatusBar.setText( " " + msg );
        }

        return;
      }

      // Aggregate the length-filtered Patman data into 'windows'
      //
      List< SequenceWindow > sequenceWindows = swh.generateSequenceWindows( p );

      // Create a tier
      //
      int windowsStartIndex = swh.getStartIndex();
      int windowsStopIndex = swh.getStopIndex();
      String sampleLabel = "Sample [" + windowsStartIndex + "-" + windowsStopIndex + "]";

      TierParameters tp = new TierParameters.Builder( sampleLabel ).glyphClass( StripedSolidRectGlyph.class ).build();

      tp.addListForId( _currentReferenceSequence.getSequenceId(), sequenceWindows );

      _tiersPanel.addTierParameters( tp );
      _tiersPanel.displayData();

      zoomTo( 0.98 * windowsStartIndex, 1.02 * windowsStopIndex );
    }

    private void displayRawPatmanData( String fileName, Patman p )
    {
      String labelPrefix = fileName.replace( ".patman", "" );

      p.sortByStartCoord();
      p.setGlyphHeightFromAbundance();

      TierParameters tp = new TierParameters.Builder( labelPrefix ).build();
      tp.addListForId( _currentReferenceSequence.getSequenceId(), p );

      _tiersPanel.addTierParameters( tp );
      _tiersPanel.displayData();
    }
  }
  //
  // End of action class


  private final class LoadParesnipDataAction extends AbstractAction
  {
    private File _paresnipFile = null;
    private File _mirbaseFile = null;

    private LoadParesnipDataAction( JMenuItem mi )
    {
      super( mi.getText(), mi.getIcon() );
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( getFiles() )
      {
        displayParesnipResults( _paresnipFile, _mirbaseFile );

        _paresnipFile = null;
        _mirbaseFile  = null;
      }
    }

    private boolean getFiles()
    {
      // Get the name of the Paresnip data file and the optional mirbase file (FASTA format)

      ParesnipFileChooser pfc = new ParesnipFileChooser();

      int optionPaneResult = JOptionPane.showConfirmDialog( SequenceVizMainFrame.this,
        pfc,  //createPanel(),
        "PARESnip data files",
        JOptionPane.OK_CANCEL_OPTION );

      if ( optionPaneResult == JOptionPane.OK_OPTION )
      {
        _paresnipFile = pfc.getParesnipFile();
        _mirbaseFile  = pfc.getMirbaseFile();

        return true;
      }

      return false;
    }
  }
  //
  // End of action class


  private final class FilterParesnipResultsAction extends AbstractAction
  {
    private final boolean _applyFilter;
    private final PlotRecordFilterList _filterList;

    private FilterParesnipResultsAction( String name, boolean applyFilter )
    {
      super( name );

      _applyFilter = applyFilter;
      _filterList = new PlotRecordFilterList();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      if ( _applyFilter )
      {
        PlotRecordCollection filteredResults = new PlotRecordCollection();

        if ( _filterList.doFiltering( SequenceVizMainFrame.this, _paresnipResults, filteredResults ) )
        {
          displayParesnipResults( _currentReferenceSequence.getSequenceId(), filteredResults );

          btnParesnipClearFilter.setEnabled( true );
        }
      }
      else
      {
        // They need re-indexing !
        _paresnipResults.createIndexIgnoreState();

        // Show all
        displayParesnipResults( _currentReferenceSequence.getSequenceId(), _paresnipResults );

        btnParesnipClearFilter.setEnabled( false );
      }
    }
  }
  //
  // End of action class
}
