package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;

/**
 *
 * @author Josh
 */
public class MessengerRNA extends Sequence implements Comparable<MessengerRNA> {

    private static ArrayList<ArrayList<MessengerRNA>> cluster7to13;
    private static ArrayList[] categoryClusters;
    private static ArrayList<MessengerRNA> messengerRNAWithInvalidBases;
    private static BitSet[] potentialGroups1to7;
    private static BitSet[] potentialGroups7to13;
    private static BitSet[] potentialGroupsTail;
    public static boolean middleRegionFound;
    public static boolean seedRegionFound;
    public static boolean tailRegionFound;
    public static int similarTailRegionSize;
    private static int tailSize;
    private static boolean isInit = false;
    private static ExecutorService executor;
    int cleavagePosition;
    int transcriptPosition;
    String geneName;
    //this is at the 5' end (and it goes 3' to 5' in my system)
    byte missingNuleotides;
    int category;
    double weightedAbundance;
    double normWeightedAbundance;
    boolean invalidBaseInRegion1To7;
    boolean invalidBaseInRegion7To13;
    boolean invalidBaseInRegionTail;
    short regionTarget1to7;
    short regionTarget7to13;
    short regionTargetTail;
    int abundanceCombined;

    public void setCombinedAbundance(int i) {
        abundanceCombined = i;
    }

    public int getCombinedAbundance() {
        return abundanceCombined;
    }

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

    public int getCategory() {
        return category;
    }

    public int getAbundance() {
        return abundance;
    }

    public double getWeightedAbundance() {
        return weightedAbundance;
    }

    public void setWrightedAbundance(double wAbundance) {
        this.weightedAbundance = wAbundance;
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

        if (invalidBaseInRegion1To7 || invalidBaseInRegion7To13 || invalidBaseInRegionTail) {
            messengerRNAWithInvalidBases.add(this);
        } else {
            cluster7to13.get(regionTarget7to13).add(this);
            //categoryClusters[category].add(this);
        }

    }

    private void initialise() {
        Engine.updateProgressText("Constructing similarity tables...");
        int num = (Paresnip2Configuration.getInstance().getNumberOfThreads() >= 3) ? 3 : Paresnip2Configuration.getInstance().getNumberOfThreads();
        //int num = 3;
        executor = Executors.newFixedThreadPool(num);

        potentialGroups1to7 = new BitSet[16384];
        potentialGroups7to13 = new BitSet[16384];
        messengerRNAWithInvalidBases = new ArrayList();
        middleRegionFound = false;
        tailRegionFound = false;
        seedRegionFound = false;

        int size = Paresnip2Configuration.getInstance().getMinSmallRNALenth();
        int groupSize;
        double numIndexes;

        if (size >= 19) {
            groupSize = 7;
        } else {
            groupSize = size - 12;
        }

        tailSize = groupSize;
        numIndexes = Math.pow(4, groupSize);
        potentialGroupsTail = new BitSet[(int) numIndexes];
        for (int i = 0; i < numIndexes; i++) {
            potentialGroupsTail[i] = new BitSet((int) numIndexes);
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

        categoryClusters = new ArrayList[5];
        for (int i = 0; i < categoryClusters.length; i++) {
            categoryClusters[i] = new ArrayList();
        }

        CountDownLatch cdl = new CountDownLatch(3);

        executor.execute(new SeedRegionAlignerWorker(cdl));
        executor.execute(new MiddleRegionAlignerWorker(cdl));
        executor.execute(new TailRegionAlignerWorker(cdl));
        //System.out.println("Waiting for tables to be built..");
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        //System.out.println("Done waiting!");
        executor.shutdown();
    }

    public void setCategory(int cat) {
        category = cat;
    }

    public MessengerRNA(String seq, String gName, boolean noSetUp, int cleavePos) {
        cleavagePosition = cleavePos;
        comment = gName;
        //replace all , characters wtih a ;
        geneName = comment;//.split(" ")[0];
        seq = new StringBuilder(seq).reverse().toString();
        length = seq.length();
        sequence = super.toBits(seq);

    }

    public MessengerRNA(String seq, String gName) {

        if (!isInit) {
            initialise();
            isInit = true;
        }

        cleavagePosition = 16;
        comment = gName;
        //replace all , characters wtih a ;
        geneName = comment;//.split(" ")[0];
        seq = new StringBuilder(seq).reverse().toString();
        length = seq.length();
        sequence = super.toBits(seq);
        missingNuleotides = 0;

        regionTarget1to7 = (short) super.toBits(seq.substring(cleavagePosition - 10, cleavagePosition - 3));
        regionTarget7to13 = (short) super.toBits(seq.substring(cleavagePosition - 4, cleavagePosition + 3));
        regionTargetTail = (short) super.toBits(seq.substring(cleavagePosition + 2, cleavagePosition + tailSize + 2));


        cluster();

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
                target |= 0L << bit | 0L << bit + 1;
                break;
            case 'G':
                target |= 1L << bit | 0L << bit + 1;
                break;
            case 'A':
                target |= 1L << bit | 1L << bit + 1;
                break;
            case 'U':
                target |= 0L << bit | 0L << bit + 1;
                break;
            case 'C':
                target |= 0L << bit | 1L << bit + 1;
                break;
            default: {
                //unknown base so can't encode it

                //WORK ON THIS LATER?
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

        return MessengerRNA.longToString(sequence, length);
    }

    public int getCleavagePosition() {
        return cleavagePosition;
    }

    //TEST THESE!!!!
    public int getTargetRegion1to7() {

        //return (int) super.toBits(stringSeq.substring(cleavagePosition - 10, cleavagePosition-3));
        //return (int) getWord(7, cleavagePosition - 8);
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

        int size = Paresnip2Configuration.getInstance().getMaxSmallRNALenth();

        if (size >= 19) {
            return 7 - missingNuleotides;
        } else {
            size = size - 7 - 5;
            return size - missingNuleotides;
        }

    }

    public static void setIsInit(boolean isInit) {
        MessengerRNA.isInit = isInit;
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
                for (int j = 0; j < potentialGroups1to7.length; j++) {
                    seedRegionFound = false;
                    if (i == j) {
                        potentialGroups1to7[i].set(j);
                    } else {
                        mRNA = longToString(j, 7);
                        RegionOneAligner mra = new RegionOneAligner(sRNA, mRNA);
                        mra.startPath();

                        if (seedRegionFound) {
                            potentialGroups1to7[i].set(j);
                            //potentialGroups1to7[j].set(i);
                        }
                    }

                }
            }
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
                sRNA = SmallRNA.longToString(i, tailSize);
                for (int j = 0; j < potentialGroupsTail.length; j++) {
                    tailRegionFound = false;
                    if (i == j) {
                        potentialGroupsTail[i].set(j);
                    } else {
                        mRNA = longToString(j, tailSize);
                        RegionThreeAligner mra = new RegionThreeAligner(sRNA, mRNA);
                        mra.startPath();

                        if (tailRegionFound) {
                            potentialGroupsTail[i].set(j);
                            //potentialGroupsTail[j].set(i);
                        }
                    }

                }
            }
            cdl.countDown();
        }

    }

    public static class MiddleRegionAlignerWorker extends Worker {

        public MiddleRegionAlignerWorker(CountDownLatch latch) {
            super(latch);
        }

        @Override
        public void run() {

            for (int i = 0; i < potentialGroups7to13.length; i++) {

                String sRNA;

                sRNA = SmallRNA.longToString(i, 7);

                for (int j = 0; j < potentialGroups7to13.length; j++) {
                    String mRNA;
     
                    middleRegionFound = false;
                    if (i == j) {
                        potentialGroups7to13[i].set(j);
                    } else {

                        mRNA = MessengerRNA.longToString(j, 7);

                        RegionTwoAligner mra = new RegionTwoAligner(sRNA, mRNA);
                        mra.startPath();

                        if (middleRegionFound) {
                            potentialGroups7to13[i].set(j);
                           // potentialGroups7to13[j].set(i);
                        }
                    }

                }
            }
            cdl.countDown();
        }

    }

}
