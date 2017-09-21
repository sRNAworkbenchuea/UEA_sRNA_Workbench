/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

import java.io.File;
import org.apache.commons.lang3.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author ezb11yfu
 */
public class Duplex implements Comparable
{
    private String id1;
    private String s1;
    private int abd1;
    private String id2;
    private String s2;
    private int abd2;

    private int duplex_abundance;

    private File temp_dir;

    // Just consider the sequences when making the hashcode.
    // Is this too agressive?  i.e. potentially treating two duplexes the same when they are not?
    @Override
    public int hashCode()
    {
        int hash = 1;
        //hash *= 31 + (id1 == null ? 0 : id1.hashCode());
        hash += 31 + (s1 == null ? 0 : s1.hashCode());
        //hash *= 31 + (id2 == null ? 0 : id2.hashCode());
        hash += 31 + (s2 == null ? 0 : s2.hashCode());
        return hash;
    }

    @Override
    public int compareTo(Object other)
    {
        Duplex d = (Duplex)other;

        int s1d = this.s1.compareTo(d.s1);

        if (s1d == 0)
            return this.s2.compareTo(d.s2);
        else
            return s1d;
    }

    @Override
    public boolean equals(Object other)
    {
        Duplex d = (Duplex)other;

        if (this.s1.equals(d.s1) && this.s2.equals(d.s2))
            return true;
        else if (this.s1.equals(d.s2) && this.s2.equals(d.s1))
            return true;
        else
            return false;
    }

    public Duplex(String id1, String s1, String id2, String s2)
    {
        this(id1, s1, 1, id2, s2, 1, 1, new File(Tools.userDataDirectoryPath));
    }

    public Duplex(String id1, String s1, int abd1, String id2, String s2, int abd2, int duplex_abundance, File temp_dir)
    {
        this.id1 = id1;
        this.id2 = id2;

        // Ensure seqs are in RNA form
        this.s1 = s1.replace('T', 'U');
        this.s2 = s2.replace('T', 'U');

        this.abd1 = abd1;
        this.abd2 = abd2;

        setDuplexAbundance(duplex_abundance);

        this.temp_dir = temp_dir;
    }

    public String getID1()          {return this.id1;}
    public String getSeq1()         {return this.s1;}
    public String getDNASeq1()      {return this.s1.replace('U','T');}
    public int getAbundance1()      {return this.abd1;}
    public String getID2()          {return this.id2;}
    public String getSeq2()         {return this.s2;}
    public String getDNASeq2()      {return this.s2.replace('U','T');}
    public int getAbundance2()      {return this.abd2;}
    public int getDuplexAbundance() {return this.duplex_abundance;}

    public final void setDuplexAbundance(int abundance)
    {
        if (abundance < 1)
            throw new IllegalArgumentException("Duplex Error: Can't have a duplex with an abundance of less than 1");

        this.duplex_abundance = abundance;
    }

    public static Duplex parse(String line)
    {
        String[] parts = line.split(":");

        int d_a = Integer.parseInt(parts[0]);

        String[] seq1_parts = parts[1].split("-");
        String id1 = seq1_parts[0];
        String s1 = seq1_parts[1].substring(0, seq1_parts[1].indexOf("("));
        int abd1 = Integer.parseInt(seq1_parts[1].substring(seq1_parts[1].indexOf("(") + 1, seq1_parts[1].indexOf(")")));

        String[] seq2_parts = parts[2].split("-");
        String id2 = seq2_parts[0];
        String s2 = seq2_parts[1].substring(0, seq2_parts[1].indexOf("("));
        int abd2 = Integer.parseInt(seq2_parts[1].substring(seq2_parts[1].indexOf("(") + 1, seq2_parts[1].indexOf(")")));

        return new Duplex(id1,s1,abd1,id2,s2,abd2,d_a,new File(Tools.userDataDirectoryPath));
    }


    public int distance()
    {
        return StringUtils.getLevenshteinDistance( s1, s2 );
    }



    public DuplexFeatures buildFeatures(String uid, BinaryExecutor be, DuplexFeatures.MirnaClass is_mirna) throws Exception
    {
        return new DuplexFeatures(
                new LengthFeatures(s1.length(), s2.length(), s1.length() - s2.length()),
                new NucleotideCompositionFeatures(s1.charAt(0), s2.charAt(0),
                    nucleotideRatio(s1, 'A'), nucleotideRatio(s1, 'U'), nucleotideRatio(s1, 'G'), nucleotideRatio(s1, 'C'),
                    nucleotideRatio(s2, 'A'), nucleotideRatio(s2, 'U'), nucleotideRatio(s2, 'G'), nucleotideRatio(s2, 'C')),
                buildAlignmentFeatures(uid, be),
                is_mirna);
    }

    private static double nucleotideRatio(String seq, char nt)
    {
        int count = 0;
        for(int i = 0; i < seq.length(); i++)
        {
            if (seq.charAt(i) == nt)
                count++;
        }
        return (double)count / (double)seq.length();
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean dna_form)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.duplex_abundance);
        sb.append(":");
        sb.append(this.id1);
        sb.append("-");
        sb.append(dna_form ? this.getDNASeq1() : this.s1);
        sb.append("(");
        sb.append(this.abd1);
        sb.append("):");
        sb.append(this.id2);
        sb.append("-");
        sb.append(dna_form ? this.getDNASeq2() : this.s2);
        sb.append("(");
        sb.append(this.abd2);
        sb.append(")");
        return sb.toString();
    }


    private AlignmentFeatures buildAlignmentFeatures(String uid, BinaryExecutor be) throws Exception
    {
        double mfe;
        mfe = new RNACoFoldRunner(this.s1, this.s2, uid, be, this.temp_dir).run().getMFE();

        MuscleRunner mr = new MuscleRunner(this.s1, this.s2, uid, be, this.temp_dir);
        mr.run();

        String input_matrix = mr.getResult1();
        String hit_matrix = mr.getResult2();

        double alignment_score = 0.0;
        int gu = 0;
        int mismatch = 0;
        int bulge = 0;
        int input_overhang = 0, hit_overhang = 0;

        if (input_matrix.length() > 0 && hit_matrix.length() > 0)
        {

            for (int i = 0; i < input_matrix.length(); i++)
            {
                char input_element = input_matrix.charAt(i);
                char hit_element = hit_matrix.charAt(i);

                if (input_element == hit_element)
                {
                    alignment_score++;
                }
                else if ((input_element == 'A') && (hit_element == 'G'))
                {
                    alignment_score += 0.5;
                    gu++;
                }
                else if ((input_element == 'C') && (hit_element == 'U'))
                {
                    alignment_score += 0.5;
                    gu++;
                }
                else if ((input_element == '-') && (hit_element == '-'))
                {
                    //alignment_score += 0.0;
                }
                else
                {
                    alignment_score--;
                    mismatch++;
                }
            }


            // Remove any overhangs from both sequences

            int i = 0;
            while (input_matrix.charAt(i++) == '-');
            int input_begin = i-1;

            i = input_matrix.length()-1;
            while (input_matrix.charAt(i--) == '-');
            int input_end = i+2;
            input_overhang = Math.abs(input_begin - (input_matrix.length() - input_end));

            String trimmed_input = input_matrix.substring(input_begin, input_end);

            i = 0;
            while (hit_matrix.charAt(i++) == '-');
            int hit_begin = i-1;

            i = hit_matrix.length() - 1;
            while (hit_matrix.charAt(i--) == '-');
            int hit_end = i+2;
            hit_overhang = Math.abs(hit_begin - (hit_matrix.length() - hit_end));

            String trimmed_hit = hit_matrix.substring(hit_begin, hit_end);

            // Count the bulges in both sequences
            for(int j = 0; j < trimmed_input.length() ; j++)
            {
                if (trimmed_input.charAt(j) == '-')
                {
                    bulge++;
                }
            }

            for(int j = 0; j < trimmed_hit.length() ; j++)
            {
                if (trimmed_hit.charAt(j) == '-')
                {
                    bulge++;
                }
            }
        }

        return new AlignmentFeatures(mfe, input_overhang, hit_overhang, alignment_score,
                mismatch, gu, bulge);
    }
}
