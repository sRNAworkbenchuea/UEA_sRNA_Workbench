package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Chris Applegate
 */
@Entity
@Table(name = "TRANSCRIPTS")
public class Transcript_Entity implements Serializable {

    private static final int geneStrLen = 1000;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "GENE", length = geneStrLen)
    private String gene;

    private int sequenceLength;

    public Transcript_Entity() {

    }

    public Transcript_Entity(String gene) {
        this.gene = gene;
    }

    public void setSequenceLength(int length) {
        this.sequenceLength = length;

    }

    public int getSequenceLength() {
        return sequenceLength;

    }

    public Long getID() {
        return this.id;
    }

    public String getGene() {
        return this.gene;
    }

    public String toString() {
        String s = "";
        s += this.getID() + " " + this.gene;
        return s;
    }
}
