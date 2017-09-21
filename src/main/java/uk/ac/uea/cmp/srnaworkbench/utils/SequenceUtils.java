/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 * Some useful sequence processing utilities for processing RNA or DNA sequences
 * @author Dan Mapleson
 */
public enum SequenceUtils
{
  DNA
  {
    /**
     * Complements a DNA nucleotide ( G <-> C, A <-> T )
     * 'U' will be converted to an 'N'
     *
     * @param nucleotide The nucleotide to complement
     * @return The nucleotide in complemented form
     */
    @Override
    public char complement(char nucleotide)
    {
      char comp = 'N';

      switch(nucleotide)
      {
        case 'G':
        case 'g':
          comp = 'C';
          break;
        case 'C':
        case 'c':
          comp = 'G';
          break;
        case 'A':
        case 'a':
          comp = 'T';
          break;
        case 'T':
        case 't':
          comp = 'A';
          break;
      }

      return comp;
    }
  },
  RNA
  {
    /**
     * Complements an RNA nucleotide ( G <-> C, A <-> U )
     * 'T' will be converted to an 'N'
     *
     * @param nucleotide The nucleotide to complement
     * @return The nucleotide in complemented form
     */
    @Override
    public char complement(char nucleotide)
    {
      char comp = 'N';

      switch(nucleotide)
      {
        case 'G':
        case 'g':
          comp = 'C';
          break;
        case 'C':
        case 'c':
          comp = 'G';
          break;
        case 'A':
        case 'a':
          comp = 'U';
          break;
        case 'U':
        case 'u':
          comp = 'A';
          break;
      }

      return comp;
    }
  },
  FOLD{
       /**
     * Complements an folded nucleotide ( ( <-> ), . <-> . )
     * 
     *
     * @param nucleotide The nucleotide to complement
     * @return The nucleotide in complemented form
     */
    @Override
    public char complement(char nucleotide)
    {
      char comp = '.';

      switch(nucleotide)
      {
        case '(':
          comp = ')';
          break;
        case ')':
          comp = '(';
          break;
      }

      return comp;
    }
  };

  /**
   * Must be implemented by concrete enum to describe how it should be complemented
   * @param nucleotide The nucleotide to complement
   * @return The nucleotide in complemented form
   */
  public abstract char complement( char nucleotide );

  /**
   * Calculates the GC content of the given sequence
   * @param sequence The sequence to test
   * @return The number of G's or C's in the given sequence
   */
  public int getGCCount( String sequence )
  {
    int count = 0;
    for ( int i = 0; i < sequence.length(); i++ )
    {
      char nt = sequence.charAt( i );

      if ( nt == 'G' || nt == 'g' || nt == 'C' || nt == 'c' )
      {
        count++;
      }
    }

    return count;
  }

  /**
   * Simply reverses the given sequence
   * @param sequence The sequence to reverse
   * @return The given sequence in reversed form
   */
  public String reverse( String sequence )
  {
    return new StringBuilder( sequence ).reverse().toString();
  }

  /**
   * Complements the given sequence
   * @param sequence The sequence to complement
   * @return The given sequence in complemented form
   */
  public String complement( String sequence )
  {
    StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < sequence.length(); i++ )
    {
      char oc = sequence.charAt( i );
      char nc = complement( oc );
      sb.append( nc );
    }
    return sb.toString();
  }

  /**
   * Reverse complements the given sequence
   * @param sequence The sequence to reverse complement
   * @return The given sequence in reverse complemented form
   */
  public String reverseComplement( String sequence )
  {
    // Rather than calling reverse and then compliment it's better for the cache
    // to do this is one loop... should therefore be faster this way.
    StringBuilder sb = new StringBuilder();

    int start = 0;
    int end = sequence.length() - 1;

    for ( int i = end; i >= start; i-- )
    {
      char oc = sequence.charAt( i );
      char nc = complement( oc );
      sb.append( nc );
    }

    return sb.toString();
  }


  public static void main( String[] args )
  {
    String sequence = "AaAaAGCTTTTTTTTTTTNXXX";

    System.out.println( "RNA" );
    System.out.println( "---" );
    System.out.println( "Sequence   : " + sequence );
    System.out.println( "Complement : " + RNA.complement( sequence ) );
    System.out.println( "Reverse    : " + RNA.reverse( sequence ) );
    System.out.println( "RC         : " + RNA.reverseComplement( sequence ) );
    System.out.println( "GC content : " + RNA.getGCCount( sequence ) );
    System.out.println();
    System.out.println( "DNA" );
    System.out.println( "---" );
    System.out.println( "Sequence   : " + sequence );
    System.out.println( "Complement : " + DNA.complement( sequence ) );
    System.out.println( "Reverse    : " + DNA.reverse( sequence ) );
    System.out.println( "RC         : " + DNA.reverseComplement( sequence ) );
    System.out.println( "GC content : " + DNA.getGCCount( sequence ) );
  }
}
