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
public class RegionTwoAligner extends AbstractRegionAligner {

    boolean hasMMPos10;
    boolean hasMMPos11;
    boolean earlyAbandon;

    public RegionTwoAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
        earlyAbandon = false;
    }

    public RegionTwoAligner(RegionTwoAligner aligner) {
        super(aligner);
        hasMMPos10 = aligner.hasMMPos10;
        hasMMPos11 = aligner.hasMMPos11;
        earlyAbandon = aligner.earlyAbandon;
    }

    @Override
    protected boolean hasReachedBaseCase() {

        if (super.hasReachedBaseCase()) {
            return true;
        }

        if (MessengerRNA.middleRegionFound) {
            return true;
        }

        if (earlyAbandon) {
            return true;
        }

        if (prevPosMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition + 6)) {
                return true;
            }
        }

        if (prevPosGU && ruleset.GUCountAsMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition + 6)) {
                return true;
            }
        }


        if (hasMMPos10 && !ruleset.isAllowedMM10()) {
            // System.out.println("Has non-permitted MM at pos 10");
            return true;
        }
        if (hasMMPos11 && !ruleset.isAllowedMM11()) {
            // System.out.println("Has non-permitted MM at pos 10");
            return true;
        }

        return false;

    }

    void continuePathLeft(boolean gap_In_SRNA) {

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

        buildPathLeft();

    }

    void continuePathRight(boolean gap_In_SRNA) {

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

        buildPathRight();
    }

    @Override
    void startPath() {

        currentSRNAPosition = 3;
        currentMRNAPosition = 3;
        buildPath();
    }

    @Override
    void buildPath() {

        buildPathLeft();

    }

    void buildPathLeft() {

        if (currentSRNAPosition < 1) {

            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
            score += pairScore;
            update();
            if (hasReachedBaseCase()) {
                earlyAbandon = true;
                return;
            }

            return;
        }

        while (currentSRNAPosition >= 0 && currentMRNAPosition >= 0) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                // System.out.println("Branched onto gapped sRNA");

                if (!(currentSRNAPosition == 3 && !ruleset.allowGap10_11)) {

                    RegionTwoAligner gappedSRNA = new RegionTwoAligner(this);
                    gappedSRNA.continuePathLeft(true);
                    // System.out.println("Branched onto gapped mRNA");
                    RegionTwoAligner gappedMRNA = new RegionTwoAligner(this);
                    gappedMRNA.continuePathLeft(false);
                    //System.out.println("BACK ON LINEAR");

                }
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
        
        currentSRNAPosition = 4;
        currentMRNAPosition = 4;
        buildPathRight();
    }

    void buildPathRight() {

        if (hasReachedBaseCase()) {
            return;
        }

        if (currentSRNAPosition == smallRNA.length() - 1 || currentMRNAPosition == messengerRNA.length() - 1) {

            score += scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
            update();
            if (hasReachedBaseCase()) {
                return;
            }
            MessengerRNA.middleRegionFound = true;
            return;
        }

        while (currentSRNAPosition < smallRNA.length() && currentMRNAPosition < messengerRNA.length()) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                if (!(currentSRNAPosition == 4 && !ruleset.allowGap10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    RegionTwoAligner gappedSRNA = new RegionTwoAligner(this);
                    gappedSRNA.continuePathRight(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    RegionTwoAligner gappedMRNA = new RegionTwoAligner(this);
                    gappedMRNA.continuePathRight(false);
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

        MessengerRNA.middleRegionFound = true;

    }

    @Override
    boolean inCoreRegion() {
        return (currentSRNAPosition + 6 >= ruleset.coreRegionStart - 1) && (currentSRNAPosition + 6 < ruleset.coreRegionEnd);

    }

    @Override
    protected void update() {

        super.update();

        if (thisPosMM && !(currentSRNAPosition == 3 || currentSRNAPosition == 4)) {

            if (ruleset.isGUCountAsMM() && thisPosGU) {
                if (ruleset.allowedMM(currentSRNAPosition + 6)) {
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

            if (ruleset.allowedMM(currentSRNAPosition + 6)) {
                prevPosMM = false;
                prevPosGU = false;
                if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                    score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
                } else {
                    score -= ruleset.scoreMM;
                }

                return;
            }
        }

        if ((thisPosMM && !ruleset.GUCountAsMM) && currentSRNAPosition == 3) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreMM;
            }
            hasMMPos10 = true;
            score += ruleset.scoreMM10;
            return;

        }

        if ((thisPosMM && !ruleset.GUCountAsMM) && currentSRNAPosition == 4) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreMM;
            }
            hasMMPos11 = true;
            score += ruleset.scoreMM11;
            return;

        }

        if (thisPosMM && currentSRNAPosition == 3) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreMM;
            }
            hasMMPos10 = true;
            score += ruleset.scoreMM10;
            return;

        }

        if (thisPosMM && currentSRNAPosition == 4) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreMM;
            }
            hasMMPos11 = true;
            score += ruleset.scoreMM11;
            return;

        }

        if (thisPosGU && currentSRNAPosition == 3) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreGUWobble * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreGUWobble;
            }

            hasMMPos10 = true;
            score += ruleset.scoreMM11;
            return;

        }

        if (thisPosGU && currentSRNAPosition == 4) {

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                score -= ruleset.scoreGUWobble * ruleset.coreRegionMultiplier;
            } else {
                score -= ruleset.scoreGUWobble;
            }
            hasMMPos11 = true;
            score += ruleset.scoreMM11;
        }

    }

}
