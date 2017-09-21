package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAelement;

/**
 * Entity for stored MA values
 *
 * @author mka07yyu
 */
@Entity
@Table(name = "MA_VALUES")
public class MA_Entity implements Serializable {

    @EmbeddedId
    @Column(name = "MA_ID")
    private MA id;
    
    @OneToOne
    @JoinColumn(name = "refSequence")
    private Sequence_Entity refSeqEntity;

    @OneToOne
    @JoinColumn(name = "obsSequence")
    private Sequence_Entity obsSeqEntity;

    @Column(name = "M_VALUE", nullable = false)
    private double M;

    @Column(name = "A_VALUE", nullable = false)
    private double A;
    
    @Column(name="Annotation_Type")
    private String annoType = "none";
    
    // Column used by some normalisation strategies to determine
    // whether to derive a normalisation factor based on this entity
    // if trimmed is false, entity will be used. If true, entity won't be used
    @Column(name = "trimmed", nullable=false)
    private boolean trimmed = false;
    
    @Column(name = "weighting")
    private double weighting;

    public double getWeighting() {
        return weighting;
    }

    public void setWeighting(double weighting) {
        this.weighting = weighting;
    }
    
    public boolean isTrimmed() {
        return trimmed;
    }

    public void setTrimmed(boolean trimmed) {
        this.trimmed = trimmed;
    }
    
    public MA_Entity(){};
    
    public MA_Entity(Sequence_Entity refSequence, Sequence_Entity obsSequence, double M, double A, NormalisationType normType, int offset)
    {
        this.refSeqEntity = refSequence;
        this.obsSeqEntity = obsSequence;
        this.M =M;
        this.A = A;
        this.id = new MA(refSequence, obsSequence, normType, offset);
    }
    
    /**
     * Create an MA_Entity using Sequence_Entity's and the desired normalisation
     * type and offset.
     *
     * @param refSequence
     * @param obsSequence
     * @param offset
     * @param normType
     */
    public MA_Entity(Sequence_Entity refSequence, Sequence_Entity obsSequence, double refExp, double obsExp, double refTotal, double obsTotal, int offset, NormalisationType normType) {
//        double refExp = normType.getDatabaseAbundance(refSequence);
//        double obsExp = normType.getDatabaseAbundance(obsSequence);
        this.refSeqEntity = refSequence;
        this.obsSeqEntity = obsSequence;
        

        // FIXME: Total abundance for each normalisation is required here...
        MAelement ma = new MAelement(refExp, obsExp, refTotal, obsTotal, 2, offset);
        this.M = ma.getM();
        this.A = ma.getA();
        this.id = new MA(refSequence, obsSequence, normType, offset);
        this.weighting = ma.getWeighting();
    }
    
    /**
     * Construct MA values without using total abundances. In this case, just the abundances will be used to calculate MA values (not the per-total)
     * @param refSequence
     * @param obsSequence
     * @param offset
     * @param normType 
     */
    public MA_Entity(Sequence_Entity refSequence, Sequence_Entity obsSequence, double refExp, double obsExp, int offset, NormalisationType normType) {
        this(refSequence, obsSequence, refExp, obsExp, 1, 1, offset, normType);
    }

    public String getAnnoType() {
        return annoType;
    }

    public void setAnnoType(String type) {
        this.annoType = type;
    }
    
    public Sequence_Entity getRefSeqEntity() {
        return refSeqEntity;
    }

    public void setRefSeqEntity(Sequence_Entity refSeqEntity) {
        this.refSeqEntity = refSeqEntity;
    }

    public Sequence_Entity getObsSeqEntity() {
        return obsSeqEntity;
    }

    public void setObsSeqEntity(Sequence_Entity obsSeqEntity) {
        this.obsSeqEntity = obsSeqEntity;
    }
    
    public double getM() {
        return M;
    }

    public void setM(double M) {
        this.M = M;
    }

    public double getA() {
        return A;
    }

    public void setA(double A) {
        this.A = A;
    }

    
    public MA getMA()
    {
        return id;
    }
    
    @Override
    public String toString()
    {
        return id.toString() + " // weighting: " + this.weighting + " trimmed: " + this.isTrimmed() + " M/A: "+this.M+"/"+this.A;
    }

    @Embeddable
    public static class MA implements Serializable {

//        @ManyToOne
//        @JoinColumn(name = "REF_SEQUENCE")
//        private Sequence_Entity refSequence;
//
//        @ManyToOne
//        @JoinColumn(name = "OBS_SEQUENCE")
//        private Sequence_Entity obsSequence;
        
//        @Column(name = "REF_SEQUENCE")
//        private String refSequence;
//        @Column(name = "OBS_SEQUENCE")
//        private String obsSequence;
        
        @Embedded
        private FilePair filePair;
        
        @Column(name="RNA_Sequence")
        private String sequence;

        @Enumerated
        NormalisationType normType;

        @Column(name = "OFFSET_VALUE", nullable = false)
        private int offset;

        public MA() {
        }

        public MA(Sequence_Entity refSequence, Sequence_Entity obsSequence, NormalisationType normType, int offset) {
//            this.refSequence = refSequence.getFilename();
//            this.obsSequence = obsSequence.getFilename();
            this.filePair = new FilePair(refSequence.getFilename(), obsSequence.getFilename());

            this.sequence = refSequence.getRNA_Sequence();

            this.offset = offset;
            this.normType = normType;
        }

        public FilePair getFilePair() {
            return filePair;
        }

        public void setFilePair(FilePair filePair) {
            this.filePair = filePair;
        }

        public String getFileKey()
        {
            return this.filePair.getPairKey();
        }
        public NormalisationType getNormType() {
            return normType;
        }

        public void setNormType(NormalisationType normType) {
            this.normType = normType;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.filePair);
            hash = 89 * hash + Objects.hashCode(this.sequence);
            hash = 89 * hash + Objects.hashCode(this.normType);
            hash = 89 * hash + this.offset;
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
            final MA other = (MA) obj;
            if (!Objects.equals(this.filePair, other.filePair)) {
                return false;
            }
            if (!Objects.equals(this.sequence, other.sequence)) {
                return false;
            }
            if (this.normType != other.normType) {
                return false;
            }
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }


        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            //sb.append(this.id).append(":: ")
            sb.append("Samples: ").append(this.filePair.getPairKey())
                    .append(" Sequence: ").append(this.sequence)
                    .append(" Normalisation: ").append(this.normType)
                    .append(" Offset: ").append(this.offset);
            return (sb.toString());
        }
    }

}
