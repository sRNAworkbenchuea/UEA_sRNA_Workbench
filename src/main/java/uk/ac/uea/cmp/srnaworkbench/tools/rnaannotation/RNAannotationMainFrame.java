/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RNAannotationMainFrame.java
 *
 * Created on 05-Jul-2011, 11:08:33
 */
package uk.ac.uea.cmp.srnaworkbench.tools.rnaannotation;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.help.JHLauncher;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.HairpinGenerateMainFrame;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;



/**
 *
 * @author w0445959
 */
public class RNAannotationMainFrame extends javax.swing.JInternalFrame implements GUIInterface
{
  private RNA_AnnotationGeneration myImageGenerator;
  private java.util.regex.Pattern actguFilter = java.util.regex.Pattern.compile( "[actguACTGU,]*" );
  //private BufferedImage originalImage;
  private ArrayList<BufferedImage> myImages = new ArrayList<BufferedImage>();
  private ArrayList<String[]> mySequences = new ArrayList<String[]>();
  private int currentDepth = -1;
  private Color secColourArray[] = new Color[14];
  private NavigableImagePanel myImagePanel;
  private boolean readingFromDisk = false;
  private File[] imagesOnDisk = null;// = new ArrayList<File>();
  private int diskImageIndex = -1;
  
  private static final int MAX_ANNOTATION_SEQUENCES = 50;

  /**
   * Creates new form RNAannotationMainFrame
   */
  public RNAannotationMainFrame()
  {

    initComponents();

    myImageGenerator = new RNA_AnnotationGeneration();
//        imageLayerPane.setLayout(new javax.swing.OverlayLayout(imageLayerPane));
//        imageLayerPane.invalidate();
//        imageLayerPane.revalidate();
    setup();

    Tools.trackPage( "RNA Annotation Main GUI Frame Class Loaded");


  }
//

  private void setup()
  {


    myImagePanel = new NavigableImagePanel();
    //String blackTextHairpin = hairpinSequence.replace( "#FFFFFF", "#000000" );
    //myImagePanel.setHairpinSequence( blackTextHairpin );
    myImagePanel.setHighQualityRenderingEnabled( true );
    myImagePanel.setBackground( new java.awt.Color( 120, 120, 120 ) );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout( myImagePanel );
    myImagePanel.setLayout( jPanel1Layout );
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 1069, Short.MAX_VALUE ) );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 632, Short.MAX_VALUE ) );



    RegexPatternFormatter actguRegexFormatter = new RegexPatternFormatter( actguFilter );
    actguRegexFormatter.setAllowsInvalid( false );
    DefaultFormatterFactory actguFactory = new DefaultFormatterFactory( actguRegexFormatter );


    miRNASequenceTxt.setFormatterFactory( actguFactory );


    for ( int i = 0; i < 14; i++ )
    {
      secColourArray[i] = new Color( 0, 0, 255 );
    }
    secColourArray[1] = new Color( 255, 0, 0 );

    //this.openMenuItem.setVisible( false );

    JHLauncher.setupContextDependentHelp( "HTML_hairpin_annot_html", loadHelp, this.getRootPane() );

    
    this.sequenceColourChooser.getSelectionModel().addChangeListener(
      new ChangeListener()
      {
        @Override
        public void stateChanged( ChangeEvent e )
        {
          Color newColour = sequenceColourChooser.getColor();
          if ( seqHighlightGroup.getSelection() != null && seqHighlightGroup.getSelection().isSelected() )
          {
  
            JRadioButton b = GroupButtonUtils.getSelectedRadioButton( seqHighlightGroup, secColourArray, newColour );

            String fullText = b.getText();
            String stripedText = fullText.substring( 0, fullText.indexOf( "(" ) );
            stripedText += "(" + newColour.getRed() + "," + newColour.getGreen() + "," + newColour.getBlue() + ")";
            b.setText( stripedText );
            b.setForeground( newColour );
          }
          //banner.setForeground( newColor );
        }
      } );
  }

  public RNAannotationMainFrame( String hairpinSequence, String hairpinDotBracket, int miRNALength, int miRNASTARLength, String MFE,
                                                                                                                         String strand )
  {
    initComponents();
    myImageGenerator = new RNA_AnnotationGeneration();
    setup();
    generateAndAdd( hairpinSequence, hairpinDotBracket, miRNALength, miRNASTARLength, MFE, strand);

    Tools.trackPage( "RNA Annotation Main GUI Class Loaded");

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

        seqHighlightGroup = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        renderOptionsPanel = new javax.swing.JPanel();
        hairpinSequencePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        hairpinSequenceTxt = new javax.swing.JTextPane();
        shortSequencePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        miRNASequenceTxt = new javax.swing.JFormattedTextField();
        jPanel1 = new javax.swing.JPanel();
        sequenceColourChooser = new javax.swing.JColorChooser();
        seqColour1 = new javax.swing.JRadioButton();
        seqColour2 = new javax.swing.JRadioButton();
        seqColour3 = new javax.swing.JRadioButton();
        seqColour4 = new javax.swing.JRadioButton();
        seqColour5 = new javax.swing.JRadioButton();
        seqColour6 = new javax.swing.JRadioButton();
        seqColour7 = new javax.swing.JRadioButton();
        seqColour8 = new javax.swing.JRadioButton();
        seqColour9 = new javax.swing.JRadioButton();
        seqColour10 = new javax.swing.JRadioButton();
        seqColour11 = new javax.swing.JRadioButton();
        seqColour12 = new javax.swing.JRadioButton();
        seqColour13 = new javax.swing.JRadioButton();
        seqColour14 = new javax.swing.JRadioButton();
        hairpinImagePanel = new javax.swing.JPanel();
        mainControlToolBar = new javax.swing.JToolBar();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        toggleViewOptions = new javax.swing.JCheckBox();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        backwardsClick = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        forwardsClick = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        saveAllButton = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        saveCurrentButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        generateButton = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        currentInformationPane = new javax.swing.JPanel();
        loadedImagesLabel = new javax.swing.JLabel();
        loadedImagesMFE = new javax.swing.JLabel();
        menuBar2 = new javax.swing.JMenuBar();
        fileMenu2 = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenu = new javax.swing.JMenu();
        saveSingleMenu = new javax.swing.JMenuItem();
        saveAllMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        loadHelp = new javax.swing.JMenuItem();

        setBackground(new java.awt.Color(120, 120, 120));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("RNA folding/annotation");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/newHAIRPIN.jpg"))); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        mainPanel.setBackground(new java.awt.Color(120, 120, 120));

        renderOptionsPanel.setBackground(new java.awt.Color(120, 120, 120));
        renderOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sequence Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N

        hairpinSequencePanel.setBackground(new java.awt.Color(120, 120, 120));
        hairpinSequencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hairpin Sequence", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N
        hairpinSequencePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(hairpinSequenceTxt);

        hairpinSequencePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        shortSequencePanel.setBackground(new java.awt.Color(120, 120, 120));
        shortSequencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Short Sequences", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N
        shortSequencePanel.setLayout(new javax.swing.BoxLayout(shortSequencePanel, javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setBackground(new java.awt.Color(120, 120, 120));
        jLabel1.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Enter up to 20 comma separated short sequences");
        shortSequencePanel.add(jLabel1);

        miRNASequenceTxt.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        miRNASequenceTxt.setToolTipText("Enter Hairpin Sequence");
        miRNASequenceTxt.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        shortSequencePanel.add(miRNASequenceTxt);

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sequence Colours (R,G,B)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(255, 255, 255))); // NOI18N

        sequenceColourChooser.setBackground(new java.awt.Color(120, 120, 120));
        sequenceColourChooser.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N

        seqColour1.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour1);
        seqColour1.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour1.setForeground(new java.awt.Color(255, 255, 255));
        seqColour1.setText("Sequence 1: (0,0,255)");

        seqColour2.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour2);
        seqColour2.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour2.setForeground(new java.awt.Color(255, 255, 255));
        seqColour2.setText("Sequence 2: (255,0,0)");

        seqColour3.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour3);
        seqColour3.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour3.setForeground(new java.awt.Color(255, 255, 255));
        seqColour3.setText("Sequence 3: (255,0,0)");

        seqColour4.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour4);
        seqColour4.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour4.setForeground(new java.awt.Color(255, 255, 255));
        seqColour4.setText("Sequence 4: (255,0,0)");

        seqColour5.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour5);
        seqColour5.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour5.setForeground(new java.awt.Color(255, 255, 255));
        seqColour5.setText("Sequence 5: (255,0,0)");

        seqColour6.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour6);
        seqColour6.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour6.setForeground(new java.awt.Color(255, 255, 255));
        seqColour6.setText("Sequence 6: (255,0,0)");

        seqColour7.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour7);
        seqColour7.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour7.setForeground(new java.awt.Color(255, 255, 255));
        seqColour7.setText("Sequence 7: (255,0,0)");

        seqColour8.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour8);
        seqColour8.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour8.setForeground(new java.awt.Color(255, 255, 255));
        seqColour8.setText("Sequence 9: (255,0,0)");

        seqColour9.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour9);
        seqColour9.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour9.setForeground(new java.awt.Color(255, 255, 255));
        seqColour9.setText("Sequence 8: (255,0,0)");

        seqColour10.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour10);
        seqColour10.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour10.setForeground(new java.awt.Color(255, 255, 255));
        seqColour10.setText("Sequence 10: (255,0,0)");

        seqColour11.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour11);
        seqColour11.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour11.setForeground(new java.awt.Color(255, 255, 255));
        seqColour11.setText("Sequence 11: (255,0,0)");

        seqColour12.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour12);
        seqColour12.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour12.setForeground(new java.awt.Color(255, 255, 255));
        seqColour12.setText("Sequence 12: (255,0,0)");

        seqColour13.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour13);
        seqColour13.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour13.setForeground(new java.awt.Color(255, 255, 255));
        seqColour13.setText("Sequence 13: (255,0,0)");

        seqColour14.setBackground(new java.awt.Color(120, 120, 120));
        seqHighlightGroup.add(seqColour14);
        seqColour14.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        seqColour14.setForeground(new java.awt.Color(255, 255, 255));
        seqColour14.setText("Sequence 14: (255,0,0)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seqColour1)
                    .addComponent(seqColour2)
                    .addComponent(seqColour3)
                    .addComponent(seqColour4)
                    .addComponent(seqColour5)
                    .addComponent(seqColour6)
                    .addComponent(seqColour7)
                    .addComponent(seqColour9)
                    .addComponent(seqColour8)
                    .addComponent(seqColour10)
                    .addComponent(seqColour11)
                    .addComponent(seqColour12)
                    .addComponent(seqColour14)
                    .addComponent(seqColour13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sequenceColourChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sequenceColourChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(seqColour1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seqColour14)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout renderOptionsPanelLayout = new javax.swing.GroupLayout(renderOptionsPanel);
        renderOptionsPanel.setLayout(renderOptionsPanelLayout);
        renderOptionsPanelLayout.setHorizontalGroup(
            renderOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderOptionsPanelLayout.createSequentialGroup()
                .addGroup(renderOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(hairpinSequencePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shortSequencePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        renderOptionsPanelLayout.setVerticalGroup(
            renderOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderOptionsPanelLayout.createSequentialGroup()
                .addGroup(renderOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, renderOptionsPanelLayout.createSequentialGroup()
                        .addComponent(hairpinSequencePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(shortSequencePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        hairpinImagePanel.setBackground(new java.awt.Color(120, 120, 120));
        hairpinImagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hairpin Image", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N
        hairpinImagePanel.setPreferredSize(new java.awt.Dimension(473, 708));

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout(hairpinImagePanel);
        hairpinImagePanel.setLayout(hairpinImagePanelLayout);
        hairpinImagePanelLayout.setHorizontalGroup(
            hairpinImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        hairpinImagePanelLayout.setVerticalGroup(
            hairpinImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 723, Short.MAX_VALUE)
        );

        mainControlToolBar.setBackground(new java.awt.Color(120, 120, 120));
        mainControlToolBar.setFloatable(false);
        mainControlToolBar.setRollover(true);
        mainControlToolBar.add(filler6);

        toggleViewOptions.setBackground(new java.awt.Color(120, 120, 120));
        toggleViewOptions.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        toggleViewOptions.setForeground(new java.awt.Color(255, 255, 255));
        toggleViewOptions.setSelected(true);
        toggleViewOptions.setText("Display Sequence Options");
        toggleViewOptions.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                toggleViewOptionsActionPerformed(evt);
            }
        });
        mainControlToolBar.add(toggleViewOptions);
        mainControlToolBar.add(filler1);

        backwardsClick.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/arrow-round_left.png"))); // NOI18N
        backwardsClick.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                backwardsClickActionPerformed(evt);
            }
        });
        mainControlToolBar.add(backwardsClick);
        mainControlToolBar.add(filler4);

        forwardsClick.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/arrow-round_right.png"))); // NOI18N
        forwardsClick.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                forwardsClickActionPerformed(evt);
            }
        });
        mainControlToolBar.add(forwardsClick);
        mainControlToolBar.add(filler5);

        saveAllButton.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        saveAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/saveAll.png"))); // NOI18N
        saveAllButton.setText("Save All Hairpins");
        saveAllButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAllButtonActionPerformed(evt);
            }
        });
        mainControlToolBar.add(saveAllButton);
        mainControlToolBar.add(filler2);

        saveCurrentButton.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        saveCurrentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        saveCurrentButton.setText("Save Current Hairpin");
        saveCurrentButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveCurrentButtonActionPerformed(evt);
            }
        });
        mainControlToolBar.add(saveCurrentButton);
        mainControlToolBar.add(filler3);

        generateButton.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        generateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/reload.png"))); // NOI18N
        generateButton.setText("Generate Hairpin");
        generateButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                generateButtonActionPerformed(evt);
            }
        });
        mainControlToolBar.add(generateButton);
        mainControlToolBar.add(filler7);

        currentInformationPane.setBackground(new java.awt.Color(120, 120, 120));
        currentInformationPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Display Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Sans Unicode", 0, 11), new java.awt.Color(255, 255, 255))); // NOI18N

        loadedImagesLabel.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N
        loadedImagesLabel.setForeground(new java.awt.Color(255, 255, 255));
        loadedImagesLabel.setText("No hairpins loaded");

        loadedImagesMFE.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N
        loadedImagesMFE.setForeground(new java.awt.Color(255, 255, 255));
        loadedImagesMFE.setText("MFE:");

        javax.swing.GroupLayout currentInformationPaneLayout = new javax.swing.GroupLayout(currentInformationPane);
        currentInformationPane.setLayout(currentInformationPaneLayout);
        currentInformationPaneLayout.setHorizontalGroup(
            currentInformationPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(currentInformationPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(currentInformationPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loadedImagesLabel)
                    .addComponent(loadedImagesMFE))
                .addContainerGap(151, Short.MAX_VALUE))
        );
        currentInformationPaneLayout.setVerticalGroup(
            currentInformationPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(currentInformationPaneLayout.createSequentialGroup()
                .addComponent(loadedImagesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(loadedImagesMFE))
        );

        mainControlToolBar.add(currentInformationPane);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainControlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(renderOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(hairpinImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1085, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(mainControlToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hairpinImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 749, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(mainPanel);

        getContentPane().add(jScrollPane3);

        menuBar2.setBackground(new java.awt.Color(213, 219, 245));

        fileMenu2.setText("File");

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/add-item.png"))); // NOI18N
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu2.add(openMenuItem);

        saveMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        saveMenu.setText("Save");
        saveMenu.setEnabled(false);

        saveSingleMenu.setText("Save Current Hairpin");
        saveSingleMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveSingleMenuActionPerformed(evt);
            }
        });
        saveMenu.add(saveSingleMenu);

        saveAllMenu.setText("Save All Hairpins");
        saveAllMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAllMenuActionPerformed(evt);
            }
        });
        saveMenu.add(saveAllMenu);

        fileMenu2.add(saveMenu);
        fileMenu2.add(jSeparator1);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/close-tool.png"))); // NOI18N
        exitMenuItem.setText("Close");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu2.add(exitMenuItem);

        menuBar2.add(fileMenu2);

        helpMenu.setText("Help");

        loadHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        loadHelp.setText("Contents");
        helpMenu.add(loadHelp);

        menuBar2.add(helpMenu);

        setJMenuBar(menuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
  public void shutdown()
  {
   this.setVisible( false );
  
  this.dispose();
  }
  @Deprecated
  public void initDemoImage()
  {
    try
    {

      BufferedImage originalImage = ImageIO.read( new File( Tools.userDataDirectoryPath + DIR_SEPARATOR + "HAIRPIN.jpg" ) );

      NavigableImagePanel imagePanel = new NavigableImagePanel( originalImage );
      imagePanel.setHighQualityRenderingEnabled( true );
      imagePanel.setBackground( new java.awt.Color( 120, 120, 120 ) );

      javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout( imagePanel );
      imagePanel.setLayout( imagePanelLayout );
      imagePanelLayout.setHorizontalGroup(
        imagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 480, Short.MAX_VALUE ) );
      imagePanelLayout.setVerticalGroup(
        imagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 666, Short.MAX_VALUE ) );

      javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
      hairpinImagePanel.setLayout( hairpinImagePanelLayout );
      hairpinImagePanelLayout.setHorizontalGroup(
        hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 480, Short.MAX_VALUE ).addGroup( hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) ) );
      hairpinImagePanelLayout.setVerticalGroup(
        hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGap( 0, 677, Short.MAX_VALUE ).addGroup( hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addGroup( hairpinImagePanelLayout.createSequentialGroup().addComponent( imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ).addContainerGap() ) ) );

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  private void formatInformation( String hairpinSequenceHTML, String hairpinDotBracketHTML, int[] miRNA_Index, int[] mirSTAR_Index, String[] formatedData, int miRNALength, int miRNASTARLength )
  {
    String hairpinSequence = hairpinSequenceHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" ).replace( "<u><font color=#0000FF>", "" ).replace( "</u></font>", "" ).replace( "<u><font color=#FF0000>", "" ).replace( "<font color=#FFFFFF>", "" ).replace( "</u>", "" );
    String hairpinDotBracket = hairpinDotBracketHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" );
    String finalHairpinDotBracket = ( ( ( ( ( hairpinDotBracket.replace( "-", "." ) ).replace( "<", "(" ) ).replace( ">", ")" ) ).replace( "{", "(" ) ).replace( "}", ")" ) ).replace( "=", "." );

    formatedData[0] = hairpinSequence;
    formatedData[1] = finalHairpinDotBracket;
    int miRNA_startIndex = -1;
    int miRNA_endIndex = -1;
    int miRNA_STAR_startIndex = -1;
    int miRNA_STAR_endIndex = -1;


    if ( hairpinDotBracket.contains( "<" ) )
    {
      int tempStartChev = hairpinDotBracket.indexOf( "<" );
      int tempStartDash = hairpinDotBracket.indexOf( "-" );
      
      int tempEndChev = hairpinDotBracket.lastIndexOf( "<" );
      int tempEndDash = hairpinDotBracket.lastIndexOf( "-" );
      
      if(tempStartDash == -1)
        miRNA_startIndex = tempStartChev;
      else
        miRNA_startIndex = Math.min( tempStartChev, tempStartDash );
      
      if(tempEndDash == -1)
      {
        miRNA_endIndex = tempEndChev;
      }
      else
      {
        miRNA_endIndex = Math.max( tempEndChev, tempEndDash );
      }
      //miRNA_endIndex = miRNA_startIndex + miRNALength;
    }
    else
    {
      int tempStartChev = hairpinDotBracket.indexOf( ">" );
      int tempStartDash = hairpinDotBracket.indexOf( "-" );
      int tempEndChev = hairpinDotBracket.lastIndexOf( ">" );
      int tempEndDash = hairpinDotBracket.lastIndexOf( "-" );
      
      if(tempStartDash == -1)
        miRNA_startIndex = tempStartChev;
      else
        miRNA_startIndex = Math.min(tempStartChev ,tempStartDash );
      //miRNA_endIndex = miRNA_startIndex + miRNALength;
      
      if(tempEndDash == -1)
      {
        miRNA_endIndex = tempEndChev;
      }
      else
      {
        miRNA_endIndex = Math.max( tempEndChev, tempEndDash );
      }
    }

    if ( hairpinDotBracket.contains( "{" ) )
    {
      int startCurlyBrace = hairpinDotBracket.indexOf( "{" );
      int startEql = hairpinDotBracket.indexOf( "=" );
      
      int endCurlyBrace = hairpinDotBracket.lastIndexOf( "{" );
      int endEql = hairpinDotBracket.lastIndexOf( "=" );
      
      if(startEql == -1)
        miRNA_STAR_startIndex = startCurlyBrace;
      else
        miRNA_STAR_startIndex = Math.min(startCurlyBrace ,  startEql);
      
      if(endEql == -1)
      {
        miRNA_STAR_endIndex = endCurlyBrace;
      }
      else
      {
        miRNA_STAR_endIndex = Math.max( endCurlyBrace, endEql );
      }
      
      //miRNA_STAR_endIndex = miRNA_STAR_startIndex + miRNASTARLength;
      mirSTAR_Index[0] = miRNA_STAR_startIndex+1;
      mirSTAR_Index[1] = miRNA_STAR_endIndex+1;
    }
    else if ( hairpinDotBracket.contains( "}" ) )
    {
      int startCurlyBrace = hairpinDotBracket.indexOf( "}" );
      int startEql = hairpinDotBracket.indexOf( "=" );

      int endCurlyBrace = hairpinDotBracket.lastIndexOf( "}" );
      int endEql = hairpinDotBracket.lastIndexOf( "=" );

      if ( startEql == -1 )
      {
        miRNA_STAR_startIndex = startCurlyBrace;
      }
      else
      {
        miRNA_STAR_startIndex = Math.min( startCurlyBrace, startEql );
      }
      if ( endEql == -1 )
      {
        miRNA_STAR_endIndex = endCurlyBrace;
      }
      else
      {
        miRNA_STAR_endIndex = Math.max( endCurlyBrace, endEql );
      }

      //miRNA_STAR_endIndex = miRNA_STAR_startIndex + miRNASTARLength;

      mirSTAR_Index[0] = miRNA_STAR_startIndex+1;
      mirSTAR_Index[1] = miRNA_STAR_endIndex+1;
    }
    miRNA_Index[0] = miRNA_startIndex +1;
    miRNA_Index[1] = miRNA_endIndex +1;

  }

  private void generateFromGUI()
  {
    
    try
    {
      String hairpinSequence = hairpinSequenceTxt.getText();
      BinaryExecutor exe = AppUtils.INSTANCE.getBinaryExecutor();

      String filePath = Tools.getNextDirectory().getPath() + DIR_SEPARATOR + "hairpinFile.fas";
      FileWriter fstream = new FileWriter( filePath );
      BufferedWriter out = new BufferedWriter( fstream );
      out.write( ">1" + LINE_SEPARATOR );
      out.write( hairpinSequence );

      out.close();
      fstream.close();

      String instruction = " -C " + filePath;
      String result = exe.execRNAFold( hairpinSequence, instruction );
      //System.out.println("result: " + result);
      String[] resultArray = result.split( " " );
      String temp1 = resultArray[1].replaceAll( "\\(", "" );
      String temp2 = temp1.replaceAll( "\\)", "" );
      resultArray[1] = temp2;
      //System.out.println( "result array 1: " + resultArray[1] );

      String hairpinHTML = StringUtils.constructHTMLStrings( hairpinSequence, "", "", "#000000", null );

      //System.out.println("dot bracket: " + resultArray[0]);
      if ( resultArray[0].contains( "(" ) || resultArray[0].contains( ")" ) )
      {

        ArrayList<int[]> shortReads = new ArrayList<int[]>();
        if ( !miRNASequenceTxt.getText().isEmpty() )
        {

          String[] splitText = miRNASequenceTxt.getText().split( "\\," );
          if ( splitText.length > 14 )
          {
            JOptionPane.showMessageDialog( this,
              "More than 14 short sequences entered" + LINE_SEPARATOR + "Only first 14 sequences will be highlighted",
              "Short Sequence Input Message",
              JOptionPane.INFORMATION_MESSAGE );
          }
          int amountOfShorts = 0;
          for ( String sequence : splitText )
          {
            if ( amountOfShorts < 14 )
            {
              int startTestIndex = hairpinSequence.indexOf( sequence );
              int endTestIndex = hairpinSequence.lastIndexOf( sequence );
              System.out.println( "lastIndex: " + endTestIndex );
              if ( startTestIndex < 0 )
              {
                boolean continueCheck = true;
                for ( int i = 0; i < sequence.length()-3 && continueCheck; i++ )
                {
                  String toCheck = sequence.substring( i, sequence.length() );
                  int startIndex = hairpinSequence.indexOf( toCheck );
                  if ( startIndex >= 0 )
                  {
                    shortReads.add(
                      new int[]
                      {
                        startIndex+1, startIndex + toCheck.length()
                      } );
                    continueCheck = false;
                  }
                }
                if ( continueCheck )
                {
                  for ( int i = sequence.length()-3; i >= 0 && continueCheck; i-- )
                  {
                    String toCheck = sequence.substring( 0, i );
                    int startIndex = hairpinSequence.indexOf( toCheck );
                    if ( startIndex >= 0 )
                    {
                      shortReads.add(
                        new int[]
                        {
                          startIndex + 1, startIndex + toCheck.length()
                        } );

                      continueCheck = false;
                    }
                  }
                }
              }
              else
              {
                shortReads.add(
                  new int[]
                  {
                    startTestIndex+1, startTestIndex + sequence.length()
                  } );
              }
              amountOfShorts++;
            }
          }
        }
        generateAndAdd( hairpinSequence, resultArray[0], shortReads, resultArray[1] );
        updateInformation( hairpinHTML, resultArray[1] );

      }
      else
      {
        JOptionPane.showMessageDialog( null,
          "Entered hairpin sequence did not fold",
          "Folding error",
          JOptionPane.ERROR_MESSAGE );
      }

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  private ArrayList<String> generateFromFile(String hairpinSequence, ArrayList<int[]> shortReads, ArrayList<String> shorts)
  {
    
    try
    {
      
      BinaryExecutor exe = AppUtils.INSTANCE.getBinaryExecutor();

      String filePath = Tools.getNextDirectory().getPath() + DIR_SEPARATOR + "hairpinFile.fas";
      FileWriter fstream = new FileWriter( filePath );
      BufferedWriter out = new BufferedWriter( fstream );
      out.write( ">1" + LINE_SEPARATOR );
      out.write( hairpinSequence );

      out.close();
      fstream.close();

      String instruction = " -C " + filePath;
      String result = exe.execRNAFold( hairpinSequence, instruction );
      //System.out.println("result: " + result);
      String[] resultArray = result.split( " " );
      String temp1 = resultArray[1].replaceAll( "\\(", "" );
      String temp2 = temp1.replaceAll( "\\)", "" );
      resultArray[1] = temp2;
      //System.out.println( "result array 1: " + resultArray[1] );

      String hairpinHTML = StringUtils.constructHTMLStrings( hairpinSequence, "", "", "#000000", null );

      //System.out.println("dot bracket: " + resultArray[0]);
      if ( resultArray[0].contains( "(" ) || resultArray[0].contains( ")" ) )
      {

        ArrayList<String> foldedInfo = new ArrayList<String>();
        foldedInfo.add( resultArray[0] );
        foldedInfo.add( resultArray[1] );
        for ( String sequence : shorts )
        {
          int startTestIndex = hairpinSequence.indexOf( sequence );
          if ( startTestIndex < 0 )
          {
            boolean continueCheck = true;
            for ( int i = 0; i < sequence.length() - 3 && continueCheck; i++ )
            {
              String toCheck = sequence.substring( i, sequence.length() );
              int startIndex = hairpinSequence.indexOf( toCheck );
              if ( startIndex >= 0 )
              {
                shortReads.add(
                  new int[]
                  {
                    startIndex + 1, startIndex + toCheck.length()
                  } );
                continueCheck = false;
              }
            }
            if ( continueCheck )
            {
              for ( int i = sequence.length() - 3; i >= 0 && continueCheck; i-- )
              {
                String toCheck = sequence.substring( 0, i );
                int startIndex = hairpinSequence.indexOf( toCheck );
                if ( startIndex >= 0 )
                {
                  shortReads.add(
                    new int[]
                    {
                      startIndex + 1, startIndex + toCheck.length()
                    } );

                  continueCheck = false;
                }
              }
            }
          }
          else
          {
            shortReads.add(
              new int[]
              {
                startTestIndex + 1, startTestIndex + sequence.length()
              } );
          }
        }
        return foldedInfo;

      }
      else
      {
        LOGGER.log(Level.WARNING, "Entered hairpin sequence did not fold{0}", hairpinSequence);
        return null;
      }

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      return null;
    }
  }
  public final void generateAndSave( String hairpinSequence, String hairpinDotBracket, int miRNALength, int miRNASTARLength,
                                     File toUse, String[] selectedExtension )
  {
    int[] miRNA_Index = new int[2];
    int[] mirSTAR_Index = new int[2];
    String[] formatedData = new String[2];
    formatInformation( hairpinSequence, hairpinDotBracket, miRNA_Index, mirSTAR_Index, formatedData, miRNALength, miRNASTARLength );
    //imageLayerPane.setLayout(new javax.swing.OverlayLayout(imageLayerPane));


    try
    {
      ArrayList<int[]> shortReads = new ArrayList<int[]>();
      shortReads.add( miRNA_Index );
      if ( ( mirSTAR_Index[0] >= 0 ) && ( mirSTAR_Index[1] >= 0 ) )
      {
        shortReads.add( mirSTAR_Index );
      }
      myImageGenerator.generateImage( formatedData[0], formatedData[1], shortReads, secColourArray );

      BufferedImage originalImage = ImageIO.read( new File( Tools.RNA_Annotation_dataPath + DIR_SEPARATOR + "HAIRPIN.jpg" ) );


      ImageIO.write( originalImage, selectedExtension[0], toUse );

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  public final void generateAndSave(String hairpinSequence, String hairpinDotBracket, ArrayList<int[]> miRNA_Index, String MFE,
                        File toUse, String[] selectedExtension )
  {
     try
    {
      myImageGenerator.generateImage( hairpinSequence, hairpinDotBracket, miRNA_Index, secColourArray );

      BufferedImage originalImage = ImageIO.read( new File( Tools.RNA_Annotation_dataPath + DIR_SEPARATOR + "HAIRPIN.jpg" ) );

      ImageIO.write( originalImage, selectedExtension[0], toUse );
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }

  }
  
  public final void openImagesFromDisc( File directory, String extension )
  {
    readingFromDisk = true;
    //this.renderOptionsPanel.setEnabled( false);
    showingOptions = false;
    checkSequenceOptions();
    
    FileFilter fileFilter = new FileTypeFilter(extension);
    
    imagesOnDisk = directory.listFiles( fileFilter );
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {

        forwardsMove();
      }
    } );
    
  }

  public final void generateAndAdd( String hairpinSequence, String hairpinDotBracket, int miRNALength, int miRNASTARLength, String MFE,
                                                                                                                            String strand)
  {

    Tools.trackEvent( "Internal Procedure", "Generating RNA image probably from miRCat");
    int[] miRNA_Index = new int[2];
    int[] mirSTAR_Index = new int[2];
    String[] formatedData = new String[2];
    
    formatInformation( hairpinSequence, hairpinDotBracket, miRNA_Index, mirSTAR_Index, formatedData, miRNALength, miRNASTARLength );

    try
    {
      ArrayList<int[]> shortReads = new ArrayList<int[]>();

      shortReads.add( miRNA_Index );
      if ( ( mirSTAR_Index[0] >= 0 ) && ( mirSTAR_Index[1] >= 0 ) )
      {
        shortReads.add( mirSTAR_Index );
      }

      
      myImageGenerator.generateImage( formatedData[0], formatedData[1], shortReads, secColourArray );

      BufferedImage originalImage = ImageIO.read( new File( Tools.RNA_Annotation_dataPath + DIR_SEPARATOR + "HAIRPIN.jpg" ) );

      myImagePanel.setImage( originalImage );
      myImages.add( originalImage );
      mySequences.add( new String[]
        {
          formatedData[0], MFE
        } );
      this.saveMenu.setEnabled( true );
      forwardsMove();

    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  public final void generateAndAdd( String hairpinSequence, String hairpinDotBracket, ArrayList<int[]> miRNA_Index, String MFE )
  {

    try
    {
      myImageGenerator.generateImage( hairpinSequence, hairpinDotBracket, miRNA_Index, secColourArray );

      //ImageIcon icon = new ImageIcon(Tools.RNA_Annotation_dataPath + Tools.FILE_SEPARATOR + "HAIRPIN.jpg" );
      //originalImage = getBufferedImageFromImage(icon.getImage());
      BufferedImage originalImage = ImageIO.read( new File( Tools.RNA_Annotation_dataPath + DIR_SEPARATOR + "HAIRPIN.jpg" ) );

      //NavigableImagePanel imagePanel = new NavigableImagePanel( originalImage );
      //myImagePanel.setImage(originalImage);
//      imagePanel.setHairpinSequence( hairpinSequence );
//      imagePanel.setHighQualityRenderingEnabled( true );
//      imagePanel.setBackground( new java.awt.Color( 120, 120, 120 ) );

      myImages.add( originalImage );
      mySequences.add( new String[]
        {
          hairpinSequence, MFE
        } );
      this.saveMenu.setEnabled( true );
      forwardsMove();
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  /**
   * This method takes the Image object and creates BufferedImage of it
   *
   * @param img
   * @return
   */
  private BufferedImage getBufferedImageFromImage( Image img )
  {
    //This line is important, this makes sure that the image is 
    //loaded fully 
    img = new ImageIcon( img ).getImage();

    //Create the BufferedImage object with the width and height of the Image 
    BufferedImage bufferedImage = new BufferedImage( img.getWidth( null ), img.getHeight( null ), BufferedImage.TYPE_INT_RGB );

    //Create the graphics object from the BufferedImage 
    Graphics g = bufferedImage.createGraphics();

    //Draw the image on the graphics of the BufferedImage 
    g.drawImage( img, 0, 0, null );

    //Dispose the Graphics 
    g.dispose();

    //return the BufferedImage 
    return bufferedImage;
  }

  private void updateInformation( String hairpinSequence, String MFE )
  {
    if ( !this.readingFromDisk )
    {
      loadedImagesLabel.setText( "Displaying hairpin " + ( currentDepth + 1 ) + " of " + myImages.size() );

      currentInformationPane.setToolTipText( hairpinSequence );
      this.loadedImagesMFE.setText( "Current Hairpin MFE: " + MFE );
    }
    else
    {
      loadedImagesLabel.setText( "Displaying hairpin " + ( this.diskImageIndex + 1 ) + " of " + this.imagesOnDisk.length );

      currentInformationPane.setToolTipText( hairpinSequence );
      this.loadedImagesMFE.setText( "Current Hairpin MFE not available " );
    }

  }

  public void clearAllCurrentImages()
  {
    myImages.clear();
    currentDepth = -1;
    loadedImagesLabel.setText( "No hairpins loaded" );
    this.hairpinImagePanel.removeAll();
  }
  private void saveSingle()
  {
    if ( currentDepth < 0 )
    {
      JOptionPane.showMessageDialog( null,
        "No images have been loaded",
        "Missing Images",
        JOptionPane.ERROR_MESSAGE );
      return;
    }
    String[] availableFormats = FileTypeFilter.getImageFormats();

    String selectedExtension[] =
    {
      ""
    };
    File saveFile = FileDialogUtils.showFileSaveDialog( this, availableFormats, "Select Image Format", false, selectedExtension );
    if ( saveFile != null )
    {
      //System.out.println(Tools.getExtension(saveFile));
      //System.out.println("format: " + selectedExtension[0]);
      File toUse = null;
      if ( saveFile.getAbsolutePath().contains( selectedExtension[0] ) )
      {
        toUse = new File( saveFile.getAbsolutePath() );
      }
      else
      {
        toUse = new File( saveFile.getAbsolutePath() + selectedExtension[0] );
      }


      try
      {
        //System.out.println("seq: " + myImages.get(currentDepth).getHairpinSequence());
        ImageIO.write( myImages.get( currentDepth ), selectedExtension[0], toUse );
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }
  private void saveAll()
  {
    String[] availableFormats = FileTypeFilter.getImageFormats();

    for ( String temp : availableFormats )
    {
      LOGGER.log( Level.FINE, temp );
    }
    String selectedExtension[] =
    {
      ""
    };
    File saveFile = FileDialogUtils.showFileSaveDialog( this, availableFormats, null, true, selectedExtension );

    //System.out.println("name: " + saveFile.getAbsolutePath());
    if ( saveFile != null )
    {
      try
      {
        //import org.apache.commons.io.FilenameUtils;
        String fileNameWithOutExt = FilenameUtils.removeExtension(saveFile.getAbsolutePath());
        File saveDirectory = new File( fileNameWithOutExt );
        if ( !saveDirectory.exists() )
        {
          saveDirectory.mkdir();
        }
        File informationFile = new File( fileNameWithOutExt + DIR_SEPARATOR + "fileLog.txt" );
        if ( !informationFile.exists() )
        {
          informationFile.createNewFile();
        }
        FileWriter outImageFile = new FileWriter( informationFile );
        PrintWriter outImageWriter = new PrintWriter( outImageFile );
        int i = 1;
        outImageWriter.println( "File type selected: " + selectedExtension[0] );
        outImageWriter.println( "Total hairpins rendered: " + myImages.size() );
        outImageWriter.println( "-----------------------------" );

        for ( BufferedImage image : myImages )
        {

          File toUse = new File( fileNameWithOutExt + DIR_SEPARATOR + "HairpinImage" + "_" + i + "." + selectedExtension[0] );

          outImageWriter.println( "Image name:" + toUse.getName() );
          outImageWriter.println( "Hairpin Sequence:" + StringUtils.removeHTML( mySequences.get( i - 1 )[0] ) );

          ImageIO.write( image, selectedExtension[0], toUse );
          outImageWriter.println( "-----------------------------" );
          outImageWriter.println();
          i++;

        }
        outImageFile.close();
        outImageWriter.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }
  private void backwardsMove()
  {
    if ( myImages.isEmpty() && !readingFromDisk)
    {
      return;
    }
    if ( readingFromDisk  )
    {
      diskImageIndex--;
      if ( diskImageIndex >= imagesOnDisk.length - 1 )
      {
        diskImageIndex = 0;
      }
      else if(diskImageIndex < 0)
        diskImageIndex = imagesOnDisk.length - 1;
      File currentImageFile = this.imagesOnDisk[diskImageIndex];
     
      try
      {
        BufferedImage image = ImageIO.read( currentImageFile );
        myImagePanel.setImage( image );

        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).
          addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).
          addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( "Sequence not available in File read mode", "MFE not currently available" );
        
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, ex.getMessage() );
      }

    }
    else
    {
      if ( currentDepth > 0 )
      {
        BufferedImage image = this.myImages.get( --currentDepth );


        myImagePanel.setImage( image );
        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( mySequences.get( currentDepth )[0], mySequences.get( currentDepth )[1] );

      }
      else
      {
        currentDepth = myImages.size() - 1;

        //NavigableImagePanel imagePanel = this.myImages.get( currentDepth );
        BufferedImage image = this.myImages.get( --currentDepth );

        myImagePanel.setImage( image );
        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( mySequences.get( currentDepth )[0], mySequences.get( currentDepth )[1] );

      }
    }
  }
private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
  openFile();
}//GEN-LAST:event_openMenuItemActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
  dispose();
}//GEN-LAST:event_exitMenuItemActionPerformed

private void saveSingleMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSingleMenuActionPerformed
  saveSingle();
}//GEN-LAST:event_saveSingleMenuActionPerformed

private void saveAllMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllMenuActionPerformed
  saveAll();
}//GEN-LAST:event_saveAllMenuActionPerformed

  private void generateButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateButtonActionPerformed
  {//GEN-HEADEREND:event_generateButtonActionPerformed

    Matcher m = actguFilter.matcher( this.hairpinSequenceTxt.getText() );
    if ( m.matches() )
    {
      generateFromGUI();
    }
    else
    {
      JOptionPane.showMessageDialog( null,
        "Missing hairpin sequence",
        "Missing required information",
        JOptionPane.ERROR_MESSAGE );

    }
  }//GEN-LAST:event_generateButtonActionPerformed

  private void saveCurrentButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveCurrentButtonActionPerformed
  {//GEN-HEADEREND:event_saveCurrentButtonActionPerformed
    saveSingle();
  }//GEN-LAST:event_saveCurrentButtonActionPerformed

  private void saveAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAllButtonActionPerformed
  {//GEN-HEADEREND:event_saveAllButtonActionPerformed
    saveAll();
  }//GEN-LAST:event_saveAllButtonActionPerformed

  private void forwardsClickActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_forwardsClickActionPerformed
  {//GEN-HEADEREND:event_forwardsClickActionPerformed
    SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {

          forwardsMove();
        }
      } );
  }//GEN-LAST:event_forwardsClickActionPerformed

  private void backwardsClickActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_backwardsClickActionPerformed
  {//GEN-HEADEREND:event_backwardsClickActionPerformed
    //System.out.println("current highest depth: " + imageLayerPane.highestLayer());
    SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {

          backwardsMove();
        }
      } );
  }//GEN-LAST:event_backwardsClickActionPerformed

  private void toggleViewOptionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleViewOptionsActionPerformed
  {//GEN-HEADEREND:event_toggleViewOptionsActionPerformed
    showingOptions = toggleViewOptions.isSelected();
    checkSequenceOptions();
  }//GEN-LAST:event_toggleViewOptionsActionPerformed

  private void forwardsMove()
  {
    if ( myImages.isEmpty() && !readingFromDisk )
    {
      return;
    }
    if ( readingFromDisk )
    {
      diskImageIndex++;
      if ( diskImageIndex >= imagesOnDisk.length - 1 )
      {
        diskImageIndex = 0;
      }
      else if(diskImageIndex < 0)
        diskImageIndex = imagesOnDisk.length - 1;
      File currentImageFile = this.imagesOnDisk[diskImageIndex];

      
      try
      {
        BufferedImage image = ImageIO.read( currentImageFile );
        myImagePanel.setImage( image );

        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).
          addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).
          addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( "Sequence not available in File read mode", "MFE not currently available" );
        
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, ex.getMessage() );
      }

      
    }
    else
    {
      if ( currentDepth <= myImages.size() - 2 )
      {
        //NavigableImagePanel imagePanel = this.myImages.get( ++currentDepth );
        BufferedImage image = this.myImages.get( ++currentDepth );

        myImagePanel.setImage( image );

        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( mySequences.get( currentDepth )[0], mySequences.get( currentDepth )[1] );

      }
      else
      {
        currentDepth = 0;

        //NavigableImagePanel imagePanel = this.myImages.get( currentDepth );
        BufferedImage image = this.myImages.get( currentDepth );
        myImagePanel.setImage( image );

        hairpinImagePanel.removeAll();

        javax.swing.GroupLayout hairpinImagePanelLayout = new javax.swing.GroupLayout( hairpinImagePanel );
        hairpinImagePanel.setLayout( hairpinImagePanelLayout );
        hairpinImagePanelLayout.setHorizontalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        hairpinImagePanelLayout.setVerticalGroup(
          hairpinImagePanelLayout.createParallelGroup( javax.swing.GroupLayout.Alignment.LEADING ).addComponent( myImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE ) );
        updateInformation( mySequences.get( currentDepth )[0], mySequences.get( currentDepth )[1] );

      }
    }
  }

  private void checkSequenceOptions()
  {
//    hairpinSequencePanel.setVisible( showingOptions );
//    shortSequencePanel.setVisible( showingOptions );

    renderOptionsPanel.setVisible( showingOptions);
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backwardsClick;
    private javax.swing.JPanel currentInformationPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu2;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JButton forwardsClick;
    private javax.swing.JButton generateButton;
    private javax.swing.JPanel hairpinImagePanel;
    private javax.swing.JPanel hairpinSequencePanel;
    private javax.swing.JTextPane hairpinSequenceTxt;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem loadHelp;
    private javax.swing.JLabel loadedImagesLabel;
    private javax.swing.JLabel loadedImagesMFE;
    private javax.swing.JToolBar mainControlToolBar;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar2;
    private javax.swing.JFormattedTextField miRNASequenceTxt;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel renderOptionsPanel;
    private javax.swing.JButton saveAllButton;
    private javax.swing.JMenuItem saveAllMenu;
    private javax.swing.JButton saveCurrentButton;
    private javax.swing.JMenu saveMenu;
    private javax.swing.JMenuItem saveSingleMenu;
    private javax.swing.JRadioButton seqColour1;
    private javax.swing.JRadioButton seqColour10;
    private javax.swing.JRadioButton seqColour11;
    private javax.swing.JRadioButton seqColour12;
    private javax.swing.JRadioButton seqColour13;
    private javax.swing.JRadioButton seqColour14;
    private javax.swing.JRadioButton seqColour2;
    private javax.swing.JRadioButton seqColour3;
    private javax.swing.JRadioButton seqColour4;
    private javax.swing.JRadioButton seqColour5;
    private javax.swing.JRadioButton seqColour6;
    private javax.swing.JRadioButton seqColour7;
    private javax.swing.JRadioButton seqColour8;
    private javax.swing.JRadioButton seqColour9;
    private javax.swing.ButtonGroup seqHighlightGroup;
    private javax.swing.JColorChooser sequenceColourChooser;
    private javax.swing.JPanel shortSequencePanel;
    private javax.swing.JCheckBox toggleViewOptions;
    // End of variables declaration//GEN-END:variables
  private boolean showingOptions = true;

  private void setShowingOptions( boolean newState )
  {
    showingOptions = newState;
    checkSequenceOptions();
  }

  public boolean openFile()
  {
    File inputFile = FileDialogUtils.showSingleFileOpenDialog( FileDialogUtils.FileExtFilter.FASTA.getFilter(), this );
    if ( inputFile != null )
    {
      try
      {

        BufferedReader br = FileUtils.createBufferedReader( inputFile );
        int nLines = Files.readLines( inputFile, Charset.defaultCharset(), new LineProcessor<Integer>()
        {
          int count = 0;

          @Override
          public Integer getResult()
          {
            return count;
          }

          @Override
          public boolean processLine( String line )
          {
            count++;
            return true;
          }
        } );
        renderAllHairpins(nLines, br);
      }
      catch ( IOException ex )
      {
        LOGGER.log(Level.SEVERE, "{0}RNA annotation read file error: ", ex.getMessage());
      }
    }
    return false;
    
  }
  private void renderAllHairpins(final int lineCount, final BufferedReader br) throws IOException
  {
   


    final HairpinGenerateMainFrame generator = new HairpinGenerateMainFrame(lineCount/2 );
    generator.setVisible( true);
    ( (MDIDesktopPane) getParent() ).add(generator);
    ( (MDIDesktopPane) getParent() ).activateFrame( generator );

    new Thread(
      new Runnable()
      {
        @Override
        public void run()
        {
          clearAllCurrentImages();
          boolean outputToDisc = false;
          File toUse = null;
          FileWriter outImageFile = null;
          PrintWriter outImageWriter = null;
          String selectedExtension[] =
          {
            ""
          };

          if ( lineCount >= MAX_ANNOTATION_SEQUENCES )
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
              LOGGER.log( Level.SEVERE, ex.getMessage() );
            }
            outImageWriter = new PrintWriter( outImageFile );
            outImageWriter.println( "File type selected: " + selectedExtension[0] );
            outImageWriter.println( "Total hairpins rendered: " + lineCount );
            outImageWriter.println( "-----------------------------" );
          }

          try
          {
            String line;
            int hairpinIndex = 0;
            while ( ( line = br.readLine() ) != null )
            {
              hairpinIndex++;
              if ( line.startsWith( ">" ) )
              {
                generator.tracker.increment();
                String[] split = line.split( "_" );
                if ( ( split.length > 1 ) && line.contains( "PRECURSOR-START") )
                {
                  //>maturemiRNA_TTGAGCCGTGCCAATATCACG_miRNA*_AGATATTAGTGCGGTTCAATC_1_PRECURSOR-START_3961364_END_39614536427
                  
                  String hairpinSequence = br.readLine();
                  ArrayList<int[]> shortReadIndexes = new ArrayList<int[]>();
                  ArrayList<String> shortReads = new ArrayList<String>();
                  shortReads.add( split[1] );
                  shortReads.add( split[3] );
                  ArrayList<String> generatedFromFile = generateFromFile(hairpinSequence, shortReadIndexes, shortReads);
                  if(generatedFromFile != null)
                  {
           
                    if ( !outputToDisc )
                    {
                      //generateAndAdd( generatedFromFile.get( 1 ), hairpinDotBracketHTML, shortReadIndexes, MFE );
                      generateAndAdd( hairpinSequence, generatedFromFile.get(0), shortReadIndexes, generatedFromFile.get(1) );
                    }
                    else
                    {

                      String fileName = FilenameUtils.removeExtension( toUse.getAbsolutePath() ) + DIR_SEPARATOR
                        + "HAIRPIN_" + hairpinIndex + "." + selectedExtension[0];
                      LOGGER.log( Level.FINE, "filename: {0}", fileName );
                      File updatedFileName = new File( fileName );
                      outImageWriter.println( "Image name:" + updatedFileName.getName() );
                      outImageWriter.println( "Hairpin Sequence:" + StringUtils.removeHTML( hairpinSequence ) );
                      outImageWriter.println( "-----------------------------" );
                      outImageWriter.println();

                      generateAndSave( hairpinSequence, generatedFromFile.get(0), shortReadIndexes, generatedFromFile.get(1),
                        updatedFileName, selectedExtension );

                    }
                  }
                }
                else
                {
                  String hairpinSequence = br.readLine();
                  
                  ArrayList<String> generatedFromFile = generateFromFile(hairpinSequence, new ArrayList<int[]>(), new ArrayList<String>());
                  if(generatedFromFile != null)
                  {
           
                    if ( !outputToDisc )
                    {
                      //generateAndAdd( generatedFromFile.get( 1 ), hairpinDotBracketHTML, shortReadIndexes, MFE );
                      generateAndAdd( hairpinSequence, generatedFromFile.get(0), new ArrayList<int[]>(), generatedFromFile.get(1) );
                    }
                    else
                    {

                      String fileName = FilenameUtils.removeExtension( toUse.getAbsolutePath() ) + DIR_SEPARATOR
                        + "HAIRPIN_" + hairpinIndex + "." + selectedExtension[0];
                      LOGGER.log( Level.FINE, "filename: {0}", fileName );
                      File updatedFileName = new File( fileName );
                      outImageWriter.println( "Image name:" + updatedFileName.getName() );
                      outImageWriter.println( "Hairpin Sequence:" + StringUtils.removeHTML( hairpinSequence ) );
                      outImageWriter.println( "-----------------------------" );
                      outImageWriter.println();

                      generateAndSave( hairpinSequence, generatedFromFile.get(0), new ArrayList<int[]>(), generatedFromFile.get(1),
                        updatedFileName, selectedExtension );

                    }
                  }
                }
              }
            }

          }
          catch ( IOException ex )
          {
            LOGGER.log( Level.SEVERE, ex.getMessage() );
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
         

          if ( outputToDisc )
          {
            openImagesFromDisc( new File( FilenameUtils.removeExtension( toUse.getAbsolutePath() ) ), selectedExtension[0] );
          }
         

        }
      } ).start();

    //
  }

  @Override
  public void runProcedure()
  {
    throw new UnsupportedOperationException( "Not supported yet." );
  }

  @Override
  public String toString()
  {
    return "RNAannotationMainFrame";
  }

  @Override
  public JPanel getParamsPanel()
  {
    throw null;
  }

  @Override
  public void setShowingParams( boolean newState )
  {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean getShowingParams()
  {
    return false;
  }
}
