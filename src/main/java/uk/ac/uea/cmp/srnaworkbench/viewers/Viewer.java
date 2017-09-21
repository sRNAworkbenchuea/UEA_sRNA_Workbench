/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.viewers;

import javax.swing.JInternalFrame;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;

/**
 * Abstract class for an InternalFrame that implements GUIInterface, allowing it
 * to be added as a GUI Tool to the Workbench
 * @author Matt
 */
public abstract class Viewer extends JInternalFrame implements GUIInterface
{
  
  public static void addViewer( final Viewer guiTool )
  {
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        // Get this instance's desktop pane
        MDIDesktopPane pane = AppUtils.INSTANCE.getMDIDesktopPane();

        // Check the pane exists
        if ( pane != null )
        {
          // Add the viewer to the desktop pane
          pane.add( guiTool );
        }

        ToolManager.getInstance().addTool( guiTool );
      }
    } );

  } 
  
}
