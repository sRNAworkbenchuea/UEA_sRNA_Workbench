/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseEntry
{
    private Map<String,String> mirna_seqs;
    private Map<String,String> mirna_star_seqs;
    private Map<String,String> hairpin_seqs;
    private String mirna_id;
    
    public MirBaseEntry()
    {
        this("", null, null, null);
    }

    
    public MirBaseEntry(String mirna_id, 
            Map<String,String> mirna_seqs, Map<String,String> mirna_star_seqs, Map<String,String> hairpin_seqs)
    {
        this.mirna_id = mirna_id;
        this.mirna_seqs = mirna_seqs == null ? new HashMap<String, String>() : mirna_seqs;
        this.mirna_star_seqs = mirna_star_seqs == null ? new HashMap<String, String>() : mirna_star_seqs;       
        this.hairpin_seqs = hairpin_seqs == null ? new HashMap<String, String>() : hairpin_seqs;;
    }

    public String getMirnaID() {
        return this.mirna_id;
    }
    
    public Map<String,String> getMirnaSeqs() {
        return mirna_seqs;
    }
    
    public Map<String,String> getMirnaStarSeqs() {
        return mirna_star_seqs;
    }
    
    public Map<String,String> getHairpinSeqs() {
        return hairpin_seqs;
    }
    
    public void addHairpinSeq(String id, String seq) 
    {
        if (this.hairpin_seqs == null)
            this.hairpin_seqs = new HashMap<String,String>();
        if (this.hairpin_seqs.get(id) != null)
            throw new IllegalArgumentException("Hairpin variant: " + id + ", already in this mirbase entry.");
        this.hairpin_seqs.put(id, seq);
    }
    
    public void addMirnaSeq(String id, String seq) 
    {
        if (this.mirna_seqs == null)
            this.mirna_seqs = new HashMap<String,String>();
        if (this.mirna_seqs.get(id) != null)
            throw new IllegalArgumentException("miRNA variant: " + id + ", already in this mirbase entry.");
        this.mirna_seqs.put(id, seq);
    }
        
    public void addMirnaStarSeq(String id, String seq) 
    {
        if (this.mirna_star_seqs == null)
            this.mirna_star_seqs = new HashMap<String,String>();
        if (this.mirna_star_seqs.get(id) != null)
            throw new IllegalArgumentException("miRNA* variant: " + id + ", already in this mirbase entry.");
        this.mirna_star_seqs.put(id, seq);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ID: " + this.mirna_id + "\n");
        
        StringBuilder mirnas = new StringBuilder();
        if (mirna_seqs != null)
        {
            for (String variant : new TreeSet<String>(mirna_seqs.keySet()))
            {
                mirnas.append(variant + "(" + mirna_seqs.get(variant) + ") ");
            }
        }
        
        StringBuilder mirna_stars = new StringBuilder();
        if (mirna_star_seqs != null)
        {
            for (String variant : new TreeSet<String>(mirna_star_seqs.keySet()))
            {
                mirna_stars.append(variant + "(" + mirna_star_seqs.get(variant) + ") ");
            }
        }
        
        StringBuilder hairpins = new StringBuilder();
        if (hairpin_seqs != null)
        {
            for (String variant : new TreeSet<String>(hairpin_seqs.keySet()))
            {
                hairpins.append(variant + "(" + hairpin_seqs.get(variant) + ") ");
            }
        }
        
        sb.append("miRNAs: " + mirnas.toString() + "\n");
        sb.append("miRNA*s: " + mirna_stars.toString() + "\n");
        sb.append("Hairpins: " + hairpins.toString() + "\n");
        return sb.toString();
    }
}
