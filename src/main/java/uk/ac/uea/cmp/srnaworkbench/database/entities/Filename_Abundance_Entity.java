package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * A table for total abundances of all files for each normalisation type
 * @author matt
 */
@Entity
@Table(name="FILENAME_ABUNDANCES")
public class Filename_Abundance_Entity implements Serializable
{
    @EmbeddedId
    private NormalisedFileID id; 
    
    @Column(name="Total_Abundance")
    private double totalAbundance;

    public Filename_Abundance_Entity(){}
    public Filename_Abundance_Entity(Filename_Entity file, NormalisationType normType, double totalAbundance) {
        this.id = new NormalisedFileID(file, normType);
        this.totalAbundance = totalAbundance;
    }

    public Filename_Entity getFilename()
    {
        return id.getFilename_entity();
    }
    
    public NormalisationType getNormType()
    {
        return id.getNormType();
    }

    public double getTotalAbundance() {
        return totalAbundance;
    }

    public void setTotalAbundance(double totalAbundance) {
        this.totalAbundance = totalAbundance;
    }
    
    @Override
    public String toString()
    {
        return this.id.toString() + ", " + this.totalAbundance; 
    }
     
    


@Embeddable
public static class NormalisedFileID implements Serializable
{
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "File_Name", insertable = false, updatable = false)    
   private Filename_Entity filename_entity;

   @Enumerated
   private NormalisationType normType;

   public NormalisedFileID(){};
    public NormalisedFileID(Filename_Entity filename_entity, NormalisationType normType) {
        this.filename_entity = filename_entity;
        this.normType = normType;
    }

    public Filename_Entity getFilename_entity() {
        return filename_entity;
    }

    public void setFilename_entity(Filename_Entity filename_entity) {
        this.filename_entity = filename_entity;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.filename_entity);
        hash = 47 * hash + Objects.hashCode(this.normType);
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
        final NormalisedFileID other = (NormalisedFileID) obj;
        if (!Objects.equals(this.filename_entity, other.filename_entity)) {
            return false;
        }
        if (this.normType != other.normType) {
            return false;
        }
        return true;
    }
    
   @Override
    public String toString(){
        return this.filename_entity.getFilename() + ", " + this.normType.toString();
    }
   
   
}
}
