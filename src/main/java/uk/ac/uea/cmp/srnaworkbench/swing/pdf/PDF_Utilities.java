/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.swing.pdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;

import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class PDF_Utilities
{
  
  public static PDDocument openPDFFile(File f)
  {
    PDDocument document = null;
    try
    {
      document = PDDocument.load( f );
      document.getClass();
      if ( document.isEncrypted() )
      {
        try
        {

          document.decrypt( "" );


        }
        catch ( CryptographyException ex )
        {
          LOGGER.log(Level.SEVERE, ex.getMessage() );
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
    
    return  document;
  }
  
   public static PDDocument openPDFFile(InputStream f)
  {
    PDDocument document = null;
    try
    {
      document = PDDocument.load( f );
      document.getClass();
      if ( document.isEncrypted() )
      {
        try
        {

          document.decrypt( "" );


        }
        catch ( CryptographyException ex )
        {
          LOGGER.log(Level.SEVERE, ex.getMessage() );
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
    
    return  document;
  }
  
}
