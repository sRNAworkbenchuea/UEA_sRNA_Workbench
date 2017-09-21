/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ezb11yfu
 */
public class Cluster
{
  private double centroid;
  private List<Double> points;

  public Cluster( final double centroid )
  {
    this( centroid, new ArrayList<Double>( Arrays.asList( new Double( centroid ) ) ) );
  }

  public Cluster( final double centroid, final List<Double> points )
  {
    this.centroid = centroid;
    this.points = points;
  }
  
  public Cluster( final Cluster copy )
  {
    this.centroid = copy.getCentroid();
    this.points = new ArrayList<Double>( copy.getPoints() );
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    final Cluster other = (Cluster) obj;
    if ( Double.doubleToLongBits( this.centroid ) != Double.doubleToLongBits( other.centroid ) )
    {
      return false;
    }
    if ( this.points != other.points && ( this.points == null || !this.points.equals( other.points ) ) )
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 29 * hash + (int) ( Double.doubleToLongBits( this.centroid ) ^ ( Double.doubleToLongBits( this.centroid ) >>> 32 ) );
    hash = 29 * hash + ( this.points != null ? this.points.hashCode() : 0 );
    return hash;
  }
  

  public double getCentroid()
  {
    return centroid;
  }

  public List<Double> getPoints()
  {
    return points;
  }
  
}
