/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author prb07qmu
 */
public enum SequenceStrand implements Serializable 
{
  POSITIVE( '+', "Positive strand" ),
  NEGATIVE( '-', "Negative strand" ),
  UNSTRANDED( '.', "No strand" ),
  RELEVANT_BUT_UNKNOWN( '?', "Strand is relevant but unknown" ),
  //
  UNKNOWN('*', "Unknown");

  private static final Map< Character, SequenceStrand > _lookup =
    new HashMap< Character, SequenceStrand >();

  static
  {
    for ( SequenceStrand s : EnumSet.allOf( SequenceStrand.class ) )
      _lookup.put( s.getCharCode(), s );
  }

  private final char _charCode;
  private final String _description;

  private SequenceStrand( char charCode, String desc )
  {
    _charCode = charCode;
    _description = desc;
  }

  public char getCharCode()
  {
    return _charCode;
  }

  public String getDescription()
  {
    return _description;
  }

  public static SequenceStrand getSequenceStrand( char charCode )
  {
    SequenceStrand ss = _lookup.get( charCode );

    if ( ss == null )
      ss = UNKNOWN;

    return ss;
  }

  @Override
  public String toString()
  {
    return "" + _charCode + " [" + _description + "]";
  }

    public String getCode()
    {
        return ""+_charCode;
    }
}
