
package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;


import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Used to match sRNAs against a Patman object containing sRNA hits to some target
 * data.  User can decide whether they want to keep the sRNAs that match or discard
 * them.  The Matcher uses multi-threading to increase the speed at which sRNAs
 * can be matched and filtered.
 * @author Dan Mapleson and Matthew Stocks
 */
public class Matcher
{
  // Input
  private FastaMap srnas;
  private Patman p;
  private boolean keep_matched;
  // Logging discarded output
  private PrintWriter pw;
  private String filter;
  //Output
  private FastaMap results;
  private StatusTracker tracker;
  private String message;
  private boolean verbose;

  /**
   * Creates a new Matcher.  This constructor is adequate for internal use where
   * progress tracking is not required.
   * @param srnas Unfiltered sRNA reads
   * @param p Patman object containing sRNA hits to target data
   * @param keep_matched Whether to keep the sRNAs that hit to the target and discard
   * the rest, or alternatively throw away the hits and keep the misses
   * @param verbose Logging and debugging data
   */
  public Matcher( FastaMap srnas, Patman p, boolean keep_matched, boolean verbose )
  {
    this( srnas, p, keep_matched, verbose, null, null, null, null );
  }

  /**
   * Creates a new Matcher.  This constructor is adequate for use where
   * progress tracking is required.
   * @param srnas Unfiltered sRNA reads
   * @param p Patman object containing sRNA hits to target data
   * @param keep_matched Whether to keep the sRNAs that hit to the target and discard
   * the rest, or alternatively throw away the hits and keep the misses
   * @param verbose Logging and debugging data
   * @param pw If non-null this print writer writes out all discarded sequences
   * @param filter Message for output
   * @param tracker Used to track progress
   * @param message Output message
   */
  public Matcher( FastaMap srnas, Patman p, boolean keep_matched, boolean verbose, PrintWriter pw, String filter, StatusTracker tracker, String message )
  {
    this.srnas = srnas;
    this.p = p;
    this.keep_matched = keep_matched;
    this.verbose = verbose;
    this.pw = pw;
    this.filter = filter;
    this.results = new FastaMap();
    this.tracker = tracker;
    this.message = message;
  }

  /**
   * Executes the matching process and filtering process.  Returns a FastaMap
   * containing filtered reads.
   * @return Filtered reads (reads that either matched or didn't)
   * @throws Exception Thrown if there was any issues.
   */
  public FastaMap match() throws Exception
  {
    LOGGER.log( Level.FINE, "**** Matching sequences ****" );
    
    int usableCores = Runtime.getRuntime().availableProcessors() < 4 ? 1 : Runtime.getRuntime().availableProcessors() - 2;

    // Split duplexes into thread pots.
    if ( this.tracker != null )
    {
      this.tracker.initKnownRuntime( message + " (setting up threads)", usableCores );
    }

    List<FastaMap> queue = new ArrayList<FastaMap>();
    int i = 0;
    int j = 0;
    int limit = srnas.size() / usableCores;
    for ( Entry<String, FastaRecord> me : srnas.entrySet() )
    {
      if ( i == 0 )
      {
        queue.add( new FastaMap() );
      }

      queue.get( j ).put( me.getKey(), me.getValue() );

      if ( i == limit )
      {
        j++;
        i = 0;

        if ( this.tracker != null )
        {
          this.tracker.increment();
        }
      }
      else
      {
        i++;
      }
    }

    // Start worker threads
    if ( this.tracker != null )
    {
      this.tracker.reset();

      // The queue should have something in it, but make sure just in case...
      if ( queue.size() > 0 )
      {
        this.tracker.initKnownRuntime( message + " (processing data)", queue.get( 0 ).size() );
      }
    }

    int k = 0;
    ExecutorService es = Executors.newFixedThreadPool( usableCores );
    Collection<MatcherTask> tasks = new ArrayList<MatcherTask>();
    for ( FastaMap chunk : queue )
    {
      tasks.add( new MatcherTask( k++, chunk, this.p, this.keep_matched ) );
    }

    LOGGER.log( Level.FINE, "Matcher: Starting threads..." );
    List<Future<FastaMap>> answers = es.invokeAll( tasks );
    LOGGER.log( Level.FINE, "Matcher: All threads finished successfully." );

    LOGGER.log( Level.FINE, "Matcher: Concatenating results... " );

    // Stitch together results.
    if ( this.tracker != null )
    {
      this.tracker.reset();
      this.tracker.initKnownRuntime( message + " (concatenating results)", answers.size() );
    }

    for ( Future<FastaMap> f : answers )
    {
      FastaMap chunk = f.get();

      if ( chunk == null )
      {
        throw new Exception( "Matcher: Couldn't collect thread results." );
      }

      this.results.putAll( chunk );

      if ( this.tracker != null )
      {
        this.tracker.increment();
      }
    }

    LOGGER.log( Level.FINE, "Matcher: done.\n" );

    if ( this.tracker != null )
    {
      this.tracker.reset();
    }

    return this.results;
  }


  private class MatcherTask implements Runnable, Callable<FastaMap>
  {
    private FastaMap unfiltered;
    private FastaMap filtered;
    private Patman p;
    private boolean keep_matched;
    private int thread_id;
    private boolean success;

    public MatcherTask( int t_id, FastaMap unfiltered, Patman p, boolean keep_matched )
    {
      this.unfiltered = unfiltered;
      this.p = p;
      this.keep_matched = keep_matched;
      this.filtered = new FastaMap();

      this.thread_id = t_id;

      this.success = true;
    }

    @Override
    public void run()
    {
      try
      {
        FastaMap itemsToDiscard = new FastaMap();
        FastaMap itemsToKeep = new FastaMap();

        FastaMap hits = p.buildFastaMap();

        int j = 0;
        int length = unfiltered.size();
        for ( Entry<String, FastaRecord> me : unfiltered.entrySet() )
        {
          boolean keepSeq = !keep_matched;
          int i = 0;
          String unfiltered_seq = me.getKey();
          FastaRecord unfiltered_fr = me.getValue();

          if ( hits.containsKey( unfiltered_seq ) )
          {
            keepSeq = keep_matched ? true : false;
          }

          j++;

          if ( !keepSeq )
          {
            itemsToDiscard.put( unfiltered_seq, unfiltered_fr );
          }
          else
          {
            itemsToKeep.put( unfiltered_seq, unfiltered_fr );
          }

          if ( j % 1000 == 0 )
          {
            LOGGER.log( Level.FINE, "Matcher: Thread {0} processed sequence: {1}/{2}\n", new Object[]{ this.thread_id, j, length });
          }

          // Only update tracker on the first thread.
          if ( this.thread_id == 0 && tracker != null )
          {
            tracker.increment();
          }
        }

        for ( Entry<String, FastaRecord> me : itemsToDiscard.entrySet() )
        {
          // Is pw synchronised?  This could cause some problems if not!!!
          if ( pw != null )
          {
            pw.print( me.getKey() + "(" + me.getValue() + ") : " + filter + LINE_SEPARATOR );
            //pw.print(me.getKey() + LINE_SEPARATOR);
          }
        }

        filtered = itemsToKeep;
      }
      catch ( Exception e )
      {
        LOGGER.log( Level.SEVERE, "Exception: {0}\n StackTrace: {1}\n", 
          new Object[]{ e.getMessage(), Tools.getStackTrace(e) });
        this.success = false;
      }
    }

    @Override
    public FastaMap call()
    {
      run();
      return this.filtered;
    }

    public boolean success()
    {
      return this.success;
    }
  }
}
