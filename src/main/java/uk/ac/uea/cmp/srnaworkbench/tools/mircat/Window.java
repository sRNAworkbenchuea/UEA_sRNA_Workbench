/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.EventQueue;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBaseHeader;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.exceptions.HairpinExtensionException;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.MiRCatLogger.MIRCAT_LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author w0445959
 */
public class Window implements Runnable {

    private FastaRecord rsqField;
    private int currentStartField;
    private int currentEndField;
    private SequenceStrand strandField;
    private PatmanEntry current_hitField;
    private int genome_hitsField;
    private String originalReadField;
    private Patman current_clusterField;
    private JTable guiOutputTable = null;
    //vars used to access result String array from RNA fold. String array is returned by method getStructure()
    private static final int FIRST_PART = 0;
    private static final int MODIFIED_REGION = 1;
    private static final int END_PART = 2;
    private static final int ORIGINAL_RNAFOLD_RESULT = 3;
    private static final int ORIGINAL_RNAFOLD_RESULT_NO_MOD = 4;
    private static final int TRUNCATED_WINDOW_SEQ = 5;
    private static final int RANDFOLD_MFE = 1;
    private static final int RANDFOLD_P_Val = 2;
    private boolean withrandfold;
    private MiRCatParams params = null;
    private boolean outputLogging = false;
    private JProgressBar myProgBar = null;
    private JLabel myStatusLabel = null;
    private StyledDocument myTextLog = null;
    private boolean debug = false;
    private String processString = "";
    private BinaryExecutor myExeMan = null;
    private HashMap<String, ArrayList<MirBaseHeader>> myMirBaseData;
    private final FastaMap patmanEntries;
    private int myThreadID;
    private File tempDir;
    private String outputDir;

    public Window(FastaRecord rsq, int currentStart, int currentEnd, SequenceStrand strand, PatmanEntry current_hit,
            int genome_hits, String originalRead, Patman current_cluster,
            FastaMap patmanEntries,
            JTable guiOutputTable,
            MiRCatParams params,
            boolean outputLogging,
            StyledDocument myTextLog,
            JProgressBar myProgBar,
            JLabel myStatusLabel,
            BinaryExecutor newExeMan,
            HashMap<String, ArrayList<MirBaseHeader>> mirBaseData,
            int myThreadID,
            File tempDir, String outputDir) throws IOException, HairpinExtensionException {
        //System.out.println("created window: " + windowSeq);
        rsqField = rsq;
        currentStartField = currentStart;
        currentEndField = currentEnd;
        strandField = strand;
        current_hitField = current_hit;
        genome_hitsField = genome_hits;
        //System.out.println( "Window: " + myThreadID + " genomichits: " + genome_hitsField );
        originalReadField = originalRead;
        current_clusterField = current_cluster;
        this.patmanEntries = patmanEntries;
        this.guiOutputTable = guiOutputTable;

        this.params = params;
        this.outputLogging = outputLogging;
        this.myTextLog = myTextLog;
        this.myProgBar = myProgBar;
        this.myStatusLabel = myStatusLabel;
        myExeMan = newExeMan;

        myMirBaseData = mirBaseData;
        this.myThreadID = myThreadID;
        this.tempDir = tempDir;
        this.outputDir = outputDir;
        
//        if(originalRead.equals("TCGGACCAGGCTTCATCCCCC"))
//        {
//            System.out.println("");
//        }
    }

    @Override
    public void run() {
        try {
            this.processWindows(rsqField, currentStartField, currentEndField, strandField, current_hitField, genome_hitsField, originalReadField, current_clusterField);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            LOGGER.log(Level.SEVERE, Tools.getStackTrace(ex));
        }

    }

    private void processWindows(FastaRecord rsq, int currentStart, int currentEnd, SequenceStrand strand,
            PatmanEntry current_hit,
            int genome_hits, String originalRead, Patman current_cluster) throws IOException, HairpinExtensionException {
        
        for(PatmanEntry pe : current_cluster)
        {
            if(pe.getSequence().equals("ACTTATTTACAATGGCTGCCA"))
                System.out.println("");
        }
        
        

        int newStart = 0;
        int newEnd = 0;
        float extendAmount = 0.0f;
        ArrayList<String> extendedSequences = new ArrayList<String>();
        ArrayList<String> processedHairpins = new ArrayList<String>();
        HashMap<Float, ArrayList<String>> data = new HashMap<Float, ArrayList<String>>();
        int[] currentPointer
                = {
                    0
                };
        String fastaHeader = ">" + rsq.getFastaHeader();
        //String fastaHeader = ">" + rsq.getSequenceId();
        float extend = params.getExtend();

        for (int retry = 0; retry < 14; retry++) {

            if (this.outputLogging) {

                MIRCAT_LOGGER.log(Level.WARNING, "Window {0}...", retry);
            }

            switch (retry) {

                case 0:
                    newStart = Math.round(currentStart - (extend));
                    newEnd = Math.round(currentEnd + (extend));

                    extendAmount = extend;
                    break;
                case 1:
                    newStart = (currentStart - Math.round(extend * 0.2f));
                    newEnd = (currentEnd + Math.round(extend * 1.8f));

                    extendAmount = extend * 0.2f;
                    break;
                case 2:
                    newStart = (currentStart - Math.round(extend * 1.8f));
                    newEnd = (currentEnd + Math.round(extend * 0.2f));

                    extendAmount = extend * 1.8f;
                    break;
                case 3:
                    newStart = (currentStart - Math.round(extend * 0.75f));
                    newEnd = (currentEnd + Math.round(extend * 0.75f));

                    extendAmount = extend * 0.75f;
                    break;
                case 4:
                    newStart = (currentStart - Math.round(extend * 0.2f));
                    newEnd = (currentEnd + Math.round(extend * 0.8f));

                    extendAmount = extend * 0.2f;
                    break;
                case 5:
                    newStart = (currentStart - Math.round(extend * 0.8f));
                    newEnd = (currentEnd + Math.round(extend * 0.2f));

                    extendAmount = extend * 0.8f;
                    break;
                case 6:
                    newStart = (currentStart - Math.round(extend * 0.9f));
                    newEnd = (currentEnd + Math.round(extend * 0.1f));

                    extendAmount = extend * 0.9f;
                    break;
                case 7:
                    newStart = (currentStart - Math.round(extend * 0.1f));
                    newEnd = (currentEnd + Math.round(extend * 0.9f));

                    extendAmount = extend * 0.1f;
                    break;
                case 8:
                    newStart = (currentStart - Math.round(extend * 0.5f));
                    newEnd = (currentEnd + Math.round(extend * 0.5f));

                    extendAmount = extend * 0.5f;
                    break;
                case 9:
                    newStart = (currentStart - Math.round(extend * 0.2f));
                    newEnd = (currentEnd + Math.round(extend * 0.7f));

                    extendAmount = extend * 0.2f;
                    break;
                case 10:
                    newStart = (currentStart - Math.round(extend * 0.7f));
                    newEnd = (currentEnd + Math.round(extend * 0.2f));

                    extendAmount = extend * 0.7f;
                    break;
                case 11:
                    newStart = (currentStart - Math.round(extend * 0.6f));
                    newEnd = (currentEnd + Math.round(extend * 0.4f));

                    extendAmount = extend * 0.6f;
                    break;
                case 12:
                    newStart = (currentStart - Math.round(extend * 0.4f));
                    newEnd = (currentEnd + Math.round(extend * 0.6f));

                    extendAmount = extend * 0.4f;
                    break;
                case 13:
                    if (!data.isEmpty()) {
                        if (this.outputLogging) {

                            MIRCAT_LOGGER.log(Level.INFO, "reached window 14 and initial checks have succeeded...");

                        }

                        if (myStatusLabel != null) {

                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        myTextLog.remove(myTextLog.getLength() - processString.length(), processString.length());
                                        myTextLog.insertString(myTextLog.getLength(), processString,
                                                myTextLog.getStyle(Tools.initStyles[5]));

                                    } catch (BadLocationException ex) {
                                        LOGGER.log(Level.SEVERE, null, ex);
                                    }
                                }
                            });
                        }

                        updateOutputModules(newStart, newEnd, currentStart, currentEnd, genome_hits,
                                strand, fastaHeader,
                                current_hit, rsq, originalRead,
                                current_cluster, data);
                    } else {
                        if (this.outputLogging) {

                            MIRCAT_LOGGER.log(Level.WARNING, "reached window 14 and all checks failed so ignoring");

                        }
                    }
                    return;
            }

            if (newStart < 1) {
                newStart = 1;
            } else {
                // if ( newEnd > rsq.getSequenceLength() - 1 )
                if (newEnd > rsq.getSequenceLength()) {
          // newEnd = rsq.getSequenceLength() - 1;
                    // the code above (and if statement) is correcting newEnd to be the "end INDEX". We actually want the end POSITION since
                    // that is what we are dealing in for currentEnd
                    newEnd = rsq.getSequenceLength();
                }
            }

            String windowSeq = rsq.getSubSequenceAsString(newStart, newEnd, strand);

            

            int calcOffset = 0;
            if (strand.equals(SequenceStrand.POSITIVE)) //calcOffset = (currentStart - newStart) - 1;
            {
                calcOffset = (currentStart - newStart); // correct offset - shouldn't need - 1!!
            } else {
                calcOffset = newEnd - currentEnd;
            }
            int originalLength = (currentEnd - currentStart) + 1;
            //int startOffset = windowSeq.indexOf( originalRead );

            processSingleWindow(windowSeq, strand, fastaHeader,
                    currentPointer, originalLength, calcOffset, newStart, newEnd,
                    extendedSequences, processedHairpins,
                    data);
            if (myStatusLabel != null) {

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        myProgBar.setValue(myProgBar.getValue() + 1);
                    }
                });

            }

        }

    }

    private void updateOutputModules(int newStart, int newEnd, int currentStart, int currentEnd, int genome_hits,
            SequenceStrand strand, String fastaHeader,
            PatmanEntry current_hit, FastaRecord chromo, String originalRead,
            Patman current_cluster, HashMap<Float, ArrayList<String>> data) throws IOException {
        
//        if(currentStart == 78932 && currentEnd == 78952)
//        {
//            System.out.println("");
//        }

        ArrayList<String> bestHit = new ArrayList<String>();
        float currentMinEnergy = Float.MAX_VALUE;
        for (Entry<Float, ArrayList<String>> bestHits : data.entrySet()) {
            if (bestHits.getKey() < currentMinEnergy) {
                bestHit = bestHits.getValue();
                currentMinEnergy = bestHits.getKey();
            }
        }

        ArrayList<Integer> pairs = gatherPairs(bestHit.get(5));
        ArrayList<String> mirSTAR_Sequences = new ArrayList<String>();
        boolean[] any_miRNA_pass
                = {
                    false, false
                };

        ArrayList<String> mirStarList = find_mir_star(bestHit.get(0), bestHit.get(1), bestHit.get(2),
                newStart, newEnd,
                pairs, Integer.parseInt(bestHit.get(4)),
                bestHit.get(6),
                mirSTAR_Sequences, any_miRNA_pass);

    //System.out.println("[0]" + any_miRNA_pass[0] + " [1]" + any_miRNA_pass[1]);
//                            hairpinData.add(fullHairpinMarked);0
//                            hairpinData.add(fullStructure);1
//                            hairpinData.add(hairpinSequence);2
//                            if(strand.equals("+"))
//                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.indexOf(startPairData))));3
//                            else
//                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.indexOf(startPairData))));
//                            hairpinData.add(Integer.toString(modifiedStructs[MODIFIED_REGION].length()));4
//                            hairpinData.add(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);5
//                            hairpinData.add(modifiedStructs[TRUNCATED_WINDOW_SEQ]);6
//                            hairpinData.add(Float.toString(mfe));7
//                            //hairpinData.add(offsetStart + pairs.indexOf(startPairData));
//                            if(strand.equals("+"))
//                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.lastIndexOf(startPairData) + 1)));
//                            else
//                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.lastIndexOf(startPairData) + 1)));8
//                            hairpinData.add(Integer.toString(newStart));9
//                            hairpinData.add(Integer.toString(newEnd));10
        if (any_miRNA_pass[0]) {

            int hairpinStartIndex = 0;
            int hairpinEndIndex = 0;

            if (strand == SequenceStrand.POSITIVE) {
                hairpinStartIndex = Integer.parseInt(bestHit.get(3));
                hairpinEndIndex = Integer.parseInt(bestHit.get(8)) - 1;

//                System.out.println("before: " + bestHit.get(2));
//                bestHit.set(2, chromo.getSequenceAsString(hairpinStartIndex, hairpinEndIndex, Strand.POSITIVE));
//                System.out.println("after: " + bestHit.get(2));
            } else {
                hairpinStartIndex = Integer.parseInt(bestHit.get(8));
                hairpinEndIndex = Integer.parseInt(bestHit.get(3)) - 1;

//                System.out.println("before: " + bestHit.get(2));
//                bestHit.set(2, chromo.getSequenceAsString(hairpinStartIndex, hairpinEndIndex, Strand.NEGATIVE));
//                System.out.println("after: " + bestHit.get(2));
            }

            float totalHitsInHairpin = 0;
            float totalContainedIn_miRNA = 0;
            for (PatmanEntry current_patman_hit : current_cluster) {
                //if(current_patman_hit)
                int hitStartPos = current_patman_hit.getStart();
                int hitEndPos = current_patman_hit.getEnd();
                //if( (hitStartPos >= hairpinStartIndex-200) && (hitEndPos <= hairpinEndIndex+200))
                {
                    //System.out.println("inside cluster. Looking at: " + hitStartPos + " and " + hitEndPos);

                    double abund = current_patman_hit.getAbundance();
                    totalHitsInHairpin += abund;
                    //check hit against miRNA

                    if ((hitStartPos >= currentStart - 2) && (hitEndPos <= currentEnd + 2)) {
            //System.out.println("hit start: " + hitStartPos + " hit end pos " + hitEndPos);
                        //System.out.println("incrementing as sequence lies around mirna : " + abund);
                        totalContainedIn_miRNA += abund;
                        continue;
                    }

          //check mirStars
                    searchMirSTAR:
                    for (String entry : mirSTAR_Sequences) {
                        int savedStart, savedEnd;
                        if (strand == SequenceStrand.POSITIVE) {
                            savedStart = hairpinEndIndex - bestHit.get(2).indexOf(entry) - 21;
                            savedEnd = hairpinEndIndex - bestHit.get(2).indexOf(entry);
                        } else {
                            savedStart = hairpinStartIndex + bestHit.get(2).indexOf(entry);
                            savedEnd = hairpinStartIndex + bestHit.get(2).indexOf(entry) + 21;
                        }
                        if ((hitStartPos >= savedStart - 2) && (hitEndPos <= savedEnd + 2)) {
                            totalContainedIn_miRNA += abund;
                            break searchMirSTAR;
                        }
                    }
                }
            }
            float percentageOverlap = (totalContainedIn_miRNA / totalHitsInHairpin) * 100.0f;

            LOGGER.log(Level.FINE, "total found: {0} totalIn miRNA{1} percentage: {2}", new Object[]{
                totalHitsInHairpin, totalContainedIn_miRNA, percentageOverlap
            });

            if (percentageOverlap >= params.getMaxOverlapPercentage()) {
                if (myStatusLabel != null) {
                    try {
                        myTextLog.remove(myTextLog.getLength() - processString.length(), processString.length());
                        myTextLog.insertString(myTextLog.getLength(), processString,
                                myTextLog.getStyle(Tools.initStyles[7]));
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }

          //myProgBar.setMaximum(14);
                }
                this.withrandfold = true;
                String[] reFold_results = refold(bestHit.get(2), fastaHeader);
                this.withrandfold = false;
                float mfe = Float.parseFloat(reFold_results[RANDFOLD_MFE].trim());
                float amfe = (mfe / (float) bestHit.get(2).length()) * 100.0f;
                float p_val = Float.parseFloat(reFold_results[RANDFOLD_P_Val].trim());
                if ((amfe < params.getMinEnergy()) && p_val < params.getPVal()) {
                    //FastaRecord hairpinSequenceObject = new FastaRecord(bestHit.get(2));

                    String finalMarkedUpHairpin = "";
                    String mostAbundMirStar = "";

                    float hairpinGC = ((((float) SequenceUtils.DNA.getGCCount(bestHit.get(2)) / (float) bestHit.get(2).length())) * 100.0f);

                    if (guiOutputTable == null) {
                        LOGGER.log(Level.FINE, "updating file output");

                        FileWriter outFile = new FileWriter(outputDir + DIR_SEPARATOR + "output.csv", true);

                        PrintWriter outputCSV = new PrintWriter(outFile);

                        FileWriter outHFile = new FileWriter(outputDir + DIR_SEPARATOR + "miRNA_hairpins.txt", true);

                        PrintWriter outputTXT = new PrintWriter(outHFile);

                        FileWriter outmiRNAFile = new FileWriter(outputDir + DIR_SEPARATOR + "miRNA.fa", true);

                        PrintWriter outputmiRNA = new PrintWriter(outmiRNAFile);

                        // chris added code to check entry for mirbase 
                        String mirBaseID = "";
                        if (myMirBaseData != null && myMirBaseData.containsKey(current_hit.getSequence())) {
                            HashMap<String, MirBaseHeader> tempMap = new HashMap<String, MirBaseHeader>();
                            ArrayList<MirBaseHeader> mirBaseEntries = myMirBaseData.get(current_hit.getSequence());
                            for (MirBaseHeader header : mirBaseEntries) {
                                tempMap.put(header.getMircode().getFamily(), header);
                            }
                            for (String family : tempMap.keySet()) {
                                mirBaseID += family;
                            }
                        } else {
                            mirBaseID += "N/A";
                        }

                        if (!any_miRNA_pass[1]) {
                            finalMarkedUpHairpin = bestHit.get(0);

                            outputCSV.println(chromo.getFastaHeader() + "," + current_hit.getStart() + "," + current_hit.getEnd() + ","
                                    + current_hit.getSequenceStrand() + "," + current_hit.getAbundance() + "," + current_hit.getSequence() + "," + current_hit.getSequenceLength() + ","
                                    + genome_hitsField + "," + finalMarkedUpHairpin.length() + "," + hairpinGC + "," + mfe + "," + amfe
                                    + ",NO," + mirBaseID + "," + p_val);
                            outputCSV.close();
                            outFile.close();
                            //outputCSV[0].flush();
                        } else {
                            int currentMaxAbund = 0;
                            String mirStars = "";
                            for (String mirStar : mirStarList) {
                                String[] mirStarSeqAndAbund = mirStar.split("\\(");

                                String tempAbun = mirStarSeqAndAbund[1].replace(")", "");
                                int currentAbund = Integer.parseInt(tempAbun.trim());
                                if (currentAbund > currentMaxAbund) {
                                    currentMaxAbund = currentAbund;
                                    mostAbundMirStar = mirStarSeqAndAbund[0];
                                }
                                mirStars += mirStar + " ";
                            }

                            finalMarkedUpHairpin = markup_mirstar(bestHit.get(2), bestHit.get(0), mostAbundMirStar);

                            outputCSV.println(chromo.getFastaHeader() + "," + current_hit.getStart() + "," + current_hit.getEnd() + ","
                                    + current_hit.getSequenceStrand() + "," + current_hit.getAbundance() + ","
                                    + current_hit.getSequence() + ","
                                    + current_hit.getSequenceLength() + ","
                                    + genome_hitsField + "," + finalMarkedUpHairpin.length() + "," + hairpinGC + "," + mfe + "," + amfe
                                    + "," + mirStars + "," + mirBaseID + "," + p_val);
              //outputCSV[0].flush();

                            outputCSV.close();
//                            for(String mirStars : mirSTAR_Sequences)
//                                System.out.println("mirStar: " + mirStars);
                        }
                        outputmiRNA.println(">" + current_hit.getSequence() + "(" + current_hit.getAbundance() + ")");
                        outputmiRNA.println(current_hit.getSequence());
                        outputmiRNA.close();
                        outmiRNAFile.close();

                        outputTXT.println(">" + originalRead + "_" + chromo.getSequenceId() + "/" + current_hit.getStart() + "-" + current_hit.getEnd());
                        outputTXT.println(bestHit.get(2));
                        outputTXT.println(finalMarkedUpHairpin);

                        outputTXT.println();
                        outHFile.close();
                        outputTXT.close();

                    } else {
                        //System.out.println("updating GUI table");
                        String mirStars = "";

                        if (!any_miRNA_pass[1]) {
                            finalMarkedUpHairpin = bestHit.get(0);
                            //System.out.println("NO MIRNA: " + finalMarkedUpHairpin);
                        } else {
                            int currentMaxAbund = 0;

                            for (String mirStar : mirStarList) {
                                String[] mirStarSeqAndAbund = mirStar.split("\\(");

                                String tempAbun = mirStarSeqAndAbund[1].replace(")", "").trim();
                                int currentAbund = Integer.parseInt(tempAbun.trim());
                                if (currentAbund > currentMaxAbund) {
                                    currentMaxAbund = currentAbund;
                                    mostAbundMirStar = mirStarSeqAndAbund[0];
                                }
                                mirStars += mirStar + " ";
                            }

                            finalMarkedUpHairpin = markup_mirstar(bestHit.get(2), bestHit.get(0), mostAbundMirStar);

                            // System.out.println("MIRNA: " + finalMarkedUpHairpin);
                        }
                        String finalHairpinSequence = "";
                        double tempAbun = current_hit.getAbundance();
                        int[] miRNA_STAR_locs
                                = {
                                    -1, -1
                                };

                        if (!any_miRNA_pass[1]) {
                            //guiOutputTable.setValueAt("NO", row, col);
                            mirStars = "NO";
                            finalHairpinSequence = constructHTMLStrings(bestHit.get(2),
                                    current_hit.getSequence(), "", "#FFFFFF", null);

                        } else {
                            finalHairpinSequence = constructHTMLStrings(bestHit.get(2),
                                    current_hit.getSequence(), mostAbundMirStar, "#FFFFFF", miRNA_STAR_locs);
                            if (strand == SequenceStrand.POSITIVE) {
                                miRNA_STAR_locs[0] += hairpinStartIndex;
                                miRNA_STAR_locs[1] += hairpinStartIndex;
                            } else {
                                int end = miRNA_STAR_locs[0];
                                miRNA_STAR_locs[0] = hairpinEndIndex - miRNA_STAR_locs[1];
                                miRNA_STAR_locs[1] = hairpinEndIndex - end - 1;
                            }
                        }
                        String mirBaseID = "<HTML><font color=#FFFFFF>";

                        if (myMirBaseData != null && myMirBaseData.containsKey(current_hit.getSequence())) {
                            HashMap<String, MirBaseHeader> tempMap = new HashMap<String, MirBaseHeader>();
                            ArrayList<MirBaseHeader> mirBaseEntries = myMirBaseData.get(current_hit.getSequence());
                            for (MirBaseHeader header : mirBaseEntries) {
                                tempMap.put(header.getMircode().getFamily(), header);
                            }
                            for (String family : tempMap.keySet()) {
                                mirBaseID += family + "<br></HTML>";
                            }
                        } else {
                            mirBaseID += "N/A";
                        }
                        mirBaseID += "";
                        final Object[] newRow = new Object[]{
                            chromo.getFastaHeader(),
                            current_hit.getSequence(),
                            mirStars,
                            finalHairpinSequence,
                            current_hit.getStart(),
                            current_hit.getEnd(),
                            current_hit.getSequenceStrand(),
                            tempAbun,
                            current_hit.getSequenceLength(),
                            finalMarkedUpHairpin.length(),
                            hairpinStartIndex,
                            hairpinEndIndex,
                            finalMarkedUpHairpin,
                            genome_hitsField,
                            mfe,
                            amfe,
                            hairpinGC,
                            miRNA_STAR_locs[0],
                            miRNA_STAR_locs[1],
                            mirBaseID,
                            p_val
                        };
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                DefaultTableModel model = (DefaultTableModel) guiOutputTable.getModel();
                                model.addRow(newRow);
                                guiOutputTable.setModel(model);
                                guiOutputTable.revalidate();
                            }
                        });
                    }
                } else {
                    if (outputLogging) {
                        MIRCAT_LOGGER.log(Level.WARNING, "Failed due randfold AMFE: {0} being too high", amfe);
                    }
                }
            } else {
                if (outputLogging) {
                    MIRCAT_LOGGER.log(Level.WARNING, "Failed due percentage of overlapping sequences in cluster: {0} being too low", percentageOverlap);
                }
            }
        } else {
            if (outputLogging) {
                MIRCAT_LOGGER.log(Level.WARNING, "Failed because all miRNA* regions failed the validity test ");
            }
        }
    }

    private synchronized void processSingleWindow(String windowSeq, SequenceStrand strand, String fastaHeader,
            int[] currentPointer, int originalLength, int startOffset, int newStart, int newEnd,
            ArrayList<String> extendedSequences, ArrayList<String> processedHairpins,
            HashMap<Float, ArrayList<String>> data) throws IOException, HairpinExtensionException {
        
        String[] modifiedStructs = getStructure(windowSeq, originalLength, startOffset);
        if ((modifiedStructs != null) && (modifiedStructs[MODIFIED_REGION].length() <= 25)) {

            ArrayList<Integer> pairs = gatherPairs(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);

            //to make this code neater, in fact this gathering stage could be completed for all windows
            //then the if statement below processed on the entires extended sequences datastructure
            gatherFlanking_miRNA(modifiedStructs[MODIFIED_REGION], modifiedStructs[FIRST_PART], modifiedStructs[END_PART], extendedSequences);

            if (extendedSequences.size() > 0) {

                while (currentPointer[0] < extendedSequences.size()) {
                    String extendedMiRNA = extendedSequences.get(currentPointer[0]);

                    //System.out.println("extendedHairpin: " + )
                    String fullStructure = modifiedStructs[FIRST_PART] + modifiedStructs[MODIFIED_REGION] + modifiedStructs[END_PART];
                    String checkBaseString = "";
                    int seqStartPos = -1;
                    if (extendedMiRNA.contains("<") || extendedMiRNA.contains("(")) {

                        checkBaseString = extendedMiRNA.replaceFirst("[\\-\\.]*", "");
                        seqStartPos = fullStructure.indexOf(checkBaseString);
                    } else {

                        int endBracketClose = extendedMiRNA.lastIndexOf(">");
                        int endBracketOpen = extendedMiRNA.lastIndexOf(")");

                        checkBaseString = extendedMiRNA.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
                        seqStartPos = fullStructure.indexOf(checkBaseString) + checkBaseString.length() - 1;
                    }
                    if (seqStartPos >= 0) {
                        int startPairData = pairs.get(seqStartPos);

                        String fullHairpin = modifiedStructs[ORIGINAL_RNAFOLD_RESULT].substring(pairs.indexOf(startPairData), pairs.lastIndexOf(startPairData) + 1);
                        String fullHairpinMarked = fullStructure.substring(pairs.indexOf(startPairData), pairs.lastIndexOf(startPairData) + 1);
                        int offsetStart = modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD].indexOf(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                        //int offsetEnd
                        String hairpinSequence = windowSeq.substring(offsetStart + pairs.indexOf(startPairData), offsetStart + pairs.lastIndexOf(startPairData) + 1);
                        int analyseResult = analyse_structure(fullHairpin);

                        if (analyseResult != 2) {

                            if (!processedHairpins.contains(hairpinSequence)) {
                                processedHairpins.add(hairpinSequence);

                                this.withrandfold = false;
                                String[] reFold_results = refold(hairpinSequence, fastaHeader);
                                this.withrandfold = true;

                                try {
                                    float mfe = StringUtils.safeFloatParse(reFold_results[RANDFOLD_MFE].trim(), Float.MIN_VALUE);
                                    float amfe = (mfe / (float) hairpinSequence.length()) * 100.0f;

                                    LOGGER.log(Level.FINE, "amfe: {0}", amfe);

                                    if (amfe <= params.getMinEnergy()) {
                                        ArrayList<String> hairpinData = new ArrayList<String>();
                                        hairpinData.add(fullHairpinMarked);
                                        hairpinData.add(fullStructure);
                                        hairpinData.add(hairpinSequence);
                                        if (strand == SequenceStrand.POSITIVE) {
                                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.indexOf(startPairData))));
                                        } else {
                                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.indexOf(startPairData))));
                                        }
                                        hairpinData.add(Integer.toString(modifiedStructs[MODIFIED_REGION].length()));
                                        hairpinData.add(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                                        hairpinData.add(modifiedStructs[TRUNCATED_WINDOW_SEQ]);
                                        hairpinData.add(Float.toString(mfe));
                                        //hairpinData.add(offsetStart + pairs.indexOf(startPairData));
                                        if (strand == SequenceStrand.POSITIVE) {
                                            hairpinData.add(Integer.toString(newStart + (offsetStart + pairs.lastIndexOf(startPairData) + 1)));
                                        } else {
                                            hairpinData.add(Integer.toString(newEnd - (offsetStart + pairs.lastIndexOf(startPairData) + 1)));
                                        }
                                        hairpinData.add(Integer.toString(newStart));
                                        hairpinData.add(Integer.toString(newEnd));
                                        data.put(amfe, hairpinData);
                                    } else {
                                        if (outputLogging) {
                                            MIRCAT_LOGGER.log(Level.WARNING, "AMFE was too high to allow hairpin through: {0}", amfe);
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    LOGGER.log(Level.FINE, "rnafold returned no result on refold");
                                }

                                String spaces = "";
                                String otherSpaces = "";
                                for (int i = 0; i < offsetStart; i++) {
                                    spaces += " ";
                                }
                                for (int i = 0; i < pairs.indexOf(startPairData); i++) {
                                    otherSpaces += " ";
                                }

                                StringBuilder sb = new StringBuilder();

                                sb.append("extendedMiRNA           : ").append(extendedMiRNA).append(LINE_SEPARATOR);
                                sb.append("full structure          : ").append(fullStructure).append(LINE_SEPARATOR);;
                                sb.append("original result         : ").append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]).append(LINE_SEPARATOR);;
                                sb.append("full structure  spaced  : ").append(spaces).append(fullStructure).append(LINE_SEPARATOR);;
                                sb.append("original result spaced  : ").append(spaces).append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT]).append(LINE_SEPARATOR);;
                                sb.append("original result no mods : ").append(modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD]).append(LINE_SEPARATOR);;
                                sb.append("hairpin  spaced         : ").append(spaces).append(otherSpaces).append(fullHairpin).append(LINE_SEPARATOR);;
                                sb.append("hairpin markup spaced   : ").append(spaces).append(otherSpaces).append(fullHairpinMarked).append(LINE_SEPARATOR);;

                                sb.append("truncated window seq    : ").append(modifiedStructs[TRUNCATED_WINDOW_SEQ]).append(LINE_SEPARATOR);;
                                sb.append("hairpin sequence spaced : ").append(spaces).append(otherSpaces).append(hairpinSequence).append(LINE_SEPARATOR);;

                                sb.append("hairpin sequence        : ").append(hairpinSequence).append(LINE_SEPARATOR);;
                                sb.append("result of analysis      : ").append(analyseResult).append(LINE_SEPARATOR);;

                                LOGGER.log(Level.FINE, sb.toString());

                                if (outputLogging) {
                                    spaces = "";
                                    otherSpaces = "";
                                    for (int i = 0; i < offsetStart; i++) {
                                        spaces += " ";
                                    }
                                    for (int i = 0; i < pairs.indexOf(startPairData); i++) {
                                        otherSpaces += " ";
                                    }
                                    MIRCAT_LOGGER.log(Level.WARNING, "extendedHairpin         : {0}", extendedMiRNA);
                                    MIRCAT_LOGGER.log(Level.WARNING, "full structure          : {0}", fullStructure);
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result         : {0}", modifiedStructs[ORIGINAL_RNAFOLD_RESULT]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "full structure  spaced  : {0}{1}", new Object[]{spaces, fullStructure});
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result spaced  : {0}{1}", new Object[]{spaces, modifiedStructs[ORIGINAL_RNAFOLD_RESULT]});
                                    MIRCAT_LOGGER.log(Level.WARNING, "original result no mods : {0}", modifiedStructs[ORIGINAL_RNAFOLD_RESULT_NO_MOD]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin  spaced         : {0}{1}{2}", new Object[]{spaces, otherSpaces, fullHairpin});
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin markup spaced   : {0}{1}{2}", new Object[]{spaces, otherSpaces, fullHairpinMarked});

                                    MIRCAT_LOGGER.log(Level.WARNING, "truncated window seq    : {0}", modifiedStructs[TRUNCATED_WINDOW_SEQ]);
                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin sequence spaced : {0}{1}{2}", new Object[]{spaces, otherSpaces, hairpinSequence});

                                    MIRCAT_LOGGER.log(Level.WARNING, "hairpin sequence        : {0}", hairpinSequence);
                                    MIRCAT_LOGGER.log(Level.WARNING, "result of analysis      : {0}", analyseResult);

                                }

//
                                //System.out.println("amfe: " + amfe + " mfe: " + mfe);
                                //else {
//                                                    System.out.println("ignoring due to min energy being too high");
//                                                }
                            }
                        } else {
                            if (outputLogging) {
                                switch (analyseResult) {
                                    case 2:
                                        MIRCAT_LOGGER.log(Level.WARNING, "the hairpin failed due to length, structure or a high percentage of unpaired bases");
                                        break;
                                    case 3:
                                        MIRCAT_LOGGER.log(Level.WARNING, "the hairpin contained complex loops. Please re-run with this option enabled");
                                        break;
                                    default:
                                        MIRCAT_LOGGER.log(Level.INFO, "Succeeded tests on hairpin");
                                }
                            }
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "problem with string locations");

                        if (outputLogging) {
                            MIRCAT_LOGGER.log(Level.WARNING, "problem with string locations. This could be a bug");
                        }
                    }
                    currentPointer[0]++;
                }

            } else {

                if (outputLogging) {
                    MIRCAT_LOGGER.log(Level.INFO, "No flanking sequences were created for miRNA. Ignore");
                }
            }
        } else {
            //System.out.println("window structure has failed");

            if (outputLogging) {
                MIRCAT_LOGGER.log(Level.INFO, "Either RNAfold returned no result or the resulting sequence to be flanked was longer than 25. Ignore");
            }
        }
    }

    private synchronized ArrayList<Integer> gatherPairs(String originalDotBracket) {
    //originalDotBracket = "(((((((....(((..((.(((.......(((.(((....)))..)))(.....).......))).))..))).)))))))....................(((((.(((........))).((((((((.((((((......))))))...)))))))).....)))))";
        //System.out.println("Result D: " + originalDotBracket);
        ArrayList<Integer> pairs = new ArrayList<Integer>();

        LinkedList<Integer> structureStarts = new LinkedList<Integer>();
        int currentOpen = 0, currentClose = 0;
        char openBracket = '(';
        char closeBracket = ')';
        char dot = '.';
        for (int i = 0; i < originalDotBracket.length(); i++) {
            if (originalDotBracket.charAt(i) == dot) {
                pairs.add(-1);
            }
            if (originalDotBracket.charAt(i) == openBracket) {
                structureStarts.addFirst(currentOpen);
                pairs.add(currentOpen);
                currentOpen++;

            } else {
                if (originalDotBracket.charAt(i) == closeBracket) {
                    try {
                        currentClose = structureStarts.removeFirst();
                    } catch (java.util.NoSuchElementException e) {
                        MIRCAT_LOGGER.log(Level.SEVERE, "Error read:" + this.originalReadField);
                        MIRCAT_LOGGER.log(Level.SEVERE, "Error struct:" + originalDotBracket);
                        throw e;
                    }
                    pairs.add(currentClose);
                }
            }
        }
        return pairs;
    }

    /**
     *
     * @param direction here is a 1 element array, this is to force java to pass
     * by reference The reason we are passing by reference using int rather than
     * String for example is for a faster comparison after the method call
     * @return
     */
    private synchronized boolean checkValid_miRNA(String toCheck, int[] direction, boolean mirSTAR) {
    //toCheck = "<..<(<<.--<<.";
        //System.out.println("checking valid sequence: " + toCheck);
        boolean findOpenRound = toCheck.contains("(");
        boolean findOpenAngle = toCheck.contains("<");
        boolean findCloseRound = toCheck.contains(")");
        boolean findCloseAngle = toCheck.contains(">");
        if ((findOpenRound || findOpenAngle) && (findCloseRound || findCloseAngle)) {

            //System.out.println("found open and close");
            if (outputLogging) {

                if (!mirSTAR) {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found open and close bracket in miRNA so failed: {0}", toCheck);
                } else {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found open and close bracket in miRNA* so failed: {0}", toCheck);
                }

            }
            return false;
        }

        int consectutiveGaps = 0;
        for (char checking : toCheck.toCharArray()) {
            if ((checking == '.') || (checking == '-')) {
                consectutiveGaps++;
            } else {
                consectutiveGaps = 0;
            }
            if (consectutiveGaps > params.getMaxGaps()) {
                //System.out.println("too many gaps");
                if (outputLogging) {
                    if (!mirSTAR) {
                        MIRCAT_LOGGER.log(Level.WARNING, "Found too many consecutive unpaired bases in miRNA so failed: {0}", toCheck);
                    } else {
                        MIRCAT_LOGGER.log(Level.WARNING, "Found too many consecutive unpaired bases in miRNA* so failed: {0}", toCheck);
                    }
                }
                return false;
            }
        }
        int howMany = 0;
        if ((findOpenRound) || (findOpenAngle)) {
            direction[0] = -1;
            int howManyRound = countOccurrences(toCheck, '(', '0');
            int howManyAngle = countOccurrences(toCheck, '<', '0');
            howMany = howManyRound + howManyAngle;
        } else {
            if ((findCloseRound) || (findCloseAngle)) {
                direction[0] = 1;
                int howManyRound = countOccurrences(toCheck, ')', '0');
                int howManyAngle = countOccurrences(toCheck, '>', '0');
                howMany = howManyRound + howManyAngle;
            }
        }

        int min_paired_to_check = params.getMinPaired();
        if (mirSTAR) {
            min_paired_to_check -= 2;
        }
        if (howMany < min_paired_to_check) {
            //System.out.println("how many too low: " + howMany);
            if (outputLogging) {

                if (mirSTAR) {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found too few paired bases in miRNA so failed: {0}", toCheck);
                } else {
                    MIRCAT_LOGGER.log(Level.WARNING, "Found too few paired bases in miRNA* so failed: {0}", toCheck);
                }

            }
            return false;
        }
        //System.out.println("succeeded tests so far. direction: " + direction[0]);
        return true;
    }

    private synchronized int countOccurrences(String miRNA, char bracket, char option) {
        int count = 0;
        for (int i = 0; i < miRNA.length(); i++) {
            if (!Character.isDigit(option)) {
                if (miRNA.charAt(i) == bracket) {
                    count++;
                }
            } else {
                if ((miRNA.charAt(i) == bracket) || (miRNA.charAt(i) == option)) {
                    count++;
                }

            }
        }
        return count;
    }

    private synchronized void gatherFlanking_miRNA(String miRNA, String firstPart, String endPart, ArrayList<String> extendedSeq) throws HairpinExtensionException {

        if (miRNA.length() == 25) {
            int[] direction
                    = {
                        0
                    };
            if (checkValid_miRNA(miRNA, direction, false)) {
                String extendedHairpin = "";
                if (direction[0] < 0) {
                    //System.out.println("going left");

                    extendedHairpin = extendSeq(miRNA, firstPart, -1);
                    //System.out.println("extended hairpin length 25 LEFT : " + extendedHairpin);
                } else {
                    if (direction[0] > 0) {
                        //System.out.println("going right");
                        extendedHairpin = extendSeq(miRNA, endPart, 1);
                        //System.out.println("extended hairpin length 25 RIGHT : " + extendedHairpin);
                    }
                }

                if (!extendedSeq.contains(extendedHairpin)) {
                    if (!extendedSeq.add(extendedHairpin)) {
                        LOGGER.log(Level.WARNING, "problem adding to extended miRNA list. Memory failure?");
                    }
                }
            }
        } else {
            int amountToAdd = 25 - miRNA.length() + 1;
            int flankAmount = 25 - miRNA.length();

            //flankA
            for (int i = 0;
                    (i < amountToAdd) && (i <= firstPart.length()) && (i <= endPart.length());
                    i++) {
                int usingFlankAmount = flankAmount - i;
                int leftSentinal = 0;
                int rightSentinal = i;
                int offset = 0;

                if (usingFlankAmount > firstPart.length()) {
                    offset = usingFlankAmount - firstPart.length();
                    leftSentinal = firstPart.length() - usingFlankAmount + offset;

                } else {
                    if (usingFlankAmount > endPart.length()) {
                        offset = usingFlankAmount - endPart.length();
                        leftSentinal = firstPart.length() - usingFlankAmount - offset;

                    } else {
                        leftSentinal = firstPart.length() - usingFlankAmount;
                    }
                }

                try {
                    String addingStart = firstPart.substring(leftSentinal);
                    String addingEnd = endPart.substring(0, rightSentinal);
                    String newPotential_miRNA = addingStart + miRNA + addingEnd;

                    int[] direction
                            = {
                                0
                            };
                    if (checkValid_miRNA(newPotential_miRNA, direction, false)) {
                        String extendedHairpin = "";
                        if (direction[0] < 0) {
                            int subStr = firstPart.length() - addingStart.length();
                            extendedHairpin = extendSeq(newPotential_miRNA, firstPart.substring(0, subStr), -1);
                        } else {
                            if (direction[0] > 0) {

                                extendedHairpin = extendSeq(newPotential_miRNA, endPart.substring(addingEnd.length(), endPart.length()), 1);

                            } else {
                                throw new HairpinExtensionException("Hairpin extension error, no hairpin created as neither left or right was available");
                            }
                        }

                        if (!extendedSeq.contains(extendedHairpin)) {
                            if (!extendedSeq.add(extendedHairpin)) {
                                LOGGER.log(Level.WARNING, "problem adding to extended miRNA list. Memory failure?");
                            }
                        }

                    }
                } catch (java.lang.StringIndexOutOfBoundsException e) {
                    // Should we do something here?
                }
            }
        }

    }

    /**
     *
     * @param extendSeq This will either be the start of the sequence up to the
     * miRNA or the end depending on what type of brackets we have found <>()
     * @param direction dictates if we are indexing from the start or the end of
     * extendSeq
     * @return
     */
    private synchronized String extendSeq(String potential_miRNA, String extendSeq, int direction) {
        String extendedSequence = "";
        String extendedSequenceTemp = "";
    //String extendedSequenceTemp2 = "";
        //System.out.println("potential: " + potential_miRNA + "extendSeq: " + extendSeq);
        if (direction < 0) {
            int position = extendSeq.lastIndexOf(")");
      //System.out.println("found the first ) at: " + position);

            if (position > 0) {
                //System.out.println("replacing");
                String subSeq = extendSeq.substring(position + 1, extendSeq.length());
                extendedSequenceTemp = subSeq + potential_miRNA;

            } else {
                extendedSequenceTemp = extendSeq + potential_miRNA;
            }

            extendedSequence = extendedSequenceTemp;

//            int endBracketClose = extendedSequenceTemp2.lastIndexOf(")");
//            int endBracketOpen = extendedSequenceTemp2.lastIndexOf("(");
            //System.out.println("index of close at end: " + endBracketClose + " index of open at end " + endBracketOpen);
            //extendedSequence = extendedSequenceTemp2.substring(0, (Math.max(endBracketOpen, endBracketClose)+1));
        } else {
            int position = extendSeq.indexOf("(");
            //System.out.println("found the first ( at: " + position);

            if (position > 0) {
                String subSeq = extendSeq.substring(0, position);
                int endBracketClose = subSeq.lastIndexOf(")");
                int endBracketOpen = subSeq.lastIndexOf("(");
        //System.out.println("index of close at end: " + endBracketClose + " index of open at end " + endBracketOpen);
                //extendedSequence = extendedSequenceTemp2.substring(0, (Math.max(endBracketOpen, endBracketClose)+1));
                extendedSequenceTemp = potential_miRNA + subSeq.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
            } else {
                if (position == 0) {
                    extendedSequenceTemp = potential_miRNA;
                } else {
                    extendedSequenceTemp = potential_miRNA + extendSeq;
                }
            }

            extendedSequence = extendedSequenceTemp;

        }

    //System.out.println("returned: " + extendedSequence);
        return extendedSequence;
    }
    //Takes extended sequence and folds. Then processes the result

    private String[] getStructure(String windowSeq, int originalLength, int startOffset) throws IOException {

        String[] modifiedDotBracketArray = new String[6];
        //System.out.println( "Sitting here" );
        String result = myExeMan.execRNAFold(windowSeq, "");
    // System.out.println( "Finished exe" );

        if (result == null) {
            //System.out.println( "no structure found" );
            if (outputLogging) {

                MIRCAT_LOGGER.log(Level.WARNING, "RNAfold produced no structure so ignoring");

            }
            return null;
        }

        //Pattern p = Pattern.compile("\\.+");
        String[] results = result.split(" ");
//        System.out.println("ResultT2: " + results[1]);
//        System.out.println("ResultT1: " + results[0]);
//        System.out.println("Result O: " + result);

    //Matcher m = p.matcher(results[0]);
        //if(!m.matches())//regex ensures no strings made of dots will pass
        if ((results.length > 0) && (results[0].contains("(") || results[0].contains(")"))) {
      //System.out.println("match found");

            int firstBracketOpen = results[0].indexOf("(");
            int firstBracketClose = results[0].indexOf(")");

            int lastBracketOpen = results[0].lastIndexOf("(");
            int lastBracketClose = results[0].lastIndexOf(")");

            modifiedDotBracketArray[TRUNCATED_WINDOW_SEQ] = windowSeq.substring(Math.min(firstBracketOpen, firstBracketClose), Math.max(lastBracketOpen, lastBracketClose) + 1);

            String newSeq = "";
            String startSeq = "";
            String endSeq = "";

            try {
                // cut out structure that aligns with mature sequence
                newSeq = results[0].substring(startOffset, (startOffset + (originalLength)));

                // cut out the beginning flanking sequence
                startSeq = results[0].substring(0, startOffset);

        // cut out the end flanking sequence
                //endSeq = results[0].substring( ( startOffset + ( originalLength - 1 ) ), results[0].length() );
                endSeq = results[0].substring((startOffset + (originalLength)), results[0].length());

            } catch (StringIndexOutOfBoundsException e) {
                MIRCAT_LOGGER.log(Level.SEVERE, "Window: " + windowSeq);
                MIRCAT_LOGGER.log(Level.SEVERE, "Result O: " + result);
                throw e;
            }

            String first = newSeq.replace('(', '<');
            String second = first.replace(')', '>');
            String third = second.replace('.', '-');

            String startSeqNoLeadDots = startSeq.replaceFirst("\\.*", "");
            //System.out.println("sorted first string: " + startSeqNoLeadDots);
            modifiedDotBracketArray[FIRST_PART] = startSeqNoLeadDots;

            modifiedDotBracketArray[MODIFIED_REGION] = third;

            int endBracketClose = endSeq.lastIndexOf(")");
            int endBracketOpen = endSeq.lastIndexOf("(");
            //System.out.println("index of close at end: " + endBracketClose + " index of open at end " + endBracketOpen);
            String endSeqNoTrailDots = endSeq.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));
            //System.out.println("modified string: " + endSeqNoTrailDots);
            modifiedDotBracketArray[END_PART] = endSeqNoTrailDots;

            modifiedDotBracketArray[ORIGINAL_RNAFOLD_RESULT] = startSeqNoLeadDots + newSeq + endSeqNoTrailDots;
            modifiedDotBracketArray[ORIGINAL_RNAFOLD_RESULT_NO_MOD] = results[0];

            //System.out.println("string to modify length: " + newSeq.length());
        } else//no structure was found
        {

            if (outputLogging) {

                MIRCAT_LOGGER.log(Level.WARNING, "RNAfold produced no structure so ignoring");

            }
            return null;
        }

        return modifiedDotBracketArray;
        //System.out.println(result);
    }
    //Refolds sequence
    private String[] refold(String hairpinSequence, String fastaHeader) throws IOException {
        String[] resultArray = new String[3];

        // Create file
        String filePath = this.tempDir.getPath() + DIR_SEPARATOR + myThreadID + "hairpinFile.fas";
        FileWriter fstream = new FileWriter(filePath);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(fastaHeader + LINE_SEPARATOR);
        out.write(hairpinSequence);

        out.close();
        fstream.close();
        if (withrandfold) {
            String instruction = " -d " + filePath + " 100";
            String result = myExeMan.execRandFold(instruction);
            //System.out.println( "Result: " + result );
            if (result.contains(" Didn't get expected results")) {
                LOGGER.log(Level.INFO, "HAIRPIN: {0}", hairpinSequence);
                LOGGER.log(Level.INFO, "HEADER: {0}", fastaHeader);
            }
            resultArray = result.split("\t");

        } else {
//            String instruction = " -C " + filePath;
//            String result = myExeMan.execRNAFold(hairpinSequence, instruction);
//            //System.out.println("result: " + result);
//            resultArray = result.split(" ");
//            String temp1 = resultArray[1].replaceAll("\\(", "");
//            String temp2 = temp1.replaceAll("\\)", "");
//            resultArray[1] = temp2;
            
            String instruction = " -C " + filePath;
            String result = myExeMan.execRNAFold(hairpinSequence, "");
            //System.out.println("result: " + result);
            resultArray = result.split(" ");
            String temp1 = resultArray[1].replaceAll("\\(", "");
            String temp2 = temp1.replaceAll("\\)", "");
            resultArray[1] = temp2;

        }
    //Close the output stream

        return resultArray;

    }
    
    // Takes extended full hairpin structure and looks to see if it forms a hairpin and isn't too bulgey

    private synchronized int analyse_structure(String toAnalyse) {

        String toAnalyseNoLeading = toAnalyse.replaceFirst("\\.*", "");

        int endBracketClose = toAnalyseNoLeading.lastIndexOf(")");
        int endBracketOpen = toAnalyseNoLeading.lastIndexOf("(");

        String toAnalyseNoGaps = toAnalyseNoLeading.substring(0, (Math.max(endBracketOpen, endBracketClose) + 1));

        //System.out.println("toanalyse no gaps: " + toAnalyseNoGaps);
        int result = 1;
    //System.out.println("toAnalyseNoGaps: " + toAnalyseNoGaps);
        //System.out.println("toAnalyse: " + toAnalyse);
        //System.out.println("toAnalyseNoLeading: " + toAnalyseNoLeading);
        Pattern p1 = Pattern.compile("^[\\<\\(\\.]{30,200}.{1,40}[\\>\\)\\.]{30,200}$");
        Matcher m1 = p1.matcher(toAnalyseNoGaps);
        Pattern p2 = Pattern.compile("^[\\<\\(\\.]+\\.+[\\>\\)\\.]+");
        Matcher m2 = p2.matcher(toAnalyseNoGaps);
        if ((m2.matches()) && (toAnalyseNoGaps.length() >= params.getMinHairpinLength())) {
            //System.out.println("hairpin is perfect.");
            result = 1;
        } else {
            if (m1.matches() && (toAnalyseNoGaps.length() >= params.getMinHairpinLength())) {

                if (params.getComplexLoops()) {

                    result = 3;
                } else {
                    result = 2;
                }

                //System.out.println("reg ex has passed + " + result);
            } else {
                if (toAnalyse.length() < params.getMinHairpinLength()) {
                    //System.out.println("hairpin is too short so failing");
                    result = 2;
                } else {
                    //System.out.println("none of the above so failing");
                    result = 2;
                }
            }
        }

        int bulge = countOccurrences(toAnalyseNoGaps, '.', '-');

    //int amountOfBrackets = countOccurrences(toAnalyseNoGaps, '(', ')');
        float no = ((float) params.getMaxUnpaired() / 100.0f) * (float) toAnalyseNoGaps.length();

        if (bulge > no) {
            //System.out.println("bulge code causing it to fail");
            result = 2;
        }

        return result;
    }

    private synchronized ArrayList<String> find_mir_star(String hairpinMarked, String fullStructure, String hairpinSequence,
            int newStart, int newEnd,
            ArrayList<Integer> pairs, int miRNALength, String windowSeq,
            ArrayList<String> sequences, boolean[] any_miRNA_pass) throws IOException {

        ArrayList<String> results = new ArrayList<String>();

        int newStartPointer = 0, newEndPointer = 0;
        int miRNA_location = 0;
        int miRNA_locationEnd = 0;
        //offsetStart + pairs.indexOf(startPairData), offsetStart + pairs.lastIndexOf(startPairData)+1

        if (hairpinMarked.contains("<")) {
            miRNA_location = fullStructure.indexOf("<");
            miRNA_locationEnd = miRNA_location + miRNALength;
            int firstPair = pairs.get(miRNA_location);
            int secondPair = pairs.lastIndexOf(firstPair);

            int hairpinStartIndex = secondPair - miRNALength;

            newEndPointer = secondPair;
            newStartPointer = hairpinStartIndex;

        } else {
            if (hairpinMarked.contains(">")) {
                miRNA_location = fullStructure.lastIndexOf(">");
                miRNA_locationEnd = miRNA_location - miRNALength;
                int firstPair = pairs.get(miRNA_location);
                int secondPair = pairs.indexOf(firstPair);

                newEndPointer = secondPair + miRNALength;
                newStartPointer = secondPair;

            } else {

                System.out.println("Seems as if the data entered into find_mir_star is incorrect...");
                return null;
            }
        }

        ArrayList<String> seqToCheckWithPatman = new ArrayList<String>();
        ArrayList<String> mirSTAR_for_validation = new ArrayList<String>();
        for (int i = 0;
                (i < 4) && (newEndPointer + i < fullStructure.length()) && (newStartPointer - i >= 0);
                i++) {

            int startBack = newStartPointer - i;
            int endBack = newEndPointer - i;
            int startForward = newStartPointer + i;
            int endForward = newEndPointer + i;

            try {
                String addFirst = windowSeq.substring(startBack, endBack);
                if (!seqToCheckWithPatman.contains(addFirst)) {
                    seqToCheckWithPatman.add(addFirst);
                }
                addFirst = windowSeq.substring(startForward, endForward);
                if (!seqToCheckWithPatman.contains(addFirst)) {
                    seqToCheckWithPatman.add(addFirst);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack);
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            //seqToCheckWithPatmanForward[0] = windowSeq.substring(startForward, endForward);
            try {
                String toAdd = windowSeq.substring(startBack - 1, endBack);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack - 1, endBack);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startForward - 1, endForward);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }

            try {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward - 1, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startBack + 1, endBack);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack + 1, endBack);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startForward + 1, endForward);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }

            try {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward + 1, endForward);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startBack, endBack - 1);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack - 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startForward, endForward - 1);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward - 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }

            try {
                String toAdd = windowSeq.substring(startBack, endBack + 1);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String temp_potential_miRSTAR_back = fullStructure.substring(startBack, endBack + 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_back)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_back);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }
            try {
                String toAdd = windowSeq.substring(startForward, endForward + 1);
                if (!seqToCheckWithPatman.contains(toAdd)) {
                    seqToCheckWithPatman.add(toAdd);
                }

            } catch (StringIndexOutOfBoundsException e) {
            }

            try {
                String temp_potential_miRSTAR_forward = fullStructure.substring(startForward, endForward + 1);
                if (!mirSTAR_for_validation.contains(temp_potential_miRSTAR_forward)) {
                    mirSTAR_for_validation.add(temp_potential_miRSTAR_forward);
                }
            } catch (StringIndexOutOfBoundsException e) {
            }

        }
        boolean[] validation_results = validate_mir_star(seqToCheckWithPatman, mirSTAR_for_validation, results);
        if (validation_results[0]) {
            any_miRNA_pass[0] = true;
            for (String sequence : seqToCheckWithPatman) {
                if (!sequences.contains(sequence)) {
                    sequences.add(sequence);
                }
            }
            if (validation_results[1]) {
                any_miRNA_pass[1] = true;

            }

        }
        return results;

    }

    private synchronized boolean[] validate_mir_star(ArrayList<String> sequences, ArrayList<String> miRNA_DB, ArrayList<String> readBacks) throws IOException {
        boolean[] result
                = {
                    false, false
                };

        int[] direction
                = {
                    0
                };
        //System.out.println("checking mirSTARS: " + miRNA_DB.size());
        for (String miRNASTAR : miRNA_DB) {
            if (checkValid_miRNA(miRNASTAR, direction, true)) {
                //System.out.println("valid continue");
                result[0] = true;
            }
        }
        int[] total
                = {
                    0
                };
        for (String sequence : sequences) {
      //patmanEntries

            if (patmanEntries.containsKey(sequence)) {

                total[0] += patmanEntries.get(sequence).getHitCount();
                String toAdd = sequence + "(" + patmanEntries.getAbundance(sequence) + ")";
                readBacks.add(toAdd);
            }
        }

        if (total[0] > 0) {
            result[1] = true;
        }

        return result;

    }

    private String markup_mirstar(String hairpinSequence, String hairpin, String mirStarSequence) {
        String newHairpin = hairpin;

        //System.out.println("original: " + newHairpin + " hairpin: " + hairpin + " mirSTARSequence: " + mirStarSequence);
        int location = hairpinSequence.indexOf(mirStarSequence);
        if (location >= 0) {
            String firstChunk = hairpin.substring(0, location);
            String secondChunk = hairpin.substring(location, location + mirStarSequence.length());
            String thirdChunk = hairpin.substring(location + mirStarSequence.length(), hairpin.length());

            String firstReplace = "";
            if (secondChunk.contains("(")) {
                firstReplace = secondChunk.replace('(', '{');
            } else {
                firstReplace = secondChunk.replace(')', '}');
            }

            String markedUpMiRStar = firstReplace.replace('.', '=');

            //System.out.println("first chunk: " + firstChunk + " Second chunk: " + markedUpMiRStar + " thirdChunk: " + thirdChunk);
            newHairpin = firstChunk + markedUpMiRStar + thirdChunk;
            //System.out.println("final hairpin: " + newHairpin);
        }

        return newHairpin;
    }

    private String searchSequence(String hairpin, String sequence, int[] startEnd) {
        String sequenceToUse = "";

        String[] search_miRNA
                = {
                    sequence,
                    sequence.substring(0, sequence.length() - 1),
                    sequence.substring(0, sequence.length() - 2),
                    sequence.substring(0, sequence.length() - 3),
                    sequence.substring(1, sequence.length()),
                    sequence.substring(2, sequence.length()),
                    sequence.substring(3, sequence.length())
                };

        int i = 0;
        while ((startEnd[0] < 0) && i < search_miRNA.length) {
            sequenceToUse = search_miRNA[i];
            startEnd[0] = hairpin.indexOf(sequenceToUse);
            startEnd[1] = startEnd[0] + sequenceToUse.length();

            i++;
        }
        if ((startEnd[0] < 0) || (startEnd[1] < 0)) {
            startEnd[0] = 0;
            startEnd[1] = 0;
        }
        return sequenceToUse;
    }

    private String constructHTMLStrings(String hairpin, String miRNA, String miRSTAR, String overallColour, int[] mirSTAR_Locs) {
        String finalHTMLString = "";
        int[] index
                = {
                    -1, -1
                };
        if (miRSTAR.isEmpty() && !miRNA.isEmpty()) {
            String miRNASearch = searchSequence(hairpin, miRNA, index);
            finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring(0, index[0]);
            finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
            finalHTMLString += hairpin.substring(index[1], hairpin.length()) + "</HTML>";

        } else {
            if (miRSTAR.isEmpty() && miRNA.isEmpty()) {
                finalHTMLString = "<HTML><font color=" + overallColour + ">" + hairpin + "</HTML>";

            } else {

                String miRNASearch = searchSequence(hairpin, miRNA, index);

                int[] star_index
                        = {
                            -1, -1
                        };
                String miRNASTARSearch = searchSequence(hairpin, miRSTAR, star_index);

                if (index[0] < star_index[0]) {

                    finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring(0, index[0]);
                    finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
                    try {
                        finalHTMLString += hairpin.substring(index[1], star_index[0]);
                        finalHTMLString += "<u><font color=#FF0000>" + miRNASTARSearch + "</u><font color=" + overallColour + ">";
                        finalHTMLString += hairpin.substring(star_index[1], hairpin.length()) + "</HTML>";
                    } catch (StringIndexOutOfBoundsException e) {
                        finalHTMLString += hairpin.substring(index[1], hairpin.length()) + "</HTML>";
                    }
                } else {
                    try {
                        finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring(0, star_index[0]);
                        finalHTMLString += "<u><font color=#FF0000>" + miRNASTARSearch + "</u><font color=" + overallColour + ">";
                        finalHTMLString += hairpin.substring(star_index[1], index[0]);
                        finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
                        finalHTMLString += hairpin.substring(index[1], hairpin.length()) + "</HTML>";
                    } catch (StringIndexOutOfBoundsException e) {
                        finalHTMLString += "<HTML><font color=" + overallColour + ">" + hairpin.substring(0, index[0]);
                        finalHTMLString += "<u><font color=#0000FF>" + miRNASearch + "</u><font color=" + overallColour + ">";
                        finalHTMLString += hairpin.substring(index[1], hairpin.length()) + "</HTML>";
                    }
                }
                if (mirSTAR_Locs != null) {
                    mirSTAR_Locs[0] = star_index[0];
                    mirSTAR_Locs[1] = star_index[1];
                }
            }
        }

        return finalHTMLString;
    }
}
