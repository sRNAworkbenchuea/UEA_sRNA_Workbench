package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution;

import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolParameters;

/**
 * Optional parameters for making abundance distributions from sequence abundances
 * @author Matthew Beckers
 */
public class AbundanceDistributionParameters extends ToolParameters {
    
    /** Logarithm to use for caclulating the distribution **/
    public static final ParameterDefinition<Logarithm> LOGARITHM = new ParameterDefinition("logarithm", Logarithm.BASE_2);
    
    public Logarithm getLogarithm()
    {
        return this.getParameterValue(Logarithm.class, LOGARITHM.getName());
    }
    
    public void setLogarithm(String logarithm)
    {
        this.setParameter(LOGARITHM, Logarithm.fromString(logarithm));
    }
    
    /** Size of each per-sequence total abundance window to calculate distributions over **/
    public static final ParameterDefinition<Integer> WINDOW_SIZE = new ParameterDefinition("window_size", 1000);
    
    public int getWindowSize(){
       return this.getParameterValue(Integer.class, WINDOW_SIZE.getName());
    }
    
    public void setWindowSize(int windowSize)
    {
        this.setParameter(WINDOW_SIZE, windowSize);
    }
    
    /**
     * Initial count to start the windows at
     */
    public static final ParameterDefinition<Integer> FIRST_COUNT = new ParameterDefinition("first_count", 1000);

    public int getFirstCount() {
        return this.getParameterValue(Integer.class, FIRST_COUNT.getName());
    }

    public void setFirstCount(int firstCount) {
        this.setParameter(FIRST_COUNT, firstCount);
    }
    
    /** Number of per-sequence total abundance windows to calculate distributions for, starting with the window containing
     * the highest count sequences
     */
    public static final ParameterDefinition<Integer> NUMBER_OF_WINDOWS = new ParameterDefinition("number_of_windows", 3);
    
    public int getNumberOfWindows(){
        return this.getParameterValue(Integer.class, NUMBER_OF_WINDOWS.getName());
    }
    
    public void setNumberOfWindows(int numberOfWindows){
        this.setParameter(NUMBER_OF_WINDOWS, numberOfWindows);
    }
    
    /**
     * Default constructor. Sets all parameters to theirs defaults until
     * one of the set setter methods are used.
     */
    public AbundanceDistributionParameters()
    {
        this.addParameter(LOGARITHM);
        this.addParameter(WINDOW_SIZE);
        this.addParameter(NUMBER_OF_WINDOWS);
        this.addParameter(FIRST_COUNT);
    }
    
}
