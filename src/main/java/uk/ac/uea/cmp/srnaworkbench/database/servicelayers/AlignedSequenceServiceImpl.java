/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.AlignedSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Alignment_Window_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.ChildBeforeParentException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
@Transactional
@Service("AlignedSequenceService")
public class AlignedSequenceServiceImpl implements GenericService<Aligned_Sequences_Entity, Aligned_Sequences_Entity.Id> {

    @Autowired
    private AlignedSequenceDAOImpl alignedSeqDao;
    
    private int maxSizeClass = -1;
    private int minSizeClass = -1;
     
    public AlignedSequenceServiceImpl() {
        
    }

    @Override
    public Aligned_Sequences_Entity findById(Aligned_Sequences_Entity.Id id) {
        return alignedSeqDao.read(id);
    }

     /**
      * Retrieves a list of all alignments to a certain chromosome
      * @param chr
      * @return 
      */
//     public List<Aligned_Sequences_Entity> findByChromosome(String chr,  Session session) {
//
//        // Session session = alignedSeqDao.getSessionFactory().getCurrentSession();//openSession();
//         String hql = "from Aligned_Sequences_Entity s where s.id.chrom = :chromosome order by s.id.start";
//        
////         List<Aligned_Sequences_Entity> findAll = session.createQuery(hql)
////                    .setParameter("chromosome", chr)
////                    .list();
//         ScrollableResults findAll = session.createQuery(hql)
//                    .setParameter("chromosome", chr)
//                    .scroll();
//         
//         while(findAll.next()){
//             Aligned_Sequences_Entity a = (Aligned_Sequences_Entity)findAll.get(0);
//             System.out.println("");
//         }
//         
//         //session.flush();
////         for (Aligned_Sequences_Entity entity : findAll)
////         {
////             session.evict(entity);
////         }
//         session.flush();
//        // session.close();
//         
//         return findAll;
//    }
     
     /**
      * Retrieves a list of unique sequences that have aligned to the reference more than maxTimesAligned times.
      * @param maxTimesAligned
      * @return 
      */
      public HashSet<String> findSeqsWithAlignmentsLessThan(int maxTimesAligned, Session session) {
         
         String sql = "SELECT t.RNA_SEQUENCE " +
                        "FROM ( SELECT RNA_SEQUENCE, COUNT(RNA_SEQUENCE) count " +
                        "       FROM  ALIGNED_SEQUENCES " +
                        "       GROUP BY RNA_SEQUENCE )  t " +
                        "WHERE t.count <= :maxCount";
        
         HashSet<String> findAll = new HashSet<String>(session.createSQLQuery(sql)
                                        .setParameter("maxCount", maxTimesAligned)
                                        .list());
         
         return findAll;
    }
     
     
//     public List<Aligned_Sequences_Entity> findByCriteriaOrdered(Order order, Criterion... criterion) {
//        Session openSession = this.sf.openSession();
//        Criteria crit = openSession.createCriteria(Aligned_Sequences_Entity.class, "A");
//        
//        //getEntityClass().
//        
//        for (Criterion c : criterion) {
//            crit.add(c);
//        }
//        crit.addOrder(order);
//        openSession.flush();
//        List<T> critList = (List<T>)crit.list();
//        openSession.close();
//        return critList;
//    }

    @Override
    public List<Aligned_Sequences_Entity> findAll() {
        return alignedSeqDao.findAll();
    }
    
    public synchronized Aligned_Sequences_Entity findOrCreate(String reference, String chrom, String rna_sequence, int start, int end, String strand, int gaps)
    {
        Aligned_Sequences_Entity.Id id = new Aligned_Sequences_Entity.Id(start, end, chrom, strand, reference);
        Aligned_Sequences_Entity db_aligned_sequence = findById(id);
        if(db_aligned_sequence == null)
        {
            return new Aligned_Sequences_Entity(reference, chrom, rna_sequence, start, end, strand, gaps);
        }
        return db_aligned_sequence;
    }

    @Override
    public synchronized void save(Aligned_Sequences_Entity se) {
        if(findById(se.getId()) == null)
            alignedSeqDao.create(se);
    }


    @Override
    public void saveOrUpdate(Aligned_Sequences_Entity se) {      
            alignedSeqDao.createOrUpdate(se);
    }

    @Override
    public synchronized void update(Aligned_Sequences_Entity se) {
        alignedSeqDao.update(se);
    }

    @Override
    public synchronized void delete(Aligned_Sequences_Entity se) {
        alignedSeqDao.delete(se);
    }

    @Override
    public void shutdown() {
        alignedSeqDao.shutdown();
    }

    public void importDataFromFile(String aligned_file_path) {
        alignedSeqDao.importDataFromFile(aligned_file_path);
        DatabaseWorkflowModule.getInstance().printLap("importing alignments to DB");
        UniqueSequencesServiceImpl service = (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");
        // Update consensus annotation type for unique sequences that now have alignments to the genome
        try{
            if(DatabaseWorkflowModule.getInstance().isDebugMode()){
                  service.importSequencesWithMappingInformation(Paths.get("tempUnique.csv"));
//                service.updateMappedConsensusAnnotations(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
            }
            else{
//                service.updateConsensusAnnotationTypes(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
                  service.importSequencesWithMappingInformation(Paths.get("tempUnique.csv"));
            }
//        }
//        catch(ChildBeforeParentException e)
//        {
//            throw new RuntimeException("Something is wrong with the added intergenic annotation");
        } catch (IOException ex) {
            Logger.getLogger(AlignedSequenceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        DatabaseWorkflowModule.getInstance().printLap("Adding intergenic value to unique sequences for mappings");
    }

    public void writeMappingQualityToJson(Path outputFile) throws IOException {
        JsonFactory jfac = DatabaseWorkflowModule.getInstance().getJsonFactory();
        JsonGenerator jg = jfac.createGenerator(outputFile.toFile(), JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();

        // Session session = this.alignedSeqDao.getSessionFactory().openSession();
    }
    
    public void printAlignments()
    {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Aligned_Sequences_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while(results.next())
        {
            Aligned_Sequences_Entity seq = (Aligned_Sequences_Entity) results.get(0);
            String print = "\t "
                    + " seq: " + seq.getRna_seq()
                    + " chrom: " + seq.getId().getChrom()
                    + " Start: "
                    + seq.getId().getStart() + "-" + seq.getId().getEnd()
                    + " Annotation: " + seq.getAnnotationType().getId().getType();
            
            if(seq.getAlignmentWindow() != null)
                    print += " Window: " + seq.getAlignmentWindow().toString();
            
            System.out.println(print);
            for (GFF_Entity gff : seq.getAnnotations()) {
                System.out.println("\t\t" + gff.toString());
            }
        }
        session.close();
    }
    
    public void printWindows()
    {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Alignment_Window_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while(results.next())
        {
            Alignment_Window_Entity window = (Alignment_Window_Entity) results.get(0);
            System.out.println(window);
        }
        session.close();
    }
    
 
    public List<Aligned_Sequences_Entity> getAlignedSequencesFromSQL(String sql) {
        // open a session
        org.hibernate.Session session = this.alignedSeqDao.getSessionFactory().openSession();
        // run generic sql query
        Query query = session.createSQLQuery(sql).addEntity(Aligned_Sequences_Entity.class);
        List<Aligned_Sequences_Entity> list = query.list();
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return list;
    }

    public void modifyMultipleAlignments(HashMap<Aligned_Sequences_Entity.Id, Integer> alignmentsToModify)
    {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        
        for(Map.Entry<Aligned_Sequences_Entity.Id, Integer> entry : alignmentsToModify.entrySet())
        {
            Aligned_Sequences_Entity alignmentToModify = findById(entry.getKey());
            Aligned_Sequences_Entity.Id id = alignmentToModify.getId();
            int start = id.getStart();
            int end = id.getEnd();
           
            // Default reference is to the genome.
            Aligned_Sequences_Entity.Id defaultID = new Aligned_Sequences_Entity.Id(-start, -end, id.getChrom(), id.getStrand(), Aligned_Sequences_Entity.DEFAULT_REFERENCE_STRING);

            alignmentToModify.setId(defaultID);
        }
        session.close();
    }
   
    public int findMaxSizeClass()
    {
        //SELECT MAX(length(RNA_SEQUENCE)) FROM ALIGNED_SEQUENCES WHERE LENGTH(RNA_SEQUENCE)<60
        if (maxSizeClass < 0)
        {
            Session session = this.alignedSeqDao.getSessionFactory().openSession();
            SQLQuery createSQLQuery = session.createSQLQuery("SELECT MAX(length(RNA_SEQUENCE)) FROM ALIGNED_SEQUENCES WHERE LENGTH(RNA_SEQUENCE)<60");
            maxSizeClass = ((BigInteger) createSQLQuery.list().get(0)).intValue();
            
            session.flush();
            session.close();
        }

        return maxSizeClass;
    }

    public int findMinSizeClass()
    {
        //SELECT MIN(length(RNA_SEQUENCE)) FROM ALIGNED_SEQUENCES WHERE LENGTH(RNA_SEQUENCE)<60
        if (minSizeClass < 0)
        {
            Session session = this.alignedSeqDao.getSessionFactory().openSession();
            SQLQuery createSQLQuery = session.createSQLQuery("SELECT MIN(length(RNA_SEQUENCE)) FROM ALIGNED_SEQUENCES");
            
            List list = createSQLQuery.list();
            
            if(!list.isEmpty())
            {
                if(createSQLQuery.list().get(0) != null)
                    minSizeClass = ((BigInteger)createSQLQuery.list().get(0)).intValue();
                else
                {
                    minSizeClass = 0;
                    LOGGER.log(Level.WARNING, "Major problem finding minimum size class");
                }
            }
            else
            {
                minSizeClass = 0;
                LOGGER.log(Level.WARNING, "Major problem finding minimum size class");
            }
            
            session.flush();
            session.close();
        }

        return minSizeClass;

    }

    public void deleteListBySizeClass(List<Integer> allRemovalFlags)
    {
        Session session = this.alignedSeqDao.getSessionFactory().openSession();
        
        session.createQuery("delete from Unique_Sequences_Entity u where length(u.RNA_Sequence) in (:sizeClasses)")
                .setParameterList("sizeClasses", allRemovalFlags)
                .executeUpdate();

//        for (Integer sc : allRemovalFlags)
//        {
//            String hql = "delete from Aligned_Sequences_Entity where length(RNA_SEQUENCE)= :sizeClass";
//            session.createQuery(hql).setInteger("sizeClass", sc).executeUpdate();
//        }
        
        session.flush();
        session.close();
    }
}
