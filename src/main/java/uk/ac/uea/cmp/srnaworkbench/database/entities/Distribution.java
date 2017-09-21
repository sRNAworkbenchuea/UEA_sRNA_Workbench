/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;

/**
 *
 * @author matt
 */
@Entity
public class Distribution implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "Log_Base")
    private String logBase;

    @Column(name = "Lower_Quartile")
    private double lq;

    @Column(name = "Median")
    private double med;

    @Column(name = "Upper_Quartile")
    private double uq;

    @Column(name = "Lower_Range")
    private double minRange;

    @Column(name = "Upper_Range")
    private double maxRange;
    
    @Column(name = "Number_Of_Sequences", nullable = true)
    private int numSeqs;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="distribution", cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<Distribution_Outlier> outliers = new HashSet<>();

    public Distribution(Logarithm logBase, double lq, double med, double uq, double minRange, double maxRange) {
        this.logBase = logBase.toString();
        this.lq = lq;
        this.med = med;
        this.uq = uq;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }
    
    protected Distribution(Distribution dist)
    {
        this.logBase = dist.logBase;
        this.lq = dist.lq;
        this.med = dist.med;
        this.uq = dist.uq;
        this.minRange = dist.minRange;
        this.maxRange = dist.maxRange;
        this.outliers = dist.outliers;
        for(Distribution_Outlier outlier: outliers)
        {
            outlier.setDist(this);
        }
    }

    // Default constructor for entity
    public Distribution() {
    }

    public int getNumSeqs() {
        return numSeqs;
    }

    public void setNumSeqs(int numSeqs) {
        this.numSeqs = numSeqs;
    }
    
    public Logarithm getLogBase() {
        return Logarithm.fromString(logBase);
    }

    public Set<Distribution_Outlier> getOutliers() {
        return outliers;
    }

    public void setOutliers(Set<Distribution_Outlier> outliers) {
        for(Distribution_Outlier outlier : outliers)
        {
            outlier.setDist(this);
        }
        this.outliers = outliers;
    }

    public void setLogBase(Logarithm logBase) {
        this.logBase = logBase.toString();
    }

    public double getLq() {
        return lq;
    }

    public void setLq(double lq) {
        this.lq = lq;
    }

    public double getMed() {
        return med;
    }

    public void setMed(double med) {
        this.med = med;
    }

    public double getUq() {
        return uq;
    }

    public void setUq(double uq) {
        this.uq = uq;
    }

    public double getMinRange() {
        return minRange;
    }

    public void setMinRange(double minRange) {
        this.minRange = minRange;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("| -").append(this.getMinRange())
                .append("---").append(this.getLq())
                .append("|==").append(this.getMed()).append("==|")
                .append(this.getUq()).append("---")
                .append(this.getMaxRange()).append("-|")
                .append(" (").append(this.getLogBase().toString()).append(")")
                .append("]")
                .append(" numseqs=").append(this.getNumSeqs());
        
        
        
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   
}
