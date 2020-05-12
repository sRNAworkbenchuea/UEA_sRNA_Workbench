/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone;

import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.Patman;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.sRNA;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.standalone.model.GenomeSequence;

/**
 *
 * @author keu13sgu
 */
public class InputOutput {

    private static HashMap<String, Patman> miRBase = new HashMap<String, Patman>();
    private static HashMap<String, Patman> miRDE = new HashMap<String, Patman>();
    private static MiRCat2SceneController controller;

    public static void setController(MiRCat2SceneController controller){
        InputOutput.controller = controller;
    }
    public static void readAllAndExecute(String patman, String genomeFile, String miRBaseFile, String DEFile,
            String outputFile, boolean allInfo) throws IOException {

        if (!miRBaseFile.equals("none")) {
            readMiRBaseFile(miRBaseFile);
        }

        if (!DEFile.equals("none")) {
            readMiRDEFile(DEFile);
        }

        int min1 = Integer.MAX_VALUE;
        int lastmin = Integer.MAX_VALUE;

        String sep = "";
        if (sRNAUtils.isWindows()) {
            sep = "\\";
        } else if (sRNAUtils.isUnix() || sRNAUtils.isMac()) {
            sep = "/";
        } else if(sRNAUtils.isMac())
        {
            sep = "/";
        }

//        String header = "Score, Chromosome, Sequence, Abundance, Start, End, Strand, "
//                + "Mismatches, Hairpin Info, p-Value, Star sequence, Star abundance, Star start,"
//                + " Star end, miRBase precursor";
//        Header and columns should be changed to match database version
        String header = "Precursor Score,Chromosome,Sequence,Abundance,Start,"
                + " End,Strand,Mismatches,Hairpin Sequence,"
                + "Hairpin Start,Hairpin End,HairpinDot-Bracket,Hairpin MFE,Hairpin aMFE,p-Value,"
                + "Star Sequence,Star Abundance,Star Start,Star End,miRBasePrecursor";

        //int lastDot = Math.max(outputFile.lastIndexOf("."), outputFile.length());
        String path = outputFile.substring(0, outputFile.lastIndexOf(sep) + 1);
        //String name = outputFile.substring(outputFile.lastIndexOf(sep) + 1, lastDot);
        String name = outputFile.substring(outputFile.lastIndexOf(sep) + 1, outputFile.lastIndexOf("."));
        //String ext = outputFile.substring( lastDot );

        

        String inMiRBase = path + name + "_output.csv";
        String folds = path + name + "_output_folds.txt";
        String patmanOut = path + name + "_output.patman";
        if (MiRCat2Main.verbose) {
            System.out.println("Output Files:");
            System.out.println(inMiRBase);
            System.out.println(folds);
            System.out.println(patmanOut);
        }

        BufferedWriter outCSV = new BufferedWriter(new FileWriter(inMiRBase));
        BufferedWriter outFolds = new BufferedWriter(new FileWriter(folds));
        BufferedWriter out = new BufferedWriter(new FileWriter(patmanOut));

        outCSV.append(header);
        outCSV.append("\n");

        HashMap<String, Integer> redundant = getRedundancy(patman);

        File file = new File(genomeFile);

        ArrayList<Integer> totNums = new ArrayList<>();
        totNums.add(0);
        totNums.add(0);
        totNums.add(0);

        BufferedReader in = new BufferedReader(new FileReader(file));
        BufferedReader in2 = new BufferedReader(new FileReader(patman));

        Patman srnas;
        String line;
        String id = in.readLine().substring(1);
        String lastId = id;
        StringBuilder sequence = new StringBuilder();

        sRNA temp = null;

        while ((line = in.readLine()) != null) {

            if (line.contains(">")) {
                id = line.substring(1);

                min1 = lastmin;
            } else if (id.equals(lastId)) {
                sequence.append(line);
            } else {
                GenomeSequence genSeq = new GenomeSequence(lastId, sequence, 0, sequence.length());
                // genSeqs.add(genSeq);
                if (MiRCat2Main.verbose) {
                System.out.println("Processing: " + lastId);
                }

                sequence = new StringBuilder();
                sequence.append(line);

                srnas = new Patman();

                if (temp != null) {
                    if (temp.getId().equals(lastId)) {
                        srnas.add(temp);
                        //temp = null;
                    } else if (temp.getId().compareTo(lastId) != 0) {
                        lastId = id;
                        continue;
                    }

                }

                String line2;
                int no = 0;
                while ((line2 = in2.readLine()) != null && !line2.isEmpty()) {

                    //split the fields
                    sRNA srna = sRNA.readFromPatman(line2);
                    if (srna == null || !srna.isComplex(Params.complex)) {
                        continue;
                    }
                    if (srna.getLength() < Params.minSize) {
                        //                  under16 ++;
                        continue;
                    }
                    no++;

                    if (!srna.getId().equals(lastId)) {

                        lastmin = srna.getAb();
                        temp = srna;

                        break;
                    }

                    if (min1 == Integer.MAX_VALUE) {
                        min1 = srna.getAb();
                    }
                    if (min1 > srna.getAb()) {
                        min1 = srna.getAb();
                    }

                    srnas.add(srna);
                }

                if (srnas.isEmpty()) {
                    continue;
                }

                RemoveNoiseManager rnm = new RemoveNoiseManager(srnas, genSeq, min1);
                Patman removeNoise = rnm.removeNoise();

                if (removeNoise == null) {
                    continue;
                }

                clearRedundant(removeNoise, redundant);

                if (removeNoise != null && !removeNoise.isEmpty()) {
                    AttributesExtracter ae = new AttributesExtracter(removeNoise, rnm.getFinalSRNAs());
                    ae.processSequences(genSeq);
                    Patman miRBaseP = null;

                    if (miRBase.containsKey(lastId)) {
                        miRBaseP = miRBase.get(lastId);
                    } else {
                        miRBaseP = null;
                    }
                    ae.printResultsToFiles(out, outCSV, outFolds, miRBaseP);
//                         for(int i = 0; i < totals.size(); i++){
//                             totNums.set(i, totNums.get(i) + totals.get(i));
//                         }
//                         System.out.println("AE done");
//                         System.out.println("");
                }
                lastId = id;
            }
        }
        //  if(false){
        GenomeSequence genSeq = new GenomeSequence(lastId, sequence, 0, sequence.length());
//        genSeqs.add(genSeq);
        //System.out.println("Last id: " + lastId);
        sequence = new StringBuilder();
        sequence.append(line);
        srnas = new Patman();

        if (temp != null && temp.getId().equals(lastId)) {
            srnas.add(temp);
        }

        String line2;
        while ((line2 = in2.readLine()) != null) {

            sRNA srna = sRNA.readFromPatman(line2);
            if (srna == null || !srna.isComplex(Params.complex)) {
                continue;
            }
            if (srna.getLength() < Params.minSize) { //                 under16 ++;
                continue;
            }
            if (min1 == Integer.MAX_VALUE) {
                min1 = srna.getAb();
            }
            if (min1 > srna.getAb()) {
                min1 = srna.getAb();
            }

            srnas.add(srna);
        }
        //System.out.println("sRNAs read: " + srnas.size());
        // System.gc();
        if (!srnas.isEmpty()) {

            RemoveNoiseManager rnm = new RemoveNoiseManager(srnas, genSeq, min1);
            Patman removeNoise = rnm.removeNoise();
            clearRedundant(removeNoise, redundant);

            if (removeNoise != null && !removeNoise.isEmpty()) {
                AttributesExtracter ae = new AttributesExtracter(removeNoise, rnm.getFinalSRNAs());
                ae.processSequences(genSeq);
                Patman miRBaseP = null;
                if (miRBase.containsKey(lastId)) {
                    miRBaseP = miRBase.get(lastId);
                } else {
                    miRBaseP = null;
                }
                ae.printResultsToFiles(out, outCSV, outFolds, miRBaseP);
            }
        }
        in.close();
        in2.close();

        out.close();
        outCSV.close();
        outFolds.close();
        //System.out.println("Under 16: " + under16 );
//        System.out.println( "MirBase: " + totNums.get(1) + " Total: " + totNums.get(0) );//+ " DE: " + totNums.get(2) + "\n");
//        System.out.println("");
        System.out.flush();
    }

    private static HashMap<String, Integer> getRedundancy(String file) throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(new File(file)));
        HashMap<String, Integer> redundancy = new HashMap<>();

        String line;
        while ((line = in.readLine()) != null) {

            //split the fields
            sRNA s = sRNA.readFromPatman(line);

            if (!redundancy.containsKey(s.getSequence())) {
                redundancy.put(s.getSequence(), 0);
            }
            redundancy.put(s.getSequence(), redundancy.get(s.getSequence()) + 1);
        }
        // System.out.println("Redundancy map created");
        in.close();

        // System.out.println(redundancy.get("CAGGCTGGAGTGCAGTGGCAC"));
        return redundancy;
    }

    private static void clearRedundant(Patman removeNoise, HashMap<String, Integer> redundant) {

        for (int i = removeNoise.size() - 1; i >= 0 ; i--) {
            sRNA s = removeNoise.get(i);
            if (redundant.containsKey(s.getSequence())) {
                if (redundant.get(s.getSequence()) >= Params.REPEATS) {
                    removeNoise.remove(i);
                    //i--;
                }
            }

        }

    }

    private static void printAligned(BufferedWriter out, File file,
            Patman sRNAsOnGenSeq) throws IOException {
        for (sRNA s : sRNAsOnGenSeq) {
            out.append(s.toStringPatman() + "\n");
        }
        out.flush();

    }

    private static void keepOnlyOneWhenOverlapping() {

        for (String s : miRDE.keySet()) {
            Patman p = miRDE.get(s);
            ArrayList<Patman> cl = sRNAUtils.makeClusters(p);

            Patman kept = new Patman();

            for (Patman pp : cl) {
                kept.add(sRNAUtils.bestSRNA(pp));
            }
            miRDE.put(s, kept);
        }

    }

    private static void readMiRBaseFile(String mirBase) {
        try {
            int total = 0;
            File mirBaseFile = new File(mirBase);
            BufferedReader in = new BufferedReader(new FileReader(mirBaseFile));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                if (line.contains("#")) {
                    continue;
                }

                String[] parts = line.split(",|\t");
                if (!parts[2].equals("miRNA_primary_transcript")) {
                    continue;
                }

                total++;
                String ch = parts[0].replace("chr", "Chr");
                int b = Integer.parseInt(parts[3]);
                int e = Integer.parseInt(parts[4]);

                String name = parts[8].split(";")[2].split("=")[1];
                sRNA s = new sRNA("", b, e, 1);
                s.setId(ch);
                s.setSequence(name);

                if (!miRBase.containsKey(ch)) {
                    miRBase.put(ch, new Patman());
                }
                miRBase.get(ch).add(s);
            }
            in.close();
            System.out.println("In MirBase: " + total);
        } catch (FileNotFoundException ex) {
            System.out.println("01");
        } catch (IOException ex) {
            System.out.println("02");
        }
    }

    private static void readMiRDEFile(String mirBase) throws IOException {
        BufferedReader in = null;
        in = new BufferedReader(new FileReader(new File(mirBase)));
        String line;

        while ((line = in.readLine()) != null) {
            if (!line.isEmpty()) {
                sRNA s = sRNA.readFromPatman(line);
                if (!miRDE.containsKey(s.getId())) {
                    miRDE.put(s.getId(), new Patman());
                }
                miRDE.get(s.getId()).add(s);

            }
        }
        in.close();
        keepOnlyOneWhenOverlapping();

        HashMap<String, Integer> map = new HashMap<>();

        for (String id : miRDE.keySet()) {
            for (sRNA s : miRDE.get(id)) {
                if (!map.containsKey(s.getSequence())) {
                    map.put(s.getSequence(), 0);
                }
                map.put(s.getSequence(), map.get(s.getSequence()) + 1);
            }
        }

        for (String id : miRDE.keySet()) {
            for (int i = 0; i < miRDE.get(id).size(); i++) {
                sRNA s = miRDE.get(id).get(i);
                if (map.get(s.getSequence()) > 40) {
                    miRDE.get(id).remove(s);
                    i--;
                }
            }

        }

        BufferedWriter outRez = new BufferedWriter(new FileWriter("C:\\Users\\keu13sgu\\Local Disk\\My work\\Files\\Removed\\Patman\\DE_1_40.patman"));
        for (String id : miRDE.keySet()) {
            for (int i = 0; i < miRDE.get(id).size(); i++) {
                sRNA s = miRDE.get(id).get(i);
                outRez.append(s.toStringPatman() + "\n");
            }

        }
        outRez.flush();
        outRez.close();

        in.close();
    }

    static void readParams(String paramFile) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(paramFile)));
            String line;

            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String parts[] = line.split("=");
                switch (parts[0].trim().toLowerCase()) {
                    case "repeats":
                        Params.REPEATS = Integer.parseInt(parts[1].trim());
                        break;

                    case "complex":
                        Params.complex = Double.parseDouble(parts[1].trim());
                        break;

                    case "pval":
                        Params.pVal = Double.parseDouble(parts[1].trim());
                        break;

                    case "minlen":
                        Params.minLen = Integer.parseInt(parts[1].trim());
                        break;

                    case "maxlen":
                        Params.maxLen = Integer.parseInt(parts[1].trim());
                        break;

                    case "minfoldlen":
                        Params.minFoldLen = Integer.parseInt(parts[1].trim());
                        break;

                    case "maxamfe":
                        Params.maxAmfe = Integer.parseInt(parts[1].trim());
                        break;

                    case "gapsinmirna":
                        Params.gapsInMirna = Integer.parseInt(parts[1].trim());
                        break;

                    case "mirstarpresent":
                        Params.miRStarPresent = (parts[1].trim().equals("true"));
                        break;

                    case "maxfoldlen":
                        Params.foldDist = Integer.parseInt(parts[1].trim());
                        Params.lFoldL = Params.foldDist;
                        break;

                    case "allowcomplexloop":
                        Params.allowComplexLoop = (parts[1].trim().equals("true"));
                        break;

                    case "noloops":
                        Params.noLoops = Integer.parseInt(parts[1].trim());
                        break;

                    case "rudval":
                        Params.UDVAL = Double.parseDouble(parts[1].trim());
                        break;

                    case "clearcutpercent":
                        Params.clearCutPercent = Double.parseDouble(parts[1].trim());
                        break;

                    case "randfold":
                        Params.execRANDFold = (parts[1].trim().equals("true"));
                        break;

                    default:
                        System.out.println("Param " + parts[0] + " is not a valid parameter");
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputOutput.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputOutput.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
