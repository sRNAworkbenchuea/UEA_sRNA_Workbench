
package uk.ac.uea.cmp.srnaworkbench.tools.mircat;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBase;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBaseHeader;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.*;
import uk.ac.uea.cmp.srnaworkbench.exceptions.HairpinExtensionException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.io.FastaFileReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.LocusUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.TierParameters;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.MiRCatLogger.MIRCAT_LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;

/**
 *
 * @author w0445959
 */
public final class Process_Hits_Patman extends RunnableTool
{
  private final static String TOOL_NAME = "MIRCAT";
  // Used for multi-threading using BinaryExecutor (each instance requires a new port)
  private static final int PORT_START = 7789;
  // miRCat Input
  private File genomeFile; //Genome file path/name
  private File inputFile; //Input file path/name (patman output)
  private File tempDir;
  private MiRCatParams params;
  // Class State
  private FastaMap patmanEntries; // sRNA hit data
  private FastaMap genomeData;    // genome data
  private HashMap<String, ArrayList<MirBaseHeader>> myMirBaseData;  // mirBase data
  // GUI dependecies (ideally we shouldn't refer directly to these from this class
  // but for simplicity they stay).
  private JTable guiOutputTable = null;
  protected volatile JProgressBar myProgBar = null;
  protected volatile JLabel myStatusLabel = null;
  protected volatile StyledDocument myTextLog = null;
  private JProgressBar myExecQBar = null;
  private JProgressBar myCompTaskBar = null;
  // Logging vars

  private ArrayList<BinaryExecutor> exe_list;
  private ThreadPoolExecutor es;
  private List<Thread> shutdownExeManThreads;
  private int totalThread;
  
  private boolean addMiRCatToLog = false;

  private ChromosomeClusterMap cluster_per_chrom;
  
  // Field holds the default output directory where results will be placed form a command line run
  // Method setOutputDirectoryFilename() can set this to something different
  private String outputDirectoryFilename = Tools.ROOT_DIR + DIR_SEPARATOR + "output";
  public void setOutputDirectoryFilename(String newDirectoryName){ this.outputDirectoryFilename = newDirectoryName; }
  
  public Process_Hits_Patman( StatusTracker tracker )
  {
    this( null, null, null, null, tracker );
  }

  public Process_Hits_Patman( MiRCatParams params, File patmanFile, File genomeFile, File tempDir, StatusTracker tracker )
  {
    super( TOOL_NAME, tracker );

    // Set input vars
    this.inputFile = patmanFile;
    this.genomeFile = genomeFile;
    this.tempDir = tempDir;
    this.params = params;

    // Intialise class state
    this.patmanEntries = null;
    this.genomeData = null;

    try
    {
      this.myMirBaseData = new MirBase().getMatureMap( true );
    }
    catch ( Exception ex )
    {
      myMirBaseData = new HashMap<String, ArrayList<MirBaseHeader>>();
      LOGGER.log( Level.FINE, "Couldnt find mirbase files...", ex );
    }
    

    Tools.trackPage( "miRCat Main Procedure Class Loaded");
    

  }
  
  public void changeLoggingMiRCat(boolean newState)
  {
    addMiRCatToLog = newState;
  }
  public void setupThreads(int numToGenerate) 
  {
    //needs work here to detect hanging threads and shut them down
    //also we will add a heart beat to the exemanager that will shut down if no workbench is detected
    this.trackerInitUnknownRuntime( "Creating thread pool" );
    totalThread = Tools.getThreadCount();
    if(numToGenerate < totalThread)
    {
      totalThread = numToGenerate;
    }
    es = (ThreadPoolExecutor) Executors.newFixedThreadPool( totalThread );
    exe_list = new ArrayList<BinaryExecutor>();
    
    for (int threadScan = 0; threadScan < totalThread; threadScan++ )
    {

        BinaryExecutor be = new BinaryExecutor();
        exe_list.add( be );
     
      //shutdownExeManThreads.add( Tools.addShutdownHook( be) );
    }

    
  }

  /**
   * Helper method for outputting information to the logger when AppUtils verbose 
   * flag is set.
   * @param message Message to output.
   */
  private void printVerbose( String message )
  {
    if ( AppUtils.INSTANCE.verbose() )
    {
      LOGGER.log( Level.INFO, message );
    }
  }

  /**
   * Sets miRCat Params after initialisation.
   * @param params 
   */
  public void setParams( MiRCatParams params )
  {
    this.params = params;
  }

  /**
   * Sets the input file (either fasta or patman) after initialisation.
   * @param inputFile Input file.
   */
  public void setInputFile( File inputFile )
  {
    this.inputFile = inputFile;
    printVerbose( this.inputFile.getPath() );
  }

  /**
   * Sets the genome file after initialisation.
   * @param genomeFile Genome file.
   */
  public void setGenomeFile( File genomeFile )
  {
    this.genomeFile = genomeFile;
    printVerbose( this.genomeFile.getPath() );
  }

  /**
   * Sets the temp directory after initialisation
   * @param tempDir Temporary directory for working files.
   */
  public void setTempDir( File tempDir )
  {
    this.tempDir = tempDir;
    printVerbose( this.tempDir.getPath() );
  }

  public void addGUIRefs( StyledDocument log,
                          JProgressBar execQueueBar, JProgressBar compTasksBar )
  {
    myTextLog = log;
    myExecQBar = execQueueBar;
    myCompTaskBar = compTasksBar;
  }

  public void addTrackingComponents( JProgressBar prog, JLabel status )
  {
    myProgBar = prog;
    myStatusLabel = status;
  }

  public void addTableRef( JTable mirCAT_OutputTable )
  {
    guiOutputTable = mirCAT_OutputTable;
  }

  /**
   * Opens the fasta file and "lazy loads" the sequences contained within it.
   * This means that the sequences headers are stored in the linked hash map
   * (genomeData) but the actual sequences are only loaded from the file when
   * the values in the hashmap are used.
   *
   * @throws FileNotFoundException
   * @throws BioException
   */
  private void openGenomeFile() throws FileNotFoundException, Exception
  {
    printVerbose( "genome: " + this.genomeFile.getPath() );

    FastaFileReader genReader = new FastaFileReader( this.genomeFile );

    boolean success = genReader.processFile();
    if ( !success )
    {
      throw new WorkbenchException( "Failed to load genome file." );
    }

    printVerbose( "genome loaded" );

    this.genomeData = genReader.getFastaMap( true );

    printVerbose( "" );
  }

  public void clearAllData()
  {

    //addMiRCatToLog = this.;

    this.patmanEntries = null;
    this.genomeData = null;

    if ( myTextLog != null )
    {
      try
      {
        myTextLog.remove( 0, myTextLog.getLength() );
      }
      catch ( BadLocationException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }
    }
    

    System.gc();
  }

  /**
   *
   */
  @Override
  public String toString()
  {
    return "mircat process file";
  }

  @Override
  protected void process() throws Exception
  {
    
    printVerbose( "Params:\n" + this.params.toString() );

    clearAllData();

    continueRun();

    // Create a map of hits to each chromsome from the input file (and genome).
    Map<String, Patman> all_coords = processInputFile();

    continueRun();

    // Sorts the hits for each chromosome into order (based on genomic location)
    sortHits( all_coords );

    continueRun();

    // Create the clusters
    cluster_per_chrom = generateLoci( all_coords );

    continueRun();

    // Print chromsome cluster stats if necessary
    if ( AppUtils.INSTANCE.verbose() )
    {
      cluster_per_chrom.printStats();
    }

    // Detects clusters in the sRNA hits.
    TopHits topHits = findTopHits( );

    trackerReset();

    //System.gc();
    continueRun();

    checkForMicroRNAs( topHits );

    
  }

  private File handleFASTAFile() throws IOException, WorkbenchException
  {
    File patman_out = new File( this.tempDir.getPath() + DIR_SEPARATOR + this.genomeFile.getName() + ".patman" );

    // Convert fasta file into patman format
    PatmanRunner pr = new PatmanRunner( inputFile, this.genomeFile, patman_out, this.tempDir, new PatmanParams() );
    pr.run();
    if ( pr.failed() )
    {
      throw new WorkbenchException( pr.getErrorMessage() );
    }

    return patman_out;
  }

  /**
   * Processes the input file, whether it is a fasta file or a patman file.  If the
   * input file is fasta format the file is first aligned to the genome using patman.
   * The patman file (whether just generated or provided by the user) is then parsed
   * and returned as a chromosome map of sRNA hits.  Also retains the original patman
   * data for use later in the process.
   * @return A map, keyed by chromosome, containing sRNA hits to the genome.
   * @throws IOException Thrown if there were any problems loading the file
   * @throws WorkbenchException Thrown if there was some fundamental problem with
   * the process.
   */
  private Map<String, Patman> processInputFile() throws IOException, WorkbenchException
  {
    File toUse = this.inputFile;

    // Convert fasta file to patman hits file if required.
    if ( !this.inputFile.getAbsolutePath().contains( "patman" ) )
    {
      this.trackerInitUnknownRuntime( "Aligning sequences to genome" );
      toUse = handleFASTAFile();
      this.trackerReset();
    }

    // Parse the patman file
    this.trackerInitUnknownRuntime( "Parsing sRNA hits" );
    Patman p = new PatmanReader( toUse ).process( params.getMinLength(), params.getMaxLength(), Integer.MIN_VALUE );
    this.patmanEntries = p.buildFastaMap();
    this.trackerReset();
    
    

    // Remove hits with a weighted abundance, less than that specified by the user
    // as the minimum to consider.
    this.trackerInitUnknownRuntime( "Removing low abundance hits" );
    Patman p_pass2 = removeInsignificantHits( p );
    this.trackerReset();

    // Clean up memory
    p = null;
    System.gc();

    // Create chromosome map from the hit list.
    this.trackerInitUnknownRuntime( "Building Chromosome Map" );
    Map<String, Patman> pList = p_pass2.buildChromosomeMap();
    this.trackerReset();

    return pList;
  }

  /**
   * Removes hits from provided hit list that have a lower weighted abundance than
   * that specified by the user.
   * @param p Hit list
   * @return Trimmed hit list.
   */
  private Patman removeInsignificantHits( Patman p )
  {
    Patman p_pass2 = new Patman();
    for ( PatmanEntry entry : p )
    {
      float abundance = (float) entry.getAbundance();
      FastaRecord fr = patmanEntries.get( entry.getSequence() );
        String sequence = entry.getSequence();
//      if(sequence.equals("ATAGGTATGCCATTAGAAATAA"))
//                System.out.println("");
      if ( fr != null )
      {
        float hitCount = (float) fr.getHitCount();
        float wabundance = abundance / hitCount;
        if ( abundance >= (float) params.getMinConsider() )
        {
          p_pass2.add( entry );
        }
      }
    }

    return p_pass2;
  }

  /**
   * Sorts all the sRNA hits in each chromosome by genomic order.
   * @param all_coords The sRNA hits mapped for each chromosome to be sorted
   */
  private void sortHits( Map<String, Patman> all_coords )
  {
    this.trackerInitKnownRuntime( "Sorting sRNA hits", all_coords.size() );

    for ( Entry<String, Patman> entry : all_coords.entrySet() )
    {
      entry.getValue().sortByStartCoord();
      this.trackerIncrement();
    }

    this.trackerReset();
  }

  private ChromosomeClusterMap generateLoci( Map<String, Patman> all_coords )
  {
    ChromosomeClusterMap cluster_per_chroms = new ChromosomeClusterMap();

    int currentClusterPointer = -1;
    String currentChromosome = "";
    int index = 0;

    // Generate sRNA loci by iterating through each chromosome adding any clusters 
    // to class variable "cluster_per_chrom".
    for ( Entry<String, Patman> temp : new TreeMap<>(all_coords).entrySet() )
    {
      this.trackerInitKnownRuntime( "Generating sRNA loci " + index++, temp.getValue().size() );

      ArrayList<Patman> clusters = new ArrayList<Patman>();

      currentClusterPointer = -1;
      boolean firstCluster = true;

      PatmanEntry previousRNA = null;
      for ( PatmanEntry nextRNA : temp.getValue() )
      {
        this.trackerIncrement();

        currentChromosome = nextRNA.getLongReadHeader();

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
          //PatmanEntry previousRNA = currentCluster.get( currentCluster.size() - 1 );

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
        cluster_per_chroms.put( currentChromosome.trim(), clusters );
      }

      this.trackerReset();
    }
    
    //cluster_per_chroms.printClusters();
    
    return cluster_per_chroms;
  }

  /**
   * Find the best sRNA hits within the clusters of each chromosome.
   * @param all_coords sRNA hits organised by chromosome.
   * @param cluster_per_chrom The clusters detected from the sRNA hits
   * @throws WorkbenchException Thrown if there were any unexpected problems with
   * the sRNA hits.
   */
  private TopHits findTopHits( ) throws WorkbenchException
  {
    TopHits top_hits_per_chromosome = new TopHits();

    this.trackerInitKnownRuntime( "Determining most abundant sequences in loci", cluster_per_chrom.size() );

    printVerbose( "now sorting clusters by abundance..." );

    // sort clusters by abundance 
    int k = 0;

    for ( Map.Entry<String, ArrayList<Patman>> clusters : cluster_per_chrom.entrySet() )
    {
      this.trackerIncrement();

      Patman top_hits = new Patman();
      k++;

      for ( Patman currentCluster : clusters.getValue() )
      {
        currentCluster.sortByAbundance();

        float currentPosClusterBias = 0.0f;
        float currentNegClusterBias = 0.0f;
        float expandedRNA = 0.0f;
        for ( PatmanEntry curData : currentCluster )
        {
          int abundance = (int)curData.getAbundance();

          expandedRNA += abundance;

          if ( curData.getSequenceStrand() == SequenceStrand.POSITIVE )
          {
            currentPosClusterBias += abundance;
          }
          else
          {
            if ( curData.getSequenceStrand() == SequenceStrand.NEGATIVE )
            {
              currentNegClusterBias += abundance;
            }
            else
            {
              throw new WorkbenchException( "Shouldn't be able to have a hit with an unkown strand bias!" );
            }
          }
        }

        float pos_percentage = ( currentPosClusterBias / expandedRNA ) * 100.0f;
        float neg_percentage = ( currentNegClusterBias / expandedRNA ) * 100.0f;

        int testAbund = (int)currentCluster.get( 0 ).getAbundance();

        if ( ( testAbund >= params.getMinConsider() ) && ( ( pos_percentage >= params.getOrientation() ) || ( neg_percentage >= params.getOrientation() ) ) )
        {
          top_hits.add( currentCluster.get( 0 ) );
        }
      }
      if ( !top_hits.isEmpty() )
      {
        String newKey = top_hits.get( 0 ).getSequenceId().trim();
        top_hits_per_chromosome.put( newKey, top_hits );
      }
    }
    this.trackerReset();

    printVerbose( "completed sorting and filtering all clusters." );

    if ( AppUtils.INSTANCE.verbose() )
    {
      top_hits_per_chromosome.printStats();
      top_hits_per_chromosome.printHits();
    }

    return top_hits_per_chromosome;
  }

  private int countGenomeHits( String toCheck ) throws IOException
  {
    if ( patmanEntries.containsKey( toCheck ) )
    {
      return patmanEntries.get( toCheck ).getHitCount();
    }
    return 0;
  }

  /**
   *here we are creating the window extensions of the sequences clustered during checkForClusters()
   */
  private void checkForMicroRNAs( TopHits top_hits_per_chromosome ) throws IOException, HairpinExtensionException, Exception
  {
    this.trackerInitUnknownRuntime( "Cleaning memory" );
    System.gc();
    this.trackerReset();

    this.trackerInitUnknownRuntime( "Loading genome" );
    openGenomeFile();
    this.trackerReset();

 
    
   
    int numThread = 0;
    this.trackerReset();


    // This is run in command line mode only
    // Just outputs the headers for the files.
    if ( guiOutputTable == null )
    {
      System.out.println( "initial setup ok, miRCat is now processing..." );
      printVerbose( "Attempting creation: " + this.outputDirectoryFilename );

      boolean success = ( new File( this.outputDirectoryFilename ) ).mkdir();

      if ( success )
      {
        printVerbose( "Directory created" );
      }
      else
      {
        printVerbose( "Directory creation failed! Previous results might be overwritten" );
      }


      FileWriter outFile = new FileWriter( this.outputDirectoryFilename + DIR_SEPARATOR + "output.csv" );

      PrintWriter out = new PrintWriter( outFile );

      FileWriter outHFile = new FileWriter( this.outputDirectoryFilename + DIR_SEPARATOR + "miRNA_hairpins.txt" );

      PrintWriter outH = new PrintWriter( outHFile );

      FileWriter outmiRNAFile = new FileWriter( this.outputDirectoryFilename + DIR_SEPARATOR + "miRNA.fa" );

      PrintWriter outputmiRNA = new PrintWriter( outmiRNAFile );

      PrintWriter[] printArray =
      {
        out, outH, outputmiRNA
      };
      // Write text to file
      printArray[0].println( "Chromosome,Start,End,Orientation,Abundance,Sequence,sRNA length,# Genomic Hits,Hairpin Length,Hairpin % G/C content,Minimum Free Energy,Adjusted MFE,miRNA*,miRBaseID,pVal" );
      printArray[1].print( "" );
      printArray[2].print("");
      printArray[0].close();
      printArray[1].close();
      printArray[2].close();
    }

    printVerbose( "cat process..." + genomeData.size() );


    this.trackerInitUnknownRuntime( "Categorising miRNAs" );

    int i = 0;
    float processed_sRNA = 0;

    //for ( Map.Entry<String, FastaRecord> entry : genomeData.entrySet() )
    for ( Map.Entry<String, FastaRecord> entry : new TreeMap<String, FastaRecord>( genomeData ).entrySet() )
    {
      continueRun();
      
      if ( this.cancelRequested() )
      {
        es.shutdownNow();
        
        Thread.sleep( 6000 );

        // Kill all binary executors used for each thread.
        this.trackerInitUnknownRuntime( "Terminating thread pool" );
        
        
        
        this.trackerReset();
      }

      final String header = entry.getKey().trim();
//            System.out.println("cluster per chrom size: " + cluster_per_chrom.size() + " top hits size: " + top_hits_per_chromosome.size());
//            System.out.println("looking at fasta header: " + header);



      Patman currentCluster = top_hits_per_chromosome.get( header );

      ArrayList<Patman> clusteredCoords = cluster_per_chrom.get( header );

      //System.out.println("testing: " + this.top_hits_per_chromosome.get(toTest).size());
      if ( currentCluster == null || clusteredCoords == null )
      {
        continue;
      }

      i++;

      if ( myStatusLabel != null )
      {

        final int clusterSize = currentCluster.size();
        EventQueue.invokeLater( new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              myTextLog.insertString( myTextLog.getLength(), "looking at fasta header: " + header + LINE_SEPARATOR,
                myTextLog.getStyle( Tools.initStyles[0] ) );

            }
            catch ( BadLocationException ex )
            {
              LOGGER.log( Level.SEVERE, null, ex );
            }
            myProgBar.setValue( 0 );
            myProgBar.setMaximum( clusterSize );
          }
        } );
      }


      for ( PatmanEntry current_hit : currentCluster )
      {

        continueRun();
        int testIndex = 0;//clusteredCoords.indexOf(currentCluster);
        Patman clusterToCheck = new Patman();
        ArrayList<Patman> patmanLociInRange = LocusUtils.getPatmanLociInRange( clusteredCoords, current_hit.getStart(), current_hit.getEnd() );

        this.trackerInitKnownRuntime( "Categorising miRNAs", top_hits_per_chromosome.totalTopHits() );

        for ( Patman data : patmanLociInRange )
        {
          for ( PatmanEntry testMe : data )
          {
            this.trackerIncrement();

            if ( testMe.getStart() == current_hit.getStart()
              && testMe.getEnd() == current_hit.getEnd() )
            {


              clusterToCheck = data;
              if ( this.addMiRCatToLog )
              {
     
                for ( PatmanEntry foundMe : data )
                {

                  MIRCAT_LOGGER.log( Level.INFO, "Found locus: {0}{1}", new Object[]{ LINE_SEPARATOR, foundMe.toString() });
                  
                }
                MIRCAT_LOGGER.log( Level.INFO, "Locus size: {0}", clusterToCheck.size());
              }
              break;
            }
          }
          testIndex++;

        }
        if ( this.addMiRCatToLog )
        {

          MIRCAT_LOGGER.log( Level.INFO, "Best hit in locus: " );


          MIRCAT_LOGGER.log( Level.INFO, current_hit.toString() );

        }
        continueRun();

        this.trackerInitKnownRuntime( "Categorising miRNAs", top_hits_per_chromosome.totalTopHits() );

        if ( clusterToCheck.size() < params.getMinLocusSize() )
        {
          if ( this.addMiRCatToLog )
          {
            MIRCAT_LOGGER.log( Level.WARNING, "Locus size less than maxUniqueHits param so ignored" );
          }
          continue;
        }


        int seqLength = current_hit.getSequenceLength();

        if ( myStatusLabel != null )
        {
          processed_sRNA++;
        }
        if ( ( seqLength <= params.getMaxLength() ) && ( seqLength >= params.getMinLength() ) )
        {
          if ( myStatusLabel != null )
          {
            final float processed = processed_sRNA;
            final float top_total = top_hits_per_chromosome.totalTopHits();
            final int esQSize = es.getQueue().size();
            final long esCTCount = es.getCompletedTaskCount();
            final long esTCount = es.getTaskCount();
            final float populationStatus = Tools.roundTwoDecimals( ( processed / top_total ) * 100.0f );
            SwingUtilities.invokeLater( new Runnable()
            {
              @Override
              public void run()
              {

                myStatusLabel.setText( "<HTML><p>Populating..." + populationStatus + "% complete<br/>"
                  + "Thread Queue: " + esQSize + " Completed: " + esCTCount + "</p></HTML>" );

                myExecQBar.setMaximum( (int) esTCount );
                myExecQBar.setValue( esQSize );

                float percentage = 0.0f;
                if ( esTCount > 0 )
                {
                  percentage = ( (float) esCTCount / (float) esTCount ) * 100.0f;
                }
                myCompTaskBar.setValue( (int) percentage );
              }
            } );
          }

          continueRun();
          String processString = "Adding: " + entry.getValue().getSequenceId() + "/" + current_hit.getStart()
            + "-" + current_hit.getEnd() + " " + current_hit.getSequence() + "(" + current_hit.getAbundance() + ")"
            + LINE_SEPARATOR;

          int genomeHits = countGenomeHits( current_hit.getSequence() );
          float gc_content = SequenceUtils.DNA.getGCCount( current_hit.getSequence() );
          float percentage = ( gc_content / (float) current_hit.getSequenceLength() ) * 100.0f;

          
          if ( this.addMiRCatToLog )
          {
            MIRCAT_LOGGER.log( Level.INFO, "genomeHits: {0} GC %: {1}", new Object[]{ genomeHits, percentage });
          }

          if ( ( genomeHits <= params.getMaxGenomeHits() ) && ( percentage >= (float) params.getMinGC() ) )
          {
            if ( myStatusLabel != null )
            {

              final String stringToPrint = processString;
              SwingUtilities.invokeLater( new Runnable()
              {
                @Override
                public void run()
                {
                  try
                  {
                    myTextLog.insertString( myTextLog.getLength(), stringToPrint,
                      myTextLog.getStyle( Tools.initStyles[6] ) );
                  }
                  catch ( BadLocationException ex )
                  {
                    LOGGER.log( Level.SEVERE, null, ex );
                  }
                }
              } );


              this.trackerInitKnownRuntime( "Processing data in thread pool", 13 );
            }

            // Output hit to table if it passes all the tests.
            es.execute( new Window( entry.getValue(), current_hit.getStart(), current_hit.getEnd(), current_hit.getSequenceStrand(),
              current_hit, genomeHits, current_hit.getSequence(), clusterToCheck, patmanEntries, guiOutputTable, params,
              addMiRCatToLog, myTextLog,
              myProgBar,
              myStatusLabel, exe_list.get( numThread ), myMirBaseData, numThread, this.tempDir, this.outputDirectoryFilename ) );
            numThread++;
            if ( numThread >= totalThread )
            {
              numThread = 0;
            }
          }
          else
          {
            if ( this.addMiRCatToLog )
            {
              MIRCAT_LOGGER.log( Level.WARNING,  "genomeHits > maxGenomeHits param or GC Percentage too high so ignoring" );
            }
          }
        }
        else
        {
          if (this.addMiRCatToLog )
          {
            MIRCAT_LOGGER.log( Level.WARNING,  "seqLength > maxLength param or seqLength < minLength param so ignored" );
          }
        }

      }
      this.trackerReset();

//      entry = null;
//      System.gc();
    }
    es.shutdown();

    while ( !es.isTerminated() )
    {
      if ( this.cancelRequested() )
      {
        this.trackerInitUnknownRuntime( "Terminating thread pool" );
        es.shutdownNow();

        Thread.sleep( 6000 );
        // Kill all binary executors used for each thread.
        
        
        this.trackerReset();
      }

      continueRun();

      printVerbose( "core size: " + es.getCorePoolSize() );
      printVerbose( "current pool: " + es.getPoolSize() );
      printVerbose( "largest pool: " + es.getLargestPoolSize() );
      printVerbose( "maximum pool: " + es.getMaximumPoolSize() );
      printVerbose( "task count: " + es.getTaskCount() );
      printVerbose( "Thread Queue: " + es.getQueue().size() + " Completed: " + es.getCompletedTaskCount() );

      final float processed = processed_sRNA;
      final float top_total = top_hits_per_chromosome.totalTopHits();
      final int esQSize = es.getQueue().size();
      final long esCTCount = es.getCompletedTaskCount();
      final long esTCount = es.getTaskCount();
      final float populationStatus = Tools.roundTwoDecimals( ( processed / top_total ) * 100.0f );


      if ( myStatusLabel != null )
      {
        SwingUtilities.invokeLater( new Runnable()
        {
          @Override
          public void run()
          {

            myStatusLabel.setText( "<HTML><p>Populating..." + populationStatus + "% complete<br/>"
              + "Thread Queue: " + esQSize + " Completed: " + esCTCount + "</p></HTML>" );
            myExecQBar.setMaximum( (int) esTCount );
            myExecQBar.setValue( esQSize );

            float percentage = 0.0f;
            if ( esTCount > 0 )
            {
              percentage = ( (float) esCTCount / (float) esTCount ) * 100.0f;
            }
            myCompTaskBar.setValue( (int) percentage );
          }
        } );
      }
    }
    this.trackerReset();

    // Kill all binary executors used for each thread.
    this.trackerInitUnknownRuntime( "Terminating thread pool" );
    
    
    this.trackerReset();

    printVerbose( "Thread pool completed." );
  }

//  public void terminateExeManagers()
//  {
//    try
//    {
//      es.shutdownNow();
//      Thread.sleep( 6000 );
//      for ( BinaryExecutor temp : exe_list )
//      {
//        try
//        {
//          temp.stopExeManager();
//        }
//        catch ( UnknownHostException ex )
//        {
//         LOGGER.log( Level.SEVERE, null, ex );
//        }
//        catch ( IOException ex )
//        {
//          LOGGER.log( Level.SEVERE, null, ex );
//        }
//      }
//    }
//    catch ( InterruptedException ex )
//    {
//      LOGGER.log( Level.SEVERE, null, ex );
//    }
//  }

    public void generateVissrArrow(boolean completeModel, Point mousePos)
  {

    int row = guiOutputTable.rowAtPoint( mousePos );


    row = this.guiOutputTable.convertRowIndexToModel( guiOutputTable.getSelectedRow() );
    int lowRange = 0;
    int highRange = 0;
    
    

    
    
    //lowRange = source.getValueAt( row,  )

    lowRange = StringUtils.safeIntegerParse( guiOutputTable.getValueAt( row, 10 ).toString(), 0 );
    highRange = StringUtils.safeIntegerParse( guiOutputTable.getValueAt( row, 11 ).toString(), 0 );
    TierParameters[] generateVissrTP = new TierParameters[2];
    generateVissrTP[0] = new TierParameters.Builder( "miRNA Loci" ).build();
    generateVissrTP[1] = new TierParameters.Builder( "miRNA GFF" ).tierLabelBackgroundColour( Color.yellow ).glyphBackgroundColour( Color.GREEN ).build();

    generateVissrTP( lowRange, highRange, row, generateVissrTP );

    SequenceVizMainFrame vissr = SequenceVizMainFrame.createVisSRInstance( this.genomeFile, false, (TierParameters) null );
    
    
    //for ( ArrayList<String> sample : myExpressionMatrix.getFileNames() )
    
    vissr.addTier(generateVissrTP[0]);
    vissr.addTier(generateVissrTP[1]);
    String chromoID = guiOutputTable.getValueAt( row, 0 ).toString();
    vissr.displaySequenceRegion( chromoID, lowRange - 5, highRange + 5 );

    AppUtils.INSTANCE.activateFrame(vissr);
  }
  private void setupGFFTier( String ID, int row, TierParameters[] TPs)
  {
    int start = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 4 ).toString() );
    int end = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 5 ).toString() );

    char strand = guiOutputTable.getModel().getValueAt( row, 6 ).toString().charAt( 0 );

    String hairpinSequenceHTML = guiOutputTable.getModel().getValueAt( row, 3 ).toString();
    String hairpinDotBracketHTML = guiOutputTable.getModel().getValueAt( row, 12 ).toString();

    String hairpinSequence = hairpinSequenceHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" ).replace( "<u><font color=#0000FF>", "" ).replace( "</u></font>", "" ).replace( "<u><font color=#FF0000>", "" ).replace( "<font color=#FFFFFF>", "" ).replace( "</u>", "" );
    String hairpinDotBracket = hairpinDotBracketHTML.replace( "<HTML>", "" ).replace( "</HTML>", "" );
    //String finalHairpinDotBracket = (((((hairpinDotBracket.replace("-", ".")).replace("<", "(")).replace(">", ")")).replace("{", "(")).replace("}", ")")).replace("=", ".");

    GFFRecord record_miRNA = new GFFRecord( ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte) 0 );
    record_miRNA.addAttribute( "hairpin_element", "miRNA" );
    //record_miRNA.addAttribute( "Abundance: ", guiOutputTable.getValueAt( row, 7 ).toString()  );


    int hairpinStart = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 10 ).toString() );
    int hairpinEnd = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 11 ).toString() );

    GFFRecord recordHairpin = new GFFRecord( ID, "miRCat", "miRNA", hairpinStart, hairpinEnd, 0.0f, strand, (byte) 0 );
    recordHairpin.addAttribute( "HairpinSequence", hairpinSequence );
    recordHairpin.addAttribute( "Marked_up_Dot-Bracket", hairpinDotBracket );

    ArrayList<SequenceRange> newList = new ArrayList<SequenceRange>();
    newList.add( recordHairpin );
    newList.add( record_miRNA );

    start = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 17 ).toString() );
    end = Integer.parseInt( guiOutputTable.getModel().getValueAt( row, 18 ).toString() );

    if ( start >= 0 && end > 0 )
    {
      //String miRNA_Stars[] = guiOutputTable.getValueAt( row, 2 ).toString().split( " ");

      
      GFFRecord recordSTAR = new GFFRecord( ID, "miRCat", "miRNA", start, end, 0.0f, strand, (byte) 0 );
      recordSTAR.addAttribute( "hairpin_element", "miRNA*" );
      //record_miRNA.addAttribute( "Abundance: ", guiOutputTable.getValueAt( row, 7 ).toString()  );

      newList.add( recordSTAR );
    }

    TPs[1].addList( newList );

  }
  public void generateVissrTP( int lowRange, int highRange, int row, TierParameters[] TPs )
  {

    Patman locusItems = new Patman();
    double totalAbundance = 0.0;
    String chromoID = guiOutputTable.getValueAt( row, 0 ).toString();

    ArrayList<Patman> lociForChromo = cluster_per_chrom.get( chromoID );
    
    ArrayList<Patman> lociInRange = LocusUtils.getPatmanLociInRange( lociForChromo, lowRange, highRange );
    //int sampleNumber = 0;
    
    for ( Patman locus : lociInRange )
    {
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;

      for ( PatmanEntry element : locus )
      {

        if ( element.getStart() < currentMinCoord )
        {
          currentMinCoord = element.getStart();
        }
        if ( element.getEnd() > currentMaxCoord )
        {
          currentMaxCoord = element.getEnd();
        }
      }
      if ( currentMinCoord >= lowRange && currentMaxCoord <= highRange )
      {
        double value = 0.0;
        for ( PatmanEntry ele : locus )
        {
          

          totalAbundance += ele.getAbundance();
          locusItems.add( ele );
        }
      }
    }
    
  
    if ( !locusItems.isEmpty() )
    {
      TPs[0].addList( locusItems );
      locusItems.setGlyphHeights( (int)totalAbundance );
    }


    
    
    setupGFFTier(chromoID, row, TPs);
    
    
  }

  

  private class ChromosomeClusterMap extends HashMap<String, ArrayList<Patman>>
  {
    public ChromosomeClusterMap()
    {
      super();
    }

    public void printStats()
    {
      int chromosome_number = 0;
      for ( Map.Entry<String, ArrayList<Patman>> clusters : this.entrySet() )
      {
        printVerbose( "chromosome: " + ++chromosome_number + " header: " + clusters.getKey() + " created " + clusters.getValue().size() + " clusters." );
      }
    }
    
      public void printClusters()
      {
          for (Map.Entry<String, ArrayList<Patman>> clusters : new TreeMap<>(this).entrySet())
          {
              printVerbose("chromosome header: " + clusters.getKey() + " created " + clusters.getValue().size() + " clusters.");
              for(Patman p : clusters.getValue())
              {
                  System.out.println("Cluster: ");
                  for (PatmanEntry pe : p)
                  {
                      System.out.println("sequence: " + pe.getSequence() + " Abund: " + pe.getAbundance());
                      System.out.println("Start: " + pe.getStart() + " End: " + pe.getEnd());
                  }
              }
          }
      }
  }

  private class TopHits extends HashMap<String, Patman>
  {
    public TopHits()
    {
      super();
    }

    public int totalTopHits()
    {
      int totalSRNA = 0;

      for ( Map.Entry<String, Patman> top_hits : this.entrySet() )
      {
        totalSRNA += top_hits.getValue().size();
      }

      return totalSRNA;
    }
    
    public void printHits()
      {
          for (Map.Entry<String, Patman> top_hits : this.entrySet())
          {
              for(PatmanEntry pe : top_hits.getValue())
              {
                  System.out.println("sequence: " + pe.getSequence() + " Abund: " + pe.getAbundance());
              }
          }
      }

    public void printStats()
    {
      int curr_chrom = 1;
      for ( Map.Entry<String, Patman> top_hits : this.entrySet() )
      {
        printVerbose( "Chromosome " + curr_chrom++ + " header: " + top_hits.getKey() + " contains " + top_hits.getValue().size() + " top hits to be processed." );
      }
      printVerbose( "completed population of top hits!" );
    }
  }
  
  public static void main(String[] args)
  {
      
      DatabaseWorkflowModule.getInstance().setDebugMode(true);
      
      AppUtils.INSTANCE.setVerbose(true);
//      
//      Process_Hits_Patman mirCatProcess = new Process_Hits_Patman( new MiRCatParams(), 
//              new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/ATH_Tests/GSM118373_Rajagopalan_col0_leaf_nr_Align.patman"),
//              new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas"), Tools.getNextDirectory(), null );
//      
      
      
//      Process_Hits_Patman mirCatProcess = new Process_Hits_Patman( new MiRCatParams(), 
//              new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/nr_small_ATH.fa"),
//              new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas"), Tools.getNextDirectory(), null );
      
      Process_Hits_Patman mirCatProcess = new Process_Hits_Patman( new MiRCatParams(), 
              new File("TutorialData/FASTA/RNAOME/GSM118373_Rajagopalan_col0_leaf_nr.fa"),
              new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas"), Tools.getNextDirectory(), null );
      
      mirCatProcess.setOutputDirectoryFilename("/Developer/Applications/sRNAWorkbench/TestingData/MEM_DB_MC_Test");

      
      try
      {
        mirCatProcess.setupThreads( 1 );
      }
      catch ( Exception ex )
      {
        LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      mirCatProcess.run();
  }
}
