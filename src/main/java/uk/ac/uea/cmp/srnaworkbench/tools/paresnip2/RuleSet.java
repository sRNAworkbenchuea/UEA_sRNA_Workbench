/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.Serializable;


/**
 * BUILDS A COLLECTION OF PATHS AND THE SCORE
 *
 * @author rew13hpu
 */
public class RuleSet implements Serializable {

    static double MAX_SCORE;
    static int MAX_ADJACENT_MM;
    static int MAX_GAPS;
    static int MAX_GU_WOBBLES;
    static int MAX_ADJACENT_MM_SEED_REGION;
    static boolean ALLOW_MM_10;
    static boolean ALLOW_MM_11;
    static double SCORE_MM_10;
    static double SCORE_MM_11;
    static boolean ALLOW_GAP_10_11;
    static double SCORE_GU_WOBBLE;
    static double SCORE_MM;
    static double SEED_REGION_MULTIPLIER;
    static double SCORE_GAP;
    static int MAX_TOTAL;
    boolean[] NO_MM_ALLOWED;
    boolean[] MM_ALLOWED;
    private static final long serialVersionUID = 169;

    public static int getMAX_ADJACENT_MM_SEED_REGION() {
        return MAX_ADJACENT_MM_SEED_REGION;
    }

    
    
    public static double getSCORE_GU_WOBBLE() {
        return SCORE_GU_WOBBLE;
    }

    public static double getSCORE_MM() {
        return SCORE_MM;
    }

    public static double getSEED_REGION_MULTIPLIER() {
        return SEED_REGION_MULTIPLIER;
    }

    public static double getMAX_SCORE() {
        return MAX_SCORE;
    }

    public static int getMAX_ADJACENT_MM() {
        return MAX_ADJACENT_MM;
    }

    public static int getMAX_GAPS() {
        return MAX_GAPS;
    }

    public static int getMAX_GU_WOBBLES() {
        return MAX_GU_WOBBLES;
    }

    public static boolean isALLOW_MM_10() {
        return ALLOW_MM_10;
    }

    public static boolean isALLOW_MM_11() {
        return ALLOW_MM_11;
    }

    public static boolean isALLOW_GAP_10_11() {
        return ALLOW_GAP_10_11;
    }

    public static double getSCORE_GAP() {
        return SCORE_GAP;
    }
    
    public RuleSet tableRules()
    {
        
        
        
        return null;
    }

    
    
    public RuleSet() {
        RuleSet.MAX_SCORE = 4.5;
        RuleSet.MAX_ADJACENT_MM = 2;
        RuleSet.MAX_ADJACENT_MM_SEED_REGION = 1;
        RuleSet.MAX_GAPS = 1;
        RuleSet.MAX_GU_WOBBLES = 3;
        RuleSet.ALLOW_GAP_10_11 = true;
        RuleSet.ALLOW_MM_10 = true;
        RuleSet.ALLOW_MM_11 = true;
        RuleSet.SCORE_GAP = 1;
        RuleSet.SCORE_GU_WOBBLE = 0.5;
        RuleSet.SCORE_MM = 1;
        RuleSet.SCORE_MM_10 = 2.5;
        RuleSet.SCORE_MM_11 = 2.5;
        RuleSet.SEED_REGION_MULTIPLIER = 1;
        RuleSet.MAX_TOTAL = 7;
        //BUILD THIS TABLE WITH THE USERS NOT PERMITTED MM
        NO_MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];
        //BUILD THIS TABLE WITH THE USERS PERMITTED MM
        MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];
        MM_ALLOWED[0] = true;
        

    }

    public RuleSet(double MAX_SCORE, int MAX_ADJACENT_MM, int MAX_GAPS, int MAX_GU_WOBBLES, boolean ALLOW_MM_10, boolean ALLOW_MM_11, boolean ALLOW_GAP_10_11, double SCORE_GU_WOBBLE, double SCORE_MM, double SEED_REGION_MULTIPLIER, double SCORE_GAP) {
        RuleSet.MAX_SCORE = MAX_SCORE;
        RuleSet.MAX_ADJACENT_MM = MAX_ADJACENT_MM;
        RuleSet.MAX_GAPS = MAX_GAPS;
        RuleSet.MAX_GU_WOBBLES = MAX_GU_WOBBLES;
        RuleSet.ALLOW_MM_10 = ALLOW_MM_10;
        RuleSet.ALLOW_MM_11 = ALLOW_MM_11;
        RuleSet.ALLOW_GAP_10_11 = ALLOW_GAP_10_11;
        RuleSet.SCORE_GU_WOBBLE = SCORE_GU_WOBBLE;
        RuleSet.SCORE_MM = SCORE_MM;
        RuleSet.SEED_REGION_MULTIPLIER = SEED_REGION_MULTIPLIER;
        RuleSet.SCORE_GAP = SCORE_GAP;
        //BUILD THIS TABLE WITH THE USERS NOT PERMITTED MM
        NO_MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];
        //BUILD THIS TABLE WITH THE USERS PERMITTED MM
        MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];

    }

}
