package uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher;

import java.awt.Dialog.ModalityType;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * @author Leighton Folkes (l.folkes@uea.ac.uk).
 */
public class Progressor extends Thread {

    /** The dialog which is the popup window. **/
    private JDialog dialog;
    /**Holds the progress bar and labels. **/
    private JPanel panel;
    /** The progress bar for display. **/
    private JProgressBar progressBar;
    /** A label to hold a description of the task being measured for progress. **/
    private JLabel taskDescription;

    /**
     * Creates a new instance of Progressor.
     * @param topLevel The top level container of the GUI.
     * @param min The minimum measure of progress (default = 0).
     * @param max The maximum measure of progress.
     * @param description A short description of the task progress is being measured for.
     */
    public Progressor(JFrame topLevel, int min, int max, String description){
        panel = new JPanel();
        taskDescription = new JLabel(description);
        progressBar = new JProgressBar(min, max);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        dialog = new JDialog();
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setTitle("Progress...");
        dialog.setSize(300,50);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(topLevel);
        panel.add(taskDescription);
        panel.add(progressBar);
        dialog.add(panel);
        dialog.pack();
    }//end constructor.

    /**
     * Makes the Progressor visible.
     */
    @Override
    public void run(){
        dialog.setVisible(true);
    }//end method.

    /**
     * When finished measuring progress, say that we are done and
     * thank the Progressor for its hard work in showing us the progress. :-)
     */
    public void doneThanks(){
        dialog.setVisible(false);
    }//end method.

    /**
     * Increment the progress of the progress bar.
     * @param increment Adds some progress to the progress bar.
     */
    public void incrementProgress(int increment){
        if(progressBar.isIndeterminate()){
            progressBar.setIndeterminate(false);
        }
        progressBar.setValue(progressBar.getValue()+increment);
    }//end method.

    /**
     * Set the progress bar to indeterminate mode.
     */
    public void setIndeterminate(){
        progressBar.setIndeterminate(true);
    }//end method.

}//end class.
