/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat2Params;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat2ServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author keu13sgu
 */
public class MiRCat2Module extends WorkflowModule{

    
    
    private Path genome = null;
    private GenomeManager genomeManager = null;
    private static Path outPath = null;
    
    private final StopWatch globalStopWatch = new StopWatch();
    

    private MiRCat2ServiceLayer miRCat2DB = (MiRCat2ServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("miRCat2ServiceLayer");
    boolean isActive = true;
    private Path sRNAFilePath;
    private boolean readyStatus = false;
    
    
    
    
    public MiRCat2Module(String id, String title,  Rectangle2D visualBounds) {
        super(id, title);

        //setDefaultParams();

        this.fxmlResource = "/fxml/MiRCat2Scene.fxml";
        this.controller = new MiRCat2SceneController(visualBounds, this);   
       
    }
    
    public void printLap(String label)
    {
        System.out.println(this.globalStopWatch.printLap(label));
    }
    

    
    public void setGenomePath(Path genome){
        this.genome = genome;
    }
    
    public static void setOutputDir(Path path){
        outPath = path;
    }
    public static Path getOutputDIR()
    {
        return outPath;
    }
        
    @Override
    protected synchronized void process() throws Exception {
        
        globalStopWatch.start();
        //genome file must be set before process() is called
        //Retrieve genome path from setup wizard
        if(!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine())
        {
            WorkflowSceneController.setWaitingNode(this.getID());
            if(!readyStatus)
                this.wait();
            WorkflowSceneController.setBusyNode(this.getID());
            ((MiRCat2SceneController) controller).setBusy(true);
            

        }

        this.configureInputs();
        isActive = true;
      
        
        this.printLap("Wating for user config");
        readGenome(genome);
        
        this.printLap("Reading genome file");
        
        String header = "Precursor Score, Chromosome, Sequence, Abundance, Start,"
                + " End, Strand, Mismatches, Hairpin Sequence, Hairpin Dot-Bracket, "
                + "Hairpin Start, Hairpin End, Hairpin MFE, Hairpin aMFE, p-Value, "
                + "Star Sequence, Star Abundance, Star Start, Star End, miRBase Precursor";
        
        String outputFileName = sRNAFilePath.getFileName().toString();
        int extIndex = outputFileName.lastIndexOf(".");
        
        if(extIndex > 0 ){
            outputFileName = outputFileName.substring(0, extIndex);
        }
        
        //outPath to be set from interface. 
        //This output method is temporary, will give more significant file names later
        try (BufferedWriter outPatman = new BufferedWriter(new FileWriter(outPath.toString() + DIR_SEPARATOR + outputFileName + "_output.patman"));
             BufferedWriter outCSV = new BufferedWriter(new FileWriter(outPath.toString() + DIR_SEPARATOR + outputFileName + "_output.csv"));  )
        {
            outCSV.append(header);
            outCSV.append("\n");
            
            miRCat2DB.process(genomeManager, outPatman, outCSV, ((MiRCat2SceneController)controller));
            this.printLap("miRCat2 Processing reads");
            outPatman.flush();
            outCSV.flush();
            
            outPatman.close();
            outCSV.close();
        }
        
        isActive = false;
        
        if(!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine())
        {
            ((MiRCat2SceneController) controller).setBusy(false);
            

        }
    }
    
    public synchronized void setReadyToContinue(boolean readyStatus)
    {
        if (readyStatus)
        {
            this.readyStatus = readyStatus;
            this.notifyAll();
        }
    }

    public boolean readyToContinue() {
        return isActive;
    }
    
     public void setGenome(GenomeManager genomeFile){
        genomeManager = genomeFile;
    }
    
      public void readGenome(Path genomeFile){
        try {
            genomeManager = new GenomeManager(genomeFile);
        } catch (IOException ex) {
            Logger.getLogger(MiRCat2ServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DuplicateIDException ex) {
            Logger.getLogger(MiRCat2ServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
      public GenomeManager getGenome(){
          return genomeManager;
      }

    
    
    public static void readParams(String paramFile) throws IOException {
            BufferedReader in = new BufferedReader(new FileReader(new File(paramFile)));
            String line;

            while ((line = in.readLine()) != null) {
                if(line.trim().isEmpty())
                    continue;
                String parts[] = line.split("=");
                switch(parts[0].trim().toLowerCase()){
                    case "repeats": 
                        MiRCat2Params.REPEATS = Integer.parseInt(parts[1].trim());
                        break;

                    case "complex": 
                        MiRCat2Params.complex = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "pval":
                        MiRCat2Params.pVal = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "minlen":
                        MiRCat2Params.minLen = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "maxlen":
                        MiRCat2Params.maxLen = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "minfoldlen":
                        MiRCat2Params.minFoldLen = Integer.parseInt(parts[1].trim());
                        break;
                        
//                    case "maxfoldlen":
//                        MiRCat2Params.lFoldL = Integer.parseInt(parts[1].trim());
//                        break;
                        
                    case "maxamfe":
                        MiRCat2Params.maxAmfe = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "gapsinmirna":
                        MiRCat2Params.gapsInMirna = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "mirstarpresent":
                        MiRCat2Params.miRStarPresent = (parts[1].trim().equals("true"));
                        break;
                        
                    case "maxfoldlen":
                        MiRCat2Params.foldDist = Integer.parseInt(parts[1].trim());
                        MiRCat2Params.lFoldL = MiRCat2Params.foldDist;
                        break;
                        
//                    case "window":
//                        MiRCat2Params.WINDOW = Integer.parseInt(parts[1].trim());
//                        break;
                        
                    case "allowcomplexloop":
                        MiRCat2Params.allowComplexLoop = (parts[1].trim().equals("true"));
                        break;
                        
                    case "noloops":
                        MiRCat2Params.noLoops = Integer.parseInt(parts[1].trim());
                        break;
                        
                    case "rudval":
                        MiRCat2Params.UDVAL = Double.parseDouble(parts[1].trim());
                        break;
                        
                    case "clearcutpercent":
                        MiRCat2Params.clearCutPercent = Double.parseDouble(parts[1].trim());
                        break;
                       
                    case "randfold":
                        MiRCat2Params.execRANDFold = (parts[1].trim().equals("true"));
                        break;
                            
                    default:
                        System.out.println("Param " + parts[0] + " is not a valid parameter");
                }
            }
  
        
        
    }
    
    public void setSpecificParams(boolean isPlant){
        if(isPlant){
            MiRCat2Params.WINDOW = 500;
        }
        else{
            MiRCat2Params.WINDOW = 300;
        }
    }

    public void write_miRNA_FASTA(Path toPath)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void write_Precursor_FASTA(Path toPath)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void writeTableCSV(Path toPath)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean configureInputs()
    {
        String next = FileHierarchyViewController.retrieveDataPaths().keySet().iterator().next();
        this.sRNAFilePath = FileHierarchyViewController.retrieveDataPaths().get(next).get(0);
        setGenomePath(FileHierarchyViewController.retrieveGenomePath());
        if (genome != null && outPath != null && Files.exists(genome) && Files.isDirectory(outPath))
        {
            
            return true;
        }
        if (!DatabaseWorkflowModule.getInstance().isDebugMode() && !AppUtils.INSTANCE.isCommandLine())
        {
            Platform.runLater(() ->
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Workbench Error");
                alert.setHeaderText("miRCat 2 Error State");
                alert.setContentText("miRCat has not been configured. Please set the genome and output path and check the parameters");

                alert.showAndWait();
            });

            LOGGER.log(Level.SEVERE, "miRCat has not been configured. Please set the genome and output path and check the parameters");
        }
        else
        {
            LOGGER.log(Level.SEVERE, "miRCat has not been configured. Please set the genome and output path and check the parameters");

        }
        return false;
    }

     
    
}
