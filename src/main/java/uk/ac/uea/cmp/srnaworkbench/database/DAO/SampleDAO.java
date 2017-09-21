package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Entity;

/**
 *
 * @author Matthew
 */
@Repository("SampleDAO")
public class SampleDAO extends GenericDaoImpl<Sample_Entity, String> {
    
    @Autowired
    public SampleDAO(SessionFactory sf){
        super(sf);
    }

    @Override
    protected Class<Sample_Entity> getEntityClass()
    {
        return Sample_Entity.class;
    }
}