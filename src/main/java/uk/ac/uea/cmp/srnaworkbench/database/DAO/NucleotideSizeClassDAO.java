package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Nucleotide_Size_Class_Entity;

/**
 *
 * @author mka07yyu
 */
@Repository("NucleotideSizeClass")
public class NucleotideSizeClassDAO extends GenericDaoImpl<Nucleotide_Size_Class_Entity, Long> {

    @Autowired
    public NucleotideSizeClassDAO(SessionFactory sf)
    {
        super(sf);
    }
    
    @Override
    protected Class<Nucleotide_Size_Class_Entity> getEntityClass() {
        return Nucleotide_Size_Class_Entity.class;
    }
    
}
