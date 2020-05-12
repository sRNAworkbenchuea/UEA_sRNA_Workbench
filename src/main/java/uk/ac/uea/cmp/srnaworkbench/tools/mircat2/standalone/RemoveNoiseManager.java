/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.sRNA;

import java.util.HashMap;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.GenomeSequence;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.KLD;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.Patman;


/**
 *
 * @author keu13sgu
 */
public class RemoveNoiseManager {
    
    ////##################################################################################################################
    //sRNA toWatch = sRNA.readFromPatman("chr1	TTATTGCTTAAGAATACGCGTAG(1719)	98046091	98046113	-	0");
    
    public static int total = 0;
    private int level = 1;
           
    static final int totalSW = (Params.WINDOW % Params.SUBWINDOW == 0 ? Params.WINDOW / Params.SUBWINDOW : Params.WINDOW / Params.SUBWINDOW + 1);
    static final double probRUD = (double)1 / (double)(totalSW);

    private final GenomeSequence genome;
    Patman allSRNA;
    
    public Patman getFinalSRNAs(){
        return allSRNA;
    }

    public RemoveNoiseManager(Patman sRNAs, GenomeSequence genome, int min1) {
        this.allSRNA = sRNAs;
        this.genome = genome;
        level = min1;
    }
    
    public Patman removeNoise() {

        HashMap<GenomeSequence, Patman> genCl;

        Patman removeNoise = coveredGenome(genome, allSRNA);
       // Patman removeNoise = removeNoise(genCl);
       
        return removeNoise;

    }

    private boolean isInBounds(sRNA get, int b, int e) {

        int len = get.getLength();
        int cov = len;

        if (get.getBeg() < b) {
            cov -= (b - get.getBeg());
        }

        if (get.getEnd() > e) {
            cov -= (get.getEnd() - e);
        }

        return cov >= (len % 2 == 0 ? len /2 : len/2 + 1);
    }

       private int removeDoublesOnDiffrentStrands(Patman p){
  
            int sumPlus = 0;
            int sumMinus = 0;
            
            int rem = 0;
            
            for(int i = 0; i< p.size(); i++){
                sRNA s1 = p.get(i);
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
                    
                    sRNA s1 = p.get(i);
                    sRNA s2 = p.get(j);
                    
                    if(s1.isSameSequence(s2) && !s1.isSameStrand(s2)){
                        
                        if(Math.abs(s1.getBeg() - s2.getEnd()) < Params.foldDist ||
                                Math.abs(s2.getBeg() - s1.getEnd()) < Params.foldDist){
                       
                            if(isNegative){
                                if(!s1.isNegative()){
                                    p.remove(i);
                                    allSRNA.remove(s1);
                                    rem++;
                                    i--;
                                    break;
                                }
                                if(!s2.isNegative()){
                                    p.remove(j);
                                    allSRNA.remove(s2);
                                    j--;
                                    rem++;
                                }

                            }
                            else{
                                if(s1.isNegative()){
                                    p.remove(i);
                                    allSRNA.remove(s1);
                                    i--;
                                    rem++;
                                    break;
                                }
                                if(s2.isNegative()){
                                    p.remove(j);
                                    allSRNA.remove(s2);
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

        for (int k = b; k <= e - Params.SUBWINDOW; k += Params.SUBWINDOW) {
            long segAb = 0;
            long localMaxSrna = 0;
            int localIndex = 0;

            for (int i = 0; i < srnas.size(); i++) {
                if (isInBounds(srnas.get(i), k, k + Params.SUBWINDOW - 1)) {
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
         int lend = end - Params.WINDOWOVERLAP;
         boolean found = false;
         
//         if(lend- beg < Params.WINDOW - Params.WINDOWOVERLAP)
//             return srnas.size() + 1;

        int i;
        for (i = poz; i < srnas.size(); i++) {
            int b = srnas.get(i).getBeg();
            int e = srnas.get(i).getEnd();
            
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
     
     private Patman coveredGenome(GenomeSequence genome, Patman srnas) {

         //Patman removeNoise = removeNoise(genCl);
         Patman removedSRNA = new Patman();
         srnas.sort();
         HashMap<GenomeSequence, Patman> genSeqs = new HashMap<>();
         int poz;
  
         for (int i = 0; i < srnas.size(); i++) {
             
             sRNA s = srnas.get(i);
           //  System.out.println(s.toStringPatman());
             
             int b = Math.max(0, s.getBeg() - Params.SUBWINDOW );
             int e = Math.min(b + Params.WINDOW, genome.getSequence().length());
     
             Patman cl = new Patman();

             poz = getSRNAsBetweenBounds(i, b, e, srnas, cl);

             if (!cl.isEmpty()) {
                 GenomeSequence newGen = new GenomeSequence(genome.getId() + "_" + b, b, e);
                 //genSeqs.put(newGen, temp);
                 
         
                 //TCGGACCAGGCTTCATTCCTC	31720	84381908	84381928

                 //SL2.40ch01 2075706 2076206


               //  if(cl.getBeg() <= 2075706 && cl.getEnd() >= 2075706)
//                 if(b <= 2075706 && e >= 2076206)
//                 if (contains(cl, "GACGGACCGTCGTGCCT", 3)) {
//                     //System.out.println("itworks");
//
//                     System.out.println(cl.get(0).getId() + " " + b + " " + e );
//                     for (int k = 0; k < e - b + 1; k++) {
//                         System.out.print("*");
//                     }
//                     System.out.println("");
////                     for (sRNA ss : cl) {
////                         System.out.println(ss.myToStringSeq(b)+ " " + ss.getStrand());
////         //                System.out.println();
////                     }
////                     System.out.flush();
//                     
//                     specialPrint(cl, b);
//                 }
                 
                 int removedNo = removeDoublesOnDiffrentStrands(cl);
                 Patman temp = removeNoiseOnGenomePart(newGen, cl);
                
                 if(temp != null && !temp.isEmpty()){
                     Patman kept = keepOnlyOneWhenOverlapping(temp);
                    sRNAUtils.addToPatman(kept, removedSRNA);
                }

                 i = i - removedNo;
                 total ++;
                 
             }

             i = poz - 1;
         }
         
         return removedSRNA;
    }
     
     private boolean contains(Patman p, String str, int ab){
         for(sRNA s: p){
             if(s.getSequence().equals(str) && s.getAb() == ab)
                 return true;
         }
         return false;
     }
     
     private void specialPrint(Patman p, int offset){
         Patman clone = (Patman) p.clone();
             
         
         int i = 0;
         String line = "";
         while(!clone.isEmpty()){
             line = "";
             sRNA s = clone.get(0);
             
             for(int j = 0; j < s.getBeg() - offset; j++)
                 line += " ";
             
             line += s.getSequence() + " " + s.getAb() + " " + s.getStrand();
             int newOffset = offset + line.length();
             clone.remove(s);
             
             for(int k = 0; k < clone.size(); k++){
                 sRNA s2 = clone.get(k);
                 
                 if(s2.getBeg() > newOffset){
                     for(int j = 0; j < s2.getBeg() - newOffset; j++){
                         line += " ";
                     }
                     line += s2.getSequence() + " " + s2.getAb() + " " + s2.getStrand();
                     clone.remove(s2);
                     k--;
                     newOffset = offset + line.length();
                 }
             }
             System.out.println(line);
             
         }
         System.out.flush();
     }
    
    private Patman removeNoise(HashMap<GenomeSequence, Patman> genCl) {

        Patman removedSRNA = new Patman();
        
        total += genCl.keySet().size();
        
        for (GenomeSequence genSeq : genCl.keySet()) {
            Patman cl = genCl.get(genSeq); 
//             if(cl.contains(sRNA.readFromPatman("chr1	GCCCGGATAGCTCAGTCGGTAGAGC(4331)	133033962	133033986	+	0"))){
//                 System.out.print(genSeq.getSequence()+ "\n");
////                    for(sRNA s: cl){
////                         System.out.print(s.myToStringSeq(genSeq.getBeg()) + " " + s.getStrand() + "\n");
////                    }
////                     System.out.flush();
//            }
            
            removeDoublesOnDiffrentStrands(cl);
            
            //##################################################################################################################
//            if(cl.contains(sRNA.readFromPatman("chr1	GCCCGGATAGCTCAGTCGGTAGAGC(4331)	133033962	133033986	+	0"))){
//                 System.out.print(genSeq.getSequence()+ "\n");
//                    for(sRNA s: cl){
//                        System.out.print(s.myToStringSeq(genSeq.getBeg()) + " " + s.getStrand() + "\n");
//                    }
//                     System.out.flush();
//            }
           
            Patman temp = removeNoiseOnGenomePart(genSeq,
                    cl);
            if(temp != null && !temp.isEmpty()){
                 Patman kept = keepOnlyOneWhenOverlapping(temp);
                sRNAUtils.addToPatman(kept, removedSRNA);
            }

            //}
        }

        return removedSRNA;

    }
    
     private Patman keepOnlyOneWhenOverlapping(Patman srnas){
        srnas.sort();
        Patman kept  = new Patman();
        Patman temp  = new Patman();
        
        for(sRNA s: srnas){
            if(temp.isEmpty()){
                temp.add(s);
            }
            else //if((s.getBeg() - temp.get(0).getEnd() <= Params.foldDist)
                   // || (temp.get(0).getBeg() - s.getEnd() <= Params.foldDist && temp.get(0).getBeg() - s.getEnd()>0)
                if(Math.abs(s.getBeg() - temp.get(0).getBeg())  <= Params.SUBWINDOW || 
                       ( Math.abs(s.getBeg() - temp.get(0).getEnd())  <= Params.SUBWINDOW
                        || Math.abs(temp.get(0).getBeg() - s.getEnd())  <= Params.SUBWINDOW   )            
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

    private void addUnique(Patman from, Patman to){
        for(sRNA s: from){
            if(!to.contains(s)){
                to.add(s);
            }
        }
    }
    
    private int getPozForSRNA(GenomeSequence genSeq, sRNA srna){
        int poz = genSeq.getBeg();
        
        //how many segments before beg of srna
        int x = (srna.getBeg() - poz) / Params.SUBWINDOW;
        
        // the coordonte for first potential beg 
        poz = (x * Params.SUBWINDOW) + poz; 
        
        if(!isInBounds(srna, poz, poz + Params.SUBWINDOW -1))
            poz += Params.SUBWINDOW;
        
        return poz;
    }
    
    private double getAbundances(GenomeSequence genSeq, Patman sRNAsOnGenSeq, HashMap<Integer, Double> abundances){
        double totalAb  = 0;
        int i;
        
        for(i = genSeq.getBeg(); i< genSeq.getEnd(); i+= Params.SUBWINDOW){    
                abundances.put(i, 0.0);
        }
         
        for (sRNA c : sRNAsOnGenSeq) {
            i = getPozForSRNA(genSeq, c);
            
            if(i < genSeq.getBeg()) i = genSeq.getBeg();
            if(i >= genSeq.getEnd()) continue;
            
            abundances.put(i, abundances.get(i) + c.getAb());
            
            totalAb += c.getAb();
        }        
        
        
        double offset = (totalAb < totalSW) ? Params.offsetLow : Params.offset;
        for(i = genSeq.getBeg(); i< genSeq.getEnd(); i+= Params.SUBWINDOW){    
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

        // if rez < Params.UDVAL, then the distribution is alredly uniform
        if (Math.abs(rez) > Params.UDVAL) {

//            if(sRNAsOnGenSeq.contains(sRNA.readFromPatman("chr21	CGCGCCAGGAAGGGCC(1)	8439759	8439774	+	0"))){
////                for(int i = 0;i < genSeq.getEnd() - genSeq.getBeg() + 1; i++){
////                    System.out.print("*");
////                }
////                System.out.println("");
////                    for(sRNA s: sRNAsOnGenSeq){
////                        System.out.println(s.myToStringSeq(genSeq.getBeg()));
////                    }
//            }
            Patman temp = new Patman();
          //  try{
             double offset = (totalAbun < totalSW) ? Params.offsetLow : Params.offset;
             recursiveCheck(0, rez, sRNAsOnGenSeq, new Patman(), genSeq,
                    temp, totalAbun, abundances, initialAbundances, 0, probabilities, offset);
             
//            }catch(Throwable e){
//                System.out.println(sRNAsOnGenSeq.get(0).toStringPatman());
//            }
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
        
        if (Params.DEPTH  == depth) {
            for(int i = 1; i<= depth && i < temp.size() && i < toRemove; i++ ){
                temp.remove(temp.size() - 1);
            }
            return rez;
        }
        
        if(Math.abs(rez) < Params.UDVAL)
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
          
        sRNA s = sRNAsOnGenSeq2.get(poz);
        if(isAPeak(segment, initialAbundances, offset) && isAPeak(segment, abundances, offset) && s.isComplex(Params.complex)){ 
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
        int range =  Params.range;
        for (int j = -range; j <= range; j++) {
            if (abund.containsKey(segment - j * Params.SUBWINDOW)) {
                sum += abund.get(segment - j * Params.SUBWINDOW);
            }
            else{
                sum += offset;
            }
        }
        
         for (int j = -range; j <= range; j++) {
            if (abund.containsKey(segment - j * Params.SUBWINDOW)) {   
                localProbs.put(segment - j * Params.SUBWINDOW, abund.get(segment - j * Params.SUBWINDOW)/ sum);
            }
            else{
                localProbs.put(segment - j * Params.SUBWINDOW, offset/ sum);
            }
        }

        double rudProb = 1 / (double) localProbs.keySet().size();
        double rez = KLD.calculateKLD(localProbs, rudProb);

        return Math.abs(rez) > Params.UDVAL;
    }
    
    
    private HashMap<Integer, Double> probabilities(HashMap<Integer, Double> abun, sRNA srna, double totalAbun, GenomeSequence gs){
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
    
    private double calculateKLD(sRNA srna, double totalAbun, HashMap<Integer, Double> abundances, HashMap<Integer, Double> myDistProb, GenomeSequence gs ) {
        
        // calculate probabilities for my actual distribution
        myDistProb.clear();
        myDistProb.putAll(probabilities(abundances, srna, totalAbun, gs));
       
        //calculate the KL Distance
        double sum = KLD.calculateKLD(myDistProb, probRUD);
        return sum;
    }
   


   
}

