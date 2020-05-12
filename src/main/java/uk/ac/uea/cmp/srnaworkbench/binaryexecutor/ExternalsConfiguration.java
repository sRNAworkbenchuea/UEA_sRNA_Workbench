/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.binaryexecutor;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class ExternalsConfiguration
{
  /* Initialised flag */
  private static boolean _initialised = false;

  /* This will be set externally. The Workbench.jar is in this directory. */
  private static String _rootPath = "";

  /* Set relative to _rootPath */
  private static String _binariesPath;
  
  /* Path to the directory to output server logs */
  private static String _logPath;

  /* Verbose flag */
  private static boolean _verbose = false;
 


  public static void setRootPath( String binPath ) throws IOException
  {
    if ( binPath == null || binPath.trim().isEmpty() )
    {
      return;
    }

    if ( !binPath.endsWith( String.valueOf( DIR_SEPARATOR ) ) )
    {
      binPath += DIR_SEPARATOR;
    }

    _rootPath = binPath;
    _binariesPath = _rootPath + "ExeFiles" + DIR_SEPARATOR + AppUtils.INSTANCE.getArchitecture().getBinariesDirectory() + DIR_SEPARATOR;

    
    checkPermissions();

    _initialised = true;
  }

  static String getRootPath()
  {
    if ( _initialised )
    {
      return _rootPath;
    }

    throw new IllegalStateException( "Externals: The working directory for the external executable file has not been set." );
  }

  static String getBinariesPath()
  {
    return _binariesPath;
  }

  static String getLogPath()
  {
    return _logPath;
  }

  static boolean verbose()
  {
    return _verbose;
  }

  private static void checkPermissions() throws IOException
  {
    // We just don't do this on Windows
    if ( AppUtils.INSTANCE.getArchitecture().isWindows() )
      return;

    String path = getBinariesPath();

    String[] executables = { "gs", "patman", "RNAplot", "RNAfold", "randfold", "RNAplex",
    "bowtie" + DIR_SEPARATOR + "bowtie",
    "bowtie" + DIR_SEPARATOR + "bowtie-align-s",
    "bowtie" + DIR_SEPARATOR + "bowtie-build-l",
    "bowtie" + DIR_SEPARATOR + "bowtie-build-s-debug",
    "bowtie" + DIR_SEPARATOR + "bowtie-inspect-l-debug",
    "bowtie" + DIR_SEPARATOR + "bowtie-align-l",
    "bowtie" + DIR_SEPARATOR + "bowtie-align-s-debug",
    "bowtie" + DIR_SEPARATOR + "bowtie-build-l-debug",
    "bowtie" + DIR_SEPARATOR + "bowtie-inspect",
    "bowtie" + DIR_SEPARATOR + "bowtie-inspect-s",
    "bowtie" + DIR_SEPARATOR + "bowtie-align-l-debug",
    "bowtie" + DIR_SEPARATOR + "bowtie-build",
    "bowtie" + DIR_SEPARATOR + "bowtie-build-s",
    "bowtie" + DIR_SEPARATOR + "bowtie-inspect-l",
    "bowtie" + DIR_SEPARATOR + "bowtie-inspect-s-debug"};

    for ( String executableName : executables )
    {
      addExecutePermission( path + executableName );
    }
  }

  private static void addExecutePermission( String executablePath ) throws IOException
  {
    String command = "chmod +x " + executablePath;

    LOGGER.log( Level.INFO, "{0}", command);
    
    // This call may throw an IOException
    // The permissions are changed to make sure the executables actually have execute permissions
    Runtime.getRuntime().exec( command );
  }
}
