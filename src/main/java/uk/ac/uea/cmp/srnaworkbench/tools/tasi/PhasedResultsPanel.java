/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FilterResutlsPanel.java
 *
 * Created on 26-May-2011, 19:09:43
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.swing.RenderData;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.StandardCellRenderer;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * 
 * @author Matthew Stocks and Dan Mapleson
 */
final class PhasedResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  
  private List<PatmanEntry> results;
  
  /** 
   * Creates new Results panel 
   */
  public PhasedResultsPanel()
  {
    initComponents();
    
    resetTable();
  }

  private void resetTable()
  {
    populateResults( new ArrayList<PatmanEntry>() );
  }

  /**
   * Update the results table with the provided tas data
   * @param tasis 
   */
  public void populateResults( List<PatmanEntry> predicted_tas_loci )
  {
    this.results = predicted_tas_loci;
    
    JTree tree = generateTreeTable();
    
    this.outline = generateResultsTable( tree );

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
    for ( PatmanEntry pe : this.results )
    {
      ArrayList<String> tas_list = new ArrayList<String>();
      tas_list.add( Integer.toString( counter ));
      tas_list.add( pe.getSequence() );
      tas_list.add( Integer.toString( (int)pe.getAbundance() ) );
      tas_list.add( pe.getLongReadHeader() );
      tas_list.add( Integer.toString( pe.getStart() ) );
      tas_list.add( Integer.toString( pe.getEnd() ) );
      tas_list.add( String.valueOf( pe.getSequenceStrand().getCharCode() ));
      
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
    TasiRowModel mrm = TasiRowModel.newTasiRowModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "Phased sRNA" );

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
        public void mousePressed(java.awt.event.MouseEvent evt) {
            table_scroll_paneMousePressed(evt);
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
  
    
  private void checkPopup( java.awt.event.MouseEvent evt )
  {
    if ( evt.isPopupTrigger() )
    {
      popupMenu.show( table_scroll_pane, (int)this.getMousePosition().getX(), (int)this.getMousePosition().getY() );
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

        table_scroll_pane.setComponentPopupMenu(popupMenu);
        table_scroll_pane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        table_scroll_pane.setOpaque(false);
        table_scroll_pane.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mousePressed(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                table_scroll_paneMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 213, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
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

  private void mnuCopyActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_mnuCopyActionPerformed
  {//GEN-HEADEREND:event_mnuCopyActionPerformed
    copySelectedLocus();
  }//GEN-LAST:event_mnuCopyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem mnuCopy;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane table_scroll_pane;
    // End of variables declaration//GEN-END:variables
}
