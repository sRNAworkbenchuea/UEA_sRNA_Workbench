/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.patman;

import java.io.*;
import java.util.*;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;

/**
 * Manages results from the patman tool (Prufer et al. 2008.  Patman: rapid alignment
 * of short sequences to large databases. Bioinformatics, 24(13), 1530-1531).  Provides
 * client with the ability to process the results in a number of ways.
 * @author Dan Mapleson
 */
public class Patman extends ArrayList<PatmanEntry>
{
  /**
   * Creates an empty Patman object.
   */
  public Patman()
  {
    super();
  }

  /**
   * Splits up the current patman object into a series of smaller patman objects,
   * which are keyed by the chromosome (patman long read header).
   * @return Map of patman objects keyed by chromosome.
   */
  public Map<String, Patman> buildChromosomeMap()
  {
    HashMap<String, Patman> chr_map = new HashMap<String,Patman>();

    for ( int i = 0; i < this.size(); i++ )
    {
      PatmanEntry pe = this.get( i );
      String chromID = pe.getLongReadHeader();

      if ( !chr_map.containsKey( chromID ) )
      {
        Patman p = new Patman();
        p.add( pe );
        chr_map.put( chromID, p );
      }
      else
      {
        chr_map.get( chromID ).add( pe );
      }
    }

    return chr_map;
  }

  /**
   * Organises PatmanEntries in this object by number of mismatches.
   */
  public void sortByMismatches()
  {
    Collections.sort( this,
      new Comparator<PatmanEntry>()
      {
        @Override
        public int compare( PatmanEntry p1, PatmanEntry p2 )
        {
          return p1.getMismatches() - p2.getMismatches();
        }
      } );
  }

  /**
   * Organises PatmanEntries in this object by position: first sorts by chromosome
   * then by start position, then by strand.
   */
  public void sortByPosition()
  {
    Collections.sort( this,
      new Comparator<PatmanEntry>()
      {
        @Override
        public int compare( PatmanEntry p1, PatmanEntry p2 )
        {
          int chromosome_diff = p1.getLongReadHeader().compareTo( p2.getLongReadHeader() );
          if ( chromosome_diff == 0 )
          {
            int start_diff = p1.getStart() - p2.getStart();
            if ( start_diff == 0 )
            {
              return p1.getSequenceStrand().compareTo( p2.getSequenceStrand() );
            }
            else
            {
              return start_diff;
            }
          }
          else
          {
            return chromosome_diff;
          }
        }
      } );
  }

  /**
   * Organises PatmanEntries in this object by position: only uses start coordinate
   * in this case.
   */
  public void sortByStartCoord()
  {
    Collections.sort( this,
      new Comparator<PatmanEntry>()
      {
        @Override
        public int compare( PatmanEntry p1, PatmanEntry p2 )
        {
          return p1.getStart() - p2.getStart();
        }
      } );
  }

  /**
   * Organises PatmanEntries in this object by abundance in descending order.
   */
  public void sortByAbundance()
  {
    Collections.sort( this,
      new Comparator<PatmanEntry>()
      {
        @Override
        public int compare( PatmanEntry p1, PatmanEntry p2 )
        {
          return (int)p2.getAbundance() - (int)p1.getAbundance();
        }
      } );
  }

  /**
   * Discards matches that are identical apart from the number of mismatches present.
   * The hits with higher number of mismatches than the best score are discarded.
   */
  public void onlyKeepBestMatches()
  {
    if ( this.size() < 2 )
    {
      return;
    }

    this.sortByMismatches();

    int best = 0;
    int i = 0;

    for ( PatmanEntry pe : this )
    {
      int mm = pe.getMismatches();

      if ( i == 0 )
      {
        best = mm;
      }

      if ( mm > best )
      {
        this.removeRange( i, this.size() - 1 );
        return;
      }

      i++;
    }
  }

  /**
   * Returns a Map to the PatmanEntries in this object that are keyed by position.
   * The key is comprised as follows: "long_read_header,start,strand".
   * @return Map of patman entries keyed by position.
   */
  public Map<String, PatmanEntry> buildPositionKeyedMap()
  {
    HashMap<String, PatmanEntry> map = new HashMap<String, PatmanEntry>();

    for ( PatmanEntry pe : this )
    {
      // If the hit is on the negative strand, correct the start position.
      int corrected_start = pe.getStart();
      int corrected_end = pe.getEnd();

      String key = pe.getLongReadHeader() + "," + corrected_start + "," + pe.getSequenceStrand().getCharCode();

      map.put( key, new PatmanEntry( pe.getLongReadHeader(), pe.getSequence(), pe.getAbundance(), corrected_start, corrected_end, pe.getSequenceStrand(), pe.getMismatches() ) );
    }

    return map;
  }
  public void stripWeighted(float minConsider)
  {
    stripWeighted(buildFastaMap(), minConsider);
  }

  public void stripWeighted(FastaMap fastaMap, float minConsider)
  {
    Patman removeMe = new Patman();
    for ( PatmanEntry pe : this )
    {
      double abundance = pe.getAbundance();
      FastaRecord fr = fastaMap.get( pe.getSequence() );
      if ( fr != null )
      {
        float hitCount = (float)fr.getHitCount();
        double wabundance = abundance/hitCount;
        if(wabundance < minConsider)
        {
          removeMe.add( pe );
        }

      }
    }

    for(PatmanEntry toRemove : removeMe)
    {
      remove( toRemove);
    }
  }
  /**
   * Produces a FastaMap from a patman_file.  Hit locations are ignored.  Essentially,
   * this returns a subset of the data originally passed to Patman.
   * @return A fasta map containing sequences that hit to the target file
   */
  public FastaMap buildFastaMap()
  {
    FastaMap fm = new FastaMap();

    for ( PatmanEntry pe : this )
    {
      FastaRecord fr = new FastaRecord( pe.getSequence(), (int)pe.getAbundance() );

      FastaRecord val = fm.get( fr.getSequence() );
      
      
    


      if ( val == null )
      {
        fr.setHitCount( 1 );
        fm.put( fr.getSequence(), fr );
      }
      else
      {
        val.incHitCount();
      }
    }

    return fm;
  }

  /**
   * Produces a filtered fasta map of filtered sequences using from hits in this Patman
   * object.  Can select hits to be kept or discarded.  This code is multi-threaded.
   * Warning: this method does modify the unfiltered reads map.
   * @param unfiltered List of reads to be filtered
   * @param keep_matched Whether to keep reads that match hits and discard everything else, or vice versa
   * @param verbose For debugging
   * @return FastaMap filtered based on hits in this patman object
   * @throws Exception Thrown if there were any issues
   */
  public FastaMap filterByMatch( FastaMap unfiltered, boolean keep_matched, boolean verbose ) throws Exception
  {
    return filterByMatch( unfiltered, keep_matched, verbose, null, null, null, null );
  }

  /**
   * Produces a filtered fasta map of filtered sequences using from hits in this Patman
   * object.  Can select hits to be kept or discarded.  This code is multi-threaded.
   * Warning: this method does modify the unfiltered reads map.
   * @param unfiltered List of reads to be filtered
   * @param keep_matched Whether to keep reads that match hits and discard everything else, or vice versa
   * @param verbose For debugging
   * @param pw where to output discarded sequences
   * @param tracker Object used for monitoring progress
   * @param message prefix for any output produced
   * @return FastaMap filtered based on hits in this patman object
   * @throws Exception Thrown if there were any issues
   */
  public FastaMap filterByMatch( FastaMap unfiltered, boolean keep_matched, boolean verbose,
                                 PrintWriter pw, String filter, StatusTracker tracker, String message ) throws Exception
  {
    return new Matcher( unfiltered, this, keep_matched, verbose, pw, filter, tracker, message ).match();
  }

  /***
   * Get an identifier to frequency breakdown, i.e. the number of times the
   * identifier appears.
   *
   * @return    A map which links the identifier (the key) to the frequency (the value).<br>
   *            A null reference is never returned.
   */
  public Map< String, Integer> getIdToFrequencyBreakdown()
  {
    // Use a treemap so that it's sorted by key
    Map< String, Integer> result = new TreeMap< String, Integer>();

    for ( PatmanEntry pe : this )
    {
      Integer count = result.get( pe.getSequenceId() );

      // Initialise the counter if it's not there, otherwise increment it
      count = ( count == null ? 1 : count + 1 );

      result.put( pe.getSequenceId(), count );
    }

    return result;
  }

  /***
   * See {@link getLengthFrequencyBreakdown(String)} with a null string as the parameter.
   */
  public Map< Integer, Integer> getLengthToFrequencyBreakdown()
  {
    return getLengthToFrequencyBreakdown( "" );
  }

  /***
   * Get a sequence length to frequency breakdown, i.e. the number of times the
   * length appears. Specify an id to filter only on that id.
   *
   * @param id  The identifier matching {@link PatmanEntry#getID()}.
   *            Pass {@code null} or {@code ""} to get an indiscriminate
   *            breakdown for all identifiers.
   * @return    A map which links the length (the key) to the frequency (the value).<br>
   *            A null reference is never returned.
   */
  public Map< Integer, Integer> getLengthToFrequencyBreakdown( String id )
  {
    // Use a treemap so that it's sorted
    TreeMap< Integer, Integer> result = new TreeMap< Integer, Integer>();

    boolean includeAll = ( id == null || id.trim().length() == 0 );

    for ( PatmanEntry pe : this )
    {
      if ( !includeAll )
      {
        // Only interested in matching IDs
        if ( !id.equals( pe.getSequenceId() ) )
        {
          continue;
        }
      }

      Integer count = result.get( pe.getLength() );

      // Initialise the counter if it's not there, otherwise increment it
      count = ( count == null ? 1 : count + 1 );

      result.put( pe.getLength(), count );
    }

    return result;
  }

  /***
   * Get a sequence length to abundancy breakdown.
   *
   * @return Map of sequence lengths to total abundance
   */
  public Map<Integer, Double> getLengthToAbundanceBreakdown()
  {
    TreeMap<Integer, Double> result = new TreeMap<Integer, Double>();

    // Populate the map
    //
    for ( PatmanEntry pe : this )
    {
      Double abundance = result.get( pe.getLength() );

      // Initialise the total abundance if it's not there, otherwise increment it
      abundance = ( abundance == null ? 0 : abundance ) + pe.getAbundance();

      result.put( pe.getLength(), abundance );
    }

    return result;
  }

  public Patman performSizeClassReduction( String id, int length )
  {
    Set< Integer> lengths = new HashSet< Integer>();
    lengths.add( length );

    return performSizeClassReduction( id, lengths );
  }

  /**
   * Create a subset of PatmanEntry objects based on the provided criteria.<br/>
   * This Patman object may be returned if the criteria lead to no filtering.
   *
   * @param id The 'id' to match the result of {@link PatmanEntry#getID()}.
   *           All ids will be included if 'id' is null or a blank string.
   * @param requiredLengths A set of integers indicating the lengths on which to filter.
   *                        All lengths will be included if the set is null or empty.
   *
   * @return A Patman object which may be this Patman object or may be a newly created one.
   */
  public Patman performSizeClassReduction( String id, Set<Integer> requiredLengths )
  {
    // Include all the ids if 'id' is null or blank
    boolean includeAllIds = ( id == null || id.trim().length() == 0 );

    // Include all the lengths if the set of required lengths is null/empty...
    boolean includeAllLengths = ( requiredLengths == null || requiredLengths.isEmpty() );

    // All ids and lengths => this one will do !
    if ( includeAllIds && includeAllLengths )
    {
      return this;
    }

    //...or the set contains all the available lengths anyway
    Map< Integer, Integer> sc = getLengthToFrequencyBreakdown( id );

    if ( !includeAllLengths )
    {
      includeAllLengths = ( sc.keySet().equals( requiredLengths ) );
    }

    // All ids and lengths => this one will do !
    if ( includeAllIds && includeAllLengths )
    {
      return this;
    }

    // We need to filter on the id or lengths or both...

    Patman p = new Patman();

    int capacity = 0;

    if ( includeAllLengths )
    {
      // We are including all the lengths => we are filtering on id, so get
      // the id->frequency map
      Map< String, Integer> mid = getIdToFrequencyBreakdown();

      if ( mid.containsKey( id ) )
      {
        // we know the id's frequency => that's how big we need the list
        capacity = mid.get( id );
      }
      else
      {
        // id is not in the map => no entries => return empty map
        return p;
      }
    }
    else
    {
      for ( Integer i : requiredLengths )
      {
        if ( sc.containsKey( i ) )
        {
          capacity += sc.get( i );
        }
      }
    }
    p.ensureCapacity( capacity );

    // It's business time...

    // Add the appropriate PatmanEntry objects to the new one...

    for ( PatmanEntry pe : this )
    {
      if ( !includeAllIds )
      {
        // Only interested in matching IDs
        if ( !id.equals( pe.getSequenceId() ) )
        {
          continue;
        }
      }

      if ( !includeAllLengths )
      {
        // Only interested in sequences of certain lengths
        if ( !requiredLengths.contains( pe.getLength() ) )
        {
          continue;
        }
      }

      p.add( pe );
    }

    return p;
  }

  /**
   * Filter the Patman data using the given sequence identifier
   *
   * @param sequenceId
   *
   * @return A Patman object - it may be this or a newly created Patman object.
   */
  public Patman performSequenceIdReduction( String sequenceId )
  {
    return performSizeClassReduction( sequenceId, null );
  }

  public int getMaxEndCoord()
  {
    if ( !this.isEmpty() )
    {
      return this.get( this.size() - 1 ).getEnd();
    }

    return 0;
  }

  public int getMinStartCoord()
  {
    if ( !this.isEmpty() )
    {
      return this.get( 0 ).getStart();
    }

    return 0;
  }

  /***
   * Criterion used to decide which sequences to include in {@link performStartStopReduction}
   * when a sequence straddles the boundary of a given range.<br>
   * <br>
   * {@code NONE}      Ignore sequences which straddle a range boundary.<br>
   * {@code MAJORITY}  Include only those sequences with the majority of nucleotides in the range.<br>
   * {@code ALL}       Include all sequences that straddle the range boundary.
   */
  public enum StraddleInclusionCriterion
  {
    // no sequences which straddle an index are included
    NONE()
    {
      @Override
      public boolean include( int sequenceLength, int basesInRange )
      {
        return false;
      }
    },
    // only sequences with the majority of bases in the range are included
    MAJORITY()
    {
      @Override
      public boolean include( int sequenceLength, int basesInRange )
      {
        return 2 * basesInRange >= sequenceLength;
      }
    },
    // all sequences which straddle the range are included
    ALL()
    {
      @Override
      public boolean include( int sequenceLength, int basesInRange )
      {
        return true;
      }
    };

    /***
     * Whether to include the sequence based on its length and the number of bases in the range
     *
     * @param sequenceLength
     * @param basesInRange
     * @return boolean
     */
    public abstract boolean include( int sequenceLength, int basesInRange );
  }

  /***
   * Return a Patman collection for a given identifier where the sequences lie
   * inside a given index range. Sequences which partly fall outside of the range
   * can be included, partly included or excluded using {@code straddleRule}
   *
   * @param id            The text identifier of the governing sequence (null implies all ids)
   * @param start         Start index
   * @param stop          Stop index
   * @param straddleRule  What to do with sequences which partly fall outside {@code [start, stop]}.
   *                      ALL and NONE will include all or exclude all respectively.
   *                      MAJORITY will only include those sequences which have the majority of their
   *                      sequence inside the range.
   * @return Patman collection of PatmanEntry
   */
  public Patman performStartStopReduction( String id, int startIndex, int stopIndex, StraddleInclusionCriterion straddleRule )
  {
    // Include all the ids if 'id' is null or blank
    boolean includeAllIds = ( id == null || id.trim().length() == 0 );

    // NOTE: Could check here whether all entries are inside [start, stop] and 'return this' if so.
    //       However, we assume that work needs to be done, so we avoid checking and get on with it.

    Patman p = new Patman();

    // Add the appropriate PatmanEntry objects to the new one...

    for ( PatmanEntry pe : this )
    {
      if ( !includeAllIds )
      {
        // Only interested in matching IDs
        if ( !id.equals( pe.getSequenceId() ) )
        {
          continue;
        }
      }

      int numNt = pe.getLengthInRange( startIndex, stopIndex );

      if ( numNt == 0 )
      {
        continue;
      }

      if ( numNt < pe.getLength() )
      {
        // Sequence partially in range
        if ( !straddleRule.include( pe.getLength(), numNt ) )
        {
          continue;
        }
      }

      p.add( pe );
    }

    return p;
  }

  /**
   * Debug information
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Found " ).append( this.size() ).append( " chromosomes").append( LINE_SEPARATOR );
    int adding = 0;
    sb.append( "total number of small RNAS: " ).append( adding).append( LINE_SEPARATOR );
    return sb.toString();
  }

  /**
   * Saves the current patman object to the specified file.
   * @param out_file The file to save this patman object to.
   * @throws IOException Thrown if there were any problems writing the file.
   */
  public void save( File out_file ) throws IOException
  {
    PrintWriter pw = new PrintWriter( new BufferedWriter( new OutputStreamWriter( new FileOutputStream( out_file ))));

    final String TAB = "\t";

    for( PatmanEntry pe : this )
    {
      pw.print( pe.getLongReadHeader() );
      pw.print( TAB );
      pw.print( pe.getShortReadHeader() );
      pw.print( TAB );
      pw.print( pe.getStart() );
      pw.print( TAB );
      pw.print( pe.getEnd() );
      pw.print( TAB );
      pw.print( pe.getSequenceStrand().getCharCode() );
      pw.print( TAB );
      pw.print( pe.getMismatches() );
      pw.print( '\n' );
    }

    pw.flush();
    pw.close();
  }

  @Override
  public boolean contains( Object o )
  {
    PatmanEntry toTest = (PatmanEntry)o;
    for(PatmanEntry pe : this)
    {
      if( (pe.getEnd() == toTest.getEnd()) && (pe.getStart() == toTest.getStart()) )
      {
        return true;
      }
    }
    return false;
  }
  public Patman getRange( String header, int start, int end )
  {
    Patman p = new Patman();

    for( PatmanEntry pe : this )
    {
      if ( pe.getLongReadHeader().equals( header ) && pe.getStart() >= start && pe.getEnd() <= end )
      {
        p.add( pe );
      }
    }

    return p;
  }
  public Patman getRangeOverlap( String header, int start, int end )
  {
    Patman p = new Patman();

    //find the index to start searching from
//    PatmanEntry middleValue = this.get(this.size()/1);
//    if()
    
    Comparator<PatmanEntry> cStart = new Comparator<PatmanEntry>() {
      @Override
      public int compare(PatmanEntry u1, PatmanEntry u2) {
        //return u1.compareTo(u2.getId());
        Integer start1 = new Integer(u1.getStart());
        Integer start2 = new Integer(u2.getStart());
        return start1.compareTo( start2 );
      }
    };
    Comparator<PatmanEntry> cEnd = new Comparator<PatmanEntry>() {
      @Override
      public int compare(PatmanEntry u1, PatmanEntry u2) {
        //return u1.compareTo(u2.getId());
        Integer end1 = new Integer(u1.getEnd());
        Integer end2 = new Integer(u2.getEnd());
        return end1.compareTo( end2 );
      }
    };
    PatmanEntry startToFind = new PatmanEntry(null, null, 0, start, end,
                      null, 0);

    
    int index = Collections.binarySearch(this, startToFind, cStart);
    //System.out.println(index); 
    
    int endIndex = Collections.binarySearch(this, startToFind, cEnd);
    //System.out.println(Math.abs( endIndex )); 

    //for( PatmanEntry pe : this )
    for(int valueIndex = Math.max(0 ,Math.abs( index )-1) ; valueIndex < Math.abs( endIndex ); valueIndex++ )
    {
      PatmanEntry pe = this.get( valueIndex);

      float lengthOfSequence = pe.getEnd() - pe.getStart();
      if(pe.getLongReadHeader().equals( header ) && pe.getStart() < start && pe.getEnd() >= start)
      {
        
        float overlap = start - pe.getStart();
        float overlapPercentage = (float)overlap / lengthOfSequence;
        if(overlapPercentage <= 50)
        {
          p.add( pe );
        }
      }
      else if(pe.getLongReadHeader().equals( header ) && pe.getStart() <= end && pe.getEnd() > end)
      {

        float overlap = pe.getEnd() - end;
        float overlapPercentage = (float)overlap /lengthOfSequence;
        if(overlapPercentage <= 50)
        {
          p.add( pe );
        }
      }
      else if ( pe.getLongReadHeader().equals( header ) && pe.getStart() >= start && pe.getEnd() <= end )
      {
        p.add( pe );
      }
    }

    return p;
  }

  /**
   * Sets the relative height of each PatmanEntry for use by VisSR.
   * The height is calculated using the formula:
   *
   *   height_scale = 100 * ( OFFSET + abundance_of_patman_entry ) / ( OFFSET + max_abundance_of_all_patman_entries )
   *
   * so that 0 < height_scale <= 100.
   * OFFSET is set to 20.
   */
  public void setGlyphHeightFromAbundance()
  {
    int maxAbundance = getMaxAbundance();

    setGlyphHeights( maxAbundance );
  }
  
  /**
   * Sets the relative height of each PatmanEntry for use by VisSR.
   * The user specifies a maximumAbundance, which may be different from the
   * maximum abundance of any entry in this collection.
   * The height is calculated using the formula:
   * 
   * height_scale = 100 * ( OFFSET + abundance_of_patman_entry ) / ( OFFSET + max_abundance_specified_by_user )
   *
   * so that 0 < height_scale <= 100.
   * OFFSET is set to 20.
   * 
   * @param max_value The maximum abundance (may not be the maximum abundance of any entry in
   * this collection.)
   */
  public void setGlyphHeights( int maxAbundance )
  {
    // Compute the relative-offset-abundance
    //
    final int OFFSET = 20;
    final float FACTOR = 100f / ( OFFSET + maxAbundance );

    float scale;

    for ( PatmanEntry pe : this )
    {
      scale = FACTOR * ( OFFSET + (float) pe.getAbundance() );
      pe.setGlyphHeightScalePercent( scale );
    }
  }
  
  /**
   * Finds the maximum abundance of any patman entry in the collection
   * @return The maximum abundance of any entry in this patman object.
   */
  public int getMaxAbundance()
  {    
    int maxAbundance = 1;
    for ( PatmanEntry pe : this )
    {
      maxAbundance = Math.max( maxAbundance, (int)pe.getAbundance() );
    }
    
    return maxAbundance;
  }
}
