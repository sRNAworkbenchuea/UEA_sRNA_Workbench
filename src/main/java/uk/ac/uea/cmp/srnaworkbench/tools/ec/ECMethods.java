/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author ezb11yfu
 */
public enum ECMethods
{
  PEARSON
  {
    @Override
    public double compute( final double[] x, final double[] y )
    {
      final double r = new PearsonsCorrelation().correlation( x,y );
      validate(r);
      return r;
    }
  },
  COSINE
  {
    @Override
    public double compute( final double[] x, final double[] y )
    {
      final double cos = EISEN.calcEISEN( x,y );
      validate(cos);
      return cos;
    }
  },
  SPEARMAN
  {
    @Override
    public double compute( final double[] x, final double[] y )
    {
      final double rho = new SpearmansCorrelation().correlation( x,y );
      validate(rho);
      return rho;
    }
  },
  KENDALL
  {
    @Override
    public double compute( final double[] x, final double[] y )
    {
      final double tau = TAU.calcTAU( x,y );
      validate(tau);
      return tau;
    }
  };
  
  public abstract double compute( final double[] x, final double[] y );
  
  private static void validate( double coeff )
  {
    if ( coeff < -1.0 || coeff > 1.0 )
    {
      throw new IllegalStateException("Correlation coefficent is outside valid range: -1.0 -> 1.0");
    }
  }
}
