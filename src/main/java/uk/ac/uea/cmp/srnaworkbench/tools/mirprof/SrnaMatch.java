/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.util.Map;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaRecord;


/**
 *
 * @author ezb11yfu
 */
final class SrnaMatch
{
    private int raw_count;
    
    private FastaMap seqs;

    public SrnaMatch()
    {
        this(0, new FastaMap());
    }

    public SrnaMatch(int raw_count,  FastaMap seqs)
    {
        this.raw_count = raw_count;
        this.seqs = seqs;
    }
    
    public int      getRawCount()       {return this.raw_count;}
    public FastaMap getSeqMap()         {return this.seqs;}
    
    public void setSeqMap(FastaMap fm)
    {
        this.seqs = fm;
    }
    
    public int incRawCount()            {return incRawCount(1);}
    public int incRawCount(int increment)      
    {
        this.raw_count += increment;
        return this.raw_count;
    }
    

    
    public void calculateCounts()
    {
        int count = 0;
        
        if (seqs == null)
        {
            this.raw_count = 0;
            return;
        }
        
        for(Map.Entry<String, FastaRecord> me : seqs.entrySet())
        {
            count += me.getValue().getAbundance();
        }
        
       
        this.raw_count = count;

    }
}
