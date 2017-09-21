/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

import java.io.IOException;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.utils.ThreadCompleteListener;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

/**
 * Handles execution of a RunnableTool from a GUI in another thread.  Also notifies
 * the GUI of progress and result made during the RunnableTool's run method.
 * @author ezb11yfu
 */
public abstract class ToolRunner implements ThreadCompleteListener
{
  protected RunnableTool engine;
  protected Thread thread;
  protected ToolHost host;
  
  /**
   * Initialises the ToolRunner
   * @param host The host that's running this tool
   */
  protected ToolRunner(ToolHost host)
  {
    this.host = host;
    this.thread = null;
    this.engine = null;
  }
  
  /**
   * Check to see if tool is currently running
   * @return true if tool is running, otherwise false
   */
  public boolean getActive()
  {
    return this.thread.isAlive();
  }

  /**
   * Clears the existing filter engine from memory
   */
  public void reset()
  {
    this.engine = null;
    this.thread = null;

    //TODO This call probably isn't required as we call GC in the RunnableTool now...
    System.gc();
  }

  /**
   * Instructs the tool to stop executing
   */
  public void cancel()
  {
    if ( this.thread != null )
    {
      this.engine.cancelRun();
    }
  }
  
  /**
   * Called when engine completes run.  If engine fails to complete successfully
   * a request is put out to the host to raise the issue with the user.  If it completes
   * successfully the host is asked to update the GUI with the results.  In both cases
   * the running status of the GUI is set to false.
   */
  @Override
  public void notifyOfThreadCompletion()
  {
    if ( this.engine.failed() )
    {
      this.host.showErrorDialog( this.engine.getErrorMessage() );
    }
    else
    {
      this.host.update();
    }

    this.host.setRunningStatus( false );
  }
    
  /**
   * Handles running of a tool for derived class.  Asks host to set the GUI into running (busy) mode.
   * Then starts a new thread and kicks off the engine in that thread.  If an error
   * occurs then the host is signalled to raise an error message with the user and
   * sets the running mode to false.
   * @param engine The RunnableTool to start
   * @throws IOException Thrown if there were any problems starting the RunnableTool
   */
  protected void run(RunnableTool engine) throws IOException
  {
    try
    {
      this.host.setRunningStatus( true );

      this.engine = engine;
      this.engine.setListener( this );

      this.thread = new Thread( this.engine );
      this.thread.start();
    }
    catch ( Exception ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );

      this.host.showErrorDialog( ex.getMessage() );
      this.host.setRunningStatus( false );
    }
  }
}
