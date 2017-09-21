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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;

/**
 *
 * @author matt
 */
@Entity
//@Table(name="ABUNDANCE_BOX_AND_WHISKERS")
public class AbundanceBoxAndWhisker_Entity extends Distribution implements Serializable
{
//    @Id
//    @Column(name="ID")
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
        
    @OneToOne
    @JoinColumn(name="File_Name")
//    @Column(name="File_Name")
    private Filename_Entity sample;
    
    @Enumerated
    private NormalisationType normType;
    
    @Column(name="Annotation_Type")
    String annotationType;
    
//    @Embedded
//    private Distribution distribution;
    
    @Embedded
    private AbundanceWindow window;
    
//    @OneToMany(fetch=FetchType.LAZY, mappedBy="dist", cascade = CascadeType.ALL)
//    Set<AbundanceBoxAndWhisker_Outlier> outliers = new HashSet<>();
    
    /**
     * No-argument constructor for Table-specific use
     */
    protected AbundanceBoxAndWhisker_Entity(){}

    public AbundanceBoxAndWhisker_Entity(Filename_Entity sample, NormalisationType normType, String annotationType, 
            Logarithm logBase, double lq, double med, double uq, double minRange, double maxRange, double minAbundance, double maxAbundance) {
        super(Logarithm.BASE_2,lq,med,uq,minRange,maxRange);
        this.sample = sample;
        this.normType = normType;
        this.annotationType = annotationType;
        //this.distribution = new Distribution(logBase, lq, med, uq, minRange, maxRange);
        this.window = new AbundanceWindow(minAbundance, maxAbundance);
    }
    public AbundanceBoxAndWhisker_Entity(Filename_Entity sample, NormalisationType normType, String annotationType,
            Distribution dist, double minAbundance, double maxAbundance) {
        super(dist);
        this.sample = sample;
        this.normType = normType;
        this.annotationType = annotationType;
        //this.distribution = new Distribution(logBase, lq, med, uq, minRange, maxRange);
        this.window = new AbundanceWindow(minAbundance, maxAbundance);
    }
    
    /**
     * Allows creation of an entity by initially setting sample and normType.
     * Distribution and window can be set later.
     * @param sample
     * @param normType 
     */
    public AbundanceBoxAndWhisker_Entity(Filename_Entity sample, NormalisationType normType, String annotationType)
    {
        this.sample = sample;
        this.normType = normType;
        this.annotationType = annotationType;
    }

//    public Distribution getDistribution() {
//        return distribution;
//    }
//
//    public void setDistribution(Distribution distribution) {
//        this.distribution = distribution;
//    }

    public AbundanceWindow getWindow() {
        return window;
    }

    public void setWindow(AbundanceWindow window) {
        this.window = window;
    }

    public Filename_Entity getSample() {
        return sample;
    }

    public void setSample(Filename_Entity sample) {
        this.sample = sample;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

//    public Set<AbundanceBoxAndWhisker_Outlier> getOutliers() {
//        return outliers;
//    }
//
//    public void setOutliers(Set<AbundanceBoxAndWhisker_Outlier> outliers) {
//        this.outliers = outliers;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append(this.id).append(":: ")
                sb.append(sample).append(", ").append(normType).append(", Annotation ").append(annotationType)
                .append("\n\t")
                //.append(this.getDistribution().toString())
                .append("[ ").append(this.getWindow().toString()).append(" ]");
        return (sb.toString());
    }
    
 }
