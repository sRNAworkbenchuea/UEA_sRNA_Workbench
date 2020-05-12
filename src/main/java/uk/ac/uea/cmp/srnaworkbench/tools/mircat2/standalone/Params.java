/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

/**
 *
 * @author keu13sgu
 */
public class Params {
    public static int WINDOW = 300;
    public static int SUBWINDOW = 20;
    public static int WINDOWOVERLAP = 100;
    public static int DEPTH = 4;   
    public static int REPEATS = 25;    
    public static double UDVAL = 1.23;
    //public static double localUDVAL = UDVAL - (UDVAL * 0.2);
    public static double offset = 1;
    public static final double offsetLow = offset - (offset * 0.1);
    public static double complex = 0.90;
    public static boolean allInfo = true;
    public static int range = 6; //4 for animals, 5 for plants
    
    

    
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
    //public static double minCG = 20;
    
    public static int clearCut = 3;
    public static double clearCutPercent = 0.95;
    public static double underClearCut = 0.70;
    
    public static int outsideHP = 3;
    public static int inLoopSmall = clearCut;
    //public static int inLoopBig = 5;
    
    public static int minFoldLen = 40; 
    public static int maxAmfe = -22;
    public static int minLoop = 3;
    
    public static int gapsInMirna = 3;
    public static double minParedPerc = 0.5;
    
    public static int minParedNucl = 15;
       
    //public static int maxDistBetweenClusters = 5;
    //public static int maxDistBetweenMirs = 200; // 40 for animals, 200 for animals
    public static double overlapAdiacentCluster = 0.05;
    //public static double overlapAdiacentClusterMir = 0.2;
    
    public static int minSize = 16;
    public static int maxSize = 35;
    
    public static boolean miRStarPresent = false;
    
}
