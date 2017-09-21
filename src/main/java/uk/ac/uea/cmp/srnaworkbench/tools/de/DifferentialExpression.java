/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.de;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionData;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionLevels;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class DifferentialExpression extends RunnableTool
{
  private final static String TOOL_NAME = "DIFF_EXPR";
  // Input vars
  private final ExpressionData expData;
  private final DEParams params;
  // Output vars
  private ExpressionData significantEntries;

  /**
   * Creates a DifferentialExpressionIdentifier object, with the input data set
   * provided. User must specify parameters which determine the significance
   * threshold required for differential expression to be considered significant
   * and whether the input data set is ordered. In order to process the data
   * call the "process" method. In order to retrieve the results call the
   * "getResult" method.
   *
   * @param expData The data matrix to process, containing sample data in each
   * column and individual sRNAs or sRNA loci in each row.
   * @param params Parameters object containing all the settings to use for
   * calculating differetial expression of sRNAs/genes.
   */
  public DifferentialExpression( ExpressionData expData, DEParams params )
  {
    super( TOOL_NAME );

    if ( expData == null || expData.getData() == null || expData.getData().getRowDimension() == 0 || expData.getData().getColumnDimension() == 0 )
    {
      throw new IllegalArgumentException( "data matrix must contain data to work with" );
    }

    this.expData = expData;
    this.params = params;
  }

  /**
   * Retrieves the result of computing the differential expression of sRNAs
   * between samples. The object returned contains a list of values indicating
   * whether a given sRNA or sRNA loci is differentially expressed across the
   * input samples provided.
   *
   * @return A list of values indicating whether a given sRNA or sRNA loci is
   * differentially expressed across the input samples provided.
   */
  public ExpressionData getResult()
  {
    return this.significantEntries;
  }

  @Override
  protected void process() throws Exception
  {
    LOGGER.log( Level.FINE, "Rows in original data: {0}", this.expData.rows() );

    DEMethods method = DEMethods.valueOf( this.params.getMethod() );

    // Compute the expression level using the selected method.
    ExpressionLevels expLevels = method.compute( this.expData, this.params );

    // Add the expression levels to the original object.
    this.expData.setExpressionLevels( expLevels );

    // Create an new ExpressionData object containing only those entries that pass
    // the user specified filtering criteria.
    this.significantEntries = this.expData.filter( this.params.getPctThreshold(), this.params.getMinExpressionLevel(), this.params.getMinAbundance() );

    LOGGER.log( Level.FINE, "Rows in filtered fc data: {0}", this.significantEntries.rows() );
  }

  /**
   * Helper method that returns a set of elements that are sufficiently
   * differentially expressed from a file using a supplied threshold.
   *
   * @param dataFile The file in csv format containing elements for differential
   * expression analysis.
   * @param params Parameters object containing all the settings to use for
   * calculating differential expression of sRNAs/genes.
   * @return Entries that are worth analysing for differential expression.
   */
  public static ExpressionData process( File dataFile, DEParams params ) throws IOException
  {
    ExpressionData expData = ExpressionData.load( dataFile );
    return process( expData, params );
  }
  
  public static ExpressionData process( ExpressionData expData, DEParams params ) throws IOException
  {    
    DifferentialExpression dei = new DifferentialExpression( expData, params );
    dei.run();
    return dei.getResult();
  }
}
