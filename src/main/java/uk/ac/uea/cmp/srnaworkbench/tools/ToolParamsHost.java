/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

/**
 *
 * @author ezb11yfu
 */
public interface ToolParamsHost
{
  /**
   * Gets the tool parameters currently set in the host GUI
   * @return The current tool parameters.
   */
  public ToolParameters getParams();
  
  /**
   * Used to update a params host GUI after parameters have been loaded.
   */
  public void update(ToolParameters params);
  
  /**
   * Requests that host opens an error dialog with the given message
   * @param message Error message to display to user
   */
  public void showErrorDialog(String message);
}
