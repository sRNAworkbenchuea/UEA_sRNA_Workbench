/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author ezb11yfu
 */
public class OrganismDistribution extends HashMap<String, Integer>
{
  public OrganismDistribution( List<MirBaseHeader> list )
  {
    HashMap<String, Integer> org_counts = new HashMap<String, Integer>();
    for ( MirBaseHeader mbh : list )
    {
      String org = mbh.getBinomialLatinName();

      int count = org_counts.get( org ) != null ? org_counts.get( org ) + 1 : 1;

      org_counts.put( org, count );
    }
  }

  public final int incOrganism( String organism )
  {
    int count = this.get( organism ) != null ? this.get( organism ) + 1 : 1;
    this.put( organism, count );

    return count;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for ( String str : new TreeSet<String>( this.keySet() ) )
    {
      sb.append( str ).append( " : " ).append( this.get( str ) );
    }
    return sb.toString();
  }
}
