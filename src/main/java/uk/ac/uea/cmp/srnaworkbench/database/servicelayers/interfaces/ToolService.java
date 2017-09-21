/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.servicelayers.interfaces;

import com.fasterxml.jackson.core.JsonFactory;
import java.io.Serializable;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.TimeableTool;

/**
 *
 * @author matt
 */
public abstract class ToolService <T, PK extends Serializable> extends TimeableTool implements GenericService <T, PK>  {
   
    protected JsonFactory jsonFactory;
    public ToolService(final String toolName)
    {
        super(toolName);
        this.jsonFactory = DatabaseWorkflowModule.getInstance().getJsonFactory();
    }

    
    
    
}
