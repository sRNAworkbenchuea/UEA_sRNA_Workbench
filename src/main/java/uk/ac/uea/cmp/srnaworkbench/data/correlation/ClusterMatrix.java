/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math.linear.RealMatrix;

/**
 * This class is intended to stores clusters across elements and timepoints
 *
 * @author ezb11yfu
 */
public class ClusterMatrix extends ArrayList<ClusterList>
{
  public ClusterMatrix()
  {
    this( 10 );
  }

  public ClusterMatrix( int capacity )
  {
    super( capacity );
  }

  public ClusterMatrix( final RealMatrix data )
  {
    super();

    ClusterList clusters = new ClusterList();

    for ( int i = 0; i < data.getRowDimension(); i++ )
    {
      for ( int j = 0; j < data.getColumnDimension(); j++ )
      {
        double val = data.getEntry( i, j );
        clusters.add( new Cluster( val ) );
      }
    }

    this.add( clusters );
  }

  public ClusterMatrix( final uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterMatrix copy )
  {
    super( copy.size() );

    for ( ClusterList cl : copy )
    {
      this.add( new ClusterList( cl ) );
    }
  }
  
  public Cluster getEntry( final int i, final int j )
  {
    return this.get( i ).get( j );
  }

  protected void calcSimilarityMatricies()
  {
    for ( ClusterList cl : this )
    {
      cl.setMostSimilarClusterList( this );
    }
  }

  public ClusterMatrix createMergedClusters( final int timePoints )
  {
    this.calcSimilarityMatricies();

    // Create space for merge cluster mappings.
    int[] cls = new int[this.size()];
    Arrays.fill( cls, 0 );

    // Create merge mappings and statistics
    final int nrCls = createMergeMappings( cls );
    final boolean[] missing = identifyMergedClusters( cls );
    final int nrClsNew = compressMappings( cls, nrCls, missing );

    // Create arrays containing counts of merged clusters
    int[] count = new int[nrClsNew];
    Arrays.fill( count, 0 );

    // Calculate new centroids
    Centroids cen = calculateNewCentroids( count, cls, nrClsNew, timePoints );

    // Shift centroids
    ClusterMatrix newClusters = new ClusterMatrix();
    for ( int i = 0; i < this.size(); i++ )
    {
      ClusterList newCL = new ClusterList();
      
      for(int j = 0; j < timePoints; j++ )
      {
        newCL.add( new Cluster(cen.getEntry( i, j )) );
      }
      
      newClusters.add( newCL );      
    }
    
    return newClusters;
  }

  protected Centroids calculateNewCentroids( final int[] count, final int[] mappings, final int nrClsNew, final int timePoints )
  {
    // Intialise cen arrays
    double[][] cen = new double[nrClsNew][timePoints];
    for ( double[] da : cen )
    {
      Arrays.fill( da, 0 );
    }

    for ( int i = 0; i < this.size(); i++ )
    {
      for ( int j = 0; j < timePoints; j++ )
      {
        cen[mappings[i]][j] += this.getEntry( i, j ).getCentroid();
      }
      count[mappings[i]]++;
    }

    for ( int i = 0; i < nrClsNew; i++ )
    {
      for ( int j = 0; j < timePoints; j++ )
      {
        if ( count[i] > 0 )
        {
          cen[i][j] /= count[i];
        }
        else
        {
          cen[i][j] = 0;
        }
      }
    }

    return new Centroids( cen );
  }

  protected int compressMappings( final int[] mappings, final int nrCls, final boolean[] missing )
  {
    int nrClsNew = nrCls;
    for ( int i = nrCls - 1; i > 0; i-- )
    {
      if ( missing[i] == false )
      {
        nrClsNew--;
        for ( int j = 0; j < this.size(); j++ )
        {
          if ( mappings[j] > i )
          {
            mappings[j]--;
          }
        }
      }
    }

    nrClsNew--;

    return nrClsNew;
  }

  protected boolean[] identifyMergedClusters( final int[] mappings )
  {
    boolean[] missing = new boolean[this.size()];
    Arrays.fill( missing, false );

    for ( int i = 0; i < this.size(); i++ )
    {
      missing[mappings[i]] = true;
    }

    return missing;
  }

  protected int createMergeMappings( int[] cls )
  {
    final int clusterSize = this.size();
    int nrCls = 1;

    for ( int i = 0; i < clusterSize; i++ )
    {
      final int mostSimilarIndex = this.get( i ).getMostSimilarClusterList().getClusterListIndex();


      if ( mostSimilarIndex == -1 )
      {
        cls[i] = nrCls;
        nrCls++;
        continue;
      }

      if ( cls[i] == 0 && cls[mostSimilarIndex] == 0 )
      {
        cls[i] = nrCls;
        cls[mostSimilarIndex] = nrCls;
        nrCls++;
        continue;
      }

      if ( cls[i] == 0 && cls[mostSimilarIndex] != 0 )
      {
        cls[i] = cls[mostSimilarIndex];
        continue;
      }

      if ( cls[i] != 0 && cls[mostSimilarIndex] == 0 )
      {
        cls[mostSimilarIndex] = cls[i];
        continue;
      }

      if ( cls[i] != 0 && cls[mostSimilarIndex] != 0 )
      {
        if ( cls[i] < cls[mostSimilarIndex] )
        {
          for ( int j = 0; j < clusterSize; j++ )
          {
            if ( cls[j] == cls[mostSimilarIndex] )
            {
              cls[j] = cls[i];
            }
          }
          cls[mostSimilarIndex] = cls[i];
        }
        else
        {
          for ( int j = 0; j < clusterSize; j++ )
          {
            if ( cls[j] == cls[i] )
            {
              cls[j] = cls[mostSimilarIndex];
            }
          }
          cls[i] = cls[mostSimilarIndex];
        }
      }
    }

    return nrCls;
  }
}
