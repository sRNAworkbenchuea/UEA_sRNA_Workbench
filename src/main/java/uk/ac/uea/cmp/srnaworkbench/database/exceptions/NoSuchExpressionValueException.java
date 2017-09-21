/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.exceptions;

/**
 *
 * @author mka07yyu
 */
public class NoSuchExpressionValueException extends Exception{
    public NoSuchExpressionValueException(String msg)
    {
        super(msg);
    }
}
