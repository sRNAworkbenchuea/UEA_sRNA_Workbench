package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Matthew
 */
@Table(name="Sample_Series")
@Entity
public class Sample_Series_Entity implements Serializable {
    @Id
    private String seriesString;
    
    @OneToMany(mappedBy="sampleSeries")
    @MapKey(name="patternString")
    private Map<String, Pattern_Entity> patterns = new HashMap<>();
    
    @ManyToMany
    @JoinTable(name="sample_series_join", 
            joinColumns={@JoinColumn(name="seriesID")},
            inverseJoinColumns={@JoinColumn(name="referenceID"), @JoinColumn(name="observedID"), @JoinColumn(name="pairKey")})
    private List<Sample_Pair_Entity> samplePairs = new ArrayList<>();

    public Sample_Series_Entity() {
    }
    
    public Sample_Series_Entity(List<Sample_Pair_Entity> pairs){
        this.samplePairs=pairs;
        seriesString = "";
        
        for(Sample_Pair_Entity pair : pairs)
        {
            seriesString += "-" + pair.getSamplePair().getPairKey();
        }
    }
    
    public String getSeriesString() {
        return seriesString;
    }

    public void setSeriesString(String seriesString) {
        this.seriesString = seriesString;
    }

    public Map<String, Pattern_Entity> getPatterns() {
        return patterns;
    }

    public void setPatterns(Map<String, Pattern_Entity> patterns) {
        this.patterns = patterns;
    }

    public List<Sample_Pair_Entity> getSamples() {
        return samplePairs;
    }

    public void setSamples(List<Sample_Pair_Entity> samplePairs) {
        this.samplePairs = samplePairs;
    }
    
    
    
}
