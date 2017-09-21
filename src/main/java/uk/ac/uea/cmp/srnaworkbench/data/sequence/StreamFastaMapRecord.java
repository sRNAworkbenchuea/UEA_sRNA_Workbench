/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.io.IORecord;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
//----------------------------------------------------------------------
public class StreamFastaMapRecord extends IORecord
{
  private static final int RECORD_LENGTH = 60;

  private byte[] seq_record;  // sequenceRecord This will contain all data

  //--------------------------------------------------
  public StreamFastaMapRecord( String completeRecord )
  {
    init( completeRecord );
  }
  
  public StreamFastaMapRecord()
  {
    seq_record  = new byte[RECORD_LENGTH];
  }

  //--------------------------------------------------

  private void init( String completeRecord )
  {
    seq_record = new byte[RECORD_LENGTH];

    if ( null != completeRecord )
    {
      int iMax = Math.min( completeRecord.length(), RECORD_LENGTH );
      for ( int i = 0; i < iMax; ++i )
      {
        seq_record[ i] = (byte) completeRecord.charAt( i );
      }
    }
  }

  //--------------------------------------------------
  public void write( RandomAccessFile hFile )
    throws IOException
  {
    hFile.write( seq_record );
  }

  //--------------------------------------------------
  public void read( RandomAccessFile hFile, long iRecNo )
    throws IOException
  {
    hFile.seek( iRecNo * length() );
    hFile.read( seq_record );
  }

  //--------------------------------------------------
  public String toString()
  {
    return new String( seq_record ).trim();
  }
  
  public String getRawSequence() 
  {
    String trim = new String( seq_record ).trim();
    String[] split = trim.split( "\\(");
    if(split.length == 0)
    {
      LOGGER.log(Level.SEVERE, "Found inconsistent record in patman FASTA map data");
      return "";
    }
    return split[0];
  }

  //--------------------------------------------------
  public String report()
  {
    return toString();
  }

  //--------------------------------------------------
  public int length()
  {
    return RECORD_LENGTH;
  }


  @Override
  public int compareTo( String o )
  {

    return getRawSequence().compareTo( o );

  }

}