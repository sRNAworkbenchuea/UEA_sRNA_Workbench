/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.vissr.sequencewindows;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.genoviz.glyph.*;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
/**
 *
 * @author prb07qmu
 */
public class SequenceWindowHelper
{
  public static int NUM_LENGTHS = StripedSolidRectGlyph.NUMBER_OF_STRIPES;

  private SequenceRangeI _referenceSequence;
  private List<SequenceRangeI> _referenceSequenceList;
  private final List< Integer > _lengths;

  private int _startIndex;
  private int _stopIndex;
  private int _numWindows;
  private String _message;

  public SequenceWindowHelper( SequenceRangeI referenceSequence, List< Integer > lengths )
  {
    NUM_LENGTHS = lengths.size();
//    if ( lengths.size() != NUM_LENGTHS )
//      throw new IllegalStateException( "The number of lengths must be " + NUM_LENGTHS + " but number is " + lengths.size() );

    _referenceSequence = referenceSequence;
    _referenceSequenceList = new ArrayList<SequenceRangeI>();
    _referenceSequenceList.add( referenceSequence );

    // Make a defensive copy
    _lengths = new ArrayList<Integer>( lengths );
  }
  
  public SequenceWindowHelper( List<SequenceRangeI> referenceSequences, List< Integer > lengths )
  {
    NUM_LENGTHS = lengths.size();

    _referenceSequenceList = referenceSequences;

    _referenceSequence = _referenceSequenceList.get( 0);
    // Make a defensive copy
    _lengths = new ArrayList<Integer>( lengths );
  }

  public int getStartIndex() { return _startIndex; }
  public int getStopIndex()  { return _stopIndex; }
  public String getMessage() { return _message; }

  /**
   * Get the lengths which are currently in use.
   *
   * @return The ordered set of lengths
   */
  public List< Integer > getLengths()
  {
    return Collections.unmodifiableList( _lengths );
  }

  public boolean getUserInput( Component parent )
  {
    // TODO: Put a proper UI around this !

    _message = "";

    // Get the number of windows
    //
    Object numWindowsO = JOptionPane.showInputDialog( parent,
            "How many windows would you like to use ?",
            "Number of windows",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            10 );

    if ( numWindowsO == null )
      return false;

    int numWindows = StringUtils.safeIntegerParse( numWindowsO.toString(), 10 );

    if ( numWindows < 5 || numWindows > 5000 )
    {
      _message = "Number of windows must be between 5 and 5000";
      return false;
    }

    // Get the start index (ask for the base position => subtract 1 to get the index)
    //
    Object startO = JOptionPane.showInputDialog( parent,
            "Please enter the start base position ?",
            "Start position",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            1 );

    if ( startO == null )
      return false;

    int startIndex = Math.max( 1, StringUtils.safeIntegerParse( startO.toString(), 1 ) ) - 1;

    // Get the end index (ask for the base position => subtract 1 to get the index)
    //
    Object stopO = JOptionPane.showInputDialog( parent,
            "Please enter the end base position ?",
            "Stop position",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            _referenceSequence.getSequenceLength() );

    if ( stopO == null )
      return false;

    int stopIndex = Math.min( _referenceSequence.getSequenceLength(), StringUtils.safeIntegerParse( stopO.toString(), 1 ) ) - 1;

    if ( startIndex > stopIndex )
    {
      int d = stopIndex;
      stopIndex = startIndex;
      startIndex = d;
    }

    _startIndex = startIndex;
    _stopIndex  = stopIndex;
    _numWindows = numWindows;

    return true;
  }
  
  public void setNumWindows(int num)
  {
    _numWindows = num;
  }
  public void setStartStopIndex(int start, int end)
  {
     _startIndex = start;
    _stopIndex  = end;
  }
  public void setReferenceSequence(String chromo)
  {
    for(SequenceRangeI ref : this._referenceSequenceList)
    {
      if(ref.getSequenceId().equals( chromo) )
      {
        this._referenceSequence = ref;
        return;
      }
    }
  }

  public List< SequenceWindow > generateSequenceWindows( Patman basePatman )
  {
    ArrayList< SequenceWindow > windows = CollectionUtils.newArrayList( _numWindows );

    performAggregation( windows, basePatman );

    performHeightAdjustment( windows );

    return windows;
  }

  private void performAggregation( List< SequenceWindow > swl, Patman basePatman )
  {
    int windowSize = 100;//( _stopIndex - _startIndex ) / _numWindows;

    for ( int i = 0; i < _numWindows; ++i )
    {
      // e.g. [ 1000, 1999 ], [ 2000, 2999 ], etc.
      int windowStartIndex = _startIndex + i * windowSize;
      int windowStopIndex  = windowStartIndex + ( windowSize - 1 );

      if ( i == _numWindows - 1 && windowStopIndex < _stopIndex )
      {
        windowStopIndex = _stopIndex;
      }

      // Create Patman object for the index range
      Patman pwin = basePatman.performStartStopReduction( null, windowStartIndex, windowStopIndex, Patman.StraddleInclusionCriterion.MAJORITY );

      pwin.sortByStartCoord();
      
      // Create the SequenceWindow object for the range and the filtered Patman data
      SequenceWindow sw = new SequenceWindow( this, pwin, windowStartIndex, windowStopIndex, "Window" + (i+1) );

      sw.setParentSequenceI( _referenceSequence );

      swl.add( sw );
    }
  }

  private void performHeightAdjustment( List< SequenceWindow > swl )
  {
    // Get the maximum total abundance
    //
    double maxTotalAbundance = 0;

    for ( SequenceWindow sw : swl )
    {
      maxTotalAbundance = Math.max( maxTotalAbundance, sw.getTotalAbundance() );
    }

    if ( maxTotalAbundance > 0 )
    {
      // TODO: When we add multiple samples we will want to scale over
      //       multiple rows (in the same column).

      // The height scaling is such that the window with the maximum abundance
      // will be half the height of the tier
      final double MAX_ABUNDANCE_SCALE = 0.5;
      double mtaScale = Math.log10( maxTotalAbundance );
      double heightScaling;

      // Set the height scaling of each sequence window
      //
      for ( SequenceWindow sw : swl )
      {
        if ( sw.getTotalAbundance() == maxTotalAbundance )
        {
          heightScaling = MAX_ABUNDANCE_SCALE;
        }
        else if ( sw.getTotalAbundance() == 0 )
        {
          heightScaling = 0;
        }
        else
        {
          heightScaling = MAX_ABUNDANCE_SCALE * (Math.log10( sw.getTotalAbundance() ) / mtaScale);
        }

        sw.setGlyphHeightScalePercent( 100f * (float)heightScaling );
      }
    }
  }
}
