/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;
//
//import com.boxysystems.jgoogleanalytics.FocusPoint;
//import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;

import java.awt.Color;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.google.AnalyticsConfigData;
import uk.ac.uea.cmp.srnaworkbench.google.JGoogleAnalyticsTracker;
import uk.ac.uea.cmp.srnaworkbench.google.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import uk.ac.uea.cmp.srnaworkbench.startup.StartupOptionsLicence;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.ExternalsConfiguration;

/**
 *
 * @author w0445959
 */
public final class Tools {

    public enum USER_TYPE {
        ACADEMIC {

            @Override
            public void writeUserData() {
                updateUserData("User_Type", "User_Type: Academic");
            }
        },
        COMMERCIAL {
            @Override
            public void writeUserData() {
                updateUserData("User_Type", "User_Type: Commercial");
            }
        };

        public abstract void writeUserData();

    }

    private Tools() {
    }

    public static synchronized Tools getInstance() {
        if (t == null) {
            t = new Tools();
        }

        return t;
    }
    /**
     * Returns a String representation of the Current Working Directory
     */
    public static final String CWD;
    /**
     * The directory containing the entire project including source code
     */
    public static final String PROJECT_DIR;
    /**
     * The directory that's at the root of the distribution
     */
    public static final String ROOT_DIR;

    /**
     * The directory that holds all of the executable files
     */
    public static final String EXE_DIR;
    /**
     * The directory that will contain the reference data for the workbench
     */
    public static final File DATA_DIR;

    /**
     * The directory that holds all of the default parameters
     */
    public static final File DEFAULT_PARAMS_DIR;

    /**
     * The directory that holds all of the default json workflow setups
     */
    public static final File DEFAULT_JSON_WF_DIR;

    public static final File WEB_SCRIPTS_DIR;

    public static USER_TYPE user_type;
    private static int toolInstanceValue = -1;
    private static Tools t = null;
    private static File userSettingsFile;

    private static boolean tracking_usage = true;

    private static JGoogleAnalyticsTracker google_tracker;

    private static AnalyticsConfigData config;

    public static final Charset UTF8_charset = Charset.forName("UTF-8");

    //private static final FocusPoint mainFocusPoint;
    /**
     * Tab character
     */
    public final static String TAB = "\t";
    public final static File TRRNA_FILE;// = System.getProperty("user.dir");

    public static String userDataDirectoryPath;
    public static String LOG_DIR;
    public static String PATH_TO_TEMP_DIR;
    public static String RNA_Annotation_dataPath;
    public static String siLoCo_dataPath;

    public static String CoLIDE_dataPath;
    public static String ExpressionMatrix_dataPath;
    public static String history_dataPath;
    public static String WEB_VIEW_DATA_Path;
    public static String PAREfirst_DATA_Path;
    public static String miRCAT_DATA_Path;
    public static String GENOME_DATA_Path;

    private static final double BYTES_PER_GB = 1073741824;

    public static final long initialTime;
    public final static String[] initStyles
            = {
                "regular", "italic", "bold", "small", "large", "RED", "BLUE", "GREEN",
                "regular", "button", "regular", "icon",
                "regular"
            };
//    String[] initStyles =
//                        {
//                            "regular", "italic", "bold", "small", "large",
//                            "regular", "button", "regular", "icon",
//                            "regular"
//                        };

    static {

        initialTime = StopWatch.getCurrentTimeInSeconds();

        // Get program directory paths
        CWD = AppPaths.INSTANCE.getCWD().getPath();
        PROJECT_DIR = AppPaths.INSTANCE.getProjectDir().getPath();
        ROOT_DIR = AppPaths.INSTANCE.getRootDir().getPath();
        EXE_DIR = ROOT_DIR + DIR_SEPARATOR + "ExeFiles";
        DATA_DIR = new File(ROOT_DIR + DIR_SEPARATOR + "data");
        DEFAULT_PARAMS_DIR = new File(ROOT_DIR + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "default_params");
        DEFAULT_JSON_WF_DIR = new File(ROOT_DIR + DIR_SEPARATOR + "data" + DIR_SEPARATOR + "default_json_setup");

        TRRNA_FILE = new File(DATA_DIR.getPath() + DIR_SEPARATOR + "t_and_r_RNAs.fa");

        if (AppUtils.INSTANCE.isRunningInIDE()) {
            WEB_SCRIPTS_DIR = new File("web").getAbsoluteFile();
        } else {
            WEB_SCRIPTS_DIR = new File(DATA_DIR.getPath() + DIR_SEPARATOR + "web");
        }

        userDataDirectoryPath = ROOT_DIR + DIR_SEPARATOR + "User";
        updateUserPaths(userDataDirectoryPath);

        // Set up the logger here. This is the earliest oppurtunity that the Logger
        // can be created (after identifying the logging dir) and the also the latest 
        // (there is stuff to log just further down)
        WorkbenchLogger.newLogger();

        try {
            ExternalsConfiguration.setRootPath(ROOT_DIR);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        try {
            checkUserDirectoryData();
        } catch (IOException ex) {
            // Can't use the logger here (because the log directory wasn't created), 
            // so just output to the console using System.err
            System.err.println("WORKBENCH ERROR: Couldn't create User directory.");
            System.err.println("WORKBENCH ERROR: Exception: " + ex.toString());
            System.err.println("WORKBENCH ERROR: Resuming, but program may be unstable.  Proceed at own risk.");
        }
    }

    private static void setupGoogleAnalytics() {

        config = new AnalyticsConfigData("UA-46497406-1");
        config.populateFromSystem();
        google_tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);

        google_tracker.setEnabled(tracking_usage);
    }

    private static String getBuildDetails() {
        // For reference...
        //            Name: sRNA Workbench
        //            Specification-Title: sRNA Workbench
        //            Specification-Version: 1.0
        //            Specification-Vendor: UEA CMP Bioinformatics Group
        //            Implementation-Title: sRNA Workbench
        //            Implementation-Version: release1
        //            Implementation-Vendor: UEA CMP Bioinformatics Group

        String productName = "sRNA Workbench";
        String productVersion = "<dev. version>";
        String buildNumber = "<build number>";

        String classPath = Tools.class.getResource(Tools.class.getSimpleName() + ".class").toString();

        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";

            Manifest manifest = null;

            try {
                manifest = new Manifest(new URL(manifestPath).openStream());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            if (manifest != null) {
                Map<String, Attributes> attrmap = manifest.getEntries();

                Attributes appAttr = null;

                if (attrmap != null) {
                    appAttr = attrmap.get("sRNA Workbench"); // Maps to "Name" in the manifest
                }

                if (appAttr != null) {
                    productName = appAttr.getValue("Specification-Title");
                    productVersion = appAttr.getValue("Specification-Version");
                    buildNumber = appAttr.getValue("Implementation-Version");
                }
            }
        }

        return StringUtils.nullSafeConcatenation(productVersion, " (", buildNumber, ")");
    }

    public static void printCurrentMemoryValues() {
        int mb = 1024 * 1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        System.out.println("##### Heap utilization statistics [MB] #####");

        //Print used memory
        System.out.println("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        //Print free memory
        System.out.println("Free Memory:"
                + runtime.freeMemory() / mb);

        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);

        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }

    public static String readUserDataField(String fieldID) {
        String result = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(userSettingsFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(fieldID)) {
                    String[] fieldData = strLine.split(":");
                    if (fieldData.length != 2) {
                        //Close the input stream
                        in.close();
                        LOGGER.log(Level.WARNING, "User Info field read error, field missing info:{0}", fieldID);
                    } else {
                        result = fieldData[1];
                        //Close the input stream
                        in.close();
                        return result.trim();
                    }
                }

            }

            LOGGER.log(Level.WARNING, "User Info field read error, field not found:{0}", fieldID);

            //Close the input stream
            in.close();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "User Info read/write error: {0}", ex.getMessage());
        }

        return result.trim();

    }

    private static void updateUserData(String fieldID, String newInfo) {
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(userSettingsFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            StringBuilder fileContent = new StringBuilder();
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(fieldID)) {

                    String newLine = newInfo + LINE_SEPARATOR;
                    fileContent.append(newLine);
                } else {
                    // update content as it is
                    fileContent.append(strLine).append(LINE_SEPARATOR);
                    //appendRequired = false;
                }

            }

            // Now fileContent will have updated content , which you can override into file
            FileWriter fstreamWrite = new FileWriter(userSettingsFile);
            BufferedWriter out = new BufferedWriter(fstreamWrite);
            out.write(fileContent.toString());
            out.close();
            //Close the input stream
            in.close();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "User Info read/write update error: {0}", ex.getMessage());
        }

    }

    public static void updateServerData(String type) {
        try {
            URL url;
            if (type.equals("com")) {
                url = new URL("http://srna-workbench.cmp.uea.ac.uk/PHP/incrementCom.php/");
            } else {
                url = new URL("http://srna-workbench.cmp.uea.ac.uk/PHP/incrementAca.php/");
            }

            HttpURLConnection conn = null;

            conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                // do something with the data here
            } else {
                InputStream err = conn.getErrorStream();
                // err may have useful information.. but could be null see javadocs for more information
            }

            conn.disconnect();

            // ""
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not update user type, possible lack of internet facing port{0}", ex.getMessage());
        }

    }

    public static void initialCLISetup() {
        if (confirmFirstUsage()) {

            tracking_usage = false;
            boolean input_recognised = true;

            //Commented out to avoid asking the user if we can track them
            //Will need to be removed completely when made open source...
//        System.out.println( "Welcome to your first usage of the small RNA Workbench" );
//        System.out.println( "Before you can continue please take a moment to read the licence agreement in your install directory" );
//        System.out.println( "Please tell the software if you are a commerical or academic user by typing com or aca" );
//        BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
//        boolean input_recognised = false;
//
//        while ( !input_recognised )
//        {
//          String s = br.readLine();
//          if ( s.equalsIgnoreCase( "com" ) )
//          {
//            user_type = USER_TYPE.COMMERCIAL;
//            input_recognised = true;
//            updateServerData("com");
//          }
//          else if ( s.equalsIgnoreCase( "aca" ) )
//          {
//
//            user_type = USER_TYPE.COMMERCIAL;
//            input_recognised = true;
//            updateServerData("aca");
//
//          }
//          else
//          {
//            System.out.println( "Input not recognised, please enter either com or aca to confirm" );
//          }
//        }
//        System.out.println( "User type confirmed!" );
//
//        user_type.writeUserData();
//        StringBuilder strB = new StringBuilder();
//
//        strB.append( "User Participation Request" )
//          .append( LINE_SEPARATOR ).append( "We are always looking for ways to improve our" ).append( LINE_SEPARATOR )
//          .append( "software. As part of this process, we rely on our users to inform us of new features they would like to be introduced into the software. "
//            + "We then " )
//          .append( LINE_SEPARATOR )
//          .append( "use this information to gauge where our efforts should be placed, help" )
//          .append( LINE_SEPARATOR ).append( "obtain new funding to continue to develop the software, and")
//          .append( LINE_SEPARATOR)
//          .append( "keep track of the impact that our software has in science, industry").append( LINE_SEPARATOR).append( "and society.")
//          .append( LINE_SEPARATOR)
//          .append( LINE_SEPARATOR )
//          .append( "For these reasons, we have implemented an additional approach to help us keep track of how our software is being used. More specifically, " 
//            + "we request that you allow the software " 
//            + "to communicate with the Google Analytic service. All of the information that we will collect in this way will" 
//            + "be completely anonymous. Moreover, none of the information can be used to determine what data is being used " 
//            + "or to give anyone information on that data, results or analysis. We simply want to keep track")
//          .append( LINE_SEPARATOR)
//          .append( "of which parts of our software are being used.")
//          .append( LINE_SEPARATOR)
//          .append( LINE_SEPARATOR ).append( "We hope that you will be happy to help us with this. If so")
//          .append( LINE_SEPARATOR)
//          .append( "please type yes (y). However, if you do not wish us to collect this data, then please type no (n)")
//          .append( LINE_SEPARATOR)
//          .append( "option from the control panel on the left.")
//          .append( LINE_SEPARATOR)
//          .append( LINE_SEPARATOR )
//          .append( "Thank you for helping us to make this software as useful as possible.")
//          .append( LINE_SEPARATOR)
//          
//          .append( LINE_SEPARATOR)
//          ;
//        System.out.println( strB );
//
//        input_recognised = false;
//        while ( !input_recognised )
//        {
//          String s = br.readLine();
//          if ( s.equalsIgnoreCase( "y" ) || s.equalsIgnoreCase( "yes" ) )
//          {
//            System.out.println( "Thank you for helping to support our work and achieve our goals" );
//            tracking_usage = true;
//            input_recognised = true;
//          }
//          else if ( s.equalsIgnoreCase( "n" ) ||  s.equalsIgnoreCase( "no" ) )
//          {
//
//              tracking_usage = false;
//              input_recognised = true;
//
//          }
//          else
//          {
//            System.out.println( "Input not recognised, please enter either y(yes) or n(no) to confirm" );
//          }
//        }
            updateUserData("First usage:", "First usage: NO");

            if (tracking_usage) {
                updateUserData("Usage_Stats:", "Usage_Stats: YES");
            } else {
                updateUserData("Usage_Stats:", "Usage_Stats: NO");
            }

        } else {
            String result = readUserDataField("Usage_Stats:");

            tracking_usage = result.equals("YES");

            String userType = readUserDataField("User_Type");
            if (userType.equals("Academic")) {
                user_type = USER_TYPE.ACADEMIC;
            } else {
                user_type = USER_TYPE.COMMERCIAL;
            }
        }

        if (tracking_usage) {
            setupGoogleAnalytics();
        }

    }

    //entry point for google analytics tool usage tracking
    //if tracking was set to false during first program use nothing will happen
    public static void trackPage(String toolName) {

        if (google_tracker != null && tracking_usage) {

            google_tracker.trackPageViewFromSearch(toolName, toolName, toolName, "Version:" + getBuildDetails(), "");
        }

    }

    public static void trackEvent(String cat, String event) {
        if (google_tracker != null && tracking_usage) {

            google_tracker.trackEvent(cat, event);
        }
    }

    public static void stopTracking() {
        if (google_tracker != null) {
            google_tracker.setEnabled(false);
        }
    }

    /**
     * Check user options for first usage of software. Used in CLI and GUI mode
     *
     * @return true or false depending on first usage
     */
    private static boolean confirmFirstUsage() {
        String result = readUserDataField("First usage:");
        String resultTrim = result.replaceAll(" ", "");
        if (resultTrim.equals("NO")) {
            return false;
        } else if (resultTrim.equals("YES")) {
            return true;
        } else {

            return true;
        }
    }

    public static void initialGUISetup() {
        if (confirmFirstUsage()) {

            StartupOptionsLicence startupWindow = new StartupOptionsLicence(null, false);
            startupWindow.setVisible(true);

            tracking_usage = startupWindow.getTrackingOption();

            updateUserData("First usage:", "First usage: NO");
            if (tracking_usage) {
                updateUserData("Usage_Stats:", "Usage_Stats: YES");
            } else {
                updateUserData("Usage_Stats:", "Usage_Stats: NO");
            }
            String userType = startupWindow.getUserType();
            if (userType.equals("Academic User")) {
                user_type = USER_TYPE.ACADEMIC;
            } else {
                user_type = USER_TYPE.COMMERCIAL;
            }
        } else {
            String result = readUserDataField("Usage_Stats:");

            tracking_usage = result.equals("YES");

            String userType = readUserDataField("User_Type");
            if (userType.equals("Academic")) {
                user_type = USER_TYPE.ACADEMIC;
            } else {
                user_type = USER_TYPE.COMMERCIAL;
            }
        }

        if (tracking_usage) {
            setupGoogleAnalytics();

            trackPage("Main GUI Window Load");
        }
    }

    public static void updateUserPaths(final String userDir) {
        PATH_TO_TEMP_DIR = userDir + DIR_SEPARATOR + "temp";
        RNA_Annotation_dataPath = userDir + DIR_SEPARATOR + "RNA_AnnotationData";
        siLoCo_dataPath = userDir + DIR_SEPARATOR + "SiLoCoData";

        PAREfirst_DATA_Path =  "PAREfirst_Data";

        miRCAT_DATA_Path = userDir + DIR_SEPARATOR + "miRPARE_Data";

        GENOME_DATA_Path = userDir + DIR_SEPARATOR + "genome";

        CoLIDE_dataPath = userDir + DIR_SEPARATOR + "CoLIDEData";

        ExpressionMatrix_dataPath = userDir + DIR_SEPARATOR + "ExpressionMatrixData";

        history_dataPath = userDir + DIR_SEPARATOR + "history";

        LOG_DIR = userDataDirectoryPath + DIR_SEPARATOR + "logs";

        WEB_VIEW_DATA_Path = userDir + DIR_SEPARATOR + "WebView";

    }

    public static void checkUserDirectoryData() throws IOException {
        File userDir = new File(Tools.userDataDirectoryPath);

        File[] dirs
                = {
                    userDir,
                    new File(Tools.PATH_TO_TEMP_DIR),
                    new File(Tools.RNA_Annotation_dataPath),
                    new File(Tools.siLoCo_dataPath),
                    new File(Tools.GENOME_DATA_Path),
                    new File(Tools.CoLIDE_dataPath),
                    new File(Tools.ExpressionMatrix_dataPath),
                    new File(Tools.history_dataPath),
                    new File(Tools.LOG_DIR),
                    new File(WEB_VIEW_DATA_Path)
                };

        for (File f : dirs) {
            org.apache.commons.io.FileUtils.forceMkdir(f);
        }

        // Check for the user settings file - create it if necessary...
        userSettingsFile = new File(Tools.userDataDirectoryPath + DIR_SEPARATOR + "UserSettings");
        Object[] msgParams = new Object[]{userSettingsFile.getAbsolutePath()};

        if (userSettingsFile.exists()) {
            LOGGER.log(Level.FINE, "Using user settings file ''{0}''", msgParams);
        } else {
            if (userDir.exists()) {
                createDefaultUserSettingsFiles(userSettingsFile);
            } else {
                LOGGER.log(Level.WARNING, "No User Directory Found!{0}", msgParams);
            }
        }

        File[] historyFiles
                = {
                    new File(Tools.history_dataPath + DIR_SEPARATOR + "sRNA_History"),
                    new File(Tools.history_dataPath + DIR_SEPARATOR + "mRNA_History"),
                    new File(Tools.history_dataPath + DIR_SEPARATOR + "transcriptome_History"),
                    new File(Tools.history_dataPath + DIR_SEPARATOR + "genome_History"),
                    new File(Tools.history_dataPath + DIR_SEPARATOR + "temp_History")
                };

        for (File file : historyFiles) {
            if (file.exists()) {
                LOGGER.log(Level.FINE, "file exists ''{0}''", msgParams);
            } else {
                file.createNewFile();
            }
        }

        //clear previous history
        FileUtils.deleteDirectory(new File(WEB_VIEW_DATA_Path + DIR_SEPARATOR + "localstorage"));
    }

    private static void createDefaultUserSettingsFiles(File userSettingsFile) {
        try {
            userSettingsFile.createNewFile();

            FileWriter userFile = new FileWriter(userSettingsFile, true);
            PrintWriter newUserData = new PrintWriter(userFile);

            newUserData.println("First usage: YES");
            newUserData.println("User_Type: NOT_SET");
            newUserData.println("Usage_Stats: NOT_SET");
            newUserData.println("miRCaT_Preferences");
            newUserData.println("Filter_Preferences");
            newUserData.println("miRProf_Preferences");

            newUserData.close();
            userFile.close();

            LOGGER.log(Level.FINE, "Created user settings file ''{0}''", userSettingsFile.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static String getExeDir() {
        return ROOT_DIR + DIR_SEPARATOR + "ExeFiles" + DIR_SEPARATOR + AppUtils.INSTANCE.getArchitecture().getBinariesDirectory();
    }

    public static String getLibExt() {
        return AppUtils.INSTANCE.getArchitecture().getLibExtension();
    }

    public static int getInstanceCode() {
        return ++toolInstanceValue;
    }

    public static String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement e : t.getStackTrace()) {
            sb.append(e.toString()).append(String.valueOf(LINE_SEPARATOR));
        }

        return sb.toString();
    }

    // If expand is true, expands all nodes in the tree.
// Otherwise, collapses all nodes in the tree.
    public static void expandAllTreeNodes(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public static File getNextDirectory() {
        File file = new File(PATH_TO_TEMP_DIR + DIR_SEPARATOR + "_" + getInstanceCode());
        if (file.exists()) {
            LOGGER.log(Level.FINE, "Temp data directory exists: {0}", file.getAbsolutePath());
            return getNextDirectory();
        } else {
            // Create user directory...
            LOGGER.log(Level.INFO, "Creating tools temp data directory {0}...{1}",
                    new Object[]{file.getAbsolutePath(), file.mkdir() ? "created" : "failed"});
            file.deleteOnExit();
            return file;
        }

    }

    /**
     * Sets the default number of threads to be used for searching trees. At
     * least one processor should be left available for running the gui and
     * engine and other OS tasks. If only one processor is available, then only
     * one thread is used.if two or more processors are available, then the
     * default number of threads used is the number of processors available to
     * the java runtime minus one.
     *
     * @return
     */
    public static int getThreadCount() {
        int processorsAvailable = Runtime.getRuntime().availableProcessors();
        if (processorsAvailable >= 2) {
            processorsAvailable -= 1;
        }
        if (processorsAvailable == 0) {
            LOGGER.log(Level.WARNING, "Runtime reports no available processors, running with low defaults");
            processorsAvailable = 1;
        }
        return processorsAvailable;
    }//end method.

    public static void removeShutdownHooks(List<Thread> bees) {
        for (Thread be : bees) {
            removeShutdownHook(be);
        }
    }

    public static void removeShutdownHook(final Thread be) {

        Runtime.getRuntime().removeShutdownHook(be);
    }

    public static boolean isWindows() {
        return AppUtils.INSTANCE.getArchitecture().isWindows();
    }

    public static boolean isMac() {
        return AppUtils.INSTANCE.getArchitecture().isMac();
    }

    public static boolean isLinux() {
        return AppUtils.INSTANCE.getArchitecture().isLinux();
    }

    public static String getDateTime() {

        DateFormat df = DateFormat.getDateTimeInstance();

        df.setTimeZone(TimeZone.getDefault());

        return df.format(new Date());
    }

    public static String getSimpleDateTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        df.setTimeZone(TimeZone.getDefault());

        return df.format(new Date());
    }

    /**
     * Get maximum size of heap in bytes. The heap cannot grow beyond this size.
     * Any attempt will result in an OutOfMemoryException.
     *
     * @return Maximum size of heap in bytes
     */
    public static double getMaxHeapSize() {
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        return (double) heapMaxSize / BYTES_PER_GB;
    }

    /**
     * Get current size of heap in bytes
     *
     * @return Current size of heap in bytes
     */
    public static double getUsedHeapSize() {
        long heapSize = Runtime.getRuntime().totalMemory();

        return (double) heapSize / BYTES_PER_GB;
    }

    /**
     * Get amount of free memory within the heap in bytes. This size will
     * increase after garbage collection and decrease as new objects are
     * created.
     *
     * @return Free memory within the heap in bytes
     */
    public static double getFreeMemorySize() {
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        return (double) heapFreeSize / BYTES_PER_GB;
    }

    /**
     * Sorts a map by its values
     *
     * @param map The map which needs sorting
     * @return The map sorted by value
     */
    public static <T, K extends Comparable<? super K>> Map<T, K> sortByValue(Map<T, K> map) {
        List<Entry<T, K>> list = new LinkedList<Entry<T, K>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<T, K>>() {
            @Override
            public int compare(Entry<T, K> o1, Entry<T, K> o2) {
                K k1 = o1.getValue();
                K k2 = o2.getValue();
                return k1.compareTo(k2);
            }
        });

        Map<T, K> result = new LinkedHashMap<T, K>();
        for (Iterator<Entry<T, K>> it = list.iterator(); it.hasNext();) {
            Map.Entry<T, K> entry = (Map.Entry<T, K>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    public static float roundTwoDecimals(float d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(d));
    }

    public static boolean isRedundantFile(File input) throws FileNotFoundException, IOException {
        BufferedReader inShortReadFile = new BufferedReader(new FileReader(input));

        String line = "";
        while ((line = inShortReadFile.readLine()) != null) {

            //find first FASTA header
            if (line.startsWith(">")) {
                //does file come in format >SEQUENCE(ABUNDANCE)?
                String[] seqAndAbun = line.split("\\(");

                if (seqAndAbun.length > 1) {
                    try {
                        String seq = seqAndAbun[0];
                        Pattern actguFilter = Pattern.compile("[actguACTGU]*");
                        Matcher m = actguFilter.matcher(seq);

                        String abundance = (seqAndAbun[1].replaceAll("\\)", ""));
                        int abundDef = -1;
                        int abundInt = StringUtils.safeIntegerParse(abundance.trim(), abundDef);
                        if (abundInt > 0) {
                            //file is non-redundant format return early

                            return false;
                        } else {
                            //file is redundant format return early
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }

                } else {
                    return true;
                }

            } //is it a patman file?
            else {
                if (line.contains(Tools.TAB)) {
                    String[] lineSeperatedValue = line.split(Tools.TAB);
                    String[] seqAndAbun = lineSeperatedValue[1].split("\\(");
                    if (seqAndAbun.length > 1) {
                        try {
                            String seq = seqAndAbun[0];
                            Pattern actguFilter = Pattern.compile("[actguACTGU]*");
                            Matcher m = actguFilter.matcher(seq);

                            String abundance = (seqAndAbun[1].replaceAll("\\)", ""));
                            int abundDef = -1;
                            int abundInt = StringUtils.safeIntegerParse(abundance.trim(), abundDef);
                            if (abundInt > 0) {
                                //file is non-redundant format return early

                                return false;
                            } else {
                                //file is redundant format return early
                                return true;
                            }
                        } catch (Exception e) {
                            return false;
                        }

                    } else {
                        return true;
                    }
                }
            }
        }
        //no lines contained > at the start...
        throw new FileNotFoundException("File does not appear to be in FASTA format");

    }

    public static ImageIcon createImageIcon(String path,
            String description) {
        java.net.URL imgURL = Tools.getInstance().getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            LOGGER.log(Level.WARNING, "Couldn't find file: {0}", path);
            return null;
        }
    }

    public static ImageIcon createImageIcon(URL path,
            String description) {

        if (path != null) {
            return new ImageIcon(path, description);
        } else {
            LOGGER.log(Level.WARNING, "Couldn't find file: {0}", path);
            return null;
        }
    }

    public static void addStylesToDocument(StyledDocument doc) {
//        //Initialize some styles.
        javax.swing.text.Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        javax.swing.text.Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "Lucida Sans Unicode");

        javax.swing.text.Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setFontSize(s, 12);

        s = doc.addStyle("RED", regular);
        StyleConstants.setBold(s, false);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setForeground(s, Color.RED);

        s = doc.addStyle("BLUE", regular);
        StyleConstants.setBold(s, false);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setForeground(s, Color.BLUE);

        s = doc.addStyle("GREEN", regular);
        StyleConstants.setBold(s, false);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setForeground(s, Color.GREEN);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);

//        s = doc.addStyle("icon", regular);
//        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
//        ImageIcon pigIcon = createImageIcon("images/Pig.gif",
//                                            "a cute pig");
//        if (pigIcon != null) {
//            StyleConstants.setIcon(s, pigIcon);
//        }
//
//        s = doc.addStyle("button", regular);
//        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
//        ImageIcon soundIcon = createImageIcon("images/sound.gif",
//                                              "sound icon");
//        JButton button = new JButton();
//        if (soundIcon != null) {
//            button.setIcon(soundIcon);
//        } else {
//            button.setText("BEEP");
//        }
//        button.setCursor(Cursor.getDefaultCursor());
//        button.setMargin(new Insets(0,0,0,0));
//        //button.setActionCommand(buttonString);
//        //button.addActionListener(this);
//        StyleConstants.setComponent(s, button);
    }
}
