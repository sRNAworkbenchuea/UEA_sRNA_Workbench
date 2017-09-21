/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.siloco;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.CoLIDERenderDataProvider;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.io.FastaFileReader;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.LocusUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 *
 * @author w0445959
 */
public class SiLoCoResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  private File genome;
  private SparseExpressionMatrix myExpressionMatrix;
  private HashMap<String, ArrayList<Patman>> cluster_per_chrom;

  /**
   * Creates new form SiLoCoResultsPanel
   */
  public SiLoCoResultsPanel()
  {
    initComponents();
  }

  public void display( 
                       HashMap<String, ArrayList<ArrayList<String>>> data,
                       SparseExpressionMatrix expressionMatrix,
                       HashMap<String, ArrayList<Patman>> cluster_per_chrom)
  {

    this.genome = expressionMatrix.getGenomeFile();
    this.myExpressionMatrix = expressionMatrix;
    this.cluster_per_chrom = cluster_per_chrom;

    SiLoCoRowModel mrm = new SiLoCoRowModel( expressionMatrix.getFileNames() );

    this.setName( "SiLoCoResultsPanel" );
    JTree tree = generateTreeTable( data );
    tree.setBackground( Color.WHITE );

    this.outline = generateResultsTable( tree, mrm );



    updateTable();


  }

  private Outline generateResultsTable( JTree results, SiLoCoRowModel mrm )
  {
    Outline o = newOutline();

    final TreeModel tm = results.getModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "ID" );


    o.setModel( mdl );

    MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
    Enumeration e = o.getColumnModel().getColumns();
    while ( e.hasMoreElements() )
    {
      TableColumn mod = ( (TableColumn) e.nextElement() );
      mod.setHeaderRenderer( renderer );
      mod.setPreferredWidth( 250 );
    }
    //o.removeColumn( o.getColumn( 7 ) );


    return o;
  }

  private void updateTable()
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        resultsScroll.setViewportView( outline );
      }
    } );
  }

  private Outline newOutline()
  {
    Outline o = new Outline();
    o.setRenderDataProvider( new CoLIDERenderDataProvider() );
    o.setDefaultRenderer( String.class, new CoLIDERenderDataProvider() );
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
    //o.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_OFF );
    o.addMouseListener( new java.awt.event.MouseAdapter()
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
    o.addKeyListener( new java.awt.event.KeyListener()
    {
      @Override
      public void keyTyped( KeyEvent e )
      {
        keyUpdate( e );
      }

      @Override
      public void keyPressed( KeyEvent e )
      {
        keyUpdate( e );
      }

      @Override
      public void keyReleased( KeyEvent e )
      {
        keyUpdate( e );
      }
    } );

    return o;
  }

  private void copySelectedRow()
  {
    //TODO Need to work out how to get the selected row into the clipboard
    // navigating the organismGroup structure keyed from row index is hard because
    // the user can expand and collapse nodes in the tree.  Maybe need to just copy
    // the text in the row rather than try to get the actual object again
    int r = this.outline.getSelectedRow();

    StringBuilder sb = new StringBuilder();

    for ( int i = 0; i < this.outline.getColumnCount(); i++ )
    {
      String s = this.outline.getModel().getValueAt( r, i ).toString().trim();
      sb.append( s );
      sb.append( "\t" );
    }

    String copyText = sb.toString().trim();

    StringSelection ss = new StringSelection( copyText );

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( ss, null );
  }

  public boolean clearAll()
  {
    if ( this.outline != null && this.outline.getRowCount() > 0 )
    {
      int result = JOptionPane.showConfirmDialog( this,
        "Information found in table will be cleared." + LINE_SEPARATOR + "Do you wish to continue?",
        "Starting new run",
        JOptionPane.WARNING_MESSAGE );
      if ( result == JOptionPane.YES_OPTION )
      {


        outline.removeAll();//.getModel();
 
        return true;
      }
      else
      {
        return false;
      }
    }
    return true;
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
        copySelectedRow();
      }
    }
  }

  private void checkPopup( java.awt.event.MouseEvent evt )
  {
    if ( evt.isPopupTrigger() )
    {
     this.popupMenu.show( this.outline, evt.getX(), evt.getY() );
      Point mousePoint = evt.getPoint();
      this.popupMenu.show( this.outline, evt.getX(), evt.getY());
      int row = this.outline.rowAtPoint( mousePoint );
      int column = outline.columnAtPoint( mousePoint );
      if ( !outline.isRowSelected( row ) )
      {
        outline.changeSelection( row, column, false, false );
        
      }
    }
  }

  /**
   * Generate the table containing the results from the CoLIDE run
   *
   * @param chromoToLocus the data as a map, Chromosome -> ArrayList of loci
   * (each locus is another array list of expression elements)
   * @return The tree table for use in the GUI
   */
  private JTree generateTreeTable( HashMap<String, ArrayList<ArrayList<String>>> data )
  {

    if ( data == null )
    {
      return new JTree( new OutlineNode( new ArrayList<String>() ) );
    }



//
    ArrayList<String> rootList = new ArrayList<String>();
    rootList.add( "root" );
    OutlineNode rootNode = new OutlineNode( rootList );
    for(Entry<String, ArrayList<ArrayList<String>>> entry : 
     new TreeMap<String, ArrayList<ArrayList<String>>>(data).entrySet())
    {
      ArrayList<String> newChromo = new  ArrayList<String>();
      newChromo.add( "Chromosome: " + entry.getKey());
      OutlineNode child1 = new OutlineNode(  newChromo );
      
      for(ArrayList<String> rowData : entry.getValue())
      {
        rowData.add( 0, "");
        OutlineNode child2 = new OutlineNode( rowData );
        child1.add( child2 );
      }
      rootNode.add( child1 );
      
    }
    return new JTree( rootNode );
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
        outputIndividualFastaMenu = new javax.swing.JMenuItem();
        outputEntireLocusFasta = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        renderSingleLocus = new javax.swing.JMenuItem();
        zoomToAggregate = new javax.swing.JMenuItem();
        resultsScroll = new javax.swing.JScrollPane();

        outputIndividualFastaMenu.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        outputIndividualFastaMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        outputIndividualFastaMenu.setText(org.openide.util.NbBundle.getMessage(SiLoCoResultsPanel.class, "SiLoCoResultsPanel.outputIndividualFastaMenu.text")); // NOI18N
        outputIndividualFastaMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputIndividualFastaMenuActionPerformed(evt);
            }
        });
        popupMenu.add(outputIndividualFastaMenu);

        outputEntireLocusFasta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/save.png"))); // NOI18N
        outputEntireLocusFasta.setText(org.openide.util.NbBundle.getMessage(SiLoCoResultsPanel.class, "SiLoCoResultsPanel.outputEntireLocusFasta.text")); // NOI18N
        outputEntireLocusFasta.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                outputEntireLocusFastaActionPerformed(evt);
            }
        });
        popupMenu.add(outputEntireLocusFasta);
        popupMenu.add(jSeparator1);

        renderSingleLocus.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        renderSingleLocus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        renderSingleLocus.setText(org.openide.util.NbBundle.getMessage(SiLoCoResultsPanel.class, "SiLoCoResultsPanel.renderSingleLocus.text")); // NOI18N
        renderSingleLocus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                renderSingleLocusActionPerformed(evt);
            }
        });
        popupMenu.add(renderSingleLocus);

        zoomToAggregate.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        zoomToAggregate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        zoomToAggregate.setText(org.openide.util.NbBundle.getMessage(SiLoCoResultsPanel.class, "SiLoCoResultsPanel.zoomToAggregate.text")); // NOI18N
        zoomToAggregate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                zoomToAggregateActionPerformed(evt);
            }
        });
        popupMenu.add(zoomToAggregate);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

  private void outputIndividualFastaMenuActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_outputIndividualFastaMenuActionPerformed
  {//GEN-HEADEREND:event_outputIndividualFastaMenuActionPerformed
    //    JTable source = (JTable) evt.getSource();
    if ( outline.getSelectedRowCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "Output not possible", "No selected loci...", JOptionPane.INFORMATION_MESSAGE );
    }
    else
    {
      File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
      if ( outputFile != null )
      {
        int[] selectedRows = outline.getSelectedRows();

        for ( int row : selectedRows )
        {
          row = outline.convertRowIndexToModel( row );

          String chromo = outline.getValueAt( row, 1 ).toString();
          int startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
          int endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 3 ).toString(), 0 );

          exportLocusToFasta( outputFile, chromo, startCoord, endCoord );
        }
      }
    }
  }//GEN-LAST:event_outputIndividualFastaMenuActionPerformed

  private void outputEntireLocusFastaActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_outputEntireLocusFastaActionPerformed
  {//GEN-HEADEREND:event_outputEntireLocusFastaActionPerformed
    if ( outline.getSelectedRowCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "Output not possible", "No selected loci...", JOptionPane.INFORMATION_MESSAGE );
    }
    else
    {
      File outputFile = FileDialogUtils.showFileSaveDialog( this, FileDialogUtils.FileExtFilter.FASTA.getFilter() );
      if ( outputFile != null )
      {
        int[] selectedRows = outline.getSelectedRows();

        FastaFileReader genReader = new FastaFileReader( genome );

        boolean success = genReader.processFile();
        if ( !success )
        {
          LOGGER.log( Level.WARNING, "Failed to load genome file. No Sequences output" );
        }
        else
        {
          FastaMap fastaMap = genReader.getFastaMap( true );

          for ( int row : selectedRows )
          {
            row = outline.convertRowIndexToModel( row );

            String chromo = outline.getValueAt( row, 1).toString();
            int startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
            int endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 3 ).toString(), 0 );

            exportEntireLocusToFasta( outputFile, chromo, startCoord, endCoord, fastaMap );
          }
        }
      }
    }
  }//GEN-LAST:event_outputEntireLocusFastaActionPerformed

  private void renderSingleLocusActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_renderSingleLocusActionPerformed
  {//GEN-HEADEREND:event_renderSingleLocusActionPerformed
    generateVissrArrow(false);
  }//GEN-LAST:event_renderSingleLocusActionPerformed

  private void zoomToAggregateActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_zoomToAggregateActionPerformed
  {//GEN-HEADEREND:event_zoomToAggregateActionPerformed
    generateVissrAggregate( false );

  }//GEN-LAST:event_zoomToAggregateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem outputEntireLocusFasta;
    private javax.swing.JMenuItem outputIndividualFastaMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem renderSingleLocus;
    private javax.swing.JScrollPane resultsScroll;
    private javax.swing.JMenuItem zoomToAggregate;
    // End of variables declaration//GEN-END:variables


  private void exportLocusToFasta( File outputFile, String chromo, int startCoord, int endCoord )
  {
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      ArrayList<Patman> lociForChromo = this.cluster_per_chrom.get( chromo);
      ArrayList<Patman> lociInRange = LocusUtils.getPatmanLociInRange( lociForChromo, startCoord, endCoord);
      
      for(Patman locus : lociInRange)
      {
        int currentMinCoord = Integer.MAX_VALUE;
        int currentMaxCoord = Integer.MIN_VALUE;

        for ( PatmanEntry element : locus )
        {

          if ( element.getStart() < currentMinCoord )
          {
            currentMinCoord = element.getStart();
          }
          if ( element.getEnd() > currentMaxCoord )
          {
            currentMaxCoord = element.getEnd();
          }
        }
        if ( currentMinCoord >= startCoord && currentMaxCoord <= endCoord )
        {
          for ( PatmanEntry ele : locus )
          {
            
              out.println( ">Chromosome-" + ele.getShortReadHeader() + "-Start-" + ele.getStart() + "-Stop-" + ele.getEnd() + "-strand-" + 
                ele.getSequenceStrand()  );
              out.println( ele.getSequence() );
          }
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

   public void generateVissrArrow(boolean completeModel)
  {
    int row = -1;// = outline.convertRowIndexToModel( outline.getSelectedRow() );

    row = outline.convertRowIndexToModel( outline.getSelectedRow() );

    int lowRange = 0;
    int highRange = 0;
    String chromoID = "";
    HashMap<String, Patman> locusItems = new HashMap<String, Patman>();

    double totalAbundance = 0.0;
    chromoID = outline.getValueAt( row, 1 ).toString();
    //lowRange = source.getValueAt( row,  )
    lowRange = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
    highRange = StringUtils.safeIntegerParse( outline.getValueAt( row, 3 ).toString(), 0 );

    ArrayList<Patman> lociForChromo = cluster_per_chrom.get( chromoID );
    
    ArrayList<Patman> lociInRange = LocusUtils.getPatmanLociInRange( lociForChromo, lowRange, highRange );
    //int sampleNumber = 0;
    for ( ArrayList<String> fileNames : this.myExpressionMatrix.getFileNames() )
    {
      if ( fileNames.size() == 1 )
      {
        locusItems.put( fileNames.get( 0 ), new Patman() );
      }
    }
    for ( Patman locus : lociInRange )
    {
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;

      for ( PatmanEntry element : locus )
      {

        if ( element.getStart() < currentMinCoord )
        {
          currentMinCoord = element.getStart();
        }
        if ( element.getEnd() > currentMaxCoord )
        {
          currentMaxCoord = element.getEnd();
        }
      }
      if ( currentMinCoord >= lowRange && currentMaxCoord <= highRange )
      {
        double value = 0.0;
        for ( PatmanEntry ele : locus )
        {
          

          HashMap<String, ExpressionElement> originalRow = myExpressionMatrix.get( ele.getSequence() );
          for ( ArrayList<String> sampleList : this.myExpressionMatrix.getFileNames() )
          {

            if ( originalRow.containsKey( sampleList.get( 0 ) ) )
            {
              value =
                  originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
              PatmanEntry clone = new PatmanEntry(ele);
              clone.setAbundance(value);
              locusItems.get( sampleList.get( 0 ) ).add( clone );
              totalAbundance += value;
            }
          }
        }
      }
    }
    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( myExpressionMatrix.getGenomeFile(), true, (TierParameters) null );
    vissr.setNormalised( true );
    int sampleNumber = 0;
    
    //for ( ArrayList<String> sample : myExpressionMatrix.getFileNames() )
    for(Entry<String, Patman> entry : locusItems.entrySet())
    {
      entry.getValue().setGlyphHeights( (int) totalAbundance );
      String tierName = "Sample " + entry.getKey();
      sampleNumber++;

      //create tier params for this sample
      TierParameters tp = new TierParameters.Builder( tierName ).build();//glyphClass( StripedSolidRectGlyph.class ).build();
      if ( !entry.getValue().isEmpty() )
      {
        tp.addList( locusItems.get( entry.getKey() ) );
      }
      vissr.addTier( tp );

    }
    vissr.displaySequenceRegion( chromoID, lowRange - 5, highRange + 5 );

    AppUtils.INSTANCE.activateFrame(vissr);


    
  }
  public void generateVissrAggregate(boolean completeModel)
  {
    
    int row;// = outline.convertRowIndexToModel( outline.getSelectedRow() );
    String chromo = "";// = outline.getValueAt( row, outline.getColumnCount() - 1 ).toString();
    int startCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
    int endCoord;// = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
    
    if(completeModel)
    {
      startCoord = -1;
      endCoord = -1;
    }
    else
    {
       row = outline.convertRowIndexToModel( outline.getSelectedRow() );
       chromo = outline.getValueAt( row, 1 ).toString();

    startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
    endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 3 ).toString(), 0 );

    }
    ArrayList<Patman> lociForChromo = cluster_per_chrom.get( chromo );
  
    SequenceVizMainFrame vissr =LocusUtils.setupAndDisplayPatmanAggregate( myExpressionMatrix, true, startCoord, endCoord, 
      lociForChromo);
    vissr.displaySequenceRegion(chromo, startCoord, endCoord );
    AppUtils.INSTANCE.activateFrame(vissr);
  }

  private void exportEntireLocusToFasta( File outputFile, String chromo, int startCoord, int endCoord, FastaMap fastaMap )
  {
    FileWriter outFASTAFile = null;
    try
    {
      outFASTAFile = new FileWriter( outputFile, true );
      PrintWriter out = new PrintWriter( outFASTAFile );
      ArrayList<Patman> lociForChromo = this.cluster_per_chrom.get( chromo);
      ArrayList<Patman> lociInRange = LocusUtils.getPatmanLociInRange( lociForChromo, startCoord, endCoord);
      
      for(Patman locus : lociInRange)
      {
        int currentMinCoord = Integer.MAX_VALUE;
        int currentMaxCoord = Integer.MIN_VALUE;

        for ( PatmanEntry element : locus )
        {

          if ( element.getStart() < currentMinCoord )
          {
            currentMinCoord = element.getStart();
          }
          if ( element.getEnd() > currentMaxCoord )
          {
            currentMaxCoord = element.getEnd();
          }
        }
        if ( currentMinCoord >= startCoord && currentMaxCoord <= endCoord )
        {
          FastaRecord get = fastaMap.get( chromo );
          out.println( ">" + chromo + "-Start:" + currentMaxCoord + "-End:" + currentMaxCoord );
          out.println( get.getSubSequenceAsString( currentMinCoord, currentMaxCoord, SequenceStrand.POSITIVE ) );
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

  Outline getOutline()
  {
    return this.outline;
  }

}
