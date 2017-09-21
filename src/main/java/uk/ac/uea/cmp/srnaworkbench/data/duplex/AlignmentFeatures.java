/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

/**
 *
 * @author ezb11yfu
 */
// Alignment properties
public class AlignmentFeatures
{
    public double complementarity_score;
    public int nb_mismatches;
    public int nb_g_u_pairs;
    public int nb_bulges;
    public int len_3p_overhang;
    public int len_5p_overhang;
    public double mfe;

    public AlignmentFeatures()
    {
        this(0.0, 2, 2, 0.0, 0, 0, 0);
    }

    public AlignmentFeatures(double mfe, int len_5p_overhang, int len_3p_overhang, 
            double complementarity_score, int nb_mismatches, int nb_g_u_pairs, int nb_bulges)
    {
        this.mfe = mfe;
        this.len_5p_overhang = len_5p_overhang;
        this.len_3p_overhang = len_3p_overhang;
        this.complementarity_score = complementarity_score;
        this.nb_mismatches = nb_mismatches;
        this.nb_g_u_pairs = nb_g_u_pairs;
        this.nb_bulges = nb_bulges;
    }
}

