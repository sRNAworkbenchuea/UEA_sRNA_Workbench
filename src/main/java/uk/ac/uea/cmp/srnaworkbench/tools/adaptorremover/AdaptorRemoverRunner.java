/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatOpenFileOptions;

/**
 * 
 * @author ezb11yfu
 */
public final class AdaptorRemoverRunner extends ToolRunner
{
  private int showingAbundancePlots = JOptionPane.NO_OPTION;
  private String singleOutPath = null;
  private boolean streamMode;
  
  public AdaptorRemoverRunner(ToolHost host, int showingAbundancePlots, boolean streamMode)
  {
    super(host);
    
    this.showingAbundancePlots = showingAbundancePlots;
    
    this.streamMode = streamMode;
  }

  public AdaptorRemoverRunner( ToolHost host, int showingAbundancePlots )
  {
    this(host, showingAbundancePlots, false);
  }
 
  
  /**
   * Gets the current execution statistics from the adaptor remover
   * @return execution statistics
   */
  public List<FilterStage> getResults()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      
      return ar_engine.getResults();
    }
    
    return null;
  }

  public AdaptorRemoverParams.HD_Protocol getHDProt()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      
      return ar_engine.getHDProt();
    }
    
    return null;
  }
  /**
   * Gets the length distribution statistics from the adaptor remover
   * @return length distribution statistics
   */
  public Map<Integer, FilterStage> getLengths()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      return ar_engine.getLengths();
    }
    
    return null;
  }

  public String getNewFileName()
  {
    return singleOutPath;
  }
  
  /**
   * Gets the processed sequences after adaptor removal
   * @return Processed sequences sans adaptors
   */
  public FastaMap getData()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      return ar_engine.getProcessedSeqs();
    }
    
    return null;
  }
  
  public FastaMap getHDData()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      return ar_engine.getProcessedSeqs_PRE_HD();
    }
    
    return null;
  }
  
  
  /**
   * Instructs the adaptor remover tool to write out the processed sequences to file
   * @param output File to write to
   */
  public void writeToFasta( File output )
  {
    try
    {
      if (this.engine instanceof StreamAdaptorRemover)
      {
        AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
        ar_engine.writeSeqsToFasta( output );
      }
    }
    catch ( IOException ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );
      this.host.showErrorDialog( ex.getMessage() );
    }
  }
  
  
  /**
   * Instructs the adaptor remover tool to start running
   * @param infile File containing reads with adaptors
   * @param outfile File to write to containing sequences with adaptors removed
   * @param ar_params The parameters describing how the engine should run
   * @param progbar Progress bar to be updated during execution
   * @param status Status box to be updated during execution
   */
  public void runAdaptorRemover( File in_file, File out_file, AdaptorRemoverParams ar_params, StatusTracker tracker )
  {
    try
    {
      if ( in_file == null )
      {
        throw new IOException( "Must specify input file(s) containing sequences with adaptors to be removed." );
      }

      if ( out_file == null )
      {
        throw new IOException( "Must specify output file directory, for files containing sequences with adaptors removed." );
      }
      
      if(!out_file.isDirectory())
      {
        throw new IOException( "Output file must be a directory." );
      }

      if ( ar_params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control the adaptor remover." );
      }

      if ( ar_params.getDiscardLog() != null )
      {
        if ( !ar_params.getDiscardLog().isDirectory() )
        {
          throw new IOException( "Discard log must be a directory to output discarded sequences." );
        }
        else
        {
          String singleDiscardPath = out_file.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_DISCARDED.fa";
          ar_params.setDiscardLog( new File( singleDiscardPath ) );
          //ar_params.setDiscardLog( singleDiscardPath );
        }
      }
      
      

      singleOutPath  = out_file.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( in_file.getName() ) + "_AR.fa";
      AdaptorRemover ar_engine;
      if(streamMode)
        ar_engine = new StreamAdaptorRemover( in_file, new File(singleOutPath), ar_params, showingAbundancePlots, tracker );
      else
        ar_engine = new AdaptorRemover( in_file, new File(singleOutPath), ar_params, showingAbundancePlots, tracker );
      this.run( ar_engine );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }

  public boolean isComplete()
  {
    if (this.engine instanceof AdaptorRemover)
    {
      AdaptorRemover ar_engine = AdaptorRemover.class.cast( this.engine );
      
      return ar_engine.getCompleted();
    }
    return false;
  }
}
