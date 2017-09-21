package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;

/**
 *
 * @author matt
 */
@Table(name="GFF")
@Entity
public class GFF_Entity implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;
    
    @Embedded
    private Aligned_Sequences_Entity.Id alignment;
    
    @Column(name="source")
    private String source;
    
//    private String type;
//    @MapsId("id")
    @ManyToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns({
        @JoinColumn(name = "type"),
        @JoinColumn(name = "reference")
    })
    Annotation_Type_Entity type;
    
    @Column(name="score")
    private float score;
    
    @Column(name="phase")
    private GFFRecord.GFFPhase phase;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "SEQUENCE_GFF", joinColumns = {
        @JoinColumn(name = "GFF_ID")}, inverseJoinColumns = {
        @JoinColumn(name="CHROM"),
        @JoinColumn(name= "SEQ_END"),
        @JoinColumn(name="REFERENCE"),
        @JoinColumn(name = "SEQ_START"),
        @JoinColumn(name="STRAND")})
    private Set<Aligned_Sequences_Entity> annotatedSequences = new HashSet<>(0);
    public Set<Aligned_Sequences_Entity> getAnnotatedSequences()
    {
        return this.annotatedSequences;
    }
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="gff")
    private Set<GFF_Attribute_Entity> attributes = new HashSet<>();
    
    public GFF_Entity(){}
    
    /**
     * Create this entity from a GFFRecord
     * @param gffRecord 
     * @param typeEntity 
     */
    public GFF_Entity(GFFRecord gffRecord, Annotation_Type_Entity typeEntity)
    {
        this.alignment = new Aligned_Sequences_Entity.Id(gffRecord.getStartIndex(), gffRecord.getEndIndex(), 
                gffRecord.getSeqid(), Character.toString(gffRecord.getSequenceStrand().getCharCode()), gffRecord.getSource());
        
        this.source = gffRecord.getSource();
        this.type = typeEntity;
        
        this.score = gffRecord.getScore();
        this.phase = gffRecord.getPhase();
        
        for(Entry<String, String> e : gffRecord.getAttributes().entrySet())
        {
            attributes.add(new GFF_Attribute_Entity(e.getKey(), e.getValue()));
        }
    }
    
    /**
     * Get the type held in this GFF as a GFF_Type_Entity
     * @return 
     */
    public Annotation_Type_Entity getType()
    {
        return this.type;
    }
    
    public GFFRecord getRecord()
    {
        GFFRecord rec = new GFFRecord(alignment.getChrom(), source, type.getId().getType(), alignment.getStart(), alignment.getEnd(), 
                score, alignment.getStrand().charAt(0), ((Integer) phase.getPhaseValue()).byteValue());
        Iterator<GFF_Attribute_Entity> attrIt = attributes.iterator();
        while(attrIt.hasNext())
        {
            GFF_Attribute_Entity e = attrIt.next();
            rec.addAttribute(e.getKey(), e.getValue());
        }
        
        // add attritbutes
        
        return rec;
    }
    
    public Aligned_Sequences_Entity.Id getId()
    {
        return this.alignment;
    }

    public void addAnnotation(Aligned_Sequences_Entity curr_q)
    {
        annotatedSequences.add(curr_q);
    }
}
