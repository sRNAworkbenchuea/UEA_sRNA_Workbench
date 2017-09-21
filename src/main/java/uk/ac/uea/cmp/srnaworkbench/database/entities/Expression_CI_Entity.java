package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author Matthew
 */
@Table(name="Expression_Confidence_Intervals")
@Entity
public class Expression_CI_Entity implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="Id")
    private Long id;
    
    @Enumerated
    @Column(name="NormalisationType")
    private NormalisationType normType;
    
    @Column(name="Interval_Minimum")
    private double minExpression;
    
    @Column(name="Interval_Maximum")
    private double maxExpression;
    
    @Column(name="Interval_Average")
    private double avgExpression; 
    
    @ManyToOne
    @JoinColumn(name="Sample_Sequence_FK")
    private Sample_Sequence_Entity sequence;

    public Sample_Sequence_Entity getSequence() {
        return sequence;
    }
    
    public Expression_CI_Entity(){}
    public Expression_CI_Entity(NormalisationType normType, double min, double avg, double max)
    {
        this.normType = normType;
        this.minExpression = min;
        this.maxExpression = max;
        this.avgExpression = avg;
    }

    public void setSequence(Sample_Sequence_Entity sequence) {
        this.sequence = sequence;
    }
    
    

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public double getMinExpression() {
        return minExpression;
    }

    public void setMinExpression(double minExpression) {
        this.minExpression = minExpression;
    }

    public double getMaxExpression() {
        return maxExpression;
    }

    public void setMaxExpression(double maxExpression) {
        this.maxExpression = maxExpression;
    }

    public double getAvgExpression() {
        return avgExpression;
    }

    public void setAvgExpression(double avgExpression) {
        this.avgExpression = avgExpression;
    }

    @Override
    public String toString() {
        return "Expression_CI_Entity{" + "id=" + id + ", normType=" + normType + ", minExpression=" + minExpression + ", maxExpression=" + maxExpression + ", avgExpression=" + avgExpression + '}';
    }
    
}
