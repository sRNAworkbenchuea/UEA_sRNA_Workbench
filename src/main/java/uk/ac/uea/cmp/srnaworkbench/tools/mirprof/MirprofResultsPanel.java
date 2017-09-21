/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FilterResutlsPanel.java
 *
 * Created on 26-May-2011, 19:09:43
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.awt.Color;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeModel;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.swing.RenderData;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterGroup;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.Mirprof.MatchGroup;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.Mirprof.MirnaEntry;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.Mirprof.OrganismGroup;
import uk.ac.uea.cmp.srnaworkbench.utils.FormattingUtils;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.MultiLineHeaderRenderer;
import uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers.StandardCellRenderer;


/**
 *
 * @author Matt Stocks and Dan Mapleson
 */
final class MirprofResultsPanel extends javax.swing.JPanel
{
  private Outline outline;
  private OrganismGroup results;
  private boolean usingGenome = true;

  /** Creates new form MirprofResultsPanel */
  public MirprofResultsPanel()
  {
    initComponents();

    resetTable();
  }

  public void resetTable()
  {
    display( new OrganismGroup(), new ArrayList<FilterGroup>(), new ArrayList<File>(), true );
  }

  /**
   * Displays miRProf results on the output table
   * @param results Result groupings
   * @param filter_stats filter statistics for each sample
   * @param input_files The list of input samples
   */
  public void display( OrganismGroup results, List<FilterGroup> filter_stats, List<File> input_files, boolean usingGenome )
  {
    this.results = results;

    this.usingGenome = usingGenome;
    MirprofTableRowModel mrm = new MirprofTableRowModel( input_files );

    JTree tree = generateTreeTable( results, filter_stats );
    tree.setBackground( Color.WHITE );

    this.outline = generateResultsTable( tree, mrm );

    updateTable();
  }



  /**
   * Generate the table containing the results from the miRProf run
   * @param org_group Results
   * @param filter_stages Filtering results
   * @return The tree table for use in the GUI
   */
  private JTree generateTreeTable( OrganismGroup org_group, List<FilterGroup> filter_stages )
  {
    if ( org_group == null || filter_stages == null )
    {
      return new JTree( new OutlineNode( new ArrayList<String>() ) );
    }

    // Gets the highest read count from all samples after the last filtering step
    int[] total_reads = new int[filter_stages.size()];
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      FilterGroup fg = filter_stages.get( i );
      total_reads[i] = fg.get( fg.size() - 1 ).getTotalReadCount();
    }

    ArrayList<String> rootList = new ArrayList<String>();
    rootList.add( "root" );
    OutlineNode rootNode = new OutlineNode( rootList );

    // data - one block per organism (which could be "all combined")
    for ( String org : new TreeSet<String>( org_group.keySet() ) )
    {
      MatchGroup mirnas = org_group.get( org );

      ArrayList<String> newOrgList = new ArrayList<String>();
      newOrgList.add( "Organism: " + org );

      OutlineNode child1 = new OutlineNode( newOrgList );
      for(Entry<String, MirnaEntry> entrySet : new TreeMap<String, MirnaEntry>(mirnas).entrySet())
      {
        String result = entrySet.getKey();
        MirnaEntry miE = entrySet.getValue();
        ArrayList<String> miRNAIDList = new ArrayList<String>();
        miRNAIDList.add( result );
        OutlineNode child2 = new OutlineNode( miRNAIDList );

        HashMap<String, ArrayList<String>> seq_to_row = new HashMap<String, ArrayList<String>>();

        //default row will be modifed later, just contains empty row data
        ArrayList<String> defaultRow = new ArrayList<String>();
        defaultRow.add( "N/A" );
        for ( int i = 1; i < ( miE.size() * 2 ) + 1; i++ )
        {
          defaultRow.add( "0" );
        }
        for ( SrnaMatch sampleData : miE )
        {
          
          FastaMap seqMap = sampleData.getSeqMap();
          if(seqMap != null)
          {
            for ( Entry<String, FastaRecord> seqEntry : seqMap.entrySet() )
            {
              //cast object to array list to prevent same memory object being used
              seq_to_row.put( seqEntry.getValue().getSequence(), (ArrayList<String>)defaultRow.clone() );
            }
            seq_to_row.put("Totals: ", defaultRow);
          }
        }


        int modifyIndex = 1;
        double normalisedTotal = 0.0;
        int genomeMatchTotal = 0;
        for ( int i = 0; i < miE.size(); i++ )
        {
          FastaMap seqMap = miE.get( i ).getSeqMap();
          if(seqMap != null)
          {

            for ( Entry<String, FastaRecord> seqEntry : seqMap.entrySet() )
            {
              if ( usingGenome )
              {
                ArrayList<String> row = seq_to_row.get( seqEntry.getKey() );
                int currentHighValue = StringUtils.safeIntegerParse( row.get( 0 ), 0);
                int newHit = Math.max(  currentHighValue, seqEntry.getValue().getHitCount());
                
                seq_to_row.get( seqEntry.getKey() ).set(0, String.valueOf( newHit ) );
                //genomeMatchTotal += seqEntry.getValue().getHitCount();
              }
              
              seq_to_row.get( seqEntry.getKey() ).set(modifyIndex, String.valueOf( seqEntry.getValue().getAbundance() ) );
              double norm_count = ( ( (double) seqEntry.getValue().getAbundance() ) / ( (double) total_reads[i] ) ) * 1e6;
              normalisedTotal += norm_count;
              seq_to_row.get( seqEntry.getKey() ).set(modifyIndex+miE.size(), FormattingUtils.formatSRNACounts( norm_count ) );
              
            }
            seq_to_row.get( "Totals: ").set(modifyIndex, String.valueOf( miE.get(i).getRawCount()));
            seq_to_row.get( "Totals: ").set(modifyIndex+miE.size(), FormattingUtils.formatSRNACounts( normalisedTotal));
            normalisedTotal = 0.0;
            
            
          }
          modifyIndex ++;
        }
        if ( usingGenome )
        {
          for ( Entry<String, ArrayList<String>> e : seq_to_row.entrySet() )
          {
            genomeMatchTotal += StringUtils.safeIntegerParse( e.getValue().get( 0 ), 0 );
          }
          seq_to_row.get( "Totals: " ).set( 0, String.valueOf( genomeMatchTotal ) );
        }
        for(Entry<String, ArrayList<String>> entry : new TreeMap<String, ArrayList<String>>(seq_to_row).entrySet())
        {
          ArrayList<String> dummyList =  new ArrayList<String>();
          dummyList.add( entry.getKey());
          dummyList.trimToSize();
          dummyList.addAll( entry.getValue());
          //dummyList.add()
          OutlineNode child3 = new OutlineNode(dummyList);
          child2.add( child3 );
        }
        child1.add( child2 );
        //ArrayList<String> rowData = new ArrayList<String>();
        //int amountOfRows = Math.max( WIDTH, WIDTH )
      }

      rootNode.add( child1 );
    }

    return new JTree( rootNode );
  }

  /**
   * Populates the Results table given a JTree containing the miRProf results
   * @param results
   */
  private Outline generateResultsTable( JTree results, MirprofTableRowModel mrm )
  {
    Outline o = newOutline();

    final TreeModel tm = results.getModel();

    OutlineModel mdl = DefaultOutlineModel.createOutlineModel( tm, mrm, true, "Matching miRNAs" );

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
      popupMenu.show( table_scroll_pane, (int)this.getMousePosition().getX(), (int)this.getMousePosition().getY() );
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

    for(int i = 0; i < this.outline.getColumnCount(); i++)
    {
      String s = this.outline.getModel().getValueAt( r, i ).toString().trim();
      sb.append( s );
      sb.append( "\t" );
    }

    String copyText = sb.toString().trim();

    StringSelection ss = new StringSelection( copyText );

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( ss, null );
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
        copyMenu = new javax.swing.JMenuItem();
        table_scroll_pane = new javax.swing.JScrollPane();

        copyMenu.setIcon(new javax.swing.ImageIcon("/Developer/Applications/sRNAWorkbench/Workbench/src/main/resources/images/SharedImages/copy-item.png")); // NOI18N
        copyMenu.setText("Copy to Clipboard");
        copyMenu.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                copyMenuActionPerformed(evt);
            }
        });
        popupMenu.add(copyMenu);

        setBackground(new java.awt.Color(120, 120, 120));
        setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N

        table_scroll_pane.setBackground(new java.awt.Color(120, 120, 120));
        table_scroll_pane.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        table_scroll_pane.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(table_scroll_pane, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

  private void copyMenuActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_copyMenuActionPerformed
  {//GEN-HEADEREND:event_copyMenuActionPerformed
    copySelectedRow();
  }//GEN-LAST:event_copyMenuActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem copyMenu;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane table_scroll_pane;
    // End of variables declaration//GEN-END:variables

  public Outline getOutline()
  {
    return this.outline;
  }
}
