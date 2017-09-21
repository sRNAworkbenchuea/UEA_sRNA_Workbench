package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Models a table used to store Reference Sequence Sets used in this 
 * database.
 * 
 * A reference sequence set is a set of sequences that the sRNAs are aligned to
 * For example a reference set called "genome" is the set of chromosome sequences
 * to which sRNAs are aligned to find genome-mapped sRNAs. The reference set
 * "miRBase" might be the set of miRNA hairpin sequences found in miRBase
 * 
 * At the moment the table only conveys additional information of the reference name
 * and its priority, with convenience mappings back to the annotation types
 * and aligned sequences found for this reference set.
 * 
 * @author Matthew Beckers
 */
@Entity
@Table(name="Reference_Sequence_Sets")
public class Reference_Sequence_Set_Entity implements Serializable {
    @Id
    @Column(name="Reference_Set_Name")
    String referenceSetName;
    
    // the priority of this reference when resolving reference conflicts for mapped
    // reads. Not to be confused with the type priority.
    @Column(name="Reference_Priority")
    int referencePriority;
    
    @Column(name="Has_Primary_Type", nullable=false)
    boolean hasPrimaryType = false;
    
    /**
     * Set to an annotation type if hasPrimaryType is true.
     * If this is the case, all other annotation types mapped by annotationTypes
     * are ignored.
     */
    //@MapsId("id")
    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumns({
        @JoinColumn(name="type", nullable=true),
        @JoinColumn(name="reference", nullable=true)
    })
    Annotation_Type_Entity primaryType = null;
    
    // The annotation types available for this reference set
    @OneToMany(fetch = FetchType.LAZY, mappedBy="id.reference", cascade=CascadeType.ALL)
    Set<Annotation_Type_Entity> annotationTypes= new HashSet<>(0);
    
    // The aligned sequences for this reference set
    @OneToMany(mappedBy="id.reference_sequence")
    Set<Aligned_Sequences_Entity> alignedSequences;
    
    public Reference_Sequence_Set_Entity(){}

    public Reference_Sequence_Set_Entity(String referenceSetName, int priority) {
        this.referenceSetName = referenceSetName;
        this.referencePriority = priority;
    }

    public boolean isHasSingleType() {
        return hasPrimaryType;
    }

    public void setHasPrimaryType(boolean hasSingleType) {
        this.hasPrimaryType = hasSingleType;
    }

    public Annotation_Type_Entity getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(Annotation_Type_Entity primaryType) {
        this.primaryType = primaryType;
    }

    public String getReferenceSetName() {
        return referenceSetName;
    }

    public void setReferenceSetName(String referenceSetName) {
        this.referenceSetName = referenceSetName;
    }

    public int getReferencePriority() {
        return referencePriority;
    }

    public void setReferencePriority(int priority) {
        this.referencePriority = priority;
    }

    public Set<Annotation_Type_Entity> getAnnotationTypes() {
        return annotationTypes;
    }

    public void setAnnotationTypes(Set<Annotation_Type_Entity> annotationTypes) {
        this.annotationTypes = annotationTypes;
    }

    public Set<Aligned_Sequences_Entity> getAlignedSequences() {
        return alignedSequences;
    }

    public void setAlignedSequences(Set<Aligned_Sequences_Entity> alignedSequences) {
        this.alignedSequences = alignedSequences;
    }

    @Override
    public String toString() {
        return "Reference " + referenceSetName + ", priority " + referencePriority;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.referenceSetName);
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
        final Reference_Sequence_Set_Entity other = (Reference_Sequence_Set_Entity) obj;
        if (!Objects.equals(this.referenceSetName, other.referenceSetName)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
