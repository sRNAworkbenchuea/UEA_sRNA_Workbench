package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.AbundanceBoxAndWhisker_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MBoxAndWhisker_Entity;

/**
 *
 * @author matt
 */
@Repository("MBoxAndWhiskerDAO")
public class MBoxAndWhiskerDAO extends GenericDaoImpl<MBoxAndWhisker_Entity, Long> {

    @Autowired
    public MBoxAndWhiskerDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<MBoxAndWhisker_Entity> getEntityClass() {
        return MBoxAndWhisker_Entity.class;
    }
    
}
