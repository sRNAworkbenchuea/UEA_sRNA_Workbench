package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Size_Class_Entity;

/**
 *
 * @author matt
 */
@Repository
public class SizeClassDAO extends GenericDaoImpl<Size_Class_Entity, Long>{

    @Autowired
    public SizeClassDAO(SessionFactory sf) {
        super(sf);
    }
    
    @Override
    protected Class getEntityClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
