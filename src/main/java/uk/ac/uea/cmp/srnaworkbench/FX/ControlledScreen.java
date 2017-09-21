/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.FX;

import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;

/**
 *
 * @author w0445959
 */
public interface ControlledScreen
{
    
     //This method will allow the injection of the Parent ScreenPane 
     public void setScreenParent(ScreensController screenPage); 
     
     public void setStageAndSetupListeners(Scene scene);
     
     public JFXStatusTracker getStatusTracker();
     
     /* this function is called when the workflow is started to allow us to
      * lock GUI set parameters so that they cannot be altered when workflow is in progress
      */
     public void workflowStartedListener();


       
}
