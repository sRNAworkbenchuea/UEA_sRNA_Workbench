/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.io.GFFFileReader;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF.FileManagerWorkflowModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.PreconfiguredWorkflows;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class HTMLWizardViewController implements Initializable, ControlledScreen
{
    @FXML
    private WebView wizardWebView;
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    private WebEngine webEngine;
    

    private static InputFiles inputs;
    private static Path previous_inputs = Paths.get(Tools.PROJECT_DIR + "/" + "previous_input.ser");
    

    private HTML_FH_Wizard parentFrame;
     
    private static String tableHTML = "";
    
    private ScreensController myController;
    
    private static Rectangle2D visualBounds;

    private Scene scene;
    
    private boolean annotationsProcessed = false;
    
    private FileHierarchyViewController parentController;
    private WizardJSReceiver wizardJSReceiver;
    
    private String myScreenName;
    
    public HTMLWizardViewController(FileHierarchyViewController parent)
    {
        parentController = parent;
        this.inputs = new InputFiles();
        clearJSON();
    }
        
    public static void set_sRNA_samples(HashMap<String, List<Path>> data)
    {
        inputs.set_sRNA_samples(data);
    }
    
    public static void set_degradome_samples(HashMap<String, List<Pair<Path, Integer>>> data)
    {
        inputs.set_degradomes(data);
    }
    
    public static void setGFFs(List<Path> data)
    {
        inputs.set_GFFs(data);
    }
    public static void setAnnoations(List<String> data)
    {
        inputs.set_annotations(data);
    }
    public static void setGenomePath(Path data)
    {
        inputs.set_genome(data);
    }
    public static void setTranscriptomePath(Path data)
    {
        inputs.set_transcriptome(data);
    }
    
    public void load_inputs()
    {
        try{
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(previous_inputs.toFile()));
            inputs = (InputFiles) reader.readObject();
            DatabaseWorkflowModule.getInstance().setMapper(inputs.mapper);
            
            // Fill wizard with loaded inputs
            String jq = "$('#filetable_div')";
            webEngine.executeScript(jq+".fileTable('clearTable')");
            
            Map<String, List<Path>> samples = inputs.get_sRNA_samples();
            for (Entry<String, List<Path>> sample : samples.entrySet()) {
                //webEngine.executeScript("fileReviewTable.addSample('"+sample.getKey()+"')");
                webEngine.executeScript(jq + ".fileTable('addSample'" + ",'" + sample.getKey() + "')");
                for (Path replicate : sample.getValue()) {
                    //webEngine.executeScript("fileReviewTable.addReplicate('"+sample.getKey()+"', '"+replicate.getFileName().toString()+"')");
                    webEngine.executeScript(jq + ".fileTable('add_file'" + ",'" + sample.getKey() + "', 'sRNA', ['" + replicate.getFileName().toString() + "'])");
                }
            }
            Map<String, List<Pair<Path, Integer>>> degs = inputs.get_degradomes();
            for (Entry<String, List<Pair<Path, Integer>>> deg : degs.entrySet())
            {
                for(int i = 0; i<deg.getValue().size(); i++){
                    Pair p = deg.getValue().get(i);
                    //webEngine.executeScript(jq + ".fileTable('add_file'" + ",'" + deg.getKey() + "', 'deg', ['" + p.getKey().toString() + "'])");
                    //webEngine.executeScript(jq + ".fileTable('setDegDataType'" + ",'" + deg.getKey() + (i+1) + "," + p.getValue()+")");
                }
            }
            
            // if there is no genome but an output mapping file, set to an output mapper
            String mapper = inputs.mapper.name().toLowerCase();
            if(inputs.get_genome() == null && inputs.get_mapping_file() != null){
                webEngine.executeScript("$('#"+mapper+"_output').prop('checked', true);");
                webEngine.executeScript("add_genome_file('"+inputs.get_mapping_file().toString()+"')");
            }
            // else if there is a genome, set check box to one of the mappers
            else if(inputs.get_genome() != null){
                webEngine.executeScript("$('#"+mapper+"_map').prop('checked', true);");
                webEngine.executeScript("add_genome_file('"+inputs.get_genome().toString()+"')");
            }

            if(!inputs.get_GFFs().isEmpty()){
                webEngine.executeScript("addGFF('"+inputs.get_GFFs().get(0)+"');");
                this.processAnnotations();
            }
            
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Inputs loaded");
            alert.setContentText("Previous Input has been loaded");
            Platform.runLater(() -> {
                alert.show();
            });
        }
        catch(JSException e)
        {
            LOGGER.log(Level.SEVERE, "Error loading previous input", e);
        }
        catch(IOException e)
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("No previous Inputs");
            alert.setContentText("No previous inputs could be loaded");
            Platform.runLater(() -> {alert.show();});
        }
        catch(ClassNotFoundException e)
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("No previous Inputs");
            alert.setContentText("The previous input files appear to be malformed");
            Platform.runLater(() -> {alert.show();});
        }
    }
    
    private void refreshWizardView() {
        try {

            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR + "/HTML/wizardcontent/FileSetupWizard.html");

            webEngine.load(h_view.toExternalForm());
        } catch (MalformedURLException ex) {
            Logger.getLogger(HTMLWizardViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Initializes the controller class.
     */
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        webEngine = wizardWebView.getEngine();
        
        webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
            wizardJSReceiver = new WizardJSReceiver(this.myController);
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("app", wizardJSReceiver);
            }
        });
        
        webEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Event: " + arg0);
        });
        
        webEngine.setOnError((WebErrorEvent event) ->
        {
            System.out.println("error in worflow scene: " + event.getMessage());
        });


        
        refreshWizardView();
    }    

    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        this.scene = scene;
    }

    public Rectangle2D getVisualBounds()
    {
        return visualBounds;
    }

    public void setVisualBounds(Rectangle2D visualBounds)
    {
//        try
//        {
//            Thread.sleep(500);
//        }
//        catch (InterruptedException ex)
//        {
//            Logger.getLogger(HTMLWizardViewController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        Platform.runLater(() ->
//        {
//            this.visualBounds = visualBounds;
//            mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
//        });
        if (mainAnchorPane != null)
        {
            this.visualBounds = visualBounds;
            mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());

        }
        else
        {
            visualBounds = new Rectangle2D(0, 0, 0, 0);
        }



    }
    
    public static Map<String, List<Path>> getSamples()
    {
        return inputs.get_sRNA_samples();
    }
    

    public static Map<String, List<Pair<Path, Integer>>> getDegradomes()
    {
        return inputs.get_degradomes();
    }
    
    public static Path getGenome()
    {
        return inputs.get_genome();
    }
    
    public static Path getMappingFile()
    {
        return inputs.get_mapping_file();
    }
    
    public static Path getTranscriptome()
    {
        return inputs.get_transcriptome();
    }

    public void addParent(HTML_FH_Wizard aThis)
    {
        parentFrame = aThis;
    }

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        myController = screenPage;
    }

    public static String getTableHTML()
    {
        return tableHTML;
    }
    
    public void setupAnnotations(ArrayList<String> annotations)
    {
        Platform.runLater(() ->
        {
            
            //window.setMember("tableHTML", tableHTML);
            for (String annot : annotations)
            {
                webEngine.executeScript("addToAnnotationTable('" + annot +"')");
            }
        });
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<Path> getGFFs()
    {
        return inputs.get_GFFs();
    }

    public static List<String> getAnnotations()
    {
        if(DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            //annotations.add("exon");
            //inputs.add_annotation("miRNA");//getting all selected options from interface
            try {
                return new ArrayList(DatabaseWorkflowModule.getInstance().getAnnotationSetList().getAllTypes());
            } catch (AnnotationNotInDatabaseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return inputs.get_annotations();
    }
    
    public static List<String> getOtherAnnotations()
    {
       
        return inputs.get_other_annotations();
    }
    
    public void clearJSON()
    {
        Path get = Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FileHierarchySQData.json");
        try
        {
            Files.write(get, new byte[1]);
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public static void configureGenericData() throws IOException, FileNotFoundException, DuplicateIDException
    {
   
        if(DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            inputs.set_genome(DatabaseWorkflowModule.getInstance().getGenomePath());
        }
        if(inputs.get_genome() == null )
        {
            throw new IOException("Genome has not been configured");
        }

        WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);

        
        WorkflowManager.getInstance().addInputData("genome", WorkflowManager.CompatibilityKey.GENOME, new GenomeManager(inputs.get_genome()));


    }
    
    public static void configureWorkflowData() throws Exception
    {
        switch (WorkflowManager.getInstance().getName())
        {
            case "mirpare":
                configureGenericData();
                String path = PreconfiguredWorkflows.configureMiRPAREWorkflow(null, null, visualBounds);
                WorkflowSceneController.getEngine().executeScript("createPreconfiguredFlow('" + path + "');");
                break;
            case "mircat":
                configureGenericData();
                WorkflowManager.getInstance().addInputData("srnaQuery", WorkflowManager.CompatibilityKey.sRNA_QUERY, new StringBuilder());
                WorkflowManager.getInstance().connectDB2Module("genome", 0, "miRCat", "genome");
                WorkflowManager.getInstance().connectDB2Module("srnaQuery", 0, "miRCat", "srnaQuery");
                break;
            default:
                break;
        }
        
    }

    @Override
    public void workflowStartedListener() {
      //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void processAnnotations()
    {
     
        if (!annotationsProcessed) {
            for (Path annot_path : inputs.get_GFFs()) {
                GFFFileReader gfr = new GFFFileReader(annot_path.toFile());

                List< GFFRecord> records = null;

                if (gfr.processFile()) {
                    records = gfr.getGFFRecords();
                }

                if (records == null) {
                    LOGGER.log(Level.SEVERE, " Unable to load annotations from {0}", annot_path);
                    return;
                }

                System.out.println(" Loaded annotations");

                // Get the GFF type sets
                //
                HashMap<String, Integer> gffTypeSetAll = new HashMap<>();

                for (GFFRecord gffrec : records) {

                    // Set of all types
                    if (gffTypeSetAll.containsKey(gffrec.getType())) {
                        Integer currentCount = gffTypeSetAll.get(gffrec.getType());
                        currentCount++;
                        gffTypeSetAll.put(gffrec.getType(), currentCount);

                    } else {
                        gffTypeSetAll.put(gffrec.getType(), 1);

                    }

                }
                for (Entry<String, Integer> type : gffTypeSetAll.entrySet()) {
                    //System.out.println("type: " + type);
                    String listid = "available_annot_list";
                    if(inputs.get_annotations().contains(type.getKey()))
                        listid = "selected_annot_list";
                    else if (inputs.get_other_annotations().contains(type.getKey()))
                        listid = "other_list";
                    webEngine.executeScript("addToAnnotationTable('" + type.getKey() + "','" + type.getValue() + "', '"+listid+"')");
                }

//                _allTypes.clear();
//                _mainTypes.clear();
//
//                _allTypes.addAll(gffTypeSetAll);
//                _mainTypes.addAll(gffTypeSetID);
//
//                Collections.sort(_allTypes);
//                Collections.sort(_mainTypes);
                //initialiseListData();
                System.gc();
            }
            annotationsProcessed = true;
        }   
    }

    public class WizardJSReceiver extends JavascriptBridge
    {

        private final int replicateLimit = 0;

        // stores the last directory that was looked in using a filechooser
        private File lastFileDir = null;

        public WizardJSReceiver(ScreensController controller)
        {
            super(controller);
        }
               
        //setup functions for each sample
        public void addSample(String id)

        {

            
            
            

       
            inputs.add_sample(id);  

        }
        
        // return whether the loaded workflow requires a transcriptome
        public boolean isTranscriptomeOptional()
        {
            return WorkflowManager.getInstance().transcriptomeOptional;
        }
        
        // return whether transcriptome constraints met
        public boolean isTranscriptomeValid()
        {
            if(WorkflowManager.getInstance().transcriptomeOptional)
                return true;
            return inputs.transcriptomeIsValid();
            //return transcriptome != null && transcriptome.toFile().exists();
        }
        
        
        public void addTranscriptome()
        {
            inputs.set_transcriptome(Paths.get(""));
        }
        
        public boolean isFileManager()
        {
            return parentController.getModule() instanceof FileManagerWorkflowModule;
        }
        
        /*
         * @author chris
         * opens a file chooser to input transcriptome
         */
        public String addFileToTranscriptome()
        {
            // create a file chooser
            FileChooser fileChooser = new FileChooser();
            // set the title
            fileChooser.setTitle("Select Primary Transcriptome File");
            // set directory to last directory visited (if available)
            if(this.lastFileDir != null)
            {
                fileChooser.setInitialDirectory(this.lastFileDir);
            }
            // open the window
            File file = fileChooser.showOpenDialog(scene.getWindow());
            // if a is specified, update the transcriptome path, the last visited directory, and return the name of the file
            if (file != null)
            {

                this.lastFileDir = file.getParentFile();
                inputs.set_transcriptome(file.toPath());
                return file.getName();
            }
            // otherwise return empty string (in this case, the previous file should remain
            return null;
        }

        public void removeSample(String id)
        {
            inputs.remove_sample(id);

        }
        public void removeGFF(String id)
        {
            inputs.remove_GFF(Paths.get(id));
            
        }
        
        public void removeTranscriptome()
        {
            inputs.remove_transcriptome();
        }
        public String addFileToGenome()
        {
            DatabaseWorkflowModule.getInstance().setMapper(DatabaseWorkflowModule.Mapper.PATMAN);
           
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select primary Genome file");
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);

            
            File file = fileChooser.showOpenDialog(scene.getWindow());
    
            
            if (file != null)
            {
                this.lastFileDir = file.getParentFile();
                inputs.set_genome(file.toPath());
                inputs.remove_mapping_file(); //reset any previous mapping file selection
                return file.getName();
            }
            
            return null;
        }
        
        public String addIndexFileToGenome()
        {
            DatabaseWorkflowModule.getInstance().setMapper(DatabaseWorkflowModule.Mapper.BOWTIE);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select indexed Genome file");
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);

            
            File file = fileChooser.showOpenDialog(scene.getWindow());
    
            
            if (file != null)
            {
                String removeExtension = FilenameUtils.removeExtension(file.toString());
                String baseName = FilenameUtils.removeExtension(removeExtension);
                this.lastFileDir = file.getParentFile();
                inputs.set_genome(Paths.get(baseName));
                inputs.remove_mapping_file(); //reset any previous mapping file selection
                return baseName;
            }
            return null;
           // return "Add File";
            
        }
        /**
         * Use the File Chooser to select a mapping file (PatMan output) in place of
         * a genome.
         * @return 
         */
        public String choosePatmanOutput()
        {
            DatabaseWorkflowModule.getInstance().setMapper(DatabaseWorkflowModule.Mapper.PATMAN);
           
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select mapping file (PatMan output)");
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);

            File file = fileChooser.showOpenDialog(scene.getWindow());
            
            if (file != null)
            {
                this.lastFileDir = file.getParentFile();
                inputs.set_mapping_file(file.toPath());
                return file.getName();
            }
            
            return "Add File";
        }
        public String chooseBowtieOutput()
        {
            DatabaseWorkflowModule.getInstance().setMapper(DatabaseWorkflowModule.Mapper.BOWTIE);
           
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select mapping file (Bowtie output)");
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);

            File file = fileChooser.showOpenDialog(scene.getWindow());
            
            if (file != null)
            {
                this.lastFileDir = file.getParentFile();
                inputs.set_mapping_file(file.toPath());
                return file.getName();
            }
            
            return "Add File";
        }
                
        public void addBlank_sRNA(String id)
        {
            inputs.add_sRNA_replicate(id, Paths.get(""));
        }
        public void addBlank_deg(String id)
        {
            inputs.add_degradome_replicate(id, Paths.get(""), 0);
        }
        
        public void setDegDataType(String id, int location, int dataType)
        {
            inputs.set_degradome_type(id, location, dataType);
        }

        public int getNumberRepsForSample(String id)
        {
            return inputs.getSampleSize(id);
        }

        public JSObject addFileTo_sRNA_Sample(String id, int replicateID, JSObject jsobject)
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select sequence files for sample: " + id);
            
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);
        //        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.txt)", "*.txt");
            //        fileChooser.getExtensionFilters().add(extFilter);

            
            //File file = fileChooser.showOpenDialog(scene.getWindow());
            List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());
            
            
            if (files != null)
            {
                this.lastFileDir = files.get(0).getParentFile();
            
                // add each chosen file to the sample list
                for(int i = 0; i<files.size(); i++){
                    
                    if (replicateID < inputs.get_sRNA_samples().get(id).size()) {
                        inputs.set_sRNA_replicate(id, replicateID, files.get(i).toPath());
                    } else {
                        inputs.add_sRNA_replicate(id, files.get(i).toPath());
                    }
                    replicateID++;
                    // add each file to returned JS object
                    jsobject.setSlot(i, files.get(i).getName());
                }
                return jsobject;
            }
            // No files returned from chooser. No changes should happen in the JS
            jsobject.setSlot(0, "Add File");
            return jsobject;
        }
        public JSObject addFileTo_deg_Sample(String id, int replicateID, JSObject jsobject)
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select sequence files for sample: " + id);
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);

            List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());
                  
            if (files != null)
            {
                this.lastFileDir = files.get(0).getParentFile();
                // add each chosen file to the sample list
                for (int i = 0; i < files.size(); i++) {
                    if(inputs.get_degradomes().get(id).size() >= replicateID)
                    {
                        inputs.set_degradome_replicate(id, replicateID, files.get(i).toPath(), 0);
                    }
                    else
                    {
                        inputs.add_degradome_replicate(id, files.get(i).toPath(), 0);
                    }
                    replicateID++;
                    // add each file to returned JS object
                    jsobject.setSlot(i, files.get(i).getName());
                }
                return jsobject;
            }
            // No files returned from chooser. No changes should happen in the JS
            jsobject.setSlot(0, "Add File");
            return jsobject;
        }
        
        public void remove_sRNA_replicate(String sampleID, int replicateID)
        {
            inputs.remove_sRNA_replicate(sampleID, replicateID);
        }
        
        public void remove_deg_replicate(String sampleID, int replicateID) {
            inputs.remove_deg_replicate(sampleID, replicateID);
        }


        public String addFileTo_GFFList()
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select GFF files");
            if(this.lastFileDir != null)
                fileChooser.setInitialDirectory(this.lastFileDir);
        //        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sequence files (*.txt)", "*.txt");
            //        fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showOpenDialog(scene.getWindow());
            
            
            if (file != null)
            {
                this.lastFileDir = file.getParentFile();
                inputs.add_GFF(file.toPath());
                return file.getName();
            }
            return null;
            //return "Add File";
        }

        
        public boolean getConfiguredReferences()
        {
            if(!isFileManager())//only check for a genome if building a database 
            {
                if(!inputs.genomeIsValid(DatabaseWorkflowModule.getInstance().getMapper()))
                {
                    //System.out.println("no genome found");
                    if(inputs.get_mapping_file() == null) // Can have a mapping file entered
                        return false;

                }
            }
            return true;
        }
        public boolean checkAllConfiguredSRNA()
        {
            return inputs.sRNAsAreValid();
        }
        
        public int getMaxSamples()
        {
            return WorkflowManager.getInstance().maxSamples;
        }
        public int getMaxReplicates()
        {
            return WorkflowManager.getInstance().maxReplicates;
        }
        
        public boolean checkAllConfiguredDegradomes()
        {
            if(WorkflowManager.getInstance().degradomeOptional)
                return true;
            if(inputs.get_degradomes().isEmpty())
                return false;
            
            for(Entry<String, List<Pair<Path, Integer>>> e : inputs.get_degradomes().entrySet())
            {
                if(e.getValue().isEmpty())
                    return false;
                for (Pair<Path, Integer> f : e.getValue())
                {
                    if (f.getKey().toString().isEmpty() || !Files.exists(f.getKey()))
                    {
                        return false;
                    }
                }
            }  
            return true; 
        }
        
        
        public boolean getConfigured_sRNA_Sample(String id, int rowCount)
        {

            if(inputs.get_sRNA_samples().get(id).size() != rowCount)
                return false;
            
            for(Path f : inputs.get_sRNA_samples().get(id))
            {
                if(f.toString().isEmpty() || !Files.exists(f))
                    return false;
            }
            
            return true;
        }
        
        public boolean checkTranscriptomeFiles()
        {
            if(inputs.get_transcriptome() == null && WorkflowManager.getInstance().transcriptomeOptional)
                return true;
            if(inputs.get_transcriptome() == null && !WorkflowManager.getInstance().transcriptomeOptional)
                return false;
            return inputs.transcriptomeIsValid();
        }
        public boolean checkGFFFiles()
        {
            return inputs.GFFsAreValid();
        }

        
        public void finalise() throws DuplicateIDException, Exception
        {
            // Serialise the inputs. Currently only used to retrieve the last input setup.
            inputs.mapper = DatabaseWorkflowModule.getInstance().getMapper();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(previous_inputs.toFile()));
            out.writeObject(inputs);
            out.close();
            
            configureWorkflowData();
            
            WorkflowSceneController.setReadyNode(parentController.getModule().getID());
            
        }
        
        
        
        public void hideWizard()
        {
            ScreensController.getInstance().setScreen(parentController.getModule().getID());
        }
        
        public void setTableHTML(String table)
        {
            tableHTML = table;
            //System.out.println(table);
            
        }
        
        public void resetAnnotationFlag()
        {
            annotationsProcessed = false;
            
            // webEngine.executeScript("clearAnnotationList()");

        }
        //process annotations
        public void processAnnotations()
        {
            HTMLWizardViewController.this.processAnnotations();
        }
        
        
        public void insertAnnotation(String annotation)
        {   
            inputs.add_annotation(annotation);
            
        }
        
        public void insertOtherAnnotation(String annotation)
        {
            inputs.add_other_annotation(annotation);
        }
        
        public void askForResize()
        {
            wizardWebView.resize(visualBounds.getWidth(), visualBounds.getHeight());
//            mainAnchorPane.setPrefSize(0,0);
//            mainAnchorPane.setPrefSize(visualBounds.getWidth(), visualBounds.getHeight());
        }
        
        public void rebuildJSON()
        {

            JsonObjectBuilder jsonNode = Json.createObjectBuilder();
            jsonNode.add("name", "Experiment Root");
        
            JsonArrayBuilder sampleArray = Json.createArrayBuilder();
            for(Entry<String, List<Path>> sample : new TreeMap<>(inputs.get_sRNA_samples()).entrySet())
            {
                
                //sampleArray.add(BigDecimal.ZERO)
                JsonObjectBuilder jsonSampleNode = Json.createObjectBuilder();
                jsonSampleNode.add("name", sample.getKey());
                jsonSampleNode.add("type", "small RNA Sample");

                JsonArrayBuilder replicateArray = Json.createArrayBuilder();
                int repID = 0;
                for(Path p : sample.getValue())
                {
                    JsonObjectBuilder jsonReplicateNode = Json.createObjectBuilder();
                    jsonReplicateNode.add("name", p.getFileName().toString());
                    jsonReplicateNode.add("type", "small RNA Replicate");
                    jsonReplicateNode.add("id", sample.getKey()+"_replicate_"+repID);
                    replicateArray.add(jsonReplicateNode);
                    repID++;
                }
                jsonSampleNode.add("parents", replicateArray.build());
                sampleArray.add(jsonSampleNode);
                
                
            }
            jsonNode.add("parents", sampleArray.build());

            Path get = Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FileHierarchySQData.json");
            try
            {

                PrintWriter writer = new PrintWriter(get.toFile());
                try (JsonWriter jsonWriter = Json.createWriter(writer))
                {
                    jsonWriter.writeObject(jsonNode.build());
                }
                writer.flush();
                writer.close();
                parentController.reloadHierarchy();

            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

    }
    
}
