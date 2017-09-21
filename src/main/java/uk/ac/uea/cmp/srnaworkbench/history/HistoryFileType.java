/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.history;

import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public enum HistoryFileType
{
  SRNA
  {
    @Override
    public String selectHistory(  )
    {
      File shortReadFileHistory = new File(Tools.history_dataPath + DIR_SEPARATOR + "sRNA_History");

      String input_path = shortReadFileHistory == null ? "" : shortReadFileHistory.getPath();
      return input_path;
    }

    @Override
    public void writeHistory( String pathAndInfo )
    {

      writeDataOut( Tools.history_dataPath + DIR_SEPARATOR + "sRNA_History", pathAndInfo );
    }
  },
  MRNA
  {
    @Override
    public String selectHistory(  )
    {
      File shortReadFileHistory = new File(Tools.history_dataPath + DIR_SEPARATOR + "mRNA_History");

      String input_path = shortReadFileHistory == null ? "" : shortReadFileHistory.getPath();
      return input_path;
    }

    @Override
    public void writeHistory( String pathAndInfo )
    {

      writeDataOut( Tools.history_dataPath + DIR_SEPARATOR + "mRNA_History", pathAndInfo );
    }
  },
  TRANSCRIPTOME
  {
    @Override
    public String selectHistory(  )
    {
      File shortReadFileHistory = new File(Tools.history_dataPath + DIR_SEPARATOR + "transcriptome_History");

      String input_path = shortReadFileHistory == null ? "" : shortReadFileHistory.getPath();
      return input_path;
    }

    @Override
    public void writeHistory( String pathAndInfo )
    {

      writeDataOut( Tools.history_dataPath + DIR_SEPARATOR + "mRNA_History", pathAndInfo );
    }
  },
  GENOME
  {
    @Override
    public String selectHistory(  )
    {
      File genomeFileHistory = new File(Tools.history_dataPath + DIR_SEPARATOR + "genome_History" );

      String input_path = genomeFileHistory == null ? "" : genomeFileHistory.getPath();
      return input_path;
    }
    @Override
    public void writeHistory( String pathAndInfo )
    {
      writeDataOut( Tools.history_dataPath + DIR_SEPARATOR + "genome_History", pathAndInfo);
    }
  },
  TEMP
  {
    @Override
    public String selectHistory( )
    {
      File tempDirectoryHistory = new File(Tools.history_dataPath + DIR_SEPARATOR + "temp_History");

      String input_path = tempDirectoryHistory == null ? "" : tempDirectoryHistory.getPath();
      return input_path;
    }
    @Override
    public void writeHistory( String pathAndInfo )
    {
      writeDataOut( Tools.history_dataPath + DIR_SEPARATOR + "temp_History", pathAndInfo);
    }
  },
  NONE
  {
    @Override
    public String selectHistory( )
    {
      return "";
    }
    @Override
    public void writeHistory( String pathAndInfo )
    {
      
    }
    
  };

  public abstract String selectHistory(  );
  public abstract void writeHistory( String pathAndInfo );
  
  private static void writeDataOut(String historyFilePath, String pathAndInfo)
  {
    try
    {
      // Open the file that is the first
      // command line parameter
      FileInputStream fstream = new FileInputStream( historyFilePath );
      // Get the object of DataInputStream
      DataInputStream in = new DataInputStream( fstream );
      BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
      String strLine;
      StringBuilder fileContent = new StringBuilder();
      //Read File Line By Line
      boolean appendRequired = true;
      while ( ( strLine = br.readLine() ) != null )
      {
        
        String tokens[] = strLine.split( "\t" );
        String inputTokens[] = pathAndInfo.split("\t");
        if( ( tokens.length == 3 ) && (inputTokens.length == 3))
        {
          // Here tokens[0] will have value of ID
          if ( tokens[0].equals( inputTokens[0] ) )
          {
            tokens[1] = inputTokens[1];
            tokens[2] = inputTokens[2];
            String newLine = tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + LINE_SEPARATOR;
            fileContent.append( newLine );
            appendRequired = false;
          }
          else
          {
            // update content as it is
            fileContent.append( strLine ).append( LINE_SEPARATOR);
            //appendRequired = false;
          }
        }
      }
      if(appendRequired)
      {
        //none so write the first entry with no edits...
        fileContent.append( pathAndInfo );
        fileContent.append( LINE_SEPARATOR );
      }


      // Now fileContent will have updated content , which you can override into file
      FileWriter fstreamWrite = new FileWriter( historyFilePath );
      BufferedWriter out = new BufferedWriter( fstreamWrite );
      out.write( fileContent.toString() );
      out.close();
      //Close the input stream
      in.close();
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.WARNING, "History read/write error: {0}", ex.getMessage());
    }

  }
}
