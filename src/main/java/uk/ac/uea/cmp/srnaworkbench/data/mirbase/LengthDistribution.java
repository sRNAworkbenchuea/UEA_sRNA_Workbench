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
public class LengthDistribution extends HashMap<Integer, Integer>
{
  public LengthDistribution()
  {
    super();
  }

  public LengthDistribution( List<MirBaseHeader> seqs )
  {
    for ( MirBaseHeader mbh : seqs )
    {
      incLength( mbh.getSeq().length() );
    }
  }

  public final int incLength( int seq_len )
  {
    int count = this.get( seq_len ) != null ? this.get( seq_len ) + 1 : 1;
    this.put( seq_len, count );

    return count;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    for ( Integer len : new TreeSet<Integer>( this.keySet() ) )
    {
      sb.append( len ).append( "\t" ).append( this.get( len ) );
    }

    return sb.toString();
  }
}
