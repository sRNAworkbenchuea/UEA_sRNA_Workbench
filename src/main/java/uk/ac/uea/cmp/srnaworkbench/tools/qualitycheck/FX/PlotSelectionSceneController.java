/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.FX;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import netscape.javascript.JSObject;
import uk.ac.uea.cmp.srnaworkbench.FX.ControlledScreen;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.FX.JavascriptBridge;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF.ReportWorkflowModule;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.fop.svg.PDFTranscoder;
import org.controlsfx.control.Notifications;
/**
 * FXML Controller class
 *
 * @author w0445959
 */
public class PlotSelectionSceneController implements Initializable, ControlledScreen
{

    /*
     * FXML injectable fields start.
     */
    //web views for each plot type

    @FXML
    private WebView mainWebView;
    
    @FXML 
    private AnchorPane mainAnchorPane;
    
    /*
    * FXML injectable fields end.
    */
    
    
    private ScreensController myController;
    
    private Scene scene;
    

    //engines for each plot type
    private WebEngine mainWebEngine;
    
    private ReportWorkflowModule myModule;
    
    private Rectangle2D webViewSize;
    
    private String stageName;
    private QCJavascriptReceiver qcJavascriptReceiver;
    
    public PlotSelectionSceneController(ReportWorkflowModule module)
    {
        this(module, new Rectangle2D(200,200,200,200));
    }
    
    public PlotSelectionSceneController(ReportWorkflowModule module, Rectangle2D size)
    {
        myModule = module;
        webViewSize = size;
    }
    
    /*
     * FXML injectable handlers start.
     */
    @FXML
    private void goToMain(ActionEvent event)
    {
        myController.setScreen(WorkflowViewer.MAIN_SCREEN);
    }
    /*
     * FXML injectable handlers end.
     */

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }
    
    private void refreshMainView()
    {
        try
        {

            final URL h_view = new URL("file:" + Tools.WEB_SCRIPTS_DIR + "/HTML/PlotMainView.html" + "?" + this.stageName);

            mainWebEngine.load(h_view.toExternalForm());
        }
        catch (MalformedURLException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private void captureSnapShot()
    {
//        File destFile = new File("test.png");
//        WritableImage snapshot = boxPlotWebView.snapshot(new SnapshotParameters(), null);
//        RenderedImage renderedImage = SwingFXUtils.fromFXImage(snapshot, null);
//        try
//        {
//            ImageIO.write(renderedImage, "png", destFile);
//            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//
//            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", byteOutput);
//
//            com.itextpdf.text.Image graph;
//            graph = com.itextpdf.text.Image.getInstance(byteOutput.toByteArray());
//        }
//        catch (IOException ex)
//        {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
//        catch (BadElementException ex)
//        {
//            Logger.getLogger(PlotSelectionSceneController.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public void resetPlotData()
    {
        
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        assert mainWebView != null : "fx:id=\"mainWebView\" was not injected: check your FXML file 'PlotSelectionScene.fxml'.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'PlotSelectionScene.fxml'.";

        mainWebEngine = mainWebView.getEngine();
  
        mainAnchorPane.setPrefSize(webViewSize.getWidth(), webViewSize.getHeight());
        
        
         mainWebEngine.setOnAlert((WebEvent<String> arg0) -> {
            System.out.println("Plot Selection Event: " + arg0);
        });
        
         mainWebEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
            
            qcJavascriptReceiver = new QCJavascriptReceiver(this.myController);
            if (newState == State.SUCCEEDED) {
                
                System.out.println("running script: " + stageName);
                JSObject window = (JSObject) mainWebEngine.executeScript("window");
                window.setMember("app", qcJavascriptReceiver);
                
                
            }
            
        });
         
         refreshMainView(); 
         
         //mainWebEngine.executeScript("setup()");
        
//        refreshBoxPlotView();
//        refreshSeqDistView();
//        refreshMAPlotView();
        
    }    
    
    public void callSetup()
    {
        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            Platform.runLater(() ->
            {
                mainWebEngine.executeScript("setup()");
            });
        }
    }
    
    @Override
    public void setStageAndSetupListeners(Scene scene)
    {
        this.scene = scene;
    }

    @Override
    public void setScreenParent(ScreensController screenPage)
    {
        myController = screenPage;
    }

    @Override
    public JFXStatusTracker getStatusTracker()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void disableMAMenu()
    {
        if(!DatabaseWorkflowModule.getInstance().isDebugMode())
            this.mainWebEngine.executeScript("disableMAMenu()");
    }

    @Override
    public void workflowStartedListener() {
  //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class QCJavascriptReceiver extends JavascriptBridge {
        
        

        public QCJavascriptReceiver(ScreensController controller)
        {
            super(controller);
        }
        
        public String getStageName()
        {
            System.out.println("getting stage name: " + stageName);
            return stageName;
        }
        
        public void loadBrowserVersion()
        {
            String url = "file://"+Tools.ROOT_DIR+"/data/web/HTML/PlotMainView.html?"+stageName;

            if (Desktop.isDesktopSupported())
            {
                Desktop desktop = Desktop.getDesktop();
                try
                {
                    desktop.browse(new URI(url));
                }
                catch (IOException | URISyntaxException e)
                {
                    LOGGER.log(Level.WARNING, "Help pages cannot be loaded{0}", e.getMessage());
                }

            }
            else
            {
                try
                {
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec("xdg-open " + url);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        /**
         * Allows the user to select a PDF or SVG file to write the chosen svg to
         * @param svgXML the svg that will be written out
         * @throws IOException
         * @throws TranscoderException 
         */
        public void saveToPDF(String svgXML) throws IOException, TranscoderException {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Graph");
            String pdfDesc = "Portable Document Format (pdf)";
            ExtensionFilter pdf = new ExtensionFilter(pdfDesc, "*.pdf");
            String svgDesc = "Scalable Vector Graphics (svg)";
            ExtensionFilter svg = new ExtensionFilter(svgDesc, "*.svg");

            
            fileChooser.getExtensionFilters().addAll(pdf,svg);
            File file = fileChooser.showSaveDialog(scene.getWindow());
            
            if(fileChooser.getSelectedExtensionFilter().getDescription().equals(svgDesc)){
                // Write SVG to file
                Files.write(Paths.get(file.toString()), svgXML.getBytes("UTF-8"));
            }
            else
            {
                // Write PDF to file
                Transcoder transcoder = new PDFTranscoder();
                try (InputStream toInputStream = IOUtils.toInputStream(svgXML, "UTF-8");
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(file.toString()))) {
                    TranscoderInput transcoderInput = new TranscoderInput(toInputStream);

                    TranscoderOutput transcoderOutput = new TranscoderOutput(fileOutputStream);
                    transcoder.transcode(transcoderInput, transcoderOutput);

                }
            }

        }
        
        /**
         * Called if the user tries to export without selecting one first.
         * Creates a non-intrusive toast as a reminder of why nothing can be exported
         */
        public void noChartSelected()
        {
            Notifications n = Notifications.create()
                    .title("No chart selected")
                    .position(Pos.BOTTOM_CENTER)
                    .text("Please select a chart from the Plot Selection menu to export");
            Platform.runLater(() -> n.showInformation());
        }
    }
    

    
    
    
}
