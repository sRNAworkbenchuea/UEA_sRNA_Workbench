/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.EmptyTableException;

/**
 * The type used by Annotation_Type_Entity is itself due to the ids implementation
 * being two Id annotations
 * @author mka07yyu
 */
@Repository("GFFTypeDAO")
public class AnnotationTypeDAO extends GenericDaoImpl<Annotation_Type_Entity, Annotation_Type_Entity.TypePK> {
    @Autowired
    public AnnotationTypeDAO(SessionFactory sf) {
        super(sf);
    }
    
    @Override
    protected Class<Annotation_Type_Entity> getEntityClass() {
        return Annotation_Type_Entity.class;
    }
    
    public Annotation_Type_Entity getHighestPriorityType(String referenceName) throws EmptyTableException {
        Session session = getSessionFactory().openSession();
        Annotation_Type_Entity type = (Annotation_Type_Entity) session.createCriteria(Annotation_Type_Entity.class)
                .add(Restrictions.eq("id.reference.referenceSetName", referenceName))
                .addOrder(Order.desc("priority")).setMaxResults(1)//.setFetchMode("keywords", FetchMode.JOIN)
                .uniqueResult();
        session.close();
        if(type == null)
            throw new EmptyTableException("No annotation types have been added to the table, so a highest priority could not be found");
        return type;
    }
    
}
