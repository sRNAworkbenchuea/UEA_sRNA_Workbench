/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 * Reads a FastA file containing sRNA reads.
 * @author Dan Mapleson
 */
public class SRNAFastaReader
{
  private int r_count;
  private int nr_count;
  private BufferedReader br;
  private InputStreamReader isr;
  private FileInputStream fi = null;

  /**
   * Initialises a new reader object using the specified file
   * @param infile The file containing sRNA reads in FastA format to read from.
   * @throws IOException Thrown if there were can problems initialising readers to
   * the file.
   */
  public SRNAFastaReader( File infile ) throws IOException
  {
    this.fi = new FileInputStream( infile );
    this.isr = new InputStreamReader( fi );
    this.br = new BufferedReader( isr );

    r_count = 0;
    nr_count = 0;
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
   * This has been modified to a temporary hack whilst we work out what to do with
   * processing non-integer data. The normal process() function now does extra work converting
   * Double abundances to Integer abundances for the sake of legacy calls to this function
   * that expect a String -> Integer Map. The actual processing of a file is now read in as if
   * all values are doubles.
   * @return
   * @throws IOException 
   */
  public HashMap<String, Integer> process() throws IOException
  {
    HashMap<String, Double> doubleData = processDoubles();
    HashMap<String, Integer> intData =  new HashMap<>();
    for(Entry<String, Double> e : doubleData.entrySet())
    {
      intData.put(e.getKey(), (int)Math.round( e.getValue() ));
    }
    
    return(intData);
    
  }

  /**
   * Starts reading from the file, creating a map keyed by distinct reads, mapping
   * to the number of reads found for that distinct sequence.
   * @return Map The map of distinct sequences mapped to abundance count
   * @throws IOException Thrown if there were any problems reading the file.
   */
  public HashMap<String, Double> processDoubles() throws IOException
  {
    HashMap<String, Double> data = new HashMap<>();

    String line = "";
    boolean done = false;
    while ( !done )
    {
      if ( line.startsWith( ">" ) )
      {
        String id = "";
        int idx;
        double abundance = 1;
        if ( ( idx = line.indexOf( "(" ) ) > -1 )
        {
          id = line.substring( 1, idx );
          if(line.indexOf( ")" )  > -1)
          {

            try{
            abundance = Double.parseDouble( line.substring( idx + 1, line.indexOf( ")" ) ) );
            }
            catch(NumberFormatException e)
            {
              LOGGER.log( Level.SEVERE, 
              "Bad formatting in input file. {0}Line with problem:{1}", 
              new Object[]{ LINE_SEPARATOR, line });
              throw new IOException("Bad formatting in input file. Line with problem: " + LINE_SEPARATOR + line);
              
            }
          }
          else
          {
            LOGGER.log( Level.SEVERE, 
              "Abundance information not available during decode of FASTA file. {0}Line with problem:{1}", 
              new Object[]{ LINE_SEPARATOR, line });
          }
          
        }
//        else
//        {
//          LOGGER.log( Level.SEVERE, 
//            "Abundance information not available during decode of FASTA file. {0}Line with problem:{1}", 
//            new Object[]{ LINE_SEPARATOR, line });
//        }

        // Assume the next line is going to be there!
        StringBuilder sb = new StringBuilder();
        while ( ( line = br.readLine() ) != null )
        {
          if ( line.startsWith( ">" ) )
          {
            break;
          }
          sb.append( line );
        }

        String seq = sb.toString();

        Double freq = data.get( seq );
        double c = ( freq == null ) ? abundance : freq + abundance;
        data.put( seq, c );
      }
      else
      {
        line = br.readLine();
      }

      if ( line == null )
      {
        done = true;
      }
    }

    // Probably not the most efficient way of getting the redundant count
    // it would probably be faster to include something in the reading loop
    // above but it's simple and clean, and I'm hoping won't impact performance
    // too much.
    for ( Double i : data.values() )
    {
      r_count += i;
    }

    nr_count = data.size();

    br.close();
    isr.close();
    fi.close();

    return data;
  }

  //****************************************************************************

  public static void main( String[] args )
  {
    String fileName = "D:/LocalData/hugh/seq_data/irina_data/Organs/GSM118372.fa";

    try
    {
      SRNAFastaReader sfr = new SRNAFastaReader( new File( fileName ) );

      java.util.Map<String, Integer> map = sfr.process();
    }
    catch ( IOException ex )
    {
      System.err.println( ex );
    }
  }
}
