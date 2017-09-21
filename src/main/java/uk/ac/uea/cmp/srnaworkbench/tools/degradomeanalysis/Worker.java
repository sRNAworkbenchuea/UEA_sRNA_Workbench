/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Josh
 */
public class Worker implements Runnable {

    SmallRNA smallRNA;
    List<AlignmentPath> validPaths;
    private static AtomicInteger numberCompleted = new AtomicInteger(1);
    private static volatile int totalSmallRNAs;

    public static void setTotal(int i) {
        totalSmallRNAs = i;
    }

    static void incrementCounter() {
        System.out.println("Completed " + numberCompleted.getAndIncrement() + " out of " + totalSmallRNAs);
    }

    public Worker(SmallRNA smallRNA) {
        this.smallRNA = smallRNA;
        this.validPaths = new ArrayList();
    }

    @Override
    public void run() {

        AlignmentController controller;
        int seedRegionNum = smallRNA.getRegion1to7();
        int middleRegionNum = smallRNA.getRegion7to13();
        int tailRegionNum = smallRNA.getRegionTail();
        boolean contains;

        BitSet bs = MessengerRNA.getPotentialGroups7to13()[middleRegionNum];

        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            for (MessengerRNA messengerRNA : MessengerRNA.getCluster7to13().get(i)) {
                contains = true;

                if (Engine.allowedCategories[messengerRNA.getCategory()]) {
                    boolean containsSeed = MessengerRNA.getPotentialGroups1to7()[seedRegionNum].get(messengerRNA.getRegionTarget1to7());  
                  //  System.out.println(MessengerRNA.getPotentialGroups1to7()[seedRegionNum].get(messengerRNA.getRegionTarget1to7()));

//                    if (!MessengerRNA.getPotentialGroups1to7()[seedRegionNum].get(messengerRNA.getRegionTarget1to7())) {
//                        contains = false;
//                    }

                    boolean containsTail = MessengerRNA.getPotentialGroupsTail()[tailRegionNum].get(messengerRNA.getRegionTargetTail());


//                    if (!MessengerRNA.getPotentialGroupsTail()[tailRegionNum].get(messengerRNA.getRegionTargetTail())) {
//                        contains = false;
//                    }

                    if (containsSeed && containsTail) {
                        controller = new AlignmentController(smallRNA, messengerRNA);
                        AlignmentPath path = controller.align();

                        if (path != null) {
                            validPaths.add(path);
                        }
                    }
                }
            }
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }

        incrementCounter();

        //System.out.println("Total number: " + validPaths.size());
//
//        for (AlignmentPath path : validPaths) {
//            path.print();
//        }

    }

}
