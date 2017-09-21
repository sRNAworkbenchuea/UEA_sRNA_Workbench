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
public class IndexedGFFRecords
{
  private final List< GFFRecord > _gffRecords;

  // Map a GFF Type -> sequence id -> list of records
  private final HashMap< String, HashMap< String, List< GFFRecord > > > _typeToSeqIdToRecordsMap = CollectionUtils.newHashMap();

  public IndexedGFFRecords( List< GFFRecord > gffRecords )
  {
    _gffRecords = gffRecords;
  }

  public void index()
  {
    // Pool the strings

    StringPool pool = new StringPool(20);

    HashSet<String> keySet  = new HashSet<String>( 20 );
    HashSet<String> typeSet = new HashSet<String>( 20 );

    for ( GFFRecord g : _gffRecords )
    {
      String key  = pool.getString( g.getSeqid() );  // e.g. Chr1, Chr2 etc.
      String type = pool.getString( g.getType() );   // e.g. gene, exon, CDS etc.

      keySet.add( key );
      typeSet.add( type );
    }

    // Add the GFF types to the map's keys
    //
    for ( String gffType : typeSet )
    {
      HashMap< String, List< GFFRecord > > m = CollectionUtils.newHashMap();
      _typeToSeqIdToRecordsMap.put( gffType, m );

      // Add the sequence id -> list mapping
      for ( String sequenceId : keySet )
      {
        m.put( sequenceId, new ArrayList<GFFRecord>( 100 ) );
      }
    }

    HashMap< String, GFFRecord > idAttributeMap = CollectionUtils.newHashMap( _gffRecords.size() );

    for ( GFFRecord g : _gffRecords )
    {
      String sequenceId = pool.getString( g.getSeqid() );
      String gffType = pool.getString( g.getType() );

      _typeToSeqIdToRecordsMap.get( gffType ).get( sequenceId ).add( g );

      // Add an ID -> GFF record if the ID attribute is set
      String idAttribute = g.getAttribute( "ID" );
      if ( idAttribute != null )
      {
        idAttributeMap.put( idAttribute, g );
      }
    }

    // Use the ID -> GFFRecord map when searching for the 'Parent' attribute
    for ( GFFRecord g : _gffRecords )
    {
      String idAttribute = g.getAttribute( "ID" );
      String parentAttribute = g.getAttribute( "Parent" );

      // Only create the parent-child relationship when the 'Parent' attribute is set (to avoid v. nested hierarchies)
      if ( idAttribute == null && parentAttribute != null )
      {
        // TODO: parentAttribute could be a comma delimited list of identifiers - need to split them up and check each one

        GFFRecord parentGFFRecord = idAttributeMap.get( parentAttribute );

        if ( parentGFFRecord != null )
        {
          parentGFFRecord.addChild( g );
        }
      }
    }

    // Sort the GFF children
    for ( GFFRecord g : _gffRecords )
    {
      g.sortChildrenByStartIndex();
    }
  }

  public List< GFFRecord > getRecords( String sequenceid, String gffType )
  {
    HashMap< String, List<GFFRecord> >  m = _typeToSeqIdToRecordsMap.get( gffType );

    if ( m == null )
      return Collections.emptyList();

    List< GFFRecord > l = m.get( sequenceid );

    if ( l == null )
      return Collections.emptyList();

    return m.get( sequenceid );
  }

  /***
   * Clear the maps - the lists are intentionally not emptied as they may be in use elsewhere.
   */
  public void clear()
  {
    for ( HashMap< String, List<GFFRecord> > m : _typeToSeqIdToRecordsMap.values() )
    {
      // Clear each map
      m.clear();
    }

    _typeToSeqIdToRecordsMap.clear();
  }
}
