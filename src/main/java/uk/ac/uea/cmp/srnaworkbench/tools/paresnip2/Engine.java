package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Josh
 */
public class Engine {

    private ExecutorService executor;
    private ExecutorService resultsWriterExecutor;
    public static int MIN_SRNA_SIZE = 19;
    public static int MAX_SRNA_SIZE = 24;
    public static int numThreads = 12;
    DegradomeTagAligner tagAligner = null;
    boolean allowCat0;
    boolean allowCat1;
    boolean allowCat2;
    boolean allowCat3;
    boolean allowCat4;
    int count;

    public Engine() {

        executor = Executors.newFixedThreadPool(numThreads);
        resultsWriterExecutor = Executors.newFixedThreadPool(1);
        RuleSet rs = new RuleSet();
        ResultWriter.initialise("results.csv");
        allowCat0 = true;
        allowCat1 = true;
        allowCat2 = true;
        allowCat3 = true;
        allowCat4 = false;
        count = 0;

        System.out.println("Aligning tags to transcript");
        //tagAligner = new DegradomeTagAligner(numThreads, "/scratch/transcript.fa", "/scratch/C0LYoung_leaf.fa");

        tagAligner = new DegradomeTagAligner(numThreads, "/scratch/transcript.fa", "/scratch/C0LYoung_leaf.fa");
        System.out.println("Building smallRNA similaritiy tables and reading small RNAs..");
        readSmallRNAs();
        System.out.println("Finished that...");
        processTags();

        shutdown();
    }

    private void testMethod() {

        AlignmentPathController apc = new AlignmentPathController();

        try {
            SmallRNA sRNA = new SmallRNA("AACCGGGACTTTTAATTTGA");
            TranscriptFragment mRNA = new TranscriptFragment("AACCCAGTTCATAAGAAAATCCCGGTTC", "Test", 10);

//            SmallRNA sRNA = new SmallRNA("TTAAAAAAAAAAAAAAAAGA");
//            TranscriptFragment mRNA = new TranscriptFragment("AGTCTTGGCCCTAAAAGAATACTTGACCCAA", "Test", 13);
            if (SmallRNA.getPotentialGroup1to7().get(sRNA.getRegion1to7()).contains(mRNA.getTargetRegion1to7())) {
                System.out.println(SmallRNA.longToString(sRNA.getRegion1to7(), 7));
                System.out.println(TranscriptFragment.longToString(mRNA.getTargetRegion1to7(), 7));
                System.out.println("Yep!");
            }

            if (SmallRNA.getPotentialGroups7to13().get(sRNA.getRegion7to13()).contains(mRNA.getTargetRegion7to13())) {
                System.out.println(SmallRNA.longToString(sRNA.getRegion7to13(), 7));
                System.out.println(TranscriptFragment.longToString(mRNA.getTargetRegion7to13(), 7));
                System.out.println("Yep!");
            }

            if (SmallRNA.getPotentialGroupTail().get(sRNA.getTailRegion()).contains(mRNA.getTargetTailRegion())) {
                System.out.println(SmallRNA.longToString(sRNA.getTailRegion(), 7));
                System.out.println(TranscriptFragment.longToString(mRNA.getTargetTailRegion(), 7));
                System.out.println("Yep!");
            }

            System.out.println("Doing test alignment...");
            Alignment alignment = apc.align(sRNA, mRNA);

            if (alignment != null) {
                //do something with it
            } else {
                System.out.println("NO ALIGNMENT FOUND");
            }
        } catch (Exception e) {
            System.out.println(e);

        }
    }

    private void shutdown() {
//
        executor.shutdown();
        resultsWriterExecutor.shutdown();
        while (!executor.isTerminated() || !resultsWriterExecutor.isTerminated()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ResultWriter.close();
    }

    private void readSmallRNAs() {

        String line;

        try (BufferedReader br = new BufferedReader(new FileReader("/scratch/seqs.fa"))) {

            while ((line = br.readLine()) != null) {
                if (!line.startsWith(">")) {
                    Sequence srna = new SmallRNA(line);
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    private void processTags() {

        HashMap<String, List<List<TranscriptFragment>>> map = tagAligner.getCategories();
        HashMap<String, List<List<TranscriptFragment>>> mapWithInvalid = tagAligner.getCategoriesWithInvalidBases();

        for (String gene : map.keySet()) {

            count++;
            System.out.println(count);

            if (allowCat0) {
                for (TranscriptFragment tag : map.get(gene).get(0)) {
                    List<Alignment> foundAlignments = findPotentialSmallRNAs(tag);
                    //resultsWriterExecutor.execute(new ResultWriter(foundAlignments));
                }
            }

            //System.out.println("Category 0: " + map.get(gene).get(0).size() + " DONE!");
            if (allowCat1) {
                for (TranscriptFragment tag : map.get(gene).get(1)) {
                    List<Alignment> foundAlignments = findPotentialSmallRNAs(tag);
                    //resultsWriterExecutor.execute(new ResultWriter(foundAlignments));
                }
            }

            //System.out.println("Category 1: " + map.get(gene).get(1).size() + " DONE!");
            if (allowCat2) {
                for (TranscriptFragment tag : map.get(gene).get(2)) {
                    List<Alignment> foundAlignments = findPotentialSmallRNAs(tag);
                    //resultsWriterExecutor.execute(new ResultWriter(foundAlignments));
                }
            }

            //System.out.println("Category 2: " + map.get(gene).get(2).size() + " DONE!");
            if (allowCat3) {
                for (TranscriptFragment tag : map.get(gene).get(3)) {
                    List<Alignment> foundAlignments = findPotentialSmallRNAs(tag);
                    // resultsWriterExecutor.execute(new ResultWriter(foundAlignments));
                }
            }

            //System.out.println("Category 3: " + map.get(gene).get(3).size() + " DONE!");
            if (allowCat4) {
                for (TranscriptFragment tag : map.get(gene).get(4)) {
                    List<Alignment> foundAlignments = findPotentialSmallRNAs(tag);
                    // resultsWriterExecutor.execute(new ResultWriter(foundAlignments));
                }
            }

            //System.out.println("Category 4: " + map.get(gene).get(4).size() + " DONE!");
        }

    }

    private List<Alignment> findPotentialSmallRNAs(TranscriptFragment mRNA) {
        List<Alignment> list = Collections.synchronizedList(new ArrayList());

        //CountDownLatch latch = new CountDownLatch(SmallRNA.getPotentialGroups7to13().get(mRNA.getTargetRegion7to13()).currentPos);
        CountDownLatch latch = new CountDownLatch(3000);

//System.out.println(latch.getCount());
        for (int i = 0; i < 3000; i++) {
            int index = SmallRNA.getPotentialGroups7to13().get(mRNA.getTargetRegion7to13()).arr[i];

            if (!SmallRNA.getCluster7to13().get(index).isEmpty()) {
                //executor.execute(new Worker(SmallRNA.getCluster7to13().get(index), mRNA, list, latch));
                latch.countDown();
            } else {
                latch.countDown();
            }

        }
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
    }

}
