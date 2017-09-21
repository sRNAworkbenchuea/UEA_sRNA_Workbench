/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.cpc;

import java.io.File;
import org.apache.commons.math.linear.RealMatrix;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterMatrix;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;

/**
 *
 * @author ezb11yfu
 */
public class CorrelatedPairClustering extends RunnableTool
{
  private final static String TOOL_NAME = "CPC";
  
  // Input variables
  private final RealMatrix correlationData;
  private final CPCParams params;
  
  private ClusterMatrix clusters;
  
  public CorrelatedPairClustering(final RealMatrix correlationData, final CPCParams params)
  {
    super( TOOL_NAME );
    
    this.correlationData = correlationData;
    this.params = params;
    this.clusters = null;
  }

  @Override
  protected void process() throws Exception
  {
    ClusterMatrix hClusters = HeirarchicalClustering.cluster( correlationData, params.getClusterThreshold());//, params.getPearsonThreshold() );
    
    ClusterMatrix kClusters = KMeansClustering.cluster( correlationData, hClusters, params.getPermittedNRClusters(), params.getSelectedNRClusters() );
    
    this.clusters = kClusters;
  }

  public ClusterMatrix getClusters()
  {
    return clusters;
  }
  
  
  
  
  public static CorrelatedPairClustering load( final File correlationData, final File clusterData, final CPCParams params )
  {
    //return new CorrelatedPairClustering( correlationData, clusterData, params );
    return null;
  }
  
  
  public static ClusterMatrix cluster( final RealMatrix correlationData, final CPCParams params )
  {
    CorrelatedPairClustering cpc = new CorrelatedPairClustering( correlationData, params );
    cpc.run();
    return cpc.getClusters();
  }
  
}
