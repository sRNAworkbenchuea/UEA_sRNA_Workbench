package uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NoSuchExpressionValueException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.FX.NormalisationController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationParams;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
/**
 * Normalises counts using one of several implemented normalisation methods and persists
 * these to the database for later retrieval.
 * 
 * This workflow currently does not support persisting several different runs of
 * the same normalisation at once. i.e. if one total count normalisation is run 
 * for mapped sequences for file A and B and another normalisation is run for mapped
 * sequences for files C and D, only files C and D would have total count normalisations.
 * Normalisation for A and B would be removed. This is to prevent confusing two 
 * different runs which may give differing results per sequence due to differing 
 * parameters.
 * 
 * @author w0445959, mka07yyu
 */
@Transactional
public class NormalisationWorkflowServiceModule extends WorkflowModule
{

    
    NormalisationServiceLayer normService = (NormalisationServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("NormalisationService");
    
    private NormalisationParams params = new NormalisationParams();
    private Collection<String> filenames;
    private static List<NormalisationType> types = new ArrayList<>();
    private String referenceSample = "";
    private Map<String, Double> libSizes = new HashMap<>();
    private AnnotationSet annotations;
    private boolean readyToContinue = false;
    
    private static final Charset charset = Charset.forName("UTF-8");
    
    public NormalisationWorkflowServiceModule(String id, Rectangle2D visualBounds) {
       super(id, "Normalisation");
       
       this.fxmlResource = "/fxml/NormalisationScene.fxml";
        this.controller = new NormalisationController(visualBounds, this);
        
        setTracker(controller.getStatusTracker());
    }

    public NormalisationWorkflowServiceModule(String id)
    {
       this(id, new Rectangle2D(0,0,0,0));
    }
    
    public void setSamples(Collection<String> filenames)
    {
        this.filenames = filenames;
    }
    
    public void setAnnotations(AnnotationSet annotations)
    {
        this.annotations = annotations;
    }
    
    public void setNormalisationTypes(List<NormalisationType> normTypes)
    {
        types = normTypes;
    }
    
    public void setParamaters(NormalisationParams parameters)
    {
        this.params = parameters;
    }
    
    /**
     * Use this method to resolve the reference sample from any default/automatic
     * value to a calculated value. Does nothing if the reference sample is already
     * properly set.
     * @return 
     */
    private String resolveReferenceSample()
    {
        if (referenceSample.equals("") || referenceSample.equals(NormalisationParams.STR_AUTO_CHOOSE)) {
            referenceSample = normService.calculateReferenceSample(annotations.getTypes());
        }
        return referenceSample;
    }
    
    private Map<String, Double> resolveLibSizes()
    {
        if(libSizes.isEmpty())
        {
            FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");

            // keep track of normalised lib sizes to add to file entity later
            libSizes = fserv.getTotalExpression(this.filenames, NormalisationType.NONE, annotations.getTypes());
            DatabaseWorkflowModule.lap("Retrieving total expressions for files");
        }
        return libSizes;
    }
    
    public static List<NormalisationType> getNorms()
    {
        return types;
    }
    
  /**
   * Switches on NormalisationType to execute the correct "performNormalisation"
   * method
   * @param type
   */
  protected long performNormalisation(NormalisationType type, BufferedWriter fout, long currentId) throws NormalisationException, IOException
  {
    // Don't do anything if NONE!
    if(type.equals(NormalisationType.NONE))
    {
        LOGGER.log( Level.WARNING, "Normalisation mode NONE selected, no normalisation action performed" );
    }
    else
    {
        try {
            // remove any values made by any previous run of the same normalisation
            if(normService.removeNormalisation(type))
            {
                LOGGER.log(Level.WARNING, "Expression values calculated by normalisation {0} will be overwritten.", type);
            }
            Map<String, Double> totals;
            switch ( type )
            {
                case TOTAL_COUNT:
                    totals = resolveLibSizes();
                    currentId = normService.performTotalCountNormalisation( filenames, totals, annotations, params.isWeightByHits(), fout, currentId);
                    break;
                case UPPER_QUARTILE:
                    currentId = normService.performUpperQuartileNormalisation(filenames, annotations, fout, currentId);
                    break;
                case TRIMMED_MEAN:
                    String ref = resolveReferenceSample();
                    DatabaseWorkflowModule.lap("Finding reference sample");
                    totals = resolveLibSizes();
                    currentId = normService.performTrimmedMeanNormalisation(filenames, totals, ref, annotations, params.getMtrim(), params.getAtrim(), params.isWeightFactors(), fout, currentId);
                    break;
                case QUANTILE:
                    currentId = normService.performQuantileNormalisation(filenames, annotations, fout, currentId);
                    break;
                case BOOTSTRAP:
                    totals = resolveLibSizes();
                    currentId = normService.performBootstrapNormalisation(filenames, totals, annotations, fout, currentId);
                    break;
                case DESEQ:
                    currentId = normService.performDEseqNormalisation(filenames, annotations, fout, currentId);
                    break;
                default:
                    LOGGER.log( Level.WARNING, "No normalisation mode found, uninitialised variable? no action performed" );
            }
        } catch (NoSuchExpressionValueException ex) {
            LOGGER.log(Level.SEVERE, "One or more expression values were not found in the database when they should have been. " + type.getFullName() + " normalisation will not be run.", ex);
            throw new NormalisationException("Some expression values were not found where they should be.");
        }
    }
    return currentId;
  }

    @Override
    public synchronized void process() throws WFModuleFailedException, NormalisationException, AnnotationNotInDatabaseException, IOException, InterruptedException
    {
        
        
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            WorkflowSceneController.setWaitingNode(this.getID());
            this.wait();
            WorkflowSceneController.setBusyNode(this.getID());
        }
        else
            readyToContinue = true;
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
            ((NormalisationController)controller).setBusy(true);
        
        // Check all filenames, annotation types, normalisation types
        if(filenames == null || this.filenames.isEmpty())
        {
            FilenameServiceImpl fserv = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
            if(DatabaseWorkflowModule.getInstance().isDebugMode())
            {
                
                filenames = fserv.getFileIDs();
            }
            else
            {
                //DatabaseWorkflowModule.getMySamples();
                List<String> selectedFiles = FileReviewWorkflowModule.getSelectedFiles();
                
        
                filenames = fserv.getFileIDs(selectedFiles);
                if(filenames.isEmpty())//perhaps no file review node was created?
                {
                    filenames = fserv.getFileIDs();
                }
            }
            if(filenames.isEmpty())
                throw new NormalisationException("No samples were specified for normalisation");
        }
        if(this.annotations == null)
        {
            annotations = new AnnotationSet("All_Annotations");
            AnnotationService aServ = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
            AnnotationSet none = aServ.getTypesForReference(ReferenceSequenceManager.NO_REFERENCE_NAME);
            
            
            List<String> standardAnnotationStrings = FileHierarchyViewController.getAnnotations();
            AnnotationSet selected_annotations = new AnnotationSet("Selected");
            for(String annot : standardAnnotationStrings)
                selected_annotations.addAnnotationType(annot);
            
            List<String> otherAnnotationStrings = FileHierarchyViewController.getOtherAnnotations();
            AnnotationSet others = new AnnotationSet("Other");
            for(String annot : otherAnnotationStrings)
                others.addAnnotationType(annot);
            
            AnnotationSetList annotationSetList = new AnnotationSetList();
            annotationSetList.addAnnotationSet(AnnotationSet.getAllSet());
            annotationSetList.addAnnotationSet(AnnotationSet.getMappedSet());
            annotationSetList.addAnnotationSet(none);
            annotationSetList.addAnnotationSet(others);
            annotationSetList.addAnnotationSet(selected_annotations);
            
            //loop over genome set
            for(String annot_type : selected_annotations.getTypes())
            {
                annotationSetList.addType(annot_type);
            }
            for(String type : annotationSetList.getAllTypes())
            {
                annotations.addAnnotationType(type);
            }
            
        }
        if(types.isEmpty())
        {
            if(DatabaseWorkflowModule.getInstance().isDebugMode())
            {
                //types.add(NormalisationType.TOTAL_COUNT);
                //types.add(NormalisationType.BOOTSTRAP);
                //types.add(NormalisationType.QUANTILE);
                //types.add(NormalisationType.DESEQ);
                //types.add(NormalisationType.TRIMMED_MEAN);
                types.add(NormalisationType.UPPER_QUARTILE);
               
                //for a test of all normalisations, remove and add individuals if required
                //types = ((NormalisationController)controller).getNorms();
            }
            else
            {
                types = ((NormalisationController)controller).getNorms();
            }
        }
        Path path = Paths.get("testExpressionOut.csv");
        Files.deleteIfExists(path);
        BufferedWriter fout = Files.newBufferedWriter(path, charset, StandardOpenOption.CREATE);
        long currentId = normService.writeExpressionTableToCSV(fout, 0);
        
        // Run for all normalisation types
        for(NormalisationType type : types)
        {
            currentId = performNormalisation(type, fout, currentId);
            DatabaseWorkflowModule.getInstance().printLap("Performing normalisation " + type.getFullName());
        }
        fout.close();
        normService.readExpressionTable(path);
        DatabaseWorkflowModule.lap("Read normalised expression back to database");
        ((NormalisationController)controller).setBusy(false);
        this.readyToContinue = true;
        //writeFASTA(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/normTests"));
        //writeCSV(Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/normTests"));
    }

    public boolean readyToContinue()
    {
        return readyToContinue;
    }

    public void writeCSV(Path location)
    {
        try
        {
            normService.writeAllToCSV(filenames, location, annotations, types);
        }
        catch (IOException ex)
        {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("Module Export Error");
                alert.setContentText("The CSV Filenames already exist");

                alert.showAndWait();
            });
            LOGGER.log(Level.WARNING, "Normalised files cannot be wriiten to CSV.", ex);
        }
    }

    public void writeFASTA(Path location)
    {
        try
        {
            normService.writeAllToFASTA(filenames, location, annotations, types);
        }
        catch (IOException ex)
        {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("Module Export Error");
                alert.setContentText("The FASTA Directories or Filenames already exist");

                alert.showAndWait();
            });
            LOGGER.log(Level.WARNING, "Normalised files cannot be wriiten to FASTA.", ex);

        }

    }

    public synchronized void setReadyToContinue(boolean readyStatus)
    {
        if (readyStatus)
        {
            this.notifyAll();
        }
    }
    
}
