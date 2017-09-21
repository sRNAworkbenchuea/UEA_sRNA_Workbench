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
public class TailRegionAligner extends RegionAligner {

    public TailRegionAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
    }

    public TailRegionAligner(TailRegionAligner tra) {
        super(tra);
    }

    @Override
    protected boolean hasReachedBaseCase() {

        
        if(super.hasReachedBaseCase())
            return true;
        
        
        
        if (MessengerRNA.tailRegionFound) {
            return true;
        }

      

        if (prevPosMM) {
            if (RuleSet.notAllowedMM(currentSRNAPosition + 12)) {
                return true;
            }
        }

        if (prevPosGU && RuleSet.GUCountAsMM) {
            if (RuleSet.notAllowedMM(currentSRNAPosition + 12)) {
                return true;
            }
        }

        return false;
    }

    void continuePath(boolean gap_In_SRNA) {

        if (RuleSet.isGapCountAsMM()) {
            currentMMs++;

            if (prevPosMM) {
                currentAdjacentMM++;
            }
            prevPosMM = true;
            prevPosGU = false;
        } else {
            prevPosGU = false;
            prevPosMM = false;
        }

        if (gap_In_SRNA) {
            //update path positions
            if (currentMRNAPosition < messengerRNA.length() - 1) {
                currentMRNAPosition++;
            }
            currentGaps++;
            score += RuleSet.SCORE_GAP;
        } else {
            //update path positions
            if (currentSRNAPosition < smallRNA.length() - 1) {
                currentSRNAPosition++;
            }
            currentGaps++;
            score += RuleSet.SCORE_GAP;
        }

        buildPath();

    }

    @Override
    void startPath() {

        currentSRNAPosition = 0;
        currentMRNAPosition = 0;
        buildPath();

    }

    @Override
    void buildPath() {

        if (hasReachedBaseCase()) {
            return;
        }

        if (currentSRNAPosition == smallRNA.length() - 1 || currentMRNAPosition == messengerRNA.length() - 1) {

            score += scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
            update();
            if (hasReachedBaseCase()) {
                return;
            }
            MessengerRNA.tailRegionFound = true;
            return;
        }

        while (currentSRNAPosition < smallRNA.length() && currentMRNAPosition < messengerRNA.length()) {
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                if (!(currentSRNAPosition == 4 && !RuleSet.ALLOW_GAP_10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    TailRegionAligner gappedSRNA = new TailRegionAligner(this);
                    gappedSRNA.continuePath(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    TailRegionAligner gappedMRNA = new TailRegionAligner(this);
                    gappedMRNA.continuePath(false);
                    //   System.out.println("BACK ON LINEAR");
                }

            }

            score += pairScore;

            update();

            if (hasReachedBaseCase()) {
                //System.out.println(toString());
                return;
            }

            currentSRNAPosition++;
            currentMRNAPosition++;
        }

        MessengerRNA.tailRegionFound = true;

    }

    @Override
    boolean inCoreRegion() {

        return (currentSRNAPosition + 12 >= RuleSet.CORE_REGION_START - 1) && (currentSRNAPosition + 12 < RuleSet.CORE_REGION_END);

    }

    @Override
    protected void update() {
        super.update();

        if (thisPosMM) {

            if (RuleSet.isGUCountAsMM() && thisPosGU) {
                if (RuleSet.allowedMM(currentSRNAPosition + 12)) {
                    prevPosMM = false;
                    prevPosGU = false;
                    if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                        score -= RuleSet.SCORE_GU_WOBBLE * RuleSet.CORE_REGION_MULTIPLIER;
                    } else {
                        score -= RuleSet.SCORE_GU_WOBBLE;
                    }

                    return;
                }
            }

            if (RuleSet.allowedMM(currentSRNAPosition + 12)) {
                prevPosMM = false;
                prevPosGU = false;
                if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                    score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
                } else {
                    score -= RuleSet.SCORE_MM;
                }
            }
        }

    }

}
