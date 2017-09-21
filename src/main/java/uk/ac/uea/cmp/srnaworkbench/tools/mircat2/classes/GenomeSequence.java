/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes;

import java.io.IOException;
import java.util.Objects;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;

/**
 *
 * @author keu13sgu
 */
public class GenomeSequence {
    private String chrom;
    
    private int beg;
    private int end;

    public String getSequence(GenomeManager genMan) throws IOException {
        return genMan.getDNA(chrom, beg + 1, end + 1, GenomeManager.DIR.POSITIVE);
    }
    
    public String getSequence(GenomeManager genMan, int start, int end) throws IOException {
        return genMan.getDNA(chrom, start + 1, end + 1, GenomeManager.DIR.POSITIVE);
    }

    public int length(){
        return end - beg + 1;
    }
    
    public GenomeSequence(String id, int beg, int end) {
        this.chrom = id;
        this.beg = beg;
        this.end = end;
    }
    

    public String getId() {
        return chrom;
    }

    public void setId(String id) {
        this.chrom = id;
    }


    public int getBeg() {
        return beg;
    }

    public void setBeg(int beg) {
        this.beg = beg;
    }

    
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        //return "id = " + id + ", sequence = " + sequence + ", beg = " + beg + ", end = " + end;
        return "id = " + chrom + ", beg = " + beg + ", end = " + end;
    }
    
     public String myToString(){
        StringBuilder str = new StringBuilder();

        for(int i = 0; i< end - beg; i++){
            str.append("-");
        }
        
        return str.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.chrom);
        //hash = 37 * hash + Objects.hashCode(this.sequence);
        hash = 37 * hash + this.beg;
        hash = 37 * hash + this.end;
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
        final GenomeSequence other = (GenomeSequence) obj;
        if (!Objects.equals(this.chrom, other.chrom)) {
            return false;
        }
//        if (!Objects.equals(this.sequence, other.sequence)) {
//            return false;
//        }
        if (this.beg != other.beg) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }
}
