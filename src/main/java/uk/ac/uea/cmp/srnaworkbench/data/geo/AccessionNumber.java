package uk.ac.uea.cmp.srnaworkbench.data.geo;

/**
 *
 * @author Matthew
 */
public abstract class AccessionNumber {
    public String accession;
    public AccessionNumber(String accessionNumber, String qualifierPrefix) throws InvalidAccessionNumberException
    {
        if(!accessionNumber.startsWith(qualifierPrefix))
            throw new InvalidAccessionNumberException("Accession number must start with the prefix " + qualifierPrefix);
        this.accession = accessionNumber;
    }
    
    public String getSubDirectory()
    {
        return accession.replaceAll(accession.substring(accession.length()-3, accession.length()), "nnn");
    }
    
    @Override
    public String toString()
    {
        return accession;
    }
}
