package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 *
 * @author Matthew
 */
public class StrandBiasDivergenceParams extends ToolParameters{
    public static final ParameterDefinition<Integer> WINDOW_SIZE = new ParameterDefinition("window_size", 4000);
    
    public void setWindowSize(int windowSize)
    {
        this.setParameter(WINDOW_SIZE, windowSize);
    }
    
    public int getWindowSize()
    {
        return this.getParameterValue(Integer.class, WINDOW_SIZE.getName());
    }
     
    public static final ParameterDefinition<Integer> NUMBER_OF_BINS = new ParameterDefinition("number_of_bins", 100);
    
    public void setNumberOfBins(int numberOfBins)
    {
        this.setParameter(NUMBER_OF_BINS, numberOfBins);
    }
    
    public int getNumberOfBins()
    {
        return this.getParameterValue(Integer.class, NUMBER_OF_BINS.getName());
    }
    
    public static final ParameterDefinition<Double> MAX_EXPRESSION_LEVEL = new ParameterDefinition("max_expression_level", 200.0);
    
    public void setMaxExpressionLevel(double maxExpressionLevel)
    {
        this.setParameter(MAX_EXPRESSION_LEVEL, maxExpressionLevel);
    }
    
    public double getMaxExpressionLevel()
    {
        return this.getParameterValue(Double.class, MAX_EXPRESSION_LEVEL.getName());
    }
    
    public StrandBiasDivergenceParams()
    {
        this.addParameter(WINDOW_SIZE);
        this.addParameter(NUMBER_OF_BINS);
        this.addParameter(MAX_EXPRESSION_LEVEL);
    }

}
