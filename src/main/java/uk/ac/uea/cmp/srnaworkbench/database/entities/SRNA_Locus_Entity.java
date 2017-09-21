/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author w0445959
 */
@Entity
@Table(name = "SRNA_LOCUS_ENTITY")
public class SRNA_Locus_Entity implements Serializable 
{
    @Id
    @Column(name = "locus_ID", nullable=false)
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long locus_ID;
    
    @Column(name = "LOCUS_START", nullable = false)
    private int locus_start;
    
    @Column(name = "LOCUS_END", nullable = false)
    private int locus_end;
    
    @Column(name = "CHROM", nullable = false)
    private String chrom;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "locus_sequences", cascade = CascadeType.ALL)
    private Set<Aligned_Sequences_Entity> sequences = new HashSet<>(0);
    
    public SRNA_Locus_Entity()
    {
        
    }

    public SRNA_Locus_Entity(int start, int end, String chrom)
    {
        this.locus_start = start;
        this.locus_end = end;
        this.chrom = chrom;

    }

    public Long getLocus_ID()
    {
        return locus_ID;
    }

    public void setLocus_ID(Long locus_ID)
    {
        this.locus_ID = locus_ID;
    }

    public String getChrom()
    {
        return chrom;
    }

    public void setChrom(String chrom)
    {
        this.chrom = chrom;
    }

    public int getStart()
    {
        return locus_start;
    }

    public void setStart(int start)
    {
        this.locus_start = start;
    }

    public int getEnd()
    {
        return locus_end;
    }

    public void setEnd(int end)
    {
        this.locus_end = end;
    }

    public Set<Aligned_Sequences_Entity> getSequences()
    {
        return sequences;
    }

    public void setSequences(Set<Aligned_Sequences_Entity> sequences)
    {
        this.sequences = sequences;
    }
    
    public void addToSequences(Aligned_Sequences_Entity child) {
        child.setLocus_sequences(this);
        this.sequences.add(child);
    }
    
    public boolean containsSequence(String toFind)
    {
        for(Aligned_Sequences_Entity se : sequences )
        {
            if(se.getRna_seq().equals(toFind))
                return true;
        }
        return false;
    }
}
