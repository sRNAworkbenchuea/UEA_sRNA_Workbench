/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.srnaConservation;

import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.AlignedSequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.InteractionServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQLFormatException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;

/**
 *
 * @author Chris Applegate
 */
public class ConservationModule extends WorkflowModule {
    
    private final static String CONSERVATION_FXML = "ConservationScene.fxml";
    // output query data container
    private final DataContainerList<HQLQuerySimple> out_query;
    // controller specific to sRNA conservation module
    private final ConservationController srnaConservationController;
    // flag representing module completion status
    private boolean complete;
    // list of input queries (internal representation)
    private List<HQLQuerySimple> in_queries;
    // list of sRNA filenames
    private List<String> filenames;

    /*
     * Contructor
     * @param id: unique ID for the module (must only contain alpha numeric characters)
     * @param title: descriptive string for the module for display purposes
     */
    public ConservationModule(final String id, final String title, Rectangle2D visualBounds) throws InstantiationException {
        super(id, title);
        this.in_queries = new LinkedList<>();
        this.filenames = null;
        this.complete = false;
        this.out_query = new DataContainerList<>("output", CompatibilityKey.sRNA_QUERY, 1, 1);
        try {
            this.out_query.add(new DataContainer<>(CompatibilityKey.sRNA_QUERY, new HQLQuerySimple(Sequence_Entity.class)));
            addOutputDataContainerList(this.out_query);
            this.srnaConservationController = new ConservationController(this);
            this.controller = srnaConservationController;
            
        } catch (MaximumCapacityException | CompatibilityException | InitialisationException | DuplicateIDException ex) {
            throw new InstantiationException("Conservation module could not be initialised: " + ex);
        }
        this.setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + CONSERVATION_FXML);
        
    }

    /* 
     * returns the completion status of the module
     */
    public boolean isComplete() {
        return complete;
    }

    /*
     * returns a descriptive sRNA sequence conservation query for display purposes (GUI)
     */
    public HQLQuery generateOutput() throws HQLFormatException, Exception {
        // make the alises for the input sRNA queries the sRNA filenames
        List<String> aliases = this.filenames;
        // loop through the aliases 
        for (int i = 0; i < aliases.size(); i++) {
            String alias = aliases.get(i);

            // replace all invalid characters with underscores
            alias = alias.replaceAll("[^a-zA-Z0-9]", "_");
            aliases.set(i, alias);
        }
        // generate the query and return it
        return generateInfoQuery(this.in_queries, aliases);
    }

    /*
     * generates output conserved sRNA query from list of input sRNA sequence queries
     * @param in_queries: list of input sRNA queries
     * @param outQuery: the output query
     */
    private static void generateConservedQuery(final List<HQLQuerySimple> in_queries, final HQLQuerySimple outQuery) throws HQLFormatException, Exception {
        String nConserved = "";
        for (int i = 0; i < in_queries.size(); i++) {
            // get the input sRNA query
            HQLQueryComplex q = new HQLQueryComplex(in_queries.get(i));
            // get the IDs of the input sRNA sequence for the current query
            q.addSelect(in_queries.get(i).getFromAlias() + ".sequenceId", "sequenceId");
            // determine abundance in the respective file
            String abundance = String.format("SELECT sum(B.abundance) FROM Sequence_Entity B WHERE A.RNA_Sequence = B.RNA_Sequence and B.sequenceId IN (%s) GROUP BY RNA_Sequence", q.eval());
            // is the sRNA sequence conserved?
            if (i > 0) {
                nConserved += " + ";
            }
            nConserved += String.format("CASE WHEN coalesce((%s),0) = 0 THEN 0 ELSE 1 END", abundance);
        }
        // query must project on records that are conserved (in more than 1 input sRNA file)
        outQuery.addWhere(String.format("(%s) > 1", nConserved));
    }

    /*
     * returns a descriptive query for sRNA conservation for display purposes
     * @param in_queries: a list of input interactions to check for conservation
     * @aliases: a list of aliases for the input sRNA queries (such as sRNA filenames)
     */
    public static HQLQuery generateInfoQuery(final List<HQLQuerySimple> in_queries, final List<String> aliases) throws HQLFormatException, Exception {
        // error check: ensure size of in queries and aliases are the same
        if (in_queries.size() != aliases.size()) {
            throw new Exception("Input queries list and alias list in sRNA Conservation Query is not equal in length.");
        }
        // create the info query
        HQLQueryComplex infoQuery = new HQLQueryComplex();
        infoQuery.addSelect("A.RNA_Sequence", "Sequence");
        // loop through the provided sRNA queries
        String nConserved = "";
        String sumAbundance = "";
        for (int i = 0; i < in_queries.size(); i++) {
            // get the input sRNA query
            HQLQueryComplex q = new HQLQueryComplex(in_queries.get(i));
            // get the IDs of the input sRNA sequence for the current query
            q.addSelect(in_queries.get(i).getFromAlias() + ".sequenceId", "id");
            // determine abundance in the respective file
            String abundance = String.format("SELECT sum(B.abundance) FROM Sequence_Entity B WHERE A.RNA_Sequence = B.RNA_Sequence and B.sequenceId IN (%s) GROUP BY RNA_Sequence", q.eval());
            if (i > 0) {
                nConserved += " + ";
                sumAbundance += " + ";
            }
            // is the sRNA sequence conserved?
            nConserved += String.format("CASE WHEN coalesce((%s),0) = 0 THEN 0 ELSE 1 END", abundance);
            // sum the total abundance
            sumAbundance += String.format("coalesce((%s),0)", abundance);
            // add this to the info query
            infoQuery.addSelect(String.format("coalesce((%s),0)", abundance), "Abundance_in_" + aliases.get(i));
        }
        // complete and return the query
        infoQuery.addSelect(sumAbundance, "Total");
        infoQuery.addSelect(nConserved, "Num_Conserved");
        infoQuery.addFrom(Sequence_Entity.class, "A");
        infoQuery.addGroup("RNA_Sequence");
        infoQuery.addOrder("Total", HQLQuery.HQL_DIR.DESC);
        return infoQuery;
    }
    
    @Override
    protected void process() throws WFModuleFailedException, HQLFormatException, Exception {
     //   printAnnotations();
// update output log in GUI
        this.srnaConservationController.write2Log("INFORMATION: sRNA Conservation module started.");
        this.srnaConservationController.write2Log("INFORMATION: Querying lists of sRNA files.");
        // get the sequence service and retrieve a list of unique sRNA input filenames
        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");
        this.filenames = sequence_service.getUniqueListOfFilenames();
        // create a separate sRNA sequence query per file
        for (String filename : this.filenames) {
            HQLQuerySimple q = new HQLQuerySimple(Sequence_Entity.class);
            q.addWhere("A.filename = '" + filename + "'");
            this.in_queries.add(q);
            this.srnaConservationController.write2Log("INFORMATION: Using file: " + filename);
        }
        this.srnaConservationController.write2Log("INFORMATION: Performing conservation query (this may take a few minutes...)");
        // generate the sRNA conservation query
        generateConservedQuery(this.in_queries, this.out_query.getContainer(0).getData());
        // lock the outgoing query so that other modules cannot modify this query
        this.out_query.getContainer(0).getData().lock();
        // set the completion flag
        this.complete = true;
        // update any GUI
        this.srnaConservationController.updateUI();
        // update the output log in GUI
        this.srnaConservationController.write2Log("INFORMATION: Conservation module completed.");
    }

    // chris added temp
    private void printAnnotations() {
        
        InteractionServiceImpl is = (InteractionServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("InteractionService");
    //    alignedService.printAlignments();
        //     AlignedSequenceServiceImpl alignedService = (AlignedSequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("AlignedSequenceService");
        //    alignedService.printAlignments();
        // alignedService.
        List<Map<String, Object>> i = is.executeGenericSQL("SELECT * FROM ANNOTATION_TYPES");
        for (Map<String, Object> row : i) {
            for (String key : row.keySet()) {
                System.out.println("key: " + key + " -> " + row.get(key));
            }
        }
    }
}
