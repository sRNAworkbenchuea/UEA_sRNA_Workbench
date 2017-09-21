/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author matt
 */

@Entity
@Table(name="SIZE_CLASSES")
public class Size_Class_Entity implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="ID")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name="File")
    private Filename_Entity file;
    
    @Column(name="Size_Class")
    private int sizeClass;
    
    @Column(name="Non_Redundant_Abundance")
    private int nrAbundance;
    
    @Column(name="Redundant_Abundance")
    private double rAbundance;
    
    @Enumerated
    private NormalisationType normType;
    
    @Enumerated
    private MappingStatus mapped;
    
    @Column(name="AnnotationType")
    private String type;
    
    public Size_Class_Entity(){}

    public Size_Class_Entity(Filename_Entity file, NormalisationType normType, int sizeClass, double rAbundance, int nrAbundance, boolean isMapped, String type) {
        this.file = file;
        this.sizeClass = sizeClass;
        this.rAbundance = rAbundance;
        this.nrAbundance = nrAbundance;
        this.normType = normType;
        this.mapped = MappingStatus.toMappingStatus(isMapped);
        this.type = type;
    }

    public Filename_Entity getFile() {
        return file;
    }

    public void setFile(Filename_Entity file) {
        this.file = file;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSizeClass() {
        return sizeClass;
    }

    public void setSizeClass(int sizeClass) {
        this.sizeClass = sizeClass;
    }

    public double getRedundantAbundance() {
        return rAbundance;
    }

    public void setRedundantAbundance(double rAbundance) {
        this.rAbundance = rAbundance;
    }
    
    public int getNonRedundantAbundance() {
        return nrAbundance;
    }

    public void setNonRedundantAbundance(int nrAbundance) {
        this.nrAbundance = nrAbundance;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public boolean isMapped() {
        return mapped.isMapped();
    }
    
    public MappingStatus getMappingStatus()
    {
        return this.mapped;
    }

    public void setMapped(boolean isMapped) {
        this.mapped = MappingStatus.toMappingStatus(isMapped);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.file);
        hash = 23 * hash + this.sizeClass;
        hash = 23 * hash + this.nrAbundance;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.rAbundance) ^ (Double.doubleToLongBits(this.rAbundance) >>> 32));
        hash = 23 * hash + Objects.hashCode(this.normType);
        hash = 23 * hash + (this.mapped.isMapped() ? 1 : 0);
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
        final Size_Class_Entity other = (Size_Class_Entity) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (this.sizeClass != other.sizeClass) {
            return false;
        }
        if (this.nrAbundance != other.nrAbundance) {
            return false;
        }
        if (Double.doubleToLongBits(this.rAbundance) != Double.doubleToLongBits(other.rAbundance)) {
            return false;
        }
        if (this.normType != other.normType) {
            return false;
        }
        if (this.mapped != other.mapped) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getFile().getFileID()).append(",").append(this.getNormType())
                .append("; ").append(this.getSizeClass()).append(": ")
                .append(this.getRedundantAbundance()).append(", ")
                .append(this.getNonRedundantAbundance()).append(", mapped: ").append(this.isMapped());
        
        return sb.toString();
    }
    
    /**
     * Specifies a mapping status i.e. whether the current entity is aligned to
     * a reference sequence or not.
     *
     * @param mapped
     * @return
     */
    public enum MappingStatus
    {
        MAPPED, UNMAPPED;
        
        public boolean isMapped()
        {
            switch(this)
            {
                case MAPPED:
                    return true;
                default:
                    return false;
            }
        }
        
        @Override
        public String toString()
        {
            switch(this)
            {
                case MAPPED:
                    return "Mapped";
                case UNMAPPED:
                    return "Unmapped";
                default:
                    return "Uknown Mapping Status";
            }
        }
        

        public static MappingStatus toMappingStatus(boolean mapped)
        {
            if(mapped)
            {
                return MappingStatus.MAPPED;
            }
            else
            {
                return MappingStatus.UNMAPPED;
            }
        }
    }
    
    
}
