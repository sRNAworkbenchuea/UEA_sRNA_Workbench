/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tplot;

/**
 *
 * @author rew13hpu
 */
public class TargetPlotConfiguration {

    int minTagLength, maxTagLength;
    boolean lowComplexityFilter;
    int numThreads;

    private static TargetPlotConfiguration config = null;

    public static TargetPlotConfiguration getInstance() {
        if (config == null) {
            config = new TargetPlotConfiguration();
        }

        return config;
    }

    private TargetPlotConfiguration() {
        minTagLength = 19;
        maxTagLength = 21;
        lowComplexityFilter = false;
        numThreads = Runtime.getRuntime().availableProcessors();
    }

    public int getMinTagLength() {
        return minTagLength;
    }

    public void setMinTagLength(int minTagLength) {
        this.minTagLength = minTagLength;
    }

    public int getMaxTagLength() {
        return maxTagLength;
    }

    public void setMaxTagLength(int maxTagLength) {
        this.maxTagLength = maxTagLength;
    }

    public boolean isLowComplexityFilter() {
        return lowComplexityFilter;
    }

    public void setLowComplexityFilter(boolean lowComplexityFilter) {
        this.lowComplexityFilter = lowComplexityFilter;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }
    
}
