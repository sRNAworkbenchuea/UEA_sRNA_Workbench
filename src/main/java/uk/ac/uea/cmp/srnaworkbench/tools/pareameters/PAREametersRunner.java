/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.pareameters;

import java.io.File;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class PAREametersRunner {

    public static void main(String args[]) {
        Tools.getInstance();
        PAREametersInput input = PAREametersInput.getInstance();

        //File dir = new File("/scratch/Josh/PAREameters/");
        File dir = new File("D:\\PAREameters\\Arabidopsis_thaliana");
        AppUtils.INSTANCE.setCommandLine(true);

        for (File sample : dir.listFiles()) {
            if (sample.isDirectory()) {
                for (File replicate : sample.listFiles()) {
                    if (replicate.getName().equals("WTC")) {
                        input.setDegradome(new File(replicate.getAbsolutePath() + File.separator + "degradome.fasta"));
                        input.setGenomeFile(new File(dir.getAbsolutePath() + File.separator + "genome.fa"));
                        input.setHairpinSeqs(new File(dir.getAbsolutePath() + File.separator + "hairpin.fa"));
                        input.setKnownMiRGFF(new File(dir.getAbsolutePath() + File.separator + "knownMiR.gff3"));
                        input.setMatureMiRNAs(new File(dir.getAbsolutePath() + File.separator + "mature.fa"));
                        input.setUsersMiRNAs(new File(dir.getAbsolutePath() + File.separator + "mature.fa"));
                        input.setOrganismName("A_thaliana");
                        input.setOutputDir(new File(replicate.getAbsolutePath() + File.separator + "PAREameters_0_1_2_3/"));
                        input.setSmallRNAs(new File(replicate.getAbsolutePath() + File.separator + "smallRNAs.fasta"));
                        input.setTranscriptome(new File(dir.getAbsolutePath() + File.separator + "transcript.fa"));

                        PAREametersEngine engine = new PAREametersEngine();
                        engine.process();
                    }
                }
            }
        }

//        File dir = new File("D:\\PAREameters\\Arabidopsis_thaliana\\Dalmay");
//        File output = new File("D:\\PAREameters\\Arabidopsis_thaliana");
//
//        for (File subDir : dir.listFiles()) {
//          
//                File dataLocation = subDir;
//
//                input.setDegradome(new File(dataLocation.getAbsolutePath() + File.separator + "degradome.fasta"));
//                input.setGenomeFile(new File(output.getAbsolutePath() + File.separator + "genome.fa"));
//                input.setHairpinSeqs(new File(output.getAbsolutePath() + File.separator + "hairpin.fa"));
//                input.setKnownMiRGFF(new File(output.getAbsolutePath() + File.separator + "knownMiR.gff3"));
//                input.setMatureMiRNAs(new File(output.getAbsolutePath() + File.separator + "mature.fa"));
//                input.setOrganismName("Arabidopsis_thaliana");
//                input.setOutputDir(new File(dataLocation.getAbsolutePath() + File.separator + "PAREameters"));
//                input.setSmallRNAs(new File(dataLocation.getAbsolutePath() + File.separator + "smallRNAs.fasta"));
//                //input.setSpeciesMiRNAs(new File(output.getAbsolutePath() + File.separator + "species_miRNAs.fa"));
//                input.setTranscriptome(new File(output.getAbsolutePath() + File.separator + "transcript.fa"));
//
//                PAREametersEngine engine = new PAREametersEngine();
//                engine.process();
//            
//        }
        //PlotProducer p = new PlotProducer(input.getOutputDir(), new File(input.getOutputDir() + File.separator + "miRNA_alignments.csv"));
        //p.producePlots();
    }

}
