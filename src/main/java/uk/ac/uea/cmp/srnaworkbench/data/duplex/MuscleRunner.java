/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.data.duplex;

import java.io.*;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.stream.ProcessStreamManager;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

/**
 *
 * @author ezb11yfu
 */
public final class MuscleRunner
{
  private String seqs;
  private String uid;
  private BinaryExecutor be;
  private File temp_dir;
  private String res1;
  private String res2;

  public MuscleRunner( String s1, String s2, String uid, BinaryExecutor be, File temp_dir ) throws IOException
  {
    if ( be == null )
    {
      throw new IllegalArgumentException( "The BinaryExecutor must be specified" );
    }

    // It's necessary to not only reverse but also complement this string, because
    // muscle is an alignment tool and expects to match only on the sense strand.
    String s2rc = SequenceUtils.RNA.reverseComplement( s2 );

    this.seqs = ">" + s1 + "\n" + s1 + "\n>" + s2rc + "\n" + s2rc;
    this.temp_dir = temp_dir;
    this.uid = uid;
    this.be = be;

    this.res1 = "";
    this.res2 = "";
  }

  private void runDirect( String args ) throws Exception
  {
    Process p = Runtime.getRuntime().exec( "muscle " + args );

    ProcessStreamManager pm = new ProcessStreamManager( p, "MUSCLE" , true);
    pm.runInForeground( false );
  }

  // For the time being will assume RNACoFold are both on the path.
  public void run() throws Exception
  {
    // Run rnacofold giving the two sequences as a file.
    File temp_file = new File( temp_dir.getPath() + DIR_SEPARATOR + "muscle" + this.uid + ".seqs" );
    File align_file = new File( temp_dir.getPath() + DIR_SEPARATOR + "muscle" + this.uid + ".seqs.align" );

    String args = "-quiet -in " + temp_file.getPath() + " -out " + align_file.getPath() + " -gapopen -3 -gapextend -1 -center 0.0";

    BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(
      new FileOutputStream( temp_file ) ) );

    bw.write( this.seqs + "\n" );
    bw.flush();
    bw.close();

    be.execMuscle( args );
    //runDirect(cmdline);


    BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream( align_file ) ) );

    String l1 = br.readLine();
    this.res1 = br.readLine();
    String l3 = br.readLine();
    this.res2 = br.readLine();
    br.close();
  }

  public String getResult1()
  {
    return this.res1;
  }

  public String getResult2()
  {
    return this.res2;
  }
}
