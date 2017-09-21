/*
 * Grep.java
 *
 * Created on December 16, 2005, 10:29 AM
 *
 */

package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 *
 * @author Max Sauer
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JTextArea;

/**
 * Implementation of grep utility
 */
public class Grep
{
    private final static Charset charset = Charset.forName("ISO-8859-1");

    private final static CharsetDecoder decoder = charset.newDecoder();

    // Pattern used to parse lines
    private static Pattern linePattern = Pattern.compile(".*\r?\n");

    // The input pattern that we're looking for
    private static Pattern pattern;

    /**
     * Compile the pattern from the command line
     * @param pat regular expression pattern
     */
    public static void compile(String pat) {
        try {
            pattern = Pattern.compile(pat);
        } catch (PatternSyntaxException x) {
            System.err.println(x.getMessage());
            System.exit(1);
        }
    }

    /**
     * Use the linePattern to break the given CharBuffer into lines, applying
     * the input pattern to each line to see if we have a match
     * @param f Actual file
     * @param cb charbuffer with the decoded file
     */
    private static void grep(File f, CharBuffer cb, JTextArea out) {
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher
        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            if (pm.find()) {
                if(out == null)
                    System.out.print(f + ":" + lines + ":" + cs);
                else
                    out.append(f + ":" + lines + ":" + cs);
            }
            if (lm.end() == cb.limit())
                break;
        }
        System.out.println("lines: " + lines);
    }
    
    private static void grep(File f, CharBuffer cb, int[] found, ArrayList<String> readBacks) {
        Matcher lm = linePattern.matcher(cb); // Line matcher
        Matcher pm = null; // Pattern matcher
        int lines = 0;
        while (lm.find()) {
            lines++;
            CharSequence cs = lm.group(); // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            if (pm.find()) {
                found[0]++;
                if(readBacks != null)
                {
                     String[] fullLine = cs.toString().split(Tools.TAB);
                    
                    if(!readBacks.contains(fullLine[1]))
                    {
                        readBacks.add(fullLine[1]);
                    }
                }
            }
            if (lm.end() == cb.limit())
                break;
        }
        //System.out.println("lines: " + lines);
    }

    /**
     * Search for occurrences of the input pattern in the given file
     * @param f Actual file
     * @throws java.io.IOException throws IOException upon i/o error
     */
    public static void grep(File f, JTextArea out) throws IOException {

        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);

        // Perform the search
        grep(f, cb, out);

        // Close the channel and the stream
        fc.close();
    }
    
    public static void grep(File f, int[] amount, ArrayList<String> readBacks) throws IOException {

        // Open the file and then get a channel from the stream
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

        // Decode the file into a char buffer
        CharBuffer cb = decoder.decode(bb);

        // Perform the search
        grep(f, cb, amount, readBacks);

        // Close the channel and the stream
        fc.close();
    }

    /**
     * Main method
     * @param args The command line arguments. Should consist of regular expression pattern and
     * the list of files which should be grepped.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Grep pattern file...");
            return;
        }
        compile(args[0]);
        for (int i = 1; i < args.length; i++) {
            File f = new File(args[i]);
            try {
                grep(f, null);
            } catch (IOException x) {
                System.err.println(f + ": " + x);
            }
        }
    }

}