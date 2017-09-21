package uk.ac.uea.cmp.srnaworkbench.data.geo;

/**
 *
 * @author Matthew
 */
public class SampleAccession extends AccessionNumber {

    public SampleAccession(String accessionNumber) throws InvalidAccessionNumberException {
        super(accessionNumber, "GSM");
    }
    
}
