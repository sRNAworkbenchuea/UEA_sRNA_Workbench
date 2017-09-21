/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

/**
 *
 * @author ezb11yfu
 */
// Nucleotide composition
public class NucleotideCompositionFeatures
{
    public char mirna_base1;
    public char mirna_star_base1;
    public double mirna_percent_a;
    public double mirna_percent_u;
    public double mirna_percent_g;
    public double mirna_percent_c;
    public double mirna_star_percent_a;
    public double mirna_star_percent_u;
    public double mirna_star_percent_g;
    public double mirna_star_percent_c;

    public NucleotideCompositionFeatures()
    {
        this('U','U',25.0,25.0,25.0,25.0,25.0,25.0,25.0,25.0);
    }

    public NucleotideCompositionFeatures(char mirna_base1, char mirna_star_base1,
            double mirna_percent_a, double mirna_percent_u, double mirna_percent_g, double mirna_percent_c,
            double mirna_star_percent_a, double mirna_star_percent_u, double mirna_star_percent_g, double mirna_star_percent_c)
    {
        this.mirna_base1 = mirna_base1;
        this.mirna_star_base1 = mirna_star_base1;
        this.mirna_percent_a = mirna_percent_a;
        this.mirna_percent_u = mirna_percent_u;
        this.mirna_percent_g = mirna_percent_g;
        this.mirna_percent_c = mirna_percent_c;
        this.mirna_star_percent_a = mirna_star_percent_a;
        this.mirna_star_percent_u = mirna_star_percent_u;
        this.mirna_star_percent_g = mirna_star_percent_g;
        this.mirna_star_percent_c = mirna_star_percent_c;
    }
}
