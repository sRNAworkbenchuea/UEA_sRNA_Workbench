package uk.ac.uea.cmp.srnaworkbench.tools.normalise;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaWriter;
import uk.ac.uea.cmp.srnaworkbench.tools.RunnableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.TimeableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterResultsPanel;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAelement;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StatusTracker;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 *
 * @author w0445959
 */
public class NormalisationProcess extends TimeableTool
{
  private final static String TOOL_NAME = "NORMALISATION";
  private final static double UPPER_QUARTILE_VALUE = 0.75;
  private final static double TRACKER_STEP = 100;
  public final static double UPSCALE_CONSTANT = 1000000;
  protected NormalisationType myType;
  private SparseExpressionMatrix myDataMatrix;
  protected NormalisationParams params;
  protected boolean completed = false;
  File outDir = null;
  
  // For BootstrapNormalisation
  Random r = new Random();
  /**
   * Used to set the seed for the Random object used in BootstrapNormalisation
   */
  public void setRandomSeed(int seed)
  {
    r.setSeed( seed );
  }

  public NormalisationProcess (NormalisationType type, 
                               ArrayList<ArrayList<File>> inputFileList, File genomeFile,
                               FilterResultsPanel filterResultsPanel, NormalisationParams params, StatusTracker tracker, File outDir)
  {
    this(type, new SparseExpressionMatrix( tracker, filterResultsPanel, inputFileList, genomeFile), params, tracker, outDir);
  }
  
  /**
   * Constructs a NormalisationProcess from an in-memory SparseExpressionMatrix.
   * This process will not write any output - the host is responsible for the result
   * of the matrix
   * @param type
   * @param matrix
   * @param tracker 
   */
  public NormalisationProcess( NormalisationType type, SparseExpressionMatrix matrix, 
                               StatusTracker tracker, File outDir )
  {
    this( type, matrix, new NormalisationParams(), tracker, outDir );
  }

  public NormalisationProcess( NormalisationType type, 
                               SparseExpressionMatrix matrix, 
                               NormalisationParams params, StatusTracker tracker, File outDir )
  {
    super( TOOL_NAME, tracker );
    myType = type;
    myDataMatrix = matrix;
    this.params = params;
    this.outDir = outDir;
  }

  protected void performUQNormalisation()
  {
    // get upper quartiles
    trackerInitUnknownRuntime( "Calculating percentiles"  );
    HashMap<String, Double> uqMap = calculatePercentiles(NormalisationProcess.UPPER_QUARTILE_VALUE);
    
    // Find mean - only needed to upscale resulting value (optional)
    double uqSum = 0;
    for (double uq : uqMap.values())
    {
      uqSum += uq;
    }
    double uqMean = uqSum/uqMap.size();
    
    trackerInitUnknownRuntime( "Normalising samples" );
    for( Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries() )
    {
      double numHits = myDataMatrix.countGenomeHits( entry.getKey() );
      for( Entry<String, ExpressionElement> e : entry.getValue().entrySet() )
      {
        String sample = e.getKey();
        ExpressionElement exp = e.getValue();
        //exp.normaliseMethod = NormalisationType.UPPER_QUARTILE;
        //exp.weightedAbundance = exp.originalAbundance/numHits;
        if (this.params.isWeightByHits() && !exp.isWeighted())
        {
          exp.setWeightedAbundance( exp.getOriginalAbundance() / numHits );
        }
          //exp.normalisedAbundance = exp.weightedAbundance/uqMap.get( sample );
        float normalisedAbundance = (float) (exp.getExpression()/uqMap.get( sample ));
        
        //OPTIONAL - multiply result by mean of UQ to increase the very small result
        //exp.normalisedAbundance *= uqMean;
        normalisedAbundance += uqMean;
        exp.setNormalisedAbundance( NormalisationType.UPPER_QUARTILE, normalisedAbundance);
      }
    }
  }

    protected void performDEseqNormalisation() {
        throw new UnsupportedOperationException("DEseq not yet supported on files without a database."); 
    }
  
  class EntryCompare implements Comparator<Entry<String, Integer>>
  {
    @Override
    public int compare( Entry<String, Integer> o1, Entry<String, Integer> o2 )
    {
      Integer v1 = o1.getValue();
      Integer v2 = o2.getValue();
      return(Integer.compare( v1, v2));
    }
    
  }
  
  /**
   * Perform a Bootstrap Normalisation.
   * 
   * The smallest library size is found and all other libraries
   * are sampled down towards this size.
   * 
   * CORNER CASES:
   *  - EspressionElements are normalised by setting a seperate field as the normalised
   * value for each sequence. In the case where the ExpressionElement was never selected,
   * we set this value to 0 but keep the ExpressionElement since it exists as an original
   * abundance.
   * 
   * 
   */
  protected void performBootstrapNormalisation()
  {
    

    TreeSet<Entry<String, Integer>> libSize_set = new TreeSet<>(new EntryCompare());
    
    for(String samplename : this.myDataMatrix.getFlattenedFileNames())
    {
      double thisLibSize = this.myDataMatrix.getTotalGenomeHit( samplename );
      
      Entry<String, Integer> size = new SimpleEntry<>(samplename, (int) thisLibSize);
      
      libSize_set.add(size);
      /*      if(thisLibSize < minLibSize)
       * {
       * minSample = samplename;
       * minLibSize = thisLibSize;
       * }*/
    }
    
    // Find smallest library size N
    // TreeSet sorts lowest to highest
    String minSample = libSize_set.first().getKey();
    double minLibSize = libSize_set.first().getValue();
    
    
    // Convert to int
    int minLibSize_int = (int) minLibSize;
    
    if(minSample == null)
      throw new IllegalArgumentException("Could not find minimum sample size! Something has gone wrong");
    
    // For each other library, randomly select (without replacement) ExpressionElements to keep N times
    for(String samplename : this.myDataMatrix.getFlattenedFileNames())
    {
      // holds all sequences in this sample
      // FIXME: Bootstrap - get the initial capacity from the data structure created by finding the min sample size
      List<String> seqsInSample = new ArrayList<>((int)this.myDataMatrix.getTotalGenomeHit( samplename ));
      
      // Don't resample smallest library!
      if(!samplename.equals( minSample ))
      {
        for(Entry<String, HashMap<String, ExpressionElement>> entry : this.myDataMatrix.getEntries())
        {
          String sequence = entry.getKey();
          HashMap<String, ExpressionElement> samples2ee = entry.getValue();
          if(samples2ee.containsKey(samplename))
          {
            double seqCount = samples2ee.get( samplename ).getOriginalAbundance();

            for(int i=0; i < seqCount; i++)
            {
              seqsInSample.add( sequence );
            }
          }
            
        }
        // Shuffle the sequence list
        Collections.shuffle(seqsInSample, r);
        
        // Replace sequence list with a list of randomly selected sequences
        seqsInSample = seqsInSample.subList( 0, minLibSize_int);
        
        // aggregate the list
        HashMap<String, Integer> newSeqCounts = new HashMap<>();
        for(String seq : seqsInSample)
        {
          if(newSeqCounts.get( seq ) == null)
          {
            newSeqCounts.put( seq, 1 );
          }
          else
          {
            int newCount = newSeqCounts.get( seq ) + 1;
            newSeqCounts.put( seq, newCount );
          }
        }
        
        // Go through the matrix and assign normalised values. Assign 0
        // if sequence was never sampled
        for(Entry<String, HashMap<String, ExpressionElement>> matrix_entry : this.myDataMatrix.getEntries())
        {
          ExpressionElement thisE = matrix_entry.getValue().get( samplename );
          if(thisE != null)
          {
            String thisSequence = matrix_entry.getKey();
            Integer normalisedValue = newSeqCounts.get( thisSequence );
            if(normalisedValue == null)
            {
              normalisedValue = 0;
            }
            thisE.setWeightedAbundance( thisE.getOriginalAbundance() / this.myDataMatrix.countGenomeHits( thisSequence) );
            thisE.setNormalisedAbundance( NormalisationType.BOOTSTRAP, normalisedValue);
          }
        }
        
        
      }
      else
      {
          //This is the min size sample. It is not resampled, so we copy over
          // the original abundance to the normalised value
          for (Entry<String, HashMap<String, ExpressionElement>> matrix_entry : this.myDataMatrix.getEntries())
          {
              ExpressionElement thisE = matrix_entry.getValue().get(samplename);
              if (thisE != null)
              {
                  thisE.setNormalisedAbundance( NormalisationType.BOOTSTRAP, (float) thisE.getOriginalAbundance());
              }
          }
      }
    }
    
    // Assess quality and accuracy of libraries
    
  }

  protected void performPTNormalisation()
  {
    trackerInitKnownRuntime( "Normalising samples", (int)(myDataMatrix.getRowCount()/  NormalisationProcess.TRACKER_STEP) );
    int i = 0;
    for ( Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries() )
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      double numHits = myDataMatrix.countGenomeHits( entry.getKey() );
      for ( Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {

        ExpressionElement currentElement = rowEntry.getValue();
        //currentElement.normaliseMethod = NormalisationType.TOTAL_COUNT;
        //currentElement.individual_abundance = 
        //currentElement.weightedAbundance = currentElement.originalAbundance / numHits;
        if (this.params.isWeightByHits() && !currentElement.isWeighted())
        {
          currentElement.setWeightedAbundance( currentElement.getOriginalAbundance() / numHits );
        }
        
        float normalisedAbundance = (float) ( ( currentElement.getExpression()
          / myDataMatrix.getTotalGenomeHit( rowEntry.getKey() ) )
          * NormalisationProcess.UPSCALE_CONSTANT );
        
        currentElement.setNormalisedAbundance(NormalisationType.TOTAL_COUNT, normalisedAbundance);

      }
      i++;
      if(i % NormalisationProcess.TRACKER_STEP == 0)
      {
        trackerIncrement();
      }
    }
  }

  public SparseExpressionMatrix getMatrix()
  {
    return myDataMatrix;
  }

  protected void performTMNormalisation() throws IOException
  {
    ArrayList<ArrayList<String>> sampleNames = this.myDataMatrix.getFileNames();
    String referenceSampleName = this.params.getRefSample();
    if ( referenceSampleName.equals( "" ) || referenceSampleName.equals(NormalisationParams.STR_AUTO_CHOOSE ) )
    {
      // currently throws an exception
      // throw new IllegalArgumentException( "Blank \"Reference Sample\" parameter currently not supported. Please supply a sample name to use as the reference sample" );
      referenceSampleName = calculateReferenceSample();
    }
    
    //Properly resolve the sample name to what we need to access the data with
    // This will return null if it can't find sample name
    String referenceSampleName_current = myDataMatrix.resolveSampleName( referenceSampleName ); 
      
    //Immediately throw a party if the refSample is not a used sample name
    if (referenceSampleName_current == null) 
      throw new IllegalArgumentException("Unknown reference sample name: " + referenceSampleName); // the party in question 

    int mTrim = this.params.getMtrim();
    int aTrim = this.params.getAtrim();
    boolean doWeighting = this.params.isWeightFactors();
    
    // Set tracker by number of samples that need to be normalised against reference
    int numSamples = this.myDataMatrix.getColumnCount();    
    trackerInitKnownRuntime( "Normalising samples", numSamples - 1);

    HashMap<String, Double> factorMap = new HashMap<>();
    // Loop over all samples
    double fsum = 0;
    for ( ArrayList<String> thisSampleSet : sampleNames )
    {
      for ( String thisSampleName : thisSampleSet )
      {
        double thisFactor = 1;
        // Find normalisation factor if current sample name is not the reference
        // otherwise the factor is set to 1 i.e. ref sample norm values are abundance*1
        if ( !thisSampleName.equals( referenceSampleName_current ) )
        {
          thisFactor = pairwiseTMfactor( referenceSampleName_current, thisSampleName, mTrim, aTrim, doWeighting );
        }
          factorMap.put( thisSampleName, thisFactor);
          fsum += Math.log( thisFactor );
      }
    }
    
    // for symmetry, allow factors to multiply to 1
    // take mean of factors and divide factor by this
    double fsym = Math.exp(fsum/factorMap.size());
      
    for ( ArrayList<String> thisSampleSet : sampleNames )
    {
      for ( String thisSampleName : thisSampleSet )
      {
        // make factors product to 1
        double thisFactor = factorMap.get( thisSampleName );
        double symFactor = thisFactor/fsym;
        System.out.println(thisSampleName+": "+symFactor);
        // Iterate over each expression element in this sample and normalise the abundance by thisFactor
        for ( Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries() )
        {
          // Filename -> expressionelement
          HashMap<String, ExpressionElement> row = entry.getValue();
          ExpressionElement thisElement = row.get( thisSampleName );
          if(thisElement != null){
            //thisElement.normaliseMethod = NormalisationType.TRIMMED_MEAN;
            double numHits = myDataMatrix.countGenomeHits( entry.getKey() );
            //thisElement.weightedAbundance = thisElement.originalAbundance / numHits;
            if (this.params.isWeightByHits() && !thisElement.isWeighted())
            {
              thisElement.setWeightedAbundance( thisElement.getOriginalAbundance() / numHits );
            }
            //thisElement.normalisedAbundance = ( thisElement.weightedAbundance / symFactor ) * NormalisationProcess.UPSCALE_CONSTANT;
            float normAbundance = (float)( ( thisElement.getExpression() /  symFactor ) * NormalisationProcess.UPSCALE_CONSTANT);
            
            thisElement.setNormalisedAbundance( NormalisationType.TRIMMED_MEAN, normAbundance );
          }
        }
        // Normalisation between these samples done, increment tracker
        trackerIncrement();
        }
      }
    }

  /**
   * Method used in TM normalisation paper to automatically find the "best"
   * reference sample.
   *
   * This is the *exact* method used in the R implementation.
   *
   * This is potentially extremely intensive because it involves finding the
   * upper quartile for each sample.
   *
   * @return Name of the sample whose upper quartile is closest to the average
   * of upper quartiles
   */
  private String calculateReferenceSample()
  {
    // If reference sample is not given - find sample by taking the sample with
    // a mean closest to the group mean

    HashMap<String, Double> uqMap = calculatePercentiles(NormalisationProcess.UPPER_QUARTILE_VALUE);

    // Sum of UQ values
    double uqSum = 0;
    for (double uq : uqMap.values())
    {
      uqSum += uq;
    }
    
    // Get mean of UQs
    double uqMean = uqSum / uqMap.size();
    
    // Finally, work out which UQ is closest to the mean
    String refSample = "";
    double smallestDiff = -1;
    for ( String sampleName : uqMap.keySet())
    {
      double thisUQ = uqMap.get( sampleName );
      double thisDiff = Math.abs( uqMean - thisUQ );
      if(smallestDiff == -1 || thisDiff < smallestDiff)
      {
        smallestDiff = thisDiff;
        refSample = sampleName;
      }
    }
    
    System.out.println("UQs: " + uqMap.toString());
    System.out.println("Mean: " + uqMean);
    System.out.println("Reference: " + refSample);
    // Return the candidate reference sample
    return (refSample);
  }
  
  /***
   * Calculate percentile values for each sample using the specified percentile
   * 
   * @param percentile fraction between 0 and 1
   * @return HashMap<String, Double> mapping of samplename -> percentile value
   */
  private HashMap<String, Double> calculatePercentiles(double percentile)
  {
     HashMap<String, Double> pMap = new HashMap<>();
    
    // Initialise array to hold sorted counts
    HashMap<String, ArrayList<Double>> sampleCounts = new HashMap<>();
    for ( ArrayList<String> sampleSet : myDataMatrix.getFileNames() )
    {
      for ( String sampleName : sampleSet )
      {
        sampleCounts.put( sampleName, new ArrayList<Double>() );

      }
    }

    // For each sample, make an array of counts and put it into the sampleCounts hashmap
    for ( Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries() )
    {
      HashMap<String, ExpressionElement> row = entry.getValue();
      for ( Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        ExpressionElement thisElement = rowEntry.getValue();
        String thisName = thisElement.sample_name;
        double thisCount = thisElement.originalAbundance;

        // add to current abundance in the right place
        sampleCounts.get( thisName ).add( thisCount );
      }
    }
    
    // Sort all sampleCounts arrays
    for ( String sampleName : sampleCounts.keySet() )
    {
          

      ArrayList<Double> theseCounts = sampleCounts.get( sampleName );
      Collections.sort( theseCounts );
      // Derive upper quartile index FOR EACH SAMPLE
      // THIS IGNORES ZERO COUNTS INCORRECTLY
      int[] trims = getPercentileIndices(0.25, theseCounts.size());
      int hiQ = trims[1];
      //System.out.println(sampleName + ": " + theseCounts);
      System.out.println("trim at: " + hiQ);
      double thisUQ = theseCounts.get( hiQ );
      pMap.put( sampleName, thisUQ );
    }
    
    return(pMap);
  }
  
  /***
   * Given a ratio and a distribution size, find the "low" index and "high" index
   * for trimming the distribution with
   * @param trimRatio to trim the x% of values from the low end and high end of the array (expressed as a ratio between 0 and 1)
   * @param distributionSize the length of the array
   * @return an array with two elements - low index in 0th and high index in 1st
   */
  private int[] getPercentileIndices(double trimRatio, int distributionSize)
  {
    int lowTrim = ((int) ( trimRatio * distributionSize ) + 1) -1;
    int highTrim = (distributionSize + 1 - lowTrim) -1;
    return new int[]{lowTrim, highTrim};
  }
    
  /**
   * Calculate TM factor between a named reference sample and observed sample
   *
   * @param refSample Name for reference sample
   * @param obsSample Name for observed sample
   * @param mTrimRatio percentage of M data to trim - must be between 0 and 49
   * @param aTrimRatio percentage of A data to trim - must be between 0 and 49
   * @param doWeighting
   * @return
   */
  private double pairwiseTMfactor( String refSample, String obsSample,
                                   double mTrimRatio, double aTrimRatio, boolean doWeighting ) throws IOException
  {

    
    LOGGER.log( Level.INFO, "Normalising sample {0} against {1}", new Object[]{ obsSample, refSample });
    // Get total abundance for samples
    // sequences with nonzero expression one or more samples may need to be
    // excluded before total hits are counted
    // R implementation includes sequences with zero expression in the library count
    //   so this implementation is fine
    //double numObs = myDataMatrix.getTotalGenomeHit( obsSample );
    
    // I actually can't know the name of the sample before the SparseExpressionMatrix
    // is built, making this parameter hard to set.
    // Solved by using resolveSampleName()
    //double numRef = myDataMatrix.getTotalGenomeHit( myDataMatrix.resolveSampleName( refSample ) );
    
    // For each sequence, get the M value and A value and store in arrays
    // initialised to number of rows in expression matrix
    // TODO: Finish the weighting option
    //ArrayList<Double> weights = new ArrayList<>();
    
    
    //int valPointer = 0;

    LOGGER.log( Level.INFO, "Finding normalisation factor");
    
    // Check that there actually is data to normalise
    if (myDataMatrix.getEntries().size() < 1)
      throw new IOException("Resulting mapped data contains no entries.");
    
    MAList maList = new MAList(myDataMatrix, refSample, obsSample, 2, 0); 
    // trim abundances based on the M values and A values (iterate over abundances again)
    /**
     * Using Percentile -- is it really needed? Percentile mPercentile = new
     * Percentile(mTrimRatio); mPercentile.setData( mValues ); double trim =
     * mPercentile.evaluate();
     *
     * Percentile aPercentile = new Percentile(aTrimRatio); mPercentile.setData(
     * aValues ); double aPercentileValue = aPercentile.evaluate();
     *
     */
    // Sorting arrays by "hand" instead...
    

    // Will mTrim and aTrim be PERCENTAGES or FACTORS???   
    // These are the indices where the values for each percentile lie
    /*    int lowMtrim = ((int) ( ( mTrimRatio / 100 ) * maList.size() ) + 1) -1;
     * int highMtrim = (maList.size() + 1 - lowMtrim) -1;
     * 
     * int lowAtrim = ((int) ( ( aTrimRatio / 100 ) * maList.size() ) + 1) -1;
     * int highAtrim = (maList.size() + 1 - lowAtrim) -1;*/

    // calculate factor only on M values that satisfy trimmed constraints
    // Instead of setting which values to keep for M,
    // just iterate over keepable values only to save on time
    // Sort by M
    //Collections.sort( maList, new MComparator());
    maList.trimByM( aTrimRatio );
    //System.out.println(maList);
    maList.trimByA( mTrimRatio );
    //System.out.println( maList );

    return(getTMMfactor(maList, doWeighting));
  }
  
  protected double getTMMfactor(MAList maList, boolean doWeighting)
  {
          // iterate between trimmings
      // TODO: R implementation uses "average ties method" when ranking, which requires further implementation but might be fairer
      //for (int i = lowMtrim; i <= highMtrim; i++)
      double mSum = 0;
      double wSum = 0;
      double numKept = 0;
      for (int i = 0; i < maList.size(); i++) {
          MAelement thisma = maList.get(i);

      //System.out.printf( "%2f / %2f -- %s\n", thisma.getM(), thisma.getA(), thisma.toKeep());
          // Must satisfy trimming on A values too
          if (thisma.toKeep()) {
              numKept++;
              if (doWeighting) {
                  // Calculate sums for weighting
                  mSum += thisma.getM() / thisma.getWeighting();
                  wSum += 1 / thisma.getWeighting();
              } else {
                  // if not weighting, just calculate 2^mean of m values, so sum them up
                  mSum += thisma.getM();
              }
          }
      }

      // Calculate the factor
      double factor;
      if (doWeighting) {
          factor = Math.pow(2, mSum / wSum);
      } else {
          // Calculate 2^mean
          factor = Math.pow(2, mSum / numKept);
      }
      String factorReport = "Normalisation factor is " + factor;
      System.err.println(factorReport);
      LOGGER.log(Level.INFO, factorReport);
      return(factor);
  }
  
  
  
  protected void performQNormalisation()
  {
    boolean rnaseqMethod = params.isRNASEQqnorm();
    trackerInitUnknownRuntime( "Preparing to normalise" );
    
    // INITIALISE ARRAY OF SORTED COUNTS
    // myDataMatrix is a Map of Sequence -> {sample, count}. We want an array per sample
    // so that we can sort samples independently
    HashMap<String, ArrayList<ExpressionElement>> sampleCounts = new HashMap<>();
    HashMap<String, HashMap<Double, ArrayList<ExpressionElement>>> theseUniqueCounts = new HashMap<>();
    for ( ArrayList<String> sampleSet : myDataMatrix.getFileNames() )
    {
      for ( String sampleName : sampleSet )
      {
        sampleCounts.put( sampleName, new ArrayList<ExpressionElement>() );
        theseUniqueCounts.put( sampleName, new HashMap<Double, ArrayList<ExpressionElement>>() );
      }
    }
    
    //HashMap<String, HashMap<Double, ArrayList<ExpressionElement>>> uniqueCounts = new HashMap<>();

    // FILL SAMPLECOUNTS
    // For each sample, make an array of counts and put it into the sampleCounts hashmap
    // Each sample data is sorted into ascending order
    for ( Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries() )
    {
      String thisSeq = entry.getKey();
      HashMap<String, ExpressionElement> row = entry.getValue();
      
      for ( Entry<String, ExpressionElement> rowEntry : row.entrySet() )
      {
        String thisSample = rowEntry.getKey();
        HashMap<Double, ArrayList<ExpressionElement>> thisUniqueCounts = theseUniqueCounts.get( thisSample );
        ExpressionElement thisElement = rowEntry.getValue();
        
        // If required, weight the abundance by number of mappings for each sequence
        if (this.params.isWeightByHits() && !thisElement.isWeighted())
        {
          double numHits = myDataMatrix.countGenomeHits( thisSeq );
          thisElement.setWeightedAbundance(thisElement.getOriginalAbundance() / numHits);
        }
        // add to current abundance in the right place
        sampleCounts.get( thisSample ).add( thisElement );

        // Add to unique counts
        
        if(thisUniqueCounts.get( thisElement.getExpression() ) == null)
        {
          ArrayList<ExpressionElement> thisExpArray = new ArrayList<>();
          thisExpArray.add( thisElement );
          thisUniqueCounts.put( thisElement.getExpression(), thisExpArray);
        }
        else
        {
          thisUniqueCounts.get( thisElement.getExpression() ).add( thisElement );
        }
      }
    }
    
    trackerInitKnownRuntime( "Ranking sequences", sampleCounts.size() );
    
    // SORT SAMPLES INDEPENDENTLY
    // iterate over filled data structure sampleCounts to individually sort samples
    for ( Entry<String, ArrayList<ExpressionElement>> sampleEntry : sampleCounts.entrySet())
    {
      ArrayList<ExpressionElement> thisExpressionArray = sampleEntry.getValue();
      //Collections.sort( thisExpressionArray, Collections.reverseOrder() );
      /*      for (ExpressionElement e : thisExpressionArray)
       * {
       * System.out.print(e.getExpression()+", ");
       * }
       * System.out.println();*/
      // Sorts in descending order
      Collections.sort( thisExpressionArray);
      /*      for (ExpressionElement e : thisExpressionArray)
       * {
       * System.out.printf("%4.0f", e.getExpression());
       * }
       * System.out.println();*/
      trackerIncrement();
    }
    
    trackerInitKnownRuntime( "Normalising samples", (int)(myDataMatrix.getRowCount() / NormalisationProcess.TRACKER_STEP) );
    // SET MEAN COUNT FOR EACH ROW OF RANK MATRIX
    for(int i = 0; i < myDataMatrix.getRowCount(); i++)
    {
      //sum up counts for each sample and get a mean
      double thisSum = 0;
      for(ArrayList<ExpressionElement> sample : sampleCounts.values())
      {
        // If there is a non-zero value for this sequence in this sample
        if(i < sample.size())
        {        
          ExpressionElement thisEE = sample.get( i );
          //System.out.print(thisEE.getExpression() + ",");
          thisSum += thisEE.getExpression();
        }
      }
      
      // Find mean. Note that zero-counts are included in the average
      double thisMean = thisSum / sampleCounts.size();
      //System.out.println(":" + thisMean);
      
      // Finally, assign normalised abundances for the seqs at row i
      for(ArrayList<ExpressionElement> sample : sampleCounts.values())
      {
        // RNASEQ MODIFICATION 1: all zero abundances remain zero
        // Sequence of zero count is not stored, so sorted array will be shorter than
        // index if sequence at this rank position is zero
        if(i < sample.size())
        {
          ExpressionElement thisEE = sample.get( i );
          thisEE.setNormalisedAbundance( NormalisationType.QUANTILE, (float)thisMean );
        }
       
      }
      if( i % 100 == 0 )
      {
        trackerIncrement();
      }
    }
    
   // myDataMatrix.printMatrix( NormalisationType.QUANTILE );
    
    // RNASEQ MODIFICATION 2: All equal input values replaced with average
    // of normalised value   
    if(rnaseqMethod)
    {   
      for ( ArrayList<String> sampleSet : myDataMatrix.getFileNames() )
      {
        for ( String sampleName : sampleSet )
        {
          //System.out.println("\t"+sampleName);
          HashMap<Double, ArrayList<ExpressionElement>> thisUniqueCounts = theseUniqueCounts.get( sampleName );
          for(Entry<Double, ArrayList<ExpressionElement>> uniqueEntry : thisUniqueCounts.entrySet())
          {
            double uCount = uniqueEntry.getKey();
            ArrayList<ExpressionElement> equalCountElements = uniqueEntry.getValue();
            //System.out.print(uCount+": ");
            if(equalCountElements.size() >= 2)
            {
              
              float normSum = 0;
              for(ExpressionElement e : equalCountElements)
              {               
                float normCount = e.getNormalisedAbundance( NormalisationType.QUANTILE );
                //System.out.print(normCount + ", ");
                normSum += normCount;
              }
              float normMean = normSum / equalCountElements.size();
              //System.out.println(":: " + normMean);
              // Replace output normalisation with this for all elements
              for(ExpressionElement e : equalCountElements)
              {
                e.setNormalisedAbundance( NormalisationType.QUANTILE, normMean );
              }
            }
          }
        }
      }
    }
    
    //myDataMatrix.printMatrix( NormalisationType.QUANTILE );

    // No need to sort anything back to original positions due to referencing...?
  }
  

    @Override
    protected void processWithTimer() throws Exception
    {
        this.completed = false;
        // TODO: allow filter pane for EACH sample

            //preprocess and buld data matrix if it has not been done already
        //methods should do nothing if they have already been called
        myDataMatrix.preProcess(params.getMinLength(), params.getMaxLength());
        myDataMatrix.build();

        continueRun();
        System.gc();
        performNormalisation();
        continueRun();
        System.gc();

        writeToFasta(outDir);

        this.completed = true;
    }

  public boolean isCompleted()
  {
    return this.completed;
  }
  
  /**
   * Switches on NormalisationType to execute the correct "performNormalisation"
   * method
   */
  protected void performNormalisation() throws IOException
  {
    switch ( myType )
    {
      case NONE:
        LOGGER.log( Level.WARNING, "No normalisation mode selected, no action performed" );
        break;
      case TOTAL_COUNT:
        performPTNormalisation();
        break;
      case UPPER_QUARTILE:
        performUQNormalisation();
        break;
      case TRIMMED_MEAN:
        performTMNormalisation();
        break;
      case QUANTILE:
        performQNormalisation();
        break;
      case BOOTSTRAP:
        performBootstrapNormalisation();
        break;
      case DESEQ:
        performDEseqNormalisation();
        break;
      default:
        LOGGER.log( Level.WARNING, "No normalisation mode found, uninitilised variable? no action performed" );
    }
  }
  
  //FIXME: This method is not part of the Tool's process() method and so is out of the
  // loop wrt exception and error handling etc.
  public void writeToFasta(File dir) throws IOException
  {    
    String output_suffix = params.getOutSfx();
    this.trackerReset();
    this.trackerInitUnknownRuntime("Preparing to output results");
    // Initialise data structure to hold sequences in a way that they can be written
    // per sample
    HashMap<String, HashMap<String, Double>> sampleCounts = new HashMap<>();
    for ( ArrayList<String> sampleSet : myDataMatrix.getFileNames() )
    {
      for ( String sampleName : sampleSet )
      {
        sampleCounts.put( sampleName, new HashMap<String, Double>() );

      }
    }
    
    // fill data structure with sequence and normalised value
    for (Map.Entry<String, HashMap<String, ExpressionElement>> entry : myDataMatrix.getEntries())
    {
      String seq = entry.getKey();
      HashMap<String, ExpressionElement> seqEntry = entry.getValue();
      for (Map.Entry<String, ExpressionElement> thisSeqSample : seqEntry.entrySet())
      {
        String sample = thisSeqSample.getKey();
        Double normAbundance = thisSeqSample.getValue().normalisedAbundance;
        sampleCounts.get( sample ).put( seq, normAbundance );
      }
    }
    
    // write each sample to a seperate fasta
    this.trackerInitKnownRuntime( "Writing results to fasta", sampleCounts.size() );
    for (Map.Entry<String, HashMap<String, Double>> entry : sampleCounts.entrySet())
    {
      String sample = entry.getKey();
      File outputSample = new File(convertInputToOutputFilename(sample));
      
      SRNAFastaWriter thisWriter = new SRNAFastaWriter(outputSample);
      // FIXME: Is it appropriate to output normalised values in the same way as Integers?
      FastaMap fm = new FastaMap(entry.getValue());
      thisWriter.process( fm, true);
      this.trackerIncrement();
    }
    
    //getTracker().setFinished(true);
  }
  
  public String convertInputToOutputFilename(String inputFileName)
  {
    String output_suffix = params.getOutSfx();
    return( outDir.getPath() + DIR_SEPARATOR + 
      FilenameUtils.removeExtension( inputFileName ) + "_" + 
      this.myType.toString().toLowerCase() + output_suffix + ".fasta" );
  }
  
  public ArrayList<String> getOutputFilenames()
  {
    ArrayList<String> outFileNames = new ArrayList<>();
    for ( ArrayList<String> sampleSet : myDataMatrix.getFileNames() )
    {
      for ( String sampleName : sampleSet )
      {
        outFileNames.add( convertInputToOutputFilename(sampleName));
      }
    }
    return outFileNames;
  }
        
  
  @Override
  /**
   * Prints a table of sequence counts for each sample,
   * giving the original and normalised values
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    ArrayList<ArrayList<String>> samples = this.myDataMatrix.getFileNames();
    sb.append( String.format( "%40s", "Sequence")).append( "\t");
    // Loop over all samples
    for ( ArrayList<String> thisSampleSet : samples )
    {
      for ( String thisSampleName : thisSampleSet )
      {
        sb.append( thisSampleName).append( "\t");
      }
    }
    sb.append(LINE_SEPARATOR);
    
    for (Entry<String, HashMap<String, ExpressionElement>> entry : this.myDataMatrix.getEntries())
    {
      HashMap<String, ExpressionElement> seq = entry.getValue();
      
      sb.append(String.format( "%40s", entry.getKey())).append("\t");
    
      // Loop over all samples
      for ( ArrayList<String> thisSampleSet : samples )
      {
        for ( String thisSampleName : thisSampleSet )
        {
          String padding = "%"+thisSampleName.length()+"s";
          ExpressionElement exp = seq.get( thisSampleName);
          if(exp == null)
          {
            sb.append( String.format(padding, "NA/NA") );
          }
          else
          {
            sb.append( String.format(padding, exp.originalAbundance + "/" + exp.normalisedAbundance)).append( "\t");
          }
        }
      }
      sb.append( LINE_SEPARATOR);
    }

    return sb.toString();
  }

  public static void main( String[] args )
  {
    try
    {
      
      StatusTracker tracker = new StatusTracker(null, null);
      
      ArrayList<File> samples = new ArrayList<>();

      samples.add( new File( "test/data/norm/ath_366868_head.fa" ) );
      samples.add( new File( "test/data/norm/ath_366868_head2.fa" ) );

      ArrayList<ArrayList<File>> sampleList = new ArrayList<>();
      sampleList.add( samples );

      File genome = new File( "TutorialData/FASTA/GENOME/Ath_TAIR9.fa" );
      
      SparseExpressionMatrix sem = new SparseExpressionMatrix(tracker, null, sampleList, genome);
      
      sem.preProcess( 16, 40);
      sem.build();
      
      NormalisationParams params = new NormalisationParams();
      params.setWeightByHits( false );
      
      NormalisationProcess npTMM = new NormalisationProcess(NormalisationType.TRIMMED_MEAN, sem, params, null, new File("dist/User/output/"));
      //NormalisationProcess npPTM = new NormalisationProcess(NormalisationType.TOTAL_COUNT, sem, params, null, new File("dist/User/output/"));
      //NormalisationProcess npQNORM = new NormalisationProcess(NormalisationType.QUANTILE, sem, params, null, new File("dist/User/output/"));
      
      npTMM.process();
      //npPTM.process();
      //npQNORM.process();

      npTMM.getMatrix().writeToCsv( new File("dist/User/output/ath_tmm.csv"), NormalisationType.TRIMMED_MEAN);
      //npQNORM.getMatrix().writeToCsv( new File("dist/User/output/ath_qnorm.csv"), NormalisationType.QUANTILE);
      //npPTM.getMatrix().writeToCsv( new File("dist/User/output/ath_ptm.csv"), NormalisationType.TOTAL_COUNT);
   
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  public static void oldmain( String[] args )
  {
    Tools.initialCLISetup();

    ArrayList<File> samples = new ArrayList<>();
//    samples.add( new File( "TutorialData/FASTA/RNAOME/Sample1/366868.fa" ) );
//    samples.add( new File( "TutorialData/FASTA/RNAOME/Sample1/366869.fa" ) );

    samples.add( new File( "test/data/norm/ath_366868_head.fa" ) );
    samples.add( new File( "test/data/norm/ath_366868_head2.fa" ) );

    ArrayList<ArrayList<File>> sampleList = new ArrayList<>();
    sampleList.add( samples );

    File genome = new File( "TutorialData/FASTA/GENOME/Ath_TAIR9.fa" );
    
    

    SparseExpressionMatrix sem = new SparseExpressionMatrix(null, null, sampleList, genome);   
    try
    {
      sem.preProcess( 16, 40 );
    }
    catch ( IOException ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage());
    }
    try
    {
      sem.build();
    }
    catch ( Exception ex )
    {
      LOGGER.log(Level.SEVERE, ex.getMessage());
    }
    //NormalisationProcess np = new NormalisationProcess(NormalisationType.TRIMMED_MEAN, sem, null);
    //np.params.setRefSample( FilenameUtils.getBaseName( samples.get( 0).toString()) + "_0_filtered.fa");
    
    //ToolManager.getInstance().addTool( new NormalisationMainFrame());
    
    //np.performTMNormalisation();
    //np.performPTNormalisation();
    //np.myDataMatrix.printElements( 100);
    //System.out.print( np.toString() );
    //np.pairwiseTMFactor( samples.get( 0).toString(), samples.get(1).toString(), 30, 10, true);

    
  }
}
