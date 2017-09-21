/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.sequencestream;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.Decoder;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.FastqDecoder;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class RawSeqDataUtils
{
  private static void createFastaRecord( byte[] record, byte[] toWrite )
  {
    throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
  }

  private static void createFastaRecord( byte[] record, byte[] toWrite, int[] index, byte[] toSort )
  {
    //write header
    char FASTAheader = '>';
    toWrite[index[0]] = '>';
    index[0]++;
    for ( byte base : record )
    {

      toWrite[index[0]] = base;
      index[0]++;
    }
    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite[index[0]] = b;
      index[0]++;
    }
    //write sequence
    for ( byte base : record )
    {

      toWrite[index[0]] = base;
      index[0]++;
      toSort[index[1]] = base;
      index[1]++;

    }

    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite[index[0]] = b;
      index[0]++;
      toSort[index[1]] = b;
      index[1]++;
    }


  }
  
   /**
   * Performs a complete transform of a FASTQ file to a FASTA file
   * No processing of the data is done at this point
   * @param data 
   * @param counts 
   * @param outputPath 
   * @param unsortedPath 
   */
  public static void FASTQ_2_FASTA(FileStreamer<byte[][]> data, int[] counts, Path outputPath, Path unsortedPath)
  {
    
    
    for ( List<byte[][]> chunk : data )//iterator
    {
      System.gc();

      byte[] toWrite = new byte[FileStreamer.getChunkSize()];
      byte[] toSort = new byte[FileStreamer.getChunkSize()];
      //each record is four length array of FASTQ data, second value in the array is the sequence line

      //2 length array to force pass by reference
      int[] index = new int[2];
      index[0] = 0;
      index[1] = 0;
      for ( byte[][] record : chunk )
      {

        //increment total sequence count
        counts[0]++;
        
        createFastaRecord( record[1], toWrite, index, toSort);
       

        
      }
      // clean data then write new chunk to file 
      byte[] clean = clean( toWrite );
      byte[] cleanSort = clean( toSort );
      
      try
      {
       
        Files.write( outputPath, clean, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        if(unsortedPath != null)
          Files.write( unsortedPath, cleanSort, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        
      }
      catch ( IOException e )
      {
        LOGGER.log( Level.SEVERE, e.getMessage() );
      }
    }
    
  }
  
  public static byte[] clean( final byte[] v )
  {
    int r, w;
    final int n = r = w = v.length;
    while ( r > 0 )
    {
      final byte s = v[--r];
      if ( s != 0 )
      {
        v[--w] = s;
      }
    }
    return Arrays.copyOfRange( v, w, n );
  }
  
  private boolean searchForSequence( byte[] recordToSearch, byte[] toWrite, FastqDecoder decoder,  Charset charset, Path toSearch)
  {
    FileStreamer<byte[][]> searcher = FileStreamer.create( decoder,  "r", toSearch.toFile());
    String findMe = new String(recordToSearch, charset);
    //Iterator<List<byte[][]>> iterator = searcher.iterator();
    for ( List<byte[][]> chunk : searcher )//iterator
    {
      MappedByteBuffer current = searcher.getBuffer();
      
      for ( byte[][] record : chunk )
      {
        if(Arrays.equals( record[1], recordToSearch ))
        {
          String header = new String(record[0], charset);
          String[] split = header.split( "\\(");
          int abundance = StringUtils.safeIntegerParse( split[1].replace( ")", ""),1);
          abundance++;
          String reconstructedHeader = split[0] + "(" + abundance + ")";
          record[0] = Arrays.copyOf( record[0], reconstructedHeader.length());
          record[0] = reconstructedHeader.getBytes();
          return true;
        }

      }
    }
    int lineStart = 0;
    int lineEnd = 0;
    for(byte b : toWrite)
    {
      lineEnd++;
      if(b == FastqDecoder.LF)
      {
        
        String toString = new String(Arrays.copyOfRange( toWrite, lineStart, lineEnd), charset);
        //System.out.print( toString );
        if(toString.equals( findMe ))
          return true;
        lineStart = lineEnd+1;
      }
    }
    return false;

  }
  public static void main( String[] args )
  {
    //build stream reader and decode the file
    FastqDecoder decoder = new FastqDecoder();
    
    FileStreamer<byte[][]> reader = FileStreamer.create( decoder,  "r", new File("/Developer/Applications/sRNAWorkbench/ExternalSource/sratoolkit.2.3.5-2-mac64/bin/SRR019550.fastq"));
    Path output = Paths.get( "/Developer/Applications/sRNAWorkbench/ExternalSource/sratoolkit.2.3.5-2-mac64/bin/SRR019550.fa" );
    int[] counts  = new int[2]; 
    FASTQ_2_FASTA(reader, counts , output, null);
  }
  

}
