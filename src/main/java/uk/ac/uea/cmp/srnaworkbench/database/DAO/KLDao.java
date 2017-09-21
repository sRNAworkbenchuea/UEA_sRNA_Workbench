package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.KL_Entity;

/**
 *
 * @author Matthew
 */
@Repository("KL_Values")
public class KLDao extends GenericDaoImpl<KL_Entity, KL_Entity.Id> {
    @Autowired
    public KLDao(SessionFactory sf)
    {
        super(sf);
    }
    @Override
    protected Class getEntityClass() {
        return KL_Entity.class;
    }
    
}
