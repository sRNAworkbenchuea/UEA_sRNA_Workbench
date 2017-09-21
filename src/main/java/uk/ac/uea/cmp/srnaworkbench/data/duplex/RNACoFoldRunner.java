/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.duplex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.stream.ProcessStreamManager;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author ezb11yfu
 */
public final class RNACoFoldRunner
{
  private String seqs;
  private String s1;
  private String s2;
  private String uid;
  private File temp_dir;
  private BinaryExecutor be;

  static
  {
    if ( Tools.isLinux() )
    {
      String cofoldlib_name = "libcofolder" + Tools.getLibExt();

      String cofoldlib_path = Tools.getExeDir() + DIR_SEPARATOR + cofoldlib_name;

      System.load( cofoldlib_path );
    }
  }

  public RNACoFoldRunner( String s1, String s2 )
  {
    this.s1 = s1;
    this.s2 = s2;
  }

  public RNACoFoldRunner( String s1, String s2, String uid, BinaryExecutor be, File temp_dir ) throws IOException
  {
    if ( be == null )
    {
      throw new IllegalArgumentException( "The BinaryExecutor must be specified" );
    }

    this.seqs = s1 + "&" + s2;
    this.uid = uid;
    this.be = be;
    this.temp_dir = temp_dir;
  }

  private List<String> runDirect( List<String> data ) throws Exception
  {
    Process p = Runtime.getRuntime().exec( "RNAcofold" );
    ProcessStreamManager pm = new ProcessStreamManager( p, "RNACOFOLD" , true);
    pm.enterData( data, "@" );
    pm.runInForeground( true );
    return pm.getStandardOutput();
  }

  // Uses JNI to call out to libRNA to do the cofolding.
  native static float external_Get_MFE( String co_seq, int cut_point );

  // It's critical that this is static and syncronized because cofold, requires
  // a global variable "cut_point" to be set before running.  This makes the function
  // intrinsically NOT thread safe, hence we shouldn't call it multiple times at once
  // from different threads.
  public static synchronized float getMFE( String s1, String s2 ) throws Exception
  {
    if ( !Tools.isLinux() )
    {
      throw new Exception( "RNACoFoldRunner Error: Can only use this method on a unix platform!" );
    }

    return external_Get_MFE( s1 + s2, s1.length() );
  }

  // For the time being will assume RNACoFold are both on the path.
  public RNACoFoldOutput run() throws Exception
  {
    List<String> data = new ArrayList<String>();
    data.add( this.seqs );

    String feedback = "";
    if ( Tools.isLinux() )
    {
      return new RNACoFoldOutput( "", getMFE( this.s1, this.s2 ) );
    }
    else
    {
      return new RNACoFoldOutput( be.execRNACoFold( data ) );
    }
    //List<String> feedback = runDirect(data);

    // 2 lines of output are expected... the first contains the sequence
    // the second contains the dot bracket notation and the MFE.
//        if (feedback.size() < 2)
//        {
//            throw new IOException("RNACOFOLD error: Didn't get expected results.");
//        }
  }

  public static void main( String[] args )
  {
    try
    {
      RNACoFoldRunner proc = new RNACoFoldRunner( "CGCGCGCGCGCGCGCG", "AUAUAUAUAUAUAUA", "0", null, new File( "/scratch/dan/tests/nogenome/temp" ) );
      RNACoFoldOutput out = proc.run();
      System.out.println( out.getDotBrackets() );
      System.out.println( out.getMFE() );
    }
    catch ( Exception e )
    {
      System.err.println( e.getMessage() );
      e.printStackTrace();
    }
  }
}
