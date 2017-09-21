/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Entity;

/**
 *
 * @author Matt
 */
@Repository("ExpressionDAO")
public class ExpressionDAO extends GenericDaoImpl<Expression_Entity, Long>
{
    @Autowired
    public ExpressionDAO(SessionFactory sf){
        super(sf);
    }

    @Override
    protected Class<Expression_Entity> getEntityClass()
    {
        return Expression_Entity.class;
    }
    
}
