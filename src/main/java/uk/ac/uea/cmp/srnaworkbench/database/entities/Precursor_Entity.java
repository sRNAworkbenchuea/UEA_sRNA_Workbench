/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;

// @author Chris Applegate
@Entity
@Table(name = "PRECURSORS")
public class Precursor_Entity implements Serializable, Comparable<Precursor_Entity> {

      //  public static final String RNAevalPath = ViennaRNAPath + "Progs/RNAeval";
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // structure is written from 5' to 3' on approporiate strand
    @Column(name = "RNA_Structure")
    private String structure;

    @Column(name = "MFE")
    private double mfe;

    @ManyToOne(cascade = CascadeType.ALL)
    private Aligned_Sequences_Entity alignment;

    public Precursor_Entity() {
        this.alignment = Aligned_Sequences_Entity.NO_ALIGNMENT;
    }

    public Precursor_Entity(String sequence, String structure, String chrID, int sIndex, int eIndex, String strand, double mfe) {

        this.alignment = new Aligned_Sequences_Entity(Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING, chrID, sequence, sIndex, eIndex, strand, 0);

        this.structure = structure;
        this.mfe = mfe;
    }
    
    public Precursor_Entity(String structure, double mfe, Aligned_Sequences_Entity ase) {

        this.alignment = ase;

        this.structure = structure;
        this.mfe = mfe;
    }

    public Aligned_Sequences_Entity getAlignment() {
        return this.alignment;
    }

    public void setAlignment(Aligned_Sequences_Entity alignment) {
        this.alignment = alignment;
    }

    public Long getID() {
        return this.id;
    }

    public double getMFE() {
        return this.mfe;
    }

    public String getStructure() {
        return this.structure;
    }

    public void setStructure(String s) {
        this.structure = s;
    }
    
    public int getStart(){
        return this.alignment.getStart();
    }
    
    public String getSequence(){
        return this.alignment.getRna_seq();
    }

    public int calculateScore() {
        /*     int score = 0;
         try
         {

         // process mature structure
         if (this.mature.getStructure().contains("(") && this.mature.getStructure().contains(")")) {
         score -= 100000;
         }
         if (!(this.mature.getStructure().contains("(") || this.mature.getStructure().contains(")"))) {
         score -= 100000;
         }
         // penalise mismatches arcoss entire structure
         for (int i = 0; i < this.structure.length(); i++) {
         if (this.structure.charAt(i) == '.') {
         score--;
         }
         }
         // penalise mismatches across mature structure
         for (int i = 0; i < this.mature.getStructure().length(); i++) {
         if (this.mature.getStructure().charAt(i) == '.') {
         score -= 10;
         }
         }
         String oppositeDuplexSequence = getOppositeSideOfDuplex(getMatureSIndex(), getMatureEIndex());
         int duplex_sIndex = this.RNA_Sequence.indexOf(oppositeDuplexSequence);
         int duplex_eIndex = duplex_sIndex + oppositeDuplexSequence.length() - 1;
         String oppositeDuplexStructure = this.structure.substring(duplex_sIndex, duplex_eIndex + 1);
         for (int i = 0; i < oppositeDuplexStructure.length(); i++) {
         if (oppositeDuplexStructure.charAt(i) == '.') {
         score -= 10;
         }
         }
         int differenceInLength = Math.abs(this.mature.getSequence().length() - oppositeDuplexSequence.length()) * 10;
         score -= differenceInLength;
         }
         catch(Exception e)
         {

         return 0;
         }
         return score;*/
        return 0;
    }

    @Override
    public int compareTo(Precursor_Entity o) {
        // calculate scores
        int thisScore = this.calculateScore();
        int otherScore = o.calculateScore();
        // compare
        if (thisScore == otherScore) {
            return 0;
        }
        if (thisScore < otherScore) {
            return 1;
        }
        return -1;
    }

    private int getMatchingBase(int index, int offsetDir) {

        int offset = 0;
        int offsetIndex = index + offset;
        char indexChar = this.structure.charAt(offsetIndex);

        while (indexChar == '.') {
            if (offsetIndex + offsetDir < this.structure.length() - 1 && offsetIndex + offsetDir > 0) {
                offset += offsetDir;
                offsetIndex = index + offset;
                indexChar = this.structure.charAt(offsetIndex);
            } else {
                return -1;
            }
        }
        if (indexChar == '(') {
            int nestLevel = 0;
            for (int i = offsetIndex; i < this.structure.length(); i++) {
                if (this.structure.charAt(i) == '(') {
                    nestLevel++;
                } else if (this.structure.charAt(i) == ')') {
                    nestLevel--;
                }
                if (nestLevel == 0) {
                    int pos = Math.max(0, Math.min(i + offset, this.structure.length()));
                    return pos;
                }
            }
        } else if (indexChar == ')') {
            int nestLevel = 0;
            for (int i = offsetIndex; i >= 0; i--) {
                if (this.structure.charAt(i) == ')') {
                    nestLevel++;
                } else if (this.structure.charAt(i) == '(') {
                    nestLevel--;
                }
                if (nestLevel == 0) {
                    int pos = Math.max(0, Math.min(i + offset, this.structure.length()));
                    return pos;
                }
            }
        }
        return -1;
    }

    public void setMFE(double mfe) {
        this.mfe = mfe;
    }

    @Override
    public String toString() {
        String alignmentID = "null";
        if (this.alignment != null) {
            alignmentID = String.format("ALIGNMENT[%s]", this.alignment.toString());
        }
        return String.format("%d %s %f %s", this.id, this.structure, this.mfe, alignmentID);
    }

    public void computeMFE() throws FileNotFoundException, IOException, InterruptedException {

        BinaryExecutor binExe = AppUtils.INSTANCE.getBinaryExecutor();
        String result = binExe.execRNAEval(this.alignment.getRna_seq(), this.structure);
        String[] fields = result.split("[ ]");
        String energyStr = fields[1].replaceAll("[()]", "").trim();
        this.mfe = Double.parseDouble(energyStr);
    }

    public String toWebString() {
        String str = "";
        str += this.getAlignment().getRna_seq();
        str += "</br>";
        str += this.structure;
        return str;
    }
}
