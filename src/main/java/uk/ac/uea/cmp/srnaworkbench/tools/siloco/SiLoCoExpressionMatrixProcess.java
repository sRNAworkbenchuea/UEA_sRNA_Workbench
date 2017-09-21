/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.siloco;

import java.awt.Container;
import java.io.*;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.text.StyledDocument;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.NumberUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.FileDialogUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;
/**
 *
 * @author w0445959
 */
public class SiLoCoExpressionMatrixProcess extends RunnableTool
{
  //Keep:
  SparseExpressionMatrix expressionMatrix;
  private final static String TOOL_NAME = "SILOCO";
  private ArrayList<ArrayList<File>> mySampleData;
  private File myGenomeFile;
  private FilterResultsPanel filterResults;  
  private SiLoCoParams params;
  private SiLoCoResultsPanel siLoCoResultsTable;
  private HashMap<String, ArrayList<Patman>> cluster_per_chrom = new HashMap<String, ArrayList<Patman>>();
  private Container myParent;
  private File outputCSVFile;
  private PrintWriter outputCSVWriter;
  private String CLI_filePath;
  private StatusTracker myTracker = null;

  
  
  

  public SiLoCoExpressionMatrixProcess( Container parent, StatusTracker tracker )
  {
    super( TOOL_NAME, tracker );

    this.myTracker = tracker;
    myParent = parent;
    
    Tools.trackPage( "SiLoCo Main Procedure Class Loaded");

  }

  public final void inputParams( SiLoCoParams newParams )
  {
    params = newParams;
  }

  public final void inputFilterResultsPanel(FilterResultsPanel panel)
  {
    this.filterResults = panel;
  }
  public final void inputGenomeFile( File genomeFile )
  {
    myGenomeFile = genomeFile;

  }

  public final void inputSampleData( ArrayList<ArrayList<File>> samples )
  {
    
    mySampleData = samples;

  }

  public void clearAllData()
  {

    System.gc();
  }

  private void initialProcessing(  ) throws IOException
  {

    expressionMatrix = 
     new SparseExpressionMatrix(getTracker(), filterResults,
     mySampleData, myGenomeFile);
    try
    {
      // Stop the stop watch.
         
      expressionMatrix.preProcess(params.getMinLength(), params.getMaxLength());
      expressionMatrix.build();
      normalise();
    }
    catch ( Exception ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage() );
    }
    checkForClusters();

  }

  public void checkForClusters() throws IOException
  {
    int currentClusterPointer;
    String currentChromosome = "";
    
    //System.out.println("all coords consists of : " + all_coords.size() + " chromosomes");
    for ( final Map.Entry<String, Patman> temp : expressionMatrix.getPatman().entrySet() )
    {
      final int max = temp.getValue().size();
      this.trackerInitKnownRuntime( "Generating sRNA loci "  + temp.getKey(), max );
      

      ArrayList<Patman> clusters = new ArrayList<Patman>();

      currentClusterPointer = -1;
      boolean firstCluster = true;

      PatmanEntry previousRNA = null;
      for ( PatmanEntry nextRNA : temp.getValue() )
      {
        this.trackerIncrement();
        currentChromosome = temp.getKey();//nextRNA.getLongReadHeader();

        if ( firstCluster )
        {
          firstCluster = false;

          Patman newCluster = new Patman();
          newCluster.add( nextRNA );
          clusters.add( newCluster );
          currentClusterPointer++;

        }
        else
        {
          int distance = ( nextRNA.getStart() - previousRNA.getEnd() );

          //System.out.println( "distance: " + distance );
          //System.out.println( "sentinal: " + params.getClusterSentinel() );
          if ( distance <= params.getClusterSentinel() )
          {
            //System.out.println("adding to current cluster");
            ArrayList<PatmanEntry> currentCluster = clusters.get( currentClusterPointer );
            currentCluster.add( nextRNA );
          }
          else
          {
            //System.out.println("created a new cluster");
            Patman newCluster = new Patman();
            newCluster.add( nextRNA );
            clusters.add( newCluster );
            currentClusterPointer++;

          }
        }
        previousRNA = nextRNA;
      }
      if ( clusters.size() > 0 )
      {
        cluster_per_chrom.put( currentChromosome.trim(), clusters );
      }
    }


    this.trackerReset();
    if ( siLoCoResultsTable == null )
    {
      //commented section for output of loci coords:
      File step3 = new File( CLI_filePath + DIR_SEPARATOR + "loci_coords.txt" );


      if ( step3.createNewFile() )
      {
        LOGGER.log( Level.FINE, "created new step 3 file" );
      }
      FileWriter outFile = new FileWriter( step3 );

      PrintWriter out = new PrintWriter( outFile );

      for ( Map.Entry<String, ArrayList<Patman>> clusters : new TreeMap<String, ArrayList<Patman>>( cluster_per_chrom ).entrySet() )
      {
        ArrayList<Patman> lociList = clusters.getValue();

        for ( Patman loci : lociList )
        {
          int minCoord = Integer.MAX_VALUE;
          int maxCoord = Integer.MIN_VALUE;

          for ( PatmanEntry line : loci )
          {
            if ( line.getStart() < minCoord )
            {
              minCoord = line.getStart();
            }
            if ( line.getEnd() > maxCoord )
            {
              maxCoord = line.getEnd();
            }


          }
          out.println( loci.get( 0 ).getSequenceId() + "\\" + minCoord + "-" + maxCoord );
        }
      }
      outFile.close();
      out.close();
    }




  }
  public void outputCSVFileFromTable( File toWrite )
  {
    exportMainTable( toWrite );
  }

  
   private void exportMainTable( File output )
  {
    FileWriter outCSVFile = null;
    PrintWriter out = null;
    try
    {
      outCSVFile = new FileWriter( output );
      out = new PrintWriter( outCSVFile );
      // Write text to file

      String header = "Chromosome,Start,Stop,";
      for ( ArrayList<File> fileName : mySampleData )
      {


        header += fileName.get( 0 ).getName() + " Abundance,";


        header += fileName.get( 0 ).getName() + " Unique sRNAs,";
        header += fileName.get( 0 ).getName() + " Average Size Class,";
        header += fileName.get( 0 ).getName() + " Strand Bias,";


      }
      header += "Mean Count,Max Difference,Locus Length";

      out.println( header );
      DefaultOutlineModel model = (DefaultOutlineModel) siLoCoResultsTable.getOutline().getModel();
      OutlineNode treeModel = (OutlineNode) model.getRoot();
      int chromosomeCount = treeModel.getChildCount();


      for ( int i = 0; i < chromosomeCount; i++ )
      {
        OutlineNode chromosomeModel = (OutlineNode) treeModel.getChildAt( i );
        //out.println("Chromso")
        int locusCount = chromosomeModel.getChildCount();
        for ( int locusID = 0; locusID < locusCount; locusID++ )
        {
          OutlineNode locusData = (OutlineNode) chromosomeModel.getChildAt( locusID );
          List<String> userObject = locusData.getUserObject();
          Iterator<String> iterator = userObject.iterator();
          iterator.next();
          while ( iterator.hasNext() )
          {
            out.print( iterator.next() + "," );
          }
          //double
          out.println();
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }
    finally
    {
      try
      {
        outCSVFile.close();
        out.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
  }

  private void normalise()
  {

    // This will now write normalised files to the temp directory even though
    // the expression matrix is directly used from RAM
    // TODO: Make NormalisationProcess flexible enough to be used as part of another
    // passing an expression matrix through it. See also ColideProcess
    NormalisationProcess newNormaliser = new NormalisationProcess( NormalisationType.TOTAL_COUNT, expressionMatrix, myTracker, FileUtils.getTempDirectory() );
    newNormaliser.run();


  }

  public void inputRefs( SiLoCoResultsPanel myOutput, JProgressBar myProg, JLabel myStatus, StyledDocument log )
  {
    this.siLoCoResultsTable = myOutput;
  }

  private void analyze() throws IOException
  {
    this.trackerInitKnownRuntime( "Performing loci analysis...", cluster_per_chrom.size() );

    //final row data indexed by chromosome, used for building tree table
    HashMap<String, ArrayList<ArrayList<String>>> data = new HashMap<String, ArrayList<ArrayList<String>>>();
    //data used for building vissr instances
    //HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromo_to_locus = new HashMap<String, ArrayList<ArrayList<ExpressionElement>>>();

    for ( Map.Entry<String, ArrayList<Patman>> clusters : new TreeMap<String, ArrayList<Patman>>( cluster_per_chrom ).entrySet() )
    {
      this.trackerIncrement();
      ArrayList<Patman> lociList = clusters.getValue();

      ArrayList<ArrayList<String>> chromosomeResults = new ArrayList<ArrayList<String>>();
      data.put( clusters.getKey(), chromosomeResults );

//      ArrayList<ArrayList<ExpressionElement>> lociForChromo = new ArrayList<ArrayList<ExpressionElement>>();
//      chromo_to_locus.put( clusters.getKey(), lociForChromo );


      for ( Patman locus : lociList )
      {
        int minCoord = Integer.MAX_VALUE;
        int maxCoord = Integer.MIN_VALUE;

        HashMap<String, Double[]> totalLocusAbundancePerSample = new HashMap<String, Double[]>();

        for ( PatmanEntry line : locus )
        {
          if ( line.getStart() < minCoord )
          {
            minCoord = line.getStart();
          }
          if ( line.getEnd() > maxCoord )
          {
            maxCoord = line.getEnd();
          }
          Map<String, Integer> uncompressedData = expressionMatrix.getCompressedValue( line.getSequence() );
 

          for ( ArrayList<String> fileName : expressionMatrix.getFileNames() )
          {
            if ( totalLocusAbundancePerSample.containsKey( fileName.get( 0 ) ) )
            {
              Double[] value = totalLocusAbundancePerSample.get( fileName.get( 0 ) );
              double toAdd = uncompressedData.get( fileName.get( 0 ) ) == null ? 0 : uncompressedData.get( fileName.get( 0 ) );
              value[0] += ( toAdd / expressionMatrix.countGenomeHits( line.getSequence() ) );
              if ( toAdd > 0 )
              {
                value[1]++;
                value[2] += line.getSequenceLength();
                if ( line.getSequenceStrand() == SequenceStrand.POSITIVE )
                {
                  value[3] += toAdd;
                }
                else
                {
                  value[4] += toAdd;
                }
              }

              totalLocusAbundancePerSample.put( fileName.get( 0 ), value );
            }
            else
            {
              double value = uncompressedData.get( fileName.get( 0 ) ) == null ? 0 : uncompressedData.get( fileName.get( 0 ) );

              double size = 0;

              double unique = 0;

              double negbias = 0.0;
              double posbias = 0.0;
              if ( value > 0 )
              {
                unique++;
                size += line.getSequenceLength();
                if ( line.getSequenceStrand() == SequenceStrand.POSITIVE )
                {
                  posbias += value;
                }
                else
                {
                  negbias += value;
                }
              }
              //System.out.println("UNCOMP: FILE NAME: " + fileName + " To Add: " + value);
              value /= expressionMatrix.countGenomeHits( line.getSequence() );

              totalLocusAbundancePerSample.put( fileName.get( 0 ), new Double[]
                {
                  value, unique, size, posbias, negbias
                } );
            }

          }
        }
        ArrayList<String> newRow = new ArrayList<String>();
        newRow.add( locus.get( 0 ).getSequenceId() );
        newRow.add( String.valueOf( minCoord ) );
        newRow.add( String.valueOf( maxCoord ) );
        int i = 3;
        double total = 0.0;
        double highestVal = Double.MIN_VALUE;
        double lowestVal = Double.MAX_VALUE;
        for ( ArrayList<String> fileName : expressionMatrix.getFileNames() )
        {
          Double[] abundData = totalLocusAbundancePerSample.get( fileName.get( 0 ) );
          double scalar = expressionMatrix.getTotalGenomeHit( fileName.get( 0 ) );
          double val = ( ( abundData[0] / scalar ) * 1000000.0 );

          newRow.add( NumberUtils.format2DP( val, RoundingMode.HALF_EVEN ) );
          total += val;
          if ( val > highestVal )
          {
            highestVal = val;
          }
          if ( val < lowestVal )
          {
            lowestVal = val;
          }
          i++;
          newRow.add( NumberUtils.format2DP( abundData[1], RoundingMode.HALF_EVEN ) );
          i++;
          if ( abundData[1] > 0.0 && abundData[2] > 0.0 )
          {
            newRow.add( NumberUtils.format2DP( ( abundData[2] / abundData[1] ), RoundingMode.HALF_EVEN ) );
          }
          else
          {
            newRow.add( "0.0" );
          }
          i++;
          if ( ( abundData[3] - abundData[4] ) > 0.0 && ( abundData[3] + abundData[4] ) > 0.0 )
          {
            newRow.add( NumberUtils.format2DP( ( ( abundData[3] - abundData[4] ) / ( abundData[3] + abundData[4] ) ), RoundingMode.HALF_EVEN ) );
          }
          else
          {
            newRow.add( "0.0" );
          }
          i++;
        }
        newRow.add( NumberUtils.format2DP( ( total / ( (double) expressionMatrix.getFileNames().size() ) ), RoundingMode.HALF_EVEN ) );
        i++;
        newRow.add( NumberUtils.format2DP( ( highestVal - lowestVal ), RoundingMode.HALF_EVEN ) );
        i++;
        newRow.add( "" + ( maxCoord - minCoord ) );
        if ( siLoCoResultsTable != null )
        {
          chromosomeResults.add( newRow );
        }
        else
        {

          for ( Object obj : newRow )
          {
            this.outputCSVWriter.print( obj.toString() + "," );
          }
          this.outputCSVWriter.println();
        }
      }
    }

    this.siLoCoResultsTable.display( data, this.expressionMatrix, cluster_per_chrom );

    this.trackerReset();

  }

  @Override
  protected void process() throws Exception
  {
    if ( siLoCoResultsTable == null )
    {
      LOGGER.log( Level.INFO, "CLI: Attempting creation: '{'0'}''{'1'}'output.csv{0}{1}{2}", new Object[]
        {
          Tools.siLoCo_dataPath, DIR_SEPARATOR, Tools.getSimpleDateTime().toString()
        } );


      CLI_filePath = Tools.siLoCo_dataPath + DIR_SEPARATOR + Tools.getSimpleDateTime().toString();

      boolean success = ( new File( CLI_filePath ) ).mkdir();

      if ( success )
      {
        LOGGER.log( Level.INFO, "Directory created" );
      }
      else
      {
        LOGGER.log( Level.INFO, "Directory creation failed!" );
      }
    }

     StopWatch sw = new StopWatch();
    sw.start();
    continueRun();
    initialProcessing();
    sw.suspend();
    LOGGER.log( Level.FINE, "pre processing time: {0}", sw.toString());
    sw.resume();
    
    continueRun();
    analyze();

    if ( siLoCoResultsTable == null )
    {
      this.outputCSVWriter.close();
    }
    LOGGER.log( Level.INFO, "threads: {0}", Tools.getThreadCount() );
    sw.stop();
    System.out.println( "Time taken to complete siloco: " + sw.toString() );
  }
}
