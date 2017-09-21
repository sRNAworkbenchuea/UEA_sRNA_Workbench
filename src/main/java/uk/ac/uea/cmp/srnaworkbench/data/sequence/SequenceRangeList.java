/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.util.*;

/**
 *
 * @author prb07qmu
 */
public class SequenceRangeList
{
  private SequenceRangeList() {}

  /***
   * Get a map of the sequence's id to each sequence.<br>
   * Sequences with the same id receive no special treatment, so the map will omit duplicate ids.<br>
   * However if the map's size equals the list's size then the ids are unique.
   *
   * @param sortIds true to sort the ids before returning
   * @return map of string id to a {@link SequenceRange}
   */
  public static Map< String, SequenceRangeI > getIdToSequenceRangeMap( List< ? extends SequenceRangeI > src, boolean sortIds )
  {
    HashMap< String, SequenceRangeI > result = CollectionUtils.newHashMap( src.size() );

    for ( SequenceRangeI sr : src )
    {
      if ( sr == null )
        continue;

      result.put( sr.getSequenceId(), sr );
    }

    if ( sortIds )
    {
      return new TreeMap< String, SequenceRangeI >( result );
    }
    else
    {
      return result;
    }
  }

  /***
   * Filter the given list using the given identifier of the parent
   *
   * @param parentSequenceId The string identifier of the parent
   * @param src The list on which to filter
   * @return The filtered list
   */
  public static List<SequenceRangeI> filterByParentId( String parentSequenceId, List<SequenceRangeI> src )
  {
    if ( parentSequenceId == null || parentSequenceId.trim().isEmpty() )
      return Collections.emptyList();

    ArrayList<SequenceRangeI> result = CollectionUtils.newArrayList( src.size() );

    for ( SequenceRangeI sr : src )
    {
      if ( parentSequenceId.equals( sr.getParentSequenceRangeI().getSequenceId() ) )
      {
        result.add( sr );
      }
    }

    result.trimToSize();

    return result;
  }

  /***
   * Map sequence identifiers to lists of sequence range objects.
   * The key is the result of the 'getSequenceId()' method.
   *
   * @param sourceList A list of objects which implement {@link SequenceRangeI}
   * @return A map where the key is the sequence id and the item is a list of type {@link SequenceRangeI}
   */
  public static Map< String, List< SequenceRangeI > > indexListById( List< SequenceRangeI > sourceList )
  {
    HashMap< String, List< SequenceRangeI > > result = CollectionUtils.newHashMap();

    for ( SequenceRangeI sr : sourceList )
    {
      List< SequenceRangeI > l = result.get( sr.getSequenceId() );

      if ( l == null )
      {
        l = CollectionUtils.newArrayList( 100 );
        result.put( sr.getSequenceId(), l );
      }

      l.add( sr );
    }

    return result;
  }
}
