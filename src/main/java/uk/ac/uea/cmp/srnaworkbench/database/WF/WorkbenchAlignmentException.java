/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.WF;

import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkflowException;
import org.hibernate.exception.ConstraintViolationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;

/**
 *
 * @author Matt
 */
class WorkbenchAlignmentException extends WorkflowException {

    public WorkbenchAlignmentException(String message, Exception ex) {
        super(message, ex);
    }
    
}
