/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.File;
import java.io.IOException;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.junit.Test;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.MirprofParams;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.ParesnipParams;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoParams;
import uk.ac.uea.cmp.srnaworkbench.tools.tasi.TasiParams;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author ezb11yfu
 */
public class ToolBoxTest
{
  private static String toolBoxPath = Tools.PROJECT_DIR + DIR_SEPARATOR + "test" + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "toolbox" + DIR_SEPARATOR;
  
  public ToolBoxTest()
  {
  }


  @Test
  public void produceDefaultParamsFiles() throws IOException
  {
    new AdaptorRemoverParams().save( new File( toolBoxPath + "default_adaptorremover_params.cfg") );
    new FilterParams().save( new File( toolBoxPath + "default_filter_params.cfg") );
    new MiRCatParams().save( new File( toolBoxPath + "default_mircat_params.cfg") );
    new MirprofParams().save( new File( toolBoxPath + "default_mirprof_params.cfg") );
    new ParesnipParams().save( new File( toolBoxPath + "default_paresnip_params.cfg") );
    new SiLoCoParams().save( new File( toolBoxPath + "default_siloco_params.cfg") );
    new TasiParams().save( new File( toolBoxPath + "default_tasi_params.cfg") );
    new NormalisationParams().save( new File( toolBoxPath + "default_norm_params.cfg" ));
  }
}
