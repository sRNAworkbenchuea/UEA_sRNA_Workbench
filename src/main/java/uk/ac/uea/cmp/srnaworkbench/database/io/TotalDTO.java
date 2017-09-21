/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.io;

import com.google.common.base.Joiner;
import java.util.Arrays;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Size_Class_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * JsonDTO for writing size class distributions
 *
 * @author matt
 */
public class TotalDTO implements JsonDTO {

    public String sample;
    public Long nr;
    public Double r;
    public Size_Class_Entity.MappingStatus mapped;
    public NormalisationType norm;

    public TotalDTO() {
    }

//        public String getSample() {
//            return sample;
//        }
//
//        public void setSample(String sample) {
//            this.sample = sample;
//        }
//
//        public Long getNr() {
//            return nr;
//        }
//
//        public void setNr(Long nr) {
//            this.nr = nr;
//        }
//
//        public Double getR() {
//            return r;
//        }
//
//        public void setR(Double r) {
//            this.r = r;
//        }
//
//        public MappingStatus getMapped() {
//            return mapped;
//        }
//
//        public void setMapped(MappingStatus mapped) {
//            this.mapped = mapped;
//        }
//
//        public NormalisationType getNorm() {
//            return norm;
//        }
//
//        public void setNorm(NormalisationType norm) {
//            this.norm = norm;
//        }
    @Override
    /**
     * To override this, match the input strings with the defined elements in
     * this DTO. Make sure to return ONLY basic wrapper types (i.e. String and
     * Number subclasses)
     */
    public Object getElement(String elementKey) {
        switch (elementKey) {
            case "Redundant":
                return r;
            case "Nonredundant":
                return nr;
            case "Sample":
                return sample;
            case "Normalisation":
                return norm.toString();
            case "Status":
                return mapped.toString();
            default:
                return "NA";
        }
    }

    @Override
    public String toString() {
        return Joiner.on(", ").join(Arrays.asList(this.norm.toString(), this.mapped.toString(), this.sample, this.r, this.nr));
    }

}
