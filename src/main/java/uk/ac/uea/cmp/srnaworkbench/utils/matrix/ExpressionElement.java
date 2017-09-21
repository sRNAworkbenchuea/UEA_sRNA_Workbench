/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.SequenceStrand;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;


/**
 * Light weight class for storing expression data
 * 
 * @author w0445959
 */
public class ExpressionElement implements Comparable
{
  public ArrayList<Integer> startCoord;
  public ArrayList<Integer> endCoord;
  public String chromosomeID;
  public String sample_name;
  
  // TODO: Robust ExpressionElement class that can store multiple Normalisation values
  // These should be made private to prevent alteration without setting
  // other fields, but SparseExpressionMatrix relies to much on them being
  // public for now.
  public double originalAbundance;
  public double weightedAbundance;
  
  // Normlised abundances are kept in a HashMap now
  public double normalisedAbundance;
  public NormalisationType normaliseMethod;
  
  //
  HashMap<NormalisationType, Float> normalisedAbundances;
  
  public String UDS_pattern;
  public double confidenceInterval[];
  public double meanAbundance;
  public String sample_ID;
  public String sequence;
  public double individual_abundance;
  public String printableSampleName;
  public SequenceStrand strand;

  private boolean weighted;
  private boolean normalised;
  
  public ExpressionElement()
  {
    startCoord = new ArrayList<Integer>();
    endCoord = new ArrayList<Integer>();
    chromosomeID = "";
    sample_name = "";
    originalAbundance = 0.0;
    individual_abundance = 0.0;
    normalisedAbundance = 0.0;
    weightedAbundance = 0.0;
    meanAbundance = 0.0;
    normaliseMethod = NormalisationType.NONE;
    normalisedAbundances = new HashMap<>();
    UDS_pattern = "---";
    confidenceInterval = new double[2];
    confidenceInterval[0] = 0.0;
    confidenceInterval[1] = 0.0;
    
    sample_ID = "-1";
    sequence = "";
    printableSampleName = "";
    strand = SequenceStrand.UNKNOWN;
    
    weighted = false;
    normalised = false; 
  }
  
  //copy constructor 
  public ExpressionElement(ExpressionElement e)
  {
    startCoord = new ArrayList<Integer>();
    endCoord = new ArrayList<Integer>();
    
    for(Integer start_coord : e.startCoord)
      startCoord.add( start_coord );
    for(Integer end_coord : e.endCoord)
      startCoord.add( end_coord );
    chromosomeID = e.chromosomeID;
    sample_name = e.sample_name;
    originalAbundance = e.originalAbundance;
    normalisedAbundance = e.normalisedAbundance;
    weightedAbundance = e.weightedAbundance;
    normaliseMethod = e.normaliseMethod;
    UDS_pattern = e.UDS_pattern;
    meanAbundance = e.meanAbundance;
    individual_abundance = e.individual_abundance;
    confidenceInterval = new double[2];
    int i = 0;
    for(double ci_val : e.confidenceInterval)
    {
      confidenceInterval[i] = ci_val;
      i++;
    }
    sample_ID = e.sample_ID;
    sequence = e.sequence;
    printableSampleName = e.printableSampleName;
    strand = e.strand;
    
    weighted = e.weighted;
    normalised = e.normalised;
  }
  
  public boolean isWeighted(){ return weighted; }
  public boolean isNormalised(){ return normalised; }
  public boolean isNormalised(NormalisationType type){return this.normalisedAbundances.containsKey( type ); }
  public double getOriginalAbundance(){ return this.originalAbundance; }

  /**
   * 
   * @return The weighted abundance for this element
   * @throws an exception if the weighted abundance was not set before hand
   */
  public double getWeightedAbundance()
  { 
    if (isWeighted())
    {
      return this.weightedAbundance; 
    }
    else
    {
      throw new IllegalArgumentException("Attempt to retrieve weighted abundance before adundance was weighted");
    }
  }
  
  /**
   * Returns the abundance for this element normalised by the method
   * specified as an argument. If NONE is specified, returns the original
   * abundance
   * @param 
   * @return the abundance normalised by the method specified, or the original
   * abundance if NONE is specifically specified throws an exception if the specified
   * method was never used on this ExpressionElement
   */
  public float getNormalisedAbundance(NormalisationType type){ 
    if(type.equals( NormalisationType.NONE ))
    {
      return( (float) this.getOriginalAbundance());
    }
    if(this.normalisedAbundances.containsKey( type )){
      return this.normalisedAbundances.get( type ); 
    }
    else{
      throw new IllegalArgumentException("Attempt to retrieve a normalised expression value"
        + " when the normalisation method was not used");
    }
  }
  
  /**
   * Returns the most relevant abundance value given the
   * work that has been done on this ExpressionElement
   * 
   * This method is deprecated. Use getExpression() instead. Note that getExpresison()
   * will never return any normalised entries, since ExpressionElement can now contain
   * more than one normalised abundance.
   * 
   * @return Normalised abundance if it has been set. If not, returns the weighted
   * abundance and if not that then it settles for the original abundance
   */
  @Deprecated
  public double getAbundance()
  {
    if( isNormalised() )
    {
      return this.normalisedAbundance;
    }
    else if( isWeighted() )
    {
      return this.weightedAbundance;
    }
    else
    {
      return this.originalAbundance;
    }
  }
  
  /**
   * Get the abundance before normalisation
   * @return the weighted abundance if weighting has been calculated. Otherwise,
   * returns the original abundance
   */
  public double getExpression()
  {
    if( isWeighted() )
    {
      return this.weightedAbundance;
    }
    else
    {
      return this.originalAbundance;
    }
  }
  
  public void setOriginalAbundance(double abundance){this.originalAbundance = abundance;}
  public void setWeightedAbundance(double abundance)
  { 
    this.weightedAbundance = abundance;
    this.weighted = true;
  }
  
  public void setNormalisedAbundance(NormalisationType method, float abundance)
  {
    // This field stores the most recent normalisation process
    this.normaliseMethod = method;
    this.normalisedAbundance = abundance;
    
    // all normalisation process are stored in here as floats
    this.normalisedAbundances.put( method, abundance );
    this.normalised = true;
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( " List of Start Coords: " )
      .append( startCoord)
      .append( " List of End Coords")
      .append( endCoord )
      .append( " chromosomeID: ")
      .append( chromosomeID )
      .append( " sample name: ")
      .append( sample_name )
      .append( " original abundance: ")
      .append( originalAbundance )
      .append( " original abundance: ")
      .append( individual_abundance )
      .append( " normalised: ")
      .append( normalisedAbundance )
      .append( " normalisation method: ")
      .append( normaliseMethod )
      .append( "Mean abundance over replicates")
      .append( meanAbundance )
      .append( " Sample ID: ")
      .append( sample_ID )
      .append( " UDS Pattern: ")
      .append( UDS_pattern )
      .append( " CI: ")
      .append( confidenceInterval[0])
      .append( " , ")
      .append(confidenceInterval[1])
      .append( "full sample name")
      .append( printableSampleName )
      .append( " Sequence: ")
      .append( sequence )
      .append(" Strand: ")
      .append( strand );
    
    
    return sb.toString();
    
  }

  /**
   * Comparisons are made using weighted abundance if isWeighted() is true.
   * Otherwise comparisons are made with original abundance
   * @param t
   * @return 
   */
  @Override
  public int compareTo( Object t )
  {
    Double first = ( (ExpressionElement) t).getExpression();
    Double second = this.getExpression();
    return first.compareTo( second );
  }
}
