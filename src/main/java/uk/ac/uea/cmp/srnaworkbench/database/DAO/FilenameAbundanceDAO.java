package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import java.util.Collection;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Abundance_Entity;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;

/**
 *
 * @author matt
 */
@Repository("FilenameAbundnanceDAO")
public class FilenameAbundanceDAO extends GenericDaoImpl<Filename_Abundance_Entity, Filename_Abundance_Entity.NormalisedFileID> {

    @Autowired
    public FilenameAbundanceDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<Filename_Abundance_Entity> getEntityClass() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Retrieve the mean total raw abundance over all files
     * @return 
     */
    public double getMeanTotalAbundances()
    {
        Session session = this.getSessionFactory().openSession();
        double avgCount = (double) session.createCriteria(Filename_Abundance_Entity.class)
                .setProjection(Projections.avg("totalAbundance")).uniqueResult();
        session.close();
        return avgCount;
    }
    
}
