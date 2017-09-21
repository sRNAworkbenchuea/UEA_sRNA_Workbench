package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Models a window amongst a group of sliding windows
 * @author Matthew
 */
@Table(name="Alignment_Windows")
@Entity
public class Alignment_Window_Entity implements Serializable {
    @EmbeddedId
    private Id id;
    
    // cascaded to alignment when persisting, but alignment should not be dropped when removed
    @OneToMany(mappedBy="id.alignment_window", fetch=FetchType.EAGER)
    private Set<Aligned_Filename_Window_Entity> aligned_filename_windows = new HashSet<>();
    
    @OneToMany(mappedBy="alignment_window", 
            //cascade=CascadeType.ALL, 
            fetch=FetchType.LAZY)
    private Set<Aligned_Sequences_Entity> aligned_sequences = new HashSet<>();

    public Alignment_Window_Entity(){}
    public Alignment_Window_Entity(Id id, Set<Aligned_Sequences_Entity> aligned_sequences) {
        this.id = id;
        this.aligned_sequences = aligned_sequences;
    }
    

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Set<Aligned_Sequences_Entity> getAligned_sequences() {
        return aligned_sequences;
    }

    public void setAligned_sequences(Set<Aligned_Sequences_Entity> aligned_sequences) {
        this.aligned_sequences = aligned_sequences;
    }
    
    public void addAlignedSequence(Aligned_Sequences_Entity alignedSequence)
    {
        this.aligned_sequences.add(alignedSequence);
    }

    public Set<Aligned_Filename_Window_Entity> getAligned_filename_windows() {
        return aligned_filename_windows;
    }

    public void setAligned_filename_windows(Set<Aligned_Filename_Window_Entity> aligned_filename_windows) {
        this.aligned_filename_windows = aligned_filename_windows;
    }
    
    public void addAlignedFilenameWindow(Aligned_Filename_Window_Entity alignedFilenameWindow)
    {
        this.aligned_filename_windows.add(alignedFilenameWindow);
    }
    
    @Embeddable
    public static class Id implements Serializable {
        
        // seqid pertaining to the reference sequence set (e.g. Chr1, tRNA_gtc)
        @Column(name="Seqid")
        private String seqid;
    
        // name of the reference sequence set (e.g. genome, Rfam)
        @Column(name="Reference_Name")
        private String reference;
    
        // id of this window
        @Column(name="Window_Id")
        private int windowId;
        
        @Column(name="Window_Length")
        private int windowLength;

        public Id(){}
        public Id(String seqid, String reference, int windowId, int windowLength) {
            this.seqid = seqid;
            this.reference = reference;
            this.windowId = windowId;
            this.windowLength = windowLength;
        }
        
        

        public String getSeqid() {
            return seqid;
        }

        public void setSeqid(String seqid) {
            this.seqid = seqid;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public int getWindowId() {
            return windowId;
        }

        public void setWindowId(int windowId) {
            this.windowId = windowId;
        }

        public int getWindowLength() {
            return windowLength;
        }

        public void setWindowLength(int windowLength) {
            this.windowLength = windowLength;
        }
        
        @Override
        public String toString()
        {
            StringJoiner sj = new StringJoiner(", ");
            sj.add(seqid).add(reference).add(Integer.toString(windowId)).add(Integer.toString(windowLength));
            return sj.toString();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + Objects.hashCode(this.seqid);
            hash = 71 * hash + Objects.hashCode(this.reference);
            hash = 71 * hash + this.windowId;
            hash = 71 * hash + this.windowLength;
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
            if (!Objects.equals(this.seqid, other.seqid)) {
                return false;
            }
            if (!Objects.equals(this.reference, other.reference)) {
                return false;
            }
            if (this.windowId != other.windowId) {
                return false;
            }
            if (this.windowLength != other.windowLength) {
                return false;
            }
            return true;
        }
        
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
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
        final Alignment_Window_Entity other = (Alignment_Window_Entity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        return this.id.toString();
    }
    
}
