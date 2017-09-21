package uk.ac.uea.cmp.srnaworkbench.exceptions;

/**
 * An exception to be created and thrown when there is a problem with
 * file IO giving some extra info about where the problem to the user.
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class PARESnipFileException extends Exception {

    public PARESnipFileException(String fileName){
        super("\n There was a problem with the IO for file "+fileName+".\nPlease check this file.");
    }
}
