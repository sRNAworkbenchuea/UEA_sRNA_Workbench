package uk.ac.uea.cmp.srnaworkbench.tools;

import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;

/**
 * Extension of RunnableTool to allow easy enabling of timing of events throughout
 * the tool.
 * @author matt
 */
public abstract class TimeableTool extends RunnableTool {
    private StopWatch sw;
    private boolean timeable = false;
    
    public TimeableTool(final String toolName)
    {
        super(toolName);
        sw = new StopWatch(toolName);
    }
    
    protected TimeableTool( final String toolName, final StatusTracker tracker )
    {
        super(toolName, tracker);
        sw = new StopWatch(toolName);
    }
    
    /**
     * Sets whether times will be printed after this tool has processed.
     * @param timeable 
     */
    public void setTimeable(boolean timeable) {
        this.timeable = timeable;
    }

    /**
     * Returns whether this process and events are being timed
     * @return 
     */
    public boolean isTimeable() {
        return this.timeable;
    }
    
    /**
     * Used by inheriting classes to lap the built-in timer for specific events
     * that need timing.
     * @param message 
     */
    public void lapTimer(String message)
    {
        if(isTimeable())
        {
            this.sw.lap(message);
        }
    }

    
    /**
     * Processes but also starts, stops and prints times if the object has been
     * set as timeable
     */
    @Override
    public void process() throws Exception {
        if(isTimeable())
            this.sw.start();
        processWithTimer();
        if(isTimeable())
            this.sw.stop();
        
        //TODO: replace this with proper logging code
        if(isTimeable())
            sw.printTimes();
    }

    /**
     * Override this method just like process() would have been overwritten
     */
    protected abstract void processWithTimer() throws Exception;
    
}
