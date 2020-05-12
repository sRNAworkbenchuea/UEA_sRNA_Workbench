/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.natsiprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import uk.ac.uea.cmp.srnaworkbench.tools.binarysearchaligner.BinarySearchAligner;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * This class will handle the running of the NATsiRNA prediction tool. However,
 * for now, it'll be used just for testing purposes
 *
 * @author rew13hpu
 */
public class NATsiRNA_PredictionRunner {

    private static NATsiRNA_PredictionConfiguration config = NATsiRNA_PredictionConfiguration.getInstance();
    private static NATsiRNA_PredictionInput input = NATsiRNA_PredictionInput.getInstance();

    public static void main(String[] args) {

        Tools.getInstance();
        
        
        File smallRNAs = new File("D:\\NATpare_project_organised\\Results_section_2\\salt_stress_single_NAT_pair\\gff_test\\seqs.fa");

        File outputDir = new File("D:\\NATpare_project_organised\\Results_section_2\\salt_stress_single_NAT_pair\\gff_test\\output");
        outputDir.mkdir();

        config.reset();
        input.reset();
//
        config = NATsiRNA_PredictionConfiguration.getInstance();
        input = NATsiRNA_PredictionInput.getInstance();
        input.setGFF_file(new File("D:\\NATpare_project_organised\\Results_section_2\\salt_stress_single_NAT_pair\\gff_test\\TAIR10_GFF3_genes.gff"));
        input.setGenomeFile(new File("D:\\NATpare_project_organised\\Results_section_2\\salt_stress_single_NAT_pair\\gff_test\\TAIR10_chr_all.fa"));
        //input.setTranscriptFile(fullLengthTranscriptFile);
// input.setDegradomeFile(degradome);
        input.setSmallRNAFile(smallRNAs);
        input.setOutputDir(outputDir);

        NATsiRNA_PredictionEngine engine = new NATsiRNA_PredictionEngine();

        config.setLowComplexityFilter(false);
        config.setCisOnly(true);
        config.setMinPhases(1);
        engine.process();
        

//        File dir = new File("D:\\rocky\\cutadapt");
//
//        for (File treatment : dir.listFiles()) {
//            if (treatment.isDirectory()) {
//                for (File replicate : treatment.listFiles()) {
//
//                    if (replicate.getName().contains("trimmed")) {
//                        File smallRNAs = replicate;
//                        File fullLengthTranscriptFile = new File("D:\\NATpare_project_organised\\Results_section_2\\transcript.fasta");
//                        File transcriptFile = fullLengthTranscriptFile;
//
//                        File outputDir = new File(treatment.getAbsolutePath() + File.separator + replicate.getName().split("\\.")[0] + "_output");
//                        outputDir.mkdir();
//
//                        config.reset();
//                        input.reset();
////
//                        config = NATsiRNA_PredictionConfiguration.getInstance();
//                        input = NATsiRNA_PredictionInput.getInstance();
////
//                        input.setFullLengthTranscriptFile(fullLengthTranscriptFile);
//                        input.setTranscriptCDNA(transcriptFile);
////                    //  input.setDegradomeFile(degradome);
//                        input.setSmallRNAFile(smallRNAs);
//                        input.setOutputDir(outputDir);
//
//                        NATsiRNA_PredictionEngine engine = new NATsiRNA_PredictionEngine();
//
//                        config.setLowComplexityFilter(false);
//                        config.setCisOnly(false);
//                        config.setMinPhases(1);
//                        engine.process();
//                    }
//                }
//            }
//        }

        //Next run it cis only and then cis only with min phases of 4?
//        File dir = new File("D:\\NATpare_project_organised\\Results_section_3\\firepat_data");
//
//        for (File tissue : dir.listFiles()) {
//            for (File temperature : tissue.listFiles()) {
//                for (File replicate : temperature.listFiles()) {
//                    File[] data = replicate.listFiles();
//                    File smallRNAs = data[1];
//                    File fullLengthTranscriptFile = new File("D:\\PAREameters\\Arabidopsis_thaliana\\transcript_trimmed.fa");
//                    //File fullLengthTranscriptFile = new File("D:\\NAT_analysis\\data\\Ath\\smallTranscript.fa");
//                    File transcriptFile = fullLengthTranscriptFile;
//                    // File degradome = new File("D:\\PAREameters\\Arabidopsis_thaliana\\Dalmay\\WTA\\degradome.fasta");
//                    //File smallRNAs = new File("D:\\NAT_analysis\\data\\Ath\\Dalmay_data\\WTA\\smallRNAs.fasta");
//                    //File degradome = new File("D:\\NAT_analysis\\data\\Ath\\Dalmay_data\\WTA\\degradome.fasta");
//
//                    File outputDir = replicate;
//                    outputDir.mkdirs();
//
//                    config.reset();
//                    input.reset();
//
//                    config = NATsiRNA_PredictionConfiguration.getInstance();
//                    input = NATsiRNA_PredictionInput.getInstance();
//
//                    input.setFullLengthTranscriptFile(fullLengthTranscriptFile);
//                    input.setTranscriptCDNA(transcriptFile);
//                    //  input.setDegradomeFile(degradome);
//                    input.setSmallRNAFile(smallRNAs);
//                    input.setOutputDir(outputDir);
//
//                    NATsiRNA_PredictionEngine engine = new NATsiRNA_PredictionEngine();
//
//                    config.setLowComplexityFilter(false);
//                    config.setCisOnly(true);
//                    config.setMinPhases(1);
//                    engine.process();
//                }
//            }
//        }
    }

}
