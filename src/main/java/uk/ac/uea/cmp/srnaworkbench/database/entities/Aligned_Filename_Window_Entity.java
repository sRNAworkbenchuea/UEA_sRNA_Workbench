package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Models an Alignment Window for a specific file
 * @author Matthew
 */
@Table(name="Aligned_Filename_Windows")
@Entity
public class Aligned_Filename_Window_Entity implements Serializable {
    @EmbeddedId
    Id id;
    
    @Column(name="Strand_Bias")
    private double strandBias;
    
    @Column(name="Abundance")
    private double abundance;
    
    public Aligned_Filename_Window_Entity(){}
    
    public Aligned_Filename_Window_Entity(Alignment_Window_Entity window, Filename_Entity filename, double strandBias, double abundance)
    {
        this.id = new Aligned_Filename_Window_Entity.Id(window, filename);
        this.strandBias = strandBias;
        this.abundance = abundance;
    }
    
    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public double getStrandBias() {
        return strandBias;
    }

    public void setStrandBias(double strandBias) {
        this.strandBias = strandBias;
    }

    public double getAbundance() {
        return abundance;
    }

    public void setAbundance(double abundance) {
        this.abundance = abundance;
    }
    
    @Embeddable
    public static class Id implements Serializable{
        @ManyToOne()
        @JoinColumns({
            @JoinColumn(name="seqid"),
            @JoinColumn(name="reference"),
            @JoinColumn(name="windowId"),
            @JoinColumn(name="windowLength")
        })  
        private Alignment_Window_Entity alignment_window;
        
        @ManyToOne
        @JoinColumn(name="Filename")
        private Filename_Entity filename;
        
        public Id(){}
        
        public Id(Alignment_Window_Entity alignment_window, Filename_Entity filename) {
            this.alignment_window = alignment_window;
            this.filename = filename;
        }

        public Alignment_Window_Entity getAlignment_window() {
            return alignment_window;
        }

        public void setAlignment_window(Alignment_Window_Entity alignment_window) {
            this.alignment_window = alignment_window;
        }

        public Filename_Entity getFilename() {
            return filename;
        }

        public void setFilename(Filename_Entity filename) {
            this.filename = filename;
        }

        @Override
        public String toString() {
            return "Id{" + "alignment_window=" + alignment_window + ", filename=" + filename.getName() + '}';
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 19 * hash + Objects.hashCode(this.alignment_window);
            hash = 19 * hash + Objects.hashCode(this.filename);
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
            final Id other = (Id) obj;
            if (!Objects.equals(this.alignment_window, other.alignment_window)) {
                return false;
            }
            if (!Objects.equals(this.filename, other.filename)) {
                return false;
            }
            return true;
        }
        
    }

    @Override
    public String toString() {
        return "Aligned_Filename_Window_Entity{" + id + ", strandBias=" + strandBias + ", abundance=" + abundance + '}';
    }
    
    
}
