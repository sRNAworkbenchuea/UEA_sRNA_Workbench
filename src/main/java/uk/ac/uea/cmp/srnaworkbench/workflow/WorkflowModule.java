/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.WorkflowThreadCompleteListener;

/**
 *
 * @author Chris Applegate and Matthew Stocks
 */
public abstract class WorkflowModule implements Runnable {

    // unique identifier for the workflow module
    //(to set in the concrete sub-class (otherwise defaults to DEFAULT_TOOL_NAME)
    private String id;
    private String title;
    // map of input data containers lists (key is unqiue identifier and value is list of data containers)
    private final Map<String, DataContainerList<?>> inputs;
    // map of output data containers lists  (key is unqiue identifier and value is list of data containers)
    private final Map<String, DataContainerList<?>> outputs;
    // flag describing the completion state of the workflow module
   // protected boolean completed;
    // log to output messages
    //protected final Log log;
    // node placement (x, y) coordinates for rendering
    private float nodeXPos;
    private float nodeYPos;

    protected String fxmlResource;
    protected ControlledScreen controller;

    private static final String DEFAULT_TOOL_NAME = "WORKFLOW_MODULE";

    // Error handling message
    private String error_message;

    // Runtime controls
    private WorkflowThreadCompleteListener listener;
    private JFXStatusTracker tracker;
    private volatile boolean continue_run;

    /*
     * Constructor
     * @param id: Unique identifier for the workflow module
     */
    protected WorkflowModule(final String id, final String title) {
        this(id, title, null);
    }

    /*
     * Constructor
     * @param id: Unique identifier for the workflow module
     */
    protected WorkflowModule(final String id, final String title, JFXStatusTracker tracker) {

        // initialise instance variables
        setID(id);
        this.title = title;
        this.inputs = new HashMap<>();
        this.outputs = new HashMap<>();
        // by default the module has not completed its execution

        //initialise the output log
      //  this.log = new Log();

        this.tracker = tracker;

        // initialise the node position on directed edge graph for rendering
        this.nodeXPos = 0.0f;
        this.nodeYPos = 0.0f;
        this.fxmlResource = "";
        this.controller = null;
    }

    public float getXPos() {
        return this.nodeXPos;
    }

    public float getYPos() {
        return this.nodeYPos;
    }

    public void setPos(float xPos, float yPos) {
        this.nodeXPos = xPos;
        this.nodeYPos = yPos;
    }

    public void setFXMLResource(String str) {
        this.fxmlResource = str;
    }

    public String getFXMLResource() {
        return this.fxmlResource;
    }

    /*
     * Performs execution of the workflow module
     */
    @Override
    public void run() {
        try {
            // Create and start a stop watch to time this tool's run method.
            StopWatch sw = new StopWatch();
            sw.start();

            // Log that the tool is starting.
            LOGGER.log(Level.INFO, "{0}: Starting.", id);

            // Let's make sure we've got as much memory as possible before starting.
            System.gc();

            // Call the client's process method.
            process();

            // If we got this far then we can say the tool finished the run successfully.
            //trackerFinished(true);
            // Stop the stop watch.
            sw.stop();
      //System.out.println( "time: " + sw.toString() );

            // Log that we completed successfully.
            LOGGER.log(Level.INFO, "{0}: Completed in: {1}.", new Object[]{
                id, sw.toString()
            });
            System.out.println("done " + this.id);
        } catch (Throwable e) {
            e.printStackTrace();

            String stackTrace = Tools.getStackTrace(e);
            String message = e.getMessage();

            // Log the exception with the stack trace for debugging.
            LOGGER.log(Level.SEVERE, "{0}: Message: {1};\nStack Trace: {2} ",
                    new Object[]{
                        id, message, stackTrace
                    });

            if (e instanceof OutOfMemoryError) {
                // Clean up and give the GC a fair chance of doing a decent job before
                // continuing.  If we try to do more stuff and there's no memory then the
                // JVM can fail in a bad way.
                System.gc();

                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    // Already handling an error condition, not sure why this would error but 
                    // if it does just make a mention in the logger and carry on.
                    LOGGER.log(Level.SEVERE, "{0}: Message: {1}",
                            new Object[]{
                                id, ex.getMessage(), stackTrace
                            });
                }

                // Reset error message to something more descriptive in this case
                message = "Out of Memory Error occured.\nWe advise "
                        + "increasing the amount of memory available to the JVM using the -Xmx "
                        + "argument or running smaller datasets through this machine.\nIf these "
                        + "options are not possible then you should use a machine with more "
                        + "memory available to run this job.\nWe also advise that you restart the "
                        + "UEA sRNA Workbench before running any subsequent jobs";

                // Log the Out of Memory message.
                LOGGER.log(Level.SEVERE, "{0}: {1}",
                        new Object[]{
                            id, message
                        });
            }

            // Set the stored error message to what the exception said
            setErrorMessage(message);

            // Tell the tracker that we're finished but 
//            trackerFinished(false);
        } finally {
            // Signal to any listener that the tool has finished its run.
            notifyListener();

            // Let's be a good citizen and tidy up after ourseleves
            //System.gc();
        }
    }

    /**
     * This is called by the run method. Start / Stop Logging is automatically
     * implemented for all RunnableTools. Client should put all of the code they
     * wish to be executed in worked thread here.
     *
     * @throws uk.ac.uea.cmp.srnaworkbench.workflow.WFModuleFailedException
     */
    protected abstract void process() throws Exception;
    
    public final void setID(String newID) {
        // remove non alpha numeric characters as these can't be used in HTML5 id attribute
        this.id = newID.replaceAll("[^a-zA-Z0-9]", "");
        if(!this.id.equals(newID))
        {
           LOGGER.log(Level.WARNING, "Workflow Module ID {0} contained non alpha numeric characters, these have been removed.", newID);
        }
    }
    
    /**
     * Retrieves the error message produced from the tool's run method if
     * present.
     *
     * @return The error message produced after the tool's run method failed, or
     * null if the tool has not yet been run or if the tool completed
     * successfully.
     */
    public String getErrorMessage() {
        return error_message;
    }

    /**
     * Sets the error message (to be used only after a tool has failed to
     * complete its run method)
     *
     * @param message The message describing the error condition.
     */
    protected void setErrorMessage(final String message) {
        this.error_message = message;
    }

    // ********* Listener handling routines ***********
    /**
     * Registers a ThreadCompleteListener object with this WorkflowModule after
     * initialisation time. The listener will be notified when the
     * RunnableTool's run method has completed (either successfully or
     * unsuccessfully).
     *
     * @param listener The listener object which should be in notified upon run
     * method completion.
     */
    public final void setListener(final WorkflowThreadCompleteListener listener) {
        this.listener = listener;
    }

    /**
     * Notifies the ThreadCompleteListener object (if registered) that the run
     * method has completed. This should only be called in one place in this
     * class.
     */
    private void notifyListener() {
        if (this.listener != null) {
            this.listener.notifyOfThreadCompletion(id);
        }
    }

    /*
     * Sets the completion state of the workflow module to true
     */
  /*  public final void complete() {
        this.completed = true;
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                controller.updateUI();
//            }
//        });

    }*/

    /*
     * Returns the completion state of the workflow module
     */
  /*  public final boolean isComplete() {
        return this.completed;
    }*/

    /*
     * Return the unique identifier of the workflow module
     */
    public final String getID() {
        return this.id;
    }

    public final String getTitle()
    {
        return this.title;
    }
    
    /*
     * Adds a specified data container list to the map of input data container lists
     * @param containerList: The data container list to add to the list of input data container lists
     */
    protected final <T> void addInputDataContainerList(DataContainerList<T> containerList) throws InitialisationException, DuplicateIDException {
        // if the input list has not been initialised
        if (this.inputs == null) {
            throw new InitialisationException("ERROR: input list of data container lists has not been initialised for Workflow Module.");
        }
        // ensure the data container is not null
        if (containerList == null) {
            String error = String.format("ERROR: Unable to add null input data container list to '%s'.", this.id);
            throw new NullPointerException(error);
        } // ensure that the workflow module does not already contain a data container list with the same unique identifier
        if (this.inputs.containsKey(containerList.getID())) {
            String error = String.format("ERROR: '%s' already input contains data container list with id '%s'.", this.id, containerList.getID());
            throw new DuplicateIDException(error);
        }
        // add the data container to the input data container list
        this.inputs.put(containerList.getID(), containerList);
    }

    /*
     * Adds a specified data container list to the map of output data container lists
     * @param containerList: The data container list to add to the list of output data container lists
     */
    protected final <T> void addOutputDataContainerList(DataContainerList<T> containerList) throws InitialisationException, DuplicateIDException {
        // if the output list has not been initialised
        if (this.outputs == null) {
            throw new InitialisationException("ERROR: output list of data container lists has not been initialised for Workflow Module.");
        }
        // ensure the data container is not null
        if (containerList == null) {
            String error = String.format("ERROR: Unable to add null output data container list to '%s'.", this.id);
            throw new NullPointerException(error);
        } // ensure that the workflow module does not already contain a data container list with the same unique identifier
        if (this.outputs.containsKey(containerList.getID())) {
            String error = String.format("ERROR: '%s' already output contains data container list with id '%s'.", this.id, containerList.getID());
            throw new DuplicateIDException(error);
        }
        // add the data container to the output data container list
        this.outputs.put(containerList.getID(), containerList);

    }

    /*
     * Returns input data container list with specified unique identifier
     * @param id: Unique identifier of data container list to return
     */
    public final DataContainerList<?> getInputDataContainerList(String id) throws InitialisationException, IDDoesNotExistException {
        // if the input list of data container lists has not been initialised
        if (this.inputs == null) {
            throw new InitialisationException("ERROR: input list of data container lists has not been initialised for Workflow Module.");
        }
        if (this.inputs.containsKey(id)) {
            return this.inputs.get(id);
        }
        String error = String.format("ERROR: Module '%s' does not contain input data container list with id '%s'.", this.id, id);
        throw new IDDoesNotExistException(error);
    }

    /*
     * Returns output data container list with sepcified unique identifier
     * @param id: Unique identifier of data container list to return
     */
    public final DataContainerList<?> getOutputDataContainerList(String id) throws InitialisationException, IDDoesNotExistException {
        // if the output list has not been initialised
        if (this.outputs == null) {
            throw new InitialisationException("ERROR: output list of data container lists has not been initialised for Workflow Module.");
        }
        if (this.outputs.containsKey(id)) {
            return this.outputs.get(id);
        }
        String error = String.format("ERROR: Module '%s' does not contain output data container list with id '%s'.", this.id, id);
        throw new IDDoesNotExistException(error);
    }

    /*
     * Returns whether there is an workflow is valid for execution (checks for mapping to real data in containers)
     */
    public final boolean hasValidInputConnections() throws InitialisationException {
        // if the  list has not been initialised
        if (this.inputs == null) {
            throw new InitialisationException("ERROR: input list of data contain lists has not been initialised for Workflow Module.");
        }

        // loop through all input data container lists
        for (String key : this.inputs.keySet()) {
            DataContainerList containerList = this.inputs.get(key);
            // checks the lists validity
            if (!containerList.isValid()) {
                return false;
            }
        }
        return true;

    }

    /* 
     * Returns the message from the log
     */
    //public final synchronized String getMessage() {
     //   return this.log.read();
    //}



    // public abstract ViewerGUI getViewer();
    void cancelRun() {
        continue_run = false;
    }

    boolean failed() {
        return this.error_message != null;
    }

    /**
     * Reset's the cancel run request.
     */
    public void resetRun() {
        continue_run = true;
    }

    /**
     * Checks if the continue run flag is set. If so an IOException is thrown.
     * This method should be called at points in the sub-classes process method
     * when it would be convenient to cancel the run.
     *
     * @throws IOException Thrown if the continue run flag has been set.
     */
    protected void continueRun() throws IOException {
        if (!continue_run) {
            throw new IOException("Run cancelled by user.");
        }
    }

    /**
     * Returns true if the user has requested that the tool's run method should
     * be cancelled.
     *
     * @return true if the continue run flag is set, otherwise false.
     */
    protected boolean cancelRequested() {
        return !this.continue_run;
    }

    // ********* Tracker handling routines ***********
    /**
     * Registers a status tracking object after initialisation. The tracker
     * object should be notified of progress during the RunnableTool's run
     * method.
     *
     * @param tracker The status tracking object to register.
     */
    public final void setTracker(final JFXStatusTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Retrieves the status tracking object if registered.
     *
     * @return The status tracking object or null if no status tracker is
     * registered.
     */
    protected final JFXStatusTracker getTracker() {
        return this.tracker;
    }

    /**
     * Should be used to indicate that the RunnableTool's run method has
     * completed, either successfully or unsuccessfully. Only to be called from
     * this class to simplify interface for client classes.
     *
     * @param success Set to true if the run method completed successfully, or
     * false if the run method failed.
     */
    private void trackerFinished(final boolean success) {
        if (this.tracker != null) {
            this.tracker.setFinished(success);
        }
    }

    /**
     * Used to notify the status tracker that the RunnableTool has initiated a
     * subprocess which has a known runtime.
     *
     * @param message The status message to output
     * @param length The length of the subprocess in arbitrary units.
     */
    protected void trackerInitKnownRuntime(final String message, final int length) {
        if (this.tracker != null) {
            this.tracker.initKnownRuntime(message, length);
        }
    }

    /**
     * Used to notify the status tracker that the RunnableTool has initiated a
     * subprocess which has an unknown runtime.
     *
     * @param message The status message to output
     */
    protected void trackerInitUnknownRuntime(final String message) {
        if (this.tracker != null) {
            this.tracker.initUnknownRuntime(message);
        }
    }

    /**
     * Returns the status tracker to its idle state.
     */
    protected void trackerReset() {
        if (this.tracker != null) {
            this.tracker.reset();
        }
    }

    /**
     * Increments the status tracker's progress bar during a known runtime
     * subprocess.
     */
    protected void trackerIncrement() {
        if (this.tracker != null) {
            this.tracker.increment();
        }
    }

    /**
     * Increments the status tracker's progress bar to declared value during a
     * known runtime subprocess.
     */
    protected void trackerIncrement(int value) {
        if (this.tracker != null) {
            this.tracker.increment(value);
        }
    }
    
    

}
