/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.cpc;

import java.util.Arrays;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Centroids;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterList;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterMatrix;

/**
 * Using the determined k values in ascending order, we applied k-means using
 * centroid linkage, for each k. For each resulting partition we computed a
 * validity measure [8] d(k). A good partition is characterized by a low inter
 * cluster similarity and a high intra cluster similarity. Since we wanted to
 * simultaneously minimize the inter cluster similarity and to maximize the
 * intra cluster similarity, both determined using PCC, we computed d(k) as
 * follows: d(k) = inter(k) + (1 - intra(k)) where k is the number of clusters,
 * intra(k) is the intra cluster similarity defined as the average PCC between
 * all distinct pairs of clusters, as represented by their centroids, and
 * inter(k) is the inter cluster similarity defined as the average of cluster
 * compactness, for all clusters (the cluster compactness for a given cluster is
 * computed as the average PCC between its centroid and all the members of the
 * cluster [8]). The optimal number of clusters was chosen to be the first value
 * of k encountered for which the function d(k) had a local minimum. Subsequent
 * minimums obtained for higher numbers of clusters were not found to improve
 * the results.
 *
 * @author ezb11yfu
 */
public class KMeansClustering
{
  private int permittedNRCls;
  private int selectedNRCls;
  private ClusterMatrix clustIn;
  private RealMatrix dataIn;

  public KMeansClustering( final RealMatrix dataIn, final ClusterMatrix clustIn, final int permittedNRCls, final int selectedNRCls )
  {
    if ( dataIn == null || dataIn.getColumnDimension() < 2 || dataIn.getRowDimension() == 0 )
    {
      throw new IllegalArgumentException( "Must have data with at least 2 columns to work with." );
    }

    if ( clustIn == null || clustIn.size() == 0 )
    {
      throw new IllegalArgumentException( "Must contain some clusters to process" );
    }

    this.dataIn = dataIn;
    this.permittedNRCls = permittedNRCls;
    this.selectedNRCls = selectedNRCls;
    this.clustIn = clustIn;
  }

  /**
   * IMPORTANT NOTE:  This class is currently far from working.  It hasn't even been
   * tested yet and I'm not sure exactly what the output should be!
   * @return 0.0... don't know what to return yet.
   */
  public double cluster()
  {
    // Reformulate the heirarchical clusters so they are the reqired size.
    ClusterMatrix clustersH = resizeHeirachicalClusters();

    Memo[] memo = new Memo[this.dataIn.getRowDimension()];

    // Go through the newly reformulated clusters, re-working the centroids (via k-means) 
    // for each cluster list until the no changes are detected.
    for ( ClusterList cl : clustersH )
    {
      int nbClusters = cl.size();

      double epsilon = (double) nbClusters / 1000.0;

      Centroids centroid = createInitialCentroidMatrix( nbClusters );

      boolean change = true;
      final int MAX_STEPS = 100;
      int step = 0;
      Centroids newCentroid = null;

      while ( change && step < MAX_STEPS )
      {
        change = false;

        newCentroid = centroid.computeNewCentroids( dataIn, nbClusters );

        for ( int i = 0; i < nbClusters; i++ )
        {
          double chg = newCentroid.calcChange( i, centroid.getRow( i ) );

          if ( chg > epsilon )
          {
            change = true;
            break;
          }
        }

        centroid = newCentroid;

        step++;
      }

      // This is probably wrong!
      for ( int i = 0; i < this.dataIn.getRowDimension(); i++ )
      {
        memo[i] = new Memo( i, newCentroid == null ? new double[0] : newCentroid.getRow( i ) );
      }
    }

    // Didn't really have time to implement this nicely.
    doRestOfProc( clustersH, memo );

    return 0.0;
  }

  public static ClusterMatrix cluster( final RealMatrix data, ClusterMatrix hClusters, final int permittedNRCls, final int selectedNRCls )
  {
    KMeansClustering kc = new KMeansClustering( data, hClusters, permittedNRCls, selectedNRCls );
    double res = kc.cluster();
    return new ClusterMatrix();
  }

  private Centroids createInitialCentroidMatrix( final int nbClusters )
  {
    final int timePoints = this.dataIn.getColumnDimension();
    Centroids centroids = new Centroids( nbClusters, timePoints );
    int[] init = new RandomDataImpl().nextPermutation( nbClusters, nbClusters );

    for ( int i = 0; i < nbClusters; i++ )
    {
      centroids.setRow( i, this.dataIn.getRow( init[i] ) );
    }

    return centroids;
  }

  private ClusterMatrix resizeHeirachicalClusters()
  {
    ClusterMatrix clustersH = new ClusterMatrix();
    int nbClusterLists = clustIn.size();

    // If heirachical clusters are larger than what we'd like, just take the
    // required amount from the end of the list.  Otherwise we have to make up
    // the shortfall
    if ( nbClusterLists > this.permittedNRCls )
    {
      for ( int i = 0; i < this.permittedNRCls; i++ )
      {
        clustersH.add( clustIn.get( nbClusterLists - i ) );
      }
    }
    else
    {
      // Add in all the clusters we have to begin with...
      for ( int i = 0; i < nbClusterLists; i++ )
      {
        clustersH.add( clustIn.get( i ) );
      }

      // Then we need to fill in the rest.
      boolean go = true;
      while ( nbClusterLists + 1 <= this.permittedNRCls && go )
      {
        int mid;
        int newIdx;
        if ( nbClusterLists % 2 == 0 )
        {
          mid = ( nbClusterLists - 1 ) / 2;
        }
        else
        {
          mid = nbClusterLists / 2;
        }
        newIdx = ( clustersH.get( mid ).size() + clustersH.get( mid + 1 ).size() ) / 2;

        if ( clustersH.get( mid ).size() - newIdx < 2 )
        {
          go = false;
        }
        for ( int i = nbClusterLists; i > mid; i-- )
        {
          clustersH.set( i, clustersH.get( i - 1 ) );
        }
        clustersH.set( mid + 1, clustersH.get( newIdx ) );
        nbClusterLists++;
      }
    }

    // Add the selected cluster to the end of the list if requested
    if ( this.selectedNRCls != 0 )
    {
      clustersH.add( this.clustIn.get( this.selectedNRCls ) );
    }

    return clustersH;
  }

  private void doRestOfProc( ClusterMatrix clustersH, Memo[] memo )
  {
    double[] intra = new double[clustersH.size()];
    double[] inter = new double[clustersH.size()];
    
    for ( int indH = 0; indH < clustersH.size(); indH++ )
    {
      ClusterList cl = clustersH.get( indH );
      
      double rAvgCls = 0.0;
      for ( int ind = 0; ind < cl.size(); ind++ )
      {
        // compute the average intracluster correlation for a given cluster
        int rCls = 0;
        int addCls = 0;
        for ( int i = 0; i <= this.dataIn.getRowDimension(); i++ )
        {
          for ( int j = i + 1; j <= this.dataIn.getRowDimension(); j++ )
          {
            if ( true ) //$memo[$i][$indH + 1] == $ind && $memo[$j][$indH + 1] == $ind )
            {
              // compute correlation
              addCls++;

              double r = new PearsonsCorrelation().correlation(
                this.dataIn.getRow( i ),
                this.dataIn.getRow( j ) );

              rCls += r;
            }
          }
        }
        if ( addCls == 0 )
        {
          rCls = 0;
        }
        else
        {
          rCls /= addCls;
        }

        rAvgCls += rCls;
      }

      rAvgCls /= cl.size();
      intra[indH] = rAvgCls;


      Centroids centroid = new Centroids( cl.size(), this.dataIn.getColumnDimension() );

      int[] add = new int[cl.size()];
      Arrays.fill( add, 0 );
      for ( int i = 0; i < this.dataIn.getRowDimension(); i++ )
      {
        //add[memo[i].getCentroids()[indH + 1]]++;
        for ( int j = 0; j < this.dataIn.getColumnDimension(); j++ )
        {
          centroid.increment( memo[i].getIndex(), j, this.dataIn.getEntry( i, j ) );
        }
      }

      for ( int i = 0; i < cl.size(); i++ )
      {
        for ( int j = 0; j < this.dataIn.getColumnDimension(); j++ )
        {
          if ( add[i] == 0 )
          {
            centroid.setEntry( i, j, 0 );
          }
          else
          {
            centroid.setEntry( i, j, centroid.getEntry( i, j ) / add[i] );
          }
        }
      }

      double rInterAvg = 0;
      for ( int i = 0; i < cl.size(); i++ )
      {
        for ( int j = i + 1; j < cl.size(); j++ )
        {
          // compute Pearson
          double r = new PearsonsCorrelation().correlation(
                centroid.getRow( i ),
                centroid.getRow( j ) );
          
          rInterAvg += r;
        }
      }
      
      if ( cl.size() > 1 )
      {
        rInterAvg /= cl.size() * ( cl.size() - 1 ) / 2;
      }
      inter[indH] = rInterAvg;
    }
    
    int maxPoz = -1;
    double maxScore = 0.0;
    
    for ( int indH = 0; indH < clustersH.size(); indH++ )
    {
      final double score = 0.4 * intra[indH] + 0.6 * ( 1 - Math.abs( inter[indH] ) );
      if ( score > maxScore )
      {
        maxScore = score;
        maxPoz = indH;
      }
    }
    
    // Not sure exactly what to return here... probably some combination of memo
    // marked with the best cluster.
  }

  public static class Memo
  {
    private int index;
    private double[] centroids;

    public Memo( int index, double[] centroids )
    {
      this.index = index;
      this.centroids = centroids;
    }

    public double[] getCentroids()
    {
      return centroids;
    }

    public int getIndex()
    {
      return index;
    }
  }
}
