/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseHeader 
{
    private MirBaseCodeEntry mircode;
    private String mirid;
    private String binomial_latin_name;
    private String mirfam;
    private String seqtype;
    private String seq;     // Not strictly part of the header, but worth having as a place holder.
    
    public MirBaseHeader(MirBaseCodeEntry mircode, String mirid, String name, String mirfam, String seqtype) 
    {
        this.mircode = mircode;
        this.mirid = mirid;
        this.binomial_latin_name = name;
        this.mirfam = mirfam;
        this.seqtype = seqtype;
    }
    
    public String getMirid() {
        return mirid;
    }


    public String getSeqtype() {
        return seqtype;
    }


    public String getMirfam() {
        return mirfam;
    }


    public String getBinomialLatinName() {
        return binomial_latin_name;
    }
    
    public MirBaseCodeEntry getMircode() {
        return mircode;
    }
    
    public String getSeq() {
        return seq;
    }
    
    public void setSeq(String seq)
    {
        this.seq = seq;
    }

    
    public static MirBaseHeader parse(String header)
    {
        String[] parts = header.split(" ");
        
        MirBaseCodeEntry mircode = MirBaseCodeEntry.parseCode(parts[0].toLowerCase());
        String mirid = parts[1];
        String name = parts[2] + " " + parts[3];
        String mirfam = parts[4];
        String seqtype = "";
        if (parts.length >= 6)
        {
            seqtype = "stem-loop";
        }
        else
        {
            seqtype = "mature";
        }
        
        return new MirBaseHeader(mircode, mirid, name, mirfam, seqtype);
    }
}
