package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationServiceLayer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 * Holds a list of MAelements calculated for each expression value between
 * two samples
 * @author Matt
 */
public class MAList extends ArrayList<MAelement>
{
    String refSample;
    String obsSample;
    
    int base; // logarithmic base to use
    int offset;
    NormalisationType normType;
    
   // construction that all the constructors do.
   public MAList(String refSample, String obsSample, NormalisationType normType, int base, int offset)
   {
       super();
       this.refSample = refSample;
       this.obsSample = obsSample;
       this.base = base;
       this.offset = offset;
       this.normType = normType;
   }
   
  /**
   * Create a new MAList from two fasta files.
   * 
   * Only sequences with recorded counts in both files are used in accordance
   * to the calculation of M and A values
   * @param refSample
   * @param obsSample 
   */
  public MAList(File refSample, File obsSample, int base, int offset) throws IOException
  {   
    this(refSample.getName(), refSample.getName(), NormalisationType.NONE, base, offset);
    SRNAFastaReader refReader = new SRNAFastaReader(refSample);
    FastaMap refMap = new FastaMap(refReader.process());
    SRNAFastaReader obsReader = new SRNAFastaReader(obsSample);
    FastaMap obsMap = new FastaMap(obsReader.process());
    double totalRef = refMap.getTotalSeqCount();
    double totalObs = obsMap.getTotalSeqCount();
    
    for ( Entry<String, FastaRecord> thisRef : refMap.entrySet() )
    {
      String thisSeq = thisRef.getKey();
      if (obsMap.containsKey( thisSeq ))
      {
        double refCount = thisRef.getValue().getRealAbundance();
        double obsCount = obsMap.getAbundance( thisSeq );
        this.add( new MAelement(refCount, obsCount, totalRef, totalObs, base, offset));
      }
    }
  }
  
  /**
   * Calculates an MAList from un-normalised data
   * @param sem
   * @param refSample
   * @param obsSample 
   */
  public MAList(SparseExpressionMatrix sem, String refSample, String obsSample, int base, int offset)
  {
    this(sem, refSample, obsSample, base, offset, NormalisationType.NONE);
  }

  /**
   * Create a new MAList from a SparseExpressionMatrix using two of the samples in
   * the matrix
   * @param sem
   * @param refSample
   * @param obsSample 
   * @param normType the NormalisationType applied to the matrix that will be retrieved to calculate MA values
   */
  public MAList(SparseExpressionMatrix sem, String refSample, String obsSample, int base, int offset, NormalisationType normType)
  {
    this(refSample, obsSample, normType, base, offset);
    double numObs = sem.getTotalGenomeHit( sem.resolveSampleName( obsSample) );
    double numRef = sem.getTotalGenomeHit( sem.resolveSampleName( refSample) );
    // iterate over Sequence -> <<filename, expression>>
    // Retrieve abundaces to calculate M and A values for calculating factor with
    for ( Map.Entry<String, HashMap<String, ExpressionElement>> entry : sem.getEntries() )
    {
      // retrieve ExpressionElements for observed and reference samples for this
      // sequence
      // Sequences not in a particular sample (contain zero) will be returned as null
      ExpressionElement expObs = entry.getValue().get( obsSample );
      ExpressionElement expRef = entry.getValue().get( sem.resolveSampleName ( refSample ) );

      // Ignore reads that have nonzero expression for either of the samples since
      // log-fold changes can not be calculated using these anyway (see Robinson 2010)
      // for further explanation
      // zero value expression is returned as NULL from SparseExpressionMatrix
      //     CORNERCASE: BootstrapNormalisation currently leaves zero values in matrix
      //                 so we still need to test for zero unfortunately
      if( (expObs != null && expObs.getNormalisedAbundance( normType ) != 0) && (expRef != null && expRef.getNormalisedAbundance( normType ) != 0) )
      {
        // expression for this sequence and per total proportions
        double countObs = expObs.getNormalisedAbundance( normType );
        double countRef = expRef.getNormalisedAbundance( normType );
        
        // M and A value for this sequence
        // Note: MUST use log to base 2, in order to be able to get the correct factor
        // as a result of 2^result
        this.add( new MAelement(countRef, countObs, numRef, numObs, base, offset, true));
      }
    }
  }
  
  /**
   * Deprecated - use MAListServiceLayer instead.
   * @param databaseSession
   * @param refSample
   * @param obsSample
   * @param refLibSize
   * @param obsLibSize
   * @param normType
   * @param base
   * @param offset
   * @deprecated
   */
  @Deprecated
  public MAList(Session databaseSession, String refSample, String obsSample, double refLibSize, double obsLibSize, NormalisationType normType, int base, int offset)
  {
      this(refSample, obsSample, normType, base, offset);
      final String SQL_GET_ABUNDANCES = "SELECT RNA_Sequence,abundance,File_Name FROM Sequences " + NormalisationServiceLayer.WHERE_ALIGNED 
              + " AND File_Name IN (:filex, :filey) "
              + "ORDER BY RNA_Sequence,File_name";
      

      
      ScrollableResults abundances = databaseSession.createSQLQuery(SQL_GET_ABUNDANCES)
              .setParameter("filex", refSample).setParameter("filey", obsSample)
              .scroll(ScrollMode.FORWARD_ONLY);
      
      if(abundances.next())
      {
        String refSeq = (String) abundances.get(0);
        double abundance = ((Integer) abundances.get(1)).doubleValue();
        while(abundances.next())
        {
            String obsSeq = (String) abundances.get(0);
            double thisAbundance = ((Integer) abundances.get(1)).doubleValue();
            if(refSeq.equals(obsSeq))
            {
                this.add(new MAelement(abundance, thisAbundance, refLibSize, obsLibSize, base, offset));
            }
            else
            {
                refSeq = obsSeq;
                abundance = thisAbundance;
            }
        }
      }
  }
  
  public double[] getAllM()
  {
    double [] mList = new double[this.size()];

    int i = 0;
    for(MAelement e : this)
    {
      mList[i] = e.getM();
      i++;
    }
    return(mList);
  }
  
  public double[] getAllA()
  {
    double [] aList = new double[this.size()];

    int i = 0;
    for(MAelement e : this)
    {
      aList[i] = e.getA();
      i++;
    }
    return(aList);
  }
  
  public void sortByA()
  {
    sortBy(MAparam.A);
  }
  
  public void sortByM()
  {
    sortBy(MAparam.M);
  }
  
  private void sortBy(MAparam param)
  {
    switch(param)
    {
      case M:
        Collections.sort( this, new MComparator());
        break;
      case A:
        Collections.sort( this, new AComparator());
        break;
      default:
        throw new UnsupportedOperationException("Can't sort by param" + param);
    }
  }
  
  public void trimByM(double trim){ trimBy(trim, MAparam.M ); }
  public void trimByA(double trim){ trimBy(trim, MAparam.A ); }

  
  /**
   * Trims the MAelements (sets keep to true) for all param that is within
   * the bounds specified by factor and 1 - factor
   * 
   * The method of trimming differs slightly to that in R.
   * 
   * The method used here is to find the threshold of indices of ordered elements which
   * would be included in R. The elements are ordered using comparators, which means
   * that if elements are equal (highly probable at the lower end of abundances), the
   * original order is preserved. In that sense the elements that are trimmed when
   * there is a tie is random.
   * 
   * In contrast R TMM trimming works by an "all-or-none" approach, where the ties
   * are resolved as the average of tied values e.g. tied values that would be ranked
   * 4,5,6,7 would be assigned 5.5. If 5.5 is above the trimming threshold, all values are
   * kept.
   * 
   * This hopefully should not make a massive difference, but testing for this is needed.
   * 
   * @param trim percentage threshold to trim each end of the distribution by
   * @param param the MAparam of this MAList to trim on
   */
  protected void trimBy(double trim, MAparam param)
  {    
    if (param.equals( MAparam.WEIGHT ))
      throw new IllegalArgumentException("Can not trim by the weights in an MA list");
    
    int lowTrim = ((int) ( ( trim / 100 ) * this.size() ) + 1)  - 1;
                                                             // ^ Off-by-one
                                                             // array index adjustment
    int highTrim = ((int) (  ( ( 1 - (trim / 100) ) * this.size() ) + 1 ) ) - 1;
    
    System.out.println("Trim by " + lowTrim + " : " + highTrim);
    
    sortBy(param);
    
    // This is where the trimming differs to the TMM in R.
    // see method comments
    HashMap<Double, HashMap<Integer, MAelement>> rankList = new HashMap<>();
    for(int i = 0; i < this.size(); i++)
    {
      MAelement thisMA = this.get( i );
      double thisVal = thisMA.getVal(param);

      if(rankList.containsKey( thisVal ))
      {
        rankList.get( thisVal ).put( i, thisMA );
      }
      else
      {
        HashMap<Integer, MAelement> newMAmap = new HashMap<>();
        newMAmap.put( i, thisMA );
        rankList.put(thisVal, newMAmap);
      }
    }
    
    // Find mean of each tie and check for trim
    for(Entry<Double, HashMap<Integer, MAelement>> e : rankList.entrySet())
    {
      //double thisM = e.getKey();
      HashMap<Integer, MAelement> ranks = e.getValue();
      int rsum = 0;
      for(int rank : ranks.keySet())
      {
        rsum += rank;
      }
      double meanrank = rsum/ranks.size();
      if( meanrank < lowTrim | meanrank > highTrim)
      {
        //update keep to true on all MAelement with this rank
        for(MAelement thisMA : ranks.values())
        {
          thisMA.setKeep(false);
        }
      }
    }
  }
  
  public void writeToCsv(File csvout) throws IOException
  {
      FileWriter csvWriter = new FileWriter(csvout);
      csvWriter.append("M,A\n");
      for (MAelement ma : this)
      {
          String m = Double.toString(ma.getM());
          String a = Double.toString(ma.getA());
          csvWriter.append(m).append(",").append(a).append("\n");
      }
      csvWriter.flush();
      csvWriter.close();
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    int i = 1;
    int space = String.valueOf( this.size() ).length();
    for (MAelement ma : this)
    {
      sb.append( String.format( "%"+space+"d", i) ).append( ": " ).append( ma.toString() ).append( "\n" );
      i++;
    }
    return(sb.toString());
  }
  
  /***
   * The following are used for sorting lists by either M or A values
   */
  class MComparator implements Comparator<MAelement>
  {

    @Override
    public int compare( MAelement a, MAelement b )
    {
      return( a.getM() < b.getM() ? -1 : (a.getM() == b.getM() ? 0  : 1));
    }
    
  }
  
  class AComparator implements Comparator<MAelement>
  {

    @Override
    public int compare( MAelement a, MAelement b )
    {
      return( a.getA() < b.getA() ? -1 : (a.getA() == b.getA() ? 0  : 1));
    }
    
  }
  
  /**
   * Helps building an MAList with optional parameters
   * for calculating
   */
  public class MAListBuilder
  {
      
  }
    
}
