/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.classes;

import java.util.HashMap;

/**
 *
 * @author keu13sgu
 */
public class KLD {
    
    public static double calculateKLD(HashMap<Integer, Double> probs, double kldProb){
        double sum = 0;       
         for (Integer i : probs.keySet()) {
            sum += Math.abs(probs.get(i) * Math.log(probs.get(i) / kldProb));
        }        
         return sum;
    }
    
}
