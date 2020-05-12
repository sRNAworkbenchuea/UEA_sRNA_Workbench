/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainMDIWindow.java
 *
 * Created on 19-Oct-2010, 10:05:47
 */
package uk.ac.uea.cmp.srnaworkbench;

import java.awt.Color;
import java.awt.Cursor;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBase;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.Updater;
import uk.ac.uea.cmp.srnaworkbench.help.AboutInternal;
import uk.ac.uea.cmp.srnaworkbench.help.JHLauncher;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;
import uk.ac.uea.cmp.srnaworkbench.tools.adaptorremover.AdaptorRemoverMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.colide.CoLIDEMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.expressionCalculator.ExpressionCalculatorMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewer;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MirCatMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.mirprof.MirprofFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.FX.NormalisationViewer;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.Degradome;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.FX.PlotSelectionViewer;
import uk.ac.uea.cmp.srnaworkbench.tools.rnaannotation.RNAannotationMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.sequencealignment.SequenceAlignmentMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.siloco.SiLoCoMainFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.tasi.TasiFrame;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.SequenceVizMainFrame;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowViewer;

/**
 *
 * @author w0445959
 */
public class MainMDIWindow extends javax.swing.JFrame {

    /**
     * Creates new form MainMDIWindow
     */
    private final MDIDesktopPane desktopPane;

    private boolean paramBrowserVisible = false;
    
    private WorkflowSceneController WF_controller = null;

    public MainMDIWindow(WorkflowSceneController WF_controller)
    {
        this();
        this.WF_controller = WF_controller;
        
    }
    /**
     *
     */
    public MainMDIWindow() {
        
        desktopPane = new MDIDesktopPane();
        initComponents();

        desktopPane.setRefs(paramContainer, paramScrollPane, scrollPane, getContentPane());

        desktopPane.setBackground(Color.GRAY);
        scrollPane.getViewport().add(desktopPane);
        paramScrollPane.setVisible(false);

        JHLauncher.setupContextDependentHelp("HTML_index_html", loadHelp, this.getRootPane());

//        if (Tools.isMac()) {
//            registerForMacOSXEvents();
//        }

        

        setupAppUtils();
        System.gc();
    }

    private void setupAppUtils() {
        AppUtils.INSTANCE.setMDIDesktopPane(desktopPane);
        AppUtils.INSTANCE.setMainMDIWindow(this);
    }

    // Generic registration with the Mac OS X application menu
    // Checks the platform, then attempts to register with the Apple EAWT
    // See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
    private void registerForMacOSXEvents() {
        if (!Tools.isMac()) {
            return;
        }

        try {
            // Generate and register the OSXAdapter, passing it the methods we wish to
            // use as delegates for various com.apple.eawt.ApplicationListener methods

            OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
            OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));

            // Uncomment the next line to automatically get a preferences menu item on MacOS
            //OSXAdapter.setPreferencesHandler( this, getClass().getDeclaredMethod( "preferences", (Class[]) null ) );
        } catch (Exception e) {
            System.err.println("Error while loading the OSXAdapter:");
            e.printStackTrace();
        }

        this.aboutMenu.setVisible(false);
        this.fileMenu.setVisible(false);
    }

    // Used by the OSXAdapter as the method to call when "About Workbench" is selected
    public void about() {
        aboutMenuActionPerformed(null);
    }

    // Used by the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quit() {
        exitMenuItemActionPerformed(null);
        return true;
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences() {
        // Add code to display preferences
    }

    /**
     * This method is called from within the constructor to initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        paramScrollPane = new javax.swing.JScrollPane();
        paramContainer = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        fileHierView = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        toolBrowseMenu = new javax.swing.JMenu();
        returnToWorkflowMnu = new javax.swing.JMenuItem();
        helperToolsMenu = new javax.swing.JMenu();
        loadAR = new javax.swing.JMenuItem();
        loadAR_Low_Mem = new javax.swing.JMenuItem();
        loadFilter = new javax.swing.JMenuItem();
        loadSeqAlign = new javax.swing.JMenuItem();
        analysisToolsMenu = new javax.swing.JMenu();
        loadMirCat = new javax.swing.JMenuItem();
        loadMirprof = new javax.swing.JMenuItem();
        loadParesnip = new javax.swing.JMenuItem();
        loadSiLoCo = new javax.swing.JMenuItem();
        loadTasi = new javax.swing.JMenuItem();
        loadCoLIDE = new javax.swing.JMenuItem();
        visualisationToolsMenu = new javax.swing.JMenu();
        loadHairpinAnnotation = new javax.swing.JMenuItem();
        loadSeqViz = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        showParams = new javax.swing.JMenuItem();
        hideParams = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        fileHierView1 = new javax.swing.JMenuItem();
        windowMenu = new WindowMenu(desktopPane);
        helpMenu = new javax.swing.JMenu();
        loadHelp = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();
        mirbaseUpdateMenu = new javax.swing.JMenuItem();
        changeUserDirMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("The UEA Small RNA Workbench");
        setBackground(new java.awt.Color(120, 120, 120));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Lucida Sans Unicode", 0, 10)); // NOI18N
        setForeground(java.awt.Color.lightGray);

        scrollPane.setBackground(new java.awt.Color(120, 120, 120));
        scrollPane.setAutoscrolls(true);

        paramContainer.setBackground(new java.awt.Color(120, 120, 120));

        javax.swing.GroupLayout paramContainerLayout = new javax.swing.GroupLayout(paramContainer);
        paramContainer.setLayout(paramContainerLayout);
        paramContainerLayout.setHorizontalGroup(
            paramContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 317, Short.MAX_VALUE)
        );
        paramContainerLayout.setVerticalGroup(
            paramContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 645, Short.MAX_VALUE)
        );

        paramScrollPane.setViewportView(paramContainer);

        menuBar.setBackground(new java.awt.Color(213, 219, 245));
        menuBar.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 12)); // NOI18N

        fileMenu.setText("File");

        fileHierView.setText("View File Hierarchy");
        fileHierView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileHierViewActionPerformed(evt);
            }
        });
        fileMenu.add(fileHierView);
        fileMenu.add(jSeparator1);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/shut-down.png"))); // NOI18N
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        toolBrowseMenu.setText("Tools");

        returnToWorkflowMnu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/tree.png"))); // NOI18N
        returnToWorkflowMnu.setText("Return to Workflow Menu");
        returnToWorkflowMnu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnToWorkflowMnuActionPerformed(evt);
            }
        });
        toolBrowseMenu.add(returnToWorkflowMnu);

        helperToolsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/first-aid.png"))); // NOI18N
        helperToolsMenu.setText("Helper Tools");

        loadAR.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadAR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/scissors.jpg"))); // NOI18N
        loadAR.setText("<html><b>Adapter Removal</b><br><font size = \"2\">Prepare and trim RAW sequence data");
        loadAR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadARActionPerformed(evt);
            }
        });
        helperToolsMenu.add(loadAR);

        loadAR_Low_Mem.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadAR_Low_Mem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/scissors.jpg"))); // NOI18N
        loadAR_Low_Mem.setText("<html><b>Adapter Removal LM (beta)</b><br><font size = \"2\">Prepare and trim RAW sequence data");
        loadAR_Low_Mem.setToolTipText("Remove adapters from raw sequence data using the new low memory modules (beta)");
        loadAR_Low_Mem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAR_Low_MemActionPerformed(evt);
            }
        });
        helperToolsMenu.add(loadAR_Low_Mem);

        loadFilter.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/filter-256.png"))); // NOI18N
        loadFilter.setText("<html><b>Filter</b><br><font size = \"2\">Filter unwanted sequences from your data");
        loadFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadFilterActionPerformed(evt);
            }
        });
        helperToolsMenu.add(loadFilter);

        loadSeqAlign.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadSeqAlign.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/dna.gif"))); // NOI18N
        loadSeqAlign.setText("<html><b>Sequence Alignment</b><br><font size = \"2\">Align sRNAs to a reference sequence");
        loadSeqAlign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSeqAlignActionPerformed(evt);
            }
        });
        helperToolsMenu.add(loadSeqAlign);

        toolBrowseMenu.add(helperToolsMenu);

        analysisToolsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lab.png"))); // NOI18N
        analysisToolsMenu.setText("Analysis Tools");

        loadMirCat.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadMirCat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/mirCAT_Icon.jpg"))); // NOI18N
        loadMirCat.setText("<html><b>miRCat</b><br><font size = \"2\">Novel micro RNA Prediction");
        loadMirCat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMirCatActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadMirCat);

        loadMirprof.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadMirprof.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/mirprof_logo_small.png"))); // NOI18N
        loadMirprof.setText("<html><b>miRProf</b><br><font size = \"2\">Known micro RNA Expression Profiling");
        loadMirprof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMirprofActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadMirprof);

        loadParesnip.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadParesnip.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/PAREsnipLOGO.png"))); // NOI18N
        loadParesnip.setText("<html><b>PAREsnip</b><br><font size = \"2\">Degradome assisted sRNA target prediction");
        loadParesnip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadParesnipActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadParesnip);

        loadSiLoCo.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadSiLoCo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/balance_big.jpg"))); // NOI18N
        loadSiLoCo.setText("<html><b>SiLoCo</b><br><font size = \"2\">Wide scale Small Interfering RNA Locus Prediction");
        loadSiLoCo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSiLoCoActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadSiLoCo);

        loadTasi.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadTasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/tasi.png"))); // NOI18N
        loadTasi.setText("<html><b>TA-SI Prediction</b><br><font size = \"2\">TA-SI RNA Locus Prediction");
        loadTasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTasiActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadTasi);

        loadCoLIDE.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/CoLIDESmall.png"))); // NOI18N
        loadCoLIDE.setText("<html><b>CoLide</b><br><font size = \"2\">Expression Pattern Driven Locus Prediction");
        loadCoLIDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCoLIDEActionPerformed(evt);
            }
        });
        analysisToolsMenu.add(loadCoLIDE);

        toolBrowseMenu.add(analysisToolsMenu);

        visualisationToolsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/bar-chart-02.png"))); // NOI18N
        visualisationToolsMenu.setText("Visualisation");

        loadHairpinAnnotation.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadHairpinAnnotation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/newHAIRPIN.jpg"))); // NOI18N
        loadHairpinAnnotation.setText("<html><b>RNA Folding/Annotation</b><br><font size = \"2\">Highlight sRNAs on a folded RNA sequence ");
        loadHairpinAnnotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadHairpinAnnotationActionPerformed(evt);
            }
        });
        visualisationToolsMenu.add(loadHairpinAnnotation);

        loadSeqViz.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        loadSeqViz.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/DNA.jpg"))); // NOI18N
        loadSeqViz.setMnemonic('g');
        loadSeqViz.setText("<html><b>VisSR</b><br><font size = \"2\">Visualisation of small RNAs as they appear on the Genome");
        loadSeqViz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSeqVizActionPerformed(evt);
            }
        });
        visualisationToolsMenu.add(loadSeqViz);

        toolBrowseMenu.add(visualisationToolsMenu);
        toolBrowseMenu.add(jSeparator2);

        showParams.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/configuration.png"))); // NOI18N
        showParams.setText("Show Parameter Browser");
        showParams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showParamsActionPerformed(evt);
            }
        });
        toolBrowseMenu.add(showParams);

        hideParams.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/configurationClose.png"))); // NOI18N
        hideParams.setText("Hide Parameter Browser");
        hideParams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideParamsActionPerformed(evt);
            }
        });
        toolBrowseMenu.add(hideParams);
        toolBrowseMenu.add(jSeparator3);

        fileHierView1.setText("View File Hierarchy");
        fileHierView1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileHierViewActionPerformed(evt);
            }
        });
        toolBrowseMenu.add(fileHierView1);

        menuBar.add(toolBrowseMenu);

        windowMenu.setText("Window");
        menuBar.add(windowMenu);

        helpMenu.setText("Help");

        loadHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/lookup.png"))); // NOI18N
        loadHelp.setText("Contents");
        loadHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadHelpActionPerformed(evt);
            }
        });
        helpMenu.add(loadHelp);

        aboutMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/workbenchSmall.jpg"))); // NOI18N
        aboutMenu.setText("About");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenu);

        mirbaseUpdateMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/GUI_Icons/mirbase-logo-blue-web.png"))); // NOI18N
        mirbaseUpdateMenu.setText("Update miRBase");
        mirbaseUpdateMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mirbaseUpdateMenuActionPerformed(evt);
            }
        });
        helpMenu.add(mirbaseUpdateMenu);

        changeUserDirMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/SharedImages/star.png"))); // NOI18N
        changeUserDirMenu.setText("Change User Directory");
        changeUserDirMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeUserDirMenuActionPerformed(evt);
            }
        });
        helpMenu.add(changeUserDirMenu);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paramScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(paramScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        closeProgram();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private GUIInterface getTopFrame() {
        //First we get all frames contained within the desktop pane.
        JInternalFrame[] iframes = desktopPane.getAllFrames();

        // a variable to store the current Z value. Set to the amount of total
        // frames-1 to set it to the end of the array
        int zValue = iframes.length - 1;

        //loop through all frames
        for (JInternalFrame iframe : iframes) {
            //determine if we find a frame with a lower z value
            if (desktopPane.getComponentZOrder(iframe) < zValue) {
                zValue = desktopPane.getComponentZOrder(iframe);
            }
        }

        //the active frame is the one with the lowest z value
        if (zValue < 0) {
            return null;
        }
        return (GUIInterface) iframes[zValue];
    }

    private void loadMirCatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadMirCatActionPerformed

        MirCatMainFrame mirCatTool = new MirCatMainFrame();
        initialiseTool(mirCatTool);

        mirCatTool.checkTutorialWindow();
    }//GEN-LAST:event_loadMirCatActionPerformed

    private void loadParesnipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadParesnipActionPerformed
        initialiseTool(new Degradome());
//      Degradome degTool = new Degradome();
//
//        toolManager.addTool(degTool);
////        toolManager.addTool(degTool);
////        degTool.setToolManager(toolManager);
//        desktopPane.add(degTool);
//        degTool.setVisible(true);

    }//GEN-LAST:event_loadParesnipActionPerformed

    private void showParamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showParamsActionPerformed

        if (ToolManager.getInstance().getTotalToolCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please first select a tool from the Tools drop down menu.",
                    "File Open Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            for (GUIInterface tool : ToolManager.getInstance()) {
                showParamBrowser(tool);
            }
            paramBrowserVisible = true;
        }
    }//GEN-LAST:event_showParamsActionPerformed

    public void showParamBrowser(GUIInterface tool) {
        tool.setShowingParams(true);

        desktopPane.showParams(tool);

    }

    public void activateFrame(JInternalFrame f) {
        desktopPane.activateFrame(f);
    }

    private void hideParamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideParamsActionPerformed

        hideParameterBrowser();

    }//GEN-LAST:event_hideParamsActionPerformed

    public void hideParameterBrowser() {
        for (GUIInterface tool : ToolManager.getInstance()) {
            tool.setShowingParams(false);
        }
        desktopPane.hideParamsActionPerformed();
        paramScrollPane.setVisible(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                )
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
        );

        paramBrowserVisible = false;
    }

    private void loadSeqVizActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadSeqVizActionPerformed
    {//GEN-HEADEREND:event_loadSeqVizActionPerformed
        initialiseTool(new SequenceVizMainFrame(false));
    }//GEN-LAST:event_loadSeqVizActionPerformed

    private void loadHairpinAnnotationActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadHairpinAnnotationActionPerformed
    {//GEN-HEADEREND:event_loadHairpinAnnotationActionPerformed
        generateHairpinAnnotationTool();
    }//GEN-LAST:event_loadHairpinAnnotationActionPerformed

private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuActionPerformed
    AboutInternal aboutFrame = AboutInternal.getInstance();
    aboutFrame.setVisible(true);
    desktopPane.add(aboutFrame);
}//GEN-LAST:event_aboutMenuActionPerformed

    private void loadSiLoCoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadSiLoCoActionPerformed
    {//GEN-HEADEREND:event_loadSiLoCoActionPerformed
        initialiseTool(new SiLoCoMainFrame());
    }//GEN-LAST:event_loadSiLoCoActionPerformed

private void loadMirprofActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadMirprofActionPerformed
    MirprofFrame mirprofFrame = new MirprofFrame();
    initialiseTool(mirprofFrame);

    mirprofFrame.setupMiRBase();

}//GEN-LAST:event_loadMirprofActionPerformed

private void mirbaseUpdateMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mirbaseUpdateMenuActionPerformed

    try {
        boolean isuptodate = Updater.isUpToDate();

        if (!isuptodate) {
            updateMirbase(false);
        } else {
            int option = JOptionPane.showConfirmDialog(this,
                    "The local miRBase version is up-to-date, do you wish to force an update?",
                    "mirBase Updater",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                updateMirbase(true);
            }
        }
    } catch (Exception e) {
        this.getContentPane().setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "mirBase Updater Error",
                JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_mirbaseUpdateMenuActionPerformed

    private void loadTasiActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadTasiActionPerformed
    {//GEN-HEADEREND:event_loadTasiActionPerformed
        initialiseTool(new TasiFrame());
    }//GEN-LAST:event_loadTasiActionPerformed

  private void changeUserDirMenuActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_changeUserDirMenuActionPerformed
  {//GEN-HEADEREND:event_changeUserDirMenuActionPerformed
      String path = Tools.userDataDirectoryPath;
      File newUserDir = FileDialogUtils.showFileSaveDialog(this, path, "Set New User Directory", null, true);
      Tools.userDataDirectoryPath = newUserDir.getAbsolutePath();
      Tools.updateUserPaths(Tools.userDataDirectoryPath);
      try {
          Tools.checkUserDirectoryData();

         
          // Tell the workbench logger about the new log dir
          WorkbenchLogger.LOGGER.recreateHandlers(new File(Tools.LOG_DIR));
      } catch (IOException e) {
          // Need to do something here.  Probably should raise an error dialog
          JOptionPane.showMessageDialog(this,
                  "Couldn't change user directory to: " + newUserDir.getAbsolutePath(),
                  "Change User Directory",
                  JOptionPane.ERROR_MESSAGE);
      }
  }//GEN-LAST:event_changeUserDirMenuActionPerformed

  private void loadCoLIDEActionPerformed( java.awt.event.ActionEvent evt )//GEN-FIRST:event_loadCoLIDEActionPerformed
  {//GEN-HEADEREND:event_loadCoLIDEActionPerformed
      CoLIDEMainFrame coLIDEMainFrame = new CoLIDEMainFrame();
      boolean setupResult = coLIDEMainFrame.setup();
      if (setupResult) {
          initialiseTool(coLIDEMainFrame);
      }
  }//GEN-LAST:event_loadCoLIDEActionPerformed

    private void loadHelpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadHelpActionPerformed
    {//GEN-HEADEREND:event_loadHelpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loadHelpActionPerformed

    private void fileHierViewActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileHierViewActionPerformed
    {//GEN-HEADEREND:event_fileHierViewActionPerformed
//        FileHierarchyViewer fileHierarchyViewer = new FileHierarchyViewer();
//        if(ToolManager.getInstance().getTool("FileHierarchyView") == null)
//        {
//            FileHierarchyViewer fileHierarchyViewer = new FileHierarchyViewer();
//            initialiseTool( fileHierarchyViewer );
//            try
//            {
//                fileHierarchyViewer.setMaximum(true);
//            }
//            catch (PropertyVetoException ex)
//            {
//                LOGGER.log(Level.SEVERE, null, ex);
//            }
//        }
//        else
//        {
//            ((JInternalFrame)ToolManager.getInstance().getTool("FileHierarchyView")).setVisible(true);
//        }
        FileHierarchyViewer.getOrCreateViewer().setVisible(true);

//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                fileHierarchyViewer.initFX();
//            }
//       });
        //fileHierarchyViewer
    }//GEN-LAST:event_fileHierViewActionPerformed

    private void loadSeqAlignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSeqAlignActionPerformed
        initialiseTool(new SequenceAlignmentMainFrame());
    }//GEN-LAST:event_loadSeqAlignActionPerformed

    private void loadFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadFilterActionPerformed
        initialiseTool(new FilterMainFrame());
    }//GEN-LAST:event_loadFilterActionPerformed

    private void loadAR_Low_MemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadAR_Low_MemActionPerformed
        initialiseTool(new AdaptorRemoverMainFrame(true));
    }//GEN-LAST:event_loadAR_Low_MemActionPerformed

    private void loadARActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadARActionPerformed
        initialiseTool(new AdaptorRemoverMainFrame());
    }//GEN-LAST:event_loadARActionPerformed

    private void returnToWorkflowMnuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnToWorkflowMnuActionPerformed
        WF_controller.setWindowToV4();
    }//GEN-LAST:event_returnToWorkflowMnuActionPerformed

    

    private void closeProgram() {
        //close any running programs
        ToolManager.getInstance().closeTools();
        //shut down the FX application thread
        Platform.exit();
        System.exit(0);

    }

    private void updateMirbase(boolean force) throws Exception {
        new Thread(new GenerateWaitCursor(this.getContentPane())).start();
        Updater.download(force);

        String latest_version = MirBase.getLatestLocalVersion();

        this.getContentPane().setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this,
                "miRBase updated successfully.  Latest version: " + latest_version,
                "mirBase Updater",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void generateHairpinAnnotationTool() {
        RNAannotationMainFrame HA_tool = new RNAannotationMainFrame();
//        toolManager.addTool(HA_tool);
//        desktopPane.add(HA_tool);
//        HA_tool.setVisible(true);
        initialiseTool(HA_tool);

        //comment this line out for release...
        //HA_tool.initDemoImage();
        //HA_tool.centreOriginalImage();
    }

    private void initialiseTool(JInternalFrame toolFrame) {
        if (toolFrame instanceof GUIInterface) {
            GUIInterface gi = (GUIInterface) toolFrame;

            ToolManager.getInstance().addTool(gi);

            if (paramBrowserVisible) {
                showParamBrowser(gi);
            }
        }

        desktopPane.add(toolFrame);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JMenu analysisToolsMenu;
    private javax.swing.JMenuItem changeUserDirMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem fileHierView;
    private javax.swing.JMenuItem fileHierView1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu helperToolsMenu;
    private javax.swing.JMenuItem hideParams;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem loadAR;
    private javax.swing.JMenuItem loadAR_Low_Mem;
    private javax.swing.JMenuItem loadCoLIDE;
    private javax.swing.JMenuItem loadFilter;
    private javax.swing.JMenuItem loadHairpinAnnotation;
    private javax.swing.JMenuItem loadHelp;
    private javax.swing.JMenuItem loadMirCat;
    private javax.swing.JMenuItem loadMirprof;
    private javax.swing.JMenuItem loadParesnip;
    private javax.swing.JMenuItem loadSeqAlign;
    private javax.swing.JMenuItem loadSeqViz;
    private javax.swing.JMenuItem loadSiLoCo;
    private javax.swing.JMenuItem loadTasi;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mirbaseUpdateMenu;
    private javax.swing.JPanel paramContainer;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JMenuItem returnToWorkflowMnu;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JMenuItem showParams;
    private javax.swing.JMenu toolBrowseMenu;
    private javax.swing.JMenu visualisationToolsMenu;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables
}
