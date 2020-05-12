/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

/**
 *
 * @author keu13sgu
 */
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.*;

import java.io.*;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.MiRCat2Main;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.SizeClassDistribution;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.Params;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.processing.attributes.LFoldThread;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.processing.attributes.RANDFoldThread;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.sRNAUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.AppPaths;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;

/**
 *
 * @author keu13sgu
 */
public final class AttributesExtracter {

    //the selected sRNAs with high abundance
    private Patman sRNAs;
    private Patman allSRNAs;

    private HashMap<sRNA, Patman> sequences;
    private HashMap<sRNA, ArrayList<LFoldInfo>> LFold;
    private HashMap<sRNA, GenomeSequence> genomeSeqs;
    private HashMap<sRNA, ArrayList<Patman>> clusters;
    private HashMap<sRNA, Integer> clusterIndex;
    private HashMap<sRNA, Integer> miRStarIndex;
    private HashMap<sRNA, sRNA> miRStar;

    private boolean printStar = false;

    private final static Runtime JAVA_RUNTIME = Runtime.getRuntime();
    // private File outputFile;
    
    private static String path = null;
    //private static 

   
    
    public AttributesExtracter(Patman sRNAs, Patman allsRNAs) {
        this.sRNAs = sRNAs;
        this.allSRNAs = allsRNAs;

        sequences = new HashMap<>();
        LFold = new HashMap<>();
        genomeSeqs = new HashMap<>();
        clusters = new HashMap<>();
        clusterIndex = new HashMap<>();
        miRStarIndex = new HashMap<>();
        miRStar = new HashMap<>();

//        this.sRNAs.sort();
//        this.allSRNAs.sort();
        
        try {
            if (path == null) {
//                path = MiRCat2Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//                path = URLDecoder.decode(path, "UTF-8");
                path = AppPaths.INSTANCE.getRootDir().getPath();
            }

        } catch (Exception ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }

        readPatmanForSequences(allsRNAs);

        prepareSequences();

    }

    public void processSequences(GenomeSequence genSeq) {
        int i = 0;

        outerloop:
        for (i = 0; i < sRNAs.size(); i++) {
            sRNA s = sRNAs.get(i);
            boolean wasStar = false;
            sRNA before = null;
            
            // check if there is a seq before that can be it's miRStar
            for (int j = i - 1; j >= 0; j--) {
                before = sRNAs.get(j);
                if (before.getEnd() + Params.foldDist < s.getBeg()) {
                    break;
                }

                if (LFold.containsKey(before) && miRStar.containsKey(before)) {
                    sRNA beforeStar = miRStar.get(before);
                    //if it is and is lower abundance, do not process it
                    if (s.equals(beforeStar)) {
                        if (s.getAb() < before.getAb()) {
                            continue outerloop;
                        } else {
                            wasStar = true;
                            break;
                        }
                    }
                }
            }
           // resetAll(s);
            //read the genome sequence
            addGenomeSeq(s, genSeq.getSequence());
            if (!sizeClassDistrOK(s)) {
                continue;
            }
            if (!hasClearCut(s)) {
                continue;
            }

            analyzeSecondaryStructure(s);

            if(Params.execRANDFold)
                filterByRANDFold(s);
            
            if (LFold.containsKey(s) && wasStar) {
                LFold.remove(before);
            }

        }

    }

    public ArrayList<Integer> printResults2(BufferedWriter out, BufferedWriter outMB, BufferedWriter outFolds,
        Patman miRBase, boolean allInfo) throws IOException {

        ArrayList<Integer> totals = new ArrayList<>();

        int count = 0;
        int countMirs = 0;

        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            resetAll(s);

            printOneSRNA(s, out, false, false);

            if (printStar) {
                printOneSRNA(s, out, false, true);
            }
            count++;

            //intersection with mirBase
            if (miRBase != null) {

                boolean isFound = false;
                for (sRNA mir : miRBase) {
                    if ((s.getBeg() - mir.getBeg() >= -Params.clearCut && s.getEnd() - mir.getEnd() <= Params.clearCut)) {

                        isFound = true;
                        if (allInfo) {//&& LFold.get(s).size() > 1
                            printOneSRNA2(s, outMB, outFolds, allInfo, false, false);
                            outMB.append(mir.getSequence() + "\n");
                        }
                        countMirs++;
                        //break;
                    } else {
                        if (printStar && miRStarIndex.containsKey(s)) {

                            sRNA st = sRNAUtils.bestSRNA(clusters.get(s).get(miRStarIndex.get(s)));

                            if ((st.getBeg() - mir.getBeg() >= -Params.clearCut && st.getEnd() - mir.getEnd() <= Params.clearCut)) {
                                isFound = true;
                                if (allInfo && printStar) {
                                    printOneSRNA2(s, outMB, outFolds, allInfo, true, false);
                                    outMB.append(mir.getSequence() + "\n");
                                }
                                //  countMirs++;
                                //break;
                            }
                        }
                    }

                }

                if (!isFound && allInfo) {
                    if (printStar) {
                        printOneSRNA2(s, outMB, outFolds, allInfo, true, true);
                    }
                    printOneSRNA2(s, outMB, outFolds, allInfo, false, true);
                }

                if (allInfo) {
                    outMB.flush();
                    outFolds.flush();
                }
            }
        }

        totals.add(0, count);
        out.flush();

        totals.add(1, countMirs);

        return totals;

    }
    

    //star = if I should print the info of mirStar of s
    private void printOneSRNA(sRNA s, BufferedWriter out, boolean allInfo, boolean star) throws IOException {

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();

        if (allInfo) {

//            if (s.isNegative() && !s.isFlipped()) {
//                flipAllsRNAs(s, b, e); //set begRel and endRel to all sRNA and Patmans
//            }

            if (!star) {
                out.append(s.toStringPatman() + '\n');
                out.append(genomeSeqs.get(s).getSequence() + "\n");
                out.append(s.myToString(b, '#') + "\n");

                for (sRNA ss : sequences.get(s)) {
                    out.append(ss.myToStringSeq(b) + "\n");
                }
                for (LFoldInfo lf : LFold.get(s)) {
                    for (int j = 0; j < lf.getPosition(); j++) {
                        out.append(" ");
                    }
                    out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getpVal() + "\n");
                }
            } else if (miRStarIndex.containsKey(s)) {

                sRNA st = miRStar.get(s);
                out.append(st.toStringPatman() + '\n');
                out.append(genomeSeqs.get(s).getSequence() + "\n");
                out.append(s.myToString(b, '#') + "\n");

                for (sRNA ss : sequences.get(s)) {
                    out.append(ss.myToStringSeq(b) + "\n");

                }
                for (LFoldInfo lf : LFold.get(s)) {
                    for (int j = 0; j < lf.getPosition(); j++) {
                        out.append(" ");
                    }

                    out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getpVal() + "\n");
                }
            }

        } else {

            if (!star) {
                out.append(s.toStringPatman() + "\n");
            } else if (miRStarIndex.containsKey(s)) {

                sRNA st = miRStar.get(s);
                out.append(st.toStringPatman() + "\n");
            }
        }
    }
    
    private void printOneFoldSRNA(sRNA s, BufferedWriter out) throws IOException {

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();


        out.append(s.toStringPatman() + '\n');
        out.append(genomeSeqs.get(s).getSequence() + "\n");
        
        for (LFoldInfo lf : LFold.get(s)) {
            int lfToEnd = e - b - lf.getPosition() - lf.getFold().length();
            for (int j = 0; j < lf.getPosition(); j++) {
                out.append(" ");
            }         
            out.append(lf.getFold());
            for(int i = 0; i < lfToEnd; i++){
                out.append(" ");
            }
            out.append( " " + lf.getAmfe() + "\n");
        }
        out.append(myToStringS(s, b, e, '#') + "\n");       
        for (sRNA ss : sequences.get(s)) {
            out.append(myToStringS(ss, b, e) + "\n");
        }
        
    }
    
     public String myToStringS(sRNA s, int beg, int end, char ch) {
        StringBuilder str = new StringBuilder();
        
        int b ;
        b = s.getBeg();
        
        int difleft = end - b - s.getLength();
        
        for(int i = 0; i< b - beg; i++){
            str.append(".");
        }
        for(int i = 0; i< s.getLength(); i++){
            str.append(ch);
        }
        for(int i = 0; i< difleft; i++){
            str.append(".");
        }
        str.append(" " + s.getAb());
        
        return str.toString();
    }
     
      public String myToStringS(sRNA s, int beg, int end) {
        StringBuilder str = new StringBuilder();
        
        int b ;
        b = s.getBeg();
        
        int difleft = end - b - s.getLength();
        
        for(int i = 0; i< b - beg; i++){
            str.append(".");
        }
        str.append(s.getSequence());
        for(int i = 0; i< difleft; i++){
            str.append(".");
        }
        str.append(" " + s.getAb());
        
        return str.toString();
    }

    private void printOneSRNA2(sRNA s, BufferedWriter out, BufferedWriter outFolds, boolean allInfo, boolean printStar, boolean printEndl) throws IOException {

        if (allInfo) {

            if (!printStar) {
                out.append(LFold.get(s).get(0).getScore() + ",");
                out.append(s.toStringPatman(',') + ',');
                out.append(getHPInfo(s) + ',');
                out.append(LFold.get(s).get(0).getpVal() + ",");
                out.append(getStarInfo(s, printStar) + ",");

                printOneSRNA(s, outFolds, allInfo, false);
                if (printEndl) {
                    out.append(",\n");
                }
            } else {
                if (miRStarIndex.containsKey(s)) {
                    sRNA st = miRStar.get(s);
                    out.append(st.toStringPatman(',') + ',');
                    out.append(getHPInfo(s) + ',');
                    out.append(getStarInfo(s, !printStar) + ",");

                    printOneSRNA(s, outFolds, allInfo, true);
                    if (printEndl) {
                        out.append(",\n");
                    }
                }
            }
        } else {
            if (!printStar) {
                out.append(s.toStringPatman() + "\n");
            } else {
                if (miRStarIndex.containsKey(s)) {
                    sRNA st = miRStar.get(s);
                    out.append(st.toStringPatman() + "\n");
                }
            }
        }
    }

    private String getStarInfo(sRNA s, boolean printStar) {
        StringBuilder str = new StringBuilder();

        if (printStar) {

            str.append(s.getSequence()).append(",");
            str.append(s.getAb()).append(",");
            str.append(s.getBegplus1()).append(",");
            str.append(s.getEndplus1());

        } else if (miRStarIndex.containsKey(s)) {

            sRNA star = miRStar.get(s);
            str.append(star.getSequence()).append(",");
            str.append(star.getAb()).append(",");
            str.append(star.getBegplus1()).append(",");
            str.append(star.getEndplus1());

        } else {
            str.append("N/A,N/A,N/A,N/A");
        }

        return str.toString();
    }

    private String getHPInfo(sRNA s) {
        StringBuilder str = new StringBuilder();
        for (LFoldInfo lf : LFold.get(s)) {
            int b = lf.getPosition();
            int genSeqLen = genomeSeqs.get(s).getSequence().length();

            String seq = genomeSeqs.get(s).getSequence().substring(Math.max(0, b),
                    Math.min(genSeqLen, b + lf.getFold().length()));
                
            if(s.isNegative()){
                seq = SequenceUtils.DNA.reverseComplement(seq);
            }
            str.append(seq).append(",");
            str.append(lf.getPosOnGenome() + 1).append(",");
            str.append(lf.getPosOnGenome() + lf.getFold().length()).append(",");
            str.append(lf.getFold()).append(",");
            str.append(lf.getMfe()).append(",");
            str.append(lf.getAmfe()).append(",");
            //str.append(lf.getpVal()).append(";");
        }
        return str.substring(0, str.length() - 1);
        //str.toString();
    }

//    private void keepOnlyOneWhenOverlapping() {
//        Patman p1 = null;
//        Patman p2 = null;
//
//        for (int i = 0; i < sRNAs.size() - 1; i++) {
//            sRNA s1 = sRNAs.get(i);
//            p1 = clusters.get(s1).get(clusterIndex.get(s1));
//
//            for (int j = i + 1; j < sRNAs.size(); j++) {
//                sRNA s2 = sRNAs.get(j);
//
//                if (!s1.getId().equals(s2.getId())) {
//                    break;
//                }
//                
//                if(s2.getBeg() > s1.getEnd() + Params.SUBWINDOW)
//                    break;
//                
//                try{
//                p2 = clusters.get(s2).get(clusterIndex.get(s2));
//                }
//                catch(Exception e){
//                    System.out.println(s1.toStringPatman() + " **** " + s2.toStringPatman());
//                  //  return;
//                }
//
//                if (p1.equals(p2)) {
//                    if (s1.getAb() > s2.getAb()) {
//                        sRNAs.remove(j);
//                        //if(j > i + 1)
//                        j--;
//                        removeSRNA(s2);
//                    } else {
//                        sRNAs.remove(i);
//                        i--;
//                       // j--;
//                        removeSRNA(s1);
//                        break;
//                    }
//                }
//            }
//        }
//
//    }
    
    private void keepOnlyOneWhenOverlapping() {
        Patman p1 = null;
        Patman p2 = null;

        for (int i = sRNAs.size() - 1; i > 0 ; i--) {
            sRNA s1 = sRNAs.get(i);
             try{
                p1 = clusters.get(s1).get(clusterIndex.get(s1));
                }
            catch(Exception e){
                    System.out.println(s1.toStringPatman() + " **** " );
                  //  return;
                }
            for (int j = i - 1; j >= 0 ; j--) {
                sRNA s2 = sRNAs.get(j);

                if (!s1.getId().equals(s2.getId())) {
                    break;
                }
                
                if(s2.getEnd() < s1.getBeg() - Params.SUBWINDOW)
                    break;
                
                try{
                p2 = clusters.get(s2).get(clusterIndex.get(s2));
                }
                catch(Exception e){
                    System.out.println(s1.toStringPatman() + " **** " + s2.toStringPatman());
                  //  return;
                }

                if (p1.equals(p2)) {
                    if (s1.getAb() > s2.getAb()) {
                        sRNAs.remove(j);
                        //if(j > i + 1)
                        i--;
                        //j++;
                        removeSRNA(s2);
                    } else {
                        sRNAs.remove(i);
                        //i--;
                       // j--;
                        removeSRNA(s1);
                        break;
                    }
                }
            }
        }

    }

//     private void keepOnlyOneWhenOverlapping(sRNA s1) throws IOException {
//        Patman p1 = null;
//        Patman p2 = null;
//        
//       
//        for (int i = 0; i < sRNAs.size(); i++) {
//            sRNA s1 = sRNAs.get(i);
//
//            for (int j = i + 1; j < sRNAs.size(); j++) {
//                sRNA s2 = sRNAs.get(j);
//
//                if (!s1.getId().equals(s2.getId())) {
//                    break;
//                }
//
//                p1 = clusters.get(s1).get(clusterIndex.get(s1));
//                p2 = clusters.get(s2).get(clusterIndex.get(s2));
//
//                if (p1.equals(p2)) {
//                    if (s1.getAb() > s2.getAb()) {
//                        sRNAs.remove(j);
//                        //if(j > i + 1)
//                        j--;
//                        removeSRNA(s2);                        
//                    } else {
//                        sRNAs.remove(i);
//                        i--;
//                        j--;
//                        removeSRNA(s1);
//                        break;
//                    }                
//                }
//            }
//
//        
//      
//
//    }
//    
    private void removeSRNA(sRNA s) {
        if (clusters.containsKey(s)) {
            clusters.remove(s);
        }
        if (clusterIndex.containsKey(s)) {
            clusterIndex.remove(s);
        }
        if (sequences.containsKey(s)) {
            sequences.remove(s);
        }
        if (genomeSeqs.containsKey(s)) {
            genomeSeqs.remove(s);
        }
//        if (miRStar.containsKey(s)) {
//            miRStar.remove(s);
//        }
    }

    public void readPatmanForSequences(Patman allSeqs) {

        int index = 0;
        int indexSeqs = 0;

        for (int i = index; i < sRNAs.size(); i++) {
            sRNA temp2 = sRNAs.get(i);
            if(temp2.getLength() < Params.minLen || temp2.getLength() > Params.maxLen){
                  sRNAs.remove(i);
                  removeSRNA(temp2);
                  i--;
                  continue;
            }
            indexSeqs = addToSequences(temp2, allSeqs, indexSeqs);
        }
    }

    private int addToSequences(sRNA s, Patman seqs, int index) {
        int limB = s.getBeg() - Params.foldDist;
        int limE = s.getEnd() + Params.foldDist;

        Patman selected = new Patman();

        int k = index;
        boolean isSet = false;
        for (int i = index; i < seqs.size(); i++) {

            if (seqs.get(i).getBeg() >= limB && seqs.get(i).getEnd() <= limE) {
                if (!isSet) {
                    isSet = true;
                    k = i;
                }
                selected.add(seqs.get(i));
            }
            if (seqs.get(i).getBeg() > limE) {
                break;
            }
        }
        sequences.put(s, selected);
        return k;
    }

//    private void getSRNAsforChr(StringBuilder sequence) {
//        for (int i = 0; i < sRNAs.size(); i++) {
//            sRNA temp2 = sRNAs.get(i);
//
//            addGenomeSeq(temp2, sequence);
//
//        }
//    }

    private void addGenomeSeq(sRNA s, StringBuilder sequence) {

        //reset sequences and Patmans before using them
        //resetAll(s);

        //Patman begins index from 1, substring begins from 0
        int b = Math.max(0, s.getBeg() - Params.foldDist);
        int e = Math.min(s.getEnd() + Params.foldDist + 1, sequence.length());

        int end = s.getEnd();

        StringBuilder genSeq = new StringBuilder(sequence.substring(b, e));
        e = e - 1;

//        if (s.isNegative()) {
//
//            genSeq = genSeq.reverse();
//            genSeq = sRNAUtils.complement(genSeq);
//            flipAllsRNAs(s, b, e); //set begRel and endRel to all sRNA and Patmans
//        }

        try {
            genomeSeqs.put(s, new GenomeSequence(s.getId(), genSeq, b, e));

        } catch (Exception ex) {
            System.out.println(b + " " + e);

        }
    }

//    private void flipAllsRNAs(sRNA s, int b, int e) {
//        for (sRNA ss : sequences.get(s)) {
//            ss.setBegEndRel(b, e);
//        }
//        
//        for (Patman p : clusters.get(s)) {
//            p.flipBegEndRel(b, e);
//        }
//        
//        flipClustersIndexes(s);
//    }

    private void resetAll(sRNA s) {
        for (sRNA ss : sequences.get(s)) {
            ss.resetBegEndRel();
        }
        
        for (Patman p : clusters.get(s)) {
            p.resetBegEndRel();
        }
        
    }

    //************************************************************
    //*********** LOGIC ******************************************
    //************************************************************
//    public void getSRNAClearCut() {
//
//        for (int i = 0; i < sRNAs.size(); i++) {
//            sRNA s = sRNAs.get(i);
//
//            boolean toBeRemoved = !checkForCleanCut(s, clusterIndex.get(s), Params.clearCutPercent, Params.underClearCut);
//            boolean star = (checkForMiRStarClearCut(s, clusterIndex.get(s), Params.clearCutPercent, Params.underClearCut));
//            if (Params.miRStarPresent || miRStarIndex.containsKey(s)) {
//                toBeRemoved &= !(star && miRStarIndex.containsKey(s));
//            }
//
//            if (toBeRemoved) {
//                sRNAs.remove(i);
//                i--;
//
//            } else {
//
//            }
//
//        }
//
//    }

    public boolean hasClearCut(sRNA s) {

        boolean toBeRemoved = !checkForCleanCut(s, clusterIndex.get(s), Params.clearCutPercent, Params.underClearCut);
        boolean star = true;//(checkForMiRStarClearCut(s, clusterIndex.get(s), Params.clearCutPercent, Params.underClearCut));
        if (Params.miRStarPresent || miRStarIndex.containsKey(s)) {
            toBeRemoved &= !(star && miRStarIndex.containsKey(s));
        }

        if (toBeRemoved) {
            // sRNAs.remove(s);
            //removeSRNA(s);
            //i--;
            return false;
        }

        return true;
    }

    private boolean checkAdiacentClusters(sRNA s, int index) {
        boolean rez = true;
        int abCl = 0;

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();

        for (int i = index - 1; i >= 0; i--) {
            for (sRNA si : clusters.get(s).get(i)) {
                if (si.isSameStrand(s) && si.getEnd() - s.getBeg() > Params.clearCut) {
                    abCl += si.getAb();
                }

            }

        }
        for (int i = index + 1; i < clusters.get(s).size(); i++) {
            for (sRNA si : clusters.get(s).get(i)) {

                if (si.isSameStrand(s) && s.getEnd() - si.getBeg() > Params.clearCut) {
                    abCl += si.getAb();
                }
            }
        }

        rez &= (abCl / (double) clusters.get(s).get(index).getAb()) <= Params.overlapAdiacentCluster;

//        int bestLeft = -1;
//        int bestAb = -1;
//        for (int i = index - 1; i >= 0; i--) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            if (si.getEnd() - s.getBeg() <= Params.clearCut
//                    && s.getBeg() - si.getEnd() < Params.maxDistBetweenClusters) {
//                int ab = clusters.get(s).get(i).getAb();
//                if (ab > bestAb) {
//                    bestAb = ab;
//                    bestLeft = i;
//                }
//
//            }
//        }
//
//        int bestRight = -1;
//        bestAb = -1;
//        for (int i = index + 1; i < clusters.get(s).size(); i++) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            if (s.getEnd() - si.getBeg() <= Params.clearCut
//                    && si.getBeg() - s.getEnd() < Params.maxDistBetweenClusters) {
//                int ab = clusters.get(s).get(i).getAb();
//                if (ab > bestAb) {
//                    bestAb = ab;
//                    bestRight = i;
//                }
//
//            }
//        }
//         if (bestLeft != -1) {
//            rez &= checkAdiactentCluster(s, clusters.get(s).get(bestLeft), index, -1);
//        }
//        
//        if (bestRight != -1) {
//            rez &= checkAdiactentCluster(s, clusters.get(s).get(bestRight), index, 1);
//        }
        return rez;
    }

//    private boolean checkAdiacentMiRSClusters(sRNA s, sRNA mirS, int index) {
//        boolean rez = true;
//        int abCl = 0;
//
//        int b = genomeSeqs.get(s).getBeg();
//        int e = genomeSeqs.get(s).getEnd();
//
//        for (int i = index - 1; i >= 0; i--) {
//            for (sRNA si : clusters.get(s).get(i)) {
//                if (si.isSameStrand(mirS) && si.getEnd() - mirS.getBeg() > Params.clearCut) {
//                    abCl += si.getAb();
//                }
//            }
//        }
//        for (int i = index + 1; i < clusters.get(s).size(); i++) {
//            for (sRNA si : clusters.get(s).get(i)) {
//
//                if (si.isSameStrand(mirS) && mirS.getEnd() - si.getBeg() > Params.clearCut) {
//                    abCl += si.getAb();
//                }
//
//            }
//        }
//
//        rez &= abCl / (double) clusters.get(s).get(index).getAb() <= Params.overlapAdiacentClusterMir;
//
//        return rez;
//    }

    private boolean checkForCleanCut(sRNA s, int index, double param, double paramLow) {

        return cleanCutOnMiRNACluster(s, clusters.get(s).get(index), 0, param, paramLow)
                && checkAdiacentClusters(s, index);

    }

//    private boolean checkForMiRStarClearCut(sRNA s, int index, double param, double paramLow) {
//
//        //find index of mir*
//        int mirSIndex = -1;
//        int ab = -1;
//        double overlap = Integer.MAX_VALUE;
//
//        int b = genomeSeqs.get(s).getBeg();
//        int e = genomeSeqs.get(s).getEnd();
//
//        for (int i = index - 1; i >= 0; i--) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            if (si.getStrand() != s.getStrand()) {
//                continue;
//            }
//            boolean foundMirS
//                    = (s.getBeg() - si.getEnd() > Params.maxDistBetweenClusters
//                    && s.getBeg() - si.getEnd() <= Params.maxDistBetweenMirs);
//
//            if (foundMirS) {
//                int abCl = clusters.get(s).get(i).getAb();
//                double ov = overlap(s, i);
//                if (abCl > ab || (abCl == ab && ov < overlap)) {
//                    mirSIndex = i;
//                    ab = abCl;
//                    overlap = ov;
//                }
//            }
//        }
//
//        for (int i = index + 1; i < clusters.get(s).size(); i++) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            if (si.getStrand() != s.getStrand()) {
//                continue;
//            }
//            boolean foundMirS = //(s.isNegative())? (si.getBeg(b,e) - s.getEnd(b,e) > Params.maxDistBetweenClusters
//                    // && si.getBeg(b,e) - s.getEnd(b,e) <= Params.maxDistBetweenMirs) :
//                    (si.getBeg() - s.getEnd() > Params.maxDistBetweenClusters
//                    && si.getBeg() - s.getEnd() <= Params.maxDistBetweenMirs);
//
//            if (foundMirS) {
//                int abCl = clusters.get(s).get(i).getAb();
//                double ov = overlap(s, i);
//                if (abCl > ab || (abCl == ab && ov < overlap)) {
//                    mirSIndex = i;
//                    ab = abCl;
//                    overlap = ov;
//                }
//            }
//        }
//
//        sRNA mirS;
//        if (mirSIndex != -1) {
//            miRStarIndex.put(s, mirSIndex);
//            mirS = sRNAUtils.bestSRNA(clusters.get(s).get(mirSIndex));
//            return cleanCutOnMiRNACluster(mirS, clusters.get(s).get(mirSIndex), 0, param, paramLow)
//                    && checkAdiacentMiRSClusters(s, mirS, mirSIndex);
//        }
//        return true;
//
//    }

    private double overlap(sRNA s, int index) {

        int abOv = 0;
        int ab = clusters.get(s).get(index).getAb();

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();

        int cei = clusters.get(s).get(index).getEnd();//(s.isNegative())? clusters.get(s).get(index).getEnd(b, e) : clusters.get(s).get(index).getEnd();
        int cbi = clusters.get(s).get(index).getBeg();//(s.isNegative())? clusters.get(s).get(index).getBeg(b, e) : clusters.get(s).get(index).getBeg();

        for (int i = 0; i < clusters.get(s).size(); i++) {
            if (i != index) {

                int ce = clusters.get(s).get(i).getEnd();//(s.isNegative())? clusters.get(s).get(i).getEnd(b, e) : clusters.get(s).get(i).getEnd();
                int cb = clusters.get(s).get(i).getBeg();//(s.isNegative())? clusters.get(s).get(i).getBeg(b, e) : clusters.get(s).get(i).getBeg();

                if ((ce - cbi > 0 && cb <= cbi)
                        || (cei - cb > 0 && cbi <= cb)) {
                    abOv += clusters.get(s).get(i).getAb();
                }
            }

        }
        return abOv / (double) ab;
    }

//    private boolean cleanCutOnMiRNASCluster2(sRNA s, int index) {
//        int mirSIndex = -1;
//        int ab = -1;
//        double overlap = Integer.MAX_VALUE;
//
//        int b = genomeSeqs.get(s).getBeg();
//        int e = genomeSeqs.get(s).getEnd();
//
//        for (int i = index - 1; i >= 0; i--) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            boolean foundMirS = //s.isNegative()? (s.getBeg(b,e) - si.getEnd(b,e) > Params.maxDistBetweenClusters
//                    //&& s.getBeg(b,e) - si.getEnd(b,e) <= Params.maxDistBetweenMirs):
//                    (s.getBeg() - si.getEnd() > Params.maxDistBetweenClusters
//                    && s.getBeg() - si.getEnd() <= Params.maxDistBetweenMirs);
//
//            if (foundMirS) {
//                int abCl = clusters.get(s).get(i).getAb();
//                double ov = overlap(s, i);
//                if (ov < overlap && abCl > ab) {
//                    mirSIndex = i;
//                    ab = abCl;
//                    overlap = ov;
//                }
//            }
//        }
//
//        for (int i = index + 1; i < clusters.get(s).size(); i++) {
//            sRNA si = sRNAUtils.bestSRNA(clusters.get(s).get(i));
//            boolean foundMirS =// s.isNegative()? ((si.getBeg(b,e) - s.getEnd(b,e) > Params.maxDistBetweenClusters
//                    //  && si.getBeg(b,e) - s.getEnd(b,e) <= Params.maxDistBetweenMirs)):
//                    (si.getBeg() - s.getEnd() > Params.maxDistBetweenClusters
//                    && si.getBeg() - s.getEnd() <= Params.maxDistBetweenMirs);
//
//            if (foundMirS) {
//                int abCl = clusters.get(s).get(i).getAb();
//                double ov = overlap(s, i);
//                if (ov < overlap && abCl > ab) {
//                    mirSIndex = i;
//                    ab = abCl;
//                    overlap = ov;
//                }
//            }
//        }
//
//        sRNA mirS;
//        if (mirSIndex != -1) {
//            mirS = sRNAUtils.bestSRNA(clusters.get(s).get(mirSIndex));
//
//            cleanCutOnMiRNACluster2(s, mirS, clusters.get(s).get(mirSIndex), 0);
//
//        }
//
//        return true;
//
//    }

//    private boolean checkForCleanCut2(sRNA s) {
//        if (clusters.containsKey(s)) {
//            cleanCutOnMiRNACluster2(s, s, clusters.get(s).get(clusterIndex.get(s)), 0);
//            checkAdiacentClusters(s, clusterIndex.get(s));
//            cleanCutOnMiRNASCluster2(s, clusterIndex.get(s));
//        }
//        return false;
//    }

    private boolean cleanCutOnMiRNACluster(sRNA s, Patman p, int dir, double param, double paramLow) {

        int beg = s.getBeg();//sRel.isNegative()? s.getBeg(b, e): s.getBeg();
        int end = s.getEnd();//sRel.isNegative()? s.getEnd(b, e): s.getEnd();

        boolean rez = true;

        int sumMAS = 0;
        int sumOutside = 0;
        double totalCut = 0;
        if (dir == 0) {
            for (sRNA sp : p) {

                int bsp = sp.getBeg();//sRel.isNegative()? sp.getBeg(b, e) : sp.getBeg();
                int esp = sp.getEnd();//sRel.isNegative()? sp.getEnd(b, e) : sp.getEnd();

                if (Math.abs(bsp - beg) <= Params.clearCut && Math.abs(esp - end) <= Params.clearCut) {
                    sumMAS += sp.getAb();
                } else {
                    sumOutside += sp.getAb();
                }
            }

            totalCut = sumMAS / (double) (sumMAS + sumOutside);
        }

        double rightCut = 0;
        int rightSumMAS = 0;
        int rightSumOutside = 0;

        if (dir == 0 || dir < 0) {
            for (sRNA sp : p) {
                int bsp = sp.getBeg();//sRel.isNegative()? sp.getBeg(b, e) : sp.getBeg();

                if (Math.abs(bsp - beg) <= Params.clearCut) {
                    rightSumMAS += sp.getAb();
                } else {
                    rightSumOutside += sp.getAb();
                }
            }
            rightCut = rightSumMAS / (double) (rightSumMAS + rightSumOutside);
        }

        double leftCut = 0;
        int leftSumMAS = 0;
        int leftSumOutside = 0;

        if (dir == 0 || dir > 0) {
            for (sRNA sp : p) {
                int esp = sp.getEnd();//sRel.isNegative()? sp.getEnd(b, e) : sp.getEnd();

                if (Math.abs(esp - end) <= Params.clearCut) {
                    leftSumMAS += sp.getAb();
                } else {
                    leftSumOutside += sp.getAb();
                }
            }

            leftCut = leftSumMAS / (double) (leftSumMAS + leftSumOutside);
        }

        if (dir == 0) {
            rez &= totalCut >= paramLow
                    && (rightCut >= param || leftCut >= param);
        }

        if (dir < 0) {
            rez &= rightCut >= param;
        }
        if (dir > 0) {
            rez &= leftCut >= param;
        }

        return rez;
    }

    private void cleanCutOnMiRNACluster2(sRNA sRel, sRNA s, Patman p, int dir) {
        int b = genomeSeqs.get(sRel).getBeg();
        int e = genomeSeqs.get(sRel).getEnd();

        int beg = s.getBeg();//sRel.isNegative()? s.getBeg(b, e): s.getBeg();
        int end = s.getEnd();//sRel.isNegative()? s.getEnd(b, e): s.getEnd();

        boolean rez = true;

        int sumMAS = 0;
        int sumOutside = 0;
        double totalCut = 0;
        if (dir == 0) {
            for (sRNA sp : p) {

                int bsp = sp.getBeg();//sRel.isNegative()? sp.getBeg(b, e) : sp.getBeg();
                int esp = sp.getEnd();//sRel.isNegative()? sp.getEnd(b, e) : sp.getEnd();

                if (Math.abs(bsp - beg) <= Params.clearCut && Math.abs(esp - end) <= Params.clearCut) {
                    sumMAS += sp.getAb();
                } else {
                    sumOutside += sp.getAb();
                }
            }

            totalCut = sumMAS / (double) (sumMAS + sumOutside);
        }

        double rightCut = 0;
        int rightSumMAS = 0;
        int rightSumOutside = 0;

        if (dir == 0 || dir < 0) {
            for (sRNA sp : p) {
                int bsp = sp.getBeg();//sRel.isNegative()? sp.getBeg(b, e) : sp.getBeg();

                if (Math.abs(bsp - beg) <= Params.clearCut) {
                    rightSumMAS += sp.getAb();
                } else {
                    rightSumOutside += sp.getAb();
                }
            }
            rightCut = rightSumMAS / (double) (rightSumMAS + rightSumOutside);
        }

        double leftCut = 0;
        int leftSumMAS = 0;
        int leftSumOutside = 0;

        if (dir == 0 || dir > 0) {
            for (sRNA sp : p) {
                int esp = sp.getEnd();//sRel.isNegative()? sp.getEnd(b, e) : sp.getEnd();

                if (Math.abs(esp - end) <= Params.clearCut) {
                    leftSumMAS += sp.getAb();
                } else {
                    leftSumOutside += sp.getAb();
                }
            }

            leftCut = leftSumMAS / (double) (leftSumMAS + leftSumOutside);
        }

        if (dir == 0) {
            rez &= rightCut >= Params.clearCutPercent || leftCut >= Params.clearCutPercent;
        }

        if (dir < 0) {
            rez &= rightCut >= Params.clearCutPercent;
        }
        if (dir > 0) {
            rez &= leftCut >= Params.clearCutPercent;
        }

    }

    public void analyzeSecondaryStructure(sRNA s) {
        try {

            String decodedPath = path;
            String rnaFoldPath = "";
            //String miRCat2ToolName = "miRCat2.0";
            
            //decodedPath = decodedPath.substring(1, decodedPath.indexOf("classes"));
//            decodedPath += miRCat2ToolName + "/dependencies/";
            decodedPath +=  "/ExeFiles/";
            
            if (sRNAUtils.isWindows()) {   
                rnaFoldPath = decodedPath + "win/RNALfold.exe";
            }
            else if (sRNAUtils.isUnix()){
                rnaFoldPath = decodedPath + "linux/RNALfold";
            }else if(sRNAUtils.isMac()){
                rnaFoldPath = decodedPath + "OSX/RNALfold";
            }

            String[] command = new String[] {
                rnaFoldPath, 
                "-L", 
                Integer.toString(Params.lFoldL)};
            
            Process process = JAVA_RUNTIME.exec(command);
            
           
            
            StringBuilder genSeq =  genomeSeqs.get(s).getSequence();
            String genSeq1 = genSeq.toString();
            int genBeg = genomeSeqs.get(s).getBeg();
            
            if(s.isNegative()){
                genSeq1 = SequenceUtils.DNA.reverseComplement(genSeq1);
            }
//                genSeq = genSeq.reverse();
//                genSeq = sRNAUtils.complement(genSeq);
//            }
            
//            if(!s.equals(sRNA.readFromPatman("17	CAGGCTGGTTAGATGGTTGTCA(197)	22657131	22657152	+	0"))){
//               return;
//            }
            
            ArrayList<LFoldInfo> executeLFold = executeLFold(process, genSeq1, s.isNegative(), genBeg);
            process.destroy();
            LFold.put(s, executeLFold);

            //****************************************************
            keepValidFolds(s);

        } catch (Exception ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void keepValidFolds(sRNA s) throws Exception {

         
//        
        removeFoldsTooShort(s);
        removeFoldsBadAmfe(s);       

//        if(s.equals(sRNA.readFromPatman("4	TAAGTGCTTCTCTTTGGGGTAG(4597)	28001570	28001591	+	0"))){
//            System.out.print("Checking folds: " + s.toStringPatman() + "\n");
//            System.out.print("**************************************************************\n\n\n");
//
//            print(s, LFold);
//            System.out.flush();
//        }
       
        
        determineDicerParts(s);

        removeFoldsNoCoverage2(s);
        removeFoldsTooManyGaps(s);
        
        removeFoldsNotAHairpin(s);
        //removeMiRNAsBasedOnLength(s);

        removeFoldsParedPercent(s);
        removeFoldsParedNucl(s);
        removeFoldsBasedOnOrientation(s);
        //removeFoldsBasedOnCGContent(s);

        selectBestFold(s);
        checkFuzzy(s);

    }

    private void selectBestFold(sRNA s) {
        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFoldInfo> folds = LFold.get(s);
        // int shortest = 0;
        int bestAmfe = 0;

        for (int i = 1; i < folds.size(); i++) {
            LFoldInfo lf = folds.get(i);
            if (lf.getAmfe() < folds.get(bestAmfe).getAmfe()) {
                bestAmfe = i;
            }

        }

        LFoldInfo lf = folds.get(bestAmfe);
        LFold.put(s, new ArrayList<>());
        LFold.get(s).add(lf);

    }

    private void print(sRNA s, HashMap<sRNA, ArrayList<LFoldInfo>> LFold1) {

     if (!LFold1.containsKey(s)) {
            return;
        }
        
        PrintStream out = System.out;

        out.append(genomeSeqs.get(s).getSequence().toString());
        out.append("\n");

        int b = genomeSeqs.get(s).getBeg();
        

        out.append(s.myToStringSeq(b));
        out.append("\n");

        for (sRNA ss : sequences.get(s)) {
            if (ss.getAb() > 0) {
                out.append(ss.myToStringSeq(b) );
                out.append("\n");
            }
        }

        for (int k = 0; k < LFold1.get(s).size(); k++) {
            LFoldInfo lf = LFold1.get(s).get(k);
            for (int i = 0; i < lf.getPosition(); i++) {
                out.append(" ");
            }
            out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getMfe());
            out.append("\n");
        }
    }

    private ArrayList<LFoldInfo> executeLFold(final Process process, final String toFold, boolean isNegative, int beg) throws IOException, Exception {

        LFoldThread res = new LFoldThread(process, toFold, toFold.length(), isNegative, beg);
        ArrayList<LFoldInfo> folds = res.call();
        return folds;
    }

    private void removeMiRNAsBasedOnLength() {
        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (!checkMiRNALength(folds.get(i), s)) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeMiRNAsBasedOnLength(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!checkMiRNALength(folds.get(i), s)) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private boolean checkMiRNALength(LFoldInfo lf, sRNA s) {

        if (lf.getEm() < Params.minLen) {
            return false;
        }
        if (lf.getFold().length() - lf.getBms() < Params.minLen) {
            return false;
        }

        if ((lf.getEm() - lf.getBm() + 1 >= Params.minLen && lf.getEm() - lf.getBm() + 1 <= Params.maxLen
                && (lf.getEms() - lf.getBms() + 1 >= Params.minLen ))
                ||
               (lf.getEms() - lf.getBms() + 1 >= Params.minLen && lf.getEms() - lf.getBms() + 1 <= Params.maxLen  
                && lf.getEm() - lf.getBm() + 1 >= Params.minLen)){
            return true;
        }
        return false;

    }

    private boolean checkOrientation(LFoldInfo lf, sRNA s, double minOr) {

        ArrayList<Patman> cl = clusters.get(s);
        int plus = 0;
        int minus = 0;
        int total = 0;

        for (Patman p : cl) {
            if (isOnFold(lf, p, s)) {
                for (sRNA ss : p) {

                    if (ss.getStrand() == '+') {
                        plus += ss.getAb();
                    } else {
                        minus += ss.getAb();
                    }
                }
            }
        }

        total = plus + minus;

        return plus / (double) total >= minOr || minus / (double) total >= minOr;
    }

    private boolean isOnFold(LFoldInfo lf, Patman p, sRNA sRef) {
        sRNA s = p.mostAbundantSRNA();

        int bg = genomeSeqs.get(sRef).getBeg();
        int eg = genomeSeqs.get(sRef).getEnd();

        if (s == null) {
            int b = p.getBeg();//(sRef.isNegative())? p.getBeg(bg,eg): p.getBeg();
            int e = p.getEnd();//(sRef.isNegative())? p.getEnd(bg,eg): p.getEnd();

            return b - lf.getPosOnGenome() >= 0 - Params.outsideHP
                    && e - lf.getPosOnGenome() <= lf.getFold().length() + Params.outsideHP;
        }

//        return ((sRef.isNegative())? s.getBeg(bg,eg): s.getBeg()) - lf.getPosOnGenome() >= 0 - Params.outsideHP
//                && ((sRef.isNegative())? s.getEnd(bg,eg): s.getEnd()) - lf.getPosOnGenome() <= lf.getFold().length() + Params.outsideHP;
        return s.getBeg() - lf.getPosOnGenome() >= 0 - Params.outsideHP
                && s.getEnd() - lf.getPosOnGenome() <= lf.getFold().length() + Params.outsideHP;
    }

    private boolean hasSRNAWithLength(Patman p, int min, int max) {
        for (sRNA s : p) {
            if (s.getLength() >= min && s.getLength() <= max) {
                return true;
            }
        }
        return false;
    }

    private void removeFoldsNoCoverage2() {
        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            GenomeSequence g = genomeSeqs.get(s);
            int gb = g.getBeg();
            int ge = g.getEnd();

            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                LFoldInfo lf = folds.get(i);

                if (miRStarIndex.containsKey(s)) {
                    sRNA ms = clusters.get(s).get(miRStarIndex.get(s)).mostAbundantSRNA();
                    int msB = ms.getBeg();//(s.isNegative())? ms.getBeg(gb, ge) : ms.getBeg();
                    int msE = ms.getEnd();//(s.isNegative())? ms.getEnd(gb, ge) : ms.getEnd();

                    if (!isOnHairpin(msB, msE, lf.getPosOnGenome(), lf.getPosOnGenome() + lf.getFold().length())) {
                        folds.remove(i);
                        i--;
                    }
                }
            }

            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsNoCoverage2(sRNA s) throws IOException {

        if (!LFold.containsKey(s)) {
            return;
        }

        GenomeSequence g = genomeSeqs.get(s);
        int gb = g.getBeg();
        ArrayList<LFoldInfo> folds = LFold.get(s);

        boolean hasMirSCluster = false;
        int miRSIndex = -1;
        //  ArrayList<Integer> starIndexes = new ArrayList<>();

        for (int i = 0; i < folds.size(); i++) {

            LFoldInfo lf = folds.get(i);

            int indexMiRS = getClusterMirS(s, lf);
            if (indexMiRS != -1) {
                hasMirSCluster = true;
                lf.setMiRStarSeq(indexMiRS);
            }

//                if(!isOnHairpin(ms.getBeg(), ms.getEnd(), lf.getPosOnGenome(), lf.getPosOnGenome() + lf.getFold().length())){
//                    folds.remove(i);
//                    i--;
//                }
        }

        if (hasMirSCluster) {
            ArrayList<Integer> starIndexes = new ArrayList<>();

            for (int i = 0; i < folds.size(); i++) {

                LFoldInfo lf = folds.get(i);
                if (lf.getMiRStarSeq() == -1) {
                    folds.remove(i);
                    i--;
                } else {
                    if (!starIndexes.contains(lf.getMiRStarSeq())) {
                        starIndexes.add(lf.getMiRStarSeq());
                    }
                }

            }

            int index = -1;
            if (starIndexes.size() > 0) {
                index = getBestCluster(s, starIndexes);
            }
            if(index != -1){
                    sRNA mirStar = null;
                    sRNA next = null;
                     for (int i = 0; i < folds.size(); i++) {

                        LFoldInfo lf = folds.get(i);
                        if(lf.getMiRStarSeq() != index){
                             folds.remove(i);
                             i--;
                        }
                        else{
                            next = getBestMirStar(clusters.get(s).get(index), lf);
                            if(mirStar == null)
                                mirStar = next;
                            else{
                                if(next.getAb() > mirStar.getAb())
                                    mirStar = next;
                            }
                            
                            if(!checkMiRStar(lf, next)){
                                folds.remove(i);
                                i--;
                                continue;
                            }
                            
                            if(lf.getMiRNASide() == -1){
                                lf.setBms(next.getBeg() - lf.getPosOnGenome());
                                lf.setEms(Math.min(next.getEnd() - lf.getPosOnGenome(), lf.getFold().length() -1));
                            }
                            else if(lf.getMiRNASide() == 1){
                                lf.setBm(Math.max(0,next.getBeg() - lf.getPosOnGenome()));
                                lf.setEm(next.getEnd() - lf.getPosOnGenome());
                            }
                        }
                    }
                miRStar.put(s, sRNAUtils.bestSRNA(clusters.get(s).get(index)));
                miRStarIndex.put(s, index);
            } else {
                for (int i = 0; i < folds.size(); i++) {
                    folds.remove(i);
                    i--;
                }
            }

        } else {
            if (Params.miRStarPresent) {
                for (int i = 0; i < folds.size(); i++) {
                    folds.remove(i);
                    i--;
                }
            }
        }

        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private int getBestCluster(sRNA s, ArrayList<Integer> indexes) throws IOException {
//        cleanCutOnMiRNACluster(mirS, clusters.get(s).get(mirSIndex), 0, param, paramLow)
//                    && checkAdiacentMiRSClusters(s, mirS, mirSIndex);

        int bestAb = -1;
        int index = -1;

        for (Integer i : indexes) {
            Patman p = clusters.get(s).get(i);
            sRNA mirS = sRNAUtils.bestSRNA(p);

//            if(cleanCutOnMiRNACluster(mirS, p, 0, Params.clearCutPercent, Params.underClearCut)
//                    && checkAdiacentMiRSClusters(s, mirS, i)){
            int pAb = p.getAb();

            if (pAb > bestAb) {
                bestAb = pAb;
                index = i;
            }
            //}
        }

        return index;

    }

    private int getClusterMirS(sRNA s, LFoldInfo lf) {
        int bs = lf.getOtherMirBeg(s) + lf.getPosOnGenome();
        int es = lf.getOtherMirEnd(s) + lf.getPosOnGenome();

        if (bs == -1 || es == -1) {
            return -1;
        }

        int index = clusterIndex.get(s);
        int indexMS = -1;

        int maxAb = 0;

        if (s.getBeg() < bs) {

            for (int i = clusters.get(s).size() - 1; i >= index + 1; i--) {

                Patman p = clusters.get(s).get(i);

                if (sRNAUtils.isInSameCluster(p, bs, es)) {
                    int pAb = p.getAb();
                    if (pAb >= maxAb) {
                        maxAb = pAb;
                        indexMS = i;
                    }

                    // break;
                } else if (p.getEnd() < bs) {
                    break;
                }
            }
        } else {
            for (int i = 0; i <= index - 1; i++) {

                Patman p = clusters.get(s).get(i);

                if (sRNAUtils.isInSameCluster(p, bs, es)) {
                    int pAb = p.getAb();
                    if (pAb > maxAb) {
                        maxAb = pAb;
                        indexMS = i;
                    }
                } else if (p.getBeg() > es) {
                    break;
                }
            }
        }

        if (indexMS != -1) {
            sRNA b = sRNAUtils.bestSRNA(clusters.get(s).get(indexMS));
            if (Math.abs(s.getBeg() - b.getEnd()) <= Params.minLoop || Math.abs(s.getEnd() - b.getBeg()) <= Params.minLoop) {
                return -1;
            }
        }

        return indexMS;
    }

//    private void removeFoldsNoCoverage2(sRNA s) {
//
//            if (!LFold.containsKey(s)) {
//                return;
//            }
//
//            GenomeSequence g = genomeSeqs.get(s);
//            int gb = g.getBeg();
//            int ge = g.getEnd();
//            
//            
//            
//            ArrayList<LFoldInfo> folds = LFold.get(s);
//
//            for (int i = 0; i < folds.size(); i++) {
//
//                LFoldInfo lf = folds.get(i);
//                
//                
//                if(miRStarIndex.containsKey(s)){
//                    sRNA ms = clusters.get(s).get(miRStarIndex.get(s)).mostAbundantSRNA();
//                    int msB = ms.getBeg();//(s.isNegative())? ms.getBeg(gb, ge) : ms.getBeg();
//                    int msE = ms.getEnd();//(s.isNegative())? ms.getEnd(gb, ge) : ms.getEnd();
//                    
//                    if(!isOnHairpin(msB, msE, lf.getPosOnGenome(), lf.getPosOnGenome() + lf.getFold().length())){
//                        folds.remove(i);
//                        i--;
//                    }
//                } 
//            }
//
//            
//            if (LFold.get(s).isEmpty()) {
//                LFold.remove(s);
//            }
//        
//    }
//    private Integer getCovered(int bLF, int eLF, sRNA s) {
//        int cov = 0;
//
//        for (sRNA ss : sequences.get(s)) {
//            if (ss.getBeg() >= bLF && ss.getEnd() <= eLF) {
//                cov += ss.getAb();
//            }
//        }
//        return cov;
//    }
    private void determineDicerParts() {
        for (sRNA s : LFold.keySet()) {

            GenomeSequence g = genomeSeqs.get(s);
            int gb = g.getBeg();
            int ge = g.getEnd();

            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {
                LFoldInfo lf = folds.get(i);
                int bLF = gb + lf.getPosition();

                lf.setPosOnGenome(bLF);
                int b = s.getBeg();//s.isNegative()? s.getBeg(gb, ge):s.getBeg();
                int e = s.getEnd();//s.isNegative()? s.getEnd(gb, ge):s.getEnd();

//                if((lf.getPosOnGenome() - Params.outsideHP) <= s.getBeg() &&
//                        (lf.getPosOnGenome() + lf.getFold().length() + Params.outsideHP) >= s.getEnd()){
                if ((lf.getPosOnGenome()) <= b
                        && (lf.getPosOnGenome() + lf.getFold().length() - 1) >= e) {

                    determineDicerPartsOnLF(lf, s, clusters.get(s));
                } else {
                    folds.remove(i);
                    i--;
                }
            }

        }
    }

    private void determineDicerParts(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        GenomeSequence g = genomeSeqs.get(s);
        int gb = g.getBeg();

        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {
            LFoldInfo lf = folds.get(i);
            int bLF = gb + lf.getPosition();

            lf.setPosOnGenome(bLF);
//                if((lf.getPosOnGenome() - Params.outsideHP) <= s.getBeg() &&
//                        (lf.getPosOnGenome() + lf.getFold().length() + Params.outsideHP) >= s.getEnd()){
            if ((lf.getPosOnGenome()) <= s.getBeg()
                    && (lf.getPosOnGenome() + lf.getFold().length() - 1) >= s.getEnd()) {
                if (!determineDicerPartsOnLF(lf, s, clusters.get(s))) {
                    folds.remove(i);
                    i--;
                }
            } else {
                folds.remove(i);
                i--;
            }
        }

        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private boolean determineDicerPartsOnLF(LFoldInfo lf, sRNA s, ArrayList<Patman> clusters) {
        determineLoop2(lf);

        if (!determineMiRNAs(lf, s, clusters)) {
            return false;
        }
        if (!setStarSequence2(lf, s.isNegative())) {
            return false;
        }

        //check to see if it doesn't overlap with the loop too much         
//        if (lf.getBms() - lf.getEm() < Params.minLoop) {
//            return false;
//        }

        return true;

    }

    private boolean setStarSequence2(LFoldInfo lf, boolean isNegative) {
         boolean onLeft = true;
         
         int overhang = Params.threePrimeOverhang;
         
//         if(isNegative){
//             overhang = - Params.threePrimeOverhang;
//         }
//         else overhang = Params.threePrimeOverhang;
                 
         
       if (lf.getBm() == -1 && lf.getBms() != -1) { //on right
            int bm = lf.getPairPosition(lf.getEms(), !onLeft) - overhang;
            
            if(bm < 0)
                return false;
            
            lf.setBm(bm);
            int em = lf.getPairPosition(lf.getBms(), !onLeft) + overhang;//bm + (lf.getEms() - lf.getBms() );
            
//            if(em > lf.getBl() + Params.clearCut)
//                return false;
            
            
            lf.setEm(em);
            return !containsLoop(lf.getFold().substring(lf.getBm(), lf.getEm() + 1));
        }
        if (lf.getBms() == -1 && lf.getBm() != -1) { //on left
            int ems = lf.getPairPosition(lf.getBm(), onLeft) + overhang;
            
            if(ems >= lf.getFold().length())
                return false;
            
            int bms = lf.getPairPosition(lf.getEm(), onLeft) - overhang;//ems - (lf.getEm() - lf.getBm() )
            
//            if(bms < lf.getEl() - Params.clearCut)
//                return false;
            
            lf.setBms(bms);
            lf.setEms(ems); 
            return !containsLoop(lf.getFold().substring(lf.getBms(), lf.getEms() + 1));
        }
        


        return true;
    }

    private void determineLoop2(LFoldInfo lf) {

        int poz1 = 0;
        int poz2 = lf.getFold().length() - 1;

        for (int i = 0; i < lf.getFold().length(); i++) {
            char ch = lf.getFold().charAt(i);
            if (ch == '(') {
                poz1 = i;
            } else if (ch == ')') {
                break;
            }
        }

        for (int i = lf.getFold().length() - 1; i >= 0; i--) {
            char ch = lf.getFold().charAt(i);
            if (ch == ')') {
                poz2 = i;
            } else if (ch == '(') {
                break;
            }
        }
        lf.setBl(poz1);
        lf.setEl(poz2);

    }

    private boolean determineMiRNAs(LFoldInfo lf, sRNA s, ArrayList<Patman> clusters) {

        int posGen = lf.getPosOnGenome();

        int allowedInLoop = Params.inLoopSmall;
        //int allowedOutside = Params.outsideHP;

//        if (lf.getEl() - lf.getBl() + 1 > 10) {
//            allowedInLoop = Params.inLoopBig;
//        }
        int bl = lf.getBl() + posGen + allowedInLoop;
        int el = lf.getEl() + posGen - allowedInLoop;

        int part = 0;
        int over = 0;

        if (s.getEnd() <= bl && s.getBeg() >= posGen) { //- allowedOutside
            over = posGen - s.getBeg();
//            if (over > allowedOutside) {
//                return;
//            }
            if (over > 0) {
                return false;
            }
        } else if (s.getBeg() >= el && s.getEnd() <= posGen + lf.getFold().length()) { //+ allowedOutside) {
            over = s.getEnd() - posGen - lf.getFold().length();

//            if (over > allowedOutside) {
//                return;
//            }
            if (over > 0) {
                return false;
            }
            part = 1;
        } else {
            return false;
        }

//        if (over > 0) {
//            String str = lf.getFold();
//            String add = "";
//
//            for (int i = 0; i < allowedOutside; i++) {
//                add += ".";
//            }
//
//            lf.setFold(add + str + add);
//            lf.setPosition(lf.getPosition() - allowedOutside);
//            lf.setPosOnGenome(posGen - allowedOutside);
//            posGen = lf.getPosOnGenome();
//
//            bl = lf.getBl() + posGen + allowedInLoop;
//            el = lf.getEl() + posGen - allowedInLoop;
//        }
        if (s.getEnd() <= bl && s.getBeg() >= posGen) {

            lf.setBm(s.getBeg() - posGen);
            lf.setEm(Math.min(bl - posGen, s.getEnd() - posGen));
            
            lf.setMiRNASide(-1);
        } else if (s.getBeg() >= el && s.getEnd() <= posGen + lf.getFold().length() - 1) {

            lf.setBms(Math.max(el - posGen, s.getBeg() - posGen));
            lf.setEms(s.getEnd() - posGen);
            
            lf.setMiRNASide(1);

            // part = 1;
        } else {
            return false;
        }

        return true;

//        sRNA closest1 = null;
//        int dif = Integer.MAX_VALUE;
//        
//        if(miRStarIndex.containsKey(s)){
//            closest1 = clusters.get(miRStarIndex.get(s)).mostAbundantSRNA();
//        }
//        for (Patman p : clusters) {
//            sRNA mostAb;
//            mostAb = p.mostAbundantSRNA();
//            if (mostAb == null) {
//                continue;
//            }
//            int bp = mostAb.getBeg();
//            int ep = mostAb.getEnd();
//
//            if (part == 0 && bp < el) {
//                continue;
//            }
//            if (part == 1 && ep > bl) {
//                continue;
//            }
//
//            if (part == 1) {
//                int dif1 = bl - ep;
//                if (dif1 >= 0 && dif1 < dif) {
//                    dif = dif1;
//                    closest1 = mostAb;
//                }
//            } else {
//                int dif2 = bp - el;
//                if (dif2 >= 0 && dif2 < dif) {
//                    dif = dif2;
//                    closest1 = mostAb;
//                }
//            }
//        }
//        int bfold = lf.getPosOnGenome();
//        int efold = bfold + lf.getFold().length();
//
//        if (closest1 != null && isOnHairpin(closest1.getBeg(), closest1.getEnd(), bfold, efold)) {
//            if (part == 0 && closest1.getEnd() - posGen <= lf.getFold().length() - 1) {
//
//               // lf.setBms(Math.max(el - posGen, closest1.getBeg() - posGen));
//                lf.setBms(closest1.getBeg() - posGen);
//                lf.setEms(closest1.getEnd() - posGen);
//            } else if (part == 1 && closest1.getBeg() - posGen >= 0) {
//
//                lf.setBm(closest1.getBeg() - posGen);
//                lf.setEm(closest1.getEnd() - posGen);
//                //lf.setEm(Math.min(bl - posGen, closest1.getEnd() - posGen));
//            }
//        }
//        else{
//            if(part ==0){
//                lf.setBms(Math.max(el - posGen, (el + (bl - s.getEnd())) - posGen));
//                lf.setEms(lf.getBms() + s.getLength() - 1 );
//            }
//            else{
//                
//                lf.setEm(Math.min(bl - posGen, (bl - (s.getBeg() - el)) - posGen));
//                lf.setBm(lf.getEm() - s.getLength() + 1);
//            }
        //  }
    }

    private void setStarSequence(LFoldInfo lf) {
        if (lf.getBm() == -1 && lf.getBms() != -1) {
            int em1 = lf.getBl() - (lf.getBms() - lf.getEl());

            if (em1 > 0) {
                lf.setEm(em1);
                lf.setBm(Math.max(0, lf.getEm() - (lf.getEms() - lf.getBms())));
            }
        }
        if (lf.getBms() == -1 && lf.getBm() != -1) {
            int bms1 = lf.getEl() + (lf.getBl() - lf.getEm());

            if (bms1 < lf.getFold().length() - 1) {
                lf.setBms(bms1);
                lf.setEms(Math.min(lf.getFold().length() - 1, lf.getBms() + (lf.getEm() - lf.getBm())));
            }
        }
    }

//    private void determineDicerPartsOnLF(LFoldInfo lf, sRNA s, ArrayList<Patman> clusters) {
//        determineLoop(lf);
//        determineMiRNAs(lf, s, clusters);
//        setStarSequence(lf);
//    }
//
//    private void determineMiRNAs(LFoldInfo lf, sRNA s, ArrayList<Patman> clusters) {
//
//        int posGen = lf.getPosOnGenome();
//
//        int allowedInLoop = Params.inLoopSmall;
//        //int allowedOutside = Params.outsideHP;
//
////        if (lf.getEl() - lf.getBl() + 1 > 10) {
////            allowedInLoop = Params.inLoopBig;
////        }
//
//        int bl = lf.getBl() + posGen + allowedInLoop;
//        int el = lf.getEl() + posGen - allowedInLoop;
//
//        int part = 0;
//        int over = 0;
//        
//        GenomeSequence g = genomeSeqs.get(s);
//        int gb = g.getBeg();
//        int ge = g.getEnd();
//        
//        int b = s.getBeg();//s.isNegative()? s.getBeg(gb, ge):s.getBeg();
//        int e = s.getEnd();//s.isNegative()? s.getEnd(gb, ge):s.getEnd();
//
//        if (e <= bl && b >= posGen ) { //- allowedOutside
//            over = posGen - b;
////            if (over > allowedOutside) {
////                return;
////            }
//            if (over > 0) {
//                return;
//            }
//        } else if (b >= el && e <= posGen + lf.getFold().length()) { //+ allowedOutside) {
//            over = e - posGen - lf.getFold().length();
//
////            if (over > allowedOutside) {
////                return;
////            }
//            if (over > 0) {
//                return;
//            }
//            part = 1;
//        } else {
//            return;
//        }
//
////        if (over > 0) {
////            String str = lf.getFold();
////            String add = "";
////
////            for (int i = 0; i < allowedOutside; i++) {
////                add += ".";
////            }
////
////            lf.setFold(add + str + add);
////            lf.setPosition(lf.getPosition() - allowedOutside);
////            lf.setPosOnGenome(posGen - allowedOutside);
////            posGen = lf.getPosOnGenome();
////
////            bl = lf.getBl() + posGen + allowedInLoop;
////            el = lf.getEl() + posGen - allowedInLoop;
////        }
//              
//
//        if (e <= bl && b >= posGen) {
//
//            lf.setBm(b - posGen);
//            lf.setEm(Math.min(bl - posGen, e - posGen));
//        } else if (b >= el && e <= posGen + lf.getFold().length() - 1) {
//
//            lf.setBms(Math.max(el - posGen, b - posGen));
//            lf.setEms(e - posGen);
//
//           // part = 1;
//        } else {
//            return;
//        }  
//
//        sRNA closest1 = null;
//        int dif = Integer.MAX_VALUE;
//        
//        if(miRStarIndex.containsKey(s)){
//            closest1 = clusters.get(miRStarIndex.get(s)).mostAbundantSRNA();
//        }
//
////        for (Patman p : clusters) {
////            sRNA mostAb;
////            mostAb = p.mostAbundantSRNA();
////            if (mostAb == null) {
////                continue;
////            }
////            int bp = mostAb.getBeg();
////            int ep = mostAb.getEnd();
////
////            if (part == 0 && bp < el) {
////                continue;
////            }
////            if (part == 1 && ep > bl) {
////                continue;
////            }
////
////            if (part == 1) {
////                int dif1 = bl - ep;
////                if (dif1 >= 0 && dif1 < dif) {
////                    dif = dif1;
////                    closest1 = mostAb;
////                }
////            } else {
////                int dif2 = bp - el;
////                if (dif2 >= 0 && dif2 < dif) {
////                    dif = dif2;
////                    closest1 = mostAb;
////                }
////            }
////        }
//        
//        int bfold = lf.getPosOnGenome();
//        int efold = bfold + lf.getFold().length();
//
//        if (closest1 != null ){
//            
//            int cb = closest1.getBeg();//s.isNegative()? closest1.getBeg(gb, ge): closest1.getBeg();
//            int ce = closest1.getEnd();//s.isNegative()? closest1.getEnd(gb, ge): closest1.getEnd();
//            
//            if( isOnHairpin(cb, ce, bfold, efold)) {
//            if (part == 0 && ce - posGen <= lf.getFold().length() - 1) {
//
//               // lf.setBms(Math.max(el - posGen, closest1.getBeg() - posGen));
//                lf.setBms(cb - posGen);
//                lf.setEms(ce - posGen);
//            } else if (part == 1 && cb - posGen >= 0) {
//
//                lf.setBm(cb - posGen);
//                lf.setEm(ce - posGen);
//                //lf.setEm(Math.min(bl - posGen, closest1.getEnd() - posGen));
//            }
//        }
//        }
//
//      
//
//    }
    private static boolean isOnHairpin(int bS, int eS, int bH, int eH) {
        return (bS >= bH && eS <= eH);
    }

    private void determineLoop(LFoldInfo lf) {
        ArrayList<Integer> posLoops = new ArrayList<>();

        char ch = '(';
        int pos = 0;
        for (int i = 0; i < lf.getFold().length(); i++) {
            char myCh = lf.getFold().charAt(i);
            if (myCh == '.') {
                if (i > 0) {
                    ch = lf.getFold().charAt(i - 1);
                }
                pos = i;
                if (i < lf.getFold().length()) {
                    i++;
                }
                while (i < lf.getFold().length() && (lf.getFold().charAt(i)) == '.') {
                    i++;
                }
                if (ch == '(' && lf.getFold().charAt(i) == ')') {
                    posLoops.add(pos);
                }
            }
        }

        int central = 0;

        if (posLoops.size() > 1) {
            int mid = lf.getFold().length() / 2;
            int dif = Integer.MAX_VALUE;

            for (int j = 0; j < posLoops.size(); j++) {
                int intDif = Math.abs(posLoops.get(j) - mid);
                if (intDif < dif) {
                    dif = intDif;
                    central = j;
                }
            }
        }
        lf.setBl(posLoops.get(central));// + lf.getPosOnGenome());

        int e = posLoops.get(central);
        while (lf.getFold().charAt(e) == '.') {
            e++;
        }
        //e--;

        lf.setEl(e);//+ lf.getPosOnGenome());
    }

    public void makeClusters() {

        for (sRNA s : sRNAs) {
            makeClusters(s, sequences.get(s));
        }

    }

    public void prepareSequences() {

        makeClusters();
        keepOnlyOneWhenOverlapping();
    }

    public void removeSizeClassDistr() {

        for (int i = 0; i < sRNAs.size(); i++) {
            sRNA s = sRNAs.get(i);
            SizeClassDistribution scd = new SizeClassDistribution(s, sequences.get(s));

            if (!scd.isSizeDistributionOK()) {
                sRNAs.remove(i);
                removeSRNA(s);
                i--;
            }

        }
    }

    private boolean sizeClassDistrOK(sRNA s) {

        SizeClassDistribution scd = new SizeClassDistribution(s, sequences.get(s));

        if (!scd.isSizeDistributionOK()) {
//            sRNAs.remove(s);
//            removeSRNA(s);
            return false;

        }

        return true;

    }

    private void makeClusters(sRNA s, Patman list) {

        int index = 0;
        ArrayList<Patman> cls = new ArrayList<>();

        Patman cl = new Patman();

        try {
            cl.add(list.get(0));
        } catch (Exception e) {
            System.out.println("Error by processing:" + s.toStringPatman() + " @@@@");
        }

        cls.add(cl);

        for (int i = 1; i < list.size(); i++) {
            boolean found = false;
            for (int j = cls.size() - 1; j >= 0; j--) {
                if (sRNAUtils.isInSameCluster(list.get(i), cl)) {
                    cl.add(list.get(i));
                    if (list.get(i).equals(s)) {
                        index = j;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                cl = new Patman();
                cl.add(list.get(i));
                cls.add(cl);
                if (list.get(i).equals(s)) {
                    index = cls.size() - 1;
                }
            }
        }

//        if(s.isNegative()){
//            index = flipClustersIndexes(cls, index);
//        }
        this.clusters.put(s, cls);
        this.clusterIndex.put(s, index);

    }

    private void flipClustersIndexes(sRNA s) {

        ArrayList<Patman> cls = clusters.get(s);
        int index = clusterIndex.get(s);

        int size = cls.size();
        for (int i = 0; i < size / 2; i++) {
            Patman temp = cls.get(i);
            cls.set(i, cls.get(size - i - 1));
            cls.set(size - i - 1, temp);
        }

        clusterIndex.put(s, size - index - 1);
        //return cls.size() - index - 1;
    }

    private void removeFoldsTooShort() {
        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (folds.get(i).getFold().length() < Params.minFoldLen) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsTooShort(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (folds.get(i).getFold().length() < Params.minFoldLen) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

    private void removeFoldsBadAmfe() {
        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (folds.get(i).getAmfe() > Params.maxAmfe) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsBadAmfe(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (folds.get(i).getAmfe() > Params.maxAmfe) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

    private void removeFoldsNotAHairpin() {
        for (sRNA s : sRNAs) {
            if (!LFold.containsKey(s)) {
                continue;
            }
            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (!isAHairpin(folds.get(i))) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsNotAHairpin(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!isAHairpin(folds.get(i))) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private void removeFoldsTooManyGaps() {
        for (sRNA s : sRNAs) {
            if (!LFold.containsKey(s)) {
                continue;
            }
            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (hasTooManyGapsInsideMiRNA(folds.get(i), Params.gapsInMirna, false)) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsTooManyGaps(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (hasTooManyGapsInsideMiRNA(folds.get(i), Params.gapsInMirna, false)) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private boolean isAHairpin(LFoldInfo lf) {

        if (lf.getBm() == -1 || lf.getBms() == -1) {
            return false;
        }

        if (lf.getBms() - lf.getEm() <= Params.minLoop - 1) {
            return false;
        }

        if (lf.getEl() - lf.getBl() < Params.minLoop - 1) {
            return false;
        }

        int noLoops = noOfLoops(lf.getFold().substring(lf.getEm(), lf.getBms()));
        if (!Params.allowComplexLoop && noLoops > 1) {
            return false;
        } else if (Params.allowComplexLoop && noLoops > Params.noLoops) {
            return false;
        }

        //no loop inside miRNA or miRStar sequence
        return !containsLoop(lf.getFold().substring(lf.getBm(), lf.getEm()))
                && !containsLoop(lf.getFold().substring(lf.getBms(), lf.getEms()))
                //no loop on hp before and after miRNA or miRStar sequence
                && !containsLoop(lf.getFold().substring(0, lf.getBm()))
                && !containsLoop(lf.getFold().substring(lf.getEms(), lf.getFold().length()));

    }

    private boolean containsLoop(String seq) {
        boolean set = false;
        char ref = '.';
        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);
            if (!set && ch != '.') {
                ref = ch;
                set = true;
            } else {
                if (ch != '.' && ch != ref) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasTooManyGapsInsideMiRNA(LFoldInfo lf, int gapsMax, boolean both) {
        if (both) {
            return hasTooManyGapsInsideMiRNA(lf.getFold().substring(lf.getBm(), lf.getEm() + 1), gapsMax)
                    && hasTooManyGapsInsideMiRNA(lf.getFold().substring(lf.getBms(), lf.getEms() + 1), gapsMax);
        } else {

            return hasTooManyGapsInsideMiRNA(lf.getFold().substring(lf.getBm(), lf.getEm() + 1), gapsMax)
                    || hasTooManyGapsInsideMiRNA(lf.getFold().substring(lf.getBms(), lf.getEms() + 1), gapsMax);

        }
    }

    private boolean hasTooManyGapsInsideMiRNA(String seq, int gapsMax) {
        int gaps = 0;

        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);
            if (ch != '.') {
                gaps = 0;
            } else {
                gaps++;
            }
            if (gaps > gapsMax) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMoreThanOneLoop(String seq) {
        char ref = '.';
        boolean set = false;

        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);

            if (ch == ')') {
                set = true;
            }
            if (set && ch == '(') {
                return true;
            }
        }
        return false;
    }

    private int noOfLoops(String seq) {
        boolean set = false;
        int loops = 1;

        for (int i = 0; i < seq.length(); i++) {
            char ch = seq.charAt(i);

            if (ch == ')') {
                set = true;
            }
            if (set && ch == '(') {
                loops++;
                set = false;
            }
        }
        return loops;
    }

    private void removeFoldsBasedOnOrientation() {

        for (sRNA s : sRNAs) {
            if (!LFold.containsKey(s)) {
                continue;
            }
            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (!checkOrientation(folds.get(i), s, Params.minOrientation)) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsBasedOnOrientation(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!checkOrientation(folds.get(i), s, Params.minOrientation)) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

    private void removeFoldsParedPercent(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!checkPared(folds.get(i))) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private void removeFoldsParedNucl() {
        for (sRNA s : sRNAs) {
            if (!LFold.containsKey(s)) {
                continue;
            }
            ArrayList<LFoldInfo> folds = LFold.get(s);

            for (int i = 0; i < folds.size(); i++) {

                if (!checkParedNucl(folds.get(i))) {
                    folds.remove(i);
                    i--;
                }
            }
            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }
        }
    }

    private void removeFoldsParedNucl(sRNA s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFoldInfo> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!checkParedNucl(folds.get(i))) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

//    private void removeFoldsbasedOnStructure() {
//        for (sRNA s : sRNAs) {
//            if (!LFold.containsKey(s)) {
//                continue;
//            }
//            ArrayList<LFoldInfo> folds = LFold.get(s);
//
//            for (int i = 0; i < folds.size(); i++) {
//
//                if (!checkStructure(folds.get(i), s)) {
//                    folds.remove(i);
//                    i--;
//                }
//            }
//            if (LFold.get(s).isEmpty()) {
//                LFold.remove(s);
//            }
//        }
//    }
    private boolean checkParedNucl(LFoldInfo get) {
        int paired1 = 0;
        int paired2 = 0;
        for (int i = get.getBm(); i <= get.getEm(); i++) {
            if (get.getFold().charAt(i) != '.') {
                paired1++;
            }
        }

        for (int i = get.getBms(); i <= get.getEms(); i++) {
            if (get.getFold().charAt(i) != '.') {
                paired2++;
            }
        }

        return paired1 >= Params.minParedNucl || paired2 >= Params.minParedNucl;
    }

    private boolean checkPared(LFoldInfo get) {
        int gapsTotal = 0;
        for (int i = 0; i < get.getFold().length(); i++) {
            if (get.getFold().charAt(i) == '.') {
                gapsTotal++;
            }
        }
        return gapsTotal / (double) get.getFold().length() < Params.minParedPerc;
    }

    private boolean checkMiRStar(LFoldInfo lf, sRNA next) {
        if(lf.getMiRNASide() == -1){
            if(next.getBeg() - lf.getPosOnGenome() < lf.getEl() - Params.clearCut)
                return false;
            if(next.getEnd() - lf.getPosOnGenome() > lf.getFold().length() -1 + Params.clearCut)
                return false;
            if (next.getBeg() - lf.getPosOnGenome() - lf.getEm() < Params.minLoop) {
                return false;
            }
        }
        else if(lf.getMiRNASide() == 1){
            if(next.getBeg() - lf.getPosOnGenome() < -Params.clearCut)
                return false;
            if(next.getEnd() - lf.getPosOnGenome() > lf.getBl() + Params.clearCut)
                return false;
            if (lf.getBms() - next.getEnd() + lf.getPosOnGenome() < Params.minLoop) {
                return false;
            }
        }
        
        
         
        return true;
    }

    private String getMiRBaseInfo(sRNA s, Patman miRBase) {
        //intersection with mirBase
        if (miRBase != null) {
            for (sRNA mir : miRBase) {
                if ((s.getBeg() - mir.getBeg() >= -Params.clearCut && s.getEnd() - mir.getEnd() <= Params.clearCut)) {
                    return mir.getSequence();
                } 
            }
        }
        return "N/A";
    }
    
    public void printResultsToFiles(BufferedWriter outPatman, BufferedWriter outCSV, BufferedWriter outFolds, Patman miRBase) throws IOException {
        for (sRNA s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            printOneSRNA(s, outPatman, false, false);            

            String csvLine = printOneSRNACSV(s, miRBase, false);
  
            printOneFoldSRNA(s, outFolds);
            
            outCSV.append(csvLine);
            outCSV.append("\n");               
        }
        
        outPatman.flush();
        outCSV.flush();
        outFolds.flush();
    }


    private class ValueComparator implements Comparator<LFoldInfo> {

        Map<LFoldInfo, Integer> base;

        public ValueComparator(Map<LFoldInfo, Integer> base) {
            this.base = base;
        }

        @Override
        public int compare(LFoldInfo o1, LFoldInfo o2) {
            if (base.get(o1) > base.get(o2)) {
                return 1;
            } else if (base.get(o1) == base.get(o2)) {
                if (o1.getMfe() < o2.getMfe()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
    }

    private void print(sRNA s, BufferedWriter out, HashMap<sRNA, ArrayList<LFoldInfo>> LFold1) throws IOException {

        if (!LFold1.containsKey(s)) {
            return;
        }

        out.append(genomeSeqs.get(s).getSequence().toString());
        out.append("\n");

        int b = genomeSeqs.get(s).getBeg();

        out.append(s.myToStringSeq(b));
        out.append("\n");

        for (sRNA ss : sequences.get(s)) {
            if (ss.getAb() > 1) {
                out.append(ss.myToStringSeq(b));
                out.append("\n");
            }
        }

        for (int k = 0; k < LFold1.get(s).size(); k++) {
            LFoldInfo lf = LFold1.get(s).get(k);
            for (int i = 0; i < lf.getPosition() - 1; i++) {
                out.append(" ");
            }
            out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getMfe());
            out.append("\n");
        }
    }

    public void filterByRANDFold(sRNA s) {
        try {
            // Create file
            if (!LFold.containsKey(s)) {
                return;
            }

            String runtimepath = path;

            Random rand = new Random();
            int value = rand.nextInt(1000);
            String filePath = runtimepath + "temp_RANDFile_" + value + ".fas";

            File f = new File(filePath);

            while (f.exists()) {
                value = rand.nextInt(1000);
                filePath = runtimepath + "temp_RANDFile_" + value + ".fas";

                f = new File(filePath);
            }

            FileWriter fstream = new FileWriter(filePath);
            BufferedWriter out = new BufferedWriter(fstream);

            LFoldInfo lf = LFold.get(s).get(0);

            String sequence = genomeSeqs.get(s).getSequence().substring(lf.getPosition(),
                    lf.getPosition() + lf.getFold().length());

            out.write(">" + s.getId() + "-" + s.getBeg() + "-" + s.getEnd() + "-" + s.getAb() + "-"
                    + lf.getPosition() + "-" + lf.getFold() + "\n");
            out.write(sequence + "\n");

            out.close();
            fstream.close();

            String instruction = " -d \"" + filePath + "\" 100";
            //System.out.println(instruction);
            String res = execRandFold(instruction);

            // String res = result.get(i);
            String partsRAND[] = res.split("\t");
            double pVal = Double.parseDouble(partsRAND[2].trim());

            if (pVal > Params.pVal) {
                LFold.get(s).remove(lf);
            } else {
                LFold.get(s).get(0).setpVal(pVal);
            }

            if (LFold.get(s).isEmpty()) {
                LFold.remove(s);
            }

            f.delete();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String execRandFold(String instruction) throws IOException, Exception {

        String decodedPath = this.path;
        String path = "";
        
        if (sRNAUtils.isWindows()) {
                decodedPath = decodedPath.substring(0, decodedPath.indexOf("build"));
                decodedPath += "dependencies/";
                path = decodedPath + "randfold.exe";
            }

            if (sRNAUtils.isUnix() || sRNAUtils.isMac()) {
                decodedPath = decodedPath.substring(0, decodedPath.indexOf("dist"));
                decodedPath += "dependencies/";
                path = decodedPath + "randfold";               
            }

        Process process = JAVA_RUNTIME.exec(path + " " + instruction);
        RANDFoldThread rft = new RANDFoldThread(process);
        String res = rft.call();

        return res;

    }

//    private void removeFoldsBasedOnPVal(File f) throws Exception {
//        RANDFold(f);
//        for (sRNA s : sRNAs) {
//            if (!LFold.containsKey(s)) {
//                continue;
//            }
//            LFoldInfo lf = LFold.get(s).get(0);
//            if (lf.getpVal() > Params.pVal) {
//                LFold.get(s).remove(lf);
//            }
//
//            if (LFold.get(s).isEmpty()) {
//                LFold.remove(s);
//            }
//        }
//    }
    private void checkFuzzy(sRNA s) {
        if (!LFold.containsKey(s)) {
            return;
        }

        LFoldInfo lf = LFold.get(s).get(0);

        int bm = lf.getPosOnGenome() + lf.getBm();
        int em = lf.getPosOnGenome() + lf.getEm();
        int bl = lf.getPosOnGenome() + lf.getBl();
        int el = lf.getPosOnGenome() + lf.getEl();
        int bms = lf.getPosOnGenome() + lf.getBms();
        int ems = lf.getPosOnGenome() + lf.getEms();

        int index = 0;

        int sumOK = clusters.get(s).get(clusterIndex.get(s)).getAb();
        int sumFuzzy = 0;

        int mirsIndex = -1;
        if (miRStarIndex.containsKey(s)) {
            mirsIndex = miRStarIndex.get(s);
            sumOK += clusters.get(s).get(mirsIndex).getAb();
        }

        for (int i = index; i < clusters.get(s).size(); i++) {
            if (i == clusterIndex.get(s)) {
                continue;
            }
            if (i == mirsIndex) {
                continue;
            }

            Patman cl = clusters.get(s).get(i);
            
            if(cl.getEnd() < lf.getPosOnGenome() || cl.getBeg() > lf.getPosOnGenome() + lf.getFold().length()){
                continue;
            }
            
//            if ((cl.getBeg() < lf.getPosOnGenome() && cl.getEnd() > lf.getPosOnGenome())){// + lf.getFold().length())) {
//                for (sRNA scl : cl) {
//                    if (scl.getBeg() < bm) {
//                    if (scl.getEnd() - bm > Params.clearCut) {
//                        sumFuzzy += scl.getAb();
//                    } else {
//                        //sumOK += scl.getAb();
//                    }
//            }
//                }
//            }
//            else
//                if(cl.getEnd() > lf.getPosOnGenome() + lf.getFold().length() && cl.getBeg() < lf.getPosOnGenome() + lf.getFold().length()){
//                    for (sRNA scl : cl) {
//                        if (scl.getBeg() > bms) {
//                    if (ems - scl.getBeg() > Params.clearCut) {
//                        sumFuzzy += scl.getAb();
//                    } else {
//                        sumOK += scl.getAb();
//                    }
//                }
//                }
                

            for (sRNA scl : cl) {
                if (scl.getBeg() < bm) {
                    if (scl.getEnd() - bm > Params.clearCut) {
                        sumFuzzy += scl.getAb();
                    } else {
                        if(scl.getBeg() >= lf.getPosOnGenome())
                            sumOK += scl.getAb();
                    }
                } else if (scl.getBeg() >= bm && scl.getEnd() <= ems) {

                    if (sRNAUtils.isInSameCluster(scl, bl, el)) //                                || sRNAUtils.isInSameCluster(scl, bm, em)
                    //                                || sRNAUtils.isInSameCluster(scl, bms, ems))
                    {
                        sumOK += scl.getAb();
                    } else if ((scl.getBeg() < bl
                            && (em - scl.getBeg() > Params.clearCut || scl.getEnd() - bl > Params.clearCut))
                            || (scl.getBeg() >= bl
                            && (el - scl.getBeg() > Params.clearCut || scl.getEnd() - bms > Params.clearCut))) {
                        sumFuzzy += scl.getAb();
                    } else {
                        sumOK += scl.getAb();
                    }
                } else if (scl.getBeg() > bms) {
                    if (ems - scl.getBeg() > Params.clearCut) {
                        sumFuzzy += scl.getAb();
                    } else {
                        if(scl.getEnd() < lf.getPosOnGenome() + lf.getFold().length())
                            sumOK += scl.getAb();
                    }
                }
            }
        }

        for (sRNA scl : clusters.get(s).get(clusterIndex.get(s))) {
            if (scl.getBeg() < bl) {
                if (bm - scl.getBeg() > Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
                if (scl.getEnd() - em > Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
            } else {
                if (bms - scl.getBeg() > Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
                if (scl.getEnd() - ems > Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
            }
        }

        if (mirsIndex != -1) {
            for (sRNA scl : clusters.get(s).get(mirsIndex)) {
                if (scl.getBeg() < bl) {
                    if (bm - scl.getBeg() > Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                    if (scl.getEnd() - em > Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                } else {
                    if (bms - scl.getBeg() > Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                    if (scl.getEnd() - ems > Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                }
            }
        }

        if (sumOK / ((double) sumOK + sumFuzzy) < Params.fuzzy) {
            LFold.remove(s);
        }
        else {
            int scoreAb = (mirsIndex != -1) ? clusters.get(s).get(mirsIndex).getAb() : 0;
            scoreAb += clusters.get(s).get(clusterIndex.get(s)).getAb();
            lf.setAbundance(scoreAb);
        }

    }
    
    private sRNA getBestMirStar(Patman cl, LFoldInfo lf) {
        
        if(lf.getMiRNASide() < 0){
            return sRNAUtils.bestSRNA(cl, lf.getPosOnGenome() + lf.getBms(), lf.getPosOnGenome() + lf.getEms());
        }
        else if(lf.getMiRNASide() > 0){
            return sRNAUtils.bestSRNA(cl, lf.getPosOnGenome() + lf.getBm(), lf.getPosOnGenome() + lf.getEm());
        }
        
        return null;
    }
    
    //call it with printStar = false
    private String  printOneSRNACSV(sRNA s, Patman miRBase, boolean printStar) {
       
        StringBuilder csvInfo = new StringBuilder();

        if (!printStar) {
            csvInfo.append(roundDecimals(LFold.get(s).get(0).getScore(),2)).append(",");
            csvInfo.append(s.toStringPatman(',')).append(",");
            csvInfo.append(getHPInfo(s)).append(",");
            csvInfo.append(roundDecimals(LFold.get(s).get(0).getpVal(),4) ).append(",");
            csvInfo.append(getStarInfo(s, printStar) ).append(",");
            csvInfo.append(getMiRBaseInfo(s, miRBase) );

        } else {
            if (miRStarIndex.containsKey(s)) {
                sRNA st = miRStar.get(s);
                csvInfo.append(st.toStringPatman(',') ).append(",");
                csvInfo.append(getHPInfo(s) ).append(",");
                csvInfo.append(getStarInfo(s, !printStar) ).append(",");
                csvInfo.append(getMiRBaseInfo(s, miRBase) );
            }
        }     
        
        return csvInfo.toString();
    }
    
    static private double roundDecimals(double number, int decimals) {
        String decString = "";
        for(int i = 0; i < decimals; i++){
            decString +="#";
        }
        DecimalFormat decimalsFormat = new DecimalFormat("#." + decString);
        return Double.valueOf(decimalsFormat.format(number));
    }
       

}
