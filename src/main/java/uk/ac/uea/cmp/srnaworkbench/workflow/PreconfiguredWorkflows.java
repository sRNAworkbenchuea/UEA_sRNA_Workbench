/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.workflow;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.NormalisationType;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModule;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.io.IOUtils.DIR_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowRunner;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.io.GenomeManager;
import uk.ac.uea.cmp.srnaworkbench.tools.degradometoolWF.DegradomeToolWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.degradometoolWF.DegradomeToolWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.mirpare.MiRPAREModule;
import uk.ac.uea.cmp.srnaworkbench.tools.mirpare.MiRPAREWorkflowRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.srnaConservation.ConservationModule;
import uk.ac.uea.cmp.srnaworkbench.tools.srnaConservation.ConservationRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.interactionConservation.InteractionConservationModule;
import uk.ac.uea.cmp.srnaworkbench.tools.interactionConservation.InteractionConservationRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.DifferentialExpressionWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.DifferentialExpressionWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.OffsetReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.differentialExpression.WF.OffsetReviewWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.filereview.WF.FileReviewWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanager.wizard.FX.HTMLWizardViewController;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF.FileManagerWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filemanagerWF.FileManagerWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.filter2WF.Filter2WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.filter2WF.Filter2WorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.locifilter.LociFilterModule;
import uk.ac.uea.cmp.srnaworkbench.tools.locifilter.LociFilterWorkflowRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.MiRCatParams;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat.WF.MiRCatModule;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF.MiRCat2Module;
import uk.ac.uea.cmp.srnaworkbench.tools.mircat2.WF.miRCat2ModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.normalise.WF.NormalisationWorkflowServiceModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.PAREsnipModule;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.PAREsnipRunner;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip.ParesnipParams;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF.ReportWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.WF.ReportWorkflowModuleRunner;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import uk.ac.uea.cmp.srnaworkbench.utils.JsonUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.MessageBox;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;

/**
 *
 * @author w0445959
 */
public class PreconfiguredWorkflows extends Application {

    private PreconfiguredWorkflows() {

    }

    public static String createDefaultMiRPAREWorkflow(Rectangle2D frameSize) throws Exception {

        AppUtils.INSTANCE.setCommandLine(true);
        WorkflowManager.getInstance().reset();
        WorkflowManager.getInstance().setName("mirpare");
        WorkflowManager.getInstance().transcriptomeOptional = false;
        WorkflowManager.getInstance().degradomeOptional = false;
        WorkflowManager.getInstance().maxSamples = 1;
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);
        DatabaseWorkflowModule.getInstance().setFrameSize(frameSize);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) frameSize.getMaxY() / 4.0f);

        File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + IOUtils.DIR_SEPARATOR + "json" + IOUtils.DIR_SEPARATOR + "miRPARE.json");
        WorkflowManager.getInstance().outputJsonFile(jsonFile);
        // return a file URL which appears to be more compatible with D3 javascript on windows
        return jsonFile.toURI().toURL().toString();

    }

    public static String configurePAREsnipWorkflow(ParesnipParams paresnipParams, Rectangle2D frameSize) throws Exception {

        System.out.println("framesize: " + frameSize.getWidth() + " " + frameSize.getHeight());
        // add genome to workflow
        Path genomePath = HTMLWizardViewController.getGenome();
        WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);
        WorkflowManager.getInstance().addInputData("genome", WorkflowManager.CompatibilityKey.GENOME, new GenomeManager(genomePath));

        // add transcriptome to workflow
        Path transcriptPath = HTMLWizardViewController.getTranscriptome();
        WorkflowManager.getInstance().addInputDataContainerList("transcripts", CompatibilityKey.TRANSCRIPT_FILE, 1, 1);
        WorkflowManager.getInstance().addInputData("transcripts", CompatibilityKey.TRANSCRIPT_FILE, transcriptPath);
        // add degradomes to workflow
        Map<String, List<Pair<Path, Integer>>> degradomes = HTMLWizardViewController.getDegradomes();
        WorkflowManager.getInstance().addInputDataContainerList("degradomes", CompatibilityKey.DEGRADOME_FILE, 1, -1);
        int nDegradomes = 0;
        for (String key : degradomes.keySet()) {
            for (Pair<Path, Integer> path : degradomes.get(key)) {
                WorkflowManager.getInstance().addInputData("degradomes", CompatibilityKey.DEGRADOME_FILE, path.getKey().toFile());
                nDegradomes++;
            }
        }
        System.out.println(nDegradomes + " degradomes");
        // create srna input queries
        Map<String, List<Path>> srnaSamples = HTMLWizardViewController.getSamples();
        WorkflowManager.getInstance().addInputDataContainerList("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, -1);
        int nSamples = 0;
        for (String key : srnaSamples.keySet()) {
            for (Path path : srnaSamples.get(key)) {
                HQLQuerySimple q = new HQLQuerySimple(Sequence_Entity.class);
                q.addWhere("A.filename = '" + path.getFileName() + "'");
                //StringBuilder hql = new StringBuilder("FROM Sequence_Entity A WHERE A.filename = '" + path.getFileName() + "'");
                WorkflowManager.getInstance().addInputData("srnaQuery", CompatibilityKey.sRNA_QUERY, q);
                nSamples++;
            }
        }
        System.out.println(nDegradomes + " srna samples");
        // positioning
        int blockSize = 100;
        int widthSpacing = blockSize + blockSize / 2;
        float height = blockSize * (nDegradomes + nSamples);
        float minY = 55.0f;
        float minX = 60.0f;
        float paresnipMinY = minY;
        float paresnipMaxY = paresnipMinY + ((nDegradomes - 1) * blockSize);
        float mircatMinY = paresnipMaxY + blockSize * 2;
        float centreY = (minY + height) / 2;

        String id = "PAREsnip";
        PAREsnipModule paresnipModule = new PAREsnipModule(id, "PAREsnip", frameSize);
        PAREsnipRunner paresnipRunner = new PAREsnipRunner(paresnipModule);
        WorkflowManager.getInstance().addModule(paresnipRunner);
        // inputs
        WorkflowManager.getInstance().connectDB2Module("genome", 0, id, "genome");
        WorkflowManager.getInstance().connectDB2Module("degradomes", 0, id, "degradome");
        WorkflowManager.getInstance().connectDB2Module("transcripts", 0, id, "transcripts");

        WorkflowManager.getInstance().connectDB2Module("srnaQuery", 0, id, "srnaQuery");
        WorkflowManager.getInstance().connectModule2Module("Database", id);

        paresnipModule.setPos(minX + widthSpacing * 2, paresnipMinY + blockSize);
        // paresnipModule.setParameters(paresnipParams);

        File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + IOUtils.DIR_SEPARATOR + "json" + IOUtils.DIR_SEPARATOR + "miRPARE.json");
        WorkflowManager.getInstance().outputJsonFile(jsonFile);
        // return a file URL which appears to be more compatible with D3 javascript on windows
        return jsonFile.toURI().toURL().toString();

    }

    public static String configureMiRCatWorkflow(MiRCatParams mircatParams, Rectangle2D frameSize) throws Exception {

        // add genome to workflow
        Path genomePath = HTMLWizardViewController.getGenome();
        WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);
        WorkflowManager.getInstance().addInputData("genome", WorkflowManager.CompatibilityKey.GENOME, new GenomeManager(genomePath));
        // create srna input queries
        Map<String, List<Path>> srnaSamples = HTMLWizardViewController.getSamples();
        WorkflowManager.getInstance().addInputDataContainerList("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, -1);
        int nSamples = 0;
        for (String key : srnaSamples.keySet()) {
            for (Path path : srnaSamples.get(key)) {
                HQLQuerySimple q = new HQLQuerySimple(Sequence_Entity.class);
                q.addWhere("A.filename = '" + path.getFileName() + "'");
                WorkflowManager.getInstance().addInputData("srnaQuery", CompatibilityKey.sRNA_QUERY, q);
                nSamples++;
            }
        }

        nSamples = 1;
        // add mircat modules
        for (int i = 0; i < nSamples; i++) {
            String id = String.format("miRCat%d", i + 1);
            MiRCatModule mircatModule = new MiRCatModule(id, String.format("miRCat (%d)", i + 1), frameSize);
            MiRCatModuleRunner mircatRunner = new MiRCatModuleRunner(mircatModule);
            WorkflowManager.getInstance().addModule(mircatRunner);
            // inputs
            WorkflowManager.getInstance().connectDB2Module("genome", 0, id, "genome");
            WorkflowManager.getInstance().connectDB2Module("srnaQuery", i, id, "srnaQuery");
            mircatModule.setParameters(mircatParams);
            WorkflowManager.getInstance().connectModule2Module("Database", id);
        }

        File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + IOUtils.DIR_SEPARATOR + "json" + IOUtils.DIR_SEPARATOR + "miRPARE.json");
        WorkflowManager.getInstance().outputJsonFile(jsonFile);
        // return a file URL which appears to be more compatible with D3 javascript on windows
        return jsonFile.toURI().toURL().toString();

    }
   
    public static String runCommandLineWorkflowMiRCat(String workflow, File jsonPath, Rectangle2D frameSize) throws Exception {
        String json = createDefaultMiRPAREWorkflow(frameSize);
        JsonObject jsonObject = JsonUtils.parseJsonFile(jsonPath);
        WorkflowManager.getInstance().setUpDatabase(jsonObject);
        MiRCatParams miRCatParams = WorkflowManager.getInstance().getMiRCatParameters(jsonObject);
        configureMiRCatWorkflow(miRCatParams, frameSize);
        return json;
    }

    public static String runCommandLineWorkflowPAREsnip(String workflow, File jsonPath, Rectangle2D frameSize) throws Exception {
        String json = createDefaultMiRPAREWorkflow(frameSize);
        JsonObject jsonObject = JsonUtils.parseJsonFile(jsonPath);
        WorkflowManager.getInstance().setUpDatabase(jsonObject);
        List<ParesnipParams> paresnipParams = WorkflowManager.getInstance().getPAREsnipParameters(jsonObject);
        configurePAREsnipWorkflow(paresnipParams.get(0), frameSize);
        return json;
    }

    public static String runCommandLineWorkflowMiRPARE(String workflow, File jsonPath, Rectangle2D frameSize) {
        String str = "";
        JsonObject jsonObject = null;

        try {
            jsonObject = JsonUtils.parseJsonFile(jsonPath);
        } catch (Exception ex) {
            MessageBox.messageJFX("Workbench Error", "Unable to load workflow", "There was an error parsing the JSON file: " + ex);
        }
        try {
            createDefaultMiRPAREWorkflow(frameSize);
            WorkflowManager.getInstance().setUpDatabase(jsonObject);
            MiRCatParams miRCatParams = WorkflowManager.getInstance().getMiRCatParameters(jsonObject);
            List<ParesnipParams> paresnipParams = WorkflowManager.getInstance().getPAREsnipParameters(jsonObject);
            str = configureMiRPAREWorkflow(miRCatParams, paresnipParams.get(0), frameSize);
        } catch (Exception ex) {
            MessageBox.messageJFX("Workbench Error", "Unable to load workflow", "There was an error loading the workflow: " + ex);
        }
        return str;
    }

    public static void parseFA(File input, File output) throws FileNotFoundException {
        Map<String, Integer> rnas = new HashMap<>();
        Scanner in = new Scanner(input);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (!line.startsWith(">") && !line.isEmpty()) {
                String rna = line.trim();
                int abundance = 0;
                if (rnas.containsKey(rna)) {
                    abundance = rnas.get(rna);
                }
                abundance++;
                rnas.put(rna, abundance);
            }
        }
        in.close();

        PrintWriter writer = new PrintWriter(output);
        for (String rna : rnas.keySet()) {
            writer.println(">" + rna + "(" + rnas.get(rna) + ")");
            writer.println(rna);
        }
        writer.close();
    }

    public static String configureMiRPAREWorkflow(MiRCatParams mircatParams, ParesnipParams paresnipParams, Rectangle2D frameSize) throws IOException, FileNotFoundException, DuplicateIDException, Exception {
        // add genome to workflow
        if (WorkflowManager.getInstance().getInputData("genome") == null) {
            Path genomePath = HTMLWizardViewController.getGenome();
            WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);
            WorkflowManager.getInstance().addInputData("genome", WorkflowManager.CompatibilityKey.GENOME, new GenomeManager(genomePath));
        }

        // add transcriptome to workflow
        Path transcriptPath = HTMLWizardViewController.getTranscriptome();
        WorkflowManager.getInstance().addInputDataContainerList("transcripts", CompatibilityKey.TRANSCRIPT_FILE, 1, 1);
        WorkflowManager.getInstance().addInputData("transcripts", CompatibilityKey.TRANSCRIPT_FILE, transcriptPath);

        ArrayList<Integer> degDataTypes = new ArrayList<>();
        // add degradomes to workflow
        Map<String, List<Pair<Path, Integer>>> degradomes = HTMLWizardViewController.getDegradomes();
        WorkflowManager.getInstance().addInputDataContainerList("degradomes", CompatibilityKey.DEGRADOME_FILE, 1, -1);
        int nDegradomes = 0;
        for (String key : degradomes.keySet()) {
            for (Pair<Path, Integer> pair : degradomes.get(key)) {
                WorkflowManager.getInstance().addInputData("degradomes", CompatibilityKey.DEGRADOME_FILE, pair.getKey().toFile());
                nDegradomes++;
                degDataTypes.add(pair.getValue());
            }
        }
        //  System.out.println(nDegradomes + " degradomes");
        // create srna input queries
        Map<String, List<Path>> srnaSamples = HTMLWizardViewController.getSamples();
        WorkflowManager.getInstance().addInputDataContainerList("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, -1);
        int nSamples = 0;
        for (String key : srnaSamples.keySet()) {
            for (Path path : srnaSamples.get(key)) {
                HQLQuerySimple q = new HQLQuerySimple(Sequence_Entity.class);
                q.addWhere("A.filename = '" + path.getFileName() + "'");
                WorkflowManager.getInstance().addInputData("srnaQuery", CompatibilityKey.sRNA_QUERY, q);
                nSamples++;
            }
        }
        //  System.out.println(nDegradomes + " srna samples");
        // positioning
        int blockSize = 100;
        int widthSpacing = blockSize + blockSize / 2;
        float height = blockSize * (nDegradomes + nSamples);
        float minY = 55.0f;
        float minX = 60.0f;
        float paresnipMinY = minY;
        float paresnipMaxY = paresnipMinY + ((nDegradomes - 1) * blockSize);
        float mircatMinY = paresnipMaxY + blockSize * 2;
        float centreY = (minY + height) / 2;

        // database module should already be in place        
        // add normalisation module
        if (nSamples > 1) {

            NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                    "Normaliser", frameSize);
            newNormaliser.setPos(600, (float) frameSize.getMaxY() / 4);
//
            NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

            WorkflowManager.getInstance().addModule(normRunner);

            // inputs
            WorkflowManager.getInstance().connectModule2Module("Database", "Normaliser");

        }

        if (nSamples > 1) {
            // add srna conservation module
            ConservationModule conservationModule = new ConservationModule("sRNAConservationFilter", "sRNA Conservation Filter", frameSize);
            ConservationRunner conservationRunner = new ConservationRunner(conservationModule);
            WorkflowManager.getInstance().addModule(conservationRunner);
            // inputs
            WorkflowManager.getInstance().connectModule2Module("Normaliser", "sRNAConservationFilter");
//        WorkflowManager.getInstance().connectDB2Module("srnaQuery", "sRNA Conservation Filter", "input");
            WorkflowManager.getInstance().getModule("sRNAConservationFilter").setPos(minX + widthSpacing, (paresnipMinY + paresnipMaxY) / 2);
        }
        // add paresnip modules
        for (int i = 0; i < nDegradomes; i++) {
            String id = String.format("PAREsnip%d", i + 1);
            PAREsnipModule paresnipModule = new PAREsnipModule(id, String.format("PAREsnip (%d)", i + 1), frameSize);
            if (degDataTypes.get(i) == 0) {
                paresnipModule.runPAREsnip = true;
            } else {
                paresnipModule.runPAREsnip = false;
            }
            PAREsnipRunner paresnipRunner = new PAREsnipRunner(paresnipModule);
            WorkflowManager.getInstance().addModule(paresnipRunner);
            // inputs
            WorkflowManager.getInstance().connectDB2Module("genome", 0, id, "genome");
            WorkflowManager.getInstance().connectDB2Module("degradomes", i, id, "degradome");
            WorkflowManager.getInstance().connectDB2Module("transcripts", 0, id, "transcripts");
            if (nSamples > 1) {
                WorkflowManager.getInstance().connectModule2Module("sRNAConservationFilter", "output", id, "srnaQuery");
            } else {
                WorkflowManager.getInstance().connectDB2Module("srnaQuery", id, "srnaQuery");
                WorkflowManager.getInstance().connectModule2Module("Database", id);
            }

            if (i > 0) {
                String previousPAREsnipID = String.format("PAREsnip%d", i);
                WorkflowManager.getInstance().connectModule2Module(previousPAREsnipID, id);
            }
            paresnipModule.setPos(minX + widthSpacing * 2, paresnipMinY + blockSize * i);
            if (paresnipParams != null) {
                paresnipModule.setParameters(paresnipParams);
            }
        }
        String lastPAREsnipID = String.format("PAREsnip%d", nDegradomes);
        // WorkflowManager.getInstance().connectModule2Module(lastPAREsnipID, "miRCat1");
        // add interaction module
        if (nDegradomes > 1) {
            InteractionConservationModule interactionConservationModule = new InteractionConservationModule("InteractionConservationFilter", "Interaction Conservation Filter", frameSize);
            InteractionConservationRunner interactionConservationRunner = new InteractionConservationRunner(interactionConservationModule);
            WorkflowManager.getInstance().addModule(interactionConservationRunner);
            // inputs
            for (int i = 0; i < nDegradomes; i++) {
                String id = String.format("PAREsnip%d", i + 1);
                WorkflowManager.getInstance().connectModule2Module(id, "interactionQuery", "InteractionConservationFilter", "input");
            }
            WorkflowManager.getInstance().getModule("InteractionConservationFilter").setPos(minX + widthSpacing * 3, paresnipMinY);
        }

        // add loci filter
        for (int i = 0; i < nSamples; i++) {
            String id = String.format("LociFilter%d", i + 1);
            LociFilterModule lociModule = new LociFilterModule(id, String.format("Loci Filter (%d)", i + 1), frameSize);
            LociFilterWorkflowRunner lociRunner = new LociFilterWorkflowRunner(lociModule);
            WorkflowManager.getInstance().addModule(lociRunner);
            if (nDegradomes > 1) {
                WorkflowManager.getInstance().connectModule2Module("InteractionConservationFilter", "output", id, "targetQuery");
            } else {
                WorkflowManager.getInstance().connectModule2Module("PAREsnip1", "interactionQuery", id, "targetQuery");
            }
            WorkflowManager.getInstance().connectDB2Module("srnaQuery", i, id, "srnaomeQuery");
        }

        // add mircat modules
        for (int i = 0; i < nSamples; i++) {
            String id = String.format("miRCat%d", i + 1);
            String lociFilterID = String.format("LociFilter%d", i + 1);
            MiRCatModule mircatModule = new MiRCatModule(id, String.format("miRCat (%d)", i + 1), frameSize);
            MiRCatModuleRunner mircatRunner = new MiRCatModuleRunner(mircatModule);
            WorkflowManager.getInstance().addModule(mircatRunner);
            // inputs
            WorkflowManager.getInstance().connectDB2Module("genome", 0, id, "genome");
            WorkflowManager.getInstance().connectModule2Module(lociFilterID, "output", id, "srnaQuery");
            //WorkflowManager.getInstance().connectDB2Module("srnaQuery", 0, id, "srnaQuery");
            //  if (nSamples > 1) {
            //      WorkflowManager.getInstance().connectModule2Module("Normaliser", id);
            // } else {
            //     WorkflowManager.getInstance().connectModule2Module("Database", id);
            // }
            if (i != 0) {

                //} else {
                String previousMiRCatID = String.format("miRCat%d", i);
                WorkflowManager.getInstance().connectModule2Module(previousMiRCatID, id);
            }
            mircatModule.setPos(minX + widthSpacing * 4, mircatMinY + blockSize * i);
            if (mircatParams != null) {
                mircatModule.setParameters(mircatParams);
            }

        }

        // add mirplant module
        //  MiRPlantModule mirPlantModule = new MiRPlantModule("mirplant");
        // MiRPlantRunner mirPlantRunner = new MiRPlantRunner(mirPlantModule);
        //WorkflowManager.getInstance().addModule(mirPlantRunner);
        // add tasi module
        /*TasiModule tasiModule = new TasiModule("tasi", "TASI", frameSize);
         TasiWorkflowRunner tasiRunner = new TasiWorkflowRunner(tasiModule);
         WorkflowManager.getInstance().addModule(tasiRunner);
         WorkflowManager.getInstance().connectDB2Module("genome", 0, "tasi", "genome");
         WorkflowManager.getInstance().connectModule2Module("LociFilter1", "output", "tasi", "srnaQuery");
         //   WorkflowManager.getInstance().connectModule2Module("Database", "tasi");
         */
        // add mirpare module
        MiRPAREModule mirpareModule = new MiRPAREModule("ResultVisualiser", "Result Visualiser", frameSize);
        MiRPAREWorkflowRunner biofuncRunner = new MiRPAREWorkflowRunner(mirpareModule);
        WorkflowManager.getInstance().addModule(biofuncRunner);
        // inputs
        for (int i = 0; i < nSamples; i++) {
            String id = String.format("miRCat%d", i + 1);
            WorkflowManager.getInstance().connectModule2Module(id, "predictionQuery", "ResultVisualiser", "predictionQuery");
        }
        if (nDegradomes > 1) {
            WorkflowManager.getInstance().connectModule2Module("InteractionConservationFilter", "output", "ResultVisualiser", "interactionQuery");
        } else {
            WorkflowManager.getInstance().connectModule2Module("PAREsnip1", "interactionQuery", "ResultVisualiser", "interactionQuery");
        }
        mirpareModule.setPos(minX + widthSpacing * 5, (centreY));

        
        
        
        
        
        
        
        
        // WorkflowManager.getInstance().connectModule2Module("Database", "mirplant");
        // WorkflowManager.getInstance().connectModule2Module("mirplant", "predictionQuery", "Result Visualiser", "predictionQuery");
        File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + IOUtils.DIR_SEPARATOR + "json" + IOUtils.DIR_SEPARATOR + "miRPARE_configured.json");
        WorkflowManager.getInstance().outputJsonFile(jsonFile);
        // return a file URL which appears to be more compatible with D3 javascript on windows
        return jsonFile.toURI().toURL().toString();

    }
    
    

    public static String createMiRPAREWorkflowTest2(Rectangle2D frameSize) throws IOException, Exception {

        WorkflowManager.getInstance().reset();
        WorkflowManager.getInstance().setName("mirpare");
        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //First step is to pull in the selected data to start the database
        //test phase, pull in the test data from the file hierarchy
        //List<List<Path>> samples = FileHierarchyViewController.retrieveTestDataPaths();
        AppUtils.INSTANCE.setCommandLine(true);

        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setDebugMode(true);
        DatabaseWorkflowModule.getInstance().setFrameSize(frameSize);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) frameSize.getMaxY() / 4.0f);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", frameSize);
        newNormaliser.setPos(600, (float) frameSize.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);
        WorkflowManager.getInstance().addModule(normRunner);
        WorkflowManager.getInstance().connectModule2Module("Database", "NORMALISER");

        return "null";//configureMiRPAREWorkflow();
        //File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + "/json/miRPARE.json");
        //WorkflowManager.getInstance().outputJsonFile(jsonFile);

        // return a file URL which appears to be more compatible with D3 javascript on windows
        //return jsonFile.toURI().toURL().toString();
    }

    public static String createQCWorkflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        //now create the first report on the initial state of the data
        List<NormalisationType> norms = new ArrayList<>();
        norms.add(NormalisationType.NONE);

        //create the first report module and add it to the workflow
        ReportWorkflowModule firstReportModule = new ReportWorkflowModule("FIRSTREPORT", norms, visualBounds);
        firstReportModule.setPos(200, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner firstReportRunner = new ReportWorkflowModuleRunner(firstReportModule);

        WorkflowManager.getInstance().addModule(firstReportRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", "FIRSTREPORT");

        //create the file review module and add it to the workflow
        FileReviewWorkflowModule fileReviewMod = new FileReviewWorkflowModule("FILEREVIEW", visualBounds);
        fileReviewMod.setPos(400, (float) visualBounds.getMaxY() / 4);
        FileReviewWorkflowModuleRunner fileRevRunner = new FileReviewWorkflowModuleRunner(fileReviewMod);

        WorkflowManager.getInstance().addModule(fileRevRunner);

        WorkflowManager.getInstance().connectModule2Module("FIRSTREPORT", "FILEREVIEW");

        // We are normalising everything aligned to the genome reference sequence only for now. This can be modified to allow specified annotations to be normalised, however
        //ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", visualBounds);
        newNormaliser.setPos(600, (float) visualBounds.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

        WorkflowManager.getInstance().addModule(normRunner);

        WorkflowManager.getInstance().connectModule2Module("FILEREVIEW", "NORMALISER");

        //create the second report module and add it to the workflow
        ReportWorkflowModule secondReportModule = new ReportWorkflowModule("SECONDREPORT", visualBounds);
        secondReportModule.setPos(800, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner secondReportRunner = new ReportWorkflowModuleRunner(secondReportModule);

        WorkflowManager.getInstance().addModule(secondReportRunner);

        WorkflowManager.getInstance().connectModule2Module("NORMALISER", "SECONDREPORT");

        try {
//            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "QualityCheck.json");
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "QualityCheck.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return "";

    }

    public static String createQCWorkflowNoReview(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        //now create the first report on the initial state of the data
        List<NormalisationType> norms = new ArrayList<>();
        norms.add(NormalisationType.NONE);

        //create the first report module and add it to the workflow
        ReportWorkflowModule firstReportModule = new ReportWorkflowModule("FIRST_REPORT", norms, visualBounds);
        firstReportModule.setPos(200, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner firstReportRunner = new ReportWorkflowModuleRunner(firstReportModule);

        WorkflowManager.getInstance().addModule(firstReportRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", "FIRST_REPORT");

        // We are normalising everything aligned to the genome reference sequence only for now. This can be modified to allow specified annotations to be normalised, however
        //ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", visualBounds);
        newNormaliser.setPos(600, (float) visualBounds.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

        WorkflowManager.getInstance().addModule(normRunner);

        WorkflowManager.getInstance().connectModule2Module("FIRST_REPORT", "NORMALISER");

        //create the second report module and add it to the workflow
        ReportWorkflowModule secondReportModule = new ReportWorkflowModule("SECOND_REPORT", visualBounds);
        secondReportModule.setPos(800, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner secondReportRunner = new ReportWorkflowModuleRunner(secondReportModule);

        WorkflowManager.getInstance().addModule(secondReportRunner);

        WorkflowManager.getInstance().connectModule2Module("NORMALISER", "SECOND_REPORT");

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "QualityCheck_NoRev.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return "";

    }

    public static String createQC_DE_Workflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();
        
        clearOldJSONS();

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        //now create the first report on the initial state of the data
        List<NormalisationType> norms = new ArrayList<>();
        norms.add(NormalisationType.NONE);

        //create the first report module and add it to the workflow
        ReportWorkflowModule firstReportModule = new ReportWorkflowModule("FIRSTREPORT", norms, visualBounds);
        firstReportModule.setPos(260, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner firstReportRunner = new ReportWorkflowModuleRunner(firstReportModule);

        WorkflowManager.getInstance().addModule(firstReportRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", firstReportModule.getID());

        //create the file review module and add it to the workflow
        FileReviewWorkflowModule fileReviewMod = new FileReviewWorkflowModule("FILEREVIEW", visualBounds);
        fileReviewMod.setPos(460, (float) visualBounds.getMaxY() / 4);
        FileReviewWorkflowModuleRunner fileRevRunner = new FileReviewWorkflowModuleRunner(fileReviewMod);

        WorkflowManager.getInstance().addModule(fileRevRunner);

        WorkflowManager.getInstance().connectModule2Module(firstReportModule.getID(), fileReviewMod.getID());

        // We are normalising everything aligned to the genome reference sequence only for now. This can be modified to allow specified annotations to be normalised, however
        //ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", visualBounds);
        newNormaliser.setPos(660, (float) visualBounds.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

        WorkflowManager.getInstance().addModule(normRunner);

        WorkflowManager.getInstance().connectModule2Module(fileReviewMod.getID(), newNormaliser.getID());

        //create the second report module and add it to the workflow
        ReportWorkflowModule secondReportModule = new ReportWorkflowModule("SECONDREPORT", visualBounds);
        secondReportModule.disableTool(ReportWorkflowModule.QCTool.JACCARD);
       
        // second report should not be used on unmapped data
        secondReportModule.setMappedOnly();

        secondReportModule.setPos(860, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner secondReportRunner = new ReportWorkflowModuleRunner(secondReportModule);

        WorkflowManager.getInstance().addModule(secondReportRunner);

        WorkflowManager.getInstance().connectModule2Module(newNormaliser.getID(), secondReportModule.getID());

        //create the offset review module and add it to the workflow
        OffsetReviewWorkflowModule off_rev = new OffsetReviewWorkflowModule("OFFSETREVIEW", visualBounds);
        off_rev.setPos(1060, (float) visualBounds.getMaxY() / 4);

        OffsetReviewWorkflowModuleRunner off_rev_runner = new OffsetReviewWorkflowModuleRunner(off_rev);

        WorkflowManager.getInstance().addModule(off_rev_runner);

        WorkflowManager.getInstance().connectModule2Module(secondReportModule.getID(), off_rev.getID());

        //create the DE module and add it
        DifferentialExpressionWorkflowModule d_e = new DifferentialExpressionWorkflowModule("DIFFERENTIALEXPRESSION", visualBounds);
        d_e.setPos(1260, (float) visualBounds.getMaxY() / 4);

        DifferentialExpressionWorkflowModuleRunner d_e_runner = new DifferentialExpressionWorkflowModuleRunner(d_e);

        WorkflowManager.getInstance().addModule(d_e_runner);

        WorkflowManager.getInstance().connectModule2Module(off_rev.getID(), d_e.getID());

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "QualityCheck_Norm_DE.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        

        return "";

    }

    public static String createNorm_DE_Workflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();
        
        clearOldJSONS();

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        // We are normalising everything aligned to the genome reference sequence only for now. This can be modified to allow specified annotations to be normalised, however
        //ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", visualBounds);
        newNormaliser.setPos(660, (float) visualBounds.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

        WorkflowManager.getInstance().addModule(normRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", newNormaliser.getID());

        //create the second report module and add it to the workflow
        ReportWorkflowModule secondReportModule = new ReportWorkflowModule("SECONDREPORT", visualBounds);
        secondReportModule.disableTool(ReportWorkflowModule.QCTool.JACCARD);
       
        // second report should not be used on unmapped data
        secondReportModule.setMappedOnly();

        secondReportModule.setPos(860, (float) visualBounds.getMaxY() / 4);
        ReportWorkflowModuleRunner secondReportRunner = new ReportWorkflowModuleRunner(secondReportModule);

        WorkflowManager.getInstance().addModule(secondReportRunner);

        WorkflowManager.getInstance().connectModule2Module(newNormaliser.getID(), secondReportModule.getID());

        //create the offset review module and add it to the workflow
        OffsetReviewWorkflowModule off_rev = new OffsetReviewWorkflowModule("OFFSETREVIEW", visualBounds);
        off_rev.setPos(1060, (float) visualBounds.getMaxY() / 4);

        OffsetReviewWorkflowModuleRunner off_rev_runner = new OffsetReviewWorkflowModuleRunner(off_rev);

        WorkflowManager.getInstance().addModule(off_rev_runner);

        WorkflowManager.getInstance().connectModule2Module(secondReportModule.getID(), off_rev.getID());

        //create the DE module and add it
        DifferentialExpressionWorkflowModule d_e = new DifferentialExpressionWorkflowModule("DIFFERENTIALEXPRESSION", visualBounds);
        d_e.setPos(1260, (float) visualBounds.getMaxY() / 4);

        DifferentialExpressionWorkflowModuleRunner d_e_runner = new DifferentialExpressionWorkflowModuleRunner(d_e);

        WorkflowManager.getInstance().addModule(d_e_runner);

        WorkflowManager.getInstance().connectModule2Module(off_rev.getID(), d_e.getID());

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "Norm_DE.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        

        return "";

    }

    public static String createQC_DE_NO_QC_Workflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        //now create the first report on the initial state of the data
        List<NormalisationType> norms = new ArrayList<>();
        norms.add(NormalisationType.NONE);

        //create the first report module and add it to the workflow
//        ReportWorkflowModule firstReportModule = new ReportWorkflowModule("FIRST_REPORT", norms, visualBounds);
//        firstReportModule.setPos(260, (float) visualBounds.getMaxY() / 4);
//        ReportWorkflowModuleRunner firstReportRunner = new ReportWorkflowModuleRunner(firstReportModule);
//
//        WorkflowManager.getInstance().addModule(firstReportRunner);
//
//        WorkflowManager.getInstance().connectModule2Module("DataBase", "FIRST_REPORT");
//
//        //create the file review module and add it to the workflow
//        FileReviewWorkflowModule fileReviewMod = new FileReviewWorkflowModule("FILE_REVIEW", visualBounds);
//        fileReviewMod.setPos(460, (float) visualBounds.getMaxY() / 4);
//        FileReviewWorkflowModuleRunner fileRevRunner = new FileReviewWorkflowModuleRunner(fileReviewMod);
//
//        WorkflowManager.getInstance().addModule(fileRevRunner);
//
//        WorkflowManager.getInstance().connectModule2Module("FIRST_REPORT", "FILE_REVIEW");
        // We are normalising everything aligned to the genome reference sequence only for now. This can be modified to allow specified annotations to be normalised, however
        //ReferenceSequenceManager genomeReference = new ReferenceSequenceManager(ReferenceSequenceManager.GENOME_REFERENCE_NAME);
        NormalisationWorkflowServiceModule newNormaliser = new NormalisationWorkflowServiceModule(
                "NORMALISER", visualBounds);
        newNormaliser.setPos(660, (float) visualBounds.getMaxY() / 4);
//
        NormalisationWorkflowServiceModuleRunner normRunner = new NormalisationWorkflowServiceModuleRunner(newNormaliser);

        WorkflowManager.getInstance().addModule(normRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", "NORMALISER");

//        //create the second report module and add it to the workflow
//        ReportWorkflowModule secondReportModule = new ReportWorkflowModule("SECOND_REPORT", visualBounds);
//        secondReportModule.setPos(860, (float) visualBounds.getMaxY() / 4);
//        ReportWorkflowModuleRunner secondReportRunner = new ReportWorkflowModuleRunner(secondReportModule);
//
//        WorkflowManager.getInstance().addModule(secondReportRunner);
//
//        WorkflowManager.getInstance().connectModule2Module("NORMALISER", "SECOND_REPORT");
//        
        //create the offset review module and add it to the workflow
        OffsetReviewWorkflowModule off_rev = new OffsetReviewWorkflowModule("OFFSETREVIEW", visualBounds);
        off_rev.setPos(1060, (float) visualBounds.getMaxY() / 4);

        OffsetReviewWorkflowModuleRunner off_rev_runner = new OffsetReviewWorkflowModuleRunner(off_rev);

        WorkflowManager.getInstance().addModule(off_rev_runner);

        WorkflowManager.getInstance().connectModule2Module("NORMALISER", "OFFSETREVIEW");

        //create the DE module and add it
        DifferentialExpressionWorkflowModule d_e = new DifferentialExpressionWorkflowModule("DIFFERENTIALEXPRESSION", visualBounds);
        d_e.setPos(1260, (float) visualBounds.getMaxY() / 4);

        DifferentialExpressionWorkflowModuleRunner d_e_runner = new DifferentialExpressionWorkflowModuleRunner(d_e);

        WorkflowManager.getInstance().addModule(d_e_runner);

        WorkflowManager.getInstance().connectModule2Module("OFFSETREVIEW", "DIFFERENTIALEXPRESSION");

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "QualityCheck_NOQC_DE.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return "";

    }

    //miRCat 1 and 2
    public static String createMirCatWorkflow(Rectangle2D visualBounds, boolean writeToFile, Path location, int threadCount) {

        WorkflowManager.getInstance().reset();

        WorkflowManager.getInstance().setName("mircat");

        //global container lists for the datatypes that will  be accessed in the workflow. GENOME here
        WorkflowManager.getInstance().addInputDataContainerList("genome", WorkflowManager.CompatibilityKey.GENOME, 1, 1);

        //sRNA query list
        WorkflowManager.getInstance().addInputDataContainerList("srnaQuery", CompatibilityKey.sRNA_QUERY, 1, -1);

        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);

        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);

        //create the first report module and add it to the workflow
        MiRCatModule miRCatModule = new MiRCatModule("miRCat", "miRCat", visualBounds);
        miRCatModule.setPos(200, (float) visualBounds.getMaxY() / 4);
        miRCatModule.setOutputDir(location.toFile());
        miRCatModule.setOutputToFileMode(false);
        miRCatModule.setRunDatabaseMode(true);
        miRCatModule.setWriteToFileMode(false);
        miRCatModule.setThreadCount(threadCount);
        MiRCatModuleRunner miRCatRunner = new MiRCatModuleRunner(miRCatModule);

        WorkflowManager.getInstance().addModule(miRCatRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", "miRCat");

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "MirCatWorkflow.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return "failed creation of miRCat Workflow";

    }
    
    public static String createMiRCat2Workflow(Rectangle2D visualBounds) throws IOException, FileNotFoundException, DuplicateIDException{
        WorkflowManager.getInstance().reset();
        


        //This method should be called after the file hierarchy has been finalised, that is, no new files can be added!
        //initial to the workflow should be the database/filehierarchy
        //Database pulls in the data within the construct method
        //create the database workflow module
        DatabaseWorkflowRunner DB_runner = new DatabaseWorkflowRunner();
        WorkflowManager.getInstance().addModule(DB_runner);
        
        //limit samples and replicates
        WorkflowManager.getInstance().maxSamples = 1;
        WorkflowManager.getInstance().maxReplicates = 1;
        
        
        //set size of file hierarchy web view
        DatabaseWorkflowModule.getInstance().setFrameSize(visualBounds);
                    

        DatabaseWorkflowModule.getInstance().setPos(60, (float) visualBounds.getMaxY() / 4.0f);
        
        //create the first report module and add it to the workflow
        MiRCat2Module miRCat2Module = new MiRCat2Module("miRCat2", "MIRCAT2", visualBounds);
        miRCat2Module.setPos(200, (float) visualBounds.getMaxY() / 4.0f);
        
        
        miRCat2ModuleRunner miRCat2ModuleRunner = new miRCat2ModuleRunner(miRCat2Module);
        
        

        WorkflowManager.getInstance().addModule(miRCat2ModuleRunner);

        WorkflowManager.getInstance().connectModule2Module("Database", "miRCat2");
  
        try
        {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "miRCat2.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        return "failed creation of miRCat 2 Workflow";
        
    }

    public static String runCommandLineWorkflowMiRCat2(String workflow, File jsonPath) {
        String str = "";
        JsonObject jsonObject = null;

        AppUtils.INSTANCE.setCommandLine(true);


        try
        {
            jsonObject = JsonUtils.parseJsonFile(jsonPath);
        }
        catch (Exception ex)
        {
            MessageBox.messageJFX("Workbench Error", "Unable to load workflow", "There was an error parsing the JSON file: " + ex);
        }
        try
        {
            str = createMiRCat2Workflow(Rectangle2D.EMPTY);

            WorkflowManager.getInstance().setUpDatabase(jsonObject);
            WorkflowManager.getInstance().configureMiRCat2(jsonObject);
        }
        catch (IOException | DuplicateIDException ex)
        {
            MessageBox.messageJFX("Workbench Error", "Unable to load workflow", "There was an error loading the workflow: " + ex);
        }
        return str;
    }

    //Degradome 2 Workflows
    
    public static String createDegradome2Workflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();
        
        clearOldJSONS();
        
        WorkflowManager.getInstance().setFirstModuleTitle("FileManager");
        
        
        //create the file manager module and add it to the workflow
        FileManagerWorkflowModule fm_module = new FileManagerWorkflowModule("FileManager", visualBounds);
        fm_module.setPos(60, (float) visualBounds.getMaxY() / 4);
        FileManagerWorkflowModuleRunner fm_runner = new FileManagerWorkflowModuleRunner(fm_module);
        WorkflowManager.getInstance().addModule(fm_runner);

        
        //create the filter module and add it to the workflow
        DegradomeToolWorkflowModule deg_module = new DegradomeToolWorkflowModule("Degradome2", visualBounds);
        deg_module.setPos(260, (float) visualBounds.getMaxY() / 4);
        DegradomeToolWorkflowModuleRunner deg_runner = new DegradomeToolWorkflowModuleRunner(deg_module);
        WorkflowManager.getInstance().addModule(deg_runner);

        WorkflowManager.getInstance().connectModule2Module("FileManager", "Degradome2");

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FM_Deg.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        

        return "";

    }

    //filters
    public static String createFM_FilterWorkflow(Rectangle2D visualBounds) {

        WorkflowManager.getInstance().reset();
        
        clearOldJSONS();
        
        WorkflowManager.getInstance().setFirstModuleTitle("FileManager");
        
        
        //create the file manager module and add it to the workflow
        FileManagerWorkflowModule fm_module = new FileManagerWorkflowModule("FileManager", visualBounds);
        fm_module.setPos(60, (float) visualBounds.getMaxY() / 4);
        FileManagerWorkflowModuleRunner fm_runner = new FileManagerWorkflowModuleRunner(fm_module);
        WorkflowManager.getInstance().addModule(fm_runner);

        
        //create the filter module and add it to the workflow
        Filter2WorkflowModule filter_module = new Filter2WorkflowModule("Filter", visualBounds);
        filter_module.setPos(260, (float) visualBounds.getMaxY() / 4);
        Filter2WorkflowModuleRunner filter_runner = new Filter2WorkflowModuleRunner(filter_module);
        WorkflowManager.getInstance().addModule(filter_runner);

        WorkflowManager.getInstance().connectModule2Module("FileManager", "Filter");

        try {
            File jsonFile = new File(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json" + DIR_SEPARATOR + "FM_FILTER.json");
            WorkflowManager.getInstance().outputJsonFile(jsonFile);

            // return a file URL which appears to be more compatible with D3 javascript on windows
            return jsonFile.toURI().toURL().toString();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        

        return "";

    }

    public static void clearOldJSONS()
    {
        try
        {
            Path jsonPath = Paths.get(Tools.WEB_SCRIPTS_DIR.getAbsolutePath() + DIR_SEPARATOR + "json");
            FileUtils.deleteDirectory(jsonPath.toFile());
            Files.createDirectory(jsonPath);
        }
        catch (IOException ex)
        {
            Logger.getLogger(PreconfiguredWorkflows.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Test harness
    public static void main(String[] args) {

        Platform.runLater(() -> {

//                try
//                {
            //DatabaseWorkflowModule.getInstance().setDebugMode(true);
            try {
                
                
                //createMiRCat2Workflow(new Rectangle2D(0, 0, 0, 0));
                //createQCWorkflow(new Rectangle2D(0, 0, 0, 0));
                createQC_DE_Workflow(new Rectangle2D(0, 0, 0, 0));
                // createMirCatWorkflow(new Rectangle2D(0, 0, 0, 0), false, Paths.get("/Developer/Applications/sRNAWorkbench/TestingData/DB_MC_test"), 1);
                //createMiRPAREWorkflow(new Rectangle2D(0, 0, 0, 0));
                //runCommandLineWorkflowMiRCat2("MiRCat2", new File("/Developer/Applications/sRNAWorkbench/TestingData/miRCat2Testing/mircat2_test_configuration.json"));

                HTMLWizardViewController.configureWorkflowData();
            } catch (Exception ex) {
                Logger.getLogger(PreconfiguredWorkflows.class.getName()).log(Level.SEVERE, null, ex);
            }
            WorkflowManager.getInstance().start();

            //Socket clientSocket = new Socket("localhost", 8000);
//                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//                    outToServer.writeBytes("close" + LINE_SEPARATOR);
//                    
//                    System.out.println("fin");
            //System.exit(0);
            //StandardServiceRegistryBuilder.destroy(serviceRegistry);
//                }
//                catch (IOException ex)
//                {
//                    Logger.getLogger(PreconfiguredWorkflows.class.getName()).log(Level.SEVERE, null, ex);
//                }
        });

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        createQCWorkflow(new Rectangle2D(0, 0, 0, 0));
        //WorkflowManager.getInstance().start();
        try (Socket clientSocket = new Socket("localhost", 8000)) {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.writeBytes("close" + LINE_SEPARATOR);

            System.out.println("fin");
        }
        System.exit(0);
        //StandardServiceRegistryBuilder.destroy(serviceRegistry);

    }
}
