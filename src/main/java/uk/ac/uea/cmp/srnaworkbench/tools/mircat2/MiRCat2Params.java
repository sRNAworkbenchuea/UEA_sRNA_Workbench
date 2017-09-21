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
public class MiRCat2Params {
    public static int WINDOW = 300;
    public static int SUBWINDOW = 20;
    public static int WINDOWOVERLAP = 100;
    public static int DEPTH = 4;   
    public static int REPEATS = 25;    
    public static double UDVAL = 1.23;
    public static double offset = 1;
    public static double offsetLow = offset - (offset * 0.1);
    public static double complex = 0.90;
    public static int range = 6; //4 for animals, 6 for plants
    
    

    
    //part 2
    public static boolean allowComplexLoop = false;
    public static int noLoops = 1;
    public static double fuzzy = 0.9;
    public static int threePrimeOverhang = 2;
    
    public static int cluster = 3;
    public static int foldDist = 250; //75 animals, 250 plants
    public static int lFoldL = foldDist;
    
    public static double pVal = 0.05;
    public static boolean execRANDFold = false;
    
    public static int minLen = 20;
    public static int maxLen = 24;
    
    public static double minOrientation = 0.8;
   // public static double minCG = 20;
    
    public static int clearCut = 3;
    public static double clearCutPercent = 0.95;
    public static double underClearCut = 0.70;
    public static int inLoop = clearCut;
    
    public static int minFoldLen = 40; 
    public static int maxAmfe = -22;
    public static int minLoop = 3;
    
    public static int gapsInMirna = 3;
    public static double minParedPerc = 0.5;
    
    public static int minParedNucl = 15;

    public static double overlapAdiacentCluster = 0.05;
    
    public static int minSize = 16;
    public static int maxSize = 35;
    
    public static boolean miRStarPresent = false;
    
}
