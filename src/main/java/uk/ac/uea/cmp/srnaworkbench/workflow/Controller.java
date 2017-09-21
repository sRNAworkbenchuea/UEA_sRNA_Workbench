/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import netscape.javascript.JSObject;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.MessageBox;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;

/**
 *
 * @author Chris Applegate
 */
public abstract class Controller implements Initializable {

    private ScreensController myController;
    private String htmlFile;
    protected Scene scene;
    private WebEngine engine;
    private Object jsClass;
    private static final int V_SCROLL_BUTTON_HEIGHT = 25;
    @FXML
    private AnchorPane mainAnchorPane;
    // protected Rectangle2D webViewSize;

    protected abstract void initJSClass();

    protected abstract void initWebFile();

    protected abstract WebView getWebView();

    protected void setJSClass(Object jsClass) {
        this.jsClass = jsClass;
    }

    protected void setWebFile(String htmlFile) {
        this.htmlFile = htmlFile;
    }

    public void setStageAndSetupListeners(Scene scene) {

        this.scene = scene;
        ScreensController.getInstance().getPrimaryStage().setResizable(true);
        ScreensController.getInstance().getPrimaryStage().widthProperty().addListener(resizeListener);
        ScreensController.getInstance().getPrimaryStage().heightProperty().addListener(resizeListener);
        getWebView().setContextMenuEnabled(false);
    }

    protected ScreensController getScreensController() {
        return this.myController;
    }

    protected WebEngine getEngine() {
        return this.engine;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert getWebView() != null : "fx:id=\"mainWebView\" was not injected: check your FXML file.";
        assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file.";

        this.engine = getWebView().getEngine();
        double w = ScreensController.getInstance().getPrimaryStage().getWidth();
        double h = ScreensController.getInstance().getPrimaryStage().getHeight();
        mainAnchorPane.setPrefSize(w, h-V_SCROLL_BUTTON_HEIGHT);
        mainAnchorPane.resize(w, h-V_SCROLL_BUTTON_HEIGHT);
            getWebView().setPrefSize(w, h-V_SCROLL_BUTTON_HEIGHT);
            getWebView().resize(w, h-V_SCROLL_BUTTON_HEIGHT);
            

        try {

            JSObject window = (JSObject) this.engine.executeScript("window");
            initJSClass();
            window.setMember("app", this.jsClass);
            initWebFile();
            final URL h_view = new URL("file:" + WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + this.htmlFile);
            this.engine.load(h_view.toExternalForm());

            this.engine.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> arg0) {
                    MessageBox.messageJFX("Workbench Info.", "JavaFX Error", arg0.getData());
                }
            });
            /* this.engine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
             if (newState == Worker.State.SUCCEEDED) {

             JSObject window = (JSObject) this.engine.executeScript("window");
             initJSClass();
             window.setMember("app", this.jsClass);
                 
             }
             });*/
            //using this as the directory should already exist and error if not
            Path getWV_Dir = Paths.get(Tools.WEB_VIEW_DATA_Path);
            if (Files.exists(getWV_Dir)) {
                File file = getWV_Dir.toFile();
                engine.setUserDataDirectory(file);
            } else {
                LOGGER.log(Level.SEVERE, "Cannot create web view local storage data");
            }

        } catch (MalformedURLException ex) {
            System.err.println("ERROR: Could not initialise controller: " + ex);
        }
    }

    /*
     * @param str: csv string where each row is separated by \n
     */
    public void saveDialogCSV(String str, boolean includeFirstColumn) throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName("data.csv");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Comma Separated Values", "*.csv"));
        File file = fileChooser.showSaveDialog(scene.getWindow());
        if (file != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getButtonTypes().clear();
            alert.setTitle("Workbench Information");
            alert.setHeaderText("Please Wait...");
            alert.setContentText("The file is saving. Please wait!...");
            alert.show();
            final Task<Integer> task = new Task<Integer>() {
                @Override
                public Integer call() throws Exception {

                    PrintWriter writer = new PrintWriter(file);
                    String[] data = str.split("\n");
                    for (String row : data) {
                        row = row.replaceAll("<pre>", "").replaceAll("</pre>", "");
                        if (!includeFirstColumn) {
                            row = row.substring(row.indexOf(",") + 1);
                        }
                        writer.println(row);
                    }
                    writer.close();
                    return 0;
                }
            };
            task.setOnSucceeded(
                    new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event
                        ) {
                            alert.setTitle("Workbench Information");
                            alert.setHeaderText("Output Saved!");
                            alert.setContentText("The output has been saved to file, press to continue");
                            alert.getButtonTypes().add(ButtonType.OK);
                            alert.hide();
                            alert.show();
                        }
                    }
            );
            Thread t = new Thread(task);
            t.setDaemon(true); // thread will not prevent application shutdown
            t.start(); // start the thread
        }
    }

    public abstract void updateUI();

    ChangeListener<Object> resizeListener = new ChangeListener<Object>() {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            double w = ScreensController.getInstance().getPrimaryStage().getWidth();
            double h = ScreensController.getInstance().getPrimaryStage().getHeight();
            mainAnchorPane.setPrefSize(w, h-V_SCROLL_BUTTON_HEIGHT);
            mainAnchorPane.resize(w, h-V_SCROLL_BUTTON_HEIGHT);
            getWebView().setPrefSize(w, h-V_SCROLL_BUTTON_HEIGHT);
            getWebView().resize(w, h-V_SCROLL_BUTTON_HEIGHT);
        }
    };
}
