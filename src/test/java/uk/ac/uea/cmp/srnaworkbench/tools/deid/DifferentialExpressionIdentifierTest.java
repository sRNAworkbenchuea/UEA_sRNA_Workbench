/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.deid;

import java.io.File;
import java.io.IOException;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import uk.ac.uea.cmp.srnaworkbench.tools.de.DEParams;
import uk.ac.uea.cmp.srnaworkbench.tools.de.DifferentialExpression;
import uk.ac.uea.cmp.srnaworkbench.data.correlation.ExpressionData;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

/**
 *
 * @author ezb11yfu
 */
public class DifferentialExpressionIdentifierTest
{
  private static final String DE_PATH = 
    Tools.PROJECT_DIR + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + 
    DIR_SEPARATOR + "firepat" + DIR_SEPARATOR + "dei" + DIR_SEPARATOR;
  
  private File inFile1 = new File(DE_PATH + "sRNA_orgPart.csv");
  
  private File logDir = new File(DE_PATH + "logs");
  
  public DifferentialExpressionIdentifierTest()
  {
    AppUtils.INSTANCE.setVerbose( true );
    try
    {
      WorkbenchLogger.LOGGER.recreateHandlers( logDir );
    }
    catch ( IOException ex )
    {
      
    }
  }

  protected DEParams makeDefaultParams( String method )
  {
    DEParams params = new DEParams.Builder()
      .setOrdered( false )
      .setOffset( 20 )
      .setPctThreshold( 100 )
      .setMethod( "OFFSET_FOLD_CHANGE" )
      .build();
    
    return params;
  }

  /**
   * Test of OFFSET_FOLD_CHANGE method
   */
  @Test
  public void testOFC() throws Exception
  {
    final String method = "OFFSET_FOLD_CHANGE";
    final File outFileOFC_good = new File(DE_PATH + "sRNA_orgPart_OFC_-good.csv");
    final File outFileOFC_test = new File(DE_PATH + "sRNA_orgPart_OFC_-test.csv");
  
    DEParams params = makeDefaultParams( method );
    
    ExpressionData result = DifferentialExpression.process( inFile1, params );
    result.getExpressionLevels().save( outFileOFC_test );    
    
    String input_md5 = FileUtils.getSmallFileMD5( inFile1 );
    String good_md5 = FileUtils.getSmallFileMD5( outFileOFC_good );
    String test_md5 = FileUtils.getSmallFileMD5( outFileOFC_test );
    
    System.out.println("Input MD5: " + input_md5);
    System.out.println("Good MD5: " + good_md5);
    System.out.println("Test MD5: " + test_md5);
    
    // Checks the output is correct
    assertTrue(good_md5.equals( test_md5 ));
        
    // Checks the MD5 sum routine isn't rubbish
    assertFalse(input_md5.equals( good_md5 ) );
  }
  
  /**
   * Test of UNUSUAL_RATIO method
   */
  @Test
  public void testUnusualRatio() throws Exception
  {
    final String method = "UNUSUAL_RATIO";
    final File outFileUnusualRatio_good = new File(DE_PATH + "sRNA_orgPart_UnusualRatio_-good.csv");
    final File outFileUnusualRatio_test = new File(DE_PATH + "sRNA_orgPart_UnusualRatio_-test.csv");
  
    DEParams params = makeDefaultParams( method );
    
    ExpressionData result = DifferentialExpression.process( inFile1, params );
    result.getExpressionLevels().save( outFileUnusualRatio_test );    
    
    String input_md5 = FileUtils.getSmallFileMD5( inFile1 );
    String good_md5 = FileUtils.getSmallFileMD5( outFileUnusualRatio_good );
    String test_md5 = FileUtils.getSmallFileMD5( outFileUnusualRatio_test );
    
    System.out.println("Input MD5: " + input_md5);
    System.out.println("Good MD5: " + good_md5);
    System.out.println("Test MD5: " + test_md5);
    
    // Checks the output is correct
    assertTrue(good_md5.equals( test_md5 ));
        
    // Checks the MD5 sum routine isn't rubbish
    assertFalse(input_md5.equals( good_md5 ) );
  }
  
  /**
   * Test of MODIFIED_SAM method
   */
  @Test
  public void testModifiedSAM() throws Exception
  {
    final String method = "MODIFIED_SAM";
    final File outFileModifiedSAM_good = new File(DE_PATH + "sRNA_orgPart_ModifiedSAM_-good.csv");
    final File outFileModifiedSAM_test = new File(DE_PATH + "sRNA_orgPart_ModifiedSAM_-test.csv");
  
    DEParams params = makeDefaultParams( method );
    
    ExpressionData result = DifferentialExpression.process( inFile1, params );
    result.getExpressionLevels().save( outFileModifiedSAM_test );    
    
    String input_md5 = FileUtils.getSmallFileMD5( inFile1 );
    String good_md5 = FileUtils.getSmallFileMD5( outFileModifiedSAM_good );
    String test_md5 = FileUtils.getSmallFileMD5( outFileModifiedSAM_test );
    
    System.out.println("Input MD5: " + input_md5);
    System.out.println("Good MD5: " + good_md5);
    System.out.println("Test MD5: " + test_md5);    
    
    // Checks the output is correct
    assertTrue(good_md5.equals( test_md5 ));
        
    // Checks the MD5 sum routine isn't rubbish
    assertFalse(input_md5.equals( good_md5 ) );
  }
}
