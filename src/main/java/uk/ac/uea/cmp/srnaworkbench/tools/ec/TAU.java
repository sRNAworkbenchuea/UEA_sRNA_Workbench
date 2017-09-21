/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

/**
 * Kendall's tau
 * @author ezb11yfu
 */
public strictfp class TAU extends ECMethod
{
  public TAU( final double[] x, final double[] y )
  {
    super( x, y );
  }

  @Override
  public double evaluate()
  {
    final double bottom = x.length * ( x.length - 1.0 );
    
    double top = 0.0;
    
    for(int i = 0; i < x.length; i++)
    {
      for(int j = 0; j < y.length; j++)
      {
        double x_sgn = Math.signum( x[i] - x[j] );
        double y_sgn = Math.signum( y[i] - y[j] );
        
        top += x_sgn * y_sgn;
      }
    }
    
    final double tau = top / bottom;
    
    return tau;
  }

  public static double calcTAU( final double[] x, final double[] y )
  {
    TAU ofc = new TAU( x, y );
    return ofc.evaluate();
  }
}
