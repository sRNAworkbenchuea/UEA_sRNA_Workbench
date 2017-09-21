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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.ArrayUtils;

import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.Decoder;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.FastqDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.TemporarySortedFastaDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.externalops.ExternalSort;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class SeqDataUtils
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
  
  
    /**
   * Starts reading from the FASTA file (which may be in redundant form), 
   * creating a non redundant file.
   * @param inputPath
   * @param outputPath the output path for the NR file (will be mapped)
   * @return Path The path to the non-redundant data
   * @throws IOException Thrown if there were any problems reading the file.
   */
  public static void convertRedundantToNonRedundant(Path inputPath, Path outputPath, int[] counts, int minLength, int maxLength) throws IOException
  {
    //convert create paths to sort input file
    String absolutePath = Tools.getNextDirectory().getAbsolutePath();
    String removeExtension = FilenameUtils.removeExtension( inputPath.getFileName().toString());
    
    //Path tempUnSortedPath = Paths.get( absolutePath + DIR_SEPARATOR + removeExtension + "_UNSORT.fa" );
    Path tempSortedPath = Paths.get( absolutePath + DIR_SEPARATOR + removeExtension + "_SORT.fa" );
    //Path tempRAW_FASTA_Path = Paths.get( FilenameUtils.removeExtension( outfile.getAbsolutePath() ) + "_RAW.fa" );

    //external sort uses default lexicographical 
    ExternalSort.sort( inputPath.toFile(), tempSortedPath.toFile() );

    // create NR output including stats
    create_NR_Output(tempSortedPath, outputPath, counts, minLength, maxLength);
    
  }
  
  private static void create_NR_Output(Path sorted_outputPath, Path outputPath, int[] counts, int minLength, int maxLength )
  {
    Charset charset = Charset.forName("UTF-8");
    if(counts == null)
        counts = new int[10];
    try
    {
      //open sorted file for streaming
      TemporarySortedFastaDecoder sortDecoder = new TemporarySortedFastaDecoder();
      FileStreamer<byte[]> reader = FileStreamer.create( sortDecoder, "r", sorted_outputPath.toFile() );

      Iterator<List<byte[]>> iterator = reader.iterator();
      List<byte[]> chunk;
      
      //record to be examined should persist over different chunks
      byte[] currentRecord = null;
      //as should its abundance score
      Integer abundance = 0;
      while ( iterator.hasNext())//iterator
      {
        chunk = iterator.next();
        //growable chunk size to give plenty of room for extra information
        ArrayList<Byte> toWriteNR = new ArrayList<>();
        int numberOfBytes[] = {0};

        int[] index = new int[1];
        index[0] = 0;
        //moving value that we are looking at in the current chunk
        for(byte[] toExamine : chunk)
        {
          //total adapter matches
          counts[2]++;
   
          //total that fit within the length range
          if ( toExamine.length >= minLength && toExamine.length <= maxLength )
            counts[8]++;
          if ( currentRecord == null )//first iteration
          {
            currentRecord = toExamine;
          }
          if ( Arrays.equals( toExamine, currentRecord ) )
          {
            abundance++;
          }
          else
          {
            //unique adapter matches
            counts[3] ++;
 
            if ( currentRecord.length >= minLength && currentRecord.length <= maxLength )
            {
              //unique that fit within the length range
              counts[9]++;
              createNR_Record( toWriteNR, numberOfBytes, currentRecord, abundance );
            }
            currentRecord = toExamine;

            abundance = 1;
          }
          
        }

        
        Byte[] clean = new Byte[numberOfBytes[0]];
        byte[] byteArray = ArrayUtils.toPrimitive(toWriteNR.toArray(clean));
        if(toWriteNR.size() > 0)
          Files.write( outputPath, byteArray, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
        
      }
      

      ArrayList<Byte> toWrite = new ArrayList<>();
      int numberOfBytes[] =
      {
        0
      };
      if ( currentRecord.length >= minLength && currentRecord.length <= maxLength )
      {
        //unique that fit within the length range
        counts[9]++;
        createNR_Record( toWrite, numberOfBytes, currentRecord, abundance );
        Byte[] clean = new Byte[numberOfBytes[0]];
        byte[] byteArray = ArrayUtils.toPrimitive( toWrite.toArray( clean ) );
        if ( toWrite.size() > 0 )
        {
          Files.write( outputPath, byteArray, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
        }
      }
      

 
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage());
    }
    

  }

  private static void createNR_Record( ArrayList<Byte> toWrite, int[] numberOfBytes, byte[] currentRecord, Integer abundance )
  {
    //write header
    byte FASTAheader = '>';
    toWrite.add( FASTAheader );
    numberOfBytes[0]++;
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      numberOfBytes[0]++;
    }
    byte open = '(';
    toWrite.add( open );
    numberOfBytes[0]++;
    for ( byte b : abundance.toString().getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
    byte close = ')';
    toWrite.add( close );
    numberOfBytes[0]++;
    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
    //write sequence
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      numberOfBytes[0]++;
    }

    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
  }

  private static boolean searchForSequence( byte[] recordToSearch, byte[] toWrite, FastqDecoder decoder, Charset charset, Path toSearch )
  {
    FileStreamer<byte[][]> searcher = FileStreamer.create( decoder, "r", toSearch.toFile() );
    String findMe = new String( recordToSearch, charset );
    //Iterator<List<byte[][]>> iterator = searcher.iterator();
    for ( List<byte[][]> chunk : searcher )//iterator
    {
      MappedByteBuffer current = searcher.getBuffer();

      for ( byte[][] record : chunk )
      {
        if ( Arrays.equals( record[1], recordToSearch ) )
        {
          String header = new String( record[0], charset );
          String[] split = header.split( "\\(" );
          int abundance = StringUtils.safeIntegerParse( split[1].replace( ")", "" ), 1 );
          abundance++;
          String reconstructedHeader = split[0] + "(" + abundance + ")";
          record[0] = Arrays.copyOf( record[0], reconstructedHeader.length() );
          record[0] = reconstructedHeader.getBytes();
          return true;
        }

      }
    }
    int lineStart = 0;
    int lineEnd = 0;
    for ( byte b : toWrite )
    {
      lineEnd++;
      if ( b == FastqDecoder.LF )
      {

        String toString = new String( Arrays.copyOfRange( toWrite, lineStart, lineEnd ), charset );
        //System.out.print( toString );
        if ( toString.equals( findMe ) )
        {
          return true;
        }
        lineStart = lineEnd + 1;
      }
    }
    return false;

  }
  

  public static void main( String[] args )
  {
//    //build stream reader and decode the file
//    FastqDecoder decoder = new FastqDecoder();
//    
//    FileStreamer<byte[][]> reader = FileStreamer.create( decoder,  "r", new File("/Developer/Applications/sRNAWorkbench/ExternalSource/sratoolkit.2.3.5-2-mac64/bin/SRR019550.fastq"));
//    Path output = Paths.get( "/Developer/Applications/sRNAWorkbench/ExternalSource/sratoolkit.2.3.5-2-mac64/bin/SRR019550.fa" );
//    int[] counts  = new int[2]; 
//    FASTQ_2_FASTA(reader, counts , output, null);
    
    Path input = Paths.get( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/R_NR_tests/GSM118373_Rajagopalan_col0_leaf_r.fa" );
    Path output = Paths.get( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/R_NR_tests/GSM118373_Rajagopalan_col0_leaf_NR.fa" );
        //counts records stats if needed
    int[] counts = {0,0,0,0,0,0,0,0,0,0};
    try
    {
      convertRedundantToNonRedundant(input, output, counts, 0, 50);
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage());
    }
  }
  

}
