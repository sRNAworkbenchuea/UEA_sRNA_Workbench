/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.normalise;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolHost;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 * ToolRunner for NormalisationProcess
 * @author Matt
 */
public class NormalisationRunner extends ToolRunner
{
  
  // used to make sure result is only displayed the first time this runner is
  // checked for completion
  boolean resultShown = false;
  
  //switch to new database code or not
  boolean databaseMode = false;
  
  // Should probably use the engine field already specified
  //NormalisationProcess normEngine;
  public NormalisationRunner(ToolHost host, boolean databaseMode)
  {
    super(host);
    this.databaseMode = databaseMode;
  }
  
    public void runNormalisation(NormalisationType type, ArrayList<File> inputFileList, File genomeFile, File outDir, FilterResultsPanel filterResultsPanel, NormalisationParams params, StatusTracker tracker)
    {
        try
        {
            // Check that there is at least two input files and one genome file
            if (!databaseMode && (inputFileList == null || inputFileList.isEmpty() || inputFileList.size() < 1))
            {
                throw new IOException("More than two input files must be selected for normalisation.");
            }

            if (!databaseMode && genomeFile == null)
            {
                throw new IOException("Must specify a genome file");
            }

            if (outDir == null)
            {
                throw new IOException("Must specify an output directory");
            }

            if (params == null)
            {
                throw new IOException("Parameters have not been specified");
            }
            
            if (databaseMode)
            {
                // TODO: allow NormalisationProcess to use NormalisationServiceLayer
               // this.engine = new NormalisationWorkflowServiceModule(type, Arrays.asList("intergenic"), new NormalisationParams(), "DEFAULT_NORM");
                //this.run(this.engine);
                LOGGER.log(Level.SEVERE, "Database mode for using normal ToolRunner and RunnableTools is not currently supported. No normalisation has occured");       
            }
            else
            {
                // Reformat file list into the nested sample structure that SparseExpressionMatrix wants.
                ArrayList<ArrayList<File>> nested_inputFileList = new ArrayList<>();
                nested_inputFileList.add(new ArrayList<>(inputFileList));

                // pre-build the matrix
                // FIXME: Set up normalisation to understand replicates and treatment types like in ColiDE when required
                SparseExpressionMatrix datamatrix = new SparseExpressionMatrix(tracker, filterResultsPanel, nested_inputFileList,
                        genomeFile, params.getMinLength(), params.getMaxLength());
                // Construct and run the NormalisationProcess
                //this.engine = new NormalisationProcess(type, nested_inputFileList, 
                //genomeFile, filterResultsPanel, params, tracker, outDir);
                this.engine = new NormalisationProcess(type, datamatrix, params, tracker, outDir);
                this.run(this.engine);
                // Write the results to multiple fasta files
                //writeToFasta(outDir);
            }

        }
        catch (IOException ice)
        {
            this.host.showErrorDialog(ice.getMessage());
        }
    }
  
  public void runNormalisation(NormalisationType type, SparseExpressionMatrix expressionMatrix, File outDir, NormalisationParams params, StatusTracker tracker)
  {
    // FIXME: carry out input checks
      this.engine = new NormalisationProcess(type, expressionMatrix, params, tracker, outDir);
      try
      {
        this.run( this.engine );
      }
      catch ( IOException ex )
      {
        this.host.showErrorDialog( ex.getMessage() );
      }
  }
  
  /**
   * Instruct NormalisationProcess to write the current normalised data to fasta files
   * @param outDir
   * @throws IOException because we can't cleanly tell the tracker that there was a problem at this level
   */
  public void writeToFasta(File outDir) throws IOException
  {    
    if (this.engine instanceof NormalisationProcess)
    {
      NormalisationProcess thisEngine = NormalisationProcess.class.cast( this.engine );
      thisEngine.writeToFasta( outDir );
    }
    
  }
  
  public ArrayList<String> getOutputFileNames()
  {
    if(this.engine instanceof NormalisationProcess)
    {
      NormalisationProcess ne = NormalisationProcess.class.cast( this.engine );
      return( ne.getOutputFilenames() );
    }
    return null;
  }
  
  public boolean isComplete()
  {
    if(this.engine instanceof NormalisationProcess)
    {
      NormalisationProcess ne = NormalisationProcess.class.cast( this.engine );
      return( ne.isCompleted() );
    }
    return( false );
  }
  
  public void setResultShown(boolean shown){this.resultShown = shown;}
  public boolean isResultShown(){return this.resultShown;}
}
