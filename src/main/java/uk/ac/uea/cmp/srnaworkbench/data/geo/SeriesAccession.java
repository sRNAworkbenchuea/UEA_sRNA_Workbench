package uk.ac.uea.cmp.srnaworkbench.data.geo;

/**
 * Models a Series accession number
 * @author Matthew
 */
public class SeriesAccession extends AccessionNumber {
    
    public SeriesAccession(String accessionNumber) throws InvalidAccessionNumberException
    {
        super(accessionNumber, "GSE");
    }
}
