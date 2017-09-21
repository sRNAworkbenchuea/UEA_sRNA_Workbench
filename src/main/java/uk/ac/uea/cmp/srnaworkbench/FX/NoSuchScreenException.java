/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.FX;

/**
 *
 * @author Matt
 */
public class NoSuchScreenException extends RuntimeException {

    public NoSuchScreenException(String name) {
        super("Screen with name " + name + " does not exist");
    }
    
}
