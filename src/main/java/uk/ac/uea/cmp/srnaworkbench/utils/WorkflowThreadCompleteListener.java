/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 * Used by a class containing a client thread that needs to be notified when that
 * thread has completed executing
 * @author MBS
 */
public interface WorkflowThreadCompleteListener 
{
  /**
   * Executed when client thread has finished executing.  Can be used for updating
   * GUI with results from client thread.
     * @param ID
   */  
  void notifyOfThreadCompletion(String ID);
}
