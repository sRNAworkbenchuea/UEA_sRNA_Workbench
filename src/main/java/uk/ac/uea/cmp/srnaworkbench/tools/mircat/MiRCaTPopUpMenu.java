/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Container;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JPopupMenu;

/**
 *
 * @author w0445959
 */
public class MiRCaTPopUpMenu extends JPopupMenu {

  
    
    public MiRCaTPopUpMenu(
    RenderHairpinMenu renderHairpin, MouseEvent e, 
    Container parent){

      
        renderHairpin.addTableRef(e, parent);
        add(renderHairpin);
    }
    
    public MiRCaTPopUpMenu(
    RenderHairpinMenu renderHairpin,
    GenerateSingleGFFMenu loadSeqVis,
    File genomeFile,
    MouseEvent e, 
    Container parent){

        renderHairpin.addTableRef(e, parent);
        loadSeqVis.addTableRef(e, parent, genomeFile);
        add(renderHairpin);
        add(loadSeqVis);
    }
    
   
    
    
   
    
}
