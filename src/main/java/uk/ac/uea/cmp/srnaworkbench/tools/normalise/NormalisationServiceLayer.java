package uk.ac.uea.cmp.srnaworkbench.tools.normalise;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.stat.Statistics;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.data.count.CountMatrix;
import uk.ac.uea.cmp.srnaworkbench.database.Batcher;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.FilenameAbundanceDAO;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NoSuchExpressionValueException;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.utils.math.Logarithm;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAService;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAService.MASeries;
import uk.ac.uea.cmp.srnaworkbench.utils.IteratingStopWatch;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;

/**
 *
 * @author w0445959
 * The "Runnable" process for normalisation on the database
 * This will be called from either the GUI or the CLI
 * Each normalisation procedure should be in its own process
 * 
 */
@Service( "NormalisationService" )
@Transactional
public class NormalisationServiceLayer
{
    @Autowired
    private SequenceDAOImpl seqDao;
    
    @Autowired
    private FilenameAbundanceDAO filenameAbundanceDao;
    
    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDao;
    
    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;
    
    
    
    public static final String WHERE_ALIGNED = "WHERE EXISTS ( SELECT 1 FROM ALIGNED_SEQUENCES WHERE SEQUENCES.RNA_SEQUENCE=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1 ) ";
    //public static final String SQL_COUNT_ALIGNED = "SELECT File_Name, COUNT(abundance) FROM SEQUENCES GROUP BY File_Name";
    public static final String SQL_UQ
            = "SELECT (SELECT MAX(abundance) FROM "
            + "(SELECT TOP :numseqs abundance FROM SEQUENCES " + WHERE_ALIGNED + " AND File_Name = :file ORDER BY ABUNDANCE) "
            + " + "
            + "SELECT MIN(abundance) FROM "
            + "(SELECT TOP :numseqs2 abundance FROM SEQUENCES " + WHERE_ALIGNED + " AND File_Name = :file ORDER BY ABUNDANCE DESC)"
            + ") / 2.0";
    
    static final String SQL_SORTED_COUNTS = 
            "SELECT S.Abundance FROM  SEQUENCES S "
                    + "WHERE EXISTS (SELECT 1 FROM Unique_Sequences U WHERE U.Type in (:atype) AND S.RNA_Sequence=U.RNA_Sequence)  "
                    + "AND S.File_ID=:fileID ORDER BY Abundance DESC";
    
    static final String expressionsForType =             
              "     SELECT s.Abundance "
            + "      FROM SEQUENCES AS s"
            + "      INNER JOIN FILENAMES f "
            + "         ON s.FILE_NAME=f.FILE_NAME "
            + "         AND f.fileID=:file "
            + "      INNER JOIN UNIQUE_SEQUENCES u "
            + "         ON u.RNA_SEQUENCE=s.RNA_SEQUENCE "
            + "      INNER JOIN ANNOTATION_TYPES a "
            + "         ON u.REFERENCE=a.REFERENCE_SET_NAME "
            + "         AND u.TYPE=a.ANNOTATION_TYPE "
            + "         AND a.ANNOTATION_TYPE IN (:annoType) ";
    
    public static final String SQL_UQ2
            = "SELECT ("
            + "SELECT MAX(Abundance) FROM ("
            + "     SELECT TOP :numseqs Abundance FROM "
            + "     (" + expressionsForType + " ORDER BY Abundance) "
            + ")"
            + " + "
            + "SELECT MIN(Abundance) FROM ("
            + "     SELECT TOP :numseqs2 Abundance FROM "
            + "     (" + expressionsForType + " ORDER BY Abundance DESC) "
            + ")"
            + ") / 2.0";
    
    public static final String SQL_SORT_SEQS_IN_COLUMN = "SELECT SEQUENCES.File_Name, SEQUENCES.abundance "
            //"SELECT * "
            + "FROM SEQUENCES "
            + "WHERE SEQUENCES.File_Name = :filename AND "
            + "exists ( select 1 from ALIGNED_SEQUENCES where SEQUENCES.RNA_SEQUENCE=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1 ) "
            + "ORDER BY SEQUENCES.abundance DESC";
    
    public static final String SQL_GET_LIB_TOTAL = "SELECT Total_Genome_Match_Abundance FROM FILENAMES WHERE File_Name = :file";
    
    public static final String SQL_ABUNDANCES_WITH_TYPE_AND_FILE = "SELECT Sequence_Id, Abundance, File_ID FROM Sequences S "
                    + "WHERE EXISTS "
                    + "(SELECT 1 FROM Unique_Sequences U "
                    + "WHERE S.RNA_Sequence=U.RNA_Sequence "
                    + "AND U.Type IN (:types) "
                    + "LIMIT 1) "
                    + "AND S.File_ID in (:filenames)";
    
    public long writeExpressionTableToCSV(BufferedWriter out, long currentId) throws IOException{
        StopWatch sw = new StopWatch("Expression Write");
        sw.start();
        StatelessSession session = seqDao.getSessionFactory().openStatelessSession();
        ScrollableResults rows = session.createSQLQuery("SELECT Expression_Id, Sequence_Id, Normalisation_Type, Expression FROM Sequence_Expressions")
                .setFetchSize(10000) // default is 100. 10000 rows should be holdable in memory
                .scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Exp query");
        long id = 0;
        while(rows.next())
        {
//            Expression_Entity exp = (Expression_Entity) rows.get(0);
            String line = rows.get(0) + "," + rows.get(1) + "," + rows.get(2) + "," + rows.get(3) + LINE_SEPARATOR;
            out.write(line);
            id++;
        }
        sw.lap("Exp writing");
        session.close();
        sw.stop();
        sw.printTimes();
        return id;
    }
    
    public long writeExpressionLine(BufferedWriter out, long currentId, long seqId, NormalisationType normType, double expression) throws IOException
    {
        currentId++;
        String line = currentId + "," + seqId + "," + normType.ordinal() + "," + expression + LINE_SEPARATOR;
        out.write(line);
        return currentId;
    }
    
    public void readExpressionTable(Path expressionCSV){
        
        Session session = seqDao.getSessionFactory().openSession();
        //Transaction tx = session.beginTransaction();
        session.doWork((Connection connection) -> {
            try (Statement s = connection.createStatement())
            {
                System.out.println("Dropping table");
                s.execute("DROP TABLE Sequence_Expressions");
                
                session.clear();
                System.out.println("CSVREAD");
                s.execute("CREATE TABLE Sequence_Expressions ("
                        + "Expression_Id LONG, "
                        + "Sequence_Id LONG, "
                        + "Normalisation_Type INT, "
                        + "Expression DOUBLE, "
                        + "PRIMARY KEY (Expression_Id)) "
                        + "AS SELECT * FROM CSVREAD('" + expressionCSV + "', 'Expression_Id,Sequence_Id,Normalisation_Type,Expression', 'UTF-8', ',')");
                s.execute("CREATE INDEX seq_exp_seq_id_idx ON Sequence_Expressions (Sequence_Id)");
                System.out.println("Done");
            }
            connection.close();
        });
        session.flush();
        session.clear();
        session.close();
        Statistics stats = this.seqDao.getSessionFactory().getStatistics();
        System.out.println("Open: " + stats.getSessionOpenCount() + " Closed: " + stats.getSessionCloseCount());
    }
    
    /**
     * The normalisation service currently only allows one normalisation run of each type
     * to be persisted to the database at any one time. This means that if a second normalisation
     * of the same type is attempted, the previous normalisation attempt is automatically removed via this method.
     * 
     * @param normType the normalisation type to be removed.
     */
    public boolean removeNormalisation(NormalisationType normType)
    {
        Session session = seqDao.getSessionFactory().openSession();
        
        // check that there is a persisted set of normalised values to remove
        Expression_Entity e = (Expression_Entity) session.createQuery("from Expression_Entity where normType=:norm").setMaxResults(1).setParameter("norm", normType).uniqueResult();
        if(e != null){
            // Warn that these values are being overwritten and then remove values
            LOGGER.log(Level.WARNING, "Normalisation {0} was already run. The previous run will be overwritten.", normType);
            session.createQuery("delete from Expression_Entity where normType=:norm").setParameter("norm", normType).executeUpdate();
            session.close();
            return true;
        }
        session.close();
        return false;
        
    }
    
    private static double getMeanLibSizes(Map<String, Double> libSizes)
    {
        double sum = 0;
        for(double size : libSizes.values())
            sum += size;
        return sum/libSizes.size();
    }
    
    /**
     * Normalise using total count normalisation. Each sequence is divided by the total library
     * size and then multiplied by an upscale constant found by finding the average of all
     * library sizes. This way, all library sizes have the same "depth" or count.
     * @param filenames The filenames of the samples for normalisation
     * @param annotations the set of annotations to be normalised
     * @param weightByHits NOT RECOMMENDED. Please avoid this parameter
     */
    public long performTotalCountNormalisation(Collection<String> filenames, Map<String, Double> libSizes, AnnotationSet annotations, boolean weightByHits, BufferedWriter out, long currentId) throws NoSuchExpressionValueException, IOException
    {
        StopWatch sw = new StopWatch("Per Total");
        final NormalisationType THIS_NORM = NormalisationType.TOTAL_COUNT;
        if(weightByHits)
            LOGGER.log(Level.WARNING, "weight by hits is no longer used");

//        double upscale = this.filenameAbundanceDao.getMeanTotalAbundances();
        double upscale = getMeanLibSizes(libSizes);
        //create a session for the database
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
        
        //build select all query from the aligned table
//        Criteria criteria = session.createCriteria(Sequence_Entity.class)
//                .createAlias("unique_sequence", "u")
//                .createAlias("u.type", "type")
//                .add(Restrictions.in("filename", filenames))
//                .add(Restrictions.in("type.id.type", annotations.getTypes()));
        
        Query seqQuery = session.createSQLQuery(SQL_ABUNDANCES_WITH_TYPE_AND_FILE)
                .setParameterList("filenames", filenames)
                .setParameterList("types", annotations.getTypes());

        //a scrollable result set iterates through the data lazy loading each one
        ScrollableResults seqs = seqQuery.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query sequences");
        while (seqs.next())
        {
//            Sequence_Entity s_e = (Sequence_Entity) seqs.get(0);
            long seqId = ((BigInteger) seqs.get(0)).longValue();
            int expression = (int) seqs.get(1);
            String fileID = (String) seqs.get(2);
            
            // calculate total count normalisation
            double totalFileAbundance = libSizes.get(fileID);
            double normalisedAbundance = (expression / totalFileAbundance) * upscale;
            currentId = writeExpressionLine(out, currentId, seqId, THIS_NORM, normalisedAbundance);
//            s_e.addExpression(THIS_NORM, normalisedAbundance);
            //in order to execute all waiting SQL statements and free resources we flush every few iterations
            b.batchClear();
        }
        sw.lap("Inserting normalised values");
        
        // Add normalised filename totals to database
        List<Filename_Entity> files = session.getNamedQuery("@HQL_GET_SELECTED_FILES")
                .setParameterList("filenames", filenames).list();
        for (Filename_Entity file : files) {
            // since this is normalised pt per "upscale constant", the totals will be UPSCALE_CONSTANT
            file.addTotalNormalisedAbundance(THIS_NORM, upscale);
            b.batchFlush();
        }
        
        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
    
    /**
     * For each sample,
     *  sums up the abundances up to the 75th percentile and uses this sum
     *  as the normalisation factor in the same way that total sum
     * is used in PT
     */
    public long performUpperQuartileNormalisation(Collection<String> fileIDs, AnnotationSet annotationTypes, BufferedWriter out, long currentId) throws NoSuchExpressionValueException, IOException
    {
        StopWatch sw = new StopWatch("Upper Quartile");
        final NormalisationType THIS_NORM = NormalisationType.UPPER_QUARTILE;
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        
        double upscale = this.filenameAbundanceDao.getMeanTotalAbundances();
        sw.lap("retrieving mean total abundances");
        
        // stores factors
        Map<String, Integer> uqs = new HashMap<>();
        //ScrollableResults numSeqs = session.createSQLQuery(SQL_COUNT_ALIGNED).scroll(ScrollMode.FORWARD_ONLY);
        
        // FInd number of unique sequences per filename for annotation
        ScrollableResults sequence_count = session.createCriteria(Sequence_Entity.class)
                .createAlias("unique_sequence", "u")
                .createAlias("u.type", "type")
                .createAlias("filename_sequence", "f")
                .add(Restrictions.in("f.fileID", fileIDs))
                .add(Restrictions.in("type.id.type", annotationTypes.getTypes()))
                .setProjection(Projections.projectionList()
                        .add(Projections.count("RNA_Sequence"))
                        .add(Projections.groupProperty("f.fileID")))
                .scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Counting unique sequences");
        while(sequence_count.next())
        {
            String filename = (String) sequence_count.get(1);
            Long count = (Long) sequence_count.get(0);
            int uqPercentile = (int) (count * 0.75);
            
            ScrollableResults uqCounts = session.createQuery("select s.abundance from Sequence_Entity s "
                    + "join s.unique_sequence u "
                    + "join u.type t join s.filename_sequence f "
                    + "where t.id.type in (:types) "
                    + "and f.fileID=:filename order by s.abundance asc")
                    .setParameterList("types", annotationTypes.getTypes())
                    .setParameter("filename", filename).scroll();
            
            uqCounts.scroll(uqPercentile);
            int uqAbundance = (int) uqCounts.get(0);
            System.out.println("UQ factor for " + filename + ": " + uqAbundance);
            // This method sums up to the upper quartile. This is apparently not how to do it
//            ScrollableResults uqCounts = session.createCriteria(Sequence_Entity.class)
//                    .createAlias("unique_sequence", "u")
//                    .createAlias("u.type", "type")
//                    .createAlias("filename_sequence", "f")
//                    .add(Restrictions.in("type.id.type", annotationTypes.getTypes()))
//                    .add(Restrictions.eq("f.fileID", filename))
//                    .setProjection(Projections.property("abundance"))
//                    .setFirstResult(uqPercentile).scroll();
//            int uqSum = 0;
//            while(uqCounts.next())
//            {
//                uqSum += (int) uqCounts.get(0);
//            }
                    //.setProjection(Projections.sum("abundance")).uniqueResult();
            uqs.put(filename, uqAbundance);
        }
        sw.lap("Sum of UQ seqs");
        
        //create a session for the database
        Batcher b = new Batcher(session);

        // Retrieve all sequences for the specified annotation types
//        Criteria criteria = session.createCriteria(Sequence_Entity.class)
//                .createAlias("unique_sequence", "u")
//                .createAlias("u.type", "type")
//                .add(Restrictions.in("filename", filenames))
//                .add(Restrictions.in("type.id.type", annotationTypes.getTypes()));
        
        ScrollableResults seqs = session.createSQLQuery(SQL_ABUNDANCES_WITH_TYPE_AND_FILE)
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotationTypes.getTypes()).scroll(ScrollMode.FORWARD_ONLY);

        //a scrollable result set iterates through the data lazy loading each one
//        ScrollableResults seqs = seqQuery.scroll(ScrollMode.FORWARD_ONLY);

        while (seqs.next()) {

            long seqId = ((BigInteger) seqs.get(0)).longValue();
            int expression = (int) seqs.get(1);
            String fileID = (String) seqs.get(2);
            
            double normalised = (expression / uqs.get(fileID)) * upscale;
//            s_e.addExpression(THIS_NORM, normalised);
            currentId = this.writeExpressionLine(out, currentId, seqId , THIS_NORM, normalised);
//            b.batchFlush();
            b.batchClear();
        }
        sw.lap("Normalising seqs");
        // Add normalised filename totals to database
        List<Filename_Entity> files = session.getNamedQuery("@HQL_GET_SELECTED_FILES")
                .setParameterList("filenames", fileIDs).list();
        for(Filename_Entity file : files) {
            // since this is normalised pt per "upscale constant", the totals will be UPSCALE_CONSTANT
            file.addTotalNormalisedAbundance(THIS_NORM, upscale);
            b.batchFlush();
        }
        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
      
    public long performBootstrapNormalisation(Collection<String> fileIDs, Map<String, Double> fileTotals, AnnotationSet annotations, BufferedWriter out, long currentId) throws NoSuchExpressionValueException, IOException
    {
        final NormalisationType THIS_NORM = NormalisationType.BOOTSTRAP;
        Random r = new Random();
        r.setSeed(1);
        
        StopWatch sw = new StopWatch("Bootstrap");
        sw.start();
        //create a session for the database
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
        
        // find smallest lib size
        // get the selected filenames
        Query filenameQuery = session.createQuery("from Filename_Entity f where f.fileID in(:filenames)")
                .setParameterList("filenames", fileIDs);
        
        // iterate through the table
        List<Filename_Entity> files = filenameQuery.list();
        double lowestAbundance = Double.MAX_VALUE;
        
        // Keeps track of the list index of redundant sequences
        HashMap<String, Integer> cumulative_pointer = new HashMap<>();
        HashMap<String, Integer> sampled_pointer = new HashMap<>();
        
        // Load file information from database
        for(Filename_Entity file_e : files){
            String fileID = file_e.getFileID();
            
            // find the total file abundance for the specified annotations
            double totalAbundance = fileTotals.get(fileID);
            
            // add file to totals hash
            fileTotals.put(fileID, totalAbundance);
            // check lowest abundance
            if(totalAbundance < lowestAbundance){
                lowestAbundance = totalAbundance;
            }
            
            // initialise pointers
            cumulative_pointer.put(fileID, 0);
            sampled_pointer.put(fileID, 0);
            // not sure this is really needed for this table
        }
        
        // Assign the lowest abundance found as total for each file
        for(Filename_Entity file_e : files){
            file_e.addTotalNormalisedAbundance(THIS_NORM, lowestAbundance);
        }
        b.batchFlush();
        
        // Holds a priority queue of sampled indices 
        HashMap<String, int[]> fileSampled = new HashMap<>();
        
        // Sampling from redundant indices for each file
        for (String fileID : fileTotals.keySet()){
            
            // run garbage collection to free up memory used by the last massive indexes array
            System.gc();
            
            // total original abundance for this file (truncate from double - shouldn't make a difference)
            int thisFileTotal = fileTotals.get(fileID).intValue();
            
            // Don't sample the libraries with the minimum sample size
            if(thisFileTotal != lowestAbundance){
                
                // initialise a list of indexes corresponding to
                // rows in the count matrix
                // Using primitive array over arraylist to keep the memory footprint as low
                // as possible
                int[] indexes = new int[thisFileTotal];
                for(int i = 0; i < thisFileTotal; i++){
                    indexes[i]=i;
                }

                // Doing the Fisher-Yates shuffle 
                int max = thisFileTotal;
                for(int i = 0; i < lowestAbundance; i++){
                    max--;
                    int rindex = r.nextInt(max);
                    int rnum = indexes[rindex];
                    int maxnum = indexes[max];
                    indexes[rindex] = maxnum;
                    indexes[max] = rnum;
                }

                // value max will end up pointing to thisFileTotal - lowestAbundance
                // Take the shuffled samples at the end of the array and make a queue          
                int[] sampledIndexes = Arrays.copyOfRange(indexes, max, thisFileTotal);
                Arrays.sort(sampledIndexes);
                fileSampled.put(fileID, sampledIndexes);
                
                sw.lap("Sampling from file " + fileID);
            }
        } 
     
        // Retrieve all sequences for the specified annotation types
        Query seqQuery = session.createSQLQuery(SQL_ABUNDANCES_WITH_TYPE_AND_FILE)
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotations.getTypes());
        
        ScrollableResults seqs = seqQuery.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query Sequences");
        // for each row (sequence)
        while(seqs.next()){
//            Sequence_Entity s_e = (Sequence_Entity) seqs.get(0);
//            String fileID = s_e.getFilename_sequence().getFileID();
            
long seqId = ((BigInteger) seqs.get(0)).longValue();
            int abundance = (int) seqs.get(1);
            String fileID = (String) seqs.get(2);

            // Converting this double to integer because a raw abundance SHOULD be an integer
            // We need to have the safety of integers specifically for this count-type bootstrapping
//            int abundance = s_e.getExpression(NormalisationType.NONE).intValue();
            int sampledAbundance = 0;

            if(fileTotals.get(fileID) != lowestAbundance)
            {
                // Retrieve relevent objects for this filename
                int[] sampled = fileSampled.get(fileID);
                int samplePointer = sampled_pointer.get(fileID);
                int sampledLength = sampled.length;

                int cumulativeAbundance = cumulative_pointer.get(fileID);

                // add abundance on to cumulative pointer
                cumulativeAbundance += abundance;

                // store new pointer for next use.
                cumulative_pointer.put(fileID, cumulativeAbundance);

                // if the pointer is more than or equal to the current sampled index
                // add a sequence to the sampled abundance and go to the next
                // sampled index
                while(samplePointer < sampledLength && cumulativeAbundance > sampled[samplePointer]){
                    sampledAbundance++;
                    samplePointer++;
                }

                sampled_pointer.put(fileID, samplePointer);

//                s_e.addExpression(THIS_NORM, sampledAbundance);
                currentId = writeExpressionLine(out, currentId, seqId, THIS_NORM, sampledAbundance);
            }
            else
            {
                // no sampling if already at minimum.
                // set abundance to non-normalised
//                s_e.addExpression(THIS_NORM, abundance);
                currentId = writeExpressionLine(out, currentId, seqId, THIS_NORM, abundance);
            }  
//            b.batchFlush();
            b.batchClear();
        }
        sw.lap("Normalising sequences to sampled abundances");
        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
    
    public long performQuantileNormalisation(Collection<String> fileIDs, AnnotationSet annotations, BufferedWriter out, long currentId) throws IOException
    {
        final NormalisationType THIS_NORM = NormalisationType.QUANTILE;
        StopWatch sw = new StopWatch("Quantile");
        sw.start();
        // create session for database
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
             
        // Get list of files. This is needed further down when totals are persisted to these entities
        Query filenameQuery = session.getNamedQuery("@HQL_GET_SELECTED_FILES")
                .setParameterList("filenames", fileIDs);
        
        List<Filename_Entity> files = filenameQuery.list();
        
        // Hold results in a map
        Map<String, ScrollableResults> sortedSeqs = new HashMap<>();
        // For each file
        Map<String, Double> libTotals = new HashMap<>();

        for(Filename_Entity file_e: files)
        {
            String fileID = file_e.getFileID();
            
            //Get ordered sequences for this file          
            String sortedSeqForFileHQL = 
                    "select s.abundance from Sequence_Entity s "
                    + "join s.unique_sequence u "
                    + "join u.type t "
                    + "where s.filename_sequence.fileID=:fileID "
                    + "and e.normType=:norm "
                    + "and t.id.type in (:types) "
                    + "order by s.Abundance asc";
            
           
            Query sortedSeqForFileQuery = session.createSQLQuery(SQL_SORTED_COUNTS)
                    .setParameter("fileID", fileID)
                    .setParameterList("atype", annotations.getTypes());
            
            //List testList = sortedSeqCrit.list();
            ScrollableResults sortedSeqResult = sortedSeqForFileQuery.scroll(ScrollMode.FORWARD_ONLY);
            sortedSeqs.put(fileID, sortedSeqResult);
            
            libTotals.put(fileID, 0.0);
        }
        sw.lap("Ranking sequences per library");
        
        int numfiles = sortedSeqs.size();
        int filesStillScrolling = numfiles;
        
        // A map to store rank counts aggregated by original count and original count frequencies
        Map<AbundanceKey,AbundanceValue> countFreqs = new HashMap<>();
        
        // Whilst some files still have results in them (files do not all have the
        // same number of seqs)
        while(filesStillScrolling > 0)
        {
           int totalRankAbundance = 0;
           // Iterate over files
           Iterator<Entry<String, ScrollableResults>> fileIt = sortedSeqs.entrySet().iterator();
           while (fileIt.hasNext())
           {
               Entry<String, ScrollableResults> e = fileIt.next();
               ScrollableResults results = e.getValue();
               
               // results.next returns false if this file is finished with
               if(results.next())
               {
                   // move to the next row in this file and get abundance
                   int originalAbundance = (int) results.get(0);
                   totalRankAbundance += originalAbundance;
               }
               else
               {
                   // remove file from sortedSeqs and decrement number
                   // of files still scrolling
                   fileIt.remove();
                   filesStillScrolling--;
               }
               
           }
           double averageRankAbundance = (double) totalRankAbundance / numfiles;
           
           // loop through files again at this row and add mean abundance of this row
           // to abundance map
           for (Entry<String, ScrollableResults> e : sortedSeqs.entrySet())
           {
               String filename = e.getKey();
               ScrollableResults results = e.getValue();
               int originalAbundance = (int) results.get(0);
               
               AbundanceKey key = new AbundanceKey(filename, originalAbundance);
               
               if (countFreqs.containsKey(key))
               {
                   countFreqs.get(key).add(averageRankAbundance);
               } else
               {
                   countFreqs.put(key, new AbundanceValue(averageRankAbundance));
               }
           }
        }
        sw.lap("Calculate ranked values");
        
        // Distribute stored abundances back to the database.
        //ScrollableResults alignedSeqs = session.getNamedQuery("@SQL_GET_ALIGNED").scroll(ScrollMode.FORWARD_ONLY);
        ScrollableResults seqs = session.createSQLQuery(SQL_ABUNDANCES_WITH_TYPE_AND_FILE)
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotations.getTypes()).scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query sequences");
        
        while(seqs.next())
        {
            
            long seqId = ((BigInteger) seqs.get(0)).longValue();
            int expression = (int) seqs.get(1);
            String fileID = (String) seqs.get(2);
            
            AbundanceKey key = new AbundanceKey(fileID, expression);
            AbundanceValue val = countFreqs.get(key);
//            se.addExpression(THIS_NORM, val.getAvg());
            currentId = writeExpressionLine(out, currentId, seqId, THIS_NORM, val.getAvg());
            double totalSum = libTotals.get(key.filename) + val.getAvg();
            libTotals.put(key.filename, totalSum);
            
//            b.batchFlush();
            b.batchClear();
        }
        sw.lap("Normalising counts");
        
        files = filenameQuery.list();
        // Add normalised filename totals to database
        for (Filename_Entity file : files) {
            file.addTotalNormalisedAbundance(NormalisationType.QUANTILE, libTotals.get(file.getFileID()));
        }

        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
    
    public long performTrimmedMeanNormalisation(Collection<String> fileIDs, Map<String, Double> libSizes, String referenceSampleName, AnnotationSet annotations, int mTrim, int aTrim, boolean weightByFactors, BufferedWriter out, long currentId) throws NoSuchExpressionValueException, IOException
    {   
        StopWatch sw = new StopWatch("TMM");
        sw.start();
        NormalisationType THIS_NORM = NormalisationType.TRIMMED_MEAN;
        // Parameters when calculating MAValues. Currently these are not
        // user-defined when calculating TMM
        Logarithm THIS_LOG = Logarithm.BASE_2;
        int THIS_OFFSET = 0;
        
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
        

        
        // maps to store the calcuated factors
        HashMap<String, Double> libSizeFactorMap = new HashMap<>();
        HashMap<String, Double> countFactorMap = new HashMap<>();
        double fsum = 0;
        
        // The tool used to store all MA values when they are calculated for retrieving later
        MAService maService = (MAService) DatabaseWorkflowModule.getInstance().getContext().getBean("MAService");
        
        // Ensure table is clear before performing normalisation
        maService.clearMAdata();
        
        Map<String, Double> normalisedLibTotals = new HashMap<>();
        Query filenameQuery = session.getNamedQuery("@HQL_GET_SELECTED_FILES")
                .setParameterList("filenames", fileIDs);
        List<Filename_Entity> files = filenameQuery.list();
        
        List<FilePair> filePairs  = new ArrayList<>();
        for (Filename_Entity fe : files) {
                String thisFile = fe.getFileID();
                filePairs.add(new FilePair(referenceSampleName, thisFile));
                
        }
        List<String> obsIDs = new ArrayList<>(fileIDs);
        obsIDs.remove(referenceSampleName);
        System.out.println("Building MA lists for TMM");
        maService.buildNonRedundantListOnRaw(referenceSampleName, obsIDs, libSizes, annotations.getTypes(),
                Arrays.asList(THIS_OFFSET), Arrays.asList(THIS_LOG));
        sw.lap("Calculate all MA values for pairs");
        for(FilePair fp : filePairs)
        {
            String thisFile = fp.getObserved();
            double factor = 1;
            if (!thisFile.equals(referenceSampleName)) {
                MASeries series = new MASeries(fp, NormalisationType.NONE, annotations, THIS_LOG, THIS_OFFSET);
                String pk = series.getFiles().getPairKey();
                // Trim M and A values within the database
                int size = maService.getNumberOfMAelements(series);
                
                maService.trimByM(series, mTrim, size);

                maService.trimByA(series, aTrim, size);

                // Use trimmed set of sequences to find a library size modification factor
                factor = getTMMfactorFromDB(session, series, weightByFactors);

            }

            // modify library sizes by factor

            libSizeFactorMap.put(thisFile, factor);
            fsum += Math.log(factor);
            
            normalisedLibTotals.put(thisFile, 0.0);

        }
        sw.lap("Calculating TMM factors");
        
        // for symmetry, allow factors to multiply to 1
        // take mean of factors and divide factor by this
        double fsym = Math.exp(fsum / libSizeFactorMap.size());
        
        // Iteration over library sizes
        double avgModifiedLibSizes = 0;
        for(String fileID : libSizeFactorMap.keySet())
        {
            // Symmetrical factor
            double symFactor = (libSizeFactorMap.get(fileID) / fsym);
            // calculate modified lib size
            double modifiedLibSize = libSizes.get(fileID) * symFactor; 
            // add to average
            System.out.println(fileID + " : symFactor " + symFactor + " | libsize " + libSizes.get(fileID) + " | modified lib size " + modifiedLibSize);
            avgModifiedLibSizes += modifiedLibSize;
            // put in count factor map
            countFactorMap.put(fileID,  modifiedLibSize);
        }
        avgModifiedLibSizes = avgModifiedLibSizes / libSizeFactorMap.size();
        
        System.out.println("Normalising sequences");
        // find count factors using corrected symmetrical factors
        ScrollableResults seqs = session.createSQLQuery(SQL_ABUNDANCES_WITH_TYPE_AND_FILE)
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotations.getTypes()).scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query sequences to normalise");
        while(seqs.next())
        {
            long seqId = ((BigInteger) seqs.get(0)).longValue();
            int expression = (int) seqs.get(1);
            String fileID = (String) seqs.get(2);
            
            double factor = countFactorMap.get(fileID);
            double normAbundance = (expression / factor ) * avgModifiedLibSizes;
//            se.addExpression(THIS_NORM, normAbundance);
            
            // add to library sums
            double normalisedLibSum = normalisedLibTotals.get(fileID) + normAbundance;
            normalisedLibTotals.put(fileID, normalisedLibSum);
            
//            b.batchFlush();
            currentId = writeExpressionLine(out, currentId, seqId, THIS_NORM, normAbundance);
            b.batchClear();
        }
        sw.lap("Scrolling and normalising");
        files = filenameQuery.list();
        // Add normalised filename totals to database
        for (Filename_Entity file : files) {
            file.addTotalNormalisedAbundance(NormalisationType.TRIMMED_MEAN, normalisedLibTotals.get(file.getFileID()));
        }
        
        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
    
    private double getTMMfactorFromDB(Session session, MASeries series, boolean doWeighting)
    {
//        ScrollableResults maresult = session.createCriteria(MA_Entity.class)
//                .add(Restrictions.in("annoType", annotationTypes))
//                .add(Restrictions.eq("id.filePair", new FilePair(ref, obs)))
//                .add(Restrictions.eq("trimmed", false))
//                .setProjection(Projections.projectionList().add(Projections.property("M")).add(Projections.property("weighting"))).scroll(ScrollMode.FORWARD_ONLY);
        
        Query query = series.setQueryParameters(session.createQuery("select ma.M, ma.weighting, ma.count " + MASeries.getFilterQuery()));
        ScrollableResults maresult = query.scroll();
        
        double mSum = 0;
        double wSum = 0;
        int numKept = 0;
        while(maresult.next())
        {
            double m = maresult.getDouble(0);
            double w = maresult.getDouble(1);
            int count = maresult.getInteger(2);
            if(doWeighting)
            {
                mSum += (m / w)*count;
                wSum += (1 / w)*count;
            }
            else
            {
                mSum += m*count;
            }
            numKept++;
        }
        
        double factor;
        
        if(doWeighting)
        {
            factor = Math.pow(2, mSum / wSum);
        }
        else
        {
            factor = Math.pow(2, mSum / numKept);
        }
        String factorReport = "TMM: Normalisation factor for " + series.getReference() + ", " + series.getObserved() + " : " + factor;
        System.err.println(factorReport);
        LOGGER.log(Level.INFO, factorReport);
        return (factor);
    }
    
    public long performDEseqNormalisation(Collection<String> fileIDs, AnnotationSet annotations, BufferedWriter out, long currentId) throws NoSuchExpressionValueException, IOException
    {
        NormalisationType THIS_NORM = NormalisationType.DESEQ;
        final String SQL_GET_NUM_FILES = "SELECT COUNT(File_Name) FROM FILENAMES";
        StopWatch sw = new StopWatch("DESeq");
        
        final String SQL_GEOMEANS = 
                  " SELECT S.RNA_Sequence,SUM(LOG(EXPRESSION))/:numfiles,COUNT(EXPRESSION) "
                + " FROM SEQUENCES AS S"
                + " INNER JOIN SEQUENCE_EXPRESSIONS AS E "
                + "     ON E.SEQUENCE_ID=S.SEQUENCE_ID "
                + "     AND E.NORMALISATION_TYPE=0" // for raw counts
                + " INNER JOIN UNIQUE_SEQUENCES AS U "
                + "     ON S.RNA_SEQUENCE=U.RNA_SEQUENCE "
                + " INNER JOIN ANNOTATION_TYPES AS A "
                + "     ON U.TYPE=A.ANNOTATION_TYPE AND U.REFERENCE=A.REFERENCE_SET_NAME "
                + "     AND A.ANNOTATION_TYPE IN (:annotTypes) " // for selected annotations
                + " INNER JOIN Filenames F ON S.File_Name=F.File_Name AND F.FileID IN (:filenames) "
                + " GROUP BY S.RNA_Sequence "
                // if number of nonzero files is the same as the number of files, the 
                // geo mean can be used to calculate the median. Otherwise ignore the row.
                + "HAVING COUNT(EXPRESSION) = :numfiles ";
        
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
        
        //BigInteger numfiles = (BigInteger) session.createSQLQuery(SQL_GET_NUM_FILES).list().get(0);
        int numfiles = fileIDs.size();
        
        // Save geometric means of each sequence over all samples to a Map.
        // TODO: Probably not very memory efficient
        Map<String, Double> logGeoMeans = new HashMap<>();
        ScrollableResults lgmScrolled= session.createSQLQuery(SQL_GEOMEANS)
                .setParameter("numfiles", numfiles)
                .setParameterList("annotTypes", annotations.getTypes())
                .setParameterList("filenames", fileIDs)
                .scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query for geometric neans");
        while(lgmScrolled.next())
        {
            // Geometric mean is computed even if count is zero in one sample
            // Deseq ignores all zero-containing rows, so we must do the same
            BigInteger numNZ = (BigInteger) lgmScrolled.get(2);

            logGeoMeans.put((String) lgmScrolled.get(0), (Double) lgmScrolled.get(1));
            b.batchFlush();
        }
        sw.lap("Saving geometric means");
        
        Map<String, List<Double>> libVars = new HashMap<>();
        Map<String, Double> libFactors = new HashMap<>();

        
        // For each sequence, find the counts - geometric mean and split into
        // the different libs
        //ScrollableResults sequences = session.getNamedQuery("@SQL_GET_ALIGNED").scroll();
        
        //build select all query from the aligned table
        Query sequencesQuery = session.getNamedQuery("@HQL_SEQUENCES_WITH_TYPE_AND_FILE")
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotations.getTypes());
        sw.lap("Query sequences");
        ScrollableResults sequences = sequencesQuery.scroll(ScrollMode.FORWARD_ONLY);
        b.finish();
        while(sequences.next())
        {
            Sequence_Entity se = (Sequence_Entity) sequences.get(0);
            String fileID = se.getFilename_sequence().getFileID();
            String sequence = se.getRNA_Sequence();
            double abundance = se.getExpression(NormalisationType.NONE);
            
            // Many sequences will have been ignored when calculating geomean,
            // check for this.
            if(logGeoMeans.containsKey(sequence))
            {
                double var = Math.log(abundance) - logGeoMeans.get(sequence);
            
                if(libVars.containsKey(fileID))
                {
                    libVars.get(fileID).add(var);
                }
                else
                {
                    List<Double> newLibList = new ArrayList<>();
                    newLibList.add(var);
                    libVars.put(fileID, newLibList);
                }
            }
            b.batchFlush();
        }
        sw.lap("Calculating count difference between lib and geometric reference");

        Map<String, Double> libTotal = new HashMap<>();

        // Find the median of these values per library
        for(String fileID : libVars.keySet())
        {
            // Find the total library size
            //BigInteger libsize = (BigInteger) session.createSQLQuery(NormalisationServiceLayer.SQL_GET_LIB_TOTAL)
            //        .setParameter("filename", filename).list().get(0);
            
            List<Double> vars = libVars.get(fileID);
            Collections.sort(vars);
            
            // floor any .5
            int medianI = (int) (vars.size() / 2);
            double maxFromBottom = vars.get(medianI);
            double minFromTop = vars.get(vars.size() - medianI);
            
            double median = (maxFromBottom + minFromTop) / 2;
            double expMedian = Math.exp(median);
            System.out.println("DESEQ: Normalisation factor for " + fileID + ": " + expMedian);
            libFactors.put(fileID, expMedian);
            
            // Initialise total sums
            libTotal.put(fileID, 0.0);
        }
        sw.lap("Calculating library factors");
        
        // Normalise all counts with the library factors
        sequences = sequencesQuery.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Query sequences");
        b.finish();
        while (sequences.next()) {
            Sequence_Entity se = (Sequence_Entity) sequences.get(0);
            double factor = libFactors.get(se.getFilename_sequence().getFileID());
            
            // TODO: Deseq factors are not upscaled like in TMM. Is this correct?
            double normAbundance = se.getExpression(NormalisationType.NONE) / factor;
//            se.addExpression(THIS_NORM, normAbundance);
            currentId = this.writeExpressionLine(out, currentId, se.getId(), THIS_NORM, normAbundance);
            
            // Update total sum for this filename
            double totalSum = libTotal.get(se.getFilename_sequence().getFileID());
            totalSum += normAbundance;
            libTotal.put(se.getFilename_sequence().getFileID(), totalSum);
            
//            b.batchFlush();
            b.batchClear();
        }
        sw.lap("Normalising counts");
        
        // Add normalised filename totals to database
        List<Filename_Entity> files = session.getNamedQuery("@HQL_GET_SELECTED_FILES").setParameterList("filenames", fileIDs).list();
        for(Filename_Entity file : files)
        {
            file.addTotalNormalisedAbundance(NormalisationType.DESEQ, libTotal.get(file.getFileID()));
        }
        
        b.finish();
        session.close();
        sw.stop();
        sw.printTimes();
        return currentId;
    }
    
   
    /**
     * Returns the file who's library size is closest to the median library size
     * @return 
     */
    public String calculateReferenceSample(Collection<String> annotationTypes)
    {
        IteratingStopWatch sw = new IteratingStopWatch();
        sw.start();
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Batcher b = new Batcher(session);
        Map<String, Double> uqs = new HashMap<>();
        int medianSum = 0;

        ScrollableResults numSeqs = session.createQuery("select seq.fileID, count(u.RNA_Sequence) from Sequence_Entity seq "
                + "join seq.unique_sequence u "
                + "where u.type.id.type in (:annoTypes) "
                + "group by seq.fileID")
                .setParameterList("annoTypes", annotationTypes).scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("Calculate number of sequences");
        while(numSeqs.next())
        {
            String filename = (String) numSeqs.get(0);
            Long count = (Long) numSeqs.get(1);
            int percentile1 = (int) (count * 0.75);
//            int percentile2 = count.intValue()-percentile1;
//  
//            Double median = ((BigDecimal) session.createSQLQuery(SQL_UQ2)
//                    .setParameter("numseqs", percentile1)
//                    .setParameter("numseqs2", percentile2)
//                    .setParameter("file", filename)
//                    .setParameterList("annoType", annotationTypes)
//                    .uniqueResult()).doubleValue();
//            sw.lap("Find median for " + filename);
//            uqs.put(filename, median); 
//            medianSum+=median;
//            b.batchFlush();
            
            ScrollableResults orderedCounts = session.createSQLQuery(SQL_SORTED_COUNTS)
                    .setParameterList("atype", annotationTypes).setParameter("fileID", filename)
                    .scroll();
            
//            ScrollableResults orderedCounts = session.createQuery("select s.abundance from Sequence_Entity s "
//                    + "join s.unique_sequence u "
//                    + "where u.type.id.type in (:atype) and s.filename_sequence.fileID = :fileID "
//                    + "order by s.abundance")
//                    .setParameterList("atype", annotationTypes).setParameter("fileID", filename)
//                    .setFirstResult(percentile1).setMaxResults(2).setFetchSize(2)
//                    .scroll();
            sw.lap("Query sorted abundances");
            orderedCounts.setRowNumber(percentile1);
            int firstVal = (int) orderedCounts.get(0);
            int secondVal = firstVal;
            if(percentile1 %2 == 0){
                orderedCounts.next();
                secondVal = (int) orderedCounts.get(0);
            }
            
            double median = (firstVal+secondVal)/2;
            uqs.put(filename, median); 
            medianSum+=median;
            sw.lap("scrolling median");
        }
        
        b.finish();
        session.close();
        
        double medianAvg = medianSum / uqs.size();
        
        String ref = "";
        double smallestDiff = -1;
        for(Entry<String, Double> e : uqs.entrySet())
        {
            String filename = e.getKey();
            double uq = e.getValue();
            double thisDiff =  Math.abs(uq - medianAvg);
            if(smallestDiff == -1 || thisDiff < smallestDiff)
            {
                ref = filename;
                smallestDiff = thisDiff;
            }
        }
        sw.stop();
        sw.printTimes();
        return ref;
    }
    
    /*
    *SECTION For Exporting data
    */

    public void writeAllToCSV(Collection<String> filenames, Path location, AnnotationSet annotations, List<NormalisationType> types)throws IOException
    {
        
        if (!types.contains(NormalisationType.NONE))
        {
            types.add(NormalisationType.NONE);
        }
                
                
        Session session = uniqueSeqDao.getSessionFactory().openSession();

        //Query fnQuery = session.getNamedQuery("@HQL_GET_ALL_FILENAMES");
            
        //a scrollable result set iterates through the data lazy loading each one
        //List<Filename_Entity> file_ents = fnQuery.list();
        
        Criteria fn_cr = session.createCriteria(Filename_Entity.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.property("sampleID"), "sampleID")
                        .add(Projections.property("replicateID"), "replicateID")
                        .add(Projections.property("filename"), "filename"))
                .setResultTransformer(Transformers.aliasToBean(Filename_Entity.class));
        
        List<Filename_Entity> file_ents = fn_cr.list();
        
        HashMap<NormalisationType, BufferedWriter> norm_to_buffers = new HashMap<>();
        for (NormalisationType type : types)
        {
            //create a file and a buffered writer for each normalisation type and place it in a map
            Path normalisedFilePath = Paths.get(location.toString() + DIR_SEPARATOR + "Normalisation_" + type + ".csv");
            BufferedWriter writer
                    = Files.newBufferedWriter(
                            normalisedFilePath,
                            Charset.forName("UTF-8"),
                            StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
            
            String header = "Sequence";

            for(Filename_Entity f_e : file_ents)
            {
                //Filename_Entity f_e = file_ents.get(0);
                header += "," + f_e.getFilename();
               //ystem.out.println("name: " + f_e.getName());
            }
            
            //write the CSV header
            writer.write(header + LINE_SEPARATOR);

            norm_to_buffers.put(type, writer);
        }
//        
//        Criteria cr = session.createCriteria(Unique_Sequences_Entity.class)
//                .setProjection(Projections.projectionList()
//                        .add(Projections.property("RNA_Sequence"), "RNA_Sequence")
//                        .add(Projections.property("sequenceRelationships"), "sequenceRelationships"))
//                .setResultTransformer(Transformers.aliasToBean(Unique_Sequences_Entity.class)
//                );

//        Criteria cr = session.createCriteria(Unique_Sequences_Entity.class)
//                
//                .setResultTransformer(Transformers.aliasToBean(Unique_Sequences_Entity.class)
//                );
        
        Query cr = session.getNamedQuery("@HQL_GET_ALL_UNIQUES");
        
        ScrollableResults seqs = cr.scroll(ScrollMode.FORWARD_ONLY);
        
        while(seqs.next())
        {
            Unique_Sequences_Entity u_s_e = (Unique_Sequences_Entity) seqs.get(0);

            for (NormalisationType type : types)
            {
                //retrieve writer for this normalisation type
                BufferedWriter writer = norm_to_buffers.get(type);
                String newRow = "";
                newRow += u_s_e.getRNA_Sequence();


                for (Filename_Entity f_e : file_ents)
                {
                    if(u_s_e.getSequenceRelationships().containsKey(f_e.getFilename()))
                        newRow += "," + u_s_e.getSequenceRelationships().get(f_e.getFilename()).getExpressions().get(type).getExpression();
                    else
                        newRow += ",0";

                    //System.out.println("seqs: " + u_s_e.getSequenceRelationships().get(f_e.getFilename()).getExpressions().get(NormalisationType.NONE).getExpression());
                }
                writer.write(newRow + LINE_SEPARATOR);
            }


        }
        
        for(Entry<NormalisationType, BufferedWriter> buffer_entry : norm_to_buffers.entrySet())
        {
            buffer_entry.getValue().close();
            
        }
        
        session.flush();
        session.close();
        
    }
    
    public CountMatrix getDataMatrix(NormalisationType norm, Collection<String> fileIDs, AnnotationSet annotations) throws NoSuchExpressionValueException
    {
        CountMatrix matrix = new CountMatrix();
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Query seqQuery = session.getNamedQuery("@HQL_SEQUENCES_WITH_TYPE_AND_FILE")
                .setParameterList("filenames", fileIDs)
                .setParameterList("types", annotations.getTypes());
        ScrollableResults seqs = seqQuery.scroll(ScrollMode.FORWARD_ONLY);
        while(seqs.next())
        {
            Sequence_Entity seqe = (Sequence_Entity) seqs.get(0);
            matrix.add(seqe.getFileID(), seqe.getRNA_Sequence(), seqe.getExpression(norm));
        }
        session.close();
        return matrix;
    }
    
    public Set<NormalisationType> getAvailableNormalisations()
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        List norms = session.createQuery("select unique(normType) from Expression_Entity").list();
        Set<NormalisationType> normSet = new HashSet<>(norms);
        session.close();
        return normSet;
    }

    public void writeAllToFASTA(Collection<String> filenames, Path location, AnnotationSet annotations, List<NormalisationType> types)throws IOException
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Query seqQuery = session.getNamedQuery("@HQL_SEQUENCES_WITH_TYPE_AND_FILE")
                .setParameterList("filenames", filenames)
                .setParameterList("types", annotations.getTypes());

        //a scrollable result set iterates through the data lazy loading each one
        ScrollableResults seqs = seqQuery.scroll(ScrollMode.FORWARD_ONLY);
        
        HashMap<String, HashMap<NormalisationType, BufferedWriter>> outputFASTAFiles = new HashMap<>();
        
        if(!types.contains(NormalisationType.NONE))
            types.add(NormalisationType.NONE);
        
        Criteria fn_cr = session.createCriteria(Filename_Entity.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.property("sampleID"), "sampleID")
                        .add(Projections.property("replicateID"), "replicateID")
                        .add(Projections.property("filename"), "filename"))
                .setResultTransformer(Transformers.aliasToBean(Filename_Entity.class));
        
        List<Filename_Entity> file_ents = fn_cr.list();
        
        for (Filename_Entity filename : file_ents)
        {
            //create directory to store all the normalised files for this filename
            Path normFileDirectory = Paths.get(location.toString() + DIR_SEPARATOR + FilenameUtils.removeExtension(filename.getFilename()) + "NormalisedFASTAFiles");
            Files.createDirectory(normFileDirectory);
            HashMap<NormalisationType, BufferedWriter> norm_to_buffers = new HashMap<>();
            for (NormalisationType type : types)
            {
                //create a file and a buffered writer for each normalisation type and place it in a map
                Path normalisedFilePath = Paths.get(normFileDirectory.toString() + DIR_SEPARATOR + filename.getFilename() + "_" + type + ".fa");
                BufferedWriter writer
                        = Files.newBufferedWriter(
                                normalisedFilePath,
                                Charset.forName("UTF-8"),
                                StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);

                
                norm_to_buffers.put(type, writer);
            }
            //now place the map of normatlisation types to writers into a map of filename keys
            outputFASTAFiles.put(filename.getFilename(), norm_to_buffers);
        }

        
        
        while (seqs.next())
        {
            Sequence_Entity s_e = (Sequence_Entity) seqs.get(0);
            for(Entry<NormalisationType,Expression_Entity> type : s_e.getExpressions().entrySet())
            {
                BufferedWriter writer = outputFASTAFiles.get(s_e.getFilename()).get(type.getKey());
                
                writer.write(">"+s_e.getRNA_Sequence()+"("+type.getValue().getExpression() + ")" + LINE_SEPARATOR);
                writer.write(s_e.getRNA_Sequence() + LINE_SEPARATOR);
            }
        }
        
        for(Entry<String, HashMap<NormalisationType, BufferedWriter>> buffer_entry : outputFASTAFiles.entrySet())
        {
            for(Entry<NormalisationType, BufferedWriter> n_2_f : buffer_entry.getValue().entrySet())
            {
                n_2_f.getValue().close();
            }
        }
        session.flush();
        session.close();
    }
    
    /*
    END of exporting data section
    */
    
    class AbundanceKey{
        String filename;
        int abundance;   
        public AbundanceKey(String filename, int abundance){
            this.filename = filename;
            this.abundance = abundance;
        }
        
        @Override
        public int hashCode(){
            return new HashCodeBuilder(17, 31).append(filename).append(abundance).toHashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if(!(obj instanceof AbundanceKey))
                return true;
            if(obj == this)
                return true;
            
            AbundanceKey rhs = (AbundanceKey) obj;
            return new EqualsBuilder().append(filename, rhs.filename).append(abundance, rhs.abundance).isEquals();
        }
    }
    
    class AbundanceValue{
        double abundance = 0;
        int frequency = 0;
        
        public AbundanceValue(double newAbundance){
            this.abundance = newAbundance;
            this.frequency++;
        }
        
        public void add(double newAbundance){
            abundance += newAbundance;
            frequency++;
        }
        
        public double getAvg(){
            return abundance/frequency;
        }
    }
}
