/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;
/**
 *
 * @author w0445959
 */
@Repository("SequenceDAO")
public class SequenceDAOImpl extends GenericDaoImpl<Sequence_Entity, String>
{

    PreparedStatement preparedStatement = null;

    String insertTableSQL = "INSERT INTO SEQUENCES"
            + "(RNA_Sequence, genomeHitCount, abundance) VALUES"
            + "(?,?,?)";
    
    SessionFactory sf = null;
    
    @Autowired
    public SequenceDAOImpl(SessionFactory sf)
    {
        super(sf);
    }
    
    public int findSequenceAbund(String seq)
    {
        Session openSession = getSessionFactory().openSession();
        openSession.clear();
        List records = openSession.createSQLQuery("SELECT abundance FROM SEQUENCES WHERE RNA_Sequence= \'" + seq + "\'")
                .list();

        openSession.clear();
        openSession.close();
        if (records.isEmpty())
        {
            return -1;
        }
        return (int) records.get(0);
    }

    //@Override
//    public Sequence_Entity read(String seq)
//    {
//        Session openSession = getSessionFactory().openSession();
//        openSession.clear();
//        
//        List records = openSession.createSQLQuery("SELECT * FROM SEQUENCES WHERE RNA_Sequence= \'" + seq + "\'")
//                .addEntity(Sequence_Entity.class).list();
//        
//        openSession.clear();
//        openSession.close();
//        //openSession.getTransaction().commit();
//        if(records.isEmpty())
//        {
//            return null;
//        }
//        return (Sequence_Entity) records.get(0);
//    }
//    public Sequence_Entity readWithFiles(String seq)
//    {
//        
//    }
//    public Sequence_Entity readWithAligned(String seq)
//    {
//        
//    }
    public Aligned_Sequences_Entity findAndAddRelationShip(PatmanEntry p_e, String key)
    {
        Session openSession = getSessionFactory().openSession();
        
        List records = openSession.createSQLQuery("SELECT * FROM SEQUENCES WHERE RNA_Sequence= \'" + key + "\'")
                .addEntity(Sequence_Entity.class).list();
        
        List rel_records = openSession.createSQLQuery("SELECT * FROM ALIGNED_SEQUENCES")
                .addEntity(Aligned_Sequences_Entity.class).list();
        
        
        Sequence_Entity located = ((Sequence_Entity) records.get(0));
        //Sequence_Entity located = read(key);
        //Hibernate.initialize(located.getAlignedSequenceRelationships());
        //getHibernateTemplate().initialize(located.getAlignedSequenceRelationships());
        
        Aligned_Sequences_Entity aligned_Sequences_Entity = new Aligned_Sequences_Entity("genome", p_e.getLongReadHeader(), located.getRNA_Sequence(), p_e.getStart(), p_e.getEnd(), 
                p_e.getSequenceStrand().getCode(), p_e.getMismatches() );
        
        rel_records = openSession.createSQLQuery("SELECT * FROM ALIGNED_SEQUENCES")
                .addEntity(Aligned_Sequences_Entity.class).list();
//        
        
        openSession.flush();
        openSession.clear();
        openSession.close();
        
        return aligned_Sequences_Entity;
        
    }
    
    public void findAndAddRelationShips(List<PatmanEntry> p_e_list)
    {
        Session openSession = getSessionFactory().openSession();
        
        Transaction tx = openSession.beginTransaction();
        int i = 0;

        for (PatmanEntry p_e : p_e_list)
        {
            List records = openSession.createSQLQuery("SELECT * FROM SEQUENCES WHERE RNA_Sequence= \'" + p_e.getSequence() + "\'")
                    .addEntity(Sequence_Entity.class).list();

            Sequence_Entity located = ((Sequence_Entity) records.get(0));
//
//            List al_records = openSession.createSQLQuery("SELECT * FROM ALIGNED_SEQUENCES")
//                    .addEntity(Aligned_Sequences_Entity.class).list();


            Aligned_Sequences_Entity aligned_Sequences_Entity = new Aligned_Sequences_Entity("genome", p_e.getLongReadHeader(), located.getRNA_Sequence(), p_e.getStart(), p_e.getEnd(),
                    p_e.getSequenceStrand().getCode(), p_e.getMismatches());
            
//            openSession.flush();
//            openSession.clear();
//            
//            al_records = openSession.createSQLQuery("SELECT * FROM ALIGNED_SEQUENCES")
//                    .addEntity(Aligned_Sequences_Entity.class).list();

            if (i % 50 == 0)
            { //50, same as the JDBC batch size
                //flush a batch of inserts and release memory:
                openSession.flush();
                openSession.clear();
            }
            i++;
    
        }

        tx.commit();
        openSession.close();


    }

    public void importDataFromFile(String filename)
    {

        Session session = getSessionFactory().openSession();
//        List records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
//                .addEntity(Sequence_Filename_Entity.class).list();
//       
        //Transaction tx = session.beginTransaction();
        Transaction beginTransaction = session.beginTransaction();
//        session.createSQLQuery("DROP TABLE SEQUENCES").executeUpdate();
//        session.clear();
//        session.createSQLQuery("CREATE TABLE SEQUENCES("
//                                + "SEQUENCE_ID LONG PRIMARY KEY, "
//                                + "RNA_Sequence VARCHAR(60), "
//                                + "GenomeHitCount INT, "
//                                + "Abundance INT, "
//                                + "PER_TOTAL DOUBLE, "
//                                + "UPPER_QUARTILE DOUBLE, "
//                                + "TRIMMED_MEAN DOUBLE, "
//                                + "QUANTILE DOUBLE, "
//                                + "BOOTSTRAP DOUBLE, "
//                                + "weighted_abundance DOUBLE, "
//                                + "File_Name VARCHAR(255)) "
//                                + "AS SELECT * FROM CSVREAD('" + filename + "', 'SEQUENCE_ID,RNA_Sequence,GenomeHitCount,Abundance,"
//                                + "PER_TOTAL,UPPER_QUARTILE,TRIMMED_MEAN,QUANTILE,BOOTSTRAP,"
//                                + "weighted_abundance,File_Name', 'UTF-8', ',')").executeUpdate();
//        session.clear();
//        session.createSQLQuery("CREATE INDEX rna_seq_idx ON SEQUENCES (RNA_Sequence)").executeUpdate();
        session.doWork(
                new Work()
                {
                    public void execute(Connection connection) throws SQLException
                    {
                        //doSomething(connection);
                        Statement s = connection.createStatement();

                        //s.execute("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(null,'SEQUENCES','" + filename + "',null,null,null,0)");
                  
                        //s.execute("DROP TABLE UNIQUE_SEQUENCES");
                        s.execute("DROP TABLE SEQUENCES");
                        s.execute("DROP TABLE SEQUENCE_EXPRESSIONS");
                        
                        
                        
                        session.clear();
                        s.execute("CREATE TABLE SEQUENCES("
                                + "SEQUENCE_ID LONG PRIMARY KEY, "
                                 + "RNA_Sequence VARCHAR(1000), " // put back due to errors: this might need to go back in but only if we see errors in Chris's code
                               //  + "RNA_Sequence VARCHAR(60), "
                                + "GenomeHitCount INT, "
                                + "Abundance INT, "
                                + "weighted_abundance DOUBLE, "
                                + "File_Name VARCHAR(255), "
                                + "File_ID VARCHAR(255))"
                                + "AS SELECT * FROM CSVREAD('" + filename + "', 'SEQUENCE_ID,RNA_Sequence,GenomeHitCount,Abundance,"
//                                + "PER_TOTAL,UPPER_QUARTILE,TRIMMED_MEAN,QUANTILE,BOOTSTRAP,DESEQ,"
                                + "weighted_abundance,File_Name,File_ID', 'UTF-8', ',')");
                        s.execute("CREATE INDEX rna_seq_idx ON SEQUENCES (RNA_Sequence)");
                        s.execute("CREATE INDEX abundance_idx ON SEQUENCES (Abundance)");
                        
                        // Create expression table with unormalised abundances by default
                        s.execute("CREATE TABLE SEQUENCE_EXPRESSIONS("
                                + "EXPRESSION_ID LONG PRIMARY KEY AUTO_INCREMENT, "
                                + "EXPRESSION DOUBLE, "
                                + "SEQUENCE_ID LONG, "
                                + "FOREIGN KEY (SEQUENCE_ID) REFERENCES SEQUENCES(SEQUENCE_ID)) ");
                        s.execute("INSERT INTO SEQUENCE_EXPRESSIONS (EXPRESSION, SEQUENCE_ID) "
                                + "SELECT ABUNDANCE, SEQUENCE_ID FROM SEQUENCES");
                        s.execute("ALTER TABLE SEQUENCE_EXPRESSIONS ADD ("
                                + "NORMALISATION_TYPE INT DEFAULT('0'))"); // NormalisationType.NONE
                                
//                        s.execute("CREATE TEXT TABLE SEQUENCES("
//                                + "SEQUENCE_ID INT PRIMARY KEY, "
//                                + "RNA_Sequence VARCHAR(60), "
//                                + "GenomeHitCount INT, "
//                                + "Abundance INT, "
//                                + "PER_TOTAL DOUBLE, "
//                                + "UPPER_QUARTILE DOUBLE, "
//                                + "TRIMMED_MEAN DOUBLE, "
//                                + "QUANTILE DOUBLE, "
//                                + "BOOTSTRAP DOUBLE, "
//                                + "weighted_abundance DOUBLE, "
//                                + "File_Name VARCHAR(255)) "
//                                );
//                        s.execute("CREATE INDEX rna_seq_idx ON SEQUENCES (RNA_Sequence)");
//                        s.execute(" SET TABLE SEQUENCES SOURCE \'" + filename + "\'");


                        
                        
//                        s.close();
//                        connection.close();

                    }
                }
        );
        //boolean active = session.getTransaction().isActive();
        //session.flush();
        beginTransaction.commit();
        //active = session.getTransaction().isActive();
        session.flush();
        session.close();

    }
    public void createBatch( List<Sequence_Entity> seqs )
    {
//        Session openSession = getSessionFactory().openSession();
//        Transaction transaction = openSession.beginTransaction();
//        for (Sequence_Entity se : seqs)
//        {
//            openSession.save(se);
//        }
//        openSession.flush();
//        openSession.clear();
//        transaction.commit();
//        openSession.close();
        
        //getHibernateTemplate().saveOrUpdateAll(seqs);
        Session session = getSessionFactory().openSession();
//        List records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
//                .addEntity(Sequence_Filename_Entity.class).list();
//        
        
        Transaction tx = session.beginTransaction();
//        session.doWork(
//                new Work()
//                {
//                    public void execute(Connection connection) throws SQLException
//                    {
//                        //doSomething(connection);
//                        PreparedStatement ps = connection.prepareStatement(
//                                "INSERT INTO SEQUENCES (RNA_Sequence, GenomeHitCount, TotalAbundance) VALUES (?, ?, ?)");
//                        for (Sequence_Entity seq : seqs)
//                        {
//                            ps.setString(1, seq.getSequence());
//                            ps.setInt(2, seq.getGenomeHitCount());
//                            ps.setInt(3, seq.getTotalAbundance());
//                            ps.addBatch();
//                        }
//                        int[] executeBatch = ps.executeBatch();
//                        
//                        ps.close();
//                        connection.close();
//
//                    }
//                }
//        );
        int i = 0;

        for (Sequence_Entity seq : seqs)
        {
            //session.save(seq);
            session.merge(seq);
            if (i % 50 == 0)
            { //50, same as the JDBC batch size
                //flush a batch of inserts and release memory:
                session.flush();
                session.clear();
            }
            i++;
//                records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
//                .addEntity(Sequence_Filename_Entity.class).list();
//        
        }

        tx.commit();
//            records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
//                .addEntity(Sequence_Filename_Entity.class).list();

        session.flush();
        session.close();



    }
    
    //@Override
    protected Class<Sequence_Entity> getEntityClass()
    {
        return Sequence_Entity.class;
    }
    
    

    
    
}
