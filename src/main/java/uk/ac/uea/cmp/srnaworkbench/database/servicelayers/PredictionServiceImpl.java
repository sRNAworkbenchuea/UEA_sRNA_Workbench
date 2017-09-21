/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.PredictionDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author Chris Applegate + Matt Stocks
 */
@Service("PredictionService")
public class PredictionServiceImpl implements GenericService<Prediction_Entity, Long> {

    @Autowired
    private PredictionDAOImpl predictionDao;

    @Override
    public Prediction_Entity findById(Long id) {
        return predictionDao.read(id);
    }

    @Override
    public List<Prediction_Entity> findAll() {
        return predictionDao.findAll();
    }

    @Override
    public synchronized void save(Prediction_Entity presistentObject) {
        predictionDao.create(presistentObject);
        predictionDao.flush();
    }

   // @Override
    public synchronized void saveOrUpdate(Prediction_Entity transient_presistent_Object, Session session) {
        predictionDao.createOrUpdate(transient_presistent_Object, session);
        predictionDao.flush();
    }
    
    @Override
    public synchronized void saveOrUpdate(Prediction_Entity transient_presistent_Object) {
        predictionDao.createOrUpdate(transient_presistent_Object);
        predictionDao.flush();
    }
    
    public synchronized void merge(Prediction_Entity transient_presistent_Object) {
        predictionDao.merge(transient_presistent_Object);
        predictionDao.flush();
    }

    @Override
    public synchronized void update(Prediction_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void delete(Prediction_Entity transient_presistent_Object) {
        predictionDao.delete(transient_presistent_Object);
        predictionDao.flush();
    }
    
    public synchronized int deleteForMir(Aligned_Sequences_Entity s, Session session) {
        String hql = "delete PREDICTIONS s where s.mature = :mature";
        
        Query query = session.createQuery(hql);
        query.setParameter("mature", s);
        int result = query.executeUpdate();
        
        return result;
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /* public List<List<String>> executeGenericSQL(String sql, boolean displayFieldNames) {
     // open a session
     org.hibernate.Session session = this.predictionDao.getSessionFactory().openSession();
     // run generic sql query
     List<List<String>> result = DatabaseWorkflowModule.executeGenericSQL(session, sql, displayFieldNames);
     // close the session
     if (session.isOpen()) {
     session.close();
     }
     // return the result
     return result;
     }
     */
    /*public List<Prediction_Entity> executeSQL(WFQuery q) throws Exception {
     List<Prediction_Entity> rows = null;
     // open a session
     org.hibernate.Session session = this.predictionDao.getSessionFactory().openSession();
     try {
     // run the query
     SQLQuery query = session.createSQLQuery(q.evaluate());
     query.addEntity(Prediction_Entity.class);
     rows = query.list();
     } catch (Exception ex) {
     // close the session
     if (session.isOpen()) {
     session.close();
     }
     throw new Exception(ex);
     }
     // close the session
     if (session.isOpen()) {
     session.close();
     }
     return rows;
     }*/
    public List<Prediction_Entity> executeSQL(String sql)  {

        List<Prediction_Entity> rows = null;

        // open a session
        org.hibernate.Session session = this.predictionDao.getSessionFactory().openSession();
        
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

  
}
