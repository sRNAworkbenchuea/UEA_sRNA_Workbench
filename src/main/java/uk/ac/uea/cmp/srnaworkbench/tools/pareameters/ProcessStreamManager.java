/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
//import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;


/**
 * Manages streams to and from an external process
 * @author Dan Mapleson & Matthew Stocks
 */
public class ProcessStreamManager
{
  private Process process;
  private String output_prefix;
  private int return_code;
  private List<String> output_data;

    public static final String LINE_SEPARATOR_UNIX = "\n";
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    public static String LINE_SEPARATOR;
  /**
   * Creates a new instance of a process stream manager for a given process
   * @param process The process to manage streams for
   * @param output_prefix An prefix to display before any output messages
   * @param runFromServer If this PSM is created from the server side then set this 
   * param to true, otherwise false.
   */
  public ProcessStreamManager( Process process, String output_prefix, boolean runFromServer )
  {
    this.process = process;
    this.output_prefix = output_prefix;
    this.return_code = 0;
    this.output_data = null;
    
    
  }
  
  

  /**
   * Pump data into a running process via its input stream.
   * @param data_in The lines of data to pump in
   * @param terminator A terminator message which should indicate to the process 
   * that all input has been provided and it can now finish.
   */
  public void enterData( List<String> data_in, String terminator )
  {
    // Should always finish before output and error streams if all went well.
    // Not sure what happens if the process terminates before all data has been
    // consumed.  Possibly we need to put some error handling in here to ensure
    // this thread is properly terminated in all cases, otherwise we might leave
    // dangling threads.
    new ProcessInputHandler( process.getOutputStream(), data_in, terminator, output_prefix ).start();
  }

  /**
   * Instructs process to run in the foreground.  i.e. forces current thread to wait
   * until the process has completed.
   * @param record_output Whether to record all output in this instance for later 
   * retrieval by the client
   * @return return code from the process
   * @throws Exception Thrown if there were any unexpected problems
   */
  public int runInForeground( boolean record_output ) throws Exception
  {
    ProcessOutputHandler err = new ProcessOutputHandler( process.getErrorStream(), output_prefix, true, false );
    ProcessOutputHandler out = new ProcessOutputHandler( process.getInputStream(), output_prefix, false, record_output );

    err.start();
    out.start();

    // Wait until the end of the process
    this.return_code = process.waitFor();

    // Make sure the output streams have finished processing too.
    err.join();
    out.join();

    this.output_data = out.getOutput();

    return this.return_code;
  }
   /**
   * Instructs process to run in the foreground.  i.e. forces current thread to wait
   * until the process has completed.
   * @param output_path write the standard out to file
   * @return return code from the process
   * @throws Exception Thrown if there were any unexpected problems
   */
  public int runInForeground( Path output_path ) throws Exception
  {
    ProcessOutputHandler err = new ProcessOutputHandler( process.getErrorStream(), output_prefix, true, false );
    ProcessOutputHandler out = new ProcessOutputHandler( process.getInputStream(), output_prefix, false, true, output_path );

    err.start();
    out.start();

    // Wait until the end of the process
    this.return_code = process.waitFor();

    // Make sure the output streams have finished processing too.
    err.join();
    out.join();

    this.output_data = out.getOutput();

    return this.return_code;
  }

  /**
   * Instructs the process to run in the background.  i.e. the current thread carries 
   * on with any other work it has to do.  The managed process will finish of its
   * on accord.
   * @throws Exception Thrown if there were any unexpected problems
   */
  public void runInBackground( ) throws Exception
  {
    new ProcessOutputHandler( process.getErrorStream(), output_prefix, true, false ).start();
    new ProcessOutputHandler( process.getInputStream(), output_prefix, false, false ).start();
  }

  /**
   * Retrieves the return code of the managed process (will return 0 if called before
   * the process has completed!)
   * @return The process return code.
   */
  public int getReturnCode()
  {
    return this.return_code;
  }

  /**
   * Retrieves any output data that has been captured from the managed process.
   * @return Output data.
   */
  public List<String> getStandardOutput()
  {
    return this.output_data;
  }

  private class ProcessInputHandler extends Thread
  {
    private BufferedWriter dataOut;
    private List<String> data;
    private String prefix;
    private String terminator;
    private boolean success;

    public ProcessInputHandler( OutputStream data_out, List<String> data, String terminator, String prefix )
    {
      this.dataOut = new BufferedWriter( new OutputStreamWriter( data_out ) );
      this.data = data;
      this.prefix = prefix + " input: ";
      this.terminator = terminator;
      this.success = true;
    }

    @Override
    public void run()
    {
      try
      {
        for ( String line : this.data )
        {
          this.dataOut.write( line );
          this.dataOut.newLine();

          String message = this.prefix + line;
          //LOGGER.log( Level.FINE, message );
        }

        this.dataOut.write( terminator );
        this.dataOut.newLine();

        String message = this.prefix + "terminator";
        //LOGGER.log( Level.FINE, message );
        
        this.dataOut.flush();
        this.dataOut.close();
      }
      catch ( IOException e )
      {
        String message = "Error in IO with ProcessInputHandler: " + e.getMessage();
        //LOGGER.log( Level.WARNING, message );
        //LOGGER.log( Level.WARNING, Utils.getStackTrace(e) );
        
        success = false;
      }
    }

    public boolean success()
    {
      return this.success;
    }
  }

  private class ProcessOutputHandler extends Thread
  {
    private BufferedReader dataIn;
    private String prefix;
    private List<String> data;
    private boolean errorStream;
    private boolean recordOutput;
    private boolean success;
    private Path output_path = null;
    
    public ProcessOutputHandler( InputStream dataIn, String outputPrefix, boolean isErrStream, boolean recordOutput, Path output_path )
    {
      this(dataIn, outputPrefix, isErrStream, recordOutput);
      this.output_path = output_path;
      
    }

    public ProcessOutputHandler( InputStream dataIn, String outputPrefix, boolean isErrStream, boolean recordOutput )
    {
      this.dataIn = new BufferedReader( new InputStreamReader( dataIn ) );
      this.prefix = outputPrefix + " output: ";
      this.data = new ArrayList<String>();
      this.errorStream = isErrStream;
      this.recordOutput = recordOutput;
      this.success = true;
    }

    @Override
    public void run()
    {
      String line;
      try
      {
        while ( ( line = dataIn.readLine() ) != null )
        {
          String message = this.prefix + line;
          Level l = errorStream ? Level.WARNING : Level.FINE;          
          //LOGGER.log( l, message );
          
          if ( recordOutput )
          {
              if(output_path != null)
              {   
                  Files.write( output_path, line.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                  Files.write( output_path,LINE_SEPARATOR.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

                  
              }
              else
              {
                data.add( line );
              }
          }
        }

        dataIn.close();
      }
      catch ( IOException e )
      {
        String message = "Error in IO with ProcessOutputHandler: " + e.getMessage();
        //LOGGER.log( Level.WARNING, message );        
        //LOGGER.log( Level.WARNING, Utils.getStackTrace(e) );
        
        this.success = false;
      }
    }

    public List<String> getOutput()
    {
      return recordOutput ? data : null;
    }

    public boolean success()
    {
      return success;
    }
  }
}
