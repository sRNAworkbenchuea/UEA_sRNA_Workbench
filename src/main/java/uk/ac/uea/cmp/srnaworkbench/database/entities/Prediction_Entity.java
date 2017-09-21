package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.proxy.HibernateProxy;

/**
 *
 * @author Chris Applegate
 */
@Entity
@Table(name = "PREDICTIONS")
public class Prediction_Entity implements Serializable, Comparable<Prediction_Entity> {



    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "pred_ID")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PRECURSOR_ID")
    private Precursor_Entity precursor;
    
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "LFold_PRECURSOR_ID")
    private LFold_Precursor_Entity lFoldPrecursor;

    @Column(name = "PREDICTOR")
    private String predictor;
//    
//    @Column(name="RNA_Sequence")
//    private String rna_sequence;

    @ManyToOne(cascade = CascadeType.ALL,  fetch = FetchType.EAGER)
    //@JoinColumn(name="RNA_Sequence")
    private Aligned_Sequences_Entity mature;

    @ManyToOne(cascade = CascadeType.ALL)//, fetch = FetchType.LAZY)
    //@JoinColumn(name="RNA_Sequence")
    private Aligned_Sequences_Entity star;

    public Prediction_Entity() {
        this.precursor = new Precursor_Entity();
        this.mature = Aligned_Sequences_Entity.NO_ALIGNMENT;
        this.star = Aligned_Sequences_Entity.NO_ALIGNMENT;
    }

    public Prediction_Entity(String predictor, Precursor_Entity precursor, Aligned_Sequences_Entity mature, Aligned_Sequences_Entity star) {
        this.precursor = precursor;
        this.predictor = predictor;
        this.mature = mature;
        this.star = star;
    }
    
        public Prediction_Entity(String predictor, LFold_Precursor_Entity precursor, Aligned_Sequences_Entity mature, Aligned_Sequences_Entity star) {
        this.lFoldPrecursor = null;
        this.predictor = predictor;
        this.mature = mature;
        //mature.getAb();
        this.star = null;
    }

    public Long getID() {
        return this.id;
    }

    public String getSequence() {
        return this.precursor.getAlignment().getRna_seq();
    }

    public String getStructure() {
        return this.precursor.getStructure();
    }

    public Aligned_Sequences_Entity getMature() {
        return this.mature;
    }

    public Precursor_Entity getPrecursor() {
        return this.precursor;
    }

    public void setPrecursor(Precursor_Entity p) {
        this.precursor = p;
    }

    public void setPredictor(String p) {
        this.predictor = p;
    }

    public String getPredictor() {
        return this.predictor;
    }

    @Override
    public int compareTo(Prediction_Entity o) {
        return this.precursor.compareTo(o.precursor);
    }

    public void setMature(Aligned_Sequences_Entity mature) {
        this.mature = mature;
    }

    public void setStar(Aligned_Sequences_Entity star) {
        this.star = star;
    }

    public int getSIndex() {
        return this.getPrecursor().getAlignment().getStart();
    }

    public int getEIndex() {
        return this.getPrecursor().getAlignment().getEnd();
    }

    public String getChromosome() {
        return this.getPrecursor().getAlignment().getChromosome();
    }

    public String getStrand() {
        return this.getPrecursor().getAlignment().getStrand();
    }

    public String toWebString() {
        return this.getPrecursor().toWebString();
    }

    @Override
    public String toString() {
        String precursorID = "null";
        String matureID = "null";
        String starID = "null";
        if (this.precursor != null) {
            precursorID = String.format("PRECURSOR[%s]", this.precursor.toString());
        }
        if (this.mature != null) {
            matureID = String.format("%s", this.mature.toString());
        }
        if (this.star != null) {
            starID = String.format("%s", this.star.toString());
        }
        return String.format("%d %s %s %s %s", this.id, precursorID, this.predictor, matureID, starID);
    }

   

    public List<String> toStringArray() {

        String nullString = "NULL";
        String idStr = String.format("%d", this.id);
        String chrom = nullString;
        String precursorStart = nullString;
        String precursorEnd = nullString;
        String precursorStrand = nullString;
        String precursorDuplex = nullString;
        String precursorMFE = nullString;
        String matureStart = nullString;
        String matureEnd = nullString;
        String matureSequence = nullString;
        String starStart = nullString;
        String starEnd = nullString;
        String starSequence = nullString;

        if (this.precursor != null && this.precursor.getAlignment() != null) {
            Aligned_Sequences_Entity aln = this.precursor.getAlignment();
            chrom = aln.getChromosome();
            precursorStart = String.format("%d", aln.getStart());
            precursorEnd = String.format("%d", aln.getEnd());
            precursorStrand = aln.getStrand();
            precursorDuplex = aln.getRna_seq() + LINE_SEPARATOR + this.precursor.getStructure();
            precursorMFE = String.format("%f", this.precursor.getMFE());
        }

        if (this.mature != null) {
            matureStart = String.format("%d", this.mature.getStart());
            matureEnd = String.format("%d", this.mature.getEnd());
            matureSequence = this.mature.getRna_seq();
        }
        if (this.star != null) {
            starStart = String.format("%d", this.star.getStart());
            starEnd = String.format("%d", this.star.getEnd());
            starSequence = this.star.getRna_seq();
        }

        List<String> out_array = new ArrayList<>();
        out_array.add(idStr);
        out_array.add(this.predictor);
        out_array.add(chrom);
        out_array.add(precursorStart);
        out_array.add(precursorEnd);
        out_array.add(precursorStrand);
        out_array.add(precursorDuplex);
        out_array.add(precursorMFE);
        out_array.add(matureStart);
        out_array.add(matureEnd);
        out_array.add(matureSequence);
        out_array.add(starStart);
        out_array.add(starEnd);
        out_array.add(starSequence);

        return out_array;
    }

    public static List<String> toStringTitleArray() {
        List<String> out_array = new ArrayList<>();
        out_array.add("ID");
        out_array.add("Predictior");
        out_array.add("Chrom");
        out_array.add("Precursor Start");
        out_array.add("Precursor End");
        out_array.add("Strand");
        out_array.add("Precursor");
        out_array.add("MFE");
        out_array.add("Mature Start");
        out_array.add("Mature End");
        out_array.add("Mature Sequence");
        out_array.add("Star Start");
        out_array.add("Star End");
        out_array.add("Star Sequence");
        return out_array;
    }
}
