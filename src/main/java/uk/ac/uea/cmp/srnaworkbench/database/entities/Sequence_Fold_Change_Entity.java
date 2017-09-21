package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author Matthew
 */
@Table(name="Sequence_Fold_Changes")
@Entity
public class Sequence_Fold_Change_Entity implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="Sequence_Fold_Change_ID")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="Unique_Sequence")
    Unique_Sequences_Entity uniqueSequence;
    
    @ManyToOne
    @JoinColumns({@JoinColumn(name="referenceSample"), @JoinColumn(name="pairKey"), @JoinColumn(name="observedSample")})
    private Sample_Pair_Entity samplePair;
    
    @ManyToOne
    @JoinColumn(name="Sequence_Pattern_FK")
    private Sequence_Pattern_Entity sequence_pattern;

    public Sequence_Pattern_Entity getSequencePattern() {
        return sequence_pattern;
    }

    public void setSequencePattern(Sequence_Pattern_Entity sequence_pattern) {
        this.sequence_pattern = sequence_pattern;
    }
    
    @OneToMany(mappedBy="sequence")
    @MapKey(name="normType")
    private Map<NormalisationType, Expression_Fold_Change_Entity> foldChanges = new HashMap<>();

    public Unique_Sequences_Entity getUniqueSequence() {
        return uniqueSequence;
    }

    public void setUniqueSequence(Unique_Sequences_Entity uniqueSequence) {
        this.uniqueSequence = uniqueSequence;
    }

    public Sample_Pair_Entity getSamplePair() {
        return samplePair;
    }

    public void setSamplePair(Sample_Pair_Entity samplePair) {
        this.samplePair = samplePair;
    }

    public Map<NormalisationType, Expression_Fold_Change_Entity> getFoldChanges() {
        return foldChanges;
    }

    public void setFoldChanges(Map<NormalisationType, Expression_Fold_Change_Entity> foldChanges) {
        this.foldChanges = foldChanges;
    }

    @Override
    public String toString() {
        return "Sequence_Fold_Change_Entity{" + "id=" + id + ", uniqueSequence=" + uniqueSequence.getRNA_Sequence() + ", samplePair=" + samplePair.getSamplePair().getPairKey() + ", foldChanges=" + foldChanges + '}';
    }
    
    
    
    
}
