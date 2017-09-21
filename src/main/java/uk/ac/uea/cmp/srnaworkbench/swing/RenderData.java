/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.swing;

import javax.swing.UIManager;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author w0445959
 */
public class RenderData implements RenderDataProvider {


    @Override
    public java.awt.Color getBackground(Object o) {
        return null; //new java.awt.Color(120, 120, 120);
    }

    @Override
    public String getDisplayName(Object o) {
        return o.toString();
    }

    @Override
    public java.awt.Color getForeground(Object o) {

        if (((OutlineNode)o).isLeaf()) {
            return UIManager.getColor("controlShadow");
        }
        return null;
        
         //return  null; //new java.awt.Color(255, 255, 255);
    }

    @Override
    public javax.swing.Icon getIcon(Object o) {
        //System.out.println("class: " + o.getClass());
        if(!((OutlineNode)o).isLeaf())
            return new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/tree.png"));
            
        return null;

    }

    @Override
    public String getTooltipText(Object o) {
        // o;
        return o.toString();
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }

}