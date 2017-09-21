/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Transcript_Entity;

/**
 *
 * @author Chris Applegate
 */
@Repository("TranscriptDAO")
public class TranscriptDAOImpl extends GenericDaoImpl<Transcript_Entity, Long> {

   // public static String transcriptID;

    @Autowired
    public TranscriptDAOImpl(SessionFactory sf) {
        super(sf);
    }
    
    @Override
    protected Class<Transcript_Entity> getEntityClass() {
        return Transcript_Entity.class;
    }
}
