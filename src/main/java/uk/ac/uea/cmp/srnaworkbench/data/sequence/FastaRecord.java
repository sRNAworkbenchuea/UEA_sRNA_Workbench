/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.io.Serializable;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * Represent a FASTA sequence.
 *
 * @author prb07qmu
 */
public final class FastaRecord extends SequenceRange implements Serializable
{
  private final String _fastaHeader;  // the first line of the record without the leading '>'
  private int _hitCount = 1;
  /**
   * Abundance is used by some of the sRNA FASTA data.
   */
  private double _abundance = Double.MIN_VALUE;

  /***
   * Constructor - for use with short reads where the Fasta header is irrelevant and the abundance is known
   *
   * @param sequence The nucleotide sequence
   * @param abundance The abundance of the sequence
   */
  public FastaRecord( String sequence, double abundance )
  {
    this( sequence, null, sequence );
    setAbundance( abundance );
  }

  /***
   * Constructor
   *
   * @param id The text identifier - usually set to the first word after the '>' from the FASTA file.
   * @param description A description of the sequence
   * @param sequence The nucleotide sequence
   */
  public FastaRecord( String sequenceId, String fastaHeader, String sequence )
  {
    super( 0, sequence.length() - 1, SequenceStrand.UNKNOWN, sequenceId, sequence );

    if ( sequenceId == null || sequenceId.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "The 'sequenceId' must be specified" );
    }

    if ( getSequence().length() == 0 )
    {
      throw new IllegalArgumentException( "The 'sequence' must be specified" );
    }

    _fastaHeader = ( fastaHeader == null ? "" : fastaHeader );
  }

  /**
   * Create a new FastaRecord from a PatmanEntry.  Sets sequence and abundance to
   * what it found in this PatmanEntry and starts the hit count at one, because
   * presumably this is the first time this sequence has been encountered.
   * @param pe PatmanEntry containing the sRNA hit to a mature miRNA
   */
  public FastaRecord( PatmanEntry pe )
  {
    this( pe.getSequence() + "(" + pe.getAbundance() + ")", pe.getLongReadHeader(), pe.getSequence() );
    this.setAbundance( pe.getAbundance() );
    this.setHitCount( 1 );
  }

  /**
   * Returns a sequence that starts on "start" 1-based and ends at "end" 1-based.
   * For example if start=3 and end=6, sequence will be GCTT from:
   *    0-1-2-3-4-5-6  0-based
   *    1-2-3-4-5-6-7  1-based
   *    A-T-G-C-T-T-T
   *        ^     ^
   *        start end
   * 
   * with a length of (end - start) + 1 = 4
   * @param start
   * @param end
   * @param strand
   * @return 
   */
  public String getSubSequenceAsString( int start, int end, SequenceStrand strand )
  {
    String newSubSeq = "";
    if ( strand == SequenceStrand.POSITIVE )
    {
      // to convert to substring indexing, start is decremented to 0-based and end
      // is kept the same because everything BEFORE end is returned
      newSubSeq = this.getSequence().substring( start - 1, end );
    }
    else if ( strand == SequenceStrand.NEGATIVE )
    {
      newSubSeq = SequenceUtils.DNA.reverseComplement( this.getSequence().substring( start - 1, end ) );
    }
    else
    {
      return null;
    }
    
    return newSubSeq;
  }

  
  public void setAbundance( double abundance )
  {
    _abundance = abundance;
  }

  public double incrementAbundance( double increment )
  {
    _abundance += increment;
    return _abundance;
  }

  public int getAbundance()
  {
    return (int)_abundance;
  }
  
  public double getRealAbundance()
  {
    return _abundance;
  }

  public int getHitCount()
  {
    return _hitCount;
  }

  public void setHitCount( int hit_count )
  {
    _hitCount = hit_count;
  }

  public int incHitCount()
  {
    _hitCount++;
    return _hitCount;
  }

  public String getFastaHeader()
  {
    return _fastaHeader;
  }

  @Override
  public String toString()
  {
    return StringUtils.nullSafeConcatenation( "FastaRecord{ ", super.toString(), ", Fasta header='", _fastaHeader, "'}" );
  }
}
