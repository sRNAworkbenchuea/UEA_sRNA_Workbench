/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.externalops;

import java.io.IOException;
import java.io.RandomAccessFile;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.StreamFastaMapRecord;
import uk.ac.uea.cmp.srnaworkbench.io.IORecord;

/**
 *
 * @author w0445959 Binary search Lexi decending string only geared at patman with seq record
 */
//----------------------------------------------------------------------




public class ExternalBinarySearch
{

  //--------------------------------------------------
  public static boolean binarySearch(
    RandomAccessFile hFile, IORecord rcdTemp, String toSearch )
    throws IOException
  {
    long nbRecords = hFile.length() / rcdTemp.length();
    IORecord rcdRes
      = binarySearchAux( hFile, rcdTemp, 0, nbRecords - 1, toSearch );

    return rcdRes != null;
  }

  //--------------------------------------------------
  private static IORecord binarySearchAux(
    RandomAccessFile hFile, IORecord rcdTemp,
    long iLeft, long iRight, String toSearch )
    throws IOException
  {

    if ( iRight < iLeft )
    {
      return null;
    }

    long iMiddle = ( iLeft + iRight ) / 2;
    rcdTemp.read( hFile, iMiddle );
    int iCompare = rcdTemp.compareTo( toSearch );

    if ( iCompare < 0 )
    {
      return binarySearchAux( hFile, rcdTemp, iLeft, iMiddle - 1, toSearch );
    }

    if ( iCompare > 0 )
    {
      return binarySearchAux( hFile, rcdTemp, iMiddle + 1, iRight, toSearch );
    }

    return rcdTemp;
  }

  //--------------------------------------------------
  public static void main( String[] args )
  {
   
    //----- binary search -----
    try
    {
      // open the file
      RandomAccessFile hFile
        = new RandomAccessFile( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/small_FASTA_MAP_NEW.pds", "r" );

      try
      {

        // search a few names
        StreamFastaMapRecord rcdTemp = new StreamFastaMapRecord(  );
        binarySearch( hFile, rcdTemp, "ATAGGTGTGTATGTGAGAAGGTA" );
        binarySearch( hFile, rcdTemp, "Bob" );
        binarySearch( hFile, rcdTemp, "ATAGGTGTGTATGTGAGAAGGTA(8)-1" );
      }
      finally
      {
        // close the file
        hFile.close();
      }
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
}
