/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench;

import javax.swing.JPanel;

/**
 *
 * @author Matt Stocks and Dan Mapleson
 */
public interface GUIInterface
{
  /**
   * Instructs the tool to start executing its main task.
   */
  public void runProcedure();

  /**
   * Some tools may make use of a parameter panel on the side of the screen.  This
   * method retrieves that panel.
   * @return The parameter browser panel for this tool.
   */
  public JPanel getParamsPanel();

  /**
   * Sets a flag in a tool that indicates it is currently displaying the parameter
   * browser.
   * @param newState Whether the tool is currently showing the parameter browser
   * or not.
   */
  public void setShowingParams( boolean newState );

  /**
   * Retrieves a flag indicating whether the parameter browser is currently being
   * displayed to the user
   * @return true if parameter browser is currently being displayed, otherwise false
   */
  public boolean getShowingParams();

  /**
   * calls any methods that require shutting the tool down
   */
  public void shutdown();
}
