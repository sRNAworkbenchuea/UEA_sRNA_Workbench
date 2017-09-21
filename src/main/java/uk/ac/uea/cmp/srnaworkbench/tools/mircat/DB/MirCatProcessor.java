/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.mircat.DB;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.uea.cmp.srnaworkbench.FX.JFXStatusTracker;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBase;
import uk.ac.uea.cmp.srnaworkbench.data.mirbase.MirBaseHeader;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.LocusDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.PredictionDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.UniqueSequenceDAOImpl;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.SRNA_Locus_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PrecursorServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.io.Chromosome;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.locus.SRNA_Locus_Service;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModule;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.FX.MiRCat2SceneController;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.SequenceUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.StopWatch;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author w0445959
 */
@Transactional
@Service("MiRCatServiceLayer")
public class MirCatProcessor
{
    
    @Autowired
    private UniqueSequenceDAOImpl uniqueSeqDao;
    
    @Autowired
    private LocusDAOImpl locusDAO;
    
    
    @Autowired
    private PredictionDAOImpl predictionDAO;

    private MiRCatParams params;

    private HashMap<String, ArrayList<MirBaseHeader>> myMirBaseData;  // mirBase data
    
    //This may need to be improved to ensure we are not running too many things at once
//    ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newWorkStealingPool();
    //private ForkJoinPool fork_join_pool = (ForkJoinPool)Executors.newWorkStealingPool();
    private ThreadPoolExecutor es;// = (ThreadPoolExecutor) Executors.newFixedThreadPool(Tools.getThreadCount()-1);
    //ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    
    
    private GenomeManager genomeManager;
    
    private String filename = "";

    private static int totalThread = Tools.getThreadCount();
    private boolean output_logging = false;
    private boolean fileMode = true;
    private String outputDir = "";
    private MiRCatModule parent;


    public MirCatProcessor()
    {
        //System.out.println("total thread count: " + totalThread);
        es = (ThreadPoolExecutor) Executors.newFixedThreadPool(totalThread);
    }

    public void  setupMirCatProcessor(MiRCatParams params, GenomeManager genome, String filename, boolean fileMode, String outputDir, JFXStatusTracker tracker,
            MiRCatModule parent)
    {

        // Set input vars
        this.params = params;
        
        this.genomeManager = genome;

        this.filename = filename;
        
        this.fileMode = fileMode;
        this.outputDir = outputDir;
        
        this.parent = parent;


        try
        {
            this.myMirBaseData = new MirBase().getMatureMap(true);
        }
        catch (Exception ex)
        {
            myMirBaseData = new HashMap<>();
            LOGGER.log(Level.FINE, "Couldnt find mirbase files...", ex);
        }

        Tools.trackPage("miRCat Workflow Main Procedure Class Loaded");

    }
    
    private void buildLoci()
    {

        SRNA_Locus_Service locus_builder = (SRNA_Locus_Service) DatabaseWorkflowModule.getInstance().getContext().getBean("SRNA_LOCUS_SERVICE");
        locus_builder.buildLoci(params);

    }
    
    public void categorise_miRNAs() throws Exception
    {
        buildLoci();
        
        cat_miRNAs();
        
        //while(es.getCompletedTaskCount() < es.getActiveCount())
        
    }
    
    private void cat_miRNAs() throws InterruptedException
    {
//        PrecursorServiceImpl precursorServ = (PrecursorServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PrecursorService");
//        PredictionServiceImpl predictionServ = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");


        StopWatch sw = new StopWatch("MiRcat Thread Population");
        Session session = locusDAO.getSessionFactory().openSession();
        
        Map<String, Chromosome> chromosomes = genomeManager.getChromosomes();
        
        int num_thread = 0;
        for(Entry<String, Chromosome> chrom : new TreeMap<>(chromosomes).entrySet())
        {
            sw.lap("Processing Chromsome: " + chrom.getValue().getOriginalHeader());
            String hql = "from SRNA_Locus_Entity s where s.chrom = :chromosome";

            List<SRNA_Locus_Entity> findAll = session.createQuery(hql)
                    .setParameter("chromosome", chrom.getValue().getOriginalHeader())
                    .list();
            
            
            
//            List<Callable<Object>> reportTasks = new ArrayList<>();
//            List<Future<Object>> answers;

            for (SRNA_Locus_Entity locus : findAll)
            {

                Set<Aligned_Sequences_Entity> sequences = locus.getSequences();
                if (sequences.size() >= 1)
                {
                    Iterator<Aligned_Sequences_Entity> iterator = sequences.iterator();
                    Aligned_Sequences_Entity potentialtopHit = iterator.next();
                    Aligned_Sequences_Entity topAlignedHit = potentialtopHit;
                    Unique_Sequences_Entity aligned_seqeunce = potentialtopHit.getAligned_seqeunce();
                    Sequence_Entity topSequenceHit = aligned_seqeunce.getSequenceRelationships().get(filename);
                    
                    //detect top hit

                    float currentPosClusterBias = 0.0f;
                    float currentNegClusterBias = 0.0f;
                    float expandedRNA = 0.0f;

                    while (iterator.hasNext())
                    {

                        potentialtopHit = iterator.next();
                        aligned_seqeunce = potentialtopHit.getAligned_seqeunce();
                        Sequence_Entity nextHit = aligned_seqeunce.getSequenceRelationships().get(filename);

                        if(nextHit.getAbundance() > topSequenceHit.getAbundance())
                        {
                            topSequenceHit = nextHit;
                            topAlignedHit = potentialtopHit;
                        }
                        int abundance = (int) nextHit.getAbundance();

                        expandedRNA += abundance;

                        switch (potentialtopHit.getStrand())
                        {
                            case "+":
                                currentPosClusterBias += abundance;
                                break;
                            case "-":
                                currentNegClusterBias += abundance;
                                break;
                        }



                    }
                    float pos_percentage = (currentPosClusterBias / expandedRNA) * 100.0f;
                    float neg_percentage = (currentNegClusterBias / expandedRNA) * 100.0f;

                    //first checks on sequence and locus level
                    if (
                            topSequenceHit.getAbundance() > params.getMinConsider() && 
                            sequences.size() >= params.getMinLocusSize() &&
                            ( ( pos_percentage >= params.getOrientation() ) || ( neg_percentage >= params.getOrientation() ) )
                            )
                    {
                        //no need to check size as the query (should) only returns sequences of a certain size to reduce work 
                        int genomeHits = topSequenceHit.getGenomeHitCount();
                        float gc_content = SequenceUtils.DNA.getGCCount(topSequenceHit.getRNA_Sequence());
                        float percentage = (gc_content / (float) topSequenceHit.getRNA_Sequence().length()) * 100.0f;
                        if ((genomeHits <= params.getMaxGenomeHits()) && (percentage >= (float) params.getMinGC()))
                        {
//
//                            MiRNAPredictor miRNAPredictor = new MiRNAPredictor(locus, topSequenceHit, topAlignedHit, params, genomeManager,
//                                    chrom.getValue().getOriginalHeader(), this.output_logging,
//                                    Tools.miRCAT_DATA_Path, num_thread, filename, this.fileMode, this.outputDir, 
//                                    null, null, this.predictionDAO, null);
//                            
                            es.execute(new MiRNAPredictor(locus, topSequenceHit, topAlignedHit, params, genomeManager,
                                    chrom.getValue().getOriginalHeader(), this.output_logging,
                                    Tools.miRCAT_DATA_Path, num_thread, filename, this.fileMode, this.outputDir, 
                                    null, null, this.predictionDAO, session));
                            
                            

                            num_thread++;
                            if (num_thread >= totalThread)
                            {
                                num_thread = 0;
                            }
                            //miRNAPredictor.run();
                            
                            //reportTasks.add(Executors.callable(miRNAPredictor));
                            
                            //miRNAPredictor.run();
                        }
                    }


                }
                
            }
            
            //answers = es.invokeAll(reportTasks);
            sw.printLap("Completed Population!");
            

        }
        //fork_join_pool.shutdown();
        es.shutdown();
        sw.stop();
        sw.printTimes();
        
        
//        es.shutdown();
//        System.out.println("task count: " + es.getTaskCount());
//        //while(es.awaitTermination(1, TimeUnit.DAYS))
        //es.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        //parent.notifyComplete();
        
        session.flush();
        session.close();

    }

    public int getTotalThread()
    {
        return totalThread;
    }

    public static void setTotalThread(int totalThreadCount)
    {
        totalThread = totalThreadCount;
    }

    public boolean getReadyToContinue()
    {
        boolean result = es.isTerminated();
        
        return result;
    }

    public long getWaitingTasks()
    {
        return es.getTaskCount() - es.getCompletedTaskCount();
    }
    
    


}
