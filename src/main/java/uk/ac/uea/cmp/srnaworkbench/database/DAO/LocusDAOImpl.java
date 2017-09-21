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
import uk.ac.uea.cmp.srnaworkbench.database.entities.SRNA_Locus_Entity;

/**
 *
 * @author w0445959
 */
@Repository("LocusDAO")
public class LocusDAOImpl  extends GenericDaoImpl<SRNA_Locus_Entity, Long>
{
    @Autowired
    public LocusDAOImpl(SessionFactory sf)
    {
        super(sf);
    }
    
    //public void createWithNoSession(SRNA_Locus)
    
    @Override
    protected Class<SRNA_Locus_Entity> getEntityClass() {
        return SRNA_Locus_Entity.class;
    }
}
