/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author w0445959
 */
public interface GenericService<T, PK extends Serializable>
{
    public T findById(PK ID);

    public List<T> findAll();

    public void save(T presistentObject);

    public void saveOrUpdate(T transient_presistent_Object);

    public void update(T transientObject);

    public void delete(T transient_presistent_Object);

    public void shutdown();
}
