package uk.ac.uea.cmp.srnaworkbench.tools.mirplant;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Aligned_Sequences_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Precursor_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Prediction_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.PredictionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

public class MiRPlantModule extends WorkflowModule {

    // controller
    private final MiRPlantController mirPlantController;
    // path of the miRPlant output file
    private Path miRPlantPath;
    // flag storing whether the module has completed its processing
    private boolean isComplete;
    // output
    private final DataContainerList<HQLQuerySimple> out_predictionQuery;

    // constructor
    public MiRPlantModule(String id, String title) {
        super(id, title);
        // initialise
        this.isComplete = false;
        // set controller
        this.mirPlantController = new MiRPlantController(this);
        this.controller = mirPlantController;
        // set fxml scene
        setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + "MiRPlantScene.fxml");
        // set-up output
        this.out_predictionQuery = new DataContainerList<>("predictionQuery", WorkflowManager.CompatibilityKey.PREDICTION_QUERY, 1, 1);
        try {
            this.out_predictionQuery.add(new DataContainer<>(WorkflowManager.CompatibilityKey.PREDICTION_QUERY, new HQLQuerySimple(Prediction_Entity.class)));
            addOutputDataContainerList(this.out_predictionQuery);
        } catch (CompatibilityException | MaximumCapacityException | InitialisationException | DuplicateIDException ex) {
            Logger.getLogger(MiRPlantModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void process() throws FileNotFoundException, HQLQuery.HQLFormatException, DuplicateIDException, Exception {

        // parse the input file
        if (miRPlantPath != null) {
            parseMiRPlantFile();
        }
        // create query
        HQLQuerySimple outQuery = this.out_predictionQuery.getContainer(0).getData();
        outQuery.addWhere("A.predictor = '" + this.getID() + "'");
        outQuery.lock();
        
        // set completion flag
        this.isComplete = true;
        // update the UI
        this.mirPlantController.updateUI();
    }

    private List<Prediction_Entity> parseMiRPlantLine(String line) throws IOException, FileNotFoundException, InterruptedException {

        List<Prediction_Entity> predictions = new ArrayList<>();
        AlignedSequenceServiceImpl alignedService = (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");

        String[] fields = line.split("[\t]");
        String chr = fields[2];
        String strand = fields[3];
        String precursorSeq = fields[7].toUpperCase();

        String[] precursorIndexStr = fields[4].split("[-]");
        int precursor_sIndex = Integer.parseInt(precursorIndexStr[0]);
        int precursor_eIndex = Integer.parseInt(precursorIndexStr[1]);

        String matureSeq = fields[9].toUpperCase();
        String[] matureIndexStr = fields[6].split("[-]");
        int mature_sIndex = Integer.parseInt(matureIndexStr[0]);
        int mature_eIndex = Integer.parseInt(matureIndexStr[1]);

        Aligned_Sequences_Entity mature_alignment = alignedService.findOrCreate("", chr, matureSeq, mature_sIndex, mature_eIndex, strand, 0);
        Aligned_Sequences_Entity precursor_alignment = alignedService.findOrCreate("", chr, precursorSeq, precursor_sIndex, precursor_eIndex, strand, 0);
        Precursor_Entity precursor = new Precursor_Entity();
        precursor.setStructure(fields[8]);
        precursor.setAlignment(precursor_alignment);

        String starStr = fields[11];
        String[] starDetails = starStr.split("[;]");
        for (String s : starDetails) {
            if (!s.isEmpty()) {
                s = s.replaceAll("[|+-]", " ");
                String[] location = s.split("[ ]");
                int star_sIndex = Integer.parseInt(location[2]);
                int star_eIndex = Integer.parseInt(location[3]);
                int offset_start = star_sIndex - precursor_sIndex;
                int offset_end = star_eIndex - precursor_sIndex;

                // is the star sequence entirely within the precursor?
                if (star_sIndex >= precursor_sIndex && star_eIndex <= precursor_eIndex) {
                    // does the star sequence overlap with the mature (if so: probably not the star so reject)
                    if (!(star_sIndex <= mature_eIndex && mature_sIndex <= star_eIndex)) {
                        String starSeq;
                        if (strand.equals("+")) {
                            starSeq = precursorSeq.substring(offset_start, offset_end + 1);
                        } else {
                            String rev = StringUtils.reverseString(precursorSeq);
                            starSeq = rev.substring(offset_start, offset_end + 1);
                        }

                        Prediction_Entity prediction = new Prediction_Entity();
                        prediction.setPredictor(this.getID());
                        prediction.setMature(mature_alignment);

                        Aligned_Sequences_Entity star_alignment = alignedService.findOrCreate("", chr, starSeq, star_sIndex, star_eIndex, strand, 0);
                        if (!mature_alignment.isSame(star_alignment)) {
                            prediction.setStar(star_alignment);
                        }
                        prediction.setPrecursor(precursor);
                        precursor.computeMFE();
                        predictions.add(prediction);
                    }
                }
            }
        }
        // if no predictions have been added (perhaps there are no star sequences or all stars were invalid), add just mature
        if (predictions.isEmpty()) {
            Prediction_Entity prediction = new Prediction_Entity();
            prediction.setPredictor(this.getID());
            prediction.setMature(mature_alignment);
            prediction.setPrecursor(precursor);
            precursor.computeMFE();
            predictions.add(prediction);
        }
        return predictions;
    }

    private void parseMiRPlantFile() throws FileNotFoundException, IOException, DuplicateIDException, Exception {

        PredictionServiceImpl prediction_service = (PredictionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("PredictionService");

        Scanner in = new Scanner(this.miRPlantPath.toFile());
        // remove header line
        in.nextLine();
        int lineNumber = 2;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            try {
                List<Prediction_Entity> predictions = parseMiRPlantLine(line);
                for (Prediction_Entity prediction : predictions) {
                    try {
                        prediction_service.save(prediction);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "miRPlant ERROR: Was unable to to persit prediction to DB", ex);
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "miRPlant ERROR: Was unable to parse line " + lineNumber + " of input ", ex);
            }
            lineNumber++;

        }
        in.close();
    }

    public HQLQuery generateOutput() throws HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException {

        HQLQueryComplex hqlQuery = new HQLQueryComplex();
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
        hqlQuery.addFrom(Prediction_Entity.class, "P");
        hqlQuery.addWhere("P.predictor = '" + this.getID() + "'");
        return hqlQuery;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public void setPath(Path path) {
        this.miRPlantPath = path;
    }

}
