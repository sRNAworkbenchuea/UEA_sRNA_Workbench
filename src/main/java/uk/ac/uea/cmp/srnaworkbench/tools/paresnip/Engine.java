package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.data.sequence.FastaMap;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WorkbenchException;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.Filter;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterParams;
import uk.ac.uea.cmp.srnaworkbench.tools.filter.FilterStage;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.Timer;
import uk.ac.uea.cmp.srnaworkbench.tools.vissr.tplots.TPlotFrame;
import uk.ac.uea.cmp.srnaworkbench.utils.*;
import uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher.ExactMatcher;
import uk.ac.uea.cmp.srnaworkbench.utils.exactmatcher.TextReader;

/**
 * An engine that powers and manages the tool.
 *
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
class Engine extends SwingWorker<Void, String> {

    /**
     * A regular expression marking a string to be printed to the information
     * text area - "**" *
     */
    private static final String PRINT_TO_INFO_TEXT_AREA = "**";
    /**
     * A regular expression marking a string to be printed to the progress bar -
     * "$" *
     */
    private static final String PRINT_PROGRESS_BAR_STRING = "$";
    /**
     * A regular expression marking a string indicating general progress - "#$"
     * *
     */
    private static final String PRINT_GENERAL_PROGRESS = "#$";

    /**
     * The total number of queries to search against sRNA tree.*
     */
    private int totalQueries;
    /**
     * Total number of sRNAs in search space.*
     */
    private int totalShortReadsInSearchSpace_AF = 0;
    /**
     * Should visual alignments be printed when in command mode?*
     */
    private boolean Deg_Tool_printingAlignmentsInCMD = true;
    /**
     * Timer for giving an estimate of time taken to run a procedure.*
     */
    private Timer Deg_Tool_timer = new Timer();
    /**
     * The start time in milliseconds of the application run procedure.*
     */
    private long startTime;
    /**
     * Total short reads in the srnaome.*
     */
    private int totalRawShortReads_AF = 0;
    /**
     * Total number of degradome hits on the transcriptome.*
     */
    private int Deg_Tool_degradome_total_unique_hits_on_transcriptome = 0;
    
    private int totalTranscripts = 0;
    
    private int totalTranscriptPlots = 0;
    
    
    /**
     * The total number of non-redundant degradome reads within the degradome
     * file.*
     */
    private int Deg_Tool_degradome_non_redundant_read_count = 0;
    /**
     * Raw redundant degradome sequence count*
     */
    private int Deg_Tool_degradome_redundant_read_count = 0;
    /**
     * Redundant Fragment Size Distribution. *
     */
    private int[] Deg_Tool_degradome_redundant_size_dist;
    /**
     * Non-Redundant Fragment Size Distribution.*
     */
    private int[] Deg_Tool_degradome_non_redundant_size_dist;
    /**
     * srna filter.*
     */
    protected Filter filter;
    /**
     * Total number of charts made.*
     */
    private int totalCharts;
    /**
     * The parameters set by the user.
     */
    private ParesnipParams params;
    /**
     * Did the user reset the tool?*
     */
    private boolean cancelled = false;
    /**
     * A list of text which would have been put into the info text area in gui
     * mode.*
     */
    private ArrayList<String> cmdInfoArea;
    /**
     * The global data object *
     */
    private final Data data = new Data();
    /**
     * The input small RNA file. *
     */
    protected File srnaFile;
    /**
     * The input degradome file.*
     */
    protected File degradomeFile;
    /**
     * The input transcripts file. *
     */
    protected File cdnaFile;
    /**
     * The input genome file. *
     */
    protected File genome;
    /**
     * The overall progress bar. *
     */
    private JProgressBar progOver;
    /**
     * The information area in the gui.*
     */
    private JTextArea infoOut;
    /**
     * The messages label. *
     */
    private JLabel infoLabel;
    /**
     * The gui output table. *
     */
    private DefaultTableModel tableModel;
    /**
     * Row identification tracker for table.*
     */
    private int recordID = 1;
    /**
     * Flag to say if the engine is running.*
     */
    public boolean engineRunning;
    /**
     * The collection of threads working on trees.*
     */
    private SearchTreeThreadPoolManager manager;
    /**
     * The collection of secondary hits.*
     */
    private HashMap<String, ArrayList<String[]>> secondaryHits;
    /**
     * The number of records/hits put for publishing.*
     */
    private int callsToPublishIN = 0;
    /**
     * The number of records/hits published*
     */
    private int callsToPublishOUT = 0;
    /**
     * The previously added record to the table. *
     */
    private String[] previousField = null;
    /**
     * Command line output stream*
     */
    private BufferedWriter out;
    /**
     * A box of trees.*
     */
    private final OriginalTreeBox treeBox;

    /**
     * Help with feedback, esp. when done
     */
    private final FeedbackI _guiFeedback;

    /**
     * Event dispatch thread helper
     */
    private final EDTHelper edtHelper;

    /**
     * A map to hold the records with the key being the ID of the gene. *
     */
    private final Map<String, PlotRecord> plotRecords = CollectionUtils.newHashMap();
    
    private ArrayList<String> treeInformation = CollectionUtils.newArrayList();
    private ArrayList<String> srnaInformation = CollectionUtils.newArrayList();
    

    /**
     * COMMAND LINE CONSTRUCTOR - Creates a new instance of Engine.
     *
     * @param srnaFile Small RNAs.
     * @param degradomeFile Degradome.
     * @param transcriptomeFile Transcriptome.
     * @param genomeFile Genome.
     * @param outputFile The output file
     */
    public Engine(ParesnipParams params, File srnaFile, File degradomeFile, File transcriptomeFile, File genomeFile, File outputFile) {
        this.params = params;

        treeBox = new OriginalTreeBox(new Tree(this.params));

        this.srnaFile = srnaFile;
        this.degradomeFile = degradomeFile;
        this.cdnaFile = transcriptomeFile;
        this.genome = genomeFile;

        manager = new SearchTreeThreadPoolManager(params.getThreadCount(), data);
        cmdInfoArea = new ArrayList<String>();

        try {
            out = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);

            // Should engine exit ?
        }
        //To keep life simple :-)
        infoLabel = new JLabel();
        progOver = new JProgressBar();

        /*
         * Create a 'null' feedback object so that code like
         *   if ( _guiFeedback != null ) _guiFeedback.methodCall(...);
         * can have the null check removed and can be written more simply as
         *   _guiFeedback.methodCall(...);
         *
         * Code for GUI mode and command-line modes looks the same
         */
        _guiFeedback = new FeedbackI() {
            @Override
            public void setStatusText(String text) {
                System.out.println(text);
            }

            @Override
            public void addProgressMessage(String text, boolean append) {
                System.out.println(text);
            }

            @Override
            public void done(boolean success) {
                System.out.println(success ? "*** Finished successfully ***" : "*** Failed ***");
            }
        };

        edtHelper = new EDTHelper(progOver);
    }

    /**
     * GUI CONSTRUCTOR - Creates a new instance of Engine.
     *
     * @param model A table model.
     * @param infoArea An information text output area.
     * @param infoLabel Information label.
     * @param progOverall A progress bar.
     * @param a Small RNAs.
     * @param b Degradome.
     * @param c Transcriptome.
     * @param d Genome.
     * @param secondaryHitsCollection The secondary hits collection.
     */
    Engine(ParesnipParams params, DefaultTableModel tableModel, JTextArea infoArea, JLabel infoLabel, JProgressBar progOverall,
            File srnaFile, File degradomeFile, File cdnaFile, File genome, HashMap<String, ArrayList<String[]>> secondaryHitsCollection,
            FeedbackI feedback) {
        this.params = params;

        treeBox = new OriginalTreeBox(new Tree(this.params));

        this.srnaFile = srnaFile;
        this.degradomeFile = degradomeFile;
        this.cdnaFile = cdnaFile;
        this.genome = genome;
        this.progOver = progOverall;
        this.tableModel = tableModel;
        this.infoLabel = infoLabel;
        this.infoOut = infoArea;

        manager = new SearchTreeThreadPoolManager(params.getThreadCount(), data);
        secondaryHits = secondaryHitsCollection;

        _guiFeedback = feedback;

        edtHelper = new EDTHelper(progOver);
    }

    /**
     * Requests the threads executing the core algorithm to stop execution.
     */
    void reset() {
        if (filter != null) {
            filter.cancelRun();
        }

        cancelled = true;
        manager.requestStop();

        super.cancel(true);
    }

    /**
     * The CORE search method.
     */
    public void startSearching() {
        plotRecords.clear();

        if (!AppUtils.INSTANCE.isCommandLine()) {
            // Parameter values have already been output when in command-line mode

            //Print the parameters used to info area.
            publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
            publish(PRINT_TO_INFO_TEXT_AREA + "Parameters used:" + LINE_SEPARATOR);
            publish(PRINT_TO_INFO_TEXT_AREA + params.toDescriptiveString() + LINE_SEPARATOR);
            //Make a seperator for nice output.
            publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
        }

        if (cancelled) {
            return;
        }

        infoLabel.setText("Building Data Structures");
        Deg_Tool_timer.startTimer();

        //Set the progress bar running.
        edtHelper.setProgressBarIndeterminate(true);

        //Start the timer for the average time per chart and estimated time till completion.
        startTime = System.nanoTime();
        //Print the message that we are starting to find degradome hits.
        publish(PRINT_PROGRESS_BAR_STRING + "Finding degradome alignments...");
        infoLabel.setText("Finding degradome alignments...");

        //USES EXACT MATCHER TO READ EVERYTHING IN AND MAKE THE ALIGNMENTS BETWEEN THE TWO FILES.
        ExactMatcher m = new ExactMatcher(Data.VERY_VERBOSE);
        List< ExactMatcher.ExactMatch> align = null;
        try {
            align = m.findExactMatches(degradomeFile, cdnaFile, params.getMinFragmentLength(), params.getMaxFragmentLength());
        } catch (WorkbenchException ex) {
            _guiFeedback.setStatusText("*** PROCESSING STOPPED ***" + LINE_SEPARATOR + ex.getMessage());
            reset();
            return;
        }

        if (cancelled) {
            return;
        }

        // Get the counts for the unique, redundant and non-redundant hits
        Deg_Tool_degradome_total_unique_hits_on_transcriptome = m.getTotalUniqueHits();
        Deg_Tool_degradome_redundant_read_count = m.getRedundantReadCount();
        Deg_Tool_degradome_non_redundant_read_count = m.getNonRedundantReadCount();

        //Set up the memory for the redundant and non-redundant size distributions.
        Deg_Tool_degradome_redundant_size_dist = new int[params.getMaxFragmentLength() + 1];
        Deg_Tool_degradome_non_redundant_size_dist = new int[params.getMaxFragmentLength() + 1];

        // Set the redundant size distribution in the local array
        for (Map.Entry< Integer, Integer> e : m.getRedundantReadSizeDistribution().entrySet()) {
            if (cancelled) {
                return;
            }
            Deg_Tool_degradome_redundant_size_dist[e.getKey().intValue()] = e.getValue().intValue();
        }

        // Set the non-redundant size distribution in the local array
        for (Map.Entry< Integer, Integer> e : m.getNonRedundantReadSizeDistribution().entrySet()) {
            if (cancelled) {
                return;
            }
            Deg_Tool_degradome_non_redundant_size_dist[e.getKey().intValue()] = e.getValue().intValue();
        }

        //Print out to the info log all info and statistics about the data.
        //The redundant number of degradome reads.
        publish(PRINT_TO_INFO_TEXT_AREA + "Total Redundant Degradome Reads: " + Deg_Tool_degradome_redundant_read_count + LINE_SEPARATOR);
        //The redundant size distribution of the degradome.
        publish(PRINT_TO_INFO_TEXT_AREA + "Redundant Size Distribution: " + LINE_SEPARATOR);
        for (int i = params.getMinFragmentLength(); i < Deg_Tool_degradome_redundant_size_dist.length; i++) {
            publish(PRINT_TO_INFO_TEXT_AREA + i + "\t" + Deg_Tool_degradome_redundant_size_dist[i] + LINE_SEPARATOR);
        }

        publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
        //Total non-redundant degradome reads.
        publish(PRINT_TO_INFO_TEXT_AREA + "Total Non-redundant Degradome Reads: " + Deg_Tool_degradome_non_redundant_read_count + LINE_SEPARATOR);
        //The non-redundant size distribution of the degradome.
        publish(PRINT_TO_INFO_TEXT_AREA + "Non-redundant Size Distribution: " + LINE_SEPARATOR);
        for (int i = params.getMinFragmentLength(); i < Deg_Tool_degradome_non_redundant_size_dist.length; i++) {
            if (cancelled) {
                return;
            }
            publish(PRINT_TO_INFO_TEXT_AREA + i + "\t" + Deg_Tool_degradome_non_redundant_size_dist[i] + LINE_SEPARATOR);
        }
        publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
        //GRAB MEMORY
        HashMap<String, Chart> tempCharts = new HashMap<String, Chart>();

        Map<String, Integer> abundances = m.getShortReadAbundances();
        Map<String, String> longReads = m.getLongReadsMap();
        Map<String, Integer> alignmentCount = m.getShortReadAlignmentCounts();

        
        totalTranscripts = m.getTotalLongReads();
        
        publish(PRINT_TO_INFO_TEXT_AREA + "Transcriptome Stats:" + LINE_SEPARATOR);
        publish(PRINT_TO_INFO_TEXT_AREA + "Total Transcripts: " + m.getTotalLongReads() + LINE_SEPARATOR);
        publish(PRINT_PROGRESS_BAR_STRING + "Constructing Charts..");
        infoLabel.setText("Constructing Charts..");

        //Make charts
        //For each alignment made by exact matcher...
        for (ExactMatcher.ExactMatch em : align) {
            if (cancelled) {
                return;
            }
            //Get the chart for the gene on which the alignment was made..
            Chart chart = tempCharts.get(em.getGeneId());
            //If a chart has not yet been created....
            if (chart == null) {
                //Get the gene id..
                String geneId = em.getGeneId();
                //Get the sequence of the gene..
                String longRead = longReads.get(geneId);
                //Create a new chart giving it the id and gene sequence..
                chart = new Chart(geneId, StringUtils.changeStringToByteArray(longRead));
                //Stick it into the temporary collection of charts.
                tempCharts.put(geneId, chart);
            }
            //Get the number of times this sequence has aligned to transcripts..
            Integer alignCountI = alignmentCount.get(em.getSequence());
            //alignment count = if(alignmentCountI == null, then alignment count = 1 else alignment count = the number of times it aligned???
            int alignCount = (alignCountI == null ? 1 : alignCountI.intValue());
            //Get the abundance of the short degradome tag..
            int abundance = abundances.get(em.getSequence()).intValue();
            //Calculate the weighted abundance of the tag (tag abundance / No. of times it hits a transcript)
            float weightedAbundance = (float) abundance / alignCount;
            //Add the hit to the chart.
            chart.addHit(em.getStartPosition(), weightedAbundance, abundance);
        }

        publish(PRINT_PROGRESS_BAR_STRING + "Compacting charts...");
        infoLabel.setText("Compacting charts...");
        //compact charts and chart contents.
        ArrayList<Chart> charts = new ArrayList<Chart>(tempCharts.size());
        {
            Iterator<String> itr = tempCharts.keySet().iterator();
            while (itr.hasNext()) {
                if (cancelled) {
                    return;
                }
                String key = itr.next();
                Chart c = tempCharts.get(key);
                c.compact();
                charts.add(c);
            }
        }

        publish(PRINT_TO_INFO_TEXT_AREA + "Total Transcript Plots: " + charts.size() + LINE_SEPARATOR);
        totalTranscriptPlots = charts.size();
        //Total number of unique transcriptome positions were hit by degradome sequences.
        publish(PRINT_TO_INFO_TEXT_AREA + "Total Unique Degradome Alignment Positions (Candidates): " + Deg_Tool_degradome_total_unique_hits_on_transcriptome + LINE_SEPARATOR);
        publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
        publish(PRINT_PROGRESS_BAR_STRING + "Cleaning up...");
        infoLabel.setText("Cleaning up...");

        //do clean up - let the gc do its job.
        m.clear();
        m = null;
        align = null;
        abundances = null;
        longReads = null;
        alignmentCount = null;
        tempCharts = null;

        System.gc();

//        //If outputting plots - make the plot records Structure.
//        if(Data.makePlotRecords){
//            if(cancelled){return;}
//            Data.plotRecords = CollectionUtils.newHashMap();
//        }
        publish(PRINT_PROGRESS_BAR_STRING + "Category calculation...");
        infoLabel.setText("Category calculation...");
        for (int i = 0; i < charts.size(); i++) {
            if (cancelled) {
                return;
            }
            charts.get(i).calculateCategories(params.isWeightedFragmentAbundance());
            //charts.get(i).printDegradomeAlignments(); - removed....
            if (params.isCalculatePvalues()) {
                charts.get(i).makeCategoryTrees(treeBox, params);
            }
            if (Data.makePlotRecords) {
                PlotRecord pr = charts.get(i).makePlotRecord(params.isWeightedFragmentAbundance());
                plotRecords.put(pr.getGeneId(), pr);
            }
        }
        if (params.isCalculatePvalues()) {
            if (cancelled) {
                return;
            }
            publish(PRINT_TO_INFO_TEXT_AREA + "Category Tree Information:" + LINE_SEPARATOR);

            for (Category cat : Category.definedCategories()) {
                String msg = PRINT_TO_INFO_TEXT_AREA + cat.toString()
                        + " Fragment Tree Node Count: " + treeBox.getOriginalCategoryTree(cat).nodeCount + LINE_SEPARATOR;

                publish(msg);
                
                treeInformation.add(cat.toString()
                        + " Fragment Tree Node Count: " + treeBox.getOriginalCategoryTree(cat).nodeCount + LINE_SEPARATOR);
                
            }
            publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
            publish(PRINT_PROGRESS_BAR_STRING + "Collecting category tree entry points...");

            for (Category cat : Category.definedCategories()) {
                treeBox.getOriginalCategoryTree(cat).listStartPositions();
            }
        }

        Runtime.getRuntime().gc();

        publish(PRINT_PROGRESS_BAR_STRING + "Making sRNA tree...");
        infoLabel.setText("Making sRNA tree...");

        //Start to build the small RNA tree.
        makeSmallRNATree();
        filter = null;
        //Print the statistics about the srnaome.
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Redundant Short Reads: "+treeBox.getOriginalSrnaTree().getRedundantSequenceCount()+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"Redundant Size Distribution: "+Tools.LINE_SEPARATOR);
        //for(int i = params.getMinSrnaLength(); i < treeBox.getOriginalSrnaTree().getRedundantSequenceDistribution().length; i++){
        //if(cancelled){return;}
        //publish( PRINT_TO_INFO_TEXT_AREA+i+"\t"+treeBox.getOriginalSrnaTree().getRedundantSequenceDistribution()[i]+Tools.LINE_SEPARATOR);
        //}
        //publish( PRINT_TO_INFO_TEXT_AREA+"-------"+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Non-redundant Short Reads: "+treeBox.getOriginalSrnaTree().getNonRedundantSequencesCount()+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"Non-redundant Size Distribution: "+Tools.LINE_SEPARATOR);
        //for(int i = params.getMinSrnaLength(); i < treeBox.getOriginalSrnaTree().getNonRedundantSequenceDistribution().length; i++){
        //    if(cancelled){return;}
        //    publish( PRINT_TO_INFO_TEXT_AREA+i+"\t"+treeBox.getOriginalSrnaTree().getNonRedundantSequenceDistribution()[i]+Tools.LINE_SEPARATOR);
        //}
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Non-redundant Short Reads in Search Space: "+totalShortReadsInSearchSpace_AF+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"-------"+Tools.LINE_SEPARATOR);
        //Clean up with gc.
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        //List the entry points into the srna tree.
        treeBox.getOriginalSrnaTree().listStartPositions();
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Short Reads in File: "+totalRawShortReads_AF+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Nodes in sRNA Tree: "+treeBox.getOriginalSrnaTree().getSrnaNodeCount()+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"Total Nodes in Split Lists: "+treeBox.getOriginalSrnaTree().getSplitListCount()+Tools.LINE_SEPARATOR);
        //publish( PRINT_TO_INFO_TEXT_AREA+"-------"+Tools.LINE_SEPARATOR);
        //Give the gc another chance to clean up properly.
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        totalCharts = charts.size();
        ///////////////////////////////////
        //For debugging
        Chart ac = null;
        byte[] sequence = null;
        int anint = -9999999;
        int anotherInt = -99999999;
        ////////////////////////////////////
        if (Data.GENERATE_SRNAS) {
            generateSrnas(charts);
        }

        try {
            publish(PRINT_PROGRESS_BAR_STRING + "Searching tree for candidates.." + LINE_SEPARATOR);
            for (int c = 0; c < charts.size(); c++) {
                if (cancelled) {
                    return;
                }
                Chart ch = charts.get(c);
                for (int i = 0; i < ch.compactWeightedAbundance.length; i++) {
                    if (cancelled) {
                        return;
                    }
                    //System.out.println(""+i+" of "+ch.compactWeightedAbundance.length);
                    int start = ch.compactPositions[i] - 19;
                    int end = ch.compactPositions[i] + 12;
                    if (end <= ch.sequence.length - 1) {
                        if (start >= 0) {
                            if (Data.CATEGORY_VERBOSE) {
                                ac = ch;
                                sequence = Data.reverseCompliment(ch.sequence, start, end, 32);
                                anint = i;
                                anotherInt = 13;
                            }
                            switch (ch.compactCategory[i]) {
                                case 0: {
                                    if (params.isCategory0()) {
                                        manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 32), ch, i, 13));
                                    }
                                    break;
                                }//end case.
                                case 1: {
                                    if (params.isCategory1()) {
                                        manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 32), ch, i, 13));
                                    }
                                    break;
                                }//end case.
                                case 2: {
                                    if (params.isCategory2()) {
                                        manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 32), ch, i, 13));
                                    }
                                    break;
                                }//end case.
                                case 3: {
                                    if (params.isCategory3()) {
                                        manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 32), ch, i, 13));
                                    }
                                    break;
                                }//end case.
                                case 4: {
                                    if (params.isCategory4()) {
                                        manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 32), ch, i, 13));
                                    }
                                    break;
                                }//end case.
                            }
                        } else if (start < 0) {
                            //do best lengths possible!!
                            for (int j = 19; j >= 0; j--) {
                                if (cancelled) {
                                    return;
                                }
                                if (ch.compactPositions[i] - j >= 0) {
                                    start = ch.compactPositions[i] - j;
                                    if (Data.CATEGORY_VERBOSE) {
                                        ac = ch;
                                        sequence = Data.reverseCompliment(ch.sequence, start, end, 13 + j);
                                        anint = i;
                                        anotherInt = 13;
                                    }
                                    switch (ch.compactCategory[i]) {
                                        case 0: {
                                            if (params.isCategory0()) {
                                                manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 13 + j), ch, i, 13));
                                            }
                                            break;
                                        }//end case.
                                        case 1: {
                                            if (params.isCategory1()) {
                                                manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 13 + j), ch, i, 13));
                                            }
                                            break;
                                        }//end case.
                                        case 2: {
                                            if (params.isCategory2()) {
                                                manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 13 + j), ch, i, 13));
                                            }
                                            break;
                                        }//end case.
                                        case 3: {
                                            if (params.isCategory3()) {
                                                manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 13 + j), ch, i, 13));
                                            }
                                            break;
                                        }//end case.
                                        case 4: {
                                            if (params.isCategory4()) {
                                                manager.pushQuery(new Query(Data.reverseCompliment(ch.sequence, start, end, 13 + j), ch, i, 13));
                                            }
                                            break;
                                        }
                                    }
                                    break;//break because we found the longest sequence length possible and we dont want to do any shorter subsequences.
                                }//end if.
                            }//end for.
                        }//end else if.
                    }//end if.
                }//end little for search.

                if (Data.CATEGORY_VERBOSE) {
                    if (ac.ID.contains("AT2G33810.1")) {
                        //publish(ch.ID+LINE_SEPARATOR);
                        //publish(ch.printDegradomeAlignments());
                        System.out.println("The Sequence: " + new String(sequence));
                        System.out.println("Sequence Length: " + sequence.length);
                        System.out.println("Full MRNS from chart: " + new String(ac.sequence));
                        System.out.println("Degradome Alignments: " + ac.printDegradomeAlignments());
                        System.out.println("element: " + anint);
                        System.out.println("sequence start pos: " + anotherInt);
                        System.out.println("CHART ID: " + ac.ID);
                        //publish("______________________________________________________________________________________________________"+LINE_SEPARATOR);
                    }
                }
            }//End big for search.
        } catch (Exception e) {
            System.out.println("Caught in for loop search tree.");
            Logger.getLogger(Exception.class.getName()).log(Level.SEVERE, null, e);
            System.out.println(e);
            System.out.println("The Sequence: " + new String(sequence));
            System.out.println("Sequence Length: " + sequence.length);
            System.out.println("Full MRNS from chart: " + new String(ac.sequence));
            System.out.println("Degradome Alignments: " + ac.printDegradomeAlignments());
            System.out.println("element: " + anint);
            System.out.println("sequence start pos: " + anotherInt);
            System.out.println("CHART ID: " + ac.ID);
        }

        //Print the stats out if we are in command line mode.
        if (AppUtils.INSTANCE.isCommandLine()) {
            for (int i = 0; i < cmdInfoArea.size(); i++) {
                System.out.println(cmdInfoArea.get(i));
            }
        }

        totalQueries = manager.getQueryCount();
        data.setCountDown(manager.getQueryCount());

        treeBox.getOriginalSrnaTree().setManager(manager);
        treeBox.setOriginalSrnaTreeCategoryTrees();

        manager.startSearching(treeBox.getOriginalSrnaTree());
        boolean running = true;
        while (running) {

            String s = manager.accessResults(null, true);

            if (s != null) {
                publish(PRINT_GENERAL_PROGRESS);
                publish(s);
                callsToPublishIN += s.split(Data.END_OF_RECORD).length;
            } else if (s == null && manager.isFinishedRunning()) {
                running = false;
            }
        }

        synchronized (this) {
            while (callsToPublishIN != callsToPublishOUT) {
                try {
                    wait(1000000000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (AppUtils.INSTANCE.isCommandLine()) {
            System.out.println();
            System.out.println("Analysis Complete.");
            System.out.println("Time Elapsed: " + Deg_Tool_timer.getTimeElapsed());

            
            //output run information to a log file
            BufferedWriter log;
            try {
                File logFile = new File(degradomeFile.getName() + "_" + srnaFile.getName() + "_" +cdnaFile.getName() +".txt");
                logFile.createNewFile();
                log = new BufferedWriter(new FileWriter(logFile));
                log.write("------------- Log file -------------" + LINE_SEPARATOR);
                log.write(LINE_SEPARATOR);
                log.write("Degradome file: " + degradomeFile.getName() + LINE_SEPARATOR);
                log.write("sRNA file: " + srnaFile.getName() + LINE_SEPARATOR);
                log.write("Transcripts File: " + cdnaFile.getName() + LINE_SEPARATOR);

                log.write(LINE_SEPARATOR);
                log.write("------------------------------------" + LINE_SEPARATOR);
                log.write(LINE_SEPARATOR);
                //output parameters
                log.write("Parameters used:" + LINE_SEPARATOR);
                log.write(params.toDescriptiveString() + LINE_SEPARATOR);

                log.write(LINE_SEPARATOR);
                log.write("------------------------------------" + LINE_SEPARATOR);
                log.write(LINE_SEPARATOR);

                log.write("Total Redundant Degradome Reads: " + Deg_Tool_degradome_redundant_read_count + LINE_SEPARATOR);
                //The redundant size distribution of the degradome.
                log.write("Redundant Size Distribution: " + LINE_SEPARATOR);
                for (int i = params.getMinFragmentLength(); i < Deg_Tool_degradome_redundant_size_dist.length; i++) {
                    log.write(i + "\t" + Deg_Tool_degradome_redundant_size_dist[i] + LINE_SEPARATOR);
                }

                log.write("-------" + LINE_SEPARATOR);
                //Total non-redundant degradome reads.
                log.write("Total Non-redundant Degradome Reads: " + Deg_Tool_degradome_non_redundant_read_count + LINE_SEPARATOR);
                //The non-redundant size distribution of the degradome.
                log.write("Non-redundant Size Distribution: " + LINE_SEPARATOR);
                for (int i = params.getMinFragmentLength(); i < Deg_Tool_degradome_non_redundant_size_dist.length; i++) {
                    if (cancelled) {
                        break;
                    }
                    log.write(i + "\t" + Deg_Tool_degradome_non_redundant_size_dist[i] + LINE_SEPARATOR);
                }
                log.write("-------" + LINE_SEPARATOR);
                
                log.write(LINE_SEPARATOR);
                
                log.write("Transcriptome Stats:" + LINE_SEPARATOR);
                log.write("Total Transcripts: " + totalTranscripts + LINE_SEPARATOR);
                log.write("Total Transcript Plots: " + totalTranscriptPlots + LINE_SEPARATOR);
                log.write("Total Unique Degradome Alignment Positions (Candidates): " + Deg_Tool_degradome_total_unique_hits_on_transcriptome + LINE_SEPARATOR);
                log.write("-------" + LINE_SEPARATOR);
                
                log.write(LINE_SEPARATOR);
                log.write("Category Tree Information: " + LINE_SEPARATOR);
                for(String str : treeInformation)
                {
                    log.write(str);
                }
                
                log.write(LINE_SEPARATOR);
                log.write("-------" + LINE_SEPARATOR);
                log.write(LINE_SEPARATOR);
                log.write("Small RNA File Filter Stats: " + LINE_SEPARATOR);
                for(String str : srnaInformation)
                {
                    log.write(str);
                }
                
                log.write(LINE_SEPARATOR);
                log.write("-------" + LINE_SEPARATOR);
                log.write(LINE_SEPARATOR);

                //output time taken
                log.write("Analysis Complete.");
                log.write("Time Elapsed: " + Deg_Tool_timer.getTimeElapsed());
                log.close();
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (cancelled) {
            return;
        }

        if (Data.makePlotRecords || params.getAutoOutputTplotPdf()) {
            savePlotRecordBasedOutput();
        }
    }

    private void savePlotRecordBasedOutput() {
        // Create a collection of the interactions

        PlotRecordCollection prc = new PlotRecordCollection();

        for (PlotRecord pr : plotRecords.values()) {
            if (pr.hasSRNAhit()) {
                prc.add(pr);
            }
        }

        if (prc.size() == 0) {
            System.out.println("No hits found in PlotRecords");
            return;
        }

        prc.createIndex();

        if (cancelled) {
            return;
        }

        // Get the directory into which we will save "PlotRecords.txt" and/or "all-tplots.pdf"
        // They will go in the same directory as the sRNA file
        File srnaFileDir = srnaFile.getAbsoluteFile().getParentFile();

        if (srnaFileDir == null || (srnaFileDir.exists() && !srnaFileDir.canWrite())) {
            System.out.println("Could not write PlotRecords and/or T-plots to '" + srnaFileDir + "', directory either doesn't exist or is not writeable");
            return;
        }

        // Save to "PlotRecords.txt"
        if (Data.makePlotRecords) {
            String prPath = srnaFileDir.getPath() + DIR_SEPARATOR + "PlotRecords.txt";
            File prFile = new File(prPath);

            outputPlotRecords(prc, prFile);
        }

        if (cancelled) {
            return;
        }

        if (params.getAutoOutputTplotPdf()) {
            String pdfPath = srnaFileDir.getPath() + DIR_SEPARATOR + "all-tplots.pdf";
            File pdfFile = new File(pdfPath);

            outputTplotPdf(prc, pdfFile);
        }
    }

    /**
     * Output the plot records to a file called 'PlotRecords.txt' in the same
     * folder as the sRNAs.
     */
    private void outputPlotRecords(PlotRecordCollection prc, File prFile) {
        if (prc == null || prFile == null) {
            return;
        }

        // Put PlotRecords in same directory as the srnas
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(prFile));

            for (PlotRecord pr : plotRecords.values()) {
                if (cancelled) {
                    return;
                }

                if (pr.hasSRNAhit()) {
                    PlotRecord.outputPlotRecord(writer, pr);
                }
            }

            writer.close();

            if (AppUtils.INSTANCE.verbose()) {
                System.out.println("Written PlotRecords to " + prFile.getAbsolutePath());
            }
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Output the plot records to a PDF file containing all of the PlotRecords
     */
    private void outputTplotPdf(PlotRecordCollection prc, File pdfFile) {
        TPlotFrame.saveToPdf(prc, pdfFile);

        if (AppUtils.INSTANCE.verbose()) {
            System.out.println("Saved T-plots to " + pdfFile);
        }
    }

    @Override
    protected void done() {
        // Update the GUI controls here (on the event dispatch thread)
        //
        if (!AppUtils.INSTANCE.isCommandLine()) {
            if (cancelled) {
                _guiFeedback.done(false);
            } else {
                finalizeTable();

                _guiFeedback.addProgressMessage("Analysis Complete." + LINE_SEPARATOR, true);
                _guiFeedback.addProgressMessage("Time Elapsed: " + Deg_Tool_timer.getTimeElapsed() + LINE_SEPARATOR, true);

                _guiFeedback.done(true);
            }
        }

        System.gc();
    }

    /**
     * This function starts the processing of the small rnas and calls other
     * functions to build the small rna tree..
     */
    public void makeSmallRNATree() {
        //Get the srnas remaining after filter has been applied.
        FastaMap filteredSmallRNAs = getFilteredSmallRNAs();

        //Make sure the progress bar is back into indeterminate mode.
        edtHelper.setProgressBarIndeterminate(true);

        //Build the small rna tree using the results from the previous step.
        buildSmallRNATree(filteredSmallRNAs);
    }//end method.

    /**
     * Builds the small rna tree.
     *
     * @param smallRnaMap The non redundant map of small rnas.
     */
    private void buildSmallRNATree(FastaMap smallRnaMap) {
        //Get a map of IDs. These are the headders from the original small rna sequence file.
        HashMap<String, SmallRNAMapHelper> idList = getSmallRNAsIdMap();
        publish(PRINT_PROGRESS_BAR_STRING + "Building Small RNA Tree...");
        infoLabel.setText("Building Small RNA Tree...");
        //Get an iterator for the useful small rna map.
        Iterator<String> smallRnaMapItr = smallRnaMap.keySet().iterator();
        //Iterate through them..
        while (smallRnaMapItr.hasNext()) {
            if (cancelled) {
                return;
            }
            //Get the next sequence.
            String key = smallRnaMapItr.next();
            //Get the abundance for the sequence.
            int abundance = smallRnaMap.get(key).getAbundance();
            //Get the id for this sequence.
            String id = idList.get(key).getID();
            //double check that the abundance is within the bounds of the cut off.

            //Set the id.
            Tree.miID = id;
            //Change the sequence into a byte array.
            byte[] temp = StringUtils.changeStringToByteArray(key);
            //Add the sequence to the tree. Note, we add the sequence to the tree however many times needed for the abundance.
            for (int i = 0; i < abundance; i++) {
                if (cancelled) {
                    return;
                }
                //Add the sequence..
                treeBox.getOriginalSrnaTree().addToTree(temp);
                //Increment the raw read count (AF = After Filter)
                totalRawShortReads_AF++;
            }//end for.
            //Increment the total number of unique sequences within the tree.
            totalShortReadsInSearchSpace_AF++;

        }//end while.
        //Tell the tree we just made how many degredation fragments there are.
        treeBox.getOriginalSrnaTree().setTotalFragmentCount(Deg_Tool_degradome_redundant_read_count);
        treeBox.getOriginalSrnaTree().setTotalShortReads(totalRawShortReads_AF);
    }//end method.

    /**
     * This method builds a hash map of sequences and their IDs from the
     * original small rna fasta file. This is to overcome a limitation in the
     * Filter object.
     *
     * @return A hash map of sequences and their IDs.
     */
    private HashMap<String, SmallRNAMapHelper> getSmallRNAsIdMap() {
        publish(PRINT_PROGRESS_BAR_STRING + "Building Small RNA ID Map...");
        infoLabel.setText("Building Small RNA ID Map...");
        //Get some memory and make a new hash map to store the ids.
        HashMap<String, SmallRNAMapHelper> smallRnaIdMap = new HashMap<String, SmallRNAMapHelper>();
        //Make a new text reader.
        TextReader r = new TextReader();
        //Open up the small rnas file.
        r.openFileA(srnaFile.getAbsolutePath());
        try {
            //Get the first line in the file.
            String s = r.getLineFileA();
            //Set the id to null
            String id = "";
            //While we still have lines to read...
            while (s != null) {
                //If the line is a headder line..
                if (s.startsWith(">")) {
                    //Then set the id string.
                    id = s;
                } else //It must be a sequence line...
                //If we have already seen this sequence...
                 if (smallRnaIdMap.containsKey(s)) {
                        //Tell the map helper that this sequence has more than one id and let it sort it out.
                        smallRnaIdMap.get(s).addAdditionalID(id);
                    } else {
                        //We have not seen this sequence before so set up a new record for it.
                        smallRnaIdMap.put(s, new SmallRNAMapHelper(id));
                    }//end else.//end else.                //Get the next line in the file.
                s = r.getLineFileA();
            }//end while.
            //Close the io stream.
            r.closeFileA();
            //Catch any IO exception...
        } catch (IOException ex) {
            Logger.getLogger(Degradome.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Return the hash map of small RNA IDs.
        return smallRnaIdMap;
    }//end method.

    /**
     * Uses a Filter object to filter the small rnas in the srnaome. This method
     * uses the filter object to remove sequences based on length, complexity,
     * abundance,invalid characters,trna and not mapping to genome if supplied.
     * This method prints to the "information log/text area" the stats supplied
     * by the Filter object showing the reducing balance of sequences retained
     * after the different filters were applied.
     *
     * @return A Map object containing non redundant sequences which have not
     * been filtered out < sequence, abundance>.
     */
    private FastaMap getFilteredSmallRNAs() {

        publish(PRINT_PROGRESS_BAR_STRING + "Filtering Small RNAs...");
        infoLabel.setText("Filtering Small RNAs...");
        try {
            //Make a file pointing to the user directory.
            File userDirectory = new File(Tools.userDataDirectoryPath);
            //Set the minimum small rna abundance to that in the parameter.
            int minimumAbundance = params.getMinSrnaAbundance();
            //Check to see if the user wants a secondary output...
            if (params.isSecondaryOutputToFile()) {
                //If they do, then set the minimum to one i.e. don't filter based on abundance.
                minimumAbundance = 1;
            }

            FilterParams filterParams = new FilterParams.Builder()
                    .setMinLength(params.getMinSrnaLength())
                    .setMaxLength(params.getMaxSrnaLength())
                    .setMinAbundance(minimumAbundance)
                    .setFilterLowComplexitySeq(params.isDiscardLowComplexitySRNAs())
                    .setFilterInvalidSeq(true)
                    .setDiscardTRRNA(params.isDiscardTrrna())
                    .setOutputNonRedundant(true)
                    .setGenome(genome).build();
            filter = new Filter(srnaFile, null, userDirectory, filterParams, new StatusTracker(progOver, infoLabel));
            filter.run();

            //Get the results from the filter.
            ArrayList<FilterStage> stats = filter.getStats();
            //Print out the stats to the infomation area.
            publish(PRINT_TO_INFO_TEXT_AREA + "Small RNA File Filter Stats:" + LINE_SEPARATOR);
            publish(PRINT_TO_INFO_TEXT_AREA + "Read Counts: \t\t\t\t\tTOTAL\t\tNR" + LINE_SEPARATOR);
            for (int i = 0; i < stats.size(); i++) {

                if (stats.get(i).getStageName().startsWith("filter by max-abundance")) {
                    publish(PRINT_TO_INFO_TEXT_AREA + stats.get(i).getStageName() + "\t\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                    srnaInformation.add(stats.get(i).getStageName() + "\t\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                } else if (stats.get(i).getStageName().startsWith("filter by min-abundance and max-abundance")) {
                    publish(PRINT_TO_INFO_TEXT_AREA + stats.get(i).getStageName() + "\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                    srnaInformation.add(stats.get(i).getStageName() + "\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                } else if (stats.get(i).getStageName().startsWith("input")) {
                    publish(PRINT_TO_INFO_TEXT_AREA + stats.get(i).getStageName() + "\t\t\t\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                    srnaInformation.add(stats.get(i).getStageName() + "\t\t\t\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                } else {
                    publish(PRINT_TO_INFO_TEXT_AREA + stats.get(i).getStageName() + "\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                    srnaInformation.add(stats.get(i).getStageName() + "\t\t\t" + stats.get(i).getTotalReadCount() + "\t\t" + stats.get(i).getDistinctReadCount() + LINE_SEPARATOR);
                }
            }
            publish(PRINT_TO_INFO_TEXT_AREA + "-------" + LINE_SEPARATOR);
        } catch (Exception ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filter.getData();
    }//end method.

    /**
     * Start searching on a worker thread.
     *
     * @return Void
     */
    @Override
    protected Void doInBackground() {
        try {
            engineRunning = true;
            startSearching();
            engineRunning = false;
        } catch (Exception e) {
            Logger.getLogger(Exception.class.getName()).log(Level.SEVERE, null, e);
            if (AppUtils.INSTANCE.isCommandLine()) {
                System.out.println(e);
                System.out.println("System exiting..");
                System.exit(1);
            } else {
                String ErrorMessage = e.toString();
                JOptionPane.showMessageDialog(infoLabel, ErrorMessage, "PAREsnip Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }//end method.

    /**
     * Process out results found by the tool.
     *
     * This is executed on the 'event dispatch thread' so GUI components can be
     * updated directly
     *
     * @param chunks
     */
    @Override
    protected void process(List<String> chunks) {
        if (AppUtils.INSTANCE.isCommandLine()) {
            boolean outputGeneralProgress = false;

            for (String chunk : chunks) {
                if (chunk.startsWith(PRINT_TO_INFO_TEXT_AREA)) {
                    cmdInfoArea.add(chunk.substring(2));
                } else if (chunk.startsWith(PRINT_PROGRESS_BAR_STRING)) {
                } else if (chunk.startsWith(PRINT_GENERAL_PROGRESS)) {
                    outputGeneralProgress = true;
                } else {
                    //We have some records to output, so split the chunk into records.
                    String[] records = chunk.split(Data.END_OF_RECORD);

                    // Split each record and output it
                    for (String record : records) {
                        try {
                            String[] field = record.split(Data.END_OF_FIELD);
                            out.write(field[0] + "\t"); //Gene
                            out.write(field[1] + "\t"); //category.
                            out.write(field[2] + "\t"); //cleavage
                            out.write(field[3] + "\t"); //p-value
                            out.write(field[4] + "\t"); //abundance.
                            out.write(field[5] + "\t"); //weighted abundance.
                            out.write(field[6] + "\t"); //Norm weighted abundance.
                            out.write(field[8] + "\t"); //Alignment score.
                            out.write(field[9] + "\t"); //short read id.
                            out.write(field[10] + "\t"); //Short read abundance.
                            out.write(field[11] + "\t"); //Normalised short read abundance.
                            out.newLine();
                            if (Deg_Tool_printingAlignmentsInCMD) {
                                out.write(field[7] + ""); //Duplex
                                out.newLine();
                            }
                            out.write("-----");
                            out.newLine();
                        } catch (IOException ex) {
                            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        callsToPublishOUT++;
                        if (callsToPublishIN == callsToPublishOUT) {
                            synchronized (this) {
                                notify();
                            }
                        }//end if.
                    }//end inner for.
                }//end else.
            }//end outer for.

            if (outputGeneralProgress && Data.cmdVerbose) {
                int processedQueries = totalQueries - data.accessCountDown(false);
                float pc = 100f * processedQueries / totalQueries;
                System.out.println(String.format("Processed %d of %d (%d%%)", processedQueries, totalQueries, Math.round(pc)));
            }
        } else {
            //For each chunk of data passed via publish.
            for (String chunk : chunks) {
                //If reset was requested by the user, don't process anything.
                if (cancelled) {
                    return;
                }
                //If the prefix says to print to the infomation text area...
                if (chunk.startsWith(PRINT_TO_INFO_TEXT_AREA)) {
                    //Remove the prefix and append it to the information text area.
                    infoOut.append(chunk.substring(2));

                    //Else if the prefix says to print to the progress bar...
                } else if (chunk.startsWith(PRINT_PROGRESS_BAR_STRING)) {
                    //Remove the prefix and set the string on the progress bar.
                    progOver.setString(chunk.substring(1));

                    //Else if the prefix says to print general progress...
                } else if (chunk.startsWith(PRINT_GENERAL_PROGRESS)) {
                    //Calculate and display the time in the info lable area.
                    calculateAndShowEstimatedTimeRemaining();

                    //Else if it was none of the above and (We are reporting in table mode - (now the only way to report))...
                } else if (Data.IS_TABLE_MODE) {//TABLE MODE OUTPUT.
                    //We have some records to output, so split the chunk into records.
                    String[] records = chunk.split(Data.END_OF_RECORD);
                    //For each record we have, split it into fields and add to the table.
                    for (String record : records) {
                        if (cancelled) {
                            return;
                        }
                        String[] field = record.split(Data.END_OF_FIELD);
                        Integer abundance = Integer.parseInt(field[10]);
                        //If Primary hit....i.e. small rna abundance is greater or equal to the small rna abundance cut off.

                        if (abundance >= params.getMinSrnaAbundance()) {
                            //If this hit is at the same position and gene as the previous hit...
                            if (params.isSubsequenceSecondaryHit() && previousField != null && field[0].equalsIgnoreCase(previousField[0]) && field[2].equalsIgnoreCase(previousField[2])) {
                                //If it is a subsequence of the previous hit...
                                if (isSubsequenceOfPrevious(field)) {
                                    //If this subsequence has a higher abundance than the previous one...
                                    if (abundance > Integer.parseInt(previousField[10])) {
                                        //Then remove the previous one from the table...
                                        tableModel.removeRow(tableModel.getRowCount() - 1);
                                        //add the previously one to the seconday hit list.
                                        addSecondaryHit(previousField);
                                        //Put the new hit in its place.
                                        addToTable(field);
                                    } else {//This hit is a subsequence but does not have a higher abundance, so add it to the secondary hits list.
                                        addSecondaryHit(field);
                                    }
                                } else {//else it is not a subsequence of the previous, so just add it to the table because it's a new hit.
                                    addToTable(field);
                                }
                            } else {//else it is not at the same position, so just add it to the table because it's a new hit.
                                addToTable(field);
                            }
                        } else {
                            //It is a secondary hit because the abundance is less than the small rna abundance cut off.
                            //Add this secondary hit to the list of secondary hits ready for output to file.
                            addSecondaryHit(field);
                        }//end else not a primary hit.
                        callsToPublishOUT++;
                        if (callsToPublishIN == callsToPublishOUT) {
                            synchronized (this) {
                                notify();
                            }
                        }
                    }//end records for.
                }//end else if table mode.
            }//end chunks for.
        }//end else not cmd line.
    }//end method.

    /**
     * Tests if the small rna in the field array provided is a subsequence of
     * the previous small rna in the field array previously added to the table.
     * Note: The previously added small rna may be a subsequence of that
     * provided and this is tested for.
     *
     * @param field An array of fields - element 7 must be the duplex.
     * @return true if one is a subsequence of the other.
     */
    private boolean isSubsequenceOfPrevious(String[] field) {

        if (previousField == null) {
            return false;
        }
        String sequence = getSequence(field);
        String previousSequence = getSequence(previousField);

        String longest;
        String shortest;
        if (sequence.length() <= previousSequence.length()) {
            shortest = sequence;
            longest = previousSequence;
        } else {
            shortest = previousSequence;
            longest = sequence;
        }

        boolean result = longest.contains(shortest);

        if (AppUtils.INSTANCE.verbose()) {
            System.out.println("PAREsnip Engine: isSubsequenceOfPrevious = " + result);
        }

        return result;
    }//end method.

    /**
     * Gets the small RNA sequence out of the html encoded duplex.
     *
     * @param field The array of fields where element 7 is the duplex.
     * @return The small RNA sequence out of the duplex.
     */
    private String getSequence(String[] field) {
        return field[7].split("3'")[0].replace("5'", "").trim();
    }//end method.

    /**
     * Add a secondary hit to the secondary hits list.
     *
     * @param field The array of fields containing info about the hit.
     */
    private void addSecondaryHit(String[] field) {
        ArrayList<String[]> secHits = secondaryHits.get(field[0]);//.add(field);
        if (secHits == null) {
            ArrayList<String[]> newEntry = new ArrayList<String[]>();
            newEntry.add(field);
            secondaryHits.put(field[0], newEntry);
        } else {
            secondaryHits.get(field[0]).add(field);
        }
    }//end method.

    /**
     * Add a record to the table in GUI mode.
     *
     * @param field The array of fields containing information about the hit.
     */
    private void addToTable(String[] field) {
        previousField = field;

        String geneId = field[0];
        Integer category = Integer.valueOf(field[1]);
        Integer cleavagePosition = Integer.valueOf(field[2]);
        Float pValue = Float.valueOf(field[3]);
        Integer abundance = Integer.valueOf(field[4]);
        Float weightedAbundance = Float.valueOf(field[5]);
        Float normalisedWeightedAbundance = Float.valueOf(field[6]);
        String wholeDuplex = field[7];
        Float alignmentScore = Float.valueOf(field[8]);
        Object shortReadId = field[9];
        Integer shortReadAbundance = Integer.valueOf(field[10]);
        Float normalisedShortReadAbundance = Float.valueOf(field[11]);

        Object[] typedFields = {
            Boolean.FALSE, // 'select' column
            geneId,
            category,
            cleavagePosition,
            pValue,
            abundance,
            weightedAbundance,
            normalisedWeightedAbundance,
            wholeDuplex,
            alignmentScore,
            shortReadId,
            shortReadAbundance,
            normalisedShortReadAbundance,
            Integer.valueOf(recordID),
            null
        };

        tableModel.insertRow(tableModel.getRowCount(), typedFields);
        recordID++;

        if (Data.makePlotRecords) {
            // Split the duplex string into (in order) the smallRNA, the alignment bars and the mRNA.
            String[] duplex = wholeDuplex.split(LINE_SEPARATOR);

            PlotRecord pr = plotRecords.get(geneId);

            pr.addS_Hit(cleavagePosition.intValue(),
                    shortReadAbundance.intValue(),
                    category.intValue(),
                    alignmentScore.floatValue(),
                    pValue.floatValue(),
                    duplex[0],
                    duplex[1],
                    duplex[2]);
        }
    }//end method.

    /**
     * Calculates and displays the estimated time remaining until analysis
     * completion. The estimated time is displayed via the progress bar and
     * information label.
     */
    public String calculateAndShowEstimatedTimeRemaining() {
        //Get how much time has elapsed since starting the process.
        long timeElapsed = System.nanoTime() - startTime;
        //Get how many queries have been processed in that time.
        int processedQueries = totalQueries - data.accessCountDown(false);
        //Get the average amount of time it takes to process a query.
        long averageNanosPerSequence = timeElapsed / processedQueries;
        //Get the number of queires left to process.
        long countLeft = totalQueries - processedQueries;
        //Get the averate time in nanos it will take to process what is left.
        long nanosLeft = countLeft * averageNanosPerSequence;
        //Turn that into milliseconds.
        long millisLeft = nanosLeft / 1000000;
        //Calculate the hours left.
        long hoursLeft = ((millisLeft / 1000) / 60) / 60;
        long hoursLeftR = hoursLeft * 60 * 60 * 1000;
        //Calculate the minuites left.
        long minsLeft = ((millisLeft - hoursLeftR) / 1000) / 60;
        long minsLeftR = minsLeft * 60 * 1000;
        long secsLeft = (((millisLeft - hoursLeftR) - minsLeftR) / 1000);
        //Print it all out to the information label.
        String labelText = "Estimated Time Remaining: h " + hoursLeft + " m " + minsLeft + " s " + secsLeft;
        //Update the label and the progress bar.
        infoLabel.setText(labelText);
        progOver.setString("" + processedQueries + " of " + totalQueries + " peaks processed..");
        return labelText;
    }//end method.

    public void finalizeTable() {
        int rows = tableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            Integer secondHitCount = (Integer) getSecondaryHitsCount((String) tableModel.getValueAt(i, 1), (Integer) tableModel.getValueAt(i, 3));
            tableModel.setValueAt(secondHitCount, i, 14);
        }

    }//end method.

    /**
     * Gets the number of secondary hits
     *
     * @param field
     * @return
     */
    public int getSecondaryHitsCount(String gene, Integer position) {
        int count = 0;
        ArrayList<String[]> notPrimaryHits = secondaryHits.get(gene);
        if (notPrimaryHits != null) {
            for (int i = 0; i < notPrimaryHits.size(); i++) {
                if (position == Integer.parseInt(notPrimaryHits.get(i)[2])) {
                    count++;
                }
            }
        }
        return count;
    }//end method.

    private void generateSrnas(ArrayList<Chart> charts) {

//        try{
//
//            Random rand = new Random(System.currentTimeMillis());
//
//            String name = "20000srnas.fa";
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 20000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//
//            name = "40000srnas.fa";
//            writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 40000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//
//            name = "60000srnas.fa";
//            writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 60000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//
//            name = "80000srnas.fa";
//            writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 80000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//            name = "100000srnas.fa";
//            writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 100000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//
//            name = "1000000srnas.fa";
//            writer = new BufferedWriter(new FileWriter(new File(name)));
//            for(int i = 0; i < 1000000; i++){
//                int randomChart = rand.nextInt(charts.size());
//                String randomSrna = charts.get(randomChart).getRandomSrnaFromTranscriptPeakOfRandomLength(rand);
//                if(randomSrna == null){
//                    i--;
//                }else{
//                    writer.write(">");
//                    writer.newLine();
//                    writer.write(randomSrna);
//                    writer.newLine();
//                }
//            }
//            writer.close();
//        }catch(IOException e){
//            System.out.println("Exception in generateSrnas(ArrayList<Chart> charts)");
//        }
    }

    void populatePlotRecordCollection(PlotRecordCollection prc) {
        for (PlotRecord pr : plotRecords.values()) {
            prc.add(pr);
        }

        prc.createIndex();
    }

    private static class EDTHelper {

        private final boolean _isCommandLine = AppUtils.INSTANCE.isCommandLine();
        private final JProgressBar _prog;

        EDTHelper(JProgressBar progressBar) {
            _prog = progressBar;
        }

        void setProgressBarIndeterminate(final boolean value) {
            if (_isCommandLine || _prog == null || !_prog.isVisible()) {
                return;
            }

            if (SwingUtilities.isEventDispatchThread()) {
                _prog.setIndeterminate(value);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (_prog.isVisible()) {
                            _prog.setIndeterminate(value);
                        }
                    }
                });
            }
        }

        void setProgressBarVisible(final boolean value) {
            if (_isCommandLine || _prog == null) {
                return;
            }

            if (SwingUtilities.isEventDispatchThread()) {
                _prog.setVisible(value);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        _prog.setVisible(value);
                    }
                });
            }
        }
    }
}
