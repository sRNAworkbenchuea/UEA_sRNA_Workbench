/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Pair;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NonOverwritableException;
import uk.ac.uea.cmp.srnaworkbench.tools.WorkflowToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.FX.FileReviewSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat2Params;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF.MiRCat2Module;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.ParesnipParams;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.RuleSet;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2DataInputWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

/**
 *
 * @author Chris Applegate & Matt Stocks This class handles the execution of a set of
 * connected workflow modules
 */
public final class WorkflowManager implements WorkflowToolHost {

    private String firstModule = "Database";

    
    

    // enum representing valid compatibility keys
    public static enum CompatibilityKey {

        GENOME, TRANSCRIPT_FILE, DEGRADOME_FILE, sRNA_FILE_QUERIES, sRNA_QUERY, INTERACTION_QUERY, PREDICTION_QUERY
    }
    // const definining -1 as unlimited input samples
    public static final int UNLIMITED_SAMPLES = -1;
    // const definining -1 as unlimited input replicates
    public static final int UNLIMITED_REPLICATES = -1;

    // stores whether this workflow requires a transcriptome
    public boolean transcriptomeOptional;
    // stores whether this workflow requires a degradome
    public boolean degradomeOptional;
    // stores the maximum number of samples for this workflow
    public int maxSamples;
    
    public int maxReplicates;

    // map of input data container lists (key is unqiue identifier and value is container list)
    private final Map<String, DataContainerList<?>> inputs;
    // graph storing the connection of workflow modules
    private final DirectedGraph<WorkflowRunner> graph;
    // list of graph nodes representing workflow modules awaiting execution
    private final List<DirectedGraphNode<WorkflowRunner>> waitingNodes;
    // list of graph nodes representing workflow modules that are currently in execution
    //private final List<DirectedGraphNode<WorkflowRunner>> activeNodes;
    // is the manager running
    private boolean isRunning;
    // static instance of the workflow manager
    private static WorkflowManager instance = null;
    // name of workflow
    private String name;

    /*
     * private default contructor 
     * note: ensures that developers can't create multiple managers
     */
    private WorkflowManager() {
        this.inputs = new HashMap<>();
        this.graph = new DirectedGraph<>();
        this.waitingNodes = new LinkedList();
//        this.activeNodes = new LinkedList<>();
        this.isRunning = false;
        this.transcriptomeOptional = true;
        this.degradomeOptional = true;
        this.maxSamples = UNLIMITED_SAMPLES;
        this.maxReplicates = UNLIMITED_REPLICATES;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /*
     * returns the only instance of the workflow manager
     * note: function creates a new workflow manager if one does not already exist
     */
    public static WorkflowManager getInstance() {
        if (instance == null) {
            instance = new WorkflowManager();
        }
        return instance;
    }

    // deletes a workflow and returns the workflow manager to initial state
    public void reset() {
        if (!this.isRunning) {
            this.inputs.clear();
            this.graph.reset();
            this.waitingNodes.clear();
            this.name = "";
        }
    }

    /*
     * Adds a workflow module to the manager
     * @param module: Workflow data container to add to the manager
     */
    public void addModule(WorkflowRunner module) {
        try {
            // add the module to the graph
            graph.addNode(module.getEngine().getID(), module);
            // does this module have an fxml viewer? If it is running in command line mode it might not want one...
            if (!AppUtils.INSTANCE.isCommandLine())
            {
                String fxml = module.getEngine().getFXMLResource();
                ControlledScreen controller = module.getEngine().controller;
                /*if(!fxml.isEmpty())
                 {
                 ScreensController.getInstance().loadScreen(module.getEngine().getID(), fxml);
                 System.out.println("added fxml: " + fxml);
                 }
                 */
                if (controller != null && fxml != null)
                {
                    ScreensController.getInstance().loadScreen(module.getEngine().getID(), fxml, controller);
                }
                else
                {
                    LOGGER.log(Level.WARNING, "could not create view for {0}", module.getEngine().getID());
                }
            }
        }
        catch (DuplicateIDException | InitialisationException ex) {
            // FIXME: Not sure if this is recoverable, so leaving it as RuntimeException for now. If it is recoverable then maybe it can
            // be thrown up again
            throw new RuntimeException("ERROR: Could not add module (" + module.getEngine().getID() + ") to workflow: " + ex);
        }
    }
    
    public void setFirstModuleTitle(String newModuleTitle)
    {
        this.firstModule = newModuleTitle;
    }


    /*
     * Creates a data container of the specified compatibility key and data and adds it to the list of data containers specified by the inputID
     * @param inputID: Unique identifier to assign to data container list
     * @param compatibilityKey: Key used to determine compatibility when linking data containers
     * @param data: The data to insert into the data container
     */
    public <T> void addInputData(String inputID, CompatibilityKey compatibilityKey, T data) {
        // create a new data container with the specified compatibility key and data
        DataContainer<T> dataContainer = new DataContainer<>(compatibilityKey, data);
        // add the data container to the list with the specified inputID
        addInputDataContainer(inputID, dataContainer);
    }

    /*
     * Creates a data container of the specified compatibility key and data and adds it to the list of data containers specified by the inputID
     * @param inputID: Unique identifier to assign to data container list
     * @param compatibilityKey: Key used to determine compatibility when linking data containers
     * @param data: The data to insert into the data container
     */
    public DataContainerList<?> getInputData(String containerName) {
        return inputs.get(containerName);
    }

    /*
     * Adds the specified data container to the manager's data container list associated with the inputID
     * @param inputID: Unique identifier to assign to data container list
     * @param dataContainer: The data container to insert into the data container list
     */
    private void addInputDataContainer(String inputID, DataContainer dataContainer) {
        if (dataContainer == null) {
            System.err.println("ERROR: Attempting to add null data container to data container list '" + inputID + "'.");
            return;
        }
        if (inputs == null) {
            System.err.println("ERROR: Input data container list for manager not initialised.");
            return;
        }
        if (!inputs.containsKey(inputID)) {
            System.err.println("ERROR: Manager does not contain data container list '" + inputID + "'.");
            return;
        }
        if (inputs.get(inputID).getCompatibilityKey() != dataContainer.getCompatibiltyKey()) {
            System.err.println("ERROR: Compatibility keys not compatible.");
            return;
        }
        try {
            inputs.get(inputID).add(dataContainer);
        } catch (MaximumCapacityException | CompatibilityException ex) {
            LOGGER.log(Level.SEVERE, "", ex);
        }
    }

    /*
     * Creates an input data container list with the specified properties
     * @param inputID: Unique identifier to assign to data container list
     * @param compatibilityKey: The compatibility key for the list
     * @param minLength: the minimum length of the list of data containers
     * @param maxLength: the maximum length of the list of data containers (-1 indicates infinite length list)
     */
    public void addInputDataContainerList(String inputID, CompatibilityKey compatibilityKey, int minLength, int maxLength) {
        if (minLength < 0) {
            System.err.println("ERROR: Minimum length of data container list cannot be less than 0.");
            return;
        }
        if (maxLength != -1 && maxLength < minLength) {
            System.err.println("ERROR: Maximum length of data container list must be greater or equal to the minimum length.");
            return;
        }
        if (inputs == null) {
            System.err.println("ERROR: Input data container list for manager not initialised.");
            return;
        }
        if (inputs.containsKey(inputID)) {
            System.err.println("ERROR: Input data container list with ID '" + inputID + "' already exists.");
            return;
        }
        inputs.put(inputID, new DataContainerList<>(inputID, compatibilityKey, minLength, maxLength));
    }

    /*
     * Connects the manager input data container at the specified list and index to an input data container list belonging to a workflow module
     * @param inputID: Unique identifier of the manager input data container list to connectDB2Module from
     * @param inputIndex: The index of the data container within the specifie manager input list
     * @param moduleID: Unique identifier of the workflow module that contains the input data container to connectDB2Module to
     * @param dataContainerID: Unique identifier of the input data container held by the specified workflow module to connectDB2Module to
     */
    public void connectDB2Module(String inputID, int inputIndex, String moduleID, String dataContainerListID) {
        try {
            DirectedGraphNode<WorkflowRunner> node = graph.getNode(moduleID);
            WorkflowRunner module = node.getElement();
            DataContainerList containerList = module.getEngine().getInputDataContainerList(dataContainerListID);
            containerList.add(inputs.get(inputID).getContainer(inputIndex));
        } catch (InitialisationException | IDDoesNotExistException | MaximumCapacityException | CompatibilityException ex) {
            String error = String.format("ERROR: Could not connect %s to %s %s.", inputID, moduleID, dataContainerListID);
            LOGGER.log(Level.SEVERE, error, ex);
        }
    }

    /*
     * Connects all of the manager input data containers in the specified list to an input data container list belonging to a workflow module
     * @param inputID: Unique identifier of the manager input data container list to connectDB2Module from
     * @param moduleID: Unique identifier of the workflow module that contains the input data container to connectDB2Module to
     * @param dataContainerID: Unique identifier of the input data container held by the specified workflow module to connectDB2Module to
     */
    public void connectDB2Module(String inputID, String moduleID, String dataContainerListID) {
        try {
            DirectedGraphNode<WorkflowRunner> node = graph.getNode(moduleID);
            WorkflowRunner module = node.getElement();
            DataContainerList containerList = module.getEngine().getInputDataContainerList(dataContainerListID);
            for (int i = 0; i < inputs.get(inputID).getListLength(); i++) {
                containerList.add(inputs.get(inputID).getContainer(i));
            }
        } catch (InitialisationException | IDDoesNotExistException | MaximumCapacityException | CompatibilityException ex) {
            String error = String.format("ERROR: Could not connect %s to %s %s.", inputID, moduleID, dataContainerListID);
            LOGGER.log(Level.SEVERE, error, ex);
        }
    }

    /*
     * Connects an output data container of a workflow module to an input data container of another workflow module
     * @param moduleA_ID: Unique identifier of the workflow module that contains the output data container to connectDB2Module from
     * @param dataContainerA_ID: Unique identifier of the output data container held by the specified output workflow module to connectDB2Module from
     * @param inputA: The index of the data container within the specified output module container list
     * @param module_ID: Unique identifier of the workflow module that contains the input data container to connectDB2Module to
     * @param dataContainerB_ID: Unique identifier of the input data container held by the specified input workflow module to connectDB2Module to
     * @param indexB: The index of the data container within the specified input module container list
     */
    public void connectModule2Module(String moduleA_ID, String dataContainerListA_ID, int indexA, String moduleB_ID, String dataContainerListB_ID, int indexB) {
        try {
            DirectedGraphNode<WorkflowRunner> nodeA = graph.getNode(moduleA_ID);
            DirectedGraphNode<WorkflowRunner> nodeB = graph.getNode(moduleB_ID);
            WorkflowRunner moduleA = nodeA.getElement();
            WorkflowRunner moduleB = nodeB.getElement();
            DataContainerList containerListA = moduleA.getEngine().getOutputDataContainerList(dataContainerListA_ID);
            DataContainerList containerListB = moduleB.getEngine().getInputDataContainerList(dataContainerListB_ID);

            containerListB.getContainer(indexB).setData(containerListA.getContainer(indexA));
            nodeA.addOutConnection(nodeB);
            nodeB.addInConnection(nodeA);
        } catch (InitialisationException | IDDoesNotExistException | CompatibilityException | NonOverwritableException | DuplicateIDException ex) {
            String error = String.format("ERROR: Could not connect %s %s to %s %s.", moduleA_ID, dataContainerListA_ID, moduleB_ID, dataContainerListB_ID);
            LOGGER.log(Level.SEVERE, error, ex);
        }
    }

    /*
     * Connects all of the specified output module data containers within the specified input list to input module data container list belonging to a workflow module
     * @param inputID: Unique identifier of the manager input data container list to connectDB2Module from
     * @param moduleID: Unique identifier of the workflow module that contains the input data container to connectDB2Module to
     * @param dataContainerID: Unique identifier of the input data container held by the specified workflow module to connectDB2Module to
     */
    public void connectModule2Module(String moduleA_ID, String dataContainerListA_ID, String moduleB_ID, String dataContainerListB_ID) {
        try {
            DirectedGraphNode<WorkflowRunner> nodeA = graph.getNode(moduleA_ID);
            DirectedGraphNode<WorkflowRunner> nodeB = graph.getNode(moduleB_ID);
            WorkflowRunner moduleA = nodeA.getElement();
            WorkflowRunner moduleB = nodeB.getElement();
            DataContainerList containerListA = moduleA.getEngine().getOutputDataContainerList(dataContainerListA_ID);
            DataContainerList containerListB = moduleB.getEngine().getInputDataContainerList(dataContainerListB_ID);

            for (int i = 0; i < containerListA.getListLength(); i++) {
                containerListB.add(containerListA.getContainer(i));
            }

            nodeA.addOutConnection(nodeB);
            nodeB.addInConnection(nodeA);
        } catch (InitialisationException | IDDoesNotExistException | MaximumCapacityException | CompatibilityException | DuplicateIDException ex) {
            String error = String.format("ERROR: Could not connect %s %s to %s %s.", moduleA_ID, dataContainerListA_ID, moduleB_ID, dataContainerListB_ID);
            LOGGER.log(Level.SEVERE, error, ex);
        }
    }

    /*

     */
    public void connectModule2Module(String moduleFrom_ID, String moduleTo_ID) {
        try {
            DirectedGraphNode<WorkflowRunner> nodeA = graph.getNode(moduleFrom_ID);
            DirectedGraphNode<WorkflowRunner> nodeB = graph.getNode(moduleTo_ID);

            nodeA.addOutConnection(nodeB);
            nodeB.addInConnection(nodeA);
        } catch (InitialisationException | IDDoesNotExistException | DuplicateIDException ex) {
            String error = String.format("ERROR: Could not connect %s to %s.", moduleFrom_ID, moduleTo_ID);
//            System.err.println(error);
//            System.err.println(ex);
            throw new WorkflowException(error, ex);
            //LOGGER.log(Level.SEVERE, "ERROR: Could not connect: {0} To: {1}", new Object[]{moduleFrom_ID, moduleTo_ID});
        }
    }

    /*
     * Returns whether the workflow is valid and ready for execution
     */
    private boolean isValidWorkflow() {
        try {
            // loop through all of the nodes and check that all inputs map to data
            for (DirectedGraphNode<WorkflowRunner> node : graph.getAllNodes()) {
                WorkflowRunner module = node.getElement();
                if (!module.getEngine().hasValidInputConnections()) {
                    LOGGER.log(Level.SEVERE, "module " + module.getEngine().getID() + " has invalid input connections");
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "", ex);
            return false;
        }
    }

    /*
     * Performs execution of the workflow if valid
     */
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        //Modify this as well as the WorkflowController                       
                        
                       
                        if (isValidWorkflow() && (DatabaseWorkflowModule.getInstance().checkReadyToBuild() || (!isUsingDB() && !isUsingFileManager()))) {
                            //System.out.println("INFORMATION: Workflow started.");
                            // put all graph nodes containing workflow modules on the waiting list
                            waitingNodes.addAll(graph.getAllNodes());

                            
                            // lock all the nodes
                            for (DirectedGraphNode<WorkflowRunner> node : graph.getAllNodes()) {
                                if(!AppUtils.INSTANCE.isCommandLine() && node.getElement().engine.controller != null)
                                    node.getElement().engine.controller.workflowStartedListener();
                            }

                            //retrieve the first node (default database)...
                            DirectedGraphNode<WorkflowRunner> database_node = graph.getNode(firstModule);
                            WorkflowSceneController.setBusyNode(firstModule);
                            //run this first, every time, exception is thrown if it does not exist
                            database_node.getElement().run();

                        } else {
                            LOGGER.log(Level.WARNING, "Workflow is not valid. Workflow aborted.{0} | {1}", new Object[]{isValidWorkflow(), DatabaseWorkflowModule.getInstance().checkReadyToBuild()});
                        }
                    } catch (InitialisationException | IDDoesNotExistException | IOException ex) {
                        LOGGER.log(Level.SEVERE, "", ex);

                    }
                }

                
            };
            thread.start();
            if(AppUtils.INSTANCE.isCommandLine())
            {
                try
                {
                    wait();
                    System.out.println("Finish");
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(WorkflowManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "Workflow Manager is already running!");

        }
    }

    public WorkflowModule getModule(String id) throws Exception {
        return this.graph.getNode(id).getElement().getEngine();
    }

    public List<WorkflowRunner> getModuleOfType(Class className) throws InitialisationException, Exception {
        List<WorkflowRunner> runners = getAllModules();
        List<WorkflowRunner> r = new LinkedList<>();
        for (WorkflowRunner runner : runners) {
            if (runner.getEngine().getClass() == className) {
                r.add(runner);
            }
        }
        return r;
    }

    /* 
     * returns a list of all of the workflow modules that have been added to the manager
     */
    public List<WorkflowRunner> getAllModules() throws Exception {
        List<WorkflowRunner> modules = new LinkedList<>();
        List<DirectedGraphNode<WorkflowRunner>> nodes = graph.getAllNodes();
        for (DirectedGraphNode<WorkflowRunner> node : nodes) {
            modules.add(node.getElement());
        }
        return modules;
    }
    
    public boolean isUsingDB()
    {
        return containsID("Database");
    }
    
    public boolean isUsingFileManager()
    {
        return containsID("FileManager");
    }
    
    /**
     * Searches the graphs for the given ID
     * @param ID
     * @return 
     */
    public boolean containsID(String ID)
    {
        try
        {
            graph.getNode(ID);//if an exception is thrown the graph list is either empty or the ID does not exist
            return true;
        }
        catch (InitialisationException | IDDoesNotExistException ex)
        {
            return false;
        }
    }

    /*
     * returns whether the workflow manager is currently executing a workflow
     */
    public boolean isRunning() {
        return isRunning;
    }

    //triggers when any thread (in this case, the modules) finish
    //from here, we need to determine which workflow module has completed and if the next module can begin
    @Override
    public synchronized void update(String ID) {
        System.out.println("Something has finished... : " + ID);
        WorkflowSceneController.addToConsole("Workflow Node has finished... : " + ID);
        WorkflowSceneController.setCompleteNode(ID);
        DatabaseWorkflowModule.getInstance().printTotal();
        try {
            DirectedGraphNode<WorkflowRunner> completed_node = this.graph.getNode(ID);
            System.out.println("checking: " + completed_node.getID() + " connections");
            WorkflowSceneController.addToConsole("checking: " + completed_node.getID() + " connections");
            List<DirectedGraphNode<WorkflowRunner>> outConnections = completed_node.getOutConnections();
            for (DirectedGraphNode<WorkflowRunner> connection : outConnections) {
                //for each connected node see if its dependent connections are completed
                if (!hasActiveDependancies(connection.getID(), completed_node.getID())) {
                    WorkflowSceneController.setBusyNode(connection.getID());
                    connection.getElement().run();
                }

            }
            this.waitingNodes.remove(completed_node);
        } catch (Exception ex) {
            Logger.getLogger(WorkflowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //now check if all nodes are complete

        for (DirectedGraphNode<WorkflowRunner> node : waitingNodes) {
            System.out.println("node: " + node.getID() + " is active: " + node.getElement().getActive());
            WorkflowSceneController.addToConsole("node: " + node.getID() + " is active: " + node.getElement().getActive());
        }
        if (waitingNodes.isEmpty()) {
            System.out.println("Workflow complete");
            if (!AppUtils.INSTANCE.isCommandLine())
            {
                //if (DatabaseWorkflowModule.getInstance().isDebugMode()) {
                Platform.runLater(() ->
                {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Workbench Information");
                    alert.setHeaderText("Workflow Status Message");
                    alert.setContentText("The workflow has completed, press to continue");

                    alert.showAndWait();
                });
            }
            else
            {
                this.notifyAll();
            }
            //}
        }
        //print sequences as test
        // DatabaseWorkflowModule.getInstance().showSequences();
        //System.out.println("finished update method");
    }

    public boolean hasActiveDependancies(String moduleID, String completedParentID) throws Exception {
        DirectedGraphNode<WorkflowRunner> potential_run_node = this.graph.getNode(moduleID);
        List<DirectedGraphNode<WorkflowRunner>> inConnections = potential_run_node.getInConnections();
        for (DirectedGraphNode<WorkflowRunner> inConnection : inConnections) {
            if (!inConnection.getID().equals(completedParentID)) {
                //System.out.println("Out connection ID: " + moduleID + " in connection: " + inConnection.getElement().getEngine().id + " active status: " + inConnection.getElement().getActive());
                WorkflowSceneController.addToConsole("Out connection ID: " + moduleID + " in connection: " + inConnection.getElement().getEngine().getID() + " active status: " + inConnection.getElement().getActive());
                if (inConnection.getElement().getActive() || !inConnection.getElement().hasStarted()) {
                    return true;
                }
            }
//            while (inConnection.getElement().getActive())
//            {
//                System.out.println("active");
//            }
        }
        return false;
    }

    //perhaps remove this, (in which case a new interface will be required)
    @Override
    public void setRunningStatus(boolean running) {
        isRunning = running;
    }

    //generic error dialog that can be triggered from any workflow module
    @Override
    public void showErrorDialog(String message)
    {
        // set back to unbusy?

        if (!AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("An Error has occured");
                alert.setContentText(message);
                
                alert.showAndWait();
            });
        }
        else
        {
            LOGGER.log(Level.SEVERE, "An Error has occured{0}", message);
        }
    }
    
    @Override
    public void showErrorDialog(Exception ex) {
        // set back to unbusy?

        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Workbench Error");
            alert.setHeaderText("An Error has occured");
            alert.setContentText(ex.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
            
            alert.showAndWait();
        });
    }

    public int getNumContainersInDBList(String listID) {
        return this.inputs.get(listID).getListLength();
    }

    private JsonObject generateJSON() throws Exception {

        int counter = 0;
        //  Map<String, Integer> idMap = new HashMap<>();
        // write nodes
        JsonArrayBuilder nodeArray = Json.createArrayBuilder();
        for (DirectedGraphNode<WorkflowRunner> node : this.graph.getAllNodes()) {
            JsonObjectBuilder jsonNode = Json.createObjectBuilder();
            jsonNode.add("id", node.getElement().getEngine().getID());
            jsonNode.add("title", node.getElement().getEngine().getTitle());
            jsonNode.add("x", node.getElement().getEngine().getXPos());
            jsonNode.add("y", node.getElement().getEngine().getYPos());
            nodeArray.add(jsonNode);
            // idMap.put(node.getID(), counter++);
        }
        // write edges
        JsonArrayBuilder edgeArray = Json.createArrayBuilder();
        for (DirectedGraphNode<WorkflowRunner> node : this.graph.getAllNodes()) {
            String sourceID = node.getID();//idMap.get(node.getID());
            List<DirectedGraphNode<WorkflowRunner>> outConnections = node.getOutConnections();
            for (DirectedGraphNode<WorkflowRunner> outConnection : outConnections) {
                String targetID = outConnection.getID();
                JsonObjectBuilder jsonEdge = Json.createObjectBuilder();
                jsonEdge.add("source", sourceID);

                jsonEdge.add("target", targetID);

                edgeArray.add(jsonEdge);
            }
        }
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("nodes", nodeArray);
        json.add("edges", edgeArray);
        return json.build();
    }

    public List<ParesnipParams> getPAREsnipParameters(JsonObject jsonObject) throws Exception {
        // get parameters
        List<ParesnipParams> paramList = new ArrayList<>();
        JsonArray paresnipParamsArray = jsonObject.getJsonArray("paresnip_parameters");
        for (int i = 0; i < paresnipParamsArray.size(); i++) {
            ParesnipParams params = new ParesnipParams();
            JsonObject paresnipParamsObject = paresnipParamsArray.getJsonObject(i);
            params.setAllowAdjacentMismatches(paresnipParamsObject.getBoolean(ParesnipParams.Definition.ALLOW_ADJACENT_MISMATCHES.getName(), params.isAllowAdjacentMismatches()));
            params.setAllowMismatchAtPositionEleven(paresnipParamsObject.getBoolean(ParesnipParams.Definition.ALLOW_MISMATCH_POSITION_11.getName(), params.isAllowMismatchAtPositionEleven()));
            params.setAllowSingleNtGap(paresnipParamsObject.getBoolean(ParesnipParams.Definition.ALLOW_SINGLE_NT_GAP.getName(), params.isAllowSingleNtGap()));
            params.setAutoOutputTplotPdf(paresnipParamsObject.getBoolean(ParesnipParams.Definition.AUTO_OUTPUT_TPLOT_PDF.getName(), params.getAutoOutputTplotPdf()));
            params.setCalculatePvalues(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CALCULATE_PVALUES.getName(), params.isCalculatePvalues()));
            params.setCategory0(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CATEGORY_0.getName(), params.isCategory0()));
            params.setCategory1(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CATEGORY_1.getName(), params.isCategory1()));
            params.setCategory2(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CATEGORY_2.getName(), params.isCategory2()));
            params.setCategory3(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CATEGORY_3.getName(), params.isCategory3()));
            params.setCategory4(paresnipParamsObject.getBoolean(ParesnipParams.Definition.CATEGORY_4.getName(), params.isCategory4()));
            params.setDiscardLowComplexityCandidates(paresnipParamsObject.getBoolean(ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_CANDIDATES.getName(), params.isDiscardLowComplexityCandidates()));
            params.setDiscardLowComplexitySRNAs(paresnipParamsObject.getBoolean(ParesnipParams.Definition.DISCARD_LOW_COMPLEXITY_SRNAS.getName(), params.isDiscardLowComplexitySRNAs()));
            params.setDiscardTrrna(paresnipParamsObject.getBoolean(ParesnipParams.Definition.DISCARD_TRRNA.getName(), params.isDiscardTrrna()));
            params.setNotIncludePvalueGrtCutoff(paresnipParamsObject.getBoolean(ParesnipParams.Definition.DO_NOT_INCLUDE_IF_GREATER_THAN_CUTOFF.getName(), params.isNotIncludePvalueGrtCutoff()));
            params.setMaxFragmentLength(paresnipParamsObject.getInt(ParesnipParams.Definition.MAXIMUM_FRAGMENT_LENGTH.getName(), params.getMaxFragmentLength()));
            JsonNumber maxMismatches = paresnipParamsObject.getJsonNumber(ParesnipParams.Definition.MAXIMUM_MISMATCHES.getName());
            if (maxMismatches != null) {
                params.setMaxMismatches(maxMismatches.doubleValue());
            }
            params.setMaxSrnaLength(paresnipParamsObject.getInt(ParesnipParams.Definition.MAXIMUM_SRNA_LENGTH.getName(), params.getMaxSrnaLength()));
            params.setMinFragmentLength(paresnipParamsObject.getInt(ParesnipParams.Definition.MINIMUM_FRAGMENT_LENGTH.getName(), params.getMinFragmentLength()));
            params.setMinSrnaAbundance(paresnipParamsObject.getInt(ParesnipParams.Definition.MINIMUM_SRNA_ABUNDANCE.getName(), params.getMinSrnaAbundance()));
            params.setMinSrnaLength(paresnipParamsObject.getInt(ParesnipParams.Definition.MINIMUM_SRNA_LENGTH.getName(), params.getMinSrnaLength()));
            params.setShuffleCount(paresnipParamsObject.getInt(ParesnipParams.Definition.NUMBER_OF_SHUFFLES.getName(), params.getShuffleCount()));
            params.setSecondaryOutputToFile(paresnipParamsObject.getBoolean(ParesnipParams.Definition.OUTPUT_SECONDARY_HITS_TO_FILE.getName(), params.isSecondaryOutputToFile()));
            JsonNumber pValCutOff = paresnipParamsObject.getJsonNumber(ParesnipParams.Definition.PVALUE_CUTOFF.getName());
            if (pValCutOff != null) {
                params.setPvalueCutoff(pValCutOff.doubleValue());
            }
            params.setSubsequenceSecondaryHit(paresnipParamsObject.getBoolean(ParesnipParams.Definition.SUBSEQUENCES_ARE_SECONDARY_HITS.getName(), params.isSubsequenceSecondaryHit()));
            params.setIsWeightedFragmentAbundance(paresnipParamsObject.getBoolean(ParesnipParams.Definition.USE_WEIGHTED_FRAGMENT_ABUNDANCE.getName(), params.isWeightedFragmentAbundance()));

            paramList.add(params);
        }
        return paramList;
    }

    public MiRCatParams getMiRCatParameters(JsonObject jsonObject) {

        MiRCatParams params = MiRCatParams.createDefaultPlantParams();
        JsonArray mircatParamsArray = jsonObject.getJsonArray("mircat_parameters");
        JsonObject mircatParamsObject = mircatParamsArray.getJsonObject(0);
        // complex loops
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.ALLOW_COMPLEX_LOOPS.getDefinition().getName())) {
            params.setComplexLoops(mircatParamsObject.getBoolean(MiRCatParams.Definition.ALLOW_COMPLEX_LOOPS.getDefinition().getName()));
        }
        // minimum cluster sep distance
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefinition().getName())) {
            params.setClusterSentinel(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefinition().getName()));
        }
        // extend
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getDefinition().getName())) {
            params.setExtend(mircatParamsObject.getInt(MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getDefinition().getName()));
        }
        // max gaps
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getDefinition().getName())) {
            params.setMaxGaps(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getDefinition().getName()));
        }
        // max genome hits
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getDefinition().getName())) {
            params.setMaxGenomeHits(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getDefinition().getName()));
        }
        // max overlap %
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefinition().getName())) {
            params.setMaxOverlapPercentage(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefinition().getName()));
        }
        // max unpaired
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefinition().getName())) {
            params.setMaxUnpaired(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefinition().getName()));
        }
        // srna abundance
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefinition().getName())) {
            params.setMinConsider(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefinition().getName()));
        }
        // mfe
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MFE_THRESHOLD.getDefinition().getName())) {
            params.setMinEnergy((float) mircatParamsObject.getJsonNumber(MiRCatParams.Definition.MFE_THRESHOLD.getDefinition().getName()).doubleValue());
        }
        // min gc
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getDefinition().getName())) {
            params.setMinGC(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getDefinition().getName()));
        }
        // min hairpin length
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getDefinition().getName())) {
            params.setMinHairpinLength(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getDefinition().getName()));
        }
        // min locus size
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getDefinition().getName())) {
            params.setMinLocusSize(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getDefinition().getName()));
        }
        // min paired
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getDefinition().getName())) {
            params.setMinPaired(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getDefinition().getName()));
        }
        // min orientation
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefinition().getName())) {
            params.setOrientation(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefinition().getName()));
        }
        // p-val
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.P_VAL_THRESHOLD.getDefinition().getName())) {
            params.setPVal((float) mircatParamsObject.getJsonNumber(MiRCatParams.Definition.P_VAL_THRESHOLD.getDefinition().getName()).doubleValue());
        }
        // min length
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getDefinition().getName())) {
            params.setLengthRange(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getDefinition().getName()), params.getMaxLength());
        }
        // max length
        if (mircatParamsObject.containsKey(MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getDefinition().getName())) {
            params.setLengthRange(params.getMinLength(), mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getDefinition().getName()));
        }
        return params;
    }
    
    public void configureMiRCat2(JsonObject jsonObject) throws IOException
    {
        if (jsonObject.containsKey("miRCAT2_params"))
        {
            Path mc_paramsPath = Paths.get(jsonObject.getString("miRCAT2_params"));
            if (Files.exists(mc_paramsPath))
            {
                MiRCat2Module.readParams(mc_paramsPath.toString());
                LOGGER.log(Level.INFO, "mircat 2 params loaded");
            }
            else
            {
                throw new IOException("Params file: " + mc_paramsPath + " not found.");
            }
        }
        else
        {
            //throw new JSONParserException("A genome file is required.");
            throw new IOException("A miRCat 2 parameter file is required.");

        }
        if (jsonObject.containsKey("miRCAT2_Output_Dir"))
        {
            Path mc_outPath = Paths.get(jsonObject.getString("miRCAT2_Output_Dir"));
            if (Files.exists(mc_outPath))
            {
                MiRCat2Module.setOutputDir(mc_outPath);
                LOGGER.log(Level.INFO, "mircat 2 out path detected: {0}", mc_outPath);
            }
        }
        else
        {
            throw new IOException("A miRCat 2 output directory is required.");

        }
    }

    
    // twh14ura
    // setup PAREsnip2 and miRCat2 parameters when running PAREfirst commandline
    public void getPAREfirstParameters(JsonObject jsonObject) throws IOException, Exception{
        
        if (jsonObject.containsKey("mircat2_parameters")){
            Path mc_paramsPath = Paths.get(jsonObject.getString("mircat2_parameters"));
            if (Files.exists(mc_paramsPath)){
                MiRCat2Module.readParams(mc_paramsPath.toString());
                LOGGER.log(Level.INFO, "mircat 2 params loaded");
            }
            else{
                throw new IOException("Params file: " + mc_paramsPath + " not found.");
            }
        }
        else{
            throw new IOException("A miRCat 2 parameter file is required.");
        }
        
        if (!jsonObject.containsKey("paresnip2_parameters")){
                System.out.println("Parameter file has not been specified... Using default stringent configuration and transcriptome as reference...");
                Paresnip2Configuration.getInstance().setDefaultStringentParameters();
        } else {
            Path path = Paths.get(jsonObject.getString("paresnip2_parameters"));
            if (Files.exists(path)){
                Paresnip2Configuration.getInstance().loadConfig(new File(path.toString()));
            }else{
                System.out.println("Parameter file has not been specified... Using default stringent configuration and transcriptome as reference...");
                Paresnip2Configuration.getInstance().setDefaultStringentParameters();
            }
        }

          if (!jsonObject.containsKey("paresnip2_rules")) {
            System.out.println("Targeting rules file has not been specified... Using default Allen et al rules...");
            RuleSet.getRuleSet().setDefaultAllen();
        } else {
            Path path = Paths.get(jsonObject.getString("paresnip2_rules"));
            if (Files.exists(path)) {
                RuleSet.getRuleSet().loadRules(new File(path.toString()));
            }else{
                System.out.println("Targeting rules file has not been specified... Using default Allen et al rules...");
                RuleSet.getRuleSet().setDefaultAllen();
            }
        }
    }
    
    //public void setUpDatabase(JsonObject jsonObject) throws IOException, JSONParserException {
    public void setUpDatabase(JsonObject jsonObject) throws IOException {

        // get srna_files
        HashMap<String, List<Path>> srna_samples = new HashMap<>();
        List<Path> srna_filenames_paths = new ArrayList<>();
        List<String> srna_filesToInclude = new ArrayList<>();
        if (jsonObject.containsKey("srna_files")) {
            JsonArray srna_files = jsonObject.getJsonArray("srna_files");
            for (int i = 0; i < srna_files.size(); i++) {
                JsonObject srna_file_obj = srna_files.getJsonObject(i);
                if (srna_file_obj.containsKey("srna_filename")) {
                    String srna_filename = srna_file_obj.getString("srna_filename");
                    if (Paths.get(srna_filename).toFile().exists()) {
                        srna_filenames_paths.add(Paths.get(srna_filename));
                        srna_filesToInclude.add(Paths.get(srna_filename).getFileName().toString());
                        String message = "Added sRNA file: " + srna_filename;
                        System.out.println(message);
                        LOGGER.log(Level.INFO, message);
                    } else {
                        throw new IOException("sRNA file: " + srna_filename + " not found.");
                    }
                }
            }
        }

        // ensure at least one sRNA file has been added
        if (srna_filenames_paths.isEmpty()) {
            //throw new JSONParserException("At least one sRNA file is required.");
            throw new IOException("At least one sRNA file is required.");
        }
        srna_samples.put("sample_0", srna_filenames_paths);
        HTMLWizardViewController.set_sRNA_samples(srna_samples);
        FileReviewSceneController.setSelectedFiles(srna_filesToInclude);

        // get degradome_files samples
        HashMap<String, List<Pair<Path, Integer>>> degradome_samples = new HashMap<>();
        List<Pair<Path, Integer>> degradome_filenames_paths = new ArrayList<>();
        if (jsonObject.containsKey("degradome_files")) {
            JsonArray degradome_files = jsonObject.getJsonArray("degradome_files");
            for (int i = 0; i < degradome_files.size(); i++) {
                JsonObject degradome_file_obj = degradome_files.getJsonObject(i);
                if (degradome_file_obj.containsKey("degradome_filename")) {
                    String degradome_filename = degradome_file_obj.getString("degradome_filename");
                    if (Paths.get(degradome_filename).toFile().exists()) {
                        degradome_filenames_paths.add(new Pair<>(Paths.get(degradome_filename), 0));
                        String message = "Added degradome file: " + degradome_filename;
                        System.out.println(message);
                        LOGGER.log(Level.INFO, message);
                    } else {
                        throw new IOException("Degradome file: " + degradome_filename + " not found.");
                    }
                }
            }
            
            // ensure at least one degradome/paresnip file has been added
            if (degradome_filenames_paths.isEmpty())
            {
                //throw new JSONParserException("At least one degradome or PAREsnip output file is required.");
                throw new IOException("At least one degradome or PAREsnip output file is required.");
            }
            degradome_samples.put("sample_0", degradome_filenames_paths);
            HTMLWizardViewController.set_degradome_samples(degradome_samples);
        }
        else
        {
            LOGGER.log(Level.INFO, "No degradome file loaded");

        }
        // get paresnip_files samples
        if (jsonObject.containsKey("paresnip_files")) {
            JsonArray paresnip_files = jsonObject.getJsonArray("paresnip_files");
            for (int i = 0; i < paresnip_files.size(); i++) {
                JsonObject paresnip_file_obj = paresnip_files.getJsonObject(i);
                if (paresnip_file_obj.containsKey("paresnip_filename")) {
                    String paresnip_filename = paresnip_file_obj.getString("paresnip_filename");
                    if (Paths.get(paresnip_filename).toFile().exists()) {
                        degradome_filenames_paths.add(new Pair<>(Paths.get(paresnip_filename), 1));
                        String message = "Added PAREsnip result file: " + paresnip_filename;
                        System.out.println(message);
                        LOGGER.log(Level.INFO, message);
                    } else {
                        throw new IOException("PAREsnip result file: " + paresnip_filename + " not found.");
                    }
                }
            }
        }
        else
        {
            LOGGER.log(Level.INFO, "No PARESNIP file loaded");

        }

        

        // get genome
        if (jsonObject.containsKey("genome_filename")) {
            Path genomePath = Paths.get(jsonObject.getString("genome_filename"));
            if (genomePath.toFile().exists()) {
                HTMLWizardViewController.setGenomePath(genomePath);
                String message = "Added genome file: " + genomePath;
                System.out.println(message);
                LOGGER.log(Level.INFO, message);
            } else {
                throw new IOException("Genome file: " + genomePath + " not found.");
            }
        } else {
            //throw new JSONParserException("A genome file is required.");
            throw new IOException("A genome file is required.");

        }

        // get transcriptome
        if (jsonObject.containsKey("transcriptome_filename")) {
            Path transcriptomePath = Paths.get(jsonObject.getString("transcriptome_filename"));
            if (transcriptomePath.toFile().exists()) {
                HTMLWizardViewController.setTranscriptomePath(transcriptomePath);
                String message = "Added transcriptome file: " + transcriptomePath;
                System.out.println(message);
                LOGGER.log(Level.INFO, message);
            } else {
                throw new IOException("Transcriptome file: " + transcriptomePath + " not found.");
            }
        } else {
            //throw new JSONParserException("A transcriptome file file is required.");
            //throw new IOException("A transcriptome file file is required.");
            LOGGER.log(Level.INFO, "No Transcriptome file loaded");

            
        }
        // get annotation
        if (jsonObject.containsKey("annotation_filename")) {
            Path annotationPath = Paths.get(jsonObject.getString("annotation_filename"));
            if(!annotationPath.toString().trim().isEmpty()){
                if (annotationPath.toFile().exists()) {
                    List<Path> pathList = new ArrayList<>();
                    pathList.add(annotationPath);
                    HTMLWizardViewController.setGFFs(pathList);
                    List<String> annotations = new ArrayList<>();
                    annotations.add("miRNA");
                    HTMLWizardViewController.setAnnoations(annotations);
                    String message = "Added annotation file: " + annotationPath;
                    System.out.println(message);
                    LOGGER.log(Level.INFO, message);
                } else {
                    throw new IOException("Annotation file: " + annotationPath + " not found.");
                }
            }
        }

    }

    /*
    public List<MiRCatParams> getMiRCatParameters(JsonObject jsonObject) throws Exception {
        // get parameters
        List<MiRCatParams> paramList = new ArrayList<>();
        //List<WorkflowRunner> runners = WorkflowManager.getInstance().getModuleOfType(MiRCatModule.class);
        JsonArray mircatParamsArray = jsonObject.getJsonArray("mircat_parameters");
        for (int i = 0; i < mircatParamsArray.size(); i++) {
            MiRCatParams params = MiRCatParams.createDefaultPlantParams();
            JsonObject mircatParamsObject = mircatParamsArray.getJsonObject(i);
            params.setComplexLoops(mircatParamsObject.getBoolean(MiRCatParams.Definition.ALLOW_COMPLEX_LOOPS.getDefinition().getName(), params.getComplexLoops()));
            params.setClusterSentinel(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_CLUSTER_SEPARATION_DISTANCE.getDefinition().getName(), params.getClusterSentinel()));
            params.setExtend(mircatParamsObject.getInt(MiRCatParams.Definition.FLANKING_SEQ_EXTENSION.getDefinition().getName(), (int) params.getExtend()));
            params.setMaxGaps(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_GAP_SIZE.getDefinition().getName(), params.getMaxGaps()));
            params.setMaxGenomeHits(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_GENOME_HITS.getDefinition().getName(), params.getMaxGenomeHits()));
            params.setMaxOverlapPercentage(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_SRNA_OVERLAP_PERCENTAGE.getDefinition().getName(), params.getMaxOverlapPercentage()));
            params.setMaxUnpaired(mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_UNPAIRED_BASES_PERCENTAGE.getDefinition().getName(), params.getMaxUnpaired()));
            params.setMinConsider(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_SRNA_ABUNDANCE.getDefinition().getName(), params.getMinConsider()));
            JsonNumber mfe = mircatParamsObject.getJsonNumber(MiRCatParams.Definition.MFE_THRESHOLD.getDefinition().getName());
            if (mfe != null) {
                params.setMinEnergy((float) mfe.doubleValue());
            }
            params.setMinGC(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_GC_PERCENTAGE.getDefinition().getName(), params.getMinGC()));
            params.setMinHairpinLength(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_HAIRPIN_LENGTH.getDefinition().getName(), params.getMinHairpinLength()));
            params.setMinLocusSize(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_LOCUS_SIZE.getDefinition().getName(), params.getMinLocusSize()));
            params.setMinPaired(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_PAIRED_BASES.getDefinition().getName(), params.getMinPaired()));
            params.setOrientation(mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_ORIENTATION_PERCENTAGE.getDefinition().getName(), params.getOrientation()));
            JsonNumber pval = mircatParamsObject.getJsonNumber(MiRCatParams.Definition.P_VAL_THRESHOLD.getDefinition().getName());
            if (pval != null) {
                params.setPVal((float) pval.doubleValue());
            }
            int minLength = mircatParamsObject.getInt(MiRCatParams.Definition.MINIMUM_SRNA_LENGTH.getDefinition().getName(), params.getMinLength());
            int maxLength = mircatParamsObject.getInt(MiRCatParams.Definition.MAXIMUM_SRNA_LENGTH.getDefinition().getName(), params.getMaxLength());
            params.setLengthRange(minLength, maxLength);

            paramList.add(params);
        }
        return paramList;
    }

    public void setUpDatabase(JsonObject jsonObject) throws IOException {
        // get samples
        HashMap<String, List<Path>> srna_samples = new HashMap<>();
        List<String> srna_filesToInclude = new ArrayList<>();
        HashMap<String, List<Pair<Path, Integer>>> degradome_samples = new HashMap<>();
        JsonArray samples = jsonObject.getJsonArray("samples");
        for (int i = 0; i < samples.size(); i++) {
            JsonObject sampleObject = samples.getJsonObject(i);
            JsonArray replicates = sampleObject.getJsonArray("replicates");
            List<Path> srna_filenames = new ArrayList<>();
            List<Pair<Path, Integer>> degradome_filenames = new ArrayList<>();

            for (int j = 0; j < replicates.size(); j++) {
                JsonObject replicate = replicates.getJsonObject(j);
                if (replicate.containsKey("srna_filename")) {
                    String srna_filename = replicate.getString("srna_filename");
                    srna_filenames.add(Paths.get(srna_filename));
                    srna_filesToInclude.add(Paths.get(srna_filename).getFileName().toString());
                }
                if (replicate.containsKey("degradome_filename")) {
                    String degradome_filename = replicate.getString("degradome_filename");
                    degradome_filenames.add(new Pair<>(Paths.get(degradome_filename), 0));
                } else if (replicate.containsKey("paresnip_filename")) {
                    String paresnip_filename = replicate.getString("paresnip_filename");
                    degradome_filenames.add(new Pair<>(Paths.get(paresnip_filename), 1));
                }

            }
            srna_samples.put("sample_" + i, srna_filenames);
            degradome_samples.put("sample_" + i, degradome_filenames);
        }
        HTMLWizardViewController.set_sRNA_samples(srna_samples);
        HTMLWizardViewController.set_degradome_samples(degradome_samples);
        // set samples for inclusion
        FileReviewSceneController.setSelectedFiles(srna_filesToInclude);
        // get genome
        Path genomePath = Paths.get(jsonObject.getString("genome_filename"));
        HTMLWizardViewController.setGenomePath(genomePath);
        // get transcriptome
        Path transcriptomePath = Paths.get(jsonObject.getString("transcriptome_filename"));
        HTMLWizardViewController.setTranscriptomePath(transcriptomePath);
        // get annotation
        if (jsonObject.containsKey("annotation_filename")) {
            Path annotationPath = Paths.get(jsonObject.getString("annotation_filename"));

            List<Path> pathList = new ArrayList<>();
            pathList.add(annotationPath);
            HTMLWizardViewController.setGFFs(pathList);
            List<String> annotations = new ArrayList<>();
            annotations.add("miRNA");
            HTMLWizardViewController.setAnnoations(annotations);
        }

    }
    */
    
    

    public static void main(String[] args) {

    }
    
    public void outputJsonFile(File f) throws Exception {
        JsonObject json = generateJSON();
        if(!Files.exists(f.toPath()))
            Files.createFile(f.toPath());
        PrintWriter writer = new PrintWriter(f);
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeObject(json);
        jsonWriter.close();
        // System.out.println("Workflow Configuration output to: " + f.getAbsolutePath());
    }

}
