/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import uk.ac.uea.cmp.srnaworkbench.utils.ShortReadStats;

/**
 *
 * @author ezb11yfu
 */
public class MirBaseAnalyser {

    private static final File mb_dir = new File("E:\\UEA\\CMPBIOINF\\sRNA\\mirbase\\20110426");
    //"D:\\Research\\miRBase\\20110426\\hairpin.fa"

    private static void outputFamily(Map<String, MirBaseEntry> map, String filter) {
        MirBaseEntry mbe = map.get(filter);

        int i = 0;

        for (Map.Entry<String, String> me : mbe.getMirnaSeqs().entrySet()) {
            System.out.println(">" + me.getKey());
            System.out.println(me.getValue());
        }
    }

    private static void outputMatureGroup(File out_file, String filter) throws Exception {
        MirBaseRunner mb = new MirBaseRunner("filter_mature", new String[]{"--out_file", out_file.getPath()}, filter);
        mb.run();
    }

    private static void outputHairpinGroup(File out_file, String filter) throws Exception {
        MirBaseRunner mb = new MirBaseRunner("filter_hairpin", new String[]{"--out_file", out_file.getPath()}, filter);
        mb.run();
    }

    private static void printStats() throws Exception {
        MirBaseRunner mb = new MirBaseRunner("build_mirna_groups", new String[]{"--group_species", "--group_variants"}, null);
        mb.run();

        Map<String, MirBaseEntry> mirna_seq_map = mb.getGrouping();

        HashMap<Integer, Integer> mirna_size_distribution = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> mirna_star_size_distribution = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> hairpin_size_distribution = new HashMap<Integer, Integer>();
        int mir_and_star = 0;

        for (String key : new TreeSet<String>(mirna_seq_map.keySet())) {
            int mirna_sz = mirna_seq_map.get(key).getMirnaSeqs().size();
            Integer m_sz_count = mirna_size_distribution.get(mirna_sz);
            mirna_size_distribution.put(mirna_sz, m_sz_count == null ? 1 : m_sz_count + 1);

            int mirna_star_sz = mirna_seq_map.get(key).getMirnaStarSeqs().size();
            Integer ms_sz_count = mirna_star_size_distribution.get(mirna_star_sz);
            mirna_star_size_distribution.put(mirna_star_sz, ms_sz_count == null ? 1 : ms_sz_count + 1);

            int hairpin_sz = mirna_seq_map.get(key).getHairpinSeqs().size();
            Integer h_sz_count = hairpin_size_distribution.get(hairpin_sz);
            hairpin_size_distribution.put(hairpin_sz, h_sz_count == null ? 1 : h_sz_count + 1);

            if (mirna_sz > 0 && mirna_star_sz > 0) {
                mir_and_star++;
            }

//                System.out.print(key + ": ");
//                System.out.print(mirna_sz + ",");
//                System.out.print(mirna_star_sz + ",");
//                System.out.print(hairpin_sz + ";\n");
        }

        System.out.println("mirna size distribution");
        ShortReadStats.printLengthDistribution(mirna_size_distribution);
        System.out.println("\nmirna star size distribution");
        ShortReadStats.printLengthDistribution(mirna_star_size_distribution);
        System.out.println("\nhairpin size distribution");
        ShortReadStats.printLengthDistribution(hairpin_size_distribution);
        System.out.println("\nFound " + mir_and_star + " groups with both miRNA and miRNA* present.");
    }

    private static void printMirnaFamily(String family) throws Exception {
        MirBaseRunner mb = new MirBaseRunner("build_mirna_groups", new String[]{"--group_species", "--group_variants"}, null);
        mb.run();

        Map<String, MirBaseEntry> mirna_seq_map = mb.getGrouping();

        outputFamily(mirna_seq_map, family);
    }

    public static void main(String[] args) {
        try {
            //outputMirnaFamily("mir-190");

            //outputStats();

            //outputMatureGroup(new File("D:\\Research\\miRBase\\20110426\\mature_metazoa.fa"), "Metazoa");
            outputHairpinGroup(new File("E:\\UEA\\CMPBIOINF\\sRNA\\mirbase\\20110426\\hairpin_metazoa.fa"), "Metazoa");
            //E:\UEA\CMPBIOINF\sRNA\mirbase\20110426

//            printOrgTypeCount(mb, "Magnoliophyta");
//            printOrgTypeCount(mb, "Brassicaceae");
//            printOrgTypeCount(mb, "Caricaceae");
//            printOrgTypeCount(mb, "Fabaceae");
//            printOrgTypeCount(mb, "Amphibia");
//            printOrgTypeCount(mb, "Mammalia");
//            printOrgTypeCount(mb, "Primates");
//            
//            printOrgTypeLengthDistribution(mb, "Metazoa");
//            printOrgTypeLengthDistribution(mb, "Mammalia");
//            printOrgTypeLengthDistribution(mb, "Primates");
//            printOrgTypeLengthDistribution(mb, "Viridiplantae");
//            printOrgTypeLengthDistribution(mb, "Magnoliophyta");
//            printOrgTypeLengthDistribution(mb, "eudicotyledons");
//            printOrgTypeLengthDistribution(mb, "Brassicaceae");
//            printOrgTypeLengthDistribution(mb, "Fabaceae");
//            printOrgTypeLengthDistribution(mb, "Malvaceae");
//            
//            printOrgTypeNucleotideDistribution(mb, null);
//            printOrgTypeNucleotideDistribution(mb, "Metazoa");
//            printOrgTypeNucleotideDistribution(mb, "Viridiplantae");

//            for(String id : new TreeSet<String>(mb.getByCode().keySet()))
//            {
//                System.out.println(mb.getByCode().get(id).toString());
//            }


//            MirBaseAnalyser hairpin = new MirBaseAnalyser(new File("D:\\Research\\miRBase\\20110426\\hairpin.fa"));
//            hairpin.buildStats();
//            System.out.println("pre-miRNA count in miRBase: " + hairpin.getCount());
//            
//            Map<Integer,Integer> hairpin_ld = hairpin.getLengthDistribution();
//            
//            System.out.println("pre-miRNA Length Distributions:");
//            for(Integer len : new TreeSet<Integer>(hairpin_ld.keySet()))
//            {
//                System.out.println(len + " : " + hairpin_ld.get(len));
//            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
