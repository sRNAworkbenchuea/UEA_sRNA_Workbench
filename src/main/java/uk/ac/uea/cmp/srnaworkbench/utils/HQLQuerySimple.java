/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

/**
 *
 * @author chris applegate
 */
public class HQLQuerySimple extends HQLQuery {

    private final Class from;
    private final String fromAlias;

    public HQLQuerySimple(Class from) {
        super();
        this.from = from;
        this.fromAlias = "A";
    }
    
    public HQLQuerySimple(Class from, String alias) throws HQLFormatException {
        super();
        this.from = from;
        if (alias.contains(" ")) {
            throw new HQLFormatException("HQL aliases must not contain spaces: " + alias);
        }
        this.fromAlias = alias;
    }

    public HQLQuerySimple(final HQLQuerySimple query) {
        super(query);
        this.from = query.from;
        this.fromAlias = query.fromAlias;
    }

    public String getFromAlias() {
        return this.fromAlias;
    }

    public Class getFromClass() {
        return this.from;
    }

    @Override
    public String evaluateSelect() {
        return "";
    }
    
    @Override
    public String evaluateFrom() {
        return " FROM " + this.from.getName() + " " + this.fromAlias;
    }
    
}
