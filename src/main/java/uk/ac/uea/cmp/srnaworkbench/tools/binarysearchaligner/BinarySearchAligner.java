package uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.exceptions.SequenceContainsInvalidBaseException;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;

/**
 *
 * @author rew13hpu
 */
public class BinarySearchAligner extends RunnableTool {

    private final static String TOOL_NAME = "SHORT READ ALIGNER";
    private int minLength;
    private int maxLength;
    private int size;
    private int chunkSize;
    protected ConcurrentHashMap<String, Set<ShortReadAlignment>> alignedReads;
    protected HashMap<String, ShortRead> shortReads;
    private ShortReadArray[] reads;
    private CircularArray[] arr;
    private boolean finishedAll;
    boolean completed;
    private String currentGene;
    protected double totalReads;
    ArrayList<Thread> threadList;
    private ExecutorService executor;
    private boolean isAlignNegative;

    protected File shortReadFile;
    protected File longReadFile;
    protected File outFile;

    public BinarySearchAligner(File shortReads, File longReads, int minLength, int maxLength, File outputDir, int numThreads, boolean deleteOnExit) {
        this(shortReads, longReads, minLength, maxLength, outputDir, numThreads);

        if (deleteOnExit) {
            outFile.deleteOnExit();
        }
    }

    public ConcurrentHashMap<String, Set<ShortReadAlignment>> getAlignedReads() {
        return alignedReads;
    }

    public BinarySearchAligner(File shortReads, File longReads, int minLength, int maxLength, File outputDir, int numThreads) {
        super(TOOL_NAME, null);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.size = this.maxLength - this.minLength + 1;
        this.shortReadFile = shortReads;
        this.longReadFile = longReads;
        finishedAll = false;
        alignedReads = new ConcurrentHashMap();
        arr = new CircularArray[size];
        chunkSize = 50000;
        executor = Executors.newFixedThreadPool(numThreads);
        outFile = new File(outputDir.getAbsoluteFile() + File.separator + shortReads.getName().split("\\.")[0]
                + "_" + longReads.getName().split("\\.")[0] + ".align");
        try {
            if (outFile.exists()) {
                outFile.delete();
            }

            outFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(BinarySearchAligner.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Create the resizable arrays to store the short reads
        reads = new ShortReadArray[this.maxLength - this.minLength + 1];
        for (int i = 0; i < reads.length; i++) {
            reads[i] = new ShortReadArray();
        }

        totalReads = 0;
    }

    public boolean isAlignNegative() {
        return isAlignNegative;
    }

    public void setAlignNegative(boolean b) {
        isAlignNegative = b;
    }

    private void processShortSequences() {

        shortReads = new HashMap();
        FileReader input;
        //Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        try {
            input = new FileReader(shortReadFile);
            ShortRead prev = null;

            BufferedReader bufRead = new BufferedReader(input, 4096);
            String seq;
            int abundance = 0;
            //Need to find a better way to do this! HASH MAPS ARE NOT THE WAY!!!
            while ((seq = bufRead.readLine()) != null) {
                if (!seq.startsWith(">")) {
                    //redundant check as it's done during NR conversion
                    //if (seq.length() >= minLength && seq.length() <= maxLength) {
                    try {
                        ShortRead sequence;// = new ShortRead(seq);
                        //This will need to be changed when I change the input
                        //to non-redundant fasta or fastq format
                        if (!shortReads.containsKey(seq)) {
                            sequence = new ShortRead(seq);
                            sequence.setAbundance(abundance);
                            shortReads.put(sequence.toString(), sequence);
                        } else {
                            System.out.println("Something went wrong with NR converter...");
                        }
                        
                    } catch (SequenceContainsInvalidBaseException ex) {
                        //handle later
                    }

                } else {
                    abundance = Integer.parseInt(seq.split("\\(")[1].split("\\)")[0]);
                    totalReads += abundance;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        //Iterate over and update the abundance
        Iterator<String> it = shortReads.keySet().iterator();

        while (it.hasNext()) {
            ShortRead shortRead = shortReads.get(it.next());
            reads[Math.abs(minLength - shortRead.toString().length())].add(shortRead);
            it.remove();
        }
        //System.out.println("Finished reading short reads, now sorting them...");
        for (ShortReadArray t : reads) {
            t.sort();
        }
        //System.out.println("Finished sorting...");

    }

    private void processLongReads() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(longReadFile), 4096);
            int c;
            StringBuilder name = null;
            StringBuilder seq = null;
            boolean foundEnd = false;

            int currentPosition;
            while ((c = br.read()) != -1) {
                name = new StringBuilder();
                seq = new StringBuilder(chunkSize);
                if (c == '>' || foundEnd) {

                    foundEnd = false;

                    if (c != '>') {
                        name.append((char) c);
                    }

                    while ((c = br.read()) != 10) {
                        name.append((char) c);
                    }
                    currentGene = name.toString();
                    currentGene = currentGene.replace("\r", "");
                    alignedReads.put(currentGene, ConcurrentHashMap.newKeySet());
                } else {
                    //System.out.println(currentGene);
                    currentPosition = 1;
                    seq.append((char) c);

                    while (!foundEnd) {
                        while (seq.length() < chunkSize && (c = br.read()) != '>' && c != -1) {
                            if (c != 10) {
                                seq.append((char) c);
                                currentPosition++;
                            }
                        }

                        if (c == -1) {
                            foundEnd = true;

                        }
                        if (c == '>') {
                            foundEnd = true;

                            //process string
                            processChunk(currentGene, seq.toString(), foundEnd, currentPosition);
                            //System.out.println("Complete: " + currentGene);
                        } else {
                            //process string
                            processChunk(currentGene, seq.toString(), foundEnd, currentPosition);

                            //System.out.println(seq.toString());
                            seq = new StringBuilder(seq.substring(seq.length() - maxLength, seq.length()));

                        }

                    }

                    //System.out.println("");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                //handle   
            }
        }
    }

    private void processChunk(String geneName, String sequence, boolean end, int position) {
        executor.execute(new Worker(geneName, sequence, position, end));
    }

    private synchronized void writeToFile(String name) {
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, true));

            ArrayList<ShortReadAlignment> list = new ArrayList(alignedReads.get(name));
            Collections.sort(list, (ShortReadAlignment o1, ShortReadAlignment o2) -> Integer.compare(o1.genePosition, o2.genePosition));
            for (ShortReadAlignment alignment : list) {
                writer.append(name + "\t" + alignment.tag.toString() + "(" + alignment.tag.abundance + ")\t" + alignment.genePosition + "\t" + (alignment.genePosition + alignment.tag.length - 1) + "\t" + alignment.getStrand() + System.getProperty("line.separator"));
            }
            writer.flush();

            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        alignedReads.get(name).clear();
    }

    private void waitForExecutorFinish() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
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

    @Override
    protected void process() throws Exception {

        //System.out.println("Reading short reads");
        processShortSequences();
        //System.out.println("Processing long reads");
        processLongReads();
        waitForExecutorFinish();

        removeEmptyLists();

    }

    public File getOutputFile() {
        return outFile;
    }

    public boolean getCompleted() {
        return completed;

    }

    public void setDeleteOnExit(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    private class Worker implements Runnable {

        private CircularArray[] arr;
        private String sequence;
        private int startPos;
        private String geneName;
        private boolean end;

        public Worker(String gene, String seq, int pos, boolean end) {
            this.sequence = seq;
            this.startPos = pos;
            this.end = end;
            this.geneName = gene;
            arr = new CircularArray[maxLength - minLength + 1];

            for (int i = 0; i < arr.length; i++) {
                arr[i] = new CircularArray(minLength + i, pos - seq.length() + 1);
            }

        }

        @Override
        public void run() {

            for (int i = 0; i < sequence.length(); i++) {
                for (int j = 0; j < maxLength - minLength + 1; j++) {
                    arr[j].add(sequence.charAt(i));
                    if (arr[j].currentEndPositive == minLength + j) {
                        doBinarySearch(arr[j], minLength + j);
                    }
                }
            }

            if (end) {
                writeToFile(geneName);
            }

        }

        private void doBinarySearch(CircularArray c, int length) {

            long l = c.toLong();

            if (l == -1) {
                return;
            }

            ShortRead shortRead = reads[length - minLength].contains(l);

            if (shortRead != null) {
                //shortRead.incrementNumAlignmentsPositive();
                ShortReadAlignment a = new ShortReadAlignment(shortRead, arr[length - minLength].getLongReadPositionPositive());
                if (!alignedReads.get(geneName).contains(a)) {
                    alignedReads.get(geneName).add(a);
                    shortRead.incrementNumAlignmentsPositive();
                }

            }

            if (isAlignNegative()) {
                l = c.toLongReverseComplement();

                if (l == -1) {
                    return;
                }

                shortRead = reads[length - minLength].contains(l);

                if (shortRead != null) {
                    //shortRead.incrementNumAlignmentsPositive();
                    ShortReadAlignment a = new ShortReadAlignment(shortRead, arr[length - minLength].getLongReadPositionNegative(), true);
                    a.negativeStrand = true;
                    if (!alignedReads.get(geneName).contains(a)) {
                        alignedReads.get(geneName).add(a);
                        shortRead.incrementNumAlignmentsNegative();
                    }

                }
            }

        }

    }

}
