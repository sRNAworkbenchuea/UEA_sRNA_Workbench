package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.ExpressionElement;
import uk.ac.uea.cmp.srnaworkbench.utils.matrix.SparseExpressionMatrix;

/**
 * Calculates and provides jaccard indices based on a SparseExpressionMatrix between
 * two samples held in that SparseExpressionMatrix
 * 
 * Jaccard index is calculated by ranking sequences by abundance and taking the top
 * N sequences for each sample. Jaccard index is then
 * 
 *              intersection of N sequences in samples/union of N sequences in samples
 * 
 * @author Matt
 */
public class JaccardIndex
{
    private final SparseExpressionMatrix sem;
    private final ArrayList<ArrayList<String>> samples;
    private final HashMap<String, Set<String>> sortedSeqs = new HashMap<>();
    
    public JaccardIndex(SparseExpressionMatrix sem)
    {
        this.sem = sem;
        this.samples = sem.getFileNames();
        
        for(String sample : this.sem.getFlattenedFileNames())
        {
            // list of EEs to sort. Adding to a list and sorting at the end should
            // be faster than maintaining a sorted TreeSet
            List<ExpressionElement> thisExpressionList = new ArrayList<>();
            
            Iterator<Entry<String, ExpressionElement>> it = this.sem.getSampleIterator(sample);
            while(it.hasNext())
            {
                Entry<String, ExpressionElement> entry = it.next();
                if(!entry.getKey().equals(entry.getValue().sequence))
                {
                    LOGGER.log(Level.WARNING, "ExpressionElement sequence field " 
                            + "is not correctly set. Matrix entry has sequence{0} and Element contains {1}" + ". This was resolved "
                            + "by using the matrix key.", new Object[]{entry.getKey(), entry.getValue().sequence});
                    entry.getValue().sequence = entry.getKey();
                }
                thisExpressionList.add(entry.getValue());
            }
            
            Collections.sort(thisExpressionList);
            
            Set<String> sortedSeqSet = new LinkedHashSet<>(thisExpressionList.size());
            
            for(ExpressionElement e : thisExpressionList)
            {
                sortedSeqSet.add(e.sequence);
            }
            
            sortedSeqs.put(sample, sortedSeqSet);        
        }
    }
    
    /**
     * This is for calculating a single index for a specific most abundant N sequences
     * @param sampleA sample name
     * @param sampleB other sample name
     * @param numberOfSeqs
     * @return jaccard index comparing the top numberOfSeqs sequences for sampleA
     * and sampleB
     */
    public double calculateJaccardIndex(String sampleA, String sampleB, int numberOfSeqs)
    {
        Iterator<String> itA = sortedSeqs.get(sampleA).iterator();
        Iterator<String> itB = sortedSeqs.get(sampleB).iterator();
        
        Set<String> setA = new LinkedHashSet<>();
        Set<String> setB = new LinkedHashSet<>();

        for(int i=0; i < numberOfSeqs; i++)
        {
            String a = itA.next();
            String b = itB.next();
            setA.add(itA.next());
            setB.add(itB.next());
        }
        
        int numUnion = calcNumUnion(setA, setB);
        int numIntersect = calcNumIntersect(setA, setB);
        
        double jaccardIndex = (double) numIntersect/numUnion;
        return(jaccardIndex);
    }
    
    /**
     * This is for calculating a series of jaccard indexes with increasing number of 
     * sequences. This can be used to obtain a graph that compares the change in sequence
     * similarities as less abundant sequences are added to the calculation.
     * @param sampleA
     * @param sampleB
     * @param increment
     * @return a List of Entry objects containing the number of sequences compared as the key
     * and the jaccard index as the value.
     */
    public List<java.util.Map.Entry<Integer, Double>> calculateJaccardSeries(String sampleA, String sampleB, int increment)
    {
        Iterator<String> itA = sortedSeqs.get(sampleA).iterator();
        Iterator<String> itB = sortedSeqs.get(sampleB).iterator();

        int minSize = Math.min(sortedSeqs.get(sampleA).size(), sortedSeqs.get(sampleB).size());
        
        Set<String> setA = new LinkedHashSet<>();
        Set<String> setB = new LinkedHashSet<>();

        List<java.util.Map.Entry<Integer, Double>> jseries = new ArrayList<>();
        
        for (int i = 0; i < minSize; i++)
        {
            setA.add(itA.next());
            setB.add(itB.next());
            
            if(i % increment == 0)
            {
                int numIntersect = calcNumIntersect(setA, setB);
                int numUnion = calcNumUnion(setA, setB);
                double jindex = (double) numIntersect/numUnion;
                jseries.add( new java.util.HashMap.SimpleEntry( i, jindex ) );
            }
        }
        
        return(jseries);
    }
    
    private int calcNumIntersect(Set<String> a, Set<String> b)
    {
        Set<String> intersect = new LinkedHashSet(a);
        intersect.retainAll(b);
        return(intersect.size());
    }
    
    private int calcNumUnion(Set<String> a, Set<String> b) {
        Set<String> union = new LinkedHashSet(a);
        union.addAll(b);
        return (union.size());
    }
    
    public static void main(String[] args)
    {
        try
        {
            SparseExpressionMatrix sem = SparseExpressionMatrix.getTestMatrix();
            JaccardIndex ji = new JaccardIndex(sem);
            // TODO: Turn this into a unit test
            System.out.println(ji.calculateJaccardIndex(sem.getFlattenedFileNames().get(0), sem.getFlattenedFileNames().get(1), 10));
            System.out.println(ji.calculateJaccardSeries(sem.getFlattenedFileNames().get(0), sem.getFlattenedFileNames().get(1), 1));
        } catch (Exception ex)
        {
            Logger.getLogger(JaccardIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}

