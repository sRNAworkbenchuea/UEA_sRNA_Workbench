/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;

/**
 *
 * @author Joshua Thody/rew13hpu
 */
public class AlignmentController {

    ArrayList<AlignmentPath> currentBest;
    SmallRNA smallRNA;
    MessengerRNA messengerRNA;
    String smallRNASeq;
    String messengerRNASeq;
    RuleSet ruleset;

    public AlignmentController(SmallRNA smallRNA, MessengerRNA messengerRNA, RuleSet rs) {
        this.ruleset = rs;
        this.smallRNA = smallRNA;
        this.messengerRNA = messengerRNA;
        this.smallRNASeq = smallRNA.getStringSeq();
        this.messengerRNASeq = messengerRNA.getStringSeq();
    }

    public ArrayList<AlignmentPath> align() {
        AlignmentPath ap = new AlignmentPath(smallRNA, messengerRNA, this, ruleset);
        currentBest = new ArrayList<>();

        ap.startPath();

        return currentBest;
    }

    public ArrayList<AlignmentPath> alignPValue() {

        AlignmentPath ap = new AlignmentPath(smallRNASeq, messengerRNASeq, this, ruleset);
        currentBest = new ArrayList<>();
        ap.startPath();

        return currentBest;
    }

    public SmallRNA getSmallRNA() {
        return smallRNA;
    }

    public MessengerRNA getMessengerRNA() {
        return messengerRNA;
    }

}
