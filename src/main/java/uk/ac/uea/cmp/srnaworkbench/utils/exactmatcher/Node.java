
package uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher;

/**
 * A Node in a tree.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
class Node
{
  private int level;

  private Node edgeA;
  private Node edgeC;
  private Node edgeG;
  private Node edgeT;
  private Node edgeU;

  private boolean isTerminator;

  /**
   * Constructs a new instance of Node which will be the root of the tree.
   * Note: use default constructor if not the root.
   * @param c The first sequence to be entered into the tree.
   * @param the level in the tree which this node is being created for.
   */
  public Node( char[] c, int level, ExactMatcher em )
  {
    this.level = level;
    addToTree( c, em );
  }

  /**
   * Adds a sequence to the tree.
   * @param c The sequence to be added to the tree.
   */
  public final void addToTree( char[] c, ExactMatcher em )
  {
    //If a terminator is found - save the abundance information.
    if ( this.level == c.length )
    {
      this.isTerminator = true;

      String s = String.copyValueOf( c );

      em.incrementShortReadAbundance( s );

      return;
    }

    // Get the edge for the nucleotide base
    Node edge = lookupEdge( c[ level ] );

    if ( edge == null )
    {
      switch ( c[level] )
      {
        case 'A': edgeA = new Node( c, level + 1, em ); break;
        case 'C': edgeC = new Node( c, level + 1, em ); break;
        case 'G': edgeG = new Node( c, level + 1, em ); break;
        case 'T': edgeT = new Node( c, level + 1, em ); break;
        case 'U': edgeU = new Node( c, level + 1, em ); break;
        default:
          System.err.println( "An unknown character was found. Character is : " + c[level] );
      }
    }
    else
    {
      edge.addToTree( c, em );
    }
  }

  /**
   * Align the sequence to the tree.
   * @param c The sequence.
   * @param currentPosition The current position.
   */
  public void align( char[] c, int currentPosition, ExactMatcher em )
  {
    if ( this.isTerminator )
    {
      //If we have reached a terminator node - then we have successfully mapped a long sequence to a short sequence.

      //name of pattern(the actual short sequence!),
      String seq = "";
      for ( int i = em.getStartPosition(); i < currentPosition; i++ )
      {
        seq += c[i];
      }

      //position of first matched base in database sequence, the sequence's beginning has position 1,
      //startPosition+1 because we are indexing from zero, but count from 1.
      //End stays the same because it is already +1 as we are at a terminator node.
      int start = em.getStartPosition() + 1;

      em.addResult( em.getGeneID(), seq, start, currentPosition );
      em.incrementShortReadAlignment( seq );

//            if(Data.CATEGORY_VERBOSE)
//            {
//                int position = 1159;//Type the position you are wanting deg tags aligned here.
//                if(start == position)
//                {
//                    System.out.println(s + " " + em.getShortReadAbundances().get(s) );
//                }
//            }
    }

    if ( currentPosition < c.length )
    {
      Node edge = lookupEdge( c[ currentPosition ] );

      if ( edge != null )
      {
        edge.align( c, currentPosition + 1, em );
      }
    }
  }

  /**
   * Get the edge for a particular nucleotide base
   *
   * @param nt The 'char' representing the nucleotide (A, C, G, T and U are accepted)
   * @return The Node object or null (for an invalid nucleotide or if the edge has not yet been constructed)
   */
  private Node lookupEdge( char nt )
  {
    Node edge = null;

    switch ( nt )
    {
      case 'A': edge = edgeA; break;
      case 'C': edge = edgeC; break;
      case 'G': edge = edgeG; break;
      case 'T': edge = edgeT; break;
      case 'U': edge = edgeU; break;
    }

    return edge;
  }
}
