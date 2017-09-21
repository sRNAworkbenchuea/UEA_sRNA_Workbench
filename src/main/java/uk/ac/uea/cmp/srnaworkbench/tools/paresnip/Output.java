
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import javax.swing.JTextArea;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 * Wrapper for the output function of the tool.
 * @author Leighton Folkes (l.fokes@uea.ac.uk)
 */
public class Output {

    /** The GUI.**/
    private JTextArea guiOut;

    /**
     * Prints the given string to the main output text area or std out.
     * @param s The string to be printed.
     */
    public void println(String s){
        if(guiOut == null){
            System.out.println(s);
        }else{
            guiOut.append(s+LINE_SEPARATOR);
            guiOut.setCaretPosition(guiOut.getDocument().getLength());
        }
    }//end method.

    /**
     * Prints a new line.
     */
    public void println(){
        if(guiOut == null){
            System.out.println();
        }else{
            guiOut.append(LINE_SEPARATOR);
            guiOut.setCaretPosition(guiOut.getDocument().getLength());
        }
    }//end method.

    /**
     * Appends the text.
     * @param s text to append.
     */
    public void print(String s){
        if(guiOut == null){
            System.out.print(s);
        }else{
            guiOut.append(s);
            guiOut.setCaretPosition(guiOut.getDocument().getLength());
        }
    }//end method.

    /**
     * Set the text area for the GUI.
     * @param t The text area.
     */
    public void setGuiOut(JTextArea t){
        guiOut = t;
    }//end method.

}//end class.
