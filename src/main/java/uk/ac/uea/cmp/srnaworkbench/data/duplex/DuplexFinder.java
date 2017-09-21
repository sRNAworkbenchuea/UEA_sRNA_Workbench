/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.duplex;

import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ezb11yfu
 */
public class DuplexFinder 
{
    private int usable_cores;
    
    private final Map<String, Integer> mirnas;
    private final Map<String, Integer> mirna_stars;
    
    private int allowed_distance;
    private int distance_cap;
    private int duplex_limit;
    
    private File temp_dir;
    
    
    public DuplexFinder(Map<String, Integer> mirnas, Map<String, Integer> mirna_stars, int allowed_distance, File temp_dir)
    {
        this(mirnas, mirna_stars, allowed_distance, 0, -1, temp_dir);
    }
    
    public DuplexFinder(Map<String, Integer> mirnas, Map<String, Integer> mirna_stars, int allowed_distance, int distance_cap, int duplex_limit, File temp_dir)
    {
        this.usable_cores = Runtime.getRuntime().availableProcessors() < 4 ? 1 : Runtime.getRuntime().availableProcessors() - 1;
        
        this.mirnas = mirnas;
        this.mirna_stars = mirna_stars;
        this.allowed_distance = allowed_distance;
        this.distance_cap = distance_cap;
        this.duplex_limit = duplex_limit;
        
        this.temp_dir = temp_dir;
    }
    
    
    public List<Duplex> find()
            throws Exception
    {
      LOGGER.log( Level.FINE, "**** Duplex Finder ****" );  
        
        // Split mirnas into thread pots.
        ArrayList<LinkedHashMap<String,Integer>> queue = new ArrayList<LinkedHashMap<String,Integer>>();
        int i = 0;
        int j = 0;
        int limit = this.mirnas.size() / this.usable_cores;
        for (Map.Entry<String, Integer> mir : this.mirnas.entrySet())
        {
            if (i == 0)
            {
                queue.add(new LinkedHashMap<String,Integer>());
            }

            queue.get(j).put(mir.getKey(), mir.getValue());

            if(i == limit)
            {
                j++;
                i = 0;
            }
            else
            {
                i++;
            }
        }

        // Start worker threads
        int k = 0;
        ExecutorService es = Executors.newFixedThreadPool(this.usable_cores);
        Collection<DuplexFinderTask> tasks = new ArrayList<DuplexFinderTask>();
        for(LinkedHashMap<String,Integer> mirs : queue)
        {
            tasks.add(new DuplexFinderTask(k++, mirs, this.mirna_stars, this.temp_dir));
        }

        LOGGER.log( Level.FINE, "DF: Starting threads.");
        List<Future<LinkedHashMap<Duplex,Integer>>> answers = es.invokeAll(tasks);
        LOGGER.log( Level.FINE, "DF: All threads finished successfully.");
        
        // Stitch together results.
        LOGGER.log( Level.FINE, "DF: Concatenating miRNA duplexes... ");
        LinkedHashMap<Duplex,Integer> duplexes = new LinkedHashMap<Duplex,Integer>();
        for(Future<LinkedHashMap<Duplex,Integer>> f : answers)
        {
            LinkedHashMap<Duplex,Integer> df = f.get();

            if (df == null)
                throw new Exception("DF: Couldn't collect thread results.");

            duplexes.putAll(df);
        }
        LOGGER.log( Level.FINE, "done.");
        LOGGER.log( Level.FINE, "DF: Found {0} duplexes.", duplexes.size());
        
        // Sort the list of duplexes
        TreeSet<Duplex> ss = new TreeSet<Duplex>(duplexes.keySet());
        
        return new ArrayList<Duplex>(ss);
    }   
    
    
    
    private class DuplexFinderTask implements Runnable, Callable<LinkedHashMap<Duplex,Integer>>
    {
        private Map<String, Integer> mirnas; 
        private Map<String, Integer> mirna_stars;
        private LinkedHashMap<Duplex,Integer> duplexes;
        private int thread_id;
        private File temp_dir;
                
        public DuplexFinderTask(int id, Map<String, Integer> mirnas, Map<String, Integer> mirna_stars, File temp_dir)
        {
            this.thread_id = id;
            this.mirnas = mirnas;
            this.mirna_stars = mirna_stars;
            this.duplexes = new LinkedHashMap<Duplex,Integer>();
            this.temp_dir = temp_dir;
        }

        // Find all possible miRNA/miRNA* duplexes.  This will produce
        // duplicate duplexes.
        @Override
        public void run()
        {
            int i = 0;
            int length = mirnas.size();
            for(String seq1 : mirnas.keySet())
            {
                for(String seq2 : mirna_stars.keySet())
                {
                    // We don't want to compare the same duplex against itself
                    if (seq1.equals(seq2))
                    {
                        continue;
                    }
                    
                    // Ensure that the mature sequence has the highest abundance.
                    // The star sequence will not have a higher abundance than the
                    // mature sequence.  If it does then it should already be a mature
                    // candidate too and simply hasn't been processed yet, in which
                    // case continue and we'll pick it up later.
                    int abd1 = 0, abd2 = 0;
                    String s1 = null, s2 = null;
                    if (mirnas.get(seq1) >= mirna_stars.get(seq2))
                    {
                        s1 = seq1;
                        s2 = seq2;
                        abd1 = mirnas.get(seq1);
                        abd2 = mirna_stars.get(seq2);
                    }
                    else
                    {
                        continue;
                    }

                    if (possibleDuplex(seq1,seq2))
                    {
                        // Note that duplexes are auto converted to RNA form.
                        Duplex d = new Duplex( 
                                s1, s1, abd1, 
                                s2, s2, abd2, 
                                1, this.temp_dir);
                        
                        if (duplexes.get(d) == null)
                        {
                            duplexes.put(d, 1);
                        }
                        else
                        {
                            // This shouldn't ever happen... but just incase increase
                            // the duplex abundance count.
                            int new_abd = duplexes.get(d) + 1;
                            duplexes.put(d, new_abd);
                            d.setDuplexAbundance(new_abd);
                        }
                    }
                }
                if (++i % 100 == 0)
                    LOGGER.log( Level.FINE, "DF: Thread {0} processed miRNA candidate: {1}/{2}", new Object[]{ this.thread_id, i, length });
            }
        }
        
        @Override
        public LinkedHashMap<Duplex,Integer> call()
        {
            run();
            return this.duplexes;
        }
    }
    
    // In this method we simply use the Levenshtein distance to work out if two 
    // sequences could be a duplex.  This isn't very sophisticated however.  In
    // Simon's perl script he uses MUSCLE (MUltiple Sequence Comparison by Log Expectation)
    // which may perform slightly better... but not necessarily.  In any case
    // there is scope for improving this method.
    private boolean possibleDuplex(String s1, String s2)
    {
        if (this.allowed_distance == -1)
            return true;
        
        // Note the duplex in reality should only reverse s2.  However, when using
        // LevenshteinDistance it expects the sequences to be identical, so complementing
        // is also required
        String s2r = SequenceUtils.DNA.reverseComplement( s2 );

        // If allowed_distance is -1 then all duplexes are valid.
        // Edit distance treats mismatches, insertions and deletions with equal weighting.
        int ld = StringUtils.getLevenshteinDistance(s1, s2r);
        
        return (ld <= this.allowed_distance && ld >= this.distance_cap);
    }
}
