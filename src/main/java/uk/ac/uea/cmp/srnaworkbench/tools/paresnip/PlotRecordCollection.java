/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.util.*;

/**
 * Maintain a collection of {@link PlotRecord} objects together with helpful methods
 *
 * @author prb07qmu
 */
public final class PlotRecordCollection
{
  /** A map linking a {@link Category} to a list of {@link PlotRecord}s */
  private final EnumMap< Category, List< PlotRecord > > _data;

  private boolean _isIndexed = false;

  /** The total length of all records and the spaces in between */
  private int _totalLength = 0;

  /** Constructor */
  public PlotRecordCollection()
  {
    _data = CollectionUtils.newEnumMap( Category.class );

    for ( Category c : Category.values() )
    {
      _data.put( c, new ArrayList< PlotRecord>() );
    }
  }

  /**
   * Add a {@link PlotRecord} to the collection
   *
   * @param pr
   */
  public void add( PlotRecord pr )
  {
    if ( pr.getMaxCategory() == Category.UNDEFINED )
      return;

    _data.get( pr.getMaxCategory() ).add( pr );

    _isIndexed = false;
  }

  public void add( List< PlotRecord > plotRecords )
  {
    for ( PlotRecord pr : plotRecords )
    {
      add( pr );
    }
  }

  /**
   * Clears all data
   */
  public void clear()
  {
    for ( Category c : Category.values() )
    {
      _data.get( c ).clear();
    }

    _data.clear();
  }

  /**
   * Get a list for a given category
   *
   * @param category  The {@link Category} on which to filter
   * @return An unmodifiable list of {@link PlotRecord}s
   */
  public List< PlotRecord > getPlotRecordsForCategory( Category category )
  {
    return Collections.unmodifiableList( _data.get( category ) );
  }

  public void createIndexIgnoreState()
  {
    _isIndexed = false;
    createIndex();
  }

  /**
   * Sort, index and set indices on the {@link PlotRecord}s.<br/>
   * This method <b>should</b> be called after all the {@link PlotRecord}s have been added.
   */
  public void createIndex()
  {
    if ( _isIndexed )
      return;

    // Sort the lists and the sRNA hits
    sort();

    // Set the start index of each PlotRecord
    setStartIndices();

    // Set the height scalings relative using the degradome hit abundance
    setHeightScalings();

    _isIndexed = true;
  }

  private void sort()
  {
    for ( List< PlotRecord > lpr : _data.values() )
    {
      Collections.sort( lpr, PlotRecord.CATEGORY_ABUNDANCE_COMPARATOR );

      for ( PlotRecord pr : lpr )
      {
        pr.sortSmallRNAHits();
      }
    }
  }

  private void setStartIndices()
  {
    _totalLength = 0;

    for ( List< PlotRecord > lpr : _data.values() )
    {
      int startIndex = PlotRecord.VISUALISATION_GAP_WIDTH / 2;

      for ( PlotRecord pr : lpr )
      {
        pr.setStartIndex( startIndex );
        startIndex = pr.getEndIndex() + PlotRecord.VISUALISATION_GAP_WIDTH + 1;
      }

      _totalLength = Math.max( _totalLength, startIndex );
    }
  }

  private void setHeightScalings()
  {
    for ( List< PlotRecord > lpr : _data.values() )
    {
      float maxAbundance = 0;

      for ( PlotRecord pr : lpr )
      {
        maxAbundance = Math.max( maxAbundance, pr.getMaxDegradomeHitAbundance() );
      }

      // These values are for making sure the glyphs do not become too small
      final float SMALL_SCALE    = 0.04f;
      final float SMALLEST_SCALE = 0.02f;

      for ( PlotRecord pr : lpr )
      {
        float scaling = 0;

        if ( pr.getMaxDegradomeHitAbundance() > 0 && maxAbundance > 0 )
        {
          /*
           * MaxDegradomeHitAbundance (MDHA) is in [ 0, maxAbundance ].
           * The scalings for the subranges are:
           *
           * MDHA value/range       Scaling                                     Note
           * ----------------       -------                                     ----
           * 0                      0                                           Eliminated by 'if' test above
           * ( 0, 1 )               SMALLEST_SCALE                              Set to a constant so that the glyph won't be too small
           * [ 1, maxAbundance )    max( MDHA / maxAbundance, SMALL_SCALE )     Linear scale but will not be below a constant
           * maxAbundance           1
           *
           * where SMALL_SCALE > SMALLEST_SCALE.
           *
           * The most likely case is MDHA in [ 1, maxAbundance )
           */

          if ( 1 <= pr.getMaxDegradomeHitAbundance() && pr.getMaxDegradomeHitAbundance() < maxAbundance )
          {
            // MDHA in [ 1, maxAbundance )
            scaling = pr.getMaxDegradomeHitAbundance() / maxAbundance;
            scaling = Math.max( scaling, SMALL_SCALE );
          }
          else if ( pr.getMaxDegradomeHitAbundance() <  1 )
          {
            // MDHA in ( 0, 1 )
            scaling = SMALLEST_SCALE;
          }
          else
          {
            // MDHA == maxAbundance
            scaling = 1;
          }
        }

        pr.setGlyphHeightScalePercent( 100 * scaling );
      }
    }
  }

  public SequenceRangeI createReferenceSequence( String sequenceId )
  {
    // Make sure - it should have been called earlier !
    createIndex();

    SequenceRangeI result = new SequenceRange( 0, _totalLength - 1, SequenceStrand.UNSTRANDED, sequenceId );

    return result;
  }

  /**
   * Match the small RNA hit alignment sequence to the given miRBase sequences.<br/>
   * Note: The same sequence may appear in the miRBase file with different identifiers.<br/>
   * The shortest identifier is used.
   *
   * @param mirbaseRecords List of records from a miRBase file
   */
  public void performMirbaseLookup( List< SequenceRangeI > mirbaseRecords )
  {
    // Want to map the sequences
    //
    Map< String, SequenceRangeI > lookup = CollectionUtils.newHashMap();

    for ( SequenceRangeI sr : mirbaseRecords )
    {
      SequenceRangeI mirbaseEntry = lookup.get( sr.getSequence() );

      if ( mirbaseEntry == null )
      {
        lookup.put( sr.getSequence(), sr );
      }
      else
      {
        // Replace the entry if the identifier is shorter
        // We do this because a mirbase id can have an extra letter, e.g. mtr-miR160, mtr-miR160b, etc.
        if ( sr.getSequenceId().length() < mirbaseEntry.getSequenceId().length() )
        {
          lookup.put( sr.getSequence(), sr );
        }
      }
    }

    // Check the sRNA sequence of the sRNA hit against the miRBase entry
    //
    for ( List< PlotRecord > l : _data.values() )
    {
      for ( PlotRecord pr : l )
      {
        if ( pr.hasSRNAhit() )
        {
          for ( PlotRecord.SmallRNAHit sh : pr.getSmallRNAHits() )
          {
            String sequence = sh.getAlignmentSRNA().replace( "-", "" ).replace( "3'", "" ).replace( "5'", "" ).trim();

            SequenceRangeI mirbaseEntry = lookup.get( sequence );

            if ( mirbaseEntry != null )
            {
              sh.setMirbaseId( mirbaseEntry.getSequenceId() );
            }
          }
        }
      }
    }

    lookup.clear();
  }

  public int size()
  {
    int size = 0;

    for ( List< PlotRecord > l : _data.values() )
    {
      size += l.size();
    }

    return size;
  }
}
