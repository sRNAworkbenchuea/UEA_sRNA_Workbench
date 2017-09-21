/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 *
 * @author w0445959
 */
public class CoLIDERunner extends ToolRunner
{
  private CoLIDEProcess c_engine;
  public CoLIDERunner(ToolHost host )
  {
    super(host);
  }
  
  /**
   * Requests the the CoLIDE tool is executed.  Progress is displayed via the provided
   * StatusTracker object.
   * @param in_file The sample files
   * @param genome_file The genome file
   * @param params The CoLIDE parameters to use
   * @param tracker The progress tracking components
   */
  public void runCoLIDETool(ArrayList<ArrayList<File>> in_files, 
                            File genome_file, 
                            CoLIDEParams params, 
                            FilterResultsPanel filterResultsTab, 
                            StatusTracker tracker,
                            CoLIDEResultsPanel resultsTable)
  {
    try
    {
      for(ArrayList<File> fileName : in_files)
      {
        if ( fileName == null || fileName.isEmpty())
        {
          throw new IOException( "At least one sample list contains no data." );
        }
      }

      if ( genome_file == null )
      {
        throw new IOException( "Must specify genome file." );
      }

      if ( params == null )
      {
        throw new IOException( "Must specify a valid set of parameters to control the CoLIDE tool." );
      }

      c_engine = new CoLIDEProcess(tracker, in_files, genome_file, params);
      c_engine.inputResultPanels( filterResultsTab, resultsTable );

      this.run( c_engine );
    }
    catch(IOException ioe)
    {
      this.host.showErrorDialog( ioe.getMessage() );
    }
  }
  
  public SparseExpressionMatrix getExpressionData()
  {
    return this.c_engine.getExpressionMatrix();
  }
  

}
