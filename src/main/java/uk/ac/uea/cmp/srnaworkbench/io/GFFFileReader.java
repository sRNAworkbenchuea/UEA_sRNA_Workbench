/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.GFFRecord;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

/**
 * Load the contents of a given GFF file
 * The GFF file format definition is at http://www.sequenceontology.org/gff3.shtml
 *
 * @author prb07qmu
 */
public class GFFFileReader extends RunnableTool
{

    @Override
    protected void process() throws Exception {
        processFile();
    }
  // A lot of auto(un)boxing goes on here.
  // However, there is no noticeable change in performance when using primitive types instead (measured on Windows7 with JDK 1.6.0_26).
  //
  private enum GFFFileColumns
  {
    SEQID()     { @Override protected Object extractImpl( String str ) { return str; } },
    SOURCE()    { @Override protected Object extractImpl( String str ) { return str; } },
    SEQ_TYPE()  { @Override protected Object extractImpl( String str ) { return str; } },
    START()     { @Override protected Object extractImpl( String str ) { return StringUtils.safeIntegerParse( str, 1 ); } },
    END()       { @Override protected Object extractImpl( String str ) { return StringUtils.safeIntegerParse( str, 1 ); } },
    SCORE()     { @Override protected Object extractImpl( String str ) { return StringUtils.safeFloatParse( str, GFFRecord.UNKNOWN_SCORE ); } },
    STRAND()    { @Override protected Object extractImpl( String str ) { return StringUtils.safeCharAt( str, 0, '*' ); } },
    PHASE()     { @Override protected Object extractImpl( String str ) { return (byte)StringUtils.safeIntegerParse( str, -1 ); } },
    ATTRIBUTES(){ @Override protected Object extractImpl( String str ) { return str; } };

    protected abstract Object extractImpl( String str );

    /**
     * Extract column specific information from the string
     *
     * @param line The line from the file
     * @param start The start index
     * @param end The end index
     * @return Object wrapper which wraps the appropriate type for the column. Guaranteed not to be null.
     */
    private Object extract( String line, int start, int end )
    {
      if ( line == null || start == -1 || end == -1 )
        return "";

      if ( start >= end || start >= line.length() )
        return "";

      if ( end >= line.length() )
        end = line.length();

      String substr = line.substring( start, end );

      return extractImpl( substr );
    }
  }

  // An EnumMap for storing the filters; each GFFFileRecordType may have an arbitrary number
  // of Strings on which to filter. Everything is included if the GFFFileRecordType is
  // not in the map or it is and its associated set is empty.
  //
  private Map< GFFFileColumns, Set< String > > _filters = CollectionUtils.newEnumMap( GFFFileColumns.class );
  
  // A map that can be optionally used to compare two competing types for priority in
  // annotation
  // e.g. if there are two types "gene" and "exon", "exon" should be used because
  // it more appropriately describes the annotation
  protected Map<String, Integer> _typePriorityMap = new HashMap<>();
  int defPriorityCounter = 0;

  protected File _gffFile;
  private ArrayList< GFFRecord > _gffRecords = CollectionUtils.newArrayList( 1000 );

  /***
   * Constructor
   *
   * @param gffFile The {@code File} object
   */
  public GFFFileReader( File gffFile )
  {
      super("GFFReaderTool");


    _gffFile = gffFile;

    _filters.put( GFFFileColumns.SEQID, new HashSet<String>() );
    _filters.put( GFFFileColumns.SEQ_TYPE, new HashSet<String>() );
  }

  protected Set<String> getSequenceIdFilters()
  {
    return Collections.unmodifiableSet( _filters.get( GFFFileColumns.SEQID ) );
  }

  protected Set<String> getSequenceTypeFilters()
  {
    return Collections.unmodifiableSet( _filters.get( GFFFileColumns.SEQ_TYPE ) );
  }

  public void addSequenceIdFilter( String sequenceId )
  {
    _filters.get( GFFFileColumns.SEQID ).add( sequenceId );
  }

  public void addSequenceTypeFilter( String sequenceType )
  {
    _filters.get( GFFFileColumns.SEQ_TYPE ).add( sequenceType );
    addTypePriority(sequenceType);
  }
  
  public void addTypePriority(String sequenceType)
  {
      _typePriorityMap.put(sequenceType, defPriorityCounter);
      defPriorityCounter++;
  }
  
  public void addTypePriority(String sequenceType, int priority)
  {
      _typePriorityMap.put(sequenceType, defPriorityCounter);
  }
  
  public String getTypePriority(Set<String> sequenceTypes){
      Iterator<String> it = sequenceTypes.iterator();
      int hi = Integer.MAX_VALUE;
      String type = "intergenic";
      while(it.hasNext())
      {
          String thistype = it.next();
          if(_typePriorityMap.get(thistype) < hi)
          {
              hi = _typePriorityMap.get(thistype);
              type= thistype;
          }
      }
      return type;
  }

  private boolean includeSequenceId( String sequenceId )
  {
    return isIncluded( GFFFileColumns.SEQID, sequenceId );
  }

  private boolean includeSequenceType( String sequenceType )
  {
    return isIncluded( GFFFileColumns.SEQ_TYPE, sequenceType );
  }

  private boolean isIncluded( GFFFileColumns filterId, String str )
  {
    Set< String > l = _filters.get( filterId );

    // null or Empty set => include everything
    if ( l == null || l.isEmpty() )
      return true;

    return l.contains( str );
  }
  
  
  
  public GFFRecord processLine(String line, StringPool pool)
  {
      int[] tabs = new int[8];

      String seqid, type, source, attributes;
      int startBasePosition, endBasePosition;
      float score;
      byte phase;
      char strand;
      
      Arrays.fill(tabs, -1);

      for (int i = 0, tabPos = 0; i < tabs.length; ++i, ++tabPos) {
          tabPos = line.indexOf('\t', tabPos);
          if (tabPos == -1) {
              break;
          }
          tabs[i] = tabPos;
      }

      // Get the SEQID and see if we are filtering on it...
      seqid = GFFFileColumns.SEQID.extract(line, 0, tabs[0]).toString();
      if (!includeSequenceId(seqid)) {
          return null;
      }

      // Get the SEQ_TYPE and see if we are filtering on it...
      type = GFFFileColumns.SEQ_TYPE.extract(line, tabs[1] + 1, tabs[2]).toString();
      if (!includeSequenceType(type)) {
          return null;
      }

      // Get the SOURCE (not usually used)
      source = GFFFileColumns.SOURCE.extract(line, tabs[0] + 1, tabs[1]).toString();

        // Check the string pool
      //
      seqid = pool.getString(seqid);
      source = pool.getString(source);
      type = pool.getString(type);

      // Get the start and end base position for the annotation (1-based)
      startBasePosition = ((Integer) GFFFileColumns.START.extract(line, tabs[2] + 1, tabs[3])).intValue();
      endBasePosition = ((Integer) GFFFileColumns.END.extract(line, tabs[3] + 1, tabs[4])).intValue();

      // Get the score (usually zero)
      score = ((Float) GFFFileColumns.SCORE.extract(line, tabs[4] + 1, tabs[5])).floatValue();

      // Get the strand ( which is in { +, -, ., -} )
      strand = ((Character) GFFFileColumns.STRAND.extract(line, tabs[5] + 1, tabs[6])).charValue();

      // Get the phase ( only required for annotations with 'type = CDS' )
      phase = ((Byte) GFFFileColumns.PHASE.extract(line, tabs[6] + 1, tabs[7])).byteValue();

        // Process the attributes
      //
      attributes = GFFFileColumns.ATTRIBUTES.extract(line, tabs[7] + 1, Integer.MAX_VALUE).toString();
      Map<String, String> attributesMap = GFFRecord.parseAttributesString(attributes, pool);

        // 1 is subtracted from the base positions because the record in the
      // GFF file is 1-based and the GFFRecord object stores a 0-based index
      //
      GFFRecord gffrec = new GFFRecord(seqid, source, type, startBasePosition - 1, endBasePosition - 1, score, strand, phase);
      
      if (attributesMap != null && !attributesMap.isEmpty()) {
          for (Map.Entry<String, String> e : attributesMap.entrySet()) {
              gffrec.addAttribute(e.getKey(), e.getValue());
          }
      }
      return gffrec;
  }

  /***
   * Read in the annotations from the file.
   *
   * @return A list of the {@code GFFRecord}s
   * @throws IOException
   */
  public boolean processFile()
  {
      
    if (_gffFile == null || !_gffFile.exists()) {
        throw new IllegalArgumentException("The GFF file must exist !");
    }
    BufferedReader br = FileUtils.createBufferedReader( _gffFile );

    if ( br == null )
      return false;


    StringPool pool = new StringPool();
    String line = "";



    /*
     * Each line in the GFF file is split by first identifying the location of the tabs.
     * The index location of each of the eight tabs is stored in an array.
     * The tokens are then extracted using the substring(...) method.
     *
     * An alternative method would be to use a regex, e.g. Pattern p = Pattern.compile( "\t" ),
     * and use the p.split( line ).
     *
     * However, the latter method was found to be almost twice as slow for the
     * Ath_TAIR9 GFF file where only chromosome 1 was required and where the types
     * are in { "gene", "miRNA", "pseudogene", "transposable_element" }.
     */
    try
    {
      while ( ( line = br.readLine() ) != null )
      {
          if (!line.startsWith("#"))//ignore comment lines
          {
              GFFRecord gffrec = processLine(line, pool);
              if (gffrec != null)
              {
                  _gffRecords.add(gffrec);
              }
          }
      }
    }
    catch( IOException e )
    {
      _gffRecords.clear();

      WorkbenchLogger.LOGGER.log( Level.SEVERE, null, e );
      return false;
    }
    finally
    {
      IOUtils.closeQuietly( br );
      br = null;

      pool.clear();
    }

    return true;
  }

  public List<GFFRecord> getGFFRecords()
  {
    return Collections.unmodifiableList( _gffRecords );
  }

  public static void main( String[] args )
  {
    ThreadUtils.safeSleep( 1000 );

    String fileName = "D:/LocalData/hugh/seq_data/irina_data/TAIR9_GFF3_genes_transposons.gff";

    StopWatch sw = new StopWatch( "Tokenise GFF file : " + fileName );
    sw.start();

    for ( int i = 0; i < 5; ++i )
    {
      testReadInFile( fileName );
      sw.lap( "Read file" );

      ThreadUtils.safeSleep( 1000 );
      sw.lap( "Sleep" );
    }

    sw.stop();
    sw.printTimes();
  }

  private static void testReadInFile( String fileName )
  {
    GFFFileReader gfr = new GFFFileReader( new File( fileName ) );
//    gfr.addSequenceIdFilter( "Chr1" );

    if ( gfr.processFile() )
    {
      List<GFFRecord> gg = gfr.getGFFRecords();
    }
  }
}
