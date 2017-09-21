/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author ezb11yfu
 */
public class AdaptorRemoverTest
{
  private final static String AR_PATH = Tools.PROJECT_DIR + DIR_SEPARATOR + "src" + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "ar" + DIR_SEPARATOR;
  private static File inFile1 = new File(AR_PATH + "test1.fa");
  
  private static File goodOutFile1 = new File(AR_PATH + "test1_-ar_good.fa");
  private static File paramsFile = new File(AR_PATH + "params.txt");
    
  private AdaptorRemoverParams paramsInternal;
  private AdaptorRemoverParams paramsExternal;
  
  
  public AdaptorRemoverTest()
  {
    try
    {
      this.paramsInternal = new AdaptorRemoverParams.Builder().set5PrimeAdaptor( "ATCGGCTA" ).set3PrimeAdaptor( "TTTTAAAA" ).build();
      this.paramsExternal = AdaptorRemoverParams.load( new AdaptorRemoverParams(), paramsFile );
    }
    catch(Exception e)
    {      
    }    
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  
  /**
   * Test of run method, of class AdaptorRemover using params built in code.
   */
  @Test
  public void testEngineParamsInternal() throws Exception
  {
    System.out.println( "Testing run method with simple file containing one sequence that requires 5' and 3' adaptor removal.  Using hard coded params object." );
    
    File testOutFileParamsInternal = new File(AR_PATH + "test1_-ar_pi_test.fa");
    AdaptorRemover instance = new AdaptorRemover( inFile1, testOutFileParamsInternal, paramsInternal );
    instance.run();
    
    assertTrue( testARResults( testOutFileParamsInternal ) );
  }
  
  /**
   * Test of run method, of class AdaptorRemover using params loaded from file
   */
  @Test
  public void testEngineParamsExternal() throws Exception
  {
    System.out.println( "Testing run method with simple file with one sequence that requires 5' and 3' adaptor removal.  Using params object loaded from file." );
    
    File testOutFileParamsExternal = new File(AR_PATH + "test1_-ar_pe_test.fa");
    AdaptorRemover instance = new AdaptorRemover( inFile1, testOutFileParamsExternal, paramsExternal );
    instance.run();
    
    assertTrue( testARResults( testOutFileParamsExternal ) );
  }
  
  /**
   * Test of run method, of class AdaptorRemover.
   */
  @Test
  public void testCLI() throws Exception
  {
    System.out.println( "Testing run method using CLI with simple file with one sequence that requires 5' and 3' adaptor removal." );
    
    File testOutFileCLI = new File(AR_PATH + "test1_-ar_cli_test.fa");
    
    Map<String,String> argmap = new HashMap<String, String>();
    argmap.put( "f", "" );
    argmap.put( "srna_file", inFile1.getPath() );
    argmap.put( "out_file", testOutFileCLI.getPath() );
    argmap.put( "params", paramsFile.getPath() );
    
    Tools.initialCLISetup();
    ToolBox.ADAPTOR_TOOL.startTool( argmap );
    
    assertTrue( testARResults( testOutFileCLI ));
  }
  
  
  
  // ******** Helper methods ********
  
  
  protected boolean testARResults( final File testFile ) throws Exception
  {
    String input_md5 = FileUtils.getSmallFileMD5( inFile1 );
    String good_md5 = FileUtils.getSmallFileMD5( goodOutFile1 );
    String test_md5 = FileUtils.getSmallFileMD5( testFile );
    
    System.out.println("Input MD5: " + input_md5);
    System.out.println("Good MD5: " + good_md5);
    System.out.println("Test MD5: " + test_md5);
    
    // Checks the output is correct
    return good_md5.equals( test_md5 );
  }

  /**
   * Test of getResults method, of class AdaptorRemover.
   */
  @Test
  public void testGetResults()
  {
    try
    {
      System.out.println( "getResults" );
      AdaptorRemover instance = new AdaptorRemover();
      List<FilterStage> expResult = null;
      List<FilterStage> result = instance.getResults();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of getLengths method, of class AdaptorRemover.
   */
  @Test
  public void testGetLengths()
  {
    try
    {
      System.out.println( "getLengths" );
      AdaptorRemover instance = new AdaptorRemover();
      Map<Integer, FilterStage> expResult = null;
      Map<Integer, FilterStage> result = instance.getLengths();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of getProcessedSeqs method, of class AdaptorRemover.
   */
  @Test
  public void testGetProcessedSeqs()
  {
    try
    {
      System.out.println( "getProcessedSeqs" );
      AdaptorRemover instance = new AdaptorRemover();
      FastaMap expResult = null;
      FastaMap result = instance.getProcessedSeqs();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of clear method, of class AdaptorRemover.
   */
  @Test
  public void testClear()
  {
    try
    {
      System.out.println( "clear" );
      AdaptorRemover instance = new AdaptorRemover();
      instance.clear();
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of process method, of class AdaptorRemover.
   */
  @Test
  public void testProcess() throws Exception
  {
    System.out.println( "process" );
    AdaptorRemover instance = new AdaptorRemover();
    instance.process();
    // TODO review the generated test code and remove the default call to fail.
    fail( "The test case is a prototype." );
  }

  /**
   * Test of writeSeqsToFasta method, of class AdaptorRemover.
   */
  @Test
  public void testWriteSeqsToFasta() throws Exception
  {
    System.out.println( "writeSeqsToFasta" );
    File out_file = null;
    AdaptorRemover instance = new AdaptorRemover();
    instance.writeSeqsToFasta( out_file );
    // TODO review the generated test code and remove the default call to fail.
    fail( "The test case is a prototype." );
  }

  /**
   * Test of generateResultsFile method, of class AdaptorRemover.
   */
  @Test
  public void testGenerateResultsFile() throws Exception
  {
    System.out.println( "generateResultsFile" );
    File results_file = null;
    Map<Integer, FilterStage> seq_lengths = null;
    List<FilterStage> results = null;
    AdaptorRemover instance = new AdaptorRemover();
    instance.generateResultsFile( results_file, seq_lengths, results );
    // TODO review the generated test code and remove the default call to fail.
    fail( "The test case is a prototype." );
  }

  /**
   * Test of generateBaseSpaceResultsFile method, of class AdaptorRemover.
   */
  @Test
  public void testGenerateBaseSpaceResultsFile() throws Exception
  {
    System.out.println( "generateBaseSpaceResultsFile" );
    File results_file = null;
    Map<Integer, FilterStage> seq_lengths = null;
    List<FilterStage> results = null;
    AdaptorRemover instance = new AdaptorRemover();
    instance.generateBaseSpaceResultFiles( results_file, seq_lengths, results );
    // TODO review the generated test code and remove the default call to fail.
    fail( "The test case is a prototype." );
  }

  /**
   * Test of getCompleted method, of class AdaptorRemover.
   */
  @Test
  public void testGetCompleted()
  {
    try
    {
      System.out.println( "getCompleted" );
      AdaptorRemover instance = new AdaptorRemover();
      boolean expResult = false;
      boolean result = instance.getCompleted();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of getHDProt method, of class AdaptorRemover.
   */
  @Test
  public void testGetHDProt()
  {
    try
    {
      System.out.println( "getHDProt" );
      AdaptorRemover instance = new AdaptorRemover();
      AdaptorRemoverParams.HD_Protocol expResult = null;
      AdaptorRemoverParams.HD_Protocol result = instance.getHDProt();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }

  /**
   * Test of getProcessedSeqs_PRE_HD method, of class AdaptorRemover.
   */
  @Test
  public void testGetProcessedSeqs_PRE_HD()
  {
    try
    {
      System.out.println( "getProcessedSeqs_PRE_HD" );
      AdaptorRemover instance = new AdaptorRemover();
      FastaMap expResult = null;
      FastaMap result = instance.getProcessedSeqs_PRE_HD();
      assertEquals( expResult, result );
      // TODO review the generated test code and remove the default call to fail.
      fail( "The test case is a prototype." );
    }
    catch ( Exception ex )
    {
      
    }
  }
}
