/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 *
 * @author Matt
 */
@Deprecated
public class GoToHairpinMenu extends JMenuItem
{
    MouseEvent myMouseEvent;

    JTabbedPane myTabPane;
    HashMap<String, Integer> myCaretPositions;
    JTable myHairpinTable;
    
    public GoToHairpinMenu(String menuText)
    {
        super(menuText);
        
        this.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToHairpinEvent(evt);
            }
        });
    }
    
    public void addTableRef(MouseEvent e, JTabbedPane mirCatTabControl, HashMap<String, Integer> caretPositionForHairpins, JTable hairpinText, Container parent)
    {

        myCaretPositions = caretPositionForHairpins;
        myTabPane = mirCatTabControl;
        myHairpinTable = hairpinText;
        myMouseEvent = e;
    }
    
    public void goToHairpinEvent(java.awt.event.ActionEvent evt)
    {
           JTable source = (JTable) myMouseEvent.getSource();

         int row = source.rowAtPoint(myMouseEvent.getPoint());
        int column = source.columnAtPoint(myMouseEvent.getPoint());  
        if (source.getModel().getRowCount() >= row)
        {
            //System.out.println("row: " + row + " col: " + column + " caret size: " + myCaretPositions.size());

            //RowSorter temp = source.getRowSorter().getModel().getValueAt(row, column);
            row = source.convertRowIndexToModel(row);

            String keyIndex = source.getModel().getValueAt(row, 0)+ "/"+source.getModel().getValueAt(row, 1) + "/" + source.getModel().getValueAt(row, 2);
            //System.out.println("key: " + keyIndex + " caret: " + myCaretPositions.get(keyIndex));
            //System.out.println(source.getModel().getValueAt(row, column));

            myTabPane.setSelectedIndex(1);
            myHairpinTable.changeSelection(myCaretPositions.get(keyIndex), 0, false, false);
            //myTextWindow.setCaretPosition(myCaretPositions.get(keyIndex));
            //guiOutputTextPane.setCaretPosition(len);
        }


    }
}
