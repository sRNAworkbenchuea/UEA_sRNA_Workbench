/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

public class RNAplexResults {

    static NATsiRNA_PredictionConfiguration config = NATsiRNA_PredictionConfiguration.getInstance();

    int queryStart, queryEnd, subjectStart, subjectEnd, largestQueryBubble,
            largestSubjectBubble;
    double energy;
    String queryHybrid, subjectHybrid;

    public RNAplexResults(String RNAplexResultsString) {
        //TODO process the String

        RNAplexResultsString = RNAplexResultsString.trim().replaceAll(" +", " ");

        String splits[] = RNAplexResultsString.split(" ");

        String hybridSplits[] = splits[0].split("&");
        
        
        subjectHybrid = hybridSplits[0];
        queryHybrid = hybridSplits[1];

        
        String subjectPositionSplit[] = splits[1].split(",");
        String queryPositionSplit[] = splits[3].split(",");
        

        queryStart = Integer.parseInt(queryPositionSplit[0]);
        queryEnd = Integer.parseInt(queryPositionSplit[1]);

        subjectStart = Integer.parseInt(subjectPositionSplit[0]);
        subjectEnd = Integer.parseInt(subjectPositionSplit[1]);

        energy = Double.parseDouble(splits[4].split("\\(")[1].split("\\)")[0]);

        largestQueryBubble = countBubbles(queryHybrid);
        largestSubjectBubble = countBubbles(subjectHybrid);

    }

    public int getQueryStart() {
        return queryStart;
    }

    public int getQueryEnd() {
        return queryEnd;
    }

    public int getSubjectStart() {
        return subjectStart;
    }

    public int getSubjectEnd() {
        return subjectEnd;
    }

    public int getLargestQueryBubble() {
        return largestQueryBubble;
    }

    public int getLargestSubjectBubble() {
        return largestSubjectBubble;
    }

    public double getEnergy() {
        return energy;
    }

    public String getQueryHybrid() {
        return queryHybrid;
    }

    public String getSubjectHybrid() {
        return subjectHybrid;
    }

    private int countBubbles(String hybrid) {
        int currentMax = Integer.MIN_VALUE;
        int temp = 0;

        for (char c : hybrid.toCharArray()) {
            if (c == '.') {
                temp++;
            } else {
                if (temp > currentMax) {
                    currentMax = temp;
                }
                temp = 0;
            }
        }

        return currentMax;

    }

    public boolean isValid(BLASTnResults blastnResults) {
        
        //check bubble sizes
        if((double)largestQueryBubble / ((double)(queryEnd-queryStart + 1)) > config.getLargestBubbleRegion())
        {
            return false;
        }
        
        if((double)largestSubjectBubble/((double)(subjectEnd-subjectStart + 1)) > config.getLargestBubbleRegion())
        {
            return false;
        }
        
        double coverageRatio = config.getCoverageRatio();

        int blastQueryStart = blastnResults.getQueryStart();
        int blastQueryEnd = blastnResults.getQueryEnd();

        int queryLength = blastQueryEnd - blastQueryStart + 1;
        
        int blastSubjectStart = blastnResults.getSubjectStart();
        int blastSubjectEnd = blastnResults.getSubjectEnd();
        
        int subjectLength = blastSubjectEnd - blastSubjectStart + 1;

        int queryCoverage = Math.min(blastQueryEnd, queryEnd) - Math.max(blastQueryStart, queryStart);
        int subjectCoverage = Math.min(blastSubjectEnd, subjectEnd) - Math.max(blastSubjectStart, subjectStart);
            
        double queryCoverageRatio = (double)queryCoverage/(double)queryLength;
        double subjectCoverageRatio = (double)subjectCoverage/(double)subjectLength;
        
        if(queryCoverageRatio >= coverageRatio && subjectCoverageRatio >= coverageRatio)
        {
            return true;
        }
            
        return false;
    }

}
