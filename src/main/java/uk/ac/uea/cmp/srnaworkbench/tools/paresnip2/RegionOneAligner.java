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
public class RegionOneAligner extends AbstractRegionAligner {

    public RegionOneAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
    }

    public RegionOneAligner(RegionOneAligner sra) {
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
            if (ruleset.notAllowedMM(currentSRNAPosition)) {
                return true;
            }
        }

        if (prevPosGU && ruleset.GUCountAsMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition)) {
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
            if (currentMRNAPosition > 0) {
                currentMRNAPosition--;
            }
            currentGaps++;
            score += ruleset.scoreGap;
        } else {
            //update path positions
            if (currentSRNAPosition > 0) {
                currentSRNAPosition--;
            }
            currentGaps++;
            score += ruleset.scoreGap;
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
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                // System.out.println("Branched onto gapped sRNA");

                RegionOneAligner gappedSRNA = new RegionOneAligner(this);
                gappedSRNA.continuePath(true);
                // System.out.println("Branched onto gapped mRNA");
                RegionOneAligner gappedMRNA = new RegionOneAligner(this);
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
        return (currentSRNAPosition >= ruleset.coreRegionStart - 1) && (currentSRNAPosition < ruleset.coreRegionEnd);

    }

    @Override
    protected void update() {
        super.update();

        if (thisPosMM) {

            if (ruleset.isGUCountAsMM() && thisPosGU) {
                if (ruleset.allowedMM(currentSRNAPosition)) {
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

            if (ruleset.allowedMM(currentSRNAPosition)) {
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
