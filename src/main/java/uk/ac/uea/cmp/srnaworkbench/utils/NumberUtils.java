/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author w0445959
 */
public class NumberUtils
{
  private static DecimalFormat df2DP = new DecimalFormat( "#.##" );

  public static String format2DP( double input, RoundingMode mode )
  {
    df2DP.setRoundingMode( mode );
    return df2DP.format( input );
  }

  public static double roundToSignificantFigures( double num, int n )
  {
    if ( num == 0 )
    {
      return 0;
    }

    final double d = Math.ceil( Math.log10( num < 0 ? -num : num ) );
    final int power = n - (int) d;

    final double magnitude = Math.pow( 10, power );
    final long shifted = Math.round( num * magnitude );
    return shifted / magnitude;
  }

  public static double logOfBase( int base, int num )
  {
    return Math.log( num ) / Math.log( base );
  }
  
  public static byte[] intToByteArray( final int integer )
  {
    byte[] result = new byte[4];

    result[0] = (byte) ( ( integer & 0xFF000000 ) >> 24 );
    result[1] = (byte) ( ( integer & 0x00FF0000 ) >> 16 );
    result[2] = (byte) ( ( integer & 0x0000FF00 ) >> 8 );
    result[3] = (byte) ( integer & 0x000000FF );

    return result;
  }

}
