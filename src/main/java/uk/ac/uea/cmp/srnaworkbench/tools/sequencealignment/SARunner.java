/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.sequencealignment;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;

import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

/**
 * Handler for executing a sequence alignment tool in an underlying thread.
 * @author Dan Mapleson
 */
public final class SARunner extends ToolRunner
{
  /**
   * Creates a new SARunner object
   * @param host The host GUI that wants to run this tool
   */
  public SARunner( ToolHost host )
  {
    super(host);
  }
  
  /**
   * Requests that the selected sequence alignment tool is executed.  Progress is displayed via the provided
   * StatusTracker object.
   * @param pattern_file The short reads to be aligned
   * @param database_file The long reads to target
   * @param out_file The file to output
   * @param params The patman processing parameters to use
   * @param tracker The progress tracking components
   */
  public void runSequenceAligner(File in_file, File database_file, File out_file, PatmanParams params, StatusTracker tracker)
  {
    try
    {
      if ( in_file == null )
      {
        throw new IOException( "Must specify short_read file containing sequences to be aligned." );
      }
      
      if ( database_file == null )
      {
        throw new IOException( "Must specify long read file containing target sequences." );
      }
      
      if ( out_file == null )
      {
        throw new IOException( "Must specify a file to write output to." );
      }
      
      if(!out_file.isDirectory())
      {
        throw new IOException( "Output file must be a directory." );
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control the sequence aligner." );
      }
      
      String singleOutPath  = out_file.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_Align.patman";
      
      PatmanRunner pr = new PatmanRunner( in_file, database_file, new File(singleOutPath),
        Tools.getNextDirectory(),
        params,
        tracker );
      
      this.run( pr );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }

  boolean isComplete()
  {
    if (this.engine instanceof PatmanRunner)
    {
      PatmanRunner sa_engine = PatmanRunner.class.cast( this.engine );
      
      return sa_engine.getCompleted();
    }
    return false;
  }
}
