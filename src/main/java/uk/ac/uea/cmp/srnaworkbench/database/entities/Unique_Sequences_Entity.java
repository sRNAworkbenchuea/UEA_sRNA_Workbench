/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 *
 * @author w0445959
 */
@Entity
@Table(name = "UNIQUE_SEQUENCES")
@NamedQueries(
{
    @NamedQuery(
            name = "@HQL_GET_ALL_UNIQUES",
            query = "from Unique_Sequences_Entity"
    )
})

@FilterDef(name = "getAligned")
@Filter(name = "getAligned", condition = "exists ( select 1 from Aligned_Sequences_Entity where RNA_Sequence=Aligned_Sequences_Entity.rna_sequence Limit 1 )")
//@Filter(name = "getAligned", condition = "alignedSequenceRelationships is not null")
public class Unique_Sequences_Entity implements Serializable
{
    @Id    
   // @Column(name = "RNA_Sequence", nullable=false, length = 50)
    @Column(name = "RNA_Sequence", nullable=false)
    private String RNA_Sequence;
    
    @Column(name = "RNA_Size", nullable=false)
    private int RNA_Size;
    
    @Column(name = "Total_Count", nullable=false)
    private int totalCount;
    
    
    
//    @Column(name="type")
//    private String annotationType = "None";
    
    // Lists the annotation type for this sequence found by
    // consensus of each of the alignment's annotations
    // Default is "None" until the sequence undergoes annotation
//    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumns({
        @JoinColumn(name = "reference"),
        @JoinColumn(name = "type")
    })
    private Annotation_Type_Entity type;
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "unique_sequence", cascade = CascadeType.ALL)
    @MapKey(name="filename")
    private Map<String, Sequence_Entity> sequenceRelationships = new HashMap<>(0);
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sequence", cascade = CascadeType.ALL)
    private Set<Sample_Sequence_Entity> sample_sequences = new HashSet<>(0);
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "aligned_sequence", cascade = CascadeType.ALL)
    private Set<Aligned_Sequences_Entity> alignedSequenceRelationships = new HashSet<>(0);
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "uniqueSequence", cascade = CascadeType.ALL)
    private Set<Sequence_Pattern_Entity> pattern = new HashSet<>(0);
    

    public Unique_Sequences_Entity(String RNA_Sequence, int RNA_Size, int totalCount) {
        this.RNA_Sequence = RNA_Sequence;
        this.RNA_Size = RNA_Size;
        this.totalCount = totalCount;
    }

    public Unique_Sequences_Entity()
    {
        
    }

   
    

    public Annotation_Type_Entity getConsensus_annotation_type() {
        return type;
    }

    public void setConsensus_annotation_type(Annotation_Type_Entity consensus_annotation_type) {
        this.type = consensus_annotation_type;
//        this.annotationType = consensus_annotation_type.getType();
    }

    public Set<Sequence_Pattern_Entity> getPattern() {
        return pattern;
    }

    public void setPattern(Set<Sequence_Pattern_Entity> pattern) {
        this.pattern = pattern;
    }
    
    public int getRNA_Size() {
        return RNA_Size;
    }

    public void setRNA_Size(int RNA_Size) {
        this.RNA_Size = RNA_Size;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getRNA_Sequence()
    {
        return RNA_Sequence;
    }

    public void setRNA_Sequence(String RNA_Sequence)
    {
        this.RNA_Sequence = RNA_Sequence;
    }

    public Map<String, Sequence_Entity> getSequenceRelationships()
    {
        return sequenceRelationships;
    }

    public void setSequenceRelationships(Map<String, Sequence_Entity> sequenceRelationships)
    {
        this.sequenceRelationships = sequenceRelationships;
    }

    public Set<Aligned_Sequences_Entity> getAlignedSequenceRelationships()
    {
        return alignedSequenceRelationships;
    }

    public void setAlignedSequenceRelationships(Set<Aligned_Sequences_Entity> alignedSequenceRelationships)
    {
        this.alignedSequenceRelationships = alignedSequenceRelationships;
    }

    public Set<Sample_Sequence_Entity> getSample_sequences() {
        return sample_sequences;
    }

    public void setSample_sequences(Set<Sample_Sequence_Entity> sample_sequences) {
        this.sample_sequences = sample_sequences;
    }
    
    @Override
    public String toString()
    {
        String str = this.RNA_Sequence + " " + this.totalCount + " " + this.getRNA_Size() + " " + this.getConsensus_annotation_type().getId().getType();
        return str;
    }
    
    
}
