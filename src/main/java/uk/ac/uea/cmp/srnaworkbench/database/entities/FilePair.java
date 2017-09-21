package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * An embeddable entity that represents a pair of files.
 * @author matt
 */
@Embeddable
public class FilePair implements Serializable {

    @Column(name = "REF_FILE")
    private String reference;
    @Column(name = "OBS_FILE")
    private String observed;
    
    private String pairKey;

    public FilePair(){}
    public FilePair(String reference, String observed) {
        
        this.reference = reference;
        this.observed = observed;
        
        if(this.reference.compareTo(this.observed) > 0)
        {
            pairKey = this.observed + "," + this.reference;
        }
        else
        {
            pairKey = this.reference + "," + this.observed;
        }
    }

    public static List<FilePair> generateAllPairs(List<String> samples) {
        List<FilePair> pairList = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            for (int j = i + 1; j < samples.size(); j++) {
                pairList.add(new FilePair(samples.get(i), samples.get(j)));
            }
        }
        return pairList;
    }

    public String getPairKey() {
        return pairKey;
    }

    public void setPairKey(String pairKey) {
        this.pairKey = pairKey;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getObserved() {
        return observed;
    }

    public void setObserved(String observed) {
        this.observed = observed;
    }

    // FIXME: The ordering of the two files SHOULD NOT MATTER, they are essentially
    // a set pair i.e. ref A, obs B is the same as ref B, obs A

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.pairKey);
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
        final FilePair other = (FilePair) obj;
        if (!Objects.equals(this.pairKey, other.pairKey)) {
            return false;
        }
        return true;
    }

    
    public String toString()
    {
        return "Reference: " + this.reference + ", Observered: " + this.observed;
    }
}
