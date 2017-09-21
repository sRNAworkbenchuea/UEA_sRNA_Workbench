/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.degradomeanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.tools.shortreadaligner.*;

/**
 *
 * @author Josh
 */
public class DegradomeTagAligner extends ShortReadAligner {

    HashMap<String, String> longReadSeqs;
    HashMap<String, int[]> abundancePositions;


    public DegradomeTagAligner(int nThreads, String shortReadsFile, String longReadsFile) {
        super(nThreads, shortReadsFile, longReadsFile, 20, 21, false);

        System.out.println("Now building categories!");
        longReadSeqs = new HashMap();
        populateLongReadSequences();
        determineCategories();
        longReadSeqs.clear();
        alignedReads.clear();
        System.out.println("Shrinking");
        MessengerRNA.shrinkToFit();
        System.out.println("Finished Shrinking");

    }

    private void determineCategories() {

        abundancePositions = new HashMap();

        for (String gene : alignedReads.keySet()) {
            //do something with alignedReads.get(gene) to build the TranscriptFragments!!!!
            List<ShortReadAlignment> list = alignedReads.get(gene);
            Collections.sort(list);

            double median;
            boolean singleHighestAbundance;

            if (list.size() == 1) {
                singleHighestAbundance = true;
            } else {
                singleHighestAbundance = list.get(list.size() - 1).getTag().getAbundance()
                        > list.get(list.size() - 2).getTag().getAbundance();
            }
            int largestAbundance = list.get(list.size() - 1).getTag().getAbundance();

            if (list.size() % 2 == 0) {
                median = ((double) list.get(list.size() / 2).getTag().getAbundance()
                        + (double) list.get(list.size() / 2 - 1).getTag().getAbundance()) / 2;
            } else {
                median = (double) list.get(list.size() / 2).getTag().getAbundance();
            }

            String geneSeq = longReadSeqs.get(gene);
            abundancePositions.put(gene, new int[geneSeq.length()]);


            for (ShortReadAlignment sra : list) {

                int abundance = sra.getTag().getAbundance();

                if (abundance == largestAbundance && abundance != 1 && singleHighestAbundance) {
                    add(gene, geneSeq, 0, sra);
                } else if (abundance == largestAbundance && abundance != 1) {
                    add(gene, geneSeq, 1, sra);
                } else if (abundance > median && abundance < largestAbundance && abundance != 1) {
                    add(gene, geneSeq, 2, sra);
                } else if (abundance <= median && abundance != 1) {
                    add(gene, geneSeq, 3, sra);
                } else {
                    add(gene, geneSeq, 4, sra);
                }
            }
        }
        
        

    }

    private void add(String geneName, String geneSeq, int category, ShortReadAlignment sra) {
        MessengerRNA fragment = null;

        int transcriptCleavagePos = sra.getGenePosition();
        String fragString;

        if (transcriptCleavagePos - 16 >= 0) {
            fragString = geneSeq.substring(transcriptCleavagePos - 16, transcriptCleavagePos + 16);
            fragment = new MessengerRNA(fragString, geneName);
            fragment.setTranscriptPosition(transcriptCleavagePos);
            fragment.category = (byte) category;
            fragment.abundance = sra.getTag().getAbundance();

        } else {

            //THIS ALL NEEDS TO BE HANDLED BETER
            //the 5' end of the mRNA must be at least 10nt in length
            //i.e. can't be shorter than the 5' end until cleavage pos (10) of sRNA
//            int numMissing = Math.abs(transcriptCleavagePos - 16);
//            fragString = geneSeq.substring(transcriptCleavagePos - 16 + numMissing, transcriptCleavagePos + 16);
//            
//            if(numMissing <= 6)
//            {
//                fragment = new MessengerRNA(fragString, geneName, 16-numMissing, (byte)numMissing );
//            }
        }

        if (fragment != null) {     
                abundancePositions.get(geneName)[fragment.transcriptPosition-1] = fragment.abundance;
        }

    }


    private void populateLongReadSequences() {
        FileReader input;
        StringBuilder sb = new StringBuilder();
        String gene = null;

        try {

            input = new FileReader(longReadsFile);

            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while ((myLine = bufRead.readLine()) != null) {

                if (myLine.startsWith(">")) {
                    if (gene != null) {
                        String longRead = sb.toString();
                        longReadSeqs.put(gene, longRead);
                        sb = new StringBuilder();
                    }

                    gene = myLine.split(" ")[0].split(">")[1];
                } else {
                    sb.append(myLine.trim());
                }

            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            String longRead = sb.toString();
            longReadSeqs.put(gene, longRead);

        }
    }

}
