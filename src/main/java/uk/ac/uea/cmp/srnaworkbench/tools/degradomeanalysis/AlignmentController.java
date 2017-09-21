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
public class AlignmentController {
    AlignmentPath currentBest;
    SmallRNA smallRNA;
    MessengerRNA messengerRNA;
    
    public AlignmentController(SmallRNA smallRNA, MessengerRNA messengerRNA)
    {
        this.smallRNA = smallRNA;
        this.messengerRNA = messengerRNA;
    }
    
    public AlignmentPath align()
    {
        AlignmentPath ap = new AlignmentPath(smallRNA, messengerRNA, this);
        
        ap.startPath();
        
        return currentBest;
    }
}
