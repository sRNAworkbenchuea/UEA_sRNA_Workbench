/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows;

import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;

import java.util.*;

/**
 * A contiguous <i>window</i> onto the governing sequence together with a list of
 * short reads (for example).<br>
 * <br>
 * TODO: Change from PatmanEntry to something more generic - similar class is used for GFFRecord.
 *
 * @author prb07qmu
 */
public class SequenceWindow extends SequenceRange
{
  // Example: SequenceWindow W_1 starts at 1 and ends at 100, W_2 starts at 101 and ends at 200, etc.

  private final SequenceWindowHelper _swh;
  private final Patman _entries;

  private final Map< Integer, Integer > _lengthDistributionMap;
  private final Map< Integer, Double > _lengthAbundanceMap;

  public SequenceWindow( SequenceWindowHelper swh, Patman entries, int start, int end, String id )
  {
    super( start, end, SequenceStrand.UNKNOWN, id );

    _swh = swh;
    _entries = entries;

    _lengthDistributionMap = ( _entries == null || _entries.isEmpty() ? null :_entries.getLengthToFrequencyBreakdown() );

    if ( _lengthDistributionMap != null && _entries != null )
    {
      // Create a map containing the length -> abundance mappings
      _lengthAbundanceMap = _entries.getLengthToAbundanceBreakdown();
    }
    else
    {
      _lengthAbundanceMap = null;
    }
  }

  public SequenceWindowHelper getSequenceWindowHelper()
  {
    return _swh;
  }

  public Patman getPatmanEntries()
  {
    return _entries;
  }

  public Map<Integer, Integer> getLengthDistributionMap()
  {
    return _lengthDistributionMap;
  }

  public Map<Integer, Double> getLengthAbundanceMap()
  {
    return _lengthAbundanceMap;
  }

  public double getTotalAbundance()
  {
    if ( _lengthAbundanceMap == null || _lengthAbundanceMap.isEmpty() )
      return 0;

    double total = 0;

    for ( Double a : _lengthAbundanceMap.values() )
    {
      total += a.doubleValue();
    }

    return total;
  }

  @Override
  public String toString()
  {
    // Note: _entries has been intentionally omitted (in case it is large)

    return StringUtils.nullSafeConcatenation( "SequenceWindow { ", super.toString(),
      ", lengthDistributionMap=", _lengthDistributionMap, ", lengthAbundanceMap=", _lengthAbundanceMap, "}" );
  }
}
