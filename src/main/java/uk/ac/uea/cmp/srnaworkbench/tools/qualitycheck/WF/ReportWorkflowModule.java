package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.FilePair;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.AnnotationNotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.exceptions.NotInDatabaseException;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.FilenameServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationService;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.AnnotationSet;
import uk.ac.uea.cmp.srnaworkbench.tools.annotate.ReferenceSequenceManager;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerFX.FileHierarchyViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.AnnotationSetList;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.FX.PlotSelectionSceneController;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution.AbundanceDistributionParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceDistribution.AbundanceDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.abundanceSizeClass.SizeClassDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard.JaccardTableParams;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.jaccard.JaccardTableTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.DatabaseMATool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MAService;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MValueDistributionParameters;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.ma.MValueDistributionTool;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.nucleotideSizeClassService.NucleotideSizeClassDistributionTool;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author w0445959
 */
public class ReportWorkflowModule extends WorkflowModule 
{

    private boolean readyToContinue = true;
    private List<NormalisationType> norms;
    private List<String> filenames;
    private AnnotationSetList annotations;
    
    private EnumSet<QCTool> tools = EnumSet.allOf(QCTool.class);
    
    private JFXStatusTracker abdStatus;
    private JFXStatusTracker scdStatus;
    private JFXStatusTracker jacStatus;
    private JFXStatusTracker nucStatus;
    private JFXStatusTracker maStatus;
    
    //This may need to be improved to ensure we are not running too many things at once
    //ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newWorkStealingPool();
    ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    
    List<Callable<Object>> reportTasks = new ArrayList<>();
    List<Future<Object>> answers;
    
    // flag that specifies whether unmapped sequences are considered in this report
    private boolean considerUnmapped;
    
    

    public ReportWorkflowModule(String id, List<NormalisationType> norms)
    {
        this(id, norms, new Rectangle2D(200,200,200,200));
    }
    
    public ReportWorkflowModule(String id, List<NormalisationType> norms, Rectangle2D frameSize, AnnotationSetList annotationSets) {
        this(id, norms, frameSize);
        this.annotations = annotationSets;
    }
    
    public ReportWorkflowModule(String id, Rectangle2D visualBounds)
    {
        this(id, null, visualBounds);
       

    }
    
    public ReportWorkflowModule(String id, List<NormalisationType> norms, List<String> filenames) {
        this(id, norms);
        this.filenames = filenames;
    }
    
    public ReportWorkflowModule(String id, List<NormalisationType> norms, Rectangle2D visualBounds)
    {
        super(id, "Report");
 
        this.norms = norms;
        this.fxmlResource = "/fxml/PlotSelectionScene.fxml";
        this.controller = new PlotSelectionSceneController(this, visualBounds);
        ((PlotSelectionSceneController)controller).setStageName(getID());
        
        this.considerUnmapped = true;
    }
    
    public void enableTool(QCTool tool)
    {
        this.tools.add(tool);
    }
    public void disableTool(QCTool tool)
    {
        this.tools.remove(tool);
    }
    
    public void insertStatusTrackers(JFXStatusTracker abdStatus, JFXStatusTracker scdStatus, JFXStatusTracker jacStatus,
                                     JFXStatusTracker nucStatus, JFXStatusTracker maStatus)
    {
        this.abdStatus = abdStatus;
        this.scdStatus = scdStatus;
        this.jacStatus = jacStatus;
        this.nucStatus = nucStatus;
        this.maStatus = maStatus;
    }
    
    /**
     * When called, this report will ignore unmapped sequences
     * @return 
     */
    public void setMappedOnly()
    {
        this.considerUnmapped = false;
    }

    public List<String> getFilenames()
    {
        return filenames;
    }

    public void setFilenames(List<String> filenames)
    {
        this.filenames = filenames;
    }

    
    /**
     * Produces the report information for the Plot viewer HTML page based on
     * all available information in the database
     *
     */
    private void produceReport(AbundanceDistributionParameters params) throws Exception
    {
        for(QCTool tool : tools){
            switch(tool){
                case ABUNDANCE_DISTRIBUTION:
                    build_boxplotdist(params);
                    break;
                case SIZE_CLASS:
                    build_scd();
                    break;
                case JACCARD:
                    build_jaccard();
                    break;
                case NUCLEOTIDE:
                    build_nucl_size_class();
                    break;
                case MA:
                    build_ma();
                    break;
                case FOLD_CHANGE_DISTRIBUTION:
                    build_ma_v_dist();
                    break;
            }
        }
//
        
        //Updating progress on completed threads
//        while (!es.isTerminated())
//        {
//            if (this.cancelRequested())
//            {
//                es.shutdownNow();
//
//                Thread.sleep(6000);
//                // Kill all binary executors used for each thread.
//                this.trackerInitUnknownRuntime("Terminating thread pool");
//                for (BinaryExecutor temp : exe_list)
//                {
//                    temp.stopExeManager();
//                }
//                Tools.removeShutdownHooks(this.shutdownExeManThreads);
//                this.trackerReset();
//            }
//
//            continueRun();
//
//            printVerbose("core size: " + es.getCorePoolSize());
//            printVerbose("current pool: " + es.getPoolSize());
//            printVerbose("largest pool: " + es.getLargestPoolSize());
//            printVerbose("maximum pool: " + es.getMaximumPoolSize());
//            printVerbose("task count: " + es.getTaskCount());
//            printVerbose("Thread Queue: " + es.getQueue().size() + " Completed: " + es.getCompletedTaskCount());
//
//            final float processed = processed_sRNA;
//            final float top_total = top_hits_per_chromosome.totalTopHits();
//            final int esQSize = es.getQueue().size();
//            final long esCTCount = es.getCompletedTaskCount();
//            final long esTCount = es.getTaskCount();
//            final float populationStatus = Tools.roundTwoDecimals((processed / top_total) * 100.0f);
//
//            if (myStatusLabel != null)
//            {
//                SwingUtilities.invokeLater(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//
//                        myStatusLabel.setText("<HTML><p>Populating..." + populationStatus + "% complete<br/>"
//                                + "Thread Queue: " + esQSize + " Completed: " + esCTCount + "</p></HTML>");
//                        myExecQBar.setMaximum((int) esTCount);
//                        myExecQBar.setValue(esQSize);
//
//                        float percentage = 0.0f;
//                        if (esTCount > 0)
//                        {
//                            percentage = ((float) esCTCount / (float) esTCount) * 100.0f;
//                        }
//                        myCompTaskBar.setValue((int) percentage);
//                    }
//                });
//            }
//        }
    }
    
    public void outputReportSummary(Path location)
    {
        //ReportSummaryService repService = (ReportSummariser) DatabaseWorkflowModule.getInstance().getContext().getBean("ReportSummariser");
        ReportSummariser reporter = new ReportSummariser();
        try
        {
            reporter.generatePrintedReport(location);
        }
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Method section for generation of JSON files containing available report data
     */

    /**
     * 
     * @param filenames
     * @throws IOException 
     */
    private void build_nucl_size_class() throws IOException
    {
        NucleotideSizeClassDistributionTool nuctool = new NucleotideSizeClassDistributionTool(nucStatus);
        nuctool.setStageName(getID());
        //nuctool.setTimeable(true);
        //es.execute(nuctool);
        reportTasks.add(Executors.callable(nuctool));
//        NucleotideSizeClassServiceLayer nucserv = (NucleotideSizeClassServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("NucleotideSizeClassService");
//        nucserv.setStageNameAndTracker(id, nucStatus);
//        es.execute(nucserv);

    }

    private void build_jaccard() throws IOException
    {
        JaccardTableTool jtool = new JaccardTableTool(filenames, norms, new JaccardTableParams(), jacStatus);
        jtool.setStageName(getID());
       // jtool.setTimeable(true);
        //es.execute(jtool);
        reportTasks.add(Executors.callable(jtool));
        
//        JaccardIndexServiceLayer j = (JaccardIndexServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("JaccardIndex");
//        j.configure(filenames, norms, 1000, id, jacStatus);
//        j.run();
    }

    private void build_scd() throws IOException
    {
        SizeClassDistributionTool scdTool = new SizeClassDistributionTool(filenames, norms, annotations, scdStatus);
        scdTool.setStageName(getID());
        //scdTool.setTimeable(true);
        //es.execute(scdTool);
        reportTasks.add(Executors.callable(scdTool));

        
//        SizeClassDistributionServiceLayer sdt = (SizeClassDistributionServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("SizeClassDistributionCalculator");
//
//        sdt.configure(filenames, norms, id, scdStatus);
//        es.execute(sdt);

    }

    private void build_ma()
    {
        FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        
        // Ensure the MA table is clear for this next report.
        MAService maService = (MAService) DatabaseWorkflowModule.getInstance().getContext().getBean("MAService");
        maService.clearMAdata();
        List<FilePair> filePairs = fileServ.getReplicatePairs(filenames);

        List<Integer> offsets = new ArrayList<>();
        offsets.add(1);

        DatabaseMATool maTool = new DatabaseMATool(filePairs, norms, annotations, offsets, maStatus);
        maTool.setStageName(getID());
    //maTool.setTimeable(true);
        //es.execute(maTool);
        reportTasks.add(Executors.callable(maTool));


        

//        MAToolServiceLayer ma = (MAToolServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("MAToolService");
//
//        ma.configureAllPairs(filenames, norms, 2, 1, maStatus);
//        es.execute(ma);
       
    }
    private void build_ma_v_dist()
    {
        FilenameServiceImpl fileServ = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        List<FilePair> filePairs = fileServ.getReplicatePairs(filenames);
        List<Integer> offsets = new ArrayList<>();
        offsets.add(1);
        MValueDistributionParameters params = new MValueDistributionParameters();
        MValueDistributionTool mtool = new MValueDistributionTool(filePairs, norms, offsets, annotations, params);
        mtool.setStageName(getID());
        reportTasks.add(Executors.callable(mtool));

    }

    private void build_boxplotdist(AbundanceDistributionParameters params) throws IOException
    {
        AbundanceDistributionTool adTool = new AbundanceDistributionTool(filenames, norms, annotations, params,  abdStatus);
        adTool.setStageName(getID());
        //adTool.setTimeable(true);
        //es.execute(adTool);
        reportTasks.add(Executors.callable(adTool));

        
//        AbundanceDistributionServiceLayer abw = (AbundanceDistributionServiceLayer) DatabaseWorkflowModule.getInstance().getContext().getBean("AbundanceDistributionService");
//        abw.setTimeable(true);
//        for (NormalisationType norm : norms)
//        {
//
//            abw.configure(norm, Logarithm.BASE_2, 5, 500, id, abdStatus);
//            es.execute(abw);
//
//        }

    }

    public boolean readyToContinue()
    {
        if(answers == null)
            return false;
        for(Future<Object> answer : answers)
        {
            if(!answer.isDone())
                return false;
        }
        
        return true;
    }
    
    public void setReadyToContinue(boolean newState)
    {
        readyToContinue = newState;
    }
    

    @Override
    protected void process() throws Exception
    {
        if(DatabaseWorkflowModule.getInstance().isDebugMode())
        {
            outputReportSummary(Paths.get(Tools.ExpressionMatrix_dataPath + DIR_SEPARATOR + "testReport.csv"));
        }
        
        this.annotations = FileHierarchyViewController.getAnnotationSetList(considerUnmapped);
        
        
        //if(this.preNormalisation)
        //    annotations.addAnnotationSet(AnnotationSet.getAllSet());
        
        //annotations.addAnnotationSet(AnnotationSet.getMappedSet());
        //if(this.preNormalisation)
        //    annotations.addAnnotationSet(none);
        
//        annotations.addAnnotationSet(selected_annotations);

        //loop over genome set
//        for (String annot_type : selected_annotations.getTypes())
//        {
//            annotations.addType(annot_type);
//        }

        
        //change this to get selected files from the review node

        FilenameServiceImpl filenameService = (FilenameServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("FilenameService");
        if(filenames == null){
            
            //if they selected no files, or this is the first report so they did not get a chance to select yet
            if( FileReviewWorkflowModule.getSelectedFiles().isEmpty())
            {
                filenames = new ArrayList<>(filenameService.getFileIDs( ));
            }
            else
            {
                filenames = new ArrayList<>(filenameService.getFileIDs( FileReviewWorkflowModule.getSelectedFiles()) );
            }
        }
        if((norms == null)||norms.isEmpty())
        {
            norms = new ArrayList<>(NormalisationWorkflowServiceModule.getNorms());
            if(!norms.contains(NormalisationType.NONE))
                norms.add(NormalisationType.NONE);
        }
        
        AbundanceDistributionParameters params = new AbundanceDistributionParameters();
        
        // parameters modified for producing paper plots. Please remove once this is finished!
        params.setNumberOfWindows(1);
        params.setWindowSize(10000);
        produceReport(params);
        
        answers = es.invokeAll(reportTasks);
        
        ((PlotSelectionSceneController)controller).callSetup();

        System.out.println("finished reporting");
        
    }
    
    public enum QCTool
    {
        ABUNDANCE_DISTRIBUTION, 
        SIZE_CLASS, 
        MA, 
        FOLD_CHANGE_DISTRIBUTION, 
        NUCLEOTIDE,
        JACCARD,
        //MAPPING_QUALITY is not seperately calculated but part of size class calculation.
    }

}
