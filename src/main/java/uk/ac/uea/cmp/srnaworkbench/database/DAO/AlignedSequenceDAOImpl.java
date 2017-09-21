/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;

/**
 *
 * @author w0445959
 */
@Repository("AlignedSequenceDAOImpl")
public class AlignedSequenceDAOImpl extends GenericDaoImpl<Aligned_Sequences_Entity, Aligned_Sequences_Entity.Id>
{

    @Autowired
    public AlignedSequenceDAOImpl(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<Aligned_Sequences_Entity> getEntityClass()
    {
        return Aligned_Sequences_Entity.class;
    }

    public void importDataFromFile(String aligned_file_path)
    {
        Session session = getSessionFactory().openSession();
        
        // check if this table exists so that it can be dropped if necessary
        int tableExists = ((BigInteger) session.createSQLQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name='ALIGNED_SEQUENCES'").uniqueResult()).intValue();
        
        if(tableExists == 1)
                        {
                            session.createSQLQuery("DROP TABLE ALIGNED_SEQUENCES").executeUpdate();
                        }
        
        String buildTable = "CREATE TABLE ALIGNED_SEQUENCES("
                                + "Chrom VARCHAR(2000) NOT NULL,"
                                + "RNA_Sequence VARCHAR(1000) NOT NULL, "
                                + "Seq_Start INT NOT NULL,"
                                + "Seq_End INT NOT NULL,"
                                + "Strand VARCHAR(1) NOT NULL,"
                                + "gaps INT,"
                                + "TYPE VARCHAR(255),"
                                + "REFERENCE VARCHAR(255),"
                                + "REFERENCE_SEQUENCE VARCHAR(255) ,"
                                + "locus_ID LONG,"
                                // Columns for joining to window entities
                                + "WINDOW_REFERENCE_SEQUENCE VARCHAR(255) NULL,"
                                + "WINDOW_CHROM VARCHAR(255) NULL,"
                                + "WINDOW_ID INT NULL,"
                                + "WINDOW_LENGTH INT NULL,"
                                //compound primary key
                                + "PRIMARY KEY(REFERENCE_SEQUENCE,Seq_Start,Seq_End,Chrom,Strand)) "
                                + "AS SELECT * FROM CSVREAD('" + aligned_file_path + "', 'Chrom\tRNA_Sequence\tSeq_Start\tSeq_End\tStrand\tgaps\ttype\treference\treference_sequence\tlocus_id\twindow_reference_sequence\twindow_chrom\twindow_id\twindow_length', 'UTF-8', CHAR(9))";
        SQLQuery createSQLQuery = session.createSQLQuery(buildTable);
        createSQLQuery.executeUpdate();
        
        String generateIndex1 = "CREATE INDEX align_rna_seq_idx ON ALIGNED_SEQUENCES (RNA_Sequence)";
        SQLQuery idx1 = session.createSQLQuery(generateIndex1);
        idx1.executeUpdate();
        
        String generateIndex2 = "CREATE INDEX align_reference_idx ON ALIGNED_SEQUENCES (REFERENCE_SEQUENCE)";
        SQLQuery idx2 = session.createSQLQuery(generateIndex2);
        idx2.executeUpdate();
//        
//  
//        
//        Transaction tx = session.beginTransaction();
//        session.doWork(
//                new Work()
//                {
//                    public void execute(Connection connection) throws SQLException
//                    {
//                        //doSomething(connection);
//                        Statement s = connection.createStatement();
//
//                        //s.execute("CALL SYSCS_UTIL.SYSCS_IMPORT_TABLE(null,'SEQUENCES','" + filename + "',null,null,null,0)");
//                        if(tableExists == 1)
//                        {
//                            s.execute("DROP TABLE ALIGNED_SEQUENCES");
//                        }
//                        session.clear();
//                        s.execute("CREATE TABLE ALIGNED_SEQUENCES("
//                                + "Chrom VARCHAR(2000),"
//                                + "RNA_Sequence VARCHAR(1000), "
//                                + "Seq_Start INT,"
//                                + "Seq_End INT,"
//                                + "Strand VARCHAR(1),"
//                                + "gaps INT,"
//                                + "PRIMARY KEY(Seq_Start,Seq_End,Chrom,Strand)) "
//                                + "AS SELECT * FROM CSVREAD('" + aligned_file_path + "', 'Chrom\tRNA_Sequence\tSeq_Start\tSeq_End\tStrand\tgaps', 'UTF-8', CHAR(9))");
//                        s.execute("ALTER TABLE ALIGNED_SEQUENCES ADD ("
//                                + "TYPE VARCHAR(255) DEFAULT('"+ReferenceSequenceManager.INTERGENIC_TYPE_NAME+"'), "
//                                // part of foreign key to annotation type
//                                + "REFERENCE VARCHAR(255) DEFAULT('"+ReferenceSequenceManager.GENOME_REFERENCE_NAME+"'), "
//                                // part of this compound id
//                                + "REFERENCE_SEQUENCE VARCHAR(255) DEFAULT('"+ReferenceSequenceManager.GENOME_REFERENCE_NAME+"'), "
//                                + "locus_ID LONG,"
//                                // Columns for joining to window entities
//                                + "WINDOW_REFERENCE_SEQUENCE VARCHAR(255) NULL,"
//                                + "WINDOW_CHROM VARCHAR(255) NULL , "
//                                + "WINDOW_ID INT NULL, "
//                                + "WINDOW_LENGTH INT NULL "
//                                + ")");
//                        s.execute("CREATE INDEX align_rna_seq_idx ON ALIGNED_SEQUENCES (RNA_Sequence)");
//                        //s.execute("CREATE INDEX align_reference_idx ON ALIGNED_SEQUENCES (REFERENCE_SEQUENCE)");
//                        
//                        s.close();
//                        connection.close();
//
//                    }
//                }
//        );
        session.clear();
        session.close();
    }

    
   
    
}
