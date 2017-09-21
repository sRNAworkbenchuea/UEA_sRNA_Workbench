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
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Entity;

/**
 *
 * @author w0445959
 */
@Repository("FilenameDAO")
public class FilenameDAOImpl extends GenericDaoImpl<Filename_Entity, String>
{
    
    @Autowired
    public FilenameDAOImpl(SessionFactory sf)
    {
        super(sf);
    }

    @Override
    protected Class<Filename_Entity> getEntityClass()
    {
        return Filename_Entity.class;
    }
    
    
//
//    public void generateTable()
//    {
//        Session session = getSessionFactory().openSession();
////        List records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
////                .addEntity(Sequence_Filename_Entity.class).list();
////       
//
//        Transaction tx = session.beginTransaction();
//        Filename_Entity f_e = new Filename_Entity();
//         tx.commit();
////            records = session.createSQLQuery("SELECT * FROM SEQUENCE_FILENAME_RELATIONSHIPS")
////                .addEntity(Sequence_Filename_Entity.class).list();
//
//        session.close();
//    }
    
}
