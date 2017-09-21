/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.entities;

import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NoSuchExpressionValueException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author w0445959
 */
@Entity
@Table(name = "SEQUENCES")
@NamedQueries({
    @NamedQuery(
            name = "@HQL_GET_ALL_SEQUENCES",
            query = "from Sequence_Entity"
    ),
    @NamedQuery(
            name = "@HQL_GET_SORTED_COLUMN",
            query = "from Sequence_Entity seqs where seqs.filename  = :filename order by seqs.abundance ASC"
    ),
    @NamedQuery(
            name="@HQL_SEQUENCES_WITH_TYPE_AND_FILE",
            //query = "from Sequence_Entity as S join S.unique_sequence as U join U.type as T with T.id.type in (:types) where S.filename in (:filenames)" 
            query="from Sequence_Entity as S join S.unique_sequence as U join U.type as T where S.fileID in (:filenames) and  T.id.type in (:types)",
            cacheable=true
    )
})
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "@SQL_GET_ALIGNED",
            query = "SELECT * "
            + "FROM SEQUENCES "
            + "WHERE exists ( select 1 from ALIGNED_SEQUENCES where SEQUENCES.RNA_SEQUENCE=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1 ) ",
            //+ "ORDER BY SEQUENCES.abundance ASC",
            resultClass = Sequence_Entity.class
    )
})

public class Sequence_Entity implements Serializable {

    //note, column is renamed here, without this hibernate will assume the primary field
    //is called classname_ID
    @Id
    @Column(name = "SEQUENCE_ID")
    private Long sequenceId;

    // @Column(name = "RNA_Sequence", nullable=false, length = 50)
    @Column(name = "RNA_Sequence", nullable = false)
    private String RNA_Sequence;
    @Column(name = "GenomeHitCount", nullable = false)
    private int genomeHitCount;
    @Column(name = "abundance", nullable = false)
    private int abundance;
    
    // inverse side
    @OneToMany(mappedBy="sequence", cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name="normType")
    private Map<NormalisationType, Expression_Entity> expressions = new HashMap<>();

    @Column(name = "weighted_abundance", nullable = true)
    private double weighted_abundance;

    @ManyToOne()
    @JoinColumn(name = "File_Name", insertable = false, updatable = false)
    private Filename_Entity filename_sequence;

    @Column(name = "File_Name", nullable = false)
    private String filename;
    
    @Column(name = "File_ID", nullable = false)
    private String fileID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RNA_Sequence", insertable = false, updatable = false)
    private Unique_Sequences_Entity unique_sequence;

    public Sequence_Entity() {
    }

    public Sequence_Entity(Long ID, String seq, int hitCount, int startingAbundance,
        //    double TOTAL_COUNT, double UPPER_QUARTILE, double TRIMMED_MEAN, double QUANTILE, double BOOTSTRAP, double DESEQ,
            double weighted_abundance, String filename) {
        this.sequenceId = ID;
        this.RNA_Sequence = seq;
        this.genomeHitCount = hitCount;
        this.abundance = startingAbundance;
        this.filename = filename;
//        this.TOTAL_COUNT = TOTAL_COUNT;
//        this.UPPER_QUARTILE = UPPER_QUARTILE;
//        this.TRIMMED_MEAN = TRIMMED_MEAN;
//        this.QUANTILE = QUANTILE;
//        this.BOOTSTRAP = BOOTSTRAP;
//        this.DESEQ = DESEQ;
        this.weighted_abundance = weighted_abundance;
        
        this.expressions.put(NormalisationType.NONE, new Expression_Entity( startingAbundance, NormalisationType.NONE, this));
    }
    
    
    public Map<NormalisationType, Expression_Entity> getExpressions()
    {
        return expressions;
    }

    public void setExpressions(Map<NormalisationType, Expression_Entity> expressions)
    {
        this.expressions = expressions;
    }
    
    public void addExpression(NormalisationType normType, double expression)
    {
        this.expressions.put(normType, new Expression_Entity(expression, normType, this));
    }
    
    public Double getExpression(NormalisationType normType) throws NoSuchExpressionValueException
    {
        Expression_Entity e = expressions.get(normType);
        if(e == null)
        {
            throw new NoSuchExpressionValueException("Expression value for sequence " + this.RNA_Sequence + ", " + this.filename + " and normalisation " + normType + " has not been stored yet.");
        }
        return e.getExpression();
    }


    @Override
    public String toString() {
        String str = "";
        str += "id: " + this.sequenceId + " ";
        str += "seq: " + this.RNA_Sequence + " ";
        str += "filename: " + this.filename + " ";
        str += "Expressions: ";
        
        for(Entry<NormalisationType, Expression_Entity> exp : this.expressions.entrySet())
        {
            str += "\t" + exp.getValue().toString();
        }
              
        return str;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public double getWeighted_abundance() {
        return (double) abundance / (double) getGenomeHitCount();
    }

    public void setWeighted_abundance(double weighted_abundance) {
        this.weighted_abundance = weighted_abundance;
    }

    public String getRNA_Sequence() {
        return this.RNA_Sequence;
    }

    public void setSequence(String seq) {
        this.RNA_Sequence = seq;
    }

    public int getGenomeHitCount() {
        return unique_sequence.getAlignedSequenceRelationships().size();
    }

    public void setGenomeHitCount(int genomeHitCount) {
        this.genomeHitCount = genomeHitCount;
    }

    public int getAbundance() {
        return abundance;
    }

    public void setAbundance(int totalAbundance) {
        this.abundance = totalAbundance;
    }


    public Filename_Entity getFilename_sequence() {
        return filename_sequence;
    }

    public void setFilename_sequence(Filename_Entity filename_sequence) {
        this.filename_sequence = filename_sequence;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getId() {
        return sequenceId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public Unique_Sequences_Entity getUnique_sequence() {
        return unique_sequence;
    }

    public void setUnique_sequence(Unique_Sequences_Entity unique_sequence) {
        this.unique_sequence = unique_sequence;
    }

    public List<String> toStringArray() {
        List<String> list = new LinkedList<>();
        list.add(this.RNA_Sequence);
        list.add(String.format("%d", this.abundance));
        return list;
    }

}
