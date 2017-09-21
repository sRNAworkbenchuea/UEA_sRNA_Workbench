/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeI;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringPool;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;


/***
 * Read in FASTA files and construct a list of {@link FastaRecord} objects.
 *
 * @author prb07qmu
 */
public final class FastaFileReader
{
  private static final int CHUNK_SIZE = 100000;

  private final File _fastaFile;
  private final boolean _lookForAbundanceInSequenceId;
  private final StringBuilder _sb = new StringBuilder();
  private final ArrayList< SequenceRangeI > _fastaRecords = CollectionUtils.newArrayList();
  private final StringPool _stringPool = new StringPool();

  // Member variables related to chunking
  private boolean _allChunksConsumed;
  private BufferedReader _chunkReader;
  private String _lastLine;

  /***
   * Constructor
   *
   * @param fastaFile The FASTA formatted {@code File} object
   */
  public FastaFileReader( File fastaFile )
  {
    this( fastaFile, false );
  }

  public FastaFileReader( File fastaFile, boolean lookForAbundanceInSequenceId )
  {
    _fastaFile = fastaFile;
    _lookForAbundanceInSequenceId = lookForAbundanceInSequenceId;

    // Used to reset chunk reader
    reset();
  }


  /**
   * Processes the specified fasta file in chunks in order to limit memory usage.
   * Use the allChunksConsumed() method to determine if all chunks have been read.
   * Use the getFastaRecords() method to access the records for the processed chunk.
   * @return Success or failure
   */
  public boolean processChunk()
  {
    if ( _chunkReader == null )
      return false;

    // Initialise capacity for reading in the sequence(s)
    _sb.ensureCapacity( 2 << 20 );

    // Read the file...
    String line = "";

    try
    {
      int i = 0;

      // Clear down any records that were stored before.
      _fastaRecords.clear();
      _stringPool.clear();
      System.gc();

      // Gets the last line read from the last chunk if necessary.
      if ( !_lastLine.isEmpty() )
      {
        line = _lastLine;
      }

      // Read records from file until end of file or chunk has been filled.
      while ( line != null && i <= CHUNK_SIZE )
      {
        line = ( line.startsWith( ">" ) ? processRecord( _chunkReader, line ) : _chunkReader.readLine() );
        i++;
      }

      // Check for end of file
      if ( (i == CHUNK_SIZE+1 && line == null) || i <= CHUNK_SIZE )
      {
        // Reached end of file
        IOUtils.closeQuietly( _chunkReader );
        _chunkReader = null;
        _allChunksConsumed = true;
      }

      // Record the last line processed for the next chunk.
      _lastLine = line;
    }
    catch ( IOException e )
    {
      _fastaRecords.clear();

      LOGGER.log( Level.SEVERE, null, e );

      IOUtils.closeQuietly( _chunkReader );
      _chunkReader = null;

      return false;
    }
    finally
    {
      _sb.delete( 0, _sb.length() );
    }

    return true;
  }

  /**
   * Returns true if all chunks have been consumed by the processChunk method.
   * @return true if all chunks have been read, otherwise false.
   */
  public boolean allChunksConsumed()
  {
    return _allChunksConsumed;
  }

  /**
   * Resets the chunk reader so that file can be re-read.
   */
  public void reset()
  {
    _allChunksConsumed = false;
    _chunkReader = FileUtils.createBufferedReader( _fastaFile );
    _lastLine = "";
    _stringPool.clear();
  }

  /***
   * Read in the sequence id line and the sequences from the file.
   *
   * @return Success or failure
   * @throws IOException
   */
  public boolean processFile()
  {
    BufferedReader br = FileUtils.createBufferedReader( _fastaFile );

    if ( br == null )
      return false;

    // Initialise capacity for reading in the sequence(s)
    //
    _sb.ensureCapacity( 2 << 20 );

    // Read the file...
    //
    String line = "";

    try
    {
      while ( line != null )
      {
        line = ( line.startsWith( ">" ) ? processRecord( br, line ) : br.readLine() );
      }
    }
    catch ( IOException e )
    {
      _fastaRecords.clear();

      LOGGER.log( Level.SEVERE, null, e );
      return false;
    }
    finally
    {
      _sb.delete( 0, _sb.length() );

      IOUtils.closeQuietly( br );
      br = null;
    }

    return true;
  }

  /**
   * Get the FASTA records
   * @return A list of {@link FastaRecord} objects (guaranteed not to be null)
   */
  public List< SequenceRangeI > getFastaRecords()
  {
    return _fastaRecords;
  }

  /**
   * Return a {@link FastaMap} linking sequence identifiers to the {@link FastaRecord} objects
   * @return A {@link FastaMap}
   */
  public FastaMap getFastaMap( boolean useFullFastaDescription )
  {
    FastaMap fm = new FastaMap();

    if ( _fastaRecords == null || _fastaRecords.isEmpty() )
      return fm;

    for ( SequenceRangeI sr : _fastaRecords )
    {
      String key = ( useFullFastaDescription ? ((FastaRecord)sr).getFastaHeader() : sr.getSequenceId() );
      fm.put( key, (FastaRecord)sr );
    }

    return fm;
  }

  /**
   * Convert the FASTA records into a Map where the key is the sequence id and the value is the sequence.
   * @return The sequence id to sequence Map
   */
  public Map< String, String > getSequenceIdToSequenceMap( boolean useFullFastaDescription )
  {
    Map< String, String > result = CollectionUtils.newHashMap( _fastaRecords.size() );

    for ( SequenceRangeI sr : _fastaRecords )
    {
      String key = ( useFullFastaDescription ? ((FastaRecord)sr).getFastaHeader() : sr.getSequenceId() );
      result.put( key, sr.getSequence() );
    }

    return result;
  }

  /***
   * Process a record (which starts with a '>')
   *
   * @param br The {@code BufferedReader}
   * @param origLine The text of the first line (where the first character is '>')
   * @return The text of the next record which starts with a '>' (could be null)
   * @throws IOException
   */
  private String processRecord( BufferedReader br, String origLine ) throws IOException
  {
    /*
     * Line might be:
     *
     * >Chr1 dumped from ADB...
     * or
     * >TGGCGTGTCTTTGAAGGGAAATA(2)
     * etc.
     *
     * Want to pick out 'Chr1', 'TGGCGTGTCTTTGAAGGGAAATA' and '2'
     */

    String line = origLine;

    String sequenceId = "";
    String fastaHeader = origLine.substring( 1 ).trim(); // Remove the '>'
    String abundanceToken = "";

    if ( _lookForAbundanceInSequenceId )
    {
      /*
       * If we are looking for the abundance in the id line then we assume the line's format is
       *
       * >TGGCGTGTCTTTGAAGGGAAATA(2)
       *
       * for example. The id is 'TGGCGTGTCTTTGAAGGGAAATA' and the abundance is 2.
       * The sequence is on the next line.
       */

      boolean setToken1 = false;
      boolean setToken2 = false;

      StringTokenizer st = new StringTokenizer( line, ">() ", true );

      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken();

        if ( ! setToken1 && ! token.isEmpty() && '>' == token.charAt( 0 ) )
        {
          sequenceId = st.nextToken();
          setToken1 = true;
        }

        if ( ! setToken2 && ! token.isEmpty() && '(' == token.charAt( 0 ) )
        {
          abundanceToken = st.nextToken();
          setToken2 = true;
        }
      }
    }
    else
    {
      // The format of the description line is not standardised.
      // We take the id to be the text between the '>' in the position of the first space.

      int spacePos = fastaHeader.indexOf( " " );

      sequenceId = fastaHeader;//( spacePos == -1 ? fastaHeader : fastaHeader.substring( 0, spacePos ) );
    }

    // Assume the next line is going to be there !

    while ( ( line = br.readLine() ) != null )
    {
      if ( line.isEmpty() )
        break;

      if ( '>' == line.charAt( 0 ) )
        break;

      _sb.append( line.trim() );
    }

    // Convert to upper case

    for ( int i = 0, N = _sb.length(); i < N; ++i )
    {
      char c = _sb.charAt( i );
      if ( Character.isLowerCase( c ) )
      {
        _sb.setCharAt( i, Character.toUpperCase( c ) );
      }
    }

    String sequence = _sb.toString(); //.toUpperCase();

    // Use the string pool for shortish strings

    if ( sequence.length() < 100 )
    {
      sequence = _stringPool.getString( sequence );
    }

    _sb.delete( 0, _sb.length() );

    //replace any U with T
    sequence.replace( 'U', 'T' );
    FastaRecord ffr = new FastaRecord( sequenceId, fastaHeader, sequence );

    // Take care of the abundance if it was included
    //
    if ( abundanceToken != null )
    {
      final int DEFAULT = Integer.MIN_VALUE;

      int abundance = StringUtils.safeIntegerParse( abundanceToken, DEFAULT );

      if ( abundance != DEFAULT )
      {
        ffr.setAbundance( abundance );
      }
    }

    _fastaRecords.add( ffr );

    return line;
  }

  //****************************************************************************

  public static void main( String[] args ) throws WorkbenchException
  {
    String fileName = null;
    //fileName = "D:/LocalData/hugh/seq_data/plant/Ath_TAIR9.Chr1.fa";
    //fileName = "D:/LocalData/hugh/seq_data/plant/Ath_TAIR9.fa";
    //fileName = "D:/LocalData/hugh/seq_data/irina_data/Organs/GSM118372.fa";
    //fileName = "D:/LocalData/hugh/seq_data/test/test.fa";
    //fileName = "D:/LocalData/hugh/seq_data/test/test2.fa";
    //fileName = "D:/seq_data/drosophila_melanogaster/srnaome/s2_adaptors_removed.fa";
    fileName = "D:/Research/RNATools/fastaReader/chunk9.fa";

    FastaFileReader ffr = new FastaFileReader( new File( fileName ) );

    final boolean CHUNK = true;

    if ( CHUNK )
    {
      List< SequenceRangeI > all_seqs = CollectionUtils.newArrayList();

      int i = 0;
      while( !ffr.allChunksConsumed() )
      {
        //List< SequenceRangeI > seqs = ffr.processChunk();
        boolean ok = ffr.processChunk();
        List< SequenceRangeI > seqs = ffr.getFastaRecords();

        if ( seqs != null )
        {
          all_seqs.addAll( seqs );
        }
        else
        {
          throw new WorkbenchException( "Argh, error occurred." );
        }
        i++;
      }

      LOGGER.log( Level.INFO, "Processed {0} records in {1} chunks.", new Object[]{ all_seqs.size(), i });
    }
    else
    {
      boolean ok = ffr.processFile();

      if ( ok )
      {
        List< SequenceRangeI > seqs = ffr.getFastaRecords();

        Map< String, SequenceRangeI > map = SequenceRangeList.getIdToSequenceRangeMap( seqs, false );

        LOGGER.log( Level.INFO, "List size = {0}", seqs.size());
        LOGGER.log( Level.INFO, " Map size = {0}", map.size());
        LOGGER.log( Level.INFO, "Ids are {0}", ( seqs.size() == map.size() ? "unique" : "not unique" ));

        FastaMap fm = ffr.getFastaMap( true );

        for ( Map.Entry<String,FastaRecord> ent : fm.entrySet() )
        {
          //LOGGER.log( Level.INFO, "Seq id: " + ent.getKey() + ", seq length : " + ent.getValue().toString() + ", Header : " + ent.getValue().getFastaHeader() );
          LOGGER.log( Level.INFO, "Sequence id: {0}, FASTA header : {1}", new Object[]{ ent.getValue().getSequenceId(), ent.getValue().getFastaHeader() });
        }
      }
      else
      {
        LOGGER.log( Level.SEVERE, "Argh, error occurred." );
      }
    }
  }
}
