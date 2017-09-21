/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.WF;

import java.util.LinkedHashMap;
import java.util.List;
import org.hibernate.transform.ResultTransformer;

/**
 *
 * @author chris
 */
public class OrderedResultTransformer implements ResultTransformer {

    public OrderedResultTransformer() {

    }
    LinkedHashMap<String, Object> map;

    @Override
    public Object transformTuple(Object[] os, String[] strings) {

        if (strings == null) {
            map = new LinkedHashMap<>(1);
            for (int i = 0; i < os.length; i++) {
                map.put("object_" + i, os[i]);
            }
        }
        else
        {
            map = new LinkedHashMap<>(strings.length);
            for (int i = 0; i < os.length; i++) {
                map.put(strings[i], os[i]);
            }
        }
        return map;
    }

    @Override
    public List transformList(List list) {
        return list;
    }

}
