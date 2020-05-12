/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

public class BLASTnResults {
    //qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore

    double percentIdenticleMatches, eValue, bitscore;
    int alignmentLength, numMismatches, numGaps, queryStart, queryEnd, subjectStart, subjectEnd;

    public BLASTnResults(String blastnResultsString) {
        String splits[] = blastnResultsString.split("\t");
        percentIdenticleMatches = Double.parseDouble(splits[2]);
        alignmentLength = Integer.parseInt(splits[3]);
        numMismatches = Integer.parseInt(splits[4]);
        numGaps = Integer.parseInt(splits[5]);
        queryStart = Integer.parseInt(splits[6]);
        queryEnd = Integer.parseInt(splits[7]);
        subjectEnd = Integer.parseInt(splits[8]);
        subjectStart = Integer.parseInt(splits[9]);
        eValue = Double.parseDouble(splits[10]);
        bitscore = Double.parseDouble(splits[11]);

    }

    public double getPercentIdenticleMatches() {
        return percentIdenticleMatches;
    }

    public double geteValue() {
        return eValue;
    }

    public double getBitscore() {
        return bitscore;
    }

    public int getAlignmentLength() {
        return alignmentLength;
    }

    public int getNumMismatches() {
        return numMismatches;
    }

    public int getNumGaps() {
        return numGaps;
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

}
