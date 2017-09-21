package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FoldChangeDirection;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author Matthew
 */
@Table(name="ExpressionFoldChanges")
@Entity
public class Expression_Fold_Change_Entity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    
    @Enumerated
    private NormalisationType normType;
    
    // typically, these are the average on the confidence intervals.
    // They are stored here as a hack to circumvent the painfulness that would
    // be storing Sample_Sequence_Entities and CI_Entities that represent zero expression
    // levels, which are so far not included in the database.
    @Column(name="Reference_Expression")
    private double reference_expression;
    
    @Column(name="Observed_Expression")
    private double observed_expression;
    
    @Column(name="Fold_Change")
    private double foldChange;
    
    @Column(name="Average_Abundance")
    private double averageAbundance;
    
    @Enumerated(value=EnumType.STRING)
    private FoldChangeDirection direction;
    
    @ManyToOne
    @JoinColumn(name="Fold_Change_Sequence_FK")
    private Sequence_Fold_Change_Entity sequence;

    public Expression_Fold_Change_Entity() {
    }

    public Expression_Fold_Change_Entity(NormalisationType normType, double reference_expression, double observed_expression, double foldChange, double averageAbundance, FoldChangeDirection direction) {
        this.normType = normType;
        this.reference_expression = reference_expression;
        this.observed_expression = observed_expression;
        this.foldChange = foldChange;
        this.averageAbundance = averageAbundance;
        this.direction = direction;
    }

    public double getReference_expression() {
        return reference_expression;
    }

    public void setReference_expression(double reference_expression) {
        this.reference_expression = reference_expression;
    }

    public double getObserved_expression() {
        return observed_expression;
    }

    public void setObserved_expression(double observed_expression) {
        this.observed_expression = observed_expression;
    }

    public double getAverageAbundance() {
        return averageAbundance;
    }

    public void setAverageAbundance(double averageAbundance) {
        this.averageAbundance = averageAbundance;
    }

    public Sequence_Fold_Change_Entity getSequence() {
        return sequence;
    }

    public void setSequence(Sequence_Fold_Change_Entity sequence) {
        this.sequence = sequence;
    }
       
    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public double getFoldChange() {
        return foldChange;
    }

    public void setFoldChange(double foldChange) {
        this.foldChange = foldChange;
    }

    public FoldChangeDirection getDirection() {
        return direction;
    }

    public void setDirection(FoldChangeDirection direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "Expression_Fold_Change_Entity{" + "id=" + id + ", normType=" + normType + ", foldChange=" + foldChange + ", direction=" + direction + '}';
    }
    
}
