/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.swing.customcomponentrenderers;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;


/**
 *
 * @author w0445959
 */
public class MultiLineHeaderRenderer<E extends String> extends JList implements TableCellRenderer
{

    public MultiLineHeaderRenderer()
    {
        setOpaque(true);
        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        ListCellRenderer renderer = getCellRenderer();
        ((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
        setCellRenderer(renderer);
    }

    @Override
  @SuppressWarnings({"UseOfObsoleteCollectionType", "unchecked"})
     
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        setFont(table.getFont());
        String str = (value == null) ? "" : value.toString() ;
        BufferedReader br = new BufferedReader(new StringReader(str));
        E line;
        Vector<E> v = new Vector<E>();
        try
        {
          
            while ((line = (E) br.readLine()) != null)
            {
              
                v.addElement(line);
            }
        }
        catch (IOException ex)
        {
          LOGGER.log(Level.SEVERE, ex.getMessage());
            
        }
        setListData(v);
        return this;
    }
}