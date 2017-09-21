/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.util.*;
import java.util.logging.Level;

/**
 * Base class for objects which model various sequence ranges, e.g. FASTA records, GFF record, etc.
 * Or something more general like a window onto a governing sequence.
 *
 * @author prb07qmu
 */
public class SequenceRange implements SequenceRangeI
{
  /*
   * A note about indices:
   *
   * _startIndex and _endIndex are zero-based indices - base positions are found by adding 1
   *
   * The sequence   'AGCTAGCT', has
   * indices         01234567, and
   * base positions  12345678
   *
   * Alternatively, on the number line :         0-1-2-3-4-5-6-7-8
   * The bases would be placed         :          A G C T A G C T
   * which is how VisSR displays it.
   *
   * Indices are used for extracting substrings, e.g. "AGCTAGCT".substring( 1, 3 ) gives GC
   */
  private int _startIndex;
  private int _endIndex;

  private SequenceStrand _strand;
  private String _sequenceId;
  private SequenceRangeI _parentSequenceRange;
  private String _sequence;

  // Set the default scaling to be 50% of the available height
  private float _glyphHeightScalePercent = 50f;

  private Map<String,String> _attributesMap = null;

  // Constructors

  public SequenceRange( int startIndex, int endIndex )
  {
    this( startIndex, endIndex, SequenceStrand.UNKNOWN, "", "", null );
  }

  public SequenceRange( int startIndex, int endIndex, SequenceStrand strand )
  {
    this( startIndex, endIndex, strand, "", "", null );
  }

  public SequenceRange( int startIndex, int endIndex, SequenceStrand strand, String id )
  {
    this( startIndex, endIndex, strand, id, "", null );
  }

  public SequenceRange( int startIndex, int endIndex, SequenceStrand strand, String id, String sequence )
  {
    this( startIndex, endIndex, strand, id, sequence, null );
  }

  /***
   * Constructor - called by the other constructors with default values
   *
   * @param start      The start index
   * @param end        The end index
   * @param strand     The strandedness of the sequence, if any.
   * @param id         A string identifier
   * @param sequence   The sequence (nucleotide bases - may be blank)
   * @param parentId   The identifier of the parent (e.g. the chromosome id of a GFF record
   */
  public SequenceRange( int startIndex, int endIndex, SequenceStrand strand, String id, String sequence, SequenceRangeI parentSequenceRange )
  {
    _startIndex = startIndex;
    _endIndex   = endIndex;
    _strand     = strand;
    _sequenceId = ( id == null ? "" : id );
    _parentSequenceRange = parentSequenceRange;
    _sequence   = ( sequence == null ? "" : sequence );

    // Check the lengths match (only want to do this if the sequence is set (sometimes it is not)
    if ( _sequence.length() > 0 )
    {
      int len = _endIndex - _startIndex + 1;

      if ( _sequence.length() != len )
      {
        WorkbenchLogger.LOGGER.log( Level.WARNING, "** _sequence.length() : {0} != {1} : (end-start+1) **", new Object[]{ _sequence.length(), len });
        throw new IllegalStateException( "Sequence lengths do not match (sequence.length() != end-start+1) !" );
      }
    }
  }

  // Start of SequenceRangeI implementation...
  //
  /**
   * {@inheritDoc}
   */
  @Override
  public final int getStartIndex()
  {
    return _startIndex;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getEndIndex()
  {
    return _endIndex;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getSequence()
  {
    return _sequence == null ? "" : _sequence;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getSequenceLength()
  {
    return _endIndex - _startIndex + 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final SequenceStrand getSequenceStrand()
  {
    return _strand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getSequenceId()
  {
    return _sequenceId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final SequenceRangeI getParentSequenceRangeI()
  {
    return _parentSequenceRange;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public float getGlyphHeightScalePercent()
  {
    return _glyphHeightScalePercent;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setGlyphHeightScalePercent( float pc )
  {
    _glyphHeightScalePercent = Math.min( 100f, Math.max( 1f, pc ) );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String,String> getAttributes()
  {
    if ( _attributesMap == null )
    {
      return Collections.emptyMap();
    }
    else
    {
      return Collections.unmodifiableMap( _attributesMap );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addAttribute( String key, String value )
  {
    if ( key == null || value == null )
      return;

    if ( _attributesMap == null )
    {
      _attributesMap = CollectionUtils.newHashMap();
    }

    _attributesMap.put( key, value );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAttribute( String key )
  {
    if ( _attributesMap == null )
    {
      return null;
    }

    return _attributesMap.get( key );
  }
  //
  // End of SequenceRangeI implementation


  public final void setParentSequenceI( SequenceRangeI parentSequenceRange )
  {
    _parentSequenceRange = parentSequenceRange;
  }

  // Base positions are 1-based so add one to the 0-based indices
  //
  public final int getStartBasePosition() { return _startIndex + 1; }
  public final int getEndBasePosition()   { return _endIndex + 1; }

  @Override
  public String toString()
  {
    String s = StringUtils.nullSafeConcatenation( "SequenceRange {id=", _sequenceId,
        ", position=", getStartBasePosition(),
        "-", getEndBasePosition(),
        ", length=", getSequenceLength(),
        ", strand=", _strand.getCharCode(), "}" );

    return s;
  }
}
