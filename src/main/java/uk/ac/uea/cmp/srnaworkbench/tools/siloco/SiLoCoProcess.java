/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.siloco;

import java.awt.Container;
import java.awt.EventQueue;
import java.io.*;
import java.math.RoundingMode;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyledDocument;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.NumberUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;

/**
 *
 * @author w0445959
 */
/**
 * Does some thing in old style.
 *
 * @deprecated Only to be used in CLI mode for now.
 * use {@link SiLoCoExpressionMatrixProcess()} instead.  
 */
@Deprecated
public class SiLoCoProcess extends RunnableTool
{
  private final static String TOOL_NAME = "SILOCO";
  private List<File> mySampleData;
  private File myGenomeFile;
  //private HashMap<String, Integer> compressedData;
  private HashMap<String, HashMap<String, Integer>> compressedData;
  private File combinedFile;

  private HashMap<String, HashMap<String, Integer>> all_data;
  private HashMap<String, ArrayList<Patman>> cluster_per_chrom = new HashMap<String, ArrayList<Patman>>();
  final ArrayList<String> fileNames = new ArrayList<String>();
  //protected Bina

  private SiLoCoParams params;
  private SiLoCoResultsPanel guiOutputTable;
  private HashMap<String, Integer> totalGenomeHitsPerFile = new HashMap<String, Integer>();
  private FastaMap patmanEntries;
  //declare an array variable to hold our Colors
  private Container myParent;
  private Map<String, Patman> all_coords;
  private File outputCSVFile;
  private PrintWriter outputCSVWriter;
  private int amountOfColumns = 0;
  private String CLI_filePath;

  public SiLoCoProcess( Container parent, StatusTracker tracker )
  {
    super( TOOL_NAME, tracker );

    all_data = new HashMap<String, HashMap<String, Integer>>();
    compressedData = new HashMap<String, HashMap<String, Integer>>();

    myParent = parent;

  }

  public SiLoCoProcess( SiLoCoParams newParams, File genomeFile, List<File> sampleFiles )
  {
    super( TOOL_NAME );

    all_data = new HashMap<String, HashMap<String, Integer>>();
    compressedData = new HashMap<String, HashMap<String, Integer>>();

    inputParams( newParams );
    inputGenomeFile( genomeFile );
    inputSampleData( sampleFiles );
    
    Tools.trackPage( "SiLoCo Deprecated Procedure Class Loaded");
  }

  public final void inputParams( SiLoCoParams newParams )
  {
    params = newParams;
  }

  public final void inputGenomeFile( File genomeFile )
  {
    myGenomeFile = genomeFile;

  }

  public final void inputSampleData( List<File> sampleFiles )
  {
    mySampleData = sampleFiles;

  }

  public void removeAllFileRedundancies() throws IOException
  {



    //for each data set contained in the array
    for ( Entry<String, HashMap<String, Integer>> data : all_data.entrySet() )
    {
      /*
       * iterate through the data checking to see if we have found it if we
       * found it then increment value if not create new entry
       */
      this.trackerInitKnownRuntime( "Merging data..." , data.getValue().size());

      for ( Entry<String, Integer> entry : data.getValue().entrySet() )
      {
        this.trackerIncrement();
        if ( compressedData.containsKey( entry.getKey() ) )
        {
          HashMap<String, Integer> valueMap = compressedData.get( entry.getKey() );
          if ( !valueMap.containsKey( data.getKey() ) )
          {
            valueMap.put( data.getKey(), entry.getValue() );
          }
          compressedData.put( entry.getKey(), valueMap );
        }
        else
        {
          HashMap<String, Integer> newMap = new HashMap<String, Integer>();
          newMap.put( data.getKey(), entry.getValue() );
          compressedData.put( entry.getKey(), newMap );
        }
      }
    }
    combinedFile = new File( Tools.siLoCo_dataPath + DIR_SEPARATOR + "combined_data.fa" );
    if ( combinedFile.createNewFile() )
    {
      LOGGER.log( Level.FINE, "created new combined file" );
    }



    FileWriter outFile = new FileWriter( combinedFile );

    PrintWriter out = new PrintWriter( outFile );

    for ( Entry<String, HashMap<String, Integer>> data : compressedData.entrySet() )
    {
      int totalAbund = 0;
      for ( Entry<String, Integer> entry : data.getValue().entrySet() )
      {
        totalAbund += entry.getValue();
      }
      out.println( ">" + data.getKey() + "(" + totalAbund + ")" );
      out.println( data.getKey() );

    }
    outFile.close();
    out.close();


   this.trackerReset();

  }

  public Map<String, Patman> processPatmanFile( File toUse ) throws IOException
  {
    this.trackerInitUnknownRuntime( "Status: Building initial map ..." );


    Patman p = new PatmanReader( toUse ).process( params.getMinLength(), params.getMaxLength(), Integer.MIN_VALUE );
    patmanEntries = p.buildFastaMap();
    this.trackerInitUnknownRuntime("Status: Building Chromosome Map..." );
   

    Map<String, Patman> pList = p.buildChromosomeMap();

    for ( Entry<String, HashMap<String, Integer>> data : all_data.entrySet() )
    {

      int currentTotal = 0;
      for ( Entry<String, Integer> entry : data.getValue().entrySet() )
      {

        currentTotal += entry.getValue();
      }
      totalGenomeHitsPerFile.put( data.getKey(), currentTotal );
    }
    this.trackerReset();
    return pList;
  }

  public void clearAllData()
  {

    System.gc();
  }

  private void initialProcessing( File alignedFile ) throws IOException
  {
    all_coords = processPatmanFile( alignedFile );
    processHitsChromByChrom();
    checkForClusters();

  }

  /**
   * Goes through hits chromosome by chromosome
   */
  public void processHitsChromByChrom()
  {

    this.trackerInitKnownRuntime( "Sorting hits...", all_coords.size() );

    for ( Entry<String, Patman> entry : all_coords.entrySet() )
    {
      this.trackerIncrement();
      entry.getValue().sortByStartCoord();
    }
    this.trackerReset();
  }

  /**
   *
   */
  public void checkForClusters() throws IOException
  {
    int currentClusterPointer;
    String currentChromosome = "";
    
    //System.out.println("all coords consists of : " + all_coords.size() + " chromosomes");
    for ( final Entry<String, Patman> temp : all_coords.entrySet() )
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
    if ( guiOutputTable == null )
    {
      //commented section for output of loci coords:
      File step3 = new File( CLI_filePath + DIR_SEPARATOR + "loci_coords.txt" );


      if ( step3.createNewFile() )
      {
        LOGGER.log( Level.FINE, "created new step 3 file" );
      }
      FileWriter outFile = new FileWriter( step3 );

      PrintWriter out = new PrintWriter( outFile );

      for ( Entry<String, ArrayList<Patman>> clusters : new TreeMap<String, ArrayList<Patman>>( cluster_per_chrom ).entrySet() )
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

  private void checkSingleFileRedundancy() throws FileNotFoundException, IOException
  {

    this.trackerInitUnknownRuntime( "Checking redundancy..." );
    
    for ( File currentFile : mySampleData )
    {


      SRNAFastaReader newReader = new SRNAFastaReader( currentFile );
      fileNames.add( currentFile.getName() );
      all_data.put( currentFile.getName(), newReader.process() );


    }

    if ( guiOutputTable != null )
    {

      SwingUtilities.invokeLater( new Runnable()
      {
        @Override
        public void run()
        {
          //Update the model here
          guiOutputTable.setVisible( true );
          //DefaultTableModel model = (DefaultTableModel) guiOutputTable.getModel();
           

        }
      });
    }
    else
    {



      LOGGER.log( Level.INFO, "Attempting creation: {0}{1}output.csv", new Object[]
        {
          CLI_filePath, DIR_SEPARATOR
        } );

      outputCSVFile = new File( CLI_filePath + DIR_SEPARATOR + "output.csv" );

      FileWriter outFile = new FileWriter( outputCSVFile, true );


      outputCSVWriter = new PrintWriter( outFile );
      String topLine = "";
      topLine += "Chromosome,";
      amountOfColumns++;
      topLine += "Start,";
      amountOfColumns++;
      topLine += "Stop,";
      amountOfColumns++;

      for ( String fileName : fileNames )
      {
        topLine += fileName + " Abundance,";
        amountOfColumns++;

        topLine += fileName + " Unique sRNAs,";
        amountOfColumns++;

        topLine += fileName + " Average Size Class,";
        amountOfColumns++;

        topLine += fileName + " Strand Bias,";
        amountOfColumns++;

      }
      topLine += "Mean Count,";
      amountOfColumns++;
      topLine += "Max Difference,";
      amountOfColumns++;
      topLine += "Locus Length,";
      amountOfColumns++;
      outputCSVWriter.println( topLine );
    }
    this.trackerReset();

  }

  public void generateVisSR()
  {
    this.trackerInitUnknownRuntime( "Generating VisSR..."  );

    HashMap<String, List<PatmanEntry>> locusItems = new HashMap<String, List<PatmanEntry>>();
    final byte PHASE = 0;
    HashMap<String, TierParameters> tp_list = new HashMap<String, TierParameters>();
    for ( Entry<String, HashMap<String, Integer>> fileSearch : all_data.entrySet() )
    {
      TierParameters tpLoci = new TierParameters.Builder( "File: " + fileSearch.getKey() ).build();
      tp_list.put( fileSearch.getKey(), tpLoci );
      //locusItems.put( fileSearch.getKey(), new ArrayList<GFFRecord>() );
      locusItems.put( fileSearch.getKey(), new ArrayList<PatmanEntry>() );
    }
    for ( Entry<String, ArrayList<Patman>> clusters : new TreeMap<String, ArrayList<Patman>>( cluster_per_chrom ).entrySet() )
    {
      ArrayList<Patman> lociList = clusters.getValue();
      for ( Patman locus : lociList )
      {
        for ( PatmanEntry entry : locus )
        {
          for ( String fileName : fileNames )
          {
            if ( all_data.get( fileName ).containsKey( entry.getSequence() ) )
            {
              locusItems.get( fileName ).add( entry );
            }
          }
        }
      }
    }
    for ( Entry<String, TierParameters> tps : tp_list.entrySet() )
    {

      tps.getValue().addList( locusItems.get( tps.getKey() ) );
    }

    TierParameters[] dummy =
    {
    };
    TierParameters[] tiers = tp_list.values().toArray( dummy );
    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( this.myGenomeFile, false, tiers );
    vissr.displaySequenceRegion( "2", 0, 10000 );
    this.trackerReset();
  }

  private File alignDataToGenome()
  {


    this.trackerInitUnknownRuntime(  "Aligning merged file to genome..." );

    
    String finalTempFilePath = Tools.siLoCo_dataPath + DIR_SEPARATOR + myGenomeFile.getName() + ".patman";

    File patman_out = new File( finalTempFilePath );

    //File tempDIR = new File(Tools.miRCat_dataPath  );

    Thread myThread = new Thread( new PatmanRunner( combinedFile, myGenomeFile, patman_out, Tools.getNextDirectory(), new PatmanParams() ) );

    myThread.start();
    try
    {
      myThread.join();
    }
    catch ( InterruptedException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }

    this.trackerReset();
    return patman_out;
  }

  public void inputRefs( SiLoCoResultsPanel myOutput, JProgressBar myProg, JLabel myStatus, StyledDocument log )
  {
    this.guiOutputTable = myOutput;
  }

  private void analyze() throws IOException
  {
    this.trackerInitKnownRuntime( "Performing loci analysis...", cluster_per_chrom.size()  );
    
    //final row data indexed by chromosome, used for building tree table
    HashMap<String, ArrayList<ArrayList<String>>> data = new HashMap<String, ArrayList<ArrayList<String>>>();
    for ( Entry<String, ArrayList<Patman>> clusters : new TreeMap<String, ArrayList<Patman>>( cluster_per_chrom ).entrySet() )
    {
      this.trackerIncrement();
      ArrayList<Patman> lociList = clusters.getValue();
      ArrayList<ArrayList<String>> chromosomeResults = new ArrayList<ArrayList<String>>();
      data.put( clusters.getKey(), chromosomeResults);


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
          Map<String, Integer> uncompressedData = compressedData.get( line.getSequence() );

          for ( String fileName : fileNames )
          {
            if ( totalLocusAbundancePerSample.containsKey( fileName ) )
            {
              Double[] value = totalLocusAbundancePerSample.get( fileName );
              double toAdd = uncompressedData.get( fileName ) == null ? 0 : uncompressedData.get( fileName );
              value[0] += ( toAdd / countGenomeHits( line.getSequence() ) );
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

              totalLocusAbundancePerSample.put( fileName, value );
            }
            else
            {
              double value = uncompressedData.get( fileName ) == null ? 0 : uncompressedData.get( fileName );

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
              value /= countGenomeHits( line.getSequence() );

              totalLocusAbundancePerSample.put( fileName, new Double[]
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
        for ( String fileName : fileNames )
        {
          Double[] abundData = totalLocusAbundancePerSample.get( fileName );
          double scalar = totalGenomeHitsPerFile.get( fileName );
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
          newRow.add( NumberUtils.format2DP( abundData[1], RoundingMode.HALF_EVEN ));
          i++;
          if(abundData[1] > 0.0 && abundData[2] > 0.0 )
            newRow.add(  NumberUtils.format2DP( ( abundData[2] / abundData[1] ), RoundingMode.HALF_EVEN ) );
          else
            newRow.add( "0.0" );
          i++;
          if(( abundData[3] - abundData[4] ) > 0.0 &&  ( abundData[3] + abundData[4] ) > 0.0)
            newRow.add( NumberUtils.format2DP( ( ( abundData[3] - abundData[4] ) / ( abundData[3] + abundData[4] ) ), RoundingMode.HALF_EVEN ));
          else 
            newRow.add( "0.0" );
          i++;
        }
        newRow.add( NumberUtils.format2DP( ( total / ( (double) fileNames.size() ) ), RoundingMode.HALF_EVEN ) );
        i++;
        newRow.add( NumberUtils.format2DP( ( highestVal - lowestVal ), RoundingMode.HALF_EVEN ) );
        i++;
        newRow.add("" + (maxCoord - minCoord));
        if ( guiOutputTable != null )
        {
          chromosomeResults.add( newRow );
        }
        else
        {

          for ( Object obj : newRow )
          {
            this.outputCSVWriter.print(obj.toString() + ",");
          }
          this.outputCSVWriter.println(  );
        }
      }
    }

    //this.guiOutputTable.display( fileNames, data, myGenomeFile );

    this.trackerReset();

  }

  double countGenomeHits( String toCheck ) throws IOException
  {

    if ( patmanEntries.containsKey( toCheck ) )
    {
      return patmanEntries.get( toCheck ).getHitCount();
    }
    return 0;
  }

  @Override
  protected void process() throws Exception
  {
    if ( guiOutputTable == null )
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
    checkSingleFileRedundancy();
    continueRun();
    removeAllFileRedundancies();
    continueRun();
    
    File alignedFile = alignDataToGenome();
    sw.suspend();
    System.out.println( "pre processing time: " + sw.toString() );
    sw.resume();
    initialProcessing( alignedFile );
    continueRun();
    analyze();
//    if ( guiOutputTable != null )
//    {
//      guiOutputTable.addMouseListener( new SiLoCoPopUpMenuListener( myParent, myGenomeFile, all_coords, all_data ) );
//    }
//    else
    if ( guiOutputTable == null )
    {
      this.outputCSVWriter.close();
    }
    LOGGER.log( Level.INFO, "threads: {0}", Tools.getThreadCount() );
    sw.stop();
    System.out.println( "Time taken to complete siloco: " + sw.toString() );
  }
}
