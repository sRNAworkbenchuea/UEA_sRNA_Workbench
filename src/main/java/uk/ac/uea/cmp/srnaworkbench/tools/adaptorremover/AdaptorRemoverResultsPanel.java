/*
 * AR_ResultsPanel.java
 *
 * Created on 26-May-2011, 19:07:40
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import uk.ac.uea.cmp.srnaworkbench.swing.RotatingIcon;

import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * GUI component for displaying results from the adaptor remover tool
 * @author Matt
 */
public final class AdaptorRemoverResultsPanel extends javax.swing.JPanel
{
  boolean firstTab = true;
  
  HashMap<String, JLabel> labelsMap = new HashMap<String, JLabel>();
  /** Creates new form AR_ResultsPanel */
  public AdaptorRemoverResultsPanel()
  {
    initComponents();

    resetExecutionStatsTable();
    resetLengthDistTable();
        
    this.scrlExecutionStats.getViewport().setBackground( new java.awt.Color( 120, 120, 120 ) );
    this.scrlLenDist.getViewport().setBackground( new java.awt.Color( 120, 120, 120 ) );    
  }

  public void setOutputStatsTabWaiting(String title, boolean waiting)
  {
    int componentIndex = 0;
    for(int i = 0; i < inputStatsTabs.getTabCount(); i++ )
    {
      String titleAt = inputStatsTabs.getTitleAt( i );
      if ( titleAt.equals( title ) )
      {
        componentIndex = i;
        if ( waiting )
        {
          this.inputStatsTabs.setIconAt( componentIndex,
            new RotatingIcon( new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/ProgressSmall.gif" ) ), inputStatsTabs ) );
        }
        else
        {
          this.inputStatsTabs.setIconAt( componentIndex, new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/completeSmall.jpeg" ) ) );
        }

      }
    }
    
  }
  public void setOutputLengthDistTabWaiting(String title, boolean waiting)
  {
    int componentIndex = 0;
    for(int i = 0; i < lengthDistTabs.getTabCount(); i++ )
    {
      if ( lengthDistTabs.getTitleAt( i ).equals( title ) )
      {
        componentIndex = i;
        if ( waiting )
        {
          this.lengthDistTabs.setIconAt( componentIndex,
            new RotatingIcon( new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/ProgressSmall.gif" ) ), lengthDistTabs ) );
        }
        else
        {
          this.lengthDistTabs.setIconAt( componentIndex, new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/completeSmall.jpeg" ) ) );
        }
//      
      }
    }
    
  }
  public void addOutputTab( String name )
  {
    for(int i = 0; i < lengthDistTabs.getTabCount(); i++ )
    {
      if(lengthDistTabs.getTitleAt( i).equals( name ))
      {
        return;
      }
    }
    if(firstTab)
    {
      firstTab = false;
      this.inputStatsTabs.setTitleAt( 0, name );
      //this.inputStatsTabs.setIconAt(0, Tools.createImageIcon( "/uk/ac/uea/cmp/srnaworkbench/images/GUI_Icons/workbench.jpg", "Processing...") );
      this.lengthDistTabs.setTitleAt( 0, name);
      //this.lengthDistTabs.setIconAt(0, Tools.createImageIcon( "/uk/ac/uea/cmp/srnaworkbench/images/GUI_Icons/workbench.jpg", "Complete!") );
    }
    else
    {
      
      JScrollPane newScrlExecutionStats = new javax.swing.JScrollPane();
      JTable newTblExecutionStats = new javax.swing.JTable();
      newTblExecutionStats.setAutoCreateColumnsFromModel( false );
      newTblExecutionStats.setBackground( new java.awt.Color( 120, 120, 120 ) );
      newTblExecutionStats.setFont( new java.awt.Font( "Lucida Sans Unicode", 0, 11 ) ); // NOI18N
      newTblExecutionStats.setForeground( new java.awt.Color( 255, 255, 255 ) );
      newTblExecutionStats.setModel( new javax.swing.table.DefaultTableModel(
        new Object[][]
        {
        },
        new String[]
        {
        } ) );
      newTblExecutionStats.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS );
      newTblExecutionStats.setCellSelectionEnabled( true );
      newTblExecutionStats.setFillsViewportHeight( true );
      newTblExecutionStats.setGridColor( new java.awt.Color( 153, 204, 255 ) );
      newTblExecutionStats.setRowHeight( 25 );
      newTblExecutionStats.setRowMargin( 10 );
      newTblExecutionStats.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
      newTblExecutionStats.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
      newTblExecutionStats.getColumnModel().getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION );

      newScrlExecutionStats.setViewportView( newTblExecutionStats );
      
      
      JScrollPane newScrlLenDist = new javax.swing.JScrollPane();
      JTable newTblLenDist = new javax.swing.JTable();
      
      newTblLenDist.setAutoCreateColumnsFromModel( false );
      newTblLenDist.setBackground( new java.awt.Color( 120, 120, 120 ) );
      newTblLenDist.setFont( new java.awt.Font( "Lucida Sans Unicode", 0, 11 ) ); // NOI18N
      newTblLenDist.setForeground( new java.awt.Color( 255, 255, 255 ) );
      newTblLenDist.setModel( new javax.swing.table.DefaultTableModel(
        new Object[][]
        {
        },
        new String[]
        {
        } ) );
      newTblLenDist.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS );
      newTblLenDist.setCellSelectionEnabled( true );
      newTblLenDist.setFillsViewportHeight( true );
      newTblLenDist.setGridColor( new java.awt.Color( 153, 204, 255 ) );
      newTblLenDist.setRowHeight( 25 );
      newTblLenDist.setRowMargin( 10 );
      newTblLenDist.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
      newTblLenDist.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
      newTblLenDist.getTableHeader().setResizingAllowed( false );
      newTblLenDist.getTableHeader().setReorderingAllowed( false );
      newTblLenDist.getColumnModel().getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION );

      
      newScrlLenDist.setViewportView( newTblLenDist );
      this.inputStatsTabs.addTab( name, new RotatingIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/ProgressSmall.gif")), 
              inputStatsTabs), 
        newScrlExecutionStats );
      this.lengthDistTabs.addTab( name, new RotatingIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/ProgressSmall.gif")), 
        lengthDistTabs), 
        newScrlLenDist);
        
    }
  }
  
  
  public void reset()
  {
    resetLengthDistTable();
    resetExecutionStatsTable();
  }
  
  // ***** Length Distance Table *****
  
  /**
   * Clears out all values in the existing length dist table
   */
  private void resetLengthDistTable()
  {
    lengthDistTabs.removeAll();
          JScrollPane newScrlExecutionStats = new javax.swing.JScrollPane();
      JTable newTblExecutionStats = new javax.swing.JTable();
      newTblExecutionStats.setAutoCreateColumnsFromModel( false );
      newTblExecutionStats.setBackground( new java.awt.Color( 120, 120, 120 ) );
      newTblExecutionStats.setFont( new java.awt.Font( "Lucida Sans Unicode", 0, 11 ) ); // NOI18N
      newTblExecutionStats.setForeground( new java.awt.Color( 255, 255, 255 ) );
      newTblExecutionStats.setModel( new javax.swing.table.DefaultTableModel(
        new Object[][]
        {
        },
        new String[]
        {
        } ) );
      newTblExecutionStats.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS );
      newTblExecutionStats.setCellSelectionEnabled( true );
      newTblExecutionStats.setFillsViewportHeight( true );
      newTblExecutionStats.setGridColor( new java.awt.Color( 153, 204, 255 ) );
      newTblExecutionStats.setRowHeight( 25 );
      newTblExecutionStats.setRowMargin( 10 );
      newTblExecutionStats.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
      newTblExecutionStats.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
      newTblExecutionStats.getColumnModel().getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION );

      newScrlExecutionStats.setViewportView( newTblExecutionStats );
      
      
      JScrollPane newScrlLenDist = new javax.swing.JScrollPane();
      JTable newTblLenDist = new javax.swing.JTable();
      
      newTblLenDist.setAutoCreateColumnsFromModel( false );
      newTblLenDist.setBackground( new java.awt.Color( 120, 120, 120 ) );
      newTblLenDist.setFont( new java.awt.Font( "Lucida Sans Unicode", 0, 11 ) ); // NOI18N
      newTblLenDist.setForeground( new java.awt.Color( 255, 255, 255 ) );
      newTblLenDist.setModel( new javax.swing.table.DefaultTableModel(
        new Object[][]
        {
        },
        new String[]
        {
        } ) );
      newTblLenDist.setAutoResizeMode( javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS );
      newTblLenDist.setCellSelectionEnabled( true );
      newTblLenDist.setFillsViewportHeight( true );
      newTblLenDist.setGridColor( new java.awt.Color( 153, 204, 255 ) );
      newTblLenDist.setRowHeight( 25 );
      newTblLenDist.setRowMargin( 10 );
      newTblLenDist.setSelectionBackground( new java.awt.Color( 102, 102, 102 ) );
      newTblLenDist.setSelectionForeground( new java.awt.Color( 153, 204, 255 ) );
      newTblLenDist.getTableHeader().setResizingAllowed( false );
      newTblLenDist.getTableHeader().setReorderingAllowed( false );
      newTblLenDist.getColumnModel().getSelectionModel().setSelectionMode( javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION );

      
      newScrlLenDist.setViewportView( newTblLenDist );
      this.inputStatsTabs.addTab( "Waiting for input...", 
        newScrlExecutionStats );
      this.lengthDistTabs.addTab( "Waiting for input...", 
        newScrlLenDist);
        
//    for ( Component comp : this.lengthDistTabs.getComponents() )
//    {
//      ( (DefaultTableModel) ((JTable)(((JScrollPane)comp).getViewport().)).getModel() ).getDataVector().removeAllElements();
//      tblLenDist.revalidate();
//    }
  }
  
  private void resetLengthDistTable(DefaultTableModel model)
  {
    model.getDataVector().removeAllElements();
    tblLenDist.revalidate();
  }
  
  
  /**
   * Populates the length distribution table with the provided statistics
   * @param theDist the length distribution statistics
   */
  public void fillLengthDistTable( Map<Integer, FilterStage> length_distribution )
  {
    resetLengthDistTable();
    
    // Leave table blank if there's no input
    if (length_distribution == null || length_distribution.isEmpty() )
      return;
        
    int col = 1;
    
    TreeSet<Integer> lengths = new TreeSet<Integer>( length_distribution.keySet() );
    
    tblLenDist.setColumnModel( createLenDistColumnModel( lengths ) );
    
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn( "Lengths:" );
    
    for( Integer size : lengths )
    {
      model.addColumn( size.toString() );
    }
    
    model.addRow( createRow(model, "Total Reads:") );
    model.addRow( createRow(model, "Distinct Reads:") );    
    
    tblLenDist.setModel( model );
    scrlLenDist.setViewportView(tblLenDist);
    
    for ( Integer size : lengths )
    {
      if ( col >= tblLenDist.getColumnCount() )
      {
        int j = 0;
        break;
      }
      
      String totalReadCount = length_distribution.get( size ).getTotalReadCount() > 0 ? 
        Integer.toString(length_distribution.get( size ).getTotalReadCount()) : "N/A";
      String distinctReadCount = length_distribution.get( size ).getDistinctReadCount() > 0 ? 
        Integer.toString(length_distribution.get( size ).getDistinctReadCount()) : "N/A";
      tblLenDist.setValueAt( totalReadCount, 0, col );
      tblLenDist.setValueAt( distinctReadCount, 1, col );
      col++;
    }
  }
    public void fillLengthDistTable(String title, Map<Integer, FilterStage> length_distribution )
  {
    int componentIndex = 0;
    for(int i = 0; i < lengthDistTabs.getTabCount(); i++ )
    {
      if(lengthDistTabs.getTitleAt( i).equals( title))
      {
        componentIndex = i;
        break;
      }
    }
    JTable view = (JTable)((JScrollPane)lengthDistTabs.getComponentAt( componentIndex)).getViewport().getView();
    resetLengthDistTable( (DefaultTableModel)view.getModel() );
    
    // Leave table blank if there's no input
    if (length_distribution == null || length_distribution.isEmpty() )
      return;
        
    int col = 1;
    
    TreeSet<Integer> lengths = new TreeSet<Integer>( length_distribution.keySet() );
    
    view.setColumnModel( createLenDistColumnModel( lengths ) );
    
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn( "Lengths:" );
    
    for( Integer size : lengths )
    {
      model.addColumn( size.toString() );
    }
    
    model.addRow( createRow(model, "Total Reads:") );
    model.addRow( createRow(model, "Distinct Reads:") );    
    
    view.setModel( model );
    ((JScrollPane)lengthDistTabs.getComponentAt( componentIndex)).setViewportView(view);
    
    for ( Integer size : lengths )
    {
      if ( col >= view.getColumnCount() )
      {
        //int j = 0;
        break;
      }
      
      view.setValueAt( length_distribution.get( size ).getTotalReadCount(), 0, col );
      view.setValueAt( length_distribution.get( size ).getDistinctReadCount(), 1, col );
      col++;
    }
  }
  private TableColumnModel createLenDistColumnModel( TreeSet<Integer> lengths )
  {
    DefaultTableCellRenderer col_header_renderer = (DefaultTableCellRenderer)tblLenDist.getTableHeader().getDefaultRenderer();
    col_header_renderer.setHorizontalAlignment( SwingConstants.RIGHT );
    
    TableColumnModel tcm = new DefaultTableColumnModel();
    
    TableColumn header = new TableColumn( 0, 100 );
    header.setResizable( false );
    header.setHeaderValue( "" );
    
    tcm.addColumn( header );
    
    DefaultTableCellRenderer cell_renderer = new DefaultTableCellRenderer();
    cell_renderer.setHorizontalAlignment( SwingConstants.RIGHT );
    
    int idx = 1;
    for( Integer size : lengths )
    {
      TableColumn tc = new TableColumn( idx++, 60 );
      tc.setResizable( true );
      tc.setCellRenderer( cell_renderer );
      tc.setHeaderRenderer( col_header_renderer );
      tc.setHeaderValue( size.toString() );   
                  
      tcm.addColumn( tc );
    }
    
    return tcm;
  }
  
  
  // ***** Execution Statistics Table *****
  
  
  /**
   * Clears out all values in the existing execution stats table
   */
  private void resetExecutionStatsTable()
  {
    this.inputStatsTabs.removeAll();
  }
  private void resetExecutionStatsTable(DefaultTableModel model)
  {
    model.getDataVector().removeAllElements();
    tblExecutionStats.revalidate();
  }
  
  /**
   * Populates the table with the provided execution statistics
   * @param theResults execution statistics
   */
  public void fillExecutionStatsTable( List<FilterStage> statistics )
  {
    resetExecutionStatsTable();

    if ( statistics == null || statistics.isEmpty() )
    {
      return;
    }
    
    tblExecutionStats.setColumnModel( createExecutionColumnModel( statistics ) );

    DefaultTableModel model = new DefaultTableModel();
    model.addColumn( "" );
    
    for( FilterStage stage : statistics )
    {
      model.addColumn( stage.getShortStageName() );
    }
    
    model.addRow( createRow(model, "Total Reads:") );
    model.addRow( createRow(model, "Distinct Reads:") );  
    
    tblExecutionStats.setModel( model );
    scrlExecutionStats.setViewportView(tblExecutionStats);
    
    // Order is as follows: Input, 3' matches, 5' matches, Invalid length, Seq remaining
    int i = 1;
    for ( FilterStage data : statistics )
    {
        
      String totalReadCount = data.getTotalReadCount() > 0 ? 
        Integer.toString(data.getTotalReadCount()) : "N/A";
      String distinctReadCount = data.getDistinctReadCount() > 0 ? 
        Integer.toString(data.getDistinctReadCount()) : "N/A";
      tblExecutionStats.setValueAt( totalReadCount, 0, i);
      tblExecutionStats.setValueAt( distinctReadCount, 1, i );
      i++;
    }
  }

  
  private TableColumnModel createExecutionColumnModel( List<FilterStage> statistics )
  {
    DefaultTableCellRenderer col_header_renderer = (DefaultTableCellRenderer)tblExecutionStats.getTableHeader().getDefaultRenderer();
    col_header_renderer.setHorizontalAlignment( SwingConstants.CENTER );
    
    TableColumnModel tcm = new DefaultTableColumnModel();
    
    TableColumn header = new TableColumn( 0, 100 );
    header.setResizable( false );
    header.setHeaderValue( "" );
    
    tcm.addColumn( header );
    
    DefaultTableCellRenderer cell_renderer = new DefaultTableCellRenderer();
    cell_renderer.setHorizontalAlignment( SwingConstants.RIGHT );
    
    int idx = 1;
    for( FilterStage stage : statistics )
    {
      TableColumn tc = new TableColumn( idx++, 100 );
      tc.setResizable( true );
      tc.setCellRenderer( cell_renderer );
      tc.setHeaderRenderer( col_header_renderer );
      tc.setHeaderValue( stage.getShortStageName() );   
                  
      tcm.addColumn( tc );
    }
    
    return tcm;
  }
  
  private Object[] createRow(DefaultTableModel model, String row_header)
  {
    Object[] oa = new Object[model.getColumnCount()];
    
    oa[0] = row_header;
    
    for(int i = 1; i < oa.length; i++)
    {
      oa[i] = "0";
    }
    
    return oa;
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

    jScrollPane1 = new javax.swing.JScrollPane();
    jPanel1 = new javax.swing.JPanel();
    lblExecutionStats = new javax.swing.JLabel();
    lengthDistTabs = new javax.swing.JTabbedPane();
    scrlLenDist = new javax.swing.JScrollPane();
    tblLenDist = new javax.swing.JTable();
    lblLenDist = new javax.swing.JLabel();
    inputStatsTabs = new javax.swing.JTabbedPane();
    scrlExecutionStats = new javax.swing.JScrollPane();
    tblExecutionStats = new javax.swing.JTable();

    setBackground(new java.awt.Color(120, 120, 120));
    setAutoscrolls(true);

    jPanel1.setBackground(new java.awt.Color(120, 120, 120));

    lblExecutionStats.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblExecutionStats.setForeground(new java.awt.Color(255, 255, 255));
    lblExecutionStats.setText("Execution Statistics (sequences remaining after each processing stage):");

    lengthDistTabs.setBackground(new java.awt.Color(120, 120, 120));
    lengthDistTabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

    tblLenDist.setAutoCreateColumnsFromModel(false);
    tblLenDist.setBackground(new java.awt.Color(120, 120, 120));
    tblLenDist.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    tblLenDist.setForeground(new java.awt.Color(255, 255, 255));
    tblLenDist.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {

      }
    ));
    tblLenDist.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    tblLenDist.setCellSelectionEnabled(true);
    tblLenDist.setFillsViewportHeight(true);
    tblLenDist.setGridColor(new java.awt.Color(153, 204, 255));
    tblLenDist.setRowHeight(25);
    tblLenDist.setRowMargin(10);
    tblLenDist.setSelectionBackground(new java.awt.Color(102, 102, 102));
    tblLenDist.setSelectionForeground(new java.awt.Color(153, 204, 255));
    tblLenDist.getTableHeader().setResizingAllowed(false);
    tblLenDist.getTableHeader().setReorderingAllowed(false);
    scrlLenDist.setViewportView(tblLenDist);
    tblLenDist.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    lengthDistTabs.addTab("Waiting For Input", scrlLenDist);

    lblLenDist.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    lblLenDist.setForeground(new java.awt.Color(255, 255, 255));
    lblLenDist.setText("Length Distribution (for remaining sequences):");

    inputStatsTabs.setBackground(new java.awt.Color(120, 120, 120));
    inputStatsTabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

    tblExecutionStats.setAutoCreateColumnsFromModel(false);
    tblExecutionStats.setBackground(new java.awt.Color(120, 120, 120));
    tblExecutionStats.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
    tblExecutionStats.setForeground(new java.awt.Color(255, 255, 255));
    tblExecutionStats.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {

      }
    ));
    tblExecutionStats.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    tblExecutionStats.setCellSelectionEnabled(true);
    tblExecutionStats.setFillsViewportHeight(true);
    tblExecutionStats.setGridColor(new java.awt.Color(153, 204, 255));
    tblExecutionStats.setRowHeight(25);
    tblExecutionStats.setRowMargin(10);
    tblExecutionStats.setSelectionBackground(new java.awt.Color(102, 102, 102));
    tblExecutionStats.setSelectionForeground(new java.awt.Color(153, 204, 255));
    scrlExecutionStats.setViewportView(tblExecutionStats);
    tblExecutionStats.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    inputStatsTabs.addTab("Waiting For Input", scrlExecutionStats);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lengthDistTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 836, Short.MAX_VALUE)
          .addComponent(inputStatsTabs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lblExecutionStats)
          .addComponent(lblLenDist))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblExecutionStats)
        .addGap(3, 3, 3)
        .addComponent(inputStatsTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lblLenDist)
        .addGap(7, 7, 7)
        .addComponent(lengthDistTabs, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    jScrollPane1.setViewportView(jPanel1);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTabbedPane inputStatsTabs;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel lblExecutionStats;
  private javax.swing.JLabel lblLenDist;
  private javax.swing.JTabbedPane lengthDistTabs;
  private javax.swing.JScrollPane scrlExecutionStats;
  private javax.swing.JScrollPane scrlLenDist;
  private javax.swing.JTable tblExecutionStats;
  private javax.swing.JTable tblLenDist;
  // End of variables declaration//GEN-END:variables

  public void fillExecutionStatsTable( String title, List<FilterStage> statistics )
  {
    int componentIndex = 0;
    for(int i = 0; i < inputStatsTabs.getTabCount(); i++ )
    {
      if(inputStatsTabs.getTitleAt( i).equals( title))
      {
        componentIndex = i;
        break;
      }
    }
    JTable view = (JTable)((JScrollPane)inputStatsTabs.getComponentAt( componentIndex)).getViewport().getView();
    resetExecutionStatsTable( (DefaultTableModel)view.getModel() );
    

    if ( statistics == null || statistics.isEmpty() )
    {
      return;
    }
    
    view.setColumnModel( createExecutionColumnModel( statistics ) );

    DefaultTableModel model = new DefaultTableModel();
    model.addColumn( "" );
    
    for( FilterStage stage : statistics )
    {
      model.addColumn( stage.getShortStageName() );
    }
    
    model.addRow( createRow(model, "Total Reads:") );
    model.addRow( createRow(model, "Distinct Reads:") );  
    
    view.setModel( model );
    ((JScrollPane)inputStatsTabs.getComponentAt( componentIndex)).setViewportView(view);
    
    // Order is as follows: Input, 3' matches, 5' matches, Invalid length, Seq remaining
    int i = 1;
    for ( FilterStage data : statistics )
    {
      String totalReadCount = data.getTotalReadCount() > 0 ? 
        Integer.toString(data.getTotalReadCount()) : "N/A";
      String distinctReadCount = data.getDistinctReadCount() > 0 ? 
        Integer.toString(data.getDistinctReadCount()) : "N/A";
      view.setValueAt( totalReadCount, 0, i);
      view.setValueAt( distinctReadCount, 1, i );
      i++;
    }
  }

 
}
