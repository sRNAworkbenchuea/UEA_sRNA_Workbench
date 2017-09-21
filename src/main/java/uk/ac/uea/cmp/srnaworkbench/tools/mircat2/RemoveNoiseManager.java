/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2;

import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes.*;
import java.util.HashMap;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;





/**
 *
 * @author keu13sgu
 */
public class RemoveNoiseManager {

    static final int totalSW = (MiRCat2Params.WINDOW % MiRCat2Params.SUBWINDOW == 0 ? MiRCat2Params.WINDOW / MiRCat2Params.SUBWINDOW : MiRCat2Params.WINDOW / MiRCat2Params.SUBWINDOW + 1);
    static final double probRUD = (double)1 / (double)(totalSW);

    private final int genLength;      

    private final Session session;
    private final String chrom;
    

     public RemoveNoiseManager(Session session, String chrom, int genLen) {

        this.session = session;
        this.chrom = chrom;
        genLength = genLen;
    }
    
    public Patman removeNoise() {
        Patman removeNoise = coveredGenome();      
        return removeNoise;
    }

    private boolean isInBounds(Aligned_Sequences_Entity get, int b, int e) {

        int len = get.getLength();
        int cov = len;

        if (get.getStart1() < b) {
            cov -= (b - get.getStart1());
        }

        if (get.getEnd1() > e) {
            cov -= (get.getEnd1() - e);
        }

        return cov >= (len % 2 == 0 ? len /2 : len/2 + 1);
    }

       private int removeDoublesOnDiffrentStrands(Patman p){
  
            int sumPlus = 0;
            int sumMinus = 0;
            
            int rem = 0;
            
            for(int i = 0; i< p.size(); i++){
                Aligned_Sequences_Entity s1 = p.get(i);
                if(s1.isNegative()){
                    sumMinus += s1.getAb();
                }
                else{
                    sumPlus += s1.getAb();
                }
            }
            
            boolean isNegative = sumMinus > sumPlus;

            for(int i = 0; i< p.size(); i++){
                for(int j = i + 1; j< p.size(); j++){
                    
                    Aligned_Sequences_Entity s1 = p.get(i);
                    Aligned_Sequences_Entity s2 = p.get(j);
                    
                    if(s1.isSameSequence(s2) && !s1.isSameStrand(s2)){
                        
                        if(Math.abs(s1.getStart1() - s2.getEnd1()) < MiRCat2Params.foldDist ||
                                Math.abs(s2.getStart1() - s1.getEnd1()) < MiRCat2Params.foldDist){
                       
                            if(isNegative){
                                if(!s1.isNegative()){
                                    p.remove(i);
                                    session.delete(s1);
                                    rem++;
                                    i--;
                                    break;
                                }
                                if(!s2.isNegative()){
                                    p.remove(j);
                                    session.delete(s2);
                                    j--;
                                    rem++;
                                }

                            }
                            else{
                                if(s1.isNegative()){
                                    p.remove(i);
                                    session.delete(s1);
                                    i--;
                                    rem++;
                                    break;
                                }
                                if(s2.isNegative()){
                                    p.remove(j);
                                    session.delete(s2);
                                    rem++;
                                    j--;
                                }
                            }

                        }
                    }
                }
            }
            return rem;
    }

    private int mostAbundantsRNAofSegments(Patman srnas, int b, int e) {
        int j = 0;
        long maxSeg = 0;

        for (int k = b; k <= e - MiRCat2Params.SUBWINDOW; k += MiRCat2Params.SUBWINDOW) {
            long segAb = 0;
            long localMaxSrna = 0;
            int localIndex = 0;

            for (int i = 0; i < srnas.size(); i++) {
                if (isInBounds(srnas.get(i), k, k + MiRCat2Params.SUBWINDOW - 1)) {
                    segAb += srnas.get(i).getAb();
                    if (localMaxSrna < srnas.get(i).getAb()) {
                        localMaxSrna = srnas.get(i).getAb();
                        localIndex = i;
                    }
                }
            }

            if (maxSeg < segAb) {
                maxSeg = segAb;
                j = localIndex;
  
            }
        }

        return j;
    }
    
    private int mostAbundantSegment(HashMap<Integer, Double> abundances ) {
        int j = -1;
        double max = 0;
        for(Integer i : abundances.keySet()){
            if(abundances.get(i) > max){
                max = abundances.get(i);
                j = i;
            }
        }
        return j;
    }

 
     private int getSRNAsBetweenBounds(int poz, int beg, int end, Patman srnas, Patman newSRnas){

         int newpoz = poz;
         int lend = end - MiRCat2Params.WINDOWOVERLAP;
         boolean found = false;


        int i;
        for (i = poz; i < srnas.size(); i++) {
            int b = srnas.get(i).getStart1();
            int e = srnas.get(i).getEnd1();
            
            if (((b < beg || e > end) && isInBounds(srnas.get(i), beg, end - 1))
                    || (b >= beg && e <= end)) {
                
                if(!found && isInBounds(srnas.get(i), lend, end - 1)){
                    newpoz = i;
                    found = true;
                }
             
                newSRnas.add(srnas.get(i));

            } else {
                if(!found && b > beg) {newpoz = i; found = true;}
                if(b > end) {
                    break;
            }     
            }
        }
        if(i >= srnas.size()){
           newpoz = srnas.size() + 1; 
        }

        return newpoz;
     }
     
     private int getSRNAsBetweenBounds(int beg, int end, ScrollableResults srnas, Patman newSRnas){

         int lend = end - MiRCat2Params.WINDOWOVERLAP;
         boolean found = false;
         int newPoz = 0;


       int i = 0;
       while(srnas.next()){
           Aligned_Sequences_Entity s = (Aligned_Sequences_Entity)srnas.get(0);
           i++;
           
            int b = s.getStart1();
            int e = s.getEnd1();
            
            if (((b < beg || e > end) && isInBounds(s, beg, end - 1))
                    || (b >= beg && e <= end)) {
                
                if(!found && isInBounds(s, lend, end - 1)){
                    newPoz = i - 1;
                    found = true;
                }
             
                newSRnas.add(s);

            } else {
                if(!found && b > beg) {
                    newPoz = i -1  ; 
                    found = true; 
                    break;
                }
                if(b > beg){
                    newPoz = i - 1;
                    break;
                }
   
            }
        }
        if(!srnas.next()){
           newPoz = i ; 
        }
        else{
            newPoz -= 1;
        }

        return i - newPoz;
     }
     
     private Patman coveredGenome() {

         Patman removedSRNA = new Patman();
         HashMap<GenomeSequence, Patman> genSeqs = new HashMap<>();
         int poz = 0;
         
         String hql = "from Aligned_Sequences_Entity s where s.id.chrom = :chromosome order by s.id.start";
        
         ScrollableResults findAll = session.createQuery(hql)
                    .setParameter("chromosome", chrom)
                    .scroll();
         
         while(findAll.next()){
             Aligned_Sequences_Entity s = (Aligned_Sequences_Entity)findAll.get(0);
             
             
             int b = Math.max(0, s.getStart1() - MiRCat2Params.SUBWINDOW );
             int e = Math.min(b + MiRCat2Params.WINDOW, genLength);
     
             Patman cl = new Patman();
             cl.add(s);
             
              if (!cl.isEmpty()) {
                 GenomeSequence newGen = new GenomeSequence("_" + b, b, e);

                  //	10	TTGTGCTTGATCTAACCATGCA(132)	17725495	17725516	+	0
//                 if (b<= 17725495 && e>= 17725495) { //TTGTGCTTGATCTAACCATGC
//                     System.out.println("");
//                 }
               

                poz = getSRNAsBetweenBounds(b, e, findAll, cl);

               //		4	TAAGTGCTTCTCTTTGGGGTAG(4597)	28001570	28001591	+	0
//                 if (b<= 28001570 && e>= 28001591) { //TTGTGCTTGATCTAACCATGC
////                 if(s.getRna_seq().equals("TTGTGCTTGATCTAACCATGC") && 
////                         s.getAb() == 13 && s.getChromosome().equals("10")){
//
//                     for (int k = 0; k < e - b + 1; k++) {
//                         System.out.print("*");
//                     }
//                     System.out.println("");
//                     for (Aligned_Sequences_Entity ss : cl) {
//                         System.out.println(ss.myToStringSeq(b));
//                     }
//
//                     System.out.flush();
//                 }
             
            
                 
                 int removedNo = removeDoublesOnDiffrentStrands(cl);
                 Patman temp = removeNoiseOnGenomePart(newGen, cl);
                
                 if(temp != null && !temp.isEmpty()){
                     Patman kept = keepOnlyOneWhenOverlapping(temp);
                    sRNAUtils.addToPatman(kept, removedSRNA);
                }

                 
                 
             }

             findAll.scroll(- (poz ));
         }
         
         return removedSRNA;
    }
    
    
     private Patman keepOnlyOneWhenOverlapping(Patman srnas){
        srnas.sort();
        Patman kept  = new Patman();
        Patman temp  = new Patman();
        
        for(Aligned_Sequences_Entity s: srnas){
           
            
            if(temp.isEmpty()){
                temp.add(s);
            }
            else
                if(overlap(temp, s) //||
//                        Math.abs(s.getStart1() - temp.get(0).getStart1())  <= MiRCat2Params.SUBWINDOW || 
//                        (Math.abs(s.getStart1() - temp.get(0).getEnd1())  <= MiRCat2Params.SUBWINDOW
//                        || Math.abs(temp.get(0).getStart1() - s.getEnd1())  <= MiRCat2Params.SUBWINDOW  )             
                ){
                temp.add(s);
            }
            else{
                kept.add(sRNAUtils.bestSRNA(temp));
                temp.clear();
                temp.add(s);
            }
        }
        kept.add(sRNAUtils.bestSRNA(temp));
        
        return kept;
        
    }
    
     
     private boolean overlap(Patman s1, Aligned_Sequences_Entity s2){
         
         if(s1.getBeg() <= s2.getStart1()){
             return s2.getStart1() <= s1.getEnd();
         }
         return s1.getBeg() <= s2.getEnd1();
     }
     
    private int getPozForSRNA(GenomeSequence genSeq, Aligned_Sequences_Entity srna){
        int poz = genSeq.getBeg();
        
        //how many segments before beg of srna
        int x = (srna.getStart1() - poz) / MiRCat2Params.SUBWINDOW;
        
        // the coordonte for first potential beg 
        poz = (x * MiRCat2Params.SUBWINDOW) + poz; 
        
        if(!isInBounds(srna, poz, poz + MiRCat2Params.SUBWINDOW - 1))
            poz += MiRCat2Params.SUBWINDOW;
        
        return poz;
    }
    
    private double getAbundances(GenomeSequence genSeq, Patman sRNAsOnGenSeq, HashMap<Integer, Double> abundances){
        double totalAb  = 0;
        int i;
        
        for(i = genSeq.getBeg(); i< genSeq.getEnd(); i+= MiRCat2Params.SUBWINDOW){    
                abundances.put(i, 0.0);
        }
         
        for (Aligned_Sequences_Entity c : sRNAsOnGenSeq) {
            i = getPozForSRNA(genSeq, c);
            
            if(i < genSeq.getBeg()) i = genSeq.getBeg();
            if(i >= genSeq.getEnd()) continue;
            
            abundances.put(i, abundances.get(i) + c.getAb());
            
            totalAb += c.getAb();
        }        
        
        
        double offset = (totalAb < totalSW) ? MiRCat2Params.offsetLow : MiRCat2Params.offset;
        for(i = genSeq.getBeg(); i< genSeq.getEnd(); i+= MiRCat2Params.SUBWINDOW){    
                abundances.put(i, abundances.get(i) + offset);
        }
        totalAb += offset * totalSW;

        return totalAb;
    }

    
     private Patman removeNoiseOnGenomePart(GenomeSequence genSeq, Patman sRNAsOnGenSeq) {

        Patman removedSRNA = new Patman();
  
        HashMap<Integer, Double> abundances = new HashMap<>();
        HashMap<Integer, Double> initialAbundances = new HashMap<>();
        double totalAbun = getAbundances(genSeq, sRNAsOnGenSeq, abundances);
        initialAbundances.putAll(abundances);
        
        HashMap<Integer, Double> probabilities = new HashMap<>();
        

        double rez = calculateKLD( null, totalAbun, abundances, probabilities, genSeq);

        // if rez < MiRCat2Params.UDVAL, then the distribution is alredly uniform
        if (Math.abs(rez) > MiRCat2Params.UDVAL) {

            Patman temp = new Patman();
            try{
             double offset = (totalAbun < totalSW) ? MiRCat2Params.offsetLow : MiRCat2Params.offset;
             recursiveCheck(0, rez, sRNAsOnGenSeq, new Patman(), genSeq,
                    temp, totalAbun, abundances, initialAbundances, 0, probabilities, offset);
             
            }catch(Throwable e){
                System.out.println(sRNAsOnGenSeq.get(0).toStringPatman());
            }
            if(!temp.isEmpty()){
               removedSRNA.addAll(temp);
            }
          
        } else {

        }

        return removedSRNA;

    }
     
  private double recursiveCheck(int depth, double rez, Patman sRNAsOnGenSeq, 
            Patman sRNAsOnGenSeq1, GenomeSequence genSeq, Patman temp, double totalAbun,
            HashMap<Integer, Double> abundances, HashMap<Integer, Double> initialAbundances, 
            int toRemove, HashMap<Integer, Double> probabilities, double offset) {
        
        Patman sRNAsOnGenSeq2 = new Patman();
        
        if (MiRCat2Params.DEPTH  == depth) {
            for(int i = 1; i<= depth && i < temp.size() && i < toRemove; i++ ){
                temp.remove(temp.size() - 1);
            }
            return rez;
        }
        
        if(Math.abs(rez) < MiRCat2Params.UDVAL)
            return rez;

        if (sRNAsOnGenSeq.isEmpty()) { 
            for(int i = 1; i<= depth && i < temp.size()  && i < toRemove; i++ ){
                temp.remove(temp.size() - 1);
            }
            return 0;
        }
        if (depth == 0) {
            sRNAsOnGenSeq1.clear();
            sRNAsOnGenSeq1.addAll(sRNAsOnGenSeq);
        }
        else {
            if (sRNAsOnGenSeq1.isEmpty()) { 
            for(int i = 1; i<= depth && i < temp.size()  && i < toRemove; i++ ){
                temp.remove(temp.size() - 1);
            }
            return 0;
            }
        }

        int poz = mostAbundantsRNAofSegments(sRNAsOnGenSeq1, genSeq.getBeg(), genSeq.getEnd());
        int segment = mostAbundantSegment(abundances);
        
        sRNAsOnGenSeq2.clear();
        sRNAsOnGenSeq2.addAll(sRNAsOnGenSeq1);
          
        Aligned_Sequences_Entity s = sRNAsOnGenSeq2.get(poz);
        if(isAPeak(segment, initialAbundances, offset) && isAPeak(segment, abundances, offset) && s.isComplex(MiRCat2Params.complex)){ 
            temp.add(s); 
            toRemove++;
        }
        totalAbun -= sRNAsOnGenSeq2.get(poz).getAb();
        sRNAsOnGenSeq2.remove(poz);
        
        
        double rez1 = calculateKLD(s,totalAbun, abundances, probabilities, genSeq);

        if(rez1 == 0 ) return 0;
        
        if (Math.abs(rez1) >= Math.abs(rez)) {
               rez = recursiveCheck(depth + 1, rez, sRNAsOnGenSeq, sRNAsOnGenSeq2,
                       genSeq, temp, totalAbun, abundances, initialAbundances, toRemove,probabilities, offset);
        } else {
                rez = rez1;

                 sRNAsOnGenSeq.clear();
                 sRNAsOnGenSeq.addAll(sRNAsOnGenSeq2);
             try{    
                 
                 rez = recursiveCheck(0, rez, sRNAsOnGenSeq, sRNAsOnGenSeq2,
                         genSeq, temp, totalAbun, abundances,initialAbundances, toRemove,probabilities, offset);
             }catch(Throwable e){
                 throw e;
             }
            }

        return rez;

  }
  
 
    
    private boolean isAPeak(int segment, HashMap<Integer, Double> abund, double offset) {

        HashMap<Integer, Double> localProbs = new HashMap<>();

        double sum = 0;
        int range =  MiRCat2Params.range;
        for (int j = -range; j <= range; j++) {
            if (abund.containsKey(segment - j * MiRCat2Params.SUBWINDOW)) {
                sum += abund.get(segment - j * MiRCat2Params.SUBWINDOW);
            }
            else{
                sum += offset;
            }
        }
        
         for (int j = -range; j <= range; j++) {
            if (abund.containsKey(segment - j * MiRCat2Params.SUBWINDOW)) {   
                localProbs.put(segment - j * MiRCat2Params.SUBWINDOW, abund.get(segment - j * MiRCat2Params.SUBWINDOW)/ sum);
            }
            else{
                localProbs.put(segment - j * MiRCat2Params.SUBWINDOW, offset/ sum);
            }
        }

        double rudProb = 1 / (double) localProbs.keySet().size();
        double rez = KLD.calculateKLD(localProbs, rudProb);

        return Math.abs(rez) > MiRCat2Params.UDVAL;
    }
    
    
    private HashMap<Integer, Double> probabilities(HashMap<Integer, Double> abun, Aligned_Sequences_Entity srna, double totalAbun, GenomeSequence gs){
        HashMap<Integer, Double> probs = new HashMap<>();
        
   
        if(srna != null){
            
            int poz = getPozForSRNA(gs, srna);
            abun.put(poz, abun.get(poz) - srna.getAb());
            
            for(Integer i: abun.keySet()){
                double a = abun.get(i);
  
                double p = 0;
                if(a != 0){
                    p = (a / (double)totalAbun);
                }

                probs.put(i, p);

            }
        }
        else{
            for(Integer i: abun.keySet()){
                double a = abun.get(i);
                double p = 0;
                if(a != 0){
                    p = (a / (double)totalAbun);
                }
                probs.put(i, p);
            }    
        }
        
        return probs;
    }
    
    private double calculateKLD(Aligned_Sequences_Entity srna, double totalAbun, HashMap<Integer, Double> abundances, HashMap<Integer, Double> myDistProb, GenomeSequence gs ) {
        
        // calculate probabilities for my actual distribution
        myDistProb.clear();
        myDistProb.putAll(probabilities(abundances, srna, totalAbun, gs));
       
        //calculate the KL Distance
        double sum = KLD.calculateKLD(myDistProb, probRUD);
        return sum;
    }
   
}

