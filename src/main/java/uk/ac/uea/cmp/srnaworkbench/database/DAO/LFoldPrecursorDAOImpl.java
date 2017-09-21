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
import uk.ac.uea.cmp.srnaworkbench.database.entities.LFold_Precursor_Entity;


/**
 *
 * @author keu13sgu
 */
@Repository("LFoldPrecursorDAOImpl")
public class LFoldPrecursorDAOImpl extends GenericDaoImpl<LFold_Precursor_Entity, Long>{

    @Override
    protected Class<LFold_Precursor_Entity> getEntityClass() {
        return LFold_Precursor_Entity.class;
    }
    
    @Autowired
    public LFoldPrecursorDAOImpl(SessionFactory sf) {
        super(sf);
    }
    
}
