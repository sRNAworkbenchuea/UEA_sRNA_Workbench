/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.filter;

import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import uk.ac.uea.cmp.srnaworkbench.swing.RotatingIcon;

/**
 * Used to display results from the filter tool to the host frame.
 * @author Matthew Stocks and Dan Mapleson
 */
public final class FilterResultsPanel extends javax.swing.JPanel
{
  private boolean firstTab = true;
  /** Creates new form FilterResutlsPanel */
  public FilterResultsPanel()
  {
    initComponents();

    resetTable();
  }

  public void resetTable()
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        for ( int i = 0; i < resultsTabPane.getTabCount(); i++ )
        {
          resultsTabPane.setTitleAt( i, "Waiting for Input" );
          JTable view = (JTable) ( (JScrollPane) resultsTabPane.getComponentAt( i ) ).getViewport().getView();
          resetTable( (DefaultTableModel) view.getModel() );
        }
        for ( int i = 1; i < resultsTabPane.getTabCount(); i++ )
        {
          resultsTabPane.remove( i );
        }
      }
    } );
  }
  private void resetTable(DefaultTableModel model)
  {
    for ( int i = 0; i < model.getRowCount(); i++ )
    {
      for ( int j = 0; j < model.getColumnCount(); j++ )
      {
        model.setValueAt( "N/A", i, j );
      }
    }
    model.setValueAt( "Total Reads: ", 0, 0 );
    model.setValueAt( "Distinct Reads: ", 1, 0 );
  }

  public void addOutputTab(String name)
  {
    
    if(firstTab)
    {
      firstTab = false;
      this.resultsTabPane.setTitleAt( 0, name );
      
    }
    else
    {
      JScrollPane newResultsScroll = new javax.swing.JScrollPane();
      JTable  newResultsTable = new javax.swing.JTable();

      newResultsTable.setBackground(new java.awt.Color(120, 120, 120));
      newResultsTable.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
      newResultsTable.setForeground(new java.awt.Color(255, 255, 255));
      newResultsTable.setModel(new javax.swing.table.DefaultTableModel(
          new Object [][] {
              {null, null, null, null, null, null, null, null},
              {null, null, null, null, null, null, null, null}
          },
          new String [] {
              "", "Input", "Seq. Length", "Complexity", "Abundance", "Invalid", "t/rRNA", "Genome"
          }
      ) {
          Class[] types = new Class [] {
              java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
          };
          boolean[] canEdit = new boolean [] {
              false, false, false, false, false, false, false, false
          };

          public Class getColumnClass(int columnIndex) {
              return types [columnIndex];
          }

          public boolean isCellEditable(int rowIndex, int columnIndex) {
              return canEdit [columnIndex];
          }
      });
      newResultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
      newResultsTable.setCellSelectionEnabled(true);
      newResultsTable.setFillsViewportHeight(true);
      newResultsTable.setGridColor(new java.awt.Color(153, 204, 255));
      newResultsTable.setRowHeight(25);
      newResultsTable.setRowMargin(10);
      newResultsTable.setSelectionBackground(new java.awt.Color(102, 102, 102));
      newResultsTable.setSelectionForeground(new java.awt.Color(153, 204, 255));
      newResultsScroll.setViewportView(newResultsTable);
      newResultsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      newResultsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
      newResultsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
      newResultsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
      DefaultTableModel model = (DefaultTableModel)newResultsTable.getModel();
      for ( int i = 0; i < model.getRowCount(); i++ )
      {
        for ( int j = 0; j < model.getColumnCount(); j++ )
        {
          model.setValueAt( "N/A", i, j );
        }
      }
      model.setValueAt( "Total Reads: ", 0, 0 );
      model.setValueAt( "Distinct Reads: ", 1, 0 );

      this.resultsTabPane.addTab( name, new RotatingIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/ProgressSmall.gif")), 
              resultsTabPane), 
        newResultsScroll );
      
        
    }
  }
  public void setResultsWaiting(String title, boolean waiting)
  {
    int componentIndex = 0;
    for(int i = 0; i < this.resultsTabPane.getTabCount(); i++ )
    {
      String titleAt = resultsTabPane.getTitleAt( i );
      if ( titleAt.equals( title ) )
      {
        componentIndex = i;
        if ( waiting )
        {
          this.resultsTabPane.setIconAt( componentIndex,
            new RotatingIcon( new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/ProgressSmall.gif" ) ), resultsTabPane ) );
        }
        else
        {
          this.resultsTabPane.setIconAt( componentIndex, new javax.swing.ImageIcon( getClass().getResource( "/images/SharedImages/completeSmall.jpeg" ) ) );
        }

      }
    }
    
  }
  public void fillResultsTable( String title, ArrayList<FilterStage> filterStages )
  {
    //resetTable();
    
    int componentIndex = 0;
    for(int i = 0; i < resultsTabPane.getTabCount(); i++ )
    {
      if(resultsTabPane.getTitleAt( i).equals( title))
      {
        componentIndex = i;
        break;
      }
    }
    JTable view = (JTable)((JScrollPane)resultsTabPane.getComponentAt( componentIndex)).getViewport().getView();
    //resetTable( (DefaultTableModel)view.getModel() );
    

    for ( FilterStage stage : filterStages )
    {
      if ( stage.getStageName().equals( "input" ) )
      {
        view.setValueAt( stage.getTotalReadCount(), 0, 1 );
        view.setValueAt( stage.getDistinctReadCount(), 1, 1 );
      }
      else
      {
        if ( stage.getStageName().equals( "filter by sequence length" ) )
        {
          view.setValueAt( stage.getTotalReadCount(), 0, 2 );
          view.setValueAt( stage.getDistinctReadCount(), 1, 2 );
        }
        else
        {
          if ( stage.getStageName().equals( "filter low-complexity sequences" ) )
          {
            view.setValueAt( stage.getTotalReadCount(), 0, 3 );
            view.setValueAt( stage.getDistinctReadCount(), 1, 3 );
          }
          else
          {
            if ( stage.getStageName().equals( "filter by max-abundance" )
              || stage.getStageName().equals( "filter by min-abundance" )
              || stage.getStageName().equals( "filter by min-abundance and max-abundance" ) )
            {
              view.setValueAt( stage.getTotalReadCount(), 0, 4 );
              view.setValueAt( stage.getDistinctReadCount(), 1, 4 );

            }
            else
            {
              if ( stage.getStageName().equals( "filter invalid sequences" ) )
              {
                view.setValueAt( stage.getTotalReadCount(), 0, 5 );
                view.setValueAt( stage.getDistinctReadCount(), 1, 5 );
              }
              else
              {
                if ( stage.getStageName().equals( "filter by t/rRNA (matches out)" ) )
                {
                  view.setValueAt( stage.getTotalReadCount(), 0, 6 );
                  view.setValueAt( stage.getDistinctReadCount(), 1, 6 );
                }
                else
                {
                  if ( stage.getStageName().equals( "filter by genome (matches in)" ) )
                  {
                    view.setValueAt( stage.getTotalReadCount(), 0, 7 );
                    view.setValueAt( stage.getDistinctReadCount(), 1, 7 );
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  public void fillResultsTable( ArrayList<FilterStage> filterStages )
  {
    resetTable();

    for ( FilterStage stage : filterStages )
    {
      if ( stage.getStageName().equals( "input" ) )
      {
        resultsTable.setValueAt( stage.getTotalReadCount(), 0, 1 );
        resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 1 );
      }
      else
      {
        if ( stage.getStageName().equals( "filter by sequence length" ) )
        {
          resultsTable.setValueAt( stage.getTotalReadCount(), 0, 2 );
          resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 2 );
        }
        else
        {
          if ( stage.getStageName().equals( "filter low-complexity sequences" ) )
          {
            resultsTable.setValueAt( stage.getTotalReadCount(), 0, 3 );
            resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 3 );
          }
          else
          {
            if ( stage.getStageName().equals( "filter by max-abundance" )
              || stage.getStageName().equals( "filter by min-abundance" )
              || stage.getStageName().equals( "filter by min-abundance and max-abundance" ) )
            {
              resultsTable.setValueAt( stage.getTotalReadCount(), 0, 4 );
              resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 4 );

            }
            else
            {
              if ( stage.getStageName().equals( "filter invalid sequences" ) )
              {
                resultsTable.setValueAt( stage.getTotalReadCount(), 0, 5 );
                resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 5 );
              }
              else
              {
                if ( stage.getStageName().equals( "filter by t/rRNA (matches out)" ) )
                {
                  resultsTable.setValueAt( stage.getTotalReadCount(), 0, 6 );
                  resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 6 );
                }
                else
                {
                  if ( stage.getStageName().equals( "filter by genome (matches in)" ) )
                  {
                    resultsTable.setValueAt( stage.getTotalReadCount(), 0, 7 );
                    resultsTable.setValueAt( stage.getDistinctReadCount(), 1, 7 );
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resultsTabPane = new javax.swing.JTabbedPane();
        resultsScroll = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();

        resultsTabPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        resultsTable.setBackground(new java.awt.Color(120, 120, 120));
        resultsTable.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 11)); // NOI18N
        resultsTable.setForeground(new java.awt.Color(255, 255, 255));
        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "", "Input", "Seq. Length", "Complexity", "Abundance", "Invalid", "t/rRNA", "Genome"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        resultsTable.setCellSelectionEnabled(true);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setGridColor(new java.awt.Color(153, 204, 255));
        resultsTable.setRowHeight(25);
        resultsTable.setRowMargin(10);
        resultsTable.setSelectionBackground(new java.awt.Color(102, 102, 102));
        resultsTable.setSelectionForeground(new java.awt.Color(153, 204, 255));
        resultsScroll.setViewportView(resultsTable);
        resultsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        resultsTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        resultsTabPane.addTab("Waiting for input...", resultsScroll);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(resultsTabPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(resultsTabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane resultsScroll;
    private javax.swing.JTabbedPane resultsTabPane;
    private javax.swing.JTable resultsTable;
    // End of variables declaration//GEN-END:variables
}
