/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.io.File;
import java.io.IOException;
import javax.swing.JTable;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 *
 * @author ezb11yfu
 */
public class MiRCatRunner extends ToolRunner
{
  private Process_Hits_Patman m_engine;
  private StatusTracker tracker;  
  
  public MiRCatRunner( ToolHost host )
  {
      this( host, null, null );  
  }
  
  public MiRCatRunner ( ToolHost host, JTable output, StatusTracker tracker ) 
  {
    super( host );
    
    try
    {
      if (output == null)
      {
        throw new IOException( "Must provide output table to display miRCat reads." );
      }
      
      if (tracker == null)
      {
        throw new IOException( "Must provide a status tracker for monitoring job progress." );
      }
      
      this.tracker = tracker;
      
      this.m_engine = new Process_Hits_Patman( this.tracker );
      this.m_engine.addTableRef( output );
      this.m_engine.addTrackingComponents( tracker.getProgressBar(), tracker.getStatusLabel() );
    }
    catch ( IOException ioe )
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }
  
  /**
   * Gets the miRCat engine
   * @return The miRCat engine
   */
  public Process_Hits_Patman getEngine()
  {
    return this.m_engine;
  }


  public void runMirCat( File patman_file, File genome_file, File temp_dir, MiRCatParams params )
  {
    try
    {
      if ( patman_file == null )
      {
        throw new IOException( "Must provide patman file containing the aligned sRNA reads." );
      }

      if ( genome_file == null )
      {
        throw new IOException( "Must specify a genome to align reads against." );
      }
      
      if ( temp_dir == null )
      {
        throw new IOException( "Must specify a temp directory to store temporary files.");
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control miRCat." );
      }
      
      m_engine.setInputFile( patman_file );
      m_engine.setGenomeFile( genome_file );
      m_engine.setParams( params );
      m_engine.setupThreads( params.getThreadCount());
      m_engine.setTempDir( temp_dir );

      this.run( m_engine );
    }
    catch ( IOException ioe )
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }

  public void runPipeline( final MiRCatParams params, final MiRCatOpenFileOptions open_options )
  {
    if( (open_options == null) || (params == null) )
      return;
    new Thread()
    {
      @Override
      public void run()
      {
        File input_file = open_options.getSRNAFile();
        File genome_file = open_options.getGenomeFile();
        File temp_dir = open_options.getTempDir();

        if ( open_options.isPreProcessing() )
        {
          open_options.runProcedures( tracker );
          input_file = open_options.getProcessedSRNAFile();
        }
        runMirCat( input_file, genome_file, temp_dir, params );
      }
    }.start();    
  }


}
