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
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;

/**
 *
 * @author Chris Applegate
 */
@Repository("PrecursorDAO")
public class PrecursorDAOImpl extends GenericDaoImpl<Precursor_Entity, Long> {

    @Autowired
    public PrecursorDAOImpl(SessionFactory sf) {
        super(sf);
    }

    @Override
    protected Class<Precursor_Entity> getEntityClass() {
        return Precursor_Entity.class;
    }
}
