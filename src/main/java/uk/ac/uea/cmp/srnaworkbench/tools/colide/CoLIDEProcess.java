/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.colide;

import java.awt.EventQueue;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import org.apache.commons.io.FileUtils;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.CoLIDEParams.SERIES_TYPE_ENUM;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationProcess;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.viewers.AbundanceDistributionViewer;

/**
 *
 * @author w0445959
 */
public class CoLIDEProcess extends RunnableTool
{
  private final static String TOOL_NAME = "COLIDE";
  private File myGenomeFile;
  private ArrayList<ArrayList<File>> mySamples;
  private FilterResultsPanel myFilterTabs;

  private final CoLIDEParams params;
  private SparseExpressionMatrix expressionMatrix;
  private final StatusTracker myTracker;
  private CoLIDEResultsPanel myResultsTable = null;
  AbundanceDistributionViewer generateAbundancePlot = null;

  public CoLIDEProcess( StatusTracker tracker )
  {
    this( tracker, null, null, new CoLIDEParams() );

  }

  public CoLIDEProcess( StatusTracker tracker, ArrayList<ArrayList<File>> inputFiles, File genomeFile, CoLIDEParams params )
  {
    super( TOOL_NAME, tracker );
    myTracker = tracker;
    mySamples = inputFiles;
    myGenomeFile = genomeFile;
    this.params = params;
    
    Tools.trackPage( "CoLIde Main Procedure Class Loaded");

  }

 
  public void inputFiles( ArrayList<ArrayList<File>> inputFiles, File genomeFile )
  {
    mySamples = inputFiles;
    myGenomeFile = genomeFile;
  }

  public void inputResultPanels( FilterResultsPanel tabList, CoLIDEResultsPanel resultsTable )
  {
    myFilterTabs = tabList;
    myResultsTable = resultsTable;
  }
  private void createExpressionMatrix() throws IOException, Exception
  {
   expressionMatrix = 
     new SparseExpressionMatrix(getTracker(), this.myFilterTabs,
     mySamples, myGenomeFile);

   // Stop the stop watch.
      
   expressionMatrix.preProcess(params.getMinLength(), params.getMaxLength());
    expressionMatrix.build();
    Thread newThread = new Thread( new Runnable()
    {
      @Override
      public void run()
      {

        generateAbundancePlot = expressionMatrix.generateAbundancePlot();

        generateAbundancePlot.revalidate();

      }
    } );

    newThread.start();
    newThread.join();
    
    //while (generateAbundancePlot == null);//wait till event thread has completed above
   ArrayList<Integer> retreiveTopAbundances = generateAbundancePlot.retreiveTopAbundances();

   SizeClassDetectionConfirmation confirmSizeClassDetection = new SizeClassDetectionConfirmation(new JFrame(), true);
   
   confirmSizeClassDetection.setupSizeClassBoxes(retreiveTopAbundances);
   confirmSizeClassDetection.setVisible( true );
   params.setSizeClassRanges( confirmSizeClassDetection.retreiveFinalAbundances() );
   //generateAbundancePlot.setIcon( true );
   //generateAbundancePlot.setVisible( false );
   
  }

  private void normalise()
  {
      

    NormalisationProcess newNormaliser = new NormalisationProcess(NormalisationType.TOTAL_COUNT, expressionMatrix, myTracker, FileUtils.getTempDirectory());
    newNormaliser.run();
  
    
  }

  
  private void identifyPatterns()
  {
    if(params.getSeriesType() == SERIES_TYPE_ENUM.ORDERED)
    {
      indentifyOrderedUDSPattern();
    }
    else
    {
      indentifyUnOrderedUDSPattern();
    }
  }

  private void indentifyOrderedUDSPattern()
  {
    ArrayList<ArrayList<String>> samples = expressionMatrix.getFileNames();
    
    

    for ( Map.Entry<String, HashMap<String, ExpressionElement>> entry : expressionMatrix.getEntries() )
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      params.getConfInter().buildConfidenceInterval( row, samples, this.params.getPercentageCIValue() );

      generateUDS(samples, row);
    }
  }


  private void generateUDS(ArrayList<ArrayList<String>> samples, HashMap<String, ExpressionElement> row)
  {
    String UDS_pattern = "";
    //start with the first file name in the first sample
    String firstKey = samples.get( 0 ).get( 0 );
    ExpressionElement firstElement = row.get( firstKey );
    ExpressionElement secondElement;
    //now loop through all of the others
    this.trackerInitKnownRuntime( "Generating UDS patterns...", samples.size() );
    for ( int i = 1; i < samples.size(); i++ )
    {
      //we only need the first expression element (file name) in each sample 
      //as they all have an identical CI 
      String secondKey = samples.get( i ).get( 0 );

      secondElement = row.get( secondKey );

      double firstCI[] =
      {
        0.0, 10.0
      };
      double secondCI[] =
      {
        0.0, 10.0
      };

      if ( firstElement != null )//found an entry for this file so grab confidence interval
      {
        firstCI = firstElement.confidenceInterval;
      }
      if ( secondElement != null )//found an entry for this file so grab confidence interval
      {
        secondCI = secondElement.confidenceInterval;
      }
      //we can perform some simple checks
      //if the high range of the first CI is below the low range of the second CI it must be a UP
      if ( ( firstCI[1] <= secondCI[0] ) )
      {
        UDS_pattern += "U";
      }
      //if the low range of the first CI is above the high range of the second CI it must be an UP
      else if ( ( firstCI[0] >= secondCI[1] ) )
      {
        UDS_pattern += "D";
      }
      //now we start to find the pattern between the intervals
      else
      {
        
        //if(firstElement.sequence.equals( "TTTGAACCAAAAGTTGGACGGCGA") )
        double length_interval = 1.0;
        double lengthFirst = firstCI[1] - firstCI[0];
        double lengthSecond = secondCI[1] - secondCI[0];
        double valueToTest = Math.min( lengthFirst, lengthSecond );
        if( ( firstCI[0] < secondCI[1] ) && (firstCI[0] > secondCI[0]) )
        {
          length_interval = Math.abs( secondCI[1] - firstCI[0] );
        }
        else if ( ( firstCI[1] > secondCI[0] ) && ( firstCI[1] < secondCI[1] ) )
        {
          length_interval = Math.abs( firstCI[1] - secondCI[0] );

        }
        else
        {
          length_interval = valueToTest;
        }
        

        //double length_interval = Math.abs( firstCI[1] - secondCI[0] );
        

        double percentageDrop = ( length_interval / valueToTest ) * 100.0;
        if ( percentageDrop > params.getPercentageOverlapValue() )
        {
          UDS_pattern += "S";
        }
        else
        {
          if ( firstCI[1] <= secondCI[1] )
          {
            UDS_pattern += "U";
          }
          else
          {
            UDS_pattern += "D";
          }
        }
 
      }
      //update the pointer to move to the next column;
      firstElement = secondElement;
      this.trackerIncrement();
    }
    if ( UDS_pattern.isEmpty() )
    {
      System.out.println( "A problem occured generating pattern string" );
    }

    this.trackerReset();
    //all samples have been examined now we add the UDS pattern string to the elements
    for ( ArrayList<String> fileNames : samples )
    {
      for ( String fileName : fileNames )
      {
        firstElement = row.get( fileName );
        if ( firstElement != null )//found an entry for this file so grab UDS
        {
          firstElement.UDS_pattern = UDS_pattern;
        }
      }
    }

  }
  @Override
  protected void process() throws Exception
  {
  
    
//    StopWatch sw = new StopWatch();
//      sw.start();
    continueRun();
    createExpressionMatrix();
//    sw.suspend();
//    System.out.println( "Pre-processing time: " + sw.toString() );
//    sw.resume();
    continueRun();
    System.gc();
    normalise();
    continueRun();
    identifyPatterns();
    continueRun();
    createOverlappingGroups();
    continueRun();
    //sw.stop();
    //System.out.println( "complete overall time: " + sw.toString() );
    
  }

  public void resetData()
  {
    expressionMatrix = null;
    myGenomeFile = null;
    mySamples.clear();
    System.gc();
  }
  private void indentifyUnOrderedUDSPattern()
  {
    
    //create confidence interval no replicate so +-X% as before
    //sort abundances in ascending order one row at a time
    //find UDS pattern
    //transform to simplified data:
    //start with 0. For up +1, for down -1, for straight +0
    //return to original order
    //find order based on the simplified values
    //find UDS pattern again based on new order

    ArrayList<ArrayList<String>> samples = expressionMatrix.getFileNames();

    for ( Map.Entry<String, HashMap<String, ExpressionElement>> entry : expressionMatrix.getEntries() )
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      ArrayList<ArrayList<String>> sortedSamplesForRow = new ArrayList<ArrayList<String>>();
      for ( ArrayList<String> replicateFiles : samples )
      {
        ArrayList<String> rowFiles = new ArrayList<String>();
        for ( String filename : replicateFiles )
        {
            rowFiles.add( filename );
        }
          sortedSamplesForRow.add( rowFiles );
      }
      //sort abundances in ascending order one row at a time
      //if only one file contained the sequence then it is already sorted
      if ( sortedSamplesForRow.size() >= 2 )
      {
        params.getConfInter().sortRow( row, sortedSamplesForRow );
      }  
      params.getConfInter().buildConfidenceInterval( row, sortedSamplesForRow, this.params.getPercentageCIValue() );
      
      generateUDS(sortedSamplesForRow, row);
      
      String orderedSimplifiedSeries = calculateSimplifiedSeries(sortedSamplesForRow,row);

      int currentPos = 0;
      char originalSimplifiedSeries[] = new char[orderedSimplifiedSeries.length()];
      
      //find original position for each sample
      for ( ArrayList<String> filesForSample : sortedSamplesForRow )
      {
        String currentOrderFileName = filesForSample.get( 0 );//get first file in sorted sample list
        //find its position in the original order
        boolean found = false;
        int originalPosition = -1;
        for(ArrayList<String> searchingOriginal : samples)
        {
          if(!found)
          {
            originalPosition++;
            if(searchingOriginal.contains( currentOrderFileName ))
            {
              found = true;
            }
          }
        }
        if ( found )
        {
          originalSimplifiedSeries[originalPosition] = orderedSimplifiedSeries.charAt( currentPos );
        }
        currentPos++;
      }
      //String finalSimplifiedSeries = new String(originalSimplifiedSeries);
      //now build the real confidence interval from the new simplified series
      String final_UDS_pattern = "";
      int previous_digit = Character.getNumericValue( originalSimplifiedSeries[0]);
      for(int i = 1 ; i < originalSimplifiedSeries.length; i++)
      {
        int digit = Character.getNumericValue( originalSimplifiedSeries[i]);
        if(digit > previous_digit)
        {
          final_UDS_pattern += "U";
        }
        else if(digit < previous_digit)
        {
          final_UDS_pattern += "D";
        }
        else if(digit == previous_digit)
        {
          final_UDS_pattern += "S";
        }
        previous_digit = digit;
      }
      //all samples have been examined now we add the UDS pattern string to the elements
      for ( ArrayList<String> fileNames : samples )
      {
        for ( String fileName : fileNames )
        {
          ExpressionElement element = row.get( fileName );
          if ( element != null )//found an entry for this file so grab UDS
          {
            element.UDS_pattern = final_UDS_pattern;
          }
        }
      }
    }
    
  
    
  }
  private String calculateSimplifiedSeries(ArrayList<ArrayList<String>> sortedRowSamples,
                                           HashMap<String, ExpressionElement> row)
  {
    String simplifiedSeries = "0";
    int simplifiedValue = 0;
    
    //loop through each collection of files per sample
    ExpressionElement firstElement = 
    row.values().iterator().next();
   
    if ( firstElement != null )//
    {
      for ( int i = 0; i < firstElement.UDS_pattern.length(); i++ )
      {
        char UDSChar = firstElement.UDS_pattern.charAt( i );
        switch ( UDSChar )
        {
          case 'U':
            simplifiedValue += 1;
            simplifiedSeries += simplifiedValue;
            break;
          case 'D':
            simplifiedValue -= 1;
            simplifiedSeries += simplifiedValue;
            break;
          case 'S':
            simplifiedValue += 0;
            simplifiedSeries += simplifiedValue;
            break;
        }
      }

    }
    else
    {
      System.out.println( "didnt find any reads for any sample!!! This should never happen" );
    }

    
    
    return simplifiedSeries;
    
  }

  private void createOverlappingGroups() throws IOException
  {
    //System.out.println( "Grabbing chunk" );
    
    Set<Entry<String, HashMap<String, ExpressionElement>>> sortedChunk;
    

    HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus = new HashMap<String, ArrayList<ArrayList<ExpressionElement>>>();
    
    
    Set<String> chromoIDS = expressionMatrix.getChromoIDS();
    
     
    for ( String chromo : chromoIDS )
    {
      continueRun();
      ArrayList<ArrayList<ExpressionElement>> overlappingPattern = new ArrayList<ArrayList<ExpressionElement>>();
      
      int minCoord = expressionMatrix.getMinCoordForChrom(chromo);
      int maxCoord = minCoord + this.params.getMaxChunk();
  
      int maxCoordForChromosome = expressionMatrix.getMaxCoordForChrom( chromo );
      this.trackerInitKnownRuntime( "Generating overlapping group for chromosome: " + chromo + "...", 100 );
      LinkedHashSet<String> UDS_List_for_Chrom = new LinkedHashSet<String>();
      while ( maxCoord <= maxCoordForChromosome )
      {
        float currentPercentage = ((float)maxCoord / (float)maxCoordForChromosome) * 100.0f;
        this.trackerIncrement((int)currentPercentage);
        sortedChunk =
          expressionMatrix.getSortedChunk( minCoord, maxCoord, chromo, params.getMinChunk() );
        if (( sortedChunk == null ))//larger chunk size contained multiple mapping
        {
          maxCoord = minCoord;
          maxCoord += params.getMinChunk();
        }
        else if(sortedChunk.isEmpty())//smaller chunk contained multiple mapping
        {

          // multiple mapping in this smaller region... ignored;
          minCoord = maxCoord;
          maxCoord += params.getMaxChunk();
        }
        else
        {
          LinkedHashSet<String> allUDSForCluster = detectAllUDSForCluster( sortedChunk );
          UDS_List_for_Chrom.addAll( allUDSForCluster );
          Entry<String, HashMap<String, ExpressionElement>> next = sortedChunk.iterator().next();

          int[] maxCoordFound =
          {
            Integer.MIN_VALUE
          };
          for ( String UDS_forCluster : allUDSForCluster )
          {

            ArrayList<ArrayList<ExpressionElement>> buildPatternInterval = buildPatternInterval( sortedChunk, UDS_forCluster, maxCoordFound );

            if ( !buildPatternInterval.isEmpty() )
            {
              overlappingPattern.addAll( buildPatternInterval );
            }
          }
          minCoord = maxCoordFound[0];
          maxCoord = minCoord + params.getMaxChunk();
          
        }
      }
      this.sortOverlappingPatternsForChromo( overlappingPattern );
      //printOverlappingPatterns(overlappingPattern, chromo);
      HashMap<String, ArrayList<ArrayList<ExpressionElement>>> UDS_Clusters = gatherUDSClusters(overlappingPattern, UDS_List_for_Chrom);
      //change to group all PIs with same UDS pattern
      //sort the groups of patterns
      printSortedPatterns(UDS_Clusters, chromo);
      
      this.trackerReset();
      
      //entire chromosome processed for clusters, now we must merge to build locus
      if(!overlappingPattern.isEmpty())
      {
        Iterator<ArrayList<ExpressionElement>> patternIterator = overlappingPattern.iterator();
        ArrayList<ExpressionElement> firstCluster = patternIterator.next();
        ArrayList<ExpressionElement> secondCluster = null;
        ArrayList<Double> all_distances = new ArrayList<Double>();
        //first gather all the distances...
        this.trackerInitKnownRuntime( "Calculating distances between loci for chromosome: " + chromo + "...", overlappingPattern.size() );
        while(patternIterator.hasNext())
        {
          this.trackerIncrement();
          secondCluster = patternIterator.next();
          double firstCoord = firstCluster.get( firstCluster.size() - 1 ).endCoord.get( 0 );
          double secondCoord = secondCluster.get( 0 ).startCoord.get( 0 );
          double distance = secondCoord - firstCoord;
          all_distances.add( distance );
          firstCluster = secondCluster;
        }
        this.trackerReset();
        //list requires sorting here so all_distances will no longer be valid
        //if we need to use it in the future we must Collections.Copy before calling this method
        double median = calcMedian(all_distances);
        //now create the list of all loci for this chromosome
        ArrayList<ArrayList<ExpressionElement>> listOfLoci = new ArrayList<ArrayList<ExpressionElement>>();
        patternIterator = null;
        int locusIndex = -1;
        this.trackerInitKnownRuntime( "Merging loci with same pattern: " + chromo + "...", UDS_Clusters.size() );
        for(Entry<String, ArrayList<ArrayList<ExpressionElement>>> UDS_Entry : UDS_Clusters.entrySet() )
        {
          continueRun();
          this.trackerIncrement();
          //a new UDS entry means a new locus is required
          listOfLoci.add( new ArrayList<ExpressionElement>() );
          locusIndex++;
 
          if(UDS_Entry.getValue().size() == 1)
          {

            listOfLoci.get( locusIndex).addAll( UDS_Entry.getValue().get( 0 ) );
            
          }
          else
          {
            patternIterator = UDS_Entry.getValue().iterator();
            
            //init both pattern intervals
            //dangerous? maybe...
            //assumtion is, there is at least 2 pattern intervals or the program would never branch into this region
            secondCluster = patternIterator.next();
            listOfLoci.get( locusIndex ).addAll( secondCluster );
            
            while ( patternIterator.hasNext() )
            {
              firstCluster = secondCluster;
              secondCluster = patternIterator.next();
              

              int firstCoord = firstCluster.get( firstCluster.size() - 1 ).endCoord.get( 0 );
              int secondCoord = secondCluster.get( 0 ).startCoord.get( 0 );
              int distance = secondCoord - firstCoord;
              if ( distance <= median )//start new locus
              {
                listOfLoci.get( locusIndex ).addAll( secondCluster );
              }
              else if(distance == 0)
              {
                listOfLoci.get( locusIndex ).addAll( secondCluster );
              }
              else
              {
                listOfLoci.add( new ArrayList<ExpressionElement>() );
                locusIndex++;
                listOfLoci.get( locusIndex ).addAll( secondCluster );
              }
            } 
          }

        }
        this.trackerReset();
        
        this.sortOverlappingPatternsForChromo( listOfLoci );
        ArrayList<ArrayList<ExpressionElement>> statisticalLociRemoved = performInitialStatisticalAnalysis(listOfLoci);
        chromoToLocus.put( chromo, statisticalLociRemoved );
      }
      this.trackerReset();
    }
    updateOutputModules(chromoToLocus);
    
    System.out.println( "FINISHED" );

  }

  public double calcMedian( ArrayList<Double> values )
  {
    Collections.sort( values );

    if ( values.size() % 2 == 1 )
    {
      return values.get( ( values.size() + 1 ) / 2 - 1 );
    }
    else
    {
      double lower = values.get( values.size() / 2 - 1 );
      double upper = values.get( values.size() / 2 );

      return ( lower + upper ) / 2.0;
    }
  }



  private void updateOutputModules(HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus)
  {
    //outputCSVFiles(chromoToLocus);
    if ( myResultsTable == null )
    {
      System.out.println( "printing start and end coords for each locus" );
      for ( Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : chromoToLocus.entrySet() )
      {
        System.out.println( "Chromosome: " + entry.getKey() );
        System.out.println( "Total amount of loci: " + entry.getValue().size() );
        int i = 0;
        for ( ArrayList<ExpressionElement> lociList : entry.getValue() )
        {

          int currentMinCoord = Integer.MAX_VALUE;
          int currentMaxCoord = Integer.MIN_VALUE;
          for ( ExpressionElement element : lociList )
          {
            System.out.println( "Element: " + element );
            if ( element.startCoord.get( 0 ) < currentMinCoord )
            {
              currentMinCoord = element.startCoord.get( 0 );
            }
            if ( element.endCoord.get( 0 ) > currentMaxCoord )
            {
              currentMaxCoord = element.endCoord.get( 0 );
            }
          }
          System.out.println( "locus: " + i + " contains " + lociList.size() + " sequences, min coord: " + currentMinCoord + " max coord: " + currentMaxCoord );
          i++;
        }
      }
    }
    else
    {
      this.myResultsTable.display( chromoToLocus, expressionMatrix, params );
    }
  }
  private LinkedHashSet<String> detectAllUDSForCluster(Set<Entry<String, HashMap<String, ExpressionElement>>> sortedChunk)
  {
    LinkedHashSet<String> UDS_List = new LinkedHashSet<String>();
    //String currentUDS = sortedHu
    for ( Map.Entry<String, HashMap<String, ExpressionElement>> entry :
      sortedChunk )
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      for ( Entry<String, ExpressionElement> EE : row.entrySet() )
      {
        UDS_List.add( EE.getValue().UDS_pattern);
      }
    }
    //System.out.println( "UDS list: " + UDS_List );
    
    return UDS_List;
  }
  private ArrayList<ArrayList<ExpressionElement>> buildPatternInterval( Set<Entry<String, HashMap<String, ExpressionElement>>> sortedChunk,
                                                             String UDS_for_Cluster, int[] maxCoordFound)
  {
    Iterator<Entry<String, HashMap<String, ExpressionElement>>> chunk_iterator = sortedChunk.iterator();
    ExpressionElement firstElement = null;// = rowIterator.next();
    HashMap<String, ExpressionElement> row;// = chunk_iterator.next().getValue();
    boolean searching = true;
    while(chunk_iterator.hasNext() && searching)
    {
      
      row = chunk_iterator.next().getValue();
      firstElement = row.values().iterator().next();
      if(firstElement.UDS_pattern.equals( UDS_for_Cluster) )
      {
        searching = false;
      }
    }
    ArrayList<ArrayList<ExpressionElement>> patternIntervalsForUDS = new ArrayList<ArrayList<ExpressionElement>>();
    patternIntervalsForUDS.add( new ArrayList<ExpressionElement>() );
    chunk_iterator = sortedChunk.iterator();
    ExpressionElement secondElement = null;
    int index = 0;

    while(chunk_iterator.hasNext())
    {
      
      row = chunk_iterator.next().getValue();
      secondElement = row.values().iterator().next();
      
      if ( firstElement.endCoord.get( 0 ) > maxCoordFound[0] )
      {
        maxCoordFound[0] = firstElement.endCoord.get( 0 ) ;
      }
      if ( secondElement.endCoord.get( 0 ) > maxCoordFound[0] )
      {
        maxCoordFound[0] = secondElement.endCoord.get( 0 ) ;
      }

      if(firstElement.UDS_pattern.equals( UDS_for_Cluster) )
      {
        if(secondElement.UDS_pattern.equals( UDS_for_Cluster) )
        {
          if(secondElement.startCoord.get( 0 ) >= firstElement.startCoord.get(0) &&
            secondElement.startCoord.get(0) <= firstElement.endCoord.get(0))//overlap
          {
            if(!patternIntervalsForUDS.get(index ).contains( firstElement) )
              patternIntervalsForUDS.get(index ).add( firstElement );
            if(!patternIntervalsForUDS.get(index ).contains( secondElement) )
              patternIntervalsForUDS.get(index ).add( secondElement );
            
            firstElement = secondElement;
          }
          else//no overlap so add new pattern interval
          {
            patternIntervalsForUDS.add( new ArrayList<ExpressionElement>());
            index++;
            firstElement = secondElement;
            if(!patternIntervalsForUDS.get(index ).contains( secondElement) )
              patternIntervalsForUDS.get(index ).add( secondElement );
            
          }
          
        }
        
      }
      
    }
    return patternIntervalsForUDS;    
  }

  private void printPutuativeLoci(HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus)
  {
    FileWriter outPutativeFile = null;
    PrintWriter outPutative = null;

    try
    {
      outPutativeFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "putative.csv" );
      outPutative = new PrintWriter( outPutativeFile );
      String line = "";
      for ( Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : chromoToLocus.entrySet() )
      {
        line += entry.getKey() + ",";
        int locusNumber = 0;
        for ( ArrayList<ExpressionElement> test : entry.getValue() )
        {
          line += locusNumber++ + ",";
          int currentMinCoord = Integer.MAX_VALUE;
          int currentMaxCoord = Integer.MIN_VALUE;
          double expressionSeries[] = new double[expressionMatrix.getFileNames().size()];
          for ( ExpressionElement element : test )
          {
            //line = ele.sequence + "," + ele.chromosomeID + "," + ele.startCoord.get( 0 ) + "," + ele.endCoord.get( 0 ) + ",";
            HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( element.sequence );
            int sampleIndex = 0;
            for ( ArrayList<String> samples : expressionMatrix.getFileNames() )
            {
              for ( String fileName : samples )
              {
                if ( originalRow.containsKey( fileName ) )
                {
                  expressionSeries[sampleIndex] += originalRow.get( fileName ).normalisedAbundance;
                }
              }
            }
            if ( element.startCoord.get( 0 ) < currentMinCoord )
            {
              currentMinCoord = element.startCoord.get( 0 );
            }
            if ( element.endCoord.get( 0 ) > currentMaxCoord )
            {
              currentMaxCoord = element.endCoord.get( 0 );
            }
          }
          line += currentMinCoord + "," + currentMaxCoord + ",";
          for(double value : expressionSeries)
            line += value + ",";
          outPutative.println(line);
          
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.WARNING, ex.getMessage() );
    }
    finally
    {
      try
      {
        outPutativeFile.close();
        outPutative.close();
       
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.WARNING, ex.getMessage() );
      }
    }

  }
  private void outputCSVFiles(HashMap<String, ArrayList<ArrayList<ExpressionElement>>> chromoToLocus)
  {
    System.out.println( "printing start and end coords for each locus" );
    FileWriter outUnNormalFile = null;
    PrintWriter outUnNormal = null;
    FileWriter outNormalFile = null;
    PrintWriter outNormal = null;
    
    FileWriter outLociFile = null;
    PrintWriter outLoci = null;
    try
    {
      outUnNormalFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "un-normalised.csv" );
      outUnNormal = new PrintWriter( outUnNormalFile );
      
      outNormalFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "normalised.csv" );
      outNormal = new PrintWriter( outNormalFile );

      outLociFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "loci.csv" );
      outLoci = new PrintWriter( outLociFile );
      
      String header = "sequence,";
      ArrayList<ArrayList<String>> fileNames = expressionMatrix.getFileNames();
      
      for ( ArrayList<String> samples : fileNames )
      {
        for ( String fileName : samples )
        {
          header += fileName + ",";
        }

      }
      outUnNormal.println( header );
      outNormal.println(header);
      outLoci.println(header);
      
      for ( Entry<String, HashMap<String, ExpressionElement>> entry : expressionMatrix.getEntries() )
      {

        String toPrintUnNormal = entry.getKey() + ",";
        String toPrintNormal = entry.getKey() + ",";
        HashMap<String, ExpressionElement> row = entry.getValue();

        for ( ArrayList<String> samples : expressionMatrix.getFileNames() )
        {
          for ( String fileName : samples )
          {
            if(row.containsKey( fileName) )
            {
              toPrintUnNormal += row.get( fileName).originalAbundance + ",";
              toPrintNormal += row.get( fileName).normalisedAbundance + ",";
            }
            else
            {
              toPrintUnNormal += "0,";
              toPrintNormal += "0,";
            }
          }
        }
        toPrintUnNormal += row.values().iterator().next().UDS_pattern;
        toPrintNormal += row.values().iterator().next().UDS_pattern;
        outUnNormal.println( toPrintUnNormal );
        outNormal.println( toPrintNormal );
        
        

      }
      
      //System.out.println( "printing start and end coords for each locus" );
      for ( Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : chromoToLocus.entrySet() )
      {
//        System.out.println( "Chromosome: " + entry.getKey() );
//        System.out.println( "Total amount of loci: " + entry.getValue().size() );
        int i = 0;
        
        for ( ArrayList<ExpressionElement> locus : entry.getValue() )
        {

          String outputLine = "" + i + "," + entry.getKey() + ",";
          int currentMinCoord = Integer.MAX_VALUE;
          int currentMaxCoord = Integer.MIN_VALUE;
          double expressionSeries[] = new double[expressionMatrix.getFileNames().size()];
          for ( ExpressionElement element : locus )
          {
            //System.out.println( "Element: " + element );
            HashMap<String, ExpressionElement> originalRow = expressionMatrix.get(element.sequence);
            int sampleIndex = 0;
            for ( ArrayList<String> sampleList : fileNames )
            {
              if ( sampleList.size() == 1 )//no replicates
              {
                if ( originalRow.containsKey( sampleList.get( 0 ) ) )
                {
                  expressionSeries[sampleIndex] += element.normalisedAbundance;
                }
              }
              else
              {
                double mean = 0.0;
                for ( String replicateName : sampleList )
                {
                  if ( originalRow.containsKey( sampleList.get( 0 ) ) )
                  {
                    mean += element.normalisedAbundance;
                  }
                }
                mean /= sampleList.size();
                expressionSeries[sampleIndex] += mean;
              }
              sampleIndex++;
            }
            if ( element.startCoord.get( 0 ) < currentMinCoord )
            {
              currentMinCoord = element.startCoord.get( 0 );
            }
            if ( element.endCoord.get( 0 ) > currentMaxCoord )
            {
              currentMaxCoord = element.endCoord.get( 0 );
            }
            
          }
          outputLine += currentMinCoord + "," + currentMaxCoord + ",";
          outputLine += locus.get(0).UDS_pattern + ",";
          double p_val = this.myResultsTable.calculateP_Val( locus );

          

          String expressionString = "";
          for ( double expressionValue : expressionSeries )
          {
            expressionString += expressionValue + ",";
          }
          outputLine += expressionString + ",";
          
          outputLine += String.valueOf( p_val ) + ",";

          outLoci.println(outputLine);
          i++;
        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.WARNING, ex.getMessage() );
    }
    finally
    {
      try
      {
        outUnNormalFile.close();
        outUnNormal.close();
        outNormalFile.close();
        outNormal.close();
        
        outLociFile.close();
        outLoci.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.WARNING, ex.getMessage() );
      }
    }

  }

  public SparseExpressionMatrix getExpressionMatrix()
  {
    return this.expressionMatrix;
  }

  private void printOverlappingPatterns( ArrayList<ArrayList<ExpressionElement>> overlappingPattern, String chromoHeader )
  {
    FileWriter outPatternFile = null;
    PrintWriter outPattern = null;
    try
    {
      
      outPatternFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "patterns.csv" );
      outPattern = new PrintWriter( outPatternFile );
      
      String header =  "Chromo ,";
      ArrayList<ArrayList<String>> fileNames = expressionMatrix.getFileNames();
      
      for ( ArrayList<String> samples : fileNames )
      {
        for ( String fileName : samples )
        {
          header += fileName + ",";
        }

      }
    
      outPattern.println( header );
      
      for ( ArrayList<ExpressionElement> pattern : overlappingPattern )
      {
        String line = chromoHeader + "," ;
        int currentMinCoord = Integer.MAX_VALUE;
        int currentMaxCoord = Integer.MIN_VALUE;
        double expressionSeries[] = new double[expressionMatrix.getFileNames().size()];
        for ( ExpressionElement element : pattern )
        {

          HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( element.sequence );
          int sampleIndex = 0;
          
          for ( ArrayList<String> sampleList : fileNames )
          {
            if ( sampleList.size() == 1 )//no replicates
            {
              if ( originalRow.containsKey( sampleList.get( 0 ) ) )
              {
                //expressionSeries[sampleIndex] += element.normalisedAbundance;
                expressionSeries[sampleIndex] += originalRow.get( sampleList.get(0)).normalisedAbundance;
              }
            }
            else
            {
              double mean = 0.0;
              for ( String replicateName : sampleList )
              {
                if ( originalRow.containsKey( replicateName ) )
                {
                  //mean += element.normalisedAbundance;
                  mean += originalRow.get( sampleList.get(0)).normalisedAbundance;
                }
              }
              mean /= sampleList.size();
              expressionSeries[sampleIndex] += mean;
            }
            sampleIndex++;
          }
          if ( element.startCoord.get( 0 ) < currentMinCoord )
          {
            currentMinCoord = element.startCoord.get( 0 );
          }
          if ( element.endCoord.get( 0 ) > currentMaxCoord )
          {
            currentMaxCoord = element.endCoord.get( 0 );
          }

        }
        
        line += pattern.get( 0 ).UDS_pattern + "," + currentMinCoord + "," + currentMaxCoord + ",";

        String expressionString = "";
        for ( double expressionValue : expressionSeries )
        {
          expressionString += expressionValue + ",";
        }
        line += expressionString + ",";
        outPattern.println(line );

      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.WARNING, ex.getMessage() );
    }
    finally
    {
      try
      {
        outPatternFile.close();
        outPattern.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.WARNING, ex.getMessage() );
      }
    }
  }

  private void sortOverlappingPatternsForChromo( ArrayList<ArrayList<ExpressionElement>> overlappingPattern )
  {
     Comparator<ArrayList<ExpressionElement>> cStart = new Comparator<ArrayList<ExpressionElement>>() {
      @Override
      public int compare(ArrayList<ExpressionElement> u1, ArrayList<ExpressionElement> u2) {
        //return u1.compareTo(u2.getId());
        int u1MinCoord = Integer.MAX_VALUE;
        int u1MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u1 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u1MinCoord )
          {
            u1MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u1MaxCoord )
          {
            u1MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        int u2MinCoord = Integer.MAX_VALUE;
        int u2MaxCoord = Integer.MIN_VALUE;
        for ( ExpressionElement flankingElement : u2 )
        {
          if ( flankingElement.startCoord.get( 0 ) < u2MinCoord )
          {
            u2MinCoord = flankingElement.startCoord.get( 0 );
          }
          if ( flankingElement.endCoord.get( 0 ) > u2MaxCoord )
          {
            u2MaxCoord = flankingElement.endCoord.get( 0 );
          }
          

        }
        Integer start1 = new Integer(u1MinCoord );
        Integer start2 = new Integer(u2MinCoord);
        return start1.compareTo( start2 );
      }
    };
    Collections.sort( overlappingPattern, cStart);
    
  }

  private HashMap<String, ArrayList<ArrayList<ExpressionElement>>> gatherUDSClusters( ArrayList<ArrayList<ExpressionElement>> overlappingPattern, 
                                                                                      LinkedHashSet<String> UDS_List_for_Chrom)
  {
    HashMap<String, ArrayList<ArrayList<ExpressionElement>>> patternMap = new HashMap<String, ArrayList<ArrayList<ExpressionElement>>>();
//    for(String UDS_pattern : UDS_List_for_Chrom)
//    {
//      patternMap.put( UDS_pattern, new ArrayList<ArrayList<ExpressionElement>>());
//    }
    for(ArrayList<ExpressionElement> singlePattern : overlappingPattern)
    {
      String currentUDS = singlePattern.get( 0 ).UDS_pattern;
      if(patternMap.containsKey( currentUDS) )
      {
      
          patternMap.get( currentUDS).add( singlePattern );
      }
      else
      {
        patternMap.put( currentUDS, new ArrayList<ArrayList<ExpressionElement>>());
        patternMap.get( currentUDS).add( singlePattern );
      }
    }
 
    for(Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : patternMap.entrySet() )
    {
      sortOverlappingPatternsForChromo(entry.getValue());
    }
    //sortOverlappingPatternsForChromo(overlappingPattern);
    return patternMap;
  }

  private void printSortedPatterns( HashMap<String, ArrayList<ArrayList<ExpressionElement>>> UDS_Clusters, String chromo )
  {
    FileWriter outPatternFile = null;
    PrintWriter outPattern = null;
    try
    {

      outPatternFile = new FileWriter( Tools.CoLIDE_dataPath + DIR_SEPARATOR + "UDS_patterns.csv" );
      outPattern = new PrintWriter( outPatternFile );

      String header = "Chromo ,";
      ArrayList<ArrayList<String>> fileNames = expressionMatrix.getFileNames();

      for ( ArrayList<String> samples : fileNames )
      {
        for ( String fileName : samples )
        {
          header += fileName + ",";
        }

      }

      outPattern.println( header );
      for ( Entry<String, ArrayList<ArrayList<ExpressionElement>>> entry : UDS_Clusters.entrySet() )
      {
        for ( ArrayList<ExpressionElement> pattern : entry.getValue() )
        {
          String line = chromo + ",";
          int currentMinCoord = Integer.MAX_VALUE;
          int currentMaxCoord = Integer.MIN_VALUE;
          double expressionSeries[] = new double[expressionMatrix.getFileNames().size()];
          for ( ExpressionElement element : pattern )
          {
            HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( element.sequence );
            int sampleIndex = 0;

            for ( ArrayList<String> sampleList : fileNames )
            {
              if ( sampleList.size() == 1 )//no replicates
              {
                if ( originalRow.containsKey( sampleList.get( 0 ) ) )
                {
                  expressionSeries[sampleIndex] += element.normalisedAbundance;
                }
              }
              else
              {
                double mean = 0.0;
                for ( String replicateName : sampleList )
                {
                  if ( originalRow.containsKey( replicateName ) )
                  {
                    mean += element.normalisedAbundance;
                  }
                }
                mean /= sampleList.size();
                expressionSeries[sampleIndex] += mean;
              }
              sampleIndex++;
            }
            if ( element.startCoord.get( 0 ) < currentMinCoord )
            {
              currentMinCoord = element.startCoord.get( 0 );
            }
            if ( element.endCoord.get( 0 ) > currentMaxCoord )
            {
              currentMaxCoord = element.endCoord.get( 0 );
            }

          }
          line += pattern.get( 0 ).UDS_pattern + "," + currentMinCoord + "," + currentMaxCoord + ",";

          String expressionString = "";
          for ( double expressionValue : expressionSeries )
          {
            expressionString += expressionValue + ",";
          }
          line += expressionString + ",";
          outPattern.println( line );

        }
      }
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.WARNING, ex.getMessage() );
    }
    finally
    {
      try
      {
        outPatternFile.close();
        outPattern.close();
      }
      catch ( IOException ex )
      {
        LOGGER.log( Level.WARNING, ex.getMessage() );
      }
    }
 
  }

  private ArrayList<ArrayList<ExpressionElement>> performInitialStatisticalAnalysis( ArrayList<ArrayList<ExpressionElement>> listOfLoci)
  {
    ArrayList<ArrayList<ExpressionElement>> listOfLociToReturn = new ArrayList<ArrayList<ExpressionElement>>();
    this.trackerInitKnownRuntime( "Performing initial statistical analysis...", listOfLoci.size() );
    for ( Iterator<ArrayList<ExpressionElement>> lociListIterator = listOfLoci.iterator(); lociListIterator.hasNext(); )
    {
      this.trackerIncrement();
      int currentMinCoord = Integer.MAX_VALUE;
      int currentMaxCoord = Integer.MIN_VALUE;
      double totalAbundance = 0.0;
      ArrayList<ExpressionElement> lociList = lociListIterator.next();
      //currentLocusIndex++;
      for ( ExpressionElement element : lociList )
      {
        HashMap<String, ExpressionElement> originalRow = expressionMatrix.get( element.sequence );
        for ( ArrayList<String> sampleList : expressionMatrix.getFileNames() )
        {
          if ( sampleList.size() == 1 )//no replicates
          {
            if ( originalRow.containsKey( sampleList.get( 0 ) ) )
            {
              //expressionSeries[sampleIndex] += element.normalisedAbundance;
              totalAbundance += originalRow.get( sampleList.get( 0 ) ).normalisedAbundance;
            }
          }
          else
          {
            double mean = 0.0;
            for ( String replicateName : sampleList )
            {
              if ( originalRow.containsKey( replicateName ) )
              {
                //mean += element.normalisedAbundance;
                mean += originalRow.get( replicateName ).normalisedAbundance;
              }
            }
            mean /= sampleList.size();
            totalAbundance += mean;
          }
        }
        if ( element.startCoord.get( 0 ) < currentMinCoord )
        {
          currentMinCoord = element.startCoord.get( 0 );
        }
        if ( element.endCoord.get( 0 ) > currentMaxCoord )
        {
          currentMaxCoord = element.endCoord.get( 0 );
        }
      }
//      if((currentMinCoord == 24215))
//          System.out.println( "" );
      int lengthOfLocus = currentMaxCoord - currentMinCoord;
      int flankStart = Math.max( 0, ( currentMinCoord - 500 ) );
      int flankEnd = currentMaxCoord + 500;

      ArrayList<Integer> flankingLociLengths = new ArrayList<Integer>();
      ArrayList<Double> flankingLociTotalAbundances = new ArrayList<Double>();
      //now find all locus that are inside the flanking region

      ArrayList<ArrayList<ExpressionElement>> chunkToCheck = LocusUtils.getLociInRange( listOfLoci, flankStart, flankEnd );
      if ( chunkToCheck.size() == 1 )
      {
        listOfLociToReturn.add( lociList );
      }
      else
      {
        for ( Iterator<ArrayList<ExpressionElement>> otherLociListIterator = chunkToCheck.iterator(); otherLociListIterator.hasNext(); )
        {

          ArrayList<ExpressionElement> otherLociList = otherLociListIterator.next();
          int internalMinCoord = Integer.MAX_VALUE;
          int internalMaxCoord = Integer.MIN_VALUE;
          double flankingLocusAbundance = 0.0;
          for ( ExpressionElement flankingElement : otherLociList )
          {
            HashMap<String, ExpressionElement> originalFlankingRow = expressionMatrix.get( flankingElement.sequence );
            if ( flankingElement.startCoord.get( 0 ) < internalMinCoord )
            {
              internalMinCoord = flankingElement.startCoord.get( 0 );
            }
            if ( flankingElement.endCoord.get( 0 ) > internalMaxCoord )
            {
              internalMaxCoord = flankingElement.endCoord.get( 0 );
            }
            for ( ArrayList<String> sampleList : expressionMatrix.getFileNames() )
            {
              if ( sampleList.size() == 1 )//no replicates
              {
                if ( originalFlankingRow.containsKey( sampleList.get( 0 ) ) )
                {
                  //expressionSeries[sampleIndex] += element.normalisedAbundance;
                  flankingLocusAbundance += originalFlankingRow.get( sampleList.get( 0 ) ).normalisedAbundance;
                }
              }
              else
              {
                double mean = 0.0;
                for ( String replicateName : sampleList )
                {
                  if ( originalFlankingRow.containsKey( replicateName ) )
                  {
                    //mean += element.normalisedAbundance;
                    mean += originalFlankingRow.get( replicateName ).normalisedAbundance;
                  }
                }
                mean /= sampleList.size();
                flankingLocusAbundance += mean;
              }
            }

          }
          if ( ( internalMinCoord >= flankStart ) && ( internalMinCoord <= flankEnd ) )
          {
            //repeat above procedure for these locus
            flankingLociLengths.add( internalMaxCoord - internalMinCoord );
            flankingLociTotalAbundances.add( flankingLocusAbundance );

          }
          else
          {
            if ( ( internalMaxCoord >= flankStart ) && ( internalMaxCoord <= flankEnd ) )
            {
              //repeat above procedure for these locus
              flankingLociLengths.add( internalMaxCoord - internalMinCoord );
              flankingLociTotalAbundances.add( flankingLocusAbundance );

            }
          }
        }
        if(flankingLociTotalAbundances.size() == 1)
        {
          listOfLociToReturn.add( lociList );
        }
        else
        {

          double meanLengths = 0.0;
          double meanAbundance = 0.0;
          double SD_Length;
          double SD_Abundance;

          for ( int i = 0; i < flankingLociLengths.size(); i++ )
          {
            meanLengths += flankingLociLengths.get( i );
            meanAbundance += flankingLociTotalAbundances.get( i );
          }
          //+1 to account for initial locus
          double N = ( (double) flankingLociLengths.size() );
          meanLengths /= N;
          meanAbundance /= N;
          /*
          * SAMPLE DISTRIBUTION:
          *
          * S = (SQRT(SUM((value-mean)^2)))/N
          *
          */
          //        if(flankingLociLengths.size() > 1)
          //          System.out.println( "" );

          double sum_length_minus_mean_sq = 0.0;
          double sum_abund_minus_mean_sq = 0.0;
          for ( int i = 0; i < flankingLociLengths.size(); i++ )
          {
            sum_length_minus_mean_sq += Math.pow( ( flankingLociLengths.get( i ) - meanLengths ), 2 );// * ( flankingLociLengths.get( i ) - meanLengths );
            double abund_mean = ( flankingLociTotalAbundances.get( i ) - meanAbundance );
            double abund_mean_sq = abund_mean * abund_mean;
            sum_abund_minus_mean_sq += abund_mean_sq;//Math.pow (,2);// * ( flankingLociTotalAbundances.get( i ) - meanAbundance );
          }
          SD_Length = Math.sqrt( ( sum_length_minus_mean_sq / N ) );
          SD_Abundance = Math.sqrt( ( sum_abund_minus_mean_sq / N ) );

          if ( ( SD_Length != 0 ) || ( SD_Abundance != 0 ) )
          {
            double Z1 = ( lengthOfLocus - meanLengths ) / SD_Length;
            double Z2 = ( totalAbundance - meanAbundance ) / SD_Abundance;
            //if ( ( Z1 > 2 ) || ( Z2 > 2 ) )
            if ( ( Z2 > 2 ) )
            {
              listOfLociToReturn.add( lociList );
              //it.remove();
              //lociListIterator.remove();
              //valuesToRemove.add( currentLocusIndex );
            }
          }
          }
      }
    }
    this.trackerReset();
    return listOfLociToReturn;
//    int[] intArray = ArrayUtils.toPrimitive(valuesToRemove.toArray(new Integer[valuesToRemove.size()]));
//    for(int indexToRemove : intArray)
//    {
//      listOfLoci.remove( indexToRemove );
//    }
  }
}
