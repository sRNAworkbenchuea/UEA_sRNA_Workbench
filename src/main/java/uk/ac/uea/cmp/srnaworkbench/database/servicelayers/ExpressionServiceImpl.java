/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

/**
 *
 * @author w0445959
 */
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.ExpressionDAO;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 * Implements the business methods for the Sequence service
 *
 */
@Service("ExpressionService")
public class ExpressionServiceImpl implements GenericService<Expression_Entity, Long> {

    @Autowired
    private ExpressionDAO expressionDao;

    @Override
    public Expression_Entity findById(Long id) {
        return expressionDao.read(id);
    }

    @Override
    public List<Expression_Entity> findAll() {
        return expressionDao.findAll();
    }

    @Override
    public synchronized void save(Expression_Entity presistentObject) {
        expressionDao.create(presistentObject);
    }

    @Override
    public synchronized void saveOrUpdate(Expression_Entity transient_presistent_Object) {
        expressionDao.createOrUpdate(transient_presistent_Object);
    }

    @Override
    public synchronized void update(Expression_Entity transientObject) {
        expressionDao.update(transientObject);
    }
    
    @Override
    public synchronized void delete(Expression_Entity transient_presistent_Object) {
        expressionDao.delete(transient_presistent_Object);
    }

    @Override
    public void shutdown() {
        expressionDao.shutdown();
    }

    public List<NormalisationType> getNormalisationTypes() {
        Session session = this.expressionDao.getSessionFactory().openSession();
        List<NormalisationType> normalisationTypes = session.createQuery("SELECT DISTINCT E.normType FROM Expression_Entity E").list();     
        if (session.isOpen()) {
            session.close();
        }
        //session.close();
        return normalisationTypes;
    }

}
