/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;

/**
 *
 * @author rew13hpu
 */
public class ShortReadAlignment implements Comparable<ShortReadAlignment> {

    ShortRead tag;
    int genePosition;
    int endPos;
    boolean negativeStrand;

    public ShortReadAlignment(ShortRead t, int position) {
        tag = t;
        genePosition = position;
        endPos = genePosition + tag.length - 1;
        negativeStrand = false;
        //tag.incrementNumAlignmentsPositive();
    }

    public ShortReadAlignment(ShortRead t, int position, boolean negative) {
        this(t, position);
        negativeStrand = negative;
    }

    public ShortRead getTag() {
        return tag;
    }

    public int getGenePosition() {
        return genePosition;
    }

    public String getStrand() {
        return negativeStrand ? "-" : "+";
    }

    @Override
    public int compareTo(ShortReadAlignment o) {

        if (Paresnip2Configuration.getInstance().isUseWeightedFragmentAbundance()) {
            return Double.compare(tag.weightedAbundancePositive, o.tag.weightedAbundancePositive);
        }
        //otherwise use this
        return Integer.compare(tag.abundance, o.tag.abundance);
    }

    @Override
    public String toString() {
        return tag.toString() + " start: " + genePosition + " end: " + endPos;
    }

    @Override
    public int hashCode() {
        return (genePosition + getStrand()).hashCode();
        //return genePosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ShortReadAlignment other = (ShortReadAlignment) obj;
        
        if (this.genePosition != other.genePosition) {
            return false;
        }
        if (this.endPos != other.endPos) {
            return false;
        }
        
        return true;
    }
    
    

}
