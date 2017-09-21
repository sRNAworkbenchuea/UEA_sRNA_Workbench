package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  REALLY NEED TO DO AN ON DISK MERGE SORT FOR FASTA FILES!!!!
 * @author Josh
 */
public class Engine {

    private HashMap<SmallRNA, Integer> smallRNAs;
    private ExecutorService executor;
    private ExecutorService resultsWriterExecutor;
    public static int MIN_SRNA_SIZE = 19;
    public static int MAX_SRNA_SIZE = 24;
    public static int MIN_SRNA_ABUNDANCE = 1;
    public static int numThreads = 12;
    DegradomeTagAligner tagAligner = null;
    public static boolean[] allowedCategories;

    public Engine() {

        executor = Executors.newFixedThreadPool(numThreads);
        resultsWriterExecutor = Executors.newFixedThreadPool(1);
        RuleSet rs = new RuleSet();
        smallRNAs = new HashMap();
        //ResultWriter.initialise("results.csv");
        allowedCategories = new boolean[5];
        allowedCategories[0] = true;
        allowedCategories[1] = true;
        allowedCategories[2] = true;
        allowedCategories[3] = true;
        allowedCategories[4] = false;

        System.out.println("Aligning tags to transcript");
        tagAligner = new DegradomeTagAligner(numThreads, "/scratch/transcript.fa", "/scratch/C0LYoung_leaf.fa");

        //tagAligner = new DegradomeTagAligner(numThreads, "transcript_small.fasta", "small_pare.fa");
        System.out.println("Building smallRNA similaritiy tables and reading small RNAs..");
        readSmallRNAs();
        System.out.println("Finished that...");
        process();

        shutdown();
    }

    private void testMethod() {

//        AlignmentPathController apc = new AlignmentPathController();
//
//        try {
//            SmallRNA sRNA = new SmallRNA("AACCGGGACTTTTAATTTGA");
//            TranscriptFragment mRNA = new TranscriptFragment("AACCCAGTTCATAAGAAAATCCCGGTTC", "Test", 10);
//
////            SmallRNA sRNA = new SmallRNA("TTAAAAAAAAAAAAAAAAGA");
////            TranscriptFragment mRNA = new TranscriptFragment("AGTCTTGGCCCTAAAAGAATACTTGACCCAA", "Test", 13);
//            if (SmallRNA.getPotentialGroup1to7().get(sRNA.getRegion1to7()).contains(mRNA.getTargetRegion1to7())) {
//                System.out.println(SmallRNA.longToString(sRNA.getRegion1to7(), 7));
//                System.out.println(TranscriptFragment.longToString(mRNA.getTargetRegion1to7(), 7));
//                System.out.println("Yep!");
//            }
//
//            if (SmallRNA.getPotentialGroups7to13().get(sRNA.getRegion7to13()).contains(mRNA.getTargetRegion7to13())) {
//                System.out.println(SmallRNA.longToString(sRNA.getRegion7to13(), 7));
//                System.out.println(TranscriptFragment.longToString(mRNA.getTargetRegion7to13(), 7));
//                System.out.println("Yep!");
//            }
//
//            if (SmallRNA.getPotentialGroupTail().get(sRNA.getTailRegion()).contains(mRNA.getTargetTailRegion())) {
//                System.out.println(SmallRNA.longToString(sRNA.getTailRegion(), 7));
//                System.out.println(TranscriptFragment.longToString(mRNA.getTargetTailRegion(), 7));
//                System.out.println("Yep!");
//            }
//
//            System.out.println("Doing test alignment...");
//            Alignment alignment = apc.align(sRNA, mRNA);
//
//            if (alignment != null) {
//                //do something with it
//            } else {
//                System.out.println("NO ALIGNMENT FOUND");
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//
//        }
    }

    private void shutdown() {
        executor.shutdown();
        resultsWriterExecutor.shutdown();
        while (!executor.isTerminated() || !resultsWriterExecutor.isTerminated()) {
            try {
                Thread.sleep(1);

            } catch (InterruptedException ex) {
                Logger.getLogger(Engine.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        // ResultWriter.close();
    }

    private void readSmallRNAs() {

        String line;
        String commentLine = null;

       // try (BufferedReader br = new BufferedReader(new FileReader("/scratch/Tools/Workbench/Workbench_100_seq/seqs.fa"))) {
        try (BufferedReader br = new BufferedReader(new FileReader("/scratch/sequences/seqs_1000000.fa"))) {

            while ((line = br.readLine()) != null) {

                if (!line.startsWith(">")) {
                    SmallRNA srna = new SmallRNA(line);
                    srna.setComment(commentLine);

                    if (smallRNAs.containsKey(srna)) {
                        smallRNAs.put(srna, smallRNAs.get(srna) + 1);
                    } else {
                        smallRNAs.put(srna, 1);
                    }

                } else {
                    commentLine = line;
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    private void process() {

        Worker.setTotal(smallRNAs.size());
        
        for (HashMap.Entry<SmallRNA, Integer> entry : smallRNAs.entrySet()) {
            SmallRNA key = entry.getKey();
            Integer value = entry.getValue();
            key.abundance = value;
            
            if(value >= MIN_SRNA_ABUNDANCE)
            {
              executor.execute(new Worker(key));
            }
            
        }

    }
    
    
}
