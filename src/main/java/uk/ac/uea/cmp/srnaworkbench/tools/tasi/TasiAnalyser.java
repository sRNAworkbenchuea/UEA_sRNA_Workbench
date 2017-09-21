/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;

/**
 * Engine for the Tasi prediction tool.  Takes a srna file and a genome as input
 * and detected phased 21nt sRNAs indicative of TAS loci.  Uses a modified version
 * of the algorithm described by Chen et al.
 * @author Dan Mapleson
 */
public final class TasiAnalyser extends RunnableTool
{
  private final static String TOOL_NAME = "TASI";
  private File in_file;
  private File out_file;
  private File temp_dir;
  private TasiParams params;
  private Map<String, PatmanEntry> hits;
  private TASMap potential_tas;
  private TASMap actual_tas;
  private List<Locus> loci;
  private Patman genome_hits;

  public TasiAnalyser() throws Exception
  {
    this( null, null, Tools.getNextDirectory(), new TasiParams() );
  }

  public TasiAnalyser( File infile, File outfile, File tempdir, TasiParams params )
  {
    this( infile, outfile, tempdir, params, null );
  }

  public TasiAnalyser( File infile, File tempdir, TasiParams params, StatusTracker tracker )
  {
    this( infile, null, tempdir, params, tracker );
  }

  public TasiAnalyser( File infile, File outfile, File tempdir, TasiParams params, StatusTracker tracker )
  {
    super( TOOL_NAME, tracker );

    if ( infile == null || !infile.exists() )
    {
      throw new IllegalArgumentException( "TASI Analyser Error: sRNA file not provided or doesn't exist." );
    }

    this.in_file = infile;
    this.out_file = outfile;
    this.temp_dir = tempdir;
    this.params = params;

    this.hits = null;
    this.potential_tas = new TASMap();
    this.actual_tas = new TASMap();
    this.loci = new ArrayList<Locus>();
    this.genome_hits = null;
    
    Tools.trackPage( "TASI Main Procedure Class Loaded");
  }

  /**
   * Gets the TASMap of the final results
   * @return Actual TAS results
   */
  public List<Locus> getResults()
  {
    return this.loci;
  }

  /**
   * Gets the currently specified parameters
   * @return Current tool parameters
   */
  public TasiParams getParams()
  {
    return this.params;
  }

  @Override
  protected void process() throws Exception
  {
    // We filter out seqs not 21 in length, low complexity and invalid sequences in order to avoid useless hits.
    FilterParams filt_params = new FilterParams.Builder()
      .setMinLength( params.getPhasingRegister() )
      .setMaxLength( params.getPhasingRegister() )
      .setMinAbundance( params.getMinAbundance() )
      .setFilterLowComplexitySeq( true )
      .setFilterInvalidSeq( true )
      .setOutputNonRedundant( true )
      .build();

    File filtered_file = new File( this.temp_dir.getPath() + DIR_SEPARATOR + this.in_file.getName() + "_filtered.fa" );

    Filter filter = new Filter( this.in_file, filtered_file, temp_dir, filt_params, this.getTracker(), JOptionPane.NO_OPTION );
    filter.run();

    if ( filter.failed() )
    {
      throw new Exception( filter.getErrorMessage() );
    }

    continueRun();

    String temp_file_prefix = this.temp_dir.getPath() + DIR_SEPARATOR + this.in_file.getName();
    File genome_match_file = new File( temp_file_prefix + "_genome_matches.patman" );

    // Run patman with default values for sRNA and genome file.
    trackerInitUnknownRuntime( "Matching sRNAs to genome with patman" );
    new PatmanRunner( filtered_file, params.getGenome(), genome_match_file, temp_dir, new PatmanParams() ).run();
    trackerReset();

    continueRun();

    // Read in only those hits that have a sequence of length 21 and have abundance >= user defined param.
    trackerInitUnknownRuntime( "Reading genome hits" );
    genome_hits = new PatmanReader( genome_match_file ).process();
    trackerReset();

    continueRun();

    trackerInitUnknownRuntime( "Sorting filtered genome hits" );
    genome_hits.sortByPosition();
    hits = genome_hits.buildPositionKeyedMap();
    trackerReset();

    continueRun();

    trackerInitUnknownRuntime( "Calculating results" );
    calcResults();
    trackerReset();

    continueRun();

    trackerInitUnknownRuntime( "Compiling results" );
    compileResults();
    trackerReset();

    continueRun();

    if ( out_file != null )
    {
      writeLoci( new File( out_file.getPath() + "_locuslist.csv" ) );
      writeSrnas( new File( out_file.getPath() + "_srnas.txt" ) );
    }
  }

  public void writeLoci( File results_file ) throws IOException
  {
    // Write out srna file to disk for consumption by patman
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( results_file ) ) );

    out.print( "Using cutoff p-value of " + params.getPValThreshold() + "\n\n" );
    out.print( "Chromosome,Start postition,End position,# sequences,# phased sequences,p-value\n" );

    if ( loci.isEmpty() )
    {
      out.print( "No results found at using this cutoff\n" );
    }
    else
    {
      for ( Locus locus : loci )
      {
        out.print( locus );
        out.print( "\n" );
      }
    }

    out.flush();
    out.close();
  }

  public void writeSrnas( File srna_file ) throws IOException
  {
    // Write out srna file to disk for consumption by patman
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( srna_file ) ) );

    out.print( "List of phased sRNAs in each of the predicted loci\n\n" );

    if ( actual_tas.isEmpty() )
    {
      out.print( "No results found at using this cutoff\n" );
    }
    else
    {
      for ( String locus : new TreeSet<String>( actual_tas.keySet() ) )
      {
        out.print( locus + "\n\n" );

        Patman sg = actual_tas.get( locus );

        for ( PatmanEntry pe : sg )
        {
          out.print( pe.getSequence() + "(" + pe.getAbundance() + ")  " + pe.getPosition() + "\n" );
        }
        out.print( "\n" );
      }

    }

    out.flush();
    out.close();
  }

  private void compileResults()
  {
    for ( Locus l : this.loci )
    {
      Patman phased_srnas = actual_tas.get( l.toString() );

      l.setPhasedSrnas( phased_srnas );

      if ( this.genome_hits != null )
      // The minus 2 offset is include hits at the beginning of the negative strand
      {
        l.setAllSrnas( this.genome_hits.getRange( l.getChromosome(), l.getStart() - 2, l.getEnd() ) );
      }
    }
  }

  private void calcResults()
  {
    Locus real_locus = null;
    Locus last_locus = null;
    Locus current_locus = null;

    String seq_ref = null;
    double min_p = 100.0;

    for ( String loc : new TreeSet<String>( hits.keySet() ) )
    {
      PatmanEntry pe = hits.get( loc );

      // Find srnas in region and phased srnas in region
      int nb_srnas = findInRegion( pe, false );
      int nb_srnas_phased = findInRegion( pe, true );

      // Calculate the p_value using the 2 counts
      double p_value = calculatePValue( nb_srnas, nb_srnas_phased );

      // Just set the current_locus here (useful for debugging)
      current_locus = new Locus( pe.getLongReadHeader(), pe.getStart(), pe.getSequenceStrand(), nb_srnas, nb_srnas_phased, p_value, params.getPhasingRegister() );

//            if (p_value < 0.99 || nb_phased_seqs > 0 || nb_seqs > 80)
//                System.out.println(current_locus);


      // Only interested in hits that score better than (less than or equal to) the user defined threshold.
      if ( p_value <= params.getPValThreshold() )
      {
        int start_diff = Math.abs( pe.getStart() - ( last_locus == null ? 0 : last_locus.getStart() ) );
        boolean same_chrom = last_locus == null ? true : pe.getLongReadHeader().equals( last_locus.getChromosome() );

        // Check if this hit is in a new region or not
        if ( !same_chrom || start_diff > TasiParams.REGION_SIZE + params.getPhasingRegister() - 1 )
        {
          // This hit is in a new region, so save the last one for output (if it exists).
          if ( real_locus != null )
          {
            addLocus( real_locus, seq_ref );
          }

          // Only interested if there are more than 2 distinct sRNAs in this region, otherwise start looking at next region.
          if ( nb_srnas > 2 )
          {
            // Save the locus and position in case we don't find anything better in the region.
            real_locus = current_locus;
            seq_ref = pe.getPosition();
          }
          else
          {
            // This hit doesn't look genuine so don't start a new locus
            real_locus = null;
          }

          // Save the p_value (it's the first and therefore the best so far in this
          // region.
          min_p = p_value;
        }
        else
        {
          // This hit is in the same region as the last one
          // Check if this has a lower (better) p_value and has at least two distint sRNAs in the region
          if ( ( p_value < min_p ) && ( nb_srnas > 2 ) )
          {
            // This is a better hit! Record locus and position. 
            // Set min_p to be this hit's p_value.
            real_locus = current_locus;
            seq_ref = pe.getPosition();
            min_p = p_value;
          }
        }

        // Record the last locus so we know how far away the next hit is.
        last_locus = current_locus;
      }
    }

    // Haven't found anything else and we're at the end of the genome, 
    // so add this final locus to the output
    if ( real_locus != null )
    {
      addLocus( real_locus, seq_ref );
    }
  }

  private void addLocus( Locus locus, String seq_ref )
  {
    loci.add( locus );

    actual_tas.put( locus.toString(), potential_tas.get( seq_ref ) );
  }

  /**
   * Counts the number of distinct sRNAs found in the region around this hit on the specified stand
   * @param hit The sRNA hit site on the genome
   * @param phased Whether or not we should look at the phasing sites only.  If we do, 
   * then the sRNA is added as potential ta-si if a hit is found at the tested phasing site.
   * @return The number of distinct sRNAs found in this region on the specified strand
   */
  private int findInRegion( PatmanEntry hit, boolean phased )
  {
    int seq_count = 0;

    SequenceStrand search_strand = SequenceStrand.POSITIVE;

    // Set the region boundaries
    int region_start = hit.getStart();
    int region_end = hit.getStart() + TasiParams.REGION_SIZE - 1;
    int increment = phased ? params.getPhasingRegister() : 1;

    // Search the sense strand first, then the anti-sense strand
    for ( int strand = 0; strand < 2; strand++ )
    {
      int offset = modifyOffset( hit.getSequenceStrand(), search_strand );
      region_start += offset;
      region_end += offset;

      // Try to find other hits in this region
      for ( int i = region_start; i <= region_end; i += increment )
      {
        String test_pos = hit.getLongReadHeader() + "," + i + "," + search_strand.getCharCode();

        // If we find one increment the counter
        if ( hits.containsKey( test_pos ) )
        {
          seq_count++;

          if ( phased )
          {
            PatmanEntry pe = hits.get( test_pos );
            this.potential_tas.addSRNA( hit.getPosition(), pe );
          }
        }
      }

      // Now onto the anti-sense strand...
      search_strand = SequenceStrand.NEGATIVE;
    }

    return seq_count;
  }

  /**
   * Depending on the strandedness of the current hit and the strand we wish to search on
   * and offset must be applied in order to cater for the overhangs Dicer introduces into 
   * the TAS gene
   * @param hit The strand of the current hit
   * @param search The strand we wish to search on
   * @return The offset the should be applied to the genome position
   */
  private int modifyOffset( SequenceStrand hit, SequenceStrand search )
  {
    boolean hit_strand = ( hit == SequenceStrand.POSITIVE ? true : false );
    boolean search_strand = ( search == SequenceStrand.POSITIVE ? true : false );

    if ( hit_strand && search_strand )
    {
      return 0;
    }
    else
    {
      if ( hit_strand && !search_strand )
      {
        return -2;
      }
      else
      {
        if ( !hit_strand && search_strand )
        {
          return 2;
        }
        else
        {
          if ( !hit_strand && !search_strand )
          {
            return 0;
          }
        }
      }
    }

    return 0;
  }

  /**
   * Calculates the p-value from the number of distinct sRNAs found in a potential TAS gene 
   * and the number of distinct phased sRNAs found in a potential TAS gene 
   * This algorithm should be the same as is found in: Chen et al.
   * @param nb_seqs number of distinct sRNAs found in a potential TAS gene
   * @param nb_phased_seqs number of distinct phased sRNAs found in a potential TAS gene 
   * @return The p-value
   */
  private double calculatePValue( int nb_seqs, int nb_phased_seqs )
  {
    double p_val = 0.0;

    for ( int w = nb_phased_seqs; w <= params.getPhasingRegister(); w++ )
    {
      double c = 1.0;
      double rr = 1.0;
      double rw = 1.0;

      for ( int j = 0; j <= w - 1; j++ )
      {
        c = c * (double) ( nb_seqs - j ) / (double) ( j + 1 );
      }
      for ( int x = 0; x <= w - 1; x++ )
      {
        rr = rr * (double) ( 21 - x ) / (double) ( 461 - x );
      }
      for ( int y = 0; y <= nb_seqs - w - 1; y++ )
      {
        rw = rw * (double) ( 440 - y ) / (double) ( 461 - w - y );
      }
      double probability = c * rr * rw;
      p_val += probability;
    }

    return p_val;
  }
}
