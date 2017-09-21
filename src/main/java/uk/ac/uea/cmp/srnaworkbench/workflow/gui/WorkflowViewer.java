/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow.gui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javax.swing.JPanel;
import uk.ac.uea.cmp.srnaworkbench.FX.ScreensController;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;

/**
 *
 * @author w0445959
 */
public class WorkflowViewer extends javax.swing.JInternalFrame implements GUIInterface
{

    public static final String MAIN_SCREEN = "workflow"; 
     public static final String MAIN_SCREEN_FXML = "/fxml/WorkflowScene.fxml"; 
//     public static final String HIERARCHY_SCREEN = "hierarchy"; 
//     public static final String HIERARCHY_SCREEN_FXML = 
//                                          "/fxml/FileHierarchyView.fxml"; 
//     public static final String NORMALISE_SCREEN = "normalise"; 
//     public static final String NORMALISE_SCREEN_FXML = 
//                                          "/fxml/NormalisationScene.fxml"; 
     
     
    private Scene scene;
    private Group root;
    
    private WorkflowSceneController controller;
    
    private JFXPanel fxPanel;
    /**
     * Creates new form WorkflowViewer
     */
    public WorkflowViewer()
    {
        initComponents();
        
        
        fxPanel = new JFXPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        javax.swing.GroupLayout fxPanelLayout = new javax.swing.GroupLayout(fxPanel);
        fxPanel.setLayout(fxPanelLayout);
        fxPanelLayout.setHorizontalGroup(
            fxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 958, Short.MAX_VALUE)
        );
        fxPanelLayout.setVerticalGroup(
            fxPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        getContentPane().add(fxPanel, java.awt.BorderLayout.CENTER);
        
        //this.mainScrollPane.setViewportView(fxPanel);
    
        Platform.runLater(() ->
        {
            initFX(fxPanel);
        });
    }
    
    private void initFX(JFXPanel fxPanel)
    {
        // This method is invoked on the JavaFX thread
        createScene();
        fxPanel.setScene(scene);
    }
    public Dimension getFrameSize()
    {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        Dimension screenDimension = env.getMaximumWindowBounds().getSize();
        Insets insets = this.getInsets();
        final int left = insets.left;
        final int right = insets.right;
        final int top = insets.top;
        final int bottom = insets.bottom;

        final int width = screenDimension.width - left - right;
        final int height = screenDimension.height - top - bottom;

        return new Dimension(width, height);
        //return this.fxPanel.getPreferredSize();
    }

    private void createScene() 
    {
         WorkflowSceneController workflowSceneController = new WorkflowSceneController();
         workflowSceneController.addParentFrame(this);
        ScreensController.getInstance().loadScreen(WorkflowViewer.MAIN_SCREEN, 
                            WorkflowViewer.MAIN_SCREEN_FXML, workflowSceneController); 
//       mainContainer.loadScreen(WorkflowViewer.HIERARCHY_SCREEN, 
//                            WorkflowViewer.HIERARCHY_SCREEN_FXML); 
//       mainContainer.loadScreen(WorkflowViewer.NORMALISE_SCREEN, 
//                            WorkflowViewer.NORMALISE_SCREEN_FXML); 

        ScreensController.getInstance().setScreen(WorkflowViewer.MAIN_SCREEN); 

       
       root = new Group(); 
       root.getChildren().addAll( ScreensController.getInstance()); 
       scene = new Scene(root); 
       
       
       
//       primaryStage.setScene(scene); 
//       primaryStage.show(); 
//        try
//        {
//
//            FXMLLoader loader = new FXMLLoader(NormalisationController.class.getResource("/fxml/WorkflowScene.fxml"));
//            root = loader.load();
//            scene = new Scene(root);
//            scene.getStylesheets().add("/styles/Styles.css");
//
//            controller = (WorkflowSceneController)loader.getController();
//            controller.setStageAndSetupListeners(scene);
//            controller.addParentFrame(this);
//        }
//        catch (IOException ex)
//        {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void runProcedure()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JPanel getParamsPanel()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setShowingParams(boolean newState)
    {
        
    }

    @Override
    public boolean getShowingParams()
    {
        return false;
    }

    @Override
    public void shutdown()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
