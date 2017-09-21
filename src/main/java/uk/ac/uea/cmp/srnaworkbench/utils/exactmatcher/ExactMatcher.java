
package uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceRangeI;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.io.FastaFileReader;
import uk.ac.uea.cmp.srnaworkbench.utils.KeyCounter;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Matches short reads against long reads using exact matching.
 *
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public final class ExactMatcher
{
  /**The ID of the longer sequence**/
  private String geneID;

  /**Start position in a longer sequence. **/
  private int startPosition;

  /**The results from the alignment process.**/
  private final List< ExactMatch > collectedResults = new ArrayList< ExactMatch >();

  /**Abundance information. **/
  private final KeyCounter< String > abundanceCounter = new KeyCounter< String >();

  /**Hash Map containing all non-redundant transcripts read in from the transcriptome.**/
  private Map< String, String > longReadsMap = null;

  /**Count of short read alignments. (The number of positions each tag has mapped to transcripts).**/
  private final KeyCounter< String > shortReadAlignmentCounter = KeyCounter.createKeyCounter();

  /**The root node of the exact matcher tree.**/
  private Node tree;

  /** Redundant reads size distribution */
  private final KeyCounter< Integer > redundantReadSizeDistribution = KeyCounter.createKeyCounter();

  /** Non-redundant reads size distribution */
  private final KeyCounter< Integer > nonRedundantReadSizeDistribution = KeyCounter.createKeyCounter();

  /** Verbosity flag */
  private final boolean _verbose;


  /**
   * Default constructor
   */
  public ExactMatcher()
  {
    this( false );
  }

  /**
   * Constructor
   * @param verbose Informational messages will be output when a true value is provided
   */
  public ExactMatcher( boolean verbose )
  {
    _verbose = verbose;
  }

  /**
   * Clear the collections to reclaim space
   */
  public void clear()
  {
    abundanceCounter.clear();
    collectedResults.clear();
    nonRedundantReadSizeDistribution.clear();
    redundantReadSizeDistribution.clear();
    shortReadAlignmentCounter.clear();

    if ( longReadsMap != null )
      longReadsMap.clear();

    tree = null;
  }

  public String getGeneID()
  {
    return geneID;
  }

  public int getStartPosition()
  {
    return startPosition;
  }

  public int getRedundantReadCount()
  {
    return redundantReadSizeDistribution.getTotalCount();
  }

  public int getNonRedundantReadCount()
  {
    return nonRedundantReadSizeDistribution.getTotalCount();
  }

  public int getTotalUniqueHits()
  {
    return shortReadAlignmentCounter.size();
  }

  /**
   * Get the total number of long reads read in from file by the exact matcher.
   * @return Total non-redundant long reads (transcripts).
   */
  public int getTotalLongReads()
  {
    if ( longReadsMap == null ) return 0;
    return longReadsMap.size();
  }

  /**
   * Get the frequency distribution of the redundant reads
   * @return Map
   */
  public Map< Integer, Integer > getRedundantReadSizeDistribution()
  {
    return Collections.unmodifiableMap( redundantReadSizeDistribution.createKeyIntegerMap() );
  }

  /**
   * Get the frequency distribution of the non-redundant reads
   * @return Map
   */
  public Map< Integer, Integer > getNonRedundantReadSizeDistribution()
  {
    return Collections.unmodifiableMap( nonRedundantReadSizeDistribution.createKeyIntegerMap() );
  }

  /**
   * Get the short read abundance information.
   * @return Short read abundances.
   */
  public Map<String, Integer> getShortReadAbundances()
  {
    return Collections.unmodifiableMap( abundanceCounter.createKeyIntegerMap() );
  }

  /**
   * Get the long reads.
   * @return Long reads map.
   */
  public Map<String, String> getLongReadsMap()
  {
    if ( longReadsMap == null ) return Collections.emptyMap();
    return Collections.unmodifiableMap( longReadsMap );
  }

  /**
   * Get the short read alignment counts.
   * @return Short read alignment counts.
   */
  public Map<String, Integer> getShortReadAlignmentCounts()
  {
    return Collections.unmodifiableMap( shortReadAlignmentCounter.createKeyIntegerMap() );
  }

  /**
   * Find all positions where any short read in the collection of short reads matches exactly
   * any region of any long read in the collection of long reads (+ strand is assumed).
   * Each alignment found is added to a collection of alignments and returned in a list.
   *
   * @param shortSequences    File of short sequences in FASTA format
   * @param longSequences     File of long sequences in FASTA format
   * @param minLength         Minimum allowable sequence length
   * @param maxLength         Maximum allowable sequence length
   *
   * @return ArrayList<String> such that each element in the array list = a match.
   */
  public List< ExactMatch > findExactMatches( File shortSequences, File longSequences, int minLength, int maxLength ) throws WorkbenchException
  {
    try
    {
      constructTree( shortSequences, minLength, maxLength );
      alignToTree( longSequences );
    }
    catch ( OutOfMemoryError err )
    {
      clear();
      System.gc();

      throw new WorkbenchException( "An out of memory error occurred in ExactMatcher", err );
    }

    if ( _verbose )
    {
      System.out.println( "--Total exact matches = "+ collectedResults.size() + " ---------------------------------------" );

      for ( ExactMatch em : collectedResults )
      {
        em.print( System.out );
      }
    }

    return Collections.unmodifiableList( collectedResults );
  }

  /**
   * Construct a 4-way tree from short sequences.
   *
   * @param shortSequencesFile A file of sequences in FASTA format
   * @param minLength Minimum acceptable sequence length
   * @param maxLength Maximum acceptable sequence length
   */
  private void constructTree( File shortSequencesFile, int minLength, int maxLength ) throws WorkbenchException
  {
    // NOTE: The sequences from the file are not kept - both SRNAFileReader and FastaFileReader perform about the same.
    //       Memory about the same too (provided the latter uses the StringPool).
    //       However, since the sequences are thrown away, the memory footprint could be further reduced by getting chunks from the file.
    // TODO: Adapt FastaFileReader to return batches of a specified amount.
    //       Use Filter tool for the filtering step

    // Flags to see if we filter out every sequence
    // i.e. we filter on length and on whether the sequence contains 'N'
    //
    boolean lengthAllFilteredOut = true;
    boolean nNtAllFilteredOut = true;

    // Process the file
    //
    FastaFileReader ffr = new FastaFileReader( shortSequencesFile );

    final boolean DO_CHUNKS = false;

    if ( DO_CHUNKS )
    {
      while( ! ffr.allChunksConsumed() )
      {
        boolean readFileOK = ffr.processChunk();

        if ( ! readFileOK )
        {
          System.err.println( "Something went wrong when reading the file " + shortSequencesFile + ". Check the file location and its format." );
          throw new WorkbenchException( "The file " + shortSequencesFile.getName() + " could not be read and/or processed." );
        }

        List< SequenceRangeI > seqs = ffr.getFastaRecords();

        // Filter the sequences
        //
        for ( SequenceRangeI sr : seqs )
        {
          int seqLength = sr.getSequenceLength();

          // Is the length OK ?
          if ( minLength <= seqLength && seqLength <= maxLength )
          {
            lengthAllFilteredOut = false;
          }
          else
          {
            continue;
          }

          // The 'N' nucleotide is not allowed - does the sequence contain one ?
          //if ( sr.getSequence().contains( "N" ) )
          if ( sr.getSequence().contains( "N" ) )
          {
            continue;
          }
          else
          {
            nNtAllFilteredOut = false;
          }

          redundantReadSizeDistribution.incrementCounter( sr.getSequenceLength(), 1 );

          // Add the sequence to the tree.
          //
          char[] seq = sr.getSequence().toCharArray();

          if ( tree == null )
          {
            tree = new Node( seq, 0, this );
          }
          else
          {
            tree.addToTree( seq, this );
          }
        }
      }

      System.gc();
    }
    else
    {
      boolean readFileOK = ffr.processFile();

      if ( ! readFileOK )
      {
        System.err.println( "Something went wrong when reading the file " + shortSequencesFile + ". Check the file location and its format." );
        throw new WorkbenchException( "The file " + shortSequencesFile.getName() + " could not be read and/or processed." );
      }

      // Filter the sequences
      //
      for ( SequenceRangeI sr : ffr.getFastaRecords() )
      {
        int seqLength = sr.getSequenceLength();

        // Is the length OK ?
        if ( minLength <= seqLength && seqLength <= maxLength )
        {
          lengthAllFilteredOut = false;
        }
        else
        {
          continue;
        }

        // The 'N' nucleotide is not allowed - does the sequence contain one ?
        //if ( sr.getSequence().contains( "N" ) )
        if ( sr.getSequence().contains( "N" ) )
        {
          continue;
        }
        else
        {
          nNtAllFilteredOut = false;
        }

        redundantReadSizeDistribution.incrementCounter( sr.getSequenceLength(), 1 );

        // Add the sequence to the tree.
        //
        char[] seq = sr.getSequence().toCharArray();

        if ( tree == null )
        {
          tree = new Node( seq, 0, this );
        }
        else
        {
          tree.addToTree( seq, this );
        }
      }
    }

    if ( tree == null )
    {
      if ( lengthAllFilteredOut )
      {
        String message = "All sequences were removed from '" + shortSequencesFile.getName() + "' during length filtering" + LINE_SEPARATOR +
          "Min. filter length was " + minLength + LINE_SEPARATOR +
          "Max. filter length was " + maxLength;

        throw new WorkbenchException( message );
      }

      if ( nNtAllFilteredOut )
      {
        throw new WorkbenchException( "All sequences were removed from '" + shortSequencesFile.getName() +
          "' because they all contained an 'N' nucleotide." );
      }
    }
  }

  /**
   * Align each of the query sequences to the reference sequences using exact match criteria.
   * @param file The long reads.
   */
  private void alignToTree( File file )
  {
    // Read in the FASTA file
    //
    FastaFileReader ffr = new FastaFileReader( file );
    ffr.processFile();

    longReadsMap = ffr.getSequenceIdToSequenceMap( true );

    for ( Map.Entry< String, String > e : longReadsMap.entrySet() )
    {
      geneID = e.getKey();
      char[] sequence = e.getValue().toCharArray();

      // Align the sequence to the tree incrementing the starting position.
      for ( int j = 0; j < sequence.length; j++ )
      {
        startPosition = j;
        tree.align( sequence, j, this );
      }
    }
  }

  public void incrementShortReadAbundance( String sequence )
  {
    if ( ! abundanceCounter.containsKey( sequence ) )
    {
      // Non-redundant : first time a given sequence has been seen so update the size distribution
      nonRedundantReadSizeDistribution.incrementCounter( sequence.length(), 1 );
    }

    abundanceCounter.incrementCounter( sequence, 1 );
  }

  public void incrementShortReadAlignment( String sequence )
  {
    shortReadAlignmentCounter.incrementCounter( sequence, 1 );
  }

  public void addResult( String geneID, String sequence, int startPosition, int endPosition )
  {
    collectedResults.add( new ExactMatch( geneID, sequence, startPosition, endPosition) );
  }

  /**
   * Helper class to store the result of an exact match
   */
  public final static class ExactMatch
  {
    /*
     * Note: The data in Patman format would be (with the spaces removed):
     * "gene-id \t short-sequence-nucleotides \t 5'-alignment-start-position \t 3'-alignment-end-position \t + \t 0"
     * i.e. "_geneId \t _sequence \t _startPosition \t _endPosition \t + \t 0"
     */

    private final String _geneId;
    private final String _sequence;
    private final int _startPosition;   // 5'-alignment-start-position
    private final int _endPosition;     // 3'-alignment-end-position

    public ExactMatch( String geneId, String sequence, int startPosition, int endPosition )
    {
      _geneId = geneId;
      _sequence = sequence;
      _startPosition = startPosition;
      _endPosition = endPosition;
    }

    public String getGeneId()     { return _geneId; }
    public String getSequence()   { return _sequence; }
    public int getStartPosition() { return _startPosition; }
    public int getEndPosition()   { return _endPosition; }

    @Override
    public String toString()
    {
      return "ExactMatch{Sequence: " + _sequence + ", start: " + _startPosition + ", end: " + _endPosition + ", gene id: " + _geneId;
    }

    private void print( PrintStream out )
    {
        out.print( "Sequence: " );  out.print( _sequence );
        out.print( ", start: " );   out.print( _startPosition );
        out.print( ", end: " );     out.print( _endPosition );
        out.print( ", gene id: " ); out.print( _geneId );
        out.println();
    }
  }
}
