/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils.degradomestatistics;

/**
 *
 * @author rew13hpu
 */
public class Teststatistics {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BinomialDistribution b = new BinomialDistribution(19, 0.0003468458);
        System.out.println(1 - b.cumulative(0));
    }

    
}
