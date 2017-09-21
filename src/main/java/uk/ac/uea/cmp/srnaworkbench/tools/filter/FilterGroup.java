/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filter;

import java.util.ArrayList;

/**
 * A collection of @link{FilterStage} objects.  The FilterGroup can be referenced 
 * by an ID String.
 * @author Dan Mapleson
 */
public final class FilterGroup extends ArrayList<FilterStage>
{
    private String id;
    
    public FilterGroup()
    {
        this("");
    }
    
    public FilterGroup(String id)
    {
        super();
        
        this.id = id;
    }
    
    public String getID()       {return this.id;}
    public void setID(String id)    {this.id = id;}
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\"Read counts\"\n");
        sb.append("\"\",\"total\",\"distinct\"\n");

        for(FilterStage stage : this)
        {
            sb.append(stage.toString());
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
