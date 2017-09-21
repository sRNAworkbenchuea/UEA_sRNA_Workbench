/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.*;
import java.text.*;
import java.util.*;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * Simple timer which uses {@code System.nanoTime()} and also provides lap times.
 *
 * @author prb07qmu
 */
public final class StopWatch extends Timer
{
  /*
   * The default number of laps.
   */
  public static final int DEFAULT_SIZE = 10;

  private final ArrayList<Long>   _lapTimes    = new ArrayList<  >();
  private final ArrayList<String> _lapMessages = new ArrayList<  >();

  /**
   *  Factory for quickly creating and starting a new watch
     * @return A new stopwatch that has been started with start()
   */
  public static StopWatch startNew()
  {
      StopWatch sw = new StopWatch();
      sw.start();
      return sw;
  }
  
  /***
   * Default constructor.
   * Calls {@code StopWatch( "", DEFAULT_SIZE )}.
   */
  public StopWatch()
  {
    this( "", DEFAULT_SIZE );
  }

  /***
   * Constructor.
   * Calls {@code StopWatch( "", numLaps)}.
   * Starts the stopwatch.
   * @param numLaps Used to set the lap-time collection's capacity.
   */
  public StopWatch( int numLaps )
  {
    this( "", numLaps );
  }

  /***
   * Constructor. Calls {@code StopWatch( title, DEFAULT_SIZE )}.
   * @param title Stopwatch title
   */
  public StopWatch( String title )
  {
    this( title, DEFAULT_SIZE );
  }

  /***
   * Constructor. Sets the title and initialises the size of the lap-time collection (in an attempt
   * to avoid the inclusion of collection resizing in the timing).
   * Starts the stop-watch.
   *
   * @param title Stopwatch title
   * @param numLaps Used to set the lap-time collection's capacity.
   */
  public StopWatch( String title, int numLaps )
  {
    _title = title == null ? "" : title;

    _lapMessages.ensureCapacity( numLaps );
    _lapTimes.ensureCapacity( numLaps );

    // Just in case 'start()' is not called by the user
    start();
  }

  /***
   * Start the stopwatch
   */
  @Override
  public void start()
  {
    _stopped = false;

    _lapMessages.clear();
    _lapTimes.clear();

    _startTime = System.nanoTime();
  }

  /***
   * Add a lap time
   */
  public void lap()
  {
    _lapTimes.add( System.nanoTime() );
    _lapMessages.add( "" );
  }

  @Override
  public void lap( String message )
  {
    _lapTimes.add( System.nanoTime() );
    _lapMessages.add( message );
  }
  
  public String printLap(String message)
  {
      _lapTimes.add(System.nanoTime());
      _lapMessages.add(message);
      long lapTime = _lapTimes.get(_lapTimes.size() - 1);
      if(_lapTimes.size() > 1)
      {
          lapTime = lapTime - _lapTimes.get(_lapTimes.size() - 2);
      }
      else
      {
          lapTime = lapTime - this._startTime;
      }
      if(!DatabaseWorkflowModule.getInstance().isDebugMode())
        WorkflowSceneController.addToConsole(message + " " + StopWatch.formatElapsedTime(lapTime, false));
      return message + " " + StopWatch.formatElapsedTime(lapTime, false);
  }

  /***
   * Stop the stopwatch
   */
  @Override
  public void stop()
  {
    if ( _stopped )
      return;

    _stopTime = System.nanoTime();
    _stopped = true;
  }


  /***
   * Output the times to the specified {@code PrintStream}
   * @param os
   */
  public void printTimes( PrintStream os, boolean outputNanos )
  {
    int n = _lapTimes.size();

    printHeader(os, outputNanos);
    
    if ( n > 0 )
    {
      int lap, padSize = 0;
      long prevTime = _startTime, lapTime;

      for ( lap = 0; lap < n; lap++ )
      {
        lapTime = _lapTimes.get( lap ).longValue();

        String msg = "Lap " + (lap + 1) + ": " + formatElapsedTime( lapTime - prevTime, outputNanos );

        if ( padSize == 0 )
        {
          padSize = msg.length() + 4;
        }

        String lapMesage = _lapMessages.get( lap );
        if ( lapMesage != null && ! lapMesage.isEmpty() )
        {
          msg = String.format( "%1$-" + padSize + "s", msg ) + lapMesage;
        }

        os.println( msg );
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
            WorkflowSceneController.addToConsole(msg);

        prevTime = lapTime;
      }

      if ( _stopped )
      {
        os.println( "Last lap time to stop : " + formatElapsedTime( _stopTime - prevTime, outputNanos ) );
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
            WorkflowSceneController.addToConsole( "Last lap time to stop : " + formatElapsedTime( _stopTime - prevTime, outputNanos ) );
      }

      if ( n > 1 )
      {
        printMaxMinTimes( os, outputNanos );
      }
    }

    os.println( "-- End of stopwatch time(s) --" );
  }

  private void printMaxMinTimes( PrintStream os, boolean outputNanos )
  {
    // Output min/max/avg aswell for the lap-times

    int n = _lapTimes.size();

    long max = Long.MIN_VALUE;
    long min = Long.MAX_VALUE;

    long lapTime;
    long prevTime = _startTime;

    for ( int lap = 0; lap < n; lap++ )
    {
      lapTime = _lapTimes.get( lap ).longValue();

      long t = lapTime - prevTime;

      if ( t > max ) max = t;
      if ( t < min ) min = t;

      prevTime = lapTime;
    }

    long avg = ( _lapTimes.get( n-1 ) - _startTime ) / n;

    String msg = "Lap times : max/min/avg = " + formatElapsedTime( max, outputNanos ) +
      " / " + formatElapsedTime( min, outputNanos ) +
      " / " + formatElapsedTime( avg, outputNanos ) ;
    os.println(msg);
    if(!DatabaseWorkflowModule.getInstance().isDebugMode())
        WorkflowSceneController.addToConsole(msg);

  }

  public static void main( String[] args )
  {
    StopWatch sw = new StopWatch();

    try
    {
      sw.start();

      Thread.sleep( 43 );

      sw.lap();

      System.out.println( sw.getFormattedElapsedTime() );

      Thread.sleep( 243 );

      sw.lap();

      Thread.sleep( 4000 );

      sw.stop();
    }
    catch ( InterruptedException ex )
    {
    }

    sw.printTimes();
  }
}
