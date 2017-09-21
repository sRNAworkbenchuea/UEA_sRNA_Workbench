package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.database.io.JsonDTO;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author mka07yyu
 */
@Entity
@Table(name="JACCARD")
public class Jaccard_Entity implements Serializable, JsonDTO{
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name="Number_Of_Sequences")
    private int numberOfSeqs;
    
    @OneToOne
    @JoinColumn
    private Filename_Entity refFile;
    
    @OneToOne
    @JoinColumn
    private Filename_Entity obsFile;
    
    @Column(name="Jaccard_Index")
    private double jaccardIndex;
    
    @Enumerated
    private NormalisationType normType;

    public Jaccard_Entity(int numberOfSeqs, Filename_Entity refSeq, Filename_Entity obsSeq, double jaccardIndex, NormalisationType normType) {
        this.numberOfSeqs = numberOfSeqs;
        this.refFile = refSeq;
        this.obsFile = obsSeq;
        this.jaccardIndex = jaccardIndex;
        this.normType = normType;
    }
    
    public Jaccard_Entity(){}

    public int getNumberOfSeqs() {
        return numberOfSeqs;
    }

    public void setNumberOfSeqs(int numberOfSeqs) {
        this.numberOfSeqs = numberOfSeqs;
    }

    public Filename_Entity getRefSeq() {
        return refFile;
    }

    public void setRefSeq(Filename_Entity refSeq) {
        this.refFile = refSeq;
    }

    public Filename_Entity getObsSeq() {
        return obsFile;
    }

    public void setObsSeq(Filename_Entity obsSeq) {
        this.obsFile = obsSeq;
    }

    public double getJaccardIndex() {
        return jaccardIndex;
    }

    public void setJaccardIndex(double jaccardIndex) {
        this.jaccardIndex = jaccardIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Top ").append(numberOfSeqs).append("\t").append(this.normType ).append("/").append(this.refFile)
                .append(", ").append(this.obsFile).append("\t").append(this.jaccardIndex);
                
        return sb.toString();
    }

    @Override
    public Object getElement(String elementKey) {
        switch(elementKey)
        {
            case "Top":
                return this.numberOfSeqs;
            case "Normalisation":
                return this.normType;
            case "Reference":
                return this.refFile;
            case "Observed":
                return this.obsFile;
            case "Jaccard":
                return this.jaccardIndex;
            default:
                return "NA";
        }
    }
    
    
    
    
}
