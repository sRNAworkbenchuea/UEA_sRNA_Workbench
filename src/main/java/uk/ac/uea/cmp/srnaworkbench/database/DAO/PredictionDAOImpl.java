/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;

/**
 *
 * @author Chris Applegate
 */
@Repository("PredictionDAO")
public class PredictionDAOImpl extends GenericDaoImpl<Prediction_Entity, Long> {

    @Autowired
    public PredictionDAOImpl(SessionFactory sf) {
        super(sf);
    }

    @Override
    protected Class<Prediction_Entity> getEntityClass() {
        return Prediction_Entity.class;
    }
    
    public void createOrUpdate(Prediction_Entity transientObject, Session openSession)
    {
        //Session openSession = sf.openSession();
        openSession.saveOrUpdate(transientObject);
        openSession.flush();
       // openSession.close();
    }
}
