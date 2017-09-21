/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.correlation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import uk.ac.uea.cmp.srnaworkbench.utils.FileUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class ExpressionData
{
  private final RealMatrix data;
  private final String[] columnHeaders;
  private final String[] rowHeaders;
  private int[] abundanceLevels;
  private ExpressionLevels expressionLevels;

  /**
   * Creates a new ExpressionData object with a data matrix containing
   * expression levels for each entry (sRNA/loci/gene) from a set of samples.
   * Initialises the expression levels to 1.0 and assumes client will calculate
   * these later, or it otherwise not interested in them.
   *
   * @param data The data matrix
   * @param rowHeaders A description of each entry in the matrix
   * @param columnHeaders A description of each sample in the matrix
   */
  public ExpressionData( final RealMatrix data, final String[] rowHeaders, final String[] columnHeaders )
  {
    // Validation
    if ( data == null || rowHeaders == null || columnHeaders == null )
    {
      throw new NullPointerException( "Input arguments must be non-null" );
    }

    if ( data.getColumnDimension() != columnHeaders.length )
    {
      throw new IllegalArgumentException( "columns in data matrix and column header list must be the same size" );
    }

    if ( data.getRowDimension() != rowHeaders.length )
    {
      throw new IllegalArgumentException( "rows in data matrix, row header list and expression levels array must be the same size" );
    }

    if ( data.getColumnDimension() < 2 || data.getRowDimension() == 0 )
    {
      throw new IllegalArgumentException( "Must provide at least 2 columns and 1 row to work with" );
    }

    this.data = data;
    this.columnHeaders = columnHeaders;
    this.rowHeaders = rowHeaders;

    // Initialise abundance to 1 for all entries
    this.abundanceLevels = new int[this.rowHeaders.length];
    Arrays.fill( abundanceLevels, 1 );
    
    // Intialise expression levels to 1.0;
    double[] els = new double[this.rowHeaders.length];
    Arrays.fill( els, 1.0 );    
    this.expressionLevels = new ExpressionLevels( els );    
  }

  /**
   * Retrieves the column headers describing the samples
   *
   * @return The column headers as a String[]
   */
  public String[] getColumnHeaders()
  {
    return columnHeaders;
  }

  /**
   * Retrieves the data matrix
   *
   * @return The data matrix
   */
  public RealMatrix getData()
  {
    return data;
  }

  /**
   * Retrieves the row headers describing the elements (sRNAs/loci/genes)
   *
   * @return The row headers as a String[]
   */
  public String[] getRowHeaders()
  {
    return rowHeaders;
  }

  /**
   * Retrieves the expression levels associated with each element across all
   * samples in the matrix.
   *
   * @return The expression level of each element in the matrix.
   */
  public ExpressionLevels getExpressionLevels()
  {
    return expressionLevels;
  }
  
  /**
   * Retrieves the abundance array for all elements in the matrix
   * @return The abundance array.
   */
  public int[] getAbundanceLevels()
  {
    return abundanceLevels;
  }
  
  /**
   * Constructs an ExpressionDataEntry object which describes a single row of this
   * data structure.  Contains: row name, abundance, expression levels per sample, 
   * and overall expression level.
   * @return A row in the ExpressionData object.
   */
  public ExpressionDataEntry getRow( int index )
  {
    return new ExpressionDataEntry( this.rowHeaders[index], this.abundanceLevels[index],
      this.data.getRow( index ), this.expressionLevels.get( index ) );
  }

  /**
   * Retrieves the number of rows in this ExpressionData object.
   *
   * @return The number of rows (sRNAs/loci/genes).
   */
  public int rows()
  {
    return this.rowHeaders.length;
  }

  /**
   * Retrieves the number columns in the ExpressionData object.
   *
   * @return The number of columns (samples).
   */
  public int columns()
  {
    return this.columnHeaders.length;
  }

  /**
   * Sets the expression levels associated with each element across all samples
   * of the matrix. The expression levels array must contain the same number of
   * elements as there are rows in the matrix.   *
   * @param expressionLevels A double[] containing expression levels for each
   * element in the matrix
   */
  public void setExpressionLevels( ExpressionLevels expressionLevels )
  {
    if ( expressionLevels.size() != this.rowHeaders.length )
    {
      throw new IllegalArgumentException( "expressionLevels array is not the same length as the data in this object" );
    }

    this.expressionLevels = expressionLevels;
  }
  
  /**
   * Sets the abundance levels associated with each element across all samples
   * of the matrix. The abundance levels array must contain the same number of
   * elements as there are rows in the matrix.
   * @param abundanceLevels A int[] containing abundance levels for each element
   * in the matrix.
   */
  public void setAbundanceLevels( int[] abundanceLevels )
  {
    if ( abundanceLevels.length != this.rowHeaders.length )
    {
      throw new IllegalArgumentException( "abundanceLevels array is not the same length as the data in this object" );
    }
    
    this.abundanceLevels = abundanceLevels;
  }

  /**
   * Loads an ExpressionData object from a CSV file. Assumes the Expression
   * Level data is not there.
   *
   * @param f The file to load
   * @return An ExpressionData object describing the contents of the file.
   * @throws IOException Thrown if there were any problems loading from the
   * file.
   */
  public static ExpressionData load( File f ) throws IOException
  {
    // File validation
    if ( f == null || !f.exists() || !f.canRead() )
    {
      throw new IllegalArgumentException( "expression file must exist and be readable." );
    }

    String[] columnHeaders = null;
    List<String> rowHeaderList = new ArrayList<String>();
    List<double[]> data = new ArrayList<double[]>();

    BufferedReader br = FileUtils.createBufferedReader( f );

    String line;
    int row_size = -1;
    boolean firstRow = true;
    while ( ( line = br.readLine() ) != null )
    {
      String[] elements = line.trim().split( "," );
      double[] des = new double[elements.length - 1];

      if ( row_size != -1 && row_size != elements.length - 1 )
      {
        throw new IllegalStateException( "Row sizes are different!" );
      }

      if ( firstRow )
      {
        row_size = elements.length - 1;

        columnHeaders = new String[row_size];
        for ( int i = 0; i < row_size; i++ )
        {
          columnHeaders[i] = elements[i + 1].trim();
        }

        firstRow = false;
      }
      else
      {
        int i = 0;
        for ( String e : elements )
        {
          String trimmed = e.trim();

          if ( i == 0 )
          {
            rowHeaderList.add( trimmed );
          }
          else
          {
            double d = Double.parseDouble( trimmed );

            des[i - 1] = d;
          }

          i++;
        }

        data.add( des );
      }
    }

    // Probably not optimal in terms of memory, but we'll work with it for now.
    double[][] matrix = new double[data.size()][row_size];

    int i = 0;
    for ( double[] d : data )
    {
      matrix[i++] = d;
    }

    RealMatrix data_matrix = new Array2DRowRealMatrix( matrix );
    String[] rowHeaders = (String[]) rowHeaderList.toArray( new String[0] );

    return new ExpressionData( data_matrix, rowHeaders, columnHeaders );
  }

  /**
   * Creates a new ExpressionData object which is a subset of the provided
   * ExpressionData object, which has had entries with insignificant expression
   * removed.
   *
   * @param ed The original ExpressionData object
   * @param cutOff The cutOff Level below which we will not consider entries.
   * @return The new filtered ExpressionData object.
   */
  protected ExpressionData cull( final boolean[] keep )
  {
    int new_size = 0;
    for ( boolean b : keep )
    {
      if ( b )
      {
        new_size++;
      }
    }

    RealMatrix data_matrix = new Array2DRowRealMatrix( new_size, this.columns() );
    String[] col_headers = this.getColumnHeaders();
    String[] row_headers = new String[new_size];
    int[] abd_lvls = new int[new_size];
    double[] exp_lvls = new double[new_size];

    for ( int i = 0, j = 0; i < this.rows(); i++ )
    {
      if ( keep[i] )
      {
        data_matrix.setRow( j, this.getData().getRow( i ) );
        row_headers[j] = this.getRowHeaders()[i];
        abd_lvls[j] = this.getAbundanceLevels()[i];
        exp_lvls[j] = this.getExpressionLevels().get( i );

        j++;
      }
    }

    ExpressionData new_ed = new ExpressionData( data_matrix, row_headers, col_headers );
    new_ed.setExpressionLevels( new ExpressionLevels( exp_lvls ) );

    return new_ed;
  }

  /**
   * Creates a new ExpressionData object containing only the top "n" percent of
   * entries in this object as determined by expression levels.
   * @param n The percentage of entries to keep.
   * @return A new ExpressionData object containing the top "n" percent of entries.
   */
  protected boolean[] keepTopNPercent( final boolean[] keep, final int n )
  {
    // Calculate the actual number of rows to use from the differential expression percentage 
    // threshold provided.
    int count_threshold = (int) ( (double) this.rows() * (double) n / 100.0 );
    LOGGER.log( Level.FINE, "de_threshold = {0}; count_threshold = {1}", new Object[]
      {
        n, count_threshold
      } );

    // Calculate the cutoff value based on the 
    double de_cutoff = calcCutoff( this.getExpressionLevels(), count_threshold );
    
    for ( int i = 0; i < this.rows(); i++ )
    {
      double e = this.expressionLevels.get( i );
      keep[i] = !keep[i] ? false : e >= de_cutoff ? true : false;
    }
    
    return keep;
  }
  
  protected boolean[] keepAbundant( final boolean[] keep, final int minAbundance)
  {
    for ( int i = 0; i < this.rows(); i++ )
    {
      double e = this.abundanceLevels[i];
      keep[i] = !keep[i] ? false : e >= minAbundance ? true : false;
    }
    
    return keep;
  }
  
  protected boolean[] keepExpressionLevel( final boolean[] keep, final double minExpLvl )
  {
    for ( int i = 0; i < this.rows(); i++ )
    {
      double e = this.expressionLevels.get( i );
      keep[i] = !keep[i] ? false : e >= minExpLvl ? true : false;
    }
    
    return keep;
  }
  
  /**
   * Filter out unwanted ExpressionDataEntries based on some filtering criteria.  Creates
   * a new ExpressionData object containing only those entries of interest.
   * @param topNPercent Only keep top N percent of entries based on their overall 
   * expression level.
   * @param minExpLvl Exclude entries with expression level less than this figure
   * @param minAbundance Exclude entries with abundance less than this figure
   * @return New ExpressionData object containing only the entries of interest.
   */
  public ExpressionData filter( final int topNPercent, final double minExpLvl, final int minAbundance )
  {
    if (topNPercent > 100 || topNPercent < 1)
      throw new IllegalArgumentException("topNPercent must be between 1 and 100");
    
    boolean top_n_filtering = topNPercent < 100 ? true : false;
    boolean abundance_filtering = minAbundance < 2 ? false : true;
    
    // Assume we want to keep everything away to begin with.
    boolean[] keep = new boolean[this.rows()];
    Arrays.fill( keep, true );
    
    if (top_n_filtering)
    {
      keep = keepTopNPercent(keep, topNPercent);
    }
    
    if (abundance_filtering)
    {
      keep = keepAbundant(keep, minAbundance);
    }
    
    keep = keepExpressionLevel(keep, minExpLvl);
    
    return cull(keep);
  }
  
  /**
   * Provides a cutoff value for OFC, above which we are willing to consider the
   * differentially expressed elements.
   * @param fc The offset fold change between each sample in the time series.
   * @param countThreshold The index of the element that contains the value
   * above which we are willing to consider.
   * @return An array containing a list of cutoff values for each column.
   */
  protected static double calcCutoff( final ExpressionLevels fc, final int countThreshold )
  {
    ExpressionLevels copy = new ExpressionLevels(fc);
    
    copy.sort();

    int idx = copy.size() - countThreshold - 1;
    idx = idx < 0 ? 0 : idx;
    double topThr = copy.get( idx );

    return topThr;
  }
  
  
  public static class ExpressionDataEntry
  {
    private String name;
    private double[] data;
    private int abundance;
    private double expressionLevel;

    public ExpressionDataEntry( String name, int abundance, double[] data, double expressionLevel )
    {
      this.name = name;
      this.data = data;
      this.abundance = abundance;
      this.expressionLevel = expressionLevel;
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
      final ExpressionDataEntry other = (ExpressionDataEntry) obj;
      if ( ( this.name == null ) ? ( other.name != null ) : !this.name.equals( other.name ) )
      {
        return false;
      }
      if ( !Arrays.equals( this.data, other.data ) )
      {
        return false;
      }
      if ( this.abundance != other.abundance )
      {
        return false;
      }
      if ( Double.doubleToLongBits( this.expressionLevel ) != Double.doubleToLongBits( other.expressionLevel ) )
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 79 * hash + ( this.name != null ? this.name.hashCode() : 0 );
      hash = 79 * hash + Arrays.hashCode( this.data );
      hash = 79 * hash + this.abundance;
      hash = 79 * hash + (int) ( Double.doubleToLongBits( this.expressionLevel ) ^ ( Double.doubleToLongBits( this.expressionLevel ) >>> 32 ) );
      return hash;
    }

    public int getAbundance()
    {
      return abundance;
    }

    public double[] getData()
    {
      return data;
    }

    public double getExpressionLevel()
    {
      return expressionLevel;
    }

    public String getName()
    {
      return name;
    }
  }
}
