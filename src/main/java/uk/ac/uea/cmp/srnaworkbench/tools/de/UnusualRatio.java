/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionLevels;

/**
 *
 * @author ezb11yfu
 */
public class UnusualRatio extends DEMethod
{
  private final double offset;
  
  public UnusualRatio( final RealMatrix data, final double offset )
  {
    super( data );
    
    this.offset = offset;
  }
  
  @Override
  public double[] evaluate()
  {
    final int rows = this.data.getRowDimension();
    
    double[] results = new double[rows]; 
    
    for( int i = 0; i < rows; i++ )
    {
      final double[] row = this.data.getRow( i );
      
      double[] z_scores = new double[row.length];
      double[] offseted_row = new double[row.length];

      for(int j = 0; j < row.length; j++)
      {
        offseted_row[j] = row[j] + offset;
      }
      
      // Calculate the mean, standard deviation and coefficient of variation for 
      // the row (sRNA/loci/gene)
      final double mean = new Mean().evaluate( offseted_row );
      final double std_dev = new StandardDeviation().evaluate( offseted_row );
      final double cv = std_dev / mean;
      
//      for (int j = 0; j < offseted_row.length; j++)
//      {
//        final double x = offseted_row[j];
//        
//        // Calculate the z score for this element and save in the array.
//        final double z = (x - mean) / std_dev;        
//        z_scores[j] = z;
//      }
      
      // Find the max z score for the row and add the coefficient or variation and
      // the offset to determine the final expression level.
      //final double max_z_score = absMax( z_scores );
      results[i] = cv;//max_z_score;
    }
    
    return results;
  }
  
  protected double absMax(double[] da)
  {
    double[] abs = new double[da.length];
    
    for( int i = 0; i < da.length; i++ )
    {
      abs[i] = Math.abs( da[i] );
    }
    
    double abs_max = new Max().evaluate( abs );
    
    return abs_max;
  }
  
  /**
   * Helper method to create, calculate and retrieve the unusual ration from
   * a data matrix.
   * @param data The data from which to calculate the expression levels
   * @return The expression levels for each entry in the provided dataset.
   */
  public static ExpressionLevels calcUnusualRatio( final RealMatrix data, final double offset )
  {
    UnusualRatio ur = new UnusualRatio( data, offset );
    double[] expLevels = ur.evaluate();
    return new ExpressionLevels( expLevels );
  }
}
