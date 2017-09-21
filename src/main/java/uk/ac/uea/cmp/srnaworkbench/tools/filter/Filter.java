/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.filter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanParams;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanReader;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanRunner;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.viewers.AbundanceDistributionViewer;

/**
 * Engine for the filter tool. Removes unwanted sequences from a FastA file.
 *
 * @author Dan Mapleson and Matthew Stocks
 * -tool filter -srna_file dist/GSM118373_Rajagopalan_col0_leaf_nr.fa -out_file dist/testFilter2.fa -params dist/default_filter_params.cfg
 */
public class Filter extends RunnableTool
{
  private final static String TOOL_NAME = "FILTER";
  private FilterParams params;
  private File infile;
  private File outfile;
  private File tempdir;
  private FilterGroup stages;
  private FastaMap data;
  private boolean loadfile;
  private PrintWriter discard_writer;
  private static final int MAX_ABD_UPPER = FilterParams.Definition.MAXIMUM_ABUNDANCE.getUpperLimit( Integer.class );
  private boolean completed = false;
  private int showingAbundancePlots = JOptionPane.NO_OPTION;

  public Filter() 
  {
    this( null, null, Tools.getNextDirectory(), new FilterParams() );
  }

  public Filter( File infile, File outfile, File tempdir, FilterParams params )
  {
    this( infile, outfile, tempdir, params, null, null, true, JOptionPane.NO_OPTION);
  }

  public Filter( FastaMap data_in, File tempdir, FilterParams params )
  {
    this( data_in, tempdir, params, null );
  }

  public Filter( File infile, File outfile, File tempdir, FilterParams params, StatusTracker tracker, int generating_plots )
  {
    this( infile, outfile, tempdir, params, tracker, null, true, generating_plots );
  }
  
  public Filter( File infile, File outfile, File tempdir, FilterParams params, StatusTracker tracker)
  {
    this( infile, outfile, tempdir, params, tracker, null, true, JOptionPane.NO_OPTION );
  }

  public Filter( FastaMap data_in, File tempdir, FilterParams params, StatusTracker tracker )
  {
    this( null, null, tempdir, params, tracker, data_in, false, JOptionPane.NO_OPTION );
  }

  private Filter( File infile, File outfile, File tempdir, FilterParams params, StatusTracker tracker, FastaMap data_in, boolean loadfile, int generating_plots )
  {
    super( TOOL_NAME, tracker );

    this.infile = infile;
    this.outfile = outfile;
    this.tempdir = tempdir;
    this.params = params;

    this.data = data_in;
    this.stages = new FilterGroup();
    this.loadfile = loadfile;
    this.discard_writer = null;
    this.showingAbundancePlots = generating_plots;
    
    Tools.trackPage( "Filter Main Procedure Class Loaded");
  }

  /**
   * Get filter statistics describing reads counts at each stage of the filter
   * pipeline
   *
   * @return Filter statistics
   */
  public FilterGroup getStats()
  {
    return this.stages;
  }

  /**
   * Retrieve filtered data in non-redundant form
   *
   * @return FastaMap containing filtered reads
   */
  public FastaMap getData()
  {
    return this.data;
  }

  /**
   * Clears any present results from previous runs
   */
  public void clear()
  {
    stages = null;
    data = null;
  }

  /**
   * Processes the unfiltered reads using the specified parameters. Produces a
   * filtered map of reads, which can be accessed later using "getData()".
   * Filter statistics for the run can be accessed using "getStats().
   * @throws java.lang.Exception
   */
  @Override
  protected void process() throws Exception
  {
    completed = false;
    if ( params.getAddDiscardLog())
    {
      this.discard_writer = new PrintWriter( new BufferedWriter( new FileWriter( params.getDiscardLog() ) ), true );
    }

    continueRun();

    // If we need to gather data from the input file, do that, otherwise use data provided
    if ( loadfile )
    {
      trackerInitUnknownRuntime( "Reading sRNAs from file" );

      // Compress input file into non-redundant format in memory using a hashmap
      SRNAFastaReader fr = new SRNAFastaReader( infile );
      data = new FastaMap( fr.process() );

      stages.add( new FilterStage( "input", fr.getTotalReadCount(), fr.getDistinctReadCount() ) );
      
      generatePlot("Original Data Statistics");

      trackerReset();
    }
    else
    {
      stages.add( new FilterStage( "input", data.getTotalSeqCount(), data.getDistinctSeqCount() ) );
    }

    continueRun();

    // Filter by sequence length (always do this)
    stages.add( filterSeqByLength( data ) );

    continueRun();

    // If requested filter out low complexity sequences
    if ( params.getFilterLowComplexitySeq() )
    {
      stages.add( filterSeqByComplexity( data ) );
      generatePlot("Low Complexity Filter");
    }

    continueRun();

    // If params make sense filter by abundance
    if ( params.getMinAbundance() >= 1 || params.getMaxAbundance() <= MAX_ABD_UPPER )
    {
      if ( params.getFilterByNormalisedAbundance() )
      {
        stages.add( filterSeqByNormalisedAbundance( data ) );
        generatePlot("Normalised Abundance Filter");
      }
      else
      {
        stages.add( filterSeqByAbundance( data ) );
        generatePlot("Raw Abundance Filter");
      }
      
    }

    continueRun();

    // If requested filter out sequences containing unknown nucleotides
    if ( params.getFilterInvalidSeq() )
    {
      stages.add( filterSeqByValidity( data ) );
      generatePlot("Invalid Sequence Filter");
    }

    continueRun();

    // Dump the filtered data to disk unless this is the last step
    File filtStep1 = new File( tempdir.getPath() + DIR_SEPARATOR + "filter.seqprop.out" );
    File filtStep2 = filtStep1;
    File filtStep3 = filtStep1;
    if ( params.getDiscardTRRNA() || params.getFilterGenomeHits() || params.getFilterKillList() )
    {
      trackerInitUnknownRuntime( "Writing out temporary data to disk for patman" );

      new SRNAFastaWriter( filtStep1 ).process( data, true );

      trackerReset();
    }

    continueRun();

    // If required, filter out all sequences matching those in the killlist
    if ( params.getFilterKillList() )
    {
      filterWithPatman( filtStep1, params.getKillList(),
        new File( tempdir.getPath() + DIR_SEPARATOR + "filter.kill-list.out.patman" ),
        true, false,
        "filter by kill-list (matches out)", "sequences found in Kill-list" );
      generatePlot("Kill List Filter");

      // If this isn't the last filter step output the data to file
      if ( params.getDiscardTRRNA() )
      {
        filtStep2 = new File( tempdir.getPath() + DIR_SEPARATOR + "filter.kill-list.out" );
        new SRNAFastaWriter( filtStep2 ).process( data, true );
        
      }
    }

    continueRun();

    // If required, filter out transfer and ribosomal RNAs
    if ( params.getDiscardTRRNA() )
    {
      filterWithPatman( filtStep2, Tools.TRRNA_FILE,
        new File( tempdir.getPath() + DIR_SEPARATOR + "filter.trrna.out.patman" ),
        false, false,
        "filter by t/rRNA (matches out)", "known transfer and ribosomal RNAs" );
      
      generatePlot("T/R RNA Filter");

      // If this isn't the last filter step output the data to file
      if ( params.getGenome() != null )
      {
        filtStep3 = new File( tempdir.getPath() + DIR_SEPARATOR + "filter.trrna.out" );
        new SRNAFastaWriter( filtStep3 ).process( data, true );
      }
    }

    continueRun();

    // If required, filter out all sequences not found in the given genome
    if ( params.getFilterGenomeHits() )
    {
      filterWithPatman( filtStep3, params.getGenome(),
        new File( tempdir.getPath() + DIR_SEPARATOR + "filter.genome.out.patman" ),
        false, true,
        "filter by genome (matches in)", "sequences not found in genome" );
      
      generatePlot("Genome Filter");
    }

    System.gc();

    deleteFile( filtStep1 );
    deleteFile( filtStep2 );
    deleteFile( filtStep3 );

    continueRun();

    // If outfile is specified then dump results and filtered file to disk
    if ( outfile != null )
    {
      trackerInitUnknownRuntime( "Outputting filtered sequences to disk" );

      
      //not as neat here as it previously was. However, the new options for outputting both types of file
      //if desired mean this is a simple solution for now
      //in the future we will want some way of determining if the data is already in redundant format...
      
      if(params.getOutputNonRedundant())
      {
        // Write filtered data to the output fasta file
        
        new SRNAFastaWriter( outfile ).process( data, true );
      }
      
      if(params.getOutputRedundant())
      {
        String redundantOutFile = FilenameUtils.removeExtension( outfile.getAbsolutePath() ) + "_R.fa";
        new SRNAFastaWriter( new File(redundantOutFile) ).process( data, false );
      }

      // Output the filtering results spreadsheet
      generateTable( new File( outfile.getPath() + "_overview.csv" ), stages );

      trackerReset();
    }

    if ( discard_writer != null )
    {
      this.discard_writer.flush();
      this.discard_writer.close();
    }
    
    completed = true;
  }

  private void generatePlot(String current_stage)
  {
    if ( showingAbundancePlots == JOptionPane.YES_OPTION)
    {
      AbundanceDistributionViewer myAbundDist = AbundanceDistributionViewer.generateAbundanceDistribution();
      myAbundDist.setTitle( "Size Class Distribution : Filter Stage" + current_stage);
      myAbundDist.inputData( data, infile.getName() );
      myAbundDist.initiliseSizeClassDistribution();
      myAbundDist.revalidate();
    }
  }
  private void deleteFile( File f )
  {
    if ( f != null && f.exists() && !f.delete() )
    {
      LOGGER.log( Level.WARNING, "Filter Warning: Failed to delete: {0}", f.getPath());
    }
  }

  /**
   * Generate csv table file with overview of filtering steps
   *
   * @param overview_file
   * @param filter_stages
   * @throws IOException
   */
  private void generateTable( File overview_file, FilterGroup filter_stages ) throws IOException
  {
    // Write out srna file to disk for consumption by patman
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( overview_file ) ) );

    out.print( "\"Overview of filtering results for " + infile.getPath() + "\"\n" );
    out.print( "\"Removed sequences: " + params.generateFiltersString() + "\"\n" );

    out.print( filter_stages.toString() );

    out.flush();
    out.close();
  }

  /**
   * Writes any present filtered results to the specified file
   *
   * @param out_file File to write filtered reads to
   * @throws IOException Thrown is there were problems writing to the file
   */
  public void writeDataToFasta( File out_file ) throws IOException
  {
    new SRNAFastaWriter( out_file ).process( data, params.getOutputNonRedundant() );
  }

  private void filterWithPatman( File patterns_in, File databases_in, File patman_out, boolean sense_only, boolean keep_matches,
                                 String filter_stage_message, String status_message ) throws Exception
  {
    trackerInitUnknownRuntime( "Running patman to identify " + status_message );

    // TODO Leighton's exact matcher tool might be able to work in this instance as we are not interested in
    // mismatches or gaps.
    // Run patman
    PatmanParams pp = new PatmanParams.Builder().setPositiveStrandOnly( sense_only ).build();

    PatmanRunner pr = new PatmanRunner( patterns_in, databases_in, patman_out, tempdir, pp );
    pr.run();
    if ( pr.failed() )
    {
      throw new WorkbenchException( pr.getErrorMessage() );
    }

    // Get the match hits from patman
    Patman matches = new PatmanReader( patman_out ).process();

    continueRun();

    trackerReset();

    // Filter out sequences not matching patman hits
    stages.add( filterMatches( matches, keep_matches, filter_stage_message, "Discarding " + status_message ) );

    continueRun();

    // Cleanup
    trackerInitUnknownRuntime( "Deleting patman file containing " + status_message );

    deleteFile( patman_out );

    trackerReset();
  }

  private FilterStage filterMatches( Patman matches, boolean keep_matches, String name, String tracker_message ) throws Exception
  {
    data = matches.filterByMatch( data, keep_matches, false, discard_writer, name, this.getTracker(), tracker_message );

    return new FilterStage( name, data.getTotalSeqCount(), data.getDistinctSeqCount() );
  }

  private FilterStage filterSeqByLength( FastaMap data )
  {
    ArrayList<String> itemsToDiscard = new ArrayList<String>();

    int invalid_size = 0;

    trackerInitKnownRuntime( "Filtering Sequences By Length", data.size() );

    // Cycle through each FASTA header
    for ( String seq : data.keySet() )
    {
      int seqLen = seq.length();

      if ( seqLen < params.getMinLength() || seqLen > params.getMaxLength() )
      {
        // invalid sequence... either too long or too short
        invalid_size++;
        itemsToDiscard.add( seq );
        continue;
      }

      trackerIncrement();
    }

    for ( String seq : itemsToDiscard )
    {
      if ( discard_writer != null )
      {
        discard_writer.print( seq + "(" + data.get( seq ) + ") : invalid length\n" );
      }
      data.remove( seq );
    }

    trackerReset();

    return new FilterStage( "filter by sequence length", data.getTotalSeqCount(), data.getDistinctSeqCount() );
  }
  private FilterStage filterSeqByComplexity( FastaMap data )
  {
    ArrayList<String> itemsToDiscard = new ArrayList<>();

    int invalid_low_comp = 0;

    trackerInitKnownRuntime( "Discarding low complexity sequences", data.size() );

    // Cycle through each FASTA header
    for ( String seq : data.keySet() )
    {
      if ( params.getFilterLowComplexitySeq() && isLowComplexity( seq ) )
      {
        // invalid sequence... not complex enough
        invalid_low_comp++;
        itemsToDiscard.add( seq );
        continue;
      }

      trackerIncrement();
    }

    for ( String seq : itemsToDiscard )
    {
      if ( discard_writer != null )
      {
        discard_writer.print( seq + "(" + data.get( seq ) + ") : low complexity\n" );
      }
      data.remove( seq );
    }

    trackerReset();

    return new FilterStage( "filter low-complexity sequences", data.getTotalSeqCount(), data.getDistinctSeqCount() );
  }

  private FilterStage filterSeqByValidity( FastaMap data )
  {
    ArrayList<String> itemsToDiscard = new ArrayList<String>();

    int invalid_seq = 0;

    trackerInitKnownRuntime( "Discarding invalid sequences {A,T,G,C}", data.size() );

    // Cycle through each FASTA header
    for ( String seq : data.keySet() )
    {
      if ( params.getFilterInvalidSeq() && isInvalid( seq ) )
      {
        // invalid sequence... contains Ns
        invalid_seq++;
        itemsToDiscard.add( seq );
        continue;
      }

      trackerIncrement();
    }

    for ( String seq : itemsToDiscard )
    {
      if ( discard_writer != null )
      {
        discard_writer.print( seq + "(" + data.get( seq ) + ") : invalid nucleotides\n" );
      }
      data.remove( seq );
    }

    trackerReset();

    return new FilterStage( "filter invalid sequences", data.getTotalSeqCount(), data.getDistinctSeqCount() );
  }

  private FilterStage filterSeqByAbundance( FastaMap data )
  {
    ArrayList<String> itemsToDiscard = new ArrayList<String>();

    int invalid_abundance = 0;

    trackerInitKnownRuntime( "Filtering Sequences By Abundance", data.size() );

    // Cycle through each FASTA header
    for ( String seq : data.keySet() )
    {
      int abundance = data.get( seq ).getAbundance();

      if ( abundance < params.getMinAbundance() || abundance > params.getMaxAbundance() )
      {
        // invalid sequence... not enough or too many hits
        invalid_abundance++;
        itemsToDiscard.add( seq );
        continue;
      }

      trackerIncrement();
    }

    for ( String seq : itemsToDiscard )
    {
      if ( discard_writer != null )
      {
        discard_writer.print( seq + "(" + data.get( seq ) + ") : invalid abundance\n" );
      }
      data.remove( seq );
    }

    trackerReset();

    boolean max_abd_enabled = params.getMaxAbundance() <= MAX_ABD_UPPER;

    String min_hits = params.getMinAbundance() >= 1 ? "min-abundance" : "";
    String and = ( params.getMinAbundance() >= 1 && max_abd_enabled ) ? " and " : "";
    String max_hits = max_abd_enabled ? "max-abundance" : "";

    return new FilterStage( "filter by " + min_hits + and + max_hits, data.getTotalSeqCount(), data.getDistinctSeqCount() );
  }

  private FilterStage filterSeqByNormalisedAbundance( FastaMap data )
  {
    ArrayList<String> itemsToDiscard = new ArrayList<String>();

    int invalid_abundance = 0;

    trackerInitKnownRuntime( "Filtering Sequences By Abundance (reads per million)", data.size() );

    int total_reads = data.getTotalSeqCount();

    // Cycle through each FASTA header
    for ( String seq : data.keySet() )
    {
      int abundance = data.get( seq ).getAbundance();
      
      double norm_abundance = ( (double) abundance / (double) total_reads ) * 1.0e6;

      if(params.getNormaliseAbundance())
      {
        data.get( seq ).setAbundance( norm_abundance );
      }
      if ( norm_abundance < params.getMinAbundance() || norm_abundance > params.getMaxAbundance() )
      {
        // invalid sequence... not enough hits
        invalid_abundance++;
        itemsToDiscard.add( seq );
        continue;
      }

      trackerIncrement();
    }

    for ( String seq : itemsToDiscard )
    {
      if ( discard_writer != null )
      {
        discard_writer.print( seq + "(" + data.get( seq ) + ") : invalid abundance\n" );
      }
      data.remove( seq );
    }

    trackerReset();

    boolean max_abd_enabled = params.getMaxAbundance() <= MAX_ABD_UPPER;

    String min_hits = params.getMinAbundance() >= 1 ? "min-abundance (" + params.getMinAbundance() + ")" : "";
    String and = ( params.getMinAbundance() >= 1 && max_abd_enabled ) ? " and " : "";
    String max_hits = max_abd_enabled ? "max-abundance (" + params.getMaxAbundance() + ")" : "";

    return new FilterStage( "filter by " + min_hits + and + max_hits + "(rpm)", total_reads, data.getDistinctSeqCount() );
  }

  /**
   * Checks to see if the given sequence is considered "low complexity", i.e.
   * contains less than 3 distinct nucleotides
   *
   * @param seq Sequence to test
   * @return true if sequence is low complexity, otherwise false
   */
  private boolean isLowComplexity( String seq )
  {
    HashMap<Character, Integer> h = new HashMap<Character, Integer>();

    for ( int i = 0; i < seq.length(); i++ )
    {
      h.put( seq.charAt( i ), 1 );
    }

    return h.size() < 3 ? true : false;
  }

  /**
   * Checks to see if the given sequence is considered "invalid", i.e. all
   * nucleotides must be one of A,T,G,C
   *
   * @param seq Sequence to test
   * @return true if sequence is invalid, otherwise false
   */
  private boolean isInvalid( String seq )
  {
    for ( int i = 0; i < seq.length(); i++ )
    {
      char c = seq.charAt( i );
      if ( c != 'A' && c != 'C' && c != 'G' && c != 'T' )
      {
        return true;
      }
    }

    return false;
  }

  boolean getCompleted()
  {
    return completed;
  }
}
