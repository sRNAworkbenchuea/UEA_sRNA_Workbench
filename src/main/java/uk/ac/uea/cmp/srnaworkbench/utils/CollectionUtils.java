/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.*;

/**
 * Collection of utility methods for collections.<br/>
 * <br/>
 * The 'newXXX' methods can be used to avoid repetitious type declarations,<br/>
 * e.g. {@code Map<String, Integer> l = CollectionUtils.newHashMap()} instead of {@code Map<String,Integer> l = new HashMap<String,Integer>()}.
 *
 */
public class CollectionUtils
{
  private CollectionUtils() {}

  public static <T> ArrayList<T> newArrayList()
  {
    return new ArrayList<T>();
  }

  public static <T> ArrayList<T> newArrayList( int initialCapacity )
  {
    return new ArrayList<T>( initialCapacity );
  }

  public static <T> HashSet<T> newHashSet()
  {
    return new HashSet<T>();
  }

  public static <T> HashSet<T> newHashSet( int initialCapacity )
  {
    return new HashSet<T>( initialCapacity );
  }

  public static <T> TreeSet<T> newTreeSet()
  {
    return new TreeSet<T>();
  }

  public static <K,V> HashMap<K,V> newHashMap()
  {
    return new HashMap<K,V>();
  }

  public static <K,V> HashMap<K,V> newHashMap( int initialCapacity )
  {
    return new HashMap<K,V>( initialCapacity );
  }

  public static <K,V> TreeMap<K,V> newTreeMap()
  {
    return new TreeMap<K,V>();
  }

  /**
   * Create an empty EnumMap.
   * @param <K> The enum
   * @param <V> The values in the map
   * @param keyType The class of the enum
   * @return An EnumMap with keys.
   */
  public static <K extends Enum<K>, V> EnumMap< K, V > newEnumMap( Class<K> keyType )
  {
    return new EnumMap< K, V>( keyType );
  }
}
