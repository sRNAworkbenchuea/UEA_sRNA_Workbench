/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;

/**
 *
 * @author w0445959
 */
public class CoLIDERenderDataProvider extends JTextArea implements RenderDataProvider, TableCellRenderer
{
  private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
  public CoLIDERenderDataProvider()
  {
    setLineWrap( true );
    setWrapStyleWord( true );
    setOpaque( true );
  }

  @Override
  public java.awt.Color getBackground( Object o )
  {
    return null;
  }

  @Override
  public String getDisplayName( Object o )
  {
    return o.toString();
  }

  @Override
  public java.awt.Color getForeground( Object o )
  {if (((OutlineNode)o).isLeaf()) {
            return UIManager.getColor("controlShadow");
        }
        return null;
  }

  @Override
  public javax.swing.Icon getIcon( Object o )
  {
    if(!((OutlineNode)o).isLeaf())
            return new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/tree.png"));
            
        return null;
  }

  @Override
  public String getTooltipText( Object o )
  {
    return o.toString();
  }

  @Override
  public boolean isHtmlDisplayName( Object o )
  {
    return false;
  }

  @Override
 public Component getTableCellRendererComponent(
    JTable table, Object value, boolean isSelected, boolean hasFocus,
    int row, int column )
  {
    if ( isSelected )
    {
      setForeground( table.getSelectionForeground() );
      setBackground( table.getSelectionBackground() );
    }
    else
    {
      setForeground( table.getForeground() );
      setBackground( table.getBackground() );
    }
    setFont( table.getFont() );
    if ( hasFocus )
    {
      setBorder( UIManager.getBorder( "Table.focusCellHighlightBorder" ) );
      if ( table.isCellEditable( row, column ) )
      {
        setForeground( UIManager.getColor( "Table.focusCellForeground" ) );
        setBackground( UIManager.getColor( "Table.focusCellBackground" ) );
      }
    }
    else
    {
      setBorder( new EmptyBorder( 1, 2, 1, 2 ) );
    }
    if ( value != null )
    {
      setText( value.toString() );
    }
    else
    {
      setText( "" );
    }
    //adjustRowHeight( (Outline)table, row, column );
    return this;
  }
  
  private void adjustRowHeight( Outline table, int row, int column )
  {
    //The trick to get this to work properly is to set the width of the column to the 
    //textarea. The reason for this is that getPreferredSize(), without a width tries 
    //to place all the text in one line. By setting the size with the with of the column, 
    //getPreferredSize() returnes the proper height which the row should have in
    //order to make room for the text.
    int cWidth = table.getTableHeader().getColumnModel().getColumn( column ).getWidth();
    setSize( new Dimension( cWidth, 1000 ) );
    int prefH = getPreferredSize().height;
    while ( rowColHeight.size() <= row )
    {
      rowColHeight.add( new ArrayList<Integer>( column ) );
    }
    List<Integer> colHeights = rowColHeight.get( row );
    while ( colHeights.size() <= column )
    {
      colHeights.add( 0 );
    }
    colHeights.set( column, prefH );
    int maxH = prefH;
    for ( Integer colHeight : colHeights )
    {
      if ( colHeight > maxH )
      {
        maxH = colHeight;
      }
    }
    if ( table.getRowHeight( row ) != maxH )
    {
      //System.out.println( "prevent here??" );
      table.setRowHeight( row, maxH );
    }
  }
}
