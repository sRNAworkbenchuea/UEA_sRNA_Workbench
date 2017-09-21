package uk.ac.uea.cmp.srnaworkbench.data.sequence;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mka07yyu
 */
public class NucleotidePosition {
    
    private Map<Nucleotide, Integer> counts;
        
    private int position;
    
    private int total;
    
    
    public NucleotidePosition(int position)
    {
        this.counts = new EnumMap<>(Nucleotide.class);
        for(Nucleotide n : Nucleotide.values())
        {
            counts.put(n, 0);
        }
        this.position = position;
    }
    
    public void addNucleotide(Nucleotide n, int count)
    {
        counts.put(n, counts.get(n)+count);
        total += count;
    }
    
    public int get(Nucleotide n)
    {
        return counts.get(n);
    }
    
    public double getProp(Nucleotide n)
    {
        return (double) counts.get(n)/total;
    }
    
    public int getPosition()
    {
        return position;
    }
}
