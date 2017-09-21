
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

/**
 *
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class Query {

    public byte[] sequence;
    public Chart chart;
    public int index;
    public int other;

    public Query(byte[] sequence, Chart chart, int index, int other){
        this.sequence = sequence;
        this.chart = chart;
        this.index = index;
        this.other = other;
    }

}//end class.
