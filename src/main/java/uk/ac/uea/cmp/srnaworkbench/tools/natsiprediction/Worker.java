/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author Joshua Thody
 */
public class Worker implements Runnable {

    //Implement some way of keeping track of how many have been completed
    //num to determine output file names
    private static Map<String, GeneAnnotation> geneAnnotationMap;
    static int staticWorkerNum = 1;
    int currentWorkerNum;

    double mfeRatio;

    //Directories of BLASTn and RNAplex
    private static String plexPath;
    private static String blastnPath;

    //BLASTn and RNAplex processes
    Process blastnProcess;
    Process rnaPlexProcess;

    //The output files used by each thread
    static File outputDir;
    File blastnQuery;
    File blastnSubject;

    //Sequence and annotation for searching
    String queryName, querySeq, subjectName, subjectSeq;
    NATsiRNA_PredictionEngine engine;
    NATsiRNA_PredictionConfiguration config;
    NATsiRNA_PredictionInput input;

    //results from BLASTn and RNAplex
    BLASTnResults blastnResults;
    RNAplexResults plexResults;

    NATPair nat;

    public Worker(NATPair nat, NATsiRNA_PredictionEngine engine) {

        this.nat = nat;
        this.queryName = nat.getQueryName();
        this.querySeq = nat.getQuerySeq();
        this.subjectName = nat.getSubjectName();
        this.subjectSeq = nat.getSubjectSeq();

        this.engine = engine;

        config = NATsiRNA_PredictionConfiguration.getInstance();
        input = NATsiRNA_PredictionInput.getInstance();

        //Check if the paths to third party executables has been set up
        if (plexPath == null || blastnPath == null) {
            String binariesDirectory = AppUtils.INSTANCE.getArchitecture().getBinariesDirectory();
            String path = Tools.EXE_DIR + DIR_SEPARATOR + binariesDirectory + DIR_SEPARATOR + "RNAplex" + AppUtils.INSTANCE.getArchitecture().getExeExtension();

            path = AppUtils.addQuotesIfNecessary(path);
            plexPath = path;

            path = Tools.EXE_DIR + DIR_SEPARATOR + binariesDirectory + DIR_SEPARATOR + "blastn" + AppUtils.INSTANCE.getArchitecture().getExeExtension();

            path = AppUtils.addQuotesIfNecessary(path);
            blastnPath = path;

            outputDir = new File(input.getOutputDir().getAbsolutePath() + File.separator + "tmp");
            outputDir.mkdirs();
        }

        currentWorkerNum = staticWorkerNum++;

        blastnQuery = new File(outputDir.getAbsolutePath() + File.separator + "query_" + currentWorkerNum + ".fa");
        blastnSubject = new File(outputDir.getAbsolutePath() + File.separator + "subject_" + currentWorkerNum + ".fa");
        //blastnResults = new File(outputDir.getAbsolutePath() + File.separator + "results_" + currentWorkerNum + ".out");

    }

    @Override
    public void run() {

        performBlastSearch();

        if (blastnResults != null) {
            if (!config.isCisOnly() || (config.isCisOnly() && blastnResults.getPercentIdenticleMatches() == 100.0)) {
                //We can avoid doing the hybridation for really long sequences
                if (querySeq.length() <= 5000 || subjectSeq.length() <= 5000) {

                    String rnaPlex = runRNAPlex(querySeq, subjectSeq);

                    if (rnaPlex != null) {
                        plexResults = new RNAplexResults(rnaPlex);
                        nat.setRNAplexResults(plexResults);
                        if (plexResults.isValid(blastnResults) || (blastnResults.getPercentIdenticleMatches() == 100 && blastnResults.getAlignmentLength() >= config.getMinOverlapLength())) {
                            String type = calculateType();
                            nat.setType(type);
                            if ((config.isCisOnly() && !type.equals("trans")) || !config.isCisOnly()) {
                                engine.addProcessedNAT(nat);
                            }
                        }
                    }
                } else {
                    //Instead, just use the highly-similar BLASTn results
                    String type = calculateType();
                    nat.setType(type);
                    if ((config.isCisOnly() && !type.equals("trans")) || !config.isCisOnly()) {
                        engine.addProcessedNAT(nat);
                    }
                }
            }
        }

        blastnQuery.delete();
        blastnSubject.delete();

    }

    private void performBlastSearch() {
        //todo implement
        //What if there is more than 1 alignment?
        //ProcessBuilder pb = new ProcessBuilder(blastnPath, " -subject " + blastnSubject.getAbsolutePath()  + " -query " + blastnQuery.getAbsolutePath() + " -strand minus -outfmt \"6\" -max_hsps 1");
        ProcessBuilder pb = new ProcessBuilder(blastnPath, "-query", blastnQuery.getName(), "-subject", blastnSubject.getName(), "-strand", "minus", "-outfmt", "6", "-max_hsps", "1");
        pb.directory(outputDir);
        BufferedReader dataIn = null;
        try {
            BufferedWriter subjectWriter = new BufferedWriter(new FileWriter(blastnSubject));
            BufferedWriter queryWriter = new BufferedWriter(new FileWriter(blastnQuery));

            //write input files for BLASTn
            subjectWriter.write(">" + subjectName + "\n" + subjectSeq);
            queryWriter.write(">" + queryName + "\n" + querySeq);
            subjectWriter.close();
            queryWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        int returnCode = -1;

        try {

            blastnProcess = pb.start();
            dataIn = new BufferedReader(new InputStreamReader(blastnProcess.getInputStream()));
            returnCode = blastnProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        if (returnCode != 0) {
            System.out.println("Something has gone wrong with BLASTn....");
            System.exit(1);
        }
        //process BLASTn results

        String resultsLine = null;
        try {
            if (dataIn != null) {
                //There should be only 1 results because of the -max_hsps 1 flag
                resultsLine = dataIn.readLine();
                dataIn.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        blastnProcess.destroy();

        if (resultsLine != null && !resultsLine.isEmpty()) {
            blastnResults = new BLASTnResults(resultsLine);
            if (blastnResults.getAlignmentLength() >= config.getMinOverlapLength()) {
                nat.setBLASTnResults(blastnResults);
            } else {
                blastnResults = null;
            }
        }
    }

    private String runRNAPlex(String qSeq, String tSeq) {

        int returnCode;
        try {

            File outFile = new File(outputDir.getAbsolutePath() + File.separator + "plex_" + currentWorkerNum + ".out");
            outFile.createNewFile();

            try {
                BufferedWriter subjectWriter = new BufferedWriter(new FileWriter(blastnSubject));
                BufferedWriter queryWriter = new BufferedWriter(new FileWriter(blastnQuery));

                //write input files for RNAplex
                queryWriter.write(">" + queryName + "\n" + qSeq);
                subjectWriter.write(">" + subjectName + "\n" + tSeq);

                subjectWriter.close();
                queryWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            ProcessBuilder pb = new ProcessBuilder(plexPath, "-q", blastnQuery.getName(), "-t", blastnSubject.getName(), "-l", "5000").redirectOutput(outFile);

            pb.directory(outputDir);

            rnaPlexProcess = pb.start();
            BufferedReader dataIn = new BufferedReader(new FileReader(outFile));

            returnCode = rnaPlexProcess.waitFor();
            if (returnCode != 0) {
                System.out.println("Something went wrong with RNAplex. Return code: " + returnCode);
                System.out.println("Please check that your machine has all the dependencies required by RNAplex.");
                System.exit(1);
            }

            String line = null;
            dataIn.readLine();
            dataIn.readLine();
            line = dataIn.readLine();

            rnaPlexProcess.destroy();
            dataIn.close();
            outFile.delete();
            return line;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    private String calculateType() {

        if (geneAnnotationMap == null || !geneAnnotationMap.containsKey(queryName) || !geneAnnotationMap.containsKey(subjectName)) {
            return basicTypeCalc();
        }

        GeneAnnotation query = geneAnnotationMap.get(queryName);
        GeneAnnotation subject = geneAnnotationMap.get(subjectName);

        if (query.getStrand().equals(subject.getStrand())) {
            return "trans";
        }
        //Calc based on the GFF3 file
        //head to head check
        if (isEnclosed()) {

            return "cis (enclosed)";

        } else if (isHeadToHead()) {

            return "cis (head-to-head)";

        } else if (isTailToTail()) {

            return "cis (tail-to-tail)";

        }

        //if we get here something isn't right...
        //System.out.println("something wrong, check....");
        return "trans";
    }

    private boolean isEnclosed() {

        GeneAnnotation query = geneAnnotationMap.get(queryName);
        GeneAnnotation subject = geneAnnotationMap.get(subjectName);

        //check if query is enclosed in subject
        if (query.getStart() >= subject.getStart() && query.getEnd() <= subject.getEnd()) {
            return true;
        }
        //check if subject is enclosed in query
        return subject.getStart() >= query.getStart() && subject.getEnd() <= query.getEnd();

    }

    private boolean isHeadToHead() {

        GeneAnnotation plus, minus;
        GeneAnnotation query = geneAnnotationMap.get(queryName);
        GeneAnnotation subject = geneAnnotationMap.get(subjectName);

        if (query.getStrand().equals(subject.getStrand())) {
            return false;
        }

        if (query.getStrand().equals("+")) {
            plus = query;
            minus = subject;
        } else {
            plus = subject;
            minus = query;
        }

        return plus.getStart() < minus.getEnd() && plus.getEnd() > minus.getEnd();
    }

    private boolean isTailToTail() {
        GeneAnnotation plus, minus;
        GeneAnnotation query = geneAnnotationMap.get(queryName);
        GeneAnnotation subject = geneAnnotationMap.get(subjectName);

        if (query.getStrand().equals(subject.getStrand())) {
            return false;
        }

        if (query.getStrand().equals("+")) {
            plus = query;
            minus = subject;
        } else {
            plus = subject;
            minus = query;
        }

        return plus.getEnd() > minus.getStart() && plus.getEnd() < minus.getEnd();

    }

    private String basicTypeCalc() {
        if (blastnResults.getPercentIdenticleMatches() == 100.0) {
            return "cis";
        }

        return "trans";
    }

    public static void extractGeneAnnotation(File gff) {
        geneAnnotationMap = new HashMap();

        try {
            BufferedReader br = new BufferedReader(new FileReader(gff));

//            if (!br.readLine().equals("##gff-version 3")) {
//                throw new Exception("Input file is not a valid GFF3 file!");
//            }
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {

                    //Figure out how to get the name including the . (version) here....
                    String splits[] = line.split("\t");
//Record record = new Record(splits[0], splits[1], splits[2], Integer.parseInt(splits[3]), Integer.parseInt(splits[4]), splits[5], splits[6], splits[7], splits[8]);

                    if (splits[2].equals("mRNA")) {
                        String chr = splits[0];
                        int start = Integer.parseInt(splits[3]);
                        int end = Integer.parseInt(splits[4]);
                        String strand = splits[6];

                        String attributes[] = splits[8].split(";");
                        String id = attributes[0].split("=")[1];

                        geneAnnotationMap.put(id, new GeneAnnotation(start, end, strand, chr));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class GeneAnnotation {

        int start;
        int end;
        String strand;
        String chr;

        public GeneAnnotation(int start, int end, String strand, String chr) {
            this.start = start;
            this.end = end;
            this.strand = strand;
            this.chr = chr;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getStrand() {
            return strand;
        }

        public String getChr() {
            return chr;
        }

    }

}
