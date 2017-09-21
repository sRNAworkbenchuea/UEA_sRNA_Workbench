/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author keu13sgu
 */

@Entity
@Table(name = "LFOLD_PRECURSORS")
public class LFold_Precursor_Entity implements Serializable{
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private final static double score_star = 3.9;
    private final static double score_star_not = -1.3;

    @OneToOne(cascade = CascadeType.ALL)
    private Precursor_Entity precursor;
    
    @Column(name = "PVAL")
    private double pVal;
    
    @Column(name = "Abundance")
    private int abundance;
    
    //-1 = left, 1 = right, 0 = unset
    @Column(name = "miRNASide")
    private int miRNASide = 0;
    
    @Column(name = "Score")
    private double score = Integer.MIN_VALUE;
    
    //begin of miRNA (on the 5' end of precursor)
    @Column(name = "BM")
    private int bm = -1;
    
    @Column(name = "EM")
    private int em = -1;
    
    //begin of miRNA (on the 3' end of precursor)
    @Column(name = "BMS")
    private int bms = -1;
     
    @Column(name = "EMS")
    private int ems = -1;
    
    //begin of loop
    @Column(name = "BL")
    private int bl = -1;
    
    @Column(name = "EL")
    private int el = -1;
    
    //index of the mirStar
    @Column(name = "hasMIRStarSeq")
    private int miRStarSeq = -1;

    public LFold_Precursor_Entity() {
    }
    
    
//    public LFold_Precursor_Entity(int position, String fold, double mfe) {
//        this.precursor = new Precursor_Entity();
//        
//        this.precursor.setMFE(mfe);
//        this.precursor.setStructure(fold);
//    }
    
    public LFold_Precursor_Entity(int position, String fold, double mfe, String seq, String chrId, String strand) {
        this.precursor = new Precursor_Entity(seq, fold, chrId, position, fold.length() + position - 1, strand, mfe);
    }
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public int getMiRNASide() {
        return miRNASide;
    }

    public void setMiRNASide(int miRNASide) {
        this.miRNASide = miRNASide;
    }

    
    public double getpVal() {
        return pVal;
    }

    public void setpVal(double pVal) {
        this.pVal = pVal;
    }

    public double getAmfe() {
        return precursor.getMFE() / (double) precursor.getStructure().length() * 100;
    }

    public int getPosOnGenome() {
        return precursor.getStart();
    }

//    public void setPosOnGenome(int posOnGenome) {
//        this.posOnGenome = posOnGenome;
//    }
    
    public void setMiRStarSeq(int index) {
        miRStarSeq = index;
    }

    public int getMiRStarSeq() {
        return miRStarSeq;
    }
    
    public String getFold() {
        return precursor.getStructure();
    }

    public double getMfe() {
        return precursor.getMFE();
    }

    public int getBm() {
        return bm;
    }

    public void setBm(int bm1) {
        this.bm = bm1;
    }

    public int getEm() {
        return em;
    }

    public void setEm(int em1) {
        this.em = em1;
    }

    public int getBms() {
        return bms;
    }

    public void setBms(int bms1) {
        this.bms = bms1;
    }

    public int getEms() {
        return ems;
    }

    public void setEms(int ems1) {
        this.ems = ems1;
    }

    public int getBl() {
        return bl;
    }

    public void setBl(int bl) {
        this.bl = bl;
    }

    public int getEl() {
        return el;
    }

    public void setEl(int el) {
        this.el = el;
    }

    public void setAbundance(int ab) {
        this.abundance = ab;
    }
    
    public int getAbundance() {
        return this.abundance;
    }
    
    public String getSequence(){
        return this.precursor.getSequence();
    }
    
    public int getPairPosition(int i, boolean isOnLeft) {

        ArrayList<Integer> pairs = gatherPairs();
        
        if (isOnLeft) {
            int l = pairs.size() - 1;
            int j;
            
            if(i == 0)
                i = 1;
            
            if (pairs.get(i) == -1 && i > 0) {
                j = i - 1;
            } else {
                j = i;
            }

            while (pairs.get(j) == -1 && j > 0){// pairs.size() - 1) {
                j--;
            }
            int pair = pairs.get(j);
            int dif = Math.abs(i - j);

            for (int k = pairs.size() - 1; k >= 0; k--) {
                if (pairs.get(k) == pair) {
                    //k is the index of your pair
                    if (pairs.get(i) == -1 && k > 0 && pairs.get(k - 1) == -1) {
                        l = k - 1;
                    } else {
                        l = k;
                    }
                    boolean inWhile = false;
                    while (pairs.get(l) == -1 && (k - l) <= dif && l > 0) {
                        l--;
                        inWhile = true;
                    }
                    if (inWhile) {
                        l++;
                    }
                    break;
                }
            }
            return l;
        } else {
            int r = 0;
            int j;
            if (i == getFold().length() - 1)
                i = getFold().length() - 2;
            
            if (pairs.get(i) == -1) {
                j = i + 1;
            } else {
                j = i;
            }

            while (pairs.get(j) == -1 && j < pairs.size() - 1) {
                j++;
            }
            int pair = pairs.get(j);
            int dif = Math.abs(i - j);

            for (int k = 0; k < pairs.size(); k++) {
                if (pairs.get(k) == pair) {
                    //k is the index of your pair
                    if (pairs.get(i) == -1 && k < pairs.size() - 1 && pairs.get(k + 1) == -1) {
                        r = k + 1;
                    } else {
                        r = k;
                    }

                    boolean inWhile = false;
                    while (pairs.get(r) == -1 && (r - k) <= dif && r < pairs.size() - 1) {
                        inWhile = true;
                        r++;
                    }
                    if (inWhile) {
                        r--;
                    }
                    break;
                }
            }
            return r;
        }

    }

    private ArrayList<Integer> gatherPairs() {
        ArrayList<Integer> pairs = new ArrayList<Integer>();

        LinkedList<Integer> structureStarts = new LinkedList<Integer>();
        int currentOpen = 0, currentClose = 0;
        char openBracket = '(';
        char closeBracket = ')';
        char dot = '.';
        for (int i = 0; i < getFold().length(); i++) {
            if (getFold().charAt(i) == dot) {
                pairs.add(-1);
            }
            if (getFold().charAt(i) == openBracket) {
                structureStarts.addFirst(currentOpen);
                pairs.add(currentOpen);
                currentOpen++;
            } else {
                if (getFold().charAt(i) == closeBracket) {
                    try {
                        currentClose = structureStarts.removeFirst();
                    } catch (java.util.NoSuchElementException e) {

                        throw e;
                    }
                    pairs.add(currentClose);
                }
            }
        }
        return pairs;
    }
    
    public int getOtherMirBeg(Aligned_Sequences_Entity s) {
        if (bm == (s.getStart1() - getPosOnGenome())) {
            return bms;
        } else if (bms == (s.getStart1() - getPosOnGenome())) {
            return bm;
        }
        return -1;
    }

    public int getOtherMirEnd(Aligned_Sequences_Entity s) {
        if (bm == (s.getStart1() - getPosOnGenome())) {
            return ems;
        } else if (bms == (s.getStart1() - getPosOnGenome())) {
            return em;
        }
        return -1;
    }

    public double getScore() {
        if (score == Integer.MIN_VALUE) {
            calculateScore();
        }
        return score;
    }
    
     private void calculateScore(){
        // this is the scoring minimum free energy of the potential precursor
      
        score = scoreAmfe(getAmfe());

        //score += scoreAmfe(mfe);
        score += scoreFreq();


        //if the majority of potential star reads fall as expected from Dicer processing
        if(miRStarSeq != -1){
            score += score_star;
        }else{
            score += score_star_not;
        }
    }

 private double scoreFreq(){
        //scores the count of reads that map to the potential precursor
        //Assumes geometric distribution
        //parameters of known precursors and background hairpins
        double parameter_test = 0.999;
        double parameter_control = 0.6;

        //log_odds calculated directly to avoid underflow
        double intercept = Math.log((1 - parameter_test) / (1 - parameter_control));
        double slope = Math.log(parameter_test / parameter_control);
        double log_odds = slope * this.abundance + intercept;

        //#if no strong evidence for 3' overhangs, limit the score contribution to 0
        if (miRStarSeq == -1){
            log_odds = Math.min(log_odds, log_odds/10);
        }
        
        //System.out.println(log_odds);
      
        return log_odds;
}



  private double scoreAmfe(double amfe){
        double mfe_adj = Math.max(1, -amfe);
        
        //parameters of known precursors and background hairpins, scale and location
        double prob_test = probGumbelDiscretized(mfe_adj, 5.5, 32);
        double prob_background = probGumbelDiscretized(mfe_adj, 4.8, 23);
        
        double odds = prob_test/prob_background;
        double log_odds = Math.log(odds);
        
        //System.out.println(log_odds);

        return log_odds;
    }


    private double probGumbelDiscretized(double var, double scale, double location) {

        double bound_lower = var - 0.5;
        double bound_upper = var + 0.5;

        double cdf_lower = cdfGumbel(bound_lower, scale, location);
        double cdf_upper = cdfGumbel(bound_upper, scale, location);

        double prob = cdf_upper - cdf_lower;
        return prob;
    }

    private double cdfGumbel(double var, double scale, double location) {
        
        //calculates the cumulative distribution function of the Gumbel distribution
        double cdf = Math.pow(Math.E, (-1) * Math.pow(Math.E, (-1) * (var - location) / scale));
        
        return cdf;
    }

    public int getPosition(int b) {
        return this.getPosOnGenome() - b;
    }
    
}
