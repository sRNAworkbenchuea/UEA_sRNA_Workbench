/*
 * AdaptorRemoverInputPanel.java
 *
 * Created on 09-May-2011, 12:37:07
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.awt.Component;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParamsHost;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.ButtonModel;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultFormatterFactory;
import uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType;
import uk.ac.uea.cmp.srnaworkbench.swing.FileSelector;

import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter;
import uk.ac.uea.cmp.srnaworkbench.utils.RegexPatternFormatter;

import static uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils.FileExtFilter.*;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.Definition.*;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol;
import uk.ac.uea.cmp.srnaworkbench.utils.GroupButtonUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

/**
 * Provides the user with a GUI for inputting the parameters to the adaptor removal
 * tool
 * @author w0445959
 */
public final class AdaptorRemoverInputPanel extends javax.swing.JPanel implements ToolParamsHost
{
  private AdaptorRemoverParams ar_params;
  
  /**
   * Creates an AdaptorRemoverInputPanel with links to execution control and output
   * results table
   * @param go_control Execution controller (start and cancel buttons)
   * @param table Place to put results
   */
  public AdaptorRemoverInputPanel()
  {
    initComponents();

    ar_params = null;
    
    this.setEnabled( false );
    
   
    
    
    
    // Setup validator for adaptor sequence fields.
    Pattern actguFilter = Pattern.compile( "[actguACTGU]*" );
    RegexPatternFormatter actguRegexFormatter = new RegexPatternFormatter( actguFilter );
    actguRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory actguFactory = new DefaultFormatterFactory( actguRegexFormatter );
    txt5Prime.setFormatterFactory( actguFactory );
    txt3Prime.setFormatterFactory( actguFactory );

    // Setup validator for sequence length fields.
    Pattern numberFilter = Pattern.compile( "[0-9]*" );
    RegexPatternFormatter numberRegexFormatter = new RegexPatternFormatter( numberFilter );
    numberRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory numberFactory = new DefaultFormatterFactory( numberRegexFormatter );
    txtMinLen.setFormatterFactory( numberFactory );
    txtMaxLen.setFormatterFactory( numberFactory );
    txtMinLen.setText( Integer.toString( MINIMUM_LENGTH.getDefault( Integer.class )));
    txtMaxLen.setText( Integer.toString( MAXIMUM_LENGTH.getDefault( Integer.class )));
    lblMinLenDetails.setText( "(min. " + MINIMUM_LENGTH.getLowerLimit( Integer.class ) + ")");
    lblMaxLenDetails.setText( "(max. " + MAXIMUM_LENGTH.getUpperLimit( Integer.class ) + ")");
    txt3PrimeLength.setFormatterFactory( numberFactory );
    txt3PrimeLength.setText( Integer.toString( ADAPTOR_3_LENGTH_TO_USE.getDefault( Integer.class )));
    txt5PrimeLength.setText( Integer.toString( ADAPTOR_5_LENGTH_TO_USE.getDefault( Integer.class )));
    lbl3PrimeLengthDetails.setText( "(min. " + ADAPTOR_3_LENGTH_TO_USE.getLowerLimit( Integer.class ) + 
      ", max. " + ADAPTOR_3_LENGTH_TO_USE.getUpperLimit( Integer.class ) + ")");
    lbl5PrimeLengthDetails.setText( "(min. " + ADAPTOR_5_LENGTH_TO_USE.getLowerLimit( Integer.class ) + 
      ", max. " + ADAPTOR_5_LENGTH_TO_USE.getUpperLimit( Integer.class ) + ")");
        
    for ( AdaptorRemoverParams.Known3PrimeAdaptor known : AdaptorRemoverParams.Known3PrimeAdaptor.values() )
    {
      cmbPreDefined3Primes.addItem( known );
    }
    
    for ( AdaptorRemoverParams.Known5PrimeAdaptor known : AdaptorRemoverParams.Known5PrimeAdaptor.values() )
    {
      cmbPreDefined5Primes.addItem( known );
    }
    
    this.inputSelectorPanel.setFilters( 
      FileExtFilter.toFilterArray( FASTQ, FASTA, RAW_READS, FASTA ));
    
    inputSelectorPanel.setToolName("Adapter Removal");
    outputSelectorPanel.setToolName("Adapter Removal");
     fspDiscardedLogFile.setHistoryType( HistoryFileType.NONE);
    fspDiscardedLogFile.setToolName("Adapter Removal");

    int widths[] = {inputSelectorPanel.getLabelWidth(),
    outputSelectorPanel.getLabelWidth(),
    fspDiscardedLogFile.getLabelWidth()};

    int maxWidth = Integer.MIN_VALUE;
    for(int currentWidth : widths)
    {
      if(currentWidth > maxWidth)
        maxWidth = currentWidth;
    }
    inputSelectorPanel.setLabelWidth( maxWidth );
    outputSelectorPanel.setLabelWidth( maxWidth );
    fspDiscardedLogFile.setLabelWidth( maxWidth );
    
    inputSelectorPanel.setFileLineAmount( 20);

    this.invalidate();
    reset();
    setHD_OptionsEnabled(false);
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

        HD_optionsBtnGrp = new javax.swing.ButtonGroup();
        pnlFiltering = new javax.swing.JPanel();
        lblMinLen = new javax.swing.JLabel();
        txtMinLen = new javax.swing.JFormattedTextField();
        lblMinLenDetails = new javax.swing.JLabel();
        lblMaxLen = new javax.swing.JLabel();
        txtMaxLen = new javax.swing.JFormattedTextField();
        lblMaxLenDetails = new javax.swing.JLabel();
        pnlAdaptors = new javax.swing.JPanel();
        pnl3Prime = new javax.swing.JPanel();
        lbl3Prime = new javax.swing.JLabel();
        txt3Prime = new javax.swing.JFormattedTextField();
        lblPreDefined3Primes = new javax.swing.JLabel();
        cmbPreDefined3Primes = new javax.swing.JComboBox();
        lbl3PrimeLength = new javax.swing.JLabel();
        txt3PrimeLength = new javax.swing.JFormattedTextField();
        lbl3PrimeLengthDetails = new javax.swing.JLabel();
        pnl5Prime = new javax.swing.JPanel();
        lbl5Prime = new javax.swing.JLabel();
        txt5Prime = new javax.swing.JFormattedTextField();
        lblPreDefined5Primes = new javax.swing.JLabel();
        cmbPreDefined5Primes = new javax.swing.JComboBox();
        lbl5PrimeLength = new javax.swing.JLabel();
        txt5PrimeLength = new javax.swing.JFormattedTextField();
        lbl5PrimeLengthDetails = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        HD_Check = new javax.swing.JCheckBox();
        HD_Settings = new javax.swing.JPanel();
        HD_full_Check = new javax.swing.JRadioButton();
        HD_Simple_5P = new javax.swing.JRadioButton();
        HD_Simple_3P = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        inputSelectorPanel = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        outputSelectorPanel = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        fspDiscardedLogFile = new uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel();
        chkForceOverwrite = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(120, 120, 120));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Adaptor Remover Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        setForeground(new java.awt.Color(255, 255, 255));
        setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        pnlFiltering.setBackground(new java.awt.Color(120, 120, 120));
        pnlFiltering.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtering", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        lblMinLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMinLen.setForeground(new java.awt.Color(255, 255, 255));
        lblMinLen.setText("Min. Length:");

        txtMinLen.setText("16");
        txtMinLen.setToolTipText("<html>\nThe minimum length for every valid adaptor sequence in the sRNA dataset.\n</html>");
        txtMinLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        txtMinLen.setMaximumSize(new java.awt.Dimension(40, 20));
        txtMinLen.setMinimumSize(new java.awt.Dimension(40, 20));
        txtMinLen.setPreferredSize(new java.awt.Dimension(40, 20));
        txtMinLen.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                txtMinLenActionPerformed(evt);
            }
        });

        lblMinLenDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMinLenDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMinLenDetails.setText("(min. 16)");

        lblMaxLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMaxLen.setForeground(new java.awt.Color(255, 255, 255));
        lblMaxLen.setText("Max. Length:");

        txtMaxLen.setText("35");
        txtMaxLen.setToolTipText("<html> The maximum length for every valid adaptor sequence in the sRNA dataset.</html>");
        txtMaxLen.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        txtMaxLen.setMaximumSize(new java.awt.Dimension(40, 20));
        txtMaxLen.setMinimumSize(new java.awt.Dimension(40, 20));
        txtMaxLen.setPreferredSize(new java.awt.Dimension(40, 20));

        lblMaxLenDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblMaxLenDetails.setForeground(new java.awt.Color(255, 255, 255));
        lblMaxLenDetails.setText("(max. 35)");

        javax.swing.GroupLayout pnlFilteringLayout = new javax.swing.GroupLayout(pnlFiltering);
        pnlFiltering.setLayout(pnlFilteringLayout);
        pnlFilteringLayout.setHorizontalGroup(
            pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilteringLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMinLen)
                .addGap(7, 7, 7)
                .addComponent(txtMinLen, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMinLenDetails)
                .addGap(36, 36, 36)
                .addComponent(lblMaxLen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMaxLen, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblMaxLenDetails)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlFilteringLayout.setVerticalGroup(
            pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilteringLayout.createSequentialGroup()
                .addGroup(pnlFilteringLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMinLen)
                    .addComponent(txtMinLen, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMinLenDetails)
                    .addComponent(lblMaxLen)
                    .addComponent(txtMaxLen, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMaxLenDetails))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        pnlAdaptors.setBackground(new java.awt.Color(120, 120, 120));
        pnlAdaptors.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Adaptor Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        pnl3Prime.setBackground(new java.awt.Color(120, 120, 120));
        pnl3Prime.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "3' Adaptor (mandatory)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N
        pnl3Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lbl3Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl3Prime.setForeground(new java.awt.Color(255, 255, 255));
        lbl3Prime.setText("Adaptor Sequence:");

        txt3Prime.setToolTipText("<html>\nThe 3' adaptor sequence to use for adaptor removal. <br/> \nThis field is mandatory. <br/>\nNot all nucleotides in this sequence will necessarily be used for adaptor removal.  The actual sequence used is governed capped by the 3' adaptor sequence length parameter. <br/> \nMost reads are unlikely to contain the full adaptor sequence so this allows the user to adjust the sensitivity and specificity of the matching with a single parameter.\n</html>");
        txt3Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lblPreDefined3Primes.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblPreDefined3Primes.setForeground(new java.awt.Color(255, 255, 255));
        lblPreDefined3Primes.setText("Pre-defined Adaptors:");

        cmbPreDefined3Primes.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmbPreDefined3Primes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPreDefined3PrimesActionPerformed(evt);
            }
        });

        lbl3PrimeLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl3PrimeLength.setForeground(new java.awt.Color(255, 255, 255));
        lbl3PrimeLength.setText("Nucleotides to Use:");

        txt3PrimeLength.setText("8");
        txt3PrimeLength.setToolTipText("<html> The number of characters to use from the beginning of the 3' adaptor sequence.</html>");
        txt3PrimeLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lbl3PrimeLengthDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl3PrimeLengthDetails.setForeground(new java.awt.Color(255, 255, 255));
        lbl3PrimeLengthDetails.setText("(min. 5nt, max. 20nt)");

        javax.swing.GroupLayout pnl3PrimeLayout = new javax.swing.GroupLayout(pnl3Prime);
        pnl3Prime.setLayout(pnl3PrimeLayout);
        pnl3PrimeLayout.setHorizontalGroup(
            pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl3PrimeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbl3PrimeLength, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPreDefined3Primes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl3Prime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl3PrimeLayout.createSequentialGroup()
                        .addComponent(txt3PrimeLength, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(lbl3PrimeLengthDetails))
                    .addComponent(cmbPreDefined3Primes, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txt3Prime, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnl3PrimeLayout.setVerticalGroup(
            pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl3PrimeLayout.createSequentialGroup()
                .addGroup(pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl3Prime)
                    .addComponent(txt3Prime, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPreDefined3Primes)
                    .addComponent(cmbPreDefined3Primes, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl3PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl3PrimeLength)
                    .addComponent(lbl3PrimeLengthDetails)
                    .addComponent(txt3PrimeLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txt3PrimeLength.getAccessibleContext().setAccessibleDescription("<html> The number of characters to use from the 3' adaptor sequence.<br>  Must be at least 5nt and no more than the 20nt. </html>");

        pnl5Prime.setBackground(new java.awt.Color(120, 120, 120));
        pnl5Prime.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "5' Adaptor (optional)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N
        pnl5Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lbl5Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl5Prime.setForeground(new java.awt.Color(255, 255, 255));
        lbl5Prime.setText("Adaptor Sequence:");

        txt5Prime.setToolTipText("<html>\nThe 5' adaptor sequence to use for adaptor removal. <br/> \nThis field is optional, as many high-throughput sequencing devices automatically trim the 5' adaptor. <br/>\nNot all nucleotides in this sequence will necessarily be used for adaptor removal.  The actual sequence used is governed capped by the 5' adaptor sequence length parameter.\n</html>");
        txt5Prime.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lblPreDefined5Primes.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lblPreDefined5Primes.setForeground(new java.awt.Color(255, 255, 255));
        lblPreDefined5Primes.setText("Pre-defined Adaptors:");

        cmbPreDefined5Primes.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        cmbPreDefined5Primes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cmbPreDefined5PrimesActionPerformed(evt);
            }
        });

        lbl5PrimeLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl5PrimeLength.setForeground(new java.awt.Color(255, 255, 255));
        lbl5PrimeLength.setText("Nucleotides to Use:");

        txt5PrimeLength.setText("8");
        txt5PrimeLength.setToolTipText("<html> The number of characters to use from the end of the 5' adaptor sequence.</html>");
        txt5PrimeLength.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        lbl5PrimeLengthDetails.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        lbl5PrimeLengthDetails.setForeground(new java.awt.Color(255, 255, 255));
        lbl5PrimeLengthDetails.setText("(min. 5nt, max. 20nt)");

        javax.swing.GroupLayout pnl5PrimeLayout = new javax.swing.GroupLayout(pnl5Prime);
        pnl5Prime.setLayout(pnl5PrimeLayout);
        pnl5PrimeLayout.setHorizontalGroup(
            pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl5PrimeLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbl5PrimeLength, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPreDefined5Primes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl5Prime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnl5PrimeLayout.createSequentialGroup()
                        .addComponent(txt5PrimeLength, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl5PrimeLengthDetails))
                    .addComponent(txt5Prime)
                    .addComponent(cmbPreDefined5Primes, 0, 205, Short.MAX_VALUE)))
        );
        pnl5PrimeLayout.setVerticalGroup(
            pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl5PrimeLayout.createSequentialGroup()
                .addGroup(pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl5Prime)
                    .addComponent(txt5Prime, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPreDefined5Primes)
                    .addComponent(cmbPreDefined5Primes, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl5PrimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl5PrimeLength)
                    .addComponent(txt5PrimeLength, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl5PrimeLengthDetails))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(120, 120, 120));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HD Adaptor Protocol", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N

        HD_Check.setBackground(new java.awt.Color(120, 120, 120));
        HD_Check.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        HD_Check.setForeground(new java.awt.Color(255, 255, 255));
        HD_Check.setText("Enable HD Adaptor Processing");
        HD_Check.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                HD_CheckActionPerformed(evt);
            }
        });

        HD_Settings.setBackground(new java.awt.Color(120, 120, 120));
        HD_Settings.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));

        HD_full_Check.setBackground(new java.awt.Color(120, 120, 120));
        HD_optionsBtnGrp.add(HD_full_Check);
        HD_full_Check.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        HD_full_Check.setForeground(new java.awt.Color(255, 255, 255));
        HD_full_Check.setSelected(true);
        HD_full_Check.setText("HD Full");
        HD_full_Check.setToolTipText("For libraries containing full HD adaptors");
        HD_full_Check.setEnabled(false);

        HD_Simple_5P.setBackground(new java.awt.Color(120, 120, 120));
        HD_optionsBtnGrp.add(HD_Simple_5P);
        HD_Simple_5P.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        HD_Simple_5P.setForeground(new java.awt.Color(255, 255, 255));
        HD_Simple_5P.setText("HD Simple 5 Prime");
        HD_Simple_5P.setToolTipText("For libraries containing an HD adaptor on the 5 prime end only");
        HD_Simple_5P.setEnabled(false);

        HD_Simple_3P.setBackground(new java.awt.Color(120, 120, 120));
        HD_optionsBtnGrp.add(HD_Simple_3P);
        HD_Simple_3P.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        HD_Simple_3P.setForeground(new java.awt.Color(255, 255, 255));
        HD_Simple_3P.setText("HD Simple 3 Prime");
        HD_Simple_3P.setToolTipText("For libraries containing an HD adaptor on the 3 prime end only");
        HD_Simple_3P.setEnabled(false);

        javax.swing.GroupLayout HD_SettingsLayout = new javax.swing.GroupLayout(HD_Settings);
        HD_Settings.setLayout(HD_SettingsLayout);
        HD_SettingsLayout.setHorizontalGroup(
            HD_SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HD_SettingsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(HD_full_Check)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(HD_Simple_5P)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(HD_Simple_3P)
                .addContainerGap())
        );
        HD_SettingsLayout.setVerticalGroup(
            HD_SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HD_SettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(HD_SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(HD_Simple_3P)
                    .addComponent(HD_full_Check)
                    .addComponent(HD_Simple_5P))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(HD_Check)
                .addGap(18, 18, 18)
                .addComponent(HD_Settings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(HD_Settings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(HD_Check)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlAdaptorsLayout = new javax.swing.GroupLayout(pnlAdaptors);
        pnlAdaptors.setLayout(pnlAdaptorsLayout);
        pnlAdaptorsLayout.setHorizontalGroup(
            pnlAdaptorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdaptorsLayout.createSequentialGroup()
                .addGroup(pnlAdaptorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAdaptorsLayout.createSequentialGroup()
                        .addComponent(pnl3Prime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnl5Prime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        pnlAdaptorsLayout.setVerticalGroup(
            pnlAdaptorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAdaptorsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAdaptorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl3Prime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnl5Prime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File I/O", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N

        inputSelectorPanel.setHistorySingleMode(false);
        inputSelectorPanel.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.SRNA);
        inputSelectorPanel.setLabel("Input File Path (mandatory): ");
        inputSelectorPanel.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.MULTI_LOAD);

        outputSelectorPanel.setHistorySingleMode(true);
        outputSelectorPanel.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.SRNA);
        outputSelectorPanel.setLabel("Output File Directory (mandatory): ");
        outputSelectorPanel.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.DIRECTORY);

        fspDiscardedLogFile.setToolTipText("Optional: If a path is specified, all sequences that are filtered out are logged at this location.");
        fspDiscardedLogFile.setFilters(FileExtFilter.toFilterArray(FileExtFilter.FASTA));
        fspDiscardedLogFile.setFocusTraversalPolicyProvider(true);
        fspDiscardedLogFile.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        fspDiscardedLogFile.setHistorySingleMode(true);
        fspDiscardedLogFile.setHistoryType(uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType.SRNA);
        fspDiscardedLogFile.setLabel("Discarded Sequence Directory (Optional): ");
        fspDiscardedLogFile.setSelector(uk.ac.uea.cmp.srnaworkbench.swing.FileSelector.DIRECTORY);

        chkForceOverwrite.setBackground(new java.awt.Color(120, 120, 120));
        chkForceOverwrite.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        chkForceOverwrite.setForeground(new java.awt.Color(255, 255, 255));
        chkForceOverwrite.setText("Force Overwriting of files");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fspDiscardedLogFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputSelectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(inputSelectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(chkForceOverwrite)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(inputSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(outputSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fspDiscardedLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chkForceOverwrite)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlFiltering, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlAdaptors, javax.swing.GroupLayout.PREFERRED_SIZE, 730, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdaptors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlFiltering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


  //method for enable/disable HD settings 
  public final void setHD_OptionsEnabled(boolean enabled)
  {
    for(Component components : this.HD_Settings.getComponents())
    {
      components.setEnabled( enabled );
    }
  }
  /**
   * Enables or disables all components on the GUI
   * @param enabled 
   */
  @Override
  public final void setEnabled( boolean enabled )
  {
    super.setEnabled( enabled );
    
    this.lbl3Prime.setEnabled( enabled );
    this.lbl5Prime.setEnabled( enabled );
    this.lblMaxLen.setEnabled( enabled );
    this.lblMaxLenDetails.setEnabled( enabled );
    this.lblMinLen.setEnabled( enabled );
    this.lblMinLenDetails.setEnabled( enabled );
    this.lbl3PrimeLength.setEnabled( enabled );
    this.txt3PrimeLength.setEnabled( enabled );
    this.lbl3PrimeLengthDetails.setEnabled( enabled );
    this.lbl5PrimeLength.setEnabled( enabled );
    this.txt5PrimeLength.setEnabled( enabled );
    this.lbl5PrimeLengthDetails.setEnabled( enabled );
    this.txt3Prime.setEnabled( enabled );
    this.txt5Prime.setEnabled( enabled );
    this.txtMaxLen.setEnabled( enabled );
    this.txtMinLen.setEnabled( enabled );
    this.lblPreDefined3Primes.setEnabled( enabled );
    this.cmbPreDefined3Primes.setEnabled( enabled );
    this.lblPreDefined5Primes.setEnabled( enabled );
    this.cmbPreDefined5Primes.setEnabled( enabled );
    this.inputSelectorPanel.setEnabled( enabled );
    this.outputSelectorPanel.setEnabled( enabled );
    this.fspDiscardedLogFile.setEnabled( enabled );
    this.chkForceOverwrite.setEnabled( enabled );
    this.HD_Check.setEnabled(enabled);
    this.setHD_OptionsEnabled( enabled );
    
  }
  
  public final void setIOPanelEnabled( boolean enabled )
  {
    this.inputSelectorPanel.setEnabled( enabled );
    this.outputSelectorPanel.setEnabled( enabled );
    this.fspDiscardedLogFile.setEnabled( enabled );
  }

    private void txtMinLenActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtMinLenActionPerformed
    {//GEN-HEADEREND:event_txtMinLenActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_txtMinLenActionPerformed


    private void cmbPreDefined3PrimesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPreDefined3PrimesActionPerformed
      this.txt3Prime.setText( ( (AdaptorRemoverParams.Known3PrimeAdaptor) this.cmbPreDefined3Primes.getSelectedItem() ).getSequence() );
    }//GEN-LAST:event_cmbPreDefined3PrimesActionPerformed

  private void cmbPreDefined5PrimesActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_cmbPreDefined5PrimesActionPerformed
  {//GEN-HEADEREND:event_cmbPreDefined5PrimesActionPerformed
    this.txt5Prime.setText( ( (AdaptorRemoverParams.Known5PrimeAdaptor) this.cmbPreDefined5Primes.getSelectedItem() ).getSequence() );
  }//GEN-LAST:event_cmbPreDefined5PrimesActionPerformed

  private void HD_CheckActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_HD_CheckActionPerformed
  {//GEN-HEADEREND:event_HD_CheckActionPerformed
    setHD_OptionsEnabled(HD_Check.isSelected());
  }//GEN-LAST:event_HD_CheckActionPerformed

  /**
   * Creates the adaptor removal params object and saves it in this instance
   * @return True if params were created successfully, otherwise false
   */
  public boolean getConfig()
  {
    if ( this.isEnabled() )
    {
      return createParams();
    }
    return true;
  }

  /**
   * Creates an adaptor removal params object using values from GUI components 
   * and saves it in this instance
   * @return True if params were created successfully, otherwise false
   */
  public boolean createParams()
  {
    try
    {
      String adpt3 = txt3Prime.getText().trim();
      String adpt5 = txt5Prime.getText().trim();
      String min_len = txtMinLen.getText().trim();
      String max_len = txtMaxLen.getText().trim();
      String adpt3_len = txt3PrimeLength.getText().trim();
      String adpt5_len = txt5PrimeLength.getText().trim();
      
      String adpt3_final = adpt3.length() > 0 ? adpt3 : null;
      String adpt5_final = adpt5.length() > 0 ? adpt5 : null;
      int min_len_final = min_len.length() > 0 ? Integer.parseInt( min_len ) : MINIMUM_LENGTH.getDefault( Integer.class );
      int max_len_final = max_len.length() > 0 ? Integer.parseInt( max_len ) : MAXIMUM_LENGTH.getDefault( Integer.class );
      int adpt3_len_final = adpt3_len.length() > 0 ? Integer.parseInt( adpt3_len ) : ADAPTOR_3_LENGTH_TO_USE.getDefault( Integer.class );
      int adpt5_len_final = adpt5_len.length() > 0 ? Integer.parseInt( adpt5_len ) : ADAPTOR_5_LENGTH_TO_USE.getDefault( Integer.class );
      File discard_log_file = fspDiscardedLogFile.getFile();
      
      HD_Protocol hD_protocol = HD_Protocol.HD_NONE;
      String selected = GroupButtonUtils.getSelectedButtonText( HD_optionsBtnGrp );
    
      if(this.HD_Check.isSelected())
      {
 
      if(selected.equals( "HD Full") )
        hD_protocol = HD_Protocol.HD_FULL;
      else if(selected.equals( "HD Simple 5 Prime") )
        hD_protocol = HD_Protocol.HD_SIMPLE_5P;
      else if(selected.equals( "HD Simple 3 Prime") )
        hD_protocol = HD_Protocol.HD_SIMPLE_3P;
      }

      ar_params = new AdaptorRemoverParams.Builder()
        .set3PrimeAdaptor( adpt3_final ).set3PrimeAdaptorLength( adpt3_len_final )
        .set5PrimeAdaptor( adpt5_final ).set5PrimeAdaptorLength( adpt5_len_final )
        .setMinLength( min_len_final ).setMaxLength( max_len_final )
        .setDiscardLog( discard_log_file )
        .set_HD_protocol( hD_protocol )
        .build();
    }
    catch ( Exception ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );

      JOptionPane.showMessageDialog( this,
        ex.getMessage(),
        "Adaptor Remover Error",
        JOptionPane.ERROR_MESSAGE );

      return false;
    }
    return true;
  }

  /**
   * Helper method to update the file in and file out txt boxes
   * @param sRNAPath File containing reads with adaptors
   * @param outputPath File to write to containing sequences with adaptors removed
   */
  public void setTextInput( String sRNAPath, String outputPath )
  {
    inputSelectorPanel.setPath( sRNAPath );
    outputSelectorPanel.setPath( outputPath );
  }

  /**
   * Retrieves the input file from GUI panel
   * @return The input file
   */
  public File getInputFile()
  {
    return this.inputSelectorPanel.getFile();
  }
  public ArrayList<File> getInputFiles()
  {
    return this.inputSelectorPanel.getFiles();
  }

  /**
   * Retrieves the output file from the GUI panel
   * @return The output file
   */
  public File getOutputDir()
  {
    return this.outputSelectorPanel.getFile();
  }
  
  public void reset()
  {
    this.ar_params = new AdaptorRemoverParams();
    
    inputSelectorPanel.setPath( "" );
    outputSelectorPanel.setPath( "" );
    
    this.update( ar_params );
  }
  
  // ***** ToolParamsHost implementation *****

  @Override
  public AdaptorRemoverParams getParams()
  {
    if ( !createParams() )
    {
      return null;
    }

    return this.ar_params;
  }
  
  @Override
  public void update( ToolParameters params )
  {
    if ( !( params instanceof AdaptorRemoverParams ) )
    {
      showErrorDialog("Unexpected error: parameters object provided is not a valid adaptor remover params file.");
      return;
    }

    AdaptorRemoverParams arp = (AdaptorRemoverParams)params;

    this.txtMinLen.setText( Integer.toString( arp.getMinLength() ) );
    this.txtMaxLen.setText( Integer.toString( arp.getMaxLength() ) );
    this.txt3PrimeLength.setText( Integer.toString( arp.get3PrimeAdaptorLength() ) );
    this.txt5PrimeLength.setText( Integer.toString( arp.get5PrimeAdaptorLength() ) );
    this.txt3Prime.setText( arp.get3PrimeAdaptor() );
    this.txt5Prime.setText( arp.get5PrimeAdaptor() );
    this.cmbPreDefined3Primes.setSelectedIndex( 0 );
    this.cmbPreDefined5Primes.setSelectedIndex( 0 );
    this.fspDiscardedLogFile.setFilePath( arp.getDiscardLog() );
  }
  
  @Override
  public void showErrorDialog( String message )
  {
    JOptionPane.showMessageDialog( this,
      message,
      "Parameter Error",
      JOptionPane.ERROR_MESSAGE );
  }
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox HD_Check;
    private javax.swing.JPanel HD_Settings;
    private javax.swing.JRadioButton HD_Simple_3P;
    private javax.swing.JRadioButton HD_Simple_5P;
    private javax.swing.JRadioButton HD_full_Check;
    private javax.swing.ButtonGroup HD_optionsBtnGrp;
    private javax.swing.JCheckBox chkForceOverwrite;
    private javax.swing.JComboBox cmbPreDefined3Primes;
    private javax.swing.JComboBox cmbPreDefined5Primes;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel fspDiscardedLogFile;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel inputSelectorPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbl3Prime;
    private javax.swing.JLabel lbl3PrimeLength;
    private javax.swing.JLabel lbl3PrimeLengthDetails;
    private javax.swing.JLabel lbl5Prime;
    private javax.swing.JLabel lbl5PrimeLength;
    private javax.swing.JLabel lbl5PrimeLengthDetails;
    private javax.swing.JLabel lblMaxLen;
    private javax.swing.JLabel lblMaxLenDetails;
    private javax.swing.JLabel lblMinLen;
    private javax.swing.JLabel lblMinLenDetails;
    private javax.swing.JLabel lblPreDefined3Primes;
    private javax.swing.JLabel lblPreDefined5Primes;
    private uk.ac.uea.cmp.srnaworkbench.swing.FileSelectorPanel outputSelectorPanel;
    private javax.swing.JPanel pnl3Prime;
    private javax.swing.JPanel pnl5Prime;
    private javax.swing.JPanel pnlAdaptors;
    private javax.swing.JPanel pnlFiltering;
    private javax.swing.JFormattedTextField txt3Prime;
    private javax.swing.JFormattedTextField txt3PrimeLength;
    private javax.swing.JFormattedTextField txt5Prime;
    private javax.swing.JFormattedTextField txt5PrimeLength;
    private javax.swing.JFormattedTextField txtMaxLen;
    private javax.swing.JFormattedTextField txtMinLen;
    // End of variables declaration//GEN-END:variables

  boolean getOverwriteConfirm()
  {
    return this.chkForceOverwrite.isSelected();
  }

  public void addToHistory( ArrayList<String> theInput )
  {
    this.inputSelectorPanel.addToHistory( theInput );
  }
}
