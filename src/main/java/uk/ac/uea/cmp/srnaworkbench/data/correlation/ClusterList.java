/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.util.ArrayList;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

/**
 * This class is intended to store a clusters across multiple time points
 *
 * @author ezb11yfu
 */
public class ClusterList extends ArrayList<Cluster>
{
  private Similarity mostSimilarClusterList;

  public ClusterList()
  {
    this.mostSimilarClusterList = new Similarity();
  }

  public ClusterList( final ClusterList copy )
  {
    super( copy.size() );

    for ( Cluster c : copy )
    {
      this.add( new Cluster( c ) );
    }

    this.mostSimilarClusterList = new Similarity( copy.getMostSimilarClusterList() );
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
    final ClusterList other = (ClusterList) obj;
    
    // Check the lists contain the same number of clusters.
    if ( this.size() != other.size() )
    {
      return false;
    }    
    
    // Check the most similar cluster list is the same.
    if  ( this.mostSimilarClusterList != other.mostSimilarClusterList && 
          ( this.mostSimilarClusterList == null || !this.mostSimilarClusterList.equals( other.mostSimilarClusterList ) ) 
        )
    {
      return false;
    }
    
    // Check if any of the clusters in the list are different.
    for( int i = 0; i < this.size(); i++ )
    {
      if ( !this.get( i ).equals( other.get( i ) ) )
      {
        return false;
      }
    }    
    
    // Passed all the tests... objects are equivalent.
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 47 * hash + ( this.mostSimilarClusterList != null ? this.mostSimilarClusterList.hashCode() : 0 );
    for( Cluster c : this )
    {
      hash = 47 * hash + ( c != null ? c.hashCode() : 0 );
    }
    return hash;
  }

  public Similarity getMostSimilarClusterList()
  {
    return mostSimilarClusterList;
  }

  public void setMostSimilarClusterList( Similarity mostSimilarClusterList )
  {
    this.mostSimilarClusterList = mostSimilarClusterList;
  }

  public void setMostSimilarClusterList( final ClusterMatrix clusters )
  {
    this.mostSimilarClusterList = findMostSimilarClusterList( clusters );
  }

  protected Similarity findMostSimilarClusterList( final ClusterMatrix clusters )
  {
    for ( int j = 0; j < clusters.size(); j++ )
    {
      double r = new PearsonsCorrelation().correlation(
        this.getCentroids(),
        clusters.get( j ).getCentroids() );

      if ( this != clusters.get( j ) && r > this.getMostSimilarClusterList().getDistance() )
      {
        return new Similarity( j, r );
      }
    }

    // Couldn't find anything better than the current most similar cluster.
    return null;
  }

  public double[] getCentroids()
  {
    double[] centroids = new double[this.size()];

    for ( int i = 0; i < this.size(); i++ )
    {
      centroids[i] = this.get( i ).getCentroid();
    }

    return centroids;
  }
}
