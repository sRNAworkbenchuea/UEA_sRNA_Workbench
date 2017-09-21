/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashMap;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author matt
 */
@Embeddable
public class AbundanceWindow implements Serializable {

    @Column(name = "Window_Min")
    double min;

    @Column(name = "Window_Max")
    double max;
    
    @Column(name = "Window_Size", nullable=true)
    int windowSize;

    HashMap<Integer, Integer> percentileRanks;

    public AbundanceWindow(double minAbundance, double maxAbundance) {
        this.min = minAbundance;
        this.max = maxAbundance;
    }

    // Default constructor for entity
    public AbundanceWindow() {
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public HashMap<Integer, Integer> getPercentileRanks() {
        return this.percentileRanks;
    }

    public double getMinAbundance() {
        return min;
    }

    public double getMaxAbundance() {
        return max;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AbundanceWindow) {
            AbundanceWindow that = (AbundanceWindow) obj;
            return this.min == that.min && this.max == that.max;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.min + " - " + this.max;
    }
}
