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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Reference_Sequence_Set_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.EmptyTableException;

/**
 *
 * @author Matthew
 */
@Repository("ReferenceSequenceSetDAO")
public class ReferenceSequenceSetDAO extends GenericDaoImpl<Reference_Sequence_Set_Entity, String>{

    @Autowired
    public ReferenceSequenceSetDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<Reference_Sequence_Set_Entity> getEntityClass() {
        return Reference_Sequence_Set_Entity.class;
    }
    
    public Reference_Sequence_Set_Entity getHighestPriority() throws EmptyTableException{
        Session session = getSessionFactory().openSession();
        Reference_Sequence_Set_Entity reference = (Reference_Sequence_Set_Entity) session.createCriteria(Reference_Sequence_Set_Entity.class)
                .addOrder(Order.desc("referencePriority")).setMaxResults(1).setFetchMode("types", FetchMode.JOIN).uniqueResult();
        session.close();
        if(reference == null)
            throw new EmptyTableException("No reference sets have been added to the table, so a highest priority could not be found");
        return reference;
    }
    
}
