/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.expressionCalculator;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.swing.RenderData;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.StandardCellRenderer;

/**
 *
 * @author w0445959
 */
public class ExpressionCalculatorResultsPanel extends javax.swing.JPanel
{
  private Outline outline;

  /**
   * Creates new form ExpressionCalculatorResultsPanel
   */
  public ExpressionCalculatorResultsPanel()
  {
    initComponents();
  }

  public void display()
  {
//    this.results = results;
//
//    this.usingGenome = usingGenome;
//    MirprofTableRowModel mrm = new MirprofTableRowModel( input_files );
//
//    JTree tree = generateTreeTable( results, filter_stats );
//    tree.setBackground( Color.WHITE );

    //this.outline = generateResultsTable( tree, mrm );

    updateTable();
  }

  private Outline generateResultsTable( JTree results, ExpressionCalculatorRowTableModel mrm )
  {
    Outline o = newOutline();

    final TreeModel tm = results.getModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "Expression Results" );

    o.setModel( mdl );

    MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer();
    Enumeration e = o.getColumnModel().getColumns();
    while ( e.hasMoreElements() )
    {
      TableColumn mod = ( (TableColumn) e.nextElement() );
      mod.setHeaderRenderer( renderer );
      mod.setPreferredWidth( 150 );
    }

    return o;
  }

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
    for ( Map.Entry<String, ArrayList<ArrayList<String>>> entry :
      new TreeMap<String, ArrayList<ArrayList<String>>>( data ).entrySet() )
    {
      ArrayList<String> newChromo = new ArrayList<String>();
      newChromo.add( "Chromosome: " + entry.getKey() );
      OutlineNode child1 = new OutlineNode( newChromo );

      for ( ArrayList<String> rowData : entry.getValue() )
      {
        rowData.add( 0, "" );
        OutlineNode child2 = new OutlineNode( rowData );
        child1.add( child2 );
      }
      rootNode.add( child1 );

    }
    return new JTree( rootNode );
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
    o.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_OFF );
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

  private void updateTable()
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        table_scroll_pane.setViewportView( outline );
      }
    } );
  }

  private void checkPopup( java.awt.event.MouseEvent evt )
  {
    if ( evt.isPopupTrigger() )
    {
      popupMenu.show( table_scroll_pane, (int) this.getMousePosition().getX(), (int) this.getMousePosition().getY() );
    }
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

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        table_scroll_pane = new javax.swing.JScrollPane();

        setBackground(new java.awt.Color(120, 120, 120));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, table_scroll_pane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, table_scroll_pane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane table_scroll_pane;
    // End of variables declaration//GEN-END:variables
}
