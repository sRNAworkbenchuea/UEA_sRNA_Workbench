/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import java.io.File;
import java.io.IOException;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.Correlation.CorrelationList;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * This JUnit test class will test all expression correlation methods: Pearson,
 * Cosine (EISEN), Spearman, and Kendall.  The expected results were generated
 * using the R implementation of the correlation methods.  The sRNA workbench implementation
 * can be found in the uk.ac.uea.cmp.srnaworkbench.tools.ec package.  
 * 
 * The tests all use the same 150 input genes and sRNA loci over 10 timeSeries.  
 * 
 * NOTE: The tests are made on the distances between the gene / sRNA loci, rather than
 * the correlation coefficients themselves, i.e. we test on values in the range of
 * 0 -> 2, rather than -1 -> 1.
 * 
 * @author ezb11yfu
 */
public class ExpressionCorrelationTest
{
  private static final String EC_PATH = 
    Tools.PROJECT_DIR + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + 
    DIR_SEPARATOR + "firepat" + DIR_SEPARATOR + "ec" + DIR_SEPARATOR;
  
  private static final File genesFile = new File(EC_PATH + "firepat_test150_genes.csv");
  private static final File srnasFile = new File(EC_PATH + "firepat_test150_srna_loci.csv");
  
  private static final double ALLOWED_DELTA = 0.0001;
  
  public ExpressionCorrelationTest()
  {
  }

  
  /**
   * Test of the Pearson Correlation method. Results from this Java version
   * should be identical with the R and Perl implementation.
   */
  @Test
  public void testPCCDistance() throws Exception
  {
    final ECMethods method = ECMethods.PEARSON;
    
    final File origCorrectFile = new File( EC_PATH + "pearson.csv" );
    final File correctFile = new File( EC_PATH + "pearson_good.csv" );
    final File testFile = new File( EC_PATH + "pearson_test.csv" );
    
    RToCorrelationListConvertor cnv = new RToCorrelationListConvertor( origCorrectFile, correctFile );
    double[] correct = cnv.process();
    
    assertTrue( testMethod( method, testFile, correct ) );    
  }
  
  /**
   * Test of the Cosine EISEN correlation method.
   */
  @Test
  public void testEISENDistance() throws Exception
  {
    final ECMethods method = ECMethods.COSINE;
    
    final File origCorrectFile = new File( EC_PATH + "eisen.csv" );
    final File correctFile = new File( EC_PATH + "eisen_good.csv" );
    final File testFile = new File( EC_PATH + "eisen_test.csv" );
    
    RToCorrelationListConvertor cnv = new RToCorrelationListConvertor( origCorrectFile, correctFile );
    double[] correct = cnv.process();
    
    assertTrue( testMethod( method, testFile, correct ) );
  }
  
  /**
   * Test of Spearman's ranking correlation method.  Results from this Java version
   * should be identical with the R implementation.
   */
  @Test
  public void testSPEARDistance() throws Exception
  {
    final ECMethods method = ECMethods.SPEARMAN;
    
    final File origCorrectFile = new File( EC_PATH + "spearman.csv" );
    final File correctFile = new File( EC_PATH + "spearman_good.csv" );
    final File testFile = new File( EC_PATH + "spearman_test.csv" );
    
    RToCorrelationListConvertor cnv = new RToCorrelationListConvertor( origCorrectFile, correctFile );
    double[] correct = cnv.process();
    
    assertTrue( testMethod( method, testFile, correct ) );
  }
  
  /**
   * Test of Kendall's Tau correlation method.  This method will currently
   * fail due to differences in the R implementation of Kendall's TAU correlation
   * and this Java implementation.  The difference comes in with the implementation 
   * of the mathematical sign function.  In R this appears to be defined as: x < 0 -> -1 and
   * x >= 0 -> 1, whereas the Java Math.signum() method is defined as follows: 
   * "zero if the argument is zero, 1.0 if the argument is greater than zero, 
   * -1.0 if the argument is less than zero.".  This means the resulting data will
   * contain differences where value to be evaluated is 0.
   */
  @Test
  public void testTAUDistance() throws Exception
  {
    final ECMethods method = ECMethods.KENDALL;
    
    final File origCorrectFile = new File( EC_PATH + "kendall.csv" );
    final File correctFile = new File( EC_PATH + "kendall_good.csv" );
    final File testFile = new File( EC_PATH + "kendall_test.csv" );
    
    RToCorrelationListConvertor cnv = new RToCorrelationListConvertor( origCorrectFile, correctFile );
    double[] correct = cnv.process();
    
    assertTrue( testMethod( method, testFile, correct ) );
  }
  
  
  
  
  
  // ********* Helper methods ************
  
  /**
   * Creates a default ECParams object to use.  Sets similarity threshold to 0,
   * which allows through all results.
   * @param method The method to use.
   * @return The new default parameters object.
   */
  protected ECParams makeDefaultParams( final ECMethods method )
  {
    ECParams params = new ECParams.Builder()
      .setSimilarityThreshold( 0 )
      .setMethod( method.name() )
      .build();
    
    return params;
  }
  
  /**
   * Used to implement the tests for each method in a consistent way.  This method
   * required the expression correlation method to use, the output file to create, 
   * which will store the produced test data for debugging purposes, and the expected 
   * distances with which to test, which allow us to determine if the correlation
   * method is working as expected.
   * @param method The correlation method to use
   * @param outputFile The output file to create which will store the produced test
   * data for debugging purposes
   * @param expectedDistances The expected distances with which to test, which allow us to determine if the correlation
   * method is working as expected.
   * @return true if test was passed, false if test failed.
   * @throws IOException Thrown if there were any issues writing out the debug data
   */
  protected boolean testMethod( ECMethods method, File outputFile, double[] expectedDistances ) throws IOException
  {
    // Make the parameters object
    ECParams params = makeDefaultParams(method);
    
    // Create the test results, using the defined input files and the new parameters object.
    CorrelationList corrList = ExpressionCorrelation.correlate( genesFile, srnasFile, params);
    
    // Save the results for debugging purposes (Keep in mind that if comparing directly
    // against the expected results file that there is a mismatch between correlation
    // coefficients and distances.
    corrList.save( outputFile );
    
    // Just get the distances from the test results to compare against the expected
    // results.
    double[] testDistances = corrList.getDistances();
    
    // Output some status messages to the console.
    System.out.println( "Method: " + method.name() );
    System.out.println( "# Input Elements:  " + expectedDistances.length );
    System.out.println( "# Output Elements: " + corrList.size() );
    
    // Compare the test results against the expected results    
    boolean success = arraysSimilar( expectedDistances, testDistances, ALLOWED_DELTA );
    
    // Output to console so we have another record of the test status.
    String status = success ? "completed successfully" : "failed";
    System.out.println("Test " + status);
    
    return success;
  }
  
 
  /**
   * Used to determine if all the elements in two arrays are similar to each other.  
   * The threshold which defines what is "similar" and what is not must be provided by 
   * the client.   * 
   * @param a First array of double values to test against second array
   * @param b Second array of double values to test against first array
   * @param threshold The maximum allowed difference between values, which allows
   * us to determine what is "similar" and what is not.
   * @return true if arrays are similar to each other, or false if not.
   */
  protected boolean arraysSimilar( double[] a, double[] b, double threshold )
  {
    if (a == null && b == null)
      return true;
    
    if (a == b)
      return true;
    
    if (a == null || b == null)
    {
      System.out.println("***Either array a or b is null");
      return false;
    }
    
    if (a.length != b.length)
    {
      System.out.println("***Arrays are of different length");
      return false;
    }
    
    boolean success = true;
    double min = 0.0;
    double max = 0.0;
    for( int i = 0; i < a.length; i++ )
    {
      double diff = a[i] - b[i];
      
      if (Math.abs( diff ) > threshold)
      {
        min = Math.min( min, diff );
        max = Math.max( max, diff );
        
        System.out.println("***Element " + i + " differs by: " + diff);
        
        success = false;
      }
    }
    
    if ( !success )
    {
      System.out.println("***Elements differ from: " + min + " to " + max);
    }
    
    return success;
  }
}
