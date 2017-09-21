/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.exceptions;

/**
 *
 * @author Matt
 */
public class WorkflowException extends RuntimeException{
    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
