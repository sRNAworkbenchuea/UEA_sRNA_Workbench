package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author Matthew
 */
@Entity
@Table(name="NonRedundant_MAs")
public class MA_NR_Entity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    
    private double M;
    
    private double A;
    
    private double refExpression;
    
    private double obsExpression;
    
    private int RNA_Size;
    
    private int count;
    
    @Column(name = "trimmed", nullable = false)
    private boolean trimmed = false;
    
    private double weighting = 1;
    
    @Embedded
    private FilePair filePair;
    
    @Enumerated
    private NormalisationType normType;
    
    private String annoType;
    
    @Column(name="M_Value_Offset")
    private double offset;
    
    private int logBase;

    public MA_NR_Entity() {}

    public MA_NR_Entity(double M, double A, double refExpression, double obsExpression, FilePair pair, NormalisationType normType, String annoType, int count, double offset, int logBase, int size) {
        this.M = M;
        this.A = A;
        this.refExpression = refExpression;
        this.obsExpression = obsExpression;
        this.filePair = pair;
        this.normType = normType;
        this.annoType = annoType;
        this.count = count;
        this.offset = offset;
        this.logBase = logBase;
        this.RNA_Size = size;
    }

    public double getWeighting() {
        return weighting;
    }

    public void setWeighting(double weighting) {
        this.weighting = weighting;
    }

    public boolean isTrimmed() {
        return trimmed;
    }

    public void setTrimmed(boolean trimmed) {
        this.trimmed = trimmed;
    }

    public int getSize() {
        return RNA_Size;
    }

    public void setSize(int size) {
        this.RNA_Size = size;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public int getLogBase() {
        return logBase;
    }

    public void setLogBase(int logBase) {
        this.logBase = logBase;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getM() {
        return M;
    }

    public void setM(double M) {
        this.M = M;
    }

    public double getA() {
        return A;
    }

    public void setA(double A) {
        this.A = A;
    }

    public double getRefExpression() {
        return refExpression;
    }

    public void setRefExpression(double refExpression) {
        this.refExpression = refExpression;
    }

    public double getObsExpression() {
        return obsExpression;
    }

    public void setObsExpression(double obsExpression) {
        this.obsExpression = obsExpression;
    }

    public FilePair getPair() {
        return filePair;
    }

    public void setPair(FilePair pair) {
        this.filePair = pair;
    }

    public NormalisationType getNormType() {
        return normType;
    }

    public void setNormType(NormalisationType normType) {
        this.normType = normType;
    }

    public String getAnnoType() {
        return annoType;
    }

    public void setAnnoType(String annoType) {
        this.annoType = annoType;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MA_NR{" + "id=" + id + ", M=" + M + ", A=" + A + ", ref=" + refExpression + ", obs=" + obsExpression + ", count=" + count + ", pair=" + filePair.getPairKey() + ", normType=" + normType.getAbbrev() + ", annoType=" + annoType + '}';
    }
}
