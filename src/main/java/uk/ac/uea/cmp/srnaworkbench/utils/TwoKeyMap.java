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
public class TwoKeyMap< K1, K2, V >
{
  // Static factory creation methods (to save typing)
  public static < K1, K2, V > TwoKeyMap< K1, K2, V > createHashMap()
  {
    return new TwoKeyMap<K1, K2, V>( false, false );
  }

  public static < K1, K2, V > TwoKeyMap< K1, K2, V > createTreeMap()
  {
    return new TwoKeyMap<K1, K2, V>( true, true );
  }

  private final Map< K1, Map< K2, V > > _map;
  private final boolean _sortKey2;

  private TwoKeyMap( boolean sortKey1, boolean sortKey2 )
  {
    _sortKey2 = sortKey2;

    if ( sortKey1 )
    {
      _map = CollectionUtils.newTreeMap();
    }
    else
    {
      _map = CollectionUtils.newHashMap();
    }
  }

  public int size()
  {
    if ( _map.isEmpty() )
      return 0;

    int size = 0;

    for ( K1 k1 : _map.keySet() )
    {
      size += getInnerMapNeverNull( k1 ).size();
    }

    return size;
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public boolean containsKey( K1 k1, K2 k2 )
  {
    if ( _map.containsKey( k1 ) )
    {
      return getInnerMapNeverNull( k1 ).containsKey( k2 );
    }

    return false;
  }

  public boolean containsValue( V v )
  {
    for ( Map< K2, V > innerMap : _map.values() )
    {
      if ( innerMap.containsValue( v ) )
        return true;
    }

    return false;
  }

  public V get( K1 k1, K2 k2 )
  {
    return getInnerMapNeverNull( k1 ).get( k2 );
  }

  public V put( K1 k1, K2 k2, V v )
  {
    Map< K2, V > innerMap = getInnerMapNeverNull( k1 );

    // If it's empty then it's a Collections.emptyMap() so create a new one...
    //
    if ( innerMap.isEmpty() )
    {
      if ( _sortKey2 )
      {
        innerMap = CollectionUtils.newTreeMap();
      }
      else
      {
        innerMap = CollectionUtils.newHashMap();
      }

      _map.put( k1, innerMap );
    }

    // return the previously held value (or null)
    return innerMap.put( k2, v );
  }

  public V remove( K1 k1, K2 k2 )
  {
    Map< K2, V > innerMap = getInnerMapNeverNull( k1 );

    if ( innerMap.isEmpty() )
      return null;

    return innerMap.remove( k2 );
  }

  public void putAll( TwoKeyMap< K1, K2, V >  m )
  {
    if ( m.isEmpty() )
      return;

    for ( K1 k1 : m._map.keySet() )
    {
      Map< K2, V > innerMap = m.getInnerMapNeverNull( k1 );

      for ( K2 k2 : innerMap.keySet() )
      {
        put( k1, k2, innerMap.get( k2 ) );
      }
    }
  }

  public void putAll( Map< ? extends K1, Map< ? extends K2, ? extends V > >  m )
  {
    if ( m.isEmpty() )
      return;

    for ( K1 k1 : m.keySet() )
    {
      Map< ? extends K2, ? extends V > innerMap = m.get( k1 );

      for ( K2 k2 : innerMap.keySet() )
      {
        put( k1, k2, innerMap.get( k2 ) );
      }
    }
  }

  public void clear()
  {
    if ( isEmpty() )
      return;

    for ( K1 k1 : _map.keySet() )
    {
      Map< K2, V > innerMap = getInnerMapNeverNull( k1 );

      if ( ! innerMap.isEmpty() )
      {
        innerMap.clear();
      }
    }

    _map.clear();
  }

  public Set< K1 >  getKey1Set()
  {
    return _map.keySet();
  }

  public Map< K2, V > getInnerMap( K1 k1 )
  {
    return _map.get( k1 );
  }

  public Set< Map.Entry< K1, Map< K2, V > > > entrySet()
  {
    return _map.entrySet();
  }

  /**
   * Get the inner map for the key
   *
   * @param k1 The 1st part of the key
   * @return The inner map or an empty map if the map doesn't contain the key
   */
  private Map< K2, V > getInnerMapNeverNull( K1 k1 )
  {
    Map< K2, V > result = _map.get( k1 );

    // Happens when 'k1' is not a key in the map
    if ( result == null )
      return Collections.emptyMap();

    return result;
  }
}
