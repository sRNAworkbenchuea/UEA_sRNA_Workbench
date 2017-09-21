package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 *
 * @author matt
 */
@Entity
public class GFF_Attribute_Entity implements Serializable {
    @Id
    @GeneratedValue
    private Long attrId;
    
    @Column(name="key")
    private String key;

    @Column(name="value")
    private String value;
    
    @ManyToOne(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="id")
    private GFF_Entity gff;
    
    public GFF_Attribute_Entity(){}
    public GFF_Attribute_Entity(String key, String value)
    {
        this.key = key;
        this.value = value;
    }


    public GFF_Entity getGff() {
        return gff;
    }

    public void setGff(GFF_Entity gff) {
        this.gff = gff;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
}
