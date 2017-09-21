package uk.ac.uea.cmp.srnaworkbench.database.exceptions;

/**
 *
 * @author Matthew
 */
public class NotInDatabaseException extends DatabaseException {
    public NotInDatabaseException(String msg)
    {
        super(msg);
    }
}
