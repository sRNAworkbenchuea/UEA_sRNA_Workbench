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
@Table(name="ExpressionPatterns")
@Entity
public class Expression_Pattern_Entity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    
    @Enumerated
    NormalisationType normType;
    
    @Column(name="Pattern_String")
    private String patternStr;
    
    @ManyToOne
    @JoinColumn(name="Pattern_Sequence_FK")
    private Sequence_Pattern_Entity sequence;
    
    public Expression_Pattern_Entity(){}
    public Expression_Pattern_Entity(NormalisationType normType, String patternString)
    {
        this.normType = normType;
        this.patternStr = patternString;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public Sequence_Pattern_Entity getSequence() {
        return sequence;
    }

    public void setSequence(Sequence_Pattern_Entity sequence) {
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatternStr() {
        return patternStr;
    }

    public void setPatternStr(String patternStr) {
        this.patternStr = patternStr;
    }

    @Override
    public String toString() {
        return "Expression_Pattern_Entity{" + "normType=" + normType + ", patternStr=" + patternStr + '}';
    }
    
}
