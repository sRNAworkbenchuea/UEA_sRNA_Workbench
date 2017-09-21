/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.*;
import java.util.concurrent.*;

/***
 * StringPool class to help minimise memory usage for oft repeated strings.
 * ConcurrentMap is used so the pool is thread-safe.
 *
 * @author prb07qmu
 */
public class StringPool
{
  private final ConcurrentMap<String,String> _map;

  public StringPool()
  {
    this( 1000 );
  }

  public StringPool( int initialCapacity )
  {
     _map = new ConcurrentHashMap< String, String >( initialCapacity );
  }

  public String getString( String str )
  {
    String mappedString = _map.get( str );

    if ( mappedString == null )
    {
      // Not in the map, try and add it...
      mappedString = _map.putIfAbsent( str, str );

      if ( mappedString == null )
      {
        // when putIfAbsent returns null, the method call worked so use new value...
        mappedString = str;
      }
    }

    return mappedString;
  }

  public void clear()
  {
    _map.clear();
  }
  
  public Map<String,String> getUnmodifiableMap()
  {
    return Collections.unmodifiableMap( _map );
  }
}
