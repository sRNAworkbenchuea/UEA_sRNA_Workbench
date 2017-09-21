package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Jaccard_Entity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;

/**
 *
 * @author mka07yyu
 */
@Repository("JaccardDAO")
public class JaccardDAO extends GenericDaoImpl<Jaccard_Entity, Long> {

    @Autowired
    public JaccardDAO(SessionFactory sf)
    {
        super(sf);
    }
    @Override
    protected Class<Jaccard_Entity> getEntityClass() {
        return Jaccard_Entity.class;
    }

 
    
}
