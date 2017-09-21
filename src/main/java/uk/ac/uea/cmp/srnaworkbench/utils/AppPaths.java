/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.exceptions.ToolsInitializationFailed;

/**
 *
 * @author ezb11yfu
 */
public enum AppPaths
{
  INSTANCE;
  
  private File cwd;
  private File projectDir;
  private File rootDir;
  
  private AppPaths()
  {
    AppUtils.Architecture arch = AppUtils.INSTANCE.getArchitecture();
    boolean ideMode = AppUtils.INSTANCE.isRunningInIDE();

    // Set the current working directory.
    cwd = new File( "" );
    
    String toolsClassFilePath = null;

    try
    {
      URL location = AppPaths.class.getProtectionDomain().getCodeSource().getLocation();
      URI uri = location.toURI();
      toolsClassFilePath = uri.toString();
    }
    catch ( URISyntaxException ex )
    {
      throw new ToolsInitializationFailed( "Tools initialisation failed due to URISyntaxException" );
    }
    
    // The JAR name will be present when running from outside of the IDE, remove 
    // it if necessary.
    String tempDirSuffixTrimmed = toolsClassFilePath.replace( "Workbench.jar", "" );

    // The /build/classes/ directory will be present at the end of the path if 
    // running in the IDE, remove it if necessary.
    tempDirSuffixTrimmed = tempDirSuffixTrimmed.replace( "/classes/", "" );

    // Remove the "file:" prefix.
    String tempDirPrefixTrimmed = tempDirSuffixTrimmed.replace( "file:", "" );

    boolean isUNCPath = tempDirPrefixTrimmed.startsWith( "//" );

    // Paths will be unix style, carrying a forward slash at the beginning of the path,
    // if on windows this needs to be removed, but only if this isn't a UNC path.
    tempDirPrefixTrimmed = ( arch.isWindows() && !isUNCPath ) ? tempDirPrefixTrimmed.substring( 1 ) : tempDirPrefixTrimmed;
    
    // If on windows convert all forward slashes to backslashes.
    String projectDirPath = arch.isWindows() ? tempDirPrefixTrimmed.replace( "/", String.valueOf( DIR_SEPARATOR ) ) : tempDirPrefixTrimmed;
    File pDir = new File ( projectDirPath );
    
    if ( pDir == null || !pDir.exists() || !pDir.isDirectory() )
      throw new NullPointerException("Couldn't locate project directory at " + projectDirPath);
    
    // If running in IDE mode then add the dist directory to the root directory.
    String dist_extension = ideMode ? ( DIR_SEPARATOR + "release" ) : "";
    String rootDirPath = projectDirPath + dist_extension;
    File rDir = new File ( rootDirPath );
    if ( rDir == null || !rDir.exists() || !rDir.isDirectory() )
      throw new NullPointerException("Couldn't locate root directory at " + rootDirPath);
    
    projectDir = pDir;
    rootDir = rDir;
  }
  
  /**
   * Gets the current working directory
   * @return The current working directory
   */
  public File getCWD()
  {
    return this.cwd;
  }
  
  /**
   * Gets the project directory (if running directly from jar this will be the same
   * as the root directory)
   * @return The project directory
   */
  public File getProjectDir()
  {
    return this.projectDir;
  }
  
  /**
   * Gets the root directory of the workbench installation
   * @return The root directory
   */
  public File getRootDir()
  {
    return this.rootDir;
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( "CWD: ").append( this.cwd.getPath() ).append( "\n" );
    sb.append( "Project Dir: ").append( this.projectDir.getPath() ).append( "\n" );
    sb.append( "Root Dir: ").append( this.rootDir.getPath() ).append( "\n" );
    
    return sb.toString();
  }
}
