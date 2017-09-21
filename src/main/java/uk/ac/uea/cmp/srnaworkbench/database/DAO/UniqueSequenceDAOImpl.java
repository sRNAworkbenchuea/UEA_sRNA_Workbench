/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;

/**
 *
 * @author w0445959
 */
@Repository("UniqueSequenceDAO")
public class UniqueSequenceDAOImpl extends GenericDaoImpl<Unique_Sequences_Entity, String>
{
        
    @Autowired
    public UniqueSequenceDAOImpl(SessionFactory sf)
    {
        super(sf);
    }

    public void importDataFromFile(String filename)
    {
        Session session = getSessionFactory().openSession();
//        List records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
//                .addEntity(Sequence_Filename_Entity.class).list();
//       

        Transaction tx = session.beginTransaction();
        
        ReferenceSequenceManager notMapped = new ReferenceSequenceManager(ReferenceSequenceManager.NO_REFERENCE_NAME);
        session.flush();
        tx.commit();
        notMapped.addType(ReferenceSequenceManager.NO_TYPE_NAME);
        notMapped.setPrimaryType(ReferenceSequenceManager.NO_TYPE_NAME);
        
        //** Add initial annotation to the database
        // genome - for the genome mappings
        // none - for unmapped sequences 
            ReferenceSequenceManager genome = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
            genome.addType(ReferenceSequenceManager.INTERGENIC_TYPE_NAME);


        
        session.flush();
        session.doWork(
                new Work()
                {
                    public void execute(Connection connection) throws SQLException
                    {
                        //doSomething(connection);
                        Statement s = connection.createStatement();

                        //s.execute("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(null,'SEQUENCES','" + filename + "',null,null,null,0)");
                        s.execute("DROP TABLE UNIQUE_SEQUENCES");
                        session.clear();
                        
                        s.execute("CREATE TABLE UNIQUE_SEQUENCES("
                              //  + "RNA_Sequence VARCHAR(60), "
                                + "RNA_Sequence VARCHAR(1000), "
                                + "RNA_Size INT,"
                                + "Total_Count INT,"
                                + "Type VARCHAR(255),"
                                + "REFERENCE VARCHAR(255),"
                                + "PRIMARY KEY(RNA_Sequence)) "
                                + "AS SELECT * FROM CSVREAD('" + filename + "', 'RNA_Sequence,RNA_Size,Total_Count,Type,Reference', 'UTF-8', ',')");
//                        s.execute("ALTER TABLE UNIQUE_SEQUENCES ADD ("
//                                + "REFERENCE VARCHAR(255) DEFAULT('"+ReferenceSequenceManager.NO_REFERENCE_NAME+"')) ");
//                                + "SAMPLE_SEQUENCES_ID INT NULL)");
                        s.execute("CREATE UNIQUE INDEX unique_rna_seq_idx ON UNIQUE_SEQUENCES (RNA_Sequence)");
                        s.execute("CREATE INDEX rna_size_idx ON UNIQUE_SEQUENCES (RNA_Size)");
                        s.execute("CREATE INDEX total_count_idx ON UNIQUE_SEQUENCES (Total_Count)");
                        s.close();
                        connection.close();

                    }
                }
        );
        session.clear();
        session.close();

    }
    
    @Override
    protected Class<Unique_Sequences_Entity> getEntityClass()
    {
        return Unique_Sequences_Entity.class;
    }

    
}
