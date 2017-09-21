/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;

/**
 *
 * @author keu13sgu
 */
public class Patman extends ArrayList<Aligned_Sequences_Entity> {

    private Aligned_Sequences_Entity mostAbundant = null;
    private boolean mostAbundantSet = false;

    private int beg = Integer.MAX_VALUE;
    private int end = Integer.MIN_VALUE;
    
    private int begRel = Integer.MAX_VALUE;
    private int endRel = Integer.MIN_VALUE;

    /**
     * Creates an empty Patman object.
     */
    public Patman() {
        super();
    }

    public Patman(List<Aligned_Sequences_Entity> list) {
        super(list);
    }


    /**
     * Organises PatmanEntries in this object by number of mismatches.
     */
    /**
     * Organises PatmanEntries in this object by abundance in descending order.
     */
    public void sortByAbundance() {
        Collections.sort(this,
                new Comparator<Aligned_Sequences_Entity>() {
                    @Override
                    public int compare(Aligned_Sequences_Entity p1, Aligned_Sequences_Entity p2) {
                        return (int) p2.getAb() - (int) p1.getAb();
                    }
                });
    }

    public int length() {
        
        if(beg == Integer.MAX_VALUE){
            if(this.isEmpty()) return 0;

            int min = this.get(0).getStart1();
            int max = this.get(0).getEnd1();

            for(int i = 1; i < this.size(); i++){
                if(min > this.get(i).getStart1()){
                    min = this.get(i).getStart1();
                }
                if(max < this.get(i).getEnd1()){
                    max = this.get(i).getEnd1();
                }
            }

            return max - min;
        } 
        
        return end - beg;
    }

    public int getBeg() {      
        if(beg == Integer.MAX_VALUE){
            int b = Integer.MAX_VALUE;
            for(Aligned_Sequences_Entity s: this){
                if(b > s.getStart1()){
                    b = s.getStart1();
                }
            }
            begRel = b;
            beg = b;
            return b;
        }
        return begRel;

    }
    

    public int getEnd() {     
         if(end == Integer.MIN_VALUE){
            int e = Integer.MIN_VALUE;
            for(Aligned_Sequences_Entity s: this){
                if(e < s.getEnd1()){
                    e = s.getEnd1();
                }
            }
            endRel = e;
            end = e;
            return e;
        }
        return endRel;
    }
    
    public void flipBegEndRel(int b, int e){
        endRel =  b + (e - end) + this.length() - 1;      
        begRel =  b + ( e - end);
        
    }
    
    public void resetBegEndRel(){
        begRel = beg;
        endRel = end;
    }

    public Aligned_Sequences_Entity mostAbundantSRNA() {

        if (mostAbundantSet == true) {
            return mostAbundant;
        }

        int max = Integer.MIN_VALUE;
        int index = -1;

        for (int i = 0; i < this.size(); i++) {
            int ab = this.get(i).getAb();
            if (ab > max ){//&& this.get(i).getLength() < 25 && this.get(i).getLength() >= 19) {
                max = ab;
                index = i;
            }
        }

        mostAbundantSet = true;

        if (index >= 0) {
            mostAbundant = this.get(index);
        }
        return mostAbundant;
    }

    public int getAb() {
        int sum = 0;
        for (Aligned_Sequences_Entity s : this) {
            sum += s.getAb();
        }
        return sum;
    }

    @Override
    public boolean add(Aligned_Sequences_Entity e) {
        if (mostAbundantSet) {
           //if (e.getLength() >= 19 && e.getLength() < 25) {
                if (mostAbundant == null) {
                    mostAbundant = e;
                } else {
                    if (e.getAb() > mostAbundant.getAb()) {
                        mostAbundant = e;
                    }
                }
           // }
        }
        if (beg > e.getStart1() ){//&&  e.getLength() < 25) {
            beg = e.getStart1();
            begRel = beg;
        }
        if (end < e.getEnd1() ){//&& e.getLength() < 25) {
            end = e.getEnd1();
            endRel = end;
       }

        return super.add(e);

    }

    public void sort() {
        Collections.sort(this, new Comparator<Aligned_Sequences_Entity>() {

            @Override
            public int compare(Aligned_Sequences_Entity o1, Aligned_Sequences_Entity o2) {
                if (o1.getChromosome().compareTo(o2.getChromosome()) < 0) {
                    return -1;
                } else if (o1.getChromosome().compareTo(o2.getChromosome()) > 0) {
                    return 1;
                } else {
                    if (o1.getStart1() > o2.getStart1()) {
                        return 1;
                    } else if (o1.getStart1() < o2.getStart1()) {
                        return -1;
                    } else {
                        return Integer.compare(o1.getEnd1(), o2.getEnd1());
                    }

                }
            }

        });
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.mostAbundant);
        hash = 31 * hash + (this.mostAbundantSet ? 1 : 0);
        hash = 31 * hash + this.beg;
        hash = 31 * hash + this.end;
        for(Aligned_Sequences_Entity s: this){
            hash = 31 * hash + Objects.hashCode(s);
        }
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
        final Patman other = (Patman) obj;
        if (!Objects.equals(this.mostAbundant, other.mostAbundant)) {
            return false;
        }
        if (this.mostAbundantSet != other.mostAbundantSet) {
            return false;
        }
        if (this.beg != other.beg) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (this.size() != other.size()) {
            return false;
        }
       // boolean cmp = true;
        for(int i = 0; i< this.size(); i++){
            Aligned_Sequences_Entity s1 = this.get(i);
            Aligned_Sequences_Entity s2 = other.get(i);
            
            if(!s1.equals(s2))
                return false;
        }
        return true;
    }

    
    
    
}
