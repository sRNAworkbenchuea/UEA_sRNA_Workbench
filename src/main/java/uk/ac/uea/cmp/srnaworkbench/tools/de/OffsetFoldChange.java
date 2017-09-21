/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import java.util.logging.Level;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionLevels;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class OffsetFoldChange extends DEMethod
{
  // Input vars
  private final boolean ordered;
  private final double offset;
  
  
  /**
   * Creates a new OffsetFoldChange object.  Doesn't do the calculation of the new
   * matrix as this might be a slow operation.  To do that call the "process" method
   * after the object is instantiated.  To retrieve the results call the "getFoldChange"
   * method.
   * @param data The data from which to calculate the offset fold change
   * @param offset The offset to apply, or if set to 0.0 or less, then we try to 
   * calculate an offset automatically.
   * @param ordered Whether we are working with ordered (e.g. timeSeries) or
   * unordered (e.g. tissue) data.
   */
  public OffsetFoldChange(final RealMatrix data, final double offset, final boolean ordered)
  {
    super( data );
    
    this.offset = offset;
    this.ordered = ordered;
  }
  
  /**
   * Looks at the user specified offset value, and if set to 0.0 or less, will return
   * a boolean indicating that automatic offset calculation should be used. Otherwise,
   * will return false, indicating that the user provided offset should be used.
   * @return 
   */
  protected boolean useAutoOffset()
  {
    return this.offset <= 0.0;
  }

  
  
  /**
   * Performs the calculations and creates the offset fold change matrix.
   */
  @Override
  public double[] evaluate()
  {
    final int fc_cols = this.data.getColumnDimension()-1;
    
    double[] results = new double[this.data.getRowDimension()]; 
    
    final double actual_offset = useAutoOffset() ? computeOffset() : this.offset;
    LOGGER.log( Level.FINE, "Offset: {0}", actual_offset );
    
    for( int i = 0; i < this.data.getRowDimension(); i++)
    {
      double[] row_results = new double[fc_cols]; 
      
      for( int j = 0; j < fc_cols; j++ )
      {
        // Max and min values are different depending on whether we are working with
        // and ordered or unordered dataset.
        
        final double max = this.ordered ? 
          Math.max( this.data.getEntry( i, j ), this.data.getEntry( i, j+1 ) ) : 
          new Max().evaluate( this.data.getRow(i) );
        
        final double min = this.ordered ?
          Math.min( this.data.getEntry( i, j ), this.data.getEntry( i, j+1 ) ) : 
          new Min().evaluate( this.data.getRow(i) );
        
        final double top = max + actual_offset;
        final double bottom = min + actual_offset;
        final double val = top / bottom;        
        
        row_results[j] = val;
      }
      
      final double fc = new Max().evaluate( row_results );
      
      results[i] = fc;
    }
    
    return results;
  }
  
  /**
   * Calculates the offset to use for the fold change matrix
   * @return The offset to be applied to the fold change matrix.
   */
  protected double computeOffset()
  {
    double new_offset = 0.0;
    
    for( int row = 0; row < this.data.getRowDimension(); row++)
    {
      for( int col = 0; col < this.data.getColumnDimension() - 1; col++ )
      {
        final double diff = this.data.getEntry( row, col ) - this.data.getEntry( row, col + 1 );
        new_offset = (diff > new_offset) ? diff : new_offset;          
      }
    }
    
    return new_offset;
  }
  
  /**
   * Helper method to create, calculate and retrieve the offset fold change from
   * an data matrix.
   * @param data The data from which to calculate the offset fold change
   * @param offset The offset to apply, or if set to 0.0 or less, then we try to 
   * calculate an offset automatically.
   * @param ordered Whether we are working with ordered (e.g. timeSeries) or
   * unordered (e.g. tissue) data.
   * @return The offset fold change list for each entry in the provided dataset.
   */
  public static ExpressionLevels calcOFC(final RealMatrix data, final double offset, final boolean ordered)
  {
    OffsetFoldChange ofc = new OffsetFoldChange(data, offset, ordered);
    double[] expLevels = ofc.evaluate();
    return new ExpressionLevels( expLevels );
  }
}
