/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.TranscriptDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Transcript_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author Chris Applegate
 */
@Service("TranscriptService")
public class TranscriptServiceImpl implements GenericService<Transcript_Entity, Long> {

    @Autowired
    private TranscriptDAOImpl transcriptDao;

    @Override
    public Transcript_Entity findById(Long id) {
        return transcriptDao.read(id);
    }

    public List<Transcript_Entity> findByDescription(String desc) {
        Session session = transcriptDao.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Transcript_Entity.class);
        criteria.add(Restrictions.eq("gene", desc));
        List<Transcript_Entity> list = criteria.list();
        session.close();
        return list;
    }

    @Override
    public List<Transcript_Entity> findAll() {
        return transcriptDao.findAll();
    }

    @Override
    public synchronized void save(Transcript_Entity presistentObject) {
        transcriptDao.create(presistentObject);
    }

    @Override
    public synchronized void saveOrUpdate(Transcript_Entity transient_presistent_Object) {
        transcriptDao.createOrUpdate(transient_presistent_Object);
    }

    @Override
    public synchronized void update(Transcript_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void delete(Transcript_Entity transient_presistent_Object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     public void parseTranscriptome(Path transcriptomePath) throws FileNotFoundException, Exception {
        // System.out.println("parsing transcriptome");
         Scanner in = new Scanner(transcriptomePath.toFile());
        String header = "";
        String transcript = "";
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.startsWith(">")) {
                if (!header.equals("") && !transcript.equals("")) {
                    saveTranscript(header, transcript);
                }
                header = line.substring(1);
                transcript = "";
               
            } else {
                line = line.replaceAll("[^TGACUtgacu]", "N");
                transcript += line;
            }
        }
        if (!header.equals("") && !transcript.equals("")) {
            saveTranscript(header, transcript);
        }
        in.close();
        // System.out.println("complete!");
    }

    public void saveTranscript(String header, String sequence) throws Exception {
        List<Transcript_Entity> transcripts = findByDescription(header);
        Transcript_Entity t;
        if (transcripts.isEmpty()) {
            t = new Transcript_Entity(header);
            t.setSequenceLength(sequence.length());
        } else if (transcripts.size() == 1) {
            t = transcripts.get(0);
        } else {
            throw new Exception("Duplicate Transcript Exception");
        }
        save(t);
    }
    
}
