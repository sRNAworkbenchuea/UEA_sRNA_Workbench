/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.stream.Collectors;

/**
 *
 * @author Josh
 */
public class AlignmentPath3Prime extends AlignmentPath {

    boolean hasMMPos11;

    public AlignmentPath3Prime(AlignmentPathController controller, String sRNA_Fragment, String mRNA_Fragment, int start_sRNA_pos, int start_mRNA_pos) {
        super(controller, sRNA_Fragment, mRNA_Fragment, start_sRNA_pos, start_mRNA_pos);
        hasMMPos11 = false;
    }

    public AlignmentPath3Prime(AlignmentPath3Prime af) {
        super(af);
        hasMMPos11 = af.hasMMPos11;
    }

    @Override
    void continuePath(boolean gap_In_SRNA) {

        alignmentSymbols.add(' ');
        if (gap_In_SRNA) {
            //update path positions
             if (currentMRNAPosition < messengerRNA_fragment.length() - 1) {
                currentMRNAPosition++;
            }
            currentGaps++;
            prevMM = true;
            score += RuleSet.SCORE_GAP;
        } else {
            //update path positions
            if (currentSRNAPosition < smallRNA_Fragment.length() - 1) {
                currentSRNAPosition++;
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

        //currentSRNAPosition = 0;
        //currentMRNAPosition = 0;
        buildPath();

    }

    @Override
    boolean reachedBaseCase() {
        
        if (controller.isPreProcessing() && controller.foundRight) {
            return true;
        }


        if (hasMMPos11 && !RuleSet.isALLOW_MM_11()) {
            //System.out.println("Has non-permitted MM at pos 11");
            return true;
        }

        if (currentGaps + controller.leftLowestGaps > RuleSet.MAX_GAPS) {
           // System.out.println("Combined with lowest number of gaps from 5' exceeds max number in rule set");
            return true;
        }

        if (currentAdjacentMM + controller.leftLowestADJMM > RuleSet.MAX_ADJACENT_MM) {
            //System.out.println("Combined with lowest number of ADJ MM from 5' exceeds max number in rule set");
            return true;
        }

        if (currentGUWobbles + controller.leftLowestGUWobbles > RuleSet.MAX_GU_WOBBLES) {
            //System.out.println("Combined with lowest number of GU wobbles from 5' exceeds max number in rule set");
            return true;
        }

        if (score + controller.leftLowestScore > RuleSet.MAX_SCORE) {
            //System.out.println("Combined with lowest score from 5' exceeds max number in rule set");

            return true;
        }
        return false;
    }

    @Override
    boolean inSeedRegion() {

        return currentSRNAPosition < 3;
    }

    @Override
    protected void update(double pairScore) {
        super.update(pairScore);

        if (currentSRNAPosition == 0 && pairScore > 0) {
            firstPosMM = true;
            hasMMPos11 = true;
            prevMM = true;
        }
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
    protected void buildPath() {
        //Base cases
        if (reachedBaseCase()) {
            return;
        }

        if (currentSRNAPosition == smallRNA_Fragment.length() - 1) {

            double pairScore = score += scorePair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));
            update(pairScore);
            if (reachedBaseCase()) {
                return;
            }
            completeStrings();
            addToTree();

            return;
        }

        while (currentSRNAPosition < smallRNA_Fragment.length() && currentMRNAPosition < messengerRNA_fragment.length()) {
           // System.out.println(currentSRNAPosition + "," + smallRNA_Fragment.charAt(currentSRNAPosition) + " : " + messengerRNA_fragment.charAt(currentMRNAPosition) + "," + currentMRNAPosition);
            double pairScore = scorePair(smallRNA_Fragment.charAt(currentSRNAPosition), messengerRNA_fragment.charAt(currentMRNAPosition));

            //do recursive calls
            if (pairScore > 0 && currentGaps < RuleSet.MAX_GAPS) {
            //    System.out.println("Branched onto gapped sRNA");
                AlignmentPath gappedSRNA = new AlignmentPath3Prime(this);
                gappedSRNA.addPair('-', messengerRNA_fragment.charAt(currentMRNAPosition));
                gappedSRNA.continuePath(true);
             //   System.out.println("Branched onto gapped mRNA");
                AlignmentPath gappedMRNA = new AlignmentPath3Prime(this);
                gappedMRNA.addPair(messengerRNA_fragment.charAt(currentMRNAPosition), '-');
                gappedMRNA.continuePath(false);
             //   System.out.println("BACK ON LINEAR");

            }

            score += pairScore;

            update(pairScore);

            if (reachedBaseCase()) {
                //System.out.println(toString());
                return;
            }

            currentSRNAPosition++;
            currentMRNAPosition++;
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
        sb.append(" MM 11:");
        sb.append(hasMMPos11);

        return sb.toString();
    }

    @Override
    void addToTree() {
        if (controller.sideRight.get(currentGaps).queue.size() >= controller.sideRight.get(currentGaps).size) {
            if (controller.sideRight.get(currentGaps).queue.peek().compareTo(this) == 1) {
                controller.sideRight.get(currentGaps).offer(this);
                controller.setFoundRight();

            }
        } else {
            controller.sideRight.get(currentGaps).offer(this);
            controller.setFoundRight();
        }
    }

    private void completeStrings() {
        while (currentMRNAPosition < messengerRNA_fragment.length()) {
            mRNA_Characters.add(messengerRNA_fragment.charAt(currentMRNAPosition));
            alignmentSymbols.add(' ');
            currentMRNAPosition++;
        }
    }

    @Override
    public String[] getFinalString() {
        
        String[] arr = new String[3];
        
        String sRNA = sRNA_Characters.stream().map(e->e.toString()).collect(Collectors.joining());
        String alignment = alignmentSymbols.stream().map(e->e.toString()).collect(Collectors.joining());
        String mRNA = mRNA_Characters.stream().map(e->e.toString()).collect(Collectors.joining());
        
        arr[0] = sRNA;
        arr[1] = alignment;
        arr[2] = mRNA;

        return arr;
    }
    
    @Override
    protected double scorePair(char charA, char charB)  {
        double s = super.scorePair(charA, charB);
        
        if(s == 0 || currentSRNAPosition != 0)
            return s;

        if(controller.isPreProcessing())
            return s;

        if(RuleSet.SCORE_MM_11 != 0)
            return RuleSet.SCORE_MM_11;
        
        return s;
    }

}
