package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;

/*
 *
 * @author Chris Applegate
 */
public final class GenomeManager {

    // the file path of the raw genome file
    private Path file_path;
    // a map of chromsome instances associated with chromosome ids
    private Map<String, Chromosome> chromosomes;

    public static enum DIR {

        POSITIVE, NEGATIVE
    };

    public static void main(String [] args)
    {
        GenomeManager g = new GenomeManager();
        g.getChrLength(null);
    }
    
    
    
    public GenomeManager() {
        this.file_path = null;
        this.chromosomes = new HashMap<>();
    }
    /*
     * Constructor
     * @param path: the file path of the raw genome file
     */

    public GenomeManager(Path path) throws FileNotFoundException, IOException, DuplicateIDException {
        load(path);
    }

    public void load(Path path) throws IOException, DuplicateIDException {
        this.file_path = path;
        this.chromosomes = new HashMap<>();
        parseFile();
    }

    public Map<String, Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void setChromosomes(Map<String, Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
    }

    private String normaliseID(String id) {
        return this.file_path.getFileName() + id.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /*
     * parse the raw genome file. Creates a memory-mapped file for each chromosome
     */
    private void parseFile() throws IOException, DuplicateIDException {
        // store the current chromosome ID
        String currentChrID = "";
        // store the current chromosome description
        String currentChrDescription = "";
        // store the current chromosome DNA data
        StringBuilder currentChrDNA = new StringBuilder("");
        // store the original header
        String originalHeader = "";
        // read the file
        Scanner in = new Scanner(this.file_path);
        while (in.hasNextLine()) {
            // read the current line of the file
            String line = in.nextLine();
            // if the line is a header line
            if (line.startsWith(">")) {
                
                // if we have already buffered DNA data save current chromosome
                if (currentChrDNA.length() > 0) {
                    addChromosome(currentChrID, currentChrDescription, currentChrDNA, originalHeader);
                }
                // parse the new chromosome information
                String[] components = line.substring(1).trim().toLowerCase().split(" ", 2);
                originalHeader = line.substring(1, line.length());
                // set the chromosome ID
                currentChrID = normaliseID(line.substring(1));
                // store the chromosome description
                if (components.length > 1) {
                    currentChrDescription = components[1];
                }
                // reset the DNA buffer
                currentChrDNA = new StringBuilder("");
            } // if this is not a header line then append DNA data to current DNA buffer
            else {
                currentChrDNA.append(line);
            }
        }
        // close the file
        in.close();
        // the end of the file has been reached therefore save the last chromosome
        addChromosome(currentChrID, currentChrDescription, currentChrDNA, originalHeader);
    }

    public int getChrLength(String id)
    {
        return this.chromosomes.get(normaliseID(id)).getLength();
    }
    
    /*
     * create a chromosome and add to the map. Chromosome must have unique id
     * @param id: unique identifier for the chromosome
     * @param description: the description of the chromosome
     * @param dna: the entire dna data for the chromosome (5'->3')
     */
    private void addChromosome(String id, String description, StringBuilder dna, String originalHeader) throws DuplicateIDException {
        // does a chromosome with this id already exist in the map?
        if (!this.chromosomes.containsKey(id)) {
            // add a new chromosome to the map of chromosome
            this.chromosomes.put(id, new Chromosome(id, description, dna, originalHeader));
        } else {
            throw new DuplicateIDException("A chromosome with this ID already exists in the map. Chromosome not added.");
        }
    }

    /*
     * returns the file path of the raw genome file
     */
    public Path getPath() {
        return this.file_path;
    }

    /*
     * returns the DNA sequence (5'->3') on appropriate strand for the genome at the specified chromosome between the start and end nucleotide positions (referenced from 5' + strand) (inclusive)
     * @param id: unique identifier for the chromosome
     * @param sIndex: the start nucleotide position to read from (inclusive). 1 is first nucleotide (referenced from 5' + strand)
     * @param eIndex: the end nucleotide position to read to (inclusive). 1 is first nucleotide (referenced from 5' + strand)
     * @param direction: the dna strand to return (+ or -)
     */
    public String getDNA(String id, int sIndex, int eIndex, DIR direction) throws FileNotFoundException, IOException {
        String dna = readMemMappedFile(this.chromosomes.get(normaliseID(id)).getPath(), sIndex - 1, eIndex - 1);
        // if the positive strand was specified then return data
        if (direction == DIR.POSITIVE) {
            return dna;
        }
        // otherwise return the reverse compliment of the dna
        return SequenceUtils.DNA.reverseComplement(dna);
    }

    /*
     * returns the DNA sequence (5'->3') on appropriate strand for the genome at the specified chromosome between the start and end nucleotide positions (referenced from 5' + strand) (inclusive)
     * note: if the specified read region extends beyond the chromosome then clamp the read region to fit the chromosome
     * @param id: unique identifier for the chromosome
     * @param sIndex: the start nucleotide position to read from (inclusive). 1 is first nucleotide (referenced from 5' + strand)
     * @param eIndex: the end nucleotide position to read to (inclusive). 1 is first nucleotide (referenced from 5' + strand)
     * @param direction: the dna strand to return (+ or -)
     */
    public String getDNAClamped(String id, int sIndex, int eIndex, DIR direction) throws FileNotFoundException, IOException, IDDoesNotExistException {
        // get the chromsome
        Chromosome chromosome = this.chromosomes.get(normaliseID(id));
        if (chromosome == null) {
            throw new IDDoesNotExistException("Cannot find: " + id);
        }

        // clamp the read indices
        sIndex = Math.max(0, sIndex);
        eIndex = Math.min((int) chromosome.getLength() - 1, eIndex);
        // read the dna
        return getDNA(id, sIndex, eIndex, direction);
    }

    public String getDNAClamped(String id, int sIndex, int eIndex, String direction) throws FileNotFoundException, IOException, IDDoesNotExistException, Exception {

        if (direction.equals("+")) {
            return getDNAClamped(id, sIndex, eIndex, DIR.POSITIVE);
        } else if (direction.equals("-")) {
            return getDNAClamped(id, sIndex, eIndex, DIR.NEGATIVE);
        }
        throw new Exception("Direcrion not valid");
    }
    /*
     * returns a string representation of the memory-mapped file between the read start and end positons (inclusive)
     * @param path: path of the memory-mapped file to read from
     * @param sIndex: the start position to read from (inclusive). 
     * @param eIndex: the end position to read to (inclusive). 
     */

    private static String readMemMappedFile(Path path, int sIndex, int eIndex) throws FileNotFoundException, IOException {
        // create a channel to the file
        FileChannel channel = new RandomAccessFile(path.toFile(), "r").getChannel();
        // determine the length of the read region
        long length = eIndex - sIndex + 1;
        // read the data
        MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY, sIndex, length);
        // close the channel
        channel.close();
        // return the data
        return Charset.defaultCharset().decode(buff).toString();
    }
}
