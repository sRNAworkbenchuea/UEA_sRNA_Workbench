/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.data.mirbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ezb11yfu
 */
public class OrganismMap 
{
    private File infile;
    private Map<String, OrganismEntry> orgmap;
    
    public OrganismMap()
    {
        
    }
    
    public OrganismMap(File infile) throws IOException
    {
        this.infile = infile;
        this.orgmap = processFile();
    }
    
    private Map<String, OrganismEntry> processFile() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.infile)));

        HashMap<String,OrganismEntry> orgmap = new HashMap<String,OrganismEntry>();
        String line = null;
        while((line = br.readLine()) != null)
        {
            OrganismEntry oe = OrganismEntry.parse(line);
            
            orgmap.put(oe.getOrgCode(), oe);
        }    
        
        return orgmap;
    }
    
    public Map<String, OrganismEntry> getMap()
    {
        return this.orgmap;
    }
      
    public Map<String, OrganismEntry> organismsInFamily(String family)
    {
        if (family == null)
            return this.orgmap;
        
        HashMap<String, OrganismEntry> fam_map = new HashMap<String, OrganismEntry>();
        
        for(String code : this.orgmap.keySet())
        {
            OrganismEntry oe = this.orgmap.get(code);
            
            for(String fam : oe.getFamilyTree())
            {
                if (fam.equalsIgnoreCase(family))
                {
                    fam_map.put(code, oe);
                }
            }
        }
        
        return fam_map;
    }
}
