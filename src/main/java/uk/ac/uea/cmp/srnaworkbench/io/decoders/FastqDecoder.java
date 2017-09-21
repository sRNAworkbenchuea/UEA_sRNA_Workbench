/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.decoders;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author M.B Stocks
 */
public class FastqDecoder implements Decoder<byte[][]> {
  public static final byte LF = 10;

  private final byte delimiter;
  private final int numFields = 4;
  
  public FastqDecoder() {
   this.delimiter = '@';
  }
  /**
   * 
   * @param buffer
   * @return two dimensional byte array. First index is field count for the line
   * second index is the field itself, if for example delimiter is new line then the array
   * will always have just one item per line 
   */
  
  @Override
  public byte[][] decode( ByteBuffer buffer )
  {
    int lineStartPos = buffer.position();
    int limit = buffer.limit();
    int lineCount = 0;
    while ( buffer.hasRemaining() )
    {
      byte b = buffer.get();
      if ( b == LF )
      { // reached line feed so parse line
        lineCount++;
      }
      if(lineCount == 4)
      {
        int lineEndPos = buffer.position();
        // set positions for one row duplication
        if ( buffer.limit() < lineEndPos + 1 )
        {
          buffer.position( lineStartPos ).limit( lineEndPos );
        }
        else
        {
          buffer.position( lineStartPos ).limit( lineEndPos + 1 );
        }
        byte[][] entry = parseRow( buffer.duplicate() );
        if ( entry != null )
        {
          // reset main buffer
          buffer.position( lineEndPos );
          buffer.limit( limit );
          // set start after LF
          lineStartPos = lineEndPos;
        }
        return entry;
      }
    }
    buffer.position( lineStartPos );
    return null;
  }
 
  public byte[][] parseRow(ByteBuffer buffer) {
    int fieldStartPos = buffer.position();
    int fieldEndPos = 0;
    int fieldNumber = 0;
    byte[][] fields = new byte[numFields][];
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      if ( b == LF) {
        fieldEndPos = buffer.position();
        // save limit
        int limit = buffer.limit();
        // set positions for one row duplication
        buffer.position(fieldStartPos).limit(fieldEndPos);
        fields[fieldNumber] = parseField(buffer.duplicate(), fieldNumber, fieldEndPos - fieldStartPos - 1);
        fieldNumber++;
        // reset main buffer
        buffer.position(fieldEndPos);
        buffer.limit(limit);
        // set start after LF
        fieldStartPos = fieldEndPos;
      }
      if (fieldNumber == numFields) {
        return fields;
      }
    }
    return null;
  }
 
  private byte[] parseField(ByteBuffer buffer, int pos, int length) {
    byte[] field = new byte[length];
    for (int i = 0; i < field.length; i++) {
      field[i] = buffer.get();
    }
    return field;
  }
  //****************************************************************************

  public static void main( String[] args )
  {
    File file = new File("/Developer/Applications/sRNAWorkbench/TestingData/ChunkStreamTests/RDR6_Test.fastq");
    //File file = new File("C:\\Users\\w0445959\\LocalDisk\\PostDoc\\NetBeansProjects\\sRNAWorkbench\\Testing\\RichardBarker\\FASTQ\\run297.9-0GS_ATCACG_L001_R1.fastq");
    Charset charset = Charset.forName("UTF-8");
    byte delimiter = LF;
    FastqDecoder decoder = new FastqDecoder();
    //DelimitedDecoder decoder = new DelimitedDecoder(2,delimiter);
    FileStreamer<byte[][]> reader = FileStreamer.create( decoder, "r",  file);

    for ( List<byte[][]> chunk : reader )
    {
//      CharSequence seq2 = new String(chunk.get( 0)[0], charset);
//      System.out.println( seq2.toString() );
      for(byte[][] small : chunk)
      {
        for(byte[] smallBit : small)
        {
          CharSequence seq2 = new String(smallBit, charset);
          System.out.println( seq2 );
        }
      }
    }
  }
}