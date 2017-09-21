/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction;

import java.io.Serializable;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDao;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;

/**
 *
 * @author w0445959
 */
@Transactional
public abstract class GenericDaoImpl  <T, PK extends Serializable> 
    implements GenericDao<T, PK> {
    
    SessionFactory sf = null;
    
    @SuppressWarnings("unchecked")
    protected GenericDaoImpl(SessionFactory sf)
    {
        //sf.getCurrentSession().setFlushMode(FlushMode.ALWAYS);
        //setSessionFactory(sf);
        this.sf = sf;
        
        
        //getHibernateTemplate().setFetchSize(1);
        //getHibernateTemplate().setAlwaysUseNewSession(true);
        //getHibernateTemplate().setMaxResults(1);
        //getHibernateTemplate().setCacheQueries(false);
        //getHibernateTemplate().getSessionFactory().openSession().setFlushMode(FlushMode.ALWAYS);
//        System.out.println("fetch size: " + getHibernateTemplate().getFetchSize());
//        System.out.println("result size: " + getHibernateTemplate().getMaxResults());

        
    }

    @Override
    @SuppressWarnings("unchecked")
    public PK create(T newInstance)
    {
        Session openSession = sf.openSession();
        PK temp = (PK)openSession.save(newInstance);
        openSession.flush();
        openSession.close();
        return temp;
        
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read(PK id)
    {
        Session openSession = sf.openSession();
        Class<T> entityClass = getEntityClass();
        T read = (T) openSession.get(entityClass, id);
        openSession.flush();
        
        openSession.close();
        //getHibernateTemplate().flush();
        //getHibernateTemplate().getSessionFactory().close();
        return read;
    }
    
    @Override
    public void update(T transientObject)
    {
        Session openSession = sf.openSession();
        openSession.update(transientObject);
        openSession.flush();
        openSession.close();
    }
    
    public T merge(T transientObject)
    {
        Session openSession = sf.openSession();
        T temp = (T)openSession.merge(transientObject);
        openSession.flush();
        openSession.close();
        return temp;
    }
    
    @Override
    public void delete(T persistentObject)
    {
        Session openSession = sf.openSession();
        openSession.delete(persistentObject);
        openSession.flush();
        openSession.close();
    }
    
    

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll()
    {
        Session openSession = sf.openSession();
        List list = openSession.createCriteria(getEntityClass()).list();
        openSession.flush();
        openSession.close();
        return list;
    }
    
    protected List<T> findByCriteria(Criterion... criterion) {
        Session openSession = sf.openSession();
        Criteria crit = openSession.createCriteria(getEntityClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        openSession.flush();
        openSession.close();
        return crit.list();
    }
    
    public List<T> findByCriteriaOrdered( Order order, Criterion... criterion) {
        Session openSession = sf.openSession();
        Criteria crit = openSession.createCriteria(getEntityClass());
        
        //getEntityClass().
        
        for (Criterion c : criterion) {
            crit.add(c);
        }
        crit.addOrder(order);
        openSession.flush();
        List<T> critList = (List<T>)crit.list();
        openSession.close();
        return critList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAllByProperty(String propertyName, Object value)
    {
//        DetachedCriteria criteria = createDetachedCriteria();
//        criteria.add(Restrictions.eq(propertyName, value));
//        return findByCriteria(criteria);
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    public List<T> findByExample(T object)
//    {
//        List<T> resultList = sf.getCurrentSession().findByExample(object, 0, 1);
//        return resultList;
//    }
//
//    public List<T> findByExample(T object, int firstResult, int maxResults)
//    {
//        List<T> resultList = sf.getCurrentSession().findByExample(object,
//                firstResult, maxResults);
//        return resultList;
//    }
    
    /**
     * Retrieve the common criteria where Sequence_Entity is retrieved
     * with alias to aligned sequences (so only aligned sequences are used)
     * @return 
     */
    public DetachedCriteria getAlignedSequencesCriteria()
    {
        return DetachedCriteria.forClass(Sequence_Entity.class)
                .createAlias("unique_sequence", "useq")
                .createAlias("useq.alignedSequenceRelationships", "aseq");               
    }

    @Override
    public void createOrUpdate(T transientObject)
    {
        Session openSession = sf.openSession();
        openSession.saveOrUpdate(transientObject);
        openSession.flush();
        openSession.close();
    }
    
    public void flush()
    {
        Session openSession = sf.openSession();
        openSession.flush();
        openSession.close();
        
        
    }
    
    public void clear()
    {
        Session openSession = sf.openSession();
        openSession.clear();
        openSession.close();
        
        
    }

    protected abstract Class<T> getEntityClass();

    protected DetachedCriteria createDetachedCriteria()
    {
        return DetachedCriteria.forClass(getEntityClass());
    }

    @Override
    public void shutdown()
    {
        int executeUpdate = sf.openSession().createSQLQuery("SHUTDOWN").executeUpdate();
        System.out.println("Shutdown operation: " + executeUpdate);

    }
 
    public SessionFactory getSessionFactory()
    {
        return this.sf;
    }
    
}