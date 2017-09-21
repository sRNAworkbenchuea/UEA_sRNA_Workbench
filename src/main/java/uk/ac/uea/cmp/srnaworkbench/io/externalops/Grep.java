

package uk.ac.uea.cmp.srnaworkbench.io.externalops;


import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;


import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */

public class Grep {

    public static void main(String[] args) throws IOException {

        Path path = Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/AtH/TAIR10_chr_all.fas_FASTA_MAP.pds");

        int[] results = new int[1];
        String report = Grep.grep("TTTTGTGGGTTAGTTTTTCAAATT", path, results, "patman");
        
        report = Grep.grep("Nope", path, results, "patman");

        System.out.println(report);

    }

    private static final int MAPSIZE = 4 * 1024 ; // 4K - make this * 1024 to 4MB in a real system.

    public static boolean search(String toSearch, Path path, String fileType)
    {
      
      boolean found = false;
      try
      {
        int[] results = new int[1];
        grep(toSearch, path, results, fileType);
        if(results[0] > 0)
        {
          found = true;
        }
      }
      catch ( IOException ex )
      {
        LOGGER.log(Level.SEVERE, "Error during grep: {0}", ex);
      }
      
      return found;
    }
    public static String grep(String grepFor, Path path, int[] results) throws IOException {
        final byte[] tosearch = grepFor.getBytes(StandardCharsets.UTF_8);
        StringBuilder report = new StringBuilder();
        int padding = 1; // need to scan 1 character ahead in case it is a word boundary.
        int linecount = 0;
        int matches = 0;
        boolean inword = false;
        boolean scantolineend = false;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            final long length = channel.size();
            int pos = 0;
            while (pos < length) {
                long remaining = length - pos;
                // int conversion is safe because of a safe MAPSIZE.. Assume a reaosnably sized tosearch.
                int trymap = MAPSIZE + tosearch.length + padding;
                int tomap = (int)Math.min(trymap, remaining);
                // different limits depending on whether we are the last mapped segment.
                int limit = trymap == tomap ? MAPSIZE : (tomap - tosearch.length);
                MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, pos, tomap);
                
                pos += (trymap == tomap) ? MAPSIZE : tomap;
                for (int i = 0; i < limit; i++) {
                    final byte b = buffer.get(i);
                    if (scantolineend) {
                        if (b == '\n') {
                            scantolineend = false;
                            inword = false;
                            linecount ++;
                        }
                    } else if (b == '\n') {
                        linecount++;
                        inword = false;
                    } else if (b == '\r' || b == ' ') {
                        inword = false;
                    } else if (!inword) {
                        if (wordMatch(buffer, i, tomap, tosearch)) {
                            matches++;
                            i += tosearch.length - 1;
                            if (report.length() > 0) {
                                report.append(", ");
                            }
                            report.append(linecount);
                            scantolineend = true;
                        } else {
                            inword = true;
                        }
                    }
                }
            }
        }
        results[0] = matches;
        return "Times found at--" + matches + "\nWord found at--" + report;
    }

    public static String grep(String grepFor, Path path, int[] results, String fileType) throws IOException {
        final byte[] tosearch = grepFor.getBytes(StandardCharsets.UTF_8);
        StringBuilder report = new StringBuilder();
        int padding = 1; // need to scan 1 character ahead in case it is a word boundary.
        int linecount = 0;
        int matches = 0;
        boolean inword = false;
        boolean scantolineend = false;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            final long length = channel.size();
            int pos = 0;
            while (pos < length) {
                long remaining = length - pos;
                // int conversion is safe because of a safe MAPSIZE.. Assume a reaosnably sized tosearch.
                int trymap = MAPSIZE + tosearch.length + padding;
                int tomap = (int)Math.min(trymap, remaining);
                // different limits depending on whether we are the last mapped segment.
                int limit = trymap == tomap ? MAPSIZE : (tomap - tosearch.length);
                MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, pos, tomap);
                
                pos += (trymap == tomap) ? MAPSIZE : tomap;
                for (int i = 0; i < limit; i++) {
                    final byte b = buffer.get(i);
                    if (scantolineend) {
                        if (b == '\n') {
                            scantolineend = false;
                            inword = false;
                            linecount ++;
                        }
                    } else if (b == '\n') {
                        linecount++;
                        inword = false;
                    } else if (b == '\r' || b == ' ') {
                        inword = false;
                    } 
                    else
                      if ( !inword )
                      {
                        boolean wordMatch;
                        switch ( fileType )
                        {
                          case "patman":
                            wordMatch = patmanWordMatch( buffer, i, tomap, tosearch );
                            break;
                          default:
                            wordMatch = wordMatch( buffer, i, tomap, tosearch );
                            break;
                        }
                        if ( wordMatch )
                        {
                          matches++;
                          i += tosearch.length - 1;
                          if ( report.length() > 0 )
                          {
                            report.append( ", " );
                          }
                          report.append( linecount );
                          scantolineend = true;
                        }
                        else
                        {
                          inword = true;
                        }
                    }
                }
            }
        }
        results[0] = matches;
        return "Times found at--" + matches + "\nWord found at--" + report;
    }

    private static boolean wordMatch(MappedByteBuffer buffer, int pos, int tomap, byte[] tosearch) {
        //assume at valid word start.
        for (int i = 0; i < tosearch.length; i++) {
            if (tosearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        byte nxt = (pos + tosearch.length) == tomap ? (byte)' ' : buffer.get(pos + tosearch.length); 
        return nxt == ' ' || nxt == '\n' || nxt == '\r' ;
    }
    private static boolean patmanWordMatch(MappedByteBuffer buffer, int pos, int tomap, byte[] tosearch) {
        //assume at valid word start.
        for (int i = 0; i < tosearch.length; i++) {
            if (tosearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        byte nxt = (pos + tosearch.length) == tomap ? (byte)' ' : buffer.get(pos + tosearch.length); 
        return nxt == ' ' || nxt == '\n' || nxt == '\r' || nxt == '(';
    }
}