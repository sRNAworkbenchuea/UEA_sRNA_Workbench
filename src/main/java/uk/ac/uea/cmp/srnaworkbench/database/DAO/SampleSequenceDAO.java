package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sample_Sequence_Entity;

/**
 *
 * @author Matthew
 */
@Repository("SampleSequenceDAO")
public class SampleSequenceDAO extends GenericDaoImpl<Sample_Sequence_Entity, Long> {
    
    @Autowired
    public SampleSequenceDAO(SessionFactory sf){
        super(sf);
    }

    @Override
    protected Class<Sample_Sequence_Entity> getEntityClass()
    {
        return Sample_Sequence_Entity.class;
    }
}