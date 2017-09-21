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
public class SeedRegionAligner extends RegionAligner {

    public SeedRegionAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
    }

    public SeedRegionAligner(SeedRegionAligner sra) {
        super(sra);
    }

    @Override
    protected boolean hasReachedBaseCase() {

        if (super.hasReachedBaseCase()) {
            return true;
        }

        if (MessengerRNA.seedRegionFound) {
            return true;
        }

        if (prevPosMM) {
            if (RuleSet.notAllowedMM(currentSRNAPosition)) {
                return true;
            }
        }

        if (prevPosGU && RuleSet.GUCountAsMM) {
            if (RuleSet.notAllowedMM(currentSRNAPosition)) {
                return true;
            }
        }

        if (currentMMs > RuleSet.MAX_MM_CORE_REGION) {
            return true;
        }

        if (currentAdjacentMM > RuleSet.getMAX_ADJACENT_MM_CORE_REGION()) {
            //System.out.println("Current AJ-MM is above MAX_ADJACENT_MM in CORE!");
            return true;
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
            if (currentMRNAPosition > 0) {
                currentMRNAPosition--;
            }
            currentGaps++;
            score += RuleSet.SCORE_GAP;
        } else {
            //update path positions
            if (currentSRNAPosition > 0) {
                currentSRNAPosition--;
            }
            currentGaps++;
            score += RuleSet.SCORE_GAP;
        }

        buildPath();

    }

    @Override
    void startPath() {
        currentSRNAPosition = smallRNA.length() - 1;
        currentMRNAPosition = messengerRNA.length() - 1;
        buildPath();

    }

    @Override
    void buildPath() {

        if (currentSRNAPosition < 1) {

            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
            score += pairScore;
            update();
            if (hasReachedBaseCase()) {
                return;
            }
            MessengerRNA.seedRegionFound = true;
            return;
        }

        while (currentSRNAPosition >= 0 && currentMRNAPosition >= 0) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                // System.out.println("Branched onto gapped sRNA");

                SeedRegionAligner gappedSRNA = new SeedRegionAligner(this);
                gappedSRNA.continuePath(true);
                // System.out.println("Branched onto gapped mRNA");
                SeedRegionAligner gappedMRNA = new SeedRegionAligner(this);
                gappedMRNA.continuePath(false);
                //System.out.println("BACK ON LINEAR");

            }

            score += pairScore;

            update();

            if (hasReachedBaseCase()) {
                //System.out.println(toString());
                return;
            }

            currentSRNAPosition--;
            currentMRNAPosition--;

        }

        MessengerRNA.seedRegionFound = true;

    }

    @Override
    boolean inCoreRegion() {
        return (currentSRNAPosition >= RuleSet.CORE_REGION_START - 1) && (currentSRNAPosition < RuleSet.CORE_REGION_END);

    }

    @Override
    protected void update() {
        super.update();

        if (thisPosMM) {

            if (RuleSet.isGUCountAsMM() && thisPosGU) {
                if (RuleSet.allowedMM(currentSRNAPosition)) {
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

            if (RuleSet.allowedMM(currentSRNAPosition)) {
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
