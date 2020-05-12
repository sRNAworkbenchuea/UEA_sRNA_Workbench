/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

/**
 *
 * @author rew13hpu
 */
public class AlignmentCluster {
    int numAlignments;
    int startSite;
    
    public AlignmentCluster(int start, int numAlignments)
    {
        startSite = start;
        this.numAlignments = numAlignments;
    }

    public int getStartSite() {
        return startSite;
    }

    public void setStartSite(int startSite) {
        this.startSite = startSite;
    }
    
    public int getNumAlignments() {
        return numAlignments;
    }
    
    public void incrementAlignments(int num)
    {
        numAlignments+=num;
    }
}
