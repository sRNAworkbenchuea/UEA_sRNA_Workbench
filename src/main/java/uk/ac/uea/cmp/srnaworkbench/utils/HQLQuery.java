/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.LinkedList;
import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author chris applegate
 */
public abstract class HQLQuery {

    public static enum HQL_DIR {

        ASC, DESC, UNDEFINED
    };
   
    private final List<String> where;
    private final List<String> group;
    private final List<Pair<String, HQL_DIR>> order;
    protected boolean locked;

    public HQLQuery() {
        this.where = new LinkedList<>();
        this.group = new LinkedList<>();
        this.order = new LinkedList<>();
        this.locked = false;
    }

    public HQLQuery(final HQLQuery query) {
        this.where = new LinkedList<>();
        this.group = new LinkedList<>();
        this.order = new LinkedList<>();
        this.locked = false;

        for (String s : query.where) {
            this.where.add(s);
        }
        for (String s : query.group) {
            this.group.add(s);
        }
        for (Pair<String, HQL_DIR> o : query.order) {
            this.order.add(new Pair<>(o.getKey(), o.getValue()));
        }
    }

    public void addWhere(String s) throws HQLQueryLockedException {
        if (!this.locked) {
            where.add(String.format("(%s)", s));
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    public void addGroup(String s) throws HQLQueryLockedException {
        if (!this.locked) {
            group.add(s);
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    public void toggleOrder(String s) throws HQLQueryLockedException {
        if (!this.locked) {
            for (int i = 0; i < order.size(); i++) {
                Pair<String, HQL_DIR> field = this.order.get(i);
                if (field.getKey().equals(s)) {
                    HQL_DIR new_dir = HQL_DIR.UNDEFINED;
                    switch (field.getValue()) {
                        case DESC:
                            new_dir = HQL_DIR.ASC;

                            break;
                        case ASC:
                        case UNDEFINED:
                            new_dir = HQL_DIR.DESC;
                            break;
                    }
                    order.clear();
                    order.add(new Pair<>(s, new_dir));
                    return;
                }
            }
            order.clear();
            order.add(new Pair<>(s, HQL_DIR.DESC));
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    public HQL_DIR getOrder(String alias) {
        for (Pair<String, HQL_DIR> field : this.order) {
            if (field.getKey().equals(alias)) {
                return field.getValue();
            }
        }
        return HQL_DIR.UNDEFINED;
    }

    public void addOrder(String s, HQL_DIR dir) throws HQLQueryLockedException {
        if (!this.locked) {
            for (int i = 0; i < order.size(); i++) {
                Pair<String, HQL_DIR> field = this.order.get(i);
                if (field.getKey().equals(s)) {
                    order.set(i, new Pair<>(s, dir));
                    return;
                }
            }
            this.order.add(new Pair<>(s, dir));
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }

    private String getSeparatedList(List<String> array, String seperator) {
        String str = "";
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                str += seperator;
            }
            str += array.get(i);
        }
        return str;
    }



    private String getOrderList() {
        String str = " ORDER BY ";
        for (int i = 0; i < this.order.size(); i++) {
            if (i > 0) {
                str += ", ";
            }
            String dir;
            if (this.order.get(i).getValue() == HQL_DIR.ASC) {
                dir = "ASC";
            } else {
                dir = "DESC";
            }
            str += String.format("%s %s", this.order.get(i).getKey(), dir);
        }
        return str;
    }

    public void clearOrder() throws HQLQueryLockedException {
        if (!this.locked) {
            this.order.clear();
        }
        else
        {
            throw new HQLQueryLockedException("HQLQuery is locked!");
        }
    }


    public String eval() {
        String query = "";
        query += evaluateSelect();
        query += evaluateFrom();
        if (!where.isEmpty()) {
            query += String.format(" WHERE %s ", getSeparatedList(this.where, " AND "));
        }
        if (!group.isEmpty()) {
            query += String.format(" GROUP BY %s ", getSeparatedList(this.group, ", "));
        }
        if (!order.isEmpty()) {
            query += getOrderList();
        }
        return query;
    }

    public abstract String evaluateSelect();
    public abstract String evaluateFrom();

    public void lock() {
        this.locked = true;
    }

    
    
    public static class HQLQueryLockedException extends Exception {

        public HQLQueryLockedException(String string) {
        }
    }

    public static class HQLFormatException extends Exception {

        public HQLFormatException(String string) {
        }
    }
}
