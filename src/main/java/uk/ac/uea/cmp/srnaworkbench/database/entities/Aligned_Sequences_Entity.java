/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;

/**
 *
 * @author w0445959
 */
@Entity
@Table(name = "ALIGNED_SEQUENCES")
@NamedQueries(
        {
            @NamedQuery(
                    name = "@HQL_GET_ALL_ALIGNMENTS",
                    query = "from Aligned_Sequences_Entity"
            )
        })

@NamedNativeQueries(
        {
            @NamedNativeQuery(
                    name = "@SQL_GET_DISTINCT_ALIGN_SEQS",
                    query = "SELECT DISTINCT RNA_Sequence FROM ALIGNED_SEQUENCES",
                    resultClass = Aligned_Sequences_Entity.class
            )
        })

public class Aligned_Sequences_Entity implements Serializable {

    // Reference string to use as default. Please use throughout the code.

    public static final String DEFAULT_REFERENCE_STRING = ReferenceSequenceManager.GENOME_REFERENCE_NAME;

    public static Aligned_Sequences_Entity NO_ALIGNMENT = new Aligned_Sequences_Entity("", "", "", 0, 0, "", 0);
    @EmbeddedId
    //@Column(name = "ID")
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY)//eager
    @JoinColumn(name = "RNA_Sequence", insertable = false, updatable = false)
    private Unique_Sequences_Entity aligned_sequence;

    @Column(name = "Gaps", nullable = false)
    private int gaps;

    @Column(name = "RNA_Sequence")
    private String rna_sequence;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "annotatedSequences")
    private Set<GFF_Entity> annotations = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locus_ID")
    private SRNA_Locus_Entity locus_sequences;
    
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mature", cascade = CascadeType.ALL)
    private Set<Prediction_Entity> mature_predictions = new HashSet<>(0);
    
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "star", cascade = CascadeType.ALL)
    private Set<Prediction_Entity> star_predictions = new HashSet<>(0);

    // annotation type having the compound id of "type" and "reference"
    // CascadeType.ALL must be here otherwise retrieval of anything other than
    // initial alignments results in an objectNorFound
//    @MapsId("id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                @JoinColumn(name = "reference"),
                @JoinColumn(name = "type")
            })
    private Annotation_Type_Entity annotType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        // name require hibernate names, referencedColumnName requires SQL columns names
        @JoinColumn(name = "window_chrom", referencedColumnName = "seqid"),
        @JoinColumn(name = "window_reference_sequence", referencedColumnName = "reference_name"),
        @JoinColumn(name = "window_Id", referencedColumnName = "window_Id", nullable = true),
        @JoinColumn(name = "window_Length", referencedColumnName = "window_Length", nullable = true)
    })
    private Alignment_Window_Entity alignment_window;
    
    
    //relative start after flipping
//   // @Column(name = "Rel_Start")
//    private int begRel;
//    
//    //relative end after flipping
//   // @Column(name = "Rel_End")
//    private int endRel;
//    
//   // @Column(name = "Is_Flipped")
//    private boolean isFlipped = false;

    public Aligned_Sequences_Entity() {

    }

    public Aligned_Sequences_Entity(String reference, String chrom, String rna_sequence, int start, int end, String strand, int gaps) {
        //this.aligned_sequence = s_e;
        this.gaps = gaps;
        this.rna_sequence = rna_sequence;
        this.getId().start = start;
        this.getId().end = end;
        this.getId().reference_sequence = reference;
        this.getId().chrom = chrom;
        this.getId().strand = strand;
        
//        this.begRel = start;
//        this.endRel = end;
    }

    public Alignment_Window_Entity getAlignmentWindow() {
        return alignment_window;
    }

    public SRNA_Locus_Entity getLocus_sequences() {
        return locus_sequences;
    }

    public void setLocus_sequences(SRNA_Locus_Entity locus_sequences) {
        this.locus_sequences = locus_sequences;
    }

    public void setAlignmentWindow(Alignment_Window_Entity window) {
        this.alignment_window = window;
    }

    public void setAnnotationType(Annotation_Type_Entity annotType) {
        this.annotType = annotType;
    }

    public Annotation_Type_Entity getAnnotationType() {
        return this.annotType;
    }

    public Set<GFF_Entity> getAnnotations() {
        return this.annotations;
    }

    public void addAnnotation(GFF_Entity gffe) {
        this.annotations.add(gffe);
    }

    @Embeddable
    public static class Id implements Serializable {

        // the name of the reference set that this sequence is aligned to
        // e.g. "genome" or "miRBase"

        @Column(name = "reference_sequence")
        private String reference_sequence;
        @Column(name = "Seq_Start")
        private int start;
        @Column(name = "Seq_End")
        private int end;
        @Column(name = "Chrom")
        private String chrom;
        @Column(name = "Strand")
        private String strand;

        public Id() {

        }

        public Id(int start, int end, String chrom, String strand, String reference) {
            this.start = start;
            this.end = end;
            this.chrom = chrom;
            this.strand = strand;
            this.reference_sequence = reference;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Id) {
                Id that = (Id) obj;
                return this.reference_sequence.equals(that.reference_sequence)
                        && this.chrom.equals(that.chrom)
                        && this.strand.equals(that.strand)
                        && this.start == that.start
                        && this.end == that.end;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return reference_sequence.hashCode() + chrom.hashCode() + strand.hashCode() + start + end;
        }

        @Override
        public String toString() {
            return String.format("%s %s %d %d %s", this.reference_sequence, this.chrom, this.start, this.end, this.strand);
        }

        public String getReference() {
            return reference_sequence;
        }

        public void setReference(String reference) {
            this.reference_sequence = reference;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }
        
        

        public void setEnd(int end) {
            this.end = end;
        }

        public String getChrom() {
            return chrom;
        }

        public void setChrom(String chrom) {
            this.chrom = chrom;
        }

        public String getStrand() {
            return strand;
        }

        public void setStrand(String strand) {
            this.strand = strand;
        }

    }

    public String getChromosome() {
        return this.id.chrom;
    }

    public int getStart() {
        return this.id.start;
    }

    public int getEnd() {
        return this.id.end;
    }
    
     public int getStart1() {
        return this.id.start - 1;
    }

    public int getEnd1() {
        return this.id.end - 1;
    }

    public String getStrand() {
        return this.id.strand;
    }

    public String getRna_seq() {
        return rna_sequence;
    }

    public void setRna_seq(String rna_seq) {
        this.rna_sequence = rna_seq;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Unique_Sequences_Entity getAligned_seqeunce() {
        return aligned_sequence;
    }

    public void setAligned_seqeunce(Unique_Sequences_Entity aligned_seqeunce) {
        this.aligned_sequence = aligned_seqeunce;

    }

    public int getGaps() {
        return gaps;
    }

    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    @Override
    public String toString() {
        String idString = "null";
        if (this.id != null) {
            idString = String.format("ID[%s]", this.id.toString());
        }
        return String.format("%s %s %d", idString, this.rna_sequence, this.gaps);
    }

    public boolean isSame(Aligned_Sequences_Entity other) {
        if (this.id.chrom.equals(other.id.chrom) && this.id.start == other.id.start && this.id.end == other.id.end && this.id.strand.equals(other.id.strand)) {
            return true;
        }
        return false;
    }
    
//     public boolean isFlipped(){
//        return isFlipped;
//    }
//    
//    public void flip(){
//        isFlipped = !isFlipped;
//    }
    
    public boolean isSameStrand(Aligned_Sequences_Entity s){
        return this.getStrand().trim().equals(s.getStrand().trim());
    }
    
//    public int getStartRel() {
//        //return beg;
//        return begRel;
//    }
     
     public int getLength() {
        return this.aligned_sequence.getRNA_Size();
    }
     
     public int getAb() {
        return this.aligned_sequence.getTotalCount();
    }
     
//     public int getEndRel() {
//       // return end;
//        return endRel;
//    }

     
     public boolean isSameSequence(Aligned_Sequences_Entity other){
        return this.getRna_seq().equals(other.getRna_seq()) 
                && this.getAb() == other.getAb();
    }
   
//     public void setBegEndRel(int b, int e){
//         begRel =  b + ( e - getEnd());
//         endRel = begRel + getLength() - 1;
//         flip();
//     }
     
     public boolean isNegative(){
        return getStrand().trim().equals("-");
    }
    
//    public void resetBegEndRel(){
//        begRel = this.getStart();
//        endRel = this.getEnd();
//        
//        if(isFlipped)
//            flip();
//    }
    
    public String myToString(int beg) {
       
        return myToString(beg, '-');
    }
    
    public String myToString(int beg, char ch) {
        StringBuilder str = new StringBuilder();
        
        int b  = this.getStart1();
       // b = this.getStartRel();
        
        for(int i = 0; i< b - beg; i++){
            str.append(" ");
        }
        for(int i = 0; i< getLength(); i++){
            str.append(ch);
        }
        
        str.append(" " + getAb());
        
        return str.toString();
    }
    
    public String myToStringSeq(int beg) {
        StringBuilder str = new StringBuilder();
        
         int b = getStart1();//getStartRel();
        
        for(int i = 0; i< b - beg; i++){
            str.append(" ");
        }
        str.append(this.getRna_seq());
        
        str.append(" " + getAb());
        
        return str.toString();
    }
    
    public boolean isComplex(double percent) {
        String seq = this.getRna_seq();
        int[] chars = new int[5];
        
        for(char c: seq.toCharArray()){
            if(c == 'A') chars[0]++;
            else
            if(c == 'C') chars[1]++;
            else
            if(c == 'G') chars[2]++;
            else
            if(c == 'T' || c == 'U') chars[3]++;
            else chars[4] ++;
        }
        
        int size = seq.length();
        
        for(int i: chars){
            if(i/(double)size >= percent)
                return false;
        }
        
        for(int i = 0; i < 5; i++){
            for(int j = i+1; j < 5; j++)
                if((chars[i] + chars[j])/(double)size >= percent)
                    return false;
        }
        
        return true;
    }
    
    public String toStringPatman() {
         return  getChromosome() + "\t" + 
                 getRna_seq() +  "(" + getAb() + ")" +
                 "\t" + (getStart()) + "\t"  + (getEnd()) +
                 "\t" +  getStrand()  + "\t" + getGaps();
    }
    
    public String toStringPatman(char ch) {
         return  getChromosome() + ch +  
                 getRna_seq() +  ch + 
                 getAb() + ch + 
                 (getStart() ) + ch + 
                 (getEnd() )  + ch +  
                 getStrand()  + ch + getGaps();
    }
    
    public static Aligned_Sequences_Entity getFromPatman(String pLine){
        String[] sa = pLine.split("\t");

        int b = 0;
        int e = 0;
        
        try{
         b = Integer.parseInt(sa[2].trim()) - 1;
         e = Integer.parseInt(sa[3].trim()) - 1 ;
        }
        catch(NumberFormatException ex){
            return null;
        }

        
        //split the sequnce from abundance
        String[] sb = sa[1].split("\\(|\\)|-");

        
        int a = 1;
        try{
            a = Integer.parseInt(sb[1].trim());
        }
        catch(NumberFormatException ex){
            return null;
        }

        String seq = sb[0];


        
        char strand = sa[4].charAt(0);

        int missm = 0;
        
        try{
            missm = Integer.parseInt(sa[5].trim());
        }
        catch(NumberFormatException ex){
            return null;
        }
        
        return new Aligned_Sequences_Entity("genome", sa[0], sb[0], b, e, sa[4], missm);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Aligned_Sequences_Entity other = (Aligned_Sequences_Entity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
