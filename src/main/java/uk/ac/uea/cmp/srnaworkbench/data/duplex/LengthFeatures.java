/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

/**
 *
 * @author ezb11yfu
 */
// Sequence length features
public class LengthFeatures
{
    public int len_mirna;
    public int len_mirna_star;
    public int len_diff;

    public LengthFeatures()
    {
        this(21,21,0);
    }

    public LengthFeatures(int len_mirna, int len_mirna_star, int len_diff)
    {
        this.len_mirna = len_mirna;
        this.len_mirna_star = len_mirna_star;
        this.len_diff = len_diff;
    }
}
