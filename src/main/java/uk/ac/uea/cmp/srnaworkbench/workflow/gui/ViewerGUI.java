/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow.gui;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.JPanel;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;

/**
 *
 * @author Chris Applegate
 */
public abstract class ViewerGUI extends javax.swing.JInternalFrame implements GUIInterface {

    private Scene scene;
    private Parent root;
    private final String sceneFilename;
    private final String styleFilename;
    private FXMLLoader loader;
    private final String id;
    private final JFXPanel fxPanel;

    public ViewerGUI(String id, String sceneFilename, String styleFilename) {
        this.id = id;
        this.sceneFilename = sceneFilename;
        this.styleFilename = styleFilename;
        this.fxPanel = new JFXPanel();
    }

    protected void initScene() {
        try {
            System.out.println("SCENE FILE: " + this.sceneFilename);
            System.out.println("STYLE FILE: " + this.styleFilename);
            this.loader = new FXMLLoader(ViewerGUI.class.getResource(this.sceneFilename));
            this.root = this.loader.load();
            this.scene = new Scene(root);
            this.scene.getStylesheets().add(this.styleFilename);
            this.fxPanel.setScene(this.scene);
        } catch (Exception ex) {
            System.err.println("ERROR: Could not set-up loader: " + ex);
        }
    }

    protected JFXPanel getPanel() {
        return this.fxPanel;
    }

    protected FXMLLoader getLoader() {
        return this.loader;
    }

    protected Scene getScene() {
        return this.scene;
    }

    public String getSceneFilename() {
        return this.sceneFilename;
    }

    public String getStyleFilename() {
        return this.styleFilename;
    }

    @Override
    public String toString() {
        return this.id;
    }
    
    @Override
    public void runProcedure() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JPanel getParamsPanel() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public void setShowingParams(boolean newState) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean getShowingParams() {
        return false;
    }

    @Override
    public void shutdown() {
      //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
