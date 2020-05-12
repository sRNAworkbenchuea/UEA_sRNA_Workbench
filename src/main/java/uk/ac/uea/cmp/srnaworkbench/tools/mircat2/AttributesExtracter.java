/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2;

/**
 *
 * @author keu13sgu & Matthew Stocks
 */
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes.*;

import java.io.*;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.LFold_Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.LFoldPrecursorServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;

/**
 *
 * @author keu13sgu
 */
public final class AttributesExtracter {

    //the selected sRNAs with high abundance
    private Patman sRNAs;
   // private Patman allSRNAs;

    private HashMap<Aligned_Sequences_Entity, Patman> sequences;
    private HashMap<Aligned_Sequences_Entity, ArrayList<LFold_Precursor_Entity>> LFold;
    private HashMap<Aligned_Sequences_Entity, GenomeSequence> genomeSeqs;
    private HashMap<Aligned_Sequences_Entity, ArrayList<Patman>> clusters;
    private HashMap<Aligned_Sequences_Entity, Integer> clusterIndex;
    private HashMap<Aligned_Sequences_Entity, Integer> miRStarIndex;
    private HashMap<Aligned_Sequences_Entity, Aligned_Sequences_Entity> miRStar;

    private boolean printStar = false;
    
    private BinaryExecutor myExeMan = null;
    private Session session;
    
    private GenomeManager genMan;
    public static String path = null;

    
    
    public AttributesExtracter(Patman sRNAs, BinaryExecutor newExeMan, Session session) {
        this.sRNAs = sRNAs;
        //this.allSRNAs = allsRNAs;
        
        this.session = session;

        sequences = new HashMap<>();
        LFold = new HashMap<>();
        genomeSeqs = new HashMap<>();
        clusters = new HashMap<>();
        clusterIndex = new HashMap<>();
        miRStarIndex = new HashMap<>();
        miRStar = new HashMap<>();
        myExeMan = newExeMan;//to be given separatly if multithreaded
        
//
//        this.sRNAs.sort();
//        this.allSRNAs.sort();

        try {
            if (path == null) {
                path = MiRCat20.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                path = URLDecoder.decode(path, "UTF-8");
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void processSequences(GenomeManager genSeq, String chrom) throws IOException {
        int i;
        this.genMan = genSeq;
        //LFoldPrecursorServiceImpl precursorServ = (LFoldPrecursorServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("LFoldPrecursorService");
        //PredictionServiceImpl predictionServ = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");

        outerloop:
        for (i = 0; i < sRNAs.size(); i++) {
            Aligned_Sequences_Entity s = sRNAs.get(i);
            //4	TAAGTGCTTCTCTTTGGGCTAG(9)	28005657	28005678	+	0
            //preprocess to get seqs 
            int removed = readPatmanForSequence(s, i);
            if(removed == 1){
                i--;
                continue;
            }       
            prepareSequences(s);
            
            boolean wasStar = false;
            Aligned_Sequences_Entity before = null;

            // check if there is a seq before that can be it's miRStar
            for (int j = i - 1; j >= 0; j--) {
                before = sRNAs.get(j);

                if (before.getEnd1() + MiRCat2Params.foldDist < s.getStart1()) {
                    break;
                }

                if (LFold.containsKey(before) && miRStar.containsKey(before)) {
                    Aligned_Sequences_Entity beforeStar = miRStar.get(before);

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

            //read the genome sequence
            addGenomeSeq(s, chrom);

            if (!sizeClassDistrOK(s)) {
                continue;
            }

            if (!hasClearCut(s)) {
                continue;
            }

            analyzeSecondaryStructure(s);

            if(MiRCat2Params.execRANDFold)
                filterByRANDFold(s);
            
            if (LFold.containsKey(s)){ 
                if( wasStar){
                    LFold.remove(before);
                   // i--;
                    removeSRNA(before);
                   //
                }
             //   precursorServ.saveOrUpdate(LFold.get(s).get(0));
                
                if(!miRStar.containsKey(s))
                    miRStar.put(s, Aligned_Sequences_Entity.NO_ALIGNMENT);
                
                //Prediction_Entity pe = new Prediction_Entity("miRCat2", LFold.get(s).get(0), s, miRStar.get(s));
               // predictionServ.saveOrUpdate(pe, session);
                // predictionServ.saveOrUpdate(pe);
               // predictionServ.saveOrUpdate(pe);
                
            }
            
        }

    }
    

    public ArrayList<Integer> printResults2(BufferedWriter out, BufferedWriter outMB, BufferedWriter outFolds,
        Patman miRBase, boolean allInfo) throws IOException {

        ArrayList<Integer> totals = new ArrayList<>();

        int count = 0;
        int countMirs = 0;

        for (Aligned_Sequences_Entity s : sRNAs) {

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
                for (Aligned_Sequences_Entity mir : miRBase) {
                    if ((s.getStart1() - mir.getStart1() >= -MiRCat2Params.clearCut && s.getEnd1() - mir.getEnd1() <= MiRCat2Params.clearCut)) {

                        isFound = true;
                        if (allInfo) {
                            printOneSRNA2(s, outMB, outFolds, allInfo, false, false);
                            outMB.append(mir.getRna_seq() + "\n");
                        }
                        countMirs++;
                        //break;
                    } else {
                        if (printStar && miRStarIndex.containsKey(s)) {

                            Aligned_Sequences_Entity st = sRNAUtils.bestSRNA(clusters.get(s).get(miRStarIndex.get(s)));

                            if ((st.getStart1() - mir.getStart1() >= -MiRCat2Params.clearCut && st.getEnd1() - mir.getEnd1() <= MiRCat2Params.clearCut)) {
                                isFound = true;
                                if (allInfo && printStar) {
                                    printOneSRNA2(s, outMB, outFolds, allInfo, true, false);
                                    outMB.append(mir.getRna_seq() + "\n");
                                }

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
    private void printOneSRNA(Aligned_Sequences_Entity s, BufferedWriter out, boolean allInfo, boolean star) throws IOException {

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();

        if (allInfo) {

//            if (s.isNegative() && !s.isFlipped()) {
//                flipAllsRNAs(s, b, e); //set begRel and endRel to all Aligned_Sequences_Entity and Patmans
//            }

            if (!star) {
                out.append(s.toStringPatman() + '\n');
                out.append(genomeSeqs.get(s).getSequence(genMan) + "\n");
                out.append(s.myToString(b, '#') + "\n");

                for (Aligned_Sequences_Entity ss : sequences.get(s)) {
                    out.append(ss.myToStringSeq(b) + "\n");
                }
                for (LFold_Precursor_Entity lf : LFold.get(s)) {
                    for (int j = 0; j < lf.getPosition(b); j++) {
                        out.append(" ");
                    }
                    out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getpVal() + "\n");
                }
            } else if (miRStarIndex.containsKey(s)) {

                Aligned_Sequences_Entity st = miRStar.get(s);
                out.append(st.toStringPatman() + '\n');
                out.append(genomeSeqs.get(s).getSequence(genMan) + "\n");
                out.append(s.myToString(b, '#') + "\n");

                for (Aligned_Sequences_Entity ss : sequences.get(s)) {
                    out.append(ss.myToStringSeq(b) + "\n");

                }
                for (LFold_Precursor_Entity lf : LFold.get(s)) {
                    for (int j = 0; j < lf.getPosition(b); j++) {
                        out.append(" ");
                    }

                    out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getpVal() + "\n");
                }
            }

        } else {

            if (!star) {
                out.append(s.toStringPatman() + "\n");
            } else if (miRStarIndex.containsKey(s)) {

                Aligned_Sequences_Entity st = miRStar.get(s);
                out.append(st.toStringPatman() + "\n");
            }
        }
    }

    private void printOneSRNA2(Aligned_Sequences_Entity s, BufferedWriter out, BufferedWriter outFolds, boolean allInfo, boolean printStar, boolean printEndl) throws IOException {

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
                    Aligned_Sequences_Entity st = miRStar.get(s);
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
                    Aligned_Sequences_Entity st = miRStar.get(s);
                    out.append(st.toStringPatman() + "\n");
                }
            }
        }
    }
    
     static private double roundDecimals(double number, int decimals) {
        String decString = "";
        for(int i = 0; i < decimals; i++){
            decString +="#";
        }
        DecimalFormat decimalsFormat = new DecimalFormat("#." + decString);
        return Double.valueOf(decimalsFormat.format(number));
    }
    
    //call it with printStar = false
    private String  printOneSRNACSV(Aligned_Sequences_Entity s, boolean printStar) {
       
        StringBuilder csvInfo = new StringBuilder();

        if (!printStar) {
            csvInfo.append(roundDecimals(LFold.get(s).get(0).getScore(),2)).append(",");
            csvInfo.append(s.toStringPatman(',') ).append( ',');
            csvInfo.append(getHPInfo(s) ).append( ',');
            csvInfo.append(roundDecimals(LFold.get(s).get(0).getpVal(),4) ).append( ",");
            csvInfo.append(getStarInfo(s, printStar) ).append( ',');
            csvInfo.append(getMiRBaseInfo(s) );

        } else {
            if (miRStarIndex.containsKey(s)) {
                Aligned_Sequences_Entity st = miRStar.get(s);
                csvInfo.append(st.toStringPatman(',') ).append( ',');
                csvInfo.append(getHPInfo(s) ).append( ',');
                csvInfo.append(getStarInfo(s, !printStar) ).append( ',');
                csvInfo.append(getMiRBaseInfo(s) );
            }
        }     
        
        return csvInfo.toString();
    }
    
    private String getMiRBaseInfo(Aligned_Sequences_Entity s )
    {
        String annotation = "";
        int annotCount = 1;
        for(GFF_Entity g_e : s.getAnnotations())
        {
            annotation += "Annotation (" + annotCount +"): ";
            if(g_e.getId().getStart()+1 == s.getStart() && g_e.getId().getEnd()+1 == s.getEnd())
            {
                annotation += "Annotation is: ";
                annotation += g_e.getRecord().getAttribute("Name");
                annotation += " - ";

            }
            else
            {
                annotation += "Seqeunce is the same as: ";
                annotation += g_e.getRecord().getAttribute("Name");
                annotation += " starting at: " +g_e.getId().getStart()+1 + " ending at: " + g_e.getId().getEnd()+1;
                annotation += " - ";
            }
            annotCount++;
            annotation += " || ";

        }
        
        return annotation.isEmpty() ? "N/A" : annotation;
    }
    
    private String getStarInfo(Aligned_Sequences_Entity s, boolean printStar) {
        StringBuilder str = new StringBuilder();

        if (printStar) {

            str.append(s.getRna_seq()).append(",");
            str.append(s.getAb()).append(",");
            str.append(s.getStart()).append(",");
            str.append(s.getEnd());

        } else if (miRStarIndex.containsKey(s)) {

            Aligned_Sequences_Entity star = miRStar.get(s);
            str.append(star.getRna_seq()).append(",");
            str.append(star.getAb()).append(",");
            str.append(star.getStart()).append(",");
            str.append(star.getEnd());

        } else {

            str.append("N/A,N/A,N/A,N/A");
        }

        return str.toString();
    }

    private String getHPInfo(Aligned_Sequences_Entity s){
        StringBuilder str = new StringBuilder();
        for (LFold_Precursor_Entity lf : LFold.get(s)) {

            int b = lf.getPosOnGenome();
            String seq = lf.getSequence(); 

            //Hp sequence, Hp start, Hp end, fold, mfe, amfe 
            str.append(seq).append(",");
            str.append(b + 1).append(",");
            str.append(b + lf.getFold().length()).append(",");
            str.append(lf.getFold()).append(",");
            str.append(roundDecimals(lf.getMfe(),2)).append(",");
            str.append(roundDecimals(lf.getAmfe(),2));
        }
        return str.toString();

    }
    
    private void keepOnlyOneWhenOverlapping() {
        Patman p1 = null;
        Patman p2 = null;

        for (int i = sRNAs.size() - 1; i > 0 ; i--) {
            Aligned_Sequences_Entity s1 = sRNAs.get(i);
            p1 = clusters.get(s1).get(clusterIndex.get(s1));

            for (int j = i - 1; j >= 0 ; j--) {
                Aligned_Sequences_Entity s2 = sRNAs.get(j);

                if (!s1.getChromosome().equals(s2.getChromosome())) {
                    break;
                }
                
                if(s2.getEnd1() < s1.getStart1() - MiRCat2Params.SUBWINDOW)
                    break;
                
                p2 = clusters.get(s2).get(clusterIndex.get(s2));


                if (p1.equals(p2)) {
                    if (s1.getAb() > s2.getAb()) {
                        sRNAs.remove(j);
                        i--;
                        removeSRNA(s2);
                    } else {
                        sRNAs.remove(i);
                        removeSRNA(s1);
                        break;
                    }
                }
            }
        }

    }
    
//     private void keepOnlyOneWhenOverlapping(Aligned_Sequences_Entity s1, Patman p1) {
//
//        Patman p2 = null;
//
//            for (int j = i - 1; j >= 0 ; j--) {
//                Aligned_Sequences_Entity s2 = sRNAs.get(j);
//
//                if (!s1.getChromosome().equals(s2.getChromosome())) {
//                    break;
//                }
//                
//                if(s2.getEnd1() < s1.getStart1() - MiRCat2Params.SUBWINDOW)
//                    break;
//                
//                p2 = clusters.get(s2).get(clusterIndex.get(s2));
//
//
//                if (p1.equals(p2)) {
//                    if (s1.getAb() > s2.getAb()) {
//                        sRNAs.remove(j);
//                        i--;
//                        removeSRNA(s2);
//                    } else {
//                        sRNAs.remove(i);
//                        removeSRNA(s1);
//                        break;
//                    }
//                }
//            }
//
//
//    }


    private void removeSRNA(Aligned_Sequences_Entity s) {
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

    }

    private int readPatmanForSequence(Aligned_Sequences_Entity seq, int index) {
        int toRemove = 0;
        
        if(seq.getLength() < MiRCat2Params.minLen || seq.getLength() > MiRCat2Params.maxLen){
              sRNAs.remove(index);
              removeSRNA(seq);
              toRemove = 1;
        }
        else
            addToSequences(seq);
        
        return toRemove;
    }
    
//    private void readPatmanForSequences(Patman allSeqs) {
//
//        int index = 0;
//        int indexSeqs = 0;
//
//        for (int i = index; i < sRNAs.size(); i++) {
//            Aligned_Sequences_Entity temp2 = sRNAs.get(i);
//            if(temp2.getLength() < MiRCat2Params.minLen || temp2.getLength() > MiRCat2Params.maxLen){
//                  sRNAs.remove(i);
//                  removeSRNA(temp2);
//                  i--;
//                  continue;
//            }
//            indexSeqs = addToSequences(temp2, allSeqs, indexSeqs);
//        }
//    }

//    private int addToSequences(Aligned_Sequences_Entity s, Patman seqs, int index) {
//        int limB = s.getStart1() - MiRCat2Params.foldDist;
//        int limE = s.getEnd1() + MiRCat2Params.foldDist;
//
//        Patman selected = new Patman();
//
//        int k = index;
//        boolean isSet = false;
//        for (int i = index; i < seqs.size(); i++) {
//
//            if (seqs.get(i).getStart1() >= limB && seqs.get(i).getEnd1() <= limE) {
//                if (!isSet) {
//                    isSet = true;
//                    k = i;
//                }
//                selected.add(seqs.get(i));
//            }
//            if (seqs.get(i).getStart1() > limE) {
//                break;
//            }
//        }
//
//        sequences.put(s, selected);
//
//        return k;
//    }
    
     private void addToSequences(Aligned_Sequences_Entity s) {
        int limB = s.getStart1() - MiRCat2Params.foldDist;
        int limE = s.getEnd1() + MiRCat2Params.foldDist;
        
        String hql = "from Aligned_Sequences_Entity s where s.id.chrom = :chromosome "
                + "and s.id.start >= :start and s.id.end <= :end order by s.id.start";
        
         List<Aligned_Sequences_Entity> findAll = session.createQuery(hql)
                    .setParameter("chromosome", s.getChromosome())
                    .setParameter("start", limB)
                    .setParameter("end", limE)
                    .list();

        Patman selected = new Patman(findAll);
        sequences.put(s, selected);
    }

    private void addGenomeSeq(Aligned_Sequences_Entity s, String chrom) throws IOException {

        //Patman begins index from 1, substring begins from 0
        int maxLen = genMan.getChrLength(chrom);
        
        int b = Math.max(1, s.getStart1() - MiRCat2Params.foldDist);
        int e = Math.min(s.getEnd1() + MiRCat2Params.foldDist + 1, maxLen) - 1;

        int end = s.getEnd1();

       // StringBuilder genSeq = new StringBuilder(genMan.getDNA(chrom, b + 1, e + 1, GenomeManager.DIR.POSITIVE));
        e = e - 1;

        try {
            genomeSeqs.put(s, new GenomeSequence(s.getChromosome(), b, e));

        } catch (Exception ex) {
            System.out.println(b + " " + e);

        }
    }

//    private void flipAllsRNAs(Aligned_Sequences_Entity s, int b, int e) {
//        for (Aligned_Sequences_Entity ss : sequences.get(s)) {
//            //ss.setBegEndRel(b, e);
//        }
//        
//        for (Patman p : clusters.get(s)) {
//            p.flipBegEndRel(b, e);
//        }
//        
//        flipClustersIndexes(s);
//    }

    private void resetAll(Aligned_Sequences_Entity s) {
        for (Aligned_Sequences_Entity ss : sequences.get(s)) {
         //   ss.resetBegEndRel();
        }
        
        for (Patman p : clusters.get(s)) {
            p.resetBegEndRel();
        }
        
    }


    public boolean hasClearCut(Aligned_Sequences_Entity s) {

        boolean toBeRemoved = !checkForCleanCut(s, clusterIndex.get(s), MiRCat2Params.clearCutPercent, MiRCat2Params.underClearCut);
        boolean star = true;//(checkForMiRStarClearCut(s, clusterIndex.get(s), MiRCat2Params.clearCutPercent, MiRCat2Params.underClearCut));
        if (MiRCat2Params.miRStarPresent || miRStarIndex.containsKey(s)) {
            toBeRemoved &= !(star && miRStarIndex.containsKey(s));
        }

        if (toBeRemoved) {
            return false;
        }

        return true;
    }

    private boolean checkAdiacentClusters(Aligned_Sequences_Entity s, int index) {
        boolean rez = true;
        int abCl = 0;

        int b = genomeSeqs.get(s).getBeg();
        int e = genomeSeqs.get(s).getEnd();

        for (int i = index - 1; i >= 0; i--) {
            for (Aligned_Sequences_Entity si : clusters.get(s).get(i)) {
                if (si.isSameStrand(s) && si.getEnd1() - s.getStart1() > MiRCat2Params.clearCut) {
                    abCl += si.getAb();
                }

            }

        }
        for (int i = index + 1; i < clusters.get(s).size(); i++) {
            for (Aligned_Sequences_Entity si : clusters.get(s).get(i)) {

                if (si.isSameStrand(s) && s.getEnd1() - si.getStart1() > MiRCat2Params.clearCut) {
                    abCl += si.getAb();
                }
            }
        }

        rez &= (abCl / (double) clusters.get(s).get(index).getAb()) <= MiRCat2Params.overlapAdiacentCluster;
        return rez;
    }

   

    private boolean checkForCleanCut(Aligned_Sequences_Entity s, int index, double param, double paramLow) {

        return cleanCutOnMiRNACluster(s, clusters.get(s).get(index), 0, param, paramLow)
                && checkAdiacentClusters(s, index);

    }


    private boolean cleanCutOnMiRNACluster(Aligned_Sequences_Entity s, Patman p, int dir, double param, double paramLow) {

        int beg = s.getStart1();
        int end = s.getEnd1();

        boolean rez = true;

        int sumMAS = 0;
        int sumOutside = 0;
        double totalCut = 0;
        if (dir == 0) {
            for (Aligned_Sequences_Entity sp : p) {

                int bsp = sp.getStart1();
                int esp = sp.getEnd1();

                if (Math.abs(bsp - beg) <= MiRCat2Params.clearCut && Math.abs(esp - end) <= MiRCat2Params.clearCut) {
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
            for (Aligned_Sequences_Entity sp : p) {
                int bsp = sp.getStart1();

                if (Math.abs(bsp - beg) <= MiRCat2Params.clearCut) {
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
            for (Aligned_Sequences_Entity sp : p) {
                int esp = sp.getEnd1();

                if (Math.abs(esp - end) <= MiRCat2Params.clearCut) {
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


    private void analyzeSecondaryStructure(Aligned_Sequences_Entity s) {
        try {

            String toFoldSeq = genomeSeqs.get(s).getSequence(genMan);
            int genBeg = genomeSeqs.get(s).getBeg();
            
            if(s.isNegative()){
                toFoldSeq = SequenceUtils.DNA.reverseComplement(toFoldSeq);
            }

            
            ArrayList<LFold_Precursor_Entity> executeLFold = executeLFold(toFoldSeq, 
                    " -L " + MiRCat2Params.lFoldL, s.isNegative(), genBeg, s);
            LFold.put(s, executeLFold);

            //****************************************************
            keepValidFolds(s);

        } catch (Exception ex) {
            Logger.getLogger(AttributesExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void keepValidFolds(Aligned_Sequences_Entity s) throws Exception {

        removeFoldsTooShort(s);
        removeFoldsBadAmfe(s);       

        //4	TAAGTGCTTCTCTTTGGGCTAG(9)	28005657	28005678	+	0
        //4	TAAGTGCTTCTCTTTGGGGTAG(4597)	28001570	28001591	+	0
//        if(s.getStart() == 28001570 && s.getEnd() == 28001591){
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
    
//     private void print(Aligned_Sequences_Entity s, HashMap<Aligned_Sequences_Entity, ArrayList<LFold_Precursor_Entity>> LFold1) throws IOException {
//
//        if (!LFold1.containsKey(s)) {
//            return;
//        }
//        
//        PrintStream out = System.out;
//
//        out.append(genomeSeqs.get(s).getSequence(genMan));
//        out.append("\n");
//
//        int b = genomeSeqs.get(s).getBeg();
//        
//
//        out.append(s.myToStringSeq(b));
//        out.append("\n");
//
//        for (Aligned_Sequences_Entity ss : sequences.get(s)) {
//            if (ss.getAb() > 0) {
//                out.append(ss.myToStringSeq(b) );
//                out.append("\n");
//            }
//        }
//
//        for (int k = 0; k < LFold1.get(s).size(); k++) {
//            LFold_Precursor_Entity lf = LFold1.get(s).get(k);
//            for (int i = 0; i < lf.getPosition(b); i++) {
//                out.append(" ");
//            }
//            out.append(lf.getFold() + " " + lf.getAmfe() + " " + lf.getMfe());
//            out.append("\n");
//        }
//    }

    private void selectBestFold(Aligned_Sequences_Entity s) {
        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);
        // int shortest = 0;
        int bestAmfe = 0;

        for (int i = 1; i < folds.size(); i++) {
            LFold_Precursor_Entity lf = folds.get(i);
            if (lf.getAmfe() < folds.get(bestAmfe).getAmfe()) {
                bestAmfe = i;
            }

        }

        LFold_Precursor_Entity lf = folds.get(bestAmfe);
        LFold.put(s, new ArrayList<>());
        LFold.get(s).add(lf);

    }

    private ArrayList<LFold_Precursor_Entity> executeLFold(String toFold, String LParam, 
            boolean isNegative, int beg, Aligned_Sequences_Entity s) throws IOException, Exception {

        String result = myExeMan.execRNALFold(toFold, LParam); 
        ArrayList<LFold_Precursor_Entity> folds = parseFolds(toFold, result, isNegative, beg, s);
        return folds;
    }

    private void removeMiRNAsBasedOnLength(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

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

    private boolean checkMiRNALength(LFold_Precursor_Entity lf, Aligned_Sequences_Entity s) {

        if (lf.getEm() < MiRCat2Params.minLen) {
            return false;
        }
        if (lf.getFold().length() - lf.getBms() < MiRCat2Params.minLen) {
            return false;
        }

        if ((lf.getEm() - lf.getBm() + 1 >= MiRCat2Params.minLen && lf.getEm() - lf.getBm() + 1 <= MiRCat2Params.maxLen
                && (lf.getEms() - lf.getBms() + 1 >= MiRCat2Params.minLen ))
                ||
               (lf.getEms() - lf.getBms() + 1 >= MiRCat2Params.minLen && lf.getEms() - lf.getBms() + 1 <= MiRCat2Params.maxLen  
                && lf.getEm() - lf.getBm() + 1 >= MiRCat2Params.minLen)){
            return true;
        }
        return false;

    }

    private boolean checkOrientation(LFold_Precursor_Entity lf, Aligned_Sequences_Entity s, double minOr) {

        ArrayList<Patman> cl = clusters.get(s);
        int plus = 0;
        int minus = 0;
        int total = 0;

        for (Patman p : cl) {
            if (isOnFold(lf, p, s)) {
                for (Aligned_Sequences_Entity ss : p) {

                    if (ss.getStrand().equals("+")) {
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

    private boolean isOnFold(LFold_Precursor_Entity lf, Patman p, Aligned_Sequences_Entity sRef) {
        Aligned_Sequences_Entity s = p.mostAbundantSRNA();

        if (s == null) {
            int b = p.getBeg();
            int e = p.getEnd();

            return b - lf.getPosOnGenome() >= 0 //- MiRCat2Params.outsideHP
                    && e - lf.getPosOnGenome() <= lf.getFold().length() ;//+ MiRCat2Params.outsideHP;
        }

        return s.getStart1() - lf.getPosOnGenome() >= 0 //- MiRCat2Params.outsideHP
                && s.getEnd1() - lf.getPosOnGenome() <= lf.getFold().length() ;//+ MiRCat2Params.outsideHP;
    }

    private void removeFoldsNoCoverage2(Aligned_Sequences_Entity s) throws IOException {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        boolean hasMirSCluster = false;

        for (int i = 0; i < folds.size(); i++) {

            LFold_Precursor_Entity lf = folds.get(i);

            int indexMiRS = getClusterMirS(s, lf);
            if (indexMiRS != -1) {
                hasMirSCluster = true;
                lf.setMiRStarSeq(indexMiRS);
            }

        }

        if (hasMirSCluster) {
            ArrayList<Integer> starIndexes = new ArrayList<>();

            for (int i = 0; i < folds.size(); i++) {

                LFold_Precursor_Entity lf = folds.get(i);
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
                    Aligned_Sequences_Entity mirStar = null;
                    Aligned_Sequences_Entity next = null;
                     for (int i = 0; i < folds.size(); i++) {

                        LFold_Precursor_Entity lf = folds.get(i);
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
                                lf.setBms(next.getStart1() - lf.getPosOnGenome());
                                lf.setEms(Math.min(next.getEnd1() - lf.getPosOnGenome(), lf.getFold().length() -1));
                            }
                            else if(lf.getMiRNASide() == 1){
                                lf.setBm(Math.max(0,next.getStart1() - lf.getPosOnGenome()));
                                lf.setEm(next.getEnd1() - lf.getPosOnGenome());
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
            if (MiRCat2Params.miRStarPresent) {
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

    private int getBestCluster(Aligned_Sequences_Entity s, ArrayList<Integer> indexes) throws IOException {

        int bestAb = -1;
        int index = -1;

        for (Integer i : indexes) {
            Patman p = clusters.get(s).get(i);
            Aligned_Sequences_Entity mirS = sRNAUtils.bestSRNA(p);


            int pAb = p.getAb();

            if (pAb > bestAb) {
                bestAb = pAb;
                index = i;
            }
            //}
        }

        return index;

    }

    private int getClusterMirS(Aligned_Sequences_Entity s, LFold_Precursor_Entity lf) {
        int bs = lf.getOtherMirBeg(s) + lf.getPosOnGenome();
        int es = lf.getOtherMirEnd(s) + lf.getPosOnGenome();

        if (bs == -1 || es == -1) {
            return -1;
        }

        int index = clusterIndex.get(s);
        int indexMS = -1;

        int maxAb = 0;

        if (s.getStart1() < bs) {

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
            Aligned_Sequences_Entity b = sRNAUtils.bestSRNA(clusters.get(s).get(indexMS));
            if (Math.abs(s.getStart1() - b.getEnd1()) <= MiRCat2Params.minLoop || Math.abs(s.getEnd1() - b.getStart1()) <= MiRCat2Params.minLoop) {
                return -1;
            }
        }

        return indexMS;
    }


    private void determineDicerParts(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        GenomeSequence g = genomeSeqs.get(s);
        int gb = g.getBeg();

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {
            LFold_Precursor_Entity lf = folds.get(i);

            if ((lf.getPosOnGenome()) <= s.getStart1()
                    && (lf.getPosOnGenome() + lf.getFold().length() - 1) >= s.getEnd1()) {
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

    private boolean determineDicerPartsOnLF(LFold_Precursor_Entity lf, Aligned_Sequences_Entity s, ArrayList<Patman> clusters) {
        determineLoop2(lf);

        if (!determineMiRNAs(lf, s, clusters)) {
            return false;
        }

        return setStarSequence2(lf, s.isNegative());

    }

    private boolean setStarSequence2(LFold_Precursor_Entity lf, boolean isNegative) {
         boolean onLeft = true;
         
         int overhang = MiRCat2Params.threePrimeOverhang;
                 
         
       if (lf.getBm() == -1 && lf.getBms() != -1) { //on right
            int bm = lf.getPairPosition(lf.getEms(), !onLeft) - overhang;
            
            if(bm < 0)
                return false;
            
            lf.setBm(bm);
            int em = lf.getPairPosition(lf.getBms(), !onLeft) + overhang;//bm + (lf.getEms() - lf.getBms() );
            
            
            lf.setEm(em);
            return !containsLoop(lf.getFold().substring(lf.getBm(), lf.getEm() + 1));
        }
        if (lf.getBms() == -1 && lf.getBm() != -1) { //on left
            int ems = lf.getPairPosition(lf.getBm(), onLeft) + overhang;
            
            if(ems >= lf.getFold().length())
                return false;
            
            int bms = lf.getPairPosition(lf.getEm(), onLeft) - overhang;//ems - (lf.getEm() - lf.getBm() )
            
            lf.setBms(bms);
            lf.setEms(ems); 
            return !containsLoop(lf.getFold().substring(lf.getBms(), lf.getEms() + 1));
        }
        


        return true;
    }

    private void determineLoop2(LFold_Precursor_Entity lf) {

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

    private boolean determineMiRNAs(LFold_Precursor_Entity lf, Aligned_Sequences_Entity s, ArrayList<Patman> clusters) {

        int posGen = lf.getPosOnGenome();

        int allowedInLoop = MiRCat2Params.inLoop;

        int bl = lf.getBl() + posGen + allowedInLoop;
        int el = lf.getEl() + posGen - allowedInLoop;

        int over = 0;

        if (s.getEnd1() <= bl && s.getStart1() >= posGen) { //- allowedOutside
            over = posGen - s.getStart1();

            if (over > 0) {
                return false;
            }
        } else if (s.getStart1() >= el && s.getEnd1() <= posGen + lf.getFold().length()) { //+ allowedOutside) {
            over = s.getEnd1() - posGen - lf.getFold().length();

            if (over > 0) {
                return false;
            }
        } else {
            return false;
        }

        if (s.getEnd1() <= bl && s.getStart1() >= posGen) {

            lf.setBm(s.getStart1() - posGen);
            lf.setEm(Math.min(bl - posGen, s.getEnd1() - posGen));
            
            lf.setMiRNASide(-1);
        } else if (s.getStart1() >= el && s.getEnd1() <= posGen + lf.getFold().length() - 1) {

            lf.setBms(Math.max(el - posGen, s.getStart1() - posGen));
            lf.setEms(s.getEnd1() - posGen);
            
            lf.setMiRNASide(1);

        } else {
            return false;
        }

        return true;

    }


    public void makeClusters() {

        for (Aligned_Sequences_Entity s : sRNAs) {
            makeClusters(s, sequences.get(s));
        }

    }

//    public void prepareSequences() {
//
//        makeClusters();
//        keepOnlyOneWhenOverlapping();
//    }
    
     public void prepareSequences(Aligned_Sequences_Entity s) {

        makeClusters(s, sequences.get(s));
       // keepOnlyOneWhenOverlapping(s, sequences.get(s));
    }

    public void removeSizeClassDistr() {

        for (int i = 0; i < sRNAs.size(); i++) {
            Aligned_Sequences_Entity s = sRNAs.get(i);
            SizeClassDistribution scd = new SizeClassDistribution(s, sequences.get(s));

            if (!scd.isSizeDistributionOK()) {
                sRNAs.remove(i);
                removeSRNA(s);
                i--;
            }

        }
    }

    private boolean sizeClassDistrOK(Aligned_Sequences_Entity s) {

        SizeClassDistribution scd = new SizeClassDistribution(s, sequences.get(s));

        if (!scd.isSizeDistributionOK()) {
            return false;

        }

        return true;

    }

    private void makeClusters(Aligned_Sequences_Entity s, Patman list) {

        int index = 0;
        ArrayList<Patman> cls = new ArrayList<>();

        Patman cl = new Patman();

        try {
            cl.add(list.get(0));
        } catch (Exception e) {
            System.out.println(s.toStringPatman() + " @@@@");
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

        this.clusters.put(s, cls);
        this.clusterIndex.put(s, index);

    }

    private void flipClustersIndexes(Aligned_Sequences_Entity s) {

        ArrayList<Patman> cls = clusters.get(s);
        int index = clusterIndex.get(s);

        int size = cls.size();
        for (int i = 0; i < size / 2; i++) {
            Patman temp = cls.get(i);
            cls.set(i, cls.get(size - i - 1));
            cls.set(size - i - 1, temp);
        }

        clusterIndex.put(s, size - index - 1);
    }


    private void removeFoldsTooShort(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (folds.get(i).getFold().length() < MiRCat2Params.minFoldLen) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

   
    private void removeFoldsBadAmfe(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }

        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (folds.get(i).getAmfe() > MiRCat2Params.maxAmfe) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

  
    private void removeFoldsNotAHairpin(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

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

    private void removeFoldsTooManyGaps(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (hasTooManyGapsInsideMiRNA(folds.get(i), MiRCat2Params.gapsInMirna, false)) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }

    }

    private boolean isAHairpin(LFold_Precursor_Entity lf) {

        if (lf.getBm() == -1 || lf.getBms() == -1) {
            return false;
        }

        if (lf.getBms() - lf.getEm() <= MiRCat2Params.minLoop - 1) {
            return false;
        }

        if (lf.getEl() - lf.getBl() < MiRCat2Params.minLoop - 1) {
            return false;
        }

        int noLoops = noOfLoops(lf.getFold().substring(lf.getEm(), lf.getBms()));
        if (!MiRCat2Params.allowComplexLoop && noLoops > 1) {
            return false;
        } else if (MiRCat2Params.allowComplexLoop && noLoops > MiRCat2Params.noLoops) {
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

    private boolean hasTooManyGapsInsideMiRNA(LFold_Precursor_Entity lf, int gapsMax, boolean both) {
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

   
    private void removeFoldsBasedOnOrientation(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

        for (int i = 0; i < folds.size(); i++) {

            if (!checkOrientation(folds.get(i), s, MiRCat2Params.minOrientation)) {
                folds.remove(i);
                i--;
            }
        }
        if (LFold.get(s).isEmpty()) {
            LFold.remove(s);
        }
    }

    private void removeFoldsParedPercent(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

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

    private void removeFoldsParedNucl(Aligned_Sequences_Entity s) {

        if (!LFold.containsKey(s)) {
            return;
        }
        ArrayList<LFold_Precursor_Entity> folds = LFold.get(s);

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


    private boolean checkParedNucl(LFold_Precursor_Entity get) {
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

        return paired1 >= MiRCat2Params.minParedNucl || paired2 >= MiRCat2Params.minParedNucl;
    }

    private boolean checkPared(LFold_Precursor_Entity get) {
        int gapsTotal = 0;
        for (int i = 0; i < get.getFold().length(); i++) {
            if (get.getFold().charAt(i) == '.') {
                gapsTotal++;
            }
        }
        return gapsTotal / (double) get.getFold().length() < MiRCat2Params.minParedPerc;
    }

    private boolean checkMiRStar(LFold_Precursor_Entity lf, Aligned_Sequences_Entity next) {
        if(lf.getMiRNASide() == -1){
            if(next.getStart1() - lf.getPosOnGenome() < lf.getEl() - MiRCat2Params.clearCut)
                return false;
            if(next.getEnd1() - lf.getPosOnGenome() > lf.getFold().length() -1 + MiRCat2Params.clearCut)
                return false;
            if (next.getStart1() - lf.getPosOnGenome() - lf.getEm() < MiRCat2Params.minLoop) {
                return false;
            }
        }
        else if(lf.getMiRNASide() == 1){
            if(next.getStart1() - lf.getPosOnGenome() < -MiRCat2Params.clearCut)
                return false;
            if(next.getEnd1() - lf.getPosOnGenome() > lf.getBl() + MiRCat2Params.clearCut)
                return false;
            if (lf.getBms() - next.getEnd1() + lf.getPosOnGenome() < MiRCat2Params.minLoop) {
                return false;
            }
        }
        
        
         
        return true;
    }

    private ArrayList<LFold_Precursor_Entity> parseFolds(String toFold, String result, boolean isNegative,
            int beg, Aligned_Sequences_Entity s) {
        ArrayList<LFold_Precursor_Entity> folds = new ArrayList<>();

        String line = null;
        int len = toFold.length();
        
        String lines[] = result.split("~");
        for (int i = 0; i < lines.length; i++) {

            
            line = lines[i];
            String parts[] = line.split(" ");
            
            if(parts.length <= 2)
                continue;
            
            String fold = parts[0];
            double mfe = 0;
            int pos = 0;
            
            int count = 0;
            
            for(int j = 1; j < parts.length; j++){
                if(!parts[j].isEmpty() && !parts[j].equals("(")){
                    if(count == 0){
                        int b = 0, e = parts[j].length() - 1;
                        if(parts[j].charAt(0) == '('){
                            b = 1;
                        }
                        mfe = Double.parseDouble(parts[j].substring(b, e));
                        count = 1;
                    }
                    else{
                        pos = Integer.parseInt(parts[j]) - 1;
                    }   
                }
            }
            
            String seq = toFold.substring(pos, pos + fold.length());
            
            if(!isNegative){
                folds.add(new LFold_Precursor_Entity(beg + pos, fold, mfe, seq, s.getChromosome(), s.getStrand()));
            }
            else{
                int flipedBeg = len - (pos + fold.length());
                 folds.add(new LFold_Precursor_Entity(beg + flipedBeg, SequenceUtils.FOLD.reverseComplement(fold), 
                         mfe, seq, s.getChromosome(), s.getStrand()));
            }

        }

        return folds;
    }

    public void filterByRANDFold(Aligned_Sequences_Entity s) {
        try {
            // Create file
            if (!LFold.containsKey(s)) {
                return;
            }

            String runtimepath = this.path ;//+ DIR_SEPARATOR ;

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

            LFold_Precursor_Entity lf = LFold.get(s).get(0);

            int bLFrel = lf.getPosOnGenome();

            String sequence = LFold.get(s).get(0).getSequence();

            out.write(">" + s.getChromosome() + "-" + s.getStart1() + "-" + s.getEnd1() + "-" + s.getAb() + "-"
                    + bLFrel + "-" + lf.getFold() + "\n");
            out.write(sequence + "\n");

            out.close();
            fstream.close();

            String instruction = " -d \"" + filePath + "\" 100";
            System.out.println(instruction);
            
            String res = myExeMan.execRandFold(instruction);

            String partsRAND[] = res.split("\t");
            double pVal = Double.parseDouble(partsRAND[2].trim());

            if (pVal > MiRCat2Params.pVal) {
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

  
    private void checkFuzzy(Aligned_Sequences_Entity s) {
        if (!LFold.containsKey(s)) {
            return;
        }

        LFold_Precursor_Entity lf = LFold.get(s).get(0);

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


            for (Aligned_Sequences_Entity scl : cl) {
                if (scl.getStart1() < bm) {
                    if (scl.getEnd1() - bm > MiRCat2Params.clearCut) {
                        sumFuzzy += scl.getAb();
                    } else {
                        if(scl.getStart1() >= lf.getPosOnGenome())
                            sumOK += scl.getAb();
                    }
                } else if (scl.getStart1() >= bm && scl.getEnd1() <= ems) {

                    if (sRNAUtils.isInSameCluster(scl, bl, el)) 
                    {
                        sumOK += scl.getAb();
                    } else if ((scl.getStart1() < bl
                            && (em - scl.getStart1() > MiRCat2Params.clearCut || scl.getEnd1() - bl > MiRCat2Params.clearCut))
                            || (scl.getStart1() >= bl
                            && (el - scl.getStart1() > MiRCat2Params.clearCut || scl.getEnd1() - bms > MiRCat2Params.clearCut))) {
                        sumFuzzy += scl.getAb();
                    } else {
                        sumOK += scl.getAb();
                    }
                } else if (scl.getStart1() > bms) {
                    if (ems - scl.getStart1() > MiRCat2Params.clearCut) {
                        sumFuzzy += scl.getAb();
                    } else {
                        if(scl.getEnd1() < lf.getPosOnGenome() + lf.getFold().length())
                            sumOK += scl.getAb();
                    }
                }
            }
        }

        for (Aligned_Sequences_Entity scl : clusters.get(s).get(clusterIndex.get(s))) {
            if (scl.getStart1() < bl) {
                if (bm - scl.getStart1() > MiRCat2Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
                if (scl.getEnd1() - em > MiRCat2Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
            } else {
                if (bms - scl.getStart1() > MiRCat2Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
                if (scl.getEnd1() - ems > MiRCat2Params.clearCut) {
                    sumOK -= scl.getAb();
                    sumFuzzy += scl.getAb();
                }
            }
        }

        if (mirsIndex != -1) {
            for (Aligned_Sequences_Entity scl : clusters.get(s).get(mirsIndex)) {
                if (scl.getStart1() < bl) {
                    if (bm - scl.getStart1() > MiRCat2Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                    if (scl.getEnd1() - em > MiRCat2Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                } else {
                    if (bms - scl.getStart1() > MiRCat2Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                    if (scl.getEnd1() - ems > MiRCat2Params.clearCut) {
                        sumOK -= scl.getAb();
                        sumFuzzy += scl.getAb();
                    }
                }
            }
        }

        if (sumOK / ((double) sumOK + sumFuzzy) < MiRCat2Params.fuzzy) {
            LFold.remove(s);
        }
        else {
            int scoreAb = (mirsIndex != -1) ? clusters.get(s).get(mirsIndex).getAb() : 0;
            scoreAb += clusters.get(s).get(clusterIndex.get(s)).getAb();
            lf.setAbundance(scoreAb);
        }

    }
    
    private Aligned_Sequences_Entity getBestMirStar(Patman cl, LFold_Precursor_Entity lf) {
        
        if(lf.getMiRNASide() < 0){
            return sRNAUtils.bestSRNA(cl, lf.getPosOnGenome() + lf.getBms(), lf.getPosOnGenome() + lf.getEms());
        }
        else if(lf.getMiRNASide() > 0){
            return sRNAUtils.bestSRNA(cl, lf.getPosOnGenome() + lf.getBm(), lf.getPosOnGenome() + lf.getEm());
        }
        
        return null;
    }

    public void printResultsToFiles(BufferedWriter outPatman, BufferedWriter outCSV, MiRCat2SceneController controller) throws IOException {
        for (Aligned_Sequences_Entity s : sRNAs) {

            if (!LFold.containsKey(s)) {
                continue;
            }

            printOneSRNA(s, outPatman, false, false);
            outPatman.flush();

            String csvLine = printOneSRNACSV(s, false);
            
            outCSV.append(csvLine);
            outCSV.append("\n");
            outCSV.flush();
            
            if(!AppUtils.INSTANCE.isCommandLine())
                controller.outputToGUI(csvLine);
        }
    }
       

}
