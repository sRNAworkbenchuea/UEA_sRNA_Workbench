/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.awt.Color;
import java.net.URISyntaxException;
import javax.swing.JInternalFrame;
import uk.ac.uea.cmp.srnaworkbench.MDIDesktopPane;
import uk.ac.uea.cmp.srnaworkbench.MainMDIWindow;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author prb07qmu + w0445959
 */
public enum AppUtils
{
  // Single item enum - guaranteed singleton (doesn't need a getInstance() method)
  INSTANCE;

  // Constant(s) ///////////////////////////////////////////////////////////////
  
  public static final String APP_NAME = "UEA sRNA Workbench";
  
  
  // MDI Desktop pane //////////////////////////////////////////////////////////

  private MDIDesktopPane _mdiDesktopPane;
  
  private MainMDIWindow _mdiMainWindow;

  public void setMDIDesktopPane( MDIDesktopPane pane )
  {
    _mdiDesktopPane = pane;
  }

  public void activateFrame( JInternalFrame frame )
  {
    _mdiDesktopPane.activateFrame( frame );
  }
  public MDIDesktopPane getMDIDesktopPane()
  {
    return _mdiDesktopPane;
  }

  // Colours ///////////////////////////////////////////////////////////////////

  //private final Color WB_GRAY = new Color( 0x00787878 );

  public Color getWorkbenchGray()
  {
    return new Color( 0x00787878 );
  }

  private final Color V_LIGHT_GRAY = new Color( 245, 245, 245 );

  public Color getVeryLightGray()
  {
    return V_LIGHT_GRAY;
  }

  //////////////////////////////////////////////////////////////////////////////

  private final boolean SHOW_DEV_FEATURES = Boolean.valueOf( System.getProperty( "show-dev-features" ) ).booleanValue();

  public boolean getShowDevFeatures()
  {
    return SHOW_DEV_FEATURES;
  }

  // Verbosity flag ////////////////////////////////////////////////////////////

  private boolean _verbose = false;

  public void setVerbose( boolean value )
  {
    _verbose = value;
  }

  public boolean verbose()
  {
    return _verbose;
  }

  //////////////////////////////////////////////////////////////////////////////

  private BinaryExecutor _binaryExecutor = new BinaryExecutor();

  public BinaryExecutor getBinaryExecutor()
  {
     
    return _binaryExecutor;
  }

  public void setBinaryExecutor( BinaryExecutor binaryExecutor )
  {
    _binaryExecutor = binaryExecutor;
  }
  
  public static String addQuotesIfNecessary( String s )
  {
    if ( s.indexOf( ' ' ) == -1 )
    {
      return s;
    }

    if ( !s.startsWith( "\"" ) )
    {
      s = "\"" + s;
    }

    if ( !s.endsWith( "\"" ) )
    {
      s = s + "\"";
    }

    return s;
  }

  //////////////////////////////////////////////////////////////////////////////

  private final Architecture _os = Architecture.getArchitecture();

  public Architecture getArchitecture()
  {
    return _os;
  }

  public void setMainMDIWindow( MainMDIWindow window )
  {
    _mdiMainWindow = window;
  }
  
  public MainMDIWindow getMainMDIWindow()
  {
    return _mdiMainWindow;
  }

  public enum Architecture
  {
    MAC
    {
      @Override public String getBinariesDirectory() { return "OSX"; }
      @Override public String getLibExtension() { return ".so"; }
      @Override public boolean isMac() { return true; }
    },
    LINUX
    {
      @Override public String getBinariesDirectory() { return "linux"; }
      @Override public String getLibExtension() { return ".so"; }
      @Override public boolean isLinux() { return true; }
    },
    WINDOWS
    {
      @Override public String getBinariesDirectory() { return "win"; }
      @Override public String getExeExtension() { return ".exe"; }
      @Override public String getLibExtension() { return ".dll"; }
      @Override public String getQuoteString() { return "\""; }
      @Override public boolean isWindows() { return true; }
    };

    public String getBinariesDirectory() { return ""; }
    public String getExeExtension() { return ""; }
    public String getLibExtension() { return ""; }
    public String getQuoteString() { return ""; }

    public boolean isMac() { return false; }
    public boolean isLinux() { return false; }
    public boolean isWindows() { return false; }

    private static Architecture getArchitecture()
    {
      String osName = System.getProperty( "os.name" ).toLowerCase();

      if ( osName.indexOf( "win" ) != -1 )
        return WINDOWS;

      if ( osName.indexOf( "nix" ) != -1 || osName.indexOf( "nux" ) != -1 )
        return LINUX;

      if ( osName.indexOf( "mac" ) != -1 )
        return MAC;

      throw new RuntimeException( "The '" + osName + "' operating system is currently unsupported." );
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private boolean _isCommandLine = false;

  public boolean isCommandLine()
  {
    return _isCommandLine;
  }

  public void setCommandLine( boolean b )
  {
    _isCommandLine = b;
  }

  //////////////////////////////////////////////////////////////////////////////
  
  private boolean _is_BASESPACE = false;
  
  public boolean isBaseSpace()
  {
    return _is_BASESPACE;
  }

  public void setBaseSpace( boolean b )
  {
    _is_BASESPACE = b;
  }

  private boolean _isRunningInIDE = false;
  private boolean _isRunningInIDESet = false;
  
  public boolean isRunningInIDE()
  {
    if ( !_isRunningInIDESet )
    {
      String classLoc = null;
      try
      {
        classLoc = AppUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      }
      catch ( URISyntaxException ex )
      {
        throw new RuntimeException( "Couldn't determine if executable is running from IDE due to URISyntaxException" );
      }

      if (classLoc == null)
        throw new NullPointerException("Couldn't determine if executable is running from IDE due to Null pointer");
      
      _isRunningInIDE = classLoc.contains( "/classes/" );
      _isRunningInIDESet = true;
    }
          
    return _isRunningInIDE;
  }
    
  //////////////////////////////////////////////////////////////////////////////
  
  public static void main( String... args )
  {
  }
}
