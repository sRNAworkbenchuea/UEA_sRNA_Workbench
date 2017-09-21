/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author w0445959
 */
@Entity
@Table(name = "FILENAMES")
@NamedQueries({
    @NamedQuery(
            name = "@HQL_GET_ALL_FILENAMES",
            query = "from Filename_Entity"
    ),
    @NamedQuery(
            name = "@HQL_GET_SELECTED_FILES",
            query = "from Filename_Entity f where f.fileID in (:filenames)",
            cacheable = true
    )

})
public class Filename_Entity implements Serializable 
{
    @Id
    @Column(name = "File_Name", nullable=false)
    private String filename;
    @Column(name = "File_Path", nullable=false)
    private String filePath;
    @Column(name = "Sample_ID", nullable=false)
    private String sampleID;
    @Column(name = "Total_Abundance", nullable=false)
    private int totalAbundance;
    @Column(name = "Total_Genome_Match_Abundance", nullable=false)
    private int totalGenomeMatchAbundance;
    
    @Column(name="ReplicateID")
    private int replicateID;
    
    @Column(name="FileID")
    private String fileID;
        
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="Sample")
    private Sample_Entity sample;
    
    // A place to store the offset for this sample
    // initially set to no offset used
    @Column(name="Expression_Offset", nullable=false)
    private double offset = 0;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="id.filename_entity", cascade = CascadeType.ALL)
    private Set<Filename_Abundance_Entity> normalisedTotalAbundances = new HashSet<>(0);
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "filename_sequence", cascade = CascadeType.ALL)
    private Set<Sequence_Entity> sequenceFilenameRelationships = new HashSet<>();
    
    
    public Filename_Entity()
    {
        
    }
    
    public Filename_Entity(String filename, String filePath, String sampleID)
    {
        this.filename = filename;
        this.filePath = filePath;
        this.sampleID = sampleID;
        
        totalAbundance = totalGenomeMatchAbundance = 0;
//        totalNormalisedAbundance = 0.0;
    }
    
    public String getName()
    {
        return this.sampleID+'_'+this.replicateID;
    }

    public int getReplicateID() {
        return replicateID;
    }

    public void setReplicateID(int replicateID) {
        this.replicateID = replicateID;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

   
    
    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public Sample_Entity getSample() {
        return sample;
    }

    public void setSample(Sample_Entity sample) {
        this.sample = sample;
    }

    public String getSampleID()
    {
        return sampleID;
    }

    public void setSampleID(String sampleID)
    {
        this.sampleID = sampleID;
    }

    public int getTotalAbundance()
    {
        return totalAbundance;
    }

    public void setTotalAbundance(int totalAbundance)
    {
        this.totalAbundance = totalAbundance;
    }

    public int getTotalGenomeMatchAbundance()
    {
        return totalGenomeMatchAbundance;
    }

    public void setTotalGenomeMatchAbundance(int totalGenomeMatchAbundance)
    {
        this.totalGenomeMatchAbundance = totalGenomeMatchAbundance;
    }
    
    public void addToTotalGenomeAbundance(int toAdd)
    {
        totalGenomeMatchAbundance += toAdd;
    }

    public Set<Filename_Abundance_Entity> getTotalNormalisedAbundance()
    {
        return this.normalisedTotalAbundances;
    }

    public void setTotalNormalisedAbundance(Set<Filename_Abundance_Entity> totalNormalisedAbundance)
    {
        this.normalisedTotalAbundances = totalNormalisedAbundance;
    }
    
    public void addTotalNormalisedAbundance(NormalisationType normType, double total) {
        this.normalisedTotalAbundances.add(new Filename_Abundance_Entity(this, normType, total));
    }
    
    public double getNormalisedTotalAbundance(NormalisationType normType)
    {
        for(Filename_Abundance_Entity fae : this.normalisedTotalAbundances)
        {
            if(normType.equals(fae.getNormType()))
            {
                return fae.getTotalAbundance();
            }
        }
        throw new IllegalArgumentException("Normalisation type " + normType + " has not yet been calculated ");
    }
    
    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    
    public Set<Sequence_Entity> getSequenceFilenameRelationships()
    {
        return sequenceFilenameRelationships;
    }

    public void setSequenceFilenameRelationships(Set<Sequence_Entity> sequenceFilenameRelationships)
    {
        this.sequenceFilenameRelationships = sequenceFilenameRelationships;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.filename);
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
        final Filename_Entity other = (Filename_Entity) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filename_Entity{" + "filename=" + filename + ", filePath=" + filePath + ", sampleID=" + sampleID + ", totalAbundance=" + 
                totalAbundance + ", totalGenomeMatchAbundance=" + 
                totalGenomeMatchAbundance + ", replicateID=" + this.replicateID + '}';
    }
    

 
    
    
}
