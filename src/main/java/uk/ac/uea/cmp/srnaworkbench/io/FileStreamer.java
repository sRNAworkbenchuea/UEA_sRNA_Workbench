/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.io;

import com.google.common.io.Closeables;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import uk.ac.uea.cmp.srnaworkbench.io.decoders.Decoder;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 * @param <T>
 */
public class FileStreamer<T> implements Iterable<List<T>>
{
  //private static final int CHUNK_SIZE = 8;//2^3 (one UTF-8 encoded character)
  //private static final int CHUNK_SIZE = 64;//2^3 (one UTF-8 encoded character)
  //private static final int CHUNK_SIZE = 512;//2^9 bytes
  //private static final int CHUNK_SIZE = 1024;//2^10 bytes
  //private static final int CHUNK_SIZE = 4096;//2^12
  //private static final int CHUNK_SIZE = 32768;//2^15
  //private static final int CHUNK_SIZE = 1048576;//1MB 2^20
  //private static final int CHUNK_SIZE = 4194304;//4MB 2^22
  //private static final int CHUNK_SIZE = 10485760; //10MB
  private static final int CHUNK_SIZE = 20971520; //20MB
  //private static final int CHUNK_SIZE = 104857600;//100MB 
  //private static final int CHUNK_SIZE = 524288000;//500MB
  private final Decoder<T> decoder;
  private Iterator<File> files;
  private String openMode;
  private FileChannel.MapMode mode;
  
  private MappedByteBuffer buffer;
  

  private FileStreamer( Decoder<T> decoder, String openMode, File... files )
  {
    this( decoder, openMode, Arrays.asList( files ) );
    
  }

  private FileStreamer( Decoder<T> decoder, String openMode, List<File> files )
  {
    this.files = files.iterator();
    this.decoder = decoder;
    this.openMode = openMode;
    if(openMode.equals( "r"))
      mode = FileChannel.MapMode.READ_ONLY;
    else
      mode = FileChannel.MapMode.READ_WRITE;
  }

  public static <T> FileStreamer<T> create( Decoder<T> decoder, String openMode, List<File> files )
  {
    return new FileStreamer<T>( decoder, openMode, files );
  }

  public static <T> FileStreamer<T> create( Decoder<T> decoder, String openMode, File... files )
  {
    return new FileStreamer<T>( decoder, openMode, files );
  }

  public static int getChunkSize()
  {
    return CHUNK_SIZE;
  }
  public MappedByteBuffer getBuffer()
  {
    return this.buffer;
  }
  @Override
  public Iterator<List<T>> iterator()
  {
    return new Iterator<List<T>>()
    {
      private List<T> entries;
      private long chunkPos = 0;
      
      private FileChannel channel;
      

      @Override
      public boolean hasNext()
      {
        if ( buffer == null || !buffer.hasRemaining() )
        {
          buffer = nextBuffer( chunkPos );
          if ( buffer == null )
          {
            return false;
          }
        }
        T result = null;
        while ( ( result = decoder.decode( buffer ) ) != null )
        {
          if ( entries == null )
          {
            entries = new ArrayList<T>();
          }
          entries.add( result );
        }
        // set next MappedByteBuffer chunk
        chunkPos += buffer.position();
        buffer = null;
        if ( entries != null )
        {
          return true;
        }
        else
        {
            try
            {
                channel.close();
            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
          return false;
        }
      }

      private MappedByteBuffer nextBuffer( long position )
      {
        try
        {
          if ( channel == null || channel.size() == position )
          {
            if ( channel != null )
            {
              channel.close();
              channel = null;
            }
            if ( files.hasNext() )
            {
              File file = files.next();
              channel = new RandomAccessFile( file, openMode ).getChannel();
              chunkPos = 0;
              position = 0;
            }
            else
            {
              return null;
            }
          }
          long chunkSize = CHUNK_SIZE;
          if ( channel.size() - position < chunkSize )
          {
            chunkSize = channel.size() - position;
          }
          return channel.map(mode , chunkPos, chunkSize );
        }
        catch ( IOException e )
        {
            try
            {
                channel.close();
            }
            catch (IOException ex)
            {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
          
          throw new RuntimeException( e );
        }
      }

      @Override
      public List<T> next()
      {
        
        List<T> res = entries;
        entries = null;
        return res;
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
      
    };
  }

}
