/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.shortreadaligner;

/**
 *
 * @author rew13hpu
 */
public class ShortReadAlignment implements Comparable<ShortReadAlignment>{
    ShortRead tag;
    int genePosition;
    boolean negativeStrand;
    
    public ShortReadAlignment(ShortRead t, int position)
    {
        tag = t;
        genePosition = position + 1;
        negativeStrand = false;
        tag.incrementNumAlignments();
    }
    
    public ShortReadAlignment(ShortRead t, int position, boolean negative)
    {
        this(t, position);
        negativeStrand = negative;
    }

    public ShortRead getTag() {
        return tag;
    }

    public int getGenePosition() {
        return genePosition;
    }
    
    public String getStrand()
    {
        return negativeStrand ? "-" : "+";
    }

    @Override
    public int compareTo(ShortReadAlignment o) {
        return Integer.compare(tag.abundance, o.tag.abundance);
    }
    
    
}
