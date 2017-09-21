
package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;
//-tool mirprof -srna_file_list /Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa -mirbase_db /Developer/Applications/sRNAWorkbench/Workbench/dist/data/mirbase/20/mature.fa -out_file /Developer/Applications/sRNAWorkbench/Workbench/dist/run.fa
//-tool mirprof -srna_file_list /Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa -mirbase_db /Developer/Applications/sRNAWorkbench/Workbench/dist/data/mirbase/20/mature.fa -genome //Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas -out_file /Developer/Applications/sRNAWorkbench/Workbench/dist/run.fa
//-tool mirprof -srna_file_list /Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa -mirbase_db /Developer/Applications/sRNAWorkbench/Workbench/dist/data/mirbase/20/mature.fa -genome //Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas -out_file /Developer/Applications/sRNAWorkbench/Workbench/dist/run.fa -params /Developer/Applications/sRNAWorkbench/TestingData/AtH/miRProfTests/default_mirprof_params.cfg
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBaseCodeEntry;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;
import uk.ac.uea.cmp.srnaworkbench.swing.OutlineNode;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterGroup;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.utils.FormattingUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;

/**
 * MiRProf determines the expression levels (normalised abundance) of sRNAs that 
 * match known miRNAs across multiple samples.  miRNAs can be grouped according 
 * to various user-defined criteria. 
 * @author ezb11yfu
 */
public final class Mirprof extends RunnableTool
{
  private final static String TOOL_NAME = "MIRPROF";
  private MirprofParams params;
  private List<File> srna_files;
  private File genome_file = null;
  private File mirbase_file;
  private File out_file;
  private File temp_dir;
  private List<FastaMap> srnas_list;
  private OrganismGroup srna_matches;
  private List<FilterGroup> filter_stats;

  public Mirprof() throws Exception
  {
    this( null, null, null, null, null, new MirprofParams() );
  }

  public Mirprof( List<File> srna_files, File genome_file, File mirbase_file, File out_file, File temp_dir, MirprofParams params )
  {
    this( srna_files, genome_file, mirbase_file, out_file, temp_dir, params, null, null );
  }

  public Mirprof( List<File> srna_files, File genome_file, File mirbase_file, File out_file, File temp_dir, MirprofParams params, StatusTracker tracker )
  {
    this( srna_files, genome_file, mirbase_file, out_file, temp_dir, params, null, tracker );
  }

  public Mirprof( List<FastaMap> srnas, File genome_file, File mirbase_file, File temp_dir, MirprofParams params )
  {
    this( null, genome_file, mirbase_file, null, temp_dir, params, srnas, null );
  }

  private Mirprof( List<File> srna_files, File genome_file, File mirbase_file, File out_file, File temp_dir, MirprofParams params, List<FastaMap> srnas_list, StatusTracker tracker )
  {
    super( TOOL_NAME, tracker );

    this.params = params;
    this.mirbase_file = mirbase_file;
    this.srnas_list = srnas_list;
    this.temp_dir = temp_dir;

    this.srna_files = srna_files;
    this.out_file = out_file;

    this.srna_matches = null;
    this.filter_stats = null;
    this.genome_file = genome_file;
    
    Tools.trackPage( "miRProf Main Procedure Class Loaded");
  }

  public List<FilterGroup> getFilterStats()
  {
    return filter_stats;
  }

  public OrganismGroup getMatches()
  {
    return this.srna_matches;
  }

  public List<File> getInputFileList()
  {
    return this.srna_files;
  }

  public MirprofParams getParams()
  {
    return this.params;
  }

  /**
   * Clears all previous results.
   */
  public void clear()
  {
    srna_matches = null;
    filter_stats = null;
  }

  @Override
  protected void process() throws Exception
  {
    // Ensure all previous results are cleared.
    clear();

    continueRun();

    // If user wants to allow for overhangs then XX's a copy of the mirbase file 
    // is created with "XX" added to the beginning and end of every sequence
    File mb_file = this.mirbase_file;
    if ( params.getOverhangsAllowed() )
    {
      trackerInitUnknownRuntime( "Converting miRBase files to support overhanging matches" );


      mb_file = convertFileForOverhangs( this.mirbase_file );


      trackerReset();
    }

    continueRun();

    // Load from srnas from files if a path was provided and the files exists.
    // Otherwise assume the data was provided directly
    if ( srna_files != null )
    {
      srnas_list = new ArrayList<FastaMap>();

      trackerInitKnownRuntime( "Loading samples", srna_files.size() );

      for ( File f : srna_files )
      {
        if ( f != null && f.exists() )
        {
          srnas_list.add( new FastaMap( new SRNAFastaReader( f ).process() ) );
        }
        else
        {
          srnas_list.add( null );
        }

        trackerIncrement();

        continueRun();
      }

      trackerReset();
    }

    // Filter short reads, per user params
    // The filter object stores the filtered data and results, so this can require a
    // lot of memory.  Setting to null after file is generated to save heap space.
    // We filter out low complexity and invalid sequences in order to avoid useless hits.
    FilterParams filt_params = new FilterParams.Builder().setMinLength( params.getMinLength() ).setMaxLength( params.getMaxLength() ).setMinAbundance( params.getMinAbundance() ).setFilterLowComplexitySeq( true ).setFilterInvalidSeq( true ).setOutputNonRedundant( true ).build();

    filter_stats = new ArrayList<FilterGroup>();

    for ( int i = 0; i < srnas_list.size(); i++ )
    {
      String tracker_start = "Sample " + i + ": ";
      String srna_filename = srna_files.get( i ).getName();

      FastaMap srnas = srnas_list.get( i );
      Filter filter = new Filter( srnas, temp_dir, filt_params, this.getTracker() );
      filter.run();

      if ( filter.failed() )
      {
        throw new WorkbenchException( filter.getErrorMessage() );
      }

      continueRun();

      File filtered_file = new File( this.temp_dir.getPath() + DIR_SEPARATOR + srna_filename + "_filtered.fa" );
      FilterGroup fg = filter.getStats();
      fg.setID( srna_filename );
      filter_stats.add( fg );

      filter.writeDataToFasta( filtered_file );
      filter = null;

      continueRun();

      File hits_file = null;
      FastaMap genome_hits = null;
      
      if ( genome_file != null )
      {
        trackerInitUnknownRuntime( tracker_start + "Matching filtered sRNAs to genome" );

        File genome_match_file = new File( this.temp_dir.getPath() + DIR_SEPARATOR + srna_filename + "_genome_matches.patman" );

        // Align filtered file to genome.
        PatmanRunner pr1 = new PatmanRunner( filtered_file, genome_file, genome_match_file, temp_dir, new PatmanParams() );
        pr1.run();
        if ( pr1.failed() )
        {
          throw new WorkbenchException( pr1.getErrorMessage() );
        }

        pr1 = null;
        genome_hits = new PatmanReader( genome_match_file ).process().buildFastaMap();
        hits_file = new File( this.temp_dir.getPath() + DIR_SEPARATOR + srna_filename + "_genome_matches.fa" );
        filter_stats.get( filter_stats.size() - 1 ).add( new FilterStage( "filter by genome (matches in)", genome_hits.getTotalSeqCount(), genome_hits.getDistinctSeqCount() ) );
        SRNAFastaWriter writer = new SRNAFastaWriter( hits_file );
        
        writer.process( genome_hits, true );
      }
      else
      {
        hits_file = filtered_file;
      }

      


      trackerReset();

      continueRun();

      // Match filtered file to mirbase using patman
      trackerInitUnknownRuntime( tracker_start + "Matching sRNAs to miRBase using patman" );

      File patman_file = new File( this.temp_dir.getPath() + DIR_SEPARATOR + "mirprof_" + mb_file.getName() + ".patman" );

      PatmanParams pp = new PatmanParams.Builder().setMaxMismatches( params.getMismatches() ).setPositiveStrandOnly( true ).build();

      
      // Align possible sRNAs to miRBase
      PatmanRunner pr2 = new PatmanRunner( hits_file, mb_file, patman_file, temp_dir, pp );
      pr2.run();
      if ( pr2.failed() )
      {
        throw new WorkbenchException( pr2.getErrorMessage() );
      }

      trackerReset();

      continueRun();

      // Build Patman object from results
      trackerInitUnknownRuntime( tracker_start + "Processing matches" );

      Patman pat = new PatmanReader( patman_file ).process();  // Read in everything

      trackerReset();

      continueRun();

      //Transpose Result Hash
      trackerInitUnknownRuntime( tracker_start + "Compiling results" );

      srna_matches = transposeResultHash( pat, i, srnas_list.size(), srna_matches, genome_hits );

      trackerReset();

      continueRun();
    }

    continueRun();

    // Generate Output
    if ( out_file != null )
    {
      trackerInitUnknownRuntime( "Writing results to disk" );

      File csv_file = new File( out_file.getPath() + "_profile.csv" );
      File fa_file = new File( out_file.getPath() + "_srnas.fa" );

      generateTableHeader( csv_file, srna_matches, filter_stats );
      generateTableNewFormat( csv_file, srna_matches, filter_stats );
      generateFASTA( fa_file, srna_matches );

      trackerReset();
    }
  }

  /**
   * Writes all output to disk.
   * @param out_file Location for files to be written
   * @throws IOException Thrown if there were any problems writing out the files.
   */
  public void writeTable( File out_file ) throws IOException
  {
    generateTableHeader( out_file, srna_matches, filter_stats );
  }

  public void writeFASTA( File out_file ) throws IOException
  {
    generateFASTA( out_file, srna_matches );
  }

  /**
   * Build a new OrganismGroup for the specified sample, which is a hash keyed
   * by matches rather than sRNAs.  This is required to print the results by match 
   * (or match group) in table and FASTA file.  Grouping and filtering for best
   * hits only is done at this step (it would not be possible to filter for best 
   * matches before all sRNAs and their matches have been read from the patman 
   * file, which is why the 2 data structures are needed)
   * @param p Patman object containing all the SRNA hits to miRBase mature miRNAs
   * @param sample_index The sample index identifying the sample currently being grouped
   * @param num_samples The total number of samples present in this run
   * @param matches The current results tree (might contain matches from previous samples)
   * @return Results tree with extra results added from the specified sample
   * @throws Exception Thrown if there were any problems
   */
  private OrganismGroup transposeResultHash( Patman p, int sample_index, int num_samples, OrganismGroup matches, FastaMap genome_hits) throws Exception
  {
    if ( params.getOnlyKeepBest() )
    {
      //    # If the keep_best option is chosen,
      //    # discard all miRNAs that have more
      //    # mismatches than the best hit(s)
      p.onlyKeepBestMatches();
    }

    // Group matches according to options
    matches = groupMatches( p, sample_index, num_samples, matches, genome_hits );

    // Calculate the counts in the match groups.
    for ( String org_code : new TreeSet<String>( matches.keySet() ) )
    {
      // Now add all (combined) matches for this sequence
      // to the global list of matches with counts in order
      for ( String group_code : new TreeSet<String>( matches.get( org_code ).keySet() ) )
      {
        List<SrnaMatch> samples_results = matches.get( org_code ).get( group_code );

        // Loop through all samples and calculate the results.
        for ( SrnaMatch sample : samples_results )
        {
          // This should be set to the number of times the sRNA was found in the genome (or in mirbase??).
          // Using 1 (i.e. weighted == raw) until we can get this figure.
          sample.calculateCounts();
        }
      }
    }

    return matches;
  }

  /**
   * Combine matches according to options. Keep miRNAs from different organisms 
   * separate because we don't want to split up counts across organisms (e.g. 
   * a sequence matching ath-miR156 and osa-miR156 shouldn't get a weighted count 
   * of 0.5). If organisms are ignored, we add all matches to organism 'all combined'
   * @param p Patman object containing all the SRNA hits to miRBase mature miRNAs
   * @param sample_index The sample index identifying the sample currently being grouped
   * @param num_samples The total number of samples present in this run
   * @param org_group The current results tree
   * @return Results tree with extra results added from the specified sample
   * @throws Exception Thrown if there were any problems
   */
  private OrganismGroup groupMatches( Patman p, int sample_index, int num_samples, OrganismGroup org_group, FastaMap genome_hits) throws Exception
  {
    org_group = org_group == null ? new OrganismGroup() : org_group;

    for ( PatmanEntry pe : p )
    {

      // Parse match_string based on mirbase data format.
      String mirbase_header = pe.getLongReadHeader();
      int end = mirbase_header.indexOf( " " ) == -1 ? mirbase_header.length() : mirbase_header.indexOf( " " );
      String mirna_code = mirbase_header.substring( 0, end ).trim();
      MirBaseCodeEntry miRNA = MirBaseCodeEntry.parseCode( mirna_code );

      StringBuilder group_code_builder = new StringBuilder();

      group_code_builder.append( miRNA.getFamily() != null ? miRNA.getFamily() : "" );

      if ( !params.getGroupVariant() )
      {
        group_code_builder.append( miRNA.getVariant() );
        if ( !miRNA.getCopy().isEmpty() )
        {
          group_code_builder.append( "-" );
          group_code_builder.append( miRNA.getCopy() );
        }

        if ( !miRNA.getArm().toString().isEmpty() )
        {
          group_code_builder.append( "-" );
          group_code_builder.append( miRNA.getArm().toString() );
        }
      }

      if ( !params.getGroupMatureAndStar() )
      {
        group_code_builder.append( miRNA.isStar() ? "*" : "" );
      }

      if ( !params.getGroupMismatches() )
      {
        group_code_builder.append( "(" );
        group_code_builder.append( pe.getMismatches() );
        group_code_builder.append( ")" );
      }


      String group_code = group_code_builder.toString();

      // Either store by organism or combine all into one
      String org = params.getGroupOrganisms() ? "all combined" : miRNA.getSpecies();

      MatchGroup match_group = org_group.get( org );

      if ( match_group == null )
      {
        // We've not seen this organism before so create the data structure from scratch                
        // and add the newly created match group to the organism group
        org_group.put( org, new MatchGroup( pe, sample_index, num_samples, group_code ) );
      }
      else
      {
        // We've seen this organism before...
        List<SrnaMatch> samples_results = match_group.get( group_code );

        if ( samples_results == null || samples_results.isEmpty() )
        {
          // ... but we haven't seen this particular miRNA before, so create a new mirna entry
          match_group.put( group_code, new MirnaEntry( pe, sample_index, num_samples ) );
        }
        else
        {
          // We've seen this particular miRNA before...
          FastaMap sm = samples_results.get( sample_index ).getSeqMap();

          if ( sm == null )
          {
            // ... but not for this sample, so create new default fasta map from the patman entry 
            // and add it to the miRNA entry.                        
            samples_results.get( sample_index ).setSeqMap( new FastaMap( pe ) );
          }
          else
          {
            // We've seen this miRNA before in this sample
            FastaRecord fr = sm.get( pe.getSequence() );

            if ( fr == null )
            {
              //int frequency = Collections.frequency( p, pe );
              
              // ... but we haven't seen this particular sRNA before in this sample,
              // so create a new default fasta record from the the patman entry and
              // add it to the fasta map.
              fr = new FastaRecord( pe );
              sm.put( pe.getSequence(),  fr);
              //fix for broken original code. Problem stem is from the first version counting miRBase hits(!!!!!) not genome matching reads
              //this should be replaced with the far more efficient version of the algorithm seen in colide and siloco
              //when I have time (which is never..)
              if ( genome_hits != null )
              {
                if ( genome_hits.containsKey( pe.getSequence() ) )
                {
                  fr.setHitCount( genome_hits.get( pe.getSequence() ).getHitCount() );
                }
              }
            }
            else
            {
              // We've already seen this srna seq in this sample for this mirna before so just increment the hit count.
              fr.incHitCount();
              
              //fix for broken original code. Problem stem is from the first version counting miRBase hits(!!!!!) not genome matching reads
              //this should be replaced with the far more efficient version of the algorithm seen in colide and siloco
              //when I have time (which is never..)
              if ( genome_hits != null )
              {
                if ( genome_hits.containsKey( pe.getSequence() ) )
                {
                  fr.setHitCount( genome_hits.get( pe.getSequence() ).getHitCount() );
                }
              }
            }
          }
        }
      }
    }

    return org_group;
  }

  /**
   * Produce a csv file containing the miRProf run results.
   * @param org_group OrganismGroup containing the results
   * @param filter_stages List of filter stage results
   * @throws IOException Thrown if there was a problem writing to file
   */
  private void generateTableHeader( File csv_file, OrganismGroup org_group, List<FilterGroup> filter_stages ) throws IOException
  {
    // Some helper strings.
    String comma = ",";
    String quote = "\"";
    String nl = "\n";

    // Gets the highest read count from all samples after the last filtering step
    int[] total_reads = new int[filter_stages.size()];
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      FilterGroup fg = filter_stages.get( i );
      total_reads[i] = fg.get( fg.size() - 1 ).getTotalReadCount();
    }

    File table_file = csv_file;
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( table_file ) ) );

    pw.write( quote + "miRProf results" + quote + nl );
    pw.write( quote + "Normalised count: count of matching sequence reads normalised to total number of reads after last filtering step (see table below). Given in parts per million" + quote + nl );

    // Print name of mirbase database used
    pw.write( quote + "miRBase database: " + this.mirbase_file.getName() );
    pw.write( comma + " number of allowed mismatches: " + params.getMismatches() + quote + nl );

    String keep_string = params.getOnlyKeepBest() ? "keep best matches only" : "keep all matches";
    pw.write( quote + "Mismatch filtering: " + keep_string + quote + nl );

    // Print used grouping options
    pw.write( quote + "Grouping options used: " + getGroupOptionsString() + quote + nl );
    pw.write( nl );

    // Print sample filenames
    pw.write( quote + "Sample filenames" + quote + nl );
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      pw.write( i + comma + filter_stages.get( i ).getID() + nl );
    }
    pw.write( nl );

    // Print table of read counts
    // stages are "input", "filtered: sequence properties", "filtered: t/rRNA"
    // We only have one sample S1 at the moment
    pw.write( quote + "Read counts" + quote + nl );
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      pw.write( comma + i + "_total" + comma + i + "_distinct" );
    }
    pw.write( nl );
    
    

    for ( int j = 0; j < filter_stages.get( 0 ).size(); j++ )
    {
      pw.write( quote + filter_stages.get( 0 ).get( j ).getStageName() + quote );

      for ( int i = 0; i < filter_stages.size(); i++ )
      {
        pw.write( comma + filter_stages.get( i ).get( j ).getTotalReadCount() );
        pw.write( comma + filter_stages.get( i ).get( j ).getDistinctReadCount() );
      }
      pw.write( nl );
    }
//
//    // data - one block per organism (which could be "all combined")
//    for ( String org : new TreeSet<String>( org_group.keySet() ) )
//    {
//      MatchGroup mirnas = org_group.get( org );
//
//      pw.write( nl + quote + "Organism: " + org + quote + nl );
//      // Header for the data
//      pw.write( quote + "matching miRNAs" + quote + nl );
//
//      for ( int i = 0; i < filter_stages.size(); i++ )
//      {
//        pw.write( comma + i + "_raw"  + comma + i + "_norm" + comma + i + "_seqs" );
//      }
//
//      pw.write( nl );
//
//      for ( String mirna : new TreeSet<String>( mirnas.keySet() ) )
//      {
//        List<SrnaMatch> samples = mirnas.get( mirna );
//
//        pw.write( mirna );
//
//        for ( int i = 0; i < samples.size(); i++ )
//        {
//          SrnaMatch sample = samples.get( i );
//
//          pw.write( comma + sample.getRawCount() );
//          
//          // We normalise to total reads from this sample after last filtering step
//          double norm_count = ( (double)sample.getRawCount() / (double)total_reads[i] ) * 1e6;
//          pw.write( comma + FormattingUtils.formatSRNACounts( norm_count ) );
//
//          // Output all the sequences in the group.
//          pw.write( comma );
//          if ( sample.getSeqMap() != null )
//          {
//            pw.write( StringUtils.join( sample.getSeqMap().keySet(), ";" ) );
//          }
//        }
//
//        pw.write( nl );
//      }
//    }
    pw.flush();
    pw.close();
  }

  private void generateTableNewFormat(File csv_file, OrganismGroup org_group, List<FilterGroup> filter_stages)
  {
    FileWriter outCSVFile = null;
    PrintWriter out = null;
    
        // Some helper strings.
    String comma = ",";
    String quote = "\"";
    try
    {
      outCSVFile = new FileWriter( csv_file, true );
      out = new PrintWriter( outCSVFile, true );
      //out.
      // Write text to file

      String header = "";
//      if ( m_params.getGroupMismatches() )
//      {
//        header += "ignore mismatches,";
//      }
      if ( !params.getGroupOrganisms() )
      {
        header += "Organism Code,";
      }
      else
      {
        header += ",";
      }
      header += "miRNA Code,Sequence,Genome Matches,";
      for ( File sampleFile : this.srna_files )
      {
        header += "Sample: " + sampleFile.getName() + " raw expression,";
      }
      for ( File sampleFile : this.srna_files )
      {
        header += "Sample: " + sampleFile.getName() + " normalised expression,";
      }

      header += LINE_SEPARATOR;
      out.println();
      out.println( header );

      // Gets the highest read count from all samples after the last filtering step
      int[] total_reads = new int[filter_stages.size()];
      for ( int i = 0; i < filter_stages.size(); i++ )
      {
        FilterGroup fg = filter_stages.get( i );
        total_reads[i] = fg.get( fg.size() - 1 ).getTotalReadCount();
      }

      for ( String org : new TreeSet<String>( org_group.keySet() ) )
      {
        out.println( org + ",");
        MatchGroup mirnas = org_group.get( org );
        for(Entry<String, MirnaEntry> e : new TreeMap<>(mirnas).entrySet())
        {
          out.println("," + e.getKey());
          int sampleIndex = 0;
          StringBuilder detailsForThis_miRNA = new StringBuilder();

          HashMap<String,Double[]> outputData = new HashMap<>();
          int totalRowSize = (e.getValue().size()*2)+1;
          double[] totals = new double[totalRowSize];
          for(SrnaMatch sample : e.getValue())
          {
            
            
            if ( sample.getSeqMap() != null )
            {
              for ( Entry<String, FastaRecord> FA_E : new TreeMap<>( sample.getSeqMap() ).entrySet() )
              {
                //String hitCount = genome_file == null ? "N/A" : String.valueOf(  );

                double normAbundance = ( (double) FA_E.getValue().getRealAbundance() / (double) total_reads[sampleIndex] ) * 1e6;
                Double[] data = outputData.get( FA_E.getKey());
                if(data != null)//entry found for this key so update
                {
                 
                  //should be no need to update hit count as will be the same for all samples
                  //however, my tests have detected a bug that can show different counts over samples!
                  //for now, I must choose the highest, however this needs further investigation...
                  data[0] = Math.max( data[0], FA_E.getValue().getHitCount());
                  data[sampleIndex + 1] = Double.valueOf( FA_E.getValue().getAbundance() );
                  data[sampleIndex + 1 + e.getValue().size()] = normAbundance;
    
                }
                else
                {
                  Double[] newRowData = new Double[totalRowSize];
                  
                  for ( int i = 0; i < totalRowSize; i++ )
                  {
                    newRowData[i] = 0.0;
                  }
                  newRowData[0] = Double.valueOf(FA_E.getValue().getHitCount());
                  newRowData[sampleIndex+1] = Double.valueOf(FA_E.getValue().getAbundance());
                  newRowData[sampleIndex+1+e.getValue().size()] = normAbundance;
                  outputData.put( FA_E.getKey(), newRowData); 
                }
               
              }
            }
            sampleIndex++;
          }
          StringBuilder totalsOutput = new StringBuilder(",,Totals: ,");
          for(Entry<String, Double[] >entry : outputData.entrySet())
          {
            detailsForThis_miRNA.append( ",," )
                  .append( entry.getKey() )
                  .append( "," );
            for ( int i = 0; i < entry.getValue().length; i++ )
            {
              totals[i] += entry.getValue()[i];
              detailsForThis_miRNA.append( entry.getValue()[i]) 
                  .append( "," );
            }
            detailsForThis_miRNA.append(LINE_SEPARATOR);
          }
          for ( int i = 0; i < totals.length; i++ )
          {
            totalsOutput.append( totals[i] ).append( "," );
          }
          
          out.print(detailsForThis_miRNA.toString());
          out.println( totalsOutput.toString() );
         
          
          
        }
      }


      // data - one block per organism (which could be "all combined")
//      for ( String org : new TreeSet<String>( org_group.keySet() ) )
//      {
//        MatchGroup mirnas = org_group.get( org );
//
//        
//        for ( int i = 0; i < filter_stages.size(); i++ )
//        {
//          out.write( comma + i + "_raw" + comma + i + "_norm" + comma + i + "_seqs" );
//        }
//
//        out.write( LINE_SEPARATOR );
//
//        for ( String mirna : new TreeSet<String>( mirnas.keySet() ) )
//        {
//          List<SrnaMatch> samples = mirnas.get( mirna );
//
//          out.write( mirna );
//
//          for ( int i = 0; i < samples.size(); i++ )
//          {
//            SrnaMatch sample = samples.get( i );
//
//            out.write( comma + sample.getRawCount() );
//
//            // We normalise to total reads from this sample after last filtering step
//            double norm_count = ( (double) sample.getRawCount() / (double) total_reads[i] ) * 1e6;
//            out.write( comma + FormattingUtils.formatSRNACounts( norm_count ) );
//
//            // Output all the sequences in the group.
//            out.write( comma );
//            if ( sample.getSeqMap() != null )
//            {
//              out.write( StringUtils.join( sample.getSeqMap().keySet(), ";" ) );
//            }
//          }
//
//          out.write( LINE_SEPARATOR );
//        }
//      }
      //////////////////////////////////
//      Outline outline = this.m_output.getOutline();
//      DefaultOutlineModel model = (DefaultOutlineModel) outline.getModel();
//      OutlineNode treeModel = (OutlineNode) model.getRoot();
//      int miRNACount = treeModel.getChildCount();
//
//      for ( int i = 0; i < miRNACount; i++ )
//      {
//        OutlineNode chromosomeModel = (OutlineNode) treeModel.getChildAt( i );
//        List<String> organismDataObject = chromosomeModel.getUserObject();
//
//        for ( String organismData : organismDataObject )
//        {
//          out.println( organismData + "," );
//          //out.print(",");
//        }
//
//        int sequenceCount = chromosomeModel.getChildCount();
//        for ( int sequenceID = 0; sequenceID < sequenceCount; sequenceID++ )
//        {
//          OutlineNode miRNAData = (OutlineNode) chromosomeModel.getChildAt( sequenceID );
//          List<String> userObject = miRNAData.getUserObject();
//          for ( String data : userObject )
//          {
//            out.println( "," + data );
//            int miRNASequenceCount = miRNAData.getChildCount();
//            for ( int miRNASeqNumber = 0; miRNASeqNumber < miRNASequenceCount; miRNASeqNumber++ )
//            {
//              OutlineNode miRNASequenceData = (OutlineNode) miRNAData.getChildAt( miRNASeqNumber );
//              List<String> miRNAStats = miRNASequenceData.getUserObject();
//              out.print( "," );
//              for ( String stat : miRNAStats )
//              {
//                out.print( "," + stat );
//              }
//              out.println();
//            }
//          }
//        }
//      }
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


  
    /**
   * Produce a csv file containing the miRProf run results.
   * @param org_group OrganismGroup containing the results
   * @param filter_stages List of filter stage results
   * @throws IOException Thrown if there was a problem writing to file
   */
  private void generateTable( File csv_file, OrganismGroup org_group, List<FilterGroup> filter_stages ) throws IOException
  {
    // Some helper strings.
    String comma = ",";
    String quote = "\"";
    String nl = "\n";

    // Gets the highest read count from all samples after the last filtering step
    int[] total_reads = new int[filter_stages.size()];
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      FilterGroup fg = filter_stages.get( i );
      total_reads[i] = fg.get( fg.size() - 1 ).getTotalReadCount();
    }

    File table_file = csv_file;
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( table_file ) ) );

    pw.write( quote + "miRProf results" + quote + nl );
    pw.write( quote + "Normalised count: count of matching sequence reads normalised to total number of reads after last filtering step (see table below). Given in parts per million" + quote + nl );

    // Print name of mirbase database used
    pw.write( quote + "miRBase database: " + this.mirbase_file.getName() );
    pw.write( comma + " number of allowed mismatches: " + params.getMismatches() + quote + nl );

    String keep_string = params.getOnlyKeepBest() ? "keep best matches only" : "keep all matches";
    pw.write( quote + "Mismatch filtering: " + keep_string + quote + nl );

    // Print used grouping options
    pw.write( quote + "Grouping options used: " + getGroupOptionsString() + quote + nl );
    pw.write( nl );

    // Print sample filenames
    pw.write( quote + "Sample filenames" + quote + nl );
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      pw.write( i + comma + filter_stages.get( i ).getID() + nl );
    }
    pw.write( nl );

    // Print table of read counts
    // stages are "input", "filtered: sequence properties", "filtered: t/rRNA"
    // We only have one sample S1 at the moment
    pw.write( quote + "Read counts" + quote + nl );
    for ( int i = 0; i < filter_stages.size(); i++ )
    {
      pw.write( comma + i + "_total" + comma + i + "_distinct" );
    }
    pw.write( nl );
    
    

    for ( int j = 0; j < filter_stages.get( 0 ).size(); j++ )
    {
      pw.write( quote + filter_stages.get( 0 ).get( j ).getStageName() + quote );

      for ( int i = 0; i < filter_stages.size(); i++ )
      {
        pw.write( comma + filter_stages.get( i ).get( j ).getTotalReadCount() );
        pw.write( comma + filter_stages.get( i ).get( j ).getDistinctReadCount() );
      }
      pw.write( nl );
    }

    // data - one block per organism (which could be "all combined")
    for ( String org : new TreeSet<String>( org_group.keySet() ) )
    {
      MatchGroup mirnas = org_group.get( org );

      pw.write( nl + quote + "Organism: " + org + quote + nl );
      // Header for the data
      pw.write( quote + "matching miRNAs" + quote + nl );

      for ( int i = 0; i < filter_stages.size(); i++ )
      {
        pw.write( comma + i + "_raw"  + comma + i + "_norm" + comma + i + "_seqs" );
      }

      pw.write( nl );

      for ( String mirna : new TreeSet<String>( mirnas.keySet() ) )
      {
        List<SrnaMatch> samples = mirnas.get( mirna );

        pw.write( mirna );

        for ( int i = 0; i < samples.size(); i++ )
        {
          SrnaMatch sample = samples.get( i );

          pw.write( comma + sample.getRawCount() );
          
          // We normalise to total reads from this sample after last filtering step
          double norm_count = ( (double)sample.getRawCount() / (double)total_reads[i] ) * 1e6;
          pw.write( comma + FormattingUtils.formatSRNACounts( norm_count ) );

          // Output all the sequences in the group.
          pw.write( comma );
          if ( sample.getSeqMap() != null )
          {
            pw.write( StringUtils.join( sample.getSeqMap().keySet(), ";" ) );
          }
        }

        pw.write( nl );
      }
    }
    pw.flush();
    pw.close();
  }
  /**
   * Produce fasta file containing distinct reads.  Header contains organism, miRNA,
   * sequence ID in miRNA group, and sequence abundance.
   * @param org_group Data to write to file
   * @throws IOException Thrown if there was a problem writing to file
   */
  private void generateFASTA( File fa_file, OrganismGroup org_group ) throws IOException
  {
    PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( fa_file ) ) );

    for ( String org : new TreeSet<String>( org_group.keySet() ) )
    {
      MatchGroup mirnas = org_group.get( org );
      for ( String mirna : new TreeSet<String>( mirnas.keySet() ) )
      {
        List<SrnaMatch> samples = mirnas.get( mirna );

        for ( SrnaMatch sample_hits : samples )
        {
          int i = 0;

          if ( sample_hits.getSeqMap() != null )
          {
            ArrayList<Map.Entry<String, FastaRecord>> list = new ArrayList<Map.Entry<String, FastaRecord>>( sample_hits.getSeqMap().entrySet() );

            java.util.Collections.sort( list, new Comparator<Map.Entry<String, FastaRecord>>()
            {
              @Override
              public int compare( Map.Entry<String, FastaRecord> entry1, Map.Entry<String, FastaRecord> entry2 )
              {
                if ( entry1.getValue().equals( entry2.getValue() ) )
                {
                  if ( entry1.getKey().equals( entry2.getKey() ) )
                  {
                    return 0;
                  }
                  else
                  {
                    return entry1.getKey().compareTo( entry2.getKey() );
                  }
                }
                else
                {
                  return entry1.getValue().getAbundance() < entry2.getValue().getAbundance() ? 1 : -1;
                }
              }
            } );


            for ( Map.Entry<String, FastaRecord> seq : list )
            {
              ++i;
              String id = org + "-" + mirna + "_" + i + "_Abundance(" + seq.getValue().getAbundance()+")";
              id.replaceAll( "\\s", "_" );

              pw.print( ">" + id + "\n" );
              pw.print( seq.getKey() + "\n" );
            }
          }
        }
      }
    }
    pw.flush();
    pw.close();
  }

  /**
   * Produces a string describing the grouping options used for this run
   * @return String describing the grouping options selected for this run.
   */
  public String getGroupOptionsString()
  {
    ArrayList<String> opts_strings = new ArrayList<String>();
    if ( params.getGroupMismatches() )
    {
      opts_strings.add( "ignore mismatches" );
    }
    if ( params.getGroupOrganisms() )
    {
      opts_strings.add( "combine organisms" );
    }
    if ( params.getGroupVariant() )
    {
      opts_strings.add( "combine variants" );
    }

    return opts_strings.size() > 0 ? StringUtils.join( opts_strings, ", " ) : "none";
  }

  /**
   * Converts the file containing miRNAs so that each sequence contains an prefix
   * and a suffix containing three Xs.
   * @param file_in File containing miRBase mature miRNAs
   * @return File containing the converted data
   * @throws IOException Thrown if there was some problem reading or writing the files
   */
  private File convertFileForOverhangs( File file_in ) throws IOException
  {
    BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( file_in ) ) );

    File file_out = new File( this.temp_dir.getPath() + DIR_SEPARATOR + file_in.getName() + "_plusXXX.fa" );
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file_out ) ) );

    // Get the second line, which will contain the first sequence.
    String line = null;
    while ( ( line = in.readLine() ) != null )
    {
      line = line.trim();

      if ( line.isEmpty() )
      {
        // Do nothing
      }
      else
      {
        if ( line.startsWith( ">" ) )
        {
          out.print( line );
          out.print( "\n" );
        }
        else
        {
          out.print( "XXX" + line + "XXX" );
          out.print( "\n" );
        }
      }
    }

    in.close();
    out.flush();
    out.close();

    return file_out;
  }

  public static void main( String[] args )
  {
    try
    {
      boolean overwriteOutput = true;
      
      //File srnaFile = ToolBox.checkInputFile( "srna_file", argmap.get( "srna_file" ), true );
      //String fileListString = argmap.get( "srna_file_list" );
//      if(fileListString == null || fileListString.isEmpty())
//        throw new IllegalArgumentException("Error: The parameter srna_file_list must be specified." );
      //String fileList[] = fileListString.split( ",");
      File sampleFiles[] = {new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa" )};

      File mirbaseFile = new File("/Developer/Applications/sRNAWorkbench/Workbench/dist/data/mirbase/20/mature.fa");
      File outFile = new File("/Developer/Applications/sRNAWorkbench/Workbench/dist/run.fa");
      File paramsFile = null;
      File genomeFile = null;

      MirprofParams params = ( paramsFile == null ) ? new MirprofParams() : MirprofParams.load( new MirprofParams(), paramsFile );


      Mirprof mirprof = new Mirprof( Arrays.asList( sampleFiles ), genomeFile, mirbaseFile, outFile, Tools.getNextDirectory(), params );
      mirprof.run();

    }
    catch ( IOException e )
    {
      System.err.println( "MIRPROF ERROR! " + e.getMessage() );
    }
  }
  /**
   * Shorthand for List<SrnaMatch>
   */
  public static class MirnaEntry extends ArrayList<SrnaMatch>
  {
    /**
     * Creates a new SrnaMatch list (of size num_samples).  
     * Populates the sRNA match indicated by the sample index with a new FastaMap
     * generated by the PatmanEntry.
     * @param pe PatmanEntry containing the sRNA hit to a mature miRNA
     * @param sample_index The sample index identifying the sample currently being grouped 
     * @param num_samples The total number of samples present in this run
     */
    public MirnaEntry( PatmanEntry pe, int sample_index, int num_samples )
    {
      super();

      // Don't worry about the scores for now... these will get calculated later.
      // but create a new srna match and add the newly create fasta map
      SrnaMatch new_match = new SrnaMatch( 0, new FastaMap( pe ) );

      for ( int i = 0; i < num_samples; i++ )
      {
        if ( i == sample_index )
        {
          this.add( new_match );
        }
        else
        {
          this.add( new SrnaMatch( 0, null ) );
        }
      }
    }
  }

  /**
   * Shorthand for HashMap<String, MirnaEntry>
   */
  public static class MatchGroup extends HashMap<String, MirnaEntry>
  {
    /**
     * Creates a new match group with the specified group code containing a single 
     * SrnaMatch list
     * @param pe PatmanEntry containing the sRNA hit to a mature miRNA
     * @param sample_index The sample index identifying the sample currently being grouped 
     * @param num_samples The total number of samples present in this run
     * @param group_code The code for the initial match group
     */
    public MatchGroup( PatmanEntry pe, int sample_index, int num_samples, String group_code )
    {
      super();

      this.put( group_code, new MirnaEntry( pe, sample_index, num_samples ) );
    }
  }

  public String getMirBaseFileName()
  {
    return this.mirbase_file.getName();
  }

  /**
   * Shorthand for HashMap<String, MatchGroup>
   */
  public static class OrganismGroup extends HashMap<String, MatchGroup>
  {
    public OrganismGroup()
    {
      super();
    }
  }
}
