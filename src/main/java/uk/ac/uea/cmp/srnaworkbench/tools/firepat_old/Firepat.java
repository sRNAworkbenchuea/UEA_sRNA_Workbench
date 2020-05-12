/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.firepat_old;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ClusterMatrix;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Correlation.CorrelationList;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionData;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.cpc.CPCParams;
import uk.ac.uea.cmp.srnaworkbench.tools.cpc.CorrelatedPairClustering;
import uk.ac.uea.cmp.srnaworkbench.tools.de.DEParams;
import uk.ac.uea.cmp.srnaworkbench.tools.de.DifferentialExpression;
import uk.ac.uea.cmp.srnaworkbench.tools.ec.ECParams;
import uk.ac.uea.cmp.srnaworkbench.tools.ec.ExpressionCorrelation;

/**
 *
 * @author ezb11yfu
 */
public class Firepat extends RunnableTool
{
  private final static String TOOL_NAME = "FIREPAT";
  
  // Input vars
  private final ExpressionData expressionData1;
  private final ExpressionData expressionData2;
  private final FirepatParams params;
  
  // Output vars
  //private double distance;

  public Firepat( final ExpressionData expressionData1, final ExpressionData expressionData2, final FirepatParams params )
  {
    super( TOOL_NAME );
    
    if ( expressionData1 == null || expressionData2 == null )
      throw new IllegalArgumentException( "Must specify two expression level objects to process");
    
    if ( expressionData1.rows() <= 0 || expressionData2.rows() <= 0 )
      throw new IllegalArgumentException( "Both expression level objects must contain entries to process" );
    
    if ( expressionData1.columns() < 2 || expressionData2.columns() < 2 )
      throw new IllegalArgumentException( "Both expression level objects must contain at least two columns to process" );
    
    this.expressionData1 = expressionData1;
    this.expressionData2 = expressionData2;
    this.params = params;
  }

    
  @Override
  protected void process() throws Exception
  {    
    // Build differential expression parameters.
    final DEParams deParams = new DEParams.Builder()
      .setMethod( this.params.getDEMethod() )
      .setMinAbundance( this.params.getMinAbundance() )
      .setMinExpressionLevel( this.params.getMinExpressionLevel() )
      .setOffset( this.params.getOffset() )
      .setOrdered( this.params.getOrdered() )
      .setPctThreshold( this.params.getDEThreshold() )
      .setReplicateCounts( StringUtils.join( this.params.getReplicateCounts(), "," ) )
      .build();
    
    // Run differntial expression calculator on both input sets
    final ExpressionData resDE1 = DifferentialExpression.process( expressionData1, deParams);
    final ExpressionData resDE2 = DifferentialExpression.process( expressionData2, deParams);
    
    // Build expression correlation parameters
    final ECParams ecParams = new ECParams.Builder()
      .setMethod( this.params.getSimMethod() )
      .setSimilarityThreshold( this.params.getSimilarityThreshold() )
      .build();
        
    // Run expression correlation algorithm on differential expression sets
    final CorrelationList resEC = ExpressionCorrelation.correlate( resDE1, resDE2, ecParams);
    
    // split
    //TODO need to generate correlation data suitable for clustering from CorrelationList resEC.
    
    // Build correlated pair clustering parameters object
    //TODO fill in the missing params.
    final CPCParams cpcParams = new CPCParams.Builder()
      .build();
    
    // Performs heirachical clustering and then kmeans clustering.
    final ClusterMatrix clusters = CorrelatedPairClustering.cluster( null, cpcParams );
    
    // log ratios
    //TODO
    
    // Create fancy output
    //TODO
  }
  
  /**
   * 
   * @param elFile1
   * @param elFile2
   * @param params
   * @return
   * @throws IOException 
   */
  public static Firepat load( final File elFile1, final File elFile2, final FirepatParams params ) throws IOException
  {
    ExpressionData ed1 = ExpressionData.load( elFile1 );
    ExpressionData ed2 = ExpressionData.load( elFile2 );
    Firepat ec = new Firepat( ed1, ed2, params );
    return ec;
  }
  /**
   * 
   * @param args 
   */
  public static void main(String[] args)
  {
    
  }
}
