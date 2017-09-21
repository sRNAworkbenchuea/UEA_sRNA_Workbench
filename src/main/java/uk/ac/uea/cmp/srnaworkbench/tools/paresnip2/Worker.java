package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Josh
 */
public class Worker implements Runnable {

    private ArrayList<SmallRNA> potentialTargets;
    private TranscriptFragment mRNA;
    private List<Alignment> list;
    private CountDownLatch countDownLatch;

    public Worker(ArrayList<SmallRNA> pTargets, TranscriptFragment tag, List<Alignment> l, CountDownLatch cdl) {
        potentialTargets = pTargets;
        mRNA = tag;
        list = l;
        countDownLatch = cdl;
    }

    @Override
    public void run() {

        
        for (SmallRNA sRNA : potentialTargets) {

            boolean contains1to7 = false;
            boolean containsTail = false;
            System.out.println(potentialTargets.size());

//            if (SmallRNA.getPotentialGroup1to7().get(mRNA.getTargetRegion1to7()).contains(sRNA.getRegion1to7())) {
//                contains1to7 = true;
//            }
//            else {
//                break;
//            }
//            
//            if (SmallRNA.getPotentialGroupTail().get(mRNA.getTargetTailRegion()).contains(sRNA.getTailRegion())) {//|| skip) {
//                containsTail = true;
//            }
//
//            if (containsTail && contains1to7) {
//                AlignmentPathController apc = new AlignmentPathController();
//
//                Alignment alignment = apc.align(sRNA, mRNA);
//                if (alignment != null) {
//                    list.add(alignment);
//                }
//
//            }
        }
        countDownLatch.countDown();
    }
}
