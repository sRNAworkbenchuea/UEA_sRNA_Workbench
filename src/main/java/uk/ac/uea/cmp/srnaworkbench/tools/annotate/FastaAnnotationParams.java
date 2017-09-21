package uk.ac.uea.cmp.srnaworkbench.tools.annotate;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 * Optional paramters for FastaAnnotation
 * @author Matthew
 */
public class FastaAnnotationParams extends ToolParameters{
    private static final ParameterDefinition<String> DELIMITER = new ParameterDefinition("delimiter", null);

    public String getDelimiter() {
        return this.getParameterValue(String.class, DELIMITER.getName());
    }

    /**
     * Set the delimiter used to delimit the fasta headers into columns,
     * and the column that can be used to represent the seqid for each
     * sequence of this reference. If not set, the whole header will be
     * used as the seqid.
     * @param delimiter the delimiter. Can be a regex.
     * */
    public void setDelimiter(String delimiter) {
        this.setParameter(DELIMITER, delimiter);
    }
    
    private static final ParameterDefinition<Integer> COLUMN_NUMBER = new ParameterDefinition("column_number", 0);
    
    public void setColumnNumber(int columnNumber)
    {
        this.setParameter(COLUMN_NUMBER, columnNumber);
    }
    
    public int getColumnNumber()
    {
        return this.getParameterValue(Integer.class, COLUMN_NUMBER.getName());
    }
    
    public FastaAnnotationParams()
    {
        this.addParameter(DELIMITER);
        this.addParameter(COLUMN_NUMBER);
    }
}
