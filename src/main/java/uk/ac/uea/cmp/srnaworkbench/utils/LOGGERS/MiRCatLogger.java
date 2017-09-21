/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS;

import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

/**
 *
 * @author w0445959
 */ 
public class MiRCatLogger extends Logger
{
   private static final Level CONSOLE_LEVEL = Level.WARNING;
  private static final Level FILE_ERROR_LEVEL = Level.SEVERE;
  private static final Level FILE_INFO_LEVEL = Level.INFO;
  private static final Level FILE_DEBUG_LEVEL = Level.ALL;
  

  /* miRCat info log filename */
  private static final String INFO_LOG_FILENAME = "miRCat_Info.log";
  
  
  /* Log prefix */
  private static final String PREFIX = "MIRCAT: ";
  
  /* We only want one */
  public static MiRCatLogger MIRCAT_LOGGER = new MiRCatLogger();
  
  /**
   * Creates a new instance of the WorkbenchLogger.  This is a standalone logger
   * independent of any parents, including the root logger.
   */
  private MiRCatLogger()
  {
    this( new File( Tools.LOG_DIR ) );
  }
  
  /**
   * Creates a new instance of the WorkbenchLogger.  This is a standalone logger
   * independent of any parents, including the root logger.  Optionally, creates
   * an extra file handler for outputting debug messages.
   */
  private MiRCatLogger( File logDir)
  {
    super( MiRCatLogger.class.getName(), null );
    
    // Make this logger independent of the root logger
    this.setUseParentHandlers( false );
    
    try
    {
      recreateHandlers( logDir );
    }
    catch(IOException e)
    {
      // Not much we can do if there was any issues opening the log file.  Just output
      // to System.err
      System.err.println( "MIRCAT LOGGER ERROR: Failed to create log files." );
      System.err.println( "MIRCAT LOGGER ERROR: Exception: " + e.toString() );
      System.err.println( "MIRCAT LOGGER ERROR: Resuming." );
    }
    
    this.setLevel( Level.ALL );
  }
  
  @Override
  public void log( LogRecord record )
  {
    record.setMessage( PREFIX + (record.getMessage() == null ? "" : record.getMessage()) );
    super.log( record );
  }
   
  public final void recreateHandlers( File dir ) throws IOException
  {
    if ( dir == null )
      throw new NullPointerException( "Log Directory must be specified." );
    
    if ( !dir.isDirectory() )
      throw new IOException( "Path specified does not describe a directory: " + dir.getPath() );
    
    if ( !dir.canWrite() )
      throw new IOException( "Can't write to this location: " + dir.getPath() );
    
    File infoLogFile = new File( dir.getPath() + DIR_SEPARATOR + INFO_LOG_FILENAME );
    
 
    
    if ( infoLogFile.exists() && !infoLogFile.canWrite() )
      throw new IOException( "Can't write to info log file: " + infoLogFile.getPath() );
    

    // Clear any existing handlers
    for(Handler h : this.getHandlers())
    {
      this.removeHandler( h );
    }
    boolean verbose =  AppUtils.INSTANCE.verbose();
    // Setup for System.err handler (only works if running the server directly)
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel( verbose ? Level.ALL : CONSOLE_LEVEL );
    this.addHandler( ch );
    
    // Setup file handlers
    SimpleFormatter formatter = new SimpleFormatter();
    
    FileHandler infoLog = new FileHandler( infoLogFile.getPath() );
    infoLog.setLevel( FILE_INFO_LEVEL );
    infoLog.setFormatter( formatter );
    this.addHandler( infoLog );

    
  }
}
