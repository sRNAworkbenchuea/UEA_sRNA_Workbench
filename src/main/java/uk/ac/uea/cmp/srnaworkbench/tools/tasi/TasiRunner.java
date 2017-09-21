/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Runs the tasi algorithm in a managed thread.
 * @author Dan Mapleson
 */
public final class TasiRunner extends ToolRunner
{
  /**
   * Creates a new TasiRunner object
   * @param host The host GUI that wants to run this tool
   */
  public TasiRunner( ToolHost host )
  {
    super(host);
  }
  
  
  /**
   * Retrieves the list of TAS loci that have been predicted in the previous run
   * @return The list of predicted TAS loci
   */
  public List<Locus> getResults()
  {
    if (this.engine instanceof TasiAnalyser)
    {
      TasiAnalyser t_engine = TasiAnalyser.class.cast( this.engine );
      return t_engine.getResults();
    }
    
    return null;
  }
  
  /**
   * Gets the currently specified parameters
   * @return Current tool parameters
   */
  public TasiParams getParams()
  {
    if (this.engine instanceof TasiAnalyser)
    {
      TasiAnalyser t_engine = TasiAnalyser.class.cast( this.engine );
      return t_engine.getParams();
    }
    
    return null;
  }
  
  /**
   * Instructs the tasi tool to write out the results to file
   * @param output File to write to
   */
  public void writeLoci( File output )
  {
    try
    {
      if (this.engine instanceof TasiAnalyser)
      {
        TasiAnalyser t_engine = TasiAnalyser.class.cast( this.engine );
        t_engine.writeLoci( output );
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }
  
  /**
   * Instructs the tasi tool to write out the phased sRNAs to file
   * @param output File to write to
   */
  public void writeSrnas( File output )
  {
    try
    {
      if (this.engine instanceof TasiAnalyser)
      {
        TasiAnalyser t_engine = TasiAnalyser.class.cast( this.engine );
        t_engine.writeSrnas( output );
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }
  
  /**
   * Requests the the tasi tool is executed.  Progress is displayed via the provided
   * StatusTracker object.
   * @param in_file The file to process
   * @param params The tasi processing parameters to use
   * @param tracker The progress tracking components
   */
  public void runTasiTool(File in_file, TasiParams params, StatusTracker tracker)
  {
    try
    {
      if ( in_file == null )
      {
        throw new IOException( "Must specify input file containing sequences be filtered." );
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control the filter tool." );
      }

      TasiAnalyser t_engine = new TasiAnalyser(in_file, Tools.getNextDirectory(), params, tracker);

      this.run( t_engine );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }
}
