package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Matthew
 */
@Table(name="SamplePairs")
@Entity
public class Sample_Pair_Entity implements Serializable {
    @EmbeddedId
    private FilePair samplePair;
    
    @OneToOne
    @JoinColumn(name="Reference_Sample")
    private Sample_Entity reference;
    
    @OneToOne
    @JoinColumn(name="Observed_Sample")
    private Sample_Entity observed;
    
    @ManyToMany(mappedBy = "samplePairs")
    Set<Sample_Series_Entity> series = new HashSet<>();
    
    public Sample_Pair_Entity() {}
    
    public Sample_Pair_Entity(Sample_Entity reference, Sample_Entity observed)
    {
        this.reference = reference;
        this.observed = observed;
        this.samplePair = new FilePair(reference.getSampleId(), observed.getSampleId());
    }

    public FilePair getSamplePair() {
        return samplePair;
    }

    public void setSamplePair(FilePair samplePair) {
        this.samplePair = samplePair;
    }

    public Sample_Entity getReference() {
        return reference;
    }

    public void setReference(Sample_Entity reference) {
        this.reference = reference;
    }

    public Sample_Entity getObserved() {
        return observed;
    }

    public void setObserved(Sample_Entity observed) {
        this.observed = observed;
    }

    @Override
    public String toString() {
        return "Sample_Pair_Entity{" + "samplePair=" + samplePair + ", reference=" + reference.getSampleId() + ", observed=" + observed.getSampleId() + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.samplePair);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sample_Pair_Entity other = (Sample_Pair_Entity) obj;
        if (!Objects.equals(this.samplePair, other.samplePair)) {
            return false;
        }
        return true;
    }
    
    
}
