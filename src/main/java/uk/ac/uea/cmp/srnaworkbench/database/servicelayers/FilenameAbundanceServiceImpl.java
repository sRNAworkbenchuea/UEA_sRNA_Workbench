
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.util.List;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.FilenameAbundanceDAO;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Filename_Abundance_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author matt
 */
@Service("FilenameAbundanceService")
public class FilenameAbundanceServiceImpl implements GenericService<Filename_Abundance_Entity, Filename_Abundance_Entity.NormalisedFileID>{
    @Autowired
    FilenameAbundanceDAO fileAbundanceDao;
    
    @Override
    public Filename_Abundance_Entity findById(Filename_Abundance_Entity.NormalisedFileID ID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Filename_Abundance_Entity> findAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void save(Filename_Abundance_Entity presistentObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveOrUpdate(Filename_Abundance_Entity transient_presistent_Object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(Filename_Abundance_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(Filename_Abundance_Entity transient_presistent_Object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void printTable()
    {
        Session session = this.fileAbundanceDao.getSessionFactory().openSession();
        ScrollableResults results = session.createCriteria(Filename_Abundance_Entity.class).scroll(ScrollMode.FORWARD_ONLY);
        
        while(results.next())
        {
            Filename_Abundance_Entity fe = (Filename_Abundance_Entity) results.get(0);
            System.out.println(fe.toString());
        }
        session.close();
    }
    
}
