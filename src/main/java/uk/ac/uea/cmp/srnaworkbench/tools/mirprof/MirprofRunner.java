/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterGroup;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.Mirprof.OrganismGroup;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Runs Mirprof in a separate thread and manages comms to and from that thread.
 * @author Dan Mapleson
 */
public final class MirprofRunner extends ToolRunner
{
  private boolean usingGenome= true;
  /**
   * Creates a new MirprofRunner object
   * @param host The host GUI that wants to run this tool
   */
  public MirprofRunner( ToolHost host )
  {
    super(host);
  }
  
  /**
   * Gets the mirprof engine
   * @return The mirprof engine
   */
  public Mirprof getEngine()
  {
    if (this.engine instanceof Mirprof)
    {
      Mirprof m_engine = Mirprof.class.cast( this.engine );
      return m_engine;
    }
    
    return null;
  }
  
  /**
   * Retrieves the matches that have been produced in the previous run
   * @return The mirprof matches
   */
  public OrganismGroup getResults()
  {
    if (this.engine instanceof Mirprof)
    {
      Mirprof m_engine = Mirprof.class.cast( this.engine );
      return m_engine.getMatches();
    }
    
    return null;
  }
  
  /**
   * Retrieves the filter statistics that have been produced for each sample in the previous run
   * @return The filtering statistics
   */
  public List<FilterGroup> getFilterStats()
  {
    if (this.engine instanceof Mirprof)
    {
      Mirprof m_engine = Mirprof.class.cast( this.engine );
      return m_engine.getFilterStats();
    }
    
    return null;
  }
  
  
  /**
   * Instructs mirprof to write out the processed sequences to file
   * @param output File to write to
   */
  public void writeFasta( File output )
  {
    try
    {
      if (this.engine instanceof Mirprof)
      {
        Mirprof m_engine = Mirprof.class.cast( this.engine );
        m_engine.writeFASTA( output );
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }
  public boolean getUsingGenome()
  {
    return this.usingGenome;
  }
  /**
   * Instructs mirprof to write out the results to file
   * @param output File to write to
   */
  public void writeResults( File output )
  {
    try
    {
      if (this.engine instanceof Mirprof)
      {
        Mirprof m_engine = Mirprof.class.cast( this.engine );
        m_engine.writeTable( output );
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }
  
  /**
   * Requests that mirprof is executed.  Progress is displayed via the provided
   * StatusTracker object.
   * @param in_files The samples to process
   * @param mirbase The mirBase file containing known miRNAs to match against
   * @param genome The genome to align reads against (for normalisation purposes)
   * @param params The mirprof processing parameters to use
   * @param tracker The progress tracking components
   */
  public void runMirprof(List<File> in_files, File mirbase, File genome, MirprofParams params, StatusTracker tracker)
  {
    try
    {
      if ( in_files == null || in_files.isEmpty() )
      {
        throw new IOException( "Must specify input files containing sequences be processed." );
      }
      
      if ( mirbase == null )
      {
        throw new IOException( "Must specify mirbase version and category to match against" );
      }
      
      if ( genome == null )
      {
        usingGenome = false;
      }
      else
      {
        usingGenome = true;
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control miRProf." );
      }
      
      // This is a bit of a problems with the parameters class.  Genome should probably
      // be removed from the parameters class and controlled via mirprof class itself
      // For the time being though we've just hacked it in here... :(
      //params.setGenome( genome );

      Mirprof m_engine = new Mirprof(in_files, genome, mirbase, null, Tools.getNextDirectory(), params, tracker);
      this.run( m_engine );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }
}
