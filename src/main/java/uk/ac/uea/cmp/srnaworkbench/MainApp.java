/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.apple.AppleApplicationHandler;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
public class MainApp 
{
    
    private static final int JFXPANEL_WIDTH_INT = 300;
    private static final int JFXPANEL_HEIGHT_INT = 250;
    private static JFXPanel fxContainer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                initAndShowGUI();
//            }
//        });
        //System.setProperty( "javafx.userAgentStylesheetUrl", "MODENA" );
        //DatabaseWorkflowModule.getInstance();
         
        Locale cLocale = new Locale.Builder().setLanguage("en").setRegion("GB").build();
        Locale.setDefault(cLocale);
        checkJavaVersion();

        
        Map<String, String> argmap = parseArgs(args);

        try
        {
            
            startWorkbench(argmap);   
            System.gc();
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Swing and JavaFX");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
        Scene scene = createScene();
        fxPanel.setScene(scene);
    }

    private static Scene createScene() 
    {
        try
        {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/MainScene.fxml"));
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/Styles.css");
            
//            stage.setTitle("JavaFX and Maven");
//            stage.setScene(scene);
//            stage.show();
//            
//            
//            Button btn = new Button();
//            btn.setText("Say 'Hello World'");
//            btn.setOnAction(new EventHandler<ActionEvent>()
//            {
//                
//                @Override
//                public void handle(ActionEvent event)
//                {
//                    System.out.println("Hello World!");
//                }
//            });
//            StackPane root = new StackPane();
//            root.getChildren().add(btn);
//            fxContainer.setScene(scene);
            return (scene);
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
//        Group  root  =  new  Group();
//        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
//        Text  text  =  new  Text();
//        
//        text.setX(40);
//        text.setY(100);
//        text.setFont(new Font(25));
//        text.setText("Welcome JavaFX!");
//
//        root.getChildren().add(text);

//        return (scene);
        return null;
    }

    
  /**
   * Parse the arguments and load them into a map.<br/>
   * Arguments are expected to start with a single '-'.<br/>
   * Key-value pairs are formatted as '-key value'.<br/>
   * Switches are formatted as '-switch' and placed in the map according to their function.<br/>
   *
   * @param args
   * @return
   */
  private static Map<String, String> parseArgs( String[] args )
  {
    HashMap<String, String> argmap = new HashMap<String, String>();

    int i = 0;

    while ( i < args.length )
    {
      String arg = args[i++];

      if ( "-help".equals( arg ) || "--help".equals( arg ) )
      {
        //TO_DO add some new help output here
        ToolBox.UNKNOWN_TOOL.printUsage();
        System.exit( 0 );
      }
      if ( "-verbose".equals( arg ) || "--verbose".equals( arg ) )
      {
        // verbose flag has special treatment
        AppUtils.INSTANCE.setVerbose( true );
        argmap.put( "verbose", Boolean.TRUE.toString() );
      }
      else
      {
        if ( arg.indexOf( '-' ) == 0 )
        {
          // argument starts with '-' so need to work out if it's a flag or a key-value pair

          // Remove the prefixes '-' or '--'
          if ( arg.indexOf( "--" ) == 0 )
          {
            arg = StringUtils.safeSubstring( arg, 2, "" );
          }
          if ( arg.indexOf( '-' ) == 0 )
          {
            arg = StringUtils.safeSubstring( arg, 1, "" );
          }

          if ( i < args.length )
          {
            String nextarg = args[i];

            if ( nextarg.indexOf( '-' ) == 0 )
            {
              // 'nextarg' is an argument (not a value) so store 'arg' in the map and move on
              argmap.put( arg, "" );
            }
            else
            {
              // key-value pair
              argmap.put( arg, nextarg );
              i++;
            }
          }
          else
          {
            // 'arg' is the last argument so it's a flag on its own
            argmap.put( arg, "" );
          }
        }
        else
        {
          throw new IllegalArgumentException( "CLI Error: Illegal argument syntax near: " + arg );
        }
      }
    }

    return argmap;
  }

  private static void checkJavaVersion()
  {
    final int REQUIRED_SUBVERSION = 7;

    String version = System.getProperty( "java.version" );

    String[] numbers = version.split( "\\." );

    if ( numbers.length > 0 )
    {
      Integer versionNumber = Integer.parseInt( numbers[1] );

      if ( versionNumber < REQUIRED_SUBVERSION )
      {
        String message = "Java version detected: " + version + System.getProperty( "line.separator" ) +
          "Please update to version 1." + REQUIRED_SUBVERSION + " or later";

        JOptionPane.showMessageDialog( null, message, "UEA sRNA Workbench", JOptionPane.ERROR_MESSAGE );

        System.exit( 1 );
      }
    }
  }

 

  private static void startWorkbench( Map<String, String> argmap ) throws IOException
  {
    // Make sure directories and files are present for both the command-line tools and the gui
    Tools.checkUserDirectoryData();

    // Assume that if the user has selected a particular tool they want to run in CLI mode.
    if ( argmap.containsKey( "tool" ) )
    {
      AppUtils.INSTANCE.setCommandLine( true );
      
      if(argmap.containsKey( "basespace"))
      {
        AppUtils.INSTANCE.setBaseSpace( true );
      }
      
      if(argmap.containsKey( "userdir"))
      {
        Tools.updateUserPaths(argmap.get("userdir"));
      }
      try {
          Tools.checkUserDirectoryData();
      
          // Tell the workbench logger about the new log dir
          WorkbenchLogger.LOGGER.recreateHandlers(new File(Tools.LOG_DIR));
      } catch (IOException e) {
          // Need to do something here.  Probably should raise an error dialog
}

      
      Tools.initialCLISetup();
      // Get the tool
      String toolName = argmap.get( "tool" );
      ToolBox tool = ToolBox.getToolForName( toolName );

      try
      {
        tool.startTool( argmap ); 

        System.exit( 0 );
      }
      catch ( IllegalArgumentException e )
      {
        System.out.println( "------------------------------------------------------------------------------------------" );
        System.out.println( e.getMessage() );
        tool.printUsage();
        System.out.println( "------------------------------------------------------------------------------------------" );

        System.exit( 1 );
      }
      catch ( Exception e )
      {
        LOGGER.log( Level.SEVERE, null, e );
        System.err.println( "WORKENCH ERROR: Unrecoverable exception occured: " + e.toString() );
        System.exit( 1 );
      }
    }
    else
    {
      startGUIMode();
    }
  }

  public static void startGUIMode()
  {
    LOGGER.log( Level.ALL, "GUI startup..." );

    

    if ( Tools.isMac() )
    {
        //-Xdock:name="My Application"
      System.setProperty( "apple.laf.useScreenMenuBar", "false" );
      System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "UEA sRNA Workbench" );
      System.setProperty( "apple.awt.brushMetalLook", "true" );
        
        
     // com.apple.eawt.Application.getApplication().getDockMenu();
//
//  com.apple.eawt.Application.getApplication().getDockIconImage();
//
      
        URL workbench_image_resource = MainApp.class.getResource("/images/GUI_Icons/workbench_transparent.png");

        ImageIcon createImageIcon = Tools.createImageIcon(workbench_image_resource, "sRNA Workbench image");
        com.apple.eawt.Application.getApplication().
                setDockIconImage(createImageIcon.getImage());

        com.apple.eawt.Application a = com.apple.eawt.Application.getApplication();

  
        AppleApplicationHandler macAdapter = new AppleApplicationHandler();
        a.addApplicationListener(macAdapter);

        // need to enable the preferences option manually
        a.setEnabledPreferencesMenu(true);

//
//  com.apple.eawt.Application.getApplication().getDockMenu();
//
//  com.apple.eawt.Application.getApplication().setDockMenu( java.awt.PopupMenu );
//
//  com.apple.eawt.Application.getApplication().setDockIconBadge( String );
        //System.setProperty("apple.awt.fileDialogForDirectories", "true");
        // Test these...
        //System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
        //System.setProperty( "com.apple.mrj.application.growbox.intrudes", "false" );
        //System.setProperty( "com.apple.macos.smallTabs", "true" );
    }

    try
    {
      // Set cross-platform Java L&F (also called "Metal")
      javax.swing.UIManager.setLookAndFeel( javax.swing.UIManager.getSystemLookAndFeelClassName() );

      LOGGER.log( Level.FINE, "GUI look and feel set to ''{0}''", javax.swing.UIManager.getSystemLookAndFeelClassName());
    }
    catch ( Exception e )
    {
      LOGGER.log( Level.WARNING, "Couldn't set GUI look and feel", e );
    }
    //JFrame.setDefaultLookAndFeelDecorated(true);
    //JDialog.setDefaultLookAndFeelDecorated(true);

    Tools.initialGUISetup();
    EventQueue.invokeLater( new Runnable()
    {
      @Override
      public void run()
      {
        MainMDIWindow mainWindow = new MainMDIWindow();
        mainWindow.setVisible( true );
//        if ( Tools.isMac() )
//        {
//          com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen( mainWindow, true );
//        }
      }
    } );
  }
    
}
