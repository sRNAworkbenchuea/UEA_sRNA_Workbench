
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

/**
 * This class represents a Node within a Tree and contains the recursive methods
 * implementing the algorithms for Tree construction and Tree search.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
final class Node
{
    static Node createRoot()
    {
      return createRoot( (byte)0 );
    }

    static Node createRoot( byte edge )
    {
      return new Node( null, edge );
    }

    public Node( Node parent, byte edge )
    {
      this.parent = parent;
      this.edge   = edge;
    }

    final Node parent;
    final byte edge;

    /** Edges. **/
    protected Node edgeA;
    protected Node edgeC;
    protected Node edgeG;
    protected Node edgeT;
    protected boolean isTerminator;
    protected Terminator terminator;
}
