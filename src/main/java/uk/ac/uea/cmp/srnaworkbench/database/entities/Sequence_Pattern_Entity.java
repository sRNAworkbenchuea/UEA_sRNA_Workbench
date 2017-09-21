package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.Cascade;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.CascadeType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * Models the pattern that a sequence shows for a specific Pattern and Unique_Sequence
 * @author Matthew
 */
@Table(name="SequencePatterns")
@Entity
public class Sequence_Pattern_Entity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    
    // the sequence this describes
    @ManyToOne
    @JoinColumn(name="Unique_Sequence")
    private Unique_Sequences_Entity uniqueSequence;
    
    // the sequence fold changes that make up this pattern
    @OneToMany(mappedBy="sequence_pattern")
    private List<Sequence_Fold_Change_Entity> sequenceFoldChanges = new ArrayList<>();

    public List<Sequence_Fold_Change_Entity> getSequenceFoldChanges() {
        return sequenceFoldChanges;
    }

    public void setSequenceFoldChanges(List<Sequence_Fold_Change_Entity> sequenceFoldChanges) {
        this.sequenceFoldChanges = sequenceFoldChanges;
    }
    
    // The pattern that this sequence entry belongs to
    @ManyToOne
    @JoinColumn(name="Pattern")
    private Pattern_Entity pattern;
    
    @OneToMany(mappedBy="sequence")
    @MapKey(name="normType")
    private Map<NormalisationType, Expression_Pattern_Entity> expressionPatterns = new HashMap<>();

    public Sequence_Pattern_Entity(){}
    public Sequence_Pattern_Entity(Unique_Sequences_Entity uniqueSequence) {
        this.uniqueSequence = uniqueSequence;
    }

    public Map<NormalisationType, Expression_Pattern_Entity> getExpressionPatterns() {
        return expressionPatterns;
    }

    public void setExpressionPatterns(Map<NormalisationType, Expression_Pattern_Entity> expressionPatterns) {
        this.expressionPatterns = expressionPatterns;
    }
    
    public void addExpressionPattern(NormalisationType normType, String patternString)
    {
        this.expressionPatterns.put(normType, new Expression_Pattern_Entity(normType, patternString));
    }
    
    public Expression_Pattern_Entity getPattern(NormalisationType normType)
    {
        return this.expressionPatterns.get(normType);
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Unique_Sequences_Entity getUniqueSequence() {
        return uniqueSequence;
    }

    public void setUniqueSequence(Unique_Sequences_Entity uniqueSequence) {
        this.uniqueSequence = uniqueSequence;
    }

    public Pattern_Entity getPattern() {
        return pattern;
    }

    public void setPattern(Pattern_Entity pattern) {
        this.pattern = pattern;
    }
    
    

}
