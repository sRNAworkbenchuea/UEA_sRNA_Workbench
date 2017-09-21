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
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Filename_Window_Entity;

/**
 *
 * @author Matthew
 */
@Repository("AlignedFilenameWindowDAO")
public class AlignedFilenameWindowDAO extends GenericDaoImpl<Aligned_Filename_Window_Entity, Aligned_Filename_Window_Entity.Id> {
    
    @Autowired
    public AlignedFilenameWindowDAO(SessionFactory sf)
    {
        super(sf);
    }
    @Override
    protected Class<Aligned_Filename_Window_Entity> getEntityClass() {
        return Aligned_Filename_Window_Entity.class;
    }
    
}
