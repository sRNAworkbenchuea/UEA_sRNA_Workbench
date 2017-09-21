/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.normalisation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;

/**
 *
 * @author Matt
 */
@Ignore("Outdated")
public class NormalisationTest
{
      private final static String NORM_PATH = Tools.PROJECT_DIR + DIR_SEPARATOR + "src" + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "norm" + DIR_SEPARATOR;
      private final static String INPUT_FILES =        
        NORM_PATH + "ath_366868_head.fa" + "," +
        NORM_PATH + "ath_366868_head2.fa";
      
      private final static String GENOME_FILE = "TutorialData/FASTA/GENOME/Ath_TAIR9.fa";
      private final static String PARAMS_FILE = NORM_PATH + "params.txt";
      
      private static final String OUT_SUFFIX = "_out.csv";
      private static final String R_OUT_SUFFIX = "_Rout.csv";
      
      NormalisationParams params;
         
      
      public NormalisationTest()
      {

        this.params = new NormalisationParams.Builder().setWeightByHits( false ).build();
        try
        {
          this.params.save( new File( PARAMS_FILE ) );
        }
        catch ( IOException ex )
        {
          
        }
      }
      
      @Ignore("Outdated")
      @Test
      public void testQuantileNormalisation() throws Exception
      {
        NormalisationType normType = NormalisationType.QUANTILE;
        
        String qnorm_output = NORM_PATH + normType.toString() + OUT_SUFFIX ;
        
        Map<String,String> argmap = new HashMap<>();
        argmap.put( "srna_file_list", INPUT_FILES );
        argmap.put( "out_dir", NORM_PATH);
        argmap.put( "params", PARAMS_FILE);
        argmap.put( "genome", GENOME_FILE );
        argmap.put( "normalisation_type", NormalisationType.QUANTILE.toString());
        argmap.put( "c", "" );
        
        Tools.initialCLISetup();
        ToolBox.NORMALISE_TOOL.startTool( argmap );
        
        assertTrue(testCSVResults(normType));

      }
      
      @Ignore("Outdated")
      protected boolean testCSVResults(NormalisationType type) throws Exception
      {
        String good_md5 = FileUtils.getSmallFileMD5( new File( NORM_PATH + type.toString() + R_OUT_SUFFIX ) );
        String output_md5 = FileUtils.getSmallFileMD5( new File( NORM_PATH + type.toString() + OUT_SUFFIX) );
        
        return(good_md5.equals( output_md5 ));
      }
}
