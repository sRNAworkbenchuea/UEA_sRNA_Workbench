/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.PrintStream;
import java.text.MessageFormat;
import static uk.ac.uea.cmp.srnaworkbench.utils.StopWatch.DEFAULT_SIZE;

/**
 *
 * @author Matthew
 */
public abstract class Timer {

    protected String _title = "";
    protected long _startTime = 0;
    protected long _stopTime = -1;
    protected boolean _stopped = false;

    // Format a time like '1h1m1s1.111ms (123ns)'
    protected static final MessageFormat TIME_FORMAT = new MessageFormat("{0,number}h{1,number}m{2,number}s{3,number,000.000}ms");
    protected static final MessageFormat NANO_FORMAT = new MessageFormat("({0,number,#,###}ns)");

    protected static final long NS_PER_MILLI = 1000000L;
    protected static final long NS_PER_SEC = NS_PER_MILLI * 1000;

    /**
     * *
     * Default constructor. Calls {@code StopWatch( "", DEFAULT_SIZE )}.
     */
    public Timer() {
        this("", DEFAULT_SIZE);
    }

    /**
     * *
     * Constructor. Calls {@code StopWatch( "", numLaps)}. Starts the stopwatch.
     *
     * @param numLaps Used to set the lap-time collection's capacity.
     */
    public Timer(int numLaps) {
        this("", numLaps);
    }

    /**
     * *
     * Constructor. Calls {@code StopWatch( title, DEFAULT_SIZE )}.
     *
     * @param title Stopwatch title
     */
    public Timer(String title) {
        this(title, DEFAULT_SIZE);
    }

    /**
     * *
     * Constructor. Sets the title and initialises the size of the lap-time
     * collection (in an attempt to avoid the inclusion of collection resizing
     * in the timing). Starts the stop-watch.
     *
     * @param title Stopwatch title
     * @param numLaps Used to set the lap-time collection's capacity.
     */
    public Timer(String title, int numLaps) {
        _title = title == null ? "" : title;

        // Just in case 'start()' is not called by the user
//        start();
    }

    /**
     * *
     * Start the stopwatch
     */
    public abstract void start();

    public abstract void stop();

    /**
     * *
     * Add a lap time
     */

    public abstract void lap(String msg);

    public abstract void printTimes(PrintStream os, boolean outputNanos);

    /**
     * *
     * Get elapsed time
     *
     * @return The elapsed time as a formatted string
     */
    public String getFormattedElapsedTime() {
        return formatElapsedTime(System.nanoTime() - _startTime, false);
    }

    /**
     * *
     * Output the times to {@code System.out}
     */
    public void printTimes() {
        printTimes(System.out, false);
    }

    /**
     * *
     * Output the times to {@code System.out}
     *
     * @param outputNanos Whether to output nanos as well as a formatted time
     */
    public void printTimes(boolean outputNanos) {
        printTimes(System.out, outputNanos);
    }
    
    public void printHeader(PrintStream os, boolean outputNanos)
    {
         if ( _title == null || _title.length() == 0 )
            os.println( "-- Stopwatch time(s) --" );
          else
            os.println( "-- Stopwatch time(s) for '" + _title + "' --" );

          if ( _stopped )
          {
            os.println( "Total elapsed time: " + formatElapsedTime( _stopTime - _startTime, outputNanos ) );
          }
          else
          {
            os.println( "StopWatch still running (call stop() to stop)." );
          }

    }

    public static String formatElapsedTime(long timeInNanos, boolean outputNanos) {
        long s = timeInNanos / NS_PER_SEC;
        long ns = timeInNanos - s * NS_PER_SEC;
        double millis = (double) ns / NS_PER_MILLI;

        long h = s / 3600;
        s -= h * 3600;

        long m = s / 60;
        s -= m * 60;

        String str = TIME_FORMAT.format(new Object[]{h, m, s, millis});

        if (outputNanos) {
            str += " " + NANO_FORMAT.format(new Object[]{timeInNanos});
        }

        return str;
    }

    public static long getCurrentTimeInSeconds() {
        long nanoTime = System.nanoTime();
        long currentTime = (long) (nanoTime / 1000000000.0);
        return currentTime;
    }

}
