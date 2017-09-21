/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Correlation;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Correlation.CorrelationList;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionData;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class ExpressionCorrelation extends RunnableTool
{
  private final static String TOOL_NAME = "CORRELATION";
  
  // Input vars
  private final ExpressionData x;
  private final ExpressionData y;
  private final ECParams params;
  
  // Output vars
  private CorrelationList correlations;

  public ExpressionCorrelation( final ExpressionData x, final ExpressionData y, final ECParams params )
  {
    super( TOOL_NAME );
    
    if ( x == null || y == null )
      throw new IllegalArgumentException( "Expression levels for x and y must be non-null");
    
    if ( x.rows() == 0 || y.rows() == 0 )
      throw new IllegalArgumentException( "Expression levels for x and y must both contain some rows to work with" );
    
    if ( x.columns() == 0 || y.columns() == 0 )
      throw new IllegalArgumentException( "Expression levels for x and y must both contain some columns to work with" );
    
    this.x = x;
    this.y = y;
    this.params = params;
    this.correlations = new CorrelationList();
  }

  public CorrelationList getCorrelations()
  {
    return correlations;
  }

    
  @Override
  protected void process() throws Exception
  { 
    CorrelationList corrs = new CorrelationList();
    
    for( int i = 0; i < x.rows(); i++ )
    {      
      for( int j = 0; j < y.rows(); j++ )
      {        
        // Select method from user defined parameters.
        ECMethods method = ECMethods.valueOf( this.params.getMethod() );

        // Compute the expression level using the selected method.
        double corrCoeff = method.compute( x.getRow( i ).getData(), y.getRow( j ).getData() );        
        LOGGER.log( Level.FINE, "corrCoeff between rows x{0} and y{1} = {2}", new Object[]{i, j, corrCoeff} );
        
        // Only consider pair if distance is above the user defined threshold.
        if ( Math.abs( corrCoeff ) >= ((double)this.params.getSimilarityThreshold()) * 0.01 )
        {
          // Convert correlation coefficient into a distance
          double distance = 1.0 - corrCoeff;          
          corrs.add( new Correlation( x.getRow( i ), y.getRow( j ), distance ) );
        }
        else
        {
          LOGGER.log( Level.INFO, "Discarded corrCoeff between rows x{0} and y{1}", new Object[]{i, j, corrCoeff} );
        }
      }
    }
    
    this.correlations = removeDuplicateCorrelations( corrs );
  }
  
  
  protected CorrelationList removeDuplicateCorrelations( CorrelationList correlations )
  {
    boolean[] marks = new boolean[correlations.size()];    
    Arrays.fill( marks, true );
    
    for( int i = 0; i < correlations.size() - 1; i++ )
    {
      Correlation c1 = correlations.get( i );
      Correlation c2 = correlations.get( i+1 );
      
      if (c1.getExprEntry1().equals( c2.getExprEntry1() ) && c1.getExprEntry2().equals( c2.getExprEntry2() ) )
      {
        marks[i+1] = false;
      }
    }
    
    CorrelationList dr = new CorrelationList();
    for( int i = 0; i < correlations.size(); i++ )
    {
      Correlation c = correlations.get( i );
      boolean marked = marks[i];
      
      if (marked)
      {
        double sum1 = new Sum().evaluate(c.getExprEntry1().getData());
        double sum2 = new Sum().evaluate(c.getExprEntry2().getData());
        
        if ( sum1 > 1.0 && sum2 > 1.0 )
        {
          dr.add( c );
        }
      }
    }
    
    return dr;
  }
  
  
  public static CorrelationList correlate( final ExpressionData x, final ExpressionData y, final ECParams params )
  {
    ExpressionCorrelation instance = new ExpressionCorrelation( x, y, params );
    instance.run();
    return instance.getCorrelations();
  }
  
  
  public static CorrelationList correlate( final File elFile1, final File elFile2, final ECParams params ) throws IOException
  {
    ExpressionData ed1 = ExpressionData.load( elFile1 );
    ExpressionData ed2 = ExpressionData.load( elFile2 );
    return correlate( ed1, ed2, params );
  }
}
