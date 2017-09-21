/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RowModel;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;

/**
 *
 * @author w0445959
 */
public class CoLIDETableRowModel implements RowModel
{
  private List<String> columns;
  
  public CoLIDETableRowModel()
  {
    this(0);
    
    
//    locusData.add( "Start: " + currentMinCoord);
//        locusData.add( "End: " + currentMaxCoord);
//        locusData.add( "Length : " + (currentMaxCoord - currentMinCoord ) );
    
    

//      for ( File f : input_files )
//      {
//        addColumn( f.getName() + LINE_SEPARATOR + "Raw" );
//        addColumn( f.getName() + LINE_SEPARATOR + "Weighted" );
//        addColumn( f.getName() + LINE_SEPARATOR + "Norm" );
//        addColumn( f.getName() + LINE_SEPARATOR + "Seqs" );
//      }
  }
  public CoLIDETableRowModel(int sampleCount)
  {
    this.columns = CollectionUtils.newArrayList();
    
//    locusData.add( "Start: " + currentMinCoord);
//        locusData.add( "End: " + currentMaxCoord);
//        locusData.add( "Length : " + (currentMaxCoord - currentMinCoord ) );
    
    addColumn("Start");
    addColumn("End");
    addColumn("Length");
    addColumn("P_val");
    
    
    
   
    //addColumn("Expression Series");

    int i = 1;
    while (i <= sampleCount )
    {
      addColumn("Sample " + i + " expression " );
      i++;
    }
    
    addColumn("Chromosome Number");
    addColumn("Differential Expression");
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
