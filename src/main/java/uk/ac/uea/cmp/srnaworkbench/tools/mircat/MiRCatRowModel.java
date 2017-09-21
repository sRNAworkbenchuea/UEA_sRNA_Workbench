/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RowModel;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;

/**
 *
 * @author w0445959
 */
public class MiRCatRowModel implements RowModel
{
  private List<String> columns;
  

  public MiRCatRowModel()
  {
    this.columns = CollectionUtils.newArrayList();
    addColumn( "Chromosome" );
    addColumn( "Start" );
    addColumn( "Stop" );
    addColumn( "Mean Count" );
    addColumn( "Max Difference" );
    addColumn( "Locus Length" );
  }

  @Override
  public int getColumnCount()
  {
    return columns.size();
  }
  
  
  public void addColumn(String name)
    {
        this.columns.add(name);
    }

  @Override
  public Object getValueFor( Object o, int i )
  {
    @SuppressWarnings("unchecked")
    List<String> f = (List<String>) ( ( (DefaultMutableTreeNode) o ).getUserObject() );

    // Skip the first column
    int nextColumn = i + 1;

    if ( f.size() > 1 && nextColumn < f.size() )
    {
      return f.get( nextColumn );
    }
    else
    {
      return "";
    }
  }

  @Override
  public Class getColumnClass( int i )
  {
    return String.class;
  }

  @Override
  public boolean isCellEditable( Object o, int i )
  {
    return false;
  }

  @Override
  public void setValueFor( Object o, int i, Object o1 )
  {
    
  }

  @Override
  public String getColumnName( int i )
  {
     return columns.get(i);
  }
  
}
