/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

import org.apache.commons.io.IOUtils;
import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 * Class for file utilities
 *
 * @author prb07qmu
 */
public class FileUtils
{
  /***
   * Wrapper around {@link countLines} which does not throw an exception.
   * If an exception occurs -1 is returned.
   *
   * @param f The {@code File} object
   * @return The number of lines in the file as an integer
   */
  public static int countLinesWithoutException( File f )
  {
    int num = -1;

    try
    {
      num = countLines( f );
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      num = -1;
    }

    return num;
  }

  /***
   * Counts the number of lines in a file.
   *
   * @param f The {@code File} object
   * @return The number of lines in the file as an integer
   * @throws IOException
   */
  public static int countLines( File f ) throws IOException
  {
    int lineCount = 0;

    if ( f.length() < (long) Integer.MAX_VALUE )
    {
      // A ByteBuffer can be used on files up to 2 Gb in size - a serious limitation in java.nio (bug id: 6347833)
      lineCount = countLinesUsingByteBuffer( f );
    }
    else
    {
      lineCount = countLinesUsingInputStream( f );
    }

    return lineCount;
  }

  private static int countLinesUsingInputStream( File f ) throws IOException
  {
    BufferedInputStream is = new BufferedInputStream( new FileInputStream( f ) );

    byte[] c = new byte[1024];
    int count = 0;
    int readChars = 0;

    try
    {
      while ( ( readChars = is.read( c ) ) != -1 )
      {
        for ( int i = 0; i < readChars; ++i )
        {
          if ( c[i] == '\n' )
          {
            ++count;
          }
        }
      }
    }
    finally
    {
      IOUtils.closeQuietly( is );
      is = null;
    }

    return count;
  }

  private static int countLinesUsingByteBuffer( File f ) throws IOException
  {
    // MappedByteBuffer can be as much as 3x faster than the BufferedInputStream implementation.
    // However, there is a 2 Gb limit on the file size !

    FileChannel fc = createFileChannel( f );
    MappedByteBuffer bb = createReadOnlyMappedByteBuffer( fc, f );

    int lineCount = 0;

    try
    {
      while ( bb.hasRemaining() )
      {
        if ( '\n' == (char) bb.get() )
        {
          ++lineCount;
        }
      }
    }
    finally
    {
      fc.close();
      fc = null;
      bb = null;
    }

    return lineCount;
  }

  /***
   * Create a {@link BufferedReader} from a given file.
   *
   * @param f The {@link File} object
   * @return The {@link BufferedReader}
   */
  public static BufferedReader createBufferedReader( File f )
  {
    FileInputStream fis = null;

    try
    {
      fis = new FileInputStream( f );
    }
    catch ( FileNotFoundException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
      return null;
    }

    BufferedReader br = new BufferedReader( new InputStreamReader( fis ) );

    return br;
  }

  /***
   * Create a {@link FileChannel} object for a given file
   *
   * @param f The {@link File} object
   * @return The {@link FileChannel}
   */
  public static FileChannel createFileChannel( File f )
  {
    try
    {
      FileInputStream fis = new FileInputStream( f );
      return fis.getChannel();
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }

    return null;
  }

  /***
   * Create a {@code MappedByteBuffer} for the file and file-channel
   *
   * @param fc The {@link FileChannel}
   * @param f The {@link File}
   * @return The {@link MappedByteBuffer}
   */
  public static MappedByteBuffer createReadOnlyMappedByteBuffer( FileChannel fc, File f )
  {
    try
    {
      return fc.map( FileChannel.MapMode.READ_ONLY, 0, f.length() );
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }

    return null;
  }

  /***
   * Ensures a file has the given extension, and appends it if not.
   *
   * @param f          The file
   * @param extension  The required extension
   * @return           The file with extension
   */
  public static File addFileExtensionIfRequired( File f, FileNameExtensionFilter extension )
  {
    if ( f == null )
    {
      throw new IllegalArgumentException( "The file must be specified when ensuring the file extension." );
    }

    if ( extension == null || extension.getExtensions().length <= 0 )
    {
      return f;
    }

    String fileName = f.getPath();

    boolean found = false;
    for( String ext : extension.getExtensions() )
    {
      String dotPlusExt = "." + ext;

      if (fileName.toLowerCase().endsWith( dotPlusExt ))
      {
        return f;
      }
    }

    String preferedExt = "." + extension.getExtensions()[0].toLowerCase();
    fileName += preferedExt;
    return new File( fileName );
  }

  public static void compressFile( File file ) throws IOException
  {
    ZipOutputStream gzos = new ZipOutputStream( new FileOutputStream( file + ".zip" ) );

    BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );

    byte[] buffer = new byte[2156];
    int i;
    while ( ( i = in.read( buffer ) ) >= 0 )
    {
      gzos.write( buffer, 0, i );
    }

    in.close();
    gzos.close();
  }

  public static void compressFiles( File[] files, File out_file ) throws IOException
  {
    byte[] readBuffer = new byte[2156];
    int bytesIn = 0;
    ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( out_file ) );

    //loop through files, and zip the files
    for ( File file : files )
    {
      //if we reached here, the File object f was not a directory
      //create a FileInputStream on top of f
      FileInputStream fis = new FileInputStream( file );
      //create a new zip entry
      ZipEntry anEntry = new ZipEntry( file.getName() );
      //place the zip entry in the ZipOutputStream object
      zos.putNextEntry( anEntry );

      //now write the content of the file to the ZipOutputStream
      while ( ( bytesIn = fis.read( readBuffer ) ) != -1 )
      {
        zos.write( readBuffer, 0, bytesIn );
      }
      zos.flush();

      //Close the input stream
      fis.close();
    }

    zos.close();
  }

  public static void decompressFile( File infile, File outfile ) throws IOException
  {
    BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( outfile ) );

    BufferedInputStream in = new BufferedInputStream( new GZIPInputStream( new FileInputStream( infile ) ) );

    byte[] buffer = new byte[2156];
    int i;
    while ( ( i = in.read( buffer ) ) >= 0 )
    {
      out.write( buffer, 0, i );
    }

    in.close();
    out.close();
  }

  // Note, presently this flattens any directory structure in the zip file.  Can be enhanced.
  public static List<File> decompressFiles( File in_file, File output_dir ) throws IOException
  {
    byte[] readBuffer = new byte[2156];
    int bytesIn = 0;
    ZipInputStream zis = new ZipInputStream( new FileInputStream( in_file ) );
    List<File> out_files = new ArrayList<File>();

    //loop through files, and zip the files
    ZipEntry ze = null;
    while ( ( ze = zis.getNextEntry() ) != null )
    {
      String zefn = new File( ze.getName() ).getName();
      File f = new File( output_dir.getPath() + DIR_SEPARATOR + zefn );
      FileOutputStream fos = new FileOutputStream( f );

      while ( ( bytesIn = zis.read( readBuffer ) ) != -1 )
      {
        fos.write( readBuffer, 0, bytesIn );
      }
      fos.flush();
      fos.close();

      zis.closeEntry();
      out_files.add( f );
    }

    zis.close();

    return out_files;
  }

  public static void compressDirectory( File dir2zip ) throws IOException
  {
    ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( dir2zip + ".zip" ) );

    compressDirectory( dir2zip, zos );

    zos.flush();
    zos.close();
  }

  public static File removeSpaceFromFile( File file )
  {
    String[] tempPath = file.getAbsolutePath().split( "." );
    File returnME = new File( Tools.userDataDirectoryPath + DIR_SEPARATOR + file.getName() );
    try
    {
      BufferedReader inPatmanFile = null;
      try
      {
        inPatmanFile = new BufferedReader( new FileReader( file ) );
      }
      catch ( FileNotFoundException ex )
      {
        LOGGER.log( Level.SEVERE, null, ex );
      }

      String thisLine = "";
      PrintWriter out = new PrintWriter( new FileWriter( returnME ) );

      while ( ( thisLine = inPatmanFile.readLine() ) != null )
      {
        out.println( thisLine.trim() );
      }
      out.close();
    }
    catch ( IOException ex )
    {
      LOGGER.log( Level.SEVERE, null, ex );
    }

    return returnME;
  }

  private static void compressDirectory( File dir2zip, ZipOutputStream zos ) throws IOException
  {
    //get a listing of the directory content
    String[] dirList = dir2zip.list();
    byte[] readBuffer = new byte[2156];
    int bytesIn = 0;

    //loop through dirList, and zip the files
    for ( int i = 0; i < dirList.length; i++ )
    {
      File f = new File( dir2zip, dirList[i] );
      if ( f.isDirectory() )
      {
        //if the File object is a directory, call this
        //function again to add its content recursively
        compressDirectory( f, zos );
        //loop again
        continue;
      }
      //if we reached here, the File object f was not a directory
      //create a FileInputStream on top of f
      FileInputStream fis = new FileInputStream( f );
      //create a new zip entry
      ZipEntry anEntry = new ZipEntry( f.getPath() );
      //place the zip entry in the ZipOutputStream object
      zos.putNextEntry( anEntry );

      //now write the content of the file to the ZipOutputStream
      while ( ( bytesIn = fis.read( readBuffer ) ) != -1 )
      {
        zos.write( readBuffer, 0, bytesIn );
      }
      zos.flush();

      //Close the input stream
      fis.close();
    }
  }


  // Combines a number of input files into a single file
  // This could probably be implemented better
  public static void concatFiles( List<File> in_files, File output_file ) throws IOException
  {
    output_file.delete();
    output_file.createNewFile();
    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( output_file, true ) ) );
    for ( File f : in_files )
    {
      BufferedReader is = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );

      String line;
      while ( ( line = is.readLine() ) != null )
      {
        out.print( line + "\n" );
      }

      is.close();
    }
    out.close();
  }

  // Splits a large input file into a number of chunks
  // This could probably be implemented better
  public static List<File> splitupFile( File input, File output_dir, int chunkSize_lines ) throws IOException
  {
    ArrayList<File> split_files = new ArrayList<File>();

    FileInputStream fis = new FileInputStream( input );
    InputStreamReader isr = new InputStreamReader( fis );
    BufferedReader is = new BufferedReader( isr );

    boolean done = false;
    int i = 0;
    int j = 0;
    while ( !done )
    {
      File f = new File( output_dir.getPath() + DIR_SEPARATOR + input.getName() + "." + i++ + ".chunk" );

      FileWriter fw = new FileWriter( f );
      BufferedWriter bw = new BufferedWriter( fw );
      PrintWriter out = new PrintWriter( bw );

      String line;
      while ( ( line = is.readLine() ) != null )
      {
        // Don't change this line to os dependent line endings,
        // some 3rd party apps require linux style line-endings!
        out.print( line + "\n" );

        if ( j++ > chunkSize_lines )
        {
          j = 0;
          break;
        }
      }

      if ( line == null )
      {
        done = true;
      }

      out.flush();
      out.close();
      bw.close();
      fw.close();

      split_files.add( f );
    }

    is.close();
    isr.close();
    fis.close();

    return split_files;
  }

  public static Map<String, String> loadKeyValuePairs( File file ) throws IOException
  {
    BufferedReader br = null;
    HashMap<String, String> map = new HashMap<String, String>();

    br = new BufferedReader( new FileReader( file ) );

    String line = null;
    while ( ( line = br.readLine() ) != null )
    {
      line = line.trim();

      if ( line.isEmpty() || line.startsWith( "#" ) )
      {
        // This line's a comment or contains whitespace... goto next line
        continue;
      }

      String[] parts = line.split( "=" );

      if ( parts.length < 2 )
      {
        throw new IOException( "Syntax error in param file: \"" + line + "\"" );
      }

      String param = parts[0].trim();
      String value = parts[1].trim();

      map.put( param, value );
    }

    br.close();

    return map;
  }

  /**
   * Tests to see if the specified file contains unix style line endings or not
   * @param input_file File to test
   * @return true if the file has unix style line endings, otherwise false
   * @throws IOException Thrown if there were any problems reading the file
   */
  public static boolean hasUnixLineEndings( File input_file ) throws IOException
  {
    FileInputStream fis = new FileInputStream( input_file );
    InputStreamReader isr = new InputStreamReader( fis );

    int current = -1;
    int last = -1;
    while ( ( current = isr.read() ) != -1 && current != 10 )
    {
      last = current;
    }

    isr.close();
    fis.close();

    if ( last == 13 )
    {
      return false;
    }

    return true;
  }


  /**
   * Creates a new file duplicating the given file with all line endings replaced
   * with the given string
   * @param input_file File to duplicate
   * @param output_file Duplicated file with the specified line endings
   * @param lineEnding The particular line ending to use (Probably should use an enum here)
   * @throws IOException Thrown if there is any problem replacing the line endings
   */
  public static void replaceLineEndings( File input_file, File output_file, String lineEnding ) throws IOException
  {
    FileInputStream fis = new FileInputStream( input_file );
    InputStreamReader isr = new InputStreamReader( fis );
    BufferedReader is = new BufferedReader( isr );

    FileWriter fw = new FileWriter( output_file );
    BufferedWriter bw = new BufferedWriter( fw );
    PrintWriter out = new PrintWriter( bw );

    String line;
    while ( ( line = is.readLine() ) != null )
    {
      out.print( line + lineEnding );
    }

    out.flush();
    out.close();
    bw.close();
    fw.close();

    is.close();
    isr.close();
    fis.close();
  }
 
  
  
  /**
   * Gets MD5 sum from a small text file by reading all the data in via a buffered reader.  
   * The entire data is tested at once for it's MD5 sum, without any carridge returns
   * or line feeds present.
   * Will be sensitive to carridge returns and line endings in the file.
   * @param f The file to process
   * @return The MD5 sum of the file
   * @throws IOException Thrown if there were problems reading from the file.
   * @throws NoSuchAlgorithmException Thrown if couldn't access MD5 algorithm.
   */
  public static String getSmallFileMD5( File f ) throws IOException, NoSuchAlgorithmException
  {
    if ( f == null )
    {
      throw new NullPointerException();
    }

    if ( !f.exists() || !f.canRead() )
    {
      throw new IOException();
    }

    MessageDigest digest = MessageDigest.getInstance( "MD5" );

    BufferedReader br = new BufferedReader(new FileReader( f ));

    try
    {
      String line = null;

      List<String> data = new ArrayList<String>();
      
      while ( ( line = br.readLine() ) != null )
      {
        data.add( line );
      }
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(baos);
      for (String element : data) {
          out.writeUTF(element);
      }
      byte[] bytes = baos.toByteArray();
      
      byte[] md5sum = digest.digest(bytes);
      BigInteger bigInt = new BigInteger( 1, md5sum );
      String output = bigInt.toString( 16 );
      return output;
    }
    finally
    {
      try
      {
        br.close();
      }
      catch ( IOException e )
      {
        throw new RuntimeException( "Unable to close input stream for MD5 calculation", e );
      }
    }
  }
  
  /**
   * Gets MD5 sum from a big file by reading the byte data in with an 8K buffer.  
   * Will be sensitive to carridge returns and line endings in the file.
   * @param f The file to process
   * @return The MD5 sum of the file
   * @throws IOException Thrown if there were problems reading from the file.
   * @throws NoSuchAlgorithmException Thrown if couldn't access MD5 algorithm.
   */
  public static String getMD5( File f ) throws IOException, NoSuchAlgorithmException
  {
    if ( f == null )
    {
      throw new NullPointerException();
    }

    if ( !f.exists() || !f.canRead() )
    {
      throw new IOException();
    }

    MessageDigest digest = MessageDigest.getInstance( "MD5" );

    InputStream is = new FileInputStream( f );

    try
    {
      byte[] buffer = new byte[8192];
      int read = 0;

      while ( ( read = is.read( buffer ) ) > 0 )
      {
        digest.update( buffer, 0, read );
      }
      byte[] md5sum = digest.digest();
      BigInteger bigInt = new BigInteger( 1, md5sum );
      String output = bigInt.toString( 16 );
      return output;
    }
    finally
    {
      try
      {
        is.close();
      }
      catch ( IOException e )
      {
        throw new RuntimeException( "Unable to close input stream for MD5 calculation", e );
      }
    }
  }

  public static void main( String[] args ) throws IOException
  {
    //testCountLines();
    testFile( new File( "C:\\windows" ) );
    testFile( new File( "D:\\LocalData\\hugh\\temp\\params.txt" ) );
  }

  private static void testCountLines()
  {
    StopWatch sw = new StopWatch( "FileUtils" );

    ThreadUtils.safeSleep( 2000 );

    File f0 = new File( "C:/LocalData/hugh/seq_data/plant/Ath_TAIR9.fa" );      // BufferedInputStream > 1.1s, MappedByteBuffer ~ 0.3
    File f1 = new File( "C:/LocalData/hugh/seq_data/plant/Ath_TAIR9.Chr1.fa" ); // BufferedInputStream > 0.3s, MappedByteBuffer ~ 0.1

    sw.start();

    countLinesWithoutException( f0 );
    sw.lap();

    countLinesWithoutException( f1 );
    sw.lap();

    countLinesWithoutException( f0 );
    sw.lap();

    countLinesWithoutException( f1 );
    sw.lap();

    sw.stop();
    sw.printTimes();
  }

  private static void testFile( File f1 )
  {
    System.out.println( "================== File info ==================" );
    System.out.println( "File Name     : " + f1.getName() );
    System.out.println( "Path          : " + f1.getPath() );
    System.out.println( "Abs. Path     : " + f1.getAbsolutePath() );
    if ( f1.exists() )
    {
      try
      {
        System.out.println( "Can. Path     : " + f1.getCanonicalPath() );
      }
      catch ( IOException ex )
      {
        System.err.println( ex );
      }
    }
    System.out.println( "Parent        : " + f1.getParent() );
    System.out.println( "ParentFile    : " + f1.getParentFile() );
    System.out.println( "Exists        : " + ( f1.exists() ? "yes" : "no" ) );
    System.out.println( "Writeable     : " + ( f1.canWrite() ? "yes" : "no" ) );
    System.out.println( "Readable      : " + ( f1.canRead() ? "yes" : "no" ) );
    System.out.println( "Directory     : " + f1.isDirectory() );
    System.out.println( "Normal file ? : " + f1.isFile() );
    System.out.println( "Absolute ?    : " + f1.isAbsolute() );
    System.out.println( "Last modified : " + f1.lastModified() );
    System.out.println( "File size     : " + f1.length() + " bytes" );
  }
}
