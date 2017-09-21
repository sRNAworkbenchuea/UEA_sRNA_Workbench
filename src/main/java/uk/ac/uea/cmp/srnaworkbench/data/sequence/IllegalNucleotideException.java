/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author mka07yyu
 */
public class IllegalNucleotideException extends Exception{
    
    public IllegalNucleotideException(char badNucleotide) {
        super(badNucleotide + "Is not an accepted nucleotide. Accepted nucleotides are" + StringUtils.join(Arrays.asList(Nucleotide.values()), ','));
    }
}
