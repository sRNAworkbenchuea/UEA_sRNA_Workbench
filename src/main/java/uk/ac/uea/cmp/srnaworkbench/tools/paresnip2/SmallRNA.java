package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NEED TO GET THIS WORKING WITH THE USERS RULESET
 *
 * @author Josh
 */
public class SmallRNA extends Sequence {

    private static boolean isInit = false;
    private static ArrayList<ArrayList<SmallRNA>> cluster7to13;
    private static ArrayList<ResizableIntArray> potentialGroups7to13;
    private static ArrayList<ResizableIntArray> potentialGroups1to7;
    private static ArrayList<ResizableIntArray> potentialGroupsTail;
    byte tailRegionLength;
    public static int similarTailRegionSize;
    private ExecutorService executor;
    boolean discard;
    int region1to7;
    int region7to13;
    int regionTail;

    public static int getSimilarTailRegionSize() {
        return similarTailRegionSize;
    }

    private static void setSimilarTailRegionSize(int similarTailRegionSize) {
        SmallRNA.similarTailRegionSize = similarTailRegionSize;
    }

    
    
    public static ArrayList<ResizableIntArray> getPotentialGroups7to13() {
        return potentialGroups7to13;
    }

    public static ArrayList<ResizableIntArray> getPotentialGroup1to7() {
        return potentialGroups1to7;
    }

    public static ArrayList<ResizableIntArray> getPotentialGroupTail() {
        return potentialGroupsTail;
    }

    public static ArrayList<ArrayList<SmallRNA>> getCluster7to13() {
        return cluster7to13;
    }

    private void cluster() {
        cluster7to13.get(getRegion7to13()).add(this);
    }

    private void initialise() {

        int num = (Engine.numThreads >= 3) ? 3 : Engine.numThreads;
        discard = false;

        executor = Executors.newFixedThreadPool(num);

        cluster7to13 = new ArrayList(16384);

        for (int i = 0; i < 16384; i++) {
            cluster7to13.add(i, new ArrayList());
        }

        potentialGroups7to13 = new ArrayList(16384);

        for (int i = 0; i < 16384; i++) {
            potentialGroups7to13.add(i, new ResizableIntArray());
        }

        potentialGroups1to7 = new ArrayList();

        for (int i = 0; i < 16384; i++) {
            potentialGroups1to7.add(i, new ResizableIntArray());
        }

//        clusterAtSeedRegion();
//           clusterAtMiddleRegion();
//        clusterAtTail();

        try {
            File f = new File("1_7_table.out");
            if (f.exists() && !f.isDirectory()) {
                // do something
                final ObjectInputStream in_1_7 = new ObjectInputStream(new FileInputStream("1_7_table.out"));
                potentialGroups1to7 = (ArrayList<ResizableIntArray>) in_1_7.readObject();

                final ObjectInputStream in_7_13 = new ObjectInputStream(new FileInputStream("7_13_table.out"));
                potentialGroups7to13 = (ArrayList<ResizableIntArray>) in_7_13.readObject();

                final ObjectInputStream in_tail = new ObjectInputStream(new FileInputStream("tail_table.out"));
                potentialGroupsTail = (ArrayList<ResizableIntArray>) in_tail.readObject();

            } else {

                final ObjectOutputStream out_1_7 = new ObjectOutputStream(new FileOutputStream("1_7_table.out"));
                final ObjectOutputStream out_7_13 = new ObjectOutputStream(new FileOutputStream("7_13_table.out"));
                final ObjectOutputStream out_tail = new ObjectOutputStream(new FileOutputStream("tail_table.out"));

                final CountDownLatch latch = new CountDownLatch(3);

                System.out.println("Starting preprocessing....");

                executor.execute(new SeedRegionWorker(latch));
                executor.execute(new MiddleRegionWorker(latch));
                executor.execute(new TailRegionWorker(latch));

                latch.await();  //main thread is waiting on CountDownLatch to finish
                System.out.println("All preprocessing complete!");
                out_1_7.writeObject(potentialGroups1to7);
                out_7_13.writeObject(potentialGroups7to13);
                out_tail.writeObject(potentialGroupsTail);
                executor.shutdown();

            }
        } catch (IOException | ClassNotFoundException | InterruptedException ie) {
            System.out.println(ie);
        }
    }

    public SmallRNA(String seq) {
        if (!isInit) {
            initialise();
            isInit = true;
        }
        if ((byte) (Engine.MIN_SRNA_SIZE - 7 - 5) > 7) {
            tailRegionLength = 7;
        } else {
            tailRegionLength = (byte) (Engine.MIN_SRNA_SIZE - 7 - 5);
        }

        sequence = super.toBits(seq);
        stringSeq = seq;
        
        region1to7 = (int) super.toBits(stringSeq.substring(0, 7));
        region7to13 = (int) super.toBits(stringSeq.substring(6, 13));
        regionTail = (int) super.toBits(stringSeq.substring(12, stringSeq.length()));
        
        if (!discard) {
            cluster();
        }

    }

    @Override
    protected long setBit(int bit, long target, char c) {

        switch (c) {
            case 'A':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'C':
                target |= (byte) 1 << bit | (byte) 0 << bit + 1;
                break;
            case 'T':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'U':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'G':
                target |= (byte) 0 << bit | (byte) 1 << bit + 1;
                break;
            default:
                discard = true;
        }

        return target;
    }

    private static char getChar(long l) {

        if (l == 0) {
            return 'A';
        } else if (l == 1) {
            return 'C';
        } else if (l == 2) {
            return 'G';
        } else {
            return 'T';
        }
    }

    @Override
    public String toString() {

        return stringSeq;
    }

    public static String longToString(long l, int n) {

        char[] s = new char[n];

        int index = 0;
        for (int i = 0; i < n * 2; i += 2) {
            long mask = (1L << (i - i + 2)) - 1;
            s[index] = getChar((l >> i) & mask);
            index++;
        }

        return new String(s);

    }

    public int getRegion1to7() {
        //return (int) getWord(7, 2);
        
        //return (int) super.toBits(stringSeq.substring(0, 7));
        
        return region1to7;
    }

    public int getRegion7to13() {
        //return (int) super.toBits(stringSeq.substring(6, 13));
        //return (int) getWord(7, 7);
        return region7to13;
    }

    public int getTailRegion() {

        //return (int) super.toBits(stringSeq.substring(12, stringSeq.length()));
        return regionTail;
    }

    int getTailRegionLength() {
        return tailRegionLength;
    }

    private class SeedRegionWorker implements Runnable {

        final CountDownLatch latch;

        public SeedRegionWorker(CountDownLatch cdl) {
            latch = cdl;
        }

        @Override
        public void run() {

            String str;
            String ref;

            for (int i = 0; i < potentialGroups1to7.size(); i++) {
                str = SmallRNA.longToString(i, 7);
                //System.out.println("Seed: " + i);
                for (int j = i; j < potentialGroups1to7.size(); j++) {
                    if (i == j) {
                        potentialGroups1to7.get(i).add(j);
                    } else {
                        ref = TranscriptFragment.longToString(j, 7);
                        AlignmentPathController apc = new AlignmentPathController();

                        if (apc.alignLeftOnly(str, ref)) {
                            potentialGroups1to7.get(i).add(j);
                            potentialGroups1to7.get(j).add(i);
                        }
                    }
                }
            }
            System.out.println("Seed region done!");
            latch.countDown();
        }

    }

    private class MiddleRegionWorker implements Runnable {

        final CountDownLatch latch;

        public MiddleRegionWorker(CountDownLatch cdl) {
            latch = cdl;
        }

        @Override
        public void run() {

            String str;
            String ref;

            for (int i = 0; i < potentialGroups7to13.size(); i++) {
                //System.out.println("Middle: " + i);
                str = SmallRNA.longToString(i, 7);
                for (int j = i; j < potentialGroups7to13.size(); j++) {
                    if (i == j) {
                        potentialGroups7to13.get(i).add(j);
                    } else {
                        ref = TranscriptFragment.longToString(j, 7);
                        AlignmentPathController apc = new AlignmentPathController();

                        if (apc.align(str, ref)) {
                            potentialGroups7to13.get(i).add(j);
                            potentialGroups7to13.get(j).add(i);
                        }

                    }
                }
            }
            System.out.println("Middle region done!");
            latch.countDown();
        }

    }

    private class TailRegionWorker implements Runnable {

        final CountDownLatch latch;

        public TailRegionWorker(CountDownLatch cdl) {
            latch = cdl;
        }

        @Override
        public void run() {
            int size = Engine.MIN_SRNA_SIZE;
            int groupSize;
            double numIndexes;

            if (size >= 19) {
                groupSize = 7;
            } else {
                groupSize = size - 14;
            }

            potentialGroupsTail = new ArrayList();
            numIndexes = Math.pow(4, groupSize);
            setSimilarTailRegionSize((int)numIndexes);

            for (int i = 0; i < numIndexes; i++) {
                potentialGroupsTail.add(i, new ResizableIntArray());
            }

            String str;
            String ref;

            for (int i = 0; i < potentialGroupsTail.size(); i++) {
                //System.out.println("Tail: " + i);
                str = SmallRNA.longToString(i, 7);
                for (int j = i; j < potentialGroupsTail.size(); j++) {
                    if (i == j) {
                        potentialGroupsTail.get(i).add(j);
                    } else {
                        ref = TranscriptFragment.longToString(j, 7);
                        AlignmentPathController apc = new AlignmentPathController();

                        if (apc.alignRightOnly(str, ref)) {
                            potentialGroupsTail.get(i).add(j);
                            potentialGroupsTail.get(j).add(i);
                        }
                    }
                }
            }
            System.out.println("Tail region done!");
            latch.countDown();
        }

    }

}
