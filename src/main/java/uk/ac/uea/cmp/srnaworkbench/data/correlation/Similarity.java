/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

/**
 *
 * @author ezb11yfu
 */
public class Similarity
{
  private int clusterListIndex;
  private double distance;

  public Similarity()
  {
    this( -1, -1.0 );
  }

  public Similarity( final int clusterListIndex, double distance )
  {
    this.clusterListIndex = clusterListIndex;
    this.distance = distance;
  }

  public Similarity( final Similarity copy )
  {
    this.clusterListIndex = copy.getClusterListIndex();
    this.distance = copy.getDistance();
  }

  public double getDistance()
  {
    return distance;
  }

  public int getClusterListIndex()
  {
    return clusterListIndex;
  }
}
