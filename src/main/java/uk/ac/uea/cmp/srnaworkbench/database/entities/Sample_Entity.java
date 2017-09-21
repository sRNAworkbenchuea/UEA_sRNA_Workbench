package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Models a sample. A sample is a collection of replicates for the same treatment
 * @author Matthew
 */
@Table(name="Samples")
@Entity
public class Sample_Entity implements Serializable {
    
    @Id
    @Column(name="Sample_ID") // This is usually the treatment name
    String sampleId;
    
    @Column(name="Sample_Number")
    int sampleNumber;
    
    @OneToMany(mappedBy="sample", cascade=CascadeType.ALL, fetch=FetchType.EAGER) // These are the replicate files that make up one sample
    List<Filename_Entity> filenames = new ArrayList<>();
       
    public Sample_Entity(){}
    
    /**
     * Sample number is used to correctly order the samples on output fromt he database
     * Without this, the naturaly ordering of the samples will not be insertion order due
     * to the id being made up of characters
     * @param sample_id
     * @param sampleNumber 
     */
    public Sample_Entity(String sample_id, int sampleNumber)
    {
        this.sampleId = sample_id;
        this.sampleNumber = sampleNumber;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public List<Filename_Entity> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<Filename_Entity> filenames) {
        this.filenames = filenames;
    }

    public int getSampleNumber() {
        return sampleNumber;
    }

    public void setSampleNumber(int sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    @Override
    public String toString() {
        return "Sample_Entity{" + "sampleId=" + sampleId + ", filenames=" + filenames + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.sampleId);
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
        final Sample_Entity other = (Sample_Entity) obj;
        if (!Objects.equals(this.sampleId, other.sampleId)) {
            return false;
        }
        return true;
    }
    
    
}
