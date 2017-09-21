package uk.ac.uea.cmp.srnaworkbench.tools.shortreadaligner;

import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josh
 */
public class ShortReadAligner {

    int MIN_LENGTH;
    int MAX_LENGTH;
    protected HashMap<String, List<ShortReadAlignment>> alignedReads;
    protected HashMap<ShortRead, Integer> shortReads;
    private ExecutorService executor;
    ShortReadArray[] reads;
    static int numRunning = 0;
    int numThreads;
    protected String longReadsFile;
    protected String shortReadsFile;
    boolean processNegativeStrand;

    public static synchronized void incrementJobs(int i) {
        numRunning += i;
    }

    //TESTING THE SHORT READ ALIGNER AKA MAKING IT BETTER WITH LONGER SEQUENCES
    public static void main(String args[])
    {
        ShortReadAligner aligner = new ShortReadAligner(8, "human.fa", "C0LY_P_trimmed.txt", 19, 24, false);
    }
    
    public ShortReadAligner(int nThreads, String longReadsFile, String shortReadsFile, int min, int max, boolean bothStrand) {
        numThreads = nThreads;
        this.longReadsFile = longReadsFile;
        this.shortReadsFile = shortReadsFile;
        MIN_LENGTH = min;
        MAX_LENGTH = max;
        processNegativeStrand = bothStrand;
        initialise();
        processShortSequences();
        processLongReads();

        //wait for all threads to finish running (probably a better way to handle this?)
        try {
            executor.shutdown();
            while (!executor.isTerminated()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ShortReadAligner.class.getName()).log(Level.SEVERE, null, ex);
        }

        removeEmptyLists();

        //print();
    }

    private void getFileFormat() {

    }

    private void processShortSequences() {

        shortReads = new HashMap();
        
        FileReader input;
        try {
            //home pc
            input = new FileReader(shortReadsFile);

            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;
            String seq;
            int abundance;

            //Need to find a better way to do this! HASH MAPS ARE NOT THE WAY!!!
            while ((myLine = bufRead.readLine()) != null) {

//                if (!myLine.startsWith(">")) {
//                    seq = myLine;
//
//                    if (seq.length() >= MIN_LENGTH && seq.length() <= MAX_LENGTH) {
//                        ShortRead sequence;
//
//                        try {
//                            sequence = new ShortRead(seq);
//
//                            if (shortReads.containsKey(sequence)) {
//                                shortReads.put(sequence, shortReads.get(sequence) + 1);
//                            } else {
//                                shortReads.put(sequence, 1);
//                            }
//
//                        } catch (SequenceContainsInvalidBaseException ex) {
//                            //handle later
//                        }
//                    }
//                }

                if (!myLine.equals("")) {
                    seq = myLine.split("\t")[0];

                    abundance = Integer.parseInt(myLine.split("\t")[1]);
                    if (seq.length() >= MIN_LENGTH && seq.length() <= MAX_LENGTH) {
                        ShortRead sequence;
                        try {
                            sequence = new ShortRead(seq, abundance);
                            reads[Math.abs(MIN_LENGTH - seq.length())].add(sequence);
                        } catch (SequenceContainsInvalidBaseException ex) {
                            //handle later
                        }
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        //Iterate over and update the abundance
//        Iterator<ShortRead> it = shortReads.keySet().iterator();
//
//        while (it.hasNext()) {
//            ShortRead shortRead = it.next();
//            shortRead.setAbundance(shortReads.get(shortRead));
//            reads[Math.abs(MIN_LENGTH - shortRead.stringSeq.length())].add(shortRead);
//            it.remove();
//        }

        System.out.println("Finished reading short reads, now sorting them...");
        for (ShortReadArray t : reads) {
            t.sort();
        }
        System.out.println("Finished sorting...");

    }

    private void processLongReads() {
        FileReader input;
        StringBuilder sb = new StringBuilder();
        String gene = null;

        try {

            input = new FileReader(longReadsFile);

            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while ((myLine = bufRead.readLine()) != null) {

                if (myLine.startsWith(">")) {
                    if (gene != null) {
                        String longRead = sb.toString();
                        process(gene, longRead);
                        sb = new StringBuilder();
                    }

                    gene = myLine.split(" ")[0].split(">")[1];
                } else {
                    sb.append(myLine.trim());
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            String longRead = sb.toString();
            process(gene, longRead);
        }

    }

    private void process(String gene, String seq) {
        
        System.out.println(seq.length());

        alignedReads.put(gene, Collections.synchronizedList(new ArrayList()));
        char[] reverseString = new char[seq.length()];

        //MAX_TAG_LENGTH - MIN_TAG_LENGTH + 1
        LongReadPositionArray[] arr = new LongReadPositionArray[MAX_LENGTH - MIN_LENGTH + 1];

        for (int z = 0; z < MAX_LENGTH - MIN_LENGTH + 1; z++) {
            arr[z] = new LongReadPositionArray();

        }

        for (int i = 0; i < seq.length() - MAX_LENGTH; i++) {
            reverseString[seq.length() - i - 1] = reverseComplement(seq.charAt(i));
            for (int x = 0; x < arr.length; x++) {
                Sequence g;
                try {
                    g = new LongReadFragment(seq.substring(i, i + MIN_LENGTH + x));
                    arr[x].add(g.getSequence(), i);
                } catch (SequenceContainsInvalidBaseException ex) {
                    arr[x].add(-1, i);
                }

            }
        }

        for (int i = seq.length() - MAX_LENGTH; i < seq.length(); i++) {
            reverseString[seq.length() - i - 1] = reverseComplement(seq.charAt(i));
        }

        for (int i = 0; i < arr.length - 1; i++) {
            if (seq.length() - MAX_LENGTH < 0) {
                for (int j = 0; j <= seq.length() - MIN_LENGTH - i; j++) {

                    LongReadFragment g;
                    try {
                        g = new LongReadFragment(seq.substring(j, j + MIN_LENGTH + i));
                        arr[i].add(g.sequence, j);
                    } catch (SequenceContainsInvalidBaseException ex) {
                        arr[i].add(-1, j);

                    }

                }
            } else {
                for (int j = seq.length() - MAX_LENGTH; j <= seq.length() - MIN_LENGTH - i; j++) {
                    LongReadFragment g;
                    try {
                        g = new LongReadFragment(seq.substring(j, j + MIN_LENGTH + i));
                        arr[i].add(g.sequence, j);
                    } catch (SequenceContainsInvalidBaseException ex) {
                        //handle later
                        arr[i].add(-1, j);
                    }

                }
            }
        }

        executor.execute(new Aligner(gene, arr, alignedReads.get(gene)));

        if (processNegativeStrand) {
            seq = new String(reverseString);

            arr = new LongReadPositionArray[MAX_LENGTH - MIN_LENGTH + 1];
            for (int z = 0; z < MAX_LENGTH - MIN_LENGTH + 1; z++) {
                arr[z] = new LongReadPositionArray();

            }

            for (int i = 0; i < seq.length() - MAX_LENGTH; i++) {
                for (int x = 0; x < arr.length; x++) {
                    LongReadFragment g;
                    try {
                        g = new LongReadFragment(seq.substring(i, i + MIN_LENGTH + x));
                        arr[x].add(g.sequence, i);
                    } catch (SequenceContainsInvalidBaseException ex) {
                        arr[x].add(-1, i);
                    }

                }

            }

            for (int i = 0; i < arr.length - 1; i++) {
                if (seq.length() - MAX_LENGTH < 0) {
                    for (int j = 0; j <= seq.length() - MIN_LENGTH - i; j++) {
                        LongReadFragment g;
                        try {
                            g = new LongReadFragment(seq.substring(j, j + MIN_LENGTH + i));
                            arr[i].add(g.sequence, j);
                        } catch (SequenceContainsInvalidBaseException ex) {
                            arr[i].add(-1, j);

                        }

                    }
                } else {
                    for (int j = seq.length() - MAX_LENGTH; j <= seq.length() - MIN_LENGTH - i; j++) {
                        LongReadFragment g;
                        try {
                            g = new LongReadFragment(seq.substring(j, j + MIN_LENGTH + i));
                            arr[i].add(g.sequence, j);
                        } catch (SequenceContainsInvalidBaseException ex) {
                            //handle later
                            arr[i].add(-1, j);
                        }

                    }
                }
            }

            executor.execute(new Aligner(gene, arr, alignedReads.get(gene), true));
        }
    }

    private void print() {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter("alignment_results\\exactmatcher_results_all.txt", "UTF-8");
            for (String s : alignedReads.keySet()) {

                List<ShortReadAlignment> l = alignedReads.get(s);

                if (!l.isEmpty()) {

                    for (ShortReadAlignment alignment : l) {
                        if (alignment.getStrand().equals("+")) {
                            writer.write(s + " " + alignment.tag.toString() + "(" + alignment.tag.abundance + ") " + alignment.genePosition + "-" + (alignment.genePosition + alignment.tag.stringSeq.length() - 1) + " " + alignment.getStrand() + System.getProperty("line.separator"));
                        } else {
                            writer.write(s + " " + alignment.tag.toString() + "(" + alignment.tag.abundance + ") " + (alignment.genePosition + alignment.tag.stringSeq.length() - 1) + "-" + alignment.genePosition + " " + alignment.getStrand() + System.getProperty("line.separator"));

                        }
                    }
                }

            }
        } catch (IOException e) {
            // do something
        } finally {
            writer.close();
        }
    }

    private static Character reverseComplement(Character c) {

        switch (c) {
            case 'A':
                return 'T';
            case 'T':
                return 'A';
            case 'C':
                return 'G';
            case 'G':
                return 'C';
            case 'U':
                return 'A';
            default:
                return c;
        }

    }

    private void initialise() {
        reads = new ShortReadArray[MAX_LENGTH - MIN_LENGTH + 1];
        for (int i = 0; i < reads.length; i++) {
            reads[i] = new ShortReadArray();
        }

        executor = Executors.newFixedThreadPool(numThreads);
        alignedReads = new HashMap();

    }

    private void removeEmptyLists() {

        Iterator<String> it = alignedReads.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            if (alignedReads.get(key).isEmpty()) {
                it.remove();
            }
        }
    }

    private class Aligner implements Runnable {

        String gene;
        LongReadPositionArray[] transcriptSeqs;
        List<ShortReadAlignment> list;
        boolean negativeStrand;

        public Aligner(String g, LongReadPositionArray[] a, List l) {
            gene = g;
            transcriptSeqs = a;
            list = l;
            negativeStrand = false;
        }

        public Aligner(String g, LongReadPositionArray[] a, List l, boolean negative) {
            this(g, a, l);
            negativeStrand = negative;
        }

        @Override
        public void run() {

            incrementJobs(1);
            for (int i = 0; i < transcriptSeqs[MAX_LENGTH - MIN_LENGTH].currentPos; i++) {
                for (int j = 0; j < MAX_LENGTH - MIN_LENGTH + 1; j++) {
                    try {
                        ShortRead t = reads[j].contains(transcriptSeqs[j].arr[i]);
                        if (t != null) {
                            list.add(new ShortReadAlignment(t, transcriptSeqs[j].positions[i], negativeStrand));
                        }
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            }

            for (int i = 0; i < MAX_LENGTH - MIN_LENGTH; i++) {
                for (int j = transcriptSeqs[MAX_LENGTH - MIN_LENGTH].currentPos; j < transcriptSeqs[i].currentPos; j++) {
                    ShortRead t = reads[i].contains(transcriptSeqs[i].arr[j]);
                    if (t != null) {
                        list.add(new ShortReadAlignment(t, transcriptSeqs[i].positions[j], negativeStrand));
                    }
                }
            }

            incrementJobs(-1);
        }

    }

    private class ShortReadArray {

        ShortRead[] arr;
        int size;
        int currentPos;

        public ShortReadArray() {
            size = 1000;
            arr = new ShortRead[(int) size];
            currentPos = 0;
        }

        public void add(ShortRead i) {
            if (currentPos < size) {
                arr[currentPos] = i;
                currentPos++;
            } else {
                resize();
                arr[currentPos] = i;
                currentPos++;
            }

        }

        private void resize() {
            size = size * 2;
            currentPos = 0;
            ShortRead[] temp = new ShortRead[size];

            for (int i = 0; i < arr.length; i++) {
                temp[i] = arr[i];
                currentPos++;
            }

            arr = temp;
        }

        public ShortRead contains(long key) {

            int low = 0;
            int high = currentPos - 1;

            while (high >= low) {
                int middle = (low + high) / 2;
                if (arr[middle].sequence == key) {
                    return arr[middle];
                }
                if (arr[middle].sequence < key) {
                    low = middle + 1;
                }
                if (arr[middle].sequence > key) {
                    high = middle - 1;
                }
            }
            return null;
        }

        public void sort() {

            size = currentPos;
            currentPos = 0;
            ShortRead[] temp = new ShortRead[(int) size];

            for (int i = 0; i < size; i++) {
                temp[i] = arr[i];
                currentPos++;
            }

            arr = temp;
            Arrays.sort(arr);
        }

    }

    public class LongReadPositionArray {

        long[] arr;
        //dont need to use this if i am process each gene in full. ONLY FOR SPLITTING IT UP!!
        int[] positions;
        double size;
        int currentPos;

        public LongReadPositionArray() {
            size = 100;
            arr = new long[(int) size];
            positions = new int[(int) size];
            currentPos = 0;
        }

        public void add(long i, int position) {
            if (currentPos < (int) size) {
                arr[currentPos] = i;
                positions[currentPos] = position;
                currentPos++;
            } else {
                resize();
                //System.out.println("resizing");
                arr[currentPos] = i;
                positions[currentPos] = position;
                currentPos++;
            }

        }

        private void resize() {
            size = size * 1.5;
            currentPos = 0;
            long[] temp = new long[(int) size];
            int[] tempPositions = new int[(int) size];

            for (int i = 0; i < arr.length; i++) {
                temp[i] = arr[i];
                tempPositions[i] = positions[i];
                currentPos++;
            }

            arr = temp;
            positions = tempPositions;
        }
    }

}
