/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * A collection of File Dialog Box helper methods
 * @author Matthew Stocks and Dan Mapleson
 */
public final class FileDialogUtils
{
  // Records the system wide last directory the user requested from a file dialog.
  // Initialise with the workbench root directory
  private static String lastDir = Tools.ROOT_DIR;

  public static String getLastDir()
  {
    return lastDir;
  }

  public static void setLastDir( String lastDir )
  {
    FileDialogUtils.lastDir = lastDir;
  }

  public enum FileExtFilter
  {
    RAW_READS( new FileNameExtensionFilter( "Raw Read Files (.fq, .fastq, .fa, .fas, .fasta, .txt)",
    "fq",
    "fastq",
    "fa",
    "fas",
    "fasta",
    "txt"
    )),
    FASTA( new FileNameExtensionFilter( "FastA File (.fa, .fas, .fasta, .txt)",
    "fa",
    "fas",
    "fasta",
    "txt"
    )),
    FASTQ( new FileNameExtensionFilter( "FastQ File (.fq, .fastq, .txt)",
      "fq",
      "fastq",
      "txt"
    )),
    PATMAN( new FileNameExtensionFilter( "Patman File (.patman, .pat)",
      "patman",
      "pat"
    )),
    PARAMETER( new FileNameExtensionFilter( "Workbench Parameter File (.cfg, .params, .txt)",
        "cfg",
        "params",
        "txt"
    )),
    GFF( new FileNameExtensionFilter( "General Feature Format (.gff, .gff3)", "gff", "gff3")),
    PDF( new FileNameExtensionFilter( "PDF document (.pdf)", "pdf" )),
    CSV( new FileNameExtensionFilter( "Comma Separated Value File (.csv)", "csv" )),
    TEXT( new FileNameExtensionFilter( "Plain Text File (.txt)", "txt" ));

    private final FileNameExtensionFilter ff;

    private FileExtFilter(FileNameExtensionFilter ff)
    {
      this.ff = ff;
    }

    public FileNameExtensionFilter getFilter()  {return this.ff;}

    /**
     * Creates an array of @link{FileNameExtensionFilter} from a list of FileExtFilters
     * @param filters Items to create filter array of.
     * @return A @link{FileNameExtensionFilter} array, created from FileExtFilter items
     */
    public static FileNameExtensionFilter[] toFilterArray(FileExtFilter... filters)
    {
      FileNameExtensionFilter[] fa = new FileNameExtensionFilter[filters.length];

      for(int i = 0; i < fa.length; i++)
      {
        fa[i] = filters[i].getFilter();
      }

      return fa;
    }
  }

  /**
   * Prevents instantiation
   */
  private FileDialogUtils(){}

  /**
   * Allows the user to request an open file dialog which returns a single file
   * using a single file filter extension.
   * @param extension Valid file extensions to filter on
   * @param parent Parent form (not required).
   * @return File File to open
   */
  public static File showSingleFileOpenDialog( FileNameExtensionFilter extension, Component parent )
  {
      
    return showSingleFileOpenDialog(new FileNameExtensionFilter[]{extension}, parent);
  }

  /**
   * Allows the user to request an open file dialog which returns a single file
   * using an array of file filters.
   * @param extensions Array of valid FileFilters to filter on
   * @param parent Parent form (not required).
   * @return File File to open
   */
  public static File showSingleFileOpenDialog( FileNameExtensionFilter[] extensions, Component parent )
  {
    JFileChooser chooser = createChooser( null, extensions, false, false );

    List<File> files = fileOpen( chooser, parent, false );

    if ( files == null || files.size() != 1 )
    {
      return null;
    }

    return files.get( 0 );
  }

  /**
   * Allows the user to request an open file dialog which can return multiple files
   * using a single FileFilter extension.
   * @param extension Valid file extensions to filter on
   * @param parent Parent form (not required).
   * @return List of Files to open
   */
  public static List<File> showMultipleFileOpenDialog( FileNameExtensionFilter extension, Component parent )
  {
    return showMultipleFileOpenDialog(new FileNameExtensionFilter[]{extension}, parent);
  }

  /**
   * Allows the user to request an open file dialog which returns a single file
   * using an array of file filters.
   * @param extensions Array of valid FileFilters to filter on
   * @param parent Parent form (not required).
   * @return List of Files to open
   */
  public static List<File> showMultipleFileOpenDialog( FileNameExtensionFilter[] extensions, Component parent )
  {
    JFileChooser chooser = createChooser( null, extensions, true, false );

    return fileOpen(chooser, parent, true);
  }

  private static JFileChooser createChooser(String dialogTitle, FileNameExtensionFilter[] extensions, boolean multipleFiles, boolean directoriesOnly )
  {
    JFileChooser chooser = new JFileChooser();

    if ( dialogTitle != null && ! dialogTitle.isEmpty() )
    {
      chooser.setDialogTitle( dialogTitle );
    }

    chooser.setCurrentDirectory( new File( lastDir ) );

    chooser.setMultiSelectionEnabled( multipleFiles );

    if ( directoriesOnly )
    {
      chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
    }

    if ( extensions != null )
    {
      for ( FileFilter ff : extensions )
      {
        chooser.addChoosableFileFilter( ff );
      }
    }

    return chooser;
  }

  private static List<File> fileOpen(JFileChooser chooser, Component parent, boolean multiple_files )
  {
    int returnVal = chooser.showOpenDialog( parent );
    chooser.invalidate();
    chooser.revalidate();

    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {

      if ( multiple_files )
      {
        lastDir = chooser.getCurrentDirectory().getPath();

        LOGGER.log( Level.FINE, "{0} files were opened:", chooser.getSelectedFiles().length);
        
        for ( File files : chooser.getSelectedFiles() )
        {
          LOGGER.log( Level.FINE, "{0}", files.getAbsolutePath() );  
        }
        
        return Arrays.asList( chooser.getSelectedFiles() );
      }
      else
      {
        LOGGER.log( Level.FINE, "You chose to open: {0}", chooser.getSelectedFile().getName());
        
        File[] singleFileList =
        {
          chooser.getSelectedFile()
        };
        lastDir = chooser.getCurrentDirectory().getPath();
        return Arrays.asList( singleFileList );
      }
    }
      return null;
  }



  /***
   * Display a file->save dialog, with a file filter provided
   *
   * @param parent       The parent component
   * @param extension    The file extension
   * @param description  A description
   * @return             The file or null if the dialog is cancelled
   */
  public static File showFileSaveDialog( Component parent, FileNameExtensionFilter extension )
  {
    File f = showFileSaveDialog( parent, new FileNameExtensionFilter[]{extension}, false );

    return f;
  }

  /***
   * Display a file->save dialog, with a file filter provided
   *
   * @param parent       The parent component
   * @param extension    The file extension
   * @param description  A description
   * @return             The file or null if the dialog is cancelled
   */
  public static File showFileSaveDialog( Component parent, String title, FileNameExtensionFilter extension )
  {
    File f = showFileSaveDialog( parent, title, new FileNameExtensionFilter[]{extension}, false );

    return f;
  }

  /***
   * Display a file->save dialog, with a list of file filters provided
   *
   * @param parent       The parent component
   * @param extensions   List of file filters
   * @param description  A description
   * @return             The file or null if the dialog is cancelled
   */
  public static File showFileSaveDialog( Component parent, FileNameExtensionFilter[] extensions )
  {
    File f = showFileSaveDialog( parent, extensions, false );

    return f;
  }


  /***
   * Display a file->save dialog, with a list of file filters.  User may also specify whether to
   * only display directories or not.
   *
   * @param parent       The parent component
   * @param extension    The file filter
   * @param directoriesOnly  Whether to only look at directories
   * @return             The file or null if the dialog is cancelled
   */
  public static File showFileSaveDialog( Component parent, FileNameExtensionFilter[] extensions, boolean directoriesOnly )
  {
    return showFileSaveDialog( parent, "", extensions, directoriesOnly );
  }
  public static File showFileSaveDialog( Component parent, String initialPath, String title, FileNameExtensionFilter[] extensions, boolean directoriesOnly )
  {
    
    JFileChooser chooser = createChooser( title, extensions, false, directoriesOnly );

    chooser.setCurrentDirectory( new File(initialPath));
    int returnVal = chooser.showSaveDialog( parent );

    File f = chooser.getSelectedFile();

    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      lastDir = chooser.getCurrentDirectory().getPath();

      if ( f.exists() && !directoriesOnly )
      {
        f = confirmOverwrite(f);
      }
    }

    // If user selects All Files then this returns a FileFilter of a different type
    // than we would expect, so in that case don't add any extension to the file.
    if ( f != null && ( chooser.getFileFilter() instanceof FileNameExtensionFilter) )
    {
      f = FileUtils.addFileExtensionIfRequired( f, (FileNameExtensionFilter)chooser.getFileFilter() );
    }

    return f;
  }

  /**
   * Display a file->save dialog, with a list of file filters.  User may also specify whether to
   * only display directories or not.
   *
   * @param parent       The parent component
   * @param title        Title for the dialog
   * @param extension    The file filter
   * @param directoriesOnly  Whether to only look at directories
   * @return             The file or null if the dialog is cancelled
   */
  public static File showFileSaveDialog( Component parent, String title, FileNameExtensionFilter[] extensions, boolean directoriesOnly )
  {
    JFileChooser chooser = createChooser( title, extensions, false, directoriesOnly );

    int returnVal = chooser.showSaveDialog( parent );

    File f = chooser.getSelectedFile();

    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      lastDir = chooser.getCurrentDirectory().getPath();

      if ( f.exists() && !directoriesOnly )
      {
        f = confirmOverwrite(f);
      }
    }

    // If user selects All Files then this returns a FileFilter of a different type
    // than we would expect, so in that case don't add any extension to the file.
    if ( f != null && ( chooser.getFileFilter() instanceof FileNameExtensionFilter) )
    {
      f = FileUtils.addFileExtensionIfRequired( f, (FileNameExtensionFilter)chooser.getFileFilter() );
    }

    return f;
  }

  /**
   * Mainly used for saving an image
   *
   * @param parent
   * @param extensions
   * @param description
   * @param directoriesOnly
   * @param selectedExtension
   * @return
   */
  public static File showFileSaveDialog( Component parent, String[] extensions, String description, boolean directoriesOnly, String[] selectedExtension )
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory( new File( lastDir ) );
    if ( directoriesOnly )
    {
      chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
    }

    if ( extensions != null )
    {
      FileTypeFilter useMe = null;

      for ( String filterExt : extensions )
      {
        FileTypeFilter filter = new FileTypeFilter( filterExt, description );
        chooser.addChoosableFileFilter( filter );
        chooser.setAcceptAllFileFilterUsed( false );

        // Favour jpg when doing images
        if ( filterExt.toLowerCase().indexOf( "jpg" ) != -1 )
        {
          useMe = filter;
        }
      }

      if ( useMe != null )
      {
        chooser.setFileFilter( useMe );
      }
    }

    int returnVal = chooser.showSaveDialog( parent );


    if ( returnVal == JFileChooser.APPROVE_OPTION )
    {
      lastDir = chooser.getCurrentDirectory().getPath();

      if ( chooser.getFileFilter() == null )
      {
        JOptionPane.showMessageDialog( null,
          "No File Format Selected.",
          "Failed to Save Images",
          JOptionPane.ERROR_MESSAGE );

        return null;
      }

      selectedExtension[0] = chooser.getFileFilter().getDescription().split( "\\(" )[1].replace( "(", "" ).replace( ")", "" ).replace( ".", "" );

      File f = chooser.getSelectedFile();

      if ( !selectedExtension[0].isEmpty() )
      {
        FileNameExtensionFilter ext = new FileNameExtensionFilter("Whatever", selectedExtension[0]);

        f = FileUtils.addFileExtensionIfRequired( f, ext );
      }

      if ( f.exists() && !directoriesOnly )
      {
        return confirmOverwrite(f);
      }
      else
      {
        return f;
      }
    }

    return null;
  }

  private static File confirmOverwrite(File f)
  {
    int response = JOptionPane.showConfirmDialog( null, "Are you sure you want to overwrite '" + f.getPath() + "'?", "Confirm",
      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );

    switch(response)
    {
      case JOptionPane.NO_OPTION:
        return null;
      case JOptionPane.YES_OPTION:
        return f;
      case JOptionPane.CLOSED_OPTION:
        return null;
    }

    return null;
  }
}
