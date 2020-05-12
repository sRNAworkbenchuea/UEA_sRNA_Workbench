package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.Criteria;
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
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.IteratingStopWatch;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author w0445959
 */
@Service( "UniqueSequencesService" )
@Transactional
public class UniqueSequencesServiceImpl implements GenericService<Unique_Sequences_Entity, String>
{

    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;

    @Override
    public Unique_Sequences_Entity findById( String seq )
    {
        
        
        
        return uniqueSeqDao.read(seq );
        
        
    }
    

    @Override
    public List<Unique_Sequences_Entity> findAll()
    {
        return uniqueSeqDao.findAll();
    }

    @Override
    public void save( Unique_Sequences_Entity se )
    {
        uniqueSeqDao.create(se );
    }
    
    
    public void importDataFromFile(String filename)
    {
        uniqueSeqDao.importDataFromFile(filename);
    }
    
    @Override
    public void saveOrUpdate( Unique_Sequences_Entity se )
    {
        uniqueSeqDao.createOrUpdate(se );
    }

    @Override
    public void update( Unique_Sequences_Entity se )
    {
        uniqueSeqDao.update( se );
    }

    @Override
    public void delete( Unique_Sequences_Entity se )
    {
        uniqueSeqDao.delete( se );
    }

    @Override
    public void shutdown()
    {
        uniqueSeqDao.shutdown();
    }
    
    public void printUniqueSequenceInformation()
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();

//        ScrollableResults uniques = session.getNamedQuery("@HQL_GET_ALL_UNIQUES")
//                .scroll(ScrollMode.FORWARD_ONLY);
        ScrollableResults uniques = session.createCriteria(Unique_Sequences_Entity.class).scroll();
        while (uniques.next()) 
        {
            Unique_Sequences_Entity get = (Unique_Sequences_Entity) uniques.get(0);
            System.out.println(get.toString());
        }
        session.close();
        
    }

    public void printSequenceRelationships()
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();

        ScrollableResults uniques = session.getNamedQuery("@HQL_GET_ALL_UNIQUES")
                .scroll(ScrollMode.FORWARD_ONLY);

        while (uniques.next())
        {
            Unique_Sequences_Entity get = (Unique_Sequences_Entity) uniques.get(0);
            //Hibernate.initialize(get.getSequenceFilenameRelationships());
            Collection<Sequence_Entity> sequenceFilenameRelationships = get.getSequenceRelationships().values();
            
            
            System.out.println("Sequence: " + get.getRNA_Sequence()+ " Maps to: " );
            for(Sequence_Entity s_e : sequenceFilenameRelationships)
                System.out.println("seq" + s_e.getRNA_Sequence() + " abund: " + s_e.getAbundance());

        }
        session.close();
    }

    public void printAlignedRelationships()
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();

        ScrollableResults uniques = session.getNamedQuery("@HQL_GET_ALL_UNIQUES")
                .scroll(ScrollMode.FORWARD_ONLY);

        while (uniques.next())
        {
            Unique_Sequences_Entity get = (Unique_Sequences_Entity) uniques.get(0);
            //Hibernate.initialize(get.getSequenceFilenameRelationships());
            Set<Aligned_Sequences_Entity> sequenceAlignedRelationships = get.getAlignedSequenceRelationships();
            
            
            System.out.println("Sequence: " + get.getRNA_Sequence()+ " ALIGNS to: " );
            for(Aligned_Sequences_Entity a_s_e : sequenceAlignedRelationships)
                System.out.println("chrom: " + a_s_e.getId().getChrom() + " Coord: " + a_s_e.getId().getStart() + "-" + a_s_e.getId().getEnd());

        }
        session.close();
    }

    public void printExpressionMatrix()
    {
//        Session session = uniqueSeqDao.getSessionFactory().openSession();
//
//        ScrollableResults uniques = session.getNamedQuery("@HQL_GET_ALL_UNIQUES")
//                .scroll(ScrollMode.FORWARD_ONLY);
//
//        while (uniques.next())
//        {
//            Unique_Sequences_Entity get = (Unique_Sequences_Entity) uniques.get(0);
//            //Hibernate.initialize(get.getSequenceFilenameRelationships());
//            Set<Sequence_Entity> sequenceFilenameRelationships = get.getSequenceRelationships();
//            
//            
//            System.out.println("Sequence: " + get.getRNA_Sequence()+ " Maps to: " );
//            for(Sequence_Entity s_e : sequenceFilenameRelationships)
//                System.out.println("seq" + s_e.getRNA_Sequence() + " abund: " + s_e.getAbundance());
//            
//            Set<Aligned_Sequences_Entity> sequenceAlignedRelationships = get.getAlignedSequenceRelationships();
//            
//            
//            System.out.println("Sequence: " + get.getRNA_Sequence()+ " ALIGNS to: " );
//            for(Aligned_Sequences_Entity a_s_e : sequenceAlignedRelationships)
//                System.out.println("chrom: " + a_s_e.getId().getChrom() + " Coord: " + a_s_e.getId().getStart() + "-" + a_s_e.getId().getEnd());
//            
//            session.evict(get);
//
//        }
//        session.close();
    }
    public void calculateTotalAlignedPerFileThreaded(int totalUniqueReads)
    {
        try
        {
            

            StopWatch sw = new StopWatch("count LM times");
            sw.start();

            ExecutorService executorService = Executors.newFixedThreadPool(Tools.getThreadCount());
            Set<Callable<HashMap<String, Integer>>> callables = new HashSet<Callable<HashMap<String, Integer>>>();

            final int batchSize = 50;
            for(int offset = 0; offset < 520; offset+=batchSize)
            {
                final int o_s = offset;
                callables.add(new Callable<HashMap<String, Integer>>()
                {
                    public HashMap<String, Integer> call() throws Exception
                    {
                        Session session = uniqueSeqDao.getSessionFactory().openSession();
                        HashMap<String, Integer> fileToAbund = new HashMap<>();
                        String query = "SELECT DISTINCT SEQUENCES.File_Name, SEQUENCES.abundance, SEQUENCES.RNA_Sequence "
                                + "FROM UNIQUE_SEQUENCES "
                                + "INNER JOIN SEQUENCES "
                                + "ON SEQUENCES.RNA_Sequence=UNIQUE_SEQUENCES.RNA_Sequence "
                                + "INNER JOIN ALIGNED_SEQUENCES "
                                + "ON UNIQUE_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence "
                                + "LIMIT "
                                +o_s
                                +","
                                +batchSize;
                        System.out.println(query);
                        List<Object[]> uniques = session.createSQLQuery(
                                query
                                )
                                .list();

                        for (Object[] obj : uniques)
                        {
                            int current = fileToAbund.get(obj[0].toString()) == null ? 0 : fileToAbund.get(obj[0].toString());
                            fileToAbund.put(obj[0].toString(), current + Integer.parseInt(obj[1].toString()));
                        }
                        session.close();
                        return fileToAbund;
                    }
                });
            }
            

            List<Future<HashMap<String, Integer>>> futures = executorService.invokeAll(callables);

            for (Future<HashMap<String, Integer>> future : futures)
            {
                for(Entry<String, Integer> entry : future.get().entrySet())
                {
                    System.out.println("filename:  = " + entry.getKey() + " total: " + entry.getValue());
                }
                
            }
            sw.stop();
            sw.printTimes();

            
        }
        catch (InterruptedException | ExecutionException ex)
        {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }
    public void calculateTotalAlignedPerFile()
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();

//        Transaction tx = session.beginTransaction();
//        tx.begin();
//        StopWatch sw = new StopWatch("count LM times");
//        sw.start();
        
//        for (ArrayList<Path> sample : samples)
//        {
//            for (Path repFile : sample)
//            {
//                String query = "SELECT SEQUENCES.File_Name, SUM(SEQUENCES.abundance) AS total, SEQUENCES.RNA_Sequence "
//                        + "FROM UNIQUE_SEQUENCES "
//                        + "INNER JOIN SEQUENCES "
//                        + "ON SEQUENCES.RNA_Sequence=UNIQUE_SEQUENCES.RNA_Sequence "
//                        + "INNER JOIN ALIGNED_SEQUENCES "
//                        + "ON UNIQUE_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence "
//                        + "WHERE SEQUENCES.File_Name= \'" + repFile.getFileName() + "\' "
//                        + "GROUP BY SEQUENCES.File_Name, SEQUENCES.RNA_Sequence";
//                String query = "SELECT DISTINCT SEQUENCES.RNA_Sequence, SUM(SEQUENCES.abundance) AS total "
//                        + "FROM UNIQUE_SEQUENCES "
//                        + "INNER JOIN SEQUENCES "
//                        + "ON SEQUENCES.RNA_Sequence=UNIQUE_SEQUENCES.RNA_Sequence "
//                        + "INNER JOIN ALIGNED_SEQUENCES "
//                        + "ON UNIQUE_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence "
//                        //+ "WHERE SEQUENCES.File_Name= \'" + repFile.getFileName() + "\' "
//                        + "GROUP BY SEQUENCES.File_Name, SEQUENCES.RNA_Sequence";
                
                String query = "SELECT DISTINCT SEQUENCES.File_Name, SUM(SEQUENCES.abundance) AS total "
                        + "FROM SEQUENCES "
                        + "WHERE exists ( select 1 from ALIGNED_SEQUENCES where SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1) "
                        + "GROUP BY SEQUENCES.File_Name ";
                        //+ "LIMIT 0,150";
//                String query = "SELECT DISTINCT SEQUENCES.File_Name, SUM(SEQUENCES.abundance) AS total "
//                        + "FROM SEQUENCES "
//                        + "WHERE SEQUENCES.RNA_Sequence NOT IN (SELECT ALIGNED_SEQUENCES.RNA_Sequence FROM ALIGNED_SEQUENCES) "
//                        + "GROUP BY SEQUENCES.File_Name "
//                        + "LIMIT 0,150";
//                String query = "SELECT DISTINCT SEQUENCES.File_Name, SUM(SEQUENCES.abundance) AS total "
//                        + "FROM SEQUENCES "
//                        + "INNER JOIN UNIQUE_SEQUENCES "
//                        + "ON SEQUENCES.RNA_Sequence=UNIQUE_SEQUENCES.RNA_Sequence "
//                        + "LEFT JOIN ALIGNED_SEQUENCES "
//                        + "ON (SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence) "
//                        + "WHERE ALIGNED_SEQUENCES.RNA_Sequence IS NULL "
//                        + "GROUP BY SEQUENCES.File_Name "
//                        + "LIMIT 0,150";

                //System.out.println("query" + query);
                List<Object[]> uniques = session.createSQLQuery(
                        query
                        )
                        .list();
                
        //        
        //        List uniques = session.createSQLQuery("SELECT DISTINCT UNIQUE_SEQUENCES.RNA_Sequence "
        //                             + "FROM UNIQUE_SEQUENCES "
        //                             + "INNER JOIN ALIGNED_SEQUENCES "
        //                             + "ON UNIQUE_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence "
        //                             + "LIMIT 0,150")
        //                             .list();
                //HashMap<String, Integer> fileToAbund = new HashMap<>();
                for (Object[] obj : uniques)
                {
                    Filename_Entity get = (Filename_Entity)session.get(Filename_Entity.class, obj[0].toString());
                    get.setTotalGenomeMatchAbundance(Integer.parseInt(obj[1].toString()));
                    get.addTotalNormalisedAbundance(NormalisationType.NONE, Integer.parseInt(obj[1].toString()));
                    //get.addToUniqueAlignedValue();
//                    System.out.println("Filename: " + obj[0].toString());
//                    System.out.println("abund: " + Integer.parseInt(obj[1].toString()));
                }
            //}
//        }
        //tx.commit();
        session.flush();
        session.clear();

        session.close();
//        sw.stop();
//        sw.printTimes();
    }

    public void calculateNonRedundantDistribution(HashMap<String, Integer> dist)
    {
        Session session = uniqueSeqDao.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        tx.begin();
        
        //String query = "SELECT ;
        
        tx.commit();
        session.flush();
        session.clear();

        session.close();
    }
    public void calculateRedundantDistribution(HashMap<String, Integer> dist)
    {
        
    }
    
    public void importSequencesWithMappingInformation(Path tempCSVpath) throws IOException
    {
        StopWatch sw = new StopWatch ("Consensus annotation by CSVREAD");
        sw.start();
        Session session;
        try (BufferedWriter out = new BufferedWriter(new FileWriter(tempCSVpath.toFile())))
        {
            session = this.uniqueSeqDao.getSessionFactory().openSession();
            ScrollableResults r = session.createSQLQuery(
                    "SELECT RNA_Sequence, RNA_Size, Total_Count "
                            + "FROM Unique_Sequences WHERE EXISTS ( "
                            + "select 1 from ALIGNED_SEQUENCES where Unique_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1)")
                    .setFetchSize(10000)
                    .scroll(ScrollMode.FORWARD_ONLY);
            sw.lap("aligned query");
            while(r.next())
            {
                out.write(r.get(0) + "," + r.get(1) + "," + r.get(2) + "," 
                        + ReferenceSequenceManager.INTERGENIC_TYPE_NAME + ","
                        + ReferenceSequenceManager.GENOME_REFERENCE_NAME + LINE_SEPARATOR);
            }   sw.lap("writing aligned");
            r = session.createSQLQuery(
                    "SELECT RNA_Sequence, RNA_Size, Total_Count, Type, Reference "
                            + "FROM Unique_Sequences WHERE NOT EXISTS ( "
                            + "select 1 from ALIGNED_SEQUENCES where Unique_SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1)")
                    .setFetchSize(10000)
                    .scroll(ScrollMode.FORWARD_ONLY);
            sw.lap("unmapped query");
            while (r.next())
            {
                out.write(r.get(0) + "," + r.get(1) + "," + r.get(2) + "," + r.get(3) + "," + r.get(4)  + LINE_SEPARATOR);
            }
            sw.lap("writing mapped");
        }
        session.close();
        this.uniqueSeqDao.importDataFromFile(tempCSVpath.toString());
        sw.lap("CSVREAD of Unique sequences");
        sw.stop();
        sw.printTimes();
        tempCSVpath.toFile().deleteOnExit();
    }
    
    /**
     * Updates all Unique_Sequences to use the intergenic-genome annotation type if there is at
     * least one alignment to the genome.
     * @param referenceSetName 
     */
    public void updateMappedConsensusAnnotations(String referenceSetName) throws ChildBeforeParentException {
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
//        Transaction tx = session.beginTransaction();
//        tx.begin();
        Batcher batch = new Batcher(session);
        StopWatch sw = new StopWatch("Consensus tests");
        sw.start();
//        List list = session.createSQLQuery("SELECT U.RNA_Sequence FROM Unique_Sequences U where exists ( select 1 from Aligned_Sequences where U.RNA_Sequence=Aligned_Sequences.RNA_sequence Limit 1 )").list();
//        this.printAlignedRelationships();
        int numrecs = session.createSQLQuery("UPDATE Unique_Sequences U SET (U.Type,U.Reference)=('intergenic','genome') where exists ( select 1 from Aligned_Sequences where U.RNA_Sequence=Aligned_Sequences.RNA_sequence Limit 1 )")
                .executeUpdate();
        sw.printLap("Big update statement");
        
//        ScrollableResults result = session.createSQLQuery("SELECT U.RNA_Sequence FROM Unique_Sequences U WHERE EXISTS ( select 1 from Aligned_Sequences where U.RNA_Sequence=Aligned_Sequences.RNA_sequence Limit 1 )").scroll();
//        System.out.println(sw.printLap("Selecting where exists"));
//        int i=0;
//        while(result.next()){
//            String seq = (String) result.get(0);
//            session.createSQLQuery("UPDATE Unique_Sequences U SET (U.Type,U.Reference)=('intergenic','genome') where U.RNA_Sequence=:seq")
//                    .setParameter("seq", seq).executeUpdate();
//            i++;
//            if(i % 10000 ==0){
//                System.out.println( i + " : " + seq);
//            }
//            batch.batchFlush();
//        }
//        System.out.println(sw.printLap("Scrolling where exists"));
        
        
        
//        AnnotationService annotServ = (AnnotationService) DatabaseWorkflowModule.getInstance().getContext().getBean("AnnotationService");
//        Annotation_Type_Entity intergenic = annotServ.getType(ReferenceSequenceManager.GENOME_REFERENCE_NAME, ReferenceSequenceManager.INTERGENIC_TYPE_NAME);
//        result=session.createQuery("from Unique_Sequences_Entity u join u.alignedSequenceRelationships a").scroll();
//        System.out.println(sw.printLap("HQL join"));
//        while(result.next()){
//            Unique_Sequences_Entity u = (Unique_Sequences_Entity) result.get(0);
//            u.setConsensus_annotation_type(intergenic);
//            batch.batchFlush();
//        }
//        System.out.println(sw.printLap("Scrolling HQL join"));

        
//        String alignedType = "SELECT a.RNA_Sequence, t.Annotation_Type, t.Reference_Set_Name, t.Priority FROM Aligned_Sequences a JOIN Annotation_Types t ON a.Reference=t.Reference_Set_Name AND a.Type=t.Annotation_Type";
//        int numrecs = session.createSQLQuery(
//                        "UPDATE Unique_Sequences U SET (U.Type,U.Reference)="
//                        + "(SELECT t1.Annotation_Type, t1.Reference_Set_Name, t1.RNA_Sequence FROM ("+alignedType+") t1 "
//                        + "LEFT OUTER JOIN ("+alignedType+") t2 "
//                        + "ON ( t1.RNA_Sequence=t2.RNA_Sequence AND t1.Annotation_Type < t2.Annotation_Type ) "
//                        + "WHERE t2.RNA_Sequence IS NULL) A where U.RNA_Sequence=A.RNA_Sequence "
//                        + "").executeUpdate();
//        tx.commit();
//        tx.begin();
//        this.printUniqueSequenceInformation();
//        session.createSQLQuery("update U set U.reference=T.Reference_Set_Name, U.Type=T.Annotation_Type "
//                + "from Unique_Sequences U join "
//                + "(select Aligned_Sequences A on U.RNA_Sequence=A.RNA_Sequence").executeUpdate();
//        int something = session.createQuery("update Unique_Sequences_Entity u set u.type.id.reference, u.type.id.type"
//                + "(select u.type.id.reference, u.type.id.type from Annotation_Type_Entity type where type.priority="
//                    + "(select max(a.annotType.priority) from Aligned_Sequences_Entity a where a.aligned_sequence=u)"
//                + ")").executeUpdate();
//        System.out.println(something);
        batch.finish();
//        tx.commit();
        session.close();
        sw.stop();
        sw.printTimes();
    }
    /**
     * Update unique sequence type property for sequences that were found on this
     * reference set of sequence
     * @param referenceSetName
     */
    public void updateConsensusAnnotationTypes(String referenceSetName) {
        Session session = this.uniqueSeqDao.getSessionFactory().openSession();
        //Transaction tx = session.beginTransaction();
        //tx.begin();
        Batcher batch = new Batcher(session);
        // retrieve every unique sequence that has at least one aligned sequence
        // mapping to this reference set
        IteratingStopWatch sw = new IteratingStopWatch();
        sw.start();
        Criteria uniqueOnRef = session.createCriteria(Unique_Sequences_Entity.class)
                // join to aligned sequences
                .createAlias("alignedSequenceRelationships", "aseq")
                // restrict to unique sequence with an aligned sequence aligning to this reference
                .add(Restrictions.eq("aseq.id.reference_sequence", referenceSetName))
                // make sure returned entities are unique
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        ScrollableResults uniqueSeqs = uniqueOnRef.scroll(ScrollMode.FORWARD_ONLY);
        sw.lap("consensusType: query execution");
        while (uniqueSeqs.next()) {
            Unique_Sequences_Entity useq = (Unique_Sequences_Entity) uniqueSeqs.get(0);
            Annotation_Type_Entity currentType = useq.getConsensus_annotation_type();
            int currentRefPriority = currentType.getId().getReference().getReferencePriority();
            sw.lap("get seq and type");
            // for each aligned sequence check if its type is higher priority than the 
            // current consensus type
            for (Aligned_Sequences_Entity aseq : useq.getAlignedSequenceRelationships()) {
                sw.lap("get aligned sequence");
                Annotation_Type_Entity atype = aseq.getAnnotationType();
                //System.out.println("atype: " + atype);
                int aRefPriority = atype.getId().getReference().getReferencePriority();
                if (aRefPriority > currentRefPriority)
                {
                    // current type should be set to this aligned type
                    // if its a higher priority
                    currentType = atype;
                }
                else if(aRefPriority == currentRefPriority && atype.getPriority() > currentType.getPriority()) {
                    // current type should be set to this aligned type
                    // if its a higher priority
                    currentType = atype;
                }
            }
            sw.lap("each alignment update");

            // update the consensus type
            useq.setConsensus_annotation_type(currentType);
            batch.batchFlush();
            sw.lap("save and flush");
        }
        batch.finish();
        //tx.commit();
        session.close();
        sw.stop();
        sw.printTimes();
    }
    
    public List<Unique_Sequences_Entity> executeSQL(String sql) {
       
        
            
      
        
        List<Unique_Sequences_Entity> rows = null;

        // open a session
        org.hibernate.Session session = this.uniqueSeqDao.getSessionFactory().openSession();
 
            // run the query
            Query query = session.createQuery(sql);
            query.setCacheable(true);
          
            rows = query.list();
      
            // close the session
            if (session.isOpen()) {
                session.close();
            }
           
       // }
        // close the session
      //  if (session.isOpen()) {
          //  session.close();
        //}
        return rows;
    }
    
}
