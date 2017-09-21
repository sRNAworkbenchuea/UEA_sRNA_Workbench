package uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.ExpressionCiDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SampleDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SampleSequenceDAO;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Distribution;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_CI_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Fold_Change_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Pattern_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Pattern_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Pair_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Series_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Fold_Change_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Pattern_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.FX.DifferentialExpressionSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution.DistributionService;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.EmptyDistributionException;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAelement;
import uk.ac.uea.cmp.srnaworkbench.utils.IteratingStopWatch;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;

/**
 *
 * @author Matthew
 */
@Service("DifferentialExpressionService")
//@Transactional
public class DifferentialExpressionService {
    
    @Autowired
    SampleDAO sampleDao;
    
    @Autowired
    SampleSequenceDAO sampleSeqDao;
    
    @Autowired
    ExpressionCiDAO expressionCiDao;
    
    private NormalisationType normType;
    private double foldChangeCutoff;
    
    private final int RESULT_THRESHOLD = 10000;

    public void calculateConfidenceIntervals(Collection<Filename_Entity> files, NormalisationType normType, AnnotationSetList annotations)
    {
        StopWatch sw = new StopWatch("Confidence Intervals");
        long startTime = System.nanoTime();
        long prevTime = startTime;
        this.normType = normType;
        Session session = sampleDao.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
//        Batcher batch = new Batcher(session,1000);
        
        
        Query uniqueSequenceQuery = session.createQuery("from Unique_Sequences_Entity u "
                + "join u.type a where a.id.type in (:annotations)")
                .setParameterList("annotations", annotations.getAllTypes());
        
//        List list = uniqueSequenceQuery.list();
        ScrollableResults sequences = uniqueSequenceQuery.scroll(ScrollMode.FORWARD_ONLY);
        DatabaseWorkflowModule.getInstance().printLap("CI: Unique_Sequences_Query");
        int i = 0;
        IteratingStopWatch dbsw = new IteratingStopWatch();
        dbsw.start();
        while(sequences.next())
        {
            dbsw.lap("sequence next");
            Unique_Sequences_Entity seq = (Unique_Sequences_Entity) sequences.get(0);
            
            Map<Sample_Entity, List<Expression_Entity>> expressions = new HashMap<>();
            
            dbsw.lap("sequence get");
            for(Filename_Entity file : files){
                Sequence_Entity fileSeq = seq.getSequenceRelationships().get(file.getFilename());
                if(fileSeq != null){
                    Expression_Entity expression = fileSeq.getExpressions().get(normType);
                    Sample_Entity sample = fileSeq.getFilename_sequence().getSample();

                    List<Expression_Entity> sampleExpressions = expressions.get(sample);
                    if(sampleExpressions == null)
                    {
                        sampleExpressions = new ArrayList<>();
                        expressions.put(sample, sampleExpressions);
                    }
                    sampleExpressions.add(expression);
                }

            }
            dbsw.lap("sample - file map building");
            
            for(Entry<Sample_Entity, List<Expression_Entity>> expressionEntry : expressions.entrySet())
            {
               
                Sample_Sequence_Entity thisSampleSeq = new Sample_Sequence_Entity();
                thisSampleSeq.setSample(expressionEntry.getKey());
                thisSampleSeq.setSequence(seq);

                dbsw.lap("new objects");
                Expression_CI_Entity ci = calculateConfidenceIntervals(expressionEntry.getValue());

                ci.setNormType(normType);
                ci.setSequence(thisSampleSeq);
                dbsw.lap("ci calc");      
                
//                sampleSeqDao.create(thisSampleSeq);
//                expressionCiDao.create(ci);
                session.save(ci);
                session.save(thisSampleSeq);
                dbsw.lap("persisting");
                if(i % 50 == 0)
                {
                    session.flush();
                    session.clear();
                }
//                if(i % 500 == 0)
//                {
//                    long thisTime = System.nanoTime();
//                    System.out.println(i + " " + seq.getRNA_Sequence() + " " + StopWatch.formatElapsedTime(thisTime - prevTime, false) + " " + StopWatch.formatElapsedTime(thisTime - startTime, false));
//                    prevTime = System.nanoTime();
//                    dbsw.lap("flush n clear");
//                    dbsw.stop();
//                    dbsw.printTimes();
//                    dbsw.start();
//                }
                i++;

//                batch.batchFlush();
            }

        }
        DatabaseWorkflowModule.getInstance().printLap("CI: Calculating confidence intervals");
//        batch.finish();
        tx.commit();
        session.close();
//        dbsw.stop();
//        dbsw.printTimes();
//        sw.lap("Calculating CI");
//        sw.stop();
//        sw.printTimes();
    }
    
    public void printSampleSequences()
    {
        Session session = sampleDao.getSessionFactory().openSession();
        ScrollableResults seqs = session.createQuery("from Sample_Sequence_Entity").scroll();
        while(seqs.next())
        {
            Sample_Sequence_Entity sampleSeq = (Sample_Sequence_Entity) seqs.get(0);
            System.out.println(sampleSeq.getSequence().getRNA_Sequence() + " " + sampleSeq.getSample().getSampleId());
            for(Expression_CI_Entity ci : sampleSeq.getConfidenceIntervals().values())
            {
                System.out.println(ci.getNormType() + ": " + ci.getMinExpression() + " - " + ci.getAvgExpression() + " - " + ci.getMaxExpression());
            }
        }
        session.close();
    }
    
    private Expression_CI_Entity calculateConfidenceIntervals(List<Expression_Entity> expressions)
    {
        Expression_CI_Entity ci = new Expression_CI_Entity();
        if(expressions.size() < 3)
        {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            
            for(Expression_Entity e : expressions)
            {
                if(e.getExpression() < min)
                {
                    min = e.getExpression();
                }
                if(e.getExpression() > max)
                {
                    max = e.getExpression();
                }
            }
            
            ci.setMinExpression(min);
            ci.setMaxExpression(max);
            ci.setAvgExpression((min+max)/2);
            
        }
        else
        {
            // find the mean and standard deviation over these replicates
            DescriptiveStatistics stats = new DescriptiveStatistics(expressions.size());
            for (Expression_Entity e : expressions)
            {
                stats.addValue(e.getExpression());
            }
            double sd = stats.getStandardDeviation();
            double mean = stats.getMean();
            
            // set the confidence intervals as mean +/- 1 SD
            ci.setMinExpression(mean-sd);
            ci.setMaxExpression(mean+sd);
            ci.setAvgExpression(mean);
        }
        return ci;
    }

    @Transactional
    public void calculateFoldChanges(List<Sample_Pair_Entity> samplePairs, NormalisationType normType, AnnotationSetList annotations)
    {
        IteratingStopWatch sw = new IteratingStopWatch();
        sw.start();
        Session session = sampleDao.getSessionFactory().openSession();
        Batcher batch = new Batcher(session, 50);
        
        // resolve offsets
        Map<Sample_Pair_Entity, Double> offsetMap = new HashMap<>();
        for(Sample_Pair_Entity pair : samplePairs)
        {
            pair = (Sample_Pair_Entity) session.merge(pair);
            DescriptiveStatistics offsetStats = new DescriptiveStatistics();
            for(Filename_Entity file : pair.getReference().getFilenames())
            {
                offsetStats.addValue(file.getOffset());
            }
            for(Filename_Entity file : pair.getObserved().getFilenames())
            {
                offsetStats.addValue(file.getOffset());
            }
            offsetMap.put(pair, offsetStats.getMean());
        }
        DatabaseWorkflowModule.getInstance().printLap("DE: Resolving offsets");
        
        Query uniqueSequenceQuery = session.createQuery("from Unique_Sequences_Entity u "
                    + "join u.type a where a.id.type in (:annotations)")
                 .setParameterList("annotations", annotations.getAllTypes());
//                + "join s.sample_sequences ss "
//                + "join ss.confidenceIntervals ci "
//                + "where ci.normType=:normType order by s.RNA_Sequence")
//                .setParameter("normType", normType);
        
        Sample_Series_Entity thisSeries = new Sample_Series_Entity(samplePairs);
        session.save(thisSeries);
//        thisPattern.setSamplePairs(samplePairs);
//        session.save(thisPattern);
        
        ScrollableResults sequences = uniqueSequenceQuery.scroll();
        
        DatabaseWorkflowModule.getInstance().printLap("DE: Unique Sequences Query");
//        sw.stop();
//        sw.printTimes();
//        sw.start();
        Expression_CI_Entity zeroExpression = new Expression_CI_Entity(normType, 0.0, 0.0, 0.0);
        int i =0;
        while(sequences.next())
        {
            
            Unique_Sequences_Entity seq = (Unique_Sequences_Entity) sequences.get(0);
            sw.lap("sequence next");

            // The fold change pattern for this sequence
            Sequence_Pattern_Entity sequencePattern = new Sequence_Pattern_Entity(seq);
//            sequencePattern.setPattern(thisPattern);
            session.save(sequencePattern);

            String pattern_string = "";
            // find fold change for each pair
            for(Sample_Pair_Entity samplePair : samplePairs)
            {
                // initially, each confidence interval is assumed be derived from a distribution of zero-count replicates
                Expression_CI_Entity reference = zeroExpression;
                Expression_CI_Entity observed = zeroExpression;
                
                // if a sample confidence interval is found in the database, we use that instead of the zero value
                for(Sample_Sequence_Entity thisSampleSeq : seq.getSample_sequences())
                {
                    if(thisSampleSeq.getSample().equals(samplePair.getReference()))
                    {
                        reference = thisSampleSeq.getConfidenceInterval(normType);
                    }
                    else if (thisSampleSeq.getSample().equals(samplePair.getObserved()))
                    {
                        observed = thisSampleSeq.getConfidenceInterval(normType);
                    }
                }
                sw.lap(samplePair.getSamplePair().getPairKey() + "--retrieving CI for both seqs");
                // resolve offsets
                double avgOffset = offsetMap.get(samplePair);

                // Determine direction and fold change
                double refExpression;
                double obsExpression;
                
                FoldChangeDirection direction;


                if (reference.getMaxExpression() < observed.getMinExpression()) {
                            // ref: |-----|
                    // obs:         |-------|
                    direction = FoldChangeDirection.UP;
                    refExpression = reference.getMaxExpression();
                    obsExpression = observed.getMinExpression();
                } else if (reference.getMinExpression() > observed.getMaxExpression()) {
                            // ref:       |-----|
                    // obs: |---|
                    direction = FoldChangeDirection.DOWN;
                    refExpression = reference.getMinExpression();
                    obsExpression = observed.getMaxExpression();
                } else {
                            // ref:       |-----|
                    // obs: |-------|
                    direction = FoldChangeDirection.STRAIGHT;
                     refExpression = reference.getAvgExpression();
                    obsExpression = observed.getAvgExpression();
                }
                double ofc = MAelement.calcM(refExpression, obsExpression, 2, avgOffset);
                double a = MAelement.calcA(refExpression, obsExpression, 2, 1);
                sw.lap(samplePair.getSamplePair().getPairKey() + "--Calcularing fold change");
                
                // Create parent sequence fold change
                Sequence_Fold_Change_Entity seqFc = new Sequence_Fold_Change_Entity();
                seqFc.setUniqueSequence(seq);
                seqFc.setSamplePair(samplePair);
                seqFc.setSequencePattern(sequencePattern);
                session.save(seqFc); // save before adding child
                
                sw.lap(samplePair.getSamplePair().getPairKey() + "--saving sequence fold change");
                i++;
                // new fold change entity child
                Expression_Fold_Change_Entity fc = 
                        new Expression_Fold_Change_Entity(normType, reference.getAvgExpression(), observed.getAvgExpression(), Math.abs(ofc), a, direction);
                fc.setSequence(seqFc);
                session.save(fc);
                sw.lap(samplePair.getSamplePair().getPairKey() + "--saving fold change");
                // update pattern string
                pattern_string += direction.toString();

//                batch.batchFlush();
            }
            
            // Persist the sequence for this pattern (Sequence_Pattern) and link it to the global pattern table (Pattern_Entity)
            Pattern_Entity thisPattern = thisSeries.getPatterns().get(pattern_string);
            if(thisPattern == null)
            {
                thisPattern = new Pattern_Entity(pattern_string);
                thisPattern.setSampleSeries(thisSeries);
                thisSeries.getPatterns().put(pattern_string, thisPattern);
                session.save(thisPattern);
            }
            sequencePattern.setPattern(thisPattern);
//            session.save(sequencePattern);
            
            Expression_Pattern_Entity expressionPattern = new Expression_Pattern_Entity(normType, pattern_string);
            expressionPattern.setSequence(sequencePattern);
            session.save(expressionPattern);
            sw.lap("session save");
            
            //thisPattern.getSequences().add(sequencePattern);
            
            
            batch.batchFlush();
            sw.lap("flush");
            
//            i++;
//            if (i % 500 == 0) {
//                sw.stop();
//                System.out.println(seq.getRNA_Sequence());
//                sw.printTimes();
//                sw.start();
//            }
        }
        System.out.println("Created " + i + "Fold changes");
        DatabaseWorkflowModule.getInstance().printLap("DE: Calculating Fold Changes");
//        sw.lap("All fold change Calculations");
//        sw.printTimes();
//        sw.stop();

        batch.finish();
        session.close();
    }
       
    public void printPatterns()
    {
        Session session = this.sampleDao.getSessionFactory().openSession();
        List<Pattern_Entity> patterns = session.createQuery("from Pattern_Entity").list();
        for(Pattern_Entity pattern : patterns)
        {
            System.out.println("Pattern: " + pattern.getPatternString() + " has " + pattern.getSequences().size() + "sequences");
//            for(Sequence_Pattern_Entity seqPat : pattern.getSequences())
//            {
//                System.out.println(seqPat.getUniqueSequence().getRNA_Sequence());
//                for(Expression_Pattern_Entity exPat : seqPat.getExpressionPatterns().values()){
//                    System.out.println(exPat.getNormType() + ": " + exPat.getPatternStr());
//                }
//                for(Sequence_Fold_Change_Entity seqFc : seqPat.getSequenceFoldChanges())
//                {
//                    System.out.println(seqFc.getSamplePair().getSamplePair().getPairKey());
//                    System.out.println(seqFc.getFoldChanges());
//                }
//            }
        }
        session.close();
    }
    
    /**
     * Write out MA values from the differential expression tables
     */
    public void writeDeMaToJson(Path outputFile, NormalisationType normType) throws IOException
    {
        JsonGenerator jg = DatabaseWorkflowModule.getInstance().getJsonFactory().createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
        
        Session session = this.sampleDao.getSessionFactory().openSession();
        Query query = session.createQuery("select pair.samplePair.pairKey, fc.foldChange, fc.averageAbundance, fc.direction, "
                + "u.RNA_Sequence "
                + "from Sequence_Fold_Change_Entity seq "
                + "join seq.uniqueSequence u "
                + "join seq.samplePair pair join seq.foldChanges fc "
                + "where fc.normType=:normType "
 //               + "group by pair.samplePair.pairKey, fc.foldChange, fc.averageAbundance "
                + "order by u.RNA_Sequence").setParameter("normType", normType);
        
        jg.writeStartArray();
        ScrollableResults results = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
        
        if(results.next())
        {
            String seq = (String) results.get(4);
            
            String currentSeq = seq;
            int pairRows = 1;
            boolean hasD = false;

            FoldChangeDirection d = (FoldChangeDirection) results.get(3);
            if(!d.equals(FoldChangeDirection.STRAIGHT))
                hasD = true;
            
            while(results.next())
            {
                d = (FoldChangeDirection) results.get(3);
                seq = (String) results.get(4);
                if(seq.equals(currentSeq))
                {
                    pairRows++;
                    if(!d.equals(FoldChangeDirection.STRAIGHT))
                        hasD = true;
                }
                else
                {
                    currentSeq = seq;
                    if(hasD){
                        int currentRow = results.getRowNumber();
                        // go back to first row with the current pair
                        results.scroll(-pairRows);
                        while (results.getRowNumber() < currentRow) {
                            double foldChange = (double) results.get(1);
                            double avgAbundance = (double) results.get(2);
                            String pairKey = (String) results.get(0);
                            d = (FoldChangeDirection) results.get(3);
                            jg.writeStartObject();
                            jg.writeStringField("Pair", pairKey);
                            jg.writeNumberField("M", foldChange);
                            jg.writeNumberField("A", avgAbundance);
                            jg.writeStringField("d", d.toString());
                            jg.writeStringField("seq", currentSeq);
                            jg.writeEndObject();
                            results.next();
                        }
                        
                        // At this point we should be back to the current Row.
                        // next iteration gives the row after to check for current sequence
                    }
                    pairRows=1;
                    hasD=false;
                }
            }
        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    /**
     * Generate boxplot information
     * @param samplePairs
     * @param normType 
     */
    public void writeDEdistributions(Path outputPath, NormalisationType normType, double minimumFoldChange) throws IOException
    {
        Session session = this.sampleDao.getSessionFactory().openSession();
        JsonGenerator jg = DatabaseWorkflowModule.getInstance().getJsonFactory().createGenerator(outputPath.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        Logarithm log = Logarithm.BASE_2;
        jg.writeStartObject();
        jg.writeArrayFieldStart("Boxplots");
        List<Sample_Series_Entity> series = session.createQuery("from Sample_Series_Entity").list();
        
        for(Sample_Series_Entity sampleSeries : series)
        {
            // generate sample series based on the least number of series to represent all pair linkages
            List<List<Sample_Entity>> seriesSet = generateSampleSeriesSet(sampleSeries.getSamples());
            
            for (String pattern : sampleSeries.getPatterns().keySet()) {
                // To create boxplots, we must only calculate one boxplot for each sample regardless
                // if it appears in more than one pair.
                Set<Sample_Entity> allSamples = new LinkedHashSet<>();
                
                for(Sample_Pair_Entity pair : sampleSeries.getSamples())
                {
                    allSamples.add(pair.getReference());
                    allSamples.add(pair.getObserved());
                }
                
                Long totalSeqs = (long) session.createQuery("select count(u) from Unique_Sequences_Entity u join u.pattern p join p.pattern pat where pat.patternString=:pattern")
                        .setParameter("pattern",pattern).uniqueResult();
                for(Sample_Entity sample : allSamples){
                    StopWatch sw = new StopWatch();
                    sw.start();
                    Query query2 = session.createQuery(
                              "select ci.avgExpression from Unique_Sequences_Entity u "
                            + "join u.sample_sequences seq join seq.confidenceIntervals ci with ci.normType=:normType "
                            + " where u.RNA_Sequence in (select fcs.uniqueSequence.RNA_Sequence from Sequence_Fold_Change_Entity fcs "
                            + "join fcs.foldChanges fc with fc.normType=:normType and fc.foldChange > :minFC where fcs.sequence_pattern.pattern.patternString=:pattern) "
                                      + "and seq.sample.sampleId=:sample "
                                      + "order by ci.avgExpression asc")
                            .setParameter("sample", sample.getSampleId()).setParameter("pattern", pattern).setParameter("minFC", minimumFoldChange).setParameter("normType", normType);
                    
                    //List list = query2.list();
                    System.out.println(sw.printLap("query generation"));
                    sw.stop();

//                    Query query = session.createQuery("select ci.avgExpression from Sequence_Pattern_Entity spat "
//                            + "join spat.pattern pattern "
//                            + "join spat.sequenceFoldChanges sfc "
//                            + "join sfc.foldChanges with sfc.normType=:normType and sfc.foldChanges > 1 "
//                            + "join spat.uniqueSequence u "
//                            + "join u.sample_sequences sampleSeq "
//                            + "join sampleSeq.sample samp "
//                            + "join sampleSeq.confidenceIntervals ci "
//                            + "where pattern.patternString=:pattern and ci.normType=:normType and samp.sampleId=:sampleId "
//                            + "order by ci.avgExpression")
//                            .setParameter("pattern", pattern).setParameter("normType", normType).setParameter("sampleId", sample.getSampleId());
                    
                    try {
                        Distribution dist = DistributionService.createDistribution(query2, log, totalSeqs.intValue());
                        System.out.println(pattern + " " + sample.getSampleId() + " " + dist.toString());
                        
                        for(List<Sample_Entity> thisSeries : seriesSet){
                            if(thisSeries.contains(sample)){
                                jg.writeStartObject();
                                jg.writeStringField("Pattern", pattern);
                                jg.writeArrayFieldStart("Series");
                                    for(Sample_Entity sample_e : thisSeries)
                                        jg.writeString(sample_e.getSampleId());
                                jg.writeEndArray();
                                jg.writeStringField("Sample", sample.getSampleId());
                                DistributionService._jsonWriteBoxplot(jg, dist);
                                jg.writeEndObject();
                            }
                        }
                    } catch (EmptyDistributionException ex) {
                        LOGGER.log(Level.WARNING, "DE Distribution for sample " + sample.getSampleId() + " in pattern " + pattern + " is empty and will not be written out.");
                    }
                    
                }
                
            }
            jg.writeEndArray();
            jg.writeArrayFieldStart("Lines");
            DatabaseWorkflowModule.getInstance().printLap("Write all DE boxplots");
            for(List<Sample_Entity> thisSeries : seriesSet){

                Query query = session.createQuery("select ci.avgExpression, u.RNA_Sequence, pattern.patternString, u.type.id.type, sampleSeq.sample.sampleId from Sequence_Pattern_Entity spat "
                        + "join spat.pattern pattern "
                        + "join spat.uniqueSequence u "
                        + "join u.sample_sequences sampleSeq "
                        + "join sampleSeq.confidenceIntervals ci "
                        + "where u.RNA_Sequence in (select fcs.uniqueSequence.RNA_Sequence from Sequence_Fold_Change_Entity fcs "
                            + "join fcs.foldChanges fc with fc.normType=:normType and fc.foldChange > :minFC) and ci.normType=:normType and sampleSeq.sample in (:samples)"
                        + "order by pattern.patternString, u.RNA_Sequence")
                        .setParameter("normType", normType).setParameterList("samples", thisSeries).setParameter("minFC", minimumFoldChange);
                
                // Keep track of the samples used and the ones that need 0 inserted
                HashMap<String, Double> sampleIDs = new HashMap<>();
                for(Sample_Entity sample_e : thisSeries){
                    sampleIDs.put(sample_e.getSampleId(), 0.0);
                }
                
                ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
                DatabaseWorkflowModule.getInstance().printLap("query for DE lines");

                
                if(results.next())
                {
                    HashMap<String,Double> trackedSamples = new HashMap<>(sampleIDs);
                    double fc = results.getDouble(0);
                    String seq = results.getString(1);
                    String pattern = results.getString(2);
                    String type = results.getString(3);
                    String sample = results.getString(4);
                    trackedSamples.put(sample, fc);
                    while (results.next()) {
                        fc = results.getDouble(0);
                        sample = results.getString(4);
                        String newType = results.getString(3);
                        String newSeq = results.getString(1);
                        String newPat = results.getString(2);
                        
                        if (!newSeq.equals(seq) || !newPat.equals(pattern)) {
                            jg.writeStartObject();
                            jg.writeStringField("Pattern", pattern);
                            jg.writeStringField("Seq", seq);
                            jg.writeStringField("Annotation", type);
                            jg.writeArrayFieldStart("SampleSeries");
                            for (Sample_Entity thisSample : thisSeries) {
                                jg.writeString(thisSample.getSampleId());
                            }
                            jg.writeEndArray();
                            pattern = newPat;
                            seq = newSeq;
                            type = newType;
                            jg.writeArrayFieldStart("Values");
                            for(Sample_Entity sample_e : thisSeries){
                                jg.writeStartObject();
                                jg.writeStringField("Sample", sample_e.getSampleId());
                                double thisExp = trackedSamples.get(sample_e.getSampleId());
                                double logexp = log.calculate(thisExp, 1);
                                jg.writeNumberField("Exp", logexp);
                                jg.writeEndObject();
                            }
                            jg.writeEndArray();
                            jg.writeEndObject();
                            trackedSamples = new HashMap<>(sampleIDs);
                        }
                        trackedSamples.put(sample, fc);
                    }

                }    
                
            }
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.close();
        session.close();
        DatabaseWorkflowModule.getInstance().printLap("write DE lines");
    }
    
    private List<List<Sample_Entity>> generateSampleSeriesSet(List<Sample_Pair_Entity> pairs)
    {
        // Algorithm to generate the simplest set of linked samples from the sample pairs
        List<List<Sample_Entity>> sampleSeriesSet = new ArrayList<>();

        // copy the list so that we can make modifications to it.
        List<Sample_Pair_Entity> samplePairs = new ArrayList<>(pairs);
        for (int i = 0; i < samplePairs.size(); i++) {
            Sample_Pair_Entity thisPair = samplePairs.get(i);

            // set to true if ref has found a sample to link before it
            boolean beforeRef = false;
            // set to true if obs has found a sample to link after it
            boolean afterObs = false;

            Sample_Entity ref = thisPair.getReference();
            Sample_Entity obs = thisPair.getObserved();
            // start new series
            List<Sample_Entity> thisSeries = new LinkedList<>();
            thisSeries.add(ref);
            thisSeries.add(obs);
            // add to the set
            sampleSeriesSet.add(thisSeries);

            boolean changed;
                // We now check to see if there are other pairs that we can join
            // to the current series.
            do {
                changed = false;
                for (int j = i + 1; j < samplePairs.size(); j++) {
                    Sample_Pair_Entity otherPair = samplePairs.get(j);
                    Sample_Entity otherRef = otherPair.getReference();
                    Sample_Entity otherObs = otherPair.getObserved();

                    // is ref found in this obs?
                    if (!beforeRef && ref.equals(otherObs)) {
                            // case: otherRef --> otherObs==ref --> obs
                        // add to start of series
                        thisSeries.add(0, otherRef);

                        // the new far left ref is otherRef
                        ref = otherRef;

                        // prevent this pair form being used again
                        samplePairs.remove(j);
                        changed = true;
                        //                        beforeRef = true;
                    } else if (!afterObs && obs.equals(otherRef)) {
                            //case: ref --> obs==otherRef --> otherObs
                        // add to end of series
                        thisSeries.add(otherObs);

                        // the new far right obs is otherObs
                        obs = otherObs;

                        // prevent this pair form being used again
                        samplePairs.remove(j);
                        changed = true;
                        //                        afterObs = true;
                    }
                }
            } while (changed); // if something has changed, go again
        }
        return sampleSeriesSet;
    }
    
    public void writeDEtoCSV(Path outputFile, NormalisationType normType, double minimumFoldChange) throws IOException
    {
        String sep = ",";
        BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
        writer.write("sequence,type,reference,observed,fc,direction\n");
        Session session = this.sampleDao.getSessionFactory().openSession();
        Query query = session.createQuery("from Sequence_Fold_Change_Entity seq join seq.foldChanges fc "
                + "join seq.samplePair pair "
                + "where fc.normType=:normType and fc.foldChange>:minimumFoldChange "
                + "order by pair.samplePair.pairKey, fc.foldChange desc")
                .setParameter("normType", normType).setParameter("minimumFoldChange", minimumFoldChange);
        
        ScrollableResults results = query.scroll();
        while(results.next())
        {
            Sequence_Fold_Change_Entity seq = (Sequence_Fold_Change_Entity) results.get(0);
            writer.write(seq.getUniqueSequence().getRNA_Sequence() 
                    + sep + seq.getUniqueSequence().getConsensus_annotation_type().getId().getType()
                    + sep + seq.getSamplePair().getReference().getSampleId() 
                    + sep + seq.getSamplePair().getObserved().getSampleId() 
                    + sep + seq.getFoldChanges().get(normType).getFoldChange()
                    + sep + seq.getFoldChanges().get(normType).getDirection() + "\n");
        }
        session.close();
        writer.close();
    }
    
    public static FoldChangeDirection getDirection(Expression_CI_Entity reference, Expression_CI_Entity observed)
    {
        if(reference.getMaxExpression() < observed.getMinExpression())
        {
            return FoldChangeDirection.UP;
        }
        if(reference.getMinExpression() > observed.getMaxExpression())
        {
            return FoldChangeDirection.DOWN;
        }
        return FoldChangeDirection.STRAIGHT;
    }

    public void populateTable(double foldChangeCutoff, DifferentialExpressionSceneController controller)
    {
        this.foldChangeCutoff = foldChangeCutoff;
        
        Session session = this.sampleDao.getSessionFactory().openSession();
        
        //count results:
        
        String sql_count = "SELECT count (*) from"
                + "(SELECT sf.UNIQUE_SEQUENCE, sf.REFERENCESAMPLE, sf.OBSERVEDSAMPLE, e.FOLD_CHANGE, e.DIRECTION, sp.PATTERN, u.TYPE, e.AVERAGE_ABUNDANCE "
                + "FROM SEQUENCE_FOLD_CHANGES sf "
                + "JOIN EXPRESSIONFOLDCHANGES e ON sf.SEQUENCE_FOLD_CHANGE_ID=e.FOLD_CHANGE_SEQUENCE_FK "
                + "JOIN UNIQUE_SEQUENCES u ON sf.UNIQUE_SEQUENCE=u.RNA_SEQUENCE "
                + "JOIN SEQUENCEPATTERNS sp ON sp.ID=sf.SEQUENCE_PATTERN_FK "
                + "WHERE e.NORMTYPE=:normType AND e.FOLD_CHANGE > :fcThreshold)";
        
        int count_results = StringUtils.safeIntegerParse(session.createSQLQuery(sql_count)
                .setParameter("normType", normType.getDatabaseReference())
                .setParameter("fcThreshold", foldChangeCutoff).list().get(0).toString(),0);
                
        controller.setResultSize(count_results, RESULT_THRESHOLD);
        
        if (count_results <= RESULT_THRESHOLD)//just load them all because there isnt many
        {

            String sql = "SELECT sf.UNIQUE_SEQUENCE, sf.REFERENCESAMPLE, sf.OBSERVEDSAMPLE, e.FOLD_CHANGE, e.DIRECTION, sp.PATTERN, u.TYPE, e.AVERAGE_ABUNDANCE "
                    + "FROM SEQUENCE_FOLD_CHANGES sf "
                    + "JOIN EXPRESSIONFOLDCHANGES e ON sf.SEQUENCE_FOLD_CHANGE_ID=e.FOLD_CHANGE_SEQUENCE_FK "
                    + "JOIN UNIQUE_SEQUENCES u ON sf.UNIQUE_SEQUENCE=u.RNA_SEQUENCE "
                    + "JOIN SEQUENCEPATTERNS sp ON sp.ID=sf.SEQUENCE_PATTERN_FK "
                    + "WHERE e.NORMTYPE=:normType AND e.FOLD_CHANGE > :fcThreshold";
            /*
        
             String sql = "SELECT sf.UNIQUE_SEQUENCE, sf.REFERENCESAMPLE, sf.OBSERVEDSAMPLE, e.FOLD_CHANGE, e.DIRECTION, sp.PATTERN, u.TYPE, e.AVERAGE_ABUNDANCE,g.VALUE "
             + "FROM SEQUENCE_FOLD_CHANGES sf "
             + "JOIN EXPRESSIONFOLDCHANGES e ON sf.SEQUENCE_FOLD_CHANGE_ID=e.FOLD_CHANGE_SEQUENCE_FK "
             + "JOIN UNIQUE_SEQUENCES u ON sf.UNIQUE_SEQUENCE=u.RNA_SEQUENCE "
             + "JOIN SEQUENCEPATTERNS sp ON sp.ID=sf.SEQUENCE_PATTERN_FK "
             + "JOIN (SELECT ATTRID,VALUE,REFERENCE FROM GFF JOIN GFF_ATTRIBUTE_ENTITY ON GFF.ID = GFF_ATTRIBUTE_ENTITY.ID) g " 
             + "ON u.type=g.reference "
             + "WHERE e.NORMTYPE=:normType AND e.FOLD_CHANGE > :fcThreshold";
             */

            ScrollableResults results = session.createSQLQuery(sql)
                    .setParameter("normType", normType.getDatabaseReference())
                    .setParameter("fcThreshold", foldChangeCutoff)
                    .scroll(ScrollMode.SCROLL_INSENSITIVE);

            while (results.next())
            {
                Object[] get = results.get();
                //System.out.println("");
                controller.populateTable(get);
            }

            controller.showResults();
        }
        else
        {
            controller.enableChunkSelect();
            
            //show first RESULT_THRESHOLD rows
            loadResultSet(0, RESULT_THRESHOLD, controller);
            
            
        }
        
        session.flush();
        session.close();
    }

    public void exportToCSV(File file)
    {
        
        Session session = this.sampleDao.getSessionFactory().openSession();
        
        String sql = "SELECT sf.UNIQUE_SEQUENCE, sf.REFERENCESAMPLE, sf.OBSERVEDSAMPLE, e.FOLD_CHANGE, e.DIRECTION, sp.PATTERN, u.TYPE, e.AVERAGE_ABUNDANCE "
                + "FROM SEQUENCE_FOLD_CHANGES sf "
                + "JOIN EXPRESSIONFOLDCHANGES e ON sf.SEQUENCE_FOLD_CHANGE_ID=e.FOLD_CHANGE_SEQUENCE_FK "
                + "JOIN UNIQUE_SEQUENCES u ON sf.UNIQUE_SEQUENCE=u.RNA_SEQUENCE "
                + "JOIN SEQUENCEPATTERNS sp ON sp.ID=sf.SEQUENCE_PATTERN_FK "
                + "WHERE e.NORMTYPE=:normType AND e.FOLD_CHANGE > :fcThreshold";
        ScrollableResults results = session.createSQLQuery(sql)
                .setParameter("normType", normType.getDatabaseReference())
                .setParameter("fcThreshold", foldChangeCutoff)
                .scroll(ScrollMode.SCROLL_INSENSITIVE);
        
        String header = "";
        try
        {
            Files.write(file.toPath(), header.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

            while (results.next())
            {
                Object[] get = results.get();
                String toWrite = get[0] + ","
                        + get[0].toString().length() + ","
                        + get[6] + ","
                        + get[6/*change this to accession when query is ready*/] + ","
                        + get[3] + ","
                        + get[4] + ","
                        + get[1] + ","
                        + get[2] + ","
                        + get[7] + ","
                        + get[5] + LINE_SEPARATOR;

                Files.write(file.toPath(), toWrite.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

                //System.out.println("");
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(DifferentialExpressionService.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        session.flush();
        session.close();
    }

    /**
    * @deprecated  As of release 4.3.2, replaced by {@link #queryAnnotationSet(String sequence, DifferentialExpressionSceneController controller)}
    */
    @Deprecated
    public void queryAnnotations(String sequence, DifferentialExpressionSceneController controller)
    {
        Session session = this.sampleDao.getSessionFactory().openSession();
        
        String sql = "SELECT gffa.value, als.SEQ_START, als.SEQ_END, als.CHROM,gffa.reference FROM SEQUENCE_GFF sgf "
                + "JOIN ALIGNED_SEQUENCES als "
                + "ON sgf.SEQ_START=als.SEQ_START AND sgf.SEQ_END= als.SEQ_END AND sgf.CHROM=als.CHROM AND sgf.STRAND=als.STRAND "
                + "JOIN "
                + "(SELECT VALUE,REFERENCE,GFF.ID, GFF_ATTRIBUTE_ENTITY.key  FROM GFF JOIN GFF_ATTRIBUTE_ENTITY ON GFF.ID = GFF_ATTRIBUTE_ENTITY.ATTRID  "
                + "WHERE GFF_ATTRIBUTE_ENTITY.key ='Name' "
                + "GROUP BY REFERENCE, VALUE, GFF.ID,GFF_ATTRIBUTE_ENTITY.key)  gffa "
                + "ON gffa.ID=sgf.GFF_ID "
                + "WHERE als.RNA_SEQUENCE=:selectedSequence";

        List<Object[]> results = session.createSQLQuery(sql)
                .setParameter("selectedSequence", sequence)
                .list();

        controller.populateAnnotationResults(results);

        session.flush();
        session.close();
    }
    
    public void queryAnnotationSet(String sequence, DifferentialExpressionSceneController controller)
    {
        Session session = this.sampleDao.getSessionFactory().openSession();
        
        List<Aligned_Sequences_Entity> als = session.createCriteria(Aligned_Sequences_Entity.class).add(Restrictions.eq("rna_sequence", sequence)).list();

        ArrayList<String[]> annotations = new ArrayList<>();
        
        for(Aligned_Sequences_Entity a_e : als)
        {
            
            for(GFF_Entity g_e : a_e.getAnnotations())
            {
                String[] dataForAnnotation = new String[5];
                dataForAnnotation[0] = g_e.getRecord().getAttribute("Name");
                dataForAnnotation[1] = Integer.toString(g_e.getId().getStart());
                dataForAnnotation[2] = Integer.toString(g_e.getId().getEnd());
                dataForAnnotation[3] = g_e.getId().getChrom();
                dataForAnnotation[4] = g_e.getRecord().getType();
                annotations.add(dataForAnnotation);


                //dataForAnnotation
                //annotation += "Annotation (" + annotCount +"): ";
//                if(g_e.getId().getStart()+1 == s.getStart() && g_e.getId().getEnd()+1 == s.getEnd())
//                {
//                    annotation += "Annotation is: ";
//                    annotation += g_e.getRecord().getAttribute("Name");
//                    annotation += " - ";
//
//                }
//                else
//                {
//                    annotation += "Seqeunce is the same as: ";
//                    annotation += g_e.getRecord().getAttribute("Name");
//                    annotation += " starting at: " +g_e.getId().getStart()+1 + " ending at: " + g_e.getId().getEnd()+1;
//                    annotation += " - ";
//                }
//                annotCount++;
//                annotation += " || ";

            }
        }
        
        
        controller.populateAnnotationResults(annotations);
        
        session.flush();
        session.close();
    }

    public void loadResultSet(int currentWindowStart, int currentWindowEnd, DifferentialExpressionSceneController controller)
    {
        
        Session session = this.sampleDao.getSessionFactory().openSession();

    

        String sql = "SELECT sf.UNIQUE_SEQUENCE, sf.REFERENCESAMPLE, sf.OBSERVEDSAMPLE, e.FOLD_CHANGE, e.DIRECTION, sp.PATTERN, u.TYPE, e.AVERAGE_ABUNDANCE "
                + "FROM SEQUENCE_FOLD_CHANGES sf "
                + "JOIN EXPRESSIONFOLDCHANGES e ON sf.SEQUENCE_FOLD_CHANGE_ID=e.FOLD_CHANGE_SEQUENCE_FK "
                + "JOIN UNIQUE_SEQUENCES u ON sf.UNIQUE_SEQUENCE=u.RNA_SEQUENCE "
                + "JOIN SEQUENCEPATTERNS sp ON sp.ID=sf.SEQUENCE_PATTERN_FK "
                + "WHERE e.NORMTYPE=:normType AND e.FOLD_CHANGE > :fcThreshold "
                + "LIMIT " + currentWindowStart + "," + (currentWindowEnd-currentWindowStart);


        ScrollableResults results = session.createSQLQuery(sql)
                .setParameter("normType", normType.getDatabaseReference())
                .setParameter("fcThreshold", foldChangeCutoff)
                .scroll(ScrollMode.SCROLL_INSENSITIVE);

        while (results.next())
        {
            Object[] get = results.get();
            //System.out.println("");
            controller.populateTable(get);
        }

        controller.showResults();

        session.flush();
        session.close();
    }


}
