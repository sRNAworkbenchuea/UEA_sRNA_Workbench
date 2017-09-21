/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import java.io.IOException;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.WorkflowToolHost;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.utils.WorkflowThreadCompleteListener;

/**
 * Handles execution of a WorkflowModule from a GUI in another thread. Also
 * notifies the Workflow Manager of progress and result made during the
 * WorkflowModule's run/process method.
 *
 * @author Matt Stocks
 */
public abstract class WorkflowRunner implements WorkflowThreadCompleteListener, Comparable<WorkflowRunner> {

    protected WorkflowModule engine;
    protected Thread thread;
    protected WorkflowToolHost host;
    private boolean started;
    
    /**
     * Initialises the WorkflowRunner
     *
     * @param engine The WorkflowModule to start
     */
    protected WorkflowRunner(WorkflowModule engine) {
        this.host = WorkflowManager.getInstance();
        this.thread = null;
        this.engine = engine;
        this.started = false;

    }

    /**
     * Check to see if tool is currently running
     *
     * @return true if tool is running, otherwise false
     */
    public boolean getActive() {
        if (this.thread == null) {
            return false;
        }
        return this.thread.isAlive();
    }

    public boolean hasStarted()
    {
        return this.started;
    }
    
    /**
     * Clears the existing filter engine from memory
     */
    public void reset() {
        this.engine = null;
        this.thread = null;

        //TODO This call probably isn't required as we call GC in the RunnableTool now...
        System.gc();
    }

    /**
     * Instructs the tool to stop executing
     */
    public void cancel() {
        if (this.thread != null) {
            this.engine.cancelRun();
        }
    }

    /**
     * Called when engine completes run. If engine fails to complete
     * successfully a request is put out to the host to raise the issue with the
     * user. If it completes successfully the host is asked to update the GUI
     * with the results. In both cases the running status of the GUI is set to
     * false.
     *
     * @param id
     */
    @Override
    public void notifyOfThreadCompletion(String id) {
        if (this.engine.failed()) {
            this.cancel();
            this.host.showErrorDialog(this.engine.getErrorMessage());
        } else {
            this.host.update(id);
        }

        this.host.setRunningStatus(false);
    }

    public WorkflowModule getEngine() {
        return engine;
    }

    /**
     * Handles running of a tool for derived class. Asks host to set the GUI
     * into running (busy) mode. Then starts a new thread and kicks off the
     * engine in that thread. If an error occurs then the host is signaled to
     * raise an error message with the user and sets the running mode to false.
     *
     * @throws IOException Thrown if there were any problems starting the
     * RunnableTool
     */
    protected void run() throws IOException {
        try {
            this.host.setRunningStatus(true);

            this.engine.setListener(this);

            this.thread = new Thread(this.engine, "WorkflowModule " + this.engine.getTitle() + " ID:" + this.engine.getID());
            this.thread.start();
            this.started = true;
        } catch (Exception ex) {
            WorkbenchLogger.LOGGER.log(Level.SEVERE, null, ex);

            this.host.showErrorDialog(ex);
            this.host.setRunningStatus(false);
        }
    }

    @Override
    public int compareTo(WorkflowRunner o)
    {
        return this.engine.getID().compareTo(o.engine.getID());
    }

}
