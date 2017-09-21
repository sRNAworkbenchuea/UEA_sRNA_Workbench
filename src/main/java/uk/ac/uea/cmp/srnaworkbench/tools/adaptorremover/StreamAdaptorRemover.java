/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.ArrayUtils;

import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.io.FileStreamer;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.DelimitedDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.FastqDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.decoders.TemporarySortedFastaDecoder;
import uk.ac.uea.cmp.srnaworkbench.io.externalops.ExternalSort;
import uk.ac.uea.cmp.srnaworkbench.io.sequencestream.SeqDataUtils;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.Definition.ADAPTOR_3_LENGTH_TO_USE;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.Definition.ADAPTOR_5_LENGTH_TO_USE;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol.HD_FULL;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol.HD_NONE;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol.HD_SIMPLE_5P;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.NumberUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.viewers.AbundanceDistributionViewer;

/**
 * Current sequencing devices produce reads with adaptor sequences at the 3' end
 * and sometimes the 5' end of the read.  This class inherits functionality to remove
 * those adaptor sequences making the reads/sequences ready for use by other tools.
 * 
 * This class is capable of loading reads from fastq and fasta files and can write
 * data out in non-redundant fasta format, or can be accessed straight from this class.
 * 
 * This class is a stream based version of the original Adaptor Removal program
 * designed to reduce memory loads by streaming data in and out of disk at run time
 * @author M.B Stocks
 */
public class StreamAdaptorRemover extends AdaptorRemover
{
  Charset charset = Charset.forName("UTF-8");
  byte delimiter = 10;//new line as default delimiter 
  private String threePrimeAdaptor;
  private String fivePrimeAdaptor;
  private Path discardedPath = null;

//  //variables for run time data statistics
  //HashMap<Integer, Integer> length_abundance = new HashMap<>();

  private HashMap<Integer, FilterStage> fiveP_Lengths;
  private HashMap<Integer, FilterStage> threeP_Lengths;
  private HashMap<Integer, FilterStage> HD_Lengths;
  private Path outputPath;
  private FastqDecoder decoder;
  private int countInputSequence;
  /**
   * Default constructor, probably shouldn't be used.
   * @throws Exception 
   */
  public StreamAdaptorRemover() throws Exception
  {
    this( null, null, new AdaptorRemoverParams() );
  }

  /**
   * Constructor for use from the command line
   * @param infile File containing reads with adaptor sequences
   * @param outfile The file to be generated containing sequences with adaptors
   * removed
   * @param params Parameters controlling the execution of the adaptor remover tool
   */
  public StreamAdaptorRemover( File infile, File outfile, AdaptorRemoverParams params )
  {
    this( infile, outfile, params, null, JOptionPane.NO_OPTION, null );
  }

  /**
   * Constructor for use from the GUI
   * @param infile File containing reads with adaptor sequences
   * @param outfile The file to be generated containing sequences with adaptors
   * removed
   * @param params Parameters controlling the execution of the adaptor remover tool
   * @param tracker The status tracker object for reporting the current execution 
   * status in real-time
   */
  public StreamAdaptorRemover( File infile, File outfile, AdaptorRemoverParams params, int showingAbundancePlots,
                         StatusTracker tracker )
  {
    this( infile, outfile, params, tracker, showingAbundancePlots, null );

  }

  /**
   * Constructor to be used from within existing code when the reads containing 
   * adaptors are already stored in memory
   * @param data_in Reads containing adaptors
   * @param params Parameters controlling the execution of the adaptor remover tool
   */
  public StreamAdaptorRemover( FastaMap data_in, AdaptorRemoverParams params )
  {
    this( null, null, params, null, JOptionPane.NO_OPTION, data_in );
  }

  /**
   * Private constructor, used internally to channel all constructors through one
   * common interface.  Not all params need to be specified.
   * @param infile File containing reads with adaptor sequences
   * @param outfile The file to be generated containing sequences with adaptors
   * removed
   * @param params Parameters controlling the execution of the adaptor remover tool
   * @param tracker The status tracker object for reporting the current execution 
   * status in real-time 
   * @param data_in Reads containing adaptors
   */
  private StreamAdaptorRemover( File infile, File outfile, AdaptorRemoverParams params, 
                                                           StatusTracker tracker, int showingAbundancePlots, FastaMap data_in )
  {
    super( infile,  outfile,  params,  tracker,  showingAbundancePlots,  data_in );

//    
    
  
    this.showingAbundancePlots = showingAbundancePlots;

    this.infile = infile;
    this.outfile = outfile;
    this.params = params;

    this.reads = data_in;
    this.loadfile = true;
    Tools.trackPage("Adapter Removal Stream Procedure Loaded");
  }
  public HashMap<Integer, FilterStage> getFiveP_Lengths()
  {
    return fiveP_Lengths;
  }

  public HashMap<Integer, FilterStage> getThreeP_Lengths()
  {
    return threeP_Lengths;
  }

  public HashMap<Integer, FilterStage> getHD_Lengths()
  {
    return HD_Lengths;
  }
  private FileStreamer<byte[][]> setup()
  {
       
    // Initialise variables for this run      
    if ( params.getDiscardLog() != null )
    {
      try
      {
        discardedPath = Files.createFile( Paths.get( params.getDiscardLog().getAbsolutePath() ) );
      }
      catch ( IOException ex )
      {
        discardedPath = Paths.get( params.getDiscardLog().getAbsolutePath() );
        LOGGER.log( Level.INFO, "{0}" + "Discard log file creation failed because this log already exists. This will cause an append", ex.getMessage() );

      }
    }
    else
    {
      discardedPath = null;
    }

    this.nr_seq_lengths = new HashMap<>();
    this.r_seq_lengths = new HashMap<>();
    this.results = new ArrayList<>();
    if(this.showingAbundancePlots == JOptionPane.YES_OPTION )
      this.threeP_Lengths = new HashMap<>();
    
    if((params.getHD_Setting() != HD_Protocol.HD_NONE)&&(this.showingAbundancePlots == JOptionPane.YES_OPTION ))
    {
      HD_Lengths = new HashMap<>();
    }
    


    // Should this be here?  Maybe not required.
    if ( params.get3PrimeAdaptor() == null || params.get3PrimeAdaptor().equals( "" ) )
    {
      throw new IllegalArgumentException( "3' adaptor is mandatory.  Please provide a 3' adaptor sequence (adaptor_sequence_3)." );
    }
    // Store the adaptors as field so chunks can access without setup each time
    threePrimeAdaptor = this.params.get3PrimeAdaptor();
    fivePrimeAdaptor = this.params.get5PrimeAdaptor();

    // Set the adaptors to null so that the checking is simpler
    if ( threePrimeAdaptor != null && threePrimeAdaptor.isEmpty() )
    {
      threePrimeAdaptor = null;
    }
    if ( fivePrimeAdaptor != null && fivePrimeAdaptor.isEmpty() )
    {
      fivePrimeAdaptor = null;
    }

    // Trim the 3' adaptor to the requested length
    int adpt3_len = this.params.get3PrimeAdaptorLength() > threePrimeAdaptor.length() ? threePrimeAdaptor.length() : this.params.get3PrimeAdaptorLength();
    int min3_len = ADAPTOR_3_LENGTH_TO_USE.getLowerLimit( Integer.class );

    if ( adpt3_len < min3_len )
    {
      throw new IllegalArgumentException( "3' adaptor sequence is too short, the 3' adaptor must be at least " + min3_len + "nt long." );
    }

    threePrimeAdaptor = threePrimeAdaptor == null ? null : threePrimeAdaptor.substring( 0, adpt3_len );

    // Trim the 5' adaptor to the requested length if required
    if ( fivePrimeAdaptor != null )
    {
      int adpt5_len = this.params.get5PrimeAdaptorLength() > fivePrimeAdaptor.length() ? fivePrimeAdaptor.length() : this.params.get5PrimeAdaptorLength();
      int min5_len = ADAPTOR_5_LENGTH_TO_USE.getLowerLimit( Integer.class );

      if ( adpt5_len < min5_len )
      {
        throw new IllegalArgumentException( "5' adaptor sequence is too short, the 5' adaptor must be at least " + min5_len + "nt long." );
      }

      int start5 = fivePrimeAdaptor.length() - adpt5_len;

      fivePrimeAdaptor = fivePrimeAdaptor == null ? null : fivePrimeAdaptor.substring( start5, fivePrimeAdaptor.length() );
      if(this.showingAbundancePlots == JOptionPane.YES_OPTION )
        this.fiveP_Lengths = new HashMap<>();
    }

    //build stream reader and decode the file
    decoder = new FastqDecoder();
    FileStreamer<byte[][]> reader = FileStreamer.create( decoder,  "r", this.infile);
    try
    {
      outputPath = Files.createFile( Paths.get( outfile.toURI() ) );
      
    }
    catch ( IOException ex )
    {
      outputPath = Paths.get( outfile.toURI() ) ;
      LOGGER.log(Level.INFO, "{0}" + "file creation failed here because file already exists. However, program should not reach this point "
        + "without first warning user of file overwrite", ex.getMessage());
      
    }

    
    return reader;
    
    
  }
  @Override
  protected void process() throws Exception
  {
    completed = false;
    // Cleanup any rubbish before processing.
    System.gc();

    FileStreamer<byte[][]> data = null;
    // If we need to gather data from the input file, do that, otherwise use data provided
    if ( loadfile )
    {
      data = setup();
    }
    
    continueRun();

    //convert FASTQ to FASTA and create file to sort
    String absolutePath = Tools.getNextDirectory().getAbsolutePath();
    Path tempUnSortedPath = Paths.get( absolutePath + DIR_SEPARATOR + FilenameUtils.removeExtension( outfile.getName() ) + "_UNSORT.fa" );
    Path R_FASTA_Path = Paths.get( FilenameUtils.removeExtension( outfile.getAbsolutePath()) + "_R.fa" );
    //Path tempRAW_FASTA_Path = Paths.get( FilenameUtils.removeExtension( outfile.getAbsolutePath() ) + "_RAW.fa" );
    
    String removeExtension = FilenameUtils.removeExtension( outputPath.toString());
    Path RAW_outputPath = null;
    String NR_outputLocation = removeExtension + "_RAW.fa";
    try
    {
      RAW_outputPath = Files.createFile( Paths.get( NR_outputLocation ) ); 
    }
    catch ( IOException ex )
    {
      RAW_outputPath = Paths.get( NR_outputLocation ) ;
      LOGGER.log(Level.INFO, "{0}" + "file creation failed here because file already exists. However, program should not reach this point "
        + "without first warning user of file overwrite", ex.getMessage());
      
    }
    
    int[] counts = {0,0,0,0,0,0,0,0,0,0};
    
    //boolean isHD = params.getHD_Setting() == HD_SIMPLE_5P || params.getHD_Setting() == HD_FULL;
    SeqDataUtils.FASTQ_2_FASTA(data, counts, RAW_outputPath, tempUnSortedPath);
    
    continueRun();
    //Remove adapters and create R output including stats
    trimInput(tempUnSortedPath, R_FASTA_Path, counts);
    
    continueRun();
    
    
    SeqDataUtils.convertRedundantToNonRedundant( R_FASTA_Path, outputPath, counts, params.getMinLength(), params.getMaxLength() );

    
    
    continueRun();
    
    results.add( new FilterStage( "Total number of sequences in input file (" + this.infile.getName() + "):", "Input", counts[0], -1 ) );

    String long_stage_name = "Sequences remaining after " + AdaptorType.THREE_PRIME.toShortString() + " adaptor removal (" + threePrimeAdaptor + "):";
    results.add( new FilterStage( long_stage_name, AdaptorType.THREE_PRIME.toShortString() + " Adaptor Matches", counts[2], counts[3] ) );
    if (fivePrimeAdaptor != null )
    {
      long_stage_name = "Sequences remaining after " + AdaptorType.FIVE_PRIME.toShortString() + " adaptor removal (" + fivePrimeAdaptor + "):";
      results.add( new FilterStage( long_stage_name, AdaptorType.FIVE_PRIME.toShortString() + " Adaptor Matches", counts[4], counts[5] ) );
      //generateAbundancePlot( "HD Adapter Removal", this.infile.getName() );
    }
    if ( this.params.getHD_Setting() != HD_Protocol.HD_NONE )
    {
      long_stage_name = "Sequences remaining after HD Adapter Stripping (" + this.params.getMinLength() + "-" + this.params.getMaxLength() + "):";
      this.results.add( new FilterStage( long_stage_name, "HD Filtering", counts[6], -1 ) );
      //generateAbundancePlot( "HD Adapter Removal", this.infile.getName() );
    }
    
    long_stage_name = "Sequences remaining after length range filtering (" + this.params.getMinLength() + "-" + this.params.getMaxLength() + "):";
    this.results.add( new FilterStage( long_stage_name, "Length Filtering", counts[8], counts[9] ) );

    if(fiveP_Lengths != null)
      generateAbundancePlot( "5' Adapter Removal", infile.getName(), this.fiveP_Lengths);
    if(threeP_Lengths != null)
      generateAbundancePlot( "3' Adapter Removal", infile.getName(), this.threeP_Lengths);
    if(HD_Lengths != null)
      generateAbundancePlot( "HD Adapter Removal", infile.getName(), this.HD_Lengths);
    
    generateAbundancePlot( "Final Results", infile.getName(), this.nr_seq_lengths);
    //clean up temporary files
    //must be enclosed in try and catch blocks because windows will not allow this to run
    try{
    Files.deleteIfExists( tempUnSortedPath );
    }
    catch(IOException e)
    {
      LOGGER.log( Level.WARNING, "Could not delete mapped files. Most likely due to Windows OS{0}", e.getMessage());
    }
    
    // If outfile is specified then dump results and filtered file to disk
    if ( outfile != null )
    {
      trackerInitUnknownRuntime( "Outputting statistics" );


      continueRun();

      // Write out results
      generateResultsFile( new File( outfile.getPath() + "_overview.txt" ), nr_seq_lengths, results );

      continueRun();

      trackerReset();
    }
    
    this.completed = true;
   
    
  }
//

  private void trimInput(Path raw_fastaPath, Path R_FASTA_Path, int[] counts)
  {
    
    try
    {
      //open FASTA file for streaming
      TemporarySortedFastaDecoder sortDecoder = new TemporarySortedFastaDecoder();
      FileStreamer<byte[]> reader = FileStreamer.create( sortDecoder, "r", raw_fastaPath.toFile() );

      Iterator<List<byte[]>> iterator = reader.iterator();
      List<byte[]> chunk;
      
      Integer abundance = 0;
      while ( iterator.hasNext())//iterator
      {
        chunk = iterator.next();
        //growable chunk size to give plenty of room for extra information
        ArrayList<Byte> toWriteNR = new ArrayList<>();
        int numberOfBytes[] = {0};
       
        byte[] toDiscard = null;
        if ( this.params.getDiscardLog() != null )
        {
          toDiscard = new byte[FileStreamer.getChunkSize()];
        }

        int[] index = new int[1];
        index[0] = 0;
        //moving value that we are looking at in the current chunk
        for(byte[] toExamine : chunk)
        {
          //array used to store processed sequences
          byte[] toProcess = null;
          //strip the adapters
          if ( fivePrimeAdaptor != null )
          {
            toProcess = removeAdapters( toExamine, toDiscard, index, counts, abundance, AdaptorType.FIVE_PRIME, fivePrimeAdaptor, threePrimeAdaptor );
          }
          else
          {
            toProcess = removeAdapters( toExamine, toDiscard, index, counts, abundance, AdaptorType.THREE_PRIME, threePrimeAdaptor );
          }
          
          if ( toProcess != null )//adapter removal went ok
          {
            createR_Record( toWriteNR, numberOfBytes, toProcess);
          }
        }

        
        Byte[] clean = new Byte[numberOfBytes[0]];
        byte[] byteArray = ArrayUtils.toPrimitive(toWriteNR.toArray(clean));
        if(toWriteNR.size() > 0)
          Files.write( R_FASTA_Path, byteArray, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
        if(toDiscard != null && toDiscard.length > 0)
        {
          Files.write( discardedPath, toDiscard, StandardOpenOption.APPEND, StandardOpenOption.CREATE );
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, ex.getMessage());
    }
    

  }
  
  private byte[] removeAdapters( byte[] record, byte[] discard, int[] index, int[] counts, int abundance,  AdaptorRemover.AdaptorType type, String... adaptors )
  {
    byte[] toProcess = null;
    String seq = new String(record, charset);
   
    // Get the details for this read.
    String processed_seq = "";
    int pos;
    
    //strip 5' adapter first if required
    if ( type == AdaptorType.FIVE_PRIME )
    {
      pos = seq.indexOf( adaptors[1] );
      if ( pos > 0 )
      {
        processed_seq = seq.substring( pos + adaptors[1].length() );
        seq = processed_seq;
      }
      else
      {
        //DISCARD
        if ( discardedPath != null )
        {
          addToDiscard( seq, "No Five Prime Adapter in Sequence", discard, index );
        }
      }

    }
    //remove 3' adapter whatever the type
    
    pos = seq.indexOf( adaptors[0] );
    //int cap = type == AdaptorType.THREE_PRIME ? 0 : -1;
    if ( pos > 0 )
    {
      processed_seq = seq.substring( 0, pos );
      int length = processed_seq.length();
      
      if(this.threeP_Lengths != null)
      {
        updateLengthCounts(length, abundance, threeP_Lengths);
      }
      
      if(this.fiveP_Lengths != null)
      {
        updateLengthCounts(length, abundance, fiveP_Lengths);
      }
      
      //discover HD settings and act
      HD_Protocol hD_Setting = params.getHD_Setting();
      String temp = "";
      switch ( hD_Setting )//process HD adaptor library type
      {

        case HD_FULL://strip 4nt from start and end of processed sequence
          if ( processed_seq.length() <= 8 )
          {
            
            //DISCARD
            if ( discardedPath != null )
            {
              addToDiscard( seq, "Sequence too short for HD adapter removal, possible Adapter Adapter ligation", discard, index );
            }
          }
          else
          {
            counts[6]++;
            temp = processed_seq.substring( 4, processed_seq.length() - 4 );
            processed_seq = temp;
            //update in case of HD adapter trimming 
            
          }
          length = processed_seq.length();
          break;
        case HD_SIMPLE_3P://strip 4nt from 3p end of processed sequence
          if ( processed_seq.length() <= 4 )
          {
            //empty sequence so it will be removed at the length filtering
            //processed_seq = "";
            //DISCARD
            if ( discardedPath != null )
            {
              addToDiscard( seq, "Sequence too short for HD adapter removal", discard, index );
            }
          }
          else
          {
            temp = processed_seq.substring( 0, processed_seq.length() - 4 );
            processed_seq = temp;
            //update in case of HD adapter trimming 
          }
          length = processed_seq.length();
          break;
        case HD_SIMPLE_5P://strip 3nt from 5p end of processed sequence
          if ( processed_seq.length() <= 4 )
          {

            //empty sequence so it will be removed at the length filtering
            //processed_seq = "";
            //DISCARD
            if ( discardedPath != null )
            {
              addToDiscard( seq, "Sequence too short for HD adapter removal", discard, index );
            }
          }
          else //5p stripping already occured during creation of FASTA data from FASTQ
          {
            temp = processed_seq.substring( 4, processed_seq.length() );
            processed_seq = temp;
         
          }
          //update in case of HD adapter trimming 
          length = processed_seq.length();
          break;
        case HD_NONE://same as default do nothing
          break;
        default://do nothing
          break;
          

      }
      
      if((this.HD_Lengths != null)&&length > 0)
      {
        updateLengthCounts(length, abundance, HD_Lengths);
      }

      {
        toProcess = Arrays.copyOf( processed_seq.getBytes(), processed_seq.getBytes().length);
      }
        

    }
    else
    {
      //DISCARD
      if ( discardedPath != null )
      {
        addToDiscard( seq, "No Three Prime Adapter in Sequence", discard, index );
      }
    }
    
    return toProcess;
    

  }

  private void addToDiscard(String processed_seq, String header, byte[] discard, int[] index)
  {
    
    discard[index[0]] = '>';
    index[0]++;
    for ( byte base : header.getBytes() )
    {

      discard[index[0]] = base;
      index[0]++;
    }
    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      discard[index[0]] = b;
      index[0]++;
    }
    //write sequence
    for ( byte base : processed_seq.getBytes() )
    {

      discard[index[0]] = base;
      index[0]++;
    }

    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      discard[index[0]] = b;
      index[0]++;
    }

  }

  private void generateAbundancePlot( String title, String name, Map<Integer, FilterStage> lengths )
  {
    if ( this.showingAbundancePlots == JOptionPane.YES_OPTION )
    {
      AbundanceDistributionViewer myAbundDist = AbundanceDistributionViewer.generateAbundanceDistribution();
      myAbundDist.setTitle( title );

      myAbundDist.setGraphTitle( "Filename: " + name );
      myAbundDist.initiliseSizeClassDistribution(lengths);
      myAbundDist.revalidate();
    }
  }

  private void createR_Record(ArrayList<Byte> toWrite, int[] numberOfBytes, byte[] currentRecord)
  {

    int length = currentRecord.length;
    //write header
    byte FASTAheader = '>';
    toWrite.add( FASTAheader );
    numberOfBytes[0]++;
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      numberOfBytes[0]++;
    }
    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }
    //write sequence
    for ( byte base : currentRecord )
    {

      toWrite.add( base );
      numberOfBytes[0]++;
    }

    for ( byte b : LINE_SEPARATOR.getBytes() )
    {
      toWrite.add( b );
      numberOfBytes[0]++;
    }

    updateLengthCounts( length, 1, r_seq_lengths );
  }
  private void updateLengthCounts(int length, int abundance, Map<Integer, FilterStage> updateMe)
  {
    FilterStage len_count = updateMe.get( length );
    if ( len_count == null )
    {
      len_count = new FilterStage( "", abundance, 1 );
    }
    else
    {
      len_count.incCounters( abundance );
    }
    updateMe.put( length, len_count );
  }

  
  
}
