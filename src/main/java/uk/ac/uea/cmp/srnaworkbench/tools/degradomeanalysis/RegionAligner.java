/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

/**
 *
 * @author rew13hpu
 */
public abstract class RegionAligner {

    protected double score;
    protected int currentAdjacentMM;
    protected int currentGaps;
    protected int currentSRNAPosition;
    protected int currentMRNAPosition;
    protected int currentGUWobbles;
    protected int currentMMs;
    protected String smallRNA;
    protected String messengerRNA;
    protected boolean gap_In_SRNA;
    protected boolean prevPosMM;
    protected boolean prevPosGU;
    protected boolean thisPosMM;
    protected boolean thisPosGU;

    public RegionAligner(RegionAligner aligner) {
        score = aligner.score;
        currentAdjacentMM = aligner.currentAdjacentMM;
        currentGaps = aligner.currentGaps;
        currentSRNAPosition = aligner.currentSRNAPosition;
        currentMRNAPosition = aligner.currentMRNAPosition;
        currentGUWobbles = aligner.currentGUWobbles;
        currentMMs = aligner.currentMMs;
        smallRNA = aligner.smallRNA;
        messengerRNA = aligner.messengerRNA;
        prevPosGU = aligner.prevPosGU;
        prevPosMM = aligner.prevPosMM;

    }

    public RegionAligner(String sRNA, String mRNA) {
        smallRNA = sRNA;
        messengerRNA = mRNA;
    }

    protected boolean hasReachedBaseCase() {
        
        double totalNum = (double) currentMMs + ((double) currentGUWobbles * RuleSet.GU_MM_SCORE);

        if (RuleSet.GapCountAsMM) {
            totalNum += (double) currentGaps;
        }
        if (totalNum > RuleSet.MAX_TOTAL_MM) {
            return true;
        }

        if (currentMMs > RuleSet.MAX_MM) {
            return true;
        }

        if (score > RuleSet.MAX_SCORE) {
            //System.out.println("Score is above MAX_SCORE");
            return true;
        }

        if (currentAdjacentMM > RuleSet.getMAX_ADJACENT_MM()) {
            //System.out.println("Current AJ-MM is above MAX_ADJACENT_MM");
            return true;
        }

        if (currentGUWobbles > RuleSet.getMAX_GU_WOBBLES()) {
            //System.out.println("Current GU_WOBBLES is above MAX_GU_WOBBLES");
            return true;
        }

        return false;
    }

    abstract void startPath();

    abstract void buildPath();

    abstract boolean inCoreRegion();

    protected void update() {
        if (!thisPosGU && !thisPosMM) {
            prevPosMM = false;
            prevPosGU = false;
            return;
        }

        if (thisPosGU) {
            currentGUWobbles++;
            prevPosGU = true;
            if (RuleSet.isGUCountAsMM()) {
                currentMMs++;
                if (prevPosMM) {
                    currentAdjacentMM++;
                }
                prevPosMM = true;
            } else {
                prevPosMM = false;
            }
            prevPosGU = true;

        } else if (thisPosMM) {
            currentMMs++;
            if (prevPosMM) {
                currentAdjacentMM++;
            }
            prevPosMM = true;
            prevPosGU = false;
        }

    }

    protected double scorePair(char charA, char charB) {

        if (charA == 'A' && charB == 'T' || charA == 'T' && charB == 'A') {
            thisPosGU = false;
            thisPosMM = false;
            return 0;
        } else if (charA == 'C' && charB == 'G' || charA == 'G' && charB == 'C') {
            thisPosGU = false;
            thisPosMM = false;
            return 0;
        } else if (charA == 'G' && charB == 'T' || charA == 'T' && charB == 'G') {

            thisPosGU = true;
            if (RuleSet.isGUCountAsMM()) {
                thisPosMM = true;
            }

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {

                return RuleSet.SCORE_GU_WOBBLE * RuleSet.getCORE_REGION_MULTIPLIER();
            }

            return RuleSet.SCORE_GU_WOBBLE;
        }

        thisPosMM = true;
        thisPosGU = false;

        if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
            return RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
        }

        return RuleSet.SCORE_MM;
    }
}
