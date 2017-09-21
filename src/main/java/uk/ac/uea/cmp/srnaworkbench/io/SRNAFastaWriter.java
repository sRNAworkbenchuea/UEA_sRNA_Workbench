/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import java.util.Map;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 * Write a FastA file containing sRNA reads.
 * @author Dan Mapleson
 */
public class SRNAFastaWriter
{
  private FileOutputStream fos;
  private OutputStreamWriter osw;
  private BufferedWriter bw;
  private PrintWriter pw;

  /**
   * Creates a writer object initialised with the specified file
   * @param out_file The file to write to.
   * @throws IOException Thrown if there were any problems initialising the file writers
   */
  public SRNAFastaWriter( File out_file ) throws IOException
  {
    this.fos = new FileOutputStream( out_file );
    this.osw = new OutputStreamWriter( fos );
    this.bw = new BufferedWriter( osw );
    this.pw = new PrintWriter( bw );
  }

  
  /**
   * Writes map data (distinct reads keyed to abundance counts) to a FastA file, 
   * displaying the abundance information in brackets after the sequence in the 
   * FastA header.
   * User can specify whether to keep the data in non-redundant format, or write
   * to redundant format.
   * @param data Distinct reads keyed to abundance counts
   * @param nr Whether to output in non-redundant or redundant format
   * @param abd Whether to output abundance in brackets after seq. in header
   * @throws IOException Thrown if there was a problem writing to file.
   */
  public void process( Map<String, Integer> data, boolean nr ) throws IOException
  {
    process( data, nr, true );
  }
  
  /**
   * Writes map data (distinct reads keyed to abundance counts) to a FastA file.
   * User can specify whether to keep the data in non-redundant format, or write
   * to redundant format.
   * @param data Distinct reads keyed to abundance counts
   * @param nr Whether to output in non-redundant or redundant format
   * @param abd Whether to output abundance in brackets after seq. in header
   * @throws IOException Thrown if there was a problem writing to file.
   */
  public void process( Map<String, Integer> data, boolean nr, boolean abd ) throws IOException
  {
    FastaMap fm = new FastaMap( data );

    process( fm, nr, abd );
  }
  
  /**
   * Writes a {@link FastaMap} to a FastA file, displaying the abundance information
   * in brackets after the sequence in the FastA header.
   * User can specify whether to keep the data in non-redundant format, or write
   * to redundant format.
   * @param data FastaMap containing data to write to disk
   * @param nr Whether to output in non-redundant or redundant format
   * @throws IOException Thrown if there was a problem writing to file.
   */
  public void process( FastaMap data, boolean nr ) throws IOException
  {
    process( data, nr, true );
  }

  /**
   * Writes a {@link FastaMap} to a FastA file.
   * User can specify whether to keep the data in non-redundant format, or write
   * to redundant format.  User can also specify if they would like the abundance
   * to be displayed in brackets after the sequence in the FastA header.
   * @param data FastaMap containing data to write to disk
   * @param nr Whether to output in non-redundant or redundant format
   * @param abd Whether to output abundance in brackets after seq. in header
   * @throws IOException Thrown if there was a problem writing to file.
   */
  public void process( FastaMap data, boolean nr, boolean abd ) throws IOException
  {
    // Format abundances to 3 dp but only when required: i.e. integers are still
    // output as integers
    DecimalFormat df = new DecimalFormat("#.###");
    if ( nr )
    {
      for ( FastaRecord fr : data.values() )
      {
        pw.print( '>' );
        pw.print( fr.getSequenceId() );
        
        if ( abd )
        {
          pw.print( '(' );
          pw.print( df.format( fr.getRealAbundance() ));
          pw.print( ")" );
        }
        
        pw.print( "\n" );

        pw.print( fr.getSequence() );
        pw.print( '\n' );
      }
    }
    else
    {
      for ( String seq : data.keySet() )
      {
        // FIXME: Fasta ouput not set up to appropriately handle non-integer data
        // Fasta output models the abundance as real counts. This is unfortunately not
        // the case when the counts are normalized, resulting in a continuous value
        // The current fix is to floor the values.
        int N = (int) data.get( seq ).getAbundance();
        String s = ">" + seq + LINE_SEPARATOR + seq + LINE_SEPARATOR;

        for ( int i = 0; i < N; ++i )
        {
          pw.print( s );
        }
      }
    }

    pw.flush();
    pw.close();
    bw.close();
    osw.close();
    fos.close();
  }
}
