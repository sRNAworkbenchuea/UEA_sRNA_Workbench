/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author keu13sgu
 */
public class MiRCat20 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
//            String patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\Patman\\Human1.patman";//WTA_24h.patman";
//            String genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\hg38.fa";//Data to run\\Genome\\Zv9_toplevel.fa");
//            String outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\all_hum.txt";

//            String patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\Patman\\WTA_48h.patman";
//            String genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\Zv9_toplevel.fa";
//            String outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\WTA_48h_rez.txt";
//            String miRBaseFile  =  "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\ZF_all_data_v21.txt";
//            String DEFile  = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\New Folder\\DE_1.patman";
//            String allInfo = "N";
            String patman;//= "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\TESTING\\PATMANS\\GSM10.patman";
            String genomeFile;// = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\hg38.fa";
            String outputFile;//= "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Human datasets\\A\\GSM10.patman";
            String miRBaseFile;// =  "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\hsa_all.csv";
            // String DEFile  = "none";
            // String allInfo ;//= "Y";
            String paramFile;
//            
//            String patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\TESTING\\bigFile_chr21.patman";
//            String genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\TESTING\\hg38_chr21.fa";

            //RUN in LINUX:
            //
            if (sRNAUtils.isWindows()) {
               // patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Results\\Patmans\\GSM450597_hsa_01.patman";
                // outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Human datasets\\A\\GSM_01.patman";

               // genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\hg38.fa";
                // genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\DM\\dm6.fa";               
                //patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\SRR060984.patman";
                // patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\DM\\dm_dsDcr1kdWA.patman";                
                //outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Human datasets\\A\\SRR060984_2.patman";
                // outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\DM\\dm_dsDcr1kdWA_rez.patman";
                //miRBaseFile  =  "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\hsa_all.csv";
                //paramFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\params.txt";
               
                //mm
//                genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\mm10.fa";
//                patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\mm_SRR1023690.patman";
//                outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\\\MM_rez.patman";
//                miRBaseFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\mmu.csv";
//                
//                paramFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\params.txt";
                
                 //zf
                genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Genomes\\Zv9_toplevel.fa";
                patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\ZF2\\WT1.patman";
                outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\ZF2\\MC2\\WT1_rez2.patman";
                miRBaseFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\zf.csv";
//                
                paramFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\params.txt";

                //to
//                genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Tomato\\to2.40.fa";
//                patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Tomato\\to_WT1.patman";
//                outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Tomato\\to_WT1_rez2.patman";
//                miRBaseFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\to.csv";
                
                //ath
//                genomeFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Ath\\at10.fa";
//                patman = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Ath\\Arab_WTA.patman";
//                outputFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Testing\\MiRCat2\\Ath\\Arab_WTA_rez2.patman";
//                miRBaseFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Software\\mirbase\\ath.csv";
//
//                paramFile = "C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\params_plant.txt";
            } else {
                patman = args[0];
                genomeFile = args[1];
                miRBaseFile = args[2];
                outputFile = args[3];
                paramFile = args[4];
            }

            String DEFile = "none";

            //InputOutput.readParams(paramFile);
            MiRCat2ServiceLayer mcl = new MiRCat2ServiceLayer();
            //mcl.readGenome(genomeFile);

           // InputOutput.readAllAndExecute(patman, genomeFile, miRBaseFile, DEFile, outputFile, Params.allInfo);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
        }

    }

}
