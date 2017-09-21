/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.LocusUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.CoLIDERenderDataProvider;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
/**
 *
 * @author w0445959
 */
public class MiRCatOutlineResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  private File genome;
  private HashMap<String, ArrayList<Patman>> cluster_per_chrom;
  private OutlineNode rootNode;

  /**
   * Creates new form MiRCatResultsPanel
   */
  public MiRCatOutlineResultsPanel()
  {
    initComponents();
  }
  
  public void display( 
                       File genomeFile,
                       HashMap<String, ArrayList<Patman>> cluster_per_chrom)
  {

    this.genome = genomeFile;

    this.cluster_per_chrom = cluster_per_chrom;

    MiRCatRowModel mrm = new MiRCatRowModel(  );

    this.setName( "MiRCatResultsPanel" );
    JTree tree = generateTreeTable(  );
    tree.setBackground( Color.WHITE );

    
    this.outline = generateResultsTable( tree, mrm );
    updateTable();


  }

  private Outline generateResultsTable( JTree results, MiRCatRowModel mrm )
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

  public void addRowToTable( String chromosome, ArrayList<String> data )
  {


    //child1.setUserObject( newChromo );
    this.outline.revalidate();

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
     this.popupMenu.show( this, evt.getX(), evt.getY() );
    }
  }

  /**
   * Generate the table containing the results from the CoLIDE run
   *
   * @param chromoToLocus the data as a map, Chromosome -> ArrayList of loci
   * (each locus is another array list of expression elements)
   * @return The tree table for use in the GUI
   */
  private JTree generateTreeTable()
  {



//
    ArrayList<String> rootList = new ArrayList<String>();
    rootList.add( "root" );
    rootNode = new OutlineNode( rootList );
     for(Entry<String, ArrayList<Patman>> entry : 
     new TreeMap<String, ArrayList<Patman>>(this.cluster_per_chrom).entrySet())
    {
      ArrayList<String> newChromo = new ArrayList<String>();
      newChromo.add( "Chromosome: " + entry.getKey() );
      OutlineNode child1 = new OutlineNode( newChromo );
      rootNode.add( child1 );
    }
    return new JTree( rootNode );
  }
  
  private TreePath find( String s )
  {
    @SuppressWarnings("unchecked")
    Enumeration<OutlineNode> e = rootNode.depthFirstEnumeration();
    while ( e.hasMoreElements() )
    {
      DefaultMutableTreeNode node = e.nextElement();
      if ( node.toString().equalsIgnoreCase( s ) )
      {
        return new TreePath( node.getPath() );
      }
    }
    return null;
  }
  public void generateVissrArrow(boolean completeModel)
  {
       int row = -1;// = outline.convertRowIndexToModel( outline.getSelectedRow() );
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
       chromo = outline.getValueAt( row, outline.getColumnCount() - 2 ).toString();
       startCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 1 ).toString(), 0 );
       endCoord = StringUtils.safeIntegerParse( outline.getValueAt( row, 2 ).toString(), 0 );
       
    }
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
    String miRNA_ID = "miRNA: ";
    locusItems.put( miRNA_ID , new Patman() );
    
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
          

          locusItems.get( miRNA_ID ).add( ele );
        }
      }
    }
    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( genome, true, (TierParameters) null );
    vissr.setNormalised( true );
    int sampleNumber = 0;
    
    //for ( ArrayList<String> sample : myExpressionMatrix.getFileNames() )
    for(Map.Entry<String, Patman> entry : locusItems.entrySet())
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

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        resultsScroll = new javax.swing.JScrollPane();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(resultsScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane resultsScroll;
    // End of variables declaration//GEN-END:variables
}
