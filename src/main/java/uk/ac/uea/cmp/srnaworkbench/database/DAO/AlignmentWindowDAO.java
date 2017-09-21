package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Alignment_Window_Entity;

/**
 *
 * @author Matthew
 */
@Repository("AlignmentWindowDAO")
public class AlignmentWindowDAO extends GenericDaoImpl<Alignment_Window_Entity, Alignment_Window_Entity.Id>{

    @Autowired
    public AlignmentWindowDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class getEntityClass() {
        return Alignment_Window_Entity.class;
    }
    
}
