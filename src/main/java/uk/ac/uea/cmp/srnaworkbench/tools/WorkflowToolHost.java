/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

/**
 * An interface to be implemented by GUIs which use a ToolRunner to do heavy
 * processing in a separate thread.
 * @author MBS
 */
public interface WorkflowToolHost
{

  /**
   * Used to update a host GUI after procedure has executed.  Implementor should 
   * handle any Exceptions inside this function, and notify user using the showErrorDialog
   * method.
     * @param id the name of the module
   */
  public void update(String id);
  
  /**
   * Informs host of whether the tool is currently running or not
   * @param running true if currently running, otherwise false
   */
  public void setRunningStatus(boolean running);
  
  /**
   * Requests that host opens an error dialog with the given message
   * @param message Error message to display to user
   */
  public void showErrorDialog(String message);
  
    /**
     * Requests that host opens an error dialog with the given message
     *
     * @param ex Exception to be displayed including an expandable stack trace
     */
    public void showErrorDialog(Exception ex);
}
