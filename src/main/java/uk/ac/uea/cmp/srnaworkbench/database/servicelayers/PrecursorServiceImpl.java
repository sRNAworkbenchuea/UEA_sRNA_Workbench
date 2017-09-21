/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.PrecursorDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces.GenericService;

/**
 *
 * @author Chris Applegate + Matt Stocks
 */
@Service("PrecursorService")
public class PrecursorServiceImpl implements GenericService<Precursor_Entity, Long> {

    @Autowired
    private PrecursorDAOImpl precursorDao;

    @Override
    public Precursor_Entity findById(Long id) {
          return precursorDao.read(id);
    }

    @Override
    public List<Precursor_Entity> findAll() {
        return precursorDao.findAll();
    }

    @Override
    public synchronized void save(Precursor_Entity presistentObject)
    {
        precursorDao.create(presistentObject);
        precursorDao.flush();
    }

    @Override
    public synchronized void saveOrUpdate(Precursor_Entity transient_presistent_Object)
    {
        precursorDao.createOrUpdate(transient_presistent_Object);
        precursorDao.flush();
    }

    @Override
    public synchronized void update(Precursor_Entity transientObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void delete(Precursor_Entity transient_presistent_Object) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
