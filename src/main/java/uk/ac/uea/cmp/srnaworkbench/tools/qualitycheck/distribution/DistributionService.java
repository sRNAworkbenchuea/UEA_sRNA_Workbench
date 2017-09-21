package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AbundanceBoxAndWhiskerDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.MBoxAndWhiskerDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.AbundanceBoxAndWhisker_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Distribution_Outlier;
import uk.ac.uea.cmp.srnaworkbench.database.entities.AbundanceWindow;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Distribution;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MA_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MBoxAndWhisker_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.EmptyDistributionException;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.UnexpectedQueryReturnTypeException;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 * A service that handles creation and writing of Distribution-type operations
 * on the database
 * @author Matthew
 */
@Service("DistributionService")
@Transactional
public class DistributionService {
    
    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;
    
    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDao;

    @Autowired
    private AbundanceBoxAndWhiskerDAO abwDao;
    
    @Autowired
    private MBoxAndWhiskerDAO mbwdao;
    
    private static final int LQ = 25;
    private static final int MED = 50;
    private static final int UQ = 75;
    private static final List<Integer> QUARTILES = new ArrayList<>();

    static {
        QUARTILES.add(LQ);
        QUARTILES.add(MED);
        QUARTILES.add(UQ);
    }

    private static final double RANGE_FACTOR = 1.5;
    
    /**
     * Calculates the index at which given percentiles are found, given also the
     * number of counts there are.
     *
     * @param percentiles a list of percentiles as a percentage of the number of
     * counts
     * @param numberOfCounts the number of counts there are
     * @return Map mapping the given percentiles to resulting indexes.
     */
    public static Map<Integer, Integer> calculatePercentileRanks(List<Integer> percentiles, int numberOfCounts) {
        Map<Integer, Integer> percentileRanks = new HashMap<>();
        for (int percentile : percentiles) {
            //int rank = (int) Math.round(numberOfCounts * ((double) percentile / 100));
            // Rounding up caused distributions of size 1 to have quartile ranks outside the distribution.
            int rank = (int) (numberOfCounts * ((double) percentile / 100));

            percentileRanks.put(percentile, rank);
        }
        return (percentileRanks);
    }
    
    
    /**
     * - Query the whole dataset
     * - Assign values to a set of distributions that will be held in memory.
     * - When all values are assigned, sort distributions and calculate statistics
     * @param samples
     * @param normTypes
     * @param annotations
     * @param numberOfWindows
     * @param windowSize
     * @param firstCount
     */
    public void calculateAbundanceWindowDistribution2(List<String> samples, List<NormalisationType> normTypes, AnnotationSetList annotations, 
            int numberOfWindows, int windowSize, int firstCount)
    {
        
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session);
        
        ScrollableResults results = session.createQuery("select e.expression as expression, s.filename_sequence as sample, "
                + "e.normType as normType, u.type as annotation, u.totalCount as totalCount "
                + "from Sequence_Entity s "
                + "join s.unique_sequence u "
                + "join s.expressions e order by u.totalCount desc")
                .setResultTransformer(Transformers.aliasToBean(DistributionBean.class))
                .scroll();
        
        // Each distribution contains numWindows * windowSize seqs, split into windows of numWindows.
        int totalSeqsPerDist = numberOfWindows * windowSize;
        
        // Add each value to the set of distributions. The object will assign the value to right distribution
        DistributionSet distributions = new DistributionSet(new DistributionWindowList(numberOfWindows, windowSize, firstCount), firstCount);
        while(!distributions.allDistributionsAreFull() && results.next())
        {
            DistributionBean bean = (DistributionBean) results.get(0);
            
            if( samples.contains(bean.getSample().getFileID()) && normTypes.contains(bean.getNormType()) 
                    && annotations.getSetForType(bean.getAnnotation().getId().getType()) != null ){
                bean.annotationSet = annotations.getSetForType(bean.getAnnotation().getId().getType()).getName();
                distributions.addValue(bean);
            }
        }
        
        List<AbundanceBoxAndWhisker_Entity> boxplots = distributions.getBoxplots();
        
        // Propogate the distributions in the otder given by the List inputs
        for(String sample : samples)
        {
            for(NormalisationType normType : normTypes)
            {
                Iterator<AbundanceBoxAndWhisker_Entity> bpIt = boxplots.iterator();
                while (bpIt.hasNext()) {
                    AbundanceBoxAndWhisker_Entity abw = bpIt.next();
                    if(abw.getSample().getFileID().equals(sample) && abw.getNormType().equals(normType))
                    {
                        session.save(abw);
                        batch.batchFlush();
                        // We have saved this boxplot to the database so we can remove it from out list
                        bpIt.remove();
                    }
                }
            }
        }

        // commit all boxplots
        batch.finish();
        session.close();
    }
    
    public void writeAllAbundanceDistributions(Path file) throws IOException
    {
        JsonFactory jfac = new JsonFactory();
        JsonGenerator jg = jfac.createGenerator(file.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        
        Session session = abwDao.getSessionFactory().openSession();
        ScrollableResults plots = session.createQuery("from AbundanceBoxAndWhisker_Entity a "
                + "join a.sample f join f.sample s order by s.sampleNumber asc, f.replicateID asc").scroll(ScrollMode.FORWARD_ONLY);
        jg.writeStartArray();

        while(plots.next())
        {
            AbundanceBoxAndWhisker_Entity dist = (AbundanceBoxAndWhisker_Entity) plots.get(0);
            jg.writeStartObject();
            jg.writeStringField("Filename", dist.getSample().getFileID());
            jg.writeStringField("Window", dist.getWindow().toString());
            jg.writeStringField("Normalisation", dist.getNormType().getAbbrev());
            jg.writeStringField("Annotation", dist.getAnnotationType());
            _jsonWriteBoxplot(jg, dist);
            jg.writeEndObject();

        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public void clearAbundanceDistributions()
    {
        Session session = abwDao.getSessionFactory().openSession();
        session.createQuery("delete from AbundanceBoxAndWhisker_Entity").executeUpdate();
//        session.createQuery("delete from AbundanceBoxAndWhisker_Outlier").executeUpdate();
        session.close();
    }
    
    public void clearMValueDistributions() {
        Session session = abwDao.getSessionFactory().openSession();
        session.createQuery("delete from MBoxAndWhisker_Entity").executeUpdate();
//        session.createQuery("delete from MBoxAndWhisker_Outlier").executeUpdate();
        session.close();
    }
    
    public void writeAllMValueDistributions(Path file) throws IOException
    {
        JsonFactory jfac = new JsonFactory();
        JsonGenerator jg = jfac.createGenerator(file.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        Session session = this.mbwdao.getSessionFactory().openSession();
        ScrollableResults plots = session.createCriteria(MBoxAndWhisker_Entity.class).addOrder(Order.asc("rnaSize")).scroll(ScrollMode.FORWARD_ONLY);//.addOrder(Order.asc("RNA_Size"))
        jg.writeStartArray();

        while (plots.next()) {
            MBoxAndWhisker_Entity dist = (MBoxAndWhisker_Entity) plots.get(0);
            jg.writeStartObject();
            jg.writeStringField("Pair", dist.getFilePair().getPairKey());
            jg.writeNumberField("Offset", dist.getOffset());
            jg.writeStringField("Normalisation", dist.getNormType().getAbbrev());
            jg.writeStringField("Annotation", dist.getAnnotation());
            jg.writeNumberField("Size", dist.getRnaSize());
            _jsonWriteBoxplot(jg, dist);
            jg.writeEndObject();

        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public static void _jsonWriteBoxplot(JsonGenerator jg, Distribution d ) throws IOException {
        jg.writeObjectFieldStart("Boxplot");

        jg.writeNumberField("Min", d.getMinRange());
        jg.writeNumberField("LQ", d.getLq());
        jg.writeNumberField("MED", d.getMed());
        jg.writeNumberField("UQ", d.getUq());
        jg.writeNumberField("Max", d.getMaxRange());
        jg.writeNumberField("N", d.getNumSeqs());
        writeOutlierArray(jg, d.getOutliers());
        jg.writeEndObject();
    }
    
    public static void writeOutlierArray(JsonGenerator jg, Set<Distribution_Outlier> outliers) throws IOException {
        jg.writeArrayFieldStart("Outliers");
        Iterator<Distribution_Outlier> outlierit = outliers.iterator();
        while (outlierit.hasNext()) {
            jg.writeNumber(outlierit.next().getOutlier());
        }
        jg.writeEndArray();
    }
    
    public void createMValueDistribution(FilePair pair, NormalisationType norm, int offset, AnnotationSet annotations) {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
            Batcher batch = new Batcher(session);
            List<Integer> sizeList = session.createQuery(
                    "select distinct(u.RNA_Size) from MA_Entity m "
                    + "join m.refSeqEntity r "
                    + "join r.unique_sequence u "
                    + "where m.id.filePair.pairKey=:pair"
            ).setString("pair", pair.getPairKey()).list();

            for (Integer size : sizeList) {
                Criteria crit = session.createCriteria(MA_Entity.class)
                        .add(Restrictions.eq("id.filePair.pairKey", pair.getPairKey()))
                        .createAlias("refSeqEntity", "seq").createAlias("seq.unique_sequence", "useq")
                        .add(Restrictions.in("annoType", annotations.getTypes()))
                        .add(Restrictions.eq("id.normType", norm))
                        .add(Restrictions.eq("id.offset", offset))
                        .add(Restrictions.eq("useq.RNA_Size", size))
                        .setProjection(Projections.property("M"))
                        .addOrder(Order.asc("M"));
                    
                try {
                    Distribution dist = createDistribution(crit,Logarithm.NONE, -1);
                    MBoxAndWhisker_Entity mbw = new MBoxAndWhisker_Entity(norm, pair, offset, size, annotations.getName(), dist);
                    this.mbwdao.createOrUpdate(mbw);
                    batch.batchFlush();
                } catch (EmptyDistributionException ex) {
                    // This distribution is empty. Skip with a warning
                    LOGGER.log(Level.WARNING, "Abundance distribution for {0} has no values. Skipping.",
                            (new StringJoiner(",")).add(pair.getPairKey()).add(norm.toString()).add(annotations.getName()).add(""+offset).add("size: "+size));
                }
            }
            batch.finish();
        session.close();
    }

    /**
     * Create a Distribution from a Query. The Query should return a list of
     * abundances.
     *
     * @param crit a Query that retrieves a list of values to make a
     * distribution from
     * @param session
     * @param logBase
     * @param normType
     * @return A filled-out Distribution entity, or null if there were no
     * results
     * @throws uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.EmptyDistributionException
     */
    public static Distribution createDistribution(Query crit, Logarithm logBase, int totalSeqs) throws EmptyDistributionException {
        ScrollableResults result = crit.scroll(ScrollMode.SCROLL_INSENSITIVE);
        //DatabaseWorkflowModule.getInstance().printLap("Querying distribution");
        Distribution d = createDistribution(result, logBase, totalSeqs);
        //DatabaseWorkflowModule.getInstance().printLap("Scrolling distribution");
        result.close();
        return d;
    }

    /**
     * Create a Distribution from a Criteria. The Criteria should return a list
     * of abundances.
     *
     * @param crit a criteria that retrieves a list of values to make a
     * distribution from
     * @param session
     * @param logBase
     * @param normType
     * @return A filled-out Distribution entity, or null if there were no
     * results
     * @throws uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.EmptyDistributionException
     */
    public static Distribution createDistribution(Criteria crit, Logarithm logBase, int totalSeqs) throws EmptyDistributionException {
        ScrollableResults result = crit.scroll(ScrollMode.SCROLL_INSENSITIVE);
        Distribution d = createDistribution(result, logBase, totalSeqs);
        result.close();
        return d;
    }
    
    public void createMValueNrDistribution(FilePair pair, NormalisationType norm, Double offset, AnnotationSet annotations, Double minExpression)
    {
        if(!annotations.getTypes().isEmpty()){
            Session session = this.alignedSeqDao.getSessionFactory().openSession();
            Batcher batch = new Batcher(session);

            List<Integer> sizeList = session.createQuery(
                    "select distinct(m.RNA_Size) from MA_NR_Entity m "
                    + "where m.filePair.pairKey=:pair"
            ).setString("pair", pair.getPairKey()).list();
            //DatabaseWorkflowModule.getInstance().printLap("Mdist: Retrieving unique size classes");

            for(int size : sizeList){
                Logarithm logBase = Logarithm.BASE_2;
                String HQL_FILTER = " where ma.RNA_Size=:size and ma.filePair.pairKey=:pair and ma.normType=:normType and ma.offset=:offset and ma.annoType in (:annoTypes) "
                        + "and ma.refExpression > :minExpression and ma.obsExpression > :minExpression ";
                Long N = (Long) session.createQuery("select sum(ma.count) from MA_NR_Entity ma " + HQL_FILTER)
                        .setString("pair", pair.getPairKey())
                        .setParameter("normType", norm)
                        .setParameter("offset", offset)
                        .setParameterList("annoTypes", annotations.getTypes())
                        .setParameter("size", size)
                        .setParameter("minExpression", minExpression).uniqueResult();

                if(N != null){

                    //DatabaseWorkflowModule.getInstance().printLap("Mdist: Counting MA values for size " + size + " pair " + pair.getPairKey() + " norm " + norm.getAbbrev());

                    Map<Integer, Integer> ranks = calculatePercentileRanks(QUARTILES, N.intValue());
                    Map<Integer, Double> pv = new HashMap<>();

                    Query query = session.createQuery("select ma.M, ma.count from MA_NR_Entity ma "
                            + HQL_FILTER
                            + "order by ma.M asc")
                            .setString("pair", pair.getPairKey())
                            .setParameter("normType", norm)
                            .setParameter("offset", offset)
                            .setParameterList("annoTypes", annotations.getTypes())
                            .setParameter("size", size).setParameter("minExpression", minExpression);

                    ScrollableResults results = query.scroll(ScrollMode.SCROLL_INSENSITIVE);

                    int distIndex = 0;
                    
                    if(results.next()){
                        for (Integer q : QUARTILES) {
                            int rank = ranks.get(q);
                            while(distIndex < rank && !results.isLast())
                            {
                                distIndex += results.getInteger(1);
                                results.next();
                            }

                            if(!results.isLast())
                                results.next();
                            double rankAbundance = results.getDouble(0);
                            pv.put(q, rankAbundance);
                        }

                        // calculate whiskers
                        double iqr = pv.get(UQ) - pv.get(LQ);

                        double minRange = pv.get(LQ) - RANGE_FACTOR * iqr;
                        double maxRange = pv.get(UQ) + RANGE_FACTOR * iqr;

                        results.first();
                        double minAbundance = (double) results.get(0);
                        double maxAbundance = minAbundance;
                        boolean withinRange = false;
                        Set<Distribution_Outlier> outliers = new HashSet<>();

                        results.beforeFirst();
                        while (results.next()) {
                            double currentAbundance = (double) results.get(0);

                            if (currentAbundance < minRange) {
                                // current abundance is a low outlier
                                outliers.add(new Distribution_Outlier(currentAbundance));
                            } 
                            else if (currentAbundance > maxRange) {
                                // current abundance is a high outlier
                                outliers.add(new Distribution_Outlier(currentAbundance));
                            } 
                            else {
                                // current abundance is between outlying ranges

                                if (!withinRange) // is this the first abundance within range?
                                {
                                    withinRange = true;
                                    minAbundance = currentAbundance; // set minAbundance
                                }

                                maxAbundance = currentAbundance; // advance max abundance
                            }
                        }




                        Distribution dist = new Distribution(logBase, pv.get(LQ), pv.get(MED), pv.get(UQ), minAbundance, maxAbundance);
                        dist.setNumSeqs(N.intValue());

                        
                        // propagate the number sequences used to calculate this distribution
                        dist.setNumSeqs(N.intValue());

                        MBoxAndWhisker_Entity mbw = new MBoxAndWhisker_Entity(norm, pair, offset.intValue(), size, annotations.getName(), dist);
                        mbw.setOutliers(outliers);
                        this.mbwdao.createOrUpdate(mbw);
                        batch.batchFlush();
                    }
                    else
                    {
                        LOGGER.log(Level.WARNING, "An M value distribution was empty when its N was calculated as " + N + ". This was unexpected.");
                    }

                }

            }
            batch.finish();
            session.close();
        }
    }

    /**
     * Create a distribution from a ScrollableResults object
     *
     * @param result
     * @param session
     * @param logBase
     * @param normType
     * @return A filled-out Distribution entity, or null if there were no
     * results
     */
    private static Distribution createDistribution(ScrollableResults result, Logarithm logBase, int totalSeqs) throws EmptyDistributionException, UnexpectedQueryReturnTypeException{
         // Just because the window is a particular size does not mean our sequence list is of that size.
        // first check the length of the criteria to determine size
        // There are two options
        //  - assume an expression matrix - missing sequences in this file are zero-value
        // all 0 abundances are sorted to the bottom. Find the difference between window length
        // and result length and correct all counting by this.
        //  - ignore missing sequences - distributions are calculated on unequal N <-- we do this because there are no zeroes that will catch on log

        // Check for null result - i.e. no sequences found for query
        if (!result.last()) {
            throw new EmptyDistributionException();
        }

        int resultLength = result.getRowNumber() + 1;
        result.first();
        
        int numberOfZeros = 0;
        if(totalSeqs != -1)
        {
            numberOfZeros = totalSeqs - resultLength;
            if(numberOfZeros < 0)
                throw new IllegalArgumentException("Bad arguments for creating a distribution. "
                        + "total seqs was given as " + totalSeqs + " but there was actually " + resultLength);
        }

        // Because we are doing the second option, we calculate percentiles based on this filename's distribution size
        Map<Integer, Integer> ranks = calculatePercentileRanks(QUARTILES, resultLength);

        int absoluteN = result.getRowNumber();

        Map<Integer, Double> pv = new HashMap<>();

        for (Integer q : QUARTILES) {
            int rank = ranks.get(q);
            double rankAbundance = 0;
            // find the rank when the zeros are taken into account.
            int rankWithoutZeros = rank - numberOfZeros;
            
            // rank must > 0 index to not be the value 0
            if(rankWithoutZeros >= 0){
                result.setRowNumber(absoluteN + rankWithoutZeros);
                if (!(result.get(0) instanceof Number)) {
                    throw new UnexpectedQueryReturnTypeException("createDistribution expects a query to return a result with type or property Number");
                }
                rankAbundance = (double) result.get(0);
            }
                    
            pv.put(q, logBase.calculate(rankAbundance, 1));
        }

        // calculate whiskers
        double iqr = pv.get(UQ) - pv.get(LQ);

        double minRange = pv.get(LQ) - RANGE_FACTOR * iqr;
        double maxRange = pv.get(UQ) + RANGE_FACTOR * iqr;

        // Since we are not using ranks to find the whiskers, we can construct new queries
        // with distinct abundances to retrieve the whiskers
//        result.first();
        Set<Distribution_Outlier> outliers = new HashSet<>();
        boolean withinRange = false;
        double minAbundance = 0;
        double maxAbundance = 0;
        if(0 < minRange)
        {
            outliers.add(new Distribution_Outlier(0));
        }
        else if(0 > maxRange)
        {
            // current abundance is a high outlier
            outliers.add(new Distribution_Outlier(0));
        }
        else
        {
            withinRange = true;
        }
        
        result.beforeFirst();
        while(result.next())
        {
            double currentAbundance = logBase.calculate((double)result.get(0) + 1);
            
            if(currentAbundance < minRange)
            {
                // current abundance is a low outlier
                outliers.add(new Distribution_Outlier(currentAbundance));
            }
            else if(currentAbundance > maxRange)
            {
                // current abundance is a high outlier
                outliers.add(new Distribution_Outlier(currentAbundance));
            }
            else
            {
                // current abundance is between outlying ranges
                
                if(!withinRange) // is this the first abundance within range?
                {
                    withinRange = true;
                    minAbundance = currentAbundance; // set minAbundance
                }
                
                maxAbundance = currentAbundance; // advance max abundance
            }
        }
        
        Distribution dist = new Distribution(logBase, pv.get(LQ), pv.get(MED), pv.get(UQ), minAbundance, maxAbundance);

        dist.setOutliers(outliers);
        // propagate the number sequences used to calculate this distribution
        dist.setNumSeqs(resultLength);
        return (dist);
    }
    
    
    /**
     * INNER CLASSES
     * These inner classes are used to store in RAM windowed distributions as an unfiltered query is scrolled from the database
     * 
     */
    
    private static class DistributionWindowList {

        private final List<DistributionWindow> windows = new ArrayList<>();
        private int totalSize = 0;

        /**
         * Create a list of windows based on the specification of seq count,
         * window size, number of windows
         *
         * @param numberOfSeqs
         * @param numberOfWindows
         * @param windowSize
         */
        public DistributionWindowList(int numberOfWindows, int windowSize, int firstCount) {
            for (int i = firstCount; i < (numberOfWindows * windowSize); i += windowSize) {
                int endRank = (i + windowSize) - 1; // make it start at 1 above the end of the next window for non overlapping...
                windows.add(new DistributionWindow(i, endRank));
                totalSize += endRank - i;
            }
        }

        public List<DistributionWindow> getWindows() {
            return windows;
        }

        public int getTotalSize() {
            return totalSize;
        }
    }

    /**
     * Represents a distribution split in to windows (windows are modelled by
     * DistributionWindow
     */
    private static class WindowedDistribution {

        private final Map<DistributionWindow, List<Double>> distribution = new HashMap<>();
        int currentSize = 0;

        // the set of windows to split this distribution in to
        DistributionWindowList windows;

        public WindowedDistribution(DistributionWindowList windows, int initialSize) {
            this.windows = windows;
           // this.currentSize= initialSize;
        }

        /**
         * Adds a value to the correct window.
         *
         * @param value
         * @return true if added, false if the value can't be added to a window.
         * most commonly this is because all windows are now full.
         */
        public boolean addValue(double value) {
            for (DistributionWindow window : windows.getWindows()) {
                if (currentSize >= window.getStart() && currentSize <= window.getEnd()) {
                    if (!distribution.containsKey(window)) {
                        List<Double> newDist = new ArrayList<>();
                        newDist.add(value);
                        distribution.put(window, newDist);
                    } else {
                        List<Double> dist = distribution.get(window);
                        dist.add(value);
                    }
                    // The value was added
                    currentSize++;
                    return true;
                }
            }
            // the value was not added
            currentSize++;
            return false;
        }

        // Are all windows of this distribution full?
        public boolean isFull() {
            return currentSize == windows.getTotalSize();
        }

        public Map<DistributionWindow, List<Double>> getDistribution() {
            return this.distribution;
        }
        
        public List<DistributionWindow> getWindowList()
        {
            return this.windows.getWindows();
        }
    }

    /**
     * Simple class that models the window of ranked abundances for a particular
     * distribution Contains a start and end
     */
    private static class DistributionWindow {

        final double start;
        final double end;

        public DistributionWindow(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public double getStart() {
            return this.start;
        }

        public double getEnd() {
            return this.end;
        }

        public double getSize() {
            return Math.abs((this.end - this.start) + 1);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (int) (Double.doubleToLongBits(this.start) ^ (Double.doubleToLongBits(this.start) >>> 32));
            hash = 97 * hash + (int) (Double.doubleToLongBits(this.end) ^ (Double.doubleToLongBits(this.end) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DistributionWindow other = (DistributionWindow) obj;
            if (Double.doubleToLongBits(this.start) != Double.doubleToLongBits(other.start)) {
                return false;
            }
            return Double.doubleToLongBits(this.end) == Double.doubleToLongBits(other.end);
        }

        @Override
        public String toString() {
            return  start + ", " + end;
        }
        
        

    }

    /**
     * A set of distributions mapped by descriptions of each window (e.g. norm
     * type, annotation)
     */
    private static class DistributionSet {

        Map<Key, WindowedDistribution> distributions = new HashMap<>();

        // Contains the total size of each distribution across all windows
        Set<Key> nonFullDistributions = new HashSet<>();

        DistributionWindowList windows;
        int startingCount;

        private boolean allDistributionsFull = false;

        public DistributionSet(DistributionWindowList windows, int startingCount) {
            this.windows = windows;
            this.startingCount = startingCount;
        }

        /**
         * Add a value given by a DistributionBean to correct windows
         *
         * @param value a DistributionBean that is probably taken from a HQL
         * Query that returns all defined fields.
         */
        public void addValue(DistributionBean value) {
            Key key = new Key(value);
            if (!this.distributions.containsKey(key) || this.nonFullDistributions.contains(key)) {
                WindowedDistribution dist;
                if (!this.distributions.containsKey(key)) {
                    dist = new WindowedDistribution(windows, startingCount);
                    this.distributions.put(key, dist);
                    nonFullDistributions.add(key);
                } else {
                    dist = this.distributions.get(key);
                }

                boolean added = dist.addValue(value.getExpression());

                // if not added, is it because windows are full?
                if (dist.isFull()) {
                    // remove from active distributions
                    this.nonFullDistributions.remove(key);
                    if (this.nonFullDistributions.isEmpty()) {
                        this.allDistributionsFull = true;
                    }
                }
            }
        }

        public boolean allDistributionsAreFull() {
            return this.allDistributionsFull;
        }

        /**
         * Sorts, calculates, and returns the distributions as Distribution
         * beans
         *
         * @return
         */
        public List<AbundanceBoxAndWhisker_Entity> getBoxplots() {
            //Map<Key, Distribution> boxplots = new HashMap<>();
            List<AbundanceBoxAndWhisker_Entity> abwlist = new ArrayList<>();
            for (Entry<Key, WindowedDistribution> dEntry : distributions.entrySet()) {
                Key key = dEntry.getKey();

                WindowedDistribution windowDistMap = dEntry.getValue();
                for(DistributionWindow window : this.windows.getWindows()){
                    
                    List<Double> values = windowDistMap.getDistribution().get(window);
                    // sort the values smallest -> largest
                    // LAMDA expression for creating an implemented comparator for Double
                    values.sort((a, b) -> a.compareTo(b));

                    // convert list to a ScrollableList
                    ScrollableList scrollList = new ScrollableList(values);

                    // calculate boxplot stats
                    try {
                        Distribution d = createDistribution(scrollList, Logarithm.BASE_2, values.size());
                        AbundanceBoxAndWhisker_Entity abw = new AbundanceBoxAndWhisker_Entity(key.fileID, key.normType, key.annotation, d, window.start, window.end);
                        abwlist.add(abw);
                    } catch (EmptyDistributionException ex) {
                        Logger.getLogger(DistributionService.class.getName()).log(Level.WARNING, null, ex);
                    } catch (UnexpectedQueryReturnTypeException ex) {
                        Logger.getLogger(DistributionService.class.getName()).log(Level.SEVERE, null, ex);
                        throw new RuntimeException("Wrong query given to the DistributionService boxplot method");
                    }
                }
            }
            return abwlist;
        }

        /**
         * Key for the HashMap that describes the Distribution it is keying to.
         */
        static class Key {

            NormalisationType normType;
            String annotation;
            Filename_Entity fileID;

            /**
             * Create a key from a DistributionBean
             *
             * @param bean
             */
            public Key(DistributionBean bean) {
                this.normType = bean.getNormType();
                this.annotation = bean.annotationSet;
                this.fileID = bean.getSample();
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 31 * hash + Objects.hashCode(this.normType);
                hash = 31 * hash + Objects.hashCode(this.annotation);
                hash = 31 * hash + Objects.hashCode(this.fileID);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Key other = (Key) obj;
                if (!Objects.equals(this.annotation, other.annotation)) {
                    return false;
                }
                if (!Objects.equals(this.fileID, other.fileID)) {
                    return false;
                }
                if (this.normType != other.normType) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "Key{" + "normType=" + normType + ", annotation=" + annotation + ", fileID=" + fileID + '}';
            }
            
            
        }
    }
    
}
