/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2;

/**
 *
 * @author keu13sgu
 */
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;

/**
 *
 * @author keu13sgu
 */
public class SizeClassDistribution {
    private Aligned_Sequences_Entity Aligned_Sequences_Entity;
    private Patman sequences;
    
    HashMap<Integer, Double> probs = new HashMap<>();

    public SizeClassDistribution(Aligned_Sequences_Entity Aligned_Sequences_Entity, Patman sequences) {
        this.Aligned_Sequences_Entity = Aligned_Sequences_Entity;
        this.sequences = sequences;
    }

    
    public boolean isSizeDistributionOK() {
        
       HashMap<Integer, Double> sizeClassDist = new HashMap<>();
       
       for(int i = MiRCat2Params.minSize; i <= MiRCat2Params.maxSize; i++){
           sizeClassDist.put(i, 0.0);
       }
       
       int totalSW = MiRCat2Params.maxSize - MiRCat2Params.minSize + 1;
       int totalAB = 0;
       
       
       for(Aligned_Sequences_Entity s: sequences){
           int size = s.getLength();
           double count = 0.0;
           
           if(!sizeClassDist.containsKey(size)){
               if(size > MiRCat2Params.maxSize){
                   count = sizeClassDist.get(MiRCat2Params.maxSize);
                   sizeClassDist.put(MiRCat2Params.maxSize, count + s.getAb());
               }
               else
                   if(size < MiRCat2Params.minSize){
                        count = sizeClassDist.get(MiRCat2Params.minSize);
                        sizeClassDist.put(MiRCat2Params.minSize, count + s.getAb());
                    }
           }
           else{
               count = sizeClassDist.get(size);
               sizeClassDist.put(size, count + s.getAb());
           }
           totalAB += count;
       }
       
       double offset = (totalAB < totalSW) ? MiRCat2Params.offsetLow : MiRCat2Params.offset;
       for(int i = MiRCat2Params.minSize; i <= MiRCat2Params.maxSize; i++){
           sizeClassDist.put(i, sizeClassDist.get(i) + offset);
       }

       return calculateKLD(sizeClassDist);

    }
    
    private boolean calculateKLD(HashMap<Integer, Double> sizeClassDist){
        probabilities(sizeClassDist);
        
        
        double probRUD = 1/(double)(MiRCat2Params.maxSize - MiRCat2Params.minSize + 1);
        double sum = KLD.calculateKLD(probs, probRUD);

        // if sum < MiRCat2Params.UDVAL, then the distribution is alredly uniform
        if( Math.abs(sum) > MiRCat2Params.UDVAL){
            int firstPeak = findPeak(1);
            //int secondPeak = findPeak(2);
            
            if(firstPeak >= MiRCat2Params.minLen && firstPeak <= MiRCat2Params.maxLen
                   // && secondPeak >= MiRCat2Params.minLen && secondPeak <= MiRCat2Params.maxLen 
                    )
                return true;
        }
        return false;
    }
    
    private int findPeak(int rank){
        
        double max1 = Double.MIN_VALUE;
        double max2 = Double.MIN_VALUE;
        
        int maxi1 = -1;
        int maxi2 = -1;
        
        for (Integer i : probs.keySet()) {
            double prob = probs.get(i);
            if(prob >= max1){
                max2 = max1;
                max1 = prob;
                
                
                maxi2 = maxi1;
                maxi1 = i;  
            }
            else if(prob > max2){
                max2 = prob;
                maxi2 = i;  
            }
        }
        
        if(rank == 1)
            return maxi1;
        else if(rank == 2) return maxi2;
        
        return -1;
    }

    private void probabilities(HashMap<Integer, Double> sizeClassDist) {
       
       int sum = 0;
       for(Integer i: sizeClassDist.keySet()){
           sum += sizeClassDist.get(i);
       }
       
       for(Integer i: sizeClassDist.keySet()){
           probs.put(i, sizeClassDist.get(i)/(double)sum);
       }
    }
}
