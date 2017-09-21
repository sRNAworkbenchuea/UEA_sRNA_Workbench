/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

/**
 *
 * @author prb07qmu
 */
interface FeedbackI
{
  void setStatusText( String text );
  void addProgressMessage( String text, boolean append );
  void done( boolean success );
}
