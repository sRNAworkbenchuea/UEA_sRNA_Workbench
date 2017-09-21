/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author Matt
 */
@Table(name="Sequence_Expressions")
@Entity
public class Expression_Entity implements Serializable
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="Expression_Id")
    Long id;
    
    @Column(name="Expression")
    double expression;
    
    @Enumerated
    @Column(name="Normalisation_Type")
    NormalisationType normType;
    
    @ManyToOne()
    @JoinColumn(name="Sequence_Id")
    Sequence_Entity sequence;
    
    public Expression_Entity(){}

    public Expression_Entity(double expression, NormalisationType normType, Sequence_Entity sequence)
    {
        this.expression = expression;
        this.normType = normType;
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getExpression()
    {
        return expression;
    }

    public void setExpression(double expression)
    {
        this.expression = expression;
    }

    public NormalisationType getNormType()
    {
        return normType;
    }

    public void setNormType(NormalisationType normType)
    {
        this.normType = normType;
    }

    public Sequence_Entity getSequence()
    {
        return sequence;
    }

    public void setSequence(Sequence_Entity sequence)
    {
        this.sequence = sequence;
    }

    @Override
    public String toString()
    {
        return this.normType.getAbbrev() + ": " + this.expression;
    }
    
    
    
}
