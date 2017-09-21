package uk.ac.uea.cmp.srnaworkbench.tools.lfold;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;

public class LfoldThread extends Thread {

    public GenomeManager genome;
    public String chrID;
    public int sIndex;
    public int eIndex;
    public String strand;
    public int flankRegion;

    boolean complete;
    private List<Prediction_Entity> predictions;

    public LfoldThread(GenomeManager genome, String chrID, int sIndex, int eIndex, String strand, int flankRegion) throws IOException {
        this.genome = genome;
        this.chrID = chrID;
        this.sIndex = sIndex;
        this.eIndex = eIndex;
        this.strand = strand.trim().charAt(0) + "";
        this.flankRegion = flankRegion;
        this.predictions = Collections.synchronizedList(new ArrayList<>());
        this.complete = false;
    }

    public String getSequence() throws IOException, FileNotFoundException, IDDoesNotExistException {
        GenomeManager.DIR dir = GenomeManager.DIR.POSITIVE;
        if (this.strand.equals("-")) {
            dir = GenomeManager.DIR.NEGATIVE;
        }
        return this.genome.getDNAClamped(this.chrID, this.sIndex - this.flankRegion, this.eIndex + this.flankRegion, dir);
    }

    @Override
    public void run() {
/*
        try {
            String sequence = getSequence();
            File inputFile = new File("RNALfold_input" + this.getId() + ".tmp");
            File outputFile = new File("RNALfold_output" + this.getId() + ".tmp");
            PrintWriter writer = new PrintWriter(inputFile);
            writer.println(">" + sequence);
            writer.println(sequence);
            writer.close();
            String[] arguments = {RNALfoldPath};
            runExe(arguments, inputFile, outputFile, null);

            Scanner in = new Scanner(outputFile);
            in.nextLine();
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (!(line.startsWith("T") || line.startsWith("G") || line.startsWith("A") || line.startsWith("C") || line.startsWith("U"))) {
                    String[] components = line.split(" ", 2);
                    String foldedStructure = components[0].trim();
                    components[1] = components[1].replaceAll("[()]", "").trim();
                    String[] subcomponents = components[1].split(" ", 2);
                    double energy = Double.parseDouble(subcomponents[0].trim());
                    int offset = Integer.parseInt(subcomponents[1].trim()) - 1;
                    String foldedSequence = sequence.substring(offset, offset + foldedStructure.length());

                    int precursor_sIndex = this.sIndex - this.flankRegion + offset;
                    int precursor_eIndex = precursor_sIndex + foldedSequence.length() - 1;
                    // only keep predictions that fully contain the input sequence (excluding flanking regions)
                    if (precursor_sIndex <= this.sIndex && precursor_eIndex >= this.eIndex) {
                        int miRNA_sIndex_offset = this.sIndex - precursor_sIndex;
                        int miRNA_eIndex_offset = this.eIndex - precursor_sIndex;
                        String miRNA_structure = foldedStructure.substring(miRNA_sIndex_offset, miRNA_eIndex_offset + 1);
                        String miRNA_sequence = foldedSequence.substring(miRNA_sIndex_offset, miRNA_eIndex_offset + 1);
                        // only store precursor if it doesn't contain both: '(' and ')'
                        if (!(miRNA_structure.contains("(") && miRNA_structure.contains(")"))) {
                            Precursor_Entity precursor = new Precursor_Entity(foldedSequence, foldedStructure, chrID, precursor_sIndex, precursor_eIndex, strand, energy);
                            Prediction_Entity prediction = new Prediction_Entity("lfold", precursor, null, null);

                            //prediction.setMature(new miRNA(miRNA_sequence, miRNA_structure));
                            predictions.add(prediction);
                        }
                    }

                } else {
                    break;
                }
            }
            inputFile.delete();
            outputFile.delete();
            this.complete = true;
        } catch (IOException | InterruptedException | NumberFormatException e) {
            System.err.println("Lfold Thread Run Exception: " + e);
            this.complete = true;
        } catch (IDDoesNotExistException ex) {
            Logger.getLogger(LfoldThread.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public List<Prediction_Entity> getPredictions() {
        return this.predictions;
    }

}
