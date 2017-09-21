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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * Models the confidence interval of a sequence from a sample
 *  - If two replicates, min and max are the lowest and highest expression
 *  - if more than two, real confidence intervals are used
 *  - if one replicate, min, max and avg are the same
 * @author Matthew
 */
@Table(name="Sample_Sequences")
@Entity
public class Sample_Sequence_Entity implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="Id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="Unique_Sequence")
    @Index(name="sequenceIndex")
    private Unique_Sequences_Entity sequence;
    
    @ManyToOne
    @JoinColumn(name="sample_id")
    private Sample_Entity sample;
    
    @OneToMany(mappedBy="sequence", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @MapKey(name="normType")
    private Map<NormalisationType, Expression_CI_Entity> confidenceIntervals = new HashMap<>();

    public Unique_Sequences_Entity getSequence() {
        return sequence;
    }

    public void setSequence(Unique_Sequences_Entity unique_sequence) {
        this.sequence = unique_sequence;
    }

    public Sample_Entity getSample() {
        return sample;
    }

    public void setSample(Sample_Entity sample) {
        this.sample = sample;
    }

    public Map<NormalisationType, Expression_CI_Entity> getConfidenceIntervals() {
        return confidenceIntervals;
    }

    public void setConfidenceIntervals(Map<NormalisationType, Expression_CI_Entity> confidenceIntervals) {
        this.confidenceIntervals = confidenceIntervals;
    }
    
    public Expression_CI_Entity getConfidenceInterval(NormalisationType normType)
    {
        return this.confidenceIntervals.get(normType);
    }

    @Override
    public String toString() {
        return "Sample_Sequence_Entity{" + "id=" + id + ", RNA_Sequence=" + sequence.getRNA_Sequence() + ", sample=" + sample + '}';
    }
    
}
