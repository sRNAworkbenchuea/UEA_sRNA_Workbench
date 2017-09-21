/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

/**
 *
 * @author Josh
 */
public class AlignmentPath implements Comparable<AlignmentPath> {

    double score;
    int currentAdjacentMM;
    int currentGaps;
    int currentSRNAPosition;
    int currentMRNAPosition;
    int currentGUWobbles;
    int currentMMs;
    int currentMMsCoreRegion;
    String smallRNA;
    String messengerRNA;
    boolean gap_In_SRNA;
    boolean prevPosMM;
    boolean prevPosGU;
    boolean thisPosMM;
    boolean thisPosGU;
    AlignmentController controller;
    boolean hasMMPos10;
    boolean hasMMPos11;
    StringBuilder sRNA_Characters;
    StringBuilder mRNA_Characters;
    StringBuilder alignmentSymbols;
    int cleavagePos;
    boolean earlyAbandon;

    AlignmentPath(SmallRNA sRNA, MessengerRNA mRNA, AlignmentController controller) {

        sRNA_Characters = new StringBuilder();
        mRNA_Characters = new StringBuilder();
        alignmentSymbols = new StringBuilder();
        this.controller = controller;
        this.smallRNA = sRNA.getStringSeq();
        this.messengerRNA = mRNA.getStringSeq();
        cleavagePos = mRNA.getCleavagePosition();

    }

    public AlignmentPath(AlignmentPath aligner) {
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
        controller = aligner.controller;
        hasMMPos10 = aligner.hasMMPos10;
        hasMMPos11 = aligner.hasMMPos11;
        controller = aligner.controller;
        cleavagePos = aligner.cleavagePos;
        sRNA_Characters = new StringBuilder(aligner.sRNA_Characters);
        mRNA_Characters = new StringBuilder(aligner.mRNA_Characters);
        alignmentSymbols = new StringBuilder(aligner.alignmentSymbols);
        earlyAbandon = aligner.earlyAbandon;
    }

    boolean hasReachedBaseCase() {

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

        if (earlyAbandon) {
            return true;
        }

        double totalNum = (double) currentMMs + ((double) currentGUWobbles * RuleSet.GU_MM_SCORE);

        if (RuleSet.GapCountAsMM) {
            totalNum += (double) currentGaps;
        }
        if (totalNum > RuleSet.MAX_TOTAL_MM) {
            return true;
        }

        if (currentMMsCoreRegion > RuleSet.MAX_MM_CORE_REGION) {
            return true;
        }

        if (currentMMs > RuleSet.MAX_MM) {
            return true;
        }

        if (score > RuleSet.MAX_SCORE) {
            //System.out.println("Score is above MAX_SCORE");
            return true;
        }

        if (currentAdjacentMM > RuleSet.getMAX_ADJACENT_MM_CORE_REGION()) {
            //System.out.println("Current AJ-MM is above MAX_ADJACENT_MM in CORE!");
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

        alignmentSymbols.append(' ');

        if (RuleSet.isGapCountAsMM()) {
            currentMMs++;

            if (inCoreRegion()) {
                currentMMsCoreRegion++;
            }

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

        alignmentSymbols.append(' ');

        if (RuleSet.isGapCountAsMM()) {

            currentMMs++;

            if (inCoreRegion()) {
                currentMMsCoreRegion++;
            }

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

    void startPath() {

        currentSRNAPosition = 9;
        currentMRNAPosition = cleavagePos - 1;
        buildPath();
    }

    void buildPath() {

        buildPathLeft();
        currentSRNAPosition = 10;
        currentMRNAPosition = cleavagePos;
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
            finishPathLeft();
            return;
        }

        while (currentSRNAPosition >= 0 && currentMRNAPosition >= 0) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                // System.out.println("Branched onto gapped sRNA");

                if (!(currentSRNAPosition == 9 && !RuleSet.ALLOW_GAP_10_11)) {

                    AlignmentPath gappedSRNA = new AlignmentPath(this);
                    gappedSRNA.addPair('-', messengerRNA.charAt(currentMRNAPosition));
                    gappedSRNA.continuePathLeft(true);
                    // System.out.println("Branched onto gapped mRNA");
                    AlignmentPath gappedMRNA = new AlignmentPath(this);
                    gappedMRNA.addPair(smallRNA.charAt(currentSRNAPosition), '-');
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

        finishPathLeft();

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

            if (!isBetterThanCurrentBest()) {
                return;
            }

            finishPathRight();

            return;
        }

        while (currentSRNAPosition < smallRNA.length() && currentMRNAPosition < messengerRNA.length()) {
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                if (!(currentSRNAPosition == 10 && !RuleSet.ALLOW_GAP_10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    AlignmentPath gappedSRNA = new AlignmentPath(this);
                    gappedSRNA.addPair('-', messengerRNA.charAt(currentMRNAPosition));
                    gappedSRNA.continuePathRight(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    AlignmentPath gappedMRNA = new AlignmentPath(this);
                    gappedMRNA.addPair(smallRNA.charAt(currentSRNAPosition), '-');
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

        if (!isBetterThanCurrentBest()) {
            return;
        }

        finishPathRight();

    }

    boolean inCoreRegion() {
        return (currentSRNAPosition >= RuleSet.CORE_REGION_START - 1) && (currentSRNAPosition < RuleSet.CORE_REGION_END);

    }

    protected void update() {

        //DO UPDATES HERE e.g. num MM
        addPair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
        //System.out.println(currentSRNAPosition + " " + smallRNA.charAt(currentSRNAPosition) + ":" + messengerRNA.charAt(currentMRNAPosition) + " " + currentMRNAPosition);

        if (!thisPosGU && !thisPosMM) {
            alignmentSymbols.append('|');
            prevPosMM = false;
            prevPosGU = false;
            return;
        }

        //Not sure if it should be like this?
        if (thisPosMM) {

            if (RuleSet.isGUCountAsMM() && thisPosGU) {
                if (RuleSet.allowedMM(currentSRNAPosition)) {
                    prevPosMM = false;
                    prevPosGU = false;
                    alignmentSymbols.append('o');
                    if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
                        score -= RuleSet.SCORE_GU_WOBBLE * RuleSet.CORE_REGION_MULTIPLIER;
                    } else {
                        score -= RuleSet.SCORE_GU_WOBBLE;
                    }

                    return;
                }
            }

            if (RuleSet.allowedMM(currentSRNAPosition)) {
                alignmentSymbols.append(' ');
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

        if (thisPosGU) {
            alignmentSymbols.append('o');
            currentGUWobbles++;
            prevPosGU = true;
            if (RuleSet.isGUCountAsMM()) {
                currentMMs++;
                if (prevPosMM) {
                    currentAdjacentMM++;
                    if (inCoreRegion()) {
                        currentMMsCoreRegion++;
                    }
                }
                prevPosMM = true;
            } else {
                prevPosMM = false;
            }
            prevPosGU = true;

        } else if (thisPosMM) {
            alignmentSymbols.append(' ');
            currentMMs++;
            if (inCoreRegion()) {
                currentMMsCoreRegion++;
            }
            if (prevPosMM) {
                currentAdjacentMM++;
            }
            prevPosMM = true;
            prevPosGU = false;
        }

    }

    protected double scorePair(char charA, char charB) {

        //ADD POSITION 10/11 SHIT HERE!!!
        if (charA == 'A' && charB == 'T' || charA == 'T' && charB == 'A') {
            thisPosGU = false;
            thisPosMM = false;
            return 0;
        } else if (charA == 'C' && charB == 'G' || charA == 'G' && charB == 'C') {
            thisPosGU = false;
            thisPosMM = false;
            return 0;
        } else if (charA == 'G' && charB == 'T' || charA == 'T' && charB == 'G') {
            //IS A GU WOBBLE CONSIDERED A MISMATCH??
            thisPosGU = true;
            if (RuleSet.isGUCountAsMM()) {
                thisPosMM = true;
            }

            if (currentSRNAPosition == 9) {
                hasMMPos10 = true;
                return RuleSet.SCORE_MM_10;
            }

            if (currentSRNAPosition == 10) {
                hasMMPos11 = true;
                return RuleSet.SCORE_MM_11;
            }

            if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {

                return RuleSet.SCORE_GU_WOBBLE * RuleSet.getCORE_REGION_MULTIPLIER();
            }

            return RuleSet.SCORE_GU_WOBBLE;
        }

        thisPosMM = true;
        thisPosGU = false;

        if (currentSRNAPosition == 9) {
            hasMMPos10 = true;
            return RuleSet.SCORE_MM_10;
        }

        if (currentSRNAPosition == 10) {
            hasMMPos11 = true;
            return RuleSet.SCORE_MM_11;
        }

        if (RuleSet.getCORE_REGION_MULTIPLIER() > 0 && inCoreRegion()) {
            return RuleSet.SCORE_MM * RuleSet.CORE_REGION_MULTIPLIER;
        }

        return RuleSet.SCORE_MM;
    }

    public void addPair(char sRNA, char mRNA) {
        sRNA_Characters.append(sRNA);
        mRNA_Characters.append(mRNA);
    }

    @Override
    public int compareTo(AlignmentPath o) {

        int gapCompare = Integer.compare(currentGaps, o.currentGaps);

        if (gapCompare != 0) {
            return gapCompare;
        }

        int scoreCompare = Double.compare(score, o.score);

        if (scoreCompare != 0) {
            return scoreCompare;
        }

        int scoreMMCore = Integer.compare(currentMMsCoreRegion, o.currentMMsCoreRegion);

        if (scoreMMCore != 0) {
            return scoreMMCore;
        }

        int scoreMM = Integer.compare(currentMMs, o.currentMMs);

        if (scoreMM != 0) {
            return scoreMM;
        } else {
            return Integer.compare(currentGUWobbles, o.currentGUWobbles);
        }
    }

    private boolean isBetterThanCurrentBest() {

        if (controller.currentBest == null) {
            return true;
        }

        return this.compareTo(controller.currentBest) < 0;

    }

    private void finishPathRight() {
        while (currentSRNAPosition < smallRNA.length()) {
            sRNA_Characters.append(smallRNA.charAt(currentSRNAPosition));
            currentSRNAPosition++;
        }

        while (currentMRNAPosition < messengerRNA.length()) {
            mRNA_Characters.append(messengerRNA.charAt(currentMRNAPosition));
            currentMRNAPosition++;
        }

        sRNA_Characters.append(' ');
        sRNA_Characters.append('\'');
        sRNA_Characters.append('3');

        mRNA_Characters.append(' ');
        mRNA_Characters.append('\'');
        mRNA_Characters.append('5');
        controller.currentBest = this;
    }

    private void finishPathLeft() {
        while (currentMRNAPosition >= 0) {
            mRNA_Characters.append(messengerRNA.charAt(currentMRNAPosition));
            sRNA_Characters.append(" ");
            alignmentSymbols.append(" ");
            currentMRNAPosition--;
        }

        mRNA_Characters.append(' ');
        alignmentSymbols.append(' ');
        alignmentSymbols.append(' ');
        alignmentSymbols.append(' ');
        sRNA_Characters.append('5');
        sRNA_Characters.append('\'');
        sRNA_Characters.append(' ');
        mRNA_Characters.append('3');
        mRNA_Characters.append('\'');

        sRNA_Characters = sRNA_Characters.reverse();
        mRNA_Characters = mRNA_Characters.reverse();
        alignmentSymbols = alignmentSymbols.reverse();
    }

    public void print() {
        System.out.println(sRNA_Characters.toString());
        System.out.println(alignmentSymbols.toString());
        System.out.println(mRNA_Characters.toString());
        System.out.println("Score: " + score);
        System.out.println("Category: " + controller.messengerRNA.category);
        System.out.println("Gene name: " + controller.messengerRNA.geneName);
        System.out.println("Abundance: " + controller.messengerRNA.getAbundance());
        System.out.println("Transcript position: " + controller.messengerRNA.transcriptPosition);
        System.out.println("-----------------");
    }
}
