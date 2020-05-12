package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.ShortReadAlignment;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu/Joshua Thody
 */
public class AlignmentFinder {

    SmallRNA miRNA;
    String geneName;
    static HashMap<String, String> geneSequences;
    static boolean isSetUp;
    List<AlignmentPath> validPaths;
    static BufferedWriter writer;
    static File alignmentFile;
    static Pattern pattern;
    static Process process;
    static String plexPath;
    double perfect_mfe;
    HashMap<String, List<ShortReadAlignment>> alignedReads;

    public AlignmentFinder(SmallRNA miRNA, String geneName, HashMap<String, List<ShortReadAlignment>> alignments) {
        if (isSetUp) {
            this.miRNA = miRNA;
            this.geneName = geneName;
            RuleSet.getRuleSet().setParameterSearchRules();
            validPaths = new ArrayList();
            alignedReads = alignments;
        }

    }

    public AlignmentFinder(SmallRNA miRNA, String geneName) {
        if (isSetUp) {
            this.miRNA = miRNA;
            this.geneName = geneName;
            RuleSet.getRuleSet().setParameterSearchRules();
            validPaths = new ArrayList();
        }

    }

    private static void writeHeader() {

        try {

            writer = new BufferedWriter(new FileWriter(alignmentFile));

            writer.write("miRNA ID(s)");
            writer.write(",");
            writer.write("miRNA Sequence");
            writer.write(",");
            writer.write("Gene");
            writer.write(",");
            writer.write("Category");
            writer.write(",");
            writer.write("Predicted Cleavage Position");
            writer.write(",");
            writer.write("Abundance");
            writer.write(",");
            writer.write("Weighted Abundance");
            writer.write(",");
            writer.write("Predicted Duplex");
            writer.write(",");
            writer.write("Alignment Score");
            writer.write(",");
            writer.write("Duplex MFE");
            writer.write(",");
            writer.write("Perfect MFE");
            writer.write(",");
            writer.write("MFE Ratio");

            writer.newLine();
        } catch (Exception ex) {
            System.out.println(ex);
        } finally {

            try {
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(AlignmentFinder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void performAlignment() {
        if (!isSetUp) {
            validPaths = new ArrayList();
            return;
        }

        int miRNA_seq_length = miRNA.getStringSeq().length();
        String geneSeq = geneSequences.get(geneName);
        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        config.setAllowedCategories(2, false);
        config.setAllowedCategories(3, false);
        config.setAllowedCategories(4, false);

        if (geneSeq != null) {

            if (alignedReads.containsKey(geneName)) {

                List<ShortReadAlignment> list = alignedReads.get(geneName);

                Collections.sort(list);

                double average = 0.0;

                boolean singleHighestAbundance;

                int count = 0;
                //Average
                for (ShortReadAlignment a : list) {
                    if (a.getTag().getAbundance() != 1) {
                        if (config.isUseWeightedFragmentAbundance()) {
                            average += a.getTag().getWeightedPositiveAbundance();
                        } else {
                            average += a.getTag().getAbundance();
                        }
                        count++;
                    }
                }
                average /= (double) count;

                if (list.size() == 1) {
                    singleHighestAbundance = true;
                } else if (config.isUseWeightedFragmentAbundance()) {
                    singleHighestAbundance = list.get(list.size() - 1).getTag().getWeightedPositiveAbundance()
                            > list.get(list.size() - 2).getTag().getWeightedPositiveAbundance();
                } else {
                    singleHighestAbundance = list.get(list.size() - 1).getTag().getAbundance()
                            > list.get(list.size() - 2).getTag().getAbundance();
                }

                double largestAbundance;

                if (config.isUseWeightedFragmentAbundance()) {
                    largestAbundance = list.get(list.size() - 1).getTag().getWeightedPositiveAbundance();
                } else {
                    largestAbundance = list.get(list.size() - 1).getTag().getAbundance();
                }

                for (ShortReadAlignment s : list) {

                    int genePos = s.getGenePosition();
                    int abundance = s.getTag().getAbundance();
                    double weightedAbundance = s.getTag().getWeightedPositiveAbundance();
                    int category;
                    if (config.isUseWeightedFragmentAbundance()) {
                        if (weightedAbundance == largestAbundance && abundance != 1 && singleHighestAbundance) {
                            category = 0;
                        } else if (weightedAbundance == largestAbundance && abundance != 1) {
                            category = 1;
                        } else if (weightedAbundance > average && weightedAbundance < largestAbundance && abundance != 1) {
                            category = 2;
                        } else if (weightedAbundance <= average && abundance != 1) {
                            category = 3;
                        } else {
                            category = 4;
                        }
                    } else if (abundance == largestAbundance && abundance != 1 && singleHighestAbundance) {
                        category = 0;
                    } else if (abundance == largestAbundance && abundance != 1) {
                        category = 1;
                    } else if (abundance > average && abundance < largestAbundance && abundance != 1) {
                        category = 2;
                    } else if (abundance <= average && abundance != 1) {
                        category = 3;
                    } else {
                        category = 4;
                    }

                    if (Paresnip2Configuration.getInstance().getAllowedCategories(category)) {

                        if (genePos >= 17) {

                            String geneFragment = geneSeq.substring(genePos - 17, genePos + 15);
                            MessengerRNA mRNA = new MessengerRNA(geneFragment, geneName, true, 16);
                            mRNA.setCategory(category);
                            mRNA.setTranscriptPosition(genePos);
                            mRNA.setAbundance(abundance);
                            mRNA.setWrightedAbundance(weightedAbundance);
                            AlignmentController controller = new AlignmentController(miRNA, mRNA, RuleSet.getRuleSet());

                            ArrayList<AlignmentPath> best = controller.align();
                            if (best != null) {

                                validPaths.addAll(best);

                            }
                        }

                    }
                }

            }
        } else {
            System.out.println("Can't find gene seq");
        }

    }

    public static void setUp(File transcriptFile, File outputFile) {

        FileReader input;
        alignmentFile = outputFile;
        StringBuilder sb = new StringBuilder();
        String gene = null;
        geneSequences = new HashMap();

        try {

            input = new FileReader(transcriptFile);

            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while ((myLine = bufRead.readLine()) != null) {

                if (myLine.startsWith(">")) {
                    if (gene != null) {
                        String longRead = sb.toString();
                        geneSequences.put(gene, longRead);
                        //transcriptomeSize += longRead.length();
                        //Engine.populateTranscriptLengths(gene, longRead.length());
                        sb = new StringBuilder();
                    }

                    //gene = myLine.split(" ")[0].split(">")[1];
                    gene = myLine.substring(1, myLine.length());
                    gene = gene.split("\\.")[0];
                    //replace any , with a ;
                    //gene = gene.replace(',', ';');
                } else {
                    sb.append(myLine.trim());
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            String longRead = sb.toString();
            geneSequences.put(gene, longRead);
            isSetUp = true;
        }

        writeHeader();

        if (plexPath == null) {
            String binariesDirectory = AppUtils.INSTANCE.getArchitecture().getBinariesDirectory();
            String path = Tools.EXE_DIR + DIR_SEPARATOR + binariesDirectory + DIR_SEPARATOR + "RNAplex" + AppUtils.INSTANCE.getArchitecture().getExeExtension();

            path = AppUtils.addQuotesIfNecessary(path);
            plexPath = path;

        }
        pattern = Pattern.compile("\\(([\\s\\-\\d\\.]+)\\)");

    }

    public void findAlignments() {
        if (alignedReads != null) {
            performAlignment();
        } else {
            performSimpleAlignment();
        }
        if (!validPaths.isEmpty()) {
            Collections.sort(validPaths);
            calcMFERatio();
            writeToFile();
        }
    }

    private void calcMFERatio() {

        if (!validPaths.isEmpty()) {
            List<String> results = new ArrayList<>();
            //For some reasons works with <= 30 but nothing more??
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
            miRNA.setPerfect_mfe(perfect_mfe);

            int count = 0;
            while (it.hasNext()) {
                AlignmentPath ap = it.next();

                double energy = getMFE(results.get(count));
                double ratio = energy / perfect_mfe;

                ap.setFreeEnergy(energy);
                ap.setFreeEnergyRatio(ratio);

                count++;
            }
        }

        Collections.sort(validPaths, new Comparator<AlignmentPath>() {
            @Override
            public int compare(AlignmentPath o1, AlignmentPath o2) {

                if (o1.getScore() == o2.getScore()) {
                    return Double.compare(o1.getFreeEnergyRatio(), o2.getFreeEnergyRatio());
                }

                return Double.compare(o1.getScore(), o2.getScore());

            }
        });

        //What if more than one position has worse score than lowest but more MFE? Track this....
        //It can be filtered later when selecting the aligned tag with the highest abundance
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
                if (curr.getScore() == prev.getScore() && curr.getFreeEnergyRatio() > prev.getFreeEnergyRatio()) {
                    processedPaths.remove(prev);
                    processedPaths.add(curr);
                    prev = curr;
                }
            }

        }

        validPaths = processedPaths;

    }

    private void executeQueries(List<AlignmentPath> paths, boolean end) {
        try {

            process = Runtime.getRuntime().exec(plexPath);
            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (AlignmentPath c : paths) {
                String query = c.getsRNA_Characters().toString();
                String target = c.getmRNA_Characters().toString();

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
                String seq = paths.get(0).getController().getSmallRNA().getStringSeq();

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
                System.out.println("Something went wrong....");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
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
            // if an occurrence of a pattern was found in a given string...
            return Double.parseDouble(m.group(1));
        }
        return 1;

    }

    private void writeToFile() {

        double bestScore = Integer.MAX_VALUE;

        try {
            for (AlignmentPath p : validPaths) {

                writer.write(p.toCSVString());
                writer.newLine();

            }

            writer.flush();
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    private void performSimpleAlignment() {

        try {
            if (!isSetUp) {
                validPaths = new ArrayList();
                return;
            }

            int miRNA_seq_length = miRNA.getStringSeq().length();
            String geneSeq = geneSequences.get(geneName.replace("\"", ""));
            //Paresnip2Configuration config = Paresnip2Configuration.getInstance();

            for (int i = 17; i < geneSeq.length() - miRNA_seq_length - 1; i++) {

                String geneFragment = geneSeq.substring(i - 17, i + 15);
                MessengerRNA mRNA = new MessengerRNA(geneFragment, geneName, true, 16);
                mRNA.setTranscriptPosition(i);
                mRNA.setAbundance(1000);
                mRNA.setWrightedAbundance(1000);
                AlignmentController controller = new AlignmentController(miRNA, mRNA, RuleSet.getRuleSet());

                ArrayList<AlignmentPath> best = controller.align();
                if (!best.isEmpty()) {
                    validPaths.addAll(best);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
