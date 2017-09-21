/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author w0445959
 */
public interface GenericDao <T, PK extends Serializable> {

    /** Persist the newInstance object into database */
    PK create(T newInstance);

    /** Retrieve an object that was previously persisted to the database using
     *   the indicated id as primary key
     */
    T read(PK id);

    /** Save changes made to a persistent object.  */
    void update(T transientObject);

    /** Remove an object from persistent storage in the database */
    void delete(T persistentObject);
    
    void createOrUpdate( T transient_presistent_Object );
    
    void shutdown();

    List<T> findAll();

    List<T> findAllByProperty(String propertyName, Object value);
}
