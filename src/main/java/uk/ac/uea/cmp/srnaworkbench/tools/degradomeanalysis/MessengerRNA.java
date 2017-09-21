package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Josh
 */
public class MessengerRNA extends Sequence implements Comparable<MessengerRNA> {

    private static ArrayList<ArrayList<MessengerRNA>> cluster7to13;
    private static ArrayList<MessengerRNA> messengerRNAWithInvalidBases;
    private static BitSet[] potentialGroups1to7;
    private static BitSet[] potentialGroups7to13;
    private static BitSet[] potentialGroupsTail;
    public static boolean middleRegionFound;
    public static boolean seedRegionFound;
    public static boolean tailRegionFound;
    public static int similarTailRegionSize;
    private static boolean isInit = false;
    private static ExecutorService executor;
    int cleavagePosition;
    int transcriptPosition;
    String geneName;
    //this is at the 5' end (and it goes 3' to 5' in my system)
    byte missingNuleotides;
    byte category;
    int abundance;
    double weightedAbundance;
    double normWeightedAbundance;
    boolean invalidBaseInRegion1To7;
    boolean invalidBaseInRegion7To13;
    boolean invalidBaseInRegionTail;
    short regionTarget1to7;
    short regionTarget7to13;
    short regionTargetTail;

    public static ArrayList<ArrayList<MessengerRNA>> getCluster7to13() {
        return cluster7to13;
    }

    public static ArrayList<MessengerRNA> getMessengerRNAWithInvalidBases() {
        return messengerRNAWithInvalidBases;
    }

    public static BitSet[] getPotentialGroups1to7() {
        return potentialGroups1to7;
    }

    public static BitSet[] getPotentialGroups7to13() {
        return potentialGroups7to13;
    }

    public static BitSet[] getPotentialGroupsTail() {
        return potentialGroupsTail;
    }

    public int getTranscriptPosition() {
        return transcriptPosition;
    }

    public void setTranscriptPosition(int transcriptPosition) {
        this.transcriptPosition = transcriptPosition;
    }

    public String getGeneName() {
        return geneName;
    }

    public byte getMissingNuleotides() {
        return missingNuleotides;
    }

    public byte getCategory() {
        return category;
    }

    public int getAbundance() {
        return abundance;
    }

    public double getWeightedAbundance() {
        return weightedAbundance;
    }

    public double getNormWeightedAbundance() {
        return normWeightedAbundance;
    }

    public boolean isInvalidBaseInRegion1To7() {
        return invalidBaseInRegion1To7;
    }

    public boolean isInvalidBaseInRegion7To13() {
        return invalidBaseInRegion7To13;
    }

    public boolean isInvalidBaseInRegionTail() {
        return invalidBaseInRegionTail;
    }

    public short getRegionTarget1to7() {
        return regionTarget1to7;
    }

    public short getRegionTarget7to13() {
        return regionTarget7to13;
    }

    public short getRegionTargetTail() {
        return regionTargetTail;
    }

    public static void shrinkToFit() {
        for (ArrayList<MessengerRNA> list : cluster7to13) {
            list.trimToSize();
        }
    }

    private void cluster() {

        cluster7to13.get(regionTarget7to13).add(this);

    }

    private void initialise() {
        int num = (Engine.numThreads >= 3) ? 3 : Engine.numThreads;
        executor = Executors.newFixedThreadPool(num);

        potentialGroups1to7 = new BitSet[16384];
        potentialGroups7to13 = new BitSet[16384];
        messengerRNAWithInvalidBases = new ArrayList();
        middleRegionFound = false;
        tailRegionFound = false;
        seedRegionFound = false;

        int size = Engine.MIN_SRNA_SIZE;
        int groupSize;
        double numIndexes;

        if (size >= 19) {
            groupSize = 7;
        } else {
            groupSize = size - 14;
        }

        numIndexes = Math.pow(4, groupSize);
        potentialGroupsTail = new BitSet[(int)numIndexes];
        for (int i = 0; i < numIndexes; i++) {
            potentialGroupsTail[i] = new BitSet((int)numIndexes);
        }
        similarTailRegionSize = (int) numIndexes;

        for (int i = 0; i < 16384; i++) {
            potentialGroups1to7[i] = new BitSet(16384);
            potentialGroups7to13[i] = new BitSet(16384);
        }

        cluster7to13 = new ArrayList(16384);

        for (int i = 0; i < 16384; i++) {
            cluster7to13.add(i, new ArrayList());
        }

        CountDownLatch cdl = new CountDownLatch(3);

        executor.execute(new SeedRegionAlignerWorker(cdl));
        executor.execute(new MiddleRegionAlignerWorker(cdl));
        executor.execute(new TailRegionAlignerWorker(cdl));
        System.out.println("Waiting for tables to be built..");
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }

        System.out.println("Done waiting!");

        executor.shutdown();

//        double average = 0;
//
//        for (int i = 0; i < potentialGroups1to7.length; i++) {
//            average += potentialGroups1to7[i].cardinality();
//        }
//
//        average /= (double) potentialGroups1to7.length;
//        System.out.println("1-7: " + average);
//
//        average = 0;
//
//        for (int i = 0; i < potentialGroups7to13.length; i++) {
//            average += potentialGroups7to13[i].cardinality();
//        }
//
//        average /= (double) potentialGroups7to13.length;
//        System.out.println("7-13: " + average);
//
//        average = 0;
//
//        for (int i = 0; i < potentialGroupsTail.length; i++) {
//            average += potentialGroupsTail[i].cardinality();
//        }
//
//        average /= (double) potentialGroupsTail.length;
//        System.out.println("Tail: " + average);
        //THIS IS HOW YOU DO IT
//        for (int i = potentialGroups7to13[0].nextSetBit(0); i >= 0; i = potentialGroups7to13[0].nextSetBit(i + 1)) {
//            System.out.println(i);
//            if (i == Integer.MAX_VALUE) {
//                break; // or (i+1) would overflow
//            }
//        }
    }

    public MessengerRNA(String seq, String gName) {

        if (!isInit) {
            initialise();
            isInit = true;
        }

        cleavagePosition = 16;
        geneName = gName;
        stringSeq = new StringBuilder(seq).reverse().toString();
        sequence = super.toBits(stringSeq);
        missingNuleotides = 0;

        regionTarget1to7 = (short) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition - 3));
        regionTarget7to13 = (short) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        regionTargetTail = (short) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));

//        System.out.println("MessengerRNA: " + stringSeq);
//        System.out.println(new StringBuilder(longToString(regionTarget1to7, 7)).toString() + ":" + regionTarget1to7);
//        System.out.println(new StringBuilder(longToString(regionTarget7to13, 7)).toString() + ":" + regionTarget7to13);
//        System.out.println(new StringBuilder(longToString(regionTargetTail, 7)).toString() + ":" + regionTargetTail);
//        System.out.println("End MessengerRNA");

        if (invalidBaseInRegion1To7 || invalidBaseInRegion7To13 || invalidBaseInRegionTail) {
            messengerRNAWithInvalidBases.add(this);
        } else {
            cluster();
        }

    }

    public MessengerRNA(String seq, String gName, int cleavePos) {

        this(seq, gName);
        this.cleavagePosition = cleavePos;
    }

    //missing nucleotides from the 5' end of the mRNA
    public MessengerRNA(String seq, String gName, int cleavePos, byte missingNuleotides) {

        this(seq, gName);
        this.cleavagePosition = cleavePos;
        this.missingNuleotides = missingNuleotides;
    }

    @Override
    protected long setBit(int bit, long target, char c) {
        switch (c) {
            case 'T':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'G':
                target |= (byte) 1 << bit | (byte) 0 << bit + 1;
                break;
            case 'A':
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;
                break;
            case 'U':
                target |= (byte) 0 << bit | (byte) 0 << bit + 1;
                break;
            case 'C':
                target |= (byte) 0 << bit | (byte) 1 << bit + 1;
                break;
            default: {
                //unknown base so can't encode it

                int missingNucleotideBits = missingNuleotides + missingNuleotides;
                //Set flags so that we can deal with it later
                if (bit < (26 - missingNucleotideBits) && bit > 10 - missingNucleotideBits) {
                    invalidBaseInRegion1To7 = true;
                } else if (bit < 40 - missingNucleotideBits) {
                    invalidBaseInRegion7To13 = true;
                } else if (bit < 40 + (SmallRNA.getSimilarTailRegionSize() * 2) - missingNucleotideBits) {
                    invalidBaseInRegionTail = true;
                }

                //just set it to an A and we'll deal with it later
                target |= (byte) 1 << bit | (byte) 1 << bit + 1;

            }
        }

        return target;
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

    private static char getChar(long l) {

        if (l == 0) {
            return 'T';
        } else if (l == 1) {
            return 'G';
        } else if (l == 2) {
            return 'C';
        } else {
            return 'A';
        }
    }

    @Override
    public String toString() {

        return stringSeq;
    }

    public int getCleavagePosition() {
        return cleavagePosition;
    }

    //TEST THESE!!!!
    public int getTargetRegion1to7() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));
        //   return (int) getWord(7, cleavagePosition - 8);
        return regionTarget1to7;
    }

    public int getTargetRegion7to13() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition - 4, cleavagePosition + 3));
        return regionTarget7to13;
        //  return (int) getWord(7, cleavagePosition - 2);
    }

    public int getTargetTailRegion() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition + 2, cleavagePosition + 9));
        return regionTargetTail;
    }

    public int getTargetTailRegionSize() {

        //DONT ACTUALLY THINK THIS IS CORRECT
        int size = Engine.MIN_SRNA_SIZE;

        if (size >= 19) {
            return 7 - missingNuleotides;
        } else {
            size = size - 7 - 5;
            return size - missingNuleotides;
        }

    }

    @Override
    public int compareTo(MessengerRNA o) {

        return Integer.compare(transcriptPosition, o.transcriptPosition);

    }

    private abstract static class Worker implements Runnable {

        CountDownLatch cdl;

        public Worker(CountDownLatch latch) {
            cdl = latch;
        }

    }

    public static class SeedRegionAlignerWorker extends Worker {

        public SeedRegionAlignerWorker(CountDownLatch latch) {
            super(latch);
        }

        @Override
        public void run() {
            String sRNA;
            String mRNA;

            for (int i = 0; i < potentialGroups1to7.length; i++) {
                sRNA = SmallRNA.longToString(i, 7);
                for (int j = i; j < potentialGroups1to7.length; j++) {
                    seedRegionFound = false;
                    if (i == j) {
                        potentialGroups1to7[i].set(j);
                    } else {
                        mRNA = longToString(j, 7);
                        SeedRegionAligner mra = new SeedRegionAligner(sRNA, mRNA);
                        mra.startPath();

                        if (seedRegionFound) {
                            potentialGroups1to7[i].set(j);
                            potentialGroups1to7[j].set(i);
                        }
                    }

                }
            }
            System.out.println("Done seed");
            cdl.countDown();
        }
    }

    public static class TailRegionAlignerWorker extends Worker {

        public TailRegionAlignerWorker(CountDownLatch latch) {
            super(latch);
        }

        @Override
        public void run() {

            String sRNA;
            String mRNA;

            for (int i = 0; i < potentialGroupsTail.length; i++) {
                sRNA = SmallRNA.longToString(i, 7);
                for (int j = i; j < potentialGroupsTail.length; j++) {
                    tailRegionFound = false;
                    if (i == j) {
                        potentialGroupsTail[i].set(j);
                    } else {
                        mRNA = longToString(j, 7);
                        TailRegionAligner mra = new TailRegionAligner(sRNA, mRNA);
                        mra.startPath();

                        if (tailRegionFound) {
                            potentialGroupsTail[i].set(j);
                            potentialGroupsTail[j].set(i);
                        }
                    }

                }
            }
            System.out.println("done tail");
            cdl.countDown();
        }

    }

    public static class MiddleRegionAlignerWorker extends Worker {

        public MiddleRegionAlignerWorker(CountDownLatch latch) {
            super(latch);
        }

        @Override
        public void run() {

            String sRNA;
            String mRNA;

            for (int i = 0; i < potentialGroups7to13.length; i++) {
                sRNA = SmallRNA.longToString(i, 7);
                for (int j = i; j < potentialGroups7to13.length; j++) {
                    middleRegionFound = false;
                    if (i == j) {
                        potentialGroups7to13[i].set(j);
                    } else {
                        mRNA = longToString(j, 7);
                        MiddleRegionAligner mra = new MiddleRegionAligner(sRNA, mRNA);
                        mra.startPath();

                        if (middleRegionFound) {
                            potentialGroups7to13[i].set(j);
                            potentialGroups7to13[j].set(i);
                        }
                    }

                }
            }
            System.out.println("Done Middle");
            cdl.countDown();
        }

    }

}
