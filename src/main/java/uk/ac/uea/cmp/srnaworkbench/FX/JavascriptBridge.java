/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.FX;

import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

/**
 *
 * @author w0445959
 */
public abstract class JavascriptBridge
{
    protected ScreensController myController;
    private static boolean firstNodeLoad = true;

    
    public JavascriptBridge(ScreensController controller)
    {
        myController = controller;
    }

    public static void resetFirstLoad()
    {
        firstNodeLoad = true;
    }
    public void setScreen(String newScreen)
    {
        if (firstNodeLoad)//show wizard by default on first load of entry point nodes (datbase or file manager)
        {
            if (WorkflowManager.getInstance().containsID("Database") && WorkflowManager.getInstance().containsID("FileManager"))
            {//if both the file manager and database modules are present, the wizard should only be shown if the node is the filemanager
                if (newScreen.equalsIgnoreCase("FileManager"))
                {
                    newScreen = "FM_WIZARD_SCREEN";
                    firstNodeLoad = false;
                }
            }
            else
            {//only either the filemanager or database are present so just set the first node to be wizard based on which entry point node is selected
                if (newScreen.equalsIgnoreCase("FileManager") )
                {
                    newScreen = "FM_WIZARD_SCREEN";
                    firstNodeLoad = false;
                }
                else if (newScreen.equalsIgnoreCase("Database"))
                {
                    newScreen = "DB_WIZARD_SCREEN";
                    firstNodeLoad = false;
                }

            }
        }
        myController.setScreen(newScreen);
    }

    public void returnToMainWorkflow()
    {
        setScreen(WorkflowViewer.MAIN_SCREEN);
    }
    
    public void log(String text)
    {
        System.out.println(text);
    }
}
