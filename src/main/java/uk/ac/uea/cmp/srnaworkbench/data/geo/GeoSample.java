package uk.ac.uea.cmp.srnaworkbench.data.geo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Models a Sample held in GEO
 * @author Matthew
 */
public class GeoSample {
    
    private SampleAccession accession;
    private String title;
    private String sraLink;
    
    /**
     * Build a sample from a miniml XML Node
     * @param miniml 
     */
    public GeoSample(Node sampleNode) throws InvalidAccessionNumberException
    {
        this.accession = new SampleAccession(sampleNode.getAttributes().getNamedItem("iid").getNodeValue());
        Element sampleE = (Element) sampleNode;
        
        this.title = sampleE.getElementsByTagName("Title").item(0).getTextContent();
        
        Node suppNode = sampleE.getElementsByTagName("Supplementary-Data").item(0);
        if (suppNode.getAttributes().getNamedItem("type").getNodeValue().equals("SRA Experiment")) {
            String sraAddress = ((Element) suppNode).getTextContent();
            sraAddress = sraAddress.trim();
            sraLink = sraAddress.replaceFirst(".+nih\\.gov/", "");
        }
    }

    @Override
    public String toString() {
        return accession + " : " + title + " : " + sraLink;
    }

    public SampleAccession getAccession() {
        return accession;
    }

    public String getTitle() {
        return title;
    }

    public String getSraLink() {
        return sraLink;
    }
    
    
}
