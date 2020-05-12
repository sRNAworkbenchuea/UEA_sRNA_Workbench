/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.AttributesExtracter;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF.MiRCat2Module;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;



import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.net.util.Base64;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class MiRCat2SceneController implements Initializable, ControlledScreen
{
    
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView mainWebView;
    
    private Scene scene;

    private ScreensController myController;

    private Rectangle2D webViewSize;

    private WebEngine mainWebEngine;

    private JFXStatusTracker tracker = new JFXStatusTracker();
    
    private final MiRCat2Module parentModule;
    private AttributesExtracter ae;
    private MiRCat2JavascriptReceiver myJSBridge;


    public MiRCat2SceneController(Rectangle2D size, MiRCat2Module engine)
    {
        this.webViewSize = size;
        
        this.parentModule = engine;
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        assert this.mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'MiRCat2Scene.fxml'.";

        assert this.mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'MiRCat2Scene.fxml'.";
        assert this.mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'MiRCat2Scene.fxml'.";

        this.mainWebEngine = this.mainWebView.getEngine();

        this.mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) ->
        {
            this.myJSBridge = new MiRCat2JavascriptReceiver(this.myController);
            if (newState == State.SUCCEEDED)
            {
                JSObject window = (JSObject) this.mainWebEngine.executeScript("window");
                window.setMember("app", myJSBridge);
            }
        });
        this.mainAnchorPane.setPrefSize(this.webViewSize.getWidth(), this.webViewSize.getHeight());
        
        this.mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("miRCAT2 Event: " + arg0);
        });

        

        refreshMainView();

    }    
    
    private void refreshMainView()
    {

        try
        {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/MiRCat2View.html");
            //final URL h_view = new URL("http://www.google.com");

            this.mainWebEngine.load(h_view.toExternalForm());
            //  mainWebEngine.loadContent("<html><body>This is a test</body></html>");
        }
        catch (MalformedURLException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        this.myController = screenPage;
    }

    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        this.scene = scene;
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        return tracker;
    }
    
     public void setBusy(boolean state)
    {
        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            Platform.runLater(() ->
            {
                this.mainWebEngine.executeScript("setBusy( '" + state + "' )");
            });
        }

    }

    @Override
    public void workflowStartedListener()
    {
    }

    
    public void outputToGUI(String printOneSRNACSV)
    {
        Platform.runLater(() ->
        {
            String[] tokens = printOneSRNACSV.split(",");

            this.mainWebEngine.executeScript(
                    "addToData('"
                    + tokens[0] + "','"
                    + tokens[1] + "','"
                    + tokens[2] + "','"
                    + tokens[3] + "','"
                    + tokens[4] + "','"
                    + tokens[5] + "','"
                    + tokens[6] + "','"
                    + tokens[7] + "','"
                    + tokens[8] + "','"
                    + tokens[11] + "','"
                    + tokens[9] + "','"
                    + tokens[10] + "','"
                    + tokens[12] + "','"
                    + tokens[13] + "','"
                    + tokens[14] + "','"
                    + tokens[15] + "','"
                    + tokens[16] + "','"
                    + tokens[17] + "','"
                    + tokens[18] + "','"
                    + tokens[19] + "')");

        });
    }

  
    
    public class MiRCat2JavascriptReceiver extends JavascriptBridge {
        
        private File lastFileDir = null;
        private final AlignedSequenceServiceImpl alignedService = 
                (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");
        
        private final UniqueSequencesServiceImpl uniqueService = 
                (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
        private Path alignmentTotalExportFile = null;
        private File coverageExportDIR = null;
        private ArrayList<File> fullCoverageList = new ArrayList<>();


        
        public MiRCat2JavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        
//        public void setOrgType(boolean plant)
//        {
//            System.out.println("org type set: " + plant);
//            parentModule.setOrganismType(plant);
//        }
        
        public void setOutputDir()
        {
            if(parentModule.isPAREfirst){
                File selectedDirectory = new File(Tools.PAREfirst_DATA_Path + DIR_SEPARATOR + parentModule.getID() + "_output");
                selectedDirectory.mkdir();
                MiRCat2Module.setOutputDir(selectedDirectory.toPath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("miRCat2 Output Directory");
                alert.setHeaderText("miRCat2 output directory Message");
                alert.setContentText("miRCat2 output directory is set to: " + selectedDirectory.getAbsolutePath());

                alert.showAndWait();
            } else {
                DirectoryChooser chooser = new DirectoryChooser();
                if (this.lastFileDir != null) {
                    chooser.setInitialDirectory(this.lastFileDir);
                }
                chooser.setTitle("Select directory to save results");

                File selectedDirectory = chooser.showDialog(scene.getWindow());

                if (selectedDirectory != null) {
                    this.lastFileDir = selectedDirectory;
                    MiRCat2Module.setOutputDir(selectedDirectory.toPath());

                }
            }
            
        }
        
        public boolean checkReadyStatus()
        {
            return parentModule.configureInputs();
        }
        
        public void setContinue(boolean state) throws Exception
        {
            //System.out.println("ready status: " + readyStatus);
            
            parentModule.setReadyToContinue(state);
            WorkflowSceneController.setReadyNode(parentModule.getID());
        }
        
        public void exportMiRNAToFASTA()
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export FASTA");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                parentModule.write_miRNA_FASTA(selectedDirectory.toPath());

            }
            
        }
        
        public void exportPreCursorToFASTA()
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export FASTA");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                parentModule.write_Precursor_FASTA(selectedDirectory.toPath());

            }
            
        }
        public void exportAlignments() 
        {
            FileChooser chooser = new FileChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select file to save all Alignments as text");
            
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            chooser.getExtensionFilters().add(extFilter);

            File selectedFile = chooser.showSaveDialog(scene.getWindow());

            if (selectedFile != null) {
                this.lastFileDir = selectedFile.getParentFile();
                this.alignmentTotalExportFile = selectedFile.toPath();
                try
                {
                    Files.write(this.alignmentTotalExportFile,("").getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                catch (UnsupportedEncodingException ex)
                {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
                catch (IOException ex)
                {
                    Logger.getLogger(MiRCat2SceneController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }
                    
        public void exportTableToCSV() 
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export all alignments as text");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                parentModule.writeTableCSV(selectedDirectory.toPath());

            }

        }
        
        public String getSequenceAlignments(int hairpinStart, int hairpinEnd, String chrom, int targetSequenceStart, int targetSequenceEnd, String strand,
                                                String hairpinSequence, String hairpinStructure, double amfe)
        {
            Aligned_Sequences_Entity.Id id = new Aligned_Sequences_Entity.Id(targetSequenceStart, targetSequenceEnd, chrom, strand, "genome");
            Aligned_Sequences_Entity targetAlignment = this.alignedService.findById(id);
            String findSequencesSQL = "SELECT * FROM ALIGNED_SEQUENCES where SEQ_START >= " + hairpinStart + " and SEQ_END <= " + hairpinEnd + " AND chrom='" 
                    + chrom + "' ORDER BY SEQ_START";
            List<Aligned_Sequences_Entity> allSequencesUnderHairpin = alignedService.getAlignedSequencesFromSQL(findSequencesSQL);
            StringBuilder alignments = new StringBuilder();
            alignments.append(targetAlignment.getChromosome()).append("_").append(hairpinStart).append("-").append(hairpinEnd).append(LINE_SEPARATOR);
            //find hairpin data
            alignments.append(hairpinSequence).append(LINE_SEPARATOR);
            for (Aligned_Sequences_Entity a_e : allSequencesUnderHairpin)
            {
                Unique_Sequences_Entity unique_seqeunce = uniqueService.findById(a_e.getRna_seq());
                String rna_sequence = unique_seqeunce.getRNA_Sequence();
                //int startPos = hairpinSequence.indexOf(rna_sequence);
                int startPos = -1;
                if(a_e.isNegative())
                {
                    startPos = hairpinEnd - a_e.getEnd();
                }
                else
                {
                    startPos = a_e.getStart() - hairpinStart;
                }
                String leftPad = StringUtils.leftPad(rna_sequence, startPos + rna_sequence.length(), '.');
                String finalAlignment = StringUtils.rightPad(leftPad, hairpinSequence.length(), '.');
                alignments.append(finalAlignment).append(" ").append(unique_seqeunce.getTotalCount()).append(LINE_SEPARATOR);
            }
            alignments.append(hairpinStructure).append(" ").append(amfe);
  
            return alignments.toString();
            
        }
        
        public void writeAlignments(int hairpinStart, int hairpinEnd, String chrom, int targetSequenceStart, int targetSequenceEnd, String strand,
                                                String hairpinSequence, String hairpinStructure, double amfe)
        {
            try
            {
                Aligned_Sequences_Entity.Id id = new Aligned_Sequences_Entity.Id(targetSequenceStart, targetSequenceEnd, chrom, strand, "genome");
                Aligned_Sequences_Entity targetAlignment = this.alignedService.findById(id);
                String findSequencesSQL = "SELECT * FROM ALIGNED_SEQUENCES where SEQ_START >= " + hairpinStart + " and SEQ_END <= " + hairpinEnd + " AND chrom='"
                        + chrom + "' ORDER BY SEQ_START";
                List<Aligned_Sequences_Entity> allSequencesUnderHairpin = alignedService.getAlignedSequencesFromSQL(findSequencesSQL);
                Files.write(this.alignmentTotalExportFile,(targetAlignment.getChromosome() + "_" + hairpinStart + "-" + hairpinEnd + LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.APPEND);
                //find hairpin data
                Files.write(this.alignmentTotalExportFile,(hairpinSequence + LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.APPEND);
                for (Aligned_Sequences_Entity a_e : allSequencesUnderHairpin)
                {
                    Unique_Sequences_Entity unique_seqeunce = uniqueService.findById(a_e.getRna_seq());
                    String rna_sequence = unique_seqeunce.getRNA_Sequence();
                    //int startPos = hairpinSequence.indexOf(rna_sequence);
                    int startPos = -1;
                    if(a_e.isNegative())
                    {
                        startPos = hairpinEnd - a_e.getEnd();
                    }
                    else
                    {
                        startPos = a_e.getStart() - hairpinStart;
                    }
                    String leftPad = StringUtils.leftPad(rna_sequence, startPos + rna_sequence.length(), '.');
                    String finalAlignment = StringUtils.rightPad(leftPad, hairpinSequence.length(), '.');
                    Files.write(this.alignmentTotalExportFile,(finalAlignment + " " + unique_seqeunce.getTotalCount() + LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.APPEND);
                }
                Files.write(this.alignmentTotalExportFile,(hairpinStructure + " " + amfe + LINE_SEPARATOR + LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.APPEND);
                
            }
            catch (UnsupportedEncodingException ex)
            {
                Logger.getLogger(MiRCat2SceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(MiRCat2SceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        public String writeSequenceCoverage(int hairpinStart, int hairpinEnd, String chrom, String hairpinSequence)
        {
            try
            {
                
                String TSV_OUTPUT_PATH = Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "TSV" + DIR_SEPARATOR + System.nanoTime() + "individualCoverage.tsv";
                //FileUtils.cleanDirectory(new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "TSV"));
                
                File tempEntry = new File(TSV_OUTPUT_PATH);
                tempEntry.deleteOnExit();
                //Random rand = new Random();
                Files.write(tempEntry.toPath(), ("hairpincoord\tshortreadabundance"+LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                String findSequencesSQL = "SELECT * FROM ALIGNED_SEQUENCES where SEQ_START >= " + hairpinStart + " and SEQ_END <= " + hairpinEnd + " AND chrom='" 
                    + chrom + "'";
                List<Aligned_Sequences_Entity> allSequencesUnderHairpin = alignedService.getAlignedSequencesFromSQL(findSequencesSQL);
                int hairpinLength = hairpinSequence.length();
                
                //java int default val is 0 so no need to initialise
                int[] XY = new int[hairpinLength+1];
                for(Aligned_Sequences_Entity a_e:allSequencesUnderHairpin)
                {
                    String currentRead = a_e.getRna_seq();
                    Unique_Sequences_Entity unique_sequence = uniqueService.findById(currentRead);
                    int startRange = hairpinSequence.indexOf(currentRead)+1;
                    for (int i = startRange; i < (startRange + currentRead.length()); i++)
                    {
                        XY[i] += unique_sequence.getTotalCount();
                    }
//                    for(char c : currentRead.toCharArray())
//                    {
//                        int startPos = hairpinSequence.indexOf(c, startRange);
//                        if(startPos <= startRange + currentRead.length())
//                            XY[startPos] += unique_seqeunce.getTotalCount();
//
//                    }
                    

                }
                for (int i = 0; i < XY.length; i++)
                {
                    Files.write(tempEntry.toPath(), (i + "\t" + XY[i] +LINE_SEPARATOR).getBytes("utf-8"), StandardOpenOption.APPEND);

                }
                
               

                return TSV_OUTPUT_PATH;
            }
            catch (UnsupportedEncodingException ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
            return null;
            
        }
        
        public void selectSeqCoverageDir()
        {
            DirectoryChooser chooser = new DirectoryChooser();
            if (this.lastFileDir != null) {
                chooser.setInitialDirectory(this.lastFileDir);
            }
            chooser.setTitle("Select directory to export coverage plots to PDF");

            File selectedDirectory = chooser.showDialog(scene.getWindow());

            if (selectedDirectory != null) {
                this.lastFileDir = selectedDirectory;
                this.coverageExportDIR = selectedDirectory;
            }
            
        }
       
        public void mergeFiles()
        {
            File destFile = new File(coverageExportDIR.getAbsolutePath() + DIR_SEPARATOR + "Total_Coverage_Plots.pdf");
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            for (File file : fullCoverageList)
            {
                
                pdfMerger.addSource(file);
            }
            if (destFile.exists())
            {
                destFile.delete();
            }
            pdfMerger.setDestinationFileName(destFile.getAbsolutePath());
            try
            {
                pdfMerger.mergeDocuments();
                for (File file : fullCoverageList)
                {

                    file.delete();
                }
            }
            catch (IOException ex)
            {
                Logger.getLogger(MiRCat2SceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (COSVisitorException ex)
            {
                Logger.getLogger(MiRCat2SceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        public void setupParams(String params, boolean isPlant) throws Exception
        {
            Path outputDIR = Paths.get(MiRCat2Module.getOutputDIR().toString(), "miRCATRunParameters.cfg");
            String replaceAll = params.replaceAll(",", LINE_SEPARATOR);
            Files.write(outputDIR, replaceAll.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
              
            MiRCat2Module.readParams(outputDIR.toString());
            
            
            parentModule.setSpecificParams(isPlant);
            this.setContinue(true);
        }
        
        public boolean writeSingleSVG(String svgXML, String id, boolean saveFile)
        {
            id = id.replaceAll("[^a-zA-Z0-9.-]", "_");
            File file = new File(coverageExportDIR.getAbsolutePath() + DIR_SEPARATOR + id + ".pdf");
            
            Transcoder transcoder = new PDFTranscoder();
            try (InputStream toInputStream = IOUtils.toInputStream(svgXML, "UTF-8");
                    FileOutputStream fileOutputStream = new FileOutputStream(file))
            {
                //Files.deleteIfExists(newPath);

                //Files.createFile(newPath);

                TranscoderInput transcoderInput = new TranscoderInput(toInputStream);

                TranscoderOutput transcoderOutput = new TranscoderOutput(fileOutputStream);
                transcoder.transcode(transcoderInput, transcoderOutput);
                
                if(saveFile)
                {
                    this.fullCoverageList.add(file);
                }
                //System.out.println("PRINTED: " + svgXML);
                return true;

            }
            catch (FileNotFoundException ex)
            {
                LOGGER.log(Level.SEVERE, "miRCat export coverage to PDF error{0}", ex.getMessage());
            }
            catch (IOException | TranscoderException ex)
            {
                LOGGER.log(Level.SEVERE, "miRCat export coverage to PDF error{0}", ex.getMessage());
            }
            return false;
        }
        
        public String getPlantDefaultPath()
        {
            return Tools.ROOT_DIR + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "default_params" + DIR_SEPARATOR + "default_miRCat2_plant_params.cfg";
        }
        public String getAnimalDefaultPath()
        {
            return Tools.ROOT_DIR + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "default_params" + DIR_SEPARATOR + "default_miRCat2_animal_params.cfg";

        }
        
        public void saveB64Encode(String dataRAW, String id)
        {
            try
            {
       
                FileChooser chooser = new FileChooser();
                if (this.lastFileDir != null) {
                    chooser.setInitialDirectory(this.lastFileDir);
                }
                chooser.setTitle("Select file to save precursor to png");

                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
                chooser.getExtensionFilters().add(extFilter);
                chooser.setInitialFileName(id);

                File selectedFile = chooser.showSaveDialog(scene.getWindow());

                if (selectedFile != null)
                {

                    String base64Image = dataRAW.split(",")[1];

                    // Convert the image code to bytes.
                    byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                    ImageIO.write(bufferedImage, "png", selectedFile);
                }

                
            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE,ex.getMessage());
            }
        }
        
        public String loadParameters() {
            String filePath = "";
            try {
                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Select saved miRCat2 configuration file");

                if (this.lastFileDir != null) {
                    fileChooser.setInitialDirectory(this.lastFileDir);
                }

                //File file = fileChooser.showOpenDialog(scene.getWindow());
                File file = fileChooser.showOpenDialog(scene.getWindow());
                if(file.exists()){
                    System.out.println(file.getAbsoluteFile());
                    if(MiRCat2Module.readParams(file.getAbsolutePath())){
                        filePath = file.getAbsolutePath();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return filePath;
        }
                
    }
}
