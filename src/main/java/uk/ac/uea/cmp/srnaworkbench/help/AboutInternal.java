/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AboutInternal.java
 *
 * Created on 23-Aug-2011, 10:52:38
 */
package uk.ac.uea.cmp.srnaworkbench.help;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class AboutInternal extends javax.swing.JInternalFrame implements GUIInterface
{
  private static AboutInternal instance = null;

  /** Creates new form AboutInternal */
  private AboutInternal()
  {
    initComponents();
    Icon icon = Tools.createImageIcon( "/uk/ac/uea/cmp/srnaworkbench/images/srNAWorkbench_1_1_1.gif", "sRNA Workbench image" );//new ImageIcon(url);

    JLabel label = new JLabel( icon );
    this.jScrollPane1.setViewportView( label );

    String buildDetails = getBuildDetails();

    Tools.addStylesToDocument( versionInformation.getStyledDocument() );
//
    StyledDocument doc = versionInformation.getStyledDocument();
    try
    {
      doc.insertString( doc.getLength(), ( "Product Version: " ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( buildDetails + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );

      doc.insertString( doc.getLength(), ( "Contact Address: " ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( "sRNAWorkbench@uea.ac.uk" + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );
      doc.insertString( doc.getLength(), ( "People: " + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( 
          " - Prof. Vincent Moulton" + LINE_SEPARATOR
        + " - Dr. Tamas Dalmay" + LINE_SEPARATOR
        + " - Dr. Matthew B Stocks" + LINE_SEPARATOR
        + " - Dr. Irina Mohorianu"
        + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );

      doc.insertString( doc.getLength(), ( "Acknowledgements: " + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( 
          " - Dr. Simon Moxon" + LINE_SEPARATOR
        + " - Dr. Frank Schwach" + LINE_SEPARATOR
        + " - Prof. David Baulcombe" + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );

      doc.insertString( doc.getLength(), ( "Dependencies: " ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( LINE_SEPARATOR
        + " - Vienna Package (RNAfold/RNAplot): http://www.tbi.univie.ac.at/RNA/" + LINE_SEPARATOR
        + " - Randfold: http://bioinformatics.psb.ugent.be/software/details/Randfold" + LINE_SEPARATOR
        + " - PatMaN: http://bioinf.eva.mpg.de/patman/" + LINE_SEPARATOR
        + " - Fasta: http://www.ebi.ac.uk/Tools/fasta/index.html" + LINE_SEPARATOR
        + " - Genoviz: http://genoviz.sourceforge.net" + LINE_SEPARATOR
        + " - iText: http://itextpdf.com" + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );

      doc.insertString( doc.getLength(), ( "Grants: " ), doc.getStyle( Tools.initStyles[2] ) );
      doc.insertString( doc.getLength(), ( "Biotechnology and Biological Sciences Research Council (grant BB/100016X/1)"
        + LINE_SEPARATOR ), doc.getStyle( Tools.initStyles[0] ) );

      versionInformation.setCaretPosition( 0 );
    }
    catch ( BadLocationException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
  }

  private String getBuildDetails()
  {
    // For reference...
    //            Name: sRNA Workbench
    //            Specification-Title: sRNA Workbench
    //            Specification-Version: 1.0
    //            Specification-Vendor: UEA CMP Bioinformatics Group
    //            Implementation-Title: sRNA Workbench
    //            Implementation-Version: release1
    //            Implementation-Vendor: UEA CMP Bioinformatics Group

    String productName = "sRNA Workbench";
    String productVersion = "<dev. version>";
    String buildNumber = "<build number>";

    String classPath = AboutInternal.class.getResource( AboutInternal.class.getSimpleName() + ".class" ).toString();

    if ( classPath.startsWith( "jar" ) )
    {
      String manifestPath = classPath.substring( 0, classPath.lastIndexOf( "!" ) + 1 ) + "/META-INF/MANIFEST.MF";

      Manifest manifest = null;

      try
      {
        manifest = new Manifest( new URL( manifestPath ).openStream() );
      }
      catch ( Exception ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }

      if ( manifest != null )
      {
        Map<String, Attributes> attrmap = manifest.getEntries();

        Attributes appAttr = null;

        if ( attrmap != null )
        {
          appAttr = attrmap.get( "sRNA Workbench" ); // Maps to "Name" in the manifest
        }

        if ( appAttr != null )
        {
          productName = appAttr.getValue( "Specification-Title" );
          productVersion = appAttr.getValue( "Specification-Version" );
          buildNumber = appAttr.getValue( "Implementation-Version" );
        }
      }
    }

    return StringUtils.nullSafeConcatenation( productName, " ", productVersion, " (", buildNumber, ")" );
  }

  public static synchronized AboutInternal getInstance()
  {
    if ( instance == null )
    {
      instance = new AboutInternal();
    }
    return instance;
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        homepageLink = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        versionInformation = new javax.swing.JTextPane();

        setClosable(true);
        setTitle("About the sRNA Workbench");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/workbenchSmall.jpg"))); // NOI18N

        jPanel1.setBackground(new java.awt.Color(120, 120, 120));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        homepageLink.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        homepageLink.setForeground(new java.awt.Color(255, 153, 102));
        homepageLink.setText("sRNA Workbench Homepage");
        homepageLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homepageLink.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                homepageLinkMouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("The UEA sRNA Workbench is software designed for the analysis of small RNA (sRNA) ");

        jLabel3.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("data sets. For more information please visit:  ");

        versionInformation.setContentType("text/HTML");
        versionInformation.setEditable(false);
        versionInformation.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        jScrollPane2.setViewportView(versionInformation);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel2)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(homepageLink))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(homepageLink)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void homepageLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_homepageLinkMouseClicked
  String url = "http://srna-workbench.cmp.uea.ac.uk/";
  try
  {
    java.awt.Desktop.getDesktop().browse( java.net.URI.create( url ) );
  }
  catch ( IOException ex )
  {
    LOGGER.log( Level.SEVERE, null, ex );
  }

}//GEN-LAST:event_homepageLinkMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel homepageLink;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane versionInformation;
    // End of variables declaration//GEN-END:variables

  @Override
  public void runProcedure()
  {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public JPanel getParamsPanel()
  {
    //throw new UnsupportedOperationException("Not supported yet.");
    return null;
  }

  @Override
  public void setShowingParams( boolean newState )
  {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean getShowingParams()
  {
    //throw new UnsupportedOperationException("Not supported yet.");
    return false;
  }

  @Override
  public void shutdown()
  {
   
  }
}
