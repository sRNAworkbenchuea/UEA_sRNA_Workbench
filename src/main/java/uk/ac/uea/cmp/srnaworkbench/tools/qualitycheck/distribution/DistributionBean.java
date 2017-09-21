/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * Used to neatly store a retrieved query containing a value and the fields needed
 * to place it in the right distribution
 */
public class DistributionBean {

    public double expression;
    public Filename_Entity sample;
    public NormalisationType normType;
    public Annotation_Type_Entity annotation;
    public String annotationSet;
    public int totalCount;

    public DistributionBean() {
    }

    public double getExpression() {
        return expression;
    }

    public void setExpression(double expression) {
        this.expression = expression;
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

    public Annotation_Type_Entity getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation_Type_Entity annotation) {
        this.annotation = annotation;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
}
