/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.io.SRNAFastaReader;

/**
 *
 * @author Dan
 */
public class ShortReadStats 
{
    private Map<String,Integer> short_reads;
    
    public ShortReadStats()
    {
        this(new HashMap<String,Integer>());
    }
    
    public ShortReadStats(Map<String,Integer> short_reads)
    {
        this.short_reads = short_reads;
    }
    
    public ShortReadStats(File infile) throws IOException
    {
        // Load from srnas from file if a path was provided and the file exists.
        // Otherwise assume the data was provided directly
        if (infile != null && infile.exists())
        {
            short_reads = new SRNAFastaReader(infile).process();
        }
        else
        {
            throw new IOException("Short read file not found: " + infile.getPath());
        }
    }
    
    public Map<Integer, Integer> getRedundantLengthDistribution()
    {
        HashMap<Integer, Integer> ld = new HashMap<Integer, Integer>();
        
        for (Map.Entry<String,Integer> me : this.short_reads.entrySet())
        {
            int seq_len = me.getKey().trim().length();
            int seq_abd = me.getValue();
            int old_len_abd = (ld.get(seq_len) == null ? 0 : ld.get(seq_len));
            int new_len_abd = old_len_abd + seq_abd;
            ld.put(seq_len, new_len_abd);
        }
        
        return ld;
    }
    
    public Map<Integer, Integer> getNonRedundantLengthDistribution()
    {
        HashMap<Integer, Integer> ld = new HashMap<Integer, Integer>();
        
        for (Map.Entry<String,Integer> me : this.short_reads.entrySet())
        {
            int seq_len = me.getKey().trim().length();
            int seq_abd = me.getValue();
            int old_len_abd = (ld.get(seq_len) == null ? 0 : ld.get(seq_len));
            int new_len_abd = old_len_abd + 1;
            ld.put(seq_len, new_len_abd);
        }
        
        return ld;
    }
    
    public int getRedundantCount()
    {
        int count = 0;
        
        for(Map.Entry<String,Integer> me : this.short_reads.entrySet())
        {
            count += me.getValue();
        }
        
        return count;
    }
    
    public int getNonRedundantCount()
    {
        return this.short_reads.size();
    }
    
    // Prints out a length distribution.  Input data format should be a Map<Integer,Integer>
    // structure where the key is the length, and the value is the count.
    public static String printLengthDistribution(Map<Integer,Integer> ld)
    {
        StringBuilder sb = new StringBuilder();
      
        for(Integer len : new TreeSet<Integer>(ld.keySet()))
        {
            sb.append( len ).append( "\t" ).append( ld.get(len));
            sb.append( LINE_SEPARATOR );
        }
        
        return sb.toString();
    }
    
    // Prints out a sorted item count.  Input data format should be a Map<String,Integer>
    // structure where the key is the item in String format, and the value is the count.
    public static String printCounts(Map<String,Integer> seqs)
    {
        StringBuilder sb = new StringBuilder();
      
        for(String str : new TreeSet<String>(seqs.keySet()))
        {
            sb.append( str ).append( " : " ).append( seqs.get(str));
            sb.append( LINE_SEPARATOR );
        }
        
        return sb.toString();
    }
}
