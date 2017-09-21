package uk.ac.uea.cmp.srnaworkbench.exceptions;

/**
 * This exception should be thrown if data has been provided but no tree has
 * been constructed.
 * 
 * This can happen in the following ways:
 * 1) The sequences provided in the file have ALL been filtered out.
 * 2) There is no 2 yet! :-)
 *
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
public class PARESnipTreeConstructionException extends Exception {

    public PARESnipTreeConstructionException(){
        super("\nERROR: A tree was not constructed.\nPlease refer to the user manual to remedy this error.");
    }
}
