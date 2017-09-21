/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;

/**
 *
 * @author Josh
 */
public abstract class AlignmentPath implements Comparable<AlignmentPath> {

    protected AlignmentPathController controller;
    protected double score;
    protected int currentAdjacentMM;
    protected int currentGaps;
    protected int currentSRNAPosition;
    protected int currentMRNAPosition;
    protected int currentGUWobbles;
    protected int currentMMs;
    protected boolean prevMM;
    protected boolean firstPosMM;
    protected String smallRNA_Fragment;
    protected String messengerRNA_fragment;
    protected ArrayList<Character> sRNA_Characters;
    protected ArrayList<Character> mRNA_Characters;
    protected ArrayList<Character> alignmentSymbols;
    protected boolean gap_In_SRNA;
    protected boolean prevPosMM;
    protected boolean prevPosGU;

    abstract void continuePath(boolean gap_In_SRNA);

    abstract void startPath();

    abstract void buildPath();

    abstract boolean reachedBaseCase();

    abstract boolean inSeedRegion();

    protected abstract void linearSearch();

    protected void update(double pairScore) {
        addPair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));

        if (!prevPosGU && !prevPosMM) {
            alignmentSymbols.add('|');
            prevMM = false;
            return;
        }

        if (prevPosGU) {
            currentGUWobbles++;
            alignmentSymbols.add('o');
            if (prevMM) {
                currentAdjacentMM++;
            }
            prevMM = true;
            prevPosGU = false;
        } else if (prevPosMM) {
            alignmentSymbols.add(' ');
            if (prevMM) {
                currentAdjacentMM++;
            }
            prevMM = true;
            prevPosMM = false;
        }

    }

    public AlignmentPath(AlignmentPathController controller, String sRNA_Fragment, String mRNA_Fragment, int start_sRNA_pos, int start_mRNA_pos) {
        this.controller = controller;

        score = 0;
        currentAdjacentMM = 0;
        currentGaps = 0;
        currentGUWobbles = 0;
        prevMM = false;
        firstPosMM = false;
        smallRNA_Fragment = sRNA_Fragment;
        messengerRNA_fragment = mRNA_Fragment;
        sRNA_Characters = new ArrayList();
        mRNA_Characters = new ArrayList();
        alignmentSymbols = new ArrayList();
        currentMMs = 0;
        currentMRNAPosition = start_mRNA_pos;
        currentSRNAPosition = start_sRNA_pos;

    }

    public AlignmentPath(AlignmentPath af) {
        this.controller = af.controller;
        this.score = af.score;
        this.currentAdjacentMM = af.currentAdjacentMM;
        this.currentGUWobbles = af.currentGUWobbles;
        this.currentGaps = af.currentGaps;
        this.currentMRNAPosition = af.currentMRNAPosition;
        this.currentSRNAPosition = af.currentSRNAPosition;
        this.mRNA_Characters = new ArrayList(af.mRNA_Characters);
        this.sRNA_Characters = new ArrayList(af.sRNA_Characters);
        this.alignmentSymbols = new ArrayList(af.alignmentSymbols);
        this.firstPosMM = af.firstPosMM;
        this.prevMM = af.prevMM;
        this.smallRNA_Fragment = af.smallRNA_Fragment;
        this.messengerRNA_fragment = af.messengerRNA_fragment;
        this.currentMMs = af.currentMMs;
    }

    public void addPair(char sRNA, char mRNA) {
        sRNA_Characters.add(sRNA);
        mRNA_Characters.add(mRNA);
    }

    protected double scorePair(char charA, char charB) {

        if (charA == 'A' && charB == 'T' || charA == 'T' && charB == 'A') {
            return 0;
        } else if (charA == 'C' && charB == 'G' || charA == 'G' && charB == 'C') {
            return 0;
        } else if (charA == 'G' && charB == 'T' || charA == 'T' && charB == 'G') {
            prevPosGU = true;
            if (RuleSet.getSEED_REGION_MULTIPLIER() > 0 && inSeedRegion()) {

                return RuleSet.SCORE_GU_WOBBLE * RuleSet.getSEED_REGION_MULTIPLIER();
            }

            return RuleSet.SCORE_GU_WOBBLE;
        }

        prevPosMM = true;

        if (RuleSet.getSEED_REGION_MULTIPLIER() > 0 && inSeedRegion()) {
            return RuleSet.SCORE_MM * RuleSet.SEED_REGION_MULTIPLIER;
        }

        return RuleSet.SCORE_MM;
    }

    @Override
    public int compareTo(AlignmentPath o) {

        int scoreCompare = Double.compare(score, o.score);

        if (scoreCompare != 0) {
            return scoreCompare;
        }

        int adjMM_Compare = Integer.compare(currentAdjacentMM, o.currentAdjacentMM);

        if (adjMM_Compare != 0) {
            return adjMM_Compare;
        }

        int GUWobble_Compare = Integer.compare(currentGUWobbles, o.currentGUWobbles);

        //Add more maybe?
        return GUWobble_Compare;

    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Score: ");
        sb.append(score);
        sb.append(" Adj MM: ");
        sb.append(currentAdjacentMM);
        sb.append(" Gaps: ");
        sb.append(currentGaps);
        sb.append(" GU Wobbles: ");
        sb.append(currentGUWobbles);

        return sb.toString();

        //return "AlignmentPath{" + "score=" + score + ", currentAdjacentMM=" + currentAdjacentMM + ", currentGaps=" + currentGaps + ", currentGUWobbles=" + currentGUWobbles + ", hasMMPos10=" + hasMMPos10 + ", hasMMPos11=" + hasMMPos11 + ", hasGap10_11=" + hasGap10_11 + '}';
    }

    abstract void addToTree();

    public abstract String[] getFinalString();

}
