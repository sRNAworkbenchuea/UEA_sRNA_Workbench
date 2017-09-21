/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 * Class for simple Thread utlities
 *
 * @author prb07qmu
 */
public class ThreadUtils
{
  /***
   * Send thread to sleep and silence a {@link InterruptedException} if required
   *
   * @param millis
   */
  public static void safeSleep( int millis )
  {
    try
    {
      Thread.sleep( (long)millis );
    }
    catch ( InterruptedException ex )
    {
      // shhh
    }
  }
}
