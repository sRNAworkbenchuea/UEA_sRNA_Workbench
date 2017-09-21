/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 *
 * @author prb07qmu
 */
public interface AnnotationTextI
{
  /***
   * Returns text which can be different from toString()
   *
   * @return Text string
   */
  public String getBasicText();

  /***
   * Returns text as HTML
   *
   * @return Text string with HTML markups
   */
  public String getHTMLText();
}
