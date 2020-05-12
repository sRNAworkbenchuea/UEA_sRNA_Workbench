/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

/**
 *
 * @author rew13hpu
 */
public class NATPair {

    String queryName, subjectName, querySeq, subjectSeq;

    String queryDistributionType, subjectDistributionType;
    
    String natType;

    double mfeRatio, overlappingDensitity, overalDensitiy,
            queryOverlappingAlignmentRatio, subjectOverlappingAlignmentRatio;

    int queryTotalAlignments, subjectTotalAlignments, queryOverlapAlignments,
            subjectOverlapAlignments, queryTotalAlignmentsNR, subjectTotalAlignmentsNR, queryOverlapAlignmentsNR,
            subjectOverlapAlignmentsNR;

    RNAplexResults plexResults;
    BLASTnResults blastnResults;

    NATPair(String geneA, String geneA_seq, String geneB, String geneB_seq) {
        queryName = geneA;
        querySeq = geneA_seq;
        subjectName = geneB;
        subjectSeq = geneB_seq;
    }

    public String getQueryDistributionType() {
        return queryDistributionType;
    }

    public void setQueryDistributionType(String queryDistributionType) {
        this.queryDistributionType = queryDistributionType;
    }

    public String getSubjectDistributionType() {
        return subjectDistributionType;
    }

    public void setSubjectDistributionType(String subjectDistributionType) {
        this.subjectDistributionType = subjectDistributionType;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getQuerySeq() {
        return querySeq;
    }

    public String getSubjectSeq() {
        return subjectSeq;
    }

    void setMFERatio(double ratio) {
        if (ratio > 1) {
            ratio = 1;
        }
        this.mfeRatio = ratio;
    }

    public double getFreeEnergy() {
        return plexResults.getEnergy();
    }

    public double getMFERatio() {
        return mfeRatio;
    }

    void setBLASTnResults(BLASTnResults blastnResults) {
        this.blastnResults = blastnResults;
    }

    void setRNAplexResults(RNAplexResults plexResults) {
        this.plexResults = plexResults;
    }

    public double getMfeRatio() {
        return mfeRatio;
    }

    public RNAplexResults getPlexResults() {
        return plexResults;
    }

    public BLASTnResults getBlastnResults() {
        return blastnResults;
    }

    public int getQueryStart() {
        return blastnResults.getQueryStart();
    }

    public int getQueryEnd() {
        return blastnResults.getQueryEnd();
    }

    public int getSubjectStart() {
        return blastnResults.getSubjectStart();
    }

    public int getSubjectEnd() {
        return blastnResults.getSubjectEnd();
    }

    public double getOverlappingDensitity() {
        return overlappingDensitity;
    }

    public void setOverlappingDensitity(double overlappingDensitity) {
        this.overlappingDensitity = overlappingDensitity;
    }

    public double getOveralDensitiy() {
        return overalDensitiy;
    }

    public void setOveralDensitiy(double overalDensitiy) {
        this.overalDensitiy = overalDensitiy;
    }

    int getOverlappingLength() {
        return blastnResults.getAlignmentLength();
    }

//    public String getCisOrTrans() {
//        //calc if cis or trans
//        //http://repository.essex.ac.uk/14289/1/DNA%20Res-2015-Yuan-233-43.pdf
//        
//        //If a GFF3 file is provided, this should be done based on that
//
//        if (blastnResults.getPercentIdenticleMatches() == 100.0) {
//            return "cis";
//        }
//
//        return "trans";
//
//    }
    
    public void setType(String type)
    {
        this.natType = type;
    }
    
      public String getType()
    {
        return natType;
    }
    
    

    public double getQueryOverlappingAlignmentRatio() {
        return (double) queryOverlapAlignments / (double) queryTotalAlignments;
    }

    public double getSubjectOverlappingAlignmentRatio() {
        return (double) subjectOverlapAlignments / (double) subjectTotalAlignments;
    }

    public double getQueryAlignmentRatio() {
        return (double) queryTotalAlignments / (double) querySeq.length();
    }

    public double getSubjectAlignmentRatio() {
        return (double) subjectTotalAlignments / (double) subjectSeq.length();
    }

    public int getQueryTotalAlignments() {
        return queryTotalAlignments;
    }

    public void setQueryTotalAlignments(int queryTotalAlignments) {
        this.queryTotalAlignments = queryTotalAlignments;
    }

    public int getSubjectTotalAlignments() {
        return subjectTotalAlignments;
    }

    public void setSubjectTotalAlignments(int subjectTotalAlignments) {
        this.subjectTotalAlignments = subjectTotalAlignments;
    }

    public int getQueryOverlapAlignments() {
        return queryOverlapAlignments;
    }

    public void setQueryOverlapAlignments(int queryOverlapAlignments) {
        this.queryOverlapAlignments = queryOverlapAlignments;
    }

    public int getSubjectOverlapAlignments() {
        return subjectOverlapAlignments;
    }

    public void setSubjectOverlapAlignments(int subjectOverlapAlignments) {
        this.subjectOverlapAlignments = subjectOverlapAlignments;
    }

    public int getQueryTotalAlignmentsNR() {
        return queryTotalAlignmentsNR;
    }

    public void setQueryTotalAlignmentsNR(int queryTotalAlignmentsNR) {
        this.queryTotalAlignmentsNR = queryTotalAlignmentsNR;
    }

    public int getSubjectTotalAlignmentsNR() {
        return subjectTotalAlignmentsNR;
    }

    public void setSubjectTotalAlignmentsNR(int subjectTotalAlignmentsNR) {
        this.subjectTotalAlignmentsNR = subjectTotalAlignmentsNR;
    }

    public int getQueryOverlapAlignmentsNR() {
        return queryOverlapAlignmentsNR;
    }

    public void setQueryOverlapAlignmentsNR(int queryOverlapAlignmentsNR) {
        this.queryOverlapAlignmentsNR = queryOverlapAlignmentsNR;
    }

    public int getSubjectOverlapAlignmentsNR() {
        return subjectOverlapAlignmentsNR;
    }

    public void setSubjectOverlapAlignmentsNR(int subjectOverlapAlignmentsNR) {
        this.subjectOverlapAlignmentsNR = subjectOverlapAlignmentsNR;
    }
    
    

    public String getCoverageType() {

        double length = (double) blastnResults.getAlignmentLength();
        double queryLength = (double) querySeq.length();
        double subjectLength = (double) subjectSeq.length();

        if ((length > queryLength / 2.0) || (length > subjectLength / 2.0)) {
            return "HC";
        } else if (length >= 100) {
            return "100nt";
        }

        return "LC";
    }

}
