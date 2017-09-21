/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.decoders;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;

/**
 *
 * @author w0445959
 */
public class PatmanDecoder implements Decoder<byte[][]>
{

  public static final byte LF = 10;

  private final byte delimiter;
  private final int numFields = 6;
  
  public PatmanDecoder() {
   this.delimiter = '\t';
  }
  
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
      if (b == delimiter || b == LF) {
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
    File file = new File("/Developer/Applications/sRNAWorkbench/Workbench/TutorialData/ALIGNED/GSM118373_SMALL.patman");
    Charset charset = Charset.forName("UTF-8");
    PatmanDecoder decoder = new PatmanDecoder();
    FileStreamer<byte[][]> reader = FileStreamer.create( decoder, "r",  file);

    for ( List<byte[][]> chunk : reader )
    {
      
      for(byte[][] record : chunk)
      {
        int recordIndex = 0;

        for(byte[] recordData : record)
        {
          CharSequence seq2 = new String(recordData, charset);
          System.out.println( "record index: " + recordIndex + " " + seq2 );
          recordIndex++;
        }
      }
    }
  }
}