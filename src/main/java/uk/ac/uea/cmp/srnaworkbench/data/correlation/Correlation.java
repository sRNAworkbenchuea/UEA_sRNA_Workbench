/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author ezb11yfu
 */
public class Correlation
{
  private ExpressionData.ExpressionDataEntry exprEntry1;
  private ExpressionData.ExpressionDataEntry exprEntry2;
  private double distance;

  public Correlation( ExpressionData.ExpressionDataEntry exprEntry1, ExpressionData.ExpressionDataEntry exprEntry2, double distance )
  {
    this.exprEntry1 = exprEntry1;
    this.exprEntry2 = exprEntry2;
    this.distance = distance;
  }

  @Override
  public boolean equals( Object obj )
  {
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    final Correlation other = (Correlation) obj;
    if ( this.exprEntry1 != other.exprEntry1 && ( this.exprEntry1 == null || !this.exprEntry1.equals( other.exprEntry1 ) ) )
    {
      return false;
    }
    if ( this.exprEntry2 != other.exprEntry2 && ( this.exprEntry2 == null || !this.exprEntry2.equals( other.exprEntry2 ) ) )
    {
      return false;
    }
    if ( Double.doubleToLongBits( this.distance ) != Double.doubleToLongBits( other.distance ) )
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 83 * hash + ( this.exprEntry1 != null ? this.exprEntry1.hashCode() : 0 );
    hash = 83 * hash + ( this.exprEntry2 != null ? this.exprEntry2.hashCode() : 0 );
    hash = 83 * hash + (int) ( Double.doubleToLongBits( this.distance ) ^ ( Double.doubleToLongBits( this.distance ) >>> 32 ) );
    return hash;
  }

  public double getDistance()
  {
    return distance;
  }

  public ExpressionData.ExpressionDataEntry getExprEntry1()
  {
    return exprEntry1;
  }

  public ExpressionData.ExpressionDataEntry getExprEntry2()
  {
    return exprEntry2;
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( this.distance );
    
    return sb.toString();
  }

  public static class CorrelationList extends ArrayList<Correlation>
  {
    public void save( final File filename ) throws IOException
    {
      PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( filename ) ) );

      for ( Correlation c : this )
      {
        pw.print( c.toString() );
        pw.print( "\n" );
      }

      pw.flush();
      pw.close();
    }
    
    public double[] getDistances()
    {
      double[] distances = new double[this.size()];
      
      for( int i = 0; i < this.size(); i++ )
      {
        distances[i] = this.get( i ).getDistance();
      }
      
      return distances;
    }
    
  }
}
