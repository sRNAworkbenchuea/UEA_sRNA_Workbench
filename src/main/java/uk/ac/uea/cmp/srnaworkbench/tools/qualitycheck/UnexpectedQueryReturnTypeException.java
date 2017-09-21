package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck;

/**
 * Thrown if a query (usually one passed in to a method) returned an unexpected type
 * or property.
 * @author Matthew
 */
public class UnexpectedQueryReturnTypeException extends RuntimeException {
    public UnexpectedQueryReturnTypeException(String msg)
    {
        super(msg);
    }
}
