/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Reads a FastQ file containing sRNA reads.
 * @author Dan Mapleson, Matthew Stocks
 */
public  class SRNAFastqReader
{
  private int count;
  private int r_count;
  private int nr_count;
  private final BufferedReader br;
  private final InputStreamReader isr;
  private FileInputStream fi = null;
  
  

  /**
   * Initialises a new reader object using the specified file, can enable memory management with this constructor
   * @param inFile The file containing sRNA reads in FastQ format to read from.
   * @throws IOException Thrown if there were can problems initialising readers to
   * the file.
   */
  public SRNAFastqReader( File inFile ) throws IOException
  {
    
    this.fi = new FileInputStream( inFile );
    this.isr = new InputStreamReader( fi );
    this.br = new BufferedReader( isr );
    

    count = 0;
    r_count = 0;
  }

  /**
   * Retrieves the number of distinct reads found in the file.  Should be equivalent
   * to calling getDistinctReadCount()
   * @return The number of distinct reads in the file
   */
  public int getCount()
  {
    return this.count;
  }

  /**
   * Retrieves the total number of reads found in the file
   * @return The total number of reads in the file
   */
  public int getTotalReadCount()
  {
    return this.r_count;
  }

  /**
   * Retrieves the number of distinct reads found in the file
   * @return The number of distinct reads in the file
   */
  public int getDistinctReadCount()
  {
    return this.nr_count;
  }

  /**
   * Starts reading from the file, creating a map keyed by distinct reads, mapping
   * to the number of reads found for that distinct sequence.
   * @return The map of distinct sequences mapped to abundance count
   * @throws IOException Thrown if there were any problems reading the file.
   */
  public HashMap<String, Integer> process() throws IOException
  {
    HashMap<String, Integer> data = new HashMap<String, Integer>();

    String line;
    while ( ( line = br.readLine() ) != null )
    {
      if ( line.startsWith( "@" ) )
      {
        // This should be the header line.  
        // We ignore it though as it's not required for this app

        // Assume the next line (containing the sequence) is going to be there!
        // Also convert to uppercase and convert RNA -> DNA
        //System.out.println("Line: " + line);
        String checkLine = br.readLine();
        if ( line == null || br == null || checkLine == null )
        {
          continue;
        }
        String seqLine = checkLine.toUpperCase().replace( 'U', 'T' );
        //System.out.println("seq line: " + seqLine);
        if ( !seqLine.matches( "^[ATGCN]*$" ) )
        {
          throw new IOException( "SRNA FASTQ ERROR: Sequence line contains unknown characters: " + seqLine );
        }

        // Ignore the next two lines as they are irrelevant for our needs.
        String line3 = br.readLine();
        if ( !line3.startsWith( "+" ) )
        {
          throw new IOException( "SRNA FASTQ ERROR: 3rd line does not start with a '+': " + line3 );
        }

        // Doesn't seem to be any structure to this line so don't do any checks.
        br.readLine();

        Integer freq = data.get( seqLine );
        int c = ( freq == null ) ? 1 : freq + 1;
        data.put( seqLine, c );

        count++;
        r_count += c;
      }
      else
      {
        throw new IOException( "SRNA FASTQ ERROR: Unexpected line: " + line );
      }
    }

    nr_count = data.size();

    br.close();
    isr.close();
    fi.close();

    return data;
  }
}
