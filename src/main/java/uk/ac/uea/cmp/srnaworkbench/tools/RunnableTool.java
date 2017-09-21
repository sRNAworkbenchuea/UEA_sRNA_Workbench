/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.ThreadCompleteListener;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Provides some runtime functionality around a tool that can be run in a separate
 * thread.
 * @author ezb11yfu
 */
public abstract class RunnableTool implements Runnable 
{
  private static final String DEFAULT_TOOL_NAME = "RUNNABLE_TOOL";
  
  // Tool's name (to set in the concrete sub-class (otherwise defaults to DEFAULT_TOOL_NAME)
  private final String tool_name;
  
  // Error handling message
  private String error_message;
  
  // Runtime controls
  private ThreadCompleteListener listener;
  private StatusTracker tracker;
  private volatile boolean continue_run;

  /**
   * Create a new RunnableTool identified with the specified tool name.
   * @param toolName The name which is used to describe the tool in any log output
   * that is produced.
   */
  protected RunnableTool( final String toolName )
  {
    this( toolName, null, null );
  }

  /**
   * Create a new RunnableTool identified with the specified tool name and a run 
   * status tracking object.
   * @param toolName The name which is used to describe the tool in any log output
   * that is produced.
   * @param tracker The object which informs the user of progress made during this
   * tool's run.
   */
  protected RunnableTool( final String toolName, final StatusTracker tracker )
  {
    this( toolName, tracker, null );
  }

  /**
   * Create a new RunnableTool identified with the specified tool name, a run 
   * status tracking object and a thread completed listener
   * @param toolName The name which is used to describe the tool in any log output
   * that is produced.
   * @param tracker The object which informs the user of progress made during this
   * tool's run.
   * @param listener The listening object which is to be informed when this tool
   * has completed it's run method.
   */
  protected RunnableTool( final String toolName, final StatusTracker tracker, final ThreadCompleteListener listener )
  {
    this.tool_name = ( toolName == null || toolName.isEmpty() ) ? DEFAULT_TOOL_NAME : toolName;
    
    this.error_message = null;
    this.continue_run = true;

    setTracker( tracker );
    setListener( listener );
  }
  
  /**
   * Retrieves the tool name specified by the client class.
   * @return The Tool Name
   */
  public String getToolName()
  {
    return this.tool_name;
  }

  
  
  // ********* Error handling routines **********
  
  /**
   * Will say whether the tool's run method has completed successfully.
   * @return true if the run method failed, otherwise false if the tool completed
   * successfully or has not yet been run.
   */
  public boolean failed()
  {
    return this.error_message != null ? true : false;
  }

  /**
   * Retrieves the error message produced from the tool's run method if present.
   * @return The error message produced after the tool's run method failed, or null
   * if the tool has not yet been run or if the tool completed successfully.
   */
  public String getErrorMessage()
  {
    return error_message;
  }

  /**
   * Sets the error message (to be used only after a tool has failed to complete
   * its run method)
   * @param message The message describing the error condition. 
   */
  protected void setErrorMessage( final String message )
  {
    this.error_message = message;
  }

  /**
   * Clears the error message.
   */
  protected void clearErrorMessage()
  {
    this.error_message = null;
  }

  // ********* Setup routines **********
  
  public void determineMemoryAmount()
  {
    Tools.printCurrentMemoryValues();
  }
  
  // ********* Runtime handling routines **********
  
  /**
   * Calling this method will request that the tool's run method should stop at
   * the first convenient opportunity.
   */
  public void cancelRun()
  {
    this.continue_run = false;
  }

  /**
   * Reset's the cancel run request.
   */
  public void resetRun()
  {
    continue_run = true;
  }

  /**
   * Checks if the continue run flag is set.  If so an IOException is thrown.  This
   * method should be called at points in the sub-classes process method when it
   * would be convenient to cancel the run.
   * @throws IOException Thrown if the continue run flag has been set.
   */
  protected void continueRun() throws IOException
  {
    if ( !continue_run )
    {
      throw new IOException( "Run cancelled by user." );
    }
  }

  /**
   * Returns true if the user has requested that the tool's run method should be
   * cancelled.
   * @return true if the continue run flag is set, otherwise false.
   */
  protected boolean cancelRequested()
  {
    return !this.continue_run;
  }

  // ********* Listener handling routines ***********
  
  /**
   * Registers a ThreadCompleteListener object with this RunnableTool after initialisation 
   * time.  The listener will be notified when the RunnableTool's run method has
   * completed (either successfully or unsuccessfully).
   * @param listener The listener object which should be in notified upon run method
   * completion.
   */
  public final void setListener( final ThreadCompleteListener listener )
  {
    this.listener = listener;
  }

  /**
   * Notifies the ThreadCompleteListener object (if registered) that the run method
   * has completed.  This should only be called in one place in this class.
   */
  private void notifyListener()
  {
    if ( this.listener != null )
    {
      this.listener.notifyOfThreadCompletion();
    }
  }

  
  // ********* Tracker handling routines ***********
  
  /**
   * Registers a status tracking object after initialisation.  The tracker object
   * should be notified of progress during the RunnableTool's run method.
   * @param tracker The status tracking object to register.
   */
  public final void setTracker( final StatusTracker tracker )
  {
    this.tracker = tracker;
  }

  /**
   * Retrieves the status tracking object if registered.
   * @return The status tracking object or null if no status tracker is registered.
   */
  protected final StatusTracker getTracker()
  {
    return this.tracker;
  }

  /**
   * Should be used to indicate that the RunnableTool's run method has completed,
   * either successfully or unsuccessfully.  Only to be called from this class
   * to simplify interface for client classes.
   * @param success Set to true if the run method completed successfully, or false
   * if the run method failed.
   */
  private void trackerFinished( final boolean success )
  {
    if ( this.tracker != null )
    {
      this.tracker.setFinished( success );
    }
  }

  /**
   * Used to notify the status tracker that the RunnableTool has initiated a subprocess
   * which has a known runtime.
   * @param message The status message to output
   * @param length The length of the subprocess in arbitrary units.
   */
  protected void trackerInitKnownRuntime( final String message, final int length )
  {
    if ( this.tracker != null )
    {
      this.tracker.initKnownRuntime( message, length );
    }
  }

  /**
   * Used to notify the status tracker that the RunnableTool has initiated a subprocess
   * which has an unknown runtime.
   * @param message The status message to output
   */
  protected void trackerInitUnknownRuntime( final String message )
  {
    if ( this.tracker != null )
    {
      this.tracker.initUnknownRuntime( message );
    }
  }

  /**
   * Returns the status tracker to its idle state.
   */
  protected void trackerReset()
  {
    if ( this.tracker != null )
    {
      this.tracker.reset();
    }
  }

  /**
   * Increments the status tracker's progress bar during a known runtime subprocess.
   */
  protected void trackerIncrement()
  {
    if ( this.tracker != null )
    {
      this.tracker.increment();
    }
  }
  /**
   * Increments the status tracker's progress bar to declared value during a known runtime subprocess.
   */
  protected void trackerIncrement(int value)
  {
    if ( this.tracker != null )
    {
      this.tracker.increment(value);
    }
  }
  
  // ********* Thread handling routines ***********
  
  /**
   * Reset any variables in RunnableTool to initial state
   */
  protected void reset()
  {
    // Clear any error flags from previous runs.
    clearErrorMessage();

    // Ensure that the tool is free to
    resetRun();

    // Reset the tracker to default state before starting.
    trackerReset();
  }
  
  /**
   * Starts the tool.  Catches all Exceptions and handles logging, posting error 
   * messages.  Also starts and stops the tracker.
   */
  @Override
  public void run()
  {
    try
    {
      // Create and start a stop watch to time this tool's run method.
      StopWatch sw = new StopWatch();
      sw.start();
            
      // Log that the tool is starting.
      LOGGER.log( Level.INFO, "{0}: Starting.", tool_name );
      
      // Let's make sure we've got as much memory as possible before starting.
      System.gc();
      
      // Reset any variable in RunnableTool
      reset();      
      
      // Call the client's process method.
      process();
      
      // If we got this far then we can say the tool finished the run successfully.
      trackerFinished( true );
      
      // Stop the stop watch.
      sw.stop();
      //System.out.println( "time: " + sw.toString() );

      // Log that we completed successfully.
      LOGGER.log( Level.INFO, "{0}: Completed in: {1}.", new Object[] { tool_name, sw.toString() } );
    }
    catch( Throwable e )
    {
        e.printStackTrace();
        
        
      String stackTrace = Tools.getStackTrace( e );      
      String message = e.getMessage();
      
      // Log the exception with the stack trace for debugging.
      LOGGER.log( Level.SEVERE, "{0}: Message: {1};\nStack Trace: {2} ",
        new Object[]{ tool_name, message, stackTrace } );
      
      if ( e instanceof OutOfMemoryError )
      {
        // Clean up and give the GC a fair chance of doing a decent job before
        // continuing.  If we try to do more stuff and there's no memory then the
        // JVM can fail in a bad way.
        System.gc();
        
        try
        {
          Thread.sleep( 2000 );
        }
        catch ( Exception ex )
        {
          // Already handling an error condition, not sure why this would error but 
          // if it does just make a mention in the logger and carry on.
          LOGGER.log( Level.SEVERE, "{0}: Message: {1}",
            new Object[]{ tool_name, ex.getMessage(), stackTrace } );
        }
        
        // Reset error message to something more descriptive in this case
        message = "Out of Memory Error occured.\nWe advise "
          + "increasing the amount of memory available to the JVM using the -Xmx "
          + "argument or running smaller datasets through this machine.\nIf these "
          + "options are not possible then you should use a machine with more "
          + "memory available to run this job.\nWe also advise that you restart the "
          + "UEA sRNA Workbench before running any subsequent jobs";
        
        // Log the Out of Memory message.
        LOGGER.log( Level.SEVERE, "{0}: {1}",
          new Object[]{ tool_name, message } );
      }        
      
      // Set the stored error message to what the exception said
      setErrorMessage( message );
      
      // Tell the tracker that we're finished but 
      trackerFinished( false );
    }
    finally
    {
      // Signal to any listener that the tool has finished its run.
      notifyListener();
      
      // Let's be a good citizen and tidy up after ourseleves
      System.gc();
    }
  }
  
  /**
   * This is called by the run method.  Start / Stop Logging is automatically implemented
   * for all RunnableTools.  Client should put all of the code they wish to be executed
   * in worked thread here.
   */
  protected abstract void process() throws Exception;    
}
