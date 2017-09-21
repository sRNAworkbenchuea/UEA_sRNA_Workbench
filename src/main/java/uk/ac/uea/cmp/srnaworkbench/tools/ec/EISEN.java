/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;

/**
 * Cosine correlation distance
 *
 * @author ezb11yfu
 */
public class EISEN extends ECMethod
{
  public EISEN( final double[] x, final double[] y )
  {
    super( x, y );
  }

  @Override
  public double evaluate()
  {
    final double x_norm = Math.sqrt( new SumOfSquares().evaluate( x ) );
    final double y_norm = Math.sqrt( new SumOfSquares().evaluate( y ) );
    final double xy_norm_product = x_norm * y_norm;

    double xy_product_sum = 0.0;    
    for ( int i = 0; i < x.length; i++ )
    {
      xy_product_sum += ( x[i] * y[i] );
    }
    
    // The correlation coefficient (Cosine correlation)
    final double cc = ( Math.abs( xy_product_sum ) ) / xy_norm_product;
    
    return cc;
  }

  public static double calcEISEN( final double[] x, final double[] y )
  {
    EISEN eisen = new EISEN( x, y );
    return eisen.evaluate();
  }
}
