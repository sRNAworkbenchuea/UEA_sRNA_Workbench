/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.filter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

/**
 * Handles execution of the Filter tool from a GUI in a separate thread.
 * @author Dan Mapleson and  Matthew Stocks
 */
public final class FilterRunner extends ToolRunner
{
  /**
   * Creates a new FilterRunner object
   * @param host The host GUI that wants to run this tool
   */
  public FilterRunner( ToolHost host )
  {
    super(host);
  }
  
  /**
   * Retrieves the filtering statistics from the previous run
   * @return The filtering statistics from the previous run
   */
  public FilterGroup getResults()
  {
    if (this.engine instanceof Filter)
    {
      Filter f_engine = Filter.class.cast( this.engine );
      return f_engine.getStats();
    }
    
    return null;
  }
  
  
  /**
   * Instructs the filter tool to write out the processed sequences to file
   * @param output File to write to
   */
  public void writeToFasta( File output )
  {
    try
    {
      if (this.engine instanceof Filter)
      {
        Filter f_engine = Filter.class.cast( this.engine );
        f_engine.writeDataToFasta( output );
      }
    }
    catch ( IOException ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }

  /**
   * Requests the the filter tool is executed.  Progress is displayed via the provided
   * StatusTracker object.
   * @param in_file The file to filter
   * @param out_dir The filtered files are placed here
   * @param params The filtering parameters to use
   * @param showingAbundancePlots should the program generate plots?
   * @param tracker The progress tracking components
   */
  public void runFilterTool(File in_file, File out_dir, FilterParams params, int showingAbundancePlots, StatusTracker tracker) 
  {
    try
    {
        FilterParams paramsModified = new FilterParams(params);
      if ( in_file == null )
      {
        throw new IOException( "Must specify input file containing sequences be filtered." );
      }

      if ( out_dir == null )
      {
        throw new IOException( "Must specify output file, which will contain filtered sequences." );
      }
      if(!out_dir.isDirectory())
      {
        throw new IOException( "Must specify output directory, which will contain files of filtered sequences." );
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control the filter tool." );
      }
      if(params.getAddDiscardLog())
      {
        if(!params.getDiscardLog().isDirectory())
        {
          throw new IOException( "Discard log must be a directory" );
        }
        else
        {
          String singleDiscardPath = out_dir.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_DISCARD_FILTER.fa";
          paramsModified.setDiscardLog( new File( singleDiscardPath ) );
        }
      }
      

      String singleOutPath  = out_dir.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_filter.fa";
      File outputFile = new File(singleOutPath);
      Filter f_engine = new Filter(in_file, outputFile, Tools.getNextDirectory(), paramsModified, tracker, showingAbundancePlots);

      this.run( f_engine );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }

  public boolean isComplete()
  {
    if (this.engine instanceof Filter)
    {
      Filter f_engine = Filter.class.cast( this.engine );
      
      return f_engine.getCompleted();
    }
    return false;
  }
}
