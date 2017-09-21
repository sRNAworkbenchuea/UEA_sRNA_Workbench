/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.awt.Component;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParamsHost;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.CoLIDEParams.CONFIDENCE_INTERVAL;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.CoLIDEParams.SERIES_TYPE_ENUM;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class CoLIDEParamBrowser extends javax.swing.JPanel implements ToolParamsHost
{
  private CoLIDEParams myParams = new CoLIDEParams();
  private int numberOfSamples = 0;
  /**
   * Creates new form coLIDEParamBrowser
   */
  public CoLIDEParamBrowser()
  {
    initComponents();
    this.percentageCI_FTB.setText( Double.toString( this.myParams.getPercentageCIValue() ));
    this.percentageOverlapFTB.setText( Double.toString( this.myParams.getPercentageOverlapValue() ));
    this.offsetChi_Sq_FTB.setText( Double.toString( this.myParams.getOffsetChiSq()) );
//    this.sizeClass1.setText( Double.toString( this.myParams.getSizeValue1() ) );
//    this.sizeClass2.setText( Double.toString( this.myParams.getSizeValue2() ) );
//    this.sizeClass3.setText( Double.toString( this.myParams.getSizeValue3() ) );
//    this.sizeClass4.setText( Double.toString( this.myParams.getSizeValue4() ) );
    checkConfEnables();
  }
  public boolean createParams()
  {
    try
    {
      String str_perc = this.percentageCI_FTB.getText().trim();

      double percentage_CI = StringUtils.safeDoubleParse( str_perc, CoLIDEParams.Definition.PERCENTAGE_CI_VALUE.getDefault( Double.class ) );

      String str_perc_over = this.percentageOverlapFTB.getText().trim();

      double percentage_overlap = StringUtils.safeDoubleParse( str_perc_over, CoLIDEParams.Definition.OVERLAP_PERCENTAGE.getDefault( Double.class ) );
      
      String str_offset_chi = this.offsetChi_Sq_FTB.getText().trim();

      double offset_chi = StringUtils.safeDoubleParse( str_offset_chi, CoLIDEParams.Definition.OFFSET_CHI_SQ.getDefault( Double.class ) );


      SERIES_TYPE_ENUM series = SERIES_TYPE_ENUM.ORDERED;//default option
      String selectedButtonText = getSelectedButton(this.seriesGroup);

      if(selectedButtonText.equals( "Unordered Series") )
      {
        series = SERIES_TYPE_ENUM.UNORDERED;
      } 
      CONFIDENCE_INTERVAL confInter = CONFIDENCE_INTERVAL.PERCENTAGE_CI;
      

      boolean replicate = false;
      
      selectedButtonText = getSelectedButton(this.replicateGroup);

      if(selectedButtonText.equals( "Replicate") )
      {
        selectedButtonText = getSelectedButton(this.CI_group);
        replicate = true;
        confInter = CONFIDENCE_INTERVAL.MIN_MAX;
        if(selectedButtonText.equals( "Mean/SD") )
        {
          int index = this.meanSDOptions.getSelectedIndex();
          switch(index)
          {
            case 0:
              confInter = CONFIDENCE_INTERVAL.P_M_1_SD;
              break;
            case 1:
              confInter = CONFIDENCE_INTERVAL.P_M_R_2_DEV2_X_SD;
              break;
            case 2:
              confInter = CONFIDENCE_INTERVAL.P_M_2_SD;
              break;
            default:
              showErrorDialog("Missing confidence interval type selection");
              return false;

          }
        } 
      }
//      
//      String str_size_class1 = this.sizeClass1.getText().trim();
//      String str_size_class2 = this.sizeClass2.getText().trim();
//      String str_size_class3 = this.sizeClass3.getText().trim();
//      String str_size_class4 = this.sizeClass4.getText().trim();
//
//      int sizeClass1_Val = StringUtils.safeIntegerParse( str_size_class1, CoLIDEParams.Definition.SIZE_VALUE_1.getDefault( Integer.class ) );
//      int sizeClass2_Val = StringUtils.safeIntegerParse( str_size_class2, CoLIDEParams.Definition.SIZE_VALUE_2.getDefault( Integer.class ) );
//      int sizeClass3_Val = StringUtils.safeIntegerParse( str_size_class3, CoLIDEParams.Definition.SIZE_VALUE_3.getDefault( Integer.class ) );
//      int sizeClass4_Val = StringUtils.safeIntegerParse( str_size_class4, CoLIDEParams.Definition.SIZE_VALUE_4.getDefault( Integer.class ) );
//
//      
//      
      CoLIDEParams newParams = new CoLIDEParams.Builder()
        .setSeriesType( series )
        .setReplicate( replicate )
        .setConfInter( confInter )
        .setPercentageCIValue( percentage_CI )
        .setOverlapPercentage( percentage_overlap )
        .setOffsetChiSq( offset_chi )
//        .setSizeValue1( sizeClass1_Val )
//        .setSizeValue2( sizeClass2_Val )
//        .setSizeValue3( sizeClass3_Val )
//        .setSizeValue4( sizeClass4_Val )
        .build();
        
      this.myParams = newParams;
    }
    catch ( IllegalArgumentException e )
    {
      LOGGER.log( Level.WARNING, "failed params: {0}", e);
      this.myParams = null;
      return false;
    }

    return true;
  }
  public void incrementSampleNumber()
  {
    numberOfSamples++;
    this.sampleNumberLbl.setText( "Number of samples with files: " + numberOfSamples);
    
  }
  public String getSelectedButton( ButtonGroup group )
  {
    Enumeration<AbstractButton> e = group.getElements();
    while ( e.hasMoreElements() )
    {
      AbstractButton b = e.nextElement();
      if ( b.isSelected() )
      {
        return b.getText();
      }
    }
    return null;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        seriesGroup = new javax.swing.ButtonGroup();
        replicateGroup = new javax.swing.ButtonGroup();
        CI_group = new javax.swing.ButtonGroup();
        paramsPanel = new javax.swing.JPanel();
        orderedRadio = new javax.swing.JRadioButton();
        unorderedRadio = new javax.swing.JRadioButton();
        replicateRadio = new javax.swing.JRadioButton();
        nonReplicateRadio = new javax.swing.JRadioButton();
        setupPnl = new javax.swing.JPanel();
        replicateSettingsPanel = new javax.swing.JPanel();
        meanSDRadio = new javax.swing.JRadioButton();
        minMaxRadio = new javax.swing.JRadioButton();
        meanSDOptions = new javax.swing.JComboBox();
        nonReplicateSettingsPanel = new javax.swing.JPanel();
        percentageCI_FTB = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        sampleNumberLbl = new javax.swing.JLabel();
        chiSqSettingsPanel = new javax.swing.JPanel();
        offsetChi_Sq_FTB = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        percentageOverlapFTB = new javax.swing.JFormattedTextField();

        setBackground(new java.awt.Color(120, 120, 120));

        paramsPanel.setBackground(new java.awt.Color(120, 120, 120));
        paramsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.paramsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        orderedRadio.setBackground(new java.awt.Color(120, 120, 120));
        seriesGroup.add(orderedRadio);
        orderedRadio.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        orderedRadio.setForeground(new java.awt.Color(255, 255, 255));
        orderedRadio.setSelected(true);
        orderedRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.orderedRadio.text")); // NOI18N
        orderedRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderedRadioActionPerformed(evt);
            }
        });

        unorderedRadio.setBackground(new java.awt.Color(120, 120, 120));
        seriesGroup.add(unorderedRadio);
        unorderedRadio.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        unorderedRadio.setForeground(new java.awt.Color(255, 255, 255));
        unorderedRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.unorderedRadio.text")); // NOI18N

        replicateRadio.setBackground(new java.awt.Color(120, 120, 120));
        replicateGroup.add(replicateRadio);
        replicateRadio.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        replicateRadio.setForeground(new java.awt.Color(255, 255, 255));
        replicateRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.replicateRadio.text")); // NOI18N
        replicateRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replicateRadioActionPerformed(evt);
            }
        });

        nonReplicateRadio.setBackground(new java.awt.Color(120, 120, 120));
        replicateGroup.add(nonReplicateRadio);
        nonReplicateRadio.setForeground(new java.awt.Color(255, 255, 255));
        nonReplicateRadio.setSelected(true);
        nonReplicateRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.nonReplicateRadio.text")); // NOI18N
        nonReplicateRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nonReplicateRadioActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout paramsPanelLayout = new org.jdesktop.layout.GroupLayout(paramsPanel);
        paramsPanel.setLayout(paramsPanelLayout);
        paramsPanelLayout.setHorizontalGroup(
            paramsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(paramsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(paramsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, replicateRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, orderedRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, unorderedRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, nonReplicateRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        paramsPanelLayout.setVerticalGroup(
            paramsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(paramsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(orderedRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(unorderedRadio)
                .add(18, 18, 18)
                .add(replicateRadio)
                .add(12, 12, 12)
                .add(nonReplicateRadio)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setupPnl.setBackground(new java.awt.Color(120, 120, 120));
        setupPnl.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.setupPnl.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        replicateSettingsPanel.setBackground(new java.awt.Color(120, 120, 120));
        replicateSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.replicateSettingsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        meanSDRadio.setBackground(new java.awt.Color(120, 120, 120));
        CI_group.add(meanSDRadio);
        meanSDRadio.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        meanSDRadio.setForeground(new java.awt.Color(255, 255, 255));
        meanSDRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.meanSDRadio.text")); // NOI18N

        minMaxRadio.setBackground(new java.awt.Color(120, 120, 120));
        CI_group.add(minMaxRadio);
        minMaxRadio.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        minMaxRadio.setForeground(new java.awt.Color(255, 255, 255));
        minMaxRadio.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.minMaxRadio.text")); // NOI18N

        meanSDOptions.setBackground(new java.awt.Color(102, 102, 102));
        meanSDOptions.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        meanSDOptions.setForeground(new java.awt.Color(51, 51, 51));
        meanSDOptions.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+-SD", "+-r(2)SD", "+-2SD" }));

        org.jdesktop.layout.GroupLayout replicateSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(replicateSettingsPanel);
        replicateSettingsPanel.setLayout(replicateSettingsPanelLayout);
        replicateSettingsPanelLayout.setHorizontalGroup(
            replicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, replicateSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(replicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(meanSDRadio)
                    .add(minMaxRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(meanSDOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        replicateSettingsPanelLayout.setVerticalGroup(
            replicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(replicateSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(minMaxRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(replicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(meanSDRadio)
                    .add(meanSDOptions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nonReplicateSettingsPanel.setBackground(new java.awt.Color(120, 120, 120));
        nonReplicateSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.nonReplicateSettingsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        percentageCI_FTB.setBackground(new java.awt.Color(102, 102, 102));
        percentageCI_FTB.setForeground(new java.awt.Color(255, 255, 255));
        percentageCI_FTB.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.percentageCI_FTB.text")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout nonReplicateSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(nonReplicateSettingsPanel);
        nonReplicateSettingsPanel.setLayout(nonReplicateSettingsPanelLayout);
        nonReplicateSettingsPanelLayout.setHorizontalGroup(
            nonReplicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, nonReplicateSettingsPanelLayout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(37, 37, 37)
                .add(percentageCI_FTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        nonReplicateSettingsPanelLayout.setVerticalGroup(
            nonReplicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(nonReplicateSettingsPanelLayout.createSequentialGroup()
                .add(nonReplicateSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(percentageCI_FTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 12, Short.MAX_VALUE))
        );

        sampleNumberLbl.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        sampleNumberLbl.setForeground(new java.awt.Color(255, 255, 255));
        sampleNumberLbl.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.sampleNumberLbl.text")); // NOI18N

        chiSqSettingsPanel.setBackground(new java.awt.Color(120, 120, 120));
        chiSqSettingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.chiSqSettingsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        offsetChi_Sq_FTB.setBackground(new java.awt.Color(102, 102, 102));
        offsetChi_Sq_FTB.setForeground(new java.awt.Color(255, 255, 255));
        offsetChi_Sq_FTB.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.offsetChi_Sq_FTB.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.jLabel3.text")); // NOI18N

        org.jdesktop.layout.GroupLayout chiSqSettingsPanelLayout = new org.jdesktop.layout.GroupLayout(chiSqSettingsPanel);
        chiSqSettingsPanel.setLayout(chiSqSettingsPanelLayout);
        chiSqSettingsPanelLayout.setHorizontalGroup(
            chiSqSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chiSqSettingsPanelLayout.createSequentialGroup()
                .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(37, 37, 37)
                .add(offsetChi_Sq_FTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        chiSqSettingsPanelLayout.setVerticalGroup(
            chiSqSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chiSqSettingsPanelLayout.createSequentialGroup()
                .add(chiSqSettingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(offsetChi_Sq_FTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 12, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout setupPnlLayout = new org.jdesktop.layout.GroupLayout(setupPnl);
        setupPnl.setLayout(setupPnlLayout);
        setupPnlLayout.setHorizontalGroup(
            setupPnlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(replicateSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, nonReplicateSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, sampleNumberLbl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, setupPnlLayout.createSequentialGroup()
                .add(chiSqSettingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        setupPnlLayout.setVerticalGroup(
            setupPnlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(setupPnlLayout.createSequentialGroup()
                .add(nonReplicateSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(replicateSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chiSqSettingsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 23, Short.MAX_VALUE)
                .add(sampleNumberLbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 13)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.jLabel2.text")); // NOI18N

        percentageOverlapFTB.setBackground(new java.awt.Color(102, 102, 102));
        percentageOverlapFTB.setForeground(new java.awt.Color(255, 255, 255));
        percentageOverlapFTB.setText(org.openide.util.NbBundle.getMessage(CoLIDEParamBrowser.class, "CoLIDEParamBrowser.percentageOverlapFTB.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(32, 32, 32)
                .add(percentageOverlapFTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(percentageOverlapFTB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 12, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(paramsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(setupPnl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(paramsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(setupPnl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

  private void orderedRadioActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_orderedRadioActionPerformed
  {//GEN-HEADEREND:event_orderedRadioActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_orderedRadioActionPerformed

  private void nonReplicateRadioActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_nonReplicateRadioActionPerformed
  {//GEN-HEADEREND:event_nonReplicateRadioActionPerformed
    checkConfEnables();
  }//GEN-LAST:event_nonReplicateRadioActionPerformed

  private void replicateRadioActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_replicateRadioActionPerformed
  {//GEN-HEADEREND:event_replicateRadioActionPerformed
    checkConfEnables();
  }//GEN-LAST:event_replicateRadioActionPerformed

  private void checkConfEnables()
  {
    if(nonReplicateRadio.isSelected())
    {
      Component[] nonReplicateComps = this.nonReplicateSettingsPanel.getComponents();
      for(Component comp : nonReplicateComps)
      {
        comp.setEnabled( true );
      }
      
      Component[] replicateComps = this.replicateSettingsPanel.getComponents();
      for(Component comp : replicateComps)
      {
        comp.setEnabled( false );
      }
    }
    else
    {
      Component[] nonReplicateComps = this.nonReplicateSettingsPanel.getComponents();
      for(Component comp : nonReplicateComps)
      {
        comp.setEnabled( false );
      }
      
      Component[] replicateComps = this.replicateSettingsPanel.getComponents();
      for(Component comp : replicateComps)
      {
        comp.setEnabled( true );
      }
      minMaxRadio.setSelected( true );
    }
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup CI_group;
    private javax.swing.JPanel chiSqSettingsPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox meanSDOptions;
    private javax.swing.JRadioButton meanSDRadio;
    private javax.swing.JRadioButton minMaxRadio;
    private javax.swing.JRadioButton nonReplicateRadio;
    private javax.swing.JPanel nonReplicateSettingsPanel;
    private javax.swing.JFormattedTextField offsetChi_Sq_FTB;
    private javax.swing.JRadioButton orderedRadio;
    private javax.swing.JPanel paramsPanel;
    private javax.swing.JFormattedTextField percentageCI_FTB;
    private javax.swing.JFormattedTextField percentageOverlapFTB;
    private javax.swing.ButtonGroup replicateGroup;
    private javax.swing.JRadioButton replicateRadio;
    private javax.swing.JPanel replicateSettingsPanel;
    private javax.swing.JLabel sampleNumberLbl;
    private javax.swing.ButtonGroup seriesGroup;
    private javax.swing.JPanel setupPnl;
    private javax.swing.JRadioButton unorderedRadio;
    // End of variables declaration//GEN-END:variables

  @Override
  public CoLIDEParams getParams()
  {
    if(createParams())
      return myParams;
    else 
      return new CoLIDEParams();
  }

  @Override
  public void update( ToolParameters params )
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public void showErrorDialog( String message )
  {
    JOptionPane.showMessageDialog( this,
      message,
      "CoLIDE Configuration error",
      JOptionPane.ERROR_MESSAGE );
  }


}
