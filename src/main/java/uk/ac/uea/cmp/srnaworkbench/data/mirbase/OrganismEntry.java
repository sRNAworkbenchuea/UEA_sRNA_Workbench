/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ezb11yfu
 */
public class OrganismEntry
{
    private String org_code;
    private String binomial_latin_name;
    private ArrayList<String> family_tree;

    public OrganismEntry()
    {
        this("xxx", "xxx", null);
    }

    public OrganismEntry(String org_code, String binomial_latin_name, ArrayList<String> family_tree)
    {
        this.org_code = org_code;
        this.binomial_latin_name = binomial_latin_name;
        this.family_tree = family_tree;
    }
    
    public String getOrgCode()              {return this.org_code;}
    public String getBinomialLatinName()    {return this.binomial_latin_name;}
    public List<String> getFamilyTree()     {return this.family_tree;}

    public static OrganismEntry parse(String org_entry)
    {
        String[] parts = org_entry.split("\t");

        String code = parts[0];
        String name = parts[2];
        String[] branches = parts[3].split(";");
        ArrayList<String> tree = new ArrayList<String>();
        for(int i = 0; i < branches.length; i++)
        {
            tree.add(branches[i]);
        }

        return new OrganismEntry(code, name, tree);
    }
}
