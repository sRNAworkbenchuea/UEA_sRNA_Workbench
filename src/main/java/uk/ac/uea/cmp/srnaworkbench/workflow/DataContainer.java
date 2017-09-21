/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NonOverwritableException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;

/**
 *
 * @author Chris Applegate
 * @param <T> Represents a I/O data container of generic data type T
 */
// this class either holds data or points to data
public final class DataContainer<T> {

    // reference to connected data container (if this data container does not hold actual data)
    private DataContainer<T> pointer;
    // actual data (if this data container does not point to another data container)
    private final T data;
    // compatibility key for data container (to determine if connections to other data containers are permitted)
    private final CompatibilityKey compatibilityKey;

    /*
     * constructor
     * @param compatibilityKey: compatibility key for data container
     */
    public DataContainer(CompatibilityKey compatibilityKey) {
        // initialise instance variables
        this.pointer = null;
        this.data = null;
        this.compatibilityKey = compatibilityKey;
    }

    /* 
     * constructor
     * @param compatibilityKey: compatibility key for data container
     * @param data: the actual and final data that this data container will store
     */
    public DataContainer(CompatibilityKey compatibilityKey, T data) {
        // initialise instance variables
        this.pointer = null;
        this.data = data;
        this.compatibilityKey = compatibilityKey;
    }

    /* 
     * constructor
     * @param compatibilityKey: compatibility key for data container
     * @param pointer: a reference to another data container that holds data
     */
    public DataContainer(CompatibilityKey compatibilityKey, DataContainer<T> pointer) {
        this.pointer = pointer;
        this.data = null;
        this.compatibilityKey = compatibilityKey;
    }

    /*
     * Returns the compatibility key of the data container
     */
    public final CompatibilityKey getCompatibiltyKey() {
        return this.compatibilityKey;
    }

    /*
     * Returns the data stored by this data container or follows the references until data is found
     */
    public final T getData() {
        // if this data container holds actual data then return the data
        if (this.data != null) {
            return this.data;
        } // otherwise if this data container points to another data container
        else if (this.pointer != null) {
            return this.pointer.getData();
        }
        // if there is no data and no pointer to another data container then return null
        return null;
    }
    
    /*
     * Sets the data container to point to another data container
     * @param pointer: Reference to a compatible data container to connect to
     */
    public final void setData(DataContainer<T> pointer) throws CompatibilityException, NonOverwritableException {
        if (this.data != null) {
            String error = "ERROR: Data container holds actual data and therefore cannot point to another data container.";
            throw new NonOverwritableException(error);
        }
        if (pointer.data != null) {
            if (this.compatibilityKey != pointer.compatibilityKey) {
                String error = String.format("Data container compatibility key '%s' is not compatible with data container compatibility key '%s'. Data container was not updated.", this.compatibilityKey, pointer.getCompatibiltyKey());
                throw new CompatibilityException(error);
            }
        }
        this.pointer = pointer;
    }
}
