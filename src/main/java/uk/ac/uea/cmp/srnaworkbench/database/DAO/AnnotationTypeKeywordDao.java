/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Annotation_Type_Keyword_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Reference_Sequence_Set_Entity;

/**
 *
 * @author Matthew
 */
@Repository("AnnotationTypeDao")
public class AnnotationTypeKeywordDao extends GenericDaoImpl<Annotation_Type_Keyword_Entity, String>{

    @Autowired
    public AnnotationTypeKeywordDao(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<Annotation_Type_Keyword_Entity> getEntityClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    Annotation_Type_Keyword getHighestPriorityKeyword(Annotation_Type_Entity type)
//    {
//        Session session = getSessionFactory().openSession();
//        session.close();
//    }
    
}
