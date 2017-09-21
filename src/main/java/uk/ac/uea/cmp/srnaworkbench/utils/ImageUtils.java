/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.Level;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author prb07qmu
 */
public class ImageUtils
{

  /**
   * Save a {@code Component} to a file as an image
   * @param cmp Specified {@code Component}
   * @param formatName Name of the image format (e.g. JPG, PNG) - see {@code formatName} in {@code ImageIO.write}
   * @param filename Name of the file
   * @throws IOException
   */
  public static void saveComponentAsImage( Component cmp, String formatName, String filename ) throws IOException
  {
    saveComponentAsImage( cmp, formatName, new File( filename ) );
  }

  /***
   * Save a {@code Component} to a file as an image
   * @param cmp Specified {@code Component}
   * @param formatName Name of the image format (e.g. JPG, PNG) - see {@code formatName} in {@code ImageIO.write}
   * @param file {@code File} object for the image
   * @throws IOException
   */
  public static void saveComponentAsImage( Component cmp, String formatName, File file ) throws IOException
  {
    int w = cmp.getWidth();
    int h = cmp.getHeight();

    BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bim.getGraphics();
    cmp.paintAll( g );
    g.dispose();

    ImageIO.write( bim, formatName, file );
  }


  /**
   * Utility method when generating screen shots
   */
  public static File saveComponentAsImageWithDialog( Component c ) throws IOException
  {
    String[] availableFormats  = FileTypeFilter.getImageFormats();
    String[] selectedExtension = {""};

    File file = FileDialogUtils.showFileSaveDialog( c, availableFormats, "Select image format", false, selectedExtension );

    if ( file != null )
    {
      FileNameExtensionFilter ext = new FileNameExtensionFilter("Whatever", selectedExtension[0]);

      file = FileUtils.addFileExtensionIfRequired( file, ext );

      saveComponentAsImage( c, selectedExtension[0], file );
    }

    return file;
  }

  /**
   * Action class (associate with a button or menuitem)
   */
  public static class TakePictureAction extends AbstractAction
  {
    private final Component _targetComponent;
    private final Component _feedbackComponent;

    public TakePictureAction( AbstractButton source, Component targetComponent, Component feedbackComponent )
    {
      super( source.getText(), source.getIcon() );

      super.putValue( Action.SHORT_DESCRIPTION, source.getToolTipText() );

      _targetComponent = targetComponent;
      _feedbackComponent = feedbackComponent;
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      File imgFile = null;
      String message = "";

      try
      {
        imgFile = saveComponentAsImageWithDialog( _targetComponent );

        if ( imgFile != null )
        {
          message = "Saved to " + imgFile.getAbsolutePath();
        }
      }
      catch ( IOException ex )
      {
        WorkbenchLogger.LOGGER.log( Level.WARNING, null, e );
        message = "An error occurred while creating the file : " + ( imgFile == null ? "<null>" : imgFile.getAbsolutePath() );
      }

      if ( message.isEmpty() )
        return;

      if ( _feedbackComponent instanceof JLabel )
      {
        ( (JLabel)_feedbackComponent ).setText( message );
      }
      else if ( _feedbackComponent instanceof JTextComponent )
      {
        ( (JTextComponent)_feedbackComponent ).setText( message );
      }
    }
  }
}
