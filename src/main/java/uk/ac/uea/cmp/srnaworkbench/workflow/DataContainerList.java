/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chris Applegate
 * @param <T>
 */
public final class DataContainerList<T> {

    // unique identifier for the list
    private final String id;
    // list of data containers
    private final List<DataContainer<T>> list;
    // the minimum length of the list of data containers
    private final int minLength;
    // the maximum length of the list of data containers (-1 indicates infinite length list)
    private final int maxLength;
    // compatibility key for all data container within the list
    private final WorkflowManager.CompatibilityKey compatibilityKey;

    /*
     * constructor
     * @param id: unique identifier for the list
     * @param key: compatibility key for all data container within the list
     * @param minLength: the minimum length of the list of data containers
     * @param maxLength: the maximum length of the list of data containers (-1 indicates infinite length list)
     */
    public DataContainerList(String id, WorkflowManager.CompatibilityKey key, int minLength, int maxLength) {
        this.id = id;
        this.compatibilityKey = key;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.list = new ArrayList<>();
    }

    /*
     * Adds a new data container to the list of data containers providing that keys are compatible
     * @param container: the container that a container for this object must connect to
     */
    public final void add(DataContainer<T> container) throws MaximumCapacityException, CompatibilityException {
        // ensure that the compatibility keys are compatible
        if (this.compatibilityKey != container.getCompatibiltyKey()) {
            throw new CompatibilityException("ERROR: Compatibility keys '" + this.compatibilityKey +"' and '" + container.getCompatibiltyKey() + "' are not compatible.");
        }
        // will adding a container violate the min/max list length rules?
        if (this.maxLength == -1 || this.list.size() < this.maxLength) {
            // create a new data container for this list
            DataContainer<T> listContainer = new DataContainer<>(this.compatibilityKey, container);
            // add this new data container to this list
            this.list.add(listContainer);
        } else {
            throw new MaximumCapacityException("ERROR: Data container list '" + this.id + "' has reached maximum capacity of '" + this.maxLength + "'");
        }
    }

    /*
     * Removes the data container at the specified index in the list
     * @param index: the index of the data container to remove
     */
    public final void remove(int index) {
        if (index > 0 && this.list.size() > index) {
            this.list.remove(index);
        }
    }

    /*
     * Returns the unique identifier for the list
     */
    public final String getID() {
        return this.id;
    }

    /*
     * Returns the compatibility key of the list
     */
    public final WorkflowManager.CompatibilityKey getCompatibilityKey() {
        return this.compatibilityKey;
    }

    /*
     * Returns the data container at the specified index in the list
     */
    public final DataContainer<T> getContainer(int index) {
        if (index < this.getListLength()) {
            return this.list.get(index);
        }
        System.err.println("ERROR: " + getID() + " trying to access container " + index + " when there are only " + this.getListLength() + " containers.");
        return null;
    }

    /*
     * Returns the length of the list of data containers
     */
    public final int getListLength() {
        return this.list.size();
    }

    /* 
     * Returns whether the data container list has valid data containers for workflow execution
     */
    public final boolean isValid() {

        // has the minimum list length requirement been met?
        if (this.list.size() < this.minLength) {
            return false;
        }
        // has the maximum list length requirement been met? (-1 indicates an infinite maximum limit on the length of the list)
        if (this.maxLength != -1 && this.list.size() > this.maxLength) {
            return false;
        }
        // loop through all of the data containers and ensure that all containers eventually map to real data.
        for (DataContainer<T> container : this.list) {
            // if the container is null or does not point to actual data: return invalid (false)
            if (container == null || container.getData() == null) {
                return false;
            }
        }
        // all checks have passed: return valid (true)
        return true;
    }

}
