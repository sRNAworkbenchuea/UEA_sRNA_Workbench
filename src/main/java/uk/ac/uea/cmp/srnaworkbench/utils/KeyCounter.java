/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.*;

/**
 *
 * @author prb07qmu
 */
public final class KeyCounter< K >
{
  /**
   * Provides type inference short-cutting
   * @param <K> The type for the KeyCounter
   * @return The KeyCounter object
   */
  public static < K > KeyCounter< K > createKeyCounter()
  {
    return new KeyCounter< K >();
  }

  private final Map< K, IntWrapper > _map;

  public KeyCounter()
  {
    _map = new HashMap< K, IntWrapper >();
  }

  public KeyCounter( Collection< K > keys )
  {
    _map = new HashMap< K, IntWrapper >( keys.size() );

    for ( K k : keys )
    {
      _map.put( k, new IntWrapper() );
    }
  }

  public boolean containsKey( K k )
  {
    return _map.containsKey( k );
  }

  public void clear()
  {
    _map.clear();
  }

  public int getTotalCount()
  {
    int count = 0;

    for ( IntWrapper iw : _map.values() )
    {
      count += iw.getCounter();
    }

    return count;
  }

  public int size()
  {
      return _map.size();
  }

  public void incrementCounter( K k, int counter )
  {
    IntWrapper iw = _map.get( k );

    if ( iw == null )
    {
      iw = new IntWrapper( counter );
      _map.put( k, iw );
    }
    else
    {
      iw.incrementCounter( counter );
    }
  }

  public Map< K, Integer > createKeyIntegerMap()
  {
    return createKeyIntegerMap( false );
  }

  public Map< K, Integer > createKeyIntegerMap( boolean sortKeys )
  {
    Map< K, Integer > map;

    if ( sortKeys )
    {
      map = CollectionUtils.newTreeMap();
    }
    else
    {
      map = CollectionUtils.newHashMap( _map.size() );
    }

    for ( Map.Entry< K, IntWrapper > ent : _map.entrySet() )
    {
      // Autobox the 'int' counter
      map.put( ent.getKey(), ent.getValue().getCounter() );
    }

    return map;
  }

  // Wrap an integer for counting purposes
  private final static class IntWrapper
  {
    private int _counter = 0;

    IntWrapper() {}

    IntWrapper( int counter )
    {
      _counter = counter;
    }

    void incrementCounter( int increment )
    {
      _counter += increment;
    }

    int getCounter()
    {
      return _counter;
    }
  }


  public static void main( String... args )
  {
    {
      Set< String > strings = new HashSet< String >();
      strings.addAll( Arrays.asList( "hello", "there", "world" ) );

      KeyCounter< String > kc = new KeyCounter< String >( strings );

      kc.incrementCounter( "hello", 5 );
      kc.incrementCounter( "hello", 7 );
      kc.incrementCounter( "there", 5 );
      kc.incrementCounter( "there", 3 );
      kc.incrementCounter( "hello", 2 );
      kc.incrementCounter( "there", 1 );

      System.out.println( "" + kc.createKeyIntegerMap( true ) );

      kc.clear();
    }

    {
      KeyCounter< String > kc = new KeyCounter< String >();

      kc.incrementCounter( "hello", 5 );
      kc.incrementCounter( "hello", 7 );
      kc.incrementCounter( "there", 5 );
      kc.incrementCounter( "there", 3 );
      kc.incrementCounter( "hello", 2 );
      kc.incrementCounter( "there", 1 );

      System.out.println( "" + kc.createKeyIntegerMap( true ) );

      kc.clear();
    }
  }
}
