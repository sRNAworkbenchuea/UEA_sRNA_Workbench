package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.Nucleotide;

/**
 *
 * @author mka07yyu
 */
@Table(name="NUCLEOTIDE_SIZE_CLASS")
@Entity
public class Nucleotide_Size_Class_Entity implements Serializable{
    @Id
    @GeneratedValue
    Long id;
    
    @Enumerated
    private Nucleotide nucleotide;
    
    @Column(name="Filename")
    private String filename;
    
    @Column(name="RNA_Size")
    private int rnaSize;
    
    @Column(name="Sequence_Position")
    private int seqPos;
    
    @Column(name="Count")
    private double count;

    public Nucleotide_Size_Class_Entity() {
    }

    public Nucleotide_Size_Class_Entity(String filename, Nucleotide nucleotide, int rnaSize, int seqPos, double count) {
        this.nucleotide = nucleotide;
        this.rnaSize = rnaSize;
        this.seqPos = seqPos;
        this.count = count;
        this.filename = filename;
    }
    
    public void addNucleotide(double count)
    {
        this.count += count;
    }

    public Nucleotide getNucleotide() {
        return nucleotide;
    }

    public void setNucleotide(Nucleotide nucleotide) {
        this.nucleotide = nucleotide;
    }

    public int getRnaSize() {
        return rnaSize;
    }

    public void setRnaSize(int rnaSize) {
        this.rnaSize = rnaSize;
    }

    public int getSeqPos() {
        return seqPos;
    }

    public void setSeqPos(int seqPos) {
        this.seqPos = seqPos;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Nucleotide_Size_Class_Entity{" + "id=" + id + ", nucleotide=" + nucleotide + ", filename=" + filename + ", rnaSize=" + rnaSize + ", seqPos=" + seqPos + ", count=" + count + '}';
    }
    

    
}
