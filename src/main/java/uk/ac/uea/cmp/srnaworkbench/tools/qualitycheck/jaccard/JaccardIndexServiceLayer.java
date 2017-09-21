/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Jaccard_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.io.JsonWriter;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author matt
 */
@Service("JaccardIndex")
public class JaccardIndexServiceLayer {
    
    @Autowired
    JaccardDAO jaccDao;
    
    public List<String> files;
    public List<NormalisationType> normTypes;
    int numSeqs;
    private String stageName = "";
    
    
    
    public double calculateJaccardIndex(String sampleA, String sampleB, NormalisationType norm1, NormalisationType norm2, int numberOfSeqs)
    {
        Session session = jaccDao.getSessionFactory().openSession();
//        DetachedCriteria decrit = DetachedCriteria.forClass(Sequence_Entity.class)
//                .setProjection(Projections.projectionList().add(Projections.property("filename")).add(Projections.property("abundance")))
//                .addOrder(Order.desc("abundance")).;
//        String testSql = "SELECT TOP :numseqs S.RNA_SEQUENCE AS SEQ, E.EXPRESSION AS EXPRESSION "
//                + "FROM SEQUENCES S "
//                + "JOIN SEQUENCE_EXPRESSIONS E ON E.Sequence_Id=S.SEQUENCE_ID "
//                + "ORDER BY E.EXPRESSION DESC";
//        List list = session.createSQLQuery(testSql)
////                .setParameter("norm1", norm1)
//                .setParameter("numseqs", numberOfSeqs)
////                .setParameter("fnameA", sampleA)
//                .list();
        // FIXME: COUNT(normcountcol) needs to be consistent for norm1 and norm2 columns
        String sql = "SELECT C, COUNT(*) FROM ( SELECT COUNT(EXPRESSION) AS C, SEQ FROM "
                + "((SELECT TOP :numseqs S.RNA_SEQUENCE AS SEQ, E.EXPRESSION AS EXPRESSION"
                + "     FROM SEQUENCES S JOIN FILENAMES F ON F.File_Name=S.File_Name AND F.FileID=:fnameA JOIN SEQUENCE_EXPRESSIONS E ON E.Sequence_Id=S.SEQUENCE_ID AND E.NORMALISATION_TYPE=:norm1 ORDER BY E.EXPRESSION DESC) "
                + "UNION ALL "
                + "(SELECT TOP :numseqs S.RNA_SEQUENCE AS SEQ, E.EXPRESSION AS EXPRESSION "
                + "     FROM SEQUENCES S JOIN FILENAMES F ON F.File_Name=S.File_Name AND F.FileID=:fnameB JOIN SEQUENCE_EXPRESSIONS E ON E.Sequence_Id=S.SEQUENCE_ID AND E.NORMALISATION_TYPE=:norm2 ORDER BY E.EXPRESSION DESC)) "
                + "GROUP BY SEQ ) GROUP BY C";
        Query q = session.createSQLQuery(sql)
                .setParameter("numseqs", numberOfSeqs)
                .setParameter("norm1", norm1.ordinal())
                .setParameter("norm2", norm2.ordinal())
                .setParameter("fnameA", sampleA)
                .setParameter("fnameB", sampleB);

        
//        crit.setFirstResult(0).setMaxResults(numberOfSeqs);
        List l = q.list();
        session.close();
        Iterator<Object[]> i = l.iterator();
        int numU = 0;
        int numI = 0;
        while(i.hasNext())
        {
            Object[] o = i.next();
            int key = ((BigInteger) o[0]).intValueExact();
            int val = ((BigInteger) o[1]).intValueExact();
            
            // Intersect is just the number of the sequences that are present in both files
            if(key == 2)
                numI = val;
            
            // Union is the number of distinct sequences whether or not they were in both files.
            numU += val;
        }
//        ScrollableResults r = q.scroll(ScrollMode.FORWARD_ONLY);
//
//        int numIntersect = 0;
//        int numUnion = 0;
//        while(rA.next() && rB.next())
//        {
//            String sA = rA.getString(0);
//            String SB = rB.getString(0);
//            
//        }
        return (double) numI/numU ;
    }
    
    public void calculateJaccardTable(Collection<String> filenames, List<NormalisationType> normTypes, int numberOfSeqs)
    {
        FilenameServiceImpl f = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        List<FilePair> filepairs = FilePair.generateAllPairs(new ArrayList<>(filenames));
        for (NormalisationType norm : normTypes) {
            for(FilePair pair : filepairs)
            {
                Filename_Entity refE = f.getFileById(pair.getReference());
                Filename_Entity obsE = f.getFileById(pair.getObserved());
                double thisJacc = calculateJaccardIndex(pair.getReference(), pair.getObserved(), norm, norm, numberOfSeqs);
                Jaccard_Entity jaccE = new Jaccard_Entity(numberOfSeqs, refE, obsE, thisJacc, norm);
                this.jaccDao.createOrUpdate(jaccE);
            }
        }

    }
    
    public void printAllRows()
    {
        Session session = this.jaccDao.getSessionFactory().openSession();
        ScrollableResults rows = session.createCriteria(Jaccard_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        while(rows.next())
        {
            Jaccard_Entity j = (Jaccard_Entity) rows.get(0);
            System.out.println(j.toString());
        }
        session.close();
    }
    
    public void writeToJson(Path path) throws IOException
    {
        Session session = this.jaccDao.getSessionFactory().openSession();
        ScrollableResults results = session.createQuery("from Jaccard_Entity j "
                + "join j.refFile r join r.sample rs "
                + "join j.obsFile o join o.sample os "
                + "order by rs.sampleNumber asc, r.replicateID asc, os.sampleNumber asc, o.replicateID asc")
                .scroll(ScrollMode.FORWARD_ONLY);
        
        JsonGenerator jg = JsonWriter.jsonFactory.createGenerator(path.toFile(), JsonEncoding.UTF8);
        jg.writeStartArray();

        while(results.next())
        {
            Jaccard_Entity obj = (Jaccard_Entity) results.get(0);
            jg.writeStartObject();
            jg.writeNumberField("Top", obj.getNumberOfSeqs());
            jg.writeStringField("Reference", obj.getRefSeq().getFileID());
            jg.writeStringField("Normalisation", obj.getNormType().toString());
            jg.writeStringField("Observed", obj.getObsSeq().getFileID());
            jg.writeNumberField("Jaccard", obj.getJaccardIndex());
            jg.writeEndObject();
        }
        jg.writeEndArray();
        jg.close();
        session.close();
    }
    
    public static void main(String[] args)
    {
        try {
            DatabaseWorkflowModule.test_construct_DB();
            JaccardIndexServiceLayer j = (JaccardIndexServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("JaccardIndex");
            
//            for(int i = 10; i < 10000; i += 10)
//            {
//                double ji = j.calculateJaccardIndex("X366868.fasta", "X366869.fasta", i);
//                System.out.println(i + ": " + ji);
//            }
              

//            calculateJaccardTable(files, )
            
        } catch (IOException ex) {
            Logger.getLogger(JaccardIndexServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(JaccardIndexServiceLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
}
