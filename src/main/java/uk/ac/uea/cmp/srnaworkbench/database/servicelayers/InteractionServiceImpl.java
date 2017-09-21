/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.InteractionDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author Chris Applegate
 */
@Service("InteractionService")
public class InteractionServiceImpl implements GenericService<Interaction_Entity, Long> {

    @Autowired
    private InteractionDAOImpl interactionDao;

    @Override
    public Interaction_Entity findById(Long id) {
        return interactionDao.read(id);
    }

    @Override
    public List<Interaction_Entity> findAll() {
        return interactionDao.findAll();
    }

    @Override
    public synchronized void save(Interaction_Entity presistentObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void saveOrUpdate(Interaction_Entity transient_presistent_Object) {
        interactionDao.createOrUpdate(transient_presistent_Object);
    }

    @Override
    public synchronized void update(Interaction_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void delete(Interaction_Entity transient_presistent_Object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Interaction_Entity> getInteractionsOrderByGene() {
        Session session = this.interactionDao.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Interaction_Entity.class);
        criteria.addOrder(Order.asc("gene"));
        return criteria.list();
    }

    public List<Interaction_Entity> getInteractionsOrderByGene(String predictorID) {
        Session session = this.interactionDao.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Interaction_Entity.class);
        criteria.add(Restrictions.eq("predictor", predictorID));
        criteria.addOrder(Order.asc("gene"));
        return criteria.list();
    }
    
    public List<String> getPredictorList() {
        Session session = this.interactionDao.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Interaction_Entity.class);
        criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("predictor")));
        return criteria.list();
    }

    public List<Map<String, Object>> executeGenericSQL(String sql) {
        // open a session
        org.hibernate.Session session = this.interactionDao.getSessionFactory().openSession();
        // run generic sql query
        List<Map<String, Object>> result = DatabaseWorkflowModule.executeGenericSQLMapped(session, sql);
        // close the session
        if (session.isOpen()) {
            session.close();
        }
        // return the result
        return result;
    }
}
