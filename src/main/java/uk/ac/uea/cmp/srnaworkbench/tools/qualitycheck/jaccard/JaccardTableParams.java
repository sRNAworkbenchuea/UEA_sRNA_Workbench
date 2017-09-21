/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 *
 * @author Matthew
 */
public class JaccardTableParams extends ToolParameters {
    private final ParameterDefinition<Integer> NUMBER_OF_SEQUENCES = new ParameterDefinition("number_of_sequences", 500);
    
    public int getNumberOfSequences()
    {
        return this.getParameterValue(Integer.class, NUMBER_OF_SEQUENCES.getName());
    }
    
    public void setNumberOfSequences(int numberOfSequences)
    {
        this.setParameter(NUMBER_OF_SEQUENCES, numberOfSequences);
    }
    
    public JaccardTableParams()
    {
        this.addParameter(NUMBER_OF_SEQUENCES);
    }
}
