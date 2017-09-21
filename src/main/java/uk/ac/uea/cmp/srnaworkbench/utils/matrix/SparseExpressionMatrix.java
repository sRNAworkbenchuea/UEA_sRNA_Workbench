/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.matrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.*;

import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.viewers.AbundanceDistributionViewer;

/**
 *
 * @author w0445959
 */
public class SparseExpressionMatrix extends RunnableTool
{
  private final static String TOOL_NAME = "SPARSE_EXPRESSION_MATRIX";
  //patman list contains all the data sorted by start position and indexed by chromosome
  private Map<String, Patman> pList;
  //total genome hits is the total abundance for each file
  private HashMap<String, Integer> totalGenomeHitsPerFile = new HashMap<String, Integer>();
  //patman entries stores each sequence with its total hits to the genome
  private FastaMap patmanEntries;
  //file name to FastaMap for that file
  private HashMap<String, FastaMap> all_data = new HashMap<String, FastaMap>();
  //compressed data is a hashmap of string sequences indexed to a map of filename to abundance in file
  private HashMap<String, HashMap<String, Integer>> compressedData = new HashMap<String, HashMap<String, Integer>>();
  private File combinedFile;
  private File myGenomeFile;
  private ArrayList<ArrayList<File>> mySamples;
  private ArrayList<ArrayList<String>> myFilteredSampleNames = new ArrayList<ArrayList<String>>();
  private FilterResultsPanel myFilterResultsPane;
  int lengthRange[] = {Integer.MAX_VALUE, Integer.MIN_VALUE};
  
  // flags to enable checking of expression matrix status
  private boolean isBuilt;
  private boolean isPreprocessed;
  
  // Keeps track of the original sample names.
  private HashMap<String, String> sampleNameMap;
  
  //The actual data structure
  //maps Sequence -> Map<FileName, ExpressionElement>
  //each expression element contains a list of all coordinates for that sequence
  //also a list of abundances for each set of coordinates
  HashMap<String, HashMap<String,ExpressionElement>> data = 
    new HashMap<String, HashMap<String,ExpressionElement>>();
  
  //The actual data structure without entries containing more than one hit to the genome
  //maps Sequence -> Map<FileName, ExpressionElement>
  //each expression element contains a list of all coordinates for that sequence
  //also a list of abundances for each set of coordinates
  HashMap<String, HashMap<String,ExpressionElement>> dataNoMultiHits = 
    new HashMap<String, HashMap<String,ExpressionElement>>();

  //The actual data structure without entries containing more than one hit to the genome
  //maps Sequence -> Map<FileName, ExpressionElement>
  //each expression element contains a list of all coordinates for that sequence
  //also a list of abundances for each set of coordinates
  //this data structure stores the data chunks used for calculating the overlapping patterns
//  HashMap<String, HashMap<String,ExpressionElement>> dataChunk = 
//    new HashMap<String, HashMap<String,ExpressionElement>>();
  private final int minSizeMapped;
  private final int maxSizeMapped;
  private boolean completed = false;
  
  
  public SparseExpressionMatrix( StatusTracker tracker, FilterResultsPanel filterResults,
                                 ArrayList<ArrayList<File>> samples,
                                 File genome ){
    // FIXME: Setting min and max size to to default all sizes
    this (tracker, filterResults, samples, genome, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
   public SparseExpressionMatrix(StatusTracker tracker, FilterResultsPanel filterResults,
                                 ArrayList<ArrayList<File>> samples,
                                 File genome, int minSizeMapped, int maxSizeMapped)
  {
    super( TOOL_NAME, tracker );
    myFilterResultsPane = filterResults;
    this.mySamples = samples;
    this.myGenomeFile = genome;
    
    // All flags initially set to false
    this.isBuilt = false;
    this.isPreprocessed = false;
    this.sampleNameMap = new HashMap<>();
    
    // Pre-set the min and max sizes mapped.
    // This is required in order to use the tool's
    // process method
    this.minSizeMapped = minSizeMapped;
    this.maxSizeMapped = maxSizeMapped;
  }
  
  /**
   * Resolve initial sample names to their filtered counterparts
   * @param sampleName a valid sample name
   * @return The name of the filtered file or the sample. If the
   * sample name given does not match any of the original sample names,
   * the input sample name is simply returned.
   */
  public String resolveSampleName(String sampleName)
  {
    if (this.sampleNameMap.containsKey( sampleName ))
    {
      return (this.sampleNameMap.get( sampleName ));
    }
    else
    {
      if (this.sampleNameMap.containsValue( sampleName ))
      {
        return (sampleName);
      }
      else
      {
        return (null);
      }
    }
  }

  public void preProcess( int minSize, int maxSize ) throws IOException
  {
    // Checks if the matrix was already preprocessed
    if(!this.isPreprocessed)
    {
      removeLowComplexity();
      compressToOneFile();
      //a map keyed by chromosome ID, each set of sequences is sorted by start coordinate
      mapReads( minSize, maxSize );

      //createExpressionMatrix(mappedReads);

      this.isPreprocessed = true;
    }
    else
    {
      // post a warning. This behaviour might be undesirable, in which case it should be changed
      LOGGER.log( Level.WARNING, "SparseExpressionMatrix has already been preprocessed");
    }
  }
  
  public AbundanceDistributionViewer generateAbundancePlot()
  {
    
    AbundanceDistributionViewer myAbundDist = AbundanceDistributionViewer.generateAbundanceDistribution();
    myAbundDist.setTitle( "Size Class Distribution : CoLIde");

    myAbundDist.inputData( all_data, "All Data Combined");
    myAbundDist.initiliseSizeClassDistribution( );
    
    return myAbundDist;
    //JOptionPane
  }

  private void removeLowComplexity() throws IOException
  {
    File tempDir = Tools.getNextDirectory();
    FilterParams filterParams = new FilterParams.Builder().
      setFilterInvalidSeq( true ).setOutputNonRedundant( true ).setMinAbundance( 1 ).build();
    
//        FilterParams filterParams = new FilterParams.Builder().setFilterLowComplexitySeq( true ).
//      setFilterInvalidSeq( true ).setMakeNonRedundant( true ).setMinAbundance( 1 ).build();
//    
    int sampleID = 0;
    for ( ArrayList<File> mySampleFiles : mySamples )
    {
      ArrayList<String> newListOfFileNames = new  ArrayList<String>();
      myFilteredSampleNames.add( newListOfFileNames );
      for ( File toFilter : mySampleFiles )
      {
        File output = new File( tempDir.getAbsolutePath() + DIR_SEPARATOR + FilenameUtils.removeExtension( toFilter.getName() ) + "_" + sampleID + "_filtered.fa" );
        Filter f = new Filter( toFilter, output, tempDir, filterParams, getTracker(), JOptionPane.NO_OPTION );
        f.run();
        
        // If tracker does not exist skip this.
        // Added so that SparseExpressionMatrix can be used with null tracker
        if (getTracker() != null){
          getTracker().reset();
        }

        if ( myFilterResultsPane != null )
        {
          
          myFilterResultsPane.fillResultsTable( toFilter.getName(), f.getStats());
          myFilterResultsPane.setResultsWaiting( toFilter.getName(), false);
//          FilterResultsPanel newResults = new FilterResultsPanel();
//          newResults.fillResultsTable( f.getStats() );
//         ((JScrollPane)myFilterResultsPane.getComponentAt( sampleID )).setViewportView(  newResults );
        }
        SRNAFastaReader newReader = new SRNAFastaReader( output );
        all_data.put( output.getName(), new FastaMap( newReader.process() ) );
        myFilteredSampleNames.get( sampleID ).add( output.getName() );
        
        //hash a link oldname -> newname
        this.sampleNameMap.put(toFilter.getName(), output.getName());
      }
      sampleID++;
    }
  }

  public ArrayList<ArrayList<String>> getFileNames()
  {
    return myFilteredSampleNames;
  }
  
  /**
   * @return filenames in a flattened array. For use when grouping replicates
   * is not required, i.e. when looping over all samples, treating each one equally
   */
  public ArrayList<String> getFlattenedFileNames()
  {
    ArrayList<String> flattened = new ArrayList<>();
    for(ArrayList<String> sampleGroup : this.getFileNames())
    {
      for(String sample : sampleGroup)
      {
        flattened.add( sample );
      }
    }
    return flattened;
  }
  
  public void printElements(int numberOfRowsToPrint)
  {
    int i = 0;
    for(Entry<String, HashMap<String, ExpressionElement>> entry : 
      new TreeMap<String, HashMap<String, ExpressionElement>>(data).entrySet())
    {
      
      if(i > numberOfRowsToPrint)
        return;
      System.out.print( entry.getKey() + " | ");
      HashMap<String, ExpressionElement> row = entry.getValue();

      for ( Map.Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        System.out.println( rowEntry.getKey() + " | " + rowEntry.getValue() );
      }
      i++;
      
    }
  }
  public void printSortedElements( int numberOfRowsToPrint )
  {
    int i = 0;
    for(Entry<String, HashMap<String, ExpressionElement>> entry : 
      new TreeMap<String, HashMap<String, ExpressionElement>>(this.dataNoMultiHits).entrySet())
    {
      
      if(i > numberOfRowsToPrint)
        return;
      
      HashMap<String, ExpressionElement> row = entry.getValue();

      System.out.print( entry.getKey() + " | ");
      if(row.size() > 1)
        System.out.println( "Sequence appears in more than one file" );
      for ( Map.Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        System.out.println( rowEntry.getKey() + " | " + rowEntry.getValue() );
      }
      i++;
      
    }
  }
  public void printElements(String UDSstringToFind)
  {

    for(Entry<String, HashMap<String, ExpressionElement>> entry : 
      new TreeMap<String, HashMap<String, ExpressionElement>>(data).entrySet())
    {
      
      
      HashMap<String, ExpressionElement> row = entry.getValue();

      for ( Map.Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        if(rowEntry.getValue().UDS_pattern.contains( UDSstringToFind) )
        {
          System.out.print( entry.getKey() + " | ");
          System.out.println( rowEntry.getKey() + " | " + rowEntry.getValue() );
        }
      }
    }
  }
  public HashMap<String,ExpressionElement> get(String key)
  {
    return data.get( key );
  }
  /**
   * compresses all the filtered data into a hashmap also creates a single
   * patman file for the compressed data hashmap compressed data contains:
   *
   * Key = Sequence
   *
   */
  private void compressToOneFile() throws IOException
  {



    //for each data set contained in the array
    //data contains FILENAME -> FastaMap
    //each FastaMap is each Sequence -> FastaRecord
    //final construction of compressed data results in a map:
    // Sequence -> Map<Filename, Abundance>
    for ( Map.Entry<String, FastaMap> dataEntry : all_data.entrySet() )
    {
      /*
       * iterate through the data checking to see if we have found it if we
       * found it then increment value if not create new entry
       */
      this.trackerInitKnownRuntime( "Merging data...", dataEntry.getValue().size() );
//
      for ( Map.Entry<String, FastaRecord> entry : dataEntry.getValue().entrySet() )
      {

        this.trackerIncrement();
        //System.out.println("Seq: " + entry.getKey());
        // if compressed contains this sequence
        if ( compressedData.containsKey( entry.getKey() ) )
        {
          // then get the Map of file -> expression for this sequence
          HashMap<String, Integer> valueMap = compressedData.get( entry.getKey() );
          // if the map does not contain the filename already
          if ( !valueMap.containsKey( dataEntry.getKey() ) )
          {
            // ad the filename -> abundance entry
            valueMap.put( dataEntry.getKey(), entry.getValue().getAbundance() );
          }
          // put the valuemap back in compressedData
          //compressedData.put( entry.getKey(), valueMap );
        }
        else
        {
          // if compressed does not contain the sequence, create a new map
          HashMap<String, Integer> newMap = new HashMap<String, Integer>();
          // ad the filename -> abundance entry
          newMap.put( dataEntry.getKey(), entry.getValue().getAbundance() );
          
          // place this new single-entry map as an initialised sequence -> map entry
          compressedData.put( entry.getKey(), newMap );
        }
      }
    }
    combinedFile = new File( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "combined_data.fa" );
    if ( combinedFile.createNewFile() )
    {
      LOGGER.log( Level.FINE, "created new combined file" );
    }



    FileWriter outFile = new FileWriter( combinedFile );

    PrintWriter out = new PrintWriter( outFile );

    for ( Map.Entry<String, HashMap<String, Integer>> compressedDataEntry : compressedData.entrySet() )
    {
      //int totalAbund = 0;
      String abundanceOut = "";
      for ( Map.Entry<String, Integer> entry : compressedDataEntry.getValue().entrySet() )
      {
        //totalAbund += entry.getValue();
        abundanceOut += entry.getKey() + "(" + entry.getValue() + ")-";
      }
      out.println( ">" + compressedDataEntry.getKey() + "-" + abundanceOut );
      out.println( compressedDataEntry.getKey() );

    }
    outFile.close();
    out.close();


    this.trackerReset();

  }
  
  public void writeToCsv(File outFile, NormalisationType normtype) throws IOException
  {
    FileWriter outFW = new FileWriter(outFile);
    PrintWriter outPW = new PrintWriter(outFW);
    
    String line = "read";
    for(ArrayList<File> sampleList : this.mySamples)
    {
      for(File aSample : sampleList)
      {
        line += "," + aSample.getName();
      }
    }
    outPW.println(line);
    
    ArrayList<String> sortedSequences = new ArrayList<>();
    sortedSequences.addAll( this.data.keySet() );
    Collections.sort( sortedSequences );
    
    //for(Entry<String, HashMap<String, ExpressionElement>> entry : this.getEntries())
    for(String sequence : sortedSequences)
    {
      //String sequence = entry.getKey();
      HashMap<String, ExpressionElement> sampleMap = this.get( sequence );
      
      line = sequence;
      for(ArrayList<String> sampleList : this.getFileNames())
      {
        for(String aSample : sampleList)
        {
          ExpressionElement thisElement;
          if(sampleMap.containsKey(aSample))
          {
            thisElement = sampleMap.get( aSample );
            double thisAbundance = thisElement.getNormalisedAbundance( normtype );
            DecimalFormat ab = new DecimalFormat("0.00");
            ab.setRoundingMode( RoundingMode.DOWN );
           line += "," + ab.format(thisAbundance);
 //           line += "," + String.format("%.2f", Float.parseFloat( ab.format(thisAbundance) ));
          }
          else
          {
            // Sequence is not in this sample. print 0
            line += ",0.00";
          }
        }
      }
      outPW.println(line);
    }
    outPW.close();
    outFW.close();
  }
  
  public void printMatrix(NormalisationType normtype)
  {
    int longestSeq = 0;
    for(String seq : this.data.keySet())
    {
      longestSeq = Math.max( seq.length(), longestSeq);
    }
    String seqPad = "%" + longestSeq + "s";
    
    String line = String.format( seqPad, "read" );
    
    for(ArrayList<File> sampleList : this.mySamples)
    {
      for(File aSample : sampleList)
      {
        line += " " + String.format(seqPad, aSample.getName());
      }
    }
    System.out.println(line);
    
    // Sort sequences lexicographically for evaluation purposes
    ArrayList<String> sortedSequences = new ArrayList<>();
    sortedSequences.addAll( this.data.keySet() );
    Collections.sort( sortedSequences );
    
    //for(Entry<String, HashMap<String, ExpressionElement>> entry : this.getEntries())
    for(String sequence : sortedSequences)
    {
      //String sequence = entry.getKey();
      HashMap<String, ExpressionElement> sampleMap = this.get( sequence );
      
      line = String.format(seqPad, sequence);
      for(ArrayList<String> sampleList : this.getFileNames())
      {
        for(String aSample : sampleList)
        {
          ExpressionElement thisElement;
          if(sampleMap.containsKey(aSample))
          {
            thisElement = sampleMap.get( aSample );
            double thisAbundance = thisElement.getNormalisedAbundance( normtype );
            DecimalFormat ab = new DecimalFormat("#.##");
            ab.setRoundingMode( RoundingMode.DOWN );
            
            line += " " + String.format("%"+aSample.length()+".2f", Float.parseFloat( ab.format(thisAbundance)));
            
          }
          else
          {
            // Sequence is not in this sample. print 0
            line += String.format("%"+aSample.length() + ".2f", 0f);
          }
        }
      }
      System.out.println(line);
    }
  }

  public File getGenomeFile()
  {
    return myGenomeFile;
  }
  private void mapReads( int minSize, int maxSize ) throws IOException
  {
    this.trackerInitUnknownRuntime( "Aligning merged file to genome..." );


    String finalTempFilePath = Tools.CoLIDE_dataPath + DIR_SEPARATOR + myGenomeFile.getName() + ".patman";

    File patman_out = new File( finalTempFilePath );

    //File tempDIR = new File(Tools.miRCat_dataPath  );

    PatmanParams newP_Params = new PatmanParams();
    newP_Params.setPreProcess( false );
    newP_Params.setPostProcess( false );
    //newP_Params.setOriginalHeader( true );
    Thread myThread = new Thread( new PatmanRunner( combinedFile, myGenomeFile, 
      patman_out, Tools.getNextDirectory(), newP_Params ) );

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


    this.trackerInitUnknownRuntime( "Status: Building initial map ..." );


    Patman p = new PatmanReader( patman_out ).process( minSize, maxSize,
      Integer.MIN_VALUE );
    //patman entries final FASTA map is
    patmanEntries = p.buildFastaMap();
    this.trackerInitUnknownRuntime( "Status: Building Chromosome Map..." );


    pList = p.buildChromosomeMap();


    for ( Map.Entry<String, FastaMap> actualData : all_data.entrySet() )
    {

      int currentTotal = 0;
      for ( Map.Entry<String, FastaRecord> entry : actualData.getValue().entrySet() )
      {
        if(patmanEntries.containsKey( entry.getValue().getSequence()) )
          currentTotal += entry.getValue().getAbundance();
      }
      totalGenomeHitsPerFile.put( actualData.getKey(), currentTotal );
        System.out.println("file: " + actualData.getKey() + "total: " + currentTotal);
    }



    this.trackerInitKnownRuntime( "Sorting hits...", pList.size() );

    for ( Map.Entry<String, Patman> entry : pList.entrySet() )
    {
      this.trackerIncrement();
      entry.getValue().sortByStartCoord();
    }

    //all_data = null;
    System.gc();

    this.trackerReset();

  }
  
  public int getRowCount()
  {
    return data.size();
  }
  public int getColumnCount()
  {
    //amount of colums is the amount of files + one column for the sequence
    return totalGenomeHitsPerFile.keySet().size() + 1;
  }
  public Set<Entry<String, HashMap<String, ExpressionElement>>> getEntries()
  {
    //return new TreeMap<String, HashMap<String, ExpressionElement>>(data).entrySet();
    return (data).entrySet();
  }
  
  public Iterator<Entry<String, ExpressionElement>> getSampleIterator(String sampleName)
  {
      return new SampleIterator(this, sampleName);
  }

  public ArrayList<Integer> getLengthRanges()
  {
    ArrayList<Integer> range = new ArrayList<Integer>();
    for(int start = lengthRange[0] ; start <= lengthRange[1]; start++ )
    {
      range.add( start );
    }
    return range;
  }
  public void build() throws Exception
  {
    if (!isBuilt)
    {
      for ( Map.Entry<String, Patman> entry : new TreeMap<String, Patman>(pList).entrySet() )
      {
        this.trackerInitKnownRuntime( "building chromosome " + entry.getKey() + "...", entry.getValue().size() );
        for(PatmanEntry value : entry.getValue())
        {
          this.trackerIncrement();
          String theSequence = value.getSequence();

  //        if(theSequence.equals( "ATAGGCTCTTGAATCACTAGA") )
  //          System.out.println( "" );
          HashMap<String, Integer> allFilesForSequence = 
            this.compressedData.get( theSequence );
          if(theSequence.length() < lengthRange[0])
            lengthRange[0] = theSequence.length();
          if(theSequence.length() > lengthRange[1])
            lengthRange[1] = theSequence.length();


          if(allFilesForSequence != null )//found any files for this sequence?
          {
            HashMap<String, ExpressionElement> currentRow = data.get(theSequence);

            if( currentRow != null )//does the main data structure already have a record?
            {
              //pull each element in the row back indexed by it's file name
              String buildPrintString = "";
              for(Entry<String, Integer> fileToAbundance : new TreeMap<String, Integer>(allFilesForSequence).entrySet())
              {
                ExpressionElement currentElement = currentRow.get( fileToAbundance.getKey());
                //as matrix is sparse perhaps an element doesnt exist for this value?
                if(currentElement != null)
                {
                  currentElement.endCoord.add( value.getEnd() );
                  currentElement.startCoord.add( value.getStart() );
                }
                else
                {

                  ExpressionElement newElement = new ExpressionElement();
                  newElement.chromosomeID = entry.getKey();
                  newElement.endCoord.add( value.getEnd() );
                  newElement.startCoord.add( value.getStart() );
                  newElement.originalAbundance = fileToAbundance.getValue();
                  newElement.sample_name = fileToAbundance.getKey();
                  String[] splitName = fileToAbundance.getKey().split( "_");
                  newElement.sample_ID = splitName[splitName.length-2];
                  buildPrintString += fileToAbundance.getKey() + "(" + fileToAbundance.getValue() + ")-";
                  newElement.printableSampleName = buildPrintString;
                  newElement.sequence = theSequence;
                  newElement.strand = value.getSequenceStrand();

                  currentRow.put( fileToAbundance.getKey(), newElement );

                }
              }
            }
            else//make a new row in ExpressionMatrix
            {


              HashMap<String, ExpressionElement> newRow = 
                new HashMap<String, ExpressionElement>();
              String buildPrintString = "";
              for(Entry<String, Integer> fileToAbundance : new TreeMap<String, Integer>(allFilesForSequence).entrySet())
              {

                ExpressionElement newElement = new ExpressionElement();
                newElement.chromosomeID = entry.getKey();
                newElement.endCoord.add( value.getEnd() );
                newElement.startCoord.add( value.getStart() );
                newElement.originalAbundance = fileToAbundance.getValue();
                newElement.sample_name = fileToAbundance.getKey();
                String[] splitName = fileToAbundance.getKey().split( "_");
                newElement.sample_ID = splitName[splitName.length-2];
                buildPrintString += fileToAbundance.getKey() + "(" + fileToAbundance.getValue() + ")-";
                newElement.printableSampleName = buildPrintString;
                newElement.sequence = theSequence;
                newElement.strand = value.getSequenceStrand();
                newRow.put( fileToAbundance.getKey(), newElement);
              }
              HashMap<String, ExpressionElement> put = data.put( theSequence , newRow);

              if(put != null)
              {
                throw new Exception("failed to build matrix correctly due to row overwrite...");
              }
              //
            }


          }
        }
        this.trackerReset();

      }

      //compressedData = null;
      System.gc();
      this.isBuilt = true;
    }
    else
    {
      LOGGER.log(Level.WARNING, "SparseExpressionMatrix has already been built");
    }
    //now build the matrix containing values that only hit the genome once


//    for ( Map.Entry<String, HashMap<String, ExpressionElement>> entry : getEntries() )
//    {
//      HashMap<String, ExpressionElement> row = entry.getValue();
//
//      boolean addThisRow = true;
//      for ( Entry<String, ExpressionElement> EE : row.entrySet() )
//      {
//        if ( EE.getValue().startCoord.size() != 1 )
//        {
//          addThisRow = false;
//        }
//      }
//      
//      if(addThisRow)
//      {
//        dataNoMultiHits.put( entry.getKey(), row );
//      }
//    }
//    
//    sortData();
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Sequence | ");
    for(Entry<String, FastaMap> entry : all_data.entrySet())
    {
      sb.append( entry.getKey()).append( " | ");
    }
    sb.append(LINE_SEPARATOR);
    for(Entry<String, HashMap<String, ExpressionElement>> entry : new TreeMap<>(data).entrySet())
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      sb.append(entry.getKey()).append( " | ");
      for(Entry<String, ExpressionElement> elements : row.entrySet())
      {
        sb.append( elements.getKey() ).append( elements.getValue()).append( " | ");
      }
      sb.append( LINE_SEPARATOR);
    }
    
    return sb.toString();
  }

  public double getTotalGenomeHit(String filename)
  {
    return totalGenomeHitsPerFile.get( filename );
  }
  public double countGenomeHits( String toCheck )
  {

    if ( this.patmanEntries.containsKey( toCheck ) )
    {
      return patmanEntries.get( toCheck ).getHitCount();
    }
    return 0;
  }

  @Override
  protected void process() throws Exception
  {
    // this method is enabled in order to build a matrix in its own thread like
    // how other tools work. NormalisationRunner uses this to pre-build the matrix
    // just once before using the same matrix to normalise several times.
    this.completed = false;
    this.preProcess( minSizeMapped, maxSizeMapped );
    this.build();
    this.completed = true;
    //throw new UnsupportedOperationException( "Not supported yet." );
  }
  
  public boolean isCompleted()
  {
    return this.completed;
  }

  public void sortData()
  {
    //ArrayList<ArrayList<String>> samples = getFileNames();
    LinkedHashMap<String, HashMap<String, ExpressionElement>> sortedByValue = sortByValue(this.dataNoMultiHits);
    
    int i = 0;
    for ( Iterator<Entry<String, HashMap<String, ExpressionElement>>> it = (sortedByValue).entrySet().iterator(); it.hasNext(); )
    {
      Entry<String, HashMap<String, ExpressionElement>> entry = it.next();
      if(i > 500)
        return;
      System.out.print( entry.getKey() + " | ");
      HashMap<String, ExpressionElement> row = entry.getValue();
      for ( Map.Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        System.out.println( rowEntry.getKey() + " | " + rowEntry.getValue() );
      }
      i++;
    }
  }
  
  private static LinkedHashMap<String, HashMap<String, ExpressionElement>> 
    sortByValue( HashMap<String, HashMap<String, ExpressionElement>> map)
  {
    List<Entry<String, HashMap<String, ExpressionElement>>> list = new LinkedList<Entry<String, HashMap<String, ExpressionElement>>>( map.entrySet() );
    Collections.sort( list, new Comparator<Entry<String, HashMap<String, ExpressionElement>>>()
    {
      @Override
      public int compare( Entry<String, HashMap<String, ExpressionElement>> o1, Entry<String, HashMap<String, ExpressionElement>> o2 )
      {
        HashMap<String, ExpressionElement> k1 =  o1.getValue();
        HashMap<String, ExpressionElement> k2 =  o2.getValue() ;
        
        return  (k1.entrySet().iterator().next().getValue().startCoord.get( 0) ).
          compareTo( (k2.entrySet().iterator().next().getValue().startCoord.get( 0)) );
      }
    } );

    LinkedHashMap<String, HashMap<String, ExpressionElement>> result = new LinkedHashMap<String, HashMap<String, ExpressionElement>>();
    for ( Iterator<Entry<String, HashMap<String, ExpressionElement>>> it = list.iterator(); it.hasNext(); )
    {
      Map.Entry<String, HashMap<String, ExpressionElement>> entry = (Map.Entry<String, HashMap<String, ExpressionElement>>) it.next();
      result.put( entry.getKey(), entry.getValue() );
    }
    return result;
  }

   /**
   * gets entries sorted on start coordinate.
   * No entries that hit the genome multiple times are included
   *
   */
  public Set<Entry<String, HashMap<String, ExpressionElement>>> getSortedEntries()
  {
    return dataNoMultiHits.entrySet();
  }

  public Set<String> getChromoIDS()
  {
    return pList.keySet();
  }
  public int getMaxCoordForChrom(String chromoID)
  {
    return pList.get( chromoID ).getMaxEndCoord();
  }
   public int getMinCoordForChrom( String chromoID )
  {
    return pList.get( chromoID ).getMinStartCoord();
  }
    
  public Set<Entry<String, HashMap<String, ExpressionElement>>> getSortedChunk( int minCoord, int maxCoord, String chromoID, int minChunkSize)
  {
    //dataChunk.clear();
    HashMap<String, HashMap<String,ExpressionElement>> dataChunk = 
    new HashMap<String, HashMap<String,ExpressionElement>>();

    Patman chromoData = pList.get( chromoID );

    //retrieve all sequences from the original patman data
    //loop through them and pull back the value from the expression matrix

    Patman dataRange = chromoData.getRangeOverlap( chromoID, minCoord, maxCoord );
    //Iterator<PatmanEntry> patmanIterator = dataRange.iterator();
    //for ( PatmanEntry value :  dataRange)
    //for(patmanIterator)

    for(Iterator<PatmanEntry> it = dataRange.iterator(); 
     it.hasNext();)
    {
      PatmanEntry value = it.next();

      HashMap<String, ExpressionElement> row = this.data.get( value.getSequence() );
      Iterator<ExpressionElement> iterator = row.values().iterator();
//
//      if(chromoID.equals( "3") && value.getStart() == 5840113 )
//        System.out.println( "" );
      ExpressionElement EE_for_row = iterator.next();
      //simple part here, sequence only hit once to the genome so add it's EE and move on
      if ( EE_for_row.startCoord.size() == 1 )
      {
        dataChunk.put( value.getSequence(), row );
      }
      //not so simple, the sequence hit more than once so we must find the coords that 
      //fit the desired sequence range and create a new EE to add to the list
      else
      {
//        ExpressionElement copyMe = EE_for_row;
//        //create deep copy of data structure
//        ExpressionElement addMe = new ExpressionElement(copyMe);
        
        ExpressionElement copyMe = EE_for_row;
        ExpressionElement addMe = new ExpressionElement();
        //create deep copy of data structure
        addMe.UDS_pattern = copyMe.UDS_pattern;
        addMe.chromosomeID = copyMe.chromosomeID;
        addMe.sample_name = copyMe.sample_name;
        addMe.originalAbundance = copyMe.originalAbundance;
        addMe.normalisedAbundance = copyMe.normalisedAbundance;
        addMe.weightedAbundance = copyMe.weightedAbundance;
        addMe.confidenceInterval[0] = copyMe.confidenceInterval[0];
        addMe.confidenceInterval[1] = copyMe.confidenceInterval[1];
        addMe.sample_ID = EE_for_row.sample_ID;
        addMe.printableSampleName = EE_for_row.printableSampleName;
        addMe.sequence = EE_for_row.sequence;
        //find the coords that are in the correct range
        addMe.sample_ID = EE_for_row.sample_ID;
        addMe.printableSampleName = EE_for_row.printableSampleName;
        addMe.sequence = EE_for_row.sequence;
        addMe.strand = EE_for_row.strand;
        //find the coords that are in the correct range

        for ( int i = 0; i < copyMe.startCoord.size(); i++ )
        {
          int start = copyMe.startCoord.get( i );
          int end = copyMe.endCoord.get( i );
          if (( end <= maxCoord ) && ( start >= minCoord ))
          {
            addMe.startCoord.add( start );
      
            addMe.endCoord.add( end );
          }
        }
        //only one mapping coord fit within the range?
        if ( ( addMe.startCoord.size() == 1 ) && ( addMe.endCoord.size() == 1 ) )
        {
          HashMap<String, ExpressionElement> tempRow = new HashMap<String, ExpressionElement>();
          tempRow.put( EE_for_row.sample_name, addMe );
          dataChunk.put( value.getSequence(), tempRow );
        }
        //the sequence mapped more than once with the larger chunk size so we return nothing and start again
        else if(maxCoord - minCoord != minChunkSize)
        {
            return null;
        }
        else//the sequence is mapping many times in close proximity so we will just ignore the whole chunk...
        {
          
          dataChunk.clear();
          return dataChunk.entrySet();
        }
        // else (no need for actual statement)
        

      }
    }
    
   
    return sortByValue(dataChunk).entrySet();
    
  }

  public Map<String, Patman> getPatman()
  {
    return this.pList;
  }
  public HashMap<String, HashMap<String, Integer>> getCompressed()
  {
    return this.compressedData;
  }

  public Map<String, Integer> getCompressedValue( String sequence )
  {
    return compressedData.get( sequence );
  }
  
  public void generateStatistics()
  {
    //fist statistics are a break down, per file of each length and R NR counts of genome alignment
    HashMap<String, HashMap<Integer, List<Double>>> stats_per_file = new HashMap<>();
    for ( String filename : this.totalGenomeHitsPerFile.keySet() )
    {
      HashMap<Integer, List<Double>> genome_hits_breakdown = new HashMap<>();
      for ( int size_class = lengthRange[0]; size_class <= lengthRange[1]; size_class++ )
      {
        ArrayList<Double> counts = new ArrayList<>( 2 );
        counts.add( 0.0 );
        counts.add( 0.0 );
        genome_hits_breakdown.put( size_class, counts );
      }
      stats_per_file.put( filename, genome_hits_breakdown );
    }
    for(Entry<String, HashMap<String, ExpressionElement>> entry : new TreeMap<>(data).entrySet())
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      //sb.append(entry.getKey()).append( " | ");
      int length = entry.getKey().length();
//      List<Integer> counts = genome_hits_breakdown.get( length );
//      counts.set( 0, counts.get( 0)+1);
//      counts.set( 1, counts.get( 1) + )
//      if(row.size() > 1)
//        System.out.println( "" );
      for(Entry<String, ExpressionElement> elements : row.entrySet())
      {
        //System.out.println( "" );
        HashMap<Integer, List<Double>> genome_hits_breakdown = stats_per_file.get( elements.getKey());
        List<Double> counts = genome_hits_breakdown.get( length );
        counts.set( 0, counts.get( 0 ) + 1 );
        counts.set( 1, counts.get( 1 ) + elements.getValue().originalAbundance);
      }
    }
    
    for(Entry<String, HashMap<Integer, List<Double>>> entry : new TreeMap<>(stats_per_file).entrySet())
    {
      System.out.println( "FileName: " + entry.getKey() );
      System.out.println( "Size Class,Unique,Total" );
      for(Entry<Integer, List<Double>> sizeToStats : new TreeMap<>(entry.getValue()).entrySet())
      {
        System.out.println( sizeToStats.getKey() + "," + sizeToStats.getValue().get( 0 ) + "," + sizeToStats.getValue().get(1)  );
      }
    }
    
  }
  
  /**
   * Debugging method that returns a quick expression matrix for testing code.
   * @return 
   */
  public static SparseExpressionMatrix getTestMatrix() throws Exception
  {

     
      StatusTracker tracker = new StatusTracker(null, null);
      
      ArrayList<File> samples = new ArrayList<>();

      samples.add( new File( "src/test/data/norm/ath_366868_head.fa" ) );
      samples.add( new File( "src/test/data/norm/ath_366868_head2.fa" ) );

      ArrayList<ArrayList<File>> sampleList = new ArrayList<>();
      sampleList.add( samples );

      File genome = new File( "TutorialData/FASTA/GENOME/Ath_TAIR9.fa" );
      
      SparseExpressionMatrix sem = new SparseExpressionMatrix(tracker, null, sampleList, genome);
      
      sem.preProcess( 16, 40);
      sem.build();
      return(sem);

    
  }
  
  public static void test()
  {
       try
    {
    
      StatusTracker tracker = new StatusTracker(null, null);
        
      ArrayList<ArrayList<File>> mySamples = new ArrayList<>();
      ArrayList<File> sample1 = new ArrayList<>();
      ArrayList<File> sample2 = new ArrayList<>();
      ArrayList<File> sample3 = new ArrayList<>();
      ArrayList<File> sample4 = new ArrayList<>();
//      sample1.add( new File( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr_small.fa"));
//      sample2.add( new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154336_carrington_col0_flower_nr_small.fa") );
      sample1.add( new File( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM118373_Rajagopalan_col0_leaf_nr.fa"));
      sample2.add( new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154336_carrington_col0_flower_nr.fa") );
      sample3.add( new File( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM154370_carrington_col0_leaf_nr.fa"));
      sample4.add( new File("/Developer/Applications/sRNAWorkbench/TestingData/AtH/Arabidopsis_with_replicates/GSM154336_carrington_col0_flower_nr/GSM257235_DCB_col0_flower_nr.fa") );
      
//      sample1.add( new File( "/Volumes/ExFAT/Darrell/Outputs/Normal_bone_ACTTGA_L002_R1_AR.fa"));
//      sample2.add( new File( "/Volumes/ExFAT/Darrell/Outputs/Mesenchymal_tumour_GTTTCG_L002_R1_AR.fa"));
      mySamples.add( sample1 );
      mySamples.add( sample2 );
      mySamples.add( sample3 );
      mySamples.add( sample4 );
     
        StopWatch sw = new StopWatch("count HM times");
        sw.start();
      
      //File myGenomeFile = new File("/Volumes/ExFAT/Darrell/hg38.fa");
      File myGenomeFile = new File( "/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas" );
      SparseExpressionMatrix expressionMatrix =
        new SparseExpressionMatrix(tracker, null,
          mySamples, myGenomeFile);
      
      // Stop the stop watch.

        expressionMatrix.preProcess(16, 60);
        expressionMatrix.build();
        NormalisationProcess newNormaliser = new NormalisationProcess(NormalisationType.TOTAL_COUNT, expressionMatrix, tracker, FileUtils.getTempDirectory());
        newNormaliser.run();
              sw.stop();
      sw.printTimes();
      System.out.println(expressionMatrix.toString());

//      expressionMatrix.build();
//      //expressionMatrix.generateStatistics();
//      System.out.println( expressionMatrix.toString() );
//      System.out.println( "" );
//    Thread newThread = new Thread( new Runnable()
//    {
//      @Override
//      public void run()
//      {
//
//        generateAbundancePlot = expressionMatrix.generateAbundancePlot();
//
//        generateAbundancePlot.revalidate();
//
//      }
//    } );
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage() );
    }
    catch ( Exception ex )
    {
       LOGGER.log(Level.SEVERE, ex.getMessage() );
    }

  }
  
  public static void main(String[] args)
  {
    
    test();
   
   
  }
  
    /**
     * An Iterator to simplify iterating over independent samples of a
     * SparseExpressionMatrix
     *
     * Since the matrix is stored and iterated over using the sequence as the
     * outer key, it can be a bit of work to separate out the samples. This
     * simplifies the coding of looping over individual samples
     *
     * @author Matthew Beckers
     */
    class SampleIterator<T extends Entry<String, ExpressionElement>> implements Iterator<Entry<String, ExpressionElement>>
    {

        String sample;
        Iterator<Entry<String, HashMap<String, ExpressionElement>>> sequenceEntriesIt;
        Entry<String, HashMap<String, ExpressionElement>> current;
        boolean isNext = false;

        public SampleIterator(SparseExpressionMatrix sem, String sample)
        {
            this.sequenceEntriesIt = sem.getEntries().iterator();
            this.sample = sample;
        }

        /**
         * Checks to see if there is another sequence, and if that sequence is
         * in this sample. Otherwise, it skips to the next sequence and does the
         * same.
         *
         * @return true if there is another sequence in this sample
         */
        @Override
        public boolean hasNext()
        {
            // First check if there are any more sequences in the matrix
            if (sequenceEntriesIt.hasNext())
            {
                // There is another sequence. Retrieve it.
                current = sequenceEntriesIt.next();

                // Check if the sequence belongs to our sample
                if (!current.getValue().containsKey(sample))
                {
                // Sequence is not in our sample. Skip this sequence and
                    // try the next one
                    return hasNext();
                } else
                {
                // There is a sequence in this sample. Label the current
                    // sequence as being the next one for returning
                    isNext = true;

                    // There is a next sequence in this sample
                    return true;
                }
            } else
            {
            // There are no more sequences in the matrix, and so no more
                // sequences in the sample
                return false;
            }
        }

        /**
         * Returns the next sequence with it's ExpressionElement
         *
         * @return an Entry<String, ExpressionElement> of sequence and
         * expression pair
         * @throws NoSuchElementException if you call this when there are no
         * elements left
         */
        @Override
        public Entry<String, ExpressionElement> next()
        {
            // has the current sequence already been returned using next()?
            if (isNext)
            {
                // current sequence has not yet been returned. Return it.
                isNext = false;
                return new AbstractMap.SimpleEntry<>(current.getKey(), current.getValue().get(sample));
            } else
            {
            // current sequence was already returned using next()
                // get another current sequence using hasNext()
                if (hasNext())
                {
                    // call next() again
                    return next();
                } else
                {
                    throw new NoSuchElementException();
                }

            }
        }
    }


 
}