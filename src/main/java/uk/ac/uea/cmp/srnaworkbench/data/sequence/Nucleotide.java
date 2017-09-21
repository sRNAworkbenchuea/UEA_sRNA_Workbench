package uk.ac.uea.cmp.srnaworkbench.data.sequence;

/**
 *
 * @author mka07yyu
 */
public enum Nucleotide {
    A, T, C, G, N;
    
    public static Nucleotide fromChar(char c) throws IllegalNucleotideException
    {
        switch(c)
        {
            case 'T': case 'U':
                return Nucleotide.T;
            case 'A':
                return Nucleotide.A;
            case 'C':
                return Nucleotide.C;
            case 'G':
                return Nucleotide.G;
            case 'N':
                return Nucleotide.N;
            default:
                throw new IllegalNucleotideException(c);
        }
    }


}
