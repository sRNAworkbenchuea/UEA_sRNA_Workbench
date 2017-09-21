package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.TranscriptServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;

/**
 *
 * @author Chris Applegate
 */
@Entity
@Table(name = "INTERACTIONS")
public class Interaction_Entity implements Serializable {

    private static final int predictorStrLen = 50;

    private static final int duplexStrLen = 500;
    private static final int shortReadIDStrLen = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "PREDICTOR", length = predictorStrLen)
    private String predictor;

    @Column(name = "CATEGORY")
    private int category;
    @Column(name = "CLEAVAGE_POS")
    private int cleavagePos;
    @Column(name = "P_VAL")
    private double pVal;
    @Column(name = "FRAGMENT_ABUNDANCE")
    private int fragmentAbundance;
    @Column(name = "WEIGHTED_FRAGMENT_ABUNDANCE")
    private double weightedFragmentAbundance;
    @Column(name = "NORMALISED_WEIGHTED_FRAGMENT_ABUNDANCE")
    private double normalisedWeightedFragmentAbundance;
    @Column(name = "DUPLEX", length = duplexStrLen)
    private String duplex;
    @Column(name = "ALIGNMENT_SCORE")
    private double alignmentScore;
    @Column(name = "SHORT_READ_ID", length = shortReadIDStrLen)
    private String shortReadID;

    @ManyToOne()
    @JoinColumn(name = "sRNA")
    private Unique_Sequences_Entity sRNA;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "GENE")
    private Transcript_Entity gene;

    public Interaction_Entity() {
        this.duplex = "";
    }

    public void setPredictor(String predictor) {
        this.predictor = StringUtils.abbreviate(predictor, predictorStrLen);
    }

    public void setTranscript(Transcript_Entity gene) {
        this.gene = gene;//StringUtils.abbreviate(gene, geneStrLen);
    }

    public void setShortReadID(String shortReadID) {
        this.shortReadID = StringUtils.abbreviate(shortReadID, shortReadIDStrLen);
    }

    //public void set_sRNA(String sRNA) {
    //    this.sRNA = StringUtils.abbreviate(sRNA, sRNAStrLen);
    // }
    public void setDuplex(String duplex) {
        this.duplex = StringUtils.abbreviate(duplex, duplexStrLen);
    }

    public void setAlignmentScore(double score) {
        this.alignmentScore = score;
    }

    public void setsRNA(Unique_Sequences_Entity sRNA) {
        this.sRNA = sRNA;
    }

    public void setTranscript(String transcript) {
        TranscriptServiceImpl transcript_service = (TranscriptServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("TranscriptService");
        List<Transcript_Entity> transcripts = transcript_service.findByDescription(transcript);

        Transcript_Entity t;
        if (transcripts.isEmpty()) {
            t = new Transcript_Entity(transcript);

        } else {
            t = transcripts.get(0);

        }
        setTranscript(t);
    }

    /*  public void addToDuplex(String str) {
     String tmp_duplex = this.duplex + str + System.getProperty("line.separator");
     setDuplex(tmp_duplex);
     }*/
    @Override
    public String toString() {
        String str = "";
        str += "ID: " + this.id + System.getProperty("line.separator");
        str += "GENE: " + this.gene + System.getProperty("line.separator");
        str += "CATEGORY: " + this.category + System.getProperty("line.separator");
        str += "CLEAVAGE POSITION: " + this.cleavagePos + System.getProperty("line.separator");
        str += "P-VALUE: " + this.pVal + System.getProperty("line.separator");
        str += "FRAGMENT ABUNDANCE: " + this.fragmentAbundance + System.getProperty("line.separator");
        str += "WEIGHTED FRAGMENT ABUNDANCE: " + this.weightedFragmentAbundance + System.getProperty("line.separator");
        str += "NORMALIZED WEIGHTED FRAGMENT ABUNDANCE: " + this.normalisedWeightedFragmentAbundance + System.getProperty("line.separator");
        str += "ALIGNMENT SCORE: " + this.alignmentScore + System.getProperty("line.separator");
        str += "SHORT READ ID: " + this.shortReadID + System.getProperty("line.separator");
        // str += "SHORT READ ABUNDANCE: " + this.shortReadAbundane + System.getProperty("line.separator");
        // str += "NORMALIZED SHORT READ ABUNDANCE: " + this.normalisedShortReadAbundance + System.getProperty("line.separator");
        str += "DUPLEX: " + this.duplex + System.getProperty("line.separator");
        str += "sRNA: " + this.sRNA.getRNA_Sequence() + System.getProperty("line.separator");
        return str;
    }

    public void setWeightedFragmentAbundance(double abundance) {
        this.weightedFragmentAbundance = abundance;
    }

    public void setNormalisedWeightedFragmentAbundance(double abundance) {
        this.normalisedWeightedFragmentAbundance = abundance;
    }

    public void setFragmentAbundance(int abundance) {
        this.fragmentAbundance = abundance;
    }

    public void setPVal(double pVal) {
        this.pVal = pVal;
    }

    public void setCleaveagePos(int pos) {
        this.cleavagePos = pos;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    /*  public int getAbundance() {
     return this.shortReadAbundane;
     }*/
    public Transcript_Entity getGene() {
        return this.gene;
    }

    public int getCleavagePos() {
        return this.cleavagePos;
    }

    public String get_sRNA() {
        return this.sRNA.getRNA_Sequence();
    }

    public Long getID() {
        return this.id;
    }

    public double getPVal() {
        return this.pVal;
    }

    public Long getPrecursorID() {
        return id;
    }

    public int getFragmentAbundance() {
        return fragmentAbundance;
    }

    public double getWeightedFragmentAbundance() {
        return weightedFragmentAbundance;
    }

    public String getDuplex() {
        return duplex;
    }

    public double getAlignmentScore() {
        return alignmentScore;
    }

    public String getShortReadID() {
        return shortReadID;
    }

    /*  public int getShortReadAbundane() {
     return shortReadAbundane;
     }

     public double getNormalisedShortReadAbundance() {
     return normalisedShortReadAbundance;
     }*/
    public int getCategory() {
        return this.category;
    }

    public double getNormalisedWeightedFragmentAbundance() {
        return this.normalisedWeightedFragmentAbundance;
    }

    public String getPredictor() {
        return this.predictor;
    }

    /*public void parseDuplex(Set<String> srnas) throws Exception {
     
     UniqueSequencesServiceImpl service = (UniqueSequencesServiceImpl)DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
     // open the PAREsnip output file
     String enclosing5Str = "5'";
     String enclosing3Str = "3'";
     String[] duplexComponents = this.duplex.split(System.getProperty("line.separator"));
     try {
     int sRNA_sIndex = duplexComponents[0].indexOf(enclosing5Str) + enclosing5Str.length();
     int sRNA_eIndex = duplexComponents[0].indexOf(enclosing3Str) - 1;
     String tmp_sRNA = duplexComponents[0].substring(sRNA_sIndex, sRNA_eIndex + 1).trim();
     String srna = tmp_sRNA.replaceAll("[-]", "");
     String file_srna = srna;
     if (!srnas.contains(srna)) {
     String bestMatch = "";
     int bestMatchDiff = 0;
     // loop through srnas in srnaome and find what we think is the sequence of intestest
     Iterator<String> it = srnas.iterator();
     while (it.hasNext()) {
     String s = it.next();
     if (s.contains(srna) && s.startsWith(srna)) {
     int sizeDiff = s.length() - srna.length();
     if (sizeDiff < bestMatchDiff || bestMatch.isEmpty()) {
     bestMatch = s;
     bestMatchDiff = sizeDiff;
     }
     }
     if (!bestMatch.isEmpty()) {
     srna = bestMatch;
     System.out.println(file_srna + " not found in srnaome. selected: " + srna);
     } else {
     throw new Exception("The target sRNA was not found.");
     }
     }

     }
     // get the corresponding unique sequence from the table
     Unique_Sequences_Entity db_unique_sequence = service.findById(srna);
     // if there is no corresponding unique sequence: throw exception
     if(db_unique_sequence != null)
     {
     this.sRNA = db_unique_sequence;
     }
     else
     {
     throw new Exception("The target sRNA was not found in the database.");   
     }
     } catch (Exception ex) {
     throw new Exception("Error parsing sRNA/mRNA duplex: " + ex);
     }
     }*/
    public static List<String> toStringTitleArray() {
        List<String> str = new ArrayList<>();
        str.add("ID");
        str.add("Predictor");
        str.add("Category");
        str.add("Gene");
        str.add("Cleavage Pos");
        str.add("sRNA");
        str.add("Duplex");
        str.add("Alignment Score");
        str.add("Fragment Abundance");
        str.add("Weighted Fragment Abundance");
        str.add("Normalised Weighted Fragment Abundance");
        str.add("P-Val");
        return str;
    }

    public List<String> toStringArray() {
        List<String> str = new ArrayList<>();
        str.add(String.format("%d", this.id));
        str.add(this.predictor);
        str.add(String.format("%d", this.category));
        str.add(this.gene.getGene());
        str.add(String.format("%d", this.cleavagePos));
        str.add(this.sRNA.getRNA_Sequence());
        str.add(this.duplex);
        str.add(String.format("%f", this.alignmentScore));
        str.add(String.format("%d", this.fragmentAbundance));
        str.add(String.format("%f", this.weightedFragmentAbundance));
        str.add(String.format("%f", this.normalisedWeightedFragmentAbundance));
        str.add(String.format("%f", this.pVal));
        return str;
    }

    // returns the sRNA in the duplex
    public String extract_sRNAFromDuplex() {
        String enclosing5Str = "5'";
        String enclosing3Str = "3'";
        String[] duplexComponents = this.duplex.split(System.getProperty("line.separator"));
        int sRNA_sIndex = duplexComponents[0].indexOf(enclosing5Str) + enclosing5Str.length();
        int sRNA_eIndex = duplexComponents[0].indexOf(enclosing3Str) - 1;
        String tmp_sRNA = duplexComponents[0].substring(sRNA_sIndex, sRNA_eIndex + 1).trim();
        return tmp_sRNA.replaceAll("[-]", "");
    }

  
}
