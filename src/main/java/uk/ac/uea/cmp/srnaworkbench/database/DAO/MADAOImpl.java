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
import uk.ac.uea.cmp.srnaworkbench.database.entities.MA_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.MA_Entity.MA;

/**
 *
 * @author mka07yyu
 */
@Repository("MADAO")
public class MADAOImpl extends GenericDaoImpl <MA_Entity, MA> {

    @Autowired
    public MADAOImpl(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<MA_Entity> getEntityClass() {
        return MA_Entity.class;
    }
    
}
