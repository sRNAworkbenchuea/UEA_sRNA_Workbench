/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.filter2WF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.geometry.Rectangle2D;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter2FX.Filter2SceneController;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.workflow.gui.WorkflowSceneController;

/**
 *
 * @author w0445959
 */
public class Filter2WorkflowModule extends WorkflowModule
{

    private boolean readyToContinue = false;
    private FilterParams params;
    private Path outputDir;
    
    private Map<String, List<Path>> samples;

    // The engines
    private HashMap<String, FilterRunner> f_runners = new HashMap<String, FilterRunner>();

    public Filter2WorkflowModule(String id)
    {
        this(id, new Rectangle2D(200, 200, 200, 200));
    }

    public Filter2WorkflowModule(String id, Rectangle2D visualBounds)
    {
        super(id, "Filter 2");

        //this.fxmlResource = DIR_SEPARATOR + "fxml" + DIR_SEPARATOR + "FileReviewScene.fxml";
        this.fxmlResource = "/fxml/Filter2Scene.fxml";

        this.controller = new Filter2SceneController(visualBounds, this);

    }

    public void setParams(FilterParams fp)
    {
        this.params = fp;
    }

    public void setOutputDir(Path toPath)
    {
        this.outputDir = toPath;

    }

    public Path getOutputDIR()
    {
        return this.outputDir;
    }

    public synchronized void setReadyToContinue(boolean newState)
    {
        readyToContinue = newState;
        if (readyToContinue)
        {

            this.notifyAll();
        }
    }

    public synchronized void updateRun()
    {

        if (!f_runners.isEmpty())
        {
            for (Entry<String, List<Path>> e : samples.entrySet())
            {
                for (Path p : e.getValue())
                {
                    final FilterRunner getTrigger = f_runners.get(FilenameUtils.removeExtension(p.getFileName().toString()));
   
                    if (getTrigger != null && getTrigger.isComplete())
                    {
                        ((Filter2SceneController) controller).addCompletedFile(p);
                        ((Filter2SceneController) controller).addOutputInfoToInterface(p.getFileName(), getTrigger.getResults());
                        f_runners.remove(FilenameUtils.removeExtension(p.getFileName().toString()));

                    }
                }
                
            }
          
            if (f_runners.isEmpty())
            {
                notifyAll();

            }
        }
        else
        {
            notifyAll();
        }

    }

    @Override
    protected synchronized void process() throws Exception
    {
        //hold this thread util user hits continue...
        WorkflowSceneController.setWaitingNode(this.getID());

        if (!DatabaseWorkflowModule.getInstance().isDebugMode())
        {

            this.wait();
        }

        samples = FileHierarchyViewController.retrieveDataPaths();

        ((Filter2SceneController) controller).addFilesToInterface(samples);

        String currentSampleID;

        for (Entry<String, List<Path>> e : samples.entrySet())
        {
            for (Path p : e.getValue())
            {
                FilterRunner runner = new FilterRunner((Filter2SceneController) controller);
                runner.runFilterTool(p.toFile(), outputDir.toFile(), params, JOptionPane.NO_OPTION, this.getTracker());

                this.f_runners.put(FilenameUtils.removeExtension(p.getFileName().toString()), runner);
            }

        }

        this.wait();
        readyToContinue = true;
    }

    boolean readyToContinue()
    {
        return this.readyToContinue;
    }

}
