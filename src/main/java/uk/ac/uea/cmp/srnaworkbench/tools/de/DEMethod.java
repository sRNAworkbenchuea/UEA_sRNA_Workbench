/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import org.apache.commons.math.linear.RealMatrix;

/**
 *
 * @author ezb11yfu
 */
public abstract class DEMethod
{
  // Input vars
  protected final RealMatrix data;
  
  /**
   * Creates a new DEMethod object containing a RealMatrix of numbers describing
   * expression levels for an element across multiple samples.
   * @param data RealMatrix  describing expression levels for an element across
   * multiple samples.
   */
  protected DEMethod(RealMatrix data)
  {
    if (data == null || data.getColumnDimension() < 2 || data.getRowDimension() == 0)
      throw new IllegalArgumentException("Must have data with at least 2 columns and 1 row to work with.");
    
    this.data = data;
  }
  
  /**
   * Calculate the differential expression levels from the data provided in the 
   * constructor.
   * @return A double array containing the differential expression levels.
   */
  public abstract double[] evaluate();
}
