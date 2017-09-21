/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.ec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ezb11yfu
 */
public class RToCorrelationListConvertor
{
  private File inFile;
  private File outFile;

  public RToCorrelationListConvertor( File inFile, File outFile )
  {
    this.inFile = inFile;
    this.outFile = outFile;
  }
  
  public double[] process() throws IOException
  {
    List<String> lines = FileUtils.readLines( inFile );
    List<String> linesOut = new ArrayList<String>();
    
    int j = 0;
    for( String line : lines )
    {
      String[] parts = line.trim().split( " " );
      
      for( int i = 1; i < parts.length; i++ )
      {
        if (!parts[i].isEmpty())
        {
          double cc = Double.parseDouble( parts[i] );
          
          // Convert correlation coefficient to a distance
          double distance = 1.0 - cc;
          
          linesOut.add( Double.toString( distance ) );
        }
      }
    }
    
    FileUtils.writeLines( outFile, linesOut );
    
    double[] distances = new double[linesOut.size()];
    
    for( int i = 0; i < linesOut.size(); i++ )
    {
      distances[i] = Double.parseDouble( linesOut.get( i ) );
    }
    
    return distances;
  }
  
  
  
}
