/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 *
 * @author Josh
 */
public class AlignmentPath5Prime extends AlignmentPath {

    //boolean hasGap10_11;
    boolean hasMMPos10;

    public AlignmentPath5Prime(AlignmentPathController controller, String sRNA_Fragment, String mRNA_Fragment, int start_sRNA_pos, int start_mRNA_pos) {
        super(controller, sRNA_Fragment, mRNA_Fragment, start_sRNA_pos, start_mRNA_pos);
        //hasGap10_11 = false;
        hasMMPos10 = false;

    }

    public AlignmentPath5Prime(AlignmentPath5Prime af) {
        super(af);
        //this.hasGap10_11 = af.hasGap10_11;
        this.hasMMPos10 = af.hasMMPos10;

    }

    @Override
    protected void linearSearch() {
        currentSRNAPosition = 0;

        for (; currentSRNAPosition < messengerRNA_fragment.length(); currentSRNAPosition++) {
            score += scorePair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));
            update(score);

        }

        if (reachedBaseCase()) {
            return;
        }

        completeStrings();
        addToTree();
    }

    @Override
    void continuePath(boolean gap_In_SRNA) {

        //ASK LEIGHTON ABOUT GAPS CONTRIBUTING TO ADJACENT MM?
        alignmentSymbols.add(' ');

        if (gap_In_SRNA) {
            //update path positions
            if (currentMRNAPosition > 0) {
                currentMRNAPosition--;
            }
            currentGaps++;
            prevMM = true;
            score += RuleSet.SCORE_GAP;
        } else {
            //update path positions
            if (currentSRNAPosition > 0) {
                currentSRNAPosition--;
            }
            currentGaps++;
            prevMM = true;

            score += RuleSet.SCORE_GAP;
        }

        buildPath();

    }

    @Override
    void startPath() {

        if (RuleSet.getMAX_GAPS() == 0) {
            linearSearch();
        }

        //currentSRNAPosition = smallRNA_Fragment.length() - 1;
        //currentMRNAPosition = messengerRNA_fragment.length() - 1;
        buildPath();

    }

    @Override
    boolean reachedBaseCase() {

        if (controller.isPreProcessing() && controller.foundLeft) {
            return true;
        }

        if (score > RuleSet.MAX_SCORE) {
            //System.out.println("Score is above MAX_SCORE");
            return true;
        }
        
        if (currentAdjacentMM > RuleSet.getMAX_ADJACENT_MM_SEED_REGION()) {
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

        return false;
    }

    @Override
    boolean inSeedRegion() {
        return true;
    }

    @Override
    protected void update(double pairScore) {
            super.update(pairScore);

        if (currentSRNAPosition == 9 && pairScore > 0) {
            firstPosMM = true;
            hasMMPos10 = true;
        }

    }

    @Override
    protected double scorePair(char charA, char charB) {
        double s = super.scorePair(charA, charB);

        if (s == 0 || currentSRNAPosition != 9) {
            return s;
        }

        if (currentSRNAPosition == 9 && RuleSet.SCORE_MM_10 != 0) {
            return RuleSet.SCORE_MM_10;
        }

        return score;
    }

    @Override
    protected void buildPath() {
        //Base cases

        if (currentSRNAPosition < 1) {

            double pairScore = scorePair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));
            score += pairScore;
            update(pairScore);
            if (reachedBaseCase()) {
                return;
            }
            completeStrings();
            addToTree();

            return;
        }

        while (currentSRNAPosition >= 0 && currentMRNAPosition >= 0) {
            // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
                // System.out.println("Branched onto gapped sRNA");
                AlignmentPath gappedSRNA = new AlignmentPath5Prime(this);
                gappedSRNA.addPair('-', messengerRNA_fragment.charAt(currentMRNAPosition));
                gappedSRNA.continuePath(true);
                // System.out.println("Branched onto gapped mRNA");
                AlignmentPath gappedMRNA = new AlignmentPath5Prime(this);
                gappedMRNA.addPair(smallRNA_Fragment.charAt(currentSRNAPosition), '-');
                gappedMRNA.continuePath(false);
                //System.out.println("BACK ON LINEAR");

            }

            score += pairScore;

            update(pairScore);

            if (reachedBaseCase()) {
                //System.out.println(toString());
                return;
            }

            currentSRNAPosition--;
            currentMRNAPosition--;
        }

        if (!reachedBaseCase()) {
            //System.out.println("Score: " + score);
            completeStrings();
            addToTree();

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
//        sb.append(" Gap 10/11:");
//        sb.append(hasGap10_11);
        sb.append(" MM 10:");
        sb.append(hasMMPos10);

        return sb.toString();
    }

    @Override
    void addToTree() {

        if (controller.leftLowestADJMM > this.currentAdjacentMM) {
            controller.setLeftLowestADJMM(currentAdjacentMM);
        }

        if (controller.leftLowestGUWobbles > this.currentGUWobbles) {
            controller.setLeftLowestGUWobbles(currentGUWobbles);
        }

        if (controller.leftLowestMM > this.currentMMs) {
            controller.setLeftLowestMM(currentMMs);
        }

        if (controller.leftLowestScore > this.score) {
            controller.setLeftLowestScore(score);
        }

        if (controller.leftLowestGaps > this.currentGaps) {
            controller.setLeftLowestGaps(currentGaps);
        }

        if (controller.sideLeft.get(currentGaps).queue.size() >= controller.sideLeft.get(currentGaps).size) {
            if (controller.sideLeft.get(currentGaps).queue.peek().compareTo(this) == 1) {

                controller.sideLeft.get(currentGaps).offer(this);
                controller.setFoundLeft();

            }
        } else {
            controller.sideLeft.get(currentGaps).offer(this);
            controller.setFoundLeft();
        }

    }

    private void completeStrings() {

        while (currentMRNAPosition >= 0) {
            mRNA_Characters.add(messengerRNA_fragment.charAt(currentMRNAPosition));
            sRNA_Characters.add(' ');
            alignmentSymbols.add(' ');
            currentMRNAPosition--;
        }

    }

    @Override
    public String[] getFinalString() {

        String[] arr = new String[3];

        Collections.reverse(sRNA_Characters);
        Collections.reverse(alignmentSymbols);
        Collections.reverse(mRNA_Characters);

        String sRNA = sRNA_Characters.stream().map(e -> e.toString()).collect(Collectors.joining());
        String alignment = alignmentSymbols.stream().map(e -> e.toString()).collect(Collectors.joining());
        String mRNA = mRNA_Characters.stream().map(e -> e.toString()).collect(Collectors.joining());

        arr[0] = sRNA;
        arr[1] = alignment;
        arr[2] = mRNA;

        return arr;

    }

}
