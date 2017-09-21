/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.awt.Component;
import java.awt.Cursor;

/**
 *
 * @author w0445959
 */
public class GenerateWaitCursor implements Runnable
{

    private Component parentTool = null;
    public GenerateWaitCursor(Component myParent)
    {
        parentTool = myParent;
    }
    @Override
    public void run()
    {
         parentTool.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
}
