package uk.ac.uea.cmp.srnaworkbench.tools.mirpare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Rectangle2D;
import javafx.util.Pair;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import uk.ac.uea.cmp.srnaworkbench.binaryexecutor.BinaryExecutor;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Expression_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.GFF_Attribute_Entity;

import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Unique_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.ExpressionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.InteractionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.UniqueSequencesServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import uk.ac.uea.cmp.srnaworkbench.utils.JsonUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

public class MiRPAREModule extends WorkflowModule {

    // inputs
    private final DataContainerList<HQLQuerySimple> in_interactionQuery;
    private final DataContainerList<HQLQuerySimple> in_predictionQuery;
    private MiRPAREController c;
    private boolean complete;
    public List<String> filenames;
    public int nDeg;
    public List<NormalisationType> normTypes;
    private boolean showOnlyFunctionalInteractionsWithHairpins;

    List<HQLQueryComplex> inInteractionQueriesIDs;
    List<HQLQueryComplex> inPredictionQueriesIDs;

    public String out;

    public MiRPAREModule(String id, String title, Rectangle2D visualBounds) {
        super(id, title);
        this.complete = false;
        this.showOnlyFunctionalInteractionsWithHairpins = false;
        // inputs
        this.in_interactionQuery = new DataContainerList<>("interactionQuery", CompatibilityKey.INTERACTION_QUERY, 1, -1);
        this.in_predictionQuery = new DataContainerList<>("predictionQuery", CompatibilityKey.PREDICTION_QUERY, 1, -1);
        // generate a map of the unique functional srnas
        // this.predictons_by_functional_srna = new HashMap<>();
        //this.allPredictions = new LinkedList<>();
        //this.allInteractions = new LinkedList<>();
        // this.viewer = new BioFuncViewer(this, "/fxml/BioFuncScene.fxml", "/styles/Styles.css");
        setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + "MiRPAREScene.fxml");
        c = new MiRPAREController(this);
        this.controller = c;
        this.filenames = null;
        this.normTypes = null;
        try {
            addInputDataContainerList(this.in_interactionQuery);
            addInputDataContainerList(this.in_predictionQuery);
        } catch (InitialisationException | DuplicateIDException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public boolean isComplete() {
        return this.complete;
    }

    public HQLQuery generatePredictionOutput(String srna) throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        HQLQueryComplex hqlQuery = new HQLQueryComplex();

        String predictionQuery = "";
        for (int i = 0; i < this.in_predictionQuery.getListLength(); i++) {
            if (i > 0) {
                predictionQuery += " OR ";
            }
            predictionQuery += String.format("P.id IN (%s)", this.inPredictionQueriesIDs.get(i).eval());
        }

        hqlQuery.addSelect("P.id", "ID");
        hqlQuery.addSelect("P.precursor.alignment.id.chrom", "Chr");
        hqlQuery.addSelect("P.precursor.alignment.id.start", "Start");
        hqlQuery.addSelect("P.precursor.alignment.id.end", "End");
        hqlQuery.addSelect("P.precursor.alignment.id.strand", "Strand");
        hqlQuery.addSelect("concat(P.precursor.alignment.rna_sequence, concat('" + IOUtils.LINE_SEPARATOR + "', P.precursor.structure))", "Hairpin");
        hqlQuery.addSelect("P.precursor.mfe", "MFE");
        hqlQuery.addSelect("P.mature.rna_sequence", "Mature");
        hqlQuery.addSelect("P.mature.id.start", "Mature_Start");
        hqlQuery.addSelect("P.mature.id.end", "Mature_End");
        hqlQuery.addSelect("P.star.rna_sequence", "Star");
        hqlQuery.addSelect("P.star.id.start", "Star_Start");
        hqlQuery.addSelect("P.star.id.end", "Star_End");
        hqlQuery.addSelect("P.predictor", "Predictor");
        hqlQuery.addFrom(Prediction_Entity.class, "P");
        hqlQuery.addWhere(predictionQuery);
        hqlQuery.addWhere(String.format("P.mature.rna_sequence = '%s'", srna));
        return hqlQuery;

        /* String q = "SELECT "
         + "P.id AS id, "
         + "P.precursor.alignment.id.chrom AS chr, "
         + "P.precursor.alignment.id.start AS start, "
         + "P.precursor.alignment.id.end AS end, "
         + "P.precursor.alignment.id.strand AS strand, "
         + "P.precursor.alignment.rna_sequence AS hpSeq, "
         + "P.precursor.structure AS hpStr, "
         + "P.precursor.mfe AS mfe, "
         + "P.mature.rna_sequence AS mature, "
         + "P.mature.id.start AS matureStart, "
         + "P.mature.id.end AS matureEnd, "
         + "P.star.rna_sequence AS star, "
         + "P.star.id.start AS starStart, "
         + "P.star.id.end AS starEnd ";
         q += String.format("FROM Prediction_Entity P WHERE (%s) AND P.mature.rna_sequence = '%s'", predictionQuery, srna);
         return q;*/
    }

    public static void createTPlots(boolean allPredictors, String predictorID) throws FileNotFoundException {

        String[] categoryColours = {"red", "pink", "yellow", "green", "brown"};
        InteractionServiceImpl interaction_service = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");

        List<Interaction_Entity> interactions = null;
        if (allPredictors) {
            interactions = interaction_service.getInteractionsOrderByGene();
        } else {
            interactions = interaction_service.getInteractionsOrderByGene(predictorID);
        }
        PrintWriter writer = null;
        String currentGene = "";
        JsonArrayBuilder pointsArray = null;
        JsonArrayBuilder legendArray = null;
        JsonObjectBuilder json = Json.createObjectBuilder();
        // how many target predictors are there?
        List<String> predictors = interaction_service.getPredictorList();
        // get all the interactions (ordered by gene id)
        for (Interaction_Entity i : interactions) {
            // if we are now looking at a different gene
            if (!currentGene.equals(i.getGene().getID() + "")) {
                // save any current file
                if (writer != null) {
                    json.add("legend", legendArray);
                    json.add("points", pointsArray);
                    JsonObject jsonObj = json.build();
                    JsonWriter jsonWriter = Json.createWriter(writer);
                    jsonWriter.writeObject(jsonObj);
                    jsonWriter.close();
                }
                // open new file
                File file = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + i.getGene().getID() + "_" + predictorID + "_tplot.json");
                writer = new PrintWriter(file);
                // save json array as single object
                json = Json.createObjectBuilder();
                pointsArray = Json.createArrayBuilder();
                legendArray = Json.createArrayBuilder();

                JsonObjectBuilder axesNode = Json.createObjectBuilder();
                int xmin = 1;
                int xmax = i.getGene().getSequenceLength();
                if (xmax < xmin) {
                    System.out.println("There was an error obtaining the length of transcript " + i.getGene().toString());
                    xmax = xmin;
                }
                axesNode.add("xmin", xmin);
                axesNode.add("xmax", xmax);
                axesNode.add("xlabel", "Transcript Position");
                axesNode.add("ylabel", "Normalised Weighted Fragment Abundance");
                json.add("axis", axesNode);

                // add cateogries to legend
                for (int category = 0; category < 5; category++) {
                    JsonObjectBuilder legendNode = Json.createObjectBuilder();
                    legendNode.add("key", "Category " + category);
                    legendNode.add("nPoints", 20);
                    legendNode.add("radius", 3);
                    legendNode.add("color", categoryColours[category]);
                    legendArray.add(legendNode);
                }
                // add degradomes to legend
                for (int degradome = 0; degradome < predictors.size(); degradome++) {
                    JsonObjectBuilder legendNode = Json.createObjectBuilder();
                    legendNode.add("key", "Degradome " + (degradome + 1));
                    legendNode.add("nPoints", degradome + 3);
                    legendNode.add("radius", 3);
                    legendNode.add("color", "black");
                    legendArray.add(legendNode);
                }

            }
            // add interaction to gene json file
            String labelText = "NW Fragment Abundance: " + i.getNormalisedWeightedFragmentAbundance() + "/n"
                    + "Fragment Abundance: " + i.getFragmentAbundance() + "/n"
                    + "Transcript Position: " + i.getCleavagePos() + "/n"
                    + "Category: " + i.getCategory() + "/n"
                    + "Degradome: " + i.getPredictor();
            JsonObjectBuilder jsonNode = Json.createObjectBuilder();
            jsonNode.add("x", i.getCleavagePos());
            jsonNode.add("y", i.getNormalisedWeightedFragmentAbundance());
            jsonNode.add("color", categoryColours[i.getCategory()]);
            jsonNode.add("label", labelText); // +3 ensures that category 0 is triangle

            //loop through the predictors
            int counter = 0;
            for (String predictor : predictors) {
                if (i.getPredictor().equals(predictor)) {
                    jsonNode.add("radius", (predictors.size() - counter) * 2 + 3);
                    jsonNode.add("nPoints", counter + 3); // +3 ensures that category 0 is triangle
                    break;
                    // hsl
                  /*  int hue = (int)(counter * 360.0f/predictors.size());
                     int saturation = 100;
                     int light = 50;
                     jsonNode.add("color", String.format("hsl(%d,%d%%,%d%%)", hue, saturation, light));
                     break;*/
                }
                counter++;
            }
            pointsArray.add(jsonNode);

            currentGene = i.getGene().getID() + "";

            // }
        }
        // save any current file
        if (writer != null) {
            JsonObject jsonObj = json.build();
            JsonWriter jsonWriter = Json.createWriter(writer);
            jsonWriter.writeObject(jsonObj);
            jsonWriter.close();
        }
    }

    public HQLQuery generateFunctionalOutput(String srna) throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        HQLQueryComplex hqlQuery = new HQLQueryComplex();

        String interactionQuery = "";
        for (int i = 0; i < this.in_interactionQuery.getListLength(); i++) {
            if (i > 0) {
                interactionQuery += " OR ";
            }
            interactionQuery += String.format("I.id IN (%s)", this.inInteractionQueriesIDs.get(i).eval());
        }

        hqlQuery.addSelect("I.gene.id", "ID");
        hqlQuery.addSelect("I.gene.gene", "Transcript");
        hqlQuery.addSelect("I.category", "Category");
        hqlQuery.addSelect("I.cleavagePos", "Cleavage_Pos");
        hqlQuery.addSelect("I.duplex", "Duplex");
        hqlQuery.addSelect("I.pVal", "P_Val");
        hqlQuery.addSelect("I.fragmentAbundance", "Fragment_Abundance");
        hqlQuery.addSelect("I.weightedFragmentAbundance", "Weighted_Fragment_Abundance");
        hqlQuery.addSelect("I.normalisedWeightedFragmentAbundance", "Normalised_Weighted_Fragment_Abundance");
        hqlQuery.addSelect("I.alignmentScore", "Score");
        hqlQuery.addSelect("I.predictor", "Predictor");
        hqlQuery.addSelect("I.gene.sequenceLength", "Transcript_Length");
        hqlQuery.addFrom(Interaction_Entity.class, "I");
        hqlQuery.addWhere(interactionQuery);
        hqlQuery.addWhere(String.format("I.sRNA.RNA_Sequence = '%s'", srna));
        /* String q = "SELECT "
         + "I.gene AS gene, "
         + "I.category AS cat, "
         + "I.cleavagePos AS cPos, "
         + "I.duplex AS duplex, "
         + "I.pVal AS pVal, "
         + "I.alignmentScore AS score ";
         q += String.format("FROM Interaction_Entity I WHERE (%s) AND I.sRNA = '%s'", interactionQuery, srna);
         return q;*/
        return hqlQuery;
    }

    public HQLQuery generateOutput() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException, Exception {
        return genInfo();
    }

    public HQLQueryComplex genInfo() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException, Exception {

        // separate sRNAs by filenames
        ArrayList<HQLQuerySimple> sqlQueries = new ArrayList<>();
        for (String filename : filenames) {
            HQLQuerySimple q = new HQLQuerySimple(Sequence_Entity.class);
            q.addWhere("A.filename = '" + filename + "'");
            sqlQueries.add(q);
        }
        // separate interactions by degradome (implicitly by tool)
        ArrayList<HQLQuerySimple> interactionQueries = new ArrayList<>();
        for (int i = 0; i < this.nDeg; i++) {
            HQLQuerySimple q = new HQLQuerySimple(Interaction_Entity.class);
            q.addWhere("A.predictor = 'PAREsnip(" + (i + 1) + ")'");
            //String query = "FROM Interaction_Entity A WHERE A.predictor ='PAREsnip(" + (i + 1) + ")'";
            interactionQueries.add(q);
        }
        // query for interactions from any input
        String interactionQuery = "";
        for (int i = 0; i < this.in_interactionQuery.getListLength(); i++) {
            if (i > 0) {
                interactionQuery += " OR ";
            }
            interactionQuery += String.format("I.id IN (%s)", this.inInteractionQueriesIDs.get(i).eval());
        }
        // create the query
        HQLQueryComplex hqlQuery = new HQLQueryComplex();
        hqlQuery.addSelect("I.sRNA.RNA_Sequence", "sRNA");
        hqlQuery.addSelect("U.type.id.type", "Annotation_Type");
        // hqlQuery.addSelect("U.type.id", "Annotation_Name");
        //hqlQuery.addSelect(U.type.id", "Annotation");

        for (NormalisationType n : normTypes) {
            if (n == NormalisationType.NONE) {
                for (int i = 0; i < sqlQueries.size(); i++) {
                    String name = "E" + i + "_" + n.getAbbrev();
                    hqlQuery.addSelect("max(" + name + ".expression)", n.getFullName().replaceAll(" ", "_") + "_IN_" + i);
                }
            }

        }

        for (int i = 0; i < interactionQueries.size(); i++) {
            String name = "I" + i;
            hqlQuery.addSelect("min(" + name + ".category)", "Min_Category_IN_" + i);
            hqlQuery.addFrom(Interaction_Entity.class, name);
            hqlQuery.addWhere("I.sRNA = " + name + ".sRNA");
            HQLQueryComplex q = new HQLQueryComplex(interactionQueries.get(i));
            q.addSelect(interactionQueries.get(i).getFromAlias() + ".id", "id");
        }

        /*int minCatScore = nDeg;
         int maxCatScore = 16 * nDeg;
         String catScoreStr = "";
         for (int i = 0; i < interactionQueries.size(); i++) {
         if (i > 0) {
         catScoreStr += " + ";
         }
         String name = "I" + i;
         catScoreStr += "power(2, min(" + name + ".category))";
         }
         catScoreStr = String.format("((%s) - %d)/(%d - %d)", catScoreStr, minCatScore, maxCatScore, minCatScore);
         // hqlQuery.addSelect(catScoreStr, "catScore");*/
        hqlQuery.addFrom(Unique_Sequences_Entity.class, "U");
        hqlQuery.addFrom(Aligned_Sequences_Entity.class, "Al");

        for (int i = 0; i < sqlQueries.size(); i++) {
            hqlQuery.addFrom(Sequence_Entity.class, "S" + i);

            for (NormalisationType n : normTypes) {
                if (n == NormalisationType.NONE) {
                    String name = "E" + i + "_" + n.getAbbrev();
                    hqlQuery.addFrom(Expression_Entity.class, name);
                }

            }
        }

        if (showOnlyFunctionalInteractionsWithHairpins) {
            String predictionQuery = "";
            for (int i = 0; i < this.in_predictionQuery.getListLength(); i++) {
                if (i > 0) {
                    predictionQuery += " OR ";
                }
                predictionQuery += String.format("P.id IN (%s)", this.inPredictionQueriesIDs.get(i).eval());
            }
            hqlQuery.addFrom(Prediction_Entity.class, "P");
            hqlQuery.addWhere("I.sRNA.RNA_Sequence = P.mature.rna_sequence");
            hqlQuery.addWhere(predictionQuery);
        }

        hqlQuery.addWhere("U.RNA_Sequence = I.sRNA");
        hqlQuery.addWhere("Al.aligned_sequence = I.sRNA");

        for (int i = 0; i < sqlQueries.size(); i++) {
            HQLQueryComplex q = new HQLQueryComplex(sqlQueries.get(i));
            q.addSelect(sqlQueries.get(i).getFromAlias() + ".id", "id");
            hqlQuery.addWhere(String.format("S%d.id IN (%s)", i, q.eval()));
            hqlQuery.addWhere("I.sRNA.RNA_Sequence = S" + i + ".RNA_Sequence");
            for (NormalisationType n : normTypes) {
                if (n == NormalisationType.NONE) {
                    String name = "E" + i + "_" + n.getAbbrev();
                    hqlQuery.addWhere(String.format("S%d.id = %s.sequence.id", i, name));
                    hqlQuery.addWhere(name + ".normType = " + n.ordinal());
                }
            }

        }
        //    hqlQuery.addFrom(GFF_Attribute_Entity.class, "GAE");
        hqlQuery.addFrom(Interaction_Entity.class, "I");
        hqlQuery.addWhere(interactionQuery);
        //  hqlQuery.addWhere("GAE.id = U.");

        hqlQuery.addGroup("I.sRNA");
        //  hqlQuery.addGroup("U.type.id.type");

        System.out.println("sql: " + hqlQuery.eval());
        return hqlQuery;

    }

    public void computeStats() throws HQLQuery.HQLFormatException, FileNotFoundException, HQLQuery.HQLQueryLockedException {

        System.out.println("all|paresnip|mircat");
        List<HQLQuery> queries = new ArrayList<>();
        // query: all unique sequences
        HQLQuerySimple unique_seq_query = new HQLQuerySimple(Unique_Sequences_Entity.class);
        queries.add(unique_seq_query);

        // query: unique srnas from input paresnip
        HQLQueryComplex unique_seq_from_paresnip_query = new HQLQueryComplex();
        unique_seq_from_paresnip_query.addFrom(Unique_Sequences_Entity.class, "U");
        unique_seq_from_paresnip_query.addFrom(Interaction_Entity.class, "I");
        String paresnip_results_condition = "";
        for (int i = 0; i < this.in_interactionQuery.getListLength(); i++) {
            if (i > 0) {
                paresnip_results_condition += " OR ";
            }
            paresnip_results_condition += String.format("I.id IN (%s)", this.inInteractionQueriesIDs.get(i).eval());
        }
        unique_seq_from_paresnip_query.addWhere(paresnip_results_condition);
        unique_seq_from_paresnip_query.addWhere("I.sRNA = U.RNA_Sequence");
        unique_seq_from_paresnip_query.addSelect("DISTINCT U.RNA_Sequence", "seq");

        queries.add(unique_seq_from_paresnip_query);

        // query: unique srnas from mircat
        HQLQueryComplex unique_seq_from_mircat_query = new HQLQueryComplex();
        unique_seq_from_mircat_query.addFrom(Unique_Sequences_Entity.class, "U");
        unique_seq_from_mircat_query.addFrom(Prediction_Entity.class, "P");
        String mircat_results_condition = "";
        for (int i = 0; i < this.in_predictionQuery.getListLength(); i++) {
            if (i > 0) {
                mircat_results_condition += " OR ";
            }
            mircat_results_condition += String.format("P.id IN (%s)", this.inPredictionQueriesIDs.get(i).eval());
        }
        unique_seq_from_mircat_query.addWhere(mircat_results_condition);
        unique_seq_from_mircat_query.addWhere("P.mature.rna_sequence = U.RNA_Sequence");
        unique_seq_from_mircat_query.addSelect("DISTINCT U.RNA_Sequence", "seq");

        queries.add(unique_seq_from_mircat_query);
        truth_table(queries);
    }

    public void truth_table(List<HQLQuery> queries) throws FileNotFoundException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        // 0 0 0
        // 0 0 1
        // 0 1 0
        // 0 1 1
        // 1 0 0
        // 1 0 1
        // 1 1 0
        // 1 1 1
        UniqueSequencesServiceImpl unique_seqService = (UniqueSequencesServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("UniqueSequencesService");

        out = "";
        int n = (int) Math.pow(2, queries.size());
        for (int i = 0; i < n; i++) {
            String binaryString = Integer.toBinaryString(i);
            while (binaryString.length() < queries.size()) {
                binaryString = "0" + binaryString;
            }
            HQLQueryComplex query = new HQLQueryComplex();
            query.addFrom(Unique_Sequences_Entity.class, "U");
            for (int j = 0; j < binaryString.length(); j++) {
                // is this 0 or 1
                if (binaryString.charAt(j) == '0') {
                    query.addWhere(String.format("U.RNA_Sequence NOT IN (%s)", queries.get((j)).eval()));
                } else {
                    query.addWhere(String.format("U.RNA_Sequence IN (%s)", queries.get((j)).eval()));
                }
            }
            query.addSelect("DISTINCT U.RNA_Sequence", "seq");
            List<Unique_Sequences_Entity> list = unique_seqService.executeSQL(query.eval());

            System.out.println(binaryString + ": " + list.size());
            if (i > 0) {
                out += ",";
            }
            out += list.size();
        }
    }

    public static void main(String[] args) {
        BinaryExecutor binExe = AppUtils.INSTANCE.getBinaryExecutor();

        binExe.execRNAPlot2("mir160a", "GUAUGCCUGGCUCCCUGUAUGCCAUAUGCUGAGCCCAUCGAGUAUCGAUGACCUCCGUGGAUGGCGUAUGAGGAGCCAUGCAUAU",
                "((((((.(((((((..(((((((((.(((.(((..(((((.....)))))..))).))).)))))))))..))))))).))))))", "xrna");

        try {
            convert_xrna_to_d3_format(new File("mir160a_ss.ss"), new File("mir160a.xrna"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MiRPAREModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void process() throws HQLQuery.HQLFormatException, Exception {

        this.inInteractionQueriesIDs = new LinkedList<>();
        for (int i = 0; i < this.in_interactionQuery.getListLength(); i++) {
            HQLQuerySimple qSimple = this.in_interactionQuery.getContainer(i).getData();
            HQLQueryComplex q = new HQLQueryComplex(qSimple);
            q.addSelect(qSimple.getFromAlias() + ".id", "id");
            inInteractionQueriesIDs.add(q);
        }

        this.inPredictionQueriesIDs = new LinkedList<>();
        for (int i = 0; i < this.in_predictionQuery.getListLength(); i++) {
            HQLQuerySimple qSimple = this.in_predictionQuery.getContainer(i).getData();
            HQLQueryComplex q = new HQLQueryComplex(qSimple);

            q.addSelect(qSimple.getFromAlias() + ".id", "id");
            inPredictionQueriesIDs.add(q);

        }

        // this.log.addLn("INFORMATION: biofunc module started.");
        // create a thread manager for plotting structures
        // retrieve the output query
        PredictionServiceImpl prediction_service = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");

        // get number of sample files
        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");
        filenames = sequence_service.getUniqueListOfFilenames();

        // get number of degradomes
        this.nDeg = 0;
        Map<String, List<Pair<Path, Integer>>> degradomes = HTMLWizardViewController.getDegradomes();
        for (String sample : degradomes.keySet()) {
            this.nDeg += degradomes.get(sample).size();
        }
        System.out.println("there are " + this.nDeg + " degradomes");
//        this.nDeg = this.in_interactionQuery.getListLength();
        // get used normalisation types
        ExpressionServiceImpl expression_service = (ExpressionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("ExpressionService");
        normTypes = expression_service.getNormalisationTypes();
        for (NormalisationType n : normTypes) {
            System.out.println("norm types: " + n.getFullName());
        }

        String interactionQuery = "";
        for (int i = 0; i < this.in_interactionQuery.getListLength(); i++) {
            if (i > 0) {
                interactionQuery += " OR ";
            }
            interactionQuery += String.format("A.id IN (%s)", inInteractionQueriesIDs.get(i).eval());
        }
        String predictionQuery = "";
        for (int i = 0; i < this.in_predictionQuery.getListLength(); i++) {
            if (i > 0) {
                predictionQuery += " OR ";
            }
            predictionQuery += String.format("B.id IN (%s)", inPredictionQueriesIDs.get(i).eval());
        }

        String q = String.format("SELECT B.id FROM Interaction_Entity A, Prediction_Entity B WHERE A.sRNA.RNA_Sequence = B.mature.rna_sequence AND (%s) AND (%s)", interactionQuery, predictionQuery);
        String q2 = String.format("FROM Prediction_Entity P WHERE P.id IN (%s)", q);

        List<Prediction_Entity> predictions = prediction_service.executeSQL(q2);

        BinaryExecutor binExe = AppUtils.INSTANCE.getBinaryExecutor();
        for (Prediction_Entity p : predictions) {

            binExe.execRNAPlot2(p.getID() + "", p.getSequence(), p.getStructure(), "xrna");
            File xrnaFile = new File(p.getID() + "_ss.ss");
            File xrnaFileDSV = new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + "hp_" + p.getID() + ".xrna");

            try {
                convert_xrna_to_d3_format(xrnaFile, xrnaFileDSV);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MiRPAREModule.class.getName()).log(Level.SEVERE, null, ex);
            }

            xrnaFile.delete();

        }
        
        try {
            saveJSON(new File(Tools.miRPARE_DATA_Path + DIR_SEPARATOR + "auto_save_mirpare.json"));
        } catch (Exception ex) {
            Logger.getLogger(MiRPAREModule.class.getName()).log(Level.SEVERE, "", ex);

        }
        /*  try {
         //  this.log.addLn("INFORMATION: biofunc module completed.");
         computeStats();
         } catch (FileNotFoundException ex) {
         Logger.getLogger(MiRPAREModule.class.getName()).log(Level.SEVERE, null, ex);
         }*/

        createTPlots(true, this.getID());
        //computeStats();
        this.complete = true;
        c.updateUI();

    }

    public String getSummary() {
        return "drawSummaryTable('" + out + "', ',', 1);";
    }

    private static void convert_xrna_to_d3_format(File xrnaFile, File d3FormatFile) throws FileNotFoundException {

        PrintWriter p = new PrintWriter(d3FormatFile);
        p.println("id n x y s e");
        Scanner in = new Scanner(xrnaFile);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (!line.startsWith("#") && !line.isEmpty()) {
                String after = "";
                while (!line.equals(after)) {
                    after = line;
                    line = line.replaceAll("  ", " ");
                }
                p.println(line);
            }
        }
        in.close();
        p.close();
    }

    public void saveJSON(File file) throws FileNotFoundException, Exception {
        // save everything in json format
        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");
        JsonArrayBuilder recordArray = Json.createArrayBuilder();
        List<Map<String, Object>> parentRecords = sequence_service.executeGenericSQL(genInfo().eval());
        for (Map<String, Object> parentRecord : parentRecords) {
            JsonObjectBuilder record = JsonUtils.buildJsonObject(parentRecord);

            String srna = parentRecord.get("sRNA").toString();
            // functional records
            List<Map<String, Object>> functionalRecords = sequence_service.executeGenericSQL(generateFunctionalOutput(srna).eval());
            JsonArray functionalRecordArray = JsonUtils.buildJsonArray(functionalRecords).build();
            record.add("interactions", functionalRecordArray);
            // biogenesis records
            List<Map<String, Object>> biogenesisRecords = sequence_service.executeGenericSQL(generatePredictionOutput(srna).eval());
            JsonArray biogenesisRecordArray = JsonUtils.buildJsonArray(biogenesisRecords).build();
            record.add("predictions", biogenesisRecordArray);
            recordArray.add(record);
        }
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("Records", recordArray);
        outputJsonFile(file, json.build());
    }

    public void outputJsonFile(File f, JsonObject json) throws Exception {
        PrintWriter writer = new PrintWriter(f);
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeObject(json);
        jsonWriter.close();
    }
}
