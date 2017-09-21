/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author ezb11yfu
 */
public class NucleotideDistribution extends HashMap<Integer, HashMap<Character, Integer>>
{
  public NucleotideDistribution()
  {
    super();
  }

  public NucleotideDistribution( List<MirBaseHeader> seqs )
  {
    super();

    for ( MirBaseHeader mbh : seqs )
    {
      String seq = mbh.getSeq();

      for ( int i = 0; i < seq.length(); i++ )
      {
        HashMap<Character, Integer> val = this.get( i );
        if ( val == null )
        {
          val = new HashMap<Character, Integer>();
        }

        char c = seq.charAt( i );
        int count = ( val.get( c ) == null ) ? 1 : val.get( c ) + 1;
        val.put( c, count );

        this.put( new Integer( i ), val );
      }
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Pos\tA\tU\tG\tC" );
    for ( Integer pos : new TreeSet<Integer>( this.keySet() ) )
    {
      Map<Character, Integer> nts = this.get( pos );
      String a = ( nts.get( 'A' ) == null ) ? "0" : nts.get( 'A' ).toString();
      String u = ( nts.get( 'U' ) == null ) ? "0" : nts.get( 'U' ).toString();
      String g = ( nts.get( 'G' ) == null ) ? "0" : nts.get( 'G' ).toString();
      String c = ( nts.get( 'C' ) == null ) ? "0" : nts.get( 'C' ).toString();
      sb.append( pos ).append( "\t" ).append( a ).append( "\t" ).append( u ).append( "\t" ).append( g ).append( "\t" ).append( c );
    }
    sb.append( "\n" );
    return sb.toString();
  }
}
