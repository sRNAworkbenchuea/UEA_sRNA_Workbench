/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.FX;

import java.io.IOException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

/**
 *
 * @author w0445959
 */
public class ScreensController extends StackPane {

    // chris: added reference to primary stage so can add window resize listeners
    private Stage primaryStage;
    // static instance of the screencontroller
    private static ScreensController instance = null;
    private HashMap<String, Node> screens = new HashMap<>();
    String activeScreenName = "";
    

    public static ScreensController getInstance() {
        if (instance == null) {
            instance = new ScreensController();
        }
        return instance;
    }
    
    // sets the primary stage
    public void setPrimaryStage(Stage primaryStage)
    {
        this.primaryStage = primaryStage;
    }
    
    // returns the primary stage
    public Stage getPrimaryStage()
    {
        return this.primaryStage;
    }
    
    private void addScreen(String name, Node screen) {
        screens.put(name, screen);

    
    }

    public void loadScreen(String name, String resource) {
        try {
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent loadScreen = (Parent) myLoader.load();
            ControlledScreen myScreenControler
                    = ((ControlledScreen) myLoader.getController());
            myScreenControler.setScreenParent(this);
            myScreenControler.setStageAndSetupListeners(new Scene(loadScreen));
            
            addScreen(name, loadScreen);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    

    public void loadScreen(String name, String resource, ControlledScreen controller) {
        //System.out.println("loading: " + name + " loc: " + resource);
        
        try {
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            controller.setScreenParent(this);
            myLoader.setController(controller);
            Parent loadScreen = (Parent) myLoader.load();
            
            
            controller.setStageAndSetupListeners(new Scene(loadScreen));
            addScreen(name, loadScreen);
        } catch (IOException ex) {
            // If the resource path is bad, this is a developer problem and not recoverable from.
            // throw an unchecked exception
            //ex.printStackTrace();
            LOGGER.log(Level.SEVERE, ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void setScreen(final String name){
        

        
        activeScreenName = name;
        if (screens.get(name) != null)
        { //screen loaded 
            final DoubleProperty opacity = opacityProperty();

            //Is there is more than one screen 
            if (!getChildren().isEmpty())
            {
//                Platform.runLater(() ->
//                {
                    Timeline fade = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(opacity, 1.0)),
                            new KeyFrame(new Duration(450),
                                    new EventHandler()
                                    {

                                        @Override
                                        public void handle(Event t)
                                        {
                                            //remove displayed screen 
                                            getChildren().remove(0);
                                            //add new screen 
                                            getChildren().add(0, screens.get(name));
                                            
                                            Timeline fadeIn = new Timeline(
                                                    new KeyFrame(Duration.ZERO,
                                                            new KeyValue(opacity, 0.0)),
                                                    new KeyFrame(new Duration(300),
                                                            new KeyValue(opacity, 1.0)));
                                            fadeIn.play();
                                          
                                        }
                                    }, new KeyValue(opacity, 0.0)));
                    fade.play();
//                });
                //System.out.println("more than one...");
            }
            else
            {
                //Platform.runLater(() ->
                //{
                    //no one else been displayed, then just show 
                    setOpacity(0.0);
                    getChildren().add(screens.get(name));
                    Timeline fadeIn = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(opacity, 0.0)),
                            new KeyFrame(new Duration(400),
                                    new KeyValue(opacity, 1.0)));
                    fadeIn.play();
//                });
                //System.out.println("only one");
            }
            
            

            //System.out.println("children: "  + getChildren().);
            //System.out.println("finished and returning");
//            return true;
        }
        else
        {
            NoSuchScreenException e = new NoSuchScreenException(name);
            LOGGER.log(Level.SEVERE,"Screen " + name + " does not exist", e);
            
            
        }
    }

    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Screen didn't exist");
            return false;
        } else {
            return true;
        }
    }

    public String getActiveScreen()
    {
        
        return activeScreenName;
    }

}
