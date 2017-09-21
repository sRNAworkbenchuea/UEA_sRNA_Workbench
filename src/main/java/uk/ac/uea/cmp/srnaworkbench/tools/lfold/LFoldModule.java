package uk.ac.uea.cmp.srnaworkbench.tools.lfold;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

public class LFoldModule extends WorkflowModule {

    // inputs
    private final DataContainerList<GenomeManager> in_genome;
    private final DataContainerList<StringBuilder> in_sRNAQuery;
    // outputs
    private final DataContainerList<StringBuilder> out_predictionQuery;

    private List<LfoldThread> allThreads;
    private int numInSequences;
    private int extend;

   // private final LFoldViewer viewer;

    public LFoldModule(String id, String title) {

        super(id, title);
        this.numInSequences = 0;
        this.extend = 350;

        this.allThreads = new LinkedList<>();

        // inputs
        this.in_genome = new DataContainerList<>("genome", CompatibilityKey.GENOME, 1, 1);
        this.in_sRNAQuery = new DataContainerList<>("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, 1);
        // outputs
        this.out_predictionQuery = new DataContainerList<>("predictionQuery", CompatibilityKey.PREDICTION_QUERY, 1, 1);
        // set-up keys for data input/output containers
        try {
            this.out_predictionQuery.add(new DataContainer<>(CompatibilityKey.PREDICTION_QUERY, new StringBuilder()));

            addInputDataContainerList(this.in_genome);
            addInputDataContainerList(this.in_sRNAQuery);
            addOutputDataContainerList(this.out_predictionQuery);
        } catch (Exception ex) {
        //    log.addLn("ERROR: could not construct data containers for lfold: " + ex);
        }
        //Uncomment for working L Fold
        //init();

        setFXMLResource("/fxml/LFoldScene.fxml");
        this.controller = new LFoldController(this);
       

    }

    private void init() {
        String version;
        try {
            version = getVersion();
        } catch (IOException | InterruptedException e) {
            version = "";
         //   log.addLn("ERROR: Could not identify LFold version!");
        }
    }


    private String getVersion() throws IOException, InterruptedException {
   /*     // get the version of the tool
        File versionFile = new File("LFold_version.tmp");
        String[] arguments = {RNALfoldPath, "--version"};
        runExe(arguments, null, versionFile, null);

        String versionStr = "";
        Scanner in = new Scanner(versionFile);
        while (in.hasNextLine()) {
            versionStr += in.nextLine();
        }
        in.close();
        versionFile.delete();
        return versionStr;*/
        return "";
    }

    public List<Prediction_Entity> getPredictions(int seqNum) {
        if (this.allThreads != null && this.allThreads.size() > seqNum && this.allThreads.get(seqNum) != null) {
            return this.allThreads.get(seqNum).getPredictions();
        }
        return new LinkedList<>();
    }

    public boolean isPredictionComplete(int seqNum) {
        if (this.allThreads != null && this.allThreads.size() > seqNum && this.allThreads.get(seqNum) != null) {
            return !this.allThreads.get(seqNum).isAlive() && this.allThreads.get(seqNum).complete;
        }
        return false;
    }

    public int getNumInputSequences() {
        return this.numInSequences;
    }

   

    public void setExtend(int extend) {
        this.extend = extend;
    }

    public List<List<String>> generateOutput() throws FileNotFoundException {
      /*  String sql = this.out_predictionQuery.getContainer(0).getData().toString();
        PredictionServiceImpl prediction_service = (PredictionServiceImpl) Database.getInstance().getContext().getBean("PredictionService");
        List<Prediction_Entity> prediction_list = prediction_service.getPredictionsFromSQL(sql + " LIMIT 25");
        List<List<String>> table_output = new LinkedList<>();
        table_output.add(Prediction_Entity.toStringTitleArray());
        for (Prediction_Entity p : prediction_list) {
            table_output.add(p.toStringArray());
        }
        return table_output;*/
        
   /*             PredictionServiceImpl prediction_service = (PredictionServiceImpl) Database.getInstance().getContext().getBean("PredictionService");
        DetachedCriteria criteria = prediction_service.getPredictionsByID();
        List<Prediction_Entity> prediction_list = prediction_service.getPredictionsFromSQL(criteria);

        List<List<String>> table_output = new LinkedList<>();
        table_output.add(Prediction_Entity.toStringTitleArray());
        for (Prediction_Entity p : prediction_list) {
            table_output.add(p.toStringArray());
        }*/
        return new LinkedList<>();//table_output;
    }

    @Override
    public void process() {
/*
        try {

            PredictionServiceImpl prediction_service = (PredictionServiceImpl) Database.getInstance().getContext().getBean("PredictionService");
            AlignedSequenceServiceImpl aligned_sequence_service = (AlignedSequenceServiceImpl) Database.getInstance().getContext().getBean("AlignedSequenceService");

            log.addLn("INFORMATION: Lfold Biogenesis Tool Started");
            log.addLn("INFORMATION: Lfold Version: " + this.getDescription());
            log.addLn("PARAMETER: flanking Region: " + extend);
            
            ThreadManager threadManager = new ThreadManager(this.log);
            // get all aligned sequences for all samples
            String in_sequences_sql = this.in_sRNAQuery.getContainer(0).getData().toString();
      
            String aligned_sequences_sql = WFQuery.innerJoin("T1.*", "SELECT * FROM ALIGNED_SEQUENCES", in_sequences_sql, "T1.RNA_Sequence = T2.RNA_Sequence");
            
            List<Aligned_Sequences_Entity> alignedSequences = aligned_sequence_service.getAlignedSequencesFromSQL(aligned_sequences_sql);

            log.addLn("INFORMATION: Folding");
            for (Aligned_Sequences_Entity aln : alignedSequences) {
                LfoldThread thread = new LfoldThread(this.in_genome.getContainer(0).getData(), aln.getId().getChrom(), aln.getId().getStart() - 1, aln.getId().getEnd() - 1, aln.getId().getStrand(), this.extend);
                threadManager.addThread(thread);
                allThreads.add(thread);
            }
            threadManager.run();
            log.addLn("INFORMATION: Filtering Predictions");
            List<Long> idList = new LinkedList<>();
            for (LfoldThread lft : allThreads) {
                for (Prediction_Entity p : lft.getPredictions()) {
                    prediction_service.saveOrUpdate(p);
                    idList.add(p.getID());
                }
            }
            // select all of these predictions by id
            String sql = DatabaseWorkflowModule.sqlSelectByPrimaryKey(idList, "PREDICTIONS", "ID");
            this.out_predictionQuery.getContainer(0).getData().append(sql);

            //log.addLn("INFORMATION: " + this.out_predictions.getContainer(0).getData().size() + " Predictions Generated");
            log.addLn("INFORMATION: Lfold Biogenesis Tool Completed");
            //precursor_service.printToFile(new File("precursor_table.txt"));
            complete();
           

        } catch (BeansException | IOException e) {
            log.addLn(e.toString());
            complete();
        }*/
      //  complete();
    }
}
