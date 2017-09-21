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
 * @param <T> Represents a directed edge graph of generic data type T
 */
public final class DirectedGraph<T> {

    // map of nodes in directed edge graph (key is unqiue identifier and value is directed graph node)
    private final Map<String, DirectedGraphNode<T>> nodes;

    // default constructor
    public DirectedGraph() {
        // initialise instance variables
        this.nodes = new HashMap<>();
    }

    
    public void reset()
    {
        this.nodes.clear();
    }
    /*
     * Adds a node to the graph
     * @param id: unique identifier of graph node
     * @param element: element to be stored within graph node
     */
    public final void addNode(String id, T element) throws InitialisationException, DuplicateIDException  {
        // if the node map has not been initialised
        if (this.nodes == null) {
            throw new InitialisationException("ERROR: node map has not been initialised for Directed Graph.");
        }
        // does the graph already contain a node with the specified id?
        if (!this.nodes.containsKey(id)) {
            DirectedGraphNode node = new DirectedGraphNode(id, element);
            this.nodes.put(id, node);
        } else {
            String error = String.format("ERROR: Directed Edge Graph already contains node with id '%s'. Node was not added.", id);
            throw new DuplicateIDException(error);
        }
    }

    /* 
     * Connects the graph nodes corresponding to the specified unique indentifiers
     * @param fromID: Unique identifier of the graph node that the connection will connect from (output)
     * @param toID: Unique identifier of the graph node that the connection will connect to (input)
     */
    public final void addConnection(String fromID, String toID) throws InitialisationException, IDDoesNotExistException, DuplicateIDException  {
        // if the node map has not been initialised
        if (this.nodes == null) {
            throw new InitialisationException("ERROR: node map has not been initialised for Directed Graph.");
        }
        boolean fromNodeExists = this.nodes.containsKey(fromID);
        boolean toNodeExists = this.nodes.containsKey(toID);
        // if both nodes exist
        if (fromNodeExists && toNodeExists) {
            // get the nodes
            DirectedGraphNode fromNode = this.nodes.get(fromID);
            DirectedGraphNode toNode = this.nodes.get(toID);
            // create the connections
            fromNode.addOutConnection(toNode);
            toNode.addInConnection(fromNode);
        }
        // if one or more of specified nodes do not exists then throw an exception
        else
        {
            String error = "ERROR: ";
            if(!fromNodeExists)
            {
                error += String.format("Directed graph does not contain node with id '%s'. ", fromID);
            }
            if(!toNodeExists)
            {
                error += String.format("Directed graph does not contain node with id '%s'. ", toID);
            }
            throw new IDDoesNotExistException(error);
        }
    }

    /* 
     * Returns a list of graph nodes
     */
    public final List<DirectedGraphNode<T>> getAllNodes() throws InitialisationException{
        // if the node map has not been initialised
        if (this.nodes == null) {
            throw new InitialisationException("ERROR: node map has not been initialised for Directed Graph.");
        }
        return new LinkedList<>(nodes.values());
       /* // create a linked list of nodes
        List<DirectedGraphNode<T>> nodeList = new LinkedList<>();
        // loop through all keys in node map
        for (String key : this.nodes.keySet()) {
            // get the node correspinding to the key
            DirectedGraphNode<T> n = this.nodes.get(key);
            // add the node to the list of nodes
            nodeList.add(n);
        }
        // return the list of nodes
        return nodeList;*/
    }

    /*
     * Returns the graph node corresponding to the specified unique identifier
     * @param id: Unique identifier of the graph node to return
     */
    public DirectedGraphNode<T> getNode(String id) throws InitialisationException, IDDoesNotExistException {
        // if the node map has not been initialised
        if (this.nodes == null) {
            throw new InitialisationException("Node map has not been initialised for Directed Graph.");
        }
        if (this.nodes.containsKey(id)) {
            return this.nodes.get(id);
        }
        String error = String.format("Directed Edge Graph does not contain node with id '%s'.", id);
        throw new IDDoesNotExistException(error);
    }

}
