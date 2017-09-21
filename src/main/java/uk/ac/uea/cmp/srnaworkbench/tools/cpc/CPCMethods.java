/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.cpc;

/**
 *
 * @author ezb11yfu
 */
public enum CPCMethods
{
  HIERARCHICAL_CLUSTERING
  {
    @Override
    public double[] cluster(  )
    {
      return new double[0];
    }
  },
  K_MEANS_CLUSTERING
  {
    @Override
    public double[] cluster(  )
    {
      return new double[0];
    }
  };
  
  public abstract double[] cluster(  );
}
