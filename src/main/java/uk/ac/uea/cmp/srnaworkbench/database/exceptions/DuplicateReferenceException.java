package uk.ac.uea.cmp.srnaworkbench.database.exceptions;

/**
 *
 * @author Matthew
 */
public class DuplicateReferenceException extends DatabaseAnnotationException {

    public DuplicateReferenceException(String reference) {
        super("Reference entity with id " + reference + " already exists");
    }
    
}
