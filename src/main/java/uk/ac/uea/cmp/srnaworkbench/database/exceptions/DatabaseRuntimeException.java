/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.exceptions;

/**
 * For Database exceptions that are unrecoverable
 * @author Matthew Beckers
 */
public class DatabaseRuntimeException extends RuntimeException{
    public DatabaseRuntimeException(String msg)
    {
        super(msg);
    }
    
}
