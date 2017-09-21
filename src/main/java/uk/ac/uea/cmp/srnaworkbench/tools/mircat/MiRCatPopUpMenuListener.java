/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
//import javax.swing.JTextPane;

/**
 *
 * @author w0445959
 */
public class MiRCatPopUpMenuListener extends MouseAdapter{
    JTabbedPane tabControl;
    HashMap<String, Integer> myCarets;
    JTable hairpinTextWindow;
    Container mirCATParent = null;
    File myGenomeFile = null;
    public MiRCatPopUpMenuListener(Container parent)
    {
       
        mirCATParent=  parent;
    }
    public MiRCatPopUpMenuListener(Container parent, File genomeFile)
    {
       myGenomeFile = genomeFile;
        mirCATParent=  parent;
    }
    @Override
    public void mousePressed(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            doPop(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            doPop(e);
        }
    }

    private void doPop(MouseEvent e)
    {
        JTable source = (JTable) e.getSource();
        int row = source.rowAtPoint(e.getPoint());
        int column = source.columnAtPoint(e.getPoint());

        //System.out.println("row count: " + source.getModel().getRowCount() + " row: " + row);
        if (!source.isRowSelected(row))
        {
            source.changeSelection( row, column, false, false);
            
        }
       
        
//        MiRCaTPopUpMenu menu = new MiRCaTPopUpMenu(
//                new RenderHairpinMenu("Render Hairpin"),
//                e, mirCATParent);
         MiRCaTPopUpMenu menu = new MiRCaTPopUpMenu(
                new RenderHairpinMenu("Render Hairpin"),
                 new GenerateSingleGFFMenu("Show Genome View"),
                 myGenomeFile,
                e, mirCATParent);
        menu.show(e.getComponent(), e.getX(), e.getY());



        
       
    }

}
