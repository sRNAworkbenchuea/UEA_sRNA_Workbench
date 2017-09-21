/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.FilenameDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.SampleDAO;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Pair_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author w0445959
 */
@Service( "FilenameService" )
@Transactional
public class FilenameServiceImpl implements GenericService<Filename_Entity, String>
{
    @Autowired
    private FilenameDAOImpl filenameDao;
    
    @Autowired
    private SampleDAO sampleDao;
    
    private static int SAMPLE_NUMBER = 0;
    

    @Override
    public Filename_Entity findById( String filename )
    {
        return filenameDao.read(filename );
    }

    @Override
    public List<Filename_Entity> findAll()
    {
        return filenameDao.findAll();
    }

    @Override
    public void save( Filename_Entity fe )
    {
        filenameDao.create(fe );
    }
    
    @Override
    public void saveOrUpdate( Filename_Entity fe )
    {
        Sample_Entity sample = this.sampleDao.read(fe.getSampleID());
        if(sample == null){
            sample = new Sample_Entity(fe.getSampleID(), SAMPLE_NUMBER);
            SAMPLE_NUMBER++;
        }
        fe.setSample(sample);
        fe.setReplicateID(sample.getFilenames().size()+1);
        String fileID = fe.getSampleID()+"_"+fe.getReplicateID();
        fe.setFileID(fileID);
//        fe.setFilename(fe.getName());
        filenameDao.createOrUpdate(fe );
        sample.getFilenames().add(fe);
        sampleDao.createOrUpdate(sample);
    }
    
    public void printSamples()
    {
        Session session = this.sampleDao.getSessionFactory().openSession();
        List<Sample_Entity> samples = session.createQuery("from Sample_Entity").list(); 
        for(Sample_Entity sample : samples)
        {
            System.out.println(sample.toString());
        }
        session.close();
    }

    @Override
    public void update( Filename_Entity fe )
    {
        filenameDao.update( fe );
    }

    @Override
    public void delete( Filename_Entity fe )
    {
        filenameDao.delete( fe );
    }

    @Override
    public void shutdown()
    {
        filenameDao.shutdown();
    }
    
    /**
     * Returns the file with the specified ID. Because this is not unique or the
     * primary index, only the first entity with this file ID is found.
     * @param fileID the id of the file (usually samplename_replicatenumber
     * @return 
     */
    public Filename_Entity getFileById(String fileID){
        Session session = filenameDao.getSessionFactory().openSession();
        List<Filename_Entity> files = session.createQuery("from Filename_Entity f where  f.fileID=:id").setParameter("id", fileID).list();
        session.close();
        if(files.isEmpty())
        {
            return null;
        }
        else
        {
            return files.get(0);
        }
    }
    
    /**
     *
     * @param fileID
     * @param offset
     */
    public void setOffsetForFileID(String fileID, double offset)
    {
        Session session = filenameDao.getSessionFactory().openSession();
        List<Filename_Entity> files = session.createQuery("from Filename_Entity f where  f.fileID=:id").setParameter("id", fileID).list();
        if(files.isEmpty())
        {
            return;
        }
        else
        {
            files.get(0).setOffset(offset);
        }
        session.flush();
        session.close();
        
    }
    
    /**
     * Returns the total abundance of a filename for the specified annotations and normalisation type
     * @param fileID
     * @param annotationTypes
     * @return 
     */
    public double getTotalExpression(String fileID, NormalisationType normType, Collection<String> annotationTypes)
    {
        Session session = filenameDao.getSessionFactory().openSession();
        double total = (double) session.createCriteria(Sequence_Entity.class)
                .createAlias("expressions", "expressions")
                .createAlias("unique_sequence", "u")
                .createAlias("u.type", "type")
                .createAlias("filename_sequence", "f")
                .add(Restrictions.eq("f.fileID", fileID))
                .add(Restrictions.in("type.id.type", annotationTypes))
                .add(Restrictions.eq("expressions.normType", normType))
                .setProjection(Projections.sum("expressions.expression"))
                .uniqueResult();
        session.close();
        return total;
    }
    
    public Map<String, Double> getTotalExpression(Collection<String> fileIDs, NormalisationType normType, Collection<String> annotationTypes)
    {
        Session session = filenameDao.getSessionFactory().openSession();
        Map<String, Double> totals = new HashMap<>();
        List<Object[]> totalList = session.createQuery("select f.fileID, sum(e.expression) from Sequence_Entity s "
                + "join s.unique_sequence u "
                + "join s.expressions e "
                + "join s.filename_sequence f "
                + "where u.type.id.type in (:annoTypes) "
                + "and f.fileID in (:fileIDs) "
                + "and e.normType=:normType "
                + "group by f.fileID").setParameterList("annoTypes", annotationTypes).setParameterList("fileIDs", fileIDs).setParameter("normType", normType)
                .list();
        for(Object[] result : totalList)
        {
            totals.put((String) result[0], (Double) result[1]);
        }
        session.close();
        return totals;
    }
    
    /**
     * Returns a list of all filenames in this table
     * @return 
     */
    public List<String> getFilenames()
    {
        Session session = filenameDao.getSessionFactory().openSession();
        List<String> files = session.createSQLQuery("SELECT File_Name FROM FILENAMES").list();
        session.close();
        return files;
    }
    
    /**
     * Returns a list of all filenames in this table
     *
     * @return
     */
    public List<String> getFileIDs() {
        Session session = filenameDao.getSessionFactory().openSession();
        List<String> files = session.createQuery("SELECT f.fileID FROM Filename_Entity f").list();
        session.close();
        return files;
    }
    
    public List<String> getFileIDs(List<String> selected) {
        Session session = filenameDao.getSessionFactory().openSession();
        List<String> files = session.createQuery("SELECT f.fileID FROM Filename_Entity f WHERE f.filename in :Files").setParameterList("Files", selected).list();
        session.close();
        return files;
    }
    
    /**
     * 
     * @return all filename entities in the order that they are in the database
     */
    public List<Filename_Entity> getFiles()
    {
        Session session = filenameDao.getSessionFactory().openSession();
        List<Filename_Entity> files = session.createQuery("from Filename_Entity").list();
        session.close();
        
        return files;
    }
    
    /**
     * 
     * @return all sample entities in the order that they are in the database
     */
    public List<Sample_Entity> getSamples()
    {
        
        Session session = filenameDao.getSessionFactory().openSession();
        List<Sample_Entity> samples = session.createQuery("from Sample_Entity").list();
        session.close();
        return samples;
    }
    
    public List<String> getSamplenames()
    {
        Session session = filenameDao.getSessionFactory().openSession();
        List<String> samplenames = session.createQuery("select s.sampleId from Sample_Entity s").list();
        session.close();
        return samplenames;
    }
    
    public Sample_Pair_Entity getSamplePair(FilePair samplePairKey)
    {
        Session session = filenameDao.getSessionFactory().openSession();
        Sample_Pair_Entity pair = (Sample_Pair_Entity) session.get(Sample_Pair_Entity.class, samplePairKey);
        if(pair == null)
        {
            Sample_Entity ref = (Sample_Entity) session.get(Sample_Entity.class, samplePairKey.getReference());
            Sample_Entity obs = (Sample_Entity) session.get(Sample_Entity.class, samplePairKey.getObserved());
            if(ref == null || obs == null)
            {
                return null;
            }
            pair = new Sample_Pair_Entity(ref, obs);
            session.save(pair);
        }
        session.close();
        return pair;
    }
    
    /**
     * Generate all possible pairings between the given replicates
     *
     * @param fileIDs
     * @return
     */
    public List<FilePair> getReplicatePairs(List<String> fileIDs) {
        Session session = filenameDao.getSessionFactory().openSession();
        List<Sample_Entity> samples = session.createQuery("from Sample_Entity")
                .list();
        List<FilePair> pairs = new ArrayList<>();
        for (Sample_Entity sample : samples) {
            List<Filename_Entity> files = sample.getFilenames();
            for (int i = 0; i < files.size() - 1; i++) {
                Filename_Entity ref = files.get(i);
                for (int j = i + 1; j < files.size(); j++) {
                    Filename_Entity obs = files.get(j);
                    if(fileIDs.contains(ref.getFileID()) && fileIDs.contains(obs.getFileID()))
                        pairs.add(new FilePair(ref.getFileID(), obs.getFileID()));
                }
            }
        }

        session.close();
        return pairs;
    }
    
    /**
     * Generate all possible pairings between replicates
     * @return 
     */
    public List<FilePair> getAllReplicatePairs()
    {
        Session session = filenameDao.getSessionFactory().openSession();
        List<Sample_Entity> samples = session.createQuery("from Sample_Entity")
                .list();
        List<FilePair> pairs = new ArrayList<>();
        for(Sample_Entity sample : samples)
        {
            List<Filename_Entity> files = sample.getFilenames();
            for(int i=0; i<files.size()-1; i++)
            {
                Filename_Entity ref = files.get(i);
                for(int j=i+1; j<files.size(); j++)
                {
                    Filename_Entity obs = files.get(j);
                    pairs.add(new FilePair(ref.getFileID(), obs.getFileID()));
                }
            }
        }
        
        session.close();
        return pairs;
    }
    
    public void setAllOffsets(double offset)
    {
        Session session = filenameDao.getSessionFactory().openSession();
        session.createSQLQuery("UPDATE FILENAMES SET Expression_Offset=" + offset).executeUpdate();
        session.close();
    }
    
    public String getFilepathFromSQL(String query)
    {

        Session session = filenameDao.getSessionFactory().openSession();
        List<String> files = session.createSQLQuery(query).list();
        session.close();
        return files.get(0);
    }

    public void printSequenceRelationships()
    {
        Session session = filenameDao.getSessionFactory().openSession();

        ScrollableResults files = session.getNamedQuery("@HQL_GET_ALL_FILENAMES")
                .scroll(ScrollMode.FORWARD_ONLY);

        while (files.next())
        {
            Filename_Entity get = (Filename_Entity) files.get(0);
            //Hibernate.initialize(get.getSequenceFilenameRelationships());
            Set<Sequence_Entity> sequenceFilenameRelationships = get.getSequenceFilenameRelationships();
            
            
            System.out.println("Name: " + get.getFilename() + " Path: " + get.getFilePath());
            for(Sequence_Entity s_e : sequenceFilenameRelationships)
                System.out.println("seq" + s_e.getRNA_Sequence());

        }
        session.close();
    }
    
}
