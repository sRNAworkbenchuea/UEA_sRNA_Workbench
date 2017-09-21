/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author chris applegate
 */
public class HQLQueryComplex extends HQLQuery {
   
    private final List<Pair<String, String>> select; // pair: field, alias
    private final Map<String, Class> from; // key is alias, value is entity

    public HQLQueryComplex() {
        super();
        this.select = new LinkedList<>();
        this.from = new HashMap<>();
    }

    public HQLQueryComplex(final HQLQueryComplex query) {
        super(query);
        this.select = new LinkedList<>();
        this.from = new HashMap<>();
        for (Pair<String, String> field : query.select) {
            this.select.add(new Pair<>(field.getKey(), field.getValue()));
        }
        for (String key : query.from.keySet()) {
            this.from.put(key, query.from.get(key));
        }
    }

     public HQLQueryComplex(final HQLQuerySimple query) {
        super(query);
        this.select = new LinkedList<>();
        this.from = new HashMap<>();
        this.from.put(query.getFromAlias(), query.getFromClass());
    }
    
    
    public void addSelect(String field, String alias) throws HQLFormatException, HQLQueryLockedException {
        if (!this.locked) {
            if (alias.contains(" ")) {
                throw new HQLFormatException("HQL aliases must not contain spaces: " + alias);
            }
            select.add(new Pair<>(alias, field));
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    public void addFrom(Class c, String alias) throws HQLFormatException, HQLQueryLockedException {
        if (!this.locked) {
            if (alias.contains(" ")) {
                throw new HQLFormatException("HQL aliases must not contain spaces: " + alias);
            }
            if (!this.from.containsKey(alias)) {
                from.put(alias, c);
            } else {
                Logger.getLogger(WFQueryResultsTable.class.getName()).log(Level.WARNING, "Duplicate HQL Query Alias: {0}. Ignored requested addition.", alias);
            }
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    private String getFromList() {
        String str = " FROM ";
        int counter = 0;
        for (String key : this.from.keySet()) {
            if (counter++ > 0) {
                str += ", ";
            }
            str += String.format("%s %s", this.from.get(key).getName(), key);
        }
        return str;
    }

    public List<String> getSelectAliases() {
        List<String> aliasList = new LinkedList<>();
        for (Pair<String, String> field : this.select) {
            aliasList.add(field.getKey());
        }
        return aliasList;
    }

    private String getSelectList() {
        String str = "SELECT ";
        for (int i = 0; i < this.select.size(); i++) {
            if (i > 0) {
                str += ", ";
            }
            str += String.format("%s AS %s", this.select.get(i).getValue(), this.select.get(i).getKey());
        }
        return str;
    }

    public String getSelect(int index) {
        if (index >= 0 && index < this.select.size()) {
            return this.select.get(index).getKey();
        }
        return null;
    }

    @Override
    public String evaluateSelect() {
        return getSelectList();
    }

    @Override
    public String evaluateFrom() {
        return getFromList();
    }
    
}
