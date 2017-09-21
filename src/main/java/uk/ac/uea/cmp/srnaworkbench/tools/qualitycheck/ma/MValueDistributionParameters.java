package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 * Option M value distribution parameters.
 * Minimum expression is the minimum expression that both reference and observed
 * values must have to be included in the fold change distribution
 * @author Matthew Beckers
 */
public class MValueDistributionParameters extends ToolParameters {
    
    // minimum expression that both reference and observed values must have to be included in distribution
    public static final ParameterDefinition<Double> MIN_EXPRESSION = new ParameterDefinition("minimum_expression", 20d);
    
    public Double getMinExpression(){
        return this.getParameterValue(Double.class, MIN_EXPRESSION.getName());
    }
    
    public void setMinExpression(Double minExpression){
        this.setParameter(MIN_EXPRESSION, minExpression);
    }
    
    /**
     * Default constructor. Sets all parameters to theirs defaults until
     * one of the set setter methods are used.
     */
    public MValueDistributionParameters()
    {
        this.addParameter(MIN_EXPRESSION);
    }
}
