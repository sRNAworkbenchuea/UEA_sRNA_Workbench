/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

/**
 *
 * @author ezb11yfu
 */
public class ExpressionLevels
{
  private final double[] expressionLevels;
  private final double mean;
  private final double stdDev;

  public ExpressionLevels( final double[] expressionLevels )
  {
    if ( expressionLevels == null )
      throw new IllegalArgumentException( "names and expressionLevels must be non-null" );
    
    if ( expressionLevels.length == 0 )
      throw new IllegalArgumentException( "names and expressionLevels must containg at least one element" );
    
    this.expressionLevels = expressionLevels;
    this.mean = calcMean();
    this.stdDev = calcStandardDeviation();    
  }
  
  /**
   * Create a new copy of the provided ExpressionLevels object.  Does not recalculate
   * the mean and standard deviation, so it retains the copies values.
   * @param copy ExpressionLevels object to copy.
   */
  public ExpressionLevels( final ExpressionLevels copy )
  {
    this( Arrays.copyOf( copy.expressionLevels, copy.expressionLevels.length ), copy.mean, copy.stdDev );
  }
  
  /**
   * We assume that all the input has already been validated if we are using this
   * constructor.
   * @param expressionLevels
   * @param mean
   * @param stdDev 
   */
  private ExpressionLevels( final double[] expressionLevels, final double mean, final double stdDev )
  {
    this.expressionLevels = expressionLevels;
    this.mean = mean;
    this.stdDev = stdDev;    
  }
  
  protected static double[] convertList( List<Double> dl )
  {
    double[] da = new double[dl.size()];
    
    for(int i = 0; i < dl.size(); i++ )
    {
      Double d = dl.get( i );
      
      da[i] = d;
    }
    
    return da;
  }

  public double[] getExpressionLevels()
  {
    return expressionLevels;
  }
  
  public double get( final int index )
  {
    return this.expressionLevels[index];
  }

  public double getMean()
  {
    return mean;
  }

  public double getStdDev()
  {
    return stdDev;
  }  
  
  public int size()
  {
    return this.expressionLevels.length;
  }
  
  public void sort()
  {
    Arrays.sort( this.expressionLevels );
  }
  
  protected final double calcMean()
  {
    return new Mean().evaluate( this.expressionLevels );
  }
  
  protected final double calcStandardDeviation()
  {
    return new StandardDeviation().evaluate( this.expressionLevels );
  }
  
  /**
   * Writes out a file with a single column containing each row's expression
   * level.
   *
   * @param f The file to write to.
   * @throws IOException Thrown if there were any problems writing to the file.
   */
  public void save( File f ) throws IOException
  {
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( f ) ) );

    for ( Double val : this.getExpressionLevels() )
    {
      pw.print( val );
      pw.print( "\n" );
    }

    pw.flush();
    pw.close();
  }
  
  public static ExpressionLevels load( final File expressionLevelsFile ) throws IOException
  {    
    List<String> lines = FileUtils.readLines( expressionLevelsFile );
    
    final List<Double> vals = new ArrayList<Double>(lines.size());
        
    // Need to parse the lines here.  (Not sure what format the files take).
    for( String line : lines )
    {
      String tl = line.trim();
      
      if (!tl.isEmpty())
      {
        vals.add( Double.parseDouble( tl ) );
      }
    }
    
    ExpressionLevels els = new ExpressionLevels( convertList(vals) );
    return els;
  }
  
}
