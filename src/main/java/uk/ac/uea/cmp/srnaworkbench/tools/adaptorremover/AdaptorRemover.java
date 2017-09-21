
package uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastqReader;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.Definition.ADAPTOR_3_LENGTH_TO_USE;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.Definition.ADAPTOR_5_LENGTH_TO_USE;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol;
import static uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverParams.HD_Protocol.HD_NONE;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.viewers.AbundanceDistributionViewer;


import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;

/**
 * Current sequencing devices produce reads with adaptor sequences at the 3' end
 * and sometimes the 5' end of the read.  This class provides functionality to remove
 * those adaptor sequences making the reads/sequences ready for use by other tools.
 * 
 * This class is capable of loading reads from fastq and fasta files and can write
 * data out in non-redundant fasta format, or can be accessed straight from this class.
 * @author ezb11yfu & Matthew Stocks
 * Example command line run used for debugging within IDE: 
 * -tool adaptor -srna_file /Developer/Applications/sRNAWorkbench/Workbench/TutorialData/FASTQ/tomato.fastq 
 * -out_file /Developer/Applications/sRNAWorkbench/TestingData/AR_output_basespace/CLI_Out/output.fa 
 * -params /Developer/Applications/sRNAWorkbench/TestingData/AR_output_basespace/default_adaptorremover_params.cfg 
 * -f 
 * -basespace
 */
public class AdaptorRemover extends RunnableTool
{
  protected final static String TOOL_NAME = "AR";
  // Input
  protected AdaptorRemoverParams params;
  protected File infile;
  protected File outfile;
  // Intenal vars
  protected Map<Integer, FilterStage> nr_seq_lengths;
  protected Map<Integer, FilterStage> r_seq_lengths;
  protected List<FilterStage> results;
  protected FastaMap reads;
  protected FastaMap seqs;
  protected boolean loadfile;
  protected boolean completed = false;
  protected int showingAbundancePlots = JOptionPane.NO_OPTION;
  protected FastaMap processed_records;

  /**
   * Default constructor, probably shouldn't be used.
   * @throws Exception 
   */
  public AdaptorRemover() throws Exception
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
  public AdaptorRemover( File infile, File outfile, AdaptorRemoverParams params )
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
  public AdaptorRemover( File infile, File outfile, AdaptorRemoverParams params, int showingAbundancePlots,
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
  public AdaptorRemover( FastaMap data_in, AdaptorRemoverParams params )
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
  protected AdaptorRemover( File infile, File outfile, 
                                         AdaptorRemoverParams params, 
                                         StatusTracker tracker, 
                                         int showingAbundancePlots, FastaMap data_in )
  {
    super( TOOL_NAME, tracker );
    
  
    this.showingAbundancePlots = showingAbundancePlots;

    this.infile = infile;
    this.outfile = outfile;
    this.params = params;

    this.reads = data_in;
    this.loadfile = true;
    Tools.trackPage("Adapter Removal Procedure Loaded");
    //JGoogleAnalyticsTracker google_tracker = Tools.generateNewTracker();
    //Tools.startTracking("Adapter Removal Main Procedure Load", google_tracker);
  }

  /**
   * Gets a breakdown of how many sequences were originally provided, how many 
   * had 3' adaptors, 5; adaptors, invalid lengths and finally how many sequences
   * were remaining after processing
   * @return Processing statistics
   */
  public List<FilterStage> getResults()
  {
    return results;
  }

  /**
   * Gets the length distribution of sequences after adaptors have been removed
   * @return Sequence length distribution
   */
  public Map<Integer, FilterStage> getLengths()
  {
    return nr_seq_lengths;
  }

  /**
   * Gets the processed sequences after adaptors removal
   * @return Trimmed reads
   */
  public FastaMap getProcessedSeqs()
  {
    return seqs;
  }

  /**
   * Clears any stored information from the object
   */
  public void clear()
  {
    this.reads = null;
    this.seqs = null;
    this.nr_seq_lengths = null;
    this.results = null;
  }

  /**
   * This method starts execution of the adaptor removal tool.  Execution path
   * will vary slightly depending on the user defined parameters but in general:
   * 1. reads containing adaptors are loaded.
   * 2. 5' adaptors are removed if required.
   * 3. 3' adaptors are removed if required.
   * 4. Sequences with adaptors removed are stored within the object for later use.
   * @throws java.lang.Exception
   */
  @Override
  protected void process() throws Exception
  {
    completed = false;
    // Cleanup any rubbish before processing.
    System.gc();

    // If we need to gather data from the input file, do that, otherwise use data provided
    if ( loadfile )
    {
      this.reads = loadReads();
    }

    continueRun();

    // Should this be here?  Maybe not required.
    if ( params.get3PrimeAdaptor() == null || params.get3PrimeAdaptor().equals( "" ) )
    {
      throw new IllegalArgumentException( "3' adaptor is mandatory.  Please provide a 3' adaptor sequence (adaptor_sequence_3)." );
    }

    // Do the work...
    this.seqs = removeAdaptors( this.reads );

    continueRun();

    // If outfile is specified then dump results and filtered file to disk
    if ( outfile != null )
    {
      trackerInitUnknownRuntime( "Outputting results" );

      // Write filtered data to the output fasta file
      new SRNAFastaWriter( outfile ).process( seqs, true, true );

      continueRun();

      // Write out results
      generateResultsFile( new File( outfile.getPath() + "_overview.txt" ), nr_seq_lengths, results );

      continueRun();

      trackerReset();
    }
    this.completed = true;
  }

  /**
   * Writes out the set of currently processed sequences to file in non-redundant
   * form
   * @param out_file The file to write to.
   * @throws IOException Thrown if there was a problem writing to the file
   */
  public void writeSeqsToFasta( File out_file ) throws IOException
  {
    writeSeqsToFasta( this.seqs, out_file, true );
  }

  private void writeSeqsToFasta( FastaMap seqs_out, File out_file, boolean abd ) throws IOException
  {
    SRNAFastaWriter writer = new SRNAFastaWriter( out_file );

    writer.process( seqs_out, true, abd );
  }

  /**
   * Loads short reads from fastq or fasta file and returns a non-redundant map
   * of the reads.
   * @return The reads in non-redundant form
   * @throws IOException Thrown if there was a problem loading the reads from file
   */
  private FastaMap loadReads() throws IOException
  {
    FastaMap loaded_reads = null;

    // Compress input file into non-redundant format in memory using a hashmap
    int dotpos = this.infile.getName().lastIndexOf( "." );
    String extension = this.infile.getName().substring( dotpos );

    if ( extension.equalsIgnoreCase( ".fastq" ) || extension.equalsIgnoreCase( ".fq" ) )
    {
      trackerInitUnknownRuntime( "Reading FastQ file" );
      loaded_reads = new FastaMap( new SRNAFastqReader( infile ).process() );
      trackerReset();
    }
    else
    {
      if ( extension.equalsIgnoreCase( ".fasta" ) || extension.equalsIgnoreCase( ".fa" ) )
      {
        trackerInitUnknownRuntime( "Reading FastA file" );
        loaded_reads = new FastaMap( new SRNAFastaReader( infile ).process() );
        trackerReset();
      }
      else
      {
        // Can't tell what the file is from the extension so look at the
        // first character in the file.
        InputStreamReader isr = new InputStreamReader( new FileInputStream( infile ) );

        int c = isr.read();
        isr.close();

        if ( c == '@' )
        {
          // This is a fastq file
          trackerInitUnknownRuntime( "Reading FastQ file" );
          loaded_reads = new FastaMap( new SRNAFastqReader( infile ).process() );
          trackerReset();
        }
        else
        {
          if ( c == '#' )
          {
            trackerInitUnknownRuntime( "Reading FastA file" );
            loaded_reads = new FastaMap( new SRNAFastaReader( infile ).process() );
            trackerReset();
          }
          else
          {
            // Have no clue what the file is so throw an exception
            throw new IOException( "Unknown input file format.  Ensure the input file is a valid FASTQ or FASTA file" );
          }
        }
      }
    }

    return loaded_reads;
  }

  /**
   * Removes adaptors from all provided reads and returns a map of sequences with
   * their adaptors removed.
   * @param reads
   * @return sequences with adaptors removed
   * @throws Exception 
   */
  private FastaMap removeAdaptors( FastaMap reads ) throws Exception
  {
    // Initialise variables for this run        
    Map<String, String> discarded = new HashMap<String, String>();

    this.nr_seq_lengths = new HashMap<Integer, FilterStage>();
    this.r_seq_lengths = new HashMap<Integer, FilterStage>();
    this.results = new ArrayList<FilterStage>();

    // Store the adaptors locally
    String threePrimeAdaptor = this.params.get3PrimeAdaptor();
    String fivePrimeAdaptor = this.params.get5PrimeAdaptor();

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
    }

    results.add( new FilterStage( "Total number of sequences in input file (" + this.infile.getName() + "):", "Input", reads.getTotalSeqCount(), 
      reads.getDistinctSeqCount() ) );

    processed_records = reads;


    // Remove 5' adaptors if necessary
    if ( fivePrimeAdaptor != null )
    {
      processed_records = doAdaptorRemoval( processed_records, fivePrimeAdaptor, discarded, AdaptorType.FIVE_PRIME );
      
      generateAbundancePlot( "5' Adapter Removal", this.infile.getName());
    }

    // Remove 3' adaptors
    processed_records = doAdaptorRemoval( processed_records, threePrimeAdaptor, discarded, AdaptorType.THREE_PRIME );

    generateAbundancePlot( "3' Adapter Removal", infile.getName());
 
    
    //strip HD Adapters
    processed_records = stripHDSequences(processed_records, discarded);
    

    if(this.params.getHD_Setting() != HD_NONE)
      generateAbundancePlot( "HD Adapter Removal", this.infile.getName());

    // Remove sequences
    processed_records = filterByLength( processed_records, discarded );
    
    generateAbundancePlot( "Filtering", this.infile.getName());

    // Compile new FastaRecords containing processed sequences
    processed_records = compileResults( processed_records );
    
    generateAbundancePlot( "Final Results", this.infile.getName());

    // Output sequences that were not used to the log file if requested
    if ( this.params.getDiscardLog() != null )
    {
      trackerInitUnknownRuntime( "Creating discarded sequence log file" );

      FastaMap disc = convertDiscarded( discarded );

      writeSeqsToFasta( disc, this.params.getDiscardLog(), false );

      trackerReset();
    }
    
    

    // Return the results
    return processed_records;
  }
  
  private FastaMap doAdaptorRemoval( FastaMap input, String adaptor, Map<String, String> discarded, AdaptorType type ) throws IOException
  {
    String adpt_str = type.toShortString();

    if ( adaptor == null )
    {
      throw new IllegalArgumentException( adpt_str + " Adaptor must not be null" );
    }

    FastaMap processed = new FastaMap();

    trackerInitKnownRuntime( "Removing " + adpt_str + " adaptors from input sequences", input.size() );

    // Run through each distinct read
    for ( Map.Entry<String, FastaRecord> me : input.entrySet() )
    {
      continueRun();

      trackerIncrement();

      // Get the details for this read.
      String seq = me.getKey();
      int abundance = me.getValue().getAbundance();


      //if(seq.contains( ""))
      int pos = seq.indexOf( adaptor );

      int cap = type == AdaptorType.THREE_PRIME ? 0 : -1;

      if ( pos > cap )
      {
        String processed_seq = "";

        if ( type == AdaptorType.THREE_PRIME )
        {
          // Extract the sequence without the 3' adaptor (and anything after it)
          processed_seq = seq.substring( 0, pos );
        }
        else
        {
          if ( type == AdaptorType.FIVE_PRIME )
          {
            // Extract the sequence without the 5' adaptor (and anything before it)
            processed_seq = seq.substring( pos + adaptor.length() );
          }
          else
          {
            throw new IllegalArgumentException( "Cannot process unknown adaptor type." );
          }
        }
        
        if (  processed.containsKey( processed_seq ) )
        {
          String orig_seq = processed.get( processed_seq ).getSequence();
          int old_abd = processed.get( processed_seq ).getAbundance();
          FastaRecord fr = new FastaRecord( orig_seq, old_abd + abundance );
          processed.put( processed_seq, fr );
        }
        else
        {
          processed.put( processed_seq, me.getValue() );
        }
      }
      else
      {
        String original_seq = me.getValue().getSequence();

        // Pass back the original sequence for the discarded seq map
        discarded.put( original_seq + "_" + abundance, "No " + adpt_str + " Adaptor Match [Processed Seq.: " + seq + "(" + abundance + ")]" );
      }
    }

    String long_stage_name = "Sequences remaining after " + adpt_str + " adaptor removal (" + adaptor + "):";
    this.results.add( new FilterStage( long_stage_name, adpt_str + " Adaptor Matches", processed.getTotalSeqCount(), processed.getDistinctSeqCount() ) );

    trackerReset();

    return processed;
  }
  private void generateAbundancePlot( String title, String name )
  {
    if ( this.showingAbundancePlots == JOptionPane.YES_OPTION )
    {
      AbundanceDistributionViewer myAbundDist = AbundanceDistributionViewer.generateAbundanceDistribution();
      myAbundDist.setTitle( title );

      myAbundDist.inputData( processed_records, "Filename: " + name );
      myAbundDist.initiliseSizeClassDistribution();
      myAbundDist.revalidate();
    }
  }
  private FastaMap stripHDSequences(FastaMap input, Map<String, String> discarded) throws IOException
  {
    
    FastaMap processed = new FastaMap();
    trackerInitKnownRuntime( "Stripping HD Adapters ", input.size() );
    
    // Run through each distinct read
    for ( Map.Entry<String, FastaRecord> me : input.entrySet() )
    {
      continueRun();

      trackerIncrement();
      
      

      // Get the details for this read.
      String processed_seq = me.getKey();
      int abundance = me.getValue().getAbundance();
      


      //discover HD settings and act
      HD_Protocol hD_Setting = params.getHD_Setting();
      String temp = "";
      switch ( hD_Setting )//process HD adaptor library type
      {

        case HD_FULL://strip 4nt from start and end of processed sequence
          if ( processed_seq.length() <= 8 )
          {
            
            processed_seq = "";
          }
          else
          {
            temp = processed_seq.substring( 4, processed_seq.length() - 4 );
            processed_seq = temp;
          }
          break;
        case HD_SIMPLE_3P://strip 4nt from 3p end of processed sequence
          if ( processed_seq.length() <= 4 )
          {
            processed_seq = "";
          }
          else
          {
            temp = processed_seq.substring( 0, processed_seq.length() - 4 );
            processed_seq = temp;
          }
          break;
        case HD_SIMPLE_5P://strip 3nt from 5p end of processed sequence
          if ( processed_seq.length() <= 4 )
          {
            
            processed_seq = "";
          }
          else
          {
            temp = processed_seq.substring( 4, processed_seq.length() );
            processed_seq = temp;
          }
          break;
        case HD_NONE://same as default do nothing
          break;
        default://do nothing
          break;
      }

      if(processed_seq.isEmpty())//length failed somewhere
      {
        String original_seq = me.getValue().getSequence();
        // Pass back the original sequence for the discarded seq map
        discarded.put( original_seq + "_" + abundance, "HD Size fail" );
      }
      else if ( processed.containsKey( processed_seq ) )
      {
        String orig_seq = processed.get( processed_seq ).getSequence();
        int old_abd = processed.get( processed_seq ).getAbundance();
        FastaRecord fr = new FastaRecord( orig_seq, old_abd + abundance );
        processed.put( processed_seq, fr );
      }
      else
      {
        processed.put( processed_seq, me.getValue() );
      }

    }

    if ( params.getHD_Setting() != HD_NONE )
    {
      String long_stage_name = "Sequences remaining after HD Adapter Stripping (" + this.params.getMinLength() + "-" + this.params.getMaxLength() + "):";
      this.results.add( new FilterStage( long_stage_name, "HD Filtering", processed.getTotalSeqCount(), processed.getDistinctSeqCount() ) );

    }
    trackerReset();
    
    return processed;

  }
  private FastaMap filterByLength( FastaMap input, Map<String, String> discarded ) throws IOException
  {
    FastaMap processed = new FastaMap();

    trackerInitKnownRuntime( "Filtering sequences after adaptor removal by length", input.size() );

    // Run through each distinct read
    for ( Map.Entry<String, FastaRecord> me : input.entrySet() )
    {
      continueRun();

      trackerIncrement();

      // Get the details for this read.
      String seq = me.getKey();
      int abundance = me.getValue().getAbundance();

      // Filter all remaining sequences based on their length, only keep sequences 
      // within user defined tolerances, discard all others.
      if ( validLength( seq ) )
      {
        updateLengthDistribution( seq, abundance );

        processed.put( seq, me.getValue() );
      }
      else
      {
        String original_seq = me.getValue().getSequence();

        // Pass back the original sequence for the discarded seq map
        discarded.put( original_seq + "_" + abundance, "Invalid length (after adaptor removal) [Len:" + seq.length() + "; Processed Seq.: " + seq + "(" + abundance + ")]" );
      }
    }

    String long_stage_name = "Sequences remaining after length range filtering (" + this.params.getMinLength() + "-" + this.params.getMaxLength() + "):";
    this.results.add( new FilterStage( long_stage_name, "Length Filtering", processed.getTotalSeqCount(), processed.getDistinctSeqCount() ) );

    trackerReset();

    return processed;
  }

  private FastaMap compileResults( FastaMap input ) throws IOException
  {
    FastaMap processed = new FastaMap();

    trackerInitKnownRuntime( "Compiling results", input.size() );

    // Run through each distinct read
    for ( Map.Entry<String, FastaRecord> me : input.entrySet() )
    {
      continueRun();

      trackerIncrement();

      // Get the details for this read.
      String seq = me.getKey();
      int abundance = me.getValue().getAbundance();

      FastaRecord fr = new FastaRecord( seq, abundance );

      processed.put( seq, fr );
    }

    trackerReset();

    return processed;
  }

  private FastaMap convertDiscarded( Map<String, String> discarded ) throws Exception
  {
    FastaMap map = new FastaMap();

    for ( Map.Entry<String, String> me : discarded.entrySet() )
    {
      String[] parts = me.getKey().split( "_" );

      if ( parts.length != 2 )
      {
        throw new IllegalArgumentException( "Incorrect discarded sequence format." );
      }

      String orig_seq = parts[0];
      int abd = Integer.parseInt( parts[1] );

      FastaRecord fr = new FastaRecord( me.getValue(), "", orig_seq );
      fr.setAbundance( abd );

      map.put( orig_seq, fr );
    }

    return map;
  }

  /**
   * Checks whether the given sequence is of a valid length
   * @param seq
   * @return true if the length of the sequence is within user specified boundaries,
   * otherwise false
   */
  private boolean validLength( String seq )
  {
    return ( seq.length() >= params.getMinLength() && seq.length() <= params.getMaxLength() );
  }

  /**
   * Either creates a new entry in sequence distribution map for the given sequence
   * or increments an existing entry
   * @param seq The sequence with adaptors removed
   * @param abundance The number of times the sequence was found in the file
   */
  private void updateLengthDistribution( String seq, int abundance )
  {
    FilterStage len_count = nr_seq_lengths.get( seq.length() );
    if ( len_count == null )
    {
      len_count = new FilterStage( "", abundance, 1 );
    }
    else
    {
      len_count.incCounters( abundance );
    }
    nr_seq_lengths.put( seq.length(), len_count );
  }

  /**
   * Writes out execution statistics, including sequence length distribution statistics
   * @param results_file The file to write to
   * @param seq_lengths The sequence length distribution statistics
   * @param results The execution statistics
   * @throws IOException Thrown if there is problem writing to results_file
   */
  protected void generateResultsFile( File results_file, Map<Integer, FilterStage> seq_lengths,
                                    List<FilterStage> results ) throws IOException
  {
    // Write out results to file
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( results_file ) ) );

    for ( FilterStage property : results )
    {
      out.print( property.getStageName() + "[total, distinct]: " + property.getTotalReadCount() + " " + property.getDistinctReadCount() + "\n" );
    }

    out.print( "\nsRNA length distribution (for remaining sequences):\n" );
    out.print( "[length (nt), total reads, distinct reads]\n" );

    for ( Integer size : new TreeSet<Integer>( seq_lengths.keySet() ) )
    {
      out.print( size + "\t" + seq_lengths.get( size ).getTotalReadCount() + "\t" + seq_lengths.get( size ).getDistinctReadCount() + "\n" );
    }

    out.flush();
    out.close();
    
    if(AppUtils.INSTANCE.isBaseSpace())
    {
      generateBaseSpaceResultFiles(results_file, seq_lengths, results);
    }
  }

  
  /**
   * Writes out execution statistics, including sequence length distribution statistics
   * @param results_file The file to write to
   * @param seq_lengths The sequence length distribution statistics
   * @param results The execution statistics
   * @throws IOException Thrown if there is problem writing to results_file
   */
  protected void generateBaseSpaceResultFiles( File results_file, Map<Integer, FilterStage> seq_lengths,
                                    List<FilterStage> results ) throws IOException
  { 
    
    String outputLocation = FilenameUtils.removeExtension( results_file.getAbsolutePath() );
    StringBuilder content = new StringBuilder();
    //Write the table header 
    try
    {
      //outputPath = Files.createFile( Paths.get( outputLocation + "_header.csv" ) );
      //id="t1", format="table", rows="4", columns="3", title="Overview"
      String title = results.get( 0).getStageName().split( "[(]")[1];
      String titleStripped = title.replaceAll( "[):]", "");
      content.append( "id=\"t1\",")
        .append( "format=\"table\",")
        .append( "rows=\"" + results.size() + "\",")
        .append( "columns=\"3\",")
        .append( "title=\""+titleStripped+"\"")
        .append( LINE_SEPARATOR)
        .append("Number of Sequences Remaining, Total (Redundant), Distinct (Non-Redundant)")
        .append(LINE_SEPARATOR)
        .append( "Total: ,")
        .append(results.get( 0).getTotalReadCount() + ",")
        .append(results.get( 0).getDistinctReadCount())
        .append(LINE_SEPARATOR);
 
      //for ( FilterStage property : results )
      for(int i = 1; i < results.size(); i++)
      {
        content.append( results.get( i ).getStageName() )
          .append( "," )
          .append( results.get(i).getTotalReadCount() )
          .append( "," )
          .append( results.get(i).getDistinctReadCount() )
          .append(  LINE_SEPARATOR);
      }
 
      Files.write( FileSystems.getDefault().getPath( outputLocation + "_header.csv"), 
                         content.toString().getBytes(), 
                         StandardOpenOption.CREATE);
    
    }
    catch ( IOException ex )
    {
     
      LOGGER.log( Level.INFO, "{0}" + "Header file creation failed", ex.getMessage() );

    }
    
    content.setLength( 0 );
    //Write the table main information 
    try
    {
      content.append( "id=\"t2\",")
        .append( "format=\"table\",")
        .append( "rows=\"" + seq_lengths.size() + "\",")
        .append( "columns=\"3\",")
        .append( "title=\"sRNA length distribution (for remaining sequences)\"")
        .append( LINE_SEPARATOR)
        .append("Length (nt), Total reads, Distinct reads")
        .append( LINE_SEPARATOR);
      
      for ( Integer size : new TreeSet<>( seq_lengths.keySet() ) )
      {
        content.append( size )
          .append( "," )
          .append( seq_lengths.get( size ).getTotalReadCount() )
          .append( "," )
          .append( seq_lengths.get( size ).getDistinctReadCount() )
          .append(LINE_SEPARATOR);
      }
       Files.write( FileSystems.getDefault().getPath( outputLocation + "_main.csv"), 
                         content.toString().getBytes(), 
                         StandardOpenOption.CREATE);
    }
    catch ( IOException ex )
    {
     
      LOGGER.log( Level.INFO, "{0}" + "Main file creation failed ", ex.getMessage() );

    }
    
    content.setLength( 0 );
    //generate the size class distribution
    try
    {
      //id="g1", format="bargraph", title="Sequence Abundance Distribution", x-axis="Class Size", y-axis="Abundance"
      content.append( "id=\"g1\"," )
        .append( "format=\"bargraph\"," )
        .append( "title=\"Final Redundant Sequence Abundance Distribution\"," )
        .append( "x-axis=\"Class Size\", y-axis=\"Abundance\"" )
        .append( LINE_SEPARATOR );
      for ( int size = 0; size < 52; size++ )
      {
        content
          .append( seq_lengths.get( size ) != null ? seq_lengths.get( size ).getTotalReadCount() : 0 )
          .append( LINE_SEPARATOR );
      }
      Files.write( FileSystems.getDefault().getPath( outputLocation + "_SCD_R.csv" ),
        content.toString().getBytes(),
        StandardOpenOption.CREATE );

      content.setLength( 0 );
      content.append( "id=\"g1\"," )
        .append( "format=\"bargraph\"," )
        .append( "title=\"Final Non-Redundant Sequence Abundance Distribution\"," )
        .append( "x-axis=\"Class Size\", y-axis=\"Abundance\"" )
        .append( LINE_SEPARATOR );
      for ( int size = 0; size < 52; size++ )
      {
        content
          .append( seq_lengths.get( size ) != null ? seq_lengths.get( size ).getDistinctReadCount() : 0 )
          .append( LINE_SEPARATOR );
      }
      Files.write( FileSystems.getDefault().getPath( outputLocation + "_SCD_NR.csv" ),
        content.toString().getBytes(),
        StandardOpenOption.CREATE );

       
    }
    catch ( IOException ex )
    {
      
      LOGGER.log( Level.INFO, "{0}" + "Final bar chart file creation failed ", ex.getMessage() );

    }

//      // Write out results to file
//      PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( results_file ) ) );
//    
//      for ( FilterStage property : results )
//      {
//        out.print( property.getStageName() + "[total, distinct]: " + property.getTotalReadCount() + " " + property.getDistinctReadCount() + "\n" );
//      }
//      
//      out.print( "\nsRNA length distribution (for remaining sequences):\n" );
//      out.print( "[length (nt), total reads, distinct reads]\n" );
//      
//      for ( Integer size : new TreeSet<>( seq_lengths.keySet() ) )
//      {
//        out.print( size + "\t" + seq_lengths.get( size ).getTotalReadCount() + "\t" + seq_lengths.get( size ).getDistinctReadCount() + "\n" );
//      }
//      
//      out.flush();
    
  }

  public boolean getCompleted()
  {
    return completed;
  }

  public HD_Protocol getHDProt()
  {
    return this.params.getHD_Setting();
  }

  FastaMap getProcessedSeqs_PRE_HD()
  {
    throw new UnsupportedOperationException( "Not yet implemented" );
  }

  protected enum AdaptorType
  {
    THREE_PRIME( "3'" ),
    FIVE_PRIME( "5'" );
    private String short_string;

    private AdaptorType( String short_string )
    {
      this.short_string = short_string;
    }

    public String toShortString()
    {
      return this.short_string;
    }
  }
}
