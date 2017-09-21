/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.StreamFastaMapRecord;
import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.PatmanDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.externalops.ExternalBinarySearch;
import uk.ac.uea.cmp.srnaworkbench.io.externalops.ExternalSort;
import uk.ac.uea.cmp.srnaworkbench.utils.ByteUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class PatmanStream 
{
  //Data Paths
  private Path streamPath;
  private Path fastaMapFilePath;

  //charset for input data 
  private final Charset charset = Charset.forName("UTF-8");
  
  private final PatmanDecoder decoder = new PatmanDecoder();
  
  
  private HashMap<String, Path> chromToPath = new HashMap<>();
  
  private HashSet<String> chromoIDs = new HashSet<>();
  //private HashMap<String, ArrayList<Byte>> chromToData = new HashMap<>();
  
  public PatmanStream()
  {
    streamPath = null;
  }

  public PatmanStream( Path patman_path )
  {
    streamPath = patman_path;
    checkStream();
  }

  public void setStreamPath( Path streamPath )
  {
    this.streamPath = streamPath;
    checkStream();
  }

  public Path getStreamPath()
  {
    return streamPath;
  }
  
  public final void checkStream()
  {
  
    if((streamPath == null) || !Files.exists( streamPath ))
      throw new IllegalArgumentException("Path to patman file is null or does not exist, please set path prior to use");
  }
  public static Comparator<String> patman_SeqLexicomparator = new Comparator<String>()
  {
    @Override
    public int compare( String r1, String r2 )
    {
      String[] split_1 = r1.split( "\t");
      String[] split_2 = r2.split( "\t");
      return split_2[1].compareTo( split_1[1] );
    }
  };
  
  public static Comparator<String> patman_StartCoordNumericComparator = new Comparator<String>()
  {
    @Override
    public int compare( String r1, String r2 )
    {
      String[] split_1 = r1.split( "\t");
      String[] split_2 = r2.split( "\t");
      Integer start1 = StringUtils.safeIntegerParse( split_1[2], 0);
      Integer start2 = StringUtils.safeIntegerParse( split_2[2], 0);
      return start1.compareTo( start2 );
    }
  };
  /**
   * Produces a FastaMap from a patman_file.  Hit locations are ignored.  Essentially,
   * this creates a file with a subset of the data originally mapped by patman.
   * The file will contain, sequence (reported once), abundance and hit count for that sequence
   */
  public void buildFastaMapedFile()
  {

    
    
    //convert create paths to sort input file
    String absolutePath = Tools.getNextDirectory().getAbsolutePath();
    String removeExtension = FilenameUtils.removeExtension( streamPath.getFileName().toString());
    
    //Path tempUnSortedPath = Paths.get( absolutePath + DIR_SEPARATOR + removeExtension + "_UNSORT.fa" );
    Path tempSortedPath = Paths.get( absolutePath + DIR_SEPARATOR + removeExtension + "_SORT.fa" );
    //Path tempRAW_FASTA_Path = Paths.get( FilenameUtils.removeExtension( outfile.getAbsolutePath() ) + "_RAW.fa" );
    try
    {
      //external sort uses default lexicographical
      ExternalSort.sort( streamPath.toFile(), tempSortedPath.toFile(), patman_SeqLexicomparator );
    
    
    
      HashMap<String, ArrayList<Byte>> chromToData = new HashMap<>();
      FileStreamer<byte[][]> reader = FileStreamer.create( decoder, "r",  tempSortedPath.toFile());

      fastaMapFilePath = Paths.get( absolutePath + DIR_SEPARATOR + FilenameUtils.removeExtension( streamPath.getFileName().toString() ) + "_FASTA_MAP.pds" );

      //record to be examined should persist over different chunks
      byte[] currentRecord = null;
      //as should its genome hit score
      Integer genomeHit = 0;
      int totalWrites = 0;
      ThreadPoolExecutor es;
      int totalThread = Tools.getThreadCount();
      //TO_DO add thread limit
//      if ( numToGenerate < totalThread )
//      {
//        totalThread = numToGenerate;
//      }
      es = (ThreadPoolExecutor) Executors.newFixedThreadPool( totalThread );

      //es.execute( new Runnable{);
      for ( List<byte[][]> chunk : reader )
      {
        //60 bytes per record
        //byte[] toWrite = new byte[chunk.size()*60];
        ArrayList<Byte> toWrite = new ArrayList<>();
        int byteIndex = 0;

        //clear any data in chrom byte arrays for writing
        for(Entry<String, ArrayList<Byte>> e : chromToData.entrySet())
        {
          e.getValue().clear();
        }

        //start execution of threads
        for ( byte[][] record : chunk )
        {
        //tells us which field of the patman record it is looking at
          //there are 6 in total
          //0=Chromosome ID, 1 = Sequence(abundance), 2 start coord, 3 end coord, 4 strand, 5 gaps
          int recordIndex = 0;

          for ( byte[] field : record )
          {
            switch ( recordIndex )
            {
              case 0:
                String chromName = new String( field, charset );
                if ( !chromToPath.containsKey( chromName ) )
                {
                  String chromFile = absolutePath + DIR_SEPARATOR + chromName.replaceAll( "[^a-zA-Z0-9\\.\\-]", "_" );
                  chromToPath.put( chromName, Paths.get( chromFile ) );
                  chromToData.put(chromName, new ArrayList<Byte>());
                }
                for ( byte[] chromData : record )
                {
                  for(byte b : chromData)
                    chromToData.get( chromName ).add(b );
                  for(byte b : "\t".getBytes())
                    chromToData.get( chromName ).add( b );
                }
                for(byte b : LINE_SEPARATOR.getBytes())
                  chromToData.get( chromName ).add( b );
//                for ( byte[] chromData : record )
//                {
//                  Files.write( chromToPath.get( chromName ), chromData, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
//                  Files.write( chromToPath.get( chromName ), "\t".getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
//                }
//                Files.write( chromToPath.get( chromName ), LINE_SEPARATOR.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
                break;
              case 1://sequence
                if ( currentRecord == null )//first iteration
                {
                  currentRecord = field;
                }
                if ( Arrays.equals( field, currentRecord ) )
                {
                  genomeHit++;
                }
                else
                {
                  totalWrites++;

                  createFastaMapRecord( toWrite, currentRecord, genomeHit );
                  //createFastaMapRecord( toWriteNR, numberOfBytes, currentRecord, genomeHit );


                  currentRecord = field;
                  //move byte pointer up to next record
                  byteIndex+=60;

                  genomeHit = 1;
                }
                break;
              case 2:
                break;
              case 3:
                break;
              case 4:
                break;
              case 5:
                break;
              default:
                break;

            }
            recordIndex++;
          }
        }
        
        //end thread execution and wait until complete then write results
        
        for(Entry<String, ArrayList<Byte>> e : chromToData.entrySet())
        {
          Byte[] clean = new Byte[e.getValue().size()];
          byte[] byteArray = ArrayUtils.toPrimitive(e.getValue().toArray(clean));
          Files.write( chromToPath.get( e.getKey() ) , byteArray, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
        }
       
        if ( toWrite.size() > 0 )
        {

          //Files.write( fastaMapPath, Arrays.copyOfRange( toWrite, 0, totalWrites*60 ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );
          Byte[] clean = new Byte[toWrite.size()];
          byte[] byteArray = ArrayUtils.toPrimitive(toWrite.toArray(clean));
          
          Files.write( fastaMapFilePath, byteArray, StandardOpenOption.APPEND, StandardOpenOption.CREATE );

        }
      }
      
      es.shutdown();
      byte[] toWrite = new byte[60];

      createFastaMapRecord( toWrite, 0, currentRecord, genomeHit );
      //es.shutdown();
      if ( toWrite.length > 0 )
      {

        Files.write( fastaMapFilePath, toWrite, StandardOpenOption.APPEND, StandardOpenOption.CREATE );

      }

      
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage() );
    }

    
  }

//  public void buildFastaDiskBasedHashMap()
//  {
//   
//    try
//    {
//      //TODO_ check if database exists already, if so do not need to remake
//
//      DB db = DatabaseManager.openDatabase(streamPath.getFileName().toString());
//      
//      //open or create FASTA MAP table
//      ConcurrentNavigableMap<String, FastaRecord> FastaMap = db.getTreeMap( "FastaMap" );
//
//      ConcurrentNavigableMap<Fun.Tuple3, byte[][]> chromoMap;
//      
//      chromoMap = db.createTreeMap( "ChromoMap" ).keySerializer( BTreeKeySerializer.TUPLE3 ).makeOrGet();
//
//      //open patman file for streaming
//      FileStreamer<byte[][]> reader = FileStreamer.create( decoder, "r", streamPath.toFile() );
//      
//      for ( List<byte[][]> chunk : reader )
//      {
//        for ( byte[][] record : chunk )
//        {
//          //tells us which field of the patman record it is looking at
//          //there are 6 in total
//          //0=Chromosome ID, 1 = Sequence(abundance), 2 start coord, 3 end coord, 4 strand, 5 gaps
//          int recordIndex = 0;
//          
//          for ( byte[] field : record )
//          {
//            switch ( recordIndex )
//            {
//              case 0:
//                String chromoID = new String( field, charset );
//                this.chromoIDs.add( chromoID );
//                String startString = new String(record[2], charset);
//                String endString = new String(record[3], charset);
//                chromoID += new String(record[4], charset);
//                
//                Fun.Tuple3<String, Integer, Integer> chromKey = 
//                  Fun.t3(chromoID, StringUtils.safeIntegerParse( startString, 0 ), StringUtils.safeIntegerParse( endString, 0 ));
//
//                chromoMap.put( chromKey, record );
//                
//                break;
//              case 1://sequence
//                String sequence = new String( field, charset );
//                String[] seqData = sequence.split( "\\(" );
//                
//                if ( seqData.length > 0 )
//                {
//                  String tempAbun = seqData[1].replace( ")", "" );
//                  int currentAbund = Integer.parseInt( tempAbun.trim() );
//                  
//                  FastaRecord fr = new FastaRecord( seqData[0], currentAbund );
//                  
//                  FastaRecord val = FastaMap.get( seqData[0] );
//                  if ( val == null )
//                  {
//                    fr.setHitCount( 1 );
//                    FastaMap.put( fr.getSequence(), fr );
//                  }
//                  else
//                  {
//                    val.incHitCount();
//                  }
//                }
//                else
//                {
//                  LOGGER.log( Level.WARNING, "Data inconsistency in patman file" );
//                }
//                break;
//              case 2:
//                break;
//              case 3:
//                break;
//              case 4:
//                break;
//              case 5:
//                break;
//              default:
//                break;
//                
//            }
//            recordIndex++;
//          }
//        }
//        // Commit and close
//        db.commit();
//      }
//      
//      
//      
//      
//    }
//    catch ( IOException ex )
//    {
//      LOGGER.log( Level.SEVERE, ex.getMessage() );
//    }
//
//  }
//  
  public void buildPatmanDatabase()
  {
    
  }

  private void createFastaMapRecord( ArrayList<Byte> toWrite, int[] numberOfBytes, byte[] currentRecord, Integer genomeHit )
  {
    //write sequence and abundance
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      numberOfBytes[0]++;
    }
    byte hyp = '-';
    toWrite.add( hyp );
    numberOfBytes[0]++;
    //write number of hits to genome
    for ( byte b : genomeHit.toString().getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
  }

  private void createFastaMapRecord( ArrayList<Byte> toWrite, byte[] currentRecord, Integer genomeHit )
  {
    //write sequence and abundance
    int totalBytes = 0;
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      totalBytes++;
    }
    byte hyp = '-';
    toWrite.add( hyp );
    totalBytes++;
    //write number of hits to genome
    for ( byte b : genomeHit.toString().getBytes() )
    {
      toWrite.add( b );
      totalBytes++;
    }
    byte[] temp = new byte[1];
    while ( totalBytes <= 60 )
    {
      toWrite.add( temp[0] );
      totalBytes++;
    }

  }

  private void createFastaMapRecord( byte[] toWrite, int currentByteIndex, byte[] currentRecord, Integer genomeHit )
  {
    //write sequence and abundance

    for ( byte base : currentRecord )
    {

      toWrite[currentByteIndex] = base;
      currentByteIndex++;
    }
    byte hyp = '-';
    toWrite[currentByteIndex] = hyp;
    currentByteIndex++;
    //write number of hits to genome
    for ( byte b : genomeHit.toString().getBytes() )
    {
      toWrite[currentByteIndex] = b;
      currentByteIndex++;
    }

  }

  public static void main( String[] args )
  {

    //PatmanStream patmanStream = new PatmanStream( Paths.get( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/small.patman"));
    //PatmanStream patmanStream = new PatmanStream(Paths.get( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa" ) );
    PatmanStream patmanStream = new PatmanStream( Paths.get( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas.patman" ) );

    //patmanStream.buildFastaDiskBasedHashMap();
    
    patmanStream.checkMap();
    //patmanStream.buildFastaMapedFile();
    //patmanStream.buildChromosomeMappedSortedFile();
    
    
    System.out.println( "fin" );
  }

  public void buildChromosomeMappedSortedFile()
  {
    for ( Entry<String, Path> e : this.chromToPath.entrySet() )
    {
      try
      {
        //external sort uses default lexicographical
        Path sortedFile = Paths.get( FilenameUtils.removeExtension( e.getValue().toString() ) + "_SORTED.patman" );
        Files.createFile( sortedFile );
        ExternalSort.sort( e.getValue().toFile(), sortedFile.toFile(), patman_StartCoordNumericComparator );
        chromToPath.put( e.getKey(), sortedFile );
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, ex.getMessage() );
      }

    }
  }

  public boolean searchDBForSequence( String toSearch )
  {

    return false;
//    //open or create FASTA MAP table
//    ConcurrentNavigableMap<String, FastaRecord> FastaMap;
//    try
//    {
//      FastaMap = DatabaseManager.openDatabase( this.streamPath.getFileName().toString() ).getTreeMap( "FastaMap" );
//      return FastaMap.containsKey( toSearch );
//    }
//    catch ( IOException ex )
//    {
//      LOGGER.log(Level.WARNING, ex.getMessage() );
//      return false;
//    }

    
  }

  public boolean searchMapForSequence( String toSearch ) 
  {
    boolean binarySearch = false;
    RandomAccessFile hFile = null;
    try
    {
      // open the file
       hFile
        = new RandomAccessFile( fastaMapFilePath.toString(), "r" );
      
      // search a few names
      StreamFastaMapRecord rcdTemp = new StreamFastaMapRecord();
      binarySearch = ExternalBinarySearch.binarySearch( hFile, rcdTemp, toSearch );
    }
    catch ( FileNotFoundException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
    finally
    {
      try
      {
        // close the file
        if(hFile != null) hFile.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log(Level.SEVERE, ex.getMessage() );
      }
    }
    
    return  binarySearch;
  }

//  public static class WordLengthCallable
//    implements Callable
//  {
//    private List<ArrayList<Byte>> results;
//
//    public WordLengthCallable( List<ArrayList<Byte>> resultSet )
//    {
//      this.results = resultSet;
//    }
//
//    @Override
//    public List<ArrayList<Byte>> call()
//    {
//      //return Integer.valueOf( word.length() );
//    }
//  }


  public void closeFastaMapDB()
  {
//    try
//    {
//      DatabaseManager.closeDatabase( this.streamPath.getFileName().toString());
//    }
//    catch ( Exception ex )
//    {
//      LOGGER.log(Level.WARNING, ex.getMessage() );
//    }
  }

  private void checkMap()
  {
//    try
//    {
//      DB db = DatabaseManager.openDatabase(streamPath.getFileName().toString());
//      ConcurrentNavigableMap<Fun.Tuple3, byte[][]> chromoMap = db.createTreeMap( "ChromoMap" ).keySerializer( BTreeKeySerializer.TUPLE3 ).makeOrGet();
//      System.out.println( "There are " + chromoMap.size() + " records in total" );
//      int printed = 0;
//      for ( byte[][] record : chromoMap.values() )
//      {
//        for(byte[] field : record)
//        {
//          System.out.print( new String(field, charset) + "             " );
//        }
//        System.out.println(  );
//        printed++;
//      }
//      System.out.println( "printed: " + printed );
//    }
//    catch ( IOException ex )
//    {
//      Exceptions.printStackTrace( ex );
//    }
  }
}
