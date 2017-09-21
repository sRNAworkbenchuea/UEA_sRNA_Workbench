/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.exceptions;

/**
 *
 * @author w0445959
 */
public class ToolsInitializationFailed extends RuntimeException {

    /**
     * Creates a new instance of <code>ToolsInitializationFailed</code> without detail message.
     */
    public ToolsInitializationFailed() {
    }


    /**
     * Constructs an instance of <code>ToolsInitializationFailed</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ToolsInitializationFailed(String msg) {
        super(msg);
    }
}
