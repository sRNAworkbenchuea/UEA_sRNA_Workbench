/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author Josh
 */
public class Alignment {
    
    String sRNA;
    String mRNA;
    String alignmentSymbols;
    SmallRNA smallRNA;
    TranscriptFragment tag;
    double score;
    
   
    
    public Alignment(AlignmentPath apL, AlignmentPath apR, SmallRNA smallRNA, TranscriptFragment tag)
    {
        String[] arr1 = apL.getFinalString();
        String[] arr2 = apR.getFinalString();
        this.tag = tag;
        this.smallRNA = smallRNA;
        sRNA = arr1[0] + arr2[0];
        alignmentSymbols = arr1[1] + arr2[1];
        mRNA = arr1[2] + arr2[2];
        score = apL.score + apR.score;
       
    }
    
}
