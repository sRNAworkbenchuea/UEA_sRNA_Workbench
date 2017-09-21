/*
 * MirprofParamsPanel.java
 *
 * Created on 11-Oct-2011, 10:40:35
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.StyledDocument;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBase;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.Updater;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParamsHost;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterGroup;
import static uk.ac.uea.cmp.srnaworkbench.tools.mirprof.MirprofParams.Definition.*;
import uk.ac.uea.cmp.srnaworkbench.utils.RegexPatternFormatter;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;


/**
 *
 * @author w0445959
 */
final class MirprofParamsPanel extends javax.swing.JPanel implements ToolParamsHost
{
  private MirprofParams params;
  private StyledDocument myTextLog;

  /** Creates new form MirprofParamsGUI */
  public MirprofParamsPanel()
  {
    initComponents();

    // Formatter for number params text boxes
    java.util.regex.Pattern numberFilter = java.util.regex.Pattern.compile( "[0-9]*" );
    RegexPatternFormatter numberRegexFormatter = new RegexPatternFormatter( numberFilter );
    numberRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory numberFactory = new DefaultFormatterFactory( numberRegexFormatter );
    this.txtMinLength.setFormatterFactory( numberFactory );
    this.txtMaxLength.setFormatterFactory( numberFactory );
    this.txtMinLength.setText( "" + MINIMUM_LENGTH.getDefault( Integer.class ) );
    this.txtMaxLength.setText( "" + MAXIMUM_LENGTH.getDefault( Integer.class ) );

    java.util.regex.Pattern mmFilter = java.util.regex.Pattern.compile( "[0-3]" );
    RegexPatternFormatter mmRegexFormatter = new RegexPatternFormatter( mmFilter );
    mmRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory mmFactory = new DefaultFormatterFactory( mmRegexFormatter );
    this.txtMismatches.setFormatterFactory( mmFactory );
    this.txtMismatches.setText( "" + ALLOWED_MISMATCHES.getDefault( Integer.class ) );


    this.tbParams.setHost( this );

    myTextLog = this.txtLog.getStyledDocument();
    Tools.addStylesToDocument( myTextLog );
    setVisible( true );
    setEnabled( true );
  }

  public boolean checkMiRBaseConfiguration()
  {
    
    // Get latest installed miRBase version and set as default value
    String latestMirBase = "";
    try
    {
      latestMirBase = MirBase.getLatestLocalVersion();
    }
    catch ( Exception e )
    {
      
    }
    if ( latestMirBase == null || latestMirBase.isEmpty() )//nothing valid was found
    {
      int option = JOptionPane.showConfirmDialog( this,
        "The requested miRBase version is not available locally, " + LINE_SEPARATOR +  "do you wish to try and download it?",
        "mirBase Updater",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE );

      if ( option == JOptionPane.YES_OPTION )
      {
        try
        {
          Updater.download( false );
          
          latestMirBase = MirBase.getLatestLocalVersion();
          
          JOptionPane.showMessageDialog(this,
                    "miRBase updated successfully.  Latest version: " + latestMirBase,
                    "mirBase Updater",
                    JOptionPane.INFORMATION_MESSAGE);
          
        }
        catch ( Exception e2 )
        {
          JOptionPane.showMessageDialog( this,
          "Something is wrong with the update procedure" + LINE_SEPARATOR + "Please check your internet connection or file a bug report",
          "mirBase Updater",
          JOptionPane.ERROR_MESSAGE );
          LOGGER.log( Level.WARNING, "Can''t get a working version of miRBase: {0}", e2.getMessage());
          
          return false;
        }
       
      }
      else
      {
        JOptionPane.showMessageDialog( this,
          "A miRNA database is required for miRProf to function" + LINE_SEPARATOR + "The tool must close",
          "mirBase Updater",
          JOptionPane.ERROR_MESSAGE );
        this.txtMirBaseVersion.setText( "N/A" );
        this.lblMirBaseVersionDetails.setText( "(latest installed version: N/A)" );
        return false;
      }
    }
    //if reached this point without returning there must be a valid miRBase version
    try
    {
      
      
      this.txtMirBaseVersion.setText( latestMirBase );
      this.lblMirBaseVersionDetails.setText( "(latest installed version: " + latestMirBase + ")" );
      
      
    }
    catch ( Exception e )
    {
    }

    
    return true;

  }
  @Override
  public final void setEnabled( boolean enabled )
  {
    super.setEnabled( enabled );

    lblMirBaseVersion.setEnabled( enabled );
    txtMirBaseVersion.setEnabled( enabled );
    lblMirBaseVersionDetails.setEnabled( enabled );
    lblMirBaseCategory.setEnabled( enabled );
    cboMirBaseCategory.setEnabled( enabled );

    lblMismatches.setEnabled( enabled );
    txtMismatches.setEnabled( enabled );
    lblMismatchesDetails.setEnabled( enabled );
    lblMaxLength.setEnabled( enabled );
    txtMaxLength.setEnabled( enabled );
    lblMaxLenDetails.setEnabled( enabled );
    lblMinLength.setEnabled( enabled );
    txtMinLength.setEnabled( enabled );
    lblMinLenDetails.setEnabled( enabled );
    lblMinAbundance.setEnabled( enabled );
    txtMinAbundance.setEnabled( enabled );

    chkOverhangsAllowed.setEnabled( enabled );

    chkGroupMatureAndStar.setEnabled( enabled );
    chkGroupOrganisms.setEnabled( enabled );
    chkGroupVariants.setEnabled( enabled );
    chkGroupMismatches.setEnabled( enabled );
    chkKeepBest.setEnabled( enabled );

    tbParams.setEnabled( enabled );
  }
  
  public void reset()
  {
    this.params = new MirprofParams();
    this.update( params );
  }

  public boolean getConfig()
  {
    if ( this.isEnabled() )
    {
      return createParams();
    }
    return false;
  }

  public String getMirbaseCategory()
  {
    return this.cboMirBaseCategory.getSelectedItem().toString();
  }

  public String getMirbaseVersion()
  {
    return this.txtMirBaseVersion.getText().trim();
  }

  private boolean createParams()
  {
    try
    {
      String min_len_str = this.txtMinLength.getText().trim();
      String max_len_str = this.txtMaxLength.getText().trim();
      String min_abd_str = this.txtMinAbundance.getText().trim();
      String mismatches_str = this.txtMismatches.getText().trim();

      int min_len = StringUtils.safeIntegerParse( min_len_str, MINIMUM_LENGTH.getDefault( Integer.class ) );
      int max_len = StringUtils.safeIntegerParse( max_len_str, MAXIMUM_LENGTH.getDefault( Integer.class ) );
      int min_abd = StringUtils.safeIntegerParse( min_abd_str, MINIMUM_ABUNDANCE.getDefault( Integer.class ) );
      int mismatches = StringUtils.safeIntegerParse( mismatches_str, ALLOWED_MISMATCHES.getDefault( Integer.class ) );
      
      ArrayList<String> opts_strings = new ArrayList<String>();
      if ( params.getGroupMismatches() )
      {
        opts_strings.add( "ignore mismatches" );
      }
      if ( params.getGroupOrganisms() )
      {
        opts_strings.add( "combine organisms" );
      }
      if ( params.getGroupVariant() )
      {
        opts_strings.add( "combine variants" );
      }

      String groupOptionsString = opts_strings.size() > 0 ? org.apache.commons.lang3.StringUtils.join( opts_strings, ", " ) : "none";

      params = new MirprofParams.Builder()
        .setOverhangsAllowed( this.chkOverhangsAllowed.isSelected() )
        .setMismatches( mismatches ).setOnlyKeepBest( this.chkKeepBest.isSelected() )
        .setGroupMismatches( this.chkGroupMismatches.isSelected() )
        .setGroupOrganisms( this.chkGroupOrganisms.isSelected() )
        .setGroupVariant( this.chkGroupVariants.isSelected() )
        .setGroupMatureAndStar( this.chkGroupMatureAndStar.isSelected() )
        .setMinLength( min_len ).setMaxLength( max_len ).setMinAbundance( min_abd )
        .setMirBaseVersion( this.txtMirBaseVersion.getText() )
        .setGroupOptionsString(groupOptionsString)
        .build();
    }
    catch ( Exception ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      JOptionPane.showMessageDialog( this,
        ex.getMessage(),
        "miRProf Input Error",
        JOptionPane.ERROR_MESSAGE );

      return false;
    }
    return true;
  }

  public StyledDocument getLog()
  {
    return this.myTextLog;
  }

  public JTable getReadCountsTable()
  {
    return this.tblFilteringResults;
  }

  /**
   * Updates log window with events during previous run
   * @param mirprof The engine
   */
  public void populateLog( final Mirprof mirprof )
  {


    EventQueue.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          final List<FilterGroup> filter_stages = mirprof.getFilterStats();

          //print database version
          myTextLog.insertString( myTextLog.getLength(), "miRBase database: ",
            myTextLog.getStyle( Tools.initStyles[2] ) );

          myTextLog.insertString( myTextLog.getLength(), mirprof.getMirBaseFileName() + LINE_SEPARATOR,
            myTextLog.getStyle( Tools.initStyles[7] ) );

          //print allowed mismatches
          myTextLog.insertString( myTextLog.getLength(), "number of allowed mismatches: ",
            myTextLog.getStyle( Tools.initStyles[2] ) );

          myTextLog.insertString( myTextLog.getLength(), params.getMismatches() + LINE_SEPARATOR,
            myTextLog.getStyle( Tools.initStyles[7] ) );

          //print options on best matches
          String keep_string = params.getOnlyKeepBest() ? "keep best matches only" : "keep all matches";
          myTextLog.insertString( myTextLog.getLength(), "Mismatch filtering: ",
            myTextLog.getStyle( Tools.initStyles[2] ) );

          myTextLog.insertString( myTextLog.getLength(), keep_string + LINE_SEPARATOR,
            myTextLog.getStyle( Tools.initStyles[7] ) );

          //print grouping options
          myTextLog.insertString( myTextLog.getLength(), "Grouping options used: ",
            myTextLog.getStyle( Tools.initStyles[2] ) );

          myTextLog.insertString( myTextLog.getLength(), mirprof.getGroupOptionsString() + LINE_SEPARATOR,
            myTextLog.getStyle( Tools.initStyles[7] ) );

          //print the filenames for each sample
          myTextLog.insertString( myTextLog.getLength(), "Sample filenames: " + LINE_SEPARATOR,
            myTextLog.getStyle( Tools.initStyles[2] ) );

          for ( int i = 0; i < filter_stages.size(); i++ )
          {
            myTextLog.insertString( myTextLog.getLength(), filter_stages.get( i ).getID() + LINE_SEPARATOR,
              myTextLog.getStyle( Tools.initStyles[7] ) );
          }

          //Update the model here
          tblFilteringResults.setVisible( true );
          DefaultTableModel model = (DefaultTableModel) tblFilteringResults.getModel();
          model.addColumn( "Stage Name" );

          for ( int i = 0; i < filter_stages.size(); i++ )
          {
            model.addColumn( filter_stages.get( i ).getID() + " _total" );

            model.addColumn( filter_stages.get( i ).getID() + "_distinct" );

          }

          for ( int j = 0; j < filter_stages.get( 0 ).size(); j++ )
          {
            int rowIndex = 0;
            Object[] row = new Object[( filter_stages.size() * 2 ) + 1];
            row[rowIndex] = filter_stages.get( 0 ).get( j ).getStageName();
            rowIndex++;

            for ( int i = 0; i < filter_stages.size(); i++ )
            {
              row[rowIndex] = filter_stages.get( i ).get( j ).getTotalReadCount();
              rowIndex++;
              row[rowIndex] = filter_stages.get( i ).get( j ).getDistinctReadCount();
              rowIndex++;
            }
            model.addRow( row );
          }

          tblFilteringResults.setModel( model );
          for ( int i = 0; i < model.getColumnCount(); i++ )
          {
            tblFilteringResults.getColumnModel().getColumn( i ).setPreferredWidth( 150 );
          }
        }
        catch ( BadLocationException ex )
        {
          LOGGER.log( Level.SEVERE, null, ex );
        }

      }
    } );
  }

  // ***** ToolParamsHost implementation *****

  @Override
  public MirprofParams getParams()
  {
    if ( !createParams() )
    {
      return null;
    }

    return this.params;
  }

  @Override
  public void update( ToolParameters params )
  {
    if ( !( params instanceof MirprofParams ) )
    {
      showErrorDialog("Unexpected error: parameters object provided is not a valid mirprof params file.");
      return;
    }

    MirprofParams mp = (MirprofParams)params;

    this.txtMinLength.setText( Integer.toString( mp.getMinLength() ) );
    this.txtMaxLength.setText( Integer.toString( mp.getMaxLength() ) );
    this.txtMinAbundance.setText( Integer.toString( mp.getMinAbundance() ) );
    this.txtMismatches.setText( Integer.toString( mp.getMismatches() ) );
    this.chkOverhangsAllowed.setSelected( mp.getOverhangsAllowed() );
    this.chkKeepBest.setSelected( mp.getOnlyKeepBest() );
    this.chkGroupOrganisms.setSelected( mp.getGroupOrganisms() );
    this.chkGroupVariants.setSelected( mp.getGroupVariant() );
    this.chkGroupMatureAndStar.setSelected( mp.getGroupMatureAndStar() );
    this.chkGroupMismatches.setSelected( mp.getGroupMismatches() );
  }

  @Override
  public void showErrorDialog( String message )
  {
    JOptionPane.showMessageDialog( this,
      message,
      "Parameter Error",
      JOptionPane.ERROR_MESSAGE );
  }


  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlParams = new javax.swing.JPanel();
        pnlFiltering = new javax.swing.JPanel();
        lblMinLength = new javax.swing.JLabel();
        txtMinLength = new javax.swing.JFormattedTextField();
        lblMinLenDetails = new javax.swing.JLabel();
        lblMaxLength = new javax.swing.JLabel();
        txtMaxLength = new javax.swing.JFormattedTextField();
        lblMaxLenDetails = new javax.swing.JLabel();
        lblMinAbundance = new javax.swing.JLabel();
        txtMinAbundance = new javax.swing.JFormattedTextField();
        pnlMirbase = new javax.swing.JPanel();
        cboMirBaseCategory = new javax.swing.JComboBox();
        lblMirBaseCategory = new javax.swing.JLabel();
        lblMirBaseVersionDetails = new javax.swing.JLabel();
        lblMirBaseVersion = new javax.swing.JLabel();
        txtMirBaseVersion = new javax.swing.JFormattedTextField();
        pnlMatching = new javax.swing.JPanel();
        lblMismatches = new javax.swing.JLabel();
        txtMismatches = new javax.swing.JFormattedTextField();
        lblMismatchesDetails = new javax.swing.JLabel();
        chkOverhangsAllowed = new javax.swing.JCheckBox();
        chkKeepBest = new javax.swing.JCheckBox();
        pnlGrouping = new javax.swing.JPanel();
        chkGroupVariants = new javax.swing.JCheckBox();
        chkGroupOrganisms = new javax.swing.JCheckBox();
        chkGroupMatureAndStar = new javax.swing.JCheckBox();
        chkGroupMismatches = new javax.swing.JCheckBox();
        tbParams = new uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar();
        pnlLog = new javax.swing.JPanel();
        scrlLog = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextPane();
        pnlFilteringResults = new javax.swing.JPanel();
        scrlFilteringResults = new javax.swing.JScrollPane();
        tblFilteringResults = new javax.swing.JTable();

        setBackground(new java.awt.Color(120, 120, 120));

        pnlParams.setBackground(new java.awt.Color(120, 120, 120));
        pnlParams.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MiRProf Parameters", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        pnlParams.setAutoscrolls(true);
        pnlParams.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        pnlParams.setPreferredSize(new java.awt.Dimension(271, 647));

        pnlFiltering.setBackground(new java.awt.Color(120, 120, 120));
        pnlFiltering.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtering", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        pnlFiltering.setForeground(new java.awt.Color(255, 255, 255));

        lblMinLength.setBackground(new java.awt.Color(120, 120, 120));
        lblMinLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMinLength.setForeground(new java.awt.Color(255, 255, 255));
        lblMinLength.setText("Min. length:");

        txtMinLength.setText("16");
        txtMinLength.setToolTipText("<html> The minimum length for sequences in the sRNA dataset. </html>");
        txtMinLength.setEnabled(false);
        txtMinLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lblMinLenDetails.setBackground(new java.awt.Color(120, 120, 120));
        lblMinLenDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMinLenDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMinLenDetails.setText("(at least 16)");

        lblMaxLength.setBackground(new java.awt.Color(120, 120, 120));
        lblMaxLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMaxLength.setForeground(new java.awt.Color(255, 255, 255));
        lblMaxLength.setText("Max. length:");

        txtMaxLength.setText("35");
        txtMaxLength.setToolTipText("<html> The maximum length for sequences in the sRNA dataset. </html>");
        txtMaxLength.setEnabled(false);
        txtMaxLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lblMaxLenDetails.setBackground(new java.awt.Color(120, 120, 120));
        lblMaxLenDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMaxLenDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMaxLenDetails.setText("(no more than 35)");

        lblMinAbundance.setBackground(new java.awt.Color(120, 120, 120));
        lblMinAbundance.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMinAbundance.setForeground(new java.awt.Color(255, 255, 255));
        lblMinAbundance.setText("Min. abundance:");

        txtMinAbundance.setText("1");
        txtMinAbundance.setToolTipText("<html>  \nThe minimum abundance for distinct sequences in the sRNA datasets. <br/> \nThe abundance of a given distinct sequence represents the number of duplicates found for that sequence.   \n</html>");
        txtMinAbundance.setEnabled(false);
        txtMinAbundance.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        javax.swing.GroupLayout pnlFilteringLayout = new javax.swing.GroupLayout(pnlFiltering);
        pnlFiltering.setLayout(pnlFilteringLayout);
        pnlFilteringLayout.setHorizontalGroup(
            pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilteringLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblMinAbundance, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMaxLength, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMinLength, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtMinAbundance)
                    .addComponent(txtMaxLength)
                    .addComponent(txtMinLength, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMaxLenDetails)
                    .addComponent(lblMinLenDetails))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlFilteringLayout.setVerticalGroup(
            pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilteringLayout.createSequentialGroup()
                .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlFilteringLayout.createSequentialGroup()
                        .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMinLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMinLenDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMaxLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMaxLenDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMinAbundance, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFilteringLayout.createSequentialGroup()
                        .addComponent(lblMinLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMaxLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMinAbundance, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMirbase.setBackground(new java.awt.Color(120, 120, 120));
        pnlMirbase.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "miRBase", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        cboMirBaseCategory.setBackground(new java.awt.Color(240, 240, 240));
        cboMirBaseCategory.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cboMirBaseCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Animal", "Plant", "Virus" }));
        cboMirBaseCategory.setToolTipText("<html>\nIt is possible to target a subset of miRBase by selecting a category from the drop-down list.\n</html>");
        cboMirBaseCategory.setEnabled(false);

        lblMirBaseCategory.setBackground(new java.awt.Color(120, 120, 120));
        lblMirBaseCategory.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMirBaseCategory.setForeground(new java.awt.Color(255, 255, 255));
        lblMirBaseCategory.setText("miRBase category:");

        lblMirBaseVersionDetails.setBackground(new java.awt.Color(120, 120, 120));
        lblMirBaseVersionDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMirBaseVersionDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMirBaseVersionDetails.setText("(latest installed version: N/A)");

        lblMirBaseVersion.setBackground(new java.awt.Color(120, 120, 120));
        lblMirBaseVersion.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMirBaseVersion.setForeground(new java.awt.Color(255, 255, 255));
        lblMirBaseVersion.setText("miRBase version:");

        txtMirBaseVersion.setText("N/A");
        txtMirBaseVersion.setToolTipText("<html>\nThe miRBase version to compare against the sRNA sequences.<br/>\nSimply type the version you wish to comapre against into this box, if the mirBase version is not currently installed it will automatically be downloaded and installed. <br/>\nTo find the latest version goto http://www.mirbase.org/.  Alternatively, the latest version can be automatically downloaded and installed from the Workbench -> Help menu.\n</html>");
        txtMirBaseVersion.setEnabled(false);
        txtMirBaseVersion.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        javax.swing.GroupLayout pnlMirbaseLayout = new javax.swing.GroupLayout(pnlMirbase);
        pnlMirbase.setLayout(pnlMirbaseLayout);
        pnlMirbaseLayout.setHorizontalGroup(
            pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMirbaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMirbaseLayout.createSequentialGroup()
                        .addComponent(lblMirBaseCategory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboMirBaseCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMirbaseLayout.createSequentialGroup()
                        .addComponent(lblMirBaseVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMirBaseVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblMirBaseVersionDetails))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMirbaseLayout.setVerticalGroup(
            pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMirbaseLayout.createSequentialGroup()
                .addGroup(pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMirBaseVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMirBaseVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMirBaseVersionDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMirBaseCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboMirBaseCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMatching.setBackground(new java.awt.Color(120, 120, 120));
        pnlMatching.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Matching", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        lblMismatches.setBackground(new java.awt.Color(120, 120, 120));
        lblMismatches.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMismatches.setForeground(new java.awt.Color(255, 255, 255));
        lblMismatches.setText("Mismatches allowed:");

        txtMismatches.setText("1");
        txtMismatches.setToolTipText("<html>\nThe number of mismatches permitted before an sRNA sequence match to a miRBase mature sequence is discarded.<br/>\n0 indicates that only exact matches are permitted.  No more than 3 mismatches can be allowed in order to limit the number of false positives produced.<br/>\nIn cases where a single distinct sRNA matches to many mature miRBase sequences the user can check the \"only keep best match\" box in order to discard all sequences that have more than the minimum number of mismatches for this sRNA.\n</html>");
        txtMismatches.setEnabled(false);
        txtMismatches.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lblMismatchesDetails.setBackground(new java.awt.Color(120, 120, 120));
        lblMismatchesDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMismatchesDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMismatchesDetails.setText("(no more than 3)");

        chkOverhangsAllowed.setBackground(new java.awt.Color(120, 120, 120));
        chkOverhangsAllowed.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkOverhangsAllowed.setForeground(new java.awt.Color(255, 255, 255));
        chkOverhangsAllowed.setSelected(true);
        chkOverhangsAllowed.setText("Overhangs allowed");
        chkOverhangsAllowed.setToolTipText("If checked, miRProf will accept overhanging bases as mismatches.  If unchecked, sRNAs with overhanging bases are always rejected.");
        chkOverhangsAllowed.setEnabled(false);

        chkKeepBest.setBackground(new java.awt.Color(120, 120, 120));
        chkKeepBest.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkKeepBest.setForeground(new java.awt.Color(255, 255, 255));
        chkKeepBest.setText("Only keep best match");
        chkKeepBest.setToolTipText("If enabled, and allowed mismatches is greater than 0, then only the hits with the least mismatches per sequence are kept.");
        chkKeepBest.setEnabled(false);

        javax.swing.GroupLayout pnlMatchingLayout = new javax.swing.GroupLayout(pnlMatching);
        pnlMatching.setLayout(pnlMatchingLayout);
        pnlMatchingLayout.setHorizontalGroup(
            pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMatchingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMatchingLayout.createSequentialGroup()
                        .addGroup(pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(chkKeepBest, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlMatchingLayout.createSequentialGroup()
                                .addComponent(lblMismatches, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMismatches, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMismatchesDetails)))
                        .addContainerGap())
                    .addGroup(pnlMatchingLayout.createSequentialGroup()
                        .addComponent(chkOverhangsAllowed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16))))
        );
        pnlMatchingLayout.setVerticalGroup(
            pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMatchingLayout.createSequentialGroup()
                .addGroup(pnlMatchingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMismatches, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMismatches, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMismatchesDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkOverhangsAllowed)
                .addGap(3, 3, 3)
                .addComponent(chkKeepBest)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        pnlGrouping.setBackground(new java.awt.Color(120, 120, 120));
        pnlGrouping.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Match Grouping", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        chkGroupVariants.setBackground(new java.awt.Color(120, 120, 120));
        chkGroupVariants.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkGroupVariants.setForeground(new java.awt.Color(255, 255, 255));
        chkGroupVariants.setText("Group variants");
        chkGroupVariants.setToolTipText("If checked, matches to different variants of the miRNA are combined into one.");
        chkGroupVariants.setEnabled(false);

        chkGroupOrganisms.setBackground(new java.awt.Color(120, 120, 120));
        chkGroupOrganisms.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkGroupOrganisms.setForeground(new java.awt.Color(255, 255, 255));
        chkGroupOrganisms.setText("Group organisms");
        chkGroupOrganisms.setToolTipText("If checked, matches to the same miRNA in different organisms are combined into one.");
        chkGroupOrganisms.setEnabled(false);

        chkGroupMatureAndStar.setBackground(new java.awt.Color(120, 120, 120));
        chkGroupMatureAndStar.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkGroupMatureAndStar.setForeground(new java.awt.Color(255, 255, 255));
        chkGroupMatureAndStar.setText("Group mature and star");
        chkGroupMatureAndStar.setToolTipText("If checked, matches to star sequences are treated as though they matched only the mature sequence.");
        chkGroupMatureAndStar.setEnabled(false);

        chkGroupMismatches.setBackground(new java.awt.Color(120, 120, 120));
        chkGroupMismatches.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkGroupMismatches.setForeground(new java.awt.Color(255, 255, 255));
        chkGroupMismatches.setText("Group mismatches");
        chkGroupMismatches.setToolTipText("If checked, the matches to the same miRNA are combined into groups regarless of the number of mismatches.");
        chkGroupMismatches.setEnabled(false);

        javax.swing.GroupLayout pnlGroupingLayout = new javax.swing.GroupLayout(pnlGrouping);
        pnlGrouping.setLayout(pnlGroupingLayout);
        pnlGroupingLayout.setHorizontalGroup(
            pnlGroupingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGroupingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGroupingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGroupingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(chkGroupMatureAndStar)
                        .addComponent(chkGroupVariants, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(chkGroupOrganisms))
                    .addGroup(pnlGroupingLayout.createSequentialGroup()
                        .addComponent(chkGroupMismatches)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(115, 115, 115))
        );
        pnlGroupingLayout.setVerticalGroup(
            pnlGroupingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGroupingLayout.createSequentialGroup()
                .addComponent(chkGroupMismatches)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkGroupOrganisms)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkGroupVariants)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkGroupMatureAndStar)
                .addGap(34, 34, 34))
        );

        tbParams.setToolTipText("<html>\nLoad and save parameters to and from disk.\n</html>");

        javax.swing.GroupLayout pnlParamsLayout = new javax.swing.GroupLayout(pnlParams);
        pnlParams.setLayout(pnlParamsLayout);
        pnlParamsLayout.setHorizontalGroup(
            pnlParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tbParams, 0, 0, Short.MAX_VALUE)
            .addComponent(pnlFiltering, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlMirbase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlMatching, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlGrouping, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlParamsLayout.setVerticalGroup(
            pnlParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamsLayout.createSequentialGroup()
                .addComponent(tbParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(pnlFiltering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMirbase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMatching, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlGrouping, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlLog.setBackground(new java.awt.Color(120, 120, 120));
        pnlLog.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MiRProf Log", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        pnlLog.setAutoscrolls(true);

        txtLog.setBackground(new java.awt.Color(120, 120, 120));
        txtLog.setBorder(null);
        txtLog.setForeground(new java.awt.Color(255, 255, 255));
        scrlLog.setViewportView(txtLog);

        pnlFilteringResults.setBackground(new java.awt.Color(120, 120, 120));
        pnlFilteringResults.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtering Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        tblFilteringResults.setBackground(new java.awt.Color(120, 120, 120));
        tblFilteringResults.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N
        tblFilteringResults.setForeground(new java.awt.Color(255, 255, 255));
        tblFilteringResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblFilteringResults.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblFilteringResults.setFillsViewportHeight(true);
        tblFilteringResults.setGridColor(new java.awt.Color(153, 204, 255));
        tblFilteringResults.setRowHeight(30);
        tblFilteringResults.setRowMargin(10);
        tblFilteringResults.setSelectionBackground(new java.awt.Color(102, 102, 102));
        tblFilteringResults.setSelectionForeground(new java.awt.Color(153, 204, 255));
        scrlFilteringResults.setViewportView(tblFilteringResults);

        javax.swing.GroupLayout pnlFilteringResultsLayout = new javax.swing.GroupLayout(pnlFilteringResults);
        pnlFilteringResults.setLayout(pnlFilteringResultsLayout);
        pnlFilteringResultsLayout.setHorizontalGroup(
            pnlFilteringResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrlFilteringResults, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
        );
        pnlFilteringResultsLayout.setVerticalGroup(
            pnlFilteringResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrlFilteringResults, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlLogLayout = new javax.swing.GroupLayout(pnlLog);
        pnlLog.setLayout(pnlLogLayout);
        pnlLogLayout.setHorizontalGroup(
            pnlLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
            .addComponent(pnlFilteringResults, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlLogLayout.setVerticalGroup(
            pnlLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLogLayout.createSequentialGroup()
                .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFilteringResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlParams, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlParams, javax.swing.GroupLayout.PREFERRED_SIZE, 636, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboMirBaseCategory;
    private javax.swing.JCheckBox chkGroupMatureAndStar;
    private javax.swing.JCheckBox chkGroupMismatches;
    private javax.swing.JCheckBox chkGroupOrganisms;
    private javax.swing.JCheckBox chkGroupVariants;
    private javax.swing.JCheckBox chkKeepBest;
    private javax.swing.JCheckBox chkOverhangsAllowed;
    private javax.swing.JLabel lblMaxLenDetails;
    private javax.swing.JLabel lblMaxLength;
    private javax.swing.JLabel lblMinAbundance;
    private javax.swing.JLabel lblMinLenDetails;
    private javax.swing.JLabel lblMinLength;
    private javax.swing.JLabel lblMirBaseCategory;
    private javax.swing.JLabel lblMirBaseVersion;
    private javax.swing.JLabel lblMirBaseVersionDetails;
    private javax.swing.JLabel lblMismatches;
    private javax.swing.JLabel lblMismatchesDetails;
    private javax.swing.JPanel pnlFiltering;
    private javax.swing.JPanel pnlFilteringResults;
    private javax.swing.JPanel pnlGrouping;
    private javax.swing.JPanel pnlLog;
    private javax.swing.JPanel pnlMatching;
    private javax.swing.JPanel pnlMirbase;
    private javax.swing.JPanel pnlParams;
    private javax.swing.JScrollPane scrlFilteringResults;
    private javax.swing.JScrollPane scrlLog;
    private uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar tbParams;
    private javax.swing.JTable tblFilteringResults;
    private javax.swing.JTextPane txtLog;
    private javax.swing.JFormattedTextField txtMaxLength;
    private javax.swing.JFormattedTextField txtMinAbundance;
    private javax.swing.JFormattedTextField txtMinLength;
    private javax.swing.JFormattedTextField txtMirBaseVersion;
    private javax.swing.JFormattedTextField txtMismatches;
    // End of variables declaration//GEN-END:variables

  boolean getGroupMismatches()
  {
    return params.getGroupMismatches();
  }

  boolean getGroupOrganisms()
  {
    return params.getGroupOrganisms();
  }

  boolean getGroupVariant()
  {
    return params.getGroupVariant();
  }


}
