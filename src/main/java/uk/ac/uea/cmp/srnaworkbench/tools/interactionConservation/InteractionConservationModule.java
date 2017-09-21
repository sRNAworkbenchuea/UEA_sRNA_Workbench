/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.interactionConservation;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import javafx.geometry.Rectangle2D;
import org.apache.commons.io.IOUtils;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;
import uk.ac.uea.cmp.srnaworkbench.exceptions.CompatibilityException;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainer;
import uk.ac.uea.cmp.srnaworkbench.workflow.DataContainerList;
import uk.ac.uea.cmp.srnaworkbench.exceptions.DuplicateIDException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.InitialisationException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.MaximumCapacityException;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowManager.CompatibilityKey;
import uk.ac.uea.cmp.srnaworkbench.workflow.WorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.exceptions.WFModuleFailedException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQLFormatException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQLQueryLockedException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQueryComplex;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuerySimple;

/**
 *
 * @author Chris Applegate
 */
public class InteractionConservationModule extends WorkflowModule {

    private static final String INTERACTION_CONSERVATION_FXML = "InteractionConservationScene.fxml";
    // input query
    private final DataContainerList<HQLQuerySimple> in_query;
    // output query
    private final DataContainerList<HQLQuerySimple> out_query;
    // interacton conservation specific controller
    private final InteractionConservationController interactionConservationController;
    // flag to store the status of the  module completion
    private boolean complete;

    /*
     * constructor
     * @param id: unique ID for module (must only contain alpha-numeric characters)
     * @param title: descriptive text for module for display purposes
     */
    public InteractionConservationModule(final String id, final String title, Rectangle2D visualBounds) throws InitialisationException {
        super(id, title);
        this.complete = false;
        this.in_query = new DataContainerList<>("input", CompatibilityKey.INTERACTION_QUERY, 2, -1);
        this.out_query = new DataContainerList<>("output", CompatibilityKey.INTERACTION_QUERY, 1, 1);
        try {

            this.out_query.add(new DataContainer<>(CompatibilityKey.INTERACTION_QUERY, new HQLQuerySimple(Interaction_Entity.class)));//DetachedCriteria.forClass(Interaction_Entity.class)));
            addInputDataContainerList(this.in_query);
            addOutputDataContainerList(this.out_query);
        } catch (MaximumCapacityException | CompatibilityException | InitialisationException | DuplicateIDException ex) {
            throw new InitialisationException("Interaction module initialisation exception: " + ex);
        }
        this.setFXMLResource(IOUtils.DIR_SEPARATOR + "fxml" + IOUtils.DIR_SEPARATOR + INTERACTION_CONSERVATION_FXML);
        this.controller = interactionConservationController = new InteractionConservationController(this);
    }

    /*
     * returns the completion status of the module
     */
    public boolean isComplete() {
        return complete;
    }

    public int getNumInteractionInputs()
    {
        return this.in_query.getListLength();
    }
    
    /*
     * returns a descriptive query for interaction conservation for display purposes
     */
    public HQLQueryComplex generateOutput() throws HQLQuery.HQLFormatException, Exception {
        // put all of the input sRNA sequence queries in a list
        List<HQLQuerySimple> in_queries = new LinkedList<>();
        for (int i = 0; i < this.in_query.getListLength(); i++) {
            in_queries.add(this.in_query.getContainer(i).getData());
        }
        return generateInfoQuery(in_queries);
    }

    /*
     * generates output conserved interaction query from list of input interaction queries
     * @param in_queries: list of input interaction queries
     * @param outQuery: the output query
     */
    private static void generateConservedQuery(final List<HQLQuerySimple> in_queries, final HQLQuerySimple outQuery) throws FileNotFoundException, HQLQuery.HQLFormatException, HQLQuery.HQLQueryLockedException, Exception {
        String nConserved = "";
        for (int i = 0; i < in_queries.size(); i++) {
            // get the input interaction query
            HQLQuerySimple inputInteractionQuery = in_queries.get(i);
            // get the IDs of the input interaction for the current query
            HQLQueryComplex inputInteractionIDQuery = new HQLQueryComplex(inputInteractionQuery);
            inputInteractionIDQuery.addSelect(inputInteractionQuery.getFromAlias() + ".id", "id");
            // how may times is the interaction in the input interaction query
            String countQuery = String.format("SELECT count(*) FROM Interaction_Entity B WHERE A.gene = B.gene and A.cleavagePos = B.cleavagePos and A.sRNA = B.sRNA and B.id IN (%s) GROUP BY gene, cleavagePos, sRNA", inputInteractionIDQuery.eval());
            // is the interaction conserved?
            if (i > 0) {
                nConserved += " + ";
            }
            nConserved += String.format("CASE WHEN coalesce((%s),0) = 0 THEN 0 ELSE 1 END", countQuery);
        }
        // query must project on records that are conserved (in more than 1 input interaction file)
        outQuery.addWhere(String.format("(%s) > 1", nConserved));
    }

    /*
     * returns a descriptive query for interaction conservation for display purposes
     * @param in_queries: a list of input interactions to check for conservation
     */
    private static HQLQueryComplex generateInfoQuery(final List<HQLQuerySimple> in_queries) throws HQLFormatException, HQLQueryLockedException, Exception {

        HQLQueryComplex infoQuery = new HQLQueryComplex();
        infoQuery.addSelect("A.gene.gene", "Transcript");
        infoQuery.addSelect("A.cleavagePos", "Cleavage_Pos");
        infoQuery.addSelect("A.sRNA.RNA_Sequence", "sRNA");

        String nConserved = "";
        for (int i = 0; i < in_queries.size(); i++) {
            // get the input interaction query
            HQLQuerySimple inputInteractionQuery = in_queries.get(i);
            // get the IDs of the input interaction for the current query
            HQLQueryComplex inputInteractionIDQuery = new HQLQueryComplex(inputInteractionQuery);
            inputInteractionIDQuery.addSelect(inputInteractionQuery.getFromAlias() + ".id", "id");
            // how may times is the interaction in the input interaction query
            String countQuery = String.format("SELECT count(*) FROM Interaction_Entity B WHERE A.gene = B.gene and A.cleavagePos = B.cleavagePos and A.sRNA = B.sRNA and B.id IN (%s) GROUP BY gene, cleavagePos, sRNA", inputInteractionIDQuery.eval());
            infoQuery.addSelect(String.format("coalesce((%s),0)", countQuery), "In_PAREsnip_" + (i + 1));
            // is the interaction conserved?
            if (i > 0) {
                nConserved += " + ";
            }
            nConserved += String.format("CASE WHEN coalesce((%s),0) = 0 THEN 0 ELSE 1 END", countQuery);
            //nConserved += String.format("CASE WHEN cast(coalesce((%s),0) as string) = '0' THEN 'no' ELSE 'YES' END", countQuery);
        
        }
        infoQuery.addSelect(nConserved, "Num_Conserved");
        infoQuery.addFrom(Interaction_Entity.class, "A");
        infoQuery.addGroup("gene");
        infoQuery.addGroup("Cleavage_Pos");
        infoQuery.addGroup("sRNA");
        infoQuery.addOrder("gene", HQLQuery.HQL_DIR.DESC);
        return infoQuery;
    }

    @Override
    protected void process() throws WFModuleFailedException, HQLFormatException, HQLQueryLockedException, Exception {
        this.interactionConservationController.write2Log("INFORMATION: Interaction Conservation module started.");
        this.interactionConservationController.write2Log("INFORMATION: Querying lists of interactions.");
        // put all of the input sRNA sequence queries in a list
        List<HQLQuerySimple> in_queries = new LinkedList<>();
        for (int i = 0; i < this.in_query.getListLength(); i++) {
            in_queries.add(this.in_query.getContainer(i).getData());
            this.interactionConservationController.write2Log("INFORMATION: Using PAREsnip Result: " + (i + 1));
        }
        // generate the conservation query and store the result in the output sRNA query
        this.interactionConservationController.write2Log("INFORMATION: Performing conservation query (this may take a few minutes...)");
        generateConservedQuery(in_queries, this.out_query.getContainer(0).getData());
        // lock the ouput sRNA sequence query so that other modules cannot modify this
        this.out_query.getContainer(0).getData().lock();
        // set the module completion flag to true
        this.complete = true;
        // update any GUI
        this.interactionConservationController.updateUI();
        this.interactionConservationController.write2Log("INFORMATION: Conservation module completed.");
    }
}
