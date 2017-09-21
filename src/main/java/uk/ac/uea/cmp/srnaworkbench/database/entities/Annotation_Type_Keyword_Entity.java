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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 *
 * @author Matthew
 */
@Entity
@Table(name="Annotation_Type_Keywords")
public class Annotation_Type_Keyword_Entity implements Serializable{
    
    @Id
    @Column(name="Keyword")
    private String keyword;
    
//    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="Keyword_Type", joinColumns={@JoinColumn(name="Keyword")}, 
//            inverseJoinColumns={@JoinColumn(name="id.type"), 
//                                @JoinColumn(name="id.reference")})
    @ManyToMany(cascade=CascadeType.ALL)
     @JoinTable(name="type_keyword", 
        joinColumns={
            @JoinColumn(name="keyword")}, 
        inverseJoinColumns={
            @JoinColumn(name="reference"), @JoinColumn(name="type")})
    private Set<Annotation_Type_Entity> types = new HashSet<>(0);
    
    /**
     * Create a new keyword entity that relates to the specified type.
     * @param keyword
     * @param type 
     */
    public Annotation_Type_Keyword_Entity(String keyword, Annotation_Type_Entity type)
    {
        this.keyword = keyword;
        this.types.add(type);
    }
    public Annotation_Type_Keyword_Entity(){}

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Set<Annotation_Type_Entity> getTypes() {
        return types;
    }

    public void setTypes(Set<Annotation_Type_Entity> types) {
        this.types = types;
    }



    @Override
    public String toString() {
        return "Annotation_Type_Keyword{" + "keyword=" + keyword + '}';
    }
    
    
}
