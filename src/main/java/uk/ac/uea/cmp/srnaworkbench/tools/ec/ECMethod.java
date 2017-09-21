/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

/**
 *
 * @author ezb11yfu
 */
public abstract class ECMethod
{
  // Input vars
  protected final double[] x;
  protected final double[] y;
  
  /**
   * Creates a new ECMethod object, containing the array of expression levels for
   * X and Y respectively.
   * @param x double[] containing the expression levels for all entries in X.
   * @param y double[] containing the expression levels for all entries in Y.
   */
  protected ECMethod(final double[] x, final double[] y)
  {
    if (x == null || y == null )
      throw new IllegalArgumentException("X and Y must be non-null.");
    
    if ( x.length == 0 || y.length == 0 )
      throw new IllegalArgumentException("X and Y must both contain at least one element");
    
    if ( x.length != y.length )
      throw new IllegalArgumentException("X and Y must both be the same size");
    
    this.x = x;
    this.y = y;
  }
  
  /**
   * Calculate the similarity of expression profiles from the two expression level
   * arrays provided in the constructor.
   * @return A RealMatrix containing the correlation distances between each entry in
   * x and each entry in y.
   */
  public abstract double evaluate();
}
