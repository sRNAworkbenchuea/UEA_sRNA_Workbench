/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 *
 * @author ezb11yfu
 */
public class FastaMap extends HashMap<String, FastaRecord>
{
    public FastaMap()
    {
        super();
    }

    /**
     * Creates a FastaMap from an exiting map of <String, Integer> where the
     * key was the sequence and the integer was the abundance count.
     * 
     * Modified 19/02/14 to allow a map of any <String, Number>
     * 
     * @param data Map of distinct short reads.
     */
    public FastaMap(Map<String, ? extends Number> data)
    {
        super(data.size());

        Map<String, FastaRecord> new_data = convertFromLean(data);

        putAll(new_data);
    }


    /**
     * Creates a map of fasta records from a list, keyed by sequence.
     * Auto increments abundance count as required.
     * @param seqs List of short reads
     */
    public FastaMap(List<FastaRecord> seqs)
    {
        super();

        Map<String, FastaRecord> new_data = convertFromList(seqs);

        putAll(new_data);
    }

    /**
     * Creates a new FastaMap from a PatmanEntry.  Starts by creating a new default
     * FastaRecord, and then adds it to the newly create FastaMap.
     * @param pe PatmanEntry containing the sRNA hit to a mature miRNA
     */
    public FastaMap(PatmanEntry pe)
    {
        super();

        // Create a new fasta record from the patman entry
        FastaRecord new_seq = new FastaRecord(pe);

        put(pe.getSequence(), new_seq);
    }

    /**
     * Convert A String -> Number representation of sequence expression
     * to String -> FastaRecord
     * 
     * This has been modified to allow all subclasses of Number to be used as the count
     * These are converted to Double, since FastaRecord stores these values as doubles anyway
     * @param data
     * @return 
     */
    private static Map<String, FastaRecord> convertFromLean(Map<String, ? extends Number> data)
    {
        Map<String, FastaRecord> new_data = new HashMap<>(data.size());

        for (Entry<String, ? extends Number> e : data.entrySet())
        {
            String sequence = e.getKey();
            String sequenceId = sequence;
            double abundance = e.getValue().doubleValue();

            if (sequence.isEmpty())
            {
                sequence = "EMPTY-SEQUENCE";
                sequenceId = "EMPTY-SEQUENCEID";
            }

            FastaRecord fr = new FastaRecord(sequenceId, null, sequence);
            fr.setAbundance(abundance);

            new_data.put(sequenceId, fr);
        }

        return new_data;
    }

    private static Map<String, FastaRecord> convertFromList(List<FastaRecord> data)
    {
        Map<String, FastaRecord> new_data = new HashMap<String, FastaRecord>();

        for (FastaRecord fr : data)
        {
            FastaRecord existing_fr = new_data.get(fr.getSequence());

            if (existing_fr != null)
            {
                existing_fr.incrementAbundance(1);
            }
            else
            {
                fr.setAbundance(1);
                new_data.put(fr.getSequence(), fr);
            }
        }

        return new_data;
    }

    /**
     * Gets the entire FastaRecord represented by the given sequence
     * @param seq The sequence to find the record for
     * @return The FastaRecord represented by the given sequence
     */
    public FastaRecord getRecord(String seq)
    {
        return this.get(seq);
    }

    /**
     * The abundance for a particular sequence in this map
     * @param seq The sequence to find the abundance for
     * @return The abundance of the requested sequence
     */
    public int getAbundance(String seq)
    {
        return this.get(seq).getAbundance();
    }

    /**
     * The number of distinct sequences represented by this map
     * @return Number of distinct sequences
     */
    public int getDistinctSeqCount()
    {
        return this.size();
    }

    /**
     * Gets the total number of reads represented by this map.  The abundance of
     * all distinct sequences are summed.
     * @return The total sequence count (number of reads)
     */
    public int getTotalSeqCount()
    {
        int count = 0;
        for (FastaRecord fr : this.values())
        {
            count += fr.getAbundance();
        }

        return count;
    }

    /**
     * Gets the total number of times this sequence has hit against a reference list
     * @return Total hits against reference list
     */
    public int getTotalHits()
    {
        int count = 0;
        for (FastaRecord fr : this.values())
        {
            count += fr.getHitCount();
        }

        return count;
    }
}
