package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Entity;

/**
 *
 * @author mka07yyu
 */
@Repository("GFFDAO")
public class GFFDAO extends GenericDaoImpl<GFF_Entity, Long> {

    @Autowired
    public GFFDAO(SessionFactory sf) {
        super(sf);
    }
    
    @Override
    protected Class<GFF_Entity> getEntityClass() {
        return GFF_Entity.class;
    }
    
    
    
}
