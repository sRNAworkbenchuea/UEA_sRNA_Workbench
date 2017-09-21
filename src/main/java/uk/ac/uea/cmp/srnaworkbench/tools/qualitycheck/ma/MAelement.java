
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma;

import java.text.DecimalFormat;
import static uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAparam.M;

/**
 * *
 * Calculates and holds M and A values of a sequence between two samples. Also
 * holds optional weighting
 */
public class MAelement
{
  private double weighting;
  private double M;
  private double A;
  private boolean keep; // flag to set when finding MA values to keep
  
  private int base;
  private int offset;
  
  public final static int DEFAULT_BASE = 2;

  public MAelement( double refExpression, double obsExpression, double refTotal, double obsTotal, int base, int offset)
  {
    this ( refExpression, obsExpression, refTotal, obsTotal, base, offset, false);
  }
  /**
   * Create new MAelement using expression data between two samples
   *
   * @param refExpression
   * @param obsExpression
   * @param refTotal
   * @param obsTotal
   * @param base
   * @param keep
   * @param offset
   */
  public MAelement( double refExpression, double obsExpression, double refTotal, double obsTotal, int base, int offset, boolean keep)
  {
    double propRef = ( ( refExpression + offset ) / refTotal );
    double propObs = ( ( obsExpression + offset ) / obsTotal );

    this.M = calcM( propRef, propObs, base);
    this.A = calcA( propRef, propObs, base);
    
    if(Double.isNaN(this.M))
    {
        int a = 1;
    }


    this.weighting = calcWeighting(refExpression, refTotal, obsExpression, obsTotal);
    this.keep = keep;
  }

  /**
   * Calculates the M value (or fold change) for two expression levels @param{ref} and @param{obs}.
   * @param ref the reference expression level.
   * @param obs the observed expression level to compare against ref.
   * @param base the base to use when taking logarithms of the expressions.
   * @param offset the offset to add to expression levels. This can be used both as
   * a pseudocount in order to include zero-level expression comparisons and to prevent
   * significant M values at low levels of expression.
   * @return The calculated M value.
   */
  public static double calcM( double ref, double obs, int base, double offset )
  {
    return( Math.log( ( obs + offset ) / ( ref + offset ) ) / Math.log( base ));
  }
  
    public static double calcM(double ref, double obs, int base) {
        return (Math.log((obs) / (ref)) / Math.log(base));
    }
  
  /**
   * Calculate the A value (log-average of expression levels) of two expression levels.
   * @param ref the reference expression level.
   * @param obs the observed expression level.
   * @param base the base to use when taking logarithms of the expressions.
   * @return the calculated A value.
   */
  public static double calcA( double ref, double obs, int base, int offset)
  {
    return( ( Math.log( obs + offset ) / Math.log( base ) + Math.log( ref + offset ) / Math.log( base ) ) / 2);
  }
   public static double calcA( double ref, double obs, int base )
   {
       return(calcA(ref, obs, base, 0));
   }
  
  
  public static double calcM( double ref, double obs){return(calcM(ref,obs,MAelement.DEFAULT_BASE));}
  public static double calcA( double ref, double obs){return(calcM(ref,obs,MAelement.DEFAULT_BASE));}
  
  public static double calcWeighting( double numRef, double countRef, double numObs, double countObs )
  {
    return ( ( numObs - countObs ) / ( numObs * countObs ) ) + ( ( numRef - countRef ) / ( numRef * countRef ) );
  }
  
  public double getM() { return(getVal(MAparam.M)); }
  public double getA() { return(getVal(MAparam.A)); }
  
  /**
   * Dynamic getter for values used within this package
   * @param param
   * @return 
   */
  double getVal(MAparam param)
  {
    switch(param){
       case M: return this.M;
       case A: return this.A;
       default: throw new UnsupportedOperationException("Unsupported trim by " + param);
    } 
  }
  
  public double getWeighting() { return(this.weighting); }
  
  public boolean toKeep() { return(this.keep); }
  
  public void setKeep(boolean keep){ this.keep = keep; }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    DecimalFormat ab = new DecimalFormat("+0.00;-0.00");
    sb.append( ab.format( M ) ).append("M ")
      .append( ab.format( A ) ).append( "A " )
      .append( ab.format( weighting ) ).append("W ")
      .append( keep );
    return(sb.toString());
  }
  
}
