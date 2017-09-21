/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.filter;

/**
 * Describes the results from the filter tool at a specific stage in the pipeline.
 * @author Dan Mapleson
 */
public class FilterStage
{
  private final String long_stage_name;
  private final String short_stage_name;
  private int total_read_count;
  private int distinct_read_count;

  /**
   * Creates an empty FilterStage
   */
  public FilterStage()
  {
    this( "", -1, -1 );
  }

  /**
   * Creates a new FilterStage with the provided parameters.
   * @param stage_name The name of this FilterStage
   * @param total_read_count The total number of reads in this FilterStage
   * @param distinct_read_count The number of distinct reads in this FilterStage
   */
  public FilterStage( String stage_name, int total_read_count, int distinct_read_count )
  {
    this( stage_name, "", total_read_count, distinct_read_count );
  }
  
  /**
   * Creates a new FilterStage with the provided parameters.
   * @param stage_name The name of this FilterStage
   * @param total_read_count The total number of reads in this FilterStage
   * @param distinct_read_count The number of distinct reads in this FilterStage
   */
  public FilterStage( String long_stage_name, String short_stage_name, int total_read_count, int distinct_read_count )
  {
    this.long_stage_name = long_stage_name;
    this.short_stage_name = short_stage_name;
    this.total_read_count = total_read_count;
    this.distinct_read_count = distinct_read_count;
  }

  
  public String getStageName()        {return this.long_stage_name;}
  public String getShortStageName()   {return this.short_stage_name;}
  public int getTotalReadCount()      {return this.total_read_count;}
  public int getDistinctReadCount()   {return this.distinct_read_count;}

  public void setTotalReadCount( int total_read_count )       {this.total_read_count = total_read_count;}
  public void setDistinctReadCount( int distinct_read_count ) {this.distinct_read_count = distinct_read_count;}
  
  /**
   * Increments the total read count by the amount specified, and returns the new
   * total read count.
   * @param increment The amount to increment the total read count by
   * @return The new total read count after increment has been applied
   */
  public int incTotalReadCount( int increment )
  {
    this.total_read_count += increment;
    return this.total_read_count;
  }

  /**
   * Increments the distinct read count by one and returns the new distinct read
   * count.
   * @return The new distinct read count after increment has been applied.
   */
  public int incDistinctReadCount()
  {
    this.distinct_read_count++;
    return this.distinct_read_count;
  }

  /**
   * This method increments the total read count by "increment" and increments 
   * the distinct read count by 1.
   * @param increment The amount to increment the total count by.
   */
  public void incCounters( int increment )
  {
    incTotalReadCount( increment );
    incDistinctReadCount();
  }

  /**
   * A representation of this FilterStage that's suitable for output to CSV file
   * @return String representation of this FilterStage suitable for output to CSV file
   */
  @Override
  public String toString()
  {
    final String QUOTE = "\"";
    final String COMMA = ",";

    StringBuilder sb = new StringBuilder();

    sb.append( QUOTE ).append( this.getStageName() ).append( QUOTE );
    sb.append( COMMA ).append( this.getTotalReadCount() );
    sb.append( COMMA ).append( this.getDistinctReadCount() );

    return sb.toString();
  }
}
