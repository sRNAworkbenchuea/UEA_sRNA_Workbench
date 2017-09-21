/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers;

import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author w0445959
 */
public class MultilineCellRenderer extends JEditorPane
        implements TableCellRenderer
{

    private final DefaultTableCellRenderer adaptee =
            new DefaultTableCellRenderer();
    /** map from table to map of rows to map of column heights */
    private final Map< JTable, Map< Integer, Map< Integer, Integer > > > cellSizes = CollectionUtils.newHashMap();
    private int targetRow, targetCol;

    public MultilineCellRenderer()
    {
        //setLineWrap(true);
        //setWrapStyleWord(true);
        setContentType("text/html");
        setEditable(false);
        targetRow = -1;
        targetCol = -1;

        //this.setMinimumSize(new Dimension(1,1));
        setForeground(new java.awt.Color(255, 255, 255));
    }

    @Override
    public Component getTableCellRendererComponent(//
            JTable table, Object obj, boolean isSelected,
            boolean hasFocus, int row, int column)
    {

        //setContentType( "text/plain" );
        // set the colours, etc. using the standard for that platform
//    adaptee.getTableCellRendererComponent(table, obj,
//        isSelected, hasFocus, row, column);
//    setForeground(adaptee.getForeground());
//    setBackground(adaptee.getBackground());
//    setBorder(adaptee.getBorder());
//    setFont(adaptee.getFont());
        //setText(obj.toString());

//        if(obj.toString().contains("<HTML>"))
//        {
//            setContentType( "text/html" );
//        }
//        else
//            setContentType( "text/plain" );
        //this.getStyledDocument();
        TableColumnModel columnModel = table.getTableHeader().getColumnModel();
        //System.out.println("size: " + columnModel.getColumn(column).getMinWidth());
        setSize(columnModel.getColumn(column).getWidth(), Integer.MAX_VALUE);
        
        int height_wanted = (int) getPreferredScrollableViewportSize().getHeight();
        
        String finalOutput = obj.toString();
        
        if(finalOutput.contains("<") && finalOutput.contains("-"))
        {
            finalOutput = finalOutput.replaceAll("<", "&lt;");
        }
        if(finalOutput.contains(">")&& finalOutput.contains("-"))
        {
            finalOutput = finalOutput.replaceAll(">", "&gt;");
        }
        //System.out.println("Rendering: " + finalOutput);
         if (!finalOutput.contains("<HTML>"))
        {
            setText("<HTML><font color=#FFFFFF>" + finalOutput + "</font></HTML>" );
        }
        else
        {
            setText(finalOutput);
        }
        if (isSelected)
        {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        }
        else
        {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        if (row == targetRow && column == targetCol)
        {
            setBorder(BorderFactory.createLineBorder(Color.red));
            setFont(table.getFont().deriveFont(Font.BOLD));
        }
        else
        {
            setBorder(table.getBorder());
            setFont(table.getFont());
        }

       // System.out.println("Contains: " + this.getText());


        // This line was very important to get it working with JDK1.4
        

        addSize(table, row, column, height_wanted);
        height_wanted = findTotalMaximumRowSize(table, row) + 10;
        if (height_wanted != table.getRowHeight(row))
        {
            table.setRowHeight(row, height_wanted);
            
        }

       
//
//
//
//        table.revalidate();
//        table.repaint();

        return this;
    }

    public void setTargetCell(int row, int col)
    {
        targetRow = row;
        targetCol = col;
    }

    private void addSize(JTable table, int row, int column, int height)
    {
        Map< Integer, Map< Integer, Integer > > rows = cellSizes.get(table);
        if (rows == null)
        {
            rows = CollectionUtils.newHashMap();
            cellSizes.put( table, rows );
        }

        Map< Integer, Integer > rowheights = rows.get( row );
        if (rowheights == null)
        {
            rowheights = CollectionUtils.newHashMap();
            rows.put( row, rowheights );
        }
        rowheights.put( column, height );
    }

    /**
     * Look through all columns and get the renderer.  If it is
     * also a TextAreaRenderer, we look at the maximum height in
     * its hash table for this row.
     */
    private int findTotalMaximumRowSize(JTable table, int row)
    {
        int maximum_height = 0;
        Enumeration columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements())
        {
            TableColumn tc = (TableColumn) columns.nextElement();
            TableCellRenderer cellRenderer = tc.getCellRenderer();
            if (cellRenderer instanceof MultilineCellRenderer)
            {
                MultilineCellRenderer tar = (MultilineCellRenderer) cellRenderer;
                maximum_height = Math.max(maximum_height,
                        tar.findMaximumRowSize(table, row));
            }
        }
        return maximum_height;
    }

    private int findMaximumRowSize(JTable table, int row)
    {
        Map rows = (Map) cellSizes.get(table);
        if (rows == null)
        {
            return 0;
        }
        Map rowheights = (Map) rows.get(new Integer(row));
        if (rowheights == null)
        {
            return 0;
        }
        int maximum_height = 0;
        for (Iterator it = rowheights.entrySet().iterator();
                it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            int cellHeight = ((Integer) entry.getValue()).intValue();
            maximum_height = Math.max(maximum_height, cellHeight);
        }
        return maximum_height;
    }
}
//    public MultilineCellRenderer() {
//      setLineWrap(true);
//      setWrapStyleWord(true);
//    }
//
//    public Component getTableCellRendererComponent(JTable jTable,
//        Object obj, boolean isSelected, boolean hasFocus, int row,
//        int column) {
//      setText((String)obj);
//      return this;
//    }

