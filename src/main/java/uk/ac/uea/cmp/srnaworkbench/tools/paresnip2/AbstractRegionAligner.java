/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author rew13hpu/Joshua thody
 */
public abstract class AbstractRegionAligner {

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
    protected RuleSet ruleset;
    protected int currentMMsCore;
    protected int currentAdjacentMMCore;

    public AbstractRegionAligner(AbstractRegionAligner aligner) {
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
        currentAdjacentMMCore = aligner.currentAdjacentMMCore;
        currentMMsCore = aligner.currentMMsCore;
        ruleset = RuleSet.getRuleSet();

    }

    public AbstractRegionAligner(String sRNA, String mRNA) {
        smallRNA = sRNA;
        messengerRNA = mRNA;
        ruleset = RuleSet.getRuleSet();
    }

    protected boolean hasReachedBaseCase() {

        if (currentMMs > ruleset.maxMM) {
            return true;
        }

        if (score > ruleset.maxScore) {
            //System.out.println("Score is above maxScore");
            return true;
        }

        if (currentGUWobbles > ruleset.getMaxGUWobbles()) {
            //System.out.println("Current GU_WOBBLES is above maxGUWobbles");
            return true;
        }

        if (currentMMsCore > ruleset.getMaxMMCoreRegion()) {
            return true;
        }

        if (currentAdjacentMMCore > ruleset.getMaxAdjacentMMCoreRegion()) {
            //System.out.println("Current AJ-MM is above maxAdjacentMM in CORE!");
            return true;
        }

        if (currentMMs > ruleset.getMaxMM()) {
            return true;
        }

        if (currentAdjacentMM > ruleset.getMaxAdjacentMM()) {
            //System.out.println("Current AJ-MM is above maxAdjacentMM in CORE!");
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
            if (ruleset.isGUCountAsMM()) {
                currentMMs++;

                if (inCoreRegion()) {
                    currentMMsCore++;
                }

                if (prevPosMM) {
                    currentAdjacentMM++;
                    if (inCoreRegion()) {
                        currentAdjacentMMCore++;
                    }
                }
                prevPosMM = true;
            } else {
                prevPosMM = false;
            }
            prevPosGU = true;

        } else if (thisPosMM) {
            currentMMs++;

            if (inCoreRegion()) {
                currentMMsCore++;
            }

            if (prevPosMM) {
                currentAdjacentMM++;
                if (inCoreRegion()) {
                    currentAdjacentMMCore++;
                }
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
            if (ruleset.isGUCountAsMM()) {
                thisPosMM = true;
            }

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {

                return ruleset.scoreGUWobble * ruleset.getCoreRegionMultiplier();
            }

            return ruleset.scoreGUWobble;
        }

        thisPosMM = true;
        thisPosGU = false;

        if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
            return ruleset.scoreMM * ruleset.coreRegionMultiplier;
        }

        return ruleset.scoreMM;
    }
}
