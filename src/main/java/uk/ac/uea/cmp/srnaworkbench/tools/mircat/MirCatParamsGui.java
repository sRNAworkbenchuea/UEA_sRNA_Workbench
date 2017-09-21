/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MirCatParamsGui.java
 *
 * Created on 18-Apr-2011, 13:24:58
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.StyledDocument;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBase;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.Updater;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParamsHost;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.MiRCatLogger.MIRCAT_LOGGER;

/**
 *
 * @author w0445959
 */
public final class MirCatParamsGui extends javax.swing.JPanel implements ToolParamsHost
{
  private MirCatMainFrame parentFrame = null;
  
  private MiRCatParams params;
  
  private StatusTracker tracker;
  private boolean usingMirBase = false;

  /** Creates new form MirCatParamsGui */
  public MirCatParamsGui( MirCatMainFrame parent )
  {
    initComponents();
    setupTextValidation();

    parentFrame = parent;
    
    createParams();
    
    this.tbParams.setHost( this );
    this.tracker = new StatusTracker( mirCatProgressBar, mirCatStatusLabel );
    
    String latestMirBase = "";
    try
    {
      latestMirBase = MirBase.getLatestLocalVersion();
    }
    catch ( Exception e )
    {
      
    }
    if ( latestMirBase == null || latestMirBase.isEmpty() )
    {
      int option = JOptionPane.showConfirmDialog( this,
        "The requested miRBase version is not available locally, do you wish to try and download it?",
        "mirBase Updater",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE );

      if ( option == JOptionPane.YES_OPTION )
      {
        try
        {
          Updater.download( false );
          try
          {
            latestMirBase = MirBase.getLatestLocalVersion();
            usingMirBase = true;
            JOptionPane.showMessageDialog(this,
                    "miRBase updated successfully.  Latest version: " + latestMirBase,
                    "mirBase Updater",
                    JOptionPane.INFORMATION_MESSAGE);
          }
          catch ( Exception e )
          {
          }
          
        }
        catch ( Exception e2 )
        {
          LOGGER.log( Level.WARNING, "Can''t get a working version of miRBase: {0}", e2.getMessage());
          usingMirBase = false;
        }
        this.txt_miRBaseVersion.setText( latestMirBase );
        this.lblMirBaseVersionDetails.setText( "(latest installed version: " + latestMirBase + ")" );
      }
      else
      {
        JOptionPane.showMessageDialog( this,
          "miRBase not updated, known miRNA will not be reported",
          "mirBase Updater",
          JOptionPane.INFORMATION_MESSAGE );
        usingMirBase = false;
        this.txt_miRBaseVersion.setText( "N/A" );
        this.lblMirBaseVersionDetails.setText( "(latest installed version: N/A)" );
      }

    }
    else
    {
      this.txt_miRBaseVersion.setText( latestMirBase );
        this.lblMirBaseVersionDetails.setText( "(latest installed version: " + latestMirBase + ")" );
    }
    
  }

  public void addTab( JPanel toAdd, String text )
  {
    this.tabLog.addTab( text, toAdd );
  }

  public void setupTextValidation()
  {
    java.util.regex.Pattern numberFilter = java.util.regex.Pattern.compile( "[0-9]+" );
    RegexPatternFormatter numberRegexFormatter = new RegexPatternFormatter( numberFilter );
    numberRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory numberFactory = new DefaultFormatterFactory( numberRegexFormatter );

    java.util.regex.Pattern negNumberFilter = java.util.regex.Pattern.compile( "(-)*[0-9]+(\\.)*([0-9])*" );
    RegexPatternFormatter negNumberRegexFormatter = new RegexPatternFormatter( negNumberFilter );
    negNumberRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory negNumberFactory = new DefaultFormatterFactory( negNumberRegexFormatter );
    
    java.util.regex.Pattern decNumberFilter = java.util.regex.Pattern.compile( "[0-9]+(\\.)*([0-9])*" );
    RegexPatternFormatter decNumberRegexFormatter = new RegexPatternFormatter( decNumberFilter );
    negNumberRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory decNumberFactory = new DefaultFormatterFactory( decNumberRegexFormatter );


    txtSeqExtension.setFormatterFactory( numberFactory );
    txtMFEThreshold.setFormatterFactory( negNumberFactory );
    txtMinPaired.setFormatterFactory( numberFactory );
    txtMaxGaps.setFormatterFactory( numberFactory );
    txtMaxHits.setFormatterFactory( numberFactory );
    txtMinSRNALength.setFormatterFactory( numberFactory );
    txtMaxSRNALength.setFormatterFactory( numberFactory );
    txtMinGCPct.setFormatterFactory( numberFactory );
    txtMaxUnpairedPct.setFormatterFactory( numberFactory );
    txtMaxOverlapPct.setFormatterFactory( numberFactory );
    txtMinSRNALocusSize.setFormatterFactory( numberFactory );
    txtMinOrientationPct.setFormatterFactory( numberFactory );
    txtMinHairpinLen.setFormatterFactory( numberFactory );
    txtMinSRNAAbundance.setFormatterFactory( numberFactory );
    txtMinClusterSepDist.setFormatterFactory( numberFactory );
    this.txt_ThreadCount.setFormatterFactory( numberFactory );
    txt_P_Val.setFormatterFactory( decNumberFactory );
  }

  public void inputRefs( MiRCatOpenFileOptions fileOpenOptions )
  {
    StyledDocument mirCatLog = this.mirCatLogPane.getStyledDocument();
    Tools.addStylesToDocument( mirCatLog );
    
    parentFrame.getEngine().addGUIRefs( mirCatLog, execQueueBar, compTasksBar );
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    paramsToolBar1 = new uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar();
    paramsPanel = new javax.swing.JPanel();
    lblSeqExtension = new javax.swing.JLabel();
    lblMFEThreshold = new javax.swing.JLabel();
    lblMaxUnpairedPct = new javax.swing.JLabel();
    lblMaxHits = new javax.swing.JLabel();
    lblMaxGaps = new javax.swing.JLabel();
    lblMinSRNALength = new javax.swing.JLabel();
    lblSRNASeqLength = new javax.swing.JLabel();
    lblMaxSRNALength = new javax.swing.JLabel();
    lblMinGCPct = new javax.swing.JLabel();
    lblMinSRNALocusSize = new javax.swing.JLabel();
    lblMinOrientationPct = new javax.swing.JLabel();
    lblMinHairpinLen = new javax.swing.JLabel();
    lblMinSRNAAbundance = new javax.swing.JLabel();
    lblClusterSepDist = new javax.swing.JLabel();
    lblMinPaired = new javax.swing.JLabel();
    lblMaxOverlapPct = new javax.swing.JLabel();
    txtSeqExtension = new javax.swing.JFormattedTextField();
    txtMFEThreshold = new javax.swing.JFormattedTextField();
    txtMaxUnpairedPct = new javax.swing.JFormattedTextField();
    txtMaxHits = new javax.swing.JFormattedTextField();
    txtMaxGaps = new javax.swing.JFormattedTextField();
    txtMinSRNALength = new javax.swing.JFormattedTextField();
    txtMaxSRNALength = new javax.swing.JFormattedTextField();
    txtMinGCPct = new javax.swing.JFormattedTextField();
    txtMinSRNALocusSize = new javax.swing.JFormattedTextField();
    txtMinOrientationPct = new javax.swing.JFormattedTextField();
    txtMinHairpinLen = new javax.swing.JFormattedTextField();
    txtMinSRNAAbundance = new javax.swing.JFormattedTextField();
    txtMinClusterSepDist = new javax.swing.JFormattedTextField();
    txtMinPaired = new javax.swing.JFormattedTextField();
    txtMaxOverlapPct = new javax.swing.JFormattedTextField();
    chkAllowComplexLoops = new javax.swing.JCheckBox();
    lblP_Val = new javax.swing.JLabel();
    txt_P_Val = new javax.swing.JFormattedTextField();
    txt_ThreadCount = new javax.swing.JFormattedTextField();
    lblThreadCount = new javax.swing.JLabel();
    mirCatLogPanel = new javax.swing.JPanel();
    tabLog = new javax.swing.JTabbedPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    mirCatLogPane = new javax.swing.JTextPane();
    outputLogFile = new javax.swing.JCheckBox();
    showMainParams = new javax.swing.JCheckBox();
    defaultParamsPane = new javax.swing.JPanel();
    defaultPlantParams = new javax.swing.JButton();
    defaultAnimalParams = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    mirCatProgressBar = new javax.swing.JProgressBar();
    mirCatStatusLabel = new javax.swing.JLabel();
    execQueueBar = new javax.swing.JProgressBar();
    compTasksBar = new javax.swing.JProgressBar();
    compTaskLbl = new javax.swing.JLabel();
    exeQueueLbl = new javax.swing.JLabel();
    tbParams = new uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar();
    pnlMirbase = new javax.swing.JPanel();
    lblMirBaseVersionDetails = new javax.swing.JLabel();
    miRBaseLabel = new javax.swing.JLabel();
    txt_miRBaseVersion = new javax.swing.JFormattedTextField();

    setBackground(new java.awt.Color(120, 120, 120));
    setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MiRCat Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 14), new java.awt.Color(153, 204, 255))); // NOI18N
    setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    paramsPanel.setBackground(new java.awt.Color(120, 120, 120));
    paramsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MirCAT Parameters", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(153, 204, 255))); // NOI18N
    paramsPanel.setAutoscrolls(true);
    paramsPanel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
    paramsPanel.setPreferredSize(new java.awt.Dimension(271, 647));

    lblSeqExtension.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblSeqExtension.setForeground(new java.awt.Color(255, 255, 255));
    lblSeqExtension.setText("sRNA Sequence Flank Extension:");
    lblSeqExtension.setToolTipText("");

    lblMFEThreshold.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMFEThreshold.setForeground(new java.awt.Color(255, 255, 255));
    lblMFEThreshold.setText("Minimum Free Energy Threshold:");

    lblMaxUnpairedPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMaxUnpairedPct.setForeground(new java.awt.Color(255, 255, 255));
    lblMaxUnpairedPct.setText("Max Unpaired Bases %:");

    lblMaxHits.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMaxHits.setForeground(new java.awt.Color(255, 255, 255));
    lblMaxHits.setText("Max Genome Hits:");

    lblMaxGaps.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMaxGaps.setForeground(new java.awt.Color(255, 255, 255));
    lblMaxGaps.setText("Max Consecutive Gaps:");

    lblMinSRNALength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinSRNALength.setForeground(new java.awt.Color(255, 255, 255));
    lblMinSRNALength.setText("Min:");

    lblSRNASeqLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblSRNASeqLength.setForeground(new java.awt.Color(255, 255, 255));
    lblSRNASeqLength.setText("sRNA Sequence Length:");

    lblMaxSRNALength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMaxSRNALength.setForeground(new java.awt.Color(255, 255, 255));
    lblMaxSRNALength.setText("Max:");

    lblMinGCPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinGCPct.setForeground(new java.awt.Color(255, 255, 255));
    lblMinGCPct.setText("Min GC %:");

    lblMinSRNALocusSize.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinSRNALocusSize.setForeground(new java.awt.Color(255, 255, 255));
    lblMinSRNALocusSize.setText("Min sRNA Locus Size:");

    lblMinOrientationPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinOrientationPct.setForeground(new java.awt.Color(255, 255, 255));
    lblMinOrientationPct.setText("Min sRNA Orientation %:");

    lblMinHairpinLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinHairpinLen.setForeground(new java.awt.Color(255, 255, 255));
    lblMinHairpinLen.setText("Min Hairpin Length:");

    lblMinSRNAAbundance.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinSRNAAbundance.setForeground(new java.awt.Color(255, 255, 255));
    lblMinSRNAAbundance.setText("Min sRNA Abundance (W):");

    lblClusterSepDist.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblClusterSepDist.setForeground(new java.awt.Color(255, 255, 255));
    lblClusterSepDist.setText("Loci Separation Distance:");

    lblMinPaired.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMinPaired.setForeground(new java.awt.Color(255, 255, 255));
    lblMinPaired.setText("Min Paired Bases:");

    lblMaxOverlapPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMaxOverlapPct.setForeground(new java.awt.Color(255, 255, 255));
    lblMaxOverlapPct.setText("Maximum Overlap %:");

    txtSeqExtension.setToolTipText("The starting value for how much each window should extend past the sRNA read (windows will be generated +- this amount)");
    txtSeqExtension.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMFEThreshold.setToolTipText("Minimum free energy for the hairpin");
    txtMFEThreshold.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMaxUnpairedPct.setToolTipText("Maximum number of unpaired bases in the hairpin");
    txtMaxUnpairedPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMaxHits.setToolTipText("Maximum number of hits to the Genome");
    txtMaxHits.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMaxGaps.setToolTipText("Maximum number of consectutive gaps in the miRNA");
    txtMaxGaps.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    txtMaxGaps.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        txtMaxGapsActionPerformed(evt);
      }
    });

    txtMinSRNALength.setToolTipText("Minimum length of sRNA sequence");
    txtMinSRNALength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    txtMinSRNALength.setVerifyInputWhenFocusTarget(false);

    txtMaxSRNALength.setToolTipText("Maximum length of sRNA sequence");
    txtMaxSRNALength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinGCPct.setToolTipText("Minimum GC percentage of sRNA");
    txtMinGCPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinSRNALocusSize.setToolTipText("Minimum size of miRNA producing loci");
    txtMinSRNALocusSize.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinOrientationPct.setToolTipText("Minimum percentage of strand orientation in loci");
    txtMinOrientationPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    txtMinOrientationPct.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        txtMinOrientationPctActionPerformed(evt);
      }
    });

    txtMinHairpinLen.setToolTipText("Minimum length of hairpin");
    txtMinHairpinLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinSRNAAbundance.setToolTipText("Minimum abundance of sRNA to be considered");
    txtMinSRNAAbundance.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinClusterSepDist.setToolTipText("Maximum distance between miRNA and miRNA*");
    txtMinClusterSepDist.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMinPaired.setToolTipText("Minimum number of paired bases in miRNA");
    txtMinPaired.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txtMaxOverlapPct.setToolTipText("Maximum percentage of overlapping sequences");
    txtMaxOverlapPct.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    chkAllowComplexLoops.setBackground(new java.awt.Color(120, 120, 120));
    chkAllowComplexLoops.setForeground(new java.awt.Color(255, 255, 255));
    chkAllowComplexLoops.setSelected(true);
    chkAllowComplexLoops.setText("Allow Complex Loops");
    chkAllowComplexLoops.setToolTipText("");

    lblP_Val.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblP_Val.setForeground(new java.awt.Color(255, 255, 255));
    lblP_Val.setText("P_Val:");

    txt_P_Val.setToolTipText("Maximum percentage of overlapping sequences");
    txt_P_Val.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    txt_ThreadCount.setToolTipText("Maximum percentage of overlapping sequences");
    txt_ThreadCount.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    lblThreadCount.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblThreadCount.setForeground(new java.awt.Color(255, 255, 255));
    lblThreadCount.setText("Thread Count: ");

    javax.swing.GroupLayout paramsPanelLayout = new javax.swing.GroupLayout(paramsPanel);
    paramsPanel.setLayout(paramsPanelLayout);
    paramsPanelLayout.setHorizontalGroup(
      paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paramsPanelLayout.createSequentialGroup()
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
            .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSRNASeqLength))
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSeqExtension))
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(txtMFEThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMFEThreshold)))
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(txtSeqExtension, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addComponent(lblMinSRNALength)
                .addGap(18, 18, 18)
                .addComponent(txtMinSRNALength, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblMaxSRNALength)
                .addGap(18, 18, 18)
                .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(txtMaxHits)
                  .addGroup(paramsPanelLayout.createSequentialGroup()
                    .addComponent(txtMaxSRNALength, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                  .addComponent(txtMaxUnpairedPct)
                  .addComponent(txtMaxGaps)
                  .addComponent(txtMinGCPct)
                  .addComponent(txtMinSRNALocusSize)
                  .addComponent(txtMinOrientationPct)
                  .addComponent(txtMinHairpinLen)
                  .addComponent(txtMinSRNAAbundance)
                  .addComponent(txtMinClusterSepDist, javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(txtMinPaired)
                  .addComponent(txtMaxOverlapPct)))
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, paramsPanelLayout.createSequentialGroup()
                .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(lblMaxHits)
                  .addComponent(lblMaxUnpairedPct)
                  .addComponent(lblMaxGaps)
                  .addComponent(lblMinGCPct)
                  .addComponent(lblMinSRNALocusSize)
                  .addComponent(lblMinOrientationPct)
                  .addComponent(lblMinHairpinLen)
                  .addComponent(lblClusterSepDist)
                  .addComponent(lblMinPaired)
                  .addComponent(lblMaxOverlapPct)
                  .addComponent(lblP_Val)
                  .addComponent(lblThreadCount))
                .addGap(18, 18, 18)
                .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(txt_ThreadCount, javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(txt_P_Val)))
              .addComponent(lblMinSRNAAbundance, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))))
        .addGap(6, 6, 6))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paramsPanelLayout.createSequentialGroup()
        .addComponent(chkAllowComplexLoops, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
    );
    paramsPanelLayout.setVerticalGroup(
      paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(paramsPanelLayout.createSequentialGroup()
        .addComponent(lblSeqExtension)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(txtSeqExtension, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblMFEThreshold)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(txtMFEThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblSRNASeqLength)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMaxSRNALength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMaxSRNALength)
          .addComponent(txtMinSRNALength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinSRNALength))
        .addGap(18, 18, 18)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMaxHits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMaxHits))
        .addGap(8, 8, 8)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMaxUnpairedPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMaxUnpairedPct, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMaxGaps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMaxGaps))
        .addGap(8, 8, 8)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinGCPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinGCPct, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinSRNALocusSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinSRNALocusSize, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinOrientationPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinOrientationPct))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinHairpinLen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinHairpinLen))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinSRNAAbundance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinSRNAAbundance))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinClusterSepDist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblClusterSepDist))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMinPaired, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMinPaired))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txtMaxOverlapPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblMaxOverlapPct, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txt_P_Val, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblP_Val, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(txt_ThreadCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblThreadCount, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(chkAllowComplexLoops)
        .addContainerGap())
    );

    mirCatLogPanel.setBackground(new java.awt.Color(153, 153, 153));
    mirCatLogPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "miRCat Log", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(153, 204, 255))); // NOI18N
    mirCatLogPanel.setAutoscrolls(true);

    tabLog.setBackground(new java.awt.Color(120, 120, 120));
    tabLog.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

    mirCatLogPane.setBackground(new java.awt.Color(120, 120, 120));
    mirCatLogPane.setForeground(new java.awt.Color(255, 255, 255));
    jScrollPane1.setViewportView(mirCatLogPane);

    tabLog.addTab("miRCAT Log", jScrollPane1);

    outputLogFile.setBackground(new java.awt.Color(120, 120, 120));
    outputLogFile.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    outputLogFile.setForeground(new java.awt.Color(255, 255, 255));
    outputLogFile.setText("Output Full Log to File");
    outputLogFile.setToolTipText("Check this box to select a file for full miRCat log");
    outputLogFile.setEnabled(false);
    outputLogFile.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        outputLogFileActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout mirCatLogPanelLayout = new javax.swing.GroupLayout(mirCatLogPanel);
    mirCatLogPanel.setLayout(mirCatLogPanelLayout);
    mirCatLogPanelLayout.setHorizontalGroup(
      mirCatLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(tabLog, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
      .addGroup(mirCatLogPanelLayout.createSequentialGroup()
        .addGap(7, 7, 7)
        .addComponent(outputLogFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(6, 6, 6))
    );
    mirCatLogPanelLayout.setVerticalGroup(
      mirCatLogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(mirCatLogPanelLayout.createSequentialGroup()
        .addComponent(outputLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(tabLog, javax.swing.GroupLayout.PREFERRED_SIZE, 436, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    showMainParams.setBackground(new java.awt.Color(120, 120, 120));
    showMainParams.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    showMainParams.setForeground(new java.awt.Color(255, 255, 255));
    showMainParams.setSelected(true);
    showMainParams.setText("Show Main Parameters");
    showMainParams.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        showMainParamsActionPerformed(evt);
      }
    });

    defaultParamsPane.setBackground(new java.awt.Color(120, 120, 120));
    defaultParamsPane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
    defaultParamsPane.setToolTipText("Default Parameters");

    defaultPlantParams.setBackground(new java.awt.Color(120, 120, 120));
    defaultPlantParams.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    defaultPlantParams.setText("Set Default Plant Parameters");
    defaultPlantParams.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        defaultPlantParamsActionPerformed(evt);
      }
    });

    defaultAnimalParams.setBackground(new java.awt.Color(120, 120, 120));
    defaultAnimalParams.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    defaultAnimalParams.setText("Set Default Animal Parameters");
    defaultAnimalParams.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        defaultAnimalParamsActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout defaultParamsPaneLayout = new javax.swing.GroupLayout(defaultParamsPane);
    defaultParamsPane.setLayout(defaultParamsPaneLayout);
    defaultParamsPaneLayout.setHorizontalGroup(
      defaultParamsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(defaultParamsPaneLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(defaultParamsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(defaultPlantParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(defaultAnimalParams, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    defaultParamsPaneLayout.setVerticalGroup(
      defaultParamsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(defaultParamsPaneLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(defaultAnimalParams, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(defaultPlantParams, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel1.setBackground(new java.awt.Color(120, 120, 120));
    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Progress", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(153, 204, 255))); // NOI18N

    mirCatProgressBar.setStringPainted(true);

    mirCatStatusLabel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    mirCatStatusLabel.setForeground(new java.awt.Color(255, 255, 255));
    mirCatStatusLabel.setText("Status:");

    execQueueBar.setOrientation(SwingConstants.VERTICAL);

    compTasksBar.setOrientation(SwingConstants.VERTICAL);

    compTaskLbl.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    compTaskLbl.setForeground(new java.awt.Color(255, 255, 255));
    compTaskLbl.setText("Completed Tasks");

    exeQueueLbl.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    exeQueueLbl.setForeground(new java.awt.Color(255, 255, 255));
    exeQueueLbl.setText("Execution Stack");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(mirCatProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(mirCatStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(execQueueBar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(compTasksBar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(exeQueueLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(54, 54, 54)
                .addComponent(compTaskLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addComponent(mirCatStatusLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(compTasksBar, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
          .addComponent(execQueueBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(compTaskLbl)
          .addComponent(exeQueueLbl))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(mirCatProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    pnlMirbase.setBackground(new java.awt.Color(120, 120, 120));
    pnlMirbase.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "miRBase", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

    lblMirBaseVersionDetails.setBackground(new java.awt.Color(120, 120, 120));
    lblMirBaseVersionDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblMirBaseVersionDetails.setForeground(new java.awt.Color(255, 255, 255));
    lblMirBaseVersionDetails.setText("(latest installed version: 18)");

    miRBaseLabel.setBackground(new java.awt.Color(120, 120, 120));
    miRBaseLabel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    miRBaseLabel.setForeground(new java.awt.Color(255, 255, 255));
    miRBaseLabel.setText("miRBase version:");

    txt_miRBaseVersion.setText("18");
    txt_miRBaseVersion.setToolTipText("<html>\nThe miRBase version to compare against the sRNA sequences.<br/>\nSimply type the version you wish to comapre against into this box, if the mirBase version is not currently installed it will automatically be downloaded and installed. <br/>\nTo find the latest version goto http://www.mirbase.org/.  Alternatively, the latest version can be automatically downloaded and installed from the Workbench -> Help menu.\n</html>");
    txt_miRBaseVersion.setEnabled(false);
    txt_miRBaseVersion.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

    javax.swing.GroupLayout pnlMirbaseLayout = new javax.swing.GroupLayout(pnlMirbase);
    pnlMirbase.setLayout(pnlMirbaseLayout);
    pnlMirbaseLayout.setHorizontalGroup(
      pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlMirbaseLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(pnlMirbaseLayout.createSequentialGroup()
            .addComponent(miRBaseLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(txt_miRBaseVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(lblMirBaseVersionDetails))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    pnlMirbaseLayout.setVerticalGroup(
      pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlMirbaseLayout.createSequentialGroup()
        .addGroup(pnlMirbaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(miRBaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(txt_miRBaseVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblMirBaseVersionDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(paramsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
      .addComponent(defaultParamsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(showMainParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(tbParams, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(mirCatLogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(pnlMirbase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(tbParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(showMainParams)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(defaultParamsPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(paramsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(pnlMirbase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(mirCatLogPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(1, 1, 1)
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents


 
  public String getMirbaseVersion()
  {
    return this.txt_miRBaseVersion.getText().trim();
  }

  public void beginRun()
  {
    this.showMainParams.setSelected( false );
      paramsPanel.setVisible( false );
  }
  
  
  public boolean createParams()
  {
    try
    {
      String str_extend = this.txtSeqExtension.getText().trim();
      String str_mfe = this.txtMFEThreshold.getText().trim();
      String str_min_paired = this.txtMinPaired.getText().trim();
      String str_max_gaps = this.txtMaxGaps.getText().trim();
      String str_max_hits = this.txtMaxHits.getText().trim();
      String str_min_len = this.txtMinSRNALength.getText().trim();
      String str_max_len = this.txtMaxSRNALength.getText().trim();
      String str_min_gc = this.txtMinGCPct.getText().trim();
      String str_max_unpaired = this.txtMaxUnpairedPct.getText().trim();
      String str_max_overlap = this.txtMaxOverlapPct.getText().trim();
      String str_min_loci_size = this.txtMinSRNALocusSize.getText().trim();
      String str_orientation = this.txtMinOrientationPct.getText().trim();
      String str_hairpin_len = this.txtMinHairpinLen.getText().trim();
      String str_min_abd = this.txtMinSRNAAbundance.getText().trim();
      String str_min_loci_sep = this.txtMinClusterSepDist.getText().trim();
      String str_p_val = this.txt_P_Val.getText().trim();
      String str_thread_count = this.txt_ThreadCount.getText().trim();
      
      float extend = StringUtils.safeFloatParse( str_extend, MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getDefault( Float.class ) );
      float mfe = StringUtils.safeFloatParse( str_mfe, MiRCatParams.Definition.MFE_THRESHOLD.getDefault( Float.class ) );
      int min_paired = StringUtils.safeIntegerParse( str_min_paired, MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getDefault( Integer.class ) );
      int max_gaps = StringUtils.safeIntegerParse( str_max_gaps, MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getDefault( Integer.class ) );
      int max_hits = StringUtils.safeIntegerParse( str_max_hits, MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getDefault( Integer.class ) );
      int min_len = StringUtils.safeIntegerParse( str_min_len, MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getDefault( Integer.class ) );
      int max_len = StringUtils.safeIntegerParse( str_max_len, MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getDefault( Integer.class ) );
      int min_gc = StringUtils.safeIntegerParse( str_min_gc, MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getDefault( Integer.class ) );
      int max_unpaired = StringUtils.safeIntegerParse( str_max_unpaired, MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefault( Integer.class ) );
      int max_overlap = StringUtils.safeIntegerParse( str_max_overlap, MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefault( Integer.class ) );
      int min_loci_sep = StringUtils.safeIntegerParse( str_min_loci_sep, MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefault( Integer.class ) );
      int orientation = StringUtils.safeIntegerParse( str_orientation, MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefault( Integer.class ) );
      int hairpin_len = StringUtils.safeIntegerParse( str_hairpin_len, MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getDefault( Integer.class ) );
      int min_abd = StringUtils.safeIntegerParse( str_min_abd, MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefault( Integer.class ) );
      int min_loci_size = StringUtils.safeIntegerParse( str_min_loci_size, MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getDefault( Integer.class ) );
      float p_val = StringUtils.safeFloatParse( str_p_val , MiRCatParams.Definition.P_VAL_THRESHOLD.getDefault( Float.class) );
      int threadCount = StringUtils.safeIntegerParse( str_thread_count, MiRCatParams.Definition.THREAD_COUNT.getDefault( Integer.class ) );
      
      MiRCatParams newParams = new MiRCatParams.Builder()
        .setExtend( extend )
        .setMinEnergy( mfe )
        .setMinPaired( min_paired )
        .setMaxGaps( max_gaps )
        .setMaxGenomeHits( max_hits )
        .setMinLength( min_len ).setMaxLength( max_len )
        .setMinGC( min_gc )
        .setMaxUnpaired( max_unpaired )
        .setMaxOverlapPercentage( max_overlap )
        .setClusterSentinel( min_loci_sep )
        .setOrientation( orientation )
        .setMinHairpinLength( hairpin_len )
        .setMinConsider( min_abd )
        .setMinLocusSize( min_loci_size )
        .setComplexLoops( chkAllowComplexLoops.isSelected())
        .setPVal( p_val)
        .setThreadCount( threadCount )
        .build();
        
      this.params = newParams;
    }
    catch ( IllegalArgumentException e )
    {
      LOGGER.log( Level.WARNING, "failed params: {0}", e);
      return false;
    }

    return true;
  }
    private void defaultAnimalParamsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defaultAnimalParamsActionPerformed
    {//GEN-HEADEREND:event_defaultAnimalParamsActionPerformed
      update(MiRCatParams.createDefaultAnimalParams());
    }//GEN-LAST:event_defaultAnimalParamsActionPerformed

    private void defaultPlantParamsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defaultPlantParamsActionPerformed
    {//GEN-HEADEREND:event_defaultPlantParamsActionPerformed
      update(MiRCatParams.createDefaultPlantParams());
    }//GEN-LAST:event_defaultPlantParamsActionPerformed

    private void outputLogFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_outputLogFileActionPerformed
    {//GEN-HEADEREND:event_outputLogFileActionPerformed
      setupLogFile();

    }//GEN-LAST:event_outputLogFileActionPerformed

    private void showMainParamsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showMainParamsActionPerformed
    {//GEN-HEADEREND:event_showMainParamsActionPerformed

      this.paramsPanel.setVisible( showMainParams.isSelected() );

    }//GEN-LAST:event_showMainParamsActionPerformed

  private void txtMinOrientationPctActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_txtMinOrientationPctActionPerformed
  {//GEN-HEADEREND:event_txtMinOrientationPctActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_txtMinOrientationPctActionPerformed

  private void txtMaxGapsActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_txtMaxGapsActionPerformed
  {//GEN-HEADEREND:event_txtMaxGapsActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_txtMaxGapsActionPerformed
  private void setupLogFile()
  {

    MIRCAT_LOGGER.log( Level.INFO, "Logging begun..." );
    parentFrame.getEngine().changeLoggingMiRCat( outputLogFile.isSelected() );

  }

  public void enableRun()
  {
    
    this.outputLogFile.setEnabled( true );
    
  }
  
 
  
  public StatusTracker getTracker()
  {
    return this.tracker;
  }

  // ***** ToolParamsHost implementation *****
  @Override
  public MiRCatParams getParams()
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
    if ( !( params instanceof MiRCatParams ) )
    {
      showErrorDialog( "Unexpected error: parameters object provided is not a valid mircat params file." );
      return;
    }

    MiRCatParams mp = (MiRCatParams) params;
    
    txtSeqExtension.setText( Float.toString( mp.getExtend() ) );
    txtMFEThreshold.setText( Float.toString( mp.getMinEnergy() ) );
    txtMinPaired.setText( Integer.toString( mp.getMinPaired() ) );
    txtMaxGaps.setText( Integer.toString( mp.getMaxGaps() ) );
    txtMaxHits.setText( Integer.toString( mp.getMaxGenomeHits() ) );
    txtMinSRNALength.setText( Integer.toString( mp.getMinLength() ) );
    txtMaxSRNALength.setText( Integer.toString( mp.getMaxLength() ) );
    txtMinGCPct.setText( Integer.toString( mp.getMinGC() ) );
    txtMaxUnpairedPct.setText( Integer.toString( mp.getMaxUnpaired() ) );
    txtMaxOverlapPct.setText( Integer.toString( mp.getMaxOverlapPercentage() ) );
    txtMinSRNALocusSize.setText( Integer.toString( mp.getMinLocusSize() ) );
    txtMinOrientationPct.setText( Integer.toString( mp.getOrientation() ) );
    txtMinHairpinLen.setText( Integer.toString( mp.getMinHairpinLength() ) );
    txtMinSRNAAbundance.setText( Integer.toString( mp.getMinConsider() ) );
    txtMinClusterSepDist.setText( Integer.toString( mp.getClusterSentinel() ) );
    chkAllowComplexLoops.setSelected( mp.getComplexLoops() );
    txt_P_Val.setText( Float.toString( mp.getPVal()));
    txt_ThreadCount.setText( Integer.toString( mp.getThreadCount()) );
  }

  @Override
  public void showErrorDialog( String message )
  {
    JOptionPane.showMessageDialog( this,
      message,
      "miRCat Configuration error",
      JOptionPane.ERROR_MESSAGE );
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox chkAllowComplexLoops;
  private javax.swing.JLabel compTaskLbl;
  private javax.swing.JProgressBar compTasksBar;
  private javax.swing.JButton defaultAnimalParams;
  private javax.swing.JPanel defaultParamsPane;
  private javax.swing.JButton defaultPlantParams;
  private javax.swing.JLabel exeQueueLbl;
  private javax.swing.JProgressBar execQueueBar;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel lblClusterSepDist;
  private javax.swing.JLabel lblMFEThreshold;
  private javax.swing.JLabel lblMaxGaps;
  private javax.swing.JLabel lblMaxHits;
  private javax.swing.JLabel lblMaxOverlapPct;
  private javax.swing.JLabel lblMaxSRNALength;
  private javax.swing.JLabel lblMaxUnpairedPct;
  private javax.swing.JLabel lblMinGCPct;
  private javax.swing.JLabel lblMinHairpinLen;
  private javax.swing.JLabel lblMinOrientationPct;
  private javax.swing.JLabel lblMinPaired;
  private javax.swing.JLabel lblMinSRNAAbundance;
  private javax.swing.JLabel lblMinSRNALength;
  private javax.swing.JLabel lblMinSRNALocusSize;
  private javax.swing.JLabel lblMirBaseVersionDetails;
  private javax.swing.JLabel lblP_Val;
  private javax.swing.JLabel lblSRNASeqLength;
  private javax.swing.JLabel lblSeqExtension;
  private javax.swing.JLabel lblThreadCount;
  private javax.swing.JLabel miRBaseLabel;
  private javax.swing.JTextPane mirCatLogPane;
  private javax.swing.JPanel mirCatLogPanel;
  private javax.swing.JProgressBar mirCatProgressBar;
  private javax.swing.JLabel mirCatStatusLabel;
  private javax.swing.JCheckBox outputLogFile;
  private javax.swing.JPanel paramsPanel;
  private uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar paramsToolBar1;
  private javax.swing.JPanel pnlMirbase;
  private javax.swing.JCheckBox showMainParams;
  private javax.swing.JTabbedPane tabLog;
  private uk.ac.uea.cmp.srnaworkbench.swing.ParamsToolBar tbParams;
  private javax.swing.JFormattedTextField txtMFEThreshold;
  private javax.swing.JFormattedTextField txtMaxGaps;
  private javax.swing.JFormattedTextField txtMaxHits;
  private javax.swing.JFormattedTextField txtMaxOverlapPct;
  private javax.swing.JFormattedTextField txtMaxSRNALength;
  private javax.swing.JFormattedTextField txtMaxUnpairedPct;
  private javax.swing.JFormattedTextField txtMinClusterSepDist;
  private javax.swing.JFormattedTextField txtMinGCPct;
  private javax.swing.JFormattedTextField txtMinHairpinLen;
  private javax.swing.JFormattedTextField txtMinOrientationPct;
  private javax.swing.JFormattedTextField txtMinPaired;
  private javax.swing.JFormattedTextField txtMinSRNAAbundance;
  private javax.swing.JFormattedTextField txtMinSRNALength;
  private javax.swing.JFormattedTextField txtMinSRNALocusSize;
  private javax.swing.JFormattedTextField txtSeqExtension;
  private javax.swing.JFormattedTextField txt_P_Val;
  private javax.swing.JFormattedTextField txt_ThreadCount;
  private javax.swing.JFormattedTextField txt_miRBaseVersion;
  // End of variables declaration//GEN-END:variables
}
