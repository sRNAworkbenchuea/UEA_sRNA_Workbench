/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

/**
 *
 * @author rew13hpu
 */
public class RegionThreeAligner extends AbstractRegionAligner {

    public RegionThreeAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
    }

    public RegionThreeAligner(RegionThreeAligner tra) {
        super(tra);
    }

    @Override
    protected boolean hasReachedBaseCase() {

        if (super.hasReachedBaseCase()) {
            return true;
        }

        if (MessengerRNA.tailRegionFound) {
            return true;
        }

        if (prevPosMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition + 12)) {
                return true;
            }
        }

        if (prevPosGU && ruleset.GUCountAsMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition + 12)) {
                return true;
            }
        }

        return false;
    }

    void continuePath(boolean gap_In_SRNA) {

        if (ruleset.isGapCountAsMM()) {
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
            score += ruleset.scoreGap;
        } else {
            //update path positions
            if (currentSRNAPosition < smallRNA.length() - 1) {
                currentSRNAPosition++;
            }
            currentGaps++;
            score += ruleset.scoreGap;
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
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                if (!(currentSRNAPosition == 4 && !ruleset.allowGap10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    RegionThreeAligner gappedSRNA = new RegionThreeAligner(this);
                    gappedSRNA.continuePath(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    RegionThreeAligner gappedMRNA = new RegionThreeAligner(this);
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

        return (currentSRNAPosition + 12 >= ruleset.coreRegionStart - 1) && (currentSRNAPosition + 12 < ruleset.coreRegionEnd);

    }

    @Override
    protected void update() {
        super.update();

        if (thisPosMM) {

            if (ruleset.isGUCountAsMM() && thisPosGU) {
                if (ruleset.allowedMM(currentSRNAPosition + 12)) {
                    prevPosMM = false;
                    prevPosGU = false;
                    if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                        score -= ruleset.scoreGUWobble * ruleset.coreRegionMultiplier;
                    } else {
                        score -= ruleset.scoreGUWobble;
                    }

                    return;
                }
            }

            if (ruleset.allowedMM(currentSRNAPosition + 12)) {
                prevPosMM = false;
                prevPosGU = false;
                if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                    score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
                } else {
                    score -= ruleset.scoreMM;
                }
            }
        }

    }

}
