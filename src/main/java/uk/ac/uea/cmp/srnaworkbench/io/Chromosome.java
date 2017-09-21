package uk.ac.uea.cmp.srnaworkbench.io;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/*
 *
 * @author Chris Applegate
 */
public class Chromosome {

    // a unique identifier for the chromosome
    private final String id;
    // the description of the chromosome
    private final String description;
    // file path of the memory-mapped chromosome
    private final Path memMapChromosomePath;
    // length of the chromosome (num nucleotides)
    private final int length;
    
    //the original header used in other programs for reference
    private final String originalHeader;

    /*
     * Constructor
     * @param id: unique identifier for the chromosome
     * @param description: the description of the chromosome
     * @param dna: the entire dna data for the chromosome (5'->3')
     */
    public Chromosome(String id, String description, StringBuilder dna, String originalHeader) {
        //set the original header
        this.originalHeader = originalHeader;
        // set the id
        this.id = id;
        // set the description
        this.description = description;
        // set the length of the chromosome
        this.length = dna.length();
        // determine the path for the memory-mapped chromosome file
        this.memMapChromosomePath = Paths.get(Tools.GENOME_DATA_Path + DIR_SEPARATOR + this.id + ".tmp");
        // warn the user if the file already exists as it will be overwritten
        if (this.memMapChromosomePath.toFile().exists()) {
            System.err.println("WARNING: " + this.memMapChromosomePath.getFileName() + " already exists. File will be overwritten!");
            // delete the file
            this.memMapChromosomePath.toFile().delete();
        }
        try {
            PrintWriter writer = new PrintWriter(this.memMapChromosomePath.toFile());
            {
                writer.print(dna);
            }
            writer.close();
            this.memMapChromosomePath.toFile().deleteOnExit();
        } catch (Exception ex) {
            System.err.println("ERROR: Could not create memory-mapped chromosome file: " + ex);
        }
    }

    public String getOriginalHeader()
    {
        return originalHeader;
    }

    /*
     * returns the id of the chromosome
     */
    public String getID() {
        return this.id;
    }

    /*
     * returns the length of the chromosome (nucleotides)
     */
    public int getLength() {
        return this.length;
    }

    /*
     * returns the memory-mapped file path of the chromosome
     */
    public Path getPath() {
        return this.memMapChromosomePath;
    }

    /*
     * returns the description of the chromosome
     */
    public String getDescription() {
        return this.description;
    }
}
