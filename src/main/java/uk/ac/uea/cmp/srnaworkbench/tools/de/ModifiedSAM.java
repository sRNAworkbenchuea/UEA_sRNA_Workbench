/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionLevels;

/**
 * Modified Significance Analysis of Microarrays (SAM).
 *
 * State I == irradiated state State U == unirradiated state
 *
 * @author ezb11yfu
 */
public class ModifiedSAM extends DEMethod
{
  private int[] replicateCounts;
  private double offset;

  public ModifiedSAM( RealMatrix data, int[] replicateCounts, double offset )
  {
    super( data );
    
    if (replicateCounts == null || replicateCounts.length < 2)
      throw new IllegalArgumentException("Must have at least two replicate sets to work through.");
    
    if (offset <= 0.0)
      throw new IllegalArgumentException("Offset must be a small positive number (i.e. > 0.0)");
    
    this.replicateCounts = replicateCounts;
    this.offset = offset;
  }

  @Override
  public double[] evaluate()
  {
    final int rows = this.data.getRowDimension();

    double[] results = new double[rows];

    for ( int i = 0; i < rows; i++ )
    {
      final double[] row = this.data.getRow( i );
      
      double[] row_results = new double[replicateCounts.length-1]; 

      int startI = 0;
      int startU = startI + replicateCounts[0];
      for ( int j = 0; j < replicateCounts.length - 1; j++ )
      {
        final double[] stateI = ArrayUtils.subarray( row, startI, startU );
        final double[] stateU = ArrayUtils.subarray( row, startU, startU + replicateCounts[j + 1] );

        // Mean of row i in state I and state U
        final double meanI = new Mean().evaluate( stateI );
        final double meanU = new Mean().evaluate( stateU );

        // Sum of squares of expression of row i in state I and state U
        final double sosI = new SumOfSquares().evaluate( stateI );
        final double sosU = new SumOfSquares().evaluate( stateU );

        // Number of measurements for row i in state I and state U
        final double nI = stateI.length;
        final double nU = stateU.length;

        // a is ??
        final double a = ( 1.0 / nI + 1.0 / nU ) / ( nI + nU - 2.0 );

        // si = gene specific scatter
        final double si = Math.sqrt( a * ( sosI + sosU ) );

        // s0 is some constant???
        final double s0 = offset;

        // di = relative difference
        final double di = ( meanI - meanU ) / ( si + s0 );

        row_results[j] = di;

        startI = startU;
        startU = startU + replicateCounts[j + 1];
      }


      final double max = new Max().evaluate( row_results );

      results[i] = max;
    }

    return results;
  }

  /**
   * Helper method to create, calculate and retrieve the unusual ration from a
   * data matrix.
   *
   * @param data The data from which to calculate the expression levels
   * @return The expression levels for each entry in the provided dataset.
   */
  public static ExpressionLevels calcModifiedSAM( final RealMatrix data, int[] replicateCounts, double offset )
  {
    ModifiedSAM ms = new ModifiedSAM( data, replicateCounts, offset );
    double[] expLevels = ms.evaluate();
    return new ExpressionLevels( expLevels );
  }
}
