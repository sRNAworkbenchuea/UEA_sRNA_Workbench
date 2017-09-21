/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.util.HashMap;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.Patman;
import uk.ac.uea.cmp.srnaworkbench.utils.patman.PatmanEntry;

/**
 *
 * @author Dan Mapleson
 */
final class TASMap extends HashMap<String, Patman>
{
    public TASMap()
    {
        super();
    }
    
    public Patman getTAS(String tas)
    {
        return this.get(tas);
    }
    
    public void addSRNA(String tas, PatmanEntry srna)
    {
        if (!this.containsKey(tas))
        {
            Patman sg = new Patman();
            sg.add(srna);
            this.put(tas, sg);
        }
        else
        {        
            this.get(tas).add(srna);
        }
    }
}
