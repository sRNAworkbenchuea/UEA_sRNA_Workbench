/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io.stream;

import java.util.Collection;
import java.util.Iterator;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 *
 * @author ezb11yfu
 */
public class Utils
{
  /**
   * Joins a collection together to produce a delimited string
   *
   * @param s The collection to be formatted with the delimiter
   * @param delimiter The delimiter to place between each element in the
   * collection
   * @return The formatted delimited collection as a string
   */
  public static String join( Collection<?> s, String delimiter )
  {
    StringBuilder builder = new StringBuilder();
    Iterator iter = s.iterator();
    while ( iter.hasNext() )
    {
      builder.append( iter.next() );
      if ( !iter.hasNext() )
      {
        break;
      }
      builder.append( delimiter );
    }
    return builder.toString();
  }

  public static String getStackTrace( Throwable t )
  {
    StringBuilder sb = new StringBuilder();

    for ( StackTraceElement e : t.getStackTrace() )
    {
      sb.append( e.toString() ).append( String.valueOf( LINE_SEPARATOR ) );
    }

    return sb.toString();
  }
}
