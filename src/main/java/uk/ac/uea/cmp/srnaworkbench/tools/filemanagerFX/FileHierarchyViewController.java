/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.swing.JFrame;
import netscape.javascript.JSObject;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF.FileManagerWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * FXML Controller class
 *This class serves as an interface for the DatabaseWorkflowModule
 * @author w0445959
 */
public final class FileHierarchyViewController implements  ControlledScreen {

    private boolean hideInputControls = false;
    private boolean hideDatabaseControls = false;
    private EntryPoint ep;
    private String wizardScreenName;
    

    
    public enum EntryPoint
    {
        DATABASE
        {

          
        },
        FILEMANAGER
        {

            
        }
    
    }
    

    @FXML
    private WebView webView;
    
   

    @FXML
    private AnchorPane hierarchyEditor;

    //Table data for hierarchy editor
    @FXML
    private TreeTableView hierarchyTable;

    @FXML
    private TreeTableColumn<LevelInformation, String> descriptionColumn;

    @FXML
    private TreeTableColumn<LevelInformation, String> levelColumn;

    @FXML
    private AnchorPane mainAnchorPane;
    //end of FXML injectable fields

    private WebEngine webEngine;

    private Scene scene;

    private int nodeCount = 1;

    private static Map<String, List<Path>> samples = new HashMap<>();
    private static List<List<Path>> degradomes = new ArrayList<>();
    private static Path genomePath;
    private static Path mappingFilePath;
    private static Path transcriptomePath;

    private final HashMap<Integer, String> levelToDescription = new HashMap<>();


    private ScreensController myController;
    
    private HTMLWizardViewController wiz;

    
    private Rectangle2D visualBounds;
    private FHJavascriptReceiver myJSBridge;
    
    private WorkflowModule parentModule = null;
    
    public FileHierarchyViewController()
    {
       wiz = new HTMLWizardViewController(this);
    }

    public FileHierarchyViewController(Rectangle2D visualBounds, FileManagerWorkflowModule parentModule)
    {
        this.visualBounds = visualBounds;
        this.parentModule = parentModule;
        this.wizardScreenName = "FM_WIZARD_SCREEN";

        wiz = new HTMLWizardViewController(this);
        //WorkflowSceneController.setWaitingNode("Database");

    }
    
    public FileHierarchyViewController(Rectangle2D visualBounds, DatabaseWorkflowModule parentModule)
    {
        
        this.parentModule = parentModule;
        this.visualBounds = visualBounds;
        this.wizardScreenName = "DB_WIZARD_SCREEN";

        wiz = new HTMLWizardViewController(this);

        //WorkflowSceneController.setWaitingNode("Database");

    }


    public WorkflowModule getModule()
    {
        return parentModule;
    }
    
    /**
     * 
     * @param ep The start point of the workflow
     */
    public void setEntryPoint(EntryPoint ep)
    {

        this.ep = ep;

    }

    public String getWizardScreenName()
    {
        return wizardScreenName;
    }

    public void setWizardScreenName(String wizardScreenName)
    {
        this.wizardScreenName = wizardScreenName;
    }
    
 
    
    
    
    /*
     * FXML injectable handlers start.
     */

    @FXML
    private void handleAddNodeButton(ActionEvent event) {
        addNode();
    }
    
    @FXML
    private void handleRemoveNodeButton(ActionEvent event) {
        removeNode();

    }

    @FXML
    private void handleHideButtonAction(ActionEvent event) {
        hideTable();
    }
    /*
     * FXML injectable handlers end.
     */
    
    public void addPossibleLevels(String type, TreeItem<LevelInformation> selectedItem) {
        if (type.equals("Add a Level")) {

            addLevels(selectedItem);
        }
        if (type.equals("Add a File")) {

            addFile(selectedItem);
        }
    }

    private void removeNode() {
        TreeItem<LevelInformation> selectedItem = getSelectedRow();
        selectedItem.getParent().getChildren().remove(selectedItem);
        hierarchyTable.getSelectionModel().clearSelection();

    }
    
    private void addNode()
    {
                TreeItem<LevelInformation> selectedItem = getSelectedRow();
        if (selectedItem.getValue().getDescription().startsWith("File: ")) {//node is a file and cannot have children added to it
//            Dialogs.create()
//                    .owner(scene.getWindow())
//                    .title("Node Configuration Error")
//                    .masthead("Cannot add nodes...")
//                    .message("Nodes cannot be added to nodes containing files")
//                    .showError();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Node Configuration Error");
            alert.setContentText("Nodes cannot be added to nodes containing files");

            Platform.runLater(()
                    -> alert.show()
            );
            return;
        }
        if (!selectedItem.getChildren().isEmpty() && selectedItem.getChildren().get(0).getValue().getDescription().startsWith("File:")) {//is the child of this node a file? Then only files can be added
            addFile(selectedItem);
        } else {
//            List<CommandLink> links = new ArrayList<>();
//            links.add(new CommandLink("Add a Standard Node",
//                    "These nodes are used to create the hierarchy (for example, a time point layer). They can have any number of children"));
//            links.add(new CommandLink("Add a File Node",
//                    "These nodes contain references to files (for example, a replicate). They are always at the leaf level"));
//
//            Action response = Dialogs.create()
//                    .owner(scene.getWindow())
//                    .title("Adding nodes to the hierarchy")
//                    .masthead(null)
//                    .message("What type of node would you like to add?")
//                    .showCommandLinks(links.get(0), links);
//
//            //System.out.println(response);
//            if (response != CANCEL)
//            {
//                if (((CommandLink) response).getText().equals("Add a Standard Node"))
//                {
//
//                    addLevels(selectedItem);
//                }
//                else
//                {
//
//                    addFile(selectedItem);
//                }
//            }

            ChoiceDialog choice = new ChoiceDialog("Add a Level", "Add a File");
            choice.setTitle("Node Selection");
            choice.setContentText("What type of node would you like to add?");
//        choice.getItems().add("three");
//        choice.getItems().add("four");

            Optional<String> result = choice.showAndWait();

            // The Java 8 way to get the response value (with lambda expression).
            result.ifPresent(letter -> addPossibleLevels(letter, selectedItem));

        }
    }
    
    public void setFrameSize(Dimension size)
    {
        mainAnchorPane.setPrefSize(size.width, size.height-50);
    }
    public void setFrameSize(Dimension size, JFrame parent)
    {
//        this.parent = parent;
        mainAnchorPane.setPrefSize(size.width, size.height-50);
    }
    
    public final void setVisualBounds(Rectangle2D visualBounds)
    {
        if(this.visualBounds == null)
            this.visualBounds = visualBounds;
        mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        loadWizard_setVisualBounds(visualBounds);

        
    }
    
    public void loadWizard_setVisualBounds(Rectangle2D visualBounds)
    {
        

        ScreensController.getInstance().loadScreen(this.wizardScreenName,
                    "/fxml/HTMLWizardView.fxml", wiz);
        wiz.setVisualBounds(visualBounds);
        this.visualBounds = visualBounds;
        if(mainAnchorPane != null)
            mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        
    }

    private void requestTranscriptomeFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Transcriptome File");
        File file = fileChooser.showOpenDialog(scene.getWindow());
        if (file != null) {
            this.transcriptomePath = file.toPath();
        }
    }

    private void requestGenomeFile() {
        //ensure this is shown on the correct thread

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Genome File");
//        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.txt)", "*.txt");
//        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(scene.getWindow());
        if (file != null) {
            //Database.insertGenomeFile(file.toPath());

            genomePath = file.toPath();
        }

    }

    public boolean buildDatabase() 
    {

        if (genomePath == null || !Files.exists(genomePath)) {

//            Platform.runLater(()
//                    -> Dialogs.create()
//                    .owner(scene.getWindow())
//                    .title("File Hierarchy Setup Error")
//                    .masthead("Database missing information")
//                    .message("Please enter a genome file (found in the file menu)")
//                    .showError());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("File Hierarchy Setup Error");
            alert.setContentText("Please enter a genome file (found in the file menu)");

            Platform.runLater(()
                    -> alert.show()
            );
            return false;

        } else if (hierarchyTable.getRoot().getChildren().isEmpty()) {
//            Platform.runLater(()
//                    -> Dialogs.create()
//                    .owner(scene.getWindow())
//                    .title("File Hierarchy Setup Error")
//                    .masthead("Hierarchy appears to have no children")
//                    .message("Please add some children to the hierarchy using the hierarchy editor (found in the edit menu)")
//                    .showError());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("File Hierarchy Setup Error");
            alert.setContentText("Please add some children to the hierarchy using the hierarchy editor (found in the edit menu)");

            Platform.runLater(()
                    -> alert.show()
            );
            return false;
        } else {
            samples.clear();
            populateFileList(hierarchyTable.getRoot());
            if (samples.isEmpty()) {
//                 Platform.runLater(()
//                    -> Dialogs.create()
//                    .owner(scene.getWindow())
//                    .title("File Hierarchy Setup Error")
//                    .masthead("Hierarchy appears to have no File Nodes")
//                    .message("Please add some file nodes to the hierarchy using the hierarchy editor (found in the edit menu)")
//                    .showError());

                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("File Hierarchy Setup Error");
                alert.setContentText("Please add some file nodes to the hierarchy using the hierarchy editor (found in the edit menu)");

                Platform.runLater(()
                        -> alert.show()
                );

                return false;
            } else //All checks passed. Now we can create the database
            {
//                Platform.runLater(() -> {
//                    FadeTransition ft = new FadeTransition(Duration.millis(3000), databaseProgressInd);
//                    ft.setFromValue(0.0);
//                    ft.setToValue(0.8);
//                    ft.setCycleCount(1);
//                    ft.setAutoReverse(true);
//                    ft.play();
//                });
                //DatabaseRunner d_b = new DatabaseRunner(this.host);
                //d_b.buildDatabase(samples, genomePath, databaseProgressInd);

                //Database is built, now move on to setting up the workflow manager
                configureWorkflow();

            }
        }
        return false;
    }
    
    public static Map<String, List<Path>> retrieveTestDataPaths()
    {
        ArrayList<Path> sample1 = new ArrayList<>();
        ArrayList<Path> sample2 = new ArrayList<>();
        ArrayList<Path> sample3 = new ArrayList<>();
        ArrayList<Path> sample4 = new ArrayList<>();
        Map<String, List<Path>> testSamples = new HashMap<>();
        
        //sample1.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa"));
        sample1.add(Paths.get("src/test/data/norm/ath_366868_head.fa"));
        //sample2.add(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154336_carrington_col0_flower_nr.fa"));
        sample2.add(Paths.get("src/test/data/norm/ath_366868_head2.fa"));

        testSamples.put("GSM118373", sample1);
        testSamples.put("GSM154336", sample2);
        
        return testSamples;
    }
    
    public static Path retrieveTestGenomePath()
    {
        return Paths.get("TutorialData/FASTA/GENOME/Ath_TAIR9.fa");
    }
    
    public static Path retrieveTestGFFPath()
    {
        return Paths.get("TutorialData/GFF/TAIR9_GFF3_genes_transposons.Chr1.ID=1.gff");
    }
    
    public static List<Path> retrieveGFFPaths()
    {
        return HTMLWizardViewController.getGFFs();
    }
    
    public static List<String> getAnnotations()
    {
        return HTMLWizardViewController.getAnnotations();
    }
    public static List<String> getOtherAnnotations()
    {
         return HTMLWizardViewController.getOtherAnnotations();
    }
    
    /**
     * Retrieves the selected annotations in a setList, including all
     * standard annotation groupings such as a "Mapped" that includes all mapped
     * anntotations
     * @param considerUnmapped true if you want to include unmapped annotations in this setList
     * @return 
     */
    public static AnnotationSetList getAnnotationSetList(boolean considerUnmapped) throws AnnotationNotInDatabaseException
    {
        AnnotationSetList annotations;
        /**
         * Set up annotations Each "AnnotationSet" should be a single selectable
         * in QC plots. e.g. one may want to select "miRNAs" but also select
         * "other", which includes several different annotations
         */
        AnnotationService aServ = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");

        // new list of annotations
        annotations = new AnnotationSetList();

        // Intergenic annotations are always added.
        annotations.addType(ReferenceSequenceManager.INTERGENIC_TYPE_NAME);

        // retrieve the standard annotations
        List<String> standardAnnotationStrings = FileHierarchyViewController.getAnnotations();
        for (String annot : standardAnnotationStrings) {
            // add each annotation by itself. Automatically creates a single annotation set for it
            annotations.addType(annot);
        }

        // Create an AnnotationSet containing all "Other" annotations
        List<String> otherAnnotationStrings = FileHierarchyViewController.getOtherAnnotations();
        AnnotationSet others = new AnnotationSet("Other");
        for (String annot : otherAnnotationStrings) {
            others.addAnnotationType(annot);
        }
        // add the other annotation set
        annotations.addAnnotationSet(others);

        // Add the set of "all mapped" annotations to allow the user to select "All" as an annotation
        // specification in QC plots
        annotations.addAnnotationSet(AnnotationSet.getMappedSet());

        // add the "All" set and "unmapped" annotations if considered
        if (considerUnmapped) {
            annotations.addAnnotationSet(AnnotationSet.getAllSet());
            AnnotationSet none = aServ.getTypesForReference(ReferenceSequenceManager.NO_REFERENCE_NAME);
            annotations.addAnnotationSet(none);
        }
        return annotations;
    }
    
    public static Path retrieveGenomePath()
    {
        genomePath = HTMLWizardViewController.getGenome();
        return genomePath;
    }
    public static Path retrieveMappingFilePath()
    {
        mappingFilePath = HTMLWizardViewController.getMappingFile();
        return mappingFilePath;
    }
    
    /*
     * @author chris: added to retrieve transcriptome
     */
    public static Path retrieveTranscriptomePath()
    {
        transcriptomePath = HTMLWizardViewController.getTranscriptome();
        return transcriptomePath;
    }
    
    public static Map<String, List<Path>> retrieveDataPaths()
    {
       samples = HTMLWizardViewController.getSamples();
       return samples;
    }
    
    public static String getTableHTML()
    {
        return HTMLWizardViewController.getTableHTML();
    }

    private void configureWorkflow() {
        try {
            //First step in the workflow setup is to create the genome list
            //list name is genome
            //Key is genome, this means it must use Chris's Genome Map class
            //last two variables say how many we can have (min 1, max 1)
            WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);
            //Now the list is created, we actually add the data, in this case, the genome path
            WorkflowManager.getInstance().addInputData("genome", WorkflowManager.CompatibilityKey.GENOME, new GenomeManager(this.genomePath));

            //Next step is to configure all small RNA files that exist in the database
            //We do this by creating an initial list of SQL queries that will return each filename in the database
            //min one file to be included but no upper limit
            WorkflowManager.getInstance().addInputDataContainerList("sRNA_File_Queries", WorkflowManager.CompatibilityKey.sRNA_FILE_QUERIES, 1, -1);

            //SQL at this point will be RAW SQL rather than using hibernate, this is so the SQL can be chained in the next tool (if needed)
            // print out all interactions
            FilenameServiceImpl service = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameAbundanceService");
            List<Filename_Entity> list = service.findAll();
            for (Filename_Entity p : list) {
                StringBuilder select = new StringBuilder("SELECT Filepath FROM Filename_Entity WHERE Filepath=\"" + p.getFilePath() + "\"");

                WorkflowManager.getInstance().addInputData("sRNA_File_Queries", WorkflowManager.CompatibilityKey.sRNA_FILE_QUERIES, select);
            }
            configureOtherData();
        } catch (IOException | DuplicateIDException ex) {
            Logger.getLogger(FileHierarchyViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // chris added: will need to be included within configure workflow function
    private void configureOtherData() {
        // add transcriptome from the file hierarchy to the manager 
        WorkflowManager.getInstance().addInputDataContainerList("transcripts", WorkflowManager.CompatibilityKey.TRANSCRIPT_FILE, 1, 1);
        WorkflowManager.getInstance().addInputData("transcripts", WorkflowManager.CompatibilityKey.TRANSCRIPT_FILE, this.transcriptomePath);
        
        // add degradomes from the file hierarchy to the manager 
        WorkflowManager.getInstance().addInputDataContainerList("degradomes", WorkflowManager.CompatibilityKey.DEGRADOME_FILE, 1, -1);
    }

    private void populateFileList(TreeItem<LevelInformation> node)
    {
        if (node.isLeaf())
        {
            return;
        }
        if (node.getChildren().get(0) != null && node.getChildren().get(0).isLeaf())
        {

            ArrayList<Path> sample = new ArrayList<>();
            //node.getChildren().stream().forEach((child) ->
            for (TreeItem<LevelInformation> child : node.getChildren())
            {
                Path filePath = Paths.get(child.getValue().getLongDescription());
                if (!child.getValue().getLongDescription().isEmpty() && Files.exists(filePath))
                {
                    //System.out.println("ID: " + child.getValue().getDescription() );
                    sample.add(filePath);
                }
            } //file nodes
            if (!sample.isEmpty())
            {
                samples.put(Integer.toString(samples.size()), sample);
            }
        }
        else
        {
            for (TreeItem<LevelInformation> child : node.getChildren())
            {
                populateFileList(child);
            }

        }
    }

    private void addFile(TreeItem<LevelInformation> selectedItem) {

        LevelInformation selected_value = selectedItem.getValue();

        int level = StringUtils.safeIntegerParse(selected_value.getLevelId(), -1) + 1;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sequence Files");
//        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.txt)", "*.txt");
//        fileChooser.getExtensionFilters().add(extFilter);
        List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                String description = "File: Replicate - " + i + " | " + files.get(i).getName() + " | ";
                levelToDescription.put(level, description);
                TreeItem<LevelInformation> new_item = new TreeItem<>(new LevelInformation(String.valueOf(level), description,
                        String.valueOf(nodeCount), files.get(i).getAbsolutePath()));

                selectedItem.getChildren().add(new_item);

                selectedItem.setExpanded(true);

                nodeCount++;
            }

            //database requires rebuilding due to File addition
            DatabaseWorkflowModule.getInstance().setSetup(false);
        }

    }

    private void addLevels(TreeItem<LevelInformation> selectedItem) {
        LevelInformation selected_value = selectedItem.getValue();
        int level = StringUtils.safeIntegerParse(selected_value.getLevelId(), -1) + 1;

        String description = levelToDescription.get(level) == null ? "Awaiting Information" : levelToDescription.get(level);

        final TextField numberField = new TextField();
        final TextField descriptionField = new TextField();

        TextInputDialog input = new TextInputDialog();
        input.setTitle("User data");
        input.setContentText("Enter name");

        input.showAndWait();
//                
//        Button btn = new Button();
//        btn.setText("Display dialog");
//        btn.setOnAction(e->System.out.println());
//        
//        StackPane root = new StackPane();
//        root.getChildren().add(btn);
//        
//        Scene scene = new Scene(root, 300, 250);

//        primaryStage.setTitle("Dialogs test");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//        final Action actionLogin = new AbstractAction("Add Nodes")
//        {
//            // This method is called when the login button is clicked ...
//            @Override
//            public void handle(ActionEvent ae)
//            {
//                Dialog d = (Dialog) ae.getSource();
//                // Do the login here.
//                d.hide();
//            }
//        };
//
//        // Create the custom dialog.
//        Dialog dlg = new Dialog(this.scene.getWindow(), "Level Setup");
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(0, 10, 0, 10));
//
//        numberField.setPromptText("Number To Add");
//        descriptionField.setPromptText("Initial Description");
//        descriptionField.setText(description);
//
//        grid.add(new Label("Enter number of nodes to add at Level " + (level - 1) + ":"), 0, 0);
//        grid.add(numberField, 1, 0);
//        grid.add(new Label("Enter Initial Description (can be modified):"), 0, 1);
//        grid.add(descriptionField, 1, 1);
//
//        ButtonBar.setType(actionLogin, ButtonType.OK_DONE);
//        actionLogin.disabledProperty().set(true);
//
//        // Do some validation (using the Java 8 lambda syntax).
//        numberField.textProperty().addListener((observable, oldValue, newValue) ->
//        {
//            actionLogin.disabledProperty().set(newValue.trim().isEmpty());
//        });
//
//        dlg.setMasthead("Enter number of nodes and an initial description");
//        dlg.setContent(grid);
//        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
//
//        // Request focus on the username field by default.
//        Platform.runLater(() -> numberField.requestFocus());
//
//        dlg.show();
        if (!descriptionField.getText().equals(description)) {
            description = descriptionField.getText();
            levelToDescription.put(level, description);
        }

        int nodeAddCount = StringUtils.safeIntegerParse(numberField.getText(), 0);
        for (int i = 0; i < nodeAddCount; i++) {

            TreeItem<LevelInformation> new_item = new TreeItem<>(new LevelInformation(String.valueOf(level), description, String.valueOf(nodeCount)));

            selectedItem.getChildren().add(new_item);

            selectedItem.setExpanded(true);

            nodeCount++;
        }

    }

    private TreeItem<LevelInformation> getSelectedRow() {
        int focusedIndex = hierarchyTable.getSelectionModel().getFocusedIndex() >= 0 ? hierarchyTable.getSelectionModel().getFocusedIndex() : 0;

        return hierarchyTable.getTreeItem(focusedIndex);
    }

    private void buildTree(TreeItem<LevelInformation> node, StringBuilder json) {
        if (node.isLeaf()) {
            return;
        }

        if (node.getParent() == null)//root
        {
            json.append("\"name\":\"").append(node.getValue().getDescription()).append("\",");
            json.append("\"children\": [");
        }
        ObservableList<TreeItem<LevelInformation>> children = node.getChildren();

        for (int i = 0; i < children.size(); i++) {
            TreeItem<LevelInformation> child = children.get(i);
            json.append("{\"name\":\"")//description of node
                    .append("Level - ")
                    .append(child.getValue().getLevelId())
                    .append(" - ")
                    .append(child.getValue().getDescription())
                    .append(" - ID: ")
                    .append(child.getValue().getID())//description ends here
                    .append("\",\"children\": [");//request for children

            buildTree(child, json);
            json.append("]},");
            if (i == children.size() - 1) {
                json.replace(json.length() - 1, json.length(), "");
            }

        }
        if (node.getParent() == null)//root
        {
            json.append("]");
        }

    }

    private void resetTable() {
        hierarchyTable.setRoot(null);

        final TreeItem<LevelInformation> root
                = new TreeItem<>(new LevelInformation("0", "Hierarchy Root", "0"));
        root.setExpanded(true);

        this.hierarchyTable.setRoot(root);

        createJSON();
    }

    private void showTable() {
        //hierarchyEditor.setVisible(true);
        //System.out.println(""+hierarchyEditor.getOpacity());
        hierarchyEditor.setVisible(true);
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(500), hierarchyEditor);
            ft.setFromValue(0.0);
            ft.setToValue(0.8);
            ft.setCycleCount(1);
            ft.setAutoReverse(true);
            ft.play();

        });
    }

    private void hideTable() {
        //hierarchyEditor.setVisible(false);
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(500), hierarchyEditor);
            ft.setFromValue(0.8);
            ft.setToValue(0.0);
            ft.setCycleCount(1);
            ft.setAutoReverse(true);
            ft.play();
            ft.onFinishedProperty().set((EventHandler<ActionEvent>) (ActionEvent actionEvent) -> {
                hierarchyEditor.setVisible(false);
            });
        });
        createJSON();

    }

    private void createJSON() {

        StringBuilder json = new StringBuilder("{");
        buildTree(hierarchyTable.getRoot(), json);
        json.append("}");

        Path get = Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FileHierarchyData.json");
        try {
            if (Files.exists(get)) {
                Files.delete(get);
            }
            Files.write(get, json.toString().getBytes(), CREATE);

        } catch (IOException ex) {
            Logger.getLogger(FileHierarchyViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

         //JsonObject hierarchy = JSON_objectBuilder.build();
//        JsonObject value = Json.createObjectBuilder()
//                .add("name", "Hierarchy Root")
//                .add("children", Json.createArrayBuilder()
//                        .add(Json.createObjectBuilder()
//                                .add("name", "Level 1")
//                                )
//                        .add(Json.createObjectBuilder()
//                                .add("name", "Level 1")
//                                ))
//                .build();
//        try (OutputStream out = new FileOutputStream(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FileHierarchyData.json");
//                JsonWriter jsonWriter = Json.createWriter(out))
//        {
//            String toString = hierarchy.toString();
//            jsonWriter.writeObject(hierarchy);
//        }
//        catch(IOException e)
//        {
//            LOGGER.log(Level.SEVERE, "AT Json creation: {0}", e.getMessage());
//        }
        refreshHierarchyView();
        //this.webView.ref
//        JSONObject obj = new JSONObject();
//	obj.put("name", "File Structure Hierarchy");
// 
//        for(File f : files)
//        {
//            obj.put("filename: ", f.getName());
//        }
////	JSONArray list = new JSONArray();
////	
////        JSONObject analyticsObj = new JSONObject();
////	analyticsObj.put("name", "analytics");
////        list.add(analyticsObj);
//// 
////	obj.put("children", list);
//
//        try
//        {
//
//           
//            Path JSON_Path = Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FileHierarchyData.json");
//            Files.write(JSON_Path, obj.toJSONString().getBytes(), CREATE);
//
//        }
//        catch (IOException e)
//        {
//            LOGGER.log(Level.SEVERE, e.getMessage());
//        }
//        
//
//	System.out.print(obj);
//        
    }
    
    
    public void setBusy(boolean state)
    {
        if ((!DatabaseWorkflowModule.getInstance().isDebugMode())&&(!AppUtils.INSTANCE.isBaseSpace())&&(!AppUtils.INSTANCE.isCommandLine()))
        {
            Platform.runLater(() ->
            {
                webEngine.executeScript("setBusy( '" + state + "' )");
            });
        }
    }
    
    private void refreshHierarchyView() {
        
        try {

            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR + "/HTML/FileHierarchyView.html");

            webEngine.load(h_view.toExternalForm());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FileHierarchyViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    //@Override
    @FXML
    public void initialize() {
        assert webView != null : "fx:id=\"webView\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert hierarchyEditor != null : "fx:id=\"hierarchyEditor\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert descriptionColumn != null : "fx:id=\"descriptionColumn\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert levelColumn != null : "fx:id=\"levelColumn\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert hierarchyEditor != null : "fx:id=\"hierarchyEditor\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'FileHierarchyView.fxml'.";

        webEngine = webView.getEngine();
        
        //if(parentModule != null)
        //{
        this.setVisualBounds(visualBounds);
        //}
        //WorkflowSceneController.setWaitingNode("Database");

        //using this as the directory should already exist and error if not
        Path getWV_Dir = Paths.get(Tools.WEB_VIEW_DATA_Path);
        if (Files.exists(getWV_Dir)) {
            File file = getWV_Dir.toFile();
            webEngine.setUserDataDirectory(file);
        } else {
            LOGGER.log(Level.SEVERE, "Cannot create web view local storage data");
        }

        webEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Event: " + arg0);
        });

        webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
            

            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("app", myJSBridge);
                window.setMember("databaseHide", hideDatabaseControls);
                window.setMember("inputHide", hideInputControls);

            }
        });

        levelColumn.setCellValueFactory(new TreeItemPropertyValueFactory<LevelInformation, String>("levelId"));
        descriptionColumn.setCellValueFactory(new TreeItemPropertyValueFactory<LevelInformation, String>("description"));

        levelColumn.setCellFactory(
                TextFieldTreeTableCell.<LevelInformation>forTreeTableColumn());

        levelColumn.setOnEditCommit((final CellEditEvent<LevelInformation, String> event) -> {
            final LevelInformation item = event.getRowValue().getValue();

            item.setLevelId(event.getNewValue());
        });

        descriptionColumn.setCellFactory(
                TextFieldTreeTableCell.<LevelInformation>forTreeTableColumn());

        descriptionColumn.setOnEditCommit((final CellEditEvent<LevelInformation, String> event) -> {
            final LevelInformation item = event.getRowValue().getValue();

            item.setDescription(event.getNewValue());
        });

        //resetTable();
        hierarchyTable.setEditable(true);

        refreshHierarchyView();
        
        
        
        //WorkflowSceneController.setWaitingNode(DatabaseWorkflowModule.getInstance().getID());

    }

    public void setStageAndSetupListeners(Scene scene) {
        this.scene = scene;
    }


    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
        myJSBridge = new FHJavascriptReceiver(this.myController);
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void reloadHierarchy()
    {
        this.webEngine.executeScript("reloadHierarchy()");
        //webEngine.reload();
    }

    @Override
    public void workflowStartedListener() {
    //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  
    

    /**
     *
     */
    public class LevelInformation {

        private SimpleStringProperty levelId = new SimpleStringProperty("");
        private SimpleStringProperty description = new SimpleStringProperty("");
        private SimpleStringProperty long_description = new SimpleStringProperty("");//used to store full file paths
        private SimpleStringProperty ID = new SimpleStringProperty("");

        public LevelInformation(String l_ID, String desc, String ID) {
            setLevelId(l_ID);
            setDescription(desc);
            setID(ID);
        }

        public LevelInformation(String l_ID, String desc, String ID, String long_description) {
            this(l_ID, desc, ID);
            setLongDescription(long_description);
        }

        public SimpleStringProperty levelIdProperty() {
            if (levelId == null) {
                levelId = new SimpleStringProperty(this, "Level 0");
            }
            return levelId;
        }

        public SimpleStringProperty descriptionProperty() {
            if (description == null) {
                description = new SimpleStringProperty(this, "N/A");
            }
            return description;
        }

        public SimpleStringProperty longDescriptionProperty() {
            if (long_description == null) {
                long_description = new SimpleStringProperty(this, "N/A");
            }
            return long_description;
        }

        public void setLevelId(String l_ID) {
            levelId.set(l_ID);
        }

        public void setDescription(String desc) {
            description.set(desc);
        }

        public void setLongDescription(String desc) {
            long_description.set(desc);
        }

        public void setID(String ID) {
            this.ID.set(ID);
        }

        public String getLevelId() {
            return levelId.get();
        }

        public String getLongDescription() {
            return long_description.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getID() {
            return ID.get();
        }

    }

    public class FHJavascriptReceiver extends JavascriptBridge {

        
        
        

        public FHJavascriptReceiver(ScreensController controller)
        {
            
            super(controller);

        }
        
        public void load_previous_inputs()
        {
            wiz.load_inputs();
        }
        //this recieves a message from the dndTree java script file
        //only when asked for by calling the function callToJavaFX within the js

        public void callFromJavascript(String msg) {
            System.out.println("Click from Javascript: " + msg);

        }

        public void showEditor() {

            showTable();

        }

        public void hideEditor() {
            hideTable();

        }

        public void resetEditor() {
            resetTable();

        }
        
        public void handleRequestGenomeFile()
        {
            requestGenomeFile();
        }
        
        public void handleRequestTranscriptomeFile()
        {
            requestTranscriptomeFile();
        }
        public String[] requestReplicateFile(int sampleIndex)
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select replicate files for sample");
            List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());
            if (files != null)
            {
                String[] returnMe = new String[files.size()];
                for (File file : files)
                {
                    samples.get(sampleIndex).add(file.toPath());
    
                }
                return returnMe;
            }
            //nothing selected
            return new String[0];
        }
        public void populateFileData(int sampleCount, int repsPerSample)
        {
            for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++)
            {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select sequence files for sample: " + sampleIndex);
        //        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.txt)", "*.txt");
        //        fileChooser.getExtensionFilters().add(extFilter);
                boolean reachedReplicateLimit = false;
                int addedForThisSample = 0;
                while (!reachedReplicateLimit)
                {
                    List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());
                    if (files != null)
                    {
                        for (File file : files)
                        {
                            samples.get(sampleIndex).add(file.toPath());
                            addedForThisSample++;
                        }
                        if(addedForThisSample == repsPerSample)
                        {
                            reachedReplicateLimit = true;
                        }
                    }
                    else//possibly hit the cancel button
                    {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Workbench Configuration Error");
                        alert.setContentText("Replicate files have not been added for this sample");

                        Platform.runLater(()
                                -> alert.show()
                        );
                        return;
                    }
                }

            }
            
        }
        

        public void hideWizard()
        {
//            System.out.println("hiding wizard");
//            wiz.setVisible(false);
            ScreensController.getInstance().setScreen(parentModule.getID());
        }
        
        public boolean canShowWizard(boolean showMessage)
        {
            boolean result = true;
            if(parentModule instanceof DatabaseWorkflowModule )
            {
                switch(ep)
                {
                    case DATABASE://nothing needs to change
                        break;
                    case FILEMANAGER://no file input settings should be accessible
                        if(showMessage)
                        {
                            Platform.runLater(() ->
                            {
                                Alert alert = new Alert(AlertType.INFORMATION);
                                alert.setTitle("Workbench Information");
                                alert.setHeaderText("Workflow Setup Message");
                                alert.setContentText("File inputs cannot be edited from this module. " + LINE_SEPARATOR + "Please refer to the File Manager module");

                                alert.showAndWait();
                            });
                        }

                        result = false;
                        break;
                        
                    default://should be unreachable due to enum
                        break;
                }
            }
            else if(parentModule instanceof FileManagerWorkflowModule )//no database settings should be accessible
            {
                result = true;
            }
            
            return result;
        }
        public boolean canControlDatabase()
        {
            boolean result = true;
            if(parentModule instanceof DatabaseWorkflowModule )
            {
                switch(ep)
                {
                    case DATABASE://nothing needs to change
                        break;
                    case FILEMANAGER:
    
                        result = true;
                        break;
                        
                    default://should be unreachable due to enum
                        break;
                }
            }
            else if(parentModule instanceof FileManagerWorkflowModule )//no database settings should be accessible
            {
                if(WorkflowManager.getInstance().containsID("Database"))
                {
                    Platform.runLater(() ->
                    {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Workbench Information");
                        alert.setHeaderText("Workflow Setup Message");
                        alert.setContentText("The database settings cannot be modified from this module. " + LINE_SEPARATOR + "Please refer to the Database module");

                        alert.showAndWait();
                    });
                }
                else
                {
                    Platform.runLater(() ->
                    {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Workbench Information");
                        alert.setHeaderText("Workflow Setup Message");
                        alert.setContentText("The database module is not included in this workflow");

                        alert.showAndWait();
                    });
                }
                
                result = false;
            }
            
            return result;
        }
        public void showWizard()
        {   
//            wiz.setVisible(true);
//            wiz.toFront();
            //wiz.setAlwaysOnTop(true);
            ScreensController.getInstance().setScreen(wizardScreenName);
            
            
        }
        
        public void debug()
        {
            System.out.println("TEST DEBUG");
        }
        public void setFilterRepeats(boolean state)
        {
            //System.out.println("repeated hits: " + state);
            DatabaseWorkflowModule.getInstance().setFilterRepeats(state);
        }
        
        public void setAlignmentGapsMismatch(int gaps, int mismatch)
        {
            DatabaseWorkflowModule.getInstance().setGapsMismatch(gaps, mismatch);
        }
        
        public void setupDatabaseFilter(int amountToFilter)
        {
            //System.out.println("filter amount: " + amountToFilter);
            DatabaseWorkflowModule.getInstance().setAlignmentFilterValue(amountToFilter);
        }
        
        public String getEntryMode()
        {
            return ep.toString();// + " " + parentModule.getClass();
        }
    }
}
