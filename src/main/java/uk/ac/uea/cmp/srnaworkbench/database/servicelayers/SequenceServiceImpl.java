/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

/**
 *
 * @author w0445959
 */
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.data.count.CountMatrix;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.SequenceFilter;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 * Implements the business methods for the Sequence service
 *
 * @author w0445959
 */
@Service("SequenceService")
public class SequenceServiceImpl implements GenericService<Sequence_Entity, String> {

    @Autowired
    private SequenceDAOImpl seqDao;

    @Override
    public Sequence_Entity findById(String seq) {

        return seqDao.read(seq);

    }

    public Aligned_Sequences_Entity create_with_relationship(PatmanEntry p_e, String key) {
        return seqDao.findAndAddRelationShip(p_e, key);

    }

    public void create_with_relationship(ArrayList<PatmanEntry> batch_patmans) {
        seqDao.findAndAddRelationShips(batch_patmans);
    }

    public int findSequenceAbund(String seq) {
        return seqDao.findSequenceAbund(seq);
    }

    @Override
    public List<Sequence_Entity> findAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void save(Sequence_Entity se) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void saveOrUpdateAll(List<Sequence_Entity> seqs) {
        seqDao.createBatch(seqs);
    }

    public void importDataFromFile(String filename) {
        seqDao.importDataFromFile(filename);
    }

    @Override
    public void saveOrUpdate(Sequence_Entity se) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Sequence_Entity se) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Sequence_Entity se) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param location
     * @param unique_data
     * @return 
     */
    public int statelessFASTAFileWrite(Path location, Path unique_data) {
        String UNIX_LINE_SEPARATOR = "\n";
        int totalUniqueReads = 0;
        Session session = seqDao.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        //session.af
        Criteria criteria = session.createCriteria(Sequence_Entity.class);
        criteria.setProjection(Projections.projectionList().add(Projections.sum("abundance"))
                .add(Projections.groupProperty("RNA_Sequence")));
//        criteria.setProjection(Projections.distinct(Projections.property("RNA_Sequence")));

        ScrollableResults seqs = criteria.scroll(ScrollMode.FORWARD_ONLY);
        //List<String> list = criteria.list();
        try (BufferedWriter writer = Files.newBufferedWriter(location, Tools.UTF8_charset);
                BufferedWriter unique_writer = Files.newBufferedWriter(unique_data, Tools.UTF8_charset)) {
            while (seqs.next()) //for(String get : list)
            {
                int abundance = ((Long) seqs.get(0)).intValue();
                String get = (String) seqs.get(1);
                writer.write(">" + get + UNIX_LINE_SEPARATOR);
                writer.write(get + UNIX_LINE_SEPARATOR);
                unique_writer.write(get + "," + get.length() + "," + abundance + "," + ReferenceSequenceManager.NO_TYPE_NAME + "," +ReferenceSequenceManager.NO_REFERENCE_NAME + UNIX_LINE_SEPARATOR);
                totalUniqueReads++;

            }
            tx.commit();
            if (session.isOpen()) {
                session.close();
            }

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        return totalUniqueReads;
    }

    public void printFileRelationships() {

        Session session = seqDao.getSessionFactory().openSession();

        ScrollableResults seqs = session.getNamedQuery("@HQL_GET_ALL_SEQUENCES")
                .scroll(ScrollMode.FORWARD_ONLY);

        while (seqs.next()) {
            Sequence_Entity get = (Sequence_Entity) seqs.get(0);
            //Hibernate.initialize(get.getSequenceFilenameRelationships());
            System.out.println("Sequence: " + get.getRNA_Sequence());
            System.out.println("\t Found in files: ");
            Filename_Entity filename_sequence = get.getFilename_sequence();

            System.out.println("Name: " + filename_sequence.getFilename() + " Path: " + filename_sequence.getFilePath());

        }
        if (session.isOpen()) {
            session.close();
        }

    }

    public void printSequences() {
        int totalUniqueReads = 0;
        Session session = seqDao.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Sequence_Entity.class);

        ScrollableResults seqs = criteria.scroll(ScrollMode.FORWARD_ONLY);

        while (seqs.next()) {
            Sequence_Entity seq = (Sequence_Entity) seqs.get(0);
            System.out.println("\t ID: " + seq.getId() + " seq: " + String.format("%30s", seq.getRNA_Sequence()) + " file: " + seq.getFilename() + " abund: " + seq.getAbundance()
//                    + " PT norm abund: " + seq.getPER_TOTAL()
//                    + " UPPER_QUARTILE norm abund: " + seq.getUPPER_QUARTILE()
//                    + " TRIMMED_MEAN norm abund: " + seq.getTRIMMED_MEAN()
//                    + " QUANTILE norm abund: " + seq.getQUANTILE()
//                    + " BOOTSTRAP norm abund: " + seq.getBOOTSTRAP()
//                    + " DESEQ norm abund: " + seq.getDESEQ()
                    + " Hit count: " + seq.getGenomeHitCount());

            session.evict(seq);
        }
        if (session.isOpen()) {
            session.close();
        }

    }
    
    public void printExpressions()
    {
        Session session = seqDao.getSessionFactory().openSession();

        Query query = session.createQuery("from Sequence_Entity");
//        List list = query.list();
        ScrollableResults seqs = query.scroll(ScrollMode.FORWARD_ONLY);

        while (seqs.next())
        {
            Sequence_Entity seq = (Sequence_Entity) seqs.get(0);
            System.out.println(seq.toString());
        } 
        session.close();
    }

    public void printAlignedRelationships() {

        Session session = seqDao.getSessionFactory().openSession();

        ScrollableResults seqs = session.getNamedQuery("@HQL_GET_ALL_SEQUENCES")
                .scroll(ScrollMode.FORWARD_ONLY);

//        while (seqs.next())
//        {
//            Sequence_Entity get = (Sequence_Entity) seqs.get(0);
//            //Hibernate.initialize(get.getSequenceFilenameRelationships());
//            System.out.println("Sequence: " + get.getSequence());
//            System.out.print("\t Aligns to: ");
//            Set<Aligned_Sequences_Entity> alignedSequenceRelationships = get.getAlignedSequenceRelationships();
//            
//            for(Aligned_Sequences_Entity a_e : alignedSequenceRelationships)
//            {
//                System.out.println("Chrom: " + a_e.getId().getChrom() + " Start: " +
//                        a_e.getId().getStart()+ " End: " +
//                        a_e.getId().getEnd() + " Strand: " + 
//                        a_e.getId().getStrand());
//            }
//
//            System.out.println("");
//        }
        if (session.isOpen()) {
            session.close();
        }

    }

    public List<Map<String, Object>> executeGenericSQL(String sql, int firstResult, int maxResults) {
        // open a session
        org.hibernate.Session session = this.seqDao.getSessionFactory().openSession();
        // run generic sql query
        List<Map<String, Object>> result = DatabaseWorkflowModule.executeGenericSQLMapped(session, sql, firstResult, maxResults);
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return result;
    }
    
     public List<Map<String, Object>> executeGenericSQL(String sql) {
        // open a session
        org.hibernate.Session session = this.seqDao.getSessionFactory().openSession();
        // run generic sql query
        List<Map<String, Object>> result = DatabaseWorkflowModule.executeGenericSQLMapped(session, sql);
         //System.out.println("sql(" + result.size() + "): " + sql);
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return result;
    }

    public int getNumRecords(String sql) {
        try
        {
        // open a session
        org.hibernate.Session session = this.seqDao.getSessionFactory().openSession();
        int numResults = DatabaseWorkflowModule.getNumRecords(session, sql);
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return numResults;
        }
        catch(DataException ex)
        {
            System.err.println("DATA EXCEPTION: " + ex);
            System.err.println(ex.getMessage());
            System.err.println(ex.getSQLException());
            System.err.println(ex.getSQLState());
            return 0;
        }
        catch(Exception ex)
        {
            System.err.println("EXCEPTION: " + ex);
            return 0;
        }
    }
    
    public void filterSequences(SequenceFilter filter) throws IOException
    {
        Session session = this.seqDao.getSessionFactory().openSession();
        Path path = Paths.get(FileUtils.getTempDirectoryPath() + "filtered_sequences.csv");
        
        try (BufferedWriter fout = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.CREATE))
        {
            ScrollableResults filteredSeqs = filter.createFilterCriteria().getExecutableCriteria(session).scroll(ScrollMode.FORWARD_ONLY);
            String sep = ",";
            while (filteredSeqs.next())
            {
                Sequence_Entity thisSeq = (Sequence_Entity) filteredSeqs.get(0);
                fout.write(thisSeq.getId() + sep + thisSeq.getRNA_Sequence() + sep + thisSeq.getGenomeHitCount()
                        + sep + thisSeq.getAbundance() + sep + thisSeq.getWeighted_abundance() + sep + thisSeq.getFilename() + sep + thisSeq.getFileID() + LINE_SEPARATOR);
            }
        }
        session.doWork((Connection connection) -> {
            try (Statement s = connection.createStatement()) {
                System.out.println("Dropping table");
                s.execute("DROP TABLE Sequences");

                session.clear();
                System.out.println("CSVREAD");
                s.execute("CREATE TABLE SEQUENCES("
                        + "SEQUENCE_ID LONG PRIMARY KEY, "
                        + "RNA_Sequence VARCHAR(60), "
                        + "GenomeHitCount INT, "
                        + "Abundance INT, "
                        + "weighted_abundance DOUBLE, "
                        + "File_Name VARCHAR(255), "
                        + "File_ID VARCHAR(255))"
                        + "AS SELECT * FROM CSVREAD('" + path.toString() + "', 'SEQUENCE_ID,RNA_Sequence,GenomeHitCount,Abundance,"
                        + "weighted_abundance,File_Name,File_ID', 'UTF-8', ',')");
                s.execute("CREATE INDEX rna_seq_idx ON SEQUENCES (RNA_Sequence)");
                s.execute("CREATE INDEX abundance_idx ON SEQUENCES (Abundance)");
                System.out.println("Done");
            }
            connection.close();
        });
        session.flush();
        session.clear();
        session.close();
        
    }

   /* public List<List<String>> executeGenericSQL(String sql, boolean displayFieldNames) {
        // open a session
        org.hibernate.Session session = this.seqDao.getSessionFactory().openSession();
        // run generic sql query
        List<List<String>> result = DatabaseWorkflowModule.executeGenericSQL(session, sql, displayFieldNames);
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return result;
    }*/

    public List<Sequence_Entity> executeSQL(String sql) {
       
        
            
      
        
        List<Sequence_Entity> rows = null;

        // open a session
        org.hibernate.Session session = this.seqDao.getSessionFactory().openSession();
 
            // run the query
            Query query = session.createQuery(sql);
            query.setCacheable(true);
          
            rows = query.list();
      
            // close the session
            if (session.isOpen()) {
                session.close();
            }

        return rows;
    }
    
    public CountMatrix getDataMatrix(AnnotationSet annots)
    {
        CountMatrix matrix = new CountMatrix();
        Session session = seqDao.getSessionFactory().openSession();
        ScrollableResults results = session.createQuery("from Sequence_Entity s join s.unique_sequence u "
                + "where u.type.id.type in (:annots)").setParameterList("annots", annots.getTypes()).scroll();
        while(results.next())
        {
            Sequence_Entity seq = (Sequence_Entity) results.get(0);
            matrix.add(seq.getFileID(), seq.getRNA_Sequence(), (double) seq.getAbundance());
        }
        session.close();
        return matrix;
    }

    public List<String> getUniqueListOfFilenames() {
        Session session = seqDao.getSessionFactory().openSession();

        Criteria criteria = session.createCriteria(Sequence_Entity.class);
        criteria.setProjection(Projections.distinct(Projections.property("filename")));
        List<String> filenames= criteria.list();
        session.close();
        return filenames;
        
    }

    public HashMap<String,Integer[]> getSequenceTotals()
    {
        HashMap<String,Integer[]> totals = new HashMap<>();
        Session session = seqDao.getSessionFactory().openSession();
        
        SQLQuery filenameToTotal = session.createSQLQuery("SELECT FILE_NAME,COUNT(RNA_SEQUENCE), SUM(ABUNDANCE) FROM SEQUENCES GROUP BY FILE_NAME");
        List<Object[]> list = filenameToTotal.list();
        
        for(Object[] data : list)
        {
            String filename = data[0].toString();
            Integer[] total = {((BigInteger)data[1]).intValue(),((BigInteger)data[2]).intValue()};
            totals.put(filename, total);
        }
        
        //totals.put(list.get(0), list.get(1));
        
        session.close();
        
        return totals;

    }

    public HashMap<String, Integer[]> getMappedTotals()
    {
        HashMap<String,Integer[]> totals = new HashMap<>();
        Session session = seqDao.getSessionFactory().openSession();
        
        SQLQuery filenameToMappedTotal_R = session.createSQLQuery("SELECT FILE_NAME,TOTAL_GENOME_MATCH_ABUNDANCE FROM FILENAMES ");
        List<Object[]> list_R = filenameToMappedTotal_R.list();
        
        SQLQuery filenameToMappedTotal_NR = session.createSQLQuery(
                "SELECT DISTINCT SEQUENCES.File_Name, COUNT(SEQUENCES.RNA_SEQUENCE) AS total " +
                "FROM SEQUENCES " +
                "WHERE exists ( select 1 from ALIGNED_SEQUENCES where SEQUENCES.RNA_Sequence=ALIGNED_SEQUENCES.RNA_Sequence LIMIT 1) " +
                "GROUP BY SEQUENCES.File_Name ");
        List<Object[]> list_NR = filenameToMappedTotal_NR.list();
        
        for(Object[] data : list_R)
        {
            String filename = data[0].toString();
            Integer[] total = {Integer.parseInt(data[1].toString()),0};
            totals.put(filename, total);
        }
        
        for(Object[] data : list_NR)
        {
            String filename = data[0].toString();
            Integer[] total = totals.get(filename);
            total[1] = Integer.parseInt(data[1].toString());
            totals.put(filename, total);
        }
        
        //totals.put(list.get(0), list.get(1));
        
        session.close();
        
        return totals;
    }
    
    public void getMappedNRSCD(HashMap<String, Integer[]> NR, HashMap<String, Integer[]> R)
    {
        Session session = seqDao.getSessionFactory().openSession();
        
        SQLQuery sizeClasses = session.createSQLQuery("SELECT MAPPED,NON_REDUNDANT_ABUNDANCE,REDUNDANT_ABUNDANCE,FILE FROM SIZE_CLASSES WHERE MAPPED=0 ORDER BY FILE");
        
        ScrollableResults scroll = sizeClasses.scroll(ScrollMode.FORWARD_ONLY);
        
        while (scroll.next())
        {

            String filename = scroll.get(3).toString();

        }
        session.close();
    }
    
    public void getUnMappedNRSCD(HashMap<String, Integer[]> NR, HashMap<String, Integer[]> R)
    {
        Session session = seqDao.getSessionFactory().openSession();
        
        SQLQuery sizeClasses = session.createSQLQuery("SELECT NON_REDUNDANT_ABUNDANCE,REDUNDANT_ABUNDANCE,FILE FROM SIZE_CLASSES ORDER BY FILE");
        
        ScrollableResults scroll = sizeClasses.scroll(ScrollMode.FORWARD_ONLY);
        
        while(scroll.next())
        {
            
        }
        session.close();
    }

    
}
