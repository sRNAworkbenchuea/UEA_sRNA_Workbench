
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 */
class PARESnipJTableCellRenderer extends JTextArea implements TableCellRenderer
{
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
    {
        // 'value' is value contained in the cell located at
        // (rowIndex, vColIndex)

        setBackground( isSelected ? table.getSelectionBackground() : table.getBackground() );
        setForeground( isSelected ? table.getSelectionForeground() : table.getForeground() );

        if(hasFocus) {
            // this cell is the anchor and the table has the focus
        }
        // Configure the component with the specified value
        if(value == null){
            setText("");
        }else{
            setText(value.toString());
        }
        // Set tool tip if desired
        this.setVisible(true);
        this.setPreferredSize(new Dimension(40,260));
        this.setEditable(false);
        this.setFont(Font.decode("monospaced-11"));
        // Since the renderer is a component, return itself
        return this;
    }

}
