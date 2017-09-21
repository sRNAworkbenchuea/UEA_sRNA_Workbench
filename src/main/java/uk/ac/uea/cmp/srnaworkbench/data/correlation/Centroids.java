/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.util.Arrays;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author ezb11yfu
 */
public class Centroids
{
  double[][] centroid;

  public Centroids()
  {
    this( 0, 0 );
  }

  public Centroids( final int width, final int height )
  {
    if ( width < 0 || height < 0 )
    {
      throw new IllegalArgumentException( "Must create a 2D matrix with a positive width and height" );
    }

    this.centroid = new double[height][width];

    for ( int i = 0; i < height; i++ )
    {
      Arrays.fill( centroid[i], 0 );
    }
  }

  public Centroids( final double[][] centroids )
  {
    this.centroid = new double[centroids.length][centroids[0].length];

    for ( int i = 0; i < centroids.length; i++ )
    {
      System.arraycopy( centroids[i], 0, this.centroid[i], 0, centroids.length );
    }
  }

  public Centroids( final Centroids copy )
  {
    if ( copy == null )
    {
      this.centroid = new double[0][0];
    }
    else
    {
      this.centroid = new double[copy.height()][copy.width()];

      for ( int i = 0; i < copy.height(); i++ )
      {
        System.arraycopy( copy.centroid[i], 0, this.centroid[i], 0, copy.width() );
      }
    }
  }

  public int width()
  {
    return this.centroid[0].length;
  }

  public int height()
  {
    return this.centroid.length;
  }

  public int nbElements()
  {
    return width() * height();
  }

  public double getEntry( final int row, final int column )
  {
    return this.centroid[row][column];
  }

  public void setEntry( final int row, final int column, final double val )
  {
    this.centroid[row][column] = val;
  }

  public void setRow( final int index, final double[] row )
  {
    System.arraycopy( row, 0, this.centroid[index], 0, row.length );
  }

  public double[] getRow( final int row )
  {
    return this.centroid[row];
  }

  public void increment( final int row, final int column, final double inc )
  {
    this.centroid[row][column] += inc;
  }

  public Centroids computeNewCentroids( final RealMatrix data, final int nbClusters )
  {
    // Calculate similarities per data row
    Similarity[] sims = calcSimilarities( data );
    
    // Create space for new centroids
    Centroids newCentroids = new Centroids( this.height(), this.width() );
    
    // Compute new centroids
    int[] count = new int[nbClusters];
    Arrays.fill( count, 0 );

    for ( int i = 0; i < data.getRowDimension(); i++ )
    {
      for ( int j = 0; j < data.getColumnDimension(); j++ )
      {
        newCentroids.increment( sims[i].getClusterListIndex(), j, data.getEntry( i, j ) );
      }

      count[sims[i].getClusterListIndex()]++;
    }

    newCentroids.weight( count );
    
    return newCentroids;
  }
  
  private void weight( final int[] count )
  {
    if ( count.length != height() )
    {
      throw new IllegalArgumentException( "counts length and height of centroids matrix must be the same." );
    }

    for ( int i = 0; i < height(); i++ )
    {
      if ( count[i] > 0 )
      {
        for ( int j = 0; j < width(); j++ )
        {
          this.centroid[i][j] /= count[i];
        }
      }
    }
  }
  
  
  private Similarity[] calcSimilarities( final RealMatrix data )
  {
    final int nbEntries = data.getRowDimension();
    
    Similarity[] sims = new Similarity[nbEntries];
    for ( int i = 0; i < nbEntries; i++ )
    {
      Similarity s = new Similarity();

      for ( int indexC = 0; indexC < this.height(); indexC++ )
      {
        double r = new PearsonsCorrelation().correlation(
          data.getRow( i ),
          this.getRow( indexC ) );

        if ( r > s.getDistance() )
        {
          s = new Similarity( indexC, r );
        }
      }

      sims[i] = s;
    }
    
    return sims;
  }
  
  
  public double calcChange( final int rowIdx, final double[] rowToCompare )
  {
    double chg = 0.0;
    for ( int j = 0; j <= this.width(); j++ )
    {
      chg += Math.scalb( getEntry( rowIdx, j ) - rowToCompare[j], 2 );
    }
    chg = Math.sqrt( chg );
    
    return chg;
  }
}
