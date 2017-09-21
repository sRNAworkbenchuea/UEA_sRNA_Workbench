/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;


/**
 * User defined rule set
 *
 * @author rew13hpu
 */
public class RuleSet {

    static double MAX_SCORE;
    static int MAX_ADJACENT_MM;
    static int CORE_REGION_START;
    static int CORE_REGION_END;
    static int MAX_MM;
    static int MAX_GAPS;
    static int MAX_GU_WOBBLES;
    static int MAX_ADJACENT_MM_CORE_REGION;
    static int MAX_MM_CORE_REGION;
    static double GU_MM_SCORE;
    static double MAX_TOTAL_MM;
    static boolean ALLOW_MM_10;
    static boolean ALLOW_MM_11;
    static double SCORE_MM_10;
    static double SCORE_MM_11;
    static boolean GUCountAsMM;
    static boolean GapCountAsMM;
    static boolean ALLOW_GAP_10_11;
    static double SCORE_GU_WOBBLE;
    static double SCORE_MM;
    static double CORE_REGION_MULTIPLIER;
    static double SCORE_GAP;
    static boolean[] NO_MM_ALLOWED;
    static boolean[] MM_ALLOWED;

    public static int getMAX_MM_CORE_REGION() {
        return MAX_MM_CORE_REGION;
    }

    public static int getMAX_ADJACENT_MM_CORE_REGION() {
        return MAX_ADJACENT_MM_CORE_REGION;
    }

    
    
    public static double getSCORE_GU_WOBBLE() {
        return SCORE_GU_WOBBLE;
    }

    public static double getSCORE_MM() {
        return SCORE_MM;
    }

    public static double getCORE_REGION_MULTIPLIER() {
        return CORE_REGION_MULTIPLIER;
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
    
    
    public static boolean allowedMM(int position)
    {
        return MM_ALLOWED[position];
    }
    
    public static boolean notAllowedMM(int position)
    {
        return NO_MM_ALLOWED[position];
    }

    public static boolean isGUCountAsMM() {
        return GUCountAsMM;
    }

    public static boolean isGapCountAsMM() {
        return GapCountAsMM;
    }
    
    public RuleSet() {
        RuleSet.MAX_SCORE = 5.0;
        RuleSet.MAX_ADJACENT_MM = 2;
        RuleSet.MAX_ADJACENT_MM_CORE_REGION = 0;
        RuleSet.MAX_GAPS = 1;
        RuleSet.MAX_TOTAL_MM = 4.5;
        RuleSet.MAX_GU_WOBBLES = 3;
        RuleSet.ALLOW_GAP_10_11 = false;
        RuleSet.ALLOW_MM_10 = true;
        RuleSet.ALLOW_MM_11 = true;
        RuleSet.SCORE_GAP = 1;
        RuleSet.SCORE_GU_WOBBLE = 0.5;
        RuleSet.SCORE_MM = 1;
        RuleSet.SCORE_MM_10 = 2.5;
        RuleSet.SCORE_MM_11 = 2.5;
        RuleSet.CORE_REGION_MULTIPLIER = 2;
        RuleSet.MAX_MM_CORE_REGION = 3;
        RuleSet.MAX_MM = 6;
        RuleSet.GUCountAsMM = false;
        RuleSet.GapCountAsMM = true;
        RuleSet.GU_MM_SCORE = 0.5;
        RuleSet.CORE_REGION_START = 2;
        RuleSet.CORE_REGION_END = 14;
        //BUILD THIS TABLE WITH THE USERS NOT PERMITTED MM
        NO_MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];
        
        //BUILD THIS TABLE WITH THE USERS PERMITTED MM
        MM_ALLOWED = new boolean[Engine.MAX_SRNA_SIZE];
        
        MM_ALLOWED[0] = true;
        

    }
}
