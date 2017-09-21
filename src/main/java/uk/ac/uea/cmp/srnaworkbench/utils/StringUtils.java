/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.IOException;
import java.util.logging.Level;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author prb07qmu
 */
public final class StringUtils
{

  public static int occurs( String str, String subStr )
  {
    int occurrences = 0;
    int fromIndex = 0;

    while ( fromIndex > -1 )
    {
      fromIndex = str.indexOf( subStr, occurrences == 0 ? fromIndex : fromIndex + subStr.length() );
      if ( fromIndex > -1 )
      {
        occurrences++;
      }
    }

  return occurrences;
  }
  private StringUtils()
  {
  }

  /***
   * Format an arbitrary number of {@code Object}s as a concatenated {@code String}.
   * A null {@code Object} will be formatted as {@code <null>}.
   *
   * @param objs The objects to concatenate
   * @return The concatenated string.
   */
  public static String nullSafeConcatenation( Object... objs )
  {
    StringBuilder sb = new StringBuilder( 100 );

    // Concatenate the stringified objects (replacing null objects with '<null>')...
    for ( int i = 0, n = objs.length; i < n; ++i )
    {
      Object o = objs[i];
      sb.append( o == null ? "<null>" : o.toString() );
    }

    return sb.toString();
  }

  /***
   * Extract an integer from a String safely, i.e. without propagating a NumberFormatException
   *
   * @param str        The input string
   * @param defaultInt Default integer
   * @return           The result of parsing the string, or the default integer if the input is invalid
   */
  public static int safeIntegerParse( String str, int defaultInt )
  {
    if ( str == null || str.length() == 0 || ".".equals( str ) )
    {
      return defaultInt;
    }

    try
    {
      return Integer.parseInt( str );
    }
    catch ( NumberFormatException e )
    {
      LOGGER.log( Level.FINE, "safeIntegerParse(..) : A NumberFormatException occurred when parsing the String : ''{0}''", str);
    }

    return defaultInt;
  }

  /***
   * Extract a float from a String safely, i.e. without propagating a NumberFormatException
   *
   * @param str          The input string
   * @param defaultFloat Default float
   * @return             The result of parsing the string, or the default float if the input is invalid
   */
  public static float safeFloatParse( String str, float defaultFloat )
  {
    if ( str == null || str.length() == 0 || ".".equals( str ) )
    {
      return defaultFloat;
    }

    try
    {
      return Float.parseFloat( str );
    }
    catch ( NumberFormatException e )
    {
      LOGGER.log( Level.FINE, "safeFloatParse(..): A NumberFormatException occurred when parsing the String : ''{0}''", str);
    }

    return defaultFloat;
  }
  
  public static double safeDoubleParse( String str, double defaultDouble )
  {
    if ( str == null || str.length() == 0 || ".".equals( str ) )
    {
      return defaultDouble;
    }

    try
    {
      return Double.parseDouble( str );
    }
    catch ( NumberFormatException e )
    {
      LOGGER.log( Level.FINE, "safeDoubleParse(..): A NumberFormatException occurred when parsing the String : ''{0}''", str);
    }

    return defaultDouble;
  }

  /***
   * Extract a char from a String safely, i.e. without propagating a StringIndexOutOfBoundsException
   *
   * @param str          The input string
   * @param index        The index into the string
   * @param defaultChar  Default char
   * @return             The char at index, or the default char if the input is invalid
   */
  public static char safeCharAt( String str, int index, char defaultChar )
  {
    if ( str == null || index < 0 || index >= str.length() )
    {
      return defaultChar;
    }

    return str.charAt( index );
  }

  public static String safeSubstring( String mainString, int startIndex, String defaultSubstring )
  {
    return safeSubstring( mainString, startIndex, Integer.MAX_VALUE, defaultSubstring );
  }

  public static String safeSubstring( String mainString, int startIndex, int endIndex, String defaultSubstring )
  {
    if ( mainString == null || mainString.length() == 0 )
    {
      return defaultSubstring;
    }

    if ( startIndex < 0 || startIndex >= endIndex || startIndex >= mainString.length() )
    {
      return defaultSubstring;
    }

    if ( endIndex < 0 )
    {
      return defaultSubstring;
    }

    if ( endIndex > mainString.length() )
    {
      endIndex = mainString.length();
    }

    String result = mainString.substring( startIndex, endIndex );

    return result;
  }

  /***
   * Reverses a String (a relatively expensive method - two allocations are required)
   * @param s The original {@code String}
   * @return The reversed {@code String}
   */
  public static String reverseString( String s )
  {
    StringBuilder dest = new StringBuilder( s );

    dest = dest.reverse(); // reverse() performs the reversal in-place

    return dest.toString();
  }

  /***
   * Reverses a {@code String} using a {char} array. Only one allocation is performed.
   * Increasingly quicker than {@code reverseString( String s ) } as String length gets longer.
   *
   * @param s The original {@code String}
   * @return Return a {@code char[]}
   */
  public static char[] reverseStringToCharArray( String s )
  {
    int n = s.length();
    char[] chars = new char[n];

    for ( int i = 0, i2 = n - 1 - i; i < n; ++i, --i2 )
    {
      chars[i] = s.charAt( i2 );
    }

    return chars;
  }

  /**
   * Changes a array of char data types to an array of byte data types.
   *
   * @param c The char array to be changed.
   * @return An array of bytes.
   */
  public static byte[] changeCharArrayToByteArray( char[] c )
  {
    byte[] b = new byte[c.length];

    for ( int i = 0; i < c.length; i++ )
    {
      b[i] = (byte) c[i];
    }

    return b;
  }

  /**
   * Changes a String to an array of bytes.
   *
   * @param s The string to be changed.
   * @return An array of bytes.
   */
  public static byte[] changeStringToByteArray( String s )
  {
    char[] c = s.toCharArray();
    return changeCharArrayToByteArray( c );
  }

  private synchronized static String searchSequence( String hairpin, String sequence, int[] startEnd )
  {
    String sequenceToUse = "";

    String[] search_miRNA =
    {
      sequence,
      sequence.substring( 0, sequence.length() - 1 ),
      sequence.substring( 0, sequence.length() - 2 ),
      sequence.substring( 0, sequence.length() - 3 ),
      sequence.substring( 1, sequence.length() ),
      sequence.substring( 2, sequence.length() ),
      sequence.substring( 3, sequence.length() )
    };

    int i = 0;
    while ( ( startEnd[0] < 0 ) && i < search_miRNA.length )
    {
      sequenceToUse = search_miRNA[i];
      startEnd[0] = hairpin.indexOf( sequenceToUse );
      startEnd[1] = startEnd[0] + sequenceToUse.length();

      i++;
    }
    return sequenceToUse;
  }

  public synchronized static String constructHTMLStrings( String hairpin, String miRNA, String miRSTAR, String overallColour, int[] mirSTAR_Locs ) throws IOException
  {
    String finalHTMLString = "";

    int[] index =
    {
      -1, -1
    };

    if ( miRSTAR.isEmpty() && !miRNA.isEmpty() )
    {
      String miRNASearch = searchSequence( hairpin, miRNA, index );
      finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring( 0, index[0] );
      finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
      finalHTMLString += hairpin.substring( index[1], hairpin.length() ) + "</HTML>";
    }
    else
    {
      if ( miRSTAR.isEmpty() && miRNA.isEmpty() )
      {
        finalHTMLString = "<HTML><font color=" + overallColour + ">" + hairpin + "</HTML>";
      }
      else
      {
        String miRNASearch = searchSequence( hairpin, miRNA, index );

        int[] star_index =
        {
          -1, -1
        };

        String miRNASTARSearch = searchSequence( hairpin, miRSTAR, star_index );

        if ( index[0] < star_index[0] )
        {
          try
          {
            finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring( 0, index[0] );
            finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
            finalHTMLString += hairpin.substring( index[1], star_index[0] );
            finalHTMLString += "<u><font color=#FF0000>" + miRNASTARSearch + "</u><font color=" + overallColour + ">";
            finalHTMLString += hairpin.substring( star_index[1], hairpin.length() ) + "</HTML>";
          }
          catch ( StringIndexOutOfBoundsException e )
          {
            LOGGER.log( Level.SEVERE, "hairpin: {0} miRNA: {1} index: {2} | {3}", new Object[]{ hairpin, miRNA, index[0], index[1] });
            LOGGER.log( Level.SEVERE, "hairpin: {0} miRSTAR: {1} STAR index: {2} | {3}", new Object[]{ hairpin, miRSTAR, star_index[0], star_index[1] });
            throw new IOException(e);
          }
        }
        else
        {
          try
          {
            finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring( 0, star_index[0] );
            finalHTMLString += "<u><font color=#FF0000>" + miRNASTARSearch + "</u><font color=" + overallColour + ">";
            finalHTMLString += hairpin.substring( star_index[1], index[0] );
            finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
            finalHTMLString += hairpin.substring( index[1], hairpin.length() ) + "</HTML>";
          }
          catch ( StringIndexOutOfBoundsException e )
          {
            LOGGER.log( Level.SEVERE, "hairpin: {0} miRNA: {1} index: {2} | {3}", new Object[]{ hairpin, miRNA, index[0], index[1] });
            LOGGER.log( Level.SEVERE, "hairpin: {0} miRSTAR: {1} STAR index: {2} | {3}", new Object[]{ hairpin, miRSTAR, star_index[0], star_index[1] });
            throw new IOException(e);
          }
        }
        if ( mirSTAR_Locs != null )
        {
          mirSTAR_Locs[0] = star_index[0];
          mirSTAR_Locs[1] = star_index[1];
        }
      }
    }

    return finalHTMLString;
  }

  public synchronized static String removeHTML( String source )
  {
    String finalString = source.replace( "<HTML>", "" ).replace( "</HTML>", "" ).replace( "/", "" ).replace( "font color=", "" )
      .replace("</color>", "").replace("<br>", "").replace( "u", "" ).replace( "#0000FF", "" ).replace( "#FF0000", "" ).replace( "#FFFFFF", "" ).replace( "<>", "");
    return finalString;
  }


  /*
   * Main and test methods at the end
   */
  public static void main( String[] args )
  {
    testStringReversal();
    testSafeSubstring();
  }

  private static void testStringReversal()
  {
    String s = "ABCD";

    System.out.println( "s = " + s + ", rev. s (String) = " + reverseString( s ) );
    System.out.print( "s = " + s + ", rev. s (char[]) = " );
    System.out.println( reverseStringToCharArray( s ) );

    s = "ABCDE";

    System.out.println( "s = " + s + ", rev. s (String) = " + reverseString( s ) );
    System.out.print( "s = " + s + ", rev. s (char[]) = " );
    System.out.println( reverseStringToCharArray( s ) );

    s = "";

    System.out.println( "s = " + s + ", rev. s (String) = " + reverseString( s ) );
    System.out.print( "s = " + s + ", rev. s (char[]) = " );
    System.out.println( reverseStringToCharArray( s ) );

    final int N = 1000000;
    String s10 = "ABCDEFGHIJ"; // length is 10
    StringBuilder sb = new StringBuilder( N );
    for ( int i = 0; i < N / s10.length(); i++ )
    {
      sb.append( s10 );
    }
    s = sb.toString();

    StopWatch sw;

    sw = new StopWatch( "String reversal - using reverseStringToCharArray()" );

    sw.start();
    for ( int i = 0; i < 10; ++i )
    {
      char[] chars = StringUtils.reverseStringToCharArray( s );
      String s2 = new String( chars );
      sw.lap();
    }
    sw.stop();
    sw.printTimes();

    sw = new StopWatch( "String reversal - using reverseString()" );

    sw.start();
    for ( int i = 0; i < 10; ++i )
    {
      String s2 = StringUtils.reverseString( s );
      sw.lap();
    }
    sw.stop();
    sw.printTimes();
  }

  private static void testSafeSubstring()
  {
    String test = null;
    System.out.println( "Result 1 : " + safeSubstring( test, 0, 0, "default" ) );

    test = "";
    System.out.println( "Result 2 : " + safeSubstring( test, 0, 0, "default" ) );

    test = "x";
    System.out.println( "Result 3 : " + safeSubstring( test, -1, 0, "default" ) );
    System.out.println( "Result 4 : " + safeSubstring( test, 0, 0, "default" ) );
    System.out.println( "Result 5 : " + safeSubstring( test, 1, 0, "default" ) );
    System.out.println( "Result 6 : " + safeSubstring( test, 1, -1, "default" ) );

    System.out.println( "Result 7 : " + safeSubstring( test, 0, -1, "default" ) );
    System.out.println( "Result 8 : " + safeSubstring( test, 0, 1, "default" ) );
    System.out.println( "Result 9 : " + safeSubstring( test, 0, 2, "default" ) );

    test = "xyz";
    System.out.println( "Result 10: " + safeSubstring( test, 1, 2, "default" ) );
    System.out.println( "Result 11: " + safeSubstring( test, 1, 10, "default" ) );
  }
}
