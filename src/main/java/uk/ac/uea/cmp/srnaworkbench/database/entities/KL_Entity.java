package uk.ac.uea.cmp.srnaworkbench.database.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Table for storing Kulback-Leibler divergence measures
 * @author Matthew
 */
@Table(name="KL_Values")
@Entity
public class KL_Entity implements Serializable {
    
    @EmbeddedId
    Id id;
    
    @Column(name="kl_value")
    double kl;
    
    @Column(name="kl_smoothed")
    double kls;
    
    public KL_Entity(){}
    public KL_Entity(Filename_Entity filename, double expression, double kl)
    {
        this.id = new Id(filename, expression);
        this.kl = kl;
        this.kls = kl;
    }
    
    

    public Id getId() {
        return id;
    }

    public double getKlSmoothed() {
        return kls;
    }

    public void setKlSmoothed(double kls) {
        this.kls = kls;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public double getKl() {
        return kl;
    }

    public void setKl(double kl) {
        this.kl = kl;
    }
    
    
    
    @Embeddable
    public static class Id implements Serializable
    {
        @ManyToOne
        @JoinColumn(name="Filename")
        Filename_Entity filename;
        
        @Column(name="Expression")
        double expression;
        
        public Id(){}
        public Id(Filename_Entity filename, double expression)
        {
            this.filename = filename;
            this.expression = expression;
        }

        public Filename_Entity getFilename() {
            return filename;
        }

        public void setFilename(Filename_Entity filename) {
            this.filename = filename;
        }

        public double getExpression() {
            return expression;
        }

        public void setExpression(double expression) {
            this.expression = expression;
        }

        @Override
        public String toString() {
            return "Id{" + "filename=" + filename + ", expression=" + expression + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.filename);
            hash = 17 * hash + (int) (Double.doubleToLongBits(this.expression) ^ (Double.doubleToLongBits(this.expression) >>> 32));
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
            final Id other = (Id) obj;
            if (!Objects.equals(this.filename, other.filename)) {
                return false;
            }
            if (Double.doubleToLongBits(this.expression) != Double.doubleToLongBits(other.expression)) {
                return false;
            }
            return true;
        }
        
        
        
    }

    @Override
    public String toString() {
        return "KL_Entity{" + id + ", kl=" + kl + '}';
    }
    
}
