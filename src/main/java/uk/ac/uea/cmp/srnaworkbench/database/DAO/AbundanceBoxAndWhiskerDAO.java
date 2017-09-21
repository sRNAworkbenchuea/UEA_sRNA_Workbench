package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.AbundanceBoxAndWhisker_Entity;

/**
 *
 * @author matt
 */
@Repository("AbundanceBoxAndWhiskerDAO")
public class AbundanceBoxAndWhiskerDAO extends GenericDaoImpl <AbundanceBoxAndWhisker_Entity, Long> {

    @Autowired
    public AbundanceBoxAndWhiskerDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<AbundanceBoxAndWhisker_Entity> getEntityClass() {
        return AbundanceBoxAndWhisker_Entity.class;
    }
    
}
