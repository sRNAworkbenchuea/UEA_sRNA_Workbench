package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author mka07yyu
 */
@Entity
public class Distribution_Outlier implements Serializable {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name="outlier")
    private double outlier;
    
    @ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name="distribution")
    private Distribution distribution;
    
    public Distribution_Outlier(){}
    
    public Distribution_Outlier(double outlier)
    {
        this.outlier = outlier;
    }
    
    public double getOutlier()
    {
        return this.outlier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Distribution getDist() {
        return distribution;
    }

    public void setDist(Distribution dist) {
        this.distribution = dist;
    }
    
    @Override
    public String toString()
    {
        return Double.toString(this.outlier);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.outlier) ^ (Double.doubleToLongBits(this.outlier) >>> 32));
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
        final Distribution_Outlier other = (Distribution_Outlier) obj;
        if (Double.doubleToLongBits(this.outlier) != Double.doubleToLongBits(other.outlier)) {
            return false;
        }
        return true;
    }
    
    
}
