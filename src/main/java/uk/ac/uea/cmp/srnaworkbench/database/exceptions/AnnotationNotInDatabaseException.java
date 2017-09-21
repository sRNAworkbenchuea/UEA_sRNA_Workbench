/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.exceptions;


public class AnnotationNotInDatabaseException extends DatabaseAnnotationException {

    public AnnotationNotInDatabaseException(String id) {
        super("Element with id " + id + "is not in the database and so can not be modified");
    }
    
}
