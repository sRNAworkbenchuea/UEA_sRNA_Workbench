package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;

/**
 *
 * @author matt
 */
//@Table(name="MBoxAndWhisker")
@Entity
public class MBoxAndWhisker_Entity extends Distribution implements Serializable{
//    @Id
//    @Column(name = "ID")
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
    
    @Column(name="RNA_Size")
    private Integer rnaSize;
    
    @Column(name="Annotation")
    private String annotation;
    
    @Enumerated
    private NormalisationType normType;
    
    // The offset used to create the mvalues for this distribution
    @Column(name="Offset_Value")
    private int offset;
    
    @Embedded
    private FilePair filePair;
    
//    @Embedded
//    private Distribution distribution;

    public MBoxAndWhisker_Entity(){}
    public MBoxAndWhisker_Entity(NormalisationType normType, FilePair pair, int offset, int rnaSize, String annotation,
            double lq, double med, double uq, double minRange, double maxRange
            //Distribution distribution, 
            ) {
        super(Logarithm.BASE_2,lq,med,uq,minRange,maxRange);
        this.normType = normType;
        this.filePair = pair;
        this.offset = offset;
        this.rnaSize = rnaSize;
        //this.distribution = distribution;
        this.annotation = annotation;
    }
    public MBoxAndWhisker_Entity(NormalisationType normType, FilePair pair, int offset, int rnaSize, String annotation,
            Distribution dist
    ) {
        super(dist);
        this.normType = normType;
        this.filePair = pair;
        this.offset = offset;
        this.rnaSize = rnaSize;
        //this.distribution = distribution;
        this.annotation = annotation;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public FilePair getFilePair() {
        return filePair;
    }

    public void setFilePair(FilePair filePair) {
        this.filePair = filePair;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }



//    public Distribution getDistribution() {
//        return distribution;
//    }
//
//    public void setDistribution(Distribution distribution) {
//        this.distribution = distribution;
//    }

    public Integer getRnaSize() {
        return rnaSize;
    }

    public void setRnaSize(Integer rnaSize) {
        this.rnaSize = rnaSize;
    }
    
    @Override
    public String toString()
    {
        return this.filePair.toString() + ", " + this.normType.toString() + ", " + this.rnaSize;// + ", " + this.distribution.toString();
    }
    
    
}
