/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.controlsfx.control.Notifications;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.MiRCat20;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.DATA_DIR;
import static uk.ac.uea.cmp.srnaworkbench.utils.Tools.WEB_SCRIPTS_DIR;
import uk.ac.uea.cmp.srnaworkbench.workflow.PreconfiguredWorkflows;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

/**
 *
 * @author w0445959
 */
public class MainAppFX extends Application {

    private static WorkflowSceneController workflowSceneController;
    
    public static void shutdown()
    {
        workflowSceneController.destroy();
        Platform.exit();

        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

//        if (AppUtils.INSTANCE.isRunningInIDE())
//        {
//            if (Files.exists(Paths.get(System.getProperty("user.dir") + DIR_SEPARATOR + "srna_workbench.mv.db")))
//            {
//                LOGGER.log(Level.INFO, "Database File exists in directory : Deleting...");
//                Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + DIR_SEPARATOR + "srna_workbench.mv.db"));
//            }
//        }
//        else
//        {
//            if (Files.exists(Paths.get(Tools.ROOT_DIR + DIR_SEPARATOR + "srna_workbench.mv.db")))
//            {
//                LOGGER.log(Level.INFO, "Database File exists in directory : Deleting...");
//                Files.deleteIfExists(Paths.get(Tools.ROOT_DIR + DIR_SEPARATOR + "srna_workbench.mv.db"));
//
//            }
//        }
        final Parameters params = getParameters();
        final List<String> parameters = params.getRaw();

        String[] args = new String[parameters.size()];

        Locale cLocale = new Locale.Builder().setLanguage("en").setRegion("GB").build();
        Locale.setDefault(cLocale);
        checkJavaVersion();

        Map<String, String> argmap = parseArgs(parameters.toArray(args));

        // Determines how uncaught exceptions are dealt with if they are thrown in the JavaFX thread
        // Not altering this causes these exceptions to be swallowed completely
        Thread.setDefaultUncaughtExceptionHandler(new WorkbenchFXExceptionHandler());
        Thread.currentThread().setUncaughtExceptionHandler(new WorkbenchFXExceptionHandler());

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            try {
                shutdown();
                //ToolManager.getInstance().closeTools();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
        try {

            startWorkbench(argmap, primaryStage);

//         
//            System.out.println("create pre conf workflow");
//            DatabaseWorkflowModule.getInstance().setDebugMode(true);
//            PreconfiguredWorkflows.createQC_DE_Workflow(new Rectangle2D(0, 0, 0, 0));
//            HTMLWizardViewController.configureWorkflowData();
//            System.out.println("being pre conf workflow");
//            WorkflowManager.getInstance().start();
            //System.gc();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
//        catch(Throwable t)
//        {
//            handleJavaFXThreadError(Thread.currentThread(), t);
//        }

        // FIXME: This moves the instantiation of AppContext from the xml config
        // to before the GUI is launched. This bit is slow (~ 7 seconds) and makes for
        // a poor user experience to have this happen on click of a workflow.
        // Slow instantiation might be due to it connecting to a new database or looking through beans...
        try {
            DatabaseWorkflowModule.getInstance();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Workbench Startup Error");
            alert.setHeaderText("Error on Workbench startup");
            alert.setContentText("The database file may currently be in use. Please make sure nothing is currently using the database file and restart the software");

            Label label = new Label("The exception stacktrace was:");
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();
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
            
            alert.getDialogPane().setExpandableContent(expContent);
            
            Platform.runLater(()
                    -> alert.showAndWait()
                            .filter(response -> response == ButtonType.OK)
                            .ifPresent(response -> shutdown())
            );
           
        }

    }

    private static void checkJavaVersion() {
        final int REQUIRED_SUBVERSION = 8;

        String version = System.getProperty("java.version");

        String[] numbers = version.split("\\.");

        if (numbers.length > 0) {
            Integer versionNumber = Integer.parseInt(numbers[1]);

            if (versionNumber < REQUIRED_SUBVERSION) {
                String message = "Java version detected: " + version + System.getProperty("line.separator")
                        + "Please update to version 1." + REQUIRED_SUBVERSION + " or later";

                JOptionPane.showMessageDialog(null, message, "UEA sRNA Workbench", JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            }
        }
    }

    /**
     * Parse the arguments and load them into a map.<br/>
     * Arguments are expected to start with a single '-'.<br/>
     * Key-value pairs are formatted as '-key value'.<br/>
     * Switches are formatted as '-switch' and placed in the map according to
     * their function.<br/>
     *
     * @param args
     * @return
     */
    private static Map<String, String> parseArgs(String[] args) {
        HashMap<String, String> argmap = new HashMap<String, String>();

        int i = 0;

        while (i < args.length) {
            String arg = args[i++];

            if ("-help".equals(arg) || "--help".equals(arg)) {
                //TO_DO goToHelp some new help output here
                ToolBox.UNKNOWN_TOOL.printUsage();
                System.exit(0);
            }
            if ("-verbose".equals(arg) || "--verbose".equals(arg)) {
                // verbose flag has special treatment
                AppUtils.INSTANCE.setVerbose(true);
                argmap.put("verbose", Boolean.TRUE.toString());
            } else {
                if (arg.indexOf('-') == 0) {
          // argument starts with '-' so need to work out if it's a flag or a key-value pair

                    // Remove the prefixes '-' or '--'
                    if (arg.indexOf("--") == 0) {
                        arg = StringUtils.safeSubstring(arg, 2, "");
                    }
                    if (arg.indexOf('-') == 0) {
                        arg = StringUtils.safeSubstring(arg, 1, "");
                    }

                    if (i < args.length) {
                        String nextarg = args[i];

                        if (nextarg.indexOf('-') == 0) {
                            // 'nextarg' is an argument (not a value) so store 'arg' in the map and move on
                            argmap.put(arg, "");
                        } else {
                            // key-value pair
                            argmap.put(arg, nextarg);
                            i++;
                        }
                    } else {
                        // 'arg' is the last argument so it's a flag on its own
                        argmap.put(arg, "");
                    }
                } else {
                    throw new IllegalArgumentException("CLI Error: Illegal argument syntax near: " + arg);
                }
            }
        }

        return argmap;
    }

    private static void startWorkbench(Map<String, String> argmap, Stage primaryStage) throws IOException {
        // Make sure directories and files are present for both the command-line tools and the gui
        Tools.checkUserDirectoryData();

        // Assume that if the user has selected a particular tool they want to run in CLI mode.
        if (argmap.containsKey("tool")) {
            AppUtils.INSTANCE.setCommandLine(true);

            if (argmap.containsKey("basespace")) {
                AppUtils.INSTANCE.setBaseSpace(true);
            }

            Tools.initialCLISetup();
            // Get the tool
            String toolName = argmap.get("tool");
            ToolBox tool = ToolBox.getToolForName(toolName);

            try {
                tool.startTool(argmap);

                //System.exit(0);
            } catch (IllegalArgumentException e) {
                System.out.println("------------------------------------------------------------------------------------------");
                System.out.println(e.getMessage());
                tool.printUsage();
                System.out.println("------------------------------------------------------------------------------------------");

                System.exit(1);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, null, e);
                System.err.println("WORKENCH ERROR: Unrecoverable exception occured: " + e.toString());
                System.exit(1);
            }
        } else {
            startGUIMode(primaryStage);
        }
    }

    public static void startGUIMode(Stage primaryStage) throws IOException {
        LOGGER.log(Level.ALL, "GUI startup...");

        primaryStage.getIcons().add(new Image(MainAppFX.class.getResource("/images/GUI_Icons/workbench_transparent.png").toString()));
        primaryStage.setTitle("The sRNA Workbench Version 4.4 Alpha (On Disk build)");

        if (Tools.isMac()) {

//
            URL workbench_image_resource = MainAppFX.class.getResource("/images/GUI_Icons/workbench_transparent.png");

            ImageIcon createImageIcon = Tools.createImageIcon(workbench_image_resource, "sRNA Workbench image");
            com.apple.eawt.Application.getApplication().
                    setDockIconImage(createImageIcon.getImage());

        } else {

        }

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        Rectangle2D internalBounds = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight() - 35);
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

       /* // chris tmp added
        primaryStage.widthProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                Double width = (Double) newValue;
                System.out.println("width: " + width);
            }
        });*/

        workflowSceneController = new WorkflowSceneController();

        workflowSceneController.setSizeValues(internalBounds);

        ScreensController.getInstance().loadScreen(WorkflowViewer.MAIN_SCREEN,
                WorkflowViewer.MAIN_SCREEN_FXML, workflowSceneController);

        ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN);
        ScreensController.getInstance().setPrimaryStage(primaryStage);
        Group root = new Group();
        //if (Tools.isMac())
        {
            final Menu helpMenu = new Menu("Help");
            MenuItem goToHelp = new MenuItem("Go To Help Pages"
            );
            goToHelp.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {

                    String url = detectCorrectHelpPage();

                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI(url));
                        } catch (IOException | URISyntaxException e) {
                            LOGGER.log(Level.WARNING, "Help pages cannot be loaded{0}", e.getMessage());
                        }

                    } else {
                        try {
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec("xdg-open " + url);
                        } catch (IOException ex) {
                            Logger.getLogger(MainAppFX.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                public String detectCorrectHelpPage() {
                    String url = "http://srna-workbench.cmp.uea.ac.uk";
                    String activeScreen = ScreensController.getInstance().getActiveScreen();
                    switch (activeScreen) {
                        case "workflow":
                            url += "/workflows";
                            break;
                        case "DataBase":
                            url += "/database";
                            break;
                        case "WIZARD_SCREEN":
                            url += "/WIZARD";
                            break;
                        case "FIRST_REPORT":
                            url += "/FIRST_REPORT";
                            break;
                        case "FILE_REVIEW":
                            url += "/FILE_REVIEW";
                            break;
                        case "NORMALISER":
                            url += "/NORMALISER";
                            break;
                        case "SECOND_REPORT":
                            url += "/SECOND_REPORT";
                            break;
                        case "OFFSET_REVIEW":
                            url += "/OFFSET_REVIEW";
                            break;
                        case "DIFFERENTIAL EXPRESSION":
                            url += "/DIFFERENTIAL_EXPRESSION";
                            break;

                    }
                    return url;
                }
            });
            helpMenu.getItems().add(goToHelp);
            MenuBar menuBar = new MenuBar();
            menuBar.getMenus().addAll(helpMenu);
            menuBar.setUseSystemMenuBar(true);
            root.getChildren().addAll(menuBar);

        }
        root.getChildren().addAll(ScreensController.getInstance());
        Scene scene = new Scene(root);
        //root.setAutoSizeChildren(true);

        primaryStage.setScene(scene);
        //primaryStage.setFullScreen(true);

        primaryStage.show();
        primaryStage.setFullScreen(false);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //deleteDirs();
        launch(args);
    }

    public static void deleteDirs() {
        List<File> filesToDelete = new ArrayList<>();
        filesToDelete.add(new File("/Users/ujy06jau/Workbench/target/release/User/genome"));
        filesToDelete.add(new File("/Users/ujy06jau/Workbench/target/release/User/history"));
        filesToDelete.add(new File("/Users/ujy06jau/Workbench/target/release/User/logs"));
        filesToDelete.add(new File("/Users/ujy06jau/Workbench/target/release/User/miRPARE_Data"));
        filesToDelete.add(new File("/Users/ujy06jau/Workbench/target/release/User/temp"));
        for (File f : filesToDelete) {

            deleteDir(f);
            System.out.println("deleted: " + f.getAbsolutePath());
        }

    }

    public static void deleteDir(File dir) {
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    deleteDir(f);
                }
                f.delete();
            }
        }
    }

    public static class WorkbenchFXExceptionHandler implements UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            System.out.println("Exception in JavaFX thread " + e);
            LOGGER.log(Level.SEVERE, "Exception in JavaFX thread ", e);

//            Notifications n = Notifications.create()
//                    .title("Fatal Error")
//                    .onAction((ActionEvent event)
//                            -> {
//                        try {
//                            Desktop.getDesktop().open(new File(Tools.LOG_DIR));
//                        } catch (IOException ex) {
//                            System.err.println("Log directory not found! " + ex);
//                        }
//                    })
//                    .text("An error occurred. Please see the logs for more information");
//            Platform.runLater(() -> n.showError());
        }
    }

}
