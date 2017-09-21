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
public class MiddleRegionAligner extends RegionAligner {

    boolean hasMMPos10;
    boolean hasMMPos11;
    boolean earlyAbandon;

    public MiddleRegionAligner(String sRNA, String mRNA) {
        super(sRNA, mRNA);
        earlyAbandon = false;
    }

    public MiddleRegionAligner(MiddleRegionAligner aligner) {
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
            if (RuleSet.notAllowedMM(currentSRNAPosition + 6)) {
                return true;
            }
        }

        double totalNum = (double) currentMMs + ((double) currentGUWobbles * RuleSet.GU_MM_SCORE);

        if (RuleSet.GapCountAsMM) {
            totalNum += (double) currentGaps;
        }

        if (totalNum > RuleSet.MAX_TOTAL_MM) {
            return true;
        }

        if (prevPosGU && RuleSet.GUCountAsMM) {
            if (RuleSet.notAllowedMM(currentSRNAPosition + 6)) {
                return true;
            }
        }

        if (currentMMs > RuleSet.MAX_MM_CORE_REGION) {
            return true;
        }

        if (hasMMPos10 && !RuleSet.isALLOW_MM_10()) {
            // System.out.println("Has non-permitted MM at pos 10");
            return true;
        }
        if (hasMMPos11 && !RuleSet.isALLOW_MM_11()) {
            // System.out.println("Has non-permitted MM at pos 10");
            return true;
        }

        return false;

    }

    void continuePathLeft(boolean gap_In_SRNA) {

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

        buildPathLeft();

    }

    void continuePathRight(boolean gap_In_SRNA) {

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
        currentSRNAPosition = 4;
        currentMRNAPosition = 4;
        buildPathRight();

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
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                // System.out.println("Branched onto gapped sRNA");

                if (!(currentSRNAPosition == 3 && !RuleSet.ALLOW_GAP_10_11)) {

                    MiddleRegionAligner gappedSRNA = new MiddleRegionAligner(this);
                    gappedSRNA.continuePathLeft(true);
                    // System.out.println("Branched onto gapped mRNA");
                    MiddleRegionAligner gappedMRNA = new MiddleRegionAligner(this);
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
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                if (!(currentSRNAPosition == 4 && !RuleSet.ALLOW_GAP_10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    MiddleRegionAligner gappedSRNA = new MiddleRegionAligner(this);
                    gappedSRNA.continuePathRight(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    MiddleRegionAligner gappedMRNA = new MiddleRegionAligner(this);
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
        return (currentSRNAPosition + 6 >= RuleSet.CORE_REGION_START - 1) && (currentSRNAPosition + 6 < RuleSet.CORE_REGION_END);

    }

    @Override
    protected void update() {

        super.update();

        if (thisPosMM && !(currentSRNAPosition == 3 || currentSRNAPosition == 4)) {

            if (RuleSet.isGUCountAsMM() && thisPosGU) {
                if (RuleSet.allowedMM(currentSRNAPosition + 6)) {
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

            if (RuleSet.allowedMM(currentSRNAPosition + 6)) {
                prevPosMM = false;
                prevPosGU = false;
                if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                    score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
                } else {
                    score -= RuleSet.SCORE_MM;
                }

                return;
            }
        }

        if ((thisPosMM && !RuleSet.GUCountAsMM) && currentSRNAPosition == 3) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_MM;
            }
            hasMMPos10 = true;
            score += RuleSet.SCORE_MM_10;
            return;

        }

        if ((thisPosMM && !RuleSet.GUCountAsMM) && currentSRNAPosition == 4) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_MM;
            }
            hasMMPos11 = true;
            score += RuleSet.SCORE_MM_11;
            return;

        }

        if (thisPosMM && currentSRNAPosition == 3) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_MM;
            }
            hasMMPos10 = true;
            score += RuleSet.SCORE_MM_10;
            return;

        }

        if (thisPosMM && currentSRNAPosition == 4) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_MM;
            }
            hasMMPos11 = true;
            score += RuleSet.SCORE_MM_11;
            return;

        }

        if (thisPosGU && currentSRNAPosition == 3) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_GU_WOBBLE * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_GU_WOBBLE;
            }

            hasMMPos10 = true;
            score += RuleSet.SCORE_MM_11;
            return;

        }

        if (thisPosGU && currentSRNAPosition == 4) {

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                score -= RuleSet.SCORE_GU_WOBBLE * RuleSet.CORE_REGION_MULTIPLIER;
            } else {
                score -= RuleSet.SCORE_GU_WOBBLE;
            }
            hasMMPos11 = true;
            score += RuleSet.SCORE_MM_11;
        }

    }

}
