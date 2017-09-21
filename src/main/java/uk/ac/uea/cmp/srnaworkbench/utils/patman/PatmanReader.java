/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;

/**
 * Loads a patman file from disk and generates a Patman instance.
 * @author Dan Mapleson
 */
public final class PatmanReader
{
  private final File _file;

  /**
   * Initialises the Reader with the provided file.  Does not start reading until 
   * client calls one of the process(...) methods.
   * @param file The file to load
   */
  public PatmanReader( File file )
  {
    _file = file;
  }

  /***
   * Process a PATMAN file - calls {@link process(int, int, int)}
   *
   * @return A {@link Patman} object containing the {@link PatmanEntry} objects.
   * @throws IOException
   */
  public Patman process() throws IOException
  {
    return process( Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE );
  }

  /***
   * Process a PATMAN file<br>
   * TODO: Improve performance for this step.  Can be slow when file is large.<br>
   * NOTE: Consider removing the parameters... I don't filtering at this step is required
   *
   * @param minSeqLength  IGNORED.
   *                      Minimum sequence length to consider (removes need to read
   *                      in short sequences, e.g. 3-mers, in an attempt to avoid memory problems.
   * @param minConsider   IGNORED.
   *                      Minimum sRNA abundance to be considered real.
   *                      Use 1 to consider all sRNAs.
   *
   * @return A {@link Patman} object containing the {@link PatmanEntry} objects.
   * @throws IOException
   */
  public Patman process( int minSeqLength, int maxSeqLength, int minConsider) throws IOException
  {
    String thisLine;
    //Pattern p = Pattern.compile("(\\S+)\\s*.*\\t(\\w+)\\((\\d+)\\)\\t(\\d+)\\t(\\d+)\\t(\\S+)\\t\\d+");
    //Matcher m = p.matcher("");

    Patman pat = new Patman();

    BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( _file ) ) );

    try
    {
      while ( ( thisLine = br.readLine() ) != null )
      {
        //m.reset(thisLine);
        //if (m.matches())//if the start of sequence matches the pattern given above -- (line validation?)
        if ( thisLine.indexOf( '\t' ) != -1 )
        {
          PatmanEntry pe = PatmanEntry.parse( thisLine );


          if(pe != null)
          {
                // Only include PatmanEntries that pass through the filter.
                int seq_len = pe.getSequence().length();
                if (seq_len >= minSeqLength && seq_len <= maxSeqLength)
                {
                    //    my $wabundance = $abundance/$genome_hits{$sequence};
                    //		unless ($wabundance >= $min_consider){
                    //		next;
                    //		}

                    //FastaMap tempMap = buildFastaMap();
                    if (pe.getAbundance() >= minConsider)
                    {
                        pat.add(pe);
                        //System.out.println(i);
                    }
                }
            }
        }
      }
    }
    finally
    {
      IOUtils.closeQuietly( br );
    }

    return pat;
  }

  public static void main( String[] args )
  {
    //doTimingTest();
    //doPatmanFileBreakdown();
    doPatmanFileRead();
  }

  private static void doTimingTest()
  {
    StopWatch sw = new StopWatch( "PatmanReader" );
    sw.start();

    Patman p = null;

    for ( int i = 0; i < 10; ++i )
    {
      p = doPatmanFileRead();
      sw.lap();
    }

    sw.stop();
    sw.printTimes();
  }

  /*
   * Read in a PATMAN file and perform a size-class reduction
   */
  private static void doPatmanFileBreakdown()
  {
    Patman pfile = doPatmanFileRead();

    // Display size -> count
    java.util.Map<Integer, Integer> m = pfile.getLengthToFrequencyBreakdown();

    for ( java.util.Map.Entry<Integer, Integer> entry : m.entrySet() )
    {
      System.out.println( "Length = " + entry.getKey() + ", Count = " + entry.getValue() );
    }

    // Want to filter on a set of lengths
    java.util.Set<Integer> aset = new java.util.HashSet<Integer>();
    aset.addAll( Arrays.asList( new Integer[]
      {
        21, 22, 23, 24
      } ) );

    // Perform the filtering
    Patman pset = pfile.performSizeClassReduction( null, aset );

    m = pset.getLengthToFrequencyBreakdown();

    // Display size -> count
    System.out.println( "After size-class reduction..." );
    for ( java.util.Map.Entry<Integer, Integer> entry : m.entrySet() )
    {
      System.out.println( "Length = " + entry.getKey() + ", Count = " + entry.getValue() );
    }

    int inc = 5000000;
    for ( int start = 0; start < 35000000; start += inc )
    {
      int stop = start + inc;
      Patman pwin = pset.performStartStopReduction( null, start, stop, Patman.StraddleInclusionCriterion.MAJORITY );

      m = pwin.getLengthToFrequencyBreakdown();
      System.out.println( "Size-class distribution for range : [" + start + ", " + stop + "]" );
      for ( java.util.Map.Entry<Integer, Integer> entry : m.entrySet() )
      {
        System.out.println( "Length = " + entry.getKey() + ", Count = " + entry.getValue() );
      }
    }
  }

  private static Patman doPatmanFileRead()
  {
    //String fileName = "D:\\LocalData\\hugh\\seq_data\\plant\\GSM118373.chr1.hits.patman";
    String fileName = "D:\\LocalData\\hugh\\seq_data\\plant\\GSM118373.chr1.patman";
    //String fileName = "D:\\LocalData\\hugh\\seq_data\\plant\\GSM118373.chr3.patman";
    File f = new File( fileName );

    Patman patman = null;

    try
    {
      PatmanReader pr = new PatmanReader( f );
      patman = pr.process();
    }
    catch ( IOException ex )
    {
      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, ex );
    }

    return patman;
  }
}
