/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;

import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.io.FastaFileReader;
import uk.ac.uea.cmp.srnaworkbench.swing.RenderData;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.tools.tasi.Locus.VissrData;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.GFFRecordGlyph;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.ThreadUtils;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.StandardCellRenderer;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.NumberUtils;
/**
 * 
 * @author Matthew Stocks and Dan Mapleson
 */
final class TasiResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  
  private List<Locus> results;
  private TasiParams params;
  private File genomeFile;
  
  /** 
   * Creates new Results panel 
   */
  public TasiResultsPanel()
  {
    initComponents();
    
    resetTable();
  }

  public void resetTable()
  {
    populateResults( new ArrayList<Locus>(), new TasiParams(), null);
  }

  /**
   * Update the results table with the provided tas data
   * @param tasis 
   */
  public void populateResults( List<Locus> predicted_tas_loci, TasiParams params, File genomeFile )
  {
    this.results = predicted_tas_loci;
    this.params = params;
    
    JTree tree = generateTreeTable();
    
    this.outline = generateResultsTable( tree );

    this.genomeFile = genomeFile;

    updateResults( );
  }

  /**
   * Generate the table containing the results from the tasi tool run
   * @param tasis Results
   * @return The tree table for use in the GUI
   */
  private JTree generateTreeTable()
  {
    if ( this.results == null )
    {
      resetTable();
      return null;
    }

    ArrayList<String> root_list = CollectionUtils.newArrayList();
    root_list.add( "root" );
    OutlineNode root_node = new OutlineNode( root_list );

    int counter = 1;
    for ( Locus locus : this.results )
    {
      ArrayList<String> tas_list = new ArrayList<String>();
      tas_list.add( Integer.toString( counter ));
      tas_list.add( locus.getChromosome() );
      tas_list.add( Integer.toString( locus.getStart() ) );
      tas_list.add( Integer.toString( locus.getEnd() ) );
      tas_list.add( Integer.toString( locus.getNbSrnas() ) );
      tas_list.add( Integer.toString( locus.getNbPhasedSrnas() ) );
      tas_list.add( Double.toString( NumberUtils.roundToSignificantFigures(locus.getPValue(), 4 ) ) );

      OutlineNode child = new OutlineNode( tas_list );

      root_node.add( child );
      counter++;
    }

    return new JTree( root_node );
  }

  private Outline generateResultsTable( JTree results )
  {
    Outline o = newOutline();
    
    if ( results == null )
    {
      resetTable();
      return o;
    }
    
    TreeModel tm = results.getModel();
    TasRowModel mrm = TasRowModel.newTasiRowModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "Locus ID" );

    o.setModel( mdl );
    
    MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
    Enumeration e = o.getColumnModel().getColumns();
    while ( e.hasMoreElements() )
    {
      TableColumn mod = ( (TableColumn) e.nextElement() );
      mod.setHeaderRenderer( renderer );
      mod.setPreferredWidth( 100 );
    }
    
    return o;
  }
  
  private Outline newOutline()
  {
    Outline o = new Outline();
    o.setRenderDataProvider( new RenderData() );
    o.setDefaultRenderer( String.class, new StandardCellRenderer() );
    o.setRootVisible( false );
    o.setBackground( new java.awt.Color( 120, 120, 120 ) );
    o.setForeground( new java.awt.Color( 255, 255, 255 ) );
    o.setFillsViewportHeight( true );
    o.setRowHeight( 30 );
    o.setGridColor( new java.awt.Color( 153, 204, 255 ) );
    o.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
    o.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
    o.setCellSelectionEnabled( true );
    o.setRowSelectionAllowed( true );
    o.setColumnSelectionAllowed( false );
    o.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS );
    o.add( popupMenu );
    o.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            table_scroll_paneMouseClicked(evt);
        }
      @Override
      public void mousePressed( java.awt.event.MouseEvent evt )
      {
       
        table_scroll_paneMousePressed( evt );
      }
      @Override
        public void mouseReleased(java.awt.event.MouseEvent evt) {
            table_scroll_paneMouseReleased(evt);
        }
     
    } );
    o.addKeyListener( new java.awt.event.KeyListener() {
      @Override
      public void keyTyped( KeyEvent e )
      {
        keyUpdate(e);
      }

      @Override
      public void keyPressed( KeyEvent e )
      {
        keyUpdate(e);
      }

      @Override
      public void keyReleased( KeyEvent e )
      {
        keyUpdate(e);
      }
    });
    
    return o;
  }
  
  private void keyUpdate( KeyEvent e )
  {
    int id = e.getID();
    
    if ( id == KeyEvent.KEY_RELEASED )
    {
      int c = e.getKeyCode();
      int mod = e.getModifiersEx();
      
      if ( c == KeyEvent.VK_C && mod == KeyEvent.CTRL_DOWN_MASK )
      {
        copySelectedLocus();
      }
    }
  }
  
  private void copySelectedLocus()
  {
    int r = this.outline.getSelectedRow();
    
    String copyText = this.results.get( r ).toString();

    StringSelection ss = new StringSelection( copyText );

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( ss, null );
  }

  private void updateResults()
  {
    table_scroll_pane.setViewportView( this.outline );
  }
  
  private void sendToVisSR()
  {
    int r = this.outline.getSelectedRow();
    File genome = this.params.getGenome();
    
    if ( genome == null )
    {
      JOptionPane.showMessageDialog( this, "Genome file not found.  Can't send locus to VisSR", "Tasi Tool Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    
    Locus l = this.results.get( r );
    VissrData vd = l.buildVissrData();
    
    // Create tiers (+ tasis, TAS locus, -tasis)
    TierParameters tpPosPhased = new TierParameters.Builder( "+ve phased sRNAs" ).build();
    TierParameters tpTAS = new TierParameters.Builder( "Loci" ).glyphBackgroundColour( Color.darkGray ).glyphClass( GFFRecordGlyph.class ).build();
    TierParameters tpNegPhased = new TierParameters.Builder( "-ve phased sRNAs" ).build();
    TierParameters tpAll = new TierParameters.Builder( "All sRNAs in Locus" ).build();
    TierParameters tpUnphased = new TierParameters.Builder( "Unphased sRNAs in Locus" ).build();

    Patman posPhasedItems = vd.getPosPhasedItems();
    Patman negPhasedItems = vd.getNegPhasedItems();
    Patman allItems = vd.getAllItems();
    Patman unphasedItems = vd.getUnphasedItems();
    
    int max = allItems.getMaxAbundance();
    
    posPhasedItems.setGlyphHeights( max );
    negPhasedItems.setGlyphHeights( max );
    unphasedItems.setGlyphHeights( max );
    
    allItems.setGlyphHeightFromAbundance();
    
    tpPosPhased.addList( posPhasedItems );
    tpTAS.addList( vd.getLocusItems() );
    tpNegPhased.addList( negPhasedItems );
    tpAll.addList( allItems );
    tpUnphased.addList( unphasedItems );
    
    
    // Show VisSR...
    setBusyCursor(true);
    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( genome, false, tpPosPhased, tpTAS, tpNegPhased, tpUnphased, tpAll );
    setBusyCursor(false);
    // Focus on the region of interest
    vissr.displaySequenceRegion( l.getChromosome().trim(), l.getStart() - 5, l.getEnd() + 5 );    
  }
  private void setBusyCursor( boolean setBusy )
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
    for ( Component cpt : this.getComponents() )
    {
      cpt.setCursor( c );
    }



    // To let the cursor update on the screen
    ThreadUtils.safeSleep( 10 );
  }
  private void showPhased()
  {
    int r = this.outline.getSelectedRow();

//    if ( !outline.isRowSelected( r ) )
//    {
//      outline.changeSelection( r, 0, false, false );
      Locus l = this.results.get( r );

      JFrame f = new JFrame();
      PhasedResultsPanel prp = new PhasedResultsPanel();
      prp.populateResults( l.getPhasedSrnas() );
      JOptionPane editPane = new JOptionPane( prp );
      editPane.setFont( new Font( "SansSerif", Font.PLAIN, 8 ) );
      JDialog dialog = editPane.createDialog( f, "Phased sRNAs for Locus: " + ( r + 1 ) );
      f.pack();
      f.setLocationRelativeTo( this );
      dialog.setVisible( true );

   // }

    
  }
  
  private void checkPopup( java.awt.event.MouseEvent evt )
  {
    if ( evt.isPopupTrigger() )
    {
      int row = outline.rowAtPoint( evt.getPoint() );
      if ( !outline.isRowSelected( row ) )
      {
        outline.changeSelection( row, 0, false, false );

      }
//        double x = getMousePosition().getX();
//        double y = getMousePosition().getY();
      popupMenu.show( table_scroll_pane, evt.getX(), evt.getY() );
    }
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

        popupMenu = new javax.swing.JPopupMenu();
        mnuCopy = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuPhasedSrnas = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuVissr = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        exportLocusToFasta = new javax.swing.JMenuItem();
        exportIndividualSequences = new javax.swing.JMenuItem();
        table_scroll_pane = new javax.swing.JScrollPane();

        mnuCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/copy-item.png"))); // NOI18N
        mnuCopy.setText("Copy to Clipboard");
        mnuCopy.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuCopyActionPerformed(evt);
            }
        });
        popupMenu.add(mnuCopy);
        popupMenu.add(jSeparator1);

        mnuPhasedSrnas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/connections.png"))); // NOI18N
        mnuPhasedSrnas.setText("Show Phased sRNAs");
        mnuPhasedSrnas.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuPhasedSrnasActionPerformed(evt);
            }
        });
        popupMenu.add(mnuPhasedSrnas);
        popupMenu.add(jSeparator2);

        mnuVissr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        mnuVissr.setText("Show Genome View");
        mnuVissr.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuVissrActionPerformed(evt);
            }
        });
        popupMenu.add(mnuVissr);
        popupMenu.add(jSeparator3);

        exportLocusToFasta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        exportLocusToFasta.setText("Export Locus to FASTA");
        exportLocusToFasta.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportLocusToFastaActionPerformed(evt);
            }
        });
        popupMenu.add(exportLocusToFasta);

        exportIndividualSequences.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        exportIndividualSequences.setText("Export Individual Sequences to FASTA");
        exportIndividualSequences.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportIndividualSequencesActionPerformed(evt);
            }
        });
        popupMenu.add(exportIndividualSequences);

        setBackground(new java.awt.Color(120, 120, 120));
        setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        table_scroll_pane.setBackground(new java.awt.Color(120, 120, 120));
        table_scroll_pane.setComponentPopupMenu(popupMenu);
        table_scroll_pane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        table_scroll_pane.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        table_scroll_pane.setPreferredSize(new java.awt.Dimension(4, 100));
        table_scroll_pane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 697, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

  private void table_scroll_paneMouseClicked( java.awt.event.MouseEvent evt )//GEN-FIRST:event_table_scroll_paneMouseClicked
  {//GEN-HEADEREND:event_table_scroll_paneMouseClicked
    // If double click select the row
    // if single click, let user select characters in the cell.
    if (evt.getClickCount() == 2 && !evt.isConsumed())
    {
      evt.consume();
      
      // Highlight the row
      //JOptionPane.showMessageDialog( this, "Double click");
    }
    else if (evt.getClickCount() == 1)
    {
      // Highlight the cell
      
    }
  }//GEN-LAST:event_table_scroll_paneMouseClicked

  private void table_scroll_paneMousePressed( java.awt.event.MouseEvent evt )//GEN-FIRST:event_table_scroll_paneMousePressed
  {//GEN-HEADEREND:event_table_scroll_paneMousePressed
    checkPopup(evt);
  }//GEN-LAST:event_table_scroll_paneMousePressed

  private void table_scroll_paneMouseReleased( java.awt.event.MouseEvent evt )//GEN-FIRST:event_table_scroll_paneMouseReleased
  {//GEN-HEADEREND:event_table_scroll_paneMouseReleased
    checkPopup(evt);
  }//GEN-LAST:event_table_scroll_paneMouseReleased

  private void mnuVissrActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuVissrActionPerformed
  {//GEN-HEADEREND:event_mnuVissrActionPerformed
    // Identify currently selected row and submit GFF to VisSR... also auto take to that location
    sendToVisSR();
  }//GEN-LAST:event_mnuVissrActionPerformed

  private void mnuPhasedSrnasActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuPhasedSrnasActionPerformed
  {//GEN-HEADEREND:event_mnuPhasedSrnasActionPerformed
    // Create a dialog containing phased sRNAs, their abundance and location
    showPhased();
  }//GEN-LAST:event_mnuPhasedSrnasActionPerformed

  private void mnuCopyActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuCopyActionPerformed
  {//GEN-HEADEREND:event_mnuCopyActionPerformed
    copySelectedLocus();
  }//GEN-LAST:event_mnuCopyActionPerformed

  private void exportIndividualSequencesActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_exportIndividualSequencesActionPerformed
  {//GEN-HEADEREND:event_exportIndividualSequencesActionPerformed
    int[] rows = this.outline.getSelectedRows();

    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
    if ( outputFile != null )
    {
      exportIndividualSequences( outputFile, rows );
    }
  }//GEN-LAST:event_exportIndividualSequencesActionPerformed

  private void exportLocusToFastaActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_exportLocusToFastaActionPerformed
  {//GEN-HEADEREND:event_exportLocusToFastaActionPerformed
    int[] rows = this.outline.getSelectedRows();

    File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
    if ( outputFile != null )
    {
      exportEntireLocus( outputFile, rows );
    }
  }//GEN-LAST:event_exportLocusToFastaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem exportIndividualSequences;
    private javax.swing.JMenuItem exportLocusToFasta;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem mnuCopy;
    private javax.swing.JMenuItem mnuPhasedSrnas;
    private javax.swing.JMenuItem mnuVissr;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane table_scroll_pane;
    // End of variables declaration//GEN-END:variables

  public void exportEntireLocus(File outputFile, int[] rows)
  {
    if(this.genomeFile == null)
    {
      LOGGER.log( Level.WARNING, null, "No genome file found during TAS export" );
      return;
    }
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, false );
      outFASTAFile.write( "");
      outFASTAFile.close();
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      FastaFileReader genReader = new FastaFileReader( this.genomeFile );

      boolean success = genReader.processFile();
      if ( !success )
      {
        throw new WorkbenchException( "Failed to load genome file." );
      }
      FastaMap fastaMap = genReader.getFastaMap( true );
      for ( int row : rows )
      {
        
        Locus l = this.results.get( row );
        FastaRecord get = fastaMap.get( l.getChromosome() );
        out.println(">" + l.getChromosome() + "-Start:" +l.getStart() + "-End:" + l.getEnd() );
        out.println(get.getSubSequenceAsString( l.getStart(), l.getEnd(), SequenceStrand.POSITIVE ));
      }
      

      
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    catch ( WorkbenchException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outFASTAFile.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }
  public void exportIndividualSequences( File outputFile, int[] rows )
  {
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, false );
      outFASTAFile.write( "");
      outFASTAFile.close();
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      for ( int row : rows )
      {
        Locus l = this.results.get( row );

        Patman allSrnas = l.getAllSrnas();
        Patman phasedSrnas = l.getPhasedSrnas();

        for ( PatmanEntry pe : allSrnas )
        {
          String header = "";
          if(phasedSrnas.contains( pe) )
          {
            header = ">PHASED_Chromo:" + pe.getLongReadHeader() + "-Start:" + pe.getStart() + "--End:" + pe.getEnd() + 
            "-Strand:" + pe.getSequenceStrand() + "-Abund(" + pe.getAbundance()+")";
          }
          else
          {
            header = ">UNPHASED_Chromo:" + pe.getLongReadHeader() + "-Start:" + pe.getStart() + "--End:" + pe.getEnd() + 
            "-Strand:" + pe.getSequenceStrand() + "-Abund(" + pe.getAbundance()+")";
          }
          out.println(header);
          out.println(pe.getSequence());
          
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
        outFASTAFile.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }

  public int[] getAllRowCount()
  {
    int[] returnMe = new int[outline.getRowCount()];
    for(int i = 0; i < returnMe.length; i++ )
    {
      returnMe[i] = i;
      
    }
    return returnMe;
  }
}
