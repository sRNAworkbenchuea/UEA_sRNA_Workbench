/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.swing;

import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.history.HistoryFileType;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 * Describes different types of File Selector 
 * @author Dan Mapleson
 */
public enum FileSelector
{
  LOAD
  {
    @Override
    public String select(FileNameExtensionFilter[] filters, HistoryFileType fileType, String info)
    {
      File shortReadFile = FileDialogUtils.showSingleFileOpenDialog( filters, null );

      String input_path = shortReadFile == null ? "" : shortReadFile.getPath();
//      if(!input_path.isEmpty())
//        fileType.writeHistory(input_path + Tools.TAB + info + Tools.TAB + Tools.getDateTime());
      return input_path;
    }
  },
  MULTI_LOAD
  {
    @Override
    public String select(FileNameExtensionFilter[] filters, HistoryFileType fileType, String info)
    {
      List<File> shortReadFiles = FileDialogUtils.showMultipleFileOpenDialog( filters, null );

      String input_path = "";
      if ( shortReadFiles != null )
      {
        for ( File shortReadFile : shortReadFiles )
        {
          input_path += shortReadFile == null ? "" : shortReadFile.getPath() + LINE_SEPARATOR;
//          String writeMe = shortReadFile == null ? "" : shortReadFile.getPath();
//          if(!writeMe.isEmpty())
//            fileType.writeHistory(writeMe + Tools.TAB + info + Tools.TAB + Tools.getDateTime());
        }
        
      }
      return input_path;
    }
  },
  SAVE
  {
    @Override
    public String select(FileNameExtensionFilter[] filters, HistoryFileType fileType, String info)
    {
      File tempOutput = FileDialogUtils.showFileSaveDialog( null, filters );

      String output_path = tempOutput == null ? "" : tempOutput.getPath();
//      if(!output_path.isEmpty())
//        fileType.writeHistory(output_path + Tools.TAB + info + Tools.TAB + Tools.getDateTime());
      return output_path;  
    }
  },
  DIRECTORY
  {
    @Override
    public String select( FileNameExtensionFilter[] filters, HistoryFileType fileType, String info )
    {
      File newTempDir = FileDialogUtils.showFileSaveDialog( new JFrame(), null, true );

      String output_path = newTempDir == null ? "" : newTempDir.getPath();
//      if(!output_path.isEmpty())
//        fileType.writeHistory(output_path + Tools.TAB + info + Tools.TAB + Tools.getDateTime());
      return output_path;
    }
  },
  OTHER
  {
    @Override
    public String select(FileNameExtensionFilter[] filters, HistoryFileType fileType, String info)
    {
      throw new UnsupportedOperationException("OTHER: does not currently have a selector implemented.");
    }
  };

  public abstract String select(FileNameExtensionFilter[] filters, HistoryFileType fileType, String info);
}
