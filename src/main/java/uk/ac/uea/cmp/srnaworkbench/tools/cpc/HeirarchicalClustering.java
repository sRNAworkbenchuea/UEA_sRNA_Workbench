/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.cpc;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterMatrix;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Similarity;

/**
 * Hierarchical clustering uses centroid linkage (a centroid for a cluster is 
 * defined as the point-wise average of the cluster members). Due to the high
 * number of input series, the agglomerative variant of hierarchical clustering
 * is used, in which, at every step, the similar elements are merged with an
 * existing group. Similar elements are determined using a full linkage approach
 * (i.e. if two expression profiles had PCC above 0:9 then they were assigned to
 * the same cluster; if an expression profile had PCC above 0:9 with all members
 * of a group, then it was added to the group). Thus, the putative numbers of
 * clusters (k) for each level in the hierarchy are calculated.
 *
 * @author ezb11yfu
 */
public class HeirarchicalClustering
{
  private static final int MAX_STEP = 100;
  private final RealMatrix data;
  private final double clusterThreshold;
  //private final double pearsonThreshold;

  public HeirarchicalClustering( final RealMatrix data, final double clusterThreshold) //, final double pearsonThreshold )
  {
    if (data == null || data.getColumnDimension() < 2 || data.getRowDimension() == 0)
      throw new IllegalArgumentException("Must have data with at least 2 columns to work with.");
    
    this.data = data;

    this.clusterThreshold = clusterThreshold;
    //this.pearsonThreshold = pearsonThreshold;
  }

  public ClusterMatrix cluster()
  {
    // Initialise clusters
    final int timePoints = this.data.getColumnDimension();
    ClusterMatrix initialClusters = new ClusterMatrix( this.data );
    ClusterMatrix mergedClusters = mergeClusters( initialClusters, timePoints );
    
    Similarity[] assignCluster = new Similarity[initialClusters.size()];
    
    for( int i = 0; i < initialClusters.size(); i++ )
    {
      for( int j = 0; j < mergedClusters.size(); j++ )
      {
        double r = new PearsonsCorrelation().correlation(
          initialClusters.get( i ).getCentroids(),
          mergedClusters.get( j ).getCentroids() );
        
        if ( i != j && r > initialClusters.get( i ).getMostSimilarClusterList().getDistance() )
        {
          assignCluster[i] = new Similarity( j, r );
        }
      }
    }
    
    return mergedClusters;
  }
  
  protected ClusterMatrix mergeClusters( final ClusterMatrix initialClusters, final int timePoints )
  {
    List<ClusterMatrix> clusterMatricies = new ArrayList<ClusterMatrix>();
    clusterMatricies.add( initialClusters );

    int step = 0;
    ClusterMatrix clusterMatrix;
    boolean noImprovement = false;
    while ( ( clusterMatrix = clusterMatricies.get( step ) ).size() > clusterThreshold 
      && clusterMatricies.size() < MAX_STEP
      && noImprovement == false )
    {
      // Create Merged Clusters for next step
      final ClusterMatrix newStep = clusterMatrix.createMergedClusters( timePoints );

      step++;

      if ( newStep.equals( clusterMatrix ) )
      {
        noImprovement = true;
      }
      else
      {
        clusterMatricies.add( newStep );
      }
    }
    
    
    // Not really sure what this is all about?  Maybe just some statistics?
    ClusterMatrix mergedClusters = clusterMatricies.get( clusterMatricies.size() - 1 );
    
    return mergedClusters;
  }
  
  
  public static ClusterMatrix cluster( final RealMatrix data, final double clusterThreshold)//, final double pearsonThreshold )
  {
    HeirarchicalClustering hc = new HeirarchicalClustering( data, clusterThreshold);//, pearsonThreshold );
    return hc.cluster();
  }
}
