/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model;

/**
 *
 * @author Clau
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class LFoldInfo
{
  private int position;
  private int posOnGenome;
  private String fold;
  private double mfe;
  private double amfe;
  private double pVal;
  private int abundance;
  private ArrayList<Integer> pairs;
  private int miRNASide = 0;
  private double score = -2.147483648E9D;
  
  public int getMiRNASide()
  {
    return this.miRNASide;
  }
  
  public void setMiRNASide(int miRNASide)
  {
    this.miRNASide = miRNASide;
  }
  
  public double getpVal()
  {
    return this.pVal;
  }
  
  public void setpVal(double pVal)
  {
    this.pVal = pVal;
  }
  
  public double getAmfe()
  {
    return this.amfe;
  }
  
  public int getPosOnGenome()
  {
    return this.posOnGenome;
  }
  
  public void setPosOnGenome(int posOnGenome)
  {
    this.posOnGenome = posOnGenome;
  }
  
  private int bm = -1;
  private int em = -1;
  private int bms = -1;
  private int ems = -1;
  private int bl = -1;
  private int el = -1;
  private int hasMiRStarSeq = -1;
  
  public void setMiRStarSeq(int index)
  {
    this.hasMiRStarSeq = index;
  }
  
  public int getMiRStarSeq()
  {
    return this.hasMiRStarSeq;
  }
  
  public LFoldInfo(int position, String fold, double mfe)
  {
    this.position = position;
    this.fold = fold;
    this.mfe = mfe;
    this.amfe = (mfe / fold.length() * 100.0D);
    this.pairs = gatherPairs();
  }
  
  public int getPosition()
  {
    return this.position;
  }
  
  public void setPosition(int position)
  {
    this.position = position;
  }
  
  public String getFold()
  {
    return this.fold;
  }
  
  public void setFold(String fold)
  {
    this.fold = fold;
  }
  
  public double getMfe()
  {
    return this.mfe;
  }
  
  public void setMfe(double mfe)
  {
    this.mfe = mfe;
  }
  
  public int getBm()
  {
    return this.bm;
  }
  
  public void setBm(int bm1)
  {
    this.bm = bm1;
  }
  
  public int getEm()
  {
    return this.em;
  }
  
  public void setEm(int em1)
  {
    this.em = em1;
  }
  
  public int getBms()
  {
    return this.bms;
  }
  
  public void setBms(int bms1)
  {
    this.bms = bms1;
  }
  
  public int getEms()
  {
    return this.ems;
  }
  
  public void setEms(int ems1)
  {
    this.ems = ems1;
  }
  
  public int getBl()
  {
    return this.bl;
  }
  
  public void setBl(int bl)
  {
    this.bl = bl;
  }
  
  public int getEl()
  {
    return this.el;
  }
  
  public void setEl(int el)
  {
    this.el = el;
  }
  
  public void setAbundance(int ab)
  {
    this.abundance = ab;
  }
  
  public String toString()
  {
    return "LFoldInfo{position=" + this.position + ", posOnGenome=" + this.posOnGenome + ", fold=" + this.fold + ", mfe=" + this.mfe + ", amfe=" + this.amfe + ", pVal=" + this.pVal + ", bm=" + this.bm + ", em=" + this.em + ", bms=" + this.bms + ", ems=" + this.ems + ", bl=" + this.bl + ", el=" + this.el + '}';
  }
  
  public int getPairPosition(int i, boolean isOnLeft) {
        if (isOnLeft) {
            int l = this.pairs.size() - 1;
            if (i == 0) {
                i = 1;
            }
            int j;
            if (this.pairs.get(i) == -1 && (i > 0)) {
                j = i - 1;
            } else {
                j = i;
            }
            while (this.pairs.get(j) == -1 && (j > 0)) {
                j--;
            }
            int pair = this.pairs.get(j);
            int dif = Math.abs(i - j);

            for (int k = this.pairs.size() - 1; k >= 0; k--) {
                if (this.pairs.get(k) == pair) {
                    //k is the index of your pair
                    if (this.pairs.get(i) == -1 && (k > 0) && this.pairs.get(k - 1) == -1) {
                        l = k - 1;
                    } else {
                        l = k;
                    }
                    boolean inWhile = false;
                    while (this.pairs.get(l) == -1 && (k - l <= dif) && (l > 0)) {
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
        } else{
            int r = 0;
            int j;
            if (i == this.fold.length() - 1) {
                i = this.fold.length() - 2;
            }
            
            if (this.pairs.get(i) == -1) {
                j = i + 1;
            } else {
                j = i;
            }
            while (this.pairs.get(j) == -1 && (j < this.pairs.size() - 1)) {
                j++;
            }
            int pair = this.pairs.get(j);
            int dif = Math.abs(i - j);
            
            for (int k = 0; k < this.pairs.size(); k++) {
                if ( this.pairs.get(k) == pair) {
                    if (this.pairs.get(i) == -1 && (k < this.pairs.size() - 1) && this.pairs.get(k + 1) == -1) {
                        r = k + 1;
                    } else {
                        r = k;
                    }
                    boolean inWhile = false;
                    while (this.pairs.get(r) == -1 && (r - k <= dif) && (r < this.pairs.size() - 1)) {
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
        ArrayList<Integer> pairs = new ArrayList();

        LinkedList<Integer> structureStarts = new LinkedList();
        int currentOpen = 0;
        int currentClose = 0;
        char openBracket = '(';
        char closeBracket = ')';
        char dot = '.';
        for (int i = 0; i < this.fold.length(); i++) {
            if (this.fold.charAt(i) == dot) {
                pairs.add(-1);
            }
            if (this.fold.charAt(i) == openBracket) {
                structureStarts.addFirst(currentOpen);
                pairs.add(currentOpen);
                currentOpen++;
            } else if (this.fold.charAt(i) == closeBracket) {
                try {
                    currentClose = structureStarts.removeFirst();
                } catch (NoSuchElementException e) {
                    throw e;
                }
                pairs.add(currentClose);
            }
        }
        return pairs;
    }
  
  public int getOtherMirBeg(sRNA s)
  {
    if (this.bm == s.beg - this.posOnGenome) {
      return this.bms;
    }
    if (this.bms == s.beg - this.posOnGenome) {
      return this.bm;
    }
    return -1;
  }
  
  public int getOtherMirEnd(sRNA s)
  {
    if (this.bm == s.beg - this.posOnGenome) {
      return this.ems;
    }
    if (this.bms == s.beg - this.posOnGenome) {
      return this.em;
    }
    return -1;
  }
  
  public double getScore()
  {
    if (this.score == Integer.MIN_VALUE) {
      calculateScore();
    }
    return this.score;
  }
  
  private static final double score_star = 3.9;
  private static final double score_star_not = -1.3;
  
  private void calculateScore()
  {
    this.score = scoreAmfe(this.amfe);
    
    this.score += scoreFreq();
    if (this.hasMiRStarSeq != -1) {
      this.score += score_star;
    } else {
      this.score += score_star_not;
    }
  }
  
  private double scoreFreq()
  {
    double parameter_test = 0.999;
    double parameter_control = 0.6;
    
    double intercept = Math.log((1 - parameter_test) / (1 - parameter_control));
    double slope = Math.log(parameter_test / parameter_control);
    double log_odds = slope * this.abundance + intercept;
    if (this.hasMiRStarSeq == -1) {
      log_odds = Math.min(log_odds, log_odds / 10);
    }
    return log_odds;
  }
  
  private double scoreAmfe(double amfe)
  {
    double mfe_adj = Math.max(1.0, -amfe);
    
    double prob_test = probGumbelDiscretized(mfe_adj, 5.5, 32);
    double prob_background = probGumbelDiscretized(mfe_adj, 4.8, 23);
    
    double odds = prob_test / prob_background;
    double log_odds = Math.log(odds);
    
    return log_odds;
  }
  
  private double probGumbelDiscretized(double var, double scale, double location)
  {
    double bound_lower = var - 0.5D;
    double bound_upper = var + 0.5D;
    
    double cdf_lower = cdfGumbel(bound_lower, scale, location);
    double cdf_upper = cdfGumbel(bound_upper, scale, location);
    
    double prob = cdf_upper - cdf_lower;
    return prob;
  }
  
  private double cdfGumbel(double var, double scale, double location)
  {
    //double cdf = Math.pow(2.718281828459045D, -1.0D * Math.pow(2.718281828459045D, -1.0D * (var - location) / scale));
    double cdf = Math.pow(Math.E, (-1) * Math.pow(Math.E, (-1) * (var - location) / scale));
    return cdf;
  }
}

