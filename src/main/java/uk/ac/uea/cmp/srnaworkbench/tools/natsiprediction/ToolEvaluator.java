/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author rew13hpu
 */
public class ToolEvaluator {
    
    static File NATpareResultsFile, toFind;
    static HashMap<String, HashSet<String>> NATpareResults;
    
    public static void main(String[] args) {
        NATpareResultsFile = new File("D:\\NAT_analysis\\performance_benchmarking\\NATpare\\predicted_NATsiRNAs.csv");
        toFind = new File("D:\\NAT_analysis\\performance_benchmarking\\toFind.csv");
        NATpareResults = new HashMap();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(NATpareResultsFile));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                
                String splits[] = line.split(",");
                
                String gene = splits[4].split("\\.")[0];
                String seq = splits[1];
                
                
                if(!NATpareResults.containsKey(gene))
                {
                    NATpareResults.put(gene, new HashSet());
                }
                
                NATpareResults.get(gene).add(seq);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        HashSet<String> notFound = new HashSet();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(toFind));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                
                String splits[] = line.split(",");
                
                String gene = splits[0];
                String seq = splits[1];
                String nat = splits[2];
                
                if(NATpareResults.containsKey(gene))
                {
                    if(!NATpareResults.get(gene).contains(seq))
                    {
                        System.out.println(seq + " not found for " + gene + " and NAT " + nat);
                    }
                }
                else
                {
                    notFound.add(gene + " not found for NAT " + nat);
                }
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        for(String s : notFound)
        {
            System.out.println(s);
        }
     
        int count = 0;
        for(String gene : NATpareResults.keySet())
        {
            count += NATpareResults.get(gene).size();
        }
        
        System.out.println("Total found: " + count);
        
    }
    
    
}
