/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.degradomestatistics.BinomialDistribution;

/**
 *
 * @author Josh
 */
public class Worker implements Runnable {

    //            //CleaveLand version with my modification
//            BinomialDistribution b = new BinomialDistribution(scoreTable.get(ap.score).num, Engine.getCalculatedCategoryPercentagePerTranscript(ap.controller.messengerRNA.geneName, ap.controller.messengerRNA.category));
    SmallRNA smallRNA;
    double perfect_mfe;
    List<AlignmentPath> validPaths;
    private static volatile int numberCompleted;
    private static volatile int totalSmallRNAs;
    RuleSet ruleset = null;
    private static volatile int previousPercent;
    Pattern pattern;
    Process process;
    static String plexPath;
    private static int numRunning;
    Paresnip2Configuration config;
    TreeMap<Double, NumberOfAlignments> scoreTable;

    Engine engine;

    public static int getNumRunning() {
        return numRunning;
    }

    public static void setTotal(int i) {
        totalSmallRNAs = i;
        numberCompleted = 0;
        previousPercent = 0;
    }

    static synchronized void incremementNumRunning() {
        numRunning++;
    }

    static synchronized void decremementNumRunning() {
        numRunning--;
    }

    static synchronized void incrementCounter() {
        numberCompleted++;
        if (AppUtils.INSTANCE.isRunningInIDE()) {
            int tmp = (int) (((double) numberCompleted / (double) totalSmallRNAs) * 100);

            if (tmp > previousPercent) {
                previousPercent = tmp;
                Engine.updateProgressPercentage(tmp);
            }
        } else if (Paresnip2Configuration.getInstance().isVerbose()) {
            System.out.println("Completed: " + numberCompleted + " out of " + totalSmallRNAs + "(" + (int) (((double) numberCompleted / (double) totalSmallRNAs) * 100) + "%)");
        }

        //System.out.println("Completed: " + numberCompleted + " out of " + totalSmallRNAs + "(" + (int) (((double) numberCompleted / (double) totalSmallRNAs) * 100) + "%)");
    }

    public Worker(SmallRNA smallRNA, Engine engine, RuleSet rs) {
        this.ruleset = rs;
        this.smallRNA = smallRNA;
        this.validPaths = new ArrayList();
        this.engine = engine;
        config = Paresnip2Configuration.getInstance();
        scoreTable = new TreeMap();

        if (plexPath == null) {
            String binariesDirectory = AppUtils.INSTANCE.getArchitecture().getBinariesDirectory();
            String path = Tools.EXE_DIR + DIR_SEPARATOR + binariesDirectory + DIR_SEPARATOR + "RNAplex" + AppUtils.INSTANCE.getArchitecture().getExeExtension();

            path = AppUtils.addQuotesIfNecessary(path);
            plexPath = path;

        }
        pattern = Pattern.compile("\\(([\\s\\-\\d\\.]+)\\)");

    }

    @Override
    public void run() {
        try {

            incremementNumRunning();
            AlignmentController controller;
            int seedRegionNum = smallRNA.getRegion1to7();
            int middleRegionNum = smallRNA.getRegion7to13();
            int tailRegionNum = smallRNA.getRegionTail();

            BitSet bs = MessengerRNA.getPotentialGroups7to13()[middleRegionNum];

            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                for (MessengerRNA messengerRNA : MessengerRNA.getCluster7to13().get(i)) {

                    if (config.getAllowedCategories(messengerRNA.getCategory())) {

                        boolean containsSeed = MessengerRNA.getPotentialGroups1to7()[seedRegionNum].get(messengerRNA.getRegionTarget1to7());
                        boolean containsTail = MessengerRNA.getPotentialGroupsTail()[tailRegionNum].get(messengerRNA.getRegionTargetTail());

                        if (containsSeed && containsTail) {
                            controller = new AlignmentController(smallRNA, messengerRNA, ruleset);

                            ArrayList<AlignmentPath> path = controller.align();

                            if (!path.isEmpty()) {
                                validPaths.addAll(path);
                            }
                        }
                    }
                }
                if (i == Integer.MAX_VALUE) {
                    break; // or (i+1) would overflow
                }
            }

            if (!validPaths.isEmpty()) {

                if (config.isUseFilter()) {
                    List<String> results = new ArrayList<>();
                    if (validPaths.size() <= 30) {
                        executeQueries(validPaths, true);
                        results.addAll(readQueryOutput());
                    } else {

                        int i = 0;
                        for (; i + 30 < validPaths.size(); i += 30) {
                            executeQueries(validPaths.subList(i, i + 30), false);
                            results.addAll(readQueryOutput());
                        }

                        executeQueries(validPaths.subList(i, validPaths.size()), true);
                        results.addAll(readQueryOutput());

                    }

                    Iterator<AlignmentPath> it = validPaths.iterator();

                    perfect_mfe = getMFE(results.get(results.size() - 1));
                    smallRNA.setPerfect_mfe(perfect_mfe);

                    int count = 0;
                    while (it.hasNext()) {
                        AlignmentPath ap = it.next();

                        double energy = getMFE(results.get(count));
                        double ratio = energy / perfect_mfe;

                        //At times the perfect MFE is less than the 
                        //Alignment MFE
                        if (ratio > 1) {
                            ratio = 1;
                        }

                        ap.setFreeEnergy(energy);
                        ap.setFreeEnergyRatio(ratio);
                        //System.out.println(smallRNA + ": MFE: " + ratio);
                        if (ratio < config.getFilterCutoff()) {
                            it.remove();
                        }

                        count++;
                    }
                }

                Collections.sort(validPaths, new Comparator<AlignmentPath>() {
                    @Override
                    public int compare(AlignmentPath o1, AlignmentPath o2) {

                        if (o1.controller.messengerRNA.geneName.equals(o2.controller.messengerRNA.geneName)) {
                            return Integer.compare(o1.controller.messengerRNA.transcriptPosition, o2.controller.messengerRNA.transcriptPosition);
                        }

                        return o1.controller.messengerRNA.geneName.compareTo(o2.controller.messengerRNA.geneName);

                    }
                });

                AlignmentPath prev = null;
                AlignmentPath curr = null;

                Iterator<AlignmentPath> it = validPaths.iterator();
                ArrayList<AlignmentPath> processedPaths = new ArrayList();

                while (it.hasNext()) {
                    if (prev == null) {
                        prev = it.next();
                        processedPaths.add(prev);
                    } else {
                        curr = it.next();

                        if (curr.controller.messengerRNA.geneName.equals(prev.controller.messengerRNA.geneName)) {
                            if (curr.controller.messengerRNA.transcriptPosition == prev.controller.messengerRNA.transcriptPosition) {

                                if (config.isUsePValue()) {
                                    if (curr.score < prev.score) {
                                        processedPaths.remove(processedPaths.size() - 1);
                                        processedPaths.add(curr);
                                    }
                                } else if (config.isUseFilter()) {
                                    if (curr.freeEnergyRatio > prev.freeEnergyRatio) {
                                        processedPaths.remove(processedPaths.size() - 1);
                                        processedPaths.add(curr);
                                    }
                                } else if (curr.score < prev.score) {
                                    processedPaths.remove(processedPaths.size() - 1);
                                    processedPaths.add(curr);
                                }

                            } else {
                                processedPaths.add(curr);
                                prev = curr;
                            }
                        } else {
                            processedPaths.add(curr);
                            prev = curr;

                        }

                    }
                }

                validPaths = processedPaths;

                for (AlignmentPath path : validPaths) {
                    if (scoreTable.containsKey(path.score)) {
                        scoreTable.get(path.score).incrementNumber();
                    } else {
                        scoreTable.put(path.score, new NumberOfAlignments());
                    }
                }

                if (config.isUsePValue()) {
                    NumberOfAlignments previous = null;
                    for (NumberOfAlignments n : scoreTable.values()) {
                        if (previous != null) {
                            n.cumulativeTotal = n.getNumber() + previous.cumulativeTotal;
                            previous = n;
                        } else {
                            n.cumulativeTotal = n.num;
                            previous = n;
                        }
                    }

                    it = validPaths.iterator();

                    while (it.hasNext()) {
                        AlignmentPath ap = it.next();

                        //CleaveLand version with my modification
                        BinomialDistribution b = new BinomialDistribution(scoreTable.get(ap.score).cumulativeTotal, Engine.getCalculatedCategoryPercentagePerTranscript(ap.controller.messengerRNA.geneName, ap.controller.messengerRNA.category));
                        //Cleaveland version
                        //BinomialDistribution b = new BinomialDistribution(scoreTable.get(ap.score).cumulativeTotal, Engine.getCategoryPercentages()[ap.controller.messengerRNA.category]);
                        double pval = 1 - b.cumulative(0);
                        ap.setpVal(pval);
                        //System.out.println(smallRNA + ": p-val: " + pval);
                        if (pval > config.getpValueCutoff()) {
                            it.remove();
                        }

                    }
                }

                engine.writeToFile(validPaths);

                //System.out.println("ending");
                //System.out.println("Total number: " + validPaths.size());
//
//        for (AlignmentController path : validPaths) {
//            System.out.println(path.smallRNA.comment.split(">")[1] + "," + path.messengerRNA.geneName.split("\\.")[0]);
//            //path.print();
//        }
            }

            decremementNumRunning();
            incrementCounter();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private AlignmentPath findValidMFE(AlignmentController controller) {

        //This method will search through all valid alignments for a given alignment path 
        //and will return the valid one (only use if the 'best' alignment isn't valid)
        return null;

    }

    private void executeQueries(List<AlignmentPath> paths, boolean end) {
        try {

            process = Runtime.getRuntime().exec(plexPath);
            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (AlignmentPath c : paths) {
                String query = c.sRNA_Characters.toString();
                String target = c.mRNA_Characters.toString();

                query = query.split("\'")[1];
                target = target.split("\'")[1];

                int i = 0;
                while (i < query.length() && query.charAt(i) == ' ') {
                    i++;
                }

                query = query.trim();
                target = target.substring(i, query.length() + i);

                target = SequenceUtils.DNA.reverse(target);

                dataOut.write(query);
                dataOut.newLine();
                dataOut.write(target);
                dataOut.newLine();
            }

            if (end) {
                //Calculate the perfect MFE and put it at the end of the results list
                String seq = paths.get(0).controller.smallRNA.getStringSeq();

                dataOut.write(seq);
                dataOut.newLine();
                dataOut.write(SequenceUtils.DNA.reverseComplement(seq));
                dataOut.newLine();
            }

            dataOut.write("@");
            dataOut.newLine();
            dataOut.flush();
        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }

        int returnCode;
        try {
            returnCode = process.waitFor();
            if (returnCode != 0) {
                System.out.println("Something went wrong with RNAplex. Return code: " + returnCode);
                System.out.println("Please check that your machine has all the dependencies required by RNAplex.");
                System.exit(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private List<String> readQueryOutput() {
        ArrayList<String> list = new ArrayList<>();

        BufferedReader dataIn = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        try {
            while ((line = dataIn.readLine()) != null) {
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }

            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
            process.destroy();
        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    private double getMFE(String result) {
        Matcher m = pattern.matcher(result);

        if (m.find()) {
            // if an occurrence if a pattern was found in a given string...
            return Double.parseDouble(m.group(1));
        }
        return 0;
    }

    private class NumberOfAlignments {

        int num;
        int cumulativeTotal;

        public NumberOfAlignments() {
            num = 1;
            cumulativeTotal = 0;
        }

        public void incrementNumber() {
            ++num;
        }

        public int getNumber() {
            return num;
        }

        public void setCumulative(int t) {
            cumulativeTotal = t;
        }

        public int getCumulative() {
            return cumulativeTotal;
        }
    }

}
