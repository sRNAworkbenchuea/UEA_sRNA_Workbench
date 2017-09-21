/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Chris Applegate
 * @param <T> Represents a directed edge graph node of generic data type T
 */
public final class DirectedGraphNode<T> {

    // unique identifier for the graph node
    private final String id;
    // element of graph node
    private final T element;
    // map of input connected directed graph nodes (key is unqiue identifier and value is directed graph node)
    private final Map<String, DirectedGraphNode<T>> inConnections;
    // map of output connected directed graph nodes (key is unqiue identifier and value is directed graph node)
    private final Map<String, DirectedGraphNode<T>> outConnections;

    /*
     * constructor
     * @param element: element to store within graph node
     */
    public DirectedGraphNode(String id, T element) {
        // initialise instance variables
        this.id = id;
        this.element = element;
        this.inConnections = new HashMap<>();
        this.outConnections = new HashMap<>();
    }

    /*
     * Returns the element stored by the graph node
     */
    public final T getElement() {
        return this.element;
    }

    /*
     * Returns unique identifier for graph node
     */
    public String getID() {
        return this.id;
    }

    /* 
     * Adds a directed graph node to the list of input connections
     * @param  node: Directed graph node to add to input connection list
     */
    public final void addInConnection(DirectedGraphNode<T> node) throws InitialisationException, DuplicateIDException {
        if (node == null) {
            String error = String.format("ERROR: Cannot add null input connection to Directed Graph Node '%s'.", this.id);
            throw new NullPointerException(error);
        }
        if (this.inConnections == null) {
            throw new InitialisationException("ERROR: Directed Graph input connections has not been initialised.");
        }
        if (!this.inConnections.containsKey(node.getID())) {
            this.inConnections.put(node.getID(), node);
        } else {
            String error = String.format("ERROR: Directed Graph Node '%s' already contains input connection with id '%s'.", this.id, node.getID());
            throw new DuplicateIDException(error);
        }
    }

    /* 
     * Adds a directed graph node to the list of output connections
     * @param  node: Directed graph node to add to output connection list
     */
    public final void addOutConnection(DirectedGraphNode node) throws InitialisationException, DuplicateIDException {
        if (node == null) {
            String error = String.format("ERROR: Cannot add null output connection to Directed Graph Node '%s'.", this.id);
            throw new NullPointerException(error);
        }
        if (this.inConnections == null) {
            throw new InitialisationException("ERROR: Directed Graph output connections has not been initialised.");
        }
        if (!this.outConnections.containsKey(node.getID())) {
            this.outConnections.put(node.getID(), node);
        } else {
            String error = String.format("ERROR: Directed Graph Node '%s' already contains output connection with id '%s'.", this.id, node.getID());
            throw new DuplicateIDException(error);
        }
    }

    /*
     * Returns a list of input directed graph node connections
     */
    public final List<DirectedGraphNode<T>> getInConnections() {
        return new LinkedList<>(this.inConnections.values());
        /* List<DirectedGraphNode<T>> connections = new LinkedList<>();
         for (String key : this.inConnections.keySet()) {
         connections.add(this.inConnections.get(key));
         }
         return connections;*/
    }

    /*
     * Returns a list of output directed graph node connections
     */
    public final List<DirectedGraphNode<T>> getOutConnections() {

        return new LinkedList<>(this.outConnections.values());
//        List<DirectedGraphNode<T>> connections = new LinkedList<>();
//        for (String key : this.outConnections.keySet()) {
//            connections.add(this.outConnections.get(key));
//        }
//        return connections;
    }

    /*
     * Returns output connection with specified unique identifier
     * @param id: unique identifier of output connection to return
     */
    public final DirectedGraphNode<T> getOutConnection(String id) throws InitialisationException, IDDoesNotExistException {
        if (this.inConnections == null) {
            throw new InitialisationException("ERROR: Directed Graph output connections has not been initialised.");
        }
        if (this.outConnections.containsKey(id)) {
            return this.outConnections.get(id);
        }
        String error = String.format("ERROR: Could not find output connection '%s' in graph node '%s'.", id, this.id);
        throw new IDDoesNotExistException(error);
    }

    /*
     * Returns input connection with specified unique identifier
     * @param id: unique identifier of input connection to return
     */
    public final DirectedGraphNode<T> getInConnection(String id) throws InitialisationException, IDDoesNotExistException {
        if (this.inConnections == null) {
            throw new InitialisationException("ERROR: Directed Graph input connections has not been initialised.");
        }
        if (this.inConnections.containsKey(id)) {
            return this.inConnections.get(id);
        }
        String error = String.format("ERROR: Could not find input connection '%s' in graph node '%s'.", id, this.id);
        throw new IDDoesNotExistException(error);
    }
}
