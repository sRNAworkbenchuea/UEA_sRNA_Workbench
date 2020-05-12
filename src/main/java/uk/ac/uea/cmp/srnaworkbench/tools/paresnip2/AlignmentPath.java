/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;

/**
 *
 * @author Josh
 */
public class AlignmentPath implements Comparable<AlignmentPath> {

    double score;
    double freeEnergy;
    double freeEnergyRatio;
    int currentAdjacentMM;
    int currentAdjacentMMCoreRegion;
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
    boolean invalidGap;
    AlignmentController controller;
    boolean hasMMPos10;
    boolean hasMMPos11;
    boolean hasGUPos10;
    boolean hasGUPos11;
    StringBuilder sRNA_Characters;
    StringBuilder mRNA_Characters;
    StringBuilder alignmentSymbols;
    int cleavagePos;
    double pVal;
    RuleSet ruleset;
    //boolean earlyAbandon;

    public double getFreeEnergy() {
        return freeEnergy;
    }

    public void setFreeEnergy(double freeEnergy) {
        this.freeEnergy = freeEnergy;
    }

    public double getFreeEnergyRatio() {
        return freeEnergyRatio;
    }

    public void setFreeEnergyRatio(double freeEnergyRatio) {
        this.freeEnergyRatio = freeEnergyRatio;
    }

    public double getpVal() {
        return pVal;
    }

    public void setpVal(double pVal) {
        this.pVal = pVal;
    }

    AlignmentPath(String sRNA, String mRNA, AlignmentController controller, RuleSet rs) {

        sRNA_Characters = new StringBuilder();
        mRNA_Characters = new StringBuilder();
        alignmentSymbols = new StringBuilder();
        this.controller = controller;
        this.smallRNA = sRNA;
        this.messengerRNA = mRNA;
        cleavagePos = 16;
        ruleset = rs;

    }

    AlignmentPath(SmallRNA sRNA, MessengerRNA mRNA, AlignmentController controller, RuleSet rs) {

        sRNA_Characters = new StringBuilder();
        mRNA_Characters = new StringBuilder();
        alignmentSymbols = new StringBuilder();
        this.controller = controller;
        this.smallRNA = sRNA.getStringSeq();
        this.messengerRNA = mRNA.getStringSeq();
        cleavagePos = mRNA.getCleavagePosition();
        ruleset = rs;

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
        hasGUPos10 = aligner.hasGUPos10;
        hasGUPos11 = aligner.hasGUPos11;
        controller = aligner.controller;
        cleavagePos = aligner.cleavagePos;
        sRNA_Characters = new StringBuilder(aligner.sRNA_Characters);
        mRNA_Characters = new StringBuilder(aligner.mRNA_Characters);
        alignmentSymbols = new StringBuilder(aligner.alignmentSymbols);
        ruleset = aligner.ruleset;
        invalidGap = aligner.invalidGap;
        currentAdjacentMMCoreRegion = aligner.currentAdjacentMMCoreRegion;
        //earlyAbandon = aligner.earlyAbandon;
    }

    boolean hasReachedBaseCase() {

        if (prevPosMM) {
            if (currentSRNAPosition >= 0 && currentSRNAPosition < smallRNA.length()) {
                if (ruleset.notAllowedMM(currentSRNAPosition)) {
                    return true;
                }
            }
        }

        if (prevPosGU && ruleset.isGUCountAsMM()) {
            if (currentSRNAPosition >= 0 && currentSRNAPosition < smallRNA.length()) {
                if (ruleset.notAllowedMM(currentSRNAPosition)) {
                    return true;
                }
            }
        }

        if (invalidGap) {
            return true;
        }

        if (currentMMsCoreRegion > ruleset.maxMMCoreRegion) {
            return true;
        }

        if (currentMMs > ruleset.maxMM) {
            return true;
        }

        if (score > ruleset.maxScore) {
            //System.out.println("Score is above maxScore");
            return true;
        }

        if (currentAdjacentMMCoreRegion > ruleset.getMaxAdjacentMMCoreRegion()) {
            //System.out.println("Current AJ-MM is above maxAdjacentMM in CORE!");
            return true;
        }

        if (currentAdjacentMM > ruleset.getMaxAdjacentMM()) {
            //System.out.println("Current AJ-MM is above maxAdjacentMM");
            return true;
        }

        if (currentGUWobbles > ruleset.getMaxGUWobbles()) {
            //System.out.println("Current GU_WOBBLES is above maxGUWobbles");
            return true;
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

        alignmentSymbols.append(' ');

        if (ruleset.GapCountAsMM) {
            if (ruleset.notAllowedMM(currentSRNAPosition)) {
                invalidGap = true;
            }
        }

        if (ruleset.isGapCountAsMM()) {
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
        } else {
            //update path positions
            if (currentSRNAPosition > 0) {
                currentSRNAPosition--;
            }
            currentGaps++;
        }

        if (inCoreRegion()) {
            score += (ruleset.scoreGap * ruleset.getCoreRegionMultiplier());
        } else {
            score += ruleset.scoreGap;
        }

        buildPathLeft();

    }

    void continuePathRight(boolean gap_In_SRNA) {

        alignmentSymbols.append(' ');

        if (ruleset.isGapCountAsMM()) {

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

        } else {
            //update path positions
            if (currentSRNAPosition < smallRNA.length() - 1) {
                currentSRNAPosition++;
            }
            currentGaps++;

        }

        if (inCoreRegion()) {
            score += (ruleset.scoreGap * ruleset.getCoreRegionMultiplier());
        } else {
            score += ruleset.scoreGap;
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

    }

    void buildPathLeft() {

        if (currentSRNAPosition < 1) {

            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));
            score += pairScore;
            update();
            if (hasReachedBaseCase()) {
                //earlyAbandon = true;
                return;
            }
            //Potential fix for addition nt at 5' end of sRNA with gap
            currentSRNAPosition--;
            currentMRNAPosition--;
            finishPathLeft();
            return;
        }

        while (currentSRNAPosition >= 0 && currentMRNAPosition >= 0) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                // System.out.println("Branched onto gapped sRNA");

                if (!(currentSRNAPosition == 9 && !ruleset.allowGap10_11)) {

                    AlignmentPath gappedSRNA = new AlignmentPath(this);
                    gappedSRNA.addPair('-', messengerRNA.charAt(currentMRNAPosition));
                    gappedSRNA.continuePathLeft(true);
                    // System.out.println("Branched onto gapped mRNA");
                    if (currentSRNAPosition != 0) {
                        AlignmentPath gappedMRNA = new AlignmentPath(this);
                        gappedMRNA.addPair(smallRNA.charAt(currentSRNAPosition), '-');
                        gappedMRNA.continuePathLeft(false);
                    }
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

//            int compare = isBetterThanCurrentBest();
//            if (compare < 0) {
//                return;
//            }
            //finishPathRight(compare);
            currentSRNAPosition++;
            currentMRNAPosition++;
            finishPathRight();

            return;
        }

        while (currentSRNAPosition < smallRNA.length() && currentMRNAPosition < messengerRNA.length()) {
            double pairScore = scorePair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < ruleset.maxGaps) {
                if (!(currentSRNAPosition == 10 && !ruleset.allowGap10_11)) {
                    //    System.out.println("Branched onto gapped sRNA");
                    AlignmentPath gappedSRNA = new AlignmentPath(this);
                    gappedSRNA.addPair('-', messengerRNA.charAt(currentMRNAPosition));
                    gappedSRNA.continuePathRight(true);
                    //   System.out.println("Branched onto gapped mRNA");
                    if (currentSRNAPosition != smallRNA.length() - 1) {
                        AlignmentPath gappedMRNA = new AlignmentPath(this);
                        gappedMRNA.addPair(smallRNA.charAt(currentSRNAPosition), '-');
                        gappedMRNA.continuePathRight(false);
                    }
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

        //int compare = isBetterThanCurrentBest();
//            if (compare < 0) {
//                return;
//            }
        //finishPathRight(compare);
        finishPathRight();

    }

    boolean inCoreRegion() {
        return (currentSRNAPosition >= ruleset.coreRegionStart - 1) && (currentSRNAPosition < ruleset.coreRegionEnd);

    }

    protected void update() {
        addPair(smallRNA.charAt(currentSRNAPosition), messengerRNA.charAt(currentMRNAPosition));

        if (!thisPosGU && !thisPosMM) {
            alignmentSymbols.append('|');
            prevPosMM = false;
            prevPosGU = false;
            return;
        }

        if (thisPosMM) {
            alignmentSymbols.append(' ');
        } else {
            alignmentSymbols.append('o');
        }

        if (ruleset.allowedMM(currentSRNAPosition)) {
            if (thisPosMM) {

                prevPosMM = false;
                prevPosGU = false;
                if (!thisPosGU) {
                    if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                        score -= ruleset.scoreMM * ruleset.coreRegionMultiplier;
                    } else {
                        score -= ruleset.scoreMM;
                    }
                } else if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                    score -= ruleset.scoreGUWobble * ruleset.coreRegionMultiplier;
                } else {
                    score -= ruleset.scoreGUWobble;
                }

            } else {
                prevPosMM = false;
                prevPosGU = false;

                if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                    score -= ruleset.scoreGUWobble * ruleset.coreRegionMultiplier;
                } else {
                    score -= ruleset.scoreGUWobble;
                }

            }

            return;
        }

        if (thisPosMM) {
            currentMMs++;
            if (inCoreRegion()) {
                currentMMsCoreRegion++;
            }
            if (prevPosMM) {
                currentAdjacentMM++;
                if (inCoreRegion()) {
                    currentAdjacentMMCoreRegion++;
                }
            }
            prevPosMM = true;
            prevPosGU = false;
        } else {
            currentGUWobbles++;
            if (ruleset.isGUCountAsMM()) {
                currentMMs++;
                if (prevPosMM) {
                    currentAdjacentMM++;
                    if (inCoreRegion()) {
                        currentMMsCoreRegion++;
                        currentAdjacentMMCoreRegion++;
                    }
                }
                prevPosMM = true;
            } else {
                prevPosMM = false;
            }
            prevPosGU = true;
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
            if (ruleset.isGUCountAsMM()) {
                thisPosMM = true;
            } else {
                thisPosMM = false;
            }

            if (currentSRNAPosition == 9) {
                if (!ruleset.isGUCountAsMM()) {
                    hasGUPos10 = true;
                    if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                        return ruleset.scoreGUWobble * ruleset.getCoreRegionMultiplier();
                    } else {
                        return ruleset.scoreGUWobble;
                    }
                }
                hasMMPos10 = true;
                return ruleset.scoreMM10;
            }

            if (currentSRNAPosition == 10) {
                if (!ruleset.isGUCountAsMM()) {
                    hasGUPos11 = true;
                    if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                        return ruleset.scoreGUWobble * ruleset.getCoreRegionMultiplier();
                    } else {
                        return ruleset.scoreGUWobble;
                    }
                }
                hasMMPos11 = true;
                return ruleset.scoreMM11;
            }

            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {

                return ruleset.scoreGUWobble * ruleset.getCoreRegionMultiplier();
            }

            return ruleset.scoreGUWobble;
        }

        thisPosMM = true;
        thisPosGU = false;

        if (currentSRNAPosition == 9) {
            hasMMPos10 = true;
            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                return ruleset.scoreMM10 * ruleset.coreRegionMultiplier;
            } else {
                return ruleset.scoreMM10;
            }
        }

        if (currentSRNAPosition == 10) {
            hasMMPos11 = true;
            if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
                return ruleset.scoreMM11 * ruleset.coreRegionMultiplier;
            } else {
                return ruleset.scoreMM11;
            }
        }

        if (ruleset.getCoreRegionMultiplier() > 0 && inCoreRegion()) {
            return ruleset.scoreMM * ruleset.coreRegionMultiplier;
        }

        return ruleset.scoreMM;
    }

    public void addPair(char sRNA, char mRNA) {
        sRNA_Characters.append(sRNA);
        mRNA_Characters.append(mRNA);
    }

    @Override
    public int compareTo(AlignmentPath o) {

        if (this.currentGaps == o.currentGaps) {
            return 0;
        }

        return Double.compare(score, o.score);

    }

    private int isBetterThanCurrentBest() {

        if (controller.currentBest.isEmpty()) {
            return 1;
        }

        return this.compareTo(controller.currentBest.get(0));

    }

    private void finishPathRight() {
//        while (currentSRNAPosition < smallRNA.length()) {
//            sRNA_Characters.append(smallRNA.charAt(currentSRNAPosition));
//            alignmentSymbols.append(" ");
//            currentSRNAPosition++;
//        }

        while (currentMRNAPosition < messengerRNA.length()) {
            mRNA_Characters.append(messengerRNA.charAt(currentMRNAPosition));
            sRNA_Characters.append(" ");
            alignmentSymbols.append(" ");
            currentMRNAPosition++;
        }

        sRNA_Characters.append(' ');
        sRNA_Characters.append('\'');
        sRNA_Characters.append('3');

        mRNA_Characters.append(' ');
        mRNA_Characters.append('\'');
        mRNA_Characters.append('5');

//        if (compare == 0) {
//            controller.currentBest.add(this);
//        } else {
//            controller.currentBest.clear();
//            controller.currentBest.add(this);
//        }
        controller.currentBest.add(this);
    }

    private void finishPathLeft() {
        while (currentMRNAPosition >= 0) {
            mRNA_Characters.append(messengerRNA.charAt(currentMRNAPosition));
            sRNA_Characters.append(" ");
            alignmentSymbols.append(" ");
            currentMRNAPosition--;
        }

        mRNA_Characters.append(" ");
        alignmentSymbols.append(" ");
        alignmentSymbols.append(" ");
        alignmentSymbols.append(" ");
        sRNA_Characters.append(" ");
        sRNA_Characters.append("\'");
        sRNA_Characters.append("5");
        mRNA_Characters.append("\'");
        mRNA_Characters.append("3");

        sRNA_Characters = sRNA_Characters.reverse();
        mRNA_Characters = mRNA_Characters.reverse();
        alignmentSymbols = alignmentSymbols.reverse();

        currentSRNAPosition = 10;
        currentMRNAPosition = cleavagePos;
        buildPathRight();
    }

    public void print() {
        System.out.println(sRNA_Characters.toString());
        System.out.println(alignmentSymbols.toString());
        System.out.println(mRNA_Characters.toString());
        System.out.println("Score: " + score);
        System.out.println("P-value: " + pVal);
        System.out.println("Category: " + controller.messengerRNA.category);
        System.out.println("Gene name: " + controller.messengerRNA.geneName);
        System.out.println("Small RNA name: " + controller.smallRNA.getComment());
        System.out.println("Abundance: " + controller.messengerRNA.getAbundance());
        System.out.println("Transcript position: " + controller.messengerRNA.transcriptPosition);
        System.out.println("-----------------");
    }

    public double getScore() {
        return score;
    }

    public String toCSVString() {
        StringBuilder sb = new StringBuilder();

        SmallRNA s = controller.smallRNA;
        MessengerRNA m = controller.messengerRNA;

        sb.append("\"").append(s.getComment()).append("\"");
        sb.append(",");
        sb.append("\"").append(s.toString()).append("\"");
        sb.append(",");
        sb.append("\"").append(m.getComment().replace(",", ";")).append("\"");
        sb.append(",");
        sb.append(m.getCategory());
        sb.append(",");
        sb.append(m.getTranscriptPosition());
        sb.append(",");
        sb.append(m.getAbundance());
        sb.append(",");
        sb.append(m.getWeightedAbundance());
        sb.append(",");
        sb.append("\"").append(sRNA_Characters.toString()).append(System.lineSeparator());
        sb.append(alignmentSymbols.toString()).append(System.lineSeparator());
        sb.append(mRNA_Characters.toString()).append("\"");
        sb.append(",");
        sb.append(score);
        sb.append(",");
        sb.append(freeEnergy);
        sb.append(",");
        sb.append(s.getPerfect_mfe());
        sb.append(",");
        sb.append(freeEnergyRatio);

        return sb.toString();
    }

    @Override
    public String toString() {
        //This turns a result into a comma seperated value String
        StringBuilder sb = new StringBuilder();

        SmallRNA s = controller.smallRNA;
        MessengerRNA m = controller.messengerRNA;

        while (alignmentSymbols.length() != mRNA_Characters.length()) {
            alignmentSymbols.append(" ");
        }

        sb.append("\"").append(s.getComment()).append("\"");
        sb.append(",");
        //sb.append("\"").append(m.getComment().replace(",", ";")).append("\"");
        //Try to not have to remove commas from gene name
        sb.append("\"").append(m.getComment()).append("\"");
        sb.append(",");
        sb.append(m.getCategory());
        sb.append(",");
        sb.append(m.getTranscriptPosition());
        sb.append(",");
        sb.append(m.getAbundance());
        sb.append(",");
        sb.append(m.getWeightedAbundance());
        sb.append(",");
        sb.append(m.getNormalisedAbundance());
        sb.append(",");
        sb.append(s.getAbundance());
        sb.append(",");
        sb.append(s.getNormalisedAbundance());
        sb.append(",");
        sb.append("\"").append(sRNA_Characters.toString()).append("\n");
        sb.append(alignmentSymbols.toString()).append("\n");
        sb.append(mRNA_Characters.toString()).append("\"");
        sb.append(",");
        sb.append(score);
        if (Paresnip2Configuration.getInstance().isUseFilter()) {
            sb.append(",");
            sb.append(freeEnergy);
            sb.append(",");
            sb.append(s.getPerfect_mfe());
            sb.append(",");
            sb.append(freeEnergyRatio);
        }

        if (Paresnip2Configuration.getInstance().isUsePValue()) {
            sb.append(",");
            sb.append(pVal);
        }

//        sb.append(controller.messengerRNA.geneName).append(System.getProperty("line.separator"));
//        sb.append(sRNA_Characters.toString()).append(System.getProperty("line.separator"));
//        sb.append(alignmentSymbols.toString()).append(System.getProperty("line.separator"));
//        sb.append(mRNA_Characters.toString()).append(System.getProperty("line.separator"));
//        sb.append("Score: ").append(score).append(System.getProperty("line.separator"));
//        sb.append("Category: ").append(controller.messengerRNA.category).append(System.getProperty("line.separator"));
//        sb.append("Abundance: ").append(controller.messengerRNA.abundance).append(System.getProperty("line.separator"));
//        sb.append("Cleavage Position: ").append(controller.messengerRNA.transcriptPosition).append(System.getProperty("line.separator"));
        return sb.toString();
    }

    public StringBuilder getsRNA_Characters() {
        return sRNA_Characters;
    }

    public StringBuilder getmRNA_Characters() {
        return mRNA_Characters;
    }

    public AlignmentController getController() {
        return controller;
    }

}
