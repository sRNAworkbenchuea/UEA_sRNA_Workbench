/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.LFoldPrecursorDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.PrecursorDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.LFold_Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author keu13sgu
 */
@Service("LFoldPrecursorService")
public class LFoldPrecursorServiceImpl implements GenericService<LFold_Precursor_Entity, Long>{
    @Autowired
    private LFoldPrecursorDAOImpl precursorDao;

    @Override
    public LFold_Precursor_Entity findById(Long id) {
          return precursorDao.read(id);
    }

    @Override
    public List<LFold_Precursor_Entity> findAll() {
        return precursorDao.findAll();
    }

    @Override
    public synchronized void save(LFold_Precursor_Entity presistentObject)
    {
        precursorDao.create(presistentObject);
        precursorDao.flush();
    }

    @Override
    public synchronized void saveOrUpdate(LFold_Precursor_Entity transient_presistent_Object)
    {
        precursorDao.createOrUpdate(transient_presistent_Object);
        precursorDao.flush();
    }

    @Override
    public synchronized void update(LFold_Precursor_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void delete(LFold_Precursor_Entity transient_presistent_Object) {
        precursorDao.delete(transient_presistent_Object);
        precursorDao.flush();
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
