/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.*;
import uk.ac.uea.cmp.srnaworkbench.utils.*;

import java.io.*;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author prb07qmu
 */
public final class ParesnipIO
{
  // Avoid instantiation
  private ParesnipIO() {}


  /**
   * Read in the contents of PARESnip output.
   *
   * @param f The file containing serialised {@link PlotRecord}s. The file is <b>assumed</b> to exist.
   * @return A map linking a {@link Category} to a list of {@link PlotRecord}s.
   *
   * @throws IOException
   */
  public static PlotRecordCollection readIn( File f ) throws IOException
  {
    // Set up the container for the paresnip results
    //
    PlotRecordCollection result = new PlotRecordCollection();

    BufferedReader reader = null;

    try
    {
      reader = FileUtils.createBufferedReader( f );

      int numRecords = 0;

      while ( reader.ready() )
      {
        PlotRecord pr = PlotRecord.inputPlotRecord( reader );

        Category c = pr.getMaxCategory();

        if ( c == Category.UNDEFINED )
        {
          System.out.println( "An unrecognised category was provided." );
        }
        else
        {
          // Add to the appropriate categorised collection
          result.add( pr );

          ++numRecords;
        }

        if ( numRecords % 10000 == 0 )
        {
          // For large files the memory usage shoots up - try and help !
          System.gc();
        }
      }
    }
    finally
    {
      IOUtils.closeQuietly( reader );
    }

    result.createIndex();

    System.gc();

    return result;
  }
}
