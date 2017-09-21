package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity.TypePK;

/**
 * Holds annotation types available in this database.
 * 
 * An annotation type is a name for a type of annotation from a particular reference
 * e.g. an "exon" type from the reference "genome".
 * 
 * Each type has a priority id which allows ties between annotations to be resolved
 * @author Matthew Beckers
 */
@Table(name="ANNOTATION_TYPES")
@Entity
//@IdClass(TypePK.class)
public class Annotation_Type_Entity implements Serializable  {
    
    // Annotation_Type has a composite id of type and foreign association reference.
    // this implemented using Hibernate-specific two Id annotations.
    // This means that the id type for this class is itself i.e. Annotation_Type_Entity.class
    @EmbeddedId
    private TypePK id;
    
    
    // The priority is used when deciding which type
    // the unique sequence should resolve to. Lower numbers
    // indicate a higher priority.
    @Column(name="Priority")
    private int priority;
    
    @ManyToMany(mappedBy="types") 
    private Set<Annotation_Type_Keyword_Entity> keywords = new HashSet<>(0);
    
    public Annotation_Type_Entity(){}
    public Annotation_Type_Entity(Reference_Sequence_Set_Entity referenceSequence, String type, int priority) {
        this.id = new TypePK(referenceSequence, type);
        this.priority = priority;
    }

    public TypePK getId() {
        return id;
    }

    public void setId(TypePK id) {
        this.id = id;
    }
    

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<Annotation_Type_Keyword_Entity> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<Annotation_Type_Keyword_Entity> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "GFF_Type_Entity{" + id + ", priority=" + priority + '}';
    }
    
    @Embeddable
    public static class TypePK implements Serializable 
    {
        @Column(name = "Annotation_Type")
        private String type;

        @ManyToOne(cascade=CascadeType.ALL)
        @JoinColumn(name = "Reference_Set_Name", insertable = false, updatable = false)
        private Reference_Sequence_Set_Entity reference;
        
        public TypePK(){}
        public TypePK(Reference_Sequence_Set_Entity reference, String type)
        {
            this.type = type;
            this.reference = reference;
        }
        
        public Reference_Sequence_Set_Entity getReference() {
            return reference;
        }

        public void setReference(Reference_Sequence_Set_Entity reference) {
            this.reference = reference;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.type);
            hash = 67 * hash + Objects.hashCode(this.reference);
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
            final TypePK other = (TypePK) obj;
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            if (!Objects.equals(this.reference, other.reference)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "TypePK{" + "type=" + type + ", reference=" + reference + '}';
        }
        
        
        
    }
    
    
}
