/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josh
 */
public class AlignmentPathController {

    ArrayList<BoundedPQueue<AlignmentPath>> sideRight;
    ArrayList<BoundedPQueue<AlignmentPath>> sideLeft;
    String smallRNA_Sequence;
    String messengerRNA_Sequence;
    boolean foundRight, foundLeft;
    int leftLowestMM;
    int leftLowestADJMM;
    int leftLowestGaps;
    int leftLowestGUWobbles;
    double leftLowestScore;
    private boolean isPreProcessing;

    public AlignmentPathController() {

        sideRight = new ArrayList();
        sideLeft = new ArrayList();

        for (int i = 0; i <= RuleSet.getMAX_GAPS(); i++) {
            sideLeft.add(new BoundedPQueue(5));
            sideRight.add(new BoundedPQueue(5));
        }

        leftLowestMM = Integer.MAX_VALUE;
        leftLowestADJMM = Integer.MAX_VALUE;
        leftLowestGaps = Integer.MAX_VALUE;
        leftLowestGUWobbles = Integer.MAX_VALUE;
        leftLowestScore = Double.MAX_VALUE;
        foundLeft = false;
        foundRight = false;
        isPreProcessing = false;

    }

    public Alignment align(SmallRNA sRNA, TranscriptFragment mRNA) {

        smallRNA_Sequence = sRNA.toString();
        messengerRNA_Sequence = mRNA.toString();

        List<List<AlignmentPath>> bestLeft = new ArrayList();
        List<List<AlignmentPath>> bestRight = new ArrayList();

        String sRNA5Prime = sRNA.toString().substring(0, 10);
        String mRNA3Prime = mRNA.toString().substring(0, mRNA.getCleavagePosition());

        String sRNA3Prime = sRNA.toString().substring(10, sRNA.toString().length());
        String mRNA5Prime = mRNA.toString().substring(mRNA.getCleavagePosition(), mRNA.toString().length());

        AlignmentPath leftPath = new AlignmentPath5Prime(this, sRNA5Prime, mRNA3Prime, sRNA5Prime.length() - 1, mRNA3Prime.length() - 1);
        leftPath.startPath();
        //System.out.println("Finished left side");

        if (!foundLeft) {
            return null;
        }

        AlignmentPath rightPath = new AlignmentPath3Prime(this, sRNA3Prime, mRNA5Prime, 0, 0);
        rightPath.startPath();
        //System.out.println("Finished right side");

        if (!foundRight) {
            return null;
        }

        for (int i = 0; i <= RuleSet.MAX_GAPS; i++) {

            bestLeft.add(i, sideLeft.get(i).toSortedList());

        }

        for (int i = 0; i <= RuleSet.MAX_GAPS - leftLowestGaps; i++) {

            bestRight.add(i, sideRight.get(i).toSortedList());

        }

        int[][] arr = findBestMatch(bestLeft, bestRight);

        if (arr == null) {
            return null;
        }

        return new Alignment(bestLeft.get(arr[0][0]).get(arr[0][1]), bestRight.get(arr[1][0]).get(arr[1][1]), sRNA, mRNA);

    }

    public boolean align(String sRNA, String mRNA) {
        isPreProcessing = true;

        List<List<AlignmentPath>> bestLeft = new ArrayList();
        List<List<AlignmentPath>> bestRight = new ArrayList();

        String sRNA5Prime = sRNA.substring(0, 4);
        String mRNA3Prime = mRNA.substring(0, 4);

        String sRNA3Prime = sRNA.substring(4, sRNA.length());
        String mRNA5Prime = mRNA.substring(4, mRNA.length());

        AlignmentPath leftPath = new AlignmentPath5Prime(this, sRNA5Prime, mRNA3Prime, sRNA5Prime.length() - 1, mRNA3Prime.length() - 1);
        leftPath.startPath();
        //System.out.println("Finished left side");

        if (!foundLeft) {
            return false;
        }

        AlignmentPath rightPath = new AlignmentPath3Prime(this, sRNA3Prime, mRNA5Prime, 0, 0);
        rightPath.startPath();
        //System.out.println("Finished right side");

        if (!foundRight) {
            return false;
        }

        for (int i = 0; i <= RuleSet.MAX_GAPS; i++) {

            bestLeft.add(i, sideLeft.get(i).toSortedList());

        }

        for (int i = 0; i <= RuleSet.MAX_GAPS - leftLowestGaps; i++) {

            bestRight.add(i, sideRight.get(i).toSortedList());

        }

        int[][] arr = findBestMatch(bestLeft, bestRight);

        if (arr == null) {
            return false;
        }

        return new AlignmentPath[]{bestLeft.get(arr[0][0]).get(arr[0][1]), bestRight.get(arr[1][0]).get(arr[1][1])} != null;

    }

    public boolean alignLeftOnly(String sRNA, String mRNA) {

        isPreProcessing = true;

        AlignmentPath leftPath = new AlignmentPath5Prime(this, sRNA, mRNA, sRNA.length() - 1, mRNA.length() - 1);
        leftPath.startPath();

        return foundLeft;

    }

    public boolean alignRightOnly(String sRNA, String mRNA) {

        leftLowestMM = 0;
        leftLowestADJMM = 0;
        leftLowestGaps = 0;
        leftLowestGUWobbles = 0;
        leftLowestScore = 0;
        isPreProcessing = true;
        AlignmentPath rightPath = new AlignmentPath3Prime(this, sRNA, mRNA, 0, 0);
        rightPath.startPath();

        return foundRight;
    }

    public int getLeftLowestMM() {
        return leftLowestMM;
    }

    public void setLeftLowestMM(int leftLowestMM) {
        this.leftLowestMM = leftLowestMM;
    }

    public int getLeftLowestADJMM() {
        return leftLowestADJMM;
    }

    public void setLeftLowestADJMM(int leftLowestADJMM) {
        this.leftLowestADJMM = leftLowestADJMM;
    }

    public int getLeftLowestGaps() {
        return leftLowestGaps;
    }

    public void setLeftLowestGaps(int leftLowestGaps) {
        this.leftLowestGaps = leftLowestGaps;
    }

    public int getLeftLowestGUWobbles() {
        return leftLowestGUWobbles;
    }

    public void setLeftLowestGUWobbles(int leftLowestGUWobbles) {
        this.leftLowestGUWobbles = leftLowestGUWobbles;
    }

    public double getLeftLowestScore() {
        return leftLowestScore;
    }

    public void setLeftLowestScore(double leftLowestScore) {
        this.leftLowestScore = leftLowestScore;
    }

    private boolean isValid(AlignmentPath left, AlignmentPath right) {

        if (left.score + right.score > RuleSet.MAX_SCORE) {
            return false;
        }

        if (left.currentAdjacentMM + right.currentAdjacentMM > RuleSet.MAX_ADJACENT_MM) {
            return false;
        }

        if (left.currentGaps + right.currentGaps > RuleSet.MAX_GAPS) {
            return false;
        }

        if (left.currentGUWobbles + right.currentGUWobbles > RuleSet.MAX_GU_WOBBLES) {
            return false;
        }

        if (left.firstPosMM && right.firstPosMM) {
            if (left.currentAdjacentMM + right.currentAdjacentMM + 1 > RuleSet.MAX_ADJACENT_MM) {
                return false;
            }
        }

        return true;
    }

    public void setFoundLeft() {
        foundLeft = true;
    }

    public void setFoundRight() {
        foundRight = true;
    }

    private int[][] findBestMatch(List<List<AlignmentPath>> left, List<List<AlignmentPath>> right) {

        int[][] arr = new int[2][2];

        for (int i = 0; i < left.size(); i++) {
            for (int j = 0; j < left.get(i).size(); j++) {
                for (int k = 0; k < right.size(); k++) {
                    for (int l = 0; l < right.get(k).size(); l++) {
                        if (isValid(left.get(i).get(j), right.get(k).get(l))) {
                            arr[0][0] = i;
                            arr[0][1] = j;
                            arr[1][0] = k;
                            arr[1][1] = l;

                            return arr;
                        }
                    }
                }

            }
        }

        return null;
    }

    boolean isPreProcessing() {
        return isPreProcessing;
    }

}
