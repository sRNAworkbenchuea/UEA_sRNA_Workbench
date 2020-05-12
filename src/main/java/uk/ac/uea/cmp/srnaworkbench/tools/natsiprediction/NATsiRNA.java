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
public class NATsiRNA implements Comparable<NATsiRNA>{

    String sequence;
    String starSequence;
    int category;
    NATPair nat;
    int alignmentPosition;
    int starAlignmentPosition;
    int abundance;
    String alignmentGene;
    String oppositeGene;

    public NATsiRNA(String sequence, NATPair nat, int alignmentPosition) {
        this.sequence = sequence;
        this.nat = nat;
        this.alignmentPosition = alignmentPosition;
    }

    public void setAbundance(int abundance) {
        this.abundance = abundance;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public NATPair getNat() {
        return nat;
    }

    public int getAlignmentPosition() {
        return alignmentPosition;
    }

    public String getStarSequence() {
        return starSequence;
    }

    public void setStarSequence(String starSequence) {
        this.starSequence = starSequence;
    }

    public int getStarAlignmentPosition() {
        return starAlignmentPosition;
    }

    public void setStarAlignmentPosition(int starAlignmentPosition) {
        this.starAlignmentPosition = starAlignmentPosition;
    }

    public String getAlignmentGene() {
        return alignmentGene;
    }

    public void setAlignmentGene(String alignmentGene) {
        this.alignmentGene = alignmentGene;
    }

    public String getOppositeGene() {
        return oppositeGene;
    }

    public void setOppositeGene(String oppositeGene) {
        this.oppositeGene = oppositeGene;
    }

    @Override
    public String toString() {
        //Here we can build the results String for output file

        /**
         * bw.write("#, Sequence, Biogenesis category, Gene A (originating
         * transcript), Alignment site, Gene B (corresponding NAT), Type,
         * Alignment distribution, Gene A overlap start," + "Gene A overlap end,
         * Gene B overlap start, Gene B overlap end, Gene A alignment density,
         * Gene A overlap alignment density, RNAplex alignment");
         * bw.write("\n"); *
         */
        StringBuilder sb = new StringBuilder();

        sb.append(sequence).append(",");
        sb.append(abundance).append(",");
        sb.append(category).append(",");
        sb.append(alignmentGene).append(",");
        sb.append(alignmentPosition).append(",");
        sb.append(oppositeGene).append(",");
        if(starSequence != null)
        {
            sb.append(starSequence).append(",");
            sb.append(starAlignmentPosition).append(",");
        }
        else
        {
           sb.append("N/A").append(",");
            sb.append("N/A").append(","); 
        }
        sb.append(nat.getType()).append(",");
        sb.append(nat.getCoverageType()).append(",");

        if (alignmentGene.equals(nat.getQueryName())) {
            sb.append(nat.getQueryDistributionType()).append(",");
            sb.append(nat.getQuerySeq().length()).append(",");
            sb.append(nat.getQueryStart()).append(",");
            sb.append(nat.getQueryEnd()).append(",");
            sb.append(nat.getSubjectSeq().length()).append(",");
            sb.append(nat.getSubjectStart()).append(",");
            sb.append(nat.getSubjectEnd()).append(",");
            
            //make sure this is correct before including. It needs to be the number of positions with alignments
//            sb.append(nat.getQueryAlignmentRatio()).append(",");
//            sb.append(nat.getQueryOverlappingAlignmentRatio()).append(",");
        } else {
            sb.append(nat.getSubjectDistributionType()).append(",");
            sb.append(nat.getSubjectSeq().length()).append(",");
            sb.append(nat.getSubjectStart()).append(",");
            sb.append(nat.getSubjectEnd()).append(",");
            sb.append(nat.getQuerySeq().length()).append(",");
            sb.append(nat.getQueryStart()).append(",");
            sb.append(nat.getQueryEnd()).append(",");
            //make sure this is correct before including. It needs to be the number of positions with alignments
//            sb.append(nat.getSubjectAlignmentRatio()).append(",");
//            sb.append(nat.getSubjectOverlappingAlignmentRatio()).append(",");

        }

        sb.append(nat.getOveralDensitiy()).append(",");
        sb.append(nat.getOverlappingDensitity()).append(",");
//        sb.append(nat.getPlexResults().getSubjectHybrid());
//        sb.append("&");
//        sb.append(nat.getPlexResults().getQueryHybrid());

        return sb.toString();
    }

    public int getAbundance() {
        return abundance;
    }

    @Override
    public int compareTo(NATsiRNA o) {
        
        if(!alignmentGene.equals(o.getAlignmentGene()))
        {
            return alignmentGene.compareTo(o.getAlignmentGene());
        }
        
        if(!oppositeGene.equals(o.getOppositeGene()))
        {
            return oppositeGene.compareTo(o.getOppositeGene());
        }
        
        if(alignmentPosition != o.getAlignmentPosition())
        {
            return Integer.compare(alignmentPosition, o.getAlignmentPosition());
        }
        
        if(category != o.getCategory())
        {
            return Integer.compare(category, o.getCategory());
        }
        
        
        return Integer.compare(sequence.length(), o.getSequence().length());
        
    }

}
