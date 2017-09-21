package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_CI_Entity;

/**
 *
 * @author Matthew
 */
@Repository("ExpressionCiDAO")
public class ExpressionCiDAO extends GenericDaoImpl<Expression_CI_Entity, Long> {
    
    @Autowired
    public ExpressionCiDAO(SessionFactory sf){
        super(sf);
    }

    @Override
    protected Class<Expression_CI_Entity> getEntityClass()
    {
        return Expression_CI_Entity.class;
    }
}