package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Matthew
 */
@Table(name="Patterns")
@Entity
public class Pattern_Entity implements Serializable {
    @Id
    private String patternString;
    
//    @OneToMany
//    @JoinTable(
//            name="Pattern_SamplePairs",
//            joinColumns = @JoinColumn(name="patternId"),
//            inverseJoinColumns = {@JoinColumn(name="referenceId"), @JoinColumn(name="observedId"), @JoinColumn(name="pairKey") }
//    )
//    private List<Sample_Pair_Entity> samplePairs = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name="SampleSeries_FK")
    private Sample_Series_Entity sampleSeries;
    
    @OneToMany(mappedBy="pattern")
    private Set<Sequence_Pattern_Entity> sequences = new HashSet<>();
    public Pattern_Entity(){}
    public Pattern_Entity(String patternString)
    {
        this.patternString = patternString;
    }

    public Sample_Series_Entity getSampleSeries() {
        return sampleSeries;
    }

    public void setSampleSeries(Sample_Series_Entity sampleSeries) {
        this.sampleSeries = sampleSeries;
    }
    
    
    public Set<Sequence_Pattern_Entity> getSequences() {
        return sequences;
    }

    public void setSequences(Set<Sequence_Pattern_Entity> sequences) {
        this.sequences = sequences;
    }

    public String getPatternString() {
        return patternString;
    }

    public void setPatternString(String patternString) {
        this.patternString = patternString;
    }

    
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

//    public List<Sample_Pair_Entity> getSamplePairs() {
//        return samplePairs;
//    }
//
//    public void setSamplePairs(List<Sample_Pair_Entity> samplePairs) {
//        this.samplePairs = samplePairs;
//    }
    
}
