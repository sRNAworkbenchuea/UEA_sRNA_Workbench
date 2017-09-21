/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;

/**
 *
 * @author ezb11yfu
 */
public class PatClust
{
  private File in_file;
  private File out_file;
  private File temp_dir;
  private int mismatches;
  private int gaps;
  private boolean overhangs;

  public PatClust()
  {
    this( null, null, null, 1, 0, false );
  }

  public PatClust( File in_file, File out_file, File temp_dir, int mismatches, int gaps, boolean overhangs )
  {
    this.in_file = in_file;
    this.out_file = out_file;
    this.temp_dir = temp_dir;
    this.mismatches = mismatches;
    this.gaps = gaps;
    this.overhangs = overhangs;
  }

  public void run() throws IOException
  {
    // Add overhangs to in_file if necessary
    File datafile = in_file;

    if ( overhangs )
    {
      HashMap<String, Integer> data = new SRNAFastaReader( in_file ).process();

      HashMap<String, Integer> new_data = new HashMap<String, Integer>();

      for ( String seq : data.keySet() )
      {
        new_data.put( "XX" + seq + "XX", data.get( seq ) );
      }

      datafile = new File( temp_dir.getPath() + DIR_SEPARATOR + datafile.getName() + ".overhang" );

      new SRNAFastaWriter( datafile ).process( new_data, true );
    }


    File pat_results = new File( temp_dir.getPath() + DIR_SEPARATOR + "patman_results.patman" );
    
    PatmanParams pp = new PatmanParams.Builder()
      .setMaxMismatches( mismatches )
      .setMaxGaps( gaps )
      .setPositiveStrandOnly( true )
      .build();
    
    new PatmanRunner( in_file, datafile, pat_results, temp_dir, pp ).run();

    // Get the match results
    Patman p = new PatmanReader( pat_results ).process();

    // Cluster the results 
    ArrayList<HashMap<String, Integer>> clusters = cluster( p );

    // Sort the clusters by size in descending order
    java.util.Collections.sort( clusters, new Comparator<HashMap<String, Integer>>()
    {
      public int compare( HashMap<String, Integer> entry1, HashMap<String, Integer> entry2 )
      {
        return entry1.size() == entry2.size() ? 0
          : entry1.size() < entry2.size() ? 1 : -1;
      }
    } );

    // Output the sorted clusters to file.
    writeClusters( clusters, this.out_file );
  }

  private ArrayList<HashMap<String, Integer>> cluster( Patman p )
  {
    ArrayList<HashMap<String, Integer>> results = new ArrayList<HashMap<String, Integer>>();

    for ( PatmanEntry pe : p )
    {
      // Strip abundance
      String m1 = pe.getLongReadHeader().substring( 0, pe.getLongReadHeader().indexOf( '(' ) );
      String m2 = pe.getSequence();

      // Strip overhangs
      m1 = m1.startsWith( "XX" ) ? m1.substring( 2, m1.length() - 2 ) : m1;
      m2 = m2.startsWith( "XX" ) ? m2.substring( 2, m2.length() - 2 ) : m2;

      // If same sequence then continue to next match
      if ( m1.equals( m2 ) )
      {
        continue;
      }

      boolean found = false;
      int last_found = -1;
      for ( int i = 0; i < results.size(); i++ )
      {
        HashMap<String, Integer> clust = results.get( i );

        // If we've already seen both sequences before continue to next match
        if ( clust.containsKey( m1 ) && clust.containsKey( m2 ) )
        {
          if ( last_found >= 0 )
          {
            for ( String seq : clust.keySet() )
            {
              results.get( last_found ).put( seq, 1 );
            }
            results.remove( i );
          }

          found = true;
          last_found = i;
          continue;
        }

        if ( clust.containsKey( m1 ) )
        {
          if ( last_found >= 0 )
          {
            for ( String seq : clust.keySet() )
            {
              results.get( last_found ).put( seq, 1 );
            }
            results.remove( i );
          }

          clust.put( m2, 1 );
          found = true;
          last_found = i;
          continue;
        }

        if ( clust.containsKey( m2 ) )
        {
          if ( last_found >= 0 )
          {
            for ( String seq : clust.keySet() )
            {
              results.get( last_found ).put( seq, 1 );
            }
            results.remove( i );
          }

          clust.put( m1, 1 );
          found = true;
          last_found = i;
          continue;
        }
      }

      // This match is not in any existing cluster so create a new one with the two entries
      if ( !found )
      {
        HashMap<String, Integer> valmap = new HashMap<String, Integer>();
        valmap.put( m1, 1 );
        valmap.put( m2, 1 );
        results.add( valmap );
      }
    }


    return results;
  }

  private void writeClusters( ArrayList<HashMap<String, Integer>> clustered_data, File out_file ) throws IOException
  {
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( out_file ) ) );

    for ( HashMap<String, Integer> cluster : clustered_data )
    {
      for ( String seq : new TreeSet<String>( cluster.keySet() ) )
      {
        out.print( seq + "; " );
      }
      out.print( "\n" );
    }

    out.flush();
    out.close();
  }

  public static void main( String[] args )
  {
    try
    {
      new PatClust(
        new File( "C:\\LocalData\\Research\\RNATools\\patclust\\soy+tomato+extra_tasi_candidates.fa" ),
        new File( "C:\\LocalData\\Research\\RNATools\\patclust\\soy+tomato+extra_tasi_candidates_overhangs.out" ),
        new File( "C:\\LocalData\\Research\\RNATools\\patclust" ),
        5, 0, true ).run();
    }
    catch ( Exception e )
    {
      System.err.println( e.getMessage() );
      e.printStackTrace();
    }
  }
}
